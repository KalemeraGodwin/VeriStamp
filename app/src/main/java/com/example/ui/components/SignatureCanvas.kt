package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack
import kotlin.math.hypot

data class StrokeLine(val points: List<Offset>)

enum class DrawingTool {
    PEN,
    RUBBER
}

@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    strokes: MutableList<StrokeLine>,
    onStrokesChanged: () -> Unit
) {
    var selectedTool by remember { mutableStateOf(DrawingTool.PEN) }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var rubberPosition by remember { mutableStateOf<Offset?>(null) }

    fun eraseNear(touch: Offset, radius: Float = 36f) {
        val newStrokes = mutableListOf<StrokeLine>()
        var changed = false

        for (stroke in strokes) {
            val remainingSegments = mutableListOf<MutableList<Offset>>()
            var currentSegment = mutableListOf<Offset>()

            for (point in stroke.points) {
                val dist = hypot((point.x - touch.x).toDouble(), (point.y - touch.y).toDouble()).toFloat()
                if (dist > radius) {
                    currentSegment.add(point)
                } else {
                    changed = true
                    if (currentSegment.size > 1) {
                        remainingSegments.add(currentSegment)
                    }
                    currentSegment = mutableListOf()
                }
            }
            if (currentSegment.size > 1) {
                remainingSegments.add(currentSegment)
            }

            for (seg in remainingSegments) {
                newStrokes.add(StrokeLine(seg))
            }
        }

        if (changed) {
            strokes.clear()
            strokes.addAll(newStrokes)
            onStrokesChanged()
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Selectors: Pen vs Rubber
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedTool == DrawingTool.PEN,
                    onClick = { selectedTool = DrawingTool.PEN },
                    label = { Text("Sign", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = RichBlack,
                        selectedLeadingIconColor = RichBlack
                    ),
                    modifier = Modifier.height(30.dp).testTag("tool_pen")
                )

                FilterChip(
                    selected = selectedTool == DrawingTool.RUBBER,
                    onClick = { selectedTool = DrawingTool.RUBBER },
                    label = { Text("Rubber", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = RichBlack,
                        selectedLeadingIconColor = RichBlack
                    ),
                    modifier = Modifier.height(30.dp).testTag("tool_rubber")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            strokes.removeAt(strokes.lastIndex)
                            onStrokesChanged()
                        }
                    },
                    enabled = strokes.isNotEmpty(),
                    modifier = Modifier.height(30.dp).testTag("signature_undo_btn"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(2.dp))
                    Text("Undo", fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = {
                        strokes.clear()
                        currentPoints.clear()
                        onStrokesChanged()
                    },
                    enabled = strokes.isNotEmpty() || currentPoints.isNotEmpty(),
                    modifier = Modifier.height(30.dp).testTag("signature_clear_btn"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(2.dp))
                    Text("Clear", fontSize = 10.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color.White)
                .border(
                    width = 1.dp,
                    color = if (selectedTool == DrawingTool.RUBBER) GoldPrimary else GoldBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .testTag("signature_touch_box")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (selectedTool == DrawingTool.PEN) {
                                    currentPoints.clear()
                                    currentPoints.add(offset)
                                } else {
                                    rubberPosition = offset
                                    eraseNear(offset)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (selectedTool == DrawingTool.PEN) {
                                    currentPoints.add(change.position)
                                } else {
                                    rubberPosition = change.position
                                    eraseNear(change.position)
                                }
                            },
                            onDragEnd = {
                                if (selectedTool == DrawingTool.PEN) {
                                    if (currentPoints.isNotEmpty()) {
                                        strokes.add(StrokeLine(currentPoints.toList()))
                                        currentPoints.clear()
                                        onStrokesChanged()
                                    }
                                } else {
                                    rubberPosition = null
                                }
                            },
                            onDragCancel = {
                                currentPoints.clear()
                                rubberPosition = null
                            }
                        )
                    }
            ) {
                // Baseline guide
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                    start = Offset(20f, size.height - 24f),
                    end = Offset(size.width - 20f, size.height - 24f),
                    strokeWidth = 1f
                )

                // Draw confirmed strokes
                for (stroke in strokes) {
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = RichBlack,
                            style = Stroke(
                                width = 4f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Draw active pen stroke
                if (currentPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = RichBlack,
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Rubber cursor indicator
                rubberPosition?.let { pos ->
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color(0x55D4AF37),
                        radius = 28f,
                        center = pos
                    )
                    drawCircle(
                        color = GoldPrimary,
                        radius = 28f,
                        center = pos,
                        style = Stroke(width = 2f)
                    )
                }
            }

            if (strokes.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = if (selectedTool == DrawingTool.RUBBER) "Drag finger over strokes to erase" else "Sign with your finger here",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

fun exportStrokesToBitmap(
    strokes: List<StrokeLine>,
    width: Int = 400,
    height: Int = 140
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.TRANSPARENT)

    val validStrokes = strokes.filter { it.points.size > 1 }
    if (validStrokes.isEmpty()) {
        return bitmap
    }

    // Calculate bounding box of all strokes
    var minX = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var minY = Float.MAX_VALUE
    var maxY = Float.MIN_VALUE

    for (stroke in validStrokes) {
        for (point in stroke.points) {
            if (point.x < minX) minX = point.x
            if (point.x > maxX) maxX = point.x
            if (point.y < minY) minY = point.y
            if (point.y > maxY) maxY = point.y
        }
    }

    if (minX >= maxX || minY >= maxY) {
        return bitmap
    }

    val contentW = maxX - minX
    val contentH = maxY - minY
    val padding = 16f
    val targetW = width - (padding * 2f)
    val targetH = height - (padding * 2f)

    val scale = minOf(targetW / contentW, targetH / contentH).coerceAtMost(2.5f)
    val drawW = contentW * scale
    val drawH = contentH * scale
    val offsetX = padding + (targetW - drawW) / 2f
    val offsetY = padding + (targetH - drawH) / 2f

    val paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        strokeWidth = (4.5f * scale.coerceIn(0.6f, 1.5f)).coerceIn(3.5f, 7f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    for (stroke in validStrokes) {
        val path = android.graphics.Path()
        val p0 = stroke.points[0]
        path.moveTo(offsetX + (p0.x - minX) * scale, offsetY + (p0.y - minY) * scale)
        for (i in 1 until stroke.points.size) {
            val p = stroke.points[i]
            path.lineTo(offsetX + (p.x - minX) * scale, offsetY + (p.y - minY) * scale)
        }
        canvas.drawPath(path, paint)
    }

    return bitmap
}
