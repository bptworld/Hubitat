/**
 *  **************** Presence Plus Child App  ****************
 *
 *  Design Usage:
 *  Creates a combined presence device that can be used with Life360 Tracker, Google Assistant, Rule Machine and More!
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
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
 *  1.0.4 - 05/05/20 - Added number of sensors required to change status
 *  1.0.3 - 05/05/20 - Added delay before status is updated
 *  1.0.2 - 04/27/20 - Cosmetic changes
 *  1.0.1 - 12/05/19 - Tightening up some code.
 *  1.0.0 - 11/01/19 - Initial release.
 *
 */

def setVersion(){
    state.name = "Presence Plus"
	state.version = "1.0.4"
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
		section(getFormat("header-green", "${getImage("Blank")}"+" Arrival Options")) {
            input "ArrTriggerType", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, submitOnChange:true
            if(ArrTriggerType) paragraph "<b>using 'AND'</b>"
            if(!ArrTriggerType) paragraph "<b>using 'OR'</b>"
			input "ArrPresenceSensors", "capability.presenceSensor", title: "Presence Sensors to combine (present)", multiple:true, required:false
            if(ArrTriggerType) input "arrNumOfSensors", "number", title: "How many sensors does it take to change status for Arrival (leave blank for All)", required:false, submitOnChange:true 
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Departure Options")) {
            input "DepTriggerType", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, submitOnChange:true
			if(DepTriggerType) paragraph "<b>using 'AND'</b>"
            if(!DepTriggerType) paragraph "<b>using 'OR'</b>"
            input "DepPresenceSensors", "capability.presenceSensor", title: "Presence Sensors to combine (not present)", multiple:true, required:false
            if(DepTriggerType) input "depNumOfSensors", "number", title: "How many sensors does it take to change status for Departed (leave blank for All)", required:false, submitOnChange:true
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Failsafe Options")) {
            paragraph "Sometimes an arrival or departure can be missed. With this option, Presence Plus will check every X minutes to see who is here based ."
            input "runEvery", "enum", title: "Check every X minutes", description: "runEvery", required:false, submitOnChange:true, options: ["Every 1 Minute", "Every 5 Minutes", "Every 10 Minutes", "Every 15 Minutes", "Every 30 Minutes", "Every 1 Hour", "Every 3 Hours"]
            input "theDelay", "number", title: "Delay setting arrival/departure status by (seconds)", required:false, submitOnChange:true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            input "mySensor", "capability.presenceSensor", title: "Select device created to hold the combined presence value", multiple:false, required:false
            paragraph "<small>* This device was automaticaly created when you entered in the app name. Look for a device with the same name as this app.</small>"
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
	initialize()
}

def initialize() {
    setDefaults()
	subscribe(ArrPresenceSensors, "presence.present", arrSensorHandler)
    subscribe(DepPresenceSensors, "presence.not present", depSensorHandler)
    
    if(runEvery == "Every 1 Minute") runEvery1Minute(arrSensorHandler)
    if(runEvery == "Every 5 Minutes") runEvery5Minutes(arrSensorHandler)
    if(runEvery == "Every 10 Minutes") runEvery10Minutes(arrSensorHandler)
    if(runEvery == "Every 15 Minutes") runEvery15Minutes(arrSensorHandler)
    if(runEvery == "Every 30 Minutes") runEvery30Minutes(arrSensorHandler)
    if(runEvery == "Every 1 Hour") runEvery1Hour(arrSensorHandler)
    if(runEvery == "Every 3 Hours") runEvery3Hours(arrSensorHandler)
}

def arrSensorHandler(evt) {
    if(ArrTriggerType == null || ArrTriggerType == "") ArrTriggerType = false
    if(logEnable) log.debug "In arrSensorHandler (${state.version}) - ArrTriggerType: ${ArrTriggerType}"	

    unschedule()
    asCount = ArrPresenceSensors.size()
    int theDelay = theDelay ?: 1
    int theArrNum = theArrNum ?: asCount
    int pCount = 0
    
    if(ArrTriggerType == false) {    // or
        //if(logEnable) log.debug "In arrSensorHandler - Arr: ${ArrTriggerType} - Should be FALSE for OR handler"
	    ArrPresenceSensors.each { it ->
		    if(it.currentValue("presence") == "present") {
			    state.pStatus = true	
            }
	    }
    }
    
    if(ArrTriggerType == true) {    // and
        //if(logEnable) log.debug "In arrSensorHandler - Arr: ${ArrTriggerType} - Should be TRUE for AND handler"
	    ArrPresenceSensors.each { it ->
		    if(it.currentValue("presence") == "present") {
			    pCount = pCount + 1	
            }
	    }
        if(logEnable) log.debug "In arrSensorHandler - Arr - sensorCount: ${asCount} - presentCount: ${pCount} - theArrNum: ${theArrNum}"
        if(pCount >= theArrNum) state.pStatus = true       
    }
    
    if(state.pStatus == true) {
        if(logEnable) log.debug "In depSensorHandler - Arr - Will set status to ${state.pStatus} after a ${theDelay} second delay"
        runIn(theDelay, statusUpdateHandler)
    }
}

def depSensorHandler(evt) {
    if(DepTriggerType == null || DepTriggerType == "") DepTriggerType = false
    if(logEnable) log.debug "In depSensorHandler (${state.version}) - DepTriggerType: ${DepTriggerType}"	

    unschedule()
    dsCount = DepPresenceSensors.size()
    int theDelay = theDelay ?: 1
    int theDepNum = theDepNum ?: dsCount
    int pCount = 0
    
    if(DepTriggerType == false) {    // or
        //if(logEnable) log.debug "In depSensorHandler - Dep: ${DepTriggerType} - Should be FALSE for OR handler"
	    DepPresenceSensors.each { it ->
		    if(it.currentValue("presence") == "not present") {
			    state.pStatus = false	
            }
	    }
    }
    
    if(DepTriggerType == true) {    // and
        //if(logEnable) log.debug "In depSensorHandler - Dep: ${DepTriggerType} - Should be TRUE for AND handler"
	    DepPresenceSensors.each { it ->
		    if(it.currentValue("presence") == "not present") {
			    pCount = pCount + 1	
            }
	    }
        if(logEnable) log.debug "In depSensorHandler - Dep - sensorCount: ${dsCount} - notPresentCount: ${pCount} - theDepNum: ${theDepNum}"
        if(pCount >= theDepNum) state.pStatus = false       
    }
    
    if(state.pStatus == false) {
        if(logEnable) log.debug "In depSensorHandler - Dep - Will set status to ${state.pStatus} after a ${theDelay} second delay"
        runIn(theDelay, statusUpdateHandler)
    }
}

def statusUpdateHandler() {
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
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
