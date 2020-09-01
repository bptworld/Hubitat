/**
 *  ****************  Hub Watchdog App  ****************
 *
 *  Design Usage:
 *  Simple way to monitor if your hub is slowing down or not.
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
 *  1.1.3 - 09/01/20 - Changes for only 40 data points
 *  1.1.2 - 08/30/20 - lots of cosmetic updates
 *  1.1.1 - 07/11/20 - Added user selectable 'Max number of times it can fail'
 *  1.1.0 - 07/10/20 - Added Maintenance Override Options
 *  1.0.9 - 05/16/20 - Logging changes
 *  1.0.8 - 04/27/20 - Cosmetic changes
 *  1.0.7 - 09/26/19 - Added a 'push all' option
 *  1.0.6 - 09/26/19 - Holds up to 80 data points, added color coding
 *  1.0.5 - 09/25/19 - Added a failsafe, test has to fail 3 times before notification is sent. Other small adjustments
 *  1.0.4 - 09/25/19 - Here comes the reports!
 *  1.0.3 - 09/25/19 - Added Rule Machine options, started working on reports section
 *  1.0.2 - 09/24/19 - Added Run time options
 *  1.0.1 - 09/24/19 - Added Data device options
 *  1.0.0 - 09/24/19 - Initial release.
 *
 */

import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Hub Watchdog"
	state.version = "1.1.3"
}

definition(
    name: "Hub Watchdog Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Simple way to monitor if your hub is slowing down or not.",
    category: "",
	parent: "BPTWorld:Hub Watchdog",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Hub%20Watchdog/HW-child.groovy",
)

