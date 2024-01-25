/**
 *  **************** Simplepush App ****************
 * Barebones app to test the Simplepush API
 */

definition(
    name: "Simplepush",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Barebones app to test the Simplepush API",
    category: "Convenience",
	//parent: "BPTWorld:Simplepush",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page name: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        section() {
            paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Simplepush Test</h2>"
        }
        
        section(){
            label title: "Enter a name for this automation", required:true, submitOnChange:true
        }
        
        section(" Options") {
            input "simpleKey", "text", title: "Simplepush Key", required:true, submitOnChange:true
            input "simpleMsg", "text", title: "Message", required:true, submitOnChange:true
            
            input "sendMsg", "button", title: "Send Test Msg", width: 3
            input "checkMsg", "button", title: "Check for Respnse", width: 3
            
        }

        section(" General") {
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	updated()
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
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        // Nothing
    }
}
  
def sendAsynchttpPost() {
    def postParams = [
		uri: "https://simplepu.sh",
		requestContentType: 'application/json',
		contentType: 'application/json',
        body : ["key": simpleKey, "title": "Hubitat Notification", "msg": simpleMsg, "actions": ["yes","no"]]
        ]
    if(logEnable) log.debug "In sendAsynchttpPost - ${postParams}"
	asynchttpPost('myCallbackMethod', postParams, [dataitem1: "datavalue1"])
}

def myCallbackMethod(response, data) {
    if(data["dataitem1"] == "datavalue1") {
    	if(logEnable) log.debug "In myCallbackMethod - Data was passed successfully"
    }
    if(logEnable) log.debug "In myCallbackMethod - Status: ${response.status} - Data: ${response.data}"
    
    // {"status":"OK","feedbackId":"123456789"}
    (theStatus,theId) = response.data.split(",")
    (theTitle,fbId) = theId.split(":")
    state.lastId = fbId.replaceAll("\"","").replaceAll("}","")
    if(logEnable) log.debug "In myCallbackMethod - lastId: ${state.lastId}"

}      

def actionHandler() {
    if(logEnable) log.debug "-----------------------------------------------"
    if(logEnable) log.debug "In actionHandler (${state.version})"

    def apiUrl = "https://simplepu.sh/1/feedback/${state.lastId}"
    if(logEnable) log.debug "actionHandler - uri: ${apiUrl}"
    try {
        httpGet(
            uri: apiUrl
        ) { response ->
            // [action_delivered_at:1706187255, action_selected:yes, action_selected_at:1706187256, success:true]
            if(logEnable) log.debug "actionHandler - Received: ${response.data}"
            if(response.data.action_selected) {
                if(logEnable) log.debug "actionHandler - Action Selected: ${response.data.action_selected}"
            } else {
                if(logEnable) log.debug "actionHandler - Action Selected: No Action has been selected"
            }
        }
    } catch (e) {
        log.error(getExceptionMessageWithLine(e))
    }
    if(logEnable) log.debug "-----------------------------------------------"
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "sendMsg") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        sendAsynchttpPost()
    } else if(buttonPressed == "checkMsg") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        actionHandler()
    }
}
