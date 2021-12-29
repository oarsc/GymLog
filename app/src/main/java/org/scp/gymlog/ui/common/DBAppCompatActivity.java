package org.scp.gymlog.ui.common;

import android.os.Bundle;
import android.widget.Toast;

import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;

public abstract class DBAppCompatActivity extends CustomAppCompatActivity {
    protected static final int CONTINUE = 0;
    protected static final int ERROR_WITHOUT_MESSAGE = -1;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBThread.run(this, db -> {
            int result = onLoad(savedInstanceState, db);
            if (result == CONTINUE) {
                runOnUiThread(() -> onDelayedCreate(savedInstanceState));
            } else {
                if (result != ERROR_WITHOUT_MESSAGE) {
                    runOnUiThread(() ->
                            Toast.makeText(this, result, Toast.LENGTH_LONG).show());
                }
                finish();
            }
        });
    }

    protected abstract int onLoad(Bundle savedInstanceState, AppDatabase db);
    protected abstract void onDelayedCreate(Bundle savedInstanceState);
}
