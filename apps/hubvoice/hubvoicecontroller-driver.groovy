metadata {
    definition(
        name: "HubVoice Controller",
        namespace: "bptworld",
        author: "Bryan Turcotte"
    ) {
        capability "Actuator"
        capability "Refresh"
        capability "Switch"

        attribute "lastStatus", "string"
        attribute "lastError", "string"
        attribute "lastSync", "string"

        attribute "hubmusicActive", "string"
        attribute "hubmusicMode", "string"
        attribute "hubmusicTitle", "string"
        attribute "hubmusicSourceUrl", "string"
        attribute "hubmusicSatellite", "string"
        attribute "hubmusicReachableCount", "number"
        attribute "stereoLeftDefault", "string"
        attribute "stereoRightDefault", "string"
        attribute "stereoVolumeDefault", "number"

        attribute "airplayRunning", "string"
        attribute "airplayName", "string"
        attribute "dlnaRunning", "string"
        attribute "dlnaName", "string"

        attribute "runtimeHost", "string"
        attribute "runtimePort", "number"

        command "syncNow"
        command "playHubMusic", [
            [name: "Source URL*", type: "STRING", description: "Audio URL to route"],
            [name: "Mode", type: "ENUM", constraints: ["single", "all_reachable", "stereo_pair"], description: "Playback mode"],
            [name: "Satellite", type: "STRING", description: "Optional satellite id/alias"],
            [name: "Exclude Satellite", type: "STRING", description: "Optional satellite id/alias to exclude"],
            [name: "Title", type: "STRING", description: "Optional title"]
        ]
        command "startDefaultHubMusic"
        command "stopHubMusic"
        command "setStereoDefaults", [
            [name: "Left Satellite", type: "STRING", description: "Stereo left default satellite"],
            [name: "Right Satellite", type: "STRING", description: "Stereo right default satellite"],
            [name: "Stereo Volume (0-100)", type: "NUMBER", description: "Default stereo test volume percent"]
        ]
        command "applySavedStereoDefaults"
        command "runStereoTest"
        command "runStereoTestOnSavedDefaults"
        command "runStereoTestWithSatellites", [
            [name: "Left Satellite", type: "STRING", description: "Optional stereo left satellite"],
            [name: "Right Satellite", type: "STRING", description: "Optional stereo right satellite"]
        ]
        command "startAirPlay", [
            [name: "Mode", type: "ENUM", constraints: ["single", "all_reachable", "stereo_pair"], description: "Playback mode"],
            [name: "Satellite", type: "STRING", description: "Optional satellite id/alias"],
            [name: "Exclude Satellite", type: "STRING", description: "Optional satellite id/alias to exclude"],
            [name: "Receiver Name", type: "STRING", description: "Optional AirPlay receiver name"]
        ]
        command "startDefaultAirPlay"
        command "stopAirPlay"
        command "startDLNA", [
            [name: "Mode", type: "ENUM", constraints: ["single", "all_reachable", "stereo_pair"], description: "Playback mode"],
            [name: "Satellite", type: "STRING", description: "Optional satellite id/alias"],
            [name: "Exclude Satellite", type: "STRING", description: "Optional satellite id/alias to exclude"],
            [name: "Friendly Name", type: "STRING", description: "Optional DLNA renderer name"]
        ]
        command "startDefaultDLNA"
        command "stopDLNA"
    }

    preferences {
        input name: "runtimeBaseUrl", type: "text", title: "HubVoice Runtime Base URL", description: "Example: http://192.168.1.50:8080", required: true
        input name: "requestTimeoutSeconds", type: "number", title: "HTTP timeout (seconds)", defaultValue: 10, required: false
        input name: "defaultHubMusicSourceUrl", type: "text", title: "Default HubMusic source URL", description: "Used by on()", required: false
        input name: "defaultHubMusicMode", type: "enum", title: "Default HubMusic mode", options: ["single", "all_reachable", "stereo_pair"], defaultValue: "single", required: false
        input name: "defaultSatellite", type: "text", title: "Default satellite (optional)", required: false
        input name: "defaultExcludeSatellite", type: "text", title: "Default exclude satellite (optional)", required: false
        input name: "defaultTitle", type: "text", title: "Default title", defaultValue: "HubVoice", required: false
        input name: "defaultStereoLeft", type: "text", title: "Default stereo left satellite (optional)", required: false
        input name: "defaultStereoRight", type: "text", title: "Default stereo right satellite (optional)", required: false
        input name: "defaultStereoVolume", type: "number", title: "Default stereo volume (0-100, optional)", required: false
        input name: "defaultAirPlayName", type: "text", title: "Default AirPlay receiver name", defaultValue: "HubVoiceAirPlay", required: false
        input name: "enableDebugLogging", type: "bool", title: "Enable debug logging", defaultValue: true, required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "lastStatus", value: "ready", isStateChange: true)
    sendEvent(name: "switch", value: "off", isStateChange: true)
    String baseUrl = resolveRuntimeBaseUrl()
    if(baseUrl) {
        syncNow()
    } else {
        updateStatus("missing_runtime_url", "Set runtimeBaseUrl and click Save Preferences")
    }
}

