/**
 *  **************** Averaging Plus Child App  ****************
 *
 *  Design Usage:
 *  Average just about anything. Get notifications based on Setpoints.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
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
 *  1.0.1 - 06/07/20 - Added more options and some error trapping
 *  1.0.0 - 05/25/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Averaging Plus"
	state.version = "1.0.1"
}

definition(
    name: "Averaging Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Average just about anything. Get notifications based on Setpoints.",
    category: "Convenience",
	parent: "BPTWorld:Averaging Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Averaging%20Plus/AP-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "highSetpointConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "lowSetpointConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "notificationOptions", title: "", install: false, uninstall: false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Average just about anything. Get notifications based on Setpoints."
            paragraph "<b>How the Averaging works</b><br>- Select a bunch of devices that share an attribute (ie. temperature)<br>- Select the Attribute to average<br>- Select the time frame between averages<br><br>For each device that has the attribute, the value will be added to the total value, then divided by the number of devices that had the attribute."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the averaging results. This device can also be selected below in the Setpoint options to be used as a switch to control other things."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have AP create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'AP - Motion Sensors')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Simple Averaging' Driver.</small>"
            }
        }      
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Devices to Average Options")) {
            paragraph "Select a group of devices that share a common attribute to average.<br><small>Note: Does NOT have to be the same 'type' of devices, just share a common attribute, ie. 'temperature'.</small>"
            input "theDevices", "capability.*", title: "Select Devices", required:false, multiple:true, submitOnChange:true
            
            if(theDevices) {
                allAttrs = []
                allAttrs = theDevices.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrsa = allAttrs.sort { a, b -> a.value <=> b.value }
                input "attrib", "enum", title: "Attribute to Average", required:true, multiple:false, submitOnChange:true, options:allAttrsa
            }
            
            if(theDevices && attrib) {
                input "triggerMode", "enum", title: "Time Between Averages", submitOnChange:true, options: ["1_Min","5_Min","10_Min","15_Min","30_Min","1_Hour","3_Hour"], required:true
                input "timeToReset", "time", title: "Reset averages at this time every day (ie. 12:00am)", required:true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Setpoint Options")) {
            paragraph "If the average becomes too high or low, notifications can be sent."
            input "highSetpoint", "number", title: "High Setpoint", required:false, submitOnChange:true, width:6
            input "lowSetpoint", "number", title: "Low Setpoint", required:false, submitOnChange:true, width:6
            
            if(highSetpoint) {
                if(spHighDevices || sendPushHigh) {
                    href "highSetpointConfig", title:"${getImage("optionsGreen")} High Setpoint Options", description:"Click here for options"
                } else {
                    href "highSetpointConfig", title:"${getImage("optionsRed")} High Setpoint Options", description:"Click here for options"
                }
            }
            
            if(lowSetpoint) {
                if(spLowDevices || sendPushLow) {
                    href "lowSetpointConfig", title:"${getImage("optionsGreen")} Low Setpoint Options", description:"Click here for options"
                } else {
                    href "lowSetpointConfig", title:"${getImage("optionsRed")} Low Setpoint Options", description:"Click here for options"
                }
            }
        }
        
        if(highSetpoint || lowSetpoint) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
                if(speakerMP || speakerSS || speakerProxy) {
                    href "notificationOptions", title:"${getImage("optionsGreen")} Notification Options", description:"Click here for options"
                } else {
                    href "notificationOptions", title:"${getImage("optionsRed")} Notification Options", description:"Click here for options"
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required:false, submitOnChange:true
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def lowSetpointConfig() {
	dynamicPage(name: "lowSetpointConfig", title: "", install:false, uninstall:false) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Average Too Low Options")) {
            input "spLowDevices", "capability.switch", title: "Turn on Device(s)", required:false, multiple:true, submitOnChange:true
            if(spLowDevices) {
                input "lowTimesOn", "number", title: "How many 'Too Low Averages' required in a row to turn switch On", defaultValue:2, submitOnChange:true
                input "lowDeviceAutoOff", "bool", title: "Automatically turn the devices off when return to normal range", defaultValue:false, required:false, submitOnChange:true
                if(lowDeviceAutoOff) {
                    input "lowTimesOff", "number", title: "How many 'Normal Averages' required in a row to turn switch Off", defaultValue:3, submitOnChange:true
                }
            }
            
            input "sendPushLow", "bool", title: "Send a Pushover notification", defaultValue:false, required:false, submitOnChange:true
        }       
    }
}

def highSetpointConfig() {
	dynamicPage(name: "highSetpointConfig", title: "", install:false, uninstall:false) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Average Too High Options")) {
            input "spHighDevices", "capability.switch", title: "Turn on Device(s)", required:false, multiple:true, submitOnChange:true
            if(spHighDevices) {
                input "highTimes", "number", title: "How many 'Too High Averages' required in a row to turn switch On", defaultValue:2, submitOnChange:true
                input "highDeviceAutoOff", "bool", title: "Automatically turn the devices off when return to normal range", defaultValue:false, required:false, submitOnChange:true
                if(highDeviceAutoOff) {
                    input "highTimesOff", "number", title: "How many 'Normal Averages' required in a row to turn switch Off", defaultValue:3, submitOnChange:true
                }
            }
            
            input "sendPushHigh", "bool", title: "Send a Pushover notification", defaultValue:false, required:false, submitOnChange:true
        }      
    }
}

def notificationOptions(){
    dynamicPage(name: "notificationOptions", title: "", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerProxy", "bool", defaultValue: false, title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange: true
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
		        paragraph "Speaker proxy in use."
            }
        }
        
        if(sendPushLow) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages - Low")) {
                paragraph "Wildcards:<br>- %avg% - Display the Average value"
                input "spLowSendPushMessage", "capability.notification", title: "Send a Push notification", multiple:true, required:false, submitOnChange:true
                input "spLowMessage", "text", title: "Message", submitOnChange:true
            }
        }
        
        if(sendPushHigh) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages - High")) {
                paragraph "Wildcards:<br>- %avg% - Display the Average value"
                input "spHighSendPushMessage", "capability.notification", title: "Send a Push notification", multiple:true, required:false, submitOnChange:true
                input "spHighMessage", "text", title: "Message", submitOnChange:true
            }
        }
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
	initialize()
}

def initialize() {
    setDefaults()
    if(theDevices && attrib) {
        if(triggerMode == "1_Min") runEvery1Minute(averageHandler)
        if(triggerMode == "5_Min") runEvery5Minutes(averageHandler)
        if(triggerMode == "10_Min") runEvery10Minutes(averageHandler)
        if(triggerMode == "15_Min") runEvery15Minutes(averageHandler)
        if(triggerMode == "30_Min") runEvery30Minutes(averageHandler)
        if(triggerMode == "1_Hour") runEvery1Hour(averageHandler)
        if(triggerMode == "3_Hour") runEvery3Hours(averageHandler)
        
        schedule(timeToReset, resetHandler)
        
        averageHandler()
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def resetHandler() {
    if(logEnable) log.debug "In zeroHandler (${state.version})"
    if(theDevices) {
        dataDevice.virtualAverage("-")
        dataDevice.todaysHigh("-")
        dataDevice.todaysLow("-")
    }
}

def averageHandler(evt) {
    if(logEnable) log.debug "In averageHandler (${state.version})"
    if(theDevices) {
        if(logEnable) log.debug "     - - - - - Start (Averaging) - - - - -     "
        totalNum = 0
        numOfDev = 0
        state.low = false
        state.high = false
        
        theDevices.each { it ->
            num1 = it.currentValue("${attrib}")
            int num = num1
            if(logEnable) log.debug "In averageHandler - working on ${it} - ${attrib} - num: ${num}"
            if(num) {
                numOfDev += 1
                totalNum += num
            }
        }
        if(totalNum == 0 || totalNum == null) {
            state.theAverage = 0
        } else {
            state.theAverage = (totalNum / numOfDev).toDouble().round(1)
        }
        if(logEnable) log.debug "In averageHandler - Sending virtualAverage: ${state.theAverage}"
        todaysHigh = dataDevice.currentValue("todaysHigh")
        todaysLow = dataDevice.currentValue("todaysLow")
        
        if(todaysHigh == null) todaysHigh = 0
        if(todaysLow == null) todaysLow = 100000
        if(state.theAverage > todaysHigh) { dataDevice.todaysHigh(state.theAverage) }
        if(state.theAverage < todaysLow) { dataDevice.todaysLow(state.theAverage) }        
        dataDevice.virtualAverage(state.theAverage)
        
        if(state.theAverage <= lowSetpoint) {
            if(logEnable) log.debug "In averageHandler - The average (${state.theAverage}) is BELOW the low setpoint (${lowSetpoint})"
            state.low = true
            state.nTimes = 0
            state.lTimes = state.lTimes + 1
            if(spLowDevices) {
                spLowDevices.each {
                    it.on()
                }
            }
            if(spLowSendPushMessage && !state.sentPush) {
                messageHandler(spLowMessage)
                pushNow(theMsg)
            }
        }
        
        if(state.theAverage >= highSetpoint) {
            if(logEnable) log.debug "In averageHandler - The average (${state.theAverage}) is ABOVE the high setpoint (${highSetpoint})"
            state.high = true
            state.nTimes = 0
            state.hTimes = state.hTimes + 1
            if(spHighDevices) {
                spHighDevices.each {
                    it.on()
                }
            }
            if(spHighSendPushMessage && !state.sentPush) {
                messageHandler(spHighMessage)
                pushNow(theMsg)
            }
        }
        
        if(state.theAverage < highSetpoint && state.theAverage > lowSetpoint) {
            if(logEnable) log.debug "In averageHandler - The average (${state.theAverage}) looks good!"
            
            state.hTimes = 0
            state.lTimes = 0
            state.nTimes = state.nTimes + 1
            
            if(spHighDevices && highDeviceAutoOff && state.nTimes >= highTimesOff) {               
                spHighDevices.each {
                    it.off()
                }
            }
            
            if(spLowDevices && lowDeviceAutoOff && state.nTimes >= lowTimesOff) {                
                spLowDevices.each {
                    it.off()
                }
            }
            
            state.sentPush = false
        }               
        if(logEnable) log.debug "     - - - - - End (Averaging) - - - - -     "
    }
}

def letsTalk(msg) {
	if(logEnable) log.debug "In letsTalk (${state.version})"
	checkTime()
	checkVol()
    if(state.timeBetween == true) {
		theMsg = msg
        speechDuration = Math.max(Math.round(theMsg.length()/12),2)+3		// Code from @djgutheinz
        speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, theMsg: ${theMsg}, volRestore: ${volRestore}"
        state.speakers.each { it ->
            if(logEnable) log.debug "Speaker in use: ${it}"
            if(speakerProxy) {
                if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                it.speak(theMsg)
            } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                def prevVolume = it.currentValue("volume")
                it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
            } else if(it.hasCommand('playTextAndRestore')) {   
                if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                def prevVolume = it.currentValue("volume")
                it.playTextAndRestore(theMsg, prevVolume)
            } else {		        
                if(logEnable) log.debug "In letsTalk - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                it.speak(theMsg)
                pauseExecution(speechDuration2)
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
            }
        }
	    if(logEnable) log.debug "In letsTalk - Finished speaking"  
	    log.info "${app.label} - ${theMsg}"
	} else {
        if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
    theMsg = ""
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

def messageHandler(msg) {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    if(msg.contains("%avg%")) {theMsg = msg.replace('%avg%', "${state.theAverage}" )}

    if(logEnable) log.debug "In messageHandler - theMsg: ${theMsg}"
    return theMsg
}

def pushNow(msg) {
    if(logEnable) log.debug "In pushNow (${state.version})"
    pushMessage = "${app.label} \n"
    pushMessage += msg
    if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
    if(state.low) spLowSendPushMessage.deviceNotification(pushMessage)
    if(state.high) spHighSendPushMessage.deviceNotification(pushMessage)
    state.sentPush = true
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Averaging Plus Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Averaging Plus unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}

// ********** Normal Stuff **********

def setDefaults() {
	if(logEnable == null){logEnable = false}
    state.nTimes = 0
    state.lTimes = 0
    state.hTimes = 0
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}

