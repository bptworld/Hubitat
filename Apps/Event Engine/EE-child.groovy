/**
 *  ****************  Event Engine Cog App  ****************
 *
 *  Design Usage:
 *  Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!
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
 *  1.6.4 - 09/20/20 - Minor change for testing
 *  1.6.3 - 09/20/20 - More logging
 *  1.6.2 - 09/20/20 - adjustments to Devices, NEW - Custom Attribute Trigger option 
 *  1.6.1 - 09/20/20 - Changes to Mode - again
 *  ---
 *  1.0.0 - 09/05/20 - Initial release.
 *
 */

import groovy.json.*
import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Event Engine"
	state.version = "1.6.4"
}

definition(
    name: "Event Engine Cog",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!",
    category: "Convenience",
	parent: "BPTWorld:Event Engine",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Event%20Engine/EE-child.groovy",
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
            paragraph "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!"
            paragraph "Please <a href='https://docs.google.com/document/d/1QtIsAKUb9vzAZ1RWTQR2SfxaZFvuLO3ePxDxmH-VU48/edit?usp=sharing' target='_blank'>visit this link</a> for a breakdown of all Event Engine Options. (Google Docs)"
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Select Triggers")) {
            input "triggerType", "enum", title: "Trigger Type", options: [
                ["xPeriodic":"Periodic"],
                ["xTimeDays":"Time/Days - Sub-Menu"],
                ["xAcceleration":"Acceleration Sensor"],
                ["xBattery":"Battery Setpoint"],
                ["xContact":"Contact Sensors"],
                ["xEnergy":"Energy Setpoint"],
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
                ["xVoltage":"Voltage Setpoint"],
                ["xWater":"Water Sensor"],
                ["xCustom":"** Custom Attribute **"]
            ], required: true, multiple:true, submitOnChange:true

            input "triggerAndOr", "bool", title: "Use 'AND' or 'OR' between Trigger types", description: "andOr", defaultValue:false, submitOnChange:true
            if(triggerAndOr) {
                paragraph "Cog will fire when <b>ANY</b> trigger is true"
            } else {
                paragraph "Cog will fire when <b>ALL</b> triggers are true"
            }
            paragraph "<hr>"
            if(triggerType == null) triggerType = ""

            if(triggerType.contains("xPeriodic")) {
                input "preMadePeriodic", "text", title: "Enter in a premade Periodic Cron Expression", required:false, submitOnChange:true

                paragraph "Create your own Expressions using the 'Periodic Expressions' app found in Hubitat Package Manager or on <a href='https://github.com/bptworld/Hubitat/' target='_blank'>my GitHub</a>."
                paragraph "<hr>"
                paragraph "Premade cron expressions can be found at <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a>. Remember, Format and spacing is critical."
            } else {
                app.removeSetting("preMadePeriodic")
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
                    
                    paragraph "By Days can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this trigger is false."
                    input "daysMatchRestriction", "bool", defaultValue: false, title: "By Days as Restriction", description: "By Days Restriction", submitOnChange:true
                    paragraph "<hr>"
                } else {
                    app.removeSetting("days")
                    app?.updateSetting("daysMatchRestriction",[value:"false",type:"bool"])
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
                    
                    paragraph "Between two times can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this trigger is false."
                    input "timeBetweenRestriction", "bool", defaultValue: false, title: "Between two times as Restriction", description: "Between two times Restriction", submitOnChange:true
                    paragraph "<hr>"
                } else {
                    app.removeSetting("fromTime")
                    app.removeSetting("toTime")
                    app?.updateSetting("midnightCheckR",[value:"false",type:"bool"])
                    app?.updateSetting("timeBetweenRestriction",[value:"false",type:"bool"])
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
                        
                        paragraph "Sunset to Sunrise can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this trigger is false."
                        input "timeBetweenSunRestriction", "bool", defaultValue: false, title: "Sunset to Sunrise as Restriction", description: "Sunset to Sunrise Restriction", submitOnChange:true
                        paragraph "<hr>"
                    }
                } else {
                    app.removeSetting("offsetSunset")
                    app.removeSetting("offsetSunrise")
                    app?.updateSetting("setBeforeAfter",[value:"false",type:"bool"])
                    app?.updateSetting("riseBeforeAfter",[value:"false",type:"bool"])
                    app?.updateSetting("timeBetweenSunRestriction",[value:"false",type:"bool"])
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
                    
                    input "sunriseToTime", "bool", title: "Set a certain time to turn off", defaultValue:false, submitOnChange:true
                    if(sunriseToTime) {
                        input "sunriseEndTime", "time", title: "Time to End", description: "Time", required: false
                        paragraph "<small>* Must be BEFORE midnight.</small>"
                    } else {
                        app.removeSetting("sunriseEndTime")
                    }
                    paragraph "<hr>"
                } else if(timeDaysType.contains("tSunset")) {
                    paragraph "<b>Just Sunset</b>"
                    input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", defaultValue:false, submitOnChange:true, width:6
                    input "offsetSunset", "number", title: "Offset (minutes)", width:6
                    
                    input "sunsetToTime", "bool", title: "Set a certain time to turn off", defaultValue:false, submitOnChange:true
                    if(sunsetToTime) {
                        input "sunsetEndTime", "time", title: "Time to End", description: "Time", required: false
                        paragraph "<small>* Must be BEFORE midnight.</small>"
                    } else {
                        app.removeSetting("sunsetEndTime")
                    }                    
                    paragraph "<hr>"
                } else {
                    app.removeSetting("sunsetEndTime")
                    app.removeSetting("offsetSunrise")
                    app.removeSetting("offsetSunset")
                    app?.updateSetting("setBeforeAfter",[value:"false",type:"bool"])
                    app?.updateSetting("riseBeforeAfter",[value:"false",type:"bool"])                   
                }
            } else {
                app.removeSetting("timeDaysType")
            }

            if(triggerType.contains("xAcceleration")) {
                paragraph "<b>Acceleration Sensor</b>"
                input "accelerationEvent", "capability.accelerationSensor", title: "By Acceleration Sensor", required: false, multiple: true, submitOnChange: true
                if(accelerationEvent) {
                    input "asInactiveActive", "bool", title: "Trigger when Inactive (off) or Active (on)", description: "Acceleration", defaultValue:false, submitOnChange:true
                    if(asInactiveActive) {
                        paragraph "Trigger will fire when Sensor(s) becomes Active"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) becomes Inactive"
                    }
                    
                    input "accelerationANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(accelerationANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Acceleration Sensor is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Acceleration Sensors are true"
                    }
                } else {
                    app.removeSetting("accelerationEvent")
                    app?.updateSetting("asInactiveActive",[value:"false",type:"bool"])
                    app?.updateSetting("accelerationANDOR",[value:"false",type:"bool"])

                }
                
                input "accelerationRestrictionEvent", "capability.accelerationSensor", title: "Restrict By Acceleration Sensor", required: false, multiple: true, submitOnChange: true
                if(accelerationRestrictionEvent) {
                    input "arInactiveActive", "bool", title: "Restrict when Inactive (off) or Active (on)", description: "Acceleration", defaultValue:false, submitOnChange:true
                    if(arInactiveActive) {
                        paragraph "Restrict when Sensor(s) becomes Active"
                    } else {
                        paragraph "Restrict when Sensor(s) becomes Inactive"
                    }
                    
                    input "accelerationRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(accelerationRANDOR) {
                        paragraph "Restrict when <b>any</b> Acceleration Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Acceleration Sensors are true"
                    }
                } else {
                    app.removeSetting("accelerationRestrictionEvent")
                    app?.updateSetting("arInactiveActive",[value:"false",type:"bool"])
                    app?.updateSetting("accelerationRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("accelerationEvent")
                app.removeSetting("accelerationRestrictionEvent")
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
                    if(csClosedOpen) {
                        paragraph "Trigger will fire when Sensor(s) become Open"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) become Closed"
                    }
                    
                    input "contactANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(contactANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Contact Sensor is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Contact Sensors are true"
                    }
                } else {
                    app.removeSetting("contactEvent")
                    app?.updateSetting("csClosedOpen",[value:"false",type:"bool"])
                    app?.updateSetting("contactANDOR",[value:"false",type:"bool"])
                }
                
                input "contactRestrictionEvent", "capability.contactSensor", title: "Restrict By Contact Sensor", required: false, multiple: true, submitOnChange: true
                if(contactRestrictionEvent) {
                    input "crClosedOpen", "bool", title: "Restrict when Closed (off) or Opened (on)", description: "Contact", defaultValue:false, submitOnChange:true
                    if(crClosedOpen) {
                        paragraph "Restrict when Sensor(s) become Open"
                    } else {
                        paragraph "Restrict when Sensor(s) become Closed"
                    }
                    
                    input "contactRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(contactRANDOR) {
                        paragraph "Restrict when <b>any</b> Contact Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Contact Sensors are true"
                    }
                } else {
                    app.removeSetting("contactRestrictionEvent")
                    app?.updateSetting("crClosedOpen",[value:"false",type:"bool"])
                    app?.updateSetting("contactRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("contactEvent")
                app.removeSetting("contactRestrictionEvent")
            }

            if(triggerType.contains("xEnergy")) {
                paragraph "<b>Energy</b>"
                input "energyEvent", "capability.powerMeter", title: "By Energy Setpoints", required:false, multiple:true, submitOnChange:true
                if(energyEvent) {
                    input "setEEPointHigh", "bool", defaultValue: "false", title: "Trigger when Energy is too High?", description: "Energy High", submitOnChange:true
                    if(setEEPointHigh) {
                        input "eeSetPointHigh", "number", title: "Energy High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("eeSetPointHigh")
                    }

                    input "setEEPointLow", "bool", defaultValue:false, title: "Trigger when Energy is too Low?", description: "Energy Low", submitOnChange:true
                    if(setEEPointLow) {
                        input "eeSetPointLow", "number", title: "Energy Low Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("eeSetPointLow")
                    }

                    if(eeSetPointHigh) paragraph "You will receive notifications if Energy reading is above ${eeSetPointHigh}"
                    if(eeSetPointLow) paragraph "You will receive notifications if Energy reading is below ${eeSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("energyEvent")
                app.removeSetting("eeSetPointHigh")
                app.removeSetting("eeSetPointLow")
                app?.updateSetting("setEEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setEEPointLow",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorEvent", "capability.garageDoorControl", title: "By Garage Door", required: false, multiple: true, submitOnChange: true
                if(garageDoorEvent) {
                    input "gdClosedOpen", "bool", title: "Trigger when Closed (off) or Open (on)", description: "Garage Door", defaultValue:false, submitOnChange:true
                    if(gdClosedOpen) {
                        paragraph "Trigger will fire when Sensor(s) become Open"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) become Closed"
                    }
                    
                    input "garageDoorANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Garage Door is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Garage Doors are true"
                    }
                } else {
                    app.removeSetting("garageDoorEvent")
                    app?.updateSetting("gdClosedOpen",[value:"false",type:"bool"])
                    app?.updateSetting("garageDoorANDOR",[value:"false",type:"bool"])
                }
                
                input "garageDoorRestrictionEvent", "capability.garageDoorControl", title: "Restrict By Garage Door", required: false, multiple: true, submitOnChange: true
                if(garageDoorRestrictionEvent) {
                    input "gdrClosedOpen", "bool", title: "Restrict when Closed (off) or Open (on)", description: "Garage Door", defaultValue:false, submitOnChange:true
                    if(gdrClosedOpen) {
                        paragraph "Restrict when Sensor(s) become Open"
                    } else {
                        paragraph "Restrict when Sensor(s) become Closed"
                    }
                    
                    input "garageDoorRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Restrict when <b>any</b> Garage Door is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Garage Doors are true"
                    }
                } else {
                    app.removeSetting("garageDoorRestrictionEvent")
                    app?.updateSetting("gdsClosedOpen",[value:"false",type:"bool"])
                    app?.updateSetting("garageDoorRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"   
            } else {
                app.removeSetting("garageDoorEvent")
                app.removeSetting("garageDoorRestrictionEvent")
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
                input "lockEvent", "capability.lock", title: "By Lock", required: false, multiple: false, submitOnChange: true
                if(lockEvent) {
                    input "lUnlockedLocked", "bool", title: "Trigger when Unlocked (off) or Locked (on)", description: "Lock", defaultValue:false, submitOnChange:true
                    if(lUnlockedLocked) {
                        paragraph "Trigger will fire when Sensor(s) become Locked"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) become Unlocked"
                    }
                
                    theNames = getLockCodeNames(lockEvent)
                    input "lockUser", "enum", title: "By Lock User", options: theNames, required: false, multiple: true, submitOnChange: true
                    
                    paragraph "<small>* Note: If you are using HubConnect and have this cog on a different hub than the Lock, the lock codes must not be encryted.</small>"
                } else {
                    app.removeSetting("lockUser")
                    app.removeSetting("lockEvent")
                    app?.updateSetting("lUnlockedLocked",[value:"false",type:"bool"])
                    app?.updateSetting("lockANDOR",[value:"false",type:"bool"])
                }
                
                input "lockRestrictionEvent", "capability.lock", title: "Restrict By Lock", required: false, multiple: false, submitOnChange: true
                if(lockRestrictionEvent) {
                    input "lrUnlockedLocked", "bool", title: "Restrict when Unlocked (off) or Locked (on)", description: "Lock", defaultValue:false, submitOnChange:true
                    if(lrUnlockedLocked) {
                        paragraph "Restrict when Sensor(s) become Locked"
                    } else {
                        paragraph "Restrict when Sensor(s) become Unlocked"
                    }
                
                    theNames = getLockCodeNames(lockRestrictionEvent)
                    input "lockRestrictionUser", "enum", title: "Restrict By Lock User", options: theNames, required: false, multiple: true, submitOnChange: true
                    
                    paragraph "<small>* Note: If you are using HubConnect and have this cog on a different hub than the Lock, the lock codes must not be encryted.</small>"
                } else {
                    app.removeSetting("lockRestrictionUser")
                    app.removeSetting("lockRestrictionEvent")
                    app?.updateSetting("lrUnlockedLocked",[value:"false",type:"bool"])
                    app?.updateSetting("lockRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>" 
            } else {
                app.removeSetting("lockEvent")
                app.removeSetting("lockRestrictionEvent")
            }

            if(triggerType.contains("xMode")) {
                paragraph "<b>Mode</b>"
                input "modeEvent", "mode", title: "By Mode", multiple:true, submitOnChange:true
                if(modeEvent) {
                    input "modeOnOff", "bool", defaultValue: false, title: "Mode Inactive (off) or Active (on)?", description: "Mode", submitOnChange:true
                    if(modeOnOff) paragraph "You will receive notifications if <b>any</b> of the modes are on."
                    if(!modeOnOff) paragraph "You will receive notifications if <b>any</b> of the modes are off."
                } else {
                    app.removeSetting("modeEvent")
                    app.removeSetting("modeOnOff")
                }
                
                input "modeRestrictionEvent", "mode", title: "Restrict By Mode", multiple:true, submitOnChange:true
                if(modeRestrictionEvent) {
                    input "modeROnOff", "bool", defaultValue: false, title: "Mode Inactive (off) or Active (on)?", description: "Mode", submitOnChange:true
                    if(modeROnOff) paragraph "Restrict if <b>any</b> of the modes are on."
                    if(!modeROnOff) paragraph "Restrict if <b>any</b> of the modes are off."
                } else {
                    app.removeSetting("modeRestrictionEvent")
                    app.removeSetting("modeROnOff")
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("modeEvent")
                app.removeSetting("modeRestrictionEvent")
            }

            if(triggerType.contains("xMotion")) {
                paragraph "<b>Motion</b>"
                input "motionEvent", "capability.motionSensor", title: "By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionEvent) {
                    input "meInactiveActive", "bool", defaultValue:false, title: "Motion Inactive (off) or Active (on)?", description: "Motion", submitOnChange:true
                    if(meInactiveActive) {
                        paragraph "Trigger will fire when Sensor(s) becomes Active"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) becomes Inactive"
                    }
                    
                    input "motionANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(motionANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Motion Sensor is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Motion Sensors are true"
                    }
                } else {
                    app.removeSetting("motionEvent")
                    app?.updateSetting("meInactiveActive",[value:"false",type:"bool"])
                    app?.updateSetting("motionANDOR",[value:"false",type:"bool"])
                }
                
                input "motionRestrictionEvent", "capability.motionSensor", title: "Restrict By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionRestrictionEvent) {
                    input "mrInactiveActive", "bool", defaultValue:false, title: "Motion Inactive (off) or Active (on)?", description: "Motion", submitOnChange:true
                    if(mrInactiveActive) {
                        paragraph "Restrict when Sensor(s) becomes Active"
                    } else {
                        paragraph "Restrict when Sensor(s) becomes Inactive"
                    }
                    
                    input "motionRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(motionRANDOR) {
                        paragraph "Restrict when <b>any</b> Motion Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Motion Sensors are true"
                    }
                } else {
                    app.removeSetting("motionRestrictionEvent")
                    app?.updateSetting("mrInactiveActive",[value:"false",type:"bool"])
                    app?.updateSetting("motionRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("motionEvent")
                app.removeSetting("motionRestrictionEvent")
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
                    if(psPresentNotPresent) {
                        paragraph "Trigger will fire when Sensor(s) become Not Present"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) become Present"
                    }
                    
                    input "presentANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(presentANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Presence Sensor is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Presence Sensors are true"
                    }
                } else {
                    app.removeSetting("presenceEvent")
                    app?.updateSetting("psPresentNotPresent",[value:"false",type:"bool"])
                    app?.updateSetting("presentANDOR",[value:"false",type:"bool"])
                }
                
                input "presenceRestrictionEvent", "capability.presenceSensor", title: "Restrict By Presence Sensor", required: false, multiple: true, submitOnChange: true
                if(presenceRestrictionEvent) {
                    input "prPresentNotPresent", "bool", title: "Restrict when Present (off) or Not Present (on)", description: "Present", defaultValue:false, submitOnChange:true
                    if(prPresentNotPresent) {
                        paragraph "Restrict when Sensor(s) become Not Present"
                    } else {
                        paragraph "Restrict when Sensor(s) become Present"
                    }
                    
                    input "presentRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(presentRANDOR) {
                        paragraph "Restrict when <b>any</b> Presence Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Presence Sensors are true"
                    }
                } else {
                    app.removeSetting("presenceRestrictionEvent")
                    app?.updateSetting("prPresentNotPresent",[value:"false",type:"bool"])
                    app?.updateSetting("presentRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("presenceEvent")
                app.removeSetting("presenceRestrictionEvent")
            }

            if(triggerType.contains("xSwitch")) {
                paragraph "<b>Switch</b>"
                input "switchEvent", "capability.switch", title: "Trigger by Switch", required:false, multiple:true, submitOnChange:true
                if(switchEvent) {
                    input "seOffOn", "bool", defaultValue:false, title: "Switch Off (off) or On (on)?", description: "Switch", submitOnChange:true
                    if(seOffOn) {
                        paragraph "Trigger will fire when Sensor(s) becomes On"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) becomes Off"
                    }
                    
                    input "switchANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(switchANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Switch is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Switches are true"
                    }                  
                } else {
                    app.removeSetting("switchEvent")
                    app?.updateSetting("seOffOn",[value:"false",type:"bool"])
                    app?.updateSetting("switchANDOR",[value:"false",type:"bool"])
                }
                
                input "switchRestrictionEvent", "capability.switch", title: "Restrict by Switch", required:false, multiple:true, submitOnChange:true
                if(switchRestrictionEvent) {
                    input "srOffOn", "bool", defaultValue:false, title: "Switch Off (off) or On (on)?", description: "Switch", submitOnChange:true
                    if(srOffOn) {
                        paragraph "Restrict when Switch(es) are On"
                    } else {
                        paragraph "Restrict when Switch(es) are Off"
                    }
                    
                    input "switchRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(switchRANDOR) {
                        paragraph "Restrict when <b>any</b> Switch is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Switches are true"
                    }                  
                } else {
                    app.removeSetting("switchRestrictionEvent")
                    app?.updateSetting("srOffOn",[value:"false",type:"bool"])
                    app?.updateSetting("switchRANDOR",[value:"false",type:"bool"])
                }
                
                paragraph "<hr>"  
            } else {
                app.removeSetting("switchEvent")
                app.removeSetting("switchRestrictionEvent")
            }

            if(triggerType.contains("xTemp")) {
                paragraph "<b>Temperature</b>"
                input "tempEvent", "capability.temperatureMeasurement", title: "By Temperature Setpoints", required:false, multiple:true, submitOnChange:true
                if(tempEvent) {
                    input "setTEPointHigh", "bool", defaultValue:false, title: "Trigger when Temperature is too High?", description: "Temp High", submitOnChange:true
                    if(setTEPointHigh) {
                        input "teSetPointHigh", "number", title: "Temperature High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("setTEPointHigh")
                    }

                    input "setTEPointLow", "bool", defaultValue:false, title: "Trigger when Temperature is too Low?", description: "Temp Low", submitOnChange:true
                    if(setTEPointLow) {
                        input "teSetPointLow", "number", title: "Temperature Low Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("setTEPointLow")
                    }

                    if(teSetPointHigh) paragraph "You will receive notifications if Temperature reading is above ${teSetPointHigh}"
                    if(teSetPointLow) paragraph "You will receive notifications if Temperature reading is below ${teSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("tempEvent")
                app.removeSetting("teSetPointHigh")
                app.removeSetting("teSetPointLow")
                app?.updateSetting("setTEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setTEPointLow",[value:"false",type:"bool"])
            }

            if(triggerType.contains("xVoltage")) {
                paragraph "<b>Voltage</b>"
                input "voltageEvent", "capability.voltageMeasurement", title: "By Voltage Setpoints", required:false, multiple:true, submitOnChange:true
                if(voltageEvent) {
                    input "setVEPointHigh", "bool", defaultValue:false, title: "Trigger when Voltage is too High?", description: "Voltage High", submitOnChange:true
                    if(setVEPointHigh) {
                        input "veSetPointHigh", "number", title: "Voltage High Setpoint", required: true, submitOnChange: true
                    } else {
                        app.removeSetting("veSetPointHigh")
                    }

                    input "setVEPointLow", "bool", defaultValue:false, title: "Trigger when Voltage is too Low?", description: "Voltage Low", submitOnChange:true
                    if(setVEPointLow) {
                        input "veSetPointLow", "number", title: "Voltage Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("veSetPointLow")
                    }

                    if(veSetPointHigh) paragraph "You will receive notifications if Voltage reading is above ${veSetPointHigh}"
                    if(veSetPointLow) paragraph "You will receive notifications if Voltage reading is below ${veSetPointLow}"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("voltageEvent")
                app.removeSetting("veSetPointHigh")
                app.removeSetting("veSetPointLow")
                app?.updateSetting("setVEPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setVEPointLow",[value:"false",type:"bool"])
            }
            
            if(triggerType.contains("xWater")) {
                paragraph "<b>Water</b>"
                input "waterEvent", "capability.waterSensor", title: "By Water Sensor", required: false, multiple: true, submitOnChange: true
                if(waterEvent) {
                    input "wsDryWet", "bool", title: "Trigger when Dry (off) or Wet (on)", description: "Water", defaultValue:false, submitOnChange:true
                    if(wsDryWet) {
                        paragraph "Trigger will fire when Sensor(s) become Wet"
                    } else {
                        paragraph "Trigger will fire when Sensor(s) become Dry"
                    }
                    input "waterANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Trigger will fire when <b>any</b> Water Sensor is true"
                    } else {
                        paragraph "Trigger will fire when <b>all</b> Water Sensors are true"
                    }
                } else {
                    app.removeSetting("waterEvent")
                    app?.updateSetting("wsDryWet",[value:"false",type:"bool"])
                    app?.updateSetting("waterANDOR",[value:"false",type:"bool"])
                }
                
                input "waterRestrictionEvent", "capability.waterSensor", title: "Restrict By Water Sensor", required: false, multiple: true, submitOnChange: true
                if(waterRestrictionEvent) {
                    input "wrDryWet", "bool", title: "Restrict when Dry (off) or Wet (on)", description: "Water", defaultValue:false, submitOnChange:true
                    if(wrDryWet) {
                        paragraph "Restrict when Sensor(s) become Wet"
                    } else {
                        paragraph "Restrict when Sensor(s) become Dry"
                    }
                    input "waterRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Restrict when <b>any</b> Water Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Water Sensors are true"
                    }
                } else {
                    app.removeSetting("waterRestrictionEvent")
                    app?.updateSetting("wrDryWet",[value:"false",type:"bool"])
                    app?.updateSetting("waterRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("waterEvent")
                app.removeSetting("waterRestrictionEvent")
            }

            if(triggerType.contains("xCustom")) {
                paragraph "<b>Custom Attribute</b>"
                input "customEvent", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
                
                if(customEvent) {
                    allAttrs1 = []
                    allAttrs1 = customEvent.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                    allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                    input "specialAtt", "enum", title: "Attribute to track", options: allAttrs1a, required:true, multiple:false, submitOnChange:true
                    
                    input "deviceORsetpoint", "bool", defaultValue:false, title: "Device (off) or Setpoint (on)", description: "Whole", submitOnChange:true
                    
                    if(deviceORsetpoint) {
                        input "setSDPointHigh", "bool", defaultValue:false, title: "Trigger when Custom is too High?", description: "Custom High", submitOnChange:true
                        if(setSDPointHigh) {
                            input "sdSetPointHigh", "number", title: "Custom High Setpoint", required: true, submitOnChange: true
                        } else {
                            app.removeSetting("sdSetPointHigh")
                        }

                        input "setSDPointLow", "bool", defaultValue:false, title: "Trigger when Custom is too Low?", description: "Custom Low", submitOnChange:true
                        if(setSDPointLow) {
                            input "sdSetPointLow", "number", title: "Custom Low Setpoint", required:true, submitOnChange:true
                        } else {
                            app.removeSetting("sdSetPointLow")
                        }

                        if(sdSetPointHigh) paragraph "You will receive notifications if Custom reading is above ${sdSetPointHigh}"
                        if(sdSetPointLow) paragraph "You will receive notifications if Custom reading is below ${sdSetPointLow}"

                        app.removeSetting("custom1")
                        app.removeSetting("custom2")
                        app?.updateSetting("sdCustom1Custom2",[value:"false",type:"bool"])
                        app?.updateSetting("customANDOR",[value:"false",type:"bool"])
                    } else {
                        paragraph "Enter in the attribute values required to trigger Cog. Must be exactly as seen in the device current stats. (ie. on/off, open/closed)"
                        input "custom1", "text", title: "Attribute Value 1", required:true, submitOnChange:true
                        input "custom2", "text", title: "Attribute Value 2", required:true, submitOnChange:true
                        
                        input "sdCustom1Custom2", "bool", title: "Trigger when ${custom1} (off) or ${custom2} (on)", description: "Custom", defaultValue:false, submitOnChange:true
                        if(sdCustom1Custom2) {
                            paragraph "Trigger will fire when Custom(s) become ${custom1}"
                        } else {
                            paragraph "Trigger will fire when Custom(s) become ${custom2}"
                        }
                        paragraph "* Remember - If trigger is working backwards, simply reverse your values above."
                        input "customANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                        if(customANDOR) {
                            paragraph "Trigger will fire when <b>any</b> Custom is true"
                        } else {
                            paragraph "Trigger will fire when <b>all</b> Custom are true"
                        }

                        app.removeSetting("sdSetPointHigh")
                        app.removeSetting("sdSetPointLow")
                        app?.updateSetting("setSDPointHigh",[value:"false",type:"bool"])
                        app?.updateSetting("setSDPointLow",[value:"false",type:"bool"])
                    }
                }
            } else {
                app.removeSetting("custom1")
                app.removeSetting("custom2")
                app?.updateSetting("sdCustom1Custom2",[value:"false",type:"bool"])
                app?.updateSetting("customANDOR",[value:"false",type:"bool"])

                app.removeSetting("sdSetPointHigh")
                app.removeSetting("sdSetPointLow")
                app?.updateSetting("setSDPointHigh",[value:"false",type:"bool"])
                app?.updateSetting("setSDPointLow",[value:"false",type:"bool"])
            }
            
            if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || (customEvent && deviceORsetpoint)) {
                input "useWholeNumber", "bool", defaultValue:false, title: "Only use Whole Numbers (round each number)", description: "Whole", submitOnChange:true
                paragraph "<small>* Note: This effects the data coming in from the device.</small>"
                
                //input "useOverRestriction", "bool", title: "Use over Setpoint as Restriction", description: "restriction", defaultValue:false, submitOnChange:true
            } else {
                app?.updateSetting("useWholeNumber",[value:"false",type:"bool"])
            }

            if(accelerationEvent || batteryEvent || contactEvent || humidityEvent || hsmAlertEvent || hsmStatusEvent || illuminanceEvent || modeEvent || motionEvent || powerEvent || presenceEvent || switchEvent || tempEvent || waterEvent) {
                input "setDelay", "bool", defaultValue:false, title: "<b>Set Delay?</b>", description: "Delay Time", submitOnChange:true, width:6
                input "randomDelay", "bool", defaultValue:false, title: "<b>Set Random Delay?</b>", description: "Random Delay", submitOnChange:true, width:6

                if(setDelay && randomDelay) paragraph "<b>Warning: Please don't select BOTH Set Delay and Random Delay.</b>"
                if(setDelay) {
                    paragraph "Delay the notifications until all devices have been in state for XX minutes."
                    input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the notifications will be cancelled.</small>"
                } else {
                    app.removeSetting("notifyDelay")
                    app?.updateSetting("setDelay",[value:"false",type:"bool"])
                }

                if(randomDelay) {
                    paragraph "Delay the notifications until all devices have been in state for XX minutes."                
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
                ["aValve":"Valves"]
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
                    input "permanentDim2", "bool", title: "Use Permanent Dim instead of Off", defaultValue:false, submitOnChange:true
                    if(permanentDim2) {
                        paragraph "Instead of turning off, lights will dim to a set level"
                        input "permanentDimLvl2", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                    } else {
                        app.removeSetting("permanentDimLvl2")
                    }
                } else {
                    app.removeSetting("permanentDimLvl2")
                    app?.updateSetting("permanentDim2",[value:"false",type:"bool"])
                }

                input "switchesToggleAction", "capability.switch", title: "Switches to Toggle", multiple:true, submitOnChange:true

                input "switchesLCAction", "bool", title: "Turn Light On, Set Level and/or Color", description: "Light OLC", defaultValue:false, submitOnChange:true
                if(switchesLCAction) {
                    input "setOnLC", "capability.switchLevel", title: "Select dimmer to set", required: false, multiple: true
                    input "levelLC", "number", title: "On Level (1 to 99)", required: false, multiple: false, defaultValue: 99, range: '1..99'
                    input "colorLC", "enum", title: "Color", required: false, multiple:false, options: [
                        ["No Change":"Keep Current Color"],
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
                label title: "Enter a name for this automation", required: false
            }
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Enable extra logging for debugging.", submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required: false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours"]
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
                wc =  "%whoHappened% - Device that caused the event to trigger<br>"
                wc += "%whatHappened% - Device status that caused the event to trigger<br>"
                wc += "%time% - Will speak the current time in 24 h<br>"
                wc += "%time1% - Will speak the current time in 12 h<br>"
                if(lockEvent) wc += "%whoUnlocked% - The name of the person who unlocked the door<br>"

                paragraph wc
                
                if(triggerType.contains("xBattery") ||  triggerType.contains("xEnergy") ||  triggerType.contains("xHumidity") ||  triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp")) {
                    paragraph "<b>Setpoint Message Options</b>"
                    input "messageH", "text", title: "Message to speak when reading is too high", required: false, submitOnChange: true
                    input "messageL", "text", title: "Message to speak when reading is too low", required: false, submitOnChange: true
                    input "messageB", "text", title: "Message to speak when reading is both too high and too low", required: false
                } else {
                    app.removeSetting("messageH")
                    app.removeSetting("messageL")
                    app.removeSetting("messageB")
                }
                
                if(!triggerType.contains("xBattery") ||  !triggerType.contains("xEnergy") ||  !triggerType.contains("xHumidity") && !triggerType.contains("xIlluminance") && !triggerType.contains("xPower") && !triggerType.contains("xTemp")) {
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
    unsubscribe()
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
        if(accelerationEvent) subscribe(accelerationEvent, "accelerationSensor", startTheProcess)             
        if(batteryEvent) subscribe(batteryEvent, "battery", startTheProcess)        
        if(contactEvent) subscribe(contactEvent, "contact", startTheProcess)
        if(energyEvent) subscribe(energyEvent, "energy", startTheProcess)
        if(garagedoorEvent) subscribe(garagedoorEvent, "door", startTheProcess)
        if(hsmAlertEvent) subscribe(location, "hsmAlert", startTheProcess)
        if(hsmStatusEvent) subscribe(location, "hsmStatus", startTheProcess)
        if(humidityEvent) subscribe(humidityEvent, "humidity", startTheProcess)
        if(illuminanceEvent) subscribe(illuminanceEvent, "illuminance", startTheProcess)
        if(lockEvent) subscribe(lockEvent, "lock", startTheProcess)
        if(modeEvent) subscribe(location, "mode", startTheProcess)
        if(motionEvent) subscribe(motionEvent, "motion", startTheProcess)
        if(powerEvent) subscribe(powerEvent, "power", startTheProcess)       
        if(switchEvent) subscribe(switchEvent, "switch", startTheProcess)   
        if(voltageEvent) subscribe(voltageEvent, "voltage", startTheProcess) 
        if(tempEvent) subscribe(tempEvent, "temperature", startTheProcess)
        if(customEvent) subscribe(customEvent, specialAtt, startTheProcess)

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
            if(logEnable) log.debug "In initialize - xPeriodic - Starting! - (${preMadePeriodic})"
            schedule(preMadePeriodic, startTheProcess)
        }
        
        if(triggerType.contains("xTimeDays")) { 
            autoSunHandler()
        } else {
            state.sunRiseTosunSet = true
        }
        
        if(fromTime && toTime) { 
            checkTime()
            schedule(fromTime, runAtTime1)
            schedule(toTime, runAtTime2)
        } else {
            state.timeBetween = true
        }
        
        if(sunriseEndTime) schedule(sunriseEndTime, runAtTime2)
        if(sunsetEndTime) schedule(sunsetEndTime, runAtTime2)   
        
        state.setpointBetweenOK = "yes"
        state.setpointHighOK = "yes"
        state.setpointLowOK = "yes"
        state.devicesOK = "yes"
        startTheProcess()
    }
}

