/**
 *  ****************  IP2IR Telnet Driver  ****************
 *
 *  Design Usage:
 *  This driver is designed to send commands to an iTach IP2IR device.
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
 ------------------------------------------------------------------------------------------------------------------------------
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
 *  V1.0.2 - 12/06/18 - Minor changes and additional error message. If the IP2IR unit is unplugged or loses connection for any
 *			reason, simply go into the IP2IR Telnet device and press the 'Initialize' button.
 *  V1.0.1 - 11/01/18 - Merged pull request from DTTerastar resend the command if busy is received. 
 *  V1.0.0 - 10/15/18 - Initial release
 */

metadata {
	definition (name: "IP2IR Telnet", namespace: "BPTWorld", author: "Bryan Turcotte") {
	capability "Initialize"
    capability "Telnet"
    capability "Notification"
    capability "Speech Synthesis"

    attribute "Telnet", ""
}
    
preferences() {    	
        section(""){
            input "ipaddress", "text", required: true, title: "iTach IP2IR IP Address", defaultValue: "0.0.0.0"
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def speak(msg) {
    state.lastmsg = msg
    LOGDEBUG("Sending Message: ${msg}")
    return new hubitat.device.HubAction("${msg}\n", hubitat.device.Protocol.TELNET)
}

def deviceNotification(message) {
    speak(message)
}

def resend(){
    LOGDEBUG("RESEND!")
    speak(state.lastmsg)
}

def initialize(){
	try {
		LOGDEBUG("Opening telnet connection")
		sendEvent([name: "telnet", value: "Opening"])
        //telnetConnect([terminalType: 'VT100', termChars:[13]], "${ipaddress}", 4998, null, null)
		telnetConnect([terminalType: 'VT100'], "${ipaddress}", 4998, null, null)
		//give it a chance to start
		pauseExecution(1000)
		LOGDEBUG("Telnet connection established")
    } catch(e) {
		LOGDEBUG("Initialize Error: ${e.message}")
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def parse(String msg) {
    LOGDEBUG "parse ${msg}"
	sendEvent([name: "telnet", value: "Connected"])
    if (msg == "busyIR,1:1,1"){
        runIn(1, resend)
    }
}

def telnetStatus(String status) {
	LOGDEBUG "telnetStatus: ${status}"
	if (status == "receive error: Stream is closed" || status == "send error: Broken pipe (Write failed)") {
		log.error("Telnet connection dropped...PLEASE OPEN THE IP2IR TELNET DEVICE IN HE AND PRESS THE 'INITIALIZE' BUTTON")
        sendEvent([name: "telnet", value: "Disconnected"])
		telnetClose()
		runIn(60, initialize)
    }
}

def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
