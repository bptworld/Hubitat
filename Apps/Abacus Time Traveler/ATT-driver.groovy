/**
 *  ****************  Abacus Time Traveler Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Abacus Time Traveler data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
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
 *  1.0.0 - 06/26/20 - Initial release
 */

metadata {
	definition (name: "Abacus Time Traveler Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"

        command "sendContactMap", ["string"]
 		command "sendMotionMap", ["string"]
		command "sendSwitchMap", ["string"]		
		command "sendThermostatMap", ["string"]
		
        attribute "bpt-abacusContact1", "string"
		attribute "bpt-abacusContact2", "string"
		attribute "bpt-abacusContact3", "string"
        
    	attribute "bpt-abacusMotion1", "string"
		attribute "bpt-abacusMotion2", "string"
		attribute "bpt-abacusMotion3", "string"
		
		attribute "bpt-abacusSwitch1", "string"
		attribute "bpt-abacusSwitch2", "string"
		attribute "bpt-abacusSwitch3", "string"
		
		attribute "bpt-abacusThermostat1", "string"
        attribute "bpt-abacusThermostat2", "string"
        attribute "bpt-abacusThermostat3", "string"
        
        attribute "abacusContactCount1", "string"
		attribute "abacusContactCount2", "string"
		attribute "abacusContactCount3", "string"
        
        attribute "abacusMotionCount1", "string"
		attribute "abacusMotionCount2", "string"
		attribute "abacusMotionCount3", "string"
        
        attribute "abacusSwitchCount1", "string"
		attribute "abacusSwitchCount2", "string"
		attribute "abacusSwitchCount3", "string"
		
		attribute "abacusThermostatCount1", "string"
        attribute "abacusThermostatCount2", "string"
        attribute "abacusThermostatCount3", "string"
	}
	preferences() {    	
        section(""){
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def sendContactMap(data) {
    if(logEnable) log.debug "In Abacus Time Traveler Driver - Contact - Received new data!"
    def (whichMap, theData) = data.split('::')
	count = theData.length()
	if(count <= 1024) {
		if(logEnable) log.debug "In Abacus Time Traveler Driver - Contact - Map has ${count} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${count})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-abacusContact1", value: theData, displayed: true)
        sendEvent(name: "abacusContactCount1", value: count, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-abacusContact2", value: theData, displayed: true)
        sendEvent(name: "abacusContactCount2", value: count, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-abacusContact3", value: theData, displayed: true)
        sendEvent(name: "abacusContactCount3", value: count, displayed: true)
    }
}

def sendMotionMap(data) {
    if(logEnable) log.debug "In Abacus Time Traveler Driver - Motion - Received new data!"
    def (whichMap, theData) = data.split('::')
	count = theData.length()
	if(count <= 1024) {
		if(logEnable) log.debug "In Abacus Time Traveler Driver - Motion - Map has ${count} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${count})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-abacusMotion1", value: theData, displayed: true)
        sendEvent(name: "abacusMotionCount1", value: count, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-abacusMotion2", value: theData, displayed: true)
        sendEvent(name: "abacusMotionCount2", value: count, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-abacusMotion3", value: theData, displayed: true)
        sendEvent(name: "abacusMotionCount3", value: count, displayed: true)
    }
}

def sendSwitchMap(data) {
    if(logEnable) log.debug "In Abacus Time Traveler Driver - Switch - Received new data!"
    def (whichMap, theData) = data.split('::')
	count = theData.length()
	if(count <= 1024) {
		if(logEnable) log.debug "In Abacus Time Traveler Driver - Switch - Map has ${count} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${count})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-abacusSwitch1", value: theData, displayed: true)
        sendEvent(name: "abacusSwitchCount1", value: count, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-abacusSwitch2", value: theData, displayed: true)
        sendEvent(name: "abacusSwitchCount2", value: count, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-abacusSwitch3", value: theData, displayed: true)
        sendEvent(name: "abacusSwitchCount3", value: count, displayed: true)
    }
}

def sendThermostatMap(data) {
    if(logEnable) log.debug "In Abacus Time Traveler Driver - Thermostat - Received new data!"
    def (whichMap, theData) = data.split('::')
	count = theData.length()
	if(count <= 1024) {
		if(logEnable) log.debug "In Abacus Time Traveler Driver - Thermostat - Map has ${count} Characters"
	} else {
		theData = "Too many characters to display on Dashboard (${count})"
	}
    if(whichMap == "1") {
	    sendEvent(name: "bpt-abacusThermostat1", value: theData, displayed: true)
        sendEvent(name: "abacusThermostatCount1", value: count, displayed: true)
    }
    if(whichMap == "2") {
	    sendEvent(name: "bpt-abacusThermostat2", value: theData, displayed: true)
        sendEvent(name: "abacusThermostatCount2", value: count, displayed: true)
    }
    if(whichMap == "3") {
	    sendEvent(name: "bpt-abacusThermostat3", value: theData, displayed: true)
        sendEvent(name: "abacusThermostatCount3", value: count, displayed: true)
    }
}
