/**
 *  ****************  Home Tracker 2 Parent App  ****************
 *
 *  Design Usage:
 *  Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!
 *
 *  Copyright 2018-2021 Bryan Turcotte (@bptworld)
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
 *  2.2.5 - 01/01/21 - Adjustments
 *  2.2.4 - 04/27/20 - Cosmetic changes
 *  2.2.3 - 01/11/20 - Fixed sensors from jumping around unless adding/removing
 *  2.2.2 - 01/11/20 - Fix for locks
 *  2.2.1 - 12/28/19 - Bug fixes
 *  2.2.0 - 12/17/19 - Initial release.
 *
 */

def setVersion(){
    state.name = "Home Tracker 2"
	state.version = "2.2.5"
}

definition(
    name:"Home Tracker 2",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Home%20Tracker/HT-parent.groovy",
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
			section("Instructions:", hideable: true, hidden: true) {
        		paragraph "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message <i>after</i> entering the home!"
        	paragraph "<b>Type of 'Welcome Home' Triggers:</b>"
    			paragraph "<b>Unlock or Door Open</b><br>Both of these work pretty much the same. When door or lock is triggered, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement."
				paragraph "Each trigger can have multiple selections but this is an 'or' function. Meaning it only takes one device to trigger the actions. ie. Door1 or Door2 has been opened. If you require a different delay per door/lock, then separate child apps would be required - one for each door or lock."
				paragraph "<b>Motion Sensor</b><br>When motion sensor becomes active, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement. If you require a different delay per motion sensor, then separate child apps would be required - one for each motion sensor."
				paragraph "This trigger also works with Hubitat's built in 'Zone Motion Controllers' app. Which allows you to do some pretty cool things with motion sensors."
				paragraph "<b>Requirements:</b>"
				paragraph "Be sure to complete the 'Advanced Config' section before creating Child Apps."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				paragraph "<b>Be sure to complete the 'Advanced Config' section before creating Child Apps.</b>"
                app(name: "anyOpenApp", appName: "Home Tracker 2 Child", namespace: "BPTWorld", title: "<b>Add a new 'Home Tracker 2' child</b>", multiple: true)
			}
            
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Config")) {}
            section("Global Variables", hideable: true) {
			    paragraph "This app <b>requires</b> a 'virtual device' to send variables between child apps. This is to prevent multiple announcements.<br>ie. Person A comes home and enters door 1, walks through the house and opens door 2 to let the dogs out.  We only want one 'Welcome Home' message to be played."
			    paragraph "* Vitual Device must use our custom 'Home Tracker Driver'"
                input "useExistingDevice", "bool", title: "Use existing device (off) or have HT create a new one for you (on)", defaultValue:false, submitOnChange:true
                if(useExistingDevice) {
                    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'HT - Tracker')", required:true, submitOnChange:true
                    paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                    if(dataName) createDataChildDevice()
                    if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                    paragraph "${statusMessageD}"
                }
                input "gvDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false, submitOnChange:true
                if(!useExistingDevice) {
                    app.removeSetting("dataName")
                    paragraph "<small>* Device must use the 'Home Tracker Driver'.</small>"
                }
		    }
            
            if(gvDevice) {
                section("Presence Sensors:", hideable: true) {
                    paragraph "<b>When adding or removing sensors - you may have to retype in your Friendly Names, as they will be out of order.</b>"
                    input "presenceSensors", "capability.presenceSensor", title: "Select Presence Sensors to track with Home Tracker 2 (max 20)", required:true, multiple:true, submitOnChange:true
                    if(presenceSensors) {     
                        try {     
                            presenceSensors = presenceSensors.sort { a, b -> a.value <=> b.value }
                            log.debug "presenceSensors: ${presenceSensors}"
                            pSensorsSize = presenceSensors.size()
                            if(logDebug) log.debug "In presenceOptions - pSensorsSize: ${pSensorsSize} - presenceSensors: ${presenceSensors}"
                            for(x=0;x < pSensorsSize.toInteger();x++) {
                                if(x < 21) {
                                    input "fName$x", "text", title: "(${x}) Friendly name for ${presenceSensors[x]}", defaultValue: "${presenceSensors[x]}", required:true, multiple:false, width:6, submitOnChange:true
                                    input "pronounce$x", "text", title: "Alt Pronunciation for ${presenceSensors[x]}", required:false, multiple:false, width:6, submitOnChange:true

                                    fNam = app."fName$x"
                                    pro = app."pronounce$x"
                                    globalName = "${x};${fNam};${pro}"
                                    gvDevice.sendDataMapName(globalName)
                                    log.debug "In Advanced Config - locks - Sending Global Data: ${globalName}" 
                                } else {
                                    paragraph "<b>Max number of Presence Sensors has been reached.</b>"
                                }
                            }
                        } catch (e) {
                            log.error (e)
                        }
                    }
                }
                section("Door Locks:", hideable: true) {
                    paragraph "<b>When adding or removing locks - you may have to retype in your Friendly Names, as they will be out of order.</b>"
                    input "locks", "capability.lock", title: "Select Locks to track with Home Tracker 2 (max 4)", required:false, multiple:true, submitOnChange:true
                    if(locks) {
                        try {     
                            locks = locks.sort { a, b -> a.value <=> b.value }
                            locksSize = locks.size()
                            if(logDebug) log.debug "In presenceOptions - locksSize: ${locksSize} - locks: ${locks}"
                            for(x=0;x < locksSize.toInteger();x++) {
                                if(x < 5) {
                                    input "lFName$x", "text", title: "(${x}) Friendly name for ${locks[x]}", defaultValue: "${locks[x]}", required:true, multiple:false, width: 6, submitOnChange:true
                                    input "lPronounce$x", "text", title: "Alt Pronunciation for ${locks[x]}", defaultValue: "", required:false, multiple:false, width: 6, submitOnChange:true

                                    lFNam = app."lFName$x"
                                    lPro = app."lPronounce$x"
                                    globalName = "${x};${lFNam};${lPro}"
                                    gvDevice.sendDataMapLockName(globalName)
                                    log.debug "In Advanced Config - locks - Sending Global Data: ${globalName}" 
                                } else {
                                    paragraph "<b>Max number of Door Locks has been reached.</b>"
                                }
                            }
                        } catch (e) {
                            log.error (e)
                        }
                    }
                }
            }
		}
		display2()
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

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Home Tracker 2 Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Home Tracker unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
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
    if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
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
