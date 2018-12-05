/**
 *  ****************  BI Control Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to work locally with Blue Iris security software.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to (@jpark40) for the original 'Blue Iris Profiles based on Modes' code that I based this app off of.
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
 *  V1.0.3 - 11/25/18 - Added PTZ camera controls.
 *  V1.0.2 - 11/05/18 - Added in the ability to move a camera to a Preset. Also added the ability to take a camera snapshot and
 *						to start or stop manual recording on camera from a Switch.
 *  V1.0.1 - 11/03/18 - Changed into Parent/Child app. BI Control now works with Modes and Switches to change Profiles.
 *  V1.0.0 - 11/03/18 - Hubitat Port of ST app 'Blue Iris Profiles based on Modes' - 2016 (@jpark40)
 *
 */

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
			display()
				section() {
					paragraph "This app is designed to work locally with Blue Iris security software."
				}
				section("Instructions:", hideable: true, hidden: true) {
					paragraph "<b>Notes:</b>"
					paragraph "BI Control keeps everything local, no Internet required!"
					paragraph "This app uses 'Virtual Switches', instead of buttons. That way the devices can be used within Google Assistant and Rule Machine. Be sure to set 'Enable auto off' within each Virtual Device to '1s' (except for recording device)."
        			paragraph "<b>Blue Iris requirements:</b>"
					paragraph "In Blue Iris settings > Web Server > Advanced > Advanced Settings<br> - Ensure 'Use secure session keys and login page' is not checked.<br> - Disable authentication, select “Non-LAN only” (preferred) or “No” to disable authentication altogether.<br> - Blue Iris only allows Admin Users to toggle profiles."	
				}
  				section("Child Apps", hideable: true, hidden: true){
					app(name: "anyOpenApp", appName: "BI Control Child", namespace: "BPTWorld", title: "<b>Add a new 'BI Control' child</b>", multiple: true)
  			    }
   				 section(" "){}
 			 	section("App Name"){
       				label title: "Enter a name for parent app (optional)", required: false
 				}

            	section("Blue Iris Server Config:", hideable: true, hidden: true) {
					paragraph "<b>Please be sure to setup the Blue Iris server per the instructions above.</b>"
					paragraph "Use the local IP address for Host, do not include http:// or anything but the IP address. ie. 192.168.1.123"
					input "biServer", "text", title: "Server", description: "Blue Iris web server IP", required: true
					input "biPort", "number", title: "Port", description: "Blue Iris web server port", required: true
					input "biUser", "text", title: "User name", description: "Blue Iris user name", required: true
					input "biPass", "password", title: "Password", description: "Blue Iris password", required: true
				}
		}
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

def display(){
	section{paragraph "Version: 1.0.3<br>@BPTWorld"}     
}         

def setVersion(){
		state.InternalName = "BIControlParent"  
}



