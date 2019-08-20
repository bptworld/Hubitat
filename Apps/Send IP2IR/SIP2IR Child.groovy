/**
 *  ****************  Send IP2IR Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
 *
 *  Thanks to Carson Dallum's (@cdallum) for the original IP2IR driver code that I based my driver off of.
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.2.0 - 07/14/19 - Minor code changes
 *  V1.1.9 - 04/16/19 - Fixed a bug, thank you @NickP!
 *  V1.1.8 - 04/15/19 - Code cleanup
 *  V1.1.7 - 01/15/19 - Updated footer with update check and links
 *  V1.1.6 - 12/30/18 - Updated to my new color theme.
 *  V1.1.5 - 12/06/18 - Code cleanup, removal of IP Address from Child Apps as it was not needed anymore.
 *  V1.1.4 - 11/30/18 - Added pause button to child apps. Added an Enable/Disable by switch option. Cleaned up code. 
 *                      NOTE: Must open and resave each child app for them to work again! Sorry.
 *  V1.1.3 - 11/02/18 - Added the ability to send multiple Switch On's, Off's or both and Button's to send multiple times with
 *                      each push. Also Fixed some typo's.
 *  V1.1.2 - 11/01/18 - Added an optional Digit 4 within Channels. Sending Enter Code after Digits is now optional. Made the
 *                      Delay between sending digits user specified and added in some instructions.
 *  V1.1.1 - 10/29/18 - Updated Channels to be either a Button or a Switch, only Switches can be used with Google Assistant.
 *  V1.1.0 - 10/20/18 - Big change in how Channels work. Only have to enter each digits IR code once, in the Advance Section of
 *			 			the Parent app. Now in the Child apps, only need to put in the digits (no IR codes!). This is a 
 *			 			non-destructive update. All existing channels will still work. Thanks to Bruce (@bravenel) for showing
 *						me how to send code from parent to child apps.
 *  V1.0.0 - 10/15/18 - Initial release
 */
 
