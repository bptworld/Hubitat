/**
 *  ****************  Home Tracker Parent App  ****************
 *
 *  Design Usage:
 *  Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *-------------------------------------------------------------------------------------------------------------------
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *.
 *  V2.0.1 - 10/04/19 - added pronunciation to friendly names (aaronward)
 *  V2.0.0 - 09/10/19 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "HomeTrackerParentVersion"
	state.version = "v2.0.0"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name:"Home Tracker",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Home%20Tracker/HT-parent.groovy",
)

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "There are ${childApps.size()} child apps"
    childApps.each {child ->
    log.info "Child app: ${child.label}"
    }
    if(awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section(getFormat("title", "${app.label}")) {
				paragraph "<div style='color:#1A77C9'>Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
        		paragraph "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message <i>after</i> entering the home!"
        	paragraph "<b>Type of 'Welcome Home' Triggers:</b>"
    			paragraph "<b>Unlock or Door Open</b><br>Both of these work pretty much the same. When door or lock is triggered, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement."
				paragraph "Each trigger can have multiple selections but this is an 'or' function. Meaning it only takes one device to trigger the actions. ie. Door1 or Door2 has been opened. If you require a different delay per door/lock, then separate child apps would be required - one for each door or lock."
				paragraph "<b>Motion Sensor</b><br>When motion sensor becomes active, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement. If you require a different delay per motion sensor, then separate child apps would be required - one for each motion sensor."
				paragraph "This trigger also works with Hubitat's built in 'Zone Motion Controllers' app. Which allows you to do some pretty cool things with motion sensors."
				paragraph "<b>Requirements:</b>"
				paragraph "Be sure to complete the 'Advanced Config' section before creating Child Apps."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				paragraph "<b>Be sure to complete the 'Advanced Config' section before creating Child Apps.</b>"
				app(name: "anyOpenApp", appName: "Home Tracker Child", namespace: "BPTWorld", title: "<b>Add a new 'Home Tracker' child</b>", multiple: true)
			}
            // ** App Watchdog Code **
            section("This app supports App Watchdog 2! Click here for more Information", hideable: true, hidden: true) {
				paragraph "<b>Information</b><br>See if any compatible app needs an update, all in one place!"
                paragraph "<b>Requirements</b><br> - Must install the app 'App Watchdog'. Please visit <a href='https://community.hubitat.com/t/release-app-watchdog/9952' target='_blank'>this page</a> for more information.<br> - When you are ready to go, turn on the switch below<br> - Then select 'App Watchdog Data' from the dropdown.<br> - That's it, you will now be notified automaticaly of updates."
                input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")
			}
            if(sendToAWSwitch) {
                section(getFormat("header-green", "${getImage("Blank")}"+" App Watchdog 2")) {    
                    if(sendToAWSwitch) input(name: "awDevice", type: "capability.actuator", title: "Please select 'App Watchdog Data' from the dropdown", submitOnChange: true, required: true, multiple: false)
			        if(sendToAWSwitch && awDevice) setVersion()
                }
            }
            // ** End App Watchdog Code **
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Config")) {}
			section("Presence Sensors:", hideable: true, hidden: true) {
				paragraph "Enter in your 'Friendly Names' for the presence sensors to be used with this app. You'll still attach them to the presence sensors within each child app. This will keep them in order across multiple child apps."
				input "friendlyName1", "text", title: "Friendly name for presence sensor 1", required: true, multiple: false, defaultValue: "Not set"
                input "pronounce1", "text", title: "Pronunciation for presence sensor 1", required: false, multiple: false, defaultValue: "Not set"
				input "friendlyName2", "text", title: "Friendly name for presence sensor 2", required: false, multiple: false, defaultValue: "Not set" 
				input "pronounce2", "text" , title: "Pronunciation for presence sensor 2", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName3", "text", title: "Friendly name for presence sensor 3", required: false, multiple: false, defaultValue: "Not set"
				input "pronounce3", "text", title: "Pronunciation for presence sensor 3", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName4", "text", title: "Friendly name for presence sensor 4", required: false, multiple: false, defaultValue: "Not set"
				input "pronounce4", "text", title: "Pronunciation for presence sensor 4", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName5", "text", title: "Friendly name for presence sensor 5", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce5", "text", title: "Pronunciation for presence sensor 5", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName6", "text", title: "Friendly name for presence sensor 6", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce6", "text", title: "Pronunciation for presence sensor 6", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName7", "text", title: "Friendly name for presence sensor 7", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce7", "text", title: "Pronunciation for presence sensor 7", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName8", "text", title: "Friendly name for presence sensor 8", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce8", "text", title: "Pronunciation for presence sensor 8", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName9", "text", title: "Friendly name for presence sensor 9", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce9", "text", title: "Pronunciation for presence sensor 9", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName10", "text", title: "Friendly name for presence sensor 10", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce10", "text", title: "Pronunciation for presence sensor 10", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName11", "text", title: "Friendly name for presence sensor 11", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce11", "text", title: "Pronunciation for presence sensor 11", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName12", "text", title: "Friendly name for presence sensor 12", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce12", "text", title: "Pronunciation for presence sensor 12", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName13", "text", title: "Friendly name for presence sensor 13", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce13", "text", title: "Pronunciation for presence sensor 13", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName14", "text", title: "Friendly name for presence sensor 14", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce14", "text", title: "Pronunciation for presence sensor 14", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName15", "text", title: "Friendly name for presence sensor 15", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce15", "text", title: "Pronunciation for presence sensor 15", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName16", "text", title: "Friendly name for presence sensor 16", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce16", "text", title: "Pronunciation for presence sensor 16", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName17", "text", title: "Friendly name for presence sensor 17", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce17", "text", title: "Pronunciation for presence sensor 17", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName18", "text", title: "Friendly name for presence sensor 18", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce18", "text", title: "Pronunciation for presence sensor 18", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName19", "text", title: "Friendly name for presence sensor 19", required: false, multiple: false, defaultValue: "Not set"
                input "pronounce19", "text", title: "Pronunciation for presence sensor 19", required: false, multiple: false, defaultValue: "Not set"
                input "friendlyName20", "text", title: "Friendly name for presence sensor 20", required: false, multiple: false, defaultValue: "Not set"
	            input "pronounce20", "text", title: "Pronunciation for presence sensor 20", required: false, multiple: false, defaultValue: "Not set"		
            }
		}
		display()
	}
}

def installCheck(){         
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  	}
  	else{
    	log.info "Parent Installed OK"
  	}
}

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Home Tracker - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  
