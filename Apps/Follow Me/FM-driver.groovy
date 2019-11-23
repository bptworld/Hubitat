/**
 *  ****************  Follow Me Driver  ****************
 *
 *  Design Usage:
 *  This driver formats Speech data to be displayed on Hubitat's Dashboards and also acts as a proxy speaker to 'Follow Me'.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums to let
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
 *  V2.1.0 - 11/14/19 - Name changed to match Follow Me. Major rework. Changes to work with the updated Follow Me (V2.0.5+)
 *  ---
 *  V1.0.0 - 01/27/19 - Initial release
 */

import groovy.json.*
    
def setVersion(){
    appName = "FollowMeDriver"
	version = "v2.1.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Follow Me Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Initialize"
		capability "Actuator"
		capability "Speech Synthesis"
		capability "Music Player"
        capability "Notification"
		
        command "playAnnouncement", 	[[name:"Text*", type:"STRING", description:"Text to play"], 
										 [name:"Volume Level", type:"NUMBER", description: "Volume level (0-100)"], 
										 [name:"Restore Volume Level",type:"NUMBER", description: "Restore volume (0-100)"]]
        command "playAnnouncement", 	[[name:"Text*", type: "STRING", description:"Text to play"], 
										 [name:"Title*", type:"STRING", description: "Title to display on Echo Show devices"], 
										 [name:"Volume Level", type:"NUMBER", description: "Volume level (0-100)"], 
										 [name:"Restore Volume Level",type:"NUMBER", description: "Restore volume (0-100)"]]
        command "playAnnouncementAll",	[[name:"Text*", type:"STRING", description:"Text to play"], 
										 [name:"Title*", type:"STRING", description: "Title to display on Echo Show devices"]]
		command "playTextAndRestore", 	[[name:"Text*", type:"STRING", description:"Text to play"]]
        command "playTrackAndRestore", 	[[name:"Track URI*", type:"STRING", description:"URI/URL of track to play"]]
        command "setVolume", 			[[name:"Volume Level*", type:"NUMBER", description: "Volume level (0-100)"]]
		command "setVolumeSpeakAndRestore", 
										[[name:"Volume Level*", type:"NUMBER", description:"Volume level (0-100)"],
										 [name:"Text*", type:"STRING", description:"Text to speak"],
										 [name:"Restore Volume Level",type:"NUMBER", description: "Restore volume (0-100)"]]										 
        command "setVolumeAndSpeak", 	[[name:"Volume Level*", type:"NUMBER", description:"Volume level (0-100)"], 
										 [name:"Text*", type:"STRING", description:"Text to speak"]]
		command "sendFollowMeSpeaker", 	[[name:"Follow Me Request*", type:"JSON_OBJECT", description:"JSON-encoded command string (see source)"]]
        
        command "sendQueue", ["string", "string", "string"]
		
    	attribute "whatDidISay", "string"
		attribute "latestMessage", "string"
        attribute "latestMessageDateTime", "string"
		attribute "speakerStatus1", "string"
		attribute "speakerStatus2", "string"
		attribute "speakerStatus3", "string"
		attribute "speakerStatus4", "string"
        
        attribute "queue1", "string"
        attribute "queue2", "string"
        attribute "queue3", "string"
        attribute "queue4", "string"
        attribute "queue5", "string"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "15")
			input("numOfLines", "number", title: "How many lines to display (from 1 to 10 only)", required:true, defaultValue: 5)
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
			input("clearData", "bool", title: "Reset All Data", required: false, defaultValue: false)
			input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

// Queue's for Home Tracker
def sendQueue(ps, theMessage, duration) {
    log.info "Follow Me - NEW Home Tracker - ps: ${ps} - duration: ${duration} - theMessage: ${theMessage}"
}

// -- code by @storageanarchy - Thank you for showing me how to pass the variables!
String composeMessageMap(method, message, priority=null, speakLevel=null, returnLevel=null, title='') {
    return JsonOutput.toJson([method: method as String, message: message as String, priority: priority as String, speakLevel: speakLevel, returnLevel: returnLevel, title: title as String])
}

def playAnnouncement(String message, volume=null, restoreVolume=null) {
    if(logEnable) log.debug "In playAnnouncement"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', state.speechReceivedFULL, 'N:X', volume, restoreVolume)
    sendEvent(name: "latestMessage", value: theMessage)
}

def playAnnouncement(String message, String title, volume=null, restoreVolume=null) {
    if(logEnable) log.debug "In playAnnouncement"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', state.speechReceivedFULL, 'N:X', volume, restoreVolume, title)
    sendEvent(name: "latestMessage", value: theMessage)
}

