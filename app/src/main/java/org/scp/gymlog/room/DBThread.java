package org.scp.gymlog.room;

import android.content.Context;

import java.util.function.Consumer;

public class DBThread {
    public static void run(Context context, Consumer<AppDatabase> process) {
        AppDatabase database = Connection.get(context);
        new Thread(()->
            process.accept(database)
        ).start();
    }
}
