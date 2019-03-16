/**
 *  ****************  Departures Parent App  ****************
 *
 *  Design Usage:
 *  Let the rest of the house know when one or more people have left the area.
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
 *  V1.0.0 - 03/15/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
    name:"Departures",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Let the rest of the house know when one or more people have left the area.",
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
				paragraph "<div style='color:#1A77C9'>Let the rest of the house know when one or more people have left the area.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
        		paragraph ""
				paragraph "<b>Requirements:</b>"
				paragraph "Be sure to complete the 'Advanced Config' section before creating Child Apps."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				paragraph "<b>Be sure to complete the 'Advanced Config' section before creating Child Apps.</b>"
				app(name: "anyOpenApp", appName: "Departures Child", namespace: "BPTWorld", title: "<b>Add a new 'Departures' child</b>", multiple: true)
			}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Departures - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  
