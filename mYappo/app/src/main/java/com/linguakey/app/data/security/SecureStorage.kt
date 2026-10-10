package com.linguakey.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.linguakey.app.domain.model.ProfileId

/**
 * Almacenamiento seguro respaldado por Android Keystore.
 * Las claves API nunca se escriben en texto plano en el disco.
 */
class SecureStorage(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "linguakey_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getApiKey(): String = prefs.getString("api_key", "") ?: ""
    fun saveApiKey(key: String) = prefs.edit().putString("api_key", key.trim()).apply()
    fun clearApiKey() = prefs.edit().remove("api_key").apply()

    fun getBaseUrl(): String = prefs.getString("base_url", "https://openrouter.ai/api/v1") ?: "https://openrouter.ai/api/v1"
    fun saveBaseUrl(url: String) = prefs.edit().putString("base_url", url.trim()).apply()

    fun getModelId(): String = prefs.getString("model_id", "meta-llama/llama-3.3-70b-instruct:free") ?: "meta-llama/llama-3.3-70b-instruct:free"
    fun saveModelId(model: String) = prefs.edit().putString("model_id", model.trim()).apply()

    fun isOnlyFreeMode(): Boolean = prefs.getBoolean("only_free_mode", true)
    fun setOnlyFreeMode(enabled: Boolean) = prefs.edit().putBoolean("only_free_mode", enabled).apply()

    fun isHapticEnabled(): Boolean = prefs.getBoolean("haptic_feedback", true)
    fun setHapticEnabled(enabled: Boolean) = prefs.edit().putBoolean("haptic_feedback", enabled).apply()

    fun getDefaultProfile(): ProfileId {
        val name = prefs.getString("default_profile", ProfileId.CASUAL.name) ?: ProfileId.CASUAL.name
        return try { ProfileId.valueOf(name) } catch (e: Exception) { ProfileId.CASUAL }
    }
    fun setDefaultProfile(profile: ProfileId) = prefs.edit().putString("default_profile", profile.name).apply()

    fun getDefaultIntensity(): Int = prefs.getInt("default_intensity", 3)
    fun setDefaultIntensity(intensity: Int) = prefs.edit().putInt("default_intensity", intensity).apply()

    fun getCustomInstructions(): String? = prefs.getString("custom_instructions", null)
}
