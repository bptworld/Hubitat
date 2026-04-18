metadata {
    definition(
        name: "HubVoice Satellite LD2410",
        namespace: "bptworld",
        author: "Bryan Turcotte"
    ) {
        capability "PresenceSensor"   // presence: "present" / "not present"  →  has_target
        capability "MotionSensor"     // motion: "active" / "inactive"         →  has_moving_target
        capability "Refresh"
        capability "Sensor"

        attribute "stillTarget",      "string"   // "active" / "inactive"  →  has_still_target
        attribute "movingDistance",   "number"   // cm
        attribute "stillDistance",    "number"   // cm
        attribute "movingEnergy",     "number"   // %
        attribute "stillEnergy",      "number"   // %
        attribute "movingGate",       "number"   // 2–8
        attribute "stillGate",        "number"   // 2–8
        attribute "radarTimeout",     "number"   // seconds
        attribute "engineeringMode",  "string"   // "on" / "off"
        attribute "bluetooth",        "string"   // "on" / "off"
        attribute "lastSync",             "string"
        attribute "lastPresenceChange",   "string"
        attribute "syncStatus",           "string"

        command "setMovingGate",   [[name: "Gate*",    type: "NUMBER", description: "2–8"]]
        command "setStillGate",    [[name: "Gate*",    type: "NUMBER", description: "2–8"]]
        command "setRadarTimeout", [[name: "Seconds*", type: "NUMBER", description: "0–65535"]]
        command "enableEngineering"
        command "disableEngineering"
        command "enableBluetooth"
        command "disableBluetooth"
        command "factoryReset"
        command "restartRadar"
        command "querySensors"
        command "syncNow"
        command "clearOldAttributes"
    }

    preferences {
        input name: "esphomeBaseUrl",        type: "text",   title: "ESPHome Web Server URL",
              description: "e.g. http://192.168.4.86:8080",  required: true
        input name: "requestTimeoutSeconds", type: "number", title: "HTTP timeout (seconds)",
              defaultValue: 10, required: false
        input name: "enableAutoSync",        type: "bool",   title: "Auto sync sensor values",
              defaultValue: true, required: false
        input name: "syncIntervalSeconds",   type: "enum",   title: "Sync interval",
              options: ["15":"Every 15 seconds", "30":"Every 30 seconds",
                        "60":"Every 1 minute", "300":"Every 5 minutes"],
              defaultValue: "30", required: false
        input name: "enableDebugLogging",    type: "bool",   title: "Enable debug logging",
              defaultValue: true, required: false
    }
}

// ============================================================================
// Lifecycle
// ============================================================================

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def initialize() {
    if (device.currentValue("presence")        == null) sendEvent(name: "presence",        value: "not present", isStateChange: true)
    if (device.currentValue("motion")          == null) sendEvent(name: "motion",          value: "inactive",    isStateChange: true)
    if (device.currentValue("stillTarget")     == null) sendEvent(name: "stillTarget",     value: "inactive",    isStateChange: true)
    if (device.currentValue("engineeringMode") == null) sendEvent(name: "engineeringMode", value: "off",         isStateChange: true)
    if (device.currentValue("bluetooth")       == null) sendEvent(name: "bluetooth",       value: "off",         isStateChange: true)
    if (device.currentValue("syncStatus")         == null) sendEvent(name: "syncStatus",         value: "idle", isStateChange: true)
    if (device.currentValue("lastPresenceChange") == null) sendEvent(name: "lastPresenceChange", value: "unknown", isStateChange: true)
    scheduleSync()
    refreshRadarState()
}

def parse(String description) { return null }

// ============================================================================
// Read commands
// ============================================================================

def refresh()  { refreshRadarState() }
def syncNow()  { refreshRadarState() }

def querySensors() {
    espPressButton("LD2410 Update Sensors")
    debugLog("LD2410 sensor query triggered")
    runIn(2, "refreshRadarState")
}

// ============================================================================
// Write commands — numbers
// ============================================================================

def setMovingGate(level) {
    Integer gate = clampToInt(level, 2, 8, 6)
    if (espSetNumber("LD2410 Max Moving Distance Gate", gate)) {
        sendEvent(name: "movingGate", value: gate, isStateChange: true)
        debugLog("Moving gate → ${gate}")
        runIn(1, "refreshRadarState")
    }
}

