package org.oar.gymlog.ui

import android.app.ActivityOptions
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.oauth.DbxCredential
import kotlinx.coroutines.launch
import org.oar.gymlog.databinding.ActivityLoadBinding
import org.oar.gymlog.service.DataBaseDumperService
import org.oar.gymlog.service.dropbox.DropboxApiWrapper
import org.oar.gymlog.service.dropbox.DropboxOAuthUtil
import org.oar.gymlog.service.dropbox.DropboxUploadApiResponse
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.ui.main.MainActivity
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.getTimestampString
import org.oar.gymlog.util.WeightUtils
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.DatabaseExts.dbThreadSuspend
import org.oar.gymlog.util.extensions.MessagingExts.toast
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean
import org.oar.gymlog.util.extensions.PreferencesExts.loadDbxCredential
import org.oar.gymlog.util.extensions.PreferencesExts.loadString
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

class LoadActivity : BindingAppCompatActivity<ActivityLoadBinding>(ActivityLoadBinding::inflate) {

    private val dataBaseDumperService by lazy { DataBaseDumperService() }
    private val dropboxOAuthUtil by lazy { DropboxOAuthUtil(this) }
    private var onResumeActions = mutableListOf<() -> Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onResumeActions.clear()

        lifecycleScope.launch {
            if (importData() || exportData()) {
                goMain()
            } else if (!dropboxExportData()) {
                WeightUtils.setConvertParameters(
                    loadBoolean(PreferencesDefinition.UNIT_CONVERSION_EXACT_VALUE),
                    loadString(PreferencesDefinition.UNIT_CONVERSION_STEP))
                if (intent.action != "keep") goMain()
            }
        }
    }

    private suspend fun importData(): Boolean {
        val importUri = intent.extras?.getParcelable("import", Uri::class.java)
        if (importUri != null) {
            intent.removeExtra("import")

            dbThreadSuspend { db ->
                contentResolver.openInputStream(importUri).use { inputStream ->
                    dataBaseDumperService.load(this, inputStream!!, db)
                    Data.exercises.clear()
                }
            }
            return true
        } else {
            return false
        }
    }

    private suspend fun exportData(): Boolean {
        val exportUri = intent.extras?.getParcelable("export", Uri::class.java)
        if (exportUri != null) {
            intent.removeExtra("export")
            val fileName = getFileName(exportUri)

            dbThreadSuspend { db ->
                (contentResolver.openOutputStream(exportUri) as FileOutputStream)
                    .use { fileOutputStream ->
                        dataBaseDumperService.save(this, fileOutputStream, db)
                        toast("Saved \"$fileName\"")
                    }
            }
            return true
        }
        return false
    }

    private fun dropboxExportData() : Boolean {
        if (intent.action == "dropbox") {
            val dbxCredential = loadDbxCredential(PreferencesDefinition.DROPBOX_CREDENTIAL)

            if (dbxCredential != null) {
                uploadToDropbox(dbxCredential)
            } else {
                onResumeActions.apply {
                    add { dropboxOAuthUtil.startDropboxAuthorizationOAuth2(this@LoadActivity) }
                    add {
                        loadDbxCredential(PreferencesDefinition.DROPBOX_CREDENTIAL)
                            ?.also(this@LoadActivity::uploadToDropbox)
                            ?: run {
                                toast("Process cancelled")
                                goMain()
                            }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun uploadToDropbox(dbxCredential: DbxCredential) {
        dbThread { db ->
            val inputStream = dataBaseDumperService.save(this, null, db)!!
            val fileName = "output-${LocalDateTime.now().getTimestampString()}.json"

            lifecycleScope.launch {
                val dropboxApiWrapper = DropboxApiWrapper(
                    dbxCredential = dbxCredential,
                    clientIdentifier = "db-${Constants.Dropbox.APP_KEY}"
                )

                val response = dropboxApiWrapper.uploadFile(fileName, inputStream)
                when (response) {
                    is DropboxUploadApiResponse.Failure ->
                        toast("Error uploading file")
                    is DropboxUploadApiResponse.Success ->
                        toast("Uploaded \"${response.fileMetadata.name}\"")
                }
                goMain()
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT ->
                runCatching {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        cursor.moveToFirst()
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
                    } ?: ""
                }.getOrDefault("")
            else -> uri.path?.let(::File)?.name ?: ""
        }
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent, options.toBundle())
        finish()
    }

    override fun onResume() {
        super.onResume()
        dropboxOAuthUtil.onResume()
        onResumeActions.removeFirstOrNull()?.also { it() }
    }
}