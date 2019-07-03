/**
 *  ****************  Life360 Tracker Child App  ****************
 *
 *  Design Usage:
 *  Track your Life360 users. Works with the Life360 with States app.
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
 *  V1.0.0 - 07/01/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
    name: "Life360 Tracker Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track your Life360 users. Works with the Life360 with States app.",
    category: "Convenience",
	parent: "BPTWorld:Life360 Tracker",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Life360%20Tracker/LT-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Tracker</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Track your Life360 users. Works with the user Life360 with States app."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Device")) {
			input "presenceDevice", "capability.presenceSensor", title: "Select Life360 User Device", required: true
            input "friendlyName", "text", title: "Friendly Name for this Device", required: true, submitOnChange: "true"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Place Tracking")) {
            paragraph "This will track the coming and going of places."
			input "trackingOptions", "enum", title: "How to track Places" , options: ["Track All","Track Specific"], required: true, submitOnChange: "true", defaultValue: "Track All"
            if(trackingOptions == "Track Specific") {
                input "trackSpecific", "text", title: "Track Specific Place, Seperate multiple Places with a <b>;</b><br><small>Must be the exact name(s) of Places already saved in your Life360 Phone App</small>", required: false, submitOnChange: "true"
                input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of Specific Places?", description: "List View", submitOnChange: "true")
                if(oG1List) {
			    	def valuesG1 = "${trackSpecific}".split(";")
			    	listMapG1 = ""
    			    valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
				    paragraph "${listMapG1}"
			    }
            }
            if(trackingOptions == "Track All") {
                paragraph "Tracking all places"
            }
            input "timeConsideredHere", "number", title: "Time to be considered at a Place (in Minutes)", required: true, submitOnChange: true, defaultValue: 2
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Sensor<br>%place% - returns the place arrived or departed"
			input "messageAT", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has arrived at %place%"
			input(name: "atMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if(atMsgList) {
				def values = "${messageAT}".split(";")
				listMapAT = ""
    			values.each { item -> listMapAT += "${item}<br>"}
				paragraph "${listMapAT}"
			}
            input "messageMOVE", "text", title: "Message to be spoken when <b>'on the move'</b> near a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% is on the move near %place%"
			input(name: "moveMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if(moveMsgList) {
				def values = "${messageMOVE}".split(";")
				listMapMove = ""
    			values.each { item -> listMapMove += "${item}<br>"}
				paragraph "${listMapMove}"
			}
		}   
        section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
           input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
			if (speechMode == "Music Player"){ 
              	input "speaker", "capability.musicPlayer", title: "Choose speaker(s)", required: true, multiple: true, submitOnChange: true
				paragraph "<hr>"
				paragraph "If you are using the 'Echo Speaks' app with your Echo devices then turn this option ON.<br>If you are NOT using the 'Echo Speaks' app then please leave it OFF."
				input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' app device?", description: "Echo speaks device", submitOnChange: true)
				if(echoSpeaks) input "restoreVolume", "number", title: "Volume to restore speaker to AFTER anouncement", description: "0-100%", required: true, defaultValue: "30"
          	}   
        	if (speechMode == "Speech Synth"){ 
         		input "speaker", "capability.speechSynthesis", title: "Choose speaker(s)", required: true, multiple: true
          	}
      	}
		section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
			paragraph "NOTE: Not all speakers can use volume controls."
			input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true
			input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true
            input "volQuiet", "number", title: "Quiet Time Speaker volume", description: "0-100", required: false, submitOnChange: true
			if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true
    		if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true
		}
    	if(speechMode){ 
			section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
        		input "fromTime", "time", title: "From", required: false
        		input "toTime", "time", title: "To", required: false
			}
    	}
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
			input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false
            input "isDataDevice", "capability.switch", title: "Turn this device on/off (On = at place, Off = moving)", required: false, multiple: false
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Extra Options")) {
            paragraph "Coming soon..."
        //    input "isDataDevice", "capability.switch", title: "Turn this device on/off based on requirements below", required: false, multiple: false
        //    paragraph "<small>* Choose as many as you like!</small>"
        //    input "allSpecific", "bool", title: "Based on Specific Places", description: "Specific places", defaultValue: false, submitOnChange: true, width:6
            
        //    if(allSpecific) input "dSpecific", "text", title: "", required: true, submitOnChange: true, defaultValue: "${trackSpecific}", width: 6
        //    if(!allSpecific) paragraph " ", width: 6

		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	updated()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
	initialize()
    runEvery5Minutes(userHandler)
}

def initialize() {
    setDefaults()
	subscribe(presenceDevice, "address1", userHandler)
}
	
def userHandler(evt) {
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    if(state.tDiff > timeHere) {
        if(logEnable) log.debug "In userHandler - Time at Place: ${state.tDiff} IS greater than: ${timeHere}"
    } else {
        if(logEnable) log.debug "In userHandler - Time at Place: ${state.tDiff} IS NOT greater than: ${timeHere}"
    }
    if(state.address1Value == state.prevPlace) {
        if(logEnable) log.debug "In userHandler - address1: ${state.address1Value} MATCHES state.prevPlace: ${state.prevPlace}"
    } else {
        if(logEnable) log.debug "In userHandler - address1: ${state.address1Value} DOES NOT MATCH state.prevPlace: ${state.prevPlace}"
    }
    
    if(state.address1Value == state.prevPlace) {
    
    // ***** Track All *****
        if(trackingOptions == "Track All") {
            if(logEnable) log.debug "In userHandler - Tracking All - ${friendlyName} is at ${state.address1Value}"

            if(state.tDiff > timeHere) {
                if(state.beenHere == "no") {
                    if(logEnable) log.debug "In userHandler - Tracking All - ${friendlyName} has arrived at ${state.address1Value}"
                    state.msg = "${messageAT}"
                    if(isDataDevice) isDataDevice.on()
                    letsTalk()
                } else {
                    if(logEnable) log.debug "In userHandler - Tracking All - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                }
                state.prevPlace = state.address1Value
                state.beenHere = "yes"
                state.onTheMove = "no"
            }
        }
  
        // ***** Track Specific *****    
        if(trackingOptions == "Track Specific") {
            theAddress1 = state.address1Value.toLowerCase()
            if(trackSpecific.toLowerCase().contains("${theAddress1}")) {
                if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} is at ${state.address1Value}"
                
                if(state.tDiff > timeHere) {
                    if(state.beenHere == "no") {
                        if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} has arrived at ${state.address1Value}"
                        state.msg = "${messageAT}"
                        if(isDataDevice) isDataDevice.on()
                        letsTalk()
                    } else {
                        if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                    }
                    state.prevPlace = state.address1Value
                    if(isDataDevice) isDataDevice.on()
                    state.beenHere = "yes"
                    state.onTheMove = "no"
                }
            } else {
		        if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} is not at a place this app is tracking ${state.address1Value}"
                state.prevPlace = state.address1Value
                if(isDataDevice) isDataDevice.off()
                state.beenHere = "no"
                state.onTheMove = "no"
            }
        }
    } else {
        if(logEnable) log.debug "In userHandler - ${friendlyName} is on the move near ${state.address1Value}"
        if(state.onTheMove == "no") {
            state.msg = "${messageMOVE}"
            if(isDataDevice) isDataDevice.off()
            letsTalk()
        }
        state.prevPlace = state.address1Value
        state.beenHere = "no"
        state.onTheMove = "yes"
        if(isDataDevice) isDataDevice.off()
    }
}

def getTimeDiff() {
	if(logEnable) log.debug "In getTimeDiff..."
	long since = presenceDevice.currentValue("since")
   	def now = new Date()
    long unxNow = now.getTime()
    unxNow = unxNow/1000    
    long timeDiff = Math.abs(unxNow-since)
    state.tDiff = timeDiff
    if(logEnable) log.debug "In getTimeDiff - since: ${since}, Now: ${unxNow}, Diff: ${timeDiff}"
    
	state.timeDay = (timeDiff / 86400).toInteger()
    state.timeHrs = ((timeDiff % 86400 ) / 3600).toInteger()
	state.timeMin = (((timeDiff % 86400 ) % 3600 ) / 60).toInteger()
	state.timeSec = (((timeDiff % 86400 ) % 3600 ) % 60).toInteger()
    
    if(logEnable) log.debug "In getTimeDiff - Time Diff: ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk..."
	checkTime()
	checkVol()
	atomicState.randomPause = Math.abs(new Random().nextInt() % 1500) + 400
	if(logEnable) log.debug "In letsTalk - pause: ${atomicState.randomPause}"
	pauseExecution(atomicState.randomPause)
	if(logEnable) log.debug "In letsTalk - continuing"
	if(state.timeBetween == true) {
		messageHandler()
		if(logEnable) log.debug "Speaker in use: ${speaker}"
		state.theMsg = "${state.theMessage}"
  		if (speechMode == "Music Player"){ 
    		if(logEnable) log.debug "In letsTalk - Music Player - speaker: ${speaker}, vol: ${state.volume}, msg: ${state.theMsg}"
			if(echoSpeaks) {
				speaker.setVolumeSpeakAndRestore(state.volume, state.theMsg, volRestore)
				state.canSpeak = "no"
				if(logEnable) log.debug "In letsTalk - Wow, that's it!"
			}
			if(!echoSpeaks) {
    			if(volSpeech) speaker.setLevel(state.volume)
    			speaker.playTextAndRestore(state.theMsg, volRestore)
				state.canSpeak = "no"
				if(logEnable) log.debug "In letsTalk - Wow, that's it!"
			}
  		}   
		if(speechMode == "Speech Synth"){ 
			speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
			atomicState.speechDuration2 = speechDuration * 1000
			if(logEnable) log.debug "In letsTalk - Speech Synth - speaker: ${speaker}, vol: ${state.volume}, msg: ${state.theMsg}"
			if(volSpeech) speaker.setVolume(state.volume)
			speaker.speak(state.theMsg)
			pauseExecution(atomicState.speechDuration2)
			if(volRestore) speaker.setVolume(volRestore)
			state.canSpeak = "no"
			if(logEnable) log.debug "In letsTalk - Wow, that's it!"
		}
		log.info "${app.label} - ${state.theMsg}"
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
    if(sendPushMessage) pushHandler()
}

def checkVol(){
	if(logEnable) log.debug "In checkVol..."
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false

  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler..."
	message = state.msg
    
	def values = "${message}".split(";")
	vSize = values.size()
	count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMessage = values[randomKey]
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMessage: ${theMessage}"
    
   	theMessage = theMessage.toLowerCase()
	if (theMessage.toLowerCase().contains("%name%")) {theMessage = theMessage.toLowerCase().replace('%name%', friendlyName )}
    if (theMessage.toLowerCase().contains("%place%")) {theMessage = theMessage.toLowerCase().replace('%place%', state.address1Value )}
	state.theMessage = "${theMessage}"
	return state.theMessage
}

def isThereData(){
	if(logEnable) log.debug "In isThereData..."
	if(state.isData) {
		isDataDevice.on()
	} else {
		isDataDevice.off()
	}
}

def pushHandler() {
	if(logEnable) log.debug "In pushNow..."
	theMessage = "${state.theMessage}"
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    if(state.beenHere == null){state.beenHere = "no"}
    if(state.address1Value == null){state.address1Value = presenceDevice.currentValue("address1")}
    if(state.prevPlace == null){state.prevPlace = state.address1Value}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Life360 Tracker - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
