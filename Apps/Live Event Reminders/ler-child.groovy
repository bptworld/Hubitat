/**
 *  **************** Life Event Reminders App  ****************
 *
 *  Design Usage:
 *  Never miss an important Life Event again! Schedule reminders easily and locally.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  1.0.0 - 03/27/22 - Initial release.
 *
 */



def setVersion(){
    state.name = "Life Event Reminders Child"
	state.version = "1.0.0"
}

definition(
    name: "Life Event Reminders Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Never miss an important Life Event again! Schedule reminders easily and locally.",
    category: "Convenience",
	parent: "BPTWorld:Life Event Reminders",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page name: "pageConfig"
    page name: "speechPushOptions", title: "", install:false, uninstall:false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Never miss an important Life Event again! Schedule reminders easily and locally."
		}

        section(getFormat("header-green", "${getImage("Blank")}"+" Data Device")) {
            createDeviceSection("Life Event Reminders Driver")
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Add to Calendar")) {
            input "theTitle", "text", title: "Event Title", submitOnChange:true
            input "theDate", "date", title: "Event Date", width:6, submitOnChange:true
            input "theTime", "time", title: "Event Time", width:6, submitOnChange:true
            input "theText", "text", title: "Event Description", submitOnChange:true

            input "msgRepeat", "bool", title: "Repeat Notifications", description: "Repeat", submitOnChange:true
            if(msgRepeat) {
                input "msgRepeatMinutes", "number", title: "Repeat every XX minutes", submitOnChange:true, width:6
                input "msgRepeatMax", "number", title: "Max number of repeats", submitOnChange:true, width:6

                if(msgRepeatMinutes && msgRepeatMax) {
                    paragraph "<small>Message will repeat every ${msgRepeatMinutes} minutes until one of the contacts/switches changes state <b>OR</b> the Max number of repeats is reached (${msgRepeatMax})</small>"
                    repeatTimeSeconds = ((msgRepeatMinutes * 60) * msgRepeatMax)
                    int inputNow=repeatTimeSeconds
                    int nDayNow = inputNow / 86400
                    int nHrsNow = (inputNow % 86400 ) / 3600
                    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
                    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
                    paragraph "<small>In this case, it would take ${nHrsNow} Hours, ${nMinNow} Mins and ${nSecNow} Seconds to reach the max number of repeats (if nothing changes state)</small>"
                    paragraph "<hr>"
                }
            } else {
                app.removeSetting("msgRepeatMinutes")
                app.removeSetting("msgRepeatMax")
            }

            paragraph "<small>* Remember to click outside any field before clicking on a button.</small>"
            input "bCancel", "button", title: "Cancel", width: 3
            input "bAdd", "button", title: "Add/Edit", width: 3
            input "bDel", "button", title: "Delete", width: 3
            input "bClear", "button", title: "Clear All", width: 3

            paragraph "<hr>"
            if(state.calendarMap == null) {
                theMap = "No devices are setup"
            } else {
                if(logEnable) log.info "Making new calendarMap display"
                theMap = "<table width=100%><tr><td><b>Title</b><td><b>Date</b><td><b>Time</b><td><b>Description</b><td><b>Repeat<br>Every</b><td><b>Repeat<br>Max</b>"
                sortedMap = state.calendarMap.sort { a, b -> a.value <=> b.value }
                sortedMap.each { cm ->
                    theTitle = cm.key
                    (theDate, theTime1, theTime2, theDesc, theRepeatMinutes, theRepeatMax) = cm.value.split(";")
                    if(theRepeatMinutes == "null") theRepeatMinutes = "-"
                    if(theRepeatMax == "null") theRepeatMax = "-"
                    theMap += "<tr><td>$theTitle<td>$theDate<td>$theTime2<td>$theDesc<td>$theRepeatMinutes<td>$theRepeatMax"
                }
                theMap += "</table>"
            }
            paragraph "${theMap}"
            paragraph "<hr>"
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            if(useSpeech || sendPushMessage) {
                href "speechPushOptions", title:"${getImage("checkMarkGreen")} Speech/Push Options", description:"Click here for options"
            } else {
                href "speechPushOptions", title:"Speech/Push Options", description:"Click here for options"
            }
            input "theSwitches", "capability.switch", title: "Additional Switches to Turn On", required:false, multiple:true, submitOnChange:true
            paragraph "<small>* The switches selected here will automatically turn on with the Event trigger and then turn off when the Event has finished.</small>"
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
        }
		display2()
	}
}

