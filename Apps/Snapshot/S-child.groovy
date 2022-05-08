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

// ~~~~~ start include (2) BPTWorld.bpt-normalStuff ~~~~~
library ( // library marker BPTWorld.bpt-normalStuff, line 1
        base: "app", // library marker BPTWorld.bpt-normalStuff, line 2
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-normalStuff, line 3
        category: "Apps", // library marker BPTWorld.bpt-normalStuff, line 4
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-normalStuff, line 5
        name: "bpt-normalStuff", // library marker BPTWorld.bpt-normalStuff, line 6
        namespace: "BPTWorld", // library marker BPTWorld.bpt-normalStuff, line 7
        documentationLink: "", // library marker BPTWorld.bpt-normalStuff, line 8
        version: "1.0.0", // library marker BPTWorld.bpt-normalStuff, line 9
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-normalStuff, line 10
) // library marker BPTWorld.bpt-normalStuff, line 11

import groovy.json.* // library marker BPTWorld.bpt-normalStuff, line 13
import hubitat.helper.RMUtils // library marker BPTWorld.bpt-normalStuff, line 14
import java.util.TimeZone // library marker BPTWorld.bpt-normalStuff, line 15
import groovy.transform.Field // library marker BPTWorld.bpt-normalStuff, line 16
import groovy.time.TimeCategory // library marker BPTWorld.bpt-normalStuff, line 17
import java.text.SimpleDateFormat // library marker BPTWorld.bpt-normalStuff, line 18

def checkHubVersion() { // library marker BPTWorld.bpt-normalStuff, line 20
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-normalStuff, line 21
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-normalStuff, line 22
    if(logEnable) log.debug "In checkHubVersion - Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-normalStuff, line 23
} // library marker BPTWorld.bpt-normalStuff, line 24

def parentCheck(){   // library marker BPTWorld.bpt-normalStuff, line 26
	state.appInstalled = app.getInstallationState()  // library marker BPTWorld.bpt-normalStuff, line 27
	if(state.appInstalled != 'COMPLETE'){ // library marker BPTWorld.bpt-normalStuff, line 28
		parentChild = true // library marker BPTWorld.bpt-normalStuff, line 29
  	} else { // library marker BPTWorld.bpt-normalStuff, line 30
    	parentChild = false // library marker BPTWorld.bpt-normalStuff, line 31
  	} // library marker BPTWorld.bpt-normalStuff, line 32
} // library marker BPTWorld.bpt-normalStuff, line 33

def createDeviceSection(driverName) { // library marker BPTWorld.bpt-normalStuff, line 35
    paragraph "This child app needs a virtual device to store values." // library marker BPTWorld.bpt-normalStuff, line 36
    input "useExistingDevice", "bool", title: "Use existing device (off) or have one created for you (on)", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 37
    if(useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 38
        input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Front Door')", required:true, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 39
        paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>" // library marker BPTWorld.bpt-normalStuff, line 40
        if(dataName) createDataChildDevice(driverName) // library marker BPTWorld.bpt-normalStuff, line 41
        if(statusMessageD == null) statusMessageD = "Waiting on status message..." // library marker BPTWorld.bpt-normalStuff, line 42
        paragraph "${statusMessageD}" // library marker BPTWorld.bpt-normalStuff, line 43
    } // library marker BPTWorld.bpt-normalStuff, line 44
    input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false // library marker BPTWorld.bpt-normalStuff, line 45
    if(!useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 46
        app.removeSetting("dataName") // library marker BPTWorld.bpt-normalStuff, line 47
        paragraph "<small>* Device must use the '${driverName}'.</small>" // library marker BPTWorld.bpt-normalStuff, line 48
    } // library marker BPTWorld.bpt-normalStuff, line 49
} // library marker BPTWorld.bpt-normalStuff, line 50

def createDataChildDevice(driverName) {     // library marker BPTWorld.bpt-normalStuff, line 52
    if(logEnable) log.debug "In createDataChildDevice (${state.version})" // library marker BPTWorld.bpt-normalStuff, line 53
    statusMessageD = "" // library marker BPTWorld.bpt-normalStuff, line 54
    if(!getChildDevice(dataName)) { // library marker BPTWorld.bpt-normalStuff, line 55
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}" // library marker BPTWorld.bpt-normalStuff, line 56
        try { // library marker BPTWorld.bpt-normalStuff, line 57
            addChildDevice("BPTWorld", driverName, dataName, 1234, ["name": "${dataName}", isComponent: false]) // library marker BPTWorld.bpt-normalStuff, line 58
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})" // library marker BPTWorld.bpt-normalStuff, line 59
            statusMessageD = "<b>Device has been been created. (${dataName})</b>" // library marker BPTWorld.bpt-normalStuff, line 60
        } catch (e) { if(logEnable) log.debug "Unable to create device - ${e}" } // library marker BPTWorld.bpt-normalStuff, line 61
    } else { // library marker BPTWorld.bpt-normalStuff, line 62
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>" // library marker BPTWorld.bpt-normalStuff, line 63
    } // library marker BPTWorld.bpt-normalStuff, line 64
    return statusMessageD // library marker BPTWorld.bpt-normalStuff, line 65
} // library marker BPTWorld.bpt-normalStuff, line 66

