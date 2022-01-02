package org.scp.gymlog.service;

import static org.scp.gymlog.util.Constants.DATE_ZERO;

import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.daos.ExerciseDao;
import org.scp.gymlog.room.daos.ExerciseMuscleCrossRefDao;
import org.scp.gymlog.room.entities.BarEntity;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.MuscleEntity;
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class InitialDataService {

    public void persist(AssetManager assets, AppDatabase db) {
        Data data = Data.getInstance();

        persistMuscles(data, db);
        createAndPersistBars(data, db);
        loadExercises(assets, db);
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

    private void loadExercises(AssetManager assets, AppDatabase db) {
        JSONArray exercisesArray = assetJsonArrayFile(assets, "initialData.json");

        ExerciseDao exerciseDao = db.exerciseDao();
        ExerciseMuscleCrossRefDao exXmuscleDao = db.exerciseMuscleCrossRefDao();

        try {
            JsonUtils.forEachObject(exercisesArray, exerciseObj -> {
                ExerciseEntity ex = new ExerciseEntity();
                ex.image = exerciseObj.getString("tag");
                ex.name = exerciseObj.getString("name");
                ex.requiresBar = exerciseObj.getBoolean("bar");
                if (ex.requiresBar) {
                    ex.lastBarId = 4; // 20kg
                }
                ex.lastTrained = DATE_ZERO;
                ex.lastWeightSpec = WeightSpecification.NO_BAR_WEIGHT;
                ex.exerciseId = (int) exerciseDao.insert(ex);


                ExerciseMuscleCrossRef[] muscle1Links = JsonUtils.mapInt(exerciseObj.getJSONArray("primary"), muscleId -> {
                    ExerciseMuscleCrossRef exXmuscle = new ExerciseMuscleCrossRef();
                    exXmuscle.exerciseId = ex.exerciseId;
                    exXmuscle.muscleId = muscleId;
                    return exXmuscle;
                }).toArray(ExerciseMuscleCrossRef[]::new);
                exXmuscleDao.insertAll(muscle1Links);


                SecondaryExerciseMuscleCrossRef[] muscle2Links = JsonUtils.mapInt(exerciseObj.getJSONArray("secondary"), muscleId -> {
                    SecondaryExerciseMuscleCrossRef exXmuscle = new SecondaryExerciseMuscleCrossRef();
                    exXmuscle.exerciseId = ex.exerciseId;
                    exXmuscle.muscleId = muscleId;
                    return exXmuscle;
                }).toArray(SecondaryExerciseMuscleCrossRef[]::new);
                exXmuscleDao.insertAll(muscle2Links);
            });
        } catch (JSONException e) {
            throw new LoadException("Unable to load initial exercises");
        }
    }

    public static JSONArray assetJsonArrayFile(AssetManager assets, String filename) {
        try {
            InputStream file = assets.open(filename);
            byte[] formArray = new byte[file.available()];
            file.read(formArray);
            file.close();
            return new JSONArray(new String(formArray));

        } catch (JSONException | IOException e) {
            throw new LoadException("Unable to load file "+filename);
        }
    }

}
