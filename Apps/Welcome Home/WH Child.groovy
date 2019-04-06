import groovy.time.TimeCategory
/**
 *  ****************  Welcome Home Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to give a personal welcome announcement after you have entered the home.
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
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
 *  V2.0.5 - 04/06/19 - Added importUrl. Volume Control overhaul. Code cleanup.
 *  V2.0.4 - 03/22/19 - Added a new option: restoreVolume for Echo Speaks devices
 *  V2.0.3 - 03/20/19 - Changed the wording on whether to turn the option for 'Echo Speaks' on or off.
 *  V2.0.2 - 02/26/19 - Reworked how the messages are stored. Added option to have random greetings. Removed Greeting and Messages
 *						from Parent app.
 *  V2.0.1 - 02/11/19 - Trobleshooting problem with Friendly Name - Fixed
 *  V2.0.0 - 02/11/19 - Major rewrite. Presence sensors are now in Parent app, so they can be shared across multiple child apps.
 *						Welcome Home now requires a new 'Virtual Device' using our custom 'Global Variables Driver'.  Each child app
 *                      will link to the same 'Virtual Device'.  This way we can track who came home across multiple child apps!
 *  V1.1.4 - 01/30/19 - Added in more message variables. Thanks to @Matthew for the coding.
 *  V1.1.3 - 01/24/19 - Welcome Home now works with Echo Speaks.
 *  V1.1.2 - 01/22/19 - Made all fields within Speech Options mandatory to avoid an error.
 *  V1.1.1 - 01/15/19 - Updated footer with update check and links
 *  V1.1.0 - 01/13/19 - Updated to announce multiple people coming home at the same time, in one message. Seems like such a simple
 *						thing but it took a huge rewrite to do it!
 *  V1.0.8 - 12/30/18 - Updated to my new color theme.
 *  V1.0.7 - 12/07/18 - Added an option to Contact Sensor trigger. Can now trigger based on Open or Closed.
 *  V1.0.6 - 12/04/18 - Code rewrite so we don't have to fill in all 20 presets. Must state in child app how many presets to use.
 *  V1.0.5 - 12/01/18 - Added 10 more random message presets! Fixed (hopefully) an issue with announcements not happening under
 *                      certain conditions. THE PARENT AND ALL CHILD APPS MUST BE OPENED AND SAVED AGAIN FOR THIS TO WORK.
 *  V1.0.4 - 11/30/18 - Found a bad bug and fixed it ;)
 *  V1.0.3 - 11/30/18 - Changed how the options are displayed, removed the Mode selection as it is not needed.
 *  V1.0.2 - 11/29/18 - Added an Enable/Disable child app switch. Fix an issue with multiple announcements on same arrival.
 *  V1.0.1 - 11/28/18 - Upgraded some of the logic and flow of the app. Added Motion Sensor Trigger, ability to choose multiple
 *  					door, locks or motion sensors. Updated the instructions.
 *  V1.0.0 - 11/25/18 - Initial release.
 *
 */

def setVersion() {
	state.version = "v2.0.5"
}

