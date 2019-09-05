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
 *  V2.0.4 - 09/05/19 - More code changes... this is a beta app ;)
 *  V2.0.3 - 09/04/19 - Fixed some typos
 *  V2.0.2 - 09/03/19 - Added 'does not contain' keywords
 *  V2.0.1 - 09/02/19 - Evolving fast, lots of changes
 *  V2.0.0 - 08/31/19 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "LogWatchdogChildVersion"
	state.version = "v2.0.4"
    
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
    description: "Keep an eye on what's important in the log.",
    category: "Convenience",
	parent: "BPTWorld:Log Watchdog",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Log%20Watchdog/LW-Log-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "pageKeySet01", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "pageKeySet02", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "pageKeySet03", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "pageKeySet04", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "pageKeySet05", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Log Watchdog</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Keep an eye on what's important in the log."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Keyset Options")) {
            href "pageKeySet01", title: "Keyset 01 Setup", description: "Click here to setup Keywords."
            if(state.if01) paragraph "Keyset 01: ${state.if01}"
            href "pageKeySet02", title: "Keyset 02 Setup", description: "Click here to setup Keywords."
            if(state.if02) paragraph "Keyset 02: ${state.if02}"
            href "pageKeySet03", title: "Keyset 03 Setup", description: "Click here to setup Keywords."
            if(state.if03) paragraph "Keyset 03: ${state.if03}"
            href "pageKeySet04", title: "Keyset 04 Setup", description: "Click here to setup Keywords."
            if(state.if04) paragraph "Keyset 04: ${state.if04}"
            href "pageKeySet05", title: "Keyset 05 Setup", description: "Click here to setup Keywords."
            if(state.if05) paragraph "Keyset 05: ${state.if05}"
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
            input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
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

def pageKeySet01(){
    dynamicPage(name: "pageKeySet01", title: "Keyset 01 Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
            input "option1", "enum", title: "Select a Opton to 'Watch'", required: true, submitOnChange: true, options: ["Logging Level","Keywords"]
            
            if(option1 == "Keywords") {
                keySetType1 = "K"
			    paragraph "<b>Primary Check</b> - Select keyword or phrase"
                input "keyword11", "text", title: "Primary Keyword 1",  required: false, submitOnChange: "true"
            } else if(option1 == "Logging Level") {
                keySetType1 = "L"
                paragraph "<b>Primary Check</b> - Select logging level"
                input "keyword11", "enum", title: "Select a Logging Level to 'Watch'", required: false, multiple: false, submitOnChange: true, options: ["Trace","Debug","Info","Warn","Error"]
            }
            paragraph "<b>AND</b>"   
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword11", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword12", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword13", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword14", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<b>BUT DOES NOT CONTAIN</b>"   
            paragraph "<b>Third Check</b> - Select up to 2 keywords"
            input "nKeyword11", "text", title: "Third Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "nKeyword12", "text", title: "Third Keyword 2",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            if(!keyword11) keyword11 = "-"
            if(!sKeyword11) sKeyword11 = "-"
            if(!sKeyword12) sKeyword12 = "-"
            if(!sKeyword13) sKeyword13 = "-"
            if(!sKeyword14) sKeyword14 = "-"
            if(!nKeyword11) nKeyword11 = "-"
            if(!nKeyword12) nKeyword12 = "-"
            
            state.if01 = "<b>(${keySetType1}) if (${keyword11}) and (${sKeyword11} or ${sKeyword12} or ${sKeyword13} or ${sKeyword14}) but not (${nKeyword11} or ${nKeyword12})</b>"
            paragraph "<b>Complete Check</b><br>${state.if01}"

            state.theData01 = "keySet01;${keySetType1};${keyword11};${sKeyword11};${sKeyword12};${sKeyword13};${sKeyword14};${nKeyword11};${nKeyword12}"
        }
    }
}

