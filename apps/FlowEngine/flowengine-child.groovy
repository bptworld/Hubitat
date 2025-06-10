definition(
    name: "Flow Engine Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Runs flows from File Manager JSON",
	parent: "BPTWorld:Flow Engine",
    category: "Automation",
    iconUrl: "",
    iconX2Url: ""
)

import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import java.text.SimpleDateFormat
state.nodeDebounce = state.nodeDebounce ?: [:]

// --- VARIABLE SUPPORT ---
state.globalVars = state.globalVars ?: []
state.varCtx = state.varCtx ?: [:]

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", install: true, uninstall: true, refreshInterval: 0) {
		display()
		
		section(getFormat("header-green", " Devices")) {
			paragraph "Remember, all devices are selected in the Parent App."
		}
		
		section(getFormat("header-green", " Select Flow File")) {
			getFileList()
			input "flowFile", "enum", title: "Choose a Flow JSON file", required: false, options: state.jsonList, submitOnChange: true
        }
 	
		section(getFormat("header-green", " General")) {
			input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
			if(logEnable) {
				input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
			}
		}
    }
}

def reloadPage(params) {
    if (settings.flowFile) {
        if(logEnable) log.info "Manually reloading flow file: ${settings.flowFile}"
        readAndParseFlow()
    }
    mainPage()
}

def installed() { 
	initialize() 
}
	
def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    if(logEnable) log.info "Initializing, reading flow: ${settings.flowFile}"
	if(flowFile) {
    	readAndParseFlow()
	}
}

def loadFlowJson() {
    if (!flowFile) return null
    def uri = "http://${location.hub.localIP}:8080/local/${flowFile}"
    def params = [
        uri: uri,
        contentType: "text/html; charset=UTF-8",
        headers: [
            "Cookie": cookie
        ]
    ]
    try {
        def flow
        httpGet(params) { resp ->
            def jsonStr = resp.getData().toString()
            if (!jsonStr) {
                log.error "No file content found for ${flowFile}"
                flow = null
            } else {
                flow = parseJson(jsonStr)
            }
        }
        return flow
    } catch (e) {
        log.error "Failed to load or parse flow: ${e}"
        return null
    }
}

def loadVariables() {
    state.varCtx = [:]
    getGlobalVars()
    
    (state.globalVars).each { v ->
        state.varCtx[v.name] = resolveVarValue(v)
    }
}

def resolveVarValue(v, _visited = []) {
    if (!v || !v.name) return ""
    if (_visited.contains(v.name)) return "ERR:Circular"
    _visited += v.name
    def val = v.value
    if (val instanceof String && (val.contains('$(') || val.matches('.*[+\\-*/><=()].*'))) {
        return evalExpression(val, _visited)
    }
    if (val ==~ /^-?\d+(\.\d+)?$/) return val.contains(".") ? val.toDouble() : val.toInteger()
    if (val.toLowerCase() == "true" || val.toLowerCase() == "false") return val.toLowerCase() == "true"
    return val
}

def evalExpression(expr, _visited = []) {
    // Variable substitution only (no Eval)
    expr = expr.replaceAll(/\$\((\w+)\)/) { full, vname ->
        def v = (state.flowVars + state.globalVars).find { it.name == vname }
        return v ? resolveVarValue(v, _visited) : "null"
    }
    return expr
}

def resolveNodeFields(data) {
    def result = [:]
    data.each { k, v ->
        if (v instanceof String && v.contains('$(')) {
            result[k] = evalExpression(v)
        } else {
            result[k] = v
        }
    }
    return result
}

String resolveVars(str) {
    if (!str || !(str instanceof String)) return str
    // Supports both $(var) and ${var}
    def pattern = /\$\((\w+)\)|\$\{(\w+)\}/
    def out = str.replaceAll(pattern) { all, v1, v2 ->
        def var = v1 ?: v2
        state.vars?.get(var)?.toString() ?:
        state.flowVars?.find { it.name == var }?.value?.toString() ?:
        state.globalVars?.find { it.name == var }?.value?.toString() ?:
        state.varCtx?.get(var)?.toString() ?:
        ""
    }
    return out
}

def getGlobalVars() {
	if(logEnable) log.debug "In getGlobalVars"
	state.globalVars = []
	uri = "http://${location.hub.localIP}:8080/local/FE_global_vars.json"
    def params = [
        uri: uri,
        contentType: "text/html; charset=UTF-8",
        headers: [
            "Cookie": cookie
        ]
    ]
    try {
        httpGet(params) { resp ->
            def jsonStr = resp.getData().toString()
            if (!jsonStr) {
                log.error "No file content found for ${settings.flowFile}"
                return
            }
            state.globalVars = parseJson(jsonStr)
			if(logEnable) log.debug "In getGlobalVars - Reloaded state.globalVars: ${state.globalVars}"
        }
    } catch (e) {
        log.error "Failed to load or parse flow: ${e}"
		state.globalVars = []
    }
}

