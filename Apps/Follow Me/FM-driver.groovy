/**
 *  ****************  Follow Me Driver  ****************
 *
 *  Design Usage:
 *  This driver formats Speech data to be displayed on Hubitat's Dashboards and also acts as a proxy speaker to 'Follow Me'.
 *
 *  Copyright 2019-2021 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  2.3.2 - 11/07/21 - I think I got it!
 *  2.3.1 - 11/07/21 - Trying again.
 *  2.3.0 - 11/07/21 - Trying to fix something I can't reproduce.
 *  2.2.9 - 10/21/21 - Adjusted speak() to reflect the new parameters
 *  2.2.8 - 07/29/21 - Added code for lastActive Speaker
 *  2.2.7 - 04/28/21 - Added 'Replay'
 *  2.2.6 - 01/12/21 - Adjustments to Priority processing
 *  2.2.5 - 11/27/20 - Adjustments
 *  2.2.4 - 07/21/20 - Adjustments
 *  2.2.3 - 07/10/20 - Added user selectable Priority colors
 *  2.2.2 - 07/06/20 - Added Priority Speaker options
 *  2.2.1 - 06/22/20 - Adjustments
 *  ---
 *  1.0.0 - 01/27/19 - Initial release
 */

import groovy.json.*

metadata {
    definition (name: "Follow Me Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Follow%20Me/FM-driver.groovy") {
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
        
        command "sendPush", ["string"]
        command "latestMessageFrom", ["string"]
        command "replayMessage", ["string"]

        attribute "bpt-whatDidISay", "string"
        attribute "whatDidISayCount", "string"
        attribute "latestMessage", "string"
        attribute "rawMessage", "string"
        attribute "latestMessageDateTime", "string"
        attribute "latestMessageFrom", "string"
        attribute "bpt-speakerStatus1", "string"
        attribute "bpt-speakerStatus2", "string"
        attribute "bpt-speakerStatus3", "string"
        attribute "bpt-lastActiveSpeaker", "string"

        attribute "bpt-queue1", "string"
        attribute "bpt-queue2", "string"
        attribute "bpt-queue3", "string"
        attribute "bpt-queue4", "string"
        attribute "bpt-queue5", "string"
        
        attribute "pushMessage", "string"
    }
    
    preferences() {    	
        section(){
            input "fontSize", "text", title: "Font Size", required: true, defaultValue: "15"
            input "fontFamily", "text", title: "Font Family (optional)<br>ie. Lucida Sans Typewriter", required: false
            input "hourType", "bool", title: "Time Selection<br>(Off for 24h, On for 12h)", required: false, defaultValue: false
            
            input "pLowColor", "text", title: "Color for Priority - Low<br>ie. Red,Yellow,Orange,Blue,etc.", required: false, defaultValue: "yellow"
            input "pNormalColor", "text", title: "Color for Priority - Normal<br>.", required: false, defaultValue: "black"
            input "pHighColor", "text", title: "Color for Priority - High<br>.", required: false, defaultValue: "red"
            
            input "clearData", "bool", title: "Reset All Data", required: false, defaultValue: false
            input "logEnable", "bool", title: "Enable logging", required: false, defaultValue: false
        }
    }
}

// Queue's for Home Tracker
def sendQueue(ps, theMessage, duration) {
    if(logEnable) log.info "Follow Me - NEW Home Tracker - ps: ${ps} - duration: ${duration} - theMessage: ${theMessage}"
}

def sendPush(data) {
    if(logEnable) log.debug "In sendPush - data: ${data}"
    sendEvent(name: "pushMessage", value: data) 
}

// -- code by @storageanarchy - Thank you for showing me how to pass the variables!
String composeMessageMap(method, message, priority=null, speakLevel=null, returnLevel=null, title='') {
    return JsonOutput.toJson([method: method as String, message: message as String, priority: priority as String, speakLevel: speakLevel, returnLevel: returnLevel, title: title as String])
}

