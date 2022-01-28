package org.scp.gymlog.ui.top;

import android.os.Bundle;

import org.scp.gymlog.model.Bit;
import org.scp.gymlog.room.AppDatabase;

import java.util.Comparator;
import java.util.stream.Stream;

public class TopSpecificActivity extends TopActivity {
    private int weight;
    private int variationId;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        weight = getIntent().getExtras().getInt("weight");
        variationId = getIntent().getExtras().getInt("variationId");
        return super.onLoad(savedInstanceState, db);
    }

    @Override
    protected Stream<Bit> getBits(AppDatabase db, int exerciseId) {
        if (variationId == 0)
            return db.bitDao().findAllByExerciseAndWeight(exerciseId, weight).stream()
                    .map(bit -> new Bit().fromEntity(bit));
        else
            return db.bitDao().findAllByExerciseAndWeight(exerciseId, variationId, weight).stream()
                    .map(bit -> new Bit().fromEntity(bit));
    }

    @Override
    protected Comparator<? super Bit> order() {
        return Comparator.comparingLong(bit -> -bit.getTimestamp().getTimeInMillis());
    }

    @Override
    protected void onElementLongClicked(Bit topBit) {
        // do nothing
    }
}