def startTheProcess(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In startTheProcess (${state.version})"
        state.nothingToDo = true
        state.devicesGood = false
        state.setpointGood = false
        state.modeMatch = false
        state.isThereDevices = false
        state.isThereSPDevices = false
        state.areRestrictions = false
        state.skip = false
        if(preMadePeriodic) state.nothingToDo = false
        
        if(evt) {
            if(evt == "again") {
                state.whoHappened = "NA"
                state.whatHappened = "NA"
            } else {
                try {
                    state.whoHappened = evt.displayName
                    state.whatHappened = evt.value
                } catch(e) {
                    // Do nothing
                }
                if(logEnable) log.trace "******************** In startTheProcess - ${app.label} - ${state.whoHappened}: ${state.whatHappened} ********************"
                state.hasntDelayedYet = true
            }
        }
        
        accelerationRestrictionHandler()
        contactRestrictionHandler()
        garageDoorRestrictionHandler()           
        lockRestrictionHandler()
        motionRestrictionHandler()             
        presenceRestrictionHandler()
        switchRestrictionHandler()            
        waterRestrictionHandler()
        modeRestrictionHandler()
        
        if(state.areRestrictions) {
            if(logEnable) log.debug "In startTheProcess - Restrictions are true, skipping"
            state.skip = true
            state.nothingToDo = true
        } else {        
            if(state.skip) {
                if(logEnable) log.debug "In startTheProcess - Skipping Time checks"
            } else {
                checkTime()
                checkTimeSun()
                dayOfTheWeekHandler()
                modeHandler()

                hsmAlertHandler(state.whatHappened)
                hsmStatusHandler(state.whatHappened)
                if(logEnable) log.debug "In startTheProcess - 1 - checkTime: ${state.timeBetween} - checkTimeSun: ${state.timeBetweenSun} - daysMatch: ${state.daysMatch} - modeMatch: ${state.modeMatch}"

                if(daysMatchRestriction || !state.daysMatch) { state.nothingToDo = true; state.skip = true }
                if(timeBetweenRestriction || !state.timeBetween) { state.nothingToDo = true; state.skip = true }
                if(timeBetweenSunRestriction || !state.timeBetweenSun) { state.nothingToDo = true; state.skip = true }                
                if(modeRestrictionEvent || !state.modeMatch) { state.nothingToDo = true; state.skip = true }
            }
            
            if(state.skip) {
                if(logEnable) log.debug "In startTheProcess - Skipping Device checks"
            } else {
                accelerationHandler()
                contactHandler()
                garageDoorHandler()           
                lockHandler()
                motionHandler()             
                presenceHandler()
                switchHandler()            
                waterHandler()

                batteryHandler()
                energyHandler()
                humidityHandler()
                illuminanceHandler()
                powerHandler()
                tempHandler()
                voltageHandler()
                
                if(deviceORsetpoint) {
                    customSetpointHandler()
                } else {
                    customDeviceHandler()
                }
            }
        }
        
        if(state.skip) { 
            // do nothing
        } else {
            if(logEnable) log.debug "In startTheProcess - 2 - setpointGood: ${state.setpointGood} - devicesGood: ${state.devicesGood} - nothingToDo: ${state.nothingToDo}"
        }
        
        if(state.nothingToDo) {
            if(logEnable) log.trace "In startTheProcess - Nothing to do - STOPING"
        } else {
            allGood = state.timeBetween && state.timeBetweenSun && state.daysMatch && state.modeMatch && state.setpointGood && state.devicesGood
            if(state.skip) { allGood = true }
            if(logEnable) log.debug "In startTheProcess - 3 - allGood: ${allGood}"
            if(allGood) {            
                if(logEnable) log.trace "In startTheProcess - HERE WE GO!"

                if(state.hasntDelayedYet == null) state.hasntDelayedYet = false
                if((notifyDelay || randomDelay || targetDelay) && state.hasntDelayedYet) {
                    if(notifyDelay) newDelay = notifyDelay
                    if(randomDelay) newDelay = Math.abs(new Random().nextInt() % (delayHigh - delayLow)) + delayLow
                    if(targetDelay) newDelay = minutesUp
                    if(logEnable) log.debug "In startTheProcess - Delay is set for ${newDelay} minutes"
                    if(actionType.contains("aSwitch") && switchedDimUpAction) { slowOnHandler() }
                    int theDelay = newDelay * 60
                    state.hasntDelayedYet = false
                    runIn(theDelay, startTheProcess, [data: "again"])
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
                if(logEnable) log.trace "In startTheProcess - GOING IN REVERSE"
                if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnReverseActionHandler() }
                if(actionType.contains("aSwitch") && switchesOffAction && permanentDimLvl2) { permanentDimHandler() }
                if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim2) { switchesOffReverseActionHandler() }
                if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
                if(actionType.contains("aSwitch") && switchesLCAction && permanentDim) { permanentDimHandler() }
                if(actionType.contains("aSwitch") && switchesLCAction && !permanentDim) { dimmerOnReverseActionHandler() }

                state.hasntDelayedYet = true
            } else {
                if(logEnable) log.trace "In startTheProcess - Something didn't pass - STOPING"
            }
        }
        if(logEnable) log.trace "******************** End startTheProcess - ${app.label} ********************"
    }
}

