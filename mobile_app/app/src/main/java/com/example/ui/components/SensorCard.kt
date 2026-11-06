package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.FacilityEntity
import com.example.data.model.FlushHealthStatus
import com.example.localization.LocalizationManager
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow

@Composable
fun SensorGrid(
    facility: FacilityEntity,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sensor 1: MQ-135 Ammonia
            val isAmmoniaAlert = facility.ammoniaPpm > 6.0f
            SensorItemCard(
                modifier = Modifier.weight(1f),
                title = LocalizationManager.getAmmoniaOdor(lang),
                value = "${facility.ammoniaPpm} ppm",
                status = if (isAmmoniaAlert) "High / Alert" else "Safe Level",
                statusColor = if (isAmmoniaAlert) DangerRed else CleanGreen,
                icon = Icons.Default.Air,
                tag = "sensor_mq135"
            )

            // Sensor 2: YF-S201 Water Flow / Flush Health
            val isFlowAbnormal = facility.flushHealthStatus != FlushHealthStatus.NORMAL
            SensorItemCard(
                modifier = Modifier.weight(1f),
                title = LocalizationManager.getWaterFlow(lang),
                value = "${facility.flushFlowRate} L/m",
                status = facility.flushHealthStatus.displayName,
                statusColor = if (isFlowAbnormal) DangerRed else CleanGreen,
                icon = Icons.Default.WaterDrop,
                tag = "sensor_yfs201"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sensor 3: PIR HC-SR501 Occupancy Footfall
            SensorItemCard(
                modifier = Modifier.weight(1f),
                title = LocalizationManager.getOccupancyLabel(lang),
                value = "${facility.occupancyCount} visits",
                status = if (facility.isOccupied) "Currently In Use" else "Vacant",
                statusColor = if (facility.isOccupied) ModerateYellow else CleanGreen,
                icon = Icons.Default.Groups,
                tag = "sensor_pir"
            )

            // Sensor 4: DHT22 Temp & Humidity
            SensorItemCard(
                modifier = Modifier.weight(1f),
                title = LocalizationManager.getTempHumidity(lang),
                value = "${facility.temperatureC}°C",
                status = "${facility.humidityPct}% Humidity",
                statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
                icon = Icons.Default.DeviceThermostat,
                tag = "sensor_dht22"
            )
        }
    }
}

@Composable
fun SensorItemCard(
    title: String,
    value: String,
    status: String,
    statusColor: Color,
    icon: ImageVector,
    tag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = statusColor,
                maxLines = 1
            )
        }
    }
}
