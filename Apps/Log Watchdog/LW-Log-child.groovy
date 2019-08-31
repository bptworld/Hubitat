/**
 *  ****************  Log Watchdog Child App  ****************
 *
 *  Design Usage:
 *  Keep an eye on what's important in the log.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
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
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.0 - 08/31/19 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "LogWatchdogChildVersion"
	state.version = "v2.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Log Watchdog Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Simple Device Timer with safety checks, multiple timers, notifications and restrictions.",
    category: "Convenience",
	parent: "BPTWorld:Log Watchdog",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Log Watchdog</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Keep an eye on what's important in the log."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
			paragraph "<b>Main Check</b> - Select 1 keywords or phrase"
            input "keyword1", "text", title: "Main Keyword 1",  required: true, submitOnChange: "true"
            paragraph "<b>AND</b>"   
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword1", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword2", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword3", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword4", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            
            if(!keyword1) mKeyOptions = ""
            if(!oBetween) oBetween = ""
            
            if(!sKeyword1) {
                sKeyword1 = ""
                and1 = ""
                finish1 = ""
            } else {
                and1 = " and ("
                finish1 = ")"
            }
            
            if(!sKeyword2) {
                sKeyword2 = ""
                or2 = ""
            } else {
                or2 = " or "
            }
            
            if(!sKeyword3) {
                sKeyword3 = ""
                or3 = ""
            } else {
                or3 = " or "
            }
            
            if(!sKeyword4) {
                sKeyword4 = ""
                or4 = ""
            } else {
                or4 = " or "
            }
            
            paragraph "<b>Complete Check</b>"
            paragraph "<b>if (${keyword1})${and1}${sKeyword1}${or2}${sKeyword2}${or3}${sKeyword3}${or4}${sKeyword4}${finish1}</b>"
            
            if(!sKeyword1) sKeyword1 = "-";if(!sKeyword2) sKeyword2 = "-"; if(!sKeyword3) sKeyword3 = "-";if(!sKeyword4) sKeyword4 = "-";
            state.theData = "${keyword1};${sKeyword1};${sKeyword2};${sKeyword3};${sKeyword4}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            paragraph "Remember, depending on your keyword settings, this could produce a lot of notifications!"
			input "sendPushMessage", "capability.notification", title: "Send a push notification?", multiple: true, required: false
            
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Watchdog Device")) {
			input "lwdDevice", "capability.actuator", title: "Select Log Watchdog Device", required: true
            paragraph "<small>* Virtual Device must use the Log Watchdog Driver</small>"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Tracking Status")) {
            try {
                if(lwdDevice) status = lwdDevice.currentValue("status")
            }
            catch(e) {
                theStatus = "Unknown"
            }
            if(status == "Open") {
                theStatus = "Connected"
            } else {
                theStatus = "Disconnected"
            }
            paragraph "This will control whether the app is actively 'watching' the log or not."
            paragraph "Current Log Watchdog status: <b>${theStatus}</b>", width: 6
            input "openConnection", "button", title: "Connect", width: 3
            input "closeConnection", "button", title: "Disconnect", width: 3
        }
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    sendToDevice()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
    subscribe(lwdDevice, "lastLogMessage", theNotifyStuff)
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}
	
def countWords(stuff) {
    if(logEnable) log.info "In countWords"
    def values = stuff.split(";")
    wordCount = values.size()
    return wordCount
}

def sendToDevice() {
    if(logEnable) log.info "In sendToDriver"
    lwdDevice.keywordInfo(state.theData) 
}

def theNotifyStuff(evt) {
    if(logEnable) log.debug "In theNotifyStuff"
    //log.info "theNotifyStuff - could push or talk if selected"
    if(sendPushMessage) pushHandler()
}

def pushHandler(){
	if(logEnable) log.debug "In pushNow"
	theMessage = "${app.label} - ${state.msg}"
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "openConnection"){
        lwcDevice.connect()
    }
    if(state.whichButton == "closeConnection"){
        lwcDevice.close()
    }
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Log Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
