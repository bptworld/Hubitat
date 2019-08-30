/**
 *  ****************  Simple Dates Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Simple Dates data to be used with Hubitat's Dashboards.
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
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
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
 *  V2.0.0 - 08/28/19 - App Watchdog compatible
 *  V1.0.0 - 06/05/19 - Initial release
 */

def setVersion(){
    appName = "SimpleDatesDriver"
	version = "v2.0.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Simple Dates Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"

		command "sendSimpleReminder1", ["string"]
		command "sendSimpleReminder2", ["string"]
		
    	attribute "reminder1", "string"
		attribute "reminder2", "string"
        attribute "dwDriverInfo", "string"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Data Font Size", required: true, defaultValue: "15")
			input("fontSizeS", "text", title: "Date Font Size", required: true, defaultValue: "12")
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
        }
    }
}

def sendSimpleReminder1(reminderMap1) {
	state.reminderDevice1a = "${reminderMap1}"
	state.reminderDevice1 = "<table width='100%'><tr>"
	state.reminderDevice1 += "<td style='text-align: left; width: 100%'>"
	state.reminderDevice1 += "<div style='font-size: ${fontSize}px'> ${state.reminderDevice1a}</div>"
	state.reminderDevice1 += "</td></tr></table>"
	state.reminderDevice1Count = state.reminderDevice1.length()
	if(state.reminderDevice1Count <= 1000) {
		if(logEnable) log.debug "reminderDevice1 - has ${state.reminderDevice1Count} Characters<br>${state.reminderDevice1}"
	} else {
		state.reminderDevice1 = "Too many characters to display on Dashboard (${state.reminderDevice1Count})"
	}
	sendEvent(name: "reminder1", value: state.reminderDevice1, displayed: true)
}

def sendSimpleReminder2(reminderMap2) {
	state.reminderDevice2a = "${reminderMap2}"
	state.reminderDevice2 = "<table width='100%'><tr>"
	state.reminderDevice2 += "<td style='text-align: left; width: 100%'>"
	state.reminderDevice2 += "<div style='font-size: ${fontSize}px'> ${state.reminderDevice2a}</div>"
	state.reminderDevice2 += "</td></tr></table>"
	state.reminderDevice2Count = state.reminderDevice2.length()
	if(state.reminderDevice2Count <= 1000) {
		if(logEnable) log.debug "reminderDevice2 - has ${state.reminderDevice2Count} Characters<br>${state.reminderDevice2}"
	} else {
		state.reminderDevice2 = "Too many characters to display on Dashboard (${state.reminderDevice2Count})"
	}
	sendEvent(name: "reminder2", value: state.reminderDevice2, displayed: true)
}

def installed(){
    log.info "Simple Dates Driver has been Installed"
    setVersion()
}

def updated() {
    log.info "Simple Dates Driver has been Updated"
    setVersion()
}