def uninstalled() { // library marker BPTWorld.bpt-normalStuff, line 68
    sendLocationEvent(name: "updateVersionInfo", value: "${app.id}:remove") // library marker BPTWorld.bpt-normalStuff, line 69
	removeChildDevices(getChildDevices()) // library marker BPTWorld.bpt-normalStuff, line 70
} // library marker BPTWorld.bpt-normalStuff, line 71

private removeChildDevices(delete) { // library marker BPTWorld.bpt-normalStuff, line 73
	delete.each {deleteChildDevice(it.deviceNetworkId)} // library marker BPTWorld.bpt-normalStuff, line 74
} // library marker BPTWorld.bpt-normalStuff, line 75

def letsTalk(msg) { // library marker BPTWorld.bpt-normalStuff, line 77
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 78
    if(useSpeech && fmSpeaker) { // library marker BPTWorld.bpt-normalStuff, line 79
        fmSpeaker.latestMessageFrom(state.name) // library marker BPTWorld.bpt-normalStuff, line 80
        fmSpeaker.speak(msg,null) // library marker BPTWorld.bpt-normalStuff, line 81
    } // library marker BPTWorld.bpt-normalStuff, line 82
} // library marker BPTWorld.bpt-normalStuff, line 83

def pushHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 85
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 86
    theMessage = "${app.label} - ${msg}" // library marker BPTWorld.bpt-normalStuff, line 87
    if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}" // library marker BPTWorld.bpt-normalStuff, line 88
    sendPushMessage.deviceNotification(theMessage) // library marker BPTWorld.bpt-normalStuff, line 89
} // library marker BPTWorld.bpt-normalStuff, line 90

def useWebOSHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 92
    if(logEnable) log.debug "In useWebOSHandler (${state.version}) - Sending to webOS - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 93
    useWebOS.deviceNotification(msg) // library marker BPTWorld.bpt-normalStuff, line 94
} // library marker BPTWorld.bpt-normalStuff, line 95

// ********** Normal Stuff ********** // library marker BPTWorld.bpt-normalStuff, line 97
def logsOff() { // library marker BPTWorld.bpt-normalStuff, line 98
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-normalStuff, line 99
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-normalStuff, line 100
} // library marker BPTWorld.bpt-normalStuff, line 101

def checkEnableHandler() { // library marker BPTWorld.bpt-normalStuff, line 103
    state.eSwitch = false // library marker BPTWorld.bpt-normalStuff, line 104
    if(disableSwitch) {  // library marker BPTWorld.bpt-normalStuff, line 105
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-normalStuff, line 106
        disableSwitch.each { it -> // library marker BPTWorld.bpt-normalStuff, line 107
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-normalStuff, line 108
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-normalStuff, line 109
        } // library marker BPTWorld.bpt-normalStuff, line 110
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-normalStuff, line 111
    } // library marker BPTWorld.bpt-normalStuff, line 112
} // library marker BPTWorld.bpt-normalStuff, line 113

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 115
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-normalStuff, line 116
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-normalStuff, line 117
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 118
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 119
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 120
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 121
    if(type == "logo") return "${loc}logo.png height=60>" // library marker BPTWorld.bpt-normalStuff, line 122
} // library marker BPTWorld.bpt-normalStuff, line 123

def getFormat(type, myText="") {			// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 125
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>" // library marker BPTWorld.bpt-normalStuff, line 126
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>" // library marker BPTWorld.bpt-normalStuff, line 127
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-normalStuff, line 128
} // library marker BPTWorld.bpt-normalStuff, line 129