def setVariable(varName, varValue) {
	if(logEnable) log.debug "In setVariable - varName: ${varName} - varValue: ${varValue}"
	// Reload FE_global_vars.json every time before setting/checking variables
	
	getGlobalVars()
	
    def updatedGlobal = false
    def updatedLocal = false
    state.flowVars = state.flowVars ?: []
    state.globalVars = state.globalVars ?: []

    // Check for global var, update if exists
	if(state.globalVars == []) log.info "There are NO Global vars"
    state.globalVars.each { v ->
		//if(logEnable) log.info "GLOBAL - Checking v.name: ${v.name} VS varName: ${varName}"
        if (v.name == varName) {
			if(logEnable) log.info "GLOBAL updating v.name: ${v.name} to varValue: ${varValue}"
            v.value = varValue
            updatedGlobal = true
        }
    }
	if(!updatedGlobal) { if(logEnable) log.debug "Global - ${varName} - NOT FOUND" }
    // Check for local var, update if exists
	if(state.flowVars == []) log.info "There are NO Flow vars"
    state.flowVars.each { v ->
		//if(logEnable) log.info "Flow - Checking v.name: ${v.name} VS varName:{varName}"
        if (v.name == varName) {
            v.value = varValue
			if(logEnable) log.info "Flow updating v.name: ${v.name} to varValue: ${varValue}"
            updatedLocal = true
        }
    }

	// Add new var if not found anywhere, default to local
	if (!updatedGlobal && !updatedLocal) {
		if(logEnable) log.info "${varName} - NOT FOUND in EITHER Global or Flow - Creating in Local"
		state.flowVars << [name: varName, value: varValue]
		updatedLocal = true
	}

	// If updatedLocal, update flow JSON's variables and resave
	if (updatedLocal) {
		if (state.flow) {
		// Save ONLY the variables array, not the whole state.flow which may be mutated at runtime!
		def cleanFlow = [:]
		cleanFlow.putAll(state.flow)
		// Only replace the variables section, never mutate drawflow!
		cleanFlow.variables = state.flowVars
		def fData = groovy.json.JsonOutput.toJson(cleanFlow)
		parent.saveFlow(flowFile.replace(".json", ""), fData)
		if(logEnable) log.debug "Saved updated variables back to flow JSON"
		}
	}

    // Save files as needed
    if (updatedGlobal) saveGlobalVarsToFile()

    // Update in-memory context for both
    state.vars = state.vars ?: [:]
    state.vars[varName] = varValue
    state.varCtx = state.varCtx ?: [:]
    state.varCtx[varName] = varValue
}

def saveGlobalVarsToFile() {
	if(logEnable) log.debug "In saveGlobalVarsToFile - Saving FE_global_vars.json"
	def flowFile = "FE_global_vars.json"
	def fData = groovy.json.JsonOutput.toJson(state.globalVars)
	uploadHubFile("${flowFile}",fData.getBytes())
}

def readAndParseFlow() {
    if(logEnable) log.debug "In readAndParseFlow"
    uri = "http://${location.hub.localIP}:8080/local/${flowFile}"
    def params = [
        uri: uri,
        contentType: "text/html; charset=UTF-8",
        headers: [
            "Cookie": cookie
        ]
    ]
    try {
        httpGet(params) { resp ->
            def jsonStr = resp.getData().toString()
            if (!jsonStr) {
                log.error "No file content found for ${settings.flowFile}"
                return
            }
            state.flow = parseJson(jsonStr)
            loadVariables()  // <--- Load variables after loading flow
            if(logEnable) log.debug "Flow loaded. Subscribing to triggers."
            subscribeToTriggers()
        }
    } catch (e) {
        log.error "Failed to load or parse flow: ${e}"
    }
}

def subscribeToTriggers() {
    if(logEnable) log.debug "In subscribeToTriggers"
    unsubscribe()
    unschedule() // Important: clear all previous schedules
    if (!state.flow) return
    state.flow.drawflow?.Home?.data.each { id, node ->
        if (node.name == "eventTrigger") {
            if (node.data.deviceId == "__time__") {
                scheduleTimeTrigger(id, node)
            } else if (node.data.deviceId == "__mode__") {
                if (logEnable) log.debug "Subscribing to location mode changes"
                getRealDeviceData(devId, "subscribeToTriggers: nodeId=${id}, attr=${attr}")
            } else {
                def devIds = []
                if (node.data.deviceIds instanceof List && node.data.deviceIds) {
                    devIds = node.data.deviceIds
                } else if (node.data.deviceId) {
                    devIds = [node.data.deviceId]
                }
                def attr = node.data.attribute
                devIds.each { devId ->
                    if (devId && attr) {
                        getRealDeviceData(devId, "subscribeToTriggers: nodeId=${id}, attr=${attr}")
                        if (state.device) {
                            if (logEnable) log.debug "Subscribing to ${state.device.displayName} - ${attr}"
                            subscribe(state.device, attr, "handleEvent")
                        } else {
                            log.warn "Device ID ${devId} not found in Hubitat"
                        }
                    }
                }
            }
        }
    }
}

