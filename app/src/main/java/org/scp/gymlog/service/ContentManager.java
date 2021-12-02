package org.scp.gymlog.service;

import android.content.Context;
import android.content.ContextWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.util.json.JsonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ContentManager {
    private static final Data DATA = Data.getInstance();
    private static final String FILENAME_EXTENSION = ".data";

    public static void saveExercises(Context context) {
        try {
            JSONArray exercises = new JSONArray();
            for (Exercise exercise : DATA.getExercises()) {
                exercises.put(exercise.toJson());
            }

            String fileSavePath = new ContextWrapper(context).getFilesDir().getPath()+"/exercises"
                    + FILENAME_EXTENSION;
            PrintWriter out = new PrintWriter(fileSavePath);
            out.print(exercises);
            out.close();

        } catch (FileNotFoundException | JSONException e) {
            throw new LoadException("Error saving exercises", e);
        }
    }

    public static void loadExercises(Context context) {
        try {
            String fileSavePath = new ContextWrapper(context).getFilesDir().getPath()+"/exercises"
                    + FILENAME_EXTENSION;

            if (new File(fileSavePath).exists()) {
                BufferedReader br = new BufferedReader(new FileReader(fileSavePath));
                String line = br.readLine();
                br.close();

                JSONArray exercises = new JSONArray(line);

                List<Exercise> dataExercises = DATA.getExercises();
                dataExercises.clear();
                JsonUtils.forEach(exercises, Exercise.class, dataExercises::add);
            }

        } catch (IOException | JSONException e) {
            throw new LoadException("Error loading exercises", e);
        }
    }
}
