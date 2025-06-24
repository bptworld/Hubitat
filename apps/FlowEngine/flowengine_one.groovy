/**
 *  **************** Flow Engine One ****************
 *  Design Usage:
 *  Feel the Flow
 *
 *  Copyright 2025 Bryan Turcotte (@bptworld)
 *
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 * ------------------------------------------------------------------------------------------------------------------------------
 *  Changes:
 *  1.0.0 - 06/21/25 - Initial Release
 */

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

definition(
    name: "Flow Engine One",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Feel the Flow - Unified App, Multiple Flows, Multiple JSONs",
    category: "Convenience",
    iconUrl: "", iconX2Url: "", iconX3Url: "",
    importUrl: "",
    oauth: true
)

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        installCheck()
        if(state.appInstalled == 'COMPLETE') {
            section(getFormat("header-green", " <b>Device Master List:</b>")) {}
            section(" Master List", hideable: true, hidden: true) {
                input "masterDeviceList", "capability.*", title: "Master List of Devices Used in this App <small><abbr title='Only devices selected here can be used in Flow Engine. This can be edited at anytime.'><b>- INFO -</b></abbr></small>", required:false, multiple:true, submitOnChange:true
            }

			section(getFormat("header-green", " Flow Engine Editor Infomation")) {
            	paragraph "This app is used to receive flow data from your Flow Engine Editor."
				paragraph "Copy and paste this info into the Flow Engine Editor - appId: ${state.appId} - token: ${state.token}"
				paragraph ""
				paragraph "<table width='100%'><tr><td align='center'><div style='font-size: 20px;font-weight: bold;'><a href='http://${location.hub.localIP}/local/flowengineeditor.html' target=_blank>Flow Engine Editor</a></div><div><small>Click to create Flows!</small></div></td></tr></table>"
				paragraph "<hr>"
				paragraph "Also note, that when saving this app (clicking Done) another file is created holding your Modes data. Anytime you edit/update your modes, be sure to come back here and simply hit 'Done'."
			}
			
            section(getFormat("header-green", " Select Flow Files to Enable")) {
                getFileList()
                input "flowFiles", "enum", title: "Choose one or more Flow JSON files to Enable (to pause a Flow, simply remove from this list)", required: false, multiple: true, options: state.jsonList, submitOnChange: true
				if (flowFiles) {
					paragraph "<small><b>Flows are enabled for:</b><br>${flowFiles.join('<br>')}</small>"
				}
            }
			
			section(getFormat("header-green", " Per-Flow Logging")) {
				if (settings?.flowFiles) {
					def opts = settings.flowFiles.collectEntries { fname -> [(fname): fname] }
					input "perFlowLogEnabled", "enum", title: "Enable logging for these flows", multiple: true, required: false, options: opts, submitOnChange: true
					if (perFlowLogEnabled) {
						paragraph "<small><b>Logging is enabled for:</b><br>${perFlowLogEnabled.join('<br>')}</small>"
					}
				} else {
					paragraph "Select at least one Flow JSON file to enable per-flow logging."
				}
			}
			section() {
				paragraph getFormat("line")
				paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
			}
        }
    }
}

def installed() { 
	initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    if (!state.accessToken) {
        createAccessToken()
        state.appId = app.id
        state.token = state.accessToken
    }
    if (!settings.flowFiles) return

    state.activeFlows = [:]
    unschedule()
    unsubscribe()
    settings.flowFiles.each { fname ->
        if(fname) loadAndStartFlow(fname)
    }
}

// --- REST API ENDPOINTS ---
mappings {
    path("/runFlow")         { action: [POST: "apiRunFlow" ] }
    path("/testFlow")        { action: [POST: "apiTestFlow" ] }
    path("/devices")         { action: [GET: "apiGetDevices"] }
    path("/uploadFile")      { action: [POST: "apiUploadFile"] }
    path("/listFiles")       { action: [GET: "apiListFiles"] }
    path("/getFile")         { action: [GET: "apiGetFile"] }
    path("/getModes")        { action: [POST: "exportModesToFile"] }
    path("/activeFlows")     { action: [GET: "apiActiveFlows"] }
}

// --- HANDLERS ---

