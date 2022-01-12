package org.scp.gymlog.ui.top;

import android.os.Bundle;

import org.scp.gymlog.model.Bit;
import org.scp.gymlog.room.AppDatabase;

import java.util.stream.Stream;

public class TopSpecificActivity extends TopActivity {
    private int weight;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        weight = getIntent().getExtras().getInt("weight");
        return super.onLoad(savedInstanceState, db);
    }

    @Override
    protected Stream<Bit> getBits(AppDatabase db, int exerciseId) {
        return db.bitDao().findAllByExerciseAndWeight(exerciseId, weight).stream()
                .map(bit -> new Bit().fromEntity(bit));
    }

    @Override
    protected void onElementLongClicked(Bit topBit) {
        // do nothing
    }
}