package com.hereliesaz.lamplight.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import lamplight.shared.generated.resources.Res
import lamplight.shared.generated.resources.lamplight_mark
import org.jetbrains.compose.resources.painterResource

// docs/lamplight_transparent.png is a tall, narrow ink-wash lamppost illustration (885x3104
// source) -- see LamplightHome's own original comment. The lantern housing (where the light
// would come from) sits near the top of the pole, roughly this fraction of the way down; the
// glow is sized generously relative to it, on the theory that a soft, slightly-oversized glow
// reads fine even if this estimate is a little off, where a small tight one wouldn't -- worth
// a real visual check on-device/in-browser once this lands, since this sandbox can't render it.
private const val LampHousingFraction = 0.14f
private const val LampWatermarkAspectRatio = 885f / 3104f

/**
 * Which color the lamp's glow takes for a given relationship to a place -- or, on Explore,
 * a given combination of active filters, using the exact same booleans. Featured wins over
 * everything else, matching the same priority Explore's own sort already gives it (see
 * ExploreScreen's compareByDescending chain); Saved (a personal claim) outranks Been/Seen
 * (weaker, more passive signals). Falls back to the app's plain brand amber when nothing here
 * distinguishes this state from the app's own resting state.
 */
fun lampGlowColorFor(featured: Boolean, saved: Boolean, visited: Boolean, seen: Boolean): Color = when {
    featured -> GlowFeatured
    saved -> GlowSaved
    visited -> GlowNextTrip
    seen -> GlowSeen
    else -> Amber
}

/**
 * The lamp watermark, with a soft colored glow behind the lantern housing that signals which
 * area/state of the app is showing -- see [lampGlowColorFor] for Explore/Detail's color rules;
 * Discover passes [GlowDiscover] directly, a fixed "area" color rather than a per-place one.
 * A radial gradient rather than a real blur: Modifier.blur() isn't uniformly supported across
 * every Compose Multiplatform target this app ships (Android/wasmJs/js), while a gradient
 * brush is plain alpha blending, identical everywhere. Animated so a filter toggle or a Save
 * tap shifts the glow rather than snapping it, matching the app's own gaslamp mood.
 */
@Composable
fun LampWatermark(glowColor: Color, modifier: Modifier = Modifier) {
    val animatedColor by animateColorAsState(glowColor, animationSpec = tween(durationMillis = 500))
    BoxWithConstraints(modifier.aspectRatio(LampWatermarkAspectRatio)) {
        val glowDiameter = maxWidth * 1.6f
        Box(
            Modifier
                .size(glowDiameter)
                .offset(
                    x = (maxWidth - glowDiameter) / 2f,
                    y = maxHeight * LampHousingFraction - glowDiameter / 2f
                )
                .background(Brush.radialGradient(listOf(animatedColor.copy(alpha = 0.55f), animatedColor.copy(alpha = 0f))))
        )
        Image(
            painter = painterResource(Res.drawable.lamplight_mark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
