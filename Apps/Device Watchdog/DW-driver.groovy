/**
 *  ****************  Device Watchdog Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Device Watchdog data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 * ------------------------------------------------------------------------------------------------------------------------------
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.4 - 04/16/19 - Cleanup Code, added importURL
 *  V1.0.3 - 03/31/19 - Added support for Status tiles
 *  V1.0.2 - 03/18/19 - Added support for mutiple tiles
 *  V1.0.1 - 02/25/19 - Added Device Status attribute
 *  V1.0.0 - 01/28/19 - Initial release
 */

metadata {
	definition (name: "Device Watchdog Tile", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Watchdog/DW-driver.groovy") {
   		capability "Actuator"

		command "sendWatchdogActivityMap1", ["string"]
		command "sendWatchdogActivityMap2", ["string"]
		command "sendWatchdogActivityMap3", ["string"]
		command "sendWatchdogActivityMap4", ["string"]
		command "sendWatchdogActivityMap5", ["string"]
		
		command "sendWatchdogBatteryMap1", ["string"]
		command "sendWatchdogBatteryMap2", ["string"]
		command "sendWatchdogBatteryMap3", ["string"]
		command "sendWatchdogBatteryMap4", ["string"]
		command "sendWatchdogBatteryMap5", ["string"]
		
		command "sendWatchdogStatusMap1", ["string"]
		command "sendWatchdogStatusMap2", ["string"]
		command "sendWatchdogStatusMap3", ["string"]
		command "sendWatchdogStatusMap4", ["string"]
		command "sendWatchdogStatusMap5", ["string"]
		
    	attribute "watchdogActivity1", "string"
		attribute "watchdogActivity2", "string"
		attribute "watchdogActivity3", "string"
		attribute "watchdogActivity4", "string"
		attribute "watchdogActivity5", "string"
		
		attribute "watchdogBattery1", "string"
		attribute "watchdogBattery2", "string"
		attribute "watchdogBattery3", "string"
		attribute "watchdogBattery4", "string"
		attribute "watchdogBattery5", "string"
		
		attribute "watchdogStatus1", "string"
		attribute "watchdogStatus2", "string"
		attribute "watchdogStatus3", "string"
		attribute "watchdogStatus4", "string"
		attribute "watchdogStatus5", "string"
	}
	preferences() {    	
        section(""){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def sendWatchdogActivityMap1(activityMap1) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity data!"
	state.activityDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.activityDevice1+= "<div style='font-size:.${fontSize}em;'>${activityMap1}</div>"
	state.activityDevice1+= "</td></tr></table>"
	state.activityDevice1Count = state.activityDevice1.length()
	if(state.activityDevice1Count <= 1000) {
		if(logEnable) log.debug "activityDevice1 - has ${state.activityDevice1Count} Characters<br>${state.activityDevice1}"
	} else {
		state.activityDevice1 = "Too many characters to display on Dashboard (${state.activityDevice1Count})"
	}
	sendEvent(name: "watchdogActivity1", value: state.activityDevice1, displayed: true)
}

def sendWatchdogActivityMap2(activityMap2) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity data!"
	state.activityDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.activityDevice2+= "<div style='font-size:.${fontSize}em;'>${activityMap2}</div>"
	state.activityDevice2+= "</td></tr></table>"
	state.activityDevice2Count = state.activityDevice2.length()
	if(state.activityDevice2Count <= 1000) {
		if(logEnable) log.debug "activityDevice2 - has ${state.activityDevice2Count} Characters<br>${state.activityDevice2}"
	} else {
		state.activityDevice2 = "Too many characters to display on Dashboard (${state.activityDevice2Count})"
	}
	sendEvent(name: "watchdogActivity2", value: state.activityDevice2, displayed: true)
}

def sendWatchdogActivityMap3(activityMap3) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity data!"
	state.activityDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.activityDevice3+= "<div style='font-size:.${fontSize}em;'>${activityMap3}</div>"
	state.activityDevice3+= "</td></tr></table>"
	state.activityDevice3Count = state.activityDevice3.length()
	if(state.activityDevice3Count <= 1000) {
		if(logEnable) log.debug "activityDevice3 - has ${state.activityDevice3Count} Characters<br>${state.activityDevice3}"
	} else {
		state.activityDevice3 = "Too many characters to display on Dashboard (${state.activityDevice3Count})"
	}
	sendEvent(name: "watchdogActivity3", value: state.activityDevice3, displayed: true)
}

def sendWatchdogActivityMap4(activityMap4) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity data!"
	state.activityDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.activityDevice4+= "<div style='font-size:.${fontSize}em;'>${activityMap4}</div>"
	state.activityDevice4+= "</td></tr></table>"
	state.activityDevice4Count = state.activityDevice4.length()
	if(state.activityDevice4Count <= 1000) {
		if(logEnable) log.debug "activityDevice4 - has ${state.activityDevice4Count} Characters<br>${state.activityDevice4}"
	} else {
		state.activityDevice4 = "Too many characters to display on Dashboard (${state.activityDevice4Count})"
	}
	sendEvent(name: "watchdogActivity4", value: state.activityDevice4, displayed: true)
}

def sendWatchdogActivityMap5(activityMap5) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity data!"
	state.activityDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.activityDevice5+= "<div style='font-size:.${fontSize}em;'>${activityMap5}</div>"
	state.activityDevice5+= "</td></tr></table>"
	state.activityDevice5Count = state.activityDevice5.length()
	if(state.activityDevice5Count <= 1000) {
		if(logEnable) log.debug "activityDevice5 - has ${state.activityDevice5Count} Characters<br>${state.activityDevice5}"
	} else {
		state.activityDevice5 = "Too many characters to display on Dashboard (${state.activityDevice5Count})"
	}
	sendEvent(name: "watchdogActivity5", value: state.activityDevice5, displayed: true)
}

