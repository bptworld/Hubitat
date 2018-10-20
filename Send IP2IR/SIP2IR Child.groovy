/**
 *  ****************  Send IP2IR Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to Andrew Parker (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to Carson Dallum's (@cdallum) for the original IP2IR driver code that I based my driver off of.
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/tree/master/Send%20IP2IR
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  
 *  V1.1.0 - 10/20/18 - Big change in how Channels work. Only have to enter each digits IR code once, in the Advance Section of
 *			the Parent app. Now in the Child apps, only need to put in the digits (no IR codes!). This is a 
 *			non-destructive update. All existing channels will still work. Thanks to Bruce (@bravenel) for showing
 *			me how to send code from parent to child apps.
 *  V1.0.0 - 10/15/18 - Initial release
 */
 
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
    )

preferences {
	display()
		section() {
     	   paragraph "This app is designed to send commands to an iTach IP2IR device."
 	    }
		section() {
            input "triggerMode", "enum", required: true, title: "Select Trigger Type", submitOnChange: true,  options: ["Button", "Switch", "Channel"] 
            if(triggerMode == "Switch"){
                section("Select a 'Virtual Switch Device' to Activate the Command") {
                	input "switch1", "capability.switch", title: "Select Trigger Device", required: true, multiple: false
            		input "msgToSendOn", "text", required: true, title: "IR Code to Send - ON", defaultValue: "sendir..."
                	input "msgToSendOff", "text", required: true, title: "IR Code to Send - OFF", defaultValue: "sendir..."
                }
            }
            if(triggerMode == "Button"){
                section("Select a 'Virtual Button Device' to Activate the Command") {
                	input "button1", "capability.pushableButton", title: "Select Button Device", required: true, multiple: false
            		input "buttonNumber", "enum", title: "Enter Button Number", required: true, options: ["1", "2", "3", "4", "5"]
                	input "msgToSendPushed", "text", required: true, title: "IR Code to Send on Push", defaultValue: "sendir..."
                }
            }
            if(triggerMode == "Channel"){
                
                section("Select a 'Virtual Button Device' to Activate the Channel") {
					input "button1", "capability.pushableButton", title: "Select Button Device", required: true, multiple: false
            		input "buttonNumber", "enum", title: "Enter Button Number", required: true, options: ["1", "2", "3", "4", "5"]
					
            		input "Digit1", "text", required: true, title: "Channel - First Digit", defaultValue: ""
            		input "Digit2", "text", required: true, title: "Channel - Second Digit", defaultValue: ""
            		input "Digit3", "text", required: true, title: "Channel - Third Digit", defaultValue: ""
        		}
            }
    }
	
    	section() {
            input "speaker", "capability.speechSynthesis", title: "Select the 'IP2IR Telnet' device created during initial setup/install.", required: true, multiple: false
    		input "ipaddress", "text", required: true, title: "iTach IP2IR IP Adress", defaultValue: "0.0.0.0"
    	}
		section() {
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: true
  	    }
}


def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize(){
    
    myMsgDigit1()
    myMsgDigit2()
    myMsgDigit3()
    myMsgDigit4()
    myMsgDigit5()
    myMsgDigit6()
    myMsgDigit7()
    myMsgDigit8()
    myMsgDigit9()
    myMsgDigit0()
    myMsgDigitE()
    
    if(triggerMode == "Switch"){subscribe(switch1, "switch", switchHandler1)}
 
    if(triggerMode == "Button"){
        if(buttonNumber == '1'){subscribe(button1, "pushed.1", buttonHandler1)}
        if(buttonNumber == '2'){subscribe(button1, "pushed.2", buttonHandler2)}
        if(buttonNumber == '3'){subscribe(button1, "pushed.3", buttonHandler3)}
        if(buttonNumber == '4'){subscribe(button1, "pushed.4", buttonHandler4)}
        if(buttonNumber == '5'){subscribe(button1, "pushed.5", buttonHandler5)}
    }
    
    if(triggerMode == "Channel"){
        if(buttonNumber == '1'){subscribe(button1, "pushed.1", channelHandler1)}
        if(buttonNumber == '2'){subscribe(button1, "pushed.2", channelHandler1)}
        if(buttonNumber == '3'){subscribe(button1, "pushed.3", channelHandler1)}
        if(buttonNumber == '4'){subscribe(button1, "pushed.4", channelHandler1)}
        if(buttonNumber == '5'){subscribe(button1, "pushed.5", channelHandler1)}
    }
}

def switchHandler1 (evt) {
def switching = evt.value
    if(switching == "on"){
        LOGDEBUG("Switch is turned on")
        LOGDEBUG("Msg to send Switch On: ${msgToSendOn}")
		speaker.speak(msgToSendOn)   
    }
        
    if(switching == "off"){
        LOGDEBUG("Switch is turned off")
        LOGDEBUG("Msg to send Switch Off: ${msgToSendOff}")
		speaker.speak(msgToSendOff)
    }         
}

