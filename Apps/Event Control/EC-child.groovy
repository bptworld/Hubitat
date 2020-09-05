/**
 *  ****************  Event Control Child App  ****************
 *
 *  Design Usage:
 *  Automatically control devices and events using multiple triggers!
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
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
 *  1.0.0 - 09/05/20 - Initial release.
 *
 */

import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Event Control"
	state.version = "1.0.0"
}

definition(
    name: "Event Control Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automatically control devices and events using multiple triggers!",
    category: "Convenience",
	parent: "BPTWorld:Event Control",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
    page name: "notificationOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Automatically control devices and events using multiple triggers!"
		}
		
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Triggers")) {
            input "triggerType", "enum", title: "Trigger Type (Each trigger will be considered an 'AND')", options: [
                ["xDays":"Time/Days"],
                ["xContact":"Contact Sensors"],
                ["xHumidity":"Humidity Setpoint"],
                ["xMode":"Mode"],
                ["xMotion":"Motion Sensors"],
                ["xPower":"Power Setpoint"],
                ["xSwitch":"Switches"],
                ["xTemp":"Temperature Setpoint"]
            ], required: true, multiple:true, submitOnChange:true
            
            paragraph "<hr>"
            if(triggerType == null) triggerType = ""
            
            if(triggerType.contains("xDays")) {
                paragraph "<b>Day/Time</b>"
                input "days", "enum", title: "Activate on these days", description: "Days to Activate", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

                if(!sunRestriction) {
                    input "betweenTimes", "bool", title: "Between two times?", description: "between times", defaultValue:false, submitOnChange:true, width:12
                } else {
                    app?.updateSetting("betweenTimes",[value:"false",type:"bool"])
                }
                
                if(!betweenTimes) {
                    input "sunRestriction", "bool", title: "Use Sunset or Sunrise?", description: "sun", defaultValue:false, submitOnChange:true, width:12
                } else {
                    app?.updateSetting("sunRestriction",[value:"false",type:"bool"])
                }
                
                if(betweenTimes) {
                    input "fromTime", "time", title: "From", required: false, width: 6, submitOnChange:true
                    input "toTime", "time", title: "To", required: false, width: 6
                    input "midnightCheckR", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                } else {
                    app.removeSetting("fromTime")
                    app.removeSetting("toTime")
                    app.removeSetting("midnightCheckR")
                }
             
                if(sunRestriction) { 
                    input "riseSet", "bool", title: "Sunrise (off) or Sunset (on)", description: "sun", defaultValue:false, submitOnChange:true, width:12
                    app.removeSetting("startTime")
                    if(riseSet) {
                        paragraph "<b>Sunset Offset</b>"
                        input "setBeforeAfter", "bool", title: "Before (off) or After (on)", defaultValue:false, submitOnChange:true, width:6
                        input "offsetSunset", "number", title: "Offset (minutes)", width:6
                        app.removeSetting("offsetSunrise") 
                    } else { 
                        paragraph "<b>Sunrise Offset</b>"
                        input "riseBeforeAfter", "bool", title: "Before (off) or After (on)", defaultValue:false, submitOnChange:true, width:6
                        input "offsetSunrise", "number", title: "Offset (minutes)", width:6
                        app.removeSetting("offsetSunset")
                    }
                } else {
                    app.removeSetting("offsetSunrise")
                    app.removeSetting("offsetSunset")
                    app.removeSetting("sunRestriction")
                }
                
                if(!betweenTimes && !sunRestriction) {
                    input "startTime", "time", title: "Time to activate", description: "Time", required: false, width:12
                    input "repeat", "bool", title: "Repeat?", description: "Repeat", defaultValue:false, submitOnChange:true
                    if(repeat) {
                        input "repeatType", "enum", title: "Repeat schedule", options: [
                            ["r1min":"1 Minute"],
                            ["r5min":"5 Minutes"],
                            ["r10min":"10 Minutes"],
                            ["r15min":"15 Minutes"],
                            ["r30min":"30 Minutes"],
                            ["r1hour":"1 Hour"],
                            ["r3hour":"3 Hours"]
                        ], required:true, multiple:true, submitOnChange:true
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("startTime")
                app.removeSetting("repeat")
                app.removeSetting("repeatType")
            }
            
            if(triggerType.contains("xContact")) {
                paragraph "<b>Contact</b>"
                input "contactEvent", "capability.contactSensor", title: "By Contact Sensor", required: false, multiple: true, submitOnChange: true
                if(contactEvent) {
                    input "csOpenClosed", "bool", defaultValue:false, title: "<b>Trigger when Closed or Opened? (off=Closed, on=Open)</b>", description: "Contact status", submitOnChange:true
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("contactEvent")
            }

            if(triggerType.contains("xHumidity")) {
                paragraph "<b>Humidity</b>"
                input "humidityEvent", "capability.relativeHumidityMeasurement", title: "By Humidity Setpoints", required:false, multiple:true, submitOnChange:true
                if(humidityEvent) {
                    input "oSetPointHigh", "bool", defaultValue:false, title: "<b>Trigger when Humidity is too High?</b>", description: "Humidity High", submitOnChange:true
                    if(oSetPointHigh) input "setPointHigh", "number", title: "Humidity High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    input "oSetPointLow", "bool", defaultValue:false, title: "<b>Trigger when Humidity is too Low?</b>", description: "Humidity Low", submitOnChange:true
                    if(oSetPointLow) input "setPointLow", "number", title: "Humidity Low Setpoint", required:true, defaultValue: 30, submitOnChange:true

                    if(oSetPointHigh) paragraph "You will receive notifications if Humidity reading is above ${setPointHigh}"
                    if(oSetPointLow) paragraph "You will receive notifications if Humidity reading is below ${setPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("humidityEvent")
            }

            if(triggerType.contains("xMode")) {
                paragraph "<b>Mode</b>"
                input "modeEvent", "mode", title: "By Mode", multiple:true, submitOnChange:true
                if(modeEvent) {
                    input "modeOnOff", "bool", defaultValue: false, title: "<b>Mode Inactive or Active? (off=Inactive, on=Active)</b>", description: "Mode status", submitOnChange:true
                    if(modeOnOff) paragraph "You will receive notifications if any of the modes are on."
                    if(!modeOnOff) paragraph "You will receive notifications if any of the modes are off."
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("modeEvent")
            }
            
            if(triggerType.contains("xMotion")) {
                paragraph "<b>Motion</b>"
                input "motionEvent", "capability.motionSensor", title: "By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionEvent) {
                    input "meOnOff", "bool", defaultValue:false, title: "<b>Motion Inactive or Active? (off=Inactive, on=Active)</b>", description: "Motion status", submitOnChange:true
                    if(meOnOff) paragraph "You will receive notifications if any of the sensors are on."
                    if(!meOnOff) paragraph "You will receive notifications if any of the sensors are off."
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("motionEvent")
            }

            if(triggerType.contains("xPower")) {
                paragraph "<b>Power</b>"
                input "powerEvent", "capability.powerMeter", title: "By Power Setpoints", required:false, multiple:true, submitOnChange:true
                if(powerEvent) {
                    input "oSetPointHigh", "bool", defaultValue: "false", title: "<b>Trigger when Power is too High?</b>", description: "Power High", submitOnChange:true
                    if(oSetPointHigh) input "setPointHigh", "number", title: "Power High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    input "oSetPointLow", "bool", defaultValue:false, title: "<b>Trigger when Power is too Low?</b>", description: "Power Low", submitOnChange:true
                    if(oSetPointLow) input "setPointLow", "number", title: "Power Low Setpoint", required: true, defaultValue: 30, submitOnChange: true

                    if(oSetPointHigh) paragraph "You will receive notifications if Power reading is above ${setPointHigh}"
                    if(oSetPointLow) paragraph "You will receive notifications if Power reading is below ${setPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("powerEvent")
            }

            if(triggerType.contains("xSwitch")) {
                paragraph "<b>Switch</b>"
                input "switchEvent", "capability.switch", title: "By Switch", required:false, multiple:true, submitOnChange:true
                if(switchEvent) {
                    input "seOnOff", "bool", defaultValue:false, title: "<b>Switch Off or On? (off=Off, on=On)</b>", description: "Switch status", submitOnChange:true
                    if(seOnOff) paragraph "You will receive notifications if any of the switches are on."
                    if(!seOnOff) paragraph "You will receive notifications if any of the switches are off."
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchEvent")
            }

            if(triggerType.contains("xTemp")) {
                paragraph "<b>Temperature</b>"
                input "tempEvent", "capability.temperatureMeasurement", title: "By Temperature Setpoints", required:false, multiple:true, submitOnChange:true
                if(tempEvent) {
                    input "oSetPointHigh", "bool", defaultValue:false, title: "<b>Trigger when Temperature is too High?</b>", description: "Temp High", submitOnChange:true
                    if(oSetPointHigh) input "setPointHigh", "number", title: "Temperature High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    input "oSetPointLow", "bool", defaultValue:false, title: "<b>Trigger when Temperature is too Low?</b>", description: "Temp Low", submitOnChange:true
                    if(oSetPointLow) input "setPointLow", "number", title: "Temperature Low Setpoint", required: true, defaultValue: 30, submitOnChange: true

                    if(oSetPointHigh) paragraph "You will receive notifications if Temperature reading is above ${setPointHigh}"
                    if(oSetPointLow) paragraph "You will receive notifications if Temperature reading is below ${setPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("tempEvent")
            }
            
            if(contactEvent || humidityEvent || modeEvent || motionEvent || powerEvent || switchEvent || tempEvent) {
                input "setDelay", "bool", defaultValue:false, title: "<b>Set Delay?</b>", description: "Delay Time", submitOnChange:true
                if(setDelay) {
                    paragraph "Delay the notifications until all devices has been in state for XX minutes."
                    input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Actions")) {
            input "actionType", "enum", title: "Actions to Perform", options: [
                ["aHSM":"Hubitat Safety Monitor"],
                ["aNotification":"Notifications (speech/push/flash)"],
                ["aMode":"Mode"],
                ["aRefresh":"Refresh"],
                ["aRule":"Rule Machine"],
                ["aSwitch":"Switches"]
            ], required:true, multiple:true, submitOnChange:true
            
            paragraph "<hr>"
            if(actionType == null) actionType = " "
            
            if(actionType.contains("aMode")) {
                paragraph "<b>Mode</b>"
                input "modeAction", "mode", title: "Change Mode to", multiple:false, submitOnChange:true
                paragraph "<hr>"
            } else {
                app.removeSetting("modeAction")
            }
            
            if(actionType.contains("aRefresh")) {
                paragraph "<b>Refresh Device</b><br><small>* Only works for devices that have the 'refresh' attribute.</small>"
                input "devicesToRefresh", "capability.refresh", title: "Devices to Refresh", multiple:true, submitOnChange:true               
            }
            
            if(actionType.contains("aRule")) {
                paragraph "<b>Rule Machine</b>"
                def rules = RMUtils.getRuleList()
                if(rules) {
                    input "rmRule", "enum", title: "Select rules", required: false, multiple: true, options: rules, submitOnChange:true
                    if(rmRule) {
                        input "rmAction", "enum", title: "Action", required: false, multiple: false, options: [
                            ["runRuleAct":"Run"],
                            ["stopRuleAct":"Stop"],
                            ["pauseRule":"Pause"],
                            ["resumeRule":"Resume"],
                            ["runRule":"Evaluate"],
                            ["setRuleBooleanTrue":"Set Boolean True"],
                            ["setRuleBooleanFalse":"Set Boolean False"]
                        ]
                    }
                } else {
                    paragraph "No active rules found."
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("rmRule")
            }
            
            if(actionType.contains("aHSM")) {
                paragraph "<b>HSM</b>"
                input "setHSM", "enum", title: "Set HSM state", required: false, multiple:false, options: [
                    ["armAway":"Arm Away"],
                    ["armHome":"Arm Home"],
                    ["armNight":"Arm Night"],
                    ["disarm":"Disarm"],
                    ["disarmAll":"Disarm All"],
                    ["armAll":"Arm Monitor Rules"],
                    ["CancelAlerts":"Cancel Alerts"]
                ]
                paragraph "<hr>"
            } else {
                app.removeSetting("setHSM")
            }
            
            if(actionType.contains("aSwitch")) {
                paragraph "<b>Switch</b>"
                input "switchesOnAction", "capability.switch", title: "Switches to turn On", multiple:true, submitOnChange:true
                input "switchesOffAction", "capability.switch", title: "Switches to turn Off", multiple:true, submitOnChange:true
                input "switchesToggleAction", "capability.switch", title: "Switches to Toggle", multiple:true, submitOnChange:true
                
                input "switchesLCAction", "bool", title: "Turn Light On, Set Level and/or Color", description: "Light OLC", defaultValue:false, submitOnChange:true
                if(switchesLCAction) {
					input "setOnLC", "capability.switchLevel", title: "Select dimmer to set", required: false, multiple: true
					input "levelLC", "number", title: "On Level (1 to 99)", required: false, multiple: false, defaultValue: 99, range: '1..99'
					input "colorLC", "enum", title: "Color", required: false, multiple:false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                } else {
                    app.removeSetting("setOnLC")
                    app.removeSetting("levelLC")
                    app.removeSetting("colorLC")
                }
                
                if(switchesOnAction || switchesOffAction || switchesLCAction) {
                    paragraph "<hr>"
                    input "reverse", "bool", title: "Reverse actions when conditions are no longer true?", defaultValue:false, submitOnChange:true
                    paragraph "<small>* Only controls on/off, does not effect color or level</small>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchesOnAction")
                app.removeSetting("switchesOffAction")
                app.removeSetting("switchesToggleAction")
                app.removeSetting("switchesLCAction")
            }
		}
        if(actionType.contains("aNotification")) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
                if(useSpeech || sendPushMessage || useTheFlasher) {
                    href "notificationOptions", title:"${getImage("checkMarkGreen")} Notification Options", description:"Click here for options"
                } else {
                    href "notificationOptions", title:"Notification Options", description:"Click here for options"
                }
            }
        }
    
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this automation", required: false
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Enable extra logging for debugging."
		}
		display2()
	}
}

def notificationOptions(){
    dynamicPage(name: "notificationOptions", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
        
        if(useSpeech || sendPushMessage) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Priority Message Instructions")) { }
            section("Instructions for Priority Message:", hideable: true, hidden: true) {
                paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place the option bracket in front of any message to be spoken and the Volume, Voice and/or Speaker will be adjusted accordingly."
                paragraph "Format: [priority:sound:speaker]<br><small>Note: Any option not needed, replace with a 0 (zero).</small>"

                paragraph "<b>Priority:</b><br>This can change the voice used and the color of the message displayed on the Dashboard Tile.<br>[F:0:0] - Fun<br>[R:0:0] - Random<br>[L:0:0] - Low<br>[N:0:0] - Normal<br>[H:0:0] - High"

                paragraph "<b>Sound:</b><br>You can also specify a sound file to be played before a message!<br>[1] - [5] - Specify a files URL"
                paragraph "<b>ie.</b> [L:0:0]Amy is home or [N:3:0]Window has been open too long or [H:0:0]Heat is on and window is open"
                paragraph "If you JUST want a sound file played with NO speech after, use [L:1:0]. or [N:3:0]. etc. Notice the DOT after the [], that is the message and will not be spoken."

                paragraph "<b>Speaker:</b><br>While Follow Me allows you to setup your speakers in many ways, sometimes you want it to ONLY speak on a specific device. This option will do just that! Just replace with the corresponding speaker number from the Follow Me Parent App."
                paragraph "<b>*</b> <i>Be sure to have the 'Priority Speaker Options' section completed in the Follow Me Parent App.</i>"

                paragraph "<hr>"
                paragraph "<b>General Notes:</b>"
                paragraph "Priority Voice and Sound options are only available when using Speech Synth option.<br>Also notice there is no spaces between the option and the message."
                paragraph "<b>ie.</b> [N:3:0]Window has been open too long"
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Messages Options")) {
                paragraph "%device% - will speak the Device Name<br>%time% - will speak the current time in 24 h<br>%time1% - will speak the current time in 12 h"
                
                if(triggerType1 == "xPower" || triggerType1 == "xTemp" || triggerType1 == "xHumidity") {
                    if(oSetPointHigh) input "messageH", "text", title: "Message to speak when reading is too high", required: true, submitOnChange: true, defaultValue: "Temp is too high"
                    if(oSetPointLow) input "messageL", "text", title: "Message to speak when reading is too low", required: true, submitOnChange: true, defaultValue: "Temp is too low"
                    if(oSetPointLow) input "messageB", "text", title: "Message to speak when reading is both too high and too low", required: true, submitOnChange: true, defaultValue: "Temp is out of range"
                } else {
                    input "message", "text", title: "Message to be spoken/pushed - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
                    input "msgList", "bool", defaultValue: false, title: "Show a list view of the messages?", description: "List View", submitOnChange: true
                    if(msgList) {
                        def values = "${message}".split(";")
                        listMap = ""
                        values.each { item -> listMap += "${item}<br>"}
                        paragraph "${listMap}"
                    }
                }  
            }
        } else {
            app.removeSetting("message")
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashOnHomePreset", "number", title: "Select the Preset to use when someone comes home (1..5)", required:true, submitOnChange:true
                input "flashOnDepPreset", "number", title: "Select the Preset to use when someone leaves (1..5)", required:true, submitOnChange:true
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()

        if(startTime) schedule(startTime, startTheProcess)
        if(restriction) autoSunHandler()
        if(contactEvent) subscribe(contactEvent, "contact", startTheProcess)
        if(humidityEvent) subscribe(humidityEvent, "humidity", startTheProcess)
        if(modeEvent) subscribe(modeEvent, "mode", startTheProcess)
        if(motionEvent) subscribe(motionEvent, "motion", startTheProcess)
        if(powerEvent) subscribe(powerEvent, "power", startTheProcess)
        if(switchEvent) subscribe(switchEvent, "switch", startTheProcess)
        if(tempEvent) subscribe(tempEvent, "temperature", startTheProcess)

        if(repeat) {
            startTheProcess()
            def rT = repeatType.toString().replace("[", "").replace("]", "")
            if(logEnable) log.debug "In initialize - repeat - repeatType: ${rT}"
            
            if(rT == "r1min") { 
                runEvery1Minute(startTheProcess)
            } else if(rT == "r5min") {
                runEvery5Minutes(startTheProcess)
            } else if(rT == "r10min") {
                runEvery10Minutes(startTheProcess) 
            } else if(rT == "r15min") {
                runEvery15Minutes(startTheProcess)
            } else if(rT == "r30min") {
                runEvery30Minutes(startTheProcess)
            } else if(rT == "r1hour") {
                runEvery1Hour(startTheProcess)
            } else if(rT == "r3hour") {
                runEvery3Hours(startTheProcess)
            } else {
                if(logEnable) log.debug "In initialize - repeat - Not repeating"
            }
        }
    }
}

def startTheProcess(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In startTheProcess (${state.version})"
        checkTime()
        dayOfTheWeekHandler()
        contactHandler()
        switchHandler()
        modeHandler()
        motionHandler()
        setPointHandler()

        if(logEnable) log.debug "In startTheProcess - timeBetween: ${state.timeBetween} - daysMatch: ${state.daysMatch} - contactStatus: ${state.contactStatus} - switchStatus: ${state.switchStatus} - modeStatus: ${state.modeStatus} - motionStatus: ${state.motionStatus} - setPointStatus: ${state.setPointStatus}"
        
        if(state.daysMatch && state.contactStatus && state.switchStatus && state.modeStatus && state.motionStatus && state.setPointStatus) {            
            if(logEnable) log.debug "In startTheProcess - Everything is GOOD"
            
            if(notifyDelay && state.hasntDelayedYet) {
                int theDelay = notifyDelay * 60
                state.hasntDelayedYet = false
                runIn(theDelay, startTheProcess)
            } else {            
                if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnHandler() }
                if(actionType.contains("aSwitch") && switchesOffAction) { switchesOffHandler() }
                if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleHandler() }
                if(actionType.contains("aSwitch") && switchesLCAction) { dimmerOnHandler() }


                if(setHSM) hsmChangeHandler()
                if(modeEvent) modeChangeHandler()
                if(devicesToRefresh) devicesToRefreshHandler()
                if(rmRule) ruleMachineHandler()
                
                state.hasntDelayedYet = true
            }
        } else if(reverse) {
            if(logEnable) log.debug "In startTheProcess - Going in REVERSE"
            if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnReverseHandler() }
            if(actionType.contains("aSwitch") && switchesOffAction) { switchesOffReverseHandler() }
            if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleHandler() }
            if(actionType.contains("aSwitch") && switchesLCAction) { dimmerOnReverseHandler() }
            
            state.hasntDelayedYet = true
        }
    }
}