def apiRunFlow() {
    // Expects: { "flow": "filename.json", ... }
    def json = request?.JSON
    def fname = json?.flow
    if (!fname || !state.activeFlows[fname]) {
        render status: 404, data: '{"error":"Flow not found"}'
        return
    }
    // Run the flow from its event triggers (simulate real trigger)
    def flowObj = state.activeFlows[fname]
    def flow = flowObj.flow
    if (!flow) {
        render status: 404, data: '{"error":"Flow not loaded"}'
        return
    }
    // Find all eventTrigger nodes and evaluate them as if a trigger happened
    def dataNodes = flow.drawflow?.Home?.data ?: [:]
    def triggered = 0
    dataNodes.each { id, node ->
        if (node.name == "eventTrigger") {
			def expectedValue = node.data.value
			def expectedPattern = node.data.clickPattern
			def eventValue = (evt instanceof Map) ? evt.value : evt?.value
			def eventPattern = (evt instanceof Map) ? evt.pattern : null

			// Strict value match (if set)
			if (expectedValue && eventValue && expectedValue.toString() != eventValue.toString()) {
				flowLog(fname, "eventTrigger did NOT match value: expected=${expectedValue}, actual=${eventValue}", "debug")
				return
			}
			// Strict pattern match (if set)
			if (expectedPattern && eventPattern && expectedPattern.toString() != eventPattern.toString()) {
				flowLog(fname, "eventTrigger did NOT match pattern: expected=${expectedPattern}, actual=${eventPattern}", "debug")
				return
			}

			// If matches, proceed as normal
			evaluateNode(fname, id, [name: "apiRunFlow", value: "API Run", pattern: eventPattern])
			triggered++
		}
    }
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson([result: "Flow triggered", triggered: triggered])
}

def apiTestFlow() {
    def json = params
	def fname = json?.flow
	def nodeId = json?.nodeId
	def value = (json?.value ?: "Test Triggered").toString().toLowerCase()
	log.info "[apiTestFlow] params: ${params} | request.JSON: ${request?.JSON} | using: ${fname}, ${nodeId}, ${value}"
    if (!fname || !state.activeFlows[fname]) {
		log.info "[apiTestFlow] Oh no, I'm in here!"
        render status: 404, data: '{"error":"Flow not found"}'
        return
    }
    // Don't error if nodeId is blank, but prefer to require it for accuracy

    // --- Build simulated event ---
    def evt = [name: "apiTestFlow", value: value]
	log.info "[apiTestFlow] evt: ${evt}"
    if (value in ["double", "doubletap", "double-tap"]) {
        evt.value = "on"
        evt.name = "pushed"
        evt.descriptionText = "Switch was doubleTapped"
        evt.data = "2"
        evt.pattern = "double"
    } else if (value in ["triple", "tripletap", "triple-tap"]) {
        evt.value = "on"
        evt.name = "pushed"
        evt.descriptionText = "Switch was tripleTapped"
        evt.data = "3"
        evt.pattern = "triple"
    } else if (value in ["hold", "held"]) {
        evt.value = "held"
        evt.name = "held"
        evt.descriptionText = "Switch was held"
        evt.data = "1"
        evt.pattern = "holdPerSecond"
    } else if (value.isInteger() && value.toInteger() > 1) {
        evt.value = "on"
        evt.name = "pushed"
        evt.descriptionText = "Switch was tapped ${value}x"
        evt.data = value
        evt.pattern = (value == "2") ? "double" : (value == "3") ? "triple" : "single"
    }
	log.info "[apiTestFlow] evt.value: ${evt.value} | evt.name: ${evt.name} | evt.descriptionText: ${evt.descriptionText} | evt.data: ${evt.data} | evt.pattern: ${evt.pattern}"

    // --- Route only to the specific node ---
    if (nodeId) {
        evaluateNode(fname, nodeId, evt)
    } else {
        // Fallback: run all eventTriggers if nodeId is missing (legacy support)
        log.warn "[${fname}] No nodeId provided for apiTestFlow, running all eventTrigger nodes"
        def flow = state.activeFlows[fname]?.flow
        flow?.drawflow?.Home?.data?.each { id, node ->
            if (node.name == "eventTrigger") {
				def expectedValue = node.data.value
				def expectedPattern = node.data.clickPattern
				def eventValue = (evt instanceof Map) ? evt.value : evt?.value
				def eventPattern = (evt instanceof Map) ? evt.pattern : null

				// Strict value match (if set)
				if (expectedValue && eventValue && expectedValue.toString() != eventValue.toString()) {
					flowLog(fname, "eventTrigger did NOT match value: expected=${expectedValue}, actual=${eventValue}", "debug")
					return
				}
				// Strict pattern match (if set)
				if (expectedPattern && eventPattern && expectedPattern.toString() != eventPattern.toString()) {
					flowLog(fname, "eventTrigger did NOT match pattern: expected=${expectedPattern}, actual=${eventPattern}", "debug")
					return
				}

				// If matches, proceed as normal
				evaluateNode(fname, id, [name: "apiRunFlow", value: "API Run", pattern: eventPattern])
				triggered++
			}
        }
    }
    render contentType: "application/json", data: '{"result":"Test triggered"}'
}

