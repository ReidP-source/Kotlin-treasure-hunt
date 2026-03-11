package com.example.treasurehunt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.treasurehunt.game.GameEngine
import com.example.treasurehunt.model.GameContent
import com.example.treasurehunt.model.HuntCompletion
import com.example.treasurehunt.ui.components.AppCard
import com.example.treasurehunt.ui.components.StickerBadge
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
@Composable
fun CompletionScreen(
    completion: HuntCompletion,
    content: GameContent,
    onHome: () -> Unit
) {
    var showReward by remember(completion.rewardStickerId) { mutableStateOf(completion.rewardWasNew) }
    val rewardSticker = content.stickers.firstOrNull { it.id == completion.rewardStickerId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7E8D1))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(26.dp))
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                background = Color(0xFFFFF8EC),
                border = Color(0xFFE6BC64)
            ) {
                Text(
                    text = "Treasure Hunt Completed",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9A4F00)
                    )
                )
                Text(
                    text = completion.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6F4A28)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryPill("Time", GameEngine.formatElapsed(completion.elapsedMs))
                    SummaryPill("Clue", completion.finalCheckpointTitle)
                }
                Text(
                    text = "Final clue location",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF8C5200)
                )
                Text(
                    text = completion.finalCheckpointHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6F4A28)
                )
                Text(
                    text = completion.finalCheckpointLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6E3800)
                )
                Text(
                    text = "Stars earned: ${"*".repeat(completion.stars)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFB36C10)
                )
                Button(
                    onClick = onHome,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6A258)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        }

        if (showReward && rewardSticker != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sticker Unlocked",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    StickerBadge(
                        stickerId = rewardSticker.id,
                        unlocked = true,
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                    )
                    Text(
                        text = rewardSticker.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = rewardSticker.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5D6470)
                    )
                    Button(
                        onClick = { showReward = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6A258))
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3E1C5)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9A4F00)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF6E3800)
            )
        }
    }
}
