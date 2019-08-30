/**
 *  ****************  Snapshot Driver  ****************
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
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
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
 *  V1.1.0 - 08/28/19 - Driver Watchdog compatible
 *  V1.0.9 - 04/30/19 - Adjust driver for Water support
 *  V1.0.8 - 04/16/19 - Code cleanup, added importUrl
 *  V1.0.7 - 04/12/19 - Adjust driver for Presence support
 *  V1.0.6 - 04/02/19 - Adjust driver for Temperature support
 *  V1.0.5 - 04/01/19 - Adjust driver for Lock support
 *  V1.0.4 - 04/01/19 - Adjust driver for priority type of tiles
 *  V1.0.3 - 03/30/19 - Adjust driver for on/off and open/close type of tiles
 *  V1.0.2 - 03/27/19 - Added support for counting how many devices are On/Off/Open/Closed
 *  V1.0.1 - 03/23/19 - Adjusted for new Dashboard requirements
 *  V1.0.0 - 03/06/19 - Initial release
 */

def setVersion(){
    appName = "SnapshotDriver"
	version = "v1.1.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Snapshot Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://github.com/bptworld/Hubitat/blob/master/Apps/Snapshot/S-driver.groovy") {
   		capability "Actuator"

		command "sendSnapshotSwitchMap1", ["string"]
		command "sendSnapshotSwitchMap2", ["string"]
		command "sendSnapshotSwitchMap3", ["string"]
		command "sendSnapshotSwitchMap4", ["string"]
		command "sendSnapshotSwitchMap5", ["string"]
		command "sendSnapshotSwitchMap6", ["string"]
		
		command "sendSnapshotContactMap1", ["string"]
		command "sendSnapshotContactMap2", ["string"]
		command "sendSnapshotContactMap3", ["string"]
		command "sendSnapshotContactMap4", ["string"]
		command "sendSnapshotContactMap5", ["string"]
		command "sendSnapshotContactMap6", ["string"]
		
		command "sendSnapshotWaterMap1", ["string"]
		command "sendSnapshotWaterMap2", ["string"]
		
		command "sendSnapshotPresenceMap1", ["string"]
		command "sendSnapshotPresenceMap2", ["string"]
		
		command "sendSnapshotLockMap1", ["string"]
		command "sendSnapshotLockMap2", ["string"]
		
		command "sendSnapshotTempMap1", ["string"]
		command "sendSnapshotTempMap2", ["string"]
		
		command "sendSnapshotSwitchCountOn", ["string"]
		command "sendSnapshotSwitchCountOff", ["string"]
		command "sendSnapshotContactCountOpen", ["string"]
		command "sendSnapshotContactCountClosed", ["string"]
		command "sendSnapshotWaterCountWet", ["string"]
		command "sendSnapshotWaterCountDry", ["string"]
		command "sendSnapshotPresenceCountNotPresent", ["string"]
		command "sendSnapshotPresenceCountPresent", ["string"]
		command "sendSnapshotLockCountUnlocked", ["string"]
		command "sendSnapshotLockCountLocked", ["string"]
		command "sendSnapshotTempCountHigh", ["string"]
		command "sendSnapshotTempCountLow", ["string"]
		
		command "sendSnapshotPrioritySwitchMap1", ["string"]
		command "sendSnapshotPrioritySwitchMap2", ["string"]
		command "sendSnapshotPriorityContactMap1", ["string"]
		command "sendSnapshotPriorityContactMap2", ["string"]
		command "sendSnapshotPriorityWaterMap1", ["string"]
		command "sendSnapshotPriorityWaterMap2", ["string"]
		command "sendSnapshotPriorityLockMap1", ["string"]
		command "sendSnapshotPriorityLockMap2", ["string"]
		command "sendSnapshotPriorityTempMap1", ["string"]
		command "sendSnapshotPriorityTempMap2", ["string"]
		
    	attribute "snapshotSwitch1", "string"
		attribute "snapshotSwitch2", "string"
		attribute "snapshotSwitch3", "string"
		attribute "snapshotSwitch4", "string"
		attribute "snapshotSwitch5", "string"
		attribute "snapshotSwitch6", "string"
		attribute "snapshotSwitchCountOn", "string"
		attribute "snapshotSwitchCountOff", "string"
		
		attribute "snapshotContact1", "string"
		attribute "snapshotContact2", "string"
		attribute "snapshotContact3", "string"
		attribute "snapshotContact4", "string"
		attribute "snapshotContact5", "string"
		attribute "snapshotContact6", "string"
		attribute "snapshotContactCountOpen", "string"
		attribute "snapshotCountactCountClosed", "string"
		
		attribute "snapshotWater1", "string"
		attribute "snapshotWater2", "string"
		attribute "snapshotWaterCountWet", "string"
		attribute "snapshotWaterCountDry", "string"
		
		attribute "snapshotPresence1", "string"
		attribute "snapshotPresence2", "string"
		attribute "snapshotPresenceCountPresent", "string"
		attribute "snapshotPresenceCountNotPresent", "string"
		
		attribute "snapshotLock1", "string"
		attribute "snapshotLock2", "string"
		attribute "snapshotLockCountUnlocked", "string"
		attribute "snapshotLockCountLocked", "string"
		
		attribute "snapshotTemp1", "string"
		attribute "snapshotTemp2", "string"
		attribute "snapshotTempCountHigh", "string"
		attribute "snapshotTempCountLow", "string"
		
		attribute "snapshotPrioritySwitch1", "string"
		attribute "snapshotPrioritySwitch2", "string"
		attribute "snapshotPriorityContact1", "string"
		attribute "snapshotPriorityContact2", "string"
		attribute "snapshotPriorityWater1", "string"
		attribute "snapshotPriorityWater2", "string"
		attribute "snapshotPriorityLock1", "string"
		attribute "snapshotPriorityLock2", "string"
		attribute "snapshotPriorityTemp1", "string"
		attribute "snapshotPriorityTemp2", "string"

        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Data Font Size", required: true, defaultValue: "15")
			input("fontSizeS", "text", title: "Date Font Size", required: true, defaultValue: "12")
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: true)
        }
    }
}