preferences {
    page name: "pageConfig"
    page name: "reportOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
            paragraph "Simple way to monitor if your hub is slowing down or not."
			paragraph "<b>Notes:</b>"
			paragraph "- You can use any type of 'switched' device you want to test. Virtual, Zwave or Zigbee<br>- Remember, any device used will turn off after 5 seconds to test.<br>- Best to use an extra plugin module for testing."
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Data Device")) {
            paragraph "Each child app needs a virtual device to store the data."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have Hub Watchdog create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this Data Device (ie. 'HW - Virtual Data')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device to send the data to", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Hub Watchdog Driver'.</small>"
            }
            if(dataDevice) {
                paragraph "<i><b>Be sure to visit the device and fill in the options!</b></i>"
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Device to watch")) {
            paragraph "This is the actual device (virtual, zigbee or zwave) that will turn on and off for testing."
            input "watchDevice", "capability.switch", title: "Device", required: true, multiple: false, submitOnChange:true
            input "maxDelay", "text", title: "Max delay allowed (in milliseconds, ie. .200, .500, etc.)", required: true, defaultValue: ".500", submitOnChange: true
            input "warnValue", "text", title: "Get a warning in over this value (in milliseconds, ie. .200, .500, etc.)", required: true, defaultValue: ".400", submitOnChange: true
            paragraph "<small>* This will not send a notification but rather color code the value in the report so it stands out.</small>"
            input "triggerMode", "enum", title: "Time Between Tests", submitOnChange: true, options: ["1_Min","5_Min","10_Min","15_Min","30_Min","1_Hour","3_Hour"], required: true
            paragraph "If delay is over the max delay, it will automaticaly rerun the test in 1 minute. If x tests fail in a row, it will then use the Notification and RM options as set below."
            input "maxFail", "number", title: "Max number of times it can fail before taking actions (range: 1 to 20)", range: "1..20", required: true, defaultValue: 3, submitOnChange:true
        }
        
        if(watchDevice && maxDelay && warnValue && triggerMode && maxFail) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Notifications")) {
                input "isDataDevice", "capability.switch", title: "Turn this device on", required: false, multiple: false         
                input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple: true, required: false, submitOnChange: true
                if(sendPushMessage) {
                    input "pushAll", "bool", title: "Sometimes you may want to get ALL reading pushed. Turn this option 'ON' to receive all readings. <small><b>* Warning: depending on your settings, this could be a lot of messages!</b></small>", description: "Push All", defaultValue:false, submitOnChange:true

                    paragraph "<u>Optional wildcards:</u><br>%adelay% - returns the Actual Delay value<br>%mdelay% - returns the Max Delay value"
                    input "nRandom", "bool", title: "Random message?", description: "Random", defaultValue:false, submitOnChange:true
                    if(!nRandom) input "nMessage", "text", title: "Message to be spoken - Single message",  required: true, defaultValue: "Hub Watchdog is reporting a delay of %adelay%, which is OVER the max delay of %mdelay%"
                    if(nRandom) {
                        input "nMessage", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
                        input "nMsgList", "bool", title: "Show a list view of the messages?", description: "List View", defaultValue:true, submitOnChange:true
                        if(nMsgList) {
                            def nvalues = "${nMessage}".split(";")
                            nlistMap = ""
                            nvalues.each { item -> nlistMap += "${item}<br>"}
                            paragraph "${nlistMap}"
                        }
                    }
                }
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Rule Machine Options")) {
                def rules = RMUtils.getRuleList()
                paragraph "Perform an action with Rule Machine."
                input "rmRule", "enum", title: "Select which rules", multiple: true, options: rules
                input "rmAction", "enum", title: "Action", multiple: false, options: [
                    ["runRuleAct":"Run"],
                    ["stopRuleAct":"Stop"],
                    ["pauseRule":"Pause"],
                    ["resumeRule":"Resume"],
                    ["runRule":"Evaluate"],
                    ["setRuleBooleanTrue":"Set Boolean True"],
                    ["setRuleBooleanFalse":"Set Boolean False"]
                ]
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance Override Options")) {
                paragraph "Sometimes there is a period that you don't want this to run. ie. During the nightly maintenance period."
                input "maintTime", "bool", title: "Use Maintenance Override", defaultValue:false, submitOnChange:true
                if(maintTime) {
                    input "QfromTime", "time", title: "Maintenance Time Start", required: false, width: 6
                    input "QtoTime", "time", title: "Maintenance Time End", required: false, width: 6
                    input "midnightCheckQ", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                } else {
                    app.removeSetting("QfromTime")
                    app.removeSetting("QtoTime")
                    app.removeSetting("midnightCheckQ")
                }
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Manual Run")) {
                paragraph "Hub Watchdog is set to run once an hour. To run now, click this button<br><small>* Remember to save any changes before running the test.</small>"
                input "testBtn1", "button", title: "Run Test Now"
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
                href "reportOptions", title: "Reports", description: "Click here to view the Data Reports."
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
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "debugging", submitOnChange:true
		}
		display2()
	}
}

def reportOptions(){
    dynamicPage(name: "reportOptions", title: "Report Data", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Report Data")) { 
            if(logEnable) log.debug "In bringOverResults (${state.version})"
            theReadings = dataDevice.currentValue("readings1")
            theDataPoints1 = dataDevice.currentValue("dataPoints1")
            theDataPoints2 = dataDevice.currentValue("dataPoints2")
            readingsSize1 = dataDevice.currentValue("readingsSize1")
            listSizeB = dataDevice.currentValue("listSizeB")
            listSizeW = dataDevice.currentValue("listSizeW")
            
            meanD = dataDevice.currentValue("meanD")
            medianD = dataDevice.currentValue("medianD")
            minimumD = dataDevice.currentValue("minimumD")
            maximumD = dataDevice.currentValue("maximumD")
            
            rangeD = "${minimumD} - ${maximumD}"
            
            paragraph "Testing Device: <b>${watchDevice}</b>"
            reportStats1 = "<table width='100%'><tr><td>Number of Data Points: ${readingsSize1}<br>Over Max Threshold: ${listSizeB}<br>Over Warning Threshold: ${listSizeW}<br>Current Max Delay: ${maxDelay}<br>Current Warning Delay: ${warnValue}</td></tr></table>"
            
            reportStats2= "<table width='100%'><tr><td>Mean Delay: ${meanD}<br>Median Delay: ${medianD}<br>Minimum Delay: ${minimumD}<br>Maximum Delay: ${maximumD}</td></tr></table>"
            
            paragraph "<table width='100%'><tr><td width='45%'>${reportStats1}</td><td width='10%'> </td><td width='45%'>${reportStats2}</td></tr></table>"
            paragraph "<hr>"
            report1 = "<table width='100%' align='center' border='1'><tr><td colspan='4'><b>Raw Data - Readings</b></a></td></tr>"
            report1+= "<tr><td width='25%'>${theDataPoints1}</td><td width='25%'>${theDataPoints2}</td></tr>"
            report1+= "</table>"
            paragraph "${report1}"
            paragraph "<hr>"
        }
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
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        subscribe(watchDevice, "switch.on", startTimeHandler)
        subscribe(watchDevice, "switch.off", endTimeHandler)

        if(triggerMode == "1_Min") runEvery1Minute(testingDevice)
        if(triggerMode == "5_Min") runEvery5Minutes(testingDevice)
        if(triggerMode == "10_Min") runEvery10Minutes(testingDevice)
        if(triggerMode == "15_Min") runEvery15Minutes(testingDevice)
        if(triggerMode == "30_Min") runEvery30Minutes(testingDevice)
        if(triggerMode == "1_Hour") runEvery1Hour(testingDevice)
        if(triggerMode == "3_Hour") runEvery3Hours(testingDevice)
    }
}

