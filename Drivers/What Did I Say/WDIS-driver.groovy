/**
 *  ****************  What Did I Say Driver  ****************
 *
 *  importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Drivers/What%20Did%20I%20Say/WDIS-driver.groovy"
 *
 *  Design Usage:
 *  This driver formats Speech data to be displayed on Hubitat's Dashboards and also acts as a proxy speaker to 'Follow Me'.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 * ------------------------------------------------------------------------------------------------------------------------------
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
 *  V2.0.0 - 08/04/19 - Changed up how speech is handled and sent to 'Follow Me'. Thanks to storageanarchy for teaching me some new tricks!
 *  V1.1.9 - 08/03/19 - Added initialize section, added deviceNotification
 *  V1.1.8 - 07/27/19 - '%5B'is replaced with '[' and '%5D'is replaced with ']' in any speech received.
 *  V1.1.7 - 07/22/19 - Second try at fixing bug in priority handling.
 *  V1.1.6 - 07/22/19 - Found bug in priority handling. '%20'is replaced with ' ' in any speech received.
 *  V1.1.5 - 07/15/19 - Minor code changes
 *  V1.1.4 - 06/09/19 - Code changes to better handle priority messages
 *  V1.1.3 - 04/16/19 - Code cleanup, added importUrl
 *  V1.1.2 - 04/12/19 - Fixed length of message typo
 *  V1.1.1 - 04/06/19 - Added setVolume to code
 *  V1.1.0 - 04/05/19 - Added speaker status to 'Reset All Data' switch 
 *  V1.0.9 - 04/03/19 - Attempt to fix an error
 *  V1.0.8 - 04/03/19 - More tweaks to speaker status
 *  V1.0.7 - 04/03/19 - Add ability to display Speaker status on dashboards 
 *  V1.0.6 - 03/27/19 - More enhancements for 'Follow Me', color coded priority messages!
 *  V1.0.5 - 03/17/19 - Added code to make this compatible with my 'Follow Me' app. Also each Message will now have a max length of 70
 *  characters displayed. To reduce load on the Dashboards. This does NOT affect what is actually spoken.
 *  V1.0.4 - 03/02/19 - Fixed the date being display on tile
 *  V1.0.2 - 02/18/19 - Adding command initialize
 *  V1.0.1 - 02/08/19 - Changed the 'How many lines' field from 5 or 10, to any number from 1 to 10. Attempt to fix a reported error.
 *  V1.0.0 - 01/27/19 - Initial release
 */

import groovy.json.*
    
def version(){"v2.0.0"}

