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

def setVersion(){
	state.version = "1.0.8"
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
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Hub Watchdog</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
            paragraph "Simple way to monitor if your hub is slowing down or not."
			paragraph "<b>Notes:</b>"
			paragraph "- You can use any type of 'switched' device you want to test. Virtual, Zwave or Zigbee<br>- Remember, any device you use will turn off after 5 seconds to test.<br>- Best to use an extra plugin module for testing."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Device to watch")) {
            input(name: "watchDevice", type: "capability.switch", title: "Device", required: true, multiple: false)
            input "maxDelay", "text", title: "Max delay allowed (in milliseconds, ie. .200, .500, etc.)", required: true, defaultValue: ".500", submitOnChange: true
            input "warnValue", "text", title: "Get a warning in over this value (in milliseconds, ie. .200, .500, etc.)", required: true, defaultValue: ".400", submitOnChange: true
            paragraph "<small>* This will not send a notification but rather color code the value in the report so it stands out.</small>"
            input "triggerMode", "enum", title: "Time Between Tests", submitOnChange: true, options: ["1_Min","5_Min","10_Min","15_Min","30_Min","1_Hour","3_Hour"], required: true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Notifications")) {
            paragraph "If delay is over the max delay, it will automaticaly rerun the test in 1 minute. If 3 tests fail in a row, then it will do the following"
			input "isDataDevice", "capability.switch", title: "Turn this device on", required: false, multiple: false         
			input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple: true, required: false, submitOnChange: true
            if(sendPushMessage) {
                input(name: "pushAll", type: "bool", defaultValue: "false", title: "Sometimes you may want to get ALL reading pushed. Turn this option 'ON' to receive all readings. <small><b>* Warning: depending on your settings, this could be a lot of messages!</b></small>", description: "Push All", submitOnChange: "true")
                
                paragraph "<u>Optional wildcards:</u><br>%adelay% - returns the Actual Delay value<br>%mdelay% - returns the Max Delay value"
                input(name: "nRandom", type: "bool", defaultValue: "false", title: "Random message?", description: "Random", submitOnChange: "true")
                if(!nRandom) input "nMessage", "text", title: "Message to be spoken - Single message",  required: true, defaultValue: "Hub Watchdog is reporting a delay of %adelay%, which is over the max %mdelay%"
                if(nRandom) {
				    input "nMessage", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				    input(name: "nMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
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
        section(getFormat("header-green", "${getImage("Blank")}"+" Data Device")) {}
		section("Instructions for Data Device:", hideable: true, hidden: true) {
            paragraph "<b>** This is where the data is stored for the reports **</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Hub Watchdog Tile'<br> - Use our 'Hub Watchdog Driver' as the Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your data on a tile!"
			}
		section() {
			input(name: "sendToDevice", type: "capability.actuator", title: "Vitual Device created to send the data to:", submitOnChange: true)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Manual Run")) {
            paragraph "Hub Watchdog is set to run once an hour. To run now, click this button<br>Remember to save any changes before running the test."
            input "testBtn1", "button", title: "Run Test Now"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "reportOptions", title: "Reports", description: "Click here to view the Data Reports."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "debugging", submitOnChange: true)
		}
		display2()
	}
}

def reportOptions(){
    dynamicPage(name: "reportOptions", title: "Report Data", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Report Data")) { 
            if(logEnable) log.debug "In bringOverResults (${state.version})"
            theReadings = sendToDevice.currentValue("readings1")
            theDataPoints1 = sendToDevice.currentValue("dataPoints1")
            theDataPoints2 = sendToDevice.currentValue("dataPoints2")
            theDataPoints3 = sendToDevice.currentValue("dataPoints3")
            theDataPoints4 = sendToDevice.currentValue("dataPoints4")
            theDataPoints5 = sendToDevice.currentValue("dataPoints5")
            theDataPoints6 = sendToDevice.currentValue("dataPoints6")
            theDataPoints7 = sendToDevice.currentValue("dataPoints7")
            theDataPoints8 = sendToDevice.currentValue("dataPoints8")
            readingsSize1 = sendToDevice.currentValue("readingsSize1")
            listSizeB = sendToDevice.currentValue("listSizeB")
            listSizeW = sendToDevice.currentValue("listSizeW")
            
            meanD = sendToDevice.currentValue("meanD")
            medianD = sendToDevice.currentValue("medianD")
            minimumD = sendToDevice.currentValue("minimumD")
            maximumD = sendToDevice.currentValue("maximumD")
            
            rangeD = "${minimumD} - ${maximumD}"
            
            paragraph "Testing Device: <b>${watchDevice}</b>"
            reportStats1 = "<table width='100%'><tr><td>Number of Data Points: ${readingsSize1}<br>Over Max Threshold: ${listSizeB}<br>Over Warning Threshold: ${listSizeW}<br>Current Max Delay: ${maxDelay}<br>Current Warning Delay: ${warnValue}</td></tr></table>"
            
            reportStats2= "<table width='100%'><tr><td>Mean Delay: ${meanD}<br>Median Delay: ${medianD}<br>Minimum Delay: ${minimumD}<br>Maximum Delay: ${maximumD}</td></tr></table>"
            
            paragraph "<table width='100%'><tr><td width='45%'>${reportStats1}</td><td width='10%'> </td><td width='45%'>${reportStats2}</td></tr></table>"
            paragraph "<hr>"
            report1 = "<table width='100%' align='center' border='1'><tr><td colspan='4'><b>Raw Data - Last 80 Readings</b></a></td></tr>"
            report1+= "<tr><td width='25%'>${theDataPoints1}</td><td width='25%'>${theDataPoints2}</td><td width='25%'>${theDataPoints3}</td><td width='25%'>${theDataPoints4}</td></tr>"
            report1+= "<tr><td>${theDataPoints5}</td><td>${theDataPoints6}</td><td>${theDataPoints7}</td><td>${theDataPoints8}</td></tr>"
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
	initialize()
}

def initialize() {
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

def testingDevice() {  
    if(logEnable) log.debug "In testingDevice (${state.version}) - Reseting device to off and waiting 5 seconds to continue"
    watchDevice.off()
    pauseExecution(5000)
    log.trace "Hub Watchdog - ***** Starting Test *****"
    if(logEnable) log.debug "In testingDevice - Turning Device On"
    state.testInProgress = "no"
    watchDevice.on()
}

def startTimeHandler(evt) {
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
    
def endTimeHandler(evt) {
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
    if(sendToDevice) {
        if(logEnable) log.debug "In sendNotification - Sending data: ${state.timeDiffMs} to ${sendToDevice}"
        sendToDevice.maxDelay(maxDelay)
        sendToDevice.warnValue(warnValue)
        sendToDevice.dataPoint1(state.timeDiffMs)
    }
    if(sendPushMessage && pushAll) pushHandler()
    
    def timeDiff = state.timeDiffMs.toFloat()
    def mDelay = maxDelay.toFloat()
    
    if(timeDiff >= mDelay) {
        if(state.failedCount <= 2) {
            state.failedCount = state.failedCount + 1
            if(logEnable) log.debug "In sendNotification - failedCount: ${state.failedCount} - Waiting 1 minute and will run again"
            runIn(60, testingDevice)
        } else {  
            if(logEnable) log.debug "In sendNotification - failedCount: ${state.failedCount} - Sending Notifications"
            if(isDataDevice) isDataDevice.on()
    
            if(sendPushMessage && !pushAll) pushHandler()
        
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
        if(logEnable) log.debug "In sendNotification - No need to send notifications"
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
	if(sendPushMessage) {
		pushMessage = "${app.label} \n"
		pushMessage += msg
		if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
        sendPushMessage.deviceNotification(pushMessage)
	}	
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "testBtn1"){
        testingDevice()
    }
}
    
// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
    state.failedCount = 0
    if(state.testInProgress == null || state.testInProgress == "") state.testInProgress = "no"
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Hub Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a><br>${state.version}</div>"
	}       
}
