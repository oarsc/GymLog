package org.scp.gymlog.service;

import android.content.Context;
import android.content.ContextWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.entities.BarEntity;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.JsonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class DataBaseDumperService {

    private final static String OUTPUT =  "output.json";

    public void save(Context context, AppDatabase database) throws JSONException, IOException {
        JSONObject object = new JSONObject();

        object.put("bars", bars(database));
        object.put("exercises", exercises(database));
        object.put("primaries", primaryMuscles(database));
        object.put("secondaries", secondaryMuscles(database));
        object.put("trainings", trainings(database));
        object.put("bits", bits(database));

        String saveStatePath = new ContextWrapper(context).getFilesDir().getPath()+"/"+OUTPUT;
        PrintWriter writer = new PrintWriter(saveStatePath, "UTF-8");
        writer.println(object.toString());
        writer.close();
    }

    private JSONArray bars(AppDatabase database) {
        return convertToJSONArray(database.barDao().getAll());
    }

    private JSONArray exercises(AppDatabase database) {
        return convertToJSONArray(database.exerciseDao().getAll());
    }

    private JSONArray primaryMuscles(AppDatabase database) {
        return convertToJSONArray(database.exerciseMuscleCrossRefDao().getAll());
    }

    private JSONArray secondaryMuscles(AppDatabase database) {
        return convertToJSONArray(database.exerciseMuscleCrossRefDao().getAllSecondaryMuscles());
    }

    private JSONArray trainings(AppDatabase database) {
        return convertToJSONArray(database.trainingDao().getAll());
    }

    private JSONArray bits(AppDatabase database) {
        return convertToJSONArray(database.bitDao().getAll());
    }

    private JSONArray convertToJSONArray(List<?> list) {
        return list.stream()
                .map(JsonUtils::jsonify)
                .collect(JsonUtils.collector());
    }


    public void load(Context context, AppDatabase database) throws JSONException, IOException {
        String saveStatePath = new ContextWrapper(context).getFilesDir().getPath()+"/"+OUTPUT;
        if (new File(saveStatePath).exists()) {

            try (BufferedReader br = new BufferedReader(new FileReader(saveStatePath))){

                String line = br.readLine();
                JSONObject obj = new JSONObject(line);

                BarEntity[] bars = bars(obj.getJSONArray("bars"));
                database.barDao().clear();
                database.barDao().insertAll(bars);

                // EXERCISES:
                ExerciseEntity[] exercises = exercises(obj.getJSONArray("exercises"));
                Map<Integer, Integer> exercisesIdMap = new HashMap<>();

                List<Exercise> currentExercises = Data.getInstance().getExercises();
                int newIds = currentExercises.size();
                for (ExerciseEntity ent : exercises) {
                    Optional<Exercise> matchingEx = currentExercises.stream()
                            .filter(ex -> ex.getName().equalsIgnoreCase(ent.name))
                            .findAny();
                    if (matchingEx.isPresent()) {
                        exercisesIdMap.put(ent.exerciseId, matchingEx.get().getId());
                        ent.exerciseId = matchingEx.get().getId();
                        database.exerciseDao().update(ent);
                    } else {
                        exercisesIdMap.put(ent.exerciseId, newIds);
                        ent.exerciseId = newIds++;
                        database.exerciseDao().insert(ent);
                    }
                }

                ExerciseMuscleCrossRef[] primaryMuscles = primaryMuscles(obj.getJSONArray("primaries"));
                for (ExerciseMuscleCrossRef primaryMuscle : primaryMuscles) {
                    primaryMuscle.exerciseId = exercisesIdMap.get(primaryMuscle.exerciseId);
                }
                database.exerciseMuscleCrossRefDao().clear();
                database.exerciseMuscleCrossRefDao().insertAll(primaryMuscles);

                SecondaryExerciseMuscleCrossRef[] secondaryMuscles = secondaryMuscles(obj.getJSONArray("secondaries"));
                for (SecondaryExerciseMuscleCrossRef secondaryMuscle : secondaryMuscles) {
                    secondaryMuscle.exerciseId = exercisesIdMap.get(secondaryMuscle.exerciseId);
                }
                database.exerciseMuscleCrossRefDao().clearSecondary();
                database.exerciseMuscleCrossRefDao().insertAll(secondaryMuscles);

                TrainingEntity[] trainings = trainings(obj.getJSONArray("trainings"));
                database.trainingDao().clear();
                database.trainingDao().insertAll(trainings);

                BitEntity[] bits = bits(obj.getJSONArray("bits"));
                for (BitEntity bit : bits) {
                    bit.exerciseId = exercisesIdMap.get(bit.exerciseId);
                }
                database.bitDao().clear();
                database.bitDao().insertAll(bits);

            } catch (JSONException | IOException e){
                System.err.println("Couldn't load \""+saveStatePath+"\"");
            }
        }
    }


    private BarEntity[] bars(JSONArray list) throws JSONException {
        return convertToObject(list, BarEntity.class).toArray(BarEntity[]::new);
    }

    private ExerciseEntity[] exercises(JSONArray list) throws JSONException {
        return convertToObject(list, ExerciseEntity.class).toArray(ExerciseEntity[]::new);
    }

    private ExerciseMuscleCrossRef[] primaryMuscles(JSONArray list) throws JSONException {
        return convertToObject(list, ExerciseMuscleCrossRef.class)
                .toArray(ExerciseMuscleCrossRef[]::new);
    }

    private SecondaryExerciseMuscleCrossRef[] secondaryMuscles(JSONArray list) throws JSONException {
        return convertToObject(list, SecondaryExerciseMuscleCrossRef.class)
                .toArray(SecondaryExerciseMuscleCrossRef[]::new);
    }

    private TrainingEntity[] trainings(JSONArray list) throws JSONException {
        return convertToObject(list, TrainingEntity.class).toArray(TrainingEntity[]::new);
    }

    private BitEntity[] bits(JSONArray list) throws JSONException {
        return convertToObject(list, BitEntity.class).toArray(BitEntity[]::new);
    }


    private <T> Stream<T> convertToObject(JSONArray list, Class<T> cls) throws JSONException {
        return JsonUtils.mapObject(list, json -> JsonUtils.objectify(json, cls));
    }
}
