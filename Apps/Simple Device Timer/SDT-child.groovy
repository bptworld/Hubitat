/**
 *  ****************  Simple Device Timer Child App  ****************
 *
 *  Design Usage:
 *  Simple Device Timer with safety checks, multiple timers, notifications and restrictions.
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
 *  V1.0.2 - 06/09/19 - Fixed issue with multiple schedules
 *  V1.0.1 - 06/03/19 - Code cleanup
 *  V1.0.0 - 05/22/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.2"
}

definition(
    name: "Simple Device Timer Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Simple Device Timer with safety checks, multiple timers, notifications and restrictions.",
    category: "Convenience",
	parent: "BPTWorld:Simple Device Timer",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Device%20Timer/SDT-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Simple Device Timer</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Simple Device Timer with safety checks, multiple timers, notifications and restrictions."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Devices")) {
			input "valveDevice", "capability.switch", title: "Select Switch Device", required: true	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Schedule")) {
			input(name: "days", type: "enum", title: "Only run on these days", description: "Days to run device", required: true, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
			paragraph "Select up to 3 sessions per day."
			input "startTime1", "time", title: "Time to turn on 1", required: true, width: 6
        	input "offTime1", "time", title: "Time to turn off 1", required: true, width: 6
			paragraph "<hr>"
			input "startTime2", "time", title: "Time to turn on 2", required: false, width: 6
        	input "offTime2", "time", title: "Time to turn off 2", required: false, width: 6
			paragraph "<hr>"
			input "startTime3", "time", title: "Time to turn on 3", required: false, width: 6
        	input "offTime3", "time", title: "Time to turn off 3", required: false, width: 6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Safety Features")) {
			paragraph "App can send the on/off command several times with a 20 second delay between commands, until either max tries is reached or device reports that it is on/off. Once max tries is reached, a notification will can be sent (if selected below)."
			input "maxTriesOn", "number", title: "Attempts to turn ON", required: true, defaultValue: 3, width: 6
			input "maxTriesOff", "number", title: "Attempts to turn OFF", required: true, defaultValue: 5, width: 6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Other Checks")) {
			paragraph "Simple Pool Timer can be disabled using any 'Switch' type device. I highly recommend using WATO to turn a virtual switch on/off based on any device parameter."
			paragraph "If ANY of the options below are ON, schedule will be cancelled."
			input "rainSensor", "capability.switch", title: "Switch 1", required: false
			input "windSensor", "capability.switch", title: "Switch 2", required: false
			input "otherSensor", "capability.switch", title: "Switch 3", required: false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
			input "sendPushMessage", "capability.notification", title: "Send a notification?", multiple: true, required: false
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
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
	schedule(startTime1, turnValveOn, [overwrite: false])
	schedule(offTime1, turnValveOff, [overwrite: false])
	if(startTime2) schedule(startTime2, turnValveOn, [overwrite: false])
	if(startTime2) schedule(offTime2, turnValveOff, [overwrite: false])
	if(startTime3) schedule(startTime3, turnValveOn, [overwrite: false])
	if(startTime3) schedule(offTime3, turnValveOff, [overwrite: false])
}
	
def turnValveOn() {
	state.valveStatus = valveDevice.currentValue("switch")
	dayOfTheWeekHandler()
	checkForWeather()
	if(state.daysMatch == "yes") {
		if(state.canWater == "yes") {
			if(logEnable) log.debug "In turnValveOn..."
			def valveTry = 0
			if(state.valveStatus == "off") {
				valveTry = valveTry + 1
				if(logEnable) log.debug "In turnValveOn - trying to turn on - will check again in 20 seconds"
				valveDevice.on()
				if(valveTry <= maxTriesOn) runIn(20, turnValveOn)		// Repeat for safety
				if(valveTry > maxTriesOn) {
					log.warn "${valveDevice} didn't turn on after ${maxTriesOn} tries."
					state.msg = "${valveDevice} didn't turn on after ${maxTriesOn} tries. Please CHECK device."
					if(sendPushMessage) pushHandler()
				}
			} else {
				if(logEnable) log.debug "In turnValveOn - Valve is now ${state.valveStatus}"
				log.warn "${valveDevice} is now ${state.valveStatus}"
				state.msg = "${valveDevice} is now ${state.valveStatus}"
				if(sendPushMessage) pushHandler()
			}
		}
	} else {
		log.info "${app.label} didn't pass other checks. ${valveDevice} not turned on."
		state.msg = "${app.label} didn't pass other checks. ${valveDevice} will not turn on."
		turnValveOff()
	}
}

def turnValveOff() {
	state.valveStatus = valveDevice.currentValue("switch")
	if(logEnable) log.debug "In turnValveOff..."
	def valveTryOff = 1
    if(state.valveStatus == "on") {
		valveTryOff = valveTryOff + 1
		if(logEnable) log.debug "In turnValveOff - trying to turn off - will check again in 20 seconds"
		valveDevice.off()
    	if(valveTryOff <= maxTriesOff) runIn(20, turnValveOff)		// Repeat for safety
		if(valveTryOff > maxTriesOff) {
			log.warn "${valveDevice} didn't turn off after ${maxTriesOff} tries."
			state.msg = "${valveDevice} didn't turn off after ${maxTriesOff} tries. Please CHECK device."
			if(sendPushMessage) pushHandler()
		}
	} else {
		if(logEnable) log.debug "In turnValveOff - Valve is now ${state.valveStatus}"
		log.warn "${valveDevice} is now ${state.valveStatus}"
		if(state.canWater == "yes") state.msg = "${valveDevice} is now ${state.valveStatus}"
		if(state.canWater == "no")  state.msg = "${app.label} didn't pass other checks. ${valveDevice} is now ${state.valveStatus}"
		if(sendPushMessage) pushHandler()
	}
}

def checkForWeather() {
	if(logEnable) log.debug "In checkForWeather..."
	if(rainSensor) state.rainDevice = rainSensor.currentValue("switch")
	if(windSensor) state.windDevice = windSensor.currentValue("switch")
	if(otherSensor) state.otherDevice = otherSensor.currentValue("switch")
	if(state.rainDevice == "on" || state.windDevice == "on" || state.otherDevice == "on") {
		if(logEnable) log.debug "In checkForWeather - Weather (other) Check failed."
		state.canWater = "no"
	} else {
		if(logEnable) log.debug "In checkForWeather - Weather (other) Check passed."
		state.canWater = "yes"
	}
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek..."
	Calendar date = Calendar.getInstance()
	int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK)
	if(dayOfTheWeek == 1) state.dotWeek = "Sunday"
	if(dayOfTheWeek == 2) state.dotWeek = "Monday"
	if(dayOfTheWeek == 3) state.dotWeek = "Tuesday"
	if(dayOfTheWeek == 4) state.dotWeek = "Wednesday"
	if(dayOfTheWeek == 5) state.dotWeek = "Thursday"
	if(dayOfTheWeek == 6) state.dotWeek = "Friday"
	if(dayOfTheWeek == 7) state.dotWeek = "Saturday"

	if(days.contains(state.dotWeek)) {
		if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Passed"
		state.daysMatch = "yes"
	} else {
		if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Check Failed"
		state.daysMatch = "no"
	}
}

def pushHandler(){
	if(logEnable) log.debug "In pushNow..."
	theMessage = "${app.label} - ${state.msg}"
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.rainDevice == null){state.rainDevice = "off"}
	if(state.windDevice == null){state.windDevice = "off"}
	if(state.otherDevice == null){state.otherDevice = "off"}
	if(state.daysMatch == null){state.daysMatch = "no"}
	if(state.msg == null){state.msg = ""}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Simple Device Timer - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
