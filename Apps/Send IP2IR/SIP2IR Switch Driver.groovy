/**
 *  Send IP2IR Switch Driver
 *
 *  Design Usage:
 *  For use with the Send IP2IR app.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
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
 * v1.0.0 - 10/20/2019 - Initial release
 *
 */

metadata {
    definition(name: "Send IP2IR Switch Driver", namespace: "BPTWorld", author: "Bryan Turcotte", component: true) {
        capability "Switch"
        
        attribute "switch", "string"
        attribute "dwDriverInfo", "string"
    }
    preferences {
        input name: "about", type: "paragraph", element: "paragraph", title: "Send IP2IR Switch Driver", description: "This driver is for use with the Send IP2IR App.<br><br>Devices are created in the Send IP2IR app. This switch will automatically turn off after 1 second, each time it is turned on."
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false, description: ""
    }
}

def updated() {
    if(logEnable) log.info "In Updated"
    if(logEnable) runIn(1800,logsOff)
}

def installed() {
    if(logEnable) log.info "In installed"
}

def logsOff(){
    if(logEnable) log.info "In LogsOff - debug logging disabled"
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def on() {
    if(logEnable) log.info "In on - Turning Switch On"
    device.on
    sendEvent(name: "switch", value: "on", displayed: true)
    runIn(1,off)
}

def off() {
    if(logEnable) log.info "In off - Turning Switch Off"
    device.off
    sendEvent(name: "switch", value: "off", displayed: true)
}
