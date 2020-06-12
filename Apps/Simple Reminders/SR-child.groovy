/**
 *  **************** Simple Reminders Child App ****************
 *
 *  Design Usage:
 *  Setup Simple Reminders through out the day.
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
 *  1.0.9 - 06/11/20 - All speech now goes through Follow Me
 *  1.0.8 - 06/07/20 - Bug fixes
 *  1.0.7 - 06/05/20 - Fixed max repeats, Added Specific Date trigger, Added Repeat every X days, Removed 'Every Other', other minor changes
 *  1.0.6 - 04/27/20 - Cosmetic changes
 *  1.0.5 - 02/03/20 - Only one reminder per child app, Repeat is still not honoring the control switch status
 *  1.0.4 - 01/31/20 - Second Attempt to fix flashing, fix Repeat
 *  1.0.3 - 01/29/20 - Attempt to fix flashing
 *  1.0.2 - 12/08/19 - Fixed push messages
 *  1.0.1 - 12/07/19 - Bug fixes
 *  1.0.0 - 10/15/19 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Simple Reminders"
	state.version = "1.0.9"
}

definition(
    name: "Simple Reminders Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Setup Simple Reminders through out the day.",
    category: "Convenience",
	parent: "BPTWorld:Simple Reminders",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Reminders/SR-child.groovy",
)

preferences {
    documentationLink: "https://community.hubitat.com/t/release-simple-reminders-setup-reminders-through-out-the-day/28816?title=Community_Thread"
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
            paragraph "Setup Simple Reminders through out the day."
		}
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Reminder Options")) {
            input "triggerType", "enum", title: "Tigger", required: true, multiple: false, submitOnChange: true, options: [
                ["xDate":"by Date"],
                ["xDayOfTheWeek":"by Day of the Week"],
                ["xEverySoManyDays":"Every So Many Days"]
            ]
            
            if(triggerType == "xDate") {
                app.removeSetting("days")
                app.removeSetting("timeToRun")
                
                app.removeSetting("howManyDays")
                app.removeSetting("timeForReminder")
                
                paragraph "Set Reminder for this Specific Date"
                input "month", "enum", title: "Select Month", required: true, multiple: false, width: 6, submitOnChange: true, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"]
                if(month == "1" || month == "3" || month == "5" || month == "7" || month == "8" || month == "10" || month == "12") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 6, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
                if(month == "4" || month == "6" || month == "9" || month == "11") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 6, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]
                if(month == "2") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 6, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"]
                paragraph " "
                input "hour", "enum", title: "Select Hour (24h format)", required: true, width: 6, options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
                input "min", "enum", title: "Select Minute", required: true, width: 6, options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"]
                paragraph " "
                paragraph "<small>* Note: This will repeat every year on this date</small>"
            }
            
            if(triggerType == "xDayOfTheWeek") {
                app.removeSetting("month")
                app.removeSetting("day")
                app.removeSetting("hour")
                app.removeSetting("min")
                
                app.removeSetting("howManyDays")
                app.removeSetting("timeForReminder")
                
                input "days", "enum", title: "Set Reminder on the Selected Days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                input "timeToRun", "time", title: "Time for Reminder", required: false, width:6
            }
            
            if(triggerType == "xEverySoManyDays") {
                app.removeSetting("days")
                app.removeSetting("timeToRun")
                
                app.removeSetting("month")
                app.removeSetting("day")
                app.removeSetting("hour")
                app.removeSetting("min")
                
                paragraph "On the day you want this to start, Come back here and hit 'Done'. It will then remind you x days out. Great for Furnace filter (every 60 days), Recycle Week (every 14 days) type reminders!"
                input "howManyDays", "number", title: "How Many Days", required: false, submitOnChange: true
                input "hmdHour", "enum", title: "Select Hour (24h format)", required: true, width: 6, options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
                input "hmdMin", "enum", title: "Select Minute", required: true, width: 6, options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"]
            }
            
            if(triggerType == "xDate" || triggerType == "xDayOfTheWeek" || triggerType == "xEverySoManyDays") {
                input "msg", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: true
				input "list1", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				if(list1) {
					def values1 = "${msg}".split(";")
					listMap1 = ""
    				values1.each { item1 -> listMap1 += "${item1}<br>" }
					paragraph "${listMap1}"
				}
                input "maxRepeats", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:6, range: '1..100', submitOnChange: true
                
				if(maxRepeats > 1) {
                    input "rControlSwitch", "capability.switch", title: "Control Switch to turn this reminder on and off", required:true, multiple:false, submitOnChange:true
					paragraph "<small><b>* Control Switch is required when Max Repeats is greater than 1.</small></b>"
					input "repeatSeconds", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					
					if(repeatSeconds) {
						paragraph "Message will repeat every ${repeatSeconds} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats})"
						repeatTimeSeconds = (repeatSeconds * maxRepeats)
						int inputNow=repeatTimeSeconds
						int nDayNow = inputNow / 86400
						int nHrsNow = (inputNow % 86400 ) / 3600
						int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
					}
				}
                paragraph "<hr>"
                input "switchesOn", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			    input "switchesOff", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                input "switchesFlash", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                if(switchesFlash) {                 
		            input "numOfFlashes", "number", title: "Number of times (default: 2)", required: false, width: 6
                    input "delayFlashes", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
                }
                input "newMode", "mode", title: "Change to Mode", required: false, multiple: false
    	    }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
    	}
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required: false
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
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def initialize() {
    setVersion()
    setDefaults()
	if(triggerType == "xDayOfTheWeek") schedule(timeToRun, startTheProcess)
    if(triggerType == "xDate") dateHandler()
    if(triggerType == "xEverySoManyDays") everySoManyDaysHandler()
}

def startTheProcess(evt) {
    if(logEnable) log.debug "In startTheProcess (${state.version})"
        
    dayOfTheWeekHandler()
    if(state.daysMatch && switchesOn) switchesOnHandler(switchesOn)
    if(state.daysMatch && switchesOff) switchesOffHandler(switchesOff)
    if(state.daysMatch && switchesFlash) flashLights(switchesFlash,numOfFlashes,delayFlashes)
    if(state.daysMatch && newMode) modeHandler(newMode)
    if(state.daysMatch && (speakerMP || speakerSS) && msg != null) letsTalk()
    
    // reset for next time
    initialize()
}

def dateHandler() {
	if(logEnable) log.debug "In dateHandler (${state.version})"
	theMonth = month
	String jDays = day.join(",")
	theDays = jDays
	
    state.schedule = "0 ${min} ${hour} ${theDays} ${theMonth} ? *"
	if(logEnable) log.debug "In dateHandler - xTime - schedule: 0 ${min} ${hour} ${theDays} ${theMonth} ? *"
    schedule(state.schedule, startTheProcess)
}

def everySoManyDaysHandler() {
	if(logEnable) log.debug "In everySoManyDaysHandler (${state.version})"
    int hmd = howManyDays
	Date futureDate = new Date().plus(hmd)
    if(logEnable) log.debug "In everySoManyDaysHandler - howManyDays: ${howManyDays} - futureDate: ${futureDate}"
    
    hmdMonth = futureDate.format("MM")
    hmdDay = futureDate.format("dd")

    hmdSchedule = "0 ${hmdMin} ${hmdHour} ${hmdDay} ${hmdMonth} ? *"
	if(logEnable) log.debug "In everySoManyDaysHandler - schedule: 0 ${hmdMin} ${hmdHour} ${hmdDay} ${hmdMonth} ? *"
    schedule(hmdSchedule, startTheProcess)
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) fmSpeaker.speak(theMsg)
    state.repeat = state.repeat + 1
    checkMaxRepeat()
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def checkMaxRepeat() {
    if(logEnable) log.debug "In checkMaxRepeat (${state.version})"
    if(state.repeat == null) state.repeat = 1   
    if(sendPushMessage && state.repeat == 1) pushNow(state.theMsg)
    
    if(maxRepeats == 1) {
        if(logEnable) log.debug "In checkMaxRepeat - Max repeats (${maxRepeats}) has been reached (${state.repeat})"
    } else {
        if(state.repeat < maxRepeats) {
            repeatDelay = repeatSeconds * 1000
            if(state.repeat > 1) pauseExecution(repeatDelay)
            def theControlSwitch = rControlSwitch.currentValue("switch")
            if(logEnable) log.debug "In checkMaxRepeat - Control switch: ${rControlSwitch} is ${theControlSwitch} - Max repeats: ${maxRepeats} - repeated: ${state.repeat}"
         
            if(theControlSwitch == "on") {
                letsTalk(state.theMsg)
            } else {
                if(logEnable) log.debug "In checkMaxRepeat - Control Switch is OFF"
            }
        } else {
            rControlSwitch.off()
            if(logEnable) log.debug "In checkMaxRepeat - Max repeats (${maxRepeats}) has been reached (${state.repeat})"
        }
    }
    state.repeat = 0
    if(logEnable) log.debug "In checkMaxRepeat - Finished - Max repeats: ${maxRepeats} - repeated: ${state.repeat}"
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
	state.theMsg = ""
    
	def values = "${msg}".split(";")
	vSize = values.size()
    count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMsg = values[randomKey]
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMsg: ${theMsg}"
    
	if(logEnable) log.debug "In messageHandler - theMsg: ${theMsg}"
        
    letsTalk(theMsg)
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

def pushNow(theMsg){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

private flashLights(switchesToFlash,numOfFlashes,delayFlashes) {    // Modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
	def doFlash = true
	def delay = delayFlashes ?: 500
	def numFlashes = numOfFlashes ?: 2

	if(logEnable) log.debug "In flashLights - LAST ACTIVATED: ${state.lastActivated}"
	if(state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (delay)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash - ELAPSED: $elapsed - LAST ACTIVATED: ${state.lastActivated}"
	}

	if(doFlash) {
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		state.lastActivated = now()
		if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switchesToFlash.collect{it.currentSwitch != "on"}

		numFlashes.times {
			if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.on()
				}
				else {
                    pauseExecution(delay)
					s.off()
				}
			}
			if(logEnable) log.debug "In flashLights - Switch off after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.off()
				}
				else {
                    pauseExecution(delay)
					s.on()
				}
			}
		}
	}
}

def switchesOnHandler(switchesOn) {
	switchesOn.each { it ->
		if(logEnable) log.debug "In switchOnHandler - Turning on ${it}"
		it.on()
	}
}

def switchesOffHandler(switchesOff) {
	switchesOff.each { it ->
		if(logEnable) log.debug "In switchOffHandler - Turning off ${it}"
		it.off()
	}
}

def modeHandler(newMode) {
	if(logEnable) log.debug "In modeHandler - Changing mode to ${newMode}"
	setLocationMode(newMode)
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
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