def apiGetDevices() {
    def output = []
    settings.masterDeviceList?.each { dev ->
        output << [
            id: dev.id,
            label: dev.displayName ?: dev.label ?: dev.name,
            name: dev.name,
            attributes: dev.supportedAttributes?.collect { attr ->
                [
                    name: attr.name,
                    currentValue: dev.currentValue(attr.name)
                ]
            } ?: [],
            commands: dev.supportedCommands?.collect { it.name } ?: []
        ]
    }
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson(output)
}

def apiUploadFile() {
    def name = params.name
    if (!name) {
        render status: 400, text: "Missing file name"
        return
    }
    def body = request?.body ?: request?.JSON
    if (!body) {
        render status: 400, text: "Missing file data"
        return
    }
    try {
        def fileText = (body instanceof String) ? body : groovy.json.JsonOutput.toJson(body)
        uploadHubFile(name, fileText.getBytes("UTF-8"))
        render contentType: "application/json", data: '{"result":"Upload successful"}'
    } catch (e) {
        render status: 500, text: "Error: ${e}"
    }
}

def apiListFiles() {
    def fileList = []
    def uri = "http://127.0.0.1:8080/hub/fileManager/json";
    try {
        httpGet([uri: uri]) { resp ->
            if (resp != null) {
                def json = resp.data
                json.files.each { rec ->
                    if (
                        rec.name?.toLowerCase()?.endsWith(".json") &&
                        !(rec.name?.startsWith("FE_")) &&
                        !(rec.name?.startsWith("var_"))
                    ) {
                        fileList << rec.name
                    }
                }
            }
        }
    } catch (e) {
        log.error e
    }
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson([files: fileList.sort()])
}

def apiGetFile() {
    def name = params.name
    if (!name) {
        render status: 400, text: "Missing file name"
        return
    }
    def fileData = null
    try {
        def url = "http://127.0.0.1:8080/local/${name}"
        httpGet([uri: url, contentType: 'text/plain']) { resp ->
            fileData = resp.data?.text
        }
        if (!fileData) {
            render status: 404, text: "File not found or empty"
            return
        }
        try {
            def obj = new groovy.json.JsonSlurper().parseText(fileData)
            render contentType: "application/json", data: groovy.json.JsonOutput.toJson(obj)
        } catch (ex) {
            render contentType: "text/plain", text: fileData
        }
    } catch (e) {
        render status: 500, text: "Error: ${e}"
    }
}

def exportModesToFile() {
    def currentMode = [ id: "current", name: location.mode]
    def modeList = location.modes.collect { [ id: it.id, name: it.name ] } + currentMode
    def json = groovy.json.JsonOutput.toJson([ modes: modeList ])
    uploadHubFile("FE_flowModes.json",json.getBytes())
    render contentType: "application/json", data: '{"result":"Modes exported"}'
}

def apiActiveFlows() {
    def list = []
    (state.activeFlows ?: [:]).each { flowName, flowObj ->
        list << [
            flowName: flowName,
            flow: flowObj?.flow ?: null,
            fileName: flowObj?.fileName ?: flowName
        ]
    }
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson(list)
}

def flowLog(fname, msg, level = "info") {
    if (settings?.perFlowLogEnabled && !(settings.perFlowLogEnabled.contains(fname))) return
    def prefix = "[${fname}]"
    switch(level) {
        case "warn":  log.warn "${prefix} ${msg}"; break
        case "error": log.error "${prefix} ${msg}"; break
        case "debug": log.debug "${prefix} ${msg}"; break
        default:      log.info "${prefix} ${msg}"
    }
}

def loadAndStartFlow(fname) {
    def flow = readFlowFile(fname)
    if(!flow) return

    state.activeFlows[fname] = [
        flow: flow,
        lastVarValues: [:],
        varCtx: [:],
        tapTracker: [:],
        holdTracker: [:],
        savedDeviceStates: [:],
        flowVars: [],
        globalVars: [],
        vars: [:]
    ]
    readAndParseFlow(fname)
    subscribeToTriggers(fname)
    scheduleVariablePolling(fname)
}

def getFileList() {
    state.jsonList = []
    try {
        def uri = "http://127.0.0.1:8080/hub/fileManager/json"
        httpGet([uri: uri]) { resp ->
            def json = resp.data
            json.files.each { rec ->
                if (
                    rec.name?.toLowerCase()?.endsWith(".json") &&
                    !(rec.name?.startsWith("FE_")) &&
                    !(rec.name?.startsWith("var_"))
                ) {
                    state.jsonList << rec.name
                }
            }
        }
    } catch (e) {
		flowLog(fname, "getFileList error: $e", "error")

    }
}

def readFlowFile(fname) {
    def uri = "http://127.0.0.1:8080/local/${fname}"
    try {
        def jsonStr = ""
        httpGet([uri: uri, contentType: "text/plain"]) { resp ->
            jsonStr = resp.data?.text
        }
        if(!jsonStr) return null
        return new JsonSlurper().parseText(jsonStr)
    } catch (e) {
		flowLog(fname, "readFlowFile($fname): $e", "error")
        return null
    }
}

