/**
 *  ****************  The Flasher Parent App  ****************
 *
 *  Design Usage:
 *  Flash your lights based on several triggers!
 *
 *  Copyright 2020-2024 Bryan Turcotte (@bptworld)
 *
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  All changes reflected in Child App
 *
 */



def setVersion(){
	state.version = null
}

definition(
    name:"The Flasher",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Flash your lights based on several triggers!",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
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
        		paragraph "Flash your lights based on several triggers!"
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
                app(name: "anyOpenApp", appName: "The Flasher Child", namespace: "BPTWorld", title: "<b>Add a new 'The Flasher' child</b>", multiple: true)
			}
            
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
		}
		display2()
	}
}

def installCheck(){   
    display()
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app"}
  	} else {
    	log.info "Parent Installed OK"
  	}
}

// ~~~~~ start include (202) BPTWorld.bpt-mandatoryStuff ~~~~~
library ( // library marker BPTWorld.bpt-mandatoryStuff, line 1
        base: "app", // library marker BPTWorld.bpt-mandatoryStuff, line 2
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-mandatoryStuff, line 3
        category: "Apps", // library marker BPTWorld.bpt-mandatoryStuff, line 4
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-mandatoryStuff, line 5
        name: "bpt-mandatoryStuff", // library marker BPTWorld.bpt-mandatoryStuff, line 6
        namespace: "BPTWorld", // library marker BPTWorld.bpt-mandatoryStuff, line 7
        documentationLink: "", // library marker BPTWorld.bpt-mandatoryStuff, line 8
        version: "1.0.0", // library marker BPTWorld.bpt-mandatoryStuff, line 9
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-mandatoryStuff, line 10
) // library marker BPTWorld.bpt-mandatoryStuff, line 11

import groovy.json.* // library marker BPTWorld.bpt-mandatoryStuff, line 13
import java.util.TimeZone // library marker BPTWorld.bpt-mandatoryStuff, line 14
import groovy.transform.Field // library marker BPTWorld.bpt-mandatoryStuff, line 15
import groovy.time.TimeCategory // library marker BPTWorld.bpt-mandatoryStuff, line 16
import java.text.SimpleDateFormat // library marker BPTWorld.bpt-mandatoryStuff, line 17

def setLibraryVersion() { // library marker BPTWorld.bpt-mandatoryStuff, line 19
    state.libraryVersion = "1.0.0" // library marker BPTWorld.bpt-mandatoryStuff, line 20
} // library marker BPTWorld.bpt-mandatoryStuff, line 21

def checkHubVersion() { // library marker BPTWorld.bpt-mandatoryStuff, line 23
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-mandatoryStuff, line 24
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-mandatoryStuff, line 25
    if(logEnable) log.debug "In checkHubVersion - Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-mandatoryStuff, line 26
} // library marker BPTWorld.bpt-mandatoryStuff, line 27

def parentCheck(){   // library marker BPTWorld.bpt-mandatoryStuff, line 29
	state.appInstalled = app.getInstallationState()  // library marker BPTWorld.bpt-mandatoryStuff, line 30
	if(state.appInstalled != 'COMPLETE'){ // library marker BPTWorld.bpt-mandatoryStuff, line 31
		parentChild = true // library marker BPTWorld.bpt-mandatoryStuff, line 32
  	} else { // library marker BPTWorld.bpt-mandatoryStuff, line 33
    	parentChild = false // library marker BPTWorld.bpt-mandatoryStuff, line 34
  	} // library marker BPTWorld.bpt-mandatoryStuff, line 35
} // library marker BPTWorld.bpt-mandatoryStuff, line 36

