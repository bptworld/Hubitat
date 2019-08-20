/**
 *  ****************  Snapshot Child  ****************
 *
 *  Design Usage:
 *  Monitor devices and sensors. Easily see their status right on your dashboard and/or get a notification - speech and phone.
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
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
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
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.1.5 - 06/11/19 - Code cleanup
 *  V1.1.4 - 04/30/19 - Added Water Sensor tracking
 *  V1.1.3 - 04/19/19 - Fixed a bug with Presence Sensors
 *  V1.1.2 - 04/15/19 - Code cleanup
 *  V1.1.1 - 04/12/19 - Added Presence Sensor tracking
 *  V1.1.0 - 04/12/19 - Added voice and pushover notifications to Priority Devices
 *  V1.0.9 - 04/10/19 - Fixed a typo in repeatSwitchHandler
 *	V1.0.8 - 04/09/19 - Fixed Temp maps
 *	V1.0.7 - 04/09/19 - Chasing gremlins
 *	V1.0.6 - 04/06/19 - Code cleanup
 *	V1.0.5 - 04/02/19 - Added Temp tile options
 *	V1.0.4 - 04/01/19 - Added Lock options
 *	V1.0.3 - 04/01/19 - Added Priority Device Options
 *	V1.0.2 - 03/30/19 - Added ability to select what type of data to report: Full, Only On/Off, Only Open/Closed. Also added count attributes.
 *	V1.0.1 - 03/22/19 - Major update to comply with Hubitat's new dashboard requirements.
 *  V1.0.0 - 03/16/19 - Initial Release
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "SnapshotChildVersion"
	state.version = "v2.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
            schedule("0 0 3 ? * * *", setVersion)
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
	name: "Snapshot Child",
	namespace: "BPTWorld",
	author: "Bryan Turcotte",
	description: "Monitor devices and sensors. Easily see their status right on your dashboard and/or get a notification - speech and phone.",
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
			paragraph "Monitor devices and sensors. Easily see their status right on your dashboard and/or get a notification - speech and phone."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Type of Trigger")) {
			input "reportMode", "enum", required: true, title: "Select Report Type", submitOnChange: true,  options: ["Regular", "Priority"]
			if(reportMode == "Regular") paragraph "Regular - Will show device state based on options below"
			if(reportMode == "Priority") paragraph "Priority - Show device state only when it's not what it should be based on options below"
			input "triggerMode", "enum", required: true, title: "Select Trigger Frequency", submitOnChange: true,  options: ["Real Time", "Every X minutes", "On Demand"]
			if(triggerMode == "Real Time") {
				paragraph "<b>Remember - Too many devices updating in real time can slow down and/or crash the hub.</b>"
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
				input "water", "capability.waterSensor", title: "Water Sensors", multiple: true, required: false, submitOnChange: true
				input "locks", "capability.lock", title: "Door Locks", multiple: true, required: false, submitOnChange: true
				input "presence", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false, submitOnChange: true
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
				if(water) {
					input "waterMode", "enum", required: true, title: "Select Water Display Output Type", submitOnChange: true,  options: ["Full", "Only Wet", "Only Dry"]
				}
				if(locks) {
					input "lockMode", "enum", required: true, title: "Select Lock Display Output Type", submitOnChange: true,  options: ["Full", "Only Unlocked", "Only Locked"]
				}
				if(presence) {
					input "presenceMode", "enum", required: true, title: "Select Presence Display Output Type", submitOnChange: true,  options: ["Full", "Only Present", "Only Not Present"]
				}
				if(temps) {
					input "tempMode", "enum", required: true, title: "Select Temperature Display Output Type", submitOnChange: true,  options: ["Full", "Only High", "Only Low"]
				}
			}
		}
		if(reportMode == "Priority") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Priority Notification Options")) {
				paragraph "Receive priority device notifications on demand with both voice and pushover options. Great before leaving the house or going to bed."
				paragraph "Recommended to create a virtual device with 'Enable auto off' set to '1s'"
				input "priorityCheckSwitch", "capability.switch", title: "Priority Check Switch", required: false
				paragraph "Each of the following messages will only be spoken if necessary..."
				input(name: "oRandomPre", type: "bool", defaultValue: "false", title: "Random Pre Message?", description: "Random", submitOnChange: "true")
				if(!oRandomPre) input "preMsg", "text", required: true, title: "Pre Message - Single message", defaultValue: "Warning"
				if(oRandomPre) {
					input "preMsg", "text", title: "Random Pre Message - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
					input(name: "oPreList", type: "bool", defaultValue: "false", title: "Show a list view of the random pre messages?", description: "List View", submitOnChange: "true")
					if(oPreList) {
						def valuesPre = "${preMsg}".split(";")
						listMapPre = ""
    					valuesPre.each { itemPre -> listMapPre += "${itemPre}<br>" }
						paragraph "${listMapPre}"
					}
				}
				input "msgDevice", "text", required: true, title: "Message to speak when a device isn't in the correct state... Will be followed by device names", defaultValue: "The following devices are in the wrong state"
				input(name: "oRandomPost", type: "bool", defaultValue: "false", title: "Random Post Message?", description: "Random", submitOnChange: "true")
				if(!oRandomPost) input "postMsg", "text", required: true, title: "Post Message - Single message", defaultValue: "This is all I have to say"
				if(oRandomPost) {
					input "postMsg", "text", title: "Random Post Message - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
					input(name: "oPostList", type: "bool", defaultValue: "false", title: "Show a list view of the random post messages?", description: "List View", submitOnChange: "true")
					if(oPostList) {
						def valuesPost = "${postMsg}".split(";")
						listMapPost = ""
    					valuesPost.each { itemPost -> listMapPost += "${itemPost}<br>" }
						paragraph "${listMapPost}"
					}
				}
			}	
        	section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
          		input "speechMode", "enum", required: false, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
				if (speechMode == "Music Player"){ 
              		input "speakers", "capability.musicPlayer", title: "Choose speaker(s)", required: true, multiple: true, submitOnChange: true
					paragraph "<hr>"
					paragraph "If you are using the 'Echo Speaks' app with your Echo devices then turn this option ON.<br>If you are NOT using the 'Echo Speaks' app then please leave it OFF."
					input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' app device?", description: "Echo speaks device", submitOnChange: true)
					if(echoSpeaks) input "restoreVolume", "number", title: "Volume to restore speaker to AFTER anouncement", description: "0-100%", required: true, defaultValue: "30"
          		}   
        		if (speechMode == "Speech Synth"){ 
         			input "speakers", "capability.speechSynthesis", title: "Choose speaker(s)", required: true, multiple: true
          		}
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
				if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
      		}
			section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
				paragraph "NOTE: Not all speakers can use volume controls."
				input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true
				input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true
            	input "volQuiet", "number", title: "Quiet Time Speaker volume", description: "0-100", required: false, submitOnChange: true
				if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true
    			if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true
			}
    		if(speechMode){ 
				section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
        			input "fromTime", "time", title: "From", required: false
        			input "toTime", "time", title: "To", required: false
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
	unschedule()
	initialize()
}

def initialize() {
	setDefaults()
	if(logEnable) log.debug "In initialize..."
	if(triggerMode == "On Demand") subscribe(onDemandSwitch, "switch.on", onDemandSwitchHandler)
	if(triggerMode == "Every X minutes") subscribe(repeatSwitch, "switch", repeatSwitchHandler)
	if(triggerMode == "Real Time") {
		subscribe(realTimeSwitch, "switch", realTimeSwitchHandler)
		subscribe(priorityCheckSwitch, "switch.on", priorityCheckHandler)
	}
}

def realTimeSwitchHandler(evt) {
	if(logEnable) log.debug "In realTimeSwitchHandler..."
	state.realTimeSwitchStatus = evt.value
	if(reportMode == "Regular") {
		if(state.realTimeSwitchStatus == "on") {
			if(logEnable) log.debug "In realTimeSwitchHandler - subscribe"
			if(switches) subscribe(switches, "switch", switchHandler)
			if(contacts) subscribe(contacts, "contact", contactHandler)
			if(water) subscribe(water, "water", waterHandler)
			if(locks) subscribe(locks, "lock", lockHandler)
			if(presence) subscribe(presence, "presence", presenceHandler)
			if(temps) subscribe(temps, "temperature", temperatureHandler)
			runIn(1, maintHandler)
		} else {
			if(logEnable) log.debug "In realTimeSwitchHandler - unsubscribe"
			unsubscribe(switches)
			unsubscribe(contacts)
			unsubscribe(water)
			unsubscribe(locks)
			unsubscribe(presence)
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

def repeatSwitchHandler(evt) {
	if(logEnable) log.debug "In repeatSwitchHandler..."
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
		runIn(state.runDelay,repeatSwitchHandler)
	}
}

def onDemandSwitchHandler(evt) {
	if(logEnable) log.debug "In onDemandSwitchHandler..."
	state.onDemandSwitchStatus = evt.value
	if(reportMode == "Regular") {
		if(state.onDemandSwitchStatus == "on") maintHandler()
	}
	if(reportMode == "Priority") {
		if(state.onDemandSwitchStatus == "on") priorityHandler()
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

def waterMapHandler() {
	if(logEnable) log.debug "In waterMapHandler..."
	checkMaps()
	if(logEnable) log.debug "In waterMapHandler - Sorting Maps"
	state.wetWaterMapS = state.wetWaterMap.sort { a, b -> a.key <=> b.key }
	state.dryWaterMapS = state.dryWaterMap.sort { a, b -> a.key <=> b.key }
	
	state.fWaterMap1S = "<table width='100%'>"
	state.fWaterMap2S = "<table width='100%'>"
	state.count = 0
	state.countWet = 0
	state.countDry = 0
	
	if(waterMode == "Full" || waterMode == "Only Wet") {
		state.wetWaterMapS.each { stuffWet -> 
			state.count = state.count + 1
			state.countWet = state.countWet + 1
			if(logEnable) log.debug "In waterMapHandler - Building Table WET with ${stuffWet.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fWaterMap1S += "<tr><td>${stuffWet.key}</td><td><div style='color: red;'>wet</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fWaterMap2S += "<tr><td>${stuffWet.key}</td><td><div style='color: red;'>wet</div></td></tr>"
		}
	}
	
	if(waterMode == "Full") {
		if((state.count >= 1) && (state.count <= 5)) { state.fWaterMap1S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
		if((state.count >= 6) && (state.count <= 10)) { state.fWaterMap2S += "<tr><td colspan='2'><hr></td></tr>"; state.count = state.count + 1 }
	}
	
	if(waterMode == "Full" || waterMode == "Only Dry") {
		state.dryWaterMapS.each { stuffDry -> 
			state.count = state.count + 1
			state.countDry = state.countDry + 1
			if(logEnable) log.debug "In waterMapHandler - Building Table DRY with ${stuffDry.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fWaterMap1S += "<tr><td>${stuffDry.key}</td><td><div style='color: green;'>dry</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fWaterMap2S += "<tr><td>${stuffDry.key}</td><td><div style='color: green;'>dry</div></td></tr>"
		}
	}
	
	state.fWaterMap1S += "</table>"
	state.fWaterMap2S += "</table>"
	
	if(state.count == 0) {
		state.fWaterMap1S = "<table width='100%'><tr><td><div style='color: green;'>No water devices to report</div></td></tr></table>"
		state.fWaterMap2S = "<table width='100%'><tr><td><div style='color: green;'>No water devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In waterMapHandler - <br>fWaterMap1S<br>${state.fWaterMap1S}"
   	snapshotTileDevice.sendSnapshotWaterMap1(state.fWaterMap1S)
	snapshotTileDevice.sendSnapshotWaterMap2(state.fWaterMap2S)
	snapshotTileDevice.sendSnapshotWaterCountWet(state.countWet)
	snapshotTileDevice.sendSnapshotWaterCountDry(state.countDry)
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

def presenceMapHandler() {
	if(logEnable) log.debug "In presenceMapHandler..."
	checkMaps()
	state.notPresentMapS = state.notPresentMap.sort { a, b -> a.key <=> b.key }
	state.presentMapS = state.presentMap.sort { a, b -> a.key <=> b.key }
	if(logEnable) log.debug "In presenceMapHandler - Building Presence Maps"
	state.fPresenceMap1S = "<table width='100%'>"
	state.fPresenceMap2S = "<table width='100%'>"
	state.count = 0
	state.countNotPresent = 0
	state.countPresent = 0
	
	if(presenceMode == "Full" || presenceMode == "Only Not Present") {
		state.notPresentMapS.each { stuffNotPresent -> 
			state.count = state.count + 1
			state.countNotPresent = state.countNotPresent + 1
			if(logEnable) log.debug "In presenceMapHandler - Building Table Not Present with ${stuffNotPresent.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fPresenceMap1S += "<tr><td>${stuffNotPresent.key}</td><td><div style='color: red;'>not present</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fPresenceMap2S += "<tr><td>${stuffNotPresent.key}</td><td><div style='color: red;'>not present</div></td></tr>"
		}
	}
	
	if(presenceMode == "Full" || presenceMode == "Only Present") {
		state.presentMapS.each { stuffPresent -> 
			state.count = state.count + 1
			state.countPresent = state.countPresent + 1
			if(logEnable) log.debug "In presenceMapHandler - Building Table Present with ${stuffPresent.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.fPresenceMap1S += "<tr><td>${stuffPresent.key}</td><td><div style='color: green;'>present</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.fPresenceMap2S += "<tr><td>${stuffPresent.key}</td><td><div style='color: green;'>present</div></td></tr>"
		}
	}
	
	state.fPresenceMap1S += "</table>"
	state.fPresenceMap2S += "</table>"
	
	if(state.count == 0) {
		state.fPresenceMap1S = "<table width='100%'><tr><td><div style='color: green;'>No presence devices to report</div></td></tr></table>"
		state.fPresenceMap2S = "<table width='100%'><tr><td><div style='color: green;'>No presence devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In presenceMapHandler - <br>fPresenceMap1S<br>${state.fPresenceMap1S}"
   	snapshotTileDevice.sendSnapshotPresenceMap1(state.fPresenceMap1S)
	snapshotTileDevice.sendSnapshotPresenceMap2(state.fPresenceMap2S)
	snapshotTileDevice.sendSnapshotPresenceCountNotPresent(state.countNotPresent)
	snapshotTileDevice.sendSnapshotPresenceCountPresent(state.countPresent)
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
			if(logEnable) log.debug "In tempMapHandler - Building Table Normal with ${stuffNormal.key} count: ${state.count}"
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

	if(logEnable) log.debug "In tempMapHandler - <br>${state.fTempMap1S}"
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

def waterHandler(evt){
	def waterName = evt.displayName
	def waterStatus = evt.value
	if(logEnable) log.debug "In waterHandler...${waterName}: ${waterStatus}"
	if(waterStatus == "wet") {
		state.dryWaterMap.remove(waterName)
		state.wetWaterMap.put(waterName, waterStatus)
		if(logEnable) log.debug "In waterHandler - WET<br>${state.wetWaterMap}"
	}
	if(waterStatus == "dry") {
		state.wetWaterMap.remove(waterName)
		state.dryWaterMap.put(waterName, waterStatus)
		if(logEnable) log.debug "In waterHandler - Dry<br>${state.dryWaterMap}"
	}
	waterMapHandler()
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
	def tempStatusI = tempStatus.toFloat()
	def tempHighI = tempHigh.toFloat()
	def tempLowI = tempLow.toFloat()
	if(logEnable) log.debug "In temperatureHandler...${tempName}: ${tempStatus} (high: ${tempHigh}, low: ${tempLow})"
	if(tempStatusI >= tempHighI) {
		if(logEnable) log.debug "In temperatureHandler - High"
		state.normalTempMap.remove(tempName)
		state.lowTempMap.remove(tempName)
		state.highTempMap.put(tempName, tempStatusI)
		if(logEnable) log.debug "In temperatureHandler - HIGH<br>${state.highTempMap}"
	} else 
	if(tempStatusI <= tempLowI) {
		if(logEnable) log.debug "In temperatureHandler - Low"
		state.normalTempMap.remove(tempName)
		state.highTempMap.remove(tempName)
		state.lowTempMap.put(tempName, tempStatusI)
		if(logEnable) log.debug "In temperatureHandler - LOW<br>${state.lowTempMap}"
	} else 
	if((tempStatusI > tempLowI) && (tempStatusI < tempHighI)) {
		if(logEnable) log.debug "In temperatureHandler - Normal"
		state.highTempMap.remove(tempName)
		state.lowTempMap.remove(tempName)
		state.normalTempMap.put(tempName, tempStatusI)
		if(logEnable) log.debug "In temperatureHandler - Normal<br>${state.normalTempMap}"
	} else {
		if(logEnable) log.debug "In temperatureHandler - Something isn't right"	
	}
	tempMapHandler()
}

def priorityHandler(evt){
	checkMaps()
// Start Priority Switch
	if(switchesOn || switchesOff) {
		state.wrongSwitchMap = [:]
		state.wrongSwitchPushMap = ""
		state.wrongStateSwitchMap = ""
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
            if(wSwitch == switchesOn) def switchID = theSwitch.deviceNetworkId
			state.count = state.count + 1
			state.isPriorityData = "true"
			state.prioritySwitch = "true"
			state.wrongStateSwitchMap += "${wSwitch.key}, "
			if(logEnable) log.debug "In priorityHandler - Building Table Wrong Switch with ${wSwitch.key} count: ${state.count}"
		//	if((state.count >= 1) && (state.count <= 5)) state.pSwitchMap1S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></a></td><td><a href='${wSwitch}.on()'><div style='color: red;'>${wSwitch.value}</div></td></tr>"
            
            //command = "input 'toggleBtn', 'button', title: 'Tog'"
            if((state.count >= 1) && (state.count <= 5)) {
                state.pSwitchMap1S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></a></td><td>"
                state.pSwitchMap1S += input 'toggleBtn', 'button', title: 'Tog'
                state.pSwitchMap1S += "</td></tr>"
            }
            
            
            
			if((state.count >= 6) && (state.count <= 10)) state.pSwitchMap2S += "<tr><td><div style='color: red;'>${wSwitch.key}</div></td><td><div style='color: red;'>${wSwitch.value}</div></td></tr>"
			state.wrongSwitchPushMap += "${wSwitch.key} \n"
		}
		state.pSwitchMap1S += "</table>"
		state.pSwitchMap2S += "</table>"
	
		if(state.count == 0) {
			state.pSwitchMap1S = "<table width='100%'><tr><td><div style='color: green;'>No devices to report</div></td></tr></table>"
			state.pSwitchMap2S = "<table width='100%'><tr><td><div style='color: green;'>No devices to report</div></td></tr></table>"
			state.wrongSwitchPushMap = ""
			state.isPriorityData = "false"
			state.prioritySwitch = "false"
		}
		snapshotTileDevice.sendSnapshotPrioritySwitchMap1(state.pSwitchMap1S)
		snapshotTileDevice.sendSnapshotPrioritySwitchMap2(state.pSwitchMap2S)
	}
	
// Start Priority Contacts
	if(contactsOpen || contactsClosed) {
		state.wrongContactMap = [:]
		state.wrongStateContactMap = ""
		state.wrongContactPushMap = ""
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
			state.priorityContact = "true"
			state.wrongStateContactMap += "${wContact.key}, "
			if(logEnable) log.debug "In priorityHandler - Building Table Wrong Contact with ${wContact.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.pContactMap1S += "<tr><td><div style='color: red;'>${wContact.key}</div></td><td><div style='color: red;'>${wContact.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.pContactMap2S += "<tr><td><div style='color: red;'>${wContact.key}</div></td><td><div style='color: red;'>${wContact.value}</div></td></tr>"
			state.wrongContactPushMap += "${wContact.key} \n"
		}
		state.pContactMap1S += "</table>"
		state.pContactMap2S += "</table>"
	
		if(state.count == 0) {
			state.pContactMap1S = "<table width='100%'><tr><td><div style='color: green;'>No contacts to report</div></td></tr></table>"
			state.pContactMap2S = "<table width='100%'><tr><td><div style='color: green;'>No contacts to report</div></td></tr></table>"
			state.wrongContactPushMap = ""
			state.isPriorityData = "false"
			state.priorityContact = "false"
		}
		snapshotTileDevice.sendSnapshotPriorityContactMap1(state.pContactMap1S)
		snapshotTileDevice.sendSnapshotPriorityContactMap2(state.pContactMap2S)
	}
		
// Start Priority Locks
	if(locksUnlocked || locksLocked) {
		state.wrongLockMap = [:]
		state.wrongStateLockMap = ""
		state.wrongLockPushMap = ""
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
			state.priorityLock = "true"
			state.wrongStateLockMap += "${wLock.key}, "
			if(logEnable) log.debug "In priorityHandler - Building Table Wrong Lock with ${wLock.key} count: ${state.count}"
			if((state.count >= 1) && (state.count <= 5)) state.pLockMap1S += "<tr><td><div style='color: red;'>${wLock.key}</div></td><td><div style='color: red;'>${wLock.value}</div></td></tr>"
			if((state.count >= 6) && (state.count <= 10)) state.pLockMap2S += "<tr><td><div style='color: red;'>${wLock.key}</div></td><td><div style='color: red;'>${wLock.value}</div></td></tr>"
			state.wrongLockPushMap += "${wLock.key} \n"
		}
		state.pLockMap1S += "</table>"
		state.pLockMap2S += "</table>"
	
		if(state.count == 0) {
			state.pLockMap1S = "<table width='100%'><tr><td><div style='color: green;'>No locks to report</div></td></tr></table>"
			state.pLockMap2S = "<table width='100%'><tr><td><div style='color: green;'>No locks to report</div></td></tr></table>"
			state.wrongLockPushMap = ""
			state.isPriorityData = "false"
			state.priorityLock = "false"
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
	if(state.dryWaterMap == null) {
		state.dryWaterMap = [:]
	}
	if(state.wetWaterMap == null) {
		state.wetWaterMap = [:]
	}
	if(state.presentMap == null) {
		state.presentMap = [:]
	}
	if(state.notPresentMap == null) {
		state.notPresentMap = [:]
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
	state.wetWaterMap = [:]
	state.dryWaterMap = [:]
	state.presentMap = [:]
	state.notPresentMap = [:]
	state.lockedLockMap = [:]
	state.unlockedLockMap = [:]
	state.highTempMap = [:]
	state.lowTempMap = [:]
	state.normalTempMap = [:]
	if(logEnable) log.debug "In maintHandler - Tables have been cleared!"
	if(logEnable) log.debug "In maintHandler - Repopulating tables"
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
	if(water) {
		water.each { device ->
			def waterName = device.displayName
			def waterStatus = device.currentValue('water')
			if(logEnable) log.debug "In maintHandler - Working on ${waterName} - ${waterStatus}"
			if(waterStatus == "wet") state.wetWaterMap.put(waterName, waterStatus)
			if(waterStatus == "dry") state.dryWaterMap.put(waterName, waterStatus)
		}
		waterMapHandler()
	}
	if(presence) {
		presence.each { device ->
			def presenceName = device.displayName
			def presenceStatus = device.currentValue('presence')
			if(logEnable) log.debug "In maintHandler - Working on ${presenceName} - ${presenceStatus}"
			if(presenceStatus == "present") state.presentMap.put(presenceName, presenceStatus)
			if(presenceStatus == "not present") state.notPresentMap.put(presenceName, presenceStatus)
		}
		presenceMapHandler()
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
			def tempStatusI = tempStatus.toFloat()
			def tempHighI = tempHigh.toFloat()
			def tempLowI = tempLow.toFloat()
			if(logEnable) log.debug "In maintHandler - Working on ${tempName} - ${tempStatusI}"
			if(tempStatusI <= tempLowI) state.lowTempMap.put(tempName, tempStatusI)
			if(tempStatusI >= tempHighI) state.highTempMap.put(tempName, tempStatusI)
			if((tempStatusI > tempLowI) && (tempStatusI < tempHighI)) state.normalTempMap.put(tempName, tempStatusI)
		}
		tempMapHandler()
	}
}

def priorityCheckHandler(evt) {
	if(logEnable) log.debug "In priorityCheckHandler..."
	priorityHandler()
	if(speakers) letsTalk()
	if(sendPushMessage) pushNow()
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk..."
		checkTime()
		checkVol()
		pauseExecution(atomicState.randomPause)
		if(logEnable) log.debug "In letsTalk - continuing"
		if(state.timeBetween == true) {
			messageHandler()
			if(logEnable) log.debug "Speaker(s) in use: ${speakers}"
  			if (speechMode == "Music Player"){ 
    			if(logEnable) log.debug "Music Player"
				if(echoSpeaks) {
					speakers.setVolumeSpeakAndRestore(state.volume, state.theMsg, volRestore)
					if(logEnable) log.debug "In letsTalk - MP Echo Speaks - Wow, that's it!"
				}
				if(!echoSpeaks) {
    				if(volSpeech) speakers.setLevel(state.volume)
    				speakers.playTextAndRestore(state.theMsg, volRestore)
					state.canSpeak = "no"
					if(logEnable) log.debug "In letsTalk - Music Player - Wow, that's it!"
				}
  			}   
			if(speechMode == "Speech Synth"){ 
				speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
				atomicState.speechDuration2 = speechDuration * 1000
				if(logEnable) log.debug "Speech Synth - speakers: ${speakers}, vol: ${state.volume}, msg: ${state.theMsg}"
				if(volSpeech) speakers.setVolume(state.volume)
				speakers.speak(state.theMsg)
				pauseExecution(atomicState.speechDuration2)
				if(volRestore) speakers.setVolume(volRestore)
				if(logEnable) log.debug "In letsTalk - Speech Synth - Wow, that's it!"
			}
			log.info "${app.label} - ${state.theMsg}"
		} else {
			if(logEnable) log.debug "In letsTalk - It's quiet time"
		}
}

def checkTime() {
	if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) {
			state.timeBetween = true
		} else {
			state.timeBetween = false
		}
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def checkVol() {
	if(logEnable) log.debug "In checkVol..."
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
    	if(state.quietTime) {
    		state.volume = volQuiet
		} else {
			state.volume = volSpeech
		}
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - volume: ${state.volume}"
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler..."
	
	if(oRandomPre) {
		def values = "${preMsg}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		state.preMsgR = values[randomKey]
		if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, Pre Msg: ${state.preMsgR}"
	} else {
		state.preMsgR = "${preMsg}"
		if(logEnable) log.debug "In messageHandler - Static - Pre Msg: ${state.preMsgR}"
	}
	
	if(oRandomPost) {
		def values = "${postMsg}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		state.postMsgR = values[randomKey]
		if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, Post Msg: ${state.postMsgR}"
	} else {
		state.postMsgR = "${postMsg}"
		if(logEnable) log.debug "In messageHandler - Static - Post Msg: ${state.postMsgR}"
	}
	
	state.theMsg = "${state.preMsgR}, ${msgDevice},"
	if(state.prioritySwitch == "true") state.theMsg += " ${state.wrongStateSwitchMap}"
	if(state.priorityContact == "true") state.theMsg += " ${state.wrongStateContactMap}"
	if(state.priorityLock == "true") state.theMsg += " ${state.wrongStateLockMap}"
	state.theMsg += ". ${state.postMsgR}"
	if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
}

def appButtonHandler(btn){  // *****************************
	// section(){input "resetBtn", "button", title: "Click here to reset maps"}
    if(reportMode == "Regular") runIn(1, maintHandler)
	if(reportMode == "Priority") runIn(1, priorityHandler)
    if(toggelBtn == "Toggle") log.info "It worked"
}  

def pushNow(){
	if(logEnable) log.debug "In pushNow..."
	theMsg = ""
	if(state.prioritySwitch == "true") {
		state.wrongSwitchPushMap2 = "PRIORITY SWITCHES IN WRONG STATE \n"
		state.wrongSwitchPushMap2 += "${state.wrongSwitchPushMap} \n"
		theMsg = "${state.wrongSwitchPushMap2} \n"
	}
	if(state.priorityContact == "true") {
		state.wrongContactPushMap2 = "PRIORITY CONTACTS IN WRONG STATE \n"
		state.wrongContactPushMap2 += "${state.wrongContactPushMap} \n"
		theMsg += "${state.wrongContactPushMap2} \n"
	}
	if(state.priorityLock == "true") {
		state.wrongLockPushMap2 = "PRIORITY LOCKS IN WRONG STATE \n"
		state.wrongLockPushMap2 += "${state.wrongLockPushMap} \n"
		theMsg += "${state.wrongLockPushMap2} \n"
	}
	pushMessage = "${theMsg}"
    if(theMsg) sendPushMessage.deviceNotification(pushMessage)
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable) log.debug "In setDefaults..."
	if(priorityCheckSwitch == null){priorityCheckSwitch = "off"}
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
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Snapshot - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
