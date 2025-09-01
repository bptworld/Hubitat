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
 *  1.0.0 - 08/25/25 - Initial Release
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
                paragraph "Don't forget, if you add devices to your system after selecting all here.  You'll need to come back here and add the new devices, if you want to use them in Flow Engine."
                input "masterDeviceList", "capability.*", title: "Master List of Devices Used in this App <small><abbr title='Only devices selected here can be used in Flow Engine. This can be edited at anytime.'><b>- INFO -</b></abbr></small>", required:false, multiple:true, submitOnChange:true
            }

            section(getFormat("header-green", " Flow Engine Editor Infomation")) {
                paragraph "This app is used to receive flow data from your Flow Engine Editor."
                paragraph "Copy and paste this info into the Flow Engine Editor - appId: ${state.appId} - token: ${state.token}"
                paragraph "<enter><b>Do not share your token with anyone, especially in screenshots!</b></center>"
                paragraph "<table width='100%'><tr><td align='center'><div style='font-size: 20px;font-weight: bold;'><a href='http://${location.hub.localIP}/local/flowengineeditor.html' target=_blank>Flow Engine Editor</a></div><div><small>Click to create Flows!</small></div></td></tr></table>"
                paragraph "<center>Tip: Once you open the Editor and enter in your appId/Token, go ahead and Bookmark the Editor.  This way you may never need to open this app again.  Control everything from within the Editor!</center>"
                paragraph "<hr>"
            }

            section(getFormat("header-green", " Select Flow Files to Enable")) {
                getFileList()
                input "flowFiles", "enum", title: "Choose one or more Flow JSON files to Enable (to pause a Flow, simply remove from this list)", required: false, multiple: true, options: state.jsonList, submitOnChange: true
                if (flowFiles) {
					input "showFiles", "bool", title: "Show List of Selected Flows", description: "Selected Flow List", defaultValue:false, submitOnChange:true
                    if(showFiles) {
                    	paragraph "<small><b>Flows are enabled for:</b><br>${flowFiles.join('<br>')}</small>"
					}
                }
            }

            section(getFormat("header-green", " Variables (State)")) {
				input "showVars", "bool", title: "Show List of Variables", description: "Show Variables", defaultValue:false, submitOnChange:true
				if(showVars) {
					// Source of truth
					List gvars = (atomicState.globalVars instanceof List) ? atomicState.globalVars : []
					Map  fmap  = (atomicState.flowVars   instanceof Map ) ? atomicState.flowVars   : [:]

					StringBuilder html = new StringBuilder()

					// Globals
					html << "<div style='margin:6px 0 4px 0; font-weight:600;'>Global Variables</div>"
					if (gvars && gvars.size()) {
						def sortedG = gvars.findAll{ it?.name }.sort{ (it.name ?: '').toString().toLowerCase() }
						sortedG.each { v ->
							html << "<div>${_hx(v.name)} (${_hx(v.type ?: 'String')}) = ${_hx(v.value)}</div>"
						}
					} else {
						html << "<div style='margin-left:8px;color:#999'>(no global variables)</div>"
					}

					html << "<hr style='margin:8px 0;border:0;border-top:1px solid #444;'>"

					

// Flow maps — show each flow’s list (ONLY flows that actually have vars)
Map nonEmpty = _pruneEmptyFlowKeys(fmap)
def flowKeys = nonEmpty?.keySet()?.collect{ it?.toString() }?.sort{ (it ?: '').toLowerCase() } ?: []
html << "<div style='margin:6px 0 4px 0; font-weight:600;'>Flow Variables</div>"
if (!flowKeys) {
    html << "<div style='margin-left:8px;color:#999'>(no flow variables)</div>"
} else {
    flowKeys.each { flowName ->
        List lst = (nonEmpty[flowName] instanceof List) ? (nonEmpty[flowName] as List) : []
        def showName = flowName?.toString()
        html << "<div style='margin:6px 0 2px 0; text-decoration:underline;'>${_hx(showName)}</div>"
        def sortedF = lst.findAll{ it?.name }.sort{ (it.name ?: '').toString().toLowerCase() }
        if (sortedF && sortedF.size()) {
            sortedF.each { v ->
                html << "<div style='margin-left:16px'>${_hx(v.name)} (${_hx(v.type ?: 'String')}) = ${_hx(v.value)}</div>"
            }
        } else {
            html << "<div style='margin-left:16px;color:#999'>(none)</div>"
        }
    }
}
paragraph html.toString()
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
    atomicState.globalVars = (atomicState.globalVars instanceof List) ? atomicState.globalVars : []
    atomicState.flowVars   = (atomicState.flowVars   instanceof Map ) ? atomicState.flowVars   : [:]
    initialize()
}

def updated() {
    atomicState.globalVars = (atomicState.globalVars instanceof List) ? atomicState.globalVars : []
    atomicState.flowVars   = (atomicState.flowVars   instanceof Map ) ? atomicState.flowVars   : [:]
    initialize()
}

// ─── initialize() ─────────────────────────────────────────────────────────────
private void initialize() {
    if (!state.accessToken) {
        createAccessToken()
    }
    state.appId  = app.id
    state.token  = state.accessToken

    // — clear everything once —
    unsubscribe()
    unschedule()

	// State-first initialization (no file reads)
	atomicState.globalVars  = (atomicState.globalVars  instanceof List) ? atomicState.globalVars  : []
	atomicState.flowVarsMap = (atomicState.flowVarsMap instanceof Map ) ? atomicState.flowVarsMap : [:]

    // ── INITIALIZE our per‐flow time‐job registry ───────────────────────────────
    state.timeJobs = state.timeJobs ?: [:]
    state.timeSubs = state.timeSubs ?: [:]

    // — sunrise/sunset still by subscription —
    subscribe(location, "sunrise", "handleLocationTimeEvent", [filterEvents:false])
    subscribe(location, "sunset",  "handleLocationTimeEvent", [filterEvents:false])

    // — ONE poller for all HH:mm & dayOfWeek triggers —
    schedule("0 * * ? * * *", "pollTimeTriggers")

    // — load your flows as before —
    state.activeFlows = [:]
    settings.flowFiles?.each { fname ->
        loadAndStartFlow(fname)
    }
}

// ─── clearFlowTimeTriggers() ───────────────────────────────────────────────────
private void clearFlowTimeTriggers(String fname) {
    // ensure the maps exist
    state.timeJobs = state.timeJobs ?: [:]
    state.timeSubs = state.timeSubs ?: [:]

    // 1a) unschedule just this flow’s cron jobs by jobName
    (state.timeJobs[fname] ?: []).each { jobName ->
        unschedule(jobName)
    }
    state.timeJobs.remove(fname)

    // 1b) unsubscribe just this flow’s sunrise/sunset hooks
    (state.timeSubs[fname] ?: []).each { evtName ->
        unsubscribe(location, evtName, "handleLocationTimeEvent")
    }
    state.timeSubs.remove(fname)
}

// --- REST API ENDPOINTS ---
mappings {
    path("/modes")        { action: [GET: "apiListModes"] }
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
	path("/saveVariable")    { action: [POST: "apiSaveVariable"] }
	path("/deleteVariable")  { action: [POST: "apiDeleteVariable"] }
	path("/variables") 		 { action: [GET: "apiListVariables"] }
}

// --- HANDLERS ---
/* ===== Variable persistence in state (no JSON files) ===== */
private String _bareFlow(Object flow) {
    return (flow ?: '').toString().replaceAll(/(?i)\.json$/, '')
}
private List _ensureGlobalVarsList() {
    if (!(atomicState.globalVars instanceof List)) atomicState.globalVars = []
    return (atomicState.globalVars as List)
}
private Map _ensureFlowVarsMap() {
    if (!(atomicState.flowVars instanceof Map)) atomicState.flowVars = [:]
    return (atomicState.flowVars as Map)
}
private Map _mkVar(Object n, Object t, Object v) {
    return [name:(n?:'').toString(), type:(t?:'String').toString(), value:v]
}

/** Remove any flow entries whose list is empty or has no named vars */
private Map _pruneEmptyFlowKeys(Map fmapIn) {
    Map fmap = (fmapIn instanceof Map) ? fmapIn : [:]
    Map out = [:]
    fmap.each { k, v ->
        if (v instanceof List && v.any { it?.name }) {
            out[k] = v
        }
    }
    return out
}

def notifyVarsUpdated(String scope, String flowName=null) {
	log.debug "In notifyVarsUpdated - scope: ${scope} - flowName: ${flowName}"
    try {
        String flowFile = (scope == 'global') ? null : (_bareFlow(flowName ?: scope) + '.json')
        sendLocationEvent(
            name: "feTrace",
            value: "varsUpdated",
            descriptionText: "Vars updated for " + (flowFile ?: "GLOBAL"),
            data: groovy.json.JsonOutput.toJson([type:"varsUpdated", flowFile:flowFile, ts:now()]),
            isStateChange: true
        )
    } catch (e) {
        log.warn "notifyVarsUpdated failed: $e"
    }
}

def handleLocationTimeEvent(evt) {
    state.activeFlows.each { fname, flowObj ->
        def dataNodes = flowObj.flow?.drawflow?.Home?.data ?: [:]
        def matches = dataNodes.findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == "__time__" &&
            node.data.attribute == "timeOfDay" &&
            (
                // Exactly matches event
                (node.data.comparator == "==" && node.data.value == evt.name) ||
                // Between window starts or ends on this event
                (node.data.comparator == "between" &&
                 node.data.value instanceof List &&
                 (node.data.value[0] == evt.name || node.data.value[1] == evt.name)) ||
                // Value is a single string and matches
                (node.data.value == evt.name) ||
                // Value is a list and contains event
                (node.data.value instanceof List && node.data.value.contains(evt.name))
            )
        }
        matches.each { id, node ->
            flowLog(fname, "Firing eventTrigger from location time event (${evt.name})", "debug")
            evaluateNode(fname, id, [ name: evt.name, value: evt.name ])
        }
    }
}

