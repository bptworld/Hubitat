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
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
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
 *	V1.0.1 - 03/20/19 - Major upgrades for Hubitat's new dashboard requirements
 *  V1.0.0 - 01/25/19 - Initial release
 */

metadata {
	definition (name: "Abacus - Counting Tile", namespace: "BPTWorld", author: "Bryan Turcotte") {
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
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
//received new counts from Abacus - Intense Counting 
def sendMotionMap1(motionMap1) {
    LOGDEBUG("In Abacus - Counting Tile 1 - Received new Motion counts!")
	state.motionDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice1+= "<div style='font-size:.${fontSize}em;'>${motionMap1}</div>"
	state.motionDevice1+= "</td></tr></table>"
	sendEvent(name: "abacusMotion1", value: state.motionDevice1, displayed: true)
}

def sendMotionMap2(motionMap2) {
    LOGDEBUG("In Abacus - Counting Tile 2 - Received new Motion counts!")
	state.motionDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice2+= "<div style='font-size:.${fontSize}em;'>${motionMap2}</div>"
	state.motionDevice2+= "</td></tr></table>"
	sendEvent(name: "abacusMotion2", value: state.motionDevice2, displayed: true)
}

def sendMotionMap3(motionMap3) {
    LOGDEBUG("In Abacus - Counting Tile 3 - Received new Motion counts!")
	state.motionDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice3+= "<div style='font-size:.${fontSize}em;'>${motionMap3}</div>"
	state.motionDevice3+= "</td></tr></table>"
	sendEvent(name: "abacusMotion3", value: state.motionDevice3, displayed: true)
}

def sendMotionMap4(motionMap4) {
    LOGDEBUG("In Abacus - Counting Tile 4 - Received new Motion counts!")
	state.motionDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice4+= "<div style='font-size:.${fontSize}em;'>${motionMap4}</div>"
	state.motionDevice4+= "</td></tr></table>"
	sendEvent(name: "abacusMotion4", value: state.motionDevice4, displayed: true)
}

def sendMotionMap5(motionMap5) {
    LOGDEBUG("In Abacus - Counting Tile 5 - Received new Motion counts!")
	state.motionDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice5+= "<div style='font-size:.${fontSize}em;'>${motionMap5}</div>"
	state.motionDevice5+= "</td></tr></table>"
	sendEvent(name: "abacusMotion5", value: state.motionDevice5, displayed: true)
}

def sendContactMap1(contactMap1) {
    LOGDEBUG("In Abacus - Counting Tile 1 - Received new Contact counts!")
	state.contactDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice1+= "<div style='font-size:.${fontSize}em;'>${contactMap1}</div>"
	state.contactDevice1+= "</td></tr></table>"
	sendEvent(name: "abacusContact1", value: state.contactDevice1, displayed: true)
}

def sendContactMap2(contactMap2) {
    LOGDEBUG("In Abacus - Counting Tile 2 - Received new Contact counts!")
	state.contactDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice2+= "<div style='font-size:.${fontSize}em;'>${contactMap2}</div>"
	state.contactDevice2+= "</td></tr></table>"
	sendEvent(name: "abacusContact2", value: state.contactDevice2, displayed: true)
}

def sendContactMap3(contactMap3) {
    LOGDEBUG("In Abacus - Counting Tile 3 - Received new Contact counts!")
	state.contactDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice3+= "<div style='font-size:.${fontSize}em;'>${contactMap3}</div>"
	state.contactDevice3+= "</td></tr></table>"
	sendEvent(name: "abacusContact3", value: state.contactDevice3, displayed: true)
}

def sendContactMap4(contactMap4) {
    LOGDEBUG("In Abacus - Counting Tile 4 - Received new Contact counts!")
	state.contactDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice4+= "<div style='font-size:.${fontSize}em;'>${contactMap4}</div>"
	state.contactDevice4+= "</td></tr></table>"
	sendEvent(name: "abacusContact4", value: state.contactDevice4, displayed: true)
}
def sendContactMap5(contactMap5) {
    LOGDEBUG("In Abacus - Counting Tile 5 - Received new Contact counts!")
	state.contactDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice5+= "<div style='font-size:.${fontSize}em;'>${contactMap5}</div>"
	state.contactDevice5+= "</td></tr></table>"
	sendEvent(name: "abacusContact5", value: state.contactDevice5, displayed: true)
}

def sendSwitchMap1(switchMap1) {
    LOGDEBUG("In Abacus - Counting Tile 1 - Received new Switch counts!")
	state.switchDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice1+= "<div style='font-size:.${fontSize}em;'>${switchMap1}</div>"
	state.switchDevice1+= "</td></tr></table>"
	sendEvent(name: "abacusSwitch1", value: state.switchDevice1, displayed: true)
}

def sendSwitchMap2(switchMap2) {
    LOGDEBUG("In Abacus - Counting Tile 2 - Received new Switch counts!")
	state.switchDevice2 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice2+= "<div style='font-size:.${fontSize}em;'>${switchMap2}</div>"
	state.switchDevice2+= "</td></tr></table>"
	sendEvent(name: "abacusSwitch2", value: state.switchDevice2, displayed: true)
}
def sendSwitchMap3(switchMap3) {
    LOGDEBUG("In Abacus - Counting Tile 3 - Received new Switch counts!")
	state.switchDevice3 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice3+= "<div style='font-size:.${fontSize}em;'>${switchMap3}</div>"
	state.switchDevice3+= "</td></tr></table>"
	sendEvent(name: "abacusSwitch3", value: state.switchDevice3, displayed: true)
}
def sendSwitchMap4(switchMap4) {
    LOGDEBUG("In Abacus - Counting Tile 4 - Received new Switch counts!")
	state.switchDevice4 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice4+= "<div style='font-size:.${fontSize}em;'>${switchMap4}</div>"
	state.switchDevice4+= "</td></tr></table>"
	sendEvent(name: "abacusSwitch4", value: state.switchDevice4, displayed: true)
}
def sendSwitchMap5(switchMap5) {
    LOGDEBUG("In Abacus - Counting Tile 5 - Received new Switch counts!")
	state.switchDevice5 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice5+= "<div style='font-size:.${fontSize}em;'>${switchMap5}</div>"
	state.switchDevice5+= "</td></tr></table>"
	sendEvent(name: "abacusSwitch5", value: state.switchDevice5, displayed: true)
}

def sendThermostatMap1(thermostatMap1) {
    LOGDEBUG("In Abacus - Counting Tile 1 - Received new Thermostat counts!")
	state.thermostatDevice1 = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.thermostatDevice1+= "<div style='font-size:.${fontSize}em;'>${thermostatMap1}</div>"
	state.thermostatDevice1+= "</td></tr></table>"
	sendEvent(name: "abacusThermostat1", value: state.thermostatDevice1, displayed: true)
}
	
def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
	