def getDeviceById(id) {
    return settings.masterDeviceList?.find { it.id.toString() == id?.toString() }
}

// ---- Variable/Helper logic ----
def readAndParseFlow(fname) { loadVariables(fname) }
def loadVariables(fname) {
    def flowObj = state.activeFlows[fname]
    flowObj.varCtx = [:]
    getGlobalVars(fname)
    flowObj.globalVars.each { v ->
        flowObj.varCtx[v.name] = resolveVarValue(fname, v)
    }
}

def getGlobalVars(fname) {
    def flowObj = state.activeFlows[fname]
    flowObj.globalVars = []
    try {
        def uri = "http://127.0.0.1:8080/local/FE_global_vars.json"
        httpGet([uri: uri, contentType: "text/html; charset=UTF-8"]) { resp ->
            def jsonStr = resp.data?.text
            if (!jsonStr) return
            flowObj.globalVars = new JsonSlurper().parseText(jsonStr)
        }
    } catch (e) {
        flowObj.globalVars = []
    }
}

def resolveVarValue(fname, v, _visited = []) {
    if (!v || !v.name) return ""
    if (_visited.contains(v.name)) return "ERR:Circular"
    _visited += v.name
    def val = v.value
    if (val instanceof String && (val.contains('$(') || val.matches('.*[+\\-*/><=()].*'))) {
        return evalExpression(fname, val, _visited)
    }
    if (val ==~ /^-?\d+(\.\d+)?$/) return val.contains(".") ? val.toDouble() : val.toInteger()
    if ("$val".toLowerCase() == "true" || "$val".toLowerCase() == "false") return "$val".toLowerCase() == "true"
    return val
}

def evalExpression(fname, expr, _visited = []) {
    expr = expr.replaceAll(/\$\((\w+)\)/) { full, vname ->
        def flowObj = state.activeFlows[fname]
        def v = (flowObj.flowVars + flowObj.globalVars).find { it.name == vname }
        return v ? resolveVarValue(fname, v, _visited) : "null"
    }
    return expr
}

String resolveVars(fname, str) {
    if (!str || !(str instanceof String)) return str
    def flowObj = state.activeFlows[fname]
    def pattern = /\$\((\w+)\)|\$\{(\w+)\}/
    def out = str.replaceAll(pattern) { all, v1, v2 ->
        def var = v1 ?: v2
        flowObj.vars?.get(var)?.toString() ?:
        flowObj.flowVars?.find { it.name == var }?.value?.toString() ?:
        flowObj.globalVars?.find { it.name == var }?.value?.toString() ?:
        flowObj.varCtx?.get(var)?.toString() ?:
        ""
    }
    return out
}

// ---- Flow Triggers ----
def subscribeToTriggers(fname) {
    def flowObj = state.activeFlows[fname]
    if(!flowObj?.flow) return
    def flow = flowObj.flow
    def dataNodes = flow.drawflow?.Home?.data ?: [:]

    dataNodes.each { id, node ->
        if (node.name == "eventTrigger" || node.name == "schedule") {
            if (node.name == "schedule") {
                // add if you use
            } else if (node.data.deviceId == "__time__") {
                // add if you use
            } else if (node.data.deviceId == "__mode__") {
                subscribe(location, "mode", { evt -> handleEvent(evt, fname) })
            } else {
                def devIds = []
                if (node.data.deviceIds instanceof List && node.data.deviceIds) devIds = node.data.deviceIds
                else if (node.data.deviceId) devIds = [node.data.deviceId]
                def attr = node.data.attribute
                devIds.each { devId ->
                    def device = getDeviceById(devId)
                    if(device && attr) {
                        subscribe(device, attr, "genericDeviceHandler")
                    }
                }
            }
        }
    }
}

def genericDeviceHandler(evt) {
    state.activeFlows.each { fname, flowObj ->
        def triggerNodes = getTriggerNodes(fname, evt)
        if (triggerNodes && triggerNodes.size() > 0) {
            handleEvent(evt, fname)
        }
    }
}

def scheduleVariablePolling(fname) {
    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return
    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]
    def hasVarTrig = dataNodes.any { id, node ->
        node?.name == "eventTrigger" && (
            node.data?.deviceId == "__variable__" ||
            (node.data?.deviceIds instanceof List && node.data.deviceIds[0] == "__variable__")
        )
    }
    if(hasVarTrig) runEvery5Seconds("checkVariableTriggers_${fname}")
}

