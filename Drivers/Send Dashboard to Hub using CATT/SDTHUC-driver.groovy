/**
 *  **************** Send Dashboard to Hub using CATT Driver ****************
 *
 *  Design Usage:
 *  This driver is designed to send the HE dashboard to a Nest Hub using CATT.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.0 - 08/15/19 - Initial release
 */

metadata {
	definition (name: "Send Dashboard to Hub using CATT Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Drivers/Send%20Dashboard%20to%20Hub%20using%20CATT/SDTHUC-driver.groovy") {
	    capability "Initialize"
        capability "Telnet"
        capability "Switch"
        capability "Audio Volume"

        attribute "Telnet", ""
    }
    
    preferences() {
        section(){
            input "ipaddress", "text", required: true, title: "Catt Server IP Address", defaultValue: "0.0.0.0"
            input "userName", "text", required: true, title: "Catt Server Username"
            input "userPass", "password", required: true, title: "Catt Server Password"
            input "gDevice", "text", required: true, title: "Exact name of the Nest Hub to use"
            input "castWebsite", "text", required: true, title: "Enter the exact webite URL including http://"
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
        }
    }
}

def resend(){
    if(logEnable) log.debug "RESEND!"
    speak(state.lastmsg)
}

def initialize(){
	try {
		if(logEnable) log.debug "Opening telnet connection"
		sendEvent([name: "telnet", value: "Opening"])
        telnetConnect([terminalType: 'VT100'], "${ipaddress}", 23, "${userName}", "${userPass}")
		//give it a chance to start
		pauseExecution(1000)
		if(logEnable) log.debug "Telnet connection established"
    } catch(e) {
		if(logEnable) log.debug "Initialize Error: ${e.message}"
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
        return new hubitat.device.HubAction("${state.lastmsg}\n", hubitat.device.Protocol.TELNET)
    }
}

def telnetStatus(String status) {
	if(logEnable) log.debug "telnetStatus: ${status}"
	if (status == "receive error: Stream is closed" || status == "send error: Broken pipe (Write failed)") {
		log.error("Telnet connection dropped...PLEASE OPEN THIS DEVICE IN HE AND PRESS THE 'INITIALIZE' BUTTON")
        sendEvent([name: "telnet", value: "Disconnected"])
		telnetClose()
		runIn(60, initialize)
    }
}

def on(msg) {
    def msgAction = "catt -d '${gDevice}' cast_site '${castWebsite}'"
    
    state.lastmsg = msgAction
    if(logEnable) log.debug "Sending msg: ${msgAction}"
    return new hubitat.device.HubAction("${msgAction}\n", hubitat.device.Protocol.TELNET)
}

def off(msg) {
    def msgAction = "catt -d '${gDevice}' stop"
    
    state.lastmsg = msgAction
    if(logEnable) log.debug "Sending msg: ${msgAction}"
    return new hubitat.device.HubAction("${msgAction}\n", hubitat.device.Protocol.TELNET)
}

def setVolume(msg) {
    def msgAction = "catt -d '${gDevice}' volume '${msg}'"
    
    state.lastmsg = msgAction
    if(logEnable) log.debug "Sending msg: ${msgAction}"
    return new hubitat.device.HubAction("${msgAction}\n", hubitat.device.Protocol.TELNET)
}

def volumeDown(msg) {
    def msgAction = "catt -d '${gDevice}' volumedown 10"
    
    state.lastmsg = msgAction
    if(logEnable) log.debug "Sending msg: ${msgAction}"
    return new hubitat.device.HubAction("${msgAction}\n", hubitat.device.Protocol.TELNET)
}

def volumeUp(msg) {
    def msgAction = "catt -d '${gDevice}' volumeup 10"
    
    state.lastmsg = msgAction
    if(logEnable) log.debug "Sending msg: ${msgAction}"
    return new hubitat.device.HubAction("${msgAction}\n", hubitat.device.Protocol.TELNET)
}

