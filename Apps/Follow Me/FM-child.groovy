/**
 *  ****************  Follow Me App  ****************
 *  Design Usage:
 *  Never miss a message again. Send messages to your occupied room speakers when home or by pushover when away. Automatically!
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
 *  Changes:
 *
 *  V1.1.2 - 04/15/19 - More Code cleanup
 *  V1.1.1 - 04/06/19 - Code cleanup
 *  V1.1.0 - 04/04/19 - More tweaks
 *  V1.0.9 - 04/03/19 - More tweaks to speaker status
 *	V1.0.8 - 04/02/19 - App now sends speaker status to the driver, can be displayed on dashboards
 *	V1.0.7 - 04/02/19 - More minor tweaks. Added import URL
 *	V1.0.6 - 04/01/19 - Fixed 'Enable/Disable Switch' and Activate by 'Switch'
 *	V1.0.5 - 03/31/19 - Fixed 'Always_On' Speakers
 *	V1.0.4 - 03/28/19 - Minor Tweaks
 *	V1.0.3 - 03/27/19 - Added volume control based on message priority.
 *	V1.0.2 - 03/20/19 - Added another Google Initialize option, every x minutes
 *  V1.0.1 - 03/19/19 - Fixed a typo, trying to fix the always on
 *  V1.0.0 - 03/17/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.1.2"
}

definition(
    name: "Follow Me Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Never miss a message again. Send messages to your occupied room speakers when home or by pushover when away. Automatically!",
    category: "",
	parent: "BPTWorld:Follow Me",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Follow%20Me/FM-child.groovy",
)

preferences {
    page(name: "pageConfig")
	page name: "pushOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Follow Me</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- Create a new child app for each room that has a speaker in it.<br>- Pushover child app can have up to 5 sensors defined.<br>- If more than 5 sensors are needed, simply add another child device."
			paragraph "<b>Priority Messages</b>"
			paragraph "- Each message sent to 'Follow Me' can have a priority assigned to it.<br>- Volume levels can then be adjusted by priority level.<br>- ie. (l)Dogs are hungry;(m)Door has been open too long;(h)Heat is on and window is open"
			paragraph "<b>Requirements:</b>"
			paragraph "- Virtual Device using our custom 'What Did I Say' driver"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Message destination")) {
    		input "messageDest", "enum", title: "Select message destination", submitOnChange: true,  options: ["Speakers","Pushover"], required: true
		}
		// Speakers
		if(messageDest == "Speakers") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Activation Type for Room Speakers")) {
    			input "triggerMode", "enum", title: "Select message activation Type", submitOnChange: true, options: ["Always_On","Contact_Sensor","Motion_Sensor","Switch"], required: true, Multiple: false
				if(triggerMode == "Always_On"){
					paragraph "Selected speakers will always play messages."	
				}
				if(triggerMode == "Contact_Sensor"){
					input "myContacts", "capability.contactSensor", title: "Select the contact sensor to activate the speaker", required: true, multiple: true
					input "contactOption", "enum", title: "Select contact option - If (option), Speaker is On", options: ["Open","Closed"], required: true
					input "sZoneWaiting", "number", title: "After contact changes, wait X minutes to turn the speaker off", required: true, defaultValue: 5
				}
				if(triggerMode == "Motion_Sensor"){
					input "myMotion", "capability.motionSensor", title: "Select the motion sensor to activate the speaker", required: true, multiple: true
					input "sZoneWaiting", "number", title: "After motion stops, wait X minutes to turn the speaker off", required: true, defaultValue: 5
				}
				if(triggerMode == "Switch"){
					input "mySwitches", "capability.switch", title: "Select Switch to activate the speaker", required: true, multiple: false
					input "sZoneWaiting", "number", title: "After Switch is off, wait X minutes to turn the speaker off", required: true, defaultValue: 5
				}
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
				input(name: "messagePriority", type: "bool", defaultValue: "false", title: "Use Message Priority features?", description: "Message Priority", submitOnChange: true)
			}
			if(messagePriority) {
				section("Instructions for Message Priority:", hideable: true, hidden: true) {
					paragraph "<b>Notes:</b>"
					paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place one of the following options in front of any message to be spoken and the volume will be adjusted accordingly.<br><b>[L]</b> - Low<br><b>[M]</b> - Medium<br><b>[H]</b> - High"
				paragraph "ie. [L]Amy is home or [M]Window has been open too long or [H]Heat is on and window is open"
				paragraph "Notice there is no spaces between the option and the message."
				}
				section() {
					paragraph "Low priority will use the standard volume set in the Volume Control Section"
					input "volMed", "number", title: "Speaker volume for Medium priority", description: "0-100", required: true, width: 6
					input "volHigh", "number", title: "Speaker volume for High priority", description: "0-100", required: true, width: 6
				}
			}
			section() {
        	   	input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
				if (speechMode == "Music Player"){ 
            	  	input "speaker", "capability.musicPlayer", title: "Choose speaker", required: true, submitOnChange: true
					input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' device?", description: "Echo speaks device?", submitOnChange: true)
          		}   
        		if (speechMode == "Speech Synth"){ 
         			input "speaker", "capability.speechSynthesis", title: "Choose speaker", required: true, submitOnChange: true
					input(name: "gSpeaker", type: "bool", defaultValue: "false", title: "Is this a Google device?", description: "Google device?", submitOnChange: true)
					if(gSpeaker) paragraph "If using Google speaker devices sometimes an Initialize is necessary (not always)."
					if(gSpeaker) input "gInitialize", "bool", title: "Initialize Google devices before sending speech?", required: true, defaultValue: false
					if(gSpeaker) input "gInitRepeat", "number", title: "Initialize Google devices every X minutes?", required: false
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
		}
		// both Speakers and Pushover
		section(getFormat("header-green", "${getImage("Blank")}"+" Speech Device")) {
			paragraph "This app requires a 'virtual device' to 'catch' the speech and send it here. All child apps will share this device. If you already use our 'What Did I Say' driver...you're allset! Just select the same device used with 'What Did I Say'."
			paragraph "* Vitual Device must use our custom 'What Did I Say Driver'"
			input "gvDevice", "capability.speechSynthesis", title: "Virtual Device created for Follow Me", required: true, multiple: false
		}
		// Pushover
		if(messageDest == "Pushover") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options for Pushover Messages")) {
				href "pushOptions", title:"Presence and Pushover Setup", description:"Select up to 5 presence sensor / pushover combinations"
			}
		}
		// both Speakers and Pushover
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def pushOptions(){
    dynamicPage(name: "pushOptions", title: "Presence and Pushover Setup", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options for Pushover Messages")) {
			paragraph "Select up to 5 presence sensor/Pushover Device combinations to receive messages when away from home."
			paragraph "<b>Combination 1</b>"
			input("presenceSensor1", "capability.presenceSensor", title: "Presence Sensor 1", required: false, width: 6)
			input("sendPushMessage1", "capability.notification", title: "Pushover Device 1", required: false, width: 6)
			paragraph "<b>Combination 2</b>"
			input("presenceSensor2", "capability.presenceSensor", title: "Presence Sensor 2", required: false, width: 6)
			input("sendPushMessage2", "capability.notification", title: "Pushover Device 2", required: false, width: 6)
			paragraph "<b>Combination 3</b>"
			input("presenceSensor3", "capability.presenceSensor", title: "Presence Sensor 3", required: false, width: 6)
			input("sendPushMessage3", "capability.notification", title: "Pushover Device 3", required: false, width: 6)
			paragraph "<b>Combination 4</b>"
			input("presenceSensor4", "capability.presenceSensor", title: "Presence Sensor 4", required: false, width: 6)
			input("sendPushMessage4", "capability.notification", title: "Pushover Device 4", required: false, width: 6)
			paragraph "<b>Combination 5</b>"
			input("presenceSensor5", "capability.presenceSensor", title: "Presence Sensor 5", required: false, width: 6)
			input("sendPushMessage5", "capability.notification", title: "Pushover Device 5", required: false, width: 6)
		}
	}
}		
			
def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
	subscribe(gvDevice, "lastSpokenUnique", lastSpokenHandler)
	if(myContact) subscribe(myContacts, "contact", contactSensorHandler)
	if(myMotion) subscribe(myMotion, "motion", motionSensorHandler)
	if(mySwitches) subscribe(mySwitches, "switch", switchHandler)
	if(presenceSensor1) subscribe(presenceSensor1, "presence", presenceSensorHandler1)
	if(presenceSensor2) subscribe(presenceSensor2, "presence", presenceSensorHandler2)
	if(presenceSensor3) subscribe(presenceSensor3, "presence", presenceSensorHandler3)
	if(presenceSensor4) subscribe(presenceSensor4, "presence", presenceSensorHandler4)
	if(presenceSensor5) subscribe(presenceSensor5, "presence", presenceSensorHandler5)
	if(gInitRepeat) runIn(gInitRepeat,initializeSpeaker)
}

def presenceSensorHandler1(evt){
	state.presenceSensorValue1 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler1 - Presence Sensor: ${state.presenceSensorValue1}"
    if(state.presenceSensorValue1 == "not present"){
    	if(logEnable) log.debug "Presence Sensor 1 is not present."
		state.IH1 = "no"
    } else {
		if(logEnable) log.debug "Presence Sensor 1 is present."
		state.IH1 = "yes"
    }
}

def presenceSensorHandler2(evt){
	state.presenceSensorValue2 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler2 - Presence Sensor: ${state.presenceSensorValue2}"
    if(state.presenceSensorValue2 == "not present"){
    	if(logEnable) log.debug "Presence Sensor 2 is not present."
		state.IH2 = "no"
    } else {
		if(logEnable) log.debug "Presence Sensor 2 is present."
		state.IH2 = "yes"
    }
}

def presenceSensorHandler3(evt){
	state.presenceSensorValue3 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler3 - Presence Sensor: ${state.presenceSensorValue3}"
    if(state.presenceSensorValue3 == "not present"){
    	if(logEnable) log.debug "Presence Sensor 3 is not present."
		state.IH3 = "no"
    } else {
		if(logEnable) log.debug "Presence Sensor 3 is present."
		state.IH3 = "yes"
    }
}

def presenceSensorHandler4(evt){
	state.presenceSensorValue4 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler4 - Presence Sensor: ${state.presenceSensorValue4}"
    if(state.presenceSensorValue4 == "not present"){
    	if(logEnable) log.debug "Presence Sensor 4 is not present."
		state.IH4 = "no"
    } else {
		if(logEnable) log.debug "Presence Sensor 4 is present."
		state.IH4 = "yes"
    }
}

def presenceSensorHandler5(evt){
	state.presenceSensorValue5 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler5 - Presence Sensor: ${state.presenceSensorValue5}"
    if(state.presenceSensorValue5 == "not present"){
    	if(logEnable) log.debug "Presence Sensor 5 is not present."
		state.IH5 = "no"
    } else {
		if(logEnable) log.debug "Presence Sensor 5 is present."
		state.IH5 = "yes"
    }
}

def alwaysOnHandler() {
	if(logEnable) log.debug "In alwaysOnHandler..."
		if(pauseApp == true){log.warn "${app.label} - App paused"}
    	if(pauseApp == false){
			if(logEnable) log.debug "In alwaysOnHandler - setting sZone to true"
			atomicState.sZone = true
			speakerStatus = "${app.label}:${atomicState.sZone}"
			gvDevice.sendFollowMeSpeaker(speakerStatus)
		}
}

def contactSensorHandler(evt) {
	if(logEnable) log.debug "In contactSensorHandler..."
		state.contactStatus = evt.value
		if(logEnable) log.debug "In contactSensorHandler - sZone: ${atomicState.sZone} - Status: ${state.contactStatus}"
		if(contactOption == "Closed") {
			if(state.contactStatus == "closed") {
				if(pauseApp == true){log.warn "${app.label} - App paused"}
    			if(pauseApp == false){
					if(logEnable) log.debug "In contactSensorHandler - setting sZone to true"
					atomicState.sZone = true
					speakerStatus = "${app.label}:${atomicState.sZone}"
					gvDevice.sendFollowMeSpeaker(speakerStatus)
				}
			}
			if(state.contactStatus == "open") {
				sOff = sZoneWaiting * 60
				runIn(sOff,speechOff)
			}
		}
		if(contactOption == "Open") {
			if(state.contactStatus == "open") {
				if(pauseApp == true){log.warn "${app.label} - App paused"}
    			if(pauseApp == false){
					if(logEnable) log.debug "In contactSensorHandler - setting sZone to true"
					atomicState.sZone = true
					speakerStatus = "${app.label}:${atomicState.sZone}"
					gvDevice.sendFollowMeSpeaker(speakerStatus)
				}
			}
			if(state.contactStatus == "closed") {
				sOff = sZoneWaiting * 60
				runIn(sOff,speechOff)
			}
		}
}

def motionSensorHandler(evt) {
	if(logEnable) log.debug "In motionSensorHandler..."
		state.motionStatus = evt.value
		if(logEnable) log.debug "In motionSensorHandler - sZone: ${atomicState.sZone} - Status: ${state.motionStatus}"
		if(state.motionStatus == "active") {
			if(pauseApp == true){log.warn "${app.label} - App paused"}
    		if(pauseApp == false){
				if(logEnable) log.debug "In motionSensorHandler - setting sZone to true"
				atomicState.sZone = true
				speakerStatus = "${app.label}:${atomicState.sZone}"
				gvDevice.sendFollowMeSpeaker(speakerStatus)
			}
		}
		if(state.motionStatus == "inactive") {
			sOff = sZoneWaiting * 60
			runIn(sOff,speechOff)
		}
}

def switchHandler(evt) {
	if(logEnable) log.debug "In switchHandler..."
		state.switchStatus = evt.value
		if(logEnable) log.debug "In switchHandler - sZone: ${atomicState.sZone} - Status: ${state.switchStatus}"
		if(state.switchStatus == "on") {
			if(pauseApp == true){log.warn "${app.label} - App paused"}
    		if(pauseApp == false){
				if(logEnable) log.debug "In switchHandler - setting sZone to true"
				atomicState.sZone = true
				speakerStatus = "${app.label}:${atomicState.sZone}"
				gvDevice.sendFollowMeSpeaker(speakerStatus)
			}
		}
		if(state.switchStatus == "off") {
			sOff = sZoneWaiting * 60
			runIn(sOff,speechOff)
		}
}

def lastSpokenHandler(speech) { 
	if(logEnable) log.debug "In lastSpoken..."
	if(triggerMode == "Always_On") alwaysOnHandler()
	state.unique = speech.value.toString()
	state.cleanUp = state.unique.drop(1)
	state.priority = state.cleanUp.take(3)
	if(state.priority == "[L]" || state.priority == "[M]" || state.priority == "[H]" || state.priority == "[l]" || state.priority == "[m]" || state.priority == "[h]") {
		state.lastSpoken = state.cleanUp.drop(3)
	} else {
		state.lastSpoken = state.cleanUp
	}
	if(logEnable) log.debug "In lastSpoken - Priority: ${state.priority} - lastSpoken: ${state.lastSpoken}"
	letsTalk()
	sendPush()
}

def speechOff() {
	if(state.motionStatus == 'active'){
		atomicState.sZone = true
		if(logEnable) log.debug "In speechOff - Speech is on - sZone: ${atomicState.sZone}"
	} else{
		atomicState.sZone = false
		speakerStatus = "${app.label}:${atomicState.sZone}"
		gvDevice.sendFollowMeSpeaker(speakerStatus)
		if(logEnable) log.debug "In speechOff - Speech is off - sZone: ${atomicState.sZone}"
	}
}

def initializeSpeaker() {
	if(logEnable) log.debug "In initializeSpeaker - Initializing ${speaker}"
	speaker.initialize()
	if(gInitRepeat) repeat = gInitRepeat * 60
	if(gInitRepeat) runIn(repeat,initializeSpeaker)
}
						  					  
def letsTalk() {
	if(logEnable) log.debug "In letsTalk..."
	if(triggerMode == "Always_On") alwaysOnHandler()
		if(atomicState.sZone == true){
			checkTime()
			checkVol()
			atomicState.randomPause = Math.abs(new Random().nextInt() % 1500) + 400
			if(logEnable) log.debug "In letsTalk - pause: ${atomicState.randomPause}"
			pauseExecution(atomicState.randomPause)
			if(logEnable) log.debug "In letsTalk - continuing"
			if(state.timeBetween == true) {
				state.sStatus = "speaking"
				speakerStatus = "${app.label}:${state.sStatus}"
				gvDevice.sendFollowMeSpeaker(speakerStatus)
				if(logEnable) log.debug "In letsTalk - ${speechMode} - ${speaker}"
  				if (speechMode == "Music Player"){ 
					if(echoSpeaks) {
						speaker.setVolumeSpeakAndRestore(state.volume, state.lastSpoken, volRestore)
					}
					if(!echoSpeaks) {
    					if(volSpeech) speaker.setLevel(state.volume)
    					speaker.playTextAndRestore(state.lastSpoken, volRestore)
					}
  				}   
				if (speechMode == "Speech Synth"){
					speechDuration = Math.max(Math.round(state.lastSpoken.length()/12),2)+3		// Code from @djgutheinz
					atomicState.speechDuration2 = speechDuration * 1000
					if(gInitialize) initializeSpeaker()
					if(volSpeech) speaker.setVolume(state.volume)
					speaker.speak(state.lastSpoken)
					pauseExecution(atomicState.speechDuration2)
					if(volRestore) speaker.setVolume(volRestore)
				}
				speakerStatus = "${app.label}:${atomicState.sZone}"
				gvDevice.sendFollowMeSpeaker(speakerStatus)
				if(logEnable) log.debug "In letsTalk...Okay, I'm done!"
			} else {
				log.info "${app.label} - Quiet Time, can not speak."
			}
		} else {
			log.info "${app.label} - Zone is Off, can not speak."
		}
}

def checkTime() {
	if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) {
			state.timeBetween = true
		} else {
			state.timeBetween = false
		}
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def checkVol() {
	if(logEnable) log.debug "In checkVol..."
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
    	if(state.quietTime) {
    		state.volume = volQuiet
		} else {
			state.volume = volSpeech
		}
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - volume: ${state.volume}"
	if(messagePriority) {
		if(logEnable) log.debug "In checkVol - priority: ${state.priority}"
		if(state.priority == "[L]" || state.priority == "[l]") { }			// No change
		if(state.priority == "[M]" || state.priority == "[m]") {state.volume = volMed}
		if(state.priority == "[H]" || state.priority == "[h]") {state.volume = volHigh}
		if(logEnable) log.debug "In checkVol - priority volume: ${state.volume}"
	}
}

def sendPush() {
	if(logEnable) log.debug "In sendPush..."
	if(state.IH1 == "no") {
		theMessage = "${state.lastSpoken}"
		if(logEnable) log.debug "In sendPush - IH1 Sending message: ${theMessage}"
    	sendPushMessage1.deviceNotification(theMessage)
	}
	if(state.IH2 == "no") {
		theMessage = "${state.lastSpoken}"
		if(logEnable) log.debug "In sendPush - IH2 Sending message: ${theMessage}"
    	sendPushMessage2.deviceNotification(theMessage)
	}
	if(state.IH3 == "no") {
		theMessage = "${state.lastSpoken}"
		if(logEnable) log.debug "In sendPush - IH3 Sending message: ${theMessage}"
    	sendPushMessage3.deviceNotification(theMessage)
	}
	if(state.IH4 == "no") {
		theMessage = "${state.lastSpoken}"
		if(logEnable) log.debug "In sendPush - IH4 Sending message: ${theMessage}"
    	sendPushMessage4.deviceNotification(theMessage)
	}
	if(state.IH5 == "no") {
		theMessage = "${state.lastSpoken}"
		if(logEnable) log.debug "In sendPush - IH5 Sending message: ${theMessage}"
    	sendPushMessage5.deviceNotification(theMessage)
	}
}

// ********** Normal Stuff **********

def setDefaults(){
    pauseHandler()
	if(logEnable) log.debug "In setDefaults..."
    if(pauseApp == null){pauseApp = false}
	if(atomicState.sZone == null){atomicState.sZone = false}
	if(state.IH1 == null){state.IH1 = "blank"}
	if(state.IH2 == null){state.IH2 = "blank"}
	if(state.IH3 == null){state.IH3 = "blank"}
	if(state.IH4 == null){state.IH4 = "blank"}
	if(state.IH5 == null){state.IH5 = "blank"}
	if(state.lastSpoken == null){state.lastSpoken = ""}
}

def getImage(type){						// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pauseApp", "bool", title: "Pause App", required: true, submitOnChange: true, defaultValue: false
		if(pauseApp) {paragraph "<font color='red'>App is Paused</font>"}
		if(!pauseApp) {paragraph "App is not Paused"}
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Follow Me - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  