definition(
    name: "Welcome Home Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "This app is designed to give a personal welcome announcement after you have entered the home.",
    category: "",
	parent: "BPTWorld:Welcome Home",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Welcome%20Home/WH%20Child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Welcome Home</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
        	paragraph "<b>Types of Triggers:</b>"
    		paragraph "<b>Unlock or Door Open</b><br>Both of these work pretty much the same. When door or lock is triggered, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement."
			paragraph "Each trigger can have multiple selections but this is an 'or' function. Meaning it only takes one device to trigger the actions. ie. Door1 or Door2 has been opened. If you require a different delay per door/lock, then separate child apps would be required - one for each door or lock."
			paragraph "<b>Motion Sensor</b><br>When motion sensor becomes active, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement. If you require a different delay per motion sensor, then separate child apps would be required - one for each motion sensor."
			paragraph "This trigger also works with Hubitat's built in 'Zone Motion Controllers' app. Which allows you to do some pretty cool things with motion sensors."
			paragraph "<b>Notes:</b>"
			paragraph "This app is designed to give a personal welcome announcement <i>after</i> you have entered the home."
			paragraph "<b>Requirements:</b>"
			paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Select Activation Type")) {
    	input "triggerMode", "enum", title: "Select activation Type", submitOnChange: true,  options: ["Contact_Sensor","Door_Lock","Motion_Sensor"], required: true, Multiple: false
			if(triggerMode == "Door_Lock"){
				input "lock1", "capability.lock", title: "Activate the welcome message when this door is unlocked", required: true, multiple: true
			}
			if(triggerMode == "Contact_Sensor"){
				input "contactSensor", "capability.contactSensor", title: "Activate the welcome message when this contact sensor is activated", required: true, multiple: true
				input "csOpenClosed", "enum", title: "Activate when Opened or Closed" , options: ["Open","Closed"], required: true, defaultValue: "Open"
			}
			if(triggerMode == "Motion_Sensor"){
				input "motionSensor1", "capability.motionSensor", title: "Activate the welcome message when this motion sensor is activated", required: true, multiple: true
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options")) {
			paragraph "If a presence sensor has been detected for less than x minutes (set the minutes below), after the trigger, then speak the message."
			paragraph "Note: If you are not seeing your 'Friendly Names', then go back to the parent app, enter them in and hit 'done' before setting up any child apps."
			input(name: "presenceSensor1a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName1}", description: "PS1", submitOnChange: true)
			if(presenceSensor1a) input("presenceSensor1", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor2a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName2}", description: "PS2", submitOnChange: true)
			if(presenceSensor2a) input("presenceSensor2", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor3a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName3}", description: "PS3", submitOnChange: true)
			if(presenceSensor3a) input("presenceSensor3", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor4a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName4}", description: "PS4", submitOnChange: true)
			if(presenceSensor4a) input("presenceSensor4", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor5a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName5}", description: "PS5", submitOnChange: true)
			if(presenceSensor5a) input("presenceSensor5", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			
			input "timeHome", "number", title: "How many minutes can the presence sensor be home and still be considered for a welcome home message (default=10)", required: true, defaultValue: 10
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
           input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
			if (speechMode == "Music Player"){ 
              	input "speakers", "capability.musicPlayer", title: "Choose speaker(s)", required: true, multiple: true, submitOnChange: true
				paragraph "<hr>"
				paragraph "If you are using the 'Echo Speaks' app with your Echo devices then turn this option ON.<br>If you are NOT using the 'Echo Speaks' app then please leave it OFF."
				input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' app device?", description: "Echo speaks device", submitOnChange: true)
				if(echoSpeaks) input "restoreVolume", "number", title: "Volume to restore speaker to AFTER anouncement", description: "0-100%", required: true, defaultValue: "30"
          	}   
        	if (speechMode == "Speech Synth"){ 
         		input "speakers", "capability.speechSynthesis", title: "Choose speaker(s)", required: true, multiple: true
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
		section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			input(name: "oRandomG1", type: "bool", defaultValue: "false", title: "Random Greeting 1?", description: "Random", submitOnChange: "true")
			if(!oRandomG1) input "greeting1", "text", required: true, title: "Greeting - 1 (am) - Single message", defaultValue: "Good Morning"
			if(oRandomG1) {
				input "greeting1", "text", title: "Random Greeting - 1 (am) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of random messages 1?", description: "List View", submitOnChange: "true")
				if(oG1List) {
					def valuesG1 = "${greeting1}".split(";")
					listMapG1 = ""
    				valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
					paragraph "${listMapG1}"
				}
			}
			paragraph "<hr>"
			input(name: "oRandomG2", type: "bool", defaultValue: "false", title: "Random Greeting 2?", description: "Random", submitOnChange: "true")
			if(!oRandomG2) input "greeting2", "text", required: true, title: "Greeting - 2 (pm before 6) - Single message", defaultValue: "Good Afternoon"
			if(oRandomG2) {
				input "greeting2", "text", title: "Random Greeting - 2 (pm before 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG2List", type: "bool", defaultValue: "false", title: "Show a list view of the random messages 2?", description: "List View", submitOnChange: "true")
				if(oG2List) {
					def valuesG2 = "${greeting2}".split(";")
					listMapG2 = ""
    				valuesG2.each { itemG2 -> listMapG2 += "${itemG2}<br>" }
					paragraph "${listMapG2}"
				}
			}
			paragraph "<hr>"
			input(name: "oRandomG3", type: "bool", defaultValue: "false", title: "Random Greeting 3?", description: "Random", submitOnChange: "true")
			if(!oRandomG3) input "greeting3", "text", required: true, title: "Greeting - 3 (pm after 6) - Single message", defaultValue: "Good Evening"
			if(oRandomG3) {
				input "greeting3", "text", title: "Random Greeting - 3 (pm after 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG3List", type: "bool", defaultValue: "false", title: "Show a list view of the random messages 3?", description: "List View", submitOnChange: "true")
				if(oG3List) {
					def valuesG3 = "${greeting3}".split(";")
					listMapG3 = ""
    				valuesG3.each { itemG3 -> listMapG3 += "${itemG3}<br>" }
					paragraph "${listMapG3}"
				}
			}
			paragraph "<hr>"
			input(name: "oRandom", type: "bool", defaultValue: "false", title: "Random Message?", description: "Random", submitOnChange: "true")
			paragraph "<u>Optional wildcards:</u><br>%greeting% - returns a greeting based on time of day.<br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
			if(!oRandom) input "message", "text", title: "Message to be spoken - Single message",  required: true
			if(oRandom) {
				input "message", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				input(name: "oMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
				if(oMsgList) {
					def values = "${message}".split(";")
					listMap = ""
    				values.each { item -> listMap += "${item}<br>"}
					paragraph "${listMap}"
				}
			}
		}
		section() {
			input "delay1", "number", title: "How many seconds from the time the trigger being activated to the announcement being made (default=10)", required: true, defaultValue: 10
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Global Variables")) {
			paragraph "This app requires a 'virtual device' to send variables between child apps. This is to prevent multiple announcements.<br>ie. Person A comes home and enters door 1, walks through the house and opens door 2 to let the dogs out.  We only want one 'Welcome Home' message to be played."
			paragraph "* Vitual Device must use our custom 'Global Variables Driver'"
			input "gvDevice", "capability.actuator", title: "Virtual Device created for Welcome Home", required: true, multiple: false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
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
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
	
	subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	subscribe(presenceSensor1, "presence", presenceSensorHandler1)
	subscribe(presenceSensor2, "presence", presenceSensorHandler2)
	subscribe(presenceSensor3, "presence", presenceSensorHandler3)
	subscribe(presenceSensor4, "presence", presenceSensorHandler4)
	subscribe(presenceSensor5, "presence", presenceSensorHandler5)
	
	if(triggerMode == "Door_Lock"){subscribe(lock1, "lock", lockHandler)}
	if(triggerMode == "Contact_Sensor"){subscribe(contactSensor, "contact", contactSensorHandler)}
	if(triggerMode == "Motion_Sensor"){subscribe(motionSensor1, "motion", motionSensorHandler)}
}

def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	if(logEnable) log.debug "In enablerSwitchHandler - Enabler Switch: ${enablerSwitch2}"
    if(state.enablerSwitch2 == "on") log.info "${app.label} is disabled."
}

