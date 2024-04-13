/**
 *  **************** Device Watchdog Tile Driver ****************
 *
 *  Design Usage:
 *  This driver formats the Device Watchdog data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019-2024 Bryan Turcotte (@bptworld)
 *  
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums! Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  All changes are listed in the child app
 */

metadata {
	definition (name: "Device Watchdog Tile", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"
        
        command "clearStates"
		command "sendWatchdogActivityMap", ["string"]
        command "sendWatchdogActivityAttMap", ["string"]
        command "sendWatchdogBatteryMap", ["string"]	
		command "sendWatchdogStatusMap", ["string"]
        command "sendWatchdogComboActBatMap", ["string"]
        command "sendWatchdogSpecialMap", ["string"]
		
    	attribute "bpt-watchdogActivity1", "string"
		attribute "bpt-watchdogActivity2", "string"
		attribute "bpt-watchdogActivity3", "string"
        attribute "bpt-ActivityNumOfDevices", "string"
        
        attribute "bpt-watchdogActivityAtt1", "string"
		attribute "bpt-watchdogActivityAtt2", "string"
		attribute "bpt-watchdogActivityAtt3", "string"        
        attribute "bpt-ActivityAttNumOfDevices", "string"
		
		attribute "bpt-watchdogBattery1", "string"
		attribute "bpt-watchdogBattery2", "string"
		attribute "bpt-watchdogBattery3", "string"
		attribute "bpt-BatteryNumOfDevices", "string"
        
		attribute "bpt-watchdogStatus1", "string"
		attribute "bpt-watchdogStatus2", "string"
		attribute "bpt-watchdogStatus3", "string"
        attribute "bpt-StatusNumOfDevices", "string"
        
        attribute "bpt-watchdogComboActBat", "string"
        
        attribute "bpt-watchdogSpecial1", "string"
		attribute "bpt-watchdogSpecial2", "string"
		attribute "bpt-watchdogSpecial3", "string"
        attribute "bpt-SpecialNumOfDevices", "string"
        
        attribute "watchdogActivityCount1", "string"
		attribute "watchdogActivityCount2", "string"
		attribute "watchdogActivityCount3", "string"
        
        attribute "watchdogActivityAttCount1", "string"
		attribute "watchdogActivityAttCount2", "string"
		attribute "watchdogActivityAttCount3", "string"
		
		attribute "watchdogBatteryCount1", "string"
		attribute "watchdogBatteryCount2", "string"
		attribute "watchdogBatteryCount3", "string"
		
		attribute "watchdogStatusCount1", "string"
		attribute "watchdogStatusCount2", "string"
		attribute "watchdogStatusCount3", "string"
        
        attribute "watchdogComboActBatCount", "string"
        
        attribute "watchdogSpecialCount1", "string"
		attribute "watchdogSpecialCount2", "string"
		attribute "watchdogSpecialCount3", "string"
	}
	preferences() {    	
        section(""){
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def clearStates() {
	state.clear()
}

def sendWatchdogActivityMap(activityMap) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity data!"
    def (whichMap, theData) = activityMap.split('::')
	activityDeviceCount = activityMap.length()
	if(activityDeviceCount <= 1024) {
		if(logEnable) log.debug "activityDevice - has ${activityDeviceCount} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${activityDeviceCount})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-watchdogActivity1", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityCount1", value: activityDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-watchdogActivity2", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityCount2", value: activityDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-watchdogActivity3", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityCount3", value: activityDeviceCount, displayed: true)
    }
}

def sendWatchdogActivityAttMap(activityAttMap) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Activity with Attributes data!"
    def (whichMap, theData) = activityAttMap.split('::')
	activityAttDeviceCount = activityAttMap.length()
	if(activityAttDeviceCount <= 1024) {
		if(logEnable) log.debug "activityAttDevice - has ${activityAttDeviceCount} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${activityAttDeviceCount})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-watchdogActivityAtt1", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityAttCount1", value: activityAttDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-watchdogActivityAtt2", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityAttCount2", value: activityAttDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-watchdogActivityAtt3", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityAttCount3", value: activityAttDeviceCount, displayed: true)
    }
}

def sendWatchdogBatteryMap(batteryMap) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Battery data!"
    def (whichMap, theData) = batteryMap.split('::')
	batteryDeviceCount = theData.length()
	if(batteryDeviceCount <= 1024) {
		if(logEnable) log.debug "batteryDevice - has ${batteryDeviceCount} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${batteryDeviceCount})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-watchdogBattery1", value: theData, displayed: true)
        sendEvent(name: "watchdogBatteryCount1", value: batteryDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-watchdogBattery2", value: theData, displayed: true)
        sendEvent(name: "watchdogBatteryCount2", value: batteryDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-watchdogBattery3", value: theData, displayed: true)
        sendEvent(name: "watchdogBatteryCount3", value: batteryDeviceCount, displayed: true)
    }    
}

def sendWatchdogStatusMap(statusMap) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Status data!"
    def (whichMap, theData) = statusMap.split('::')
    statusDeviceCount = theData.length()
	if(statusDeviceCount <= 1024) {
		if(logEnable) log.debug "statusDevice - has ${statusDeviceCount} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${statusDeviceCount})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-watchdogStatus1", value: theData, displayed: true)
        sendEvent(name: "watchdogStatusCount1", value: statusDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-watchdogStatus2", value: theData, displayed: true)
        sendEvent(name: "watchdogStatusCount2", value: statusDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-watchdogStatus3", value: theData, displayed: true)
        sendEvent(name: "watchdogStatusCount3", value: statusDeviceCount, displayed: true)
    }
}

def sendWatchdogComboActBatMap(comboMap) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Combo data!"
    def (whichMap, theData) = comboMap.split('::')
    comboDeviceCount = theData.length()
	if(comboDeviceCount <= 1024) {
		if(logEnable) log.debug "comboDevice - has ${comboDeviceCount} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${comboDeviceCount})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-watchdogComboActBat", value: theData, displayed: true)
        sendEvent(name: "watchdogComboActBatCount", value: comboDeviceCount, displayed: true)
    }
}

def sendWatchdogSpecialMap(specialMap) {
    if(logEnable) log.debug "In Device Watchdog Tile - Received new Special data!"
    def (whichMap, theData) = specialMap.split('::')
	specialDeviceCount = specialMap.length()
	if(specialDeviceCount <= 1024) {
		if(logEnable) log.debug "specialDevice - has ${specialDeviceCount} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${specialDeviceCount})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-watchdogSpecial1", value: theData, displayed: true)
        sendEvent(name: "watchdogSpecialCount1", value: specialDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-watchdogSpecial2", value: theData, displayed: true)
        sendEvent(name: "watchdogSpecialCount2", value: specialDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-watchdogSpecial3", value: theData, displayed: true)
        sendEvent(name: "watchdogSpecialCount3", value: specialDeviceCount, displayed: true)
    }
}