def speechPushOptions(){
    dynamicPage(name: "speechPushOptions", title: "", install: false, uninstall:false){
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required:true, submitOnChange:true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
        }
        
        if(useSpeech || sendPushMessage) {
            wc = "<u>Optional wildcards:</u><br>"
            wc += "%title% - returns the Event Title<br>"
            wc += "%description% - returns the Event Description<br>"
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
                paragraph "${wc}"
                input "messages", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)", required:true, submitOnChange:true
                input "msgList", "bool", defaultValue: true, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
                if(msgList) {
                    def values = "${messages}".split(";")
                    listMap = ""
                    values.each { item -> listMap += "${item}<br>"}
                    paragraph "${listMap}"
                }
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Repeat Notifications Control")) {
                paragraph "If using the Repeat option with any Event Reminder, please use the ${dataDevice} device as a control switch. This switch will automatically turn on when the Event triggers and back off again when the Event has finished. If you want the Event to finish early, manually turn this switch off."
            }
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
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp) {
        log.info "${app.label} is Paused"
    } else {
        state.oldSwitchValue = null
        state.numOfRepeats = 1
        scheduleHandler()        
    }
}

def startTheProcess(data) {
    if(logEnable) log.debug "******************** Start - startTheProcess (${state.version}) ********************"
    if(logEnable) log.debug "In startTheProcess (${state.version}) - data: ${data}"
    state.calendarMap.each { cm ->
        theTitle = cm.key
        if(theTitle == data) {
            if(theSwitches) {
                theSwitches.each { it ->
                    it.on()
                }
            }
            (theDate, theTime1, theTime2, theDesc, theRepeatMinutes, theRepeatMax) = cm.value.split(";")
            if(useSpeech || sendPushMessage) messageHandler(theTitle, theDesc)
        }
    }
    
    if(msgRepeat) {
        if(logEnable) log.debug "In startTheProcess - Repeat - numOfRepeats: ${state.numOfRepeats} - msgRepeatMax: ${theRepeatMax}"
        repeat = false
        if(state.numOfRepeats == null) state.numOfRepeats = 1
        if(state.numOfRepeats < msgRepeatMax) {
            if(state.numOfRepeats == 1) dataDevice.on()
            repeat = dataDevice.currentValue("switch")
            if(repeat == "on") {
                if(logEnable) log.debug "In startTheProcess - repeat is ${repeat}"
                rTime = (theRepeatMinutes.toInteger() * 60)
                state.numOfRepeats += 1
                if(logEnable) log.debug "In startTheProcess - Repeat - Running again in ${theRepeatMinutes} minutes (${rTime})"
                runIn(rTime, startTheProcess, [data: theTitle]) 
            } else {
                state.oldSwitchValue = null
                state.numOfRepeats = 1
            }
        } else {
            dataDevice.off()
            if(theSwitches) {
                theSwitches.each { it ->
                    it.off()
                }
            }
            state.oldSwitchValue = null
            state.numOfRepeats = 1
        }
    } else {
        if(logEnable) log.debug "In startTheProcess - No repeat"
    }
    if(logEnable) log.debug "******************** End startTheProcess (${state.version}) ********************"
}

