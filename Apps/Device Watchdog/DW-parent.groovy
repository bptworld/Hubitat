/**
 *  ****************  Device Watchdog Parent App  ****************
 *
 *  Design Usage:
 *  Keep an eye on your devices and see how long it's been since they checked in.
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
 *  V1.0.3 - 12/30/18 - Added 'app child name' to Pushover reports
 *  V1.0.2 - 12/29/18 - Changed wording on Push notification option to specify Pushover.
 *						Added option to select 'all devices' for Battery Level trigger.
 *						Fixed Pushover to send a 'No devices to report' message instead of a blank message.
 *  V1.0.1 - 12/27/18 - Code cleanup.
 *  V1.0.0 - 12/21/18 - Initial release.
 *
 */

definition(
    name:"Device Watchdog",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Keep an eye on your devices and see how long it's been since they checked in.",
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
					paragraph "Keep an eye on your devices and see how long it's been since they checked in."
				}
				section("Instructions:", hideable: true, hidden: true) {
					paragraph "<b>Notes:</b>"
					paragraph "- Devices may show up in multiple lists but each device only needs to be selected once.<br>- Be sure to generate a new report before trying to view the 'Last Device Status Report'.<br>- All changes are saved right away, no need to exit out and back in before generating a new report."
				}
  				section("Child Apps", hideable: true, hidden: true){
					app(name: "anyOpenApp", appName: "Device Watchdog Child", namespace: "BPTWorld", title: "<b>Add a new 'Device Watchdog' child</b>", multiple: true)
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
		paragraph "<b>Device Watchdog</b><br>App Version: 1.0.3<br>@BPTWorld"
	}        
}         
