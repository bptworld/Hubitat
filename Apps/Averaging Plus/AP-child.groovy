/**
 *  **************** Averaging Plus Child App  ****************
 *
 *  Design Usage:
 *  Average just about anything. Get notifications based on Setpoints.
 *
 *  Copyright 2020-2022 Bryan Turcotte (@bptworld)
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
 *  1.2.3 - 02/27/22 - I got carried away, major rewrite!
 *  1.2.2 - 02/27/22 - Fixed a typo, other minor changes
 *  1.2.1 - 02/27/22 - Adding 'reference' device for Delta
 *  1.2.0 - 02/26/22 - Added option to average a certain device over time, also added time between restriction.
 *  ---
 *  1.0.0 - 05/25/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Averaging Plus"
	state.version = "1.2.3"
}

definition(
    name: "Averaging Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Average just about anything. Get notifications based on Setpoints.",
    category: "Convenience",
	parent: "BPTWorld:Averaging Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Averaging%20Plus/AP-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "actionsConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Average just about anything. Get notifications based on Setpoints."
            paragraph "<b>How the Averaging works</b><br>- Select a bunch of devices that share an attribute (ie. temperature)<br>- Select the Attribute to average<br>- Select the time frame between averages<br><br>For each device that has the attribute, the value will be added to the total value, then divided by the number of devices that have the attribute."
            paragraph "<b>How the Delta works</b><br>- The Delta is the difference between the Average and the Reference Device.<br>- The app can then check if the Delta value is within a certain range."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the averaging results. This device can also be selected below in the Setpoint options to be used as a switch to control other things."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have AP create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'AP - Motion Sensors')", required:true, submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Simple Averaging Driver'.</small>"
            }
        }      
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Average Options")) {
            input "groupORsingle", "bool", title: "Average a group of devices OR a single device over time", defaultValue:false, submitOnChange:true
            if(groupORsingle) {
                paragraph "Select a single device to average a certain attribute over time."
                input "theDevices", "capability.*", title: "Select Device", required:false, multiple:false, submitOnChange:true
            } else {
                paragraph "Select a group of devices that share a common attribute to average.<br><small>Note: Does NOT have to be the same 'type' of devices, just share a common attribute, ie. 'temperature'.</small>"
                input "theDevices", "capability.*", title: "Select Devices", required:false, multiple:true, submitOnChange:true
            }
            if(theDevices) {
                allAttrs = []
                theDevices.each { dev ->
                    attributes = dev.supportedAttributes
                    attributes.each { att ->
                        theType = att.getDataType()
                        if(theType == "NUMBER") {
                            allAttrs << att.name
                        }
                    }
                }
                allAttrsa = allAttrs.unique().sort()
                input "attrib", "enum", title: "Attribute to Average", required:true, multiple:false, submitOnChange:true, options:allAttrsa
                input "decimals", "bool", title: "Use Decimal Points (off) or Round (On)", defaultValue:false, submitOnChange:true
            }
            if(theDevices && attrib && dataDevice) { 
                match = false
                list = dataDevice.supportedAttributes
                lista = list.join(",")
                listb = lista.split(",")            
                listb.each { lt ->
                    if(lt == attrib) { match = true }
                }
                if(!match) paragraph "<b>** Please post the attribute name that you are trying to Average on the 'Average Plus' thread to get it properly added to the driver. **</b>"
            }
            if(theDevices && attrib) {
                input "triggerMode", "enum", title: "Time Between Averages", submitOnChange:true, options: ["1_Min","5_Min","10_Min","15_Min","30_Min","1_Hour","3_Hour"], required:true
                input "timeToReset", "time", title: "Reset the Daily Averages at this time, every day (ie. 12:00am)", required:true
                input "days", "enum", title: "Reset the Weekly Averages on this day, every week, at the same time as the Daily (ie. Monday)", description: "Days", required:true, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                input "timeBetween", "bool", title: "Only average between certain times", defaultValue:false, submitOnChange:true
                if(timeBetween) {
                    input "fromTime", "time", title: "From <small><abbr title='Exact time for the Average to start'><b>- INFO -</b></abbr></small>", required:false, width: 6, submitOnChange:true
                    input "toTime", "time", title: "To <small><abbr title='Exact time for the Average to End'><b>- INFO -</b></abbr></small>", required:false, width: 6, submitOnChange:true
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Reference Options")) {
            paragraph "This 'Reference' device can be used to compare the average above and this devices value."
            input "reference", "capability.*", title: "Select a Reference Device", required:false, multiple:false, submitOnChange:true
            if(reference) {
                refAttrs = []
                attributes = reference.supportedAttributes
                attributes.each { att ->
                    theType = att.getDataType()
                    if(theType == "NUMBER") {
                        refAttrs << att.name
                    }
                }
                refAttrs = refAttrs.unique().sort()
                input "refAtt", "enum", title: "Attribute", options:refAttrs, required:true, multiple:false, submitOnChange:true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Comparison Options")) {            
            input "percType", "enum", title: "Select the type of percentage to use", options: [
                ["S":"High/Low Setpoints"],
                ["1":"average VS reference"],
                ["2":"newValue VS reference"],
                ["3":"newValue VS average"],
                ["4":"newValue VS lastValue"]
            ], multiple:false, submitOnChange:true

            if(percType == "S") {
                paragraph "If the Average becomes too high or low, actions can happen."
                input "highSetpoint", "number", title: "High Setpoint", required:false, submitOnChange:true, width:6
                input "lowSetpoint", "number", title: "Low Setpoint", required:false, submitOnChange:true, width:6
            } else if((percType == "1" || percType == "2") && !reference) {
                paragraph "Please select a Reference Device to use this option"
            } else {
                input "percVSdelta", "bool", title: "Use Percentage (off) or Delta Value (on)", defaultValue:false, submitOnChange:true
                if(percVSdelta) {
                    input "deltaMax", "number", title: "Max Delta value", required:false, submitOnChange:true, width:6
                    input "deltaHighLow", "bool", title: "Only valid if Delta is too high or low", defaultValue:false, submitOnChange:true
                    if(deltaHighLow) {
                        input "onlyDeltaLowHigh", "bool", title: "Select when Low (off) or High (on)", defaultValue:false, submitOnChange:true
                        if(onlyDeltaLowHigh) {
                            paragraph "Delta will only be used when it is too High"
                        } else {
                            paragraph "Delta will only be used when it is too Low"
                        }
                    } else {
                        paragraph "Delta will be used for values too high and too Low"
                    }
                } else {
                    input "pDifference", "number", title: "Percentage Difference", submitOnChange:true
                    input "percHighLow", "bool", title: "Only valid if Percentage is too high or low", defaultValue:false, submitOnChange:true
                    if(percHighLow) {
                        input "onlyPercLowHigh", "bool", title: "Select when Low (off) or High (on)", defaultValue:false, submitOnChange:true
                        if(onlyPercLowHigh) {
                            paragraph "Percentage will only be used when it is too High"
                        } else {
                            paragraph "Percentage will only be used when it is too Low"
                        }
                    } else {
                        paragraph "Percentage will be used for values too high and too Low"
                    }
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            paragraph "Sometimes a device can stop sending values (ie. run out of battery). With this option, if the device hasn't reported in - in X hours - do not include the value in the average."
            input "maxHours", "number", title: "Max Hours Since Reporting (1 to 24)", range: '1..24', submitOnChange:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            href "actionsConfig", title:"${getImage("optionsGreen")} Notification Options", description:"Click here for options"
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
                input "resetStuff", "bool", title: "Reset Stats/Averages", required:false, submitOnChange:true
                if(resetStuff) {
                    resetHandler()
                    app.updateSetting("resetStuff",[value:"false",type:"bool"])
                }
            }
        }
		display2()
	}
}

def actionsConfig() {
	dynamicPage(name: "actionsConfig", title: "", install:false, uninstall:false) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Value Too Low Options")) {
            input "spLowDevices", "capability.switch", title: "Turn on -OR- off Device(s)", required:false, multiple:true, submitOnChange:true
            if(spLowDevices) {
                input "offORonLow", "bool", title: "Turn devices Off (off) or On (on)", defaultValue:false, submitOnChange:true
                if(!offORonLow) {
                    input "lowTimesOff", "number", title: "How many 'Too Low Value' required in a row to turn switch Off", defaultValue:2, submitOnChange:true
                } else {
                    input "lowTimesOn", "number", title: "How many 'Too Low Value' required in a row to turn switch On", defaultValue:2, submitOnChange:true
                    input "lowDeviceAutoOff", "bool", title: "Automatically turn the devices off when return to normal range", defaultValue:false, required:false, submitOnChange:true
                    if(lowDeviceAutoOff) {
                        input "lowTimesOff", "number", title: "How many 'Normal Value' required in a row to automatically turn switch Off", defaultValue:3, submitOnChange:true
                    }
                }
            }            
            input "sendPushLow", "bool", title: "Send a Pushover notification", defaultValue:false, required:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Value Too High Options")) {
            input "spHighDevices", "capability.switch", title: "Turn on -OR- off Device(s)", required:false, multiple:true, submitOnChange:true
            if(spHighDevices) {
                input "offORonHigh", "bool", title: "Turn devices Off (off) or On (on)", defaultValue:false, submitOnChange:true
                if(!offORonHigh) {
                    input "highTimesOff", "number", title: "How many 'Too High Value' required in a row to turn switch Off", defaultValue:2, submitOnChange:true
                } else {                    
                    input "highTimesOn", "number", title: "How many 'Too High Value' required in a row to turn switch On", defaultValue:2, submitOnChange:true
                    input "highDeviceAutoOff", "bool", title: "Automatically turn the devices off when return to normal range", defaultValue:false, required:false, submitOnChange:true
                    if(highDeviceAutoOff) {
                        input "highTimesOff", "number", title: "How many 'Normal Value' required in a row to automatically turn switch Off", defaultValue:3, submitOnChange:true
                    }
                }
            }           
            input "sendPushHigh", "bool", title: "Send a Pushover notification", defaultValue:false, required:false, submitOnChange:true
        }
    }
}

def notificationOptions(){
    dynamicPage(name: "notificationOptions", title: "", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
            paragraph "<hr>"
            input "pushMessage", "capability.notification", title: "Send a Push notification to certain users", multiple:true, required:false, submitOnChange:true
        }

        if(sendPushLow) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages - Low")) {
                paragraph "Wildcards:<br>- %avg% - Display the Average value"
                input "spLowMessage", "text", title: "Message", submitOnChange:true
            }
        }
        
        if(sendPushHigh) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages - High")) {
                paragraph "Wildcards:<br>- %avg% - Display the Average value"
                input "spHighMessage", "text", title: "Message", submitOnChange:true
            }
        }
        
        if(sendPushRef) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages - Delta")) {
                paragraph "Wildcards:<br>- %avg% - Display the Average value"
                input "deltaMessage", "text", title: "Message", submitOnChange:true
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
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    if(theDevices && attrib) {
        if(triggerMode == "1_Min") runEvery1Minute(averageHandler)
        if(triggerMode == "5_Min") runEvery5Minutes(averageHandler)
        if(triggerMode == "10_Min") runEvery10Minutes(averageHandler)
        if(triggerMode == "15_Min") runEvery15Minutes(averageHandler)
        if(triggerMode == "30_Min") runEvery30Minutes(averageHandler)
        if(triggerMode == "1_Hour") runEvery1Hour(averageHandler)
        if(triggerMode == "3_Hour") runEvery3Hours(averageHandler)
        schedule(timeToReset, resetHandler)
        if(timeBetween && fromTime && toTime) {
            schedule(fromTime, startTimeBetween)
            schedule(toTime, endTimeBetween)
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
        averageHandler()
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def resetHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In resetHandler (${state.version})"
        state.valueMap = []
        if(theDevices) {
            dataDevice.virtualAverage("-")
            dataDevice.todaysHigh("-")
            dataDevice.todaysLow("-")
        }
        weeklyResetHandler()
    }
}

def weeklyResetHandler() {
    dayOfTheWeekHandler()
    if(state.daysMatch) {
        dataDevice.weeklyHigh("-")
        dataDevice.weeklyLow("-")
    }
}

def averageHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In averageHandler (${state.version})"
        if(state.betweenTime) {
            if(theDevices) {
                if(logEnable) log.debug "     - - - - - Start (Averaging) - - - - -     "
                totalNum = 0
                numOfDev = 0
                state.low = false
                state.high = false
                floatingPoint = false
                if(logEnable) log.debug "In averageHandler - Attribute: ${attrib}"
                if(groupORsingle) {
                    getTimeDiff(theDevices)
                    if(state.active) {
                        if(state.valueMap == null) state.valueMap = []
                        totalValue = 0
                        newValue = theDevices.currentValue("${attrib}")
                        if(state.lastValue == null) state.lastValue = newValue
                        if(newValue) {
                            state.valueMap << newValue
                        }

                        state.valueMap.each { it ->
                            totalValue += it.toInteger()
                        }

                        mapSize = state.valueMap.size()
                        state.theAverage = (totalValue / mapSize).toDouble().round(1)
                        if(logEnable) log.debug "In averageHandler - mapSize: ${mapSize}"
                        if(logEnable) log.debug "In averageHandler - theAverage: ${state.theAverage}"
                    }
                } else {
                    theDevices.each { it ->
                        getTimeDiff(it)
                        if(state.active) {
                            newValue = it.currentValue("${attrib}")
                            if(newValue) {
                                num = newValue.toDouble()
                                if(logEnable) log.debug "In averageHandler - working on ${it} - num: ${num}"
                                if(num) {
                                    numOfDev += 1
                                    totalNum += num
                                }
                            } else {
                                if(num == null) num = "0"
                                if(logEnable) log.debug "In averageHandler - working on ${it} - num: ${num}"
                                numOfDev += 1
                            }
                        }
                    }

                    if(totalNum == 0 || totalNum == null) {
                        state.theAverage = 0
                    } else {
                        state.theAverage = (totalNum / numOfDev).toDouble().round(1)
                    }
                    if(decimals) state.theAverage = state.theAverage.toInteger()
                    if(logEnable) log.debug "In averageHandler - totalNum: ${totalNum} - numOfDev: ${numOfDev} - theAverage: ${state.theAverage}"
                }
                
                todaysHigh = dataDevice.currentValue("todaysHigh")
                todaysLow = dataDevice.currentValue("todaysLow")
                weeklyHigh = dataDevice.currentValue("weeklyHigh")
                weeklyLow = dataDevice.currentValue("weeklyLow")

                if(todaysHigh == null) todaysHigh = 0
                if(todaysLow == null) todaysLow = 100000
                if(weeklyHigh == null) weeklyHigh = todaysHigh
                if(weeklyLow == null) weeklyLow = todaysLow

                if(state.theAverage > todaysHigh) { dataDevice.todaysHigh(state.theAverage) }
                if(state.theAverage < todaysLow) { dataDevice.todaysLow(state.theAverage) }  
                if(state.theAverage > weeklyHigh) { dataDevice.weeklyHigh(state.theAverage) }
                if(state.theAverage < weeklyLow) { dataDevice.weeklyLow(state.theAverage) } 
                
                // increase = [(new value - original value)/original value] * 100
                
                if(logEnable) log.debug "------------------------------------------------"
                if(reference) {
                    refValue = reference.currentValue("${refAtt}")
                    if(percType == "1") {
                        if(logEnable) log.debug "In averageHandler - percentage 1 - average VS reference - theAverage: $state.theAverage - refValue: $refValue"
                        perc = (((state.theAverage - refValue)/refValue) * 100).toDouble().round(1)
                        if(perc >= 0) { 
                            if(logEnable) log.debug "In averageHandler - percentage 1 - average VS reference - Value is UP by ${perc}%"
                        } else {
                            if(logEnable) log.debug "In averageHandler - percentage 1 - average VS reference - Value is DOWN by ${perc}%"
                        }
                        theDelta = (state.theAverage - refValue).toDouble().round(1)
                        if(theDelta >= 0) { 
                            if(logEnable) log.debug "In averageHandler - Delta 1 - average VS reference - Value is UP by ${theDelta}"
                        } else {
                            if(logEnable) log.debug "In averageHandler - Delta 1 - average VS reference - Value is DOWN by ${theDelta}"
                        }
                        if(logEnable) log.debug "------------------------------------------------"
                    } else if(percType == "2") {
                        if(logEnable) log.debug "In averageHandler - percentage 2 - newValue VS reference - newValue: $newValue - refValue: $refValue"
                        perc = (((newValue - refValue)/refValue) * 100).toDouble().round(1)
                        if(perc >= 0) { 
                            if(logEnable) log.debug "In averageHandler - percentage 2 - newValue VS reference - Value is UP by ${perc}%"
                        } else {
                            if(logEnable) log.debug "In averageHandler - percentage 2 - newValue VS reference - Value is DOWN by ${perc}%"
                        }
                        theDelta = (newValue - refValue).toDouble().round(1)
                        if(theDelta >= 0) { 
                            if(logEnable) log.debug "In averageHandler - Delta 2 - newValue VS reference - Value is UP by ${theDelta}"
                        } else {
                            if(logEnable) log.debug "In averageHandler - Delta 2 - newValue VS reference - Value is DOWN by ${theDelta}"
                        }
                        if(logEnable) log.debug "------------------------------------------------"
                    }
                } else {
                    if(logEnable) log.debug "Please select a Reference Device to use this option"
                }
                
                if(percType == "3") {
                    if(logEnable) log.debug "In averageHandler - percentage 3 - newValue VS average - newValue: $newValue - theAverage: $state.theAverage"
                    perc = (((newValue - state.theAverage)/state.theAverage) * 100).toDouble().round(1)
                    if(perc >= 0) { 
                        if(logEnable) log.debug "In averageHandler - percentage 3 - newValue VS average - Value is UP by ${perc}%"
                    } else {
                        if(logEnable) log.debug "In averageHandler - percentage 3 - newValue VS average - Value is DOWN by ${perc}%"
                    }
                    theDelta = (newValue - state.theAverage).toDouble().round(1)
                    if(theDelta >= 0) { 
                        if(logEnable) log.debug "In averageHandler - Delta 3 - newValue VS average - Value is UP by ${theDelta}"
                    } else {
                        if(logEnable) log.debug "In averageHandler - Delta 3 - newValue VS average - Value is DOWN by ${theDelta}"
                    }
                    if(logEnable) log.debug "------------------------------------------------"
                } else if(percType == "4") {
                    if(logEnable) log.debug "In averageHandler - percentage 4 - newValue VS lastValue - newValue: $newValue - lastValue: $state.lastValue"
                    perc = (((newValue - state.lastValue)/state.lastValue) * 100).toDouble().round(1)
                    if(perc >= 0) { 
                        if(logEnable) log.debug "In averageHandler - percentage 4 - newValue VS lastValue - Value is UP by ${perc}%"
                    } else {
                        if(logEnable) log.debug "In averageHandler - percentage 4 - newValue VS lastValue - Value is DOWN by ${perc}%"
                    }
                    theDelta = (newValue - state.lastValue).toDouble().round(1)
                    if(theDelta >= 0) { 
                        if(logEnable) log.debug "In averageHandler - Delta 4 - newValue VS lastValue - Value is UP by ${theDelta}"
                    } else {
                        if(logEnable) log.debug "In averageHandler - Delta 4 - newValue VS lastValue - Value is DOWN by ${theDelta}"
                    }
                    state.lastValue = newValue
                    if(logEnable) log.debug "------------------------------------------------"
                }
                if(theDelta)dataDevice.sendEvent(name: "delta", value: theDelta, isStateChange: true)
                if(refValue) dataDevice.sendEvent(name: "reference", value: refValue, isStateChange: true)

                theData = "${attrib}:${state.theAverage}"
                if(logEnable) log.debug "In averageHandler - Sending theData: ${theData}"
                dataDevice.virtualAverage(theData)

                if(percType != "S") {
                    if(percVSdelta) {
                        if(deltaHighLow) {
                            if(onlyDeltaLowHigh) {
                                theHigh = refValue + deltaMax
                                theLow = null
                            } else {
                                theHigh = null
                                theLow = refValue - deltaMax
                            }
                        } else {
                            theHigh = refValue + deltaMax
                            theLow = refValue - deltaMax
                        }
                    } else {
                        if(percHighLow) {
                            if(onlyPercLowHigh) {
                                theHigh = refValue + pDifference
                                theLow = null
                            } else {
                                theHigh = null
                                theLow = refValue - pDifference
                            }
                        } else {
                            theHigh = refValue + pDifference
                            theLow = refValue - pDifference
                        }
                    }

                    app.updateSetting("highSetpoint", [type: "number", value: "${theHigh}"])
                    app.updateSetting("lowSetpoint", [type: "number", value: "${theLow}"])
                }
                if(logEnable) log.debug "In averageHandler - highSetpoint: ${highSetpoint} - lowSetpoint: ${lowSetpoint}"

                if(state.theAverage && (lowSetpoint != "null")) {
                    if(state.theAverage <= lowSetpoint) {
                        if(logEnable) log.debug "In averageHandler - The average (${state.theAverage}) is BELOW the low setpoint (${lowSetpoint})"
                        state.low = true
                        state.nTimes = 0

                        if(state.lTimes == null) state.lTimes = 0
                        state.lTimes = state.lTimes + 1

                        if(spLowDevices) {
                            spLowDevices.each {
                                if(offORonLow) {
                                    it.on()
                                } else {
                                    it.off()
                                }
                            }
                        }
                        state.theMsg = spLowMessage
                        if(pushMessage || useSpeech) messageHandler()
                        if(pushMessage && !state.sentPush) pushNow()
                        if(useSpeech && fmSpeaker) letsTalk()
                    }
                }

                if(state.theAverage && (highSetpoint != "null")) {
                    if(state.theAverage >= highSetpoint) {
                        if(logEnable) log.debug "In averageHandler - The average (${state.theAverage}) is ABOVE the high setpoint (${highSetpoint})"
                        state.high = true
                        state.nTimes = 0

                        if(state.hTimes == null) state.hTimes = 0
                        state.hTimes = state.hTimes + 1

                        if(spHighDevices) {
                            spHighDevices.each {
                                if(offORonHigh) {
                                    it.on()
                                } else {
                                    it.off()
                                }
                            }
                        }
                        state.theMsg = spHighMessage
                        if(pushMessage || useSpeech) messageHandler()
                        if(pushMessage && !state.sentPush) pushNow()
                        if(useSpeech && fmSpeaker) letsTalk()
                    }
                }

                if(state.theAverage && (highSetpoint != "null") && (lowSetpoint != "null")) {
                    if(state.theAverage <= highSetpoint && state.theAverage >= lowSetpoint) {
                        if(logEnable) log.debug "In averageHandler - The average (${state.theAverage}) looks good!"

                        state.hTimes = 0
                        state.lTimes = 0

                        if(state.nTimes == null) state.nTimes = 0
                        state.nTimes = state.nTimes + 1

                        if(spHighDevices && highDeviceAutoOff && state.nTimes >= highTimesOff) {               
                            spHighDevices.each {
                                it.off()
                            }
                        }

                        if(spLowDevices && lowDeviceAutoOff && state.nTimes >= lowTimesOff) {                
                            spLowDevices.each {
                                it.off()
                            }
                        }

                        state.sentPush = false
                    } 
                }
                
                if(logEnable) log.debug "     - - - - - End (Averaging) - - - - -     "
            }
        } else {
            if(logEnable) log.debug "In averageHandler - betweenTime: ${state.betweenTime} - Time is outside of range, no average taken."
        }
    }
}

def letsTalk() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - theMsg: ${state.theMsg}"
        if(useSpeech && fmSpeaker) {
            fmSpeaker.latestMessageFrom(state.name)
            fmSpeaker.speak(state.theMsg)
        }
        state.theMsg = ""
        if(logEnable) log.debug "In letsTalk - *** Finished ***"
    }
}

def messageHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In messageHandler (${state.version})"
        if(state.theMsg) {
            if(state.theMsg.contains("%avg%")) {state.theMsg = state.theMsg.replace('%avg%', "${state.theAverage}" )}
            if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
        } else {
            if(logEnable) log.debug "In messageHandler - No message to send."
        }
    }
}

def pushNow() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In pushNow (${state.version})"
        thePushMessage = "${app.label} \n"
        thePushMessage += state.theMsg
        if(logEnable) log.debug "In pushNow - Sending message: ${thePushMessage}"
        if(state.low) pushMessage.deviceNotification(thePushMessage)
        if(state.high) pushMessage.deviceNotification(thePushMessage)
        state.sentPush = true
    }
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Averaging Plus Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Averaging Plus unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
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

def getTimeDiff(aDevice) { 
    if(logEnable) log.debug "In getTimeDiff (${state.version}) - working on ${aDevice}"
    try {
	    since = aDevice.getLastActivity()
        def prev = Date.parse("yyy-MM-dd HH:mm:ssZ","${since}".replace("+00:00","+0000"))
        def now = new Date()
        use(TimeCategory) {       
            theDur = now - prev
            theDays = theDur.days
            theHours = theDur.hours
        }
    } catch (e) {
        log.warn "Device Watchdog - ${aDevice} does not have a Last Activity value."
    }
    
    if(!theDays) theDays = 0
    if(!theHours) theHours = 0
        
    theDays = theDays.toInteger()
    theHours = theHours.toInteger()
    totalHours = ((theDays * 24) + theHours)
        
    if(logEnable) log.info "In getTimeDiff - ${aDevice} - dur: ${theDur} - days: ${theDays} - hours: ${theHours} - totalHours: ${totalHours}"
    maxHours = maxHours ?: 12
    if(totalHours >= maxHours) {
        if(logEnable) log.info "In getTimeDiff - ${aDevice} - Hasn't reported in ${totalHours}. Tossing value."
        state.active = false
    } else {
        state.active = true
    }
}

def startTimeBetween() {
    if(logEnable) log.debug "In startTimeBetween (${state.version}) - Start"
    state.betweenTime = true
}

def endTimeBetween() {
    if(logEnable) log.debug "In endTimeBetween (${state.version}) - End"
    state.betweenTime = false
}

// ********** Normal Stuff **********
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
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
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" }
        paragraph getFormat("line")
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
    }
}

def getHeaderAndFooter() {
    timeSinceNewHeaders()
    if(state.checkNow == null) state.checkNow = true
    if(state.totalHours > 6 || state.checkNow) {
        def params = [
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
            requestContentType: "application/json",
            contentType: "application/json",
            timeout: 10
        ]
        try {
            def result = null
            httpGet(params) { resp ->
                state.headerMessage = resp.data.headerMessage
                state.footerMessage = resp.data.footerMessage
            }
        } catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
        } catch(e) {
            prev = state.previous
        }
    }
    def now = new Date()
    use(TimeCategory) {
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
}
