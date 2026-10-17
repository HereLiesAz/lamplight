package com.hereliesaz.lamplight

// Importing the module is what registers the tz data with @js-joda/core.
@JsModule("@js-joda/timezone")
internal external object JsJodaTimeZoneModule

internal actual fun loadTimeZoneData() {
    @Suppress("UNUSED_VARIABLE")
    val module = JsJodaTimeZoneModule
}
