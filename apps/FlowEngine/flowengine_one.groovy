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
 *  1.0.0 - 07/14/25 - Initial Release
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
				paragraph "<enter><b>Do not share your token with anyone, especially in screenshots!</b></center>"
				paragraph "<table width='100%'><tr><td align='center'><div style='font-size: 20px;font-weight: bold;'><a href='http://${location.hub.localIP}/local/flowengineeditor.html' target=_blank>Flow Engine Editor</a></div><div><small>Click to create Flows!</small></div></td></tr></table>"
				paragraph "<hr>"
				paragraph "Also note, that when saving this app (clicking Done) another file is created holding your Modes data. Anytime you edit/update your modes, be sure to come back here and simply hit 'Done'."
				paragraph "<hr>"
				paragraph "I've been having an issue with the app OAuth changing on it's own.  If this happens to you, you'll need to update your OAuth token and add the appId and Token in the Editor. Once you have use the toggle below, click 'Done' and come back into the app to see your new token. AT THIS POINT DO NOT HIT 'DONE'.  CLICK ON ANYTHING OUTSIDE OF THE APP - DEVICES, APPS, SETTINGS, ANYTHING.  JUST DON'T HIT 'DONE'."
				
				input "updateOAuth", "bool", title: "Do you need to update OAuth?", submitOnChange:true
				if(updateOAuth) {
					input "areYouSureOAuth", "bool", title: "Are you sure?", submitOnChange:true
					if(areYouSureOAuth) {
						createAccessToken()
						state.appId = app.id
						state.token = state.accessToken
						pauseExecution(1000)
						changed = true
						app.updateSetting("updateOAuth",[value:"false",type:"bool"])
						app.updateSetting("areYouSureOAuth",[value:"false",type:"bool"])
					}
				}
				paragraph "<hr>"
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
					input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
					if(logEnable) {
						def opts = settings.flowFiles.collectEntries { fname -> [(fname): fname] }
						input "perFlowLogEnabled", "enum", title: "Enable logging for these flows", multiple: true, required: false, options: opts, submitOnChange: true
						if (perFlowLogEnabled) {
							paragraph "<small><b>Logging is enabled for:</b><br>${perFlowLogEnabled.join('<br>')}</small>"
						}
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
	path("/forceReload") 	 { action: [POST: "apiForceReload" ] }
	path("/selectFlow") 	 { action: [POST: "apiSelectFlow"] }
	path("/deselectFlow") 	 { action: [POST: "apiDeselectFlow"] }
	path("/selectFlowLog") 	 { action: [POST: "apiSelectFlowLogging"] }
	path("/deselectFlowLog") { action: [POST: "apiDeselectFlowLogging"] }
	path("/settings") 		 { action: [GET: "apiGetSettings"] }
	path("/deleteFile") 	 { action: [GET: "apiDeleteFlow", DELETE: "apiDeleteFlow"] }
}

// --- HANDLERS ---

def handleLocationTimeEvent(evt) {
    log.debug "Sun event: ${evt.name}@${evt.date}"
    state.activeFlows.each { fname, flowObj ->
        def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]
        def matches = dataNodes.findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == "__time__" &&
            node.data.attribute == "timeOfDay" &&
            (
              node.data.comparator == "between"
                ? (node.data.value instanceof List && node.data.value[0] == evt.name)
                : (node.data.value == evt.name)
            )
        }

        matches.each { id, node ->
            handleEvent(evt, fname)
        }
    }
}

def apiDeleteFlow() {
    // Read the filename from the query string: ?name=FlowName.json
    def fname = params.name
    if (!fname) {
        render status: 400, contentType: "application/json",
               data: '{"error":"Missing file name"}'
        return
    }
    // Ensure it ends in .json
    if (!fname.toLowerCase().endsWith('.json')) {
        fname += '.json'
    }
    deleteHubFile(fname)

    // Re-initialize so settings.flowFiles removes it, if needed
    updated()
    flowLog(fname, "File has been deleted from File Manager", "info")

    render contentType: "application/json",
           data: groovy.json.JsonOutput.toJson([result: "File deleted"])
}