def parse(String description) {
    return null
}

def on() {
    String source = sanitizeText(settings?.defaultHubMusicSourceUrl)
    if(!source) {
        updateStatus("missing_default_source", "Set defaultHubMusicSourceUrl")
        return [ok: false, error: "missing_default_source"]
    }
    return playHubMusic(
        source,
        sanitizeMode(settings?.defaultHubMusicMode),
        sanitizeText(settings?.defaultSatellite),
        sanitizeText(settings?.defaultExcludeSatellite),
        sanitizeText(settings?.defaultTitle) ?: "HubVoice"
    )
}

def off() {
    return stopHubMusic()
}

def refresh() {
    return syncNow()
}

def syncNow() {
    Map result = runtimeGet("/hubmusic/status")
    if(!result?.ok || !(result?.status instanceof Map)) {
        String details = "Could not load /hubmusic/status"
        if(result?.statusCode) {
            details = "Could not load /hubmusic/status (HTTP ${result.statusCode})"
        } else if(result?.message) {
            details = sanitizeText(result.message)
        } else if(result?.error) {
            details = sanitizeText(result.error)
        }
        updateStatus("sync_failed", details)
        return result
    }

    Map status = result.status as Map
    Map airplay = (status.airplay instanceof Map) ? (Map) status.airplay : [:]
    Map dlna = (status.dlna instanceof Map) ? (Map) status.dlna : [:]

    sendEvent(name: "hubmusicActive", value: asBool(status.active) ? "on" : "off", isStateChange: true)
    sendEvent(name: "hubmusicMode", value: sanitizeText(status.mode), isStateChange: true)
    sendEvent(name: "hubmusicTitle", value: sanitizeText(status.title), isStateChange: true)
    sendEvent(name: "hubmusicSourceUrl", value: sanitizeText(status.source_url), isStateChange: true)
    sendEvent(name: "hubmusicSatellite", value: sanitizeText(status.satellite), isStateChange: true)
    sendEvent(name: "hubmusicReachableCount", value: safeInt(status.reachable_count, 0), isStateChange: true)
    sendEvent(name: "stereoLeftDefault", value: sanitizeText(status.stereo_left_default), isStateChange: true)
    sendEvent(name: "stereoRightDefault", value: sanitizeText(status.stereo_right_default), isStateChange: true)
    sendEvent(name: "stereoVolumeDefault", value: safeInt(status.stereo_volume_default, 0), isStateChange: true)

    sendEvent(name: "airplayRunning", value: asBool(airplay.running) ? "on" : "off", isStateChange: true)
    sendEvent(name: "airplayName", value: sanitizeText(airplay.name), isStateChange: true)
    sendEvent(name: "dlnaRunning", value: asBool(dlna.running) ? "on" : "off", isStateChange: true)
    sendEvent(name: "dlnaName", value: sanitizeText(dlna.friendly_name), isStateChange: true)

    sendEvent(name: "runtimeHost", value: sanitizeText(status.runtime_host), isStateChange: true)
    sendEvent(name: "runtimePort", value: safeInt(status.runtime_port, 8080), isStateChange: true)
    sendEvent(name: "switch", value: asBool(status.active) ? "on" : "off", isStateChange: true)

    sendEvent(name: "lastSync", value: new Date().format("yyyy-MM-dd HH:mm:ss", location?.timeZone ?: TimeZone.getTimeZone("UTC")), isStateChange: true)
    updateStatus("ok", "")
    return [ok: true, status: status]
}

