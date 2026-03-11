package com.example.treasurehunt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.treasurehunt.model.GameContent
import com.example.treasurehunt.model.PermissionStatus
import com.example.treasurehunt.ui.components.AppCard
import com.example.treasurehunt.ui.components.CompassGlyph
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
@Composable
fun PermissionsScreen(
    permissionStatus: PermissionStatus,
    content: GameContent?,
    onRequestPermissions: () -> Unit,
    onContinue: () -> Unit
) {
    val uiText = content?.uiText
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7E8D1))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            background = Color(0xFFFFF6EA),
            border = Color(0xFFE4BC68)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                CompassGlyph(
                    color = Color(0xFFE07800),
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = uiText?.permissionsTitle ?: "Location Access",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E4100)
                    )
                )
                Text(
                    text = uiText?.permissionsBody
                        ?: "Treasure Hunt needs precise location to validate clues and keep the adventure fair.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6C4623)
                )
                Text(
                    text = uiText?.approximateBody
                        ?: "Approximate location still works, but scoring and target checks become more lenient.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8F6238)
                )
                if (permissionStatus.fineGranted) {
                    Text(
                        text = "Precise location is already enabled.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1D7C33)
                    )
                }
                Button(
                    onClick = onRequestPermissions,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6A258)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant location access")
                }
                OutlinedButton(
                    onClick = onContinue,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to home")
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
