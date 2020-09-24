/**
 *  **************** Remote Wellness Check Child ****************
 *
 *  Design Usage:
 *  Stay connected to your loved ones. Get notified if they haven't triggered a device in a specified time.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  1.0.5 - 09/24/20 - Adjustments
 *  1.0.4 - 06/19/20 - Added The Flasher
 *  1.0.3 - 04/27/20 - Cosmetic changes
 *  1.0.2 - 01/17/20 - Minor changes
 *  1.0.1 - 01/16/20 - Added more 'Check' options
 *  1.0.0 - 01/15/20 - Initial release
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Remote Wellness Check"
	state.version = "1.0.5"
}

definition(
	name:"Remote Wellness Check Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Stay connected to your loved ones. Get notified if they haven't triggered a device in a specified time.",
    category: "Convenience",
    parent: "BPTWorld:Remote Wellness Check",
	iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Remote%20Wellness%20Check/RWC-child.groovg"
)

preferences {
	page(name: "pageConfig", title:"", content: "disclaimerPage", install: true, uninstall: true)
}

def disclaimerPage() {
    // Modified from ST
	def disclaimerText = "BPTWORLD APPS/DRIVERS REMOTE WELLNESS CHECK SUPPLEMENTAL TERMS AND DISCLAIMER\n\n" +
			"BPTWorld Apps/Drivers is not an emergency medical response service of any kind and does not provide " +
			"medical or health-related advice, which should be obtained from qualified medical personnel. " +
			"BPTWorld Apps/Drivers, the contents of the app (such as text, graphics, images, videos, data and "+
			"information contained therein) and such materials obtained from third parties are provided for " +
			"information purposes only and are not substitutes for professional medical advice, diagnosis, " +
			"examination, or treatment by a health care provider. If you think you or a loved one has a medical " +
			"emergency, call your doctor or 911 immediately. Do not rely on electronic communications or " +
			"communication through this app for immediate, urgent medical needs.\n\n" +
			"THIS APP IS NOT DESIGNED TO FACILITATE OR AID IN MEDICAL EMERGENCIES.\n\n"+ 
			"RELIANCE ON ANY INFORMATION PROVIDED BY THE APP OR OTHER THIRD-PARTY PLATFORMS IS SOLELY AT YOUR OWN RISK.\n\n" + 
			"While BPTWorld Apps/Drivers strives to make the information on the app as timely and accurate as possible, " + 
			"BPTWorld Apps/Drivers makes no claims, promises, or guarantees about the accuracy, completeness, " + 
			"or adequacy of the content or information on the app. BPTWorld Apps/Drivers expressly disclaims liability for any errors "+
			"and omissions in content or for the availability of content on the app. " +
			"BPTWorld Apps/Drivers will not be liable for any losses, injuries, or damages arising from the display " +
			"or use of content on the app.\n\n" +
            "BPTWORLD APPS/DRIVERS, ITS OFFICERS, EMPLOYEES AND AGENTS DO NOT ACCEPT LIABILITY HOWEVER ARISING, INCLUDING LIABILITY FOR NEGLIGENCE, " +
			"FOR ANY LOSS RESULTING FROM THE USE OF OR RELIANCE UPON THE INFORMATION AND/OR SERVICES AT ANY TIME."

	if(disclaimerResponse && disclaimerResponse == "I agree to these terms") {
		pageConfig()
	} else {
		dynamicPage(name: "pageConfig") {
			section(disclaimerText){
				input "disclaimerResponse", "enum", title: "Accept terms", required:true, options: ["I agree to these terms", "I do not agree to these terms"], submitOnChange:true
			}
		}
	}
}

def pageConfig(){
	dynamicPage(name: "", title: "", install: true, uninstall: true) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Name Options")) {
			input "person", "text", title: "Name of Person/People to check on"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            paragraph "Devices to show wellness"
			input "contact", "capability.contactSensor", title: "Contact Sensors", multiple:true, required:false
            input "motion", "capability.motionSensor", title: "Motion Sensors", multiple:true, required:false
            input "switches", "capability.switch", title: "Switches", multiple:true, required:false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Check Options")) {
            paragraph "<b>Run 'Check' anytime this switch is turned on.</b><br>Recommended to create a 'virtual switch' with 'Enable auto off' set to '1s'"
            input "onDemandSwitch", "capability.switch", title: "Check On Demand Switch", required:false
            paragraph "<hr>"
            paragraph "<b>Run 'Check' on a schedule.</b>"
            input "runEvery", "enum", title: "Run 'Check' every", options: ["1 Min","5 Min","10 Min","15 Min","30 Min","1 Hour","3 Hours"], required:false, multiple:false, submitOnChange:true
            input "days", "enum", title: "Only run 'Check' on these days", description: "Days to run", required:false, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            
            paragraph "Only 'Check' between what times"
            input "fromTime", "time", title: "From", required:false, width: 6
        	input "toTime", "time", title: "To", required:false, width: 6
            
            input "modeName", "mode", title: "Only when in this Mode", required: false, multiple: true
            
            paragraph "<hr>"
            paragraph "<b>Go back 'How Many Minutes' to see if there were any events</b>"
            input "minsToCheck", "number", title: "Check the Past x Minutes (60 = 1 hour, 180 = 3 hours, etc)"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
		    input "isDataDevice", "capability.switch", title: "Turn this device on", submitOnChange:true, required:false, multiple:false
            input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple:true, required:false, submitOnChange:true
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashPreset", "number", title: "Select the Preset to use (1..5)", required:true, submitOnChange:true
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
            label title: "Enter a name for this child app", required: false, submitOnChange: true
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
        }
        display2()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
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
        if(onDemandSwitch) subscribe(onDemandSwitch, "switch.on", scheduleCheck)

        if(runEvery == "1 Min") runEvery1Minute(scheduleCheck)
        if(runEvery == "5 Min") runEvery5Minutes(scheduleCheck)
        if(runEvery == "10 Min") runEvery10Minutes(scheduleCheck)
        if(runEvery == "15 Min") runEvery15Minutes(scheduleCheck)
        if(runEvery == "30 Min") runEvery30Minutes(scheduleCheck)
        if(runEvery == "1 Hour") runEvery1Hour(scheduleCheck)
        if(runEvery == "3 Hours") runEvery3Hours(scheduleCheck)
    }
}

def scheduleCheck(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "********************  Start - Remote Wellness Check - ${state.version}  ********************"
        recentContact()
        recentMotion()
        recentSwitch()
        if(logEnable) log.debug "In scheduleCheck - recentContact: ${state.recentContact} - recentMotion: ${state.recentMotion} - recentSwitch: ${state.recentSwitch}"
        if(state.recentContact && state.recentMotion && state.recentSwitch) {
            checkTime()
            dayOfTheWeekHandler()
            modeHandler()
            if(state.daysMatch) {
                if(state.timeBetween) {
                    if(state.matchFound) {
                        def person = person ?: "who ever"
                        def msg = "Alert! There has been NO activity at ${person}‘s place in the last ${minsToCheck} minutes!"
                        if(logEnable) log.debug msg
                        if(isDataDevice) { isDataDevice.on() }
                        if(sendPushMessage) pushHandler(msg)
                        if(useTheFlasher && flashPreset) {
                            flashData = "Preset::${flashPreset}"
                            theFlasherDevice.sendPreset(flashData)
                        }
                    } else {
                        if(logEnable) log.debug "Mode did not match, not checking for alert"
                    }
                } else {
                    if(logEnable) log.debug "Outside of scheduled times, not checking for alert"
                }
            } else {
                if(logEnable) log.debug "Day of the Week did not match, not checking for alert"
            }
        } else {
            if(isDataDevice) { isDataDevice.off() }
            if(logEnable) log.debug "There has been recent activity, no need to send alert"
        }
        if(logEnable) log.debug "********************  End - Remote Wellness Check - ${state.version}  ********************"
    }
}

private recentContact() {
    if(logEnable) log.debug "In recentContact (${state.version})"
    numContactEvents = 0
    int minCheck = minsToCheck ?: 240
    use( groovy.time.TimeCategory ) {
        lastActivity = minCheck.minutes.ago
        if(logEnable) log.debug "In recentContact - Go back and check from: ${lastActivity}"
    } 
	if(contact) {
        contact.each { it ->           
		    def contactEvents = it.eventsSince(lastActivity)
            numContactEvents = numContactEvents + contactEvents.size()
        }
        if(logEnable) log.debug "In recentContact - There have been <b>${numContactEvents}</b> contact events in the last ${minCheck} minutes"
        if(numContactEvents > 0) {            
			state.recentContact = false
        } else{
			state.recentContact = true
        }
	} else {
		if(logEnable) log.debug "In recentContact - No Contact sensors enabled"
		state.recentContact = true
	}
}

private recentMotion() {
    if(logEnable) log.debug "In recentMotion (${state.version})"
	numMotionEvents = 0
    int minCheck = minsToCheck ?: 240
    use( groovy.time.TimeCategory ) {
        lastActivity = minCheck.minutes.ago
        if(logEnable) log.debug "In recentMotion - Go back and check from: ${lastActivity}"
    } 
	if(motion) {
        motion.each { it ->           
		    def motionEvents = it.eventsSince(lastActivity)
            numMotionEvents = numMotionEvents + motionEvents.size()
        }
        if(logEnable) log.debug "In recentMotion - There have been <b>${numMotionEvents}</b> motion events in the last ${minCheck} minutes"
        if(numMotionEvents > 0) {   
			state.recentMotion = false
        } else{
			state.recentMotion = true
        }
	} else {
		if(logEnable) log.debug "In recentMotion - No Motion sensors enabled"
		state.recentMotion = true
	}
}

private recentSwitch() {
    if(logEnable) log.debug "In recentSwitch (${state.version})"
	numSwitchEvents = 0
    int minCheck = minsToCheck ?: 240
    use( groovy.time.TimeCategory ) {
        lastActivity = minCheck.minutes.ago
        if(logEnable) log.debug "In recentSwitch - Go back and check from: ${lastActivity}"
    } 
	if(switches) {
        switches.each { it ->           
		    def switchEvents = it.eventsSince(lastActivity)
            numSwitchEvents = numSwitchEvents + switchEvents.size()
        }
        if(logEnable) log.debug "In recentSwitch - There have been <b>${numSwitchEvents}</b> switch events in the last ${minCheck} minutes"
        if(numSwitchEvents > 0) {            
			state.recentSwitch = false
        } else{
			state.recentSwitch = true
        }
	} else {
		if(logEnable) log.debug "In recentSwitch - No Switches enabled"
		state.recentSwitch = true
	}
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def pushHandler(msg){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${msg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	msg = ""
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
    if(days) {
	    Calendar date = Calendar.getInstance()
	    int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK)
	    if(dayOfTheWeek == 1) state.dotWeek = "Sunday"
	    if(dayOfTheWeek == 2) state.dotWeek = "Monday"
	    if(dayOfTheWeek == 3) state.dotWeek = "Tuesday"
	    if(dayOfTheWeek == 4) state.dotWeek = "Wednesday"
	    if(dayOfTheWeek == 5) state.dotWeek = "Thursday"
	    if(dayOfTheWeek == 6) state.dotWeek = "Friday"
	    if(dayOfTheWeek == 7) state.dotWeek = "Saturday"

	    if(days.contains(state.dotWeek)) {
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

def modeHandler() {
    if(logEnable) log.debug "In modeHandler (${state.version}) - Mode: ${location.mode}"
	state.modeNow = location.mode
    state.matchFound = false
    
    if(modeName) {
        modeName.each { it ->
            if(logEnable) log.debug "In modeHandler - Checking if ${state.modeNow} contains ${it}"
		    if(state.modeNow.contains(it)){
                state.matchFound = true
			    if(logEnable) log.debug "In modeHandler - Match Found - modeName1: ${modeName1} - modeNow: ${state.modeNow}"
		    }
        }
    } else {
        state.matchFound = true
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
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
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
