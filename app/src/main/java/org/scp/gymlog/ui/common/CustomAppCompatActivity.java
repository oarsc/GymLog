package org.scp.gymlog.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.scp.gymlog.util.Constants.INTENT;

public abstract class CustomAppCompatActivity extends AppCompatActivity {
    public static final String INTENT_CALLER_ID = "_intentCallerId";

    private int intentResultId = INTENT.NONE;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    onActivityResult(intentResultId, result.getData());
                }
                intentResultId = INTENT.NONE;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public void onActivityResult(int intentResultId, Intent data) {}

    protected void startActivityForResult(int intentResultId, Intent intent) {
        if (this.intentResultId != INTENT.NONE) {
            throw new RuntimeException("Intent "+intentResultId+" not captured");
        }
        if (intentResultId != INTENT.NONE) {
            this.intentResultId = intentResultId;
            intent.putExtra(INTENT_CALLER_ID, intentResultId);
        }
        activityResultLauncher.launch(intent);
    }

    protected void startActivityForResult(Intent intent) {
        if (this.intentResultId != INTENT.NONE) {
            throw new RuntimeException("Intent "+intentResultId+" not captured");
        }
        activityResultLauncher.launch(intent);
    }

    protected void startActivity(int intentResultId, Intent intent) {
        if (this.intentResultId != INTENT.NONE) {
            throw new RuntimeException("Intent "+intentResultId+" not captured");
        }
        if (intentResultId != 0) {
            intent.putExtra(INTENT_CALLER_ID, intentResultId);
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        if (this.intentResultId != INTENT.NONE) {
            throw new RuntimeException("Intent "+intentResultId+" not captured");
        }
        super.startActivity(intent);
    }
}