def sendSnapshotSwitchMap1(switchMap1) {
	state.switchDevice1a = "${switchMap1}"
	state.switchDevice1 = "<table width='100%'><tr>"
	state.switchDevice1 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice1 += "<div style='font-size: ${fontSize}px'> ${state.switchDevice1a}</div>"
	state.switchDevice1 += "</td></tr></table>"
	state.switchDevice1Count = state.switchDevice1.length()
	if(state.switchDevice1Count <= 1000) {
		if(logEnable) log.debug "switchDevice1 - has ${state.switchDevice1Count} Characters<br>${state.switchDevice1}"
	} else {
		state.switchDevice1 = "Too many characters to display on Dashboard (${state.switchDevice1Count})"
	}
	sendEvent(name: "snapshotSwitch1", value: state.switchDevice1, displayed: true)
}

def sendSnapshotSwitchMap2(switchMap2) {
	state.switchDevice2a = "${switchMap2}"
	state.switchDevice2 = "<table width='100%'><tr>"
	state.switchDevice2 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice2 += "<div style='font-size: ${fontSize}px'> ${state.switchDevice2a}</div>"
	state.switchDevice2 += "</td></tr></table>"
	state.switchDevice2Count = state.switchDevice2.length()
	if(state.switchtDevice2Count <= 1000) {
		if(logEnable) log.debug "switchDevice2 - has ${state.switchDevice2Count} Characters<br>${state.switchDevice2}"
	} else {
		state.switchDevice2 = "Too many characters to display on Dashboard (${state.switchDevice2Count})"
	}
	sendEvent(name: "snapshotSwitch2", value: state.switchDevice2, displayed: true)
}

def sendSnapshotSwitchMap3(switchMap3) {
	state.switchDevice3a = "${switchMap3}"
	state.switchDevice3 = "<table width='100%'><tr>"
	state.switchDevice3 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice3 += "<div style='font-size: ${fontSize}px'> ${state.switchDevice3a}</div>"
	state.switchDevice3 += "</td></tr></table>"
	state.switchDevice3Count = state.switchDevice3.length()
	if(state.switchDevice3Count <= 1000) {
		if(logEnable) log.debug "switchDevice3 - has ${state.switchDevice3Count} Characters<br>${state.switchDevice3}"
	} else {
		state.switchDevice3 = "Too many characters to display on Dashboard (${state.switchDevice3Count})"
	}
	sendEvent(name: "snapshotSwitch3", value: state.switchDevice3, displayed: true)
}

