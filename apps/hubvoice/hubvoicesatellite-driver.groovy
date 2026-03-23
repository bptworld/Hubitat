metadata {
    definition(
        name: "HubVoice Satellite TTS",
        namespace: "bptworld",
        author: "Bryan Turcotte"
    ) {
        capability "Actuator"
        capability "Notification"
        capability "Refresh"

        attribute "lastMessage", "string"
        attribute "lastStatus", "string"
        attribute "targetSatellite", "string"
    }

    preferences {
        input name: "runtimeBaseUrl", type: "text", title: "HubVoice Runtime Base URL", description: "Example: http://192.168.1.50:8080", required: true
        input name: "satelliteId", type: "text", title: "Satellite ID", description: "Example: sat-lr", required: true
        input name: "requestTimeoutSeconds", type: "number", title: "HTTP timeout (seconds)", defaultValue: 10, required: false
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
    sendEvent(name: "targetSatellite", value: sanitizeSatelliteId(settings?.satelliteId), isStateChange: true)
    if(device.currentValue("lastStatus") == null) {
        sendEvent(name: "lastStatus", value: "ready", isStateChange: true)
    }
}

def parse(String description) {
    return null
}

def refresh() {
    sendEvent(name: "targetSatellite", value: sanitizeSatelliteId(settings?.satelliteId), isStateChange: true)
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
        if(value == null || value.toString().trim() == "") return fallbackValue
        return value.toString().trim().toInteger()
    } catch(e) {
        return fallbackValue
    }
}

private void debugLog(String msg) {
    if(settings?.enableDebugLogging != false) {
        log.debug msg
    }
}
