/**
 *  ****************  Notifier Plus Child App  ****************
 *
 *  Design Usage:
 *  Notifications based on date/day, time and more. A perfect way to get reminders or create a wakeup alarm.
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
 *  V1.0.5 - 02/28/19 - Changed 'Speaker Synth' speaker device into two branches - Google Speakers and other speakers, fixes an 
 *						issue with 'Initialize Google' speaker option.  Contact Sensors triggers and Switch Triggers can now act
 *						as the Control Switch. Delay notifications now work.
 *  V1.0.4 - 02/28/19 - Fixed speaking bug.
 *  V1.0.3 - 02/27/19 - Attempt to fix a bad bug in the letsTalk / messageHandler routines.
 *  V1.0.2 - 02/27/19 - Name change to Notifier Plus. Added in triggers for Contact Sensors and Switches. (more to come!)
 *  V1.0.1 - 02/24/19 - Added color to lighting options. Other code cleanup.
 *  V1.0.0 - 02/22/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.5"
}

definition(
    name: "Notifier Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Notifications based on date/day, time and more. A perfect way to get reminders or create a wakeup alarm.",
    category: "",
	parent: "BPTWorld:Notifier Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Notifier Plus</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Notifications based on date/day, time and more. A perfect way to get reminders or create a wakeup alarm."
			paragraph "Get nofified when it's a holiday, birthday, special occasion, etc. Great for telling Hubitat when it's school vacation."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Select Trigger Type")) {
			if((xDate && xDay) || (xDate && xContact) || (xDate && xSwitch) || (xDay && xContact) || (xDay && xSwitch) || (xContact && xSwitch)) {
				paragraph "Please only choose <b>one</b> option. <b>BAD THINGS WILL HAPPEN IF MULTIPLE OPTIONS ARE USED!</b>"
			} else {
				paragraph "Please only choose <b>one</b> option. If multiple options are selected bad things will happen."
			}
			input(name: "xDate", type: "bool", defaultValue: "false", title: "<b>by Date?</b><br>This will notify you on the Month/Day(s)/Year selected only.", description: "Date", submitOnChange: "true", width: 6)
			input(name: "xDay", type: "bool", defaultValue: "false", title: "<b>by Day of the Week?</b><br>This will notify you on each day selected, week after week, at the time specified.", description: "Day of the Week", submitOnChange: "true", width: 6)
			input(name: "xContact", type: "bool", defaultValue: "false", title: "<b>by Contact Sensor?</b><br>Contact Sensor Notifications", description: "Contact Sensor Notifications", submitOnChange: "true", width: 6)
			input(name: "xSwitch", type: "bool", defaultValue: "false", title: "<b>by Device?</b><br>Device Notifications", description: "Device Notifications", submitOnChange: "true", width: 6)
		}
		section("<b>Please remember to clear any selection made before switching to another trigger.</b>") {}
		section() {
			if(xDate) {
				input "month", "enum", title: "Select Month", required: true, multiple: false, width: 4, submitOnChange: true, options: [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
				if(month == "Jan" || month == "Mar" || month == "May" || month == "Jun" || month == "Aug" || month == "Oct" || month == "Dec") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
				if(month == "Apr" || month == "Jun" || month == "Sep" || month == "Nov") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]
				if(month == "Feb") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"]
				input "year", "enum", title: "Select Year", required: true, multiple: false, width: 4, options: [ "2019", "2020", "2021", "2022"], defaultValue: "2019"
				input "hour", "enum", title: "Select Hour (24h format)", required: true, width: 6, options: [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
				//input "min", "enum", title: "Select Minute", required: true, width: 6, options: [ "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"]
				// Used in testing
				input "min", "enum", title: "Select Minute", required: true, width: 6, options: [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"]
			}
		}
		section() {
			if(xDay) {
				input(name: "days", type: "enum", title: "Notify on these days", description: "Days to notify", required: true, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
				input(name: "startTime", type: "time", title: "Time to notify", description: "Time", required: true)
			}
		}
		section() {
			if(xContact) {
				input(name: "contactEvent", type: "capability.contactSensor", title: "Trigger Notifications based on a Contact Sensor", required: true, multiple: true, submitOnChange: true)
				if(contactEvent) {
					input(name: "csOpenClosed", type: "bool", defaultValue: "false", title: "<b>Contact Closed or Opened? (off=Closed, on=Open)</b>", description: "Contact status", submitOnChange: "true")
					if(csOpenClosed) paragraph "You will recieve notifications if any of the contact sensors have been OPENED."
					if(!csOpenClosed) paragraph "You will recieve notifications if any of the contact sensors have been CLOSED."
					input(name: "oContactTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Contact Time", submitOnChange: true)
					if(oContactTime) {
						paragraph "Delay the notification until the device has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
		}
		section() {
			if(xSwitch) {
				input(name: "switchEvent", type: "capability.switch", title: "Trigger Notifications based on a Switch", required: true, multiple: true, submitOnChange: true)
				if(switchEvent) {
					input(name: "seOnOff", type: "bool", defaultValue: "false", title: "<b>Switch Off or On? (off=Off, on=On)</b>", description: "Switch status", submitOnChange: "true")
					if(seOnOff) paragraph "You will recieve notifications if any of the switches are on."
					if(!seOnOff) paragraph "You will recieve notifications if any of the switches are off."
					input(name: "oSwitchTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange: true)
					if(oSwitchTime) {
						paragraph "Delay the notification until the device has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
		}
		section() {
			if((xDate && xDay) || (xDate && xContact) || (xDate && xSwitch) || (xDay && xContact) || (xDay && xSwitch) || (xContact && xSwitch)) {
				paragraph "Please only choose <b>one</b> option. <b>BAD THINGS WILL HAPPEN IF MULTIPLE OPTIONS ARE USED!</b>"
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Choose Your Notify Options")) {
			paragraph "Select as many options as you like. Control switch required for Lighting and/or Message Options."
			input(name: "oControl", type: "bool", defaultValue: "false", title: "<b>Control Switch Options</b>", description: "Control Options", submitOnChange: "true", width: 6)
			input(name: "oLighting", type: "bool", defaultValue: "false", title: "<b>Lighting Options</b>", description: "Light Options", submitOnChange: "true", width: 6)
			input(name: "oDevice", type: "bool", defaultValue: "false", title: "<b>Device Options</b>", description: "Device Options", submitOnChange: "true", width: 6)
			input(name: "oMessage", type: "bool", defaultValue: "false", title: "<b>Message Options</b>", description: "Message Options", submitOnChange: "true", width: 6)
		}
		if(oControl) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Control Switch")) {
				paragraph "This is your child app on/off switch. <b>Required if using Lighting and/or Message Options.</b>"
				if(xContact) {
					paragraph "If choosing to use either Contact or Switch for Control... Be sure to remove any device from the control switch option below."
					input(name: "oControlContact", type: "bool", defaultValue: "false", title: "<b>Use Trigger Contact Sensor as Control Switch?</b>", description: "Control Options", submitOnChange: true)
				}
				if(xSwitch) input(name: "oControlSwitch", type: "bool", defaultValue: "false", title: "<b>Use Trigger Switch as Control Switch?</b>", description: "Control Options", submitOnChange: true)
				if((oControlContact) || (oControlSwitch)) {
					paragraph ""
				} else {
					input(name: "controlSwitch", type: "capability.switch", title: "Turn the app on or off with this switch", required: true, multiple: false)
				}
			}
		}
		if(oLighting) {
    		section(getFormat("header-green", "${getImage("Blank")}"+" Lighting Options")) {
				if(oLighting && !oControl) paragraph "<b>* Control Switch is required when using Lighting options.</b>"
				input(name: "oSetLC", type: "bool", defaultValue: "false", title: "<b>Turn Light On, Set Level and Color</b>", description: "Light On", submitOnChange: "true", width: 12)
				input(name: "oDimUp", type: "bool", defaultValue: "false", title: "<b>Slowly Dim Lighting UP</b>", description: "Dim Up", submitOnChange: "true", width: 6)
				input(name: "oDimDn", type: "bool", defaultValue: "false", title: "<b>Slowly Dim Lighting DOWN</b>", description: "Dim Down", submitOnChange: "true", width: 6)
				if(oSetLC) {
					input "setOnLC", "capability.switchLevel", title: "Select dimmer to turn on", required: true, multiple: true
					input "levelLC", "number", title: "On Level (1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
					input "colorLC", "enum", title: "Color", required: true, multiple:false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				}
				if(oDimUp) {
					input "slowDimmerUp", "capability.switchLevel", title: "Select dimmer devices to slowly rise", required: true, multiple: true
    				input "minutesUp", "number", title: "Takes how many minutes to raise (1 to 60)", required: true, multiple: false, defaultValue:15, range: '1..60'
    				input "targetLevelHigh", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
					input "colorUp", "enum", title: "Color", required: true, multiple:false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
					paragraph "Slowly raising a light level is a great way to wake up in the morning. If you want everything to delay happening until the light reaches its target level, turn this switch on."
					input(name: "oDelay", type: "bool", defaultValue: "false", title: "<b>Delay Until Finished</b>", description: "Future Options", submitOnChange: "true")
				}
				if(oDimDn) {
					input "slowDimmerDn", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required: true, multiple: true
    				input "minutesDn", "number", title: "Takes how many minutes to dim (1 to 60)", required: true, multiple: false, defaultValue:15, range: '1..60'
    				input "targetLevelLow", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 0, range: '1..99'
					input(name: "dimDnOff", type: "bool", defaultValue: "false", title: "<b>Turn dimmer off after target is reached?</b>", description: "Dim Off Options", submitOnChange: "true")
					input "colorDn", "enum", title: "Color", required: true, multiple:false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				}
			}
		}
		if(oDevice) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
				paragraph "Great for turning on/off alarms, lighting, fans, coffee makers, etc..."
				input(name: "switchesOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "switchesOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				if(xDate || xDay) input(name: "newMode", type: "mode", title: "Change Mode", required: false, multiple: false)
			}
		}
		if(oMessage) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
				if(oMessage && !oControl) paragraph "<b>* Control Switch is required when using Message options.</b>"
				input(name: "oRandom", type: "bool", defaultValue: "false", title: "<b>Random Message?</b>", description: "Random", submitOnChange: "true")
				if(!oRandom) input "message", "text", title: "Message to be spoken", required: true, submitOnChange: true
				if(oRandom) {
					input "message", "text", title: "Message to be spoken - Separate each message with ; ",  required: true
					input(name: "oM1List", type: "bool", defaultValue: "false", title: "Show a list view of random messages?", description: "List View", submitOnChange: "true")
					if(oM1List) {
						def valuesM1 = "${message}".split(";")
						listMapM1 = ""
    					valuesM1.each { itemM1 -> listMapM1 += "${itemM1}<br>" }
						paragraph "${listMapM1}"
					}
				}
				input(name: "oRepeat", type: "bool", defaultValue: "false", title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: "true")
				if(oRepeat) input "repeatSeconds", "number", title: "Repeat message every xx seconds until control switch is turned off (1 to 600)", required: true, multiple: false, defaultValue:10, range: '1..600'
				input(name: "oSpeech", type: "bool", defaultValue: "false", title: "<b>Speech Options</b>", description: "Speech Options", submitOnChange: "true", width: 6)
				input(name: "oPush", type: "bool", defaultValue: "false", title: "<b>Pushover Options</b>", description: "Pushover Options", submitOnChange: "true", width: 6)
			}
		}
		if(oSpeech) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
           		input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
				if (speechMode == "Music Player"){ 
              		input "speaker", "capability.musicPlayer", title: "Choose speaker(s)", required: false, multiple: true, submitOnChange: true
					input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' device?", description: "Echo speaks device")
					input "volume1", "number", title: "Speaker volume", description: "0-100%", required: true, defaultValue: "75"
          		}   
        		if (speechMode == "Speech Synth"){ 
					input "speaker", "capability.speechSynthesis", title: "Choose speaker(s)", required: false, multiple: false
         			input "gSpeaker", "capability.speechSynthesis", title: "Choose Google speaker(s) (Home, Hub, Max and Mini's)", required: false, multiple: true, submitOnChange: true
					if(gSpeaker) input "gInitialize", "bool", title: "Initialize Google devices before sending speech", required: true, defaultValue: false
				}
          	}
      	}
		if(oPush) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Pushover Options")) {
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
		section() {
			paragraph "Global on/off switch. Use it to turn off multiple apps at one time. This switch is included in all BPTWorld Apps."
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable multiple apps with one switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
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
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	logCheck()
    setDefaults()
	
	scheduleHandler()
}

def scheduleHandler(){
	LOGDEBUG("In scheduleHandler...") 
	if(xDate) {
		state.monthName = month   
    	if(state.monthName == "Jan") {state.theMonth = "1"}
    	if(state.monthName == "Feb") {state.theMonth = "2"}
    	if(state.monthName == "Mar") {state.theMonth = "3"}
    	if(state.monthName == "Apr") {state.theMonth = "4"}
    	if(state.monthName == "May") {state.theMonth = "5"}
    	if(state.monthName == "Jun") {state.theMonth = "6"}
    	if(state.monthName == "Jul") {state.theMonth = "7"}
    	if(state.monthName == "Aug") {state.theMonth = "8"}
    	if(state.monthName == "Sep") {state.theMonth = "9"}
    	if(state.monthName == "Oct") {state.theMonth = "10"}
    	if(state.monthName == "Nov") {state.theMonth = "11"}
    	if(state.monthName == "Dec") {state.theMonth = "12"}
		LOGDEBUG("In scheduleHandler - day: ${day}")
		String jDays = day.join(",")
		state.theDays = jDays
		state.theHour = hour
		state.theMin = min
	
    	state.schedule = "0 ${state.theMin} ${state.theHour} ${state.theDays} ${state.theMonth} ? *"
		LOGDEBUG("In scheduleHandler - xTime - schedule: 0 ${state.theMin} ${state.theHour} ${state.theDays} ${state.theMonth} ? *")
    	schedule(state.schedule, magicHappensHandler)
	}
	if(enablerSwitch1) subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	if(controlSwitch) subscribe(controlSwitch, "switch", controlSwitchHandler)

	if(xDay) schedule(startTime, magicHappensHandler)
	if(xContact) subscribe(contactEvent, "contact", contactSensorHandler)
	if(xSwitch) subscribe(switchEvent, "switch", switchHandler)
	
}
def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	LOGDEBUG("In enablerSwitchHandler - Enabler Switch = ${enablerSwitch2}")
	LOGDEBUG("Enabler Switch = $state.enablerSwitch2")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("Enabler Switch is OFF - Child app is active.")
    }
}

def controlSwitchHandler(evt){
	LOGDEBUG("In controlSwitchHandler...Checking what type of trigger to use")
	if((controlSwitch) && (!oControlContact) && (!oControlSwitch)) {
		state.controlSwitch2 = evt.value
		LOGDEBUG("In controlSwitchHandler - Control Switch: ${state.controlSwitch2}")
    	if(state.controlSwitch2 == "on"){
    		log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
		} else {
			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    	}
	}
	
	if(oControlContact) {
		LOGDEBUG("In controlSwitchHandler - Contact Sensor: ${state.contactStatus}")
    	if(state.contactStatus == "open"){
			state.controlSwitch2 = "on"
    		log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
		} else {
			state.controlSwitch2 = "off"
			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    	}
	}
	
	if(oControlSwitch) {
		LOGDEBUG("In controlSwitchHandler - Switch: ${state.switchStatus}")
    	if(state.switchStatus == "on"){
			state.controlSwitch2 = "on"
    		log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
		} else {
			state.controlSwitch2 = "off"
			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    	}
	}
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	controlSwitchHandler()
	if(state.enablerSwitch2 == "off") {
		LOGDEBUG("In contactSensorHandler - contact Status: ${state.contactStatus}")
		if(csOpenClosed) {
			if(state.contactStatus == "open") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In contactSensorHandler...Pause: ${pause1}")
					magicHappensHandler()
				}
			}
			if(state.contactStatus == "closed") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In contactSensorHandler...Pause: ${pause1}")
					reverseTheMagicHandler()
				}
			}
		}
		if(!csOpenClosed) {
			if(state.contactStatus == "closed") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In contactSensorHandler...Pause: ${pause1}")
					magicHappensHandler()
				}
			}
			if(state.contactStatus == "open") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In contactSensorHandler...Pause: ${pause1}")
					reverseTheMagicHandler()
				}
			}
		}
	} else {
		LOGDEBUG("In contactSensorHandler - Enabler Switch is ON - Child app is disabled.")
	}
}

def switchHandler(evt) {
	state.switchStatus = evt.value
	controlSwitchHandler()
	if(state.enablerSwitch2 == "off") {
		LOGDEBUG("In switchHandler - Switch Status: ${state.switchStatus}")
		if(seOnOff) {
			if(state.switchStatus == "on") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In switchHandler...Pause: ${pause1}")
					magicHappensHandler()
				}
			}
			if(state.switchStatus == "off") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In switchHandler...Pause: ${pause1}")
					reverseTheMagicHandler()
				}
			}
		}
		if(!seOnOff) {
			if(state.switchStatus == "off") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In switchHandler...Pause: ${pause1}")
					magicHappensHandler()
				}
			}
			if(state.switchStatus == "on") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In switchHandler...Pause: ${pause1}")
					reverseTheMagicHandler()
				}
			}
		}
	} else {
		LOGDEBUG("In switchHandler - Enabler Switch is ON - Child app is disabled.")
	}
}






def magicHappensHandler() {
	LOGDEBUG("In magicHappensHandler...CS: ${state.controlSwitch2}")
		if(oDelay) {
			LOGDEBUG("In magicHappensHandler...Waiting ${minutesUp} minutes before notifications - CS: ${state.controlSwitch2}")
			if(minutesUp) state.realSeconds = minutesUp * 60
			if(notifyDelay) state.notifyDel = notifyDelay * 60
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) runIn(state.realSeconds,slowOffHandler)
			if(oSetLC && oControl) runIn(state.realSeconds,dimmerOnHandler)
			if(oMessage && oControl) runIn(state.realSeconds,messageHandler)
			if(oPush && oControl) runIn(state.realSeconds,pushHandler)
			if(oSpeech && oControl) runIn(state.realSeconds,letsTalk)
			if(oDevice) runIn(state.realSeconds,switchesOnHandler)
			if(oDevice) runIn(state.realSeconds,switchesOffHandler)
			if(newMode) runIn(state.realSeconds, modeHandler)
		} else if(notifyDelay) {
			LOGDEBUG("In magicHappensHandler...Waiting ${notifyDelay} minutes before notifications - CS: ${state.controlSwitch2}")
			if(minutesUp) state.realSeconds = minutesUp * 60
			if(notifyDelay) state.notifyDel = notifyDelay * 60
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) runIn(state.notifyDel,slowOffHandler)
			if(oSetLC && oControl) runIn(state.notifyDel,dimmerOnHandler)
			if(oMessage && oControl) runIn(state.notifyDel,messageHandler)
			if(oPush && oControl) runIn(state.notifyDel,pushHandler)
			if(oSpeech && oControl) runIn(state.notifyDel,letsTalk)
			if(oDevice) runIn(state.notifyDel,switchesOnHandler)
			if(oDevice) runIn(state.notifyDel,switchesOffHandler)
			if(newMode) runIn(state.notifyDel, modeHandler)
		} else {
			if(minutesUp) state.realSeconds = minutesUp * 60
			if(notifyDelay) state.notifyDel = notifyDelay * 60
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) slowOffHandler()
			if(oSetLC && oControl) dimmerOnHandler()
			if(oMessage && oControl) messageHandler()
			if(oPush && oControl) pushHandler()
			if(oSpeech && oControl) letsTalk()
			if(oDevice) switchesOnHandler()
			if(oDevice) switchesOffHandler()
			if(newMode) modeHandler()
		}
}

def reverseTheMagicHandler() {
	LOGDEBUG("In reverseTheMagicHandler...CS: ${state.controlSwitch2}")
	if(minutesUp) state.realSeconds = minutesUp * 60
	if(notifyDelay) state.notifyDel = notifyDelay * 60
	if(oDimUp && oControl) slowDimmerUp.off()
	if(oDimDn && oControl) slowDimmerDn.off()
	if(oSetLC && oControl) setOnLC.off()
	if(switchesOn) switchesOn.off()
	if(switchesOff) switchesOff.on()
}

def slowOnHandler(evt) {
	if(state.controlSwitch2 == "on") {
		dayOfTheWeekHandler()
		if(state.dotwMatch == "yes") {
			LOGDEBUG("In slowOnHandler...")
			if(state.enablerSwitch2 == "off") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In slowOnHandler...Pause: ${pause1}")
					state.fromWhere = "slowOn"
					state.onLevel = 1
					state.color = "${colorUp}"
					setLevelandColorHandler()
        			state.currentLevel = slowDimmerUp[0].currentLevel
    				if(minutesUp == 0) return
    				seconds = minutesUp * 6
    				state.dimStep = targetLevelHigh / seconds
    				state.dimLevel = state.currentLevel
    				LOGDEBUG("slowOnHandler - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
					if(oDelay) log.info "${app.label} - Will start talking in ${minutesUp} minutes (${state.realSeconds} seconds)"
					runIn(10,dimStepUp)
				}
			} else {
				LOGDEBUG("Enabler Switch is ON - ${app.label} is disabled.")
			}
		} else {
			LOGDEBUG("${app.label} - Day of the Week didn't match - No need to run.")
		}
	} else {
		LOGDEBUG("${app.label} - Control Switch is OFF - No need to run.")
	}
}

def slowOffHandler(evt) {
	if(state.controlSwitch2 == "on") {
		dayOfTheWeekHandler()
		if(state.dotwMatch == "yes") {
			LOGDEBUG("In slowOffHandler...")
			if(state.enablerSwitch2 == "off") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In slowOffandler...Pause: ${pause1}")
					state.fromWhere = "slowOff"
					state.onLevel = 99
					state.color = "${colorDn}"
					setLevelandColorHandler()
        			state.currentLevel = slowDimmerDn[0].currentLevel
    				if(minutesDn == 0) return
    				seconds = minutesDn * 6
    				state.dimStep1 = (targetLevelLow / seconds) * 100
    				state.dimLevel = state.currentLevel
   					LOGDEBUG("slowoffHandler - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
    				runIn(10,dimStepDown)					
				}				
									
			} else {
				LOGDEBUG("Enabler Switch is ON - ${app.label} is disabled.")
			}
		} else {
			LOGDEBUG("${app.label} - Day of the Week didn't match - No need to run.")
		}
	} else {
		LOGDEBUG("${app.label} - Control Switch is ${state.controlSwitch2} - No need to run.")
	}
}

def dimStepUp() {
	LOGDEBUG("In dimStepUp...")
	if(state.controlSwitch2 == "on") {
    	if(state.currentLevel < targetLevelHigh) {
        	state.dimLevel = state.dimLevel + state.dimStep
        	if(state.dimLevel > targetLevelHigh) {state.dimLevel = targetLevelHigh}
       	 	state.currentLevel = state.dimLevel.toInteger()
    		slowDimmerUp.setLevel(state.currentLevel)
       	 	LOGDEBUG("dimStepUp - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
        	runIn(10,dimStepUp)				
		} else {
			LOGDEBUG("dimStepUp - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
			log.info "${app.label} - Target Level has been reached"
		}
	} else {
		log.info "${app.label} - Control Switch is ${state.controlSwitch2}"
	}
}

def dimStepDown() {
	LOGDEBUG("In dimStepDown...")			
    if(state.controlSwitch2 == "on") {
    	if(state.currentLevel > targetLevelLow) {
            state.dimStep = state.dimStep1
        	state.dimLevel = state.dimLevel - state.dimStep
            if(state.dimLevel < targetLevelLow) {state.dimLevel = targetLevelLow}
        	state.currentLevel = state.dimLevel.toInteger()
    		slowDimmerDn.setLevel(state.currentLevel)
            LOGDEBUG("dimStepDown - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
        	runIn(10,dimStepDown)
    	} else {
			if(dimDnOff) slowDimmerDn.off()
			LOGDEBUG("dimStepDown - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
			log.info "${app.label} - Target Level has been reached"
		} 
    } else{
        LOGDEBUG("${app.label} - Control Switch is ${state.controlSwitch2}")
    }						
}

def letsTalk() {							// Modified from @Cobra Code
	if(speaker) LOGDEBUG("In letsTalk...Speaker(s) in use: ${speaker} and controlSwitch2: ${state.controlSwitch2}")
	if(gSpeaker) LOGDEBUG("In letsTalk...gSpeaker(s) in use: ${gSpeaker} and controlSwitch2: ${state.controlSwitch2}")
	if(state.controlSwitch2 == "on") {
		messageHandler()
  		if(speechMode == "Music Player"){
			LOGDEBUG("Music Player - ${state.msg}")
			if(echoSpeaks) {
				speaker.setLevel(volume1)
				speaker.setVolumeSpeakAndRestore(state.volume, state.msg)
			}
			if(!echoSpeaks) {
    			speaker.setLevel(volume1)
    			speaker.playTextAndRestore(state.msg)
			}
  		}   
		if(speechMode == "Speech Synth"){
			LOGDEBUG("Speech Synth - ${state.msg}")
			if(gInitialize) gSpeaker.initialize()
			if(gSpeaker) gSpeaker.speak(state.msg)
			if(speaker) speaker.speak(state.msg)
		}
		if(oRepeat) {
			runIn(repeatSeconds,letsTalk)
		}
	} else {
		log.info "${app.label} - Control Switch is ${state.controlSwitch2}"
		LOGDEBUG("In letsTalk...Okay, I'm done!")
	}
}

def messageHandler() {
	LOGDEBUG("In messageHandler - Control Switch: ${state.controlSwitch2}")
	if(state.controlSwitch2 == "on") {
		if(oRandom) {
			def values = "${message}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.msg = values[randomKey]
			LOGDEBUG("In messageHandler - vSize: ${vSize}, randomKey: ${randomKey}, msgRandom: ${state.msg}")
		} else {
			state.msg = "${message}"
		}
	} else {
		LOGDEBUG("In messageHandler - Control Switch is ${state.controlSwitch2}")
	}
}

def switchesOnHandler() {
	switchesOn.each { it ->
		LOGDEBUG("In switchOnHandler - Turning on ${it}")
		it.on()
	}
}

def switchesOffHandler() {
	switchesOff.each { it ->
		LOGDEBUG("In switchOffHandler - Turning off ${it}")
		it.off()
	}
}

def dimmerOnHandler() {
	LOGDEBUG("In dimmerOnHandler...")
	state.fromWhere = "dimmerOn"
	state.color = "${colorLC}"
	state.onLevel = levelLC
	setLevelandColorHandler()
}

def modeHandler() {
	LOGDEBUG("In modeHandler - Changing mode to ${newMode}")
	setLocationMode(newMode)
}

def dayOfTheWeekHandler() {
	LOGDEBUG("In dayOfTheWeek...")
	if(xDay) {
		state.dotwMatch = "no"
		Calendar date = Calendar.getInstance()
		int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK)
		if(dayOfTheWeek == 1) state.dotWeek = "Sunday"
		if(dayOfTheWeek == 2) state.dotWeek = "Monday"
		if(dayOfTheWeek == 3) state.dotWeek = "Tuesday"
		if(dayOfTheWeek == 4) state.dotWeek = "Wednesday"
		if(dayOfTheWeek == 5) state.dotWeek = "Thursday"
		if(dayOfTheWeek == 6) state.dotWeek = "Friday"
		if(dayOfTheWeek == 7) state.dotWeek = "Saturday"
		LOGDEBUG("In dayOfTheWeek...dayOfTheWeek: ${dayOfTheWeek} dotWeek: ${state.dotWeek}")
		LOGDEBUG("In dayOfTheWeek...days: ${days}")
		def values = "${days}".split(",")
		values.each { it ->
			it2 = it.replace("[","")
			it3 = it2.replace("]","")
			it4 = it3.replace(" ","")
			if(it4 == state.dotWeek) { state.dotwMatch = "yes" }
			LOGDEBUG("In dayOfTheWeekHandler - state.dotWeek: ${state.dotWeek} - values: ${it4}")
		}
		if(state.dotwMatch != "yes") state.dotwMatch = "no"
		LOGDEBUG("In dayOfTheWeekHandler - dotwMatch: ${state.dotwMatch}")
	} else {
		state.dotwMatch = "yes"
	}
}

def pushHandler(){
	count = 0
	if(count == 0) {
		LOGDEBUG("In pushNow...")
		theMessage = "${app.label} - ${state.msg}"
		LOGDEBUG("In pushNow...Sending message: ${theMessage}")
    	sendPushMessage.deviceNotification(theMessage)
		count = count + 1
	}
}

def setLevelandColorHandler() {
	LOGDEBUG("In setLevelandColorHandler - fromWhere: ${state.fromWhere}, onLevel: ${state.onLevel}, color: ${state.color}")
    def hueColor = 0
    def saturation = 100
	int onLevel = state.onLevel
    switch(state.color) {
            case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }
	def value = [switch: "on", hue: hueColor, saturation: saturation, level: onLevel as Integer ?: 100]
    LOGDEBUG("In setLevelandColorHandler - value: $value")
	if(state.fromWhere == "dimmerOn") {
    	setOnLC.each {
        	if (it.hasCommand('setColor')) {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, setColor($value)")
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, setLevel($value)")
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, on()")
            	it.on()
        	}
    	}
	}
	if(state.fromWhere == "slowOn") {
    	slowDimmerUp.each {
        	if (it.hasCommand('setColor')) {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, setColor($value)")
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, setLevel($value)")
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, on()")
            	it.on()
        	}
    	}
	}
	if(state.fromWhere == "slowOff") {
    	slowDimmerDn.each {
        	if (it.hasCommand('setColor')) {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, setColor($value)")
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, setLevel($value)")
            	it.setLevel(level as Integer ?: 100)
        	} else {
            	LOGDEBUG("In setLevelandColorHandler - $it.displayName, on()")
            	it.on()
        	}
    	}
	}
}

// ********** Normal Stuff **********

def pauseOrNot(){						// Modified from @Cobra Code
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
	if(state.controlSwitch2 == null){state.controlSwitch2 = "off"}
	if(notifyDelay == null){notifyDelay = 0}
	if(minutesUp == null){minutesUp = 0}
}

def logCheck(){					// Modified from @Cobra Code
	state.checkLog = logEnable
	if(state.logEnable == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.logEnable == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){				// Modified from @Cobra Code
    try {
		if (settings.logEnable) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
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
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Notifier Plus - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