def playHubMusic(String sourceUrl, String mode = null, String satellite = null, String excludeSatellite = null, String title = null) {
    String source = sanitizeText(sourceUrl)
    if(!source) {
        updateStatus("missing_source", "Source URL is required")
        return [ok: false, error: "missing_source"]
    }

    Map body = [
        source_url: source,
        mode: sanitizeMode(mode ?: settings?.defaultHubMusicMode),
        satellite: sanitizeText(satellite ?: settings?.defaultSatellite),
        exclude_satellite: sanitizeText(excludeSatellite ?: settings?.defaultExcludeSatellite),
        title: sanitizeText(title ?: settings?.defaultTitle) ?: "HubVoice"
    ]

    Map result = runtimePost("/hubmusic/play", body)
    if(result?.ok) {
        updateStatus("hubmusic_play_started", "")
        runIn(1, "syncNow")
    }
    return result
}

def startDefaultHubMusic() {
    return playHubMusic(
        sanitizeText(settings?.defaultHubMusicSourceUrl),
        sanitizeMode(settings?.defaultHubMusicMode),
        sanitizeText(settings?.defaultSatellite),
        sanitizeText(settings?.defaultExcludeSatellite),
        sanitizeText(settings?.defaultTitle) ?: "HubVoice"
    )
}

def stopHubMusic() {
    Map result = runtimePost("/hubmusic/stop", [:])
    if(result?.ok) {
        updateStatus("hubmusic_stopped", "")
        runIn(1, "syncNow")
    }
    return result
}

def setStereoDefaults(String leftSatellite = null, String rightSatellite = null, def volumePct = null) {
    Map body = [:]
    String left = sanitizeText(leftSatellite)
    String right = sanitizeText(rightSatellite)
    if(left) body.left_satellite = left
    if(right) body.right_satellite = right
    if(volumePct != null && sanitizeText(volumePct) != "") {
        body.volume_pct = clampPercent(safeInt(volumePct, 50))
    }
    if(body.isEmpty()) {
        updateStatus("stereo_defaults_missing", "Provide left, right, and/or volume")
        return [ok: false, error: "stereo_defaults_missing"]
    }

    Map result = runtimePost("/hubmusic/stereo-config", body)
    if(result?.ok) {
        updateStatus("stereo_defaults_updated", "")
        runIn(1, "syncNow")
    }
    return result
}

def applySavedStereoDefaults() {
    String left = resolveSavedStereoLeft()
    String right = resolveSavedStereoRight()
    Integer volume = resolveSavedStereoVolume()
    return setStereoDefaults(left, right, volume)
}

def runStereoTest() {
    Map result = runtimePost("/hubmusic/stereo-test", [:])
    if(result?.ok) {
        updateStatus("stereo_test_sent", "")
        runIn(1, "syncNow")
    }
    return result
}

def runStereoTestWithSatellites(String leftSatellite = null, String rightSatellite = null) {
    Map body = [:]
    String left = sanitizeText(leftSatellite)
    String right = sanitizeText(rightSatellite)
    if(left) body.left_satellite = left
    if(right) body.right_satellite = right

    Map result = runtimePost("/hubmusic/stereo-test", body)
    if(result?.ok) {
        updateStatus("stereo_test_sent", "")
        runIn(1, "syncNow")
    }
    return result
}