metadata {
	definition (name: "What Did I Say", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Drivers/What%20Did%20I%20Say/WDIS-driver.groovy") {
   		capability "Initialize"
		capability "Actuator"
		capability "Speech Synthesis"
		capability "Music Player"
        capability "Notification"
		
        command "playAnnouncement", ["string", "number", "number"]
        command "playAnnouncement", ["string", "string", "number", "number"]
        command "playAnnouncementAll", ["string", "string"]
		command "playTextAndRestore", ["string", "number"]
        command "playTrackAndRestore", ["string"]
        command "setLevel", ["string"]
        command "setVolume", ["string"]
		command "setVolumeSpeakAndRestore", ["number", "string", "number"]
        command "setVolumeAndSpeak", ["number", "string"]
		command "speak", ["string"]
		command "sendFollowMeSpeaker", ["string"]
        command "deviceNotification", ["string"]
		
    	attribute "whatDidISay", "string"
		attribute "lastSpoken", "string"
		attribute "latestMessage", "string"
		attribute "speakerStatus1", "string"
		attribute "speakerStatus2", "string"
		attribute "speakerStatus3", "string"
		attribute "speakerStatus4", "string"
        
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
			input("numOfLines", "number", title: "How many lines to display (from 1 to 10 only)", required:true, defaultValue: 5)
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
			input("clearData", "bool", title: "Reset All Data", required: false, defaultValue: false)
			input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

//Received new messages from apps

// -- code by @storageanarchy - Thank you for showing me how to pass all the variables!
String composeMessageMap(method, message, priority='n', speakLevel=null, returnLevel=null, title='') {
    return JsonOutput.toJson([method: method as String, message: message as String, priority: priority as String, speakLevel: speakLevel, returnLevel: returnLevel, title: title as String])
}

def playAnnouncement(message, arg2, arg3) {
    state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', state.speechReceivedFULL, arg2, arg3)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def playAnnouncement(String message, title=null, arg3, arg4) {
    state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', state.speechReceivedFULL, arg3, arg4, title)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def playAnnouncementAll(message, title=null, arg2) {
    state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncementAll', state.speechReceivedFULL, arg2)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def deviceNotification(message) {
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('deviceNotification', state.speechReceivedFULL)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def playTextAndRestore(message) {
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTextAndRestore', state.speechReceivedFULL, arg2)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def playTrackAndRestore(message) {
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTrackAndRestore', state.speechReceivedFULL, arg2)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def setLevel(message) {
    state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setLevel', state.speechReceivedFULL)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()  
}

def setVolume(message) {
    state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolume', state.speechReceivedFULL)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()     
}

def setVolumeSpeakAndRestore(message, arg2, arg3) {
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', arg2, state.speechReceivedFULL, arg3)
    sendEvent(name: "latestMessage", value: theMessage)
	populateMap()
}

def setVolumeAndSpeak(message, arg2) {
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', state.speechReceivedFULL, arg2)
    sendEvent(name: "latestMessage", value: theMessage)
	populateMap()
}

def speak(message) {
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('speak', state.speechReceivedFULL)
    sendEvent(name: "latestMessage", value: theMessage)
    populateMap()
}

def makeUnique() {
	if(state.unique == null) state.unique = "a"
	if(state.unique == "a") {
		state.unique = "b"
	} else {
		state.unique = "a"
	}
    state.speechReceivedUnique = "${state.unique}" + "${state.speechReceivedFULL}"
	sendEvent(name: "lastSpokenUnique", value: state.speechReceivedUnique, displayed: true)
}

def populateMap() {
	if(logEnable) log.debug "What Did I Say - Received new Speech! ${state.speechReceivedFULL}"
    state.speechReceived = state.speechReceivedFULL.take(70)
	makeUnique()
	sendEvent(name: "lastSpoken", value: state.speechReceivedFULL, displayed: true)
    
	if(state.speechReceived.contains("]")) {
		def (priority, msgA) = state.speechReceived.split(']')
		state.priority = priority.drop(1)
		state.speech = msgA
	} else{
		state.speech = state.speechReceived
        state.priority = ""
	}
	
	if((state.priority.toLowerCase().contains("l")) || (state.priority.toLowerCase().contains("n")) || (state.priority.toLowerCase().contains("h"))) {
		if(state.priority.toLowerCase().contains("l")) { state.lastSpoken = "<font color='yellow'>${state.speech}</font>" }
		if(state.priority.toLowerCase().contains("n")) { state.lastSpoken = "${state.speech}" }
		if(state.priority.toLowerCase().contains("h")) { state.lastSpoken = "<font color='red'>${state.speech}</font>" }
	} else {
		state.lastSpoken = "${state.speech}"
	}
	
	// Read in the maps
	try {
		sOne = state.speechMap1.get(state.s,nMessage)
		sTwo = state.speechMap2.get(state.s,nMessage)
		sThree = state.speechMap3.get(state.s,nMessage)
		sFour = state.speechMap4.get(state.s,nMessage)
		sFive = state.speechMap5.get(state.s,nMessage)
		sSix = state.speechMap6.get(state.s,nMessage)
		sSeven = state.speechMap7.get(state.s,nMessage)
		sEight = state.speechMap8.get(state.s,nMessage)
		sNine = state.speechMap9.get(state.s,nMessage)
		sTen = state.speechMap10.get(state.s,nMessage)
	}
	catch (e) {
        //log.error "Error:  $e"
    }
	
	if(logEnable) log.debug "What Did I Say - OLD -<br>sOne: ${sOne}<br>sTwo: ${sTwo}<br>sThree: ${sThree}<br>sFour: ${sFour}<br>sFive: ${sFive}"
	
	if(sOne == null) sOne = "${state.nMessage}"
	if(sTwo == null) sTwo = "${state.nMessage}"
	if(sThree == null) sThree = "${state.nMessage}"
	if(sFour == null) sFour = "${state.nMessage}"
	if(sFive == null) sFive = "${state.nMessage}"
	if(sSix == null) sSix = "${state.nMessage}"
	if(sSeven == null) sSeven = "${state.nMessage}"
	if(sEight == null) sEight = "${state.nMessage}"
	if(sNine == null) sNine = "${state.nMessage}"
	if(sTen == null) sTen = "${state.nMessage}"
	
	// Move all messages down 1 slot
	mTen = sNine
	mNine = sEight
	mEight = sSeven
	mSeven = sSix
	mSix = sFive
	mFive = sFour
	mFour = sThree
	mThree = sTwo
	mTwo = sOne
	
	getDateTime()
	mOne = state.newdate + " - " + state.lastSpoken
	
	if(logEnable) log.debug "What Did I Say - NEW -<br>mOne: ${mOne}<br>mTwo: ${mTwo}<br>mThree: ${mThree}<br>mFour: ${mFour}<br>mFive: ${mFive}"
	
	// Fill the maps back in
	try {
		state.speechMap1.put(state.s,mOne)
		state.speechMap2.put(state.s,mTwo)
		state.speechMap3.put(state.s,mThree)
		state.speechMap4.put(state.s,mFour)
		state.speechMap5.put(state.s,mFive)
		state.speechMap6.put(state.s,mSix)
		state.speechMap7.put(state.s,mSeven)
		state.speechMap8.put(state.s,mEight)
		state.speechMap9.put(state.s,mNine)
		state.speechMap10.put(state.s,mTen)
	}
	catch (e) {
        //log.error "Error:  $e"
    }
	
	state.speechTop = "<table width='100%'><tr><td align='left'>"
	if(numOfLines == 1) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}</div>"
	}
	if(numOfLines == 2) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}</div>"
	}
	if(numOfLines == 3) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}</div>"
	}
	if(numOfLines == 4) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}</div>"
	}
	if(numOfLines == 5) {
		state.speechTop+= "<div style=';font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}</div>"
	} 
	if(numOfLines == 6) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}</div>"
	}
	if(numOfLines == 7) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}</div>"
	}
	if(numOfLines == 8) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}</div>"
	}
	if(numOfLines == 9) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}</div>"
	}
	if(numOfLines == 10) {
		state.speechTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}<br>${mTen}</div>"
	}
	state.speechTop+= "</td></tr></table>"
	
	state.speechTopCount = state.speechTop.length()
	if(state.speechTopCount <= 1000) {
		if(logEnable) log.debug "What Did I Say - ${state.speechTopCount} Characters<br>${state.speechTop}"
	} else {
		state.speechTop = "Too many characters to display on Dashboard"
	}
	sendEvent(name: "whatDidISay", value: state.speechTop, displayed: true)
}

