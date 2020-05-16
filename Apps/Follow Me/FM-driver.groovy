/**
 *  ****************  Follow Me Driver  ****************
 *
 *  Design Usage:
 *  This driver formats Speech data to be displayed on Hubitat's Dashboards and also acts as a proxy speaker to 'Follow Me'.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  2.1.5 - 05/16/20 - Minor change
 *  2.1.4 - 05/12/20 - All tiles now scroll
 *  2.1.3 - 05/11/20 - Added more code traps
 *  2.1.2 - 04/21/20 - Code cleanup, Added optional Text formatting, modified whatDidISay list code by @alan564923 (thank you!)
 *  2.1.1 - 03/18/20 - Fixed message priority features
 *  2.1.0 - 11/14/19 - Name changed to match Follow Me. Major rework. Changes to work with the updated Follow Me (V2.0.5+)
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

        attribute "whatDidISay", "string"
        attribute "whatDidISayCount", "string"
        attribute "latestMessage", "string"
        attribute "latestMessageDateTime", "string"
        attribute "speakerStatus1", "string"
        attribute "speakerStatus2", "string"
        attribute "speakerStatus3", "string"

        attribute "queue1", "string"
        attribute "queue2", "string"
        attribute "queue3", "string"
        attribute "queue4", "string"
        attribute "queue5", "string"
    }
    preferences() {    	
        section(){
            input("fontSize", "text", title: "Font Size", required: true, defaultValue: "15")
            input("fontFamily", "text", title: "Font Family (optional)<br>ie. Lucida Sans Typewriter", required: false)
            input("hourType", "bool", title: "Time Selection<br>(Off for 24h, On for 12h)", required: false, defaultValue: false)
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
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def playAnnouncement(String message, String title, volume=null, restoreVolume=null) {
    if(logEnable) log.debug "In playAnnouncement"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncement', state.speechReceivedFULL, 'N:X', volume, restoreVolume, title)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def playAnnouncementAll(String message, title=null) {
    if(logEnable) log.debug "In playAnnouncementAll"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playAnnouncementAll', state.speechReceivedFULL, 'N:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def deviceNotification(message) {
    if(logEnable) log.debug "In deviceNotification"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('deviceNotification', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def playText(message) {
    if(logEnable) log.debug "In playText"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playText', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def playTextAndRestore(message) {
    if(logEnable) log.debug "In playTextAndRestore"
    //state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTextAndRestore', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def playTrack(message) {
    if(logEnable) log.debug "In playTrack"
    theMessage = composeMessageMap('playTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def playTrackAndRestore(message) {
    if(logEnable) log.debug "In playTrackAndRestore"
    //NB - Maybe shouldn't strip the URL encoding, as this is supposed to be a URL
    state.speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('playTrackAndRestore', state.speechReceivedFULL, 'X:0')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def restoreTrack(message) {
    if(logEnable) log.debug "In restoreTrack"
    theMessage = composeMessageMap('restoreTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def resumeTrack(message) {
    if(logEnable) log.debug "In resumeTrack"
    theMessage = composeMessageMap('resumeTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def setTrack(message) {
    if(logEnable) log.debug "In setTrack"
    theMessage = composeMessageMap('setTrack', state.speechReceivedFULL, 'X:X')
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def setVolume(volume) {
    if(logEnable) log.debug "In setVolume"
    theMessage = composeMessageMap('setVolume', '', 'X:X', volume, null, null)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)   
}

def setVolumeSpeakAndRestore(volume, message, restoreVolume) {
    if(logEnable) log.debug "In setVolumeSpeakAndRestore"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', state.speechReceivedFULL, 'N:X', volume, restoreVolume)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def setVolumeAndSpeak(volume, message) {
    if(logEnable) log.debug "In setVolumeAndSpeak"
    speechReceivedFULL = message.replace("%20"," ").replace("%5B","[").replace("%5D","]")
    theMessage = composeMessageMap('setVolumeSpeakAndRestore', state.speechReceivedFULL, 'N:X', volume)
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
}

def speak(message) {    
    if(logEnable) log.debug "In speak - message: ${message}"
    priorityHandler(message)
    // returned priority,lastSpoken
    speechReceivedFULL = lastSpoken.replace("%20"," ").replace("%5B","[").replace("%5D","]")   
    theMessage = composeMessageMap('speak', speechReceivedFULL, priority)
    if(logEnable) log.debug "In speak - theMessage: ${theMessage}"
    sendEvent(name: "latestMessage", value: theMessage, isStateChange: true)
    latestMessageDate()
    populateMap(priority,lastSpoken)
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

def populateMap(priority,speech) {
    if(logEnable) log.debug "In populateMap - Received new Speech! ${speech}"
    speechReceived = speech.take(80)

    try {
        def thePriority = priority.split(":")
        priorityValue = thePriority[0]
        priorityVoice = thePriority[1]
        if(logEnable) log.debug "In populateMap - priorityValue: ${priorityValue} - priorityVoice: ${priorityVoice}"
    } catch (e) {
        log.warn "Follow Me Driver - Something went wrong with your speech priority formatting. Please check your syntax. ie. [N:1]"
        if(logEnable) log.error "In populateMap - ${e}"
        priorityValue = "X"
        priorityVoice = "X"
    }

    if((priorityValue.toUpperCase().contains("L")) || (priorityValue.toUpperCase().contains("N")) || (priorityValue.toUpperCase().contains("H"))) {
        if(priorityValue.toUpperCase().contains("L")) { lastSpoken = "<span style='color:yellow'>${speech}</span>" }
        if(priorityValue.toUpperCase().contains("N")) { lastSpoken = "${speech}" }
        if(priorityValue.toUpperCase().contains("H")) { lastSpoken = "<span style='color:red'>${speech}</span>" }
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
    sMap4S = "Waiting for Data"
    sendEvent(name: "speakerStatus1", value: sMap1S)
    sendEvent(name: "speakerStatus2", value: sMap2S)
    sendEvent(name: "speakerStatus3", value: sMap3S)
    sendEvent(name: "speakerStatus4", value: sMap4S)

    speechTop = "Waiting for Data..."
    sendEvent(name: "whatDidISay", value: speechTop)
    if (clearData) runIn(2,clearDataOff)
}	

def sendFollowMeSpeaker(status) {
    def (sName, sStatus) = status.split(':')
    if(sName == null) sName = "blank"
    if(sStatus == null) sStatus = "not found"
    if(logEnable) log.debug "In sendFollowMeSpeaker - sName: ${sName} - sStatus: ${sStatus}"
    if(state.speakerMap == null) state.speakcounterMap = [:]
    state.speakerMap.put(sName, sStatus)

    def tblhead = "<div style='overflow:auto;height:90%'><table width=100% style='line-height:1.00;font-size:${fontSize}px;text-align:left'>"
    def line = "" 
    def tbl = tblhead
    def tileCount = 1
    theDevices = state.speakerMap.sort { a, b -> a.key <=> b.key }

    theDevices.each { it ->
        status = it.value

        if(status == "true") line = "<tr><td>${it.key}<td style='color:green;font-size:${fontSize}px'>Active"
        if(status == "false") line = "<tr><td>${it.key}<td style='color:red;font-size:${fontSize}px'>Inactive"
        if(status == "speaking") line = "<tr><td>${it.key}<td style='color:blue;font-size:${fontSize}px'>Speaking"

        totalLength = tbl.length() + line.length()
        if(logEnable) log.debug "In sendFollowMeSpeaker - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
        if (totalLength < 1009) {
            tbl += line
        } else {
            tbl += "</table></div>"
            if(logEnable) log.debug "${tbl}"
            tbl = tblhead + line
            if(tileCount == 1) sendEvent(name: "speakerStatus1", value: tbl)
            if(tileCount == 2) sendEvent(name: "speakerStatus2", value: tbl)
            if(tileCount == 3) sendEvent(name: "speakerStatus3", value: tbl)
            tileCount = tileCount + 1
        }
    }

    if (tbl != tblhead) {
        tbl += "</table></div>"
        if(logEnable) log.debug "${tbl}"
        if(tileCount == 1) sendEvent(name: "speakerStatus1", value: tbl)
        if(tileCount == 2) sendEvent(name: "speakerStatus2", value: tbl)
        if(tileCount == 3) sendEvent(name: "speakerStatus3", value: tbl)
        tileCount = tileCount + 1
    }

    for(x=tileCount;x<4;x++) {
        if(tileCount == 1) sendEvent(name: "speakerStatus1", value: "No Data")
        if(tileCount == 2) sendEvent(name: "speakerStatus2", value: "No Data")
        if(tileCount == 3) sendEvent(name: "speakerStatus3", value: "No Data")
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
}
