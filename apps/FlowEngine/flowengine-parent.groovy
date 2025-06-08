/**
 *  **************** Flow Engine Parent ****************
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
 *  Changes are listed in child app
 */

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Field
import groovy.json.JsonOutput

definition(
    name:"Flow Engine",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Feel the Flow",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
	oauth: true
)

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			display()
			
			section(getFormat("header-green", " Child Apps")) {
                app(name: "anyOpenApp", appName: "Flow Engine Child", namespace: "BPTWorld", title: "<b>Add a new 'Flow Engine' child</b>", multiple: true)
			}
			
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
			
			section(getFormat("header-green", " Flow Engine Editor Infomation")) {
				input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
				if(logEnable) {
					input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
				}
			}
			display2()
		}
	}
}

def installed() {
    if(logEnable) log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    //if(logEnable) log.debug "Updated with settings: ${settings}"
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
	if (!state.accessToken) {
        createAccessToken()
		state.lastFullUrl = "http://${location.hub.localIP}:80/apps/api/${app.id}/flow?access_token=${state.accessToken}"
		state.appId = app.id
		state.token = state.accessToken
    }
	notifyFlowTrace(flowFile=null, nodeId=null, nodeType=null)
	exportModesToFile()
}
	
def installCheck(){
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		display()
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  	}
  	else{
    	if(logEnable) log.info "Parent Installed OK"
  	}
}

mappings {
    path("/runFlow") 	 { action: [POST: "handleFlow" ] }
	path("/listFiles") 	 { action: [GET: "apiListFiles"] }
    path("/getFile")  	 { action: [GET: "apiGetFile"] }
	path("/testTile") 	 { action: [POST: "testTileHandler"] }
	path("/devices")     { action: [GET: "apiGetDevices"] }
	path("/uploadFile")  { action: [POST: "apiUploadFile"] }
	path("/getModes")  	 { action: [POST: "exportModesToFile"] }
}

def exportModesToFile() {
	def currentMode = [ id: "current", name: location.mode]
    def modeList = location.modes.collect { [ id: it.id, name: it.name ] } + currentMode
    def json = groovy.json.JsonOutput.toJson([ modes: modeList ])
	uploadHubFile("flowModes.json",json.getBytes())
}