def messageHandler(theTitle, theDesc) {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    def mValues = "${messages}".split(";")
	mSize = mValues.size().toInteger()
    def randomKey = new Random().nextInt(mSize)
    state.message = mValues[randomKey]

    if(state.message.contains("%title%")) {state.message = state.message.replace('%title%', "${theTitle}")}
    if(state.message.contains("%description%")) {state.message = state.message.replace('%description%', "${theDesc}")}
    if(logEnable) log.debug "In messageHandler - message: ${state.message}"
    if(useSpeech) letsTalk(state.message)
    if(sendPushMessage) pushHandler(state.message)
}

def scheduleHandler() {
	if(logEnable) log.debug "In scheduleHandler (${state.version})"
    state.calendarMap.each { cm ->
        theTitle = cm.key
        (theDate, theTime1, theTime2, theDesc, theRepeatMinutes, theRepeatMax) = cm.value.split(";")
        (theYear, theMonth, theDay) = theDate.split("-")
        (theHour, theMin) = theTime1.split(":")

        theSchedule = "0 ${theMin} ${theHour} ${theDay} ${theMonth} ? *"
        if(logEnable) log.debug "In scheduleHandler - Setting schedule for ${theTitle}: 0 ${theMin} ${theHour} ${theDay} ${theMonth} ? *"
        schedule(theSchedule, startTheProcess, [data: theTitle])
    }
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    log.debug "theDate: ${theDate} -- theTime: ${theTime} -- theText: ${theText}"
    if(state.calendarMap == null) state.calendarMap = [:]

    if(buttonPressed == "bDel") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"       
        state.calendarMap.remove(theTitle)
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "bAdd") {
        
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        Date newDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", theTime)
        String timePart1 = newDate.format("HH:mm")      // 24 h
        String timePart2 = newDate.format("hh:mm a")    // 12 h
        if(logEnable) log.debug "In appButtonHandler - ${theDate} - ${timePart1} - ${timePart2} - ${theText} - ${msgRepeatMinutes} - ${msgRepeatMax}"   
        state.calendarMap.put(theTitle,"${theDate};${timePart1};${timePart2};${theText};${msgRepeatMinutes};${msgRepeatMax}")
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
        
    } else if(buttonPressed == "bCancel") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "bClear") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"       
        state.calendarMap = [:]
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    }
}

// ~~~~~ start include (2) BPTWorld.bpt-normalStuff ~~~~~
library ( // library marker BPTWorld.bpt-normalStuff, line 1
        base: "app", // library marker BPTWorld.bpt-normalStuff, line 2
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-normalStuff, line 3
        category: "Apps", // library marker BPTWorld.bpt-normalStuff, line 4
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-normalStuff, line 5
        name: "bpt-normalStuff", // library marker BPTWorld.bpt-normalStuff, line 6
        namespace: "BPTWorld", // library marker BPTWorld.bpt-normalStuff, line 7
        documentationLink: "", // library marker BPTWorld.bpt-normalStuff, line 8
        version: "1.0.0", // library marker BPTWorld.bpt-normalStuff, line 9
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-normalStuff, line 10
) // library marker BPTWorld.bpt-normalStuff, line 11

import groovy.json.* // library marker BPTWorld.bpt-normalStuff, line 13
import hubitat.helper.RMUtils // library marker BPTWorld.bpt-normalStuff, line 14
import java.util.TimeZone // library marker BPTWorld.bpt-normalStuff, line 15
import groovy.transform.Field // library marker BPTWorld.bpt-normalStuff, line 16
import groovy.time.TimeCategory // library marker BPTWorld.bpt-normalStuff, line 17
import java.text.SimpleDateFormat // library marker BPTWorld.bpt-normalStuff, line 18

def checkHubVersion() { // library marker BPTWorld.bpt-normalStuff, line 20
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-normalStuff, line 21
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-normalStuff, line 22
    log.trace "Hub Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-normalStuff, line 23
} // library marker BPTWorld.bpt-normalStuff, line 24

