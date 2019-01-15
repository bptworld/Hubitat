/**
 *  ****************  Welcome Home Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to give a personal welcome announcement after you have entered the home.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
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

def version(){"v1.1.1"}

definition(
    name:"Welcome Home",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Parent App for 'Welcome Home' childapps ",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
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
				paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "Welcome Home Child", namespace: "BPTWorld", title: "<b>Add a new 'Welcome Home' child</b>", multiple: true)
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Config")) {
				paragraph "<b>Be sure to enter in the Preset Values in Advanced Config before creating Child Apps</b>"
			}
            section("Advanced Config:", hideable: true, hidden: true) {
				paragraph "<br>%greeting% - will return a greeting based on time of day.<br>%name% - will return the Friendly Name associcated with a Presence Sensor<br>Note: adding a . anywhere will give the message a little pause"
				input "greeting1", "text", required: true, title: "Greeting - 1 (am)", defaultValue: "Good Morning"
                input "greeting2", "text", required: true, title: "Greeting - 2 (before 6pm)", defaultValue: "Good Afternoon"
                input "greeting3", "text", required: true, title: "Greeting - 3 (after 6pm)", defaultValue: "Good Evening"
				paragraph "<br>"
            	input "msg1", "text", required: true, title: "Message - 1", defaultValue: "Welcome home. %name%"
                input "msg2", "text", required: false, title: "Message - 2", defaultValue: "Long time no see. %name%"
                input "msg3", "text", required: false, title: "Message - 3", defaultValue: "Look who's home. it's %name%"
                input "msg4", "text", required: false, title: "Message - 4", defaultValue: "Nice to have you back. %name%"
                input "msg5", "text", required: false, title: "Message - 5", defaultValue: "%greeting%. %name%"
                input "msg6", "text", required: false, title: "Message - 6", defaultValue: "%greeting%. Oh ya. %name% is home"
                input "msg7", "text", required: false, title: "Message - 7", defaultValue: "How are you doing. %name%"
                input "msg8", "text", required: false, title: "Message - 8", defaultValue: "%greeting%. Anything I can do for you. %name%"
                input "msg9", "text", required: false, title: "Message - 9", defaultValue: "%greeting% I'm at your service, %name%"
                input "msg10", "text", required: false, title: "Message - 10", defaultValue: "%greeting%. The dogs have been waiting for you. %name%"
				input "msg11", "text", required: false, title: "Message - 11", defaultValue: ""
                input "msg12", "text", required: false, title: "Message - 12", defaultValue: ""
                input "msg13", "text", required: false, title: "Message - 13", defaultValue: ""
                input "msg14", "text", required: false, title: "Message - 14", defaultValue: ""
                input "msg15", "text", required: false, title: "Message - 15", defaultValue: ""
                input "msg16", "text", required: false, title: "Message - 16", defaultValue: ""
                input "msg17", "text", required: false, title: "Message - 17", defaultValue: ""
                input "msg18", "text", required: false, title: "Message - 18", defaultValue: ""
                input "msg19", "text", required: false, title: "Message - 19", defaultValue: ""
                input "msg20", "text", required: false, title: "Message - 20", defaultValue: ""
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

def checkForUpdate(){
	def params = [uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Welcome%20Home/version.json",
				   	contentType: "application/json"]
       	try {
			httpGet(params) { response ->
				def results = response.data
				def appStatus
				if(version() == results.currVersion){
					appStatus = "${version()} - No Update Available - ${results.discussion}"
				}
				else {
					appStatus = "<div style='color:#FF0000'>${version()} - Update Available (${results.currVersion})!</div><br>${results.parentRawCode}  ${results.childRawCode}  ${results.discussion}"
					log.warn "${app.label} has an update available - Please consider updating."
				}
				return appStatus
			}
		} 
        catch (e) {
        	log.error "Error:  $e"
    	}
}

def display(){
	section() {
		def verUpdate = "${checkForUpdate()}"
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Welcome Home - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${verUpdate}</div>"
	}       
}  

