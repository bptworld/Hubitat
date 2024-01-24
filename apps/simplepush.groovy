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

def simplePushHandler() {
    if(logEnable) log.debug "In simplePush (${state.version})"

    def apiUrl = "https://api.simplepush.io/send"
    //def apiUrl = "https://simplepu.sh"

    def params = [
        'key': simpleKey,
        'title': 'Hubitat Notification',
        'msg': simpleMsg,
        'actions': ["yes", "no"]
    ]

    log.info "Simplepush - uri: ${apiUrl} - body: ${params}"
    try {
        httpPost(
            uri: apiUrl,
            body: params
        ) { response ->
            if(logEnable) log.debug "Simplepush - Received Status code: ${response.status}"
        }
    } catch (e) {
        log.error(getExceptionMessageWithLine(e))
    }
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "sendMsg") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        simplePushHandler()
    }
}