// ********** Start Triggers **********

def customDeviceHandler() {
    if(customEvent) {
        state.eventName = customEvent
        state.eventType = specialAtt
        state.type = sdCustom1Custom2
        state.typeValue1 = custom1
        state.typeValue2 = custom2
        state.typeAO = customANDOR
        devicesGoodHandler()
    }
}

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
        state.typeAO = false
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
    if(!state.isThereDevices) { state.devicesGood = true }    // Keep in LAST device
}

def devicesGoodHandler() {
    if(logEnable) log.debug "In devicesGoodHandler (${state.version}) - ${state.eventType.toUpperCase()}"
    state.isThereDevices = true
    deviceTrue = 0
    try {
        theCount = state.eventName.size()
    } catch(e) {
        theCount = 1
    }

    state.eventName.each { it ->
        theValue = it.currentValue("${state.eventType}")
        if(logEnable) log.debug "In devicesGoodHandler - Checking: ${it.displayName} - ${state.eventType} - Testing Current Value - ${theValue}"

        if(state.type) {
            if(theValue == state.typeValue1) { 
                if(logEnable) log.debug "In devicesGoodHandler - Working 1: ${state.typeValue1} and Current Value: ${theValue}"
                if(state.eventType == "lock") {
                    if(logEnable) log.debug "In devicesGoodHandler - Lock"
                    state.whoUnlocked = it.currentValue("lastCodeName")
                    lockUser.each { us ->
                        if(logEnable) log.trace "I'm checking lock names - $us vs $state.whoUnlocked"
                        if(us == state.whoUnlocked) { 
                            if(logEnable) log.trace "MATCH: ${state.whoUnlocked}"
                            deviceTrue = deviceTrue + 1
                        }                   
                    }
                } else {
                    if(logEnable) log.debug "In devicesGoodHandler - Everything Else 1"
                    deviceTrue = deviceTrue + 1
                }           
            } 
        } else if(theValue == state.typeValue2) { 
            if(logEnable) log.debug "In devicesGoodHandler - Working 2: ${state.typeValue2} and Current Value: ${theValue}"
            if(state.eventType == "lock") {
                state.whoUnlocked = it.currentValue("lastCodeName")
                lockUser.each { us ->
                    if(logEnable) log.trace "I'm checking lock names - $us vs $state.whoUnlocked"
                    if(us == state.whoUnlocked) { 
                        if(logEnable) log.trace "MATCH: ${state.whoUnlocked}"
                        deviceTrue = deviceTrue + 1
                    }                   
                }
            } else {
                if(logEnable) log.debug "In devicesGoodHandler - Everything Else 2"
                deviceTrue = deviceTrue + 1
            }
        }
    }
    if(logEnable) log.debug "In devicesGoodHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue} vs ${theCount} - type: ${state.typeAO}" 
    if(state.typeAO) {
        if(deviceTrue >= 1) { // Bad
            if(state.devicesOK == "yes") {
                state.devicesOK = "no"
                state.devicesGood = true
                state.nothingToDo = false
            } else {
                state.nothingToDo = true
                state.devicesOK == "yes"
            }
        } else {  // Good
            if(state.devicesOK == "yes") {
                state.nothingToDo = true
            } else {
                state.devicesOK = "yes" 
                if(reverse) {
                    state.devicesGood = false
                    state.nothingToDo = false
                } else {
                    state.devicesGood = true
                    state.nothingToDo = true
                }
            }
        }
    } else {
        if(deviceTrue == theCount) { // Bad
            if(state.devicesOK == "yes") {
                state.devicesOK = "no"
                state.devicesGood = true
                state.nothingToDo = false
            } else {
                state.nothingToDo = true
                state.devicesOK == "yes"
            }
        } else { // Good
            state.devicesOK = "yes" 
            if(reverse) {
                state.devicesGood = false
                state.nothingToDo = false
            } else {
                state.devicesGood = true
                state.nothingToDo = true
            }
        }   
    }
    if(logEnable) log.debug "In devicesGoodHandler - ${state.eventType.toUpperCase()} - devicesGood: ${state.devicesGood} - nothingToDo: ${state.nothingToDo}"
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
    if(logEnable) log.debug "In modeHandler (${state.version})"
    if(modeEvent) {
        if(logEnable) log.debug "In modeHandler - modeEvent: ${modeEvent}"

        modeEvent.each { it ->
            theValue = location.mode
            if(logEnable) log.debug "In modeHandler - Checking: ${it} - value: ${theValue}"

            if(theValue == it){
                if(logEnable) log.debug "In modeHandler - MATCH"
                if(modeOnOff) {
                    state.modeMatch = true
                    state.nothingToDo = false
                }
                if(!modeOnOff) {
                    state.modeMatch = true
                    state.nothingToDo = false
                }
            }
        }
    } else {
        state.modeMatch = true
    }
    if(logEnable) log.debug "In modeHandler - modeMatch: ${state.modeMatch} - nothingToDo: ${state.nothingToDo}"
}

