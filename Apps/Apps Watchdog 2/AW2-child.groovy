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
 *  App updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
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
    state.appName = "AppWatchdog2ChildVersion"
	state.version = "v2.0.4"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
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
            paragraph "Note: This will only track apps, not drivers. Thanks"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageAppstoUpdate", title: "App Watchdog Report", description: "Click here to view the App Watchdog Report."
			href "pageCurrent", title: "App Installed Report", description: "Click here to view the App Installed Report."
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
	dynamicPage(name: "pageCurrent", title: "Apps Watchdog - App Versions", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
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
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
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
							// Get Github Data from json
							if(logEnable) log.debug "Getting NEW Versions from json - AppName: ${item} - Type: ${state.aType}"
							if(state.aType == "App") {
								state.appParentVersion = results."${item}ParentVersion"
                                if(!state.appParentVersion) state.appParentVersion = "-"
                                
								state.appChild1Version = results."${item}Child1Version"
                                if(!state.appChild1Version) state.appChild1Version = "-"
                                
                                state.appChild1Name = results."${item}Child1Name"
                                if(!state.appChild1Name) state.appChild1Name = "Child 1"
                                
                                state.appChild2Version = results."${item}Child2Version"
                                if(!state.appChild2Version) state.appChild2Version = "-"
                                
                                state.appChild2Name = results."${item}Child2Name"
                                if(!state.appChild2Name) state.appChild2Name = "Child 2"
                                
                                state.appChild3Version = results."${item}Child3Version"
                                if(!state.appChild3Version) state.appChild3Version = "-"
                                
                                state.appChild3Name = results."${item}Child3Name"
                                if(!state.appChild3Name) state.appChild3Name = "Child 3"
                                
                                state.appChild4Version = results."${item}Child4Version"
                                if(!state.appChild4Version) state.appChild4Version = "-"
                                
                                state.appChild4Name = results."${item}Child4Name"
                                if(!state.appChild4Name) state.appChild4Name = "Child 4"
                                
								state.appParentRawCode = results."${item}ParentRawCode"
                                if(!state.appParentRawCode) state.appParentRawCode = "-"
                                
								state.appChild1RawCode = results."${item}Child1RawCode"
                                if(!state.appChild1RawCode) state.appChild1RawCode = "-"
                                
                                state.appChild2RawCode = results."${item}Child2RawCode"
                                if(!state.appChild2RawCode) state.appChild2RawCode = "-"
                                
                                state.appChild3RawCode = results."${item}Child3Raw3ode"
                                if(!state.appChild3RawCode) state.appChild3RawCode = "-"
                                
                                state.appChild4RawCode = results."${item}Child4RawCode"
                                if(!state.appChild4RawCode) state.appChild4RawCode = "-"
                                
								state.appDiscussion = results."${item}Discussion"
                                if(!state.appDiscussion) state.appDiscussion = "-"
                                
								state.appUpdateNote = results."${item}UpdateNote"
                                if(!state.appUpdateNote) state.appUpdateNote = "-"
                                
                                log.warn "This is what I got FROM JSON- appsName: ${state.appsName} - appParentVersion: ${state.appParentVersion} - appChild1Name: ${state.appChild1Name} - appChild1Version: ${state.appChild1Version}"
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
                                   // if(traceEnable) log.trace "Working on: appName: ${appName} - does it contain - appChild1Name: ${state.appChild1Name}"
                                   // if(traceEnable) log.trace "This is what I got FROM MAP - appName: ${appName} - appVer: ${appVer}"
                                    
                                    dName = state.dName.replace(" ", "")
                                    if(appName.contains("Parent") && appName.contains("${dName}")) state.oldAppParentVersion = appVer
                                    
                    if(appName.contains("Child") && !appName.contains("Child2") && !appName.contains("Child3") && !appName.contains("Child4") 
                       && appName.contains("${dName}")) state.oldAppChild1Version = appVer
                                              
                    if(appName.contains("Child2") && appName.contains("${dName}")) state.oldAppChild2Version = appVer
                    if(appName.contains("Child3") && appName.contains("${dName}")) state.oldAppChild3Version = appVer
                    if(appName.contains("Child3") && appName.contains("${dName}")) state.oldAppChild4Version = appVer
                                    
                                    if(!state.oldAppChild1Version) state.oldAppChild1Version = "-"
                                    if(!state.oldAppChild2Version) state.oldAppChild2Version = "-"
                                    if(!state.oldAppChild3Version) state.oldAppChild3Version = "-"
                                    if(!state.oldAppChild4Version) state.oldAppChild4Version = "-"
                                    
                    if(traceEnable) log.trace "appName: ${appName} - appChild1Name: ${state.appChild1Name} - Does it contain?: ${appName.contains("${state.appChild1Name}".replace(" ", ""))}"
                                    
                                }
                                if(traceEnable) log.trace "*** Sending to checkTheAppData - dName: ${dName} - appChild1Name: ${state.appChild1Name} - oldAppChild1Version: ${state.oldAppChild1Version}"
                                if(state.aType == "App") checkTheAppData()
							   
                                state.oldAppParentVersion = ""
                                state.oldAppChild1Version = ""
                                state.oldAppChild2Version = ""
                                state.oldAppChild3Version = ""
                                state.oldAppChild4Version = ""
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
    } else appChild4RawCode2 = "-"

    getAppNameHandler()
		
	if(parentCheck == "yes" || childCheck == "yes"|| childCheck2 == "yes" || childCheck3 == "yes"|| childCheck4 == "yes"){
		if(state.dName != "Example") {
			state.appMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>${childShortName}: ${state.oldAppChild1Version}</td><td width='32%'> </td></tr>"
			state.appMap += "<tr><td width='36%'><i>Github</i>:  Parent: ${state.appParentVersion}</td><td width='32%'>Child 1: ${state.appChild1Version}</td><td width='32%'> </td></tr>"
			state.appMap += "<tr><td width='36%'>${pnew}${appParentRawCode2}</td><td width='32%'>${cnew}${appChild1RawCode2}</td><td width='32%'> </td></tr>"
            
            if(state.appChild2Version != "-" || state.appChild3Version != "-" || state.appChild4Version != "-" || state.oldAppChild2Version != "-" || state.oldAppChild3Version != "-" || state.oldAppChild4Version != "-") {
                state.appMap += "<tr><td colspan='3'> </td></tr>"
                state.appMap += "<tr><td width='36%'>${child2ShortName}: ${state.oldAppChild2Version}</td><td width='32%'>${child3ShortName}: ${state.oldAppChild3Version}</td><td width='32%'>${child4ShortName}: ${state.oldAppChild4Version}</td></tr>"
			    state.appMap += "<tr><td width='36%'>Child 2: ${state.appChild2Version}</td><td width='32%'>Child 3: ${state.appChild3Version}</td><td width='32%'>Child 4: ${state.appChild4Version}</td></tr>"
                state.appMap += "<tr><td width='36%'>${c2new}${appChild2RawCode2}</td><td width='32%'>${c3new}${appChild3RawCode2}</td><td width='32%'>${c4new}${appChild4RawCode2}</td></tr>"
            }
			if(state.appUpdateNote) { state.appMap += "<tr><td width='100%' colspan='3' align='left'>Notes: ${state.appUpdateNote}</td></tr>" }
			state.appMap += "<tr><td width='100%' colspan='3' align='center'>________________________________________________________________</td></tr>"
			state.appMapDash += "<tr><td>${state.dName}</td></tr>"
			state.appMapPhone += "${state.dName} has an update available \n"
		}
	}
	if(state.dName != "Example") {
		state.appAllMap += "<tr><td width='75%' colspan='2'><b>${state.dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
		state.appAllMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>Child: ${state.oldAppChild1Version}</td><td width='32%'> </td></tr>"
		state.appAllMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
	}
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
        if(state.appsName == state.app26NoSpace) state.dName = state.app26
        if(state.appsName == state.app27NoSpace) state.dName = state.app27
        if(state.appsName == state.app28NoSpace) state.dName = state.app28
        if(state.appsName == state.app29NoSpace) state.dName = state.app29
        if(state.appsName == state.app30NoSpace) state.dName = state.app30   
        if(state.appsName == state.app31NoSpace) state.dName = state.app31
        if(state.appsName == state.app32NoSpace) state.dName = state.app32
        if(state.appsName == state.app33NoSpace) state.dName = state.app33
        if(state.appsName == state.app34NoSpace) state.dName = state.app34
        if(state.appsName == state.app35NoSpace) state.dName = state.app35
        if(state.appsName == state.app36NoSpace) state.dName = state.app36
        if(state.appsName == state.app37NoSpace) state.dName = state.app37
        if(state.appsName == state.app38NoSpace) state.dName = state.app38
        if(state.appsName == state.app39NoSpace) state.dName = state.app39
        if(state.appsName == state.app40NoSpace) state.dName = state.app40
    
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
