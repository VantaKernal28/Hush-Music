package com.music.bitchord.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom minimalist vector iconography matching ICONS.jpg.
 *
 * Characteristics:
 * - Solid silhouette with rounded corners
 * - Negative space cuts (keyholes, slots, apertures)
 * - Balanced 24x24 viewport
 * - Zero wireframe clutter: bold, modern, cohesive
 */
object HushIcons {
    /** User / Profile avatar (ICONS.jpg Row 1, Col 2) */
    val User: ImageVector = ImageVector.Builder(
        name = "Hush.User",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Head
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            cubicTo(14.21f, 2f, 16f, 3.79f, 16f, 6f)
            cubicTo(16f, 8.21f, 14.21f, 10f, 12f, 10f)
            cubicTo(9.79f, 10f, 8f, 8.21f, 8f, 6f)
            cubicTo(8f, 3.79f, 9.79f, 2f, 12f, 2f)
            close()
        }
        // Body
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 12f)
            cubicTo(7.58f, 12f, 4f, 14.42f, 4f, 17.5f)
            lineTo(4f, 19.5f)
            cubicTo(4f, 20.88f, 5.12f, 22f, 6.5f, 22f)
            lineTo(17.5f, 22f)
            cubicTo(18.88f, 22f, 20f, 20.88f, 20f, 19.5f)
            lineTo(20f, 17.5f)
            cubicTo(20f, 14.42f, 16.42f, 12f, 12f, 12f)
            close()
        }
    }.build()

    /** Double Users (ICONS.jpg Row 1, Col 3) */
    val Users: ImageVector = ImageVector.Builder(
        name = "Hush.Users",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Front User Head
        path(fill = SolidColor(Color.White)) {
            moveTo(9f, 3f)
            cubicTo(10.93f, 3f, 12.5f, 4.57f, 12.5f, 6.5f)
            cubicTo(12.5f, 8.43f, 10.93f, 10f, 9f, 10f)
            cubicTo(7.07f, 10f, 5.5f, 8.43f, 5.5f, 6.5f)
            cubicTo(5.5f, 4.57f, 7.07f, 3f, 9f, 3f)
            close()
        }
        // Front User Body
        path(fill = SolidColor(Color.White)) {
            moveTo(9f, 12f)
            cubicTo(5.13f, 12f, 2f, 14.13f, 2f, 17f)
            lineTo(2f, 19.5f)
            cubicTo(2f, 20.88f, 3.12f, 22f, 4.5f, 22f)
            lineTo(13.5f, 22f)
            cubicTo(14.88f, 22f, 16f, 20.88f, 16f, 19.5f)
            lineTo(16f, 17f)
            cubicTo(16f, 14.13f, 12.87f, 12f, 9f, 12f)
            close()
        }
        // Back User Head & Shoulder
        path(fill = SolidColor(Color.White)) {
            moveTo(16.5f, 4.5f)
            cubicTo(18.16f, 4.5f, 19.5f, 5.84f, 19.5f, 7.5f)
            cubicTo(19.5f, 9.16f, 18.16f, 10.5f, 16.5f, 10.5f)
            cubicTo(15.7f, 10.5f, 14.97f, 10.18f, 14.43f, 9.67f)
            cubicTo(14.79f, 8.71f, 15f, 7.64f, 15f, 6.5f)
            cubicTo(15f, 5.76f, 14.91f, 5.04f, 14.74f, 4.37f)
            cubicTo(15.26f, 4.45f, 15.86f, 4.5f, 16.5f, 4.5f)
            close()
            moveTo(17.5f, 13f)
            cubicTo(19.98f, 13.5f, 22f, 15.2f, 22f, 17.5f)
            lineTo(22f, 19.5f)
            cubicTo(22f, 20.88f, 21f, 22f, 19.8f, 22f)
            lineTo(17.5f, 22f)
            lineTo(17.5f, 19.5f)
            cubicTo(17.5f, 16.8f, 15.6f, 14.5f, 13.2f, 13.3f)
            cubicTo(14.5f, 13f, 16f, 13f, 17.5f, 13f)
            close()
        }
    }.build()

    /** Headphones (ICONS.jpg Row 1, Col 5) */
    val Headphones: ImageVector = ImageVector.Builder(
        name = "Hush.Headphones",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Headband arch
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            cubicTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            lineTo(2f, 14f)
            cubicTo(2f, 15.1f, 2.9f, 16f, 4f, 16f)
            lineTo(5f, 16f)
            cubicTo(5.55f, 16f, 6f, 15.55f, 6f, 15f)
            lineTo(6f, 11f)
            cubicTo(6f, 7.69f, 8.69f, 5f, 12f, 5f)
            cubicTo(15.31f, 5f, 18f, 7.69f, 18f, 11f)
            lineTo(18f, 15f)
            cubicTo(18f, 15.55f, 18.45f, 16f, 19f, 16f)
            lineTo(20f, 16f)
            cubicTo(21.1f, 16f, 22f, 15.1f, 22f, 14f)
            lineTo(22f, 12f)
            cubicTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
        }
        // Left Ear Cup
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 11f)
            cubicTo(1.9f, 11f, 1f, 11.9f, 1f, 13f)
            lineTo(1f, 19f)
            cubicTo(1f, 20.66f, 2.34f, 22f, 4f, 22f)
            lineTo(5.5f, 22f)
            cubicTo(6.88f, 22f, 8f, 20.88f, 8f, 19.5f)
            lineTo(8f, 13.5f)
            cubicTo(8f, 12.12f, 6.88f, 11f, 5.5f, 11f)
            lineTo(3f, 11f)
            close()
        }
        // Right Ear Cup
        path(fill = SolidColor(Color.White)) {
            moveTo(21f, 11f)
            cubicTo(22.1f, 11f, 23f, 11.9f, 23f, 13f)
            lineTo(23f, 19f)
            cubicTo(23f, 20.66f, 21.66f, 22f, 20f, 22f)
            lineTo(18.5f, 22f)
            cubicTo(17.12f, 22f, 16f, 20.88f, 16f, 19.5f)
            lineTo(16f, 13.5f)
            cubicTo(16f, 12.12f, 17.12f, 11f, 18.5f, 11f)
            lineTo(21f, 11f)
            close()
        }
    }.build()

    /** Lock / Security (ICONS.jpg Row 2, Col 1) */
    val Lock: ImageVector = ImageVector.Builder(
        name = "Hush.Lock",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.EvenOdd,
        ) {
            // Shackle
            moveTo(12f, 2f)
            cubicTo(8.69f, 2f, 6f, 4.69f, 6f, 8f)
            lineTo(6f, 10f)
            lineTo(18f, 10f)
            lineTo(18f, 8f)
            cubicTo(18f, 4.69f, 15.31f, 2f, 12f, 2f)
            close()
            moveTo(9.5f, 8f)
            cubicTo(9.5f, 6.62f, 10.62f, 5.5f, 12f, 5.5f)
            cubicTo(13.38f, 5.5f, 14.5f, 6.62f, 14.5f, 8f)
            lineTo(14.5f, 10f)
            lineTo(9.5f, 10f)
            close()
            // Body
            moveTo(4.5f, 10f)
            cubicTo(3.12f, 10f, 2f, 11.12f, 2f, 12.5f)
            lineTo(2f, 19.5f)
            cubicTo(2f, 20.88f, 3.12f, 22f, 4.5f, 22f)
            lineTo(19.5f, 22f)
            cubicTo(20.88f, 22f, 22f, 20.88f, 22f, 19.5f)
            lineTo(22f, 12.5f)
            cubicTo(22f, 11.12f, 20.88f, 10f, 19.5f, 10f)
            close()
            // Keyhole negative space
            moveTo(12f, 13.5f)
            cubicTo(10.9f, 13.5f, 10f, 14.4f, 10f, 15.5f)
            cubicTo(10f, 16.2f, 10.36f, 16.82f, 10.9f, 17.18f)
            lineTo(10.5f, 19f)
            lineTo(13.5f, 19f)
            lineTo(13.1f, 17.18f)
            cubicTo(13.64f, 16.82f, 14f, 16.2f, 14f, 15.5f)
            cubicTo(14f, 14.4f, 13.1f, 13.5f, 12f, 13.5f)
            close()
        }
    }.build()

    /** Folder / Local Music (ICONS.jpg Row 5, Col 1) */
    val Folder: ImageVector = ImageVector.Builder(
        name = "Hush.Folder",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(4.5f, 4f)
            cubicTo(3.12f, 4f, 2f, 5.12f, 2f, 6.5f)
            lineTo(2f, 18.5f)
            cubicTo(2f, 19.88f, 3.12f, 21f, 4.5f, 21f)
            lineTo(19.5f, 21f)
            cubicTo(20.88f, 21f, 22f, 19.88f, 22f, 18.5f)
            lineTo(22f, 9.5f)
            cubicTo(22f, 8.12f, 20.88f, 7f, 19.5f, 7f)
            lineTo(12.5f, 7f)
            lineTo(10.5f, 4.5f)
            cubicTo(10f, 4.18f, 9.38f, 4f, 8.8f, 4f)
            close()
        }
    }.build()

    /** Archive / Downloads Box (ICONS.jpg Row 7, Col 2) */
    val DownloadsBox: ImageVector = ImageVector.Builder(
        name = "Hush.DownloadsBox",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Lid
        path(fill = SolidColor(Color.White)) {
            moveTo(3.5f, 4f)
            cubicTo(2.67f, 4f, 2f, 4.67f, 2f, 5.5f)
            lineTo(2f, 7.5f)
            cubicTo(2f, 8.33f, 2.67f, 9f, 3.5f, 9f)
            lineTo(20.5f, 9f)
            cubicTo(21.33f, 9f, 22f, 8.33f, 22f, 7.5f)
            lineTo(22f, 5.5f)
            cubicTo(22f, 4.67f, 21.33f, 4f, 20.5f, 4f)
            close()
        }
        // Container with handle cutout
        path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(3f, 10f)
            lineTo(3f, 18.5f)
            cubicTo(3f, 19.88f, 4.12f, 21f, 5.5f, 21f)
            lineTo(18.5f, 21f)
            cubicTo(19.88f, 21f, 21f, 19.88f, 21f, 18.5f)
            lineTo(21f, 10f)
            close()
            // Handle slot
            moveTo(9.5f, 13f)
            cubicTo(9.5f, 12.45f, 9.95f, 12f, 10.5f, 12f)
            lineTo(13.5f, 12f)
            cubicTo(14.05f, 12f, 14.5f, 12.45f, 14.5f, 13f)
            lineTo(14.5f, 14.5f)
            cubicTo(14.5f, 15.05f, 14.05f, 15.5f, 13.5f, 15.5f)
            lineTo(10.5f, 15.5f)
            cubicTo(9.95f, 15.5f, 9.5f, 15.05f, 9.5f, 14.5f)
            close()
        }
    }.build()

    /** Paintbrush / Appearance (ICONS.jpg Row 3, Col 5) */
    val Paintbrush: ImageVector = ImageVector.Builder(
        name = "Hush.Paintbrush",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(21.41f, 4.59f)
            cubicTo(22.2f, 3.8f, 22.2f, 2.54f, 21.41f, 1.76f)
            cubicTo(20.63f, 0.98f, 19.37f, 0.98f, 18.59f, 1.76f)
            lineTo(10.83f, 9.51f)
            lineTo(8.5f, 11.84f)
            lineTo(12.16f, 15.5f)
            lineTo(14.49f, 13.17f)
            lineTo(21.41f, 4.59f)
            close()
            // Brush belly & tip
            moveTo(9.74f, 16.32f)
            lineTo(7.68f, 14.26f)
            cubicTo(5.5f, 15.5f, 3f, 18f, 2f, 22f)
            cubicTo(6f, 21f, 8.5f, 18.5f, 9.74f, 16.32f)
            close()
        }
    }.build()

    /** Settings Gear (ICONS.jpg Row 5, Col 2) */
    val Gear: ImageVector = ImageVector.Builder(
        name = "Hush.Gear",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(19.43f, 12.98f)
            cubicTo(19.47f, 12.66f, 19.5f, 12.34f, 19.5f, 12f)
            cubicTo(19.5f, 11.66f, 19.47f, 11.34f, 19.43f, 11.02f)
            lineTo(21.54f, 9.37f)
            cubicTo(21.73f, 9.22f, 21.78f, 8.95f, 21.66f, 8.73f)
            lineTo(19.66f, 5.27f)
            cubicTo(19.54f, 5.05f, 19.27f, 4.97f, 19.05f, 5.05f)
            lineTo(16.56f, 6.05f)
            cubicTo(16.04f, 5.65f, 15.48f, 5.32f, 14.87f, 5.07f)
            lineTo(14.49f, 2.42f)
            cubicTo(14.46f, 2.18f, 14.25f, 2f, 14f, 2f)
            lineTo(10f, 2f)
            cubicTo(9.75f, 2f, 9.54f, 2.18f, 9.51f, 2.42f)
            lineTo(9.13f, 5.07f)
            cubicTo(8.52f, 5.32f, 7.96f, 5.66f, 7.44f, 6.05f)
            lineTo(4.95f, 5.05f)
            cubicTo(4.72f, 4.96f, 4.46f, 5.05f, 4.34f, 5.27f)
            lineTo(2.34f, 8.73f)
            cubicTo(2.21f, 8.95f, 2.27f, 9.22f, 2.46f, 9.37f)
            lineTo(4.57f, 11.02f)
            cubicTo(4.53f, 11.34f, 4.5f, 11.67f, 4.5f, 12f)
            cubicTo(4.5f, 12.33f, 4.53f, 12.66f, 4.57f, 12.98f)
            lineTo(2.46f, 14.63f)
            cubicTo(2.26f, 14.78f, 2.21f, 15.05f, 2.34f, 15.27f)
            lineTo(4.34f, 18.73f)
            cubicTo(4.46f, 18.95f, 4.73f, 19.03f, 4.95f, 18.95f)
            lineTo(7.44f, 17.95f)
            cubicTo(7.96f, 18.35f, 8.52f, 18.68f, 9.13f, 18.93f)
            lineTo(9.51f, 21.58f)
            cubicTo(9.54f, 21.82f, 9.75f, 22f, 10f, 22f)
            lineTo(14f, 22f)
            cubicTo(14.25f, 22f, 14.46f, 21.82f, 14.49f, 21.58f)
            lineTo(14.87f, 18.93f)
            cubicTo(15.48f, 18.68f, 16.04f, 18.34f, 16.56f, 17.95f)
            lineTo(19.05f, 18.95f)
            cubicTo(19.28f, 19.04f, 19.54f, 18.95f, 19.66f, 18.73f)
            lineTo(21.66f, 15.27f)
            cubicTo(21.78f, 15.05f, 21.73f, 14.78f, 21.54f, 14.63f)
            lineTo(19.43f, 12.98f)
            close()
            // Center circular cutout
            moveTo(12f, 8.5f)
            cubicTo(10.07f, 8.5f, 8.5f, 10.07f, 8.5f, 12f)
            cubicTo(8.5f, 13.93f, 10.07f, 15.5f, 12f, 15.5f)
            cubicTo(13.93f, 15.5f, 15.5f, 13.93f, 15.5f, 12f)
            cubicTo(15.5f, 10.07f, 13.93f, 8.5f, 12f, 8.5f)
            close()
        }
    }.build()

    /** Bell / Notifications (ICONS.jpg Row 7, Col 4) */
    val Bell: ImageVector = ImageVector.Builder(
        name = "Hush.Bell",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            cubicTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
            lineTo(9f, 5.18f)
            cubicTo(6.16f, 6.36f, 4.5f, 9.25f, 4.5f, 12.5f)
            lineTo(4.5f, 16.5f)
            lineTo(3.29f, 17.71f)
            cubicTo(2.66f, 18.34f, 3.1f, 19.5f, 4f, 19.5f)
            lineTo(20f, 19.5f)
            cubicTo(20.9f, 19.5f, 21.34f, 18.34f, 20.71f, 17.71f)
            lineTo(19.5f, 16.5f)
            lineTo(19.5f, 12.5f)
            cubicTo(19.5f, 9.25f, 17.84f, 6.36f, 15f, 5.18f)
            lineTo(15f, 5f)
            cubicTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
            close()
            // Clapper
            moveTo(12f, 22.5f)
            cubicTo(13.38f, 22.5f, 14.5f, 21.38f, 14.5f, 20f)
            lineTo(9.5f, 20f)
            cubicTo(9.5f, 21.38f, 10.62f, 22.5f, 12f, 22.5f)
            close()
        }
    }.build()

    /** Grid / Dashboard (ICONS.jpg Row 1, Col 4) */
    val Grid: ImageVector = ImageVector.Builder(
        name = "Hush.Grid",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            // Top-left block
            moveTo(5.5f, 3f)
            cubicTo(4.12f, 3f, 3f, 4.12f, 3f, 5.5f)
            lineTo(3f, 8.5f)
            cubicTo(3f, 9.88f, 4.12f, 11f, 5.5f, 11f)
            lineTo(8.5f, 11f)
            cubicTo(9.88f, 11f, 11f, 9.88f, 11f, 8.5f)
            lineTo(11f, 5.5f)
            cubicTo(11f, 4.12f, 9.88f, 3f, 8.5f, 3f)
            close()
            // Top-right block
            moveTo(15.5f, 3f)
            cubicTo(14.12f, 3f, 13f, 4.12f, 13f, 5.5f)
            lineTo(13f, 8.5f)
            cubicTo(13f, 9.88f, 14.12f, 11f, 15.5f, 11f)
            lineTo(18.5f, 11f)
            cubicTo(19.88f, 11f, 21f, 9.88f, 21f, 8.5f)
            lineTo(21f, 5.5f)
            cubicTo(21f, 4.12f, 19.88f, 3f, 18.5f, 3f)
            close()
            // Bottom-left block
            moveTo(5.5f, 13f)
            cubicTo(4.12f, 13f, 3f, 14.12f, 3f, 15.5f)
            lineTo(3f, 18.5f)
            cubicTo(3f, 19.88f, 4.12f, 21f, 5.5f, 21f)
            lineTo(8.5f, 21f)
            cubicTo(9.88f, 21f, 11f, 19.88f, 11f, 18.5f)
            lineTo(11f, 15.5f)
            cubicTo(11f, 14.12f, 9.88f, 13f, 8.5f, 13f)
            close()
            // Bottom-right block
            moveTo(15.5f, 13f)
            cubicTo(14.12f, 13f, 13f, 14.12f, 13f, 15.5f)
            lineTo(13f, 18.5f)
            cubicTo(13f, 19.88f, 14.12f, 21f, 15.5f, 21f)
            lineTo(18.5f, 21f)
            cubicTo(19.88f, 21f, 21f, 19.88f, 21f, 18.5f)
            lineTo(21f, 15.5f)
            cubicTo(21f, 14.12f, 19.88f, 13f, 18.5f, 13f)
            close()
        }
    }.build()

    /** Display / Monitor (ICONS.jpg Row 2, Col 3) */
    val Display: ImageVector = ImageVector.Builder(
        name = "Hush.Display",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(4.5f, 4f)
            cubicTo(3.12f, 4f, 2f, 5.12f, 2f, 6.5f)
            lineTo(2f, 14.5f)
            cubicTo(2f, 15.88f, 3.12f, 17f, 4.5f, 17f)
            lineTo(10.5f, 17f)
            lineTo(10f, 19.5f)
            lineTo(8f, 19.5f)
            cubicTo(7.45f, 19.5f, 7f, 19.95f, 7f, 20.5f)
            cubicTo(7f, 21.05f, 7.45f, 21.5f, 8f, 21.5f)
            lineTo(16f, 21.5f)
            cubicTo(16.55f, 21.5f, 17f, 21.05f, 17f, 20.5f)
            cubicTo(17f, 19.95f, 16.55f, 19.5f, 16f, 19.5f)
            lineTo(14f, 19.5f)
            lineTo(13.5f, 17f)
            lineTo(19.5f, 17f)
            cubicTo(20.88f, 17f, 22f, 15.88f, 22f, 14.5f)
            lineTo(22f, 6.5f)
            cubicTo(22f, 5.12f, 20.88f, 4f, 19.5f, 4f)
            close()
        }
    }.build()

    /** Heart (ICONS.jpg Row 5, Col 5) */
    val Heart: ImageVector = ImageVector.Builder(
        name = "Hush.Heart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 21.35f)
            lineTo(10.55f, 20.03f)
            cubicTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
            cubicTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
            cubicTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
            cubicTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
            cubicTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
            cubicTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
            lineTo(12f, 21.35f)
            close()
        }
    }.build()
}
