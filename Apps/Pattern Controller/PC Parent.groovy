/**
 *  **************** Pattern Controller Parent ****************
 *
 *  Design Usage:
 *  Create any pattern you want using the zones associated with the Lifx Light Strip.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  1.0.0 - 02/14/22 - Initial release.
 *
 */

def setVersion(){
    state.name = "Pattern Controller"
	state.version = "1.0.0"
}

definition(
    name:"Pattern Controller",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create any pattern you want using the zones associated with the Lifx Light Strip.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: ""
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
    pcMapOfChildren()
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section("Information:", hideable: true, hidden: true) {
				paragraph "<b>Information</b>"
				paragraph "Create any pattern you want using the zones associated with the Lifx Light Strip."
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "Pattern Controller Child", namespace: "BPTWorld", title: "<b>Add a new 'Pattern Controller' child</b>", multiple: true)
			}
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Lifx Strip Device")) {
                input "zone1", "capability.switchLevel", title: "Select Zone 1 (z1)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone2", "capability.switchLevel", title: "Select Zone 2 (z2)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone3", "capability.switchLevel", title: "Select Zone 3 (z3)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone4", "capability.switchLevel", title: "Select Zone 4 (z4)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone5", "capability.switchLevel", title: "Select Zone 5 (z5)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone6", "capability.switchLevel", title: "Select Zone 6 (z6)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone7", "capability.switchLevel", title: "Select Zone 7 (z7)", required:false, multiple:false, submitOnChange:true, width:6
                input "zone8", "capability.switchLevel", title: "Select Zone 8 (z8)", required:false, multiple:false, submitOnChange:true, width:6
                input "needMore", "bool", title: "Need More?", defaultValue:false, submitOnChange:true
                if(needMore) {
                    input "zone9", "capability.switchLevel", title: "Select Zone 9 (z9)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone10", "capability.switchLevel", title: "Select Zone 10 (z10)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone11", "capability.switchLevel", title: "Select Zone 11 (z11)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone12", "capability.switchLevel", title: "Select Zone 12 (z12)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone13", "capability.switchLevel", title: "Select Zone 13 (z13)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone14", "capability.switchLevel", title: "Select Zone 14 (z14)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone15", "capability.switchLevel", title: "Select Zone 15 (z15)", required:false, multiple:false, submitOnChange:true, width:6
                    input "zone16", "capability.switchLevel", title: "Select Zone 16 (z16)", required:false, multiple:false, submitOnChange:true, width:6
                }
            }
            section() {
                theZones = []
                if(zone1) {
                    theZones << "zone1"
                    input "testZone1", "button", title: "Test Zone 1", width: 3
                }
                if(zone2) {
                    theZones << "zone2"
                    input "testZone2", "button", title: "Test Zone 2", width: 3
                }
                if(zone3) {
                    theZones << "zone3"
                    input "testZone3", "button", title: "Test Zone 3", width: 3
                }
                if(zone4) {
                    theZones << "zone4"
                    input "testZone4", "button", title: "Test Zone 4", width: 3
                }
                if(zone5) {
                    theZones << "zone5"
                    input "testZone5", "button", title: "Test Zone 5", width: 3
                }
                if(zone6) {
                    theZones << "zone6"
                    input "testZone6", "button", title: "Test Zone 6", width: 3
                }
                if(zone7) {
                    theZones << "zone7"
                    input "testZone7", "button", title: "Test Zone 7", width: 3
                }
                if(zone8) {
                    theZones << "zone8"
                    input "testZone8", "button", title: "Test Zone 8", width: 3
                }
                if(needMore) {
                    if(zone9) {
                        theZones << "zone9"
                        input "testZone9", "button", title: "Test Zone 9", width: 3
                    }
                    if(zone10) {
                        theZones << "zone10"
                        input "testZone10", "button", title: "Test Zone 10", width: 3
                    }
                    if(zone11) {
                        theZones << "zone11"
                        input "testZone11", "button", title: "Test Zone 11", width: 3
                    }
                    if(zone12) {
                        theZones << "zone12"
                        input "testZone12", "button", title: "Test Zone 12", width: 3
                    }
                    if(zone13) {
                        theZones << "zone13"
                        input "testZone13", "button", title: "Test Zone 13", width: 3
                    }
                    if(zone14) {
                        theZones << "zone14"
                        input "testZone14", "button", title: "Test Zone 14", width: 3
                    }
                    if(zone15) {
                        theZones << "zone15"
                        input "testZone15", "button", title: "Test Zone 15", width: 3
                    }
                    if(zone16) {
                        theZones << "zone16"
                        input "testZone16", "button", title: "Test Zone 16", width: 3
                    }
                }
                input "activeZones", "text", title: "Active Zones (Do NOT Edit)", defaultValue: theZones, required:false, submitOnChange:true
            }
            
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
                input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
 			}
			display2()
		}
	}
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "testZone1") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone1.on()
        pauseExecution(1000)
        zone1.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone2") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone2.on()
        pauseExecution(1000)
        zone2.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone3") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone3.on()
        pauseExecution(1000)
        zone3.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone4") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone4.on()
        pauseExecution(1000)
        zone4.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone5") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone5.on()
        pauseExecution(1000)
        zone5.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone6") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone6.on()
        pauseExecution(1000)
        zone6.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone7") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone7.on()
        pauseExecution(1000)
        zone7.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "testZone8") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        zone8.on()
        pauseExecution(1000)
        zone8.off()
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    }
}

def pcMapOfChildren(data=null) {
    if(logEnable) log.debug "In pcMapOfChildren - data: ${data}"
    pcMap = [:]
    childApps.each { cog ->
        pcMap.put("${cog.id}","${cog.label}")
    }
    if(pcMap) {
        if(logEnable) log.debug "In pcMapOfChildren - Sending $pcMap"
        sendLocationEvent(name: "pcChildren", value: pcMap.toString())
    }
}

def installCheck(){
    display()
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
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
		paragraph getFormat("line")
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
	}       
}

def getHeaderAndFooter() {
    def params = [
	    uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
		requestContentType: "application/json",
		contentType: "application/json",
		timeout: 30
	]
    
    try {
        def result = null
        httpGet(params) { resp ->
            state.headerMessage = resp.data.headerMessage
            state.footerMessage = resp.data.footerMessage
        }
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
