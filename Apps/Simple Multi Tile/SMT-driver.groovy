/**
 *  ****************  Simple Multi Tile Driver  ****************
 *
 *  Design Usage:
 *  Transform devices into other types of devices.
 *
 *  Copyright 2021 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  1.0.0 - 06/04/21 - Initial release
*/

metadata {
    definition (name: "Simple Multi Tile Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
        capability "Actuator"
        
        attribute "bpt-simpleMultiTile", "text"
        attribute "lastUpdated", "text"
        attribute "tableCount", "number"
    }
}

preferences {
    input title:"<b>Simple Multi Tile</b>", description:"This device was automatically created by Simple Multi Tile<br>Note: The buttons above do not do anything.", type:"paragraph", element:"paragraph"
    input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false, submitOnChange: true
    input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"], defaultValue: "1 Hour"
}

def updated() {
    installed()
}

def installed() {
    unschedule()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
}

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
}