def apiGetSettings() {
    def keys = ["perFlowLogEnabled", "logEnable", "flowFiles"]
    def out = [:]
    keys.each { out[it] = settings[it] }
    render contentType: "application/json", data: JsonOutput.toJson(out)
}

def apiSelectFlow() {
    def fn = request?.JSON?.flow
	def fname = fn + ".json"

    // Ensure json list is populated
    getFileList()
    def validOptions = state.jsonList ?: []
    if (!validOptions.contains(fname)) {
		flowLog(fname, "Stopped - Flow file not found", "error")
        render status: 404, contentType: "application/json", data: '{"error":"Flow file not found"}'
        return
    }

    def existing = settings?.flowFiles ?: []
    def newList = (existing + fname).unique().findAll { validOptions.contains(it) }

    // Force-save using app.updateSetting with correct enum options
    app.updateSetting("flowFiles", [type: "enum", value: newList])

    // Optional: Reinitialize the app to activate the flow
    updated()
	flowLog(fname, "File has been selected and enabled in app", "info")
    render contentType: "application/json", data: '{"result":"Flow selected and enabled in app"}'
}

def apiDeselectFlow() {
    def fn = request?.JSON?.flow
	def fname = fn + ".json"
	
    if (!fname) {
        render status: 400, contentType: "application/json", data: '{"error":"Missing flow filename"}'
        return
    }

    getFileList()
    def validOptions = state.jsonList ?: []
    def existing = settings?.flowFiles ?: []
    def newList = existing.findAll { it != fname && validOptions.contains(it) }

    app.updateSetting("flowFiles", [type: "enum", value: newList])
    updated()
	flowLog(fname, "File has been de-selected and disabled in app", "info")
    render contentType: "application/json", data: '{"result":"Flow deselected in app"}'
}

def apiSelectFlowLogging() {
    def fn = request?.JSON?.flow
	def fname = fn + ".json"

    // Ensure json list is populated
    getFileList()
    def validOptions = state.jsonList ?: []
    if (!validOptions.contains(fname)) {
		flowLog(fname, "Stopped - Flow file not found", "error")
        render status: 404, contentType: "application/json", data: '{"error":"Flow file not found"}'
        return
    }

    def existing = settings?.perFlowLogEnabled ?: []
    def newList = (existing + fname).unique().findAll { validOptions.contains(it) }

    // Force-save using app.updateSetting with correct enum options
    app.updateSetting("perFlowLogEnabled", [type: "enum", value: newList])

    // Optional: Reinitialize the app to activate the flow
    updated()
	flowLog(fname, "Logging has been enabled in app", "info")
    render contentType: "application/json", data: '{"result":"Logging enabled in app"}'
}

def apiDeselectFlowLogging() {
    def fn = request?.JSON?.flow
	def fname = fn + ".json"
	
    if (!fname) {
        render status: 400, contentType: "application/json", data: '{"error":"Missing flow filename"}'
        return
    }

    getFileList()
    def validOptions = state.jsonList ?: []
    def existing = settings?.perFlowLogEnabled ?: []
    def newList = existing.findAll { it != fname && validOptions.contains(it) }

    app.updateSetting("perFlowLogEnabled", [type: "enum", value: newList])
    updated()
	flowLog(fname, "Logging has been disabled in app", "info")
    render contentType: "application/json", data: '{"result":"Logging disabled in app"}'
}

def apiForceReload() {
	flowLog(fname, "In apiForceReload", "debug")
    updated()
    render contentType: "application/json", data: '{"result":"App reloaded (updated() called)"}'
}

