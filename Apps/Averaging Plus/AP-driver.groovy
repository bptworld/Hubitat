/**
 *  ****************  Averaging Plus Driver  ****************
 *
 *  Design Usage:
 *  This driver works with the Averaging Plus app.
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
 *  V1.0.1 - 06/10/20 - Lots of changes to match app
 *  V1.0.0 - 05/25/20 - Initial release
 */

metadata {
	definition (name: "Averaging Plus Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Averaging%20Plus/AP-driver.groovy") {
        capability "Actuator"
        capability "Switch"
        capability "Contact Sensor"
        capability "Motion Sensor"
        capability "Water Sensor"
        capability "IlluminanceMeasurement"

        command "clearData"
        command "virtualAverage", ["string"]
        command "todaysHigh", ["string"]
        command "todaysLow", ["string"]
        command "weeklyHigh", ["string"]
        command "weeklyLow", ["string"]
        
        attribute "average", "number"
        attribute "battery", "number"
        attribute "energy", "number"
        attribute "humidity", "number"
        attribute "illuminance", "number"
        attribute "level", "number"
        attribute "power", "number"
        attribute "temperature", "number"
        attribute "ultravioletIndex", "number"
        attribute "voltage", "number"

        attribute "todaysHigh", "number"
        attribute "todaysLow", "number"
        attribute "weeklyHigh", "number"
        attribute "weeklyLow", "number"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Averaging Plus</b>", description: "This device was created by Averaging Plus<br>"
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def virtualAverage(data) {
    def (theType, theValue) = data.split(":")
    if(logEnable) log.info "In Averaging Plus Driver - Recieved ${data}"

    sendEvent(name: "average", value: theValue, isStateChange: true)
    if(theType == "battery") sendEvent(name: "battery", value: theValue, isStateChange: true)
    if(theType == "energy") sendEvent(name: "energy", value: theValue, isStateChange: true)
    if(theType == "humidity") sendEvent(name: "humidity", value: theValue, isStateChange: true)
    if(theType == "illuminance") sendEvent(name: "illuminance", value: theValue, isStateChange: true)
    if(theType == "level") sendEvent(name: "level", value: theValue, isStateChange: true)
    if(theType == "power") sendEvent(name: "power", value: theValue, isStateChange: true)
    if(theType == "temperature") sendEvent(name: "temperature", value: theValue, isStateChange: true)
    if(theType == "ultravioletIndex") sendEvent(name: "ultravioletIndex", value: theValue, isStateChange: true)
    if(theType == "voltage") sendEvent(name: "voltage", value: theValue, isStateChange: true)
} 

def todaysHigh(data) {
    if(logEnable) log.info "In Averaging Plus Driver - Setting todaysHigh to ${data}"
    sendEvent(name: "todaysHigh", value: data, isStateChange: true)
}

def todaysLow(data) {
    if(logEnable) log.info "In Averaging Plus Driver - Setting todaysLow to ${data}"
    sendEvent(name: "todaysLow", value: data, isStateChange: true)
}

def weeklyHigh(data) {
    if(logEnable) log.info "In Averaging Plus Driver - Setting weeklyHigh to ${data}"
    sendEvent(name: "weeklyHigh", value: data, isStateChange: true)
}

def weeklyLow(data) {
    if(logEnable) log.info "In Averaging Plus Driver - Setting weeklyLow to ${data}"
    sendEvent(name: "weeklyLow", value: data, isStateChange: true)
}

def on() {
    if(logEnable) log.info "In Averaging Plus Driver - Turning Switch On"
    device.on
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
    if(logEnable) log.info "In Averaging Plus Driver - Turning Switch Off"
    device.off
    sendEvent(name: "switch", value: "off", isStateChange: true)
} 

def clearData() {
    if(logEnable) log.info "In Averaging Plus Driver - Clearing the Data"
    sendEvent(name: "average", value: "-")
    sendEvent(name: "todaysHigh", value: "-")
    sendEvent(name: "todaysLow", value: "-")
    sendEvent(name: "weeklyHigh", value: "-")
    sendEvent(name: "weeklyLow", value: "-")
    sendEvent(name: "switch", value: "off")
}
