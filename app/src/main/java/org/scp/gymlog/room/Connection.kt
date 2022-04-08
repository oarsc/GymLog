package org.scp.gymlog.room

import android.content.Context
import androidx.room.Room

object Connection {

    private var db: AppDatabase? = null

    operator fun get(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(context,  //getApplicationContext(),
                AppDatabase::class.java, "gymlog-db").build()
        }
        return db!!
    }
}
