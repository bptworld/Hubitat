/**
 *  ****************  Event 42 Child App  ****************
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
 *  1.1.8 - 09/11/20 - Added random delay option, added Test button
 *  1.1.7 - 09/10/20 - To keep E42 lean and mean, Removed the Periodic Cron Expression maker and turned it into its own app.
 *  Added Triggers: HSM Alert, HSM Status, other minor changes
 *  1.1.6 - 09/10/20 - Minor changes
 *  1.1.5 - 09/10/20 - Fixed some typos, Name change: Event 42 (thanks furom!)
 *  ---
 *  1.0.0 - 09/05/20 - Initial release.
 *
 */

import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Event 42"
	state.version = "1.1.8"
}

definition(
    name: "Event 42 Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automatically control devices and events using multiple triggers!",
    category: "Convenience",
	parent: "BPTWorld:Event 42",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Event%2042/E42-child.groovy",
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
            input "triggerType", "enum", title: "Trigger Type", options: [
                ["xPeriodic":"Periodic"],
                ["xTimeDays":"Time/Days - Sub-Menu"],
                ["xAcceleration":"Acceleration Sensor"],
                ["xBattery":"Battery Setpoint"],
                ["xContact":"Contact Sensors"],
                ["xGarageDoor":"Garage Doors"],
                ["xHSMAlert":"HSM Alerts *** not tested ***"],
                ["xHSMStatus":"HSM Status *** not tested ***"],
                ["xHumidity":"Humidity Setpoint"],
                ["xIlluminance":"Illuminance Setpoint"],
                ["xLock":"Locks"],
                ["xMode":"Mode"],
                ["xMotion":"Motion Sensors"],
                ["xPower":"Power Setpoint"],
                ["xPresence":"Presence Sensor"],
                ["xSwitch":"Switches"],
                ["xTemp":"Temperature Setpoint"],
                ["xWater":"Water Sensor"]
            ], required: true, multiple:true, submitOnChange:true
            
            paragraph "<hr>"
            if(triggerType == null) triggerType = ""
            
            if(triggerType.contains("xPeriodic")) {
                input "preMadePeriodic", "text", title: "Enter in a premade Periodic Cron Expression", required:false, submitOnChange:true
                
                paragraph "Create your own Expressions using the 'Periodic Expressions' app found in Hubitat Package Manager or on <a href='https://github.com/bptworld/Hubitat/' target='_blank'>my GitHub</a>."
                paragraph "<hr>"
                paragraph "Premade cron expressions can be found at <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a>. Remember, Format and spacing is critical."
                
            }
            
            if(triggerType.contains("xTimeDays")) {
                paragraph "<b>Time/Days - Sub-Menu</b>"
                input "timeDaysType", "enum", title: "Trigger Type", options: [
                    ["tDays":"By Days"],
                    ["tTime":"Certain Time"],
                    ["tBetween":"Between Two Times"],
                    ["tSunsetSunrise":"Sunset to Sunrise"],                  
                    ["tSunrise":"Just Sunrise"],
                    ["tSunset":"Just Sunset"],
                ], required: true, multiple:true, submitOnChange:true
                
                paragraph "<hr>"
                if(timeDaysType == null) timeDaysType = ""
                
                if(timeDaysType.contains("tDays")) {
                    paragraph "<b>By Days</b>"
                    input "days", "enum", title: "Activate on these days", description: "Days to Activate", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                    paragraph "<hr>"
                } else {
                    app.removeSetting("days")
                }

                if(timeDaysType.contains("tTime")) {
                    paragraph "<b>Certain Time</b>"
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
                    paragraph "<hr>"
                } else {
                    app.removeSetting("startTime")
                    app.removeSetting("repeat")
                    app.removeSetting("repeatType")
                    app?.updateSetting("repeat",[value:"false",type:"bool"])
                }
                
                if(timeDaysType.contains("tBetween")) {
                    paragraph "<b>Between two times</b>"
                    input "fromTime", "time", title: "From", required: false, width: 6, submitOnChange:true
                    input "toTime", "time", title: "To", required: false, width: 6
                    input "midnightCheckR", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                    paragraph "<hr>"
                } else {
                    app.removeSetting("fromTime")
                    app.removeSetting("toTime")
                    app?.updateSetting("midnightCheckR",[value:"false",type:"bool"])
                }
                
                if(timeDaysType.contains("tSunsetSunrise")) {
                    if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunrise")) {
                        paragraph "<b>'Sunset to Sunrise' and 'Just Sunrise' can't be used at the same time. Please deselect one of them.</b>"
                    } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunset")) {
                        paragraph "<b>'Sunset to Sunrise' and 'Just Sunset' can't be used at the same time. Please deselect one of them.</b>"
                    } else {
                        paragraph "<b>Sunset to Sunrise</b>"                   
                        paragraph "Sunset"
                        input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", defaultValue:false, submitOnChange:true, width:6
                        input "offsetSunset", "number", title: "Offset (minutes)", width:6

                        paragraph "Sunrise"
                        input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", defaultValue:false, submitOnChange:true, width:6
                        input "offsetSunrise", "number", title: "Offset(minutes)", width:6
                        paragraph "<hr>"
                    }
                }
                
                if(timeDaysType.contains("tSunrise") && timeDaysType.contains("tSunset")) {
                    paragraph "<b>Please select 'Sunset to Sunrise', instead of both 'Just Sunrise' and 'Just Sunset'.</b>"
                } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunrise")) {
                    // Messge above will show
                } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunset")) {
                    // Messge above will show
                } else if(timeDaysType.contains("tSunrise")) {
                    paragraph "<b>Just Sunrise</b>"
                    input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", defaultValue:false, submitOnChange:true, width:6
                    input "offsetSunrise", "number", title: "Offset (minutes)", width:6
                    paragraph "<hr>"
                } else if(timeDaysType.contains("tSunset")) {
                    paragraph "<b>Just Sunset</b>"
                    input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", defaultValue:false, submitOnChange:true, width:6
                    input "offsetSunset", "number", title: "Offset (minutes)", width:6
                    paragraph "<hr>"
                }
                
                if(!timeDaysType.contains("tSunsetSunrise") && !timeDaysType.contains("tSunrise") && !timeDaysType.contains("tSunset")) { 
                    app.removeSetting("setBeforeAfter")
                    app.removeSetting("offsetSunset")
                    app.removeSetting("riseBeforeAfter")
                    app.removeSetting("offsetSunrise")
                    app?.updateSetting("setBeforeAfter",[value:"false",type:"bool"])
                    app?.updateSetting("riseBeforeAfter",[value:"false",type:"bool"])
                }
            }

            if(triggerType.contains("xAcceleration")) {
                paragraph "<b>Acceleration Sensor</b>"
                input "accelerationEvent", "capability.accelerationSensor", title: "By Acceleration Sensor", required: false, multiple: true, submitOnChange: true
                if(accelerationEvent) {
                    input "asInactiveActive", "bool", title: "Trigger when Inactive (off) or Active (on)", description: "Acceleration", defaultValue:false, submitOnChange:true
                    input "accelerationANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(accelerationANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("accelerationEvent")
                app?.updateSetting("asInactiveActive",[value:"false",type:"bool"])
                app?.updateSetting("accelerationANDOR",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xBattery")) {
                paragraph "<b>Battery</b>"
                input "batteryEvent", "capability.battery", title: "By Battery Setpoints", required:false, multiple:true, submitOnChange:true
                if(batteryEvent) {
                    input "setBEPointHigh", "bool", defaultValue:false, title: "Trigger when Battery is too High?", description: "Battery High", submitOnChange:true
                    if(setBEPointHigh) {
                        input "beSetPointHigh", "number", title: "Battery High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("beSetPointHigh")
                    }
                    
                    input "setBEPointLow", "bool", defaultValue:false, title: "Trigger when Battery is too Low?", description: "Battery Low", submitOnChange:true
                    if(setBEPointLow) {
                        input "beSetPointLow", "number", title: "Battery Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("beSetPointLow")
                    }
                    
                    if(beSetPointHigh) paragraph "You will receive notifications if Battery reading is above ${beSetPointHigh}"
                    if(beSetPointLow) paragraph "You will receive notifications if Battery reading is below ${beSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("batteryEvent")
                app.removeSetting("beSetPointHigh")
                app.removeSetting("beSetPointLow")
                app?.updateSetting("setBEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setBEPointLow",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xContact")) {
                paragraph "<b>Contact</b>"
                input "contactEvent", "capability.contactSensor", title: "By Contact Sensor", required: false, multiple: true, submitOnChange: true
                if(contactEvent) {
                    input "csClosedOpen", "bool", title: "Trigger when Closed (off) or Opened (on)", description: "Contact", defaultValue:false, submitOnChange:true
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
                app?.updateSetting("csClosedOpen",[value:"false",type:"bool"])
                app?.updateSetting("contactANDOR",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorEvent", "capability.garageDoorControl", title: "By Garage Door", required: false, multiple: true, submitOnChange: true
                if(garageDoorEvent) {
                    input "gdClosedOpen", "bool", title: "Trigger when Closed (off) or Open (on)", description: "Garage Door", defaultValue:false, submitOnChange:true
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
                app?.updateSetting("gdClosedOpen",[value:"false",type:"bool"])
                app?.updateSetting("garageDoorANDOR",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xHSMAlert")) {
                paragraph "<b>HSM Alert</b>"
                paragraph "<b>Warning: This Trigger has not been tested. Use at your own risk.</b>"
                input "hsmAlertEvent", "enum", title: "By HSM Alert", options: ["arming", "armingHome", "armingNight", "cancel", "cancelRuleAlerts", "intrusion", "intrusion-delay", "intrusion-home", "intrusion-home-delay", "intrusion-night", "intrusion-night-delay", "rule", "smoke", "water"], multiple:true, submitOnChange:true
                if(hsmAlertEvent) paragraph "You will receive notifications if <b>any</b> of the HSM Alerts are active."
                paragraph "<hr>"
            } else {
                app.removeSetting("hsmAlertEvent")
            }
            
            if(triggerType.contains("xHSMStatus")) {
                paragraph "<b>HSM Status</b>"
                paragraph "<b>Warning: This Trigger has not been tested. Use at your own risk.</b>"
                input "hsmStatusEvent", "enum", title: "By HSM Status", options: ["All Disarmed", "Armed Away", "Armed Home", "Armed Night", "Delayed Armed Away", "Delayed Armed Home", "Delayed Armed Night", "Disarmed"], multiple:true, submitOnChange:true
                if(hsmStatusEvent) paragraph "You will receive notifications if <b>any</b> of the HSM Status are active."
                paragraph "<hr>"
            } else {
                app.removeSetting("hsmStatusEvent")
            }
            
            if(triggerType.contains("xHumidity")) {
                paragraph "<b>Humidity</b>"
                input "humidityEvent", "capability.relativeHumidityMeasurement", title: "By Humidity Setpoints", required:false, multiple:true, submitOnChange:true
                if(humidityEvent) {
                    input "setHEPointHigh", "bool", defaultValue:false, title: "Trigger when Humidity is too High?", description: "Humidity High", submitOnChange:true
                    if(setHEPointHigh) {
                        input "heSetPointHigh", "number", title: "Humidity High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("heSetPointHigh")
                    }
                    
                    input "setHEPointLow", "bool", defaultValue:false, title: "Trigger when Humidity is too Low?", description: "Humidity Low", submitOnChange:true
                    if(setHEPointLow) {
                        input "heSetPointLow", "number", title: "Humidity Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("heSetPointLow")
                    }
                    
                    if(heSetPointHigh) paragraph "You will receive notifications if Humidity reading is above ${heSetPointHigh}"
                    if(heSetPointLow) paragraph "You will receive notifications if Humidity reading is below ${heSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("humidityEvent")
                app.removeSetting("heSetPointHigh")
                app.removeSetting("heSetPointLow")
                app?.updateSetting("setHEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setHEPointLow",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xIlluminance")) {
                paragraph "<b>Illuminance</b>"
                input "illuminanceEvent", "capability.illuminanceMeasurement", title: "By Illuminance Setpoints", required:false, multiple:true, submitOnChange:true
                if(illuminanceEvent) {
                    input "setIEPointHigh", "bool", defaultValue:false, title: "Trigger when Illuminance is too High?", description: "High", submitOnChange:true
                    if(setIEPointHigh) {
                        input "ieSetPointHigh", "number", title: "Illuminance High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("ieSetPointHigh")
                    }
                    
                    input "setIEPointLow", "bool", defaultValue:false, title: "Trigger when Illuminance is too Low?", description: "Low", submitOnChange:true
                    if(setIEPointLow) {
                        input "ieSetPointLow", "number", title: "Illuminance Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("ieSetPointLow")
                    }

                    if(iSetPointHigh) paragraph "You will receive notifications if Humidity reading is above ${ieSetPointHigh}"
                    if(iSetPointLow) paragraph "You will receive notifications if Humidity reading is below ${ieSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("illuminanceEvent")
                app.removeSetting("ieSetPointHigh")
                app.removeSetting("ieSetPointLow")
                app?.updateSetting("setIEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setIEPointLow",[value:"false",type:"bool"])
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
                app?.updateSetting("lUnlockedLocked",[value:"false",type:"bool"])
                app?.updateSetting("lockANDOR",[value:"false",type:"bool"])
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
                app?.updateSetting("modeOnOff",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xMotion")) {
                paragraph "<b>Motion</b>"
                input "motionEvent", "capability.motionSensor", title: "By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionEvent) {
                    input "meInactiveActive", "bool", defaultValue:false, title: "Motion Inactive (off) or Active (on)?", description: "Motion", submitOnChange:true
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
                app?.updateSetting("meInactiveActive",[value:"false",type:"bool"])
                app?.updateSetting("motionANDOR",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xPower")) {
                paragraph "<b>Power</b>"
                input "powerEvent", "capability.powerMeter", title: "By Power Setpoints", required:false, multiple:true, submitOnChange:true
                if(powerEvent) {
                    input "setPEPointHigh", "bool", defaultValue: "false", title: "Trigger when Power is too High?", description: "Power High", submitOnChange:true
                    if(setPEPointHigh) {
                        input "peSetPointHigh", "number", title: "Power High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("peSetPointHigh")
                    }
                    
                    input "setPEPointLow", "bool", defaultValue:false, title: "Trigger when Power is too Low?", description: "Power Low", submitOnChange:true
                    if(setPEPointLow) {
                        input "peSetPointLow", "number", title: "Power Low Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("peSetPointLow")
                    }

                    if(peSetPointHigh) paragraph "You will receive notifications if Power reading is above ${peSetPointHigh}"
                    if(peSetPointLow) paragraph "You will receive notifications if Power reading is below ${peSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("powerEvent")
                app.removeSetting("peSetPointHigh")
                app.removeSetting("peSetPointLow")
                app?.updateSetting("setPEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setPEPointLow",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xPresence")) {
                paragraph "<b>Presence</b>"
                input "presenceEvent", "capability.presenceSensor", title: "By Presence Sensor", required: false, multiple: true, submitOnChange: true
                if(presenceEvent) {
                    input "psPresentNotPresent", "bool", title: "Trigger when Present (off) or Not Present (on)", description: "Present", defaultValue:false, submitOnChange:true
                    input "presentANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(presentANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("presenceEvent")
                app?.updateSetting("psPresentNotPresent",[value:"false",type:"bool"])
                app?.updateSetting("presentANDOR",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xSwitch")) {
                paragraph "<b>Switch</b>"
                input "switchEvent", "capability.switch", title: "By Switch", required:false, multiple:true, submitOnChange:true
                if(switchEvent) {
                    input "seOffOn", "bool", defaultValue:false, title: "Switch Off (off) or On (on)?", description: "Switch", submitOnChange:true
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
                app?.updateSetting("seOnOff",[value:"false",type:"bool"])
                app?.updateSetting("switchANDOR",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xTemp")) {
                paragraph "<b>Temperature</b>"
                input "tempEvent", "capability.temperatureMeasurement", title: "By Temperature Setpoints", required:false, multiple:true, submitOnChange:true
                if(tempEvent) {
                    input "setTEPointHigh", "bool", defaultValue:false, title: "Trigger when Temperature is too High?", description: "Temp High", submitOnChange:true
                    if(setTEPointHigh) {
                        input "teSetPointHigh", "number", title: "Temperature High Setpoint", required: true, defaultValue: 75, submitOnChange: true
                    } else {
                        app.removeSetting("setTEPointHigh")
                    }
                    
                    input "setTEPointLow", "bool", defaultValue:false, title: "Trigger when Temperature is too Low?", description: "Temp Low", submitOnChange:true
                    if(setTEPointLow) {
                        input "teSetPointLow", "number", title: "Temperature Low Setpoint", required: true, defaultValue: 30, submitOnChange: true
                    } else {
                        app.removeSetting("setTEPointLow")
                    }

                    if(teSetPointHigh) paragraph "You will receive notifications if Temperature reading is above ${teSetPointHigh}"
                    if(teSetPointLow) paragraph "You will receive notifications if Temperature reading is below ${teSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("tempEvent")
                app.removeSetting("setTEPointHigh")
                app.removeSetting("setTEPointLow")
                app?.updateSetting("setTEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setTEPointLow",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xWater")) {
                paragraph "<b>Water</b>"
                input "waterEvent", "capability.waterSensor", title: "By Water Sensor", required: false, multiple: true, submitOnChange: true
                if(waterEvent) {
                    input "wsDryWet", "bool", title: "Trigger when Dry (off) or Wet (on)", description: "Water", defaultValue:false, submitOnChange:true
                    input "waterANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Trigger will fire when <b>any</b> device is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> devices are true"
                    }
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("waterEvent")
                app?.updateSetting("wsDryWet",[value:"false",type:"bool"])
                app?.updateSetting("waterANDOR",[value:"false",type:"bool"])
            }
            
            if(accelerationEvent || batteryEvent || contactEvent || humidityEvent || hsmAlertEvent || hsmStatusEvent || illuminanceEvent || modeEvent || motionEvent || powerEvent || presenceEvent || switchEvent || tempEvent || waterEvent) {
                input "setDelay", "bool", defaultValue:false, title: "<b>Set Delay?</b>", description: "Delay Time", submitOnChange:true, width:6
                input "randomDelay", "bool", defaultValue:false, title: "<b>Set Random Delay?</b>", description: "Random Delay", submitOnChange:true, width:6
                
                if(setDelay && randomDelay) paragraph "<b>Warning: Please don't select BOTH Set Delay and Random Delay.</b>"
                if(setDelay) {
                    paragraph "Delay the notifications until all devices has been in state for XX minutes."
                    input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the notifications will be cancelled.</small>"
                } else {
                    app.removeSetting("notifyDelay")
                    app?.updateSetting("setDelay",[value:"false",type:"bool"])
                }
                
                if(randomDelay) {
                    paragraph "Delay the notifications until all devices has been in state for XX minutes."                
                    input "delayLow", "number", title: "Delay Low Limit (1 to 60)", required: true, multiple: false, range: '1..60', submitOnChange:true
                    input "delayHigh", "number", title: "Delay High Limit (1 to 60)", required: true, multiple: false, range: '1..60', submitOnChange:true
                    if(delayHigh <= delayLow) paragraph "<b>Delay High must be greater than Delay Low.</b>"                    
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the notifications will be cancelled.</small>"
                } else {
                    app.removeSetting("delayLow")
                    app.removeSetting("delayHigh")
                    app?.updateSetting("randomDelay",[value:"false",type:"bool"])
                }
            } else {
                app.removeSetting("notifyDelay")
                app?.updateSetting("setDelay",[value:"false",type:"bool"])
                app.removeSetting("delayLow")
                app.removeSetting("delayHigh")
                app?.updateSetting("randomDelay",[value:"false",type:"bool"])
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
                ["aSwitch":"Switch Devices"],
                ["aValves":"Valves"]
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
                paragraph "<b>Notification</b>"
                if(useSpeech || sendPushMessage || useTheFlasher) {
                    href "notificationOptions", title:"${getImage("checkMarkGreen")} Notification Options", description:"Click here for options"
                } else {
                    href "notificationOptions", title:"Notification Options", description:"Click here for options"
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
                paragraph "<b>Switch Devices</b>"
                input "switchesOnAction", "capability.switch", title: "Switches to turn On", multiple:true, submitOnChange:true
                input "switchesOffAction", "capability.switch", title: "Switches to turn Off<br><small>Can also be used as Permanent Dim</small>", multiple:true, submitOnChange:true
                if(switchesOffAction){
                    input "permanentDim", "bool", title: "Use Permanent Dim instead of Off", defaultValue:false, submitOnChange:true
                    if(permanentDim) {
                        paragraph "Instead of turning off, lights will dim to a set level"
                        input "permanentDimLvl", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                    }
                }
                
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
                    app?.updateSetting("switchesLCAction",[value:"false",type:"bool"])
                }
                
                input "switchedDimUpAction", "bool", defaultValue: false, title: "Slowly Dim Lighting UP", description: "Dim Up", submitOnChange:true, width:6
                input "switchedDimDnAction", "bool", defaultValue: false, title: "Slowly Dim Lighting DOWN", description: "Dim Down", submitOnChange:true, width:6
                
                if(switchedDimUpAction) {
                    paragraph "<hr>"
                    paragraph "<b>Slowly Dim Lighting UP</b>"
                    input "slowDimmerUp", "capability.switchLevel", title: "Select dimmer devices to slowly rise", required: true, multiple: true
                    input "minutesUp", "number", title: "Takes how many minutes to raise (1 to 60)", required: true, multiple: false, defaultValue:15, range: '1..60'
                    input "startLevelHigh", "number", title: "Starting Level (5 to 99)", required: true, multiple: false, defaultValue: 5, range: '5..99'
                    input "targetLevelHigh", "number", title: "Target Level (5 to 99)", required: true, multiple: false, defaultValue: 99, range: '5..99'
                    input "colorUp", "enum", title: "Color", required: true, multiple:false, options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                    paragraph "Slowly raising a light level is a great way to wake up in the morning. If you want everything to delay happening until the light reaches its target level, turn this switch on."
                    input "targetDelay", "bool", defaultValue: false, title: "<b>Delay Until Finished</b>", description: "Target Delay", submitOnChange:true
                } else {
                    app.removeSetting("slowDimmerUp")
                    app.removeSetting("minutesUp")
                    app.removeSetting("startLevelHigh")
                    app.removeSetting("targetLevelHigh")
                    app.removeSetting("colorUp")
                    app?.updateSetting("targetDelay",[value:"false",type:"bool"])
                }
                
                if(switchedDimDnAction) {
                    paragraph "<hr>"
                    paragraph "<b>Slowly Dim Lighting DOWN</b>"
                    input "slowDimmerDn", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required: true, multiple: true
                    input "minutesDn", "number", title: "Takes how many minutes to dim (1 to 60)", required: true, multiple: false, defaultValue:15, range: '1..60'

                    input "useMaxLevel", "bool", title: "Use a set starting level for all lights (off) or dim from the current level of each light (on)", defaultValue:false, submitOnChange:true
                    if(useMaxLevel) {
                        paragraph "The highest level light will start the process of dimming, each light will join in as the dim level reaches the lights current value"
                        app.removeSetting("startLevelLow")
                    } else {
                        input "startLevelLow", "number", title: "Starting Level (5 to 99)", required: true, multiple: false, defaultValue: 99, range: '5..99'
                    }

                    input "targetLevelLow", "number", title: "Target Level (5 to 99)", required: true, multiple: false, defaultValue: 5, range: '5..99'
                    input "dimDnOff", "bool", defaultValue: false, title: "<b>Turn dimmer off after target is reached?</b>", description: "Dim Off Options", submitOnChange: true
                    input "colorDn", "enum", title: "Color", required: true, multiple:false, options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                } else {
                    app.removeSetting("slowDimmerDn")
                    app.removeSetting("minutesDn")
                    app.removeSetting("startLevelLow")
                    app.removeSetting("targetLevelLow")
                    app.removeSetting("dimDnOff")
                    app.removeSetting("colorDn")
                    app?.updateSetting("useMaxLevel",[value:"false",type:"bool"])
                    app?.updateSetting("dimDnOff",[value:"false",type:"bool"])
                }

                if(switchesOnAction || switchesOffAction || switchesLCAction) {
                    paragraph "<hr>"
                    input "reverse", "bool", title: "Reverse actions when conditions are no longer true?", defaultValue:false, submitOnChange:true
                    paragraph "<small>* Only controls on/off, does not effect color or level</small>"
                    
                    if(reverse && (switchesOnAction || switchesLCAction)){
                        paragraph "If a light has been turned on, Reversing it will turn it off. But with the Permanent Dim option, the light can be Dimmed to a set level instead!"
                        input "permanentDim", "bool", title: "Use Permanent Dim instead of Off", defaultValue:false, submitOnChange:true
                        if(permanentDim) {
                            paragraph "Instead of turning off, lights will dim to a set level"
                            input "permanentDimLvl", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                        } else {
                            app.removeSetting("permanentDimLvl")
                        }
                    } else {
                        app.removeSetting("permanentDimLvl")
                        app?.updateSetting("permanentDim",[value:"false",type:"bool"])
                    }
                } else {
                    app.removeSetting("permanentDimLvl")
                    app?.updateSetting("permanentDim",[value:"false",type:"bool"])
                    app?.updateSetting("reverse",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchesOnAction")
                app.removeSetting("switchesOffAction")
                app.removeSetting("switchesToggleAction")
                app.removeSetting("switchesLCAction")
                app?.updateSetting("switchedDimUpAction",[value:"false",type:"bool"])
                app?.updateSetting("switchedDimDnAction",[value:"false",type:"bool"])
                app?.updateSetting("reverse",[value:"false",type:"bool"])
                app?.updateSetting("permanentDim",[value:"false",type:"bool"])
                app.removeSetting("permanentDimLvl")
                
            }
            
            if(actionType.contains("aValve")) {
                paragraph "<b>Valves</b>"
                input "valveClosedAction", "capability.valve", title: "Close Devices", multiple:true, submitOnChange:true
                input "valveOpenAction", "capability.valve", title: "Open Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
            } else {
                app.removeSetting("valveClosedAction")
                app.removeSetting("valveOpenAction")
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
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Enable extra logging for debugging.", submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required: false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours"]
                paragraph "<hr>"
                input "testButton", "button", title: "Test Event"
            }
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
                paragraph "%time% - will speak the current time in 24 h<br>%time1% - will speak the current time in 12 h"
                
                if(triggerType.contains("xBattery") ||  triggerType.contains("xHumidity") ||  triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp")) {
                    paragraph "<b>Setpoint Message Options</b>"
                    input "messageH", "text", title: "Message to speak when reading is too high", required: false, submitOnChange: true
                    input "messageL", "text", title: "Message to speak when reading is too low", required: false, submitOnChange: true
                    input "messageB", "text", title: "Message to speak when reading is both too high and too low", required: false
                } else {
                    app.removeSetting("messageH")
                    app.removeSetting("messageL")
                    app.removeSetting("messageB")
                }
                
                if(!triggerType.contains("xBattery") ||  !triggerType.contains("xHumidity") && !triggerType.contains("xIlluminance") && !triggerType.contains("xPower") && !triggerType.contains("xTemp")) {
                    paragraph "<b>Random Message Options</b>"
                    input "message", "text", title: "Message to be spoken/pushed - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                    input "msgList", "bool", defaultValue:false, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
                    if(msgList) {
                        def values = "${message}".split(";")
                        listMap = ""
                        values.each { item -> listMap += "${item}<br>"}
                        paragraph "${listMap}"
                    }
                } else {
                    app.removeSetting("message")  
                }
            }
        } else {
            app.removeSetting("message")
            app.removeSetting("messageH")
            app.removeSetting("messageL")
            app.removeSetting("messageB")
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashOnHomePreset", "number", title: "Select the Preset to use when someone comes home (1..5)", required:true, submitOnChange:true
                input "flashOnDepPreset", "number", title: "Select the Preset to use when someone leaves (1..5)", required:true, submitOnChange:true
            } else {
                app.removeSetting("theFlasherDevice")
                app.removeSetting("flashOnHomePreset")
                app.removeSetting("flashOnDepPreset")
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
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite: false])
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
        if(accelerationEvent) subscripe(accelerationEvent, "accelerationSensor", startTheProcess)
        if(batteryEvent) subscribe(batteryEvent, "battery", startTheProcess)
        if(contactEvent) subscribe(contactEvent, "contact", startTheProcess)
        if(garagedoorEvent) subscribe(garagedoorEvent, "door", startTheProcess)
        if(hsmAlertEvent) subscribe(location, "hsmAlert", startTheProcess)
        if(hsmStatusEvent) subscribe(location, "hsmStatus", startTheProcess)
        if(humidityEvent) subscribe(humidityEvent, "humidity", startTheProcess)
        if(illuminanceEvent) subscribe(illuminanceEvent, "illuminanceEvent", startTheProcess)
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
            if(logEnable) log.debug "In initialize - xPeriodic - Starting! - (${state.theSchedule})"
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
        
        if(evt) {
            def whoHappened = evt.displayName
            def whatHappened = evt.value
            if(logEnable) log.trace "******************** In startTheProcess - ${app.label} - ${whoHappened}: ${whatHappened} ********************"
            state.hasntDelayedYet = true
        }
        
        if(runTest) {
            state.timeBetween = true
            state.timeBetweenSun = true
            state.daysMatch = true
            state.modeStatus = true
            state.setPointGood = true
            state.devicesGood = true
            if(logEnable) log.warn "*********** RUNNING TEST ***********"
        } else {
            state.setPointGood = true
            state.devicesGood = true

            checkTime()
            checkTimeSun()
            accelerationHandler()
            batteryHandler()
            contactHandler()
            dayOfTheWeekHandler()
            garageDoorHandler()
            hsmAlertHandler(whatHappened)
            hsmStatusHandler(whatHappened)
            humidityHandler()
            illuminanceHandler()
            modeHandler()
            motionHandler() 
            powerHandler()
            presenceHandler()
            switchHandler()
            tempHandler()
            waterHandler()
        }
        
        if(logEnable) log.debug "In startTheProcess - checkTime: ${state.timeBetween} - checkTimeSun: ${state.timeBetweenSun} - daysMatch: ${state.daysMatch} - modeStatus: ${state.modeStatus} - setPointGood: ${state.setPointGood} - devicesGood: ${state.devicesGood}"
        
        if(state.timeBetween && state.timeBetweenSun && state.daysMatch && state.modeStatus && state.setPointGood && state.devicesGood) {            
            if(logEnable) log.debug "In startTheProcess - Everything is GOOD - Here we go!"

            if(state.hasntDelayedYet == null) state.hasntDelayedYet = false
            if((notifyDelay || randomDelay || targetDelay) && state.hasntDelayedYet) {
                if(notifyDelay) newDelay = notifyDelay
                // Math.abs(new Random().nextInt() % ([UPPER_LIMIT] - [LOWER_LIMIT])) + [LOWER_LIMIT]
                if(randomDelay) newDelay = Math.abs(new Random().nextInt() % (delayHigh - delayLow)) + delayLow
                if(targetDelay) newDelay = minutesUp
                if(logEnable) log.debug "In startTheProcess - Delay is set for ${newDelay} minutes"
                if(actionType.contains("aSwitch") && switchedDimUpAction) { slowOnHandler() }
                int theDelay = newDelay * 60
                state.hasntDelayedYet = false
                runIn(theDelay, startTheProcess)
            } else {     
                if(actionType == null) actionType = ""
                if(actionType.contains("aGarageDoor") && (garageDoorOpenAction || garageDoorClosedAction)) { garageDoorActionHandler() }
                if(actionType.contains("aLock") && (lockAction || unlockAction)) { lockActionHandler() }
                if(actionType.contains("aValve") && (valveOpenAction || valveClosedAction)) { valveActionHandler() }
                
                if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnActionHandler() }
                if(actionType.contains("aSwitch") && switchesOffAction && permanentDim) { permanentDimHandler() }
                if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim) { switchesOffActionHandler() }
                if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
                if(actionType.contains("aSwitch") && switchesLCAction) { dimmerOnActionHandler() }
                if(actionType.contains("aSwitch") && switchedDimDnAction) { slowOffHandler() }
                if(targetDelay == false) { 
                    if(actionType.contains("aSwitch") && switchedDimUpAction) {
                        slowOnHandler()
                    } 
                }

                if(actionType.contains("aNotification")) { messageHandler() }

                if(setHSM) hsmChangeActionHandler()
                if(modeAction) modeChangeActionHandler()
                if(devicesToRefresh) devicesToRefreshActionHandler()
                if(rmRule) ruleMachineHandler()

                state.hasntDelayedYet = true
            }
        } else if(reverse) {
            if(logEnable) log.debug "In startTheProcess - Going in REVERSE"
            if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnReverseActionHandler() }
            if(actionType.contains("aSwitch") && switchesOffAction && permanentDimLvl) { permanentDimHandler() }
            if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim) { switchesOffReverseActionHandler() }
            if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
            if(actionType.contains("aSwitch") && switchesLCAction && permanentDim) { permanentDimHandler() }
            if(actionType.contains("aSwitch") && switchesLCAction && !permanentDim) { dimmerOnReverseActionHandler() }
            
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

def accelerationHandler() {
    if(accelerationEvent) {
        state.eventName = accelerationEvent
        state.eventType = "acceleration"
        state.type = asInactiveActive
        state.typeValue1 = "active"
        state.typeValue2 = "inactive"
        state.typeAO = accelerationANDOR
        devicesGoodHandler()
    } 
}

def contactHandler() {
    if(contactEvent) {
        state.eventName = contactEvent
        state.eventType = "contact"
        state.type = csClosedOpen
        state.typeValue1 = "open"
        state.typeValue2 = "closed"
        state.typeAO = contactANDOR
        devicesGoodHandler()
    } 
}

def garageDoorHandler() {
    if(garageDoorEvent) {
        state.eventName = garageDoorEvent
        state.eventType = "door"
        state.type = gdClosedOpen
        state.typeValue1 = "open"
        state.typeValue2 = "closed"
        state.typeAO = garageDoorANDOR
        devicesGoodHandler()
    } 
}

def lockHandler() {
    if(lockEvent) {
        state.eventName = lockEvent
        state.eventType = "lock"
        state.type = lUnlockedLocked
        state.typeValue1 = "locked"
        state.typeValue2 = "unlocked"
        state.typeAO = lockANDOR
        devicesGoodHandler()
    } 
}

def motionHandler() {
    if(motionEvent) {
        state.eventName = motionEvent
        state.eventType = "motion"
        state.type = meInactiveActive
        state.typeValue1 = "active"
        state.typeValue2 = "inactive"
        state.typeAO = motionANDOR
        devicesGoodHandler()
    } 
}

def presenceHandler() {
    if(presenceEvent) {
        state.eventName = presenceEvent
        state.eventType = "presence"
        state.type = pePresentNotPresent
        state.typeValue1 = "not present"
        state.typeValue2 = "present"
        state.typeAO = presenceANDOR
        devicesGoodHandler()
    } 
}

def switchHandler() {
    if(switchEvent) {
        state.eventName = switchEvent
        state.eventType = "switch"
        state.type = seOffOn
        state.typeValue1 = "on"
        state.typeValue2 = "off"
        state.typeAO = switchANDOR
        devicesGoodHandler()
    } 
}

def waterHandler() {
    if(waterEvent) {
        state.eventName = waterEvent
        state.eventType = "water"
        state.type = weDryWet
        state.typeValue1 = "Wet"
        state.typeValue2 = "Dry"
        state.typeAO = waterANDOR
        devicesGoodHandler()
    } 
}

def devicesGoodHandler() {
    if(logEnable) log.debug "In devicesGoodHandler (${state.version}) - ${state.eventType.toUpperCase()}" 
    state.devicesGood = false
    deviceTrue = 0
    theCount = state.eventName.size()

    state.eventName.each {
        theValue = it.currentValue("${state.eventType}")
        if(logEnable) log.debug "In devicesGoodHandler - Checking: ${it.displayName} - Testing for typeValue1: ${state.typeValue1} - device is ${theValue}"
        if(state.type) {
            if(theValue == state.typeValue1) { deviceTrue = deviceTrue + 1 }              
        }
        if(!state.type) {
            if(theValue == state.typeValue2) { deviceTrue = deviceTrue + 1 }
        }
    }
    if(logEnable) log.debug "In devicesGoodHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue}" 
    if(state.typeAO) {
        if(deviceTrue >= 1) { state.devicesGood = true }           // OR
    } else {
        if(deviceTrue == theCount) { state.devicesGood = true }    // AND
    }
    if(logEnable) log.debug "In devicesGoodHandler - ${state.eventType.toUpperCase()} - devicesGood: ${state.devicesGood}"
}

def hsmAlertHandler(data) {
    if(hsmAlertEvent) {
        if(logEnable) log.debug "In hsmAlertHandler (${state.version})"
        state.hsmAlertStatus = false
        String theValue = data
        
        hsmAlertEvent.each { it ->
            if(logEnable) log.debug "In hsmAlertHandler - Checking: ${it} - value: ${theValue}"

            if(theValue == it){
                state.hsmAlertStatus = true
            }
        }
    } else {
        state.hsmAlertStatus = true
    }
    if(logEnable) log.debug "In hsmAlertHandler - hsmAlertStatus: ${state.hsmAlertStatus}"
}

def hsmStatusHandler(data) {
    if(hsmStatusEvent) {
        if(logEnable) log.debug "In hsmStatusHandler (${state.version})"
        state.hsmStatus = false
        String theValue = data
        
        hsmStatusEvent.each { it ->
            if(logEnable) log.debug "In hsmStatusHandler - Checking: ${it} - value: ${theValue}"

            if(theValue == it){
                state.hsmStatus = true
            }
        }
    } else {
        state.hsmStatus = true
    }
    if(logEnable) log.debug "In hsmStatusHandler - hsmStatus: ${state.hsmStatus}"
}

def modeHandler() {
    if(modeEvent) {
        if(logEnable) log.debug "In modeHandler (${state.version})"
        state.modeStatus = false

        modeEvent.each { it ->
            theValue = location.mode
            if(logEnable) log.debug "In modeHandler - Checking: ${it} - value: ${theValue}"

            if(theValue == it){
                if(modeOnOff) {
                    if(theValue) { state.modeStatus = true }
                }
                if(!modeOnOff) {
                    if(!theValue) { state.modeStatus = true }
                }
            }
        }
    } else {
        state.modeStatus = true
    }
    if(logEnable) log.debug "In modeHandler - modeStatus: ${state.modeStatus}"
}

def ruleMachineHandler() {
    if(logEnable) log.debug "In ruleMachineHandler - Rule: ${rmRule} - Action: ${rmAction}"
    RMUtils.sendAction(rmRule, rmAction, app.label)
}

// ***** Start Setpoint Handlers *****

def batteryHandler() {
    if(batteryEvent) {
        state.spName = batteryEvent
        state.spType = "battery"
        state.setPointHigh = beSetPointHigh
        state.setPointLow = beSetPointLow
        setPointHandler()
    } 
}

def humidityHandler() {
    if(humidityEvent) {
        state.spName = humidityEvent
        state.spType = "humidity"
        state.setPointHigh = heSetPointHigh
        state.setPointLow = heSetPointLow
        setPointHandler()
    } 
}

def illuminanceHandler() {
    if(illuminanceEvent) {
        state.spName = illuminanceEvent
        state.spType = "illuminance"
        state.setPointHigh = ieSetPointHigh
        state.setPointLow = ieSetPointLow
        setPointHandler()
    } 
}

def powerHandler() {
    if(powerEvent) {
        state.spName = powerEvent
        state.spType = "power"
        state.setPointHigh = peSetPointHigh
        state.setPointLow = peSetPointLow
        setPointHandler()
    } 
}

def tempHandler() {
    if(tempEvent) {
        state.spName = tempEvent
        state.spType = "temperature"
        state.setPointHigh = teSetPointHigh
        state.setPointLow = teSetPointLow
        setPointHandler()
    } 
}

def setPointHandler() {
    if(logEnable) log.debug "In setPointHandler (${state.version})" 
    state.setPointGood = false
    log.trace "spName: ${state.spName}"
    state.spName.each {
        setPointValue = it.currentValue("${state.spType}")
        if(logEnable) log.debug "In setPointHandler - Working on: ${it} - setPointValue: ${setPointValue}"
        setPointValue1 = setPointValue.toDouble()

        // *** setPointHigh ***
        if(state.setPointHigh && !state.setPointLow) {
            state.setPointLowOK = "yes"
            if(setPointValue1 > state.setPointHigh) {
                if(state.setPointHighOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Hgh) - Device: ${it}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${state.setPointHigh}"
                    state.setPointHighOK = "no"
                    state.setPointGood = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (High) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 < state.setPointHigh) {
                if(state.setPointHighOK == "no") {
                    if(logEnable) log.debug "In setPointHandler (High) - Device: ${it}, Actual value: ${setPointValue1} is Less THAN setPointHigh: ${state.setPointHigh}"
                    state.setPointHighOK = "yes"
                    state.setPointGood = false
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }

        // *** setPointLow ***
        if(state.setPointLow && !state.setPointHigh) {
            state.setPointHighOK = "yes"
            if(setPointValue1 < state.setPointLow) {
                if(state.setPointLowOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${it}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${state.setPointLow}"
                    state.setPointLowOK = "no"
                    state.setPointGood = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 > state.setPointLow) {
                if(state.setPointLowOK == "no") {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${it}, Actual value: ${setPointValue1} is GREATER THAN setPointLow: ${state.setPointLow}"
                    state.setPointLowOK = "yes"
                    state.setPointGood = false
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }

        // *** Inbetween ***
        if(state.setPointHigh && state.setPointLow) {
            if(setPointValue1 > state.setPointHigh) {
                if(state.setPointHighOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${it}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${state.setPointHigh}"
                    state.setPointHighOK = "no"
                    state.setPointGood = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 < state.setPointLow) {
                if(state.setPointLowOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${it}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${state.setPointLow}"
                    state.setPointLowOK = "no"
                    state.setPointGood = true
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if((setPointValue1 <= state.setPointHigh) && (setPointValue1 >= state.setPointLow)) {
                if(state.setPointHighOK == "no" || state.setPointLowOK == "no") {
                    if(logEnable) log.debug "InsetPointHandler (Both) - Device: ${it}, Actual value: ${setPointValue1} is BETWEEN tempHigh: ${state.setPointHigh} and setPointLow: ${state.setPointLow}"
                    state.setPointHighOK = "yes"
                    state.setPointLowOK = "yes"
                    state.setPointGood = false
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both) - Device: ${it}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
    }
    if(logEnable) log.debug "In setPointHandler - ${state.spType.toUpperCase()} - setPointGood: ${state.setPointGood}"
}

// ********** Start Actions **********

def dimmerOnActionHandler() {
	if(logEnable) log.debug "In dimmerOnActionHandler (${state.version})"
	state.fromWhere = "dimmerOn"
	state.color = "${colorLC}"
    state.onLevel = levelLC
	setLevelandColorHandler()
}

def dimmerOnReverseActionHandler() {
    if(switchesLCAction) {
        setOnLC.each { it ->
            if(logEnable) log.debug "In dimmerOnReverseActionHandler - Turning off ${it}"
            it.off()
        }
    }
}

def permanentDimHandler() {
    if(switchesLCAction) {
        setOnLC.each { it ->
            if(logEnable) log.debug "In permanentDimHandler - Set Level on ${it} to ${permanentDimLvl}"
            it.setLevel(permanentDimLvl)
        }
    }
    
    if(switchesOnAction) {
        switchesOnAction.each { it ->
            if(it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In permanentDimHandler - Set Level on ${it} to ${permanentDimLvl}"
                it.setLevel(permanentDimLvl)
            }
        }
    }
}

def devicesToRefreshActionHandler() {
    devicesToRefresh.each { it ->
        if(logEnable) log.debug "In devicesToRefreshActionHandler - Refreshing ${it}"
        it.refresh()
    }
}

def garageDoorActionHandler() {
    if(logEnable) log.debug "In garageDoorActionHandler (${state.version})"
    if(garageDoorClosedAction) {
        garageDoorClosedAction.each { it ->
            if(logEnable) log.debug "In garageDoorActionHandler - Closing ${it}"
            it.close()
        }
    }
    
    if(garageDoorOpenAction) {
        garageDoorClosedAction.each { it ->
            if(logEnable) log.debug "In garageDoorActionHandler - Open ${it}"
            it.open()
        }
    }
}

def hsmChangeActionHandler() {
    if(logEnable) log.debug "In hsmChangeActionHandler (${state.version}) - Setting to ${setHSM}"
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

def modeChangeActionHandler() {
	if(logEnable) log.debug "In modeChangeActionHandler - Changing mode to ${modeAction}"
	setLocationMode(modeAction)
}

def switchesOnActionHandler() {
	switchesOnAction.each { it ->
		if(logEnable) log.debug "In switchesOnActionHandler - Turning on ${it}"
		it.on()
	}
}

def switchesOnReverseActionHandler() {
	switchesOnAction.each { it ->
		if(logEnable) log.debug "In switchesOnReverseActionHandler - Turning off ${it}"
		it.off()
	}
}

def switchesOffActionHandler() {
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchesOffActionHandler - Turning off ${it}"
        it.off()
    }
}

def switchesOffReverseActionHandler() {
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchesOffReverseActionHandler - Turning on ${it}"
        it.on()
    }
}

def switchesToggleActionHandler() {
	switchesToggleAction.each { it ->
        status = it.currentValue("switch")
        if(status == "off") {
            if(logEnable) log.debug "In switchesToggleActionHandler - Turning on ${it}"
            it.on()
        } else {
            if(logEnable) log.debug "In switchesToggleActionHandler - Turning off ${it}"
            it.off()
        }
	}
}

def slowOnHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In slowOnHandler (${state.version})"
        state.fromWhere = "slowOn"
        state.currentLevel = startLevelHigh ?: 1
        state.onLevel = state.currentLevel
        state.color = "${colorUp}"
        setLevelandColorHandler()
        if(minutesUp == 0) return
        seconds = (minutesUp * 60) - 10
        difference = targetLevelHigh - state.currentLevel
        state.dimStep = (difference / seconds) * 10
        if(logEnable) log.debug "In slowOnHandler - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh} - color: ${state.color}"
        atLeastOneUpOn = false
        runIn(5,dimStepUp)
    }
}

def slowOffHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In slowOffHandler (${state.version})"
        state.fromWhere = "slowOff"

        if(useMaxLevel) {
            findHighestCurrentValue()
        } else {            
            state.highestLevel = startLevelLow ?: 99    
        }

        state.color = "${colorDn}"
        setLevelandColorHandler()
        if(minutesDn == 0) return
        seconds = (minutesDn * 60) - 10           
        difference = state.highestLevel - targetLevelLow                
        state.dimStep1 = (difference / seconds) * 10
        if(logEnable) log.debug "slowOffHandler - highestLevel: ${state.highestLevel} - targetLevel: ${targetLevelLow} - dimStep1: ${state.dimStep1} - color: ${state.color}"
        atLeastOneDnOn = false
        runIn(5,dimStepDown)
    }
}

def findHighestCurrentValue() {
    if(logEnable) log.debug "In findHighestCurrentValue (${state.version})"
    state.highestLevel = 0
    
    slowDimmerDn.each { it->
        checkLevel = it.currentValue("level")
        if(checkLevel > state.highestLevel) state.highestLevel = checkLevel
    }
    
    if(logEnable) log.debug "In findHighestCurrentValue - highestLevel: ${state.highestLevel})"
}

def dimStepUp() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "-------------------- dimStepUp --------------------"
        if(logEnable) log.debug "In dimStepUp (${state.version})"

        if(state.currentLevel < targetLevelHigh) {
            state.currentLevel = state.currentLevel + state.dimStep
            if(state.currentLevel > targetLevelHigh) { state.currentLevel = targetLevelHigh }
            if(logEnable) log.debug "In dimStepUp - Setting currentLevel: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"

            slowDimmerUp.each { it->
                deviceOn = it.currentValue("switch")
                if(logEnable) log.debug "In dimStepUp - ${it} is: ${deviceOn}"
                if(deviceOn == "on") {
                    atLeastOneUpOn = true
                    it.setLevel(state.currentLevel)
                }
            }

            if(atLeastOneUpOn) {
                runIn(10,dimStepUp)
            } else {
                log.info "${app.label} - All devices are turned off"
            }    
        } else {
            if(logEnable) log.debug "-------------------- End dimStepUp --------------------"
            if(logEnable) log.info "In dimStepUp - Current Level: ${state.currentLevel} has reached targetLevel: ${targetLevelHigh}"
        }
    }
}

def dimStepDown() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "-------------------- dimStepDown --------------------"
        if(logEnable) log.debug "In dimStepDown (${state.version})"

        if(state.highestLevel > targetLevelLow) {
            state.highestLevel = state.highestLevel - state.dimStep1                   
            if(state.highestLevel < targetLevelLow) { state.highestLevel = targetLevelLow }                   
            if(logEnable) log.debug "In dimStepDown - Starting Level: ${state.highestLevel} - targetLevelLow: ${targetLevelLow}"

            slowDimmerDn.each { it->
                deviceOn = it.currentValue("switch")
                int cLevel = it.currentValue("level")
                int wLevel = state.highestLevel

                if(logEnable) log.debug "In dimStepDown - ${it} is: ${deviceOn} - cLevel: ${cLevel} - wLevel: ${wLevel}"
                if(deviceOn == "on") {
                    atLeastOneDnOn = true
                    if(wLevel <= cLevel) { it.setLevel(wLevel) }
                }
            }

            if(atLeastOneDnOn) {
                runIn(10,dimStepDown)
            } else {
                log.info "${app.label} - All devices are turned off"
            }    
        } else {
            if(dimDnOff) slowDimmerDn.off()
            if(logEnable) log.debug "-------------------- End dimStepDown --------------------"
            if(logEnable) log.info "In dimStepDown - Current Level: ${state.currentLevel} has reached targetLevel: ${targetLevelLow}"
        } 
    }
}

def valveActionHandler() {
    if(logEnable) log.debug "In valveActionHandler (${state.version})"
    if(valveClosedAction) {
        valveClosedAction.each { it ->
            if(logEnable) log.debug "In valveActionHandler - Closing ${it}"
            it.close()
        }
    }
    
    if(valveOpenAction) {
        valveClosedAction.each { it ->
            if(logEnable) log.debug "In valveActionHandler - Open ${it}"
            it.open()
        }
    }
}

// ********** End Actions **********

def messageHandler() {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    
    if(triggerType.contains("xBattery") ||  triggerType.contains("xHumidity") ||  triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp")) {
        if(logEnable) log.debug "In messageHandler (setpoint) - setPointHighOK: ${state.setPointHighOK} - setPointLowOK: ${state.setPointLowOK}"
        if(state.setPointHighOK == "no") theMessage = "${messageH}"
        if(state.setPointLowOK == "no") theMessage = "${messageL}"
        if((state.setPointHighOK == "no") && (state.setPointLowOK == "no")) theMessage = "${messageB}"
    } else {
        if(logEnable) log.debug "In messageHandler - Random - raw message: ${message}"
        def values = "${message}".split(";")
        vSize = values.size()
        count = vSize.toInteger()
        def randomKey = new Random().nextInt(count)

        theMessage = values[randomKey]
        if(logEnable) log.debug "In messageHandler - Random - theMessage: ${theMessage}" 
    }
    
    state.message = theMessage
   
	if (state.message.contains("%time%")) {state.message = state.message.replace('%time%', state.theTime)}
	if (state.message.contains("%time1%")) {state.message = state.message.replace('%time1%', state.theTime1)}
    
    if(logEnable) log.debug "In messageHandler - message: ${state.message}"
    msg = state.message
    if(useSpeech) letsTalk(msg)
    if(sendPushMessage) pushHandler(msg)
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(msg)
    }
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
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

def checkTimeSun() {
    // checkTimeSun - This is to ensure that the it's BETWEEN sunset/sunrise with offsets
	if(logEnable) log.debug "In checkTimeSun (${state.version})"
    if(sunRestriction) {    
        def sunriseTime = location.sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        def sunsetTime = location.sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        
        nextSunset = toDateTime(sunsetTime)
        nextSunrise = toDateTime(sunriseTime)+1
        
        int theOffsetSunset = offsetSunset ?: 1    
        if(setBeforeAfter) {
            use( TimeCategory ) { nextSunsetOffset = nextSunset + theOffsetSunset.minutes }
        } else {
            use( TimeCategory ) { nextSunsetOffset = nextSunset - theOffsetSunset.minutes }
        }
        
        int theOffsetSunrise = offsetSunrise ?: 1
        if(riseBeforeAfter) {
            use( TimeCategory ) { nextSunriseOffset = nextSunrise + theOffsetSunrise.minutes }
        } else {
            use( TimeCategory ) { nextSunriseOffset = nextSunrise - theOffsetSunrise.minutes }
        }

        if(logEnable) log.debug "In checkTimeSun - nextSunset: ${nextSunset} - nextSunsetOffset: ${nextSunsetOffset}"
        if(logEnable) log.debug "In checkTimeSun - nextSunrise: ${nextSunrise} - nextSunriseOffset: ${nextSunriseOffset}"
        
        state.timeBetweenSun = timeOfDayIsBetween(nextSunsetOffset, nextSunrise, new Date(), location.timeZone)

        if(logEnable) log.debug "In checkTimeSun - nextSunsetOffset: ${nextSunsetOffset} - nextSunriseOffset: ${nextSunriseOffset}"
        
		if(state.timeBetweenSun) {
            if(logEnable) log.debug "In checkTimeSun - Time within range"
			state.timeBetweenSun = true
		} else {
            if(logEnable) log.debug "In checkTimeSun - Time outside of range"
			state.timeBetweenSun = false
		}
        if(logEnable) log.debug "In checkTimeSun - timeBetweenSun: ${state.timeBetweenSun}"
  	} else {
		state.timeBetweenSun = true
        if(logEnable) log.debug "In checkTimeSun - timeBetweenSun: ${state.timeBetweenSun}"
  	}
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version})"
	if(fromTime) {
        if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
        if(midnightCheckR) {
            state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime)+1, new Date(), location.timeZone)
        } else {
		    state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
        }
		if(state.betweenTime) {
            if(logEnable) log.debug "In checkTime - Time within range"
			state.timeBetween = true
		} else {
            if(logEnable) log.debug "In checkTime - Time outside of range"
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
	if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, color: ${state.color}"
    def hueColor = 0
    def saturation = 100
    
    if(state.fromWhere == "slowOff") {
        state.onLevel = state.highestLevel
    } else {
	    state.onLevel = state.onLevel ?: 99
    }
    
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
    
    int onLevel = state.onLevel
	def value = [switch: "on", hue: hueColor, saturation: saturation, level: onLevel]
    if(logEnable) log.debug "In setLevelandColorHandler - value: $value"
    
	if(state.fromWhere == "dimmerOn") {
        if(logEnable) log.debug "In setLevelandColorHandler - dimmerOn - setOnLC: ${setOnLC}"
    	setOnLC.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 99)
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
            	it.setLevel(onLevel as Integer ?: 99)
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
            	it.setLevel(level as Integer ?: 99)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    
    if(state.whichButton == "testButton"){
        log.debug "In appButtonHandler - testButton"
        runTest = true
        startTheProcess()
        runTest = false
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
