package com.linguakey.app

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.linguakey.app.data.api.AiTranslationRepository
import com.linguakey.app.data.security.SecureStorage
import com.linguakey.app.domain.model.ProfileId
import com.linguakey.app.ui.keyboard.LinguaKeyKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LinguaKeyImeService
 *
 * Implementa el servicio nativo de teclado Android (InputMethodService).
 * Gestiona:
 * - Ciclo de vida del IME.
 * - Captura e inyección de texto mediante InputConnection.
 * - Traducción en el hilo de fondo (Coroutines I/O).
 * - Protección del texto original si la llamada de red falla.
 */
class LinguaKeyImeService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var secureStorage: SecureStorage
    private lateinit var translationRepo: AiTranslationRepository
    private var vibrator: Vibrator? = null

    // Estado del teclado en memoria
    private var currentProfileId: ProfileId = ProfileId.CASUAL
    private var currentIntensity: Int = 3
    private var isTranslating: Boolean = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        secureStorage = SecureStorage(applicationContext)
        translationRepo = AiTranslationRepository(secureStorage)
        vibrator = getSystemService(Vibrator::class.java)

        // Cargar preferencias del usuario
        currentProfileId = secureStorage.getDefaultProfile()
        currentIntensity = secureStorage.getDefaultIntensity()
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LinguaKeyImeService)
            setViewTreeSavedStateRegistryOwner(this@LinguaKeyImeService)
            setContent {
                LinguaKeyKeyboard(
                    currentProfile = currentProfileId,
                    intensity = currentIntensity,
                    isTranslating = isTranslating,
                    onKeyPress = { key -> handleKeyPress(key) },
                    onTranslateClick = { performTranslation() },
                    onProfileChanged = { newProfile -> currentProfileId = newProfile },
                    onIntensityChanged = { newInt -> currentIntensity = newInt },
                    onCloseKeyboard = { requestHideSelf(0) }
                )
            }
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        scope.cancel()
    }

    /**
     * Procesa la pulsación de teclas estándar (QWERTY, Shift, Espacio, Retroceso, Intro).
     */
    private fun handleKeyPress(key: String) {
        triggerHaptic()
        val ic: InputConnection = currentInputConnection ?: return

        when (key) {
            "BACKSPACE" -> {
                val selected = ic.getSelectedText(0)
                if (selected.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }
            "SPACE" -> ic.commitText(" ", 1)
            "ENTER" -> ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            else -> ic.commitText(key, 1)
        }
    }

    /**
     * Ejecuta la traducción inteligente preservando el texto original ante cualquier error.
     */
    private fun performTranslation() {
        val ic: InputConnection? = currentInputConnection
        if (ic == null) {
            Toast.makeText(this, "No hay campo de texto activo para traducir", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener texto antes del cursor (hasta 2000 caracteres)
        val beforeText = ic.getTextBeforeCursor(2000, 0)?.toString() ?: ""
        val trimmed = beforeText.trim()

        if (trimmed.isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje en español antes de pulsar Traducir", Toast.LENGTH_SHORT).show()
            return
        }

        isTranslating = true

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                translationRepo.translate(
                    sourceText = trimmed,
                    profile = currentProfileId,
                    intensity = currentIntensity
                )
            }

            isTranslating = false

            if (result.isSuccess) {
                val translated = result.getOrNull()
                if (!translated.isNullOrBlank()) {
                    // Reemplazo seguro: primero seleccionamos/borramos el texto original y colocamos la traducción
                    ic.beginBatchEdit()
                    ic.deleteSurroundingText(trimmed.length, 0)
                    ic.commitText(translated, 1)
                    ic.endBatchEdit()
                    triggerHaptic(heavy = true)
                } else {
                    Toast.makeText(this@LinguaKeyImeService, "Respuesta vacía del proveedor. Texto original intacto.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Conservar texto original y mostrar error comprensible
                val errorMsg = result.exceptionOrNull()?.message ?: "Error de traducción"
                Toast.makeText(this@LinguaKeyImeService, "Error: $errorMsg (Texto conservado)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun triggerHaptic(heavy: Boolean = false) {
        if (!secureStorage.isHapticEnabled()) return
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (heavy) {
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(12, 100)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(if (heavy) 35L else 12L)
            }
        }
    }
}
