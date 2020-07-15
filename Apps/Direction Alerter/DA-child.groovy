/**
 *  ****************  Direction Alerter Child App  ****************
 *
 *  Design Usage:
 *  Get notified on the direction something is moving in. Great for a Driveway Alert with direction.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
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
 *  1.0.0 - 06/20/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Direction Alerter"
	state.version = "1.0.0"
}

definition(
    name: "Direction Alerter Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Get notified on the direction something is moving in. Great for a Driveway Alert with direction.",
    category: "Convenience",
	parent: "BPTWorld:Direction Alerter",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
    page(name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        display()
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Get notified on the direction something is moving in. Great for a Driveway Alert with direction."
		}
               
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the data."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have DA create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this Data Device (ie. 'DA - Driveway')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device to send the data to", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Direction Alerter Driver'.</small>"
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Options")) {
            paragraph "If motion 1 triggers before motion 2 - Direction is considered <b>Right</b><br>If motion 2 triggers before motion 1 - Direction is considered <b>Left</b>"
            input "motion1", "capability.motionSensor", title: "Motion Sensor 1", mulitple:false, required:true, submitOnChange:true
            input "motion2", "capability.motionSensor", title: "Motion Sensor 2", mulitple:false, required:true, submitOnChange:true
            paragraph "Note: If the wrong direction is reported, simply reverse the two motion inputs."
            
            if(fmSpeaker) {
                href "speechOptions", title:"${getImage("optionsGreen")} Select Notification options", description:"Click here for Options"
            } else {
                href "speechOptions", title:"${getImage("optionsRed")} Select Notification options", description:"Click here for Options"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this child app", required: false, submitOnChange:true
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue:false, submitOnChange:true
		}
		display2()
	}
}

def speechOptions() {
    dynamicPage(name: "speechOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Options")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
    	}
        
        if(useSpeech || sendPushMessage) {
            
            exampleT = "<table width=100%><tr>"
            exampleT += "<td colspan=2>Example Messages:"
            exampleT += "<tr><td width=50%> - Someone is going down the hallway<td width=50%> - Someone is coming up the hallway"
            exampleT += "<tr><td> - Something just entered the driveway<td> - Something just exited the driveway"
            exampleT += "<tr><td> - Motion going up the stairs<td> - Motion going down the stairs"
            exampleT += "</table>"
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Messages")) {
                paragraph "${exampleT}"
                paragraph "Message when going to the <b>Right</b>"
                input "messageRight", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
                input "oG1List", "bool", defaultValue: false, title: "Show a list view of random messages?", description: "List View", submitOnChange: true
                if(oG1List) {
                    def valuesG1 = "${messageRight}".split(";")
                    listMapG1 = ""
                    valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
                    paragraph "${listMapG1}"
                }
            
                paragraph "Message when going to the <b>Left</b>"               
                input "messageLeft", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)", required:true, submitOnChange:true
                input "oG2List", "bool", defaultValue:false, title: "Show a list view of random messages?", description: "List View", submitOnChange:true
                if(oG2List) {
                    def valuesG2 = "${messageLeft}".split(";")
                    listMapG2 = ""
                    valuesG2.each { itemG2 -> listMapG2 += "${itemG2}<br>" }
                    paragraph "${listMapG2}"
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" The Flasher Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashRightPreset", "number", title: "Select the Preset to use when going to the Right (1..5)", required:true, submitOnChange:true
                input "flashLeftPreset", "number", title: "Select the Preset to use when going to the Left (1..5)", required:true, submitOnChange:true
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
	initialize()
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def initialize() {
    setDefaults()
    subscribe(motion1, "motion.active", motionOneHandler)
    subscribe(motion1, "motion.inactive", inactiveOneHandler)
    subscribe(motion2, "motion.active", motionTwoHandler)
    subscribe(motion2, "motion.inactive", inactiveTwoHandler)
}

def motionOneHandler(evt) {
    if(logEnable) log.debug "In motionOneHandler (${state.version})"
    if(atomicState.first != "two") { atomicState.first = "one" } 
    atomicState.motionOneActive = true
    if(logEnable) log.debug "In motion One Handler - first: ${atomicState.first}"
    if(atomicState.first == "two") activeHandler()
}

def motionTwoHandler(evt) {
    if(logEnable) log.debug "In motionTwoHandler (${state.version})"
    if(atomicState.first != "one") { atomicState.first = "two" }
    atomicState.motionTwoActive = true
    if(logEnable) log.debug "In motion Two Handler - first: ${atomicState.first}"
    if(atomicState.first == "one") activeHandler()
}

def activeHandler() {
    if(logEnable) log.debug "In activeHandler (${state.version})"
    if(atomicState.motionOneActive && atomicState.motionTwoActive) {
        if(atomicState.first == "one") { state.direction = "right" }
        if(atomicState.first == "two") { state.direction = "left" }
        if(dataDevice) dataDevice.on()
        if(logEnable) log.debug "In activeHandler - first: ${atomicState.first} - direction: ${state.direction}"
        dataDevice.direction(state.direction)
        messageHandler(state.direction)
    }
}

def inactiveOneHandler(evt) {
    if(logEnable) log.debug "In inactiveOneHandler (${state.version})"
    if(atomicState.first == "one") atomicState.first = ""
    atomicState.motionOneActive = false
    state.direction = ""
    if(dataDevice) dataDevice.off()
}

def inactiveTwoHandler(evt) {
    if(logEnable) log.debug "In inactiveTwoHandler (${state.version})"
    if(atomicState.first == "two") atomicState.first = ""
    atomicState.motionTwoActive = false
    state.direction = ""
    if(dataDevice) dataDevice.off()
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    dayOfTheWeekHandler()
    modeHandler()
    if(state.daysMatch && state.modeMatch && useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom("Direction Alerter")
        fmSpeaker.speak(msg)
    }
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def modeHandler() {
	if(logEnable) log.debug "In modeHandler (${state.version})"
	state.modeNow = location.mode
    state.modeMatch = false
    
    if(modeName) {
        modeName.each { it ->
            if(state.modeNow.contains(it)) {
                state.modeMatch = true
                if(logEnable) log.debug "In modeHandler - Match Found - modeName: ${modeName} - modeNow: ${state.modeNow}"
            }
        }
    } else {
        state.modeMatch = true
    }
    if(logEnable) log.debug "In modeHandler - modeMatch: ${state.modeMatch}"
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"  
    state.daysMatch = false
    if(days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        def dayCheck = days.contains(day)

        if(dayCheck) {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Passed"
            state.daysMatch = true
        } else {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Check Failed"
            state.daysMatch = false
        }
    } else {
        state.daysMatch = true
    }
    if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch}"
}

def messageHandler(data) {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    theMsg = ""

    if(data == "right") {
        def values = "${messageRight}".split(";")
        vSize = values.size()
        count = vSize.toInteger()
        def randomKey = new Random().nextInt(count)
        theMsg = values[randomKey]
        if(logEnable) log.debug "In messageHandler - theMsg: ${theMsg}"
    }

    if(data == "left") {
        def values = "${messageLeft}".split(";")
        vSize = values.size()
        count = vSize.toInteger()
        def randomKey = new Random().nextInt(count)
        theMsg = values[randomKey]
        if(logEnable) log.debug "In messageHandler - theMsg: ${theMsg}"
    }

    if(fmSpeaker) { letsTalk(theMsg) }
    if(sendPushMessage) { pushHandler(theMsg) }

    
    if(useTheFlasher && data == "right") { 
        flashData = "Preset::${flashRightPreset}"
        theFlasherDevice.sendPreset(flashData)
    }
    
    if(useTheFlasher && data == "left") { 
        flashData = "Preset::${flashLeftPreset}"
        theFlasherDevice.sendPreset(flashData)
    }
}

def pushHandler(theMsg) {
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

private flashLights() {    // Modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
	def doFlash = true
	def delay = delayFlashes ?: 500
	def numFlashes = numOfFlashes ?: 2

	if(logEnable) log.debug "In flashLights - LAST ACTIVATED: ${state.lastActivated}"
	if(state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (delay)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash - ELAPSED: $elapsed - LAST ACTIVATED: ${state.lastActivated}"
	}

	if(doFlash) {
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		state.lastActivated = now()
		if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switchesToFlash.collect{it.currentSwitch != "on"}

		numFlashes.times {
			if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.on()
				}
				else {
                    pauseExecution(delay)
					s.off()
				}
			}
			if(logEnable) log.debug "In flashLights - Switch off after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.off()
				}
				else {
                    pauseExecution(delay)
					s.on()
				}
			}
		}
	}
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Direction Alerter Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child tile device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Direction Alerter unable to create data device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
    state.motionOneActive = false
    state.motionTwoActive = false
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
