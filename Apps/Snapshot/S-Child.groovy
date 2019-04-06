/**
 *  ****************  Snapshot Child  ****************
 *
 *  Design Usage:
 *  Monitor lights, devices and sensors. Easily see their status right on your dashboard.
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
 *	V1.0.6 - 04/06/19 - Code cleanup
 *	V1.0.5 - 04/02/19 - Added Temp tile options
 *	V1.0.4 - 04/01/19 - Added Lock options
 *	V1.0.3 - 04/01/19 - Added Priority Device Options
 *	V1.0.2 - 03/30/19 - Added ability to select what type of data to report: Full, Only On/Off, Only Open/Closed. Also added count attributes.
 *	V1.0.1 - 03/22/19 - Major update to comply with Hubitat's new dashboard requirements.
 *  V1.0.0 - 03/16/19 - Initial Release
 *
 */

def setVersion() {
	state.version = "v1.0.6"
}

definition(
	name: "Snapshot Child",
	namespace: "BPTWorld",
	author: "Bryan Turcotte",
	description: "Monitor lights, devices and sensors. Easily see their status right on your dashboard.",
	category: "Convenience",
	parent: "BPTWorld:Snapshot",
	iconUrl: "",
	iconX2Url: "",
	iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Snapshot/S-Child.groovy",
)

preferences {
	page(name: "pageConfig")
}

