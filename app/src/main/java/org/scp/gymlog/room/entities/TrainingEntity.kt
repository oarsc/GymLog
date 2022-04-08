package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.scp.gymlog.util.Constants
import java.util.*

@Entity(
    tableName = "training",
    indices = [
        Index("start", "end")
    ]
)
class TrainingEntity {
    @PrimaryKey(autoGenerate = true)
    var trainingId = 0
    var start: Calendar = Constants.DATE_ZERO
    var end: Calendar? = null
}