def scheduleTimeTrigger(nodeId, node) {
    def attr = node.data.attribute
    def cmp = node.data.comparator
    def value = resolveVars(node.data.value)
	def value2 = resolveVars(node.data.value2)
    def offsetMin = (node.data.offsetMin ?: 0) as Integer
    def days = node.data.selectedDays ?: []
    def allowDays = node.data.allowDayOfWeek
    def tz = location.timeZone
    def now = new Date()
    String logPrefix = "Time Trigger Node $nodeId: "

    // Helper to schedule jobs in the future, rolls to next day if time has passed
    def scheduleFuture = { Date time, String extra="" ->
        if (time.before(now)) time = new Date(time.getTime() + 86400000L)
        if (logEnable) log.info "${logPrefix}Scheduling for $attr$extra at $time"
        runOnce(time, "handleScheduledTrigger", [data:[nodeId: nodeId]])
    }

    if (attr == "timeOfDay") {
        if (["sunrise", "sunset"].contains(value)) {
            def baseTime = (value == "sunrise") ? location.sunrise : location.sunset
            if (baseTime) {
                use(groovy.time.TimeCategory) { baseTime = baseTime + offsetMin.minutes }
                scheduleFuture(baseTime, " ($value${offsetMin!=0?" offset "+offsetMin+" min":""})")
            }
        } else if (value?.contains(":")) {
            def parts = value.split(":")
            if (parts.size() == 2) {
                def runAt = new Date(now.year, now.month, now.date, parts[0] as int, parts[1] as int, 0)
                scheduleFuture(runAt)
            }
        }
    } else if (attr == "currentTime") {
        if (cmp == "==") {
            def parts = value.split(":")
            if (parts.size() == 2) {
                def runAt = new Date(now.year, now.month, now.date, parts[0] as int, parts[1] as int, 0)
                scheduleFuture(runAt)
            }
        } else if (cmp in ["between", "outside"]) {
            def parts1 = value.split(":")
            def parts2 = value2.split(":")
            if (parts1.size() == 2 && parts2.size() == 2) {
                def runAt1 = new Date(now.year, now.month, now.date, parts1[0] as int, parts1[1] as int, 0)
                def runAt2 = new Date(now.year, now.month, now.date, parts2[0] as int, parts2[1] as int, 0)
                scheduleFuture(runAt1, " (between/outside start)")
                scheduleFuture(runAt2, " (between/outside end)")
            }
        } else if (cmp == "before" || cmp == "after") {
            def parts = value.split(":")
            if (parts.size() == 2) {
                def runAt = new Date(now.year, now.month, now.date, parts[0] as int, parts[1] as int, 0)
                scheduleFuture(runAt, " ($cmp boundary)")
            }
        } else if (cmp == "betweenSunriseSunset" || cmp == "betweenSunsetSunrise") {
            def sunrise = location.sunrise
            def sunset = location.sunset
            if (sunrise && sunset) {
                scheduleFuture(sunrise, " (betweenSunriseSunset)")
                scheduleFuture(sunset, " (betweenSunriseSunset)")
            }
        }
    } else if (attr == "dayOfWeek") {
        days.each { day ->
            int dowNum = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"].indexOf(day)
            if (dowNum >= 0) {
                def target = getNextWeekdayDate(dowNum)
                scheduleFuture(target, " (dayOfWeek $day)")
            }
        }
    }
    // Combo: If allowDays or days are set, and a time is present, schedule only for those days at that time
    if ((allowDays || (days && days.size() > 0)) && (attr == "currentTime" || attr == "timeOfDay")) {
        days.each { day ->
            int dowNum = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"].indexOf(day)
            if (dowNum >= 0) {
                def parts = value?.split(":")
                if (parts && parts.size() == 2) {
                    def target = getNextWeekdayDate(dowNum, parts[0] as int, parts[1] as int)
                    scheduleFuture(target, " (days+time)")
                }
            }
        }
    }
}

// Helper to get next occurrence of a weekday (optionally with hour/minute)
def getNextWeekdayDate(int targetDow, int hour = 0, int min = 0) {
    def now = new Date()
    int todayDow = now[Calendar.DAY_OF_WEEK] - 1 // 0=Sunday
    int delta = (targetDow - todayDow + 7) % 7
    if (delta == 0 && (now.hours > hour || (now.hours == hour && now.minutes >= min))) delta = 7 // today but time passed
    def target = new Date(now.year, now.month, now.date + delta, hour, min, 0)
    return target
}

def handleScheduledTrigger(data) {
    def nodeId = data?.nodeId ?: data
    if (logEnable) log.info "Scheduled time trigger fired for nodeId=$nodeId"
    def node = flowNodes()[nodeId]
	def rdata = resolveNodeFields(node.data)
    def timeValue = node?.data?.value ?: "scheduled"
    evaluateNode(nodeId, [name: "time", value: timeValue], null, null)
}

