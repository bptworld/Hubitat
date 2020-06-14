/**
 *  ****************  Send IP2IR Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018-2020 Bryan Turcotte (@bptworld)
 *
 *  Thanks to Carson Dallum's (@cdallum) for the original IP2IR driver code that I based my driver off of.
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  2.0.4 - 06/13/20 - Changes to Digit Separator
 *  2.0.3 - 06/11/20 - Added 'Digit Separator' option, fixed problem with auto created device
 *  2.0.2 - 04/27/20 - Cosmetic changes
 *  2.0.1 - 10/20/19 - Time for an overhaul, this was one of the first big apps I created and it's definitely time for some changes!
        - removed all button code since Google Home only uses switches. Switches are also much more versatile (on and off options)
        - App now creates all the virtual switches for you based on child app name (also will delete it if app is uninstalled)
        - Logs automatically turn off after 30 minutes
        - Moved telnetDevice to parent app
        - Code cleanup
        - Cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  --
 *  1.0.0 - 10/15/18 - Initial release
 */
 
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Send IP2IR"
	state.version = "2.0.4"
}

definition(
    name: "Send IP2IR Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "This app is designed to send commands to an iTach IP2IR device.",
    category: "",
	parent: "BPTWorld:Send IP2IR",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Send%20IP2IR/SIP2IR%20Child.groovy",
)

preferences {
    page(name: "pageConfig")
}
	
