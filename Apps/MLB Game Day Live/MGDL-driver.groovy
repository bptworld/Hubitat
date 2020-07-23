/**
 *  ****************  MLB Game Day Live Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the MLB Game Day Live data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums! Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
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
 *  V1.0.0 - 07/21/20 - Initial release.
 */

metadata {
	definition (name: "MLB Game Day Live Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"

        command "liveScoreboard", ["string"]
        command "gameSchedule", ["string"]
        command "gameStats", ["string"]
        
        attribute "bpt-liveScoreboard", "string"
        attribute "awayTeam", "string"
        attribute "homeTeam", "string"
        attribute "totalAwayRuns", "string"
        attribute "totalHomeRuns", "string"
        attribute "currentInning", "string"       
               
        attribute "bpt-gameSchedule", "string"
        attribute "gameScheduleCount", "number"
                
        attribute "lastUpdated", "string"
	}
	preferences() {    	
        section(){
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
def liveScoreboard(data) {
    if(logEnable) log.debug "In liveScoreboard - Received new data!"
    sendEvent(name: "bpt-liveScoreboard", value: data, isStateChange: true)
}

def gameSchedule(data) {
    if(logEnable) log.debug "In gameSchedule - Received new data!"
    sendEvent(name: "bpt-gameSchedule", value: data, isStateChange: true)
}

def gameStats(data) {
    if(logEnable) log.debug "In gameStats - Received new data!"
    theData = data.split(";")
    sendEvent(name: "awayTeam", value: theData[0], isStateChange: true)
    sendEvent(name: "homeTeam", value: theData[1], isStateChange: true)
    sendEvent(name: "totalAwayRuns", value: theData[2], isStateChange: true)
    sendEvent(name: "totalHomeRuns", value: theData[3], isStateChange: true)
    sendEvent(name: "currentInning", value: theData[4], isStateChange: true)
}
