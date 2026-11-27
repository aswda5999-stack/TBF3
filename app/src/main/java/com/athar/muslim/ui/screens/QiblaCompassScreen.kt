package com.athar.muslim.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val KAABA_LAT = 21.422487
private const val KAABA_LON = 39.826206

/** زاوية القبلة بالدرجات من الشمال الحقيقي (0..360) */
private fun qiblaBearing(lat: Double, lon: Double): Float {
    val phi1 = Math.toRadians(lat)
    val phi2 = Math.toRadians(KAABA_LAT)
    val deltaLambda = Math.toRadians(KAABA_LON - lon)
    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val degrees = Math.toDegrees(atan2(y, x))
    return ((degrees + 360.0) % 360.0).toFloat()
}

private fun readBestLocation(context: Context): Location? {
    val granted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    if (!granted) return null
    return try {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (provider in providers) {
            val loc = try {
                manager?.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
            if (loc != null && (best == null || loc.time > best.time)) best = loc
        }
        best
    } catch (_: Exception) {
        null
    }
}

@Composable
fun QiblaCompassScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    val locationState = remember { mutableStateOf(readBestLocation(context)) }
    val headingState = remember { mutableFloatStateOf(0f) }
    val sensorAvailableState = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensorManager == null || rotationSensor == null) {
            sensorAvailableState.value = false
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    try {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuth = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                        // تنعيم القراءة مع معالجة القفزة عند 0/360
                        val previous = headingState.floatValue
                        var delta = azimuth - previous
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f
                        if (abs(delta) > 0.5f) {
                            headingState.floatValue = ((previous + delta * 0.2f) + 360f) % 360f
                        }
                    } catch (_: Exception) {
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val location = locationState.value
    val bearing = if (location != null) qiblaBearing(location.latitude, location.longitude) else null
    val heading = headingState.floatValue
    val arrowRotation = if (bearing != null) (bearing - heading + 360f) % 360f else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "بوصلة القبلة",
            color = scheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(18.dp))

        if (bearing == null) {
            Text(
                text = "فعّل خدمة الموقع ومنح إذن الموقع للتطبيق ثم اضغط «تحديث الموقع» لحساب اتجاه القبلة.",
                color = scheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        } else {
            val ringColor = scheme.primary
            val arrowColor = scheme.tertiary
            val arrowBase = scheme.primaryContainer
            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .rotate(arrowRotation)
            ) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension / 2f - 6.dp.toPx()
                drawCircle(color = arrowBase, radius = r, center = c)
                drawCircle(color = ringColor, radius = r, center = c, style = Stroke(width = 4.dp.toPx()))
                val arrow = Path().apply {
                    moveTo(c.x, c.y - r + 14.dp.toPx())
                    lineTo(c.x + 26.dp.toPx(), c.y + 10.dp.toPx())
                    lineTo(c.x, c.y - 4.dp.toPx())
                    lineTo(c.x - 26.dp.toPx(), c.y + 10.dp.toPx())
                    close()
                }
                drawPath(arrow, color = arrowColor)
                drawCircle(color = ringColor, radius = 8.dp.toPx(), center = c)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "السهم الذهبي يشير إلى القبلة — زاوية القبلة ${bearing.roundToInt()}° من الشمال",
                color = scheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (!sensorAvailableState.value) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "جهازك لا يحتوي مستشعر اتجاه، لذلك يظهر السهم بالنسبة للشمال فقط.",
                    color = scheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = { locationState.value = readBestLocation(context) },
            colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
        ) {
            Text("تحديث الموقع", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "حرّك الهاتف على شكل رقم ثمانية للمعايرة وابتعد عن المغناطيس.",
            color = scheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
