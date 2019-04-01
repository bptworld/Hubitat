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
 *	V1.0.4 - 04/01/19 - Added Lock options
 *	V1.0.3 - 04/01/19 - Added Priority Device Options
 *	V1.0.2 - 03/30/19 - Added ability to select what type of data to report: Full, Only On/Off, Only Open/Closed. Also added count attributes.
 *	V1.0.1 - 03/22/19 - Major update to comply with Hubitat's new dashboard requirements.
 *  V1.0.0 - 03/16/19 - Initial Release
 *
 */

def setVersion() {
	state.version = "v1.0.4"
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
			} else {
				paragraph "Note: Choose a max of 30 devices in each category."
            	input "switches", "capability.switch", title: "Switches", multiple: true, required: false, submitOnChange: true
            	input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false, submitOnChange: true
				input "locks", "capability.lock", title: "Door Locks", multiple: true, required: false, submitOnChange: true
			}
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			if(reportMode == "Priority") {
				input "isDataDevice", "capability.switch", title: "Turn this device on if there are devices to report", submitOnChange: true, required: false, multiple: false
			} else {
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
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Snapshot Tile'<br> - Use our 'Snapshot Tile' Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Snapshot Tile<br>- Pick a template = attribute<br>- 3rd box = snapshotSwitch1-6, snapshotContact1-6, snapshotPrioritySwitch1-2 or snapshotPriorityContact1-2"
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
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOGDEBUG("Updated with settings: ${settings}")
	unsubscribe()
	logCheck()
	initialize()
}

def initialize() {
	LOGDEBUG("In initialize...")
	if(enableSwitch1) subscribe(enableSwitch, "switch", switchEnable)
	if(triggerMode == "On Demand") subscribe(onDemandSwitch, "switch.on", onDemandSwitchHandler)
	if(triggerMode == "Every X minutes") subscribe(repeatSwitch, "switch", repeatSwitchHandler)
	if(triggerMode == "Real Time") subscribe(realTimeSwitch, "switch", realTimeSwitchHandler)
}

def realTimeSwitchHandler(evt) {
	LOGDEBUG("In realTimeSwitchHandler...")
	state.realTimeSwitchStatus = evt.value
	if(reportMode == "Regular") {
		if(state.realTimeSwitchStatus == "on") {
			LOGDEBUG("In realTimeSwitchHandler - subscribe")
			subscribe(switches, "switch", switchHandler)
			subscribe(contacts, "contact", contactHandler)
			subscribe(locks, "lock", lockHandler)
			runIn(1, maintHandler)
		} else {
			LOGDEBUG("In realTimeSwitchHandler - unsubscribe")
			unsubscribe(switches)
			unsubscribe(contacts)
			unsubscribe(locks)
		}
	}
	if(reportMode == "Priority") {
		if(state.realTimeSwitchStatus == "on") {
			LOGDEBUG("In realTimeSwitchHandler Priority - subscribe")
			subscribe(switchesOn, "switch", priorityHandler)
			subscribe(switchesOff, "switch", priorityHandler)
			subscribe(contactsOpen, "contact", priorityHandler)
			subscribe(contactsClosed, "contact", priorityHandler)
			subscribe(locksUnlocked, "lock", priorityHandler)
			subscribe(locksLocked, "lock", priorityHandler)
			runIn(1, priorityHandler)
		} else {
			LOGDEBUG("In realTimeSwitchHandler Priority - unsubscribe")
			unsubscribe(switchesOn)
			unsubscribe(switchesOff)
			unsubscribe(contactsOpen)
			unsubscribe(contactsClosed)
			unsubscribe(locksUnlocked)
			unsubscribe(locksLocked)
		}
	}
}

