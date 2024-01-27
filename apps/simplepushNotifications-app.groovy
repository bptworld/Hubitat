/**
 *  **************** Simplepush Notifications App ****************
 *
 *  Design Usage:
 *  Send Simplepush notifications to your phone with Actions!
 *
 *  Copyright 2024 Bryan Turcotte (@bptworld)
 *
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via:
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Thanks to the great work/additions by @TMLeafs
 *
 *  Changes:
 *  0.0.4 - 01/26/24 - Initial release
 */

def setVersion(){
    state.name = "Simplepush Notifications"
    state.version = "0.0.4"
}

def syncVersion(evt){
    setVersion()
    sendLocationEvent(name: "updateVersionsInfo", value: "${state.name}:${state.version}")
}

definition(
    name: "Simplepush Notifications",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Send Simplepush notifications to your phone with Actions!",
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
    
     def oauthStatus = ""
    //enable OAuth in the app settings or this call will fail
    try{
        if (!state.accessToken) {
            createAccessToken()
        }
    }
    catch (e) {
        oauthStatus = "Edit Apps Code -> Simplepush Noitications.  Select 'oAUTH' in the top right and use defaults to enable oAUTH to continue."
        if(logEnable) log.debug "{$oauthStatus}"
    }

    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        // if we didn't get a token, display the error and stop
        if (oauthStatus != "") {
            section("<h2>${oauthStatus}</h2>") {}
        } else if (state.installed != true) {
            section("<h3>Select '<b>Done</b>' to finsh the initial app installation and then re-select the Simplepush Noitications app to finish configuration.</h3>") {}
        } else {
        // Do not share your accessToken or OAuth Url when posting screenshots on Hubitat forums or anywhere else
        //if(logEnable) log.debug "The accessToken is: {$state.accessToken}"
        def extUri = fullApiServerUrl().replaceAll("null","webhook?access_token=${state.accessToken}")
        //if(logEnable) log.debug "The OAUTH Url is {$extUri}"
    
        display()
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
            paragraph "If you are not getting notifications on your phone, go to app preferences, and 'allow' notifications."
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:true, submitOnChange:true
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Devices")) {
            paragraph "Each 'Action' needs to have its own Virtual Device. An 'Action' can be things like 'Did you take your meds?', 'General announcements', etc. Try to use a logical nameing system per device/user.  Bryan Meds, Bryan Announcements, Amy Meds, ect."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have one created for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
                input "dataName", "text", title: "Add a new Vitual Device (ie. 'Simplepush - Bryan')", submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDev("Simplepush Notification Driver")
                if(statusMessageD == null) statusMessageD = " "
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Select ALL virtual devices created with this app", required:true, multiple:true, submitOnChange:true
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Simplepush Notification Driver'.</small>"
            }
            
            input "deleteDevice", "bool", title: "Delete a Virtual Device?", defaultValue:false, submitOnChange:true
            if(deleteDevice) {
                input "dDevice", "capability.actuator", title: "Select the Virtual Device created with this app to DELETE", submitOnChange:true
                
                if(dDevice) {
                    input "lastChance", "bool", title: "Delete device<br><small>* This can not be undone</small", defaultValue:false, submitOnChange:true
                    if(lastChance) {
                        deleteChildDevice(dDevice.deviceNetworkId)
                        log.info "Simplepush Notifications - ${dDevice.name} was deleted"
                        app.updateSetting("lastChance",[value:"false",type:"bool"])
                    }
                }
            }
        }
        
        if(logEnable) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Debug Option - Send a Test Message")) {
                paragraph "Only used to test the parent app."
                input "simpleKey", "text", title: "Simplepush Key", submitOnChange:true, width:6
                input "eventType", "text", title: "Event (Optional) - Set up in the phone app first.", sumbitOnChange:true, width:6
                input "simpleMsg", "text", title: "Message", submitOnChange:true
                input "sendMsg", "button", title: "Send Test Msg", width: 3
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app <small>(When selected switch is ON, app is disabled.)</small>", submitOnChange:true, required:false, multiple:true
        }
        display2()
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
    state.installed = true
}

def initialize() {
   if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        // Nothing
    }
}

