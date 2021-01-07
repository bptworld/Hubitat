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
 *  V1.0.2 - 12/31/20 - Added windowShade
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
        capability "WindowShade"
        
        command "virtualContact", ["string"]
        command "virtualGroup1", ["string"]
        command "virtualGroup2", ["string"]
        command "virtualGroup3", ["string"]
        command "virtualLock", ["string"]
        command "virtualMotion", ["string"]
        command "virtualShade", ["string"]
        command "virtualSwitch", ["string"]
        command "virtualWater", ["string"]
        
        attribute "contact", "string"
        attribute "group1", "string"
        attribute "group2", "string"
        attribute "group3", "string"
        attribute "lock", "string"
        attribute "motion", "string"
        attribute "windowShade", "string"
        attribute "switch", "string"
        attribute "water", "string"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Simple Groups</b>", description: "This device was created by Simple Groups<br><br>The buttons above don't do anything, so please don't try to use them."
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def on() {
    virtualSwitch("on")
}

def off() {
    virtualSwitch("off")
}

def lock() {
    virtualLock("locked")
}

def unlock() {
    virtualLock("unlocked")
}

def virtualContact(data) {
    if(logEnable) log.info "In Simple Groups Driver - Contact"
    if(data == "open") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to open"
        sendEvent(name: "contact", value: "open", isStateChange: true)
    }

    if(data == "closed") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to closed"
        sendEvent(name: "contact", value: "closed", isStateChange: true)
    }
} 

def virtualGroup1(data) {
    if(logEnable) log.info "In Simple Groups Driver - Group Of Groups 1"
    if(data == "true") {
        if(logEnable) log.info "In Simple Groups Driver - Setting group1 to true"
        sendEvent(name: "group1", value: "true", isStateChange: true)
    }

    if(data == "false") {
        if(logEnable) log.info "In Simple Groups Driver - Setting group1 to false"
        sendEvent(name: "group1", value: "false", isStateChange: true)
    }
}

def virtualGroup2(data) {
    if(logEnable) log.info "In Simple Groups Driver - Group Of Groups 2"
    if(data == "true") {
        if(logEnable) log.info "In Simple Groups Driver - Setting group2 to true"
        sendEvent(name: "group2", value: "true", isStateChange: true)
    }

    if(data == "false") {
        if(logEnable) log.info "In Simple Groups Driver - Setting group2 to false"
        sendEvent(name: "group2", value: "false", isStateChange: true)
    }
}

def virtualGroup3(data) {
    if(logEnable) log.info "In Simple Groups Driver - Group Of Groups 3"
    if(data == "true") {
        if(logEnable) log.info "In Simple Groups Driver - Setting group3 to true"
        sendEvent(name: "group3", value: "true", isStateChange: true)
    }

    if(data == "false") {
        if(logEnable) log.info "In Simple Groups Driver - Setting group3 to false"
        sendEvent(name: "group3", value: "false", isStateChange: true)
    }
}

def virtualLock(data) {
    if(logEnable) log.info "In Simple Groups Driver - Lock"
    if(data == "unlocked") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to unlocked"
        sendEvent(name: "lock", value: "unlocked", isStateChange: true)
    }

    if(data == "locked") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to locked"
        sendEvent(name: "lock", value: "locked", isStateChange: true)
    }
} 

def virtualMotion(data) {
    if(logEnable) log.info "In Simple Groups Driver - Motion"
    if(data == "active") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to active"
        sendEvent(name: "motion", value: "active", isStateChange: true)
    }

    if(data == "inactive") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to inactive"
        sendEvent(name: "motion", value: "inactive", isStateChange: true)
    }
} 

def virtualShade(data) {
    if(logEnable) log.info "In Simple Groups Driver - Shade"
    if(data == "open") {
        if(logEnable) log.info "In Simple Groups Driver - Turning Shade open"
        sendEvent(name: "windowShade", value: "open", isStateChange: true)
    }

    if(data == "closed") {
        if(logEnable) log.info "In Simple Groups Driver - Turning Shade closed"
        sendEvent(name: "windowShade", value: "closed", isStateChange: true)
    }
}


def virtualSwitch(data) {
    if(logEnable) log.info "In Simple Groups Driver - Switch"
    if(data == "on") {
        if(logEnable) log.info "In Simple Groups Driver - Turning Switch On"
        sendEvent(name: "switch", value: "on", isStateChange: true)
    }

    if(data == "off") {
        if(logEnable) log.info "In Simple Groups Driver - Turning Switch Off"
        sendEvent(name: "switch", value: "off", isStateChange: true)
    }
}

def virtualWater(data) {
    if(logEnable) log.info "In Simple Groups Driver - Water"
    if(data == "wet") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to wet"
        sendEvent(name: "water", value: "wet", isStateChange: true)
    }

    if(data == "dry") {
        if(logEnable) log.info "In Simple Groups Driver - Setting device to dry"
        sendEvent(name: "water", value: "dry", isStateChange: true)
    }
}