// *********** Start sunRestriction ***********
def autoSunHandler() {
    // autoSunHandler - This is to trigger AT the exact times with offsets
    if(logEnable) log.debug "In autoSunHandler (${state.version}) - ${app.label}"
    
    def sunriseString = location.sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    def sunsetString = location.sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", sunsetString)
    int theOffsetSunset = offsetSunset ?: 1    
    if(setBeforeAfter) {
        state.timeSunset = new Date(sunsetTime.time + (theOffsetSunset * 60 * 1000))
    } else {
        state.timeSunset = new Date(sunsetTime.time - (theOffsetSunset * 60 * 1000))
    }
    
    def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", sunriseString)
    int theOffsetSunrise = offsetSunrise ?: 1
    if(riseBeforeAfter) {
        state.timeSunrise = new Date(sunriseTime.time + (theOffsetSunrise * 60 * 1000))
    } else {
        state.timeSunrise = new Date(sunriseTime.time - (theOffsetSunrise * 60 * 1000))
    }

    if(logEnable) log.debug "In autoSunHandler - sunsetTime: ${sunsetTime} - theOffsetSunset: ${theOffsetSunset} - setBeforeAfter: ${setBeforeAfter}"
    if(logEnable) log.debug "In autoSunHandler - sunriseTime: ${sunriseTime} - theOffsetSunrise: ${theOffsetSunrise} - riseBeforeAfter: ${riseBeforeAfter}"
    if(logEnable) log.debug "In autoSunHandler - ${app.label} - timeSunset: ${state.timeSunset} - timeAfterSunrise: ${state.timeSunrise}"

    // check for new sunset/sunrise times every day at 12:05 am
    schedule("0 5 0 ? * * *", autoSunHandler)
        
    if(riseSet) { schedule(state.timeSunset, runAtSunset) }
    if(!riseSet) { schedule(state.timeSunrise, runAtSunrise) }
}

