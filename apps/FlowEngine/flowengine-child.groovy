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
            //href "reloadPage", title: "Reload Flow from File Manager", description: "Tap to reload and subscribe", params: [p: "reload"]
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
				subscribe(location, "mode", "handleEvent")
            } else {
                // Regular device/attribute trigger
                def devId = node.data.deviceId
                def attr = node.data.attribute
                if (devId && attr) {
                    getRealDeviceData(devId)
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

def scheduleTimeTrigger(nodeId, node) {
    def attr = node.data.attribute
    def cmp = node.data.comparator
    def value = node.data.value
    def value2 = node.data.value2
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
    def timeValue = node?.data?.value ?: "scheduled"
    evaluateNode(nodeId, [name: "time", value: timeValue], null, null)
}

def handleEvent(evt) {
    if(logEnable) log.info "------------------------ In handleEvent ------------------------"
    if(logEnable) log.info "Triggered by ${evt.device.displayName} ${evt.name}=${evt.value}"
    def triggerNodes = getTriggerNodes(evt)
    if (!triggerNodes) {
        log.warn "No trigger node found for this event."
        return
    }
    triggerNodes.each { triggerId, triggerNode ->
        getRealDeviceData(triggerNode.data.deviceId)
        def devName = state.device?.displayName ?: "Unknown Device"
        if (logEnable) log.info "Trigger node matched: Node $triggerId (${devName} - ${evt.name}=${evt.value})"
        def forMin = (triggerNode.data.sustainedMin ?: 0) as Integer
        def expectedValue = triggerNode.data.value
        def comparator = triggerNode.data.comparator ?: "=="
        unschedule("sustainedTriggerHandler")
        if (forMin > 0) {
            if (evaluateComparator(evt.value, expectedValue, comparator)) {
                if (logEnable) log.info "Scheduling sustainedTriggerHandler for node ${triggerId} (${devName}) in ${forMin} min"
                runIn(forMin * 60, "sustainedTriggerHandler", [data:[triggerId: triggerId, deviceId: evt.device.id, attribute: evt.name, value: evt.value]])
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
    def expectedValue = node.data.value
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
def flowNodes() { state.flow?.drawflow?.Home?.data ?: [:] }

def getTriggerNodes(evt) {
    if (evt.name == "mode") {
        // Find triggers for mode changes
        return flowNodes().findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == "__mode__" &&
            node.data.attribute == "mode"
        }
    } else {
        // Regular device triggers
        return flowNodes().findAll { id, node ->
            node.name == "eventTrigger" &&
            node.data.deviceId == evt.device.id.toString() &&
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
				def expected = node.data.value
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
				passes = evaluateComparator(evt.value, node.data.value, node.data.comparator)
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
                def value = node.data.value
                def value2 = node.data.value2
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
				def expected = node.data.value
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

            // Regular device/attribute condition
            if(logEnable) log.debug "----- In condition Regular device/attribute -----"
            getRealDeviceData(node.data.deviceId)
            def attrVal = state.device?.currentValue(node.data.attribute)
            def passes = evaluateComparator(attrVal, node.data.value, node.data.comparator)
            if (logEnable) log.debug "Condition node: device=${state.device.displayName}, CurrentVal=${attrVal}, ExpectedVal=${node.data.value}, comparator=${node.data.comparator}, passes=$passes"
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
			def cmd = node.data.command
			def val = node.data.value
			def output = []

			// Handle Location Mode as action
			if (devIds.size() == 1 && devIds[0] == "__mode__" && cmd == "setMode" && val) {
				def modeObj = location.modes.find { it.name == val }
				if (modeObj) {
					setLocationMode(val)
					render contentType: "text/plain", data: "Set location mode to ${val}"
					return
				} else {
					render contentType: "text/plain", data: "Mode '${val}' not found"
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
			def ids = node.data.targetDeviceId instanceof List ? node.data.targetDeviceId : [node.data.targetDeviceId]
			def msg = node.data.message ?: "Test Notification"
			ids.each { devId ->
				def dev = masterDeviceList?.find { it.id == devId }
				if (dev && node.data.notificationType == "push" && dev.hasCommand("deviceNotification")) {
					dev.deviceNotification(msg)
				}
				if (dev && node.data.notificationType == "speech" && dev.hasCommand("speak")) {
					dev.speak(msg)
				}
			}
			render contentType: "text/plain", data: "Notification sent to device(s)"
			return

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
    def expectedValue = node.data.value
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

def getRealDeviceData(devId) {
	if(logEnable) log.debug "----- In getRealDeviceData -----"
	state.device = null
	if(devId != "__time__") {
		allDevices = parent.masterDeviceList						
		allDevices.each { it ->
			if(it.deviceId.toString() == devId.toString()) {
				state.device = it
			}
		}
		if(logEnable) log.debug "In getRealDeviceData - Found: ${state.device.displayName} - ${state.device.deviceId}"
	} else {
		if(logEnable) log.debug "In getRealDeviceData - Skipping for ${devId}"
	}
	if (!state.device && logEnable) log.warn "Device with ID ${devId} not found in masterDeviceList"
}

mappings {
    path("/runFlow") {
        action: [
            POST: "apiRunFlow"
        ]
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
