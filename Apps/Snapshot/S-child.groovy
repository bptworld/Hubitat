/**
 *  ****************  Snapshot Child  ****************
 *
 *  Design Usage:
 *  Monitor devices and sensors. Easily see their status right on your dashboard and/or get a notification - speech and phone.
 *
 *  Copyright 2019-2022 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  2.0.5 - 05/08/22 - Major rewrite
 *  2.0.4 - 03/16/21 - Fixed presence, other adjustments
 *  2.0.3 - 01/03/21 - Fix real-time handler names
 *  2.0.2 - 04/27/20 - Cosmetic changes
 *  2.0.1 - 09/01/19 - Added custom color options
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  --
 *  V1.0.0 - 03/16/19 - Initial Release
 *
 */

#include BPTWorld.bpt-normalStuff

def setVersion(){
    state.name = "Snapshot"
	state.version = "2.0.5"
    sendLocationEvent(name: "updateVersionInfo", value: "${state.name}:${state.version}")
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
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Snapshot/S-child.groovy",
)

preferences {
	page(name: "pageConfig")
    page(name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
	dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "Monitor devices and sensors. Easily see their status right on your dashboard and/or get a notification - speech and phone."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Type of Trigger")) {
			input "reportMode", "enum", required: true, title: "Select Report Type", submitOnChange: true,  options: ["Regular", "Priority"]
			if(reportMode == "Regular") paragraph "Regular - Will show device state based on options below"
			if(reportMode == "Priority") paragraph "Priority - Show device state only when it's not what it should be based on options below"
			input "triggerMode", "enum", required: true, title: "Select Trigger Frequency", submitOnChange: true,  options: ["Real Time", "Every X minutes", "On Demand"]
			if(triggerMode == "Real Time") {
				paragraph "<b>Remember - Too many devices updating in real time can slow down and/or crash the hub.</b>"
			}
			if(triggerMode == "Every X minutes") {
				paragraph "<b>Choose how often to take a Snapshot of your selected devices.</b>"
				input "timeDelay", "number", title: "Every X Minutes (1 to 60)", required: true, range: '1..60'
			}
			if(triggerMode == "On Demand") {
				paragraph "<b>Only take a snapshot when this switch is turned on.</b>"
				createDeviceSection("Snapshot Driver")
                paragraph "<small>Recommended to set 'Enable auto off' to '1s' after creating device</small>"
			}
            createDeviceSection("Snapshot Driver")
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Devices to Monitor")) {
			if(reportMode == "Priority") {
				input "switchesOn", "capability.switch", title: "Switches that should be ON", multiple: true, required: false, submitOnChange: true
				input "switchesOff", "capability.switch", title: "Switches that should be OFF", multiple: true, required: false, submitOnChange: true
				input "contactsOpen", "capability.contactSensor", title: "Contact Sensors that should be OPEN", multiple: true, required: false, submitOnChange: true
				input "contactsClosed", "capability.contactSensor", title: "Contact Sensors that should be CLOSED", multiple: true, required: false, submitOnChange: true
                input "motionActive", "capability.motion", title: "Motion Sensors that should be ACTIVE", multiple: true, required: false, submitOnChange: true
				input "motionInactive", "capability.motion", title: "Motion Sensors that should be INACTIVE", multiple: true, required: false, submitOnChange: true
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
                input "motion", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false, submitOnChange: true
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
                input "waterWetColor", "enum", required: true, title: "Color when water is wet", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "waterDryColor", "enum", required: true, title: "Color when water is dry", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(locks || locksLocked || lockUnlocked) {
                input "locksUnlockedColor", "enum", required: true, title: "Color when lock is unlocked", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "locksLockedColor", "enum", required: true, title: "Color when lock is locked", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
            }
            if(motion) {
                input "motionInactiveColor", "enum", required: true, title: "Color when motion is Inactive", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
                input "motionActiveColor", "enum", required: true, title: "Color when motion is Active", submitOnChange: true,  options: ["Black","Blue","Brown","Green","Grey","Navy","Orange","Purple","Red","White","Yellow"], width: 6
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
                if(motion) {
					input "motionMode", "enum", required: true, title: "Select Motion Display Output Type", submitOnChange: true,  options: ["Full", "Only Active", "Only Inactive"]
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
                if(fmSpeaker) {
                    href "speechOptions", title:"${getImage("optionsGreen")} Select Notification Options", description:"Click here for Options"
                } else {
                    href "speechOptions", title:"${getImage("optionsRed")} Select Notification Options", description:"Click here for Options"
                }
				paragraph "Each of the following messages will only be spoken if necessary"
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
				input "msgDevice", "text", required: true, title: "Message to speak when a device isn't in the correct state. Will be followed by device names", defaultValue: "The following devices are in the wrong state"
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
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph "All you have to do is add the device selected above to one of your dashboards to see your data on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Snapshot Tile<br>- Pick a template = attribute<br>- 3rd box = switch1-6, contact1-6, lock1-6, etc."
		}
		section() {
            paragraph "To save characters, enter in filters to remove characters from each device name. Must be exact, including case.<br><small>ie. Motion;Sensor;Contact</small>"
			input "dnFilter", "text", title: "Filters (separtate each with a ; (semicolon))", required:false, submitOnChange:true   
		}
                
		section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
        }
		display2()
	}
}

def speechOptions() {
    dynamicPage(name: "speechOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required:true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
            if(sendPushMessage) {
                input "includeAppName", "bool", title: "Include App name in Push", defaultValue:false, submitOnChange:true
            }
    	}
    }
}

