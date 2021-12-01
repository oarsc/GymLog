package org.scp.gymlog.ui.tools;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.scp.gymlog.R;

public abstract class BackAppCompatActivity extends AppCompatActivity {

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
}
