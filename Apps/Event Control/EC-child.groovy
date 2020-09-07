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
 *  1.0.3 - 09/07/20 - Fixed typo with Modes. Added Locks and Garage Door to Triggers/Actions. Can add in premade periodic expressions.
 *  1.0.2 - 09/06/20 - Added Periodic Options, minor adjustments
 *  1.0.1 - 09/06/20 - Made Contact, Motion and Switch Triggers 'and' or 'or'
 *  1.0.0 - 09/05/20 - Initial release.
 *
 */

import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Event Control"
	state.version = "1.0.3"
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
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Event%20Control/EC-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "notificationOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "periodicOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Automatically control devices and events using multiple triggers!"
		}
		
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Triggers")) {
            input "triggerType", "enum", title: "Trigger Type", options: [
                ["xPeriodic":"Periodic"],
                ["xDays":"Time/Days"],
                ["xContact":"Contact Sensors"],
                ["xGarageDoor":"Garage Doors"],
                ["xHumidity":"Humidity Setpoint"],
                ["xLock":"Locks"],
                ["xMode":"Mode"],
                ["xMotion":"Motion Sensors"],
                ["xPower":"Power Setpoint"],
                ["xSwitch":"Switches"],
                ["xTemp":"Temperature Setpoint"]
            ], required: true, multiple:true, submitOnChange:true
            
            paragraph "<hr>"
            if(triggerType == null) triggerType = ""
            
            if(triggerType.contains("xPeriodic")) {
                input "preMadePeriodic", "text", title: "Enter in a premade Periodic Cron Expression", required:false, submitOnChange:true
                
                href "periodicOptions", title:"Create your own Periodic Schedule Options", description:"Click here for options"
                
                paragraph "<hr>"
                paragraph "Premade cron expressions can be found at <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a>. Format and spacing is critical, only enter if you know this is correct."
                
                if(preMadePeriodic) {
                    state.remove("theSchedule")
                    state.remove("inEnglish")
                    state.theSchedule = preMadePeriodic
                } else {
                    if(state.inEnglish) { paragraph "${state.inEnglish}" }
                    app.removeSetting("preMadePeriodic")
                }
                
                if(state.theSchedule) { paragraph "Using: <small>${state.theSchedule}</small>" }
                paragraph "<hr>"
            } else {
                if(state.theSchedule || state.inEnglish) {
                    state.remove("theSchedule")
                    state.remove("inEnglish")
                }
            }
            
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
                    input "csOpenClosed", "bool", title: "Trigger when Closed (off) or Opened (on)", description: "Contact", defaultValue:false, submitOnChange:true
                    input "contactANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(contactANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("contactEvent")
            }

            if(triggerType.contains("xGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorEvent", "capability.garageDoorControl", title: "By Garage Door", required: false, multiple: true, submitOnChange: true
                if(garageDoorEvent) {
                    input "gdOpenClose", "bool", title: "Trigger when Open (off) or Closed (on)", description: "Garage Door", defaultValue:false, submitOnChange:true
                    input "garageDoorANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("garageDoorEvent")
            }
            
            if(triggerType.contains("xHumidity")) {
                paragraph "<b>Humidity</b>"
                input "humidityEvent", "capability.relativeHumidityMeasurement", title: "By Humidity Setpoints", required:false, multiple:true, submitOnChange:true
                if(humidityEvent) {
                    input "oSetPointHigh", "bool", defaultValue:false, title: "Trigger when Humidity is too High?", description: "Humidity High", submitOnChange:true
                    if(oSetPointHigh) input "setPointHigh", "number", title: "Humidity High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    input "oSetPointLow", "bool", defaultValue:false, title: "Trigger when Humidity is too Low?", description: "Humidity Low", submitOnChange:true
                    if(oSetPointLow) input "setPointLow", "number", title: "Humidity Low Setpoint", required:true, defaultValue: 30, submitOnChange:true

                    if(oSetPointHigh) paragraph "You will receive notifications if Humidity reading is above ${setPointHigh}"
                    if(oSetPointLow) paragraph "You will receive notifications if Humidity reading is below ${setPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("humidityEvent")
            }

            if(triggerType.contains("xLock")) {
                paragraph "<b>Lock</b>"
                input "lockEvent", "capability.lock", title: "By Lock", required: false, multiple: true, submitOnChange: true
                if(lockEvent) {
                    input "lUnlockedLocked", "bool", title: "Trigger when Unlocked (off) or Locked (on)", description: "Lock", defaultValue:false, submitOnChange:true
                    input "lockANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(lockANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("lockEvent")
            }
            
            if(triggerType.contains("xMode")) {
                paragraph "<b>Mode</b>"
                input "modeEvent", "mode", title: "By Mode", multiple:true, submitOnChange:true
                if(modeEvent) {
                    input "modeOnOff", "bool", defaultValue: false, title: "Mode Inactive (off) or Active (on)?", description: "Mode", submitOnChange:true
                    if(modeOnOff) paragraph "You will receive notifications if <b>any</b> of the modes are on."
                    if(!modeOnOff) paragraph "You will receive notifications if <b>any</b> of the modes are off."
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("modeEvent")
            }
            
            if(triggerType.contains("xMotion")) {
                paragraph "<b>Motion</b>"
                input "motionEvent", "capability.motionSensor", title: "By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionEvent) {
                    input "meOnOff", "bool", defaultValue:false, title: "Motion Inactive (off) or Active (on)?", description: "Motion", submitOnChange:true
                    input "motionANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(motionANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("motionEvent")
            }

            if(triggerType.contains("xPower")) {
                paragraph "<b>Power</b>"
                input "powerEvent", "capability.powerMeter", title: "By Power Setpoints", required:false, multiple:true, submitOnChange:true
                if(powerEvent) {
                    input "oSetPointHigh", "bool", defaultValue: "false", title: "Trigger when Power is too High?", description: "Power High", submitOnChange:true
                    if(oSetPointHigh) input "setPointHigh", "number", title: "Power High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    input "oSetPointLow", "bool", defaultValue:false, title: "Trigger when Power is too Low?", description: "Power Low", submitOnChange:true
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
                    input "seOnOff", "bool", defaultValue:false, title: "Switch Off (off) or On (on)?", description: "Switch", submitOnChange:true
                    input "switchANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(switchANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchEvent")
            }

            if(triggerType.contains("xTemp")) {
                paragraph "<b>Temperature</b>"
                input "tempEvent", "capability.temperatureMeasurement", title: "By Temperature Setpoints", required:false, multiple:true, submitOnChange:true
                if(tempEvent) {
                    input "oSetPointHigh", "bool", defaultValue:false, title: "Trigger when Temperature is too High?", description: "Temp High", submitOnChange:true
                    if(oSetPointHigh) input "setPointHigh", "number", title: "Temperature High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    input "oSetPointLow", "bool", defaultValue:false, title: "Trigger when Temperature is too Low?", description: "Temp Low", submitOnChange:true
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
        
// ********** Start Actions **********
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Actions")) {
            input "actionType", "enum", title: "Actions to Perform", options: [
                ["aGarageDoor":"Garage Doors"],
                ["aHSM":"Hubitat Safety Monitor"],
                ["aLock":"Locks"],
                ["aMode":"Modes"],
                ["aNotification":"Notifications (speech/push/flash)"],               
                ["aRefresh":"Refresh"],
                ["aRule":"Rule Machine"],
                ["aSwitch":"Switches"]
            ], required:false, multiple:true, submitOnChange:true
            
            paragraph "<hr>"
            if(actionType == null) actionType = " "
            
            if(actionType.contains("aGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorClosedAction", "capability.garageDoorControl", title: "Close Devices", multiple:true, submitOnChange:true
                input "garageDoorOpenAction", "capability.garageDoorControl", title: "Open Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
            } else {
                app.removeSetting("garageDoorClosedAction")
                app.removeSetting("garageDoorOpenAction")
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
            
            if(actionType.contains("aLock")) {
                paragraph "<b>Lock</b>"
                input "lockAction", "capability.lock", title: "Lock Devices", multiple:true, submitOnChange:true
                input "unlockAction", "capability.lock", title: "Unlock Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
            } else {
                app.removeSetting("lockAction")
                app.removeSetting("unlockAction")
            }
            
            if(actionType.contains("aMode")) {
                paragraph "<b>Mode</b>"
                input "modeAction", "mode", title: "Change Mode to", multiple:false, submitOnChange:true
                paragraph "<hr>"
            } else {
                app.removeSetting("modeAction")
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
       
// ********** End Actions **********
        
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

def periodicOptions(){
    dynamicPage(name: "periodicOptions", title: "Periodic Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Periodic Schedule Options")) {
            paragraph "If not familiar with Cron Generators, please visit <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a> to see what this can do! Be sure to scroll down on that page to see the Cron expression examples."
            input "frequency", "enum", title: "Frequency", options: ["Seconds", "Minutes", "Hours", "Day", "Month", "Year"], submitOnChange:true
                        
            if(frequency == "Seconds") {
                input "secondTrigger", "enum", title: "Select the Seconds Option", options: [
                    ["1":"1. Every Second"],
                    ["2":"2. Every 'x' Second(s), starting at 'Second'"],
                    ["3":"3. Specific Second(s)"],
                    ["4":"4. Every Second between Second 'x' and Second 'y'"]
                ], submitOnChange:true
                
                if(secondTrigger == "1") {
                    paragraph "No options available (notice the '*' in Second)"
                    state.seconds = "*"
                    
                    // in English
                    state.inEnglish1s = "Every Second."
                    
                } else if(secondTrigger == "2") {
                    input "everySeconds2", "number", title: "Every 'x' Seconds (1 to 24)", range: '1..24', submitOnChange:true
                    input "startingSeconds2", "enum", title: "Starting at Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true 
                    
                    if(everySeconds2 == null) everySeconds2 = "0"
                    if(startingSeconds2 == null) startingSeconds2 = "0"
                    state.seconds = "${startingSeconds2}/${everySeconds2}" 
                    
                    // in English
                    state.inEnglish1s = "Every ${everySeconds2} Second(s), starting at second ${startingSeconds2}."
                    
                } else if(secondTrigger == "3") {
                    input "seconds3", "enum", title: "Specific Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], multiple:true, submitOnChange:true
                    state.seconds = seconds3
                    
                    // in English
                    state.inEnglish1s = "Specific Second(s) - ${seconds3}"
                    
                } else if(secondTrigger == "4") {
                    input "sbetweenX4", "enum", title: "Between Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    input "sbetweenY4", "enum", title: "and Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    state.seconds = "${sbetweenX4}-${sbetweenY4}"
                    
                    // in English
                    state.inEnglish1s = "Every Second between Second ${sbetweenX4} and Second ${sbetweenY4}"
                    
                } else {
                    state.seconds = "*"
                }
                
                state.seconds = state.seconds.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Minutes") {
                input "minuteTrigger", "enum", title: "Select the Minute Option", options: [
                    ["1":"1. Every Minute"],
                    ["2":"2. Every 'x' Minute(s), starting at 'Minute'"],
                    ["3":"3. Specific Minute(s)"],
                    ["4":"4. Every Minute between Minute 'x' and Minute 'y'"]
                ], submitOnChange:true
                
                if(minuteTrigger == "1") {
                    paragraph "No options available (notice the '*' in Minutes)"
                    state.minutes = "*"
                    
                    // in English
                    state.inEnglish1m = "Every Minute."
                    
                } else if(minuteTrigger == "2") {
                    input "everyMinutes2", "number", title: "Every 'x' Minutes (1 to 59)", range: '1..59', submitOnChange:true
                    input "startingMinutes2", "enum", title: "Starting at Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true 
                    
                    if(everyMinutes2 == null) everyMinutes2 = "0"
                    if(startingMinutes2 == null) startingMinutes2 = "0"
                    state.minutes = "${startingMinutes2}/${everyMinutes2}" 
                    
                    // in English
                    state.inEnglish1m = "Every ${everyMinutes2} Minute(s), starting at Minute ${startingMinutes2}."
                    
                } else if(minuteTrigger == "3") {
                    input "minutes3", "enum", title: "Specific Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], multiple:true, submitOnChange:true
                    state.minutes = minutes3
                    
                    // in English
                    state.inEnglish1m = "Specific Minute(s) - ${minutes3}"
                    
                } else if(minuteTrigger == "4") {
                    input "mbetweenX4", "enum", title: "Between Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    input "mbetweenY4", "enum", title: "and Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    state.minutes = "${mbetweenX4}-${mbetweenY4}"
                    
                    // in English
                    state.inEnglish1m = "Every Minute between Minute ${mbetweenX4} and Minute ${mbetweenY4}"
                    
                } else {
                    state.minutes = "*"
                }
                
                state.minutes = state.minutes.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Hours") {
                input "hourTrigger", "enum", title: "Select the Hour Option", options: [
                    ["1":"1. Every Hour"],
                    ["2":"2. Every 'x' Hour(s), starting at 'Hour'"],
                    ["3":"3. Specific Hour(s)"],
                    ["4":"4. Every Hour between Hour 'x' and Hour 'y'"]
                ], submitOnChange:true
                
                if(hourTrigger == "1") {
                    paragraph "No options available (notice the '*' in Hours)"
                    state.theHours = "*"
                    
                    // in English
                    state.inEnglish1m = "Every Hour."
                    
                } else if(hourTrigger == "2") {
                    input "everyHours2", "number", title: "Every 'x' Hours (1 to 24)", range: '1..24', submitOnChange:true
                    input "startingHours2", "enum", title: "Starting at Hours", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true 
                    
                    if(everyHours2 == null) everyHours2 = "0"
                    if(startingHours2 == null) startingHours2 = "0"
                    state.theHours = "${startingHours2}/${everyHours2}" 
                    
                    // in English
                    state.inEnglish1m = "Every ${everyHours2} Hour(s), starting at Hour ${startingHours2}."
                    
                } else if(hourTrigger == "3") {
                    input "hours3", "enum", title: "Specific Hour", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], multiple:true, submitOnChange:true
                    state.theHours = hours3
                    
                    // in English
                    state.inEnglish1m = "Specific Hour(s) - ${hours3}"
                    
                } else if(hourTrigger == "4") {
                    input "mbetweenX4", "enum", title: "Between Hour", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    input "mbetweenY4", "enum", title: "and Hour", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    state.theHours = "${mbetweenX4}-${mbetweenY4}"
                    
                    // in English
                    state.inEnglish1m = "Every Hour between Hour ${mbetweenX4} and Hour ${mbetweenY4}"
                    
                } else {
                    state.theHours = "*"
                }
                
                state.theHours = state.theHours.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Day") {
                input "dayTrigger", "enum", title: "Select the Day Option", options: [
                    ["1":"1. Every Day"],
                    ["2":"2. Every 'x' Day(s), starting on 'Day'"],
                    ["3":"3. Every 'x' Day(s), starting on the 'x' Day of the Month"],
                    ["4":"4. Specific Day(s) of Week"],
                    ["5":"5. Specific Day(s) of the Month"],
                    ["6":"6. On the Last Day of the Month"],
                    ["7":"7. On the Last Weekday of the Month"],
                    ["8":"8. On the Last 'x' of the Month"],
                    ["9":"9. 'x' Day(s) before the end of the Month"],
                    ["10":"10. Nearest Weekday (Monday to Friday) to the 'x' of the Month"],
                    ["11":"11. On the '(1st, 2nd, etc)' '(Mon, Tue, etc)' of the Month"],
                ], submitOnChange:true
                              
                if(dayTrigger == "1") {
                    paragraph "1. No options available (notice the '*' in the Day in the Week)"
                    state.daily = "?"
                    state.dayOfTheWeek = "*"
                    state.inEnglish2 = "Every Day."
                    
                } else if(dayTrigger == "2") {
                    input "everyDays2", "number", title: "2. Every How Many Days (1 to 7)", range: '1..7', submitOnChange:true
                    input "startingDays2", "enum", title: "Starting on Day", options: [
                        ["1":"Sunday"],
                        ["2":"Monday"], 
                        ["3":"Tuesday"],
                        ["4":"Wednesday"],
                        ["5":"Thursday"],
                        ["6":"Friday"],
                        ["7":"Saturday"]
                    ], submitOnChange:true 

                    if(everyDays2 == null) everyDays2 = "1"
                    if(startingDays2 == null) startingDays2 = "1"
                    state.daily = "?"
                    state.dayOfTheWeek = "${startingDays2}/${everyDays2}"
                    
                    // In English                    
                    if(startingDays2 == "1") theDayName = "Sunday"
                    if(startingDays2 == "2") theDayName = "Monday"
                    if(startingDays2 == "3") theDayName = "Tuesday"
                    if(startingDays2 == "4") theDayName = "Wednesday"
                    if(startingDays2 == "5") theDayName = "Thursday"
                    if(startingDays2 == "6") theDayName = "Friday"
                    if(startingDays2 == "7") theDayName = "Saturday"
                    state.inEnglish2 = "Every ${everyDays2} Day(s), starting on ${theDayName}."
                    
                } else if(dayTrigger == "3") {
                    input "everyDays3", "number", title: "3. Every How Many Days (1 to 31)", range: '1..31', submitOnChange:true
                    input "startingDays3", "enum", title: "Starting on Day", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true 

                    if(everyDays3 == null) everyDays3 = "1"
                    if(startingDays3 == null) startingDays3 = "1"
                    state.daily = "${startingDays3}/${everyDays3}"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "Every ${everyDays3} Day(s), starting on the ${startingDays3} of the Month."
                    
                } else if(dayTrigger == "4") {
                    input "everyDays4", "enum", title: "4. Specific Day(s) of the Week", multiple:true, options: [
                        ["SUN":"Sunday"],
                        ["MON":"Monday"], 
                        ["TUE":"Tuesday"],
                        ["WED":"Wednesday"],
                        ["THU":"Thursday"],
                        ["FRI":"Friday"],
                        ["SAT":"Saturday"]
                    ], submitOnChange:true  

                    if(everyDays4 == null) everyDays4 = "SUN"
                    state.daily = "?"
                    state.dayOfTheWeek = "${everyDays4}"
                    
                    // in English
                    state.inEnglish2 = "Every ${everyDays4} of the Month."
                    
                } else if(dayTrigger == "5") {
                    input "everyDays5", "enum", title: "5. Specific Day(s) of the Month", multiple:true, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true  

                    if(everyDays5 == null) everyDays5 = "1"
                    state.daily = "${everyDays}"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "Every ${everyDays5} of the Month."
                    
                } else if(dayTrigger == "6") {
                    paragraph "No options available (notice the 'L' in the Day of Month)"
                    state.daily = "L"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "On the Last Day of the Month."
                    
                } else if(dayTrigger == "7") {
                    paragraph "No options available (notice the 'LW' in the Day of Month)"
                    state.daily = "LW"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "On the Last Weekday of the Month."
                    
                } else if(dayTrigger == "8") {
                    input "everyDays8", "enum", title: "On the Last 'x' of the Month", options: [
                        ["1L":"Sunday"],
                        ["2L":"Monday"], 
                        ["3L":"Tuesday"],
                        ["4L":"Wednesday"],
                        ["5L":"Thursday"],
                        ["6L":"Friday"],
                        ["7L":"Saturday"]
                    ], submitOnChange:true  

                    if(everyDays8 == null) everyDays8 = "1L"
                    state.daily = "?"
                    state.dayOfTheWeek = "${everyDays8}"
                    
                    // in English
                    if(everyDays8 == "1L") dayName = "Sunday"
                    if(everyDays8 == "2L") dayName = "Monday"
                    if(everyDays8 == "3L") dayName = "Tuesday"
                    if(everyDays8 == "4L") dayName = "Wednesday"
                    if(everyDays8 == "5L") dayName = "Thursday"
                    if(everyDays8 == "6L") dayName = "Friday"
                    if(everyDays8 == "7L") dayName = "Saturday"
                    state.inEnglish2 = "On the Last ${dayName} of the Month."
                    
                } else if(dayTrigger == "9") {
                    input "everyDays9", "enum", title: "'x' Day(s) before the end of the Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true  

                    if(everyDays9 == null) everyDays9 = "1"
                    state.daily = "L-${everyDays9}"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "${everyDays9} Day(s) before the end of the Month."
                    
                } else if(dayTrigger == "10") {
                    input "everyDays10", "enum", title: "Nearest Weekday (Monday to Friday) to the 'x' of the Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true  

                    if(everyDays10 == null) everyDays10 = "1"
                    state.daily = "${everyDays10}W"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "Nearest Weekday (Monday to Friday) to the ${everyDays10} of the Month."
                    
                } else if(dayTrigger == "11") {
                    input "everyDays11", "enum", title: "On the 'x' of the Month", options: [
                        ["1":"1st"],
                        ["2":"2nd"],
                        ["3":"3rd"],
                        ["4":"4th"],
                        ["5":"5th"]
                    ], submitOnChange:true
                    input "startingDays11", "enum", title: "Day", options: [
                        ["1":"Sunday"],
                        ["2":"Monday"], 
                        ["3":"Tuesday"],
                        ["4":"Wednesday"],
                        ["5":"Thursday"],
                        ["6":"Friday"],
                        ["7":"Saturday"]
                    ], submitOnChange:true 
                    
                    if(everyDays11 == null) everyDays11 = "1"
                    if(startingDays11 == null) startingDays11 = "1"
                    state.daily = "?"
                    state.dayOfTheWeek = "${startingDays11}#${everyDays11}" 
                    
                    // in English
                    if(everyDays11 == "1") which = "1st"
                    if(everyDays11 == "2") which = "2nd"
                    if(everyDays11 == "3") which = "3rd"
                    if(everyDays11 == "4") which = "4th"
                    if(everyDays11 == "5") which = "5th"
                    
                    if(startingDays11 == "1") dName = "Sunday"
                    if(startingDays11 == "2") dName = "Monday"
                    if(startingDays11 == "3") dName = "Tuesday"
                    if(startingDays11 == "4") dName = "Wednesday"
                    if(startingDays11 == "5") dName = "Thursday"
                    if(startingDays11 == "6") dName = "Friday"
                    if(startingDays11 == "7") dName = "Saturday"
                    state.inEnglish2 = "On the ${which} ${dName} of the Month"
                }
                
                state.dayOfTheWeek = state.dayOfTheWeek.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Month") {
                input "monthTrigger", "enum", title: "Select the Month Option", options: [
                    ["1":"1. Every Month"],
                    ["2":"2. Every 'x' Month(s), starting at 'Month'"],
                    ["3":"3. Specific Month(s)"],
                    ["4":"4. Every Month between Month 'x' and Month 'y'"]
                ], submitOnChange:true
                
                if(monthTrigger == "1") {
                    paragraph "No options available (notice the '*' in Months)"
                    state.months = "*"
                    
                    // in English
                    state.inEnglish3 = "Every Month."
                    
                } else if(monthTrigger == "2") {
                    input "everyMonth2", "number", title: "Every How Many Months (1 to 12)", range: '1..12', submitOnChange:true
                    input "startingMonths2", "enum", title: "Starting at Month", options: ["*", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"], submitOnChange:true 

                    if(everyMonth2 == null) everyMonth2 = "0"
                    if(startingMonths2 == null) startingMonths2 = "0"
                    state.months = "${startingMonths2}/${everyMonth2}" 
                    
                    // in English
                    state.inEnglish3 = "Every ${everyMonth2} Month(s), starting at ${startingMonths2}."
                    
                } else if(monthTrigger == "3") {    
                    input "monthly3", "enum", title: "Specific Hour", options: ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"], multiple:true, submitOnChange:true
                    state.months = monthly3
                    
                    // in English
                    state.inEnglish3 = "Specific Month(s) - ${monthly3}"
                    
                } else if(monthTrigger == "4") {
                    input "mbetweenX4", "enum", title: "Between Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"], submitOnChange:true
                    input "mbetweenY4", "enum", title: "and Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"], submitOnChange:true
                    state.months = "${mbetweenX4}-${mbetweenY4}"
                    
                    // in English
                    state.inEnglish3 = "Every Month between Month ${mbetweenX4} and Month ${mbetweenY4}."
                    
                } else {
                    state.months = "*"
                }
                
                state.months = state.months.toString().replace("[", "").replace("]", "").replace(" ","")
            }

            if(frequency == "Year") {
                input "yearTrigger", "enum", title: "Select the Year Option", options: [
                    ["1":"1. Every Year"],
                    ["2":"2. Every 'x' Year(s), starting at 'Year'"],
                    ["3":"3. Specific Year(s)"],
                    ["4":"4. Every Year between Month 'x' and Year 'y'"]
                ], submitOnChange:true
                
                state.inEnglish4 = "Every Year."
                
                if(yearTrigger == "1") {
                    paragraph "No options available (notice the '*' in Years)"
                    state.years = "*"
                } else if(yearTrigger == "2") {
                    input "everyYear2", "number", title: "Every How Many Years (1 to 10)", range: '1..10', submitOnChange:true
                    input "startingYears2", "enum", title: "Starting at year", options: ["2020", "2021", "2022", "2023", "2024", "2025"], submitOnChange:true 

                    if(everyYear2 == null) everyYear2 = "0"
                    if(startingYears2 == null) startingYears2 = "0"
                    state.years = "${startingYears2}/${everyYear2}" 
                    
                    // in English
                    state.inEnglish4 = "Every ${everyYear2} Years(s), starting at ${startingYears2}."
                    
                } else if(yearTrigger == "3") {
                    input "yearly3", "enum", title: "Specific Year", options: ["2020", "2021", "2022", "2023", "2025", "2026", "2027", "2028", "2029", "2030"], multiple:true, submitOnChange:true
                    state.years = yearly3

                    // in English
                    state.inEnglish4 = "Specific Year(s) - ${yearly3}"
                    
                } else if(yearTrigger == "4") {
                    input "ybetweenX4", "enum", title: "Between Month", options: ["2020", "2021", "2022", "2023", "2025", "2026", "2027", "2028", "2029", "2030"], submitOnChange:true
                    input "ybetweenY4", "enum", title: "and Month", options: ["2020", "2021", "2022", "2023", "2025", "2026", "2027", "2028", "2029", "2030"], submitOnChange:true
                    state.years = "${ybetweenX4}-${ybetweenY4}"
                    
                    // in English
                    state.inEnglish4 = "Every Year between Year ${ybetweenX4} and Year ${ybetweenY4}."
                    
                } else {
                    state.years = "*"
                }
                
                state.years = state.years.toString().replace("[", "").replace("]", "").replace(" ","")
            }
               
            if(state.seconds == null) state.seconds = "*"
            if(state.minutes == null) state.minutes = "*"
            if(state.theHours == null) state.theHours = "*"
            if(state.daily == null) state.daily = "?"
            if(state.months == null) state.months = "*"
            if(state.dayOfTheWeek == null) state.dayOfTheWeek = "*"
            if(state.years == null) state.years = "*"

            paragraph "<hr>"
            
            if(state.inEnglish1h == null || state.inEnglish1h == "") state.inEnglish1h = "Every Hour."
            if(state.inEnglish1m == null || state.inEnglish1m == "") state.inEnglish1m = "Every Minute."
            if(state.inEnglish1s == null || state.inEnglish1s == "") state.inEnglish1s = "Every Second."
            
            if(state.inEnglish2 == null || state.inEnglish2 == "") state.inEnglish2 = "Every Day."
            if(state.inEnglish3 == null || state.inEnglish3 == "") state.inEnglish3 = "Every Month."
            if(state.inEnglish4 == null || state.inEnglish4 == "") state.inEnglish4 = "Every Year."
            
            state.inEnglish = "<b>Will Run:<br>${state.inEnglish1h} ${state.inEnglish1m} ${state.inEnglish1s}<br>${state.inEnglish2} ${state.inEnglish3} ${state.inEnglish4}</b>"
            
            paragraph "${state.inEnglish}"
            state.theSchedule = "${state.seconds} ${state.minutes} ${state.theHours} ${state.daily} ${state.months} ${state.dayOfTheWeek} ${state.years}"
            
            table = "<table width=90% align=center><tr align=center>"
            table += "<td><b>Seconds</b><br>${state.seconds}<td><b>Minutes</b><br>${state.minutes}<td><b>Hours</b><br>${state.theHours}<td><b>Day Of Month</b><br>${state.daily}<td><b>Months</b><br>${state.months}<td><b>Day Of Week</b><br>${state.dayOfTheWeek}<td><b>Years</b><br>${state.years}"
            table += "</table>"
            
            paragraph "<hr>"
            paragraph "${table}"
            paragraph "<hr>"
            paragraph "Cron Schedule: <b>${state.theSchedule}</b>" 
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
        if(garagedoorEvent) subscribe(garagedoorEvent, "door", startTheProcess)
        if(humidityEvent) subscribe(humidityEvent, "humidity", startTheProcess)
        if(lockEvent) subscribe(lockEvent, "lock", startTheProcess)
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
        
        if(triggerType.contains("xPeriodic")) { 
            if(logEnable) log.debug "In initialize - xPeriodic - Starting!- (${state.theSchedule})"
            schedule(state.theSchedule, startTheProcess)
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
        contactHandler()
        dayOfTheWeekHandler()
        garageDoorHandler()
        modeHandler()
        motionHandler()
        setPointHandler()
        switchHandler()
        
        if(logEnable) log.debug "In startTheProcess - checkTime: ${state.timeBetween} - contactStatus: ${state.contactStatus} - daysMatch: ${state.daysMatch} - garageDoor: ${state.garageDoorStatus} - modeStatus: ${state.modeStatus} - motionStatus: ${state.motionStatus} - setPointStatus: ${state.setPointStatus} - switchStatus: ${state.switchStatus}"
        
        if(state.daysMatch && state.contactStatus && state.switchStatus && state.modeStatus && state.motionStatus && state.setPointStatus) {            
            if(logEnable) log.debug "In startTheProcess - Everything is GOOD"
            
            if(notifyDelay && state.hasntDelayedYet) {
                int theDelay = notifyDelay * 60
                state.hasntDelayedYet = false
                runIn(theDelay, startTheProcess)
            } else {     
                if(actionType == null) actionType = ""
                
                if(actionType.contains("aGarageDoor") && (garageDoorOpenAction || garageDoorClosedAction)) { garageDoorActionHandler() }
                if(actionType.contains("aLock") && (lockAction || unlockAction)) { lockActionHandler() }
                
                if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnHandler() }
                if(actionType.contains("aSwitch") && switchesOffAction) { switchesOffHandler() }
                if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleHandler() }
                if(actionType.contains("aSwitch") && switchesLCAction) { dimmerOnHandler() }


                if(setHSM) hsmChangeHandler()
                if(modeAction) modeChangeHandler()
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
            if(logEnable) log.debug "In contactSensorHandler - Checking: ${it.displayName} - value: ${theValue}"
            if(csOpenClosed) {
                if(theValue == "open") { deviceTrue = deviceTrue + 1 }              
            }
            if(!csOpenClosed) {
                if(theValue == "closed") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In contactSensorHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(contactANDOR) {
            if(deviceTrue >= 1) { state.contactStatus = true }           // OR
        } else {
            if(deviceTrue == theCount) { state.contactStatus = true }    // AND
        }
    } else {
        state.contactStatus = true
    }
}

def garageDoorHandler(evt) {
    if(garageDoorEvent) {
        if(logEnable) log.debug "In garageDoorHandler (${state.version})"
        state.garageDoorStatus = false
        deviceTrue = 0
        theCount = garageDoorEvent.size()
        
        garageDoorEvent.each {
            theValue = it.currentValue("door")
            if(logEnable) log.debug "In garageDoorHandler - Checking: ${it.displayName} - value: ${theValue}"
            if(gdOpenClosed) {
                if(theValue == "closed") { deviceTrue = deviceTrue + 1 }
            }         
            if(!gdOpenClosed) {
                if(theValue == "open") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In garageDoorHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(garageDoorANDOR) {
            if(deviceTrue >= 1) { state.garageDoorStatus = true }           // OR
        } else {
            if(deviceTrue == theCount) { state.garageDoorStatus = true }    // AND
        }
    } else {
        state.garageDoorStatus = true
    }
}

def modeHandler(evt) {
    if(modeEvent) {
        if(logEnable) log.debug "In modeHandler (${state.version})"
        state.modeStatus = false

        modeEvent.each { it ->
            theValue = location.mode
            if(logEnable) log.debug "In modeHandler - Checking: ${it} - value: ${theValue}"

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

def lockHandler(evt) {
    if(lockEvent) {
        if(logEnable) log.debug "In lockHandler (${state.version})"
        state.lockStatus = false
        deviceTrue = 0
        theCount = lockEvent.size()
        
        lockEvent.each {
            theValue = it.currentValue("lock")
            if(logEnable) log.debug "In lockHandler - Checking: ${it.displayName} - value: ${theValue}"
            if(lUnlockedLocked) {
                if(theValue == "locked") { deviceTrue = deviceTrue + 1 }
            }         
            if(!lUnlockedLocked) {
                if(theValue == "unlocked") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In lockHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(lockANDOR) {
            if(deviceTrue >= 1) { state.lockStatus = true }           // OR
        } else {
            if(deviceTrue == theCount) { state.lockStatus = true }    // AND
        }
    } else {
        state.lockStatus = true
    }
}

def motionHandler(evt) {
    if(motionEvent) {
        if(logEnable) log.debug "In motionHandler (${state.version})"
        state.motionStatus = false
        deviceTrue = 0
        theCount = motionEvent.size()
        
        motionEvent.each {
            theValue = it.currentValue("motion")
            if(logEnable) log.debug "In motionHandler - Checking: ${it.displayName} - value: ${theValue}"
            if(meOnOff) {
                if(theValue == "active") { deviceTrue = deviceTrue + 1 }
            }         
            if(!meOnOff) {
                if(theValue == "inactive") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In motionHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(motionANDOR) {
            if(deviceTrue >= 1) { state.motionStatus = true }           // OR
        } else {
            if(deviceTrue == theCount) { state.motionStatus = true }    // AND
        }
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

def switchHandler(evt) {
    if(switchEvent) {
        if(logEnable) log.debug "In switchHandler (${state.version})"
        state.switchStatus = false
        deviceTrue = 0
        theCount = switchEvent.size()
        
        switchEvent.each {
            theValue = it.currentValue("switch")
            if(logEnable) log.debug "In switchHandler - Checking: ${it.displayName} - value: ${theValue}"
            if(seOnOff) {
                if(theValue == "on") { deviceTrue = deviceTrue + 1 }
            }
            if(!seOnOff) {
                if(theValue == "off") { deviceTrue = deviceTrue + 1 }
            }
        }
        if(logEnable) log.debug "In switchHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
        if(switchANDOR) {
            if(deviceTrue >= 1) { state.switchStatus = true }           // OR
        } else {
            if(deviceTrue == theCount) { state.switchStatus = true }    // AND
        }
    } else {
        state.switchStatus = true
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

def garageDoorHandler() {
    if(logEnable) log.debug "In garageDoorHandler (${state.version})"
    if(garageDoorClosedAction) {
        garageDoorClosedAction.each { it ->
            if(logEnable) log.debug "In garageDoorHandler - Closing ${it}"
            it.close()
        }
    }
    
    if(garageDoorOpenAction) {
        garageDoorClosedAction.each { it ->
            if(logEnable) log.debug "In garageDoorHandler - Open ${it}"
            it.open()
        }
    }
}

def hsmChangeHandler() {
    if(logEnable) log.debug "In hsmChangeHandler (${state.version}) - Setting to ${setHSM}"
    sendLocationEvent (name: "hsmSetArm", value: "${setHSM}")
}

def lockActionHandler() {
    if(logEnable) log.debug "In lockActionHandler (${state.version})"
    if(lockAction) {
        lockAction.each { it ->
            if(logEnable) log.debug "In lockActionHandler - Locking ${it}"
            it.lock()
        }
    }
    
    if(unlockAction) {
        unlockAction.each { it ->
            if(logEnable) log.debug "In unlockActionHandler - Unlocking ${it}"
            it.unlock()
        }
    }
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
