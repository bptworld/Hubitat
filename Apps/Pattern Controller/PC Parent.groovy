/**
 *  **************** Patterns Plus Parent ****************
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
 *  1.1.0 - 02/21/22 - Major Update
 *  1.0.0 - 02/14/22 - Initial release.
 *
 */

def setVersion(){
    state.name = "Patterns Plus"
	state.version = "1.1.0"
}

definition(
    name:"Patterns Plus",
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
                app(name: "anyOpenApp", appName: "Patterns Plus Child", namespace: "BPTWorld", title: "<b>Add a new 'Patterns Plus' child</b>", multiple: true)
				app(name: "anyOpenApp", appName: "Pattern Controller OLD Child", namespace: "BPTWorld", title: "<b>'Pattern Controller OLD' child</b><br>Once pattern is Exported, the child device should be deleted. This section will be going away on March 1, 2022.", multiple: true)
                app(name: "anyOpenApp", appName: "Patterns Plus File Convertor Child", namespace: "BPTWorld", title: "<b>'Pattern Controller to 'Patterns Plus' File Convertor' child</b>", multiple: true)
			}
                             
            section("${getImage('instructions')} <b>Device Master List:</b>", hideable: true, hidden: true) {
                input "masterswitchLevel", "capability.switchLevel", title: "Master List of Devices Needed in this App <small><abbr title='Only devices selected here can be used below. This can be edited at anytime.'><b>- INFO -</b></abbr></small>", required:false, multiple:true, submitOnChange:true
                masterList = masterswitchLevel.toString().replace("[","").replace("]","").split(", ")
                masterList = masterList.sort { a, b -> a <=> b }
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Device Per Zone")) {
                if(state.working == null) { state.working = false }
                input "zNumber", "text", title: "Zone Number (1 to 100)", range: '1..100', submitOnChange:true
                input "zone", "enum", title: "Select Device", options: masterList, required:false, multiple:false, submitOnChange:true
                if(state.cNum == null) state.cNum = zNumber
                if(zNumber.toString() != state.cNum.toString()) {
                    state.cNum = zNumber
                    state.working = false
                    if(logEnable) log.debug "In devicePerZone - zNumber doesn't match - removing zone - state.working: $state.working"
                    app.updateSetting("zone",[type:"enum", value:""])
                }
                if(zNumber) {
                    if(!state.working) {
                        state.working = true
                        if(state.zDevices == null) state.zDevices = [:]
                        wData = state.zDevices.get(zNumber)
                        if(wData) {
                            if(logEnable) log.debug "In devicePerZone - wData - Getting Zone info - $wData"
                            app.updateSetting("zone",[type:"enum", value:"${wData}"])
                        }
                    }
                }            
                paragraph "<small>Remember to click outside of any field before clicking on a button.</small>"
                input "enterDevice", "button", title: "Add/Edit Pattern", textColor: "white", backgroundColor: "green", width:3
                input "delDevice", "bool", title: "Delete Zone/Device", defaultValue:false, submitOnChange:true, width:3
                if(delDevice) {
                    state.zDevices.remove(zNumber)
                    app.updateSetting("zNumber",[type:"text", value:""])
                    app.updateSetting("zone",[type:"enum", value:""])
                    app?.updateSetting("delDevice",[value:"false",type:"bool"])
                }
                input "delAllDevices", "bool", title: "Delete ALL Zone/Devices", defaultValue:false, submitOnChange:true, width:3
                if(delAllDevices) {
                    state.zDevices = [:]
                    app.updateSetting("zNumber",[type:"text", value:""])
                    app.updateSetting("zone",[type:"enum", value:""])
                    app?.updateSetting("delAllDevices",[value:"false",type:"bool"])
                }
                input "resetZones", "bool", title: "Reset ALL Device to Off", submitOnChange:true, width: 3
                if(resetZones) {
                    masterswitchLevel.each { msl ->
                        msl.off()
                        pauseExecution(500)
                    }
                    app?.updateSetting("resetZones",[value:"false",type:"bool"])
                }
                input "testDevice", "bool", title: "Test Device", submitOnChange:true, width: 3
                if(testDevice) {
                    cleanDeviceHandler(zNumber)
                    if(state.match) {
                        theDevice = state.td
                        theDevice.on()
                        pauseExecution(1000)
                        theDevice.off()
                    }
                    app?.updateSetting("testDevice",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
                makeDisplayTable()
                paragraph "$state.theTable"
                paragraph "<hr>"
            }

			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
                input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
 			}
			display2()
		}
	}
}

def cleanDeviceHandler(data) {
    if(logEnable) log.debug "In cleanDeviceHandler (${state.version})"
    state.match = false
    state.td = null
    theDevice = state.zDevices.get(data)
    tDevice = theDevice.toString().replace(" ","")
    masterswitchLevel.each { it ->
        tIt = it.toString().replace(" ","")
        if(logEnable) log.debug "In cleanDeviceHandler - Checking $tIt - vs - $tDevice"
        if(tIt == tDevice) {
            if(logEnable) log.debug "In cleanDeviceHandler - Match!"
            state.td = it
            state.match = true
        }
    }
    if(state.match) {
        if(logEnable) log.debug "In cleanDeviceHandler - Returning: $state.td"
    } else {
        if(logEnable) log.debug "In cleanDeviceHandler - NO Match!"
    }
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "enterDevice") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        if(state.zDevices == null) state.zDevices = [:]
        newZone = zone.replace(" ","")
        state.zDevices.put(zNumber,zone)       
    }
}

def makeDisplayTable() {
    if(logEnable) log.debug "In makeDisplayTable (${state.version})"
    if(state.zDevices) {
        state.theTable = "<table width=100%><tr><td><b>Zone</b><td><b>Device</b>"
        state.zDevices.each { it ->
            if(logEnable) log.debug "In makeDisplayTable - Working on: $it.key - $it.value"
            theKey = it.key
            theValue = it.value
            state.theTable += "<tr><td>$theKey<td>$theValue"
        }
        state.theTable += "</table>"
    } else {
        state.theTable = "<table width=100%><tr><td><b>Zone</b><td><b>Device</b>"
        state.theTable += "</table>"
    }
    app.updateSetting("masterZones",[type:"enum", value:"${state.zDevices}"])
    if(logEnable) log.debug "In makeDisplayTable - Finished making the table"
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