def handleEvent(evt) {
    if(logEnable) {
		def triggerDevName = evt?.device?.displayName ?: "Unknown Device"
		log.info "------------------------ In handleEvent ------------------------"
		log.info "Triggered by ${triggerDevName} ${evt?.name}=${evt?.value}"
	}
    def triggerNodes = getTriggerNodes(evt)
    if (!triggerNodes) {
        log.warn "No trigger node found for this event."
        return
    }
    triggerNodes.each { triggerId, triggerNode ->
		def eventDeviceId = evt.device?.id?.toString()
		getRealDeviceData(eventDeviceId, "handleEvent: triggerId=${triggerId}")
        if (!state.device) {
            if (logEnable) log.warn "Skipping trigger for missing device ID: ${triggerNode.data.deviceId}"
            return // Skip this trigger node only
        }
        def devName = state.device?.displayName ?: "Unknown Device"
        if (logEnable) log.info "Trigger node matched: Node $triggerId (${devName} - ${evt?.name}=${evt?.value})"
        def forMin = (triggerNode.data.sustainedMin ?: 0) as Integer
        def expectedValue = triggerNode.data.value
        def comparator = triggerNode.data.comparator ?: "=="
        unschedule("sustainedTriggerHandler")
        if (forMin > 0) {
            if (evaluateComparator(evt.value, expectedValue, comparator)) {
                if (logEnable) log.info "Scheduling sustainedTriggerHandler for node ${triggerId} (${devName}) in ${forMin} min"
                runIn(forMin * 60, "sustainedTriggerHandler", [data: [triggerId: triggerId, deviceId: evt?.device?.id, attribute: evt?.name, value: evt?.value]])
                state.sustainedTimers = state.sustainedTimers ?: [:]
                state.sustainedTimers[triggerId] = [startValue: evt.value, start: now()]
            } else {
                if (logEnable) log.info "Event did not match value for sustained trigger, no timer scheduled."
            }
        } else {
            evaluateNode(triggerId, evt)
        }
    }
}

def sustainedTriggerHandler(data) {
    def triggerId = data?.triggerId
    if (!triggerId) return
    def node = flowNodes()[triggerId]
    if (!node) return
    def devId = node.data.deviceId
    def attr = node.data.attribute
    def expectedValue = resolveVars(node.data.value)
    def comparator = node.data.comparator ?: "=="
    getRealDeviceData(devId)
    def currentValue = state.device?.currentValue(attr)
    if (evaluateComparator(currentValue, expectedValue, comparator)) {
        if (logEnable) log.info "Sustained trigger fired for node $triggerId; value=$currentValue still matches expected $expectedValue"
        evaluateNode(triggerId, [name: attr, value: currentValue])
    } else {
        if (logEnable) log.info "Sustained trigger for node $triggerId aborted; current=$currentValue, expected=$expectedValue"
    }
    if (state.sustainedTimers) state.sustainedTimers.remove(triggerId)
}

// Map of nodeID -> node from flow
def flowNodes() {
    def freshFlow = loadFlowJson()
    return freshFlow?.drawflow?.Home?.data ?: [:]
}

def getTriggerNodes(evt) {
    if (evt.name == "mode") {
        // Find triggers for mode changes
        return flowNodes().findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == "__mode__" &&
            node.data.attribute == "mode"
        }
    } else {
        // Multi-device triggers
        return flowNodes().findAll { id, node ->
            if (node.name != "eventTrigger") return false
            def devIds = []
            if (node.data.deviceIds instanceof List && node.data.deviceIds) {
                devIds = node.data.deviceIds
            } else if (node.data.deviceId) {
                devIds = [node.data.deviceId]
            }
            return devIds.contains(evt.device.id.toString()) &&
                   node.data.attribute == evt.name
        }
    }
}

