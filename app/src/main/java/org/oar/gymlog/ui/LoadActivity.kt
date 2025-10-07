package org.oar.gymlog.ui

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.oauth.DbxCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import org.oar.gymlog.util.extensions.DatabaseExts.db
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
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

    private val progressHandler = RangedProgress(this::updateProgress)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onResumeActions.clear()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (importData()) goMain()
                else if (exportData()) goMain()
                else if (!dropboxExportData()) {
                    WeightUtils.setConvertParameters(
                        loadBoolean(PreferencesDefinition.UNIT_CONVERSION_EXACT_VALUE),
                        loadString(PreferencesDefinition.UNIT_CONVERSION_STEP))
                    if (intent.action != "keep") goMain()
                }
            }
        }
    }

    private fun importData(): Boolean {
        val importUri = intent.extras?.getParcelable("import", Uri::class.java)
        if (importUri != null) {
            progressHandler.clear()
            intent.removeExtra("import")

            contentResolver.openInputStream(importUri).use { inputStream ->
                dataBaseDumperService.load(this, inputStream!!, db, progressHandler)
                Data.exercises.clear()
            }
            return true
        } else {
            return false
        }
    }

    private fun exportData(): Boolean {
        val exportUri = intent.extras?.getParcelable("export", Uri::class.java)
        if (exportUri != null) {
            progressHandler.clear()
            intent.removeExtra("export")
            val fileName = getFileName(exportUri)

            contentResolver.openOutputStream(exportUri).use { fileOutputStream ->
                dataBaseDumperService.save(this, fileOutputStream as FileOutputStream, db, progressHandler)
                toast("Saved \"$fileName\"")
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
            progressHandler.clear()
            progressHandler.setRange(0, 80)

            val inputStream = dataBaseDumperService.save(
                context = this,
                fos = null,
                database = db,
                progressNotify = progressHandler
            )!!

            updateProgress(80, "Upload")

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

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int, message: String? = null) {
        runOnUiThread {
            binding.progressBar.progress = progress
            message?.let {
                binding.message.text = "$it..."
            }
        }
    }
    override fun onResume() {
        super.onResume()
        dropboxOAuthUtil.onResume()
        onResumeActions.removeFirstOrNull()?.also { it() }
    }
}

class RangedProgress(private val updateCallback: (Int, String?) -> Unit) {
    private val ranges = mutableListOf<Pair<Int, Int>>()

    fun clear() = ranges.clear()
    fun setRange(min: Int, max: Int, message: String? = null) {
        ranges.add(0, min to max)
        update(0, message)
    }
    fun removeRange() = ranges.removeAt(0)
    fun replaceRange(min: Int, max: Int, message: String? = null) {
        removeRange()
        setRange(min, max, message)
    }

    fun update(progress: Int, message: String? = null) {
        val progressValue = ranges.fold(progress) { value, (min, max) ->
            val value = (max - min) * (value / 100f) + min
            value.toInt()
        }

        updateCallback(progressValue, message)
    }
}