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

import org.scp.gymlog.R;

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

		EditTextPreference editTextPreference = getPreferenceManager().findPreference("restTime");
		assert editTextPreference != null;
		editTextPreference.setOnBindEditTextListener(editText -> {
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
				editText.setSelection(editText.getText().length());
			});
		editTextPreference.setSummary(editTextPreference.getText()+" "+
				getString(R.string.text_seconds).toLowerCase());
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
				EditTextPreference pref = findPreference(key);
				assert pref != null;
				pref.setSummary(pref.getText()+" "+
						getString(R.string.text_seconds).toLowerCase());
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




}
