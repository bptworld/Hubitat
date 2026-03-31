metadata {
    definition(
        name: "HubVoice Satellite TTS",
        namespace: "bptworld",
        author: "Bryan Turcotte"
    ) {
        capability "Actuator"
        capability "Notification"
        capability "Refresh"
        capability "Switch"
        capability "SwitchLevel"

        attribute "lastMessage", "string"
        attribute "lastStatus", "string"
        attribute "targetSatellite", "string"
        attribute "whisperMode", "string"
        attribute "volume", "number"
        attribute "mute", "string"
        attribute "wakeSoundVolume", "number"
        attribute "wakeSoundMute", "string"
        attribute "bass", "number"
        attribute "treble", "number"
        attribute "lastSync", "string"
        attribute "syncStatus", "string"

        command "setSpeakerVolume", [[name: "Level*", type: "NUMBER", description: "0-100"]]
        command "mute"
        command "unmute"
        command "setWakeSoundVolume", [[name: "Level*", type: "NUMBER", description: "40-100"]]
        command "muteWakeSound"
        command "unmuteWakeSound"
        command "setBassLevel", [[name: "Bass dB*", type: "NUMBER", description: "-10 to 10"]]
        command "setTrebleLevel", [[name: "Treble dB*", type: "NUMBER", description: "-10 to 10"]]
        command "syncNow"
    }

    preferences {
        input name: "runtimeBaseUrl", type: "text", title: "HubVoice Runtime Base URL", description: "Example: http://192.168.1.50:8080", required: true
        input name: "satelliteId", type: "text", title: "Satellite ID", description: "Satellite ID or Alias (example: sat-lr or Living Room)", required: true
        input name: "requestTimeoutSeconds", type: "number", title: "HTTP timeout (seconds)", defaultValue: 10, required: false
        input name: "wakeMuteFloor", type: "number", title: "Wake sound mute floor", description: "Volume used when wake sound is muted", defaultValue: 40, required: false
        input name: "enableAutoSync", type: "bool", title: "Auto sync control values", defaultValue: true, required: false
        input name: "syncIntervalSeconds", type: "enum", title: "Sync interval", options: ["15":"Every 15 seconds", "30":"Every 30 seconds", "60":"Every 1 minute", "120":"Every 2 minutes", "300":"Every 5 minutes"], defaultValue: "30", required: false
        input name: "enableDebugLogging", type: "bool", title: "Enable debug logging", defaultValue: true, required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def initialize() {
    sendEvent(name: "targetSatellite", value: sanitizeSatelliteId(settings?.satelliteId), isStateChange: true)
    if(device.currentValue("lastStatus") == null) {
        sendEvent(name: "lastStatus", value: "ready", isStateChange: true)
    }
    if(device.currentValue("switch") == null) {
        sendEvent(name: "switch", value: "off", isStateChange: true)
    }
    if(device.currentValue("whisperMode") == null) {
        sendEvent(name: "whisperMode", value: "off", isStateChange: true)
    }
    if(device.currentValue("volume") == null) {
        sendEvent(name: "volume", value: 65, isStateChange: true)
    }
    if(device.currentValue("mute") == null) {
        sendEvent(name: "mute", value: "unmuted", isStateChange: true)
    }
    if(device.currentValue("wakeSoundVolume") == null) {
        sendEvent(name: "wakeSoundVolume", value: 65, isStateChange: true)
    }
    if(device.currentValue("wakeSoundMute") == null) {
        sendEvent(name: "wakeSoundMute", value: "unmuted", isStateChange: true)
    }
    if(device.currentValue("bass") == null) {
        sendEvent(name: "bass", value: 0, isStateChange: true)
    }
    if(device.currentValue("treble") == null) {
        sendEvent(name: "treble", value: 0, isStateChange: true)
    }
    if(device.currentValue("syncStatus") == null) {
        sendEvent(name: "syncStatus", value: "idle", isStateChange: true)
    }
    scheduleSync()
    refreshSatelliteState()
}

// Switch capability: on = whisper mode on, off = whisper mode off
def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
    sendEvent(name: "whisperMode", value: "on", isStateChange: true)
    setWhisperMode(true)
}

def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true)
    sendEvent(name: "whisperMode", value: "off", isStateChange: true)
    setWhisperMode(false)
}

