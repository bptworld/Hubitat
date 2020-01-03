/**
 *  ****************  IP2IR Telnet Driver  ****************
 *
 *  Design Usage:
 *  This driver is designed to send commands to an iTach IP2IR device.
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
 *  v1.0.5 - 01/03/20 - Fix for App Watchdog 2
 *  V1.0.4 - 08/29/19 - App Watchdog Compatible
 *  V1.0.3 - 04/16/19 - Code cleanup
 *  V1.0.2 - 12/06/18 - Minor changes and additonal error message. If the IP2IR unit is unplugged or loses connection for any
 *			 			reason, simply go into the IP2IR Telnet device and press the 'Initialize' button.
 *  V1.0.1 - 11/01/18 - Merged pull request from DTTerastar resend the command if busy is received. 
 *  V1.0.0 - 10/15/18 - Initial release
 */

def setVersion(){
    appName = "IP2IRTelnet"
	version = "v1.0.5" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "IP2IR Telnet", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Send%20IP2IR/SIP2IR%20Driver.groovy") {
    capability "Actuator"
	capability "Initialize"
    capability "Telnet"
    capability "Notification"
    capability "Speech Synthesis"

    attribute "Telnet", ""
        
    attribute "dwDriverInfo", "string"
    command "updateVersion"
}
    
preferences() {    	
        section(""){
            input "ipaddress", "text", required: true, title: "iTach IP2IR IP Address", defaultValue: "0.0.0.0"
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def speak(msg) {
    state.lastmsg = msg
    if(logEnable) log.debug "In speak - Sending Message: ${msg}"
    return new hubitat.device.HubAction("${msg}\n", hubitat.device.Protocol.TELNET)
}

def deviceNotification(message) {
    if(logEnable) log.debug "In deviceNotification - Sending Message: ${message}"
    speak(message)
}

def resend(){
    if(logEnable) log.debug "RESEND!"
    speak(state.lastmsg)
}

def initialize(){
	try {
		if(logEnable) log.debug "Opening telnet connection"
		sendEvent([name: "telnet", value: "Opening"])
        //telnetConnect([terminalType: 'VT100', termChars:[13]], "${ipaddress}", 4998, null, null)
		telnetConnect([terminalType: 'VT100'], "${ipaddress}", 4998, null, null)
		//give it a chance to start
		pauseExecution(1000)
		if(logEnable) log.debug "Telnet connection established"
    } catch(e) {
		log.warn "Initialize Error: ${e.message}"
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def parse(String msg) {
    if(logEnable) log.debug "parse ${msg}"
	sendEvent([name: "telnet", value: "Connected"])
    if (msg == "busyIR,1:1,1"){
        runIn(1, resend)
    }
}

def telnetStatus(String status) {
	if(logEnable) log.debug "telnetStatus: ${status}"
	if (status == "receive error: Stream is closed" || status == "send error: Broken pipe (Write failed)") {
		log.error("Telnet connection dropped...PLEASE OPEN THE IP2IR TELNET DEVICE IN HE AND PRESS THE 'INITIALIZE' BUTTON")
        sendEvent([name: "telnet", value: "Disconnected"])
		telnetClose()
		runIn(60, initialize)
    }
}