def pageKeySet02(){
    dynamicPage(name: "pageKeySet02", title: "Keyset 02 Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
            input "option2", "enum", title: "Select a Opton to 'Watch'", required: true, submitOnChange: true, options: ["Logging Level","Keywords"]
            
            if(option2 == "Keywords") {  
                keySetType2 = "K"
			    paragraph "<b>Primary Check</b> - Select keyword or phrase"
                input "keyword21", "text", title: "Primary Keyword 1",  required: false, submitOnChange: "true"
            } else if(option2 == "Logging Level") {
                keySetType2 = "L"
                paragraph "<b>Primary Check</b> - Select logging level"
                input "keyword21", "enum", title: "Select a Logging Level to 'Watch'", required: false, multiple: false, submitOnChange: true, options: ["Trace","Debug","Info","Warn","Error"]
            }
            paragraph "<b>AND</b>"    
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword21", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword22", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword23", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword24", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<b>BUT DOES NOT CONTAIN</b>"   
            paragraph "<b>Third Check</b> - Select up to 2 keywords"
            input "nKeyword21", "text", title: "Third Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "nKeyword22", "text", title: "Third Keyword 2",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            if(!keyword21) keyword21 = "-"
            if(!sKeyword21) sKeyword21 = "-"
            if(!sKeyword22) sKeyword22 = "-"
            if(!sKeyword23) sKeyword23 = "-"
            if(!sKeyword24) sKeyword24 = "-"
            if(!nKeyword21) nKeyword21 = "-"
            if(!nKeyword22) nKeyword22 = "-"
            
            state.if02 = "<b>(${keySetType2}) if (${keyword21}) and (${sKeyword21} or ${sKeyword22} or ${sKeyword23} or ${sKeyword24}) but not (${nKeyword21} or ${nKeyword22})</b>"
            paragraph "<b>Complete Check</b><br>${state.if02}"
            
            state.theData02 = "keySet02;${keySetType2};${keyword21};${sKeyword21};${sKeyword22};${sKeyword23};${sKeyword24};${nKeyword21};${nKeyword22}"
        }
    }
}

def pageKeySet03(){
    dynamicPage(name: "pageKeySet03", title: "Keyset 03 Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
			input "option3", "enum", title: "Select a Opton to 'Watch'", required: true, submitOnChange: true, options: ["Logging Level","Keywords"]
            
            if(option3 == "Keywords") {  
                keySetType3 = "K"
			    paragraph "<b>Primary Check</b> - Select keyword or phrase"
                input "keyword31", "text", title: "Primary Keyword 1",  required: false, submitOnChange: "true"
            } else if(option3 == "Logging Level") {
                keySetType3 = "L"
                paragraph "<b>Primary Check</b> - Select logging level"
                input "keyword31", "enum", title: "Select a Logging Level to 'Watch'", required: false, multiple: false, submitOnChange: true, options: ["Trace","Debug","Info","Warn","Error"]
            }
            paragraph "<b>AND</b>"    
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword31", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword32", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword33", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword34", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<b>BUT DOES NOT CONTAIN</b>"   
            paragraph "<b>Third Check</b> - Select up to 2 keywords"
            input "nKeyword31", "text", title: "Third Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "nKeyword32", "text", title: "Third Keyword 2",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            if(!keyword31) keyword31 = "-"
            if(!sKeyword31) sKeyword31 = "-"
            if(!sKeyword32) sKeyword32 = "-"
            if(!sKeyword33) sKeyword33 = "-"
            if(!sKeyword34) sKeyword34 = "-"
            if(!nKeyword31) nKeyword31 = "-"
            if(!nKeyword32) nKeyword32 = "-"
            
            state.if03 = "<b>(${keySetType3}) if (${keyword31}) and (${sKeyword31} or ${sKeyword32} or ${sKeyword33} or ${sKeyword34}) but not (${nKeyword31} or ${nKeyword32})</b>"
            paragraph "<b>Complete Check</b><br>${state.if03}"
       
            state.theData03 = "keySet03;${keySetType3};${keyword31};${sKeyword31};${sKeyword32};${sKeyword33};${sKeyword34};${nKeyword31};${nKeyword32}"
        }
    }
}

