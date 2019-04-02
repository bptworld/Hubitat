/**
 *  ****************  Follow Me App  ****************
 *
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
	state.version = "v1.0.7"
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
		section(getFormat("header-green", "${getImage("Blank")}"+" Select Activation Type for Room Speakers")) {
    		input "messageDest", "enum", title: "Select message destination", submitOnChange: true,  options: ["Speakers","Pushover"], required: true
		}
		// Speakers
		if(messageDest == "Speakers") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Select Activation Type for Room Speakers")) {
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
            	  	input "speaker", "capability.musicPlayer", title: "Choose speaker(s)", required: true, multiple: true, submitOnChange: true
					input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' device?", description: "Echo speaks device?", submitOnChange: true)
          		}   
        		if (speechMode == "Speech Synth"){ 
         			input "speaker", "capability.speechSynthesis", title: "Choose speaker(s)", required: true, multiple: true
					input(name: "gSpeaker", type: "bool", defaultValue: "false", title: "Is this a Google device?", description: "Google device?", submitOnChange: true)
					if(gSpeaker) paragraph "If using Google speaker devices sometimes an Initialize is necessary (not always)."
					if(gSpeaker) input "gInitialize", "bool", title: "Initialize Google devices before sending speech?", required: true, defaultValue: false
					if(gSpeaker) input "gInitRepeat", "number", title: "Initialize Google devices every X minutes?", required: false
         	 	}
      		}
			section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
				paragraph "NOTE: Not all speakers can use volume controls."
				if(speechMode == "Music Player") {
					input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true
					input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true
				}
				if(speechMode == "Speech Synth") {
					input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: false
					input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: false
				}
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
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
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
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
	
	if(enablerSwitch1) subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
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
	LOGDEBUG("In presenceSensorHandler1 - Presence Sensor = ${state.presenceSensorValue1}")
    if(state.presenceSensorValue1 == "not present"){
    	LOGDEBUG("Presence Sensor 1 is not present.")
		state.IH1 = "no"
    } else {
		LOGDEBUG("Presence Sensor 1 is present.")
		state.IH1 = "yes"
    }
}

def presenceSensorHandler2(evt){
	state.presenceSensorValue2 = evt.value
	LOGDEBUG("In presenceSensorHandler2 - Presence Sensor = ${state.presenceSensorValue2}")
    if(state.presenceSensorValue2 == "not present"){
    	LOGDEBUG("Presence Sensor 2 is not present.")
		state.IH2 = "no"
    } else {
		LOGDEBUG("Presence Sensor 2 is present.")
		state.IH2 = "yes"
    }
}

def presenceSensorHandler3(evt){
	state.presenceSensorValue3 = evt.value
	LOGDEBUG("In presenceSensorHandler3 - Presence Sensor = ${state.presenceSensorValue3}")
    if(state.presenceSensorValue3 == "not present"){
    	LOGDEBUG("Presence Sensor 3 is not present.")
		state.IH3 = "no"
    } else {
		LOGDEBUG("Presence Sensor 3 is present.")
		state.IH3 = "yes"
    }
}

def presenceSensorHandler4(evt){
	state.presenceSensorValue4 = evt.value
	LOGDEBUG("In presenceSensorHandler4 - Presence Sensor = ${state.presenceSensorValue4}")
    if(state.presenceSensorValue4 == "not present"){
    	LOGDEBUG("Presence Sensor 4 is not present.")
		state.IH4 = "no"
    } else {
		LOGDEBUG("Presence Sensor 4 is present.")
		state.IH4 = "yes"
    }
}

def presenceSensorHandler5(evt){
	state.presenceSensorValue5 = evt.value
	LOGDEBUG("In presenceSensorHandler5 - Presence Sensor = ${state.presenceSensorValue5}")
    if(state.presenceSensorValue5 == "not present"){
    	LOGDEBUG("Presence Sensor 5 is not present.")
		state.IH5 = "no"
    } else {
		LOGDEBUG("Presence Sensor 5 is present.")
		state.IH5 = "yes"
    }
}

def alwaysOnHandler() {
	LOGDEBUG("In alwaysOnHandler...")
	if(state.enablerSwitch2 == "off") {
		if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    	if(pause1 == false){
			LOGDEBUG("In alwaysOnHandler - setting sZone to true")
			state.sZone = true
		}
	} else {
		LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	}
}

def contactSensorHandler(evt) {
	LOGDEBUG("In contactSensorHandler...")
	if(state.enablerSwitch2 == "off") {
		state.contactStatus = evt.value
		LOGDEBUG("In contactSensorHandler - sZone: ${state.sZone} - Status: ${state.contactStatus}")
		if(contactOption == "Closed") {
			if(state.contactStatus == "closed") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){
					LOGDEBUG("In contactSensorHandler - setting sZone to true")
					state.sZone = true
				}
			}
			if(state.contactStatus == "open") {
				sOff = sZoneWaiting * 60
				runIn(sOff,speechOff)
			}
		}
		if(contactOption == "Open") {
			if(state.contactStatus == "open") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){
					LOGDEBUG("In contactSensorHandler - setting sZone to true")
					state.sZone = true
				}
			}
			if(state.contactStatus == "closed") {
				sOff = sZoneWaiting * 60
				runIn(sOff,speechOff)
			}
		}
	} else {
		LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	}
}

def motionSensorHandler(evt) {
	LOGDEBUG("In motionSensorHandler...")
	if(state.enablerSwitch2 == "off") {
		state.motionStatus = evt.value
		LOGDEBUG("In motionSensorHandler - sZone: ${state.sZone} - Status: ${state.motionStatus}")
		if(state.motionStatus == "active") {
			if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    		if(pause1 == false){
				LOGDEBUG("In motionSensorHandler - setting sZone to true")
				state.sZone = true
			}
		}
		if(state.motionStatus == "inactive") {
			sOff = sZoneWaiting * 60
			runIn(sOff,speechOff)
		}
	} else {
		LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	}
}

def switchHandler(evt) {
	LOGDEBUG("In switchHandler...")
	if(state.enablerSwitch2 == "off") {
		state.switchStatus = evt.value
		LOGDEBUG("In switchHandler - sZone: ${state.sZone} - Status: ${state.switchStatus}")
		if(state.switchStatus == "on") {
			if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    		if(pause1 == false){
				LOGDEBUG("In switchHandler - setting sZone to true")
				state.sZone = true
			}
		}
		if(state.switchStatus == "off") {
			sOff = sZoneWaiting * 60
			runIn(sOff,speechOff)
		}
	} else {
		LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	}
}

def lastSpokenHandler(speech) { 
	LOGDEBUG("In lastSpoken...")
	if(triggerMode == "Always_On") alwaysOnHandler()
	state.unique = speech.value.toString()
	state.cleanUp = state.unique.drop(1)
	state.priority = state.cleanUp.take(3)
	if(state.priority == "[L]" || state.priority == "[M]" || state.priority == "[H]" || state.priority == "[l]" || state.priority == "[m]" || state.priority == "[h]") {
		state.lastSpoken = state.cleanUp.drop(3)
	} else {
		state.lastSpoken = state.cleanUp
	}
	LOGDEBUG("In lastSpoken - Priority: ${state.priority} - lastSpoken: ${state.lastSpoken}")
	letsTalk()
	sendPush()
}

def speechOff() {
	if(state.motionStatus == 'active'){
		state.sZone = true
		LOGDEBUG( "In speechOff - Speech is on - sZone: ${state.sZone}")
	} else{
		state.sZone = false
		LOGDEBUG( "In speechOff - Speech is off - sZone: ${state.sZone}")
	}
}

def initializeSpeaker() {
	LOGDEBUG( "In initializeSpeaker - Initializing ${speaker}")
	speaker.initialize()
	repeat = gInitRepeat * 60
	if(gInitRepeat) runIn(repeat,initializeSpeaker)
}
						  
						  
def letsTalk() {
	LOGDEBUG("In letsTalk...")
	if(triggerMode == "Always_On") alwaysOnHandler()
	if(state.enablerSwitch2 == "off") {
		if(state.sZone == true){
			checkTime()
			checkVol()
			if(state.timeOK == true) {
				LOGDEBUG("In letsTalk - ${speechMode} - ${speaker}")
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
					speechDuration2 = speechDuration * 1000
					if(gInitialize) initializeSpeaker()
					if(volSpeech) speaker.setVolume(state.volume)
					speaker.speak(state.lastSpoken)
					pauseExecution(speechDuration2)
					if(volRestore) speaker.setVolume(volRestore)
				}
				LOGDEBUG("In letsTalk...Okay, I'm done!")
			} else {
				log.info "${app.label} - Quiet Time, can not speak."
			}
		} else {
			log.info "${app.label} - Zone is Off, can not speak."
		}
	} else {
		log.info "${app.label} - Disable Switch is ON - can not speak."
	}
}

def checkTime(){							// Modified from @Cobra
	LOGDEBUG("In checkTime...")
	def timecheckNow = fromTime
	if (timecheckNow != null){
    
	def between = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
    if (between) {
    	state.timeOK = true
   		LOGDEBUG("Time is ok so can continue")
	}
	else if (!between) {
		state.timeOK = false
		LOGDEBUG("Time is NOT ok so can't continue")
	}
  	}
	else if (timecheckNow == null){  
		state.timeOK = true
  		LOGDEBUG("Time restrictions have not been configured - Continue")
  	}
}

def checkVol(){
	LOGDEBUG("In checkVol...")
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
	LOGDEBUG("In checkVol - volume: ${state.volume}")
	if(messagePriority) {
		LOGDEBUG("In checkVol - priority: ${state.priority}")
		if(state.priority == "[L]" || state.priority == "[l]") { }			// No change
		if(state.priority == "[M]" || state.priority == "[m]") {state.volume = volMed}
		if(state.priority == "[H]" || state.priority == "[h]") {state.volume = volHigh}
		LOGDEBUG("In checkVol - priority volume: ${state.volume}")
	}
}

def sendPush(){
	LOGDEBUG("In sendPush...")
	if(state.IH1 == "no") {
		theMessage = "${state.lastSpoken}"
		LOGDEBUG("In sendPush - IH1 Sending message: ${theMessage}")
    	sendPushMessage1.deviceNotification(theMessage)
	}
	if(state.IH2 == "no") {
		theMessage = "${state.lastSpoken}"
		LOGDEBUG("In sendPush - IH2 Sending message: ${theMessage}")
    	sendPushMessage2.deviceNotification(theMessage)
	}
	if(state.IH3 == "no") {
		theMessage = "${state.lastSpoken}"
		LOGDEBUG("In sendPush - IH3 Sending message: ${theMessage}")
    	sendPushMessage3.deviceNotification(theMessage)
	}
	if(state.IH4 == "no") {
		theMessage = "${state.lastSpoken}"
		LOGDEBUG("In sendPush - IH4 Sending message: ${theMessage}")
    	sendPushMessage4.deviceNotification(theMessage)
	}
	if(state.IH5 == "no") {
		theMessage = "${state.lastSpoken}"
		LOGDEBUG("In sendPush - IH5 Sending message: ${theMessage}")
    	sendPushMessage5.deviceNotification(theMessage)
	}
}

// ********** Normal Stuff **********
def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	LOGDEBUG("In enablerSwitchHandler - Enabler Switch: ${state.enablerSwitch2}")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("Enabler Switch is OFF - Child app is active.")
    }
}

def pauseOrNot(){						// Modified from @Cobra
    state.pauseNow = pause1
    if(state.pauseNow == true){
        state.pauseApp = true
        if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
             	LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
        }
    }
    if(state.pauseNow == false){
    	state.pauseApp = false
        if(app.label){
     		if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     			LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
          	}
        }
	}      
}

def setDefaults(){
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
	if(state.sZone == null){state.sZone = false}
	if(state.IH1 == null){state.IH1 = "blank"}
	if(state.IH2 == null){state.IH2 = "blank"}
	if(state.IH3 == null){state.IH3 = "blank"}
	if(state.IH4 == null){state.IH4 = "blank"}
	if(state.IH5 == null){state.IH5 = "blank"}
	if(state.lastSpoken == null){state.lastSpoken = ""}
}

def LOGDEBUG(txt){
	if(settings.logEnable) { log.debug("${app.label} - ${txt}") }
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
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Follow Me - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  
