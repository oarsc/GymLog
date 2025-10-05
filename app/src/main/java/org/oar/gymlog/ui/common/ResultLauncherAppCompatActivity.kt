package org.oar.gymlog.ui.common

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import org.oar.gymlog.util.Constants.IntentReference

abstract class ResultLauncherAppCompatActivity: AppCompatActivity() {

    companion object {
        private const val INTENT_CALLER_ID = "_intentCallerId"
    }

    private var intentResultId = IntentReference.NONE

    private val activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            onActivityResult(intentResultId, result.data!!)
        }
        intentResultId = IntentReference.NONE
    }

    fun getIntentCall(): IntentReference {
        val mode = intent.extras?.getInt(INTENT_CALLER_ID) ?: 0
        return IntentReference.entries[mode]
    }

    open fun onActivityResult(intentReference: IntentReference, data: Intent) {}

    fun startActivityForResult(intent: Intent, intentReference: IntentReference, options: ActivityOptionsCompat? = null) {
        checkEmptyIntent()
        if (intentReference !== IntentReference.NONE) {
            intentResultId = intentReference
            intent.putExtra(INTENT_CALLER_ID, intentReference.ordinal)
        }
        activityResultLauncher.launch(intent, options)
    }

    protected fun startActivityForResult(intent: Intent) {
        checkEmptyIntent()
        activityResultLauncher.launch(intent)
    }

    protected fun startActivity(intent: Intent, intentReference: IntentReference) {
        checkEmptyIntent()
        if (intentReference !== IntentReference.NONE) {
            intent.putExtra(INTENT_CALLER_ID, intentReference.ordinal)
        }
        super.startActivity(intent)
    }

    override fun startActivity(intent: Intent) {
        checkEmptyIntent()
        super.startActivity(intent)
    }

    private fun checkEmptyIntent() {
        if (intentResultId !== IntentReference.NONE) {
            throw RuntimeException("Intent $intentResultId not captured")
        }
    }
}