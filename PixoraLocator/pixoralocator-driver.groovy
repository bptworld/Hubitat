import groovy.json.JsonSlurper

metadata {
    definition(name: "Pixora Locator", namespace: "pixorahq", author: "PixoraHQ") {
        capability "Sensor"
        capability "PresenceSensor"
        capability "Battery"
        capability "Refresh"

        attribute "latitude", "string"
        attribute "longitude", "string"
        attribute "accuracy", "number"
        attribute "place", "string"
        attribute "lastLocationAt", "string"
        attribute "deliveryStatus", "string"

        command "updateLocation", [[name: "Location payload", type: "STRING", description: "Pixora Locator JSON payload"]]
        command "clearLocation"
    }
}

preferences {
    input name: "staleAfterMinutes", type: "number", title: "Mark delivery stale after this many minutes", defaultValue: 45, range: "15..1440"
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "presence", value: device.currentValue("presence") ?: "not present")
    sendEvent(name: "deliveryStatus", value: device.currentValue("deliveryStatus") ?: "waiting")
    unschedule()
    runEvery15Minutes("refresh")
}

def updateLocation(String payload) {
    Map locationData
    try {
        locationData = new JsonSlurper().parseText(payload ?: "{}") as Map
    } catch (Exception ignored) {
        sendEvent(name: "deliveryStatus", value: "invalid payload")
        return
    }
    if (!(locationData.latitude instanceof Number) || !(locationData.longitude instanceof Number)) {
        sendEvent(name: "deliveryStatus", value: "invalid coordinates")
        return
    }
    String capturedAt = (locationData.capturedAt ?: new Date().format("yyyy-MM-dd'T'HH:mm:ssXXX", TimeZone.getTimeZone("UTC"))).toString()
    String displayedAt = capturedAt
    try {
        long capturedMillis = java.time.OffsetDateTime.parse(capturedAt).toInstant().toEpochMilli()
        TimeZone hubTimeZone = location?.timeZone ?: TimeZone.getDefault()
        displayedAt = new Date(capturedMillis).format("M-d-yyyy h:mm a", hubTimeZone).toLowerCase()
    } catch (Exception ignored) {
        // Keep the original timestamp if an older sender provides an unexpected format.
    }
    sendEvent(name: "latitude", value: locationData.latitude.toString())
    sendEvent(name: "longitude", value: locationData.longitude.toString())
    BigDecimal accuracy = locationData.accuracy instanceof Number ? new BigDecimal(locationData.accuracy.toString()) : BigDecimal.ZERO
    if (accuracy < BigDecimal.ZERO) accuracy = BigDecimal.ZERO
    sendEvent(name: "accuracy", value: accuracy, unit: "m")
    sendEvent(name: "place", value: (locationData.place ?: "not_home").toString())
    sendEvent(name: "lastLocationAt", value: displayedAt)
    sendEvent(name: "presence", value: locationData.presence == "present" ? "present" : "not present")
    if (locationData.battery instanceof Number) {
        int battery = (locationData.battery as Number).intValue()
        battery = Math.max(0, Math.min(100, battery))
        sendEvent(name: "battery", value: battery, unit: "%")
    }
    sendEvent(name: "deliveryStatus", value: "current")
    state.lastDelivery = now()
}

def refresh() {
    long staleMinutes = settings.staleAfterMinutes instanceof Number ? (settings.staleAfterMinutes as Number).longValue() : 45L
    if (staleMinutes < 15L) staleMinutes = 15L
    long maximumAge = staleMinutes * 60_000L
    if (state.lastDelivery && now() - (state.lastDelivery as Long) > maximumAge) {
        sendEvent(name: "deliveryStatus", value: "stale")
    }
}

def clearLocation() {
    sendEvent(name: "latitude", value: "")
    sendEvent(name: "longitude", value: "")
    sendEvent(name: "accuracy", value: 0, unit: "m")
    sendEvent(name: "place", value: "unavailable")
    sendEvent(name: "lastLocationAt", value: "")
    sendEvent(name: "presence", value: "not present")
    sendEvent(name: "deliveryStatus", value: "disabled")
    state.remove("lastDelivery")
}