// ---- Variable trigger polling and tap/hold clear helpers ----
def checkVariableTriggers_BPTW_FLOW(fname) {
    def flowObj = state.activeFlows[fname]
    def nodes = flowObj.flow.drawflow?.Home?.data.findAll { id, node ->
        node?.name == "eventTrigger" &&
        (node.data?.deviceId == "__variable__" ||
         (node.data?.deviceIds instanceof List && node.data.deviceIds[0] == "__variable__"))
    }
    if (!nodes) return
    getGlobalVars(fname)
    def globals = flowObj.globalVars ?: []
    def globalMap = [:]
    globals.each { v -> globalMap[v.name] = v.value }
    nodes.each { id, node ->
        def varName = node.data?.varName
        if (!varName) return
        def curValue = globalMap[varName]
        def lastValue = flowObj.lastVarValues[varName]
        if (curValue != lastValue) {
            flowObj.lastVarValues[varName] = curValue
            evaluateNode(fname, id, [name: varName, value: curValue])
        }
    }
}

def clearTapTracker(data) {
    def fname = data.fname
    def flowObj = state.activeFlows[fname]
    if (flowObj) flowObj.tapTracker.remove("${data.devId}:${data.attr}")
}

def clearHoldTracker(data) {
    def fname = data.fname
    def flowObj = state.activeFlows[fname]
    if (flowObj) flowObj.holdTracker.remove("${data.devId}:${data.attr}")
}

// ---- Main Event Handler ----
def handleEvent(evt, fname) {
    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return
    flowObj.tapTracker = flowObj.tapTracker ?: [:]
    flowObj.holdTracker = flowObj.holdTracker ?: [:]
    def triggerNodes = getTriggerNodes(fname, evt)
    triggerNodes.each { triggerId, triggerNode ->
        def eventDeviceId = evt.device?.id?.toString()
        def attr = evt.name
        def pattern = triggerNode.data.clickPattern ?: "single"
		def incomingPattern = (evt instanceof Map) ? evt.pattern : null
		if (incomingPattern && pattern == incomingPattern) {
			flowLog(fname, "UI Test: Forcing trigger for pattern '${pattern}'", "debug")
			evaluateNode(fname, triggerId, evt)
			return
		}
		if (pattern == "single") {
			flowLog(fname, "In handleEvent - ${pattern}", "debug")
			evaluateNode(fname, triggerId, evt)
		} else if (pattern in ["double", "triple"]) {
			flowLog(fname, "In handleEvent - ${pattern}", "debug")
            def key = "${eventDeviceId}:${attr}"
            def required = (pattern == "double") ? 2 : 3
            def nowMs = now()
            def windowMs = 1200
            def tracker = flowObj.tapTracker[key] ?: [times: []]
            tracker.times = tracker.times.findAll { nowMs - it < windowMs }
            tracker.times << nowMs
            flowObj.tapTracker[key] = tracker
            if (tracker.times.size() == required) {
                evaluateNode(fname, triggerId, evt)
                flowObj.tapTracker.remove(key)
            } else {
                runIn(2, "clearTapTracker", [data: [fname: fname, devId: eventDeviceId, attr: attr]])
            }
        } else if (pattern == "holdPerSecond" && evt.value == "held") {
			flowLog(fname, "In handleEvent - ${pattern}", "debug")
            def key = "${eventDeviceId}:${attr}"
            def tracker = flowObj.holdTracker[key] ?: [lastSecond: -1, lastEpoch: 0]
            def durationSec = 0
            if (evt.data && evt.data =~ /\d+/) {
                def match = (evt.data =~ /\d+/)
                if (match) durationSec = match[0] as Integer
            } else if (evt.descriptionText && evt.descriptionText =~ /\d+ sec/) {
                def match = (evt.descriptionText =~ /(\d+) sec/)
                if (match) durationSec = match[0][1] as Integer
            } else {
                durationSec = tracker.lastSecond + 1
            }
            if (durationSec > tracker.lastSecond) {
                ((tracker.lastSecond + 1)..durationSec).each { _ ->
                    evaluateNode(fname, triggerId, evt)
                }
                tracker.lastSecond = durationSec
                tracker.lastEpoch = now()
                flowObj.holdTracker[key] = tracker
            }
            runIn(3, "clearHoldTracker", [data: [fname: fname, devId: eventDeviceId, attr: attr]])
        } else {
            evaluateNode(fname, triggerId, evt)
        }
    }
}

def getTriggerNodes(fname, evt) {
    def flowObj = state.activeFlows[fname]
    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]
    if (evt.name == "mode") {
        return dataNodes.findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == "__mode__" &&
            node.data.attribute == "mode"
        }
    } else {
        return dataNodes.findAll { id, node ->
            if (node.name != "eventTrigger") return false
            def devIds = []
            if (node.data.deviceIds instanceof List && node.data.deviceIds) devIds = node.data.deviceIds
            else if (node.data.deviceId) devIds = [node.data.deviceId]
            return devIds.contains(evt.device.id.toString()) && node.data.attribute == evt.name
        }
    }
}