def ruleMachineHandler() {
    if(logEnable) log.debug "In ruleMachineHandler - Rule: ${rmRule} - Action: ${rmAction}"
    RMUtils.sendAction(rmRule, rmAction, app.label)
}

// ***** Start Setpoint Handlers *****

def customSetpointHandler() {
    if(customEvent) {
        state.spName = customEvent
        state.spType = specialAtt
        state.setpointHigh = sdSetPointHigh
        state.setpointLow = sdSetPointLow
        setpointHandler()
    }
}

def batteryHandler() {
    if(batteryEvent) {
        state.spName = batteryEvent
        state.spType = "battery"
        state.setpointHigh = beSetPointHigh
        state.setpointLow = beSetPointLow
        setpointHandler()
    }
}

def energyHandler() {
    if(energyEvent) {
        state.spName = energyEvent
        state.spType = "energy"
        state.setpointHigh = eeSetPointHigh
        state.setpointLow = eeSetPointLow
        setpointHandler()
    }
}

def humidityHandler() {
    if(humidityEvent) {
        state.spName = humidityEvent
        state.spType = "humidity"
        state.setpointHigh = heSetPointHigh
        state.setpointLow = heSetPointLow
        setpointHandler()
    } 
}

def illuminanceHandler() {
    if(illuminanceEvent) {
        state.spName = illuminanceEvent
        state.spType = "illuminance"
        state.setpointHigh = ieSetPointHigh
        state.setpointLow = ieSetPointLow
        setpointHandler()
    }
}

