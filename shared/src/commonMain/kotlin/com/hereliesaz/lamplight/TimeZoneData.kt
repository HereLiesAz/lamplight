package com.hereliesaz.lamplight

/**
 * Makes sure the platform has an IANA time-zone database before a named zone is requested.
 * A no-op on Android; on the web targets it loads js-joda's tz data, which kotlinx-datetime
 * needs for [kotlinx.datetime.TimeZone.of] with region IDs.
 */
internal expect fun loadTimeZoneData()
