/**
 *  ****************  Snapshot Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Snapshot data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019-2022 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  1.2.3 - 05/08/22 - Major rewrite
 *  1.2.2 - 01/23/22 - Time for an overhaul
 *  ---
 *  1.0.0 - 03/06/19 - Initial release
 */

metadata {
	definition (name: "Snapshot Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://github.com/bptworld/Hubitat/blob/master/Apps/Snapshot/S-driver.groovy") {
   		capability "Actuator"
		capability "Switch"

        attribute "switchMap1", "string"
		attribute "switchMap2", "string"
		attribute "switchMap3", "string"
		attribute "switchMap4", "string"
		attribute "switchMap5", "string"
		attribute "switchMap6", "string"
		attribute "switchCountOn", "string"
		attribute "switchCountOff", "string"
		
		attribute "contactMap1", "string"
		attribute "contactMap2", "string"
		attribute "contactMap3", "string"
		attribute "contactMap4", "string"
		attribute "contactMap5", "string"
		attribute "contactMap6", "string"
		attribute "contactCountOpen", "string"
		attribute "countactCountClosed", "string"
		
		attribute "waterMap1", "string"
		attribute "waterMap2", "string"
        attribute "waterMap3", "string"
        attribute "waterMap4", "string"
        attribute "waterMap5", "string"
        attribute "waterMap6", "string"
		attribute "waterCountWet", "string"
		attribute "waterCountDry", "string"
		
		attribute "presenceMap1", "string"
		attribute "presenceMap2", "string"
        attribute "presenceMap3", "string"
        attribute "presenceMap4", "string"
        attribute "presenceMap5", "string"
        attribute "presenceMap6", "string"
		attribute "presenceCountNotPresent", "string"
        attribute "presenceCountPresent", "string"
        
        attribute "motionMap1", "string"
		attribute "motionMap2", "string"
        attribute "motionMap3", "string"
        attribute "motionMap4", "string"
        attribute "motionMap5", "string"
        attribute "motionMap6", "string"
		attribute "motionCountActive", "string"
		attribute "motionCountInactive", "string"
		
		attribute "lockMap1", "string"
		attribute "lockMap2", "string"
        attribute "lockMap3", "string"
        attribute "lockMap4", "string"
        attribute "lockMap5", "string"
        attribute "lockMap6", "string"
		attribute "lockCountUnlocked", "string"
		attribute "lockCountLocked", "string"
		
		attribute "tempMap1", "string"
		attribute "tempMap2", "string"
        attribute "tempMap3", "string"
        attribute "tempMap4", "string"
        attribute "tempMap5", "string"
        attribute "tempMap6", "string"
		attribute "tempCountHigh", "string"
		attribute "tempCountLow", "string"
		
		attribute "prioritySwitch1", "string"
		attribute "prioritySwitch2", "string"
		attribute "priorityContact1", "string"
		attribute "priorityContact2", "string"
		attribute "priorityWater1", "string"
		attribute "priorityWater2", "string"
        attribute "priorityMotion1", "string"
		attribute "priorityMotion2", "string"
		attribute "priorityLock1", "string"
		attribute "priorityLock2", "string"
		attribute "priorityTemp1", "string"
		attribute "priorityTemp2", "string"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Data Font Size", required: true, defaultValue: "15")
			input("fontSizeS", "text", title: "Date Font Size", required: true, defaultValue: "12")
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: true)
        }
    }
}

def installed(){
    log.info "Snapshot Driver has been Installed"
}

def updated() {
    log.info "Snapshot Driver has been Updated"
    state.clear()
}

def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true)
}
