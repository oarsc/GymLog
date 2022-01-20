package org.scp.gymlog.ui.common.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.flexbox.FlexboxLayout;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.ui.common.CustomAppCompatActivity;
import org.scp.gymlog.util.Constants.IntentReference;
import org.scp.gymlog.util.TaskRunner;

import java.io.IOException;
import java.io.InputStream;

public class ImageSelectorActivity extends CustomAppCompatActivity {
    private AssetManager assets;
    private FlexboxLayout layout;
    private RelativeLayout.LayoutParams defaultLayoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);

        int title = getIntent().getExtras().getInt("title");
        assets = getAssets();
        layout = findViewById(R.id.imageSelectorLayout);
        defaultLayoutParams = new RelativeLayout.LayoutParams(175, 175);

        if (title == IntentReference.CREATE_EXERCISE.ordinal()) {
            setTitle(R.string.title_create_exercise);
            try {
                loadExercises();
            } catch (IOException e) {
                throw new LoadException("Error loading exercises", e);
            }
        }
    }

    private void loadExercises() throws IOException {
        final String folder = "previews";
        TaskRunner tr = new TaskRunner();
        for (String asset : assets.list(folder)) {
            tr.executeAsync(
                    () -> getImageViewFromAsset(folder +"/" + asset),
                    view -> layout.addView(view, defaultLayoutParams));
        }
    }

    private View getImageViewFromAsset(String fileName) throws IOException {
        InputStream ims = assets.open(fileName);
        Drawable d = Drawable.createFromStream(ims, null);

        ImageView imageView = (ImageView) getLayoutInflater().inflate(R.layout.list_element_fragment_image_selector, null);
        imageView.setImageDrawable(d);

        imageView.setOnClickListener(view -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("fileName", fileName);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        });

        return imageView;
    }
}