def repeatSwitchHandler(evt) {
	LOGDEBUG("In repeatSwitchHandler...")
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

def onDemandSwitchHandler(evt) {
	LOGDEBUG("In onDemandSwitchHandler...")
	state.onDemandSwitchStatus = evt.value
	if(reportMode == "Regular") {
		if(state.onDemandSwitchStatus == "on") maintHandler()
	}
	if(reportMode == "Priority") {
		if(state.onDemandSwitchStatus == "on") priorityHandler()
	}
}

def switchMapHandler() {
	LOGDEBUG("In switchMapHandler...")
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
			LOGDEBUG("In switchMapHandler - Building Table ON with ${stuffOn.key} count: ${state.count}")
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
			LOGDEBUG("In switchMapHandler - Building Table OFF with ${stuffOff.key} count: ${state.count}")
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
	
	LOGDEBUG("In switchMapHandler - <br>fSwitchMap1S<br>${state.fSwitchMap1S}")
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
	LOGDEBUG("In contactMapHandler...")
	checkMaps()
	LOGDEBUG("In contactMapHandler - Sorting Maps")
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
			LOGDEBUG("In contactMapHandler - Building Table OPEN with ${stuffOpen.key} count: ${state.count}")
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
			LOGDEBUG("In contactMapHandler - Building Table CLOSED with ${stuffClosed.key} count: ${state.count}")
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

	LOGDEBUG("In contactMapHandler - <br>fContactMap1S<br>${state.fContactMap1S}")
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
	LOGDEBUG("In lockMapHandler...")
	checkMaps()
	state.unlockedLockMapS = state.unlockedLockMap.sort { a, b -> a.key <=> b.key }
	state.lockedLockMapS = state.lockedLockMap.sort { a, b -> a.key <=> b.key }
	LOGDEBUG("In lockMapHandler - Building Lock Maps")
	state.fLockMap1S = "<table width='100%'>"
	state.fLockMap2S = "<table width='100%'>"
	state.count = 0
	state.countUnlocked = 0
	state.countLocked = 0
	
	if(lockMode == "Full" || lockMode == "Only Unlocked") {
		state.unlockedLockMapS.each { stuffUnlocked -> 
			state.count = state.count + 1
			state.countUnlocked = state.countUnlocked + 1
			LOGDEBUG("In lockMapHandler - Building Table UNLOCKED with ${stuffUnlocked.key} count: ${state.count}")
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
			LOGDEBUG("In lockMapHandler - Building Table LOCKED with ${stuffLocked.key} count: ${state.count}")
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

	LOGDEBUG("In lockMapHandler - <br>fLockMap1S<br>${state.fLockMap1S}")
   	snapshotTileDevice.sendSnapshotLockMap1(state.fLockMap1S)
	snapshotTileDevice.sendSnapshotLockMap2(state.fLockMap2S)
	snapshotTileDevice.sendSnapshotLockCountUnlocked(state.countUnlocked)
	snapshotTileDevice.sendSnapshotLockCountLocked(state.countLocked)
}

def switchHandler(evt){
	def switchName = evt.displayName
	def switchStatus = evt.value
	LOGDEBUG("In switchHandler...${switchName} - ${switchStatus}")
	if(switchStatus == "on") {
		state.offSwitchMap.remove(switchName)
		state.onSwitchMap.put(switchName, switchStatus)
		LOGDEBUG("In switchHandler - ON<br>${state.onSwitchMap}")
	}
	if(switchStatus == "off") {
		state.onSwitchMap.remove(switchName)
		state.offSwitchMap.put(switchName, switchStatus)
		LOGDEBUG("In switchHandler - OFF<br>${state.offSwitchMap}")
	}
	switchMapHandler()
}

def contactHandler(evt){
	def contactName = evt.displayName
	def contactStatus = evt.value
	LOGDEBUG("In contactHandler...${contactName}: ${contactStatus}")
	if(contactStatus == "open") {
		state.closedContactMap.remove(contactName)
		state.openContactMap.put(contactName, contactStatus)
		LOGDEBUG("In contactHandler - OPEN<br>${state.openContactMap}")
	}
	if(contactStatus == "closed") {
		state.openContactMap.remove(contactName)
		state.closedContactMap.put(contactName, contactStatus)
		LOGDEBUG("In contactHandler - CLOSED<br>${state.closedContactMap}")
	}
	contactMapHandler()
}

def lockHandler(evt){
	def lockName = evt.displayName
	def lockStatus = evt.value
	LOGDEBUG("In lockHandler...${lockName}: ${lockStatus}")
	if(lockStatus == "unlocked") {
		state.lockedLockMap.remove(lockName)
		state.unlockedLockMap.put(lockName, lockStatus)
		LOGDEBUG("In lockHandler - UNLOCKED<br>${state.unlockedLockMap}")
	}
	if(lockStatus == "locked") {
		state.unlockedLockMap.remove(lockName)
		state.lockedLockMap.put(lockName, lockStatus)
		LOGDEBUG("In lockHandler - LOCKED<br>${state.lockedLockMap}")
	}
	lockMapHandler()
}

def priorityHandler(evt){
	state.wrongSwitchMap = [:]
	state.wrongContactMap = [:]
	state.wrongLockMap = [:]
	switchesOn.each { sOn -> 
		def switchName = sOn.displayName
		def switchStatus = sOn.currentValue('switch')
		LOGDEBUG("In priorityHandler - Switch On - ${switchName} - ${switchStatus}")
		if(switchStatus == "off") state.wrongSwitchMap.put(switchName, switchStatus)
	}
	switchesOff.each { sOff -> 
		def switchName = sOff.displayName
		def switchStatus = sOff.currentValue('switch')
		LOGDEBUG("In priorityHandler - Switch Off - ${switchName} - ${switchStatus}")
		if(switchStatus == "on") state.wrongSwitchMap.put(switchName, switchStatus)
	}
	contactsOpen.each { cOpen ->
		def contactName = cOpen.displayName
		def contactStatus = cOpen.currentValue('contact')
		LOGDEBUG("In priorityHandler - Contact Open - ${contactName} - ${contactStatus}")
		if(contactStatus == "closed") state.wrongContactMap.put(contactName, contactStatus)
	}
	contactsClosed.each { cClosed ->
		def contactName = cClosed.displayName
		def contactStatus = cClosed.currentValue('contact')
		LOGDEBUG("In priorityHandler - Contact Closed - ${contactName} - ${contactStatus}")
		if(contactStatus == "open") state.wrongContactMap.put(contactName, contactStatus)
	}
	locksUnlocked.each { lUnlocked ->
		def lockName = lUnlocked.displayName
		def lockStatus = lUnlocked.currentValue('lock')
		LOGDEBUG("In priorityHandler - Locks Unlocked - ${lockName} - ${lockStatus}")
		if(lockStatus == "locked") state.wrongLockMap.put(lockName, lockStatus)
	}
	locksLocked.each { lLocked ->
		def lockName = lLocked.displayName
		def lockStatus = lLocked.currentValue('lock')
		LOGDEBUG("In priorityHandler - Locks Locked - ${lockName} - ${lockStatus}")
		if(lockStatus == "unlocked") state.wrongLockMap.put(lockName, lockStatus)
	}
	checkMaps()
	state.wrongSwitchMapS = state.wrongSwitchMap.sort { a, b -> a.key <=> b.key }
	state.wrongContactMapS = state.wrongContactMap.sort { a, b -> a.key <=> b.key }
	state.wrongLockMapS = state.wrongLockMap.sort { a, b -> a.key <=> b.key }
// Start Priority Switch
	state.pSwitchMap1S = "<table width='100%'>"
	state.pSwitchMap2S = "<table width='100%'>"
	state.count = 0
	state.wrongSwitchMapS.each { wSwitch -> 
		state.count = state.count + 1
		state.isPriorityData = "true"
		LOGDEBUG("In priorityHandler - Building Table Wrong Switch with ${wSwitch.key} count: ${state.count}")
		if((state.count >= 1) && (state.count <= 5)) state.pSwitchMap1S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></td><td><div style='color: red;'>${wSwitch.value}</div></td></tr>"
		if((state.count >= 6) && (state.count <= 10)) state.pSwitchMap2S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></td><td><div style='color: red;'>${wSwitch.value}</div></td></tr>"
	}
	state.pSwitchMap1S += "</table>"
	state.pSwitchMap2S += "</table>"
	
	if(state.count == 0) {
		state.pSwitchMap1S = "<table width='100%'><tr><td><div style='color: green;'>No devices to report<br>Everything is OK</div></td></tr></table>"
		state.pSwitchMap2S = "<table width='100%'><tr><td><div style='color: green;'>No devices to report<br>Everything is OK</div></td></tr></table>"
		state.isPriorityData = "false"
	}
	
// Start Priority Contacts
	state.pContactMap1S = "<table width='100%'>"
	state.pContactMap2S = "<table width='100%'>"
	state.count = 0
	state.wrongContactMapS.each { wContact -> 
		state.count = state.count + 1
		state.isPriorityData = "true"
		LOGDEBUG("In priorityHandler - Building Table Wrong Contact with ${wContact.key} count: ${state.count}")
		if((state.count >= 1) && (state.count <= 5)) state.pContactMap1S += "<tr><td><div style='color: red;'>${wContact.key}</div></td><td><div style='color: red;'>${wContact.value}</div></td></tr>"
		if((state.count >= 6) && (state.count <= 10)) state.pContactMap2S += "<tr><td><div style='color: red;'>${wContact.key}</div></td><td><div style='color: red;'>${wContact.value}</div></td></tr>"
	}
	state.pContactMap1S += "</table>"
	state.pContactMap2S += "</table>"
	
	if(state.count == 0) {
		state.pContactMap1S = "<table width='100%'><tr><td><div style='color: green;'>Everything is OK</div></td></tr></table>"
		state.pContactMap2S = "<table width='100%'><tr><td><div style='color: green;'>Everything is OK</div></td></tr></table>"
		state.isPriorityData = "false"
	}
// Start Priority Locks
	state.pLockMap1S = "<table width='100%'>"
	state.pLockMap2S = "<table width='100%'>"
	state.count = 0
	state.wrongLockMapS.each { wLock -> 
		state.count = state.count + 1
		state.isPriorityData = "true"
		LOGDEBUG("In priorityHandler - Building Table Wrong Lock with ${wLock.key} count: ${state.count}")
		if((state.count >= 1) && (state.count <= 5)) state.pLockMap1S += "<tr><td><div style='color: red;'>${wLock.key}</div></td><td><div style='color: red;'>${wLock.value}</div></td></tr>"
		if((state.count >= 6) && (state.count <= 10)) state.pLockMap2S += "<tr><td><div style='color: red;'>${wLock.key}</div></td><td><div style='color: red;'>${wLock.value}</div></td></tr>"
	}
	state.pLockMap1S += "</table>"
	state.pLockMap2S += "</table>"
	
	if(state.count == 0) {
		state.pLockMap1S = "<table width='100%'><tr><td><div style='color: green;'>Everything is OK</div></td></tr></table>"
		state.pLockMap2S = "<table width='100%'><tr><td><div style='color: green;'>Everything is OK</div></td></tr></table>"
		state.isPriorityData = "false"
	}
	
// Sending to tile
	snapshotTileDevice.sendSnapshotPrioritySwitchMap1(state.pSwitchMap1S)
	snapshotTileDevice.sendSnapshotPrioritySwitchMap2(state.pSwitchMap2S)
	snapshotTileDevice.sendSnapshotPriorityContactMap1(state.pContactMap1S)
	snapshotTileDevice.sendSnapshotPriorityContactMap2(state.pContactMap2S)
	snapshotTileDevice.sendSnapshotPriorityLockMap1(state.pLockMap1S)
	snapshotTileDevice.sendSnapshotPriorityLockMap2(state.pLockMap2S)
	
	if((isDataDevice) && (state.isPriorityData == "true")) isDataDevice.on()
	if((isDataDevice) && (state.isPriorityData == "false")) isDataDevice.off()
}

def checkMaps() {
	LOGDEBUG("In checkMaps...") 
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
	if(state.wrongSwitchMap == null) {
		state.wrongSwitchMap = [:]
	}
	if(state.wrongContactMap == null) {
		state.wrongContactMap = [:]
	}
	LOGDEBUG("In checkMaps - Finished")
}

def maintHandler(evt){
	LOGDEBUG("In maintHandler...")
	state.offSwitchMap = [:]
	state.onSwitchMap = [:]
	state.closedContactMap = [:]
	state.openContactMap = [:] 
	state.lockedLockMap = [:]
	state.unlockedLockMap = [:]
	LOGDEBUG("In maintHandler...Tables have been cleared!")
	LOGDEBUG("In maintHandler...Repopulating tables")
	switches.each { device ->
		def switchName = device.displayName
		def switchStatus = device.currentValue('switch')
		LOGDEBUG("In maintHandler - Working on ${switchName} - ${switchStatus}")
		if(switchStatus == "on") state.onSwitchMap.put(switchName, switchStatus)
		if(switchStatus == "off") state.offSwitchMap.put(switchName, switchStatus)
	}
	switchMapHandler()
	contacts.each { device ->
		def contactName = device.displayName
		def contactStatus = device.currentValue('contact')
		LOGDEBUG("In maintHandler - Working on ${contactName} - ${contactStatus}")
		if(contactStatus == "open") state.openContactMap.put(contactName, contactStatus)
		if(contactStatus == "closed") state.closedContactMap.put(contactName, contactStatus)
	}
	contactMapHandler()
	locks.each { device ->
		def lockName = device.displayName
		def lockStatus = device.currentValue('lock')
		LOGDEBUG("In maintHandler - Working on ${lockName} - ${lockStatus}")
		if(lockStatus == "locked") state.lockedLockMap.put(lockName, lockStatus)
		if(lockStatus == "unlocked") state.unlockedLockMap.put(lockName, lockStatus)
	}
	lockMapHandler()
}

def appButtonHandler(btn){  // *****************************
	// section(){input "resetBtn", "button", title: "Click here to reset maps"}
    if(reportMode == "Regular") runIn(1, maintHandler)
	if(reportMode == "Priority") runIn(1, priorityHandler)
}  

// Normal Stuff

def pauseOrNot(){
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {						// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){				// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Snapshot - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
