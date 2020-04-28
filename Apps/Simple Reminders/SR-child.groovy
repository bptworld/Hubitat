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

def setVersion(){
    state.name = "Simple Reminders"
	state.version = "1.0.6"
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
            input "days", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
            if(days) {
                input "timeToRun", "time", title: "Time for Reminder", required: false, width:6
                input "everyOther", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                input "msg", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
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
            paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required:false, multiple:true, submitOnChange:true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required:false, multiple:true, submitOnChange:true
           paragraph "This app supports speaker proxies like, 'Follow Me'. This allows all speech to be controlled by one app. Follow Me features - Priority Messaging, volume controls, voices, sound files and more!"
           input "speakerProxy", "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange:true
        }
        if(!speakerProxy) {
            if(speakerMP || speakerSS) {
		        section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		            paragraph "NOTE: Not all speakers can use volume controls.", width:8
                    paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                    input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required:true, width:6
		            input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required:true, width:6
                    input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required:false, submitOnChange:true
		    	    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required:true, width:6
    	    	    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required:true, width:6
                }
		    }
		    section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
                input "fromTime", "time", title: "From", required:false, width: 6
        	    input "toTime", "time", title: "To", required:false, width: 6
		    }
        } else {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Proxy")) {
		        paragraph "Speaker proxy in use"
            }
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
	if(timeToRun) schedule(timeToRun, startTheProcess)
}

def startTheProcess(evt) {
    if(logEnable) log.debug "In startTheProcess (${state.version})"
    state.beenHere = 0
    if(everyOther) {
        state.everyOther = true
    } else {
        state.everyOther = false
    }
        
    dayOfTheWeekHandler(days)
    if(state.daysMatch && switchesOn) switchesOnHandler(switchesOn)
    if(state.daysMatch && switchesOff) switchesOffHandler(switchesOff)
    if(state.daysMatch && switchesFlash) flashLights(switchesFlash,numOfFlashes,delayFlashes)
    if(state.daysMatch && newMode) modeHandler(newMode)
    if(state.daysMatch && (speakerMP || speakerSS) && msg != null) messageHandler(msg)
}

def letsTalk(msg) {
	if(logEnable) log.debug "In letsTalk (${state.version})"
	checkTime()
	checkVol()
    checkEveryOther()
    if(logEnable) log.debug "In letsTalk - Checking daysMatch: ${state.daysMatch} - timeBetween: ${state.timeBetween} - thisTime: ${state.thisTime}"
    if(state.timeBetween && state.daysMatch && state.thisTime == "yes") {
        if(msg) state.theMsg = msg
              
        def duration = Math.max(Math.round(state.theMsg.length()/12),2)+3
        theDuration = duration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
    	if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${state.theMsg}, volRestore: ${volRestore}"
        state.speakers.each { it ->
            if(logEnable) log.debug "Speaker in use: ${it}"
            if(speakerProxy) {
                if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                it.speak(state.theMsg)
            } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                def prevVolume = it.currentValue("volume")
                it.setVolumeSpeakAndRestore(state.volume, state.theMsg, prevVolume)
            } else if(it.hasCommand('playTextAndRestore')) {   
                if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                def prevVolume = it.currentValue("volume")
                it.playTextAndRestore(state.theMsg, prevVolume)
            } else {		        
                if(logEnable) log.debug "In letsTalk - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                it.speak(state.theMsg)
                pauseExecution(theDuration)
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
            }
        }
        checkMaxRepeat()
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
}

def checkMaxRepeat() {
    if(logEnable) log.debug "In checkMaxRepeat (${state.version})"
    if(state.beenHere == null) state.beenHere = 0
    state.beenHere = state.beenHere + 1
    
    if(sendPushMessage && state.beenHere == 1) pushNow(state.theMsg)
    
    if(maxRepeats == 1) {
        if(logEnable) log.debug "In checkMaxRepeat - Max repeats (${maxRepeats}) has been reached"
    } else {
        if(state.beenHere <= maxRepeats) {
            repeatDelay = repeatSeconds * 1000
            pauseExecution(repeatDelay)
            def theControlSwitch = rControlSwitch.currentValue("switch")
            if(logEnable) log.debug "In checkMaxRepeat - ${rControlSwitch}: ${theControlSwitch} - **********"
         
            if(theControlSwitch == "on") {
                letsTalk(state.theMsg)
            } else {
                if(logEnable) log.debug "In checkMaxRepeat - Control Switch is OFF"
            }
        } else {
            rControlSwitch.off()
            if(logEnable) log.debug "In checkMaxRepeat - Max repeats (${maxRepeats}) has been reached"
        }
    }
    state.beenHere = 0
    if(logEnable) log.debug "In checkMaxRepeat - Finished speaking"
}

def checkVol(){
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - setting volume: ${state.volume}"
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

def checkEveryOther() {
    if(logEnable) log.debug "In checkEveryOther (${state.version})"
    if(state.everyOther) {
        if(state.thisTime == null || state.thisTime == "") state.thisTime = "no"
        if(state.thisTime == "yes") {
            state.thisTime = "no"
        } else {
            state.thisTime = "yes"
        }
    } else {
        state.thisTime = "yes"
    }
    if(logEnable) log.debug "In checkEveryOther - thisTime: ${state.thisTime}"
}

def messageHandler(msg) {
	if(logEnable) log.debug "In messageHandler (${state.version})"
	state.theMsg = ""
    
	def values = "${msg}".split(";")
	vSize = values.size()
    count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	msg = values[randomKey]
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, msg: ${msg}"
    
	if(logEnable) log.debug "In messageHandler - theMsg: ${theMsg}"
        
    letsTalk(msg)
}

def dayOfTheWeekHandler(days) {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
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
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