def pageConfig() {
	dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Snapshot</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "REAL TIME - Be careful with this option. Too many devices updating in real time <u>WILL</u> slow down and/or crash the hub."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Type of Trigger")) {
			input "reportMode", "enum", required: true, title: "Select Report Type", submitOnChange: true,  options: ["Regular", "Priority"]
			if(reportMode == "Regular") paragraph "Regular - Will show device state based on options below"
			if(reportMode == "Priority") paragraph "Priority - Show device state only when it's not what it should be based on options below"
			input "triggerMode", "enum", required: true, title: "Select Trigger Frequency", submitOnChange: true,  options: ["Real Time", "Every X minutes", "On Demand"]
			if(triggerMode == "Real Time") {
				paragraph "<b>Be careful with this option. Too many devices updating in real time <u>WILL</u> slow down and/or crash the hub.</b>"
				paragraph "Remember to flip this switch off and back on again if any changes to this app have been made."
				input "realTimeSwitch", "capability.switch", title: "App Control Switch", required: true
			}
			if(triggerMode == "Every X minutes") {
				paragraph "<b>Choose how often to take a Snapshot of your selected devices.</b>"
				paragraph "Remember to flip this switch off and back on again if any changes to this app have been made."
				input "repeatSwitch", "capability.switch", title: "App Control Switch", required: true
				input "timeDelay", "number", title: "Every X Minutes (1 to 60)", required: true, range: '1..60'
			}
			if(triggerMode == "On Demand") {
				paragraph "<b>Only take a snapshot when this switch is turned on OR the Maintenance Reset button is pressed.</b>"
				paragraph "Recommended to create a virtual device with 'Enable auto off' set to '1s'"
				input "onDemandSwitch", "capability.switch", title: "App Control Switch", required: true
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Devices to Monitor")) {
			if(reportMode == "Priority") {
				input "switchesOn", "capability.switch", title: "Switches that should be ON", multiple: true, required: false, submitOnChange: true
				input "switchesOff", "capability.switch", title: "Switches that should be OFF", multiple: true, required: false, submitOnChange: true
				input "contactsOpen", "capability.contactSensor", title: "Contact Sensors that should be OPEN", multiple: true, required: false, submitOnChange: true
				input "contactsClosed", "capability.contactSensor", title: "Contact Sensors that should be CLOSED", multiple: true, required: false, submitOnChange: true
				input "locksLocked", "capability.lock", title: "Door Locks that should be LOCKED", multiple: true, required: false, submitOnChange: true
				input "locksUnlocked", "capability.lock", title: "Door Locks that should be UNLOCKED", multiple: true, required: false, submitOnChange: true
				input "temps", "capability.temperatureMeasurement", title: "Temperature Devices", multiple: true, required: false, submitOnChange: true
				if(temps) input "tempHigh", "number", title: "Temp to consider High if over X", required: true, submitOnChange: true
				if(temps) input "tempLow", "number", title: "Temp to consider Low if under X", required: true, submitOnChange: true
			}
			if(reportMode == "Regular") {
				paragraph "Note: Choose a max of 30 devices in each category."
            	input "switches", "capability.switch", title: "Switches", multiple: true, required: false, submitOnChange: true
            	input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false, submitOnChange: true
				input "locks", "capability.lock", title: "Door Locks", multiple: true, required: false, submitOnChange: true
				input "temps", "capability.temperatureMeasurement", title: "Temperature Devices", multiple: true, required: false, submitOnChange: true
				if(temps) input "tempHigh", "number", title: "Temp to consider High if over X", required: true, submitOnChange: true
				if(temps) input "tempLow", "number", title: "Temp to consider Low if under X", required: true, submitOnChange: true
			}
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			if(reportMode == "Priority") {
				if(temps) {
					input "tempMode", "enum", required: true, title: "Select Temperature Display Output Type", submitOnChange: true,  options: ["Full", "Only High", "Only Low"]
				}
				input "isDataDevice", "capability.switch", title: "Turn this device on if there are devices to report", submitOnChange: true, required: false, multiple: false
			}
			if(reportMode == "Regular") {
				paragraph "Choose the amount/type of data to record"
				if(switches) {
					input "switchMode", "enum", required: true, title: "Select Switches Display Output Type", submitOnChange: true,  options: ["Full", "Only On", "Only Off"]
				}
				if(contacts) {
					input "contactMode", "enum", required: true, title: "Select Contact Display Output Type", submitOnChange: true,  options: ["Full", "Only Open", "Only Closed"]
				}
				if(locks) {
					input "lockMode", "enum", required: true, title: "Select Lock Display Output Type", submitOnChange: true,  options: ["Full", "Only Unlocked", "Only Locked"]
				}
				if(temps) {
					input "tempMode", "enum", required: true, title: "Select Temperature Display Output Type", submitOnChange: true,  options: ["Full", "Only High", "Only Low"]
				}
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Snapshot Tile'<br> - Use our 'Snapshot Tile' Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Snapshot Tile<br>- Pick a template = attribute<br>- 3rd box = snapshotSwitch1-6, snapshotContact1-6, snapshotLock1-2, snapshotPrioritySwitch1-2, snapshotPriorityContact1-2 or snapshotPriorityLock1-2"
		}
		section() {
			input(name: "snapshotTileDevice", type: "capability.actuator", title: "Vitual Device created to send the data to:", submitOnChange: true, required: false, multiple: false)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Maintenance")) {
			paragraph "When removing devices from app, it will be necessary to reset the maps. After turning on switch, Click the button that will appear. All tables will be cleared and repopulated with the current devices."
            input(name: "maintSwitch", type: "bool", defaultValue: "false", title: "Clear all tables", description: "Clear all tables", submitOnChange: "true")
			if(maintSwitch) input "resetBtn", "button", title: "Click here to reset maps"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false, submitOnChange: true}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
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
	unsubscribe()
	initialize()
}

def initialize() {
	if(logEnable) log.debug "In initialize..."
	if(enablerSwitch1) subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	if(triggerMode == "On Demand") subscribe(onDemandSwitch, "switch.on", onDemandSwitchHandler)
	if(triggerMode == "Every X minutes") subscribe(repeatSwitch, "switch", repeatSwitchHandler)
	if(triggerMode == "Real Time") subscribe(realTimeSwitch, "switch", realTimeSwitchHandler)
}

def realTimeSwitchHandler(evt) {
	if(logEnable) log.debug "In realTimeSwitchHandler..."
	if(state.enablerSwitch2 == "off") {
		if(pauseApp == true){log.warn "${app.label} - App paused"}
    	if(pauseApp == false){
			state.realTimeSwitchStatus = evt.value
			if(reportMode == "Regular") {
				if(state.realTimeSwitchStatus == "on") {
					if(logEnable) log.debug "In realTimeSwitchHandler - subscribe"
					if(switches) subscribe(switches, "switch", switchHandler)
					if(contacts) subscribe(contacts, "contact", contactHandler)
					if(locks) subscribe(locks, "lock", lockHandler)
					if(temps) subscribe(temps, "temperature", temperatureHandler)
					runIn(1, maintHandler)
				} else {
					if(logEnable) log.debug "In realTimeSwitchHandler - unsubscribe"
					unsubscribe(switches)
					unsubscribe(contacts)
					unsubscribe(locks)
					unsubscribe(temps)
				}
			}
			if(reportMode == "Priority") {
				if(state.realTimeSwitchStatus == "on") {
					if(logEnable) log.debug "In realTimeSwitchHandler Priority - subscribe"
					subscribe(switchesOn, "switch", priorityHandler)
					subscribe(switchesOff, "switch", priorityHandler)
					subscribe(contactsOpen, "contact", priorityHandler)
					subscribe(contactsClosed, "contact", priorityHandler)
					subscribe(locksUnlocked, "lock", priorityHandler)
					subscribe(locksLocked, "lock", priorityHandler)
					subscribe(temps, "temperature", priorityHandler)
					runIn(1, priorityHandler)
				} else {
					if(logEnable) log.debug "In realTimeSwitchHandler Priority - unsubscribe"
					unsubscribe(switchesOn)
					unsubscribe(switchesOff)
					unsubscribe(contactsOpen)
					unsubscribe(contactsClosed)
					unsubscribe(locksUnlocked)
					unsubscribe(locksLocked)
					unsubscribe(temps)
				}
			}
		}
	} else {
		if(logEnable) log.debug "${app.label} is disabled."
	}
}

def repeatSwitchHandler(evt) {
	if(logEnable) log.debug "In repeatSwitchHandler..."
	if(state.enablerSwitch2 == "off") {
		if(pauseApp == true){log.warn "${app.label} - App paused"}
    	if(pauseApp == false){
			state.repeatSwitchStatus = repeatSwitch.currentValue("switch")
			state.runDelay = timeDelay * 60
			if(reportMode == "Regular") {
				if(state.repeatSwitchStatus == "on") {
					maintHandler()
				}
				runIn(state.runDelay,repeatSwitchHandler)
			}
			if(reportMode == "Priority") {
				if(state.repeatSwitchStatus == "on") {
					priorityHandler()
				}
				runIn(state.runDelay,priorityHandler)
			}
		}
	} else {
		if(logEnable) log.debug "${app.label} is disabled."
	}
}

def onDemandSwitchHandler(evt) {
	if(logEnable) log.debug "In onDemandSwitchHandler..."
	if(state.enablerSwitch2 == "off") {
		if(pauseApp == true){log.warn "${app.label} - App paused"}
    	if(pauseApp == false){
			state.onDemandSwitchStatus = evt.value
			if(reportMode == "Regular") {
				if(state.onDemandSwitchStatus == "on") maintHandler()
			}
			if(reportMode == "Priority") {
				if(state.onDemandSwitchStatus == "on") priorityHandler()
			}
		}
	} else {
		if(logEnable) log.debug "${app.label} is disabled."
	}
}

def switchMapHandler() {
	if(logEnable) log.debug "In switchMapHandler..."
	checkMaps()
	state.onSwitchMapS = state.onSwitchMap.sort { a, b -> a.key <=> b.key }
	state.offSwitchMapS = state.offSwitchMap.sort { a, b -> a.key <=> b.key }
	
	state.fSwitchMap1S = "<table width='100%'>"
	state.fSwitchMap2S = "<table width='100%'>"
	state.fSwitchMap3S = "<table width='100%'>"
	state.fSwitchMap4S = "<table width='100%'>"
	state.fSwitchMap5S = "<table width='100%'>"
	state.fSwitchMap6S = "<table width='100%'>"
	state.count = 0
	state.countOn = 0
	state.countOff = 0
	
	if(switchMode == "Full" || switchMode == "Only On") {
		state.onSwitchMapS.each { stuffOn -> 
			state.count = state.count + 1
			state.countOn = state.countOn + 1
			if(logEnable) log.debug "In switchMapHandler - Building Table ON with ${stuffOn.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fSwitchMap1S += "<tr><td>${stuffOn.key}</td><td><div style='color: red;'>on</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fSwitchMap2S += "<tr><td>${stuffOn.key}</td><td><div style='color: red;'>on</div></td></tr>"
			if((state.count >= 11) && (state.count <= 15)) state.fSwitchMap3S += "<tr><td>${stuffOn.key}</td><td><div style='color: red;'>on</div></td></tr>"
			if((state.count >= 16) && (state.count <= 20)) state.fSwitchMap4S += "<tr><td>${stuffOn.key}</td><td><div style='color: red;'>on</div></td></tr>"
			if((state.count >= 21) && (state.count <= 25)) state.fSwitchMap5S += "<tr><td>${stuffOn.key}</td><td><div style='color: red;'>on</div></td></tr>"
			if((state.count >= 26) && (state.count <= 30)) state.fSwitchMap6S += "<tr><td>${stuffOn.key}</td><td><div style='color: red;'>on</div></td></tr>"
		}
	}
	
	if(switchMode == "Full") {
		if((state.count >= 1) && (state.count <= 5)) { state.fSwitchMap1S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 6) && (state.count <= 10)) { state.fSwitchMap2S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 11) && (state.count <= 15)) { state.fSwitchMap3S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 16) && (state.count <= 20)) { state.fSwitchMap4S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 21) && (state.count <= 25)) { state.fSwitchMap5S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 26) && (state.count <= 30)) { state.fSwitchMap6S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
	}
	
	if(switchMode == "Full" || switchMode == "Only Off") {
		state.offSwitchMapS.each { stuffOff -> 
			state.count = state.count + 1
			state.countOff = state.countOff + 1
			if(logEnable) log.debug "In switchMapHandler - Building Table OFF with ${stuffOff.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fSwitchMap1S += "<tr><td>${stuffOff.key}</td><td><div style='color: green;'>off</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fSwitchMap2S += "<tr><td>${stuffOff.key}</td><td><div style='color: green;'>off</div></td></tr>"
			if((state.count >= 11) && (state.count <= 15)) state.fSwitchMap3S += "<tr><td>${stuffOff.key}</td><td><div style='color: green;'>off</div></td></tr>"
			if((state.count >= 16) && (state.count <= 20)) state.fSwitchMap4S += "<tr><td>${stuffOff.key}</td><td><div style='color: green;'>off</div></td></tr>"	
			if((state.count >= 21) && (state.count <= 25)) state.fSwitchMap5S += "<tr><td>${stuffOff.key}</td><td><div style='color: green;'>off</div></td></tr>"	
			if((state.count >= 26) && (state.count <= 30)) state.fSwitchMap6S += "<tr><td>${stuffOff.key}</td><td><div style='color: green;'>off</div></td></tr>"	
		}
	}
	
	state.fSwitchMap1S += "</table>"
	state.fSwitchMap2S += "</table>"
	state.fSwitchMap3S += "</table>"
	state.fSwitchMap4S += "</table>"
	state.fSwitchMap5S += "</table>"
	state.fSwitchMap6S += "</table>"
	
	if(state.count == 0) {
		state.fSwitchMap1S = "<table width='100%'><tr><td><div style='color: green;'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap2S = "<table width='100%'><tr><td><div style='color: green;'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap3S = "<table width='100%'><tr><td><div style='color: green;'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap4S = "<table width='100%'><tr><td><div style='color: green;'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap5S = "<table width='100%'><tr><td><div style='color: green;'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap6S = "<table width='100%'><tr><td><div style='color: green;'>No switch devices to report</div></td></tr></table>"
	}
	
	if(logEnable) log.debug "In switchMapHandler - <br>fSwitchMap1S<br>${state.fSwitchMap1S}"
    snapshotTileDevice.sendSnapshotSwitchMap1(state.fSwitchMap1S)
	snapshotTileDevice.sendSnapshotSwitchMap2(state.fSwitchMap2S)
	snapshotTileDevice.sendSnapshotSwitchMap3(state.fSwitchMap3S)
	snapshotTileDevice.sendSnapshotSwitchMap4(state.fSwitchMap4S)
	snapshotTileDevice.sendSnapshotSwitchMap5(state.fSwitchMap5S)
	snapshotTileDevice.sendSnapshotSwitchMap6(state.fSwitchMap6S)
	snapshotTileDevice.sendSnapshotSwitchCountOn(state.countOn)
	snapshotTileDevice.sendSnapshotSwitchCountOff(state.countOff)
}