def buttonHandler1(evt){
    	LOGDEBUG("You pressed button 1")
    	LOGDEBUG("Msg to send Pushed: ${msgToSendPushed}")
		speaker.speak(msgToSendPushed)        
}

def buttonHandler2(evt){
    	LOGDEBUG("You pressed button 2")
    	LOGDEBUG("Msg to send Pushed: ${msgToSendPushed}")
		speaker.speak(msgToSendPushed)        
}

def buttonHandler3(evt){
    	LOGDEBUG("You pressed button 3")
    	LOGDEBUG("Msg to send Pushed: ${msgToSendPushed}")
		speaker.speak(msgToSendPushed)        
}

def buttonHandler4(evt){
    	LOGDEBUG("You pressed button 4")
    	LOGDEBUG("Msg to send Pushed: ${msgToSendPushed}")
		speaker.speak(msgToSendPushed)        
}

def buttonHandler5(evt){
    	LOGDEBUG("You pressed button 5")
    	LOGDEBUG("Msg to send Pushed: ${msgToSendPushed}")
		speaker.speak(msgToSendPushed)        
}

def myMsgDigit1(msgDigit1) {}

def myMsgDigit2(msgDigit2) {}

def myMsgDigit3(msgDigit3) {}

def myMsgDigit4(msgDigit4) {}

def myMsgDigit5(msgDigit5) {}

def myMsgDigit6(msgDigit6) {}

def myMsgDigit7(msgDigit7) {}

def myMsgDigit8(msgDigit8) {}

def myMsgDigit9(msgDigit9) {}

def myMsgDigit0(msgDigit0) {}

def myMsgDigitE(msgDigitE) {}

def PresetToSend1(){
    if(Digit1 == "1") {
       myMsgDigit1()
       msgToSend1 = parent.msgDigit1
    }
    if(Digit1 == "2") {
       myMsgDigit2()
       msgToSend1 = parent.msgDigit2
    }
    if(Digit1 == "3") {
       myMsgDigit3()
       msgToSend1 = parent.msgDigit3
    }
    if(Digit1 == "4") {
       myMsgDigit4()
       msgToSend1 = parent.msgDigit4
    }
    if(Digit1 == "5") {
       myMsgDigit5()
       msgToSend1 = parent.msgDigit5
    }
	if(Digit1 == "6") {
       myMsgDigit6()
       msgToSend1 = parent.msgDigit6
    }
	if(Digit1 == "7") {
       myMsgDigit7()
       msgToSend1 = parent.msgDigit7
    }
	if(Digit1 == "8") {
       myMsgDigit8()
       msgToSend1 = parent.msgDigit8
    }
	if(Digit1 == "9") {
       myMsgDigit9()
       msgToSend1 = parent.msgDigit9
    }
	if(Digit1 == "0") {
       myMsgDigit0()
       msgToSend1 = parent.msgDigit0
    }
    LOGDEBUG("Getting Digit 1...${Digit1} - ${msgToSend1}")
}

def PresetToSend2(){
    if(Digit2 == "1") {
       myMsgDigit1()
       msgToSend2 = parent.msgDigit1
    }
    if(Digit2 == "2") {
       myMsgDigit2()
       msgToSend2 = parent.msgDigit2
    }
    if(Digit2 == "3") {
       myMsgDigit3()
       msgToSend2 = parent.msgDigit3
    }
    if(Digit2 == "4") {
       myMsgDigit4()
       msgToSend2 = parent.msgDigit4
    }
    if(Digit2 == "5") {
       myMsgDigit5()
       msgToSend2 = parent.msgDigit5
    }
	if(Digit2 == "6") {
       myMsgDigit6()
       msgToSend2 = parent.msgDigit6
    }
	if(Digit2 == "7") {
       myMsgDigit7()
       msgToSend2 = parent.msgDigit7
    }
	if(Digit2 == "8") {
       myMsgDigit8()
       msgToSend2 = parent.msgDigit8
    }
	if(Digit2 == "9") {
       myMsgDigit9()
       msgToSend2 = parent.msgDigit9
    }
	if(Digit2 == "0") {
       myMsgDigit0()
       msgToSend2 = parent.msgDigit0
    }
    LOGDEBUG("Getting Digit 2...${Digit2} - ${msgToSend2}")
}

