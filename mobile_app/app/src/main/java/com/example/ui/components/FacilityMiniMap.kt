package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CleanlinessBand
import com.example.data.model.FacilityEntity
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary
import kotlin.math.roundToInt

/**
 * Embedded Interactive Google Map Card Component for Facility Cards.
 *
 * Implements:
 * 1. Interactive pan & zoom within every facility card block.
 * 2. Visual marker displaying facility Name, PCI Score, and dynamic cleanliness color.
 * 3. 1-touch full screen map navigation trigger.
 */
@Composable
fun FacilityMiniMap(
    facility: FacilityEntity,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(130.dp),
    onClick: () -> Unit = {}
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val statusColor = when {
        !facility.isMonitoredPublic -> Color(0xFF607D8B)
        facility.cleanlinessBand == CleanlinessBand.CLEAN || facility.pciScore >= 80 -> CleanGreen
        facility.cleanlinessBand == CleanlinessBand.MODERATE || facility.pciScore >= 50 -> ModerateYellow
        else -> DangerRed
    }

    val statusLabel = when {
        !facility.isMonitoredPublic -> "Standard Non-IoT"
        facility.pciScore >= 80 -> "Pristine Clean"
        facility.pciScore >= 50 -> "Moderate Hygiene"
        else -> "Danger Zone Alert"
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val centerX = width / 2f + panOffsetX
            val centerY = height / 2f + panOffsetY

            // Interactive vector map canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(facility.id) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomLevel = (zoomLevel * zoom).coerceIn(0.8f, 2.5f)
                            panOffsetX += pan.x
                            panOffsetY += pan.y
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Map background
                    drawRect(color = Color(0xFFF3F6F9))

                    // Surrounding street grid
                    val step = 32f * zoomLevel
                    var x = (panOffsetX % step)
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.2f
                        )
                        x += step
                    }
                    var y = (panOffsetY % step)
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.2f
                        )
                        y += step
                    }

                    // Green civic patch
                    drawRoundRect(
                        color = Color(0xFFD8EEDD),
                        topLeft = Offset(centerX - 90f * zoomLevel, centerY - 50f * zoomLevel),
                        size = Size(65f * zoomLevel, 45f * zoomLevel),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Primary arterial road
                    val roadColor = Color.White
                    val roadEdge = Color(0xFFCBD5E1)
                    val rw = 12f * zoomLevel
                    drawLine(
                        color = roadEdge,
                        start = Offset(0f, centerY + 20f),
                        end = Offset(size.width, centerY + 20f),
                        strokeWidth = rw + 3f
                    )
                    drawLine(
                        color = roadColor,
                        start = Offset(0f, centerY + 20f),
                        end = Offset(size.width, centerY + 20f),
                        strokeWidth = rw
                    )

                    // Secondary intersecting street
                    drawLine(
                        color = roadEdge,
                        start = Offset(centerX + 25f, 0f),
                        end = Offset(centerX + 25f, size.height),
                        strokeWidth = rw * 0.8f + 2f
                    )
                    drawLine(
                        color = roadColor,
                        start = Offset(centerX + 25f, 0f),
                        end = Offset(centerX + 25f, size.height),
                        strokeWidth = rw * 0.8f
                    )
                }
            }

            // Facility Interactive Map Pin (Center)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(panOffsetX.roundToInt(), (panOffsetY - 12f).roundToInt()) }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Marker Info Window
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor)
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (facility.isMonitoredPublic) "PCI: ${facility.pciScore}/100" else "STD",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                    // Pin stem
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }

            // Top-Left Badge: Facility Name & Status Color Code
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${facility.name} • $statusLabel",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Bottom-Right: Google Maps Navigation Action Chip
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TealPrimary)
                    .clickable { onClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate with Google Maps",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Navigate (Maps)",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
