/**
 *  **************** Presence Plus Child App  ****************
 *
 *  Design Usage:
 *  Creates a combined presence device that can be used with Life360 Tracker, Google Assistant, Rule Machine and More!
 *
 *  Copyright 2019-2021 Bryan Turcotte (@bptworld)
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
 *  1.0.9 - 04/03/21 - Fixed error
 *  1.0.8 - 03/13/21 - Added Contacts as a Presence option
 *  1.0.7 - 09/24/20 - Lots of Adjustments
 *  1.0.6 - 05/12/20 - Added separate delays for Present and Not Present
 *  1.0.5 - 05/05/20 - Added Advanced Present section giving users a second set of Present options
 *  1.0.4 - 05/05/20 - Added number of sensors required to change status
 *  1.0.3 - 05/05/20 - Added delay before status is updated
 *  1.0.2 - 04/27/20 - Cosmetic changes
 *  1.0.1 - 12/05/19 - Tightening up some code.
 *  1.0.0 - 11/01/19 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Presence Plus"
	state.version = "1.0.9"
}

definition(
    name: "Presence Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Creates a combined presence device that can be used with Life360 Tracker, Google Assistant, Rule Machine and More!",
    category: "Convenience",
	parent: "BPTWorld:Presence Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Presence%20Plus/PP-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Instructions:</b>"
            paragraph "<i>Trigger Type:</i><br>OR - If any selected sensor changes status, then device created will also change<br>AND - If all selected sensors show the same status, the device created will change to that status"
            paragraph "The device created will hold the present/not present value and will also turn on/off which can be used with Goolge Assistant. On = present, Off = not present"
            paragraph "<b>Notes:</b>"
            paragraph "This is a simple way to combine presence sensors. Built with other BPTWorld apps in mind, this will make it easier to expand the functions of other BPTWorld apps."
            paragraph "What ever name you choose for this child app will also be the name of the device automaticaly created."
            paragraph "Also, it's <i>always</i> a good idea to go into the newly created device and set the initial presence state."
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this child app", required:false, submitOnChange:true
            paragraph "Note: What ever name you place in here will also be the name of the device automaticaly created."
            if(app.label) createChildDevice()
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Present Options")) {
            input "ArrTriggerType", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, submitOnChange:true
            if(ArrTriggerType) paragraph "<b>using 'AND'</b>"
            if(!ArrTriggerType) paragraph "<b>using 'OR'</b>"
			input "ArrPresenceSensors", "capability.presenceSensor", title: "Presence Sensors to combine (present)", multiple:true, required:false
            input "ArrConPresenceSensors", "capability.contactSensor", title: "Contact Sensors to combine (present when closed)", multiple:true, required:false
            if(ArrTriggerType) input "arrNumOfSensors", "number", title: "How many sensors does it take to change status for Present (leave blank for All)", required:false, submitOnChange:true 
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Present Options")) {
            paragraph "Advanced Present Options give you a second set of Present Triggers to choose from.<br>ie. if sensor1 = present -> Present 'else' if sensor2 and sensor3 = present -> Present"
            input "useAdvancedArr", "bool", title: "Use Advanced Present Options", description: "Advanced Present", required:false, submitOnChange:true
            if(useAdvancedArr) {
                input "ArrTriggerType2", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, submitOnChange:true
                if(ArrTriggerType2) paragraph "<b>using 'AND'</b>"
                if(!ArrTriggerType2) paragraph "<b>using 'OR'</b>"
                input "ArrPresenceSensors2", "capability.presenceSensor", title: "Presence Sensors to combine (present)", multiple:true, required:false
                input "ArrConPresenceSensors2", "capability.contactSensor", title: "Contact Sensors to combine (present when closed)", multiple:true, required:false
                if(ArrTriggerType2) input "arrNumOfSensors2", "number", title: "How many sensors does it take to change status for Present (leave blank for All)", required:false, submitOnChange:true 
            } else {
                app.removeSetting("ArrTriggerType2")
                app.removeSetting("ArrPresenceSensors2")
                app.removeSetting("ArrConPresenceSensors2")
                app.removeSetting("arrNumOfSensors2")           
            }
		}
 
        section(getFormat("header-green", "${getImage("Blank")}"+" Not Present Options")) {
            input "DepTriggerType", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, submitOnChange:true
			if(DepTriggerType) paragraph "<b>using 'AND'</b>"
            if(!DepTriggerType) paragraph "<b>using 'OR'</b>"
            input "DepPresenceSensors", "capability.presenceSensor", title: "Presence Sensors to combine (not present)", multiple:true, required:false
            input "DepConPresenceSensors", "capability.contactSensor", title: "Contact Sensors to combine (not present when open)", multiple:true, required:false
            if(DepTriggerType) input "depNumOfSensors", "number", title: "How many sensors does it take to change status for Not Present (leave blank for All)", required:false, submitOnChange:true
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Failsafe Options")) {
            paragraph "Sometimes a Present or Not Present can be missed. With this option, Presence Plus will check every X minutes to see who is here."
            input "runEvery", "enum", title: "Check every X minutes", description: "runEvery", required:false, submitOnChange:true, options: ["Every 1 Minute", "Every 5 Minutes", "Every 10 Minutes", "Every 15 Minutes", "Every 30 Minutes", "Every 1 Hour", "Every 3 Hours"]
            input "theDelayArr", "number", title: "Delay setting Present status by (seconds)", required:false, submitOnChange:true
            input "theDelayDep", "number", title: "Delay setting Not Present status by (seconds)", required:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            input "mySensor", "capability.presenceSensor", title: "Select device created to hold the combined presence value", multiple:false, required:false
            paragraph "<small>* This device was automaticaly created when you entered in the app name. Look for a device with the same name as this app.</small>"
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "logEnable", "bool", title: "Enable Debug Logging", description: "Debugging", defaultValue:true, submitOnChange:true
        }
        display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def uninstalled() {
    if(logEnable) log.debug "Uninstalled ${app.label}."
	for (device in getChildDevices()) deleteChildDevice(device.deviceNetworkId)	
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        if(ArrPresenceSensors) subscribe(ArrPresenceSensors, "presence.present", arrSensorHandler)
        if(ArrPresenceSensors2) subscribe(ArrPresenceSensors2, "presence.present", arrSensorHandler2)
        if(ArrConPresenceSensors) subscribe(ArrConPresenceSensors, "contact.closed", arrSensorHandler)
        if(DepPresenceSensors) subscribe(DepPresenceSensors, "presence.not present", depSensorHandler)
        if(DepConPresenceSensors) subscribe(DepConPresenceSensors, "contact.open", arrSensorHandler)

        if(runEvery == "Every 1 Minute") runEvery1Minute(arrSensorHandler)
        if(runEvery == "Every 5 Minutes") runEvery5Minutes(arrSensorHandler)
        if(runEvery == "Every 10 Minutes") runEvery10Minutes(arrSensorHandler)
        if(runEvery == "Every 15 Minutes") runEvery15Minutes(arrSensorHandler)
        if(runEvery == "Every 30 Minutes") runEvery30Minutes(arrSensorHandler)
        if(runEvery == "Every 1 Hour") runEvery1Hour(arrSensorHandler)
        if(runEvery == "Every 3 Hours") runEvery3Hours(arrSensorHandler)
    }
}

def arrSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(ArrTriggerType == null || ArrTriggerType == "") ArrTriggerType = false
        if(ArrTriggerType2 == null || ArrTriggerType2 == "") ArrTriggerType2 = false
        if(logEnable) log.debug "In arrSensorHandler (${state.version}) - ArrTriggerType: ${ArrTriggerType} - ArrTriggerType2: ${ArrTriggerType2}"	

        unschedule()
        int theDelayArr = theDelayArr ?: 1
        int pCount = 0
        int pCount2 = 0
	int theArrNum = 0
	int theArrNum2 = 0

        if(ArrPresenceSensors || ArrConPresenceSensors) {
            if(ArrPresenceSensors) {
                preSensors = ArrPresenceSensors.size()
            } else {
                preSensors = 0
            }
            if(ArrConPresenceSensors) {
                conSensors = ArrConPresenceSensors.size()
            } else {
                conSensors = 0
            }
            asCount = preSensors + conSensors
            theArrNum = arrNumOfSensors ?: asCount
        }

        if(ArrPresenceSensors2 || ArrConPresenceSensors2) {
            if(ArrPresenceSensors2) {
                preSensors2 = ArrPresenceSensors2.size()
            } else {
                preSensors2 = 0
            }
            if(ArrConPresenceSensors2) {
                conSensors2 = ArrConPresenceSensors2.size()
            } else {
                conSensors2 = 0
            }
            asCount2 = preSensors2 + conSensors2
            theArrNum2 = arrNumOfSensors2 ?: asCount2
        }

        if(ArrTriggerType == false) {    // or
            //if(logEnable) log.debug "In arrSensorHandler - Arr: ${ArrTriggerType} - Should be FALSE for OR handler"
            if(ArrPresenceSensors) {
                ArrPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "present") {
                        state.pStatus = true	
                    }
                }
            }
            if(ArrConPresenceSensors) {
                ArrConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "closed") {
                        state.pStatus = true	
                    }
                }
            }
        }

        if(ArrTriggerType == true) {    // and
            //if(logEnable) log.debug "In arrSensorHandler - Arr: ${ArrTriggerType} - Should be TRUE for AND handler"
            if(ArrPresenceSensors) {
                ArrPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "present") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(ArrConPresenceSensors) {
                ArrConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "closed") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(logEnable) log.debug "In arrSensorHandler - Arr - sensorCount: ${asCount} - presentCount: ${pCount} - theArrNum: ${theArrNum}"
            if(pCount >= theArrNum) state.pStatus = true       
        }

        if(useAdvancedArr) {
            if(ArrTriggerType2 == false) {    // or
                //if(logEnable) log.debug "In arrSensorHandler - Arr2: ${ArrTriggerType2} - Should be FALSE for OR handler"
                if(ArrPresenceSensors2) {
                    ArrPresenceSensors2.each { it ->
                        if(it.currentValue("presence") == "present") {
                            state.pStatus = true	
                        }
                    }
                }
                if(ArrConPresenceSensors2) {
                    ArrConPresenceSensors2.each { it ->
                        if(it.currentValue("contact") == "closed") {
                            state.pStatus = true	
                        }
                    }
                }
            }

            if(ArrTriggerType2 == true) {    // and
                //if(logEnable) log.debug "In arrSensorHandler - Arr2: ${ArrTriggerType2} - Should be TRUE for AND handler"
                if(ArrPresenceSensors2) {
                    ArrPresenceSensors2.each { it ->
                        if(it.currentValue("presence") == "present") {
                            pCount2 = pCount2 + 1	
                        }
                    }
                }
                if(ArrConPresenceSensors2) {
                    ArrConPresenceSensors2.each { it ->
                        if(it.currentValue("contact") == "closed") {
                            pCount2 = pCount2 + 1	
                        }
                    }
                }
                if(logEnable) log.debug "In arrSensorHandler - Adv Arr - sensorCount: ${asCount2} - presentCount: ${pCount2} - theArrNum2: ${theArrNum2}"
                if(pCount2 >= theArrNum2) state.pStatus = true       
            }
        }

        if(state.pStatus == true) {
            if(logEnable) log.debug "In depSensorHandler - Arr - Will set status to ${state.pStatus} after a ${theDelayArr} second delay"
            runIn(theDelayArr, statusUpdateHandler)
        }
    }
}

def depSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(DepTriggerType == null || DepTriggerType == "") DepTriggerType = false
        if(logEnable) log.debug "In depSensorHandler (${state.version}) - DepTriggerType: ${DepTriggerType}"	

        unschedule()
        if(DepPresenceSensors) {
            depSensors = DepPresenceSensors.size()
        } else {
            depSensors = 0
        }
        if(DepConPresenceSensors) {
            depConSensors = DepConPresenceSensors.size()
        } else {
            depConSensors = 0
        }
        dsCount = depSensors + depConSensors
        int theDelayDep = theDelayDep ?: 1
        int theDepNum = depNumOfSensors ?: dsCount
        int pCount = 0

        if(DepTriggerType == false) {    // or
            //if(logEnable) log.debug "In depSensorHandler - Dep: ${DepTriggerType} - Should be FALSE for OR handler"
            if(DepPresenceSensors) {
                DepPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "not present") {
                        state.pStatus = false	
                    }
                }
            }
            if(DepConPresenceSensors) {
                DepConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "open") {
                        state.pStatus = false	
                    }
                }
            }
        }

        if(DepTriggerType == true) {    // and
            //if(logEnable) log.debug "In depSensorHandler - Dep: ${DepTriggerType} - Should be TRUE for AND handler"
            if(DepPresenceSensors) {
                DepPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "not present") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(DepConPresenceSensors) {
                DepConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "open") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(logEnable) log.debug "In depSensorHandler - Dep - sensorCount: ${dsCount} - notPresentCount: ${pCount} - theDepNum: ${theDepNum}"
            if(pCount >= theDepNum) state.pStatus = false       
        }

        if(state.pStatus == false) {
            if(logEnable) log.debug "In depSensorHandler - Dep - Will set status to ${state.pStatus} after a ${theDelayDep} second delay"
            runIn(theDelayDep, statusUpdateHandler)
        }
    }
}

def statusUpdateHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In statusUpdateHandler (${state.version}) - pStatus: ${state.pStatus}"
        if(state.pStatus == true) {
            def mySensorStatus = mySensor.currentValue("switch")
            if(logEnable) log.debug "In statusUpdateHandler - Sending ON for Present if needed (switch is ${mySensorStatus})"
            if(mySensorStatus == "off") mySensor.on()
        } else {
            def mySensorStatus = mySensor.currentValue("switch")
            if(logEnable) log.debug "In statusUpdateHandler - Sending OFF for Not Present if needed (switch is ${mySensorStatus})"
            if(mySensorStatus == "on") mySensor.off()
        }
    }
}

def createChildDevice() {
    if(logEnable) log.debug "In createChildDevice (${state.version})"
    if (!getChildDevice("PP" + app.getId())) {
        if(logEnable) log.warn "In createChildDevice - Child device not found - Creating device ${app.label}"
        try {
            addChildDevice("BPTWorld", "Presence Plus Driver", "PP" + app.getId(), 1234, ["name": "${app.label}", isComponent: false])
            if(logEnable) log.debug "In createChildDevice - Child device has been created! (${app.label})"
        } catch (e) { log.warn "Presence Plus unable to create device - ${e}" }
    }
}

// ********** Normal Stuff **********
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable == null) logEnable = false
    if(state.pStatus == null) state.pStatus = false
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
}