def setStillGate(level) {
    Integer gate = clampToInt(level, 2, 8, 6)
    if (espSetNumber("LD2410 Max Still Distance Gate", gate)) {
        sendEvent(name: "stillGate", value: gate, isStateChange: true)
        debugLog("Still gate → ${gate}")
        runIn(1, "refreshRadarState")
    }
}

def setRadarTimeout(seconds) {
    Integer t = clampToInt(seconds, 0, 65535, 15)
    if (espSetNumber("LD2410 Timeout", t)) {
        sendEvent(name: "radarTimeout", value: t, isStateChange: true)
        debugLog("Radar timeout → ${t}s")
        runIn(1, "refreshRadarState")
    }
}

// ============================================================================
// Write commands — switches
// ============================================================================

def enableEngineering() {
    if (espSetSwitch("LD2410 Engineering Mode", true)) {
        sendEvent(name: "engineeringMode", value: "on", isStateChange: true)
        debugLog("Engineering mode on")
    }
}

def disableEngineering() {
    if (espSetSwitch("LD2410 Engineering Mode", false)) {
        sendEvent(name: "engineeringMode", value: "off", isStateChange: true)
        debugLog("Engineering mode off")
    }
}

def enableBluetooth() {
    if (espSetSwitch("LD2410 Bluetooth", true)) {
        sendEvent(name: "bluetooth", value: "on", isStateChange: true)
        debugLog("LD2410 Bluetooth on")
    }
}

def disableBluetooth() {
    if (espSetSwitch("LD2410 Bluetooth", false)) {
        sendEvent(name: "bluetooth", value: "off", isStateChange: true)
        debugLog("LD2410 Bluetooth off")
    }
}

// ============================================================================
// Write commands — buttons
// ============================================================================

def factoryReset() {
    espPressButton("LD2410 Factory Reset")
    debugLog("LD2410 factory reset triggered")
}

def restartRadar() {
    espPressButton("LD2410 Restart")
    debugLog("LD2410 restart triggered")
}

def clearMotion() {
    sendEvent(name: "motion", value: "inactive", isStateChange: true)
    debugLog("Motion cooldown expired — inactive")
}

def clearOldAttributes() {
    // Remove state attributes left behind by HubVoice_Satellite_TTS.groovy
    // when this driver was swapped onto a device that previously ran that driver.
    ["targetSatellite", "lastMessage", "lastStatus",
     "whisperMode", "volume", "mute",
     "wakeSoundVolume", "wakeSoundMute",
     "bass", "treble",
     "switch", "level"].each { String attr ->
        if (device.currentState(attr) != null) {
            device.deleteCurrentState(attr)
            log.info "HubVoice LD2410: removed stale attribute '${attr}'"
        }
    }
    log.info "HubVoice LD2410: clearOldAttributes complete"
}

// ============================================================================
// State polling
// ============================================================================