def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "SendIP2IRChildVersion"
	state.version = "v2.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
            schedule("0 0 3 ? * * *", setVersion)
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
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
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Send IP2IR</h2>", install: true, uninstall: true, refreshInterval:0) {
		display()
		section("Instructions:", hideable: true, hidden: true) {
				paragraph "There are 4 types of Triggers that can be made."
        		paragraph "<b>Switch:</b><br>To turn anything on/off. ie. Television, Stereo, Cable Box, etc. Remember, it's okay to put the same code in box on and off if necessary."
        		paragraph "<b>Button:</b><br>Used to send just one command. ie. Volume Up, Channel Down, etc. Note: this can not be used with Google Assistant."
        		paragraph "<b>Channel_Switch:</b><br>Used to send 1 to 4 commands at the same time. This is used to send Channels numbers based on the Presets in the Parent app."
            	paragraph "<b>Channel_Button:</b><br>Also, used to send 1 to 4 commands at the same time. This is used to send Channels numbers based on the Presets in the Parent app. Note: this can not be used with Google Assistant."
				paragraph "<b>Important:</b><br>Each child app takes a device to trigger the commands, so be sure to create either a Virtual Switch or Virtual Button before trying to create a child app."
				paragraph "<b>Google Assistant Notes:</b><br>Google Assistant only works with switches. If creating virtual switches for channels, be sure to use the 'Enable auto off' @ '500ms' to give the effect of a button in a Dashboard but still be able to tell Google to control it."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Select Trigger")) {
			paragraph "<b>Select the Trigger Type to Activate to send the IR command</b>"
            input "triggerMode", "enum", required: true, title: "Select Trigger Type", submitOnChange: true,  options: ["Button", "Switch", "Channel_Switch", "Channel_Button"]
			
            if(triggerMode == "Switch"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Select a 'Virtual Switch Device' to Activate the Command")) {
                	input "switch1", "capability.switch", title: "Select Trigger Device", required: true, multiple: false
            		input "msgToSendOn", "text", required: true, title: "IR Code to Send - ON", defaultValue: "sendir..."
                	input "msgToSendOff", "text", required: true, title: "IR Code to Send - OFF", defaultValue: "sendir..."
					input "mCommands", "enum", required: true, title: "Send the IR Code multiple times (think volume control)", submitOnChange: true,  options: ["No", "Yes"], defaultValue: "No"
					if(mCommands == "Yes"){
						input "xTimesOn", "enum", title: "How many times to send 'On' IR Code", required: true, options: ["1", "2", "3", "4"]
						input "xTimesOff", "enum", title: "How many times to send 'Off' IR Code", required: true, options: ["1", "2", "3", "4"]
						input "Delay", "number", required: true, title: "Delay between IR Codes", defaultValue: 1000
					}
                }
            }
            if(triggerMode == "Button"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Select a 'Virtual Button Device' to Activate the Command")) {
                	input "button1", "capability.pushableButton", title: "Select Button Device", required: true, multiple: false
            		input "buttonNumber", "enum", title: "Enter Button Number", required: true, options: ["1", "2", "3", "4", "5"]
                	input "msgToSendPushed", "text", required: true, title: "IR Code to Send on Push", defaultValue: "sendir..."
					input "xTimes", "enum", title: "How many times to send IR Code", required: true, options: ["1", "2", "3", "4"]
					input "Delay", "number", required: true, title: "Delay between sending IR Codes", defaultValue: 1000
                }
            }
            if(triggerMode == "Channel_Switch"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Select a 'Virtual Switch Device' to Activate the Command")) {
					input "switch2", "capability.switch", title: "Select Trigger Device", required: true, multiple: false
					paragraph "<b>Input between 1 and 4 digits to send.</b>"
            		input "Digit1", "text", required: true, title: "Channel - First Digit", defaultValue: ""
            		input "Digit2", "text", required: false, title: "Channel - Second Digit", defaultValue: ""
            		input "Digit3", "text", required: false, title: "Channel - Third Digit", defaultValue: ""
					input "Digit4", "text", required: fasle, title: "Channel - Fourth Digit", defaultValue: ""
					input "Delay", "number", required: true, title: "Delay between sending Digits", defaultValue: 1000
					input "EnterCode", "bool", title: "Send Enter Code after Digits", required: true, defaultValue: false
				}
			}
			if(triggerMode == "Channel_Button"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Select a 'Virtual Button Device' to Activate the Command")) {
					input "button1", "capability.pushableButton", title: "Select Button Device", required: false, multiple: false
            		input "buttonNumber", "enum", title: "Enter Button Number", required: true, options: ["1", "2", "3", "4", "5"]
					paragraph "<b>Input between 1 and 4 digits to send.</b>"
            		input "Digit1", "text", required: true, title: "Channel - First Digit", defaultValue: ""
            		input "Digit2", "text", required: false, title: "Channel - Second Digit", defaultValue: ""
            		input "Digit3", "text", required: false, title: "Channel - Third Digit", defaultValue: ""
					input "Digit4", "text", required: fasle, title: "Channel - Fourth Digit", defaultValue: ""
					input "Delay", "number", required: true, title: "Delay between sending Digits", defaultValue: 1000
					input "EnterCode", "bool", title: "Send Enter Code after Digits", required: true, defaultValue: false
				}
            }
		}
    	section(getFormat("header-green", "${getImage("Blank")}"+" Telnet Setup")) {
            input "speaker", "capability.speechSynthesis", title: "Select the 'IP2IR Telnet' device created during initial setup/install.", required: true, multiple: false
    	}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this child app", required: true
        }
		section() {
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
  	    }
		display2()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Installed with settings: ${settings}"
	unsubscribe()
	unschedule()
	initialize()
}