def sendSnapshotSwitchMap4(switchMap4) {
	state.switchDevice4a = "${switchMap4}"
	state.switchDevice4 = "<table width='100%'><tr>"
	state.switchDevice4 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice4 += "<div style='font-size: ${fontSize}px'> ${state.switchDevice4a}</div>"
	state.switchDevice4 += "</td></tr></table>"
	state.switchDevice4Count = state.switchDevice4.length()
	if(state.switchDevice4Count <= 1000) {
		if(logEnable) log.debug "switchDevice4 - has ${state.switchDevice4Count} Characters<br>${state.switchDevice4}"
	} else {
		state.switchDevice4 = "Too many characters to display on Dashboard (${state.switchDevice4Count})"
	}
	sendEvent(name: "snapshotSwitch4", value: state.switchDevice4, displayed: true)
}

def sendSnapshotSwitchMap5(switchMap5) {
	state.switchDevice5a = "${switchMap5}"
	state.switchDevice5 = "<table width='100%'><tr>"
	state.switchDevice5 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice5 += "<div style='font-size: ${fontSize}px'> ${state.switchDevice5a}</div>"
	state.switchDevice5 += "</td></tr></table>"
	state.switchDevice5Count = state.switchDevice5.length()
	if(state.switchDevice5Count <= 1000) {
		if(logEnable) log.debug "switchDevice5 - has ${state.switchDevice5Count} Characters<br>${state.switchDevice5}"
	} else {
		state.switchDevice5 = "Too many characters to display on Dashboard (${state.switchDevice5Count})"
	}
	sendEvent(name: "snapshotSwitch5", value: state.switchDevice5, displayed: true)
}

def sendSnapshotSwitchMap6(switchMap6) {
	state.switchDevice6a = "${switchMap6}"
	state.switchDevice6 = "<table width='100%'><tr>"
	state.switchDevice6 += "<td style='text-align: left; width: 100%'>"
	state.switchDevice6 += "<div style='font-size: ${fontSize}px'> ${state.switchDevice6a}</div>"
	state.switchDevice6 += "</td></tr></table>"
	state.switchDevice6Count = state.switchDevice6.length()
	if(state.switchDevice6Count <= 1000) {
		if(logEnable) log.debug "switchDevice6 - has ${state.switchDevice6Count} Characters<br>${state.switchDevice6}"
	} else {
		state.switchDevice6 = "Too many characters to display on Dashboard (${state.switchDevice6Count})"
	}
	sendEvent(name: "snapshotSwitch6", value: state.switchDevice6, displayed: true)
}

def sendSnapshotSwitchCountOn(switchCountOn) {
	sendEvent(name: "snapshotSwitchCountOn", value: switchCountOn, displayed: true)
}

def sendSnapshotSwitchCountOff(switchCountOff) {
	sendEvent(name: "snapshotSwitchCountOff", value: switchCountOff, displayed: true)
}

def sendSnapshotContactMap1(contactMap1) {
	state.contactDevice1a = "${contactMap1}"
	state.contactDevice1 = "<table width='100%'><tr>"
	state.contactDevice1 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice1 += "<div style='font-size: ${fontSize}px'> ${state.contactDevice1a}</div>"
	state.contactDevice1 += "</td></tr></table>"
	state.contactDevice1Count = state.contactDevice1.length()
	if(state.contactDevice1Count <= 1000) {
		if(logEnable) log.debug "contactDevice1 - has ${state.contactDevice1Count} Characters<br>${state.contactDevice1}"
	} else {
		state.contactDevice1 = "Too many characters to display on Dashboard (${state.contactDevice1Count})"
	}
	sendEvent(name: "snapshotContact1", value: state.contactDevice1, displayed: true)
}

def sendSnapshotContactMap2(contactMap2) {
	state.contactDevice2a = "${contactMap2}"
	state.contactDevice2 = "<table width='100%'><tr>"
	state.contactDevice2 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice2 += "<div style='font-size: ${fontSize}px'> ${state.contactDevice2a}</div>"
	state.contactDevice2 += "</td></tr></table>"
	state.contactDevice2Count = state.contactDevice2.length()
	if(state.contactDevice2Count <= 1000) {
		if(logEnable) log.debug "contactDevice2 - has ${state.contactDevice2Count} Characters<br>${state.contactDevice2}"
	} else {
		state.contactDevice2 = "Too many characters to display on Dashboard (${state.contactDevice2Count})"
	}
	sendEvent(name: "snapshotContact2", value: state.contactDevice2, displayed: true)
}