// ---- Node Evaluation (ALL node types, with delay logic preserved) ----
def evaluateNode(fname, nodeId, evt, incomingValue = null, Set visited = null) {
	flowLog(fname, "In evaluateNode - fname: ${fname} - nodeId: ${nodeId} - evt: ${evt}", "debug")
    def flowObj = state.activeFlows[fname]
    if (!visited) visited = new HashSet()
			if (visited.contains(nodeId)) return null
    visited << nodeId
    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]
    def node = dataNodes[nodeId]
    if (!node) return null
	
	try {
		notifyFlowTrace(fname, nodeId, node?.name)
	} catch (e) {
		log.warn "Failed to write flow trace: $e"
	}

    switch (node.name) {
        case "eventTrigger":
			flowLog(fname, "In evaluateNode - eventTrigger", "debug")
			// ---- Strict matching added here ----
			def expectedValue = node.data.value
			def expectedPattern = node.data.clickPattern
			def eventValue = (evt instanceof Map) ? evt.value : evt?.value
			def eventPattern = (evt instanceof Map) ? evt.pattern : null

			if (expectedValue && eventValue && expectedValue.toString() != eventValue.toString()) {
				flowLog(fname, "eventTrigger did NOT match value: expected=${expectedValue}, actual=${eventValue}", "debug")
				return // STOP! Value didn't match
			}
			if (expectedPattern && eventPattern && expectedPattern.toString() != eventPattern.toString()) {
				flowLog(fname, "eventTrigger did NOT match pattern: expected=${expectedPattern}, actual=${eventPattern}", "debug")
				return // STOP! Pattern didn't match
			}
			// ---- END strict matching ----

			def passes = true
			node.outputs?.each { outName, outObj ->
				outObj.connections?.each { conn ->
					if (passes) evaluateNode(fname, conn.node, evt, null, visited)
				}
			}
			break

        case "condition":
			flowLog(fname, "In evaluateNode - condition", "debug")
            def devIds = []
            if (node.data.deviceIds instanceof List && node.data.deviceIds) devIds = node.data.deviceIds
            else if (node.data.deviceId) devIds = [node.data.deviceId]
            def attr = node.data.attribute
            def passes = false
            def currentValue
            if (evt && evt.name == attr && evt.value != null) {
                currentValue = evt.value
            } else if (devIds && devIds[0]) {
                def device = getDeviceById(devIds[0])
                currentValue = device?.currentValue(attr)
            } else {
                currentValue = null
            }
            passes = evaluateComparator(currentValue, resolveVars(fname, node.data.value), node.data.comparator)
            node.outputs?.output_1?.connections?.each { conn ->
                if (passes) evaluateNode(fname, conn.node, evt, null, visited)
            }
            node.outputs?.output_2?.connections?.each { conn ->
                if (!passes) evaluateNode(fname, conn.node, evt, null, visited)
            }
            return passes

        case "AND":
			flowLog(fname, "In evaluateNode - AND", "debug")
            def passes = (incomingValue == true)
            node.outputs?.true?.connections?.each { conn -> if (passes) evaluateNode(fname, conn.node, evt, null, visited) }
            node.outputs?.false?.connections?.each { conn -> if (!passes) evaluateNode(fname, conn.node, evt, null, visited) }
            return passes

        case "OR":
			flowLog(fname, "In evaluateNode - OR", "debug")
            def passes = (incomingValue == true)
            node.outputs?.output_1?.connections?.each { conn -> if (passes) evaluateNode(fname, conn.node, evt, null, visited) }
            node.outputs?.output_2?.connections?.each { conn -> if (!passes) evaluateNode(fname, conn.node, evt, null, visited) }
            return passes

        case "NOT":
			flowLog(fname, "In evaluateNode - NOT", "debug")
            def input = node.inputs?.collect { k, v -> v.connections*.node }.flatten()?.getAt(0)
            def result = !evaluateNode(fname, input, evt, null, visited)
            node.outputs?.true?.connections?.each { conn -> if (result) evaluateNode(fname, conn.node, evt, null, visited) }
            node.outputs?.false?.connections?.each { conn -> if (!result) evaluateNode(fname, conn.node, evt, null, visited) }
            return result

        case "device":
			flowLog(fname, "In evaluateNode - device", "debug")
            def devIds = []
            if (node.data.deviceIds instanceof List) devIds = node.data.deviceIds
            else if (node.data.deviceIds) devIds = [node.data.deviceIds]
            else if (node.data.deviceId) devIds = [node.data.deviceId]
            def cmd = resolveVars(fname, node.data.command)
            def val = resolveVars(fname, node.data.value)
            devIds.each { devId ->
                def device = getDeviceById(devId)
                if (device && cmd) {
                    if (val != null && val != "") {
                        device."${cmd}"(val)
                    } else {
                        device."${cmd}"()
                    }
                }
            }
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            return

        case "notification":
			flowLog(fname, "In evaluateNode - notification", "debug")
            sendNotification(fname, node.data, evt)
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "delayMin":
			flowLog(fname, "In evaluateNode - delayMin", "debug")
            def min = (node.data.delayMin ?: 1) as Integer
            pauseExecution(min * 60000)
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "delay":
			flowLog(fname, "In evaluateNode - delay", "debug")
            def ms = (node.data.delayMs ?: 1000) as Integer
            pauseExecution(ms)
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "setVariable":
            def varName = resolveVars(fname, node.data.varName)
            def varValue = resolveVars(fname, node.data.varValue)
            setVariable(fname, varName, varValue)
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "saveDeviceState":
			flowLog(fname, "In evaluateNode - saveDeviceState", "debug")
            def devId = node.data.deviceId
            if (devId) {
                def device = getDeviceById(devId)
                if (device) {
                    def devState = [:]
                    device.supportedAttributes.each { attr ->
                        try {
                            def val = device.currentValue(attr.name)
                            if (val != null) devState[attr.name] = val
                        } catch (e) {}
                    }
                    flowObj.savedDeviceStates = flowObj.savedDeviceStates ?: [:]
                    flowObj.savedDeviceStates[devId] = devState
                }
            }
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "restoreDeviceState":
			flowLog(fname, "In evaluateNode - restoreDeviceState", "debug")
            def devId = node.data.deviceId
            if (devId) {
                def device = getDeviceById(devId)
                def devState = flowObj.savedDeviceStates?.get(devId)
                if (device && devState) {
                    devState.each { attrName, attrValue ->
                        try {
                            def cmd = "set${attrName.capitalize()}"
                            if (device.hasCommand(cmd)) {
                                device."${cmd}"(attrValue)
                            } else if (attrName == "switch" && device.hasCommand(attrValue)) {
                                device."${attrValue}"()
                            }
                        } catch (e) {}
                    }
                }
            }
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "notMatchingVar":
			flowLog(fname, "In evaluateNode - notMatchingVar", "debug")
            def varName = resolveVars(fname, node.data.varName)
            def varValue = resolveVars(fname, node.data.varValue)
            def actual = flowObj.vars?.get(varName) ?: ""
            def passes = actual != varValue
            node.outputs?.output_1?.connections?.each { conn ->
                if (passes) evaluateNode(fname, conn.node, evt, null, visited)
            }
            node.outputs?.output_2?.connections?.each { conn ->
                if (!passes) evaluateNode(fname, conn.node, evt, null, visited)
            }
            return passes

		case "doNothing":
			flowLog(fname, "In evaluateNode - doNothing", "debug")
			break
		
        default:
            log.warn "Unknown node type: ${node.name}"
    }
}

