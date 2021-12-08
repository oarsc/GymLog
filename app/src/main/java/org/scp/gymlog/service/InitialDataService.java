package org.scp.gymlog.service;

import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.BarEntity;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.MuscleEntity;
import org.scp.gymlog.util.Data;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InitialDataService {
    public void persist(AppDatabase db) {
        Data data = Data.getInstance();

        persistMuscles(data, db);
        createAndPersistBars(data, db);
        addTestData(db);
    }

    private static void persistMuscles(Data data, AppDatabase db) {
        db.muscleDao().insertAll(
                data.getMuscles().stream()
                        .map(EntityMapped::toEntity)
                        .toArray(MuscleEntity[]::new)
        );
    }

    private static void createAndPersistBars(Data data, AppDatabase db) {
        List<Bar> bars = data.getBars();
        bars.clear();
        int barId = 0;
        Arrays.asList(
                new Bar(++barId, new Weight(new BigDecimal("7.5"), true)),
                new Bar(++barId, new Weight(new BigDecimal("10"), true)),
                new Bar(++barId, new Weight(new BigDecimal("15"), true)),
                new Bar(++barId, new Weight(new BigDecimal("20"), true)),
                new Bar(++barId, new Weight(new BigDecimal("25"), true))
        ).forEach(bars::add);

        db.barDao().insertAll(
                data.getBars().stream()
                        .map(EntityMapped::toEntity)
                        .toArray(BarEntity[]::new)
        );
    }

    private static void addTestData(AppDatabase db) {
        ExerciseEntity ex = new ExerciseEntity();
        ex.image = "abs_bar_knee_raise_rest_on_arms_1";
        ex.name = "Test";
        ex.lastTrained = new Date(0);
        ex.lastWeightSpec = WeightSpecification.TOTAL_WEIGHT;
        int id = (int) db.exerciseDao().insert(ex);

        ExerciseMuscleCrossRef exx = new ExerciseMuscleCrossRef();
        exx.exerciseId = id;
        exx.muscleId = 1;
        db.exerciseMuscleCrossRefDao().insert(exx);
    }
}
