package org.scp.gymlog.service;

import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.BarEntity;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.MuscleEntity;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.util.Data;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

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
        ex.image = "chest_bench_barbell_press_lying";
        ex.name = "Bench press";
        ex.requiresBar = true;
        ex.lastBarId = 4; // 20kg
        ex.lastTrained = new Date(1639086000000L);
        ex.lastWeightSpec = WeightSpecification.ONE_SIDE_WEIGHT;
        ex.exerciseId = (int) db.exerciseDao().insert(ex);

        ExerciseMuscleCrossRef exXmuscle = new ExerciseMuscleCrossRef();
        exXmuscle.exerciseId = ex.exerciseId;
        exXmuscle.muscleId = 1;
        db.exerciseMuscleCrossRefDao().insert(exXmuscle);

        TrainingEntity training = new TrainingEntity();
        training.start = new Date(1639080000000L);
        training.end = new Date(1639090000000L);
        training.trainingId = (int) db.trainingDao().insert(training);

        Consumer<Long> createBit = (date) -> {
            BitEntity bit = new BitEntity();
            bit.barId = 4; // 20kg
            bit.exerciseId = ex.exerciseId;
            bit.timestamp = new Date(date);
            bit.kilos = true;
            bit.totalWeight = 9000;
            bit.reps = 10;
            bit.note = "";
            bit.trainingId = training.trainingId;
            db.bitDao().insert(bit);
        };

        createBit.accept(1639081000000L);
        createBit.accept(1639082000000L);
        createBit.accept(1639083000000L);
        createBit.accept(1639084000000L);
        createBit.accept(1639085000000L);
        createBit.accept(1639086000000L);

    }
}
