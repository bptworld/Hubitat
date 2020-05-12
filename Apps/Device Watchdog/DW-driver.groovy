/**
 *  ****************  Device Watchdog Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Device Watchdog data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  V1.0.7 - 05/09/20 - Changes to match new app
 *  V1.0.6 - 11/26/19 - Minor changes
 *  V1.0.5 - 08/29/19 - App Watchdog compatible
 *  V1.0.4 - 04/16/19 - Cleanup Code, added importURL
 *  V1.0.3 - 03/31/19 - Added support for Status tiles
 *  V1.0.2 - 03/18/19 - Added support for mutiple tiles
 *  V1.0.1 - 02/25/19 - Added Device Status attribute
 *  V1.0.0 - 01/28/19 - Initial release
 */

metadata {
	definition (name: "Device Watchdog Tile", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Watchdog/DW-driver.groovy") {
   		capability "Actuator"
        
        command "clearStates"
		command "sendWatchdogActivityMap", ["string"]	
        command "sendWatchdogBatteryMap", ["string"]	
		command "sendWatchdogStatusMap", ["string"]
        command "sendWatchdogComboActBatMap", ["string"]
		
    	attribute "watchdogActivity1", "string"
		attribute "watchdogActivity2", "string"
		attribute "watchdogActivity3", "string"
		
		attribute "watchdogBattery1", "string"
		attribute "watchdogBattery2", "string"
		attribute "watchdogBattery3", "string"
		
		attribute "watchdogStatus1", "string"
		attribute "watchdogStatus2", "string"
		attribute "watchdogStatus3", "string"
        
        attribute "watchdogComboActBat", "string"
        
        attribute "watchdogActivityCount1", "string"
		attribute "watchdogActivityCount2", "string"
		attribute "watchdogActivityCount3", "string"
		
		attribute "watchdogBatteryCount1", "string"
		attribute "watchdogBatteryCount2", "string"
		attribute "watchdogBatteryCount3", "string"
		
		attribute "watchdogStatusCount1", "string"
		attribute "watchdogStatusCount2", "string"
		attribute "watchdogStatusCount3", "string"
        
        attribute "watchdogComboActBatCount", "string"
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
	    sendEvent(name: "watchdogActivity1", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityCount1", value: activityDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "watchdogActivity2", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityCount2", value: activityDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "watchdogActivity3", value: theData, displayed: true)
        sendEvent(name: "watchdogActivityCount3", value: activityDeviceCount, displayed: true)
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
	    sendEvent(name: "watchdogBattery1", value: theData, displayed: true)
        sendEvent(name: "watchdogBatteryCount1", value: batteryDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "watchdogBattery2", value: theData, displayed: true)
        sendEvent(name: "watchdogBatteryCount2", value: batteryDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "watchdogBattery3", value: theData, displayed: true)
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
	    sendEvent(name: "watchdogStatus1", value: theData, displayed: true)
        sendEvent(name: "watchdogStatusCount1", value: statusDeviceCount, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "watchdogStatus2", value: theData, displayed: true)
        sendEvent(name: "watchdogStatusCount2", value: statusDeviceCount, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "watchdogStatus3", value: theData, displayed: true)
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
	    sendEvent(name: "watchdogComboActBat", value: theData, displayed: true)
        sendEvent(name: "watchdogComboActBatCount", value: comboDeviceCount, displayed: true)
    }
}
