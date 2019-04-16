/**
 *  ****************  App Watchdog Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the App Watcher data to be used with Hubitat's Dashboards.
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
 *	V1.0.2 - 04/15/19 - Code cleanup, added importUrl
 *  V1.0.1 - 02/08/19 - Messing around with attributes
 *  V1.0.0 - 02/07/19 - Initial release
 */

metadata {
	definition (name: "App Watchdog Tile", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/App%20Watchdog/AW-driver.groovy") {
   		capability "Actuator"

		command "sendDataMap", ["string"]
		command "sendAWinfo", ["string"]
		
    	attribute "appVersions", "string"
		attribute "app01", "string"
		attribute "app02", "string"
		attribute "app03", "string"
		attribute "app04", "string"
		attribute "app05", "string"
		
	}
	preferences() {    	
        section(""){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def sendDataMap(dataMap) {
    if(logEnable) log.debug "In App Watchdog Tile - Received new app data!"
	state.appData = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.appData+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${dataMap}</div>"
	state.appData+= "</td></tr></table>"
	sendEvent(name: "appVersions", value: state.appData, displayed: true)
}

def sendAWinfo(verMap) {
    if(logEnable) log.debug "In App Watchdog Tile - Received new version data!"
	if(logEnable) log.debug "In sendVersionMap..."
	def newMap = "${verMap}"
	def (appName, appVer) = newMap.split(':')
	if(logEnable) log.debug "In sendVersionMap...appName: ${appName} - appVer: ${appVer}"
	
	if(state.versionMap == null) state.versionMap = [:]
	
	state.versionMap.put(appName, appVer)
	
	sendEvent(name: "${theApp}", value: state.appVersion, displayed: true)
}
	