def pageKeySet04(){
    dynamicPage(name: "pageKeySet04", title: "Keyset 04 Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
			input "option4", "enum", title: "Select a Opton to 'Watch'", required: true, submitOnChange: true, options: ["Logging Level","Keywords"]
            
            if(option4 == "Keywords") { 
                keySetType4 = "K"
			    paragraph "<b>Primary Check</b> - Select keyword or phrase"
                input "keyword41", "text", title: "Primary Keyword 1",  required: false, submitOnChange: "true"
            } else if(option4 == "Logging Level") {
                keySetType4 = "L"
                paragraph "<b>Primary Check</b> - Select logging level"
                input "keyword41", "enum", title: "Select a Logging Level to 'Watch'", required: false, multiple: false, submitOnChange: true, options: ["Trace","Debug","Info","Warn","Error"]
            }
            paragraph "<b>AND</b>"    
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword41", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword42", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword43", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword44", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<b>BUT DOES NOT CONTAIN</b>"   
            paragraph "<b>Third Check</b> - Select up to 2 keywords"
            input "nKeyword41", "text", title: "Third Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "nKeyword42", "text", title: "Third Keyword 2",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            if(!keyword41) keyword41 = "-"
            if(!sKeyword41) sKeyword41 = "-"
            if(!sKeyword42) sKeyword42 = "-"
            if(!sKeyword43) sKeyword43 = "-"
            if(!sKeyword44) sKeyword44 = "-"
            if(!nKeyword41) nKeyword41 = "-"
            if(!nKeyword42) nKeyword42 = "-"
            
            state.if04 = "<b>(${keySetType4}) if (${keyword41}) and (${sKeyword41} or ${sKeyword42} or ${sKeyword43} or ${sKeyword44}) but not (${nKeyword41} or ${nKeyword42})</b>"
            paragraph "<b>Complete Check</b><br>${state.if04}"

            state.theData04 = "keySet04;${keySetType4};${keyword41};${sKeyword41};${sKeyword42};${sKeyword43};${sKeyword44};${nKeyword41};${nKeyword42}"
        }
    }
}

def pageKeySet05(){
    dynamicPage(name: "pageKeySet05", title: "Keyset 05 Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
			input "option5", "enum", title: "Select a Opton to 'Watch'", required: true, submitOnChange: true, options: ["Logging Level","Keywords"]
            
            if(option5 == "Keywords") {  
                keySetType5 = "K"
			    paragraph "<b>Primary Check</b> - Select keyword or phrase"
                input "keyword51", "text", title: "Primary Keyword 1",  required: false, submitOnChange: "true"
            } else if(option5 == "Logging Level") {
                keySetType5 = "L"
                paragraph "<b>Primary Check</b> - Select logging level"
                input "keyword51", "enum", title: "Select a Logging Level to 'Watch'", required: false, multiple: false, submitOnChange: true, options: ["Trace","Debug","Info","Warn","Error"]
            }
            paragraph "<b>AND</b>"    
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword51", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword52", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword53", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword54", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<b>BUT DOES NOT CONTAIN</b>"   
            paragraph "<b>Third Check</b> - Select up to 2 keywords"
            input "nKeyword51", "text", title: "Third Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "nKeyword52", "text", title: "Third Keyword 2",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            if(!keyword51) keyword51 = "-"
            if(!sKeyword51) sKeyword51 = "-"
            if(!sKeyword52) sKeyword52 = "-"
            if(!sKeyword53) sKeyword53 = "-"
            if(!sKeyword54) sKeyword54 = "-"
            if(!nKeyword51) nKeyword51 = "-"
            if(!nKeyword52) nKeyword52 = "-"
           
            state.if05 = "<b>(${keySetType5}) if (${keyword51}) and (${sKeyword51} or ${sKeyword52} or ${sKeyword53} or ${sKeyword54}) but not (${nKeyword51} or ${nKeyword52})</b>"
            paragraph "<b>Complete Check</b><br>${state.if05}"

            state.theData05 = "keySet05;${keySetType5};${keyword51};${sKeyword51};${sKeyword52};${sKeyword53};${sKeyword54};${nKeyword51};${nKeyword52}"
        }
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
    if(state.theData01) {
        lwdDevice.keywordInfo(state.theData01) 
        log.info "Log Watchdog - Sending theData01"
    }
    if(state.theData02) {
        lwdDevice.keywordInfo(state.theData02)
        log.info "Log Watchdog - Sending theData02"
    }
    if(state.theData03) {
        lwdDevice.keywordInfo(state.theData03)
        log.info "Log Watchdog - Sending theData03"
    }
    if(state.theData04) {
        lwdDevice.keywordInfo(state.theData04)
        log.info "Log Watchdog - Sending theData04"
    }
    if(state.theData05) {
        lwdDevice.keywordInfo(state.theData05)
        log.info "Log Watchdog - Sending theData05"
    }
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
        lwdDevice.connect()
    }
    if(state.whichButton == "closeConnection"){
        lwdDevice.close()
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
