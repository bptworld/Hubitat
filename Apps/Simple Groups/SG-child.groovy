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
	state.version = "1.0.4"
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
    page(name: "contactSensorOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "groupOfGroupsOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "lockOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "motionSensorOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "shadeOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "switchOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "waterSensorOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Group just about anything. Even groups of groups!"
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
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Grouping Options")) {
            if(contactSensors) {
                href "contactSensorOptions", title:"${getImage("optionsGreen")} Setup Contact Sensor Groups", description:"Click here for Options"
            } else {
                href "contactSensorOptions", title:"${getImage("optionsRed")} Setup Contact Sensor Groups", description:"Click here for Options"
            }
            
            if(locks) {
                href "lockOptions", title:"${getImage("optionsGreen")} Setup Lock Groups", description:"Click here for Options"
            } else {
                href "lockOptions", title:"${getImage("optionsRed")} Setup Lock Groups", description:"Click here for Options"
            }
            
            if(motionSensors) {
                href "motionSensorOptions", title:"${getImage("optionsGreen")} Setup Motion Sensor Groups", description:"Click here for Options"
            } else {
                href "motionSensorOptions", title:"${getImage("optionsRed")} Setup Motion Sensor Groups", description:"Click here for Options"
            }
            
            if(shades) {
                href "shadeOptions", title:"${getImage("optionsGreen")} Setup Shade Groups", description:"Click here for Options"
            } else {
                href "shadeOptions", title:"${getImage("optionsRed")} Setup Shade Groups", description:"Click here for Options"
            }
            
            if(switches) {
                href "switchOptions", title:"${getImage("optionsGreen")} Setup Switch Groups", description:"Click here for Options"
            } else {
                href "switchOptions", title:"${getImage("optionsRed")} Setup Switch Groups", description:"Click here for Options"
            }
            
            if(waterSensors) {
                href "waterSensorOptions", title:"${getImage("optionsGreen")} Setup Water Sensor Groups", description:"Click here for Options"
            } else {
                href "waterSensorOptions", title:"${getImage("optionsRed")} Setup Water Sensor Groups", description:"Click here for Options"
            }
            
            if(group1 || group2 || group3) {
                href "groupOfGroupsOptions", title:"${getImage("optionsGreen")} Setup Goup of Groups", description:"Click here for Options"
            } else {
                href "groupOfGroupsOptions", title:"${getImage("optionsRed")} Setup Group of Groups", description:"Click here for Options"
            }
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

def contactSensorOptions() {
    dynamicPage(name: "contactSensorOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Contact Sensor Grouping Options")) {
            paragraph "Setup a group of Contact Sensors to report as one."
            input "contactSensors", "capability.contactSensor", title: "Select Contact Sensor Device(s)", required:false, multiple:true, submitOnChange:true           
            paragraph "<hr>"
        }
    }
}

def groupOfGroupsOptions() {
    dynamicPage(name: "groupOfGroupsOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Groups of Groups Options")) {
            paragraph "Setup the Groups of Groups to report as one."
            
            input "group1", "enum", title: "Select Group 1 Options", options: [
                "contactGroup":"Contact Sensor Group",
                "lockGroup":"Lock Group",
                "motionGroup":"Motion Sensor Group",
                "switchGroup":"Switch Group",
                "waterGroup":"Water Group"
            ], required:false, multiple:true, submitOnChange:true
            
            input "group2", "enum", title: "Select Group 2 Options", options: [
                "contactGroup":"Contact Sensor Group",
                "lockGroup":"Lock Group",
                "motionGroup":"Motion Sensor Group",
                "switchGroup":"Switch Group",
                "waterGroup":"Water Group"
            ], required:false, multiple:true, submitOnChange:true
            
            input "group3", "enum", title: "Select Group 3 Options", options: [
                "contactGroup":"Contact Sensor Group",
                "lockGroup":"Lock Group",
                "motionGroup":"Motion Sensor Group",
                "switchGroup":"Switch Group",
                "waterGroup":"Water Group"
            ], required:false, multiple:true, submitOnChange:true
            paragraph "<hr>"
        }
    }
}

def lockOptions() {
    dynamicPage(name: "lockOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Lock Grouping Options")) {
            paragraph "Setup a group of Locks to report as one."
            input "locks", "capability.lock", title: "Select Lock Device(s)", required:false, multiple:true, submitOnChange:true         
            paragraph "<hr>"
        }
    }
}

def motionSensorOptions() {
    dynamicPage(name: "motionSensorOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Motion Sensor Grouping Options")) {
            paragraph "Setup a group of Motion Sensors to report as one."
            input "motionSensors", "capability.motionSensor", title: "Select Motion Sensor Device(s)", required:false, multiple:true, submitOnChange:true              
            paragraph "<hr>"
        }
    }
}

