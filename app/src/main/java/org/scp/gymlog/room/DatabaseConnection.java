package org.scp.gymlog.room;

import android.content.Context;

import androidx.room.Room;

public class DatabaseConnection {

    private static AppDatabase db;

    public static AppDatabase get(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context, //getApplicationContext(),
                    AppDatabase.class, "gymlog-db").build();
        }
        return db;
    }
}