def sendSnapshotContactMap3(contactMap3) {
	state.contactDevice3a = "${contactMap3}"
	state.contactDevice3 = "<table width='100%'><tr>"
	state.contactDevice3 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice3 += "<div style='font-size: ${fontSize}px'> ${state.contactDevice3a}</div>"
	state.contactDevice3 += "</td></tr></table>"
	state.contactDevice3Count = state.contactDevice3.length()
	if(state.contactDevice3Count <= 1000) {
		if(logEnable) log.debug "contactDevice3 - has ${state.contactDevice3Count} Characters<br>${state.contactDevice3}"
	} else {
		state.contactDevice3 = "Too many characters to display on Dashboard (${state.contactDevice3Count})"
	}
	sendEvent(name: "snapshotContact3", value: state.contactDevice3, displayed: true)
}

def sendSnapshotContactMap4(contactMap4) {
	state.contactDevice4a = "${contactMap4}"
	state.contactDevice4 = "<table width='100%'><tr>"
	state.contactDevice4 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice4 += "<div style='font-size: ${fontSize}px'> ${state.contactDevice4a}</div>"
	state.contactDevice4 += "</td></tr></table>"
	state.contactDevice4Count = state.contactDevice4.length()
	if(state.contactDevice4Count <= 1000) {
		if(logEnable) log.debug "contactDevice4 - has ${state.contactDevice4Count} Characters<br>${state.contactDevice4}"
	} else {
		state.contactDevice4 = "Too many characters to display on Dashboard (${state.contactDevice4Count})"
	}
	sendEvent(name: "snapshotContact4", value: state.contactDevice4, displayed: true)
}

def sendSnapshotContactMap5(contactMap5) {
	state.contactDevice5a = "${contactMap5}"
	state.contactDevice5 = "<table width='100%'><tr>"
	state.contactDevice5 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice5 += "<div style='font-size: ${fontSize}px'> ${state.contactDevice5a}</div>"
	state.contactDevice5 += "</td></tr></table>"
	state.contactDevice5Count = state.contactDevice5.length()
	if(state.contactDevice5Count <= 1000) {
		if(logEnable) log.debug "contactDevice5 - has ${state.contactDevice5Count} Characters<br>${state.contactDevice5}"
	} else {
		state.contactDevice5 = "Too many characters to display on Dashboard (${state.contactDevice5Count})"
	}
	sendEvent(name: "snapshotContact5", value: state.contactDevice5, displayed: true)
}

def sendSnapshotContactMap6(contactMap6) {
	state.contactDevice6a = "${contactMap6}"
	state.contactDevice6 = "<table width='100%'><tr>"
	state.contactDevice6 += "<td style='text-align: left; width: 100%'>"
	state.contactDevice6 += "<div style='font-size: ${fontSize}px'> ${state.contactDevice6a}</div>"
	state.contactDevice6 += "</td></tr></table>"
	state.contactDevice6Count = state.contactDevice6.length()
	if(state.contactDevice6Count <= 1000) {
		if(logEnable) log.debug "contactDevice6 - has ${state.contactDevice6Count} Characters<br>${state.contactDevice6}"
	} else {
		state.contactDevice6 = "Too many characters to display on Dashboard (${state.contactDevice6Count})"
	}
	sendEvent(name: "snapshotContact6", value: state.contactDevice6, displayed: true)
}

def sendSnapshotContactCountOpen(contactCountOpen) {
	sendEvent(name: "snapshotContactCountOpen", value: contactCountOpen, displayed: true)
}

def sendSnapshotContactCountClosed(contactCountClosed) {
	sendEvent(name: "snapshotContactCountClosed", value: contactCountClosed, displayed: true)
}

def sendSnapshotWaterMap1(waterMap1) {
	state.waterDevice1a = "${waterMap1}"
	state.waterDevice1 = "<table width='100%'><tr>"
	state.waterDevice1 += "<td style='text-align: left; width: 100%'>"
	state.waterDevice1 += "<div style='font-size: ${fontSize}px'> ${state.waterDevice1a}</div>"
	state.waterDevice1 += "</td></tr></table>"
	state.waterDevice1Count = state.waterDevice1.length()
	if(state.waterDevice1Count <= 1000) {
		if(logEnable) log.debug "waterDevice1 - has ${state.waterDevice1Count} Characters<br>${state.waterDevice1}"
	} else {
		state.waterDevice1 = "Too many characters to display on Dashboard (${state.waterDevice1Count})"
	}
	sendEvent(name: "snapshotWater1", value: state.waterDevice1, displayed: true)
}

