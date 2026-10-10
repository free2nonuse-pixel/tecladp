package com.linguakey.app.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguakey.app.domain.model.ProfileId

@Composable
fun LinguaKeyKeyboard(
    currentProfile: ProfileId,
    intensity: Int,
    isTranslating: Boolean,
    onKeyPress: (String) -> Unit,
    onTranslateClick: () -> Unit,
    onProfileChanged: (ProfileId) -> Unit,
    onIntensityChanged: (Int) -> Unit,
    onCloseKeyboard: () -> Unit
) {
    var isShifted by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131722))
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        // BARRA SUPERIOR: Selector de Perfil y Botón Traducir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Perfiles disponibles
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ProfileId.values().take(4).forEach { profile ->
                    val isSelected = profile == currentProfile
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFF1F2937),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onProfileChanged(profile) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = profile.displayName,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // BOTÓN TRADUCIR
            Button(
                onClick = onTranslateClick,
                enabled = !isTranslating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("⚡ Traducir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // FILAS DE TECLAS
        val rows = if (isSymbolsMode) {
            listOf(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
                listOf("=", "*", """, "'", ":", ";", "!", "?", "BACKSPACE"),
                listOf("ABC", ",", "SPACE", ".", "ENTER")
            )
        } else {
            val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
            val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ")
            val r3 = listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BACKSPACE")
            val r4 = listOf("?123", ",", "SPACE", ".", "ENTER")
            listOf(r1, r2, r3, r4)
        }

        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                rowKeys.forEach { keyLabel ->
                    val weight = when (keyLabel) {
                        "SPACE" -> 4f
                        "SHIFT", "BACKSPACE", "?123", "ABC", "ENTER" -> 1.5f
                        else -> 1f
                    }

                    KeyButton(
                        label = if (isShifted && keyLabel.length == 1) keyLabel.uppercase() else keyLabel,
                        modifier = Modifier.weight(weight),
                        onClick = {
                            when (keyLabel) {
                                "SHIFT" -> isShifted = !isShifted
                                "?123" -> isSymbolsMode = true
                                "ABC" -> isSymbolsMode = false
                                else -> {
                                    val sendChar = if (isShifted && keyLabel.length == 1) keyLabel.uppercase() else keyLabel
                                    onKeyPress(sendChar)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isAction = label in listOf("SHIFT", "BACKSPACE", "?123", "ABC", "ENTER")
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                color = if (isAction) Color(0xFF242C3D) else Color(0xFF1E2533),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (label) {
                "BACKSPACE" -> "⌫"
                "SHIFT" -> "⇧"
                "ENTER" -> "↵"
                "SPACE" -> ""
                else -> label
            },
            color = Color.White,
            fontSize = if (label.length > 2) 11.sp else 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
