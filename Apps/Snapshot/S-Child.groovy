/**
 *  ****************  Snapshot Child  ****************
 *
 *  Design Usage:
 *  Monitor devices and sensors. Easily see their status right on your dashboard and/or get a notification - speech and phone.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
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
 *  2.0.2 - 04/27/20 - Cosmetic changes
 *  2.0.1 - 09/01/19 - Added custom color options
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  --
 *  V1.0.0 - 03/16/19 - Initial Release
 *
 */

def setVersion(){
    state.name = "Snapshot"
	state.version = "2.0.2"
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
	dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {	
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
                if(temps) {
                    input "tempHigh", "number", title: "Temp to consider High if over X", required: true, submitOnChange: true, width: 6
                    input "tempHighColor", "enum", required: true, title: "Color when temp is high", submitOnChange: true,  options: ["Black", "Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                }
                if(temps) {
                    input "tempLow", "number", title: "Temp to consider Low if under X", required: true, submitOnChange: true, width: 6
                    input "tempLowColor", "enum", required: true, title: "Color when temp is high", submitOnChange: true,  options: ["Black", "Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                }
			}
			if(reportMode == "Regular") {
				paragraph "Note: Choose a max of 30 devices in each category."
            	input "switches", "capability.switch", title: "Switches", multiple: true, required: false, submitOnChange: true
            	input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false, submitOnChange: true
				input "water", "capability.waterSensor", title: "Water Sensors", multiple: true, required: false, submitOnChange: true
				input "locks", "capability.lock", title: "Door Locks", multiple: true, required: false, submitOnChange: true
				input "presence", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false, submitOnChange: true
				input "temps", "capability.temperatureMeasurement", title: "Temperature Devices", multiple: true, required: false, submitOnChange: true
                if(temps) input "tempHigh", "number", title: "Temp to consider High if over X", required: true, submitOnChange: true, width: 6
                if(temps) input "tempLow", "number", title: "Temp to consider Low if under X", required: true, submitOnChange: true, width: 6
			}
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Color Options")) {
            if(switches || switchesOn || switchesOff) {
                input "switchesOnColor", "enum", required: true, title: "Color when switch is on", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "switchesOffColor", "enum", required: true, title: "Color when switch is off", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(contacts || contactsOpen || contactsClosed) {
                input "contactsOpenColor", "enum", required: true, title: "Color when contact is open", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "contactsClosedColor", "enum", required: true, title: "Color when contact is closed", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(water) {
                input "wateWetColor", "enum", required: true, title: "Color when water is wet", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "waterDryColor", "enum", required: true, title: "Color when water is dry", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(locks || locksLocked || lockUnlocked) {
                input "locksUnlockedColor", "enum", required: true, title: "Color when lock is unlocked", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "locksLockedColor", "enum", required: true, title: "Color when lock is locked", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(presence) {
                input "presenceNotPresentColor", "enum", required: true, title: "Color when presence is Not Present", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "presencePresentColor", "enum", required: true, title: "Color when presence is Present", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(temps) {
                input "tempHighColor", "enum", required: true, title: "Color when temp is High", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "tempLowColor", "enum", required: true, title: "Color when temp is Low", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            input "textNoColor", "enum", required: true, title: "Text color when there is no devices to display", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"]
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			if(reportMode == "Priority") {
				if(temps) {
					input "tempMode", "enum", required: true, title: "Select Temperature Display Output Type", submitOnChange: true,  options: ["Only High", "Only Low"]
				}
				input "isDataDevice", "capability.switch", title: "Turn this device on if there are devices to report", submitOnChange: true, required: false, multiple: false
			}
			if(reportMode == "Regular") {
				paragraph "Choose the amount/type of data to record"
				if(switches) {
					input "switchMode", "enum", required: true, title: "Select Switches Display Output Type", submitOnChange: true,  options: ["Full", "Only On", "Only Off"]
                    paragraph "If device has power reporting capabilities, should it be reported on the tile?"
                    input(name: "switchPower", type: "bool", defaultValue: "false", title: "Report device Power", description: "Power", submitOnChange: "true")
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
	if(triggerMode == "Real Time" && reportMode == "Regular") {
		if(switches) subscribe(switches, "switch", switchMapHandler)
		if(contacts) subscribe(contacts, "contact", contactHandler)
		if(water) subscribe(water, "water", waterHandler)
		if(locks) subscribe(locks, "lock", lockHandler)
		if(presence) subscribe(presence, "presence", presenceHandler)
		if(temps) subscribe(temps, "temperature", temperatureHandler)
	}
    if(triggerMode == "Real Time" && reportMode == "Priority") {
        if(switchesOn) subscribe(switchesOn, "switch", priorityHandler)
		if(switchesOff) subscribe(switchesOff, "switch", priorityHandler)
		if(contactsOpen) subscribe(contactsOpen, "contact", priorityHandler)
		if(contactsClosed) subscribe(contactsClosed, "contact", priorityHandler)
		if(locksUnlocked) subscribe(locksUnlocked, "lock", priorityHandler)
		if(locksLocked) subscribe(locksLocked, "lock", priorityHandler)
		if(temps) subscribe(temps, "temperature", priorityHandler)
    }
}

def repeatSwitchHandler(evt) {
	if(logEnable) log.debug "In repeatSwitchHandler..."
	state.repeatSwitchStatus = repeatSwitch.currentValue("switch")
	state.runDelay = timeDelay * 60
	if(reportMode == "Regular") {
		if(state.repeatSwitchStatus == "on") {
			if(switches) switchMapHandler()
            if(contacts) contactMapHandler()
            if(water) waterMapHandler()
            if(locks) lockMapHandler()
            if(presence) presenceMapHandler()
            if(temps) tempMapHandler()
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
        if(state.onDemandSwitchStatus == "on") {
            if(switches) switchMapHandler()
            if(contacts) contactMapHandler()
            if(water) waterMapHandler()
            if(locks) lockMapHandler()
            if(presence) presenceMapHandler()
            if(temps) tempMapHandler()
        }
	}
	if(reportMode == "Priority") {
		if(state.onDemandSwitchStatus == "on") priorityHandler()
	}
}

def switchMapHandler(evt) {
    if(logEnable) log.debug "------------------------------- Start Switch Map Handler -------------------------------"
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
		switches.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("switch")
            try {
                powerLvl = it.currentValue("power")
                if(powerLvl == null) powerLvl = ""
                if(logEnable) log.trace "Snapshot - device: ${it} - power: ${powerLvl}"
            } catch(e) {
                if(logEnable) log.trace "Snapshot - device: ${it} - Doesn't have power attribute"
                if(powerLvl == null) powerLvl = ""
            }
            if(logEnable) log.debug "In switchMapHandler - Working on: ${switchName} - ${switchStatus}"
            if(switchStatus == "on") {  
			    state.count = state.count + 1
			    state.countOn = state.countOn + 1
			    if(logEnable) log.debug "In switchMapHandler - Building Table ON with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fSwitchMap1S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>on</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fSwitchMap2S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>on</div></td></tr>"
			    if((state.count >= 11) && (state.count <= 15)) state.fSwitchMap3S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>on</div></td></tr>"
			    if((state.count >= 16) && (state.count <= 20)) state.fSwitchMap4S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>on</div></td></tr>"
			    if((state.count >= 21) && (state.count <= 25)) state.fSwitchMap5S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>on</div></td></tr>"
			    if((state.count >= 26) && (state.count <= 30)) state.fSwitchMap6S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>on</div></td></tr>"
		    }
        }
        
	    switches.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("switch")
            try {
                powerLvl = it.currentValue("power")
                if(powerLvl == null) powerLvl = ""
                if(logEnable) log.trace "Snapshot - device: ${it} - power: ${powerLvl}"
            } catch(e) {
                if(logEnable) log.trace "Snapshot - device: ${it} - Doesn't have power attribute"
                if(powerLvl == null) powerLvl = ""
            }
            if(logEnable) log.debug "In switchMapHandler - Working on: ${switchName} - ${switchStatus}"
	        if(switchMode == "Full" || switchMode == "Only Off") {
	    	    if(switchStatus == "off") {
		           	state.count = state.count + 1
			        state.countOff = state.countOff + 1
			        if(logEnable) log.debug "In switchMapHandler - Building Table OFF with ${switchName} count: ${state.count}"
			        if((state.count >= 1) && (state.count <= 5)) state.fSwitchMap1S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>off</div></td></tr>"
			        if((state.count >= 6) && (state.count <= 10)) state.fSwitchMap2S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>off</div></td></tr>"
		    	    if((state.count >= 11) && (state.count <= 15)) state.fSwitchMap3S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>off</div></td></tr>"
		    	    if((state.count >= 16) && (state.count <= 20)) state.fSwitchMap4S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>off</div></td></tr>"	
		    	    if((state.count >= 21) && (state.count <= 25)) state.fSwitchMap5S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>off</div></td></tr>"	
			        if((state.count >= 26) && (state.count <= 30)) state.fSwitchMap6S += "<tr><td>${switchName}</td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>off</div></td></tr>"	
		        }
            }
	    }
    }
	
	state.fSwitchMap1S += "</table>"
	state.fSwitchMap2S += "</table>"
	state.fSwitchMap3S += "</table>"
	state.fSwitchMap4S += "</table>"
	state.fSwitchMap5S += "</table>"
	state.fSwitchMap6S += "</table>"
	
	if(state.count == 0) {
        state.fSwitchMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap3S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap4S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap5S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No switch devices to report</div></td></tr></table>"
		state.fSwitchMap6S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No switch devices to report</div></td></tr></table>"
	}
	
	if(logEnable) log.debug "In switchMapHandler - <br>fSwitchMap1S<br>${state.fSwitchMap1S}"
    if(snapshotTileDevice) {
        snapshotTileDevice.sendSnapshotSwitchMap1(state.fSwitchMap1S)
	    snapshotTileDevice.sendSnapshotSwitchMap2(state.fSwitchMap2S)
	    snapshotTileDevice.sendSnapshotSwitchMap3(state.fSwitchMap3S)
	    snapshotTileDevice.sendSnapshotSwitchMap4(state.fSwitchMap4S)
	    snapshotTileDevice.sendSnapshotSwitchMap5(state.fSwitchMap5S)
	    snapshotTileDevice.sendSnapshotSwitchMap6(state.fSwitchMap6S)
	    snapshotTileDevice.sendSnapshotSwitchCountOn(state.countOn)
	    snapshotTileDevice.sendSnapshotSwitchCountOff(state.countOff)
    } else {
        log.warn "Snapshot - NO DEVICE choosen to send data to"   
    }
    if(logEnable) log.debug "------------------------------- End Switch Map Handler -------------------------------"
}

def contactMapHandler(evt) {
    if(logEnable) log.debug "------------------------------- Start Contact Map Handler -------------------------------"
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
		contacts.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("contact")
            if(logEnable) log.debug "In switchMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "open") {
			    state.count = state.count + 1
			    state.countOpen = state.countOpen + 1
			    if(logEnable) log.debug "In contactMapHandler - Building Table OPEN with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fContactMap1S += "<tr><td>${switchName}</td><td><div style='color: ${contactsOpenColor};'>open</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fContactMap2S += "<tr><td>${switchName}</td><td><div style='color: ${contactsOpenColor};'>open</div></td></tr>"
			    if((state.count >= 11) && (state.count <= 15)) state.fContactMap3S += "<tr><td>${switchName}</td><td><div style='color: ${contactsOpenColor};'>open</div></td></tr>"
			    if((state.count >= 16) && (state.count <= 20)) state.fContactMap4S += "<tr><td>${switchName}</td><td><div style='color: ${contactsOpenColor};'>open</div></td></tr>"
			    if((state.count >= 21) && (state.count <= 25)) state.fContactMap5S += "<tr><td>${switchName}</td><td><div style='color: ${contactsOpenColor};'>open</div></td></tr>"
			    if((state.count >= 26) && (state.count <= 30)) state.fContactMap6S += "<tr><td>${switchName}</td><td><div style='color: ${contactsOpenColor};'>open</div></td></tr>"
		    }
        }
	}

	if(contactMode == "Full" || contactMode == "Only Closed") {
        contacts.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("contact")
            if(logEnable) log.debug "In contactMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "closed") {
		        state.count = state.count + 1
			    state.countClosed = state.countClosed + 1
			    if(logEnable) log.debug "In contactMapHandler - Building Table CLOSED with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fContactMap1S += "<tr><td>${switchName}</td><td><div style='color: ${contactsClosedColor};'>closed</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fContactMap2S += "<tr><td>${switchName}</td><td><div style='color: ${contactsClosedColor};'>closed</div></td></tr>"
			    if((state.count >= 11) && (state.count <= 15)) state.fContactMap3S += "<tr><td>${switchName}</td><td><div style='color: ${contactsClosedColor};'>closed</div></td></tr>"
			    if((state.count >= 16) && (state.count <= 20)) state.fContactMap4S += "<tr><td>${switchName}</td><td><div style='color: ${contactsClosedColor};'>closed</div></td></tr>"
			    if((state.count >= 21) && (state.count <= 25)) state.fContactMap5S += "<tr><td>${switchName}</td><td><div style='color: ${contactsClosedColor};'>closed</div></td></tr>"
			    if((state.count >= 26) && (state.count <= 30)) state.fContactMap6S += "<tr><td>${switchName}</td><td><div style='color: ${contactsClosedColor};'>closed</div></td></tr>"
		    }
        }
	}
	
	state.fContactMap1S += "</table>"
	state.fContactMap2S += "</table>"
	state.fContactMap3S += "</table>"
	state.fContactMap4S += "</table>"
	state.fContactMap5S += "</table>"
	state.fContactMap6S += "</table>"
	
	if(state.count == 0) {
		state.fContactMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contact devices to report</div></td></tr></table>"
		state.fContactMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contact devices to report</div></td></tr></table>"
		state.fContactMap3S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contact devices to report</div></td></tr></table>"
		state.fContactMap4S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contact devices to report</div></td></tr></table>"
		state.fContactMap5S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contact devices to report</div></td></tr></table>"
		state.fContactMap6S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contact devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In contactMapHandler - <br>fContactMap1S<br>${state.fContactMap1S}"
    if(snapshotTileDevice) {
       	snapshotTileDevice.sendSnapshotContactMap1(state.fContactMap1S)
	    snapshotTileDevice.sendSnapshotContactMap2(state.fContactMap2S)
	    snapshotTileDevice.sendSnapshotContactMap3(state.fContactMap3S)
	    snapshotTileDevice.sendSnapshotContactMap4(state.fContactMap4S)
	    snapshotTileDevice.sendSnapshotContactMap5(state.fContactMap5S)
	    snapshotTileDevice.sendSnapshotContactMap6(state.fContactMap6S)
	    snapshotTileDevice.sendSnapshotContactCountOpen(state.countOpen)
	    snapshotTileDevice.sendSnapshotContactCountClosed(state.countClosed)
    } else {
        log.warn "Snapshot - NO DEVICE choosen to send data to"   
    }
    if(logEnable) log.debug "------------------------------- End Contact Map Handler -------------------------------"
}

def waterMapHandler(evt) {
    if(logEnable) log.debug "------------------------------- Start Water Map Handler -------------------------------"
	state.fWaterMap1S = "<table width='100%'>"
	state.fWaterMap2S = "<table width='100%'>"
	state.count = 0
	state.countWet = 0
	state.countDry = 0
	
	if(waterMode == "Full" || waterMode == "Only Wet") {
		water.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("water")
            if(logEnable) log.debug "In waterMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "wet") {
			    state.count = state.count + 1
			    state.countWet = state.countWet + 1
			    if(logEnable) log.debug "In waterMapHandler - Building Table WET with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fWaterMap1S += "<tr><td>${switchName}</td><td><div style='color: ${wateWetColor};'>wet</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fWaterMap2S += "<tr><td>${switchName}</td><td><div style='color: ${wateWetColor};'>wet</div></td></tr>"
		    }
        }
	}
	
	if(waterMode == "Full" || waterMode == "Only Dry") {
		water.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("water")
            if(logEnable) log.debug "In waterMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "dry") {
			    state.count = state.count + 1
			    state.countDry = state.countDry + 1
			    if(logEnable) log.debug "In waterMapHandler - Building Table DRY with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fWaterMap1S += "<tr><td>${switchName}</td><td><div style='color: ${wateDryColor};'>dry</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fWaterMap2S += "<tr><td>${switchName}</td><td><div style='color: ${wateDryColor};'>dry</div></td></tr>"
            }
		}
	}
	
	state.fWaterMap1S += "</table>"
	state.fWaterMap2S += "</table>"
	
	if(state.count == 0) {
		state.fWaterMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No water devices to report</div></td></tr></table>"
		state.fWaterMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No water devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In waterMapHandler - <br>fWaterMap1S<br>${state.fWaterMap1S}"
    if(snapshotTileDevice) {
   	    snapshotTileDevice.sendSnapshotWaterMap1(state.fWaterMap1S)
	    snapshotTileDevice.sendSnapshotWaterMap2(state.fWaterMap2S)
	    snapshotTileDevice.sendSnapshotWaterCountWet(state.countWet)
	    snapshotTileDevice.sendSnapshotWaterCountDry(state.countDry)
    } else {
        log.warn "Snapshot - NO DEVICE choosen to send data to"   
    }
    if(logEnable) log.debug "------------------------------- End Water Map Handler -------------------------------"
}

def lockMapHandler(evt) {
    if(logEnable) log.debug "------------------------------- Start Lock Map Handler -------------------------------"
	state.fLockMap1S = "<table width='100%'>"
	state.fLockMap2S = "<table width='100%'>"
	state.count = 0
	state.countUnlocked = 0
	state.countLocked = 0
	
	if(lockMode == "Full" || lockMode == "Only Unlocked") {
		locks.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("lock")
            if(logEnable) log.debug "In lockMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "unlocked") {
			    state.count = state.count + 1
			    state.countUnlocked = state.countUnlocked + 1
			    if(logEnable) log.debug "In lockMapHandler - Building Table UNLOCKED with ${stuffUnlocked.key} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fLockMap1S += "<tr><td>${stuffUnlocked.key}</td><td><div style='color: ${locksUnlockedColor};'>unlocked</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fLockMap2S += "<tr><td>${stuffUnlocked.key}</td><td><div style='color: ${locksUnlockedColor};'>unlocked</div></td></tr>"
            }
		}
	}

	if(lockMode == "Full" || lockMode == "Only Locked") {
		locks.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("lock")
            if(logEnable) log.debug "In lockMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "locked") {
			    state.count = state.count + 1
			    state.countLocked = state.countLocked + 1
			    if(logEnable) log.debug "In lockMapHandler - Building Table LOCKED with ${stuffLocked.key} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fLockMap1S += "<tr><td>${stuffLocked.key}</td><td><div style='color: ${locksLockedColor};'>locked</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fLockMap2S += "<tr><td>${stuffLocked.key}</td><td><div style='color: ${locksLockedColor};'>locked</div></td></tr>"
            }
		}
	}
	
	state.fLockMap1S += "</table>"
	state.fLockMap2S += "</table>"
	
	if(state.count == 0) {
		state.fLockMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No lock devices to report</div></td></tr></table>"
		state.fLockMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No lock devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In lockMapHandler - <br>fLockMap1S<br>${state.fLockMap1S}"
    if(snapshotTileDevice) {
   	    snapshotTileDevice.sendSnapshotLockMap1(state.fLockMap1S)
	    snapshotTileDevice.sendSnapshotLockMap2(state.fLockMap2S)
	    snapshotTileDevice.sendSnapshotLockCountUnlocked(state.countUnlocked)
	    snapshotTileDevice.sendSnapshotLockCountLocked(state.countLocked)
        } else {
        log.warn "Snapshot - NO DEVICE choosen to send data to"   
    }
    if(logEnable) log.debug "------------------------------- End Lock Map Handler -------------------------------"
}