def apiDeleteFlow() {
    def fname = params.name
    if (!fname) {
        render status: 400, contentType: "application/json",
               data: '{"error":"Missing file name"}'
        return
    }
    if (!fname.toLowerCase().endsWith('.json')) {
        fname += '.json'
    }
    deleteHubFile(fname)
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

    getFileList()
    def validOptions = state.jsonList ?: []
    if (!validOptions.contains(fname)) {
		flowLog(fname, "Stopped - Flow file not found", "error")
        render status: 404, contentType: "application/json", data: '{"error":"Flow file not found"}'
        return
    }

    def existing = settings?.flowFiles ?: []
    def newList = (existing + fname).unique().findAll { validOptions.contains(it) }

    app.updateSetting("flowFiles", [type: "enum", value: newList])

    updated()
	flowLog(fname, "File has been selected and ENABLED in app", "info")
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
	flowLog(fname, "File has been de-selected and DISABLED in app", "info")
    render contentType: "application/json", data: '{"result":"Flow deselected in app"}'
}

def apiSelectFlowLogging() {
    def fn = request?.JSON?.flow
	def fname = fn + ".json"

    getFileList()
    def validOptions = state.jsonList ?: []
    if (!validOptions.contains(fname)) {
		flowLog(fname, "Stopped - Flow file not found", "error")
        render status: 404, contentType: "application/json", data: '{"error":"Flow file not found"}'
        return
    }

    def existing = settings?.perFlowLogEnabled ?: []
    def newList = (existing + fname).unique().findAll { validOptions.contains(it) }
    app.updateSetting("perFlowLogEnabled", [type: "enum", value: newList])
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
    flowLog("APP", "In apiForceReload", "debug")  // was: flowLog(fname, ...)
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
	flowLog(fname, "---------- In apiRunFlow ----------", "info")
    def json = request?.JSON
    def fname = json?.flow
    if (!fname || !state.activeFlows[fname]) {
        render status: 404, data: '{"error":"Flow not found"}'
        return
    }
    
    def flowObj = state.activeFlows[fname]
    def flow = flowObj.flow
    if (!flow) {
        render status: 404, data: '{"error":"Flow not loaded"}'
        return
    }
    
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
    def json   = request?.JSON
    def fname  = json?.flow
    def value  = json?.value?.toString()
    def dryRun = (json?.dryRun as Boolean) ?: false

    if (!fname || !state.activeFlows[fname]) {
        render status: 404, contentType: "application/json",
               data: JsonOutput.toJson([ error: "Flow not found" ])
        return
    }
    flowLog(fname, "----- In apiTestFlow - dryRun: ${dryRun} -----", "info")

    // Persist dry‑run flag for evaluateNode()
    state.activeFlows[fname].testDryRun = dryRun

    def flowObj   = state.activeFlows[fname]
    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]

    dataNodes.each { nodeId, node ->
        if (node.name != "eventTrigger") return

        // gather one or more deviceIds (or "__time__" or "__variable__")
        def devIds = (node.data.deviceIds instanceof List && node.data.deviceIds) ?
                         node.data.deviceIds :
                         [ node.data.deviceId ]

        devIds.each { devId ->
            if (devId == "__time__") {
                // (existing time test code)
                Date testDate = new Date()
                def parts = value.tokenize(':')
                if (parts.size() == 2) {
                    testDate.hours   = parts[0].toInteger()
                    testDate.minutes = parts[1].toInteger()
                }
                def evt = [
                    name:           node.data.attribute,     // "timeOfDay"
                    value:          node.data.value,         // e.g. [sunrise, 20:00]
                    date:           testDate,
                    descriptionText:"Simulated time trigger at ${value}"
                ]
                handleEvent(evt, fname)
            }
            else if (devId == "__variable__") {
                // New: Simulate a variable event for testing
                def evt = [
                    name: node.data.variableName ?: node.data.varName ?: "variable",
                    value: value,
                    descriptionText:"Simulated variable trigger: ${value}"
                ]
                handleEvent(evt, fname)
            }
            else {
                // (existing device test logic)
                def device = getDeviceById(devId)
                if (!device) return

                def evt = [
                    device:         device,
                    name:           node.data.attribute,
                    value:          value,
                    descriptionText:"Simulated ${node.data.attribute} → ${value}"
                ]
                handleEvent(evt, fname)
            }
        }
    }

    render contentType: "application/json",
           data: JsonOutput.toJson([
               result: "Flow ${fname} triggered${dryRun ? ' (dry run)' : ''}",
               dryRun: dryRun
           ])
}

// ---- Main Event Handler ----
def handleEvent(evt, fname) {
    flowLog(fname, "────────────────────────────────────────────────────────────────────────────", "debug")
    flowLog(fname, "In handleEvent - evt: ${evt}", "debug")

    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return

    // ── If already running, cancel it ──────────────────────────────────────
    if (flowObj.isRunning) {
        flowLog(fname, "----- Cancelling previous run; starting new -----", "warn")

        // 1) Cancel any scheduled helpers
        unschedule("clearTapTracker")
        unschedule("clearHoldTracker")

        // 2) Emit a cancel trace
        try {
            notifyFlowTrace(fname, null, "cancelled")
        } catch (ex) {
            log.warn "[${fname}] Failed to write cancel-trace: $ex"
        }

        // 3) Clear per-run trackers
        flowObj.tapTracker?.clear()
        flowObj.holdTracker?.clear()
    }

    // ── New run token (used to cancel stale delayed resumes) ───────────────
    flowObj.runId = ((flowObj.runId ?: 0L) + 1L)
    def currentRunId = flowObj.runId

    // ── Mark running & reset trackers ──────────────────────────────────────
    flowObj.isRunning   = true
    flowObj.tapTracker  = [:]
    flowObj.holdTracker = [:]
	flowObj.pending     = 0

    // ── Locate & fire trigger nodes ────────────────────────────────────────
    def triggerNodes = getTriggerNodes(fname, evt)
    triggerNodes.each { triggerId, triggerNode ->
        def pattern = triggerNode.data.clickPattern ?: "single"

        // UI-test override
        if (evt instanceof Map && evt.pattern && evt.pattern == pattern) {
            flowLog(fname, "Forcing '${pattern}' trigger (UI Test)", "debug")
            evaluateNode(fname, triggerId, evt)
            return
        }

        switch(pattern) {
            case "single":
                flowLog(fname, "Single-tap trigger", "debug")
                evaluateNode(fname, triggerId, evt)
                break
            case "double":
            case "triple":
                // your existing tap/hold pattern logic
                handleTapHoldPattern(fname, triggerId, triggerNode, evt)
                break
            default:
                flowLog(fname, "----- Trigger -  triggerId: ${triggerId} = evt: ${evt} -----", "info")
                evaluateNode(fname, triggerId, evt)
        }
    }

    // ── End-of-flow cleanup ────────────────────────────────────────────────
    try {
		if (!(flowObj.pending ?: 0)) {
			notifyFlowTrace(fname, null, "endOfFlow")
			flowObj.isRunning = false
		}
	} catch (ex) {
		log.warn "[${fname}] Failed to write end-of-flow trace: $ex"
	}
}