def evaluateNode(nodeId, evt, incomingValue = null, Set visited = null) {
    if(logEnable) log.info "------------------------ In evaluateNode ------------------------"
    if (!visited) visited = new HashSet()
    if (visited.contains(nodeId)) {
        return null
    }
    visited << nodeId

    def node = flowNodes()[nodeId]
    if (!node) return null

	try {
		parent.notifyFlowTrace(flowFile, nodeId, node?.name)
	} catch (e) {
		log.warn "Failed to notify parent for flow trace: $e"
	}

    if(logEnable) log.debug "EVALUATING NODE: $nodeId (${node?.name}) - incomingValue: $incomingValue"

    switch (node.name) {
        case "eventTrigger":
			def debounceSec = (node.data.debounceSec ?: 0) as Integer
			if (debounceSec > 0) {
				def nowMs = now()
				def lastMs = state.flowDebounce ?: 0
				if ((nowMs - lastMs) < debounceSec * 1000) {
					if (logEnable) log.info "Flow DEBOUNCED (interval ${debounceSec}s, last ${(nowMs - lastMs) / 1000}s ago)"
					return
				}
				state.flowDebounce = nowMs
			}
			def passes
			if (node.data.deviceId == "__time__") {
				passes = true   // Schedules always fire at the correct time!
			} else if (node.data.deviceId == "__mode__") {
				def expected = resolveVars(node.data.value)
				if (expected instanceof List) {
					if (node.data.comparator == "==") {
						passes = expected.contains(evt.value)
					} else if (node.data.comparator == "!=") {
						passes = !expected.contains(evt.value)
					} else {
						passes = evaluateComparator(evt.value, expected, node.data.comparator)
					}
				} else {
					passes = evaluateComparator(evt.value, expected, node.data.comparator)
				}
			} else {
				passes = evaluateComparator(evt.value, resolveVars(node.data.value), node.data.comparator)
			}
			if(logEnable) log.debug "Trigger node: device=${state.device?.displayName ?: node.data.deviceId}, event value=${evt.value}, expected value=${node.data.value}, comparator=${node.data.comparator}, passes=$passes"
			node.outputs?.each { outName, outObj ->
				if(logEnable) log.debug "Trigger node outputs: $outName -> ${outObj.connections}"
				outObj.connections?.each { conn ->
					if (passes) evaluateNode(conn.node, evt, null, visited)
				}
			}
			break

                case "condition":
					// Handle time/day conditions
					if(node.data.deviceId == "__time__") {
						if(logEnable) log.debug "----- In __time__ -----"
						def attr = node.data.attribute
						def cmp = node.data.comparator
						def value = resolveVars(node.data.value)
						def value2 = resolveVars(node.data.value2)
						def days = node.data.selectedDays ?: []
						def allowDays = node.data.allowDayOfWeek
						def passes = false

						def today = getTodayName()
						def nowTime = getNowTimeStr()
						def sunrise = location.sunrise
						def sunset = location.sunset

						if (logEnable) log.debug "Time/Day Condition: attr=${attr}, cmp=${cmp}, value=${value}, value2=${value2}, days=${days}, allowDays=${allowDays}, today=${today}, nowTime=${nowTime}"

                if (attr == "currentTime") {
                    if (cmp == "==") {
                        passes = (nowTime == value)
                    } else if (cmp == "between") {
                        passes = isNowBetween(value, value2)
                    } else if (cmp == "outside") {
                        passes = !isNowBetween(value, value2)
                    } else if (cmp == "before") {
                        passes = nowTime < value
                    } else if (cmp == "after") {
                        passes = nowTime > value
                    } else if (cmp == "betweenSunriseSunset") {
                        def now = new Date()
                        passes = (now.after(sunrise) && now.before(sunset))
                    } else if (cmp == "betweenSunsetSunrise") {
                        def now = new Date()
                        passes = (now.before(sunrise) || now.after(sunset))
                    }
                    if (allowDays && days && days.size() > 0) {
                        passes = passes && days.contains(today)
                    }
                    if (logEnable) log.info formatTimeEvalLog(
                        attr: attr, cmp: cmp, value: value, value2: value2, days: days, nowTime: nowTime,
                        sunrise: sunrise?.format("HH:mm", location.timeZone),
                        sunset: sunset?.format("HH:mm", location.timeZone),
                        passes: passes
                    )
                } else if (attr == "dayOfWeek") {
                    passes = days && days.contains(today)
                    if (logEnable) log.info formatTimeEvalLog(
                        attr: attr, cmp: cmp, days: days, nowTime: nowTime, passes: passes
                    )
                } else if (attr == "timeOfDay") {
                    def offsetMin = (node.data.offsetMin ?: 0) as Integer
                    def eventTime = (value == "sunrise") ? sunrise : sunset
                    if (eventTime) {
                        use(groovy.time.TimeCategory) {
                            eventTime = eventTime + offsetMin.minutes
                        }
                    }
                    def eventStr = eventTime ? eventTime.format("HH:mm", location.timeZone) : null
                    passes = (nowTime == eventStr)
                    if (logEnable) log.info formatTimeEvalLog(
                        attr: attr, cmp: cmp, value: value, offsetMin: offsetMin, nowTime: nowTime,
                        eventStr: eventStr, passes: passes
                    )
                }

                if(logEnable) log.debug "Result: passes=${passes}"
                node.outputs?.output_1?.connections?.each { conn ->
                    if (passes) evaluateNode(conn.node, evt, null, visited)
                }
                node.outputs?.output_2?.connections?.each { conn ->
                    if (!passes) evaluateNode(conn.node, evt, null, visited)
                }
                return passes
            }
			if (node.data.deviceId == "__mode__" && node.data.attribute == "mode") {
				def currentMode = location.mode
				def expected = resolveVars(node.data.value)
				def passes = false
				if (expected instanceof List) {
					if (node.data.comparator == "==") {
						passes = expected.contains(currentMode)
					} else if (node.data.comparator == "!=") {
						passes = !expected.contains(currentMode)
					}
				} else {
					passes = evaluateComparator(currentMode, expected, node.data.comparator)
				}
				if (logEnable) log.debug "Mode Condition: currentMode=${currentMode}, expected=${expected}, cmp=${node.data.comparator}, passes=${passes}"
				node.outputs?.output_1?.connections?.each { conn ->
					if (passes) evaluateNode(conn.node, evt, null, visited)
				}
				node.outputs?.output_2?.connections?.each { conn ->
					if (!passes) evaluateNode(conn.node, evt, null, visited)
				}
				return passes
			}

            // Regular device/attribute condition -- PATCHED
            if(logEnable) log.debug "----- In condition Regular device/attribute -----"
            def devIds = []
			if (node.data.deviceIds instanceof List && node.data.deviceIds) {
				devIds = node.data.deviceIds
			} else if (node.data.deviceId) {
				devIds = [node.data.deviceId]
			}
			def attr = node.data.attribute
			def passes = false
            def currentValue

            // --- PATCH: Use event value if attribute matches ---
            if (evt && evt.name == attr && evt.value != null) {
                currentValue = evt.value
            } else if (devIds && devIds[0]) {
                getRealDeviceData(devIds[0])
                currentValue = state.device?.currentValue(attr)
            } else {
                currentValue = null
            }
            // --- END PATCH ---

			def debugDeviceName = devIds ? devIds.collect { did ->
				getRealDeviceData(did)
				state.device?.displayName ?: did
			}.join(", ") : "none"

			passes = evaluateComparator(currentValue, resolveVars(node.data.value), node.data.comparator)

			if (logEnable) log.debug "Condition node: device(s)=${debugDeviceName}, ExpectedVal=${node.data.value}, comparator=${node.data.comparator}, actual=${currentValue}, passes=$passes"
            node.outputs?.output_1?.connections?.each { conn ->
                if (passes) evaluateNode(conn.node, evt, null, visited)
            }
            node.outputs?.output_2?.connections?.each { conn ->
                if (!passes) evaluateNode(conn.node, evt, null, visited)
            }
            return passes

        case "AND":
            if(logEnable) log.debug "----- In AND Condition node -----"
            def passes = (incomingValue == true)
            if(logEnable) log.debug "AND node outputs: ${node.outputs?.true?.connections} (true), ${node.outputs?.false?.connections} (false)"
            node.outputs?.true?.connections?.each { conn -> if (passes) evaluateNode(conn.node, evt, null, visited) }
            node.outputs?.false?.connections?.each { conn -> if (!passes) evaluateNode(conn.node, evt, null, visited) }
            return passes

        case "OR":
            if(logEnable) log.debug "----- In OR Condition node -----"
            def passes = (incomingValue == true)
            if(logEnable) log.debug "OR node outputs: ${node.outputs?.output_1?.connections} (true), ${node.outputs?.output_2?.connections} (false)"
            node.outputs?.output_1?.connections?.each { conn -> if (passes) evaluateNode(conn.node, evt, null, visited) }
            node.outputs?.output_2?.connections?.each { conn -> if (!passes) evaluateNode(conn.node, evt, null, visited) }
            return passes

        case "NOT":
            if(logEnable) log.debug "----- In NOT Condition node -----"
            def input = node.inputs?.collect { k, v -> v.connections*.node }.flatten()?.getAt(0)
            def result = !evaluateNode(input, evt, null, visited)
            node.outputs?.true?.connections?.each { conn -> if (result) evaluateNode(conn.node, evt, null, visited) }
            node.outputs?.false?.connections?.each { conn -> if (!result) evaluateNode(conn.node, evt, null, visited) }
            return result

        case "device":
			def devIds = []
			if (node.data.deviceIds instanceof List) {
				devIds = node.data.deviceIds
			} else if (node.data.deviceIds) {
				devIds = [node.data.deviceIds]
			} else if (node.data.deviceId) {
				devIds = [node.data.deviceId]
			}
			def cmd = resolveVars(node.data.command)
            def val = resolveVars(node.data.value)
			def output = []

			if (devIds.size() == 1 && devIds[0] == "__mode__" && cmd == "setMode" && val) {
				def modeToSet = (val instanceof List && val) ? val[0] : val
				def modeObj = location.modes.find { it.name == modeToSet }
				if (modeObj) {
					setLocationMode(modeToSet)
					render contentType: "text/plain", data: "Set location mode to ${modeToSet}"
					return
				} else {
					render contentType: "text/plain", data: "Mode '${modeToSet}' not found"
					return
				}
			}

			devIds.each { devId ->
				def device = parent.getDeviceById(devId)
				if (device && cmd) {
					if (cmd == "setColor" && node.data.color) {
						// Convert hex to HSV or at least something the driver can use
						def color = node.data.color
						def rgb = color?.startsWith("#") ? color.substring(1) : color
						if (rgb.size() == 6) {
							def r = Integer.parseInt(rgb.substring(0,2),16) / 255.0
							def g = Integer.parseInt(rgb.substring(2,4),16) / 255.0
							def b = Integer.parseInt(rgb.substring(4,6),16) / 255.0

							def max = [r, g, b].max()
							def min = [r, g, b].min()
							def h, s, v
							v = max
							def d = max - min
							s = max == 0 ? 0 : d / max
							if (max == min) {
								h = 0 // achromatic
							} else if (max == r) {
								h = (g - b) / d + (g < b ? 6 : 0)
							} else if (max == g) {
								h = (b - r) / d + 2
							} else if (max == b) {
								h = (r - g) / d + 4
							}
							h = h / 6

							def hue = (h * 100).toInteger()
							def sat = (s * 100).toInteger()
							def lev = (v * 100).toInteger()
							def colorMap = [hue: hue, saturation: sat, level: lev]
							device.setColor(colorMap)
							output << "Executed setColor on ${device.displayName} with ${colorMap}"
						} else {
							output << "Invalid color format: ${color}"
						}
					} else if (val != null && val != "") {
						def arg = val
						if (val.isInteger()) arg = val.toInteger()
						else if (val.isDouble()) arg = val.toDouble()
						device."${cmd}"(arg)
					} else {
						device."${cmd}"()
					}
					output << "Executed ${cmd} on ${device.displayName} ${val ? "with value $val" : ""}"
					if (logEnable) log.info "Device Action: ${device.displayName} (${device.id}) -- Command: ${cmd}${val ? ", Value: $val" : ""}"
				}
			}
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(conn.node, evt, null, visited)
			}
			return

        case "doNothing":
            if(logEnable) log.debug "----- In Do Nothing, node reached: $nodeId - doing nothing."
            break

        case "delayMin":
			def min = (node.data.delayMin ?: 1) as Integer
			def minDelay = min * 60000
			if(logEnable) log.debug "----- In DelayMin node reached: Delaying for ${min} minute(s) -----"
			pauseExecution(minDelay)
			if(logEnable) log.debug "----- In DelayMin - Finished -----"
			// Continue to next node(s) after delay
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(conn.node, evt, null, visited)
			}
			break

        case "delay":
            def ms = (node.data.delayMs ?: 1000) as Integer
            if(logEnable) log.debug "----- Delay node reached: Delaying for ${ms} ms -----"
            pauseExecution(ms)
			if(logEnable) log.debug "----- In DelayMs - Finished -----"
			// Continue to next node(s) after delay
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(conn.node, evt, null, visited)
			}
            break
		
		case "notification":
			def ids = node.data.targetDeviceId instanceof List ? node.data.targetDeviceId : [resolveVars(node.data.targetDeviceId)]
            def msg = resolveVars(node.data.message) ?: "Test Notification"

			ids.each { devId ->
				getRealDeviceData(devId)
		
				if (state.device && node.data.notificationType == "push" && state.device.hasCommand("deviceNotification")) {
					if(logEnable) log.debug "push - ${msg}"
					state.device.deviceNotification(msg)
				}
				if (state.device && node.data.notificationType == "speech" && state.device.hasCommand("speak")) {
					if(logEnable) log.debug "speech - ${msg}"
					state.device.speak(msg)
				}
			}
			if(logEnable) log.debug "Notification sent to device(s)"
			return
		
		case "setVariable":
			def varName = resolveVars(node.data.varName)
			def varValue = resolveVars(node.data.varValue)
			setVariable(varName, varValue)
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(conn.node, evt, null, visited)
			}
			break

		case "notMatchingVar":
			if (logEnable) log.debug "----- In notMatchingVar node -----"

			// Gather device IDs (multi or single)
			def devIds = []
			if (node.data.deviceIds instanceof List && node.data.deviceIds) {
				devIds = node.data.deviceIds
			} else if (node.data.deviceId) {
				devIds = [node.data.deviceId]
			}

			def attr = node.data.attribute
			def expectedValue = resolveVars(node.data.value)
			def comparator = node.data.comparator ?: "=="
			def outputVar = (node.data.outputVar && node.data.outputVar.trim() && node.data.outputVar.toLowerCase() != "undefined")
				? node.data.outputVar.trim()
				: "notMatchingVar"

			def notMatching = []

			devIds.each { devId ->
				getRealDeviceData(devId)
				def deviceName = state.device?.displayName ?: devId
				def currentValue = state.device?.currentValue(attr)
				if (!evaluateComparator(currentValue, expectedValue, comparator)) {
					notMatching << [id: devId, name: deviceName, value: currentValue]
				}
			}

			// Save to state variable for flow logic
			state.vars = state.vars ?: [:]
			state.vars[outputVar] = notMatching
			state.varCtx = state.varCtx ?: [:]
			state.varCtx[outputVar] = notMatching

			// Save to /local/<outputVar>.json
			try {
				def filename = outputVar.endsWith(".json") ? outputVar : (outputVar + ".json")
				def jsonStr = groovy.json.JsonOutput.toJson(notMatching)
				uploadHubFile(filename, jsonStr.getBytes("UTF-8"))
				if (logEnable) log.info "Saved notMatching devices to /local/${filename}"
			} catch (e) {
				log.error "Failed to save custom notMatchingVar json: ${e}"
			}

			// Continue the flow (output_1)
			node.outputs?.output_1?.connections?.each { conn ->
				evaluateNode(conn.node, evt, null, visited)
			}
			break

        default:
            log.warn "Unknown node type: ${node.name}"
    }
}