def setWhisperMode(boolean enable) {
    String satId = sanitizeSatelliteId(settings?.satelliteId)
    String baseUrl = sanitizeBaseUrl(settings?.runtimeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    String stateStr = enable ? "on" : "off"

    debugLog("setWhisperMode: satId='${satId}', baseUrl='${baseUrl}', timeout=${timeout}, stateStr='${stateStr}'")

    if(!baseUrl || !satId) {
        log.warn "HubVoice Satellite TTS: runtimeBaseUrl or satelliteId not configured"
        return
    }

    Map params = [
        uri: "${baseUrl}/satellite-switch",
        query: [d: satId, entity: "whisper_mode", state: stateStr],
        timeout: timeout
    ]

    debugLog("Calling: ${baseUrl}/satellite-switch with query d=${satId}, entity=whisper_mode, state=${stateStr}")
    try {
        httpGet(params) { resp ->
            Integer code = resp?.status as Integer
            debugLog("Response status: ${code}")
            if(code >= 200 && code < 300) {
                sendEvent(name: "switch", value: stateStr, isStateChange: true)
                sendEvent(name: "whisperMode", value: stateStr, isStateChange: true)
                debugLog("Whisper mode set to ${stateStr} on '${satId}'")
            } else {
                log.warn "HubVoice Satellite TTS: satellite-switch returned status ${code}"
            }
        }
    } catch(e) {
        log.error "HubVoice Satellite TTS: failed to set whisper mode: ${e.message}"
        debugLog("Exception details: ${e}")
    }
}

def parse(String description) {
    return null
}

def refresh() {
    sendEvent(name: "targetSatellite", value: sanitizeSatelliteId(settings?.satelliteId), isStateChange: true)
    refreshSatelliteState()
}

def syncNow() {
    refreshSatelliteState()
}

def setLevel(level, duration = null) {
    return setSpeakerVolume(level)
}

def setSpeakerVolume(level) {
    Integer normalized = clampToInt(level, 0, 100, 65)
    Map result = runtimePost("/satellite-media-volume", [satellite: sanitizeSatelliteId(settings?.satelliteId), value: normalized])
    if(result?.ok) {
        markPendingValue("volume", normalized)
        sendEvent(name: "volume", value: normalized, isStateChange: true)
        if(result?.applied == false) {
            updateStatus("speaker_volume_deferred_${normalized}", null)
            sendEvent(name: "syncStatus", value: "pending_apply", isStateChange: true)
        } else {
            updateStatus("speaker_volume_${normalized}", null)
        }
        runIn(1, "refreshSatelliteState")
    }
    return result
}

def mute() {
    Map result = runtimePost("/satellite-media", [satellite: sanitizeSatelliteId(settings?.satelliteId), muted: true])
    if(result?.ok) {
        markPendingValue("mute", "muted")
        sendEvent(name: "mute", value: "muted", isStateChange: true)
        updateStatus("speaker_muted", null)
        runIn(1, "refreshSatelliteState")
    }
    return result
}

def unmute() {
    Map result = runtimePost("/satellite-media", [satellite: sanitizeSatelliteId(settings?.satelliteId), muted: false])
    if(result?.ok) {
        markPendingValue("mute", "unmuted")
        sendEvent(name: "mute", value: "unmuted", isStateChange: true)
        updateStatus("speaker_unmuted", null)
        runIn(1, "refreshSatelliteState")
    }
    return result
}

def setWakeSoundVolume(level) {
    Integer wakeMin = clampToInt(settings?.wakeMuteFloor, 0, 100, 40)
    Integer normalized = clampToInt(level, wakeMin, 100, wakeMin)
    Map result = runtimePost("/satellite-number", [satellite: sanitizeSatelliteId(settings?.satelliteId), entity: "wake_sound_volume", value: normalized])
    if(result?.ok) {
        String wakeMuteState = (normalized <= wakeMin) ? "muted" : "unmuted"
        markPendingValue("wakeSoundVolume", normalized)
        markPendingValue("wakeSoundMute", wakeMuteState)
        sendEvent(name: "wakeSoundVolume", value: normalized, isStateChange: true)
        sendEvent(name: "wakeSoundMute", value: wakeMuteState, isStateChange: true)
        if(normalized > wakeMin) {
            state.wakePreviousVolume = normalized
        }
        updateStatus("wake_volume_${normalized}", null)
        runIn(1, "refreshSatelliteState")
    }
    return result
}

def muteWakeSound() {
    Integer wakeMin = clampToInt(settings?.wakeMuteFloor, 0, 100, 40)
    Integer current = clampToInt(device.currentValue("wakeSoundVolume"), wakeMin, 100, wakeMin)
    if(current > wakeMin) {
        state.wakePreviousVolume = current
    }
    return setWakeSoundVolume(wakeMin)
}

def unmuteWakeSound() {
    Integer wakeMin = clampToInt(settings?.wakeMuteFloor, 0, 100, 40)
    Integer restored = clampToInt(state.wakePreviousVolume, wakeMin, 100, Math.max(wakeMin + 5, 65))
    return setWakeSoundVolume(restored)
}

def setBassLevel(level) {
    Integer normalized = clampToInt(level, -10, 10, 0)
    Map result = runtimePost("/satellite-number", [satellite: sanitizeSatelliteId(settings?.satelliteId), entity: "bass_level", value: normalized])
    if(result?.ok) {
        markPendingValue("bass", normalized)
        sendEvent(name: "bass", value: normalized, isStateChange: true)
        updateStatus("bass_${normalized}", null)
        runIn(1, "refreshSatelliteState")
    }
    return result
}

def setTrebleLevel(level) {
    Integer normalized = clampToInt(level, -10, 10, 0)
    Map result = runtimePost("/satellite-number", [satellite: sanitizeSatelliteId(settings?.satelliteId), entity: "treble_level", value: normalized])
    if(result?.ok) {
        markPendingValue("treble", normalized)
        sendEvent(name: "treble", value: normalized, isStateChange: true)
        updateStatus("treble_${normalized}", null)
        runIn(1, "refreshSatelliteState")
    }
    return result
}



def deviceNotification(String text) {
    return sendRuntimeAnnouncement(text)
}

private Map sendRuntimeAnnouncement(def rawText) {
    String text = sanitizeText(rawText)
    String satId = sanitizeSatelliteId(settings?.satelliteId)
    String baseUrl = sanitizeBaseUrl(settings?.runtimeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)

    if(!text) {
        updateStatus("ignored_empty_message", null)
        return [ok: false, error: "empty_message"]
    }
    if(!baseUrl) {
        updateStatus("missing_runtime_url", text)
        log.warn "HubVoice Satellite TTS: runtimeBaseUrl is not configured"
        return [ok: false, error: "missing_runtime_url"]
    }
    if(!satId) {
        updateStatus("missing_satellite_id", text)
        log.warn "HubVoice Satellite TTS: satelliteId is not configured"
        return [ok: false, error: "missing_satellite_id"]
    }

    Map params = [
        uri: "${baseUrl}/answer",
        query: [r: text, d: satId],
        timeout: timeout
    ]

    debugLog("Queueing message for satellite '${satId}': ${text}")
    try {
        httpGet(params) { resp ->
            Integer code = resp?.status as Integer
            if(code >= 200 && code < 300) {
                updateStatus("queued", text)
                debugLog("Runtime accepted message for satellite '${satId}' with status ${code}")
            } else {
                updateStatus("runtime_status_${code}", text)
                log.warn "HubVoice Satellite TTS: runtime returned status ${code}"
            }
        }
        return [ok: true, queued: true, satellite: satId, text: text]
    } catch(e) {
        updateStatus("runtime_error", text)
        log.warn "HubVoice Satellite TTS: failed to send message to runtime: ${e}"
        return [ok: false, error: "runtime_error", message: e?.toString()]
    }
}

private void refreshSatelliteState() {
    String satId = sanitizeSatelliteId(settings?.satelliteId)
    if(!satId) {
        sendEvent(name: "syncStatus", value: "missing_satellite", isStateChange: true)
        updateStatus("missing_satellite_id", null)
        return
    }
    Map result = runtimeGet("/satellites")
    if(!(result?.ok) || !(result?.satellites instanceof List)) {
        sendEvent(name: "syncStatus", value: "runtime_unreachable", isStateChange: true)
        return
    }
    Map selected = null
    String wanted = satId.toLowerCase()
    selected = (result.satellites as List).find { row ->
        String id = (row?.id ?: "").toString().toLowerCase()
        String alias = (row?.alias ?: "").toString().toLowerCase()
        return id == wanted || alias == wanted
    } as Map
    if(!(selected?.control_state instanceof Map)) {
        sendEvent(name: "syncStatus", value: "satellite_not_found", isStateChange: true)
        updateStatus("satellite_not_found", null)
        return
    }

    Map c = selected.control_state as Map
    Integer volume = resolvePendingInt("volume", c.speaker_volume, 0, 100, 65)
    Integer wakeFloor = clampToInt(settings?.wakeMuteFloor, 0, 100, 40)
    Integer wakeVolume = resolvePendingInt("wakeSoundVolume", c.wake_sound_volume, wakeFloor, 100, wakeFloor)
    Integer bass = resolvePendingInt("bass", c.bass_level, -10, 10, 0)
    Integer treble = resolvePendingInt("treble", c.treble_level, -10, 10, 0)
    String speakerMuteValue = resolvePendingString("mute", asBool(c.speaker_muted) ? "muted" : "unmuted")
    String wakeMuteValue = resolvePendingString("wakeSoundMute", (asBool(c.wake_muted) || wakeVolume <= wakeFloor) ? "muted" : "unmuted")

    sendEvent(name: "volume", value: volume, isStateChange: true)
    sendEvent(name: "mute", value: speakerMuteValue, isStateChange: true)
    sendEvent(name: "wakeSoundVolume", value: wakeVolume, isStateChange: true)
    sendEvent(name: "wakeSoundMute", value: wakeMuteValue, isStateChange: true)
    sendEvent(name: "bass", value: bass, isStateChange: true)
    sendEvent(name: "treble", value: treble, isStateChange: true)
    sendEvent(name: "lastSync", value: new Date().format("yyyy-MM-dd HH:mm:ss", location?.timeZone ?: TimeZone.getTimeZone("UTC")), isStateChange: true)
    sendEvent(name: "syncStatus", value: hasPendingValues() ? "pending_apply" : "ok", isStateChange: true)
    updateStatus("refreshed", null)
}

private void scheduleSync() {
    boolean enabled = settings?.enableAutoSync != false
    if(!enabled) {
        sendEvent(name: "syncStatus", value: "auto_sync_disabled", isStateChange: true)
        return
    }

    Integer seconds = safeInt(settings?.syncIntervalSeconds, 30)
    if(seconds <= 15) {
        runEvery15Seconds("refreshSatelliteState")
    } else if(seconds <= 30) {
        runEvery30Seconds("refreshSatelliteState")
    } else if(seconds <= 60) {
        runEvery1Minute("refreshSatelliteState")
    } else if(seconds <= 120) {
        runEvery2Minutes("refreshSatelliteState")
    } else {
        runEvery5Minutes("refreshSatelliteState")
    }
}

private Map runtimeGet(String path, Map query = [:]) {
    String satId = sanitizeSatelliteId(settings?.satelliteId)
    String baseUrl = sanitizeBaseUrl(settings?.runtimeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)

    if(!baseUrl || !satId) {
        log.warn "HubVoice Satellite TTS: runtimeBaseUrl or satelliteId not configured"
        return [ok: false, error: "missing_config"]
    }

    Map params = [
        uri: "${baseUrl}${path}",
        query: query,
        timeout: timeout
    ]

    try {
        Map output = [ok: false]
        httpGet(params) { resp ->
            Integer code = resp?.status as Integer
            Map data = (resp?.data instanceof Map) ? (Map) resp.data : [:]
            output = (code >= 200 && code < 300) ? data + [ok: (data.ok != false)] : [ok: false, status: code, data: data]
        }
        return output
    } catch(e) {
        log.warn "HubVoice Satellite TTS: GET ${path} failed: ${e}"
        return [ok: false, error: "runtime_get_error", message: e?.toString()]
    }
}

private Map runtimePost(String path, Map body = [:]) {
    String satId = sanitizeSatelliteId(settings?.satelliteId)
    String baseUrl = sanitizeBaseUrl(settings?.runtimeBaseUrl)
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)

    if(!baseUrl || !satId) {
        log.warn "HubVoice Satellite TTS: runtimeBaseUrl or satelliteId not configured"
        return [ok: false, error: "missing_config"]
    }

    Map payload = [:] + body
    if(!(payload.satellite)) {
        payload.satellite = satId
    }

    Map params = [
        uri: "${baseUrl}${path}",
        requestContentType: "application/json",
        contentType: "application/json",
        body: payload,
        timeout: timeout
    ]

    try {
        Map output = [ok: false]
        httpPost(params) { resp ->
            Integer code = resp?.status as Integer
            Map data = (resp?.data instanceof Map) ? (Map) resp.data : [:]
            output = (code >= 200 && code < 300) ? data + [ok: (data.ok != false)] : [ok: false, status: code, data: data]
        }
        if(!output.ok) {
            updateStatus("runtime_status_${output.status ?: 'error'}", null)
        }
        return output
    } catch(e) {
        updateStatus("runtime_error", null)
        log.warn "HubVoice Satellite TTS: POST ${path} failed: ${e}"
        return [ok: false, error: "runtime_post_error", message: e?.toString()]
    }
}

