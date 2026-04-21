/**
 *  HubVoice Routine Device
 *  Created with help from Claude AI
 */

metadata {
    definition(
        name: "HubVoice Routine Device",
        namespace: "bptworld",
        author: "Bryan Turcotte",
        description: "Virtual device with optional momentary behavior"
    ) {
        capability "Actuator"
        capability "Switch"

        attribute "momentaryMode", "string"
        attribute "autoOffSeconds", "number"

        command "toggle"
        command "push"
    }

    preferences {
        input name: "momentaryModeEnabled", type: "bool", title: "Momentary mode", defaultValue: false, required: false
        input name: "autoOffSecondsSetting", type: "number", title: "Auto-off seconds", defaultValue: 1, required: false
        input name: "enableDebugLogging", type: "bool", title: "Enable debug logging", defaultValue: false, required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

private void initialize() {
    if(device.currentValue("switch") == null) {
        sendEvent(name: "switch", value: "off", isStateChange: true)
    }
    syncModeAttributes()
}

def parse(String description) {
    return null
}

def on() {
    debugLog("on()")
    unschedule("autoTurnOff")
    sendEvent(name: "switch", value: "on", isStateChange: true)

    if(isMomentaryModeEnabled()) {
        Integer secs = getAutoOffSeconds()
        runIn(secs, "autoTurnOff")
    }
}

def off() {
    debugLog("off()")
    unschedule("autoTurnOff")
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

def toggle() {
    String current = (device.currentValue("switch") ?: "off").toString()
    debugLog("toggle() current=${current}")
    if(current == "on") {
        off()
    } else {
        on()
    }
}

def push() {
    debugLog("push()")
    on()
}

def autoTurnOff() {
    debugLog("autoTurnOff()")
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

private boolean isMomentaryModeEnabled() {
    return settings?.momentaryModeEnabled == true
}

private Integer getAutoOffSeconds() {
    Integer secs = safeInt(settings?.autoOffSecondsSetting, 1)
    if(secs == null || secs < 1) secs = 1
    if(secs > 3600) secs = 3600
    return secs
}

private void syncModeAttributes() {
    sendEvent(name: "momentaryMode", value: isMomentaryModeEnabled() ? "on" : "off", isStateChange: true)
    sendEvent(name: "autoOffSeconds", value: getAutoOffSeconds(), isStateChange: true)
}

private Integer safeInt(def value, Integer fallback = null) {
    try {
        if(value == null) return fallback
        if(value instanceof Number) return ((Number)value).intValue()
        String s = value.toString().trim()
        if(!s) return fallback
        return Integer.parseInt(s.replaceAll("[^0-9-]", ""))
    } catch(e) {
        return fallback
    }
}

private void debugLog(String msg) {
    if(settings?.enableDebugLogging == true) {
        log.debug "HubVoice Routine Device: ${msg}"
    }
}
