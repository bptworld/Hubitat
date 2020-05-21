/**
 *  ****************  Simple Groups Driver  ****************
 *
 *  Design Usage:
 *  This driver works with the Simple Groups app.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
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
 *  V1.0.1 - 05/21/20 - Added more stuff
 *  V1.0.0 - 05/20/20 - Initial release
 */


metadata {
	definition (name: "Simple Groups Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Groups/SG-driver.groovy") {
        capability "Contact Sensor"
        capability "Lock"
        capability "Motion Sensor"
        capability "Switch"
        capability "Water Sensor"
        
        command "virtualContact", ["string"]
        command "virtualLock", ["string"]
        command "virtualMotion", ["string"]
        command "virtualSwitch", ["string"]
        command "virtualWater", ["string"]
        
        attribute "contact", "string"
        attribute "contactStatus", "string"
        attribute "lock", "string"
        attribute "lockStatus", "string"
        attribute "motion", "string"
        attribute "motionStatus", "string"
        attribute "switch", "string"
        attribute "switchStatus", "string"
        attribute "water", "string"
        attribute "waterStatus", "string"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Simple Groups</b>", description: "This device was created by Simple Groups<br>"
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def virtualContact(data) {
    if(logEnable) log.info "In Simple Groups Driver - Contact"
    if(data == "open") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to open"
        sendEvent(name: "contact", value: "open", isStateChange: true)
        sendEvent(name: "contactStatus", value: "true", isStateChange: true)
    }

    if(data == "closed") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to closed"
        sendEvent(name: "contact", value: "closed", isStateChange: true)
        sendEvent(name: "contactStatus", value: "false", isStateChange: true)
    }
} 

def virtualLock(data) {
    if(logEnable) log.info "In Simple Groups Driver - Lock"
    if(data == "unlocked") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to unlocked"
        sendEvent(name: "lock", value: "unlocked", isStateChange: true)
        sendEvent(name: "lockStatus", value: "true", isStateChange: true)
    }

    if(data == "locked") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to locked"
        sendEvent(name: "lock", value: "locked", isStateChange: true)
        sendEvent(name: "lockStatus", value: "false", isStateChange: true)
    }
} 

def virtualMotion(data) {
    if(logEnable) log.info "In Simple Groups Driver - Motion"
    if(data == "active") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to active"
        sendEvent(name: "motion", value: "active", isStateChange: true)
        sendEvent(name: "motionStatus", value: "true", isStateChange: true)
    }

    if(data == "inactive") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to inactive"
        sendEvent(name: "motion", value: "inactive", isStateChange: true)
        sendEvent(name: "motionStatus", value: "false", isStateChange: true)
    }
} 

def virtualSwitch(data) {
    if(logEnable) log.info "In Simple Groups Driver - Switch"
    if(data == "on") {
        if(logEnable) log.info "In Simple Groups Driver - Turning Switch On"
        sendEvent(name: "switch", value: "on", isStateChange: true)
        sendEvent(name: "switchStatus", value: "true", isStateChange: true)
    }

    if(data == "off") {
        if(logEnable) log.info "In Simple Groups Driver - Turning Switch Off"
        sendEvent(name: "switch", value: "off", isStateChange: true)
        sendEvent(name: "switchStatus", value: "false", isStateChange: true)
    }
}

def virtualWater(data) {
    if(logEnable) log.info "In Simple Groups Driver - Water"
    if(data == "wet") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to wet"
        sendEvent(name: "water", value: "wet", isStateChange: true)
        sendEvent(name: "waterStatus", value: "true", isStateChange: true)
    }

    if(data == "dry") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to dry"
        sendEvent(name: "water", value: "dry", isStateChange: true)
        sendEvent(name: "waterStatus", value: "false", isStateChange: true)
    }
}
