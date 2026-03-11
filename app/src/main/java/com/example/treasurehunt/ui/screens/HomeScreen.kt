package com.example.treasurehunt.ui.screens

import android.R.attr.scaleX
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.treasurehunt.R
import com.example.treasurehunt.model.AdventureDifficulty
import com.example.treasurehunt.model.GameContent
import com.example.treasurehunt.model.PermissionStatus
import com.example.treasurehunt.model.PlayerProgress
import com.example.treasurehunt.ui.components.BulletLine
import com.example.treasurehunt.ui.components.CompassGlyph
import com.example.treasurehunt.ui.components.StarCircleButton
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
@Composable
fun HomeScreen(
    content: GameContent,
    permissionStatus: PermissionStatus,
    progress: PlayerProgress,
    onStorySelected: () -> Unit,
    onAdventureSelected: (AdventureDifficulty) -> Unit,
    onStickerCollection: () -> Unit,
    onPermissionsSelected: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4CC66))
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                StarCircleButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp)
                        .offset(y = (-24).dp),
                    onClick = onStickerCollection
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.96f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.asset_ribbonbanner_trimmed),
                        contentDescription = "Treasure Hunt banner",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.70f)
                            .offset(y = (-8).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompassGlyph(
                            color = Color(0xFFE07800),
                            modifier = Modifier.size(28.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.asset_treasuretext_trimmed),
                            contentDescription = "Treasure Hunt",
                            modifier = Modifier.weight(1f),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        }

        item {
            StartCard(
                title = "STORY MODE",
                onClick = onStorySelected,
                titleOffsetY = (-10).dp,
                startOffsetY = 20.dp
            )
        }

        item {
            StartCard(
                title = "ADVENTURE MODE",
                onClick = { onAdventureSelected(AdventureDifficulty.MEDIUM) },
                titleOffsetY = (-10).dp,
                startOffsetY = 20.dp
            )
        }

        item {
            HowToPlayCard(
                steps = content.uiText.howToPlaySteps,
                titleOffsetX = 0.dp,
                titleOffsetY = 14.dp,
                stepsTopPadding = 72.dp
            )
        }

        item {
            Button(
                onClick = onPermissionsSelected,
                modifier = Modifier
                    .fillMaxWidth(0.88f),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
            ) {
                Text(
                    text = when {
                        permissionStatus.fineGranted -> "LOCATION PERMISSIONS (PRECISE ENABLED)"
                        permissionStatus.coarseGranted -> "UPGRADE TO PRECISE LOCATION"
                        else -> "ENABLE LOCATION PERMISSIONS"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StartCard(
    title: String,
    onClick: () -> Unit,
    titleOffsetX: Dp = 0.dp,
    titleOffsetY: Dp = 0.dp,
    startOffsetX: Dp = 0.dp,
    startOffsetY: Dp = 0.dp
) {
    // Hunt Boxes
    Box(
        modifier = Modifier
            .fillMaxWidth(0.84f)
            .height(86.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.asset_whitebox_trimmed),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.971f).graphicsLayer(scaleX = 1.005f),
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = R.drawable.asset_boxoutline_trimmed),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = 1.01f, scaleY = 1.03f),

            contentScale = ContentScale.FillBounds
        )
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = titleOffsetX, y = titleOffsetY),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        Text(
            text = "- START -",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = startOffsetX, y = startOffsetY),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
    }
}

@Composable
private fun HowToPlayCard(
    steps: List<String>,
    modifier: Modifier = Modifier,
    titleOffsetX: Dp = 0.dp,
    titleOffsetY: Dp = 0.dp,
    stepsTopPadding: Dp = 72.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(top = 2.dp)
            .height(276.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.asset_howtobox_trimmed),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = R.drawable.asset_howtoboxoutline_trimmed),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = 1.01f, scaleY = 1.03f),
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = R.drawable.asset_howtotext_trimmed),
            contentDescription = "How To Play",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.82f)
                .offset(x = titleOffsetX, y = titleOffsetY),
            contentScale = ContentScale.FillWidth
        )
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 12.dp, top = stepsTopPadding, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            steps.forEach { step ->
                BulletLine(text = step)
            }
        }
    }
}
