package org.scp.gymlog.ui.common

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import org.scp.gymlog.util.Constants.IntentReference

open class CustomFragment : Fragment() {

    companion object {
        private const val INTENT_CALLER_ID = "_intentCallerId"
    }

    private var intentResultId = IntentReference.NONE

    private val activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onActivityResult(intentResultId, result.data!!)
            }
            intentResultId = IntentReference.NONE
    }

    open fun onActivityResult(intentReference: IntentReference, data: Intent) {}

    protected fun startActivityForResult(intent: Intent, intentReference: IntentReference) {
        checkEmptyIntent()
        if (intentReference !== IntentReference.NONE) {
            intentResultId = intentReference
            intent.putExtra(INTENT_CALLER_ID, intentReference.ordinal)
        }
        activityResultLauncher.launch(intent)
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