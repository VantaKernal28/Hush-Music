package com.music.bitchord

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * The home-screen icon choices. Each is an `<activity-alias>` in the manifest
 * carrying the launcher intent filter; exactly one is enabled at a time.
 *
 * The glass icons are adaptive icons whose foreground already holds the finished
 * icon in its own shape (Apple squircle, Samsung squircle or Pixel circle), so
 * [previewRes] — that foreground — shows the variant as designed rather than
 * through the device's launcher mask. The first two keep yesterday's alias
 * names so a choice made before the update stays enabled.
 */
enum class AppIcon(val alias: String, val iconRes: Int, val labelRes: Int, val previewRes: Int? = null) {
    Default("LauncherDefault", R.mipmap.ic_launcher, R.string.app_icon_black_white),
    WhiteBlack("LauncherWhiteBlack", R.mipmap.ic_launcher_white_black, R.string.app_icon_white_black),
    Monochrome("LauncherMonochrome", R.mipmap.ic_monochrome, R.string.app_icon_monochrome),
    LiquidGlass("LauncherGlassLight", R.mipmap.ic_liquid_glass, R.string.app_icon_liquid_glass, R.mipmap.ic_liquid_glass_foreground),
    LiquidGlassDark("LauncherGlassDark", R.mipmap.ic_liquid_glass_dark, R.string.app_icon_liquid_glass_dark, R.mipmap.ic_liquid_glass_dark_foreground),
    LiquidGlassClear("LauncherGlassClear", R.mipmap.ic_liquid_glass_clear, R.string.app_icon_liquid_glass_clear, R.mipmap.ic_liquid_glass_clear_foreground),
    LiquidGlassClearDark("LauncherGlassClearDark", R.mipmap.ic_liquid_glass_clear_dark, R.string.app_icon_liquid_glass_clear_dark, R.mipmap.ic_liquid_glass_clear_dark_foreground),
    SamsungGlass("LauncherSamsungGlass", R.mipmap.ic_samsung_glass, R.string.app_icon_samsung_glass, R.mipmap.ic_samsung_glass_foreground),
    SamsungGlassDark("LauncherSamsungGlassDark", R.mipmap.ic_samsung_glass_dark, R.string.app_icon_samsung_glass_dark, R.mipmap.ic_samsung_glass_dark_foreground),
    SamsungGlassClear("LauncherSamsungGlassClear", R.mipmap.ic_samsung_glass_clear, R.string.app_icon_samsung_glass_clear, R.mipmap.ic_samsung_glass_clear_foreground),
    SamsungGlassClearDark("LauncherSamsungGlassClearDark", R.mipmap.ic_samsung_glass_clear_dark, R.string.app_icon_samsung_glass_clear_dark, R.mipmap.ic_samsung_glass_clear_dark_foreground),
    PixelGlass("LauncherPixelGlass", R.mipmap.ic_pixel_glass, R.string.app_icon_pixel_glass, R.mipmap.ic_pixel_glass_foreground),
    PixelGlassDark("LauncherPixelGlassDark", R.mipmap.ic_pixel_glass_dark, R.string.app_icon_pixel_glass_dark, R.mipmap.ic_pixel_glass_dark_foreground),
    PixelGlassClear("LauncherPixelGlassClear", R.mipmap.ic_pixel_glass_clear, R.string.app_icon_pixel_glass_clear, R.mipmap.ic_pixel_glass_clear_foreground),
    PixelGlassClearDark("LauncherPixelGlassClearDark", R.mipmap.ic_pixel_glass_clear_dark, R.string.app_icon_pixel_glass_clear_dark, R.mipmap.ic_pixel_glass_clear_dark_foreground),
    NeonSilver("LauncherNeonSilver", R.mipmap.ic_neon_silver, R.string.app_icon_neon_silver, R.mipmap.ic_neon_silver_foreground),
    NeonCyan("LauncherNeonCyan", R.mipmap.ic_neon_cyan, R.string.app_icon_neon_cyan, R.mipmap.ic_neon_cyan_foreground),
    NeonViolet("LauncherNeonViolet", R.mipmap.ic_neon_violet, R.string.app_icon_neon_violet, R.mipmap.ic_neon_violet_foreground),
    SamsungNeonSilver("LauncherSamsungNeonSilver", R.mipmap.ic_samsung_neon_silver, R.string.app_icon_samsung_neon_silver, R.mipmap.ic_samsung_neon_silver_foreground),
    SamsungNeonCyan("LauncherSamsungNeonCyan", R.mipmap.ic_samsung_neon_cyan, R.string.app_icon_samsung_neon_cyan, R.mipmap.ic_samsung_neon_cyan_foreground),
    SamsungNeonViolet("LauncherSamsungNeonViolet", R.mipmap.ic_samsung_neon_violet, R.string.app_icon_samsung_neon_violet, R.mipmap.ic_samsung_neon_violet_foreground),
    PixelNeonSilver("LauncherPixelNeonSilver", R.mipmap.ic_pixel_neon_silver, R.string.app_icon_pixel_neon_silver, R.mipmap.ic_pixel_neon_silver_foreground),
    PixelNeonCyan("LauncherPixelNeonCyan", R.mipmap.ic_pixel_neon_cyan, R.string.app_icon_pixel_neon_cyan, R.mipmap.ic_pixel_neon_cyan_foreground),
    PixelNeonViolet("LauncherPixelNeonViolet", R.mipmap.ic_pixel_neon_violet, R.string.app_icon_pixel_neon_violet, R.mipmap.ic_pixel_neon_violet_foreground);

    private fun component(context: Context) =
        ComponentName(context.packageName, "com.music.bitchord.$alias")

    companion object {
        fun current(context: Context): AppIcon {
            return runCatching {
                val pm = context.packageManager
                entries.firstOrNull {
                    runCatching {
                        pm.getComponentEnabledSetting(it.component(context)) ==
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    }.getOrDefault(false)
                }
            }.getOrNull() ?: Default
        }

        /** Disables other launcher aliases before enabling the target icon to ensure the launcher updates cleanly without creating duplicate icons. */
        fun apply(context: Context, icon: AppIcon) {
            runCatching {
                val pm = context.packageManager
                val targetComponent = icon.component(context)

                entries.filter { it != icon }.forEach {
                    runCatching {
                        pm.setComponentEnabledSetting(
                            it.component(context),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP,
                        )
                    }
                }

                pm.setComponentEnabledSetting(
                    targetComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