def powerHandler() {
    if(powerEvent) {
        state.spName = powerEvent
        state.spType = "power"
        state.setpointHigh = peSetPointHigh
        state.setpointLow = peSetPointLow
        setpointHandler()
    }
}

def tempHandler() {
    if(tempEvent) {
        state.spName = tempEvent
        state.spType = "temperature"
        state.setpointHigh = teSetPointHigh
        state.setpointLow = teSetPointLow
        setpointHandler()
    }
}

def voltageHandler() {
    if(voltageEvent) {
        state.spName = voltageEvent
        state.spType = "voltage"
        state.setpointHigh = veSetPointHigh
        state.setpointLow = veSetPointLow
        setpointHandler()
    }    
    if(!state.isThereSPDevices) { state.setpointGood = true }    // Keep in LAST setpoint
}
    
def setpointHandler() {
    if(logEnable) log.trace "In setpointHandler (${state.version}) - spName: ${state.spName}"
    if(logEnable) log.trace "PREVIOUS: prevSPV: ${state.preSPV} - setpointLowOK: ${state.setpointLowOK} - setpointHighOK: ${state.setpointHighOK} - setpointGood: ${state.setpointGood} - nothingToDo: ${state.nothingToDo}"
    state.isThereSPDevices = true
    state.spName.each {
        spValue = it.currentValue("${state.spType}")
        if(useWholeNumber) {
            setpointValue = Math.round(spValue)
        } else {
            setpointValue = spValue.toDouble()
        }     
        state.preSPV = setpointValue
        if(logEnable) log.trace "In setpointHandler - Working on: ${it} - setpointValue: ${setpointValue} - setpointLow: ${state.setpointLow} - setpointHigh: ${state.setpointHigh} - nothingToDo: ${state.nothingToDo}"
        
        // *** setpointHigh ***
        if(state.setpointHigh && !state.setpointLow) {
            int setpointHighValue = setpointValue
            int setpointHigh = state.setpointHigh
            if(logEnable) log.trace "In setpointHandler (High) - setpointHighOK: ${state.setpointHighOK} - setpointHighValue: ${setpointHighValue} - setpointHigh: ${setpointHigh}"
            if(setpointHighValue > setpointHigh) {  // bad
                if(state.setpointHighOK == "yes") {
                    if(logEnable) log.trace "In setpointHandler (High) - Device: ${it}, Value: ${setpointHighValue} is GREATER THAN setpointHigh: ${setpointHigh}"
                    state.setpointHighOK = "no"
                    state.setpointGood = true
                    state.nothingToDo = false
                } else {
                    if(logEnable) log.trace "In setpointHandler (High) - Device: ${it}, Value: ${setpointHighValue} is STILL HIGH. Nothing to do."
                    state.nothingToDo = true
                    state.setpointHighOK == "yes"
                }
            } else {  // good
                if(logEnable) log.trace "In setpointHandler (High) - Device: ${it}, Value: ${setpointHighValue} is LESS THAN setpointHigh: ${setpointHigh} - All Good"
                if(state.setpointHighOK == "yes") {
                    state.nothingToDo = true
                } else {
                    state.setpointHighOK = "yes" 
                    if(reverse) {
                        state.setpointGood = false
                        state.nothingToDo = false
                    } else {
                        state.setpointGood = true
                        state.nothingToDo = true
                    }
                }
            }
        }

        // *** setpointLow ***
        if(!state.setpointHigh && state.setpointLow) {
            int setpointLowValue = setpointValue
            int setpointLow = state.setpointLow
            if(logEnable) log.trace "In setpointHandler (Low) - setpointLowOK: ${state.setpointLowOK} - setpointLowValue: ${setpointLowValue} - setpointLow: ${setpointLow}"
            if(setpointLowValue < setpointLow) {  // bad
                if(state.setpointLowOK == "yes") {
                    if(logEnable) log.trace "In setpointHandler (Low) - Device: ${it}, Value: ${setpointLowValue} is LESS THAN setpointLow: ${setpointLow}"
                    state.setpointLowOK = "no"
                    state.setpointGood = true
                    state.nothingToDo = false
                } else {
                    if(logEnable) log.trace "In setpointHandler (Low) - Device: ${it}, Value: ${setpointLowValue} is STILL LOW. Nothing to do."
                    state.nothingToDo = true
                    state.setpointLowOK == "yes"
                }
            } else {  // good
                if(logEnable) log.trace "In setpointHandler (Low) - Device: ${it}, Value: ${setpointLowValue} is GREATER THAN setpointLow: ${setpointLow} - All Good"
                if(state.setpointLowOK == "yes") {
                    state.nothingToDo = true
                } else {
                    state.setpointLowOK = "yes" 
                    if(reverse) {
                        state.setpointGood = false
                        state.nothingToDo = false
                    } else {
                        state.setpointGood = true
                        state.nothingToDo = true
                    }
                }
            }
        }
        
        // *** Inbetween ***
        if(state.setpointHigh && state.setpointLow) {
            if(logEnable) log.debug "In setpointHandler (Between) - setpointBetweenOK: ${state.setpointBetweenOK}"
            int setpointValue = setpointValue
            int setpointLow = state.setpointLow
            int setpointHigh = state.setpointHigh
            if((setpointValue < setpointLow) || (setpointValue > setpointHigh)) {  // bad
                if(state.setpointBetweenOK == "yes") {
                    if(logEnable) log.debug "In setpointHandler (Between) - Device: ${it}, Value: ${setpointValue} is NOT BETWEEN setpoints."
                    state.setpointBetweenOK = "no"
                    state.setpointGood = true
                    state.nothingToDo = false
                } else {
                    if(logEnable) log.debug "In setpointHandler (Between) - Device: ${it}, Value: ${setpointValue} is STILL NOT BETWEEN. Nothing to do."
                    state.nothingToDo = true
                }
            } else {  // good
                if(logEnable) log.debug "In setpointHandler (Between) - Device: ${it}, Value: ${setpointValue} is BETWEEN setpoints - All Good."
                if(state.setpointBetweenOK == "yes") {
                    state.nothingToDo = true
                } else {
                    state.setpointBetweenOK = "yes" 
                    if(reverse) {
                        state.setpointGood = false
                        state.nothingToDo = false
                    } else {
                        state.setpointGood = true
                        state.nothingToDo = true
                    }
                }
            }
        }
    }
    if(logEnable) log.debug "In setpointHandler - ${state.spType.toUpperCase()} - setpointGood: ${state.setpointGood} - nothingToDo: ${state.nothingToDo}"
}

