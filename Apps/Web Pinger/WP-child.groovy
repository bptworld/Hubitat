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
 *  2.0.9 - 06/13/20 - Minor adjustments, error catching
 *  2.0.8 - 06/05/20 - Minor adjustments
 *  2.0.7 - 05/25/20 - Little adjustments here and there
 *  2.0.6 - 04/27/20 - Cosmetic changes
 *  2.0.5 - 04/18/20 - Adjustments
 *  2.0.4 - 12/09/19 - Fixed a bug 
 *  2.0.3 - 12/04/19 - Added more logging
 *  2.0.2 - 12/03/19 - Added more time options. Cosmetic changes
 *  2.0.1 - 09/06/19 - Add new section to 'Turn Switch(es) OFF if URL is not available, ON if everything is good'
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  1.0.4 - 04/15/19 - Code cleanup
 *  1.0.3 - 03/12/19 - Fixed stuff
 *  1.0.2 - 01/15/19 - Updated footer with update check and links
 *  1.0.1 - 01/10/19 - Tons of cosmetic changes. Added in Push option, time between polls. Changed it up to turn the switch off
 *						if website/internet becomes active again. Added in all the normal stuff - like debug logging
 *  1.0.0 - 01/09/19 - Hubitat Port of ST app 'SmartPing' - 2016 Jason Botello
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Web Pinger"
	state.version = "2.0.9"
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
			input "timeToPing", "enum", title: "Time between pings (minutes)", submitOnChange: true,  options: ["30s","1","5","10","15","30", "59"], required: true, Multiple: false
            paragraph "<small>* Minimum recommended interval is 5 minutes.</small>"
            if(timeToPing == "30s") paragraph "<b>30 second interval is not recommended and might slow down your hub.</b>"
            if(timeToPing == "1") paragraph "<b>1 minute interval is not recommended and might slow down your hub.</b>"
            input(name: "threshold", title:"False alarm safety net (minutes)", type: "number", required: true, defaultValue:2)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Turn Switch(es) ON if URL is not available, OFF if everything is good.")) {
            input "switches", "capability.switch", title: "Control these switches", multiple: true, required: false, submitOnChange: true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Turn Switch(es) OFF if URL is not available, ON if everything is good.")) {
            input "switches2", "capability.switch", title: "Control these switches", multiple: true, required: false, submitOnChange: true
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
            input(name: "resetSwitches", type: "bool", defaultValue: false, title: "Auto reset Switches?", description: "Auto reset Switches", submitOnChange: true)
            if(resetSwitches) input(name: "resetTime", title:"Reset swtiches after (seconds)", type: "number", required: true, defaultValue:60)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Notifications")) {
			input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
			if(sendPushMessage) {
				paragraph "Enter in a custom message you would like sent when website is not available.<br>ie. Web Pinger: Web Request failed to Google"
				input "message", "text", required: true, title: "Message to Push"
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: false, title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
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
	if (validateURL()) {
		state.downHost = false
		state.pollVerify = false
		runIn(5, poll)
    }
}

