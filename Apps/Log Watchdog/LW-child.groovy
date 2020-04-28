/**
 *  ****************  Log Watchdog Child App  ****************
 *
 *  Design Usage:
 *  Keep an eye on what's important in the log.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  2.0.7 - 04/27/20 - Cosmetic changes
 *  2.0.6 - 11/24/19 - Code enhancements
 *  2.0.5 - 10/08/19 - Reduce child apps to just one keyset to prevent run away conditions 
 *  2.0.4 - 09/05/19 - More code changes... this is a beta app ;)
 *  2.0.3 - 09/04/19 - Fixed some typos
 *  2.0.2 - 09/03/19 - Added 'does not contain' keywords
 *  2.0.1 - 09/02/19 - Evolving fast, lots of changes
 *  2.0.0 - 08/31/19 - Initial release.
 *
 */

def setVersion(){
    state.name = "Log Watchdog"
	state.version = "2.0.7"
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
    page name: "pageKeySet", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Keep an eye on what's important in the log."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Keyset Options")) {
            href "pageKeySet", title: "Keyset Setup", description: "Click here to setup Keywords."
            if(state.if01) paragraph "Keyset: ${state.if01}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            paragraph "Remember, depending on your keyword settings, this could produce a lot of notifications!"
			input "sendPushMessage", "capability.notification", title: "Send a push notification?", multiple: true, required: false
            
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Watchdog Device - Required")) {
			input "lwdDevice", "capability.actuator", title: "Select Log Watchdog Device", required: false
            paragraph "<small>* Virtual Device must use the Log Watchdog Driver</small>"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Debugging", submitOnChange:true
            paragraph "<hr>"
            input "testPrimaryKeyword", "button", title: "Test Pri Keyword 1"
            
            input "testSecondaryKeyword1", "button", title: "Test Sec Keyword 1", width: 3
            input "tesSecondarytKeyword2", "button", title: "Test Sec Keyword 2", width: 3
            input "testSecondaryKeyword3", "button", title: "Test Sec Keyword 3", width: 3
            input "testSecondaryKeyword4", "button", title: "Test Sec Keyword 4", width: 3
            paragraph "<hr>"
            
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

def pageKeySet(){
    dynamicPage(name: "pageKeySet", title: "Keyset 01 Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Keywords")) {
            input "option1", "enum", title: "Select a Opton to 'Watch'", required: true, submitOnChange: true, options: ["Logging Level","Keywords"]
            
            if(option1 == "Keywords") {
                keySetType1 = "K"
			    paragraph "<b>Primary Check</b> - Select keyword or phrase"
                input "keyword1", "text", title: "Primary Keyword 1",  required: false, submitOnChange: "true"
            } else if(option1 == "Logging Level") {
                keySetType1 = "L"
                paragraph "<b>Primary Check</b> - Select logging level"
                input "keyword1", "enum", title: "Select a Logging Level to 'Watch'", required: false, multiple: false, submitOnChange: true, options: ["Trace","Debug","Info","Warn","Error"]
            }
            paragraph "<b>AND</b>"   
            paragraph "<b>Secondary Check</b> - Select up to 4 keywords"
            input "sKeyword1", "text", title: "Secondary Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "sKeyword2", "text", title: "Secondary Keyword 2",  required: false, submitOnChange: "true", width: 6
            input "sKeyword3", "text", title: "Secondary Keyword 3",  required: false, submitOnChange: "true", width: 6
            input "sKeyword4", "text", title: "Secondary Keyword 4",  required: false, submitOnChange: "true", width: 6
            paragraph "<b>BUT DOES NOT CONTAIN</b>"   
            paragraph "<b>Third Check</b> - Select up to 2 keywords"
            input "nKeyword1", "text", title: "Third Keyword 1",  required: false, submitOnChange: "true", width: 6
            input "nKeyword2", "text", title: "Third Keyword 2",  required: false, submitOnChange: "true", width: 6
            paragraph "<hr>"
            if(!keyword1) keyword1 = "-"
            if(!sKeyword1) sKeyword1 = "-"
            if(!sKeyword2) sKeyword2 = "-"
            if(!sKeyword3) sKeyword3 = "-"
            if(!sKeyword4) sKeyword4 = "-"
            if(!nKeyword1) nKeyword1 = "-"
            if(!nKeyword2) nKeyword2 = "-"
            
            state.if01 = "<b>(${keySetType1}) - if (${keyword1}) and (${sKeyword1} or ${sKeyword2} or ${sKeyword3} or ${sKeyword4}) but not (${nKeyword1} or ${nKeyword2})</b>"
            paragraph "<b>Complete Check</b><br>${state.if01}"

            state.theData01 = "KeySet;${keySetType1};${keyword1};${sKeyword1};${sKeyword2};${sKeyword3};${sKeyword4};${nKeyword1};${nKeyword2}"
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
    if(state.whichButton == "testPrimaryKeyword"){
        log.info "Log Watchdog - Testing Primary Keyword1: ${keyword1}"
    }
    if(state.whichButton == "testSecondaryKeyword1"){
        log.info "Log Watchdog - Testing Secondary Keyword1: ${sKeyword1}"
    }
    if(state.whichButton == "testSecondaryKeyword2"){
        log.info "Log Watchdog - Testing Secondary Keyword2: ${sKeyword2}"
    }
    if(state.whichButton == "testSecondaryKeyword3"){
        log.info "Log Watchdog - Testing Secondary Keyword3: ${sKeyword3}"
    }
    if(state.whichButton == "testSecondaryKeyword4"){
        log.info "Log Watchdog - Testing Secondary Keyword4: ${sKeyword4}"
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
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
		paragraph getFormat("line")
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
	}       
}

def getHeaderAndFooter() {
    if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
    def params = [
	    uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 30
	]
    
    try {
        def result = null
        httpGet(params) { resp ->
            state.headerMessage = resp.data.headerMessage
            state.footerMessage = resp.data.footerMessage
        }
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