def initialize(){
	if(logEnable) log.debug "Inside Initialize..."
    setDefaults()
    if(triggerMode == "Switch"){
		if(logEnable) log.debug "Initialize... triggerMode=Switch"
		subscribe(switch1, "switch", switchHandler1)
	}
    if(triggerMode == "Button"){
		if(logEnable) log.debug "Initialize... triggerMode=Button"
        if(buttonNumber == '1'){subscribe(button1, "pushed.1", buttonHandler1)}
        if(buttonNumber == '2'){subscribe(button1, "pushed.2", buttonHandler2)}
        if(buttonNumber == '3'){subscribe(button1, "pushed.3", buttonHandler3)}
        if(buttonNumber == '4'){subscribe(button1, "pushed.4", buttonHandler4)}
        if(buttonNumber == '5'){subscribe(button1, "pushed.5", buttonHandler5)}
    }
    if(triggerMode == "Channel_Button"){
		if(logEnable) log.debug "Initialize... triggerMode=Channel_Button"
        if(buttonNumber == '1'){subscribe(button1, "pushed.1", channelHandler1)}
        if(buttonNumber == '2'){subscribe(button1, "pushed.2", channelHandler1)}
        if(buttonNumber == '3'){subscribe(button1, "pushed.3", channelHandler1)}
        if(buttonNumber == '4'){subscribe(button1, "pushed.4", channelHandler1)}
        if(buttonNumber == '5'){subscribe(button1, "pushed.5", channelHandler1)}
	}
	if(triggerMode == "Channel_Switch"){
		if(logEnable) log.debug "Initialize... triggerMode=Channel_Switch"
		subscribe(switch2, "switch", channelHandlerSwitch)
	}
}

def switchHandler1 (evt) {
			def switching = evt.value
    		if(switching == "on"){
        		if(logEnable) log.debug "Switch is turned on"
        		if(logEnable) log.debug "Msg to send Switch On: ${msgToSendOn}"
				if(logEnable) log.debug "mCommands = ${mCommands}"
				if(mCommands == "No") {speaker.speak(msgToSendOn)}
					if(xTimesOn == "1") {
					speaker.speak(msgToSendOn)
				}
				if(xTimesOn == "2") {
					speaker.speak(msgToSendOn)
					pauseExecution(Delay)
					speaker.speak(msgToSendOn)
				}
				if(xTimesOn == "3") {
					speaker.speak(msgToSendOn)
					pauseExecution(Delay)
					speaker.speak(msgToSendOn)
					pauseExecution(Delay)
					speaker.speak(msgToSendOn)
				}
				if(xTimesOn == "4") {
					speaker.speak(msgToSendOn)
					pauseExecution(Delay)
					speaker.speak(msgToSendOn)
					pauseExecution(Delay)
					speaker.speak(msgToSendOn)
					pauseExecution(Delay)
					speaker.speak(msgToSendOn)
				}
			}
        
    		if(switching == "off"){
        		if(logEnable) log.debug "Switch is turned off"
        		if(logEnable) log.debug "Msg to send Switch Off: ${msgToSendOff}"
				if(logEnable) log.debug "mCommands = ${mCommands}"
				if(mCommands == "No") {speaker.speak(msgToSendOff)}
				if(xTimesOff == "1") {
					speaker.speak(msgToSendOff)
				}
				if(xTimesOff == "2") {
					speaker.speak(msgToSendOff)
					pauseExecution(Delay)
					speaker.speak(msgToSendOff)
				}
				if(xTimesOff == "3") {
					speaker.speak(msgToSendOff)
					pauseExecution(Delay)
					speaker.speak(msgToSendOff)
					pauseExecution(Delay)
					speaker.speak(msgToSendOff)
				}
				if(xTimesOff == "4") {
					speaker.speak(msgToSendOff)
					pauseExecution(Delay)
					speaker.speak(msgToSendOff)
					pauseExecution(Delay)
					speaker.speak(msgToSendOff)
					pauseExecution(Delay)
					speaker.speak(msgToSendOff)
				}
    		}
}

def buttonHandler1(evt){
    	if(logEnable) log.debug "You pressed button 1"
    	if(logEnable) log.debug "Msg to send Pushed: ${msgToSendPushed}"
		if(xTimes == "1") {
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "2") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "3") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "4") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
}