def presenceMapHandler(evt) {
    if(logEnable) log.debug "------------------------------- Start Presence Map Handler -------------------------------"
	if(logEnable) log.debug "In presenceMapHandler - Building Presence Maps"
	state.fPresenceMap1S = "<table width='100%'>"
	state.fPresenceMap2S = "<table width='100%'>"
	state.count = 0
	state.countNotPresent = 0
	state.countPresent = 0
	
	if(presenceMode == "Full" || presenceMode == "Only Not Present") {
		presence.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("lock")
            if(logEnable) log.debug "In presenceMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "not present") { 
			    state.count = state.count + 1
			    state.countNotPresent = state.countNotPresent + 1
			    if(logEnable) log.debug "In presenceMapHandler - Building Table Not Present with ${stuffNotPresent.key} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fPresenceMap1S += "<tr><td>${stuffNotPresent.key}</td><td><div style='color: ${presenceNotPresentColor};'>not present</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fPresenceMap2S += "<tr><td>${stuffNotPresent.key}</td><td><div style='color: ${presenceNotPresentColor};'>not present</div></td></tr>"
            }
		}
	}
	
	if(presenceMode == "Full" || presenceMode == "Only Present") {
		presence.each { it ->
            switchName = it.displayName
	        switchStatus = it.currentValue("lock")
            if(logEnable) log.debug "In presenceMapHandler - Working on: ${switchName} - ${switchStatus}" 
            if(switchStatus == "present") { 
			    state.count = state.count + 1
			    state.countPresent = state.countPresent + 1
			    if(logEnable) log.debug "In presenceMapHandler - Building Table Present with ${stuffPresent.key} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fPresenceMap1S += "<tr><td>${stuffPresent.key}</td><td><div style='color: ${presencePresentColor};'>present</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fPresenceMap2S += "<tr><td>${stuffPresent.key}</td><td><div style='color: ${presencePresentColor};'>present</div></td></tr>"
            }
		}
	}
	
	state.fPresenceMap1S += "</table>"
	state.fPresenceMap2S += "</table>"
	
	if(state.count == 0) {
		state.fPresenceMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No presence devices to report</div></td></tr></table>"
		state.fPresenceMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No presence devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In presenceMapHandler - <br>fPresenceMap1S<br>${state.fPresenceMap1S}"
    if(snapshotTileDevice) {
   	    snapshotTileDevice.sendSnapshotPresenceMap1(state.fPresenceMap1S)
	    snapshotTileDevice.sendSnapshotPresenceMap2(state.fPresenceMap2S)
	    snapshotTileDevice.sendSnapshotPresenceCountNotPresent(state.countNotPresent)
	    snapshotTileDevice.sendSnapshotPresenceCountPresent(state.countPresent)
    } else {
        log.warn "Snapshot - NO DEVICE choosen to send data to"   
    }
    if(logEnable) log.debug "------------------------------- End Presence Map Handler -------------------------------"
}