def recheckAllTriggers() {
    state.activeFlows?.each { fname, flowObj ->
        def dataNodes = flowObj.flow?.drawflow?.Home?.data ?: [:]
        dataNodes.each { id, node ->
            if (node.name == "eventTrigger" && node.data.deviceId == "__time__") {
                def attr         = node.data.attribute
                def expected     = node.data.value
                def comparator   = node.data.comparator
                def curValue
                if (attr == "currentTime") {
                    curValue = new Date().format("HH:mm", location.timeZone)
                } else if (attr == "dayOfWeek") {
                    curValue = new Date().format("EEEE", location.timeZone)
                } else {
                    return
                }
                if (evaluateComparator(curValue, expected, comparator)) {
                    // Fire just this node
                    evaluateNode(fname, id, [ name: attr, value: curValue ])
                }
            }
        }
    }
}

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
			if(!triggered) triggered = 0
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
	def dryRun = (json?.dryRun == "true")
	
	def scrubbedParams = params.clone()
	scrubbedParams.remove('access_token')
	def scrubbedJson = request?.JSON ? request.JSON.clone() : null
	if (scrubbedJson instanceof Map) {
		scrubbedJson.remove('access_token')
	}
	log.info "-------------------- [apiTestFlow - ${fname}] --------------------"
	log.info "[apiTestFlow] params: ${scrubbedParams} | request.JSON: ${scrubbedJson} | using: ${fname}, ${nodeId}, ${value}"

    if (!fname || !state.activeFlows[fname]) {
        render status: 404, data: '{"error":"Flow not found"}'
        return
    }
    // Don't error if nodeId is blank, but prefer to require it for accuracy
	state.testDryRun = dryRun
    // --- Build simulated event for both clickPattern AND time triggers ---
	def evt = [:]
	if (nodeId) {
		// look up this trigger node’s settings
		def node        = state.activeFlows[fname]?.flow?.drawflow?.Home?.data[nodeId]
		def patternType = node?.data?.clickPattern
		def deviceId    = node?.data?.deviceId
		def attr        = node?.data?.attribute

		if (deviceId == "__time__") {
			// --- Time triggers ---
			if (attr in ["sunrise","sunset"]) {
				// simulate a sunrise/sunset event
				evt.name            = attr
				evt.value           = attr
				evt.descriptionText = "Time event: ${attr}"
				evt.data            = attr
				evt.pattern         = attr
			}
			else if (attr == "currentTime" && (value ==~ /\d{2}:\d{2}/)) {
				// simulate currentTime compare
				evt.name            = "currentTime"
				evt.value           = value
				evt.descriptionText = "Current time is ${value}"
				evt.data            = value
				evt.pattern         = "currentTime"
			}
			else if (attr == "dayOfWeek") {
				// simulate dayOfWeek compare (value should be full weekday name)
				evt.name            = "dayOfWeek"
				evt.value           = value.capitalize()
				evt.descriptionText = "Day of week is ${evt.value}"
				evt.data            = evt.value
				evt.pattern         = "dayOfWeek"
			}
			else {
				// fallback if user enters something unexpected
				evt.name            = attr
				evt.value           = value
				evt.descriptionText = "Time event: ${value}"
				evt.data            = value
				evt.pattern         = attr
			}
		}
		else if (patternType == "taps" && value.isInteger()) {
			// --- Multi-taps ---
			int tapCount = value.toInteger()
			evt.name            = "pushed"
			evt.value           = "${tapCount}"
			evt.descriptionText = "Switch was tapped ${tapCount}x"
			evt.data            = "${tapCount}"
			evt.pattern         = "taps"
		}
		else if (patternType == "holdPerSecond" && value.isInteger()) {
			// --- Hold per second ---
			int sec = value.toInteger()
			evt.name            = "held"
			evt.value           = "held"
			evt.descriptionText = "Switch was held ${sec} sec"
			evt.data            = "${sec}"
			evt.pattern         = "holdPerSecond"
		}
		else {
			// --- Fallback for any other text/value triggers ---
			evt.name            = "pushed"
			evt.value           = value
			evt.descriptionText = "Switch event: ${value}"
			evt.data            = value
			evt.pattern         = patternType ?: value
		}
	} else {
		// legacy: no nodeId → generic test event
		evt = [ name: "apiTestFlow", value: value ]
	}

	// log exactly what’s being passed into evaluateNode()
	log.info "[apiTestFlow] evt.name:${evt.name} | evt.value:${evt.value} | evt.descriptionText:${evt.descriptionText} | evt.data:${evt.data} | evt.pattern:${evt.pattern}"

	// --- Route to the specific node or all triggers ---
	if (nodeId) {
		evaluateNode(fname, nodeId, evt)
	} else {
		state.activeFlows[fname]?.flow?.drawflow?.Home?.data?.each { id, node ->
			if (node.name == "eventTrigger") {
				evaluateNode(fname, id, evt)
			}
		}
	}
	state.testDryRun = false
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
	if(logEnable) {
		if (settings?.perFlowLogEnabled && !(settings.perFlowLogEnabled.contains(fname))) return
		def prefix = "[${fname}]"
		switch(level) {
			case "warn":  log.warn "${prefix} ${msg}"; break
			case "error": log.error "${prefix} ${msg}"; break
			case "debug": log.debug "${prefix} ${msg}"; break
			default:      log.info "${prefix} ${msg}"
		}
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

/**
 * Subscribe all eventTrigger and schedule nodes for a given flow.
 */
def subscribeToTriggers(String fname) {
    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return

    // Grab all nodes in this flow
    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]

    dataNodes.each { nodeId, node ->
        switch(node.name) {
            case "eventTrigger":
                // — Mode triggers —
                if (node.data.deviceId == "__mode__") {
                    subscribe(location, "mode") { evt ->
                        handleEvent(evt, fname)
                    }
                }
                // — Virtual Time device (sunrise/sunset + offsets + polling) —
                else if (node.data.deviceId == "__time__") {
                    def attr   = node.data.attribute       // "timeOfDay", "currentTime", or "dayOfWeek"
                    def value  = node.data.value           // for timeOfDay: "sunrise" or "sunset"
                    def offset = (node.data.offset as Integer) ?: 0

                    if (attr == "timeOfDay") {
                        def opts = offset != 0 ? [timeOffset: offset] : [:]
			            subscribe(location, value, "handleLocationTimeEvent", opts)
                    }
                    else if (attr in ["currentTime", "dayOfWeek"]) {
                        // poll once a minute for currentTime/dayOfWeek matches
                        runEvery1Minute("recheckAllTriggers")
                    }
                }
                // — Physical device triggers —
                else {
                    def devIds = (node.data.deviceIds instanceof List && node.data.deviceIds) ?
                                  node.data.deviceIds : [ node.data.deviceId ]
                    devIds.each { devId ->
                        def device = getDeviceById(devId)
                        if (device) {
                            subscribe(device, node.data.attribute, "genericDeviceHandler")
                        }
                    }
                }
                break

            case "schedule":
                // — Cron expression subscription —
                if (node.data.cron) {
                    schedule(node.data.cron) {
                        handleEvent(
                            [ name: "schedule",
                              value: node.data.cron,
                              data: node.data ],
                            fname
                        )
                    }
                }
                // — RepeatDays + HH:mm subscription —
                if (node.data.repeatDays instanceof List && node.data.time) {
                    node.data.repeatDays.each { day ->
                        def (h, m) = node.data.time.tokenize(":")
                        def dow     = day[0..2].toUpperCase()
                        def cronExp = "0 ${m} ${h} ? * ${dow}"
                        schedule(cronExp) {
                            handleEvent(
                                [ name: "schedule",
                                  value: node.data.time,
                                  data: node.data ],
                                fname
                            )
                        }
                    }
                }
                break

            default:
                // any other node types are ignored here
                break
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

def scheduleVariablePolling(String fname) {
    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return

    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]
    def hasVarTrig = dataNodes.any { id, node ->
        node?.name == "eventTrigger" && (
            node.data?.deviceId == "__variable__" ||
            (node.data?.deviceIds instanceof List && node.data.deviceIds[0] == "__variable__")
        )
    }
    if (hasVarTrig && !state._varPollScheduled) {
        state._varPollScheduled = true
        runEvery5Seconds("checkVariableTriggers")
    }
}

// ---- Variable trigger polling and tap/hold clear helpers ----
def checkVariableTriggers() {
    state.activeFlows.each { fname, flowObj ->
        def nodes = flowObj.flow.drawflow?.Home?.data.findAll { id, node ->
            node?.name == "eventTrigger" &&
            (node.data?.deviceId == "__variable__" ||
             (node.data?.deviceIds instanceof List && node.data.deviceIds[0] == "__variable__"))
        }
        if (!nodes) return

        getGlobalVars(fname)
        def globalMap = flowObj.globalVars.collectEntries { [(it.name): it.value] }

        nodes.each { id, node ->
            def varName   = node.data?.varName
            if (!varName) return

            def curValue  = globalMap[varName]
            def lastValue = flowObj.lastVarValues[varName]

            if (curValue != lastValue) {
                flowObj.lastVarValues[varName] = curValue
                evaluateNode(fname, id, [ name: varName, value: curValue ])
            }
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

			// Pull comparator, expected and actual values
			def attr       = node.data.attribute
			def comparator = node.data.comparator
			def expected   = resolveVars(fname, node.data.value)
			def actual

			if (incomingValue != null) {
				actual = incomingValue
			} else if (node.data.deviceIds instanceof List && node.data.deviceIds) {
				actual = getDeviceById(node.data.deviceIds[0])?.currentValue(attr)
			} else if (node.data.deviceId) {
				actual = getDeviceById(node.data.deviceId)?.currentValue(attr)
			}

			// Evaluate and route
			if (evaluateComparator(actual, expected, comparator)) {
				node.outputs?.output_1?.connections?.each { conn ->
					evaluateNode(fname, conn.node, evt, null, visited)
				}
			} else {
				node.outputs?.output_2?.connections?.each { conn ->
					evaluateNode(fname, conn.node, evt, null, visited)
				}
			}
			break

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
			flowLog(fname, "In evaluateNode - device (dryRun=${state.testDryRun})", "debug")
			// collect device IDs
			def devIds = []
			if (node.data.deviceIds instanceof List) devIds = node.data.deviceIds
			else if (node.data.deviceIds)          devIds = [node.data.deviceIds]
			else if (node.data.deviceId)           devIds = [node.data.deviceId]

			def cmd = resolveVars(fname, node.data.command)
			def val = resolveVars(fname, node.data.value)

			if (cmd == "toggle") {
				flowLog(fname, "In evaluateNode - toggle (dryRun=${state.testDryRun})", "debug")
				devIds.each { devId ->
					def device = getDeviceById(devId)
					if (device && device.hasCommand("on") && device.hasCommand("off")) {
						def currentVal = device.currentValue("switch")
						if (!state.testDryRun) {
							if (currentVal == "on") {
								device.off()
							} else {
								device.on()
							}
						} else {
							flowLog(fname,
								"DRY RUN: would toggle device ${devId} (current state: ${currentVal})",
								"info")
						}
					} else {
						flowLog(fname, "Device does not support on/off toggle: $devId", "warn")
					}
				}
				// continue downstream
				node.outputs?.output_1?.connections?.each { conn ->
					evaluateNode(fname, conn.node, evt, null, visited)
				}
			} else {
				devIds.each { devId ->
					def device = getDeviceById(devId)
					if (device && cmd) {
						if (!state.testDryRun) {
							if (val != null && val != "") {
								device."${cmd}"(val)
							} else {
								device."${cmd}"()
							}
						} else {
							flowLog(fname,
								"DRY RUN: would run ${cmd} on device ${devId}" +
								(val ? " with value '${val}'" : ""),
								"info")
						}
					}
				}
				// continue downstream
				node.outputs?.output_1?.connections?.each { conn ->
					evaluateNode(fname, conn.node, evt, null, visited)
				}
			}
			return

        case "notification":
			flowLog(fname, "In evaluateNode - notification (dryRun=${state.testDryRun})", "debug")
			if (!state.testDryRun) {
            	sendNotification(fname, node.data, evt)
			} else {
				flowLog(fname, "DRY RUN: would have sent notification", "info")
			}
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
		flowLog(fname, "In evaluateNode - delayMs: ${node.data.delayMs} - node: ${node.data}", "debug")
            def ms = (node.data.ms ?: 1000) as Integer
            pauseExecution(ms)
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "setVariable":
			flowLog(fname, "In evaluateNode - setVariable (dryRun=${state.testDryRun})", "debug")
			def varName = resolveVars(fname, node.data.varName)
			def varValue = resolveVars(fname, node.data.varValue)
			if (!state.testDryRun) {
				setVariable(fname, varName, varValue)
			} else {
				flowLog(fname, "DRY RUN: would have set Variable - ${varName} - ${varValue}", "info")
			}
				
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "saveDeviceState":
			flowLog(fname, "In evaluateNode - saveDeviceState (dryRun=${state.testDryRun})", "debug")
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
					if (!state.testDryRun) {
                    	flowObj.savedDeviceStates = flowObj.savedDeviceStates ?: [:]
                    	flowObj.savedDeviceStates[devId] = devState
					} else {
						flowLog(fname, "DRY RUN: would have Saved State", "info")
					}
                }
            }
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "restoreDeviceState":
			flowLog(fname, "In evaluateNode - restoreDeviceState (dryRun=${state.testDryRun})", "debug")
			if (!state.testDryRun) {
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
			} else {
				flowLog(fname, "DRY RUN: would have Restored Device State", "info")
			}
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "notMatchingVar":
			flowLog(fname, "In evaluateNode - saveDevicesToVar (append=${node.data.append}) (dryRun=${state.testDryRun})", "debug")
			if (!state.testDryRun) {
				// 1) Resolve variable name
				def varName = resolveVars(fname, node.data.varName)
				// 2) Gather device IDs
				def devIds = (node.data.deviceIds instanceof List && node.data.deviceIds) ?
							  node.data.deviceIds :
							  (node.data.deviceId ? [node.data.deviceId] : [])
				// 3) Filter devices by the “Not” condition
				def attr       = node.data.attribute
				def comparator = node.data.comparator
				def expected   = resolveVars(fname, node.data.value)
				def newLines   = []
				devIds.each { devId ->
					def device = getDeviceById(devId)
					if (device) {
						def curVal = device.currentValue(attr)
						// only include when **NOT** matching
						if (!evaluateComparator(curVal, expected, comparator)) {
							def name = device.displayName ?: device.name
							newLines << "${name}:${curVal}"
						}
					}
				}
				// 4) Merge or overwrite
				def appendMode = node.data.append as Boolean
				def existing   = flowObj.vars?.get(varName)?.toString() ?: ""
				def lines      = existing ? existing.split('\n') as List : []
				if (appendMode) {
					newLines.each { nl ->
						def id = nl.split(':')[0]
						def idx = lines.findIndexOf { it.split(':')[0] == id }
						if (idx >= 0) lines[idx] = nl else lines << nl
					}
				} else {
					lines = newLines
				}
				// 5) Save back into in-memory vars
				flowObj.vars = flowObj.vars ?: [:]
				flowObj.vars[varName] = lines.join('\n')
				flowLog(fname, "Saved ${lines.size()} entries to \${varName}", "info")
			} else {
				flowLog(fname, "DRY RUN: would have Saved Devices to Variable", "info")
			}
			// 6) Continue downstream (only “true” path used here)
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(fname, conn.node, evt, null, visited)
			}
			return

		case "doNothing":
			flowLog(fname, "In evaluateNode - doNothing (dryRun=${state.testDryRun})", "debug")
			break
		
		case "repeat":
			flowLog(fname, "In evaluateNode - repeat (dryRun=${state.testDryRun})", "debug")
			// flowObj is already declared in this scope; just initialize its repeatCounts map:
			if (!flowObj.repeatCounts) flowObj.repeatCounts = [:]

			// Pull the repeat mode ("count" or "until")
			def mode = node.data.repeatMode ?: "count"

			if (mode == "count") {
				// —— COUNT mode ——
				def repeatMax = (node.data.repeatMax ?: 1) as Integer
				def count     = (flowObj.repeatCounts[nodeId.toString()] ?: 0) as Integer

				if (count < repeatMax) {
					flowObj.repeatCounts[nodeId.toString()] = count + 1
					flowLog(fname, "Repeat node (count): iteration ${count+1} of ${repeatMax}", "info")
					// loop back
					recheckAllTriggers(fname)
				} else {
					flowLog(fname, "Repeat node (count): reached ${repeatMax}, moving on.", "info")
					flowObj.repeatCounts[nodeId.toString()] = 0
					// fire downstream once
					node.outputs?.output_1?.connections?.each { conn ->
						evaluateNode(fname, conn.node, evt, null, visited)
					}
				}
			}
			else if (mode == "until") {
				// —— UNTIL mode ——
				def devIds = []
				if (node.data.deviceIds instanceof List && node.data.deviceIds) {
					devIds = node.data.deviceIds
				} else if (node.data.deviceId) {
					devIds = [node.data.deviceId]
				}
				def attr       = node.data.attribute
				def comparator = node.data.comparator
				def expected   = resolveVars(fname, node.data.value)

				// Determine currentValue
				def currentValue = null
				if (evt?.name == attr && evt.value != null) {
					currentValue = evt.value
				} else if (devIds && devIds[0]) {
					currentValue = getDeviceById(devIds[0])?.currentValue(attr)
				}

				// Evaluate the condition
				def passes = evaluateComparator(currentValue, expected, comparator)
				if (!passes) {
					flowLog(fname, "Repeat node (until): condition not met (${attr} ${comparator} ${expected}), looping.", "info")
					recheckAllTriggers(fname)
				} else {
					flowLog(fname, "Repeat node (until): condition met, moving on.", "info")
					node.outputs?.output_1?.connections?.each { conn ->
						evaluateNode(fname, conn.node, evt, null, visited)
					}
				}
			}
			else {
				log.warn "Unknown repeatMode ‘${mode}’ in Repeat node – skipping."
			}
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
    //if(logEnable) log.debug "Saving to file - ${fName}"
	String listJson = JsonOutput.toJson(fData) as String
	uploadHubFile("${fName}",listJson.getBytes())
}

// ---- Notification support ----
def sendNotification(fname, data, evt) {
	flowLog(fname, "In sendNotification - data: ${data} - evt: ${evt}", "debug")
    def msg = data.message ?: ""
    msg = expandWildcards(fname, msg, evt)
	if(data.notificationType == "speech") {
		data.targetDeviceId.each { it ->
        	def speaker = getDeviceById(it)
			flowLog(fname, "In sendNotification - Going to Speak on - ${speaker}")
			if (speaker) speaker.speak(msg)
		}
	} else {
		data.targetDeviceId.each { it ->
        	def push = getDeviceById(it)
			flowLog(fname, "In sendNotification - Going to Push to - ${push}")
			if (push) push.deviceNotification(msg)
		}
    }
}

def expandWildcards(fname, msg, evt) {
    flowLog(fname, "In expandWildcards - msg: ${msg} - evt: ${evt}", "debug")
    // Standard event fields
    def device    = evt?.device?.displayName ?: ""
    def attribute = evt?.name ?: ""
    def value     = evt?.value ?: ""
    def nowDate   = new Date()
    def time24    = nowDate.format("HH:mm")
    def time12    = nowDate.format("h:mm a")
    def date      = nowDate.format("MM-dd-yyyy")

    // Build map of simple wildcards
    def wilds = [
        "{device}"        : device,
        "{attribute}"     : attribute,
        "{value}"         : value,
        "{time24}"        : time24,
        "{time12}"        : time12,
        "{date}"          : date,
        "{now}"           : nowDate.toString(),
        "{variableName}"  : attribute,
        "{variableValue}" : value
    ]
    // Apply all of the above
    wilds.each { k, v ->
        msg = msg.replace(k, v instanceof Closure ? v() : v)
    }

    // Handle {var:VARname} syntax (legacy and explicit variable lookup)
    msg = msg.replaceAll(/\{var:([a-zA-Z0-9_]+)\}/) { all, varName ->
        getVarValue(fname, varName)
    }

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