def installed(){
    log.info "What Did I Say has been Installed"
    clearSpeechData()
}

def updated() {
    log.info "What Did I Say has been Updated"
    if (clearData) runIn(2,clearSpeechData)
}

def initialize() {
    log.info "In initialize..."
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) state.newdate=date.format("MM-d HH:mm")
	if(hourType == true) state.newdate=date.format("MM-d hh:mm")
}

def clearDataOff(){
    log.info "What Did I Say has cleared the data"
    device.updateSetting("clearData",[value:"false",type:"bool"])
}

def clearSpeechData(){
	if(logEnable) log.debug "What Did I Say - clearing the data"
	state.nMessage = "No Data"
	state.s = "s"
	state.speechMap1 = [:]
	state.speechMap1.put(state.s,state.nMessage)
	state.speechMap2 = [:]
	state.speechMap2.put(state.s,state.nMessage)
	state.speechMap3 = [:]
	state.speechMap3.put(state.s,state.nMessage)
	state.speechMap4 = [:]
	state.speechMap4.put(state.s,state.nMessage)
	state.speechMap5 = [:]
	state.speechMap5.put(state.s,state.nMessage)
	state.speechMap6 = [:]
	state.speechMap6.put(state.s,state.nMessage)
	state.speechMap7 = [:]
	state.speechMap7.put(state.s,state.nMessage)
	state.speechMap8 = [:]
	state.speechMap8.put(state.s,state.nMessage)
	state.speechMap9 = [:]
	state.speechMap9.put(state.s,state.nMessage)
	state.speechMap10 = [:]
	state.speechMap10.put(state.s,state.nMessage)
	
	state.speakerMap  = [:]
	state.speakerMapS  = [:]
	state.sMap1S = [:]
	state.sMap2S = [:]
	state.sMap3S = [:]
	state.sMap4S = [:]
	state.sMap1S = "Waiting for Data"
	state.sMap2S = "Waiting for Data"
	state.sMap3S = "Waiting for Data"
	state.sMap4S = "Waiting for Data"
	sendEvent(name: "speakerStatus1", value: state.sMap1S, displayed: true)
	sendEvent(name: "speakerStatus2", value: state.sMap2S, displayed: true)
	sendEvent(name: "speakerStatus3", value: state.sMap3S, displayed: true)
	sendEvent(name: "speakerStatus4", value: state.sMap4S, displayed: true)
	
	state.speechTop = "Waiting for Data..."
	sendEvent(name: "whatDidISay", value: state.speechTop, displayed: true)
	if (clearData) runIn(2,clearDataOff)
}	