def playAnnouncementAll(String message, title=null) {
    if(logEnable) log.debug "In playAnnouncementAll"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncementAll', state.speechReceivedFULL, 'N:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def deviceNotification(message) {
    if(logEnable) log.debug "In deviceNotification"
	speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('deviceNotification', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def playText(message) {
    if(logEnable) log.debug "In playText"
	speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playText', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def playTextAndRestore(message) {
    if(logEnable) log.debug "In playTextAndRestore"
	//state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTextAndRestore', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def playTrack(message) {
    if(logEnable) log.debug "In playTrack"
    theMessage = composeMessageMap('playTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def playTrackAndRestore(message) {
    if(logEnable) log.debug "In playTrackAndRestore"
	//NB - Maybe shouldn't strip the URL encoding, as this is supposed to be a URL
	state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTrackAndRestore', state.speechReceivedFULL, 'X:0')
    sendEvent(name: "latestMessage", value: theMessage)
}

def restoreTrack(message) {
    if(logEnable) log.debug "In restoreTrack"
    theMessage = composeMessageMap('restoreTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def resumeTrack(message) {
    if(logEnable) log.debug "In resumeTrack"
    theMessage = composeMessageMap('resumeTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def setTrack(message) {
    if(logEnable) log.debug "In setTrack"
    theMessage = composeMessageMap('setTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage)
}

def setVolume(volume) {
    if(logEnable) log.debug "In setVolume"
    theMessage = composeMessageMap('setVolume', '', 'X:X', volume, null, null)
    sendEvent(name: "latestMessage", value: theMessage)   
}

def setVolumeSpeakAndRestore(volume, message, restoreVolume) {
    if(logEnable) log.debug "In setVolumeSpeakAndRestore"
	speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', state.speechReceivedFULL, 'N:X', volume, restoreVolume)
    sendEvent(name: "latestMessage", value: theMessage)
}

def setVolumeAndSpeak(volume, message) {
    if(logEnable) log.debug "In setVolumeAndSpeak"
	speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', state.speechReceivedFULL, 'N:X', volume)
    sendEvent(name: "latestMessage", value: theMessage)
}

def speak(message) {    
    if(logEnable) log.debug "In speak - message: ${message}"
    priorityHandler(message)
    // returned priority,lastSpoken
	speechReceivedFULL = lastSpoken.replace("%20"," ").replace("%5B","[").replace("%5D","]")   
    theMessage = composeMessageMap('speak', speechReceivedFULL, priority)
    if(logEnable) log.debug "In speak - theMessage: ${theMessage}"
    sendEvent(name: "latestMessage", value: theMessage)
    //sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
    latestMessageDate()
    populateMap(lastSpoken)
}

def priorityHandler(message) { 
	if(logEnable) log.debug "In priorityHandler - message: ${message}"

	if(message.contains("]")) {
		def (prior, msgA) = message.split(']')
		priority = prior.drop(1)
		lastSpoken = msgA
    } else {
        priority = "X:X"
        lastSpoken = message
	}
 
	if(logEnable) log.debug "In priorityHandler - priority: ${priority} - lastSpoken: ${lastSpoken}"
    return [priority,lastSpoken]   
}

def populateMap(speech) {
	if(logEnable) log.debug "In populateMap - Received new Speech! ${speech}"
    speechReceived = speech.take(70)
    
	if(speechReceived.contains("]")) {
		def (priority, msgA) = speechReceived.split(']')
		priority = priority.drop(1)
		speech = msgA
	} else{
		speech = speechReceived
        priority = "X:X"
	}
	
	if((priority.toUpperCase().contains("L")) || (priority.toUpperCase().contains("N")) || (priority.toUpperCase().contains("H"))) {
		if(priority.toUpperCase().contains("L")) { lastSpoken = "<font color='yellow'>${speech}</font>" }
		if(priority.toUpperCase().contains("N")) { lastSpoken = "${speech}" }
		if(priority.toUpperCase().contains("H")) { lastSpoken = "<font color='red'>${speech}</font>" }
	} else {
		lastSpoken = "${speech}"
	}
	
    if(logEnable) log.debug "In populateMap - lastSpoken: ${lastSpoken}"
    
    try {
        if(state.list1 == null) state.list1 = []
        
        getDateTime()
        last = "${newdate} - ${lastSpoken}"
        state.list1.add(0,last)  

        if(state.list1) {
            listSize1 = state.list1.size()
        } else {
            listSize1 = 0
        }
            
        if(listSize1 > 10) state.list1.removeAt(10)

        String result1 = state.list1.join(";")
        def lines1 = result1.split(";")
    
        if(logEnable) log.debug "In makeList - All - listSize1: ${listSize1}"
        theData1 = "<table><tr><td align='left'><div style='font-size:${fontSize}px'>"
        if(listSize1 >= 1) theData1 += "${lines1[0]}<br>"
        if(listSize1 >= 2) theData1 += "${lines1[1]}<br>"
        if(listSize1 >= 3) theData1 += "${lines1[2]}<br>"
        if(listSize1 >= 4) theData1 += "${lines1[3]}<br>"
        if(listSize1 >= 5) theData1 += "${lines1[4]}<br>"
        if(listSize1 >= 6) theData1 += "${lines1[5]}<br>"
        if(listSize1 >= 7) theData1 += "${lines1[6]}<br>"
        if(listSize1 >= 8) theData1 += "${lines1[7]}<br>"
        if(listSize1 >= 9) theData1 += "${lines1[8]}<br>"
        if(listSize1 >= 10) theData1 += "${lines1[9]}<br>"
        theData1 += "</div></td></tr></table>"
            
        dataCharCount1 = theData1.length()
	    if(dataCharCount1 <= 1024) {
	        if(logEnable) log.debug "What did I Say Attribute - dataPoints1 - ${dataCharCount1} Characters"
	    } else {
            theData1 = "Too many characters to display on Dashboard (${dataCharCount1})"
	    }
        
	    sendEvent(name: "whatDidISay", value: theData1)
        sendEvent(name: "whatDidISayCount", value: dataCharCount1)
    } catch(e) {
        log.error "Follow Me Driver - ${e}"  
    }
}

def installed(){
    log.info "Follow Me Driver has been Installed"
    clearSpeechData()
}

def updated() {
    log.info "Follow Me Driver has been Updated"
    if (clearData) runIn(2,clearSpeechData)
}

def initialize() {
    log.info "In initialize"
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newdate=date.format("MM-d HH:mm")
	if(hourType == true) newdate=date.format("MM-d hh:mm")
    return newdate
}

def latestMessageDate() {
    def date = new Date()
    latestMessageDateTime = date
    sendEvent(name: "latestMessageDateTime", value: date)
}

def clearDataOff(){
    log.info "Follow Me Driver has cleared the data"
    device.updateSetting("clearData",[value:"false",type:"bool"])
}

def clearSpeechData(){
	if(logEnable) log.debug "Follow Me Driver - clearing the data"
    state.list1 = []
	
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
	sendEvent(name: "speakerStatus1", value: state.sMap1S)
	sendEvent(name: "speakerStatus2", value: state.sMap2S)
	sendEvent(name: "speakerStatus3", value: state.sMap3S)
	sendEvent(name: "speakerStatus4", value: state.sMap4S)
	
	state.speechTop = "Waiting for Data..."
	sendEvent(name: "whatDidISay", value: state.speechTop)
	if (clearData) runIn(2,clearDataOff)
}	

def sendFollowMeSpeaker(status) {
//	if(logEnable) log.debug "In sendFollowMeSpeaker - Received new speaker status - ${status}"
	def (sName, sStatus) = status.split(':')
//	if(logEnable) log.debug "In sendFollowMeSpeaker - sName: ${sName} - sStatus: ${sStatus}"
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
//		if(logEnable) log.debug "In sendFollowMeSpeaker - Building Speaker Table with ${it.key} count: ${state.count}"
		if((state.count >= 1) && (state.count <= 5)) {
			if(status == "true") state.sMap1S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: green;font-size:${fontSize}px;'>Active</div></td></tr>"
			if(status == "false") state.sMap1S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: red;font-size:${fontSize}px;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap1S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: blue;font-size:${fontSize}px;'>Speaking</div></td></tr>"
		}
		if((state.count >= 6) && (state.count <= 10)) {
			if(status == "true") state.sMap2S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: green;font-size:${fontSize}px;'>Active</div></td></tr>"
			if(status == "false") state.sMap2S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: red;font-size:${fontSize}px;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap2S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: blue;font-size:${fontSize}px;'>Speaking</div></td></tr>"
		}
		if((state.count >= 11) && (state.count <= 15)) {
			if(status == "true") state.sMap3S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: green;font-size:${fontSize}px;'>Active</div></td></tr>"
			if(status == "false") state.sMap3S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: red;font-size:${fontSize}px;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap3S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: blue;font-size:${fontSize}px;'>Speaking</div></td></tr>"
		}
		if((state.count >= 16) && (state.count <= 20)) {
			if(status == "true") state.sMap4S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: green;font-size:${fontSize}px;'>Active</div></td></tr>"
			if(status == "false") state.sMap4S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: red;font-size:${fontSize}px;'>Inactive</div></td></tr>"
			if(status == "speaking") state.sMap4S += "<tr><td align='left'><div style='font-size:${fontSize}px'>${it.key}</div></td><td><div style='color: blue;font-size:${fontSize}px;'>Speaking</div></td></tr>"
		}
	}
	state.sMap1S += "</table>"
	state.sMap2S += "</table>"
	state.sMap3S += "</table>"
	state.sMap4S += "</table>"
	
	sendEvent(name: "speakerStatus1", value: state.sMap1S)
	sendEvent(name: "speakerStatus2", value: state.sMap2S)
	sendEvent(name: "speakerStatus3", value: state.sMap3S)
	sendEvent(name: "speakerStatus4", value: state.sMap4S)
}
