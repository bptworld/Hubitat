/**
 *  ****************  Send IP2IR Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
 *
 *  Thanks to Carson Dallum's (@cdallum) for the original IP2IR driver code that I based mine off of.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.1.7 - 01/15/19 - Updated footer with update check and links
 *  V1.1.6 - 12/30/18 - Updated to my new color theme.
 *  V1.1.5 - 12/06/18 - Code cleanup, removal of IP Address from Child Apps as it was not needed anymore. 
 *  V1.1.4 - 11/30/18 - Added pause button to child apps. Added an Enable/Disable by switch option. Cleaned up code.
 *  V1.1.3 - 11/02/18 - Added the ability to send multiple Switch On's, Off's or both and Button's to send mutilple times with
 *                      each push. Also Fixed some typo's.
 *  V1.1.2 - 11/01/18 - Added an optional Digit 4 within Channels. Sending Enter Code after Digits is now optional. Made the
 *                      Delay between sending digits user specified and added in some instructions.
 *  V1.1.1 - 10/29/18 - Updated Channels to be either a Button or a Switch, only Switches can be used with Google Assistant.  
 *  V1.1.0 - 10/20/18 - Big change in how Channels work. Only have to enter each digits IR code once, in the Advance Section of
 *			 			the Parent app. Now in the Child apps, only need to put in the digits (no IR codes!). This is a 
 *			 			non-destructive update. All existing channels will still work. Thanks to Bruce (@bravenel) for showing
 *						me how to send code from parent to child apps.
 *  V1.0.0 - 10/15/18 - Initial release
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "SendIP2IRParentVersion"
	state.version = "v2.0.0"
    
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
    name:"Send IP2IR",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "This app is designed to send commands to an iTach IP2IR device.",
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
				paragraph "<div style='color:#1A77C9'>This app is designed to send commands to an iTach IP2IR device.<br>Be sure to enter in the Preset Values in Advanced Config before creating Child Apps</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "There are 4 types of Triggers that can be made."
        		paragraph "<b>Switch:</b><br>To turn anything on/off. ie. Television, Stereo, Cable Box, etc. Remember, it's okay to put the same code in box on and off if necessary."
    			paragraph "<b>Button:</b><br>Used to send one command. ie. Volume Up, Channel Down, etc. Note: this can not be used with Google Assistant."
        		paragraph "<b>Channel_Switch:</b><br>Used to send 1 to 4 commands at the same time. This is used to send Channels numbers based on the Presets in the Parent app."
        		paragraph "<b>Channel_Button:</b><br>Also, used to send 1 to 4 commands at the same time. This is used to send Channels numbers based on the Presets in the Parent app. Note: this can not be used with Google Assistant."
        		paragraph "<b>Important:</b><br>Each child app takes a device to trigger the commands, so be sure to create either a Virtual Switch or Virtual Button before trying to create a child app."
				paragraph "<b>Google Assistant Notes:</b><br>Google Assistant only works with switches. If creating virtual switches for channels, be sure to use the 'Enable auto off' @ '500ms' to give the effect of a button in a Dashboard but still be able to tell Google to control it."
				}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "Send IP2IR Child", namespace: "BPTWorld", title: "<b>Add a new 'Send IP2IR' child</b>", multiple: true)
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
			section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Config")) {
            	paragraph "<b>Be sure to enter in the Preset Values in Advanced Config before creating Child Apps</b>"
			}
            	section("Advanced Config:", hideable: true, hidden: true) {
            		input "msgDigit1", "text", required: true, title: "IR Code to Send - 1", defaultValue: ""
                    input "msgDigit2", "text", required: true, title: "IR Code to Send - 2", defaultValue: ""
                    input "msgDigit3", "text", required: true, title: "IR Code to Send - 3", defaultValue: ""
                    input "msgDigit4", "text", required: true, title: "IR Code to Send - 4", defaultValue: ""
                    input "msgDigit5", "text", required: true, title: "IR Code to Send - 5", defaultValue: ""
                    input "msgDigit6", "text", required: true, title: "IR Code to Send - 6", defaultValue: ""
                    input "msgDigit7", "text", required: true, title: "IR Code to Send - 7", defaultValue: ""
                    input "msgDigit8", "text", required: true, title: "IR Code to Send - 8", defaultValue: ""
                    input "msgDigit9", "text", required: true, title: "IR Code to Send - 9", defaultValue: ""
                    input "msgDigit0", "text", required: true, title: "IR Code to Send - 0", defaultValue: ""
                    input "msgDigitE", "text", required: false, title: "IR Code to Send - Enter", defaultValue: ""
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Send IP2IR - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}          
