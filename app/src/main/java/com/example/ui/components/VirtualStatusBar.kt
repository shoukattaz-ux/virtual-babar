package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.VirtualSurfaceCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Top Header / Status Bar for the Virtual Machine environment:
 * Displays simulated Wi-Fi, battery, 5G signal icons, sandbox security status,
 * time, and the active virtual phone model ("Running as: Google Pixel 8 Pro").
 */
@Composable
fun VirtualStatusBar(
    modifier: Modifier = Modifier,
    activePhoneModel: String = "Google Pixel 8 Pro",
    batteryLevel: Int = 98
) {
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(10000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VirtualSurfaceCard.copy(alpha = 0.90f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("virtual_status_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Virtual Time & "Running as: Model" status sync
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextPrimaryDark
                )
            )

            // Live Masked Model Tag ("Running as: Pixel 8 Pro")
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCyan.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CyberEmerald)
                )
                Text(
                    text = "Running as: $activePhoneModel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right side: Signal, Fake Wi-Fi, Fake Battery Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Virtual Environment Shield",
                modifier = Modifier.size(14.dp),
                tint = CyberEmerald
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCell,
                    contentDescription = "Simulated 5G Signal",
                    modifier = Modifier.size(15.dp),
                    tint = TextPrimaryDark
                )
                Text(
                    text = "5G",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Simulated Wi-Fi",
                modifier = Modifier.size(15.dp),
                tint = TextPrimaryDark
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryDark
                    )
                )
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Simulated Battery Level",
                    modifier = Modifier.size(15.dp),
                    tint = CyberEmerald
                )
            }
        }
    }
}