def runAtSunset() {
    if(logEnable) log.debug "In runAtSunset (${state.version}) - ${app.label} - Starting"
    startTheProcess()
}

def runAtSunrise() {
    if(logEnable) log.debug "In runAtSunrise (${state.version}) - ${app.label} - Starting"
    startTheProcess()
}
// *********** End sunRestriction ***********

def contactHandler(evt) {
    if(contactEvent) {
        if(logEnable) log.debug "In contactSensorHandler (${state.version})" 
        state.contactStatus = false
        deviceTrue = 0
        theCount = contactEvent.size()
        
        contactEvent.each {
            theValue = it.currentValue("contact")
            if(logEnable) log.debug "In contactSensorHandler - Checking: ${it.displayName} - value: ${contactValue}"
            if(csOpenClosed) {
                if(theValue == "open") { deviceTrue = deviceTrue + 1 }
            }
            if(!csOpenClosed) {
                if(theValue == "closed") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In contactSensorHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(deviceTrue == theCount) { state.contactStatus = true }
    } else {
        state.contactStatus = true
    }
}

def switchHandler(evt) {
    if(switchEvent) {
        if(logEnable) log.debug "In switchHandler (${state.version})"
        state.contactStatus = false
        deviceTrue = 0
        theCount = switchEvent.size()
        
        switchEvent.each {
            theValue = it.currentValue("switch")
            if(logEnable) log.debug "In switchHandler - Checking: ${it.displayName} - value: ${switchValue}"
            if(seOnOff) {
                if(theValue == "on") { deviceTrue = deviceTrue + 1 }
            }
            if(!seOnOff) {
                if(theValue == "off") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In switchHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(deviceTrue == theCount) { state.switchStatus = true }
    } else {
        state.switchStatus = true
    }
}

def modeHandler(evt) {
    if(modeEvent) {
        if(logEnable) log.debug "In modeHandler (${state.version})"
        state.modeStatus = false
        
        modeEvent.each { it ->
            theValue = location.mode
            if(logEnable) log.debug "In modeHandler - Checking: ${it} - value: ${modeValue}"
            
            if(modeValue.contains(it)){
                if(modeOnOff) {
                    if(theValue) { state.modeStatus = true }
                }
                if(!modeOnOff) {
                    if(!theValue) { state.modeStatus = true }
                }
                startTheProcess()
            }
        }
    } else {
        state.modeStatus = true
    }
    if(logEnable) log.debug "In modeHandler - modeStatus: ${state.modeStatus}"
}

def motionHandler(evt) {
    if(motionEvent) {
        if(logEnable) log.debug "In motionHandler (${state.version})"
        state.motionStatus = false
        deviceTrue = 0
        theCount = motionEvent.size()
        
        motionEvent.each {
            theValue = it.currentValue("motion")
            if(logEnable) log.debug "In motionHandler - Checking: ${it.displayName} - value: ${motionValue}"
            if(meOnOff) {
                if(theValue == "active") { deviceTrue = deviceTrue + 1 }
            }         
            if(!meOnOff) {
                if(theValue == "inactive") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In motionHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(deviceTrue == theCount) { state.motionStatus = true }
    } else {
        state.motionStatus = true
    }
}

def ruleMachineHandler() {
    if(logEnable) log.debug "In ruleMachineHandler - Rule: ${rmRule} - Action: ${rmAction}"
    RMUtils.sendAction(rmRule, rmAction, app.label)
}

def setPointHandler(evt) {
    if(humidityEvent || powerEvent || tempEvent) {
        state.setPointStatus = false
        state.setPointDevice = evt.displayName
        setPointValue = evt.value
        setPointValue1 = setPointValue.toDouble()
        if(logEnable) log.debug "In setPointHandler - Device: ${state.setPointDevice}, setPointHigh: ${setPointHigh}, setPointLow: ${setPointLow}, Acutal value: ${setPointValue1} - setPointHighOK: ${state.setPointHighOK}, setPointLowOK: ${state.setPointLowOK}"
        // *** setPointHigh ***
        if(oSetPointHigh && !oSetPointLow) {
            if(setPointValue1 > setPointHigh) {
                if(state.setPointHighOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Hgh) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${setPointHigh}"
                    state.setPointHighOK = "no"
                    state.setPointStatus = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 < setPointHigh) {
                if(state.setPointHighOK == "no") {
                    if(logEnable) log.debug "In setPointHandler (High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is Less THAN setPointHigh: ${setPointHigh}"
                    state.setPointHighOK = "yes"
                    //reverseTheMagicHandler()
                    state.setPointStatus = false
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
        // *** setPointLow ***
        if(oSetPointLow && !oSetPointHigh) {
            if(setPointValue1 < setPointLow) {
                if(state.setPointLowOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${setPointLow}"
                    state.setPointLowOK = "no"
                    state.setPointStatus = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 > setPointLow) {
                if(state.setPointLowOK == "no") {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointLow: ${setPointLow}"
                    state.setPointLowOK = "yes"
                    //reverseTheMagicHandler()
                    state.setPointStatus = false
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
        // *** Inbetween ***
        if(oSetPointHigh && oSetPointLow) {
            if(setPointValue1 > setPointHigh) {
                if(state.setPointHighOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${setPointHigh}"
                    state.setPointHighOK = "no"
                    state.setPointStatus = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 < setPointLow) {
                if(state.setPointLowOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${state.setPointDevice}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${setPointLow}"
                    state.setPointLowOK = "no"
                    state.setPointStatus = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if((setPointValue1 <= setPointHigh) && (setPointValue1 >= setPointLow)) {
                if(state.setPointHighOK == "no" || state.setPointLowOK == "no") {
                    if(logEnable) log.debug "InsetPointHandler (Both) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is BETWEEN tempHigh: ${setPointHigh} and setPointLow: ${setPointLow}"
                    state.setPointHighOK = "yes"
                    state.setPointLowOK = "yes"
                    //reverseTheMagicHandler()
                    state.setPointStatus = false
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
    } else {
        state.setPointStatus = true
    }
}



// ********** Start Actions **********

def dimmerOnHandler() {
	if(logEnable) log.debug "In dimmerOnHandler (${state.version})"
	state.fromWhere = "dimmerOn"
	state.color = "${colorLC}"
	state.onLevel = levelLC
	setLevelandColorHandler()
}

def dimmerOnReverseHandler() {
    switchesLCAction.each { it ->
        if(logEnable) log.debug "In dimmerOnReverseHandler - Turning off ${it}"
        it.off()
    }
}

def devicesToRefreshHandler() {
    devicesToRefresh.each { it ->
        if(logEnable) log.debug "In devicesToRefresh - Refreshing ${it}"
        it.refresh()
    }
}

def hsmChangeHandler() {
    if(logEnable) log.debug "In hsmChangeHandler (${state.version}) - Setting to ${setHSM}"
    sendLocationEvent (name: "hsmSetArm", value: "${setHSM}")
}

def modeChangeHandler() {
	if(logEnable) log.debug "In modeChangeHandler - Changing mode to ${modeAction}"
	setLocationMode(modeAction)
}

def switchesOnHandler() {
	switchesOnAction.each { it ->
		if(logEnable) log.debug "In switchOnHandler - Turning on ${it}"
		it.on()
	}
}

def switchesOnReverseHandler() {
	switchesOnAction.each { it ->
		if(logEnable) log.debug "In switchOnReverseHandler - Turning off ${it}"
		it.off()
	}
}

def switchesOffHandler() {
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchOffHandler - Turning off ${it}"
        it.off()
    }
}

def switchesOffReverseHandler() {
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchesOffReverseHandler - Turning on ${it}"
        it.on()
    }
}

def switchesToggleHandler() {
	switchesToggleAction.each { it ->
        status = it.currentValue("switch")
        if(status == "off") {
            if(logEnable) log.debug "In switchesToggleHandler - Turning on ${it}"
            it.on()
        } else {
            if(logEnable) log.debug "In switchesToggleHandler - Turning off ${it}"
            it.off()
        }
	}
}

// ********** End Actions **********

def messageHandler(message) {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    
    if(triggerType1 == "xHumidity" || triggerType1 == "xPower" || triggerType1 == "xTemp") {
        if(logEnable) log.debug "In messageHandler (Humidity) - oSetPointHigh: ${oSetPointHigh}, oSetPointLow: ${oSetPointLow}, state.setPointHighOK: ${state.setPointHighOK}, state.setPointLowOK: ${state.setPointLowOK}"
        if(oSetPointHigh && state.setPointHighOK == "no") theMessage = "${messageH}"
        if(oSetPointLow && state.setPointLowOK == "no") theMessage = "${messageL}"
        if((oSetPointHigh && state.setPointHighOK == "no") && (oSetPointLow && state.setPointLowOK == "no")) theMessage = "${messageB}"
    } else {
        def values = "${message}".split(";")
        vSize = values.size()
        count = vSize.toInteger()
        def randomKey = new Random().nextInt(count)

        theMessage = values[randomKey]
        if(logEnable) log.debug "In messageDeparted - Random - theMessage: ${theMessage}" 
    }
    
    state.message = theMessage

    if (state.message.contains("%time%") || state.message.contains("%time1%")) { currentDateTime() }
        
    if (state.message.contains("%device%")) {state.message = state.message.replace('%device%', state.setPointDevice)}
	if (state.message.contains("%time%")) {state.message = state.message.replace('%time%', state.theTime)}
	if (state.message.contains("%time1%")) {state.message = state.message.replace('%time1%', state.theTime1)}
    
    if(logEnable) log.debug "In messageHandler - message: ${state.message}"
    theMessage = state.message
    if(useSpeech) letsTalk(theMessage)
    if(sendPushMessage) pushHandler(theMessage)
}

def letsTalk(msg) {
    if(logEnable) log.warn "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(msg)
    }
    if(logEnable) log.warn "In letsTalk - *** Finished ***"
}

def pushHandler(msg){
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}"
	theMessage = "${app.label} - ${msg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

def currentDateTime() {
	if(logEnable) log.debug "In currentDateTime - Control Switch: ${state.controlSwitch2}"
	Date date = new Date()
	String datePart = date.format("dd/MM/yyyy")
	String timePart = date.format("HH:mm")
	String timePart1 = date.format("h:mm a")
	state.theTime = timePart		// 24 h
	state.theTime1 = timePart1		// AM PM
	if(logEnable) log.debug "In currentDateTime - ${state.theTime}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
    state.timeBetween = false
	if(fromTime) {
        if(midnightCheckR) {
            state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime)+1, new Date(), location.timeZone)
        } else {
		    state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
        }
		if(state.betweenTime) {
            if(logEnable) log.debug "In checkTime - Time within range - Don't run"
			state.timeBetween = true
		} else {
            if(logEnable) log.debug "In checkTime - Time outside of range - Can run"
			state.timeBetween = false
		}
  	} else {  
        if(logEnable) log.debug "In checkTime - NO Time Restriction Specified"
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"    
    if(days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        def dayCheck = days.contains(day)

        if(dayCheck) {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Passed"
            state.daysMatch = true
        } else {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Check Failed"
            state.daysMatch = false
        }
    } else {
        state.daysMatch = true
    }
    if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch}"
}

def setLevelandColorHandler() {
	if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, onLevel: ${state.onLevel}, color: ${state.color}"
    def hueColor = 0
    def saturation = 100
	int onLevel = state.onLevel
    switch(state.color) {
            case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }
    
	def value = [switch: "on", hue: hueColor, saturation: saturation, level: onLevel as Integer ?: 100]
    if(logEnable) log.debug "In setLevelandColorHandler - value: $value"
	if(state.fromWhere == "dimmerOn") {
    	switchesLCAction.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
    
	if(state.fromWhere == "slowOn") {
    	slowDimmerUp.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
    
	if(state.fromWhere == "slowOff") {
    	slowDimmerDn.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(level as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
}

// ********** Normal Stuff **********

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            state.eSwitch = it.currentValue("switch")
            if(state.eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.daysMatch == null){state.daysMatch = false}
	if(state.msg == null){state.msg = ""}
    if(state.sunRiseTosunSet == null) state.sunRiseTosunSet = false
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
