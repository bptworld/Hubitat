/**
 *  ****************  App Watchdog 2 Child ****************
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.0 - 08/18/19 - Now data is automaticaly sent from compatible apps and then compared to github, when app is opened and on a daily schedule.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "AppWatchdog2ChildVersion"
	state.version = "v2.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
            schedule("0 0 3 ? * * *", setVersion)
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "App Watchdog 2 Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "See if any compatible app needs an update, all in one place.",
    category: "",
	parent: "BPTWorld:App Watchdog 2",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Apps%20Watchdog%202/AW2-child.groovy",
)

preferences {
    page(name: "pageConfig")
	page(name: "pageAppstoUpdate")
	page(name: "pageCurrent")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>App Watchdog 2</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "See if any compatible app needs an update, all in one place."
            paragraph "Note: This will only track apps, not drivers. Hopefully at some point it will also track drivers. Thanks"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageAppstoUpdate", title: "App Watchdog Report", description: "Click here to view the App Watchdog Report."
			href "pageCurrent", title: "App Current Report", description: "Click here to view the App Current Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
			input "gitHubURL", "text", title: "URL for the GitHub to follow", required: true, submitOnChange: true
			if(gitHubURL) {
				gitHubCheck()
				paragraph "${state.gitHubAuthor}"
			}
    		input "installedApps", "enum", title: "Select which apps you have installed", options: [
				["${state.app01NoSpace}":"${state.app01}"],
				["${state.app02NoSpace}":"${state.app02}"],
				["${state.app03NoSpace}":"${state.app03}"],
				["${state.app04NoSpace}":"${state.app04}"],
				["${state.app05NoSpace}":"${state.app05}"],
				["${state.app06NoSpace}":"${state.app06}"],
				["${state.app07NoSpace}":"${state.app07}"],
				["${state.app08NoSpace}":"${state.app08}"],
				["${state.app09NoSpace}":"${state.app09}"],
				["${state.app10NoSpace}":"${state.app10}"],
				["${state.app11NoSpace}":"${state.app11}"],
				["${state.app12NoSpace}":"${state.app12}"],
				["${state.app13NoSpace}":"${state.app13}"],
				["${state.app14NoSpace}":"${state.app14}"],
				["${state.app15NoSpace}":"${state.app15}"],
				["${state.app16NoSpace}":"${state.app16}"],
				["${state.app17NoSpace}":"${state.app17}"],
				["${state.app18NoSpace}":"${state.app18}"],
				["${state.app19NoSpace}":"${state.app19}"],
				["${state.app20NoSpace}":"${state.app20}"],
                ["${state.app21NoSpace}":"${state.app21}"],
                ["${state.app22NoSpace}":"${state.app22}"],
                ["${state.app23NoSpace}":"${state.app23}"],
                ["${state.app24NoSpace}":"${state.app24}"],
                ["${state.app25NoSpace}":"${state.app25}"],
                ["${state.app26NoSpace}":"${state.app26}"],
                ["${state.app27NoSpace}":"${state.app27}"],
                ["${state.app28NoSpace}":"${state.app28}"],
                ["${state.app29NoSpace}":"${state.app29}"],
                ["${state.app30NoSpace}":"${state.app30}"],
                ["${state.app31NoSpace}":"${state.app31}"],
                ["${state.app32NoSpace}":"${state.app32}"],
                ["${state.app33NoSpace}":"${state.app33}"],
                ["${state.app34NoSpace}":"${state.app34}"],
                ["${state.app35NoSpace}":"${state.app35}"],
                ["${state.app36NoSpace}":"${state.app36}"],
                ["${state.app37NoSpace}":"${state.app37}"],
                ["${state.app38NoSpace}":"${state.app38}"],
                ["${state.app39NoSpace}":"${state.app39}"],
                ["${state.app40NoSpace}":"${state.app40}"],
			], required: true, multiple: true, submitOnChange: true
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			input "timeToRun", "time", title: "Check Apps at this time daily", required: true
			input "isDataDevice", "capability.switch", title: "Turn this device on if there is data to report", required: false, multiple: false
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false, submitOnChange: true
			if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
		section() {
			input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
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
				paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Apps with an update</h2><a href='${state.gitHubMainURL}' target='_blank'>${state.gitHubAuthor}</a>"
				paragraph "${updateMap}"
        	}
		} else {
			section() { 
				paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Apps with an update</h2><br><a href='${state.gitHubMainURL}' target='_blank'>${state.gitHubAuthor}</a>"
				paragraph "All apps are up to date"
			}
		}
	}
}

def pageCurrent(params) {
	dynamicPage(name: "pageCurrent", title: "Apps Watchdog - Current App Versions", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		appMapHandler()
		
		section() {
			updateAllMap = "<table width='100%'>${state.appAllMap}</table>"
			paragraph "<h2 style='color:#1A77C9;font-weight: bold'>Apps - All</h2><a href='${state.gitHubMainURL}' target='_blank'>${state.gitHubAuthor}</a>"
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
}

def gitHubCheck() {
	def params = [uri: "${gitHubURL}", contentType: "application/json"]
	if(logEnable) log.debug "In gitHubCheck... About to 'try' - ${gitHubURL}"
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor = "GitHub: ${results.GitHubAuthor}"
			state.gitHubMainURL = results.GitHubMainURL
			state.app01 = results.App01
			state.app02 = results.App02
			state.app03 = results.App03
			state.app04 = results.App04
			state.app05 = results.App05
			state.app06 = results.App06
			state.app07 = results.App07
			state.app08 = results.App08
			state.app09 = results.App09
			state.app10 = results.App10
			state.app11 = results.App11
			state.app12 = results.App12
			state.app13 = results.App13
			state.app14 = results.App14
			state.app15 = results.App15
			state.app16 = results.App16
			state.app17 = results.App17
			state.app18 = results.App18
			state.app19 = results.App19
			state.app20 = results.App20
            state.app21 = results.App21
            state.app22 = results.App22
            state.app23 = results.App23
            state.app24 = results.App24
            state.app25 = results.App25
            state.app26 = results.App26
            state.app27 = results.App27
            state.app28 = results.App28
            state.app29 = results.App29
            state.app30 = results.App30
            state.app31 = results.App31
            state.app32 = results.App32
            state.app33 = results.App33
            state.app34 = results.App34
            state.app35 = results.App35
            state.app36 = results.App36
            state.app37 = results.App37
            state.app38 = results.App38
            state.app39 = results.App39
            state.app40 = results.App40
			
			if(state.app01) {
				state.app01NoSpace = state.app01.replace(" ", "")
			} else { 
				state.app01NoSpace = "NoApp"
				state.app01 = "No App"
			}
			if(state.app02) {
				state.app02NoSpace = state.app02.replace(" ", "")
			} else { 
				state.app02NoSpace = "NoApp"
				state.app02 = "No App"
			}
			if(state.app03) { 
				state.app03NoSpace = state.app03.replace(" ", "")
			} else { 
				state.app03NoSpace = "NoApp"
				state.app03 = "No App"
			}
			if(state.app04) {
				state.app04NoSpace = state.app04.replace(" ", "")
			} else { 
				state.app04NoSpace = "NoApp"
				state.app04 = "No App"
			}
			if(state.app05) {
				state.app05NoSpace = state.app05.replace(" ", "")
			} else { 
				state.app05NoSpace = "NoApp"
				state.app05 = "No App"
			}
			if(state.app06) {
				state.app06NoSpace = state.app06.replace(" ", "")
			} else { 
				state.app06NoSpace = "NoApp"
				state.app06 = "No App"
			}
			if(state.app07) {
				state.app07NoSpace = state.app07.replace(" ", "")
			} else { 
				state.app07NoSpace = "NoApp"
				state.app07 = "No App"
			}
			if(state.app08) {
				state.app08NoSpace = state.app08.replace(" ", "")
			} else { 
				state.app08NoSpace = "NoApp"
				state.app08 = "No App"
			}
			if(state.app09) {
				state.app09NoSpace = state.app09.replace(" ", "")
			} else { 
				state.app09NoSpace = "NoApp"
				state.app09 = "No App"
			}
			if(state.app10) {
				state.app10NoSpace = state.app10.replace(" ", "")
			} else { 
				state.app10NoSpace = "NoApp"
				state.app10 = "No App"
			}
			if(state.app11) {
				state.app11NoSpace = state.app11.replace(" ", "")
			} else { 
				state.app11NoSpace = "NoApp"
				state.app11 = "No App"
			}
			if(state.app12) {
				state.app12NoSpace = state.app12.replace(" ", "")
			} else { 
				state.app12NoSpace = "NoApp"
				state.app12 = "No App"
			}
			if(state.app13) {
				state.app13NoSpace = state.app13.replace(" ", "")
			} else { 
				state.app13NoSpace = "NoApp"
				state.app13 = "No App"
			}
			if(state.app14) {
				state.app14NoSpace = state.app14.replace(" ", "")
			} else { 
				state.app14NoSpace = "NoApp"
				state.app14 = "No App"
			}
			if(state.app15) {
				state.app15NoSpace = state.app15.replace(" ", "")
			} else { 
				state.app15NoSpace = "NoApp"
				state.app15 = "No App"
			}
			if(state.app16) {
				state.app16NoSpace = state.app16.replace(" ", "")
			} else { 
				state.app16NoSpace = "NoApp"
				state.app16 = "No App"
			}
			if(state.app17) {
				state.app17NoSpace = state.app17.replace(" ", "")
			} else { 
				state.app17NoSpace = "NoApp"
				state.app17 = "No App"
			}
			if(state.app18) {
				state.app18NoSpace = state.app18.replace(" ", "")
			} else { 
				state.app18NoSpace = "NoApp"
				state.app18 = "No App"
			}
			if(state.app19) {
				state.app19NoSpace = state.app19.replace(" ", "")
			} else { 
				state.app19NoSpace = "NoApp"
				state.app19 = "No App"
			}
			if(state.app20) {
				state.app20NoSpace = state.app20.replace(" ", "")
			} else { 
				state.app20NoSpace = "NoApp"
				state.app20 = "No App"
			}
            if(state.app21) {
				state.app21NoSpace = state.app21.replace(" ", "")
			} else { 
				state.app21NoSpace = "NoApp"
				state.app21 = "No App"
			}
            if(state.app22) {
				state.app22NoSpace = state.app22.replace(" ", "")
			} else { 
				state.app22NoSpace = "NoApp"
				state.app22 = "No App"
			}
            if(state.app23) {
				state.app23NoSpace = state.app23.replace(" ", "")
			} else { 
				state.app23NoSpace = "NoApp"
				state.app23 = "No App"
			}
            if(state.app24) {
				state.app24NoSpace = state.app24.replace(" ", "")
			} else { 
				state.app24NoSpace = "NoApp"
				state.app24 = "No App"
			}
            if(state.app25) {
				state.app25NoSpace = state.app25.replace(" ", "")
			} else { 
				state.app25NoSpace = "NoApp"
				state.app25 = "No App"
			}
            if(state.app26) {
				state.app26NoSpace = state.app26.replace(" ", "")
			} else { 
				state.app26NoSpace = "NoApp"
				state.app26 = "No App"
			}
            if(state.app27) {
				state.app27NoSpace = state.app27.replace(" ", "")
			} else { 
				state.app27NoSpace = "NoApp"
				state.app27 = "No App"
			}
            if(state.app28) {
				state.app28NoSpace = state.app28.replace(" ", "")
			} else { 
				state.app28NoSpace = "NoApp"
				state.app28 = "No App"
			}
            if(state.app29) {
				state.app29NoSpace = state.app29.replace(" ", "")
			} else { 
				state.app29NoSpace = "NoApp"
				state.app29 = "No App"
			}
            if(state.app30) {
				state.app30NoSpace = state.app30.replace(" ", "")
			} else { 
				state.app30NoSpace = "NoApp"
				state.app30= "No App"
			}
            if(state.app31) {
				state.app31NoSpace = state.app31.replace(" ", "")
			} else { 
				state.app31NoSpace = "NoApp"
				state.app31= "No App"
			}
            if(state.app32) {
				state.app32NoSpace = state.app32.replace(" ", "")
			} else { 
				state.app32NoSpace = "NoApp"
				state.app32= "No App"
			}
            if(state.app33) {
				state.app33NoSpace = state.app33.replace(" ", "")
			} else { 
				state.app33NoSpace = "NoApp"
				state.app33= "No App"
			}
            if(state.app34) {
				state.app34NoSpace = state.app34.replace(" ", "")
			} else { 
				state.app34NoSpace = "NoApp"
				state.app34= "No App"
			}
            if(state.app35) {
				state.app35NoSpace = state.app35.replace(" ", "")
			} else { 
				state.app35NoSpace = "NoApp"
				state.app35= "No App"
			}
            if(state.app36) {
				state.app36NoSpace = state.app36.replace(" ", "")
			} else { 
				state.app36NoSpace = "NoApp"
				state.app36= "No App"
			}
            if(state.app37) {
				state.app37NoSpace = state.app37.replace(" ", "")
			} else { 
				state.app37NoSpace = "NoApp"
				state.app37= "No App"
			}
            if(state.app38) {
				state.app38NoSpace = state.app38.replace(" ", "")
			} else { 
				state.app38NoSpace = "NoApp"
				state.app38= "No App"
			}
            if(state.app39) {
				state.app39NoSpace = state.app39.replace(" ", "")
			} else { 
				state.app39NoSpace = "NoApp"
				state.app39= "No App"
			}
            if(state.app40) {
				state.app40NoSpace = state.app40.replace(" ", "")
			} else { 
				state.app40NoSpace = "NoApp"
				state.app40= "No App"
			}
		}
	} 
    catch (e) {
        log.info "Warning:  GitHub URL not found"
		state.gitHubAuthor = "GitHub: Not found"
    }
}

def appMapHandler(evt) {
	if(logEnable) log.debug "In appMapHandler..."
			clearMaps()
			if(installedApps) {
				installedApps.each { item ->
					state.appsName = item
					if(logEnable) log.debug "----------- Starting App: ${item} -----------"
					
					def params = [uri: "${gitHubURL}", contentType: "application/json"]
					if(logEnable) log.debug "In appMapHandler... About to 'try' - ${item}"
       				try {
						httpGet(params) { response ->
							results = response.data
					
							state.aType = results."${item}Type"
							// Get Current Data from json
							if(logEnable) log.debug "Getting NEW Versions from json - AppName: ${item} - Type: ${state.aType}"
							if(state.aType == "App") {
								state.appParentVersion = results."${item}ParentVersion"
								state.appChildVersion = results."${item}ChildVersion"
								state.appDriverVersion = results."${item}DriverVersion"
								state.appParentRawCode = results."${item}ParentRawCode"
								state.appChildRawCode = results."${item}ChildRawCode"
								state.appDriverRawCode = results."${item}DriverRawCode"
								state.appDiscussion = results."${item}Discussion"
								state.appUpdateNote = results."${item}UpdateNote"
							}
							if(state.aType == "Driver") {
								state.appDriver1Version = results."${item}Driver1Version"
								state.appDriver2Version = results."${item}Driver2Version"
								state.appDriver3Version = results."${item}Driver3Version"
								state.appDriver4Version = results."${item}Driver4Version"
								state.appDriver5Version = results."${item}Driver5Version"
								state.appDriver6Version = results."${item}Driver6Version"
								state.appDriver1RawCode = results."${item}Driver1RawCode"
								state.appDriver2RawCode = results."${item}Driver2RawCode"
								state.appDriver3RawCode = results."${item}Driver3RawCode"
								state.appDriver4RawCode = results."${item}Driver4RawCode"
								state.appDriver5RawCode = results."${item}Driver5RawCode"
								state.appDriver6RawCode = results."${item}Driver6RawCode"
								state.appDriver1Name = results."${item}Driver1Name"
								state.appDriver2Name = results."${item}Driver2Name"
								state.appDriver3Name = results."${item}Driver3Name"
								state.appDriver4Name = results."${item}Driver4Name"
								state.appDriver5Name = results."${item}Driver5Name"
								state.appDriver6Name = results."${item}Driver6Name"
								state.appDiscussion = results."${item}Discussion"
								state.appUpdateNote = results."${item}UpdateNote"
							}
								
							// Get Old Data from map
							try {
                                getAppNameHandler()
                                
                                def watchMap = ""
                                
                                watchMap = parent.awDevice.currentValue("sendAWinfoMap").replace("{","").replace("}","")
                                if(logEnable) log.debug "watchMap: ${watchMap}"
                                
                                
                                def theMap = watchMap.split(',').collectEntries { entry ->
                                    def pair = entry.split('=')
                                    [(pair.first()):pair.last()]
                                }
                                
                                theMapS = theMap.sort { a, b -> a.key <=> b.key }
                                
                                if(logEnable) log.debug "Made it - theMap: ${theMapS}"
                                
                                theMapS.each { it ->
                                    appName = it.key
                                    appVer = it.value
                                    if(logEnable) log.debug "Working on watchMap - dName: ${state.dName} - appName: ${appName} - appVer: ${appVer}"
                               
                                    dName = state.dName.replace(" ", "")
                                    if(appName.contains("Parent") && appName.contains("${dName}")) state.oldAppParentVersion = appVer
                                    if(appName.contains("Child") && appName.contains("${dName}")) state.oldAppChildVersion = appVer
                                    if(appName.contains("Driver") && appName.contains("${dName}")) state.oldAppDriverVersion = appVer
								
							        //	state.oldAppDriver1Version = state.oldDriver1Map.get(item)
							        //	state.oldAppDriver2Version = state.oldDriver2Map.get(item)
							        //	state.oldAppDriver3Version = state.oldDriver3Map.get(item)
							        //	state.oldAppDriver4Version = state.oldDriver4Map.get(item)
							        //	state.oldAppDriver5Version = state.oldDriver5Map.get(item)
							        //	state.oldAppDriver6Version = state.oldDriver6Map.get(item) 
                                }
                                if(state.aType == "App") checkTheAppData()
							    if(state.aType == "Driver") checkTheDriverData()
                                state.oldAppParentVersion = ""
                                state.oldAppChildVersion = ""
                                state.oldAppDriverVersion = ""
							}
							catch (e) {
								//if(logEnable) log.debug "***** In appMapHandler - Something went wrong!  *****"
                                if(logEnable) log.error "${e}"
							}
						}
					} 
       			 	catch (e) {
        				log.error "Error:  $e"
    				}
					if(logEnable) log.debug "----------- End App: ${item} -----------"		 
				}		
		}
	if(maintSwitch2 != true) {
		if(sendPushMessage) pushNow()
		if(parent.awDevice) tileHandler()
	}
}

def checkTheAppData() {
	// Parent Check
	if(logEnable) log.debug "Check Parent - old: ${state.oldAppParentVersion} vs. new: ${state.appParentVersion} +++++++++++++++++++++++"
	if(state.oldAppParentVersion == state.appParentVersion){
		parentCheck = "no"
		pnew = ""
		if(logEnable) log.debug "In checkTheData...Old Parent Version: ${state.oldAppParentVersion} - No Update Available - New: ${state.appParentVersion}"
	}
	else {
		parentCheck = "yes"
		pnew = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheData...Old Parent Version: ${state.oldAppParentVersion} - Update Available! - New: ${state.appParentVersion}"
	}
							
	// Child Check
	if(state.oldAppChildVersion == state.appChildVersion){
		childCheck = "no"
		cnew = ""
		if(logEnable) log.debug "In checkTheData...Old Child Version: ${state.oldAppChildVersion} - No Update Available - New: ${state.appChildVersion}"
	}
	else {
		childCheck = "yes"
		cnew = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheData...Old Child Version: ${state.oldAppChildVersion} - Update Available! - New: ${state.appChildVersion}"
	}
							
	// Driver Check
	if(state.oldAppDriverVersion == state.appDriverVersion){
		driverCheck = "no"
		dnew = ""
		if(logEnable) log.debug "In checkTheData...Old Driver Version: ${state.oldAppDriverVersion} - No Update Available - New: ${state.appDriverVersion}"
	}
	else {
		driverCheck = "yes"
		//dnew = "<span style='color:red'>NEW </span>"
        dnew = ""
		if(logEnable) log.debug "In checkTheData...Old Driver Version: ${state.oldAppDriverVersion} - Update Available!- New: ${state.appDriverVersion}"
	}

		if(state.appDiscussion != "NA") {
			appDiscussion2 = "<a href='${state.appDiscussion}' target='_blank'>[App Discussion]</a>"
		} else {
			appDiscussion2 = "NA"
		}
		if(state.appParentRawCode != "NA") {
			appParentRawCode2 = "<a href='${state.appParentRawCode}' target='_blank'>[Parent Raw Code]</a>"
		} else {
			appParentRawCode2 = "NA"
		}	
		if(state.appChildRawCode != "NA") {
			appChildRawCode2 = "<a href='${state.appChildRawCode}' target='_blank'>[Child Raw Code]</a>"
		} else {
			appChildRawCode2 = "NA"
		}	
		if(state.appDriverRawCode != "NA") {
			appDriverRawCode2 = "<a href='${state.appDriverRawCode}' target='_blank'>[Driver Raw Code]</a>"
		} else {
			appDriverRawCode2 = "NA"
		}	
	
    if(state.oldAppParentVersion == "" || state.oldAppParentVersion == null) state.oldAppParentVersion = "No Data"
    if(state.oldAppChildVersion == "" || state.oldAppChildVersion == null) state.oldAppChildVersion = "No Data"
    if(state.oldAppDriverVersion == "" || state.oldAppDriverVersion == null) state.oldAppDriverVersion = "No Data"
    
    getAppNameHandler()
		
	if(parentCheck == "yes" || childCheck == "yes" || driverCheck == "yes") {
		if(state.dName != "Example") {
			state.appMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>Child: ${state.oldAppChildVersion}</td><td width='32%'>Driver: ${state.oldAppDriverVersion}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Current</i>:  Parent: ${state.appParentVersion}</td><td width='32%'>Child: ${state.appChildVersion}</td><td width='32%'>Driver: ${state.appDriverVersion}</td></tr>"
			state.appMap += "<tr><td width='36%'>${pnew}${appParentRawCode2}</td><td width='32%'>${cnew}${appChildRawCode2}</td><td width='32%'>${dnew}${appDriverRawCode2}</td></tr>"
			if(state.appUpdateNote != "NA") { state.appMap += "<tr><td width='100%' colspan='3' align='left'>Notes: ${state.appUpdateNote}</td></tr>" }
			state.appMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
			state.appMapDash += "<tr><td>${state.dName}</td></tr>"
			state.appMapPhone += "${state.dName} has an update available \n"
		}
	}
	if(state.dName != "Example") {
		state.appAllMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
		state.appAllMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>Child: ${state.oldAppChildVersion}</td><td width='32%'>Driver: ${state.oldAppDriverVersion}</td></tr>"
		state.appAllMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
	}
}

def checkTheDriverData() {
	// Driver1 Check
	if(logEnable) log.debug "Check Driver 1 - old: ${state.oldAppDriver1Version} vs. new: ${state.appDriver1Version} +++++++++++++++++++++++"
	if(state.oldAppDriver1Version == state.appDriver1Version){
		driver1Check = "no"
		d1new = ""
		if(logEnable) log.debug "In checkTheDriverData...Old Driver1 Version: ${state.oldAppDriver1Version} - No Update Available - New: ${state.appDriver1Version}"
	}
	else {
		driver1Check = "yes"
		d1new = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheDriverData...Old Driver1 Version: ${state.oldAppDriver1Version} - Update Available! - New: ${state.appDriver1Version}"
	}
	// Driver2 Check
	if(logEnable) log.debug "Check Driver 2 - old: ${state.oldAppDriver2Version} vs. new: ${state.appDriver2Version} +++++++++++++++++++++++"
	if(state.oldAppDriver2Version == state.appDriver2Version){
		driver2Check = "no"
		d2new = ""
		if(logEnable) log.debug "In checkTheDriverData...Old Driver2 Version: ${state.oldAppDriver2Version} - No Update Available - New: ${state.appDriver2Version}"
	}
	else {
		driver2Check = "yes"
		d2new = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheDriverData...Old Driver2 Version: ${state.oldAppDriver2Version} - Update Available! - New: ${state.appDriver2Version}"
	}
	// Driver3 Check
	if(logEnable) log.debug "Check Driver 3 - old: ${state.oldAppDriver3Version} vs. new: ${state.appDriver3Version} +++++++++++++++++++++++"
	if(state.oldAppDriver3Version == state.appDriver3Version){
		driver3Check = "no"
		d3new = ""
		if(logEnable) log.debug "In checkTheDriverData...Old Driver3 Version: ${state.oldAppDriver3Version} - No Update Available - New: ${state.appDriver3Version}"
	}
	else {
		driver3Check = "yes"
		d3new = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheDriverData...Old Driver3 Version: ${state.oldAppDriver3Version} - Update Available! - New: ${state.appDriver3Version}"
	}
	// Driver4 Check
	if(logEnable) log.debug "Check Driver 4 - old: ${state.oldAppDriver4Version} vs. new: ${state.appDriver4Version} +++++++++++++++++++++++"
	if(state.oldAppDriver4Version == state.appDriver4Version){
		driver4Check = "no"
		d4new = ""
		if(logEnable) log.debug "In checkTheDriverData...Old Driver4 Version: ${state.oldAppDriver4Version} - No Update Available - New: ${state.appDriver4Version}"
	}
	else {
		driver4Check = "yes"
		d4new = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheDriverData...Old Driver4 Version: ${state.oldAppDriver4Version} - Update Available! - New: ${state.appDriver4Version}"
	}
	// Driver5 Check
	if(logEnable) log.debug "Check Driver 5 - old: ${state.oldAppDriver5Version} vs. new: ${state.appDriver5Version} +++++++++++++++++++++++"
	if(state.oldAppDriver5Version == state.appDriver5Version){
		driver5Check = "no"
		d5new = ""
		if(logEnable) log.debug "In checkTheDriverData...Old Driver5 Version: ${state.oldAppDriver5Version} - No Update Available - New: ${state.appDriver5Version}"
	}
	else {
		driver5Check = "yes"
		d5new = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheDriverData...Old Driver5 Version: ${state.oldAppDriver5Version} - Update Available! - New: ${state.appDriver5Version}"
	}
	// Driver6 Check
	if(logEnable) log.debug "Check Driver 6 - old: ${state.oldAppDriver6Version} vs. new: ${state.appDriver6Version} +++++++++++++++++++++++"
	if(state.oldAppDriver6Version == state.appDriver6Version){
		driver6Check = "no"
		d6new = ""
		if(logEnable) log.debug "In checkTheDriverData...Old Driver6 Version: ${state.oldAppDriver6Version} - No Update Available - New: ${state.appDriver6Version}"
	}
	else {
		driver6Check = "yes"
		d6new = "<span style='color:red'>NEW </span>"
		if(logEnable) log.debug "In checkTheDriverData...Old Driver6 Version: ${state.oldAppDriver6Version} - Update Available! - New: ${state.appDriver6Version}"
	}
		if(state.appDiscussion != "NA") {
			appDiscussion2 = "<a href='${state.appDiscussion}' target='_blank'>[Driver Discussion]</a>"
		} else {
			appDiscussion2 = "Not Found"
		}
		if(state.appDriver1RawCode != "NA") {
			appDriver1RawCode2 = "<a href='${state.appDriver1RawCode}' target='_blank'>[Driver 1 Raw Code]</a>"
		} else {
			appDriver1RawCode2 = "Not Found"
		}	
		if(state.appDriver2RawCode != "NA") {
			appDriver2RawCode2 = "<a href='${state.appDriver2RawCode}' target='_blank'>[Driver 2 Raw Code]</a>"
		} else {
			appDriver2RawCode2 = "Not Found"
		}	
		if(state.appDriver3RawCode != "NA") {
			appDriver3RawCode2 = "<a href='${state.appDriver3RawCode}' target='_blank'>[Driver 3 Raw Code]</a>"
		} else {
			appDriver3RawCode2 = "Not Found"
		}	
		if(state.appDriver4RawCode != "NA") {
			appDriver4RawCode2 = "<a href='${state.appDriver4RawCode}' target='_blank'>[Driver 4 Raw Code]</a>"
		} else {
			appDriver4RawCode2 = "Not Found"
		}
		if(state.appDriver5RawCode != "NA") {
			appDriver5RawCode2 = "<a href='${state.appDriver5RawCode}' target='_blank'>[Driver 5 Raw Code]</a>"
		} else {
			appDriver5RawCode2 = "Not Found"
		}
		if(state.appDriver6RawCode != "NA") {
			appDriver6RawCode2 = "<a href='${state.appDriver6RawCode}' target='_blank'>[Driver 6 Raw Code]</a>"
		} else {
			appDriver6RawCode2 = "Not Found"
		}
		
		getAppNameHandler()
    
	if(driver1Check == "yes" || driver2Check == "yes" || driver3Check == "yes" || driver4Check == "yes" || driver5Check == "yes" || driver6Check == "yes") {	
		if(state.dName != "Example") {
			state.appMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Driver:</i> ${state.appDriver1Name}</td><td width='32%'>${state.appDriver2Name}</td><td width='32%'>${state.appDriver3Name}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Installed</i>: Driver 1: ${state.oldAppDriver1Version}</td><td width='32%'>Driver 2: ${state.oldAppDriver2Version}</td><td width='32%'>Driver 3: ${state.oldAppDriver3Version}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Current</i>:  Driver 1: ${state.appDriver1Version}</td><td width='32%'>Driver 2: ${state.appDriver2Version}</td><td width='32%'>Driver 3: ${state.appDriver3Version}</td></tr>"
			state.appMap += "<tr><td width='36%'>${d1new}${appDriver1RawCode2}</td><td width='32%'>${d2new}${appDriver2RawCode2}</td><td width='32%'>${d3new}${appDriver3RawCode2}</td></tr>"
			if(state.appUpdateNote != "NA") { state.appMap += "<tr><td width='100%' colspan='3' align='left'>Notes: ${state.appUpdateNote}</td></tr>" }
			state.appMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
			
			if(state.oldAppDriver4Version != "NA") {
				state.appMap += "<tr><td width='36%'><i>Driver:</i> ${state.appDriver4Name}</td><td width='32%'>${state.appDriver5Name}</td><td width='32%'>${state.appDriver6Name}</td></tr>"
				state.appMap += "<tr><td width='36%'><i>Installed</i>: Driver 4: ${state.oldAppDriver4Version}</td><td width='32%'>Driver 5: ${state.oldAppDriver5Version}</td><td width='32%'>Driver 6: ${state.oldAppDriver6Version}</td></tr>"
				state.appMap += "<tr><td width='36%'><i>Current</i>:  Driver 4: ${state.appDriver4Version}</td><td width='32%'>Driver 5: ${state.appDriver5Version}</td><td width='32%'>Driver 6: ${state.appDriver6Version}</td></tr>"
				state.appMap += "<tr><td width='36%'>${d4new}${appDriver4RawCode2}</td><td width='32%'>${d5new}${appDriver5RawCode2}</td><td width='32%'>${d6new}${appDriver6RawCode2}</td></tr>"
				state.appMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
			}
			state.appMapDash += "<tr><td>${state.dName}</td></tr>"
			state.appMapPhone += "${state.dName} has an update available \n"
		}
	}
	if(state.dName != "Example") {
		state.appAllMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
		state.appAllMap += "<tr><td width='36%'><i>Driver:</i> ${state.appDriver1Name}</td><td width='32%'>${state.appDriver2Name}</td><td width='32%'>${state.appDriver3Name}</td></tr>"
		state.appAllMap += "<tr><td width='36%'><i>Installed</i>: Driver 1: ${state.oldAppDriver1Version}</td><td width='32%'>Driver 2: ${state.oldAppDriver2Version}</td><td width='32%'>Driver 3: ${state.oldAppDriver3Version}</td></tr>"
		if(state.oldAppDriver4Version != "NA") {
			state.appAllMap += "<tr><td width='36%'><i>Driver:</i> ${state.appDriver4Name}</td><td width='32%'>${state.appDriver5Name}</td><td width='32%'>${state.appDriver6Name}</td></tr>"
			state.appAllMap += "<tr><td width='36%'><i>Installed</i>: Driver 4: ${state.oldAppDriver4Version}</td><td width='32%'>Driver 5: ${state.oldAppDriver5Version}</td><td width='32%'>Driver 6: ${state.oldAppDriver6Version}</td></tr>"
		}
		state.appAllMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
	}
}

def tileHandler(evt) {
	if(state.appMapDash) appMap = "${state.appMapDash}"
	if(state.appMap == "") { 
		appMap = "All Apps are up to date."
	} else {
		appMap = "<table width='100%'><b>Apps/Drivers to update</b><br>${appMap}</table>"
	}
	if(logEnable) log.debug "In tileHandler...Sending new App Watchdog data to ${parent.awDevice} - ${appMap}"
    parent.awDevice.sendDataMap(appMap)
}

def tileVersionHandler() {
	childVersion = "${state.version}"
	verMap = "${app.name}:${childVersion}"
	log.info("In tileVersionHandler...appName: ${app.name}")
    parent.awDevice.sendVersionMap(verMap)
}

def clearMaps() {
	if(logEnable) log.debug "In clearMaps..."
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

def getAppNameHandler() {
    if(logEnable) log.debug "In getAppNameHandler - appsName: ${state.appsName}"
		if(state.appsName == state.app01NoSpace) state.dName = state.app01
		if(state.appsName == state.app02NoSpace) state.dName = state.app02
		if(state.appsName == state.app03NoSpace) state.dName = state.app03
		if(state.appsName == state.app04NoSpace) state.dName = state.app04
		if(state.appsName == state.app05NoSpace) state.dName = state.app05
		if(state.appsName == state.app06NoSpace) state.dName = state.app06
		if(state.appsName == state.app07NoSpace) state.dName = state.app07
		if(state.appsName == state.app08NoSpace) state.dName = state.app08
		if(state.appsName == state.app09NoSpace) state.dName = state.app09
		if(state.appsName == state.app10NoSpace) state.dName = state.app10
		if(state.appsName == state.app11NoSpace) state.dName = state.app11
		if(state.appsName == state.app12NoSpace) state.dName = state.app12
		if(state.appsName == state.app13NoSpace) state.dName = state.app13
		if(state.appsName == state.app14NoSpace) state.dName = state.app14
		if(state.appsName == state.app15NoSpace) state.dName = state.app15
		if(state.appsName == state.app16NoSpace) state.dName = state.app16
		if(state.appsName == state.app17NoSpace) state.dName = state.app17
		if(state.appsName == state.app18NoSpace) state.dName = state.app18
		if(state.appsName == state.app19NoSpace) state.dName = state.app19
		if(state.appsName == state.app20NoSpace) state.dName = state.app20
        if(state.appsName == state.app21NoSpace) state.dName = state.app21
        if(state.appsName == state.app22NoSpace) state.dName = state.app22
        if(state.appsName == state.app23NoSpace) state.dName = state.app23
        if(state.appsName == state.app24NoSpace) state.dName = state.app24
        if(state.appsName == state.app25NoSpace) state.dName = state.app25
    if(logEnable) log.debug "In getAppNameHandler - dName: ${state.dName}"
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
		paragraph "<div style='color:#1A77C9;text-align:center'>App Watchdog 2 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