// ********** Start Restrictions **********

def accelerationRestrictionHandler() {
    if(accelerationRestrictionEvent) {
        state.rEventName = accelerationRestrictionEvent
        state.rEventType = "acceleration"
        state.rType = arInactiveActive
        state.rTypeValue1 = "active"
        state.rTypeValue2 = "inactive"
        state.rTypeAO = accelerationRANDOR
        restrictionHandler()
    }
}

def contactRestrictionHandler() {
    if(contactRestrictionEvent) {
        state.rEventName = contactRestrictionEvent
        state.rEventType = "contact"
        state.rType = crClosedOpen
        state.rTypeValue1 = "open"
        state.rTypeValue2 = "closed"
        state.rTypeAO = contactRANDOR
        restrictionHandler()
    } 
}

def garageDoorRestrictionHandler() {
    if(garageDoorRestrictionEvent) {
        state.rEventName = garageDoorRestrictionEvent
        state.rEventType = "door"
        state.rEype = gdrClosedOpen
        state.rTypeValue1 = "open"
        state.rTypeValue2 = "closed"
        state.rTypeAO = garageDoorRANDOR
        restrictionHandler()
    } 
}

def lockRestrictionHandler() {
    if(lockRestrictionEvent) {
        state.rEventName = lockRestrictionEvent
        state.rEventType = "lock"
        state.rType = lrUnlockedLocked
        state.rTypeValue1 = "locked"
        state.rTypeValue2 = "unlocked"
        state.rTypeAO = false
        restrictionHandler()
    } 
}

