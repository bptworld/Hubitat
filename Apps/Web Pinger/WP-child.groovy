/**
 *  ****************  Web Pinger Child  ****************
 *
 *  Design Usage:
 *  Monitor a website and trigger automations if not found or if your internet goes down.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *
 *  Thanks to Jason Botello for the original 2016 'SmartPing' code that I based this app off of.
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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
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
 *  2.1.9 - 12/29/20 - More Adjustments
 *  2.1.8 - 12/29/20 - Adjustments
 *  2.1.7 - 08/26/20 - Cosmetic changes
 *  2.1.6 - 08/25/20 - Added notifications when website is available again, other enhancements
 *  2.1.5 - 07/17/20 - Added auto logs off after 1 hour
 *  2.1.4 - 07/17/20 - Adjustments
 *  2.1.3 - 06/19/20 - Added The Flasher
 *  2.1.2 - 06/16/20 - More changes. Make sure to reinput your Switches.
 *  2.1.1 - 06/15/20 - Fixed 'Control Switch'
 *  2.1.0 - 06/15/20 - Added 'Control Switch'
 *  ---
 *  1.0.0 - 01/09/19 - Hubitat Port of ST app 'SmartPing' - 2016 Jason Botello
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion() {
    state.name = "Web Pinger"
	state.version = "2.1.9"
}

definition(
	name: "Web Pinger Child",
	namespace: "BPTWorld",
	author: "Bryan Turcotte",
	description: "Monitor a website and trigger automations if not found or if your internet goes down.",
	category: "Convenience",
	parent: "BPTWorld:Web Pinger",
	iconUrl: "",
	iconX2Url: "",
	iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Web%20Pinger/WP-child.groovy",
)

preferences {
	page(name: "pageConfig")
}

def pageConfig() {
	dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Create a new child app for each website you would like to ping.<br><br>- Enter in any valid URL, ie. google.com<br>- Enter in how long between pings<br>- Enter in the False Alarm Safety Net, this is how long it will keep trying before turning on the switches and/or sending out a Pushover notification.<br>- Optional - Have the app send a Pushover if website is not available"	
		}
		
		section(getFormat("header-green", "${getImage("Blank")}"+" URL to monitor")) {
            input(name: "website", title:"URL", type: "text", required: true)
			input "timeToPing", "enum", title: "Time between pings (minutes)", submitOnChange: true,  options: ["1","5","10","15","30", "59"], required: true, Multiple: false
            paragraph "<small>* Minimum recommended interval is 5 minutes.</small>"
            if(timeToPing == "1") paragraph "<b>1 minute interval is not recommended and might slow down your hub.</b>"
            input(name: "threshold", title:"False alarm safety net (minutes) - Should be long enough for a couple of regular pings", type: "number", required: true, defaultValue:20)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Control Switch")) {
            paragraph "Recomended to create a virtual switch with the 'Enable auto off' set to '1s'."
            input "sendPingSwitch", "capability.switch", title: "Turn this switch 'on' to send a new 'ping' at any time", required:false, submitOnChange:true
        }
    
        section(getFormat("header-green", "${getImage("Blank")}"+" Actions")) {
            paragraph "<b>The following is two different groups. You should NOT use the same switch in both groups. Bad things WILL happen.</b>"
            input "switches1", "capability.switch", title: "Turn Switch(es) ON if URL is not available, OFF if everything is good.", multiple: true, required: false, submitOnChange: true

            input "switches2", "capability.switch", title: "Turn Switch(es) OFF if URL is not available, ON if everything is good.", multiple: true, required: false, submitOnChange: true
            paragraph "<hr>"
            input "resetSwitches", "bool", defaultValue:false, title: "Auto reset Switches?", description: "Auto reset Switches", submitOnChange: true
            if(resetSwitches) {
                paragraph "<small>This will change the switch(es) back to their previous state. ie. If on, they will turn off. If off, they will turn on.</small>"
                input(name: "resetTime", title:"Reset swtiches after (seconds) even if website is still down", type: "number", required: true, defaultValue:60)
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Notifications")) {     
            paragraph "Remember: If your internet is truly down, Push notifications will not work."
			input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
			if(sendPushMessage) {
				paragraph "Enter in a custom message you would like sent when website is not available.<br>ie. Web Pinger: Web Request failed to Google"
				input "message", "text", required: true, title: "Message to Push"
                
                input "notifyWhenBack1", "bool", title: "Notify when available again", defaultValue:false, submitOnChange:true
                if(notifyWhenBack1) {
                    paragraph "Enter in a custom message you would like sent when website is available again.<br>ie. Web Pinger: Google is back online"
                    input "messageUp", "text", required: true, title: "Message to Push"
                }
			}
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashPreset", "number", title: "Select the Preset to use when website is not available. (1..5)", required:true, submitOnChange:true
                
                input "notifyWhenBack2", "bool", title: "Notify when available again", defaultValue:false, submitOnChange:true
                if(notifyWhenBack2) {
                    input "theFlasherDeviceUp", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                    input "flashPresetUp", "number", title: "Select the Preset to use when website is available again. (1..5)", required:true, submitOnChange:true
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
            input "logEnable", "bool", title: "Enable Debug Logging", description: "debug", defaultValue: false
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
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp) {
        log.info "${app.label} is Paused"
    } else {
        if(validateURL()) {
            state.downHost = false
            state.pollVerify = false
            schedule("0 0/${timeToPing} * * * ?", pollHandler)
            runIn(5, pollHandler)
        }
        if(sendPingSwitch) subscribe(sendPingSwitch, "switch.on", pollHandler)
    }
}

def validateURL() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        try {
            state.website = website.toLowerCase()
            state.website = state.website.trim()
            if(state.website.startsWith("http://")) {
                state.website = state.website.replace("http://", "")
                state.website = state.website.replace("www.", "")
            }
            if(state.website.startsWith("https://")) {
                state.website = state.website.replace("https://", "")
                state.website = state.website.replace("www.", "")
            }
            if(state.website.startsWith("www.")) {
                state.website = state.website.replace("www.", "")
            }
            if(logEnable) log.debug "In validateURL - URL is valid"
            state.validURL = true
            return true
        }
        catch (e) {
            if(logEnable) log.debug "In validateURL - URL is NOT valid"
            state.validURL = false
            return false
        }
    }
}

def pollHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        def reqParams = [
            uri: "http://${state.website}",
            timeout: 30
        ]

        if(state.validURL) {
            try {
                httpGet(reqParams) { resp ->
                    if(logEnable) log.debug "In pollHandler (${state.version}) - Response was ${resp.status}"
                    if(resp.status == 200) {
                        if(state.downHost) {
                            if(logEnable) log.debug "In pollHandler - Successful response (${resp.status}) from ${state.website} - Live Again!"
                            state.downHost = false
                            state.pollVerify = false
                            if(switches1) turnOffHandler()
                            if(switches2) turnOnHandler() 
                            //  **** NEW - Send notification that website is back ****
                            if(sendPushMessage && notifyWhenBack1) pushNow()

                            if(useTheFlasher && notifyWhenBack2) {
                                flashData = "Preset::${flashPresetUp}"
                                theFlasherDeviceUp.sendPreset(flashData)
                            }
                        } else {
                            if(logEnable) log.debug "In pollHandler - Successful response (${resp.status}) from ${state.website} - All Good"
                            state.downHost = false
                            state.pollVerify = false
                            if(switches1) turnOffHandler()
                            if(switches2) turnOnHandler()
                        }
                    } else { 
                        if(!state.downHost) {
                            if(!state.pollVerify) {
                                if(logEnable) log.debug "In pollHandler - Request failed (${resp.status}) to ${state.website}, running pollVerify in ${threshold} minutes"
                                state.pollVerify = true
                                runIn(60*threshold, pollVerify)
                            }
                        } else {
                            if(logEnable) log.debug "In pollHandler - pollVerify already called"
                        }
                    }
                }
            } catch (e) {
                if(!state.downHost) {
                    if(!state.pollVerify) {
                        state.pollVerify = true
                        if(logEnable) log.debug "In pollHandler - err - Request failed (NO status code) to ${state.website}, running pollVerify in ${threshold} minutes"
                        runIn(60*threshold, pollVerify)          			
                    }
                } else {
                    if(logEnable) log.debug "In pollHandler - err - pollVerify already called"
                }
            }
        }
    }
}

def pollVerify() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        def reqParams = [
            uri: "http://${state.website}",
            timeout: 30
        ]
        try {
            httpGet(reqParams) { resp ->
                if(logEnable) log.debug "In pollVerify (${state.version}) - Response was ${resp.status}"
                if(resp.status == 200) {
                    if(logEnable) log.debug "In pollVerify - Successful response (${resp.status}) from ${state.website}, false alarm avoided"
                    state.downHost = false
                    state.pollVerify = false
                    if(switches1) turnOffHandler()
                    if(switches2) turnOnHandler()
                } else {
                    if(logEnable) log.debug "In pollVerify - Request failed (${resp.status}) to ${state.website} - Verified"
                    state.downHost = true
                    state.pollVerify = false
                    if(switches1) turnOnHandler()
                    if(switches2) turnOffHandler()
                }
            }
        } catch (e) {
            state.downHost = true
            state.pollVerify = false
            if(switches1) turnOnHandler()
            if(switches2) turnOffHandler()
            if(sendPushMessage) pushNow()
            if(useTheFlasher) {
                flashData = "Preset::${flashPreset}"
                theFlasherDevice.sendPreset(flashData)
            }
            if(logEnable) log.debug "In pollVerify - err - Request failed to ${state.website} - Verified"
        }
    }
}

def turnOnHandler() {
    if(switches1) {
        switches1.each { s1 ->
            theStatus = s1.currentValue("switch")
            if(theStatus == "off") {
                s1.on()
                if(logEnable) log.debug "In turnOnHandler - Switches 1 - Turning on ${s1}"
            }
        }

        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches1.each{ rs1 ->
                theStatus = rs1.currentValue("switch")
                if(theStatus == "on") {
                    rs1.off()
                    if(logEnable) log.debug "In turnOnHandler - Switches 1 - Resetting - Turning off ${rs1}"
                }
            }
        }
    }

    if(switches2) {
        switches2.each{ s2 ->
            theStatus = s2.currentValue("switch")
            if(theStatus == "off") {
                s2.on()
                if(logEnable) log.debug "In turnOnHandler - Switches 2 - Turning on ${s2}"
            }
        }

        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches2.each{ rs2 ->
                theStatus = rs2.currentValue("switch")
                if(theStatus == "on") {
                    rs2.off()
                    if(logEnable) log.debug "In turnOnHandler - Switches 2 - Resetting - Turning off ${rs2}"
                }
            }
        }
    }
}

def turnOffHandler() {
    if(switches1) {
        switches1.each{ s1 ->
            theStatus = s1.currentValue("switch")
            if(theStatus == "on") {
                s1.off()
                if(logEnable) log.debug "In turnOnHandler - Switches 1 - Turning off ${s1}"
            }
        }

        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches1.each{ rs1 ->
                theStatus = rs1.currentValue("switch")
                if(theStatus == "off") {
                    rs1.on()
                    if(logEnable) log.debug "In turnOnHandler - Switches 1 - Resetting - Turning on ${rs1}"
                }
            }
        }
    }

    if(switches2) {
        switches2.each{ s2 ->
            theStatus = s2.currentValue("switch")
            if(theStatus == "on") {
                s2.off()
                if(logEnable) log.debug "In turnOnHandler - Switches 2 - Turning off ${s2}"
            }
        }

        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches2.each{ rs2 ->
                theStatus = rs2.currentValue("switch")
                if(theStatus == "off") {
                    rs2.on()
                    if(logEnable) log.debug "In turnOnHandler - Switches 2 - Resetting - Turning on ${rs2}"
                }
            }
        }
    }
}

def pushNow(data) {
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending message"
    if(state.downHost) {
        sendPushMessage.deviceNotification(message)
    } else {
        sendPushMessage.deviceNotification(messageUp)
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
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
            if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch} - ${eSwitch}"
        }
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
}