def testingDevice() {  
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        maintHandler()
        if(isMaintTime) {
            if(logEnable) log.debug "In testingDevice (${state.version}) - Maintenance Time - Testing will resume once outside this time window"
        } else {
            if(logEnable) log.debug "In testingDevice (${state.version}) - Reseting device to off and waiting 5 seconds to continue"
            watchDevice.off()
            pauseExecution(5000)
            log.trace "Hub Watchdog - ***** Starting Test *****"
            if(logEnable) log.debug "In testingDevice - Turning Device On"
            state.testInProgress = "no"
            watchDevice.on()
        }
    }
}

def startTimeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In startTimeHandler (${state.version})"
        cStatus = watchDevice.currentValue("switch")
        if(cStatus == "on") {
            prevLastActivity = watchDevice.getLastActivity()
            if(logEnable) log.debug "In startTimeHandler - prevLastActivity: ${prevLastActivity}" 
            long unxPrev = prevLastActivity.getTime()
            unxPrev = unxPrev
            state.unxPrev = unxPrev
            state.testInProgress = "yes"
        } else {
            if(logEnable) log.debug "In startTimeHandler - Device wasn't on! - device: ${cStatus}"
        }
        if(logEnable) log.debug "In startTimeHandler - Turning device off AFTER 5 seconds"
        pauseExecution(5000)
        watchDevice.off()
    }
}
    
def endTimeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In endTimeHandler (${state.version})"
        cStatus = watchDevice.currentValue("switch")
        if(cStatus == "off" && state.testInProgress == "yes") {
            newLastActivity = watchDevice.getLastActivity()
            if(logEnable) log.debug "In startTimeHandler - newLastActivity: ${newLastActivity}"  
            long timeDiff
            long unxNow = newLastActivity.getTime()
            unxNow = unxNow
            state.unxNow = unxNow
            runIn(1, lookingAtData)
        } else {
            if(logEnable) log.debug "In endTimeHandler - Device wasn't off or testInProgress was no! - device: ${cStatus} - testInProgress: ${state.testInProgress}"
        }
    }
}

def lookingAtData() {
    try {
        def unxPrev = state.unxPrev.toInteger()
        def unxNow = state.unxNow.toInteger()
    
        if(logEnable) log.debug "In lookingAtData - unxNow: ${unxNow} - unxPrev: ${unxPrev}"
        timeDiff = Math.abs(unxNow-unxPrev)
        state.timeDiffMs = (timeDiff / 1000) - 5
    
        if(logEnable) log.debug "Hub Watchdog - ${app.label} - ${watchDevice} - timeDiff: ${state.timeDiffMs} msec"
    }
    catch(e) {
        log.warn "Something went wrong - ${e}"
    }
    state.testInProgress = "no"
    log.trace "Hub Watchdog - ***** Ending Test *****"
    sendNotification()
}

