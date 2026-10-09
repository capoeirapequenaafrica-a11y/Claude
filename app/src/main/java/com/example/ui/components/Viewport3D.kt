package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Mesh3D
import com.example.engine.Vector3
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.VioletAccent
import kotlin.math.PI
import kotlin.math.max

enum class RenderMode3D {
    SHADED,
    WIREFRAME,
    VERTICES,
    HYBRID
}

@Composable
fun Viewport3D(
    mesh: Mesh3D,
    renderMode: RenderMode3D = RenderMode3D.HYBRID,
    autoRotate: Boolean = true,
    scaleFactor: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    var yaw by remember { mutableFloatStateOf(0.4f) }
    var pitch by remember { mutableFloatStateOf(-0.35f) }

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoRotate"
    )

    val currentYaw = if (autoRotate) yaw + spinAngle else yaw
    val lightDir = remember { Vector3(0.5f, 0.8f, 1.0f).normalize() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B14))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    yaw += dragAmount.x * 0.01f
                    pitch += dragAmount.y * 0.01f
                    pitch = pitch.coerceIn(-1.5f, 1.5f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val fov = size.minDimension * 0.85f * scaleFactor
            val cameraDist = 4.2f

            // Transform vertices: Pitch (X), Yaw (Y)
            val transformedVertices = mesh.vertices.map { v ->
                v.rotateX(pitch).rotateY(currentYaw)
            }

            // Project 3D -> 2D
            val projected = transformedVertices.map { v ->
                val zDepth = v.z + cameraDist
                val zSafe = if (zDepth < 0.2f) 0.2f else zDepth
                val px = (v.x / zSafe) * fov + cx
                val py = (-v.y / zSafe) * fov + cy
                Offset(px, py)
            }

            // 1. Draw Subtle 3D Perspective Grid Floor
            val gridY = -1.6f
            val gridRange = listOf(-2f, -1f, 0f, 1f, 2f)
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.5f)

            for (gx in gridRange) {
                val p1 = Vector3(gx, gridY, -2f).rotateX(pitch).rotateY(currentYaw)
                val p2 = Vector3(gx, gridY, 2f).rotateX(pitch).rotateY(currentYaw)
                val z1 = max(0.2f, p1.z + cameraDist)
                val z2 = max(0.2f, p2.z + cameraDist)
                val off1 = Offset((p1.x / z1) * fov + cx, (-p1.y / z1) * fov + cy)
                val off2 = Offset((p2.x / z2) * fov + cx, (-p2.y / z2) * fov + cy)
                drawLine(gridColor, off1, off2, strokeWidth = 1f)
            }
            for (gz in gridRange) {
                val p1 = Vector3(-2f, gridY, gz).rotateX(pitch).rotateY(currentYaw)
                val p2 = Vector3(2f, gridY, gz).rotateX(pitch).rotateY(currentYaw)
                val z1 = max(0.2f, p1.z + cameraDist)
                val z2 = max(0.2f, p2.z + cameraDist)
                val off1 = Offset((p1.x / z1) * fov + cx, (-p1.y / z1) * fov + cy)
                val off2 = Offset((p2.x / z2) * fov + cx, (-p2.y / z2) * fov + cy)
                drawLine(gridColor, off1, off2, strokeWidth = 1f)
            }

            // 2. Render Polygons (Painter's Algorithm: Z-sort back to front)
            if (renderMode == RenderMode3D.SHADED || renderMode == RenderMode3D.HYBRID) {
                val sortedFaces = mesh.faces.map { face ->
                    val v0 = transformedVertices[face.i0]
                    val v1 = transformedVertices[face.i1]
                    val v2 = transformedVertices[face.i2]
                    val avgZ = (v0.z + v1.z + v2.z) / 3f
                    val rotatedNormal = face.normal.rotateX(pitch).rotateY(currentYaw).normalize()
                    Triple(face, avgZ, rotatedNormal)
                }.sortedBy { it.second } // furthest first

                for ((face, _, norm) in sortedFaces) {
                    val p0 = projected[face.i0]
                    val p1 = projected[face.i1]
                    val p2 = projected[face.i2]

                    // Lambertian lighting
                    val dot = max(0f, norm.dot(lightDir))
                    val ambient = 0.25f
                    val lightIntensity = (ambient + 0.75f * dot).coerceIn(0.15f, 1.0f)

                    val faceColor = face.color.copy(
                        red = (face.color.red * lightIntensity).coerceIn(0f, 1f),
                        green = (face.color.green * lightIntensity).coerceIn(0f, 1f),
                        blue = (face.color.blue * lightIntensity).coerceIn(0f, 1f),
                        alpha = if (renderMode == RenderMode3D.HYBRID) 0.82f else 0.95f
                    )

                    val path = Path().apply {
                        moveTo(p0.x, p0.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }
                    drawPath(path, faceColor)
                }
            }

            // 3. Render Wireframe Edges
            if (renderMode == RenderMode3D.WIREFRAME || renderMode == RenderMode3D.HYBRID) {
                val wireColor = if (renderMode == RenderMode3D.HYBRID) CyanPrimary.copy(alpha = 0.6f) else CyanPrimary
                val wireWidth = if (renderMode == RenderMode3D.HYBRID) 1.5f else 2f

                for ((iA, iB) in mesh.edges) {
                    if (iA < projected.size && iB < projected.size) {
                        drawLine(
                            color = wireColor,
                            start = projected[iA],
                            end = projected[iB],
                            strokeWidth = wireWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 4. Render Vertices (Glowing points)
            if (renderMode == RenderMode3D.VERTICES || renderMode == RenderMode3D.WIREFRAME) {
                drawPoints(
                    points = projected,
                    pointMode = PointMode.Points,
                    color = VioletAccent,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Overlay status badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(10.dp).align(Alignment.TopStart)
        ) {
            Text(
                text = "${mesh.name} • ${mesh.vertexCount}v • ${mesh.polygonCount}p",
                style = MaterialTheme.typography.labelSmall,
                color = CyanPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(10.dp).align(Alignment.BottomEnd)
        ) {
            Text(
                text = "Drag to Orbit • 3D GLB Engine",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