void notifyFlowTrace(flowFile, nodeId, nodeType) {
    if (!flowFile) return
    // Track flows by runId (or generate one if missing)
    if (!state.flowTraces) state.flowTraces = []

    // When eventTrigger or new flow, start a new runId
    def runId = state.lastRunId
    if (nodeType == "eventTrigger" || !runId || state.lastFlowFile != flowFile) {
        runId = "${now()}_${new Random().nextInt(1000000)}"
        state.lastRunId = runId
        state.lastFlowFile = flowFile
        // Remove finished/old runs if needed (optional, e.g. keep last 3)
        if (state.flowTraces.size() > 2) state.flowTraces = state.flowTraces[-2..-1]
        // Add a new active flow path
        state.flowTraces << [ runId: runId, flowFile: flowFile, steps: [] ]
    }

    // Find the active flow object for this run
    def thisFlow = state.flowTraces.find { it.runId == runId }
    if (!thisFlow) {
        thisFlow = [ runId: runId, flowFile: flowFile, steps: [] ]
        state.flowTraces << thisFlow
    }

    def trace = [
        flowFile: flowFile,
        nodeId: nodeId,
        nodeType: nodeType,
        timestamp: new Date().time
    ]
    thisFlow.steps << trace

    // Optional: remove oldest steps if too big
    if (thisFlow.steps.size() > 40) thisFlow.steps = thisFlow.steps[-40..-1]

    // Save all active flow paths
    saveFlow("FE_flowtrace.json", state.flowTraces)
}

void saveFlow(fName, fData) {
    if(logEnable) log.debug "Saving to file - ${fName}"
	String listJson = JsonOutput.toJson(fData) as String
	uploadHubFile("${fName}",listJson.getBytes())
}

