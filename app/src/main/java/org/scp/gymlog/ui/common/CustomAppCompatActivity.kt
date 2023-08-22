package org.scp.gymlog.ui.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import org.scp.gymlog.util.Constants.IntentReference

open class CustomAppCompatActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //supportActionBar!!.setDisplayShowHomeEnabled(true);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun getIntentCall(): IntentReference {
        val mode = intent.extras?.getInt(INTENT_CALLER_ID) ?: 0
        return IntentReference.values()[mode]
    }

    open fun onActivityResult(intentReference: IntentReference, data: Intent) {}

    fun startActivityForResult(intent: Intent, intentReference: IntentReference) {
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