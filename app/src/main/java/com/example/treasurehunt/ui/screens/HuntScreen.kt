package com.example.treasurehunt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.treasurehunt.R
import com.example.treasurehunt.game.GameEngine
import com.example.treasurehunt.model.ActiveHuntState
import com.example.treasurehunt.model.GameMode
import com.example.treasurehunt.model.GeoPoint
import com.example.treasurehunt.model.HuntCheckpoint
import com.example.treasurehunt.model.HuntGuidanceState
import com.example.treasurehunt.model.displayName
import com.example.treasurehunt.ui.components.AppCard
import com.example.treasurehunt.ui.components.RotatingPointer
import com.example.treasurehunt.ui.components.StatusBanner
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.StateFlow
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
private object HuntAssetTuning {
    val backArrowOffsetX: Dp = 0.dp
    val backArrowOffsetY: Dp = 0.dp
    val storyHeaderOffsetX: Dp = 0.dp
    val storyHeaderOffsetY: Dp = 0.dp
    val checkpointLabelOffsetX: Dp = (-20).dp
    val checkpointLabelOffsetY: Dp = 0.dp
    val checkpointCountOffsetX: Dp = 0.dp
    val checkpointCountOffsetY: Dp = 0.dp
    val subtitleOffsetX: Dp = 0.dp
    val subtitleOffsetY: Dp = 0.dp
    val compassTitleOffsetX: Dp = (-20).dp
    val compassTitleOffsetY: Dp = 0.dp
    val clueLabelOffsetX: Dp = 0.dp
    val clueLabelOffsetY: Dp = 0.dp
    val clueTitleOffsetX: Dp = 0.dp
    val clueTitleOffsetY: Dp = 0.dp
    val clueBodyOffsetX: Dp = 0.dp
    val clueBodyOffsetY: Dp = 0.dp
    val startTextOffsetX: Dp = 0.dp
    val startTextOffsetY: Dp = 0.dp
}

