package org.scp.gymlog.ui.common;

import android.os.Bundle;

import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;

public abstract class BackDBAppCompatActivity extends BackAppCompatActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new DBThread(this, db -> {
            onLoad(savedInstanceState, db);
            runOnUiThread( () -> onDelayedCreate(savedInstanceState));
        });
    }

    protected abstract void onLoad(Bundle savedInstanceState, AppDatabase db);
    protected abstract void onDelayedCreate(Bundle savedInstanceState);
}