def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display()
        state.deviceCreatedMessage = ""
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "There are 3 types of Triggers that can be used."
        	paragraph "<b>Switch (on/off):</b><br>To turn anything on/off. ie. Television, Stereo, Cable Box, etc. Remember, it's okay to put the same code in box on and off if necessary."
            paragraph "<b>Switch (auto off):</b><br>This works just like a button, press to turn on and then in 1 second it will turn off."
        	paragraph "<b>Channel:</b><br>Used to send 1 to 4 commands at the same time. This is used to send Channel numbers based on the Presets in the Parent app. Switch will also auto turn off after 1 second."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Trigger")) {
			paragraph "<b>Select the Trigger Type to Activate to send the IR command</b>"
            input "triggerMode", "enum", required: true, title: "Select Trigger Type", submitOnChange: true,  options: ["Switch - on/off", "Switch - auto off", "Channel"]
        }
        if(triggerMode) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Name Child App and Create Virtual Device")) {
                label title: "Enter a name for this child app.<br>This will also be the name of the virtual switch that will control the IR command.<br><small>- Switch will be automaticaly created for you!</small>", required:true, submitOnChange:true
                if(app.label) checkVirtualChild()
                paragraph "${state.deviceCreatedMessage}"
                if(app.label) input "switch1", "capability.switch", title: "Select Trigger Device just created", required: true, multiple: false
            }
        }
		       
        if(triggerMode == "Switch - on/off"){
	        section(getFormat("header-green", "${getImage("Blank")}"+" IR Command to Send")) {
            	input "msgToSendOn", "text", required: true, title: "IR Code to Send - ON", defaultValue: ""
                input "msgToSendOff", "text", required: true, title: "IR Code to Send - OFF", defaultValue: ""
                input "mCommands", "bool", title: "Send the IR Code multiple times (think volume control)", required:true, defaultValue:false, submitOnChange:true
				if(mCommands){
					input "xTimesOn", "number", title: "How many times to send 'On' IR Code (1 to 10)", required:false, defaultValue:1, range: '1..10'
					input "xTimesOff", "number", title: "How many times to send 'Off' IR Code (1 to 10)", required: false, defaultValue:1, range: '1..10'
					input "Delay", "number", required: true, title: "Delay between IR Commands", defaultValue:1000
				}
            }
        }
        
        if(triggerMode == "Switch - auto off"){
	        section(getFormat("header-green", "${getImage("Blank")}"+" IR Command to Send")) {
            	input "msgToSendOn", "text", required: true, title: "IR Code to Send - ON", defaultValue: "sendir..."
                input "mCommands", "bool", title: "Send the IR Code multiple times (think volume control)", required:true, defaultValue:false, submitOnChange:true
				if(mCommands){
					input "xTimesOn", "number", title: "How many times to send 'On' IR Code (1 to 10)", required:false, defaultValue:1, range: '1..10'
					input "Delay", "number", required: true, title: "Delay between IR Commands", defaultValue:1000
				}
            }
        }

        if(triggerMode == "Channel"){
		    section(getFormat("header-green", "${getImage("Blank")}"+" IR Command to Send")) {
			    paragraph "<b>Input between 1 and 4 digits to send.</b>"
            	input "Digit1", "text", required: true, title: "Channel - First Digit", defaultValue: ""
                if(parent.msgDigitDS) input "dSeparator1", "bool", title: "Use Digit Separator between First and Second Digit", defaultValue:false
            	input "Digit2", "text", required: false, title: "Channel - Second Digit", defaultValue: ""
                if(parent.msgDigitDS) input "dSeparator2", "bool", title: "Use Digit Separator between Second and Third Digit", defaultValue:false
            	input "Digit3", "text", required: false, title: "Channel - Third Digit", defaultValue: ""
                if(parent.msgDigitDS) input "dSeparator3", "bool", title: "Use Digit Separator between Third and Fourth Digit", defaultValue:false
				input "Digit4", "text", required: fasle, title: "Channel - Fourth Digit", defaultValue: ""
				input "Delay", "number", required: true, title: "Delay between sending Digits", defaultValue: 1000
				input "EnterCode", "bool", title: "Send Enter Code after Digits", required: true, defaultValue: false
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false, submitOnChange:true
  	    }
		display2()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def uninstalled() {
    unschedule()
    unsubscribe()
    def children = getChildDevices()
    if(logEnable) log.debug "In uninstall - children: ${children}"
    if(children) {
        children.each { child ->
            deleteChildDevice(child.deviceNetworkId)
        }
    } else {
        paragraph "In uninstall - NO Children to remove"
    }
}

def updated() {
	log.debug "Installed with settings: ${settings}"
	unsubscribe()
	unschedule()
    if(logEnable) runIn(1800,logsOff)
	initialize()
}

def initialize(){
	if(logEnable) log.debug "Inside Initialize..."
    setDefaults()
    if(triggerMode == "Switch - on/off") subscribe(switch1, "switch", switchHandlerOnOff) 
    if(triggerMode == "Switch - auto off") subscribe(switch1, "switch", switchHandlerAuto)    
	if(triggerMode == "Channel") subscribe(switch1, "switch", channelHandlerSwitch)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def switchHandlerOnOff (evt) {
	def switching = evt.value
    if(switching == "on"){
        if(logEnable) log.debug "In switchHandlerOnOff - telnet Device: ${parent.telnetDevice} - mCommands: ${mCommands} - Switch is turned on - msg: ${msgToSendOn}"
		if(!mCommands) { 
            parent.telnetDevice.speak(msgToSendOn)
        } else {
            for (i = 0; i < xTimesOn; i++) {
                parent.telnetDevice.speak(msgToSendOn)
			    pauseExecution(Delay)
            }
        }
	}
        
    if(switching == "off"){
        if(logEnable) log.debug "In switchHandlerOnOff - telnet Device: ${parent.telnetDevice} - mCommands: ${mCommands} - Switch is turned off - msg: ${msgToSendOff}"
		if(!mCommands) {
            parent.telnetDevice.speak(msgToSendOff)
        } else {
		    for (i = 0; i < xTimesOff; i++) {
                parent.telnetDevice.speak(msgToSendOff)
			    pauseExecution(Delay)
            }
        }
    }
}

def switchHandlerAuto (evt) {
	def switching = evt.value
    if(switching == "on"){
        if(logEnable) log.debug "In switchHandlerAuto - telnet Device: ${parent.telnetDevice}  - mCommands: ${mCommands} - Switch is turned on - msg: ${msgToSendOn}"
		if(!mCommands) {
            parent.telnetDevice.speak(msgToSendOn)
        } else {
            for (i = 0; i < xTimesOn; i++) {
                parent.telnetDevice.speak(msgToSendOn)
			    pauseExecution(Delay)
            }
        }
	}
}

def PresetToSend1(){
    if(Digit1 == "1") {msgToSend1 = parent.msgDigit1}
    if(Digit1 == "2") {msgToSend1 = parent.msgDigit2}
    if(Digit1 == "3") {msgToSend1 = parent.msgDigit3}
    if(Digit1 == "4") {msgToSend1 = parent.msgDigit4}
    if(Digit1 == "5") {msgToSend1 = parent.msgDigit5}
	if(Digit1 == "6") {msgToSend1 = parent.msgDigit6}
	if(Digit1 == "7") {msgToSend1 = parent.msgDigit7}
	if(Digit1 == "8") {msgToSend1 = parent.msgDigit8}
	if(Digit1 == "9") {msgToSend1 = parent.msgDigit9}
	if(Digit1 == "0") {msgToSend1 = parent.msgDigit0}
    if(logEnable) log.debug "Getting Digit 1...${Digit1} - ${msgToSend1}"
}

def PresetToSend2(){
    if(Digit2 == "1") {msgToSend2 = parent.msgDigit1}
    if(Digit2 == "2") {msgToSend2 = parent.msgDigit2}
    if(Digit2 == "3") {msgToSend2 = parent.msgDigit3}
    if(Digit2 == "4") {msgToSend2 = parent.msgDigit4}
    if(Digit2 == "5") {msgToSend2 = parent.msgDigit5}
	if(Digit2 == "6") {msgToSend2 = parent.msgDigit6}
	if(Digit2 == "7") {msgToSend2 = parent.msgDigit7}
	if(Digit2 == "8") {msgToSend2 = parent.msgDigit8}
	if(Digit2 == "9") {msgToSend2 = parent.msgDigit9}
	if(Digit2 == "0") {msgToSend2 = parent.msgDigit0}
    if(logEnable) log.debug "Getting Digit 2...${Digit2} - ${msgToSend2}"
}

def PresetToSend3(){
    if(Digit3 == "1") {msgToSend3 = parent.msgDigit1}
    if(Digit3 == "2") {msgToSend3 = parent.msgDigit2}
    if(Digit3 == "3") {msgToSend3 = parent.msgDigit3}
    if(Digit3 == "4") {msgToSend3 = parent.msgDigit4}
    if(Digit3 == "5") {msgToSend3 = parent.msgDigit5}
	if(Digit3 == "6") {msgToSend3 = parent.msgDigit6}
	if(Digit3 == "7") {msgToSend3 = parent.msgDigit7}
	if(Digit3 == "8") {msgToSend3 = parent.msgDigit8}
	if(Digit3 == "9") {msgToSend3 = parent.msgDigit9}
	if(Digit3 == "0") {msgToSend3 = parent.msgDigit0}
    if(logEnable) log.debug "Getting Digit 3...${Digit3} - ${msgToSend3}"
}

def PresetToSend4(){
    if(Digit4 == "1") {msgToSend4 = parent.msgDigit1}
    if(Digit4 == "2") {msgToSend4 = parent.msgDigit2}
    if(Digit4 == "3") {msgToSend4 = parent.msgDigit3}
    if(Digit4 == "4") {msgToSend4 = parent.msgDigit4}
    if(Digit4 == "5") {msgToSend4 = parent.msgDigit5}
	if(Digit4 == "6") {msgToSend4 = parent.msgDigit6}
	if(Digit4 == "7") {msgToSend4 = parent.msgDigit7}
	if(Digit4 == "8") {msgToSend4 = parent.msgDigit8}
	if(Digit4 == "9") {msgToSend4 = parent.msgDigit9}
	if(Digit4 == "0") {msgToSend4 = parent.msgDigit0}
    if(logEnable) log.debug "Getting Digit 4...${Digit4} - ${msgToSend4}"
}

def PresetToSendE(){
    if(parent.msgDigitE) msgToSendE = parent.msgDigitE    
    if(logEnable) log.debug "Getting Digit E...${PresetToSendE} - ${msgToSendE}"
}

def PresetToSendDS() {
    if(parent.msgDigitDS) msgToSendDS = parent.msgDigitDS
    if(logEnable) log.debug "Getting Digit DS...${PresetToSendDS} - ${msgToSendDS}"
}

def channelHandlerSwitch(evt) {
	def switching = evt.value
    if(switching == "on"){
        if(logEnable) log.debug "You pressed Channel Switch On"
    	PresetToSend1()
    	PresetToSend2()
		PresetToSend3()
		PresetToSend4()
   		PresetToSendE()
        PresetToSendDS()
    
		if(logEnable) log.debug "In channelHandlerSwitch - Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
		if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
		parent.telnetDevice.speak(msgToSend1)
    	pauseExecution(Delay)
        
        if(dSeparator1) {
            if(logEnable) log.debug "Msg to send Digit Separator: ${dSeparator1} - ${msgToSendDS}"
			parent.telnetDevice.speak(msgToSendDS)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Digit Separator"
		}

		if(Digit2 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
			parent.telnetDevice.speak(msgToSend2)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 2"
		}
        
        if(dSeparator2) {
            if(logEnable) log.debug "Msg to send Digit Separator: ${dSeparator2} - ${msgToSendDS}"
			parent.telnetDevice.speak(msgToSendDS)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Digit Separator"
		}
        
		if(Digit3 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
			parent.telnetDevice.speak(msgToSend3)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 3"
		}
        
        if(dSeparator3) {
            if(logEnable) log.debug "Msg to send Digit Separator: ${dSeparator3} - ${msgToSendDS}"
			parent.telnetDevice.speak(msgToSendDS)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Digit Separator"
		}
        
		if(Digit4 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
			parent.telnetDevice.speak(msgToSend4)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 4"
		}
		if(logEnable) log.debug "${EnterCode}"
		if(EnterCode) {
    		if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    		parent.telnetDevice.speak(msgToSendE)
		} else{
			if(logEnable) log.debug "Did not send Channel Enter"
		}
	}
}

def checkVirtualChild(){
    if(logEnable) log.debug "In checkVirtualChild"
    def children = getChildDevices()
    if(logEnable) log.debug "In checkVirtualChild - children: ${children}"
    if(children) {
        if(logEnable) log.debug "In checkVirtualChild - Device already created."
        state.deviceCreatedMessage = "Device found."
    } else createVirtualChild()
}

def createVirtualChild() {
    if(logEnable) log.debug "In createVirtualChild - No children Found - Time to create device"
    try {
        if(triggerMode == "Switch - auto off" || triggerMode == "Channel") addChildDevice("BPTWorld", "Send IP2IR Switch Driver", "IP2IR-${app.id}", null, [name: "${app.label}", label: "${app.label}"])
        if(triggerMode == "Switch - on/off") addChildDevice("hubitat", "Virtual Switch", "IP2IR-${app.id}", null, [name: "${app.label}", label: "${app.label}"])
        if(logEnable) log.debug "In createVirtualChild - Device ${app.label} was successfully created!"
        state.deviceCreatedMessage = "Device created!"
    } catch (e) {
        log.error "Send IP2IR - ${e}"
    }   
}

// ********** Normal Stuff **********

def logsOff(){
    if(logEnable) log.info "In LogsOff - debug logging disabled"
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def setDefaults(){
	if(logEnable == null){logEnable = false}
    if(state.uniqueIdentifier == null || state.uniqueIdentifier == "") state.uniqueIdentifier = 0
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