def sendFollowMeSpeaker(status) {
	if(logEnable) log.debug "In sendFollowMeSpeaker - Received new speaker status - ${status}"
	def (sName, sStatus) = status.split(':')
	if(logEnable) log.debug "In sendFollowMeSpeaker - sName: ${sName} - sStatus: ${sStatus}"
	if(state.speakerMap == null) state.speakerMap = [:]
	state.speakerMap.put(sName, sStatus)
	state.speakerMapS = [:]
	state.sMap1S = [:]
	state.sMap2S = [:]
	state.sMap3S = [:]
	state.sMap4S = [:]
	state.speakerMapS = state.speakerMap.sort { a, b -> a.key <=> b.key }
	state.count = 0
	state.sMap1S = "<table width='100%'>"
	state.sMap2S = "<table width='100%'>"
	state.sMap3S = "<table width='100%'>"
	state.sMap4S = "<table width='100%'>"
	state.speakerMapS.each { it -> 
		status = it.value
		state.count = state.count + 1
		if(logEnable) log.debug "In sendFollowMeSpeaker - Building Speaker Table with ${it.key} count: ${state.count}"
		if((state.count >= 1) && (state.count <= 5)) {
			if(status == "true") state.sMap1S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: green;font-size:.${fontSize}em;'>Active</div></td></tr>"
			if(status == "false") state.sMap1S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: red;font-size:.${fontSize}em;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap1S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: blue;font-size:.${fontSize}em;'>Speaking</div></td></tr>"
		}
		if((state.count >= 6) && (state.count <= 10)) {
			if(status == "true") state.sMap2S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: green;font-size:.${fontSize}em;'>Active</div></td></tr>"
			if(status == "false") state.sMap2S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: red;font-size:.${fontSize}em;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap2S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: blue;font-size:.${fontSize}em;'>Speaking</div></td></tr>"
		}
		if((state.count >= 11) && (state.count <= 15)) {
			if(status == "true") state.sMap3S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: green;font-size:.${fontSize}em;'>Active</div></td></tr>"
			if(status == "false") state.sMap3S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: red;font-size:.${fontSize}em;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap3S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: blue;font-size:.${fontSize}em;'>Speaking</div></td></tr>"
		}
		if((state.count >= 16) && (state.count <= 20)) {
			if(status == "true") state.sMap4S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: green;font-size:.${fontSize}em;'>Active</div></td></tr>"
			if(status == "false") state.sMap4S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: red;font-size:.${fontSize}em;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap4S += "<tr><td align='left'><div style='font-size:.${fontSize}em;'>${it.key}</div></td><td><div style='color: blue;font-size:.${fontSize}em;'>Speaking</div></td></tr>"
		}
	}
	state.sMap1S += "</table>"
	state.sMap2S += "</table>"
	state.sMap3S += "</table>"
	state.sMap4S += "</table>"
	
	sendEvent(name: "speakerStatus1", value: state.sMap1S, displayed: true)
	sendEvent(name: "speakerStatus2", value: state.sMap2S, displayed: true)
	sendEvent(name: "speakerStatus3", value: state.sMap3S, displayed: true)
	sendEvent(name: "speakerStatus4", value: state.sMap4S, displayed: true)
}