def setupNewStuff() {
	if(logEnable) log.debug "In setupNewStuff - Setting up Maps"
	if(state.presenceMap == null) state.presenceMap = [:]
}
		
def presenceSensorHandler1(evt){
	state.presenceSensorValue1 = evt.value
	if(logEnable) log.debug "IN presenceSensorHandler1 - Presence Sensor: ${state.presenceSensorValue1}"
    if(state.presenceSensorValue1 == "not present"){
    	if(logEnable) log.debug "Presence Sensor is not present - Been Here is now off."
		state.globalBH1 = "no"
		gvDevice.sendDataMap1(state.globalBH1)
    } else {
		if(logEnable) log.debug "Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler2(evt){
	state.presenceSensorValue2 = evt.value
	if(logEnable) log.debug "IN presenceSensorHandler2 - Presence Sensor: ${state.presenceSensorValue2}"
    if(state.presenceSensorValue2 == "not present"){
    	if(logEnable) log.debug "Presence Sensor is not present - Been Here is now off."
		state.globalBH2 = "no"
		gvDevice.sendDataMap2(state.globalBH2)
    } else {
		if(logEnable) log.debug "Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler3(evt){
	state.presenceSensorValue3 = evt.value
	if(logEnable) log.debug "IN presenceSensorHandler3 - Presence Sensor: ${state.presenceSensorValue3}"
    if(state.presenceSensorValue3 == "not present"){
    	if(logEnable) log.debug "Presence Sensor is not present - Been Here is now off."
		state.globalBH3 = "no"
		gvDevice.sendDataMap3(state.globalBH3)
    } else {
		if(logEnable) log.debug "Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler4(evt){
	state.presenceSensorValue4 = evt.value
	if(logEnable) log.debug "IN presenceSensorHandler4 - Presence Sensor: ${state.presenceSensorValue4}"
    if(state.presenceSensorValue4 == "not present"){
    	if(logEnable) log.debug "Presence Sensor is not present - Been Here is now off."
		state.globalBH4 = "no"
		gvDevice.sendDataMap4(state.globalBH4)
    } else {
		if(logEnable) log.debug "Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler5(evt){
	state.presenceSensorValue5 = evt.value
	if(logEnable) log.debug "IN presenceSensorHandler5 - Presence Sensor: ${state.presenceSensorValue5}"
    if(state.presenceSensorValue5 == "not present"){
    	if(logEnable) log.debug "Presence Sensor is not present - Been Here is now off."
		state.globalBH5 = "no"
		gvDevice.sendDataMap5(state.globalBH5)
    } else {
		if(logEnable) log.debug "Presence Sensor is present - Let's go!"
    }
}

def lockHandler(evt) {
	if(state.enablerSwitch2 == "off") {
		state.lockStatus = evt.value
		if(logEnable) log.debug "Lock Status: ${state.lockStatus}"
		if(state.lockStatus == "unlocked") {
			if(pause1 == true) log.info "${app.label} has been paused."
    		if(pause1 == false) {
				if(logEnable) log.debug "In lockHandler...Pause: ${pause1}"
				state.presenceMap = [:]
				state.nameCount = 0
				state.canSpeak = "no"
				if(presenceSensor1) getTimeDiff1()
				if(presenceSensor2) getTimeDiff2()
				if(presenceSensor3) getTimeDiff3()
				if(presenceSensor4) getTimeDiff4()
				if(presenceSensor5) getTimeDiff5()
				if(state.canSpeak == "yes") letsTalk()
			}
		}
	} else {
		if(logEnable) log.info "${app.label} is disabled."
	}
}

def contactSensorHandler(evt) {
	if(state.enablerSwitch2 == "off") {
		state.contactStatus = evt.value
		if(logEnable) log.debug "contact Status: ${state.contactStatus}"
		if(csOpenClosed == "Open") {
			if(state.contactStatus == "open") {
				if(pause1 == true) log.info "${app.label} has been paused."
    			if(pause1 == false) {
					if(logEnable) log.debug "In contactSensorHandler - Pause: ${pause1}"
					state.presenceMap = [:]
					state.nameCount = 0
					state.canSpeak = "no"
					if(logEnable) log.debug "In contactSensorHandler - canSpeak: ${state.canSpeak}"
					if(presenceSensor1) getTimeDiff1()
					if(presenceSensor2) getTimeDiff2()
					if(presenceSensor3) getTimeDiff3()
					if(presenceSensor4) getTimeDiff4()
					if(presenceSensor5) getTimeDiff5()
					if(state.canSpeak == "yes") letsTalk()
				}
			}
		}
		if(csOpenClosed == "Closed") {
			if(state.contactStatus == "closed") {
				if(pause1 == true) log.info "${app.label} has been paused."
    			if(pause1 == false) {
					if(logEnable) log.debug "In contactSensorHandler - Pause: ${pause1}"
					state.presenceMap = [:]
					state.nameCount = 0
					state.canSpeak = "no"
					if(logEnable) log.debug "In contactSensorHandler - canSpeak: ${state.canSpeak}"
					if(presenceSensor1) getTimeDiff1()
					if(presenceSensor2) getTimeDiff2()
					if(presenceSensor3) getTimeDiff3()
					if(presenceSensor4) getTimeDiff4()
					if(presenceSensor5) getTimeDiff5()
					if(state.canSpeak == "yes") letsTalk()
				}
			}
		}
	} else {
		if(logEnable) log.info "${app.label} is disabled."
	}
}

def motionSensorHandler(evt) {
	if(state.enablerSwitch2 == "off") {
		state.motionStatus = evt.value
		if(logEnable) log.debug "In motionSensorHandler - motion Status: ${state.motionStatus}"
		if(state.motionStatus == "active") {
			if(pause1 == true) log.info "${app.label} has been paused."
    		if(pause1 == false) {
				if(logEnable) log.debug "In motionSensorHandler - Pause: ${pause1}"
				state.presenceMap = [:]
				state.nameCount = 0
				state.canSpeak = "no"
				if(presenceSensor1) getTimeDiff1()
				if(presenceSensor2) getTimeDiff2()
				if(presenceSensor3) getTimeDiff3()
				if(presenceSensor4) getTimeDiff4()
				if(presenceSensor5) getTimeDiff5()
				if(state.canSpeak == "yes") letsTalk()
			}
		}
	} else {
		if(logEnable) log.info "${app.label} is disabled."
	}
}
										
def getTimeDiff1() {
	if(logEnable) log.debug "In getTimeDiff1..."
	def sensorStatus1 = presenceSensor1.currentValue("presence")
	if(logEnable) log.debug "Presence Sensor Status - 1: ${sensorStatus1}"
	if(sensorStatus1 == "present") {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH1}"
		def lastActivity1 = presenceSensor1.getLastActivity()
		
		if(logEnable) log.debug "lastActivity: ${lastActivity1}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity1}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "timeDiff: ${timeDiff}"
  		if(timeDiff < timeHome) {
			globalBeenHere()
			if(state.globalBH1 == "no") {
				log.info "${app.label} - ${parent.friendlyName1} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
				state.presenceMap = [parent.friendlyName1]
				state.canSpeak = "yes"
				state.globalBH1 = "yes"
				dataMap1 = "globalBH1:yes"
				gvDevice.sendDataMap1(state.globalBH1)
			} else {
				log.info "Global 'Been Here' is ${state.globalBH1}. No announcement needed."
			}
		} else {
			state.globalBH1 = "no"
			dataMap1 = "globalBH1:no"
			gvDevice.sendDataMap1(state.globalBH1)
			log.info "${app.label} - ${parent.friendlyName1} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH1}"
		if(logEnable) log.debug "Presence Sensor: ${sensorStatus1} - No announcement needed."
		state.globalBH1 = "no"
		dataMap1 = "globalBH1:no"
		gvDevice.sendDataMap1(state.globalBH1)
	}
}

def getTimeDiff2() {
	if(logEnable) log.debug "In getTimeDiff2..."
	def sensorStatus2 = presenceSensor2.currentValue("presence")
	if(logEnable) log.debug "Presence Sensor Status - 2: ${sensorStatus2}"
	if(sensorStatus2 == "present") {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH2}"
		def lastActivity2 = presenceSensor2.getLastActivity()
			
		if(logEnable) log.debug "lastActivity: ${lastActivity2}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity2}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "timeDiff: ${timeDiff}"	
  		if(timeDiff < timeHome) {
			globalBeenHere()
			if(state.globalBH2 == "no") {
				state.globalBH2 = "yes"
				dataMap2 = "globalBH2:yes"
				gvDevice.sendDataMap2(state.globalBH2)
				log.info "${app.label} - ${parent.friendlyName2} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
				if(state.nameCount == 1) state.presenceMap = [parent.friendlyName2]
				if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName2]
				state.canSpeak = "yes"
			} else {
				log.info "Global 'Been Here' is ${state.globalBH2}. No announcement needed."
			}
		} else {
			state.globalBH2 = "no"
			dataMap1 = "globalBH2:no"
			gvDevice.sendDataMap2(state.globalBH2)
			log.info "${app.label} - ${parent.friendlyName2} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH2}"
		if(logEnable) log.debug "Presence Sensor: ${sensorStatus2} - No announcement needed."
		state.globalBH2 = "no"
		dataMap2 = "globalBH2:no"
		gvDevice.sendDataMap2(state.globalBH2)
	}
}

def getTimeDiff3() {
	if(logEnable) log.debug "In getTimeDiff3..."
	def sensorStatus3 = presenceSensor3.currentValue("presence")
	if(logEnable) log.debug "Presence Sensor Status - 3: ${sensorStatus3}"
	if(sensorStatus3 == "present") {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH3}"
		def lastActivity3 = presenceSensor3.getLastActivity()
			
		if(logEnable) log.debug "lastActivity: ${lastActivity3}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity3}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "timeDiff: ${timeDiff}"
  		if(timeDiff < timeHome) {
			globalBeenHere()
			if(state.globalBH3 == "no") {
				state.globalBH3 = "yes"
				dataMap3 = "globalBH3:yes"
				gvDevice.sendDataMap3(state.globalBH3)
				log.info "${app.label} - ${parent.friendlyName3} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
				if(state.nameCount == 1) state.presenceMap = [parent.friendlyName3]
				if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName3]
				state.canSpeak = "yes"
			} else {
				log.info "Global 'Been Here' is ${state.globalBH3}. No announcement needed."
			}
		} else {
			state.globalBH3 = "no"
			dataMap3 = "globalBH3:no"
			gvDevice.sendDataMap3(state.globalBH3)
			log.info "${app.label} - ${parent.friendlyName3} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH3}"
		if(logEnable) log.debug "Presence Sensor: ${sensorStatus3} - No announcement needed."
		state.globalBH3 = "no"
		dataMap3 = "globalBH3:no"
		gvDevice.sendDataMap3(state.globalBH3)
	}
}

def getTimeDiff4() {
	if(logEnable) log.debug "In getTimeDiff4..."
	def sensorStatus4 = presenceSensor4.currentValue("presence")
	if(logEnable) log.debug "getTimeDiff4 - Presence Sensor Status - 4: ${sensorStatus4}"
	if(sensorStatus4 == "present") {
		if(logEnable) log.debug "getTimeDiff4 - Global Been Here: ${state.globalBH4}"
		def lastActivity4 = presenceSensor4.getLastActivity()
			
		if(logEnable) log.debug "getTimeDiff4 - lastActivity: ${lastActivity4}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity4}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "getTimeDiff4 - getTimeDiff4 - timeDiff: ${timeDiff}"
  		if(timeDiff < timeHome) {
			globalBeenHere()
			if(state.globalBH4 == "no") {
				state.globalBH4 = "yes"
				gvDevice.sendDataMap4(state.globalBH4)
				log.info "${app.label} - ${parent.friendlyName4} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
				if(state.nameCount == 1) state.presenceMap = [parent.friendlyName4]
				if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName4]
				state.canSpeak = "yes"
			} else {
				log.info "Global 'Been Here' is ${state.globalBH4}. No announcement needed."
			}
		} else {
			state.globalBH4 = "no"
			gvDevice.sendDataMap4(state.globalBH4)
			log.info "${app.label} - ${parent.friendlyName4} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
		if(logEnable) log.debug "getTimeDiff4 - Global Been Here: ${state.globalBH4}"
		if(logEnable) log.debug "getTimeDiff4 - Presence Sensor: ${sensorStatus4} - No announcement needed."
		state.globalBH4 = "no"
		gvDevice.sendDataMap4(state.globalBH4)
	}
}

def getTimeDiff5() {
	if(logEnable) log.debug "In getTimeDiff5..."
	def sensorStatus5 = presenceSensor5.currentValue("presence")
	if(logEnable) log.debug "Presence Sensor Status - 5: ${sensorStatus5}"
	if(sensorStatus5 == "present") {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH5}"
		def lastActivity5 = presenceSensor5.getLastActivity()
			
		if(logEnable) log.debug "lastActivity: ${lastActivity5}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity5}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "timeDiff: ${timeDiff}"
  		if(timeDiff < timeHome) {
			globalBeenHere()
			if(state.globalBH5 == "no") {
				state.globalBH5 = "yes"
				dataMap5 = "globalBH5:yes"
				gvDevice.sendDataMap5(state.globalBH5)
				log.info "${app.label} - ${parent.friendlyName5} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
				if(state.nameCount == 1) state.presenceMap = [parent.friendlyName5]
				if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName5]
				state.canSpeak = "yes"
			} else {
				log.info "Global 'Been Here' is ${state.globalBH5}. No announcement needed."
			}
		} else {
			state.globalBH5 = "no"
			dataMap5 = "globalBH5:no"
			gvDevice.sendDataMap5(state.globalBH5)
			log.info "${app.label} - ${parent.friendlyName5} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
		if(logEnable) log.debug "Global Been Here: ${state.globalBH5}"
		if(logEnable) log.debug "Presence Sensor: ${sensorStatus5} - No announcement needed."
		state.globalBH5 = "no"
		dataMap5 = "globalBH5:no"
		gvDevice.sendDataMap5(state.globalBH5)
	}
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk..."
	if(state.enablerSwitch2 == "off") {
		checkTime()
		checkVol()
		atomicState.randomPause = Math.abs(new Random().nextInt() % 1500) + 400
		if(logEnable) log.debug "In letsTalk - pause: ${atomicState.randomPause}"
		pauseExecution(atomicState.randomPause)
		if(logEnable) log.debug "In letsTalk - continuing"
		if(state.timeOK == true) {
			messageHandler()
			if(logEnable) log.debug "Speaker(s) in use: ${speakers}"
			state.theMsg = "${state.theMessage}"
			if(logEnable) log.debug "In letsTalk - Waiting ${delay1} seconds to Speak"
			def delay1ms = delay1 * 1000
			pauseExecution(delay1ms)
  			if (speechMode == "Music Player"){ 
    			if(logEnable) log.debug "Music Player"
				if(echoSpeaks) {
					speakers.setVolumeSpeakAndRestore(state.volume, state.theMsg, volRestore)
					state.canSpeak = "no"
					if(logEnable) log.debug "In letsTalk - Wow, that's it!"
				}
				if(!echoSpeaks) {
    				if(volSpeech) speakers.setLevel(state.volume)
    				speakers.playTextAndRestore(state.theMsg, volRestore)
					state.canSpeak = "no"
					if(logEnable) log.debug "In letsTalk - Wow, that's it!"
				}
  			}   
			if(speechMode == "Speech Synth"){ 
				speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
				atomicState.speechDuration2 = speechDuration * 1000
				if(logEnable) log.debug "Speech Synth - speakers: ${speakers}, vol: ${state.volume}, msg: ${state.theMsg}"
				if(volSpeech) speakers.setVolume(state.volume)
				speakers.speak(state.theMsg)
				pauseExecution(atomicState.speechDuration2)
				if(volRestore) speakers.setVolume(volRestore)
				state.canSpeak = "no"
				if(logEnable) log.debug "In letsTalk - Wow, that's it!"
			}
		} else {
			state.canSpeak = "no"
			if(logEnable) log.debug "In letsTalk - It's quiet time"
		}
	} else {
		state.canSpeak = "no"
		if(logEnable) log.info "${app.label} is disabled."
	}
}

def checkVol(){
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
}

def checkTime(){							// Modified from @Cobra Code
	if(logEnable) log.debug "In checkTime..."
	def timecheckNow = fromTime
	if (timecheckNow != null){
    
	def between = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
    if (between) {
    	state.timeOK = true
   		if(logEnable) log.debug "In checkTime - Time is ok, continue"
	}
	else if (!between) {
		state.timeOK = false
		if(logEnable) log.debug "In checkTime - Time is NOT ok, can't continue"
	}
  	}
	else if (timecheckNow == null){  
		state.timeOK = true
  		if(logEnable) log.debug "In checkTime - No Time restrictions - Continue"
  	}
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler..."
	if(oRandom) {
		def values = "${message}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		theMessage = values[randomKey]
		if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMessage: ${theMessage}"
	} else {
		theMessage = "${message}"
		if(logEnable) log.debug "In messageHandler - Static - theMessage: ${theMessage}"
	}
   	theMessage = theMessage.toLowerCase()
	if (theMessage.toLowerCase().contains("%greeting%")) {theMessage = theMessage.toLowerCase().replace('%greeting%', getGreeting() )}
	if (theMessage.toLowerCase().contains("%name%")) {theMessage = theMessage.toLowerCase().replace('%name%', getName() )}
	if (theMessage.toLowerCase().contains("%is_are%")) {theMessage = theMessage.toLowerCase().replace('%is_are%', "${is_are}" )}
	if (theMessage.toLowerCase().contains("%has_have%")) {theMessage = theMessage.toLowerCase().replace('%has_have%', "${has_have}" )}
	state.theMessage = "${theMessage}"
	return state.theMessage
}

private getName(){
	if(logEnable) log.debug "In getName - Number of Names: ${state.nameCount}, Names: ${state.presenceMap}"
	name = ""
	myCount = 0
	if(state.nameCount == 1) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=1: myCount = ${myCount}"
			name = "${it.value}"
		}
	}
	if(state.nameCount == 2) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=2: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it.value} "
			if(myCount == 1) name = "${name}" + "and "
		}
		name = "${name}" + "!"
	}
	if(state.nameCount == 3) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=3: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it.value}, "
			if(myCount == 2) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 4) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=4: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it.value}, "
			if(myCount == 3) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 5) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=5: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it.value}, "
			if(myCount == 4) name = "${name}" + "and "
		}
	}
	is_are = (name.contains(' and ') ? 'are' : 'is')
	has_have = (name.contains(' and ') ? 'have' : 'has')
	if(name == null) names = "Whoever you are"
	if(name == "") names = "Whoever you are"
	if(logEnable) log.debug "AGAIN...Name = ${name}"
	return name
}

