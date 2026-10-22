package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberIndigo
import com.example.ui.theme.CyberRose
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VirtualBgDark
import com.example.ui.theme.VirtualBorder
import com.example.ui.theme.VirtualSurfaceCard
import com.example.ui.theme.VirtualSurfaceDark
import com.example.util.SensorJitterShieldManager
import com.example.util.ShieldIntensity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorShieldScreen(
    onNavigateBack: () -> Unit
) {
    val telemetry by SensorJitterShieldManager.telemetry.collectAsState()
    BackHandler { onNavigateBack() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sensor_shield_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sensor & Behavioral Shield",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "Silicon Defect & Cadence Fingerprinting Defense",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sensor_shield_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VirtualSurfaceDark)
            )
        },
        containerColor = VirtualBgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Shield Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (telemetry.isShieldEnabled) VirtualSurfaceCard else VirtualSurfaceDark
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (telemetry.isShieldEnabled) CyberEmerald.copy(alpha = 0.6f) else VirtualBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (telemetry.isShieldEnabled) CyberEmerald.copy(alpha = 0.2f)
                                            else VirtualBorder
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (telemetry.isShieldEnabled) CyberEmerald else TextMutedDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (telemetry.isShieldEnabled) "Anti-Tracking Shield ACTIVE" else "Shield Paused",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (telemetry.isShieldEnabled) CyberEmerald else TextPrimaryDark
                                        )
                                    )
                                    Text(
                                        text = "Intercepting SensorEvent & MotionEvent streams",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = TextSecondaryDark
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = telemetry.isShieldEnabled,
                                onCheckedChange = { SensorJitterShieldManager.toggleShield(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberEmerald,
                                    checkedTrackColor = CyberEmerald.copy(alpha = 0.3f),
                                    uncheckedThumbColor = TextMutedDark,
                                    uncheckedTrackColor = VirtualSurfaceDark
                                ),
                                modifier = Modifier.testTag("toggle_shield_switch")
                            )
                        }

                        Text(
                            text = "Microscopic manufacturing defects in accelerometers and gyroscopes create a unique hardware signature. The virtual proxy injects random micro-noise (±${telemetry.intensity.jitterAmplitude}) to neutralize device tracking.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        )
                    }
                }
            }

            // Shield Intensity Preset Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jitter Intensity Level",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShieldIntensity.entries.filter { it != ShieldIntensity.OFF }.forEach { level ->
                            val isSelected = telemetry.intensity == level
                            FilterChip(
                                selected = isSelected,
                                onClick = { SensorJitterShieldManager.setShieldIntensity(level) },
                                label = {
                                    Text(
                                        text = level.label.substringBefore(" "),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberCyan,
                                    containerColor = VirtualSurfaceCard,
                                    labelColor = TextSecondaryDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyberCyan else VirtualBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Dual Real-time Waveform / Sensor Scope Canvas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
                    border = BorderStroke(1.dp, VirtualBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Live Silicon Jitter Oscilloscope",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CyberCyan)
                                    )
                                    Text("Raw", style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan, fontSize = 10.sp))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CyberEmerald)
                                    )
                                    Text("Shielded", style = MaterialTheme.typography.labelSmall.copy(color = CyberEmerald, fontSize = 10.sp))
                                }
                            }
                        }

                        // Canvas Graph Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF070B14))
                                .border(BorderStroke(1.dp, VirtualBorder), RoundedCornerShape(12.dp))
                        ) {
                            SensorWaveformCanvas(
                                history = telemetry.accelHistory,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Live Readings Comparison Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SensorMetricPill(
                                title = "RAW ACCEL (X, Y, Z)",
                                value = String.format("%.3f, %.3f, %.3f", telemetry.rawAccel.first, telemetry.rawAccel.second, telemetry.rawAccel.third),
                                color = CyberCyan,
                                modifier = Modifier.weight(1f)
                            )
                            SensorMetricPill(
                                title = "SHIELDED (JITTERED)",
                                value = String.format("%.3f, %.3f, %.3f", telemetry.jitteredAccel.first, telemetry.jitteredAccel.second, telemetry.jitteredAccel.third),
                                color = CyberEmerald,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Behavioral Touch Cadence Scrambler Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VirtualSurfaceCard),
                    border = BorderStroke(1.dp, VirtualBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = CyberIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Touch Cadence Scrambler Pad",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                )
                            }

                            Switch(
                                checked = telemetry.isTouchScramblerEnabled,
                                onCheckedChange = { SensorJitterShieldManager.toggleTouchScrambler(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberIndigo,
                                    checkedTrackColor = CyberIndigo.copy(alpha = 0.3f),
                                    uncheckedThumbColor = TextMutedDark,
                                    uncheckedTrackColor = VirtualSurfaceDark
                                ),
                                modifier = Modifier.testTag("toggle_touch_scrambler")
                            )
                        }

                        Text(
                            text = "Tap or drum repeatedly on the pad below. It intercepts raw timing intervals and adds microsecond jitter to defeat biometric typing profiling:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondaryDark)
                        )

                        // Interactive Tap Test Pad
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberIndigo.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, CyberIndigo.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        SensorJitterShieldManager.processTouchEvent(rawPressure = 0.85f)
                                    }
                                )
                                .testTag("touch_jitter_test_pad"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = CyberIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "TAP HERE RAPIDLY TO TEST TOUCH JITTER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = CyberIndigo
                                    )
                                )
                            }
                        }

                        // Last Tap Cadence Telemetry Log
                        val lastSample = telemetry.recentTouchSamples.lastOrNull()
                        if (lastSample != null && lastSample.rawIntervalMs > 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VirtualSurfaceDark)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Raw Inter-Tap Interval:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 10.sp))
                                    Text("${lastSample.rawIntervalMs} ms", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = CyberCyan, fontSize = 10.sp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Shielded Dispatch Cadence:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 10.sp))
                                    Text("${lastSample.jitteredIntervalMs} ms (Jitter: ${lastSample.jitteredIntervalMs - lastSample.rawIntervalMs}ms)", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = CyberEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pressure Signature:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 10.sp))
                                    Text(String.format("Raw: %.2f ➔ Masked: %.3f", lastSample.rawPressure, lastSample.jitteredPressure), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = CyberAmber, fontSize = 10.sp))
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Protection Metrics Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VirtualSurfaceDark),
                    border = BorderStroke(1.dp, VirtualBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Telemetry & Anti-Fraud Neutralization",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )

                        HorizontalDivider(color = VirtualBorder, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Intercepted Events:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 11.sp))
                            Text("${telemetry.totalInterceptedCount}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 11.sp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Micro-Jitter Noise Band:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 11.sp))
                            Text("±${telemetry.intensity.jitterAmplitude} (Gaussian)", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = CyberEmerald, fontSize = 11.sp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sensor Entropy Score:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 11.sp))
                            Text("${telemetry.entropyScorePercent}% (Undetectable)", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = CyberAmber, fontSize = 11.sp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hardware Shield Coverage:", style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark, fontSize = 11.sp))
                            Text("Accel, Gyro, Magnet, Touch", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = TextPrimaryDark, fontSize = 11.sp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorWaveformCanvas(
    history: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw horizontal center grid line
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        if (history.size < 2) return@Canvas

        val stepX = width / (history.size - 1).coerceAtLeast(1)

        val rawPath = Path()
        val jitteredPath = Path()

        history.forEachIndexed { index, (raw, jittered) ->
            val x = index * stepX
            // Scale accelerometer variation
            val yRaw = (centerY - (raw * 180f)).coerceIn(8f, height - 8f)
            val yJitter = (centerY - (jittered * 180f)).coerceIn(8f, height - 8f)

            if (index == 0) {
                rawPath.moveTo(x, yRaw)
                jitteredPath.moveTo(x, yJitter)
            } else {
                rawPath.lineTo(x, yRaw)
                jitteredPath.lineTo(x, yJitter)
            }
        }

        // Draw Raw Trace (Cyan)
        drawPath(
            path = rawPath,
            color = CyberCyan.copy(alpha = 0.85f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Draw Jittered Trace (Emerald)
        drawPath(
            path = jitteredPath,
            color = CyberEmerald,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun SensorMetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(VirtualSurfaceDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = TextMutedDark,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            ),
            maxLines = 1
        )
    }
}
