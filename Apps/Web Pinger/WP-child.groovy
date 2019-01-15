/**
 *  ****************  Web Pinger Child  ****************
 *
 *  Design Usage:
 *  Monitor a website and trigger automations if not found or if your internet goes down.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to Jason Botello for the original 2016 'SmartPing' code that I based this app off of.
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
 *  V1.0.2 - 01/15/19 - Updated footer with update check and links
 *  V1.0.1 - 01/10/19 - Tons of cosmetic changes. Added in Push option, time between polls. Changed it up to turn the switch off
 *						if website/internet becomes active again. Added in all the normal stuff - pause, enable/disable and debug
 *						logging.
 *  V1.0.0 - 01/09/19 - Hubitat Port of ST app 'SmartPing' - 2016 Jason Botello
 *
 */

def version(){"v1.0.2"}

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
)

preferences {
	page(name: "pageConfig")
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOGDEBUG("Updated with settings: ${settings}")
	unsubscribe()
	unschedule()
	logCheck()
	initialize()
}

def initialize() {
	if (validateURL()) {
		state.downHost = "false"
		state.pollVerify = "false"
		runIn(5, poll)
    }
}

def pageConfig() {
	dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Web Pinger</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Create a new child app for each website you would like to ping.<br><br>- Enter in any valid URL, ie. google.com<br>- Enter in how long between pings<br>- Enter in the False Alarm Safety Net, this is how long it will keep trying before turning on the switches and/or sending out a Pushover notification.<br>- Optional - Have the app send a Pushover if website is not available"	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" URL to monitor")) {
            input(name: "website", title:"URL", type: "text", required: true)
			input "timeToPing", "enum", title: "Time between pings (minutes)", submitOnChange: true,  options: ["5","10","15","30", "59"], required: true, Multiple: false
            input(name: "threshold", title:"False alarm safety net (minutes)", type: "number", required: true, defaultValue:2)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Turn Switch(es) ON if URL is not available, OFF if everything is good.")) {
            input "switches", "capability.switch", title: "Control these switches", multiple: true, required: false, submitOnChange: true
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false, submitOnChange: true
			if(sendPushMessage) {
				paragraph "Enter in a custom message you would like sent when website is not available.<br>ie. Web Pinger: Web Request failed to Google"
				input "message", "text", required: true, title: "Message to Push"
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def validateURL() {
	state.website = website.toLowerCase()
    	if (state.website.contains(".com") || state.website.contains(".net") || state.website.contains(".org") || state.website.contains(".biz") || state.website.contains(".us") || state.website.contains(".info") || state.website.contains(".io") || state.website.contains(".ca") || state.website.contains(".co.uk") || state.website.contains(".tv") || state.website.contains(":")) {
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
    		state.validURL = "true"
    		return true
	} else {
    		state.validURL = "false"
        	return false
    	}
}

def poll() {
	def reqParams = [
            uri: "http://${state.website}"
    	]
    	if (state.validURL == "true") {
    		try {
        		httpGet(reqParams) { resp ->
            			if (resp.status == 200) {
                			if (state.downHost == "true") {
            					turnOffHandler()
                    			LOGDEBUG("Successful response from ${state.website}")
                			} else {
								turnOffHandler()
                    			LOGDEBUG("Successful response from ${state.website}")
                			}
            			} else {
            				if (state.downHost == "false") {
                				if (state.pollVerify == "false") {
        						runIn(60*threshold, pollVerify)
            						state.pollVerify = "true"
            					}
                				LOGDEBUG("Request failed to ${state.website}, calling pollVerify with a ${threshold} minute threshold")
                			} else {
                				LOGDEBUG("pollVerify already called")
                			}
            			}
        		}
    		} catch (e) {
        		if (state.downHost == "false") {
        			if (state.pollVerify == "false") {
        				runIn(60*threshold, pollVerify)
            				state.pollVerify = "true"
            			}
            			LOGDEBUG("Request failed to ${state.website}, calling pollVerify with a ${threshold} minute threshold")
        		} else {
           			LOGDEBUG("pollVerify already called")
        		}
    		}
    	}
    if(timeToPing == "5") schedule("0 0/5 * * * ?", poll)
	if(timeToPing == "10") schedule("0 0/10 * * * ?", poll)
	if(timeToPing == "15") schedule("0 0/15 * * * ?", poll)
	if(timeToPing == "30") schedule("0 0/30 * * * ?", poll)
	if(timeToPing == "59") schedule("0 0/59 * * * ?", poll)
}

def pollVerify() {
	def reqParams = [
		uri: "http://${state.website}"
	]
    	try {
        	httpGet(reqParams) { resp ->
            		if (resp.status == 200) {
                		state.downHost = "false"
                		state.pollVerify = "false"
                		turnOffHandler()
                		LOGDEBUG("Successful response from ${state.website}, false alarm avoided")
            		} else {
            			state.downHost = "true"
                		state.pollVerify = "false"
            			turnOnHandler()
                		LOGDEBUG("Request failed to ${state.website}, turning on Switch(es)")
            		}
        	}
    	} catch (e) {
        	state.downHost = "true"
        	state.pollVerify = "false"
        	turnOnHandler()
        	LOGDEBUG("Request failed to ${state.website}, turning on Switch(es)")
    	}
}

def turnOnHandler() {
	if (switches) {
    	switches.on()
    	LOGDEBUG("Website NOT found, turning on switch(es)")
		if(sendPushMessage) pushNow()
   	}
}

def turnOffHandler() {
    if (switches) {
    	switches.off()
    	LOGDEBUG("Website found, turning off switch(es)")
    }
}

def pushNow(){
	LOGDEBUG("In pushNow...")
	if(state.downHost == "true") {
		sendPushMessage.deviceNotification(message)
	}	
}
	
// Normal Stuff

def pauseOrNot(){
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def checkForUpdate(){
	def params = [uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Web%20Pinger/version.json",
				   	contentType: "application/json"]
       	try {
			httpGet(params) { response ->
				def results = response.data
				def appStatus
				if(version() == results.currVersion){
					appStatus = "${version()} - No Update Available - ${results.discussion}"
				}
				else {
					appStatus = "<div style='color:#FF0000'>${version()} - Update Available (${results.currVersion})!</div><br>${results.parentRawCode}  ${results.childRawCode}  ${results.discussion}"
					log.warn "${app.label} has an update available - Please consider updating."
				}
				return appStatus
			}
		} 
        catch (e) {
        	log.error "Error:  $e"
    	}
}

def display2(){
	section() {
		def verUpdate = "${checkForUpdate()}"
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Web Pinger - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${verUpdate}</div>"
	}       
} 
