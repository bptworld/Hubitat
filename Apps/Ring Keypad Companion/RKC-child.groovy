/**
 *  **************** Ring Keypad Companion Child App  ****************
 *
 *  Design Usage:
 *  Make the Ring Keypad Gen2 do more! For use with the Ring Alarm Gen 2 Keypad using the Ring Alarm Keypad G2 Community Driver.
 *
 *  Copyright 2021 Bryan Turcotte (@bptworld)
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
 *  1.0.0 - 11/14/21 - Initial release.
 *
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat
import hubitat.helper.RMUtils


def setVersion(){
    state.name = "Ring Keypad Companion Child"
	state.version = "1.0.0"
}

definition(
    name: "Ring Keypad Companion Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Make the Ring Keypad Gen2 do more! For use with the Ring Alarm Gen 2 Keypad using the Ring Alarm Keypad G2 Community Driver.",
    category: "Convenience",
	parent: "BPTWorld:Ring Keypad Companion",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Make the Ring Keypad Gen2 do more! For use with the Ring Alarm Gen 2 Keypad using the Ring Alarm Keypad G2 Community Driver."
            paragraph "This is a bare bones companion app to make it easy to use the advanced options available within the Keypad Community Driver. If more features/options are required, Event Engine has support for the community driver built in!"
		paragraph "Be sure to download the Ring Alarm Keypad G2 Community Driver from my Drivers section on GitHub!"
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Keypad Options")) {
            paragraph "<b>Security Keypad</b><br>For use with the Ring Alarm Gen 2 Keypad using the Ring Alarm Keypad G2 Community Driver.<br><small>This will NOT work with the built in Ring G2 driver.</small>"
            input "keypadEvent", "capability.securityKeypad", title: "By Security Keypad", required:false, multiple:true, submitOnChange:true
            paragraph "<small>* Note: If you are using Hub Mesh and have this cog on a different hub than the Keyapd, the codes must not be encrypted.</small>"
            if(keypadEvent) {
                input "keypadEventType", "enum", title: "Type of Keypad Event", options: ["Alarm User/Status", "Panic Buttons", "Alt Codes"], required:true, Multiple:false, submitOnChange:true                
                if(keypadEventType == "Alarm User/Status") {
                    app.removeSetting("keypadPanic")
                    app.removeSetting("keypadAltCode")
                    theNames = getLockCodeNames(keypadEvent)
                    input "keypadUser", "enum", title: "By Keypad User <small><abbr title='Only the selected users will trigger the Cog to run.'><b>- INFO -</b></abbr></small>", options: theNames, required:false, multiple:true, submitOnChange:true
                    input "keypadStatus", "enum", title: "By Keypad Status <small><abbr title='Only the selected status will trigger the Cog to run.'><b>- INFO -</b></abbr></small>", options: ["armed", "disarmed"], required:true, multiple:true, submitOnChange:true
                    theCogTriggers += "<b>-</b> By Keypad: ${keypadEvent} - keypadUser: ${keypadUser} - keypadStatus: ${keypadStatus}<br>"
                } else if(keypadEventType == "Panic Buttons") {
                    app.removeSetting("keypadUser")
                    app.removeSetting("keypadStatus")
                    app.removeSetting("keypadAltCode")
                    input "keypadPanic", "enum", title: "By Keypad Panic Button <small><abbr title='Cog will run when this Panic Button has been pressed.'><b>- INFO -</b></abbr></small>", options: ["police", "fire", "medical"], required:true, multiple:true, submitOnChange:true                       
                } else if(keypadEventType == "Alt Codes") {
                    app.removeSetting("keypadUser")
                    app.removeSetting("keypadStatus")
                    app.removeSetting("keypadPanic")
                    paragraph "Enter in any code to trigger the Cog. This code will need to be entered on the keypad and then press the 'check mark' button. IMPORTANT: This code can not match any of the alarm codes."
                    paragraph "Again: The codes entered here, CAN NOT be saved in the alarm codes of the keypad driver."
                    input "keypadAltCode", "text", title: "Alt Code", required:true, submitOnChange:true, width:6
                }
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Actions")) {
            paragraph "<b>Rule Machine</b>"
            input "rmRuleType", "bool", title: "Rule Type: Legacy Rules (off) or Rule 5.x and Over (on)", defaultValue:false, submitOnChange:true
            if(rmRuleType) {
                def rules50 = RMUtils.getRuleList('5.0')
                if(rules50) {
                    input "rmRule", "enum", title: "Select Rules 5.x", required:false, multiple:true, options: rules50, submitOnChange:true
                    if(rmRule) {
                        input "rmAction", "enum", title: "Action", required:false, multiple:false, options: [
                            ["runRuleAct":"Run"],
                            ["stopRuleAct":"Stop"],
                            ["pauseRule":"Pause"],
                            ["resumeRule":"Resume"],
                            ["runRule":"Evaluate"],
                            ["setRuleBooleanTrue":"Set Boolean True"],
                            ["setRuleBooleanFalse":"Set Boolean False"]
                        ], submitOnChange:true
                    }
                } else {
                    paragraph "No active Rule 5.x found."
                }
            } else {
                def rules = RMUtils.getRuleList()
                if(rules) {
                    input "rmRule", "enum", title: "Select Legacy Rules", required:false, multiple:true, options: rules, submitOnChange:true
                    if(rmRule) {
                        input "rmAction", "enum", title: "Action", required:false, multiple:false, options: [
                            ["runRuleAct":"Run"],
                            ["stopRuleAct":"Stop"],
                            ["pauseRule":"Pause"],
                            ["resumeRule":"Resume"],
                            ["runRule":"Evaluate"],
                            ["setRuleBooleanTrue":"Set Boolean True"],
                            ["setRuleBooleanFalse":"Set Boolean False"]
                        ], submitOnChange:true
                    }
                } else {
                    paragraph "No active Legacy Rules found."
                }
            }            
            paragraph "<hr>"
            paragraph "<b>Switch Devices</b>"
            input "switchesOn", "capability.switch", title: "Switches to turn On", multiple:true, submitOnChange:true
            input "switchesOff", "capability.switch", title: "Switches to turn Off", multiple:true, submitOnChange:true
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
        if(keypadEvent) subscribe(keypadEvent, "lastCodeName", securityKeypadHandler)
        if(keypadEvent) subscribe(keypadEvent, "securityKeypad", securityKeypadHandler)
    }
}

def securityKeypadHandler(evt) {
    if(logEnable) log.debug "In securityKeypadHandler (${state.version})"
    whoHappened = evt.displayName
    whatHappened = evt.value
    theDevice = evt.device
    theStatus = theDevice.currentValue("securityKeypad")
    theLastCodeName = theDevice.currentValue("lastCodeName")
    state.securityOK = false
    kUser = false
    kStatus = false
    if(logEnable) log.debug "In securityKeypadHandler - whoHappened: ${whoHappened} - whatHappened: ${whatHappened} - theStatus: ${theStatus} - theLastCodeName: ${theLastCodeName}"
    if(keypadEventType == "Alarm User/Status") {
        if(keypadUser && keypadStatus) {
            if(logEnable) log.debug "In securityKeypadHandler - keypadUser: ${keypadUser} contains ${whatHappened} - keypadStatus: ${keypadStatus} contains ${theStatus}"
            keypadUser.each {
                if(it.toString() == whatHappened.toString()) {
                    kUser = true
                }
            }
            keypadStatus.each {
                if(it.toString() == theStatus.toString()) {
                    kStatus = true
                }
            }
            if(kUser && kStatus) state.securityOK = true
        } else if(keypadUser && !keypadStatus) {
            keypadUser.each {
                if(it.toString() == whatHappened.toString()) {
                    state.securityOK = true
                }
            }
        } else if(!keypadUser && keypadStatus) {
            keypadStatus.each {
                if(it.toString() == theStatus.toString()) {
                    state.securityOK = true
                }
            }
        }
    }    
    if(keypadEventType == "Panic Buttons") {
        if(logEnable) log.debug "In securityKeypadHandler - Panic Buttons - keypadPanic: ${keypadPanic} VS ${whatHappened}"
        theButtons = keypadPanic.toString()
        eButton = theButtons.split(",")
        eButton.each { it ->
            if(it.startsWith(" ") || it.startsWith("[")) theBut = it.substring(1)
            theBut = theBut.replace("]","")
            if(logEnable) log.debug "In securityKeypadHandler - Panic Buttons - Checking: ${theBut}"
            if(whatHappened == theBut) {
                if(logEnable) log.debug "In securityKeypadHandler - Panic Buttons - MATCH!"
                state.securityOK = true
            }
        }
    }   
    if(keypadEventType == "Alt Codes") {
        if(logEnable) log.debug "In securityKeypadHandler - Alt Codes - keypadAltCode: ${keypadAltCode} VS ${whatHappened}"
        if(whatHappened == keypadAltCode) {
            if(logEnable) log.debug "In securityKeypadHandler - Alt Codes - MATCH!"
            state.securityOK = true
        }
    }
    if(logEnable) log.debug "In securityKeypadHandler - ********** securityOK: ${state.securityOK} **********"
    if(state.securityOK) {
        if(rmAction) ruleMachineHandler()
        if(switchesOn || switchesOff) switchesHandler()
    }
}

def ruleMachineHandler() {
    if(logEnable) log.debug "In ruleMachineHandler - rmRuleType: ${rmRuleType} - Rule: ${rmRule} - Action: ${rmAction}"
    if(rmRuleType) {
        RMUtils.sendAction(rmRule, rmAction, app.label, '5.0')
    } else {
        RMUtils.sendAction(rmRule, rmAction, app.label)
    }
}
    
def switchesHandler() {
    if(logEnable) log.debug "In switchesHandler (${state.version})"
    if(switchesOn) {
        switchesOn.each { it ->
            if(logEnable) log.debug "In switchesHandler - Turning $it.displayName - On"
            it.on()
        }
    }
    if(switchesOff) {
        switchesOff.each { it ->
            if(logEnable) log.debug "In switchesHandler - Turning $it.displayName - Off"
            it.off()
        }
    }
}

def getLockCodeNames(myDev) {  // Special thanks to Bruce @bravenel for this code
    def list = []
    myDev.each {
        list += getLockCodesFromDevice(it).tokenize(",")
    }
    lista = list.flatten().unique{ it }
    listb = lista.sort { a, b -> a <=> b }
    return listb
}

def getLockCodesFromDevice(device) {  // Special thanks to Bruce @bravenel for this code
    def lcText = device?.currentValue("lockCodes")
    if (!lcText?.startsWith("{")) {
        lcText = decrypt(lcText)
    } 
    def lockCodes
    if(lcText) lockCodes = new JsonSlurper().parseText(lcText)
    def result = ""
    lockCodes.each {if(it.value.name) result += it.value.name + ","}
    return result ? result[0..-2] : ""
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

def checkHubVersion() { // library marker BPTWorld.bpt-normalStuff, line 13
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-normalStuff, line 14
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-normalStuff, line 15
    log.trace "Hub Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-normalStuff, line 16
} // library marker BPTWorld.bpt-normalStuff, line 17

def createDeviceSection(driverName) { // library marker BPTWorld.bpt-normalStuff, line 19
    paragraph "This child app needs a virtual device to store values." // library marker BPTWorld.bpt-normalStuff, line 20
    input "useExistingDevice", "bool", title: "Use existing device (off) or have one created for you (on)", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 21
    if(useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 22
        input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Front Door')", required:true, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 23
        paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>" // library marker BPTWorld.bpt-normalStuff, line 24
        if(dataName) createDataChildDevice(driverName) // library marker BPTWorld.bpt-normalStuff, line 25
        if(statusMessageD == null) statusMessageD = "Waiting on status message..." // library marker BPTWorld.bpt-normalStuff, line 26
        paragraph "${statusMessageD}" // library marker BPTWorld.bpt-normalStuff, line 27
    } // library marker BPTWorld.bpt-normalStuff, line 28
    input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false // library marker BPTWorld.bpt-normalStuff, line 29
    if(!useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 30
        app.removeSetting("dataName") // library marker BPTWorld.bpt-normalStuff, line 31
        paragraph "<small>* Device must use the '${driverName}'.</small>" // library marker BPTWorld.bpt-normalStuff, line 32
    } // library marker BPTWorld.bpt-normalStuff, line 33
} // library marker BPTWorld.bpt-normalStuff, line 34

def createDataChildDevice(driverName) {     // library marker BPTWorld.bpt-normalStuff, line 36
    if(logEnable) log.debug "In createDataChildDevice (${state.version})" // library marker BPTWorld.bpt-normalStuff, line 37
    statusMessageD = "" // library marker BPTWorld.bpt-normalStuff, line 38
    if(!getChildDevice(dataName)) { // library marker BPTWorld.bpt-normalStuff, line 39
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}" // library marker BPTWorld.bpt-normalStuff, line 40
        try { // library marker BPTWorld.bpt-normalStuff, line 41
            addChildDevice("BPTWorld", driverName, dataName, 1234, ["name": "${dataName}", isComponent: false]) // library marker BPTWorld.bpt-normalStuff, line 42
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})" // library marker BPTWorld.bpt-normalStuff, line 43
            statusMessageD = "<b>Device has been been created. (${dataName})</b>" // library marker BPTWorld.bpt-normalStuff, line 44
        } catch (e) { if(logEnable) log.debug "Unable to create device - ${e}" } // library marker BPTWorld.bpt-normalStuff, line 45
    } else { // library marker BPTWorld.bpt-normalStuff, line 46
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>" // library marker BPTWorld.bpt-normalStuff, line 47
    } // library marker BPTWorld.bpt-normalStuff, line 48
    return statusMessageD // library marker BPTWorld.bpt-normalStuff, line 49
} // library marker BPTWorld.bpt-normalStuff, line 50

def uninstalled() { // library marker BPTWorld.bpt-normalStuff, line 52
	removeChildDevices(getChildDevices()) // library marker BPTWorld.bpt-normalStuff, line 53
} // library marker BPTWorld.bpt-normalStuff, line 54

private removeChildDevices(delete) { // library marker BPTWorld.bpt-normalStuff, line 56
	delete.each {deleteChildDevice(it.deviceNetworkId)} // library marker BPTWorld.bpt-normalStuff, line 57
} // library marker BPTWorld.bpt-normalStuff, line 58

def letsTalk(msg) { // library marker BPTWorld.bpt-normalStuff, line 60
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 61
    if(useSpeech && fmSpeaker) { // library marker BPTWorld.bpt-normalStuff, line 62
        fmSpeaker.latestMessageFrom(state.name) // library marker BPTWorld.bpt-normalStuff, line 63
        fmSpeaker.speak(msg,null) // library marker BPTWorld.bpt-normalStuff, line 64
    } // library marker BPTWorld.bpt-normalStuff, line 65
} // library marker BPTWorld.bpt-normalStuff, line 66

def pushHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 68
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 69
    theMessage = "${app.label} - ${msg}" // library marker BPTWorld.bpt-normalStuff, line 70
    if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}" // library marker BPTWorld.bpt-normalStuff, line 71
    sendPushMessage.deviceNotification(theMessage) // library marker BPTWorld.bpt-normalStuff, line 72
} // library marker BPTWorld.bpt-normalStuff, line 73

// ********** Normal Stuff ********** // library marker BPTWorld.bpt-normalStuff, line 75
def logsOff() { // library marker BPTWorld.bpt-normalStuff, line 76
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-normalStuff, line 77
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-normalStuff, line 78
} // library marker BPTWorld.bpt-normalStuff, line 79

def checkEnableHandler() { // library marker BPTWorld.bpt-normalStuff, line 81
    state.eSwitch = false // library marker BPTWorld.bpt-normalStuff, line 82
    if(disableSwitch) {  // library marker BPTWorld.bpt-normalStuff, line 83
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-normalStuff, line 84
        disableSwitch.each { it -> // library marker BPTWorld.bpt-normalStuff, line 85
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-normalStuff, line 86
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-normalStuff, line 87
        } // library marker BPTWorld.bpt-normalStuff, line 88
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-normalStuff, line 89
    } // library marker BPTWorld.bpt-normalStuff, line 90
} // library marker BPTWorld.bpt-normalStuff, line 91

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 93
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-normalStuff, line 94
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-normalStuff, line 95
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 96
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 97
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 98
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 99
    if(type == "logo") return "${loc}logo.png height=60>" // library marker BPTWorld.bpt-normalStuff, line 100
} // library marker BPTWorld.bpt-normalStuff, line 101

def getFormat(type, myText="") {			// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 103
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>" // library marker BPTWorld.bpt-normalStuff, line 104
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>" // library marker BPTWorld.bpt-normalStuff, line 105
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-normalStuff, line 106
} // library marker BPTWorld.bpt-normalStuff, line 107

def display(data) { // library marker BPTWorld.bpt-normalStuff, line 109
    if(data == null) data = "" // library marker BPTWorld.bpt-normalStuff, line 110
    setVersion() // library marker BPTWorld.bpt-normalStuff, line 111
    getHeaderAndFooter() // library marker BPTWorld.bpt-normalStuff, line 112
    if(app.label) { // library marker BPTWorld.bpt-normalStuff, line 113
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-normalStuff, line 114
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-normalStuff, line 115
        } else { // library marker BPTWorld.bpt-normalStuff, line 116
            theName = app.label // library marker BPTWorld.bpt-normalStuff, line 117
        } // library marker BPTWorld.bpt-normalStuff, line 118
    } // library marker BPTWorld.bpt-normalStuff, line 119
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-normalStuff, line 120
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) { // library marker BPTWorld.bpt-normalStuff, line 121
        paragraph "${state.headerMessage}" // library marker BPTWorld.bpt-normalStuff, line 122
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 123
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 124
    } // library marker BPTWorld.bpt-normalStuff, line 125
} // library marker BPTWorld.bpt-normalStuff, line 126

def display2() { // library marker BPTWorld.bpt-normalStuff, line 128
    section() { // library marker BPTWorld.bpt-normalStuff, line 129
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 130
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>" // library marker BPTWorld.bpt-normalStuff, line 131
        paragraph "${state.footerMessage}" // library marker BPTWorld.bpt-normalStuff, line 132
    } // library marker BPTWorld.bpt-normalStuff, line 133
} // library marker BPTWorld.bpt-normalStuff, line 134

def getHeaderAndFooter() { // library marker BPTWorld.bpt-normalStuff, line 136
    timeSinceNewHeaders() // library marker BPTWorld.bpt-normalStuff, line 137
    if(state.checkNow == null) state.checkNow = true // library marker BPTWorld.bpt-normalStuff, line 138
    if(state.totalHours > 6 || state.checkNow) { // library marker BPTWorld.bpt-normalStuff, line 139
        def params = [ // library marker BPTWorld.bpt-normalStuff, line 140
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json", // library marker BPTWorld.bpt-normalStuff, line 141
            requestContentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 142
            contentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 143
            timeout: 10 // library marker BPTWorld.bpt-normalStuff, line 144
        ] // library marker BPTWorld.bpt-normalStuff, line 145
        try { // library marker BPTWorld.bpt-normalStuff, line 146
            def result = null // library marker BPTWorld.bpt-normalStuff, line 147
            httpGet(params) { resp -> // library marker BPTWorld.bpt-normalStuff, line 148
                state.headerMessage = resp.data.headerMessage // library marker BPTWorld.bpt-normalStuff, line 149
                state.footerMessage = resp.data.footerMessage // library marker BPTWorld.bpt-normalStuff, line 150
            } // library marker BPTWorld.bpt-normalStuff, line 151
        } catch (e) { } // library marker BPTWorld.bpt-normalStuff, line 152
    } // library marker BPTWorld.bpt-normalStuff, line 153
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>" // library marker BPTWorld.bpt-normalStuff, line 154
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>" // library marker BPTWorld.bpt-normalStuff, line 155
} // library marker BPTWorld.bpt-normalStuff, line 156

def timeSinceNewHeaders() {  // library marker BPTWorld.bpt-normalStuff, line 158
    if(state.previous == null) {  // library marker BPTWorld.bpt-normalStuff, line 159
        prev = new Date() // library marker BPTWorld.bpt-normalStuff, line 160
    } else { // library marker BPTWorld.bpt-normalStuff, line 161
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // library marker BPTWorld.bpt-normalStuff, line 162
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000")) // library marker BPTWorld.bpt-normalStuff, line 163
    } // library marker BPTWorld.bpt-normalStuff, line 164
    def now = new Date() // library marker BPTWorld.bpt-normalStuff, line 165
    use(TimeCategory) { // library marker BPTWorld.bpt-normalStuff, line 166
        state.dur = now - prev // library marker BPTWorld.bpt-normalStuff, line 167
        state.days = state.dur.days // library marker BPTWorld.bpt-normalStuff, line 168
        state.hours = state.dur.hours // library marker BPTWorld.bpt-normalStuff, line 169
        state.totalHours = (state.days * 24) + state.hours // library marker BPTWorld.bpt-normalStuff, line 170
    } // library marker BPTWorld.bpt-normalStuff, line 171
    state.previous = now // library marker BPTWorld.bpt-normalStuff, line 172
} // library marker BPTWorld.bpt-normalStuff, line 173

// ~~~~~ end include (2) BPTWorld.bpt-normalStuff ~~~~~
