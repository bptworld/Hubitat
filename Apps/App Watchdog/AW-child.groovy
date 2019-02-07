/**
 *  ****************  App Watchdog Child ****************
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
 *  V1.0.3 - 02/07/19 - Beta
 *  V1.0.0 - 02/01/19 - Initial Beta release.
 *
 */

def version(){"v1.0.3"}

definition(
    name: "App Watchdog Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "See if any compatible app needs an update, all in one place.",
    category: "",
	parent: "BPTWorld:App Watchdog",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
	page(name: "pageStatus")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>App Watchdog</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "See if any compatible app needs an update, all in one place."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageStatus", title: "App Watchdog Report", description: "Click here to view the App Watchdog Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
			input "gitHubURL", "text", title: "URL for the GitHub to follow", required: true, submitOnChange: true
			if(gitHubURL) {
				gitHubCheck()
				paragraph "${state.gitHubAuthor}"
			}
			paragraph "<b>Beta Limitation:</b> If you need to uncheck an app from this list, Only remove ONE app at a time, then hit 'done', go back in... remove another, hit 'done'. If not you WILL break this app."
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
				[Example:"Must Keep Checked"],
			], required: false, multiple: true, defaultValue: "Example", submitOnChange: true
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			input "timeToRun", "time", title: "Check Apps at this time daily", required: true, submitOnChange: true
			input "isDataDevice", "capability.switch", title: "Turn this device on if there is data to report", submitOnChange: true, required: false, multiple: false
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false, submitOnChange: true
			if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'App Watchdog Tile'<br> - Use our 'App Watchdog Tile' Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your data on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = App Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = appVersions"
			}
		section() {
			input(name: "tileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Maintenance")) {
			paragraph "Once you've updated all of the apps, flip this switch on, a second switch will appear. Turn on the second switch to update ALL version data to Current. Use with caution, it can not be undone. After about 10 seconds you can turn this switch back off."
            input(name: "maintSwitch", type: "bool", defaultValue: "false", title: "Update all versions to current?", description: "Update all Version Data to Current", submitOnChange: "true")
			if(maintSwitch) {
				paragraph "Be sure to turn this switch off after update!"
				input(name: "maintSwitch2", type: "bool", defaultValue: "false", title: "Turn this switch on", description: "Update", submitOnChange: "true")
				appMapHandler()
			}
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
			input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def pageStatus(params) {
	dynamicPage(name: "pageStatus", title: "Apps Watchdog - Status", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		appMapHandler()
			section("How to Update", hideable: true, hidden: true) {
				paragraph "<b>To update your code:</b><br>(<i>Do this for each item that needs updating.</i>)"
				paragraph " - Right-click on any link and choose 'Copy Link Location'<br> - Go into your 'Apps Code' section<br> - Select the corresponding app<br> - 'Import' it in, 'Save' and done!"
				paragraph "<b>To Reset this Data AFTER Updating:</b>"
				paragraph " - Go back into this app<br> - Flip the switch 'Update ALL version data to Current'<br> - Then flip the second switch<br> - All set!"
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

def installed() {
    log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	unschedule()
	logCheck()
	initialize()
}

def initialize() {
	setDefaults()
	schedule(timeToRun, appMapHandler)
}

def setupNewStuff() {
	LOGDEBUG("In setupNewStuff...")
	if(state.oldParentMap == null) {
		state.oldParentMap = [:]
		state.oldChildMap = [:]
		state.oldDriverMap = [:]
	}
	try {
		installedApps.each { stuff -> 
			def stuffing = "NA"
			def oldParentMap = state.oldParentMap.get(stuff)
			LOGDEBUG("In setupNewStuff...Checking to see if ${stuff} is in map...${state.oldParentMap}")
			if(oldParentMap != null) {
				LOGDEBUG("In setupNewStuff...Found ${stuff}! All is good.")
			} else {
				LOGDEBUG("In setupNewStuff...Did not find ${stuff}. Adding it in.")	 
				state.oldParentMap.put(stuff, stuffing)
				state.oldChildMap.put(stuff, stuffing)
				state.oldDriverMap.put(stuff, stuffing)
			}
		}
	}
	catch (e) {
        	//log.error "Error:  $e"
    }
	LOGDEBUG("In setupNewStuff...Here is the oldParentMap: ${state.oldParentMap} **********")
	LOGDEBUG("In setupNewStuff...End New Stuff")
	removingStuff()
}

def removingStuff() {	
	LOGDEBUG("In removingStuff...")
	LOGDEBUG("In removingStuff...Time to Clean up the Maps")
	LOGDEBUG("In removingStuff...Checking Map: ${state.oldParentMap}")
	if(state.oldParentMap) {
		try {
			state.oldParentMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(installedApps.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.oldParentMap.remove(stuff2.key)
					state.oldChildMap.remove(stuff2.key)
					state.oldDriverMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.oldParentMap}")
	} else { LOGDEBUG("In removingStuff...state.oldParentMap was NULL") }
}

def gitHubCheck() {
	def params = [uri: "${gitHubURL}", contentType: "application/json"]
	LOGDEBUG("In gitHubCheck... About to 'try' - ${gitHubURL}")
    try {
		httpGet(params) { response ->
			results = response.data
			state.gitHubAuthor = "GitHub: ${results.GitHubAuthor}"
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
		}
	} 
    catch (e) {
        log.info "Warning:  GitHub URL not found"
		state.gitHubAuthor = "GitHub: Not found"
    }
}

def appMapHandler(evt) {
	LOGDEBUG("In appMapHandler...")
	if(state.enablerSwitch2 == "off") {
		if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
   		if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
			clearMaps()
			LOGDEBUG("In appMapHandler...")
			if(installedApps) {
				installedApps.each { item ->
					state.appsName = item
					LOGDEBUG("----------- Starting App: ${item} -----------")
					
					def params = [uri: "${gitHubURL}", contentType: "application/json"]
					LOGDEBUG("In appMapHandler... About to 'try' - ${item}")
       				try {
						httpGet(params) { response ->
							results = response.data
					
							// Get Current Data from json
							LOGDEBUG("Getting NEW Versions from json")
							state.appParentVersion = results."${item}ParentVersion"
							state.appChildVersion = results."${item}ChildVersion"
							state.appDriverVersion = results."${item}DriverVersion"
							state.appParentRawCode = results."${item}ParentRawCode"
							state.appChildRawCode = results."${item}ChildRawCode"
							state.appDriverRawCode = results."${item}DriverRawCode"
							state.appDiscussion = results."${item}Discussion"
						
							LOGDEBUG("********** GET NEW - P:${state.appParentVersion}, C:${state.appChildVersion}, D:${state.appDriverVersion} **********")
							
							if(maintSwitch == true && maintSwitch2 == true) {
								LOGDEBUG("Maintenance... Replacing Old data with New")
								state.oldParentMap.put(item, state.appParentVersion)
								state.oldChildMap.put(item, state.appChildVersion)
								state.oldDriverMap.put(item, state.appDriverVersion)
								LOGDEBUG("Maintenance... Finished")
								state.buttonPress = "false"
							}
								
							// Get Old Data from map
							state.oldAppParentVersion = state.oldParentMap.get(item)
							state.oldAppChildVersion = state.oldChildMap.get(item)
							state.oldAppDriverVersion = state.oldDriverMap.get(item)
							LOGDEBUG("********** GET OLD - P:${state.oldAppParentVersion}, C:${state.oldAppChildVersion}, D:${state.oldAppDriverVersion} **********")
							
							checkTheData()
						}
					} 
       			 	catch (e) {
        				log.error "Error:  $e"
    				}
					LOGDEBUG("----------- End App: ${item} -----------")		 
				}
			}			
		}
	}
	if(maintSwitch2 != true) {
		if(sendPushMessage) pushNow()
	}
}

def checkTheData() {
	// Parent Check
	LOGDEBUG("Check Parent - old: ${state.oldAppParentVersion} vs. new: ${state.appParentVersion} +++++++++++++++++++++++")
	if(state.oldAppParentVersion == state.appParentVersion){
		parentCheck = "no"
		pnew = ""
		LOGDEBUG("In checkTheData...Old Parent Version: ${state.oldAppParentVersion} - No Update Available - New: ${state.appParentVersion}")
	}
	else {
		parentCheck = "yes"
		pnew = "<span style='color:red'>NEW </span>"
		LOGDEBUG("In checkTheData...Old Parent Version: ${state.oldAppParentVersion} - Update Available! - New: ${state.appParentVersion}")
	}
							
	// Child Check
	if(state.oldAppChildVersion == state.appChildVersion){
		childCheck = "no"
		cnew = ""
		LOGDEBUG("In checkTheData...Old Child Version: ${state.oldAppChildVersion} - No Update Available - New: ${state.appChildVersion}")
	}
	else {
		childCheck = "yes"
		cnew = "<span style='color:red'>NEW </span>"
		LOGDEBUG("In checkTheData...Old Child Version: ${state.oldAppChildVersion} - Update Available! - New: ${state.appChildVersion}")
	}
							
	// Driver Check
	if(state.oldAppDriverVersion == state.appDriverVersion){
		driverCheck = "no"
		dnew = ""
		LOGDEBUG("In checkTheData...Old Driver Version: ${state.oldAppDriverVersion} - No Update Available - New: ${state.appDriverVersion}")
	}
	else {
		driverCheck = "yes"
		dnew = "<span style='color:red'>NEW </span>"
		LOGDEBUG("In checkTheData...Old Driver Version: ${state.oldAppDriverVersion} - Update Available!- New: ${state.appDriverVersion}")
	}

	if(parentCheck == "yes" || childCheck == "yes" || driverCheck == "yes") {
		if(state.appDiscussion != "NA") {
			appDiscussion2 = "<a href='${state.appDiscussion}' target='_blank'>[App Discussion]</a>"
		} else {
			appDiscussion2 = "Not Found"
		}
		if(state.appParentRawCode != "NA") {
			appParentRawCode2 = "<a href='${state.appParentRawCode}' target='_blank'>[Parent Raw Code]</a>"
		} else {
			appParentRawCode2 = "Not Found"
		}	
		if(state.appChildRawCode != "NA") {
			appChildRawCode2 = "<a href='${state.appChildRawCode}' target='_blank'>[Child Raw Code]</a>"
		} else {
			appChildRawCode2 = "Not Found"
		}	
		if(state.appDriverRawCode != "NA") {
			appDriverRawCode2 = "<a href='${state.appDriverRawCode}' target='_blank'>[Driver Raw Code]</a>"
		} else {
			appDriverRawCode2 = "Not Found"
		}	
		def dName = state.appsName
		if(state.appsName == state.app01NoSpace) dName = state.app01
		if(state.appsName == state.app02NoSpace) dName = state.app02
		if(state.appsName == state.app03NoSpace) dName = state.app03
		if(state.appsName == state.app04NoSpace) dName = state.app04
		if(state.appsName == state.app05NoSpace) dName = state.app05
		if(state.appsName == state.app06NoSpace) dName = state.app06
		if(state.appsName == state.app07NoSpace) dName = state.app07
		if(state.appsName == state.app08NoSpace) dName = state.app08
		if(state.appsName == state.app09NoSpace) dName = state.app09
		if(state.appsName == state.app10NoSpace) dName = state.app10
		if(state.appsName == state.app11NoSpace) dName = state.app11
		if(state.appsName == state.app12NoSpace) dName = state.app12
		if(state.appsName == state.app13NoSpace) dName = state.app13
		if(state.appsName == state.app14NoSpace) dName = state.app14
		if(state.appsName == state.app15NoSpace) dName = state.app15
		if(state.appsName == state.app16NoSpace) dName = state.app16
		if(state.appsName == state.app17NoSpace) dName = state.app17
		if(state.appsName == state.app18NoSpace) dName = state.app18
		if(state.appsName == state.app19NoSpace) dName = state.app19
		if(state.appsName == state.app20NoSpace) dName = state.app20
		
		if(dName != "Example") {
			state.appMap += "<tr><td width='75%' colspan='2'><b>${dName}</b></td><td width='25%'>${appDiscussion2}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Installed</i>: Parent: ${state.oldAppParentVersion}</td><td width='32%'>Child: ${state.oldAppChildVersion}</td><td width='32%'>Driver: ${state.oldAppDriverVersion}</td></tr>"
			state.appMap += "<tr><td width='36%'><i>Current</i>:  Parent: ${state.appParentVersion}</td><td width='32%'>Child: ${state.appChildVersion}</td><td width='32%'>Driver: ${state.appDriverVersion}</td></tr>"
			state.appMap += "<tr><td width='36%'>${pnew}${appParentRawCode2}</td><td width='32%'>${cnew}${appChildRawCode2}</td><td width='32%'>${dnew}${appDriverRawCode2}</td></tr>"
			state.appMap += "<tr><td width='100%' colspan='3' align='center'>-</td></tr>"
			
			state.appMapPhone += "${dName} has an update available : "
		}
	}
}

def tileHandler(evt) {
	def appMap = "${state.appMap}"
	LOGDEBUG("In tileHandler...Sending new App Watchdog data to ${tileDevice}")
    tileDevice.sendDataMap(appMap)
}

def clearMaps() {
	LOGDEBUG("In clearMaps...")
	state.appMap = [:]
	state.appMap = ""
	state.appMapPhone = [:]
	state.appMapPhone = ""
	LOGDEBUG("In clearMaps...Maps are clear")
}

def isThereData(){
	LOGDEBUG("In isThereData...")
	if(state.appMapPhone) {
		isDataDevice.on()
	} else {
		isDataDevice.off()
	}
}

def pushNow(){
	LOGDEBUG("In pushNow...")
	if(sendPushMessage) {
		if(state.appMapPhone) {
			pushMessage = "${app.label} - ${state.appMapPhone}"
			LOGDEBUG("In pushNow...Sending message: ${pushMessage}")
        	sendPushMessage.deviceNotification(pushMessage)
		} else {
			if(pushAll == true) {
				log.info "${app.label} - No push needed...Nothing to report."
			} else {
				emptyMapPhone = "${app.label} - Nothing to report."
				LOGDEBUG("In pushNow...Sending message: ${emptyMapPhone}")
        		sendPushMessage.deviceNotification(emptyMapPhone)
			}
		}
	}	
}

// ********** Normal Stuff **********

def pauseOrNot(){			// Modified from @Cobra Code
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            	if(app.label.contains('red')){
                	log.warn "Paused"}
            	else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              		LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            	}
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def setDefaults(){
	setupNewStuff()
	pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
	if(pushAll == null){pushAll = false}
}

def logCheck(){			// Modified from @Cobra Code
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){		// Modified from @Cobra Code
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {				// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){		// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>App Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${version()}</div>"
	}       
} 