private void updateStatus(String status, String text) {
    sendEvent(name: "lastStatus", value: status, isStateChange: true)
    if(text != null) {
        sendEvent(name: "lastMessage", value: text.take(255), isStateChange: true)
    }
    sendEvent(name: "targetSatellite", value: sanitizeSatelliteId(settings?.satelliteId), isStateChange: true)
}

private String sanitizeText(def rawText) {
    return (rawText == null) ? "" : rawText.toString().trim()
}

private String sanitizeSatelliteId(def rawSatelliteId) {
    return (rawSatelliteId == null) ? "" : rawSatelliteId.toString().trim()
}

private String sanitizeBaseUrl(def rawBaseUrl) {
    String value = (rawBaseUrl == null) ? "" : rawBaseUrl.toString().trim()
    if(!value) return ""
    while(value.endsWith("/")) {
        value = value.substring(0, value.length() - 1)
    }
    return value
}

private Integer safeInt(def value, Integer fallbackValue) {
    try {
        if(value == null) return fallbackValue
        if(value instanceof Number) return ((Number) value).intValue()
        String text = value.toString().trim()
        if(text == "") return fallbackValue
        if(text.contains(".")) {
            return new BigDecimal(text).intValue()
        }
        return text.toInteger()
    } catch(e) {
        return fallbackValue
    }
}