// Returns today as "Monday", "Tuesday", etc.
def getTodayName() {
    def todayIdx = new Date().format('u', location.timeZone) as Integer // 1=Monday, 7=Sunday
    def dayNames = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    return dayNames[(todayIdx - 1) % 7]
}

// Returns now time as "HH:mm"
def getNowTimeStr() {
    return new Date().format("HH:mm", location.timeZone)
}

def formatTimeEvalLog(Map args = [:]) {
    // args: attr, cmp, value, value2, days, nowTime, sunrise, sunset, offsetMin, passes
    String msg = ""
    def cmp = args.cmp
    switch (cmp) {
        case "==":
            msg = "Current time ${args.nowTime} == ${args.value}"
            break
        case "between":
            msg = "Current time ${args.nowTime} is between ${args.value} and ${args.value2}"
            break
        case "outside":
            msg = "Current time ${args.nowTime} is outside ${args.value} and ${args.value2}"
            break
        case "before":
            msg = "Current time ${args.nowTime} is before ${args.value}"
            break
        case "after":
            msg = "Current time ${args.nowTime} is after ${args.value}"
            break
        case "betweenSunriseSunset":
            msg = "Current time ${args.nowTime} is between sunrise (${args.sunrise}) and sunset (${args.sunset})"
            break
        case "betweenSunsetSunrise":
            msg = "Current time ${args.nowTime} is between sunset (${args.sunset}) and sunrise (${args.sunrise})"
            break
        case "timeOfDay":
            msg = "Current time ${args.nowTime} compared to ${args.value} (with offset ${args.offsetMin} min): evaluated as ${args.eventStr}"
            break
        default:
            msg = "Time eval: attr=${args.attr}, cmp=${args.cmp}, value=${args.value}, value2=${args.value2}, nowTime=${args.nowTime}"
    }
    if (args.days && args.days.size() > 0) msg += " on days: ${args.days}"
    msg += " => ${args.passes ? "MATCH" : "NO MATCH"}"
    return msg
}

