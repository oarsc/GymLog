package org.oar.gymlog.util.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.dropbox.core.oauth.DbxCredential
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition

object PreferencesExts {

    private val Context.preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)

    private val Context.preferencesEditor: SharedPreferences.Editor
        get() = preferences.edit()


    private fun Context.save(name: String, content: String?) {
        preferencesEditor.apply {
            putString(name, content)
            apply()
        }
    }

    private fun Context.save(name: String, content: Int) {
        preferencesEditor.apply {
            putInt(name, content)
            apply()
        }
    }

    private fun Context.save(name: String, content: Boolean) {
        preferencesEditor.apply {
            putBoolean(name, content)
            apply()
        }
    }

    fun Context.save(preferenceDef: PreferencesDefinition, content: String?) =
        save(preferenceDef.key, content)

    fun Context.save(preferenceDef: PreferencesDefinition, content: Int) =
        save(preferenceDef.key, content)

    fun Context.save(preferenceDef: PreferencesDefinition, content: Boolean) =
        save(preferenceDef.key, content)

    fun Context.save(preferenceDef: PreferencesDefinition, dbxCredential: DbxCredential) =
        save(preferenceDef.key, DbxCredential.Writer.writeToString(dbxCredential))

    private fun Context.loadString(name: String, def: String = ""): String {
        return preferences.getString(name, def) ?: def
    }

    private fun Context.loadNullableString(name: String, def: String? = null): String? {
        return if (preferences.contains(name)) preferences.getString(name, null)
            else def
    }

    private fun Context.loadInteger(name: String, def: Int = 0): Int {
        return preferences.getInt(name, def)
    }

    private fun Context.loadBoolean(name: String, def: Boolean = false): Boolean {
        return preferences.getBoolean(name, def)
    }

    fun Context.loadString(preferenceDef: PreferencesDefinition): String {
        val def = preferenceDef.defaultString
            ?: throw RuntimeException("Default String value not defined for ${preferenceDef.key}")
        return loadString(preferenceDef.key, def)
    }

    fun Context.loadNullableString(preferenceDef: PreferencesDefinition) =
        loadNullableString(preferenceDef.key, preferenceDef.defaultString)

    fun Context.loadInteger(preferenceDef: PreferencesDefinition): Int {
        val def = preferenceDef.defaultInteger
            ?: throw RuntimeException("Default Integer value not defined for ${preferenceDef.key}")
        return loadInteger(preferenceDef.key, def)
    }

    fun Context.loadBoolean(preferenceDef: PreferencesDefinition): Boolean {
        val def = preferenceDef.defaultBoolean
            ?: throw RuntimeException("Default Boolean value not defined for ${preferenceDef.key}")
        return loadBoolean(preferenceDef.key, def)
    }

    fun Context.loadDbxCredential(preferenceDef: PreferencesDefinition): DbxCredential? {
        val serializedCredentialJson = loadNullableString(preferenceDef.key, preferenceDef.defaultString)
        return try {
            DbxCredential.Reader.readFully(serializedCredentialJson)
        } catch (e: Exception) {
            // Something went wrong parsing the credential, clearing it
            save(preferenceDef.key, null)
            null
        }
    }
}