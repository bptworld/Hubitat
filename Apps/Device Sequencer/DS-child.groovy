/**
 *  ****************  Device Sequencer Child  ****************
 *
 *  Design Usage:
 *  Turn on/off several devices in a row, with a user defined pause in between each.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to Jody Albritton for the original 'One At A Time Please' code that I based this app off of.
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.1 - 01/12/19 - Made the Control switch stand out more.
 *  V1.0.0 - 01/12/19 - Initial Release
 *
 */

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
    )

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Device Sequencer</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "* Select as many devices from each group as needed.<br>* All devices selected will turn on/off with the Control Switch.<br>* When executed, group 1 will run first, then group 2, group 3, group 4 and group 5.<br>* Each group can have a different pause between devices AND a different pause between groups."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Define Switch Groups")) {
			paragraph "<b>Group 1</b>"
			input "g1Switches", "capability.switch", title: "Group 1 - Switches to control", required: true, multiple: true, submitOnChange: true
			if(g1Switches) input "timeToPause1", "number", title: "Group 1 - Time to pause between devices (in seconds)", required: true, defaultValue: 1
			if(g1Switches) input "timeToPause1a", "number", title: "<b>*</b> Extra Time to pause between Group 1 and 2 (in seconds)", required: true, defaultValue: 0
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
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=35 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	logCheck()
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
			LOGDEBUG("In deviceOnHandler 1...turning on ${device}")
        	device.on()
			pauseExecution(delay1)
    	}
		int delay1a = timeToPause1a * 1000
		pauseExecution(delay1a)
	}
	if(g2Switches) { 
		int delay2 = timeToPause2 * 1000	
		g2Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 2...turning on ${device}")
        	device.on()
			pauseExecution(delay2)
    	}
		int delay2a = timeToPause2a * 1000
		pauseExecution(delay2a)
	}
	if(g3Switches) { 
		int delay3 = timeToPause3 * 1000
		g3Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 3...turning on ${device}")
        	device.on()
			pauseExecution(delay3)
    	}
		int delay3a = timeToPause3a * 1000
		pauseExecution(delay3a)
	}
	if(g4Switches) { 
		int delay4 = timeToPause4 * 1000
		g4Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 4...turning on ${device}")
        	device.on()
			pauseExecution(delay4)
    	}
		int delay4a = timeToPause4a * 1000
		pauseExecution(delay4a)
	}
	if(g5Switches) { 
		int delay5 = timeToPause5 * 1000
		g5Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 5...turning on ${device}")
        	device.on()
			pauseExecution(delay5)
    	}
	}
}

def deviceOffHandler(evt) {
	if(g1Switches) { 
		int delay1 = timeToPause1 * 1000
   		g1Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 1...turning on ${device}")
        	device.off()
			pauseExecution(delay1)
    	}
		int delay1a = timeToPause1a * 1000
		pauseExecution(delay1a)
	}
	if(g2Switches) { 
		int delay2 = timeToPause2 * 1000	
		g2Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 2...turning off ${device}")
        	device.off()
			pauseExecution(delay2)
    	}
		int delay2a = timeToPause2a * 1000
		pauseExecution(delay2a)
	}
	if(g3Switches) { 
		int delay3 = timeToPause3 * 1000
		g3Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 3...turning off ${device}")
        	device.off()
			pauseExecution(delay3)
    	}
		int delay3a = timeToPause3a * 1000
		pauseExecution(delay3a)
	}
	if(g4Switches) { 
		int delay4 = timeToPause4 * 1000
		g4Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 4...turning off ${device}")
        	device.off()
			pauseExecution(delay4)
    	}
		int delay4a = timeToPause4a * 1000
		pauseExecution(delay4a)
	}
	if(g5Switches) { 
		int delay5 = timeToPause5 * 1000
		g5Switches.each { device ->
			LOGDEBUG("In deviceOnHandler 5...turning off ${device}")
        	device.off()
			pauseExecution(delay5)
    	}
	}
}

// ***** Normal Stuff *****

def pauseOrNot(){
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Sequencer - App Version: 1.0.1 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a></div>"
	}
}