def tempMapHandler(evt) {
    if(logEnable) log.debug "------------------------------- Start Temp Map Handler -------------------------------"
	if(logEnable) log.debug "In tempMapHandler - Building Temp Maps"
	state.fTempMap1S = "<table width='100%'>"
	state.fTempMap2S = "<table width='100%'>"
	state.count = 0
	state.countHigh = 0
	state.countLow = 0
	
	if(tempMode == "Full" || tempMode == "Only High") {
		temps.each { it ->
            switchName = it.displayName
	        tempStatus = it.currentValue("temperature")
            tempStatusI = tempStatus.toFloat()
	        tempHighI = tempHigh.toFloat()
	        tempLowI = tempLow.toFloat()
            if(logEnable) log.debug "In tempMapHandler - Working on: ${switchName} - ${tempStatus}" 
            if(tempStatusI >= tempHighI) { 
			    state.count = state.count + 1
			    state.countHigh = state.countHigh + 1
			    if(logEnable) log.debug "In tempMapHandler - Building Table High with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fTempMap1S += "<tr><td>${switchName}</td><td><div style='color: ${tempHighColor};'>${tempStatusI}</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fTempMap2S += "<tr><td>${switchName}</td><td><div style='color: ${tempHighColor};'>${tempStatusI}</div></td></tr>"
            }
		}
	}
	
	if(tempMode == "Full") {
		temps.each { it ->
            switchName = it.displayName
	        tempStatus = it.currentValue("temperature")
            tempStatusI = tempStatus.toFloat()
	        tempHighI = tempHigh.toFloat()
	        tempLowI = tempLow.toFloat()
            if(logEnable) log.debug "In tempMapHandler - Working on: ${switchName} - ${tempStatus}" 
            if((tempStatusI < tempHighI) && (tempStatusI > tempLowI)) { 
			    state.count = state.count + 1
			    if(logEnable) log.debug "In tempMapHandler - Building Table Normal with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fTempMap1S += "<tr><td>${switchName}</td><td>${tempStatusI}</td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fTempMap2S += "<tr><td>${switchName}</td><td>${tempStatusI}</td></tr>"
            }
		}
	}
	
	if(tempMode == "Full" || tempMode == "Only Low") {
		temps.each { it ->
            switchName = it.displayName
	        tempStatus = it.currentValue("temperature")
            tempStatusI = tempStatus.toFloat()
	        tempHighI = tempHigh.toFloat()
	        tempLowI = tempLow.toFloat()
            if(logEnable) log.debug "In tempMapHandler - Working on: ${switchName} - ${tempStatus}" 
            if(tempStatusI <= tempLowI) { 
			    state.count = state.count + 1
			    state.countLow = state.countLow + 1
			    if(logEnable) log.debug "In tempMapHandler - Building Table Low with ${switchName} count: ${state.count}"
			    if((state.count >= 1) && (state.count <= 5)) state.fTempMap1S += "<tr><td>${switchName}</td><td><div style='color: ${tempLowColor};'>${tempStatusI}</div></td></tr>"
			    if((state.count >= 6) && (state.count <= 10)) state.fTempMap2S += "<tr><td>${switchName}</td><td><div style='color: ${tempLowColor};'>${tempStatusI}</div></td></tr>"
            }
		}
	}
	
	state.fTempMap1S += "</table>"
	state.fTempMap2S += "</table>"
	
	if(state.count == 0) {
		state.fTempMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No temp devices to report</div></td></tr></table>"
		state.fTempMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No temp devices to report</div></td></tr></table>"
	}

	if(logEnable) log.debug "In tempMapHandler - <br>${state.fTempMap1S}"
    if(snapshotTileDevice) {
       	snapshotTileDevice.sendSnapshotTempMap1(state.fTempMap1S)
	    snapshotTileDevice.sendSnapshotTempMap2(state.fTempMap2S)
	    snapshotTileDevice.sendSnapshotTempCountHigh(state.countHigh)
	    snapshotTileDevice.sendSnapshotTempCountLow(state.countLow)
    } else {
        log.warn "Snapshot - NO DEVICE choosen to send data to"   
    }
    if(logEnable) log.debug "------------------------------- End Temp Map Handler -------------------------------"
}