@Composable
fun HuntScreen(
    hunt: ActiveHuntState,
    guidanceState: StateFlow<HuntGuidanceState>,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onFoundIt: () -> Unit,
    onShowHint: () -> Unit,
    onDismissHint: () -> Unit,
    onCompassActiveChange: (Boolean) -> Unit
) {
    DisposableEffect(hunt.hasStarted) {
        onCompassActiveChange(hunt.hasStarted)
        onDispose { onCompassActiveChange(false) }
    }

    val progressLabel = GameEngine.checkpointProgressLabel(
        activeIndex = hunt.activeIndex,
        totalCount = hunt.checkpoints.size.coerceAtLeast(1)
    )
    val progressCountLabel = progressLabel.removePrefix("Checkpoint ")
    var showAllClues by rememberSaveable(
        hunt.mode.name,
        hunt.difficulty?.name,
        hunt.checkpoints.size
    ) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4CC66))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                if (hunt.mode == GameMode.STORY) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.asset_backarrow_hand),
                            contentDescription = "Back",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(width = 30.dp, height = 22.dp)
                                .offset(
                                    x = HuntAssetTuning.backArrowOffsetX,
                                    y = HuntAssetTuning.backArrowOffsetY
                                ),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            painter = painterResource(id = R.drawable.asset_storymode_hand),
                            contentDescription = "Story Mode",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(0.74f)
                                .offset(
                                    x = HuntAssetTuning.storyHeaderOffsetX,
                                    y = HuntAssetTuning.storyHeaderOffsetY
                                ),
                            contentScale = ContentScale.Fit
                        )
                        // Keep back behavior while using a static hand-drawn header image.
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(width = 64.dp, height = 52.dp)
                                .clickable(onClick = onBack)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "<",
                            modifier = Modifier
                                .clickable(onClick = onBack)
                                .padding(end = 12.dp),
                            style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFF2D2D2D)),
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = hunt.mode.displayName(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF3A1A11)
                            )
                        )
                    }
                }
            }

            hunt.statusMessage?.let { message ->
                item {
                    StatusBanner(
                        message = message,
                        modifier = Modifier.fillMaxWidth(),
                        isWarning = !message.contains("cleared", ignoreCase = true)
                    )
                }
            }

            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    background = Color.White,
                    border = Color(0xFF4B1D21)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showAllClues = !showAllClues }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.asset_checkpointtext_hand),
                                contentDescription = "Checkpoint",
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(22.dp)
                                    .offset(
                                        x = HuntAssetTuning.checkpointLabelOffsetX,
                                        y = HuntAssetTuning.checkpointLabelOffsetY
                                    ),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = progressCountLabel,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F1F1F)
                                ),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .offset(
                                        x = HuntAssetTuning.checkpointCountOffsetX,
                                        y = HuntAssetTuning.checkpointCountOffsetY
                                    )
                            )
                            Text(
                                text = if (showAllClues) " ^" else " v",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF5B6474),
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                            )
                        }
                        HandTimerChip(label = GameEngine.formatElapsed(hunt.elapsedMs))
                    }

                    Text(
                        text = hunt.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF6E7280),
                        modifier = Modifier.offset(
                            x = HuntAssetTuning.subtitleOffsetX,
                            y = HuntAssetTuning.subtitleOffsetY
                        )
                    )

                    if (hunt.mode == GameMode.ADVENTURE && !hunt.hasStarted && hunt.checkpoints.isEmpty()) {
                        Text(
                            text = "Tap Start Hunt to generate your route from the location where you are standing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5A5F6C)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val checkpoints = hunt.checkpoints.ifEmpty {
                            listOf(
                                HuntCheckpoint(
                                    id = "placeholder",
                                    title = "Current clue",
                                    clue = "Press Start Hunt to reveal the first clue.",
                                    hint = "",
                                    target = GeoPoint(0.0, 0.0),
                                    isFinalTarget = false
                                )
                            )
                        }
                        val visibleCheckpoints = if (showAllClues) {
                            checkpoints
                        } else {
                            listOf(checkpoints.getOrElse(hunt.activeIndex.coerceAtLeast(0)) { checkpoints.first() })
                        }
                        visibleCheckpoints.forEach { checkpoint ->
                            val index = checkpoints.indexOfFirst { it.id == checkpoint.id }
                            val state = when {
                                index < hunt.activeIndex || checkpoint.completed -> CheckpointCardState.COMPLETED
                                index == hunt.activeIndex -> CheckpointCardState.CURRENT
                                else -> CheckpointCardState.LOCKED
                            }
                            CheckpointCard(
                                title = checkpoint.title,
                                body = when {
                                    !hunt.hasStarted && state == CheckpointCardState.CURRENT && hunt.mode == GameMode.STORY ->
                                        "Press Start Hunt to reveal the first clue."
                                    !hunt.hasStarted && state == CheckpointCardState.CURRENT ->
                                        "Press Start Hunt to generate your first checkpoint."
                                    state == CheckpointCardState.LOCKED ->
                                        "Complete the previous checkpoint to unlock this clue."
                                    else -> checkpoint.clue
                                },
                                state = state,
                                showHint = state == CheckpointCardState.CURRENT && hunt.hasStarted,
                                onHintClick = onShowHint
                            )
                        }
                    }
                }
            }

            item {
                CompassCard(
                    huntHasStarted = hunt.hasStarted,
                    guidanceState = guidanceState
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(onClick = if (hunt.hasStarted) onFoundIt else onStart),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.asset_starthuntbox_hand),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    if (!hunt.hasStarted) {
                        Image(
                            painter = painterResource(id = R.drawable.asset_starthunttext_hand),
                            contentDescription = "Start Hunt",
                            modifier = Modifier
                                .fillMaxHeight(0.58f)
                                .offset(
                                    x = HuntAssetTuning.startTextOffsetX,
                                    y = HuntAssetTuning.startTextOffsetY
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = "Found It",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (hunt.isHintVisible) {
        AlertDialog(
            onDismissRequest = onDismissHint,
            title = {
                Text(
                    text = hunt.activeCheckpoint?.title ?: "Hint",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text(
                    text = hunt.activeCheckpoint?.hint ?: "No hint available yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                OutlinedButton(onClick = onDismissHint) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CheckpointCard(
    title: String,
    body: String,
    state: CheckpointCardState,
    showHint: Boolean,
    onHintClick: () -> Unit
) {
    val titleColor = if (state == CheckpointCardState.LOCKED) Color(0xFF7C838F) else Color(0xFF232834)
    val bodyColor = if (state == CheckpointCardState.LOCKED) Color(0xFF7C838F) else Color(0xFF232834)
    val iconAlpha = if (state == CheckpointCardState.LOCKED) 0.45f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 98.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.asset_cluebox_hand),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = if (state == CheckpointCardState.COMPLETED) 0.92f else 1f
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.asset_lightbulb_hand),
                        contentDescription = "Clue",
                        modifier = Modifier
                            .size(20.dp)
                            .offset(y = (-1).dp),
                        contentScale = ContentScale.Fit,
                        alpha = iconAlpha
                    )
                    Image(
                        painter = painterResource(id = R.drawable.asset_cluetext_hand),
                        contentDescription = "Clue",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(18.dp)
                            .offset(
                                x = HuntAssetTuning.clueLabelOffsetX,
                                y = HuntAssetTuning.clueLabelOffsetY
                            ),
                        contentScale = ContentScale.Fit,
                        alpha = iconAlpha
                    )
                    Text(
                        text = " $title",
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .offset(
                                x = HuntAssetTuning.clueTitleOffsetX,
                                y = HuntAssetTuning.clueTitleOffsetY
                            ),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = titleColor
                        )
                    )
                }
                if (showHint) {
                    OutlinedButton(
                        onClick = onHintClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF4B1D21))
                    ) {
                        Text("Hint")
                    }
                }
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = bodyColor,
                    lineHeight = 18.sp
                ),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(
                    x = HuntAssetTuning.clueBodyOffsetX,
                    y = HuntAssetTuning.clueBodyOffsetY
                )
            )
        }
    }
}