def sendSnapshotWaterMap2(waterMap2) {
	state.waterDevice2a = "${waterMap2}"
	state.waterDevice2 = "<table width='100%'><tr>"
	state.waterDevice2 += "<td style='text-align: left; width: 100%'>"
	state.waterDevice2 += "<div style='font-size: ${fontSize}px'> ${state.waterDevice2a}</div>"
	state.waterDevice2 += "</td></tr></table>"
	state.waterDevice2Count = state.waterDevice2.length()
	if(state.waterDevice2Count <= 1000) {
		if(logEnable) log.debug "waterDevice2 - has ${state.waterDevice2Count} Characters<br>${state.waterDevice2}"
	} else {
		state.waterDevice2 = "Too many characters to display on Dashboard (${state.waterDevice2Count})"
	}
	sendEvent(name: "snapshotWater2", value: state.waterDevice2, displayed: true)
}

def sendSnapshotWaterCountWet(waterCountWet) {
	sendEvent(name: "snapshotWaterCountWet", value: waterCountWet, displayed: true)
}

def sendSnapshotWaterCountDry(waterCountDry) {
	sendEvent(name: "snapshotWaterCountDry", value: waterCountDry, displayed: true)
}

def sendSnapshotPresenceMap1(presenceMap1) {
	state.presenceDevice1a = "${presenceMap1}"
	state.presenceDevice1 = "<table width='100%'><tr>"
	state.presenceDevice1 += "<td style='text-align: left; width: 100%'>"
	state.presenceDevice1 += "<div style='font-size: ${fontSize}px'> ${state.presenceDevice1a}</div>"
	state.presenceDevice1 += "</td></tr></table>"
	state.presenceDevice1Count = state.presenceDevice1.length()
	if(state.presenceDevice1Count <= 1000) {
		if(logEnable) log.debug "presenceDevice1 - has ${state.presenceDevice1Count} Characters<br>${state.presenceDevice1}"
	} else {
		state.presenceDevice1 = "Too many characters to display on Dashboard (${state.presenceDevice1Count})"
	}
	sendEvent(name: "snapshotPresence1", value: state.presenceDevice1, displayed: true)
}

def sendSnapshotPresenceMap2(presenceMap2) {
	state.presenceDevice2a = "${presenceMap2}"
	state.presenceDevice2 = "<table width='100%'><tr>"
	state.presenceDevice2 += "<td style='text-align: left; width: 100%'>"
	state.presenceDevice2 += "<div style='font-size: ${fontSize}px'> ${state.presenceDevice2a}</div>"
	state.presenceDevice2 += "</td></tr></table>"
	state.presenceDevice2Count = state.presenceDevice2.length()
	if(state.presenceDevice2Count <= 1000) {
		if(logEnable) log.debug "presenceDevice2 - has ${state.presenceDevice2Count} Characters<br>${state.presenceDevice2}"
	} else {
		state.presenceDevice2 = "Too many characters to display on Dashboard (${state.presenceDevice2Count})"
	}
	sendEvent(name: "snapshotPresence2", value: state.presenceDevice2, displayed: true)
}

def sendSnapshotPresenceCountNotPresent(presenceCountNotPresent) {
	sendEvent(name: "snapshotPresenceCountNotPresent", value: presenceCountNotPresent, displayed: true)
}

def sendSnapshotPresenceCountPresent(presenceCountPresent) {
	sendEvent(name: "snapshotPresenceCountPresent", value: presenceCountPresent, displayed: true)
}

def sendSnapshotLockMap1(lockMap1) {
	state.lockDevice1a = "${lockMap1}"
	state.lockDevice1 = "<table width='100%'><tr>"
	state.lockDevice1 += "<td style='text-align: left; width: 100%'>"
	state.lockDevice1 += "<div style='font-size: ${fontSize}px'> ${state.lockDevice1a}</div>"
	state.lockDevice1 += "</td></tr></table>"
	state.lockDevice1Count = state.lockDevice1.length()
	if(state.lockDevice1Count <= 1000) {
		if(logEnable) log.debug "lockDevice1 - has ${state.lockDevice1Count} Characters<br>${state.lockDevice1}"
	} else {
		state.lockDevice1 = "Too many characters to display on Dashboard (${state.lockDevice1Count})"
	}
	sendEvent(name: "snapshotLock1", value: state.lockDevice1, displayed: true)
}