def motionRestrictionHandler() {
    if(motionRestrictionEvent) {
        state.rEventName = motionRestrictionEvent
        state.rEventType = "motion"
        state.rType = mrInactiveActive
        state.rTypeValue1 = "active"
        state.rTypeValue2 = "inactive"
        state.rTypeAO = motionRANDOR
        restrictionHandler()
    }
}

def presenceRestrictionHandler() {
    if(presenceRestrictionEvent) {
        state.rEventName = presenceRestrictionEvent
        state.rEventType = "presence"
        state.rType = prPresentNotPresent
        state.rTypeValue1 = "not present"
        state.rTypeValue2 = "present"
        state.rTypeAO = presenceRANDOR
        restrictionHandler()
    }
}

def switchRestrictionHandler() {
    if(switchRestriction) {
        state.rEventName = switchRestrictionEvent
        state.rEventType = "switch"
        state.rType = srOffOn
        state.rTypeValue1 = "on"
        state.rTypeValue2 = "off"
        state.rTypeAO = switchRANDOR
        restrictionHandler()
    }
}

def waterRestrictionHandler() {
    if(waterRestrictionEvent) {
        state.rEventName = waterRestrictionEvent
        state.rEventType = "water"
        state.rType = wrDryWet
        state.rTypeValue1 = "Wet"
        state.rTypeValue2 = "Dry"
        state.rTypeAO = waterRANDOR
        restrictionHandler()
    }
}

def restrictionHandler() {
    if(logEnable) log.debug "In restrictionHandler (${state.version}) - ${state.rEventType.toUpperCase()}"
    deviceTrue = 0
    try {
        theCount = state.rEventName.size()
    } catch(e) {
        theCount = 1
    }

    state.rEventName.each { it ->
        theValue = it.currentValue("${state.rEventType}")
        if(logEnable) log.debug "In restrictionHandler - Checking: ${it.displayName} - ${state.rEventType} - Testing Current Value - ${theValue}"

        if(state.rType) {
            if(theValue == state.rTypeValue1) { 
                if(state.rEventType == "lock") {
                    if(logEnable) log.debug "In restrictionHandler - Lock"
                    state.whoUnlocked = it.currentValue("lastCodeName")
                    lockRestrictionUser.each { us ->
                        if(logEnable) log.trace "I'm checking lock names - $us vs $state.whoUnlocked"
                        if(us == state.whoUnlocked) { 
                            if(logEnable) log.trace "MATCH: ${state.whoUnlocked}"
                            deviceTrue = deviceTrue + 1
                        }                   
                    }
                } else {
                    if(logEnable) log.debug "In restrictionHandler - Everything Else 1"
                    deviceTrue = deviceTrue + 1
                }                  
            } 
        } else if(theValue == state.rTypeValue2) { 
            if(state.rEventType == "lock") {
                state.whoUnlocked = it.currentValue("lastCodeName")
                lockRestrictionUser.each { us ->
                    if(logEnable) log.trace "I'm checking lock names - $us vs $state.whoUnlocked"
                    if(us == state.whoUnlocked) { 
                        if(logEnable) log.trace "MATCH: ${state.whoUnlocked}"
                        deviceTrue = deviceTrue + 1
                    }                   
                }
            } else {
                if(logEnable) log.debug "In restrictionHandler - Everything Else 2"
                deviceTrue = deviceTrue + 1
            }
        }
    }
    if(logEnable) log.debug "In restrictionHandler - theCount: ${theCount} - deviceTrue: ${deviceTrue} vs ${theCount} - type: ${state.rTypeAO}" 
    if(state.rTypeAO) {
        if(deviceTrue >= 1) { // OR
            state.areRestrictions = true
        }
    } else {
        if(deviceTrue == theCount) { // AND
            state.areRestrictions = true
        }   
    }   
    if(logEnable) log.debug "In restrictionHandler - ${state.rEventType.toUpperCase()} - areRestrictions: ${state.areRestrictions}"   
}