def contactMapHandler() {
	if(logEnable) log.debug "In contactMapHandler..."
	checkMaps()
	if(logEnable) log.debug "In contactMapHandler - Sorting Maps"
	state.openContactMapS = state.openContactMap.sort { a, b -> a.key <=> b.key }
	state.closedContactMapS = state.closedContactMap.sort { a, b -> a.key <=> b.key }
	
	state.fContactMap1S = "<table width='100%'>"
	state.fContactMap2S = "<table width='100%'>"
	state.fContactMap3S = "<table width='100%'>"
	state.fContactMap4S = "<table width='100%'>"
	state.fContactMap5S = "<table width='100%'>"
	state.fContactMap6S = "<table width='100%'>"
	state.count = 0
	state.countOpen = 0
	state.countClosed = 0
	
	if(contactMode == "Full" || contactMode == "Only Open") {
		state.openContactMapS.each { stuffOpen -> 
			state.count = state.count + 1
			state.countOpen = state.countOpen + 1
			if(logEnable) log.debug "In contactMapHandler - Building Table OPEN with ${stuffOpen.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fContactMap1S += "<tr><td>${stuffOpen.key}</td><td><div style='color: red;'>open</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fContactMap2S += "<tr><td>${stuffOpen.key}</td><td><div style='color: red;'>open</div></td></tr>"
			if((state.count >= 11) && (state.count <= 15)) state.fContactMap3S += "<tr><td>${stuffOpen.key}</td><td><div style='color: red;'>open</div></td></tr>"
			if((state.count >= 16) && (state.count <= 20)) state.fContactMap4S += "<tr><td>${stuffOpen.key}</td><td><div style='color: red;'>open</div></td></tr>"
			if((state.count >= 21) && (state.count <= 25)) state.fContactMap5S += "<tr><td>${stuffOpen.key}</td><td><div style='color: red;'>open</div></td></tr>"
			if((state.count >= 26) && (state.count <= 30)) state.fContactMap6S += "<tr><td>${stuffOpen.key}</td><td><div style='color: red;'>open</div></td></tr>"
		}
	}
	
	if(contactMode == "Full") {
		if((state.count >= 1) && (state.count <= 5)) { state.fContactMap1S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 6) && (state.count <= 10)) { state.fContactMap2S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 11) && (state.count <= 15)) { state.fContactMap3S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 16) && (state.count <= 20)) { state.fContactMap4S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 21) && (state.count <= 25)) { state.fContactMap5S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 26) && (state.count <= 30)) { state.fContactMap6S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
	}
	
	if(contactMode == "Full" || contactMode == "Only Closed") {
		state.closedContactMapS.each { stuffClosed -> 
			state.count = state.count + 1
			state.countClosed = state.countClosed + 1
			if(logEnable) log.debug "In contactMapHandler - Building Table CLOSED with ${stuffClosed.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fContactMap1S += "<tr><td>${stuffClosed.key}</td><td><div style='color: green;'>closed</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fContactMap2S += "<tr><td>${stuffClosed.key}</td><td><div style='color: green;'>closed</div></td></tr>"
			if((state.count >= 11) && (state.count <= 15)) state.fContactMap3S += "<tr><td>${stuffClosed.key}</td><td><div style='color: green;'>closed</div></td></tr>"
			if((state.count >= 16) && (state.count <= 20)) state.fContactMap4S += "<tr><td>${stuffClosed.key}</td><td><div style='color: green;'>closed</div></td></tr>"
			if((state.count >= 21) && (state.count <= 25)) state.fContactMap5S += "<tr><td>${stuffClosed.key}</td><td><div style='color: green;'>closed</div></td></tr>"
			if((state.count >= 26) && (state.count <= 30)) state.fContactMap6S += "<tr><td>${stuffClosed.key}</td><td><div style='color: green;'>closed</div></td></tr>"
		}
	}
	
	state.fContactMap1S += "</table>"
	state.fContactMap2S += "</table>"
	state.fContactMap3S += "</table>"
	state.fContactMap4S += "</table>"
	state.fContactMap5S += "</table>"
	state.fContactMap6S += "</table>"
	
	if(state.count == 0) {
		state.fContactMap1S = "<table width='100%'><tr><td><div style='color: green;'>No contact devices to report</div></td></tr></table>"
		state.fContactMap2S = "<table width='100%'><tr><td><div style='color: green;'>No contact devices to report</div></td></tr></table>"
		state.fContactMap3S = "<table width='100%'><tr><td><div style='color: green;'>No contact devices to report</div></td></tr></table>"
		state.fContactMap4S = "<table width='100%'><tr><td><div style='color: green;'>No contact devices to report</div></td></tr></table>"
		state.fContactMap5S = "<table width='100%'><tr><td><div style='color: green;'>No contact devices to report</div></td></tr></table>"
		state.fContactMap6S = "<table width='100%'><tr><td><div style='color: green;'>No contact devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In contactMapHandler - <br>fContactMap1S<br>${state.fContactMap1S}"
   	snapshotTileDevice.sendSnapshotContactMap1(state.fContactMap1S)
	snapshotTileDevice.sendSnapshotContactMap2(state.fContactMap2S)
	snapshotTileDevice.sendSnapshotContactMap3(state.fContactMap3S)
	snapshotTileDevice.sendSnapshotContactMap4(state.fContactMap4S)
	snapshotTileDevice.sendSnapshotContactMap5(state.fContactMap5S)
	snapshotTileDevice.sendSnapshotContactMap6(state.fContactMap6S)
	snapshotTileDevice.sendSnapshotContactCountOpen(state.countOpen)
	snapshotTileDevice.sendSnapshotContactCountClosed(state.countClosed)
}

