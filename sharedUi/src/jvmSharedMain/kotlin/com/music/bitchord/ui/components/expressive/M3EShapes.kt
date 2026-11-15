package com.music.bitchord.ui.components.expressive

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 12-Shape Material 3 Expressive (M3E) Shape System.
 *
 * Implements the flagship Material 3 Expressive shapes:
 * 1. Squircle (smooth continuous curvature)
 * 2. 12-Lobe Scallop (floral rosette)
 * 3. 4-Lobe Clover
 * 4. Stadium / Pill
 * 5. Architectural Arch
 * 6. 8-Lobe Wavy Octagon
 * 7. Rounded Diamond
 * 8. Rounded Hexagon
 * 9. Organic Starburst
 * 10. Sunny Radiance
 * 11. Asymmetric Organic Blob
 * 12. Pure Circle
 */
object M3EShapes {

    val Squircle: Shape = SquircleShape(24.dp)
    val Scallop12: Shape = ScallopedFlowerShape(lobes = 12, innerRadiusRatio = 0.85f)
    val Clover4: Shape = ScallopedFlowerShape(lobes = 4, innerRadiusRatio = 0.76f)
    val Pill: Shape = RoundedCornerShape(percent = 50)
    val Arch: Shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
    val WavyOctagon8: Shape = ScallopedFlowerShape(lobes = 8, innerRadiusRatio = 0.82f)
    val Diamond: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 32.dp)
    val Hexagon: Shape = RoundedPolygonShape(sides = 6)
    val Starburst: Shape = ScallopedFlowerShape(lobes = 6, innerRadiusRatio = 0.70f)
    val Sunny: Shape = ScallopedFlowerShape(lobes = 16, innerRadiusRatio = 0.88f)
    val OrganicBlob: Shape = RoundedCornerShape(topStart = 36.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 36.dp)
    val Circle: Shape = CircleShape

    val allShapes: List<Shape> = listOf(
        Squircle,
        Scallop12,
        Clover4,
        Pill,
        Arch,
        WavyOctagon8,
        Diamond,
        Hexagon,
        Starburst,
        Sunny,
        OrganicBlob,
        Circle,
    )

    fun get(index: Int): Shape = allShapes[abs(index) % allShapes.size]

    fun get(key: String): Shape = get(key.hashCode())
}

/**
 * Smooth rounded regular polygon shape for M3E (e.g. Hexagon).
 */
class RoundedPolygonShape(
    val sides: Int = 6,
    val cornerRadiusRatio: Float = 0.2f,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = minOf(centerX, centerY)
        if (radius <= 0f) return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))

        val angleStep = (2.0 * PI / sides).toFloat()
        val path = Path()

        val vertices = Array(sides) { i ->
            val angle = i * angleStep - (PI / 2.0).toFloat()
            Offset(
                x = centerX + radius * cos(angle),
                y = centerY + radius * sin(angle),
            )
        }

        path.moveTo((vertices[0].x + vertices[sides - 1].x) / 2f, (vertices[0].y + vertices[sides - 1].y) / 2f)

        for (i in 0 until sides) {
            val curr = vertices[i]
            val next = vertices[(i + 1) % sides]
            val midX = (curr.x + next.x) / 2f
            val midY = (curr.y + next.y) / 2f
            path.quadraticTo(curr.x, curr.y, midX, midY)
        }

        path.close()
        return Outline.Generic(path)
    }
}
