/**
 *  ****************  Welcome Home Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to give a personal welcome announcement after you have entered the home.
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
 *
 *  V2.0.3 - 08/18/19 - Now App Watchdog compliant
 *  V2.0.2 - 04/06/19 - Added importUrl
 *  V2.0.1 - 02/26/19 - Reworked how the messages are stored. Added option to have random greetings. Removed Greeting and Messages
 *						from Parent app.
 *  V2.0.0 - 02/11/19 - Major rewrite. Presence sensors are now in Parent app, so they can be shared across multiple child apps.
 *						Welcome Home now requires a new 'Virtual Device' using our custom 'Global Variables Driver'.  Each child app
 *                      will link to the same 'Virtual Device'.  This way we can track who came home across multiple child apps!
 *  V1.1.1 - 01/15/19 - Updated footer with update check and links
 *  V1.1.0 - 01/13/19 - Updated to announce multiple people coming home at the same time, in one message. Seems like such a simple
 *						thing but it took a huge rewrite to do it!
 *  V1.0.8 - 12/30/18 - Updated to my new color theme.
 *  V1.0.7 - 12/07/18 - Added an option to Contact Sensor trigger. Can now trigger based on Open or Closed.
 *  V1.0.6 - 12/04/18 - Code rewrite so we don't have to fill in all 20 presets. Must state in child app how many presets to use.
 *  V1.0.5 - 12/01/18 - Added 10 more random message presets! Fixed (hopefully) an issue with announcements not happening under
 *                      certain conditions. THE PARENT AND ALL CHILD APPS MUST BE OPENED AND SAVED AGAIN FOR THIS TO WORK.
 *  V1.0.4 - 11/30/18 - Found a bad bug and fixed it ;)
 *  V1.0.3 - 11/30/18 - Changed how the options are displayed, removed the Mode selection as it is not needed.
 *  V1.0.2 - 11/29/18 - Added an Enable/Disable child app switch. Fix an issue with multiple announcements on same arrival.
 *  V1.0.1 - 11/28/18 - Upgraded some of the logic and flow of the app. Added Motion Sensor Trigger, ability to choose multiple
 *  					door, locks or motion sensors. Updated the instructions.
 *  V1.0.0 - 11/25/18 - Initial release.
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "WelcomeHomeParentVersion"
	state.version = "v2.0.3"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
            schedule("0 0 3 ? * * *", setVersion)
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name:"Welcome Home",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "This app is designed to give a personal welcome announcement after you have entered the home.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Welcome%20Home/WH%20Parent.groovy",
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
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section(getFormat("title", "${app.label}")) {
				paragraph "<div style='color:#1A77C9'>This app is designed to give a personal welcome announcement after you have entered the home.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
        		paragraph "<b>Types of Triggers:</b>"
    			paragraph "<b>Unlock or Door Open</b><br>Both of these work pretty much the same. When door or lock is triggered, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement."
				paragraph "Each trigger can have multiple selections but this is an 'or' function. Meaning it only takes one device to trigger the actions. ie. Door1 or Door2 has been opened. If you require a different delay per door/lock, then separate child apps would be required - one for each door or lock."
				paragraph "<b>Motion Sensor</b><br>When motion sensor becomes active, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement. If you require a different delay per motion sensor, then separate child apps would be required - one for each motion sensor."
				paragraph "This trigger also works with Hubitat's built in 'Zone Motion Controllers' app. Which allows you to do some pretty cool things with motion sensors."
				paragraph "<b>Notes:</b>"
				paragraph "This app is designed to give a personal welcome announcement <i>after</i> you have entered the home."
				paragraph "<b>Requirements:</b>"
				paragraph "Be sure to complete the 'Advanced Config' section before creating Child Apps."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				paragraph "<b>Be sure to complete the 'Advanced Config' section before creating Child Apps.</b>"
				app(name: "anyOpenApp", appName: "Welcome Home Child", namespace: "BPTWorld", title: "<b>Add a new 'Welcome Home' child</b>", multiple: true)
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
				input "friendlyName2", "text", title: "Friendly name for presence sensor 2", required: false, multiple: false, defaultValue: "Not set"
				input "friendlyName3", "text", title: "Friendly name for presence sensor 3", required: false, multiple: false, defaultValue: "Not set"
				input "friendlyName4", "text", title: "Friendly name for presence sensor 4", required: false, multiple: false, defaultValue: "Not set"
				input "friendlyName5", "text", title: "Friendly name for presence sensor 5", required: false, multiple: false, defaultValue: "Not set"
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Welcome Home - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  