def sendNotification() {
    if(logEnable) log.debug "In sendNotification (${state.version})"
    if(dataDevice) {
        if(logEnable) log.debug "In sendNotification - Sending data: ${state.timeDiffMs} to ${dataDevice}"
        dataDevice.maxDelay(maxDelay)
        dataDevice.warnValue(warnValue)
        dataDevice.dataPoint1(state.timeDiffMs)
    }
    
    def timeDiff = state.timeDiffMs.toFloat()
    def mDelay = maxDelay.toFloat()    
    nMaxFail = maxFail ?: 3
    
    if(timeDiff >= mDelay) {
        if(state.failedCount <= nMaxFail) {
            state.failedCount = state.failedCount + 1
            if(logEnable) log.debug "In sendNotification - failedCount: ${state.failedCount} - Waiting 1 minute and will run again"
            runIn(60, testingDevice)
        } else {  
            if(logEnable) log.debug "In sendNotification - failedCount: ${state.failedCount} - Sending Notifications"
            if(isDataDevice) isDataDevice.on()
    
            if(sendPushMessage) {
                if(logEnable) log.debug "In sendNotification - Going to pushHandler"
                pushHandler()
            }
        
            if(rmRule) {
                if(logEnable) log.debug "In ruleMachineHandler - Rule: ${rmRule} - Action: ${rmAction}"
                RMUtils.sendAction(rmRule, rmAction, app.label)
            }
            
            state.failedCount = 0
        }
    } else {
        if(isDataDevice) isDataDevice.off()
        state.failedCount = 0
        state.testInProgress = "no"
        if(sendPushMessage && pushAll) {
            if(logEnable) log.debug "In sendNotification - pushAll is enabled, going to pushNow"
            msg = "Hub Watchdog is reporting a delay of ${timeDiff}, which is UNDER the max delay of ${mDelay}"
            pushNow(msg)
        } else {
            if(logEnable) log.debug "In sendNotification - No need to send notifications"
        }
    }
}

def pushHandler() {
    if(nRandom) {
        def nvalues = "${nMessage}".split(";")
		nvSize = nvalues.size()
		ncount = nvSize.toInteger()
    	def nrandomKey = new Random().nextInt(ncount)
        theMessage = nvalues[nrandomKey]
		if(logEnable) log.debug "In sendNotification - Random - theMessage: ${theMessage}"
	} else {
		theMessage = "${nMessage}"
		if(logEnable) log.debug "In sendNotification - Static - theMessage: ${theMessage}"
    }
    if (theMessage.contains("%adelay%")) {theMessage = theMessage.replace('%adelay%', "${state.timeDiffMs}" )}
    if (theMessage.contains("%mdelay%")) {theMessage = theMessage.replace('%mdelay%', "${maxDelay}" )}
    pushNow(theMessage)
}

def pushNow(msg) {
	if(logEnable) log.debug "In pushNow (${state.version})"
	if(sendPushMessage && msg) {
		pushMessage = "${app.label} \n"
		pushMessage += msg
		if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
        sendPushMessage.deviceNotification(pushMessage)
	}	
}

def maintHandler() {
	if(logEnable) log.debug "In maintHandler (${state.version})"
    isMaintTime = false
    
	if(QfromTime) {
        if(midnightCheckQ) {
            state.maintTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime)+1, new Date(), location.timeZone)
        } else {
		    state.maintTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
        }
    	if(state.maintTime) {
            if(logEnable) log.debug "In maintHandler - Time within range - Using Maint Time"
    		isMaintTime = true
		} else {
            if(logEnable) log.debug "In maintHandler - Time outside of range - Not using Maint Time"
			isMaintTime = false
		}
	} else {
        if(logEnable) log.debug "In maintHandler - NO Maint Time Specified"
	}
    return isMaintTime
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "testBtn1"){
        testingDevice()
    }
}
    
def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Hub Watchdog Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "${app.label}: Unable to create the data device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
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
    state.failedCount = 0
    if(state.testInProgress == null || state.testInProgress == "") state.testInProgress = "no"
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
        //if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
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
