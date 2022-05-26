/**
*  **************** Event Engine Cog ****************
*
*  Design Usage:
*  Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!
*
*  Copyright 2020-2022 Bryan Turcotte (@bptworld)
* 
*  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums! Thanks.
*
*  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
*  Donations are never necessary but always appreciated. Donations to support development efforts are accepted via: 
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
*  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! - @BPTWorld
*
*  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
*
* ------------------------------------------------------------------------------------------------------------------------------
*
*  Changes:
*
* * - Still more to do with iCal (work on reoccuring)
* * - Need to Fix sorting with event engine cog list
*
*  3.7.4 - 05/25/22 - Adjustments
*  3.7.3 - 05/25/22 - Added Hub Variables to both Conditions and Actions!
*  3.7.2 - 05/24/22 - Adjustment to 'special message' in Lock handler
*  3.7.1 - 05/23/22 - Added Look for a 'special' lock message when all other options won't work
*  3.7.0 - 05/23/22 - Added wildcard %whoLocked% to message options
*  ---
*  1.0.0 - 09/05/20 - Initial release.
*/




def setVersion(){
    state.name = "Event Engine"
    state.version = "3.7.4"
    sendLocationEvent(name: "updateVersionInfo", value: "${state.name}:${state.version}")
}

definition(
    name: "Event Engine Cog",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!",
    category: "Convenience",
    parent: "BPTWorld:Event Engine",
    installOnOpen: true,
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Event%20Engine/EE-child.groovy",
    singleThreaded: true
)

preferences {
    page name: "pageConfig"
    page name: "notificationOptions", title: "", install:false, uninstall:false, nextPage: "pageConfig"
}

@Field static String theCogTriggers = ""
@Field static String theCogActions = ""
@Field static String theCogNotifications = ""

def pageConfig() {
    dynamicPage(name: "", title: "", install:true, uninstall:true, refreshInterval:0) {
        display()
        testLogEnable = false
        theCogTriggers += "<b><u>Conditions</u></b><br>"
        section("Instructions:", hideable:true, hidden:true) {
            paragraph "<b>Notes:</b>"
            paragraph "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!"
            paragraph "<abbr title='Look for these little INFO tags to receive more information about the option.'><b>INFO</b></abbr> - Hover over for more!"
            paragraph "<hr>"
            cVSr = "<b>Condition versus Restriction:</b><br>"
            cVSr += "When choosing conditions, it’s important to note the difference between conditions and restrictions.<br><br>"
            cVSr += "<u>Conditions</u> - This is what the cog looks at to start the whole process. Without conditions nothing at all would happen. When a condition is met, the Cog fires. If the condition is true, everything goes as planned (lights turn on, notifications are sent, etc.). If the condition is false and a ‘reverse’ is set. That portion of the cog will run. (lights turn off, etc.)<br><br>"
            cVSr += "<u>Restrictions</u> - The is the ignition key of the cog. If ‘xx as Restriction’ is true, and the condition is met, Cog will fire, if the condition is false, nothing will happen at all.<br><br>"
            cVSr += "Example Cog:<br>"
            cVSr += "<b>Conditions</b><br>"
            cVSr += "By Days - [Monday, Tuesday] - as Restriction: false<br>"
            cVSr += "Certain Time - 2020-09-27T06:40:00.000-0400 - Repeat: false - Schedule: NA<br>"
            cVSr += "<b>Actions</b><br>"
            cVSr += "Switches to turn On: [Test Zwave Bulb - Multi Color]<br>"
            cVSr += "Reverse: true - Delay On Reverse: false<br><br>"
            cVSr += "In this example:<br>"
            cVSr += "When days equal Monday or Tuesday @ 6:40am the light will come on.<br>"
            cVSr += "When days don’t equal Monday or Tuesday, the light will go in ‘reverse’. In this case, they would turn off @ 6:40am.<br><br>"
            cVSr += "By simply changing the ‘as Restriction’ to true<br>"
            cVSr += "When days = Monday or Tuesday @ 6:40am the light will come on.<br>"
            cVSr += "When days don’t equal Monday or Tuesday, Nothing at all will happen."
            paragraph "${cVSr}"
            paragraph "<hr>"
            bTt = "<b>Between Two Times (including Sunrise/Sunset):</b><br>"
            bTt += "Between two times will look at the other conditions when the first time is reached. Turning on and off devices as specified in the cog. During the in between time, it will then continue to work - turning things on/off/dim/etc - including Reverse if it is chosen.<br>"
            bTt += "Once the end time is reached, it will once again look at the conditions and (if chosen) Reverse the devices.<br>"
            bTt += "If ‘Used as Restriction’ is selected, it will not reverse the devices when the end time is reached."
            paragraph "${bTt}"
            paragraph "<hr>"
            rf = "<b>Reverse Feature:</b><br>"
            rf += "Reverse is a unique feature to Event Engine. It works with ‘Turn Light(s) On, Set Level and Color’.<br><br>"
            rf += "<u>Basically:</u><br>"
            rf += "If the level was different, it will revert back to that level<br>"
            rf += "If the color was different, it will revert back to that color<br>"
            rf += "If switch was off, it will turn off<br><br>"
            rf += "<u>Simple Example</u><br>"
            rf += "A light is off.<br>"
            rf += "A cog becomes true and turns the light on.<br>"
            rf += "After a while the cog becomes false.<br>"
            rf += "The light turns back off.<br><br>"
            rf += "<u>More advanced Example</u><br>"
            rf += "A light is off. (last time it was on, it was color: blue - level: 70)<br>"
            rf += "A cog becomes true and turns the light on (color: red - level: 99).<br>"
            rf += "After a while the cog becomes false.<br>"
            rf += "The light goes back to color:blue - level:70, then turns off."
            paragraph "${rf}"
            paragraph "<hr>"
        }        
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Conditions")) {
            input "triggerType", "enum", title: "Condition Type <small><abbr title='Description and examples can be found at the top of Cog, in Instructions.'><b>- INFO -</b></abbr></small>", options: [
                ["tTimeDays":"Time/Days/Mode/Holidays - Sub-Menu"],
                ["xAcceleration":"Acceleration Sensor"],
                ["xBattery":"Battery Setpoint"],
                ["xButton":"Button"],
                ["xContact":"Contact Sensors"],
                ["xDirectional":"Directional Condition"],
                ["xEnergy":"Energy Setpoint"],
                ["xEventLogWatchdog":"Event/Log Watchdog (beta)"],
                ["xGarageDoor":"Garage Doors"],
                ["xGVar":"EE Global Variables"],
                ["xHSMAlert":"HSM Alerts (Beta)"],
                ["xHSMStatus":"HSM Status (Beta)"],
                ["xHubCheck":"Hub Check Options"],
                ["xHubVariable":"Hub Variables"],
                ["xHumidity":"Humidity Setpoint"],
                ["xIlluminance":"Illuminance Setpoint"],
                ["xIPPing":"IP Ping"],
                ["xLock":"Locks"],
                ["xMotion":"Motion Sensors"],
                ["xPower":"Power Setpoint"],
                ["xPresence":"Presence Sensor"],
                ["xSecurityKeypad":"Ring Security Keypad G2"],
                ["xSNStatus":"Safety Net Status"],
                ["xSwitch":"Switches"],
                ["xSystemStartup":"Sytem Startup"],
                ["xTemp":"Temperature Setpoint"],
                ["xTherm":"Thermostat Activity"],
                ["xTransition":"Transitions"],
                ["xVoltage":"Voltage Setpoint"],
                ["xWater":"Water Sensor"],
                ["xCustom":"** Custom Attribute **"]
            ], required:false, multiple:true, submitOnChange:true, width:6

            if(triggerType == null) triggerType = ""
            if(timeDaysType == null) timeDaysType = ""
            if(state.conditionsMap == null) state.conditionsMap = [:]
            if(triggerType != "") {
                theData = "${triggerType}"
                state.conditionsMap.put("triggerType",theData)
            } else {
                state.conditionsMap.remove("triggerType")
            }

            if(triggerType.contains("tTimeDays")) {
                input "timeDaysType", "enum", title: "Time/Days/Mode/Holidays - Sub-Menu", options: [
                    ["tBetween":"Between Two Times"],
                    ["tMode":"By Mode"],
                    ["tDays":"By Days"],
                    ["tTime":"Certain Time"],
                    ["tcertainTimeHasPassed":"Certain Time Has Passed"],
                    ["tHoliday":"Holidays (Calendarific)"],
                    ["tIcal":"iCal Events (beta)"],
                    ["tSunrise":"Just Sunrise"],
                    ["tSunset":"Just Sunset"],
                    ["tPeriodic":"Periodic Expression"],
                    ["tSunsetSunrise":"Sunset/Sunrise"]
                ], required:true, multiple:true, submitOnChange:true, width:6
                paragraph "<hr>"
            } else {
                paragraph " ", width:6
                app.removeSetting("timeDaysType")
            }
            if(timeDaysType != "") {
                theData = "${timeDaysType}"
                state.conditionsMap.put("timeDaysType",theData)
            } else {
                state.conditionsMap.remove("timeDaysType")
            }         
            input "triggerAndOr", "bool", title: "Use 'AND' or 'OR' between Condition types <small><abbr title='‘AND’ requires that all selected conditions are true. ‘OR’ requires that any selected condition is true'><b>- INFO -</b></abbr></small>", description: "andOr", submitOnChange:true, width:12
            if(triggerAndOr) {
                theData = "${triggerAndOr}"
                state.conditionsMap.put("triggerAndOr",theData)
                paragraph "Cog will fire when <b>ANY</b> Condition is true"
                theCogTriggers += "<b>*</b> Cog will fire when <b>ANY</b> Condition is true (Using OR)<br>"
            } else {
                theData = "${triggerAndOr}"
                state.conditionsMap.put("triggerAndOr",theData)
                paragraph "Cog will fire when <b>ALL</b> Conditions are true"
                theCogTriggers += "<b>*</b> Cog will fire when <b>ALL</b> Condition are true (Using AND)<br>"
            }
            paragraph "<small>* Excluding any Time/Days/Mode selections.</small>"
            paragraph "<hr>"
            theData = "${triggerAndOr}"
            state.conditionsMap.put("triggerAndOr",theData)
            // -----------
            if(timeDaysType.contains("tPeriodic")) {
                paragraph "<b>By Periodic</b>"
                input "preMadePeriodic", "text", title: "Enter in a Periodic Cron Expression to 'Run the Cog' <small><abbr title='Use a Periodic Cron Expression Generator to create powerful schedules.'><b>- INFO -</b></abbr></small>", required:false, submitOnChange:true
                paragraph "In addtion to setting up an Expression to start the Cog, you can enter in a second Expression to 'Reverse the Cog'. ie. When the Cog is first run, it turns on a light.  When the second expression is triggered, it will turn the light off."
                input "preMadePeriodic2", "text", title: "Enter in a Periodic Cron Expression to 'Reverse the Cog' (optional)", required:false, submitOnChange:true
                paragraph "Premade cron expressions can be found at <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a>. Remember, Format and spacing is critical."
                paragraph "Or create your own Expressions locally using the 'Periodic Expressions' app found in Hubitat Package Manager or on <a href='https://github.com/bptworld/Hubitat/' target='_blank'>my GitHub</a>."
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By Periodic - Run: ${preMadePeriodic} - Reverse: ${preMadePeriodic2}<br>"
            } else {
                app.removeSetting("preMadePeriodic")
            }
// -----------
            if(timeDaysType.contains("tMode")) {
                paragraph "<b>By Mode</b>"
                input "modeEvent", "mode", title: "By Mode <small><abbr title='Choose the Modes to use with this Cog'><b>- INFO -</b></abbr></small>", multiple:true, submitOnChange:true                
                input "modeCondition", "bool", title: "When in the selected mode(s) (off) - or - When NOT in the selected Mode(s) (on)", description: "mode", submitOnChange:true
                if(modeCondition) {
                    paragraph "Condition is true when NOT in modes selected."
                } else {
                    paragraph "Condition is true when in modes selected."
                }
                input "modeMatchRestriction", "bool", title: "By Mode as Restriction <small><abbr title='When used as a Restriction, if condidtion is not met nothing will happen based on this condition.'><b>- INFO -</b></abbr></small>", description: "By Mode Restriction", submitOnChange:true
                input "modeMatchConditionOnly", "bool", title: "Use Mode as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By Mode - ${modeEvent} - Not while in selected Modes: ${modeCondition} - as Restriction: ${modeMatchRestriction} - just Condition: ${modeMatchConditionOnly}<br>"
            } else {
                app.removeSetting("modeEvent")
                app.removeSetting("modeCondition")
                app.removeSetting("modeMatchRestriction")
                app.removeSetting("modeMatchConditionOnly")
            }
// -----------
            if(timeDaysType.contains("tDays")) {
                paragraph "<b>By Days</b>"
                input "days", "enum", title: "Activate on these days <small><abbr title='Choose the Days to use with this Cog'><b>- INFO -</b></abbr></small>", description: "Days to Activate", required:true, multiple:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                paragraph "<hr>"
                paragraph "You can also choose if the condition will trigger only on Even or Odd days. Great for watering schedules.<br>Or choose by Even or Odd weeks. Great for Recycle schedules."
                input "useDayWeek", "bool", title: "Use Day (off) or Week (On)", description: "Day or Week", defaultVAlue:false, submitOnChange:true, width:6
                if(useDayWeek) {
                    paragraph "Week starts on Sunday and ends on Saturday", width:6
                    input "evenDays", "bool", title: "But only on Even Weeks (2,4,6,8,etc)", description: "Even Weeks", submitOnChange:true, width:6
                    input "oddDays", "bool", title: "But only on Odd Weeks (1,3,5,7,etc)", description: "Odd Weeks", submitOnChange:true, width:6
                    if(evenDays && oddDays) paragraph "<b>Please only select one option, either Even or Odd, not both!</b>"
                    Date date = new Date() 
                    numberWeek = date[Calendar.WEEK_OF_YEAR]
                    nDay = numberWeek.toInteger()
                    if(nDay % 2 == 0) { 
                        weekIS = "even"
                    } else {
                        weekIS = "odd"
                    }
                    paragraph "<b>The current week number is ${numberWeek}, which is an ${weekIS} number.</b>"    
                    theCogTriggers += "<b>-</b> By Weeks - ${days} - Even: ${evenDays} - Odd: ${oddDays} - as Restriction: ${daysMatchRestriction} - just Condition: ${daysMatchConditionOnly}<br>"
                } else {
                    input "useDayMonthYear", "bool", title: "Use Day of the Month (off) or Day of the Year (on)", description: "The Days", submitOnChange:true, width:6
                    input "evenDays", "bool", title: "But only on Even Days (2,4,6,8,etc)", description: "Even Days", submitOnChange:true, width:6
                    input "oddDays", "bool", title: "But only on Odd Days (1,3,5,7,etc)", description: "Odd Days", submitOnChange:true, width:6
                    if(evenDays && oddDays) paragraph "<b>Please only select one option, either Even or Odd, not both!</b>"
                    Date date = new Date()
                    if(useDayMonthYear) {
                        numberDay = date[Calendar.DAY_OF_YEAR]
                    } else {
                        numberDay = date[Calendar.DAY_OF_MONTH]
                    }
                    nDay = numberDay.toInteger()
                    if(nDay % 2 == 0) { 
                        dayIS = "even"
                    } else {
                        dayIS = "odd"
                    }
                    paragraph "<b>The current day number is ${numberDay}, which is an ${dayIS} number.</b>"
                    theCogTriggers += "<b>-</b> By Days - ${days} - Use Month/Year: ${useDayMonthYear} - Even: ${evenDays} - Odd: ${oddDays} - as Restriction: ${daysMatchRestriction} - just Condition: ${daysMatchConditionOnly}<br>"
                }
                paragraph "<hr>"
                input "daysMatchRestriction", "bool", title: "By Days as Restriction <small><abbr title='When used as a Restriction, if condidtion is not met nothing will happen based on this condition.'><b>- INFO -</b></abbr></small>", description: "By Days Restriction", submitOnChange:true
                input "daysMatchConditionOnly", "bool", title: "Use Days as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                paragraph "<b>Reminder: You'll need to select a Time for the Cog to run on the Days selected here.</b>"
                paragraph "<hr>"
            } else {
                app.removeSetting("days")
                app.removeSetting("useDayWeek")
                app.removeSetting("evenDays")
                app.removeSetting("oddDays")
                app.removeSetting("daysMatchRestriction")
                app.removeSetting("daysMatchConditionOnly")
            }
// -----------
            if(timeDaysType.contains("tTime")) {
                paragraph "<b>Certain Time</b>"
                input "startTime", "time", title: "Time to activate <small><abbr title='Exact time for the Cog to run'><b>- INFO -</b></abbr></small>", description: "Time", required:false, width:12
                input "repeat", "bool", title: "Repeat", description: "Repeat <small><abbr title='Choose for the Cog to repeat or not'><b>- INFO -</b></abbr></small>", submitOnChange:true
                if(repeat) {
                    input "repeatType", "enum", title: "Repeat schedule <small><abbr title='Choose how often to repeat'><b>- INFO -</b></abbr></small>", options: [
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
                if(startTime) theDate = toDateTime(startTime)
                theCogTriggers += "<b>-</b> Certain Time - ${theDate} - Repeat: ${repeat} - Schedule: ${repeatType}<br>"
            } else {
                app.removeSetting("startTime")
                app.removeSetting("repeat")
                app.removeSetting("repeatType")
                app.removeSetting("repeat")
            }
// -----------
            if(timeDaysType.contains("tBetween")) {
                paragraph "<b>Between two times</b> <small><abbr title='Description and examples can be found at the top of Cog, in Instructions.'><b>- INFO -</b></abbr></small>"
                input "fromTime", "time", title: "From <small><abbr title='Exact time for the Cog to start'><b>- INFO -</b></abbr></small>", required:false, width: 6, submitOnChange:true
                input "toTime", "time", title: "To <small><abbr title='Exact time for the Cog to End'><b>- INFO -</b></abbr></small>", required:false, width: 6, submitOnChange:true
                input "timeBetweenRestriction", "bool", title: "Between two times as Restriction <small><abbr title='When used as a Restriction, if condidtion is not met nothing will happen based on this condition.'><b>- INFO -</b></abbr></small>", description: "Between two times Restriction", submitOnChange:true
                input "timeBetweenMatchConditionOnly", "bool", title: "Use Time Between as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(fromTime && toTime) {
                    theDate1 = toDateTime(fromTime)
                    theDate2 = toDateTime(toTime)            
                    toValue = theDate2.compareTo(theDate1)
                    if(toValue > 0) {
                        nextToDate = theDate2
                    } else {
                        nextToDate = theDate2.next()
                    }
                    state.betweenTime = timeOfDayIsBetween(theDate1, nextToDate, new Date(), location.timeZone)
                    paragraph "From: ${theDate1} - To: ${nextToDate}<br>Currently, Between equals ${state.betweenTime}"
                }
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> Between two times - From: ${theDate1} - To: ${nextToDate} - as Restriction: ${timeBetweenRestriction} - just Condition: ${timeBetweenMatchConditionOnly}<br>"
            } else {
                app.removeSetting("fromTime")
                app.removeSetting("toTime")
                app.removeSetting("timeBetweenRestriction")
                app.removeSetting("timeBetweenMatchConditionOnly")
            }
// -----------
            if(timeDaysType.contains("tSunsetSunrise")) {
                if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunrise")) {
                    paragraph "<b>'Sunset/Sunrise' and 'Just Sunrise' can not be used at the same time. Please deselect one of them.</b>"
                } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunset")) {
                    paragraph "<b>'Sunset/Sunrise' and 'Just Sunset' can not be used at the same time. Please deselect one of them.</b>"
                } else {
                    paragraph "<b>Sunset/Sunrise</b>"
                    input "fromSun", "bool", title: "Sunset to Sunrise (off) or Sunrise to Sunset (on) <small><abbr title='Choose when the Cog will be active'><b>- INFO -</b></abbr></small>", submitOnChange:true, width:6
                    if(fromSun) {
                        paragraph "Sunrise <small><abbr title='Choose whether or not you want to use offsets. Each offset can be before or after and have a selectable number of minutes.'><b>- INFO -</b></abbr></small>"
                        input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", submitOnChange:true, width:6
                        input "offsetSunrise", "number", title: "Offset(minutes) <small><abbr title='Enter 99 for a Random offset!'><b>- INFO -</b></abbr></small>", width:6, submitOnChange:true
                        if(offsetSunrise == 99) {
                            input "sunriseDelayLow", "number", title: "Random Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            input "sunriseDelayHigh", "number", title: "Random Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            if(sunriseDelayHigh <= sunriseDelayLow) { paragraph "<b>Delay High must be greater than Delay Low.</b>" }
                            theCogTriggers += "<b>-</b> Sunrise Random Delay - Delay Low: ${sunriseDelayLow} - Delay High: ${sunriseDelayHigh}<br>"
                        } else {
                            app.removeSetting("sunriseDelayLow")
                            app.removeSetting("sunriseDelayHigh")
                        }
                        paragraph "Sunset"
                        input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", submitOnChange:true, width:6
                        input "offsetSunset", "number", title: "Offset (minutes) <small><abbr title='Enter 99 for a Random offset!'><b>- INFO -</b></abbr></small>", width:6, submitOnChange:true
                        if(offsetSunset == 99) {
                            input "sunsetDelayLow", "number", title: "Random Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            input "sunsetDelayHigh", "number", title: "Random Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            if(sunsetDelayHigh <= sunsetDelayLow) { paragraph "<b>Delay High must be greater than Delay Low.</b>" }
                            theCogTriggers += "<b>-</b> Sunset Random Delay - Delay Low: ${sunsetDelayLow} - Delay High: ${sunsetDelayHigh}<br>"
                        } else {
                            app.removeSetting("sunsetDelayLow")
                            app.removeSetting("sunsetDelayHigh")
                        }
                    } else {
                        paragraph "Sunset <small><abbr title='Choose whether or not you want to use offsets. Each offset can be before or after and have a selectable number of minutes.'><b>- INFO -</b></abbr></small>"
                        input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", width:6, submitOnChange:true
                        input "offsetSunset", "number", title: "Offset (minutes) <small><abbr title='Enter 99 for a Random offset!'><b>- INFO -</b></abbr></small>", width:6, submitOnChange:true
                        if(offsetSunset == 99) {
                            input "sunsetDelayLow", "number", title: "Random Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            input "sunsetDelayHigh", "number", title: "Random Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            if(sunsetDelayHigh <= sunsetDelayLow) { paragraph "<b>Delay High must be greater than Delay Low.</b>" }
                            theCogTriggers += "<b>-</b> Sunset Random Delay - Delay Low: ${sunsetDelayLow} - Delay High: ${sunsetDelayHigh}<br>"
                        } else {
                            app.removeSetting("sunsetDelayLow")
                            app.removeSetting("sunsetDelayHigh")
                        }
                        paragraph "Sunrise"
                        input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", submitOnChange:true, width:6
                        input "offsetSunrise", "number", title: "Offset(minutes) <small><abbr title='Enter 99 for a Random offset!'><b>- INFO -</b></abbr></small>", width:6, submitOnChange:true
                        if(offsetSunrise == 99) {
                            input "sunriseDelayLow", "number", title: "Random Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            input "sunriseDelayHigh", "number", title: "Random Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                            if(sunriseDelayHigh <= sunriseDelayLow) { paragraph "<b>Delay High must be greater than Delay Low.</b>" }
                            theCogTriggers += "<b>-</b> Sunrise Random Delay - Delay Low: ${sunriseDelayLow} - Delay High: ${sunriseDelayHigh}<br>"
                        } else {
                            app.removeSetting("sunriseDelayLow")
                            app.removeSetting("sunriseDelayHigh")
                        }
                    }
                    paragraph "<small>* Be sure offsets don't cause the time to cross back and forth over midnight or this won't work as expected.</small>"
                    input "timeBetweenSunRestriction", "bool", title: "Sunset/Sunrise as Restriction <small><abbr title='When used as a Restriction, if condidtion is not met nothing will happen based on this condition.'><b>- INFO -</b></abbr></small>", description: "Sunset/Sunrise Restriction", submitOnChange:true
                    checkSunHandler()
                    input "sunsetSunriseMatchConditionOnly", "bool", title: "Use Sunset/Sunrise as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                    paragraph "<hr>"
                    if(fromSun) {
                        theCogTriggers += "<b>-</b> Sunrise to Sunset - Sunrise Offset: ${offsetSunrise}, BeforeAfter: ${riseBeforeAfter} - Sunset Offset: ${offsetSunset} - BeforeAfter: ${setBeforeAfter} - with Restriction: ${timeBetweenSunRestriction} - just Condition: ${sunsetSunriseMatchConditionOnly}<br>"
                        theCogTriggers += "<b>-</b> After Offsets - timeSunrise: ${state.timeSunrise} - timeSunset: ${state.timeSunset}<br>"
                    } else {
                        theCogTriggers += "<b>-</b> Sunset to Sunrise - Sunset Offset: ${offsetSunset} - BeforeAfter: ${setBeforeAfter} - Sunrise Offset: ${offsetSunrise} - BeforeAfter: ${riseBeforeAfter} - with Restriction: ${timeBetweenSunRestriction} - just Condition: ${sunsetSunriseMatchConditionOnly}<br>"
                        theCogTriggers += "<b>-</b> After Offsets - timeSunset: ${state.timeSunset} - timeSunrise: ${state.timeSunrise}<br>"
                    }
                }
            } else {
                app.removeSetting("timeBetweenSunRestriction")
                app.removeSetting("timeBetweenSunMatchConditionOnly")
            }
// -----------
            if(timeDaysType.contains("tSunrise") && timeDaysType.contains("tSunset")) {
                paragraph "<b>Please select 'Sunset/Sunrise', instead of both 'Just Sunrise' and 'Just Sunset'.</b>"
            } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunrise")) {
                // Messge above will show
            } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunset")) {
                // Messge above will show
            } else if(timeDaysType.contains("tSunrise")) {
                paragraph "<b>Just Sunrise</b> <small><abbr title='This is the start time of the Cog. An offset can also be selected.'><b>- INFO -</b></abbr></small>"
                input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", submitOnChange:true, width:6
                input "offsetSunrise", "number", title: "Offset (minutes) <small><abbr title='Enter 99 for a Random offset!'><b>- INFO -</b></abbr></small>", width:6, submitOnChange:true
                if(offsetSunrise == 99) {
                    input "sunriseDelayLow", "number", title: "Random Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                    input "sunriseDelayHigh", "number", title: "Random Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                    if(sunriseDelayHigh <= sunriseDelayLow) { paragraph "<b>Delay High must be greater than Delay Low.</b>" }
                    theCogTriggers += "<b>-</b> Sunrise Random Delay - Delay Low: ${sunriseDelayLow} - Delay High: ${sunriseDelayHigh}<br>"
                } else {
                    app.removeSetting("sunriseDelayLow")
                    app.removeSetting("sunriseDelayHigh")
                }
                input "sunriseToTime", "bool", title: "Set a certain time to turn off <small><abbr title='Choose this to also include an end time.'><b>- INFO -</b></abbr></small>", submitOnChange:true
                if(sunriseToTime) {
                    input "sunriseEndTime", "time", title: "Time to End", description: "Time", required:false
                    paragraph "<small>* Must be BEFORE midnight.</small>"
                } else {
                    app.removeSetting("sunriseEndTime")
                }
                checkSunHandler()
                paragraph "<hr>"
                if(sunriseEndTime) theDate = toDateTime(sunriseEndTime)
                theCogTriggers += "<b>-</b> Just Sunrise - Sunrise Offset: ${offsetSunrise}, BeforeAfter: ${riseBeforeAfter} - Time to End: ${theDate}<br>"
                theCogTriggers += "<b>-</b> After Offsets - timeSunrise: ${state.timeSunrise}<br>"
            } else if(timeDaysType.contains("tSunset")) {
                paragraph "<b>Just Sunset</b>"
                input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset <small><abbr title='This is the start time of the Cog. An offset can also be selected.'><b>- INFO -</b></abbr></small>", submitOnChange:true, width:6
                input "offsetSunset", "number", title: "Offset (minutes) <small><abbr title='Enter 99 for a Random offset!'><b>- INFO -</b></abbr></small>", width:6, submitOnChange:true
                if(offsetSunset == 99) {
                    input "sunsetDelayLow", "number", title: "Random Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                    input "sunsetDelayHigh", "number", title: "Random Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', width:6, submitOnChange:true
                    if(sunsetDelayHigh <= sunsetDelayLow) { paragraph "<b>Delay High must be greater than Delay Low.</b>" }
                    theCogTriggers += "<b>-</b> Sunset Random Delay - Delay Low: ${sunsetDelayLow} - Delay High: ${sunsetDelayHigh}<br>"
                } else {
                    app.removeSetting("sunsetDelayLow")
                    app.removeSetting("sunsetDelayHigh")
                }
                input "sunsetToTime", "bool", title: "Set a certain time to turn off <small><abbr title='Choose this to also include an end time.'><b>- INFO -</b></abbr></small>", submitOnChange:true
                if(sunsetToTime) {
                    input "sunsetEndTime", "time", title: "Time to End", description: "Time", required:false
                    paragraph "<small>* Must be BEFORE midnight.</small>"
                } else {
                    app.removeSetting("sunsetEndTime")
                }
                checkSunHandler()
                paragraph "<hr>"
                if(sunsetEndTime) theDate = toDateTime(sunsetEndTime)
                theCogTriggers += "<b>-</b> Just Sunset - Sunset Offset: ${offsetSunset}, BeforeAfter: ${setBeforeAfter} - Time to End: ${theDate}<br>"
                theCogTriggers += "<b>-</b> After Offsets - timeSunset: ${state.timeSunset}<br>"
            } else {
                app.removeSetting("sunsetEndTime")
            }

            if(!timeDaysType.contains("tSunsetSunrise") && !timeDaysType.contains("tSunrise") && !timeDaysType.contains("tSunset")) {
                app.removeSetting("offsetSunrise")
                app.removeSetting("offsetSunset")
                app.removeSetting("setBeforeAfter")
                app.removeSetting("riseBeforeAfter")
                app.removeSetting("fromSun")
                app.removeSetting("sunsetDelayLow")
                app.removeSetting("sunsetDelayHigh")
            }
// -----------          
            if(timeDaysType.contains("tHoliday")) {
                paragraph "<b>Holidays using Calendarific</b>"
                if(parent.apiKey) {
                    if(state.cName == null) { getAPICountries() }
                    input "apiCountry", "enum", title: "Select Country", options: state.cName.sort(), required:true, submitOnChange:true, width:6
                    input "apiYear", "enum", title: "Select Year", options: ["2020","2021","2022","2023","2024"], required:true, submitOnChange:true, width:6
                    if(apiCountry && apiYear) { 
                        getHolidayList()
                        input "apiHolidays", "enum", title: "Select Holiday", options: state.cHolidays.sort(), required:true, multiple:true, submitOnChange:true
                        if(apiHolidays) {
                            getHolidayInfo()
                            paragraph "${state.holidayInfo}"
                            paragraph "<hr>"
                        }
                    }
                    if(apiHolidays) {
                        input "apiTimeToTrigger", "time", title: "Time for the Cog to trigger, if Holiday", description: "Time", required:true, submitOnChange:true
                    }
                    if(apiTimeToTrigger) apiTime = toDateTime(apiTimeToTrigger)
                    theCogTriggers += "<b>-</b> By Holidays: ${apiCountry} - ${apiYear} - Time: ${apiTime}<br>"
                    theCogTriggers += " <b>-</b> Holidays: ${apiHolidays}<br>"
                } else {
                    paragraph "Calendarific API Key not found.  Please be sure to enter in your free Calendarific Key in the EE parent app.  If you don't have one, a link is also in the parent app."
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("apiCountry")
                app.removeSetting("apiYear")
                app.removeSetting("apiHolidays")
                app.removeSetting("apiTimeToTrigger")
            }
// -----------          
            if(timeDaysType.contains("tIcal")) {
                paragraph "<b>iCal Events</b>" 
                paragraph "Note: Right now this does not work with Reoccuring Events. Hopefully will find a work around soon."
                input "iCalLinks", "text", title: "Enter in your iCal addresses. Seperate multiple addresses with a semicolon (;)", required:true, submitOnChange:true
                theInstructions =  "Use the search field to search for a keyword or phrase within events.<br>"
                theInstructions += " - Enter in a Asterisk, * for all events<br>"
                theInstructions += " - Search in NOT case sensitive<br>"
                theInstructions += " - Seperate each word or phrase by a semicolon (;)<br>"
                theInstructions += " - ie. bryan, birthday, doctors appointment, red sox"
                paragraph theInstructions
                paragraph "Each morning at 12:02am, EE will check for events happening on that day and if they match your options. If it passes, events will be created."
                input "iCalSearch", "text", title: "iCal Search Words or Phrases", required:true, submitOnChange:true
                input "iCalTime", "time", title: "If event is an all day event, What time to trigger the actions", required:true, submitOnChange:true
                input "iCalPrior", "number", title: "If event is scheduled for a specific time, How many minutes prior to event to trigger the actions", required:true, submitOnChange:true
                if(iCalLinks) {
                    input "showEvents", "bool", title: "Show Upcoming Events (Takes a few seconds to load)", submitOnChange:true
                    if(showEvents) {
                        getIcalDataHandler()
                        tDate = new SimpleDateFormat("yyyyMMdd").parse(todaysDate)
                        paragraph "<u><b>Today - ${tDate}:</b></u><br>"
                        mapAllToday = "<table width=100% align=center><tr><td width=30%><b>Start Date/Time</b><td width=70%><b>Summary</b>"
                        state.iCalMap1.each { theMap ->
                            theKey = theMap.key
                            getZdate(theKey)
                            theValue = theMap.value
                            (endDate, summary) = theValue.split(";")
                            mapAllToday += "<tr><td>$state.zDate<td>$summary"
                        }
                        mapAllToday += "</table>"
                        paragraph mapAllToday

                        tDate1 = new SimpleDateFormat("yyyyMMdd").parse(todaysDate1)
                        paragraph "<u><b>Tomorrow - $tDate1}:</b></u><br>"
                        mapAll1 = "<table width=100% align=center><tr><td width=30%><b>Start Date/Time</b><td width=70%><b>Summary</b>"
                        state.iCalMap2.each { theMap ->
                            theKey = theMap.key
                            getZdate(theKey)
                            theValue = theMap.value
                            (endDate, summary) = theValue.split(";")
                            mapAll1 += "<tr><td>$state.zDate<td>$summary"
                        }
                        mapAll1 += "</table>"
                        paragraph mapAll1

                        tDate2 = new SimpleDateFormat("yyyyMMdd").parse(todaysDate2)
                        paragraph "<u><b>Two Days Out - ${tDate2}:</b></u><br>"
                        mapAll2 = "<table width=100% align=center><tr><td width=30%><b>Start Date/Time</b><td width=70%><b>Summary</b>"
                        state.iCalMap2.each { theMap ->
                            theKey = theMap.key
                            getZdate(theKey)
                            theValue = theMap.value
                            (endDate, summary) = theValue.split(";")
                            mapAll2 += "<tr><td>$state.zDate<td>$summary"
                        }
                        mapAll2 += "</table>"
                        paragraph mapAll2
                        paragraph "<hr>"
                        paragraph "<u><b>Way Out:</b></u><br>"
                        mapAll = "<table width=100% align=center><tr><td width=30%><b>Start Date/Time</b><td width=70%><b>Summary</b>"
                        state.iCalMapAll.each { theMap ->
                            theKey = theMap.key
                            getZdate(theKey)
                            theValue = theMap.value
                            (endDate, summary) = theValue.split(";")
                            mapAll += "<tr><td>$state.zDate<td>$summary"
                        }
                        mapAll += "</table>"
                        paragraph mapAll
                    }
                    theCogTriggers += "<b>-</b> By iCal Events: Search: ${iCalSearch} - Time: ${state.zDate} - Prior: ${iCalPrior}<br>"
                }
                if(!iCalLinks) {
                    app.removeSetting("iCalLinks")
                    app.removeSetting("iCalSearch")
                    app.removeSetting("iCalTime")
                    app.removeSetting("iCalPrior")
                }
            }
// -----------
            if(timeDaysType.contains("tcertainTimeHasPassed")) {
                paragraph "<b>Certain Time Has Passed</b>"
                input "certainTimeHasPassedEvent", "number", title: "Number of Minutes until this Cog can trigger again (minutes) <small><abbr title='Cog can not run until xx minutes has passed since the last time it ran.'><b>- INFO -</b></abbr></small>", submitOnChange:true
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> Certain Time Has Passed - ${certainTimeHasPassedEvent}<br>"
            } else {
                app.removeSetting("certainTimeHasPassedEvent")
            }
// -----------
            if(triggerType.contains("xAcceleration")) {
                paragraph "<b>Acceleration Sensor</b>"
                input "accelerationEvent", "capability.accelerationSensor", title: "By Acceleration Sensor", required:false, multiple:true, submitOnChange:true
                if(accelerationEvent) {
                    input "asInactiveActive", "bool", title: "Condition true when Inactive (off) or Active (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Acceleration", submitOnChange:true
                    if(asInactiveActive) {
                        paragraph "Condition true when Sensor(s) becomes Active"
                    } else {
                        paragraph "Condition true when Sensor(s) becomes Inactive"
                    }
                    input "accelerationANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(accelerationANDOR) {
                        paragraph "Condition true when <b>any</b> Acceleration Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Acceleration Sensors are true"
                    }
                    theCogTriggers += "<b>-</b> By Acceleration Sensor: ${accelerationEvent} - InactiveActive: ${asInactiveActive}, ANDOR: ${accelerationANDOR}<br>"
                } else {
                    app.removeSetting("accelerationEvent")
                    app.removeSetting("asInactiveActive")
                    app.removeSetting("accelerationANDOR")
                }
                input "accelerationRestrictionEvent", "capability.accelerationSensor", title: "Restrict By Acceleration Sensor", required:false, multiple:true, submitOnChange:true
                if(accelerationRestrictionEvent) {
                    input "arInactiveActive", "bool", title: "Restrict when Inactive (off) or Active (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Acceleration", submitOnChange:true
                    if(arInactiveActive) {
                        paragraph "Restrict when Sensor(s) becomes Active"
                    } else {
                        paragraph "Restrict when Sensor(s) becomes Inactive"
                    }
                    input "accelerationRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(accelerationRANDOR) {
                        paragraph "Restrict when <b>any</b> Acceleration Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Acceleration Sensors are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Acceleration Sensor: ${accelerationRestrictionEvent} - InactiveActive: ${arInactiveActive}, ANDOR: ${accelerationRANDOR}<br>"
                } else {
                    app.removeSetting("accelerationRestrictionEvent")
                    app.removeSetting("arInactiveActive")
                    app.removeSetting("accelerationRANDOR")
                }
                input "accelerationConditionOnly", "bool", title: "Use Acceleration as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(accelerationConditionOnly) {
                    theCogTriggers += " - Condition Only: ${accelerationConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("accelerationEvent")
                app.removeSetting("asInactiveActive")
                app.removeSetting("accelerationANDOR")
                app.removeSetting("accelerationRestrictionEvent")
                app.removeSetting("arInactiveActive")
                app.removeSetting("accelerationRANDOR")
                app.removeSetting("accelerationConditionOnly")
            }
// -----------
            if(triggerType.contains("xBattery")) {
                paragraph "<b>Battery</b>"
                input "batteryEvent", "capability.battery", title: "By Battery Setpoints", required:false, multiple:true, submitOnChange:true
                if(batteryEvent) {
                    input "setBEPointHigh", "bool", title: "Condition true when Battery is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Battery High", submitOnChange:true
                    if(setBEPointHigh) {
                        input "beSetPointHigh", "decimal", title: "Battery High Setpoint", required:true, submitOnChange:true
                    }
                    input "setBEPointLow", "bool", title: "Condition true when Battery is too Low <small><abbr title='Cog will run when reading is less than Setpoint.'><b>- INFO -</b></abbr></small>", description: "Battery Low", submitOnChange:true
                    if(setBEPointLow) {
                        input "beSetPointLow", "decimal", title: "Battery Low Setpoint", required:true, submitOnChange:true
                    }
                    input "setBEPointBetween", "bool", title: "Condition true when Battery is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Battery Between", submitOnChange:true
                    if(setBEPointBetween) {
                        input "beSetPointLow", "decimal", title: "Battery Low Setpoint", required:true, submitOnChange:true, width:6
                        input "beSetPointHigh", "decimal", title: "Battery High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    if(setBEPointHigh) paragraph "Cog Will trigger when Battery reading is above or equal to ${beSetPointHigh}"
                    if(setBEPointLow) paragraph "Cog will trigger when Battery reading is below ${beSetPointLow}"
                    if(setTEPointBetween) paragraph "Cog will trigger when Battery reading is between ${beSetPointLow} and ${beSetPointHigh}"
                }
                input "batteryConditionOnly", "bool", title: "Use Acceleration as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Battery Setpoints: ${batteryEvent} - setpoint Low: ${beSetPointLow}, setpoint High: ${beSetPointHigh}, inBetween: ${setBEPointBetween}}<br>"
                if(batteryConditionOnly) {
                    theCogTriggers += " - Condition Only: ${batteryConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("batteryEvent")
                app.removeSetting("beSetPointHigh")
                app.removeSetting("beSetPointLow")
                app.removeSetting("setBEPointHigh")
                app.removeSetting("setBEPointLow")
            }
// -----------
            if(triggerType.contains("xButton")) {
                paragraph "<b>Button</b>"
                input "buttonEvent", "capability.pushableButton", title: "By Button <small><abbr title='If choosing mulitple buttons, be sure each button has the attribute that you want to use.'><b>- INFO -</b></abbr></small>", required:false, multiple:true, submitOnChange:true
                if(buttonEvent) {
                    attr = []
                    buttonEvent.each { button ->
                        if(button.hasAttribute("doubleTapped")) attr << "doubleTapped"
                        if(button.hasAttribute("held")) attr << "held"
                        if(button.hasAttribute("pushed")) attr << "pushed"
                        if(button.hasAttribute("released")) attr << "released"
                        if(button.hasAttribute("taps")) attr << "taps"   
                    }
                    input "buttonNumber", "text", title: "Button Number", required:true, submitOnChange:true
                    input "buttonAction", "enum", title: "When Button is:", required:true, multiple:false, options: attr, submitOnChange:true
                    if(buttonAction == "taps") {
                        input "buttonTaps", "number", title: "Number of Button Taps (2-4)", range: '2..4', required:true, submitOnChange:true
                    } else {
                        buttonTaps = "NA"
                    }
                    theCogTriggers += "<b>-</b> By Button: ${buttonEvent} - Button Number: ${buttonNumber}, Button Action: ${buttonAction}}, Button Taps: ${buttonTaps}<br>"
                } else {
                    app.removeSetting("buttonNumber")
                    app.removeSetting("buttonAction")
                    app.removeSetting("buttonTaps")
                }
                paragraph "<hr>"
            }
// -----------
            if(triggerType.contains("xContact")) {
                paragraph "<b>Contact</b>"
                input "contactEvent", "capability.contactSensor", title: "By Contact Sensor", required:false, multiple:true, submitOnChange:true
                if(contactEvent) {
                    input "csClosedOpen", "bool", title: "Condition true when Closed (off) or Opened (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Contact", submitOnChange:true
                    if(csClosedOpen) {
                        paragraph "Condition true when Sensor(s) become Open"
                    } else {
                        paragraph "Condition true when Sensor(s) become Closed"
                    }
                    input "contactANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(contactANDOR) {
                        paragraph "Condition true when <b>any</b> Contact Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Contact Sensors are true"
                    }
                    theCogTriggers += "<b>-</b> By Contact Sensor: ${contactEvent} - ClosedOpen: ${csClosedOpen}, ANDOR: ${contactANDOR}<br>"
                } else {
                    app.removeSetting("contactEvent")
                    app.removeSetting("csClosedOpen")
                    app.removeSetting("contactANDOR")
                }
                
                if(contactEvent) {
                    theList = []
                    contactEvent.each { ids ->
                        theId = ids.id
                        theList << theId
                    }
                    theData = "${theList};${csClosedOpen};${contactANDOR}"
                    state.conditionsMap.put("contactEvent",theData)
                } else {
                    state.conditionsMap.remove("contactEvent")
                }

                input "contactRestrictionEvent", "capability.contactSensor", title: "Restrict By Contact Sensor", required:false, multiple:true, submitOnChange:true
                if(contactRestrictionEvent) {
                    input "crClosedOpen", "bool", title: "Restrict when Closed (off) or Opened (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Contact", submitOnChange:true
                    if(crClosedOpen) {
                        paragraph "Restrict when Sensor(s) become Open"
                    } else {
                        paragraph "Restrict when Sensor(s) become Closed"
                    }
                    input "contactRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(contactRANDOR) {
                        paragraph "Restrict when <b>any</b> Contact Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Contact Sensors are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Contact Sensor: ${contactRestrictionEvent} - ClosedOpen: ${crClosedOpen}, ANDOR: ${contactRANDOR}<br>"
                } else {
                    app.removeSetting("contactRestrictionEvent")
                    app.removeSetting("crClosedOpen")
                    app.removeSetting("contactRANDOR")
                }
                input "contactConditionOnly", "bool", title: "Use Contact as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(contactConditionOnly) {
                    theCogTriggers += " - Condition Only: ${contactConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("contactEvent")
                app.removeSetting("csClosedOpen")
                app.removeSetting("contactANDOR")
                app.removeSetting("contactRestrictionEvent")
                app.removeSetting("crClosedOpen")
                app.removeSetting("contactRANDOR")
                app.removeSetting("contactConditionOnly")
            }
// -----------
            if(triggerType.contains("xDirectional")) {

                paragraph "<b>Directional Condition</b> <small><abbr title='Get notified on the direction something is moving in. Great for a Driveway Alert with direction.'><b>- INFO -</b></abbr></small>"
                paragraph "If device 1 triggers before device 2 - Direction is considered <b>Right</b><br>If device 2 triggers before device 1 - Direction is considered <b>Left</b><br><small>Note: If the wrong direction is reported, simply reverse the two inputs.</small>"
                input "theType1", "bool", title: "Device 1: Use Motion Sensor (off) or Contact Sensor (on)", submitOnChange:true
                if(theType1) {
                    input "device1", "capability.contactSensor", title: "Contact Sensor 1", mulitple:false, required:true, submitOnChange:true
                } else {
                    input "device1", "capability.motionSensor", title: "Motion Sensor 1", mulitple:false, required:true, submitOnChange:true
                }
                input "theType2", "bool", title: "Device 2: Use Motion Sensor (off) or Contact Sensor (on)", submitOnChange:true
                if(theType2) {
                    input "device2", "capability.contactSensor", title: "Contact Sensor 2", mulitple:false, required:true, submitOnChange:true
                } else {
                    input "device2", "capability.motionSensor", title: "Motion Sensor 2", mulitple:false, required:true, submitOnChange:true
                }
                input "theDirection", "enum", title: "Which direction to use as the condition", multiple:false, options: ["Left", "Right"], submitOnChange:true
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By Directional Condition: motion/contact 1: ${theType1} - device1: ${device1}, motion/contact 2: ${theType2} device2: ${device2}, theDirection: ${theDirection}<br>"
            } else {
                app.removeSetting("theType1")
                app.removeSetting("device1")
                app.removeSetting("device2")
            }
// -----------
            if(triggerType.contains("xEnergy")) {
                paragraph "<b>Energy</b>"
                input "energyEvent", "capability.powerMeter", title: "By Energy Setpoints", required:false, multiple:true, submitOnChange:true
                if(energyEvent) {
                    input "setEEPointHigh", "bool", defaultValue: "false", title: "Condition true when Energy is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Energy High", submitOnChange:true
                    if(setEEPointHigh) {
                        input "eeSetPointHigh", "decimal", title: "Energy High Setpoint", required:true, submitOnChange:true
                    }
                    input "setEEPointLow", "bool", title: "Condition true when Energy is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Energy Low", submitOnChange:true
                    if(setEEPointLow) {
                        input "eeSetPointLow", "decimal", title: "Energy Low Setpoint", required:true, submitOnChange:true
                    }
                    input "eetBEPointBetween", "bool", title: "Condition true when Energy is Between two Setpoints <small><abbr title='Cog will run when reading is Between two Setpoints.'><b>- INFO -</b></abbr></small>", description: "Energy Between", submitOnChange:true
                    if(setBEPointBetween) {
                        input "eeSetPointLow", "decimal", title: "Energy Low Setpoint", required:true, submitOnChange:true, width:6
                        input "eeSetPointHigh", "decimal", title: "Energy High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    if(setEEPointHigh) paragraph "Cog will trigger when Energy reading is above or equal to ${eeSetPointHigh}"
                    if(setEEPointLow) paragraph "Cog will trigger when Energy reading is below ${eeSetPointLow}"
                    if(setTEPointBetween) paragraph "Cog will trigger when Energy reading is between ${eeSetPointLow} and ${eeSetPointHigh}"
                }
                paragraph "<hr>"
                input "energyConditionOnly", "bool", title: "Use Energy as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Energy Setpoints: ${energyEvent} - setpoint Low: ${eeSetPointLow}, setpoint High: ${eeSetPointHigh}, inBetween: ${setTEPointBetween}<br>"
                if(energyConditionOnly) {
                    theCogTriggers += " - Condition Only: ${energyConditionOnly}<br>"
                }
            } else {
                app.removeSetting("energyEvent")
                app.removeSetting("eeSetPointHigh")
                app.removeSetting("eeSetPointLow")
                app.removeSetting("setEEPointHigh")
                app.removeSetting("setEEPointLow")
                app.removeSetting("energyconditionOnly")
            }
// -----------
            if(triggerType.contains("xEventLogWatchdog")) {
                paragraph "<b>Event/Log Watchdog</b> <small><abbr title='Check if anything special is going on in the event or Log system.'><b>- INFO -</b></abbr></small>"
                createDeviceSection("Event Watchdog for EE Driver")
                input "eventLog", "bool", title: "Watch for Events (off) or the Log (on)", submitOnChange:true
                paragraph "<b>Note: Right now the Log watchdog doesn't work. It tends to run wild!</b>"
                paragraph "<b>Primary Check</b> - Select Keyword or Phrase to Watch<br> - seperate multiple keywords with a semi-colon (;)<br> - If multiple keywords are used, they are considered 'or'"
                input "ewKeyword1", "text", title: "Primary Keyword",  required:false, submitOnChange:true                 
                paragraph "<b>AND</b>"   
                paragraph "<b>Secondary Check</b>"
                input "ewKeyword2", "text", title: "Secondary Keyword - seperate multiple keywords with a semi-colon (;)",  required:false, submitOnChange:true
                paragraph "<b>BUT DOES NOT CONTAIN</b>"   
                input "ewKeyword3", "text", title: "Third Keyword - seperate multiple keywords with a semi-colon (;)",  required:false, submitOnChange:true
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By Event Watchdog: ${ewKeyword1} - AND - ${ewKeyword2}, BUT NOT - ${ewKeyword3}<br>"
            } else {
                app.removeSetting("ewKeyword1")
                app.removeSetting("ewKeyword2")
                app.removeSetting("ewKeyword3")
            }
// -----------
            if(triggerType.contains("xGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorEvent", "capability.garageDoorControl", title: "By Garage Door", required:false, multiple:true, submitOnChange:true
                if(garageDoorEvent) {
                    input "gdClosedOpen", "bool", title: "Condition true when Closed (off) or Open (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Garage Door", submitOnChange:true
                    if(gdClosedOpen) {
                        paragraph "Condition true when Sensor(s) become Open"
                    } else {
                        paragraph "Condition true when Sensor(s) become Closed"
                    }
                    input "garageDoorANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Condition true when <b>any</b> Garage Door is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Garage Doors are true"
                    }
                    theCogTriggers += "<b>-</b> By Garage Door: ${garageDoorEvent} - ClosedOpen: ${gdClosedOpen}, ANDOR: ${garageDoorANDOR}<br>"
                } else {
                    app.removeSetting("garageDoorEvent")
                    app.removeSetting("gdClosedOpen")
                    app.removeSetting("garageDoorANDOR")
                }

                input "garageDoorRestrictionEvent", "capability.garageDoorControl", title: "Restrict By Garage Door", required:false, multiple:true, submitOnChange:true
                if(garageDoorRestrictionEvent) {
                    input "gdrClosedOpen", "bool", title: "Restrict when Closed (off) or Open (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Garage Door", submitOnChange:true
                    if(gdrClosedOpen) {
                        paragraph "Restrict when Sensor(s) become Open"
                    } else {
                        paragraph "Restrict when Sensor(s) become Closed"
                    }
                    input "garageDoorRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Restrict when <b>any</b> Garage Door is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Garage Doors are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Garage Door: ${garageDoorRestrictionEvent} - ClosedOpen: ${gdrClosedOpen}, ANDOR: ${garageDoorANDOR}<br>"
                } else {
                    app.removeSetting("garageDoorRestrictionEvent")
                    app.removeSetting("gdsClosedOpen")
                    app.removeSetting("garageDoorRANDOR")
                }
                input "garageDoorConditionOnly", "bool", title: "Use Garage Door as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(garageDoorConditionOnly) {
                    theCogTriggers += " - Condition Only: ${garageDoorConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("garageDoorEvent")
                app.removeSetting("gdClosedOpen")
                app.removeSetting("garageDoorANDOR")
                app.removeSetting("garageDoorRestrictionEvent")
                app.removeSetting("gdsClosedOpen")
                app.removeSetting("garageDoorRANDOR")
                app.removeSetting("garageDoorConditionOnly")
            }
// -----------
            if(triggerType.contains("xGVar")) {
                paragraph "<b>EE Global Variables</b>"
                paragraph "<small>Be sure to setup an EE Global Variable in the parent app before trying to use this option.</small>"
                if(state.gvMap) {
                    theList = "${state.gvMap.keySet()}".replace("[","").replace("]","").replace(", ", ",")
                    theList2 = theList.split(",")              
                    input "globalVariableEvent", "enum", title: "By EE Global Variable", options: theList2, submitOnChange:true
                    input "gvStyle", "bool", title: "Use as Text (off) or Number (on)", submitOnChange:true
                    if(gvStyle) {
                        if(globalVariableEvent) {
                            input "setGVPointHigh", "bool", title: "Condition true when Variable is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Variable High", submitOnChange:true
                            if(setGVPointHigh) {
                                input "gvSetPointHigh", "decimal", title: "Variable High Setpoint", required:true, submitOnChange:true
                            }
                            input "setGVPointLow", "bool", title: "Condition true when Variable is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Variable Low", submitOnChange:true
                            if(setGVPointLow) {
                                input "gvSetPointLow", "decimal", title: "Variable Low Setpoint", required:true, submitOnChange:true
                            }
                            input "setGVPointBetween", "bool", title: "Condition true when Variable is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Varible Between", submitOnChange:true
                            if(setGVPointBetween) {
                                input "gvSetPointLow", "decimal", title: "Variable Low Setpoint", required:true, submitOnChange:true, width:6
                                input "gvSetPointHigh", "decimal", title: "Variable High Setpoint", required:true, submitOnChange:true, width:6
                            }
                            if(setGVPointHigh) paragraph "Cog will trigger when Variable reading is above or equal to ${gvSetPointHigh}"
                            if(setGVPointLow) paragraph "Cog will trigger when Variable reading is below ${gvSetPointLow}"
                            if(setGVPointBetween) paragraph "Cog will trigger when Variable reading is between ${gvSetPointLow} and ${gvSetPointHigh}"
                            app.removeSetting("gvValue")
                            theCogTriggers += "<b>-</b> By EE Global Variable Setpoints: ${globalVariableEvent} - setpoint Low: ${gvSetPointLow}, setpoint High: ${gvSetPointHigh}, inBetween: ${setGVPointBetween}<br>"
                        }
                    } else {
                        input "gvValue", "text", title: "Value", required:false, submitOnChange:true
                        app.removeSetting("gvSetPointHigh")
                        app.removeSetting("gvSetPointLow")
                        app.removeSetting("setGVPointHigh")
                        app.removeSetting("setGVPointLow")
                        theCogTriggers += "<b>-</b> By EE Global Variable: ${globalVariableEvent} - Value: ${gvValue}<br>"
                    }
                } else {
                    paragraph "<b>In order to use the EE Global Variables, please be sure to do the following</b><br>- Setup at least one EE Global Variable in the parent app.<br>- This Cog needs to be saved first. Please scroll down and hit 'Done' before continuing. Then open the Cog again.</b>"
                }
                paragraph "<hr>"               
            } else {
                app.removeSetting("globalVariableEvent")
                app.removeSetting("gvValue")
                app.removeSetting("gvSetPointHigh")
                app.removeSetting("gvSetPointLow")
                app.removeSetting("setGVPointHigh")
                app.removeSetting("setGVPointLow")
            }
// -----------
            if(triggerType.contains("xHSMAlert")) {
                paragraph "<b>HSM Alert</b>"
                paragraph "<b>Warning: This Condition has not been tested. Use at your own risk.</b>"
                input "hsmAlertEvent", "enum", title: "By HSM Alert", options: ["arming", "armingHome", "armingNight", "cancel", "cancelRuleAlerts", "intrusion", "intrusion-delay", "intrusion-home", "intrusion-home-delay", "intrusion-night", "intrusion-night-delay", "rule", "smoke", "water"], multiple:true, submitOnChange:true
                if(hsmAlertEvent) paragraph "Cog will trigger when <b>any</b> of the HSM Alerts are active."
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By HSM Alert: ${hsmAlertEvent}<br>"
            } else {
                app.removeSetting("hsmAlertEvent")
            }
// -----------
            if(triggerType.contains("xHSMStatus")) {
                paragraph "<b>HSM Status</b>"
                paragraph "<b>Warning: This Condition has not been tested. Use at your own risk.</b>"
                input "hsmStatusEvent", "enum", title: "By HSM Status", options: [
                    ["allDisarmed":"All Disarmed"],
                    ["armedAway":"Armed Away"],
                    ["armedHome":"Armed Home"],
                    ["armedNight":"Armed Night"],
                    ["delayedArmed":"Delayed Armed Away"],
                    ["delayedArmedHome":"Delayed Armed Home"],
                    ["delayedArmedNight":"Delayed Armed Night"],
                    ["disarmed":"Disarmed"]
                ], multiple:true, submitOnChange:true, width:6
                if(hsmStatusEvent) paragraph "Cog will trigger when <b>any</b> of the HSM Status are active."
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By HSM Status: ${hsmStatusEvent}<br>"
            } else {
                app.removeSetting("hsmStatusEvent")
            }
// -----------            
            if(triggerType.contains("xHubCheck")) {
                paragraph "<b>Hub Check Options</b><br>This can be used to check any hub on your network."
                input "xhttpIP", "string", title: "Enter the IP Address of the Hub (ie. http://192.168.86.81)", defaultValue: "http://", submitOnChange:true
                input "xhttpCommand", "enum", title: "Choose Command", options: [
                    ["/hub/advanced/freeOSMemory":"Check Free OS Memory"]
                ], submitOnChange:true
                paragraph "<b>Does not work with Hub Security Enabled. Work in progress</b>"
                // "xhubSecurity", "bool", title: "Hub Security Enabled", submitOnChange:true
                app.removeSetting("xhubSecurity")
                if(xhubSecurity) {
                    input "xhubUsername", "string", title: "Hub Username", required:true, submitOnChange:true
                    input "xhubPassword", "password", title: "Hub Password", required:true, submitOnChange:true
                } else {
                    app.removeSetting("xhubUsername")
                    app.removeSetting("xhubPassword")
                }
                if(xhttpCommand) {
                    if(xhttpCommand.contains("freeOSMemory")) {
                        input "xMinMemory", "number", title: "Minimum amount of Memory Available Set Point", defaultValue:40000, submitOnChange:true
                        input "xfreeOSMemLog", "bool", title: "Show Free OS Memory in Log with each check", submitOnChange:true
                        state.useRollingAverage = true
                    } else {
                        state.useRollingAverage = false
                    }
                }
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> Send HTTP: ${xhttpIP}:8080${xhttpCommand}<br>"
            } else {
                app.removeSetting("xhttpIP")
                app.removeSetting("xhttpCommand")
                app.removeSetting("xhubUsername")
                app.removeSetting("xhubPassword")
            }
// -----------            
            if(triggerType.contains("xHubVariable")) {
                paragraph "<b>Hub Variables</b>"
                HashMap varMap = getAllGlobalVars()
                state.varList = []              
                varMap.each {
                    state.varList.add("$it.key")
                }
                input "theVariable", "enum", title: "Select Variable", options: state.varList.sort(), multiple:false, required:false, submitOnChange:true, width:6
                if(theVariable) {
                    varData = getGlobalVar(theVariable)
                    if(varData.type == "integer" || varData.type == "bigdecimal") {
                        input "varComp", "enum", title: "Comparision", options: ["equals", "does not equal", "less than", "greater than", "less than or equal to", "greater than or equal to", "changed", "increased", "decreased"], submitOnChange:true, width:6
                        if(varComp == "equals" || varComp == "does not equal" || varComp == "less than" || varComp == "greater than" || varComp == "less than or equal to" || varComp == "greater than or equal to") {
                            input "varCompValue", "number", title: "Value (${varData.type})", submitOnChange:true
                        }
                    } else if(varData.type == "string") {
                        input "varComp", "enum", title: "Comparision", options: ["equals", "does not equal", "contains", "changed", "is empty"], submitOnChange:true, width:6
                        if(varComp == "equals" || varComp == "does not equal" || varComp == "contains") {
                            input "varCompValue", "text", title: "Value (${varData.type})", submitOnChange:true
                        }
                    } else if(varData.type == "boolean") {
                        input "varComp", "enum", title: "Boolean", options: ["true", "false", "changed"], submitOnChange:true, width:6
                    } else if(varData.type == "datetime") {
                        paragraph "Work in progress"
                    }
                    paragraph "Current Value: ${varData.value}"
                    if(logEnable) {
                        paragraph "<hr>"
                        paragraph "varData: ${varData}"
                    }
                } else {
                    app.removeSetting("varText")
                }
                theCogTriggers += "<b>-</b> By Hub Variable: ${theVariable} - Comp: ${varComp}, Comp Value: ${varCompValue}<br>"                
            } else {
                app.removeSetting("theVariable")
                app.removeSetting("varComp")
                app.removeSetting("varCompValue")
            }
// -----------
            if(triggerType.contains("xHumidity")) {
                paragraph "<b>Humidity</b>"
                input "humidityEvent", "capability.relativeHumidityMeasurement", title: "By Humidity Setpoints", required:false, multiple:true, submitOnChange:true
                if(humidityEvent) {
                    input "setHEPointHigh", "bool", title: "Condition true when Humidity is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Humidity High", submitOnChange:true
                    if(setHEPointHigh) {
                        input "heSetPointHigh", "number", title: "Humidity High Setpoint", required:true, submitOnChange:true
                    }
                    input "setHEPointLow", "bool", title: "Condition true when Humidity is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Humidity Low", submitOnChange:true
                    if(setHEPointLow) {
                        input "heSetPointLow", "number", title: "Humidity Low Setpoint", required:true, submitOnChange:true
                    }
                    input "setHEPointBetween", "bool", title: "Condition true when Humidity is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Humidity Between", submitOnChange:true
                    if(setHEPointBetween) {
                        input "heSetPointLow", "number", title: "Humidity Low Setpoint", required:true, submitOnChange:true, width:6
                        input "heSetPointHigh", "number", title: "Humidity High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    input "humidityConditionOnly", "bool", title: "Use Humidity as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                    if(humidityConditionOnly) {
                        if(setHEPointHigh) paragraph "Cog will use 'as condition' when Humidity reading is above or equal to ${heSetPointHigh}"
                        if(setHEPointLow) paragraph "Cog will use 'as condition' when Humidity reading is below ${heSetPointLow}"
                        if(setHEPointBetween) paragraph "Cog will use 'as condition' when Humidity reading is between ${heSetPointLow} and ${heSetPointHigh}"
                    } else {
                        if(setHEPointHigh) paragraph "Cog will trigger when Humidity reading is above or equal to ${heSetPointHigh}"
                        if(setHEPointLow) paragraph "Cog will trigger when Humidity reading is below ${heSetPointLow}"
                        if(setHEPointBetween) paragraph "Cog will trigger when Humidity reading is between ${heSetPointLow} and ${heSetPointHigh}"
                    }
                }
                paragraph "<hr>"
                input "humidityConditionOnly", "bool", title: "Use Humidity as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Humidity Setpoints: ${humidityEvent} - setpoint Low: ${heSetPointLow}, setpoint High: ${heSetPointHigh}, inBetween: ${setHEPointBetween}<br>"
                if(humidityConditionOnly) {
                    theCogTriggers += " - Condition Only: ${humidityConditionOnly}<br>"
                }
            } else {
                app.removeSetting("humidityEvent")
                app.removeSetting("heSetPointHigh")
                app.removeSetting("heSetPointLow")
                app.removeSetting("setHEPointHigh")
                app.removeSetting("setHEPointLow")
                app.removeSetting("setHEPointBetween")
                app.removeSetting("humidityConditionOnly")
            }
// -----------
            if(triggerType.contains("xIlluminance")) {
                paragraph "<b>Illuminance</b>"
                input "illuminanceEvent", "capability.illuminanceMeasurement", title: "By Illuminance Setpoints", required:false, multiple:true, submitOnChange:true
                if(illuminanceEvent) {
                    input "setIEPointHigh", "bool", title: "Condition true when Illuminance is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "High", submitOnChange:true
                    if(setIEPointHigh) {
                        input "ieSetPointHigh", "decimal", title: "Illuminance High Setpoint", required:true, submitOnChange:true
                    }
                    input "setIEPointLow", "bool", title: "Condition true when Illuminance is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Low", submitOnChange:true
                    if(setIEPointLow) {
                        input "ieSetPointLow", "decimal", title: "Illuminance Low Setpoint", required:true, submitOnChange:true
                    }
                    input "setIEPointBetween", "bool", title: "Condition true when Illuminance is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Illuminance Between", submitOnChange:true
                    if(setIEPointBetween) {
                        input "ieSetPointLow", "decimal", title: "Illuminance Low Setpoint", required:true, submitOnChange:true, width:6
                        input "ieSetPointHigh", "decimal", title: "Illuminance High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    input "illumConditionOnly", "bool", title: "Use Illuminance as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                    if(illumConditionOnly) {
                        if(setIEPointHigh) paragraph "Cog will use 'as condition' when Illuminance reading is above or equal to ${ieSetPointHigh}"
                        if(setIEPointLow) paragraph "Cog will use 'as condition' when Illuminance reading is below ${ieSetPointLow}"
                        if(setIEPointBetween) paragraph "Cog will use 'as condition' when Illuminance reading is between ${ieSetPointLow} and ${ieSetPointHigh}"
                    } else {
                        if(setIEPointHigh) paragraph "Cog will trigger when Illuminance reading is above or equal to ${ieSetPointHigh}"
                        if(setIEPointLow) paragraph "Cog will trigger when Illuminance reading is below ${ieSetPointLow}"
                        if(setIEPointBetween) paragraph "Cog will trigger when Illuminance reading is between ${ieSetPointLow} and ${ieSetPointHigh}"
                    }
                }              
                theCogTriggers += "<b>-</b> By Illuminance Setpoints: ${illuminanceEvent} - trigger/condition: ${illumConditionOnly} - setpoint Low: ${ieSetPointLow}, setpoint High: ${ieSetPointHigh}, inBetween: ${setIEPointBetween}<br>"
                if(illumConditionOnly) {
                    theCogTriggers += " - Condition Only: ${illumConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("illuminanceEvent")
                app.removeSetting("ieSetPointHigh")
                app.removeSetting("ieSetPointLow")
                app.removeSetting("setIEPointHigh")
                app.removeSetting("setIEPointLow")
            }
// -----------
            if(triggerType.contains("xIPPing")) {
                paragraph "<b>IP Ping</b>"
                if(location.hub.firmwareVersionString > "2.2.6.140") {
                    input "ipAddress", "text", title :"URL (ie. 192.168.68.5", required: true
                    input "numPings", "number", title: "Number of Ping attempts per trigger (1 to 5)", required:true, range: '1..5'
                    input "timeToPing", "enum", title: "Time between ping events (minutes)",  options: ["1","5","10","15","30", "60"], required:true, Multiple:false, submitOnChange:true
                    if(ipAddress) {
                        input "ipFound", "bool", title: "Condition true when not found (off) or is found (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Contact", submitOnChange:true
                        if(ipFound) {
                            paragraph "Condition true when IP URL is found"
                        } else {
                            paragraph "Condition true when IP URL is not found"
                        }
                    }
                    paragraph "<small>* Minimum recommended interval is 5 minutes.</small>"
                    if(timeToPing == "1") paragraph "<b>1 minute interval is not recommended and might slow down your hub.</b>"
                    theCogTriggers += "<b>-</b> By IP Ping: ${ipAddress} - num of Pings: ${numPings} - time To Ping: ${timeToPing} - ipFound: ${ipFound}<br>"
                    paragraph "<hr>"
                } else {
                    paragraph "Ping Options are only available for hub model C-7 running version 2.2.6.140 or above."
                }
            } else {
                app.removeSetting("ipAddress")
                app.removeSetting("numPings")
                app.removeSetting("timeToPing")
                app.removeSetting("ipFound")
            }
// -----------            
            if(triggerType.contains("xLock")) {
                paragraph "<b>Lock</b>"
                input "lockEvent", "capability.lock", title: "By Lock", required:false, multiple:true, submitOnChange:true
                if(lockEvent) {
                    input "lUnlockedLocked", "bool", title: "Condition true when Unlocked (off) or Locked (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Lock", submitOnChange:true
                    if(lUnlockedLocked) {
                        paragraph "Condition true when Lock(s) become Locked"
                    } else {
                        paragraph "Condition true when Lock(s) become Unlocked"
                    }
                    input "lockANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(lockANDOR) {
                        paragraph "Condition true when <b>any</b> Lock is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Locks are true"
                    }
                    theNames = getLockCodeNames(lockEvent)
                    input "lockUser", "enum", title: "By Lock User <small><abbr title='Only the selected users will trigger the Cog to run. Leave blank for all users.'><b>- INFO -</b></abbr></small>", options: theNames, required:false, multiple:true, submitOnChange:true
                    paragraph "<small>* Note: If you are using Hub Mesh and have this cog on a different hub than the Lock, the lock codes must not be encrypted.</small>"
                    if(lUnlockedLocked) {
                        input "noCodeLocks", "bool", title: "Include Manual Locks (hand turn, key and/or digital without code)", submitOnChange:true
                    } else {
                        input "noCodeUnlocks", "bool", title: "Include Manual Unlocks (hand turn, key and/or digital without code)", submitOnChange:true        
                    }
                    input "specialMessage", "bool", title: "Look for a 'special' lock message when all other options don't fit<br><small>* This will override all other Lock options</small>", submitOnChange:true
                    if(specialMessage) {
                        input "sLockMessage", "text", title: "When lock message contains (separate multiple messages with a semi-colon ';')", submitOnChange:true
                    } else {
                        app.removeSetting("sLockMessage")
                    }
                    if(lUnlockedLocked) {
                        theCogTriggers += "<b>-</b> By Lock: ${lockEvent} - UnlockedLocked: ${lUnlockedLocked}, lockANDOR: ${lockANDOR}, Lock User: ${lockUser}, noCodeLocks: ${noCodeLocks}<br>"
                    } else {
                        theCogTriggers += "<b>-</b> By Lock: ${lockEvent} - UnlockedLocked: ${lUnlockedLocked}, lockANDOR: ${lockANDOR}, Lock User: ${lockUser}, noCodeUnlocks: ${noCodeUnlocks}<br>"
                    }
                    if(specialMessage) { theCogTriggers += "<b>-</b> By Lock - Special Message: ${sLockMessage}" }
                } else {
                    app.removeSetting("lockUser")
                    app.removeSetting("lockEvent")
                    app.removeSetting("lUnlockedLocked")
                    app.removeSetting("lockANDOR")
                    app.removeSetting("noCodeUnlocks")
                }

                input "lockRestrictionEvent", "capability.lock", title: "Restrict By Lock", required:false, multiple:false, submitOnChange:true
                if(lockRestrictionEvent) {
                    input "lrUnlockedLocked", "bool", title: "Restrict when Unlocked (off) or Locked (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Lock", submitOnChange:true
                    if(lrUnlockedLocked) {
                        paragraph "Restrict when Sensor(s) become Locked"
                    } else {
                        paragraph "Restrict when Sensor(s) become Unlocked"
                    }
                    input "lockRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(lockRANDOR) {
                        paragraph "Restrict when <b>any</b> Lock is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Locks are true"
                    }
                    theNames = getLockCodeNames(lockRestrictionEvent)
                    input "lockRestrictionUser", "enum", title: "Restrict By Lock User <small><abbr title='Only the selected users will trigger the Cog to run. Leave blank for all users.'><b>- INFO -</b></abbr></small>", options: theNames, required:false, multiple:true, submitOnChange:true
                    paragraph "<small>* Note: If you are using Hub Mesh and have this cog on a different hub than the Lock, the lock codes must not be encryted.</small>"
                    theCogTriggers += "<b>Restriction:</b> By Lock: ${lockRestrictionEvent} - UnlockedLocked: ${lrUnlockedLocked}, lock User: ${lockRestrictionUser}<br>"
                } else {
                    app.removeSetting("lockRestrictionUser")
                    app.removeSetting("lockRestrictionEvent")
                    app.removeSetting("lrUnlockedLocked")
                    app.removeSetting("lockRANDOR")
                }
                input "lockConditionOnly", "bool", title: "Use Lock as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(lockConditionOnly) {
                    theCogTriggers += " - Condition Only: ${lockConditionOnly}<br>"
                }
                paragraph "<hr>" 
            } else {
                app.removeSetting("lockUser")
                app.removeSetting("lockEvent")
                app.removeSetting("lUnlockedLocked")
                app.removeSetting("lockANDOR")
                app.removeSetting("lockRestrictionUser")
                app.removeSetting("lockRestrictionEvent")
                app.removeSetting("lrUnlockedLocked")
                app.removeSetting("lockRANDOR")
                app.removeSetting("lockConditionOnly")
            }
// -----------
            if(triggerType.contains("xMotion")) {
                paragraph "<b>Motion</b>"
                input "motionEvent", "capability.motionSensor", title: "By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionEvent) {
                    input "meInactiveActive", "bool", title: "Motion Inactive (off) or Active (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Motion", submitOnChange:true
                    if(meInactiveActive) {
                        paragraph "Condition true when Sensor(s) becomes Active"
                    } else {
                        paragraph "Condition true when Sensor(s) becomes Inactive"
                    }
                    input "motionANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(motionANDOR) {
                        paragraph "Condition true when <b>any</b> Motion Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Motion Sensors are true"
                    }
                    theCogTriggers += "<b>-</b> By Motion Sensor: ${motionEvent} - InactiveActive: ${meInactiveActive}, ANDOR: ${motionANDOR}<br>"
                } else {
                    app.removeSetting("motionEvent")
                    app.removeSetting("meInactiveActive")
                    app.removeSetting("motionANDOR")
                }

                input "motionRestrictionEvent", "capability.motionSensor", title: "Restrict By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionRestrictionEvent) {
                    input "mrInactiveActive", "bool", title: "Motion Inactive (off) or Active (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Motion", submitOnChange:true
                    if(mrInactiveActive) {
                        paragraph "Restrict when Sensor(s) becomes Active"
                    } else {
                        paragraph "Restrict when Sensor(s) becomes Inactive"
                    }
                    input "motionRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(motionRANDOR) {
                        paragraph "Restrict when <b>any</b> Motion Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Motion Sensors are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Motion Sensor: ${motionRestrictionEvent} - InactiveActive: ${mrInactiveActive}, ANDOR: ${motionRANDOR}<br>"
                } else {
                    app.removeSetting("motionRestrictionEvent")
                    app.removeSetting("mrInactiveActive")
                    app.removeSetting("motionRANDOR")
                }
                input "motionConditionOnly", "bool", title: "Use Motion as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(motionConditionOnly) {
                    theCogTriggers += " - Condition Only: ${motionConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("motionEvent")
                app.removeSetting("meInactiveActive")
                app.removeSetting("motionANDOR")
                app.removeSetting("motionRestrictionEvent")
                app.removeSetting("mrInactiveActive")
                app.removeSetting("motionRANDOR")
                app.removeSetting("motionConditionOnly")
            }
// -----------
            if(triggerType.contains("xPower")) {
                paragraph "<b>Power</b>"
                input "powerEvent", "capability.powerMeter", title: "By Power Setpoints", required:false, multiple:true, submitOnChange:true
                if(powerEvent) {
                    input "setPEPointHigh", "bool", defaultValue: "false", title: "Condition true when Power is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Power High", submitOnChange:true
                    if(setPEPointHigh) {
                        input "peSetPointHigh", "decimal", title: "Power High Setpoint", required:true, submitOnChange:true
                    }
                    input "setPEPointLow", "bool", title: "Condition true when Power is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Power Low", submitOnChange:true
                    if(setPEPointLow) {
                        input "peSetPointLow", "decimal", title: "Power Low Setpoint", required:true, submitOnChange:true
                    }
                    input "setPEPointBetween", "bool", title: "Condition true when Power is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Power Between", submitOnChange:true
                    if(setPEPointBetween) {
                        input "peSetPointLow", "decimal", title: "Power Low Setpoint", required:true, submitOnChange:true, width:6
                        input "peSetPointHigh", "decimal", title: "Power High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    if(setPEPointHigh) paragraph "Cog will trigger when Power reading is above or equal to ${peSetPointHigh}"
                    if(setPEPointLow) paragraph "Cog will trigger when Power reading is below ${peSetPointLow}"
                    if(setPEPointBetween) paragraph "Cog will trigger when Power reading is between ${peSetPointLow} and ${peSetPointHigh}"
                }
                input "powerConditionOnly", "bool", title: "Use Power as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Power Setpoints: ${powerEvent} - setpoint Low: ${peSetPointLow}, setpoint High: ${peSetPointHigh}, inBetween: ${setPEPointBetween}<br>"
                if(powerConditionOnly) {
                    theCogTriggers += " - Condition Only: ${powerConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("powerEvent")
                app.removeSetting("peSetPointHigh")
                app.removeSetting("peSetPointLow")
                app.removeSetting("setPEPointHigh")
                app.removeSetting("setPEPointLow")
            }
// -----------
            if(triggerType.contains("xPresence")) {
                paragraph "<b>Presence</b>"
                input "presenceEvent", "capability.presenceSensor", title: "By Presence Sensor", required:false, multiple:true, submitOnChange:true
                if(presenceEvent) {
                    input "psPresentNotPresent", "bool", title: "Condition true when Present (off) or Not Present (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Present", submitOnChange:true
                    if(psPresentNotPresent) {
                        paragraph "Condition true when Sensor(s) become Not Present"
                    } else {
                        paragraph "Condition true when Sensor(s) become Present"
                    }

                    input "presenceANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(presenceANDOR) {
                        paragraph "Condition true when <b>any</b> Presence Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Presence Sensors are true"
                    }
                    theCogTriggers += "<b>-</b> By Presence Sensor: ${presenceEvent} - PresentNotPresent: ${psPresentNotPresent}, ANDOR: ${presenceANDOR}<br>"
                } else {
                    app.removeSetting("presenceEvent")
                    app.removeSetting("psPresentNotPresent")
                    app.removeSetting("presenceANDOR")
                }

                input "presenceRestrictionEvent", "capability.presenceSensor", title: "Restrict By Presence Sensor", required:false, multiple:true, submitOnChange:true
                if(presenceRestrictionEvent) {
                    input "prPresentNotPresent", "bool", title: "Restrict when Present (off) or Not Present (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Present", submitOnChange:true
                    if(prPresentNotPresent) {
                        paragraph "Restrict when Sensor(s) become Not Present"
                    } else {
                        paragraph "Restrict when Sensor(s) become Present"
                    }

                    input "presentRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(presentRANDOR) {
                        paragraph "Restrict when <b>any</b> Presence Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Presence Sensors are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Presence Sensor: ${presenceRestrictionEvent} - PresentNotPresent: ${prPresentNotPresent}, ANDOR: ${presentRANDOR}<br>"
                } else {
                    app.removeSetting("presenceRestrictionEvent")
                    app.removeSetting("prPresentNotPresent")
                    app.removeSetting("presentRANDOR")
                }
                input "presenceConditionOnly", "bool", title: "Use Presence as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(presenceConditionOnly) {
                    theCogTriggers += " - Condition Only: ${presenceConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("presenceEvent")
                app.removeSetting("psPresentNotPresent")
                app.removeSetting("presenceANDOR")
                app.removeSetting("presenceRestrictionEvent")
                app.removeSetting("prPresentNotPresent")
                app.removeSetting("presentRANDOR")
                app.removeSetting("presenceConditionOnly")
            }
// -----------
            if(triggerType.contains("xSecurityKeypad")) {
                paragraph "<b>Security Keypad</b><br>For use with the Ring Alarm Gen 2 Keypad using the Ring Alarm Keypad G2 Community Driver.<br><small>This will NOT work with the built in Ring G2 driver.</small>"
                input "keypadEvent", "capability.securityKeypad", title: "By Security Keypad", required:false, multiple:true, submitOnChange:true
                paragraph "<small>* Note: If you are using Hub Mesh and have this cog on a different hub than the Keyapd, the codes must not be encrypted.</small>"
                if(keypadEvent) {
                    input "keypadEventType", "enum", title: "Type of Keypad Event", options: ["Alarm User/Status", "Emergency Buttons", "Alt Codes"], required:true, Multiple:false, submitOnChange:true                
                    if(keypadEventType == "Alarm User/Status") {
                        app.removeSetting("keypadPanic")
                        app.removeSetting("keypadAltCode")
                        theNames = getLockCodeNames(keypadEvent)
                        input "keypadUser", "enum", title: "By Keypad User <small><abbr title='Only the selected users will trigger the Cog to run.'><b>- INFO -</b></abbr></small>", options: theNames, required:false, multiple:true, submitOnChange:true
                        input "keypadStatus", "enum", title: "By Keypad Status <small><abbr title='Only the selected status will trigger the Cog to run.'><b>- INFO -</b></abbr></small>", options: ["armed", "disarmed"], required:true, multiple:true, submitOnChange:true
                    } else if(keypadEventType == "Emergency Buttons") {
                        app.removeSetting("keypadUser")
                        app.removeSetting("keypadStatus")
                        app.removeSetting("keypadAltCode")
                        input "keypadPanic", "enum", title: "By Keypad Emergency Button <small><abbr title='Cog will run when this Emergency Button has been pressed.'><b>- INFO -</b></abbr></small>", options: ["police", "fire", "medical"], required:true, multiple:true, submitOnChange:true                       
                    } else if(keypadEventType == "Alt Codes") {
                        app.removeSetting("keypadUser")
                        app.removeSetting("keypadStatus")
                        app.removeSetting("keypadPanic")
                        paragraph "Enter in any code to trigger the Cog. This code will need to be entered on the keypad and then press the 'check mark' button. IMPORTANT: This code can not match any of the alarm codes."
                        paragraph "Again: The codes entered here, CAN NOT be saved in the alarm codes of the keypad driver."
                        input "keypadAltCode", "text", title: "Alt Code", required:true, submitOnChange:true, width:6
                    }
                    if(keypadEventType == "Alarm User/Status") {
                        theCogTriggers += "<b>-</b> By Security Keypad: ${keypadEvent} - User: ${keypadUser} - Status: ${keypadStatus}<br>"
                    } else if(keypadEventType == "Emergency Buttons") {
                        theCogTriggers += "<b>-</b> By Security Keypad: ${keypadEvent} - Emergency Button: ${keypadPanic}<br>"
                    } else if(keypadEventType == "Alt Codes") {
                        theCogTriggers += "<b>-</b> By Security Keypad: ${keypadEvent} - Alt Code: ${keypadAltCode}<br>"
                    }
                    paragraph "<hr>"
                }
            } else {
                app.removeSetting("keypadEvent")
                app.removeSetting("keypadUser")
                app.removeSetting("keypadStatus")
                app.removeSetting("keypadPanic")
                app.removeSetting("keypadAltCode")
            }
// -----------
            if(triggerType.contains("xSNStatus")) {
                paragraph "<b>Safety Net Status</b>"
                paragraph "<b>Safety Net is not available... yet.</b>"
                input "snDeviceEvent", "capability.actuator", title: "Safety Net Device", required:false, submitOnChange:true
                input "snStatus", "enum", title: "By SN Status", options: [
                    ["disarmed":"Disarmed"],
                    ["armed away":"Armed Away"],
                    ["armed home":"Armed Home"],
                    ["armed night":"Armed Night"],
                    ["police alert":"Police Alert"],
                    ["fire alert":"Fire Alert"],
                    ["medical alert":"Medical Alert"],
                    ["alarm alert":"Alarm Alert"]
                ], multiple:true, submitOnChange:true, width:6
                paragraph "Cog will trigger when <b>any</b> of the selected status match the actual Safety Net status."
                input "snRestriction", "bool", defaultValue:true, title: "By Safety Net as Restriction <small><abbr title='When used as a Restriction, if condidtion is not met nothing will happen based on this condition.'><b>- INFO -</b></abbr></small>", description: "By Safety Net Restriction", submitOnChange:true
                paragraph "<hr>"
                theCogTriggers += "<b>-</b> By Safety Net: ${snDeviceEvent} - Status: ${snStatus} - restriction: ${snRestriction}<br>"
            } else {
                app.removeSetting("snDeviceEvent")
                app.removeSetting("snStatus")
                app.removeSetting("snRestriction")
            }
// -----------
            if(triggerType.contains("xSwitch")) {
                paragraph "<b>Switch</b>"
                input "switchEvent", "capability.switch", title: "By Switch", required:false, multiple:true, submitOnChange:true
                if(switchEvent) {
                    input "seOffOn", "bool", title: "Switch Off (off) or On (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Switch", submitOnChange:true
                    input "seType", "bool", title: "Only when Physically pushed <small><abbr title='Choose this to distinguish between a physical push vs Hubitat turning it on.'><b>- INFO -</b></abbr></small>", description: "Switch Type", submitOnChange:true
                    if(seOffOn) {
                        paragraph "Condition true when Sensor(s) becomes On"
                    } else {
                        paragraph "Condition true when Sensor(s) becomes Off"
                    }
                    if(seType) { paragraph "<small>* Event 'Description Text' must contain '[physical]' for this to work. HE stock drivers do, others may vary.</small>" }
                    input "switchANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(switchANDOR) {
                        paragraph "Condition true when <b>any</b> Switch is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Switches are true"
                    }
                    theCogTriggers += "<b>-</b> By Switch: ${switchEvent} - OffOn: ${seOffOn}, ANDOR: ${switchANDOR}, Physical: ${seType}<br>"
                } else {
                    app.removeSetting("switchEvent")
                    app.removeSetting("seOffOn")
                    app.removeSetting("switchANDOR")
                    app.removeSetting("seStateMin")
                    app.removeSetting("seInState")
                }

                input "switchRestrictionEvent", "capability.switch", title: "Restrict by Switch", required:false, multiple:true, submitOnChange:true
                if(switchRestrictionEvent) {
                    input "srOffOn", "bool", title: "Switch Off (off) or On (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Switch", submitOnChange:true
                    if(srOffOn) {
                        paragraph "Restrict when Switch(es) are On"
                    } else {
                        paragraph "Restrict when Switch(es) are Off"
                    }
                    input "switchRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(switchRANDOR) {
                        paragraph "Restrict when <b>any</b> Switch is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Switches are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Switch: ${switchRestrictionEvent} - OffOn: ${srOffOn}, ANDOR: ${switchRANDOR}<br>"
                } else {
                    app.removeSetting("switchRestrictionEvent")
                    app.removeSetting("srOffOn")
                    app.removeSetting("switchRANDOR")
                }
                input "switchConditionOnly", "bool", title: "Use Switch as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(switchConditionOnly) {
                    theCogTriggers += " - Condition Only: ${switchConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchEvent")
                app.removeSetting("seOffOn")
                app.removeSetting("switchANDOR")
                app.removeSetting("seStateMin")
                app.removeSetting("seInState")
                app.removeSetting("switchRestrictionEvent")
                app.removeSetting("srOffOn")
                app.removeSetting("switchRANDOR")
                app.removeSetting("switchConditionOnly")
            }
// -----------
            if(triggerType.contains("xSystemStartup")) {
                paragraph "<b>System Startup</b>"
                input "startupEvent", "bool", title: "Run Cog when system first starts up", description: "System Startup", submitOnChange:true
                if(startupEvent) {
                    paragraph "<b>Cog is set to run when system is starting up</b>"
                    theCogTriggers += "<b>-</b> At System Startup: ${startupEvent}<br>"
                } else {
                    paragraph ""
                }
                paragraph "<hr>"
            }
// ----------- setpointHandler - for search
            if(triggerType.contains("xTemp")) {
                paragraph "<b>Temperature</b>"
                input "tempEvent", "capability.temperatureMeasurement", title: "By Temperature Setpoints", required:false, multiple:true, submitOnChange:true
                if(tempEvent) {
                    input "setTEPointHigh", "bool", title: "Condition true when Temperature is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Temp High", submitOnChange:true
                    if(setTEPointHigh) {
                        input "teSetPointHigh", "decimal", title: "Temperature High Setpoint", required:true, submitOnChange:true
                    }
                    input "setTEPointLow", "bool", title: "Condition true when Temperature is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Temp Low", submitOnChange:true
                    if(setTEPointLow) {
                        input "teSetPointLow", "decimal", title: "Temperature Low Setpoint", required:true, submitOnChange:true
                    }
                    input "setTEPointBetween", "bool", title: "Condition true when Temperature is Between two Setpoints <small><abbr title='Cog will run when reading is between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Temp Between", submitOnChange:true
                    if(setTEPointBetween) {
                        input "teSetPointLow", "decimal", title: "Temperature Low Setpoint", required:true, submitOnChange:true, width:6
                        input "teSetPointHigh", "decimal", title: "Temperature High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    input "tempConditionOnly", "bool", title: "Use Temperature as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                    if(tempConditionOnly) {
                        if(setTEPointHigh && teSetPointHigh) paragraph "Cog will use 'as condition' when Temperature reading is above or equal to ${teSetPointHigh}"
                        if(setTEPointLow && teSetPointLow) paragraph "Cog will use 'as condition' when Temperature reading is below ${teSetPointLow}"
                        if(setTEPointBetween) paragraph "Cog will use 'as condition' when Temperature reading is between ${teSetPointLow} and ${teSetPointHigh}"
                    } else {
                        if(setTEPointHigh && teSetPointHigh) paragraph "Cog will trigger when Temperature reading is above or equal to ${teSetPointHigh}"
                        if(setTEPointLow && teSetPointLow) paragraph "Cog will trigger when Temperature reading is below ${teSetPointLow}"
                        if(setTEPointBetween) paragraph "Cog will trigger when Temperature reading is between ${teSetPointLow} and ${teSetPointHigh}"
                    }
                }
                input "tempConditionOnly", "bool", title: "Use Temp as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Temperature Setpoints: ${tempEvent} - setpoint Low: ${teSetPointLow}, setpoint High: ${teSetPointHigh}, inBetween: ${setTEPointBetween}<br>"
                if(tempConditionOnly) {
                    theCogTriggers += " - Condition Only: ${tempConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("tempEvent")
                app.removeSetting("teSetPointHigh")
                app.removeSetting("teSetPointLow")
                app.removeSetting("setTEPointHigh")
                app.removeSetting("setTEPointLow")
            }
// -----------            
            if(triggerType.contains("xTherm")) {
                paragraph "<b>Thermostat</b>"
                paragraph "Tracks the state of the thermostat. It will react if not in Idle. (ie. heating or cooling)"
                input "thermoEvent", "capability.thermostat", title: "Thermostat to track", required:false, multiple:true, submitOnChange:true
                input "thermoANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                if(thermoANDOR) {
                    paragraph "Condition true when <b>any</b> Thermostat is true"
                } else {
                    paragraph "Condition true when <b>all</b> Thermostats are true"
                }
                input "thermoConditionOnly", "bool", title: "Use Thermostat as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Thermostat: ${thermoEvent} - ANDOR: ${thermoANDOR}<br>"
                if(thermoConditionOnly) {
                    theCogTriggers += " - Condition Only: ${thermoConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("thermoEvent")
            }
// -----------
            if(triggerType.contains("xTransition")) {
                paragraph "<b>Transitions</b>"
                input "transitionType", "enum", title: "Type of Transition", options: ["Device Attribute", "HSM Status", "Mode"], required:true, multiple:false, submitOnChange:true               
                if(transitionType == "Device Attribute") {
                    paragraph "<u>Device Attribute</u>"
                    input "attTransitionEvent", "capability.*", title: "Select a device", required:false, multiple:false, submitOnChange:true
                    if(attTransitionEvent) {
                        allAttrs1 = []
                        allAttrs1 = attTransitionEvent.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                        allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                        input "attTransitionAtt", "enum", title: "Attribute to track", options: allAttrs1a, required:true, multiple:false, submitOnChange:true                   
                        paragraph "Enter in the attribute values required to trigger Cog. Must be exactly as seen in the device current stats. (ie. on/off, open/closed)"
                        input "atAttribute1", "text", title: "FROM Attribute Value", required:true, submitOnChange:true, width:6
                        input "atAttribute2", "text", title: "TO Attribute Value", required:true, submitOnChange:true, width:6        
                        theCogTriggers += "<b>-</b> By Device Attribute Transition: ${attTransitionEvent} - From: ${atAttribute1} - To: ${atAttribute2}<br>"
                    } else {
                        app.removeSetting("attTransitionAtt")
                    }
                } else if(transitionType == "HSM Status") {
                    paragraph "<u>HSM Status</u>"
                    input "atAttribute1", "enum", title: "TO HSM Status", options: [
                        ["allDisarmed":"All Disarmed"],
                        ["armedAway":"Armed Away"],
                        ["armedHome":"Armed Home"],
                        ["armedNight":"Armed Night"],
                        ["disarmed":"Disarmed"]
                    ], multiple:false, submitOnChange:true, width:6
                    input "atAttribute2", "enum", title: "TO HSM Status", options: [
                        ["allDisarmed":"All Disarmed"],
                        ["armedAway":"Armed Away"],
                        ["armedHome":"Armed Home"],
                        ["armedNight":"Armed Night"],
                        ["disarmed":"Disarmed"]
                    ], multiple:false, submitOnChange:true, width:6
                    theCogTriggers += "<b>-</b> By Transition: ${transitionType} - attTransitionEvent: ${attTransitionEvent} - From: ${atAttribute1} - To: ${atAttribute2}<br>"
                } else if(transitionType == "Mode") {
                    paragraph "<u>Mode</u>"
                    input "atAttribute1", "mode", title: "FROM Mode", multiple:false, submitOnChange:true, width:6
                    input "atAttribute2", "mode", title: "TO Mode", multiple:false, submitOnChange:true, width:6
                    theCogTriggers += "<b>-</b> By Transition: ${transitionType} - attTransitionEvent: ${attTransitionEvent} - From: ${atAttribute1} - To: ${atAttribute2}<br>"
                }               
            } else {
                app.removeSetting("transitionType")
                app.removeSetting("attTransitionEvent")
                app.removeSetting("attTransitionAtt")
                app.removeSetting("atAttribute1")
                app.removeSetting("atAttribute2")
            }
// -----------
            if(triggerType.contains("xVoltage")) {
                paragraph "<b>Voltage</b>"
                input "voltageEvent", "capability.voltageMeasurement", title: "By Voltage Setpoints", required:false, multiple:true, submitOnChange:true
                if(voltageEvent) {
                    input "setVEPointHigh", "bool", title: "Condition true when Voltage is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Voltage High", submitOnChange:true
                    if(setVEPointHigh) {
                        input "veSetPointHigh", "decimal", title: "Voltage High Setpoint", required:true, submitOnChange:true
                    }
                    input "setVEPointLow", "bool", title: "Condition true when Voltage is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Voltage Low", submitOnChange:true
                    if(setVEPointLow) {
                        input "veSetPointLow", "decimal", title: "Voltage Low Setpoint", required:true, submitOnChange:true
                    }
                    input "setVEPointBetween", "bool", title: "Condition true when Voltage is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Voltage Between", submitOnChange:true
                    if(setVEPointBetween) {
                        input "veSetPointLow", "decimal", title: "Voltage Low Setpoint", required:true, submitOnChange:true, width:6
                        input "veSetPointHigh", "decimal", title: "Voltage High Setpoint", required:true, submitOnChange:true, width:6
                    }
                    if(veSetPointHigh) paragraph "Cog will trigger when Voltage reading is above or equal to ${veSetPointHigh}"
                    if(veSetPointLow) paragraph "Cog will trigger when Voltage reading is below ${veSetPointLow}"
                }
                input "voltageConditionOnly", "bool", title: "Use Voltage as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                theCogTriggers += "<b>-</b> By Voltage Setpoints: ${voltageEvent} - setpoint Low: ${veSetPointLow}, setpoint High: ${veSetPointHigh}, inBetween: ${setVEPointBetween}<br>"
                if(voltageConditionOnly) {
                    theCogTriggers += " - Condition Only: ${voltageConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("voltageEvent")
                app.removeSetting("veSetPointHigh")
                app.removeSetting("veSetPointLow")
                app.removeSetting("setVEPointHigh")
                app.removeSetting("setVEPointLow")
            }
// -----------
            if(triggerType.contains("xWater")) {
                paragraph "<b>Water</b>"
                input "waterEvent", "capability.waterSensor", title: "By Water Sensor", required:false, multiple:true, submitOnChange:true
                if(waterEvent) {
                    input "wsDryWet", "bool", title: "Condition true when Dry (off) or Wet (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Water", submitOnChange:true
                    if(wsDryWet) {
                        paragraph "Condition true when Sensor(s) become Wet"
                    } else {
                        paragraph "Condition true when Sensor(s) become Dry"
                    }
                    input "waterANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Condition true when <b>any</b> Water Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Water Sensors are true"
                    }
                    theCogTriggers += "<b>-</b> By Water Sensor: ${waterEvent} - DryWet: ${wsDryWet}, ANDOR: ${waterANDOR}<br>"
                } else {
                    app.removeSetting("waterEvent")
                    app.removeSetting("wsDryWet")
                    app.removeSetting("waterANDOR")
                }

                input "waterRestrictionEvent", "capability.waterSensor", title: "Restrict By Water Sensor", required:false, multiple:true, submitOnChange:true
                if(waterRestrictionEvent) {
                    input "wrDryWet", "bool", title: "Restrict when Dry (off) or Wet (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Water", submitOnChange:true
                    if(wrDryWet) {
                        paragraph "Restrict when Sensor(s) become Wet"
                    } else {
                        paragraph "Restrict when Sensor(s) become Dry"
                    }
                    input "waterRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Restrict when <b>any</b> Water Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Water Sensors are true"
                    }
                    theCogTriggers += "<b>Restriction:</b> By Water Sensor: ${waterRestrictionEvent} - DryWet: ${wrDryWet}, ANDOR: ${waterANDOR}<br>"
                } else {
                    app.removeSetting("waterRestrictionEvent")
                    app.removeSetting("wrDryWet")
                    app.removeSetting("waterRANDOR")
                }
                input "waterConditionOnly", "bool", title: "Use Water as a Condition but NOT as a Trigger <small><abbr title='If this is true, the selection will be included in the Cogs logic BUT can not cause the Cog to start on it's own.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(waterConditionOnly) {
                    theCogTriggers += " - Condition Only: ${waterConditionOnly}<br>"
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("waterEvent")
                app.removeSetting("wsDryWet")
                app.removeSetting("waterANDOR")
                app.removeSetting("waterRestrictionEvent")
                app.removeSetting("wrDryWet")
                app.removeSetting("waterRANDOR")
                app.removeSetting("waterConditionOnly")
            }
// -----------
            if(triggerType.contains("xCustom")) {
                paragraph "<b>Custom Attribute</b>"
                input "customEvent", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
                if(customEvent) {
                    allAttrs1 = []
                    allAttrs1 = customEvent.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                    allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                    input "specialAtt", "enum", title: "Attribute to track", options: allAttrs1a, required:true, multiple:false, submitOnChange:true
                    input "deviceORsetpoint", "bool", title: "Device (off) or Setpoint (on)", description: "Whole", submitOnChange:true
                    if(deviceORsetpoint) {
                        input "setSDPointHigh", "bool", title: "Condition true when Custom is too High <small><abbr title='Cog will run when reading is greater than setpoint.'><b>- INFO -</b></abbr></small>", description: "Custom High", submitOnChange:true
                        if(setSDPointHigh) {
                            input "sdSetPointHigh", "decimal", title: "Custom High Setpoint", required:true, submitOnChange:true
                        }
                        input "setSDPointLow", "bool", title: "Condition true when Custom is too Low <small><abbr title='Cog will run when reading is less than setpoint.'><b>- INFO -</b></abbr></small>", description: "Custom Low", submitOnChange:true
                        if(setSDPointLow) {
                            input "sdSetPointLow", "decimal", title: "Custom Low Setpoint", required:true, submitOnChange:true
                        }
                        input "setSDPointBetween", "bool", title: "Condition true when Custom is Between two Setpoints <small><abbr title='Cog will run when reading is Between two setpoints.'><b>- INFO -</b></abbr></small>", description: "Custom Between", submitOnChange:true
                        if(setSDPointBetween) {
                            input "sdSetPointLow", "decimal", title: "Custom Low Setpoint", required:true, submitOnChange:true, width:6
                            input "sdSetPointHigh", "decimal", title: "Custom High Setpoint", required:true, submitOnChange:true, width:6
                        }
                        if(setSDPointHigh) paragraph "Cog will trigger when Custom reading is above or equal to ${sdSetPointHigh}"
                        if(setSDPointLow) paragraph "Cog will trigger when Custom reading is below ${sdSetPointLow}"
                        if(setSDPointBetween) paragraph "Cog will trigger when Custom reading is between ${sdSetPointLow} and ${sdSetPointHigh}"
                        theCogTriggers += "<b>-</b> By Custom Setpoints: ${customEvent} - setpoint Low: ${sdSetPointLow}, setpoint High: ${sdSetPointHigh}, inbetween: ${setSDPointBetween}<br>"          
                        app.removeSetting("custom1")
                        app.removeSetting("custom2")
                        app.removeSetting("sdCustom1Custom2")
                        app.removeSetting("customANDOR")
                    } else {
                        paragraph "Enter in the attribute values required to trigger Cog. Must be exactly as seen in the device current stats. (ie. on/off, open/closed)"
                        input "custom1", "text", title: "Attribute Value 1", required:true, submitOnChange:true
                        input "custom2", "text", title: "Attribute Value 2", required:true, submitOnChange:true
                        input "sdCustom1Custom2", "bool", title: "Condition true when ${custom1} (off) or ${custom2} (on) <small><abbr title='Choose which status will be considered true and run the Cog'><b>- INFO -</b></abbr></small>", description: "Custom", submitOnChange:true
                        if(sdCustom1Custom2) {
                            paragraph "Condition true when Custom(s) become ${custom1}"
                        } else {
                            paragraph "Condition true when Custom(s) become ${custom2}"
                        }
                        paragraph "* Remember - If Conditions are working backwards, simply reverse your values above."
                        input "customANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on) <small><abbr title='‘AND’ requires that ALL selected devices are in the state selected. ‘OR’ requires that ANY selected device is in the state selected.'><b>- INFO -</b></abbr></small>", description: "And Or", submitOnChange:true
                        if(customANDOR) {
                            paragraph "Condition true when <b>any</b> Custom is true"
                        } else {
                            paragraph "Condition true when <b>all</b> Custom are true"
                        }
                        theCogTriggers += "<b>-</b> By Custom Setpoints: ${customEvent} - setpoint Low: ${sdSetPointLow}, setpoint High: ${sdSetPointHigh}, inBetween: ${setSDPointBetween}<br>"
                        theCogTriggers += "<b>-</b> By Custom: ${customEvent} - custom1: ${custom1} - custom2: ${custom2} - value1or2: ${sdCustom1Custom2}, ANDOR: ${customANDOR}<br>"
                        app.removeSetting("sdSetPointHigh")
                        app.removeSetting("sdSetPointLow")
                        app.removeSetting("setSDPointHigh")
                        app.removeSetting("setSDPointLow")
                    }
                }
            } else {
                app.removeSetting("customEvent")
                app.removeSetting("specialAtt")
                app.removeSetting("custom1")
                app.removeSetting("custom2")
                app.removeSetting("sdCustom1Custom2")
                app.removeSetting("customANDOR")
                app.removeSetting("sdSetPointHigh")
                app.removeSetting("sdSetPointLow")
                app.removeSetting("setSDPointHigh")
                app.removeSetting("setSDPointLow")
            }

            if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || state.useRollingAverage || (customEvent && deviceORsetpoint)) {
                input "spDirection", "bool", title: "Track direction of setpoint <small><abbr title='Condition can also track which direction the setpoint is heading.'><b>- INFO -</b></abbr></small>", description: "Cond Only", submitOnChange:true
                if(spDirection) {
                    input "spDirDownUp", "bool", title: "Condition is 'true' when setpoint is headind Down (off) or Up (on)", description: "Setpoint Direction", submitOnChange:true
                    input "spDirMinValue", "decimal", title: "Min Difference to count towards direction <small><abbr title='If difference between last reading and current reading is less than Min Difference, the current value will be tossed and not used to determine direction.'><b>- INFO -</b></abbr></small>", defaultValue:0, submitOnChange:true
                    paragraph "<hr>"
                } else {
                    app.removeSetting("spDirDownUp")
                    app.removeSetting("spDirMinValue")
                }
                input "setpointRollingAverage", "bool", title: "Use a rolling Average for setpoints <small><abbr title='Use multiple readings instead of a single instance to control the Cog.'><b>- INFO -</b></abbr></small>", description: "average", submitOnChange:true
                if(setpointRollingAverage) {
                    paragraph "<small>*All values are rounded for this option</small>"
                    input "numOfPoints", "number", title: "Number of points to average", required:true, submitOnChange:true
                    app.updateSetting("useWholeNumber",[value:"true",type:"bool"])
                } else {
                    app.removeSetting("numOfPoints")
                }
                input "useWholeNumber", "bool", title: "Only use Whole Numbers (round each number)", description: "Whole", submitOnChange:true
                if(setpointRollingAverage) paragraph "<b>When using a Rolling Average, use Whole Numbers MUST also be true.</b>"
                paragraph "<small>* Note: This effects the data coming in from the device.</small>"
                paragraph "<hr>"
                paragraph "Setpoint truths can also be reset one time daily. Typically to allow another notification of a high/low reading."
                input "spResetTime", "time", title: "Time to reset Setpoint truths (optional)", description: "Reset SP", required:false
                if(spDirection) {
                    if(spDirDownUp) {
                        theDir = "Up"
                    } else {
                        theDir = "Down"
                    }
                } else {
                    theDir = "NA"
                }
                theCogTriggers += "<b>-</b> Setpoint Options: Rolling Average: ${setpointRollingAverage} - Use Whole Numbers: ${useWholeNumber} - ResetTime: ${spResetTime} - Direction: ${theDir} - Min Change: ${spDirMinValue}<br>"
            } else {
                app.removeSetting("spResetTime")
                app.removeSetting("useWholeNumber")
                app.removeSetting("setpointRollingAverage")
                app.removeSetting("spDirDownUp")
                app.removeSetting("spDirMinValue")
            }

            if(accelerationEvent || batteryEvent || contactEvent || humidityEvent || hsmAlertEvent || hsmStatusEvent || illuminanceEvent || modeEvent || motionEvent || powerEvent || presenceEvent || switchEvent || tempEvent || waterEvent || xhttpIP) {
                input "setDelay", "bool", title: "<b>Set Delay</b> <small><abbr title='Delay the notifications until all devices have been in state for XX minutes.'><b>- INFO -</b></abbr></small>", description: "Delay Time", submitOnChange:true, width:6
                input "randomDelay", "bool", title: "<b>Set Random Delay</b> <small><abbr title='Delay the notifications until all devices have been in state for XX minutes.'><b>- INFO -</b></abbr></small>", description: "Random Delay", submitOnChange:true, width:6
                if(setDelay && randomDelay) paragraph "<b>Warning: Please don't select BOTH Set Delay and Random Delay.</b>"
                if(setDelay) {
                    input "notifyDelay", "number", title: "Delay (1 to 60)", required:true, multiple:false, range: '1..60', width:6
                    input "minSec", "bool", title: "Use Minutes (off) or Seconds (on)", description: "minSec", submitOnChange:true, width:6
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the actions will be cancelled.</small>"
                    if(minSec) {
                        minSecValue = "Second(s)"
                    } else {
                        minSecValue = "Minute(s)"
                    }
                    theCogTriggers += "<b>-</b> Set Delay: ${setDelay} - notifyDelay: ${notifyDelay} ${minSecValue} - Random Delay: ${randomDelay}<br>"
                } else {
                    app.removeSetting("notifyDelay")
                    app.updateSetting("setDelay",[value:"false",type:"bool"])
                }
                if(randomDelay) {
                    input "delayLow", "number", title: "Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', submitOnChange:true
                    input "delayHigh", "number", title: "Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', submitOnChange:true
                    if(delayHigh <= delayLow) paragraph "<b>Delay High must be greater than Delay Low.</b>"
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the notifications will be cancelled.</small>"
                    theCogTriggers += "<b>-</b> Random Delay: ${randomDelay} - Delay Low: ${delayLow} - Delay High: ${delayHigh}<br>"
                } else {
                    app.removeSetting("delayLow")
                    app.removeSetting("delayHigh")
                    app.updateSetting("randomDelay",[value:"false",type:"bool"])
                }
            } else {
                app.removeSetting("notifyDelay")
                app.removeSetting("setDelay")
                app.removeSetting("delayLow")
                app.removeSetting("delayHigh")
                app.removeSetting("randomDelay")
            }
        }
// ***** Debounce Option Start *****
        section(getFormat("header-green", "${getImage("Blank")}"+" Debounce Option")) {
            paragraph "With this option enabled:<br> - Conditions become True, runs the Actions<br> - It won't run the Actions again until the Conditions become False"
            input "useAppStatus", "bool", title: "Use App Debounce", submitOnChange:true     
        }  
// ***** Condition Helper Start *****
        section(getFormat("header-green", "${getImage("Blank")}"+" Condition Helper (optional)")) {}
        section("${getImage('instructions')} Condition Helper Examples", hideable: true, hidden: true) {
            paragraph "Examples of Primary and Secondary Condition use"
            paragraph "<b>Bathroom</b><br>Walk into bathroom and trigger the 'Ceiling Motions Sensor' (primary), lights come on. Stay still too long and lights will turn off."
            paragraph "Close the door to trigger the 'contact sensor' (secondary). Even if the motion becomes inactive, (it can not see you when in the shower), the lights will not turn off until that door is opened and the motion is inactive."
            paragraph "<hr>"
            paragraph "<b>Kitchen</b><br>Lights are off - 'Kitchen Ceiling Motion Sensor' (primary) triggers room to be occupied, lights come on.  'Motion sensor under table' (secondary) helps lights to stay on even if 'Kitchen Ceiling Motion Sensor' becomes inactive."
            paragraph "Dog walks under table and triggers the 'Motion sensor under table' (secondary) but the lights were off, lights stay off."
            paragraph "<hr>"
            paragraph "<b>Living Room</b><br>Walk into the room and trigger the 'Ceiling Motion Sensor' (primary), lights come on. If sensor becomes inactive, lights will turn off"
            paragraph "Place phone on 'charger' (secondary). Lights will stay on even if 'Ceiling Motion Sensor' becomes inactive."
            paragraph "<hr>"
            paragraph "<i>Have something neat that you do with primary and secondary triggers? Please post it on the forums and I just might add it here! Thanks</i>"
        }
        section() {
            input "useHelper", "bool", title: "Use Condition Helper <small><abbr title='This will help the conditions stay true but not trigger the conditions on its own.'><b>- INFO -</b></abbr></small>", submitOnChange:true
            if(useHelper) {
                input "myContacts2", "capability.contactSensor", title: "Select the Contact sensor(s) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(myContacts2) input "contactOption2", "bool", title: "Condition true when Closed (off) or Open (on) <small><abbr title='Choose which status will be considered true and help keep the Cog in state.'><b>- INFO -</b></abbr></small>", description: "bool", submitOnChange:true
                
                input "myMotion2", "capability.motionSensor", title: "Select the Motion sensor(s) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(myMotion2) input "motionOption2", "bool", title: "Condition true when Inactive (off) or Active (on) <small><abbr title='Choose which status will be considered true and help keep the Cog in state.'><b>- INFO -</b></abbr></small>", description: "bool", submitOnChange:true
                
                input "myPresence2", "capability.presenceSensor", title: "Select the Presence Sensor(s) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(myPresence2) input "presenceOption2", "bool", title: "Condition true when Present (off) or Not Present (on) <small><abbr title='Choose which status will be considered true and help keep the Cog in state.'><b>- INFO -</b></abbr></small>", description: "bool", submitOnChange:true
                
                input "mySwitches2", "capability.switch", title: "Select Switch(es) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(mySwitches2) input "switchesOption2", "bool", title: "Condition true when Off (off) or On (on) <small><abbr title='Choose which status will be considered true and help keep the Cog in state.'><b>- INFO -</b></abbr></small>", description: "bool", submitOnChange:true
                paragraph "<small>* All helpers are considered 'OR'</small>"
                if(myContacts2) {
                    theCogTriggers += "<b>-</b> Condition Helper - Contacts: ${myContacts2} - Closed/Open: ${contactOption2}<br>"
                }
                if(myMotion2) {
                    theCogTriggers += "<b>-</b> Condition Helper - Motion: ${myMotion2} - Inactive/Active: ${motionOption2}<br>"
                }
                if(myPresence2) {
                    theCogTriggers += "<b>-</b> Condition Helper - Presence: ${myPresence2} - Present/Not Active: ${presenceOption2}<br>"
                }
                if(mySwitches2) {
                    theCogTriggers += "<b>-</b> Condition Helper - Switches: ${mySwitches2} - Off/On: ${switchesOption2}<br>"
                }
            } else {
                app.removeSetting("myContacts2")
                app.removeSetting("myMotion2")
                app.removeSetting("myPresence2")
                app.removeSetting("mySwitches2")                
                app.removeSetting("contactOption2")
                app.removeSetting("motionOption2")
                app.removeSetting("presenceOption2")
                app.removeSetting("switchesOption2")
            }  
        }
// ***** Condition Helper End *****        
// ********** Start Actions **********
        theCogActions += "<b><u>Actions</u></b><br>"
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Actions")) {
            input "actionType", "enum", title: "Actions to Perform <small><abbr title='This is what will happen once the conditions are met. Choose as many as you need.'><b>- INFO -</b></abbr></small>", options: [
                ["aBlueIris":"Blue Iris Control"],
                ["aEventEngine":"Event Engine"],
                ["aFan":"Fan Control"],
                ["aGarageDoor":"Garage Doors"],
                ["aHubVariables":"Hub Variables"],
                ["aHSM":"Hubitat Safety Monitor"],
                ["aLZW45":"Inovelli Light Strip (LZW45)"],
                ["aLifxStrip":"Lifx Pattern Controller"],
                ["aLock":"Locks"],
                ["aMode":"Modes"],
                ["aNotification":"Notifications (speech/push/flash)"],
                ["aRefresh":"Refresh"],
                ["aSecurityKeypad":"Ring Security Keypad G2"],
                ["aRule":"Rule Machine"],
                ["aSendHTTP":"Send Hub Command"],
                ["aGVar":"Set EE Global Variable"],
                ["aSwitch":"Switches"],
                ["aSwitchSequence":"Switches In Sequence"],
                ["aSwitchesPerMode":"Switches Per Mode"],
                ["aSwitchesColorChange":"Switches To Color Change"],
                ["aSwitchesToSync":"Switches To Sync"],
                ["aThermostat":"Thermostat"],
                ["aValve":"Valves"],
                ["aWindowBlind":"Window Blind"],
                ["aWindowShade":"Window Shade"],
                ["aVirtualContact":"* Virtual Contact Sensor"]
            ], required:false, multiple:true, submitOnChange:true
            paragraph "<hr>"
            if(actionType == null) actionType = " "
// -----------
            if(actionType.contains("aBlueIris")) {                
                paragraph "<b>Blue Iris Control</b>"
                if(parent.biServer && parent.biUser && parent.biPass) {
                    input "biControl", "enum", title: "Select Control Type", submitOnChange:true, options: ["Switch_Profile", "Switch_Schedule", "Camera_Preset", "Camera_Snapshot", "Camera_Trigger", "Camera_PTZ", "Camera_Reboot", "Camera_Enable", "Camera_Disable"], required:true, Multiple:false             
                    if(biControl == "Switch_Profile") {
                        input "switchProfileOn", "enum", title: "Profile to change to when switch is On", options: [
                            [Pon0:"Profile 0"],
                            [Pon1:"Profile 1"],
                            [Pon2:"Profile 2"],
                            [Pon3:"Profile 3"],
                            [Pon4:"Profile 4"],
                            [Pon5:"Profile 5"],
                            [Pon6:"Profile 6"],
                            [Pon7:"Profile 7"],
                        ], required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Profile: ${switchProfileOn}<br>"
                    } else {
                        app.removeSetting("switchProfileOn")
                    }                    
                    if(biControl == "Switch_Schedule") {
                        input "biScheduleName", "text", title: "Schedule Name", description: "The exact name of the BI schedule"
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Schedule: ${biScheduleName}<br>"
                    } else {
                        app.removeSetting("biScheduleName")
                    }                                    
                    if(biControl == "Camera_Preset") {
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        input "biCameraPreset", "enum", title: "Preset number", options: [
                            [PS1:"Preset 1"],
                            [PS2:"Preset 2"],
                            [PS3:"Preset 3"],
                            [PS4:"Preset 4"],
                            [PS5:"Preset 5"],
                        ], required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Camera: ${biCamera} - Preset: ${biCameraPreset}<br>"
                    } else {
                        app.removeSetting("biCameraPreset")
                    }
                    if(biControl == "Camera_Snapshot"){
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Camera: ${biCamera}<br>"
                    } else {
                    }
                    if(biControl == "Camera_Trigger"){
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        paragraph "Camera Trigger can use two methods. If one doesn't work for you, please try the other."
                        input "useMethod", "bool", title: "Manrec (off) or Trigger (on)", submitOnChange:true
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - useMethod: ${useMethod}<br>"
                    } else {
                        app.removeSetting("useMethod")
                    }
                    if(biControl == "Camera_PTZ"){
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        input "biCameraPTZ", "enum", title: "PTZ Command", options: [
                            [PTZ0:"0 - Left"],
                            [PTZ1:"1 - Right"],
                            [PTZ2:"2 - Up"],
                            [PTZ3:"3 - Down"],
                            [PTZ4:"4 - Home"],
                            [PTZ5:"5 - Zoom In"],
                            [PTZ6:"6 - Zoom Out"],
                        ], required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Camera: ${biCamera} - PTZ Command: ${biCameraPTZ}<br>"
                    } else {
                        app.removeSetting("biCameraPTZ")
                    }
                    if(biControl == "Camera_Reboot"){
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Reboot Camera: ${biCamera}<br>"
                    }
                    paragraph "<hr>"
                    if(biControl == "Camera_Enable"){
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Enable Camera: ${biCamera}<br>"
                    }
                    paragraph "<hr>"
                    if(biControl == "Camera_Disable"){
                        input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                        theCogActions += "<b>-</b> Blue Iris: ${biControl} - Disable Camera: ${biCamera}<br>"
                    }
                    paragraph "<hr>"
                } else {
                    paragraph "<b>Be sure to fill out the Blue Iris Information in the EE Parent app before selecting this option.</b>"
                    paragraph "<hr>"
                }
            } else {
                app.removeSetting("switchProfileOn")
                app.removeSetting("biScheduleName")
                app.removeSetting("biControl")
                app.removeSetting("biCamera")
                app.removeSetting("biCameraPreset")
                app.removeSetting("useMethod")
                app.removeSetting("biCameraPTZ")
            }
// -----------
            if(actionType.contains("aEventEngine")) {
                paragraph "<b>Event Engine Control</b>"
                paragraph "Sorry, this option has been Removed - for now."
                //data = app.id
                //parent.mapOfChildren(app.id)                
                //input "eeAction", "enum", title: "Event Engine Cog", options: state.mapOfChildren, multiple:true, submitOnChange:true
                //input "eeCommand", "enum", title: "Command", options: ["pause", "resume", "reverse", "run"], multiple:false, submitOnChange:true
                paragraph "<hr>"
                //theCogActions += "<b>-</b> Event Engine: ${eeAction} - Command: ${eeCommand}<br>"
                app.removeSetting("eeAction")
                app.removeSetting("eeCommand")
            } else {
                app.removeSetting("eeAction")
                app.removeSetting("eeCommand")
            }
            
            if(actionType.contains("aFan")) {
                paragraph "<b>Fan Control</b>"
                input "fanAction", "capability.fanControl", title: "Fan Devices", multiple:true, submitOnChange:true
                input "fanSpeed", "enum", title: "Set Fan Speed", required:false, multiple:false, options: ["low","medium-low","medium","medium-high","high","on","off","auto"]
                paragraph "<hr>"
                theCogActions += "<b>-</b> Set Fan: ${fanAction} - speed: ${fanSpeed}<br>"
            } else {
                app.removeSetting("fanAction")
                app.removeSetting("fanSpeed")
            }

            if(actionType.contains("aGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorClosedAction", "capability.garageDoorControl", title: "Close Devices", multiple:true, submitOnChange:true
                input "garageDoorOpenAction", "capability.garageDoorControl", title: "Open Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
                theCogActions += "<b>-</b> Garage Door - Close Devices: ${garageDoorClosedAction} - Open Devices: ${garageDoorOpenAction}<br>"
            } else {
                app.removeSetting("garageDoorClosedAction")
                app.removeSetting("garageDoorOpenAction")
            }

            if(actionType.contains("aHubVariables")) {
                paragraph "<b>Hub Variables</b>"
                paragraph "<small>Be sure to setup Hub Variables in the Settings/Hub Variables section of Hubitat.</small>"
                HashMap varMap = getAllGlobalVars()
                state.varList = []              
                varMap.each {
                    state.varList.add("$it.key")
                }
                input "theActVariable", "enum", title: "Select Variable", options: state.varList.sort(), multiple:false, required:false, submitOnChange:true, width:4
                if(theActVariable) {
                    varActData = getGlobalVar(theActVariable)
                    if(varActData.type == "integer" || varActData.type == "bigdecimal") {
                        input "varOperation", "enum", title: "Operation", options: ["Set Number", "Add Number", "Device Attribute - Replace", "Device Attribute - Add To"], submitOnChange:true, width:6
                        if(varOperation == "Set Number") {
                            input "varOperationValue", "number", title: "Value - Sets a completely new value", submitOnChange:true
                            paragraph "${varActData.name} - Current Value: ${varActData.value}"
                        } else if(varOperation == "Add Number") {
                            if(varActData.type == "integer") {
                                input "varOperationValue", "number", title: "Value - Add this integer to the variable", submitOnChange:true
                            } else {
                                input "varOperationValue", "text", title: "Value - Add this bigdecimal to the variable", submitOnChange:true
                            }
                            paragraph "${varActData.name} - Current Value: ${varActData.value}"
                        } else if(varOperation == "Device Attribute - Replace" || varOperation == "Device Attribute - Add To") {
                            input "varOperationDevice", "capability.*", title: "Select Device", multiple:false, submitOnChange:true, width:6
                            if(varOperationDevice) {
                                allAttrs = []
                                attributes = varOperationDevice.supportedAttributes
                                attributes.each { att ->
                                    theType = att.getDataType()
                                    if(theType == "NUMBER") {
                                        allAttrs << att.name
                                    }
                                }
                                allAttrs = allAttrs.unique().sort()
                                input "varOperationAttribute", "enum", title: "Select Attribute", options:allAttrs, submitOnChange:true, width:6
                                paragraph "<small>* Note: only 'number' attributes are shown</small>"
                            }
                            paragraph "<hr>"
                            if(theActVariable) {
                                paragraph "${varActData.name} - Current Value: ${varActData.value}"
                            }
                            if(varOperationAttribute) {
                                curVal = varOperationDevice.currentValue("$varOperationAttribute")
                                paragraph "${varOperationDevice} - Current Value: ${curVal}"
                            }
                        }
                    } else if(varActData.type == "string") {
                        input "varOperation", "enum", title: "Operation", options: ["Set String", "Remove String", "Replace String", "Device Attribute"], submitOnChange:true, width:6
                        if(varOperation == "Set String") {
                            input "varOperationValue", "text", title: "Value - Sets a completely new string", submitOnChange:true
                            paragraph "${varActData.name} - Current Value: ${varActData.value}"
                        } else if(varOperation == "Remove String") {
                            input "varOperationValue", "text", title: "Value - Can be any part of the string", submitOnChange:true
                            paragraph "${varActData.name} - Current Value: ${varActData.value}"
                        } else if(varOperation == "Replace String") {
                            input "varOperationValueOld", "text", title: "Value - Can be any part of the string", submitOnChange:true
                            input "varOperationValueNew", "text", title: "Replacement String", submitOnChange:true
                            paragraph "${varActData.name} - Current Value: ${varActData.value}"
                        } else if(varOperation == "Device Attribute") {
                            input "varOperationDevice", "capability.*", title: "Select Device", multiple:false, submitOnChange:true, width:6
                            if(varOperationDevice) {
                                allAttrs = []
                                attributes = varOperationDevice.supportedAttributes
                                attributes.each { att ->
                                    allAttrs << att.name
                                }
                                allAttrs = allAttrs.unique().sort()
                                input "varOperationAttribute", "enum", title: "Select Attribute", options:allAttrs, submitOnChange:true, width:6
                            }
                            paragraph "<hr>"
                            if(theActVariable) {
                                paragraph "${varActData.name} - Current Value: ${varActData.value}"
                            }
                            if(varOperationAttribute) {
                                curVal = varOperationDevice.currentValue("$varOperationAttribute")
                                paragraph "${varOperationDevice} - Current Value: ${curVal}"
                            }
                        }                      
                    } else if(varActData.type == "boolean") {
                        input "varOperationValue", "enum", title: "Set Boolean", options: ["true", "false"], submitOnChange:true, width:6
                        paragraph "${varActData.name} - Current Value: ${varActData.value}"                      
                    } else if(varActData.type == "datetime") {
                        paragraph "Work in progress"
                    }
                    
                    if(logEnable) {
                        paragraph "<hr>"
                        paragraph "varActData: ${varActData}"
                    }
                }  
                paragraph "<hr>"
                theCogActions += "<b>-</b> Set Hub Variable: ${theActVariable} - Operator: ${actVar}<br>"
                
            } else {
                app.removeSetting("theActVariable")
                app.removeSetting("setGVvalue")
            }
            
            if(actionType.contains("aHSM")) {
                paragraph "<b>HSM</b>"
                input "setHSM", "enum", title: "Set HSM state", required:false, multiple:false, options: [
                    ["armAway":"Arm Away"],
                    ["armHome":"Arm Home"],
                    ["armNight":"Arm Night"],
                    ["disarm":"Disarm"],
                    ["disarmAll":"Disarm All"],
                    ["armAll":"Arm Monitor Rules"],
                    ["CancelAlerts":"Cancel Alerts"]
                ]
                paragraph "<hr>"
                theCogActions += "<b>-</b> Set HSM state: ${setHSM}<br>"
            } else {
                app.removeSetting("setHSM")
            }

            if(actionType.contains("aLZW45")) {
                paragraph "<b>Inovelli Light Strip (LZW45)</b>"
                paragraph "Help setting up your Light Strip can be found <a href='https://support.inovelli.com/portal/en/kb/articles/how-to-setup-effects-with-the-red-series-smart-led-strip-hubitat' target=_blank>HERE</a>."
                input "lzw45Action", "capability.switchLevel", title: "Light Strip Device", multiple:false, submitOnChange:true
                if(lzw45Action) {
                    input "lzw45Command", "enum", title: "Command (more to come!)", required:false, multiple:false, options: ["on", "off", "Custom Effect Start", "Pixel Effect Start", "Start Notification"], submitOnChange:true
                    if(lzw45Command == "Custom Effect Start") {
                        input "cesParam1", "text", title: "Custom Effect", submitOnChange:true
                        paragraph "<small>Obtain Effect number from <a href='https://nathanfiscus.github.io/inovelli-led-strip-toolbox/' target=_blank>HERE</a>.</small>"
                    } else {
                        app.removeSetting("cesParam1")
                    }
                    if(lzw45Command == "Pixel Effect Start") {
                        input "pesParam1", "number", title: "Pixel Effect", submitOnChange:true, width:6
                        input "pesParam2", "number", title: "Level", submitOnChange:true, width:6
                        paragraph "<small>Obtain Pixel Effect number from <a href='https://support.inovelli.com/portal/en/kb/articles/pixel-effects-rgbtw-smart-led-strip-controller-kit-lzw45' target=_blank>HERE</a>.</small>"
                    } else {
                        app.removeSetting("pesParam1")
                        app.removeSetting("pesParam2")
                    }
                    if(lzw45Command == "Start Notification") {
                        input "snParam1", "number", title: "Notification Number", submitOnChange:true
                        paragraph "<small>Obtain Notification number from <a href='https://nathanfiscus.github.io/inovelli-notification-calc/' target=_blank>HERE</a>.</small>"
                    } else {
                        app.removeSetting("snParam1")
                    }
                }
                paragraph "<hr>"
                theCogActions += "<b>-</b> Inovelli Light Strip: ${lzw45Action} - command: ${lzw45Command}<br>"
            } else {
                app.removeSetting("lzw45Action")
                app.removeSetting("lzw45Command")
            }
            
            if(actionType.contains("aLifxStrip")) {
                paragraph "<b>Pattern Controller</b><br><small>For use with the Lifx Strip Light and the Pattern Controller app</small>"
                if(state.pcMapOfChildren) {
                    input "pcAction", "enum", title: "Pattern to Control", options: state.pcMapOfChildren, multiple:true, submitOnChange:true
                    input "pcCommand", "enum", title: "Command", options: ["run"], multiple:false, submitOnChange:true
                    paragraph "<small>If you don't see the Pattern you're looking for:<br>- Please flip the 'Refresh Patterns' switch to continue.</small>"
                    paragraph "<hr>"
                    theCogActions += "<b>-</b> Lifx Strip Zone Controller: ${pcAction} - Command: ${pcCommand}<br>"
                } else {
                    paragraph "Looks like this is the first time using the Pattern Controller.<br> - Please flip the 'Refresh Patterns' switch, you'll need to flip it twice."
                }
                input "getPatterns", "bool", title: "Refresh Patterns", submitOnChange:true
                if(getPatterns) {
                    subscribe(location, "pcChildren", pcMapOfChildrenHandler)
                    sendLocationEvent (name: "getLifxChildren", value: "getChildren")
                    app.updateSetting("getPatterns",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("pcAction")
                app.removeSetting("pcCommand")
            }
            
            if(actionType.contains("aLock")) {
                paragraph "<b>Lock</b>"
                input "lockAction", "capability.lock", title: "Lock Devices", multiple:true, submitOnChange:true
                input "unlockAction", "capability.lock", title: "Unlock Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
                theCogActions += "<b>-</b> Lock Devices: ${lockAction} - Unlock Devices: ${unlockAction}<br>"
            } else {
                app.removeSetting("lockAction")
                app.removeSetting("unlockAction")
            }

            if(actionType.contains("aMode")) {
                paragraph "<b>Mode</b>"
                input "modeAction", "mode", title: "Change Mode to", multiple:false, submitOnChange:true
                paragraph "<hr>"
                theCogActions += "<b>-</b> Change Mode to: ${modeAction}<br>"
            } else {
                app.removeSetting("modeAction")
            }

            if(actionType.contains("aNotification")) {
                paragraph "<b>Notification</b>"
                if(useSpeech || sendPushMessage || useTheFlasher || useWebOS) {
                    href "notificationOptions", title:"${getImage("checkMarkGreen")} Notification Options", description:"Click here for options"
                } else {
                    href "notificationOptions", title:"Notification Options", description:"Click here for options"
                }
            } else {
                app.removeSetting("message")
                app.removeSetting("messageH")
                app.removeSetting("messageL")
                app.removeSetting("messageB")
                app.removeSetting("wmessage")
                app.removeSetting("wmessageH")
                app.removeSetting("wmessageL")
                app.removeSetting("wmessageB")
                app.removeSetting("useSpeech")
                app.removeSetting("fmSpeaker")
                app.removeSetting("sendPushMessage")
                app.removeSetting("useWebOS")
            }

            if(actionType.contains("aRefresh")) {
                paragraph "<b>Refresh Device</b><br>This will run before ANY other actions.<br><small>* Only works for devices that have the 'refresh' attribute.</small>"
                input "devicesToRefresh", "capability.refresh", title: "Devices to Refresh", multiple:true, submitOnChange:true
                theCogActions += "<b>-</b> Devices to Refresh: ${devicesToRefresh}<br>"
            } else {
                app.removeSetting("devicesToRefresh")
            }

            if(actionType.contains("aSecurityKeypad")) {

                paragraph "<b>Security Keypad</b><br>For use with the Ring Alarm Gen 2 Keypad using the Ring Alarm Keypad G2 Community Driver.<br><small>This will NOT work with the built in Ring G2 driver.</small>"
                input "keypadAction", "capability.securityKeypad", title: "By Security Keypad", required:false, multiple:true, submitOnChange:true
                if(keypadAction) {   
                    input "keypadTone", "enum", title: "Send Tone to selected Keypads", options: [
                        ["Tone_1":"(Tone_1) Siren (default)"],
                        ["Tone_2":"(Tone_2) Fast 3 Beeps"],
                        ["Tone_3":"(Tone_3) Fast 4 Beeps"],
                        ["Tone_4":"(Tone_4) Navi"],
                        ["Tone_5":"(Tone_5) Guitar"],
                        ["Tone_6":"(Tone_6) Windchimes"],
                        ["Tone_7":"(Tone_7) DoorBell 1"],
                        ["Tone_8":"(Tone_8) DoorBell 2"],
                        ["Tone_9":"(Tone_9) Invalid Code Sound"]
                    ], required:true, Multiple:false, submitOnChange:true
                    theCogActions += "<b>-</b> Security Keypad: Keypads: ${keypadAction} - Tone: ${keypadTone}<br>"
                } else {
                    app.removeSetting("keypadTone")
                }              
            }
            
            if(actionType.contains("aRule")) {
                paragraph "<b>Rule Machine</b>"
                input "rmRuleType", "bool", title: "Rule Type: Legacy Rules (off) or Rule 5.x and Over (on)", submitOnChange:true
                if(rmRuleType) {
                    def rules50 = RMUtils.getRuleList('5.0')
                    if(rules50) {
                        input "rmRule", "enum", title: "Select Rules 5.x", required:false, multiple:true, options: rules50, submitOnChange:true
                        if(rmRule) {
                            input "rmAction", "enum", title: "Action", required:false, multiple:false, options: [
                                ["runRuleAct":"Run"],
                                ["stopRuleAct":"Stop"],
                                ["pauseRule":"Pause"],
                                ["resumeRule":"Resume"],
                                ["runRule":"Evaluate"],
                                ["setRuleBooleanTrue":"Set Boolean True"],
                                ["setRuleBooleanFalse":"Set Boolean False"]
                            ], submitOnChange:true
                        }
                    } else {
                        paragraph "No active Rule 5.x found."
                    }
                } else {
                    def rules = RMUtils.getRuleList()
                    if(rules) {
                        input "rmRule", "enum", title: "Select Legacy Rules", required:false, multiple:true, options: rules, submitOnChange:true
                        if(rmRule) {
                            input "rmAction", "enum", title: "Action", required:false, multiple:false, options: [
                                ["runRuleAct":"Run"],
                                ["stopRuleAct":"Stop"],
                                ["pauseRule":"Pause"],
                                ["resumeRule":"Resume"],
                                ["runRule":"Evaluate"],
                                ["setRuleBooleanTrue":"Set Boolean True"],
                                ["setRuleBooleanFalse":"Set Boolean False"]
                            ], submitOnChange:true
                        }
                    } else {
                        paragraph "No active Legacy Rules found."
                    }
                }
                paragraph "<hr>"
                theCogActions += "<b>-</b> Rule Machine: ${rmRuleType} - ${rmRule} - Action: ${rmAction}<br>"
            } else {
                app.removeSetting("rmRule")
            }
            if(actionType.contains("aSendHTTP")) {
                paragraph "<b>Send HTTP Command</b><br>This can be used to send a http command to any hub on your network."
                input "httpIP", "string", title: "Enter the IP Address of the Hub (ie. http://192.168.86.81)", defaultValue: "http://", submitOnChange:true
                input "httpCommand", "enum", title: "Choose Command", options: [
                    ["/hub/reboot":"Reboot Hub"],
                    ["/hub/restart":"Restart Hub"],
                    ["/hub/zwaveRepair":"Zwave Repair"]
                ], submitOnChange:true
                paragraph "<b>Does not work with Hub Security Enabled. Work in progress</b>"
                // "hubSecurity", "bool", title: "Hub Security Enabled", submitOnChange:true
                app.removeSetting("hubSecurity")
                if(hubSecurity) {
                    input "hubUsername", "string", title: "Hub Username", required:true
                    input "hubPassword", "password", title: "Hub Password", required:true
                } else {
                    app.removeSetting("hubUsername")
                    app.removeSetting("hubPassword")
                }
                if(httpCommand) {
                    if(httpCommand.contains("reboot") || httpCommand.contains("restart")) {
                        paragraph "<b>* Once triggered, this will happen without warnings or other messages.</b>"    
                    } else if(httpCommand.contains("zwaveRepair")) {
                        paragraph "<b>* Remember to look in the log for updates and for the completion message.</b>"
                    } else {
                        paragraph ""
                    }
                }
                paragraph "<hr>"
                theCogActions += "<b>-</b> Send HTTP: ${httpIP}:8080${httpCommand}<br>"
            } else {
                app.removeSetting("httpIP")
                app.removeSetting("httpCommand")
                app.removeSetting("hubUsername")
                app.removeSetting("hubPassword")
            }

            if(actionType.contains("aGVar")) {
                paragraph "<b>Set EE Global Variable</b>"
                paragraph "<small>Be sure to setup an EE Global Variable in the parent app before trying to use this option.</small>"
                if(state.gvMap) {
                    theList = "${state.gvMap.keySet()}".replace("[","").replace("]","").replace(", ", ",")
                    theList2 = theList.split(",")              
                    input "setGVname", "enum", title: "Select EE Global Variable to Set", options: theList2, submitOnChange:true, width:6
                    if(setGVname) {
                        input "setGVvalue", "text", title: "Value", required:false, submitOnChange:true, width:6
                    }
                    paragraph "<hr>"
                    theCogActions += "<b>-</b> Set EE Global Variable: ${setGVname} - To: ${setGVvalue}<br>"
                } else {
                    paragraph "<b>In order to use the EE Global Variables, please be sure to do the following</b><br>- Setup at least one EE Global Variable in the parent app.<br>- This Cog needs to be saved first. Please scroll down and hit 'Done' before continuing. Then open the Cog again.</b>"
                }
            } else {
                app.removeSetting("setGVname")
                app.removeSetting("setGVvalue")
            }
          
            if(actionType.contains("aSwitchesColorChange")){
                paragraph "<b>Switches to Color Change</b>"
                paragraph "Used to change colors between 1 minute and 3 hours"
                input "switchesToChange", "capability.colorControl", title: "Select Color Changing Switches", required:false, multiple:true, submitOnChange:true
                input "changeTime", "number", title: "Enter the delay between change in minutes (range 1 to 180)", required:true, defaultValue:60, range:'1..180'
                input "cycleHow", "enum", title: "Cycle each light individually or all together", defaultValue:"individual", options: ["individual","combined"], required:true, multiple:false, submitOnChange:true
                input "pattern", "enum", title: "Cycle or Randomize each color", defaultValue: "randomize", options: ["randomize","cycle"], required:true, multiple:false, submitOnChange:true
                useCustomColorsHandler()
                input "colorSelection", "enum", title: "Choose your colors", options: theColors, required:true, multiple:true, submitOnChange:true
                input "lightLevel", "number", title: "Lighting Level (1 to 99)", required:true, multiple:false, defaultValue: 99, range: '1..99', submitOnChange:true
                paragraph "<hr>"
                theCogActions +=  "<b>-</b> Switches To Color Change: ${switchesToChange}: changeTime: ${changeTime} - cycleHow: ${cycleHow} - pattern: ${pattern} - colorSelection: ${colorSelection} - lightLevel: ${lightLevel}<br>"
                
            } else {
                app.removeSetting("switchesToChange")
                app.removeSetting("changeTime")
                app.removeSetting("cycleHow")
                app.removeSetting("pattern")
                app.removeSetting("colorSelection")
                app.removeSetting("lightLevel")
            }
                        
            if(actionType.contains("aSwitchesToSync")) {
                paragraph "<b>Switches to Sync</b>"
                paragraph " - Works with on/off, level, hue, saturation and colorTemperature Attributes.<br> - Each attribute can only change once every 3 seconds<br> - ie. If one switch turns on, all switches will turn on"
                input "switchesToSync", "capability.switch", title: "Switches to Sync", multiple:true, submitOnChange:true
                if(switchEvent && !switchConditionOnly) {
                    paragraph "Note: When using this option, any Switches selected in the 'By Switch' Condition section above, will not be used to trigger this Cog. Only the switches selected here (Switches to Sync) will trigger this Cog. All other conditions will still be used."
                }
                paragraph "<hr>"
                if(switchesToSync) theCogActions +=  "<b>-</b> Switches To Sync: ${switchesToSync}<br>"
            } else {
                app.removeSetting("switchesToSync")
            }
                       
            if(actionType.contains("aSwitch")) {
                paragraph "<b>Switch Devices</b>"
                input "switchesOnAction", "capability.switch", title: "Switches to turn On", multiple:true, submitOnChange:true
                input "switchesOffAction", "capability.switch", title: "Switches to turn Off<br><small>Can also be used as Permanent Dim</small>", multiple:true, submitOnChange:true
                if(switchesOnAction) theCogActions += "<b>-</b> Switches to turn On: ${switchesOnAction}<br>"
                if(switchesOffAction) theCogActions += "<b>-</b> Switches to turn Off: ${switchesOffAction}<br>"
                if(switchesOffAction){
                    input "permanentDim2", "bool", title: "Use Permanent Dim instead of Off <small><abbr title='If a light has been turned on, Reversing it will turn it off. But with the Permanent Dim option, the light can be Dimmed to a set level and/or color instead!'><b>- INFO -</b></abbr></small>", submitOnChange:true
                    if(permanentDim2) {
                        input "permanentDimLvl2", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                        input "pdColorTemp2", "bool", title: "Use Color (off) or Temperature (on)", submitOnChange:true
                        if(pdColorTemp2) {
                            input "pdTemp2", "number", title: "Color Temperature", submitOnChange:true
                            app.removeSetting("pdColor2")
                            app.removeSetting("useCustomColors")
                        } else {
                            useCustomColorsHandler()
                            input "pdColor2", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: theColors, submitOnChange:true
                            app.removeSetting("pdTemp2")
                        }
                    } else {
                        app.removeSetting("permanentDimLvl2")
                        app.removeSetting("pdColorTemp2")
                        app.removeSetting("pdColor2")
                        app.removeSetting("pdTemp2")
                    }
                    if(permanentDim2) theCogActions += "<b>-</b> Use Permanent Dim instead of Off: ${permanentDim2} - Level: ${permanentDimLvl2} - color: ${pdColor2} - Temp: ${pdTemp2}<br>"
                } else {
                    app.removeSetting("permanentDimLvl2")
                    app.removeSetting("pdColor2")
                    app.removeSetting("pdTemp2")
                    app.removeSetting("permanentDim2")
                }
                
                input "switchesToggleAction", "capability.switch", title: "Switches to Toggle", multiple:true, submitOnChange:true
                if(switchesToggleAction) {
                    theCogActions += "<b>-</b> Switches to Toggle: ${switchesToggleAction}<br>"
                }
                paragraph "<hr>"
                input "setOnLC", "capability.switchLevel", title: "Dimmer to set", required:false, multiple:true, submitOnChange:true
                if(setOnLC) {
                    input "levelLC", "number", title: "On Level (1 to 99)", required:false, multiple:false, defaultValue: 99, range: '1..99'
                    input "lcColorTemp", "bool", title: "Use Color (off) or Temperature (on)", submitOnChange:true
                    if(lcColorTemp) {
                        input "tempLC", "number", title: "Color Temperature", submitOnChange:true
                        app.removeSetting("colorLC")
                        app.removeSetting("useCustomColors")
                    } else {
                        useCustomColorsHandler()
                        input "colorLC", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: theColors, submitOnChange:true
                        app.removeSetting("tempLC")
                    }
                    theCogActions += "<b>-</b> Dimmers to Set: ${setOnLC} - On Level: ${levelLC} - Color: ${colorLC} - Temp: ${tempLC}<br>"   
                } else {
                    app.removeSetting("setOnLC")
                    app.removeSetting("levelLC")
                    app.removeSetting("colorLC")
                    app.removeSetting("lcColorTemp")
                }
                paragraph "<hr>"
                input "switchedDimUpAction", "bool", title: "Slowly Dim Lighting UP", description: "Dim Up", submitOnChange:true, width:6
                input "switchedDimDnAction", "bool", title: "Slowly Dim Lighting DOWN", description: "Dim Down", submitOnChange:true, width:6

                if(switchedDimUpAction) {
                    paragraph "<hr>"
                    paragraph "<b>Slowly Dim Lighting UP</b>"
                    input "slowDimmerUp", "capability.switchLevel", title: "Select dimmer devices to slowly rise", required:true, multiple:true
                    input "minutesUp", "number", title: "Takes how many minutes to raise (1 to 60)", required:true, multiple:false, defaultValue:15, range: '1..60'
                    input "startLevelUp", "number", title: "Starting Level (5 to 99)", required:true, multiple:false, defaultValue: 5, range: '5..99'
                    input "targetLevelHigh", "number", title: "Target Level (5 to 99)", required:true, multiple:false, defaultValue: 99, range: '5..99'
                    useCustomColorsHandler()
                    input "colorUp", "enum", title: "Color", required:true, multiple:false, options: theColors
                    paragraph "Slowly raising a light level is a great way to wake up in the morning. If you want everything to delay happening until the light reaches its target level, turn this switch on."
                    input "targetDelay", "bool", title: "Delay Until Finished", description: "Target Delay", submitOnChange:true
                    theCogActions += "<b>-</b> Select dimmer devices to slowly rise: ${slowDimmerUp} - Minutes: ${minutesUp} - Starting Level: ${startLevelUp} - Target Level: ${targetLevelHigh} - Color: ${colorUp}<br>"
                } else {
                    app.removeSetting("slowDimmerUp")
                    app.removeSetting("minutesUp")
                    app.removeSetting("startLevelUp")
                    app.removeSetting("targetLevelHigh")
                    app.removeSetting("colorUp")
                    app.updateSetting("targetDelay",[value:"false",type:"bool"])
                }

                if(switchedDimDnAction) {
                    paragraph "<hr>"
                    paragraph "<b>Slowly Dim Lighting DOWN</b>"
                    input "slowDimmerDn", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required:true, multiple:true
                    input "minutesDn", "number", title: "Takes how many minutes to dim (1 to 60)", required:true, multiple:false, defaultValue:15, range: '1..60'
                    input "useMaxLevel", "bool", title: "Use a set starting level for all lights (off) or dim from the current level of each light (on)", submitOnChange:true
                    if(useMaxLevel) {
                        paragraph "The highest level light will start the process of dimming, each light will join in as the dim level reaches the lights current value"
                        app.removeSetting("startLevelLow")
                    } else {
                        input "startLevelLow", "number", title: "Starting Level (5 to 99)", required:true, multiple:false, defaultValue: 99, range: '5..99'
                    }
                    input "targetLevelLow", "number", title: "Target Level (5 to 99)", required:true, multiple:false, defaultValue: 5, range: '5..99'
                    input "dimDnOff", "bool", title: "Turn dimmer off after target is reached", description: "Dim Off Options", submitOnChange:true
                    input "turnOnBeforeDim", "bool", defaultVAlue:false, title: "Turn dimmer On (if it's off) before dimming", submitOnChange:true
                    useCustomColorsHandler()
                    input "colorDn", "enum", title: "Color", required:true, multiple:false, options: theColors
                    theCogActions += "<b>-</b> Select dimmer devices to slowly dim: ${slowDimmerDn} - Minutes: ${minutesDn} - useMaxLevel: ${useMaxLevel} - Starting Level: ${startLevelLow} - Target Level: ${targetLevelLow} - Dim to Off: ${dimDnOff} - Turn On: ${turnOnBeforeDim} - Color: ${colorDn}<br>"
                } else {
                    app.removeSetting("slowDimmerDn")
                    app.removeSetting("minutesDn")
                    app.removeSetting("startLevelLow")
                    app.removeSetting("targetLevelLow")
                    app.removeSetting("dimDnOff")
                    app.removeSetting("colorDn")
                    app.removeSetting("useMaxLevel")
                    app.removeSetting("dimDnOff")
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchesOnAction")
                app.removeSetting("switchesOffAction")
                app.removeSetting("switchesToggleAction")
                app.removeSetting("switchedDimUpAction")
                app.removeSetting("switchedDimDnAction")
                app.removeSetting("lcColorTemp")
            }
          
            if(actionType.contains("aSwitchSequence")) {
                paragraph "<b>Switches In Sequence</b>"
                paragraph "Sometimes you need things to turn on/off in a specific order. This section will do just that. Great for entertainment systems!"
                input "deviceSeqAction1", "capability.switch", title: "Switches to Command - 1", multiple:true, submitOnChange:true
                if(deviceSeqAction1) {
                    input "dsaCommand1", "enum", title: "Command to Send to all Switches - 1", options: ["on","off"], defaultValue: "on", submitOnChange:true
                }
                input "deviceSeqAction2", "capability.switch", title: "Switches to Command - 2", multiple:true, submitOnChange:true
                if(deviceSeqAction2) {
                    input "dsaCommand2", "enum", title: "Command to Send to all Switches - 2", options: ["on","off"], defaultValue: "on", submitOnChange:true
                }
                input "deviceSeqAction3", "capability.switch", title: "Switches to Command - 3", multiple:true, submitOnChange:true
                if(deviceSeqAction3) {
                    input "dsaCommand3", "enum", title: "Command to Send to all Switches - 3", options: ["on","off"], defaultValue: "on", submitOnChange:true
                }
                input "deviceSeqAction4", "capability.switch", title: "Switches to Command - 4", multiple:true, submitOnChange:true
                if(deviceSeqAction4) {
                    input "dsaCommand4", "enum", title: "Command to Send to all Switches - 4", options: ["on","off"], defaultValue: "on", submitOnChange:true
                }
                input "deviceSeqAction5", "capability.switch", title: "Switches to Command - 5", multiple:true, submitOnChange:true
                if(deviceSeqAction5) {
                    input "dsaCommand5", "enum", title: "Command to Send to all Switches - 5", options: ["on","off"], defaultValue: "on", submitOnChange:true
                }
                theCogActions += "<b>-</b> Switches to turn On in order: ${deviceSeqAction1} (${dsaCommand1}) - ${deviceSeqAction2} (${dsaCommand2}) - ${deviceSeqAction3} (${dsaCommand3}) - ${deviceSeqAction4} (${dsaCommand4}) - ${deviceSeqAction5} (${dsaCommand5})<br>"
                paragraph "<small>* Note: If Reverse Action is selected below, the switches selected here will turn on/off in reverse order. ie. 5,4,3,2,1</small>"
                paragraph "<hr>"
            } else {
                app.removeSetting("deviceSeqAction1")
                app.removeSetting("deviceSeqAction2")
                app.removeSetting("deviceSeqAction3")
                app.removeSetting("deviceSeqAction4")
                app.removeSetting("deviceSeqAction5")
                app.removeSetting("dsaCommand1")
                app.removeSetting("dsaCommand2")
                app.removeSetting("dsaCommand3")
                app.removeSetting("dsaCommand4")
                app.removeSetting("dsaCommand5")
            }
                
// ***** Start Switches per Mode *****   
            if(actionType.contains("aSwitchesPerMode")) {
                paragraph "<b>Switches Per Mode</b>"
                input "masterDimmersPerMode", "capability.switchLevel", title: "Master List of Dimmers Needed in this Cog <small><abbr title='Only devices selected here can be used below. This can be edited at anytime.'><b>- INFO -</b></abbr></small>", required:false, multiple:true, submitOnChange:true
                masterList = masterDimmersPerMode.toString().replace("[","").replace("]","").split(",")
                paragraph "- <b>To add or edit</b>, fill in the Mode, Device and Values below. Then press the Add/Edit button<br>- <b>To delete a variable</b>, fill in the Mode. Then press the Delete button.<br><small>* Remember to click outside all fields before pressing a button.</small>"
                if(state.working) {
                    paragraph "Working on <b>$sdPerModeName</b>"
                } else {
                    input "sdPerModeName", "mode", title: "Mode", required:false, width:6, submitOnChange:true
                }

                if(sdPerModeName && state.sdPerModeMap && !state.working) {
                    state.found = false
                    app.removeSetting("setDimmersPerMode")
                    app.removeSetting("sdPerModeLevel")
                    app.removeSetting("sdPerModeColorTemp")
                    app.removeSetting("sdPerModeColor")
                    app.removeSetting("sdTimePerMode")
                    app.removeSetting("sdReverseTimeType")
                    app.removeSetting("sdPerModeTime")
                    theMap = state.sdPerModeMap.toString().replace("[","").replace("]","")
                    theData = theMap.split(",")
                    theData.each { data ->
                        def pieces = data.split(":")
                        theMode = pieces[0]
                        if(sdPerModeName.toString() == theMode.toString()) {
                            state.working = true
                            theMode = pieces[0]; theDevices = pieces[1]; theLevel = pieces[2]; theTempType = pieces[3]; theTemp = pieces[4]; theColor = pieces[5]; theTime = pieces[6]; theTimeType = pieces[7]
                            app.updateSetting("sdPerModeLevel", theLevel)
                            app.updateSetting("sdPerModeColorTemp", [value:"${theTempType}",type:"bool"])
                            app.updateSetting("sdPerModeTemp", theTemp)
                            app.updateSetting("sdPerModeColor", theColor)
                            if(theTime) { app.updateSetting("sdReverseTimeType",[value:"true",type:"bool"]) }
                            app.updateSetting("sdTimePerMode", theTime) 
                            app.updateSetting("sdReverseTimeType",[value:"${theTimeType}",type:"bool"])                            
                            state.found = true
                        }
                    }
                    if(!state.found) { state.working = true }
                }
                input "setDimmersPerMode", "enum", title: "Dimmers to set for this Mode", required:false, multiple:true, options:masterList, submitOnChange:true
                input "sdPerModeLevel", "number", title: "On Level (1 to 99 - Leave blank to keep the Current Level)", required:false, multiple:false, range: '1..99', submitOnChange:true
                input "sdPerModeColorTemp", "bool", title: "Use Color (off) or Temperature (on)", submitOnChange:true
                if(sdPerModeColorTemp) {
                    input "sdPerModeTemp", "number", title: "Color Temperature", submitOnChange:true
                    app.removeSetting("sdPerModeColor")
                    app.removeSetting("useCustomColors")
                } else {
                    useCustomColorsHandler()
                    input "sdPerModeColor", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: theColors, submitOnChange:true
                    app.removeSetting("sdPerModeTemp")
                }
                input "sdTimePerMode", "bool", title: "Use Time to Reverse Per Mode <small><abbr title='Switches and Virtual Contact Sensor can also be Reversed! More info below in the Reverse Feature section.'><b>- INFO -</b></abbr></small>", submitOnChange:true
                if(sdTimePerMode) {
                    app.removeSetting("timeToReverse")
                    input "sdReverseTimeType", "bool", title: "Use Minutes (off) or Seconds (on)"
                    if(sdReverseTimeType) {
                        input "sdPerModeTime", "number", title: "Time to Reverse (in seconds)", submitOnChange:true
                    } else {
                        input "sdPerModeTime", "number", title: "Time to Reverse (in minutes)", submitOnChange:true
                    }
                    paragraph "<small>* For use with 'Reverse' below, this can be used to set a different 'Time to Reverse' per mode.</small>"
                } else {
                    app.removeSetting("sdPerModeTime")
                    app.removeSetting("sdReverseTimeType")
                }
// *** Start Mode Map ***
                input "sdPerModeCancel", "button", title: "Cancel", width: 3
                input "sdPerModeAdd", "button", title: "Add/Edit Mode", width: 3
                input "sdPerModeDel", "button", title: "Delete Mode", width: 3
                input "sdPerModeClear", "button", title: "Clear Table <small><abbr title='This will delete all Modes, use with caution. This can not be undone.'><b>- INFO -</b></abbr></small>", width: 3
                //input "refreshMap", "bool", title: "Refresh the Map", description: "Map", submitOnChange:true, width:3               
                input "sdPerModeRebuild", "button", title: "Rebuild Table <small><abbr title='This should only be needed when changes to the table are made by the developer.'><b>- INFO -</b></abbr></small>", width: 3
                if(refreshMap) {
                    app.removeSetting("setDimmersPerMode")
                    app.removeSetting("sdPerModeName")
                    app.removeSetting("sdPerModeLevel")
                    app.removeSetting("sdPerModeTemp")
                    app.removeSetting("sdPerModeColor")
                    app.removeSetting("sdPerModeTime")
                    app.removeSetting("sdPerModeTimeType")
                    app.updateSetting("sdPerModeColorTemp",[value:"false",type:"bool"])
                    app.updateSetting("sdTimePerMode",[value:"false",type:"bool"])
                    app.updateSetting("sdReverseTimeType",[value:"false",type:"bool"])                  
                    app.updateSetting("refreshMap",[value:"false",type:"bool"])
                }
                paragraph "<small>* Remember to click outside all fields before pressing a button. Also, most of the time the button needs to be pushed twiced to add/edit. Need to work on that!</small>"
                paragraph "<hr>"
                if(state.thePerModeMap == null) {
                    theMap = "No devices are setup"
                } else {
                    theMap = state.thePerModeMap
                }
                paragraph "${theMap}"
                // *** End Mode Map ***
                paragraph "<hr>"

                theCogActions += "<b>-</b> Switches Per Mode:<br>${state.thePerModeMap}<br>"   
            } else {
                app.removeSetting("masterDimmersPerMode")
                app.removeSetting("setDimmersPerMode")
                app.removeSetting("sdPerModeName")
                app.removeSetting("sdPerModeLevel")
                app.removeSetting("sdPerModeTemp")
                app.removeSetting("sdPerModeColor")
                app.removeSetting("sdPerModeTime")
                app.removeSetting("sdPerModeTimeType")
                app.updateSetting("sdPerModeColorTemp",[value:"false",type:"bool"])
                app.updateSetting("sdTimePerMode",[value:"false",type:"bool"])
                app.updateSetting("sdReverseTimeType",[value:"false",type:"bool"])                  
                app.updateSetting("refreshMap",[value:"false",type:"bool"])
                state.sdPerModeMap = [:]
                state.thePerModeMap = null
            }
// ***** End SwitchLevel per Mode *****                 

            if(actionType.contains("aThermostat")) {
                paragraph "<b>Thermostat</b>"
                input "thermostatAction", "capability.thermostat", title: "Thermostats", multiple:true, submitOnChange:true
                if(thermostatAction) {
                    input "setThermostatFanMode", "enum", title: "Set Thermostat Fan mode", required:false, multiple:false, options: ["on", "circulate", "auto"], submitOnChange:true
                    input "setThermostatMode", "enum", title: "Set Thermostat mode", required:false, multiple:false, options: ["auto", "off", "heat", "emergency heat", "cool"], submitOnChange:true
                    input "coolingSetpoint", "number", title: "Set Cooling Setpoint", required:false, multiple:false, submitOnChange:true, width:6
                    input "heatingSetpoint", "number", title: "Set Heating Setpoint", required:false, multiple:false, submitOnChange:true, width:6
                }
                paragraph "<hr>"
                if(setThermostatMode) theCogActions += "<b>-</b> Set Thermostats (${thermostatAction}) to mode: ${setThermostatMode}<br>"
                if(coolingSetpoint) theCogActions += "<b>-</b> Set Thermostats Cooling Setpoint to: ${coolingSetpoint}<br>"
                if(heatingSetpoint) theCogActions += "<b>-</b> Set Thermostats Heating Setpoint to: ${heatingSetpoint}<br>"
            } else {            
                app.removeSetting("thermostatAction")
                app.removeSetting("setThermostatMode")
            }
            
            if(actionType.contains("aValve")) {
                paragraph "<b>Valves</b>"
                input "valveClosedAction", "capability.valve", title: "Close Devices", multiple:true, submitOnChange:true
                input "valveOpenAction", "capability.valve", title: "Open Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
                theCogActions += "<b>-</b> Close Valves: ${valveClosedAction} - Open Valves: ${valveOpenAction}<br>"
            } else {
                app.removeSetting("valveClosedAction")
                app.removeSetting("valveOpenAction")
            }
            
            if(actionType.contains("aWindowBlind")) {
                paragraph "<b>Window Blinds</b>"
                input "blindAction", "capability.windowBlind", title: "Window Blinds to Adjust", multiple:true, submitOnChange:true
                input "blindCommand", "enum", title: "Window Blinds Action", options: ["open","close","set position","tilt"], submitOnChange:true
                if(blindCommand == "set position") {
                    input "blindPos", "number", title: "Window Blinds Position (0..100)", range: '0..100', submitOnChange:true
                }
                if(blindCommand == "tilt") {
                    input "blindTilt", "number", title: "Window Blinds Tilt Level (0..100)", range: '0..100', submitOnChange:true
                }
                paragraph "<hr>"
                theCogActions += "<b>-</b> Window Blinds - ${blindAction} - Command: ${blindCommand} - Position: ${blindPos} - Tilt: ${blindTilt}<br>"
            } else {
                app.removeSetting("blindAction")
                app.removeSetting("blindCommand")
                app.removeSetting("blindPos")
                app.removeSetting("blindTilt")
            }
            
            if(actionType.contains("aWindowShade")) {
                paragraph "<b>Window Shades</b>"
                input "shadeAction", "capability.windowShade", title: "Window Shades to Adjust", multiple:true, submitOnChange:true
                input "shadeCommand", "enum", title: "Window Shade Action", options: ["open","close","set position"], submitOnChange:true
                if(shadeCommand == "set position") {
                    input "shadePos", "number", title: "Window Shade Position (0..100)", range: '0..100', submitOnChange:true
                }
                paragraph "<hr>"
                theCogActions += "<b>-</b> Window Shades - ${shadeAction} - Command: ${shadeCommand} - Position: ${shadePos}<br>"
            } else {
                app.removeSetting("shadeAction")
                app.removeSetting("shadeCommand")
                app.removeSetting("shadePos")
            }
            
            if(actionType.contains("aVirtualContact")) {
                paragraph "<b>Virtual Contact Sensor</b><br><small>* Can be used with Alexa Routines!</small>"
                input "contactCloseAction", "capability.contactSensor", title: "Close Sensors", multiple:true, submitOnChange:true
                input "contactOpenAction", "capability.contactSensor", title: "Open Sensors", multiple:true, submitOnChange:true
                paragraph "<hr>"
                theCogActions += "<b>-</b> Virtual Contact Sensor - Close Sensors: ${contactClosedAction} - Open Sensors: ${contactOpenAction}<br>"
            } else {
                app.removeSetting("contactClosedAction")
                app.removeSetting("contactOpenAction")
            }      
        
// Start Reverse Options
            tdType = false
            if(timeDaysType) {
                if(timeDaysType.contains("tSunsetSunrise") || timeDaysType.contains("tBetween")) {
                    tdType = true
                }
            }
            if(fanAction || switchesOnAction || switchesOffAction || deviceSeqAction1 || setOnLC || contactOpenAction || masterDimmersPerMode || lzw45Action || biControl == "Camera_Enable" || biControl == "Camera_Disable" || switchesToChange) {
                if(contactEvent || garagedoorEvent || xhttpCommand || lockEvent || motionEvent || presenceEvent || switchEvent || thermoEvent || waterEvent || lzw45Command || tdType || biControl || modeEvent) {
                    paragraph "<b>Reverse</b> <small><abbr title='Description and examples can be found at the top of Cog, in Instructions.'><b>- INFO -</b></abbr></small>" 
                    input "trueReverse", "bool", title: "Reverse to Previous State (off) or Use True Reverse (on) <small><abbr title='- PREVIOUS STATE - Each time the Cog is activated, it stores the State of each device and then restores each device to its previous state when reversed. - TRUE REVERSE - If cog turns a device on, it will turn it off on reverse. Regardless of its previous state.'><b>- INFO -</b></abbr></small>", submitOnChange:true
                    
                    input "reverseTrue", "bool", title: "Only reverse when Cog changes from true to false <small><abbr title='- If one trigger is true but another is false, using reverse can lead to things happening that you do not want. With this option, the cog must have been true before reversing actions.'><b>- INFO -</b></abbr></small>", submitOnChange:true
                    
                    paragraph "<small><b>Please only select ONE Reverse Action option below</b></small>"
                    input "reverse", "bool", title: "Reverse actions when conditions are no longer true (immediately)", submitOnChange:true
                    input "reverseWithDelay", "bool", title: "Reverse actions when conditions are no longer true (with delay)", submitOnChange:true
                    if(reverseWithDelay) {
                        paragraph "<hr>"
                        input "dimWhileDelayed", "bool", title: "Dim lights DURING delay as a warning", submitOnChange:true
                        input "dimAfterDelayed", "bool", title: "Dim lights AFTER delay as a warning", submitOnChange:true
                        if(dimWhileDelayed || dimAfterDelayed) {
                            input "warningDimSec", "number", title: "Length of Dim (in Seconds - 1 to 60) <small><abbr title='Be sure this is less than Time To Reverse or it will be cut short.'><b>- INFO -</b></abbr></small>", range: '1..60', width:6
                            input "warningDimLvl", "number", title: "Warning Dim Level (1 to 99)", range: '1..99', width:6
                            paragraph "<small>* This level will override the Permanent Dim option below.</small>"
                        }
                        paragraph "<hr>"
                    } else {
                        app.removeSetting("warningDimSec")
                        app.removeSetting("warningDimLvl")
                    }
                    input "timeReverse", "bool", title: "Reverse actions after a set number of minutes (even if Conditions are still true)", submitOnChange:true
                    if(timeReverse) {
                        input "timeReverseMinutes", "number", title: "Time to Reverse (in minutes)", submitOnChange:true
                    }
                    if(reverseWithDelay) {
                        paragraph "<hr>"
                        if(sdTimePerMode) {
                            paragraph "Using Time to Reverse Per Mode."
                        } else {
                            input "reverseTimeType", "bool", title: "Use Minutes (off) or Seconds (on)", submitOnChange:true
                            if(reverseTimeType) {
                                input "timeToReverse", "number", title: "Time to Reverse (in seconds)", submitOnChange:true
                            } else {
                                input "timeToReverse", "number", title: "Time to Reverse (in minutes)", submitOnChange:true
                            }
                        }
                    }
                    if(!timeReverse) {
                        app.removeSetting("timeReverseMinutes")
                    }
                    if(!reverseWithDelay) {
                        app.removeSetting("timeToReverse")
                    }
                    app.updateSetting("reverseWhenHigh",[value:"false",type:"bool"])
                    app.updateSetting("reverseWhenLow",[value:"false",type:"bool"])
                    app.updateSetting("reverseWhenBetween",[value:"false",type:"bool"])
                } else if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || (customEvent && deviceORsetpoint)) {
                    paragraph "<small><b>Please only select ONE Reverse option</b></small>"
                    input "reverseWhenHigh", "bool", title: "Reverse actions when conditions are no longer true - Setpoint is High", submitOnChange:true
                    input "reverseWhenLow", "bool", title: "Reverse actions when conditions are no longer true - Setpoint is Low", submitOnChange:true
                    if(setTEPointBetween) input "reverseWhenBetween", "bool", title: "Reverse actions when conditions are no longer true - Setpoint is Not Between", submitOnChange:true
                    paragraph "<hr>"
                    app.updateSetting("reverse",[value:"false",type:"bool"])
                    app.updateSetting("reverseWithDelay",[value:"false",type:"bool"])
                    app.updateSetting("dimWhileDelayed",[value:"false",type:"bool"])
                    app.removeSetting("warningDimLvl")
                    app.updateSetting("timeReverse",[value:"false",type:"bool"])
                    app.removeSetting("timeToReverse")
                    app.removeSetting("timeReverseMinutes") 
                } else {
                    app.updateSetting("reverseWhenHigh",[value:"false",type:"bool"])
                    app.updateSetting("reverseWhenLow",[value:"false",type:"bool"])
                    app.updateSetting("reverseWhenBetween",[value:"false",type:"bool"])              
 
                    app.updateSetting("reverse",[value:"false",type:"bool"])
                    app.updateSetting("reverseWithDelay",[value:"false",type:"bool"])
                    app.updateSetting("dimWhileDelayed",[value:"false",type:"bool"])
                    app.removeSetting("warningDimLvl")
                    app.updateSetting("timeReverse",[value:"false",type:"bool"])
                    app.removeSetting("timeToReverse")
                    app.removeSetting("timeReverseMinutes")
                }
                // ***** Start Reverse Stuff *****
                if(trueReverse) {
                    theCogActions += "<b>-</b> True Reverse: ${trueReverse}<br>"
                }
                if(reverseTrue) {
                    theCogActions += "<b>-</b> Reverse only when Cog goes from true to false: ${reverseTrue}<br>"
                }
                if(reverse) { 
                    theCogActions += "<b>-</b> Reverse: ${reverse}<br>" 
                }
                if(timeReverse) {
                    theCogActions += "<b>-</b> Reverse: ${timeReverseMinutes} minute(s), even if Conditions are still true<br>"
                }       
                if(reverseWithDelay) {
                    if(reverseTimeType) {
                        theCogActions += "<b>-</b> Reverse: ${timeToReverse} second(s), after Conditions become false - Dim While Delayed: ${dimWhileDelayed} - Dim After Delayed: ${dimAfterDelayed} - Dim Length: ${warningDimSec} - Dim Level: ${warningDimLvl}<br>"
                    } else {
                        theCogActions += "<b>-</b> Reverse: ${timeToReverse} minute(s), after Conditions become false - Dim While Delayed: ${dimWhileDelayed} - Dim After Delayed: ${dimAfterDelayed} - Dim Length: ${warningDimSec} - Dim Level: ${warningDimLvl}<br>"
                    }
                }
                if(reverseWhenHigh || reverseWhenLow || reverseWhenBetween) {
                    theCogActions += "<b>-</b> Reverse High: ${reverseWhenHigh} - Reverse Low: ${reverseWhenLow} - Reverse Not Between: ${reverseWhenBetween}<br>"
                }
                if((reverse || reverseWithDelay || reverseWhenHigh || reverseWhenLow || reverseWhenBetween) && (switchesOnAction || setOnLC || masterDimmersPerMode)){
                    paragraph "<hr>"
                    input "permanentDim", "bool", title: "Use Permanent Dim instead of Off <small><abbr title='If a light has been turned on, Reversing it will turn it off. But with the Permanent Dim option, the light can be Dimmed to a set level and/or color instead!'><b>- INFO -</b></abbr></small>", submitOnChange:true
                    if(permanentDim) {
                        input "permanentDimLvl", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                        input "pdColorTemp", "bool", title: "Use Color (off) or Temperature (on)", submitOnChange:true
                        if(pdColorTemp) {
                            input "pdTemp", "number", title: "Color Temperature", submitOnChange:true
                            app.removeSetting("pdColor")
                            app.removeSetting("useCustomColors")
                        } else {
                            useCustomColorsHandler()
                            input "pdColor", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: theColors, submitOnChange:true
                            app.removeSetting("pdTemp")
                        }
                    } else {
                        app.removeSetting("permanentDimLvl")
                        app.updateSetting("pdColorTemp",[value:"false",type:"bool"])
                        app.removeSetting("pdColor")
                        app.removeSetting("pdTemp")
                    }
                    paragraph "<hr>"
                    paragraph "<b>Additional Switches To Turn Off on Reverse</b>"
                    input "additionalSwitches", "capability.switch", title: "Additional Switches to turn Off", multiple:true, submitOnChange:true
                    if(permanentDim) theCogActions += "<b>-</b> Use Permanent Dim: ${permanentDim} - PD Level: ${permanentDimLvl} - PD Color: ${pdColor} - Temp: ${pdTemp}<br>"
                    if(additionalSwitches) theCogActions += "<b>-</b> Addtional Switches to Turn Off: ${additionalSwitches}<br>"
                } else {
                    app.removeSetting("permanentDimLvl")
                    app.removeSetting("pdColor")
                    app.updateSetting("permanentDim",[value:"false",type:"bool"])
                    app.updateSetting("pdColorTemp",[value:"false",type:"bool"])
                    app.removeSetting("additionalSwitches")
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("timeToReverse")
                app.removeSetting("timeReverse")
                app.removeSetting("timeReverseMinutes")
                app.removeSetting("permanentDimLvl")
                app.removeSetting("pdColor")              
                app.removeSetting("pdColorTemp")
                app.removeSetting("permanentDim")
                app.removeSetting("reverse")
                app.removeSetting("reverseWithDelay")
                app.removeSetting("warningDimSec")
                app.removeSetting("warningDimLvl")
                app.removeSetting("additionSwitches")
                app.removeSetting("reverseTimeType")
            }
            paragraph "<b>Special Action Option</b><br>Sometimes devices can miss commands due to HE's speed. This option will allow you to adjust the time between commands being sent."
            actionDelayValue = parent.pActionDelay ?: 100
            input "actionDelay", "number", title: "Delay (in milliseconds - 1000 = 1 second, 3 sec max)", range: '1..3000', defaultValue:actionDelayValue, required:false, submitOnChange:true
            theCogActions += "<b>-</b> Delay Between Actions: ${actionDelay}<br>"
        }                
        // ********** End Actions **********

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", submitOnChange:true
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
            input "longDescription", "textarea", title: "Cog Description (optional)", submitOnChange:true
            input "otherNotes", "textarea", title: "Other Notes (optional)", submitOnChange:true
            input "runAtStartup", "bool", title: "Run on System Startup <small><abbr title='If the system is rebooted for any reason, cog will run once the system comes back up.'><b>- INFO -</b></abbr></small>", submitOnChange:true
            if(runAtStartup) {
                theCogActions += "<br><b>*</b> Cog is set to run on system startup<br>"
            }
            input "runNow", "bool", title: "Run Cog when Saving", description: "Run Now", submitOnChange:true
            input "logOptions", "bool", title: "Enable Debug Options", description: "Log Options", submitOnChange:true
            if(logOptions) {
                input "logEnable", "bool", title: "Enable Debug Logging - THIS is the option you want to turn on, most of the time.", description: "Debug Log", submitOnChange:true
                input "shortLog", "bool", title: "Short Logs - Please only post short logs if the Developer asks for it", description: "log size", submitOnChange:true
                input "extraLogs", "bool", title: "Use Extra Logs  - Please only Use Extra logs if the Developer asks for it", description: "Extra Logs", submitOnChange:true
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
                paragraph "<hr>"
            } else {
                app.updateSetting("logEnable",[value:"false",type:"bool"])
                app.updateSetting("shortLog",[value:"false",type:"bool"])
                app.updateSetting("extraLogs",[value:"false",type:"bool"])
                app.updateSetting("clearMaps",[value:"false",type:"bool"])
                app.removeSetting("logOffTime")
            }
            input "testEnable", "bool", title: "Enable Testing Options", description: "Debug Testing", submitOnChange:true
            if(testEnable) {
                paragraph "Note: All of the debug options below are made just for me to test things. But, you may find some of them useful too. Just remember to not complain/post/ask questions about them. They are for testing only and may or may not work at any given time."
                input "clearMaps", "bool", title: "Clear state.oldMaps", description: "clear", width:6, submitOnChange:true
                if(clearMaps) {
                    state.oldMap = [:]
                    state.running = "Stopped"
                    state.tryRunning = 0
                    app.updateSetting("clearMaps",[value:"false",type:"bool"])
                }
                input "clearStates", "bool", title: "Clear All States", description: "clear", width:6, submitOnChange:true
                if(clearStates) {
                    state.clear()
                    state.running = "Stopped"
                    state.tryRunning = 0
                    app.updateSetting("clearStates",[value:"false",type:"bool"])
                }
            }
        }        
        section(getFormat("header-green", "${getImage("Blank")}"+" The Cog Description")) {
            paragraph "This will give a break down on how the Cog will operate. This is also an easy way to share how to do things. Just copy the text below and post it on the HE forums!"
            paragraph "<hr>"
            paragraph "<b>Event Engine Cog (${state.version}) - ${app.label} (${app.id})</b>"
            if(longDescription) paragraph "<b>Description:</b> ${longDescription}<br>"
            if(theCogTriggers) paragraph theCogTriggers.replaceAll("null","NA")
            if(theCogActions) paragraph theCogActions.replaceAll("null","NA")
            if(state.theCogNotifications) paragraph state.theCogNotifications.replaceAll("null","NA")
            if(otherNotes) {
                paragraph "<hr>"
                paragraph "<b>Other Notes:</b> ${otherNotes}<br>"
            }
            paragraph "<hr>"
            paragraph "<small>* If you're not seeing your Notification settings, please re-visit the Notifications section.</small>"
            input "resetCog", "bool", title: "Refresh The Cog Description <small>(This will happen immediately)</small>", description: "Cog", submitOnChange:true
            if(resetCog) {
                app.updateSetting("resetCog",[value:"false",type:"bool"])
            }
        }
        display2()
    }
}

def notificationOptions(){
    dynamicPage(name: "notificationOptions", title: "Notification Options", install:false, uninstall:false){
        state.theCogNotifications = "<b><u>Notifications</u></b><br>"
        section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications. Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", submitOnChange:true
            if(useSpeech) {
                input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required:true, submitOnChange:true
                state.theCogNotifications += "<b>-</b> Use Speech: ${fmSpeaker}<br>"
            } else {
                app.removeSetting("fmSpeaker")
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple:true, required:false, submitOnChange:true
            if(sendPushMessage) {
                state.theCogNotifications += "<b>-</b> Send Push: ${sendPushMessage}<br>"
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Send Notifications to Other Devices")) {
            paragraph "Send notifications to things like a webOS Television! (anything that has the 'deviceNotification' command)<br><small>Note: webOS requires the use of <a href='https://github.com/as-j/LG_Smart_TV_hubitat' target=_blank>this code</a>.<br>* The webOS code is not my creation. I can not offer support. For any/all issues please visit <a href='https://community.hubitat.com/t/port-lg-smart-tv-discovery-2012/12761' target=_blank>this post</a>.</small>"
            input "useWebOS", "capability.notification", title: "Use Other Notifications", multiple:true, required:false, submitOnChange:true
            if(useWebOS) {
                state.theCogNotifications += "<b>-</b> Use Other Notifications: ${useWebOS}<br>"
            }
        }

        if(useSpeech || sendPushMessage || useWebOS) {
            if(useSpeech || sendPushMessage) {
                section(getFormat("header-green", "${getImage("Blank")}"+" Speaker - Priority Message Instructions")) { }
                section("Instructions for Priority Message:", hideable:true, hidden:true) {
                    paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place the option bracket in front of any message to be spoken and the Volume, Voice and/or Speaker will be adjusted accordingly."
                    paragraph "Format: [priority:sound:speaker]<br><small>Note: Any option not needed, replace with a 0 (zero).</small>"
                    paragraph "<b>Priority:</b><br>This can change the voice used and the color of the message displayed on the Dashboard Cog.<br>[F:0:0] - Fun<br>[R:0:0] - Random<br>[L:0:0] - Low<br>[N:0:0] - Normal<br>[H:0:0] - High"
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

                section(getFormat("header-green", "${getImage("Blank")}"+" Speaker - Message Options")) {
                    wc =  "%whoHappened% - Device that caused the event to trigger<br>"
                    wc += "%whatHappened% - Device status that caused the event to trigger<br>"
                    wc += "%time% - Will speak the current time in 24 h<br>"
                    wc += "%time1% - Will speak the current time in 12 h<br>"
                    wc += "%setPointHigh% - If using a setpoint, this will speak the actual High Setpoint<br>"
                    wc += "%setPointLow% - If using a setpoint, this will speak the actual Low Setpoint<br>"
                    if(theType1) wc += "%lastDirection% - Will speak the last direction reported<br>" 
                    if(lockEvent) {
                        wc += "%whoLocked% - The name of the person who locked the door<br>"
                        wc += "%whoUnlocked% - The name of the person who unlocked the door<br>"
                    }
                    if(iCalLinks) wc += "%iCalValue% - Uses the last iCal event value<br>"
                    paragraph wc
                    if(triggerType) {
                        if(triggerType.contains("xBattery") || triggerType.contains("xEnergy") || triggerType.contains("xHumidity") || triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp") || deviceORsetpoint) {
                            paragraph "<b>Setpoint Message Options</b>"
                            input "messageH", "text", title: "Message to speak when reading is too high", required:false, submitOnChange:true
                            input "messageL", "text", title: "Message to speak when reading is too low", required:false, submitOnChange:true
                            input "messageB", "text", title: "Message to speak when reading is in between", required:false, submitOnChange:true
                            if(messageH) state.theCogNotifications += "<b>-</b> Message when reading is too high: ${messageH}<br>"
                            if(messageL) state.theCogNotifications += "<b>-</b> Message when reading is too low: ${messageL}<br>"
                            if(messageB) state.theCogNotifications += "<b>-</b> Message when reading is in between: ${messageB}<br>"
                        } else {
                            app.removeSetting("messageH")
                            app.removeSetting("messageL")
                            app.removeSetting("messageB")
                        }

                        if(!triggerType.contains("xBattery") || !triggerType.contains("xEnergy") || !triggerType.contains("xHumidity") && !triggerType.contains("xIlluminance") && !triggerType.contains("xPower") && !triggerType.contains("xTemp") || !deviceORsetpoint) {
                            paragraph "<b>Random Message Options</b>"
                            input "message", "text", title: "Message to be spoken/pushed - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                            input "msgList", "bool", title: "Show a list view of the messages", description: "List View", submitOnChange:true
                            if(message) state.theCogNotifications += "<b>-</b> Message: ${message}<br>"
                            if(msgList) {
                                def values = "${message}".split(";")
                                listMap = ""
                                values.each { item -> listMap += "${item}<br>"}
                                paragraph "${listMap}"
                            }
                        } else {
                            app.removeSetting("message")
                            app.removeSetting("msgList")
                        }
                    } else {
                        paragraph "<b>Can't add a message until a Condition Type is selected.</b>"
                    }
                }

                section(getFormat("header-green", "${getImage("Blank")}"+" Repeat Notifications")) {
                    input "msgRepeat", "bool", title: "Repeat Notifications", description: "List View", submitOnChange:true
                    if(msgRepeat) {
                        input "msgRepeatMinutes", "number", title: "Repeat every XX minutes", submitOnChange:true, width:6
                        input "msgRepeatMax", "number", title: "Max number of repeats", submitOnChange:true, width:6

                        if(msgRepeatMinutes && msgRepeatMax) {
                            paragraph "Message will repeat every ${msgRepeatMinutes} minutes until one of the contacts/switches changes state <b>OR</b> the Max number of repeats is reached (${msgRepeatMax})"
                            repeatTimeSeconds = ((msgRepeatMinutes * 60) * msgRepeatMax)
                            int inputNow=repeatTimeSeconds
                            int nDayNow = inputNow / 86400
                            int nHrsNow = (inputNow % 86400 ) / 3600
                            int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
                            int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
                            paragraph "In this case, it would take ${nHrsNow} Hours, ${nMinNow} Mins and ${nSecNow} Seconds to reach the max number of repeats (if nothing changes state)"
                        }

                        input "msgRepeatContact", "capability.contactSensor", title: "Contact to turn the Repeat Off", multiple:false, submitOnChange:true
                        input "msgRepeatSwitch", "capability.switch", title: "Switch to turn the Repeat Off", multiple:false, submitOnChange:true 
                        if(msgRepeatContact) { paragraph "<small>* Contact will turn off Repeat when changing to any state.</small>" }
                        if(msgRepeatSwitch) { paragraph "<small>* Switch will turn off Repeat when changing to any state.</small>" }
                        state.theCogNotifications += "<b>-</b> msgRepeat: ${msgRepeat} - msgRepeatMinutes: ${msgRepeatMinutes} - msgRepeatContact: ${msgRepeatContact} - msgRepeatSwitch: ${msgRepeatSwitch}<br>"
                    } else {
                        app.removeSetting("msgRepeatMinutes")
                        app.removeSetting("msgRepeatContact")
                        app.removeSetting("msgRepeatSwitch")
                        app.removeSetting("msgRepeatMax")
                    }
                }
            }
            if(useWebOS) {
                section(getFormat("header-green", "${getImage("Blank")}"+" Other Device Options")) {
                    wc =  "%whoHappened% - Device that caused the event to trigger<br>"
                    wc += "%whatHappened% - Device status that caused the event to trigger<br>"
                    wc += "%time% - Will speak the current time in 24 h<br>"
                    wc += "%time1% - Will speak the current time in 12 h<br>"
                    wc += "%setPointHigh% - If using a setpoint, this will speak the actual High Setpoint<br>"
                    wc += "%setPointLow% - If using a setpoint, this will speak the actual Low Setpoint<br>"
                    if(theType1) wc += "%lastDirection% - Will speak the last direction reported<br>" 
                    if(lockEvent) wc += "%whoUnlocked% - The name of the person who unlocked the door<br>"
                    if(iCalLinks) wc += "%iCalValue% - Uses the last iCal event value<br>"
                    paragraph wc
                    if(triggerType) {
                        if(triggerType.contains("xBattery") || triggerType.contains("xEnergy") || triggerType.contains("xHumidity") || triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp") || deviceORsetpoint) {
                            paragraph "<b>Setpoint Message Options</b>"
                            input "wmessageH", "text", title: "Message to speak when reading is too high", required:false, submitOnChange:true
                            input "wmessageL", "text", title: "Message to speak when reading is too low", required:false, submitOnChange:true
                            input "wmessageB", "text", title: "Message to speak when reading is in between", required:false, submitOnChange:true
                            if(wmessageH) state.theCogNotifications += "<b>-</b> Message when reading is too high: ${wmessageH}<br>"
                            if(wmessageL) state.theCogNotifications += "<b>-</b> Message when reading is too low: ${wmessageL}<br>"
                            if(wmessageB) state.theCogNotifications += "<b>-</b> Message when reading is in between: ${wmessageB}<br>"
                        } else {
                            app.removeSetting("wmessageH")
                            app.removeSetting("wmessageL")
                            app.removeSetting("wmessageB")
                        }

                        if(!triggerType.contains("xBattery") || !triggerType.contains("xEnergy") || !triggerType.contains("xHumidity") && !triggerType.contains("xIlluminance") && !triggerType.contains("xPower") && !triggerType.contains("xTemp") || !deviceORsetpoint) {
                            paragraph "<b>Random Message Options</b>"
                            input "wmessage", "text", title: "Message to be sent - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                            input "wmsgList", "bool", title: "Show a list view of the messages", description: "List View", submitOnChange:true
                            if(wmessage) state.theCogNotifications += "<b>-</b> Message: ${wmessage}<br>"
                            if(wmsgList) {
                                def values = "${wmessage}".split(";")
                                wlistMap = ""
                                values.each { item -> wlistMap += "${item}<br>"}
                                paragraph "${wlistMap}"
                            }
                        } else {
                            app.removeSetting("wmessage")
                            app.removeSetting("wmsgList")
                        }
                    } else {
                        paragraph "<b>Can't add a webOS message until a Condition Type is selected.</b>"
                    }
                }
            }
        } else {
            app.removeSetting("message")
            app.removeSetting("messageH")
            app.removeSetting("messageL")
            app.removeSetting("messageB")
            app.removeSetting("wmessage")
            app.removeSetting("wmessageH")
            app.removeSetting("wmessageL")
            app.removeSetting("wmessageB")
            app.removeSetting("useSpeech")
            app.removeSetting("fmSpeaker")
            app.removeSetting("useWebOS")
            app.removeSetting("sendPushMessage")
            app.removeSetting("msgRepeat")
            app.removeSetting("msgRepeatMinutes")
            app.removeSetting("msgRepeatContact")
            app.removeSetting("msgRepeatSwitch")
            app.removeSetting("msgRepeatMax")
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights. Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Preset you wish to use", required:true, multiple:false
                if(useTheFlasher) state.theCogNotifications += "<b>-</b> Use The Flasher: ${useTheFlasher} - Device: ${theFlasherDevice}<br>"
            } else {
                app.removeSetting("theFlasherDevice")
            }
        }
    }
}

def installed() {
    initialize()
}

def updated() {	
    unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, "logsOff", [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
    initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.trace "***** Initialize (${state.version}) - ${app.label} *****"
        state.syncOnRunning = "no"
        state.syncOffRunning = "no"
        state.syncColorRunning = "no"
        state.syncHueRunning = "no"
        state.syncLevelRunning = "no"
        state.syncSaturationRunning = "no"
        if(accelerationConditionOnly == null) accelerationConditionOnly = false
        if(batteryConditionOnly == null) batteryConditionOnly = false
        if(buttonConditionOnly == null) buttonConditionOnly = false
        if(contactConditionOnly == null) contactConditionOnly = false
        if(energyConditionOnly == null) energyConditionOnly = false
        if(garageDoorConditionOnly == null) garageDoorConditionOnly = false
        if(humidityConditionOnly == null) humidityConditionOnly = false
        if(illumConditionOnly == null) illumConditionOnly = false
        if(lockConditionOnly == null) lockConditionOnly = false
        if(modeMatchConditionOnly == null) modeMatchConditionOnly = false
        if(motionConditionOnly == null) motionConditionOnly = false
        if(powerConditionOnly == null) powerConditionOnly = false
        if(presenceConditionOnly == null) presenceConditionOnly = false
        if(startupConditionOnly == null) startupConditionOnly = false
        if(switchConditionOnly == null) switchConditionOnly = false
        if(voltageConditionOnly == null) voltageConditionOnly = false
        if(tempConditionOnly == null) tempConditionOnly = false
        if(thermoConditionOnly == null) thermoConditionOnly = false
        
        if(startTime) schedule(startTime, "certainTime")
        if(accelerationEvent && !accelerationConditionOnly) subscribe(accelerationEvent, "accelerationSensor", startTheProcess) 
        if(batteryEvent && !batteryConditionOnly) subscribe(batteryEvent, "battery", startTheProcess)
        if(buttonEvent) {
            bAction = buttonAction.toString()
            if(bAction == "pushed") subscribe(buttonEvent, "pushed", buttonHandler)
            if(bAction == "held") subscribe(buttonEvent, "held", buttonHandler)
            if(bAction == "doubleTapped") subscribe(buttonEvent, "doubleTapped", buttonHandler)
            if(bAction == "released") subscribe(buttonEvent, "released", buttonHandler)            
            if(bAction == "taps") subscribe(buttonEvent, "taps", buttonHandler)
        }
        if(contactEvent && !contactConditionOnly) subscribe(contactEvent, "contact", startTheProcess)
        if(energyEvent && !energyConditionOnly) subscribe(energyEvent, "energy", startTheProcess)
        if(garagedoorEvent && !garageDoorConditionOnly) subscribe(garagedoorEvent, "door", startTheProcess)
        if(hsmAlertEvent) subscribe(location, "hsmAlert", startTheProcess)
        if(hsmStatusEvent) subscribe(location, "hsmStatus", startTheProcess)
        if(humidityEvent && !humidityConditionOnly) subscribe(humidityEvent, "humidity", startTheProcess)
        if(illuminanceEvent && !illumConditionOnly) subscribe(illuminanceEvent, "illuminance", startTheProcess)
        if(lockEvent && !lockConditionOnly) subscribe(lockEvent, "lock", startTheProcess)
        if(modeEvent && !modeMatchConditionOnly) subscribe(location, "mode", startTheProcess)
        if(motionEvent && !motionConditionOnly) subscribe(motionEvent, "motion", startTheProcess)
        if(powerEvent && !powerConditionOnly) subscribe(powerEvent, "power", startTheProcess)
        if(presenceEvent && !presenceConditionOnly) subscribe(presenceEvent, "presence", startTheProcess)
        if(startupEvent && !startupConditionOnly) subscribe(location, "systemStart", startTheProcess)
        if(voltageEvent && !voltageConditionOnly) subscribe(voltageEvent, "voltage", startTheProcess) 
        if(tempEvent && !tempConditionOnly) subscribe(tempEvent, "temperature", startTheProcess)
        if(thermoEvent && !thermoConditionOnly) subscribe(thermoEvent, "thermostatOperatingState", startTheProcess) 
        if(customEvent) subscribe(customEvent, specialAtt, startTheProcess)
        
        if(myContacts2) subscribe(myContacts2, "contact.closed", startTheProcess)
        if(myMotion2) subscribe(myMotion2, "motion.inactive", startTheProcess)
        if(myPresence2) subscribe(myPresence2, "presence.not present", startTheProcess)
        if(mySwitches2) subscribe(mySwitches2, "switch.off", startTheProcess)
        
        if(keypadEvent) subscribe(keypadEvent, "lastCodeName", startTheProcess)
        if(keypadEvent) subscribe(keypadEvent, "securityKeypad", startTheProcess)
        if(snDeviceEvent) subscribe(snDeviceEvent, "alarmStatus", startTheProcess)
        
        if(theVariable) {
            var="variable:$theVariable"
            subscribe(location, "$var", startTheProcess)
            getGlobalVarCurrent()
        }
        
        if(aLifxStrip) subscribe(location, "pcChildren", pcMapOfChildrenHandler)
        
        if(switchesToSync) {
            subscribe(switchesToSync, "colorTemperature", switchesToSyncColorTempHandler)
            subscribe(switchesToSync, "hue", switchesToSyncHueHandler)
            subscribe(switchesToSync, "level", switchesToSyncLevelHandler)
            subscribe(switchesToSync, "saturation", switchesToSyncSaturationHandler)
            subscribe(switchesToSync, "switch.on", switchesToSyncOnHandler)
            subscribe(switchesToSync, "switch.off", switchesToSyncOffHandler)
        } else {
            if(switchEvent && !switchConditionOnly) {
                subscribe(switchEvent, "switch", startTheProcess)
            }
        }
        
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

        if(triggerType) {
            if(triggerType.contains("xDirectional")) {
                if(theType1) {
                    subscribe(device1, "contact", activeOneHandler)
                } else {
                    subscribe(device1, "motion", activeOneHandler)
                }
                if(theType2) {
                    subscribe(device2, "contact", activeTwoHandler)
                } else {
                    subscribe(device2, "motion", activeTwoHandler)
                }
            }
            
            if(triggerType.contains("xTransition")) {
                if(transitionType == "Device Attribute") { subscribe(attTransitionEvent, attTransitionAtt, startTheProcess) }
                if(transitionType == "HSM Status") { subscribe(location, "hsmStatus", startTheProcess) }
                if(transitionType == "Mode") { subscribe(location, "mode", startTheProcess) }
            }
        }
        
        if(timeDaysType) {
            if(timeDaysType.contains("tPeriodic")) { 
                if(logEnable) log.debug "In initialize - tPeriodic - Creating Cron Jobs"
                if(preMadePeriodic) { schedule(preMadePeriodic, "runAtTime1") }
                if(preMadePeriodic2) { schedule(preMadePeriodic2, "runAtTime2") }
            } else if(timeDaysType.contains("tHoliday")) {
                if(logEnable) log.debug "In initialize - tHoliday - Creating Cron Jobs"
                schedule("0 2 0 ? * * *", checkForHoliday)
                checkForHoliday()
            } else if(iCalLinks && iCalSearch) {
                unschedule()
                schedule("0 2 0 ? * * *", getIcalDataHandler)
                schedule("0 5 0 ? * * *", iCalHandler)
                getIcalDataHandler()
                iCalHandler()
            }
        }
       
        if(dataDevice && ewKeyword1) {
            if(eventLog) {
                elStatus = "log"
            } else {
                elStatus = "event"
            }
            // Log doesn't work yet!
            if(elStatus == "event") {
                dataDevice.sendEvent(name: "watching", value: elStatus, isStateChange: true)
                dataDevice.keywordInfo("$ewKeyword1:$ewKeyword2:$ewKeyword3")
                subscribe(dataDevice, "bpt-lastEventMessage", startTheProcess)
                dataDevice.appStatus("active")
                dataDevice.initialize()
            }
        }

        if(ipAddress) {
            schedule("0 0/${timeToPing} * * * ?", pingHandler)
            runIn(2, "pingHandler")
        }
        
        // This will auto run app on system startup
        if(runAtStartup) { subscribe("location", "systemStart", startTheProcess) }
        
        checkSunHandler() 
        if(fromTime && toTime) {
            schedule(fromTime, "startTimeBetween")
            schedule(toTime, "endTimeBetween")
            theDate1 = toDateTime(fromTime)
            theDate2 = toDateTime(toTime)          
            toValue = theDate2.compareTo(theDate1)
            if(toValue > 0) {
                nextToDate = theDate2
            } else {
                nextToDate = theDate2.next()
            }
            state.betweenTime = timeOfDayIsBetween(theDate1, nextToDate, new Date(), location.timeZone)
        } else {
            state.betweenTime = true
        }
        if(fromTime && toTime) {
            if(logEnable) { log.debug "In initialize - betweenTime: ${state.betweenTime}" }
        }
        if(runNow) {
            app.updateSetting("runNow",[value:"false",type:"bool"])
            runIn(5, startTheProcess, [data: "runNow"])
        }
    }
}

def startTheProcess(evt) {
    setVersion()
    if(switchesToSync) {
        if(state.syncOnRunning == "yes" || state.syncOffRunning == "yes" || state.syncColorRunning == "yes" || state.syncHueRunning == "yes" || state.syncLevelRunning == "yes" || state.syncSaturationRunning == "yes") {
            if(logEnable) log.debug "In startTheProcess - Switch Sync is still Running - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus}"
        }
    } else {
        if(logEnable || shortLog) log.debug "In startTheProcess - Starting - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus}"
        if(state.running == null) state.running = "Stopped"
        if(state.tryRunning == null) state.tryRunning = 0
        checkEnableHandler()
        if(pauseApp || state.eSwitch) {
            log.info "${app.label} is Paused or Disabled"
        } else if(state.running == "Running") {
            state.tryRunning += 1
            if(state.tryRunning > 2) {
                state.tryRunning = 0
                state.running = "Stopped"
                if(logEnable || shortLog) log.debug "*** ${app.label} - Was already running, will run again next time ***"
            } else {
                if(logEnable || shortLog) log.debug "*** ${app.label} - Already running (${state.tryRunning}) ***"
            }
        } else if(state.whatToDo == "stop") {
            state.tryRunning = 0
            state.running = "Stopped"
            if(logEnable || shortLog) log.debug "*** whatToDo: ${state.whatToDo} ***"
            state.whatToDo = ""
        } else if(actionType) {
            try {
                state.running = "Running"
                state.tryRunning = 0
                if(logEnable || shortLog) log.trace "*"
                if(logEnable || shortLog) log.trace "******************** Start - startTheProcess (${state.version}) - ${app.label} - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus} ********************"
                if(actionType.contains("aSwitchesPerMode")) { app.updateSetting("modeMatchRestriction",[value:"true",type:"bool"]) }
                state.isThereDevices = false;    state.isThereSPDevices = false;    state.areRestrictions = false;    state.setpointLow = null;    state.setpointHigh = null;    state.whoText = ""
                if(startTime || preMadePeriodic) {
                    state.totalMatch = 1;    state.totalConditions = 1
                }
                if(triggerType == null) triggerType = ""
                if(evt) {
                    if(evt == "runAfterDelay") {
                        // Keeping original who and what happened
                    } else if(evt == "timeReverse" || evt == "reverse") {
                        state.whatToDo = "skipToReverse"
                    } else if(evt == "run" || evt == "runNow") {
                        state.whatToDo = "run"
                    } else {
                        try {
                            state.whoHappened = evt.displayName
                            state.whatHappened = evt.value
                            state.whoText = evt.descriptionText
                        } catch(e) {
                            //if(logEnable) log.debug "In startTheProcess - Whoops! evt: ${evt}"
                            //log.error(getExceptionMessageWithLine(e))
                        }
                        state.whoLocked = null; state.whoUnlocked = null
                        state.hasntDelayedYet = true
                        state.hasntDelayedReverseYet = true
                        state.whatToDo = "run"
                    }
                    if(logEnable || shortLog) log.debug "In startTheProcess - whoHappened: ${state.whoHappened} - whatHappened: ${state.whatHappened} - whoText: ${state.whoText}"
                } else {
                    if(logEnable || shortLog) log.debug "In startTheProcess - No EVT (evt: ${evt})"
                    state.whatToDo = "run"
                }
                if(accelerationRestrictionEvent) { accelerationHandler("restriction") }
                if(contactRestrictionEvent) { contactHandler("restriction") }
                if(garageDoorRestrictionEvent) { garageDoorHandler("restriction") }
                if(lockRestrictionEvent) { lockHandler("restriction") }
                if(motionRestrictionEvent) { motionHandler("restriction") }
                if(motionRestrictionEvent2) { motionHandler2"restriction"() }
                if(presenceRestrictionEvent) { presenceHandler("restriction") }
                if(switchRestrictionEvent) { switchHandler("restriction") }
                if(watereRestrictionEvent) { waterHandler("restriction") }
                if(state.areRestrictions) {
                    if(logEnable) log.debug "In startTheProcess - whatToDo: ${state.whatToDo} - Restrictions are true, skipping"
                    state.whatToDo = "stop"
                } else {
                    if(state.whatToDo == "stop" || state.whatToDo == "skipToReverse") {
                        if(logEnable) log.debug "In startTheProcess - Skipping Time checks - whatToDo: ${state.whatToDo}"
                    } else {
                        checkSunHandler()
                        dayOfTheWeekHandler()
                        modeHandler()
                        hsmAlertHandler(state.whatHappened)
                        hsmStatusHandler(state.whatHappened)
                        safetyNetHandler()
                        if(logEnable) log.debug "In startTheProcess - 1A - betweenTime: ${state.betweenTime} - timeBetweenSun: ${state.timeBetweenSun} - daysMatch: ${state.daysMatch} - modeMatch: ${state.modeMatch} - snMatch: ${state.snMatch}"
                        if(daysMatchRestriction && !state.daysMatch) { state.whatToDo = "stop" }
                        if(timeBetweenRestriction && !state.betweenTime) { state.whatToDo = "stop" }
                        if(timeBetweenSunRestriction && !state.timeBetweenSun) { state.whatToDo = "stop" } 
                        if(modeMatchRestriction && !state.modeMatch) { state.whatToDo = "stop" }
                        if(snRestriction && !state.snMatch) { state.whatToDo = "stop" }
                    }           
                    if(logEnable) log.debug "In startTheProcess - 1B - daysMatchRestic: ${daysMatchRestriction} - timeBetweenRestric: ${timeBetweenRestriction} - timeBetweenSunRestric: ${timeBetweenSunRestriction} - modeMatchRestric: ${modeMatchRestriction} - snRestric: ${snRestriction}"          

                    if(state.whatToDo == "stop" || state.whatToDo == "skipToReverse") {
                        if(logEnable) log.debug "In startTheProcess - Skipping Device checks - whatToDo: ${state.whatToDo}"
                    } else {
                        if(accelerationEvent) { accelerationHandler("condition") }
                        if(contactEvent) { contactHandler("condition") }
                        if(myContacts2) { contactHandler("helper") }
                        if(garageDoorEvent) { garageDoorHandler("condition") }
                        if(lockEvent) { lockHandler("condition") }
                        if(motionEvent) { motionHandler("condition") }
                        if(myMotion2) { motionHandler("helper") }
                        if(presenceEvent) { presenceHandler("condition") }
                        if(myPresence2) { presenceHandler("helper") }
                        if(switchEvent) { switchHandler("condition") }
                        if(mySwitches2) { switchHandler("helper") }
                        if(thermoEvent) { thermostatHandler("condition") }
                        if(waterEvent) { waterHandler("condition") }
                        if(batteryEvent) { batteryHandler() }
                        if(energyEvent) { energyHandler() }
                        if(humidityEvent) { humidityHandler() }
                        if(illuminanceEvent) { illuminanceHandler() }
                        if(powerEvent) { powerHandler() }
                        if(tempEvent) { tempHandler() }
                        if(voltageEvent) { voltageHandler() }
                        if(ipAddress) { 
                            // pingHandler()
                        } else {
                            if(logEnable) log.debug "In startTheProcess - NOT using IP Ping - Setting value based on triggerAndOr: ${triggerAndOr}"
                            if(triggerAndOr) {
                                state.ipStatusOK = false
                            } else {
                                state.ipStatusOK = true
                            }
                        }
                        if(theVariable) {
                            hubVariableConditions()
                        } else {
                            state.variablesOK = true
                        }
                        if(keypadEvent) { 
                            securityKeypadHandler(evt)
                        } else {
                            if(logEnable) log.debug "In startTheProcess - NOT using Security Keypad - Setting value based on triggerAndOr: ${triggerAndOr}"
                            if(triggerAndOr) {
                                state.securityOK = false
                            } else {
                                state.securityOK = true
                            }
                        }
                        if(!state.isThereSPDevices) {
                            if(logEnable) log.debug "In startTheProcess - NOT using Setpoint Devices - Setting value based on triggerAndOr: ${triggerAndOr}"
                            if(triggerAndOr) {
                                state.setpointOK = false
                            } else {
                                state.setpointOK = true
                            }
                            state.setpointHighOK = "yes"
                            state.setpointLowOK = "yes"
                            state.setpointBetweenOK = "yes"
                        }
                        if(attTransitionEvent) {
                            checkTransitionHandler()
                        } else {
                            if(logEnable) log.debug "In startTheProcess - NOT using transitions - Setting value based on triggerAndOr: ${triggerAndOr}"
                            if(triggerAndOr) {
                                state.transitionOK = false
                            } else {
                                state.transitionOK = true
                            }
                        }  
                        if(deviceORsetpoint) {
                            if(customEvent) { customSetpointHandler() }
                        } else {
                            if(customEvent) { customDeviceHandler("condition") }
                        }                
                        if(gvStyle) { 
                            if(globalVariableEvent) { globalVariablesNumberHandler() }
                        } else {
                            if(globalVariableEvent) { globalVariablesTextHandler() }
                        }               
                        if(triggerType.contains("xHubCheck")) { sendHttpHandler() }
                                             
                        checkingWhatToDo()     // Putting it all together!       
                    }
                }

                if(state.whatToDo == "stop") {
                    if(logEnable || shortLog) log.debug "In startTheProcess - Nothing to do - STOPING - whatToDo: ${state.whatToDo}"
                    state.appStatus = "inactive"
                } else {                   
                    if(state.whatToDo == "run") {
                        if(state.modeMatch && state.daysMatch && state.betweenTime && state.timeBetweenSun && state.modeMatch && state.snMatch) {
                            if(useAppStatus) {
                                if(state.appStatus == null) state.appStatus = "inactive"
                            } else {
                                state.appStatus = "inactive"
                            }
                            if(logEnable || shortLog) log.debug "In startTheProcess - RUN - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus}"
                            if(state.appStatus == "inactive") {
                                if(logEnable || shortLog) log.debug "In startTheProcess - HERE WE GO! - whatToDo: ${state.whatToDo}"
                                if(state.hasntDelayedYet == null) state.hasntDelayedYet = false
                                if((notifyDelay || randomDelay || targetDelay) && state.hasntDelayedYet) {
                                    if(notifyDelay && minSec) {
                                        theDelay = notifyDelay
                                        if(logEnable || shortLog) log.debug "In startTheProcess - Delay is set for ${notifyDelay} second(s)"
                                    } else if(notifyDelay && !minSec) {
                                        theDelay = notifyDelay * 60
                                        if(logEnable || shortLog) log.debug "In startTheProcess - Delay is set for ${notifyDelay} minute(s)"
                                    } else if(randomDelay) {
                                        newDelay = Math.abs(new Random().nextInt() % (delayHigh - delayLow)) + delayLow
                                        theDelay = newDelay * 60
                                        if(logEnable || shortLog) log.debug "In startTheProcess - Delay is set for ${newDelay} minute(s)"
                                    } else if(targetDelay) {
                                        theDelay = minutesUp * 60
                                        if(logEnable || shortLog) log.debug "In startTheProcess - Delay is set for ${minutesUp} minute(s)"
                                    } else {
                                        if(logEnable) log.warn "In startTheProcess - Something went wrong"
                                    }
                                    if(actionType) {
                                        if(actionType.contains("aSwitch") && switchedDimUpAction) { slowOnHandler() }
                                    }                              
                                    state.hasntDelayedYet = false
                                    state.setpointHighOK = "yes"
                                    state.setpointLowOK = "yes"
                                    state.setpointBetweenOK = "yes"
                                    runIn(theDelay, "startTheProcess", [data: "runAfterDelay"])
                                } else {
                                    certainTimeHasPassedHandler()
                                    if(state.certainTimeHasPassed) {
                                        state.lastRunTime = new Date()
                                        state.cogTrue = true
                                        if(actionType) {
                                            if(logEnable || shortLog) log.debug "In startTheProcess - actionType: ${actionType} - ${state.lastRunTime} - cogTrue: ${state.cogTrue}"
                                            unschedule(permanentDimHandler)
                                            if(devicesToRefresh) devicesToRefreshActionHandler()
                                            if(actionType.contains("aFan")) { fanActionHandler() }
                                            if(actionType.contains("aGarageDoor") && (garageDoorOpenAction || garageDoorClosedAction)) { garageDoorActionHandler() }
                                            if(actionType.contains("aHubVariables") && theActVariable) { hubVariableActions() }
                                            if(actionType.contains("aLZW45") && lzw45Action) { lzw45ActionHandler() }
                                            if(actionType.contains("aLock") && (lockAction || unlockAction)) { lockActionHandler() }
                                            if(actionType.contains("aValve") && (valveOpenAction || valveClosedAction)) { valveActionHandler() }
                                            if(actionType.contains("aWindowShade") && shadeAction) { windowShadeActionHandler() }
                                            if(actionType.contains("aWindowBlind") && blindAction) { windowBlindActionHandler() }
                                            if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnActionHandler() }
                                            if(actionType.contains("aSwitch") && switchesOffAction && permanentDim2) { permanentDimHandler() }
                                            if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim2) { switchesOffActionHandler() }
                                            if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
                                            if(actionType.contains("aSwitch") && setOnLC) { dimmerOnActionHandler() }
                                            if(actionType.contains("aSwitch") && switchedDimDnAction) { slowOffHandler() }
                                            if(actionType.contains("aSwitch") && switchedDimUpAction) { slowOnHandler() }
                                            if(actionType.contains("aSwitchesColorChange")) { colorChangeHandler() }
                                            if(actionType.contains("aSwitchSequence")) { switchesInSequenceHandler() }
                                            if(actionType.contains("aSwitchesPerMode")) { switchesPerModeActionHandler() }
                                            if(actionType.contains("aThermostat")) { thermostatActionHandler() }
                                            if(actionType.contains("aSendHTTP")) { actionHttpHandler() }
                                            if(state.betweenTime) {
                                                if(actionType.contains("aNotification")) { 
                                                    state.doMessage = true
                                                    messageHandler() 
                                                    if(useTheFlasher) theFlasherHandler()
                                                }
                                            }
                                            if(actionType.contains("aBlueIris")) {
                                                if(biControl == "Switch_Profile") { profileSwitchHandler() }
                                                if(biControl == "Switch_Schedule") { scheduleSwitchHandler() }
                                                if(biControl == "Camera_Preset") { cameraPresetHandler() }                                   
                                                if(biControl == "Camera_Snapshot") { cameraSnapshotHandler() }
                                                if(biControl == "Camera_Trigger") { cameraTriggerHandler() }
                                                if(biControl == "Camera_PTZ") { cameraPTZHandler() }
                                                if(biControl == "Camera_Reboot") { cameraRebootHandler() }
                                                if(biControl == "Camera_Enable") { biChangeHandler("1") }
                                                if(biControl == "Camera_Disable") { biChangeHandler("0") }
                                            }
                                            if(actionType.contains("aVirtualContact") && (contactOpenAction || contactClosedAction)) { contactActionHandler() }
                                            state.appStatus = "active"
                                            state.appRevStatus = "round1"
                                        }
                                        if(keypadAction) securityKeypadActionHandler()
                                        if(setHSM) hsmChangeActionHandler()
                                        if(modeAction) modeChangeActionHandler()
                                        if(rmRule) ruleMachineHandler()
                                        if(setGVname && setGVvalue) setGlobalVariableHandler()
                                        //if(eeAction) eventEngineHandler()
                                        if(pcAction) patternControllerHandler()
                                        state.hasntDelayedYet = true
                                        if(timeReverse) {
                                            theDelay = timeReverseMinutes * 60
                                            if(logEnable || shortLog) log.debug "In startTheProcess - Reverse will run in ${timeReverseMinutes} minutes"
                                            runIn(theDelay, "startTheProcess", [data: "timeReverse"])
                                        }
                                    } else {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                                        lastRan = dateFormat.parse("${state.lastRunTime}".replace("+00:00","+0000"))
                                        if(logEnable || shortLog) log.debug "In startTheProcess - Can't run again until ${certainTimeHasPassedEvent} minutes have passed since the last time it ran. (last ran: $lastRan)"
                                    }
                                }
                            } else {
                                if(logEnable || shortLog) log.debug "In startTheProcess - RUN - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus}, so skipping"
                            }
                        } else {
                            if(logEnable) log.debug "In startTheProcess - One of the Time Conditions didn't match - Stopping"
                            state.appStatus = "inactive"
                            state.appRevStatus = "round1"
                        }
                    } else if(state.whatToDo == "reverse" || state.whatToDo == "skipToReverse") {
                        if(useAppStatus) {
                            if(state.appStatus == null) state.appStatus = "active"
                            if(state.appRevStatus == null) state.appRevStatus = "round1"
                        } else {
                            state.appStatus = "active"
                            if(state.appRevStatus == null) state.appRevStatus = "round1"
                        }
                        if(logEnable || shortLog) log.debug "In startTheProcess - REVERSE - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus}"
                        if(state.appStatus == "active" || state.appRevStatus == "round2") {
                            if(reverseWithDelay && state.hasntDelayedReverseYet) {
                                if(logEnable || shortLog) log.debug "In startTheProcess - SETTING UP DELAY REVERSE"
                                if(reverseWithDelay) {
                                    if(sdTimePerMode) {
                                        if(logEnable) log.debug "In startTheProcess - Reverse-sdTimePerMode"
                                        masterDimmersPerMode.each { itOne ->
                                            def theData = "${state.sdPerModeMap}".split(",")        
                                            theData.each { itTwo -> 
                                                def pieces = itTwo.split(":")
                                                try {
                                                    theMode = pieces[0]; theDevices = pieces[1]; theLevel = pieces[2]; theTempType = pieces[3]; theTemp = pieces[4]; theColor = pieces[5]; theTime = pieces[6]; theTimeType = pieces[7]
                                                } catch(e) {
                                                    if(logEnable || shortLog) log.debug "In startTheProcess - Reverse-sdTimePerMode - Something Went Wrong"
                                                    log.error(getExceptionMessageWithLine(e))
                                                }
                                                if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
                                                theTimeType = theTimeType.replace("]","")
                                                if(logEnable || shortLog) log.debug "In startTheProcess - Reverse-sdTimePerMode - theMode: ${theMode} - theTime: ${theTime} - theTimeType: ${theTimeType}"
                                                currentMode = location.mode
                                                def modeCheck = currentMode.contains(theMode)
                                                if(modeCheck) {
                                                    if(theTimeType == "false") {    // Minutes
                                                        timeTo = theTime ?: 2
                                                        theDelay = timeTo.toInteger() * 60
                                                    } else {
                                                        timeTo = theTime ?: 120
                                                        theDelay = timeTo.toInteger()
                                                    }
                                                    if((logEnable || shortLog)) log.debug "In startTheProcess - Reverse-sdTimePerMode - currentMode: ${currentMode} - modeCheck: ${modeCheck} - timeTo: ${timeTo} - theTimeType: ${theTimeType}"
                                                    if(theTimeType) {
                                                        if((logEnable || shortLog)) log.debug "In startTheProcess - Reverse - Delay is set for ${timeTo} minute(s) (theDelay: ${theDelay})"
                                                    } else {
                                                        if((logEnable || shortLog)) log.debug "In startTheProcess - Reverse - Delay is set for ${timeTo} second(s) (theDelay: ${theDelay})"
                                                    }
                                                } else {
                                                    if(logEnable) log.debug "In startTheProcess - Reverse-sdTimePerMode - No Match"
                                                }
                                            }
                                        }
                                    } else {
                                        if(reverseTimeType) {
                                            timeTo = timeToReverse ?: 60
                                            theDelay = timeTo.toInteger()
                                        } else {
                                            timeTo = timeToReverse ?: 2
                                            theDelay = timeTo.toInteger() * 60
                                        }                   
                                        if(logEnable || shortLog) {
                                            log.debug "In startTheProcess - Reverse - reverseTimeType: ${reverseTimeType}"
                                            if(reverseTimeType) {
                                                log.debug "In startTheProcess - Reverse - Delay is set for ${timeTo} second(s) (theDelay: ${theDelay})"
                                            } else {
                                                log.debug "In startTheProcess - Reverse - Delay is set for ${timeTo} minute(s) (theDelay: ${theDelay})"
                                            }
                                        }
                                    }
                                } else {
                                    if(logEnable || shortLog) log.warn "In startTheProcess - Reverse - Something went wrong"
                                }
                                state.hasntDelayedReverseYet = false
                                if(dimWhileDelayed && (state.appStatus == "active")) {
                                    state.appStatus = "inactive"
                                    state.appRevStatus = "round2"
                                    permanentDimHandler()
                                    runIn(theDelay, "startTheProcess", [data: "runAfterDelay"])
                                } else if(dimAfterDelayed && (state.appStatus == "active")) { 
                                    theDelay = theDelay ?: 60
                                    wds = warningDimSec ?: 30
                                    firstDelay = theDelay - wds
                                    if(logEnable || shortLog) log.debug "In startTheProcess - Reverse - Will warn ${wds} seconds before Reverse"
                                    state.appStatus = "inactive"
                                    state.appRevStatus = "round2"
                                    runIn(firstDelay, "permanentDimHandler")
                                    runIn(theDelay, "startTheProcess", [data: "runAfterDelay"])
                                } else {
                                    state.appStatus = "inactive"
                                    state.appRevStatus = "round2"
                                    runIn(theDelay, "startTheProcess", [data: "runAfterDelay"])
                                }
                            } else {
                                if(!reverseTrue) { state.cogTrue = true }
                                if(actionType && state.cogTrue) {
                                    if(logEnable || shortLog) log.debug "In startTheProcess - GOING IN REVERSE - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus} - cogTrue: ${state.cogTrue}"
                                    state.appStatus = "inactive"
                                    state.appRevStatus = "round1"
                                    state.cogTrue = false
                                    if(devicesToRefresh) devicesToRefreshActionHandler()
                                    if(actionType.contains("aFan")) { fanReverseActionHandler() }
                                    if(actionType.contains("aLZW45") && lzw45Action) { lzw45ReverseHandler() }
                                    if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnReverseActionHandler() }
                                    if(actionType.contains("aSwitch") && switchesOffAction && permanentDim2) { permanentDimHandler() }
                                    if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim2) { switchesOffReverseActionHandler() }
                                    if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
                                    if(actionType.contains("aSwitch") && setOnLC && permanentDim) { permanentDimHandler() }
                                    if(actionType.contains("aSwitch") && setOnLC && !permanentDim) { switchOnReverseActionHandler("aSwitch") }  
                                    if(actionType.contains("aSwitchSequence")) { switchesInSequenceReverseHandler() }
                                    if(actionType.contains("aSwitchesPerMode") && permanentDim) { permanentDimHandler() }
                                    if(actionType.contains("aSwitchesPerMode") && !permanentDim) { switchesPerModeReverseActionHandler() }
                                    if(actionType.contains("aSwitchesColorChange") && permanentDim) { permanentDimHandler() }
                                    if(actionType.contains("aSwitchesColorChange") && !permanentDim) { switchOnReverseActionHandler("aSwitchesColorChange") }
                                    if(additionalSwitches) { additionalSwitchesHandler() }
                                    if(state.betweenTime) {
                                        if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || (customEvent && deviceORsetpoint)) {
                                            if(actionType.contains("aNotification")) { 
                                                state.doMessage = true
                                                messageHandler() 
                                                if(useTheFlasher) theFlasherHandler()
                                            }
                                        }
                                    }
                                    if(actionType.contains("aBlueIris")) {
                                        if(biControl == "Camera_Enable") { biChangeHandler("0") }
                                        if(biControl == "Camera_Disable") { biChangeHandler("1") }
                                    }
                                    if(actionType.contains("aVirtualContact") && (contactOpenAction || contactClosedAction)) { contactReverseActionHandler() }
                                }
                                state.hasntDelayedReverseYet = true
                                state.oldMap = [:]
                            }
                        } else {
                            if(logEnable || shortLog) log.debug "In startTheProcess - REVERSE - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus}, so skipping"
                        }
                    } else {
                        if(logEnable) log.debug "In startTheProcess - Something isn't right - STOPING"
                        state.appStatus = "inactive"
                        state.appRevStatus = "round1"
                    }
                }
                resetStatesHandler()
                if(logEnable || shortLog) log.trace "********************* End - startTheProcess (${state.version}) - ${app.label} - appStatus: ${state.appStatus} - appRevStatus: ${state.appRevStatus} *********************"
                if(logEnable || shortLog) log.trace "*"
                state.running = "Stopped"
            } catch(e) {
                resetStatesHandler()
                log.error(getExceptionMessageWithLine(e))
            }
        } else {
            resetStatesHandler()
            if(logEnable || shortLog) log.trace "No Actions selected. Ending"
            state.appStatus = "inactive"
            state.appRevStatus = "round1"
        }
        if(timeDaysType) {
            if(timeDaysType.contains("tHoliday")) { unschedule(startTheProcess) }
        }
    }
}

def checkingWhatToDo() {
    if(logEnable) log.debug "In checkingWhatToDo (${state.version})"
    state.jumpToStop = false
    if(state.betweenTime && state.timeBetweenSun && state.modeMatch && state.daysMatch) {
        state.timeOK = true
    } else {
        state.timeOK = false
        if(!state.betweenTime && timeBetweenRestriction) { state.jumpToStop = true }
        if(!state.timeBetweenSun && timeBetweenSunRestriction) { state.jumpToStop = true }
        if(!state.modeMatch && modeMatchRestriction) { state.jumpToStop = true }
        if(!state.daysMatch && daysMatchRestriction) { state.jumpToStop = true }
    }
    if(triggerAndOr) {
        theStatus = "In checkingWhatToDo - USING OR - totalMatch: ${state.totalMatch} - totalMatchHelper: ${state.totalMatchHelper} - setpointOK: ${state.setpointOK} - transitionOK: ${state.transitionOK} - ipStatusOK: ${state.ipStatusOK} - securityOK: ${state.securityOK} - variablesOK: ${state.variablesOK}"
        if(theStatus.contains("true")) {
            if(logEnable) log.debug "${theStatus}"
        } else {
            if(logEnable) log.warn "${theStatus}"
        }
        if(state.timeOK) {
            if((state.totalMatch >= 1) || state.setpointOK || state.transitionOK || state.ipStatusOK || state.securityOK || state.variablesOK) {
                state.everythingOK = true
            } else {
                if(state.totalMatchHelper >= 1) {
                    state.everythingOK = true
                } else {
                    state.everythingOK = false
                }
            }
        } else {
            state.everythingOK = false
            if(logEnable) log.warn "In checkingWhatToDo - USING OR - timeOK FAILED - everythingOK: ${state.everythingOK}"
        }
    } else {
        theStatus = "In checkingWhatToDo - USING AND - totalMatch: ${state.totalMatch} - totalMatchHelper: ${state.totalMatchHelper} - totalConditions: ${state.totalConditions} - setpointOK: ${state.setpointOK} - transitionOK: ${state.transitionOK} - ipStatusOK: ${state.ipStatusOK} - securityOK: ${state.securityOK} - variablesOK: ${state.variablesOK}"
        if(theStatus.contains("false")) {
            if(logEnable) log.warn "${theStatus}"
        } else {
            if(logEnable) log.debug "${theStatus}"
        }
        if(state.timeOK) {
            if((state.totalMatch == state.totalConditions) && state.setpointOK && state.transitionOK && state.ipStatusOK && state.securityOK && state.variablesOK) {
                state.everythingOK = true
            } else {
                if(state.totalMatchHelper >= 1) {
                    state.everythingOK = true
                } else {
                    state.everythingOK = false
                }
            }
        } else {
            state.everythingOK = false
            if(logEnable) log.warn "In checkingWhatToDo - USING AND - timeOK FAILED - everythingOK: ${state.everythingOK}"
        }
    }   
    theStatus = "In checkingWhatToDo - everythingOK: ${state.everythingOK}"
    if(theStatus.contains("false")) {
            if(logEnable) log.warn "${theStatus}"
        } else {
            if(logEnable) log.debug "${theStatus}"
        }
    if(state.everythingOK) {
        state.whatToDo = "run"
        if(logEnable) log.debug "In checkingWhatToDo - Run"
    } else {
        if(state.jumpToStop) {
            state.whatToDo = "stop"
            if(logEnable) log.debug "In checkingWhatToDo - Stop"
        } else if(reverse || reverseWithDelay || reverseWhenHigh || reverseWhenLow || reverseWhenBetween) {
            state.whatToDo = "reverse"
            if(logEnable) log.debug "In checkingWhatToDo - Reverse"
        } else {
            state.whatToDo = "stop"
            if(logEnable) log.debug "In checkingWhatToDo - Stop"
        }
    }   
    if(logEnable) log.debug "In checkingWhatToDo - **********  whatToDo: ${state.whatToDo}  **********"
}

def resetStatesHandler() {
    state.totalMatch = 0
    state.totalMatchHelper = 0
    state.totalConditions = 0
    state.totalRestrictionMatch = 0
    state.totalRestrictions = 0
    state.whatToDo = ""
    state.running = "Stopped"
}
// ********** Start Conditions **********
def buttonHandler(evt) {
    if(logEnable || shortLog) log.debug "In buttonEvent (${state.version})"
    def whichButton = evt.displayName
    def bNumber = evt.value
    def pressType = evt.name    
    state.whoHappened = evt.displayName
    state.whatHappened = evt.value  
    if(logEnable || shortLog) log.debug "In buttonEvent - ${whichButton}: bNumber: ${bNumber} - pressType ${pressType}"    
    if(buttonAction == "taps") {
        bNum = bNumber.toInteger()
        bTap = buttonTaps.toInteger()
        if(bNum == bTap) {
            if(logEnable || shortLog) log.debug "In buttonEvent - Number of Presses: ${bNum} matches presses Needed: ${bTap}"
            startTheProcess("button")
        } else {
            if(logEnable || shortLog) log.debug "In buttonEvent - Number of Presses: ${bNum} does NOT match presses Needed: ${bTap} - stopping"
        }
    } else if(bNumber == buttonNumber) {
        if(logEnable || shortLog) log.debug "In buttonEvent - Button Pressed: ${bNumber} matches Button Needed: ${buttonNumber}"
        if(pressType == buttonAction) {
            if(logEnable || shortLog) log.debug "In buttonEvent - Press Type: ${pressType} matches event Needed: ${buttonAction}"
            startTheProcess("button")
        } else {
            if(logEnable || shortLog) log.debug "In buttonEvent - Press Type: ${pressType} did NOT match event Needed: ${buttonAction} - stopping"
        }
    } else {
        if(logEnable || shortLog) log.debug "In buttonEvent - Button Pressed: ${bNumber} did NOT match Button Needed: ${buttonNumber} - Stopping"
    }
}
def customDeviceHandler(theType) {
    state.eventType = specialAtt;    state.typeValue1 = custom1;    state.typeValue2 = custom2
    if(theType == "condition") {
        deviceHandler(theType, customEvent, sdCustom1Custom2, customANDOR)
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        //
    }
}
def accelerationHandler(theType) {
    state.eventType = "acceleration";    state.typeValue1 = "active";    state.typeValue2 = "inactive"
    if(theType == "condition") {
        deviceHandler(theType, accelerationEvent, asInactiveActive, accelerationANDOR)
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        deviceHandler(theType, accelerationRestrictionEvent, arInactiveActive, accelerationRANDOR)
    } 
}
def contactHandler(theType) {
    state.eventType = "contact";    state.typeValue1 = "open";    state.typeValue2 = "closed"
    if(theType == "condition") {
        deviceHandler(theType, contactEvent, csClosedOpen, contactANDOR)
    } else if(theType == "helper") {
        deviceHandler(theType, myContacts2, contactOption2, "false")
    } else if(theType == "restriction") {
        deviceHandler(theType, contactRestrictionEvent, crClosedOpen, contactRANDOR)
    }
}
def garageDoorHandler(theType) {
    state.eventType = "door";    state.typeValue1 = "open";    state.typeValue2 = "closed"
    if(theType == "condition") {
        deviceHandler(theType, garageDoorEvent, gdClosedOpen, garageDoorANDOR)
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        deviceHandler(theType, garageDoorRestrictionEvent, gdrClosedOpen, garageDoorRANDOR)
    }
}
def globalVariablesTextHandler(theType) {
    state.eventType = "globalVariable";    state.typeValue1 = gvValue;    state.typeValue2 = "noData"
    if(theType == "condition") {
        deviceHandler(theType, globalVariableEvent, "true", "false")
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        //
    }
}
def lockHandler(theType) {
    state.eventType = "lock";    state.typeValue1 = "locked";    state.typeValue2 = "unlocked"
    if(theType == "condition") {
        deviceHandler(theType, lockEvent, lUnlockedLocked, lockANDOR)
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        deviceHandler(theType, lockRestrictionEvent, lrUnlockedLocked, "false")
    }
}
def motionHandler(theType) {
    state.eventType = "motion";    state.typeValue1 = "active";    state.typeValue2 = "inactive"
    if(theType == "condition") {
        deviceHandler(theType, motionEvent, meInactiveActive, motionANDOR)
    } else if(theType == "helper") {
        deviceHandler(theType, myMotion2, motionOption2, "false")
    } else if(theType == "restriction") {
        deviceHandler(theType, motionRestrictionEvent, mrInactiveActive, motionRANDOR)
    }
}
def presenceHandler(theType) {
    state.eventType = "presence";    state.typeValue1 = "not present";    state.typeValue2 = "present"
    if(theType == "condition") {
        deviceHandler(theType, presenceEvent, pePresentNotPresent, presenceANDOR)
    } else if(theType == "helper") {
        deviceHandler(theType, myPresence2, presenceOption2, "false")
    } else if(theType == "restriction") {
        deviceHandler(theType, presenceRestrictionEvent, prPresentNotPresent, presenceRANDOR)
    }    
}
def switchHandler(theType) {
    state.eventType = "switch";    state.typeValue1 = "on";    state.typeValue2 = "off"
    if(theType == "condition") {
        deviceHandler(theType, switchEvent, seOffOn, switchANDOR)
    } else if(theType == "helper") {
        deviceHandler(theType, mySwitches2, switchesOption2, "false")
    } else if(theType == "restriction") {
        deviceHandler(theType, switchRestrictionEvent, srOffOn, switchRANDOR)
    }
}
def thermostatHandler(theType) {
    state.eventType = "thermostatOperatingState";    state.typeValue1 = "idle";    state.typeValue2 = "thermostatEvent"
    if(theType == "condition") {
        deviceHandler(theType, thermoEvent, "false", thermoANDOR)
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        //
    }
}
def waterHandler(theType) {
    state.eventType = "water";    state.typeValue1 = "Wet";    state.typeValue2 = "Dry"
    if(theType == "condition") {
        deviceHandler(theType, waterEvent, weDryWet, waterANDOR)
    } else if(theType == "helper") {
        //
    } else if(theType == "restriction") {
        deviceHandler(theType, waterRestrictionEvent, wrDryWet, waterRANDOR)
    }
}

def deviceHandler(theType, eventName, type, typeAO) {
    if(logEnable) log.debug "In deviceHandler (${state.version}) - ${state.eventType.toUpperCase()} - theType: ${theType}"
    state.deviceMatch = 0;    state.restrictionMatch = 0;    state.count = 0;    deviceTrue1 = 0;    deviceTrue2 = 0
    if(type == "false") type = false
    if(type == "true") type = true
    if(typeAO == "false") typeAO = false
    if(typeAO == "true") typeAO = true
    if(state.totalConditions == null) state.totalConditions = 0
    if(state.totalMatch == null) state.totalMatch = 0
    if(state.totalMatchHelper == null) state.totalMatchHelper = 0
    if(state.totalRestrictions == null) state.totalRestrictions = 0
    if(state.totalRestrictionMatch == null) state.totalRestrictionMatch = 0
    state.isThereDevices = true
    if(theType == "condition") { state.totalConditions = state.totalConditions + 1 }
    if(theType == "restriction") { state.totalRestrictions = state.totalRestrictions + 1 }
    try {
        if(state.eventType == "globalVariable") {
            theList = []
            theList << globalVariableEvent
            eventName = theList
            state.theCount = eventName.size()
        } else {
            state.theCount = eventName.size()
        }
    } catch(e) { 
        state.theCount = 1
    }
    if(state.whoText == null) state.whoText = ""
    if(state.whoLocked == null) state.whoLocked = ""
    if(state.whoUnlocked == null) state.whoUnlocked = ""
    if(eventName) {
        eventName.each { it ->
            if(state.eventType == "globalVariable") {
                def theData = state.gvMap.get(globalVariableEvent)
                theValue = theData.toString()
            } else {
                theValue = it.currentValue("${state.eventType}")
            }
            if(logEnable) log.debug "In deviceHandler - Checking: ${it.displayName} - ${state.eventType} - Testing Current Value - ${theValue}"
            if(theValue == state.typeValue1) {
                if(logEnable) log.debug "In deviceHandler - Working 1: ${state.typeValue1} and Current Value: ${theValue}"
                if(state.eventType == "lock") {
                    if(specialMessage) {
                        tMessage = sLockMessage.split(";")
                        lText = state.whoText.toLowerCase()
                        tMessage.each { ms ->
                            if(logEnable) log.debug "In deviceHandler - Checking lock for message - $ms"
                            if(lText.contains("${ms.toLowerCase()}")) {
                                state.whoLocked = "${ms}"
                                if(logEnable) log.debug "In deviceHandler - Message MATCH - whoLocked: ${state.whoLocked}"
                                deviceTrue1 = deviceTrue1 + 1
                            }
                        }
                    } else {  
                        if(state.whoText.contains("was locked") || state.whoText.contains("locked by")) {
                            if(state.whoText.contains("digital")) {
                                if(noCodeLocks) {
                                    if(logEnable) log.debug "In deviceHandler - Lock was digitally locked, Including"
                                    deviceTrue1 = deviceTrue1 + 1
                                } else {
                                    if(logEnable) log.debug "In deviceHandler - Lock was digitally locked, NOT Including"
                                }
                            } else if(state.whoText.contains("keypad")) {
                                if(noCodeLocks) {
                                    if(logEnable) log.debug "In deviceHandler - Lock was locked by keypad, Including"
                                    deviceTrue1 = deviceTrue1 + 1
                                } else {
                                    if(logEnable) log.debug "In deviceHandler - Lock was locked by keypad, NOT Including"
                                }
                            } else if(state.whoText.contains("physical") || state.whoText.contains("manual")) {
                                if(noCodeLocks) {
                                    if(logEnable) log.debug "In deviceHandler - Lock was manually locked, Including"
                                    deviceTrue1 = deviceTrue1 + 1
                                } else {
                                    if(logEnable) log.debug "In deviceHandler - Lock was manually locked, NOT Including"
                                }
                            } else if(lockUser) {
                                if(logEnable) log.debug "In deviceHandler - Lock was locked by code - Checking"
                                lockUser.each { us ->
                                    if(logEnable && extraLogs) log.debug "In deviceHandler - Checking lock names - $us"
                                    if(state.whoText.contains("${us}")) {
                                        state.whoLocked = "${us}"
                                        if(logEnable) log.debug "In deviceHandler - MATCH - whoLocked: ${state.whoLocked}"
                                        deviceTrue1 = deviceTrue1 + 1
                                    }
                                }
                            } else {
                                if(logEnable) log.debug "In deviceHandler - moving on"
                                deviceTrue1 = deviceTrue1 + 1
                            }
                        } else {
                            if(noCodeLocks) {
                                if(logEnable) log.debug "In deviceHandler - Lock was manually locked, Including"
                                deviceTrue1 = deviceTrue1 + 1
                            } else {
                                if(logEnable) log.debug "In deviceHandler - Lock was manually locked, NOT Including"
                            }
                        }
                    }
                } else if(state.eventType == "switch") {
                    if(seType) {
                        if(logEnable) log.debug "In deviceHandler - Switch - ONLY Physical"
                        if(state.whoText.contains("[physical]")) { deviceTrue1 = deviceTrue1 + 1 }
                    } else {
                        if(logEnable) log.debug "In deviceHandler - Switch - Digital AND Physical"
                        deviceTrue1 = deviceTrue1 + 1
                    }  
                } else {
                    deviceTrue1 = deviceTrue1 + 1
                    if(logEnable) log.debug "In deviceHandler - Adding to deviceTrue1: ${deviceTrue1}"
                }
            } else if(theValue == state.typeValue2) { 
                if(logEnable) log.debug "In deviceHandler - Working 2: ${state.typeValue2} and Current Value: ${theValue}"
                if(state.eventType == "lock") {
                    if(specialMessage) {
                        tMessage = sLockMessage.split(";")
                        lText = state.whoText.toLowerCase()
                        tMessage.each { ms ->
                            if(logEnable) log.debug "In deviceHandler - Checking lock for message - $ms"
                            if(lText.contains("${ms.toLowerCase()}")) {
                                state.whoUnLocked = "${ms}"
                                if(logEnable) log.debug "In deviceHandler - Message MATCH - whoUnLocked: ${state.whoUnLocked}"
                                deviceTrue2 = deviceTrue2 + 1
                            }
                        }
                    } else {
                        if(state.whoText.contains("was unlocked") || state.whoText.contains("unlocked by")) {
                            if(state.whoText.contains("digital")) {
                                if(noCodeUnlocks) {
                                    if(logEnable) log.debug "In deviceHandler - Lock was digitally unlocked, Including"
                                    deviceTrue2 = deviceTrue2 + 1
                                } else {
                                    if(logEnable) log.debug "In deviceHandler - Lock was digitally unlocked, NOT Including"
                                }
                            } else if(state.whoText.contains("physical") || state.whoText.contains("manual")) {
                                if(noCodeUnlocks) {
                                    if(logEnable) log.debug "In deviceHandler - Lock was manually unlocked, Including"
                                    deviceTrue2 = deviceTrue2 + 1
                                } else {
                                    if(logEnable) log.debug "In deviceHandler - Lock was manually unlocked, NOT Including"
                                }
                            } else if(lockUser) {
                                state.whoUnlocked = it.currentValue("lastCodeName")
                                lockUser.each { us ->
                                    if(logEnable && extraLogs) log.debug "In deviceHandler - Checking lock names - $us vs $state.whoUnlocked"
                                    if(us == state.whoUnlocked) { 
                                        if(logEnable) log.debug "In deviceHandler - MATCH - whoUnlocked: ${state.whoUnlocked}"
                                        deviceTrue2 = deviceTrue2 + 1
                                    }
                                }
                            } else {
                                if(logEnable) log.debug "In deviceHandler - moving on"
                                deviceTrue2 = deviceTrue2 + 1
                            }
                        } else {
                            if(noCodeUnlocks) {
                                if(logEnable) log.debug "In deviceHandler - Lock was manually unlocked, Including"
                                deviceTrue2 = deviceTrue2 + 1
                            } else {
                                if(logEnable) log.debug "In deviceHandler - Lock was manually unlocked, NOT Including"
                            }
                        }
                    }
                } else if(state.eventType == "switch") {
                    if(seType) {
                        if(logEnable) log.debug "In deviceHandler - Switch - Only Physical"
                        if(state.whoText.contains("[physical]")) { deviceTrue2 = deviceTrue2 + 1 }
                    } else {
                        if(logEnable) log.debug "In deviceHandler - Switch - Digital and Physical"
                        deviceTrue2 = deviceTrue2 + 1
                    }  
                } else {
                    deviceTrue2 = deviceTrue2 + 1
                    if(logEnable) log.debug "In deviceHandler - Adding to deviceTrue2: ${deviceTrue2}"
                }
            } else {
                if(state.eventType == "thermostatOperatingState") {
                    if(theValue != "idle") {
                        deviceTrue2 = deviceTrue2 + 1
                        if(logEnable && extraLogs) log.debug "In deviceHandler - Thermostat - Working 2: Current Value: ${theValue}"
                    }
                } else {
                    // next option
                }
            }
        }
    }
    if(type) {
        state.deviceMatch = state.deviceMatch + deviceTrue1
    } else {
        state.deviceMatch = state.deviceMatch + deviceTrue2
    }
    if(logEnable) log.debug "In deviceHandler - ($theType) - type: ${type} - deviceMatch: ${state.deviceMatch} - theCount: ${state.theCount} - typeAO: ${typeAO}"
    if(typeAO) {  // OR (true)
        if(logEnable) log.debug "In deviceHandler - ($theType) - Using OR"
        if(state.deviceMatch >= 1) {
            if(theType == "condition") { state.totalMatch = state.totalMatch + 1 }
            if(theType == "helper") { state.totalMatchHelper = state.totalMatchHelper + 1 }
            if(theType == "restriction") {
                state.totalRestrictionMatch = state.totalRestrictionMatch + 1
                state.areRestrictions = true
            }
        }
    } else {  // AND (False)
        if(logEnable) log.debug "In deviceHandler - ($theType) - Using AND"
        if(state.deviceMatch == state.theCount) {           
            if(theType == "condition") { state.totalMatch = state.totalMatch + 1 }
            if(theType == "helper") { state.totalMatchHelper = state.totalMatchHelper + 1 }        
            if(theType == "restriction") {
                state.totalRestrictionMatch = state.totalRestrictionMatch + 1
                state.areRestrictions = true
            }
        }
    }
    if(typeAO) {
        if(theType == "condition") { 
            if(logEnable) log.debug "In deviceHandler - ($theType) - ${state.eventType.toUpperCase()} - OR - count: ${state.theCount} - totalMatch: ${state.totalMatch} - totalConditions: ${state.totalConditions}"
        } else if(theType == "helper") { 
            if(logEnable) log.debug "In deviceHandler - ($theType) - ${state.eventType.toUpperCase()} - OR - count: ${state.theCount} - totalMatch: ${state.totalMatchHelper} - totalConditions: ${state.totalConditions}"
        } else if(theType == "restriction") { 
            if(logEnable) log.debug "In deviceHandler - ($theType) - ${state.eventType.toUpperCase()} - OR - count: ${state.theCount} - totalRestrictionMatch: ${state.totalRestrictionMatch} - totalRestrictions: ${state.totalRestrictions}"
        }
    } else {
        if(theType == "condition") { 
            if(logEnable) log.debug "In deviceHandler - ($theType) - ${state.eventType.toUpperCase()} - AND - count: ${state.theCount} - totalMatch: ${state.totalMatch} - totalConditions: ${state.totalConditions}"
        } else if(theType == "helper") { 
            if(logEnable) log.debug "In deviceHandler - ($theType) - ${state.eventType.toUpperCase()} - OR - count: ${state.theCount} - totalMatch: ${state.totalMatchHelper} - totalConditions: ${state.totalConditions}"
        } else if(theType == "restriction") { 
            if(logEnable) log.debug "In deviceHandler - ($theType) - ${state.eventType.toUpperCase()} - OR - count: ${state.theCount} - totalRestrictionMatch: ${state.totalRestrictionMatch} - totalRestrictions: ${state.totalRestrictions}"
        }    
    }
}

def hsmAlertHandler(data) {
    if(hsmAlertEvent) {
        if(logEnable) log.debug "In hsmAlertHandler (${state.version})"
        String theValue = data
        hsmAlertEvent.each { it ->
            if(logEnable && extraLogs) log.debug "In hsmAlertHandler - Checking: ${it} - value: ${theValue}"
            if(theValue == it){
                state.totalMatch = 1
                state.totalConditions = 1
            }
        }
        if(logEnable) log.debug "In hsmAlertHandler - totalMatch: ${state.totalMatch} - totalConditions: ${state.totalConditions}"
    } else {
        if(logEnable) log.debug "In hsmAlertHandler - NOT being used."
    }
}

def hsmStatusHandler(data) {
    if(hsmStatusEvent) {
        if(logEnable) log.debug "In hsmStatusHandler (${state.version})"
        String theValue = data
        hsmStatusEvent.each { it ->
            if(logEnable && extraLogs) log.debug "In hsmStatusHandler - Checking: ${it} - value: ${theValue}"
            if(theValue == it){
                state.totalMatch = 1
                state.totalConditions = 1
            }
        }
    if(logEnable) log.debug "In hsmStatusHandler - totalMatch: ${state.totalMatch} - totalConditions: ${state.totalConditions}"
    } else {
        if(logEnable) log.debug "In hsmStatusHandler - NOT being used."
    }
}

def ruleMachineHandler() {
    if(logEnable) log.debug "In ruleMachineHandler - rmRuleType: ${rmRuleType} - Rule: ${rmRule} - Action: ${rmAction}"
    if(rmRuleType) {
        RMUtils.sendAction(rmRule, rmAction, app.label, '5.0')
    } else {
        RMUtils.sendAction(rmRule, rmAction, app.label)
    }
}

def safetyNetHandler() {
    if(snDeviceEvent) {
        state.snMatch = false
        if(logEnable) log.debug "In safetyNetHandler (${state.version})"
        alarmStatus = snDeviceEvent.currentValue("alarmStatus")
        snStatus.each { it ->
            if(logEnable) log.debug "In safetyNetHandler - Checking: ${snDeviceEvent} for: ${it} - VS - ${alarmStatus}"
            if(it.toString() == alarmStatus.toString()){
                if(logEnable) log.debug "In safetyNetHandler - MATCH!"
                state.snMatch = true
            }
        }
        if(logEnable) log.debug "In safetyNetHandler - snMatch: ${state.snMatch}"
    } else {
        state.snMatch = true
        if(logEnable) log.debug "In safetyNetHandler - Safety Net NOT being used - snMatch: ${state.snMatch}"
    }
}

// ***** Start Setpoint Handlers *****
def customSetpointHandler() {
    state.spName = customEvent; state.spType = specialAtt; state.setpointHigh = sdSetPointHigh; state.setpointLow = sdSetPointLow; state.spInBetween = setSDPointBetween
    setpointHandler()
}
def batteryHandler() {
    state.spName = batteryEvent; state.spType = "battery"; state.setpointHigh = beSetPointHigh; state.setpointLow = beSetPointLow; state.spInBetween = setBEPointBetween
    setpointHandler()
}
def energyHandler() {
    state.spName = energyEvent; state.spType = "energy"; state.setpointHigh = eeSetPointHigh; state.setpointLow = eeSetPointLow; state.spInBetween = setEEPointBetween
    setpointHandler()
}
def globalVariablesNumberHandler() {
    state.spName = globalVariableEvent; state.spType = "globalVariable"; state.setpointHigh = gvSetPointHigh; state.setpointLow = gvSetPointLow; state.spInBetween = setGVPointBetween
    setpointHandler()
}
def humidityHandler() {
    state.spName = humidityEvent; state.spType = "humidity"; state.setpointHigh = heSetPointHigh; state.setpointLow = heSetPointLow; state.spInBetween = setHEPointBetween
    setpointHandler()
}
def illuminanceHandler() {
    state.spName = illuminanceEvent; state.spType = "illuminance"; state.setpointHigh = ieSetPointHigh; state.setpointLow = ieSetPointLow; state.spInBetween = setIEPointBetween
    setpointHandler()
}
def powerHandler() {
    state.spName = powerEvent; state.spType = "power"; state.setpointHigh = peSetPointHigh; state.setpointLow = peSetPointLow; state.spInBetween = setPEPointBetween
    setpointHandler()
}
def tempHandler() {
    state.spName = tempEvent; state.spType = "temperature"; state.setpointHigh = teSetPointHigh; state.setpointLow = teSetPointLow; state.spInBetween = setTEPointBetween
    setpointHandler()
}
def voltageHandler() {
    state.spName = voltageEvent; state.spType = "voltage"; state.setpointHigh = veSetPointHigh; state.setpointLow = veSetPointLow; state.spInBetween = setSDPointBetween
    setpointHandler()
}

def setpointHandler() {
    if(logEnable) log.debug "In setpointHandler (${state.version}) - spName: ${state.spName}"
    if(state.setpointHighOK == null) state.setpointHighOK = "yes"
    if(state.setpointLowOK == null) state.setpointLowOK = "yes"
    if(state.setpointBetweenOK == null) state.setpointBetweenOK = "yes"
    state.isThereSPDevices = true
    state.spName.each {
        if(state.spType == "globalVariable") {
            def theData = state.gvMap.get(globalVariableEvent)
            spValue = theData
            if(logEnable) log.debug "In setpointHandler - theData: ${theData}"
        } else {
            spValue = it.currentValue("${state.spType}")
        }
        if(logEnable) log.debug "In setpointHandler - spValue: ${spValue}"
        if(spValue || spValue == 0) {
            if(useWholeNumber) {
                setpointValue = Math.round(spValue)
            } else {
                setpointValue = spValue.toFloat().round(2)
            }
            if(state.preSPV == null) state.preSPV = setpointValue
            if(logEnable) log.debug "In setpointHandler - setpointValue: ${setpointValue} - state.preSPV: ${state.preSPV}"
            if(spDirection) {
                if(setpointValue < state.preSPV) {
                    theDiff = state.preSPV - setpointValue
                } else {
                    theDiff = setpointValue - state.preSPV
                }
                theDifference = theDiff.toFloat().round(2)
                if(spDirMinValue == null) spDirMinValue = 0
                if(logEnable) log.debug "In setpointHandler - Checking if theDifference: ${theDifference} is greater than spDirMinValue: ${spDirMinValue}"
                if(theDifference > spDirMinValue) {
                    if(!spDirDownUp) {
                        if(setpointValue < state.preSPV) {
                            direction = "DOWN"
                            theDirection = true
                        } else {
                            direction = "UP"
                            theDirection = false
                        }
                    }
                    if(spDirDownUp) {
                        if(setpointValue < state.preSPV) {
                            direction = "DOWN"
                            theDirection = false
                        } else {
                            direction = "UP"
                            theDirection = true
                        }
                    }
                    if(spDirDownUp) {
                        neededDir = "UP"
                    } else {
                        neededDir = "DOWN"
                    }
                    if(logEnable) log.debug "In setpointHandler - Direction - Setpoint value is going ${direction}, needs to go ${neededDir}"
                    meetsMinValue = true
                    state.preSPV = setpointValue
                } else {
                    if(logEnable) log.debug "In setpointHandler - Direction - The difference (${theDifference}) was not greater than the Min Difference (${spDirMinValue}). Reading not recorded."
                    meetsMinValue = false
                }
            } else {
                theDirection = true
                meetsMinValue = true
                state.preSPV = setpointValue
            }
            if(meetsMinValue) {
                if(theDirection) {
                    if(logEnable) log.debug "In setpointHandler - Direction - Setpoint value is going in the correct direction"
                    if(setpointRollingAverage && setpointValue) {
                        theReadings = state.readings
                        if(theReadings == null) theReadings = []
                        theReadings.add(0,setpointValue)        
                        int maxReadingSize = numOfPoints
                        int readings = theReadings.size()
                        if(readings > maxReadingSize) theReadings.removeAt(maxReadingSize)
                        state.readings = theReadings
                        setpointRollingAverageHandler(maxReadingSize)
                        if(state.theAverage >= 0) setpointValue = state.theAverage
                    }
                    if(state.setpointHigh && state.setpointLow && state.spInBetween) {
                        setpointLow = state.setpointLow
                        setpointHigh = state.setpointHigh
                        if(setpointValue <= setpointHigh && setpointValue > setpointLow) {
                            if(logEnable) log.debug "In setpointHandler (Between) - Device: ${it}, Value: ${setpointValue} is BETWEEN setpointHigh: ${setpointHigh} and setpointLow: ${setpointLow}"
                            state.setpointBetweenOK = "no"
                        } else {
                            if(logEnable) log.debug "In setpointHandler (Between) - Device: ${it}, Value: ${setpointValue} is NOT BETWEEN setpointHigh: ${setpointHigh} and setpointLow: ${setpointLow}"
                            state.setpointBetweenOK = "yes"
                        }
                    } else {
                        if(state.setpointHigh) {
                            setpointHigh = state.setpointHigh
                            if(setpointValue >= setpointHigh) {  // bad
                                if(logEnable) log.debug "In setpointHandler (High) - Device: ${it}, Value: ${setpointValue} is GREATER THAN setpointHigh: ${setpointHigh} (Bad)"
                                state.setpointHighOK = "no"
                            } else {
                                if(logEnable) log.debug "In setpointHandler (High) - Device: ${it}, Value: ${setpointValue} is LESS THAN setpointHigh: ${setpointHigh} (Good)"
                                state.setpointHighOK = "yes"
                            }
                        }
                            
                        if(state.setpointLow) {
                            setpointLow = state.setpointLow
                            if(setpointValue < setpointLow) {  // bad
                                if(logEnable) log.debug "In setpointHandler (Low) - Device: ${it}, Value: ${setpointValue} is LESS THAN setpointLow: ${setpointLow} (Bad)"
                                state.setpointLowOK = "no"
                            } else {
                                if(logEnable) log.debug "In setpointHandler (Low) - Device: ${it}, Value: ${setpointValue} is GREATER THAN setpointLow: ${setpointLow} (Good)"
                                state.setpointLowOK = "yes"
                            }
                        }
                        
                        if(state.setpointHighOK == "no" || state.setpointLowOK == "no" || state.setpointBetweenOK == "no") {
                            state.setpointOK = true
                        } else {
                            state.setpointOK = false
                        }
                    }
                } else {
                    if(logEnable) log.debug "In setpointHandler - Direction - Setpoint value is going in the wrong direction"
                    state.setpointHighOK = "yes"
                    state.setpointLowOK = "yes"
                    state.setpointOK = false
                }
            }
        } else {
            if(state.setpointHigh && state.setpointLow) {
                state.setpointBetweenOK = "no"
                state.setpointOK = true
            } else if(state.setpointHigh) {
                state.setpointHighOK = "no"
                state.setpointOK = true
            } else if(state.setpointLow) {  
                state.setpointLowOK = "no"
                state.setpointOK = true
            }
        }
    }
    if(logEnable) log.debug "In setpointHandler - ${state.spType.toUpperCase()} - setpointOK: ${state.setpointOK}"
}

def setpointRollingAverageHandler(data) {
    if(logEnable) log.debug "In setpointRollingAverageHandler (${state.version})"
    int totalNum = 0
    int maxReadingSize = data    
    floatingPoint = false
    if(logEnable) log.debug "In setpointRollingAverageHandler  - state.readings: ${state.readings}"
    String reading = state.readings
    readings = reading.replace("[","").replace("]","")
    def theNumbers = readings.split(",")
    int readingsSize = theNumbers.size()
    if(readingsSize > 1) {       
        for(x=0;x<readingsSize;x++) {
            try {
                int theNumber = theNumbers[x].toInteger()
                if(logEnable) log.debug "In setpointRollingAverageHandler - ${x} - ${theNumber}"
                totalNum = totalNum + theNumber  
            } catch(e) {
                theReadings.removeAt(x)
                readingsSize = readingsSize - 1
                if(logEnable) log.debug "In setpointRollingAverageHandler - Removed some bad data."
            }      
        }       
        if(logEnable) log.debug "In setpointRollingAverageHandler - totalNum: ${totalNum} - readingsSize: ${readingsSize}"
        if(totalNum == 0 || totalNum == null) {
            state.theAverage = 0
        } else {
            state.theAverage = (totalNum / readingsSize).toDouble().round()
        }
    }
    if(logEnable) log.debug "In setpointRollingAverageHandler - theAverage: ${state.theAverage} - readingSize: ${readingsSize} - readings: ${readings}"
}

// ********** Start Actions **********
def switchesPerModeActionHandler() {
    if(logEnable) log.debug "In switchesPerModeActionHandler - (${state.version})"
    currentMode = location.mode
    state.modeMatch = false
    masterDimmersPerMode.each { itOne ->
        def theData = "${state.sdPerModeMap}".split(",")        
        theData.each { itTwo -> 
            def pieces = itTwo.split(":")
            try {
                theMode = pieces[0]; theDevices = pieces[1]; theLevel = pieces[2]; theTempType = pieces[3]; theTemp = pieces[4]; theColor = pieces[5]; theTime = pieces[6]; theTimeType = pieces[7]
            } catch(e) {
                log.error(getExceptionMessageWithLine(e))
                log.warn "${app.label} - Something went wrong, please rebuild your Switches Per Mode table"
                if(logEnable) log.warn "In switchesPerModeActionHandler - Oops 1 - 0: ${theMode} - 1: ${theDevices} - 2: ${theLevel} - 3: ${theTempType} - 4: ${theTemp} - 5: ${theColor} - 6: ${theTime} - 7: ${theTimeType}"
            }
            if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
            def modeCheck = currentMode.contains(theMode)
            if(logEnable) log.debug "In switchesPerModeActionHandler - currentMode: ${currentMode} - modeCheck: ${modeCheck}"
            if(modeCheck) {
                state.modeMatch = true
                theColor = theColor.replace("]","")
                theTime = theTime.replace("]","")
                def cleanOne = "${itOne}"
                def cleanTwo = theDevices.replace("[","").replace("]","").split(";")
                cleanTwo.each { itThree ->
                    if(itThree.startsWith(" ") || itThree.startsWith("[")) itThree = itThree.substring(1)
                    if(logEnable) log.debug "In switchesPerModeActionHandler - Comparing cleanOne: ${cleanOne} - itThree: ${itThree}"
                    if(cleanOne == itThree) {
                        if((logEnable) || shortLog) log.debug "In switchesPerModeActionHandler - MATCH - Sending: ${itOne}"
                        state.fromWhere = "switchesPerMode"
                        state.onColor = "${theColor}"
                        state.onTemp = theTemp
                        state.onLevel = theLevel
                        setLevelandColorHandler(itOne)
                    }
                }
            }
        }
    }
    if(!state.modeMatch) switchesPerModeReverseActionHandler()
}

def switchesPerModeReverseActionHandler() {
    if(masterDimmersPerMode) {
        masterDimmersPerMode.each { it ->
            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Working on $it"
            name = (it.displayName).replace(" ","")
            try {
                data = state.oldMap.get(name)
                if(data) {                
                    def theData = data.split("::")
                    oldStatus = theData[0]
                    hueColor = theData[1]
                    saturation = theData[2]
                    level = theData[3]
                    cTemp = theData[4]
                    cMode = theData[5]
                } else {
                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Found NO data"
                }
            } catch(e) { 
                log.error(getExceptionMessageWithLine(e))
            }
            if(it.hasCommand("setColor")) {
                if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Using setColor"
                try {
                    if(cMode == "CT") {
                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor CT - Reversing Light: ${it} - oldStatus: ${oldStatus} - cTemp: ${ctemp} - level: ${level} - trueReverse: ${trueReverse}"                          
                        if(oldStatus == "off" || trueReverse) {                            
                            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor CT - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        } else {
                            pauseExecution(actionDelay)
                            it.setColorTemperature(cTemp)
                        }
                    } else {
                        if(level) level = level.toInteger()
                        def theValue = [hue: hueColor, saturation: saturation, level: level]
                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - theValue: ${theValue} - trueReverse: ${trueReverse}"
                        if(oldStatus == "off" || trueReverse) {
                            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        } else {
                            pauseExecution(actionDelay)
                            it.setLevel(level)
                            pauseExecution(actionDelay)
                            it.setColor(theValue)
                        }
                    }
                } catch(e) {
                    log.warn(getExceptionMessageWithLine(e))
                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor Oops - Turning Off (${it})"
                    pauseExecution(actionDelay)
                    it.off()
                }
            } else if(it.hasCommand("setColorTemperature") && theColor == "NA") {
                if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Using setColorTemperature"
                try {
                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColorTemp - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level} - cTemp: ${cTemp} - trueReverse: ${trueReverse}"
                    if(oldStatus == "off" || trueReverse) {
                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColorTemp - Turning light off (${it})"
                        pauseExecution(actionDelay)
                        it.off()
                    } else {
                        pauseExecution(actionDelay)
                        it.setLevel(level)
                        pauseExecution(actionDelay)
                        it.setColorTemperature(cTemp)
                    }
                } catch(e) {
                    log.warn(getExceptionMessageWithLine(e))
                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColorTemp Oops - Turning Off (${it})"
                    pauseExecution(actionDelay)
                    it.off()
                }      
            } else if(it.hasCommand("setLevel")) {
                if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Using setLevel"
                try {
                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setLevel - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level} - trueReverse: ${trueReverse}"
                    if(oldStatus == "off" || trueReverse) {
                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setLevel - Turning light off (${it})"
                        pauseExecution(actionDelay)
                        it.off()
                    } else {
                        pauseExecution(actionDelay)
                        it.setLevel(level)
                    }
                } catch(e) {
                    log.warn(getExceptionMessageWithLine(e))
                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setLevel Oops - Turning Off (${it})"
                    pauseExecution(actionDelay)
                    it.off()
                }
            }
            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Removing ${it} from oldMap."
            if(name && state.oldMap) state.oldMap.remove(name)
        }
    }
    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - oldMap: ${state.oldMap}"
}

def switchesInSequenceHandler() {
    if(logEnable) log.debug "In switchesInSequenceHandler (${state.version}) - deviceSeqAction1: ${deviceSeqAction1} - deviceSeqAction2: ${deviceSeqAction2} - deviceSeqAction3: ${deviceSeqAction3} - deviceSeqAction4: ${deviceSeqAction4} - deviceSeqAction5: ${deviceSeqAction5}"
    if(deviceSeqAction1) {
        deviceSeqAction1.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand1 == "on") {
                it.on()
            } else if(dsaCommand1 == "off") {
                it.off()
            }
        }
    }
    if(deviceSeqAction2) {
        deviceSeqAction2.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand2 == "on") {
                it.on()
            } else if(dsaCommand2 == "off") {
                it.off()
            }
        }
    }
    if(deviceSeqAction3) {
        deviceSeqAction3.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand3 == "on") {
                it.on()
            } else if(dsaCommand3 == "off") {
                it.off()
            }
        }
    }
    if(deviceSeqAction4) {
        deviceSeqAction4.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand4 == "on") {
                it.on()
            } else if(dsaCommand4 == "off") {
                it.off()
            }
        }
    }
    if(deviceSeqAction5) {
        deviceSeqAction5.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand5 == "on") {
                it.on()
            } else if(dsaCommand5 == "off") {
                it.off()
            }
        }
    }
}

def switchesInSequenceReverseHandler() {
    if(logEnable) log.debug "In switchesInSequenceReverseHandler (${state.version}) - deviceSeqAction1: ${deviceSeqAction1} - deviceSeqAction2: ${deviceSeqAction2} - deviceSeqAction3: ${deviceSeqAction3} - deviceSeqAction4: ${deviceSeqAction4} - deviceSeqAction5: ${deviceSeqAction5}"
    if(deviceSeqAction5) {
        deviceSeqAction5.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand5 == "on") {
                it.off()
            } else if(dsaCommand5 == "off") {
                it.on()
            }
        }
    }
    if(deviceSeqAction4) {
        deviceSeqAction4.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand4 == "on") {
                it.off()
            } else if(dsaCommand4 == "off") {
                it.on()
            }
        }
    }
    if(deviceSeqAction3) {
        deviceSeqAction3.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand3 == "on") {
                it.off()
            } else if(dsaCommand3 == "off") {
                it.on()
            }
        }
    }
    if(deviceSeqAction2) {
        deviceSeqAction2.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand2 == "on") {
                it.off()
            } else if(dsaCommand2 == "off") {
                it.on()
            }
        }
    }
    if(deviceSeqAction1) {
        deviceSeqAction1.each { it ->
            pauseExecution(actionDelay)
            if(dsaCommand1 == "on") {
                it.off()
            } else if(dsaCommand1 == "off") {
                it.on()
            }
        }
    }
}

def dimmerOnActionHandler() {
    if(logEnable) log.debug "In dimmerOnActionHandler (${state.version})"
    state.fromWhere = "dimmerOn"; state.dimmerDevices = setOnLC; state.onColor = "${colorLC}"; state.onLevel = levelLC; state.onTemp = tempLC
    setLevelandColorHandler()
}

def switchOnReverseActionHandler(data) {
    if(logEnable) log.debug "In switchOnReverseActionHandler (${state.version}) - $data"
    if(data == "aSwitch") { theDevices = setOnLC }
    if(data == "aSwitchesColorChange") { theDevices = switchesToChange }
    theDevices.each { it ->
        currentONOFF = it.currentValue("switch")
        if(logEnable) log.debug "In switchOnReverseActionHandler - ${it.displayName} - ${currentONOFF}"
        if(logEnable) log.debug "In switchOnReverseActionHandler - oldMap: ${state.oldMap}"
        if(currentONOFF == "on") {
            name = (it.displayName).replace(" ","")
            try {
                data = state.oldMap.get(name)
                if(logEnable) log.trace "In switchOnReverseActionHandler - data: ${data}"
                if(data != "null" && data != null) {                
                    def theData = data.split("::")
                    oldStatus = theData[0]
                    hueColor = theData[1]
                    saturation = theData[2]
                    level = theData[3]
                    cTemp = theData[4]
                    cMode = theData[5]
                    theStuff = "good"
                } else {
                    if(logEnable) log.debug "In switchOnReverseActionHandler - Found NO data"
                }
            } catch(e) {
                log.error(getExceptionMessageWithLine(e))
                if(logEnable) log.debug "In switchOnReverseActionHandler - Oops, no DATA - Turning Off (${it})"
                pauseExecution(actionDelay)
                it.off()
            }
            if(theStuff == "good") {
                if(cMode == "CT") {
                    if(it.hasCommand("setColorTemperature") && state.onTemp) {
                        if(logEnable) log.debug "In switchOnReverseActionHandler - setColorTemp - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level} - cTemp: ${cTemp} - trueReverse: ${trueReverse}"
                        if(oldStatus == "off" || trueReverse) {
                            if(logEnable) log.debug "In switchOnReverseActionHandler - setColorTemp - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        } else {
                            pauseExecution(actionDelay)
                            if(level) it.setLevel(level.toInteger())
                            pauseExecution(actionDelay)
                            it.setColorTemperature(cTemp.toInteger())
                        }
                    }
                } else {
                    if(it.hasCommand("setColor") && state.onColor != "No Change") {
                        if(logEnable) log.debug "In switchOnReverseActionHandler - setColor - level: $level"
                        def theValue = [hue: hueColor, saturation: saturation, level: level.toInteger() ?: 99]
                        if(logEnable) log.debug "In switchOnReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - theValue: ${theValue} - trueReverse: ${trueReverse}"
                        if(oldStatus == "off" || trueReverse) {
                            if(logEnable) log.debug "In switchOnReverseActionHandler - setColor - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        } else {
                            pauseExecution(actionDelay)
                            it.setColor(theValue)
                        }
                    } else if(it.hasCommand("setLevel")) {
                        if(logEnable) log.debug "In switchOnReverseActionHandler - setLevel - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level} - trueReverse: ${trueReverse}"
                        if(oldStatus == "off" || trueReverse) {
                            if(logEnable) log.debug "In switchOnReverseActionHandler - setLevel - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        } else {
                            pauseExecution(actionDelay)                   
                            if(level) it.setLevel(level.toInteger())
                        }
                    }
                }
                if(name && state.oldMap) state.oldMap.remove(name)
            } else {
                if(logEnable) log.debug "In switchOnReverseActionHandler - ${it} - No Data - Stuff is BAD - Turning device OFF"
                pauseExecution(actionDelay)
                it.off()
            }
        } else {
            if(logEnable) log.debug "In switchOnReverseActionHandler - ${it} was already off - Nothing to do"
        }
    }
}

def permanentDimHandler() {
    if(logEnable) log.debug "In permanentDimHandler (${state.version})"
    if(setDimmersPerMode) {
        currentMode = location.mode
        setDimmersPerMode.each { it ->
            if(logEnable) log.debug "In permanentDimHandler - Working on $it"
            def theData = "${state.sdPerModeMap}".split(",")
            theData.each { itTwo -> 
                def (theMode, theDevice, theLevel, theTempType, theTemp, theColor,theTime,theTimeType) = itTwo.split(":")
                if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
                theColor = theColor.replace("]","")           
                def cleandevices = theDevice.split(";")
                def cleanOne = "${it}"    
                cleandevices.each { cleanD ->
                    if(cleanD == cleanOne) {
                        if(currentMode == theMode) {
                            if(logEnable || shortLog) log.debug "In permanentDimHandler - Dimming: $it"
                            state.fromWhere = "permanentDimPerHandler"
                            state.dimmerDevices = it
                            state.onColor = theColor
                            if(permanentDimLvl) state.onLevel = permanentDimLvl
                            if(warningDimLvl) state.onLevel = warningDimLvl
                            state.onTemp = theTemp
                            setLevelandColorHandler()
                        }
                    }
                }
            }
        }
    }
    if(setOnLC) {
        if(logEnable) log.debug "In permanentDimHandler - Set Level Dim - Permanent Dim Level: ${permanentDimLvl} - Waring Dim Level: ${warningDimLvl} - Color: ${pdColor} - Temp: ${pdTemp}"
        state.fromWhere = "permanentDimHandler"
        if(permanentDimLvl) state.onLevel = permanentDimLvl
        if(warningDimLvl) state.onLevel = warningDimLvl
        state.onColor = pdColor
        setLevelandColorHandler()
    }
    if(switchesOnAction) {
        switchesOnAction.each { it ->
            if(it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In permanentDimHandler - Set Level Dim (on) - Permanent Dim Level: ${permanentDimLvl} - Waring Dim Level: ${warningDimLvl}"
                pauseExecution(actionDelay)
                theStatus = it.currentValue("switch")
                if(permanentDimLvl) it.setLevel(permanentDimLvl)
                if(warningDimLvl && theStatus == "on") it.setLevel(warningDimLvl)
            }
        }
    }
    if(switchesOffAction) {
        switchesOffAction.each { it ->
            if(it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In permanentDimHandler - Set Level Dim (off) - Permanent Dim Level2: ${permanentDimLvl2} - Waring Dim Level: ${warningDimLvl}"
                pauseExecution(actionDelay)
                theStatus = it.currentValue("switch")
                if(permanentDimLvl2) it.setLevel(permanentDimLvl2)
                if(warningDimLvl && theStatus == "on") it.setLevel(warningDimLvl)
            }
        }
    }
}

def devicesToRefreshActionHandler() {
    devicesToRefresh.each { it ->
        if(logEnable) log.debug "In devicesToRefreshActionHandler - Refreshing ${it}"
        it.refresh()
        pauseExecution(actionDelay)
    }
}

def additionalSwitchesHandler() {
    if(logEnable) log.debug "In additionalSwitchesHandler (${state.version})"
    additionalSwitches.each { it ->
        pauseExecution(actionDelay)
        it.off()
    }
}

def fanActionHandler() {
    fanAction.each { it ->
        if(logEnable) log.debug "In fanActionHandler - Changing ${it} to ${fanSpeed}"
        if(!state.setFanOldMap) {
            state.oldFanMap = [:]
            name = (it.displayName).replace(" ","")
            status = it.currentValue("speed")
            oldStatus = "${status}"
            state.oldFanMap.put(name,oldStatus) 
            state.setFanOldMap = true
        }
        log.warn "oldStatus: ${oldStatus}"
        pauseExecution(actionDelay)
        it.setSpeed(fanSpeed)
    }
}

def fanReverseActionHandler() {
    if(state.oldFanMap) {
        fanAction.each { it ->
            name = (it.displayName).replace(" ","")
            data = state.oldFanMap.get(name)
            def fanSpeed = data
            if(logEnable) log.debug "In fanReverseActionHandler - Changing ${it} to ${fanSpeed}"
            pauseExecution(actionDelay)
            it.setSpeed(fanSpeed)
            state.setFanOldMap = false
        }
    }
}

def garageDoorActionHandler() {
    if(logEnable) log.debug "In garageDoorActionHandler (${state.version})"
    if(garageDoorClosedAction) {
        garageDoorClosedAction.each { it ->
            if(logEnable) log.debug "In garageDoorActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(garageDoorOpenAction) {
        garageDoorOpenAction.each { it ->
            if(logEnable) log.debug "In garageDoorActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
}

def hsmChangeActionHandler() {
    if(logEnable) log.debug "In hsmChangeActionHandler (${state.version}) - Setting to ${setHSM}"
    pauseExecution(actionDelay)
    sendLocationEvent (name: "hsmSetArm", value: "${setHSM}")
}

def lzw45ActionHandler() {
    if(logEnable) log.debug "In lzw45ActionHandler - Sending to ${lzw45Action} - command: ${lzw45Command}"
    state.oldLZW45Switch = lzw45Action.currentValue("switch")
    state.oldLZW45Level = lzw45Action.currentValue("level")
    pauseExecution(actionDelay)
    if(lzw45Command == "on") lzw45Action.on()
    if(lzw45Command == "off") lzw45Action.off()
    if(lzw45Command == "Custom Effect Start") lzw45Action.customEffectStart(cesParam1)
    if(lzw45Command == "Pixel Effect Start") lzw45Action.pixelEffectStart(pesParam1,pesParam2)
    if(lzw45Command == "Start Notification") lzw45Action.startNotification(snParam1)
}

def lzw45ReverseHandler() {
    if(logEnable) log.debug "In lzw45ReverseHandler - Sending to ${lzw45Action}"
    if(lzw45Command == "Custom Effect Start") lzw45Action.customEffectStop()
    if(lzw45Command == "Pixel Effect Start") lzw45Action.pixelEffectStop()
    if(lzw45Command == "Start Notification") lzw45Action.stopNotification()
    pauseExecution(actionDelay)
    if(state.oldLZW45Switch == "on") {
        lzw45Action.setLevel(state.oldLZW45Level)
        pauseExecution(actionDelay)
        lzw45Action.on() 
    } else {
        lzw45Action.setLevel(state.oldLZW45Level)
        pauseExecution(actionDelay)
        lzw45Action.off()
    }
}

def modeChangeActionHandler() {
    if(logEnable) log.debug "In modeChangeActionHandler - Changing mode to ${modeAction}"
    pauseExecution(actionDelay)
    setLocationMode(modeAction)
}

def sendHttpHandler() {
    cookie = ""
    if(state.httpRAN == null) state.httpRAN = false
    if(logEnable) log.debug "In sendHttpHandler - Sending Command to URL: ${xhttpIP}:8080${xhttpCommand}"
    if(xhubSecurity) {
        if(logEnable) log.debug "In sendHttpHandler - Hub Security Enabled - Getting Cookie"      
        httpGet(        // Based on code from @dman2306. Thank you!
            [
                uri: "${xhttpIP}:8080",
                path: "/login", query: [loginRedirect: "/"],
                body: [
                    username: xhubUsername,
                    password: xhubPassword,
                    submit: "Login"
                ]
            ]) { resp -> 
            cookie = resp?.headers?.'Set-Cookie'?.split(';')?.getAt(0)
            //log.info "cookie: ${cookie}"
        }
    }   
    def params = [
        uri: "${xhttpIP}:8080",
        path: xhttpCommand,
        headers: ["Cookie": cookie]
    ]
    theData = ""
    if(xhttpCommand.contains("freeOSMemory")) {       
        httpGet(params) { resp ->
            if(resp.data != null) {
                theData += resp.data
            }
        }
        state.theData = theData
        if(logEnable) log.debug "In sendHttpHandler (freeOSMemory) - theCommand: ${xhttpCommand} - theData: ${state.theData}"
    }
    if(xhttpCommand.contains("freeOSMemory")) {        
        if(setpointRollingAverage && theData) {
            theReadings = state.readings
            if(theReadings == null) theReadings = []
            theReadings.add(0,theData)        
            int maxReadingSize = numOfPoints
            int readings = theReadings.size()
            if(readings > maxReadingSize) theReadings.removeAt(maxReadingSize)
            state.readings = theReadings
            setpointRollingAverageHandler(maxReadingSize)
            if(state.theAverage >= 0) setpointValue = state.theAverage           
            int setpointLow = xMinMemory
            if(setpointValue < setpointLow) {  // Bad
                if(logEnable) log.debug "In sendHttpHandler (freeOSMemory) - Value: ${setpointValue} is LESS THAN setpointLow: ${setpointLow} (Bad)"
                state.setpointLowOK = "no"
                state.setpointOK = true
            } else {  // Good
                if(logEnable) log.debug "In sendHttpHandler (freeOSMemory) - Value: ${setpointValue} is GREATER THAN setpointLow: ${setpointLow} (Good)"
                state.setpointLowOK = "yes"
                state.setpointOK = false
            }
            if(xfreeOSMemLog) log.info "$app.label - Free OS Memory: ${state.theData} - Average: ${setpointValue}"
        } else {
            if(xfreeOSMemLog) log.info "$app.label - Free OS Memory: ${theData}"
        }
    }
}

def actionHttpHandler() {
    cookie = ""
    state.httpRAN = false
    if(logEnable) log.debug "In actionHttpHandler - Sending Command to URL: ${httpIP}:8080${httpCommand}"
    if(hubSecurity) {
        if(logEnable) log.debug "In actionHttpHandler - Hub Security Enabled - Getting Cookie"
        httpGet(        // Based on code from @dman2306. Thank you!
            [
                uri: "${httpIP}:8080",
                path: "/login", query: [loginRedirect: "/"],
                body: [
                    username: hubUsername,
                    password: hubPassword,
                    submit: "Login"
                ]
            ]) {
            resp -> 
            cookie = resp?.headers?.'Set-Cookie'?.split(';')?.getAt(0)
            //log.info "cookie: ${cookie}"
        }
    }   
    def params = [
        uri: "${httpIP}:8080",
        path: httpCommand,
        headers: ["Cookie": cookie]
    ]
    theData = ""
    if(httpCommand.contains("zwaveRepair")) {
        httpGet(params) { resp ->
            log.info "${app.label} - Zwave repair has started"
        }
    }    
    if(httpCommand.contains("reboot") || httpCommand.contains("restart")) {
        httpPost(params) { resp ->
            if(logEnable) log.debug "In actionHttpHandler (post) - theCommand: ${httpCommand} - actionData:<br>${state.actionData}"
        }
    }
}

def switchesOnActionHandler() {
    state.switchesOnMap = [:]
    switchesOnAction.each { it ->
        if(logEnable) log.debug "In switchesOnActionHandler - Turning on ${it}"
        cStatus = it.currentValue('switch')
        name = (it.displayName).replace(" ","")
        state.switchesOnMap.put(name,cStatus)
        pauseExecution(actionDelay)
        if(cStatus == "off") it.on()
    }
}

def switchesOnReverseActionHandler() {
    if(logEnable) log.debug "In switchesOnReverseActionHandler - switchesOnMap: ${state.switchesOnMap}"
    switchesOnAction.each { it ->
        name = (it.displayName).replace(" ","")
        data = state.switchesOnMap.get(name)
        if(logEnable) log.debug "In switchesOnReverseActionHandler - Reversing ${it} - Previous status: ${data} - trueReverse: ${trueReverse}"        
        pauseExecution(actionDelay)
        if(trueReverse) {
            it.off()
        } else {
            if(data) {
                if(data == "off") it.off()
                if(data == "on") it.on()
            } else {
                if(logEnable) log.debug "In switchesOnReverseActionHandler - Reversing ${it} - Previous status: ${data}, so turning off by default"
                it.off()
            }
        }
    }
    state.switchesOnMap = [:]
}

def switchesOffActionHandler() {
    state.switchesOffMap = [:]
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchesOffActionHandler - Turning off ${it}"
        cStatus = it.currentValue('switch')
        name = (it.displayName).replace(" ","")
        state.switchesOffMap.put(name,cStatus)
        pauseExecution(actionDelay)
        if(cStatus == "on") it.off()
    }
}

def switchesOffReverseActionHandler() {
    switchesOffAction.each { it ->
        name = (it.displayName).replace(" ","")
        data = state.switchesOffMap.get(name)
        if(logEnable) log.debug "In switchesOffReverseActionHandler - Reversing ${it} - Previous status: ${data} - trueReverse: ${trueReverse}"
        pauseExecution(actionDelay)
        if(trueReverse) {
            it.on()
        } else {
            if(data) {
                if(data == "off") it.off()
                if(data == "on") it.on()
            } else {
                if(logEnable) log.debug "In switchesOffReverseActionHandler - Reversing ${it} - Previous status: ${data}, so turning on by default"
                it.on()
            }
        }
    }
    state.switchesOffMap = [:]
}

def switchesToggleActionHandler() {
    switchesToggleAction.each { it ->
        status = it.currentValue("switch")
        if(status == "off") {
            if(logEnable) log.debug "In switchesToggleActionHandler - Turning on ${it}"
            pauseExecution(actionDelay)
            it.on()
        } else {
            if(logEnable) log.debug "In switchesToggleActionHandler - Turning off ${it}"
            pauseExecution(actionDelay)
            it.off()
        }
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
        state.onColor = "${colorDn}"
        state.onLevel = state.highestLevel
        if(turnOnBeforeDim) { setLevelandColorHandler() }
        if(minutesDn == 0) return
        seconds = (minutesDn * 60) - 10
        difference = state.highestLevel - targetLevelLow
        state.dimStep1 = (difference / seconds) * 10
        if(logEnable) log.debug "In slowOffHandler - highestLevel: ${state.highestLevel} - targetLevel: ${targetLevelLow} - dimStep1: ${state.dimStep1} - color: ${state.onColor}"
        atLeastOneDnOn = false
        runIn(5,"dimStepDown")
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

def slowOnHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In slowOnHandler (${state.version})"
        state.fromWhere = "slowOn"; state.currentLevel = startLevelUp ?: 5; state.onLevel = startLevelUp ?: 5; state.onColor = "${colorUp}"
        setLevelandColorHandler()
        if(minutesUp == 0) return
        seconds = (minutesUp * 60) - 10
        difference = targetLevelHigh - state.currentLevel
        state.dimStep = (difference / seconds) * 10
        if(logEnable) log.debug "In slowOnHandler - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh} - color: ${state.onColor}"
        atLeastOneUpOn = false
        runIn(5,"dimStepUp")
    }
}

def dimStepUp(theValue) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable && extraLogs) log.debug "-------------------- dimStepUp --------------------"
        if(logEnable && extraLogs) log.debug "In dimStepUp (${state.version})"
        if(state.currentLevel < targetLevelHigh) {
            state.currentLevel = state.currentLevel + state.dimStep
            if(state.currentLevel > targetLevelHigh) { state.currentLevel = targetLevelHigh }
            if(logEnable) log.debug "In dimStepUp - Setting currentLevel: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
            slowDimmerUp.each { it->
                deviceOn = it.currentValue("switch")
                if(logEnable && extraLogs) log.debug "In dimStepUp - ${it} is: ${deviceOn}"
                if(deviceOn == "on") {
                    atLeastOneUpOn = true
                    state.onLevel = state.currentLevel
                    setLevelandColorHandler()
                }
            }
            if(atLeastOneUpOn) {
                runIn(10,"dimStepUp")
            } else {
                log.info "${app.label} - All devices are turned off"
            }
        } else {
            if(logEnable) log.info "In dimStepUp - Current Level: ${state.currentLevel.toInteger()} has reached targetLevel: ${targetLevelHigh}"
            if(logEnable) log.debug "-------------------- End dimStepUp --------------------"
        }
    }
}

def dimStepDown() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable && extraLogs) log.debug "-------------------- dimStepDown --------------------"
        if(logEnable && extraLogs) log.debug "In dimStepDown (${state.version})"
        atLeastOneDnOn = false
        if(state.highestLevel > targetLevelLow) {
            state.highestLevel = state.highestLevel - state.dimStep1
            if(state.highestLevel < targetLevelLow) { state.highestLevel = targetLevelLow }
            if(logEnable && extraLogs) log.debug "In dimStepDown - Starting Level: ${state.highestLevel} - targetLevelLow: ${targetLevelLow}"
            slowDimmerDn.each { it->
                deviceOn = it.currentValue("switch")
                int cLevel = it.currentValue("level")
                int wLevel = state.highestLevel
                if(logEnable) log.debug "In dimStepDown - ${it} is: ${deviceOn} - cLevel: ${cLevel} - wLevel: ${wLevel}"
                if(deviceOn == "on") {
                    atLeastOneDnOn = true
                    if(wLevel <= cLevel) { 
                        state.onLevel = wLevel
                        setLevelandColorHandler()
                    }
                }
            }
            if(atLeastOneDnOn) {
                runIn(10,"dimStepDown")
            } else {
                if(logEnable) log.info "${app.label} - All devices are turned off"
            }
        } else {
            if(dimDnOff) slowDimmerDn.off()
            if(logEnable) log.info "In dimStepDown - Current Level: ${state.highestLevel.toInteger()} has reached targetLevel: ${targetLevelLow}"
            if(logEnable) log.debug "-------------------- End dimStepDown --------------------"
        } 
    }
}

def thermostatActionHandler() {
    thermostatAction.each { it ->
        if(setThermostatFanMode) {
            if(logEnable) log.debug "In thermostatActionHandler - Fan Mode - Setting ${it} to ${setThermostatFanMode}"
            pauseExecution(actionDelay)
            it.setThermostatFanMode(setThermostatFanMode)
        }
        if(setThermostatMode) {
            if(logEnable) log.debug "In thermostatActionHandler - Thermostat Mode - Setting ${it} to ${setThermostatMode}"
            pauseExecution(actionDelay)
            it.setThermostatMode(setThermostatMode)
        }
        if(coolingSetpoint) {
            if(logEnable) log.debug "In thermostatActionHandler - Cooling Setpoint - Setting ${it} to ${coolingSetpoint}"
            pauseExecution(actionDelay)
            it.setCoolingSetpoint(coolingSetpoint)
        }
        if(heatingSetpoint) {
            if(logEnable) log.debug "In thermostatActionHandler - Heating Setpoint - Setting ${it} to ${heatingSetpoint}"
            pauseExecution(actionDelay)
            it.setHeatingSetpoint(heatingSetpoint)
        }
    }
}

def valveActionHandler() {
    if(logEnable) log.debug "In valveActionHandler (${state.version})"
    if(valveClosedAction) {
        valveClosedAction.each { it ->
            if(logEnable) log.debug "In valveActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(valveOpenAction) {
        valveOpenAction.each { it ->
            if(logEnable) log.debug "In valveActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
}

def windowBlindActionHandler() {
    if(logEnable) log.debug "In windowBlindActionHandler (${state.version})"
    if(blindCommand == "open") {
        blindAction.each { it ->
            if(logEnable) log.debug "In windowBlindActionHandler - Opening ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
    if(blindCommand == "close") {
        blindAction.each { it ->
            if(logEnable) log.debug "In windowBlindActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(blindCommand == "set position") {
        blindAction.each { it ->
            if(logEnable) log.debug "In windowBlindActionHandler - Setting Position to ${it}"
            pauseExecution(actionDelay)
            it.setPosition(blindPos)
        }
    }
    if(blindCommand == "tilt") {
        blindAction.each { it ->
            if(logEnable) log.debug "In windowBlindActionHandler - Setting Tilt Level to ${it}"
            pauseExecution(actionDelay)
            it.setTiltLevel(blindTilt)
        }
    }
}

def windowShadeActionHandler() {
    if(logEnable) log.debug "In windowShadeActionHandler (${state.version})"
    if(shadeCommand == "open") {
        shadeAction.each { it ->
            if(logEnable) log.debug "In windowShadeActionHandler - Opening ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
    if(shadeCommand == "close") {
        shadeAction.each { it ->
            if(logEnable) log.debug "In windowShadeActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(shadeCommand == "set position") {
        shadeAction.each { it ->
            if(logEnable) log.debug "In windowShadeActionHandler - Setting Position to ${it}"
            pauseExecution(actionDelay)
            it.setPosition(shadePos)
        }
    }
}

def contactActionHandler() {
    if(logEnable) log.debug "In contactActionHandler (${state.version})"
    if(contactClosedAction) {
        contactClosedAction.each { it ->
            if(logEnable) log.debug "In contactActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(contactOpenAction) {
        contactOpenAction.each { it ->
            if(logEnable) log.debug "In contactActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
}

def contactReverseActionHandler() {
    if(logEnable) log.debug "In contactReverseActionHandler (${state.version})"
    if(contactClosedAction) {
        contactClosedAction.each { it ->
            if(logEnable) log.debug "In contactReverseActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
    if(contactOpenAction) {
        contactOpenAction.each { it ->
            if(logEnable) log.debug "In contactReverseActionHandler - Close ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
}
// ********** End Actions **********

def messageHandler() {
    if(logEnable) log.debug "In messageHandler (${state.version}) - doMessage: ${state.doMessage}"
    if(msgRepeatContact) { subscribe(msgRepeatContact, "contact", repeatCheck) }    
    if(msgRepeatSwitch) { subscribe(msgRepeatSwitch, "switch", repeatCheck) }
    if(msgRepeat) {
        state.msgRepMax = msgRepeatMax ?: 2
        if(state.repeatCount == null) state.repeatCount = 0
        if(logEnable) log.debug "In messageHandler (${state.version}) - repeatCount: ${state.repeatCount} - msgRepeatMax: ${state.msgRepMax}"
        if(state.repeatCount > state.msgRepMax) { state.doMessage = false }
        state.repeatCount = state.repeatCount + 1
    }
    if(state.doMessage) {   
        if(triggerType) {
            if(triggerType.contains("xBattery") || triggerType.contains("xEnergy") || triggerType.contains("xHumidity") || triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp") || deviceORsetpoint) {
                if(logEnable) log.debug "In messageHandler (setpoint) - setpointHighOK: ${state.setpointHighOK} - setpointLowOK: ${state.setpointLowOK} - setpointBetweenOK: ${state.setpointBetweenOK}"
                if(messageH || messageL || messageB) {
                    if(state.setpointHighOK == "no") theMessage = "${messageH}"
                    if(state.setpointLowOK == "no") theMessage = "${messageL}"
                    if(state.setpointBetweenOK == "no") theMessage = "${messageB}"
                } else if(wmessageH || wmessageL || wmessageB) {
                    if(state.setpointHighOK == "no") theMessage = "${wmessageH}"
                    if(state.setpointLowOK == "no") theMessage = "${wmessageL}"
                    if(state.setpointBetweenOK == "no") theMessage = "${wmessageB}"
                }
            } else {
                if(message) {
                    theMessage = message
                } else if(wmessage) {
                    theMessage = wmessage
                }
            }
            if(theMessage == null) {
                if(message) {
                    theMessage = message
                } else if(wmessage) {
                    theMessage = wmessage
                }
            }
            if(logEnable && extraLogs) log.debug "In messageHandler - Random - raw message: ${theMessage}"
            def values = "${theMessage}".split(";")
            vSize = values.size()
            count = vSize.toInteger()
            def randomKey = new Random().nextInt(count)
            msg1 = values[randomKey]
            if(logEnable && extraLogs) log.debug "In messageHandler - Random - msg1: ${msg1}" 
        }
        state.message = msg1
        if(state.message) { 
            if (state.message.contains("%whatHappened%")) {state.message = state.message.replace('%whatHappened%', state.whatHappened)}
            if (state.message.contains("%whoHappened%")) {state.message = state.message.replace('%whoHappened%', state.whoHappened)}
            if (state.message.contains("%whoUnlocked%")) {
                if(state.whoUnlocked) {
                    state.message = state.message.replace('%whoUnlocked%', state.whoUnlocked)
                }
            }
            if (state.message.contains("%whoLocked%")) {
                if(state.whoLocked) {
                    state.message = state.message.replace('%whoLocked%', state.whoLocked)
                }
            }
            if (state.message.contains("%setPointHigh%")) {
                spHigh = state.setpointHigh.toString()
                state.message = state.message.replace('%setPointHigh%', spHigh)
            }
            if (state.message.contains("%setPointLow%")) {
                spLow = state.setpointLow.toString()
                state.message = state.message.replace('%setPointLow%', spLow)
            }
            if (state.message.contains("%time%")) {
                currentDateTime()
                state.message = state.message.replace('%time%', state.theTime)
            }
            if (state.message.contains("%time1%")) {
                currentDateTime()
                state.message = state.message.replace('%time1%', state.theTime1)
            }
            if (state.message.contains("%lastDirection%")) {state.message = state.message.replace('%lastDirection%', state.lastDirection)}
            if (state.message.contains("%iCalValue%")) {state.message = state.message.replace('%iCalValue%', state.currentIcalValue)}
            if(state.message) {
                if(logEnable) log.debug "In messageHandler - message: ${state.message}"
                if(useSpeech) letsTalk(state.message)
                if(sendPushMessage) pushHandler(state.message)
                if(useWebOS) useWebOSHandler(state.message)
            } else {
                if(logEnable) log.debug "In messageHandler - No message was found."
            }
        }
        if(msgRepeat) {
            repeatSeconds = msgRepeatMinutes * 60
            runIn(repeatSeconds, "messageHandler")
        }
    } else {
        if(logEnable) log.debug "In messageHandler - Repeat is now off"
        unsubscribe(msgRepeatContact)
        unsubscribe(msgRepeatSwitch)
        state.repeatCount = 0
        state.doMessage = false
    }
}

def repeatCheck(evt) {
    if(logEnable) log.debug "In repeatCheck (${state.version}) - Repeat Check was triggered, Repeat Off"
    unsubscribe(msgRepeatContact)
    unsubscribe(msgRepeatSwitch)
    state.repeatCount = 0
    state.doMessage = false
}

def theFlasherHandler() {
    if(logEnable) log.debug "In theFlasherHandler (${state.version})"
    flashData = "flash"
    if(logEnable) log.debug "In theFlasherHandler - Sending: ${flashData}"
    theFlasherDevice.sendPreset(flashData)    
}

def currentDateTime() {
    if(logEnable && extraLogs) log.debug "In currentDateTime (${state.version})"
    Date date = new Date()
    String datePart = date.format("dd/MM/yyyy")
    String timePart = date.format("HH:mm")
    String timePart1 = date.format("h:mm a")
    state.theTime = timePart		// 24 h
    state.theTime1 = timePart1		// AM PM
    if(logEnable) log.debug "In currentDateTime - ${state.theTime}"
}

def checkSunHandler() {
    if(timeDaysType == null) timeDaysType = ""
    if(logEnable) log.debug "********** In checkSunHandler (${state.version}) - timeDaysType: ${timeDaysType} - **********"
    if(timeDaysType.contains("tSunsetSunrise") || timeDaysType.contains("tSunset") || timeDaysType.contains("tSunrise")) {
        if(offsetSunset == 99) {
            sunsetHigh = sunsetDelayHigh ?: 5
            sunsetLow = sunsetDelayLow ?: 1
            try {
                sunsetDelay = Math.abs(new Random().nextInt() % (sunsetHigh - sunsetLow)) + sunsetLow
            } catch(e) { 
                // nothing
            }
            theOffsetSunset = sunsetDelay ?: 1
        } else {
            theOffsetSunset = offsetSunset ?: 1
        }       
        if(offsetSunrise == 99) {
            sunsetHigh = sunsetDelayHigh ?: 5
            sunsetLow = sunsetDelayLow ?: 1
            try {
                sunriseDelay = Math.abs(new Random().nextInt() % (sunriseHigh - sunriseLow)) + sunriseLow
            } catch(e) { 
                // nothing
            }
            theOffsetSunrise = sunriseDelay ?: 1
        } else {
            theOffsetSunrise = offsetSunrise ?: 1
        }       
        if(fromSun) {
            sunriseTime = getSunriseAndSunset().sunrise
        } else {
            sunriseTime = (getSunriseAndSunset().sunrise)+1
        }
        sunsetTime = getSunriseAndSunset().sunset
        oSunset = theOffsetSunset.toInteger()
        oSunrise = theOffsetSunrise.toInteger()
        if(logEnable) {
            log.debug "In checkSunHandler - Sunrise: ${sunriseTime} - Sunset: ${sunsetTime}"
            log.debug "In checkSunHandler - oSunrise: ${oSunrise} - oSunset: ${oSunset}"
        }
        if(setBeforeAfter) {
            use( TimeCategory ) { nextSunsetOffset = sunsetTime + oSunset.minutes }
        } else {
            use( TimeCategory ) { nextSunsetOffset = sunsetTime - oSunset.minutes }
        }    
        if(riseBeforeAfter) {          
            use( TimeCategory ) { nextSunriseOffset = sunriseTime + oSunrise.minutes }
        } else {
            use( TimeCategory ) { nextSunriseOffset = sunriseTime - oSunrise.minutes }
        }
        cDate = new Date()
        if(logEnable) log.debug "In checkSunHandler - NEW Sunrise: ${nextSunriseOffset} - NEW Sunset: ${nextSunsetOffset}"
        if(logEnable) log.debug "In checkSunHandler - Date to Check: ${cDate}"
        if(triggerType.contains("tTimeDays")) {
            if(fromSun) {    // Sunrise to Sunset                
                state.timeBetweenSun = timeOfDayIsBetween(nextSunriseOffset, nextSunsetOffset, cDate, location.timeZone)
                if(logEnable) log.debug "In checkSunHandler - timeBetweenSun: ${state.timeBetweenSun}"
            } else {        // Sunset to Sunrise
                state.timeBetweenSun = timeOfDayIsBetween(nextSunsetOffset, nextSunriseOffset, cDate, location.timeZone)
                if(logEnable) log.debug "In checkSunHandler - timeBetweenSun: ${state.timeBetweenSun}"
            }
        }        
        if(fromSun || timeDaysType.contains("tSunrise")) {                    // Sunrise to Sunset
            if(!sunsetSunriseMatchConditionOnly) {
                schedule(nextSunriseOffset, "runAtTime1")
                if(sunriseEndTime) schedule(sunriseEndTime, "runAtTime2")
                if(timeDaysType.contains("tSunsetSunrise")) schedule(nextSunsetOffset, "runAtTime2")
            }
        } else if(!fromSun || timeDaysType.contains("tSunset")) {             // Sunset to Sunrise
            if(!sunsetSunriseMatchConditionOnly) {
                schedule(nextSunsetOffset, "runAtTime1")
                if(sunsetEndTime) schedule(sunsetEndTime, "runAtTime2")
                if(timeDaysType.contains("tSunsetSunrise")) schedule(nextSunriseOffset, "runAtTime2") 
            }
        } else {
            state.timeBetweenSun = true
        }
        schedule("0 5 12 ? * * *", checkSunHandler)
        if(state.timeBetweenSun) {
            if(logEnable) log.debug "In checkSunHandler - timeBetweenSun PASSED - timeBetweenSun: ${state.timeBetweenSun}"
        } else {
            if(logEnable) log.warn "In checkSunHandler - timeBetweenSun FAILED - timeBetweenSun: ${state.timeBetweenSun}"
        }
    } else {
        state.timeBetweenSun = true
        if(logEnable) log.debug "In checkSunHandler - sunrise/sunset NOT being used - timeBetweenSun: ${state.timeBetweenSun}"
    }
    state.timeSunset = nextSunsetOffset
    state.timeSunrise = nextSunriseOffset
    if(logEnable) log.debug "********** In checkSunHandler - timeBetweenSun: ${state.timeBetweenSun} **********"
}

def runAtTime1() {
    if(logEnable) log.debug "In runAtTime1 (${state.version}) - Run"
    startTheProcess("run")
}

def runAtTime2() {
    if(logEnable) log.debug "In runAtTime2 (${state.version}) - Reverse"
    startTheProcess("reverse")
}

def startTimeBetween() {
    if(logEnable) log.debug "In startTimeBetween (${state.version}) - Start"
    state.betweenTime = true
    runAtTime1()
}

def endTimeBetween() {
    if(logEnable) log.debug "In endTimeBetween (${state.version}) - End"
    state.betweenTime = false
    if(!timeBetweenRestriction) { runAtTime2() }
}

def certainTime() {
    if(logEnable) log.debug "In certainTime (${state.version})"  
    startTheProcess("certainTime")
}

def dayOfTheWeekHandler() {
    if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
    state.daysMatch = null
    Date date = new Date() 
    if(useDayMonthYear) {
        state.numberDay = date[Calendar.DAY_OF_YEAR]
    } else {
        state.numberDay = date[Calendar.DAY_OF_MONTH]
    }
    state.numberWeek = date[Calendar.WEEK_OF_YEAR]
    if(days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        def dayCheck = days.contains(day)
        if(dayCheck) {
            state.daysMatch = true
        } else {
            state.daysMatch = false
        }
    }
    if(evenDays) {
        if(useDayWeek) {
            nDay = state.numberWeek.toInteger()
        } else {
            nDay = state.numberDay.toInteger()
        }
        if(nDay % 2 == 0) { 
            state.daysMatch = true
        } else {
            state.daysmatch = false
        }
        hmmm = nDay % 2
        if(logEnable) log.debug "In dayOfTheWeekHandler - Even - nDay: ${nDay} - ${state.daysMatch} (${hmmm})"
    } 
    if(oddDays) {
        if(useDayWeek) {
            nDay = state.numberWeek.toInteger()
        } else {
            nDay = state.numberDay.toInteger()
        }
        if(nDay % 2 == 0) { 
            state.daysMatch = false
        } else {
            state.daysmatch = true
        }
        hmmm = nDay % 2
        if(logEnable) log.debug "In dayOfTheWeekHandler - Odd - nDay: ${nDay} - ${state.daysMatch} (${hmmm})"
    }
    if(state.daysMatch == null) state.daysMatch = true
    if(state.daysMatch) {
        if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch PASSED - daysMatch: ${state.daysMatch}"
    } else {
        if(logEnable) log.warn "In dayOfTheWeekHandler - daysMatch FAILED - daysMatch: ${state.daysMatch}"
    }
}

def modeHandler() {
    if(logEnable) log.debug "In modeHandler (${state.version})"
    if(modeEvent) {
        state.modeMatch = false
        theValue = location.mode
        modeEvent.each { it ->
            if(logEnable) log.debug "In modeHandler - Checking current Mode: ${theValue} vs: ${it}"
            if(theValue == it) modeCheck = true
            if(modeCondition) {
                if(!modeCheck) {
                    state.modeMatch = true
                }
            } else {
                if(modeCheck) {
                    state.modeMatch = true
                }
            }
        }
    } else {
        state.modeMatch = true
        if(logEnable) log.debug "In modeHandler - Mode NOT being used - modeMatch: ${state.modeMatch}"
    }
    if(state.modeMatch) {
        if(logEnable) log.debug "In modeHandler - modeMatch PASSED - modeMatch: ${state.modeMatch}"
    } else {
        if(logEnable) log.warn "In modeHandler - modeMatch FAILED - modeMatch: ${state.modeMatch}"
    }
}
// *****  End Time Handlers *****

def setLevelandColorHandler(newData) {  
    if(state.onColor == null || state.onColor == "null" || state.onColor == "") state.onColor = "No Change"
    if(state.onLevel == "NA") state.onLevel = null
    if(state.onTemp == "NA") state.onTemp = null
    if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, color: ${state.onColor} - temp: ${state.onTemp} - onLevel: ${state.onLevel}"
    switch(state.onColor) {
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
            saturation = 100
            break;
        case "Green":
            hueColor = 39
            saturation = 100
            break;
        case "Yellow":
            hueColor = 25
            saturation = 100
            break;
        case "Orange":
            hueColor = 10
            saturation = 100
            break;
        case "Purple":
            hueColor = 75
            saturation = 100
            break;
        case "Pink":
            hueColor = 83
            saturation = 100
            break;
        case "Red":
            hueColor = 100
            saturation = 100
            break;
        case "Custom Color 1":
            hueColor = cc1hue.toInteger()
            saturation = cc1sat.toInteger()
            break;
        case "Custom Color 2":
            hueColor = cc2hue.toInteger()
            saturation = cc2sat.toInteger()
            break;
        case "Custom Color 3":
            hueColor = cc3hue.toInteger()
            saturation = cc3sat.toInteger()
            break;
        case "Custom Color 4":
            hueColor = cc4hue.toInteger()
            saturation = cc4sat.toInteger()
            break;
        case "Custom Color 5":
            hueColor = cc5hue.toInteger()
            saturation = cc5sat.toInteger()
            break;
        case "Custom Color 6":
            hueColor = cc6hue.toInteger()
            saturation = cc6sat.toInteger()
            break;
    }
    onLevel = state.onLevel
    if(logEnable) log.debug "In setLevelandColorHandler - 1 - hue: ${hueColor} - saturation: ${saturation} - onLevel: ${onLevel}"
    // if(it.getDataValue("model")) {}
    if(onLevel) onLevel = onLevel.toInteger()
    value = [hue: hueColor, saturation: saturation, level: onLevel]
    if(state.oldMap == null) state.oldMap = [:]
    theSetOldMap = state.oldMap.toString().replace("[","").replace("]","")
    theMap = theSetOldMap.split(",")
    if(logEnable) log.debug "In setLevelandColorHandler - theMap: ${theMap}"
    
    if(state.fromWhere == "colorChangeHandler") {		
        if(logEnable) log.debug "In setLevelandColorHandler - colorChangeHandler - newData: ${newData} - value: ${value}"
        alreadyThere = false
        try {
            if(state.oldMap == [:]) {
                // nothing
            } else {
                sData = newData.toString().replace(" ","")
                data = state.oldMap.get(sData)
                if(data) {
                    if(logEnable) log.debug "In setLevelandColorHandler - colorChangeHandler - Data Found!"
                    alreadyThere = true
                }
            }
        } catch(e) {
            state.oldMap = [:]
        }
        if(logEnable) log.debug "In setLevelandColorHandler - colorChangeHandler - alreadyThere: ${alreadyThere}"
        if(!alreadyThere) {
            oldHueColor = newData.currentValue("hue")
            oldSaturation = newData.currentValue("saturation")
            oldLevel = newData.currentValue("level")
            oldColorTemp = newData.currentValue("colorTemperature")
            oldColorMode = newData.currentValue("colorMode")
            name = (newData.displayName).replace(" ","")
            status = newData.currentValue("switch")
            oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}::${oldColorTemp}::${oldColorMode}"
            state.oldMap.put(name,oldStatus) 
            if(logEnable) log.debug "In setLevelandColorHandler - colorChangeHandler - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
        }
        if(logEnable) log.debug "In setLevelandColorHandler - colorChangeHandler - Setting ${newData} to: ${state.onColor} (${value})"
        currentStatus = newData.currentValue("switch")
        if(currentStatus == "off") { newData.on() }
        pauseExecution(actionDelay)
        newData.setColor(value)      
    }
    
    if(state.fromWhere == "switchesPerMode") {
        if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - Working on: ${newData}"
        theSDPM = newData.toString().replace(" ","")   
        alreadyThere = false
        try {
            if(state.oldMap == [:]) {
                // Do nothing
            } else {
                theMap.each { it ->
                    itValue = it.split(":")
                    tDevice = itValue[0]
                    if(tDevice.startsWith(" ") || tDevice.startsWith("[")) tDevice = tDevice.substring(1)
                    if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - sPDM: ${theSDPM} - tDevice: ${tDevice}"
                    if(theSDPM == tDevice) {
                        alreadyThere = true
                    }
                }
            }
        } catch(e) {
            state.oldMap = [:]
        }
        if(logEnable) log.debug "In setLevelandColorHandler - alreadyThere: ${alreadyThere}"
        theDevice = newData
        if(!alreadyThere) {
            oldHueColor = theDevice.currentValue("hue")
            oldSaturation = theDevice.currentValue("saturation")
            oldLevel = theDevice.currentValue("level")
            oldColorTemp = theDevice.currentValue("colorTemperature")
            oldColorMode = theDevice.currentValue("colorMode")
            name = (theDevice.displayName).replace(" ","")
            status = theDevice.currentValue("switch")
            oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}::${oldColorTemp}::${oldColorMode}"
            state.oldMap.put(name,oldStatus) 
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setColor - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
        }
        if(theDevice.hasCommand('setColorTemperature') && state.onTemp) {
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setColorTemp - $theDevice.displayName, setColorTemp($state.onTemp)"
            pauseExecution(actionDelay)
            theDevice.setLevel(onLevel as Integer ?: 99)
            pauseExecution(actionDelay)
            if(state.onTemp) {
                onTemp = state.onTemp.toInteger()
                theDevice.setColorTemperature(onTemp)
            }
        } else if(theDevice.hasCommand('setColor') && state.onColor != "No Change") {
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setColor - $theDevice.displayName, setColor: $value"
            currentStatus = theDevice.currentValue("switch")
            if(currentStatus == "off") { theDevice.on() }
            pauseExecution(actionDelay)
            theDevice.setColor(value)  
        } else if(theDevice.hasCommand('setLevel')) {
            if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - switchesPerMode - setLevel - $it.displayName, setLevel: $value"
            pauseExecution(actionDelay)
            theDevice.setLevel(onLevel as Integer ?: 99)
        } else {
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - ${theDevice.displayName}, on()"
            pauseExecution(actionDelay)
            theDevice.on()
        }
    }
    
    if(state.fromWhere == "dimmerOn") {
        if(logEnable) log.debug "In setLevelandColorHandler - dimmerOn/switchesPerMode"
        state.dimmerDevices.each { it ->
            alreadyThere = false
            try {
                if(state.oldMap == [:]) {
                    // Do nothing
                } else {
                    theMap.each { olds ->
                        itValue = olds.split(":")
                        tDevice = itValue[0]
                        if(tDevice.startsWith(" ") || tDevice.startsWith("[")) tDevice = tDevice.substring(1)
                        if(logEnable) log.debug "In setLevelandColorHandler - dimmerOn/switchesPerMode - it: ${it} - tDevice: ${tDevice}"
                        if(it == tDevice) {
                            alreadyThere = true
                        }
                    }
                }
            } catch(e) {
                state.oldMap = [:]
            }
            if(logEnable) log.debug "In setLevelandColorHandler - dimmerOn/switchesPerMode - Working on ${it} - alreadyThere: ${alreadyThere}"
            if(logEnable) log.debug "In setLevelandColorHandler - 2 - hue: ${hueColor} - saturation: ${saturation} - onLevel: ${onLevel}"
            if(!alreadyThere) {
                oldHueColor = it.currentValue("hue")
                oldSaturation = it.currentValue("saturation")
                oldLevel = it.currentValue("level")
                oldColorTemp = it.currentValue("colorTemperature")
                oldColorMode = it.currentValue("colorMode")
                name = (it.displayName).replace(" ","")
                status = it.currentValue("switch")
                oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}::${oldColorTemp}::${oldColorMode}"
                state.oldMap.put(name,oldStatus) 
                if(logEnable) log.debug "In setLevelandColorHandler - setColor - OLD STATUS - ${name} - ${oldStatus}"
            }
            if(lcColorTemp) {
                if(it.hasCommand('setColorTemperature') && state.onTemp) {
                    if(logEnable) log.debug "In setLevelandColorHandler - setColorTemp - NEW VALUE - ${it.displayName} - setColorTemp($state.onTemp)"
                    pauseExecution(actionDelay)
                    it.setLevel(onLevel as Integer ?: 99)
                    pauseExecution(actionDelay)
                    if(state.onTemp) {
                        onTemp = state.onTemp.toInteger()
                        it.setColorTemperature(onTemp)
                    }
                }
            } else {
                if(it.hasCommand('setColor') && state.onColor != "No Change") {
                    if(logEnable) log.debug "In setLevelandColorHandler - setColor - NEW VALUE - ${it.displayName} - setColor: ${value}"
                    currentStatus = it.currentValue("switch")
                    if(currentStatus == "off") { it.on() }
                    pauseExecution(actionDelay)
                    it.setColor(value)
                } else if (it.hasCommand('setLevel')) {
                    if(logEnable) log.debug "In setLevelandColorHandler - setLevel - NEW VALUE - ${it.displayName} - setLevel: ${value}"
                    pauseExecution(actionDelay)
                    it.setLevel(onLevel as Integer ?: 99)
                    if(logEnable) log.debug "In setLevelandColorHandler - setLevel - *** Just setLevel on ${it.displayName} to ${onLevel} ***"
                } else {
                    if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - $it.displayName, on()"
                    pauseExecution(actionDelay)
                    it.on()
                }
            }
        }
    }

    if(state.fromWhere == "slowOn") {
        slowDimmerUp.each {
            if (it.hasCommand('setColor')) {
                if(logEnable) log.debug "In setLevelandColorHandler - SlowOn (setColor) - $it.displayName, setColor: $value"
                it.setColor(value)
            } else if (it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In setLevelandColorHandler - SlowOn (setLevel) - $it.displayName, setLevel: $onLevel"
                it.setLevel(onLevel as Integer ?: 10)
            } else {
                log.info "In setLevelandColorHandler - $it.displayName, is not dimmable."
                it.on()
            }
        }
    }

    if(state.fromWhere == "slowOff") {
        slowDimmerDn.each {
            if (it.hasCommand('setColor')) {
                if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - SlowOff (setColor) - $it.displayName, setColor: $value"
                it.setColor(value)
            } else if (it.hasCommand('setLevel')) {
                if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - SlowOff (setLevel) - $it.displayName, setLevel: $level"
                it.setLevel(onLevel as Integer ?: 99)
            } else {
                log.info "In setLevelandColorHandler - $it.displayName, is not dimmable."
                it.on()
            }
        }
    }

    if(state.fromWhere == "permanentDimHandler") {
        setOnLC.each {
            theStatus = it.currentValue("switch")
            if(pdColor && it.hasCommand('setColor')) {
                if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - PD - $it.displayName, setColor: $value"
                it.setColor(value)
            } else if(pdTemp && it.hasCommand('setColorTemperature')) {
                if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - PD - $it.displayName, setColorTemp: $pdTemp, level: ${permanentDimLvl} (or warningLvl: ${warningDimLvl})"
                pauseExecution(actionDelay)
                if(permanentDimLvl) { it.setLevel(permanentDimLvl) }
                if(warningDimLvl && theStatus == "on") { it.setLevel(warningDimLvl) }
                pauseExecution(actionDelay)
                it.setColorTemperature(pdTemp)
            } else {
                if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - PD - $it.displayName, setLevel: $permanentDimLvl (or warningLvl: ${warningDimLvl})"
                pauseExecution(actionDelay)
                if(permanentDimLvl) { it.setLevel(permanentDimLvl) }
                if(warningDimLvl && theStatus == "on") { it.setLevel(warningDimLvl) }
            }
        }
    }

    if(state.fromWhere == "permanentDimPerHandler") {
        currentMode = location.mode
        masterDimmersPerMode.each { itOne ->
            def theData = "${state.sdPerModeMap}".split(",")        
            theData.each { itTwo -> 
                def (theMode, theDevice, theLevel, theTempType, theTemp, theColor) = itTwo.split(":")
                if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
                def modeCheck = currentMode.contains(theMode)
                if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - currentMode: ${currentMode} - modeCheck: ${modeCheck}"
                if(modeCheck) {
                    theColor = theColor.replace("]","")           
                    def cleanOne = "${itOne}"
                    def cleanTwo = theDevice.replace("[","").replace("]","").split(";")
                    cleanTwo.each { itThree ->
                        if(itThree.startsWith(" ") || itThree.startsWith("[")) itThree = itThree.substring(1)
                        if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - Comparing cleanOne: ${cleanOne} - itThree: ${itThree}"
                        if(cleanOne == itThree) {
                            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - MATCH - Working on: ${itOne}"
                            theDevice = itOne
                            theStatus = theDevice.currentValue("switch")
                            if(lcColorTemp) {
                                if(theDevice.hasCommand('setColorTemperature') && state.onColor == "NA") { 
                                    if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - switchesPerMode - $it.displayName, setColorTemp: $pdTemp, level: ${permanentDimLvl} (or warningLvl: ${warningDimLvl})"
                                    pauseExecution(actionDelay)
                                    if(permanentDimLvl) { theDevice.setLevel(permanentDimLvl) }
                                    if(warningDimLvl && theStatus == "on") { theDevice.setLevel(warningDimLvl) }
                                    pauseExecution(actionDelay)
                                    theDevice.setColorTemperature(pdTemp)
                                }
                            } else {      
                                if(theDevice.hasCommand('setColor')) {
                                    if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - switchesPerMode - $it.displayName, setColor: $value"
                                    pauseExecution(actionDelay)
                                    theDevice.setColor(value) 
                                } else {
                                    if(logEnable && extraLogs) log.debug "In setLevelandColorHandler - switchesPerMode - $it.displayName, setLevel: $permanentDimLvl (or warningLvl: ${warningDimLvl})"
                                    pauseExecution(actionDelay)
                                    if(permanentDimLvl) { theDevice.setLevel(permanentDimLvl) }
                                    if(warningDimLvl && theStatus == "on") { theDevice.setLevel(warningDimLvl) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    //log.debug "In setLevelandColorHandler - end - oldMap: ${state.oldMap}"
}

def getLockCodeNames(myDev) {  // Special thanks to Bruce @bravenel for this code
    def list = []
    myDev.each {
        list += getLockCodesFromDevice(it).tokenize(",")
    }
    lista = list.flatten().unique{ it }
    listb = lista.sort { a, b -> a <=> b }
    return listb
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

def sendSettingsToParentHandler() {
    if(logEnable) log.debug "In sendSettingsToParentHandler (${state.version})"
    parent.getSettingsFromChild(mySettings)
}

def globalVariablesHandler(data) {
    if(data) { state.gvMap = data }
    if(globalVariableEvent) startTheProcess("global")
}

def versionIsGoodHandler(data) {
    if(data) { state.verIsGood = data }
}

def cronExpressionsHandler(data) {
    if(data) { state.ceMap = data }
}

def sdPerModeHandler(data) {
    if(logEnable) log.debug "In sdPerModeHandler (${state.version}) - data: ${data}"
    def (theType, newData) = data.split(";")
    if(state.sdPerModeMap == null) state.sdPerModeMap = [:]
    theMode = sdPerModeName.toString()
    if(theType == "add") {
        if(logEnable) log.debug "In sdPerModeHandler - ADD"
        if(sdPerModeLevel == null) sdPerModeLevel = "NA"
        if(sdPerModeTempType == null) theTempType = "false"
        if(sdPerModeTemp == null) sdPerModeTemp = "NA"        
        if(sdPerModeColor == null) sdPerModeColor = "NA"
        if(sdPerModeTime == null) sdPerModeTime = "NA"
        if(sdReverseTimeType == null) sdReverseTimeType = "false"
        dpm = setDimmersPerMode.toString().replace("[","").replace("]","").replace(", ",";")
        theValue = "${dpm}:${sdPerModeLevel}:${sdPerModeTempType}:${sdPerModeTemp}:${sdPerModeColor}:${sdPerModeTime}:${sdReverseTimeType}"
        if(logEnable) log.debug "mode: ${theMode} - theValue: ${theValue}"
        state.sdPerModeMap.put(theMode,theValue)
    } else if(theType == "del") {
        if(logEnable) log.debug "In sdPerModeHandler - DELETE"
        state.sdPerModeMap.remove(theMode)
    } else if(theType == "clear") {
        if(logEnable) log.debug "In sdPerModeHandler - CLEAR"
        state.sdPerModeMap = [:]  
    } else if(theType == "rebuild") {
        if(logEnable) log.debug "In sdPerModeHandler - REBUILD"
        def theData = "${state.sdPerModeMap}".split(",")
        theData.each { it -> 
            def pieces = it.split(":")
            try {
                theMode = pieces[0]; theDevices = pieces[1]; theLevel = pieces[2]; theTempType = pieces[3]; theTemp = pieces[4]; theColor = pieces[5]; theTime = pieces[6]; theTimeType = pieces[7]
            } catch (e) {
                if(theTime == null) theTime = "NA"
                if(theTimeType == null) theTimeType = "false"
                try {
                    theTimeType = pieces[6]
                } catch (e2) {
                    if(theTimeType == null) theTimeType = "false"
                }
            }
            if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
            theColor = theColor.replace("]","")
            theTime = theTime.replace("]","")
            theTimeType = theTimeType.replace("]","")       
            if(theDevices == null) theDevices = "NA"
            if(theLevel == null) theLevel = "NA"
            if(theTempType == null) theTempType = false
            if(theTemp == null) theTemp = "NA"
            if(theColor == null) theColor = "NA"
            if(theTime == null) theTime = "NA"
            if(theTimeType == null) theTimeType = "false"
            dpm = theDevices.toString().replace("[","").replace("]","").replace(", ",";")
            theValue = "${dpm}:${theLevel}:${theTempType}:${theTemp}:${theColor}:${theTime}:${theTimeType}"
            if(logEnable) log.debug "Rebuilding: mode: ${theMode} - theValue: ${theValue}"
            state.sdPerModeMap.put(theMode,theValue)
        }
    }
    if(state.sdPerModeMap) state.sdPerModeMap = state.sdPerModeMap.sort { a, b -> a.key <=> b.key }
// ***** Make Map *****    
    if(logEnable) log.debug "In sdPerModeHandler - Map: ${state.sdPerModeMap}"
    if(state.sdPerModeMap) {
        thePerModeMap =  "<table width=90% align=center><tr><td><b><u>Mode</u></b><td><b><u>Devices</u></b><td><b><u>Level</u></b><td><b><u>Temp</u></b><td><b><u>Color</u></b><td><b><u>TimeRev</u></b><td><b><u>MinSec</u></b>"
        def theData = "${state.sdPerModeMap}".split(",")
        theData.each { it -> 
            def pieces = it.split(":")
            try {
                theMode = pieces[0]; theDevices = pieces[1]; theLevel = pieces[2]; theTempType = pieces[3]; theTemp = pieces[4]; theColor = pieces[5]; theTime = pieces[6]; theTimeType = pieces[7]
            } catch (e) {
                log.warn "${app.label} - Something went wrong, please rebuild your Switches Per Mode table"
                if(logEnable) log.warn "In Make Map - Oops - theMode(0): ${theMode} - theDevice(1): ${theDevice} - theLevel(2): ${theLevel} - theTemp(3): ${theTemp} - theColor(4): ${theColor} - theTime(5): ${theTime} - theTimeType(6): ${theTimeType}"
            }
            if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
            if(theColor) theColor = theColor.replace("]","")
            if(theTimeType) theTimeType = theTimeType.replace("]","")
            if(theTimeType == "false") {
                timeType = "Min"
            } else {
                timeType = "Sec"
            }
            theDevicesList = ""
            theDs = theDevices.split(";")
            theDs.each { d ->
                if(d.startsWith(" ") || d.startsWith("[")) d = d.substring(1)
                theDevicesList += "${d}<br>"
            }
            thePerModeMap += "<tr><td>${theMode}<td>${theDevicesList}<td>${theLevel}<td>${theTemp}<td>${theColor}<td>${theTime}<td>${timeType}"
        }                
        thePerModeMap += "</table>"
    } else {
        thePerModeMap = "Empty"
    }
    state.thePerModeMap = thePerModeMap
    app.removeSetting("setDimmersPerMode")
    app.removeSetting("sdPerModeName")
    app.removeSetting("sdPerModeLevel")
    app.removeSetting("sdPerModeTempType")
    app.removeSetting("sdPerModeTemp")
    app.removeSetting("sdPerModeColor")
    app.removeSetting("sdPerModeTime")
    app.removeSetting("sdPerModeTimeType")
    app.updateSetting("sdPerModeColorTemp",[value:"false",type:"bool"])
    app.updateSetting("sdTimePerMode",[value:"false",type:"bool"])
    app.updateSetting("sdReverseTimeType",[value:"false",type:"bool"])
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(sdPerModeName && state.whichButton == "sdPerModeDel") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        sdPerModeHandler("del;nothing")
        state.working = false
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(state.whichButton == "sdPerModeRebuild") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        sdPerModeHandler("rebuild;nothing")
        state.working = false
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(sdPerModeName && state.whichButton == "sdPerModeAdd") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        sdPerModeHandler("add;nothing")
        state.working = false
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(sdPerModeName && state.whichButton == "sdPerModeCancel") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        app.removeSetting("masterDimmersPerMode")
        state.working = false
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(state.whichButton == "sdPerModeClear"){
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        sdPerModeHandler("clear;nothing")
        state.working = false
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(state.whichButton == "resetMaps") {
        state.oldMap = [:]
        state.sdPerModeMap = [:]
    } else if(state.whichButton == "checkForUpdate") {
        if(logEnable) log.debug "In appButtonHandler - Checking for Update"
        state.checkNow = true
        autoUpdateHandler()
    }
}

// ***** Calendarific *****
def getAPICountries() {
    if(logEnable) log.debug "In getAPICountries (${state.version})"
    countryURL = "https://calendarific.com/api/v2/countries?api_key=${parent.apiKey}"    
    try {
        state.cn = [:]
        def params = [
            uri: "${countryURL}",
        ]
        if(logEnable) log.debug "In getAPICountries - httpGet: ${params}"
        httpGet(params) { resp ->
            def json = resp.data
            for (rec in json.response.countries) {
                iso = rec['iso-3166']
                coun = rec.country_name
                state.cn.put("$iso", coun)
            } 
        }
        state.cName = state.cn.sort { a, b -> a.value <=> b.value }
    } catch(e) {
        if(logEnable) log.warn "In getAPICountries - Something went wrong, error to follow"
        log.error(getExceptionMessageWithLine(e))
    }
}

def getHolidayList() {
    if(logEnable) log.debug "In getHolidayList (${state.version})"
    state.BASE_URL = "https://calendarific.com/api/v2/holidays?api_key=${parent.apiKey}"
    if(apiCountry) state.country = "&country=${apiCountry}"
    if(apiYear) {
        state.year = "&year=${apiYear}"
    } else {
        state.year = new Date().year
    }    
    theURL = "${state.BASE_URL}${state.country}${state.year}"
    try {
	    state.hol = [:]
        def params = [
            uri: "${theURL}",
        ]
        if(logEnable) log.debug "In getHolidayList - httpGet: ${params}"
        httpGet(params) { resp ->
            def json = resp.data
            if(json.meta.code == 200) {
                for (rec in json.response.holidays) {
                    theName = rec.name
                    state.hol.put("$theName", theName)
                }
            } else {
                metaCodeIssue(json.meta.code)
            }
        }
        state.cHolidays = state.hol.sort { a, b -> a.value <=> b.value }
    } catch(e) {
        if(logEnable) log.warn "In getHolidayList - Something went wrong, error to follow"
        log.error(getExceptionMessageWithLine(e))
    }
}

def getHolidayInfo() {
    if(logEnable) log.debug "In getHolidayInfo (${state.version})"
    theURL = "${state.BASE_URL}${state.country}${state.year}"
    state.holidayInfo = "<table><tr><td width=15% align=center><b><u>DATE</b></u><td width=25% align=center><b><u>HOLIDAY</b></u><td width=60% align=center><b><u>DESCRIPTION</b></u>"
    state.myHolidays = [:]
    try {
        def params = [
            uri: "${theURL}",
        ]
        if(logEnable) log.debug "In getHolidayInfo - httpGet: ${params}"
        httpGet(params) { resp ->
            def json = resp.data
            if(json.meta.code == 200) {
                if(logEnable) log.info "In getHolidayInfo - ${json.meta.code} - Good to Go!"
                for (rec in json.response.holidays) {
                    theName = rec.name
                    if(apiHolidays.contains(theName)) {
                        if(logEnable) log.info "In getHolidayInfo - ${theName} IS IN ${apiHolidays} - MATCH"
                        theDate = rec.date.iso
                        theDesc = rec.description
                        state.holidayInfo += "<tr><td>${theDate}<td>${theName}<td>${theDesc}"                      
                        theData = "${theName}::${theDesc}"
                        state.myHolidays.put("$theDate", theData)
                    }
                }
            } else {
                metaCodeIssue(json.meta.code)
            } 
        }
        if(addTodayAsHoliday) {
            theDate = new Date().format( 'yyyy-MM-dd' ); theName = "Test Holiday"; theDesc = "Testing today as Holdiday"
            state.holidayInfo += "<tr><td>${theDate}<td>${theName}<td>${theDesc}"
            theData = "${theName}::${theDesc}"
            state.myHolidays.put("$theDate", theData)
            if(logEnable) log.info "In getHolidayInfo - Adding ${theName} to Holidays for Testing - MATCH"
        }
        if(addTomorrowAsHoliday) {
            theDate1 = new Date() + 1; theDate = theDate1.format( 'yyyy-MM-dd' ); theName = "Test Holiday 2"; theDesc = "Testing tomorrow as Holdiday"
            state.holidayInfo += "<tr><td>${theDate}<td>${theName}<td>${theDesc}"
            theData = "${theName}::${theDesc}"
            state.myHolidays.put("$theDate", theData)
            if(logEnable) log.info "In getHolidayInfo - Adding ${theName} to Holidays for Testing - MATCH"
        }
        state.holidayInfo += "</table>"
    } catch(e) {
        if(logEnable) log.warn "In getHolidayInfo - Something went wrong, error to follow"
        log.error(getExceptionMessageWithLine(e))
    }
}

def metaCodeIssue(code) {
    if(logEnable) log.debug "In metaCodeIssue (${state.version})"
    if(code == 401) {
        if(logEnable) log.warn "In getHolidayInfo - ${code} - Unauthorized Missing or incorrect API token in header."
    } else if(code == 422) {
        if(logEnable) log.warn "In getHolidayInfo - ${code} - Un-processable Entity meaning something with the message isn’t quite right, this could be malformed JSON or incorrect fields."
    } else if(code == 500) {
        if(logEnable) log.warn "In getHolidayInfo - ${code} - Internal Server Error This is an issue with Calendarific's servers processing your request."
    } else if(code == 503) {
        if(logEnable) log.warn "In getHolidayInfo - ${code} - Service Unavailable During planned service outages."
    } else if(code == 429) {
        if(logEnable) log.warn "In getHolidayInfo - ${code} - Too many requests. API limits reached."
    }
}

def checkForHoliday() {
    if(state.myHolidays) {
        if(logEnable) log.debug "In checkForHoliday (${state.version})"
        todaysDate = new Date().format( 'yyyy-MM-dd' )
        if(logEnable) log.debug "In checkForHoliday - todaysDate: ${todaysDate}"
        state.apiMatch = false
        theDates = state.myHolidays.keySet()
        theDates.each { it ->
            if(logEnable) log.debug "In checkForHoliday - it: ${it} vs ${todaysDate}"
            if(it == todaysDate) {
                if(logEnable) log.debug "In checkForHoliday - It's a MATCH!"
                state.apiMatch = true
            }
        } 
        if(state.apiMatch) {
            if(logEnable) log.debug "In checkForHoliday - Setting up trigger at ${apiTimeToTrigger} to startTheProcess."
            schedule(apiTimeToTrigger, "startTheProcess")
        } else {
            if(logEnable) log.debug "In checkForHoliday - Today is not a holiday, will check again tomorrow."
            unschedule(startTheProcess)
        }
    }
}
// ***** End Calendarific *****

def checkTransitionHandler() {
    if(logEnable) log.debug "In checkTransitionHandler (${state.version}) - transitionType: ${transitionType}"
    if(transitionType == "Device Attribute") {
        if(logEnable) log.debug "In checkTransitionHandler - Device Attribute"
        if(attTransitionEvent) {
            if(state.previousAtt == null) state.previousAtt = attTransitionEvent.currentValue(attTransitionAtt)
            state.currentAtt = attTransitionEvent.currentValue(attTransitionAtt)
            if(logEnable) log.debug "In checkTransitionHandler - Comparing Previous Att: ${state.previousAtt} to condition Att 1: ${atAttribute1}"
            if(logEnable) log.debug "In checkTransitionHandler - Comparing Current Att: ${state.currentAtt} to condition Att 2: ${atAttribute2}"
            if(state.previousAtt == atAttribute1 && state.currentAtt == atAttribute2) {
                if(logEnable) log.debug "In checkTransitionHandler - We have a MATCH!"
                state.transitionOK = true
            } else {
                if(logEnable) log.debug "In checkTransitionHandler - Transition does not match."
                state.previousAtt = state.currentAtt
                state.transitionOK = false
            }
        }
    } else if(transitionType == "HSM Status") {
        if(logEnable) log.debug "In checkTransitionHandler - HSM Status"
        if(state.previousAtt == null) state.previousAtt = location.hsmStatus
        state.currentAtt = location.hsmStatus
        if(logEnable) log.debug "In checkTransitionHandler - Comparing Previous HSM Status: ${state.previousAtt} to condition Att 1: ${atAttribute1}"
        if(logEnable) log.debug "In checkTransitionHandler - Comparing Current HSM Status: ${state.currentAtt} to condition Att 2: ${atAttribute2}"
        if(state.previousAtt == atAttribute1 && state.currentAtt == atAttribute2) {
            if(logEnable) log.debug "In checkTransitionHandler - We have a MATCH!"
            state.transitionOK = true
        } else {
            if(logEnable) log.debug "In checkTransitionHandler - Transition does not match."
            state.previousAtt = state.currentAtt
            state.transitionOK = false
        }      
    } else if(transitionType == "Mode") {
        if(logEnable) log.debug "In checkTransitionHandler - Mode"
        if(state.previousAtt == null) state.previousAtt = location.mode
        state.currentAtt = location.mode
        if(logEnable) log.debug "In checkTransitionHandler - Comparing Previous Mode: ${state.previousAtt} to condition Att 1: ${atAttribute1}"
        if(logEnable) log.debug "In checkTransitionHandler - Comparing Current Mode: ${state.currentAtt} to condition Att 2: ${atAttribute2}"
        if(state.previousAtt == atAttribute1 && state.currentAtt == atAttribute2) {
            if(logEnable) log.debug "In checkTransitionHandler - We have a MATCH!"
            state.transitionOK = true
        } else {
            if(logEnable) log.debug "In checkTransitionHandler - Transition does not match."
            state.previousAtt = state.currentAtt
            state.transitionOK = false
        }
    } else {
        if(logEnable) log.debug "In checkTransitionHandler - No transitionType Matched - triggerAndOr: ${triggerAndOr}"
        if(triggerAndOr) {
            state.transitionOK = false
        } else {
            state.transitionOK = true
        }
    }
    if(state.transitionOK) {
        if(logEnable) log.debug "In checkTransitionHandler - Transition PASSED - transitionOK: ${state.transitionOK}"
    } else {
        if(logEnable) log.warn "In checkTransitionHandler - Transition FAILED - transitionOK: ${state.transitionOK}"
    }
}

void getIcalDataHandler() {
    if(logEnable) log.debug "In getIcalDataHandler (${state.version})"
    iCalMap1 = [:]
    iCalMap2 = [:]
    iCalMap3 = [:]
    iCalMapAll = [:]
    state.within = false
    state.foundOne = false    
    try {
        iCLinks = iCalLinks.split(";")
        iCLinks.each { it ->
            if(it.startsWith(" ")) it = it.replaceFirst(" ","")
            def params = [
                uri: it,
                timeout: 10
            ]
            httpGet(params) { resp ->
                if(resp.status == 200) {
                    if(logEnable) log.debug "In getIcalDataHandler - Response Status${resp.status}"
                    theData = resp.data
                    state.line = 0
                    if(logEnable) log.info "---------- ---------- ---------- ---------- ----------"
                    theData.eachLine {
                        log.debug it
                        if(state.line <= 10) {
                            if(it == "BEGIN:VEVENT" || state.within) {
                                state.within = true
                            } 
                            if(it.contains("DTSTART")) { 
                                startDate = it.split(":")
                                if(startDate[1]) {
                                    state.startDate = startDate[1]
                                } else {
                                    state.startDate = "No Data"
                                }
                                state.foundOne = true
                                state.line = state.line + 1
                            }
                            if(it.contains("DTEND")) { 
                                endDate = it.split(":")
                                if(endDate[1]) {
                                    state.endDate = endDate[1]
                                } else {
                                    state.endDate = "No Data"
                                }
                            }
                            if(it.contains("SUMMARY")) {
                                summaryData = it.split(":")
                                if(summaryData[1]) {
                                    state.summary = summaryData[1]
                                } else {
                                    state.summary = "No Data"
                                }
                            }
                            if(it.contains("RRULE:FREQ")) {
                                summaryData = it.split("=")
                                if(summaryData[1]) {
                                    state.RR = summaryData[1]
                                } else {
                                    state.RR = "No Data"
                                }
                                log.info "RR: $state.RR"
                            }
                            if(it == "END:VEVENT" && state.foundOne) {
                                sDate = state.startDate.toString()
                                eDate = state.endDate.toString()
                                eSummary = state.summary.toString()
                                todaysDate = new Date().format( 'yyyyMMdd' )
                                tDate1 = new Date() + 1
                                todaysDate1 = tDate1.format( 'yyyyMMdd' )
                                tDate2 = new Date() + 2
                                todaysDate2 = tDate2.format( 'yyyyMMdd' )
                                workingDate = sDate.take(8)
                                log.info "---------- ---------- ---------- ---------- ----------"
                                if(todaysDate.toString() == workingDate.toString()) {
                                    newData = "${eDate};${state.summary}".toString()
                                    iCalMap1.put(sDate, newData)
                                    if(logEnable) log.debug "In getIcalDataHandler - Todays Map:<br>$sDate, $newData"
                                }
                                if(todaysDate1.toString() == workingDate.toString()) {
                                    newData = "${eDate};${state.summary}".toString()
                                    iCalMap2.put(sDate, newData)
                                    if(logEnable) log.debug "In getIcalDataHandler - 2 Day Map:<br>$sDate, $newData"
                                }
                                if(todaysDate2.toString() == workingDate.toString()) {
                                    newData = "${eDate};${state.summary}".toString()
                                    iCalMap3.put(sDate, newData)
                                    if(logEnable) log.debug "In getIcalDataHandler - 3 Day Map:<br>$sDate, $newData"
                                }
                                if(todaysDate && workingDate) {
                                    if(workingDate.toInteger() > todaysDate2.toInteger()) {
                                        newData = "${eDate};${state.summary}".toString()
                                        iCalMapAll.put(sDate, newData) 
                                    } else {
                                        // log.info "Hmmm: td: ${todaysDate} - wd: ${workingDate} - sum: ${state.summary}"
                                    }
                                }
                                state.within = false
                                state.foundOne = false
                                state.startDate = null
                                state.endDate = null
                                state.startDate = null
                            }
                        }
                    }
                } else {
                    if(logEnable) log.debug "In getIcalDataHandler - Response code ${resp.status} - NOT Good!"
                }
            }
        }
    } catch(e) {
        if(logEnable) log.debug "In getIcalDataHandler - Something went wrong"
        log.error(getExceptionMessageWithLine(e))
    }    
    state.iCalMap1 = iCalMap1.sort{ a, b -> a.key <=> b.key}
    state.iCalMap2 = iCalMap2.sort{ a, b -> a.key <=> b.key}
    state.iCalMap3 = iCalMap3.sort{ a, b -> a.key <=> b.key}
    state.iCalMapAll = iCalMapAll.sort{ a, b -> a.key <=> b.key}
}

def iCalHandler() {            // tCal for search
    if(logEnable) log.debug "In iCalHandler (${state.version})"
    todaysDate = new Date().format( 'yyyyMMdd' )
    tDate = todaysDate.toString()
    state.iCalMap1.each { theKey, theValue ->       
        tKey = theKey.substring(0,8).toString()
        keyLength = theKey.length()
        if(tDate == tKey) {
            if(logEnable) log.debug "In iCalHandler - Passed for Today's Event - $tKey"
            icSearch = iCalSearch.split(";")
            icSearch.each { theSearch ->
                if(theSearch.startsWith(" ")) theSearch = theSearch.replaceFirst(" ","")
                if(logEnable) log.debug "In iCalHandler - searching for: ${theSearch} in: ${theValue}"
                if(theValue.toLowerCase().contains("${theSearch.toLowerCase()}") || theSearch == "*") {
                    if(logEnable) log.debug "In iCalHandler - Match!"         
                    if(keyLength > 8) {
                        getZdate(theKey)                    
                        if(logEnable) log.debug "In iCalHandler - theKey: ${theKey} - zDate: ${state.zDate}"
                        icp = iCalPrior.toInteger()
                        use( TimeCategory ) { zOffset = state.zDate - icp.minutes }
                        def (endDate, message) = theValue.split(";")
                        state.currentIcalValue = message
                        if(logEnable) log.debug "In iCalHandler - zOffset: ${zOffset}"
                        runOnce(zOffset, startTheProcess, [overwrite: false])
                        if(logEnable) log.debug "In iCalHandler - Setting time to run @ ${zOffset}"
                    } else {
                        if(iCalTime) {
                            if(logEnable) log.debug "In iCalHandler - Setting time to run @ ${iCalTime}"
                            def (endDate, message) = theValue.split(";")
                            state.currentIcalValue = message
                            runOnce(iCalTime, startTheProcess, [overwrite: false])
                        } else {
                            if(logEnable) log.debug "In iCalHandler - No start time, not set to run"
                        }
                    }
                }
            }
        }
    }
}

def getZdate(data) {            // Modified from iCal Viewer Driver - @mark.cockcroft
    if(logEnable) log.debug "In getZdate - $data"
    Date zDate
    if(data.contains("Z")) {
        zDate =  toDateTime(data)
    } else if(data.contains("T")) {
        zDate = new SimpleDateFormat("yyyyMMdd'T'kkmmss").parse(data)
    } else {
        zDate = new SimpleDateFormat("yyyy-MM-dd").parse(data)
    }
    if(logEnable) log.debug "in getZdate - zDate: ${zDate}"    
    state.zDate = zDate
}

def pcMapOfChildrenHandler(evt) {
    if(logEnable) log.debug "In pcMapOfChildrenHandler (${state.version})"
    if(logEnable) log.debug "In pcMapOfChildrenHandler - Received: $evt.value"
    theData = evt.value.toString().replace("[","").replace("]","").split(",")
    state.pcMapOfChildren = [:]
    theData.each { it ->
        (theId,theName) = it.split(":")
        if(theId.startsWith(" ")) { theId = theId.substring(1) }
        if(theName.startsWith(" ")) { theName = theName.substring(1) }
        state.pcMapOfChildren.put("${theId}","${theName}")
    }
    if(logEnable) log.debug "In pcMapOfChildrenHandler - pcMapOfChildren: $state.pcMapOfChildren"
}

def patternControllerHandler() {
    if(logEnable) log.debug "In patternControllerHandler (${state.version})"
    sendLocationEvent(name: "pattern", value: ["${pcAction}", "${pcCommand}"])
}

def mapOfChildrenHandler(data) {
    if(logEnable) log.debug "In mapOfChildrenHandler (${state.version})"
    state.mapOfChildren = [:]
    state.mapOfChildren = data
}

def eventEngineHandler() {
    if(logEnable) log.debug "In eventEngineHandler (${state.version}) - Removed"
    /*
    eeAction.each { it ->
        if(logEnable) log.debug "In eventEngineHandler - Sending Cog Number: ${it} - Command: ${eeCommand}"
        data = "$it:$eeCommand"
        parent.runEEHandler(data)
    }
    */
}

def commandFromParentHandler(data) {
    if(logEnable) log.debug "In commandFromParentHandler (${state.version}) - data: ${data}"
    if(data == "pause") {
        app.updateSetting("pauseApp",[value:"true",type:"bool"])
    } else if(data == "resume") {
        app.updateSetting("pauseApp",[value:"false",type:"bool"])
    } else if(data == "reverse") {
        startTheProcess("reverse")
    } else if(data == "run") {
        startTheProcess("run")
    }
}

def certainTimeHasPassedHandler() {
    if(logEnable) log.debug "In certainTimeHasPassedHandler (${state.version})"
    if(certainTimeHasPassedEvent) {
        if(state.lastRunTime == null) state.lastRunTime = new Date()
        if(logEnable) log.debug "In certainTimeHasPassedHandler - lastRunTime: ${state.lastRunTime}"
        date1 = new Date()
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        date2 = dateFormat.parse("${state.lastRunTime}".replace("+00:00","+0000"))
        use(TimeCategory) {
            def duration = date1 - date2
            if(logEnable) log.debug "In certainTimeHasPassedHandler - ***** Minutes: ${duration.minutes} *****"
            dur = duration.minutes
        }
        nwt = certainTimeHasPassedEvent.toInteger()
        if(dur >= nwt) {
            state.certainTimeHasPassed = true
            if(logEnable) log.debug "In certainTimeHasPassedHandler - state.certainTimeHasPassed: true"
        } else {
            state.certainTimeHasPassed = false
            if(logEnable) log.debug "In certainTimeHasPassedHandler - state.certainTimeHasPassed: false"
        }
    } else {
        state.certainTimeHasPassed = true
        if(logEnable) log.debug "In certainTimeHasPassedHandler - state.certainTimeHasPassed: true"
    }
}

def switchesToSyncOnHandler(evt) {
    if(state.syncOnRunning == null) state.syncOnRunning = "no"
    if(state.syncOnRunning == "no") {
        state.syncOnRunning = "yes"
        if(logEnable) log.debug "-------------------- syncOnRunning: ${state.syncOnRunning} ----------------------"
        if(logEnable) log.debug "In switchesToSyncOnHandler (${state.version})"
        whoHappened = evt.displayName
        whoID = evt.deviceId
        whatHappened = evt.value
        if(logEnable) log.debug "In switchesToSyncOnHandler - whoHappened: ${whoHappened} - whatHappened: ${whatHappened}"
        switchesToSync.each {
            if(it.deviceId.toString() != whoID.toString()) {
                if(logEnable) log.debug "In switchesToSyncOnHandler - Turning ${it.displayName} - ${whatHappened}"
                it.on()
            }
        }
        pauseExecution(3000)
        state.syncOnRunning = "no"
        if(logEnable) log.debug "In switchesToSyncOnHandler - All Done!"
        if(logEnable) log.debug "-------------------- syncOnRunning: ${state.syncOnRunning} ----------------------"
        startTheProcess(evt)
    }
}

def switchesToSyncOffHandler(evt) {
    if(state.syncOffRunning == null) state.syncOffRunning = "no"
    if(state.syncOffRunning == "no") {
        state.syncOffRunning = "yes"
        if(logEnable) log.debug "-------------------- syncOffRunning: ${state.syncOffRunning} ----------------------"
        if(logEnable) log.debug "In switchesToSyncOffHandler (${state.version})"
        whoHappened = evt.displayName
        whoID = evt.deviceId
        whatHappened = evt.value
        if(logEnable) log.debug "In switchesToSyncOffHandler - whoHappened: ${whoHappened} - whatHappened: ${whatHappened}"
        switchesToSync.each {
            if(it.deviceId.toString() != whoID.toString()) {
                if(logEnable) log.debug "In switchesToSyncOffHandler - Turning ${it.displayName} - ${whatHappened}"
                it.off()
            }
        }
        pauseExecution(3000)
        state.syncOffRunning = "no"
        if(logEnable) log.debug "In switchesToSyncOffHandler - All Done!"
        if(logEnable) log.debug "-------------------- syncOffRunning: ${state.syncOffRunning} ----------------------"
        startTheProcess(evt)
    }
}

def switchesToSyncColorTempHandler(evt) {
    if(state.syncColorRunning == null) state.syncColorRunning = "no"
    if(state.syncColorRunning == "no") {
        state.syncColorRunning = "yes"
        if(logEnable) log.debug "-------------------- syncColorRunning: ${state.syncColorRunning} ----------------------"
        if(logEnable) log.debug "In switchesToSyncColorTempHandler (${state.version})"
        whoHappened = evt.displayName
        whoID = evt.deviceId
        whatHappened = evt.value.toInteger()
        switchesToSync.each {
            if(it.hasCommand("setColorTemperature")) {
                if(it.deviceId.toString() != whoID.toString()) {
                    if(logEnable) log.debug "In switchesToSyncColorTempHandler - Setting Color Temperature on ${it.displayName} - to ${whatHappened}"
                    it.setColorTemperature(whatHappened)
                }
            } else {
                if(logEnable) log.debug "In switchesToSyncColorTempHandler - ${it.displayName} doesn't have attribute 'colorTemperature', so Skipping."
            }
        }
        pauseExecution(3000)
        state.syncColorRunning = "no"
        if(logEnable) log.debug "In switchesToSyncLevelHandler - All Done!"
        if(logEnable) log.debug "-------------------- syncColorRunning: ${state.syncColorRunning} ----------------------"
        startTheProcess(evt)
    }
}

def switchesToSyncHueHandler(evt) {
    if(state.syncHueRunning == null) state.syncHueRunning = "no"
    if(state.syncHueRunning == "no") {
        state.syncHueRunning = "yes"
        if(logEnable) log.debug "-------------------- syncHueRunning: ${state.syncHueRunning} ----------------------"
        if(logEnable) log.debug "In switchesToSyncHueHandler (${state.version})"
        whoHappened = evt.displayName
        whoID = evt.deviceId
        whatHappened = evt.value.toInteger()
        switchesToSync.each {
            if(it.hasCommand("setHue")) {
                if(it.deviceId.toString() != whoID.toString()) {
                    if(logEnable) log.debug "In switchesToSyncHueHandler - Setting Hue on ${it.displayName} - to ${whatHappened}"
                    it.setHue(whatHappened)
                }
            } else {
                if(logEnable) log.debug "In switchesToSyncHueHandler - ${it.displayName} doesn't have attribute 'hue', so Skipping."
            }
        }
        pauseExecution(3000)
        state.syncHueRunning = "no"
        if(logEnable) log.debug "In switchesToSyncLevelHandler - All Done!"
        if(logEnable) log.debug "-------------------- syncHueRunning: ${state.syncHueRunning} ----------------------"
        startTheProcess(evt)
    }
}

def switchesToSyncLevelHandler(evt) {
    if(state.syncLevelRunning == null) state.syncLevelRunning = "no"
    if(state.syncLevelRunning == "no") {
        state.syncLevelRunning = "yes"
        if(logEnable) log.debug "-------------------- syncLevelRunning: ${state.syncLevelRunning} ----------------------"
        if(logEnable) log.debug "In switchesToSyncLevelHandler (${state.version})"
        whoHappened = evt.displayName
        whoID = evt.deviceId
        whatHappened = evt.value.toInteger()
        switchesToSync.each {
            if(it.hasCommand("setLevel")) {
                if(it.deviceId.toString() != whoID.toString()) {
                    if(logEnable) log.debug "In switchesToSyncLevelHandler - Setting Level on ${it.displayName} - to ${whatHappened}"
                    it.setLevel(whatHappened)
                }
            } else {
                if(logEnable) log.debug "In switchesToSyncLevelHandler - ${it.displayName} doesn't have attribute 'level', so Skipping."
            }
        }
        pauseExecution(3000)
        state.syncLevelRunning = "no"
        if(logEnable) log.debug "In switchesToSyncLevelHandler - All Done!"
        if(logEnable) log.debug "-------------------- syncLevelRunning: ${state.syncLevelRunning} ----------------------"
        startTheProcess(evt)
    }
}

def switchesToSyncSaturationHandler(evt) {
    if(state.syncSaturationRunning == null) state.syncSaturationRunning = "no"
    if(state.syncSaturationRunning == "no") {
        state.syncSaturationRunning = "yes"
        if(logEnable) log.debug "-------------------- syncSaturationRunning: ${state.syncSaturationRunning} ----------------------"
        if(logEnable) log.debug "In switchesToSyncSaturationHandler (${state.version})"
        whoHappened = evt.displayName
        whoID = evt.deviceId
        whatHappened = evt.value.toInteger()
        switchesToSync.each {
            if(it.hasCommand("setSaturation")) {
                if(it.deviceId.toString() != whoID.toString()) {
                    if(logEnable) log.debug "In switchesToSyncSaturationHandler - Setting Saturation on ${it.displayName} - to ${whatHappened}"
                    it.setSaturation(whatHappened)
                }
            } else {
                if(logEnable) log.debug "In switchesToSyncSaturationHandler - ${it.displayName} doesn't have attribute 'saturation', so Skipping."
            }
        }
        pauseExecution(3000)
        state.syncSaturationRunning = "no"
        if(logEnable) log.debug "In switchesToSyncSaturationHandler - All Done!"
        if(logEnable) log.debug "-------------------- syncSaturationRunning: ${state.syncSaturationRunning} ----------------------"
        startTheProcess(evt)
    }
}

def securityKeypadHandler(evt) {
    if(logEnable) log.debug "In securityKeypadHandler (${state.version})"
    whoHappened = evt.displayName
    whatHappened = evt.value
    theDevice = evt.device
    theStatus = theDevice.currentValue("securityKeypad")
    theLastCodeName = theDevice.currentValue("lastCodeName")
    state.securityOK = false
    kUser = false
    kStatus = false
    if(logEnable) log.debug "In securityKeypadHandler - whoHappened: ${whoHappened} - whatHappened: ${whatHappened} - theStatus: ${theStatus} - theLastCodeName: ${theLastCodeName}"
    if(keypadEventType == "Alarm User/Status") {
        if(keypadUser && keypadStatus) {
            if(logEnable) log.debug "In securityKeypadHandler - keypadUser: ${keypadUser} contains ${whatHappened} - keypadStatus: ${keypadStatus} contains ${theStatus}"
            keypadUser.each {
                if(it.toString() == whatHappened.toString()) {
                    kUser = true
                }
            }
            keypadStatus.each {
                if(it.toString() == theStatus.toString()) {
                    kStatus = true
                }
            }
            if(kUser && kStatus) state.securityOK = true
        } else if(keypadUser && !keypadStatus) {
            keypadUser.each {
                if(it.toString() == whatHappened.toString()) {
                    state.securityOK = true
                }
            }
        } else if(!keypadUser && keypadStatus) {
            keypadStatus.each {
                if(it.toString() == theStatus.toString()) {
                    state.securityOK = true
                }
            }
        }
    }    
    if(keypadEventType == "Panic Buttons") {
        if(logEnable) log.debug "In securityKeypadHandler - Panic Buttons - keypadPanic: ${keypadPanic} VS ${whatHappened}"
        theButtons = keypadPanic.toString()
        eButton = theButtons.split(",")
        eButton.each { it ->
            if(it.startsWith(" ") || it.startsWith("[")) theBut = it.substring(1)
            theBut = theBut.replace("]","")
            if(logEnable) log.debug "In securityKeypadHandler - Panic Buttons - Checking: ${theBut}"
            if(whatHappened == theBut) {
                if(logEnable) log.debug "In securityKeypadHandler - Panic Buttons - MATCH!"
                state.securityOK = true
            }
        }
    }   
    if(keypadEventType == "Alt Codes") {
        if(logEnable) log.debug "In securityKeypadHandler - Alt Codes - keypadAltCode: ${keypadAltCode} VS ${whatHappened}"
        if(whatHappened == keypadAltCode) {
            if(logEnable) log.debug "In securityKeypadHandler - Alt Codes - MATCH!"
            state.securityOK = true
        }
    }
    if(logEnable) log.debug "In securityKeypadHandler - securityOK: ${state.securityOK}"
}

def pingHandler() {
    if(ipAddress) {
        checkEnableHandler()
        if(pauseApp || state.eSwitch) {
            log.info "${app.label} is Paused or Disabled"
        } else {
            if(location.hub.firmwareVersionString > "2.2.6.140") {
                try {
                    if(logEnable) log.debug "In pingHandler (${state.version}) - Trying: ${ipAddress}"
                    hubitat.helper.NetworkUtils.PingData pingData = hubitat.helper.NetworkUtils.ping(ipAddress, numPings.toInteger())
                    int pTran = pingData.packetsTransmitted.toInteger()
                    if (pTran == 0){ // 2.2.7.121 bug returns all zeroes on not found per @thebearmay
                        pingData.packetsTransmitted = numPings
                        pingData.packetLoss = 100
                    }
                    if(logEnable) log.debug "In pingHandler - Pinging $ipAddress - Transmitted: ${pingData.packetsTransmitted}, Received: ${pingData.packetsReceived}, %Lost: ${pingData.packetLoss}"

                    if(pingData.packetLoss < 100) {
                        if(logEnable) log.debug "In pingHandler - Passed"
                        state.ipStatusOK = true
                    } else {
                        if(logEnable) log.debug "In pingHandler - Failed"
                        state.ipStatusOK = false
                    }
                } catch(e) {
                    if(logEnable) log.debug "In pingHandler - Something went wrong"
                    log.error(getExceptionMessageWithLine(e))
                }
            } else {
                if(logEnable) log.debug "In pingHandler - firmwareVersion: $location.hub.firmwareVersionString - Needs to be above 2.2.6.140"
            }
        }
    } else {
        if(logEnable) log.debug "In pingHandler - NOT using IP Ping - Setting value based on triggerAndOr: ${triggerAndOr}"
        if(triggerAndOr) {
            state.ipStatusOK = false
        } else {
            state.ipStatusOK = true
        }
    }
    if(logEnable) log.debug "In pingHandler - ipFound: ${ipFound} - ipStatusOK: ${state.ipStatusOK}"
    if(ipFound && state.ipStatusOK) {
        if(logEnable) log.debug "In pingHandler - Using ipFound is true and ipStatusOK is true"
        startTheProcess()
    } else if(!ipFound && !state.ipStatusOK) {
        if(logEnable) log.debug "In pingHandler - Using ipFound is false and ipStatusOK is false"
        startTheProcess()
    } else {
        if(logEnable) log.debug "In pingHandler - No need to startTheProcess"
    }
}

def colorChangeHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In colorChangeHandler (${state.version}) - Color Selection: ${colorSelection}"		
        slpTime = (changeTime*60)
        def colors = []
        colors = colorSelection
        def numLights = switchesToChange.size()
        def numColors = colors.size()

        if(logEnable) log.debug "In colorChangeHandler - pattern: ${pattern}"
        if (pattern == 'randomize') {
            randOffset = Math.abs(new Random().nextInt()%numColors)
            if(logEnable) log.debug "In colorChangeHandler - Pattern: ${pattern} - Offset: ${randOffset}"
            if(cycleHow == 'combined') {
                state.fromWhere = "colorChangeHandler"
                state.onLevel = lightLevel
                state.onColor = colors[randOffset]
                switchesToChange.each { stc ->
                    if(logEnable) log.debug "In colorChangeHandler - randomize-combined - stc: ${stc.displayName}, onColor: ${state.onColor} - randOffset: ${randOffset}"
                    setLevelandColorHandler(stc)
                }
            } else {
                for(def i=0;i<numLights;i++) {
                    state.fromWhere = "colorChangeHandler"
                    state.onLevel = lightLevel
                    state.onColor = colors[(randOffset + i) % numColors]
                    if(logEnable) log.debug "In colorChangeHandler - randomize-randomize - onLights: ${switchesToChange[i]}, onColor: ${state.onColor}"
                    setLevelandColorHandler(switchesToChange[i])
                }
            }
        } else if (pattern == 'cycle') {
            if(!state.colorOffset) { state.colorOffset = 0 }
            if(switchesToChange) {
                if(state.colorOffset >= numColors ) {
                    state.colorOffset = 0
                }
                if(cycleHow == 'combined') {                       
                    state.fromWhere = "colorChangeHandler"
                    state.onLevel = lightLevel
                    state.onColor = colors[state.colorOffset]
                    switchesToChange.each { stc ->
                        if(logEnable) log.debug "In colorChangeHandler - cycle-combined - stc: ${stc.displayName}, onColor: ${state.onColor} - colorOffset: ${state.colorOffset}"
                        setLevelandColorHandler(stc)
                    }
                } else {
                    for(i=0;i<numLights;i++) {
                        state.fromWhere = "colorChangeHandler"
                        state.onLevel = lightLevel
                        state.onColor = colors[(state.colorOffset + i) % numColors]
                        if(logEnable) log.debug "In colorChangeHandler - cycle-randomize - onLights: ${switchesToChange[i]}, onColor: ${state.onColor}"
                        setLevelandColorHandler(switchesToChange[i])
                    }
                }
                state.colorOffset = state.colorOffset + 1
            }
        }
    }
    if(logEnable) log.debug "In colorChangeHandler - slpTime: ${slpTime}"
    runIn(slpTime, "startTheProcess")
}

def useCustomColorsHandler() {
    input "useCustomColors", "bool", title: "Use Custom Colors", submitOnChange:true
    if(useCustomColors) {
        paragraph "<hr>"
        paragraph "<table width=100%><tr><td width=50%><b>Custom Color 1</b><td width=50%><b>Custom Color 2</b></table>"
        input "cc1hue", "text", title: "Hue", width: 3, submitOnChange:true
        input "cc1sat", "text", title: "Saturation", width: 3, submitOnChange:true
        input "cc2hue", "text", title: "Hue", width: 3, submitOnChange:true
        input "cc2sat", "text", title: "Saturation", width: 3, submitOnChange:true
        paragraph "<table width=100%><tr><td width=50%><b>Custom Color 3</b><td width=50%><b>Custom Color 4</b></table>"
        input "cc3hue", "text", title: "Hue", width: 3, submitOnChange:true
        input "cc3sat", "text", title: "Saturation", width: 3, submitOnChange:true
        input "cc4hue", "text", title: "Hue", width: 3, submitOnChange:true
        input "cc4sat", "text", title: "Saturation", width: 3, submitOnChange:true
        paragraph "<table width=100%><tr><td width=50%><b>Custom Color 5</b><td width=50%><b>Custom Color 6</b></table>"
        input "cc5hue", "text", title: "Hue", width: 3, submitOnChange:true
        input "cc5sat", "text", title: "Saturation", width: 3, submitOnChange:true
        input "cc6hue", "text", title: "Hue", width: 3, submitOnChange:true
        input "cc6sat", "text", title: "Saturation", width: 3, submitOnChange:true
        paragraph "<hr>"
    } else {
        app.removeSetting("cc1hue")
        app.removeSetting("cc1sat")
        app.removeSetting("cc2hue")
        app.removeSetting("cc2sat")
        app.removeSetting("cc3hue")
        app.removeSetting("cc3sat")
        app.removeSetting("cc4hue")
        app.removeSetting("cc4sat")
        app.removeSetting("cc5hue")
        app.removeSetting("cc5sat")
        app.removeSetting("cc6hue")
        app.removeSetting("cc6sat")
    }
    theColors = ['Soft White','White','Daylight','Warm White','Red','Green','Blue','Yellow','Orange','Purple','Pink']
    if(cc1hue && cc1sat) theColors.add("Custom Color 1")
    if(cc2hue && cc2sat) theColors.add("Custom Color 2")
    if(cc3hue && cc3sat) theColors.add("Custom Color 3")
    if(cc4hue && cc4sat) theColors.add("Custom Color 4")
    if(cc5hue && cc5sat) theColors.add("Custom Color 5")
    if(cc6hue && cc6sat) theColors.add("Custom Color 6")
    return theColors
}

def activeOneHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In Directional Condition - activeOneHandler (${state.version}) - evt: ${evt.displayName} - ${evt.value}"
        if(evt.value == "open" || evt.value == "active") {
            if(state.first != "two") { state.first = "one" } 
            state.motionOneActive = true
            if(logEnable) log.debug "In Directional Condition - activeOneHandler - first: ${state.first}"
            if(state.first == "two") activeHandler()
        } else {
            inactiveOneHandler()
        }
    }
}

def activeTwoHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In Directional Condition - activeTwoHandler (${state.version}) - evt: ${evt.displayName} - ${evt.value}"
        if(evt.value == "open" || evt.value == "active") {
            if(state.first != "one") { state.first = "two" }
            state.motionTwoActive = true
            if(logEnable) log.debug "In Directional Condition - activeTwoHandler - first: ${state.first}"
            if(state.first == "one") activeHandler()
        } else {
            inactiveTwoHandler()
        }
    }
}

def activeHandler() {
    if(logEnable) log.debug "In Directional Condition - activeHandler (${state.version})"
    if(state.motionOneActive && state.motionTwoActive) {
        if(state.first == "one") { state.direction = "right" }
        if(state.first == "two") { state.direction = "left" }
        state.lastDirection = state.direction
        if(logEnable) log.debug "In Directional Condition - activeHandler - first: ${state.first} - direction: ${state.direction}"
        if(theDirection == "Right" && state.direction == "right") { 
            state.totalMatch = 1
            state.totalConditions = 1
            startTheProcess("direction") 
        }
        if(theDirection == "Left" && state.direction == "left") {
            state.totalMatch = 1
            state.totalConditions = 1
            startTheProcess("direction") 
        }
    }
}

def inactiveOneHandler(evt) {
    if(logEnable) log.debug "In Directional Condition - inactiveOneHandler (${state.version})"
    if(state.first == "one") state.first = ""
    state.motionOneActive = false
    state.direction = ""
    if(logEnable) log.debug "In Directional Condition - inactiveOneHandler - first: ${state.first} - (should be blank)"
    startTheProcess("reverse")
}

def inactiveTwoHandler(evt) {
    if(logEnable) log.debug "In Directional Condition - inactiveTwoHandler (${state.version})"
    if(state.first == "two") state.first = ""
    state.motionTwoActive = false
    state.direction = ""
    if(logEnable) log.debug "In Directional Condition - inactiveTwoHandler - first: ${state.first} - (should be blank)"
    startTheProcess("reverse")
}

def securityKeypadActionHandler() {
    if(logEnable) log.debug "In securityKeypadActionHandler (${state.version})"
    if(keypadTone) {
        keypadAction.each { it ->
            if(logEnable) log.debug "In securityKeypadActionHandler - Sending ${keypadTone} to ${it}"
            pauseExecution(actionDelay)
            it.playTone(keypadTone)
        }
    }
}

def lockActionHandler() {
    if(logEnable) log.debug "In lockActionHandler (${state.version})"
    if(lockAction) {
        lockAction.each { it ->
            if(logEnable) log.debug "In lockActionHandler - Locking ${it}"
            pauseExecution(actionDelay)
            it.lock()
        }
    }
    if(unlockAction) {
        unlockAction.each { it ->
            if(logEnable) log.debug "In unlockActionHandler - Unlocking ${it}"
            pauseExecution(actionDelay)
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
            if(logEnable) log.debug "In lockUserActionHandler (${state.version}) - Lock: ${lockName} - Status: ${lockStatus}"
            if(lockStatus == "unlocked") {
                if(logEnable) log.debug "In lockUserActionHandler - Lock: ${lockName} - Status: ${lockStatus} - We're in!"
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
                        if(logEnable) log.debug "In lockUserActionHandler - Lock Code not available."
                        return
                    }
                    codeName = "${codeMap?.value?.name}"
                    if(logEnable) log.debug "In lockUserActionHandler - ${lockName} was unlocked by ${codeName}"	
                }
            }
        }
    }
}

def getGlobalVarCurrent() {
    if(logEnable) log.debug "In getGlobalVarCurrent (${state.version})"
    varData = getGlobalVar(theVariable)
    state.varCurrentValue = varData.value
    if(logEnable) log.debug "In getGlobalVarCurrent - variable: ${theVariable} - value: ${state.varCurrentValue}"
}

def hubVariableConditions() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In hubVariableConditions (${state.version})"
        state.variablesOK = false
        varData = getGlobalVar(theVariable)
        if(logEnable) log.debug "In hubVariableConditions - varData: ${varData}"
        if(varData.type == "integer" || varData.type == "bigdecimal") {
            if(varComp == "equals") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- varCompValue: ${varCompValue}"
                if(varData.value == varCompValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "does not equal") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- varCompValue: ${varCompValue}"
                if(varData.value != varCompValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "less than") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Less Than- varCompValue: ${varCompValue}"
                if(varData.value < varCompValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "greater than") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Greater Than- varCompValue: ${varCompValue}"
                if(varData.value > varCompValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "less than or equal to") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Less Than or Equal To- varCompValue: ${varCompValue}"
                if(varData.value <= varCompValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "greater than or equal to") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Greater Than or Equal To- varCompValue: ${varCompValue}"
                if(varData.value >= varCompValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "changed") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Changed- varCurrentValue: ${state.varCurrentValue}"
                if(varData.value != state.varCurrentValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "increased") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Increased- varCurrentValue: ${state.varCurrentValue}"
                if(varData.value > state.varCurrentValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "decreased") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Decreased- varCurrentValue: ${state.varCurrentValue}"
                if(varData.value < state.varCurrentValue) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} ??"
            }
        } else if(varData.type == "string") {
            if(varComp == "equals") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- varCompValue: ${varCompValue}"
                if(varData.value.toString() == varCompValue.toString()) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "does not equal") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- varCompValue: ${varCompValue}"
                if(varData.value.toString() != varCompValue.toString()) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "contains") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Contains- varCompValue: ${varCompValue}"
                if(varCompValue.toString().contains("${varData.value.toString()}")) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "changed") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -Changed- varCurrentValue: ${state.varCurrentValue}"
                if(varData.value.toString() != state.varCurrentValue.toString()) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "is empty") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -is empty- varCurrentValue: ${state.varCurrentValue}"
                if(varData.value == null || varData.value == "") {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} ??"
            }
        } else if(varData.type == "boolean") {
            if(varComp == "true") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- true"
                if(varData.value.toString() == "true") {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "false") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- false"
                if(varData.value.toString() == "false") {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else if(varComp == "changed") {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} - varData: ${varData.value} -VS- varCurrentValue: ${state.varCurrentValue}"
                if(varData.value.toString() == state.varCurrentValue.toString()) {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - Match!"
                    state.variablesOK = true
                } else {
                    if(logEnable) log.debug "In hubVariableConditions - ${varComp} - NO Match!"
                }
            } else {
                if(logEnable) log.debug "In hubVariableConditions - ${varComp} ??"
            }            
        } else {
            if(logEnable) log.debug "In hubVariableConditions - ${varComp} - No Match"
        }
        if(logEnable) log.debug "In hubVariableConditions - variablesOK: ${state.variablesOK}"
        state.varCurrentValue = varData.value
    }
}

def hubVariableActions() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In hubVariableActions (${state.version})"
        if(theActVariable) {
            varActData = getGlobalVar(theActVariable)
            if(varActData.type == "integer") {
                if(logEnable) log.debug "In hubVariableActions - integer"
                if(varOperation == "Set Number") {
                    setGlobalVar(theActVariable, varOperationValue.toInteger())
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $varOperationValue"
                } else if(varOperation == "Add Number") {
                    newValue = varActData.value + varOperationValue.toInteger()
                    setGlobalVar(theActVariable, newValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $newValue"
                } else if(varOperation == "Device Attribute - Replace") {
                    curVal = varOperationDevice.currentValue("$varOperationAttribute").toInteger()
                    setGlobalVar(theActVariable, curVal)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $curVal"
                } else if(varOperation == "Device Attribute - Add To") {
                    curVal = varOperationDevice.currentValue("$varOperationAttribute").toInteger()
                    newValue = varActData.value + curVal
                    setGlobalVar(theActVariable, newValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $newValue"
                }
            } else if(varActData.type == "bigdecimal") {
                if(logEnable) log.debug "In hubVariableActions - bigdecimal"
                if(varOperation == "Set Number") {
                    setGlobalVar(theActVariable, varOperationValue.toDouble())
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $varOperationValue"
                } else if(varOperation == "Add Number") {
                    newValue = varActData.value + varOperationValue.toDouble()
                    setGlobalVar(theActVariable, newValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $newValue"
                } else if(varOperation == "Device Attribute - Replace") {
                    curVal = varOperationDevice.currentValue("$varOperationAttribute").toDouble()
                    setGlobalVar(theActVariable, curVal)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $curVal"
                } else if(varOperation == "Device Attribute - Add To") {
                    curVal = varOperationDevice.currentValue("$varOperationAttribute").toDouble()
                    newValue = varActData.value + curVal
                    setGlobalVar(theActVariable, newValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $newValue"
                }   
            } else if(varActData.type == "string") {
                if(logEnable) log.debug "In hubVariableActions - string"
                if(varOperation == "Set String") {
                    setGlobalVar(theActVariable, varOperationValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $varOperationValue"
                } else if(varOperation == "Remove String") {
                    newValue = varActData.value.replace("$varOperationValue", "")
                    setGlobalVar(theActVariable, newValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $newValue"
                } else if(varOperation == "Replace String") {
                    newValue = varActData.value.replace("$varOperationValueOld", "$varOperationValueNew")
                    setGlobalVar(theActVariable, newValue)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $newValue"
                } else if(varOperation == "Device Attribute") {
                    curVal = varOperationDevice.currentValue("$varOperationAttribute")
                    setGlobalVar(theActVariable, curVal)
                    if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $curVal"
                }                      
            } else if(varActData.type == "boolean") {
                if(logEnable) log.debug "In hubVariableActions - boolean"
                setGlobalVar(theActVariable, varOperationValue)
                if(logEnable) log.debug "In hubVariableActions - Setting $theActVariable to $varOperationValue"                    
            } else if(varActData.type == "datetime") {
                if(logEnable) log.debug "In hubVariableActions - datetime"
                paragraph "Work in progress"
            } else {
                if(logEnable) log.debug "In hubVariableActions - no match - ${varActData.type}"
            }
        }       
    }
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

// ~~~~~ start include (7) BPTWorld.bpt-blueIrisActions ~~~~~
library ( // library marker BPTWorld.bpt-blueIrisActions, line 1
        base: "app", // library marker BPTWorld.bpt-blueIrisActions, line 2
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-blueIrisActions, line 3
        category: "Apps", // library marker BPTWorld.bpt-blueIrisActions, line 4
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-blueIrisActions, line 5
        name: "bpt-blueIrisActions", // library marker BPTWorld.bpt-blueIrisActions, line 6
        namespace: "BPTWorld", // library marker BPTWorld.bpt-blueIrisActions, line 7
        documentationLink: "", // library marker BPTWorld.bpt-blueIrisActions, line 8
        version: "1.0.0", // library marker BPTWorld.bpt-blueIrisActions, line 9
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-blueIrisActions, line 10
) // library marker BPTWorld.bpt-blueIrisActions, line 11

def profileSwitchHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 13
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 14
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 15
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 16
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 17
        if(logEnable) log.debug "In switchChangeHandler (${state.version})" // library marker BPTWorld.bpt-blueIrisActions, line 18
        if(logEnable) log.debug "In switchChangeHandler - switchProfileOn: ${switchProfileOn}" // library marker BPTWorld.bpt-blueIrisActions, line 19
        if(switchProfileOn == "Pon0") { // library marker BPTWorld.bpt-blueIrisActions, line 20
            biChangeHandler("0") // library marker BPTWorld.bpt-blueIrisActions, line 21
        } else if(switchProfileOn == "Pon1") { // library marker BPTWorld.bpt-blueIrisActions, line 22
            biChangeHandler("1") // library marker BPTWorld.bpt-blueIrisActions, line 23
        } else if(switchProfileOn == "Pon2") { // library marker BPTWorld.bpt-blueIrisActions, line 24
            biChangeHandler("2") // library marker BPTWorld.bpt-blueIrisActions, line 25
        } else if(switchProfileOn == "Pon3") { // library marker BPTWorld.bpt-blueIrisActions, line 26
            biChangeHandler("3") // library marker BPTWorld.bpt-blueIrisActions, line 27
        } else if(switchProfileOn == "Pon4") { // library marker BPTWorld.bpt-blueIrisActions, line 28
            biChangeHandler("4") // library marker BPTWorld.bpt-blueIrisActions, line 29
        } else if(switchProfileOn == "Pon5") { // library marker BPTWorld.bpt-blueIrisActions, line 30
            biChangeHandler("5") // library marker BPTWorld.bpt-blueIrisActions, line 31
        } else if(switchProfileOn == "Pon6") { // library marker BPTWorld.bpt-blueIrisActions, line 32
            biChangeHandler("6") // library marker BPTWorld.bpt-blueIrisActions, line 33
        } else if(switchProfileOn == "Pon7") { // library marker BPTWorld.bpt-blueIrisActions, line 34
            biChangeHandler("7") // library marker BPTWorld.bpt-blueIrisActions, line 35
        } // library marker BPTWorld.bpt-blueIrisActions, line 36
    } // library marker BPTWorld.bpt-blueIrisActions, line 37
} // library marker BPTWorld.bpt-blueIrisActions, line 38

def scheduleSwitchHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 40
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 41
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 42
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 43
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 44
        if(logEnable) log.debug "In switchChangeHandler (${state.version})" // library marker BPTWorld.bpt-blueIrisActions, line 45
        if(logEnable) log.debug "In scheduleSwitchHandler - switchScheduleOn: ${biScheduleName}" // library marker BPTWorld.bpt-blueIrisActions, line 46
        biChangeHandler(biScheduleName) // library marker BPTWorld.bpt-blueIrisActions, line 47
    } // library marker BPTWorld.bpt-blueIrisActions, line 48
} // library marker BPTWorld.bpt-blueIrisActions, line 49

def cameraPresetHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 51
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 52
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 53
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 54
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 55
        if(logEnable) log.debug "In cameraPresetHandler (${state.version}) - biCameraPreset: ${biCameraPreset}" // library marker BPTWorld.bpt-blueIrisActions, line 56
        if(biCameraPreset == "PS1") { // library marker BPTWorld.bpt-blueIrisActions, line 57
            biChangeHandler("1") // library marker BPTWorld.bpt-blueIrisActions, line 58
        } else if(biCameraPreset == "PS2") { // library marker BPTWorld.bpt-blueIrisActions, line 59
            biChangeHandler("2") // library marker BPTWorld.bpt-blueIrisActions, line 60
        } else if(biCameraPreset == "PS3") { // library marker BPTWorld.bpt-blueIrisActions, line 61
            biChangeHandler("3") // library marker BPTWorld.bpt-blueIrisActions, line 62
        } else if(biCameraPreset == "PS4") { // library marker BPTWorld.bpt-blueIrisActions, line 63
            biChangeHandler("4") // library marker BPTWorld.bpt-blueIrisActions, line 64
        } else if(biCameraPreset == "PS5") { // library marker BPTWorld.bpt-blueIrisActions, line 65
            biChangeHandler("5") // library marker BPTWorld.bpt-blueIrisActions, line 66
        } // library marker BPTWorld.bpt-blueIrisActions, line 67
    } // library marker BPTWorld.bpt-blueIrisActions, line 68
} // library marker BPTWorld.bpt-blueIrisActions, line 69

def cameraSnapshotHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 71
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 72
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 73
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 74
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 75
        if(logEnable) log.debug "In cameraSnapshotHandler (${state.version})" // library marker BPTWorld.bpt-blueIrisActions, line 76
        if(logEnable) log.debug "In cameraSnapshotHandler - Switch on" // library marker BPTWorld.bpt-blueIrisActions, line 77
        biChangeHandler("0") // library marker BPTWorld.bpt-blueIrisActions, line 78
    } // library marker BPTWorld.bpt-blueIrisActions, line 79
} // library marker BPTWorld.bpt-blueIrisActions, line 80

def cameraTriggerHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 82
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 83
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 84
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 85
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 86
        if(logEnable) log.debug "In cameraTriggerHandler (${state.version})" // library marker BPTWorld.bpt-blueIrisActions, line 87
        if(logEnable) log.debug "cameraTriggerHandler - On" // library marker BPTWorld.bpt-blueIrisActions, line 88
        biChangeHandler("1") // library marker BPTWorld.bpt-blueIrisActions, line 89
    } // library marker BPTWorld.bpt-blueIrisActions, line 90
} // library marker BPTWorld.bpt-blueIrisActions, line 91

def cameraPTZHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 93
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 94
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 95
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 96
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 97
        if(logEnable) log.debug "In cameraPTZHandler (${state.version})" // library marker BPTWorld.bpt-blueIrisActions, line 98
        if(logEnable) log.debug "In cameraPTZHandler - biCameraPTZ: ${biCameraPTZ}" // library marker BPTWorld.bpt-blueIrisActions, line 99
        if(biCameraPTZ == "PTZ0") { // library marker BPTWorld.bpt-blueIrisActions, line 100
            biChangeHandler("0") // library marker BPTWorld.bpt-blueIrisActions, line 101
        } else if(biCameraPTZ == "PTZ1") { // library marker BPTWorld.bpt-blueIrisActions, line 102
            biChangeHandler("1") // library marker BPTWorld.bpt-blueIrisActions, line 103
        } else if(biCameraPTZ == "PTZ2") { // library marker BPTWorld.bpt-blueIrisActions, line 104
            biChangeHandler("2") // library marker BPTWorld.bpt-blueIrisActions, line 105
        } else if(biCameraPTZ == "PTZ3") { // library marker BPTWorld.bpt-blueIrisActions, line 106
            biChangeHandler("3") // library marker BPTWorld.bpt-blueIrisActions, line 107
        } else if(biCameraPTZ == "PTZ4") { // library marker BPTWorld.bpt-blueIrisActions, line 108
            biChangeHandler("4") // library marker BPTWorld.bpt-blueIrisActions, line 109
        } else if(biCameraPTZ == "PTZ5") { // library marker BPTWorld.bpt-blueIrisActions, line 110
            biChangeHandler("5") // library marker BPTWorld.bpt-blueIrisActions, line 111
        } else if(biCameraPTZ == "PTZ6") { // library marker BPTWorld.bpt-blueIrisActions, line 112
            biChangeHandler("6") // library marker BPTWorld.bpt-blueIrisActions, line 113
        } // library marker BPTWorld.bpt-blueIrisActions, line 114
    } // library marker BPTWorld.bpt-blueIrisActions, line 115
} // library marker BPTWorld.bpt-blueIrisActions, line 116

def cameraRebootHandler() { // library marker BPTWorld.bpt-blueIrisActions, line 118
    checkEnableHandler() // library marker BPTWorld.bpt-blueIrisActions, line 119
    if(pauseApp || state.eSwitch) { // library marker BPTWorld.bpt-blueIrisActions, line 120
        log.info "${app.label} is Paused or Disabled" // library marker BPTWorld.bpt-blueIrisActions, line 121
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 122
        if(logEnable) log.debug "In cameraRebootHandler (${state.version})" // library marker BPTWorld.bpt-blueIrisActions, line 123
        if(logEnable) log.debug "In cameraRebootHandler - Switch on" // library marker BPTWorld.bpt-blueIrisActions, line 124
        biChangeHandler("0") // library marker BPTWorld.bpt-blueIrisActions, line 125
    } // library marker BPTWorld.bpt-blueIrisActions, line 126
} // library marker BPTWorld.bpt-blueIrisActions, line 127

def biChangeHandler(num) { // library marker BPTWorld.bpt-blueIrisActions, line 129
    if(logEnable) log.debug "In biChangeHandler (${state.version}) - biControl: ${biControl}" // library marker BPTWorld.bpt-blueIrisActions, line 130
	biHost = "${parent.biServer}:${parent.biPort}" // library marker BPTWorld.bpt-blueIrisActions, line 131
	if(biControl == "Switch_Profile") { // library marker BPTWorld.bpt-blueIrisActions, line 132
		if(logEnable) log.debug "I'm in Switch_Profile" // library marker BPTWorld.bpt-blueIrisActions, line 133
		biRawCommand = "/admin?profile=${num}&user=${parent.biUser}&pw=${parent.biPass}"         // library marker BPTWorld.bpt-blueIrisActions, line 134
    } else if(biControl == "Switch_Contact_Motion") { // library marker BPTWorld.bpt-blueIrisActions, line 135
        if(logEnable) log.debug "I'm in Switch, Contact or Motion" // library marker BPTWorld.bpt-blueIrisActions, line 136
        biRawCommand = "/admin?profile=${num}&user=${parent.biUser}&pw=${parent.biPass}"        // library marker BPTWorld.bpt-blueIrisActions, line 137
    } else if(biControl == "Camera_Preset") { // library marker BPTWorld.bpt-blueIrisActions, line 138
        if(logEnable) log.debug "I'm in Camera_Preset" // library marker BPTWorld.bpt-blueIrisActions, line 139
        biRawCommand = "/admin?camera=${biCamera}&preset=${num}&user=${parent.biUser}&pw=${parent.biPass}"         // library marker BPTWorld.bpt-blueIrisActions, line 140
        // /admin?camera=x&preset=x // library marker BPTWorld.bpt-blueIrisActions, line 141
    } else if(biControl == "Camera_Snapshot") { // library marker BPTWorld.bpt-blueIrisActions, line 142
        if(logEnable) log.debug "I'm in Camera_Snapshot" // library marker BPTWorld.bpt-blueIrisActions, line 143
        biRawCommand = "/admin?camera=${biCamera}&snapshot&user=${parent.biUser}&pw=${parent.biPass}"         // library marker BPTWorld.bpt-blueIrisActions, line 144
        // /admin?camera=x&snapshot // library marker BPTWorld.bpt-blueIrisActions, line 145
    } else if(biControl == "Camera_Trigger") { // library marker BPTWorld.bpt-blueIrisActions, line 146
        if(logEnable) log.debug "I'm in Camera_Trigger" // library marker BPTWorld.bpt-blueIrisActions, line 147
        if(!useMethod) biRawCommand = "/admin?camera=${biCamera}&manrec=${num}&user=${parent.biUser}&pw=${parent.biPass}" // library marker BPTWorld.bpt-blueIrisActions, line 148
        if(useMethod) biRawCommand = "/admin?camera=${biCamera}&trigger&user=${parent.biUser}&pw=${parent.biPass}"         // library marker BPTWorld.bpt-blueIrisActions, line 149
        // NOTE: if this Command doesn't work for you, try the second one instead // library marker BPTWorld.bpt-blueIrisActions, line 150
        // /admin?camera=x&manrec=1 // library marker BPTWorld.bpt-blueIrisActions, line 151
    } else if(biControl == "Camera_PTZ") { // library marker BPTWorld.bpt-blueIrisActions, line 152
        if(logEnable) log.debug "I'm in Camera_PTZ" // library marker BPTWorld.bpt-blueIrisActions, line 153
        biRawCommand = "/cam/${biCamera}/pos=${num}"         // library marker BPTWorld.bpt-blueIrisActions, line 154
        // /cam/{cam-short-name}/pos=x Performs a PTZ command on the specified camera, where x= 0=left, 1=right, 2=up, 3=down, 4=home, 5=zoom in, 6=zoom out // library marker BPTWorld.bpt-blueIrisActions, line 155
    } else if(biControl == "Camera_Reboot") { // library marker BPTWorld.bpt-blueIrisActions, line 156
        if(logEnable) log.debug "I'm in Camera_Reboot" // library marker BPTWorld.bpt-blueIrisActions, line 157
        biRawCommand = "/admin?camera=${biCamera}&reboot&user=${parent.biUser}&pw=${parent.biPass}" // library marker BPTWorld.bpt-blueIrisActions, line 158
        // /admin?camera=x&reboot // library marker BPTWorld.bpt-blueIrisActions, line 159
    } else if(biControl == "Camera_Enable" || biControl == "Camera_Disable") { // library marker BPTWorld.bpt-blueIrisActions, line 160
        if(logEnable) log.debug "I'm in Camera_Enable/Disable" // library marker BPTWorld.bpt-blueIrisActions, line 161
        biRawCommand = "/admin?camera=${biCamera}&enable=${num}&user=${parent.biUser}&pw=${parent.biPass}"            // library marker BPTWorld.bpt-blueIrisActions, line 162
        // /admin?camera=x&enable=1 or 0 Enable or disable camera x (short name) // library marker BPTWorld.bpt-blueIrisActions, line 163
    } else if(biControl == "Switch_Schedule") {     // library marker BPTWorld.bpt-blueIrisActions, line 164
        if(logEnable) log.debug "I'm in Switch_Schedule" // library marker BPTWorld.bpt-blueIrisActions, line 165
        biRawCommand = "/admin?schedule=${num}&user=${parent.biUser}&pw=${parent.biPass}"         // library marker BPTWorld.bpt-blueIrisActions, line 166
    } else { // library marker BPTWorld.bpt-blueIrisActions, line 167
        biRawCommand = "*** Something went wrong! ***" // library marker BPTWorld.bpt-blueIrisActions, line 168
    } // library marker BPTWorld.bpt-blueIrisActions, line 169
    if(logEnable) log.debug "In biChangeHandler - biHost: ${biHost} - biUser: ${parent.biUser} - biPass: ${parent.biPass} - num: ${num}" // library marker BPTWorld.bpt-blueIrisActions, line 170
	if(logEnable) log.debug "In biChangeHandler - sending GET to URL: ${biHost}${biRawCommand}" // library marker BPTWorld.bpt-blueIrisActions, line 171
	def httpMethod = "GET" // library marker BPTWorld.bpt-blueIrisActions, line 172
	def httpRequest = [ // library marker BPTWorld.bpt-blueIrisActions, line 173
		method:		httpMethod, // library marker BPTWorld.bpt-blueIrisActions, line 174
		path: 		biRawCommand, // library marker BPTWorld.bpt-blueIrisActions, line 175
		headers:	[ // library marker BPTWorld.bpt-blueIrisActions, line 176
			HOST:		biHost, // library marker BPTWorld.bpt-blueIrisActions, line 177
			Accept: 	"*/*", // library marker BPTWorld.bpt-blueIrisActions, line 178
		] // library marker BPTWorld.bpt-blueIrisActions, line 179
	] // library marker BPTWorld.bpt-blueIrisActions, line 180
	def hubAction = new hubitat.device.HubAction(httpRequest) // library marker BPTWorld.bpt-blueIrisActions, line 181
	sendHubCommand(hubAction) // library marker BPTWorld.bpt-blueIrisActions, line 182
} // library marker BPTWorld.bpt-blueIrisActions, line 183

// ~~~~~ end include (7) BPTWorld.bpt-blueIrisActions ~~~~~