def shadeOptions() {
    dynamicPage(name: "shadeOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Shade Grouping Options")) {
            paragraph "Setup a group of Shades to report as one."
            input "shades", "capability.windowShade", title: "Select Shade Device(s)", required:false, multiple:true, submitOnChange:true              
            paragraph "<hr>"
        }
    }
}

def switchOptions() {
    dynamicPage(name: "switchOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Switch Grouping Options")) {
            paragraph "Setup a group of Switches to report as one."
            input "switches", "capability.switch", title: "Select Switch Device(s)", required:false, multiple:true, submitOnChange:true               
            paragraph "<hr>"
        }
    }
}

def waterSensorOptions() {
    dynamicPage(name: "waterSensorOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Water Sensor Grouping Options")) {
            paragraph "Setup a group of Water Sensors to report as one."
            input "waterSensors", "capability.waterSensor", title: "Select Water Sensor Device(s)", required:false, multiple:true, submitOnChange:true               
            paragraph "<hr>"
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
        if(contactSensors) subscribe(contactSensors, "contact", contactGroupHandler)
        if(locks) subscribe(locks, "lock", lockGroupHandler)
        if(motionSensors) subscribe(motionSensors, "motion", motionGroupHandler)
        if(shades) subscribe(shades, "windowShade", shadeGroupHandler)
        if(switches) subscribe(switches, "switch", switchGroupHandler)
        if(waterSensors) subscribe(waterSensors, "water", waterGroupHandler)
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def contactGroupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In contactGroupHandler (${state.version})"
        if(contactSensors) {
            if(logEnable) log.debug "     - - - - - Start (Contact Grouping) - - - - -     "

            data = false

            contactSensors.each { it ->
                if(logEnable) log.debug "In contactGroupHandler - Working on ${it.displayName}"
                theValue = it.currentValue("contact")
                if(theValue == "open") {
                    data = true
                }
            }

            if(data) {
                if(logEnable) log.debug "In contactGroupHandler - Setting group device to Open"
                theValue = dataDevice.currentValue("contact")
                if(theValue == "closed") dataDevice.virtualContact("open")
            } else {
                if(logEnable) log.debug "In contactGroupHandler - Setting group device to Closed"
                theValue = dataDevice.currentValue("contact")
                if(theValue == "open") dataDevice.virtualContact("closed")
            }
            if(logEnable) log.debug "     - - - - - End (Contact Grouping) - - - - -     "
        }
        groupOfGroupsHandler()
    }
}

def lockGroupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In lockGroupHandler (${state.version})"
        if(locks) {
            if(logEnable) log.debug "     - - - - - Start (Lock Grouping) - - - - -     "

            data = false

            locks.each { it ->
                if(logEnable) log.debug "In lockGroupHandler - Working on ${it.displayName}"
                theValue = it.currentValue("lock")
                if(theValue == "unlocked") {
                    data = true
                }
            }

            if(data) {
                if(logEnable) log.debug "In lockGroupHandler - Setting group device to Unlocked"
                theValue = dataDevice.currentValue("lock")
                if(theValue == "locked") dataDevice.virtualLock("unlocked")
            } else {
                if(logEnable) log.debug "In lockGroupHandler - Setting group device to Locked"
                theValue = dataDevice.currentValue("lock")
                if(theValue == "unlocked") dataDevice.virtualLock("locked")
            }
            if(logEnable) log.debug "     - - - - - End (Lock Grouping) - - - - -     "
        }
        groupOfGroupsHandler()
    }
}

def motionGroupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In motionGroupHandler (${state.version})"
        if(motionSensors) {
            if(logEnable) log.debug "     - - - - - Start (Motion Grouping) - - - - -     "

            data = false

            motionSensors.each { it ->
                if(logEnable) log.debug "In motionGroupHandler - Working on ${it.displayName}"
                theValue = it.currentValue("motion")
                if(theValue == "active") {
                    data = true
                }
            }

            if(data) {
                if(logEnable) log.debug "In motionGroupHandler - Setting group device to Active"
                theValue = dataDevice.currentValue("motion")
                if(theValue == "inactive") dataDevice.virtualMotion("active")
            } else {
                if(logEnable) log.debug "In motionGroupHandler - Setting group device to Inactive"
                theValue = dataDevice.currentValue("motion")
                if(theValue == "active") dataDevice.virtualMotion("inactive")
            }
            if(logEnable) log.debug "     - - - - - End (Motion Grouping) - - - - -     "
        }
        groupOfGroupsHandler()
    }
}

def shadeGroupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In shadeGroupHandler (${state.version})"
        if(shades) {
            if(logEnable) log.debug "     - - - - - Start (Shade Grouping) - - - - -     "

            data = false

            shades.each { it ->
                if(logEnable) log.debug "In shadeGroupHandler - Working on ${it.displayName}"
                theValue = it.currentValue("switch")
                if(theValue == "on") {
                    data = true
                }
            }

            if(data) {
                if(logEnable) log.debug "In shadeGroupHandler - Setting group device to Open"
                theValue = dataDevice.currentValue("switch")
                if(theValue == "closed") dataDevice.virtualShade("open")
            } else {
                if(logEnable) log.debug "In shadeGroupHandler - Setting group device to Closed"
                theValue = dataDevice.currentValue("switch")
                if(theValue == "open") dataDevice.virtualShade("closed")
            }
            if(logEnable) log.debug "     - - - - - End (Shade Grouping) - - - - -     "
        }
        groupOfGroupsHandler()
    }
}

def switchGroupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In switchGroupHandler (${state.version})"
        if(switches) {
            if(logEnable) log.debug "     - - - - - Start (Switch Grouping) - - - - -     "

            data = false

            switches.each { it ->
                if(logEnable) log.debug "In switchGroupHandler - Working on ${it.displayName}"
                theValue = it.currentValue("switch")
                if(theValue == "on") {
                    data = true
                }
            }

            if(data) {
                if(logEnable) log.debug "In switchGroupHandler - Setting group device to On"
                theValue = dataDevice.currentValue("switch")
                if(theValue == "off") dataDevice.virtualSwitch("on")
            } else {
                if(logEnable) log.debug "In switchGroupHandler - Setting group device to Off"
                theValue = dataDevice.currentValue("switch")
                if(theValue == "on") dataDevice.virtualSwitch("off")
            }
            if(logEnable) log.debug "     - - - - - End (Switch Grouping) - - - - -     "
        }
        groupOfGroupsHandler()
    }
}

def waterGroupHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In waterGroupHandler (${state.version})"
        if(waterSensors) {
            if(logEnable) log.debug "     - - - - - Start (Water Grouping) - - - - -     "

            data = false

            waterSensors.each { it ->
                if(logEnable) log.debug "In waterGroupHandler - Working on ${it.displayName}"
                theValue = it.currentValue("water")
                if(theValue == "wet") {
                    data = true
                }
            }

            if(data) {
                if(logEnable) log.debug "In waterGroupHandler - Setting group device to Wet"
                theValue = dataDevice.currentValue("water")
                if(theValue == "dry") dataDevice.virtualWater("wet")
            } else {
                if(logEnable) log.debug "In waterGroupHandler - Setting group device to Dry"
                theValue = dataDevice.currentValue("water")
                if(theValue == "wet") dataDevice.virtualWater("dry")
            }
            if(logEnable) log.debug "     - - - - - End (Water Grouping) - - - - -     "
        }
        groupOfGroupsHandler()
    }
}


def groupOfGroupsHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In groupOfGroupsHandler (${state.version})"
        if(group1) {
            if(logEnable) log.debug "     - - - - - Start (Group of Groups Grouping) - - - - -     "

            for(x=1;x<4;x++) {
                data = false
                if(x == 1) {
                    groups = group1
                    virtualGroup = "virtualGroup1"
                }
                if(x == 2) {
                    groups = group2
                    virtualGroup = "virtualGroup2"
                }
                if(x == 3) {
                    groups = group3
                    virtualGroup = "virtualGroup3"
                }

                if(groups == null) {
                    if(logEnable) log.debug "In groupOfGroupsHandler - Group ${x} is blank so skipping"
                } else {
                    groups.each { it ->
                        if(logEnable) log.debug "In groupOfGroupsHandler - Working on Group ${x} - ${it}"
                        if(it.contains("contactGroup")) {
                            theValue = dataDevice.currentValue("contact")
                            if(theValue == "open") data = true
                        }

                        if(it.contains("lockGroup")) {
                            theValue = dataDevice.currentValue("lock")
                            if(theValue == "unlocked") data = true
                        }

                        if(it.contains("motionGroup")) {
                            theValue = dataDevice.currentValue("motion")
                            if(theValue == "active") data = true
                        }

                        if(it.contains("shadeGroup")) {
                            theValue = dataDevice.currentValue("shade")
                            if(theValue == "on") data = true
                        }

                        if(it.contains("switchGroup")) {
                            theValue = dataDevice.currentValue("switch")
                            if(theValue == "on") data = true
                        }

                        if(it.contains("waterGroup")) {
                            theValue = dataDevice.currentValue("water")
                            if(theValue == "wet") data = true
                        }

                        if(data) {
                            if(logEnable) log.debug "In groupOfGroupsHandler - Setting group ${x} device to True"
                            dataDevice."${virtualGroup}"("true")
                        } else {
                            if(logEnable) log.debug "In groupOfGroupsHandler - Setting group ${x} device to False"
                            dataDevice."${virtualGroup}"("false")
                        }
                    }
                }
            }
            if(logEnable) log.debug "     - - - - - End (Group of Groups Grouping) - - - - -     "
        }
    }
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