private getGreeting(){						// Heavily modified from @Cobra Code
	if(logEnable) log.debug "In getGreeting..."
    def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
	def timeHH = calendar.get(Calendar.HOUR) toInteger()
    def timeampm = calendar.get(Calendar.AM_PM) ? "pm" : "am" 
	if(logEnable) log.debug "timeHH = $timeHH"
	if(timeampm == 'am'){
		if(oRandomG1) {
			def values = "${greeting1}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			if(logEnable) log.debug "In getGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm: ${timeampm} - timehh: ${timeHH}"
		} else {
			state.greeting = "${greeting1}"
			if(logEnable) log.debug "In getGreeting - Static - greeting: ${state.greeting}"
		}
	}
	else if(timeampm == 'pm' && timeHH < 6){
		if(oRandomG2) {
			def values = "${greeting2}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			if(logEnable) log.debug "In getGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm: ${timeampm} - timehh: ${timeHH}"
		} else {
			state.greeting = "${greeting2}"
			Lif(logEnable) log.debug "In getGreeting - Static - greeting: ${state.greeting}"
		}
	}
	else if(timeampm == 'pm' && timeHH >= 6){
		if(oRandomG3) {
			def values = "${greeting3}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			if(logEnable) log.debug "In getGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm = ${timeampm} - timehh = ${timeHH}"
		} else {
			state.greeting = "${greeting3}"
			if(logEnable) log.debug "In getGreeting - Static - greeting: ${state.greeting}"
		}
	}
	return state.greeting
}

