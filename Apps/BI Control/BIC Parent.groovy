/**
 *  ****************  BI Control Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to work locally with Blue Iris security software.
 *
 *  Copyright 2018-2020 Bryan Turcotte (@bptworld)
 *
 *  Thanks to (@jpark40) for the original 'Blue Iris Profiles based on Modes' code that I based this app off of.
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  2.0.1 - 04/27/20 - Cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog 2 compliant
 *  1.0.3 - 01/15/19 - Updated footer with update check and links
 *  1.0.4 - 12/30/18 - Updated to my new color theme. Applied pull request from the-other-andrew - Added Mode mappings and switch
 *						support for Blue Iris schedules.
 *  1.0.3 - 11/25/18 - Added PTZ camera controls.
 *  1.0.2 - 11/05/18 - Added in the ability to move a camera to a Preset. Also added the ability to take a camera snapshot and
 *						to start or stop manual recording on camera from a Switch.
 *  1.0.1 - 11/03/18 - Changed into Parent/Child app. BI Control now works with Modes and Switches to change Profiles.
 *  1.0.0 - 11/03/18 - Hubitat Port of ST app 'Blue Iris Profiles based on Modes' - 2016 (@jpark40)
 *
 */

def setVersion(){
	state.version = "v2.0.1"
}

definition(
    name:"BI Control",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Parent App for 'BI Control' childapps ",
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
				paragraph "<div style='color:#1A77C9'>This app is designed to work locally with Blue Iris security software.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Notes:</b>"
				paragraph "BI Control keeps everything local, no Internet required!"
				paragraph "This app uses 'Virtual Switches', instead of buttons. That way the devices can be used within Google Assistant and Rule Machine. Be sure to set 'Enable auto off' within each Virtual Device to '1s' (except for recording device)."
        		paragraph "<b>Blue Iris requirements:</b>"
				paragraph "In Blue Iris settings > Web Server > Advanced > Advanced Settings<br> - Ensure 'Use secure session keys and login page' is not checked.<br> - Disable authentication, select “Non-LAN only” (preferred) or “No” to disable authentication altogether.<br> - Blue Iris only allows Admin Users to toggle profiles."	
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "BI Control Child", namespace: "BPTWorld", title: "<b>Add a new 'BI Control' child</b>", multiple: true)
  			}
            
   			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Blue Iris Server Config")) {
				paragraph "<b>Please be sure to setup the Blue Iris server per the instructions above.</b>"
				paragraph "Use the local IP address for Host, do not include http:// or anything but the IP address. ie. 192.168.1.123"
				input "biServer", "text", title: "Server", description: "Blue Iris web server IP", required: true
				input "biPort", "number", title: "Port", description: "Blue Iris web server port", required: true
				input "biUser", "text", title: "User name", description: "Blue Iris user name", required: true
				input "biPass", "password", title: "Password", description: "Blue Iris password", required: true
			}
		}
		display()
	}
}

def update() {
	if(biServer) childApps.each {child -> child.mymsgbiServer(biServer)}
	if(biPort) childApps.each {child -> child.mymsgbiPort(biPort)}
	if(biUser) childApps.each {child -> child.mymsgbiUser(biUser)}
	if(biPass) childApps.each {child -> child.mymsgbiPass(biPass)}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>BI Control - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a><br>${state.version}</div>"
	}       
}         