def runStereoTestOnSavedDefaults() {
    String left = resolveSavedStereoLeft()
    String right = resolveSavedStereoRight()
    Integer volume = resolveSavedStereoVolume()

    if(left || right || volume != null) {
        Map configResult = setStereoDefaults(left, right, volume)
        if(!configResult?.ok) {
            return configResult
        }
    }

    if(left || right) {
        return runStereoTestWithSatellites(left, right)
    }
    return runStereoTest()
}

def startAirPlay(String mode = null, String satellite = null, String excludeSatellite = null, String receiverName = null) {
    Map body = [
        mode: sanitizeMode(mode ?: settings?.defaultHubMusicMode),
        satellite: sanitizeText(satellite ?: settings?.defaultSatellite),
        exclude_satellite: sanitizeText(excludeSatellite ?: settings?.defaultExcludeSatellite)
    ]
    String name = sanitizeText(receiverName ?: settings?.defaultAirPlayName)
    if(name) {
        body.name = name
    }
    Map result = runtimePost("/airplay/start", body)
    if(result?.ok) {
        updateStatus("airplay_started", "")
        runIn(1, "syncNow")
    }
    return result
}

def startDefaultAirPlay() {
    return startAirPlay(
        sanitizeMode(settings?.defaultHubMusicMode),
        sanitizeText(settings?.defaultSatellite),
        sanitizeText(settings?.defaultExcludeSatellite),
        sanitizeText(settings?.defaultAirPlayName)
    )
}

def stopAirPlay() {
    Map result = runtimePost("/airplay/stop", [:])
    if(result?.ok) {
        updateStatus("airplay_stopped", "")
        runIn(1, "syncNow")
    }
    return result
}

def startDLNA(String mode = null, String satellite = null, String excludeSatellite = null, String friendlyName = null) {
    Map body = [
        mode: sanitizeMode(mode ?: settings?.defaultHubMusicMode),
        satellite: sanitizeText(satellite ?: settings?.defaultSatellite),
        exclude_satellite: sanitizeText(excludeSatellite ?: settings?.defaultExcludeSatellite),
        friendly_name: sanitizeText(friendlyName)
    ]
    Map result = runtimePost("/dlna/start", body)
    if(result?.ok) {
        updateStatus("dlna_started", "")
        runIn(1, "syncNow")
    }
    return result
}

def startDefaultDLNA() {
    return startDLNA(
        sanitizeMode(settings?.defaultHubMusicMode),
        sanitizeText(settings?.defaultSatellite),
        sanitizeText(settings?.defaultExcludeSatellite),
        ""
    )
}

def stopDLNA() {
    Map result = runtimePost("/dlna/stop", [:])
    if(result?.ok) {
        updateStatus("dlna_stopped", "")
        runIn(1, "syncNow")
    }
    return result
}

private Map runtimeGet(String path, Map query = [:]) {
    String baseUrl = resolveRuntimeBaseUrl()
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    if(!baseUrl) {
        updateStatus("missing_runtime_url", "Set runtimeBaseUrl and click Save Preferences")
        return [ok: false, error: "missing_runtime_url"]
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
            output = (code >= 200 && code < 300) ? data + [ok: (data.ok != false), statusCode: code] : [ok: false, statusCode: code, data: data]
        }
        if(!output.ok) {
            updateStatus("http_${output.statusCode ?: 'error'}", "")
        }
        return output
    } catch(e) {
        updateStatus("runtime_get_error", e?.toString())
        return [ok: false, error: "runtime_get_error", message: e?.toString()]
    }
}

private Map runtimePost(String path, Map body = [:]) {
    String baseUrl = resolveRuntimeBaseUrl()
    Integer timeout = safeInt(settings?.requestTimeoutSeconds, 10)
    if(!baseUrl) {
        updateStatus("missing_runtime_url", "Set runtimeBaseUrl and click Save Preferences")
        return [ok: false, error: "missing_runtime_url"]
    }

    Map params = [
        uri: "${baseUrl}${path}",
        requestContentType: "application/json",
        contentType: "application/json",
        body: body,
        timeout: timeout
    ]

    try {
        Map output = [ok: false]
        httpPost(params) { resp ->
            Integer code = resp?.status as Integer
            Map data = (resp?.data instanceof Map) ? (Map) resp.data : [:]
            output = (code >= 200 && code < 300) ? data + [ok: (data.ok != false), statusCode: code] : [ok: false, statusCode: code, data: data]
        }
        if(!output.ok) {
            updateStatus("http_${output.statusCode ?: 'error'}", "")
        }
        return output
    } catch(e) {
        updateStatus("runtime_post_error", e?.toString())
        return [ok: false, error: "runtime_post_error", message: e?.toString()]
    }
}

