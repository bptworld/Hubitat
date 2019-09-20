/**
 *  ****************  Tile Master Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Tile Master data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V2.0.1 - 09/20/19 - Initial release.
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.0 - 02/16/19 - Initially started working on this concept but never released.
 */

def setVersion(){
    appName = "TileMasterDriver"
	version = "v2.0.1" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Tile Master Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Tile%20Master/TM-Driver.groovy") {
   		capability "Actuator"

		command "sendTile01", ["string"]
		
    	attribute "tile01", "string"
        attribute "tile01Count", "number"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(""){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
def sendTile01(tile1) {
    if(logEnable) log.debug "In Tile Master Driver - Received new data!"
    state.device1 = tile1
	state.device1Count = state.device1.length()
	if(state.device1Count <= 1024) {
		if(logEnable) log.debug "device1 - has ${state.device1Count} Characters<br>${state.device1}"
	} else {
		state.device1 = "Too many characters to display on Dashboard (${state.device1Count})"
	}
	sendEvent(name: "tile01", value: state.device1, displayed: true)
    sendEvent(name: "tile01Count", value: state.device1Count, displayed: true)
}
