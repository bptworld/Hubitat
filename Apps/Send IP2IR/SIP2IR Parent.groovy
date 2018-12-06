/**
 *  ****************  Send IP2IR Parent App  ****************
 *
 *  Design Usage:
 *  This app is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to Andrew Parker (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to Carson Dallum's (@cdallum) for the original IP2IR driver code that I based mine off of.
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

definition(
    name:"Send IP2IR",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Parent App for 'Send IP2IR' childapps ",
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
					paragraph "This app is designed to send commands to an iTach IP2IR device."
            		paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps"
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
  				section("Child Apps", hideable: true, hidden: true){
					app(name: "anyOpenApp", appName: "Send IP2IR Child", namespace: "BPTWorld", title: "<b>Add a new 'Send IP2IR'</b>", multiple: true)
  			    }
   				 section(" "){}
 			 	section("App Name"){
       				label title: "Enter a name for parent app (optional)", required: false
 				}  
            	section("<b>Be sure to enter in the Preset Values in Advanced Config before creating Child Apps</b>") {}
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
	section{paragraph "<b>Send IP2IR</b><br>App Version: 1.1.5<br>@BPTWorld"}        
}         
