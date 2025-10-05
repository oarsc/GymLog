package org.oar.gymlog.ui.main.preferences

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.oar.gymlog.R
import org.oar.gymlog.databinding.FragmentPreferencesBinding
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.DEFAULT_REST_TIME
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.THEME
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.UNIT_CONVERSION_EXACT_VALUE
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.UNIT_CONVERSION_STEP
import org.oar.gymlog.util.WeightUtils
import java.util.Locale

class PreferencesFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = FragmentPreferencesBinding.inflate(inflater, container, false)
        .apply {
            toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

            val prefContent = super.onCreateView(inflater, container, savedInstanceState)
            listContainer.addView(prefContent)
        }
        .root

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences, rootKey)
		loadNumberedEditText(DEFAULT_REST_TIME.key,
			InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED,
			getString(R.string.text_seconds).lowercase(Locale.getDefault()))

		loadNumberedEditText(
			UNIT_CONVERSION_STEP.key,
			InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
					InputType.TYPE_NUMBER_FLAG_DECIMAL,
			null)
	}

	private fun loadNumberedEditText(key: String, inputType: Int, summarySuffix: String?) {
		val editTextPreference = preferenceManager.findPreference<EditTextPreference>(key)!!
		editTextPreference.setOnBindEditTextListener { editText ->
			editText.inputType = inputType
			editText.setSelection(editText.text.length)
		}

		editTextPreference.summary = if (summarySuffix == null)
				editTextPreference.text
			else
				editTextPreference.text + " " + summarySuffix
	}

	override fun onResume() {
		super.onResume()
		preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
	}

	override fun onPause() {
		super.onPause()
		preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		if (sharedPreferences == null || key == null) return

		when (key) {
			DEFAULT_REST_TIME.key -> {
				val prefRestTime = findPreference<EditTextPreference>(key)!!
				prefRestTime.summary = prefRestTime.text + " " +
						getString(R.string.text_seconds).lowercase(Locale.getDefault())
			}
			UNIT_CONVERSION_STEP.key -> {
				val prefConversionStep = findPreference<EditTextPreference>(key)!!
				prefConversionStep.summary = prefConversionStep.text
				updateFormatUtils()
			}
			UNIT_CONVERSION_EXACT_VALUE.key -> updateFormatUtils()
			THEME.key -> {
				if (sharedPreferences.getBoolean(key, false)) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
				}
				requireActivity().recreate()
			}
		}
	}

	private fun updateFormatUtils() {
		val prefRestTime = findPreference<EditTextPreference>(UNIT_CONVERSION_STEP.key)
		val conversionExactValue = findPreference<SwitchPreferenceCompat>(UNIT_CONVERSION_EXACT_VALUE.key)

		if (prefRestTime != null && conversionExactValue != null) {
			WeightUtils.setConvertParameters(
				conversionExactValue.isChecked,
				prefRestTime.text!!)
		}
	}
}