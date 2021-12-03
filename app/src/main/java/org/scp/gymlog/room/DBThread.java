package org.scp.gymlog.room;

import android.content.Context;

import java.util.function.Consumer;

public class DBThread {
    public DBThread(Context context, Consumer<AppDatabase> process) {
        AppDatabase database = DatabaseConnection.get(context);
        new Thread(()->
            process.accept(database)
        ).start();
    }
}
