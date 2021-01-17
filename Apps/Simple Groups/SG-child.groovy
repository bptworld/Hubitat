/**
 *  **************** Simple Groups Child App  ****************
 *
 *  Design Usage:
 *  Group just about anything. Even groups of groups!
 *
 *  Copyright 2020-2021 Bryan Turcotte (@bptworld)
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
 *  2.0.0 - 01/17/21 - Complete rewrite, Added Delay. Other adjustments
 *  1.0.4 - 01/13/21 - Adjustments
 *  1.0.3 - 12/31/20 - Fixed boo-boo with switches, Added Shades, Added pause and disable switch
 *  1.0.2 - 08/07/20 - Fixed switchOptions
 *  1.0.1 - 05/21/20 - Added more stuff
 *  1.0.0 - 05/20/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Simple Groups"
	state.version = "2.0.0"
}

definition(
    name: "Simple Groups Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Group just about anything. Even groups of groups!",
    category: "Convenience",
	parent: "BPTWorld:Simple Groups",
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
    		paragraph "Group just about anything."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the grouping results. Each device can hold data for each of the options beleow, no need for multiple devices. This is the device you'll use to control other things."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have SG create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'SG - Motion Sensors')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.switch", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Simple Groups' Driver.</small>"
            }
        }      
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Group Options")) {
            paragraph "Select any devices that share a common attribute to group."
            input "groupEvent", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
            if(groupEvent) {
                allAttrs1 = []
                allAttrs1 = groupEvent.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                input "groupAtt", "enum", title: "Attribute to track", options: allAttrs1a, required:true, multiple:false, submitOnChange:true
                if(groupAtt == "acceleration") { theAtt1 = "active";theAtt2 = "inactive" }
                if(groupAtt == "carbonMonoxide") { theAtt1 = "detected";theAtt2 = "clear" }
                if(groupAtt == "contact") { theAtt1 = "open";theAtt2 = "closed" }
                if(groupAtt == "door") { theAtt1 = "open";theAtt2 = "closed" }
                if(groupAtt == "filterStatus") { theAtt1 = "replace";theAtt2 = "normal" }
                if(groupAtt == "lock") { theAtt1 = "unlocked";theAtt2 = "locked" }
                if(groupAtt == "motion") { theAtt1 = "active";theAtt2 = "inactive" }
                if(groupAtt == "windowShade") { theAtt1 = "open";theAtt2 = "closed" }
                if(groupAtt == "shock") { theAtt1 = "detected";theAtt2 = "clear" }
                if(groupAtt == "sleeping") { theAtt1 = "not sleeping";theAtt2 = "sleeping" }
                if(groupAtt == "smoke") { theAtt1 = "detected";theAtt2 = "clear" }
                if(groupAtt == "sound") { theAtt1 = "detected";theAtt2 = "clear" }
                if(groupAtt == "switch") { theAtt1 = "on";theAtt2 = "off" }
                if(groupAtt == "tamper") { theAtt1 = "detected";theAtt2 = "clear" }
                if(groupAtt == "valve") { theAtt1 = "open";theAtt2 = "closed" }
                if(groupAtt == "water") { theAtt1 = "wet";theAtt2 = "dry" }
                
                if(theAtt1 == null) {
                    paragraph "The selected attribute isn't supported at this time. Please let me know what attribute you want added and I can see if it's possible. Thanks."
                } else {
                    input "att1", "text", title: "Attribute Value 1", required:true, defaultValue:theAtt1, submitOnChange:true
                    input "att2", "text", title: "Attribute Value 2", required:true, defaultValue:theAtt2, submitOnChange:true
                    paragraph "Condition is True when ANY device becomes '${att1}'"
                    paragraph "Condition is False when ALL devices become '${att2}'"
                    paragraph "<small>* If any Conditions are working backwards, Please let me know!</small>"
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
            input "onDelay", "number", title: "How long to be '${att1}' before triggering a change (in seconds)", required:true, defaultValue:1, submitOnChange:true
            input "offDelay", "number", title: "How long to be '${att2}' before triggering a change (in seconds)", required:true, defaultValue:120, submitOnChange:true
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
        if(groupEvent) subscribe(groupEvent, groupAtt, groupHandler)
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def groupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In groupHandler (${state.version})"
        if(groupEvent) {
            if(logEnable) log.debug "     - - - - - Start - - - - -     "
            activeCount = 0
            theOnDelay = onDelay ?: 1
            theOffDelay = offDelay ?: 120

            groupEvent.each { it ->
                if(logEnable) log.debug "In groupHandler - Working on ${it.displayName}"
                theValue = it.currentValue(groupAtt)
                if(theValue == "$att1") {
                    activeCount += 1 
                }
            }
            if(logEnable) log.debug "In groupHandler - activeCount: ${activeCount}"
            if(activeCount >= 1) {
                if(logEnable) log.debug "In groupHandler - Received ${att1} - waiting ${theOnDelay} seconds"
                unschedule(sendOffCommand)
                runIn(theOnDelay, sendOnCommand)
            } else {
                if(logEnable) log.debug "In groupHandler - Received ${att2} - waiting ${theOffDelay} seconds"
                runIn(theOffDelay, sendOffCommand)
            }
            if(logEnable) log.debug "     - - - - - End - - - - -     "
        }
    }
}

// Send Commands

def sendOnCommand() {
    if(logEnable) log.debug "In sendOnCommand - Setting '${dataDevice}' to ${att1}"
    dataDevice.sendEvent(name: groupAtt, value: att1, isStateChange: true)
}

def sendOffCommand() {
    if(logEnable) log.debug "In sendOffCommand - Setting '${dataDevice}' to ${att2}"
    dataDevice.sendEvent(name: groupAtt, value: att2, isStateChange: true)
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Simple Groups Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Simple Groups unable to create device - ${e}" }
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