def display(data) { // library marker BPTWorld.bpt-normalStuff, line 131
    if(data == null) data = "" // library marker BPTWorld.bpt-normalStuff, line 132
    setVersion() // library marker BPTWorld.bpt-normalStuff, line 133
    getHeaderAndFooter() // library marker BPTWorld.bpt-normalStuff, line 134
    if(app.label) { // library marker BPTWorld.bpt-normalStuff, line 135
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-normalStuff, line 136
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-normalStuff, line 137
        } else { // library marker BPTWorld.bpt-normalStuff, line 138
            theName = app.label // library marker BPTWorld.bpt-normalStuff, line 139
        } // library marker BPTWorld.bpt-normalStuff, line 140
    } // library marker BPTWorld.bpt-normalStuff, line 141
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-normalStuff, line 142
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) { // library marker BPTWorld.bpt-normalStuff, line 143
        paragraph "${state.headerMessage}" // library marker BPTWorld.bpt-normalStuff, line 144
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 145
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 146
    } // library marker BPTWorld.bpt-normalStuff, line 147
} // library marker BPTWorld.bpt-normalStuff, line 148

def display2() { // library marker BPTWorld.bpt-normalStuff, line 150
    section() { // library marker BPTWorld.bpt-normalStuff, line 151
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" } // library marker BPTWorld.bpt-normalStuff, line 152
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 153
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>" // library marker BPTWorld.bpt-normalStuff, line 154
        paragraph "${state.footerMessage}" // library marker BPTWorld.bpt-normalStuff, line 155
    } // library marker BPTWorld.bpt-normalStuff, line 156
} // library marker BPTWorld.bpt-normalStuff, line 157

def getHeaderAndFooter() { // library marker BPTWorld.bpt-normalStuff, line 159
    timeSinceNewHeaders() // library marker BPTWorld.bpt-normalStuff, line 160
    if(state.checkNow == null) state.checkNow = true // library marker BPTWorld.bpt-normalStuff, line 161
    if(state.totalHours > 6 || state.checkNow) { // library marker BPTWorld.bpt-normalStuff, line 162
        def params = [ // library marker BPTWorld.bpt-normalStuff, line 163
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json", // library marker BPTWorld.bpt-normalStuff, line 164
            requestContentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 165
            contentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 166
            timeout: 10 // library marker BPTWorld.bpt-normalStuff, line 167
        ] // library marker BPTWorld.bpt-normalStuff, line 168
        try { // library marker BPTWorld.bpt-normalStuff, line 169
            def result = null // library marker BPTWorld.bpt-normalStuff, line 170
            httpGet(params) { resp -> // library marker BPTWorld.bpt-normalStuff, line 171
                state.headerMessage = resp.data.headerMessage // library marker BPTWorld.bpt-normalStuff, line 172
                state.footerMessage = resp.data.footerMessage // library marker BPTWorld.bpt-normalStuff, line 173
            } // library marker BPTWorld.bpt-normalStuff, line 174
        } catch (e) { } // library marker BPTWorld.bpt-normalStuff, line 175
    } // library marker BPTWorld.bpt-normalStuff, line 176
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>" // library marker BPTWorld.bpt-normalStuff, line 177
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>" // library marker BPTWorld.bpt-normalStuff, line 178
} // library marker BPTWorld.bpt-normalStuff, line 179

def timeSinceNewHeaders() {  // library marker BPTWorld.bpt-normalStuff, line 181
    if(state.previous == null) {  // library marker BPTWorld.bpt-normalStuff, line 182
        prev = new Date() // library marker BPTWorld.bpt-normalStuff, line 183
    } else { // library marker BPTWorld.bpt-normalStuff, line 184
        try { // library marker BPTWorld.bpt-normalStuff, line 185
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // library marker BPTWorld.bpt-normalStuff, line 186
            prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000")) // library marker BPTWorld.bpt-normalStuff, line 187
        } catch(e) { // library marker BPTWorld.bpt-normalStuff, line 188
            prev = state.previous // library marker BPTWorld.bpt-normalStuff, line 189
        } // library marker BPTWorld.bpt-normalStuff, line 190
    } // library marker BPTWorld.bpt-normalStuff, line 191
    def now = new Date() // library marker BPTWorld.bpt-normalStuff, line 192
    use(TimeCategory) { // library marker BPTWorld.bpt-normalStuff, line 193
        state.dur = now - prev // library marker BPTWorld.bpt-normalStuff, line 194
        state.days = state.dur.days // library marker BPTWorld.bpt-normalStuff, line 195
        state.hours = state.dur.hours // library marker BPTWorld.bpt-normalStuff, line 196
        state.totalHours = (state.days * 24) + state.hours // library marker BPTWorld.bpt-normalStuff, line 197
    } // library marker BPTWorld.bpt-normalStuff, line 198
    state.previous = now // library marker BPTWorld.bpt-normalStuff, line 199
} // library marker BPTWorld.bpt-normalStuff, line 200

// ~~~~~ end include (2) BPTWorld.bpt-normalStuff ~~~~~