def sendSnapshotLockMap2(lockMap2) {
	state.lockDevice2a = "${lockMap2}"
	state.lockDevice2 = "<table width='100%'><tr>"
	state.lockDevice2 += "<td style='text-align: left; width: 100%'>"
	state.lockDevice2 += "<div style='font-size: ${fontSize}px'> ${state.lockDevice2a}</div>"
	state.lockDevice2 += "</td></tr></table>"
	state.lockDevice2Count = state.lockDevice2.length()
	if(state.lockDevice2Count <= 1000) {
		if(logEnable) log.debug "lockDevice2 - has ${state.lockDevice2Count} Characters<br>${state.lockDevice2}"
	} else {
		state.lockDevice2 = "Too many characters to display on Dashboard (${state.lockDevice2Count})"
	}
	sendEvent(name: "snapshotLock2", value: state.lockDevice2, displayed: true)
}

def sendSnapshotLockCountUnlocked(lockCountUnlocked) {
	sendEvent(name: "snapshotLockCountUnlocked", value: lockCountUnlocked, displayed: true)
}

def sendSnapshotLockCountLocked(lockCountLocked) {
	sendEvent(name: "snapshotLockCountLocked", value: lockCountLocked, displayed: true)
}

def sendSnapshotTempMap1(tempMap1) {
	state.tempDevice1a = "${tempMap1}"
	state.tempDevice1 = "<table width='100%'><tr>"
	state.tempDevice1 += "<td style='text-align: left; width: 100%'>"
	state.tempDevice1 += "<div style='font-size: ${fontSize}px'> ${state.tempDevice1a}</div>"
	state.tempDevice1 += "</td></tr></table>"
	state.tempDevice1Count = state.tempDevice1.length()
	if(state.tempDevice1Count <= 1000) {
		if(logEnable) log.debug "tempDevice1 - has ${state.tempDevice1Count} Characters<br>${state.tempDevice1}"
	} else {
		state.tempDevice1 = "Too many characters to display on Dashboard (${state.tempDevice1Count})"
	}
	sendEvent(name: "snapshotTemp1", value: state.tempDevice1, displayed: true)
}

def sendSnapshotTempMap2(tempMap2) {
	state.tempDevice2a = "${tempMap2}"
	state.tempDevice2 = "<table width='100%'><tr>"
	state.tempDevice2 += "<td style='text-align: left; width: 100%'>"
	state.tempDevice2 += "<div style='font-size: ${fontSize}px'> ${state.tempDevice2a}</div>"
	state.tempDevice2 += "</td></tr></table>"
	state.tempDevice2Count = state.tempDevice2.length()
	if(state.tempDevice2Count <= 1000) {
		if(logEnable) log.debug "tempDevice2 - has ${state.tempDevice2Count} Characters<br>${state.tempDevice2}"
	} else {
		state.tempDevice2 = "Too many characters to display on Dashboard (${state.tempDevice2Count})"
	}
	sendEvent(name: "snapshotTemp2", value: state.tempDevice2, displayed: true)
}

def sendSnapshotTempCountHigh(tempCountHigh) {
	sendEvent(name: "snapshotTempCountHigh", value: tempCountHigh, displayed: true)
}

def sendSnapshotTempCountLow(tempCountLow) {
	sendEvent(name: "snapshotTempCountLow", value: tempCountLow, displayed: true)
}

def sendSnapshotPrioritySwitchMap1(pSwitchMap1S) {
	state.pSwitchDevice1a = "${pSwitchMap1S}"
	state.pSwitchDevice1 = "<table width='100%'><tr>"
	state.pSwitchDevice1 += "<td style='text-align: left; width: 100%'>"
	state.pSwitchDevice1 += "<div style='font-size: ${fontSize}px'> ${state.pSwitchDevice1a}</div>"
	state.pSwitchDevice1 += "</td></tr></table>"
	state.pSwitchDevice1Count = state.pSwitchDevice1.length()
	if(state.pSwitchDevice1Count <= 1000) {
		if(logEnable) log.debug "pSwitchDevice1 - has ${state.pSwitchDevice1Count} Characters<br>${state.pSwitchDevice1}"
	} else {
		state.pSwitchDevice1 = "Too many characters to display on Dashboard (${state.pSwitchDevice1Count})"
	}
	sendEvent(name: "snapshotPrioritySwitch1", value: state.pSwitchDevice1, displayed: true)
}

