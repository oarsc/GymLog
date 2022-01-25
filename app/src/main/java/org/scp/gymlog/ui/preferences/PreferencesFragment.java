package org.scp.gymlog.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import org.scp.gymlog.R;
import org.scp.gymlog.util.WeightUtils;

public class PreferencesFragment extends PreferenceFragmentCompat
		implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setNavigationOnClickListener(a-> getActivity().onBackPressed());

		return view;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);

		loadNumberedEditText("restTime",
				InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED,
				getString(R.string.text_seconds).toLowerCase());

		loadNumberedEditText("conversionStep",
				InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
						InputType.TYPE_NUMBER_FLAG_DECIMAL,
				null);
	}

	private void loadNumberedEditText(String key, int inputType, String summarySuffix) {
		EditTextPreference editTextPreference = getPreferenceManager().findPreference(key);
		assert editTextPreference != null;
		editTextPreference.setOnBindEditTextListener(editText -> {
			editText.setInputType(inputType);
			editText.setSelection(editText.getText().length());
		});
		if (summarySuffix == null) {
			editTextPreference.setSummary(editTextPreference.getText());
		} else {
			editTextPreference.setSummary(editTextPreference.getText() +" "+ summarySuffix);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key) {
			case "restTime":
				EditTextPreference prefRestTime = findPreference(key);
				assert prefRestTime != null;
				prefRestTime.setSummary(prefRestTime.getText()+" "+
						getString(R.string.text_seconds).toLowerCase());
				break;
			case "conversionStep":
				EditTextPreference prefConversionStep = findPreference(key);
				assert prefConversionStep != null;
				prefConversionStep.setSummary(prefConversionStep.getText());

			case "conversionExactValue":
				updateFormatUtils();
				break;
			case "nightTheme":
				boolean value = sharedPreferences.getBoolean(key, false);

				if (value) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
				getActivity().recreate();
				break;
		}
	}

	private void updateFormatUtils() {
		EditTextPreference prefRestTime = findPreference("conversionStep");
		SwitchPreferenceCompat conversionExactValue = findPreference("conversionExactValue");

		if (prefRestTime != null && conversionExactValue != null) {
			WeightUtils.setConvertParameters(
					conversionExactValue.isChecked(),
					prefRestTime.getText());
		}
	}
}