def appControlSection() { // library marker BPTWorld.bpt-mandatoryStuff, line 38
    input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-mandatoryStuff, line 39
    if(pauseApp) { // library marker BPTWorld.bpt-mandatoryStuff, line 40
        if(app.label) { // library marker BPTWorld.bpt-mandatoryStuff, line 41
            if(!app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-mandatoryStuff, line 42
                app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>") // library marker BPTWorld.bpt-mandatoryStuff, line 43
            } // library marker BPTWorld.bpt-mandatoryStuff, line 44
        } // library marker BPTWorld.bpt-mandatoryStuff, line 45
    } else { // library marker BPTWorld.bpt-mandatoryStuff, line 46
        if(app.label) { // library marker BPTWorld.bpt-mandatoryStuff, line 47
            if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-mandatoryStuff, line 48
                app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>") // library marker BPTWorld.bpt-mandatoryStuff, line 49
            } // library marker BPTWorld.bpt-mandatoryStuff, line 50
        } // library marker BPTWorld.bpt-mandatoryStuff, line 51
    } // library marker BPTWorld.bpt-mandatoryStuff, line 52
    if(pauseApp) {  // library marker BPTWorld.bpt-mandatoryStuff, line 53
        paragraph app.label // library marker BPTWorld.bpt-mandatoryStuff, line 54
    } else { // library marker BPTWorld.bpt-mandatoryStuff, line 55
        label title: "Enter a name for this automation", required:true, submitOnChange:true // library marker BPTWorld.bpt-mandatoryStuff, line 56
    } // library marker BPTWorld.bpt-mandatoryStuff, line 57
} // library marker BPTWorld.bpt-mandatoryStuff, line 58

def appGeneralSection() { // library marker BPTWorld.bpt-mandatoryStuff, line 60
    input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-mandatoryStuff, line 61
    if(logEnable) { // library marker BPTWorld.bpt-mandatoryStuff, line 62
        input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"] // library marker BPTWorld.bpt-mandatoryStuff, line 63
    } // library marker BPTWorld.bpt-mandatoryStuff, line 64
    paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time." // library marker BPTWorld.bpt-mandatoryStuff, line 65
    input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app <small>(When selected switch is ON, app is disabled.)</small>", submitOnChange:true, required:false, multiple:true // library marker BPTWorld.bpt-mandatoryStuff, line 66
} // library marker BPTWorld.bpt-mandatoryStuff, line 67

def uninstalled() { // library marker BPTWorld.bpt-mandatoryStuff, line 69
    sendLocationEvent(name: "updateVersionInfo", value: "${app.id}:remove") // library marker BPTWorld.bpt-mandatoryStuff, line 70
} // library marker BPTWorld.bpt-mandatoryStuff, line 71

def logsOff() { // library marker BPTWorld.bpt-mandatoryStuff, line 73
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-mandatoryStuff, line 74
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-mandatoryStuff, line 75
} // library marker BPTWorld.bpt-mandatoryStuff, line 76

def checkEnableHandler() { // library marker BPTWorld.bpt-mandatoryStuff, line 78
    setVersion() // library marker BPTWorld.bpt-mandatoryStuff, line 79
    state.eSwitch = false // library marker BPTWorld.bpt-mandatoryStuff, line 80
    if(disableSwitch) {  // library marker BPTWorld.bpt-mandatoryStuff, line 81
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-mandatoryStuff, line 82
        disableSwitch.each { it -> // library marker BPTWorld.bpt-mandatoryStuff, line 83
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-mandatoryStuff, line 84
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-mandatoryStuff, line 85
        } // library marker BPTWorld.bpt-mandatoryStuff, line 86
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-mandatoryStuff, line 87
    } // library marker BPTWorld.bpt-mandatoryStuff, line 88
} // library marker BPTWorld.bpt-mandatoryStuff, line 89

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-mandatoryStuff, line 91
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-mandatoryStuff, line 92
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-mandatoryStuff, line 93
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 94
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 95
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 96
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 97
    if(type == "logo") return "${loc}logo.png height=40>" // library marker BPTWorld.bpt-mandatoryStuff, line 98
    if(type == "qmark") return "${loc}question-mark-icon.png height=16>" // library marker BPTWorld.bpt-mandatoryStuff, line 99
    if(type == "qmark2") return "${loc}question-mark-icon-2.jpg height=16>" // library marker BPTWorld.bpt-mandatoryStuff, line 100
} // library marker BPTWorld.bpt-mandatoryStuff, line 101

def getFormat(type, myText=null, page=null) {			// Modified code from @Stephack // library marker BPTWorld.bpt-mandatoryStuff, line 103
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid #000000;box-shadow: 2px 3px #80BC00;border-radius: 10px'>${myText}</div>" // library marker BPTWorld.bpt-mandatoryStuff, line 104
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />" // library marker BPTWorld.bpt-mandatoryStuff, line 105
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-mandatoryStuff, line 106

    if(type == "button-blue") return "<a style='color:white;text-align:center;font-size:20px;font-weight:bold;background-color:#03FDE5;border:1px solid #000000;box-shadow:3px 4px #8B8F8F;border-radius:10px' href='${page}'>${myText}</a>" // library marker BPTWorld.bpt-mandatoryStuff, line 108
} // library marker BPTWorld.bpt-mandatoryStuff, line 109