def sendWatchdogBatteryMap1(batteryMap1) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Battery data!"
	state.batteryDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.batteryDevice1+= "<div style='font-size:.${fontSize}em;'>${batteryMap1}</div>"
	state.batteryDevice1+= "</td></tr></table>"
	state.batteryDevice1Count = state.batteryDevice1.length()
	if(state.batteryDevice1Count <= 1000) {
		if(logEnable) log.debug "batteryDevice1 - has ${state.batteryDevice1Count} Characters<br>${state.batteryDevice1}"
	} else {
		state.batteryDevice1 = "Too many characters to display on Dashboard (${state.batteryDevice1Count})"
	}
	sendEvent(name: "watchdogBattery1", value: state.batteryDevice1, displayed: true)
}

def sendWatchdogBatteryMap2(batteryMap2) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Battery data!"
	state.batteryDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.batteryDevice2+= "<div style='font-size:.${fontSize}em;'>${batteryMap2}</div>"
	state.batteryDevice2+= "</td></tr></table>"
	state.batteryDevice2Count = state.batteryDevice2.length()
	if(state.batteryDevice2Count <= 1000) {
		if(logEnable) log.debug "batteryDevice2 - has ${state.batteryDevice2Count} Characters<br>${state.batteryDevice2}"
	} else {
		state.batteryDevice2 = "Too many characters to display on Dashboard (${state.batteryDevice2Count})"
	}
	sendEvent(name: "watchdogBattery2", value: state.batteryDevice2, displayed: true)
}

def sendWatchdogBatteryMap3(batteryMap3) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Battery data!"
	state.batteryDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.batteryDevice3+= "<div style='font-size:.${fontSize}em;'>${batteryMap3}</div>"
	state.batteryDevice3+= "</td></tr></table>"
	state.batteryDevice3Count = state.batteryDevice3.length()
	if(state.batteryDevice3Count <= 1000) {
		if(logEnable) log.debug "batteryDevice3 - has ${state.batteryDevice3Count} Characters<br>${state.batteryDevice3}"
	} else {
		state.batteryDevice3 = "Too many characters to display on Dashboard (${state.batteryDevice3Count})"
	}
	sendEvent(name: "watchdogBattery3", value: state.batteryDevice3, displayed: true)
}

def sendWatchdogBatteryMap4(batteryMap4) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Battery data!"
	state.batteryDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.batteryDevice4+= "<div style='font-size:.${fontSize}em;'>${batteryMap4}</div>"
	state.batteryDevice4+= "</td></tr></table>"
	state.batteryDevice4Count = state.batteryDevice4.length()
	if(state.batteryDevice4Count <= 1000) {
		if(logEnable) log.debug "batteryDevice4 - has ${state.batteryDevice4Count} Characters<br>${state.batteryDevice4}"
	} else {
		state.batteryDevice4 = "Too many characters to display on Dashboard (${state.batteryDevice4Count})"
	}
	sendEvent(name: "watchdogBattery4", value: state.batteryDevice4, displayed: true)
}

def sendWatchdogBatteryMap5(batteryMap5) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Battery data!"
	state.batteryDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.batteryDevice5+= "<div style='font-size:.${fontSize}em;'>${batteryMap5}</div>"
	state.batteryDevice5+= "</td></tr></table>"
	state.batteryDevice5Count = state.batteryDevice5.length()
	if(state.batteryDevice5Count <= 1000) {
		if(logEnable) log.debug "batteryDevice5 - has ${state.batteryDevice5Count} Characters<br>${state.batteryDevice5}"
	} else {
		state.batteryDevice5 = "Too many characters to display on Dashboard (${state.batteryDevice5Count})"
	}
	sendEvent(name: "watchdogBattery5", value: state.batteryDevice5, displayed: true)
}

def sendWatchdogStatusMap1(statusMap1) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Status data!"
	state.statusDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.statusDevice1+= "<div style='font-size:.${fontSize}em;'>${statusMap1}</div>"
	state.statusDevice1+= "</td></tr></table>"
	sendEvent(name: "watchdogStatus1", value: state.statusDevice1, displayed: true)
}

def sendWatchdogStatusMap2(statusMap2) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Status data!"
	state.statusDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.statusDevice2+= "<div style='font-size:.${fontSize}em;'>${statusMap2}</div>"
	state.statusDevice2+= "</td></tr></table>"
	sendEvent(name: "watchdogStatus2", value: state.statusDevice2, displayed: true)
}

def sendWatchdogStatusMap3(statusMap3) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Status data!"
	state.statusDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.statusDevice3+= "<div style='font-size:.${fontSize}em;'>${statusMap3}</div>"
	state.statusDevice3+= "</td></tr></table>"
	sendEvent(name: "watchdogStatus3", value: state.statusDevice3, displayed: true)
}

def sendWatchdogStatusMap4(statusMap4) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Status data!"
	state.statusDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.statusDevice4+= "<div style='font-size:.${fontSize}em;'>${statusMap4}</div>"
	state.statusDevice4+= "</td></tr></table>"
	sendEvent(name: "watchdogStatus4", value: state.statusDevice4, displayed: true)
}

def sendWatchdogStatusMap5(statusMap5) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Status data!"
	state.statusDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.statusDevice5+= "<div style='font-size:.${fontSize}em;'>${statusMap5}</div>"
	state.statusDevice5+= "</td></tr></table>"
	sendEvent(name: "watchdogStatus5", value: state.statusDevice5, displayed: true)
}
	
