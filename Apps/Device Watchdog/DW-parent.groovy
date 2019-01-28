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
 *  V1.0.9 - 01/18/19 - Not going to update Parent apps anymore unless there is a change in the Parent app, not just the child.
 *  V1.0.8 - 01/15/19 - Updated footer with update check and links
 *  V1.0.7 - 01/04/19 - Modification by rayzurbock. Report now shows 'battery level isn't reporting' when a device's battery
 *						attribute is null/blank/non-existent. Previously it showed 0.  Also adjusted the output on the Push report.
 *  V1.0.6 - 01/01/19 - Fixed typo in Pushover module.
 *  V1.0.5 - 12/31/18 - Fixed debug logging.
 *  V1.0.4 - 12/30/18 - Updated to my new color theme.
 *  V1.0.3 - 12/30/18 - Added 'app child name' to Pushover reports
 *  V1.0.2 - 12/29/18 - Changed wording on Push notification option to specify Pushover.
 *						Added option to select 'all devices' for Battery Level trigger.
 *						Fixed Pushover to send a 'No devices to report' message instead of a blank message.
 *  V1.0.1 - 12/27/18 - Code cleanup.
 *  V1.0.0 - 12/21/18 - Initial release.
 *
 */

def version(){"v1.0.9"}

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
			section(getFormat("title", "${app.label}")) {
				paragraph "<div style='color:#1A77C9'>Keep an eye on your devices and see how long it's been since they checked in.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Notes:</b>"
				paragraph "- Devices may show up in multiple lists but each device only needs to be selected once.<br>- Be sure to generate a new report before trying to view the 'Last Device Status Report'.<br>- All changes are saved right away, no need to exit out and back in before generating a new report."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "Device Watchdog Child", namespace: "BPTWorld", title: "<b>Add a new 'Device Watchdog' child</b>", multiple: true)
  			}
 			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       				label title: "Enter a name for parent app (optional)", required: false
 			}
			display()
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
	def params = [uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Watchdog/version.json",
				   	contentType: "application/json"]
       	try {
			httpGet(params) { response ->
				def results = response.data
				def appStatus
				if(version() == results.currParentVersion){
					appStatus = "${version()} - No Update Available - ${results.discussion}"
				}
				else {
					appStatus = "<div style='color:#FF0000'>${version()} - Update Available (${results.currParentVersion})!</div><br>${results.parentRawCode}  ${results.childRawCode}  ${results.driverRawCode}  ${results.discussion}"
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${verUpdate}</div>"
	}       
}         
