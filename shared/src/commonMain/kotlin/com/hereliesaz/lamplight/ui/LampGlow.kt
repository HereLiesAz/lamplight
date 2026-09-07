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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import lamplight.shared.generated.resources.Res
import lamplight.shared.generated.resources.lamplight_mark
import org.jetbrains.compose.resources.painterResource

// docs/lamplight_transparent.png is a tall, narrow ink-wash lamppost illustration (885x3104
// source) -- see LamplightHome's own original comment. These two fractions were measured
// against the real PNG's alpha channel (its ink is pure black; the shape lives entirely in
// alpha), not guessed: for y in the lantern-housing band, the alpha-weighted ink centroid
// sits at roughly (30%, 14%) of the watermark's own (width, height), with the housing's ink
// spanning roughly x in [1%, 71%]. The glow is sized generously relative to that, on the
// theory that a soft, slightly-oversized glow reads fine even if this estimate is a little
// off, where a small tight one wouldn't -- worth a real visual check on-device/in-browser
// once this lands, since this sandbox can't render Compose UI, only inspect the source pixels.
private const val LampHousingHorizontalFraction = 0.30f
private const val LampHousingVerticalFraction = 0.14f
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
 * `clipToBounds()` on the outer box matters here, not just tidiness: the glow box is
 * deliberately oversized relative to the watermark (see the fractions above), and Compose
 * doesn't clip a child's drawing to its parent's bounds on its own -- without this, the glow
 * paints straight through into whatever sits behind/around the watermark in the caller
 * (Explore's header and search field on Home, for one), not just behind the lantern.
 */
@Composable
fun LampWatermark(glowColor: Color, modifier: Modifier = Modifier) {
    val animatedColor by animateColorAsState(glowColor, animationSpec = tween(durationMillis = 500))
    BoxWithConstraints(modifier.aspectRatio(LampWatermarkAspectRatio).clipToBounds()) {
        val glowDiameter = maxWidth * 1.6f
        Box(
            Modifier
                .size(glowDiameter)
                .offset(
                    x = maxWidth * LampHousingHorizontalFraction - glowDiameter / 2f,
                    y = maxHeight * LampHousingVerticalFraction - glowDiameter / 2f
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