def sendSnapshotPrioritySwitchMap2(pSwitchMap2S) {
	state.pSwitchDevice2a = "${pSwitchMap2S}"
	state.pSwitchDevice2 = "<table width='100%'><tr>"
	state.pSwitchDevice2 += "<td style='text-align: left; width: 100%'>"
	state.pSwitchDevice2 += "<div style='font-size: ${fontSize}px'> ${state.pSwitchDevice2a}</div>"
	state.pSwitchDevice2 += "</td></tr></table>"
	state.pSwitchDevice2Count = state.pSwitchDevice2.length()
	if(state.pSwitchDevice2Count <= 1000) {
		if(logEnable) log.debug "pSwitchDevice2 - has ${state.pSwitchDevice2Count} Characters<br>${state.pSwitchDevice2}"
	} else {
		state.pSwitchDevice2 = "Too many characters to display on Dashboard (${state.pSwitchDevice2Count})"
	}
	sendEvent(name: "snapshotPrioritySwitch2", value: state.pSwitchDevice2, displayed: true)
}

def sendSnapshotPriorityContactMap1(pContactMap1S) {
	state.pContactDevice1a = "${pContactMap1S}"
	state.pContactDevice1 = "<table width='100%'><tr>"
	state.pContactDevice1 += "<td style='text-align: left; width: 100%'>"
	state.pContactDevice1 += "<div style='font-size: ${fontSize}px'> ${state.pContactDevice1a}</div>"
	state.pContactDevice1 += "</td></tr></table>"
	state.pContactDevice1Count = state.pContactDevice1.length()
	if(state.pContactDevice1Count <= 1000) {
		if(logEnable) log.debug "pContactDevice1 - has ${state.pContactDevice1Count} Characters<br>${state.pContactDevice1}"
	} else {
		state.pContactDevice1 = "Too many characters to display on Dashboard (${state.pContactDevice1Count})"
	}
	sendEvent(name: "snapshotPriorityContact1", value: state.pContactDevice1, displayed: true)
}

def sendSnapshotPriorityContactMap2(pContactMap2S) {
	state.pContactDevice2a = "${pContactMap2S}"
	state.pContactDevice2 = "<table width='100%'><tr>"
	state.pContactDevice2 += "<td style='text-align: left; width: 100%'>"
	state.pContactDevice2 += "<div style='font-size: ${fontSize}px'> ${state.pContactDevice2a}</div>"
	state.pContactDevice2 += "</td></tr></table>"
	state.pContactDevice2Count = state.pContactDevice2.length()
	if(state.pContactDevice2Count <= 1000) {
		if(logEnable) log.debug "pContactDevice2 - has ${state.pContactDevice2ount} Characters<br>${state.pContactDevice2}"
	} else {
		state.pContactDevice2 = "Too many characters to display on Dashboard (${state.pContactDevice2Count})"
	}
	sendEvent(name: "snapshotPriorityContact2", value: state.pContactDevice2, displayed: true)
}

def sendSnapshotPriorityWaterMap1(pWaterMap1S) {
	state.pWaterDevice1a = "${pWaterMap1S}"
	state.pWaterDevice1 = "<table width='100%'><tr>"
	state.pWaterDevice1 += "<td style='text-align: left; width: 100%'>"
	state.pWaterDevice1 += "<div style='font-size: ${fontSize}px'> ${state.pWaterDevice1a}</div>"
	state.pWaterDevice1 += "</td></tr></table>"
	state.pWaterDevice1Count = state.pWaterDevice1.length()
	if(state.pWaterDevice1Count <= 1000) {
		if(logEnable) log.debug "pWaterDevice1 - has ${state.pWaterDevice1Count} Characters<br>${state.pWaterDevice1}"
	} else {
		state.pWaterDevice1 = "Too many characters to display on Dashboard (${state.pWaterDevice1Count})"
	}
	sendEvent(name: "snapshotPriorityWater1", value: state.pWaterDevice1, displayed: true)
}

def sendSnapshotPriorityWaterMap2(pWaterMap2S) {
	state.pWaterDevice2a = "${pWaterMap2S}"
	state.pWaterDevice2 = "<table width='100%'><tr>"
	state.pWaterDevice2 += "<td style='text-align: left; width: 100%'>"
	state.pWaterDevice2 += "<div style='font-size: ${fontSize}px'> ${state.pWaterDevice2a}</div>"
	state.pWaterDevice2 += "</td></tr></table>"
	state.pWaterDevice2Count = state.pWaterDevice2.length()
	if(state.pWaterDevice2Count <= 1000) {
		if(logEnable) log.debug "pWaterDevice2 - has ${state.pWaterDevice2Count} Characters<br>${state.pWaterDevice2}"
	} else {
		state.pWaterDevice2 = "Too many characters to display on Dashboard (${state.pWaterDevice2Count})"
	}
	sendEvent(name: "snapshotPriorityWater2", value: state.pWaterDevice2, displayed: true)
}

