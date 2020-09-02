/**
 *  **************** Averaging Plus Child App  ****************
 *
 *  Design Usage:
 *  Average just about anything. Get notifications based on Setpoints.
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
 *  1.1.1 - 07/18/20 - Added 'Off' options to setpoints, cosmetic changes
 *  1.1.0 - 07/09/20 - Fixed Disable switch
 *  1.0.9 - 07/05/20 - Adjustments
 *  1.0.8 - 06/25/20 - Added App Control options
 *  1.0.7 - 06/22/20 - Changes to letsTalk
 *  1.0.6 - 06/21/20 - Minor changes
 *  1.0.5 - 06/16/20 - Changes by @rvrolyk, thanks!
 *  1.0.4 - 06/13/20 - Fixed letsTalk typo
 *  1.0.3 - 06/11/20 - All speech now goes through Follow Me
 *  1.0.2 - 06/10/20 - Attribute average now save under same attribute name when possible, app will only display attributres that are numbers, added weekly high/low.
 *  1.0.1 - 06/07/20 - Added more options and some error trapping
 *  1.0.0 - 05/25/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Averaging Plus"
	state.version = "1.1.1"
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
    page name: "highSetpointConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "lowSetpointConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "notificationOptions", title: "", install: false, uninstall: false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Average just about anything. Get notifications based on Setpoints."
            paragraph "<b>How the Averaging works</b><br>- Select a bunch of devices that share an attribute (ie. temperature)<br>- Select the Attribute to average<br>- Select the time frame between averages<br><br>For each device that has the attribute, the value will be added to the total value, then divided by the number of devices that had the attribute."
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
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Devices to Average Options")) {
            paragraph "Select a group of devices that share a common attribute to average.<br><small>Note: Does NOT have to be the same 'type' of devices, just share a common attribute, ie. 'temperature'.</small>"
            input "theDevices", "capability.*", title: "Select Devices", required:false, multiple:true, submitOnChange:true
            
            if(theDevices) {
                allAttrs = []
                //allAttrs = theDevices.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name}"] }
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
            }
            if(theDevices && attrib) { 
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
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Daily Setpoint Options")) {
            paragraph "If the Daily Average becomes too high or low, notifications can be sent."
            input "highSetpoint", "number", title: "High Setpoint", required:false, submitOnChange:true, width:6
            input "lowSetpoint", "number", title: "Low Setpoint", required:false, submitOnChange:true, width:6
            
            if(highSetpoint) {
                if(spHighDevices || sendPushHigh) {
                    href "highSetpointConfig", title:"${getImage("optionsGreen")} High Setpoint Options", description:"Click here for options"
                } else {
                    href "highSetpointConfig", title:"${getImage("optionsRed")} High Setpoint Options", description:"Click here for options"
                }
            }
            
            if(lowSetpoint) {
                if(spLowDevices || sendPushLow) {
                    href "lowSetpointConfig", title:"${getImage("optionsGreen")} Low Setpoint Options", description:"Click here for options"
                } else {
                    href "lowSetpointConfig", title:"${getImage("optionsRed")} Low Setpoint Options", description:"Click here for options"
                }
            }
        }
        
        if(highSetpoint || lowSetpoint) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
                if(speakerSS) {
                    href "notificationOptions", title:"${getImage("optionsGreen")} Notification Options", description:"Click here for options"
                } else {
                    href "notificationOptions", title:"${getImage("optionsRed")} Notification Options", description:"Click here for options"
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
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required:false, submitOnChange:true
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def lowSetpointConfig() {
	dynamicPage(name: "lowSetpointConfig", title: "", install:false, uninstall:false) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Average Too Low Options")) {
            input "spLowDevices", "capability.switch", title: "Turn on OR off Device(s)", required:false, multiple:true, submitOnChange:true
            if(spLowDevices) {
                input "offORonLow", "bool", title: "Turn devices Off (off) or On (on)", defaultValue:false, submitOnChange:true
                if(!offORonLow) {
                    input "lowTimesOff", "number", title: "How many 'Too Low Averages' required in a row to turn switch Off", defaultValue:2, submitOnChange:true
                } else {
                    input "lowTimesOn", "number", title: "How many 'Too Low Averages' required in a row to turn switch On", defaultValue:2, submitOnChange:true
                    input "lowDeviceAutoOff", "bool", title: "Automatically turn the devices off when return to normal range", defaultValue:false, required:false, submitOnChange:true
                    if(lowDeviceAutoOff) {
                        input "lowTimesOff", "number", title: "How many 'Normal Averages' required in a row to automatically turn switch Off", defaultValue:3, submitOnChange:true
                    }
                }
            }
            
            input "sendPushLow", "bool", title: "Send a Pushover notification", defaultValue:false, required:false, submitOnChange:true
        }       
    }
}

def highSetpointConfig() {
	dynamicPage(name: "highSetpointConfig", title: "", install:false, uninstall:false) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Average Too High Options")) {
            input "spHighDevices", "capability.switch", title: "Turn on OR off Device(s)", required:false, multiple:true, submitOnChange:true
            if(spHighDevices) {
                input "offORonHigh", "bool", title: "Turn devices Off (off) or On (on)", defaultValue:false, submitOnChange:true
                if(!offORonHigh) {
                    input "highTimesOff", "number", title: "How many 'Too High Averages' required in a row to turn switch Off", defaultValue:2, submitOnChange:true
                } else {                    
                    input "highTimesOn", "number", title: "How many 'Too High Averages' required in a row to turn switch On", defaultValue:2, submitOnChange:true
                    input "highDeviceAutoOff", "bool", title: "Automatically turn the devices off when return to normal range", defaultValue:false, required:false, submitOnChange:true
                    if(highDeviceAutoOff) {
                        input "highTimesOff", "number", title: "How many 'Normal Averages' required in a row to automatically turn switch Off", defaultValue:3, submitOnChange:true
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
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        if(theDevices && attrib) {
            if(triggerMode == "1_Min") runEvery1Minute(averageHandler)
            if(triggerMode == "5_Min") runEvery5Minutes(averageHandler)
            if(triggerMode == "10_Min") runEvery10Minutes(averageHandler)
            if(triggerMode == "15_Min") runEvery15Minutes(averageHandler)
            if(triggerMode == "30_Min") runEvery30Minutes(averageHandler)
            if(triggerMode == "1_Hour") runEvery1Hour(averageHandler)
            if(triggerMode == "3_Hour") runEvery3Hours(averageHandler)

            schedule(timeToReset, resetHandler)        
            averageHandler()
        }
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def resetHandler() {
    if(logEnable) log.debug "In zeroHandler (${state.version})"
    if(theDevices) {
        dataDevice.virtualAverage("-")
        dataDevice.todaysHigh("-")
        dataDevice.todaysLow("-")
    }
    weeklyResetHandler()
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
        if(theDevices) {
            if(logEnable) log.debug "     - - - - - Start (Averaging) - - - - -     "
            totalNum = 0
            numOfDev = 0
            state.low = false
            state.high = false
            floatingPoint = false

            theDevices.each { it ->
                if(logEnable) log.debug "In averageHandler - working on ${it} - ${attrib}"
                num1 = it.currentValue("${attrib}")
                if(num1) {
                    int num = num1
                    if(logEnable) log.debug "In averageHandler - working on ${it} - ${attrib} - num: ${num}"
                    if(num) {
                        numOfDev += 1
                        totalNum += num
                    }
                }
            }
            if(totalNum == 0 || totalNum == null) {
                state.theAverage = 0
            } else {
                state.theAverage = floatingPoint
                ? (totalNum / numOfDev).toDouble().round(1)
                : (totalNum / numOfDev).toDouble().round()
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

            theData = "${attrib}:${state.theAverage}"
            if(logEnable) log.debug "In averageHandler - Sending theData: ${theData}"
            dataDevice.virtualAverage(theData)

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
                if(pushMessage && !state.sentPush) {
                    state.theMsg = spLowMessage
                    messageHandler()
                    pushNow()
                }
                if(useSpeech && fmSpeaker) letsTalk()
            }

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
                if(pushMessage && !state.sentPush) {
                    messageHandler = spHighMessage
                    messageHandler()
                    pushNow()
                }
                if(useSpeech && fmSpeaker) letsTalk()
            }

            if(state.theAverage < highSetpoint && state.theAverage > lowSetpoint) {
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
            if(logEnable) log.debug "     - - - - - End (Averaging) - - - - -     "
        }
    }
}

def letsTalk() {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - theMsg: ${state.theMsg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(state.theMsg)
    }
    state.theMsg = ""
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def messageHandler() {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    if(state.theMsg.contains("%avg%")) {state.theMsg = state.theMsg.replace('%avg%', "${state.theAverage}" )}

    if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
}

def pushNow() {
    if(logEnable) log.debug "In pushNow (${state.version})"
    thePushMessage = "${app.label} \n"
    thePushMessage += state.theMsg
    if(logEnable) log.debug "In pushNow - Sending message: ${thePushMessage}"
    if(state.low) pushMessage.deviceNotification(thePushMessage)
    if(state.high) pushMessage.deviceNotification(thePushMessage)
    state.sentPush = true
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

def setDefaults() {
	if(logEnable == null){logEnable = false}
    state.nTimes = 0
    state.lTimes = 0
    state.hTimes = 0
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
