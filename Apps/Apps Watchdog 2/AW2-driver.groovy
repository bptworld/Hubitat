/**
 *  ****************  Apps Watchdog 2 Driver  ****************
 *
 *  Design Usage:
 *  This driver captures the version information from participating apps.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  Changes:
 *
 *  V2.0.3 - 08/27/19 - add a 'Clear Data' option, Minor code changes
 *  V2.0.2 - 08/25/19 - Code cleanup by @stephack, thank you! Also sorted the data.
 *  V2.0.1 - 08/24/19 - Bug fixes
 *  V2.0.0 - 08/18/19 - Initial release
 */

def setVersion(){
    appName = "AppWatchdog2DriverVersion"
	version = "v2.0.3" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "driverInfo", value: dwInfo, displayed: true)
}

metadata {
	definition (name: "App Watchdog 2 Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Apps%20Watchdog%202/AW2-driver.groovy") {
        capability "Actuator"
		
        attribute "sendAWinfoMap", "string"
        attribute "appTile", "string"
        attribute "driverInfo", "string"
        
        command "sendAWinfoMap", ["Text"]
        command "sendDataMap", ["Text"]
        command "clearData"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "App Watchdog", description: "ONLY click 'Clear Data' to clear the version data. Once pressed, all apps will have to resend their info, usually done the next morning."
            input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
        }
    }
}

def sendAWinfoMap(appWatchdogData) {
    if(logEnable) log.debug "In sendAWinfo (${state.version})"
    
	def newData = stringToMap(appWatchdogData) // this converts the string to a map
    if(logEnable) log.debug "In sendAWinfo - Incoming Data - ${newData}"
    
    state.theDataMap << newData
    if(logEnable) log.debug "In sendAWinfoMap - newData: ${newData}"
    
    def allMap = state.theDataMap.collectEntries{ [(it.key):(it.value)] }
    
    allMapS = allMap.sort{it.key}
    
    allMapSize = allMapS.size()
    if(logEnable) log.debug "In sendAWinfoMap - mapSize: ${allMapSize} - allMap: ${allMapS}"
    
    sendEvent(name: "sendAWinfoMap", value: allMapS, displayed: true)
}

def installed(){
    log.info "Apps Watchdog Device has been Installed"
    initialize()
}

def updated() {
    log.info "Apps Watchdog Device has been Updated"
    setVersion()
}

def initialize() {
    log.info "In initialize"
    setVersion()
}

def sendDataMap(dataMap) {
    if(logEnable) log.debug "In sendDataMap - Received new app data!"
	theTile = "<table width='100%'><tr><td width='10'><td align='left'>"
	theTile += "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${dataMap}</div>"
	theTile += "</td></tr></table>"
	sendEvent(name: "appTile", value: theTile, displayed: true)
}

def clearData() {
    log.info "In initialize - Clearing maps"
    state.theDataMap = [:]
    sendEvent(name: "sendAWinfoMap", value: state.theDataMap, displayed: true)
}

