package com.linguakey.app.domain.model

enum class ProfileId(val displayName: String, val badge: String) {
    FORMAL("Formal", "Trabajo"),
    NATURAL("Natural", "Cotidiano"),
    CASUAL("Casual", "Relajado"),
    INFORMAL("Informal", "Coloquial"),
    INTERNET("Internet", "Chat / Redes"),
    GAMER("Gamer", "Gaming"),
    CUSTOM("Personalizado", "A tu medida");

    fun getSystemPrompt(intensity: Int, customInstructions: String? = null): String {
        return when (this) {
            FORMAL -> "You are a professional Spanish-to-English translator for LinguaKey. Translate into polished, courteous, formal English. Intensity: $intensity/5. Output ONLY the translation."
            NATURAL -> "You are a natural Spanish-to-English translator. Translate into fluent everyday English as spoken by native speakers. Output ONLY the translation."
            CASUAL -> "Translate into relaxed, friendly casual English using natural contractions (I'm, don't, gonna). Intensity: $intensity/5. Output ONLY the translation."
            INFORMAL -> "Translate into lively, spontaneous colloquial English. Match the authentic mood. Intensity: $intensity/5. Output ONLY the translation."
            INTERNET -> "Translate into contemporary English online chat style. You may use natural chat abbreviations like 'ngl', 'fr', 'idk', 'lol' ONLY when fitting. Never force slang. Output ONLY the translation."
            GAMER -> "Translate into authentic competitive multiplayer gamer lingo (AFK, gg, diff, nerf, buff, clutch, grind). Output ONLY the translation."
            CUSTOM -> "Translate from Spanish to English following these custom rules: ${customInstructions ?: "Natural modern tone"}. Output ONLY the translation."
        }
    }
}
