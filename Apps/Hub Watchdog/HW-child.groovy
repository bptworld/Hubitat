/**
 *  ****************  Hub Watchdog App  ****************
 *
 *  Design Usage:
 *  Simple way to monitor if your hub is slowing down or not.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V1.0.0 - 02/16/19 - Initial release.
 *
 */

import hubitat.helper.RMUtils

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "HubWatchdogChildVersion"
	state.version = "v1.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
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
            input "maxDelay", "text", title: "Max delay allowed (in milliseconds, ie. .200, .500, etc.)", required: true, defaultValue: ".500"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Notifications")) {
            paragraph "If over the Max Delay, do the following"
			input "isDataDevice", "capability.switch", title: "Turn this device on", required: false, multiple: false         
			input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple: true, required: false, submitOnChange: true
            if(sendPushMessage) {
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
/**
            paragraph "<b>Rule Machine</b>"
            def rules = RMUtils.getRuleList()
            input "ruleMachine", "enum", title: "Select which rules to run", options: rules, multiple: false
*/            
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your counts on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Hub Watchdog Tile'<br> - Use our 'Hub Watchdog Driver' as the Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your data on a tile!"
			}
		section() {
			input(name: "sendToDevice", type: "capability.actuator", title: "Vitual Device created to send the data to:", submitOnChange: true)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Manual Run")) {
            paragraph "Hub Watchdog is set to run once an hour. To run now, click this button"
            input "testBtn1", "button", title: "Run Test Now"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "debugging")
		}
		display2()
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
    
	runEvery1Hour(testingDevice)

    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def testingDevice() {  
    if(logEnable) log.debug "In testingDevice (${state.version}) - Reseting device to off and waiting 5 seconds to continue"
    watchDevice.off()
    pauseExecution(5000)
    if(logEnable) log.debug "In testingDevice - ***** Starting Test *****"
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
    } else {
        if(logEnable) log.debug "In startTimeHandler - Device wasn't off or testInProgress was no! - device: ${cStatus} - testInProgress: ${state.testInProgress}"
    }
    runIn(1, lookingAtData)
}

def lookingAtData() {
    try {
        def unxPrev = state.unxPrev.toInteger()
        def unxNow = state.unxNow.toInteger()
    
        if(logEnable) log.debug "In lookingAtData - unxNow: ${unxNow} - unxPrev: ${unxPrev}"
        timeDiff = Math.abs(unxNow-unxPrev)
        state.timeDiffMs = (timeDiff / 1000) - 5
    
        log.info "Hub Watchdog - ${app.label} - ${watchDevice} - timeDiff: ${state.timeDiffMs} msec"
    }
    catch(e) {
        log.warn "Something went wrong - ${e}"
    }
    state.testInProgress = "no"
    if(logEnable) log.debug "In lookingAtData - ***** Ending Test *****"
    sendNotification()
}

def sendNotification() {
    if(logEnable) log.debug "In sendNotification (${state.version})"
    
    if(sendToDevice) {
        if(logEnable) log.debug "In sendNotification - Sending ${state.timeDiffMs} to ${sendToDevice}"
        sendToDevice.dataPoint1(state.timeDiffMs)
    }
    
    def timeDiff = state.timeDiffMs.toFloat()
    def mDelay = maxDelay.toFloat()
    
    if(timeDiff >= mDelay) {
        if(isDataDevice) isDataDevice.on()
    
        if(sendPushMessage) {
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
        
        if(ruleMachine) rulesHandler(ruleMachine)
    
    } else {
        if(isDataDevice) isDataDevice.off()
        if(logEnable) log.debug "In sendNotification - No need to send notifications"
    }
}

def rulesHandler(rules) {
    if(logEnable) log.debug "In rulesHandler - rules: ${rules}"
    try {
        RMUtils.sendAction(rules, "runRule", app.label)
    }
    catch(e) {
        log.debug "In rulesHandler - Something went wrong - ${e}"
    }
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Hub Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