def playAnnouncement(String message, volume=null, restoreVolume=null) {
    if(logEnable) log.debug "In playAnnouncement"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', speechReceivedFULL, 'N:X', volume, restoreVolume)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def playAnnouncement(String message, String title, volume=null, restoreVolume=null) {
    if(logEnable) log.debug "In playAnnouncement"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', speechReceivedFULL, 'N:X', volume, restoreVolume, title)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def playAnnouncementAll(String message, title=null) {
    if(logEnable) log.debug "In playAnnouncementAll"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncementAll', speechReceivedFULL, 'N:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def deviceNotification(message) {
    if(logEnable) log.debug "In deviceNotification"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('deviceNotification', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def playText(message) {
    if(logEnable) log.debug "In playText"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playText', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def playTextAndRestore(message, returnLevel) {
    if(logEnable) log.debug "In playTextAndRestore"
    speechReceivedFULL = message
    theMessage = composeMessageMap('playTextAndRestore', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def playTrack(message) {
    if(logEnable) log.debug "In playTrack"
    speechReceivedFULL = message
    theMessage = composeMessageMap('playTrack', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def playTrackAndRestore(message, returnLevel) {
    if(logEnable) log.debug "In playTrackAndRestore"
    //NB - Maybe shouldn't strip the URL encoding, as this is supposed to be a URL
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTrackAndRestore', speechReceivedFULL, 'X:0')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def restoreTrack(message) {
    if(logEnable) log.debug "In restoreTrack"
    speechReceivedFULL = message
    theMessage = composeMessageMap('restoreTrack', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def resumeTrack(message) {
    if(logEnable) log.debug "In resumeTrack"
    speechReceivedFULL = message
    theMessage = composeMessageMap('resumeTrack', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def setTrack(message) {
    if(logEnable) log.debug "In setTrack"
    speechReceivedFULL = message
    theMessage = composeMessageMap('setTrack', speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def setVolume(volume) {
    if(logEnable) log.debug "In setVolume"
    theMessage = composeMessageMap('setVolume', '', 'X:X', volume, null, null)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)   
    sendEvent(name: "rawMessage", value: message)
}

def setVolumeSpeakAndRestore(volume, message, restoreVolume) {
    if(logEnable) log.debug "In setVolumeSpeakAndRestore"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', speechReceivedFULL, 'N:X', volume, restoreVolume)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def setVolumeAndSpeak(volume, message) {
    if(logEnable) log.debug "In setVolumeAndSpeak"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeAndSpeak', speechReceivedFULL, 'N:X', volume)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
    sendEvent(name: "rawMessage", value: message)
}

def speak(message, option) {
    if(logEnable) log.debug "In speak - message: ${message} - option: ${option}"
    if(message) {
        priorityHandler(message)
        // returns priority,lastSpoken
        speechReceivedFULL = lastSpoken.replace("%20"," ").replace("%5B","[").replace("%5D","]")   
        theMessage = composeMessageMap('speak', speechReceivedFULL, priority)
        if(logEnable) log.debug "In speak - theMessage: ${theMessage}"
        sendEvent(name: "latestMessage", value: theMessage, isStateChange:true)
        sendEvent(name: "rawMessage", value: message)
        latestMessageDate()
        populateMap(priority,lastSpoken)
    } else {
        lmf = device.currentValue("latestMessageFrom")
        log.warn "Follow Me - speak - Something went Wrong, No message received. (${lmf})"
    }
}

def replayMessage(data) {
    if(logEnable) log.debug "In replay"
    def replayMessage = device.currentValue('rawMessage')
    speak(replayMessage)
}

def priorityHandler(message) { 
    if(logEnable) log.debug "In priorityHandler - message: ${message}"

    if(message.startsWith("[")) {
		def (prior, msgA) = message.split(']')
		priority = prior.drop(1)
		lastSpoken = msgA
    } else {
        priority = "X:X:X"
        lastSpoken = message
    }

    if(logEnable) log.debug "In priorityHandler - priority: ${priority} - lastSpoken: ${lastSpoken}"
    return [priority,lastSpoken]   
}

def populateMap(priority,speech) {
    if(logEnable) log.debug "In populateMap - Received new Speech! ${speech}"
    speechReceived = speech.take(80)

    def thePriority = priority.split(":")
    
    theValueCount = thePriority.size()
    if(logEnable) log.debug "In populateMap - theValueCount: ${theValueCount}"
    
    try {       
        if(theValueCount >= 1) priorityValue = thePriority[0]
        if(theValueCount >= 2) priorityVoice = thePriority[1]
        if(theValueCount >= 3) prioritySpeaker = thePriority[2]
        
        if(priorityValue == null || priorityValue == "0") priorityValue = "X"
        if(priorityVoice == null || priorityVoice == "0") priorityVoice = "X"
        if(prioritySpeaker == null || prioritySpeaker == "0") prioritySpeaker = "X"
        
        if(logEnable) log.debug "In populateMap - priorityValue: ${priorityValue} - priorityVoice: ${priorityVoice} - prioritySpeaker: ${prioritySpeaker}"
    } catch (e) {
        log.warn "Follow Me Driver - Something went wrong with your speech priority formatting. Please check your syntax. ie. [N:1:0]"
        if(logEnable) log.error "In populateMap - ${e}"
        priorityValue = "X"
        priorityVoice = "X"
        prioritySpeaker = "X"
    }

    if((priorityValue.toUpperCase().contains("L")) || (priorityValue.toUpperCase().contains("N")) || (priorityValue.toUpperCase().contains("H"))) {
        if(priorityValue.toUpperCase().contains("L")) { lastSpoken = "<span style='color:${pLowColor}'>${speech}</span>" }
        if(priorityValue.toUpperCase().contains("N")) { lastSpoken = "<span style='color:${pNormalColor}'>${speech}</span>" }
        if(priorityValue.toUpperCase().contains("H")) { lastSpoken = "<span style='color:${pHighColor}'>${speech}</span>" }
        if(logEnable) log.debug "In populateMap - Contains(L,N,H) - lastSpoken: ${lastSpoken}"
    } else {
        lastSpoken = "${speech}"
        if(logEnable) log.debug "In populateMap - Does NOT Contain(L,N,H) - lastSpoken: ${lastSpoken}"
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

        int intNumOfLines = 10
        if (listSize1 > intNumOfLines) state.list1.removeAt(intNumOfLines)
        String result1 = state.list1.join(";")
        def lines1 = result1.split(";")

        if(logEnable) log.debug "In makeList - All - listSize1: ${listSize1} - intNumOfLines: ${intNumOfLines}"

        if(fontFamily) {
            theData1 = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px;font-family:${fontFamily}'><tr><td>"
        } else {
            theData1 = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px'><tr><td>"
        }
        for (i=0; i<intNumOfLines && i<listSize1 && theData1.length() < 927;i++)
        theData1 += "${lines1[i]}<br>"

        theData1 += "</table></div>"
        if(logEnable) log.debug "theData1 - ${theData1.replace("<","!")}"       

        dataCharCount1 = theData1.length()
        if(dataCharCount1 <= 1024) {
            if(logEnable) log.debug "What did I Say Attribute - theData1 - ${dataCharCount1} Characters"
        } else {
            theData1 = "Too many characters to display on Dashboard (${dataCharCount1})"
        }

        sendEvent(name: "bpt-whatDidISay", value: theData1)
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
    cleanUp()
    if(clearData) runIn(2,clearSpeechData)
}

def initialize() {
    log.info "In initialize"
}

def getDateTime() {
    def date = new Date()
    if(hourType == false) newdate=date.format("MM-d HH:mm")
    if(hourType == true) newdate=date.format("MM-d hh:mm a")
    return newdate
}

def latestMessageDate() {
    def date = new Date()
    latestMessageDateTime = date
    sendEvent(name: "latestMessageDateTime", value: date)
}

def latestMessageFrom(data) {
    sendEvent(name: "latestMessageFrom", value: data)
}

def clearDataOff(){
    log.info "Follow Me Driver has cleared the data"
    device.updateSetting("clearData",[value:"false",type:"bool"])
}

def clearSpeechData(){
    if(logEnable) log.debug "Follow Me Driver - clearing the data"
    state.list1 = []
    sMap1S = "Waiting for Data"
    sMap2S = "Waiting for Data"
    sMap3S = "Waiting for Data"
    sendEvent(name: "bpt-speakerStatus1", value: sMap1S)
    sendEvent(name: "bpt-speakerStatus2", value: sMap2S)
    sendEvent(name: "bpt-speakerStatus3", value: sMap3S)
    speechTop = "Waiting for Data..."
    state.speakerMap = null
    sendEvent(name: "whatDidISay", value: speechTop)
    if (clearData) runIn(2,clearDataOff)
}

def sendFollowMeSpeaker(status) {
    if(logEnable) log.debug "In sendFollowMeSpeaker - status: ${status}"
    def theData = status.split(':')    
    try {
        sName = theData[0]
        sStatus = theData[1]
        sID = theData[2]
        sLastAct = theData[3]
    } catch(e) {
        log.warn "Follow Me Driver - In sendFollowMeSpeaker - Something isn't setup right. Post a screenshot of the DEBUG LOGS (not just the error) of the Device the created this error AND the Follow Me app."
    }
    if(logEnable) log.debug "In sendFollowMeSpeaker - sName: ${sName} - sStatus: ${sStatus} - sID: ${sID} - sLastAct: ${sLastAct}"
    if(state.speakerMap == null) state.speakerMap = [:]
    ndata = "${sStatus}:${sID}:${sLastAct}"
    state.speakerMap.put(sName, ndata)
    if(sLastAct == "true") {
        if(logEnable) log.debug "In sendFollowMeSpeaker - Using LastActive - Going to driverToChildApp - status: ${status}"
        parent.driverToChildApp(status)
        sendEvent(name: "bpt-lastActiveSpeaker", value: sName)
    }
    runIn(1, makeListHandler)
}

def makeListHandler() {
    def tblhead = "<div style='overflow:auto;height:90%'><table width=100% style='line-height:1.00;font-size:${fontSize}px;text-align:left'>"
    def line = "" 
    def tbl = tblhead
    def tileCount = 1
    theDevices = state.speakerMap.sort { a, b -> a.key <=> b.key }

    theDevices.each { it ->
        deviceName = it.key
        deviceData = it.value
        def theData = deviceData.split(":")
        try {
            theStatus = theData[0]
            appID = theData[1]
            theLastAct = theData[2]
        } catch(e) {
            log.info "Follow Me Driver - In makeListHandler - If no other errors are shown, this will work itself out."
        }
        if(theLastAct == "true") {
            line = "<tr><td>${it.key}<td style='color:orange;font-size:${fontSize}px'>lastActive"
        } else {
            if(theStatus == "true") line = "<tr><td>${it.key}<td style='color:green;font-size:${fontSize}px'>Active"
            if(theStatus == "false") line = "<tr><td>${it.key}<td style='color:red;font-size:${fontSize}px'>Inactive"
            if(theStatus == "speaking") line = "<tr><td>${it.key}<td style='color:blue;font-size:${fontSize}px'>Speaking"
        }
        totalLength = tbl.length() + line.length()
        //if(logEnable) log.debug "In makeListHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
        if (totalLength < 1009) {
            tbl += line
        } else {
            tbl += "</table></div>"
            //if(logEnable) log.debug "${tbl}"
            tbl = tblhead + line
            if(tileCount == 1) sendEvent(name: "bpt-speakerStatus1", value: tbl)
            if(tileCount == 2) sendEvent(name: "bpt-speakerStatus2", value: tbl)
            if(tileCount == 3) sendEvent(name: "bpt-speakerStatus3", value: tbl)
            tileCount = tileCount + 1
        }
    }

    if (tbl != tblhead) {
        tbl += "</table></div>"
        //if(logEnable) log.debug "${tbl}"
        if(tileCount == 1) sendEvent(name: "bpt-speakerStatus1", value: tbl)
        if(tileCount == 2) sendEvent(name: "bpt-speakerStatus2", value: tbl)
        if(tileCount == 3) sendEvent(name: "bpt-speakerStatus3", value: tbl)
        tileCount = tileCount + 1
    }

    for(x=tileCount;x<4;x++) {
        if(tileCount == 1) sendEvent(name: "bpt-speakerStatus1", value: "No Data")
        if(tileCount == 2) sendEvent(name: "bpt-speakerStatus2", value: "No Data")
        if(tileCount == 3) sendEvent(name: "bpt-speakerStatus3", value: "No Data")
    }
}

private cleanUp() {
    // Cleaning up the driver from previous versions
    state.remove("sMap1S")
    state.remove("sMap2S")
    state.remove("sMap3S")
    state.remove("sMap4S")
    state.remove("speechTop")
    state.remove("speakerMapS")
    state.remove("count")
    
    sendEvent(name: "speakerStatus1", value: "No longer used")
    sendEvent(name: "speakerStatus2", value: "No longer used")
    sendEvent(name: "speakerStatus3", value: "No longer used")
    sendEvent(name: "whatDidISay", value: "No longer used")
}