private void refreshRadarState() {
    String baseUrl = sanitizeBaseUrl(settings?.esphomeBaseUrl)
    if (!baseUrl) {
        sendEvent(name: "syncStatus", value: "missing_url", isStateChange: true)
        log.warn "HubVoice LD2410: esphomeBaseUrl is not configured"
        return
    }

    sendEvent(name: "syncStatus", value: "syncing", isStateChange: true)

    // --- Binary sensors ---
    Map presenceResult = espGet(baseUrl, "binary_sensor", "Presence")
    Map movingResult   = espGet(baseUrl, "binary_sensor", "Moving Target")
    Map stillResult    = espGet(baseUrl, "binary_sensor", "Still Target")

    if (!presenceResult?.ok) {
        sendEvent(name: "syncStatus", value: "unreachable", isStateChange: true)
        log.warn "HubVoice LD2410: could not reach ${baseUrl}/binary_sensor/Presence"
        return
    }

    String newPresence = asBool(presenceResult.value) ? "present" : "not present"
    if (device.currentValue("presence") != newPresence) {
        sendEvent(name: "lastPresenceChange", value: new Date().format("yyyy-MM-dd HH:mm:ss", location?.timeZone ?: TimeZone.getTimeZone("UTC")), isStateChange: true)
    }
    sendEvent(name: "presence",    value: newPresence,                                              isStateChange: true)
    sendEvent(name: "stillTarget", value: asBool(stillResult?.value) ? "active" : "inactive",       isStateChange: true)

    // Moving target with 5s cooldown — avoids spamming active/inactive on brief gaps.
    if (asBool(movingResult?.value)) {
        unschedule("clearMotion")
        sendEvent(name: "motion", value: "active", isStateChange: true)
    } else if (device.currentValue("motion") == "active") {
        runIn(5, "clearMotion")
    }

    // --- Distance / energy sensors ---
    Map movDistResult  = espGet(baseUrl, "sensor", "LD2410 Moving Distance")
    Map stDistResult   = espGet(baseUrl, "sensor", "LD2410 Still Distance")
    Map movEngResult   = espGet(baseUrl, "sensor", "LD2410 Moving Energy")
    Map stEngResult    = espGet(baseUrl, "sensor", "LD2410 Still Energy")

    if (movDistResult?.ok)  sendEvent(name: "movingDistance", value: safeDouble(movDistResult.value,  0.0), unit: "cm", isStateChange: true)
    if (stDistResult?.ok)   sendEvent(name: "stillDistance",  value: safeDouble(stDistResult.value,   0.0), unit: "cm", isStateChange: true)
    if (movEngResult?.ok)   sendEvent(name: "movingEnergy",   value: safeDouble(movEngResult.value,   0.0), unit: "%",  isStateChange: true)
    if (stEngResult?.ok)    sendEvent(name: "stillEnergy",    value: safeDouble(stEngResult.value,    0.0), unit: "%",  isStateChange: true)

    // --- Number entities (config) ---
    Map movGateResult  = espGet(baseUrl, "number", "LD2410 Max Moving Distance Gate")
    Map stGateResult   = espGet(baseUrl, "number", "LD2410 Max Still Distance Gate")
    Map timeoutResult  = espGet(baseUrl, "number", "LD2410 Timeout")

    if (movGateResult?.ok)  sendEvent(name: "movingGate",   value: safeInt(movGateResult.value,  6),  isStateChange: true)
    if (stGateResult?.ok)   sendEvent(name: "stillGate",    value: safeInt(stGateResult.value,   6),  isStateChange: true)
    if (timeoutResult?.ok)  sendEvent(name: "radarTimeout", value: safeInt(timeoutResult.value,  15), isStateChange: true)

    // --- Switch entities ---
    Map engResult = espGet(baseUrl, "switch", "LD2410 Engineering Mode")
    Map btResult  = espGet(baseUrl, "switch", "LD2410 Bluetooth")

    if (engResult?.ok) sendEvent(name: "engineeringMode", value: asBool(engResult.value) ? "on" : "off", isStateChange: true)
    if (btResult?.ok)  sendEvent(name: "bluetooth",       value: asBool(btResult.value)  ? "on" : "off", isStateChange: true)

    sendEvent(name: "lastSync",   value: new Date().format("yyyy-MM-dd HH:mm:ss", location?.timeZone ?: TimeZone.getTimeZone("UTC")), isStateChange: true)
    sendEvent(name: "syncStatus", value: "ok", isStateChange: true)
    debugLog("Radar state refreshed — presence=${presenceResult.value}, moving=${movingResult?.value}, still=${stillResult?.value}")
}

private void scheduleSync() {
    if (settings?.enableAutoSync == false) {
        sendEvent(name: "syncStatus", value: "auto_sync_disabled", isStateChange: true)
        return
    }
    Integer seconds = safeInt(settings?.syncIntervalSeconds, 30)
    if      (seconds <= 15)  schedule("0/15 * * * * ? *", refreshRadarState)
    else if (seconds <= 30)  schedule("0/30 * * * * ? *", refreshRadarState)
    else if (seconds <= 60)  runEvery1Minute("refreshRadarState")
    else                     runEvery5Minutes("refreshRadarState")
}

// ============================================================================
// ESPHome REST helpers
// ============================================================================