def validateURL() {
		try {
			state.website = website.toLowerCase()
   	 		state.website = state.website.trim()
  	  		if (state.website.startsWith("http://")) {
   	 			state.website = state.website.replace("http://", "")
       			state.website = state.website.replace("www.", "")
    		}
   	 		if (state.website.startsWith("https://")) {
    			state.website = state.website.replace("https://", "")
       			state.website = state.website.replace("www.", "")
    		}
    		if (state.website.startsWith("www.")) {
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

def poll() {
		def reqParams = [
            uri: "http://${state.website}",
            timeout: 30
    	]
    	if (state.validURL) {
    		try {
        		httpGet(reqParams) { resp ->
					if(logEnable) log.debug "In Poll - Response was ${resp.status}"
            		if (resp.status == 200) {
                		if (state.downHost) {
            				if(switches) turnOffHandler()
                            if(switches2) turnOnHandler()
                    		if(logEnable) log.debug "Successful response (${resp.status}) from ${state.website}"
                		} else {
							if(switches) turnOffHandler()
                            if(switches2) turnOnHandler()
                    		if(logEnable) log.debug "Successful response (${resp.status}) from ${state.website}"
                		}
            		} else { 
            			if (!state.downHost) {
                			if (!state.pollVerify) {
        						runIn(60*threshold, pollVerify)
            					state.pollVerify = true
            				}
                			if(logEnable) log.debug "Request failed (${resp.status}) to ${state.website}, calling pollVerify with a ${threshold} minute threshold"
                		} else {
                			if(logEnable) log.debug "pollVerify already called"
                		}
            		}
        		}
    		} catch (e) {
        		if (!state.downHost) {
        			if (!state.pollVerify) {
        				runIn(60*threshold, pollVerify)
            			state.pollVerify = true
            		}
            		if(logEnable) log.debug "Request failed (NO status code) to ${state.website}, calling pollVerify with a ${threshold} minute threshold"
        		} else {
                    if(switches) turnOnHandler()
                    if(switches2) turnOffHandler()
           			if(logEnable) log.debug "pollVerify already called"
        		}
    		}
    	}
        if(timeToPing == "30s") runIn(30, poll)   
        if(timeToPing == "1") schedule("0 0/1 * * * ?", poll)
    	if(timeToPing == "5") schedule("0 0/5 * * * ?", poll)
		if(timeToPing == "10") schedule("0 0/10 * * * ?", poll)
		if(timeToPing == "15") schedule("0 0/15 * * * ?", poll)
		if(timeToPing == "30") schedule("0 0/30 * * * ?", poll)
		if(timeToPing == "59") schedule("0 0/59 * * * ?", poll)
}

def pollVerify() {
	def reqParams = [
		uri: "http://${state.website}",
        timeout: 30
	]
    	try {
        	httpGet(reqParams) { resp ->
				if(logEnable) log.debug "In pollVerify - Response was ${resp.status}"
            	if (resp.status == 200) {
                	state.downHost = false
                	state.pollVerify = false
                	if(switches) turnOffHandler()
                    if(switches2) turnOnHandler()
                	if(logEnable) log.debug "Successful response (${resp.status}) from ${state.website}, false alarm avoided"
            	} else {
            		state.downHost = true
                	state.pollVerify = false
            		if(switches) turnOnHandler()
                    if(switches2) turnOffHandler()
                	if(logEnable) log.debug "Request failed (${resp.status}) to ${state.website}"
            	}
        	}
    	} catch (e) {
        	state.downHost = true
        	state.pollVerify = false
        	if(switches) turnOnHandler()
            if(switches2) turnOffHandler()
            if(sendPushMessage) pushNow()
        	if(logEnable) log.debug "Request failed to ${state.website}"
    	}
}

def turnOnHandler() {
	if (switches) {
        switches.each{ s1 ->
            theStatus = s1.currentValue("switch")
    	    if(theStatus == "off") s1.on()
            if(logEnable) log.debug "In turnOnHandler - Switches 1 - Turning on ${s1}"
        }
        
        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches.each{ rs1 ->
                theStatus = rs1.currentValue("switch")
                if(theStatus == "on") rs1.off()
                if(logEnable) log.debug "In turnOnHandler - Switches 1 - Resetting - Turning off ${rs1}"
            }
        }
   	}
    
    if (switches2) {
    	switches2.each{ s2 ->
            theStatus = s2.currentValue("switch")
    	    if(theStatus == "off") s2.on()
            if(logEnable) log.debug "In turnOnHandler - Switches 2 - Turning on ${s2}"
        }
		
        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches2.each{ rs2 ->
                theStatus = rs2.currentValue("switch")
                if(theStatus == "on") rs2.off()
                if(logEnable) log.debug "In turnOnHandler - Switches 2 - Resetting - Turning off ${rs2}"
            }
        }
   	}
}

def turnOffHandler() {
    if (switches1) {
        switches1.each{ s1 ->
            theStatus = s1.currentValue("switch")
    	    if(theStatus == "on") s1.off()
            if(logEnable) log.debug "In turnOnHandler - Switches 1 - Turning off ${s1}"
        }
        
        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches1.each{ rs1 ->
                theStatus = rs1.currentValue("switch")
                if(theStatus == "off") rs1.on()
                if(logEnable) log.debug "In turnOnHandler - Switches 1 - Resetting - Turning on ${rs1}"
            }
        }
   	}
    
    if (switches2) {
    	switches2.each{ s2 ->
            theStatus = s2.currentValue("switch")
    	    if(theStatus == "on") s2.off()
            if(logEnable) log.debug "In turnOnHandler - Switches 2 - Turning off ${s2}"
        }
		
        if(resetSwitches) {
            rTime = resetTime * 1000
            pauseExecution(rTime)
            switches2.each{ rs2 ->
                theStatus = rs2.currentValue("switch")
                if(theStatus == "off") rs2.on()
                if(logEnable) log.debug "In turnOnHandler - Switches 2 - Resetting - Turning on ${rs2}"
            }
        }
    }
}

def pushNow(){
	if(logEnable) log.debug "In pushNow - Sending message"
	if(state.downHost) {
		sendPushMessage.deviceNotification(message)
	}	
}
	
// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable) log.debug "In setDefaults..."
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

