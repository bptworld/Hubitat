/**
 *  ****************  Simple Irrigation Child App  ****************
 *
 *  Design Usage:
 *  For use with any valve device connected to your hose, like the Orbit Hose Water Timer. Features multiple timers and
 *  restrictions.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
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
 *  2.0.2 - 04/29/20 - Check for days match before turning valve off
 *  2.0.1 - 04/27/20 - Cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  1.0.6 - 06/09/19 - Fixed issue with multiple schedules
 *  1.0.5 - 06/03/19 - Code cleanup
 *  1.0.4 - 05/24/19 - Added more safety features, max retries
 *  1.0.3 - 05/13/19 - Added pushover notifications
 *  1.0.2 - 05/07/19 - Fix one thing break another
 *  1.0.1 - 05/07/19 - Fixed typo with selecting devices
 *  1.0.0 - 04/22/19 - Initial release.
 *
 */

def setVersion(){
    state.name = "Simple Irrigation"
	state.version = "2.0.2"
}

definition(
    name: "Simple Irrigation Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "For use with any valve device connected to your hose, like the Orbit Hose Water Timer. Features multiple timers and restrictions.",
    category: "Convenience",
	parent: "BPTWorld:Simple Irrigation",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Irrigation/SI-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "For use with any valve device connected to your hose, like the Orbit Hose Water Timer. Features multiple timers and restrictions."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Valve Devices")) {
			input "valveDevice", "capability.valve", title: "Select Valve Device", required: true	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Schedule")) {
			input(name: "days", type: "enum", title: "Only water on these days", description: "Days to water", required: true, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
			paragraph "Select up to 3 watering sessions per day."
			input "startTime1", "time", title: "Time to turn on 1", required: true, width: 6
        	input "onLength1", "number", title: "Leave valve on for how long (in minutes)", required: true, width: 6
			paragraph "<hr>"
			input "startTime2", "time", title: "Time to turn on 2", required: false, width: 6
        	input "onLength2", "number", title: "Leave valve on for how long (in minutes)", required: false, width: 6
			paragraph "<hr>"
			input "startTime3", "time", title: "Time to turn on 3", required: false, width: 6
        	input "onLength3", "number", title: "Leave valve on for how long (in minutes)", required: false, width: 6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Safety Features")) {
			paragraph "App can send the open/closed command several times with a 20 second delay between commands, until either max tries is reached or device reports that it is open/closed. Once max tries is reached, a notification will can be sent (if selected below)."
			input "maxTriesOn", "number", title: "Attempts to OPEN", required: true, defaultValue: 3, width: 6
			input "maxTriesOff", "number", title: "Attempts to CLOSE", required: true, defaultValue: 5, width: 6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Check the Weather")) {
			paragraph "Disable app using any 'Switch' type device. I highly recommend using WATO to turn a virtual switch on/off based on any device parameter."
			paragraph "If ANY of the options below are ON, watering will be cancelled."
			input "rainSensor", "capability.switch", title: "Rain Switch", required: false
			input "windSensor", "capability.switch", title: "Wind Switch", required: false
			input "otherSensor", "capability.switch", title: "Other Switch", required: false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
			input "sendPushMessage", "capability.notification", title: "Send a notification?", multiple: true, required: false
            input(name: "sendSafetyPushMessage", type: "bool", title: "Send close notification even if weather switch has cancelled the schedule.", defaultValue: "true", required: true)
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
	if(startTime1) schedule(startTime1, turnValveOn, [overwrite: false])
	if(startTime2) schedule(startTime2, turnValveOn, [overwrite: false])
	if(startTime3) schedule(startTime3, turnValveOn, [overwrite: false])
}
	
def turnValveOn() {
	state.valveStatus = valveDevice.currentValue("valve")
	dayOfTheWeekHandler()
	checkForWeather()
	if(state.daysMatch == "yes") {
		if(state.canWater == "yes") {
			if(logEnable) log.debug "In turnValveOn..."
			def valveTry = 0
			if(state.valveStatus == "closed") {
				valveTry = valveTry + 1
				if(logEnable) log.debug "In turnValveOn - trying to turn on - will check again in 20 seconds"
				valveDevice.open()
				if(valveTry <= maxTriesOn) runIn(20, turnValveOn)		// Repeat for safety
				if(valveTry > maxTriesOn) {
					log.warn "${valveDevice} didn't open after ${maxTriesOn} tries."
					state.msg = "${valveDevice} didn't open after ${maxTriesOn} tries. Please CHECK device."
					if(sendPushMessage) pushHandler()
				}
			} else {
				def delay = onLength1 * 60
				if(logEnable) log.debug "In turnValveOn - Valve is now ${state.valveStatus}, Setting valve timer to off in ${onLength1} minutes"
				log.warn "${valveDevice} is now ${state.valveStatus}"
				state.msg = "${valveDevice} is now ${state.valveStatus}"
				if(sendPushMessage) pushHandler()
				runIn(delay, turnValveOff)
			}
		} else {
			log.info "${app.label} didn't pass weather check. ${valveDevice} not turned on."
			turnValveOff()
		}
	} else {
		log.info "${app.label} didn't pass day check. Water not turned on."
		state.msg = "${app.label} didn't pass day check. ${valveDevice} will not turn on."
		turnValveOff()
	}	
}

def turnValveOff() {		
    dayOfTheWeekHandler()
	if(state.daysMatch == "yes") {
        if(logEnable) log.debug "In turnValveOff..."
        state.valveStatus = valveDevice.currentValue("valve")
        def valveTryOff = 0
        if(state.valveStatus == "open") {
            valveTryOff = valveTryOff + 1
            if(logEnable) log.debug "In turnValveOff - trying to turn off - will check again in 20 seconds"
            valveDevice.close()
            if(valveTryOff <= maxTriesOff) runIn(20, turnValveOff)		// Repeat for safety
            if(valveTryOff > maxTriesOff) {
                log.warn "${valveDevice} didn't close after ${maxTriesOff} tries."
                state.msg = "${valveDevice} didn't close after ${maxTriesOff} tries. Please CHECK device."
                if(sendPushMessage) pushHandler()
            }
        } else {
            if(logEnable) log.debug "In turnValveOff - Valve is now ${state.valveStatus}"
            log.warn "${valveDevice} is now ${state.valveStatus}"
            state.msg = "${valveDevice} is now ${state.valveStatus}"
            if (state.canWater == "no") {
				state.msg = "${valveDevice} is now ${state.valveStatus}. Watering session skipped due to weather switch."
			}
			if ((state.canWater == "no" && sendSafetyPushMessage == true) || (state.canWater == "yes")) {
            	if(sendPushMessage) pushHandler()
			}
        }
    }
}

def checkForWeather() {
	if(logEnable) log.debug "In checkForWeather..."
	if(rainSensor) state.rainDevice = rainSensor.currentValue("switch")
	if(windSensor) state.windDevice = windSensor.currentValue("switch")
	if(otherSensor) state.otherDevice = otherSensor.currentValue("switch")
	if(state.rainDevice == "on" || state.windDevice == "on" || state.otherDevice == "on") {
		if(logEnable) log.debug "In checkForWeather - Weather Check failed."
		state.canWater = "no"
	} else {
		if(logEnable) log.debug "In checkForWeather - Weather Check passed."
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
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
