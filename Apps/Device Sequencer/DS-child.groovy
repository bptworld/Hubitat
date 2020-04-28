/**
 *  ****************  Device Sequencer Child  ****************
 *
 *  Design Usage:
 *  Turn on/off several devices in a row, with a user defined pause in between each.
 *
 *  Copyright 2018-2020 Bryan Turcotte (@bptworld)
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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
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
 *  2.0.1 - 04/27/20 - Cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  1.0.5 - 04/16/19 - Fixed some code 
 *  1.0.4 - 04/15/19 - Code cleanup
 *  1.0.3 - 03/12/19 - Fixed pause
 *  1.0.2 - 01/15/19 - Updated footer with update check and links
 *  1.0.1 - 01/12/19 - Made the Control switch stand out more.
 *  1.0.0 - 01/12/19 - Initial Release
 *
 */

def setVersion(){
    state.name = "Device Sequencer"
	state.version = "2.0.1"
}

definition(
    name: "Device Sequencer Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Turn on/off several devices in a row, with a user defined pause in between each.",
    category: "",
	parent: "BPTWorld:Device Sequencer",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Sequencer/DS-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "* Select as many devices from each group as needed.<br>* All devices selected will turn on/off with the Control Switch.<br>* When executed, group 1 will run first, then group 2, group 3, group 4 and group 5.<br>* Each group can have a different pause between devices AND a different pause between groups."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Define Switch Groups")) {
            for(x=1;x < maxGroups;x++) {
                paragraph "<b>Group $x</b>"
                input "g1Switches_$x", "capability.switch", title: "Devices to control", required: false, multiple: true, submitOnChange: true
                input "modes", "mode", title: "Change Mode to", multiple: false, required: false, submitOnChange: true
                input "hsm", "enum", title: "Set HSM", multiple: false, options:
                    ['armAway','armHome','disarm','armRules','disarmRules','disarmAll','armAll','cancelAlerts'],
                    required: false, submitOnChange: true
                
                if(g1Switches) input "timeToPause1", "number", title: "Time to pause between devices (in seconds)", required: true, defaultValue: 1
                if(g1Switches) input "timeToPause1a", "number", title: "<b>*</b> Extra Time to pause between actions(in seconds)", required: true, defaultValue: 0
                
                
            }

            
            
			paragraph "<b>Group 2</b>"
			input "g2Switches", "capability.switch", title: "Group 2 - Switches to control", required: false, multiple: true, submitOnChange: true
			if(g2Switches) input "timeToPause2", "number", title: "Group 2 - Time to pause between devices (in seconds)", required: true, defaultValue: 1
			if(g2Switches) input "timeToPause2a", "number", title: "<b>*</b> Extra Time to pause between Group 2 and 3 (in seconds)", required: true, defaultValue: 0
			paragraph "<b>Group 3</b>"
			input "g3Switches", "capability.switch", title: "Group 3 - Switches to control", required: false, multiple: true, submitOnChange: true
			if(g3Switches) input "timeToPause3", "number", title: "Group 3 - Time to pause between devices (in seconds)", required: true, defaultValue: 1
			if(g3Switches) input "timeToPause3a", "number", title: "<b>*</b> Extra Time to pause between Group 3 and 4 (in seconds)", required: true, defaultValue: 0
			paragraph "<b>Group 4</b>"
			input "g4Switches", "capability.switch", title: "Group 4 - Switches to control", required: false, multiple: true, submitOnChange: true
			if(g4Switches) input "timeToPause4", "number", title: "Group 4 - Time to pause between devices (in seconds)", required: true, defaultValue: 1
			if(g4Switches) input "timeToPause4a", "number", title: "<b>*</b> Extra Time to pause between Group 4 and 5 (in seconds)", required: true, defaultValue: 0
			paragraph "<b>Group 5</b>"
			input "g5Switches", "capability.switch", title: "Group 5 - Switches to control", required: false, multiple: true, submitOnChange: true
			if(g5Switches) input "timeToPause5", "number", title: "Group 5 - Time to pause between devices (in seconds)", required: true, defaultValue: 1
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Control Switch")) {
			input "controlSwitch", "capability.switch", title: "Select the switch to control the sequence (on/off)", required: true, multiple: false 
		} 
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
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
    unsubscribe()
	initialize()
}

def initialize() {
	subscribe(controlSwitch, "switch.on", deviceOnHandler)
	subscribe(controlSwitch, "switch.off", deviceOffHandler)
}

def deviceOnHandler(evt) {
	if(g1Switches) { 
		int delay1 = timeToPause1 * 1000
   		g1Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 1...turning on ${device}"
        	device.on()
			pauseExecution(delay1)
    	}
		int delay1a = timeToPause1a * 1000
		pauseExecution(delay1a)
	}
	if(g2Switches) { 
		int delay2 = timeToPause2 * 1000	
		g2Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 2...turning on ${device}"
        	device.on()
			pauseExecution(delay2)
    	}
		int delay2a = timeToPause2a * 1000
		pauseExecution(delay2a)
	}
	if(g3Switches) { 
		int delay3 = timeToPause3 * 1000
		g3Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 3...turning on ${device}"
        	device.on()
			pauseExecution(delay3)
    	}
		int delay3a = timeToPause3a * 1000
		pauseExecution(delay3a)
	}
	if(g4Switches) { 
		int delay4 = timeToPause4 * 1000
		g4Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 4...turning on ${device}"
        	device.on()
			pauseExecution(delay4)
    	}
		int delay4a = timeToPause4a * 1000
		pauseExecution(delay4a)
	}
	if(g5Switches) { 
		int delay5 = timeToPause5 * 1000
		g5Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 5...turning on ${device}"
        	device.on()
			pauseExecution(delay5)
    	}
	}
}

def deviceOffHandler(evt) {
	if(g1Switches) { 
		int delay1 = timeToPause1 * 1000
   		g1Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 1...turning on ${device}"
        	device.off()
			pauseExecution(delay1)
    	}
		int delay1a = timeToPause1a * 1000
		pauseExecution(delay1a)
	}
	if(g2Switches) { 
		int delay2 = timeToPause2 * 1000	
		g2Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 2...turning off ${device}"
        	device.off()
			pauseExecution(delay2)
    	}
		int delay2a = timeToPause2a * 1000
		pauseExecution(delay2a)
	}
	if(g3Switches) { 
		int delay3 = timeToPause3 * 1000
		g3Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 3...turning off ${device}"
        	device.off()
			pauseExecution(delay3)
    	}
		int delay3a = timeToPause3a * 1000
		pauseExecution(delay3a)
	}
	if(g4Switches) { 
		int delay4 = timeToPause4 * 1000
		g4Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 4...turning off ${device}"
        	device.off()
			pauseExecution(delay4)
    	}
		int delay4a = timeToPause4a * 1000
		pauseExecution(delay4a)
	}
	if(g5Switches) { 
		int delay5 = timeToPause5 * 1000
		g5Switches.each { device ->
			if(logEnable) log.debug "In deviceOnHandler 5...turning off ${device}"
        	device.off()
			pauseExecution(delay5)
    	}
	}
}

// ***** Normal Stuff *****

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

