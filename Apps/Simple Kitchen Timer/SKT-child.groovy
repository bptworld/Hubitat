/**
 *  **************** Simple Kitchen Timer Child App ****************
 *
 *  Design Usage:
 *  Create a simple kitchen timer with controls for use with Dashboards
 *
 *  Copyright 2020-2021 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  1.0.7 - 12/21/21 - Added Cloud option
 *  1.0.6 - 06/22/20 - Changes to letsTalk
 *  1.0.5 - 06/19/20 - Added The Flasher, all speech now goes through 'Follow Me'
 *  1.0.4 - 04/27/20 - Cosmetic changes
 *  1.0.3 - 04/04/20 - Fixed push, fixed issue with 'unexpected error' with Notification page, added wildcard for timer name in message.
 *  1.0.2 - 03/29/20 - Bug hunting
 *  1.0.1 - 03/29/20 - Added switch control to Finished options, added timer name options, added Tile character count to Maint section
 *  1.0.0 - 03/29/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Simple Kitchen Timer"
	state.version = "1.0.7"
}

definition(
    name: "Simple Kitchen Timer Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a simple kitchen timer with controls for use with Dashboards",
    category: "Convenience",
	parent: "BPTWorld:Simple Kitchen Timer",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Kitchen%20Timer/SKT-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "notificationOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "messageOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "flashOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "deviceOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
            paragraph "Create a simple kitchen timer with controls for use with Dashboards"
            paragraph "After installing and setting up this app, be sure to add the new device to Maker API before adding it to any dashboard"
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device and Dashboard Tile")) {}
        section("Important Information Regarding the Virtual Device:", hideable: true, hidden: true) {
            paragraph "Simple Kitchen Timer uses an iFrame within the table that creates the Dashboard Tile. This is the magic that makes device control possible without a new window opening up and ruining the whole experience."
            paragraph "This also has the downside of messing with the virtual device created. While the Dashboard tile isn't effected and continues to update as usual, the Virtual Device itself will not load from the Device page. You will just see a blank (white) screen and the spinning blue thing in the corner. Again, this does not effect the workings of this app or the Dashboard tile. Just the annoyance of not being able to view the device page."
            paragraph "With that said, there really is no reason to view the device page as there are no options, it's just a holding place for the Dashboard tile. But, if for any reason you do want to view the device page, I've added in a switch to turn the iFrame off."
            paragraph "What will happen if this is off?<br> - If you click a value in the Sample tile, a new window will open<br> - If you click a value in the Device page, a new window will open<br> - If you click a value in the Dashboard tile, everything should work as usual (no window opening)"
            paragraph "If you experience anything different, you should turn the iFrame back on and post on the forums. Be sure to mention the issue and what browser you are using."

            input "iFrameOff", "bool", title: "Turn iFrame off?", defaultValue:false, description: "iFrame", submitOnChange:true
            if(iFrameOff) paragraph "<div style='color: green'>iFrames are turned off, virtual device is now accessible from device menu.</div>"
            if(!iFrameOff) paragraph "<div style='color: red'>iFrames are turned on, virtual device will not load from device menu.</div>"
        }

        section() {
            paragraph "Each child app needs a virtual device to store the Tile Master data. Enter a short descriptive name for this device."
			input "userName", "text", title: "Enter a name for this Tile Device (ie. 'Kitchen' will become 'SKT - Kitchen')", required:true, submitOnChange:true
            paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
            if(userName) createChildDevice()
            if(statusMessage == null) statusMessage = "Waiting on status message..."
            paragraph "${statusMessage}"
            input "tileDevice", "capability.switch", title: "Vitual Device created to send the data to:", required:true, multiple:false
        } 
        
        section() {
            input "ipORcloud", "bool", title: "Use Local or Cloud control", defaultValue:false, description: "Ip or Cloud", submitOnChange:true
            if(ipORcloud) {
                if(!parent.cloudToken) {
                    paragraph "<b>Be sure to fill out the Maker API section in the parent app</b>"
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Timers")) {
            paragraph "Each Timer Device can have up to 3 different timers built in. Each timer can display the length or a short name. Remember to watch your character count!"
            input name: "timer1", type: "number", title: "<b>Timer 1:</b><br>How many Seconds to set timer<br><small>(ie. 300 = 5 minutes)</small>", width:6
            input name: "timer1n", type: "text", title: "<b>Timer 1:</b><br>Enter a SHORT name for the timer<br><small>Optional</small>", width:6
            
            input name: "timer2", type: "number", title: "<b>Timer 2:</b><br>How many Seconds to set timer<br><small>(ie. 300 = 5 minutes)</small>", width:6
            input name: "timer2n", type: "text", title: "<b>Timer 2:</b><br>Enter a SHORT name for the timer<br><small>Optional</small>", width:6
            
            input name: "timer3", type: "number", title: "<b>Timer 3:</b><br>How many Seconds to set timer<br><small>(ie. 300 = 5 minutes)</small>", width:6
            input name: "timer3n", type: "text", title: "<b>Timer 3:</b><br>Enter a SHORT name for the timer<br><small>Optional</small>", width:6
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Tile Options")) {
            input "countFontSize", "text", title: "Countdown Font Size", required: true, defaultValue: "40"
            input "countColor1", "text", title: "Countdown Color when > 10", required: true, defaultValue: "green", width:6
            input "countColor2", "text", title: "Countdown Color when <= 10", required: true, defaultValue: "blue", width:6
            input "countColor3", "text", title: "Countdown Color when <= 5", required: true, defaultValue: "orange", width:6
            input "countColor4", "text", title: "Countdown Color when Finished (0)", required: true, defaultValue: "red", width:6
        }

		section(getFormat("header-green", "${getImage("Blank")}"+" Options, Options, Options")) {
            if(randomMessage10 && randomMessage0) {
                href "messageOptions", title:"${getImage("checkMarkGreen")} Message Options", description:"Click here to setup Messaging options"
            } else {
                href "messageOptions", title:"Message Options", description:"Click here to setup Messaging options"
            }
            
            if(fmSpeaker || switchesOn || switchesOff) {
                href "notificationOptions", title:"${getImage("checkMarkGreen")} Select Notification options here", description:"Click here for Options"
            } else {
                href "notificationOptions", title:"Select Notification options here", description:"Click here for Options"
            }
            
            if(flash) {
                href "flashOptions", title:"${getImage("checkMarkGreen")} Select Flash Lights options here", description:"Click here for Options"
            } else {
                href "flashOptions", title:"Select Flash Lights options here", description:"Click here for Options"
            }
            
            if(deviceOn || deviceOff) {
                href "deviceOptions", title:"${getImage("checkMarkGreen")} Select Device options here", description:"Click here for Options"
            } else {
                href "deviceOptions", title:"Select Device options here", description:"Click here for Options"
            }
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required: false
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
            try { state.tileCount = tileDevice.currentValue('tileCount') } catch (e) {state.tileCount = "0"}
            paragraph "<hr>"
            paragraph "<b>To check Character count:</b><br>- Toggle the logEnable switch above<br>- Then click the button below"
            input "sendData", "button", title: "Update"
            paragraph "<b>Tile character count: ${state.tileCount}</b><br><small>* Max character count is 1024</small>"
		}
		display2()
	}
}

def notificationOptions() {
    dynamicPage(name: "notificationOptions", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
    }
}

def messageOptions(){
    dynamicPage(name: "messageOptions", title: "Message Options", install: false, uninstall:false){    
        section(getFormat("header-green", "${getImage("Blank")}"+" Messaging Options")) {
            paragraph "<u>Optional wildcards:</u><br>%name% - returns the Name associcated with the Timer"
	        input "randomMessage10", "text", title: "Random Message to be spoken at 10 seconds - Separate each message with <b>;</b> (semicolon)", required: false, submitOnChange: true
			input "rm10List", "bool", defaultValue: false, title: "Show a list view of the messages?", description: "List View", submitOnChange: true
			if(rm10List) {
				def rm10 = "${randomMessage10}".split(";")
				rm10a = ""
    		    rm10.each { item -> rm10a += "${item}<br>"}
				paragraph "${rm10a}"
            }
            
            input "randomMessage0", "text", title: "Random Message to be spoken when Finished - Separate each message with <b>;</b> (semicolon)", required: false, submitOnChange: true
			input "rm0List", "bool", defaultValue: false, title: "Show a list view of the messages?", description: "List View", submitOnChange: true
			if(rm0List) {
				def rm0 = "${randomMessage0}".split(";")
				rm0a = ""
    		    rm0.each { item -> rm0a += "${item}<br>"}
				paragraph "${rm0a}"
            }
        }  
    }
}

def flashOptions(){
    dynamicPage(name: "flashOptions", title: "Flash Lights Options", install: false, uninstall:false){  
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashPreset", "number", title: "Select the Preset to use with Timer (1..5)", required:true, submitOnChange:true
            }
        }
    }
}

def deviceOptions(){
    dynamicPage(name: "deviceOptions", title: "Device Options", install: false, uninstall:false){  
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            input "deviceOn", "capability.switch", title: "Select Device(s) to turn ON when timer is finished", multiple: true, required: false
            input "deviceOff", "capability.switch", title: "Select Device(s) to turn OFF when timer is finished", multiple: true, required: false
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
	initialize()
}

def initialize() {
    setVersion()
    setDefaults()
    subscribe(tileDevice, "isFinished", startHandler)

    sendDataToDriver()
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def startHandler(evt) {
    status = evt.value
    if(logEnable) log.debug "In startHandler (${state.version}) - status: ${status}"
    
    if(status == "at10") {
        if(logEnable) log.debug "In startHandler - status: ${status}"
        messageHandler(randomMessage10)
    }
    
    if(status == "finished") {
        if(logEnable) log.debug "In startHandler - status: ${status}"
        messageHandler(randomMessage0)
        if(deviceOn) deviceOnHandler()
        if(deviceOff) deviceOffHandler()
        if(usingTheFlasher) {
            if(useTheFlasher) {
                flashData = "Preset::${flashPreset}"
                theFlasherDevice.sendPreset(flashData)
            }
        }
    }
}

def letsTalk(msg) {
    if(logEnable) log.warn "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(msg)
    }
    if(logEnable) log.warn "In letsTalk - *** Finished ***"
}

def checkVol(){
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - setting volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}


def messageHandler(msg) {
	if(logEnable) log.debug "In messageHandler (${state.version})"
	state.theMsg = ""
    
	def values = "${msg}".split(";")
	vSize = values.size()
    count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	msg = values[randomKey]
    
    if(msg.contains("%name%")) {
        def currentTimer = tileDevice.currentValue("currentTimer")
        if(logEnable) log.debug "In messageHandler - currentTimer: ${currentTimer}"
        msg = msg.replace('%name%', "${currentTimer}" )
    }
    
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, msg: ${msg}"
    if(sendPushMessage) pushNow(msg)
    letsTalk(msg)
}

def pushNow(theMsg){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

def deviceOnHandler() {
    if(logEnable) log.debug "In deviceOnHandler (${state.version})"
    
    deviceOn.each { it ->
        it.on()
    }
}

def deviceOffHandler() {
    if(logEnable) log.debug "In deviceOffHandler (${state.version})"
    
    deviceOff.each { it ->
        it.off()
    }
}

def sendDataToDriver() {
    if(logEnable) log.debug "In sendDataToDriver (${state.version})"
    cDevID = tileDevice.id
    theData = "${parent.hubIP}:${parent.cloudToken}:${parent.makerID}:${parent.accessToken}:${cDevID}:${timer1}:${timer2}:${timer3}:${timer1n}:${timer2n}:${timer3n}:${iFrameOff}:${countFontSize}:${countColor1}:${countColor2}:${countColor3}:${countColor4}:${ipORcloud}"
    //if(logEnable) log.debug "In sendDataToDriver - Sending: ${parent.hubIP}:${parent.cloudToken}:${parent.makerID}:${parent.accessToken}:${cDevID}:${timer1}:${timer2}:${timer3}:${timer1n}:${timer2n}:${timer3n}:${iFrameOff}:${countFontSize}:${countColor1}:${countColor2}:${countColor3}:${countColor4}:${ipORcloud}"
    tileDevice.sendDataToDriver(theData)
}

def createChildDevice() {    
    if(logEnable) log.debug "In createChildDevice (${state.version})"
    statusMessage = ""
    if(!getChildDevice("SKT - " + userName)) {
        if(logEnable) log.debug "In createChildDevice - Child device not found - Creating device Simple Kitchen Timer - ${userName}"
        try {
            addChildDevice("BPTWorld", "Simple Kitchen Timer Driver", "SKT - " + userName, 1234, ["name": "SKT - ${userName}", isComponent: false])
            if(logEnable) log.debug "In createChildDevice - Child device has been created! (SKT - ${userName})"
            statusMessage = "<b>Device has been been created. (SKT - ${userName})</b>"
        } catch (e) { if(logEnable) log.debug "Simple Kitchen Timer unable to create device - ${e}" }
    } else {
        statusMessage = "<b>Device Name (SKT - ${userName}) already exists.</b>"
    }
    return statusMessage
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "sendData"){
        if(logEnable) log.debug "In testButtonHandler - Sending data"
        sendDataToDriver()
    }
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    state.numRepeats = 1
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
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