// ***** Priority Stuff *****


def priorityHandler(evt){
    if(logEnable) log.debug "In priorityHandler"
    state.wrongSwitchPushMap = ""
    state.wrongContactPushMap = ""
    state.wrongLockPushMap = ""
    
    state.wrongStateSwitchPushMap = ""
    state.wrongStateContactPushMap = ""
    state.wrongStateLockPushMap = ""
    
    state.wrongStateSwitchMap = ""
    state.wrongStateContactMap = ""
    state.wrongStateLockMap = ""
    
    state.tempTooHighMap = ""
    state.tempTooLowMap = ""
    
    state.tempTooHighPushMap = ""
    state.tempTooLowPushMap = ""

// Start Priority Switch
	if(switchesOn || switchesOff) {
        if(logEnable) log.debug "In priorityHandler - switches"
		state.pSwitchMap1S = "<table width='100%'>"
		state.pSwitchMap2S = "<table width='100%'>"
		state.count = 0
		if(switchesOn) {
            if(logEnable) log.debug "In priorityHandler - switchesOn - ${sOn}"
			switchesOn.each { sOn -> 
				def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('switch')
                try {
                    powerLvl = sOn.currentValue("power")
                    if(powerLvl == null) powerLvl = ""
                    if(logEnable) log.trace "Snapshot - device: ${sOn} - power: ${powerLvl}"
                } catch(e) {
                    if(logEnable) log.trace "Snapshot - device: ${sOn} - Doesn't have power attribute"
                    if(powerLvl == null) powerLvl = ""
                }
                if(switchStatus == "off") {
			        state.count = state.count + 1
			        state.isPriorityData = "true"
			        state.prioritySwitch = "true"
			        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Switch with ${switchName} count: ${state.count}"
                    if((state.count >= 1) && (state.count <= 5)) state.pSwitchMap1S += "<tr><td><div style='color: ${switchesOffColor};'>${switchName}</div></td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>${switchStatus}</div></td></tr>"
                    if((state.count >= 6) && (state.count <= 10)) state.pSwitchMap2S += "<tr><td><div style='color: ${switchesOffColor};'>${switchName}</div></td><td> ${powerLvl} </td><td><div style='color: ${switchesOffColor};'>${switchStatus}</div></td></tr>" 
                    state.wrongSwitchPushMap += "${switchName} \n"
                }
            }
        }
        
        if(switchesOff) {
            if(logEnable) log.debug "In priorityHandler - switchesOff"
            switchesOff.each { sOff -> 
                if(logEnable) log.debug "In priorityHandler - switchesOff - ${sOff}"
				def switchName = sOff.displayName
				def switchStatus = sOff.currentValue('switch')
                try {
                    powerLvl = sOff.currentValue("power")
                    if(powerLvl == null) powerLvl = ""
                    if(logEnable) log.trace "Snapshot - device: ${sOff} - power: ${powerLvl}"
                } catch(e) {
                    if(logEnable) log.trace "Snapshot - device: ${sOff} - Doesn't have power attribute"
                    if(powerLvl == null) powerLvl = ""
                }
                if(switchStatus == "on") {
			        state.count = state.count + 1
			        state.isPriorityData = "true"
			        state.prioritySwitch = "true"
			        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Switch with ${switchName} count: ${state.count}"
                    if((state.count >= 1) && (state.count <= 5)) state.pSwitchMap1S += "<tr><td><div style='color: ${switchesOnColor};'>${switchName}</div></td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>${switchStatus}</div></td></tr>"
                    if((state.count >= 6) && (state.count <= 10)) state.pSwitchMap2S += "<tr><td><div style='color: ${switchesOnColor};'>${switchName}</div></td><td> ${powerLvl} </td><td><div style='color: ${switchesOnColor};'>${switchStatus}</div></td></tr>"    
                    state.wrongSwitchPushMap += "${switchName} \n"
                }
            }
		}
		state.pSwitchMap1S += "</table>"
		state.pSwitchMap2S += "</table>"
	
		if(state.count == 0) {
			state.pSwitchMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
			state.pSwitchMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
			state.wrongSwitchPushMap = ""
			state.isPriorityData = "false"
			state.prioritySwitch = "false"
		}
        if(snapshotTileDevice) {
		    snapshotTileDevice.sendSnapshotPrioritySwitchMap1(state.pSwitchMap1S)
		    snapshotTileDevice.sendSnapshotPrioritySwitchMap2(state.pSwitchMap2S)
        } else {
            log.warn "Snapshot - NO DEVICE choosen to send data to"   
        }
	}
	
// Start Priority Contacts
	if(contactsOpen || contactsClosed) {
        if(logEnable) log.debug "In priorityHandler - contacts"
        state.pContactMap1S = "<table width='100%'>"
		state.pContactMap2S = "<table width='100%'>"
		state.count = 0
		if(contactsClosed) {
			contactsClosed.each { sOn -> 
				def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('contact')
                if(switchStatus == "open") {
			        state.count = state.count + 1
			        state.isPriorityData = "true"
			        state.priorityContact = "true"
			        state.wrongStateContactMap += "${switchName}, "
			        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Contact with ${switchName} count: ${state.count}"
			        if((state.count >= 1) && (state.count <= 5)) state.pContactMap1S += "<tr><td><div style='color: ${contactsOpenColor};'>${switchName}</div></td><td><div style='color: ${contactsOpenColor};'>${switchStatus}</div></td></tr>"
			        if((state.count >= 6) && (state.count <= 10)) state.pContactMap2S += "<tr><td><div style='color: ${contactsOpenColor};'>${switchName}</div></td><td><div style='color: ${contactsOpenColor};'>${switchStatus}</div></td></tr>"
			        state.wrongContactPushMap += "${switchName} \n"
                }
            }
		}
        
        if(contactsOpen) {
			contactsOpen.each { sOn -> 
				def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('contact')
                if(switchStatus == "closed") {
			        state.count = state.count + 1
			        state.isPriorityData = "true"
			        state.priorityContact = "true"
			        state.wrongStateContactMap += "${switchName}, "
			        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Contact with ${switchName} count: ${state.count}"
			        if((state.count >= 1) && (state.count <= 5)) state.pContactMap1S += "<tr><td><div style='color: ${contactsClosedColor};'>${switchName}</div></td><td><div style='color: ${contactsClosedolor};'>${switchStatus}</div></td></tr>"
			        if((state.count >= 6) && (state.count <= 10)) state.pContactMap2S += "<tr><td><div style='color: ${contactsClosedColor};'>${switchName}</div></td><td><div style='color: ${contactsClosedColor};'>${switchStatus}</div></td></tr>"
			        state.wrongContactPushMap += "${switchName} \n"
                }
            }
		}
        
		state.pContactMap1S += "</table>"
		state.pContactMap2S += "</table>"
	
		if(state.count == 0) {
			state.pContactMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contacts to report</div></td></tr></table>"
			state.pContactMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No contacts to report</div></td></tr></table>"
			state.wrongContactPushMap = ""
			state.isPriorityData = "false"
			state.priorityContact = "false"
		}
        if(snapshotTileDevice) {
		    snapshotTileDevice.sendSnapshotPriorityContactMap1(state.pContactMap1S)
		    snapshotTileDevice.sendSnapshotPriorityContactMap2(state.pContactMap2S)
        } else {
            log.warn "Snapshot - NO DEVICE choosen to send data to"   
        }
	}
		
// Start Priority Locks

	if(locksUnlocked || locksLocked) {
        if(logEnable) log.debug "In priorityHandler - locks" 
		state.pLockMap1S = "<table width='100%'>"
		state.pLockMap2S = "<table width='100%'>"
		state.count = 0
		if(locksUnlocked) {
			locksUnlocked.each { sOn -> 
                def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('lock')
                if(switchStatus == "locked") {
			        state.count = state.count + 1
			        state.isPriorityData = "true"
			        state.priorityLock = "true"
			        state.wrongStateLockMap += "${switchName}, "
			        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Lock with ${switchName} count: ${state.count}"
                    if((state.count >= 1) && (state.count <= 5)) state.pLockMap1S += "<tr><td><div style='color: ${locksLockedColor};'>${switchName}</div></td><td><div style='color: ${locksLockedColor};'>${switchStatus}</div></td></tr>"
                    if((state.count >= 6) && (state.count <= 10)) state.pLockMap2S += "<tr><td><div style='color: ${locksLockedColor};'>${switchName}</div></td><td><div style='color: ${locksLockedColor};'>${switchStatus}</div></td></tr>"
			        state.wrongLockPushMap += "${switchName} \n"
                }
            }
		}
        
        if(locksLocked) {
			locksLocked.each { sOff -> 
                def switchName = sOff.displayName
				def switchStatus = sOff.currentValue('lock')
                if(switchStatus == "unlocked") {
			        state.count = state.count + 1
			        state.isPriorityData = "true"
			        state.priorityLock = "true"
			        state.wrongStateLockMap += "${switchName}, "
			        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Lock with ${switchName} count: ${state.count}"
                    if((state.count >= 1) && (state.count <= 5)) state.pLockMap1S += "<tr><td><div style='color: ${locksUnlockedColor};'>${switchName}</div></td><td><div style='color: ${locksUnlockedColor};'>${switchStatus}</div></td></tr>"
                    if((state.count >= 6) && (state.count <= 10)) state.pLockMap2S += "<tr><td><div style='color: ${locksUnlockedColor};'>${switchName}</div></td><td><div style='color: ${locksUnlockedColor};'>${switchStatus}</div></td></tr>"
			        state.wrongLockPushMap += "${switchName} \n"
                }
            }
		}
        
		state.pLockMap1S += "</table>"
		state.pLockMap2S += "</table>"
	
		if(state.count == 0) {
			state.pLockMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No locks to report</div></td></tr></table>"
			state.pLockMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No locks to report</div></td></tr></table>"
			state.wrongLockPushMap = ""
			state.isPriorityData = "false"
			state.priorityLock = "false"
		}
        if(snapshotTileDevice) {
		    snapshotTileDevice.sendSnapshotPriorityLockMap1(state.pLockMap1S)
		    snapshotTileDevice.sendSnapshotPriorityLockMap2(state.pLockMap2S)
        } else {
            log.warn "Snapshot - NO DEVICE choosen to send data to"   
        }
	}
	
// Start Priority Temps

	if(temps) {
        if(logEnable) log.debug "In priorityHandler - temps" 
		state.pTempMap1S = "<table width='100%'>"
		state.pTempMap2S = "<table width='100%'>"
		state.count = 0
        if(tempMode == "Only High") {
		    temps.each { it ->
                switchName = it.displayName
	            tempStatus = it.currentValue("temperature")
                tempStatusI = tempStatus.toFloat()
	            tempHighI = tempHigh.toFloat()
	            tempLowI = tempLow.toFloat()
                if(logEnable) log.debug "In tempMapHandler - High - Working on: ${switchName} - ${tempStatus} - tempHigh: ${tempHighI}" 
                if(tempStatusI >= tempHighI) {
				    state.count = state.count + 1
				    state.isPriorityData = "true"
                    state.priorityTempHigh = "true"
                    state.tempTooHighMap += "${switchName}, "
				    if(logEnable) log.debug "In priorityHandler - Building Table High with ${switchName} count: ${state.count}"
				    if((state.count >= 1) && (state.count <= 5)) state.pTempMap1S += "<tr><td>${switchName}</td><td><div style='color: ${tempHighColor};'>${tempStatusI}</div></td></tr>"
				    if((state.count >= 6) && (state.count <= 10)) state.pTempMap2S += "<tr><td>${switchName}</td><td><div style='color: ${tempHighColor};'>${tempStatusI}</div></td></tr>"
                    state.tempTooHighPushMap += "${switchName} \n"
                }
			}
        }
	
		if(tempMode == "Only Low") {
			temps.each { it ->
                switchName = it.displayName
	            tempStatus = it.currentValue("temperature")
                tempStatusI = tempStatus.toFloat()
	            tempHighI = tempHigh.toFloat()
	            tempLowI = tempLow.toFloat()
                if(logEnable) log.debug "In tempMapHandler - Low - Working on: ${switchName} - ${tempStatus} - tempLow: ${tempLowI}" 
                if(tempStatusI <= tempLowI) {
				    state.count = state.count + 1
				    state.isPriorityData = "true"
                    state.priorityTempLow = "true"
                    state.tempTooLowMap += "${switchName}, "
				    if(logEnable) log.debug "In priorityHandler - Building Table Low with ${switchName} count: ${state.count}"
				    if((state.count >= 1) && (state.count <= 5)) state.pTempMap1S += "<tr><td>${switchName}</td><td><div style='color: ${tempLowColor};'>${tempStatusI}</div></td></tr>"
				    if((state.count >= 6) && (state.count <= 10)) state.pTempMap2S += "<tr><td>${switchName}</td><td><div style='color: ${tempLowColor};'>${tempStatusI}</div></td></tr>"
                    state.tempTooLowPushMap += "${switchName} \n"
                }
			}
		}
	
		state.pTempMap1S += "</table>"
		state.pTempMap2S += "</table>"
	
		if(state.count == 0) {
			state.pTempMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No temp devices to report</div></td></tr></table>"
			state.pTempMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No temp devices to report</div></td></tr></table>"
			state.isPriorityData = "false"
            state.priorityTempHigh = "false"
            state.priorityTempLow = "false"
		}
        if(snapshotTileDevice) {
		    snapshotTileDevice.sendSnapshotPriorityTempMap1(state.pTempMap1S)
		    snapshotTileDevice.sendSnapshotPriorityTempMap2(state.pTempMap2S)
        } else {
            log.warn "Snapshot - NO DEVICE choosen to send data to"   
        }
	}
	
	if((isDataDevice) && (state.isPriorityData == "true")) isDataDevice.on()
	if((isDataDevice) && (state.isPriorityData == "false")) isDataDevice.off()
    
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
	
	state.theMsg = "${state.preMsgR}, "
	if(state.prioritySwitch == "true") state.theMsg += " ${state.wrongStateSwitchMap}"
	if(state.priorityContact == "true") state.theMsg += " ${state.wrongStateContactMap}"
	if(state.priorityLock == "true") state.theMsg += " ${state.wrongStateLockMap}"
	state.theMsg += ". ${state.postMsgR}"
	if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
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
    if(state.priorityTempHigh == "true") {
		state.tempTooHighPushMap2 = "PRIORITY TEMPS TOO HIGH \n"
		state.tempTooHighPushMap2 += "${state.tempTooHighPushMap} \n"
		theMsg += "${state.tempTooHighPushMap2} \n"
	}
    if(state.priorityTempLow == "true") {
		state.tempTooLowPushMap2 = "PRIORITY TEMPS TOO LOW \n"
		state.tempTooLowPushMap2 += "${state.tempTooLowPushMap} \n"
		theMsg += "${state.tempTooLowPushMap2} \n"
	}
	pushMessage = "${theMsg}"
    if(theMsg) sendPushMessage.deviceNotification(pushMessage)
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable) log.debug "In setDefaults..."
	if(priorityCheckSwitch == null){priorityCheckSwitch = "off"}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
		paragraph getFormat("line")
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
	}       
}

def getHeaderAndFooter() {
    if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
    def params = [
	    uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 30
	]
    
    try {
        def result = null
        httpGet(params) { resp ->
            state.headerMessage = resp.data.headerMessage
            state.footerMessage = resp.data.footerMessage
        }
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