def installed() {
    if(logEnable) log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
    initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In initialize (${state.version})"
        if(triggerMode == "On Demand") subscribe(dataDevice, "switch.on", onDemandSwitchHandler)
        if(triggerMode == "Every X minutes") subscribe(dataDevice, "switch", repeatSwitchHandler)
        if(triggerMode == "Real Time" && reportMode == "Regular") {
            if(switches) subscribe(switches, "switch", onDemandSwitchHandler)
            if(contacts) subscribe(contacts, "contact", onDemandSwitchHandler)
            if(water) subscribe(water, "water", onDemandSwitchHandler)
            if(locks) subscribe(locks, "lock", onDemandSwitchHandler)
            if(motion) subscribe(motion, "motion", onDemandSwitchHandler)
            if(presence) subscribe(presence, "presence", onDemandSwitchHandler)
            if(temps) subscribe(temps, "temperature", onDemandSwitchHandler)
        }
        if(triggerMode == "Real Time" && reportMode == "Priority") {
            if(switchesOn) subscribe(switchesOn, "switch", priorityHandler)
            if(switchesOff) subscribe(switchesOff, "switch", priorityHandler)
            if(contactsOpen) subscribe(contactsOpen, "contact", priorityHandler)
            if(contactsClosed) subscribe(contactsClosed, "contact", priorityHandler)
            if(motionInactive) subscribe(motionInactive, "motion.inactive", priorityHandler)
            if(motionActive) subscribe(motionActive, "motion.active", priorityHandler)
            if(locksUnlocked) subscribe(locksUnlocked, "lock", priorityHandler)
            if(locksLocked) subscribe(locksLocked, "lock", priorityHandler)
            if(temps) subscribe(temps, "temperature", priorityHandler)
        }
    }
}

def repeatSwitchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In repeatSwitchHandler (${state.version})"
        state.repeatSwitchStatus = dataDevice.currentValue("switch")
        state.runDelay = timeDelay * 60
        if(reportMode == "Regular") {
            if(state.repeatSwitchStatus == "on") {
                if(switches) mapHandler("switch")
                if(contacts) mapHandler("contact")
                if(water) mapHandler("water")
                if(locks) mapHandler("lock")
                if(motion) mapHandler("motion")
                if(presence) mapHandler("presence")
                if(temps) mapHandler("temperature")
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
}

def onDemandSwitchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In onDemandSwitchHandler (${state.version})"
        state.onDemandSwitchStatus = evt.value
        if(reportMode == "Regular") {
            if(switches) mapHandler("switch")
            if(contacts) mapHandler("contact")
            if(water) mapHandler("water")
            if(locks) mapHandler("lock")
            if(motion) mapHandler("motion")
            if(presence) mapHandler("presence")
            if(temps) mapHandler("temperature")
        }
        if(reportMode == "Priority") {
            priorityHandler()
        }
    }
}