// ---- Notification support ----
def sendNotification(fname, data, evt) {
	flowLog(fname, "In sendNotification - data: ${data} - evt: ${evt}", "debug")
    def msg = data.message ?: ""
    msg = expandWildcards(fname, msg, evt)
    if (data.speakerId) {
        def speaker = getDeviceById(data.speakerId)
        if (speaker) speaker.speak(msg)
    }
    if (data.sendPush && data.pushDeviceId) {
        def push = getDeviceById(data.pushDeviceId)
        if (push) push.deviceNotification(msg)
    }
}

def expandWildcards(fname, msg, evt) {
	flowLog(fname, "In expandWildcards - msg: ${msg} - evt: ${evt}", "debug")
    def device = evt?.device?.displayName ?: ""
    def attribute = evt?.name ?: ""
    def value = evt?.value ?: ""
    def nowDate = new Date()
    def time24 = nowDate.format("HH:mm")
    def time12 = nowDate.format("h:mm a")
    def date = nowDate.format("MM-dd-yyyy")
    def wilds = [
        "{device}": device,
        "{attribute}": attribute,
        "{value}": value,
        "{time24}": time24,
        "{time12}": time12,
        "{date}": date,
        "{now}": nowDate.toString()
    ]
    wilds.each { k, v ->
        msg = msg.replace(k, v instanceof Closure ? v() : v)
    }
    // Handle {var:VAR} wildcards
    msg = msg.replaceAll(/\{var:([a-zA-Z0-9_]+)\}/) { m, varName -> getVarValue(fname, varName) }
	flowLog(fname, "In expandWildcards - msg: ${msg}", "debug")
    return msg
}

def getVarValue(fname, vname) {
	flowLog(fname, "In getVarValue - vname: ${vname}", "debug")
    def flowObj = state.activeFlows[fname]
    return flowObj.vars?.get(vname) ?: flowObj.varCtx?.get(vname) ?: ""
}

def setVariable(fname, varName, varValue) {
	flowLog(fname, "In setVariable - varName: ${varName} - varValue: ${varValue}", "debug")
    def flowObj = state.activeFlows[fname]
    getGlobalVars(fname)
    def updatedGlobal = false
    def updatedLocal = false
    flowObj.flowVars = flowObj.flowVars ?: []
    flowObj.globalVars = flowObj.globalVars ?: []
    flowObj.globalVars.each { v ->
        if (v.name == varName) {
            v.value = varValue
            updatedGlobal = true
        }
    }
    flowObj.flowVars.each { v ->
        if (v.name == varName) {
            v.value = varValue
            updatedLocal = true
        }
    }
    if (!updatedGlobal && !updatedLocal) {
        flowObj.flowVars << [name: varName, value: varValue]
        updatedLocal = true
    }
    flowObj.vars = flowObj.vars ?: [:]
    flowObj.varCtx = flowObj.varCtx ?: [:]
    flowObj.vars[varName] = varValue
    flowObj.varCtx[varName] = varValue
}

def saveGlobalVarsToFile(globals) {
	flowLog(fname, "In saveGlobalVarsToFile - globals: ${globals}", "debug")
    def flowFile = "FE_global_vars.json"
    def fData = groovy.json.JsonOutput.toJson(globals)
}

// --- Comparators ---
def evaluateComparator(actual, expected, cmp) {
    switch(cmp) {
        case "==":      return "$actual" == "$expected"
        case "!=":      return "$actual" != "$expected"
        case ">":       return toDouble(actual) > toDouble(expected)
        case "<":       return toDouble(actual) < toDouble(expected)
        case ">=":      return toDouble(actual) >= toDouble(expected)
        case "<=":      return toDouble(actual) <= toDouble(expected)
        case "contains":    return "$actual".toLowerCase().contains("$expected".toLowerCase())
        case "notcontains": return !("$actual".toLowerCase().contains("$expected".toLowerCase()))
        case "startsWith":  return "$actual".toLowerCase().startsWith("$expected".toLowerCase())
        case "endsWith":    return "$actual".toLowerCase().endsWith("$expected".toLowerCase())
        case "empty":   return !actual
        case "isTrue":  return actual == true || "$actual" == "true"
        case "isFalse": return actual == false || "$actual" == "false"
        default:        return "$actual" == "$expected"
    }
}

def toDouble(x) { try { return x as Double } catch(e) { return 0.0 } }

def getFormat(type, myText=null, page=null) {
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid #000000;box-shadow: 2px 3px #8B8F8F;border-radius: 5px'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />"
}

def installCheck(){
    state.appInstalled = app.getInstallationState() 
    if(state.appInstalled != 'COMPLETE'){
        section{paragraph "Please hit 'Done' to install '${app.label}' app "}
    } else {
        //if(logEnable) log.info "App Installed OK"
    }
}