def PresetToSend3(){
    if(Digit3 == "1") {
       myMsgDigit1()
       msgToSend3 = parent.msgDigit1
    }
    if(Digit3 == "2") {
       myMsgDigit2()
       msgToSend3 = parent.msgDigit2
    }
    if(Digit3 == "3") {
       myMsgDigit3()
       msgToSend3 = parent.msgDigit3
    }
    if(Digit3 == "4") {
       myMsgDigit4()
       msgToSend3 = parent.msgDigit4
    }
    if(Digit3 == "5") {
       myMsgDigit5()
       msgToSend3 = parent.msgDigit5
    }
	if(Digit3 == "6") {
       myMsgDigit6()
       msgToSend3 = parent.msgDigit6
    }
	if(Digit3 == "7") {
       myMsgDigit7()
       msgToSend3 = parent.msgDigit7
    }
	if(Digit3 == "8") {
       myMsgDigit8()
       msgToSend3 = parent.msgDigit8
    }
	if(Digit3 == "9") {
       myMsgDigit9()
       msgToSend3 = parent.msgDigit9
    }
	if(Digit3 == "0") {
       myMsgDigit0()
       msgToSend3 = parent.msgDigit0
    }
    LOGDEBUG("Getting Digit 3...${Digit3} - ${msgToSend3}")
}

def PresetToSendS(){
    myMsgDigitE()
    msgToSendE = parent.msgDigitE
    LOGDEBUG("Getting Digit E...${PresetToSendS} - ${msgToSendE}")
}

def channelHandler1(evt) {
        LOGDEBUG("You pressed Channel button 1")
    	PresetToSend1()
    	PresetToSend2()
    	PresetToSend3()
    	PresetToSendS()
    	
        LOGDEBUG("Msg to send Digit One: ${Digit1} - ${msgToSend1}")
		speaker.speak(msgToSend1)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Two: ${Digit2} - ${msgToSend2}")
		speaker.speak(msgToSend2)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Three: ${Digit3} - ${msgToSend3}")
		speaker.speak(msgToSend3)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Enter: ${DigitE} - ${msgToSendE}")
    	speaker.speak(msgToSendE)
}

def channelHandler2(evt) {
        LOGDEBUG("You pressed Channel button 2")
    	PresetToSend1()
    	PresetToSend2()
    	PresetToSend3()
    	PresetToSendS()
    	
        LOGDEBUG("Msg to send Digit One: ${Digit1} - ${msgToSend1}")
		speaker.speak(msgToSend1)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Two: ${Digit2} - ${msgToSend2}")
		speaker.speak(msgToSend2)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Three: ${Digit3} - ${msgToSend3}")
		speaker.speak(msgToSend3)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Enter: ${DigitE} - ${msgToSendE}")
    	speaker.speak(msgToSendE)
}

def channelHandler3(evt) {
        LOGDEBUG("You pressed Channel button 3")
    	PresetToSend1()
    	PresetToSend2()
    	PresetToSend3()
    	PresetToSendS()
    	
    	LOGDEBUG("Msg to send Digit One: ${Digit1} - ${msgToSend1}")
		speaker.speak(msgToSend1)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Two: ${Digit2} - ${msgToSend2}")
		speaker.speak(msgToSend2)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Three: ${Digit3} - ${msgToSend3}")
		speaker.speak(msgToSend3)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Enter: ${DigitE} - ${msgToSendE}")
    	speaker.speak(msgToSendE)
}

def channelHandler4(evt) {
        LOGDEBUG("You pressed Channel button 4")
    	PresetToSend1()
    	PresetToSend2()
    	PresetToSend3()
    	PresetToSendS()
    	
        LOGDEBUG("Msg to send Digit One: ${Digit1} - ${msgToSend1}")
		speaker.speak(msgToSend1)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Two: ${Digit2} - ${msgToSend2}")
		speaker.speak(msgToSend2)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Three: ${Digit3} - ${msgToSend3}")
		speaker.speak(msgToSend3)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Enter: ${DigitE} - ${msgToSendE}")
    	speaker.speak(msgToSendE)
}

def channelHandler5(evt) {
        LOGDEBUG("You pressed Channel button 5")
    	PresetToSend1()
    	PresetToSend2()
    	PresetToSend3()
    	PresetToSendS()
    	
        LOGDEBUG("Msg to send Digit One: ${Digit1} - ${msgToSend1}")
		speaker.speak(msgToSend1)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Two: ${Digit2} - ${msgToSend2}")
		speaker.speak(msgToSend2)
    	pauseExecution(500)
    	LOGDEBUG("Msg to send Digit Three: ${Digit3} - ${msgToSend3}")
		speaker.speak(msgToSend3)
    	pauseExecution(500)
        LOGDEBUG("Msg to send Enter: ${DigitE} - ${msgToSendE}")
    	speaker.speak(msgToSendE)
}

// define debug action
def logCheck(){
state.checkLog = debugMode
if(state.checkLog == true){
log.info "All Logging Enabled"
}
else if(state.checkLog == false){
log.info "Further Logging Disabled"
}

}

// logging...
def LOGDEBUG(txt){
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}

def display(){
	section{paragraph "Version: 1.1.0<br>@BPTWorld"}     
}

def setVersion(){
		state.InternalName = "SendIP2IRChild"
}
    
