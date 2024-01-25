/**
 *  **************** Simplepush App ****************
 * Barebones app to test the Simplepush API
 *
 *  As always, You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. Have FUN!
 *
 * Be sure to enable OAuth
 *
 * Thanks to the great work/additions from @TMLeafs
 */

definition(
    name: "Simplepush",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Barebones app to test the Simplepush API",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page name: "pageConfig"
}

def pageConfig() {
    // Do not share your accessToken or OAuth Url when posting screenshots on Hubitat forums or anywhere else
    if(logEnable) log.debug "The accessToken is: {$state.accessToken}"
    def extUri = fullApiServerUrl().replaceAll("null","webhook?access_token=${state.accessToken}")
    if(logEnable) log.debug "The OAUTH Url is {$extUri}"
    

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
    def oauthStatus = ""
    //enable OAuth in the app settings or this call will fail
    try{
        if (!state.accessToken) {
            createAccessToken()
        }
    }
    catch (e) {
        oauthStatus = "Edit Apps Code -> Simplepush.  Select 'oAUTH' in the top right and use defaults to enable oAUTH to continue."
        logError(oauthStatus)
    }
        
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        // Nothing
    }
}

mappings { 
    path("/webhook") { action: [ GET: "webhook"] }
}

def webhook() { 
    if(logEnable) log.info "${app.getLabel()} executing 'webhook()'"
    if(logEnable) log.info "params: $params"   
    return render(contentType: "text/html", data: "webhook params:<br>$params <br><br>webhook request:<br>$request", status: 200)
}
 
def sendAsynchttpPost() {
    def extUri = fullApiServerUrl().replaceAll("null","webhook?access_token=${state.accessToken}")
    def postParams = [
        uri: "https://simplepu.sh",
        requestContentType: 'application/json',
        contentType: 'application/json',
        body : ["key": simpleKey, "title": "Hubitat Notification", "msg": simpleMsg, "actions": [["name": "yes", "url": "${extUri}&action=yes"],["name": "no", "url": "${extUri}&action=no"]]]
    ]
    if(logEnable) log.debug "In sendAsynchttpPost - ${postParams}"
	asynchttpPost('myCallbackMethod', postParams, [dataitem1: "datavalue1"])
}

def myCallbackMethod(response, data) {
    if(data["dataitem1"] == "datavalue1")
    	if(logEnable) log.debug "Data was passed successfully"
    if(logEnable) log.debug "status of post call is: ${response.status}"
}      

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "sendMsg") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
    sendAsynchttpPost()}
}
