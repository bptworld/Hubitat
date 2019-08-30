/**
 *  **************** App Watchdog 2 Driver Child ****************
 *
 *  Design Usage:
 *  See if any compatible driver needs an update, all in one place.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.0 - 08/28/19 - Initial Release.
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. YourAppsNameParentVersion, YourAppsNameChildVersion
    state.appName = "AppWatchdog2DriversChildVersion"
	state.version = "v2.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "App Watchdog 2 Drivers Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "See if any compatible driver needs an update, all in one place.",
    category: "",
	parent: "BPTWorld:App Watchdog 2",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Apps%20Watchdog%202/AW2-Drivers-child.groovy",
)

preferences {
    page name: "pageConfig"
	page name: "pageDriverstoUpdate", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "pageCurrent", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "developerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>App Watchdog 2 - Driver</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "See if any compatible driver needs an update, all in one place."
            paragraph "Note: This will only track drivers, not apps. Thanks"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
            paragraph "Be sure to setup at least one developer before running a report."
			href "pageDriverstoUpdate", title: "Driver Watchdog Report", description: "Click here to view the Driver Watchdog Report."
			href "pageCurrent", title: "Driver Installed Report", description: "Click here to view the Driver Installed Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
            href "developerOptions", title:"Select Developers to follow here", description:"Click here to setup Developer Options"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			input "timeToRun", "time", title: "Check Drivers at this time daily", required: true
			input "isDataDevice", "capability.switch", title: "Turn this device on if there is data to report", required: false, multiple: false
			input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
			if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
		section() {
			input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.", width: 6)
            input(name: "traceEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Trace Logging", description: "Enable extra logging for debugging.", width: 6)
		}
		display2()
	}
}

def developerOptions(){
    dynamicPage(name: "developerOptions", title: "Developer Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Developer 1 Options")) {
            input "gitHubURL1", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldDrivers.json":"BPTWorld"]
            ]
			if(gitHubURL1) {
                gitHubCheck1()
				paragraph "Developer: ${state.gitHubAuthor1}"
                state.values1 = "${state.gitHubDrivers1}".split(",")
    		    input "installedDrivers1", "enum", title: "Select which drivers you have installed", options: state.values1, required: true, multiple: true, submitOnChange: true
                
                if(installedDrivers1) {
                    dSelected = installedDrivers1.join(", ")
                    paragraph "Drivers Selected: ${dSelected}"
                
                    input name: "matchingDevices1", type: "capability.actuator", title: "Select one existing device for each driver selected, that uses that driver.", required: true, multiple: true, submitOnChange: true
                }
                
                if(state.paypalLink1) paragraph "<b>If you find ${state.gitHubAuthor1}'s apps useful. Please consider a donation via <a href='${state.paypalLink1}' target='_blank'>Paypal</a></b>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 2 Options")) {
            input "gitHubURL2", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldDrivers.json":"BPTWorld"]
            ]
			if(gitHubURL2) {
                gitHubCheck2()
				paragraph "Developer: ${state.gitHubAuthor2}"
                state.values2 = "${state.gitHubDrivers2}".split(",")
    		    input "installedDrivers2", "enum", title: "Select which drivers you have installed", options: state.values2, required: true, multiple: true, submitOnChange: true
                
                if(installedDrivers2) {
                    dSelected = installedDrivers2.join(", ")
                    paragraph "Drivers Selected: ${dSelected}"
                
                    input name: "matchingDevices2", type: "capability.actuator", title: "Select one existing device for each driver selected, that uses that driver.", required: true, multiple: true, submitOnChange: true
                }
                
                if(state.paypalLink2) paragraph "<b>If you find ${state.gitHubAuthor2}'s apps useful. Please consider a donation via <a href='${state.paypalLink2}' target='_blank'>Paypal</a></b>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 3 Options")) {
            input "gitHubURL3", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldDrivers.json":"BPTWorld"]
            ]
			if(gitHubURL3) {
                gitHubCheck3()
				paragraph "Developer: ${state.gitHubAuthor3}"
                state.values3 = "${state.gitHubDrivers1}".split(",")
    		    input "installedDrivers3", "enum", title: "Select which drivers you have installed", options: state.values3, required: true, multiple: true, submitOnChange: true
                
                if(installedDrivers3) {
                    dSelected = installedDrivers3.join(", ")
                    paragraph "Drivers Selected: ${dSelected}"
                
                    input name: "matchingDevices3", type: "capability.actuator", title: "Select one existing device for each driver selected, that uses that driver.", required: true, multiple: true, submitOnChange: true
                }
                
                if(state.paypalLink3) paragraph "<b>If you find ${state.gitHubAuthor3}'s apps useful. Please consider a donation via <a href='${state.paypalLink1}' target='_blank'>Paypal</a></b>"
            }
        }   
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 4 Options")) {
            input "gitHubURL4", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldDrivers.json":"BPTWorld"]
            ]
			if(gitHubURL4) {
                gitHubCheck4()
				paragraph "Developer: ${state.gitHubAuthor4}"
                state.values4 = "${state.gitHubDrivers4}".split(",")
    		    input "installedDrivers4", "enum", title: "Select which drivers you have installed", options: state.values1, required: true, multiple: true, submitOnChange: true
                
                if(installedDrivers4) {
                    dSelected = installedDrivers4.join(", ")
                    paragraph "Drivers Selected: ${dSelected}"
                
                    input name: "matchingDevices4", type: "capability.actuator", title: "Select one existing device for each driver selected, that uses that driver.", required: true, multiple: true, submitOnChange: true
                }
                
                if(state.paypalLink4) paragraph "<b>If you find ${state.gitHubAuthor4}'s apps useful. Please consider a donation via <a href='${state.paypalLink4}' target='_blank'>Paypal</a></b>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 5 Options")) {
            input "gitHubURL5", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldDrivers.json":"BPTWorld"]
            ]
			if(gitHubURL5) {
                gitHubCheck5()
				paragraph "Developer: ${state.gitHubAuthor5}"
                state.values5 = "${state.gitHubDrivers5}".split(",")
    		    input "installedDrivers5", "enum", title: "Select which drivers you have installed", options: state.values1, required: true, multiple: true, submitOnChange: true
                
                if(installedDrivers5) {
                    dSelected = installedDrivers5.join(", ")
                    paragraph "Drivers Selected: ${dSelected}"
                
                    input name: "matchingDevices5", type: "capability.actuator", title: "Select one existing device for each driver selected, that uses that driver.", required: true, multiple: true, submitOnChange: true
                }
                
                if(state.paypalLink5) paragraph "<b>If you find ${state.gitHubAuthor5}'s apps useful. Please consider a donation via <a href='${state.paypalLink5}' target='_blank'>Paypal</a></b>"
            }
        }

        state.allDrivers = [installedDrivers1,installedDrivers2,installedDrivers3,installedDrivers4,installedDrivers5].flatten().findAll{it} 
    }
}
            
def pageDriverstoUpdate(params) {
	dynamicPage(name: "pageDriverstoUpdate", title: "Drivers Watchdog - Drivers to Update", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		driverMapHandler()
			section("How to Update", hideable: true, hidden: true) {
				paragraph "<b>To update your code:</b><br>(<i>Do this for each item that needs updating.</i>)"
				paragraph " - Right-click on any link and choose 'Copy Link Location'<br> - Go into your 'Drivers Code' section<br> - Select the corresponding driver<br> - 'Import' it in, 'Save' and done!"
				paragraph "* Driver version data will update the next time the device is opened"
			}
		if(state.driverMap) {
			section() {
				updateMap = "<table width='100%'>${state.driverMap}</table>"
				paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Drivers with an update</h2>"
				paragraph "${updateMap}"
        	}
		} else {
			section() { 
				paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Drivers with an update</h2>"
				paragraph "All drivers are up to date"
			}
		}
	}
}

def pageCurrent(params) {
	dynamicPage(name: "pageCurrent", title: "Drivers Watchdog - Driver Versions", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		driverMapHandler()
		section() {
			updateAllMap = "<table width='100%'>${state.driverAllMap}</table>"
			paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Drivers - All</h2>"
			paragraph "${updateAllMap}"
        }
	}
}

def installed() {
    log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setVersion()
	setDefaults()
	schedule(timeToRun, driverMapHandler)
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def gitHubCheck1() {
	def params = [uri: "${gitHubURL1}", contentType: "application/json"]
	if(traceEnable) log.trace "In gitHubCheck1... About to 'try' - ${gitHubURL1}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor1 = "${results.GitHubAuthor}"
			state.gitHubMainURL1 = results.GitHubMainURL
            state.gitHubDrivers1 = results.Drivers
            state.paypalLink1 = results.PaypalLink
        }
	} 
    catch (e) {
        log.info "Warning:  GitHub URL 1 not found"
		state.gitHubAuthor1 = "GitHub: Not found"
    }
}

def gitHubCheck2() {
	def params = [uri: "${gitHubURL2}", contentType: "application/json"]
	if(logEnable) log.debug "In gitHubCheck2... About to 'try' - ${gitHubURL2}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor2 = "${results.GitHubAuthor}"
			state.gitHubMainURL2 = results.GitHubMainURL
            state.gitHubDrivers2 = results.Drivers
            state.paypalLink2 = results.PaypalLink
        }
	} 
    catch (e) {
        log.info "Warning:  GitHub URL 2 not found"
		state.gitHubAuthor2 = "GitHub: Not found"
    }
}

def gitHubCheck3() {
	def params = [uri: "${gitHubURL3}", contentType: "application/json"]
	if(logEnable) log.debug "In gitHubCheck3... About to 'try' - ${gitHubURL3}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor3 = "${results.GitHubAuthor}"
			state.gitHubMainURL3 = results.GitHubMainURL
            state.gitHubDrivers3 = results.Drivers
            state.paypalLink3 = results.PaypalLink
        }
	} 
    catch (e) {
        log.info "Warning:  GitHub URL 3 not found"
		state.gitHubAuthor3 = "GitHub: Not found"
    }
}

def gitHubCheck4() {
	def params = [uri: "${gitHubURL4}", contentType: "application/json"]
	if(logEnable) log.debug "In gitHubCheck4... About to 'try' - ${gitHubURL4}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor4 = "${results.GitHubAuthor}"
			state.gitHubMainURL4 = results.GitHubMainURL
            state.gitHubDrivers4 = results.Drivers
            state.paypalLink4 = results.PaypalLink
        }
	} 
    catch (e) {
        log.info "Warning:  GitHub URL 4 not found"
		state.gitHubAuthor4 = "GitHub: Not found"
    }
}

def gitHubCheck5() {
	def params = [uri: "${gitHubURL5}", contentType: "application/json"]
	if(logEnable) log.debug "In gitHubCheck5... About to 'try' - ${gitHubURL5}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor5 = "${results.GitHubAuthor}"
			state.gitHubMainURL5 = results.GitHubMainURL
            state.gitHubDrivers5 = results.Drivers
            state.paypalLink5 = results.PaypalLink
        }
	} 
    catch (e) {
        log.info "Warning:  GitHub URL 5 not found"
		state.gitHubAuthor5 = "GitHub: Not found"
    }
}

def buildDriverMap(matchingDevices) {
    if(traceEnable) log.trace "In buildDriverMap"
    
    def mDevices = matchingDevices
    if(traceEnable) log.debug "In buildDriverMap - mDevices: ${mDevices}"
    
    if(traceEnable) log.trace "In buildDriverMap - updating devices"
    mDevices.each { it ->
        it.updateVersion()
    }   
        
    state.theDriverMap = [:]
    mDevices.each { it ->
        if(traceEnable) log.debug "In buildDriverMap - working on ${it}"
        dwInfo = it.currentValue("dwDriverInfo")
        if(traceEnable) log.debug "In buildDriverMap - found ${dwInfo}"
        if(dwInfo) {
            def (newKey, newValue) = dwInfo.split(":")
            state.theDriverMap.put(newKey,newValue)
            if(traceEnable) log.debug "In buildDriverMap - theDataMap: ${state.theDriverMap}"
        }
        if(traceEnable) log.debug "In buildDriverMap - theDataMap: ${state.theDriverMap}"
    }
}

def driverMapHandler(evt) {
	if(logEnable) log.debug "In driverMapHandler..."
			clearMaps()
			if(state.allDrivers) {
                if(traceEnable) log.warn "allDrivers: ${state.allDrivers}"
				state.allDrivers.each { item ->
					state.driversName = item.replace(" ","")
					if(traceEnable) log.trace "----------- Starting Driver: ${item} -----------"
                    
                    if(installedDrivers1) {
                        buildDriverMap(matchingDevices1)
                        if(traceEnable) log.trace "Looking at InstalledDrivers 1"
                        def gitHub1 = installedDrivers1.toListString()
                        if(gitHub1.contains("${item}")) {
                            state.params = [uri: "${gitHubURL1}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor1
                            state.authorMainURL = state.gitHubMainURL1
                            if(traceEnable) log.trace "installedDrivers1 - driversName: ${item} - author: ${state.authorName} - ${state.params}"
                        } else {
                            log.trace "installedDrivers1 - No Match"
                        }
                    }
                    if(installedDrivers2) {
                        buildDriverMap(matchingDevices2)
                        if(traceEnable) log.trace "Looking at InstalledDrivers 2"
                        def gitHub2 = installedDrivers2.toListString()
                        if(gitHub2.contains("${item}")) {
                            state.params = [uri: "${gitHubURL2}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor2
                            state.authorMainURL = state.gitHubMainURL2
                            if(traceEnable) log.trace "installedDrivers2 - driversName: ${item} - author: ${state.authorName} - ${state.params}"
                        } else {
                            log.trace "installedDrivers2 - No Match"
                        }
                    }
                    if(installedDrivers3) {
                        buildDriverMap(matchingDevices3)
                        if(traceEnable) log.trace "Looking at InstalledDrivers 3"
                        def gitHub3 = installedDrivers3.toListString()
                        if(gitHub3.contains("${item}")) {
                            state.params = [uri: "${gitHubURL3}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor3
                            state.authorMainURL = state.gitHubMainURL3
                            if(traceEnable) log.trace "installedDrivers3 - driversName: ${item} - author: ${state.authorName} - ${state.params}"
                        } else {
                            log.trace "installedDrivers3 - No Match"
                        }
                    }
                    if(installedDrivers4) {
                        buildDriverMap(matchingDevices4)
                        if(traceEnable) log.trace "Looking at InstalledDrivers 4"
                        def gitHub4 = installedDrivers4.toListString()
                        if(gitHub4.contains("${item}")) {
                            state.params = [uri: "${gitHubURL4}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor4
                            state.authorMainURL = state.gitHubMainURL4
                            if(traceEnable) log.trace "installedDrivers4 - driversName: ${item} - author: ${state.authorName} - ${state.params}"
                        } else {
                            log.trace "installedDrivers4 - No Match"
                        }
                    }
                    if(installedDrivers5) {
                        buildDriverMap(matchingDevices5)
                        if(traceEnable) log.trace "Looking at InstalledDrivers 5"
                        def gitHub5 = installedDrivers5.toListString()
                        if(gitHub5.contains("${item}")) {
                            state.params = [uri: "${gitHubURL5}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor5
                            state.authorMainURL = state.gitHubMainURL5
                            if(traceEnable) log.trace "installedDrivers5 - driversName: ${item} - author: ${state.authorName} - ${state.params}"
                        } else {
                            log.trace "installedDrivers5 - No Match"
                        }
                    }
                    
       				try {
						httpGet(state.params) { response ->
							results = response.data
                            
							// Get Github Data from json
							if(traceEnable) log.trace "Getting Versions from json - driversName: ${state.driversName}"

							state.driverName = results."${state.driversName}Name"
                            if(!state.driverName) state.driverName = "-"
                                
						    state.driverVersion = results."${state.driversName}Version"
                            if(!state.driverVersion) state.driverVersion = "-"
                                    
                            state.driverRawCode = results."${state.driversName}RawCode"
                            if(!state.driverRawCode) state.driverRawCode = "-"
                            
						    state.driverDiscussion = results."${state.driversName}Discussion"
                            if(!state.driverDiscussion) state.driverDiscussion = "-"
                                
							state.driverUpdateNote = results."${state.driversName}UpdateNote"
                            if(!state.driverUpdateNote) state.driverUpdateNote = "-"
                            
                            if(traceEnable) log.trace "JSON results - ${item} - ${state.driverName} - version: ${state.driverVersion}"
                        }

						// Get Old Data from map

                        state.dName = item
                                
                        def watchMap = ""
                                
                        watchMap = state.theDriverMap
                        if(logEnable) log.debug "watchMap: ${watchMap}"
                                
                        def theMap = watchMap.collectEntries{ [(it.key):(it.value)] }
                       
                        theMapS = theMap.sort { a, b -> a.key <=> b.key }
                                
                        if(logEnable) log.debug "Sorted theMap: ${theMapS}"
                                
                        theMapS.each { it ->
                            driverName = it.key
                            driverVer = it.value
                            dName = state.dName.replace(" ", "")
                            if(traceEnable) log.trace "Working on watchMap - Does driverName: ${driverName} contain dName: ${dName}"
                            if(driverName.contains("${dName}")) state.oldDriverVersion = driverVer
                            
                            if(!state.oldDriverVersion) state.oldDriverVersion = "-"
                        }
                        if(traceEnable) log.trace "INSTALLED results - ${item} - ${state.oldDriverVersion}"

                        checkTheDriverData()
							   
                        state.oldDriverVersion = ""
                        state.params = ""
				    }
                    catch(e) {
                        log.error "${e}"
                        log.warn "Somehting Went Wrong"
                    }
			    if(traceEnable) log.trace "----------- End Driver: ${item} -----------"	
            }
		}
	if(maintSwitch2 != true) {
		if(sendPushMessage) pushNow()
		if(parent.awDevice) tileHandler()
	}
}

def checkTheDriverData() {  
    if(state.oldDriverVersion == "" || state.oldDriverVersion == null) state.oldDriverVersion = ""

	// Check
	if(logEnable) log.debug "Check - old: ${state.oldDriverVersion} vs. new: ${state.driverVersion} +++++++++++++++++++++++"
	if(state.oldDriverVersion == state.driverVersion){
		theCheck = "no"
		pnew = ""
		if(logEnable) log.warn "In checkTheData...Old Version: ${state.oldDriverVersion} - No Update Available - New: ${state.driverVersion}"
	}
	else {
		theCheck = "yes"
		pnew = "<span style='color:red'>NEW </span>"
		if(logEnable) log.warn "In checkTheData...Old Version: ${state.oldDriverVersion} - Update Available! - New: ${state.driverVersion}"
	}
 
	if(state.driverDiscussion) driverDiscussion2 = "<a href='${state.driverDiscussion}' target='_blank'>[Driver Discussion]</a>"
    if(state.driverRawCode != "-") {
        driverawCode2 = "<a href='${state.driverRawCode}' target='_blank'>[Parent Raw Code]</a>"
    } else driverRawCode2 = "-"
		
	if(theCheck == "yes"){
		state.driverMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b> <a href='${state.authorMainURL}' target='_blank'>(${state.authorName})</a></td><td width='25%'>${driverDiscussion2}</td></tr>"
		state.driverMap += "<tr><td width='36%'><i>Installed</i>: ${state.oldDriverVersion}</td><td width='32%'><i>Github</i>:  ${state.driverVersion}</td><td width='32%'>${pnew}${driverRawCode2}</td></tr>"

	    if(state.driverUpdateNote) state.driverMap += "<tr><td width='100%' colspan='3' align='left'>Notes: ${state.driverUpdateNote}</td></tr>"
		state.driverMap += "<tr><td width='100%' colspan='3' align='center'>________________________________________________________________</td></tr>"
		state.driverMapDash += "<tr><td>${state.dName}</td></tr>"
		state.driverMapPhone += "${state.dName} has an update available \n"
    }

	state.driverAllMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b> <a href='${state.authorMainURL}' target='_blank'>(${state.authorName})</a></td><td width='25%'>${driverDiscussion2}</td></tr>"
	state.driverAllMap += "<tr><td width='36%'><i>Installed</i>: ${state.oldDriverVersion}</td><td width='32%' </td><td width='32%'> </td></tr>"
	state.driverAllMap += "<tr><td width='100%' colspan='3' align='center'>________________________________________________________________</td></tr>"
}

def tileHandler(evt) {
	if(state.driverMapDash == "") { 
		driverMap2 = "All Drivers are up to date."
	} else {
		driverMap2 = "<table width='100%'><b>Drivers to update</b><br>${state.driverMapDash}</table>"
	}
	if(logEnable) log.debug "In tileHandler...Sending new Driver Watchdog data to ${parent.awDevice} - ${driverMap2}"
    parent.awDevice.sendDriverMap(driverMap2)
}

def clearMaps() {
	if(logEnable) log.debug "In clearMaps..."
    //states
    state.driverVersion = "-"
    state.driverRawCode = "-"
    state.driverDiscussion = "-"
    state.driverUpdateNote = "-"

    //Maps
	state.driverMap = [:]
	state.driverMap = ""
	state.driverAllMap = [:]
	state.driverAllMap = ""
	state.driverMapPhone = [:]
	state.driverMapPhone = ""
	state.driverMapDash = [:]
	state.driverMapDash = ""
	if(logEnable) log.debug "In clearMaps...Maps are clear"
}

def isThereData(){
	if(logEnable) log.debug "In isThereData..."
	if(state.driverMapPhone) {
		isDataDevice.on()
	} else {
		isDataDevice.off()
	}
}

def pushNow() {
	if(logEnable) log.debug "In pushNow..."
	if(sendPushMessage) {
		if(state.driverMapPhone) {
			pushMessage = "${driver.label} \n"
			pushMessage += "${state.driverMapPhone}"
			if(logEnable) log.debug "In pushNow...Sending message: ${pushMessage}"
        	sendPushMessage.deviceNotification(pushMessage)
		} else {
			if(pushAll == true) {
				log.info "${driver.label} - No push needed...Nothing to report."
			} else {
				emptyMapPhone = "${driver.label} \n"
				emptyMapPhone += "Nothing to report."
				if(logEnable) log.debug "In pushNow...Sending message: ${emptyMapPhone}"
        		sendPushMessage.deviceNotification(emptyMapPhone)
			}
		}
	}	
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable) log.debug "In setDefaults..."
	if(logEnable == null){logEnable = false}
	if(pushAll == null){pushAll = false}
	if(logEnable) log.debug "In setDefaults - Finished defaults"
}

def getImage(type) {				// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){		// Modified from @Stephack
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>App Watchdog 2 - Driver - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
