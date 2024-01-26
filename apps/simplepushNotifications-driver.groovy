/**
 *  ****************  Simplepush Notification Driver  ****************
 *
 *  Design Usage:
 *  This driver works with the Simplepush Notification app.
 *
 *  Copyright 2024 Bryan Turcotte (@bptworld)
 *  
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *  Changes are listed in the app.
 */

metadata {
	definition (name: "Simplepush Notification Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
        capability "Notification"
        capability "Actuator"
        capability "Switch"

        command "sendSimplepush", 	[[name:"theMessage*", type:"STRING", description:"Message to send"], 
                                     [name:"theEvent", type:"STRING", description: "Optional Event"],
                                     [name:"actions", type:"STRING", description: "Optional - enter YES or NO (for now)"]]

        attribute "lastMessage", "string"
        attribute "sentAt", "string"
        attribute "switch", "string"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Simplepush Notification</b>", description: "This device was created by Simplepush Notification<br><br>Each 'User/Phone' will have one Key that can be used across as many virtual devices as you need."
            input name: "simpleKey", type: "text", title: "Simplepush Key"
            
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def sendSimplepush(theMessage, theEvent=null, actions=null) {
    if(simpleKey) {
        if(logEnable) log.info "In sendSimplepush - ${theMessage} - Event: ${theEvent}"
        def data = new Date()
        sendEvent(name: "sentAt", value: data, displayed: true)
        sendEvent(name: "lastMessage", value: theMessage, displayed: true)
        if(theEvent == null) theEvent = ""
        if(actions == null) actions = ""
        theDevice = device.id
        parent.sendAsynchttpPost(theDevice, simpleKey, theMessage, theEvent, actions)
    } else {
        log.warn "Simplepush Driver - Be sure to enter your Simplepush Key in to the driver."
    }
}

def actionHandler(theAction) {
    if(logEnable) log.info "In actionHandler - ${theAction}"
    if(theAction.toLowerCase() == "yes") {
        on()
    } else {
        off()
    }
}

def on() {
    sendEvent(name: "switch", value: "on", displayed: true)
}

def off() {
    sendEvent(name: "switch", value: "off", displayed: true)
}
