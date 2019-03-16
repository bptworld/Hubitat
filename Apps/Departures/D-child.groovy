import groovy.time.TimeCategory
/**
 *  ****************  Departures Child App  ****************
 *
 *  Design Usage:
 *  Let the rest of the house know when one or more people have left the area.
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
 *  V1.0.0 - 03/15/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
    name: "Departures Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Let the rest of the house know when one or more people have left the area.",
    category: "",
	parent: "BPTWorld:Departures",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Departures</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "When a sensor becomes 'not present', it will wait X minutes and then check to see who else just left before making the announcement."
			paragraph "<b>Requirements:</b>"
			paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options")) {
			paragraph "Note: If you are not seeing your 'Friendly Names', then go back to the parent app, enter them in and hit 'done' before setting up any child apps."
			input(name: "presenceSensor1a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName1}", description: "PS1", submitOnChange: true)
			if(presenceSensor1a) input("presenceSensor1", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor2a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName2}", description: "PS2", submitOnChange: true)
			if(presenceSensor2a) input("presenceSensor2", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor3a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName3}", description: "PS3", submitOnChange: true)
			if(presenceSensor3a) input("presenceSensor3", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor4a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName4}", description: "PS4", submitOnChange: true)
			if(presenceSensor4a) input("presenceSensor4", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
			input(name: "presenceSensor5a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName5}", description: "PS5", submitOnChange: true)
			if(presenceSensor5a) input("presenceSensor5", "capability.presenceSensor", title: "Match a Presence Sensor to the Friendly Name", required: true, multiple: false)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
           input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
			if (speechMode == "Music Player"){ 
              	input "speakers", "capability.musicPlayer", title: "Choose speaker(s)", required: true, multiple: true, submitOnChange: true
				input(name: "echoSpeaks", type: "bool", defaultValue: "false", title: "Is this an 'echo speaks' device?", description: "Echo speaks device?")
				input "volume1", "number", title: "Speaker volume", description: "0-100%", required: true, defaultValue: "75"
              	input "volume2", "number", title: "Quiet Time Speaker volume", description: "0-100%",  required: true, defaultValue: "30"		
				input "fromTime2", "time", title: "Quiet Time Start", required: true
    		  	input "toTime2", "time", title: "Quiet Time End", required: true
          	}   
        	if (speechMode == "Speech Synth"){ 
         		input "speakers", "capability.speechSynthesis", title: "Choose speaker(s)", required: true, multiple: true
          	}
      	}
    	if(speechMode){ 
			section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
        		input "fromTime", "time", title: "From", required: false
        		input "toTime", "time", title: "To", required: false
			}
    	}
		section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			input(name: "oRandomG1", type: "bool", defaultValue: "false", title: "Random Pre-announcement 1?", description: "Random", submitOnChange: "true")
			if(!oRandomG1) input "greeting1", "text", required: true, title: "Pre-announcement - 1 (am) - Single message", defaultValue: "Good Morning"
			if(oRandomG1) {
				input "greeting1", "text", title: "Random Pre-announcement - 1 (am) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of random messages 1?", description: "List View", submitOnChange: "true")
				if(oG1List) {
					def valuesG1 = "${greeting1}".split(";")
					listMapG1 = ""
    				valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
					paragraph "${listMapG1}"
				}
			}
			paragraph "<hr>"
			input(name: "oRandomG2", type: "bool", defaultValue: "false", title: "Random Pre-announcement 2?", description: "Random", submitOnChange: "true")
			if(!oRandomG2) input "greeting2", "text", required: true, title: "Pre-announcement - 2 (pm before 6) - Single message", defaultValue: "Good Afternoon"
			if(oRandomG2) {
				input "greeting2", "text", title: "Random Pre-announcement - 2 (pm before 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG2List", type: "bool", defaultValue: "false", title: "Show a list view of the random messages 2?", description: "List View", submitOnChange: "true")
				if(oG2List) {
					def valuesG2 = "${greeting2}".split(";")
					listMapG2 = ""
    				valuesG2.each { itemG2 -> listMapG2 += "${itemG2}<br>" }
					paragraph "${listMapG2}"
				}
			}
			paragraph "<hr>"
			input(name: "oRandomG3", type: "bool", defaultValue: "false", title: "Random Pre-announcement 3?", description: "Random", submitOnChange: "true")
			if(!oRandomG3) input "greeting3", "text", required: true, title: "Pre-announcement - 3 (pm after 6) - Single message", defaultValue: "Good Evening"
			if(oRandomG3) {
				input "greeting3", "text", title: "Random Pre-announcement - 3 (pm after 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG3List", type: "bool", defaultValue: "false", title: "Show a list view of the random messages 3?", description: "List View", submitOnChange: "true")
				if(oG3List) {
					def valuesG3 = "${greeting3}".split(";")
					listMapG3 = ""
    				valuesG3.each { itemG3 -> listMapG3 += "${itemG3}<br>" }
					paragraph "${listMapG3}"
				}
			}
			paragraph "<hr>"
			input(name: "oRandom", type: "bool", defaultValue: "false", title: "Random Message?", description: "Random", submitOnChange: "true")
			paragraph "<u>Optional wildcards:</u><br>%greeting% - returns a pre-announcement based on time of day.<br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
			if(!oRandom) input "message", "text", title: "Message to be spoken - Single message",  required: true
			if(oRandom) {
				input "message", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				input(name: "oMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
				if(oMsgList) {
					def values = "${message}".split(";")
					listMap = ""
    				values.each { item -> listMap += "${item}<br>"}
					paragraph "${listMap}"
				}
			}
		}
		section() {
			paragraph "This next option is so the app can pickup if any other sensors become 'not present' around the same time."
			input "delay1", "number", title: "How many minutes from the time a trigger being activated to the announcement being made (default=2)", required: true, defaultValue: 2
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
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
	
	subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	subscribe(presenceSensor1, "presence", checkAllHandler)
	subscribe(presenceSensor2, "presence", checkAllHandler)
	subscribe(presenceSensor3, "presence", checkAllHandler)
	subscribe(presenceSensor4, "presence", checkAllHandler)
	subscribe(presenceSensor5, "presence", checkAllHandler)
}

def checkAllHandler(evt) {
	LOGDEBUG("In checkAllHandler...")
	state.presenceMap = ""
	state.nameCount = 0
	if(presenceSensor1) presenceSensorHandler1()
	if(presenceSensor2) presenceSensorHandler2()
	if(presenceSensor3) presenceSensorHandler3()
	if(presenceSensor4) presenceSensorHandler4()
	if(presenceSensor5) presenceSensorHandler5()
	LOGDEBUG("In checkAllHandler - Waiting to talk - ${state.canSpeak}")
	delay1m = delay1 * 60
	if(state.canSpeak == "yes" && state.nameCount >= 1) {
		runIn(delay1m,letsTalk)
	} else {
		log.info "${app.label} - No need for an announcement."
	}
}

def enablerSwitchHandler(evt) {
	state.enablerSwitch2 = evt.value
	LOGDEBUG("IN enablerSwitchHandler - Enabler Switch = ${enablerSwitch2}")
	LOGDEBUG("Enabler Switch = $state.enablerSwitch2")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("Enabler Switch is OFF - Child app is active.")
    }
}

def setupNewStuff() {
	LOGDEBUG("In setupNewStuff...Setting up Maps")
	if(state.presenceMap == null) state.presenceMap = [:]
}
		
def presenceSensorHandler1() {
	state.presenceSensorValue1 = presenceSensor1.currentValue("presence")
	LOGDEBUG("In presenceSensorHandler1 - Presence Sensor: ${state.presenceSensorValue1}")
   	if(state.presenceSensorValue1 == "not present"){
    	LOGDEBUG("In presenceSensorHandler1 - Presence Sensor is not present")
		def lastActivity1 = presenceSensor1.getLastActivity()
		LOGDEBUG("In presenceSensorHandler1 - lastActivity: ${lastActivity1}")
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity1}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		LOGDEBUG("In presenceSensorHandler1 - PS1 has been gone for ${timeDiff} minutes")	
		if(timeDiff < delay1) {
			log.info "${app.label} - ${parent.friendlyName1} just left! Time Diff: ${timeDiff}"
			state.nameCount = state.nameCount + 1
			if(state.nameCount == 1) state.presenceMap = [parent.friendlyName1]
			if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName1]
			LOGDEBUG("In presenceSensorHandler1 - ${state.presenceMap}")
		} else {
			log.info "${app.label} - ${parent.friendlyName1} has been gone too long. No announcement needed."
		}
    } else {
		LOGDEBUG("In presenceSensorHandler1 - Presence Sensor is present. No announcement needed.")
		state.canSpeak = "yes"
    }
}

def presenceSensorHandler2() {
	state.presenceSensorValue2 = presenceSensor2.currentValue("presence")
	LOGDEBUG("In presenceSensorHandler2 - Presence Sensor: ${state.presenceSensorValue2}")
   	if(state.presenceSensorValue2 == "not present"){
    	LOGDEBUG("In presenceSensorHandler2 - Presence Sensor is not present")
		def lastActivity2 = presenceSensor2.getLastActivity()
		LOGDEBUG("In presenceSensorHandler2 - lastActivity: ${lastActivity2}")
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity2}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		LOGDEBUG("In presenceSensorHandler2 - PS1 has been gone for ${timeDiff} minutes")	
		if(timeDiff < delay1) {
			log.info "${app.label} - ${parent.friendlyName2} just left! Time Diff: ${timeDiff}"
			state.nameCount = state.nameCount + 1
			if(state.nameCount == 1) state.presenceMap = [parent.friendlyName2]
			if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName2]
			LOGDEBUG("In presenceSensorHandler2 - ${state.presenceMap}")
		} else {
			log.info "${app.label} - ${parent.friendlyName2} has been gone too long. No announcement needed."
		}
    } else {
		LOGDEBUG("In presenceSensorHandler2 - Presence Sensor is present. No announcement needed.")
		state.canSpeak = "yes"
    }
}

def presenceSensorHandler3() {
	state.presenceSensorValue3 = presenceSensor3.currentValue("presence")
	LOGDEBUG("In presenceSensorHandler3 - Presence Sensor: ${state.presenceSensorValue3}")
   	if(state.presenceSensorValue3 == "not present"){
    	LOGDEBUG("In presenceSensorHandler3 - Presence Sensor is not present")
		def lastActivity3 = presenceSensor3.getLastActivity()
		LOGDEBUG("In presenceSensorHandler3 - lastActivity: ${lastActivity3}")
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity3}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		LOGDEBUG("In presenceSensorHandler3 - PS3 has been gone for ${timeDiff} minutes")	
		if(timeDiff < delay1) {
			log.info "${app.label} - ${parent.friendlyName3} just left! Time Diff: ${timeDiff}"
			state.nameCount = state.nameCount + 1
			if(state.nameCount == 1) state.presenceMap = [parent.friendlyName3]
			if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName3]
			LOGDEBUG("In presenceSensorHandler3 - ${state.presenceMap}")
		} else {
			log.info "${app.label} - ${parent.friendlyName3} has been gone too long. No announcement needed."
		}
    } else {
		LOGDEBUG("In presenceSensorHandler3 - Presence Sensor is present. No announcement needed.")
		state.canSpeak = "yes"
    }
}

def presenceSensorHandler4() {
	state.presenceSensorValue4 = presenceSensor4.currentValue("presence")
	LOGDEBUG("In presenceSensorHandler4 - Presence Sensor: ${state.presenceSensorValue4}")
   	if(state.presenceSensorValue4 == "not present"){
    	LOGDEBUG("In presenceSensorHandler4 - Presence Sensor is not present")
		def lastActivity4 = presenceSensor4.getLastActivity()
		LOGDEBUG("In presenceSensorHandler4 - lastActivity: ${lastActivity4}")
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity4}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		LOGDEBUG("In presenceSensorHandler4 - PS4 has been gone for ${timeDiff} minutes")	
		if(timeDiff < delay1) {
			log.info "${app.label} - ${parent.friendlyName4} just left! Time Diff: ${timeDiff}"
			state.nameCount = state.nameCount + 1
			if(state.nameCount == 1) state.presenceMap = [parent.friendlyName4]
			if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName4]
			LOGDEBUG("In presenceSensorHandler4 - ${state.presenceMap}")
		} else {
			log.info "${app.label} - ${parent.friendlyName4} has been gone too long. No announcement needed."
		}
    } else {
		LOGDEBUG("In presenceSensorHandler4 - Presence Sensor is present. No announcement needed.")
		state.canSpeak = "yes"
    }
}

def presenceSensorHandler5() {
	state.presenceSensorValue5 = presenceSensor5.currentValue("presence")
	LOGDEBUG("In presenceSensorHandler5 - Presence Sensor: ${state.presenceSensorValue5}")
   	if(state.presenceSensorValue5 == "not present"){
    	LOGDEBUG("In presenceSensorHandler5 - Presence Sensor is not present")
		def lastActivity5 = presenceSensor5.getLastActivity()
		LOGDEBUG("In presenceSensorHandler5 - lastActivity: ${lastActivity5}")
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity5}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		LOGDEBUG("In presenceSensorHandler5 - PS5 has been gone for ${timeDiff} minutes")	
		if(timeDiff < delay1) {
			log.info "${app.label} - ${parent.friendlyName5} just left! Time Diff: ${timeDiff}"
			state.nameCount = state.nameCount + 1
			if(state.nameCount == 1) state.presenceMap = [parent.friendlyName5]
			if(state.nameCount >= 2) state.presenceMap += [parent.friendlyName5]
			LOGDEBUG("In presenceSensorHandler5 - ${state.presenceMap}")
		} else {
			log.info "${app.label} - ${parent.friendlyName5} has been gone too long. No announcement needed."
		}
    } else {
		LOGDEBUG("In presenceSensorHandler5 - Presence Sensor is present. No announcement needed.")
		state.canSpeak = "yes"
    }
}

def letsTalk() {								// Heavily Modified from @Cobra Code
	LOGDEBUG("In letsTalk...")
	checkTime()
	if(state.timeOK == true) {
		messageHandler()
		LOGDEBUG("In letsTalk - Speaker(s) in use: ${speakers}")
		state.theMsg = "${state.theMessage}"
  		if (speechMode == "Music Player"){ 
    		LOGDEBUG("In letsTalk - Music Player")
			if(echoSpeaks) {
				setVolume()
				speakers.setVolumeSpeakAndRestore(state.volume, state.theMsg)
				log.info "${app.label} - speaking: ${state.theMsg}"
				LOGDEBUG("In letsTalk - Wow, that's it!")
			}
			if(!echoSpeaks) {
    			setVolume()
    			speakers.playTextAndRestore(state.theMsg)
				log.info "${app.label} - speaking: ${state.theMsg}"
				LOGDEBUG("In letsTalk - Wow, that's it!")
			}
  		}   
		if (speechMode == "Speech Synth"){ 
			LOGDEBUG("In letsTalk - Speech Synth - ${state.theMsg}")
			speakers.speak(state.theMsg)
			log.info "${app.label} - speaking: ${state.theMsg}"
			LOGDEBUG("In letsTalk - Wow, that's it!")
		}
	} else {
		LOGDEBUG("In letsTalk - It's quiet time...Can't talk right now")
	}
	state.canSpeak = "no"
}

def setVolume(){								// Modified from @Cobra Code
	LOGDEBUG("In setVolume...")
	def timecheck = fromTime2
	if (timecheck != null){
		def between2 = timeOfDayIsBetween(toDateTime(fromTime2), toDateTime(toTime2), new Date(), location.timeZone)
    if (between2) {
    	state.volume = volume2
   		if(!echoSpeaks) speakers.setLevel(state.volume)
   		LOGDEBUG("In setVolume - Quiet Time = Yes - Setting Quiet time volume")
   		LOGDEBUG("In setVolume - between2 = $between2 - state.volume = $state.volume - Speaker = $speakers - Echo Speakes = $echoSpeaks") 
	}
	if (!between2) {
		state.volume = volume1
		if(!echoSpeaks) speakers.setLevel(state.volume)
		LOGDEBUG("In setVolume - Quiet Time = No - Setting Normal time volume")
		LOGDEBUG("In setVolume - between2 = $between2 - state.volume = $state.volume - Speaker = $speakers - Echo Speakes = $echoSpeaks")
	}
	}
	else if (timecheck == null){
		state.volume = volume1
		if(!echoSpeaks) speakers.setLevel(state.volume)
	}
}

def checkTime(){							// Modified from @Cobra Code
	LOGDEBUG("In checkTime...")
	def timecheckNow = fromTime
	if (timecheckNow != null){
    
	def between = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
    if (between) {
    	state.timeOK = true
   		LOGDEBUG("In checkTime - Time is ok so can continue")
	}
	else if (!between) {
		state.timeOK = false
		LOGDEBUG("In checkTime - Time is NOT ok so can't continue")
	}
  	}
	else if (timecheckNow == null){  
		state.timeOK = true
  		LOGDEBUG("In checkTime - Time restrictions have not been configured - Continue")
  	}
}

def messageHandler() {
	LOGDEBUG("In messageHandler...")
	if(oRandom) {
		def values = "${message}".split(";")
		vSize = values.size()
		count = vSize.toInteger()
    	def randomKey = new Random().nextInt(count)
		theMessage = values[randomKey]
		LOGDEBUG("In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMessage: ${theMessage}") 
	} else {
		theMessage = "${message}"
		LOGDEBUG("In messageHandler - Static - theMessage: ${theMessage}")
	}
   	theMessage = theMessage.toLowerCase()
	if (theMessage.toLowerCase().contains("%greeting%")) {theMessage = theMessage.toLowerCase().replace('%greeting%', getGreeting() )}
	if (theMessage.toLowerCase().contains("%name%")) {theMessage = theMessage.toLowerCase().replace('%name%', getName() )}
	if (theMessage.toLowerCase().contains("%is_are%")) {theMessage = theMessage.toLowerCase().replace('%is_are%', "${is_are}" )}
	if (theMessage.toLowerCase().contains("%has_have%")) {theMessage = theMessage.toLowerCase().replace('%has_have%', "${has_have}" )}
	state.theMessage = "${theMessage}"
	return state.theMessage
}

private getName(){
	LOGDEBUG("In getName...")
	LOGDEBUG("In getName...Number of Names: ${state.nameCount} - Names: ${state.presenceMap}")
	name = ""
	myCount = 0
	if(state.nameCount == 1) {
		state.presenceMap.each { it -> 
			LOGDEBUG("*********** In nameCount=1: myCount = ${myCount}")
			name = "${it.value}"
		}
	}
	if(state.nameCount == 2) {
		state.presenceMap.each { it -> 
			LOGDEBUG("*********** In nameCount=2: myCount = ${myCount}")
			myCount = myCount + 1
			name = "${name}" + "${it.value} "
			if(myCount == 1) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 3) {
		state.presenceMap.each { it -> 
			LOGDEBUG("*********** In nameCount=3: myCount = ${myCount}")
			myCount = myCount + 1
			name = "${name}" + "${it.value}, "
			if(myCount == 2) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 4) {
		state.presenceMap.each { it -> 
			LOGDEBUG("*********** In nameCount=4: myCount = ${myCount}")
			myCount = myCount + 1
			name = "${name}" + "${it.value}, "
			if(myCount == 3) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 5) {
		state.presenceMap.each { it -> 
			LOGDEBUG("*********** In nameCount=5: myCount = ${myCount}")
			myCount = myCount + 1
			name = "${name}" + "${it.value}, "
			if(myCount == 4) name = "${name}" + "and "
		}
	}
	is_are = (name.contains(' and ') ? 'are' : 'is')
	has_have = (name.contains(' and ') ? 'have' : 'has')
	if(name == null) names = "Whoever you are"
	if(name == "") names = "Whoever you are"
	LOGDEBUG("AGAIN...Name = ${name}")
	return name
}

private getGreeting(){						// Modified from @Cobra Code
	LOGDEBUG("In getGreeting...")
    def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
	def timeHH = calendar.get(Calendar.HOUR) toInteger()
    def timeampm = calendar.get(Calendar.AM_PM) ? "pm" : "am" 
	LOGDEBUG("timeHH = $timeHH")
	if(timeampm == 'am'){
		if(oRandomG1) {
			def values = "${greeting1}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			LOGDEBUG("In getGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm = ${timeampm} - timehh = ${timeHH}") 
		} else {
			state.greeting = "${greeting1}"
			LOGDEBUG("In getGreeting - Static - greeting: ${state.greeting}")
		}
	}
	else if(timeampm == 'pm' && timeHH < 6){
		if(oRandomG2) {
			def values = "${greeting2}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			LOGDEBUG("In getGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm = ${timeampm} - timehh = ${timeHH}") 
		} else {
			state.greeting = "${greeting2}"
			LOGDEBUG("In getGreeting - Static - greeting: ${state.greeting}")
		}
	}
	else if(timeampm == 'pm' && timeHH >= 6){
		if(oRandomG3) {
			def values = "${greeting3}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			LOGDEBUG("In getGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm = ${timeampm} - timehh = ${timeHH}") 
		} else {
			state.greeting = "${greeting3}"
			LOGDEBUG("In getGreeting - Static - greeting: ${state.greeting}")
		}
	}
	return state.greeting
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
	setupNewStuff()
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
	state.nameCount = 0
	state.canSpeak = "no"
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
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Departures - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
