/**
 *  ****************  Global Variables Driver  ****************
 *
 *  Design Usage:
 *  This driver stores Global Variables to be used with BPTWorld apps.
 *  Apps that use this driver:
 *  	- Welcome Home
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
 *  V1.0.0 - 02/09/19 - Initial release
 */

def version(){"v1.0.0"}

metadata {
	definition (name: "Global Variables Driver", namespace: "BPTWorld", author: "Bryan Turcotte") {
   		capability "Actuator"
		
		command "sendDataMap1", ["string"]
		command "sendDataMap2", ["string"]
		command "sendDataMap3", ["string"]
		command "sendDataMap4", ["string"]
		command "sendDataMap5", ["string"]

		attribute "globalBH1", "string"
		attribute "globalBH2", "string"
		attribute "globalBH3", "string"
		attribute "globalBH4", "string"
		attribute "globalBH5", "string"
		
	}
	preferences() {    	
        section(){
            input("debugMode", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

// *** start global variables from Welcome Home ***
def sendDataMap1(dataMap1) {
    LOGDEBUG("In Welcome Home - sendDataMap1 - Received new data!")
	def dMap1 = "${dataMap1}"
	sendEvent(name: "globalBH1", value: dMap1, displayed: true)
}
def sendDataMap2(dataMap2) {
    LOGDEBUG("In Welcome Home - sendDataMap2 - Received new data!")
	def dMap2 = "${dataMap2}"
	sendEvent(name: "globalBH2", value: dMap2, displayed: true)
}
def sendDataMap3(dataMap3) {
    LOGDEBUG("In Welcome Home - sendDataMap3 - Received new data!")
	def dMap3 = "${dataMap3}"
	sendEvent(name: "globalBH3", value: dMap3, displayed: true)
}
def sendDataMap4(dataMap4) {
    LOGDEBUG("In Welcome Home - sendDataMap4 - Received new data!")
	def dMap4 = "${dataMap4}"
	sendEvent(name: "globalBH4", value: dMap4, displayed: true)
}
def sendDataMap5(dataMap5) {
    LOGDEBUG("In Welcome Home - sendDataMap5 - Received new data!")
	def dMap5 = "${dataMap5}"
	sendEvent(name: "globalBH5", value: dMap5, displayed: true)
}
//   *** End Welcome Home ***
	
def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