private void updateStatus(String status, String errorMessage) {
    sendEvent(name: "lastStatus", value: sanitizeText(status), isStateChange: true)
    sendEvent(name: "lastError", value: sanitizeText(errorMessage), isStateChange: true)
    debugLog("status=${status} err=${errorMessage}")
}

private String sanitizeText(def value) {
    return (value == null) ? "" : value.toString().trim()
}

private String sanitizeBaseUrl(def rawBaseUrl) {
    String value = sanitizeText(rawBaseUrl)
    if(!value) return ""
    if(!(value.toLowerCase().startsWith("http://") || value.toLowerCase().startsWith("https://"))) {
        value = "http://${value}"
    }
    while(value.endsWith("/")) {
        value = value.substring(0, value.length() - 1)
    }
    String lower = value.toLowerCase()
    if(lower.endsWith("/control")) {
        value = value.substring(0, value.length() - "/control".length())
    }
    lower = value.toLowerCase()
    if(lower.endsWith("/hubmusic") || lower.endsWith("/airplay") || lower.endsWith("/dlna")) {
        int slash = value.lastIndexOf("/")
        if(slash > value.indexOf("://") + 2) {
            value = value.substring(0, slash)
        }
    }
    return value
}

private String resolveRuntimeBaseUrl() {
    String configured = sanitizeBaseUrl(settings?.runtimeBaseUrl)
    if(configured) {
        state.lastRuntimeBaseUrl = configured
        return configured
    }
    return sanitizeBaseUrl(state?.lastRuntimeBaseUrl)
}

private String sanitizeMode(def value) {
    String mode = sanitizeText(value).toLowerCase()
    if(["single", "all_reachable", "stereo_pair"].contains(mode)) {
        return mode
    }
    return "single"
}

private String resolveSavedStereoLeft() {
    String fromSetting = sanitizeText(settings?.defaultStereoLeft)
    if(fromSetting) return fromSetting
    return sanitizeText(device.currentValue("stereoLeftDefault"))
}

private String resolveSavedStereoRight() {
    String fromSetting = sanitizeText(settings?.defaultStereoRight)
    if(fromSetting) return fromSetting
    return sanitizeText(device.currentValue("stereoRightDefault"))
}

private Integer resolveSavedStereoVolume() {
    String fromSetting = sanitizeText(settings?.defaultStereoVolume)
    if(fromSetting != "") {
        return clampPercent(safeInt(fromSetting, 50))
    }
    String fromAttribute = sanitizeText(device.currentValue("stereoVolumeDefault"))
    if(fromAttribute != "") {
        return clampPercent(safeInt(fromAttribute, 50))
    }
    return null
}

private Integer safeInt(def value, Integer fallbackValue) {
    try {
        if(value == null) return fallbackValue
        if(value instanceof Number) return ((Number) value).intValue()
        String text = value.toString().trim()
        if(text == "") return fallbackValue
        if(text.contains(".")) return new BigDecimal(text).intValue()
        return text.toInteger()
    } catch(e) {
        return fallbackValue
    }
}

private Integer clampPercent(Integer value) {
    if(value == null) return 0
    if(value < 0) return 0
    if(value > 100) return 100
    return value
}

private boolean asBool(def value) {
    if(value instanceof Boolean) return (Boolean) value
    String text = sanitizeText(value).toLowerCase()
    return ["1", "true", "on", "yes", "active"].contains(text)
}

private void debugLog(String msg) {
    if(settings?.enableDebugLogging != false) {
        log.debug msg
    }
}