def mapHandler(data) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setVersion()
        if(logEnable) log.debug "------------------------------- Start Map Handler (${data})-------------------------------"
        state.count = 0
        state.countOn = 0
        state.countOff = 0
        fMap1S="";fMap2S="";fMap3S="";fMap4S="";fMap5S="";fMap6S=""
        
        if(data == "switch") {
            theDevices = switches
            if(switchMode == "Full") { dMode = "full" }
            if(switchMode == "Only On") { dMode = "1" }
            if(switchMode == "Only Off") { dMode = "2" }
            onColor = switchesOnColor
            offColor = switchesOffColor
        }
        if(data == "contact") {
            theDevices = contacts
            if(contactMode == "Full") { dMode = "full" }
            if(contactMode == "Only Open") { dMode = "1" }
            if(contactMode == "Only Closed") { dMode = "2" }
            onColor = contactsOpenColor
            offColor = contactsClosedColor
        }
        if(data == "water") {
            theDevices = water
            if(waterMode == "Full") { dMode = "full" }
            if(waterMode == "Only Wet") { dMode = "1" }
            if(waterMode == "Only Dry") { dMode = "2" }
            onColor = waterWetColor
            offColor = waterDryColor
        }
        if(data == "lock") {
            theDevices = locks
            if(lockMode == "Full") { dMode = "full" }
            if(lockMode == "Only Unlocked") { dMode = "1" }
            if(lockMode == "Only Locked") { dMode = "2" }
            onColor = locksUnlockedColor
            offColor = locksLockedColor
        }
        if(data == "motion") {
            theDevices = motion
            if(motionMode == "Full") { dMode = "full" }
            if(motionMode == "Only Active") { dMode = "1" }
            if(motionMode == "Only Inactive") { dMode = "2" }
            onColor = motionActiveColor
            offColor = motionInactiveColor
        }
        if(data == "presence") {
            theDevices = presence
            if(presenceMode == "Full") { dMode = "full" }
            if(presenceMode == "Only Not Present") { dMode = "1" }
            if(presenceMode == "Only Present") { dMode = "2" }
            onColor = presenceNotPresentColor
            offColor = presencPresentfColor
        }
        if(data == "temperature") {
            theDevices = temps
            if(tempMode == "Full") { dMode = "full" }
            if(tempMode == "Only High") { dMode = "1" }
            if(tempMode == "Only Low") { dMode = "2" }
            onColor = tempHighColor
            offColor = tempLowColor
        }
        
        theDevices = theDevices.sort { it.displayName }
        filters = dnFilter.split(";")
        
        theDevices.each { it ->
            dName = it.displayName
            if(filters) {
                filters.each { filt ->
                    dName = dName.replace("${filt}", "")
                }
            }
           
            dStatus = it.currentValue(data)
            try {
                powerLvl = it.currentValue("power")
                if(powerLvl == null) powerLvl = ""
                if(logEnable) log.trace "Snapshot - device: ${it} - power: ${powerLvl}"
            } catch(e) {
                if(logEnable) log.trace "Snapshot - device: ${it} - Doesn't have power attribute"
                if(powerLvl == null) powerLvl = ""
            }
            if(data == "temperature") {
                tempStatusI = tempStatus.toFloat()
                tempHighI = tempHigh.toFloat()
                tempLowI = tempLow.toFloat()
                if(tempStatusI >= tempHighI) {
                    dStatus = "high"
                } else {
                    dStatus = "low"
                }
            }
            
            if(logEnable) log.debug "In mapHandler - Working on: ${dName} - ${dStatus}"
            if(dMode == "full" || dMode == "1") {
                if(dStatus == "on" || dStatus == "open" || dStatus == "wet" || dStatus == "unlocked" || dStatus == "active" || dStatus == "not present" || dStatus == "high") {  
                    state.count = state.count + 1
                    state.countOn = state.countOn + 1
                    if(logEnable) log.debug "In mapHandler - Building Table ${dStatus} with ${dName} count: ${state.count}"
                    if((state.count >= 1) && (state.count <= 5)) fMap1S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${onColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 6) && (state.count <= 10)) fMap2S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${onColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 11) && (state.count <= 15)) fMap3S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${onColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 16) && (state.count <= 20)) fMap4S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${onColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 21) && (state.count <= 25)) fMap5S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${onColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 26) && (state.count <= 30)) fMap6S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${onColor};'>${dStatus}</div></td></tr>"
                }
            }
            
            if(dMode == "full" || dMode == "2") {
                if(dStatus == "off" || dStatus == "closed" || dStatus == "dry" || dStatus == "locked" || dStatus == "inactive" || dStatus == "present" || dStatus == "low") {
                    state.count = state.count + 1
                    state.countOff = state.countOff + 1
                    if(logEnable) log.debug "In mapHandler - Building Table ${dStatus} with ${dName} count: ${state.count}"
                    if((state.count >= 1) && (state.count <= 5)) fMap1S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${offColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 6) && (state.count <= 10)) fMap2S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${offColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 11) && (state.count <= 15)) fMap3S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${offColor};'>${dStatus}</div></td></tr>"
                    if((state.count >= 16) && (state.count <= 20)) fMap4S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${offColor};'>${dStatus}</div></td></tr>"	
                    if((state.count >= 21) && (state.count <= 25)) fMap5S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${offColor};'>${dStatus}</div></td></tr>"	
                    if((state.count >= 26) && (state.count <= 30)) fMap6S += "<tr><td align=left>${dName}</td><td> ${powerLvl} </td><td><div style='color: ${offColor};'>${dStatus}</div></td></tr>"	
                }
            }
        }

        fMap1 = "<table width='100%'>${fMap1S}</table>"
        fMap2 = "<table width='100%'>${fMap2S}</table>"
        fMap3 = "<table width='100%'>${fMap3S}</table>"
        fMap4 = "<table width='100%'>${fMap4S}</table>"
        fMap5 = "<table width='100%'>${fMap5S}</table>"
        fMap6 = "<table width='100%'>${fMap6S}</table>"
            
        if(state.count == 0) {
            fMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
            fMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
            fMap3S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
            fMap4S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
            fMap5S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
            fMap6S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No devices to report</div></td></tr></table>"
        }

        if(logEnable) log.debug "In mapHandler - <br>fMap1S<br>${fMap1S}"
        if(dataDevice) {
            dataDevice.sendEvent(name: "${data}Map1", value: fMap1, isStateChange: true)
            dataDevice.sendEvent(name: "${data}Map2", value: fMap2, isStateChange: true)
            dataDevice.sendEvent(name: "${data}Map3", value: fMap3, isStateChange: true)
            dataDevice.sendEvent(name: "${data}Map4", value: fMap4, isStateChange: true)
            dataDevice.sendEvent(name: "${data}Map5", value: fMap5, isStateChange: true)
            dataDevice.sendEvent(name: "${data}Map6", value: fMap6, isStateChange: true)
            dataDevice.sendEvent(name: "${data}CountOn", value: state.countOn, isStateChange: true)
            dataDevice.sendEvent(name: "${data}CountOff", value: state.countOff, isStateChange: true)
        } else {
            log.warn "Snapshot - NO DEVICE choosen to send data to"   
        }
        if(logEnable) log.debug "------------------------------- End Map Handler -------------------------------"
    }
}


