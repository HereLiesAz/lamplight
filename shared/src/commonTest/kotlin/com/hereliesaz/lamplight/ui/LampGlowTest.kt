package com.hereliesaz.lamplight.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LampGlowTest {

    @Test
    fun `no state selected falls back to the plain brand amber`() {
        assertEquals(Amber, lampGlowColorFor(featured = false, saved = false, visited = false, seen = false))
    }

    @Test
    fun `each state alone maps to its own color`() {
        assertEquals(GlowFeatured, lampGlowColorFor(featured = true, saved = false, visited = false, seen = false))
        assertEquals(GlowSaved, lampGlowColorFor(featured = false, saved = true, visited = false, seen = false))
        assertEquals(GlowNextTrip, lampGlowColorFor(featured = false, saved = false, visited = true, seen = false))
        assertEquals(GlowSeen, lampGlowColorFor(featured = false, saved = false, visited = false, seen = true))
    }

    @Test
    fun `featured wins over every other state, matching Explore's own sort priority`() {
        assertEquals(GlowFeatured, lampGlowColorFor(featured = true, saved = true, visited = true, seen = true))
    }

    @Test
    fun `saved wins over visited and seen when featured is off`() {
        assertEquals(GlowSaved, lampGlowColorFor(featured = false, saved = true, visited = true, seen = true))
    }

    @Test
    fun `visited wins over seen when featured and saved are off`() {
        assertEquals(GlowNextTrip, lampGlowColorFor(featured = false, saved = false, visited = true, seen = true))
    }
}