private Integer clampToInt(def rawValue, Integer minValue, Integer maxValue, Integer fallbackValue) {
    Integer value = safeInt(rawValue, fallbackValue)
    if(value < minValue) return minValue
    if(value > maxValue) return maxValue
    return value
}

private void markPendingValue(String key, def value) {
    if(!(state.pendingSyncValues instanceof Map)) {
        state.pendingSyncValues = [:]
    }
    state.pendingSyncValues[key] = [
        value: value,
        ts: now()
    ]
}

private boolean hasPendingValues() {
    prunePendingValues(7000L)
    return (state.pendingSyncValues instanceof Map) && !((Map) state.pendingSyncValues).isEmpty()
}

private void prunePendingValues(Long windowMs) {
    if(!(state.pendingSyncValues instanceof Map)) {
        state.pendingSyncValues = [:]
        return
    }
    long nowMs = now()
    Map current = (Map) state.pendingSyncValues
    Map kept = [:]
    current.each { String k, def rec ->
        Long ts = safeLong(rec?.ts, 0L)
        if((nowMs - ts) <= windowMs) {
            kept[k] = rec
        }
    }
    state.pendingSyncValues = kept
}

private Integer resolvePendingInt(String key, def remoteValue, Integer minValue, Integer maxValue, Integer fallbackValue) {
    prunePendingValues(7000L)
    Integer remote = clampToInt(remoteValue, minValue, maxValue, fallbackValue)
    if(!(state.pendingSyncValues instanceof Map)) {
        return remote
    }
    def rec = ((Map) state.pendingSyncValues)[key]
    if(!(rec instanceof Map)) {
        return remote
    }
    Integer pending = clampToInt(rec.value, minValue, maxValue, remote)
    if(remote == pending) {
        ((Map) state.pendingSyncValues).remove(key)
        return remote
    }
    return pending
}

private String resolvePendingString(String key, String remoteValue) {
    prunePendingValues(7000L)
    String remote = (remoteValue ?: "").toString()
    if(!(state.pendingSyncValues instanceof Map)) {
        return remote
    }
    def rec = ((Map) state.pendingSyncValues)[key]
    if(!(rec instanceof Map)) {
        return remote
    }
    String pending = (rec.value ?: "").toString()
    if(remote == pending) {
        ((Map) state.pendingSyncValues).remove(key)
        return remote
    }
    return pending
}

private Long safeLong(def value, Long fallbackValue) {
    try {
        if(value == null || value.toString().trim() == "") return fallbackValue
        return value.toString().trim().toLong()
    } catch(e) {
        return fallbackValue
    }
}

private boolean asBool(def value) {
    if(value instanceof Boolean) return (Boolean) value
    String text = (value == null) ? "" : value.toString().trim().toLowerCase()
    return ["1", "true", "on", "yes", "muted"].contains(text)
}

private void debugLog(String msg) {
    if(settings?.enableDebugLogging != false) {
        log.debug msg
    }
}