def lockMapHandler() {
	if(logEnable) log.debug "In lockMapHandler..."
	checkMaps()
	state.unlockedLockMapS = state.unlockedLockMap.sort { a, b -> a.key <=> b.key }
	state.lockedLockMapS = state.lockedLockMap.sort { a, b -> a.key <=> b.key }
	if(logEnable) log.debug "In lockMapHandler - Building Lock Maps"
	state.fLockMap1S = "<table width='100%'>"
	state.fLockMap2S = "<table width='100%'>"
	state.count = 0
	state.countUnlocked = 0
	state.countLocked = 0
	
	if(lockMode == "Full" || lockMode == "Only Unlocked") {
		state.unlockedLockMapS.each { stuffUnlocked -> 
			state.count = state.count + 1
			state.countUnlocked = state.countUnlocked + 1
			if(logEnable) log.debug "In lockMapHandler - Building Table UNLOCKED with ${stuffUnlocked.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fLockMap1S += "<tr><td>${stuffUnlocked.key}</td><td><div style='color: red;'>unlocked</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fLockMap2S += "<tr><td>${stuffUnlocked.key}</td><td><div style='color: red;'>unlocked</div></td></tr>"
		}
	}
	
	if(lockMode == "Full") {
		if((state.count >= 1) && (state.count <= 5)) { state.fLockMap1S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 6) && (state.count <= 10)) { state.fLockMap2S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
	}
	
	if(lockMode == "Full" || lockMode == "Only Locked") {
		state.lockedLockMapS.each { stuffLocked -> 
			state.count = state.count + 1
			state.countLocked = state.countLocked + 1
			if(logEnable) log.debug "In lockMapHandler - Building Table LOCKED with ${stuffLocked.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fLockMap1S += "<tr><td>${stuffLocked.key}</td><td><div style='color: green;'>locked</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fLockMap2S += "<tr><td>${stuffLocked.key}</td><td><div style='color: green;'>locked</div></td></tr>"
		}
	}
	
	state.fLockMap1S += "</table>"
	state.fLockMap2S += "</table>"
	
	if(state.count == 0) {
		state.fLockMap1S = "<table width='100%'><tr><td><div style='color: green;'>No lock devices to report</div></td></tr></table>"
		state.fLockMap2S = "<table width='100%'><tr><td><div style='color: green;'>No lock devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In lockMapHandler - <br>fLockMap1S<br>${state.fLockMap1S}"
   	snapshotTileDevice.sendSnapshotLockMap1(state.fLockMap1S)
	snapshotTileDevice.sendSnapshotLockMap2(state.fLockMap2S)
	snapshotTileDevice.sendSnapshotLockCountUnlocked(state.countUnlocked)
	snapshotTileDevice.sendSnapshotLockCountLocked(state.countLocked)
}

def tempMapHandler() {
	if(logEnable) log.debug "In tempMapHandler..."
	checkMaps()
	state.highTempMapS = state.highTempMap.sort { a, b -> a.key <=> b.key }
	state.lowTempMapS = state.lowTempMap.sort { a, b -> a.key <=> b.key }
	state.normalTempMapS = state.normalTempMap.sort { a, b -> a.key <=> b.key }
	if(logEnable) log.debug "In tempMapHandler - Building Temp Maps"
	state.fTempMap1S = "<table width='100%'>"
	state.fTempMap2S = "<table width='100%'>"
	state.count = 0
	state.countHigh = 0
	state.countLow = 0
	
	if(tempMode == "Full" || tempMode == "Only High") {
		state.highTempMapS.each { stuffHigh -> 
			state.count = state.count + 1
			state.countHigh = state.countHigh + 1
			if(logEnable) log.debug "In tempMapHandler - Building Table High with ${stuffHigh.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fTempMap1S += "<tr><td>${stuffHigh.key}</td><td><div style='color: red;'>${stuffHigh.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fTempMap2S += "<tr><td>${stuffHigh.key}</td><td><div style='color: red;'>${stuffHigh.value}</div></td></tr>"
		}
	}
	
	if(tempMode == "Full") {
		state.normalTempMapS.each { stuffNormal ->
			state.count = state.count + 1
			if((state.count >= 1) && (state.count <= 5)) state.fTempMap1S += "<tr><td>${stuffNormal.key}</td><td>${stuffNormal.value}</td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fTempMap2S += "<tr><td>${stuffNormal.key}</td><td>${stuffNormal.value}</td></tr>"
		}
	}
	
	if(tempMode == "Full" || tempMode == "Only Low") {
		state.lowTempMapS.each { stuffLow -> 
			state.count = state.count + 1
			state.countLow = state.countLow + 1
			if(logEnable) log.debug "In tempMapHandler - Building Table Low with ${stuffLow.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fTempMap1S += "<tr><td>${stuffLow.key}</td><td><div style='color: blue;'>${stuffLow.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fTempMap2S += "<tr><td>${stuffLow.key}</td><td><div style='color: blue;'>${stuffLow.value}</div></td></tr>"
		}
	}
	
	state.fTempMap1S += "</table>"
	state.fTempMap2S += "</table>"
	
	if(state.count == 0) {
		state.fTempMap1S = "<table width='100%'><tr><td><div style='color: green;'>No temp devices to report</div></td></tr></table>"
		state.fTempMap2S = "<table width='100%'><tr><td><div style='color: green;'>No temp devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In tempMapHandler - <br>fTempMap1S<br>${state.fTempMap1S}"
   	snapshotTileDevice.sendSnapshotTempMap1(state.fTempMap1S)
	snapshotTileDevice.sendSnapshotTempMap2(state.fTempMap2S)
	snapshotTileDevice.sendSnapshotTempCountHigh(state.countHigh)
	snapshotTileDevice.sendSnapshotTempCountLow(state.countLow)
}

def switchHandler(evt){
	def switchName = evt.displayName
	def switchStatus = evt.value
	if(logEnable) log.debug "In switchHandler...${switchName} - ${switchStatus}"
	if(switchStatus == "on") {
		state.offSwitchMap.remove(switchName)
		state.onSwitchMap.put(switchName, switchStatus)
		if(logEnable) log.debug "In switchHandler - ON<br>${state.onSwitchMap}"
	}
	if(switchStatus == "off") {
		state.onSwitchMap.remove(switchName)
		state.offSwitchMap.put(switchName, switchStatus)
		if(logEnable) log.debug "In switchHandler - OFF<br>${state.offSwitchMap}"
	}
	switchMapHandler()
}

def contactHandler(evt){
	def contactName = evt.displayName
	def contactStatus = evt.value
	if(logEnable) log.debug "In contactHandler...${contactName}: ${contactStatus}"
	if(contactStatus == "open") {
		state.closedContactMap.remove(contactName)
		state.openContactMap.put(contactName, contactStatus)
		if(logEnable) log.debug "In contactHandler - OPEN<br>${state.openContactMap}"
	}
	if(contactStatus == "closed") {
		state.openContactMap.remove(contactName)
		state.closedContactMap.put(contactName, contactStatus)
		if(logEnable) log.debug "In contactHandler - CLOSED<br>${state.closedContactMap}"
	}
	contactMapHandler()
}

def lockHandler(evt){
	def lockName = evt.displayName
	def lockStatus = evt.value
	if(logEnable) log.debug "In lockHandler...${lockName}: ${lockStatus}"
	if(lockStatus == "unlocked") {
		state.lockedLockMap.remove(lockName)
		state.unlockedLockMap.put(lockName, lockStatus)
		if(logEnable) log.debug "In lockHandler - UNLOCKED<br>${state.unlockedLockMap}"
	}
	if(lockStatus == "locked") {
		state.unlockedLockMap.remove(lockName)
		state.lockedLockMap.put(lockName, lockStatus)
		if(logEnable) log.debug "In lockHandler - LOCKED<br>${state.lockedLockMap}"
	}
	lockMapHandler()
}

def temperatureHandler(evt){
	def tempName = evt.displayName
	def tempStatus = evt.value
	if(logEnable) log.debug "In temperatureHandler...${tempName}: ${tempStatus}"
	if(tempStatus >= tempHigh) {
		state.highTempMap.remove(tempName)
		state.highTempMap.put(tempName, tempStatus)
		if(logEnable) log.debug "In temperatureHandler - HIGH<br>${state.highTempMap}"
	}
	if(tempStatus <= tempLow) {
		state.lowTempMap.remove(tempName)
		state.lowTempMap.put(tempName, tempStatus)
		if(logEnable) log.debug "In temperatureHandler - LOW<br>${state.lowTempMap}"
	}
	tempMapHandler()
}

def priorityHandler(evt){
	checkMaps()
// Start Priority Switch
	if(switchesOn || switchesOff) {
		state.wrongSwitchMap = [:]
		if(switchesOn) {
			switchesOn.each { sOn -> 
				def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('switch')
				if(logEnable) log.debug "In priorityHandler - Switch On - ${switchName} - ${switchStatus}"
				if(switchStatus == "off") state.wrongSwitchMap.put(switchName, switchStatus)
			}
		}
		if(switchesOff) {
			switchesOff.each { sOff -> 
				def switchName = sOff.displayName
				def switchStatus = sOff.currentValue('switch')
				if(logEnable) log.debug "In priorityHandler - Switch Off - ${switchName} - ${switchStatus}"
				if(switchStatus == "on") state.wrongSwitchMap.put(switchName, switchStatus)
			}
		}
		state.wrongSwitchMapS = state.wrongSwitchMap.sort { a, b -> a.key <=> b.key }
		state.pSwitchMap1S = "<table width='100%'>"
		state.pSwitchMap2S = "<table width='100%'>"
		state.count = 0
		state.wrongSwitchMapS.each { wSwitch -> 
			state.count = state.count + 1
			state.isPriorityData = "true"
			if(logEnable) log.debug "In priorityHandler - Building Table Wrong Switch with ${wSwitch.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.pSwitchMap1S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></td><td><div style='color: red;'>${wSwitch.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.pSwitchMap2S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></td><td><div style='color: red;'>${wSwitch.value}</div></td></tr>"
		}
		state.pSwitchMap1S += "</table>"
		state.pSwitchMap2S += "</table>"
	
		if(state.count == 0) {
			state.pSwitchMap1S = "<table width='100%'><tr><td><div style='color: green;'>No devices to report</div></td></tr></table>"
			state.pSwitchMap2S = "<table width='100%'><tr><td><div style='color: green;'>No devices to report</div></td></tr></table>"
			state.isPriorityData = "false"
		}
		snapshotTileDevice.sendSnapshotPrioritySwitchMap1(state.pSwitchMap1S)
		snapshotTileDevice.sendSnapshotPrioritySwitchMap2(state.pSwitchMap2S)
	}
	
// Start Priority Contacts
	if(contactsOpen || contactsClosed) {
		state.wrongContactMap = [:]
		if(contactsOpen) {
			contactsOpen.each { cOpen ->
				def contactName = cOpen.displayName
				def contactStatus = cOpen.currentValue('contact')
				if(logEnable) log.debug "In priorityHandler - Contact Open - ${contactName} - ${contactStatus}"
				if(contactStatus == "closed") state.wrongContactMap.put(contactName, contactStatus)
			}
		}
		if(contactsClosed) {
			contactsClosed.each { cClosed ->
				def contactName = cClosed.displayName
				def contactStatus = cClosed.currentValue('contact')
				if(logEnable) log.debug "In priorityHandler - Contact Closed - ${contactName} - ${contactStatus}"
				if(contactStatus == "open") state.wrongContactMap.put(contactName, contactStatus)
			}
		}
		state.wrongContactMapS = state.wrongContactMap.sort { a, b -> a.key <=> b.key }
		state.pContactMap1S = "<table width='100%'>"
		state.pContactMap2S = "<table width='100%'>"
		state.count = 0
		state.wrongContactMapS.each { wContact -> 
			state.count = state.count + 1
			state.isPriorityData = "true"
			if(logEnable) log.debug "In priorityHandler - Building Table Wrong Contact with ${wContact.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.pContactMap1S += "<tr><td><div style='color: red;'>${wContact.key}</div></td><td><div style='color: red;'>${wContact.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.pContactMap2S += "<tr><td><div style='color: red;'>${wContact.key}</div></td><td><div style='color: red;'>${wContact.value}</div></td></tr>"
		}
		state.pContactMap1S += "</table>"
		state.pContactMap2S += "</table>"
	
		if(state.count == 0) {
			state.pContactMap1S = "<table width='100%'><tr><td><div style='color: green;'>No contacts to report</div></td></tr></table>"
			state.pContactMap2S = "<table width='100%'><tr><td><div style='color: green;'>No contacts to report</div></td></tr></table>"
			state.isPriorityData = "false"
		}
		snapshotTileDevice.sendSnapshotPriorityContactMap1(state.pContactMap1S)
		snapshotTileDevice.sendSnapshotPriorityContactMap2(state.pContactMap2S)
	}
		
// Start Priority Locks
	if(locksUnlocked || locksLocked) {
		state.wrongLockMap = [:]
		if(locksUnlocked) {
			locksUnlocked.each { lUnlocked ->
				def lockName = lUnlocked.displayName
				def lockStatus = lUnlocked.currentValue('lock')
				if(logEnable) log.debug "In priorityHandler - Locks Unlocked - ${lockName} - ${lockStatus}"
				if(lockStatus == "locked") state.wrongLockMap.put(lockName, lockStatus)
			}
		}
		if(locksLocked) {
			locksLocked.each { lLocked ->
				def lockName = lLocked.displayName
				def lockStatus = lLocked.currentValue('lock')
				if(logEnable) log.debug "In priorityHandler - Locks Locked - ${lockName} - ${lockStatus}"
				if(lockStatus == "unlocked") state.wrongLockMap.put(lockName, lockStatus)
			}
		}
		state.wrongLockMapS = state.wrongLockMap.sort { a, b -> a.key <=> b.key }
		state.pLockMap1S = "<table width='100%'>"
		state.pLockMap2S = "<table width='100%'>"
		state.count = 0
		state.wrongLockMapS.each { wLock -> 
			state.count = state.count + 1
			state.isPriorityData = "true"
			if(logEnable) log.debug "In priorityHandler - Building Table Wrong Lock with ${wLock.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.pLockMap1S += "<tr><td><div style='color: red;'>${wLock.key}</div></td><td><div style='color: red;'>${wLock.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.pLockMap2S += "<tr><td><div style='color: red;'>${wLock.key}</div></td><td><div style='color: red;'>${wLock.value}</div></td></tr>"
		}
		state.pLockMap1S += "</table>"
		state.pLockMap2S += "</table>"
	
		if(state.count == 0) {
			state.pLockMap1S = "<table width='100%'><tr><td><div style='color: green;'>No locks to report</div></td></tr></table>"
			state.pLockMap2S = "<table width='100%'><tr><td><div style='color: green;'>No locks to report</div></td></tr></table>"
			state.isPriorityData = "false"
		}
		snapshotTileDevice.sendSnapshotPriorityLockMap1(state.pLockMap1S)
		snapshotTileDevice.sendSnapshotPriorityLockMap2(state.pLockMap2S)
	}
	
// Start Priority Temps
	if(temps) {
		state.pHighTempMap = [:]
		state.pLowTempMap = [:]
		state.pNormalTempMap = [:]
		state.pHighTempMapS = [:]
		state.pLowTempMapS = [:]
		state.pNormalTempMapS = [:]
		temps.each { device ->
			def tempName = device.displayName
			def tempStatus = device.currentValue('temperature')
			if(logEnable) log.debug "In priorityHandler - Temps - Working on ${tempName} - ${tempStatus}"
			if(tempStatus <= tempLow) state.pLowTempMap.put(tempName, tempStatus)
			if(tempStatus >= tempHigh) state.pHighTempMap.put(tempName, tempStatus)
			if((tempStatus > tempLow) && (tempStatus < tempHigh)) state.pNormalTempMap.put(tempName, tempStatus)
		}
	   	state.pHighTempMapS = state.pHighTempMap.sort { a, b -> a.key <=> b.key }
		state.pLowTempMapS = state.pLowTempMap.sort { a, b -> a.key <=> b.key }
		state.pNormalTempMapS = state.pNormalTempMap.sort { a, b -> a.key <=> b.key }
		if(logEnable) log.debug "In priorityHandler - pHighTempMapS<br>${state.pHighTempMapS}"
		if(logEnable) log.debug "In priorityHandler - pNormalTempMapS<br>${state.pNormalTempMapS}"
		if(logEnable) log.debug "In priorityHandler - pLowTempMapS<br>${state.pLowTempMapS}"
		state.pTempMap1S = "<table width='100%'>"
		state.pTempMap2S = "<table width='100%'>"
		state.count = 0
		if(tempMode == "Full" || tempMode == "Only High") {
			state.pHighTempMapS.each { stuffHigh -> 
				state.count = state.count + 1
				state.isPriorityData = "true"
				if(logEnable) log.debug "In priorityHandler - Building Table High with ${stuffHigh.key} count: ${state.count}"
				if((state.count >= 1) && (state.count <= 5)) state.pTempMap1S += "<tr><td>${stuffHigh.key}</td><td><div style='color: red;'>${stuffHigh.value}</div></td></tr>"
				if((state.count >= 6) && (state.count <= 10)) state.pTempMap2S += "<tr><td>${stuffHigh.key}</td><td><div style='color: red;'>${stuffHigh.value}</div></td></tr>"
			}
		}
	
		if(tempMode == "Full") {
			state.pNormalTempMapS.each { stuffNormal ->
				state.count = state.count + 1
				state.isPriorityData = "true"
				if(logEnable) log.debug "In priorityHandler - Building Table Low with ${stuffNormal.key} count: ${state.count}"
				if((state.count >= 1) && (state.count <= 5)) state.pTempMap1S += "<tr><td>${stuffNormal.key}</td><td>${stuffNormal.value}</td></tr>"
				if((state.count >= 6) && (state.count <= 10)) state.pTempMap2S += "<tr><td>${stuffNormal.key}</td><td>${stuffNormal.value}</td></tr>"
			}
		}
	
		if(tempMode == "Full" || tempMode == "Only Low") {
			state.pLowTempMapS.each { stuffLow -> 
				state.count = state.count + 1
				state.isPriorityData = "true"
				if(logEnable) log.debug "In priorityHandler - Building Table Low with ${stuffLow.key} count: ${state.count}"
				if((state.count >= 1) && (state.count <= 5)) state.pTempMap1S += "<tr><td>${stuffLow.key}</td><td><div style='color: blue;'>${stuffLow.value}</div></td></tr>"
				if((state.count >= 6) && (state.count <= 10)) state.pTempMap2S += "<tr><td>${stuffLow.key}</td><td><div style='color: blue;'>${stuffLow.value}</div></td></tr>"
			}
		}
	
		state.pTempMap1S += "</table>"
		state.pTempMap2S += "</table>"
	
		if(state.count == 0) {
			state.pTempMap1S = "<table width='100%'><tr><td><div style='color: green;'>No temp devices to report</div></td></tr></table>"
			state.pTempMap2S = "<table width='100%'><tr><td><div style='color: green;'>No temp devices to report</div></td></tr></table>"
			state.isPriorityData = "false"
		}
		snapshotTileDevice.sendSnapshotPriorityTempMap1(state.pTempMap1S)
		snapshotTileDevice.sendSnapshotPriorityTempMap2(state.pTempMap2S)
	}
	
	if((isDataDevice) && (state.isPriorityData == "true")) isDataDevice.on()
	if((isDataDevice) && (state.isPriorityData == "false")) isDataDevice.off()
}

def checkMaps() {
	if(logEnable) log.debug "In checkMaps..."
	if(state.offSwitchMap == null) {
		state.offSwitchMap = [:]
	}
	if(state.onSwitchMap == null) {
		state.onSwitchMap = [:]
	}
	if(state.closedContactMap == null) {
		state.closedContactMap = [:]
	}
	if(state.openContactMap == null) {
		state.openContactMap = [:]
	}
	if(state.lockedLockMap == null) {
		state.lockedLockMap = [:]
	}
	if(state.unlockedLockMap == null) {
		state.unlockedLockMap = [:]
	}
	if(state.lowTempMap == null) {
		state.lowTempMap = [:]
	}
	if(state.highTempMap == null) {
		state.highTempMap = [:]
	}
	if(state.normalTempMap == null) {
		state.normalTempMap = [:]
	}
	if(state.wrongSwitchMap == null) {
		state.wrongSwitchMap = [:]
	}
	if(state.wrongContactMap == null) {
		state.wrongContactMap = [:]
	}
	if(logEnable) log.debug "In checkMaps - Finished"
}

def maintHandler(evt){
	if(logEnable) log.debug "In maintHandler..."
	state.offSwitchMap = [:]
	state.onSwitchMap = [:]
	state.closedContactMap = [:]
	state.openContactMap = [:] 
	state.lockedLockMap = [:]
	state.unlockedLockMap = [:]
	state.highTempMap = [:]
	state.lowTempMap = [:]
	state.normalTempMap = [:]
	if(logEnable) log.debug "In maintHandler...Tables have been cleared!"
	if(logEnable) log.debug "In maintHandler...Repopulating tables"
	if(switches) {
		switches.each { device ->
			def switchName = device.displayName
			def switchStatus = device.currentValue('switch')
			if(logEnable) log.debug "In maintHandler - Working on ${switchName} - ${switchStatus}"
			if(switchStatus == "on") state.onSwitchMap.put(switchName, switchStatus)
			if(switchStatus == "off") state.offSwitchMap.put(switchName, switchStatus)
		}
		switchMapHandler()
	}
	if(contacts) {
		contacts.each { device ->
			def contactName = device.displayName
			def contactStatus = device.currentValue('contact')
			if(logEnable) log.debug "In maintHandler - Working on ${contactName} - ${contactStatus}"
			if(contactStatus == "open") state.openContactMap.put(contactName, contactStatus)
			if(contactStatus == "closed") state.closedContactMap.put(contactName, contactStatus)
		}
		contactMapHandler()
	}
	if(locks) {
		locks.each { device ->
			def lockName = device.displayName
			def lockStatus = device.currentValue('lock')
			if(logEnable) log.debug "In maintHandler - Working on ${lockName} - ${lockStatus}"
			if(lockStatus == "locked") state.lockedLockMap.put(lockName, lockStatus)
			if(lockStatus == "unlocked") state.unlockedLockMap.put(lockName, lockStatus)
		}
		lockMapHandler()
	}
	if(temps) {
		temps.each { device ->
			def tempName = device.displayName
			def tempStatus = device.currentValue('temperature')
			if(logEnable) log.debug "In maintHandler - Working on ${tempName} - ${tempStatus}"
			if(tempStatus <= tempLow) state.lowTempMap.put(tempName, tempStatus)
			if(tempStatus >= tempHigh) state.highTempMap.put(tempName, tempStatus)
			if((tempStatus > tempLow) && (tempStatus < tempHigh)) state.normalTempMap.put(tempName, tempStatus)
		}
		tempMapHandler()
	}
}

def appButtonHandler(btn){  // *****************************
	// section(){input "resetBtn", "button", title: "Click here to reset maps"}
    if(reportMode == "Regular") runIn(1, maintHandler)
	if(reportMode == "Priority") runIn(1, priorityHandler)
}  

// ********** Normal Stuff **********
def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	if(logEnable) log.debug "In enablerSwitchHandler - Enabler Switch: ${state.enablerSwitch2}"
	if(state.enablerSwitch2 == "on") { if(logEnable) log.debug "${app.label} is disabled." }
}

def pauseAppHandler(){
    if(pauseApp == true){
        if(app.label.contains('Paused')){
			if(logEnable) log.debug "App Paused - state.pauseApp: ${state.pauseApp}"
		} else {
			app.updateLabel(app.label + ("<font color='red'> (Paused) </font>"))
			if(logEnable) log.debug "App Paused - state.pauseApp: ${state.pauseApp}"
       	}
    }
    if(pauseApp == false){
     	if(app.label.contains('Paused')){
			app.updateLabel(app.label.minus("<font color='red'> (Paused) </font>"))
			if(logEnable) log.debug "App no longer Paused - state.pauseApp: ${state.pauseApp}"                        
        }
	}      
}

def getImage(type){							// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){				// Modified from @Stephack
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pauseApp", "bool", title: "Pause App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Snapshot - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
