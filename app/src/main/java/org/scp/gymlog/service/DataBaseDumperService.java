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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataBaseDumperService {

    private final static String OUTPUT =  "output.json";
    private final static String OUTPUT_LEGACY =  "output-legacy.json";

    public void save(Context context, AppDatabase database) throws JSONException, IOException {
        JSONObject object = new JSONObject();

        //object.put("bars", bars(database));
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

                String line = br.lines().collect(Collectors.joining(""));
                JSONObject obj = new JSONObject(line);

                /*
                BarEntity[] bars = bars(obj.getJSONArray("bars"));
                database.barDao().clear();
                database.barDao().insertAll(bars);
                /**/

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
                        //database.exerciseDao().update(ent);
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

                BitEntity[] bits = bits(obj.getJSONArray("bits"));
                for (BitEntity bit : bits) {
                    bit.bitId = 0;
                    bit.exerciseId = exercisesIdMap.get(bit.exerciseId);
                    bit.trainingId = trainingsIdMap.get(bit.trainingId);
                }
                database.bitDao().insertAll(bits);

                // Update most recent bit to exercises
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


    public void loadLegacy(Context context, AppDatabase database) throws JSONException, IOException {
        String saveStatePath = new ContextWrapper(context).getFilesDir().getPath()+"/"+OUTPUT_LEGACY;
        if (new File(saveStatePath).exists()) {

            try (BufferedReader br = new BufferedReader(new FileReader(saveStatePath))){

                String line = br.lines().collect(Collectors.joining(""));
                JSONArray obj = new JSONArray(line);

                Map<Integer, Integer> trainingsIdMap = new HashMap<>();
                List<TrainingEntity> trainings = new ArrayList<>();

                JsonUtils.forEachObject(obj, exerciseObj -> {
                    int trainingId = exerciseObj.getInt("trainingId");

                    if (trainingsIdMap.containsKey(trainingId)) {
                        TrainingEntity tre = trainings.get(trainings.size()-1);
                        tre.end.setTimeInMillis(exerciseObj.getLong("timestamp"));

                    } else {
                        trainingsIdMap.put(trainingId, null);
                        TrainingEntity tre = new TrainingEntity();
                        tre.start = Calendar.getInstance();
                        tre.end = Calendar.getInstance();
                        tre.start.setTimeInMillis(exerciseObj.getLong("timestamp"));
                        tre.end.setTimeInMillis(exerciseObj.getLong("timestamp"));
                        trainings.add(tre);
                    }
                });

                long[] ids = database.trainingDao().insertAll(trainings.toArray(new TrainingEntity[0]));
                for (int i=0; i<ids.length; i++) {
                    trainingsIdMap.put(i+1, (int)ids[i]);
                }


                List<Exercise> exercises = Data.getInstance().getExercises();

                BitEntity[] bits = JsonUtils.mapObject(obj, exerciseObj -> {
                        BitEntity bitE = new BitEntity();
                        final String name = exerciseObj.getString("name");

                        Exercise exercise = exercises.stream()
                                .filter(ex -> ex.getName().equals(name))
                                .findFirst()
                                .orElseThrow(() -> new LoadException("Exercise not found: "+name));

                        bitE.exerciseId = exercise.getId();
                        bitE.trainingId = trainingsIdMap.get(exerciseObj.getInt("trainingId"));
                        bitE.reps = exerciseObj.getInt("reps");
                        bitE.totalWeight = exerciseObj.getInt("weight");
                        bitE.kilos = true;
                        bitE.instant = false;
                        bitE.note = exerciseObj.getString("note");
                        if (bitE.note.equals("/\\")) {
                            bitE.note = "";
                            bitE.instant = true;
                        }
                        bitE.timestamp = Calendar.getInstance();
                        bitE.timestamp.setTimeInMillis(exerciseObj.getLong("timestamp"));

                        return bitE;
                    }).toArray(BitEntity[]::new);

                //database.bitDao().clear();
                database.bitDao().insertAll(bits);


                // Update most recent bit to exercises
                database.exerciseDao().getAll().stream()
                        .peek(ee ->
                            ee.lastTrained = Arrays.stream(bits)
                                    .filter(bb -> bb.exerciseId == ee.exerciseId)
                                    .map(bb -> bb.timestamp)
                                    .reduce(DATE_ZERO, (val, acc) -> val.compareTo(acc) > 0? val:acc)
                        )
                        .filter(ee -> ee.lastTrained.compareTo(DATE_ZERO) > 0)
                        .forEach(database.exerciseDao()::update);
                /**/

            } catch (JSONException | IOException e){
                System.err.println("Couldn't load \""+saveStatePath+"\"");
            }
        }
    }
}
