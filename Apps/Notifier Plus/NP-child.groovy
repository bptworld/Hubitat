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
 *  V1.1.7 - 06/06/19 - Added more wording to Volume Control Options. Code cleanup.
 *  V1.1.6 - 06/03/19 - Fixed error with every other week option
 *  V1.1.5 - 05/17/19 - Time can now be spoken in 24 or 12 hour formats
 *  V1.1.4 - 04/25/19 - Fixed a bug when selecting 'Jun'
 *  V1.1.3 - 04/25/19 - Some code tweaking. Killed a few bugs.
 *  V1.1.2 - 04/22/19 - Fixed and error with Push messages. Finally fixed the code so you don't have to flick the 'control switch'
 *						on and off after adjusting child apps!
 *  V1.1.1 - 04/20/19 - Reworked speech options to match my other apps. Added Notifications by Mode. Added 'Hourly option' and an
 *						'every other option' to 'By Day of the week' trigger. Added %time% to speech options.
 *  V1.1.0 - 04/15/19 - Code cleanup
 *  V1.0.9 - 03/07/19 - Reworked Message Section so Control Switch is only needed if Repeat Messages is selected.
 *  V1.0.8 - 03/04/19 - Added Temp, Humidity and Power Triggers
 *  V1.0.7 - 03/02/19 - Added more options to 'repeat message' section.
 *  V1.0.6 - 03/01/19 - Fixed 'pause' button. Fixed 'by Day of the Week'. Special Thank you to RCJordan for all the testing!
 *						Added Motion Sensor to Triggers.
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
	state.version = "v1.1.6"
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
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Notifier%20Plus/NP-child.groovy",
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
			if((xDate && xDay) || (xDate && xContact) || (xDate && xMotion) || (xDate && xSwitch) || (xDate && xTemp) ||  (xDate && xPower) || (xDay && xContact) || (xDay && xMotion) || (xDay && xSwitch) || (xDay && xTemp) || (xDay && xPower)) {
				paragraph "Please only choose <b>one</b> option. <b>BAD THINGS WILL HAPPEN IF MULTIPLE OPTIONS ARE USED!</b>"
			} else {
				paragraph "Please only choose <b>one</b> option. If multiple options are selected bad things will happen."
			}
			input(name: "xDate", type: "bool", defaultValue: "false", title: "<b>by Date?</b><br>This will notify you on the Month/Day(s)/Year selected only.", description: "Date", submitOnChange: "true", width: 6)
			input(name: "xDay", type: "bool", defaultValue: "false", title: "<b>by Day of the Week?</b><br>This will notify you on each day selected, week after week, at the time specified.", description: "Day of the Week", submitOnChange: "true", width: 6)
			input(name: "xContact", type: "bool", defaultValue: "false", title: "<b>by Contact Sensor?</b><br>Contact Sensor Notifications", description: "Contact Sensor Notifications", submitOnChange: "true", width: 6)
			input(name: "xHumidity", type: "bool", defaultValue: "false", title: "<b>by Humidity Level?</b><br>Power Notifications", description: "Humidity Notifications", submitOnChange: "true", width: 6)
			input(name: "xMode", type: "bool", defaultValue: "false", title: "<b>by Mode?</b><br>Power Notifications", description: "Mode Notifications", submitOnChange: "true", width: 6)
			input(name: "xMotion", type: "bool", defaultValue: "false", title: "<b>by Motion Sensor?</b><br>Motion Sensor Notifications", description: "Motion Sensor Notifications", submitOnChange: "true", width: 6)
			input(name: "xSwitch", type: "bool", defaultValue: "false", title: "<b>by Switch?</b><br>Switch Notifications", description: "Switch Notifications", submitOnChange: "true", width: 6)
			input(name: "xTemp", type: "bool", defaultValue: "false", title: "<b>by Temperature?</b><br>Temperature Notifications", description: "Temperature Notifications", submitOnChange: "true", width: 6)
			input(name: "xPower", type: "bool", defaultValue: "false", title: "<b>by Power Meter?</b><br>Power Notifications", description: "Power Notifications", submitOnChange: "true", width: 6)
		}
		section("<b>If you would like to change to a different trigger, be sure to remove all selections associated with that trigger before choosing a different trigger. It is strongly recommended to simply delete this child app and create a new one, if a different trigger is needed.</b>") {
			paragraph "<hr>"
			if(xDate) {
				app.clearSetting("xDay")
				input "month", "enum", title: "Select Month", required: true, multiple: false, width: 4, submitOnChange: true, options: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
				if(month == "Jan" || month == "Mar" || month == "May" || month == "Jul" || month == "Aug" || month == "Oct" || month == "Dec") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
				if(month == "Apr" || month == "Jun" || month == "Sep" || month == "Nov") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]
				if(month == "Feb") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"]
				
				input "year", "enum", title: "Select Year", required: true, multiple: false, width: 4, options: ["2019", "2020", "2021", "2022"], defaultValue: "2019"
				input "hour", "enum", title: "Select Hour (24h format)", required: true, width: 6, options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
				input "min", "enum", title: "Select Minute", required: true, width: 6, options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"]
			}
			if(xDay) {
				input(name: "everyOther", type: "bool", defaultValue: "false", title: "Only run every other week on the day created?", description: "by every other", submitOnChange: "true")
				if(everyOther) {
					paragraph "In order for this to work, you must create the child app ON the day you want it to run, every other week."
					paragraph "<b>ie.</b> If created on Tuesday, it will run every other Tuesday and notify you at the time selected below."
				}
				if(!everyOther) input(name: "days", type: "enum", title: "Notify on these days", description: "Days to notify", required: true, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
				if(!everyOther) input(name: "startTimeHourly", type: "bool", defaultValue: "false", title: "Every hour?", description: "by hour", submitOnChange: "true")
				if((!startTimeHourly) && (!everyOther)) input(name: "startTime", type: "time", title: "Time to notify", description: "Time", required: false)
				if(everyOther) input "hour", "enum", title: "Select Hour (24h format)", required: true, width: 6, options: [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
				if(everyOther) input "min", "enum", title: "Select Minute", required: true, width: 6, options: [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"]
			}
			if(xContact) {
				paragraph "<b>by Contact Sensor</b>"
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
			if(xHumidity) {
				paragraph "<b>by Humidity Level</b>"
				input(name: "humidityEvent", type: "capability.relativeHumidityMeasurement", title: "Trigger Notifications based on Humidity Setpoints", required: true, multiple: true, submitOnChange: true)
				input(name: "oSetPointHigh", type: "bool", defaultValue: "false", title: "<b>Trigger when Humidity is too High?</b>", description: "Humidity High", submitOnChange: true)
				if(oSetPointHigh) input "setPointHigh", "number", title: "Humidity High Setpoint", required: true, defaultValue: 75, submitOnChange: true
				input(name: "oSetPointLow", type: "bool", defaultValue: "false", title: "<b>Trigger when Humidity is too Low?</b>", description: "Humidity Low", submitOnChange: true)
				if(oSetPointLow) input "setPointLow", "number", title: "Humidity Low Setpoint", required: true, defaultValue: 30, submitOnChange: true
				if(powerEvent) {
					if(oSetPointHigh) paragraph "You will recieve notifications if Humidity reading is above ${setPointHigh}"
					if(oSetPointLow) paragraph "You will recieve notifications if Humidity reading is below ${setPointLow}"
					input(name: "oSwitchTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange: true)
					if(oSwitchTime) {
						paragraph "Delay the notification until the device has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
			if(xMode) {
				paragraph "<b>by Mode Changes</b>"
				input(name: "modeEvent", type: "mode", title: "Trigger Notifications based on a Mode", multiple: true, submitOnChange: true)
				if(modeEvent) {
					input(name: "modeOnOff", type: "bool", defaultValue: "false", title: "<b>Mode Inactive or Active? (off=Inactive, on=Active)</b>", description: "Mode status", submitOnChange: "true")
					if(modeOnOff) paragraph "You will recieve notifications if any of the modes are on."
					if(!modeOnOff) paragraph "You will recieve notifications if any of the modes are off."
					input(name: "oSwitchTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange: true)
					if(oSwitchTime) {
						paragraph "Delay the notification until the mode has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
			if(xMotion) {
				paragraph "<b>by Motion Sensor</b>"
				input(name: "motionEvent", type: "capability.motionSensor", title: "Trigger Notifications based on a Motion Sensor", required: true, multiple: true, submitOnChange: true)
				if(motionEvent) {
					input(name: "meOnOff", type: "bool", defaultValue: "false", title: "<b>Motion Inactive or Active? (off=Inactive, on=Active)</b>", description: "Motion status", submitOnChange: "true")
					if(meOnOff) paragraph "You will recieve notifications if any of the sensors are on."
					if(!meOnOff) paragraph "You will recieve notifications if any of the sensors are off."
					input(name: "oSwitchTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange: true)
					if(oSwitchTime) {
						paragraph "Delay the notification until the device has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
			if(xPower) {
				paragraph "<b>by Power Meter</b>"
				input(name: "powerEvent", type: "capability.powerMeter", title: "Trigger Notifications based on Power Setpoints", required: true, multiple: true, submitOnChange: true)
				input(name: "oSetPointHigh", type: "bool", defaultValue: "false", title: "<b>Trigger when Power is too High?</b>", description: "Power High", submitOnChange: true)
				if(oSetPointHigh) input "setPointHigh", "number", title: "Power High Setpoint", required: true, defaultValue: 75, submitOnChange: true
				input(name: "oSetPointLow", type: "bool", defaultValue: "false", title: "<b>Trigger when Power is too Low?</b>", description: "Power Low", submitOnChange: true)
				if(oSetPointLow) input "setPointLow", "number", title: "Power Low Setpoint", required: true, defaultValue: 30, submitOnChange: true
				if(powerEvent) {
					if(oSetPointHigh) paragraph "You will recieve notifications if Power reading is above ${setPointHigh}"
					if(oSetPointLow) paragraph "You will recieve notifications if Power reading is below ${setPointLow}"
					input(name: "oSwitchTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange: true)
					if(oSwitchTime) {
						paragraph "Delay the notification until the device has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
			if(xSwitch) {
				paragraph "<b>by Switch</b>"
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
			if(xTemp) {
				paragraph "<b>by Temperature</b>"
				input(name: "tempEvent", type: "capability.temperatureMeasurement", title: "Trigger Notifications based on Temperature Setpoints", required: true, multiple: true, submitOnChange: true)
				input(name: "oSetPointHigh", type: "bool", defaultValue: "false", title: "<b>Trigger when Temperature is too High?</b>", description: "Temp High", submitOnChange: true)
				if(oSetPointHigh) input "setPointHigh", "number", title: "Temperature High Setpoint", required: true, defaultValue: 75, submitOnChange: true
				input(name: "oSetPointLow", type: "bool", defaultValue: "false", title: "<b>Trigger when Temperature is too Low?</b>", description: "Temp Low", submitOnChange: true)
				if(oSetPointLow) input "setPointLow", "number", title: "Temperature Low Setpoint", required: true, defaultValue: 30, submitOnChange: true
				if(powerEvent) {
					if(oSetPointHigh) paragraph "You will recieve notifications if Temperature reading is above ${setPointHigh}"
					if(oSetPointLow) paragraph "You will recieve notifications if Temperature reading is below ${setPointLow}"
					input(name: "oSwitchTime", type: "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange: true)
					if(oSwitchTime) {
						paragraph "Delay the notification until the device has been in state for XX minutes."
						input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					}
				}
			}
		}
		section() {
			if((xDate && xDay) || (xDate && xContact) || (xDate && xMotion) || (xDate && xSwitch) || (xDate && xTemp) ||  (xDate && xPower) || (xDay && xContact) || (xDay && xMotion) || (xDay && xSwitch) || (xDay && xTemp) || (xDay && xPower)) {
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
				paragraph "This is your child app on/off switch. <b>Required if using Lighting and/or Message Repeat Options.</b>"
				paragraph "If choosing to use either Contact, Motion or Switch for Control...Be sure to remove any device from the control switch option below."
				if(xContact) input(name: "oControlContact", type: "bool", defaultValue: "false", title: "<b>Use Trigger Contact Sensor as Control Switch?</b>", description: "Control Options", submitOnChange: true)
				if(xMotion) input(name: "oControlMotion", type: "bool", defaultValue: "false", title: "<b>Use Trigger Motion Sensor as Control Switch?</b>", description: "Control Options", submitOnChange: true)
				if(xSwitch) input(name: "oControlSwitch", type: "bool", defaultValue: "false", title: "<b>Use Trigger Switch as Control Switch?</b>", description: "Control Options", submitOnChange: true)
				if((oControlContact) || (oControlSwitch) || (oControlMotion)) {
					paragraph ""
					state.controlSW = "no"
				} else {
					input(name: "controlSwitch", type: "capability.switch", title: "Turn the app on or off with this switch", required: true, multiple: false)
					state.controlSW = "yes"
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
				input(name: "oRandom", type: "bool", defaultValue: "false", title: "<b>Random Message?</b>", description: "Random", submitOnChange: "true")
				paragraph "%device% - will speak the Device Name<br>%time% - will speak the current time in 24 h<br>%time1% - will speak the current time in 12 h"
				if(!oRandom) {
					if(xPower || xTemp || xHumidity) {
						if(oSetPointHigh) input "messageH", "text", title: "Message to speak when reading is too high", required: true, submitOnChange: true, defaultValue: "Temp is too high"
						if(oSetPointLow) input "messageL", "text", title: "Message to speak when reading is too low", required: true, submitOnChange: true, defaultValue: "Temp is too low"
						if(oSetPointLow) input "messageB", "text", title: "Message to speak when reading is both too high and too low", required: true, submitOnChange: true, defaultValue: "Temp is out of range"
					} else {
						input "message", "text", title: "Message to speak", required: true, submitOnChange: true
					}
				}
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
				if(oRepeat) {
					paragraph "<b>* Control Switch is required when using Message Repeat option.</b>"
					paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					input "repeatSeconds", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					input "maxRepeats", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: "true"
					if(repeatSeconds) {
						paragraph "Message will repeat every ${repeatSeconds} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats})"
						repeatTimeSeconds = (repeatSeconds * maxRepeats)
						int inputNow=repeatTimeSeconds
						int nDayNow = inputNow / 86400
						int nHrsNow = (inputNow % 86400 ) / 3600
						int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						paragraph "In this case, it would take ${nHrsNow} Hours, ${nMinNow} Mins and ${nSecNow} Seconds to reach the max number of repeats (if Control Switch is not turned off)"
					}
				}
				input(name: "oSpeech", type: "bool", defaultValue: "false", title: "<b>Speech Options</b>", description: "Speech Options", submitOnChange: "true", width: 6)
				input(name: "oPush", type: "bool", defaultValue: "false", title: "<b>Pushover Options</b>", description: "Pushover Options", submitOnChange: "true", width: 6)
			}
		}
		if(oSpeech) {
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
				paragraph "NOTE: Not all speakers can use volume controls. If you would like to use volume controls with Echo devices please use the app 'Echo Speaks' and then choose the 'Music Player' option instead of Speech Synth."
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
		if(oPush) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Pushover Options")) {
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
			}
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
	scheduleHandler()
}

def scheduleHandler(){
	if(logEnable) log.debug "In scheduleHandler..."
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
		if(logEnable) log.debug "In scheduleHandler - day: ${day}"
		String jDays = day.join(",")
		state.theDays = jDays
		state.theHour = hour
		state.theMin = min
	
    	state.schedule = "0 ${state.theMin} ${state.theHour} ${state.theDays} ${state.theMonth} ? *"
		if(logEnable) log.debug "In scheduleHandler - xTime - schedule: 0 ${state.theMin} ${state.theHour} ${state.theDays} ${state.theMonth} ? *"
    	schedule(state.schedule, magicHappensHandler)
	}
	stHourly = "0 0 */2 ? * *"
	if(everyOther) {
		Date futureDate = new Date().plus(14)
		futureDateS = futureDate.format("MM-dd")
		fDateS = futureDateS.split("-")
		if(logEnable) log.debug "In scheduleHandler - 14 Date: ${futureDateS}"
		everyO = "0 ${min} ${hour} ${fDateS[1]} ${fDateS[0]} ? *"
		if(logEnable) log.debug "In scheduleHandler - everyO cron: Sec: 0 Min: ${min} Hour: ${hour} Day: ${fDateS[1]} Month: ${fDateS[0]} DoW: ? Year: *"
	}
	if(controlSwitch) subscribe(controlSwitch, "switch", controlSwitchHandler)
	if(xDay && startTime) schedule(startTime, dayOfTheWeekHandler)
	if(xDay && startTimeHourly) schedule(stHourly, dayOfTheWeekHandler)
	if(xDay && everyOther) schedule(everyO, magicHappensHandler)
	if(xContact) subscribe(contactEvent, "contact", contactSensorHandler)
	if(xSwitch) subscribe(switchEvent, "switch", switchHandler)
	if(xMotion) subscribe(motionEvent, "motion", motionHandler)		
	if(xTemp) subscribe(tempEvent, "temperature", setPointHandler)
	if(xPower) subscribe(powerEvent, "power", setPointHandler)
	if(xHumidity) subscribe(humidityEvent, "humidity", setPointHandler)
	if(xMode) subscribe(location, "mode", modeHandler)
}

def controlSwitchHandler(evt){
	if(logEnable) log.debug "In controlSwitchHandler...Checking what type of trigger to use"
	if(state.controlSW == "yes") {
		state.controlSwitch2 = controlSwitch.currentValue("switch")
		if(logEnable) log.debug "In controlSwitchHandler - Control Switch: ${state.controlSwitch2}"
    	if(state.controlSwitch2 == "on"){
    		log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
		} else {
			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    	}
	}
	if(oControlContact) {
		if(csOpenClosed) {
			if(logEnable) log.debug "In controlSwitchHandler - Contact Sensor: ${state.contactStatus}"
    		if(state.contactStatus == "open"){
				state.controlSwitch2 = "on"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "off"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
		if(!csOpenClosed) {
			if(logEnable) log.debug "In controlSwitchHandler - Contact Sensor: ${state.contactStatus}"
    		if(state.contactStatus == "open"){
				state.controlSwitch2 = "off"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "on"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
	}
	if(oControlSwitch) {
		state.controlSwitch2 = controlSwitch.currentValue("switch")
		if(seOnOff) {
			if(logEnable) log.debug "In controlSwitchHandler - Switch: ${state.switchStatus}"
    		if(state.switchStatus == "on"){
				state.controlSwitch2 = "on"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "off"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
		if(!seOnOff) {
			if(logEnable) log.debug "In controlSwitchHandler - Switch: ${state.switchStatus}"
    		if(state.switchStatus == "on"){
				state.controlSwitch2 = "off"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "on"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
	}
	if(oControlMotion) {
		if(meOnOff) {
			if(logEnable) log.debug "In controlSwitchHandler - Motion: ${state.motionStatus}"
    		if(state.motionStatus == "active"){
				state.controlSwitch2 = "on"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "off"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
		if(!meOnOff) {
			if(logEnable) log.debug "In controlSwitchHandler - Motion: ${state.motionStatus}"
    		if(state.motionStatus == "active"){
				state.controlSwitch2 = "off"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "on"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
	}
	if(oControlMode) {
		if(modeOnOff) {
			if(logEnable) log.debug "In controlSwitchHandler - Mode: ${state.modeStatus}"
    		if(state.modeStatus){
				state.controlSwitch2 = "on"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "off"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
		if(!modeOnOff) {
			if(logEnable) log.debug "In controlSwitchHandler - Mode: ${state.modeStatus}"
    		if(state.modeStatus){
				state.controlSwitch2 = "off"
    			log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
			} else {
				state.controlSwitch2 = "on"
				log.info "${app.label} - Control Switch is set to ${state.controlSwitch2}."
    		}
		}
	}
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		if(csOpenClosed) {
			if(state.contactStatus == "open") {magicHappensHandler()}
			if(state.contactStatus == "closed") {reverseTheMagicHandler()}
		}
		if(!csOpenClosed) {
			if(state.contactStatus == "closed") {magicHappensHandler()}
			if(state.contactStatus == "open") {reverseTheMagicHandler()}
		}
	} else {
		if(logEnable) log.debug "${app.label} - Control Switch is OFF - No need to run."
	}
}

def switchHandler(evt) {
	state.switchStatus = evt.value
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		if(seOnOff) {
			if(state.switchStatus == "on") {magicHappensHandler()}
			if(state.switchStatus == "off") {reverseTheMagicHandler()}
		}
		if(!seOnOff) {
			if(state.switchStatus == "off") {magicHappensHandler()}
			if(state.switchStatus == "on") {reverseTheMagicHandler()}
		}
	} else {
		if(logEnable) log.debug "${app.label} - Control Switch is OFF - No need to run."
	}
}

def modeHandler(evt) {
	state.modeStatus = evt.value
	def modeMatch = modeEvent.contains(location.mode)
	if(modeMatch){
		controlSwitchHandler()
		if(state.controlSwitch2 == "on") {
			if(modeOnOff) {
				if(state.modeStatus) {magicHappensHandler()}
				if(!state.modeStatus) {reverseTheMagicHandler()}
			}
			if(!meOnOff) {
				if(!state.modeStatus) {magicHappensHandler()}
				if(state.modeStatus) {reverseTheMagicHandler()}
			}
		} else {
			if(logEnable) log.debug "${app.label} - Control Switch is OFF - No need to run."
		}
	} else {
		if(logEnable) log.debug "${app.label} - Mode does not match - No need to run."
	}
}

def motionHandler(evt) {
	state.motionStatus = evt.value
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		if(meOnOff) {
			if(state.motionStatus == "active") {magicHappensHandler()}
			if(state.motionStatus == "inactive") {reverseTheMagicHandler()}
		}
		if(!meOnOff) {
			if(state.motionStatus == "inactive") {magicHappensHandler()}
			if(state.motionStatus == "active") {reverseTheMagicHandler()}
		}
	} else {
		if(logEnable) log.debug "${app.label} - Control Switch is OFF - No need to run."
	}
}

def setPointHandler(evt) {
	state.setPointDevice = evt.displayName
	setPointValue = evt.value
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		setPointValue1 = setPointValue.toDouble()
		if(logEnable) log.debug "In setPointHandler - Device: ${state.setPointDevice}, setPointHigh: ${setPointHigh}, setPointLow: ${setPointLow}, Acutal value: ${setPointValue1} - setPointHighOK: ${state.setPointHighOK}, setPointLowOK: ${state.setPointLowOK}"
		// *** setPointHigh ***
		if(oSetPointHigh && !oSetPointLow) {
			if(setPointValue1 > setPointHigh) {
				if(state.setPointHighOK != "no") {
					if(logEnable) log.debug "In setPointHandler (Hgh) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${setPointHigh}"
					state.setPointHighOK = "no"
					magicHappensHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
			if(setPointValue1 < setPointHigh) {
				if(state.setPointHighOK == "no") {
					if(logEnable) log.debug "In setPointHandler (High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is Less THAN setPointHigh: ${setPointHigh}"
					state.setPointHighOK = "yes"
					reverseTheMagicHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
		}
		// *** setPointLow ***
		if(oSetPointLow && !oSetPointHigh) {
			if(setPointValue1 < setPointLow) {
				if(state.setPointLowOK != "no") {
					if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${setPointLow}"
					state.setPointLowOK = "no"
					magicHappensHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
			if(setPointValue1 > setPointLow) {
				if(state.setPointLowOK == "no") {
					if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointLow: ${setPointLow}"
					state.setPointLowOK = "yes"
					reverseTheMagicHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
		}
		// *** Inbetween ***
		if(oSetPointHigh && oSetPointLow) {
			if(setPointValue1 > setPointHigh) {
				if(state.setPointHighOK != "no") {
					if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${setPointHigh}"
					state.setPointHighOK = "no"
					magicHappensHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
			if(setPointValue1 < setPointLow) {
				if(state.setPointLowOK != "no") {
					if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${state.setPointDevice}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${setPointLow}"
					state.setPointLowOK = "no"
					magicHappensHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
			if((setPointValue1 <= setPointHigh) && (setPointValue1 >= setPointLow)) {
				if(state.setPointHighOK == "no" || state.setPointLowOK == "no") {
					if(logEnable) log.debug "InsetPointHandler (Both) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is BETWEEN tempHigh: ${setPointHigh} and setPointLow: ${setPointLow}"
					state.setPointHighOK = "yes"
					state.setPointLowOK = "yes"
					reverseTheMagicHandler()
				} else {
					if(logEnable) log.debug "In setPointHandler (Both) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
				}
			}
		}
	} else {
		if(logEnable) log.debug "${app.label} - Control Switch is OFF - No need to run."
	}
}

def magicHappensHandler() {
	controlSwitchHandler()
	if(logEnable) log.debug "In magicHappensHandler...CS: ${state.controlSwitch2}"
	if(state.controlSwitch2 == "on") {
		if(oDelay) {
			if(logEnable) log.debug "In magicHappensHandler...Waiting ${minutesUp} minutes before notifications - CS: ${state.controlSwitch2}"
			if(minutesUp) state.realSeconds = minutesUp * 60
			if(notifyDelay) state.notifyDel = notifyDelay * 60
			if(maxRepeats) state.numRepeats = 1
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) runIn(state.realSeconds,slowOffHandler)
			if(oSetLC && oControl) runIn(state.realSeconds,dimmerOnHandler)
			if(oMessage) runIn(state.realSeconds,messageHandler)
			if(oPush) runIn(state.realSeconds,pushHandler)
			if(oSpeech) runIn(state.realSeconds,letsTalk)
			if(oDevice) runIn(state.realSeconds,switchesOnHandler)
			if(oDevice) runIn(state.realSeconds,switchesOffHandler)
			if(newMode) runIn(state.realSeconds, modeHandler)
		} else if(notifyDelay) {
			if(logEnable) log.debug "In magicHappensHandler...Waiting ${notifyDelay} minutes before notifications - CS: ${state.controlSwitch2}"
			if(minutesUp) state.realSeconds = minutesUp * 60
			if(notifyDelay) state.notifyDel = notifyDelay * 60
			if(maxRepeats) state.numRepeats = 1
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) runIn(state.notifyDel,slowOffHandler)
			if(oSetLC && oControl) runIn(state.notifyDel,dimmerOnHandler)
			if(oMessage) runIn(state.notifyDel,messageHandler)
			if(oPush) runIn(state.notifyDel,pushHandler)
			if(oSpeech) runIn(state.notifyDel,letsTalk)
			if(oDevice) runIn(state.notifyDel,switchesOnHandler)
			if(oDevice) runIn(state.notifyDel,switchesOffHandler)
			if(newMode) runIn(state.notifyDel, modeHandler)
		} else {
			if(minutesUp) state.realSeconds = minutesUp * 60
			if(notifyDelay) state.notifyDel = notifyDelay * 60
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) slowOffHandler()
			if(oSetLC && oControl) dimmerOnHandler()
			if(oMessage) messageHandler()
			if(oPush) pushHandler()
			if(oSpeech) letsTalk()
			if(oDevice) switchesOnHandler()
			if(oDevice) switchesOffHandler()
			if(newMode) modeHandler()
		}
	} else {
		log.info "${app.label} - Control Switch is OFF - No need to run."
	}
}

def reverseTheMagicHandler() {
	controlSwitchHandler()
	if(logEnable) log.debug "In reverseTheMagicHandler...CS: ${state.controlSwitch2}"
	if(minutesUp) state.realSeconds = minutesUp * 60
	if(notifyDelay) state.notifyDel = notifyDelay * 60
	if(oDimUp && oControl) slowDimmerUp.off()
	if(oDimDn && oControl) slowDimmerDn.off()
	if(oSetLC && oControl) setOnLC.off()
	if(switchesOn) switchesOn.off()
	if(switchesOff) switchesOff.on()
}

def slowOnHandler(evt) {
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		if(logEnable) log.debug "In slowOnHandler..."
		state.fromWhere = "slowOn"
		state.onLevel = 1
		state.color = "${colorUp}"
		setLevelandColorHandler()
       	state.currentLevel = slowDimmerUp[0].currentLevel
    	if(minutesUp == 0) return
    	seconds = minutesUp * 6
    	state.dimStep = targetLevelHigh / seconds
    	state.dimLevel = state.currentLevel
    	if(logEnable) log.debug "slowOnHandler - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
		if(oDelay) log.info "${app.label} - Will start talking in ${minutesUp} minutes (${state.realSeconds} seconds)"
		runIn(10,dimStepUp)
	} else {
		if(logEnable) log.debug "${app.label} - Control Switch is OFF - No need to run."
	}
}

def slowOffHandler(evt) {
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		if(logEnable) log.debug "In slowOffHandler..."
		state.fromWhere = "slowOff"
		state.onLevel = 99
		state.color = "${colorDn}"
		setLevelandColorHandler()
       	state.currentLevel = slowDimmerDn[0].currentLevel
    	if(minutesDn == 0) return
    	seconds = minutesDn * 6
    	state.dimStep1 = (targetLevelLow / seconds) * 100
    	state.dimLevel = state.currentLevel
   		if(logEnable) log.debug "slowoffHandler - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}"
	} else {
		if(logEnable) log.debug "${app.label} - Control Switch is ${state.controlSwitch2} - No need to run."
	}
}

def dimStepUp() {
	controlSwitchHandler()
	if(logEnable) log.debug "In dimStepUp..."
	if(state.controlSwitch2 == "on") {
    	if(state.currentLevel < targetLevelHigh) {
        	state.dimLevel = state.dimLevel + state.dimStep
        	if(state.dimLevel > targetLevelHigh) {state.dimLevel = targetLevelHigh}
       	 	state.currentLevel = state.dimLevel.toInteger()
    		slowDimmerUp.setLevel(state.currentLevel)
       	 	if(logEnable) log.debug "dimStepUp - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
        	runIn(10,dimStepUp)				
		} else {
			if(logEnable) log.debug "dimStepUp - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
			log.info "${app.label} - Target Level has been reached"
		}
	} else {
		log.info "${app.label} - Control Switch is ${state.controlSwitch2}"
	}
}

def dimStepDown() {
	controlSwitchHandler()
	if(logEnable) log.debug "In dimStepDown..."
    if(state.controlSwitch2 == "on") {
    	if(state.currentLevel > targetLevelLow) {
            state.dimStep = state.dimStep1
        	state.dimLevel = state.dimLevel - state.dimStep
            if(state.dimLevel < targetLevelLow) {state.dimLevel = targetLevelLow}
        	state.currentLevel = state.dimLevel.toInteger()
    		slowDimmerDn.setLevel(state.currentLevel)
            if(logEnable) log.debug "dimStepDown - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}"
        	runIn(10,dimStepDown)
    	} else {
			if(dimDnOff) slowDimmerDn.off()
			if(logEnable) log.debug "dimStepDown - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}"
			log.info "${app.label} - Target Level has been reached"
		} 
    } else{
        if(logEnable) log.debug "${app.label} - Control Switch is ${state.controlSwitch2}"
    }						
}

def letsTalk() {
	controlSwitchHandler()
	if(state.controlSwitch2 == "on") {
		if(logEnable) log.debug "In letsTalk..."
		if(speaker) if(logEnable) log.debug "In letsTalk...Speaker(s) in use: ${speaker} and controlSwitch2: ${state.controlSwitch2}"
		if(gSpeaker) if(logEnable) log.debug "In letsTalk...gSpeaker(s) in use: ${gSpeaker} and controlSwitch2: ${state.controlSwitch2}"
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
		}
		if(oRepeat) {
			if(state.controlSwitch2 == "on") {
				if(logEnable) log.debug "In letsTalk - oRepeat - ${state.numRepeats}"
				if(state.numRepeats < maxRepeats) {
					state.numRepeats = state.numRepeats + 1
					runIn(repeatSeconds,letsTalk)
				} else {
					log.info "${app.label} - Max repeats has been reached."
				}
			} else {
				log.info "${app.label} - Set to repeat but Control Switch is Off."
			}
		} else {
			log.info "${app.label} - Control Switch is ${state.controlSwitch2}"
			if(logEnable) log.debug "In letsTalk...Okay, I'm done!"
		}
	} else {
		log.info "${app.label} - Control Switch is ${state.controlSwitch2}"
		if(logEnable) log.debug "In letsTalk...Okay, I'm done!"
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
	if(logEnable) log.debug "In messageHandler - Control Switch: ${state.controlSwitch2}"
	currentDateTime()
	if(oRandom) {
		def values = "${message}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		theMessage = values[randomKey]
		if(logEnable) log.debug "In messageHandler - vSize: ${vSize}, randomKey: ${randomKey}, msgRandom: ${theMessage}"
	} else {
		if(xHumidity || xPower || xTemp) {
			if(logEnable) log.debug "In messageHandler (Humidity) - oSetPointHigh: ${oSetPointHigh}, oSetPointLow: ${oSetPointLow}, state.setPointHighOK: ${state.setPointHighOK}, state.setPointLowOK: ${state.setPointLowOK}"
			if(oSetPointHigh && state.setPointHighOK == "no") theMessage = "${messageH}"
			if(oSetPointLow && state.setPointLowOK == "no") theMessage = "${messageL}"
			if((oSetPointHigh && state.setPointHighOK == "no") && (oSetPointLow && state.setPointLowOK == "no")) theMessage = "${messageB}"
		}
		if(!xPower && !xTemp && !xHumidity) theMessage = "${message}"
	}
	if(logEnable) log.debug "In messageHandler - theMessage: ${theMessage}"
	theMessage = theMessage.toLowerCase()
	if (theMessage.toLowerCase().contains("%device%")) {theMessage = theMessage.toLowerCase().replace('%device%', state.setPointDevice)}
	if (theMessage.toLowerCase().contains("%time%")) {theMessage = theMessage.toLowerCase().replace('%time%', state.theTime)}
	if (theMessage.toLowerCase().contains("%time1%")) {theMessage = theMessage.toLowerCase().replace('%time1%', state.theTime1)}
	state.theMessage = "${theMessage}"
	if(logEnable) log.debug "In messageHandler - msg: ${state.theMessage}"
}

def currentDateTime() {
	if(logEnable) log.debug "In currentDateTime - Control Switch: ${state.controlSwitch2}"
	Date date = new Date()
	String datePart = date.format("dd/MM/yyyy")
	String timePart = date.format("HH:mm")
	String timePart1 = date.format("h:mm a")
	state.theTime = timePart		// 24 h
	state.theTime1 = timePart1		// AM PM
	if(logEnable) log.debug "In currentDateTime - ${state.theTime}"
}

def switchesOnHandler() {
	switchesOn.each { it ->
		if(logEnable) log.debug "In switchOnHandler - Turning on ${it}"
		it.on()
	}
}

def switchesOffHandler() {
	switchesOff.each { it ->
		if(logEnable) log.debug "In switchOffHandler - Turning off ${it}"
		it.off()
	}
}

def dimmerOnHandler() {
	if(logEnable) log.debug "In dimmerOnHandler..."
	state.fromWhere = "dimmerOn"
	state.color = "${colorLC}"
	state.onLevel = levelLC
	setLevelandColorHandler()
}

def modeHandler() {
	if(logEnable) log.debug "In modeHandler - Changing mode to ${newMode}"
	setLocationMode(newMode)
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek..."
	dayMatches = "no"
	if(xDay) {
		Calendar date = Calendar.getInstance()
		int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK)
		if(dayOfTheWeek == 1) state.dotWeek = "Sunday"
		if(dayOfTheWeek == 2) state.dotWeek = "Monday"
		if(dayOfTheWeek == 3) state.dotWeek = "Tuesday"
		if(dayOfTheWeek == 4) state.dotWeek = "Wednesday"
		if(dayOfTheWeek == 5) state.dotWeek = "Thursday"
		if(dayOfTheWeek == 6) state.dotWeek = "Friday"
		if(dayOfTheWeek == 7) state.dotWeek = "Saturday"
		if(logEnable) log.debug "In dayOfTheWeek...dayOfTheWeek: ${dayOfTheWeek} dotWeek: ${state.dotWeek}"
		if(logEnable) log.debug "In dayOfTheWeek...days: ${days}"
		def values = "${days}".split(",")
		values.each { it ->
			it2 = it.replace("[","")
			it3 = it2.replace("]","")
			it4 = it3.replace(" ","")
			if(it4 == state.dotWeek) {
				if(logEnable) log.debug "In dayOfTheWeekHandler - Match: state.dotWeek: ${state.dotWeek} - values: ${it4}"
				dayMatches = "yes"
			} else {
				if(logEnable) log.debug "In dayOfTheWeekHandler - Days set to run (${it4}) does not match today (${state.dotWeek})"
			}
		}
		if(dayMatches == "yes") magicHappensHandler()
	}
}

def pushHandler(){
	count = 0
	if(count == 0) {
		if(logEnable) log.debug "In pushHandler..."
		state.theMsg = "${state.theMessage}"
		theMessage = "${app.label} - ${state.theMsg}"
		if(logEnable) log.debug "In pushHandler...Sending message: ${theMessage}"
    	sendPushMessage.deviceNotification(theMessage)
		count = count + 1
	}
}

def setLevelandColorHandler() {
	if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, onLevel: ${state.onLevel}, color: ${state.color}"
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
    if(logEnable) log.debug "In setLevelandColorHandler - value: $value"
	if(state.fromWhere == "dimmerOn") {
    	setOnLC.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
	if(state.fromWhere == "slowOn") {
    	slowDimmerUp.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
	if(state.fromWhere == "slowOff") {
    	slowDimmerDn.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(level as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.controlSwitch2 == null){state.controlSwitch2 = "off"}
	if(notifyDelay == null){notifyDelay = 0}
	if(minutesUp == null){minutesUp = 0}
	if(startTimeHourly == null){startTimeHourly = false}
	if(oControlContact == null){oControlContact = false}
	if(oControlMotion == null){oControlMotion = false}
	if(oControlSwitch == null){oControlSwitch = false}
	if(state.numRepeats == null){state.numRepeats = 1}
	if(setPointHigh == null){setPointHigh = 0}
	if(setPointLow == null){setPointLow = 0}
	if(state.setPointHighOK == null){state.setPointHighOK = "yes"}
	if(state.setPointLowOK == null){state.setPointLowOK = "yes"}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Notifier Plus - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
