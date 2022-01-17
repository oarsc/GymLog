package org.scp.gymlog.service;

import static org.scp.gymlog.util.Constants.DATE_ZERO;

import android.content.Context;
import android.content.ContextWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.room.AppDatabase;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataBaseDumperService {

    private final static String OUTPUT =  "output.json";

    public void save(Context context, AppDatabase database) throws JSONException, IOException {
        JSONObject object = new JSONObject();

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
        JSONArray bits = convertToJSONArray(database.bitDao().getAll());
        try {
            JsonUtils.forEachObject(bits, bit -> {
                int id = bit.getInt("exerciseId");
                bit.remove("exerciseId");
                bit.put("exerciseName", Data.getExercise(id).getName());
            });
        } catch (JSONException e) {
            throw new LoadException("",e);
        }
        return bits;
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

                String line = br.lines().collect(Collectors.joining(""));
                JSONObject obj = new JSONObject(line);

                // EXERCISES:
                ExerciseEntity[] exercises = exercises(obj.getJSONArray("exercises"));
                Map<Integer, Integer> exercisesIdMap = new HashMap<>();

                List<Exercise> currentExercises = Data.getInstance().getExercises();
                List<Integer> addedIds = new ArrayList<>();
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
                        exercisesIdMap.put(ent.exerciseId, ++newIds);
                        ent.exerciseId = newIds;
                        addedIds.add(newIds);
                        database.exerciseDao().insert(ent);
                    }
                }

                ExerciseMuscleCrossRef[] primaryMuscles = primaryMuscles(obj.getJSONArray("primaries"));
                for (ExerciseMuscleCrossRef primaryMuscle : primaryMuscles) {
                    primaryMuscle.exerciseId = exercisesIdMap.get(primaryMuscle.exerciseId);
                }
                database.exerciseMuscleCrossRefDao().insertAll(
                        Arrays.stream(primaryMuscles)
                                .filter(entity -> addedIds.contains(entity.exerciseId))
                                .toArray(ExerciseMuscleCrossRef[]::new)
                    );


                SecondaryExerciseMuscleCrossRef[] secondaryMuscles = secondaryMuscles(obj.getJSONArray("secondaries"));
                for (SecondaryExerciseMuscleCrossRef secondaryMuscle : secondaryMuscles) {
                    secondaryMuscle.exerciseId = exercisesIdMap.get(secondaryMuscle.exerciseId);
                }
                database.exerciseMuscleCrossRefDao().insertAll(
                        Arrays.stream(secondaryMuscles)
                                .filter(entity -> addedIds.contains(entity.exerciseId))
                                .toArray(SecondaryExerciseMuscleCrossRef[]::new)
                    );

                List<Integer> trainingOrig = new ArrayList<>();
                Map<Integer, Integer> trainingsIdMap = new HashMap<>();

                TrainingEntity[] trainings = trainings(obj.getJSONArray("trainings"));
                for (TrainingEntity trainingEntity : trainings) {
                    trainingOrig.add(trainingEntity.trainingId);
                    trainingEntity.trainingId = 0;
                }

                long[] newTrIds = database.trainingDao().insertAll(trainings);
                for (int i = 0; i<newTrIds.length; i++) {
                    trainingsIdMap.put(trainingOrig.get(i), (int) newTrIds[i]);
                }

                BitEntity[] bits = bits(obj.getJSONArray("bits"), exercises);
                for (BitEntity bit : bits) {
                    bit.trainingId = trainingsIdMap.get(bit.trainingId);
                }
                database.bitDao().insertAll(bits);

                // Update most recent bit to exercises
                //*
                database.exerciseDao().getAll().stream()
                        .filter(ee -> Arrays.stream(bits).anyMatch(bb -> bb.exerciseId == ee.exerciseId))
                        .peek(ee ->
                            ee.lastTrained = Arrays.stream(bits)
                                    .filter(bb -> bb.exerciseId == ee.exerciseId)
                                    .map(bb -> bb.timestamp)
                                    .reduce(DATE_ZERO, (val, acc) -> val.compareTo(acc) > 0? val:acc)
                        )
                        .filter(ee -> ee.lastTrained.compareTo(DATE_ZERO) > 0)
                        .forEach(database.exerciseDao()::update);
                /**/

                // Reindex trainings:
                /*
                List<TrainingEntity> allTrainings = database.trainingDao().getAll();
                allTrainings.sort(Comparator.comparing(t -> t.trainingId));
                for (int i=1; i<=allTrainings.size(); i++) {
                    TrainingEntity tr = allTrainings.get(i-1);
                    if (tr.trainingId != i) {
                        database.trainingDao().updateId(tr.trainingId, i);
                        tr.trainingId = i;
                    }
                }
                /**/

            } catch (JSONException | IOException e){
                System.err.println("Couldn't load \""+saveStatePath+"\"");
            }
        }
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

    private BitEntity[] bits(JSONArray list, ExerciseEntity[] exercises) throws JSONException {
        JsonUtils.forEachObject(list, bit -> {
            String name = bit.getString("exerciseName");
            /*int id = Data.getInstance().getExercises().stream()
                    .filter(e -> e.getName().equals(name))
                    .map(e -> e.getId())
                    .findFirst()
                    .orElseThrow(() -> new LoadException("Couldn't find exercise: "+name));*/
            int id = Arrays.stream(exercises)
                    .filter(e -> e.name.equals(name))
                    .map(e -> e.exerciseId)
                    .findFirst()
                    .orElseThrow(() -> new LoadException("Couldn't find exercise: "+name));
            bit.put("exerciseId", id);
        });
        return convertToObject(list, BitEntity.class).toArray(BitEntity[]::new);
    }


    private <T> Stream<T> convertToObject(JSONArray list, Class<T> cls) throws JSONException {
        return JsonUtils.mapObject(list, json -> JsonUtils.objectify(json, cls));
    }
}
