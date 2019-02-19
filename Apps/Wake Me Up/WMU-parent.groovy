/**
 *  ****************  Wake Me Up Parent App  ****************
 *
 *  Design Usage:
 *  A better way to wake up, with a slowly raising light level and a random announcement.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
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
 *
 *  V1.0.0 - 02/18/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
    name:"Wake Me Up",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "A better way to wake up, with a slowly raising light level and a random announcement.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
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
				paragraph "<div style='color:#1A77C9'>A better way to wake up, with a slowly raising light level and a random announcement.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Notes:</b>"
				paragraph "A better way to wake up, with a slowly raising light level and a random announcement."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				paragraph "<b>Be sure to complete the 'Advanced Config' section before creating Child Apps.</b>"
				app(name: "anyOpenApp", appName: "Wake Me Up Child", namespace: "BPTWorld", title: "<b>Add a new 'Wake Me Up' child</b>", multiple: true)
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Config")) {}
            section("Messages:", hideable: true, hidden: true) {
				paragraph "<br>%name% - returns the Friendly Name associcated within a child app<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors<br>Note: adding a . anywhere will give the message a little pause<br><b>Be sure to hit 'Done' after editing.</b>"
            	input "msg1", "text", required: true, title: "Message - 1", defaultValue: "Good morning. %name%"
                input "msg2", "text", required: false, title: "Message - 2", defaultValue: "Time to get up."
                input "msg3", "text", required: false, title: "Message - 3", defaultValue: "Rise and shine "
                input "msg4", "text", required: false, title: "Message - 4", defaultValue: "Nice to see you again. %name%"
                input "msg5", "text", required: false, title: "Message - 5", defaultValue: "Hey, wake up!"
                input "msg6", "text", required: false, title: "Message - 6", defaultValue: "Come on, get out of bed."
                input "msg7", "text", required: false, title: "Message - 7", defaultValue: "It's going to be a beautiful day."
                input "msg8", "text", required: false, title: "Message - 8", defaultValue: "Let's do this. %name%"
                input "msg9", "text", required: false, title: "Message - 9", defaultValue: "Are you still in bed?"
                input "msg10", "text", required: false, title: "Message - 10", defaultValue: "Wake up. Wake up."
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

def display(){
	section() {
		setVersion()
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Wake Me Up - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  

