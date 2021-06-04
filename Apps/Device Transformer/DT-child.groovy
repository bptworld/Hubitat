/**
 *  **************** Device Transformer Child App  ****************
 *
 *  Design Usage:
 *  Transform a device into a virtual multi-sensor device.
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
 *  1.0.0 - 06/04/21 - Initial release.
 *
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Device Transformer Child"
	state.version = "1.0.0"
}

definition(
    name: "Device Transformer Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Transform a device into a virtual multi-sensor device.",
    category: "Convenience",
	parent: "BPTWorld:Device Transformer",
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
    		paragraph "Transform a device into a virtual multi-sensor device."
		}

        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the transformed results."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have DT create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
                input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'DT - Front Door Sensor')", required:true, submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Device Transformer Driver'.</small>"
            }
        } 
       
        section(getFormat("header-green", "${getImage("Blank")}"+" Origin Device")) {
            input "typeOfDevice", "enum", title: "Type of Device to Transform", options: ["Contact Sensor", "Lock", "Motion Sensor", "Presence Sensor", "Switch"], multiple:false, submitOnChange:true
            if(typeOfDevice) {
                if(typeOfDevice == "Contact Sensor") {
                    input "xContact", "capability.contactSensor", title: "Select a Contact Sensor", multiple:false, submitOnChange:true
                } else if(typeOfDevice == "Lock") {
                    input "xLock", "capability.lock", title: "Select a Lock", multiple:false, submitOnChange:true
                } else if(typeOfDevice == "Motion Sensor") {
                    input "xMotion", "capability.motionSensor", title: "Select a Motion Sensor", multiple:false, submitOnChange:true
                } else if(typeOfDevice == "Presence Sensor") {
                    input "xPresence", "capability.presenceSensor", title: "Select a Presence Sensor", multiple:false, submitOnChange:true
                } else if(typeOfDevice == "Switch") {
                    input "xSwitch", "capability.switch", title: "Select a Switch", multiple:false, submitOnChange:true
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Transform Options")) {
            input "reverse", "bool", title: "Want to reverse the output?", defaultValue:false, submitOnChange:true
            if(reverse) {
                if(typeOfDevice == "Contact Sensor") {
                    state.theOutput1 =  "Contact Sensor - open will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                    state.theOutput2 = "Contact Sensor - closed will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                } else if(typeOfDevice == "Lock") {
                    state.theOutput1 =  "Lock - unlocked will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                    state.theOutput2 = "Lock - locked will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                } else if(typeOfDevice == "Motion Sensor") {
                    state.theOutput1 =  "Motion Sensor - active will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                    state.theOutput2 = "Motion Sensor - inactive will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                } else if(typeOfDevice == "Presence Sensor") {
                    state.theOutput1 =  "Presence Sensor - present will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                    state.theOutput2 = "Presence Sensor - not present will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                } else if(typeOfDevice == "Switch") {
                    state.theOutput1 =  "Switch - on will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                    state.theOutput2 = "Switch - off will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                }
            } else {
                if(typeOfDevice == "Contact Sensor") {
                    state.theOutput1 =  "Contact Sensor - open will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                    state.theOutput2 = "Contact Sensor - closed will become...<br> - Contact: <br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                } else if(typeOfDevice == "Lock") {
                    state.theOutput1 =  "Lock - unlocked will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                    state.theOutput2 = "Lock - locked will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                } else if(typeOfDevice == "Motion Sensor") {
                    state.theOutput1 =  "Motion Sensor - active will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                    state.theOutput2 = "Motion Sensor - inactive will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                } else if(typeOfDevice == "Presence Sensor") {
                    state.theOutput1 =  "Presence Sensor - present will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                    state.theOutput2 = "Presence Sensor - not present will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                } else if(typeOfDevice == "Switch") {
                    state.theOutput1 =  "Switch - on will become...<br> - Contact: open<br> - Lock: unlocked<br> - Motion: active<br> - Presence: present<br> - Switch: on"
                    state.theOutput2 = "Switch - off will become...<br> - Contact: closed<br> - Lock: locked<br> - Motion: inactive<br> - Presence: not present<br> - Switch: off"
                }
            }            
            paragraph "<hr>"
            paragraph "${state.theOutput1}"
            paragraph "${state.theOutput2}"
            paragraph "<hr>"       
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
        if(xContact) subscribe(xContact, "contact", theDeviceHandler)
        if(xLock) subscribe(xLock, "lock", theDeviceHandler)
        if(xMotion) subscribe(xMotion, "motion", theDeviceHandler)
        if(xPresence) subscribe(xPresence, "presence", theDeviceHandler)
        if(xSwitch) subscribe(xSwitch, "switch", theDeviceHandler)
    }
}

def theDeviceHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In theDeviceHandler (${state.version})"
        whatHappened = evt.value
        state.recognised = false
        if(logEnable) log.debug "In theDeviceHandler - whatHappened: ${whatHappened}"
        if(whatHappened == "open" || whatHappened == "unlocked" || whatHappened == "active" || whatHappened == "present" || whatHappened == "on") {
            state.recognised = true
            if(reverse) {
                contactValue = "closed"
                lockValue = "locked"
                motionValue = "inactive"
                presenceValue = "not present"
                switchValue = "off"
            } else {
                contactValue = "open"
                lockValue = "unlocked"
                motionValue = "active"
                presenceValue = "present"
                switchValue = "on"
            }
        } else if(whatHappened == "closed" || whatHappened == "locked" || whatHappened == "inactive" || whatHappened == "not present" || whatHappened == "off") {
            state.recognised = true
            if(reverse) {
                contactValue = "open"
                lockValue = "unlocked"
                motionValue = "active"
                presenceValue = "present"
                switchValue = "on"
            } else {
                contactValue = "closed"
                lockValue = "locked"
                motionValue = "inactive"
                presenceValue = "not present"
                switchValue = "off"
            }
        } else {
            if(logEnable) log.debug "In theDeviceHandler - unrecognised value: ${whatHappened}" 
        }
        
        if(state.recognised) {
            dataDevice.sendEvent(name: "contact", value: contactValue, isStateChange: true)
            dataDevice.sendEvent(name: "lock", value: lockValue, isStateChange: true)
            dataDevice.sendEvent(name: "motion", value: motionValue, isStateChange: true)
            dataDevice.sendEvent(name: "presence", value: presenceValue, isStateChange: true)
            dataDevice.sendEvent(name: "switch", value: switchValue, isStateChange: true)  
            dataDevice.sendEvent(name: "lastUpdated", value: new Date(), isStateChange: true)  
        }
    }
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Device Transformer Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Device Transformer unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}

// ********** Normal Stuff **********
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
            if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch} - ${eSwitch}"
        }
    }
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

def display(data) {
    if(data == null) data = ""
    setVersion()
    getHeaderAndFooter()
    if(app.label) {
        if(app.label.contains("(Paused)")) {
            theName = app.label - " <span style='color:red'>(Paused)</span>"
        } else {
            theName = app.label
        }
    }
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
        paragraph getFormat("line")
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
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
}