// ***** Priority Stuff *****

def priorityHandler(evt){
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setVersion()
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
            if(dataDevice) {
                dataDevice.sendEvent(name: "prioritySwitchMap1", value: state.pSwitchMap1S, isStateChange: true)
                dataDevice.sendEvent(name: "prioritySwitchMap2", value: state.pSwitchMap2S, isStateChange: true)
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
                        if((state.count >= 1) && (state.count <= 5)) state.pContactMap1S += "<tr><td><div style='color: ${contactsClosedColor};'>${switchName}</div></td><td><div style='color: ${contactsClosedColor};'>${switchStatus}</div></td></tr>"
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
            if(dataDevice) {
                dataDevice.sendEvent(name: "priorityContactMap1", value: state.pContactMap1S, isStateChange: true)
                dataDevice.sendEvent(name: "priorityContactMap2", value: state.pContactMap2S, isStateChange: true)
            } else {
                log.warn "Snapshot - NO DEVICE choosen to send data to"   
            }
        }

        // Start Priority Motion
        if(motionActive || motionInactive) {
            if(logEnable) log.debug "In priorityHandler - motion"
            state.pMotionMap1S = "<table width='100%'>"
            state.pMotionMap2S = "<table width='100%'>"
            state.count = 0
            if(motionInactive) {
                motionInactive.each { sOn -> 
                    def switchName = sOn.displayName
                    def switchStatus = sOn.currentValue('motion')
                    if(switchStatus == "active") {
                        state.count = state.count + 1
                        state.isPriorityData = "true"
                        state.priorityMotion = "true"
                        state.wrongStateMotionMap += "${switchName}, "
                        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Motion with ${switchName} count: ${state.count}"
                        if((state.count >= 1) && (state.count <= 5)) state.pMotionMap1S += "<tr><td><div style='color: ${motionActiveColor};'>${switchName}</div></td><td><div style='color: ${motionActiveColor};'>${switchStatus}</div></td></tr>"
                        if((state.count >= 6) && (state.count <= 10)) state.pMotionMap2S += "<tr><td><div style='color: ${motionActiveColor};'>${switchName}</div></td><td><div style='color: ${motionActiveColor};'>${switchStatus}</div></td></tr>"
                        state.wrongMotionPushMap += "${switchName} \n"
                    }
                }
            }

            if(motionActive) {
                motionActive.each { sOn -> 
                    def switchName = sOn.displayName
                    def switchStatus = sOn.currentValue('motion')
                    if(switchStatus == "inactive") {
                        state.count = state.count + 1
                        state.isPriorityData = "true"
                        state.priorityMotion = "true"
                        state.wrongStateMotionMap += "${switchName}, "
                        if(logEnable) log.debug "In priorityHandler - Building Table Wrong Motion with ${switchName} count: ${state.count}"
                        if((state.count >= 1) && (state.count <= 5)) state.pMotionMap1S += "<tr><td><div style='color: ${motionInactiveColor};'>${switchName}</div></td><td><div style='color: ${motionInactiveColor};'>${switchStatus}</div></td></tr>"
                        if((state.count >= 6) && (state.count <= 10)) state.pMotionMap2S += "<tr><td><div style='color: ${motionInactiveColor};'>${switchName}</div></td><td><div style='color: ${motionInactiveColor};'>${switchStatus}</div></td></tr>"
                        state.wrongMotionPushMap += "${switchName} \n"
                    }
                }
            }

            state.pMotionMap1S += "</table>"
            state.pMotionMap2S += "</table>"

            if(state.count == 0) {
                state.pMotionMap1S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No motion to report</div></td></tr></table>"
                state.pMotionMap2S = "<table width='100%'><tr><td><div style='color: ${textNoColor};'>No motion to report</div></td></tr></table>"
                state.wrongMotionPushMap = ""
                state.isPriorityData = "false"
                state.priorityMotion = "false"
            }
            if(dataDevice) {
                dataDevice.sendEvent(name: "priorityMotionMap1", value: state.pMotionMap1S, isStateChange: true)
                dataDevice.sendEvent(name: "priorityMotionMap2", value: state.pMotionMap2S, isStateChange: true)
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
            if(dataDevice) {
                dataDevice.sendEvent(name: "priorityLockMap1", value: state.pLockMap1S, isStateChange: true)
                dataDevice.sendEvent(name: "priorityLockMap2", value: state.pLockMap2S, isStateChange: true)
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
            if(dataDevice) {
                dataDevice.sendEvent(name: "priorityTempMap1", value: state.pTempMap1S, isStateChange: true)
                dataDevice.sendEvent(name: "priorityTempMap2", value: state.pTempMap2S, isStateChange: true)
            } else {
                log.warn "Snapshot - NO DEVICE choosen to send data to"   
            }
        }

        if((isDataDevice) && (state.isPriorityData == "true")) isDataDevice.on()
        if((isDataDevice) && (state.isPriorityData == "false")) isDataDevice.off()

        messageHandler()
        if(fmSpeaker) letsTalk(state.theMsg)
        if(sendPushMessage) pushNow()
    }
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    state.theMsg = null
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
	if(logEnable) log.debug "In pushNow (${state.version})"
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
