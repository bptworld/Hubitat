/**
 *  ****************  Notifier Child App  ****************
 *
 *  Design Usage:
 *  Notifications based on date/day and time. A perfect way to get reminders or create a wakeup alarm.
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
 *
 *  V1.0.0 - 02/22/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
    name: "Notifier Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Notifications based on date/day and time. A perfect way to get reminders or create a wakeup alarm.",
    category: "",
	parent: "BPTWorld:Notifier",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Notifier</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Notifications based on date/day and time. A perfect way to get reminders or create a wakeup alarm."
			paragraph "Get nofified when it's a holiday, birthday, special occasion, etc. Great for telling Hubitat when it's school vacation."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Set Notifier Type")) {
			if(xDate && xDay) {
				paragraph "Please only choose <b>one</b> option. <b>BAD THINGS WILL HAPPEN IF MULTIPLE OPTIONS ARE USED!</b>"
			} else {
				paragraph "Please only choose <b>one</b> option. If multiple options are selected bad things will happen."
			}
			input(name: "xDate", type: "bool", defaultValue: "false", title: "<b>by Date?</b><br>This will notify you on the Month/Day(s)/Year selected only.", description: "Date", submitOnChange: "true")
			if(xDate) {
				input "month", "enum", title: "Select Month", required: true, multiple: false, width: 4, submitOnChange: true, options: [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
				if(month == "Jan" || month == "Mar" || month == "May" || month == "Jun" || month == "Aug" || month == "Oct" || month == "Dec") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
				if(month == "Apr" || month == "Jun" || month == "Sep" || month == "Nov") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]
				if(month == "Feb") input "day", "enum", title: "Select Day(s)", required: true, multiple: true, width: 4, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"]
				input "year", "enum", title: "Select Year", required: true, multiple: false, width: 4, options: [ "2019", "2020", "2021", "2022"], defaultValue: "2019"
				input "hour", "enum", title: "Select Hour (24h format)", required: true, width: 6, options: [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
				input "min", "enum", title: "Select Minute", required: true, width: 6, options: [ "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"]
				paragraph "<hr>"
			}
			if(xDate && xDay) {
				paragraph "Please only choose <b>one</b> option. <b>BAD THINGS WILL HAPPEN IF MULTIPLE OPTIONS ARE USED!</b>"
				paragraph "<hr>"
			}
		}
		section() {
			input(name: "xDay", type: "bool", defaultValue: "false", title: "<b>by Day of the Week?</b><br>This will notify you on each day selected, week after week, at the time specified.", description: "Day of the Week", submitOnChange: "true")
			if(xDay) {
				input(name: "days", type: "enum", title: "Notify on these days", description: "Days to notify", required: true, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
				input(name: "startTime", type: "time", title: "Time to notify", description: "Time", required: true)
				paragraph "<hr>"
			}
			if(xDate && xDay) {
				paragraph "Please only choose <b>one</b> option. <b>BAD THINGS WILL HAPPEN IF MULTIPLE OPTIONS ARE USED!</b>"
				paragraph "<hr>"
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Choose Your Notify Options")) {
			paragraph "Select as many options as you like. Control switch required for Lighting and/or Message Options."
			input(name: "oControl", type: "bool", defaultValue: "false", title: "<b>Control Switch Options</b>", description: "Control Options", submitOnChange: "true", width: 6)
			input(name: "oLighting", type: "bool", defaultValue: "false", title: "<b>Lighting Options</b>", description: "Light Options", submitOnChange: "true", width: 6)
			input(name: "oDevice", type: "bool", defaultValue: "false", title: "<b>Device Options</b>", description: "Device Options", submitOnChange: "true", width: 6)
			input(name: "oMessage", type: "bool", defaultValue: "false", title: "<b>Message Options</b>", description: "Message Options", submitOnChange: "true", width: 6)
		}
		if(oControl) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Control Switch")) {
				paragraph "This is your child app on/off switch. <b>Required is using Lighting and/or Message Options.</b>"
				input(name: "controlSwitch", type: "capability.switch", title: "Turn the app on or off with this switch", required: true, multiple: false)
			}
		}
		if(oLighting) {
    		section(getFormat("header-green", "${getImage("Blank")}"+" Lighting Options")) {
				if(oLighting && !oControl) paragraph "<b>* Control Switch is required when using Lighting options.</b>"
				input(name: "oSetOn", type: "bool", defaultValue: "false", title: "<b>Turn Light On and Set To Level</b>", description: "Dim Up", submitOnChange: "true", width: 12)
				input(name: "oDimUp", type: "bool", defaultValue: "false", title: "<b>Slowly Dim Lighting UP</b>", description: "Dim Up", submitOnChange: "true", width: 6)
				input(name: "oDimDn", type: "bool", defaultValue: "false", title: "<b>Slowly Dim Lighting DOWN</b>", description: "Dim Down", submitOnChange: "true", width: 6)
				if(oSetOn) {
					input "setOn", "capability.switchLevel", title: "Select dimmer to turn on", required: true, multiple: true
					input "onLevel", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
				}
				if(oDimUp) {
					input "slowDimmerUp", "capability.switchLevel", title: "Select dimmer devices to slowly rise", required: true, multiple: true
    				input "minutesUp", "number", title: "Takes how many minutes to raise (1 to 60)", required: true, multiple: false, defaultValue:15, range: '1..60'
    				input "targetLevelHigh", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
					paragraph "Slowly raising a light level is a great way to wake up in the morning. If you want everything to delay happening until the light reaches its target level, turn this switch on."
					input(name: "oDelay", type: "bool", defaultValue: "false", title: "<b>Delay Until Finished</b>", description: "Future Options", submitOnChange: "true")
					paragraph "<hr>"
				}
				if(oDimDn) {
					input "slowDimmerDn", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required: true, multiple: true
    				input "minutesDn", "number", title: "Takes how many minutes to dim (1 to 60)", required: true, multiple: false, defaultValue:15, range: '1..60'
    				input "targetLevelHigh", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
					paragraph "<hr>"
				}
			}
		}
		if(oDevice) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
				paragraph "Great for turning on/off alarms, lighting, fans, coffee makers, etc..."
				input(name: "switchesOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "switchesOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "newMode", type: "mode", title: "Change Mode", required: false, multiple: false)
			}
		}
		if(oMessage) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
				if(oMessage && !oControl) paragraph "<b>* Control Switch is required when using Message options.</b>"
				input(name: "oRandom", type: "bool", defaultValue: "false", title: "Random Message?", description: "Random", submitOnChange: "true")
				if(!oRandom) input "message", "text", title: "Message to be spoken",  required: true
				if(oRandom) input "message", "text", title: "Message to be spoken - Separate each message with ; ",  required: true
				input(name: "oRepeat", type: "bool", defaultValue: "false", title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: "true")
				if(oRepeat) input "repeatSeconds", "number", title: "Repeat message every xx seconds until control switch is turned off (1 to 600)", required: true, multiple: false, defaultValue:10, range: '1..600'
				input(name: "oSpeech", type: "bool", defaultValue: "false", title: "<b>Speech Options</b>", description: "Speech Options", submitOnChange: "true", width: 6)
				input(name: "oPush", type: "bool", defaultValue: "false", title: "<b>Pushover Options</b>", description: "Pushover Options", submitOnChange: "true", width: 6)
			}
		}
		if(oSpeech) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
           		input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
				if (speechMode == "Music Player"){ 
              		input "speaker", "capability.musicPlayer", title: "Choose speaker(s)", required: true, multiple: true, submitOnChange: true
					input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' device?", description: "Echo speaks device")
					input "volume1", "number", title: "Speaker volume", description: "0-100%", required: true, defaultValue: "75"
          		}   
        		if (speechMode == "Speech Synth"){ 
         			input "speaker", "capability.speechSynthesis", title: "Choose speaker(s)", required: true, multiple: true
					input "gInitialize", "bool", title: "Initialize Google devices before sending speech", required: true, defaultValue: false
				}
          	}
      	}
		if(oPush) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Pushover Options")) {
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
		section() {
			paragraph "Global on/off switch. Use it to turn off multiple apps at one time. This switch is included in all BPTWorld Apps."
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable multiple apps with one switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	logCheck()
    setDefaults()
	
	if(enablerSwitch1) subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	subscribe(controlSwitch, "switch", controlSwitchHandler)
	scheduleHandler()
}

def scheduleHandler(){
	LOGDEBUG("In scheduleHandler...") 
	if(xDate) {
		state.monthName = month   
    	if(state.monthName == "Jan") {state.theMonth = "1"}
    	if(state.monthName == "Feb") {state.theMonth = "2"}
    	if(state.monthName == "Mar") {state.theMonth = "3"}
    	if(state.monthName == "Apr") {state.theMonth = "4"}
    	if(state.monthName == "May") {state.theMonth = "5"}
    	if(state.monthName == "Jun") {state.theMonth = "6"}
    	if(state.monthName == "Jul") {state.theMonth = "7"}
    	if(state.monthName == "Aug") {state.theMonth = "8"}
    	if(state.monthName == "Sep") {state.theMonth = "9"}
    	if(state.monthName == "Oct") {state.theMonth = "10"}
    	if(state.monthName == "Nov") {state.theMonth = "11"}
    	if(state.monthName == "Dec") {state.theMonth = "12"}
		LOGDEBUG("In scheduleHandler - day: ${day}")
		String jDays = day.join(",")
		state.theDays = jDays
		state.theHour = hour
		state.theMin = min
	
    	state.schedule = "0 ${state.theMin} ${state.theHour} ${state.theDays} ${state.theMonth} ? *"
		LOGDEBUG("In scheduleHandler - xTime - schedule: 0 ${state.theMin} ${state.theHour} ${state.theDays} ${state.theMonth} ? *")
    	schedule(state.schedule, magicHappensHandler)
	}
	
	if(xDay) {
		schedule(startTime, magicHappensHandler)
	}
	
}
def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	LOGDEBUG("In enablerSwitchHandler - Enabler Switch = ${enablerSwitch2}")
	LOGDEBUG("Enabler Switch = $state.enablerSwitch2")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("Enabler Switch is OFF - Child app is active.")
    }
}

def controlSwitchHandler(evt){
	state.controlSwitch2 = evt.value
	LOGDEBUG("In controlSwitchHandler - Control Switch = ${state.controlSwitch2}")
	LOGDEBUG("Enabler Switch = $state.enablerSwitch2")
    if(state.controlSwitch2 == "on"){
    	log.info "${app.label} - Control Switch is set to On."
	} else {
		log.info "${app.label} - Control Switch is set to Off."
    }
}

def magicHappensHandler() {
	LOGDEBUG("In magicHappensHandler...")
		if(oDelay) {
			state.realSeconds = minutesUp * 60
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) runIn(state.realSeconds,slowOffHandler)
			if(oSetOn && oControl) runIn(state.realSeconds,dimmerOnHandler)
			if(oMessage && oControl) runIn(state.realSeconds,messageHandler)
			if(oPush && oControl) runIn(state.realSeconds,pushHandler)
			if(oSpeech && oControl) runIn(state.realSeconds,letsTalk)
			if(oDevice) runIn(state.realSeconds,switchesOnHandler)
			if(oDevice) runIn(state.realSeconds,switchesOffHandler)
			if(newMode) runIn(state.realSeconds, modeHandler)
		} else {
			if(oDimUp && oControl) slowOnHandler()
			if(oDimDn && oControl) slowOffHandler()
			if(oSetOn && oControl) dimmerOnHandler()
			if(oMessage && oControl) messageHandler()
			if(oPush && oControl) pushHandler()
			if(oSpeech && oControl) letsTalk()
			if(oDevice) switchesOnHandler()
			if(oDevice) switchesOffHandler()
			if(newMode) modeHandler()
		}
}

def slowOnHandler(evt) {
	if(state.controlSwitch2 == "on") {
		dayOfTheWeekHandler()
		if(state.dotwMatch == "yes") {
			LOGDEBUG("In slowOnHandler...")
			if(state.enablerSwitch2 == "off") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In slowOnHandler...Pause: ${pause1}")
					if(slowDimmerUp[0].currentSwitch == "off") {
        				slowDimmerUp.setLevel(0)
        				state.currentLevel = 0
    				} else {
        				state.currentLevel = slowDimmerUp[0].currentLevel
    				}
    				if(minutesUp == 0) return
    				seconds = minutesUp * 6
    				state.dimStep = targetLevelHigh / seconds
    				state.dimLevel = state.currentLevel
    				LOGDEBUG("slowOnHandler - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
    				dimStepUp()
					LOGDEBUG("slowOnHandler - Will start talking in ${minutesUp} minutes (${state.realSeconds} seconds)")
				}
			} else {
				LOGDEBUG("Enabler Switch is ON - ${app.label} is disabled.")
			}
		} else {
			LOGDEBUG("${app.label} - Day of the Week didn't match - No need to run.")
		}
	} else {
		LOGDEBUG("${app.label} - Control Switch is OFF - No need to run.")
	}
}

def slowOffHandler(evt) {
	if(state.controlSwitch2 == "on") {
		dayOfTheWeekHandler()
		if(state.dotwMatch == "yes") {
			LOGDEBUG("In slowOffHandler...")
			if(state.enablerSwitch2 == "off") {
				if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    			if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
					LOGDEBUG("In slowOffandler...Pause: ${pause1}")
					if(slowDimmerDn[0].currentSwitch == "off") {
        				slowDimmerDn.setLevel(99)
        				state.currentLevel = 99
    				} else{
        				state.currentLevel = slowDimmerDn[0].currentLevel
    				}
    				if(minutesDn == 0) return
    				seconds = minutesDn * 6
    				state.dimStep1 = (targetLevelLow / seconds) * 100
    				state.dimLevel = state.currentLevel
   					LOGDEBUG("slowoffHandler - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
    				dimStepDown()					
				}				
									
			} else {
				LOGDEBUG("Enabler Switch is ON - ${app.label} is disabled.")
			}
		} else {
			LOGDEBUG("${app.label} - Day of the Week didn't match - No need to run.")
		}
	} else {
		LOGDEBUG("${app.label} - Control Switch is OFF - No need to run.")
	}
}

def dimStepUp() {
	LOGDEBUG("In dimStepUp...")
	if(state.controlSwitch2 == "on") {
    	if(state.currentLevel < targetLevelHigh) {
        	state.dimLevel = state.dimLevel + state.dimStep
        	if(state.dimLevel > targetLevelHigh) {state.dimLevel = targetLevelHigh}
       	 	state.currentLevel = state.dimLevel.toInteger()
    		slowDimmerUp.setLevel(state.currentLevel)
       	 	LOGDEBUG("dimStepUp - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
        	runIn(10,dimStepUp)				
		}
	} else {
		log.info "${app.label} - Control Switch is off"
	}
}

def dimStepDown() {
	LOGDEBUG("In dimStepDown...")			
    if(switches.currentValue("switch") == "on") {
    	if(state.currentLevel > targetLevelLow) {
            state.dimStep = state.dimStep1
        	state.dimLevel = state.dimLevel - state.dimStep
            if(state.dimLevel < targetLevelLow) {state.dimLevel = targetLevelLow}
        	state.currentLevel = state.dimLevel.toInteger()
    		slowDimmerDn.setLevel(state.currentLevel)
            LOGDEBUG("dimStepDown - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
        	runIn(10,dimStepDown)
    	} 
    } else{
        LOGDEBUG("${app.label} - Control Switch is Off")
    }						
}

def letsTalk() {							// Modified from @Cobra Code
	LOGDEBUG("In letsTalk...Speaker(s) in use: ${speaker}")
	if(state.controlSwitch2 == "on") {
  		if(speechMode == "Music Player"){ 
			if(echoSpeaks) {
				speaker.setLevel(volume1)
				speaker.setVolumeSpeakAndRestore(state.volume, state.msg)
			}
			if(!echoSpeaks) {
    			speaker.setLevel(volume1)
    			speaker.playTextAndRestore(state.msg)
			}
  		}   
		if(speechMode == "Speech Synth"){
			if(gInitialize) speaker.initialize()
			LOGDEBUG("Speech Synth - ${state.msg}")
			speaker.speak(state.msg)
		}
		if(oRepeat) {
			repeatSeconds2 = repeatSeconds + 1
			runIn(repeatSeconds,messageHandler)
			runIn(repeatSeconds2,letsTalk)
		}
	} else {
		log.info "${app.label} - Control Switch is off"
		LOGDEBUG("In letsTalk...Okay, I'm done!")
	}
}

def messageHandler() {
	if(state.controlSwitch2 == "on") {
		if(oRandom) {
			def values = "${message}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.msg = values[randomKey]
			LOGDEBUG("In messageHandler - vSize: ${vSize}, randomKey: ${randomKey}, msgRandom: ${state.msg}") 
		} else {
			state.msg = "${message}"
		}
	}
}

def switchesOnHandler() {
	switchesOn.each { it ->
		LOGDEBUG("In switchOnHandler - Turning on ${it}")
		it.on()
	}
}

def switchesOffHandler() {
	switchesOff.each { it ->
		LOGDEBUG("In switchOnHandler - Turning off ${it}")
		it.off()
	}
}

def dimmerOnHandler() {
	setOn.each { it ->
		LOGDEBUG("In switchOnHandler - Turning on ${it} @ ${onLevel}%")
		it.setLevel(onLevel,duration=1)
	}
}

def modeHandler() {
	LOGDEBUG("In modeHandler - Changing mode to ${newMode}")
	setLocationMode(newMode)
}

def dayOfTheWeekHandler() {
	LOGDEBUG("In dayOfTheWeek...")
	if(xDay) {
		state.dotwMatch = "no"
		Calendar date = Calendar.getInstance()
		int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK)
		if(dayOfTheWeek == 1) state.dotWeek = "Sunday"
		if(dayOfTheWeek == 2) state.dotWeek = "Monday"
		if(dayOfTheWeek == 3) state.dotWeek = "Tuesday"
		if(dayOfTheWeek == 4) state.dotWeek = "Wednesday"
		if(dayOfTheWeek == 5) state.dotWeek = "Thursday"
		if(dayOfTheWeek == 6) state.dotWeek = "Friday"
		if(dayOfTheWeek == 7) state.dotWeek = "Saturday"
		LOGDEBUG("In dayOfTheWeek...dayOfTheWeek: ${dayOfTheWeek} dotWeek: ${state.dotWeek}")
		LOGDEBUG("In dayOfTheWeek...days: ${days}")
		def values = "${days}".split(",")
		values.each { it ->
			it2 = it.replace("[","")
			it3 = it2.replace("]","")
			it4 = it3.replace(" ","")
			if(it4 == state.dotWeek) { state.dotwMatch = "yes" }
			LOGDEBUG("In dayOfTheWeekHandler - state.dotWeek: ${state.dotWeek} - values: ${it4}")
		}
		if(state.dotwMatch != "yes") state.dotwMatch = "no"
		LOGDEBUG("In dayOfTheWeekHandler - dotwMatch: ${state.dotwMatch}")
	} else {
		state.dotwMatch = "yes"
	}
}

def pushHandler(){
	count = 0
	if(count == 0) {
		LOGDEBUG("In pushNow...")
		theMessage = "${app.label} - ${state.msg}"
		LOGDEBUG("In pushNow...Sending message: ${theMessage}")
    	sendPushMessage.deviceNotification(theMessage)
		count = count + 1
	}
}

// ********** Normal Stuff **********

def pauseOrNot(){						// Modified from @Cobra Code
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

def setDefaults(){
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
}

def logCheck(){					// Modified from @Cobra Code
	state.checkLog = logEnable
	if(state.logEnable == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.logEnable == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){				// Modified from @Cobra Code
    try {
		if (settings.logEnable) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
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
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	section() {
		setVersion()
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Notifier - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