// Simple comparators for string/number
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
		case "changes": 
    		// Always true, because an event means it changed
    		return true

        // Add more as needed
        default:        return "$actual" == "$expected"
    }
}

def sustainedTriggerHandler(triggerId, data) {
    def node = flowNodes()[triggerId]
    if (!node) return
    def devId = node.data.deviceId
    def attr = node.data.attribute
    def expectedValue = resolveVars(node.data.value)
    def comparator = node.data.comparator ?: "=="
    getRealDeviceData(devId)
    def currentValue = state.device?.currentValue(attr)
    if (evaluateComparator(currentValue, expectedValue, comparator)) {
        if (logEnable) log.info "Sustained trigger fired for node $triggerId; value=$currentValue still matches expected $expectedValue"
        evaluateNode(triggerId, [name: attr, value: currentValue])
    } else {
        if (logEnable) log.info "Sustained trigger for node $triggerId aborted; current=$currentValue, expected=$expectedValue"
    }
    if (state.sustainedTimers) state.sustainedTimers.remove(triggerId)
}

def toDouble(val) {
    try { return val as Double } catch(e) { return 0 }
}

Boolean getFileList(){
    if(logEnable) log.debug "Getting list of files"
    uri = "http://${location.hub.localIP}:8080/hub/fileManager/json";
    def params = [ uri: uri ]
    try {
        def tempList = []
        httpGet(params) { resp ->
            if (resp != null) {
                if(logEnable) log.debug "In getFileList - Found the files"
                def json = resp.data
                for (rec in json.files) {
                    if(rec.name?.toLowerCase()?.endsWith('.json')) {
                        tempList << rec.name
                    }
                }
            }
        }
		state.jsonList = tempList.sort { a, b -> a <=> b }
    } catch (e) {
        log.error e
        state.jsonList = ""
    }
}