def parentCheck(){   // library marker BPTWorld.bpt-normalStuff, line 26
	state.appInstalled = app.getInstallationState()  // library marker BPTWorld.bpt-normalStuff, line 27
	if(state.appInstalled != 'COMPLETE'){ // library marker BPTWorld.bpt-normalStuff, line 28
		parentChild = true // library marker BPTWorld.bpt-normalStuff, line 29
  	} else { // library marker BPTWorld.bpt-normalStuff, line 30
    	parentChild = false // library marker BPTWorld.bpt-normalStuff, line 31
  	} // library marker BPTWorld.bpt-normalStuff, line 32
} // library marker BPTWorld.bpt-normalStuff, line 33

def createDeviceSection(driverName) { // library marker BPTWorld.bpt-normalStuff, line 35
    paragraph "This child app needs a virtual device to store values. Remember, multiple child apps can share this device if needed." // library marker BPTWorld.bpt-normalStuff, line 36
    input "useExistingDevice", "bool", title: "Use existing device (off) or have one created for you (on)", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 37
    if(useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 38
        input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Front Door')", required:true, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 39
        paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>" // library marker BPTWorld.bpt-normalStuff, line 40
        if(dataName) createDataChildDevice(driverName) // library marker BPTWorld.bpt-normalStuff, line 41
        if(statusMessageD == null) statusMessageD = "Waiting on status message..." // library marker BPTWorld.bpt-normalStuff, line 42
        paragraph "${statusMessageD}" // library marker BPTWorld.bpt-normalStuff, line 43
    } // library marker BPTWorld.bpt-normalStuff, line 44
    input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false // library marker BPTWorld.bpt-normalStuff, line 45
    if(!useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 46
        app.removeSetting("dataName") // library marker BPTWorld.bpt-normalStuff, line 47
        paragraph "<small>* Device must use the '${driverName}'.</small>" // library marker BPTWorld.bpt-normalStuff, line 48
    } // library marker BPTWorld.bpt-normalStuff, line 49
} // library marker BPTWorld.bpt-normalStuff, line 50

def createDataChildDevice(driverName) {     // library marker BPTWorld.bpt-normalStuff, line 52
    if(logEnable) log.debug "In createDataChildDevice (${state.version})" // library marker BPTWorld.bpt-normalStuff, line 53
    statusMessageD = "" // library marker BPTWorld.bpt-normalStuff, line 54
    if(!getChildDevice(dataName)) { // library marker BPTWorld.bpt-normalStuff, line 55
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}" // library marker BPTWorld.bpt-normalStuff, line 56
        try { // library marker BPTWorld.bpt-normalStuff, line 57
            addChildDevice("BPTWorld", driverName, dataName, 1234, ["name": "${dataName}", isComponent: false]) // library marker BPTWorld.bpt-normalStuff, line 58
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})" // library marker BPTWorld.bpt-normalStuff, line 59
            statusMessageD = "<b>Device has been been created. (${dataName})</b>" // library marker BPTWorld.bpt-normalStuff, line 60
        } catch (e) { if(logEnable) log.debug "Unable to create device - ${e}" } // library marker BPTWorld.bpt-normalStuff, line 61
    } else { // library marker BPTWorld.bpt-normalStuff, line 62
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>" // library marker BPTWorld.bpt-normalStuff, line 63
    } // library marker BPTWorld.bpt-normalStuff, line 64
    return statusMessageD // library marker BPTWorld.bpt-normalStuff, line 65
} // library marker BPTWorld.bpt-normalStuff, line 66

def uninstalled() { // library marker BPTWorld.bpt-normalStuff, line 68
    sendLocationEvent(name: "updateVersionInfo", value: "${app.id}:remove") // library marker BPTWorld.bpt-normalStuff, line 69
	removeChildDevices(getChildDevices()) // library marker BPTWorld.bpt-normalStuff, line 70
} // library marker BPTWorld.bpt-normalStuff, line 71

private removeChildDevices(delete) { // library marker BPTWorld.bpt-normalStuff, line 73
	delete.each {deleteChildDevice(it.deviceNetworkId)} // library marker BPTWorld.bpt-normalStuff, line 74
} // library marker BPTWorld.bpt-normalStuff, line 75

