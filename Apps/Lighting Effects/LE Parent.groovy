/**
 *  ****************  Lighting Effects Parent App  ****************
 *
 *  Design Usage:
 *   - Designed to make static holiday lights dim or flicker randomly.
 *   - Randomly change colors on color change bulbs, completely separate or all together.
 *   - Slowly dim a set of dimmable devices, great for night time routines.
 *   - Slowly raise a set of dimmable devices, great for morning routines.
 *   - Setup a loop to continually raise and lower dimmable devices. 
 *   - Create a spooky, sparkly or party effect.
 *
 *  Copyright 2018-2020 @BPTWorld - Bryan Turcotte
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 * V2.0.1 - 04/12/20 - Removed color options from parent app
 */

def setVersion(){
	state.version = "v2.0.1"
}

definition(
    name:"Lighting Effects",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Parent App for 'Lighting Effects' childapps ",
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
            section("Instructions:", hideable: true, hidden: true) {
                paragraph "<b>Lighting Effects is designed to work with Hue devices. While other brands may work, nothing is guaranteed.</b> If not using Hue devices, you can try turning on 'Enable Hue in degrees (0-360)' for each device used with this app."
                paragraph "<b>Fast Dimming:</b>"
                paragraph "For each Child App, multiple devices can be selected. Each device will run sequential, Device 1, then Device 2, Back to device 1, then device 2..etc."
                paragraph "To create a random effect, put each device in a separate Child App, using the same switch to turn them on."
                paragraph "<b>Fast Color Changing:</b>"
                paragraph "Designed for color changing bulbs. This section can control lights individually, or all together within the same child app. Used to change colors between 5 sec and 5 minutes."
                paragraph "<b>Slow Color Changing:</b>"
                paragraph "Designed for color changing bulbs. This section can control lights individually, or all together within the same child app. Used to change colors between 5 minutes and 3 hours."
                paragraph "<b>Slow Off, On and Loop:</b>"
                paragraph "Designed to slowly raise or lower any dimmable device. Great for morning or night routines. Also has the ability to setup a loop to continually raise and lower a dimmable device. Note: The dimming is not smooth but rather done in steps."
                paragraph "<b>Important:</b>"
                paragraph "Be sure to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"
                paragraph "<b>Very Important:</b>"
                paragraph "Remember that the more devices you add and the faster you send commands, the more you're flooding the network. If you see 'normal' devices not responded as quickly or not at all, be sure to scale back the lighting effects."
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
                app(name: "anyOpenApp", appName: "Lighting Effects Child", namespace: "BPTWorld", title: "<b>Add a new 'Lighting Effects' child</b>", multiple: true)
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
                label title: "Enter a name for parent app (optional)", required: false
            }
        }
		display2()
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

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "cogWithWrench") return "${loc}cogWithWrench.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    section (getFormat("title", "${getImage("logo")}" + " Lighting Effects")) {
        paragraph "<div style='color:#1A77C9'>Create a spooky, sparkly or party effect.</div>"
		paragraph getFormat("line")
	}
}

def display2(){
    setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Lighting Effects - @BPTWorld<br>${state.version}</div>"
	}       
} 