def uninstalled() {
    sendLocationEvent(name: "updateVersionInfo", value: "${app.id}:remove")
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

mappings { 
    path("/webhook") { action: [ GET: "webhook"] }
}

def webhook() { 
    if(logEnable) log.info "In webhook - ${app.getLabel()} executing 'webhook()'"
    if(logEnable) log.info "In webhook - params: $params"
    if(logEnable) log.info "In webhook - action: $params.action"
    selectedAction = params.action
    childList = getChildDevices()
    childList.each { theD ->
        if(logEnable) log.info "In webhook - $theD.id vs. $state.theDevice"
        if(theD.id == state.theDevice) {
            theD.actionHandler(selectedAction)
        }
    }
        
    return render(contentType: "text/html", data: "webhook params:<br>$params <br><br>webhook request:<br>$request", status: 200)
}
 
def sendAsynchttpPost(theDevice, simpleKey, simpleMsg, title, eventType=null, actions=null) {
    if(logEnable) log.debug "In sendAsync - ${theDevice} - ${simpleKey} - ${title} - ${simpleMsg} - ${eventType} - ${actions}"
    state.theDevice = theDevice
    (action1, action2) = actions.split("-")
    def extUri = fullApiServerUrl().replaceAll("null","webhook?access_token=${state.accessToken}")
    if(actions) {
        theActions = [["name": "${action1}", "url": "${extUri}&action=on"],["name": "${action2}", "url": "${extUri}&action=off"]]
    } else {
        theActions = null
    }
    if(title) {
        theTitle = title
    } 
    else {
        theTitle = "Hubitat Notifcation"
    }

    def postParams = [
        uri: "https://simplepu.sh",
        requestContentType: 'application/json',
        contentType: 'application/json',
        body : ["key": simpleKey, "title": theTitle, "msg": simpleMsg, "event": eventType, "attachments": attach, "actions": theActions]
    ]
    if(logEnable) log.debug "In sendAsynchttpPost - ${postParams}"
	asynchttpPost('myCallbackMethod', postParams, [dataitem1: "datavalue1"])
}

def myCallbackMethod(response, data) {
    if(data["dataitem1"] == "datavalue1")
    	if(logEnable) log.debug "Data was passed successfully"
    if(logEnable) log.debug "In myCallbackMethod - status of post call is: ${response.status}"
}      

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "sendMsg") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
    sendAsynchttpPost("123", simpleKey, simpleMsg, eventType, actions)}
}

def createDataChildDev(driverName) {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", driverName, dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    app.removeSetting("dataName")
    return statusMessageD
}

// ********** Normal Stuff **********
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    setVersion()
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            theStatus = it.currentValue("switch")
            if(theStatus == "on") { state.eSwitch = true }
        }
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}"
    }
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=40>"
    if(type == "qmark") return "${loc}question-mark-icon.png height=16>"
    if(type == "qmark2") return "${loc}question-mark-icon-2.jpg height=16>"
}

def getFormat(type, myText=null, page=null) {			// Modified code from @Stephack
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid #000000;box-shadow: 2px 3px #80BC00;border-radius: 10px'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
    
    if(type == "button-blue") return "<a style='color:white;text-align:center;font-size:20px;font-weight:bold;background-color:#03FDE5;border:1px solid #000000;box-shadow:3px 4px #8B8F8F;border-radius:10px' href='${page}'>${myText}</a>"
}

def display(data) {
    if(data == null) data = ""
    if(app.label) {
        if(app.label.contains("(Paused)")) {
            theName = app.label - " <span style='color:red'>(Paused)</span>"
        } else {
            theName = app.label
        }
    }
    if(theName == null || theName == "") theName = "New Child App"
    if(!state.name) { state.name = "" }
    if(state.name == theName) {
        headerName = state.name
    } else {
        if(state.name == null || state.name == "") {
            headerName = "${theName}"
        } else {
            headerName = "${state.name} - ${theName}"
        }
    }
    section() {
        paragraph "<h2 style='color:#1A77C9;font-weight: bold'>${headerName}</h2><div style='color:#1A77C9'>Beta: Anything could change</div>"
        
        //<a href='https://community.hubitat.com/t/release-bundle-manager-the-only-place-to-find-bptworld-bundles-find-install-and-update-bundles-quickly-and-easily/94567/295' target='_blank'>Bundle Manager</a>!
        
        paragraph getFormat("line")
    }
}

def display2() {
    setVersion()
    section() {
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" }
        paragraph getFormat("line")
        if(state.version) {
            bMes = "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}"
        } else {
            bMes = "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name}"
        }
        bMes += "</div>"
        paragraph "${bMes}"
        paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
    }
}