private Map espGet(String baseUrl, String domain, String objectId) {
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    String encodedId = objectId.replaceAll(" ", "%20")
    try {
        Map output = [ok: false]
        httpGet([uri: "${baseUrl}/${domain}/${encodedId}", timeout: timeout]) { resp ->
            Integer code = resp?.status as Integer
            if (code >= 200 && code < 300) {
                Map data = (resp?.data instanceof Map) ? (Map) resp.data : [:]
                output = data + [ok: true]
            }
        }
        debugLog("GET ${domain}/${objectId} → ${output}")
        return output
    } catch (e) {
        debugLog("GET ${domain}/${objectId} failed: ${e.message}")
        return [ok: false, error: e?.toString()]
    }
}

private boolean espSetNumber(String objectId, Number value) {
    String baseUrl = sanitizeBaseUrl(settings?.esphomeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    String encodedId = objectId.replaceAll(" ", "%20")
    try {
        boolean ok = false
        httpPost([
            uri:                "${baseUrl}/number/${encodedId}/set",
            requestContentType: "application/x-www-form-urlencoded",
            body:               "value=${value}",
            timeout:            timeout
        ]) { resp ->
            ok = ((resp?.status as Integer) ?: 500) < 300
        }
        debugLog("SET number/${objectId}=${value} → ok=${ok}")
        return ok
    } catch (e) {
        log.warn "HubVoice LD2410: espSetNumber ${objectId} failed: ${e.message}"
        return false
    }
}

private boolean espSetSwitch(String objectId, boolean on) {
    String baseUrl = sanitizeBaseUrl(settings?.esphomeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    String encodedId = objectId.replaceAll(" ", "%20")
    String action = on ? "turn_on" : "turn_off"
    try {
        boolean ok = false
        httpPost([
            uri:                "${baseUrl}/switch/${encodedId}/${action}",
            requestContentType: "application/x-www-form-urlencoded",
            body:               "",
            timeout:            timeout
        ]) { resp ->
            ok = ((resp?.status as Integer) ?: 500) < 300
        }
        debugLog("SET switch/${objectId}/${action} → ok=${ok}")
        return ok
    } catch (e) {
        log.warn "HubVoice LD2410: espSetSwitch ${objectId} failed: ${e.message}"
        return false
    }
}

private void espPressButton(String objectId) {
    String baseUrl = sanitizeBaseUrl(settings?.esphomeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    String encodedId = objectId.replaceAll(" ", "%20")
    try {
        httpPost([
            uri:                "${baseUrl}/button/${encodedId}/press",
            requestContentType: "application/x-www-form-urlencoded",
            body:               "",
            timeout:            timeout
        ]) { resp ->
            debugLog("PRESS button/${objectId} → status=${resp?.status}")
        }
    } catch (e) {
        log.warn "HubVoice LD2410: espPressButton ${objectId} failed: ${e.message}"
    }
}

// ============================================================================
// Utilities  (same conventions as HubVoice_Satellite_TTS.groovy)
// ============================================================================

private String sanitizeBaseUrl(def rawUrl) {
    String value = (rawUrl == null) ? "" : rawUrl.toString().trim()
    if (!value) return ""
    while (value.endsWith("/")) { value = value.substring(0, value.length() - 1) }
    return value
}

private Integer safeInt(def value, Integer fallbackValue) {
    try {
        if (value == null) return fallbackValue
        if (value instanceof Number) return ((Number) value).intValue()
        String text = value.toString().trim()
        if (text == "") return fallbackValue
        if (text.contains(".")) return new BigDecimal(text).intValue()
        return text.toInteger()
    } catch (e) { return fallbackValue }
}

private Double safeDouble(def value, Double fallbackValue) {
    try {
        if (value == null) return fallbackValue
        if (value instanceof Number) return ((Number) value).doubleValue()
        String text = value.toString().trim()
        if (text == "") return fallbackValue
        return new BigDecimal(text).doubleValue()
    } catch (e) { return fallbackValue }
}

private Integer clampToInt(def rawValue, Integer minValue, Integer maxValue, Integer fallbackValue) {
    Integer value = safeInt(rawValue, fallbackValue)
    if (value < minValue) return minValue
    if (value > maxValue) return maxValue
    return value
}

private boolean asBool(def value) {
    if (value instanceof Boolean) return (Boolean) value
    String text = (value == null) ? "" : value.toString().trim().toLowerCase()
    return ["1", "true", "on", "yes"].contains(text)
}

private void debugLog(String msg) {
    if (settings?.enableDebugLogging != false) log.debug msg
}