def letsTalk(msg) { // library marker BPTWorld.bpt-normalStuff, line 77
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 78
    if(useSpeech && fmSpeaker) { // library marker BPTWorld.bpt-normalStuff, line 79
        fmSpeaker.latestMessageFrom(state.name) // library marker BPTWorld.bpt-normalStuff, line 80
        fmSpeaker.speak(msg,null) // library marker BPTWorld.bpt-normalStuff, line 81
    } // library marker BPTWorld.bpt-normalStuff, line 82
} // library marker BPTWorld.bpt-normalStuff, line 83

def pushHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 85
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 86
    theMessage = "${app.label} - ${msg}" // library marker BPTWorld.bpt-normalStuff, line 87
    if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}" // library marker BPTWorld.bpt-normalStuff, line 88
    sendPushMessage.deviceNotification(theMessage) // library marker BPTWorld.bpt-normalStuff, line 89
} // library marker BPTWorld.bpt-normalStuff, line 90

def useWebOSHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 92
    if(logEnable) log.debug "In useWebOSHandler (${state.version}) - Sending to webOS - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 93
    useWebOS.deviceNotification(msg) // library marker BPTWorld.bpt-normalStuff, line 94
} // library marker BPTWorld.bpt-normalStuff, line 95

// ********** Normal Stuff ********** // library marker BPTWorld.bpt-normalStuff, line 97
def logsOff() { // library marker BPTWorld.bpt-normalStuff, line 98
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-normalStuff, line 99
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-normalStuff, line 100
} // library marker BPTWorld.bpt-normalStuff, line 101

def checkEnableHandler() { // library marker BPTWorld.bpt-normalStuff, line 103
    state.eSwitch = false // library marker BPTWorld.bpt-normalStuff, line 104
    if(disableSwitch) {  // library marker BPTWorld.bpt-normalStuff, line 105
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-normalStuff, line 106
        disableSwitch.each { it -> // library marker BPTWorld.bpt-normalStuff, line 107
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-normalStuff, line 108
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-normalStuff, line 109
        } // library marker BPTWorld.bpt-normalStuff, line 110
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-normalStuff, line 111
    } // library marker BPTWorld.bpt-normalStuff, line 112
} // library marker BPTWorld.bpt-normalStuff, line 113

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 115
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-normalStuff, line 116
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-normalStuff, line 117
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 118
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 119
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 120
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 121
    if(type == "logo") return "${loc}logo.png height=60>" // library marker BPTWorld.bpt-normalStuff, line 122
} // library marker BPTWorld.bpt-normalStuff, line 123

def getFormat(type, myText="") {			// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 125
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>" // library marker BPTWorld.bpt-normalStuff, line 126
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>" // library marker BPTWorld.bpt-normalStuff, line 127
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-normalStuff, line 128
} // library marker BPTWorld.bpt-normalStuff, line 129

def display(data) { // library marker BPTWorld.bpt-normalStuff, line 131
    if(data == null) data = "" // library marker BPTWorld.bpt-normalStuff, line 132
    setVersion() // library marker BPTWorld.bpt-normalStuff, line 133
    getHeaderAndFooter() // library marker BPTWorld.bpt-normalStuff, line 134
    if(app.label) { // library marker BPTWorld.bpt-normalStuff, line 135
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-normalStuff, line 136
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-normalStuff, line 137
        } else { // library marker BPTWorld.bpt-normalStuff, line 138
            theName = app.label // library marker BPTWorld.bpt-normalStuff, line 139
        } // library marker BPTWorld.bpt-normalStuff, line 140
    } // library marker BPTWorld.bpt-normalStuff, line 141
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-normalStuff, line 142
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) { // library marker BPTWorld.bpt-normalStuff, line 143
        paragraph "${state.headerMessage}" // library marker BPTWorld.bpt-normalStuff, line 144
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 145
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 146
    } // library marker BPTWorld.bpt-normalStuff, line 147
} // library marker BPTWorld.bpt-normalStuff, line 148

