/**
 *  ****************  Snapshot Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Snapshot data to be used with Hubitat's Dashboards.
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
 *  V1.0.0 - 03/06/19 - Initial release
 */

metadata {
	definition (name: "Snapshot Tile", namespace: "BPTWorld", author: "Bryan Turcotte") {
   		capability "Actuator"

		command "sendSnapshotSwitchOnMap", ["string"]
		command "sendSnapshotSwitchOffMap", ["string"]
		command "sendSnapshotContactOpenMap", ["string"]
		command "sendSnapshotContactClosedMap", ["string"]
		
    	attribute "snapshotSwitch", "string"
		attribute "snapshotContact", "string"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Data Font Size", required: true, defaultValue: "15")
			input("fontSizeS", "text", title: "Date Font Size", required: true, defaultValue: "12")
            input("debugMode", "bool", title: "Enable logging", required: true, defaultValue: true)
        }
    }
}

def sendSnapshotSwitchOnMap(switchOnMap) {
	state.switchOnDevice = "${switchOnMap}"
	sendSnapshotMap()
}

def sendSnapshotSwitchOffMap(switchOffMap) {
	state.switchOffDevice = "${switchOffMap}"
	sendSnapshotMap()
}

def sendSnapshotMap() {
	state.switchDevice1 = "<table width='100%'><tr>"
	state.switchDevice1 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice1 += "<div style='font-size: ${fontSize}px'> ${state.switchOnDevice}</div>"
	state.switchDevice1 += "</td></tr></table>"

	state.switchDevice2 = "<table width='100%'><tr>"
	state.switchDevice2 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice2 += "<div style='font-size: ${fontSize}px'> ${state.switchOffDevice}</div>"
	state.switchDevice2 += "</td></tr></table>"
	
	def rightNowS = new Date()
	fontSizeS = 12
	state.theDateS = "<table width='100%'><tr>"
	state.theDateS += "<td style='text-align: left; width: 100%'>"
	state.theDateS += "<div style='font-size: ${fontSizeS}px'> ${rightNowS}</div>"
	state.theDateS += "</td></tr></table>"
	
	state.switchDevice = "${state.switchDevice1}${state.switchDevice2}${state.theDateS}"
	sendEvent(name: "snapshotSwitch", value: state.switchDevice, displayed: true)
}

def sendSnapshotContactOpenMap(contactOpenMap) {
	state.contactOpenDevice = "${contactOpenMap}"
	sendSnapshotContactMap()
}

def sendSnapshotContactClosedMap(contactClosedMap) {
	state.contactClosedDevice = "${contactClosedMap}"
	sendSnapshotContactMap()
}

def sendSnapshotContactMap() {
    LOGDEBUG("In Snapshot Tile - Received new Contact data!")
	
	state.contactDevice1 = "<table width='100%'><tr>"
	state.contactDevice1 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice1 += "<div style='font-size: ${fontSize}px'> ${state.contactOpenDevice}</div>"
	state.contactDevice1 += "</td></tr></table>"

	state.contactDevice2 = "<table width='100%'><tr>"
	state.contactDevice2 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice2 += "<div style='font-size: ${fontSize}px'> ${state.contactClosedDevice}</div>"
	state.contactDevice2 += "</td></tr></table>"
	
	def rightNowC = new Date()
	fontSizeS = 12
	state.theDateC = "<table width='100%'><tr><td> </td></tr><tr>"
	state.theDateC += "<td style='text-align: left; width: 100%'>"
	state.theDateC += "<div style='font-size: ${fontSizeS}px'> ${rightNowC}</div>"
	state.theDateC += "</td></tr></table>"
	
	state.contactDevice = "${state.contactDevice1}${state.contactDevice2}${state.theDateC}"
	sendEvent(name: "snapshotContact", value: state.contactDevice, displayed: true)
}

def installed(){
    log.info "Snapshot Tile has been Installed"
}

def updated() {
    log.info "Snap Tile has been Updated"
}

def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
	