def getRealDeviceData(devId, context = null) {
	if(logEnable) log.debug "----- In getRealDeviceData - Looking for ${devId} -----"
    state.device = null
    if(devId != "__time__") {
        allDevices = parent.masterDeviceList
        allDevices.each { it ->
            if(it.deviceId.toString() == devId.toString()) {
                state.device = it
            }
        }
        if (logEnable) {
            if (state.device) {
                log.debug "In getRealDeviceData - Found: ${state.device.displayName} - ${state.device.deviceId}"
            } else {
                def ctxStr = context ? " [Context: ${context}]" : ""
                log.warn "Device with ID '${devId}' not found in masterDeviceList${ctxStr}"
            }
        }
    } else {
        if(logEnable) log.debug "In getRealDeviceData - Skipping for ${devId}"
    }
}

mappings {
    path("/runFlow") 	{ action: [ POST: "apiRunFlow" ] }
	path("/setVar") 	{ action: [ POST: "apiSetVar" ] }
}

def apiSetVar() {
    def json = request.JSON
    def name = json?.name
    def value = json?.value
    if (name) {
        setVariable(name, value)
        render contentType: "application/json", data: [result: "ok", name: name, value: value] as groovy.json.JsonBuilder
    } else {
        render status: 400, contentType: "application/json", data: [error: "Missing variable name"] as groovy.json.JsonBuilder
    }
}

def apiRunFlow() {
    def json = request.JSON
    if(logEnable) log.info "Received flow for execution: ${json}"
    // Optionally, store it
    state.lastRunFlow = json
    // Now, parse and execute the flow
    handleEvent(json)
    render contentType: "text/plain", data: "Flow received and executed."
}

def getFormat(type, myText=null, page=null) {
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid #000000;box-shadow: 2px 3px #8B8F8F;border-radius: 5px'>${myText}</div>"
	if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />"
}

def display() {
    section() {
        paragraph getFormat("line")
		label title: "Enter a name for this automation", required:true, submitOnChange:true
    }
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
    }
}