def display2() { // library marker BPTWorld.bpt-normalStuff, line 150
    section() { // library marker BPTWorld.bpt-normalStuff, line 151
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" } // library marker BPTWorld.bpt-normalStuff, line 152
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 153
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>" // library marker BPTWorld.bpt-normalStuff, line 154
        paragraph "${state.footerMessage}" // library marker BPTWorld.bpt-normalStuff, line 155
    } // library marker BPTWorld.bpt-normalStuff, line 156
} // library marker BPTWorld.bpt-normalStuff, line 157

def getHeaderAndFooter() { // library marker BPTWorld.bpt-normalStuff, line 159
    timeSinceNewHeaders() // library marker BPTWorld.bpt-normalStuff, line 160
    if(state.checkNow == null) state.checkNow = true // library marker BPTWorld.bpt-normalStuff, line 161
    if(state.totalHours > 6 || state.checkNow) { // library marker BPTWorld.bpt-normalStuff, line 162
        def params = [ // library marker BPTWorld.bpt-normalStuff, line 163
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json", // library marker BPTWorld.bpt-normalStuff, line 164
            requestContentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 165
            contentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 166
            timeout: 10 // library marker BPTWorld.bpt-normalStuff, line 167
        ] // library marker BPTWorld.bpt-normalStuff, line 168
        try { // library marker BPTWorld.bpt-normalStuff, line 169
            def result = null // library marker BPTWorld.bpt-normalStuff, line 170
            httpGet(params) { resp -> // library marker BPTWorld.bpt-normalStuff, line 171
                state.headerMessage = resp.data.headerMessage // library marker BPTWorld.bpt-normalStuff, line 172
                state.footerMessage = resp.data.footerMessage // library marker BPTWorld.bpt-normalStuff, line 173
            } // library marker BPTWorld.bpt-normalStuff, line 174
        } catch (e) { } // library marker BPTWorld.bpt-normalStuff, line 175
    } // library marker BPTWorld.bpt-normalStuff, line 176
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>" // library marker BPTWorld.bpt-normalStuff, line 177
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>" // library marker BPTWorld.bpt-normalStuff, line 178
} // library marker BPTWorld.bpt-normalStuff, line 179

def timeSinceNewHeaders() {  // library marker BPTWorld.bpt-normalStuff, line 181
    if(state.previous == null) {  // library marker BPTWorld.bpt-normalStuff, line 182
        prev = new Date() // library marker BPTWorld.bpt-normalStuff, line 183
    } else { // library marker BPTWorld.bpt-normalStuff, line 184
        try { // library marker BPTWorld.bpt-normalStuff, line 185
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // library marker BPTWorld.bpt-normalStuff, line 186
            prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000")) // library marker BPTWorld.bpt-normalStuff, line 187
        } catch(e) { // library marker BPTWorld.bpt-normalStuff, line 188
            prev = state.previous // library marker BPTWorld.bpt-normalStuff, line 189
        } // library marker BPTWorld.bpt-normalStuff, line 190
    } // library marker BPTWorld.bpt-normalStuff, line 191
    def now = new Date() // library marker BPTWorld.bpt-normalStuff, line 192
    use(TimeCategory) { // library marker BPTWorld.bpt-normalStuff, line 193
        state.dur = now - prev // library marker BPTWorld.bpt-normalStuff, line 194
        state.days = state.dur.days // library marker BPTWorld.bpt-normalStuff, line 195
        state.hours = state.dur.hours // library marker BPTWorld.bpt-normalStuff, line 196
        state.totalHours = (state.days * 24) + state.hours // library marker BPTWorld.bpt-normalStuff, line 197
    } // library marker BPTWorld.bpt-normalStuff, line 198
    state.previous = now // library marker BPTWorld.bpt-normalStuff, line 199
} // library marker BPTWorld.bpt-normalStuff, line 200

// ~~~~~ end include (2) BPTWorld.bpt-normalStuff ~~~~~