void notifyFlowTrace(flowFile, nodeId, nodeType) {
    if (!flowFile) return

    // Track flows by runId (or generate one if missing)
    if (!state.activeFlows) state.activeFlows = []

    // When eventTrigger or new flow, start a new runId
    def runId = state.lastRunId
    if (nodeType == "eventTrigger" || !runId || state.lastFlowFile != flowFile) {
        runId = "${now()}_${new Random().nextInt(1000000)}"
        state.lastRunId = runId
        state.lastFlowFile = flowFile
        // Remove finished/old runs if needed (optional, e.g. keep last 3)
        if (state.activeFlows.size() > 2) state.activeFlows = state.activeFlows[-2..-1]
        // Add a new active flow path
        state.activeFlows << [ runId: runId, flowFile: flowFile, steps: [] ]
    }

    // Find the active flow object for this run
    def thisFlow = state.activeFlows.find { it.runId == runId }
    if (!thisFlow) {
        thisFlow = [ runId: runId, flowFile: flowFile, steps: [] ]
        state.activeFlows << thisFlow
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
    saveFlow("flowtrace.json", state.activeFlows)
}

def apiGetDevices() {
    // Only allow with token
    if (!state.accessToken || params.access_token != state.accessToken) {
        render status: 401, text: "Unauthorized"
        return
    }
    def output = []
    masterDeviceList?.each { dev ->
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

void saveFlow(fName, fData) {
    if(logEnable) log.debug "Saving to file - ${fName}"
	String listJson = JsonOutput.toJson(fData) as String
	uploadHubFile("${fName}",listJson.getBytes())
}

def apiGetFile() {
    log.debug "In apiGetFile"
    def name = params.name
    if (!name) {
        log.debug "In apiGetFile - Missing file name"
        render status: 400, text: "Missing file name"
        return
    }
    def fileData = null
    try {
        def url = "http://${location.hub.localIP}:8080/local/${name}"
        log.debug "Fetching file via httpGet: ${url}"
        httpGet([uri: url, contentType: 'text/plain']) { resp ->
            fileData = resp.data?.text
        }
        if (!fileData) {
            log.debug "In apiGetFile - File not found or empty"
            render status: 404, text: "File not found or empty"
            return
        }
        try {
            def obj = new groovy.json.JsonSlurper().parseText(fileData)
            render contentType: "application/json", data: groovy.json.JsonOutput.toJson(obj)
        } catch (ex) {
            log.debug "In apiGetFile - returning as text"
            render contentType: "text/plain", text: fileData
        }
    } catch (e) {
        log.debug "In apiGetFile - 500 error: ${e}"
        render status: 500, text: "Error: ${e}"
    }
}



def apiListFiles() {
    if(logEnable) log.debug "Getting list of files"
    uri = "http://${location.hub.localIP}:8080/hub/fileManager/json";
    def params = [
        uri: uri,
        headers: [
				"Cookie": cookie
            ]        
    ]
    try {
        fileList = []
        httpGet(params) { resp ->
            if (resp != null){
                log.debug "Found the files"
                def json = resp.data
                for (rec in json.files) {
					if (rec.name?.toLowerCase()?.endsWith(".json")) {
                    	fileList << rec.name
					}
                }
            } else {
                //
            }
        }
        if(logEnable) log.debug fileList.sort()
    } catch (e) {
        log.error e
    }
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson([files: fileList])
}

def getDeviceById(id) {
	if(logDebug) log.debug "In getDeviceById - ${id}"
    theDevice = settings.masterDeviceList?.find { it.id.toString() == id?.toString() }
	if(logDebug) log.debug "In getDeviceById - Returning: ${theDevice.deviceLabel}"
	return theDevice
}

def testTileHandler() {
    try {
        def slurper = new groovy.json.JsonSlurper()
        def req = request.JSON ?: slurper.parseText(request?.body ?: "{}")
        def node = req.node
        if (logEnable) log.info "In testTileHandler: Received node: ${node?.name}, id=${node?.id}"
        // Minimal emulation: process node as if running in a flow
        switch (node?.name) {
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
                devIds.each { devId ->
                    def device = getDeviceById(devId)
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
					}
                }
                render contentType: "text/plain", data: output ? output.join("; ") : "No device command executed."
                return
            case "condition":
                def device = getDeviceById(node.data.deviceId)
                if (!device) {
                    render contentType: "text/plain", data: "Device not found"
                    return
                }
                def attrVal = device.currentValue(node.data.attribute)
                def passes = evaluateComparator(attrVal, node.data.value, node.data.comparator)
                render contentType: "text/plain", data: "Condition result: ${passes} (current ${node.data.attribute}: ${attrVal})"
                return
            case "eventTrigger":
                render contentType: "text/plain", data: "Test not implemented for eventTrigger (needs event context)."
                return
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

            // Add other node types if you want
            default:
                render contentType: "text/plain", data: "Node type ${node?.name} not supported for test."
        }
    } catch (ex) {
        log.error "TestTileHandler error: $ex"
        render contentType: "text/plain", data: "TestTileHandler error: $ex"
    }
}

def apiUploadFile() {
    // Require token
    if (!state.accessToken || params.access_token != state.accessToken) {
        render status: 401, text: "Unauthorized"
        return
    }
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
        // Handle both raw body and JSON (most browsers send JSON)
        def fileText = (body instanceof String) ? body : groovy.json.JsonOutput.toJson(body)
        uploadHubFile(name, fileText.getBytes("UTF-8"))
        render contentType: "application/json", data: groovy.json.JsonOutput.toJson([ok: true])
    } catch (e) {
        log.error "apiUploadFile error: $e"
        render status: 500, text: "Error: $e"
    }
}

def getFormat(type, myText=null, page=null) {
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid #000000;box-shadow: 2px 3px #8B8F8F;border-radius: 5px'>${myText}</div>"
	if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />"
}

def display() {
    section() {
        paragraph getFormat("line")
		label title: "Enter a name for this automation", required:false, submitOnChange:true
    }
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
    }
}
