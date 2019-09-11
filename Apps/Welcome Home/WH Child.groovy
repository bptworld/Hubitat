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
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.1.2 - 09/11/19 - Lot's of code changes, now handles up to 20 presence sensors/friendly names
 *  V2.1.1 - 08/18/19 - Now App Watchdog compliant
 *  V2.1.0 - 06/06/19 - Added more wording to Volume Control Options. Code cleanup.
 *  V2.0.9 - 06/04/19 - More code cleanup
 *  V2.0.8 - 04/18/19 - Fixed quiet time
 *  V2.0.7 - 04/15/19 - Code cleanup
 *  V2.0.6 - 04/13/19 - Made a ton of debug and info enhancements to try and make it easier to see what's going on!
 *  V2.0.5 - 04/06/19 - Added importUrl. Volume Control overhaul. Code cleanup.
 *  V2.0.4 - 03/22/19 - Added a new option: restoreVolume for Echo Speaks devices
 *  V2.0.3 - 03/20/19 - Changed the wording on whether to turn the option for 'Echo Speaks' on or off.
 *  V2.0.2 - 02/26/19 - Reworked how the messages are stored. Added option to have random greetings. Removed Greeting and Messages
 *						from Parent app.
 *  V2.0.1 - 02/11/19 - Trobleshooting problem with Friendly Name - Fixed
 *  V2.0.0 - 02/11/19 - Major rewrite. Presence sensors are now in Parent app, so they can be shared across multiple child apps.
 *						Welcome Home now requires a new 'Virtual Device' using our custom 'Welcome Home Driver'.  Each child app
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
 *  V1.0.0 - 11/25/18 - Initial release. This app was influnced by message central (@Cobra) and used a template. Thank you.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "WelcomeHomeChildVersion"
	state.version = "v2.1.2"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
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
    page name: "presenceOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
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
            href "presenceOptions", title:"Presence Options", description:"Click here to setup the Presence Options"
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
			paragraph "NOTE: Not all speakers can use volume controls. If you would like to use volume controls with Echo devices please use the app 'Echo Speaks' and then choose the 'Music Player' option instead of Spech Synth."
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
			paragraph "* Vitual Device must use our custom 'Welcome Home Driver'"
			input "gvDevice", "capability.actuator", title: "Virtual Device created for Welcome Home", required: true, multiple: false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def presenceOptions(){
    dynamicPage(name: "presenceOptions", title: "Presence Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options")) { 
			paragraph "If a presence sensor has been detected for less than x minutes (set the minutes below), after the trigger, then speak the message."
			paragraph "Note: If you are not seeing your 'Friendly Names', then go back to the parent app, enter them in and hit 'done' before setting up any child apps."
            input "timeHome", "number", title: "How many minutes can the presence sensor be home and still be considered for a welcome home message (default=10)", required: true, defaultValue: 10  
            
			input(name: "ps1a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName1}", description: "PS1", submitOnChange: true)
			if(ps1a) input("presenceSensor1", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			input(name: "ps2a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName2}", description: "PS2", submitOnChange: true)
			if(ps2a) input("presenceSensor2", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			input(name: "ps3a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName3}", description: "PS3", submitOnChange: true)
			if(ps3a) input("presenceSensor3", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			input(name: "ps4a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName4}", description: "PS4", submitOnChange: true)
			if(ps4a) input("presenceSensor4", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			input(name: "ps5a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName5}", description: "PS5", submitOnChange: true)
			if(ps5a) input("presenceSensor5", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            
            input(name: "ps6a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName6}", description: "PS6", submitOnChange: true)
			if(ps6a) input("presenceSensor6", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps7a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName7}", description: "PS7", submitOnChange: true)
			if(ps7a) input("presenceSensor7", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps8a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName8}", description: "PS8", submitOnChange: true)
			if(ps8a) input("presenceSensor8", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps9a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName9}", description: "PS9", submitOnChange: true)
			if(ps9a) input("presenceSensor9", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps10a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName10}", description: "PS10", submitOnChange: true)
			if(ps10a) input("presenceSensor10", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            
            input(name: "ps11a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName11}", description: "PS11", submitOnChange: true)
			if(ps11a) input("presenceSensor11", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps12a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName12}", description: "PS12", submitOnChange: true)
			if(ps12a) input("presenceSensor12", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps13a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName13}", description: "PS13", submitOnChange: true)
			if(ps13a) input("presenceSensor13", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps14a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName14}", description: "PS14", submitOnChange: true)
			if(ps14a) input("presenceSensor14", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps15a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName15}", description: "PS15", submitOnChange: true)
			if(ps15a) input("presenceSensor15", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            
            input(name: "ps16a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName16}", description: "PS16", submitOnChange: true)
			if(ps16a) input("presenceSensor16", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps17a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName17}", description: "PS17", submitOnChange: true)
			if(ps17a) input("presenceSensor17", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps18a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName18}", description: "PS18", submitOnChange: true)
			if(ps18a) input("presenceSensor18", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps19a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName19}", description: "PS19", submitOnChange: true)
			if(ps19a) input("presenceSensor19", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            input(name: "ps20a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName20}", description: "PS20", submitOnChange: true)
			if(ps20a) input("presenceSensor20", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
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

	if(presenceSensor1) subscribe(presenceSensor1, "presence", presenceSensorHandler1)
	if(presenceSensor2) subscribe(presenceSensor2, "presence", presenceSensorHandler2)
	if(presenceSensor3) subscribe(presenceSensor3, "presence", presenceSensorHandler3)
	if(presenceSensor4) subscribe(presenceSensor4, "presence", presenceSensorHandler4)
	if(presenceSensor5) subscribe(presenceSensor5, "presence", presenceSensorHandler5)
    if(presenceSensor6) subscribe(presenceSensor6, "presence", presenceSensorHandler6)
    if(presenceSensor7) subscribe(presenceSensor7, "presence", presenceSensorHandler7)
    if(presenceSensor8) subscribe(presenceSensor8, "presence", presenceSensorHandler8)
    if(presenceSensor9) subscribe(presenceSensor9, "presence", presenceSensorHandler9)
    if(presenceSensor10) subscribe(presenceSensor10, "presence", presenceSensorHandler10)
    if(presenceSensor11) subscribe(presenceSensor11, "presence", presenceSensorHandler11)
    if(presenceSensor12) subscribe(presenceSensor12, "presence", presenceSensorHandler12)
    if(presenceSensor13) subscribe(presenceSensor13, "presence", presenceSensorHandler13)
    if(presenceSensor14) subscribe(presenceSensor14, "presence", presenceSensorHandler14)
    if(presenceSensor15) subscribe(presenceSensor15, "presence", presenceSensorHandler15)
    if(presenceSensor16) subscribe(presenceSensor16, "presence", presenceSensorHandler16)
    if(presenceSensor17) subscribe(presenceSensor17, "presence", presenceSensorHandler17)
    if(presenceSensor18) subscribe(presenceSensor18, "presence", presenceSensorHandler18)
    if(presenceSensor19) subscribe(presenceSensor19, "presence", presenceSensorHandler19)
    if(presenceSensor20) subscribe(presenceSensor20, "presence", presenceSensorHandler20)
	
	if(triggerMode == "Door_Lock"){subscribe(lock1, "lock", lockHandler)}
	if(triggerMode == "Contact_Sensor"){subscribe(contactSensor, "contact", contactSensorHandler)}
	if(triggerMode == "Motion_Sensor"){subscribe(motionSensor1, "motion", motionSensorHandler)}
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def setupNewStuff() {
	if(logEnable) log.debug "In setupNewStuff - Setting up Maps"
	if(state.presenceMap == null) state.presenceMap = [:]
}
		
def presenceSensorHandler1(evt){
	state.presenceSensorValue1 = evt.value   
	if(logEnable) log.debug "In presenceSensorHandler1 - ${parent.friendlyName1} - Presence Sensor: ${state.presenceSensorValue1}"
    if(state.presenceSensorValue1 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName1} - Presence Sensor is not present - Been Here is now off."
		state.globalBH1 = "no"
		gvDevice.sendDataMap1(state.globalBH1)
    } else {
		if(logEnable) log.debug "${parent.friendlyName1} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler2(evt){
	state.presenceSensorValue2 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler2 - ${parent.friendlyName2} - Presence Sensor: ${state.presenceSensorValue2}"
    if(state.presenceSensorValue2 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName2} - Presence Sensor is not present - Been Here is now off."
		state.globalBH2 = "no"
		gvDevice.sendDataMap2(state.globalBH2)
    } else {
		if(logEnable) log.debug "${parent.friendlyName2} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler3(evt){
	state.presenceSensorValue3 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler3 - ${parent.friendlyName3} - Presence Sensor: ${state.presenceSensorValue3}"
    if(state.presenceSensorValue3 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName3} - Presence Sensor is not present - Been Here is now off."
		state.globalBH3 = "no"
		gvDevice.sendDataMap3(state.globalBH3)
    } else {
		if(logEnable) log.debug "${parent.friendlyName3} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler4(evt){
	state.presenceSensorValue4 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler4 - ${parent.friendlyName4} - Presence Sensor: ${state.presenceSensorValue4}"
    if(state.presenceSensorValue4 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName4} - Presence Sensor is not present - Been Here is now off."
		state.globalBH4 = "no"
		gvDevice.sendDataMap4(state.globalBH4)
    } else {
		if(logEnable) log.debug "${parent.friendlyName4} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler5(evt){
	state.presenceSensorValue5 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler5 - ${parent.friendlyName5} - Presence Sensor: ${state.presenceSensorValue5}"
    if(state.presenceSensorValue5 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName5} - Presence Sensor is not present - Been Here is now off."
		state.globalBH5 = "no"
		gvDevice.sendDataMap5(state.globalBH5)
    } else {
		if(logEnable) log.debug "${parent.friendlyName5} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler6(evt){
	state.presenceSensorValue6 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler6 - ${parent.friendlyName6} - Presence Sensor: ${state.presenceSensorValue6}"
    if(state.presenceSensorValue6 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName6} - Presence Sensor is not present - Been Here is now off."
		state.globalBH6 = "no"
		gvDevice.sendDataMap6(state.globalBH6)
    } else {
		if(logEnable) log.debug "${parent.friendlyName6} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler7(evt){
	state.presenceSensorValue7 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler7 - ${parent.friendlyName7} - Presence Sensor: ${state.presenceSensorValue7}"
    if(state.presenceSensorValue7 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName7} - Presence Sensor is not present - Been Here is now off."
		state.globalBH7 = "no"
		gvDevice.sendDataMap7(state.globalBH7)
    } else {
		if(logEnable) log.debug "${parent.friendlyName7} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler8(evt){
	state.presenceSensorValue8 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler8 - ${parent.friendlyName8} - Presence Sensor: ${state.presenceSensorValue8}"
    if(state.presenceSensorValue8 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName8} - Presence Sensor is not present - Been Here is now off."
		state.globalBH8 = "no"
		gvDevice.sendDataMap8(state.globalBH8)
    } else {
		if(logEnable) log.debug "${parent.friendlyName8} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler9(evt){
	state.presenceSensorValue9 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler9 - ${parent.friendlyName9} - Presence Sensor: ${state.presenceSensorValue9}"
    if(state.presenceSensorValue9 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName9} - Presence Sensor is not present - Been Here is now off."
		state.globalBH9 = "no"
		gvDevice.sendDataMap9(state.globalBH9)
    } else {
		if(logEnable) log.debug "${parent.friendlyName9} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler10(evt){
	state.presenceSensorValue10 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler10 - ${parent.friendlyName10} - Presence Sensor: ${state.presenceSensorValue10}"
    if(state.presenceSensorValue10 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName10} - Presence Sensor is not present - Been Here is now off."
		state.globalBH10 = "no"
		gvDevice.sendDataMap10(state.globalBH10)
    } else {
		if(logEnable) log.debug "${parent.friendlyName10} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler11(evt){
	state.presenceSensorValue11 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler11 - ${parent.friendlyName11} - Presence Sensor: ${state.presenceSensorValue11}"
    if(state.presenceSensorValue11 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName11} - Presence Sensor is not present - Been Here is now off."
		state.globalBH11 = "no"
		gvDevice.sendDataMap11(state.globalBH11)
    } else {
		if(logEnable) log.debug "${parent.friendlyName11} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler12(evt){
	state.presenceSensorValue12 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler12 - ${parent.friendlyName12} - Presence Sensor: ${state.presenceSensorValue12}"
    if(state.presenceSensorValue12 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName12} - Presence Sensor is not present - Been Here is now off."
		state.globalBH12 = "no"
		gvDevice.sendDataMap12(state.globalBH12)
    } else {
		if(logEnable) log.debug "${parent.friendlyName12} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler13(evt){
	state.presenceSensorValue13 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler13 - ${parent.friendlyName13} - Presence Sensor: ${state.presenceSensorValue13}"
    if(state.presenceSensorValue13 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName13} - Presence Sensor is not present - Been Here is now off."
		state.globalBH13 = "no"
		gvDevice.sendDataMap13(state.globalBH13)
    } else {
		if(logEnable) log.debug "${parent.friendlyName13} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler14(evt){
	state.presenceSensorValue14 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler14 - ${parent.friendlyName14} - Presence Sensor: ${state.presenceSensorValue14}"
    if(state.presenceSensorValue14 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName14} - Presence Sensor is not present - Been Here is now off."
		state.globalBH14 = "no"
		gvDevice.sendDataMap14(state.globalBH14)
    } else {
		if(logEnable) log.debug "${parent.friendlyName14} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler15(evt){
	state.presenceSensorValue15 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler15 - ${parent.friendlyName15} - Presence Sensor: ${state.presenceSensorValue15}"
    if(state.presenceSensorValue15 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName15} - Presence Sensor is not present - Been Here is now off."
		state.globalBH15 = "no"
		gvDevice.sendDataMap15(state.globalBH15)
    } else {
		if(logEnable) log.debug "${parent.friendlyName15} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler16(evt){
	state.presenceSensorValue16 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler16 - ${parent.friendlyName16} - Presence Sensor: ${state.presenceSensorValue16}"
    if(state.presenceSensorValue16 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName16} - Presence Sensor is not present - Been Here is now off."
		state.globalBH16 = "no"
		gvDevice.sendDataMap16(state.globalBH16)
    } else {
		if(logEnable) log.debug "${parent.friendlyName16} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler17(evt){
	state.presenceSensorValue17 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler17 - ${parent.friendlyName17} - Presence Sensor: ${state.presenceSensorValue17}"
    if(state.presenceSensorValue17 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName17} - Presence Sensor is not present - Been Here is now off."
		state.globalBH17 = "no"
		gvDevice.sendDataMap17(state.globalBH17)
    } else {
		if(logEnable) log.debug "${parent.friendlyName17} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler18(evt){
	state.presenceSensorValue18 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler18 - ${parent.friendlyName18} - Presence Sensor: ${state.presenceSensorValue18}"
    if(state.presenceSensorValue18 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName18} - Presence Sensor is not present - Been Here is now off."
		state.globalBH18 = "no"
		gvDevice.sendDataMap18(state.globalBH18)
    } else {
		if(logEnable) log.debug "${parent.friendlyName18} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler19(evt){
	state.presenceSensorValue19 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler19 - ${parent.friendlyName19} - Presence Sensor: ${state.presenceSensorValue19}"
    if(state.presenceSensorValue19 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName19} - Presence Sensor is not present - Been Here is now off."
		state.globalBH19 = "no"
		gvDevice.sendDataMap19(state.globalBH19)
    } else {
		if(logEnable) log.debug "${parent.friendlyName19} - Presence Sensor is present - Let's go!"
    }
}

def presenceSensorHandler20(evt){
	state.presenceSensorValue20 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler20 - ${parent.friendlyName20} - Presence Sensor: ${state.presenceSensorValue20}"
    if(state.presenceSensorValue20 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName20} - Presence Sensor is not present - Been Here is now off."
		state.globalBH20 = "no"
		gvDevice.sendDataMap20(state.globalBH20)
    } else {
		if(logEnable) log.debug "${parent.friendlyName20} - Presence Sensor is present - Let's go!"
    }
}

def lockHandler(evt) {
	state.lockStatus = evt.value
	state.lockName = evt.displayName
	if(logEnable) log.debug "In lockHandler - Lock: ${state.lockName} - Status: ${state.lockStatus}"
	if(state.lockStatus == "unlocked") {
		if(logEnable) log.debug "In lockHandler..."
        whosHere()
	}
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	state.contactName = evt.displayName
	if(logEnable) log.debug "In contactSensorHandler - Contact: ${state.contactName} - Status: ${state.contactStatus}"
	if(csOpenClosed == "Open") {
		if(state.contactStatus == "open") {
			if(logEnable) log.debug "In contactSensorHandler..."
			whosHere()
		}
	}
	if(csOpenClosed == "Closed") {
		if(state.contactStatus == "closed") {
			if(logEnable) log.debug "In contactSensorHandler..."
			whosHere()
		}
	}
}

def motionSensorHandler(evt) {
	state.motionStatus = evt.value
	state.motionName = evt.displayName
	if(logEnable) log.debug "In motionSensorHandler - Motion Name: ${state.motionName} - Status: ${state.motionStatus}"
	if(state.motionStatus == "active") {
		if(logEnable) log.debug "In motionSensorHandler..."
		whosHere()
	}
}

def whosHere() {
    state.presenceMap = [:]
	state.nameCount = 0
	state.canSpeak = "no"
    if(presenceSensor1) getTimeDiff(1)
	if(presenceSensor2) getTimeDiff(2)
	if(presenceSensor3) getTimeDiff(3)
	if(presenceSensor4) getTimeDiff(4)
	if(presenceSensor5) getTimeDiff(5)
    if(presenceSensor6) getTimeDiff(6)
    if(presenceSensor7) getTimeDiff(7)
    if(presenceSensor8) getTimeDiff(8)
    if(presenceSensor9) getTimeDiff(9)
    if(presenceSensor10) getTimeDiff(10)
    if(presenceSensor11) getTimeDiff(11)
    if(presenceSensor12) getTimeDiff(12)
    if(presenceSensor13) getTimeDiff(13)
    if(presenceSensor14) getTimeDiff(14)
    if(presenceSensor15) getTimeDiff(15)
    if(presenceSensor16) getTimeDiff(16)
    if(presenceSensor17) getTimeDiff(17)
    if(presenceSensor18) getTimeDiff(18)
    if(presenceSensor19) getTimeDiff(19)
    if(presenceSensor20) getTimeDiff(20)
    
	if(state.canSpeak == "yes") letsTalk()
}

def getTimeDiff(numb) {
    if(numb == 1) { pSensor = presenceSensor1;fName = "${parent.friendlyName1}";globalBH = "${state.globalBH1}";sendDataM = "sendDataMap1";globalBH = gvDevice.currentValue("globalBH1") }
    if(numb == 2) { pSensor = presenceSensor2;fName = "${parent.friendlyName2}";globalBH = "${state.globalBH2}";sendDataM = "sendDataMap2";globalBH = gvDevice.currentValue("globalBH2") }
    if(numb == 3) { pSensor = presenceSensor3;fName = "${parent.friendlyName3}";globalBH = "${state.globalBH3}";sendDataM = "sendDataMap3";globalBH = gvDevice.currentValue("globalBH3") }
    if(numb == 4) { pSensor = presenceSensor4;fName = "${parent.friendlyName4}";globalBH = "${state.globalBH4}";sendDataM = "sendDataMap4";globalBH = gvDevice.currentValue("globalBH4") }
    if(numb == 5) { pSensor = presenceSensor5;fName = "${parent.friendlyName5}";globalBH = "${state.globalBH5}";sendDataM = "sendDataMap5";globalBH = gvDevice.currentValue("globalBH5") }
    if(numb == 6) { pSensor = presenceSensor6;fName = "${parent.friendlyName6}";globalBH = "${state.globalBH6}";sendDataM = "sendDataMap6";globalBH = gvDevice.currentValue("globalBH6") }
    if(numb == 7) { pSensor = presenceSensor7;fName = "${parent.friendlyName7}";globalBH = "${state.globalBH7}";sendDataM = "sendDataMap7";globalBH = gvDevice.currentValue("globalBH7") }
    if(numb == 8) { pSensor = presenceSensor8;fName = "${parent.friendlyName8}";globalBH = "${state.globalBH8}";sendDataM = "sendDataMap8";globalBH = gvDevice.currentValue("globalBH8") }
    if(numb == 9) { pSensor = presenceSensor9;fName = "${parent.friendlyName9}";globalBH = "${state.globalBH9}";sendDataM = "sendDataMap9";globalBH = gvDevice.currentValue("globalBH9") }
    if(numb == 10) { pSensor = presenceSensor10;fName = "${parent.friendlyName10}";globalBH = "${state.globalBH10}";sendDataM = "sendDataMap10";globalBH = gvDevice.currentValue("globalBH10") }
    if(numb == 11) { pSensor = presenceSensor11;fName = "${parent.friendlyName11}";globalBH = "${state.globalBH11}";sendDataM = "sendDataMap11";globalBH = gvDevice.currentValue("globalBH11") }
    if(numb == 12) { pSensor = presenceSensor12;fName = "${parent.friendlyName12}";globalBH = "${state.globalBH12}";sendDataM = "sendDataMap12";globalBH = gvDevice.currentValue("globalBH12") }
    if(numb == 13) { pSensor = presenceSensor13;fName = "${parent.friendlyName13}";globalBH = "${state.globalBH13}";sendDataM = "sendDataMap13";globalBH = gvDevice.currentValue("globalBH13") }
    if(numb == 14) { pSensor = presenceSensor14;fName = "${parent.friendlyName14}";globalBH = "${state.globalBH14}";sendDataM = "sendDataMap14";globalBH = gvDevice.currentValue("globalBH14") }
    if(numb == 15) { pSensor = presenceSensor15;fName = "${parent.friendlyName15}";globalBH = "${state.globalBH15}";sendDataM = "sendDataMap15";globalBH = gvDevice.currentValue("globalBH15") }
    if(numb == 16) { pSensor = presenceSensor16;fName = "${parent.friendlyName16}";globalBH = "${state.globalBH16}";sendDataM = "sendDataMap16";globalBH = gvDevice.currentValue("globalBH16") }
    if(numb == 17) { pSensor = presenceSensor17;fName = "${parent.friendlyName17}";globalBH = "${state.globalBH17}";sendDataM = "sendDataMap17";globalBH = gvDevice.currentValue("globalBH17") }
    if(numb == 18) { pSensor = presenceSensor18;fName = "${parent.friendlyName18}";globalBH = "${state.globalBH18}";sendDataM = "sendDataMap18";globalBH = gvDevice.currentValue("globalBH18") }
    if(numb == 19) { pSensor = presenceSensor19;fName = "${parent.friendlyName19}";globalBH = "${state.globalBH19}";sendDataM = "sendDataMap19";globalBH = gvDevice.currentValue("globalBH19") }
    if(numb == 20) { pSensor = presenceSensor20;fName = "${parent.friendlyName20}";globalBH = "${state.globalBH20}";sendDataM = "sendDataMap20";globalBH = gvDevice.currentValue("globalBH20") }

    if(logEnable) log.debug "In getTimeDiff New ${numb} - ${fName}"
	def sensorStatus = pSensor.currentValue("presence")
	if(logEnable) log.debug "${fName} - Presence Sensor Status: ${sensorStatus}"
	if(sensorStatus == "present") {
		if(logEnable) log.debug "${fName} - Global Been Here: ${globalBH}"
		def lastActivity = pSensor.getLastActivity()
		
		if(logEnable) log.debug "${fName} - lastActivity: ${lastActivity}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "${fName} - timeDiff: ${timeDiff}"
  		if(timeDiff < timeHome) {
			if(globalBH == "no") {
				log.info "${app.label} - ${fName} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
				state.presenceMap = ["${fName}"]
				state.canSpeak = "yes"
				globalBH = "yes"
                dataMap = "${globalBH}:yes"
				gvDevice."${sendDataM}"(globalBH)
			} else {
                log.info "${app.label} - ${fName} - Global 'Been Here' is ${globalBH}. No announcement needed."
			}
		} else {
			globalBH = "no"
            dataMap = "${globalBH}:no"
			gvDevice."${sendDataM}"(globalBH)
            log.info "${app.label} - ${fName} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
        if(logEnable) log.debug "${fName} - Global Been Here: ${globalBH}"
        if(logEnable) log.debug "${fName} - Presence Sensor: ${sensorStatus} - No announcement needed."
		globalBH = "no"
        dataMap = "${globalBH}:no"
        gvDevice."${sendDataM}"(globalBH)
	}
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
		if(logEnable) log.debug "In letsTalk - Waiting ${delay1} seconds to Speak"
		def delay1ms = delay1 * 1000
		pauseExecution(delay1ms)
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
		state.canSpeak = "no"
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
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
   	
	if (theMessage.contains("%greeting%")) {theMessage = theMessage.replace('%greeting%', getGreeting() )}
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', getName() )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
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

private getGreeting(){						// modified from @Cobra Code
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
			if(logEnable) log.debug "In getGreeting - Static - greeting: ${state.greeting}"
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

// ********** Normal Stuff **********

def setDefaults(){
	setupNewStuff()
	if(logEnable == null){logEnable = false}
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
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Welcome Home - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
