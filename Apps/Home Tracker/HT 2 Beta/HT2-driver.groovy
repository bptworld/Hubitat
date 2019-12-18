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
        attribute "sensor21BH", "string"
        attribute "sensor22BH", "string"
        attribute "sensor23BH", "string"
        attribute "sensor24BH", "string"
        
        attribute "sensor0LA", "string"
        attribute "sensor1LA", "string"
        attribute "sensor2LA", "string"
        attribute "sensor3LA", "string"
        attribute "sensor4LA", "string"
        attribute "sensor5LA", "string"
        attribute "sensor6LA", "string"
        attribute "sensor7LA", "string"
        attribute "sensor8LA", "string"
        attribute "sensor9LA", "string"
        attribute "sensor10LA", "string"
        attribute "sensor11LA", "string"
        attribute "sensor12LA", "string"
        attribute "sensor13LA", "string"
        attribute "sensor14LA", "string"
        attribute "sensor15LA", "string"
        attribute "sensor16LA", "string"
        attribute "sensor17LA", "string"
        attribute "sensor18LA", "string"
        attribute "sensor19LA", "string"
        attribute "sensor20LA", "string"
        attribute "sensor21LA", "string"
        attribute "sensor22LA", "string"
        attribute "sensor23LA", "string"
        attribute "sensor24LA", "string"
        
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
    if(logEnable) log.debug "In Home Tracker Driver - sendDataMap1 - ${dataMap}"
	status = dataMap.split(";")
    
    sendEvent(name: "sensor${status[0]}BH", value: status[1])
    sendEvent(name: "sensor${status[0]}LA", value: status[2])  
}