def buttonHandler2(evt){
    	if(logEnable) log.debug "You pressed button 2"
    	if(logEnable) log.debug "Msg to send Pushed: ${msgToSendPushed}"
		if(xTimes == "1") {
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "2") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "3") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "4") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		} 
}

def buttonHandler3(evt){
    	if(logEnable) log.debug "You pressed button 3"
    	if(logEnable) log.debug "Msg to send Pushed: ${msgToSendPushed}"
		if(xTimes == "1") {
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "2") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "3") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "4") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		} 
}

def buttonHandler4(evt){
    	if(logEnable) log.debug "You pressed button 4"
    	if(logEnable) log.debug "Msg to send Pushed: ${msgToSendPushed}"
		if(xTimes == "1") {
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "2") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "3") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "4") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
}

def buttonHandler5(evt){
    	if(logEnable) log.debug "You pressed button 5"
    	if(logEnable) log.debug "Msg to send Pushed: ${msgToSendPushed}"
		if(xTimes == "1") {
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "2") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "3") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
		}
		if(xTimes == "4") {
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
			pauseExecution(Delay)
			speaker.speak(msgToSendPushed)
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

def PresetToSendS(){
    msgToSendE = parent.msgDigitE
    if(logEnable) log.debug "Getting Digit E...${PresetToSendS} - ${msgToSendE}"
}

def channelHandlerSwitch(evt) {
			def switching = evt.value
    		if(switching == "on"){
        		if(logEnable) log.debug "You pressed Channel Switch On"
    			PresetToSend1()
    			PresetToSend2()
				PresetToSend3()
				PresetToSend4()
   				PresetToSendS()
    
				if(logEnable) log.debug "Inside channelHandlerSwitch Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
				if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
				speaker.speak(msgToSend1)
    			pauseExecution(Delay)

				if(Digit2 != "null") {
    				if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
					speaker.speak(msgToSend2)
					pauseExecution(Delay)
				} else{
					if(logEnable) log.debug "Did not send Channel Digit 2"
				}
				if(Digit3 != "null") {
    				if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
					speaker.speak(msgToSend3)
    				pauseExecution(Delay)
				} else{
					if(logEnable) log.debug "Did not send Channel Digit 3"
				}
				if(Digit4 != "null") {
    				if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
					speaker.speak(msgToSend4)
    				pauseExecution(Delay)
				} else{
					if(logEnable) log.debug "Did not send Channel Digit 4"
				}
				if(logEnable) log.debug "${EnterCode}"
				if(EnterCode == true) {
    				if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    				speaker.speak(msgToSendE)
				} else{
					if(logEnable) log.debug "Did not send Channel Enter"
				}
			}
}

def channelHandler1(evt) {
        	if(logEnable) log.debug "You pressed Channel button 1"
    		PresetToSend1()
    		PresetToSend2()
			PresetToSend3()
			PresetToSend4()
   			PresetToSendS()
    
			if(logEnable) log.debug "Inside channelHandlerSwitch Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
			if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
			speaker.speak(msgToSend1)
    		pauseExecution(Delay)

			if(Digit2 != "null") {
    			if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
				speaker.speak(msgToSend2)
				pauseExecution(Delay)
			} else{
				if(logEnable) log.debug "Did not send Channel Digit 2"
			}
			if(Digit3 != "null") {
    			if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
				speaker.speak(msgToSend3)
    			pauseExecution(Delay)
			} else{
				if(logEnable) log.debug "Did not send Channel Digit 3"
			}
			if(Digit4 != "null") {
    			if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
				speaker.speak(msgToSend4)
    			pauseExecution(Delay)
			} else{
				if(logEnable) log.debug "Did not send Channel Digit 4"
			}
			if(logEnable) log.debug "${EnterCode}"		 
			if(EnterCode == true) {
    			if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    			speaker.speak(msgToSendE)
			} else{
				if(logEnable) log.debug "Did not send Channel Enter"
			}
}

def channelHandler2(evt) {
        if(logEnable) log.debug "You pressed Channel button 2"
    	PresetToSend1()
    	PresetToSend2()
		PresetToSend3()
		PresetToSend4()
   		PresetToSendS()
    
		if(logEnable) log.debug "Inside channelHandlerSwitch Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
		if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
		speaker.speak(msgToSend1)
    	pauseExecution(Delay)

		if(Digit2 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
			speaker.speak(msgToSend2)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 2"
		}
		if(Digit3 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
			speaker.speak(msgToSend3)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 3"
		}
		if(Digit4 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
			speaker.speak(msgToSend4)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 4"
		}
		if(logEnable) log.debug "${EnterCode}"
		if(EnterCode == true) {
    		if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    		speaker.speak(msgToSendE)
		} else{
			if(logEnable) log.debug "Did not send Channel Enter"
		}
}

def channelHandler3(evt) {
        if(logEnable) log.debug "You pressed Channel button 3"
    	PresetToSend1()
    	PresetToSend2()
		PresetToSend3()
		PresetToSend4()
   		PresetToSendS()
    
		if(logEnable) log.debug "Inside channelHandlerSwitch Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
		if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
		speaker.speak(msgToSend1)
    	pauseExecution(Delay)

		if(Digit2 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
			speaker.speak(msgToSend2)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 2"
		}
		if(Digit3 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
			speaker.speak(msgToSend3)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 3"
		}
		if(Digit4 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
			speaker.speak(msgToSend4)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 4"
		}
		if(logEnable) log.debug "${EnterCode}"		 
		if(EnterCode == true) {
    		if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    		speaker.speak(msgToSendE)
		} else{
			if(logEnable) log.debug "Did not send Channel Enter"
		}
}

def channelHandler4(evt) {
        if(logEnable) log.debug "You pressed Channel button 4"
    	PresetToSend1()
    	PresetToSend2()
		PresetToSend3()
		PresetToSend4()
   		PresetToSendS()
    
		if(logEnable) log.debug "Inside channelHandlerSwitch Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
		if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
		speaker.speak(msgToSend1)
    	pauseExecution(Delay)

		if(Digit2 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
			speaker.speak(msgToSend2)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 2"
		}
		if(Digit3 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
			speaker.speak(msgToSend3)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 3"
		}
		if(Digit4 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
			speaker.speak(msgToSend4)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 4"
		}
		if(logEnable) log.debug "${EnterCode}"
		if(EnterCode == true) {
    		if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    		speaker.speak(msgToSendE)
		} else{
			if(logEnable) log.debug "Did not send Channel Enter"
		}
}

def channelHandler5(evt) {
        if(logEnable) log.debug "You pressed Channel button 5"
    	PresetToSend1()
    	PresetToSend2()
		PresetToSend3()
		PresetToSend4()
   		PresetToSendS()
    
		if(logEnable) log.debug "Inside channelHandlerSwitch Digits ${Digit1} ${Digit2} ${Digit3} ${Digit4} ${EnterCode}"
	
		if(logEnable) log.debug "Msg to send Digit One: ${Digit1} - ${msgToSend1}"
		speaker.speak(msgToSend1)
    	pauseExecution(Delay)

		if(Digit2 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Two: ${Digit2} - ${msgToSend2}"
			speaker.speak(msgToSend2)
			pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 2"
		}
		if(Digit3 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Three: ${Digit3} - ${msgToSend3}"
			speaker.speak(msgToSend3)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 3"
		}
		if(Digit4 != "null") {
    		if(logEnable) log.debug "Msg to send Digit Four: ${Digit4} - ${msgToSend4}"
			speaker.speak(msgToSend4)
    		pauseExecution(Delay)
		} else{
			if(logEnable) log.debug "Did not send Channel Digit 4"
		}
		if(logEnable) log.debug "${EnterCode}" 
		if(EnterCode == true) {
    		if(logEnable) log.debug "Msg to send Enter: ${EnterCode} - ${msgToSendE}"
    		speaker.speak(msgToSendE)
		} else{
			if(logEnable) log.debug "Did not send Channel Enter"
		}
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
}

def getImage(type) {									// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){							// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Send IP2IR - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 

