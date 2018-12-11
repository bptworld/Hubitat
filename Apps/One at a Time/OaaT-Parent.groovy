/**
 *  ****************  One at a Time Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to allow only one switch, in a group of switches, to be on at a time.
 *	When one switch is turned on, the other swithes in the group will turn off.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to Jody Albritton for the original 'One At A Time Please' code that I based this app off of.
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
 *  V1.1.0 - 12/10/18 - Changed over to Parent/Child type app. Create as many groups as you like. Also added in all the normal
 *						stuff: Enable/Disable switch, Pause switch and Logging options.
 *  V1.0.0 - 12/09/18 - Hubitat Port of ST app 'One at a Time Please' - 2015 Jody Albritton
 *
 */

definition(
    name:"One at a Time",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "This app is designed to allow only one switch, in a group of switches, to be on at a time.",
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
					paragraph "This app is designed to allow only one switch, in a group of switches, to be on at a time."
				}
				section("Instructions:", hideable: true, hidden: true) {
					paragraph "<b>Notes:</b>"
					paragraph "* When one switch is turned on, the other switches in the group will turn off.<br>* Create as many groups as you like.<br>* Great for making sure only one scene is active at a time!"	
				}
  				section("Child Apps", hideable: true, hidden: true){
					app(name: "anyOpenApp", appName: "One at a Time Child", namespace: "BPTWorld", title: "<b>Add a new 'One at a Time' child</b>", multiple: true)
  			    }
 			 	section("App Name"){
       				label title: "Enter a name for parent app (optional)", required: false
 				}
		}
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

def display(){
	section{
		paragraph "<b>One at a Time</b><br>App Version: 1.1.0<br>@BPTWorld"
	}        
}         
