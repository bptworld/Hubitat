/**
 *  ****************  Life360 Tracker Child App  ****************
 *
 *  Design Usage:
 *  Track your Life360 users. Works with the Life360 with States app.
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
 *  V1.0.5 - 07/07/19 - First attempt at departure notifications and a few other goodies, please check your child apps
 *  V1.0.4 - 07/05/19 - Complete rewrite of how the app speaks
 *  V1.0.3 - 07/04/19 - Made pushover an option with or without speech, Trying to change up how volume is restored (thanks @doug)
 *  V1.0.2 - 07/04/19 - Added an optional Map link to each push, added Options to turn Speaking on/off, changed/added some descriptions
 *  V1.0.1 - 07/04/19 - Added all attributes as wildcards
 *  V1.0.0 - 07/01/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.5"
}

definition(
    name: "Life360 Tracker Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track your Life360 users. Works with the Life360 with States app.",
    category: "Convenience",
	parent: "BPTWorld:Life360 Tracker",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Life360%20Tracker/LT-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "alertsConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Tracker</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Track your Life360 users. Works with the user Life360 with States app."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Device")) {
			input "presenceDevice", "capability.presenceSensor", title: "Select Life360 User Device", required: true
            input "friendlyName", "text", title: "Friendly Name used in messages for this Device", required: true, submitOnChange: "true"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Place Tracking")) {
            paragraph "This will track the coming and going of places."
			input "trackingOptions", "enum", title: "How to track Places" , options: ["Track All","Track Specific"], required: true, submitOnChange: "true", defaultValue: "Track All"
            if(trackingOptions == "Track Specific") {
                input "trackSpecific", "text", title: "Track Specific Place, Seperate multiple Places with a <b>;</b><br><small>Must be the exact name(s) of Places already saved in your Life360 Phone App</small>", required: false, submitOnChange: "true"
                input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of Specific Places?", description: "List View", submitOnChange: "true")
                if(oG1List) {
			    	def valuesG1 = "${trackSpecific}".split(";")
			    	listMapG1 = ""
    			    valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
				    paragraph "${listMapG1}"
			    }
            }
            if(trackingOptions == "Track All") {
                paragraph "Tracking all places"
            }
            input "timeConsideredHere", "number", title: "Time to be considered at a Place (in Minutes)", required: true, submitOnChange: true, defaultValue: 2
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Time to Track")) {
        	input "timeToTrack", "enum", title: "How often to track Places", options: ["1 Minute","5 Minutes"], required: true, submitOnChange: true, defaultValue: "5 Minutes"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a device<br>%place% - returns the place arrived or departed"
            paragraph "* PLUS - all attribute names can be used as wildcards! Just make sure the name is exact, capitalization counts!  ie. %powerSource%, %distanceMiles% or %wifiState%"
            input(name: "speakHasArrived", type: "bool", defaultValue: "false", title: "Speak when someone 'Has arrived'", description: "Speak Has Arrived", submitOnChange: true)
			if(speakHasArrived) input "messageAT", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has arrived at %place%"
			if(speakHasArrived) input(name: "atMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if(speakHasArrived && atMsgList) {
				def values = "${messageAT}".split(";")
				listMapAT = ""
    			values.each { item -> listMapAT += "${item}<br>"}
				paragraph "${listMapAT}"
			}
            if(speakHasArrived) paragraph "<hr>"
            
            
            input(name: "speakHasDepated", type: "bool", defaultValue: "false", title: "Speak when someone 'Has departed'", description: "Speak Has departed", submitOnChange: true)
			if(speakHasDepated) input "messageDEP", "text", title: "Random Message to be spoken when <b>'has departed'</b> a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has departed from %place%"
			if(speakHasDepated) input(name: "depMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if(speakHasDepated && depMsgList) {
				def values = "${messageDEP}".split(";")
				listMapDEP = ""
    			values.each { item -> listMapDEP += "${item}<br>"}
                paragraph "${listMapDEP}"
			}
            if(speakHasDepated) paragraph "<hr>"
            
            
            input(name: "speakOnTheMove", type: "bool", defaultValue: "false", title: "Speak when someone 'is on the move'", description: "Speak On the Move", submitOnChange: true)
            if(speakOnTheMove) input "messageMOVE", "text", title: "Random Message to be spoken when <b>'on the move'</b> near a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% is on the move near %place%"
			if(speakOnTheMove) input(name: "moveMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if(speakOnTheMove && moveMsgList) {
				def values = "${messageMOVE}".split(";")
				listMapMove = ""
    			values.each { item -> listMapMove += "${item}<br>"}
				paragraph "${listMapMove}"
			}
            if(speakOnTheMove) paragraph "<hr>"
            
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
            if(sendPushMessage) input(name: "linkPush", type: "bool", defaultValue: "false", title: "Send Map Link with Push", description: "Send Google Maps Link")
		}
        if(speakHasArrived || speakHasDeparted || speakOnTheMove) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
                paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
              	input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
         		input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
                input(name: "speakerProxy", type: "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy")
          	}
		    section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		    	paragraph "NOTE: Not all speakers can use volume controls. Please click the button to test your selected speakers. Then check your logs to see how they did.", width:8
                input "testSpeaker", "button", title: "Test Speaker", submitOnChange: true, width: 4
                paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true, width: 6
		        input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true, width: 6
                input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required: false, submitOnChange: true
			    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true, width: 6
    		    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true, width: 6
		    }
			section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
        		input "fromTime", "time", title: "From", required: false, width: 6
        		input "toTime", "time", title: "To", required: false, width: 6
			}
    	}
		section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            input "isDataDevice", "capability.switch", title: "Turn this device on/off (On = at place, Off = moving)", required: false, multiple: false
        }
//        section(getFormat("header-green", "${getImage("Blank")}"+" Extra Options")) {
//            href "alertsConfig", title: "Alerts", description: "Click here to setup Alerts."
//		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def alertsConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Tracker - Alerts</h2>", install: false, uninstall: false, refreshInterval:0) {
		display() 
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Alerts")) {
            paragraph "<b>Battery Alert</b>"

		}
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	updated()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()
	if(timeToTrack == "1 Minute") runEvery1Minute(userHandler)
    if(timeToTrack == "5 Minutes") runEvery5Minutes(userHandler)
}
	
def userHandler(evt) {
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    if(state.tDiff > timeHere) {
        if(logEnable) log.debug "In userHandler - Time at Place: ${state.tDiff} IS greater than: ${timeHere}"
    } else {
        if(logEnable) log.debug "In userHandler - Time at Place: ${state.tDiff} IS NOT greater than: ${timeHere}"
    }
    if(state.address1Value == state.prevPlace) {
        if(logEnable) log.debug "In userHandler - address1: ${state.address1Value} MATCHES state.prevPlace: ${state.prevPlace}"
    } else {
        if(logEnable) log.debug "In userHandler - address1: ${state.address1Value} DOES NOT MATCH state.prevPlace: ${state.prevPlace}"
    }
    
    if(state.address1Value == state.prevPlace) {
    
    // ***** Track All *****
        if(trackingOptions == "Track All") {
            if(logEnable) log.debug "In userHandler - Tracking All - ${friendlyName} is at ${state.address1Value}"

            if(state.tDiff > timeHere) {
                if(state.beenHere == "no") {
                    if(logEnable) log.debug "In userHandler - Tracking All - ${friendlyName} has arrived at ${state.address1Value}"
                    state.msg = "${messageAT}"
                    if(isDataDevice) isDataDevice.on()
                    if(speakHasArrived) messageHandler()
                } else {
                    if(logEnable) log.debug "In userHandler - Tracking All - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                }
                state.prevPlace = state.address1Value
                state.beenHere = "yes"
                state.onTheMove = "no"
            }
        }
  
        // ***** Track Specific *****    
        if(trackingOptions == "Track Specific") {
            theAddress1 = state.address1Value.toLowerCase()
            if(trackSpecific.toLowerCase().contains("${theAddress1}")) {
                if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} is at ${state.address1Value}"
                
                if(state.tDiff > timeHere) {
                    if(state.beenHere == "no") {
                        if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} has arrived at ${state.address1Value}"
                        state.msg = "${messageAT}"
                        if(isDataDevice) isDataDevice.on()
                        if(speakHasArrived) messageHandler()
                    } else {
                        if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                    }
                    state.prevPlace = state.address1Value
                    if(isDataDevice) isDataDevice.on()
                    state.beenHere = "yes"
                    state.onTheMove = "no"
                }
            } else {
		        if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} is not at a place this app is tracking ${state.address1Value}"
                state.prevPlace = state.address1Value
                if(isDataDevice) isDataDevice.off()
                state.beenHere = "no"
                state.onTheMove = "no"
            }
        }
    } else {
        if(state.onTheMove == "no") {
            if(logEnable) log.debug "In userHandler - ${friendlyName} has departed from ${state.address1Value}"
            state.msg = "${messageDeparted}"
            if(isDataDevice) isDataDevice.off()
            if(speakHasDeparted) messageHandler()
        } else {
            if(logEnable) log.debug "In userHandler - ${friendlyName} is on the move near ${state.address1Value}"
            state.msg = "${messageMOVE}"
            if(isDataDevice) isDataDevice.off()
            if(speakOnTheMove) messageHandler()
        }
        state.prevPlace = state.address1Value
        state.beenHere = "no"
        state.onTheMove = "yes"
        if(isDataDevice) isDataDevice.off()
    }
}

def getTimeDiff() {
	if(logEnable) log.debug "In getTimeDiff..."
	long since = presenceDevice.currentValue("since")
   	def now = new Date()
    long unxNow = now.getTime()
    unxNow = unxNow/1000    
    long timeDiff = Math.abs(unxNow-since)
    state.tDiff = timeDiff
    if(logEnable) log.debug "In getTimeDiff - since: ${since}, Now: ${unxNow}, Diff: ${timeDiff}"
    
	state.timeDay = (timeDiff / 86400).toInteger()
    state.timeHrs = ((timeDiff % 86400 ) / 3600).toInteger()
	state.timeMin = (((timeDiff % 86400 ) % 3600 ) / 60).toInteger()
	state.timeSec = (((timeDiff % 86400 ) % 3600 ) % 60).toInteger()
    
    if(logEnable) log.debug "In getTimeDiff - Time Diff: ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk..."
	checkTime()
	checkVol()
	atomicState.randomPause = Math.abs(new Random().nextInt() % 1500) + 400
	if(logEnable) log.debug "In letsTalk - pause: ${atomicState.randomPause}"
	pauseExecution(atomicState.randomPause)
	if(logEnable) log.debug "In letsTalk - continuing"
	if(state.timeBetween == true) {
		state.theMsg = "${state.theMessage}"
    	if(logEnable) log.debug "In letsTalk - speaker: ${speaker}, vol: ${state.volume}, msg: ${state.theMsg}, volRestore: ${volRestore}"
        speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
        atomicState.speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
            state.speakers.each {
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
                    pauseExecution(atomicState.speechDuration2)
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                    if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
                }
            }
        pauseExecution(atomicState.speechDuration2)
        state.canSpeak = "no"
	    if(logEnable) log.debug "In letsTalk - that's it!"  
		log.info "${app.label} - ${state.theMsg}"
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
}

def checkVol(){
	if(logEnable) log.debug "In checkVol..."
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler..."
	message = state.msg
    
	def values = "${message}".split(";")
	vSize = values.size()
	count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMessage = values[randomKey]
    
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMessage: ${theMessage}" 
	if(theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', friendlyName )}
    if(theMessage.contains("%place%")) {theMessage = theMessage.replace('%place%', state.address1Value )}
    if(theMessage.contains("%address1%")) {theMessage = theMessage.replace('%address1%', presenceDevice.currentValue("address1") )}
    if(theMessage.contains("%address2%")) {theMessage = theMessage.replace('%address2%', presenceDevice.currentValue("address2") )}
    if(theMessage.contains("%battery%")) {theMessage = theMessage.replace('%battery%', presenceDevice.currentValue("battery") )}
    if(theMessage.contains("%charge%")) {theMessage = theMessage.replace('%charge%', presenceDevice.currentValue("charge") )}
    if(theMessage.contains("%distanceKm%")) {theMessage = theMessage.replace('%distanceKm%', presenceDevice.currentValue("distanceKm") )}
    if(theMessage.contains("%distanceMetric%")) {theMessage = theMessage.replace('%distanceMetric%', presenceDevice.currentValue("distanceMetric") )}
    if(theMessage.contains("%distanceMiles%")) {theMessage = theMessage.replace('%distanceMiles%', presenceDevice.currentValue("distanceMiles") )}
    if(theMessage.contains("%inTransit%")) {theMessage = theMessage.replace('%inTransit%', presenceDevice.currentValue("inTransit") )}
    if(theMessage.contains("%isDriving%")) {theMessage = theMessage.replace('%isDriving%', state.presenceDevice.currentValue("isDriving") )}
    if(theMessage.contains("%lastCheckin%")) {theMessage = theMessage.replace('%lastCheckin%', state.presenceDevice.currentValue("lastCheckin") )}
    if(theMessage.contains("%latitude%")) {theMessage = theMessage.replace('%latitude%', state.presenceDevice.currentValue("latitude") )}
    if(theMessage.contains("%longitude%")) {theMessage = theMessage.replace('%longitude%', state.presenceDevice.currentValue("longitude") )}
    if(theMessage.contains("%powerSource%")) {theMessage = theMessage.replace('%powerSource%', state.presenceDevice.currentValue("powerSource") )}
    if(theMessage.contains("%presence%")) {theMessage = theMessage.replace('%presence%', state.presenceDevice.currentValue("presence") )}
    if(theMessage.contains("%speedKm%")) {theMessage = theMessage.replace('%speedKm%', state.presenceDevice.currentValue("speedKm") )}
    if(theMessage.contains("%speedMetric%")) {theMessage = theMessage.replace('%speedMetric%', state.presenceDevice.currentValue("speedMetric") )}
    if(theMessage.contains("%speedMiles%")) {theMessage = theMessage.replace('%speedMiles%', state.presenceDevice.currentValue("speedMiles") )}
    if(theMessage.contains("%wifiState%")) {theMessage = theMessage.replace('%wifiState%', state.presenceDevice.currentValue("wifiState") )}
    if(theMessage.contains("%display%")) {theMessage = theMessage.replace('%display%', state.presenceDevice.currentValue("display") )}
    if(theMessage.contains("%status%")) {theMessage = theMessage.replace('%status%', state.presenceDevice.currentValue("status") )}
    if(theMessage.contains("%lastLocationUpdate%")) {theMessage = theMessage.replace('%lastLocationUpdate%', state.presenceDevice.currentValue("lastLocationUpdate") )}
	state.theMessage = "${theMessage}"
	
    if(speakHasArrived || speakOnTheMove) letsTalk()
    if(sendPushMessage) pushHandler()
}

def pushHandler() {
	if(logEnable) log.debug "In pushNow..."
    theMessage = "${state.theMessage}\n\n"
    if(linkPush) {theMessage += "https://www.google.com/maps/search/?api=1&query=${presenceDevice.currentValue("latitude")},${presenceDevice.currentValue("longitude")}"}
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "testSpeaker"){
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.debug "In testButtonHandler - Testing Speaker"
        testResult = "<table><tr><td colspan=3 align=center>----------------------------------------------------------------</td></tr>"
        testResult += "<tr><td colspan=3 align=center><b>Speaker Test Results</b></td></tr>"
        state.speakers.each {
            if(it.hasCommand('setVolumeSpeakAndRestore')) {
                testResult += "<tr><td>${it}</td><td> - </td><td>uses setVolumeSpeakAndRestore</td></tr>"
            } else if(it.hasCommand('playTextAndRestore')) {
                testResult += "<tr><td>${it}</td><td> - </td><td>uses playTextAndRestore</td></tr>"
            } else {
                testResult += "<tr><td>${it}</td><td> - </td><td>needs all volume fields filled in</td></tr>"
            }
        }
        testResult += "<tr><td colspan=3><br>*Note: Speaker proxies can't be accurately tested.<br>If using a speaker proxy like 'What Did I Say', always fill in the failsafe fields.</td><tr>"
        testResult += "<tr><td colspan=3 align=center>----------------------------------------------------------------</td></tr>"
        testResult += "</table>"
        log.info "${testResult}"
    }
}
                   
// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    if(state.beenHere == null){state.beenHere = "no"}
    if(state.address1Value == null){state.address1Value = presenceDevice.currentValue("address1")}
    if(state.prevPlace == null){state.prevPlace = state.address1Value}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Life360 Tracker - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
