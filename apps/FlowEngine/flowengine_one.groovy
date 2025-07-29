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
 *  1.0.0 - 07/22/25 - Initial Release
 */

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
state.globalVarsCache = state.globalVarsCache ?: []

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
	flowLog(fname, "In updated", "info")
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

	try {
    	state.globalVarsCache = readFlowFile("FE_global_vars.json") ?: []
	} catch (e) {
		state.globalVarsCache = []
	}

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
            evaluateNode(fname, id, [ name: evt.name, value: evt.value ])
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

        // gather one or more deviceIds (or "__time__")
        def devIds = (node.data.deviceIds instanceof List && node.data.deviceIds) ?
                         node.data.deviceIds :
                         [ node.data.deviceId ]

        devIds.each { devId ->
            if (devId == "__time__") {
                // ── TEST MODE: use the 'value' string (HH:mm) as the event time ──
                Date testDate = new Date()
                def parts = value.tokenize(':')
                if (parts.size() == 2) {
                    testDate.hours   = parts[0].toInteger()
                    testDate.minutes = parts[1].toInteger()
                }
                // build a fake time event that carries the node's value list
                def evt = [
                    name:           node.data.attribute,     // "timeOfDay"
                    value:          node.data.value,         // e.g. [sunrise, 20:00]
                    date:           testDate,
                    descriptionText:"Simulated time trigger at ${value}"
                ]
                handleEvent(evt, fname)
            }
            else {
                // ── DEVICE TEST: fire through the normal device path ─────────────
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
            log.warn "[${fname}] Failed to write cancel‑trace: $ex"
        }

        // 3) Clear per‑run trackers
        flowObj.tapTracker?.clear()
        flowObj.holdTracker?.clear()
    }

    // ── Mark running & reset trackers ──────────────────────────────────────
    flowObj.isRunning   = true
    flowObj.tapTracker  = [:]
    flowObj.holdTracker = [:]

    // ── Locate & fire trigger nodes ────────────────────────────────────────
    def triggerNodes = getTriggerNodes(fname, evt)
    triggerNodes.each { triggerId, triggerNode ->
        def pattern = triggerNode.data.clickPattern ?: "single"
        // UI‑test override
        if (evt instanceof Map && evt.pattern && evt.pattern == pattern) {
            flowLog(fname, "Forcing '${pattern}' trigger (UI Test)", "debug")
            evaluateNode(fname, triggerId, evt)
            return
        }
        switch(pattern) {
            case "single":
                flowLog(fname, "Single‑tap trigger", "debug")
                evaluateNode(fname, triggerId, evt)
                break
            case "double":
            case "triple":
                // … your existing tap/hold logic here …
                handleTapHoldPattern(fname, triggerId, triggerNode, evt)
                break
            default:
				flowLog(fname, "----- Trigger -  triggerId: ${triggerId} = evt: ${evt} -----", "info")
                evaluateNode(fname, triggerId, evt)
        }
    }

    // ── End‑of‑flow cleanup ────────────────────────────────────────────────
    try {
        notifyFlowTrace(fname, null, "endOfFlow")
    } catch (ex) {
        log.warn "[${fname}] Failed to write end‑of‑flow trace: $ex"
    } finally {
        flowObj.isRunning = false
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

		case "condition":
			flowLog(fname, "In evaluateNode - condition", "debug")
			try {
				def attr       = node.data.attribute
				def comparator = node.data.comparator?.toLowerCase()
				def expected   = resolveVars(fname, node.data.value)
				def logic      = (node.data.logic?.toString() ?: "or").toLowerCase()
				boolean passes = false

				// 1) TIME‑RANGE (“between”) always uses real clock
				if ((attr in ["currentTime","timeOfDay"]) && comparator == "between") {
					Date now      = new Date()
					int  actualMin = now.hours * 60 + now.minutes

					List bounds = (node.data.value instanceof List)
								  ? node.data.value
								  : [ node.data.value?.toString() ]
					if (bounds.size() == 2) {
						int lowMin  = toTimeMinutes(bounds[0].toString())
						int highMin = toTimeMinutes(bounds[1].toString())
						passes = (actualMin >= lowMin && actualMin <= highMin)
						flowLog(fname, "---------- In evaluateNode-condition1 -------start", "info")
						flowLog(fname, "In evaluateNode-condition1: actualMin: ${actualMin} | lowMin: ${lowMin} | highMin: ${highMin} | passes: ${passes}", "info")
						flowLog(fname, "---------- In evaluateNode-condition1 ---------end", "info")
					} else {
						log.warn "Condition 'between' on ${attr} needs two values, got ${bounds}"
						passes = false
					}
				}
				// 2) incomingValue override for all other comparators
				else if (incomingValue != null) {
					passes = evaluateComparator(incomingValue, expected, comparator)
					flowLog(fname, "---------- In evaluateNode-condition2 -------start", "info")
					flowLog(fname, "In evaluateNode-condition2: incomingValue: ${incomingValue} | expected: ${expected} | comparator: ${comparator} | passes: ${passes}", "info")
					flowLog(fname, "---------- In evaluateNode-condition2 ---------end", "info")
				}
				// 3) Multi‑device AND/OR logic for real attributes
				else {
					def devIds = []
					if (node.data.deviceIds instanceof List && node.data.deviceIds) {
						devIds = node.data.deviceIds
					} else if (node.data.deviceId) {
						devIds = [ node.data.deviceId ]
					}

					if (logic in ["and","all"]) {
						flowLog(fname, "---------- In evaluateNode-condition-and -------start", "info")
						passes = devIds.every { devId ->
							def device = getDeviceById(devId)
							def actual = device ? device.currentValue(attr) : null
							flowLog(fname, "In evaluateNode-condition-and: actual: ${actual} | expected: ${expected} | comparator: ${comparator}", "info")
							evaluateComparator(actual, expected, comparator ?: '==')
						}
						flowLog(fname, "In evaluateNode-condition-and: passes: ${passes}", "info")
						flowLog(fname, "---------- In evaluateNode-condition-and ---------end", "info")
					} else {
						flowLog(fname, "---------- In evaluateNode-condition-or -------start", "info")
						passes = devIds.any { devId ->
							def device = getDeviceById(devId)
							def actual = device ? device.currentValue(attr) : null
							flowLog(fname, "In evaluateNode-condition-or: actual: ${actual} | expected: ${expected} | comparator: ${comparator}", "info")
							evaluateComparator(actual, expected, comparator ?: '==')
						}
						flowLog(fname, "In evaluateNode-condition-or: passes: ${passes}", "info")
						flowLog(fname, "---------- In evaluateNode-condition-or -------end", "info")
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
			flowLog(fname, "In evaluateNode - setVariable (dryRun=${testDryRun})", "debug")
			def varName = resolveVars(fname, node.data.varName)
			def varValue = resolveVars(fname, node.data.varValue)
			if(testDryRun) {
				flowLog(fname, "Dry Run: fname: ${fname} - varName: ${varName} - varValue: {varValue}", "debug")
			} else {
				setVariable(fname, varName, varValue)
			}
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

def loadVariables(fname) {
    def flowObj = state.activeFlows[fname]
    flowObj.varCtx = [:]
    // Load global vars from file
    def globalVars = []
    try {
        globalVars = readFlowFile("FE_global_vars.json") ?: []
    } catch (e) {
        flowLog(fname, "Could not load FE_global_vars.json: $e", "warn")
    }
    // Store in flowObj for compatibility, if you want
    flowObj.globalVars = globalVars
    // Build variable context
    globalVars.each { v ->
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
    state.activeFlows.each { fname, flowObj ->
        def triggerNodes = getTriggerNodes(fname, evt)
        if (triggerNodes && triggerNodes.size() > 0) {
            handleEvent(evt, fname)
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
                    subscribe(
                        location,
                        s,
                        'handleLocationTimeEvent',
                        [ filterEvents: false, data: [ fname: fname, nodeId: nodeId ] ]
                    )
                    state.timeSubs[fname] << s
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

// ---- Variable trigger polling and tap/hold clear helpers ----
def checkVariableTriggers() {
    def globalVars = state.globalVarsCache ?: []
    state.activeFlows.each { fname, flowObj ->
        def nodes = flowObj.flow.drawflow?.Home?.data.findAll { id, node ->
            node?.name == "eventTrigger" &&
            (node.data?.deviceId == "__variable__" ||
             (node.data?.deviceIds instanceof List && node.data.deviceIds[0] == "__variable__"))
        }
		if (!nodes) return
        def globalMap = globalVars.collectEntries { [(it.name): it.value] }

        nodes.each { id, node ->
            def varName   = node.data?.variableName
			if (!varName) {
				log.warn "In checkVariableTriggers - Missing varName in node ${id}: ${node.data}"
				return
			}

            def curValue  = globalMap[varName]
            def lastValue = flowObj.lastVarValues[varName] ?: "firstrunforthisvar"
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

    // Everything else: device AND __time__ triggers
    return dataNodes.findAll { id, node ->
        if (node.name != "eventTrigger") return false

        // pull out your list of device IDs (could be a single or many)
        def devIds = []
        if (node.data.deviceIds instanceof List && node.data.deviceIds) {
            devIds = node.data.deviceIds
        } else if (node.data.deviceId) {
            devIds = [ node.data.deviceId ]
        }

        // 1) time‐based test: if this node is a "__time__" trigger
        if (devIds.contains("__time__") && node.data.attribute == evt.name) {
            return true
        }

        // 2) real device test: only if evt.device is non‐null
        if (evt.device && devIds.contains(evt.device.id.toString()) && node.data.attribute == evt.name) {
            return true
        }

        return false
    }
}

void notifyFlowTrace(flowFile, nodeId, nodeType) {
    if (!flowFile) return

    // initialize list of runs
    if (!state.flowTraces) state.flowTraces = []

    // get or create a runId for this invocation
    def runId = state.lastRunId
    if (nodeType == "eventTrigger" || !runId || state.lastFlowFile != flowFile) {
        // new run for this flowFile
        runId             = "${now()}_${new Random().nextInt(1000000)}"
        state.lastRunId   = runId
        state.lastFlowFile = flowFile

        // ── KEEP only one run per flowFile: remove any existing trace for this flowFile
        state.flowTraces.removeAll { it.flowFile == flowFile }

        // ── start fresh steps for this run
        state.flowTraces << [
            runId:    runId,
            flowFile: flowFile,
            steps:    []
        ]
    }

    // locate our run object
    def thisFlow = state.flowTraces.find { it.runId == runId }
    if (!thisFlow) {
        thisFlow = [
            runId:    runId,
            flowFile: flowFile,
            steps:    []
        ]
        state.flowTraces << thisFlow
    }

    // append this step
    def trace = [
        flowFile:  flowFile,
        nodeId:    nodeId,
        nodeType:  nodeType,
        timestamp: new Date().time
    ]
    thisFlow.steps << trace

    // optional: cap number of steps per run
    if (thisFlow.steps.size() > 40) {
        thisFlow.steps = thisFlow.steps[-40..-1]
    }

    // write ALL last‐runs back to FE_flowtrace.json
    saveFlow("FE_flowtrace.json", state.flowTraces)

    // ── NEW: emit a Location event so the Editor will detect and poll for the updated trace
    sendLocationEvent(
        name:           "flowTraceUpdated",
        value:          new Date().time,
        descriptionText:"Flow trace updated for ${flowFile}"
    )
}

void saveFlow(fName, fData) {
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

    // Load the latest global vars from file
    def globalVars = []
    try {
        globalVars = readFlowFile("FE_global_vars.json") ?: []
        //flowLog(fname, "Loaded globalVars from file: " + globalVars*.name, "debug")
    } catch (e) {
        flowLog(fname, "Could not load global vars from file, falling back to local: $e", "warn")
    }

    flowObj.flowVars = flowObj.flowVars ?: []
    def isGlobal = globalVars.any { it.name == varName }
    flowLog(fname, "In setVariable - isGlobal: ${isGlobal}", "debug")
    def updated = false

    if (isGlobal) {
		flowLog(fname, "In setVariable - Parsing vars", "debug")
        // Update in loaded list
        globalVars.each { v ->
            if (v.name == varName) {
                v.value = varValue
                updated = true
            }
        }
        // Save the new list to file
		if (updated) {
			flowLog(fname, "In setVariable - Saving vars", "debug")
			saveGlobalVarsToFile(globalVars)
			state.globalVarsCache = globalVars
			checkVariableTriggers()
		}
    } else {
        // Update or add as local
        flowObj.flowVars.each { v ->
            if (v.name == varName) {
                v.value = varValue
                updated = true
            }
        }
        if (!updated) {
            flowObj.flowVars << [name: varName, value: varValue]
        }
    }

    // Always update context maps
    flowObj.vars = flowObj.vars ?: [:]
    flowObj.varCtx = flowObj.varCtx ?: [:]
    flowObj.vars[varName] = varValue
    flowObj.varCtx[varName] = varValue
}

def saveGlobalVarsToFile(globals) {
    flowLog(fname, "In saveGlobalVarsToFile – globals: ${globals}", "debug")

    // 1) upload the JSON to FE_global_vars.json
    def fileName   = "FE_global_vars.json"
    def jsonString = JsonOutput.toJson(globals)
    uploadHubFile(fileName, jsonString.getBytes("UTF-8"))

    // 2) fire a LOCATION event so Maker‑API will push it over WebSocket
    sendLocationEvent(
        name:           "globalVarsUpdated",
        value:          new Date().time,
        descriptionText: "Flow‑Engine: global‑vars file updated"
    )
    flowLog(fname, "Dispatched globalVarsUpdated location event", "debug")
}

// --- Comparators ---
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

// helper to coerce any value into a double
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

// ── Helper to coerce String or Number into an Integer ───────────────────
private Integer parseIntValue(val, Integer defaultVal = 0) {
    try {
        if (val instanceof Number) return val as Integer
        if (val != null)           return val.toString().toInteger()
    } catch (e) {
        log.warn "Could not parse time value '${val}', defaulting to ${defaultVal}"
    }
    return defaultVal
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