def globalBeenHere() { 
    state.globalBH1 = gvDevice.currentValue("globalBH1")
	state.globalBH2 = gvDevice.currentValue("globalBH2")
	state.globalBH3 = gvDevice.currentValue("globalBH3")
	state.globalBH4 = gvDevice.currentValue("globalBH4")
	state.globalBH5 = gvDevice.currentValue("globalBH5")
	if(logEnable) log.debug "globalBH1: ${state.globalBH1} - globalBH2: ${state.globalBH2} - globalBH3: ${state.globalBH3} - globalBH4: ${state.globalBH4} - globalBH5: ${state.globalBH5}"
}

// ********** Normal Stuff **********

def pauseOrNot(){						// Modified from @Cobra
    state.pauseNow = pause1
    if(state.pauseNow == true){
    	state.pauseApp = true
        if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              	if(logEnable) log.debug "App Paused - state.pauseApp: $state.pauseApp"
            }
        }
    }
    if(state.pauseNow == false){
    	state.pauseApp = false
        if(app.label){
     		if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     		if(logEnable) log.debug "App Released - state.pauseApp: $state.pauseApp"                         
          	}
         }
	}      
}

def setDefaults(){
	setupNewStuff()
	globalBeenHere()
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
	state.nameCount = 0
	state.canSpeak = "no"
}

def getImage(type) {					// Modified from @Stephack
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Welcome Home - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
