/**
 *  ****************  Life Event Calendar Driver  ****************
 *
 *  Design Usage:
 *  This driver stores the info for Life Event Calendar app.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
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
 *  Special thanks to @dan.t and his sample code for making the websocket connection.
 *
 *  Changes:
 *
 *  1.0.0 - 03/27/22 - Initial release
 *
 */

metadata {
	definition (name: "Life Event Calendar Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"
        capability "Switch"
        
        //command "putACommandHere"
        
        attribute "switch", "string"       
        attribute "nextEvent", "string"        
        attribute "nextThree", "string"
    }
    preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Life Event Calendar Driver</b>", description: ""
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
            
        }
    }
}

def installed() {
	if(logEnable) log.debug "Installing and configuring Life Events Calendar Driver"
}

def updated() {	
   
}

def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true)
}
