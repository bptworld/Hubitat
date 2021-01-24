/**
 *  ****************  IP2IR Telnet Driver  ****************
 *
 *  Design Usage:
 *  This driver is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018-2021 Bryan Turcotte (@bptworld)
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
 *  1.0.7 - 01/24/21 - Cosmetic changes
 *  1.0.6 - 03/05/20 - Minor changes
 *  1.0.5 - 01/03/20 - Fix for App Watchdog 2
 *  1.0.4 - 08/29/19 - App Watchdog Compatible
 *  1.0.3 - 04/16/19 - Code cleanup
 *  1.0.2 - 12/06/18 - Minor changes and additonal error message. If the IP2IR unit is unplugged or loses connection for any
 *			 			reason, simply go into the IP2IR Telnet device and press the 'Initialize' button.
 *  1.0.1 - 11/01/18 - Merged pull request from DTTerastar resend the command if busy is received. 
 *  1.0.0 - 10/15/18 - Initial release
 */

metadata {
	definition (name: "IP2IR Telnet", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Send%20IP2IR/SIP2IR%20Driver.groovy") {
        capability "Actuator"
	    capability "Initialize"
        capability "Telnet"
        capability "Notification"

        attribute "telnet", ""
    }
}
    
preferences() {    	
    section() {
        input "ipaddress", "text", required: true, title: "iTach IP2IR IP Address", defaultValue: "0.0.0.0"
        input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
    }
}

def deviceNotification(msg) {
    state.lastmsg = msg
    if(logEnable) log.debug "In deviceNotification - Sending Message: ${msg}"
    return new hubitat.device.HubAction("${msg}\n", hubitat.device.Protocol.TELNET)
}

def resend() {
    if(logEnable) log.debug "RESEND!"
    deviceNotification(state.lastmsg)
}

def isConnected() {
    pauseExecution(1000)
    def telnet1 = device.currentValue('telnet')
    if(logEnable) log.debug "In isConnected: telnet status: ${telnet1}"
    if(telnet1 != "Error") {
        log.debug "Send IP2IR - Telnet connection established"
        sendEvent(name: "telnet", value: "Connected")
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
    state.aSetupInstructions = "<b>To setup this Device:</b><br> - Enter in your iTach IP Address and press 'Save Preferences'"
    state.aTestingTheConnection = "<b>To test the telnet connection:</b><br> - Under 'Current States', it should say 'Connected'.<br> - If not press the 'Initialize' button. Make sure it now says connected.<br> - If not check your IP address."
    state.aTestingTheCommands = "<b>To test a command:</b><br> - Have Logs open in another tab.<br> - Paste a valid 'send' command into the 'Device Notification' box and press the 'Device Notification' button.<br> - IR device should have responded (turned on/off, ect.).<br> - If device didn't respond, check logs for errors."
	try {
		if(logEnable) log.debug "Opening telnet connection"
		sendEvent(name: "telnet", value: "Opening")
        //telnetConnect([terminalType: 'VT100', termChars:[13]], "${ipaddress}", 4998, null, null)
		telnetConnect([terminalType: 'VT100'], "${ipaddress}", 4998, null, null)
		//give it a chance to start
		isConnected()
    } catch(e) {
		log.warn "Initialize Error: ${e.message}"
        sendEvent(name: "telnet", value: "Error")
    }
}

def parse(String msg) {
    if(logEnable) log.debug "parse ${msg}"
	sendEvent(name: "telnet", value: "Connected")
    if (msg == "busyIR,1:1,1") {
        runIn(1, resend)
    }
}

def telnetStatus(String status) {
	if(logEnable) log.debug "telnetStatus: ${status}"
	if (status == "receive error: Stream is closed" || status == "send error: Broken pipe (Write failed)") {
		log.error("Telnet connection dropped...PLEASE OPEN THE IP2IR TELNET DEVICE IN HE AND PRESS THE 'INITIALIZE' BUTTON")
        sendEvent(name: "telnet", value: "Disconnected")
		telnetClose()
		runIn(60, initialize)
    } else {
        //sendEvent(name: "telnet", value: "${status}")
    }
}