def modeRestrictionHandler() {
    if(logEnable) log.debug "In modeRestrictionHandler (${state.version})"
    if(modeRestrictionEvent) {
        if(modeRestrictionEvent) log.debug "In modeRestrictionHandler - modeRestrictionEvent: ${modeRestrictionEvent}"
        
        modeRestrictionEvent.each { it ->
            theValue = location.mode
            if(logEnable) log.debug "In modeRestrictionHandler - Checking: ${it} - value: ${theValue}"

            if(theValue == it){
                if(logEnable) log.debug "In modeHandler - MATCH"
                if(modeROnOff) {
                    state.areRestrictions = true
                }
                if(!modeROnOff) {
                    state.areRestrictions = true
                }
            }
        }
    }
    if(logEnable) log.debug "In modeRestrictionHandler - areRestrictions: ${state.areRestrictions}"
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
            if(it.hasCommand("setColor")) {
                name = (it.displayName).replace(" ","")
                data = state.oldMap.get(name)            
                def (oldStatus, oldHueColor, oldSaturation, oldLevel) =  data.split("::")           
                int hueColor = oldHueColor.toInteger()
                int saturation = oldSaturation.toInteger()
                int level = oldLevel.toInteger()           
                def theValue = [hue: hueColor, saturation: saturation, level: level]
                if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - theValue: ${theValue}"
                it.setColor(theValue)
                pauseExecution(1000)
                if(oldStatus == "off") {
                    if(logEnable) log.trace "In dimmerOnReverseActionHandler - setColor - Turning light off (${it})"
                    it.off()
                }
            } else if(it.hasCommand("setLevel")) {
                name = (it.displayName).replace(" ","")
                data = state.oldLevelMap.get(name)            
                def (oldStatus, oldLevel) =  data.split("::")           
                int level = oldLevel.toInteger()           
                def theValue = [level: level]
                if(logEnable) log.debug "In dimmerOnReverseActionHandler - setLevel - Reversing Light: ${it} - oldStatus: ${oldStatus} - theValue: ${theValue}"
                it.setLevel(theValue)
                pauseExecution(1000)
                if(oldStatus == "off") {
                    if(logEnable) log.trace "In dimmerOnReverseActionHandler - setLevel - Turning light off (${it})"
                    it.off()
                }
            }
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
    
    if(switchesOffAction) {
        switchesOffAction.each { it ->
            if(it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In permanentDimHandler - Set Level on ${it} to ${permanentDimLvl2}"
                it.setLevel(permanentDimLvl2)
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

def lockUserActionHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.warn "In lockUserActionHandler (${state.version})"

        if(evt) {
            lockdata = evt.data
            lockStatus = evt.value
            lockName = evt.displayName
            if(logEnable) log.trace "In lockUserActionHandler (${state.version}) - Lock: ${lockName} - Status: ${lockStatus}"
            if(lockStatus == "unlocked") {
                if(logEnable) log.trace "In lockUserActionHandler - Lock: ${lockName} - Status: ${lockStatus} - We're in!"
                if(theLocks) {
                    if (lockdata && !lockdata[0].startsWith("{")) {
                        lockdata = decrypt(lockdata)
                        if (lockdata == null) {
                            log.debug "Unable to decrypt lock code from device: ${lockName}"
                            return
                        }
                    }
                    def codeMap = parseJson(lockdata ?: "{}").find{ it }
                    if (!codeMap) {
                        if(logEnable) log.trace "In lockUserActionHandler - Lock Code not available."
                        return
                    }
                    codeName = "${codeMap?.value?.name}"         
                    if(logEnable) log.trace "In lockUserActionHandler - ${lockName} was unlocked by ${codeName}"	
                }
            }
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
    
    if(triggerType.contains("xBattery") ||  triggerType.contains("xEnergy") ||  triggerType.contains("xHumidity") ||  triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp")) {
        if(logEnable) log.debug "In messageHandler (setpoint) - setpointHighOK: ${state.setpointHighOK} - setpointLowOK: ${state.setpointLowOK}"
        if(state.setpointHighOK == "no") theMessage = "${messageH}"
        if(state.setpointLowOK == "no") theMessage = "${messageL}"
        if((state.setpointHighOK == "no") && (state.setpointLowOK == "no")) theMessage = "${messageB}"
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
    
    if(state.message) { 
        if (state.message.contains("%whatHappened%")) {state.message = state.message.replace('%whatHappened%', state.whatHappened)}
        if (state.message.contains("%whoHappened%")) {state.message = state.message.replace('%whoHappened%', state.whoHappened)}
        if (state.message.contains("%whoUnlocked%")) {state.message = state.message.replace('%whoUnlocked%', state.whoUnlocked)}       
        if (state.message.contains("%time%")) {state.message = state.message.replace('%time%', state.theTime)}
        if (state.message.contains("%time1%")) {state.message = state.message.replace('%time1%', state.theTime1)}

        if(logEnable) log.debug "In messageHandler - message: ${state.message}"
        msg = state.message
        if(useSpeech) letsTalk(msg)
        if(sendPushMessage) pushHandler(msg)
    }
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
	if(logEnable) log.debug "In currentDateTime (${state.version})"
	Date date = new Date()
	String datePart = date.format("dd/MM/yyyy")
	String timePart = date.format("HH:mm")
	String timePart1 = date.format("h:mm a")
	state.theTime = timePart		// 24 h
	state.theTime1 = timePart1		// AM PM
	if(logEnable) log.debug "In currentDateTime - ${state.theTime}"
}

def autoSunHandler() {
    // autoSunHandler - This is to trigger AT the exact times with offsets
    if(logEnable) log.debug "In autoSunHandler (${state.version}) - ${app.label}"
    
    def sunriseTime = getSunriseAndSunset().sunrise
    def sunsetTime = getSunriseAndSunset().sunset

    int theOffsetSunset = offsetSunset ?: 1    
    if(setBeforeAfter) {
        state.timeSunset = new Date(sunsetTime.time + (theOffsetSunset * 60 * 1000))
    } else {
        state.timeSunset = new Date(sunsetTime.time - (theOffsetSunset * 60 * 1000))
    }
    
    int theOffsetSunrise = offsetSunrise ?: 1
    if(riseBeforeAfter) {
        state.timeSunrise = new Date(sunriseTime.time + (theOffsetSunrise * 60 * 1000))
    } else {
        state.timeSunrise = new Date(sunriseTime.time - (theOffsetSunrise * 60 * 1000))
    }

    if(logEnable) log.debug "In autoSunHandler - sunsetTime: ${sunsetTime} - theOffsetSunset: ${theOffsetSunset} - setBeforeAfter: ${setBeforeAfter}"
    if(logEnable) log.debug "In autoSunHandler - sunriseTime: ${sunriseTime} - theOffsetSunrise: ${theOffsetSunrise} - riseBeforeAfter: ${riseBeforeAfter}"
    if(logEnable) log.debug "In autoSunHandler - ${app.label} - timeSunset: ${state.timeSunset} - timeAfterSunrise: ${state.timeSunrise}"

    // check for new sunset/sunrise times every day at 12:05 pm
    schedule("0 5 12 ? * * *", autoSunHandler)
        
    if(riseSet) { schedule(state.timeSunset, runAtTime1) }
    if(!riseSet) { schedule(state.timeSunrise, runAtTime2) }
}

def runAtTime1() {
    if(logEnable) log.debug "In runAtTime1 (${state.version}) - ${app.label} - Starting"
    startTheProcess()
}

def runAtTime2() {
    if(logEnable) log.debug "In runAtTime2 (${state.version}) - ${app.label} - Starting"
    startTheProcess()
}

def checkTimeSun() {
    // checkTimeSun - This is to ensure that the it's BETWEEN sunset/sunrise with offsets
	if(logEnable) log.debug "In checkTimeSun (${state.version})"
    if(sunRestriction) {    
        def nextSunrise = (getSunriseAndSunset().sunrise)+1
        def nextSunset = getSunriseAndSunset().sunset
        
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
			state.sunRiseTosunSet = true
            state.nothingToDo = false
		} else {
            if(logEnable) log.debug "In checkTimeSun - Time outside of range"
			state.sunRiseTosunSet = false
            if(reverse) state.nothingToDo = false
		}
  	} else {
        if(logEnable) log.debug "In checkTimeSun - NOT Specified"
		state.timeBetweenSun = true
  	}
    
    if(logEnable) log.debug "In checkTimeSun - timeBetweenSun: ${state.timeBetweenSun} - nothingToDo: ${state.nothingToDo}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version})"
	if(fromTime && toTime) {
        if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
        if(midnightCheckR) {
            state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime)+1, new Date(), location.timeZone)
        } else {
		    state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
        }
		if(state.betweenTime) {
            if(logEnable) log.debug "In checkTime - Time within range"
			state.timeBetween = true
            state.nothingToDo = false
		} else {
            if(logEnable) log.debug "In checkTime - Time outside of range"
			state.timeBetween = false
            if(reverse) state.nothingToDo = false
		}
  	} else {  
        if(logEnable) log.debug "In checkTime - NO Time Restriction Specified"
		state.timeBetween = true
  	}
    
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween} - nothingToDo: ${state.nothingToDo}"
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
        if(logEnable) log.debug "In dayOfTheWeekHandler - NO Days Specified"
        state.daysMatch = true
    }
    
    if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch} - nothingToDo: ${state.nothingToDo}"
}

def setLevelandColorHandler() {
    if(state.fromWhere == "slowOff") {
        state.onLevel = state.highestLevel
    } else {
	    state.onLevel = state.onLevel ?: 99
    }
    if(state.color == null || state.color == "null" || state.color == "") state.color = "No Change"
    
    if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, color: ${state.color} - onLevel: ${state.onLevel}"
    
    switch(state.color) {
        case "No Change":
            hueColor = null
            saturation = null
            break;
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
	if(saturation == null) saturation = 100
    
    if(logEnable) log.debug "In setLevelandColorHandler - 1 - hue: ${hueColor} - saturation: ${saturation} - onLevel: ${onLevel}"
    
    
	if(state.fromWhere == "dimmerOn") {
        if(logEnable) log.debug "In setLevelandColorHandler - dimmerOn - setOnLC: ${setOnLC}"  
        state.oldMap = [:]
        state.oldLevelMap = [:]
        setOnLC.each {
            if(state.color == "No Change") {
                hueColor = it.currentValue("hue")
                saturation = it.currentValue("saturation")              
            }
            
            def value = [hue: hueColor, saturation: saturation, level: onLevel] 
            if(logEnable) log.debug "In setLevelandColorHandler - 2 - hue: ${hueColor} - saturation: ${saturation} - onLevel: ${onLevel}"
            
        	if (it.hasCommand('setColor')) {
                oldHueColor = it.currentValue("hue")
                oldSaturation = it.currentValue("saturation")
                oldLevel = it.currentValue("level")
                name = (it.displayName).replace(" ","")
                status = it.currentValue("switch")
                oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}"
                state.oldMap.put(name,oldStatus) 
                if(logEnable) log.debug "In setLevelandColorHandler - setColor - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
                if(logEnable) log.debug "In setLevelandColorHandler - setColor - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
                oldLevel = it.currentValue("level")
                name = (it.displayName).replace(" ","")
                status = it.currentValue("switch")
                oldStatus = "${status}::${oldLevel}"
                state.oldLevelMap.put(name,oldStatus)
                if(logEnable) log.debug "In setLevelandColorHandler - setLevel - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
            	if(logEnable) log.debug "In setLevelandColorHandler - setLevel - $it.displayName, setLevel($value)"
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

def getLockCodeNames(myDev) {  // Special thanks to Bruce @bravenel for this code
	def list = ["**Any Lock Code**"] + getLockCodesFromDevice(myDev).tokenize(",")
	return list
}

def getLockCodesFromDevice(device) {  // Special thanks to Bruce @bravenel for this code
	def lcText = device?.currentValue("lockCodes")
	if (!lcText?.startsWith("{")) {
		lcText = decrypt(lcText)
	} 
	def lockCodes
	if(lcText) lockCodes = new JsonSlurper().parseText(lcText)
	def result = ""
	lockCodes.each {if(it.value.name) result += it.value.name + ","}
	return result ? result[0..-2] : ""
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
            theStatus = it.currentValue("switch")
            if(theStatus == "on") { state.eSwitch = true }
        }
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}"
    }
}

def setDefaults(){
	if(logEnable == null){logEnable = false}
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

def display(data) {
    if(data == null) data = ""
    setVersion()
    getHeaderAndFooter()
    
    if(app.label) {
        if(app.label.contains("(Paused)")) {
            theName = app.label - " <span style='color:red'>(Paused)</span>"
        } else {
            theName = app.label
        }
    }
    
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
		paragraph getFormat("line")
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
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
