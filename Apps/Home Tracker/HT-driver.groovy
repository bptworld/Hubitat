/**
 *  ****************  Home Tracker 2 Driver  ****************
 *
 *  Design Usage:
 *  This driver stores Global Variables to be used with Home Tracker.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  V1.2.0 - 12/17/19 - Major rewrite
 *
 */

def setVersion(){
    appName = "HomeTrackerDriver"
	version = "v1.2.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Home Tracker 2 Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Home%20Tracker/HT-driver.groovy") {
   		capability "Actuator"
		
		command "sendDataMap", ["string"]
        command "sendDataMapName", ["string"]
        command "sendDataMapLock", ["string"]
        command "sendDataMapLockName", ["string"]
        command "clearData"
        
        attribute "sensor0BH", "string"
		attribute "sensor1BH", "string"
        attribute "sensor2BH", "string"
        attribute "sensor3BH", "string"
        attribute "sensor4BH", "string"
        attribute "sensor5BH", "string"
        attribute "sensor6BH", "string"
        attribute "sensor7BH", "string"
        attribute "sensor8BH", "string"
        attribute "sensor9BH", "string"
        attribute "sensor10BH", "string"
        attribute "sensor11BH", "string"
        attribute "sensor12BH", "string"
        attribute "sensor13BH", "string"
        attribute "sensor14BH", "string"
        attribute "sensor15BH", "string"
        attribute "sensor16BH", "string"
        attribute "sensor17BH", "string"
        attribute "sensor18BH", "string"
        attribute "sensor19BH", "string"
        attribute "sensor20BH", "string"
        
        attribute "sensor0Name", "string"
        attribute "sensor1Name", "string"
        attribute "sensor2Name", "string"
        attribute "sensor3Name", "string"
        attribute "sensor4Name", "string"
        attribute "sensor5Name", "string"
        attribute "sensor6Name", "string"
        attribute "sensor7Name", "string"
        attribute "sensor8Name", "string"
        attribute "sensor9Name", "string"
        attribute "sensor10Name", "string"
        attribute "sensor11Name", "string"
        attribute "sensor12Name", "string"
        attribute "sensor13Name", "string"
        attribute "sensor14Name", "string"
        attribute "sensor15Name", "string"
        attribute "sensor16Name", "string"
        attribute "sensor17Name", "string"
        attribute "sensor18Name", "string"
        attribute "sensor19Name", "string"
        attribute "sensor20Name", "string"
        
        attribute "lock0BH", "string"
        attribute "lock1BH", "string"
        attribute "lock2BH", "string"
        attribute "lock3BH", "string"
        
        attribute "lock0Name", "string"
        attribute "lock1Name", "string"
        attribute "lock2Name", "string"
        attribute "lock3Name", "string"

        attribute "dwDriverInfo", "string"
        command "updateVersion"	
	}
	preferences() {    	
        section(){
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def sendDataMap(dataMap) {
    if(logEnable) log.debug "In Home Tracker Driver - sendDataMap - ${dataMap}"
	status = dataMap.split(";")
    sendEvent(name: "sensor${status[0]}BH", value: "${dataMap}")
}

def sendDataMapName(dataMap) {
    if(logEnable) log.debug "In Home Tracker Driver - sendDataMapName - ${dataMap}"
	status = dataMap.split(";")
    sendEvent(name: "sensor${status[0]}Name", value: "${dataMap}")
}

def sendDataMapLock(dataMap) {
    if(logEnable) log.debug "In Home Tracker Driver - sendDataMapLock - ${dataMap}"
	status = dataMap.split(";")
    sendEvent(name: "lock${status[0]}BH", value: "${dataMap}")
}

def sendDataMapLockName(dataMap) {
    if(logEnable) log.debug "In Home Tracker Driver - sendDataMapLockName - ${dataMap}"
	status = dataMap.split(";")
    sendEvent(name: "lock${status[0]}Name", value: "${dataMap}")
}

def clearData() {
    log.info "Home Tracker 2 - Clearing Presesnce Sensor Data"
    for(x=0;x < 20;x++){
        sendEvent(name: "sensor${x}BH", value: "-;-")
        sendEvent(name: "sensor${x}Name", value: "-;-;-")
    }
    log.info "Home Tracker 2 - Clearing Lock Data"
    for(x=0;x < 4;x++){
        sendEvent(name: "lock${x}BH", value: "-;-")
        sendEvent(name: "lock${x}Name", value: "-;-;-")
    }
}

