/**
 *  **************** Simple Kitchen Timer Child App ****************
 *
 *  Design Usage:
 *  Create a simple kitchen timer with controls for use with Dashboards
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
 *  V1.0.0 - 03/29/20 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "SimpleKitchenTimerChildVersion"
	state.version = "v1.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
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
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Timers")) {
            paragraph "Each Timer Device can have up to 3 different timers built in."
            input name: "timer1", type: "number", title: "<b>Timer 1:</b><br>How many Seconds to set timer<br><small>(ie. 300 = 5 minutes)</small>"
            input name: "timer2", type: "number", title: "<b>Timer 2:</b><br>How many Seconds to set timer<br><small>(ie. 300 = 5 minutes)</small>"
            input name: "timer3", type: "number", title: "<b>Timer 3:</b><br>How many Seconds to set timer<br><small>(ie. 300 = 5 minutes)</small>" 
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Tile Options")) {
            input "countFontSize", "text", title: "Countdown Font Size", required: true, defaultValue: "40"
            input "countColor1", "text", title: "Countdown Color when > 10", required: true, defaultValue: "green", width:6
            input "countColor2", "text", title: "Countdown Color when <= 10", required: true, defaultValue: "blue", width:6
            input "countColor3", "text", title: "Countdown Color when <= 5", required: true, defaultValue: "orange", width:6
            input "countColor4", "text", title: "Countdown Color when Finished (0)", required: true, defaultValue: "red", width:6
        }

		section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            if(randomMessage10 && randomMessage0) {
                href "messageOptions", title:"${getImage("checkMarkGreen")} Message Options", description:"Click here to setup Messaging options"
            } else {
                href "messageOptions", title:"Message Options", description:"Click here to setup Messaging options"
            }
            
            if(speakerMP || speakerSS || switchesOn || switchesOff) {
                href "notificationOptions", title:"${getImage("checkMarkGreen")} Select Notification options here", description:"Click here for Options"
            } else {
                href "notificationOptions", title:"Select Notification options here", description:"Click here for Options"
            }
            
            if(flash) {
                href "flashOptions", title:"${getImage("checkMarkGreen")} Select Flash Lights options here", description:"Click here for Options"
            } else {
                href "flashOptions", title:"Select Flash Lights options here", description:"Click here for Options"
            }
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required: false
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def notificationOptions() {
    dynamicPage(name: "speechOptions", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerProxy", "bool", defaultValue: false, title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange: true
        }
        if(!speakerProxy) {
            if(speakerMP || speakerSS) {
		        section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		            paragraph "NOTE: Not all speakers can use volume controls.", width:8
                    paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                    input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required:true, width:6
		            input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required:true, width:6
                    input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required:false, submitOnChange:true
		    	    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required:true, width:6
    	    	    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required:true, width:6
                }
		    }
		    section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
                input "fromTime", "time", title: "From", required:false, width: 6
        	    input "toTime", "time", title: "To", required:false, width: 6
		    }
        } else {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Proxy")) {
		        paragraph "Speaker proxy in use."
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
    }
}

def messageOptions(){
    dynamicPage(name: "messageOptions", title: "Message Options", install: false, uninstall:false){    
        section(getFormat("header-green", "${getImage("Blank")}"+" Messaging Options")) {
	        input "randomMessage10", "text", title: "Random Message to be spoken at 10 seconds - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
			input "rm10List", "bool", defaultValue: true, title: "Show a list view of the messages?", description: "List View", submitOnChange: true
			if(rm10List) {
				def rm10 = "${randomMessage10}".split(";")
				rm10a = ""
    		    rm10.each { item -> rm10a += "${item}<br>"}
				paragraph "${rm10a}"
            }
            
            input "randomMessage0", "text", title: "Random Message to be spoken when Finished - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
			input "rm0List", "bool", defaultValue: true, title: "Show a list view of the messages?", description: "List View", submitOnChange: true
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
            input "flash", "bool", defaultValue: false, title: "Flash light(s) when timer is finished?", description: "Flash", submitOnChange: true
            if(flash) {
                input "switchesFlash", "capability.switch", title: "Flash these lights", multiple: true
		        input "numFlashes", "number", title: "Number of times", required: false, defaultValue: 2, width: 6
                input "delayFlashes", "number", title: "Milliseconds for lights to be on/off<br><small>(500=.5 sec, 1000=1 sec)</small>", required: false, defaultValue: 1000, width: 6
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
	initialize()
}

def initialize() {
    setVersion()
    setDefaults()
    subscribe(tileDevice, "isFinished", startHandler)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
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
        if(flash) flashLights()
    }
}

def letsTalk(msg) {
	if(logEnable) log.debug "In letsTalk (${state.version})"
	checkTime()
	checkVol()
    if(logEnable) log.debug "In letsTalk - Checking timeBetween: ${state.timeBetween}"
    if(state.timeBetween) {
        theMsg = msg
        
        def duration = Math.max(Math.round(theMsg.length()/12),2)+3
        theDuration = duration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
    	if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${theMsg}, volRestore: ${volRestore}"
        state.speakers.each { it ->
            if(logEnable) log.debug "Speaker in use: ${it}"
            if(speakerProxy) {
                if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                it.speak(theMsg)
            } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                def prevVolume = it.currentValue("volume")
                it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
            } else if(it.hasCommand('playTextAndRestore')) {   
                if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                def prevVolume = it.currentValue("volume")
                it.playTextAndRestore(theMsg, prevVolume)
            } else {		        
                if(logEnable) log.debug "In letsTalk - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                it.speak(theMsg)
                pauseExecution(theDuration)
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
            }
        }
            
        if(logEnable) log.debug "In letsTalk - Finished speaking"  
        if(sendPushMessage) pushNow(theMsg)
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
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
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, msg: ${msg}"
        
    letsTalk(msg)
}

def pushNow(theMsg){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

private flashLights() {    // Code modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
    
	def doFlash = true
    
	if(logEnable) log.debug "In flashLights - lastActivated: ${state.lastActivated}"
	if(state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if(doFlash) {
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		def initialActionOn = switchesFlash.collect{it.currentSwitch != "on"}
		def delayOn = delayFlashes * 1000
        
		numFlashes.times {
			switchesFlash.eachWithIndex {s, i ->
				if(initialActionOn[i]) {
                    if(logEnable) log.debug "In flashLights - 1 - Switching Lights On after $delayOn second(s)"
					s.on()
                    pauseExecution(delayFlashes)
                    if(logEnable) log.debug "In flashLights - 1 - Switching Lights Off after $delayOn second(s)"
                    s.off()
                    pauseExecution(delayFlashes)
				} else {
                    if(logEnable) log.debug "In flashLights - 2 - Switching Lights Off after $delayOn second(s)"
					s.off()
                    pauseExecution(delayFlashes)
                    if(logEnable) log.debug "In flashLights - 2 - Switching Lights On after $delayOn second(s)"
                    s.on()
                    pauseExecution(delayFlashes)
				}
			}
		}
	}
}

def sendDataToDriver() {
    if(logEnable) log.debug "In sendDataToDriver (${state.version})"
    cDevID = tileDevice.id
    theData = "${parent.hubIP}:${parent.makerID}:${parent.accessToken}:${cDevID}:${timer1}:${timer2}:${timer3}:${iFrameOff}:${countFontSize}:${countColor1}:${countColor2}:${countColor3}:${countColor4}"
    if(logEnable) log.debug "In sendDataToDriver - Sending: ${parent.hubIP}:${parent.makerID}:${parent.accessToken}:${cDevID}:${timer1}:${timer2}:${timer3}:${iFrameOff}:${countFontSize}:${countColor1}:${countColor2}:${countColor3}:${countColor4}"
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

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    state.numRepeats = 1
}

def getImage(type) {					// Modified Code from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText=""){			// Modified Code from @Stephack   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " Simple Kitchen Timer - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Simple Kitchen Timer - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
