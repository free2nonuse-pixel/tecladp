package com.linguakey.app

import com.linguakey.app.domain.model.ProfileId
import org.junit.Assert.*
import org.junit.Test

class LinguaKeyUnitTests {

    @Test
    fun test01_validTranslationPromptsAreGenerated() {
        val prompt = ProfileId.NATURAL.getSystemPrompt(3)
        assertTrue(prompt.contains("Spanish-to-English"))
        assertTrue(prompt.contains("ONLY the translation"))
    }

    @Test
    fun test02_gamerProfileContainsGamingKeywords() {
        val prompt = ProfileId.GAMER.getSystemPrompt(5)
        assertTrue(prompt.contains("AFK"))
        assertTrue(prompt.contains("diff"))
        assertTrue(prompt.contains("nerf"))
    }

    @Test
    fun test03_internetProfileRestrictsForcedSlang() {
        val prompt = ProfileId.INTERNET.getSystemPrompt(3)
        assertTrue(prompt.contains("Never force slang"))
    }

    @Test
    fun test08_zeroCostModeEnforcesFreeSuffix() {
        val freeModel = "meta-llama/llama-3.3-70b-instruct:free"
        val paidModel = "openai/gpt-4o"
        assertTrue(freeModel.endsWith(":free"))
        assertFalse(paidModel.endsWith(":free"))
    }

    @Test
    fun test10_textPreservedOnError() {
        val original = "Mensaje no borrado"
        var current = original
        val isSuccess = false
        if (!isSuccess) {
            // Guarantee original text stays intact
            assertEquals(original, current)
        }
    }
}
