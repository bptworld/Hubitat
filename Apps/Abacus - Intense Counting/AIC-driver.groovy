/**
 *  ****************  Abacus - Counting Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Abacus Counting data to be used with Hubitat's Dashboards.
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
 *	V1.0.2 - 04/15/19 - Code cleanup, added importUrl
 *	V1.0.1 - 03/20/19 - Major upgrades for Hubitat's new dashboard requirements
 *  V1.0.0 - 01/25/19 - Initial release
 */

metadata {
	definition (name: "Abacus - Counting Tile", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Abacus%20-%20Intense%20Counting/AIC-driver.groovy") {
   		capability "Actuator"

		command "sendMotionMap1", ["string"]
		command "sendMotionMap2", ["string"]
		command "sendMotionMap3", ["string"]
		command "sendMotionMap4", ["string"]
		command "sendMotionMap5", ["string"]
		
		command "sendContactMap1", ["string"]
		command "sendContactMap2", ["string"]
		command "sendContactMap3", ["string"]
		command "sendContactMap4", ["string"]
		command "sendContactMap5", ["string"]
		
		command "sendSwitchMap1", ["string"]
		command "sendSwitchMap2", ["string"]
		command "sendSwitchMap3", ["string"]
		command "sendSwitchMap4", ["string"]
		command "sendSwitchMap5", ["string"]
		
		command "sendThermostatMap1", ["string"]
		
    	attribute "abacusMotion1", "string"
		attribute "abacusMotion2", "string"
		attribute "abacusMotion3", "string"
		attribute "abacusMotion4", "string"
		attribute "abacusMotion5", "string"
		
		attribute "abacusContact1", "string"
		attribute "abacusContact2", "string"
		attribute "abacusContact3", "string"
		attribute "abacusContact4", "string"
		attribute "abacusContact5", "string"
		
		attribute "abacusSwitch1", "string"
		attribute "abacusSwitch2", "string"
		attribute "abacusSwitch3", "string"
		attribute "abacusSwitch4", "string"
		attribute "abacusSwitch5", "string"
		
		attribute "abacusThermostat1", "string"
	}
	preferences() {    	
        section(""){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
//received new counts from Abacus - Intense Counting 
def sendMotionMap1(motionMap1) {
    if(logEnable) log.debug "In Abacus - Counting Tile 1 - Received new Motion counts!"
	state.motionDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice1+= "<div style='font-size:.${fontSize}em;'>${motionMap1}</div>"
	state.motionDevice1+= "</td></tr></table>"
	state.motionDevice1Count = state.motionDevice1.length()
	if(state.motionDevice1Count <= 1000) {
		if(logEnable) log.debug "motionDevice1 - has ${state.motionDevice1Count} Characters<br>${state.motionDevice1}"
	} else {
		state.motionDevice1 = "Too many characters to display on Dashboard (${state.motionDevice1Count})"
	}
	sendEvent(name: "abacusMotion1", value: state.motionDevice1, displayed: true)
}

def sendMotionMap2(motionMap2) {
    if(logEnable) log.debug "In Abacus - Counting Tile 2 - Received new Motion counts!"
	state.motionDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice2+= "<div style='font-size:.${fontSize}em;'>${motionMap2}</div>"
	state.motionDevice2+= "</td></tr></table>"
	state.motionDevice2Count = state.motionDevice2.length()
	if(state.motionDevice2Count <= 1000) {
		if(logEnable) log.debug "motionDevice2 - has ${state.motionDevice2Count} Characters<br>${state.motionDevice2}"
	} else {
		state.motionDevice2 = "Too many characters to display on Dashboard (${state.motionDevice2Count})"
	}
	sendEvent(name: "abacusMotion2", value: state.motionDevice2, displayed: true)
}

def sendMotionMap3(motionMap3) {
    if(logEnable) log.debug "In Abacus - Counting Tile 3 - Received new Motion counts!"
	state.motionDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice3+= "<div style='font-size:.${fontSize}em;'>${motionMap3}</div>"
	state.motionDevice3+= "</td></tr></table>"
	state.motionDevice3Count = state.motionDevice3.length()
	if(state.motionDevice3Count <= 1000) {
		if(logEnable) log.debug "motionDevice3 - has ${state.motionDevice3Count} Characters<br>${state.motionDevice3}"
	} else {
		state.motionDevice3 = "Too many characters to display on Dashboard (${state.motionDevice3Count})"
	}
	sendEvent(name: "abacusMotion3", value: state.motionDevice3, displayed: true)
}

def sendMotionMap4(motionMap4) {
    if(logEnable) log.debug "In Abacus - Counting Tile 4 - Received new Motion counts!"
	state.motionDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice4+= "<div style='font-size:.${fontSize}em;'>${motionMap4}</div>"
	state.motionDevice4+= "</td></tr></table>"
	state.motionDevice4Count = state.motionDevice4.length()
	if(state.motionDevice4Count <= 1000) {
		if(logEnable) log.debug "motionDevice4 - has ${state.motionDevice4Count} Characters<br>${state.motionDevice4}"
	} else {
		state.motionDevice4 = "Too many characters to display on Dashboard (${state.motionDevice4Count})"
	}
	sendEvent(name: "abacusMotion4", value: state.motionDevice4, displayed: true)
}

def sendMotionMap5(motionMap5) {
    if(logEnable) log.debug "In Abacus - Counting Tile 5 - Received new Motion counts!"
	state.motionDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice5+= "<div style='font-size:.${fontSize}em;'>${motionMap5}</div>"
	state.motionDevice5+= "</td></tr></table>"
	state.motionDevice5Count = state.motionDevice5.length()
	if(state.motionDevice5Count <= 1000) {
		if(logEnable) log.debug "motionDevice5 - has ${state.motionDevice5Count} Characters<br>${state.motionDevice5}"
	} else {
		state.motionDevice5 = "Too many characters to display on Dashboard (${state.motionDevice5Count})"
	}
	sendEvent(name: "abacusMotion5", value: state.motionDevice5, displayed: true)
}

def sendContactMap1(contactMap1) {
    if(logEnable) log.debug "In Abacus - Counting Tile 1 - Received new Contact counts!"
	state.contactDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice1+= "<div style='font-size:.${fontSize}em;'>${contactMap1}</div>"
	state.contactDevice1+= "</td></tr></table>"
	state.contactDevice1Count = state.contactDevice1.length()
	if(state.contactDevice1Count <= 1000) {
		if(logEnable) log.debug "contactDevice1 - has ${state.contactDevice1Count} Characters<br>${state.contactDevice1}"
	} else {
		state.contactDevice1 = "Too many characters to display on Dashboard (${state.contactDevice1Count})"
	}
	sendEvent(name: "abacusContact1", value: state.contactDevice1, displayed: true)
}

def sendContactMap2(contactMap2) {
    if(logEnable) log.debug "In Abacus - Counting Tile 2 - Received new Contact counts!"
	state.contactDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice2+= "<div style='font-size:.${fontSize}em;'>${contactMap2}</div>"
	state.contactDevice2+= "</td></tr></table>"
	state.contactDevice2Count = state.contactDevice2.length()
	if(state.contactDevice2Count <= 1000) {
		if(logEnable) log.debug "contactDevice2 - has ${state.contactDevice2Count} Characters<br>${state.contactDevice2}"
	} else {
		state.contactDevice2 = "Too many characters to display on Dashboard (${state.contactDevice2Count})"
	}
	sendEvent(name: "abacusContact2", value: state.contactDevice2, displayed: true)
}

def sendContactMap3(contactMap3) {
    if(logEnable) log.debug "In Abacus - Counting Tile 3 - Received new Contact counts!"
	state.contactDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice3+= "<div style='font-size:.${fontSize}em;'>${contactMap3}</div>"
	state.contactDevice3+= "</td></tr></table>"
	state.contactDevice3Count = state.contactDevice3.length()
	if(state.contactDevice3Count <= 1000) {
		if(logEnable) log.debug "contactDevice3 - has ${state.contactDevice3Count} Characters<br>${state.contactDevice3}"
	} else {
		state.contactDevice3 = "Too many characters to display on Dashboard (${state.contactDevice3Count})"
	}
	sendEvent(name: "abacusContact3", value: state.contactDevice3, displayed: true)
}

def sendContactMap4(contactMap4) {
    if(logEnable) log.debug "In Abacus - Counting Tile 4 - Received new Contact counts!"
	state.contactDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice4+= "<div style='font-size:.${fontSize}em;'>${contactMap4}</div>"
	state.contactDevice4+= "</td></tr></table>"
	state.contactDevice4Count = state.contactDevice4.length()
	if(state.contactDevice4Count <= 1000) {
		if(logEnable) log.debug "contactDevice4 - has ${state.contactDevice4Count} Characters<br>${state.contactDevice4}"
	} else {
		state.contactDevice4 = "Too many characters to display on Dashboard (${state.contactDevice4Count})"
	}
	sendEvent(name: "abacusContact4", value: state.contactDevice4, displayed: true)
}
def sendContactMap5(contactMap5) {
    if(logEnable) log.debug "In Abacus - Counting Tile 5 - Received new Contact counts!"
	state.contactDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice5+= "<div style='font-size:.${fontSize}em;'>${contactMap5}</div>"
	state.contactDevice5+= "</td></tr></table>"
	state.contactDevice5Count = state.contactDevice5.length()
	if(state.contactDevice5Count <= 1000) {
		if(logEnable) log.debug "contactDevice5 - has ${state.contactDevice5Count} Characters<br>${state.contactDevice5}"
	} else {
		state.contactDevice5 = "Too many characters to display on Dashboard (${state.contactDevice5Count})"
	}
	sendEvent(name: "abacusContact5", value: state.contactDevice5, displayed: true)
}

def sendSwitchMap1(switchMap1) {
    if(logEnable) log.debug "In Abacus - Counting Tile 1 - Received new Switch counts!"
	state.switchDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice1+= "<div style='font-size:.${fontSize}em;'>${switchMap1}</div>"
	state.switchDevice1+= "</td></tr></table>"
	state.switchDevice1Count = state.switchDevice1.length()
	if(state.switchDevice1Count <= 1000) {
		if(logEnable) log.debug "switchDevice1 - has ${state.switchDevice1Count} Characters<br>${state.switchDevice1}"
	} else {
		state.switchDevice1 = "Too many characters to display on Dashboard (${state.switchDevice1Count})"
	}
	sendEvent(name: "abacusSwitch1", value: state.switchDevice1, displayed: true)
}

def sendSwitchMap2(switchMap2) {
    if(logEnable) log.debug "In Abacus - Counting Tile 2 - Received new Switch counts!"
	state.switchDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice2+= "<div style='font-size:.${fontSize}em;'>${switchMap2}</div>"
	state.switchDevice2+= "</td></tr></table>"
	state.switchDevice2Count = state.switchDevice2.length()
	if(state.switchDevice2Count <= 1000) {
		if(logEnable) log.debug "switchDevice2 - has ${state.switchDevice2Count} Characters<br>${state.switchDevice2}"
	} else {
		state.switchDevice2 = "Too many characters to display on Dashboard (${state.switchDevice2Count})"
	}
	sendEvent(name: "abacusSwitch2", value: state.switchDevice2, displayed: true)
}
def sendSwitchMap3(switchMap3) {
    if(logEnable) log.debug "In Abacus - Counting Tile 3 - Received new Switch counts!"
	state.switchDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice3+= "<div style='font-size:.${fontSize}em;'>${switchMap3}</div>"
	state.switchDevice3+= "</td></tr></table>"
	state.switchDevice3Count = state.switchDevice3.length()
	if(state.switchDevice3Count <= 1000) {
		if(logEnable) log.debug "switchDevice3 - has ${state.switchDevice3Count} Characters<br>${state.switchDevice3}"
	} else {
		state.switchDevice3 = "Too many characters to display on Dashboard (${state.switchDevice3Count})"
	}
	sendEvent(name: "abacusSwitch3", value: state.switchDevice3, displayed: true)
}
def sendSwitchMap4(switchMap4) {
    if(logEnable) log.debug "In Abacus - Counting Tile 4 - Received new Switch counts!"
	state.switchDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice4+= "<div style='font-size:.${fontSize}em;'>${switchMap4}</div>"
	state.switchDevice4+= "</td></tr></table>"
	state.switchDevice4Count = state.switchDevice4.length()
	if(state.switchDevice4Count <= 1000) {
		if(logEnable) log.debug "switchDevice4 - has ${state.switchDevice4Count} Characters<br>${state.switchDevice4}"
	} else {
		state.switchDevice4 = "Too many characters to display on Dashboard (${state.switchDevice4Count})"
	}
	sendEvent(name: "abacusSwitch4", value: state.switchDevice4, displayed: true)
}
def sendSwitchMap5(switchMap5) {
    if(logEnable) log.debug "In Abacus - Counting Tile 5 - Received new Switch counts!"
	state.switchDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice5+= "<div style='font-size:.${fontSize}em;'>${switchMap5}</div>"
	state.switchDevice5+= "</td></tr></table>"
	state.switchDevice5Count = state.switchDevice5.length()
	if(state.switchDevice5Count <= 1000) {
		if(logEnable) log.debug "switchDevice5 - has ${state.switchDevice5Count} Characters<br>${state.switchDevice5}"
	} else {
		state.switchDevice5 = "Too many characters to display on Dashboard (${state.switchDevice5Count})"
	}
	sendEvent(name: "abacusSwitch5", value: state.switchDevice5, displayed: true)
}

def sendThermostatMap1(thermostatMap1) {
    if(logEnable) log.debug "In Abacus - Counting Tile 1 - Received new Thermostat counts!"
	state.thermostatDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.thermostatDevice1+= "<div style='font-size:.${fontSize}em;'>${thermostatMap1}</div>"
	state.thermostatDevice1+= "</td></tr></table>"
	state.thermostatDevice1Count = state.thermostatDevice1.length()
	if(state.thermostatDevice1Count <= 1000) {
		if(logEnable) log.debug "thermostatDevice1 - has ${state.thermostatDevice1Count} Characters<br>${state.thermostatDevice1}"
	} else {
		state.thermostatDevice1 = "Too many characters to display on Dashboard (${state.thermostatDevice1Count})"
	}
	sendEvent(name: "abacusThermostat1", value: state.thermostatDevice1, displayed: true)
}
