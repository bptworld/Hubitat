/**
 *  ****************  App Watchdog 2 Apps Child ****************
 *
 *  Design Usage:
 *  See if any compatible app needs an update, all in one place.
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
 *  V2.1.0 - 08/28/19 - Found a big bug in Developer 1 selection
 *  V2.0.9 - 08/27/19 - Fixed another typo
 *  V2.0.8 - 08/27/19 - Found a typo, added optional Paypal link to Developers page.
 *  V2.0.7 - 08/26/19 - Bug fixes
 *  V2.0.6 - 08/26/19 - Developers can now be selected from a dropdown box.
 *  V2.0.5 - 08/25/19 - Now you can select mutiple developers in one child app to truly get all your update notices together!
 *  V2.0.4 - 08/25/19 - Removed any code pertaining to 'drivers'. Squashed, smashed and swatted bugs.
 *  V2.0.3 - 08/25/19 - Code cleanup, squashing bugs! Working on changes suggested by @aaron, so things will look funny right now.
 *  V2.0.2 - 08/24/19 - Each Parent can now have up to 4 child apps. Working on a ton of bugs. There are still some to squash, sorry.
 *  V2.0.1 - 08/20/19 - Fixed some missing code in dName.
 *  V2.0.0 - 08/18/19 - Now data is automaticaly sent from compatible apps and then compared to github, when app is opened and on a daily schedule.
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. YourAppsNameParentVersion, YourAppsNameChildVersion
    state.appName = "AppWatchdog2AppsChildVersion"
	state.version = "v2.1.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "App Watchdog 2 Apps Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "See if any compatible app needs an update, all in one place.",
    category: "",
	parent: "BPTWorld:App Watchdog 2",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Apps%20Watchdog%202/AW2-Apps-child.groovy",
)

preferences {
    page name: "pageConfig"
	page name: "pageAppstoUpdate", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "pageCurrent", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "developerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>App Watchdog 2 - Apps</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "See if any compatible app needs an update, all in one place."
            paragraph "Note: This will only track apps, not drivers. Thanks"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
            paragraph "Be sure to setup at least one developer before running a report."
			href "pageAppstoUpdate", title: "App Watchdog Report", description: "Click here to view the App Watchdog Report."
			href "pageCurrent", title: "App Installed Report", description: "Click here to view the App Installed Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
            href "developerOptions", title:"Select Developers to follow here", description:"Click here to setup Developer Options"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			input "timeToRun", "time", title: "Check Apps at this time daily", required: true
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
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldApps.json":"BPTWorld"],
                ["https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/AaronWardAppUpdate.json":"Aaron Ward"]
            ]
			if(gitHubURL1) {
                gitHubCheck1()
				paragraph "Developer: ${state.gitHubAuthor1}"
                state.values1 = "${state.gitHubApps1}".split(",")
    		    input "installedApps1", "enum", title: "Select which apps you have installed", options: state.values1, required: true, multiple: true, submitOnChange: true
                if(state.paypalLink1) paragraph "<b>If you find ${state.gitHubAuthor1}'s apps useful. Please consider a donation via <a href='${state.paypalLink1}' target='_blank'>Paypal</a></b>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 2 Options")) {
            input "gitHubURL2", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldApps.json":"BPTWorld"],
                ["https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/AaronWardAppUpdate.json":"Aaron Ward"]
            ]
            if(gitHubURL2) {
				gitHubCheck2()
				paragraph "Developer: ${state.gitHubAuthor2}"
                state.values2 = "${state.gitHubApps2}".split(",")
    		    input "installedApps2", "enum", title: "Select which apps you have installed", options: state.values2, required: true, multiple: true, submitOnChange: true
                if(state.paypalLink2) paragraph "<b>If you find ${state.gitHubAuthor2}'s apps useful. Please consider a donation via <a href='${state.paypalLink2}' target='_blank'>Paypal</a></b>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 3 Options")) {
            input "gitHubURL3", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldApps.json":"BPTWorld"],
                ["https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/AaronWardAppUpdate.json":"Aaron Ward"]
            ]
            if(gitHubURL3) {
				gitHubCheck3()
				paragraph "Developer: ${state.gitHubAuthor3}"
                state.values3 = "${state.gitHubApps3}".split(",")
    		    input "installedApps3", "enum", title: "Select which apps you have installed", options: state.values3, required: true, multiple: true, submitOnChange: true
                if(state.paypalLink3) paragraph "<b>If you find ${state.gitHubAuthor3}'s apps useful. Please consider a donation via <a href='${state.paypalLink3}' target='_blank'>Paypal</a></b>"
            }
        }   
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 4 Options")) {
            input "gitHubURL4", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldApps.json":"BPTWorld"],
                ["https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/AaronWardAppUpdate.json":"Aaron Ward"]
            ]
            if(gitHubURL4) {
				gitHubCheck4()
				paragraph "Developer: ${state.gitHubAuthor4}"
                state.values4 = "${state.gitHubApps4}".split(",")
    		    input "installedApps4", "enum", title: "Select which apps you have installed", options: state.values4, required: true, multiple: true, submitOnChange: true
                if(state.paypalLink4) paragraph "<b>If you find ${state.gitHubAuthor4}'s apps useful. Please consider a donation via <a href='${state.paypalLink4}' target='_blank'>Paypal</a></b>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Developer 5 Options")) {
            input "gitHubURL5", "enum", title: "Select a Developer to follow", required: false, multiple: false, submitOnChange: true, options: [
                ["https://raw.githubusercontent.com/bptworld/Hubitat/master/bptworldApps.json":"BPTWorld"],
                ["https://raw.githubusercontent.com/PrayerfulDrop/Hubitat/master/AaronWardAppUpdate.json":"Aaron Ward"]
            ]
            if(gitHubURL5) {
				gitHubCheck5()
				paragraph "Developer: ${state.gitHubAuthor5}"
                state.values5 = "${state.gitHubApps5}".split(",")
    		    input "installedApps5", "enum", title: "Select which apps you have installed", options: state.values5, required: true, multiple: true, submitOnChange: true
                if(state.paypalLink5) paragraph "<b>If you find ${state.gitHubAuthor5}'s apps useful. Please consider a donation via <a href='${state.paypalLink5}' target='_blank'>Paypal</a></b>"
            }
        }

        state.allApps = [installedApps1,installedApps2,installedApps3,installedApps4,installedApps5].flatten().findAll{it} 
    }
}
            
def pageAppstoUpdate(params) {
	dynamicPage(name: "pageAppstoUpdate", title: "Apps Watchdog - Apps to Update", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		appMapHandler()
			section("How to Update", hideable: true, hidden: true) {
				paragraph "<b>To update your code:</b><br>(<i>Do this for each item that needs updating.</i>)"
				paragraph " - Right-click on any link and choose 'Copy Link Location'<br> - Go into your 'Apps Code' section<br> - Select the corresponding app<br> - 'Import' it in, 'Save' and done!"
				paragraph "* App version data will update the next time the app is opened or on it's next scheduled date/time."
			}
		if(state.appMap) {
			section() {
				updateMap = "<table width='100%'>${state.appMap}</table>"
				paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Apps with an update</h2>"
				paragraph "${updateMap}"
        	}
		} else {
			section() { 
				paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Apps with an update</h2>"
				paragraph "All apps are up to date"
			}
		}
	}
}

def pageCurrent(params) {
	dynamicPage(name: "pageCurrent", title: "Apps Watchdog - App Versions", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		appMapHandler()
		section() {
			updateAllMap = "<table width='100%'>${state.appAllMap}</table>"
			paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Apps - All</h2>"
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
	setDefaults()
	schedule(timeToRun, appMapHandler)
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def gitHubCheck1() {
	def params = [uri: "${gitHubURL1}", contentType: "application/json"]
	if(logEnable) log.debug "In gitHubCheck1... About to 'try' - ${gitHubURL1}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor1 = "${results.GitHubAuthor}"
			state.gitHubMainURL1 = results.GitHubMainURL
            state.gitHubApps1 = results.Apps
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
            state.gitHubApps2 = results.Apps
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
            state.gitHubApps3 = results.Apps
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
            state.gitHubApps4 = results.Apps
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
            state.gitHubApps5 = results.Apps
            state.paypalLink5 = results.PaypalLink
        }
	} 
    catch (e) {
        log.info "Warning:  GitHub URL 5 not found"
		state.gitHubAuthor5 = "GitHub: Not found"
    }
}

def appMapHandler(evt) {
	if(logEnable) log.debug "In appMapHandler..."
			clearMaps()
			if(state.allApps) {
				state.allApps.each { item ->
					state.appsName = item.replace(" ","")
					if(traceEnable) log.trace "----------- Starting App: ${item} -----------"
                    
                    if(installedApps1) {  
                        if(traceEnable) log.trace "Looking at InstalledApps 1"
                        def gitHub1 = installedApps1.toListString()
                        if(gitHub1.contains("${item}")) {
                            state.params = [uri: "${gitHubURL1}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor1
                            state.authorMainURL = state.gitHubMainURL1
                            if(traceEnable) log.trace "installedApps1 - appsName: ${item} - author: ${state.authorName} - ${state.params}"
                        } else {
                            log.trace "installedApps1 - No Match"
                        }
                    }
                    if(installedApps2) {
                        if(traceEnable) log.trace "Looking at InstalledApps 2"
                        def gitHub2 = installedApps2.toListString()
                        if(gitHub2.contains("${item}")) {
                            state.params = [uri: "${gitHubURL2}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor2
                            state.authorMainURL = state.gitHubMainURL2
                            if(traceEnable) log.trace "installedApps2 - appsName: ${item} - author: ${state.authorName}"
                        } else {
                            log.trace "installedApps2 - No Match"
                        }
                    }
                    if(installedApps3) {
                        if(traceEnable) log.trace "Looking at InstalledApps 3"
                        def gitHub3 = installedApps3.toListString()
                        if(gitHub3.contains("${item}")) {
                            state.params = [uri: "${gitHubURL3}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor3
                            state.authorMainURL = state.gitHubMainURL3
                            if(traceEnable) log.trace "installedApps3 - appsName: ${item} - author: ${state.authorName}"
                        } else {
                            log.trace "installedApps3 - No Match"
                        }
                    }
                    if(installedApps4) {
                        if(traceEnable) log.trace "Looking at InstalledApps 4"
                        def gitHub4 = installedApps4.toListString()
                        if(gitHub4.contains("${item}")) {
                            state.params = [uri: "${gitHubURL4}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor4
                            state.authorMainURL = state.gitHubMainURL4
                            if(traceEnable) log.trace "installedApps4 - appsName: ${item} - author: ${state.authorName}"
                        } else {
                            log.trace "installedApps4 - No Match"
                        }
                    }
                    if(installedApps5) {
                        if(traceEnable) log.trace "Looking at InstalledApps 5"
                        def gitHub5 = installedApps5.toListString()
                        if(gitHub5.contains("${item}")) {
                            state.params = [uri: "${gitHubURL5}", contentType: "application/json"]
                            state.authorName = state.gitHubAuthor5
                            state.authorMainURL = state.gitHubMainURL5
                            if(traceEnable) log.trace "installedApps5 - appsName: ${item} - author: ${state.authorName}"
                        } else {
                            log.trace "installedApps5 - No Match"
                        }
                    }
                    
       				try {
						httpGet(state.params) { response ->
							results = response.data
                            
							// Get Github Data from json
							if(traceEnable) log.trace "Getting Versions from json - AppName: ${state.appsName}"

							state.appParentVersion = results."${state.appsName}ParentVersion"
                            if(!state.appParentVersion) state.appParentVersion = "-"
                                
						    state.appChild1Version = results."${state.appsName}Child1Version"
                            if(!state.appChild1Version) state.appChild1Version = "-"
                                
                            state.appChild1Name = results."${state.appsName}Child1Name"
                            if(!state.appChild1Name) state.appChild1Name = "Child 1"
                                
                            state.appChild2Version = results."${state.appsName}Child2Version"
                            if(!state.appChild2Version) state.appChild2Version = "-"
                                
                            state.appChild2Name = results."${state.appsName}Child2Name"
                            if(!state.appChild2Name) state.appChild2Name = "Child 2"
                                
                            state.appChild3Version = results."${state.appsName}Child3Version"
                            if(!state.appChild3Version) state.appChild3Version = "-"
                                
                            state.appChild3Name = results."${state.appsName}Child3Name"
                            if(!state.appChild3Name) state.appChild3Name = "Child 3"
                                
                            state.appChild4Version = results."${state.appsName}Child4Version"
                            if(!state.appChild4Version) state.appChild4Version = "-"
                                
                            state.appChild4Name = results."${state.appsName}Child4Name"
                            if(!state.appChild4Name) state.appChild4Name = "Child 4"
                                
					        state.appParentRawCode = results."${state.appsName}ParentRawCode"
                            if(!state.appParentRawCode) state.appParentRawCode = "-"
                                
							state.appChild1RawCode = results."${state.appsName}Child1RawCode"
                            if(!state.appChild1RawCode) state.appChild1RawCode = "-"
                                
                            state.appChild2RawCode = results."${state.appsName}Child2RawCode"
                            if(!state.appChild2RawCode) state.appChild2RawCode = "-"
                                
                            state.appChild3RawCode = results."${state.appsName}Child3Raw3ode"
                            if(!state.appChild3RawCode) state.appChild3RawCode = "-"
                                
                            state.appChild4RawCode = results."${state.appsName}Child4RawCode"
                            if(!state.appChild4RawCode) state.appChild4RawCode = "-"
                                
						    state.appDiscussion = results."${state.appsName}Discussion"
                            if(!state.appDiscussion) state.appDiscussion = "-"
                                
							state.appUpdateNote = results."${state.appsName}UpdateNote"
                            if(!state.appUpdateNote) state.appUpdateNote = "-"
                            
                            if(traceEnable) log.trace "JSON results - ${item} - ${state.appParentVersion} - Child 1: ${state.appChild1Version} - Child 2: ${state.appChild2Version}"
                        }

						// Get Old Data from map

                        state.dName = item
                                
                        def watchMap = ""
                                
                        watchMap = parent.awDevice.currentValue("sendAWinfoMap").replace("{","").replace("}","")
                        if(logEnable) log.debug "watchMap: ${watchMap}"
                                
                        def theMap = watchMap.split(',').collectEntries { entry ->
                            def pair = entry.split('=')
                            [(pair.first()):pair.last()]
                        }
                                
                        theMapS = theMap.sort { a, b -> a.key <=> b.key }
                                
                        if(logEnable) log.debug "Sorted theMap: ${theMapS}"
                                
                        theMapS.each { it ->
                            appName = it.key
                            appVer = it.value
                            dName = state.dName.replace(" ", "")
                            //if(traceEnable) log.trace "Working on watchMap - Does appName: ${appName} contain dName: ${dName}"
                            if(appName.contains("Parent") && appName.contains("${dName}")) state.oldAppParentVersion = appVer
                                    
                            if(appName.contains("Child") && !appName.contains("Child2") && !appName.contains("Child3") && !appName.contains("Child4") && appName.contains("${dName}")) state.oldAppChild1Version = appVer                       
                            if(appName.contains("Child2") && appName.contains("${dName}")) state.oldAppChild2Version = appVer
                            if(appName.contains("Child3") && appName.contains("${dName}")) state.oldAppChild3Version = appVer
                            if(appName.contains("Child3") && appName.contains("${dName}")) state.oldAppChild4Version = appVer
                                    
                            if(!state.oldAppChild1Version) state.oldAppChild1Version = "-"
                            if(!state.oldAppChild2Version) state.oldAppChild2Version = "-"
                            if(!state.oldAppChild3Version) state.oldAppChild3Version = "-"
                            if(!state.oldAppChild4Version) state.oldAppChild4Version = "-"
                        }
                        if(traceEnable) log.trace "INSTALLED results - ${item} - ${state.oldAppParentVersion} - Child 1: ${state.oldAppChild1Version} - Child 2: ${state.oldAppChild2Version}"

                        checkTheAppData()
							   
                        state.oldAppParentVersion = ""
                        state.oldAppChild1Version = ""
                        state.oldAppChild2Version = ""
                        state.oldAppChild3Version = ""
                        state.oldAppChild4Version = ""
                        state.params = ""
				    }
                    catch(e) {
                        log.error "${e}"
                        log.warn "Somehting Went Wrong"
                    }
			    if(traceEnable) log.trace "----------- End App: ${item} -----------"	
            }
		}
	if(maintSwitch2 != true) {
		if(sendPushMessage) pushNow()
		if(parent.awDevice) tileHandler()
	}
}

def checkTheAppData() {
    childShortName = ""
    childShort2Name = ""
    childShort3Name = ""
    childShort4Name = ""
    
    if(state.oldAppParentVersion == "" || state.oldAppParentVersion == null) state.oldAppParentVersion = ""
    
    if(state.oldAppChild1Version == null) state.oldAppChild1Version = "-"
    if(state.oldAppChild2Version == null) state.oldAppChild2Version = "-"
    if(state.oldAppChild3Version == null) state.oldAppChild3Version = "-"
    if(state.oldAppChild4Version == null) state.oldAppChild4Version = "-"

	// Parent Check
	if(logEnable) log.debug "Check Parent - old: ${state.oldAppParentVersion} vs. new: ${state.appParentVersion} +++++++++++++++++++++++"
	if(state.oldAppParentVersion == state.appParentVersion){
		parentCheck = "no"
		pnew = ""
		if(logEnable) log.warn "In checkTheData...Old Parent Version: ${state.oldAppParentVersion} - No Update Available - New: ${state.appParentVersion}"
	}
	else {
		parentCheck = "yes"
		pnew = "<span style='color:red'>NEW </span>"
		if(logEnable) log.warn "In checkTheData...Old Parent Version: ${state.oldAppParentVersion} - Update Available! - New: ${state.appParentVersion}"
	}
							
	// Child Check
	if(state.oldAppChild1Version == state.appChild1Version){
		childCheck = "no"
		cnew = ""
        if(state.appChild1Name) childShortName = state.appChild1Name.take(10)
        else childShortName = "Child 1"
		if(logEnable) log.warn "In checkTheData...Old Child 1 Version: ${state.oldAppChild1Version} - No Update Available - New: ${state.appChild1Version}"
	}
	else {
		childCheck = "yes"
		cnew = "<span style='color:red'>NEW </span>"
        if(state.appChild1Name) childShortName = state.appChild1Name.take(10)
        else childShortName = "Child 1"
		if(logEnable) log.warn "In checkTheData...Old Child 1 Version: ${state.oldAppChild1Version} - Update Available! - New: ${state.appChild1Version}"
	}
    if(state.oldAppChild2Version == state.appChild2Version){
		childCheck2 = "no"
		c2new = ""
        if(state.appChild2Name) child2ShortName = state.appChild2Name.take(10)
        else child2ShortName = "Child 2"
		if(logEnable) log.warn "In checkTheData...Old Child 2 Version: ${state.oldAppChild2Version} - No Update Available - New: ${state.appChild2Version}"
	}
	else {
		childCheck2 = "yes"
		c2new = "<span style='color:red'>NEW </span>"
        if(state.appChild2Name) child2ShortName = state.appChild2Name.take(10)
        else child2ShortName = "Child 2"
		if(logEnable) log.warn "In checkTheData...Old Child 2 Version: ${state.oldAppChild2Version} - Update Available! - New: ${state.appChild2Version}"
	}
	if(state.oldAppChild3Version == state.appChild3Version){
		childCheck3 = "no"
		c3new = ""
        if(state.appChild3Name) child3ShortName = state.appChild3Name.take(10)
        else child3ShortName = "Child 3"
		if(logEnable) log.warn "In checkTheData...Old Child 3 Version: ${state.oldAppChild3Version} - No Update Available - New: ${state.appChild3Version}"
	}
	else {
		childCheck3 = "yes"
		c3new = "<span style='color:red'>NEW </span>"
        if(state.appChild3Name) child3ShortName = state.appChild3Name.take(10)
        else child3ShortName = "Child 3"
		if(logEnable) log.warn "In checkTheData...Old Child 3 Version: ${state.oldAppChild3Version} - Update Available! - New: ${state.appChild3Version}"
	}
    if(state.oldAppChild4Version == state.appChild4Version){
		childCheck4 = "no"
		c4new = ""
        if(state.appChild4Name) child4ShortName = state.appChild4Name.take(10)
        else child4ShortName = "Child 4"
		if(logEnable) log.warn "In checkTheData...Old 4 Child Version: ${state.oldAppChild4Version} - No Update Available - New: ${state.appChild4Version}"
	}
	else {
		childCheck4 = "yes"
		c4new = "<span style='color:red'>NEW </span>"
        if(state.appChild4Name) child4ShortName = state.appChild4Name.take(10)
        else child4ShortName = "Child 4"
		if(logEnable) log.warn "In checkTheData...Old Child 4 Version: ${state.oldAppChild4Version} - Update Available! - New: ${state.appChild4Version}"
	}
 
	if(state.appDiscussion) appDiscussion2 = "<a href='${state.appDiscussion}' target='_blank'>[App Discussion]</a>"
    if(state.appParentRawCode != "-") {
        appParentRawCode2 = "<a href='${state.appParentRawCode}' target='_blank'>[Parent Raw Code]</a>"
    } else appParentRawCode2 = "-"
    if(state.appChild1RawCode != "-") {
        appChild1RawCode2 = "<a href='${state.appChild1RawCode}' target='_blank'>[Child Raw Code]</a>"
    } else appChild1RawCode2 = "-"
    if(state.appChild2RawCode != "-") {
        appChild2RawCode2 = "<a href='${state.appChild2RawCode}' target='_blank'>[Child Raw Code]</a>"
    } else appChild2RawCode2 = "-"
    if(state.appChild3RawCode != "-") {
        appChild3RawCode2 = "<a href='${state.appChild3RawCode}' target='_blank'>[Child Raw Code]</a>"
    } else appChild3RawCode2 = "-"
    if(state.appChild4RawCode != "-") {
        appChild4RawCode2 = "<a href='${state.appChild4RawCode}' target='_blank'>[Child Raw Code]</a>"
    } else {
        appChild4RawCode2 = "-"
    }
		
	if(parentCheck == "yes" || childCheck == "yes"|| childCheck2 == "yes" || childCheck3 == "yes"|| childCheck4 == "yes"){
		state.appMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b> <a href='${state.authorMainURL}' target='_blank'>(${state.authorName})</a></td><td width='25%'>${appDiscussion2}</td></tr>"
		state.appMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>${childShortName}: ${state.oldAppChild1Version}</td><td width='32%'> </td></tr>"
		state.appMap += "<tr><td width='36%'><i>Github</i>:  Parent: ${state.appParentVersion}</td><td width='32%'>Child 1: ${state.appChild1Version}</td><td width='32%'> </td></tr>"
		state.appMap += "<tr><td width='36%'>${pnew}${appParentRawCode2}</td><td width='32%'>${cnew}${appChild1RawCode2}</td><td width='32%'> </td></tr>"
            
        if(state.appChild2Version != "-" || state.appChild3Version != "-" || state.appChild4Version != "-" || state.oldAppChild2Version != "-" || state.oldAppChild3Version != "-" || state.oldAppChild4Version != "-") {
            state.appMap += "<tr><td colspan='3'> </td></tr>"
            state.appMap += "<tr><td width='36%'>${child2ShortName}: ${state.oldAppChild2Version}</td><td width='32%'>${child3ShortName}: ${state.oldAppChild3Version}</td><td width='32%'>${child4ShortName}: ${state.oldAppChild4Version}</td></tr>"
			state.appMap += "<tr><td width='36%'>Child 2: ${state.appChild2Version}</td><td width='32%'>Child 3: ${state.appChild3Version}</td><td width='32%'>Child 4: ${state.appChild4Version}</td></tr>"
            state.appMap += "<tr><td width='36%'>${c2new}${appChild2RawCode2}</td><td width='32%'>${c3new}${appChild3RawCode2}</td><td width='32%'>${c4new}${appChild4RawCode2}</td></tr>"
        }

	    if(state.appUpdateNote) state.appMap += "<tr><td width='100%' colspan='3' align='left'>Notes: ${state.appUpdateNote}</td></tr>"
		state.appMap += "<tr><td width='100%' colspan='3' align='center'>________________________________________________________________</td></tr>"
		state.appMapDash += "<tr><td>${state.dName}</td></tr>"
		state.appMapPhone += "${state.dName} has an update available \n"
    }

	state.appAllMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b> <a href='${state.authorMainURL}' target='_blank'>(${state.authorName})</a></td><td width='25%'>${appDiscussion2}</td></tr>"
	state.appAllMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>Child: ${state.oldAppChild1Version}</td><td width='32%'> </td></tr>"
	state.appAllMap += "<tr><td width='100%' colspan='3' align='center'>________________________________________________________________</td></tr>"
}

def tileHandler(evt) {
	if(state.appMapDash == "") { 
		appMap2 = "All Apps are up to date."
	} else {
		appMap2 = "<table width='100%'><b>Apps to update</b><br>${state.appMapDash}</table>"
	}
	if(logEnable) log.debug "In tileHandler...Sending new App Watchdog 2 data to ${parent.awDevice} - ${appMap2}"
    parent.awDevice.sendDataMap(appMap2)
}

def clearMaps() {
	if(logEnable) log.debug "In clearMaps..."
    //states
    state.appParentVersion = "-"
    state.appChild1Version = "-"
    state.appChild2Version = "-"
    state.appChild3Version = "-"
    state.appChild4Version = "-"
    
    state.appChild1Name = "-"
    state.appChild2Name = "-"
    state.appChild3Name = "-"
    state.appChild4Name = "-"
  
    state.appParentRawCode = "-"
    
    state.appChild1RawCode = "-"
    state.appChild2RawCode = "-"
    state.appChild3RawCode = "-"
    state.appChild4RawCode = "-"
    
    state.appDiscussion = "-"
    state.appUpdateNote = "-"

    //Maps
	state.appMap = [:]
	state.appMap = ""
	state.appAllMap = [:]
	state.appAllMap = ""
	state.appMapPhone = [:]
	state.appMapPhone = ""
	state.appMapDash = [:]
	state.appMapDash = ""
	if(logEnable) log.debug "In clearMaps...Maps are clear"
}

def isThereData(){
	if(logEnable) log.debug "In isThereData..."
	if(state.appMapPhone) {
		isDataDevice.on()
	} else {
		isDataDevice.off()
	}
}

def pushNow() {
	if(logEnable) log.debug "In pushNow..."
	if(sendPushMessage) {
		if(state.appMapPhone) {
			pushMessage = "${app.label} \n"
			pushMessage += "${state.appMapPhone}"
			if(logEnable) log.debug "In pushNow...Sending message: ${pushMessage}"
        	sendPushMessage.deviceNotification(pushMessage)
		} else {
			if(pushAll == true) {
				log.info "${app.label} - No push needed...Nothing to report."
			} else {
				emptyMapPhone = "${app.label} \n"
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
		paragraph "<div style='color:#1A77C9;text-align:center'>App Watchdog 2 - Apps - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
