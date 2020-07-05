/**
 *  ****************  Room Director Driver  ****************
 *
 *  Design Usage:
 *  This driver works with the Room Director app.
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
 *  1.0.0 - 07/05/20 - Initial release
 */

metadata {
	definition (name: "Room Director Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Room%20Director/RD-driver.groovy") {
        capability "Switch"

        command "settingStatus", [[name: "settingStatus",type:"ENUM", description:"Room Status", constraints: ["occupied", "unoccupied", "sleep"]]]
        
        attribute "switch", "string"
        attribute "status", "string"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Room Director</b>", description: "This device was created by Room Director<br>"
            input "logEnable", "bool", title: "Enable logging", required: false, defaultValue: false
        }
    }
}

def settingStatus(data) {
    if(logEnable) log.info "In Room Director Driver - Setting Status - ${data}"
    
    if(data == "occupied") {
        if(logEnable) log.info "In Room Director Driver - Setting switch to occupied"
        sendEvent(name: "switch", value: "on", displayed: true)
        sendEvent(name: "status", value: "occupied", isStateChange: true)
    }
    
    if(data == "unoccupied") {
        if(logEnable) log.info "In Room Director Driver - Setting switch to unoccupied"
        sendEvent(name: "switch", value: "off", displayed: true)
        sendEvent(name: "status", value: "unoccupied", isStateChange: true)
    }
       
    if(data == "sleep") {
        if(logEnable) log.info "In Room Director Driver - Setting switch to sleep"
        sendEvent(name: "switch", value: "off", displayed: true)
        sendEvent(name: "status", value: "sleep", isStateChange: true)
    }
} 