@Composable
private fun CompassCard(
    huntHasStarted: Boolean,
    guidanceState: StateFlow<HuntGuidanceState>
) {
    val guidance by guidanceState.collectAsStateWithLifecycle()
    val heading = guidance.headingDegrees
    val bearing = guidance.absoluteBearingToTarget
    val relativeBearing = if (heading != null && bearing != null) {
        GameEngine.relativeBearingDegrees(heading, bearing)
    } else {
        0f
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        background = Color(0xFFF0F1F5),
        border = Color(0xFF4B1D21)
    ) {
        Image(
            painter = painterResource(id = R.drawable.asset_compasstext_hand),
            contentDescription = "Compass",
            modifier = Modifier
                .fillMaxWidth(0.42f)
                .height(24.dp)
                .offset(
                    x = HuntAssetTuning.compassTitleOffsetX,
                    y = HuntAssetTuning.compassTitleOffsetY
                ),
            contentScale = ContentScale.Fit
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RotatingPointer(
                    rotationDegrees = relativeBearing,
                    modifier = Modifier.size(108.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = guidance.headingDegrees?.let { "Heading ${it.roundToInt()} deg" }
                        ?: "Waiting for compass...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = guidance.distanceToTargetMeters?.let { "About ${it.roundToInt()} m to target" }
                        ?: if (huntHasStarted) {
                            "Move outside for a stronger location signal."
                        } else {
                            "Start the hunt to activate guidance."
                        },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF49505C),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

private enum class CheckpointCardState {
    CURRENT,
    LOCKED,
    COMPLETED
}

@Composable
private fun HandTimerChip(
    label: String
) {
    Box(
        modifier = Modifier.size(width = 82.dp, height = 44.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.asset_timerbox_hand),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        )
    }
}