// ---- Node Evaluation (ALL node types, with delay logic preserved) ----
def evaluateNode(fname, nodeId, evt, incomingValue = null, Set visited = null) {
	testDryRun = state.activeFlows[fname].testDryRun
	flowLog(fname, "In evaluateNode - fname: ${fname} - nodeId: ${nodeId} - evt: ${evt} - dryRun: ${testDryRun}", "info")
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

			// ── 1) Comparator & expected value ───────────────────────────────────
			def comparator = node.data.comparator
			def expected   = resolveVars(fname, node.data.value)

			// ── 2) AND/OR logic defaulting to OR ────────────────────────────────
			def logic = (node.data.logic?.toString() ?: "or").toLowerCase()

			// ── 3) Build list of device IDs ─────────────────────────────────────
			List<String> devIds = []
			if (node.data.deviceIds instanceof List && node.data.deviceIds) {
				devIds = node.data.deviceIds.collect { it.toString() }
			} else if (node.data.deviceId) {
				devIds = [ node.data.deviceId.toString() ]
			} else {
				flowLog(fname, "eventTrigger: no deviceId(s) defined", "warn")
				return
			}

			// ── 4) Strict value & pattern matching ──────────────────────────────
			def expectedValue   = node.data.value
			def expectedPattern = node.data.clickPattern
			def eventValue      = (evt instanceof Map) ? evt.value        : evt?.value
			def eventPattern    = (evt instanceof Map) ? evt.pattern      : null

			if (expectedValue && eventValue && expectedValue.toString() != eventValue.toString()) {
				flowLog(fname, "eventTrigger did NOT match value: expected=${expectedValue}, actual=${eventValue}", "debug")
				return
			}
			if (expectedPattern && eventPattern && expectedPattern.toString() != eventPattern.toString()) {
				flowLog(fname, "eventTrigger did NOT match pattern: expected=${expectedPattern}, actual=${eventPattern}", "debug")
				return
			}

			// ── 5) DEVICE‑FILTER & AND‑MODE ONLY FOR REAL EVENTS ────────────────
			if (!(evt instanceof Map)) {
				// 5a) Figure out which device actually fired
				String incomingId = null
				if (evt.deviceId != null) {
					incomingId = evt.deviceId.toString()
				}
				// map sunrise/sunset → "__time__"
				else if (evt?.name in ["sunrise","sunset"]) {
					incomingId = "__time__"
				}

				// 5b) Drop anything not in our devIds
				if (!(incomingId in devIds)) {
					flowLog(fname, "eventTrigger: event from ${incomingId} not in ${devIds}", "debug")
					return
				}

				// 5c) If AND‑mode, require ALL devices’ currentValues to match
				if (logic in ["and","all"]) {
					boolean allMatch = devIds.every { devId ->
						def device = getDeviceById(devId)
						def actual = device ? device.currentValue(node.data.attribute) : null
						evaluateComparator(actual, expected, comparator ?: '==')
					}
					flowLog(fname, "eventTrigger AND across ${devIds} ⇒ ${allMatch}", "debug")
					if (!allMatch) {
						return
					}
				}
			}

			// ── 6) Time‑of‑day “between” filter (only if __time__ is in devIds) ─
			if ("__time__" in devIds &&
				node.data.attribute == "timeOfDay" &&
				comparator?.toLowerCase() == "between") {

				Date now      = (evt.date as Date) ?: new Date()
				int actualMin = now.hours * 60 + now.minutes

				List bounds = (node.data.value instanceof List)
					? node.data.value
					: [ node.data.value.toString() ]

				if (bounds.size() == 2) {
					int lowMin  = toTimeMinutes(bounds[0].toString())
					int highMin = toTimeMinutes(bounds[1].toString())
					flowLog(fname, "timeOfDay between → actual:${actualMin}, low:${lowMin}, high:${highMin}", "debug")
					if (!(actualMin >= lowMin && actualMin <= highMin)) {
						flowLog(fname, "timeOfDay outside range, skipping", "debug")
						return
					}
				} else {
					log.warn "timeOfDay 'between' needs two values, got ${bounds}"
					return
				}
			}

			// ── 7) All checks passed—fire downstream ──────────────────────────────
			node.outputs?.each { outName, outObj ->
				outObj.connections?.each { conn ->
					evaluateNode(fname, conn.node, evt, null, visited)
				}
			}
			break
		
        case "schedule":
            // Pass-through trigger for Schedule node – just continue downstream
            flowLog(fname, "In evaluateNode - schedule (cron=" + (node?.data?.cron ?: node?.data?.scheduleSpec?.cronText) + ")", "debug")
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

		case "condition":
			flowLog(fname, "In evaluateNode - condition", "debug")
			try {
				def attr       = node.data.attribute
				def comparator = (node.data.comparator ?: '').toString().toLowerCase()
				def expected   = resolveVars(fname, node.data.value)
				def logic      = (node.data.logic?.toString() ?: "or").toLowerCase()
				boolean passes = false

				// 1) TIME-RANGE (“between”) always uses real clock
				if ((attr in ["currentTime","timeOfDay"]) && comparator == "between") {
					Date now = new Date()
					int actualMin = now.hours * 60 + now.minutes
					List bounds = (node.data.value instanceof List) ? node.data.value : [ node.data.value?.toString() ]
					if (bounds.size() == 2) {
						int lowMin  = toTimeMinutes(bounds[0].toString())
						int highMin = toTimeMinutes(bounds[1].toString())
						passes = (actualMin >= lowMin && actualMin <= highMin)
					} else {
						log.warn "Condition 'between' on ${attr} needs two values, got ${bounds}"
						passes = false
					}
				}
				// 2) incomingValue override
				else if (incomingValue != null) {
					passes = evaluateComparator(incomingValue, expected, comparator)
				}
				// 3) Device OR Variable evaluation
				else {
					// Collect device ids if present
					List devIds = []
					if (node.data.deviceIds instanceof List && node.data.deviceIds) {
						devIds = node.data.deviceIds
					} else if (node.data.deviceId) {
						devIds = [ node.data.deviceId ]
					}

					// ── VARIABLE branch: no devices (or explicit __variable__)
					if (!devIds || devIds.isEmpty() || devIds.contains("__variable__")) {
						String varName = (node.data.variableName ?: node.data.varName ?: attr)?.toString()
						flowObj = state.activeFlows[fname]

						// Pull from varCtx first (resolved/typed), then raw lists
						def actual =
							flowObj?.varCtx?.get(varName) ?:
							(flowObj?.flowVars?.find { it?.name == varName }?.with { resolveVarValue(fname, it) }) ?:
							(flowObj?.globalVars?.find { it?.name == varName }?.with { resolveVarValue(fname, it) })

						passes = evaluateComparator(actual, expected, comparator ?: '==')
						flowLog(fname, "Condition(variable): ${varName}=${actual} ${comparator ?: '=='} ${expected} ⇒ ${passes}", "debug")
					}
					// ── DEVICE branch: same logic you had before
					else if (logic in ["and","all"]) {
						passes = devIds.every { devId ->
							def device = getDeviceById(devId)
							def actual = device ? device.currentValue(attr) : null
							evaluateComparator(actual, expected, comparator ?: '==')
						}
					} else {
						passes = devIds.any { devId ->
							def device = getDeviceById(devId)
							def actual = device ? device.currentValue(attr) : null
							evaluateComparator(actual, expected, comparator ?: '==')
						}
					}
				}

				// 4) Route based on result
				if (passes) {
					node.outputs?.output_1?.connections?.each { conn ->
						evaluateNode(fname, conn.node, evt, null, visited)
					}
				} else {
					node.outputs?.output_2?.connections?.each { conn ->
						evaluateNode(fname, conn.node, evt, null, visited)
					}
				}
			} catch (e) {
				log.error getExceptionMessageWithLine(e)
			}
			break
		
        case "device":
			flowLog(fname, "In evaluateNode - device (dryRun=${testDryRun})", "debug")
			// collect device IDs
			def devIds = []
			if (node.data.deviceIds instanceof List) devIds = node.data.deviceIds
			else if (node.data.deviceIds)          devIds = [node.data.deviceIds]
			else if (node.data.deviceId)           devIds = [node.data.deviceId]

			def cmd = resolveVars(fname, node.data.command)
			def val = resolveVars(fname, node.data.value)

			if (cmd == "toggle") {
				flowLog(fname, "In evaluateNode - toggle (dryRun=${testDryRun})", "debug")
				devIds.each { devId ->
					def device = getDeviceById(devId)
					if (device && device.hasCommand("on") && device.hasCommand("off")) {
						def currentVal = device.currentValue("switch")
						if (currentVal == "on") {
							device.off()
						} else {
							device.on()
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
						if (val != null && val != "") {
							if(testDryRun) {
								flowLog(fname, "Dry Run: device: ${device} - cmd: ${cmd} - val: {val}", "debug")
							} else {
								if (cmd == "setLevel" || cmd == "setColorTemperature") {
									device."${cmd}"(val.toInteger())
								} else {
									device."${cmd}"(val)
								}
							}
						} else {
							if(testDryRun) {
								flowLog(fname, "Dry Run: device: ${device} - cmd: ${cmd}", "debug")
							} else {
								device."${cmd}"()
							}
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
			flowLog(fname, "In evaluateNode - notification (dryRun=${testDryRun})", "debug")
			if(testDryRun) {
				flowLog(fname, "Dry Run: fname: ${fname} - node.data: ${node.data} - evt: {evt}", "debug")
			} else {
				sendNotification(fname, node.data, evt)
			}
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
			}
            break

        case "delayMin": 
			Integer min = (node.data.delayMin ?: node.data.min ?: 1) as Integer
			Long ms = min * 60000L
			flowLog(fname, "In evaluateNode - delayMin (${min} min → ${ms} ms)", "debug")

			node.outputs?.output_1?.connections?.each { conn ->
				// track a pending continuation
				state.activeFlows[fname].pending = (state.activeFlows[fname].pending ?: 0) + 1

				runInMillis(
					ms,
					"resumeAfterDelay",
					[ data: [
						fname: fname,
						nextId: conn.node,
						evt: _packEvt(evt),
						runId: (state.activeFlows[fname]?.runId ?: 0L)
					], overwrite: false ]
				)
			}
			break

		case "delay": 
			Integer ms = (node.data.delayMs ?: node.data.ms ?: 1000) as Integer
			flowLog(fname, "In evaluateNode - delay (ms=${ms})", "debug")

			node.outputs?.output_1?.connections?.each { conn ->
				state.activeFlows[fname].pending = (state.activeFlows[fname].pending ?: 0) + 1

				runInMillis(
					ms as Long,
					"resumeAfterDelay",
					[ data: [
						fname: fname,
						nextId: conn.node,
						evt: _packEvt(evt),
						runId: (state.activeFlows[fname]?.runId ?: 0L)
					], overwrite: false ]
				)
			}
			break

		case "setVariable":
			flowLog(fname, "In evaluateNode - setVariable (dryRun=${testDryRun})", "debug")

			String varName  = resolveVars(fname, node.data.varName)
			def    varValue = resolveVars(fname, node.data.varValue)
			String varType  = (node.data?.varType ?: "String").toString()

			// Decide scope smartly (explicit → existing vars → default Flow)
			def target = _resolveVarScopeAndKey(fname, node.data as Map, varName)  // [scope, key]
			String scope = target.scope
			String key   = target.key   // bare flow name for flow scope; null for global

			if (testDryRun) {
				flowLog(fname, "Dry Run: scope=${scope} name=${varName} value=${varValue}", "debug")
			} else {
				_saveVariableInternal(fname, scope, varName, varType, varValue)
			}

			// continue downstream
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(fname, conn.node, evt, null, visited)
			}
			break

        case "saveDeviceState":
			flowLog(fname, "In evaluateNode - saveDeviceState (dryRun=${testDryRun})", "debug")
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
					if(testDryRun) {
						flowLog(fname, "Dry Run: device: ${device} - devState: ${devState}", "debug")
					} else {
						flowObj.savedDeviceStates = flowObj.savedDeviceStates ?: [:]
						flowObj.savedDeviceStates[devId] = devState
					}
                }
            }
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "restoreDeviceState":
			flowLog(fname, "In evaluateNode - restoreDeviceState (dryRun=${testDryRun})", "debug")
			def devId = node.data.deviceId
			if (devId) {
				def device = getDeviceById(devId)
				def devState = flowObj.savedDeviceStates?.get(devId)
				if(testDryRun) {
					flowLog(fname, "Dry Run: device: ${device} - devState: ${devState}", "debug")
				} else {
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
			}
            node.outputs?.output_1?.connections?.each { conn ->
                evaluateNode(fname, conn.node, evt, null, visited)
            }
            break

        case "notMatchingVar":
			flowLog(fname, "In evaluateNode - saveDevicesToVar (append=${node.data.append})", "debug")
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
			// 6) Continue downstream (only “true” path used here)
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(fname, conn.node, evt, null, visited)
			}
			return

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
		
		case "doNothing":
			flowLog(fname, "In evaluateNode - doNothing", "debug")
			break
		
		case "repeat":
			flowLog(fname, "In evaluateNode - repeat", "debug")
			if (!flowObj.repeatCounts) flowObj.repeatCounts = [:]

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
					node.outputs?.output_1?.connections?.each { conn ->
						evaluateNode(fname, conn.node, evt, null, visited)
					}
				}
			}
			else if (mode == "until") {
				// —— UNTIL mode ——
				// device ids (may be empty or "__variable__")
				List devIds = []
				if (node.data.deviceIds instanceof List && node.data.deviceIds) {
					devIds = node.data.deviceIds
				} else if (node.data.deviceId) {
					devIds = [ node.data.deviceId ]
				}

				def attr       = node.data.attribute
				def comparator = node.data.comparator
				def expected   = resolveVars(fname, node.data.value)

				// Determine currentValue from: variable OR device OR event snapshot
				def currentValue = null
				boolean variableMode = (!devIds || devIds.isEmpty() || devIds.contains("__variable__"))

				if (variableMode) {
					String varName = (node.data.variableName ?: node.data.varName ?: attr)?.toString()
					currentValue = _getVarActual(fname, varName)
					flowLog(fname, "Repeat(until) VAR: ${varName} → ${currentValue}", "debug")
				} else if (evt?.name == attr && evt.value != null) {
					currentValue = evt.value
				} else if (devIds && devIds[0]) {
					currentValue = getDeviceById(devIds[0])?.currentValue(attr)
				}

				def passes = evaluateComparator(currentValue, expected, comparator)
				if (!passes) {
					flowLog(fname, "Repeat node (until): condition not met (${attr ?: 'var'} ${comparator} ${expected}), looping.", "info")
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
    if (!name) { render status:400, text:"Missing file name"; return }
    def lname = name?.toString()?.toLowerCase()

    if (lname == "fe_global_vars.json") {
        render contentType:"application/json",
               data: groovy.json.JsonOutput.toJson(_ensureGlobalVarsList() ?: [])
        return
    }
    if (lname == "fe_flow_vars.json") {
        Map fmap = _ensureFlowVarsMap()
        Map out = [:]
        fmap.each { k, arr ->
            String bare = _bareFlow(k)
            List lst = (arr instanceof List) ? (arr as List) : []
            out[bare] = lst.findAll{ it?.name }.collect { v ->
                [ name: v.name, type: (v.type ?: "String"), value: v.value ]
            }
        }
        render contentType:"application/json", data: groovy.json.JsonOutput.toJson(out)
        return
    }

    // ... (keep your existing file-serving logic for real flow JSONs)
}

def exportModesToFile() {
    def currentMode = [ id: "current", name: location.mode ]
    def modeList = location.modes.collect { [ id: it.id, name: it.name ] } + currentMode
    def json = groovy.json.JsonOutput.toJson(modeList)
    uploadHubFile("FE_flowModes.json", json.getBytes())
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
    loadVariables(fname)
    subscribeToTriggers(fname)
	scheduleTimeBasedTriggers(fname)
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
	state.jsonList = state.jsonList.sort { it?.toLowerCase() }
}

def readFlowFile(fname) {
    def uri     = "http://127.0.0.1:8080/local/${fname}"
    def jsonStr = ""
    try {
        httpGet([uri: uri, contentType: "text/plain"]) { resp ->
            jsonStr = resp.data?.text
        }
        if (!jsonStr) return null

        // 1) Try normal parse
        try {
            return new JsonSlurper().parseText(jsonStr)
        }
        catch (parseEx) {
            // 2) Log the error, then attempt auto‑fix
            flowLog(fname, "JSON parse error: ${parseEx.message}. Attempting auto‑fix…", "warn")

            def fixed = jsonStr
                .replaceAll(/^\uFEFF/, "")                    // strip BOM
                .replaceAll(/\/\/.*$/, "")                    // remove single‑line comments
                .replaceAll(/\/\*[\s\S]*?\*\//, "")           // remove multi‑line comments
                .replaceAll(/,\s*([}\]])/, '$1')              // remove trailing commas
                .replaceAll(/(['"])?([A-Za-z_][\w]*)\1\s*:/, '"$2":') // quote unquoted keys
                .replaceAll(/'([^']*)'/, '"$1"')              // convert single → double quotes
                .replaceAll(/\bNaN\b/, "null")                // NaN → null
                .replaceAll(/\bInfinity\b/, "null")           // Infinity → null

            // 3) Re‑parse the fixed text
            def obj = new JsonSlurper().parseText(fixed)
            flowLog(fname, "Auto‑fixed JSON on load for \"${fname}\"", "info")
            return obj
        }
    }
    catch (e) {
        flowLog(fname, "readFlowFile(${fname}) error: ${e}", "error")
        return null
    }
}

def getDeviceById(id) {
    return settings.masterDeviceList?.find { it.id.toString() == id?.toString() }
}

def apiListVariables() {
    render contentType:"application/json",
           data: JsonOutput.toJson([globals: _ensureGlobalVarsList(), flows: _ensureFlowVarsMap()])
}

def apiSaveVariable() {
    def json  = request?.JSON ?: params
    String scopeRaw = (json?.scope ?: 'global').toString()
    String scope    = scopeRaw?.toLowerCase()
    String name     = (json?.name  ?: '').toString()
    String type     = (json?.type  ?: 'String').toString()
    def    value    = json?.value

    if (!name) { render status:400, contentType:"application/json", data:'{"error":"Missing variable name"}'; return }

    // GLOBAL
    if (scope in ['global','globals']) {
        List gvars = _ensureGlobalVarsList()
        def existing = gvars.find { (it?.name?.toString() ?: '') == name }
        if (existing) { existing.type = type; existing.value = value }
        else          { gvars << _mkVar(name, type, value) }
        atomicState.globalVars = gvars.collect { it }   // assign new List for persistence edge cases
        _refreshVarCaches(null)
        try { notifyVarsUpdated('global') } catch (e) {}
        render contentType:"application/json",
               data: groovy.json.JsonOutput.toJson([result:"Variable saved", name:name, type:type, value:value, scope:"global"])
        return
    }

    // FLOW (explicit `scope: flow` + flow param OR legacy `scope: <flowName>`)
    String flowParam = (json?.flow ?: json?.flowName ?: json?.flow_file ?: '').toString()
    String key = (scope == 'flow') ? _bareFlow(flowParam) : _bareFlow(scopeRaw)
    if (!key) {
        render status:400, contentType:"application/json",
               data: groovy.json.JsonOutput.toJson([error:"Missing flow name for flow-scoped variable", scope: scopeRaw, flow: flowParam])
        return
    }

    Map fmap  = _ensureFlowVarsMap()
    List list = (fmap[key] instanceof List) ? (fmap[key] as List) : []
    def existing = list.find { (it?.name?.toString() ?: '') == name }
    if (existing) { existing.type = type; existing.value = value }
    else          { list << _mkVar(name, type, value) }
    fmap[key] = list
    atomicState.flowVars = fmap.collectEntries { k,v -> [(k): v] } // assign new Map for persistence

    _refreshVarCaches(key)
    try { notifyVarsUpdated('flow', key) } catch (e) {}
    render contentType:"application/json",
           data: groovy.json.JsonOutput.toJson([result:"Variable saved", name:name, type:type, value:value, scope:"flow", flow:key])
}

def apiDeleteVariable() {
    def body = request.JSON ?: params
    String scopeRaw = (body.scope ?: '').toString()
    String scope    = scopeRaw?.toLowerCase()
    String name     = (body.name  ?: '').toString()
    if (!scope || !name) { render status:400, text:"name/scope required"; return }

    if (scope in ['global','globals']) {
        List g = _ensureGlobalVarsList()
        g.removeAll { (it?.name?.toString() ?: '') == name }
        atomicState.globalVars = g.collect { it }
        _refreshVarCaches(null)
        try { notifyVarsUpdated('global') } catch (e) {}
        render contentType:"application/json", data: groovy.json.JsonOutput.toJson([ok:true, scope:'global'])
        return
    }

    if (scope == 'flow') {
        String bare = _bareFlow(body.flow ?: '')
        Map fmap = _ensureFlowVarsMap()
        List arr = (fmap[bare] instanceof List) ? (fmap[bare] as List) : []
        arr.removeAll { (it?.name?.toString() ?: '') == name }
        if (arr && arr.size()) fmap[bare] = arr else fmap.remove(bare)
        atomicState.flowVars = fmap.collectEntries { k,v -> [(k): v] }
        _refreshVarCaches(bare)
        try { notifyVarsUpdated('flow', bare) } catch (e) {}
        render contentType:"application/json", data: groovy.json.JsonOutput.toJson([ok:true, scope:'flow', flow:bare])
        return
    }

    render status:400, text:"invalid scope"
}

def loadVariables(fname) {
    def flowObj = state.activeFlows[fname]
    if (!flowObj) return
    flowObj.varCtx = [:]

    // 1) globals
    List globalVars = []
    try { globalVars = (_ensureGlobalVarsList() ?: []).findAll { it?.name } }
    catch (e) { flowLog(fname, "Could not load globals from atomicState: $e", "warn") }

    // 2) flow vars for this flow
    List flowVarsList = []
    try {
        String bareName = _bareFlow(fname)
        Map fmap = _ensureFlowVarsMap()
        List arr = (fmap[bareName] instanceof List) ? (fmap[bareName] as List) : []
        flowVarsList = (arr ?: []).findAll { it?.name }
    } catch (e) {
        flowLog(fname, "Could not load flow vars from atomicState: $e", "warn")
    }
    flowObj.flowVars = flowVarsList

    // 3) merge (flow overrides globals by name)
    List mergedVars = []
    Set gnames = (globalVars.collect { it?.name }.findAll { it }) as Set
    globalVars.each { g -> mergedVars << (flowVarsList.find { it?.name == g?.name } ?: g) }
    flowVarsList.each { fv -> if (fv?.name && !gnames.contains(fv.name)) mergedVars << fv }

    // 4) expose for compatibility and 5) build varCtx
    flowObj.globalVars = mergedVars
    mergedVars.each { v -> if (v?.name) flowObj.varCtx[v.name] = resolveVarValue(fname, v) }
}

private void _refreshVarCaches(Object targetBare = null) {
    try {
        Map af = (state.activeFlows instanceof Map) ? (state.activeFlows as Map) : [:]
        String tBare = (targetBare != null) ? targetBare.toString() : null
        def keys = af?.keySet() ?: []
        for (def k : keys) {
            String fname = k?.toString()
            def fobj = af[k]
            // Only refresh real flows that have a .flow (skip FE_flowtrace.json etc.)
            if (!(fobj instanceof Map) || !fobj.containsKey('flow')) continue
            String bare  = _bareFlow(fname)
            //log.debug "In _refreshVarCaches - fname: ${fname} - bare: ${bare} - tBare: ${tBare} - targetBare: ${targetBare}"
            if (tBare == null || tBare == bare) {
                try { loadVariables(fname) } catch (Throwable ex) { log.warn "loadVariables(${fname}) failed: ${ex}" }
            }
        }
    } catch (Throwable e) {
        log.warn "_refreshVarCaches failed: ${e}"
    }
}

def getGlobalVars() {
    return atomicState.globalVars ?: []
}

def resolveVarValue(fname, v, _visited = []) {
    if (!v || !v.name) return ""
    if (_visited.contains(v.name)) return "ERR:Circular"
    _visited += v.name
    def val = v.value
    if (val instanceof String && (val.contains('$(') || val.matches('.*[+\\-*/><=()].*'))) {
        return evalExpression(fname, val, _visited)
    }
    if ("$val" ==~ /^-?\d+(\.\d+)?$/) return "$val".contains(".") ? val.toDouble() : val.toInteger()
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

def subscribeToTriggers(String fname) {
    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return

    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]
    dataNodes.each { nodeId, node ->
        if (node.name != "eventTrigger") return

        // — Mode triggers —
        if (node.data.deviceId == "__mode__") {
            subscribe(location, "mode") { evt ->
                handleEvent(evt, fname)
            }
        }
        // — Physical device triggers —
        else if (node.data.deviceId != "__time__") {
            def devIds = (node.data.deviceIds instanceof List && node.data.deviceIds) ? node.data.deviceIds : [ node.data.deviceId ]
            devIds.each { devId ->
                def device = getDeviceById(devId)
                if (device) subscribe(device, node.data.attribute, "genericDeviceHandler")
            }
        }
        // — note: __time__ handled separately below —
    }
}

def genericDeviceHandler(evt) {
    // Snapshot keys to avoid ConcurrentModificationException if handleEvent mutates state
    def keys = (state.activeFlows?.keySet() ?: []) as List
    keys.each { fname ->
        def flowObj = state.activeFlows[fname]
        if (!flowObj) return
        def triggerNodes = getTriggerNodes(fname, evt)
        if (triggerNodes && triggerNodes.size() > 0) {
            try {
                handleEvent(evt, fname)
            } catch (e) {
                log.error "[${fname}] genericDeviceHandler error: ${e}"
            }
        }
    }
}

private void scheduleTimeBasedTriggers(String fname) {
    // 1) Clear this flow’s old cron closures & subscriptions
    clearFlowTimeTriggers(fname)

    // 2) Initialize per‑flow registry for subscriptions
    state.timeSubs = state.timeSubs ?: [:]
    state.timeSubs[fname] = []

    // 3) Walk every "__time__" eventTrigger node in this flow
    def dataNodes = state.activeFlows[fname]?.flow?.drawflow?.Home?.data ?: [:]
    dataNodes.each { nodeId, node ->
        if (node.name != 'eventTrigger' || node.data.deviceId != '__time__') return

        // Only subscribe sunrise/sunset here
        if (node.data.attribute == 'timeOfDay') {
            def times = node.data.value instanceof List ? node.data.value : [ node.data.value ]
            times.each { raw ->
                String s = raw.toString()
                if (s == 'sunrise' || s == 'sunset') {
					def offset = node.data.offsetMin ?: 0
					if (offset == 0) {
						// Normal behavior: subscribe to actual event
						subscribe(
							location,
							s,
							'handleLocationTimeEvent',
							[ filterEvents: false, data: [ fname: fname, nodeId: nodeId ] ]
						)
						state.timeSubs[fname] << s
					} else {
						// Offset: schedule at offset from sunrise/sunset
						def now = new Date()
						def baseTime = getSunriseAndSunset()[s]
						if (baseTime) {
							def targetTime = new Date(baseTime.getTime() + (offset * 60 * 1000))
							schedule(targetTime, "handleOffsetSunEvent", [data: [ fname: fname, nodeId: nodeId, eventName: s, offset: offset ]])
							state.timeSubs[fname] << "${s}_offset_${offset}"
						}
					}
				}
            }
        }
    }
}

def pollTimeTriggers() {
    // current HH:mm and weekday
    def now       = new Date()
    def nowHHmm   = now.format('HH:mm', location.timeZone)
    def todayDOW  = now.format('EEEE', location.timeZone)

    state.activeFlows.each { fname, flowObj ->
        def dataNodes = flowObj.flow?.drawflow?.Home?.data ?: [:]
        dataNodes.each { nodeId, node ->
            if (node.name!='eventTrigger' || node.data.deviceId!='__time__') return

            def attr   = node.data.attribute
            def values = node.data.value instanceof List ? node.data.value : [ node.data.value ]

            switch(attr) {
                // fixed HH:mm  
                case 'timeOfDay':
                    if (values.any{ it.toString()==nowHHmm }) {
                        handleTimeTrigger([ fname:fname, nodeId:nodeId ])
                    }
                    break

                // legacy currentTime  
                case 'currentTime':
                    def txt = values[0].toString()
                    if (txt == nowHHmm) {
                        handleTimeTrigger([ fname:fname, nodeId:nodeId ])
                    }
                    break

                // day of week, e.g. "Monday"  
                case 'dayOfWeek':
                    if (values.any{ it.toString()==todayDOW }) {
                        handleTimeTrigger([ fname:fname, nodeId:nodeId ])
                    }
                    break
            }
        }
    }

    // Also run any Schedule Trigger nodes that match the current minute via cron
    state.activeFlows.each { fname, flowObj ->
        def dataNodes = flowObj.flow?.drawflow?.Home?.data ?: [:]
        dataNodes.each { nodeId, node ->
            def cronExpr = null
            try {
                // Detect schedule nodes by type/name/data
                if (node?.data?.cron) cronExpr = node.data.cron?.toString()
                if (!cronExpr) return
            } catch (ignored) { return }

            if (_cronMatchesNow(cronExpr, now)) {
                // Fire the flow from this schedule trigger
                notifyFlowTrace(fname, nodeId, "eventTrigger")
                def evt = [ name: "schedule", value: cronExpr, date: now ]
                handleEvent(evt, fname)
            }
        }
    }

}

def handleTimeTrigger(Map data) {
    String fname  = data.fname
    String nodeId = data.nodeId

    notifyFlowTrace(fname, nodeId, "eventTrigger")

    def flowObj = state.activeFlows[fname]
    if (!flowObj?.flow) return
    def node = flowObj.flow.drawflow?.Home?.data[nodeId]
    if (!node) return

    def evt = [
        name : node.data.attribute,
        value: node.data.value,
        date : new Date()
    ]
    handleEvent(evt, fname)
}

def checkVariableTriggers() {
    // Canonical source of truth
    def globalVars = atomicState.globalVars ?: []

    state.activeFlows.each { fname, flowObj ->
        def nodes = flowObj.flow.drawflow?.Home?.data.findAll { id, node ->
            node?.name == "eventTrigger" &&
            (
              node.data?.deviceId == "__variable__" ||
              (node.data?.deviceIds instanceof List && node.data.deviceIds[0] == "__variable__")
            )
        }
        if (!nodes) return

        // Map: varName -> value for quick lookups
        def globalMap = globalVars.collectEntries { [(it.name): it.value] }

        nodes.each { id, node ->
            def varName = node.data?.variableName
            if (!varName) {
                log.warn "In checkVariableTriggers - Missing varName in node ${id}: ${node.data}"
                return
            }

            def curValue   = globalMap[varName]
            def lastValue  = flowObj.lastVarValues[varName] ?: "firstrunforthisvar"
            def comparator = node.data?.comparator ?: '=='
            def expected   = node.data?.value

            flowLog(varName, "curValue: ${curValue} - comparator: ${comparator} - lastValue: ${lastValue}", "debug")

            if (evaluateComparator(curValue, expected, comparator) && curValue != lastValue) {
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

def getTriggerNodes(String fname, evt) {
    def flowObj   = state.activeFlows[fname]
    def dataNodes = flowObj.flow.drawflow?.Home?.data ?: [:]

    // Mode triggers stay the same
    if (evt.name == "mode") {
        return dataNodes.findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == "__mode__" &&
            node.data.attribute == "mode"
        }
    }

    
    // Schedule cron (Schedule node): route directly by matching cron text
    if (evt?.name == "schedule") {
        return dataNodes.findAll { id, node ->
            try {
                node?.name == "schedule" && (
                    (node?.data?.cron?.toString() == evt?.value?.toString()) ||
                    (node?.data?.scheduleSpec?.cronText?.toString() == evt?.value?.toString())
                )
            } catch (ignored) { false }
        }
    }

	// Everything else: device, time, and variable triggers
    return dataNodes.findAll { id, node ->
        if (node.name != "eventTrigger") return false

        // Gather device IDs (could be a single or many)
        def devIds = []
        if (node.data.deviceIds instanceof List && node.data.deviceIds) {
            devIds = node.data.deviceIds
        } else if (node.data.deviceId) {
            devIds = [ node.data.deviceId ]
        }

        // 1) Time-based: "__time__" triggers (attribute must match event)
        if (devIds.contains("__time__") && node.data.attribute == evt.name) {
            return true
        }

        // 2) Variable-based: "__variable__" triggers (variableName/varName/attribute must match event)
        if (devIds.contains("__variable__")) {
            def varName = node.data.variableName ?: node.data.varName ?: node.data.attribute
            if (varName == evt.name) {
                return true
            }
        }

        // 3) Real device: must have evt.device and attribute match
        if (evt.device && devIds.contains(evt.device.id.toString()) && node.data.attribute == evt.name) {
            return true
        }

        return false
    }
}

void notifyFlowTrace(String flowFile, def nodeId, String nodeType) {
    if (!flowFile) return
    // One live run per flowFile
    state._live = state._live ?: [:]
    def live = state._live[flowFile] ?: [runId: null, prev: null, finished: false]

    // Start a new run on first node or when file changes / ended
    if (!live.runId || nodeType == "eventTrigger" || live.finished) {
        live.runId   = "${now()}_${Math.abs(new Random().nextInt())}"
        live.prev    = null
        live.finished = false
        state._live[flowFile] = live

        sendLocationEvent(
            name: "feTrace",
            value: "start",
            descriptionText: "Live trace start for ${flowFile}",
            data: JsonOutput.toJson([type:"start", flowFile: flowFile, runId: live.runId, ts: now()])
        )
        // reset in-memory last run for this flow
        state.flowTraces = (state.flowTraces instanceof List) ? state.flowTraces : []
        state.flowTraces.removeAll { it.flowFile == flowFile }
        state.flowTraces << [runId: live.runId, flowFile: flowFile, steps: []]
    }

    // Append step to in-memory last run
    def step = [flowFile: flowFile, nodeId: nodeId, nodeType: nodeType, timestamp: now()]
    def thisFlow = state.flowTraces.find { it.flowFile == flowFile && it.runId == live.runId }
    if (thisFlow) {
        thisFlow.steps << step
        if (thisFlow.steps.size() > 200) thisFlow.steps = thisFlow.steps[-200..-1]
    }

    // Push live step (prev -> nodeId)
    sendLocationEvent(
        name: "feTrace",
        value: "step",
        descriptionText: "Live trace step for ${flowFile}",
        data: JsonOutput.toJson([
            type: "step", flowFile: flowFile, runId: live.runId,
            nodeId: nodeId, prevNodeId: live.prev, nodeType: nodeType, ts: now()
        ])
    )
    live.prev = nodeId
    state._live[flowFile] = live

    // On end, finalize + write file once (for your Last Trace button)
    if (nodeType == "endOfFlow") {
        live.finished = true
        state._live[flowFile] = live
        saveFlow("FE_flowtrace.json", state.flowTraces)
        sendLocationEvent(
            name: "feTrace",
            value: "end",
            descriptionText: "Live trace end for ${flowFile}",
            data: JsonOutput.toJson([type:"end", flowFile: flowFile, runId: live.runId, ts: now()])
        )
    }
}

def sendNotification(fname, data, evt) {
	flowLog(fname, "In sendNotification - data: ${data} - evt: ${evt}", "debug")
    def msg = data.message ?: ""
    msg = expandWildcards(fname, msg, evt, data)
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

def expandWildcards(fname, msg, evt, nodeData = null) {
    flowLog(fname, "In expandWildcards - msg: ${msg} - evt: ${evt}", "debug")
    def nowDate = new Date()
    def flowObj = state.activeFlows[fname]

    // Support all the Editor’s actual field names
    def varName = nodeData?.msgVarName ?: nodeData?.variableName ?: nodeData?.varName ?: ""
    def varVal  = "[not found]"

    // DEBUG: See all available vars and varName
    flowLog(fname, "expandWildcards: available vars: flowObj.vars=${flowObj?.vars}, varCtx=${flowObj?.varCtx}, flowVars=${flowObj?.flowVars}, globalVars=${flowObj?.globalVars}", "debug")
    flowLog(fname, "expandWildcards: requested varName='${varName}', nodeData=${nodeData}", "debug")

    if ((nodeData?.useMsgVar || nodeData?.useVariableInMsg) && varName) {
        varVal =
            (flowObj.vars?.get(varName)?.toString()) ?:
            (flowObj.varCtx?.get(varName)?.toString()) ?:
            (flowObj.flowVars?.find{ it.name == varName }?.value?.toString()) ?:
            (flowObj.globalVars?.find{ it.name == varName }?.value?.toString()) ?:
            "[not found]"
    } else {
        varName = "[not found]"
        varVal  = "[not found]"
    }

    def wilds = [
        "{device}"        : evt?.device?.displayName ?: "",
        "{attribute}"     : evt?.name ?: "",
        "{value}"         : evt?.value ?: "",
        "{text}"          : (evt?.descriptionText ?: (evt?.stringValue ?: evt?.value ?: "")),
        "{time24}"        : nowDate.format("HH:mm"),
        "{time12}"        : nowDate.format("h:mm a"),
        "{date}"          : nowDate.format("MM-dd-yyyy"),
        "{now}"           : nowDate.toString(),
        "{variableName}"  : varName,
        "{variableValue}" : varVal
    ]
    wilds.each { k, v ->
        msg = msg.replace(k, v instanceof Closure ? v() : v)
    }

    // New wildcard forms {varName_*} and {varValue_*}
    msg = msg.replaceAll(/\{varName_([A-Za-z0-9_]+)\}/) { all, nm -> nm }
    msg = msg.replaceAll(/\{varValue_([A-Za-z0-9_]+)\}/) { all, vname ->
        def vval =
            (flowObj?.vars?.get(vname)?.toString()) ?:
            (flowObj?.varCtx?.get(vname)?.toString()) ?:
            (flowObj?.flowVars?.find{ it.name == vname }?.value?.toString()) ?:
            (flowObj?.globalVars?.find{ it.name == vname }?.value?.toString()) ?:
            "[not found]"
        vval
    }
    msg = msg.replaceAll(/\{var:([a-zA-Z0-9_]+)\}/) { all, vname ->
        def vval =
            (flowObj.vars?.get(vname)?.toString()) ?:
            (flowObj.varCtx?.get(vname)?.toString()) ?:
            (flowObj.flowVars?.find{ it.name == vname }?.value?.toString()) ?:
            (flowObj.globalVars?.find{ it.name == vname }?.value?.toString()) ?:
            "[not found]"
        vval
    }

    flowLog(fname, "In expandWildcards - msg: ${msg}", "debug")
    return msg
}

def getVarValue(fname, vname) {
	flowLog(fname, "In getVarValue - vname: ${vname}", "debug")
    def flowObj = state.activeFlows[fname]
    return flowObj.vars?.get(vname) ?: flowObj.varCtx?.get(vname) ?: ""
}

def setVariable(String fname, String varName, def varValue) {
    String name = (varName ?: "").toString().trim()
    if (!name) {
        log.warn "setVariable: blank name; ignoring"
        return
    }

    // 1) Load exactly what the UI reads (globals list), copy to a fresh list
    List current = _ensureGlobalVarsList()
    List g = (current instanceof List) ? current.collect { it } : []

    // 2) Replace existing entry (by name) or append a new one
    int idx = g.findIndexOf { (it?.name?.toString() ?: "") == name }
    if (idx >= 0) {
        def old = g[idx] ?: [:]
        g[idx] = [name: name, type: (old.type ?: "String"), value: varValue]
    } else {
        g << [name: name, type: "String", value: varValue]
    }

    // 3) Assign a NEW list into state so Hubitat persists it
    atomicState.globalVars = g.collect { it }

    // 4) Persist to disk (so /variables sees the same) and notify editor
    try { saveGlobalVarsToFile(atomicState.globalVars) } catch (e) { log.warn "saveGlobalVarsToFile failed: $e" }
    try { notifyVarsUpdated('global') } catch (e) { log.debug "notifyVarsUpdated error: $e" }
}

def normalizeFlowName(fname) {
    return fname?.replaceAll(/(?i)\.json$/, '')
}

def saveGlobalVarsToFile(globals) {
    try {
        // normalize to [{name,type,value}, ...]
        List list = (globals instanceof List) ? globals.findAll { it?.name } : []
        list = list.collect { v ->
            [
                name : (v.name ?: "").toString(),
                type : (v.type ?: "String").toString(),
                value: v.value
            ]
        }

        // single source of truth: state
        synchronized(this) {
            atomicState.globalVars = list
        }

        // optional: lightweight log + event so UI/Maker API listeners can refresh
        flowLog("globals", "Updated GLOBAL vars in state (${list.size()})", "debug")
        sendLocationEvent(
            name: "globalVarsUpdated",
            value: now(),
            descriptionText: "Flow-Engine: global vars updated (state)"
        )
    } catch (e) {
        log.warn "saveGlobalVarsToFile(state) failed: $e"
    }
}

def evaluateComparator(actual, expected, cmp) {
    // normalize operator and log each invocation
    String op = (cmp ?: '').toString().toLowerCase()

    switch(op) {
        case '==':
            return "$actual" == "$expected"
        case '!=':
            return "$actual" != "$expected"
        case '>':
            return toDouble(actual) > toDouble(expected)
        case '<':
            return toDouble(actual) < toDouble(expected)
        case '>=':
            return toDouble(actual) >= toDouble(expected)
        case '<=':
            return toDouble(actual) <= toDouble(expected)
        case 'between':
            // build a two‑element list from expected
            List bounds
            if (expected instanceof List) {
                bounds = expected
            } else {
                bounds = expected.toString()
                                 .replaceAll(/[\[\]\s]/, '')
                                 .split(',')
                                 .toList()
            }
            if (bounds.size() == 2) {
                double low  = toDouble(bounds[0])
                double high = toDouble(bounds[1])
                double val  = toDouble(actual)
                return (val >= low && val <= high)
            }
            log.warn "evaluateComparator: 'between' requires exactly 2 values, got ${bounds}"
            return false
        case 'contains':
            return "$actual".toLowerCase().contains("$expected".toLowerCase())
        case 'notcontains':
            return !"$actual".toLowerCase().contains("$expected".toLowerCase())
        case 'startswith':
            return "$actual".toLowerCase().startsWith("$expected".toLowerCase())
        case 'endswith':
            return "$actual".toLowerCase().endsWith("$expected".toLowerCase())
        case 'empty':
            return !actual
        case 'istrue':
            return actual == true || "$actual" == "true"
        case 'isfalse':
            return actual == false || "$actual" == "false"
        default:
            return "$actual" == "$expected"
    }
}

private double toDouble(x) {
    if (x instanceof Number) {
        return ((Number)x).doubleValue()
    }
    String s = x?.toString()?.trim()
    if (!s) return 0.0
    try {
        return Double.parseDouble(s)
    } catch (Exception e) {
        log.warn "toDouble: cannot convert '${s}' to a number"
        return 0.0
    }
}

private int toTimeMinutes(String value) {
    if (value == "sunrise" || value == "sunset") {
        def ss = getSunriseAndSunset()
        Date dt = (value == "sunrise") ? ss.sunrise : ss.sunset
        return dt.hours * 60 + dt.minutes
    }
    // otherwise assume “HH:mm”
    def parts = value.tokenize(':')
    return (parts[0].toInteger() * 60) + parts[1].toInteger()
}

private Integer parseIntValue(val, Integer defaultVal = 0) {
    try {
        if (val instanceof Number) return val as Integer
        if (val != null)           return val.toString().toInteger()
    } catch (e) {
        log.warn "Could not parse time value '${val}', defaulting to ${defaultVal}"
    }
    return defaultVal
}

def handleOffsetSunEvent(data) {
    def fname  = data.fname
    def nodeId = data.nodeId
    def evt = [
        name : data.eventName,
        value: data.eventName,
        date : new Date(),
        offset: data.offset
    ]
    handleEvent(evt, fname)
}

private String _hx(Object x) {
    String s = x == null ? "" : x.toString()
    s = s.replaceAll("&", "&amp;")
         .replaceAll("<", "&lt;")
         .replaceAll(">", "&gt;")
    return s
}

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

def afterFlowLoad() {
    state.activeFlows?.each { fname, fobj ->
        }
}

private List _collectCronsFromFlow(Object flowObj) {
    try {
        def nodes = (flowObj?.nodes instanceof Collection) ? flowObj.nodes : []
        def out = []
        nodes.each { n ->
            try {
                if ((n?.type ?: n?.name) == "schedule") {
                    def c = n?.data?.cron
                    if (c) out << c.toString().trim()
                }
            } catch (ignored) {}
        }
        return out.unique().sort()
    } catch (e) {
        log.warn "collectCronsFromFlow: ${e}"
        return []
    }
}

def saveFlow(String fname, Object flowObj) {
    if(!state.activeFlows) state.activeFlows = [:]
    def prevObj   = state.activeFlows[fname]
    def prevCrons = _collectCronsFromFlow(prevObj)
    def newCrons  = _collectCronsFromFlow(flowObj)

    // Persist flow JSON to Hubitat File Manager (legacy behavior)
    try {
        String listJson = groovy.json.JsonOutput.toJson(flowObj) as String
        uploadHubFile("${fname}", listJson.getBytes("UTF-8"))
    } catch (e) {
        log.warn "saveFlow: upload failed for ${fname}: ${e}"
    }

    // Update in-memory state
    state.activeFlows[fname] = flowObj

    // Rebind schedules only if the cron set changed (including transitions to/from empty)
    if (prevCrons != newCrons) {
        // If the new set is empty, this will unschedule and register nothing
	}
}

def refreshAllSchedules() {
    try {
        unschedule("pollTimeTriggers")     // clear just the minute poller if present
    } catch (ignored) {}
    schedule("0 * * ? * * *", "pollTimeTriggers")
}

def removeFlow(fname) {
    if(state.activeFlows?.containsKey(fname)) {
        state.activeFlows.remove(fname)
        // Keep the minute poller; no global unschedule here
        refreshAllSchedules()  // ensures poller continues to run
    }
}

private boolean _cronMatchesNow(String cronExpr, Date now = new Date()) {
    if (!cronExpr) return false
    def parts = cronExpr.trim().split(/\s+/)
    if (parts.size() == 6) {
        // Quartz style without year: [sec min hour dom mon dow] -> drop seconds
        parts = parts[1..5]
    } else if (parts.size() != 5) {
        // Try to normalize 5, otherwise bail
        return false
    }
    def tz = location?.timeZone ?: TimeZone.getDefault()
    int m   = now.format('m', tz) as int
    int h   = now.format('H', tz) as int
    int dom = now.format('d', tz) as int
    int mon = now.format('M', tz) as int
    int dow = (now.format('u', tz) as int) % 7  // 0=Sunday per cron tradition (convert from ISO 1..7)
    def fields = [parts[0], parts[1], parts[2], parts[3], parts[4]]
    def nowVals = [m, h, dom, mon, dow]

    for (int i=0; i<5; i++) {
        if (!_cronFieldMatches(fields[i], nowVals[i], i)) return false
    }
    return true
}

private boolean _cronFieldMatches(String field, int value, int idx) {
    // idx: 0=min(0-59), 1=hour(0-23), 2=dom(1-31), 3=mon(1-12), 4=dow(0-6 Sun=0)
    field = field?.trim()
    if (!field) return false
    if (field == "*") return true

    boolean ok = false
    field.split(",").each { token ->
        token = token.trim()
        if (!token) return

        String base = token
        int step = 1
        if (token.contains("/")) {
            def sp = token.split("/")
            base = sp[0]
            step = (sp[1] as int)
            if (step <= 0) step = 1
        }

        // Determine min/max per field
        int minV = (idx==0)?0:(idx==1)?0:(idx==2)?1:(idx==3)?1:0
        int maxV = (idx==0)?59:(idx==1)?23:(idx==2)?31:(idx==3)?12:6

        if (base == "*" || base == "?") {
            if ((value - minV) % step == 0) { ok = true }
        } else if (base.contains("-")) {
            def rng = base.split("-")
            int a = (rng[0] as int), b = (rng[1] as int)
            if (value >= a && value <= b && ((value - a) % step == 0)) { ok = true }
        } else {
            int v = (base as int)
            if (v == value || (step>1 && value>=v && ((value - v) % step == 0))) { ok = true }
        }
    }
    return ok
}

private List _computeHubModes() {
    try {
        def lst = (location?.modes ?: []).collect { [ id: it?.id as Long, name: it?.name?.toString() ] }
        return lst ?: []
    } catch (e) {
        log.warn "computeHubModes failed: $e"
        return []
    }
}

def handleModeChange(evt) {
    try {
        atomicState.hubModes = _computeHubModes()
    } catch (e) {
        log.warn "handleModeChange failed: $e"
    }
}

def apiListModes() {
    try {
        // Ensure cache exists
        if (!(atomicState.hubModes instanceof List)) {
            atomicState.hubModes = _computeHubModes()
        }
        def cur = location?.mode?.toString()
        render contentType: "application/json", data: groovy.json.JsonOutput.toJson([
            modes: (atomicState.hubModes ?: []),
            currentMode: cur
        ])
    } catch (e) {
        render status: 500, contentType: "application/json",
               data: groovy.json.JsonOutput.toJson([ error: "apiListModes failed", message: e?.toString() ])
    }
}

private Map _packEvt(evt) {
    if (evt instanceof Map) return evt
    try {
        return [
            name     : evt?.name,
            value    : evt?.value,
            deviceId : evt?.deviceId?.toString(),
            date     : (evt?.date ? evt.date.getTime() : null),
            descriptionText: evt?.descriptionText
        ]
    } catch (ignored) {
        return [ name: evt?.name, value: evt?.value ]
    }
}

private Map _unpackEvt(Map m) {
    if (!m) return [:]
    if (m.date) { try { m.date = new Date(m.date as Long) } catch (ignored) {} }
    return m
}

def resumeAfterDelay(Map data) {
    String fname = data.fname
    def flowObj = state.activeFlows[fname]
    if (!flowObj) return

    if ((flowObj.runId ?: -1L) != (data.runId ?: -2L)) {
        flowLog(fname, "Stale delay resume ignored (runId=${data.runId}, current=${flowObj.runId})", "debug")
        return
    }

    def evt  = _unpackEvt(data.evt as Map)
    def next = (data.nextId ?: "").toString()

    def freshVisited = new HashSet()
    evaluateNode(fname, next, evt, null, freshVisited)

    // this resume finished; update counter
    flowObj.pending = Math.max((flowObj.pending ?: 0) - 1, 0)

    // if nothing pending, finalize the trace/run now
    if (flowObj.pending == 0) {
        try { notifyFlowTrace(fname, null, "endOfFlow") } catch (_){}
        flowObj.isRunning = false
    }
}

private void _saveVariableInternal(String fname, String scope, String name, String type, def value) {
    if (!name) return
    scope = (scope ?: "flow").toLowerCase()
    type  = (type  ?: "String").toString()

    if (scope in ["global","globals"]) {
        // Update GLOBAL list
        List gvars = _ensureGlobalVarsList()
        def existing = gvars.find { (it?.name?.toString() ?: "") == name }
        if (existing) { existing.type = type; existing.value = value }
        else          { gvars << _mkVar(name, type, value) }
        // assign fresh list to ensure persistence
        atomicState.globalVars = gvars.collect { it }
        try {
            _refreshVarCaches(null)
            notifyVarsUpdated("global", null)
        } catch (Throwable ignore) {}
        return
    }

    // FLOW scope (default)
    String bare = _bareFlow(fname)  // runtime flow name
    Map fmap    = _ensureFlowVarsMap()
    List list   = (fmap[bare] instanceof List) ? (fmap[bare] as List) : []

    def existing = list.find { (it?.name?.toString() ?: "") == name }
    if (existing) { existing.type = type; existing.value = value }
    else          { list << _mkVar(name, type, value) }

    fmap[bare] = list
    // assign fresh map to ensure persistence
    atomicState.flowVars = fmap.collectEntries { k, v -> [(k): v] }

    try {
        _refreshVarCaches(bare)
        notifyVarsUpdated("flow", bare)
    } catch (Throwable ignore) {}
}

private Map _resolveVarScopeAndKey(String fname, Map nodeData, String varName) {
    String explicit = ((nodeData?.varScope ?: nodeData?.scope) ?: "").toString().toLowerCase()
    if (explicit in ["flow","global","globals"]) {
        return [scope: (explicit == "global" || explicit == "globals") ? "global" : "flow",
                key:   (explicit == "flow") ? _bareFlow(fname) : null]
    }
    // boolean hints some editor builds use
    if (nodeData?.isGlobal == true || nodeData?.useGlobal == true) {
        return [scope: "global", key: null]
    }

    // Look at what actually exists right now
    String bare = _bareFlow(fname)
    Map fmap    = _ensureFlowVarsMap()
    List flowL  = (fmap[bare] instanceof List) ? (fmap[bare] as List) : []
    List gvars  = _ensureGlobalVarsList()

    boolean flowHas   = flowL?.any { (it?.name?.toString() ?: "") == varName }
    boolean globalHas = gvars?.any { (it?.name?.toString() ?: "") == varName }

    if (flowHas)   return [scope: "flow",   key: bare]
    if (globalHas) return [scope: "global", key: null]

    // Default for brand-new names: Flow (more specific)
    return [scope: "flow", key: bare]
}

// Get current value of a variable by name (prefers typed/ resolved varCtx)
private def _getVarActual(String fname, String varName) {
    def flowObj = state.activeFlows[fname]
    return flowObj?.varCtx?.get(varName) ?:
           (flowObj?.flowVars?.find { it?.name == varName }?.with { resolveVarValue(fname, it) }) ?:
           (flowObj?.globalVars?.find { it?.name == varName }?.with { resolveVarValue(fname, it) })
}