def sendSnapshotPriorityLockMap1(pLockMap1S) {
	state.pLockDevice1a = "${pLockMap1S}"
	state.pLockDevice1 = "<table width='100%'><tr>"
	state.pLockDevice1 += "<td style='text-align: left; width: 100%'>"
	state.pLockDevice1 += "<div style='font-size: ${fontSize}px'> ${state.pLockDevice1a}</div>"
	state.pLockDevice1 += "</td></tr></table>"
	state.pLockDevice1Count = state.pLockDevice1.length()
	if(state.pLockDevice1Count <= 1000) {
		if(logEnable) log.debug "pLockDevice1 - has ${state.pLockDevice1Count} Characters<br>${state.pLockDevice1}"
	} else {
		state.pLockDevice1 = "Too many characters to display on Dashboard (${state.pLockDevice1Count})"
	}
	sendEvent(name: "snapshotPriorityLock1", value: state.pLockDevice1, displayed: true)
}

def sendSnapshotPriorityLockMap2(pLockMap2S) {
	state.pLockDevice2a = "${pLockMap2S}"
	state.pLockDevice2 = "<table width='100%'><tr>"
	state.pLockDevice2 += "<td style='text-align: left; width: 100%'>"
	state.pLockDevice2 += "<div style='font-size: ${fontSize}px'> ${state.pLockDevice2a}</div>"
	state.pLockDevice2 += "</td></tr></table>"
	state.pLockDevice2Count = state.pLockDevice2.length()
	if(state.pLockDevice2Count <= 1000) {
		if(logEnable) log.debug "pLockDevice2 - has ${state.pLockDevice2Count} Characters<br>${state.pLockDevice2}"
	} else {
		state.pLockDevice2 = "Too many characters to display on Dashboard (${state.pLockDevice2Count})"
	}
	sendEvent(name: "snapshotPriorityLock2", value: state.pLockDevice2, displayed: true)
}

def sendSnapshotPriorityTempMap1(pTempMap1S) {
	state.pTempDevice1a = "${pTempMap1S}"
	state.pTempDevice1 = "<table width='100%'><tr>"
	state.pTempDevice1 += "<td style='text-align: left; width: 100%'>"
	state.pTempDevice1 += "<div style='font-size: ${fontSize}px'> ${state.pTempDevice1a}</div>"
	state.pTempDevice1 += "</td></tr></table>"
	state.pTempDevice1Count = state.pTempDevice1.length()
	if(state.pTempDevice1Count <= 1000) {
		if(logEnable) log.debug "pTempDevice1 - has ${state.pTempDevice1Count} Characters<br>${state.pTempDevice1}"
	} else {
		state.pTempDevice1 = "Too many characters to display on Dashboard (${state.pTempDevice1Count})"
	}
	sendEvent(name: "snapshotPriorityTemp1", value: state.pTempDevice1, displayed: true)
}

def sendSnapshotPriorityTempMap2(pTempMap2S) {
	state.pTempDevice2a = "${pTempMap2S}"
	state.pTempDevice2 = "<table width='100%'><tr>"
	state.pTempDevice2 += "<td style='text-align: left; width: 100%'>"
	state.pTempDevice2 += "<div style='font-size: ${fontSize}px'> ${state.pTempDevice2a}</div>"
	state.pTempDevice2 += "</td></tr></table>"
	state.pTempDevice2Count = state.pTempDevice2.length()
	if(state.pTempDevice2Count <= 1000) {
		if(logEnable) log.debug "pTempDevice2 - has ${state.pTempDevice2Count} Characters<br>${state.pTempDevice2}"
	} else {
		state.pTempDevice2 = "Too many characters to display on Dashboard (${state.pTempDevice2Count})"
	}
	sendEvent(name: "snapshotPriorityTemp2", value: state.pTempDevice2, displayed: true)
}

def installed(){
    log.info "Snapshot Driver has been Installed"
    setVersion()
}

def updated() {
    log.info "Snap Driver has been Updated"
    setVersion()
}
