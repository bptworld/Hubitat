/**
 *  **************** Simple Multi Tile Child App  ****************
 *
 *  Design Usage:
 *  Create a simple multi device tile with just a few clicks
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
 *  1.0.1 - 06/05/21 - Fixed an error
 *  1.0.0 - 06/04/21 - Initial release.
 *
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Simple Multi Tile Child"
	state.version = "1.0.1"
}

definition(
    name: "Simple Multi Tile Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a simple multi device tile with just a few clicks",
    category: "Convenience",
	parent: "BPTWorld:Simple Multi Tile",
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
    		paragraph "Create a simple multi device tile with just a few clicks"
            paragraph "Remember, this is a SIMPLE multi tile app.  If you need more, please take a look at Tile Master."
		}

        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the transformed results."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have DT create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
                input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'SMT - Front Door')", required:true, submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Simple Multi Tile Driver'.</small>"
            }
        } 
       
        section(getFormat("header-green", "${getImage("Blank")}"+" Device to Track")) {
            input "device1", "capability.*", title: "Select a device", required:false, multiple:false, submitOnChange:true
            if(device1) {
                allAttrs1 = []
                allAttrs1 = device1.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                input "deviceAtt1a", "enum", title: "Attribute to track", options: allAttrs1a, required:true, multiple:false, submitOnChange:true, width:4
                input "deviceAtt1b", "enum", title: "Attribute to track", options: allAttrs1a, required:false, multiple:false, submitOnChange:true, width:4
                input "deviceAtt1c", "enum", title: "Attribute to track", options: allAttrs1a, required:false, multiple:false, submitOnChange:true, width:4
            }
            
            input "device2", "capability.*", title: "Select a device", required:false, multiple:false, submitOnChange:true
            if(device2) {
                allAttrs2 = []
                allAttrs2 = device2.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs2a = allAttrs2.sort { a, b -> a.value <=> b.value }
                input "deviceAtt2a", "enum", title: "Attribute to track", options: allAttrs2a, required:true, multiple:false, submitOnChange:true, width:4
                input "deviceAtt2b", "enum", title: "Attribute to track", options: allAttrs2a, required:false, multiple:false, submitOnChange:true, width:4
                input "deviceAtt2c", "enum", title: "Attribute to track", options: allAttrs2a, required:false, multiple:false, submitOnChange:true, width:4
            }
            
            input "device3", "capability.*", title: "Select a device", required:false, multiple:false, submitOnChange:true
            if(device3) {
                allAttrs3 = []
                allAttrs3 = device3.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs3a = allAttrs3.sort { a, b -> a.value <=> b.value }
                input "deviceAtt3a", "enum", title: "Attribute to track", options: allAttrs3a, required:true, multiple:false, submitOnChange:true, width:4
                input "deviceAtt3b", "enum", title: "Attribute to track", options: allAttrs3a, required:false, multiple:false, submitOnChange:true, width:4
                input "deviceAtt3c", "enum", title: "Attribute to track", options: allAttrs3a, required:false, multiple:false, submitOnChange:true, width:4
            }
            
            input "device4", "capability.*", title: "Select a device", required:false, multiple:false, submitOnChange:true
            if(device4) {
                allAttrs4 = []
                allAttrs4 = device4.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs4a = allAttrs4.sort { a, b -> a.value <=> b.value }
                input "deviceAtt4a", "enum", title: "Attribute to track", options: allAttrs4a, required:true, multiple:false, submitOnChange:true, width:4
                input "deviceAtt4b", "enum", title: "Attribute to track", options: allAttrs4a, required:false, multiple:false, submitOnChange:true, width:4
                input "deviceAtt4c", "enum", title: "Attribute to track", options: allAttrs4a, required:false, multiple:false, submitOnChange:true, width:4
            }
            
            input "device5", "capability.*", title: "Select a device", required:false, multiple:false, submitOnChange:true
            if(device5) {
                allAttrs5 = []
                allAttrs5 = device5.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs5a = allAttrs5.sort { a, b -> a.value <=> b.value }
                input "deviceAtt5a", "enum", title: "Attribute to track", options: allAttrs5a, required:true, multiple:false, submitOnChange:true, width:4
                input "deviceAtt5b", "enum", title: "Attribute to track", options: allAttrs5a, required:false, multiple:false, submitOnChange:true, width:4
                input "deviceAtt5c", "enum", title: "Attribute to track", options: allAttrs5a, required:false, multiple:false, submitOnChange:true, width:4
            }
        }
               
        section(getFormat("header-green", "${getImage("Blank")}"+" Filter Options")) {
            paragraph "To save characters, enter in a filter to remove characters from each device name. Must be exact, including case.<br><small>ie. 'Motion Sensor', 'Bedroom', 'Contact'</small>"
			input "bFilter1", "text", title: "Filter 1", required:false, submitOnChange:true, width:6
            input "bFilter2", "text", title: "Filter 2", required:false, submitOnChange:true, width:6
            
            input "bFilter3", "text", title: "Filter 3", required:false, submitOnChange:true, width:6
            input "bFilter4", "text", title: "Filter 4", required:false, submitOnChange:true, width:6
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

        section(getFormat("header-green", "${getImage("Blank")}"+" The Tile")) {
            theDeviceHandler()
            paragraph "${state.theTable}", width:10
            paragraph "<hr>"
            paragraph "Table Count: ${state.tableCount}<br>* Must be under 1024 to show on a Tile."
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
        if(device1) {
            if(deviceAtt1a) subscribe(device1, deviceAtt1a, theDeviceHandler)
            if(deviceAtt1b) subscribe(device1, deviceAtt1b, theDeviceHandler)
            if(deviceAtt1c) subscribe(device1, deviceAtt1c, theDeviceHandler)
        }
        if(device2) {
            if(deviceAtt2a) subscribe(device2, deviceAtt2a, theDeviceHandler)
            if(deviceAtt2b) subscribe(device2, deviceAtt2b, theDeviceHandler)
            if(deviceAtt2c) subscribe(device2, deviceAtt2c, theDeviceHandler)
        }
        if(device3) {
            if(deviceAtt3a) subscribe(device3, deviceAtt3a, theDeviceHandler)
            if(deviceAtt3b) subscribe(device3, deviceAtt3b, theDeviceHandler)
            if(deviceAtt3c) subscribe(device3, deviceAtt3c, theDeviceHandler)
        }
        if(device4) {
            if(deviceAtt4a) subscribe(device4, deviceAtt4a, theDeviceHandler)
            if(deviceAtt4b) subscribe(device4, deviceAtt4b, theDeviceHandler)
            if(deviceAtt4c) subscribe(device4, deviceAtt4c, theDeviceHandler)
        }
        if(device5) {
            if(deviceAtt5a) subscribe(device5, deviceAtt5a, theDeviceHandler)
            if(deviceAtt5b) subscribe(device5, deviceAtt5b, theDeviceHandler)
            if(deviceAtt5c) subscribe(device5, deviceAtt5c, theDeviceHandler)
        }
    }
}

def theDeviceHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In theDeviceHandler (${state.version})"
        if(evt) whatHappened = evt.value
        state.recognised = false
        if(logEnable) log.debug "In theDeviceHandler - whatHappened: ${whatHappened}"
        
        if(device1) { 
            currentDevice1a = device1.currentValue(deviceAtt1a) ?: "-"
            currentDevice1b = device1.currentValue(deviceAtt1b) ?: "-"
            currentDevice1c = device1.currentValue(deviceAtt1c) ?: "-"
        }
        if(device2) { 
            currentDevice2a = device2.currentValue(deviceAtt2a) ?: "-"
            currentDevice2b = device2.currentValue(deviceAtt2b) ?: "-"
            currentDevice2c = device2.currentValue(deviceAtt2c) ?: "-"
        }
        if(device3) { 
            currentDevice3a = device3.currentValue(deviceAtt3a) ?: "-"
            currentDevice3b = device3.currentValue(deviceAtt3b) ?: "-"
            currentDevice3c = device3.currentValue(deviceAtt3c) ?: "-"
        }
        if(device4) {
            currentDevice4a = device4.currentValue(deviceAtt4a) ?: "-"
            currentDevice4b = device4.currentValue(deviceAtt4b) ?: "-"
            currentDevice4c = device4.currentValue(deviceAtt4c) ?: "-"
        }
        if(device5) { 
            currentDevice5a = device5.currentValue(deviceAtt5a) ?: "-"
            currentDevice5b = device5.currentValue(deviceAtt5b) ?: "-"
            currentDevice5c = device5.currentValue(deviceAtt5c) ?: "-"
        }
            
        if(device1) {
            theName1 = device1.displayName          
            if(bFilter1) { theName1 = theName1.replace("${bFilter1}", "") }
            if(bFilter2) { theName1 = theName1.replace("${bFilter2}", "") }
            if(bFilter3) { theName1 = theName1.replace("${bFilter3}", "") }
            if(bFilter4) { theName1 = theName1.replace("${bFilter4}", "") }
        }
        if(device2) {
            theName2 = device2.displayName              
            if(bFilter1) { theName2 = theName2.replace("${bFilter1}", "") }
            if(bFilter2) { theName2 = theName2.replace("${bFilter2}", "") }
            if(bFilter3) { theName2 = theName2.replace("${bFilter3}", "") }
            if(bFilter4) { theName2 = theName2.replace("${bFilter4}", "") }
        }
        if(device3) {
            theName3 = device3.displayName              
            if(bFilter1) { theName3 = theName3.replace("${bFilter1}", "") }
            if(bFilter2) { theName3 = theName3.replace("${bFilter2}", "") }
            if(bFilter3) { theName3 = theName3.replace("${bFilter3}", "") }
            if(bFilter4) { theName3 = theName3.replace("${bFilter4}", "") }
        }
        if(device4) {
            theName4 = device4.displayName              
            if(bFilter1) { theName4 = theName4.replace("${bFilter1}", "") }
            if(bFilter2) { theName4 = theName4.replace("${bFilter2}", "") }
            if(bFilter3) { theName4 = theName4.replace("${bFilter3}", "") }
            if(bFilter4) { theName4 = theName4.replace("${bFilter4}", "") }
        }
        if(device5) {
            theName5 = device5.displayName              
            if(bFilter1) { theName5 = theName5.replace("${bFilter1}", "") }
            if(bFilter2) { theName5 = theName5.replace("${bFilter2}", "") }
            if(bFilter3) { theName5 = theName5.replace("${bFilter3}", "") }
            if(bFilter4) { theName5 = theName5.replace("${bFilter4}", "") }
        }
        state.theTable =  "<table width=100%><tr><td width=55% align=center><u>Device</u><td width=15%><u>Value 1</u><td width=15%><u>Value 2</u><td width=15%><u>Value 3</u>"
        if(device1) state.theTable += "<tr><td>${theName1}<td>${currentDevice1a}<td>${currentDevice1b}<td>${currentDevice1c}"
        if(device2) state.theTable += "<tr><td>${theName2}<td>${currentDevice2a}<td>${currentDevice2b}<td>${currentDevice2c}"
        if(device3) state.theTable += "<tr><td>${theName3}<td>${currentDevice3a}<td>${currentDevice3b}<td>${currentDevice3c}"
        if(device4) state.theTable += "<tr><td>${theName4}<td>${currentDevice4a}<td>${currentDevice4b}<td>${currentDevice4c}"
        if(device5) state.theTable += "<tr><td>${theName5}<td>${currentDevice5a}<td>${currentDevice5b}<td>${currentDevice5c}"
        state.theTable += "</table>"
        
        if(state.theTable) { state.tableCount = state.theTable.size() }
        if(logEnable) log.debug "In theDeviceHandler - tableCount: ${state.tableCount}"
        if(dataDevice) {
            dataDevice.sendEvent(name: "bpt-simpleMultiTile", value: state.theTable, isStateChange: true)
            dataDevice.sendEvent(name: "tableCount", value: state.tableCount, isStateChange: true)  
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
            addChildDevice("BPTWorld", "Simple Multi Tile Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Simple Multi Tile was unable to create device - ${e}" }
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
