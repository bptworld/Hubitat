/**
 *  ****************  gCalendar Driver  ****************
 *
 *  Design Usage:
 *  Retrieves a Google Calendar to be used with HE Dashboards.
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
 *  Original concept by @TechMedX
 *
 *  Changes:
 *  1.0.0 - 02/28/21 - Initial release
*/

metadata {
    definition (name: "gCalendar", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
        capability "Actuator"
        attribute "gCal", "text"
        attribute "lastUpdated", "text"
        command "refresh"
    }
}

preferences {
    input title:"<b>Google Calendar Tile</b>", description:"Note: Calendar will be updated once every hour or when 'Refresh' button is pushed.<br><br><b>Setup:</b><br>1) Go to your Google Calendar<br>2) For the calendar you want to display, click Settings<br>3) Scroll down until you see the Embed Code<br>4) Copy that code and paste it into URL field here<br>5) Press 'Save Preferences'", type:"paragraph", element:"paragraph"
    input "gCal", "text", title: "Google Calendar URL",  required: true
    input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false, submitOnChange: true
    input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"], defaultValue: "1 Hour"
}

def refresh() {
    if(logEnable) log.debug "In refresh"
    if(gCal.contains("<iframe src=\"")) gCal = gCal.replace("<iframe src=\"", "").replace(">", "").replace("</iframe>", "")
    if(logEnable) log.debug "In refresh - gCal URl: ${gCal}"
    lu = new Date()
    theCal = "<div style='height:100%;width:100%'><iframe src='${gCal}' style='height:100%;width:100%;border:none'></iframe></div>"
    sendEvent(name: "gCal", value: theCal)
    sendEvent(name: "lastUpdated", value: lu)
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
    refresh()
    schedule("0 0 * ? * * *", refresh)
}

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
}
