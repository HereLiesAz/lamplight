package com.hereliesaz.lamplight

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OpeningHoursTest {

    @Test
    fun `no periods means unknown, not closed`() {
        assertNull(isOpenNow(emptyList(), DayOfWeek.MONDAY, LocalTime(12, 0)))
    }

    @Test
    fun `open during a same-day period`() {
        val periods = listOf(OpeningPeriod(openDay = 1, openTime = "09:00", closeDay = 1, closeTime = "17:00"))

        assertEquals(true, isOpenNow(periods, DayOfWeek.MONDAY, LocalTime(12, 0)))
    }

    @Test
    fun `closed before opening and after closing on the same day`() {
        val periods = listOf(OpeningPeriod(openDay = 1, openTime = "09:00", closeDay = 1, closeTime = "17:00"))

        assertEquals(false, isOpenNow(periods, DayOfWeek.MONDAY, LocalTime(8, 0)))
        assertEquals(false, isOpenNow(periods, DayOfWeek.MONDAY, LocalTime(18, 0)))
    }

    @Test
    fun `closed on a day with no matching period`() {
        val periods = listOf(OpeningPeriod(openDay = 1, openTime = "09:00", closeDay = 1, closeTime = "17:00"))

        assertEquals(false, isOpenNow(periods, DayOfWeek.TUESDAY, LocalTime(12, 0)))
    }

    @Test
    fun `a period spanning past midnight is open on both sides of the boundary`() {
        // Friday 6pm to Saturday 2am.
        val periods = listOf(OpeningPeriod(openDay = 5, openTime = "18:00", closeDay = 6, closeTime = "02:00"))

        assertEquals(true, isOpenNow(periods, DayOfWeek.FRIDAY, LocalTime(23, 0)))
        assertEquals(true, isOpenNow(periods, DayOfWeek.SATURDAY, LocalTime(1, 0)))
        assertEquals(false, isOpenNow(periods, DayOfWeek.SATURDAY, LocalTime(3, 0)))
        assertEquals(false, isOpenNow(periods, DayOfWeek.FRIDAY, LocalTime(17, 0)))
    }

    @Test
    fun `the 24-7 sentinel period is always open, any day`() {
        val periods = listOf(OpeningPeriod(openDay = 0, openTime = "00:00", closeDay = null, closeTime = null))

        DayOfWeek.entries.forEach { day ->
            assertEquals(true, isOpenNow(periods, day, LocalTime(3, 30)))
        }
    }
}

class NewOrleansTimeTest {

    @Test
    fun `reads venue-local time regardless of device zone`() {
        // 2026-07-01 02:00 UTC is 21:00 on June 30 in New Orleans (CDT, UTC-5) -- a Pacific
        // device would show 19:00 and an Eastern one 22:00.
        val local = newOrleansLocalDateTime(Instant.parse("2026-07-01T02:00:00Z"))

        assertEquals(DayOfWeek.TUESDAY, local.dayOfWeek)
        assertEquals(LocalTime(21, 0), local.time)
    }

    @Test
    fun `uses standard time in winter`() {
        val local = newOrleansLocalDateTime(Instant.parse("2026-01-15T03:30:00Z"))

        assertEquals(DayOfWeek.WEDNESDAY, local.dayOfWeek)
        assertEquals(LocalTime(21, 30), local.time)
    }

    @Test
    fun `open and closed badge follows New Orleans time`() {
        val periods = listOf(OpeningPeriod(openDay = 2, openTime = "17:00", closeDay = 2, closeTime = "23:00"))
        val local = newOrleansLocalDateTime(Instant.parse("2026-07-01T02:00:00Z")) // Tue 21:00 CT

        assertEquals(true, isOpenNow(periods, local.dayOfWeek, local.time))
    }
}
