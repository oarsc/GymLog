package org.scp.gymlog.ui.common;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import org.scp.gymlog.util.Constants.IntentReference;

public class CustomFragment extends Fragment {
    private static final String INTENT_CALLER_ID = "_intentCallerId";
    private IntentReference intentResultId = IntentReference.NONE;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    onActivityResult(intentResultId, result.getData());
                }
                intentResultId = IntentReference.NONE;
            });


    public void onActivityResult(IntentReference intentReference, Intent data) {}

    protected void startActivityForResult(Intent intent, IntentReference intentReference) {
        checkEmptyIntent();
        if (intentReference != IntentReference.NONE) {
            this.intentResultId = intentReference;
            intent.putExtra(INTENT_CALLER_ID, intentReference.ordinal());
        }
        activityResultLauncher.launch(intent);
    }

    protected void startActivityForResult(Intent intent) {
        checkEmptyIntent();
        activityResultLauncher.launch(intent);
    }

    protected void startActivity(Intent intent, IntentReference intentReference) {
        checkEmptyIntent();
        if (intentReference != IntentReference.NONE) {
            intent.putExtra(INTENT_CALLER_ID, intentReference.ordinal());
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        checkEmptyIntent();
        super.startActivity(intent);
    }

    private void checkEmptyIntent() {
        if (this.intentResultId != IntentReference.NONE) {
            throw new RuntimeException("Intent "+intentResultId+" not captured");
        }
    }
}
