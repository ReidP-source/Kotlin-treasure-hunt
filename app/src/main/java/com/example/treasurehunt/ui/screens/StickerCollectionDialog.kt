package com.example.treasurehunt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.treasurehunt.model.GameContent
import com.example.treasurehunt.model.PlayerProgress
import com.example.treasurehunt.model.StickerDefinition
import com.example.treasurehunt.ui.components.StickerBadge
import com.example.treasurehunt.ui.components.UnderlineTitle
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
@Composable
fun StickerCollectionDialog(
    content: GameContent,
    progress: PlayerProgress,
    onDismiss: () -> Unit
) {
    val stickerGridItems = remember(content.stickers) { buildStickerGridItems(content.stickers) }
    var selectedStickerId by remember(content.stickers) {
        mutableStateOf(content.stickers.firstOrNull()?.id)
    }
    val selectedSticker = content.stickers.firstOrNull { it.id == selectedStickerId }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 114.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UnderlineTitle(
                        text = "STICKERS",
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            items = stickerGridItems,
                            key = { index, sticker -> sticker?.id ?: "empty-$index" }
                        ) { _, sticker ->
                            if (sticker == null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                            } else {
                                val unlocked = progress.unlockedStickerIds.contains(sticker.id)
                                val interactionSource = remember(sticker.id) { MutableInteractionSource() }
                                StickerBadge(
                                    stickerId = sticker.id,
                                    unlocked = unlocked,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { selectedStickerId = sticker.id }
                                )
                            }
                        }
                    }
                    selectedSticker?.let { sticker ->
                        val unlocked = progress.unlockedStickerIds.contains(sticker.id)
                        StickerDetail(
                            sticker = sticker,
                            unlocked = unlocked
                        )
                    }
                    Text(
                        text = "${progress.unlockedStickerIds.size} / ${content.stickers.size} unlocked",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.Black
                    )
                    Text(
                        text = "Close",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(onClick = onDismiss),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StickerDetail(
    sticker: StickerDefinition,
    unlocked: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = sticker.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.Black
        )
        Text(
            text = sticker.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B4B4B)
        )
        Text(
            text = if (unlocked) {
                "Earned from: ${stickerUnlockSource(sticker)}"
            } else {
                "Unlock by: ${stickerUnlockSource(sticker)}"
            },
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF6A4D17)
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
}

private fun stickerUnlockSource(sticker: StickerDefinition): String {
    if (sticker.storyReward) {
        return "completing Story Mode for the first time"
    }
    val labels = sticker.difficulties
        .map { it.lowercase() }
        .mapNotNull { difficulty ->
            when (difficulty) {
                "easy" -> "Easy"
                "medium" -> "Medium"
                "hard" -> "Hard"
                else -> null
            }
        }
    return when {
        labels.isEmpty() -> "completing Adventure Mode"
        labels.size == 1 -> "Adventure Mode ${labels.first()}"
        else -> "Adventure Mode (${labels.joinToString(", ")})"
    }
}

private fun buildStickerGridItems(stickers: List<StickerDefinition>): List<StickerDefinition?> {
    val byId = stickers.associateBy { it.id }
    val result = mutableListOf<StickerDefinition?>()
    val preferredOrder = listOf(
        "gift",
        "crown",
        "gem",
        "wand",
        "map",
        "compass",
        "trophy",
        "star",
        "key"
    )

    preferredOrder.forEach { id ->
        byId[id]?.let(result::add)
    }

    // Center candy in row 4, column 2 by reserving row 4, column 1.
    byId["candy"]?.let { candy ->
        result.add(null)
        result.add(candy)
    }

    stickers
        .filter { sticker ->
            sticker.id !in preferredOrder && sticker.id != "candy"
        }
        .forEach(result::add)

    return result
}