def display(data) { // library marker BPTWorld.bpt-mandatoryStuff, line 111
    if(data == null) data = "" // library marker BPTWorld.bpt-mandatoryStuff, line 112
    if(app.label) { // library marker BPTWorld.bpt-mandatoryStuff, line 113
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-mandatoryStuff, line 114
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-mandatoryStuff, line 115
        } else { // library marker BPTWorld.bpt-mandatoryStuff, line 116
            theName = app.label // library marker BPTWorld.bpt-mandatoryStuff, line 117
        } // library marker BPTWorld.bpt-mandatoryStuff, line 118
    } // library marker BPTWorld.bpt-mandatoryStuff, line 119
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-mandatoryStuff, line 120
    if(!state.name) { state.name = "" } // library marker BPTWorld.bpt-mandatoryStuff, line 121
    if(state.name == theName) { // library marker BPTWorld.bpt-mandatoryStuff, line 122
        headerName = state.name // library marker BPTWorld.bpt-mandatoryStuff, line 123
    } else { // library marker BPTWorld.bpt-mandatoryStuff, line 124
        if(state.name == null || state.name == "") { // library marker BPTWorld.bpt-mandatoryStuff, line 125
            headerName = "${theName}" // library marker BPTWorld.bpt-mandatoryStuff, line 126
        } else { // library marker BPTWorld.bpt-mandatoryStuff, line 127
            headerName = "${state.name} - ${theName}" // library marker BPTWorld.bpt-mandatoryStuff, line 128
        } // library marker BPTWorld.bpt-mandatoryStuff, line 129
    } // library marker BPTWorld.bpt-mandatoryStuff, line 130
    section() { // library marker BPTWorld.bpt-mandatoryStuff, line 131
        paragraph "<h2 style='color:#1A77C9;font-weight: bold'>${headerName}</h2><div style='color:#1A77C9'>A BPTWorld App</div>" // library marker BPTWorld.bpt-mandatoryStuff, line 132

        //<a href='https://community.hubitat.com/t/release-bundle-manager-the-only-place-to-find-bptworld-bundles-find-install-and-update-bundles-quickly-and-easily/94567/295' target='_blank'>Bundle Manager</a>! // library marker BPTWorld.bpt-mandatoryStuff, line 134

        paragraph getFormat("line") // library marker BPTWorld.bpt-mandatoryStuff, line 136
    } // library marker BPTWorld.bpt-mandatoryStuff, line 137
} // library marker BPTWorld.bpt-mandatoryStuff, line 138

def display2() { // library marker BPTWorld.bpt-mandatoryStuff, line 140
    setVersion() // library marker BPTWorld.bpt-mandatoryStuff, line 141
    section() { // library marker BPTWorld.bpt-mandatoryStuff, line 142
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" } // library marker BPTWorld.bpt-mandatoryStuff, line 143
        paragraph getFormat("line") // library marker BPTWorld.bpt-mandatoryStuff, line 144
        if(state.version) { // library marker BPTWorld.bpt-mandatoryStuff, line 145
            bMes = "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}" // library marker BPTWorld.bpt-mandatoryStuff, line 146
        } else { // library marker BPTWorld.bpt-mandatoryStuff, line 147
            bMes = "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name}" // library marker BPTWorld.bpt-mandatoryStuff, line 148
        } // library marker BPTWorld.bpt-mandatoryStuff, line 149
        bMes += "</div>" // library marker BPTWorld.bpt-mandatoryStuff, line 150
        paragraph "${bMes}" // library marker BPTWorld.bpt-mandatoryStuff, line 151
        paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>" // library marker BPTWorld.bpt-mandatoryStuff, line 152
    } // library marker BPTWorld.bpt-mandatoryStuff, line 153
} // library marker BPTWorld.bpt-mandatoryStuff, line 154

// ~~~~~ end include (202) BPTWorld.bpt-mandatoryStuff ~~~~~
