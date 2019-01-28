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
 *
 *  V1.0.0 - 01/25/19 - Initial release
 */

metadata {
	definition (name: "Abacus - Counting Tile", namespace: "BPTWorld", author: "Bryan Turcotte") {
   		capability "Actuator"

		command "sendMotionMap", ["string"]
		command "sendContactMap", ["string"]
		command "sendSwitchMap", ["string"]
		command "sendThermostatMap", ["string"]
		
    	attribute "abacusMotion", "string"
		attribute "abacusContact", "string"
		attribute "abacusSwitch", "string"
		attribute "abacusThermostat", "string"
	}
	preferences() {    	
        section(""){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
//received new counts from Abacus - Intense Counting 
def sendMotionMap(motionMap) {
    LOGDEBUG("In Abacus - Counting Tile - Received new Motion counts!")
	state.motionDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.motionDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${motionMap}</div>"
	state.motionDevice+= "</td></tr></table>"
	
	sendEvent(name: "abacusMotion", value: state.motionDevice, displayed: true)
}

def sendContactMap(contactMap) {
    LOGDEBUG("In Abacus - Counting Tile - Received new Contact counts!")
	state.contactDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.contactDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${contactMap}</div>"
	state.contactDevice+= "</td></tr></table>"
	
	sendEvent(name: "abacusContact", value: state.contactDevice, displayed: true)
}

def sendSwitchMap(switchMap) {
    LOGDEBUG("In Abacus - Counting Tile - Received new Switch counts!")
	state.switchDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.switchDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${switchMap}</div>"
	state.switchDevice+= "</td></tr></table>"
	
	sendEvent(name: "abacusSwitch", value: state.switchDevice, displayed: true)
}

def sendThermostatMap(thermostatMap) {
    LOGDEBUG("In Abacus - Counting Tile - Received new Thermostat counts!")
	state.thermostatDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.thermostatDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${thermostatMap}</div>"
	state.thermostatDevice+= "</td></tr></table>"
	
	sendEvent(name: "abacusThermostat", value: state.thermostatDevice, displayed: true)
}
	
def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
	
