/**
 *  ****************  Life360 Tracker Child App  ****************
 *
 *  Design Usage:
 *  Track your Life360 users. Works with the Life360 with States app.
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
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.3.0 - 08/15/19 - Changed trigger back to lastLocationUpdate. Working on Departure delay.
 *  V1.2.9 - 08/11/19 - Fix for 'Places Not Allowed'
 *  V1.2.8 - 08/10/19 - Changes to try and fix 'Track All' issues. Added 'Places Not Allowed' Alerts.
 *  V1.2.7 - 08/06/19 - Changed trigger from lastLocationUpdate to address1. Changed the 'Free' version to use the two places from Life360 (Home, Work) and combine them with 'My Places'. If using the 'free' version be sure to remove any 'my places' that reference Home or Work.
 *  V1.2.6 - 07/31/19 - Found typo in getTimeDiff
 *  V1.2.5 - 07/26/19 - Missed a line with 'On the Move' in 'Track All'
 *  V1.2.4 - 07/25/19 - Fixed typo with 'On the Move' in 'Track All'
 *  V1.2.3 - 07/25/19 - Added user selectable time to report 'On the Move' to 'Track All'
 *  V1.2.2 - 07/24/19 - Added user selectable time to report 'On the Move' to 'Track Specific'
 *  V1.2.1 - 07/23/19 - More and more and more tweaking to get arrived/departed/move to work correctly.
 *  V1.2.0 - 07/22/19 - More adjustments to 'Home'
 *  V1.1.9 - 07/19/19 - Found a big bug with Arrived.
 *  V1.1.8 - 07/19/19 - More tweaking of the 'on the move' code. Added 'Log History when' option, added clickable Map to History.
 *  V1.1.7 - 07/17/19 - Trying to fix 'moving'. Added an option to not announce when a user departs from Home. Also added map to attributes.
 *  V1.1.6 - 07/16/19 - Fix typo with Departed Push notifications, thanks spalexander68
 *  V1.1.5 - 07/16/19 - Added 'Alerts' - Battery Alert: Alert when phone is less than X and not charging.
 *  V1.1.4 - 07/14/19 - Added Home features, Merged code so one app handles both Free and Paid versions. My places now in parent app.
 *  V1.1.3 - 07/12/19 - Added a completly rewritten 'Track All' option back in.
 *  V1.1.2 - 07/12/19 - Removed 'How often to track Places'. Lots of changes to arrived/moving/departed.
 *  V1.1.1 - 07/11/19 - Added code for dashboard tiles, Info and Places tiles. Complete rewrite for arrived/moving/departed. Removed 'track all'.
 *  V1.1.0 - 07/09/19 - Still trying to get push only working correctly!
 *  V1.0.9 - 07/09/19 - Lots of changes to arrived/departed and on the move sections. Fixed push only.
 *  V1.0.8 - 07/09/19 - Another minor change to departed
 *  V1.0.7 - 07/08/19 - Fixed another typo with Departed. Select places now gets the places from the Life360 driver.
 *  V1.0.6 - 07/08/19 - Fix typo speakHasDepated vs speakHasDeparted (thanks spalexander68!)
 *  V1.0.5 - 07/07/19 - First attempt at departure notifications and a few other goodies, please check your child apps
 *  V1.0.4 - 07/05/19 - Complete rewrite of how the app speaks
 *  V1.0.3 - 07/04/19 - Made pushover an option with or without speech, Trying to change up how volume is restored (thanks @doug)
 *  V1.0.2 - 07/04/19 - Added an optional Map link to each push, added Options to turn Speaking on/off, changed/added some descriptions
 *  V1.0.1 - 07/04/19 - Added all attributes as wildcards
 *  V1.0.0 - 07/01/19 - Initial release.
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "Life360TrackerChildVersion"
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
    name: "Life360 Tracker Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track your Life360 users. Works with the Life360 with States app.",
    category: "Convenience",
	parent: "BPTWorld:Life360 Tracker",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Life360%20Tracker/LT-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "alertsConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "placesWarningConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Tracker</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Track your Life360 users. Works with the user Life360 with States app."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Type")) {
			input "lifeVersion", "enum", title: "Life360 Version", options: ["Free","Paid"], required: true, submitOnChange: "true"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Device")) {
			input "presenceDevice", "capability.presenceSensor", title: "Select Life360 User Device", required: true
            input "friendlyName", "text", title: "Friendly Name used in messages for this Device", required: true, submitOnChange: "true"
		}
    if(presenceDevice) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Place Tracking")) {
            paragraph "This will track the coming and going of places."
            input "trackingOptions", "enum", title: "How to track Places", options: ["Track All","Track Specific"], required: true, submitOnChange: "true"
            if(trackingOptions == "Track Specific") {
                if(lifeVersion == "Free") {
                    paragraph "This will combine your two Places from Life360 (Home,Work) and 'My Places' created in the Life360 Parent app."
                    state.lPlaces = presenceDevice.currentValue("savedPlaces").replace("[","").replace("]","").replace(" ,", ",").replace(", ", ",")
                    buildMyPlacesList()
                    state.allPlaces = [state.lPlaces, state.myPlacesList].flatten().findAll{it} 
                    state.thePlaces = "${state.allPlaces}".replace("[","").replace("]","").replace("]","").replace(" ,", ",").replace(", ", ",")
                    //if(logEnable) log.debug "In pageConfig - Free - thePlaces: ${state.thePlaces}"
                } else {
                    paragraph "This list will show all of your 'Starred' places from the Life360 app."
                    state.thePlaces = presenceDevice.currentValue("savedPlaces").replace("[","").replace("]","").replace(" ,", ",").replace(", ", ",")
                    //if(logEnable) log.debug "In pageConfig - Paid - thePlaces: ${state.thePlaces}"
                }  
                state.values = "${state.thePlaces}".split(",")
                //if(logEnable) log.debug "In pageConfig - values: ${state.values}"
                input "trackSpecific", "enum", title:"Life360 Places", options: state.values, multiple: true, required:true, submitOnChange: true
                input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of Specific Places?", description: "List View", submitOnChange: "true")
                if(oG1List) {
                    def valuesG1 = "${trackSpecific}".split(",")
			        listMapG1 = ""
    			    valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
				    paragraph "${listMapG1}".replace("[","").replace("]","")
                }
            }
            if(trackingOptions == "Track All") {
                paragraph "Tracking all places"
            }
            input "timeConsideredHere", "number", title: "Time to be considered at a Place (in Minutes, range 2 to 10)", required: true, submitOnChange: true, defaultValue: 2, range: '2..10'
        }
        
// *** Home ***            
        section(getFormat("header-green", "${getImage("Blank")}"+" Home")) {    
            paragraph "'Home' is a unique place in Life360 and with Automation. For most us we want things to happen as soon as possible.  With this app, one needs to be at a Place for at least 2 minutes before it will trigger things. This is to stop false alarms and to be sure we are not just passing by.  But with 'Home' sometimes things should work different..."
            input(name: "homeDelay", type: "bool", defaultValue: "false", title: "Should Tracker announce when you arrive at 'Home', after the 2 minute wait? (off='No', on='Yes')", description: "Home Wait", submitOnChange: "true")
            paragraph "<small>This is useful if you have another app announcing when you are home, like 'Welcome Home'</small>"
            input(name: "homeNow", type: "bool", defaultValue: "false", title: "Should Tracker announce when the User arrives at 'Home', with NO wait? (off='No', on='Yes')", description: "Home Instant", submitOnChange: "true")
            paragraph "<small>This will give a nice heads up that someone is home. But can be a false alarm if they are just driving by.</small>"
            input(name: "homeDepartedDelayed", type: "bool", defaultValue: "false", title: "Should Tracker announce when the User departs from 'Home', after a 2 minute delay? (off='No', on='Yes') ** Needs Testing **", description: "Delayed Home Depart", submitOnChange: "true")
            paragraph "<small>This will give a heads up that someone has departed home. But help with false announcements.</small>"
            input(name: "homeDeparted", type: "bool", defaultValue: "false", title: "Should Tracker announce right away, when the User departs from 'Home'? (off='No', on='Yes')", description: "Home Depart", submitOnChange: "true")
            paragraph "<small>This will give a heads up that someone has departed home. As soon as it is detected.</small>"
            paragraph "Note: Home options will only work if you have the Speak or Push when 'Has arrived'and/or 'Has departed' switch in Message Options turned on."
        }
// *** End Home ***
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a device<br>%place% - returns the current place<br>%lastplace% - returns the place departed"
            paragraph "* PLUS - all attribute names can be used as wildcards! Just make sure the name is exact, capitalization counts!  ie. %powerSource%, %distanceMiles% or %wifiState%"
            paragraph "<b>'Has Arrived'</b>"
            input(name: "speakHasArrived", type: "bool", defaultValue: "false", title: "Speak when", description: "Speak Has Arrived", width: 4, submitOnChange: true)
            input(name: "pushHasArrived", type: "bool", defaultValue: "false", title: "Push when", description: "Push Has Arrived", width: 4, submitOnChange: true)
            input(name: "historyHasArrived", type: "bool", defaultValue: "false", title: "Log History when", description: "History Has arrived", width: 4, submitOnChange: true)
			if(speakHasArrived || pushHasArrived || historyHasArrived) input "messageAT", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has arrived at %place%"
			if(speakHasArrived || pushHasArrived || historyHasArrived) input(name: "atMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if((speakHasArrived || pushHasArrived || historyHasArrived) && atMsgList) {
				def values = "${messageAT}".split(";")
				listMapAT = ""
    			values.each { item -> listMapAT += "${item}<br>"}
				paragraph "${listMapAT}"
			}
            if(speakHasArrived || pushHasArrived || historyHasArrived) paragraph "<hr>"
            
            paragraph "<b>'Has Departed'</b>"
            input(name: "speakHasDeparted", type: "bool", defaultValue: "false", title: "Speak when", description: "Speak Has departed", width: 4, submitOnChange: true)
            input(name: "pushHasDeparted", type: "bool", defaultValue: "false", title: "Push when", description: "Push Has departed", width: 4, submitOnChange: true)
            input(name: "historyHasDeparted", type: "bool", defaultValue: "false", title: "Log History when", description: "History Has departed", width: 4, submitOnChange: true)
			if(speakHasDeparted || pushHasDeparted || historyHasDeparted) input "messageDEP", "text", title: "Random Message to be spoken when <b>'has departed'</b> a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has departed from %place%"
			if(speakHasDeparted || pushHasDeparted || historyHasDeparted) input(name: "depMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if((speakHasDeparted || pushHasDeparted || historyHasDeparted) && depMsgList) {
				def values = "${messageDEP}".split(";")
				listMapDEP = ""
    			values.each { item -> listMapDEP += "${item}<br>"}
                paragraph "${listMapDEP}"
			}
            if(speakHasDeparted || pushHasDeparted || historyHasDeparted) paragraph "<hr>"
            
            paragraph "<b>'On the Move'</b>"
            input(name: "speakOnTheMove", type: "bool", defaultValue: "false", title: "Speak when", description: "Speak On the Move", width: 4, submitOnChange: true)
            input(name: "pushOnTheMove", type: "bool", defaultValue: "false", title: "Push when", description: "Push On the Move", width: 4, submitOnChange: true)
            input(name: "historyOnTheMove", type: "bool", defaultValue: "false", title: "Log History when", description: "History On the Move", width: 4, submitOnChange: true)
            if(speakOnTheMove || pushOnTheMove || historyOnTheMove) input "timeMove", "number", title: "How often to report 'On the Move' (in Minutes, range 1 to 30)", required: true, submitOnChange: true, defaultValue: 5, range: '1..30'
            if(speakOnTheMove || pushOnTheMove || historyOnTheMove) input "messageMOVE", "text", title: "Random Message to be spoken when <b>'on the move'</b> near a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% is on the move near %place%"
			if(speakOnTheMove || pushOnTheMove || historyOnTheMove) input(name: "moveMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if((speakOnTheMove || pushOnTheMove || historyOnTheMove) && moveMsgList) {
				def values = "${messageMOVE}".split(";")
				listMapMove = ""
    			values.each { item -> listMapMove += "${item}<br>"}
				paragraph "${listMapMove}"
			}
            paragraph "<hr>"
            input(name: "historyMap", type: "bool", defaultValue: "false", title: "Add Map Link to History message?", description: "History Map", submitOnChange: true)
            paragraph "<small>This will put a clickable map link on the dashboard history tile for each place.</small>"
            if(speakOnTheMove || pushOnTheMove || historyOnTheMove) paragraph "<hr>"
        }
        
        
        if(pushHasArrived || pushHasDeparted || pushOnTheMove) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Options")) { 
                input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
                if(sendPushMessage && (pushHasArrived || pushHasDeparted || pushOnTheMove)) input(name: "linkPush", type: "bool", defaultValue: "false", title: "Send Map Link with Push", description: "Send Google Maps Link")
            }
		}
        if(speakHasArrived || speakHasDeparted || speakOnTheMove) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
                paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
              	input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
         		input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
                input(name: "speakerProxy", type: "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy")
          	}
		    section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		    	paragraph "NOTE: Not all speakers can use volume controls. Please click the button to test your selected speakers. Then check your logs to see how they did.", width:8
                input "testSpeaker", "button", title: "Test Speaker", submitOnChange: true, width: 4
                paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true, width: 6
		        input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true, width: 6
                input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required: false, submitOnChange: true
			    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true, width: 6
    		    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true, width: 6
		    }
			section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
        		input "fromTime", "time", title: "From", required: false, width: 6
        		input "toTime", "time", title: "To", required: false, width: 6
			}
    	}
		section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            input "isDataDevice", "capability.switch", title: "Turn this device on/off (On = at place, Off = moving)", required: false, multiple: false
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Extra Options")) {           
            href "alertsConfig", title: "Alerts", description: "Phone Battery - Places Not Allowed"
		}
/**
        section(getFormat("header-green", "${getImage("Blank")}"+" App Watchdog")) {
			paragraph "This app supports Device Watchdog."
            input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Send this apps version data to App Watchdog?", description: "Update App Watchdog", submitOnChange: "true")
            if(sendToAWSwitch) input(name: "awDevice", type: "capability.actuator", title: "Vitual Device created to send the AW Data to:", submitOnChange: true, required: true, multiple: false)
			if(sendToAWSwitch && awDevice) setVersion()
        }
*/
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
    }
		display2()
	}
}

def alertsConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Tracker - Alerts</h2>", install: false, uninstall:false) {
		display() 
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Alerts")) {
            paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a device<br>%place% - returns the place arrived or departed"
            paragraph "* PLUS - all attribute names can be used as wildcards! Just make sure the name is exact, capitalization counts!  ie. %powerSource%, %distanceMiles% or %wifiState%"
            paragraph "<hr>"
            paragraph "<b>Battery Alert</b><br>If Battery is too low and not Charging, send Alert"
            input "alertBatt", "number", title: "At what percentage of battery life to Alert (range 0 to 100)", description: "0-100", required: false, range: '0..100'
            input(name: "speakAlertsBatt", type: "bool", defaultValue: "false", title: "Speak on Alert", description: "Speak On Alert", width:6)
            input(name: "pushAlertsBatt", type: "bool", defaultValue: "false", title: "Push on Alert", description: "Push On Alert", width:6)
            input "messageAlertBatt", "text", title: "Random Message to be spoken when <b>'Alert - Battery'</b> - Separate each message with <b>;</b> (semicolon)",  required: true, defaultValue: "%name%'s phone battery (%battery%) needs charging"
            paragraph "<hr>"
            paragraph "<b>Places Warning</b> - Sometimes there are Places one should not go."
            input "trackSpecific2", "enum", title:"Warn when at these Places", options: state.values, multiple: true, required:false
            input(name: "speakHasArrived2", type: "bool", defaultValue: "false", title: "Speak when 'Has arrived'", description: "Speak Has Arrived", width: 4)
            input(name: "pushHasArrived2", type: "bool", defaultValue: "false", title: "Push when 'Has arrived'", description: "Push Has Arrived", width: 4)
            input(name: "historyHasArrived2", type: "bool", defaultValue: "false", title: "Log History when 'Has arrived'", description: "History Has arrived", width: 4)
			input "messageAT2", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)", required: true, defaultValue: "%name% has arrived at %place% but should NOT be there"
            
			input(name: "speakHasDeparted2", type: "bool", defaultValue: "false", title: "Speak when 'Has departed'", description: "Speak Has departed", width: 4)
            input(name: "pushHasDeparted2", type: "bool", defaultValue: "false", title: "Push when 'Has departed'", description: "Push Has departed", width: 4)
            input(name: "historyHasDeparted2", type: "bool", defaultValue: "false", title: "Log History when 'Has departed'", description: "History Has departed", width: 4)
			input "messageDEP2", "text", title: "Random Message to be spoken when <b>'has departed'</b> at a place - Separate each message with <b>;</b> (semicolon)", required: true, defaultValue: "%name% has departed %place% but should NOT have been there"
            paragraph "<hr>"
		}
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	updated()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
	initialize()
}

def initialize() {
    setDefaults()
    if(lifeVersion == "Paid") subscribe(presenceDevice, "lastLocationUpdate", userHandler)
    if(lifeVersion == "Free") subscribe(presenceDevice, "lastLocationUpdate", whereAmI)
}

def userHandler(evt) {
    if(logEnable) log.debug "---------- Start Log - Life360 Tracker Child - App version: ${state.version} (${lifeVersion}) ----------"
    if(trackSpecific2 == null){trackSpecific2 = "None selected"}
    state.address1Value = presenceDevice.currentValue("address1")
    if(logEnable) log.debug "In userHandler - address1Value: ${state.address1Value} - prevPlace: ${state.prevPlace} - beenHere: ${state.beenHere} - version: ${state.version}"
    if(lifeVersion == "Paid") alertBattHandler()

    if(state.address1Value == "Home" || state.prevPlace == "Home" || state.delayDepHome == "yes") {
        if(logEnable) log.debug "In UserHandler (${state.version}) - Going to trackHomeHandler  *******" 
        trackHomeHandler()
    } else if(trackSpecific2.contains(state.address1Value)) {
        if(logEnable) log.debug "In UserHandler (${state.version}) - Going to placeNotAllowedHandler  *******"
        placeNotAllowedHandler()
    } else {
        if(logEnable) log.debug "In UserHandler (${state.version}) - Going to track All/Specific Handler  *******"
        if(trackingOptions == "Track All") { trackAllHandler() }
        if(trackingOptions == "Track Specific") { trackSpecificHandler() }
    }

    if(logEnable) log.debug "---------- End Log - Life360 Tracker Child - App version: ${state.version} (${lifeVersion}) ----------"
}

// *** Track All ***

def trackAllHandler() {
    if(logEnable) log.debug "In trackAllHandler (${state.version})"
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    int timeMoving = timeMove * 60
    // reset speaking
    state.speakAT = "no"
    state.speakDEP = "no"
    state.speakMOVE = "no"
    state.msg = ""
    
    if(logEnable) log.debug "In trackAllHandler  (${state.version}) - ${friendlyName} is at ${state.address1Value}"
    if(state.address1Value == state.prevPlace) {
        if(state.tDiff >= timeHere) {
            if(logEnable) log.debug "In trackAllHandler (${state.version}) - Time at Place: ${state.tDiff} IS greater than: ${timeHere} - beenHere: ${state.beenHere}"
            if(state.beenHere == "no") {
                if(logEnable) log.debug "In trackAllHandler (${state.version}) - ${friendlyName} has arrived at ${state.address1Value}"
                state.msg = "${messageAT}"
                state.speakAT = "yes"
                state.lastAtPlace = state.address1Value
                messageHandler()
            } else {
                if(logEnable) log.debug "In trackAllHandler (${state.version}) - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs - beenHere: ${state.beenHere}"
            }
            if(isDataDevice) isDataDevice.on()
            state.prevPlace = state.address1Value
            state.lastAtPlace = state.address1Value
            state.beenHere = "yes"
            if(logEnable) log.debug "In trackAllHandler (${state.version}) - state.tDiff > timeHere - TRUE - beenHere: ${state.beenHere}"
        } else {  // ***  state.tDiff is NOT GREATER THAN timeHere ***
            if(logEnable) log.debug "In trackAllHandler - Time at Place: ${state.tDiff} IS NOT greater than: ${timeHere} - beenHere: ${state.beenHere}"
            if(isDataDevice) isDataDevice.off()
            state.prevPlace = state.address1Value
            state.beenHere = "no"
            if(logEnable) log.debug "In trackAllHandler (${state.version}) - state.tDiff > timeHere - FALSE - beenHere: ${state.beenHere}"
        }
    } else {  // *** ! state.address1Value == state.prevPlace ***
        if(logEnable) log.debug "In trackAllHandler (${state.version}) - address1: ${state.address1Value} DOES NOT MATCH state.prevPlace: ${state.prevPlace} - beenHere: ${state.beenHere}"
        if(state.beenHere == "yes") {
            if(logEnable) log.debug "In trackAllHandler (${state.version}) - ${friendlyName} has departed from ${state.lastAtPlace}"
            state.msg = "${messageDEP}"
            state.speakDEP = "yes"
            state.justDEP = "yes"
            messageHandler()
        } else {
            if(state.sMove == null) {
                def now = new Date()
                long startMove = now.getTime()
                state.sMove = startMove
                if(logEnable) log.debug "In trackAllHandler (${state.version}) - Time Moving: ${now} - sMove: ${state.sMove}"
            }
            if(state.justDEP == "no") {
                getTimeMoving()
                if(state.mDiff >= timeMoving) {
                    if(logEnable) log.debug "In trackAllHandler (${state.version}) - ${friendlyName} is on the move near ${state.address1Value}"
                    state.msg = "${messageMOVE}"
                    state.speakMOVE = "yes"
                    messageHandler()
                    state.beenHere = "no"
                    def now = new Date()
                    long startMove = now.getTime()
                    state.sMove = startMove
                    if(logEnable) log.debug "In trackAllHandler (${state.version}) - Time Moving: ${now} - sMove: ${state.sMove}"
                } else {
                    if(logEnable) log.debug "In trackAllHandler (${state.version}) - ${friendlyName} has been on the move less than ${timeMove} minutes but is near ${state.address1Value}"   
                }
            } else {
                state.justDEP = "no"
                if(logEnable) log.debug "In trackAllHandler (${state.version}) - ${friendlyName} has just departed so skipping first move notice, but is near ${state.address1Value}"
            }
        }
        state.prevPlace = state.address1Value
        if(isDataDevice) isDataDevice.off()
        state.beenHere = "no"
        if(logEnable) log.debug "In trackAllHandler (${state.version}) - End of Departed/Move - beenHere: ${state.beenHere}"
    }
}

// *** Track Home ***

def trackHomeHandler() {
    if(logEnable) log.debug "In trackHomeHandler (${state.version})"
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    int timeMoving = timeMove * 60
    // reset speaking
    state.speakAT = "no"
    state.speakDEP = "no"
    state.speakMOVE = "no"
    state.msg = ""
    
    if(state.address1Value == "Home") {
        if((homeNow) && (state.beenHere == "no")) {
            if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Home Now (beenHere: ${state.beenHere}) - ${friendlyName} has arrived at ${state.address1Value}"
            state.msg = "${messageAT}"
            state.speakAT = "yes"
            messageHandler()

            if(isDataDevice) isDataDevice.on()
            state.beenHere = "yes"
            state.lastAtPlace = state.address1Value
        } else {
            if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Home Now (beenHere: ${state.beenHere}) - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
        }
        
        if((homeDelay) && (state.tDiff > timeHere) && (state.beenHere == "no")) {
            if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Home Delay (beenHere: ${state.beenHere}) - ${friendlyName} has arrived at ${state.address1Value}"
            state.msg = "${messageAT}"
            state.speakAT = "yes"
            messageHandler()

            if(isDataDevice) isDataDevice.on()
            state.beenHere = "yes"
            state.lastAtPlace = state.address1Value
        } else {
            if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Home Delay (beenHere: ${state.beenHere}) - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
        }
    }
    
    if(state.address1Value != "Home") {
        if((homeDeparted) && (state.prevPlace == "Home") && (state.beenHere == "yes")) {
            if(homeDeparted) {
                if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Home Departed - ${friendlyName} has departed from ${state.prevPlace}"
                state.msg = "${messageDEP}"
                state.justDEP = "yes"
                state.speakDEP = "yes"
                messageHandler()
                state.beenHere = "no"
            }
        } else if(homeDepartedDelay) {
            // Announce AFTER 2 minutes has elapsed
            if(state.sMove == null) {
                def now = new Date()
                long startMove = now.getTime()
                state.sMove = startMove
                if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Time Moving: ${now} - sMove: ${state.sMove}"
            }
             
            getTimeMoving()
            if(state.mDiff >= 2) {
                if(logEnable) log.debug "In trackHomeHandler (${state.version}) - ${friendlyName} has departed Home over 2 minutes ago and is on the move near ${state.address1Value}"
                state.msg = "${messageDEP}"
                state.justDEP = "yes"
                state.speakDEP = "yes"
                messageHandler()
                state.beenHere = "no"
                state.delayDepHome = "no"
            } else {
                if(logEnable) log.debug "In trackHomeHandler (${state.version}) - ${friendlyName} departed Home less than 2 minutes ago but is near ${state.address1Value}"  
                state.delayDepHome = "yes"
            }
        } else {
           if(logEnable) log.debug "In trackHomeHandler (${state.version}) - Home Departed (beenHere: ${state.beenHere}) - Previous Place: ${state.prevPlace} - Home Departed: ${homeDeparted} - No announcement needed"
           state.justDEP = "no"
           state.delayDepHome = "no"
        }
    }
    state.prevPlace = state.address1Value
}

// *** Track placeNotAllowed ***

def placeNotAllowedHandler() {
    if(logEnable) log.debug "In placeNotAllowedHandler (${state.version})"
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    int timeMoving = timeMove * 60
    // reset speaking
    state.speakAT = "no"
    state.speakDEP = "no"
    state.speakMOVE = "no"
    state.msg = ""
    
    if(state.address1Value == state.prevPlace) {
        if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - address1: ${state.address1Value} MATCHES state.prevPlace: ${state.prevPlace}"
        if(trackSpecific2.contains(state.address1Value)) {
            if(state.tDiff >= timeHere) {
                if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - Time at Place: ${state.tDiff} IS greater than: ${timeHere} - beenHere: ${state.beenHere}"
                if(state.beenHere == "no") {
                    if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - ${friendlyName} has arrived at ${state.address1Value}"
                    state.msg = "${messageAT2}"
                    state.speakAT = "yes"
                    messageHandler()
                    
                    if(isDataDevice) isDataDevice.on()
                    state.beenHere = "yes"
                } else {
                    if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                }
                if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - END - state.tDiff > timeHere - TRUE - beenHere: ${state.beenHere}"
            } else {  // *** state.tDiff is NOT GREATER THAN timeHere ***
                if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - Time at Place: ${state.tDiff} IS NOT greater than: ${timeHere} - address1Value: ${state.address1Value} - lastAtPlace: ${state.lastAtPlace} - beenHere: ${state.beenHere}"
                if(isDataDevice) isDataDevice.off()
                state.beenHere = "no"
                state.lastAtPlace = "${state.address1Value}"
                if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - END - state.tDiff > timeHere - FALSE - beenHere: ${state.beenHere}"
            }
        } else {  // *** ! trackSpecific.contains(state.address1Value) ***
		    if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - ${friendlyName} is not at a place this app is tracking ${state.address1Value}"
            if(isDataDevice) isDataDevice.off()
            state.beenHere = "no"
            if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - END - trackSpecific.contains(state.address1Value) - FALSE - beenHere: ${state.beenHere}"
        }
    } else {  // ***  state.address1Value DOES NOT EQUAL state.prevPlace ***
        if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - address1: ${state.address1Value} DOES NOT MATCH state.prevPlace: ${state.prevPlace}"
        if(state.beenHere == "yes") {
            if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - ${friendlyName} has departed from ${state.lastAtPlace}"
            state.msg = "${messageDEP2}"
            state.speakDEP = "yes"
            state.justDEP = "yes"
            messageHandler()
            state.beenHere = "no"
        }
        
        state.prevPlace = state.address1Value
        state.lastAtPlace = state.address1Value
        if(isDataDevice) isDataDevice.off()
        if(logEnable) log.debug "In placeNotAllowedHandler (${state.version}) - END - trackSpecific2.contains(state.address1Value) - Departed - beenHere: ${state.beenHere}"
    } 
}

// *** Track Specific ***

def trackSpecificHandler() {
    if(logEnable) log.debug "In trackSpecificHandler (${state.version})"
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    int timeMoving = timeMove * 60
    // reset speaking
    state.speakAT = "no"
    state.speakDEP = "no"
    state.speakMOVE = "no"
    state.msg = ""
    
    if(state.address1Value == state.prevPlace) {
        if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - address1: ${state.address1Value} MATCHES state.prevPlace: ${state.prevPlace}"
        if(trackSpecific.contains(state.address1Value)) {
            if(state.tDiff >= timeHere) {
                if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Time at Place: ${state.tDiff} IS greater than: ${timeHere} - beenHere: ${state.beenHere}"
                if(state.beenHere == "no") {
                    if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Track Specific - ${friendlyName} has arrived at ${state.address1Value}"
                    state.msg = "${messageAT}"
                    state.speakAT = "yes"
                    messageHandler()
                    
                    if(isDataDevice) isDataDevice.on()
                    state.beenHere = "yes"
                } else {
                    if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Track Specific - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                }
                if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - END - state.tDiff > timeHere - TRUE - beenHere: ${state.beenHere}"
            } else {  // *** state.tDiff is NOT GREATER THAN timeHere ***
                if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Time at Place: ${state.tDiff} IS NOT greater than: ${timeHere} - address1Value: ${state.address1Value} - lastAtPlace: ${state.lastAtPlace} - beenHere: ${state.beenHere}"
                if(isDataDevice) isDataDevice.off()
                state.beenHere = "no"
                state.lastAtPlace = "${state.address1Value}"
                if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - END - state.tDiff > timeHere - FALSE - beenHere: ${state.beenHere}"
            }
        } else {  // *** ! trackSpecific.contains(state.address1Value) ***
		    if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Track Specific - ${friendlyName} is not at a place this app is tracking ${state.address1Value}"
            if(isDataDevice) isDataDevice.off()
            state.beenHere = "no"
            if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - END - trackSpecific.contains(state.address1Value) - FALSE - beenHere: ${state.beenHere}"
        }
    } else {  // ***  state.address1Value DOES NOT EQUAL state.prevPlace ***
        if(state.delayDepHome == "yes"){
            trackHomeHandler()
        } else {
            
        if(logEnable) log.debug "In trackSpecificHandler - address1: ${state.address1Value} DOES NOT MATCH state.prevPlace: ${state.prevPlace}"
        if(state.beenHere == "yes") {
            if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - ${friendlyName} has departed from ${state.lastAtPlace}"
                state.msg = "${messageDEP}"
                state.speakDEP = "yes"
                state.justDEP = "yes"
                messageHandler()
                state.beenHere = "no"
        } else {
            if(state.sMove == null) {
                def now = new Date()
                long startMove = now.getTime()
                state.sMove = startMove
                if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Time Moving: ${now} - sMove: ${state.sMove}"
            }
            if(state.justDEP == "no") {
                getTimeMoving()
                if(state.mDiff >= timeMoving) {
                    if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - ${friendlyName} is on the move near ${state.address1Value}"
                    state.msg = "${messageMOVE}"
                    state.speakMOVE = "yes"
                    messageHandler()
                    state.beenHere = "no"
                    def now = new Date()
                    long startMove = now.getTime()
                    state.sMove = startMove
                    if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - Time Moving: ${now} - sMove: ${state.sMove}"
                } else {
                    if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - ${friendlyName} has been on the move less than ${timeMove} minutes but is near ${state.address1Value}"   
                }
             } else {
                state.justDEP = "no"
                if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - ${friendlyName} has just departed so skipping first move notice, but is near ${state.address1Value}"
            }   
        }
        
        state.prevPlace = state.address1Value
        state.lastAtPlace = state.address1Value
        if(isDataDevice) isDataDevice.off()
        if(logEnable) log.debug "In trackSpecificHandler (${state.version}) - END - trackSpecific.contains(state.address1Value) - Departed/Move - beenHere: ${state.beenHere}"
            
        }
    } 
}

def alertBattHandler() {
    if(logEnable) log.debug "In alertBattHandler (${state.version})"
    state.battery = presenceDevice.currentValue("battery")
    state.charge = presenceDevice.currentValue("charge")
    
    if(logEnable) log.debug "In alertBattHandler (${state.version}) - battery: ${state.battery} - prev battery: ${state.prevBatt} - charge: ${state.charge} - alertBattRepeat: ${state.alertBattRepeat}"
    
    if((state.battery <= alertBatt) && (state.charge == "false") && (state.alertBattRepeat != "yes")) {
        if(logEnable) log.debug "In alertBattHandler (${state.version}) - battery (${state.battery}) needs charging! - Step 1 - battery: ${state.battery} <= Prev: ${state.prevBatt}"
        state.msg = "${messageAlertBatt}"
        state.speakAT = "yes"
        state.prevBatt = state.battery - 10
        state.alerts = "yes"
        state.alertBattRepeat = "yes"
        messageHandler()
    } else if((state.battery <= state.prevBatt) && (state.charge == "false")) {
        if(logEnable) log.debug "In alertBattHandler (${state.version}) - battery (${state.battery}) needs charging! - Step 2 - battery: ${state.battery} <= Prev: ${state.prevBatt}"
        state.msg = "${messageAlertBatt}"
        state.prevBatt = state.prevBatt - 10
        state.alertBattRepeat = "yes"
        state.alerts = "yes"
        messageHandler()
    } else if(state.charge == "true") {
        if(logEnable) log.debug "In alertBattHandler (${state.version}) - battery (${state.battery}) is charging. - Step 3"
        state.alertBattRepeat = "no"
        state.prevBatt = 0
    }  
    state.alerts = "no"
}

def getTimeDiff() {
	if(logEnable) log.debug "In getTimeDiff (${state.version}) "
	long since = presenceDevice.currentValue("since")
   	def now = new Date()
    long unxNow = now.getTime()
    unxNow = unxNow/1000    
    long timeDiff = Math.abs(unxNow-since)
    state.tDiff = timeDiff
    if(logEnable) log.debug "In getTimeDiff (${state.version}) - since: ${since}, Now: ${unxNow}, Diff: ${timeDiff}"
    
	state.timeDay = (timeDiff / 86400).toInteger()
    state.timeHrs = ((timeDiff % 86400 ) / 3600).toInteger()
	state.timeMin = (((timeDiff % 86400 ) % 3600 ) / 60).toInteger()
	state.timeSec = (((timeDiff % 86400 ) % 3600 ) % 60).toInteger()
    
    if(logEnable) log.debug "In getTimeDiff (${state.version}) - Time Diff: ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
}

def getTimeMoving() {
	if(logEnable) log.debug "In getTimeMoving (${state.version})"
   	def now = new Date()
    long unxNow = now.getTime()
    long unxPrev = state.sMove
    unxNow = unxNow/1000
    unxPrev = unxPrev/1000
    long timeDiff = Math.abs(unxNow-unxPrev)
    state.mDiff = timeDiff
    if(logEnable) log.debug "In getTimeMoving (${state.version}) - prev: ${unxPrev}, Now: ${unxNow}, Diff: ${timeDiff}"
    
	state.mTimeDay = (timeDiff / 86400).toInteger()
    state.mTimeHrs = ((timeDiff % 86400 ) / 3600).toInteger()
	state.mTimeMin = (((timeDiff % 86400 ) % 3600 ) / 60).toInteger()
	state.mTimeSec = (((timeDiff % 86400 ) % 3600 ) % 60).toInteger()
    
    if(logEnable) log.debug "In getTimeMoving (${state.version}) - Time Diff: ${state.mTimeDay} days, ${state.mTimeHrs} hrs, ${state.mTimeMin} mins & ${state.mTimeSec} secs"
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk (${state.version})"
	checkTime()
	checkVol()
	atomicState.randomPause = Math.abs(new Random().nextInt() % 1500) + 400
	if(logEnable) log.debug "In letsTalk (${state.version}) - pause: ${atomicState.randomPause}"
	pauseExecution(atomicState.randomPause)
	if(logEnable) log.debug "In letsTalk (${state.version}) - continuing"
	if(state.timeBetween == true) {
		state.theMsg = "${state.theMessage}"
    	if(logEnable) log.debug "In letsTalk (${state.version}) - speaker: ${speaker}, vol: ${state.volume}, msg: ${state.theMsg}, volRestore: ${volRestore}"
        speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
        atomicState.speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
            state.speakers.each {
                if(logEnable) log.debug "Speaker in use: ${it}"
                if(speakerProxy) {
                    if(logEnable) log.debug "In letsTalk (${state.version}) - speakerProxy - ${it}"
                    it.speak(state.theMsg)
                } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                    if(logEnable) log.debug "In letsTalk (${state.version}) - setVolumeSpeakAndRestore - ${it}"
                    def prevVolume = it.currentValue("volume")
                    it.setVolumeSpeakAndRestore(state.volume, state.theMsg, prevVolume)
                } else if(it.hasCommand('playTextAndRestore')) {   
                    if(logEnable) log.debug "In letsTalk (${state.version}) - playTextAndRestore - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    def prevVolume = it.currentValue("volume")
                    it.playTextAndRestore(state.theMsg, prevVolume)
                } else {		        
                    if(logEnable) log.debug "In letsTalk (${state.version}) - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    it.speak(state.theMsg)
                    pauseExecution(atomicState.speechDuration2)
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                    if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
                }
            }
        pauseExecution(atomicState.speechDuration2)
        state.canSpeak = "no"
	    if(logEnable) log.debug "In letsTalk (${state.version}) - that's it!"  
		log.info "${app.label} - ${state.theMsg}"
	} else {
		if(logEnable) log.debug "In letsTalk (${state.version}) - Messages not allowed at this time"
	}
}

def checkVol(){
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol (${state.version}) - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol (${state.version}) - volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime (${state.version}) - timeBetween: ${state.timeBetween}"
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
	message = state.msg
    
	def values = "${message}".split(";")
	vSize = values.size()
	count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMessage = values[randomKey]
    
	if(logEnable) log.debug "In messageHandler (${state.version}) - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMessage: ${theMessage}" 
	if(theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', friendlyName )}
    if(theMessage.contains("%place%")) {theMessage = theMessage.replace('%place%', presenceDevice.currentValue("address1") )}
    if(theMessage.contains("%lastplace%")) {theMessage = theMessage.replace('%lastplace%', state.lastAtPlace )}
    if(theMessage.contains("%address1%")) {theMessage = theMessage.replace('%address1%', presenceDevice.currentValue("address1") )}
    if(theMessage.contains("%address2%")) {theMessage = theMessage.replace('%address2%', presenceDevice.currentValue("address2") )}
    if(theMessage.contains("%battery%")) {
        String currBatt = presenceDevice.currentValue("battery")
        theMessage = theMessage.replace('%battery%', currBatt)
    }
    if(theMessage.contains("%charge%")) {theMessage = theMessage.replace('%charge%', presenceDevice.currentValue("charge") )}
    if(theMessage.contains("%distanceKm%")) {theMessage = theMessage.replace('%distanceKm%', presenceDevice.currentValue("distanceKm") )}
    if(theMessage.contains("%distanceMetric%")) {theMessage = theMessage.replace('%distanceMetric%', presenceDevice.currentValue("distanceMetric") )}
    if(theMessage.contains("%distanceMiles%")) {theMessage = theMessage.replace('%distanceMiles%', presenceDevice.currentValue("distanceMiles") )}
    if(theMessage.contains("%inTransit%")) {theMessage = theMessage.replace('%inTransit%', presenceDevice.currentValue("inTransit") )}
    if(theMessage.contains("%isDriving%")) {theMessage = theMessage.replace('%isDriving%', state.presenceDevice.currentValue("isDriving") )}
    if(theMessage.contains("%lastCheckin%")) {
        String currLastCheckin = presenceDevice.currentValue("lastCheckin")
        theMessage = theMessage.replace('%lastCheckin%', currLastCheckin)    
    }
    if(theMessage.contains("%latitude%")) {
        String currLatitude = presenceDevice.currentValue("latitude")
        theMessage = theMessage.replace('%latitude%', currLatitude)    
    }
    if(theMessage.contains("%longitude%")) {
        String currLongitude = presenceDevice.currentValue("longitude")
        theMessage = theMessage.replace('%longitude%', currLongitude)    
    }
    if(theMessage.contains("%powerSource%")) {theMessage = theMessage.replace('%powerSource%', state.presenceDevice.currentValue("powerSource") )}
    if(theMessage.contains("%presence%")) {theMessage = theMessage.replace('%presence%', state.presenceDevice.currentValue("presence") )}
    if(theMessage.contains("%speedKm%")) {theMessage = theMessage.replace('%speedKm%', state.presenceDevice.currentValue("speedKm") )}
    if(theMessage.contains("%speedMetric%")) {theMessage = theMessage.replace('%speedMetric%', state.presenceDevice.currentValue("speedMetric") )}
    if(theMessage.contains("%speedMiles%")) {theMessage = theMessage.replace('%speedMiles%', state.presenceDevice.currentValue("speedMiles") )}
    if(theMessage.contains("%wifiState%")) {theMessage = theMessage.replace('%wifiState%', state.presenceDevice.currentValue("wifiState") )}
    if(theMessage.contains("%display%")) {theMessage = theMessage.replace('%display%', state.presenceDevice.currentValue("display") )}
    if(theMessage.contains("%status%")) {theMessage = theMessage.replace('%status%', state.presenceDevice.currentValue("status") )}
    if(theMessage.contains("%lastLocationUpdate%")) {theMessage = theMessage.replace('%lastLocationUpdate%', state.presenceDevice.currentValue("lastLocationUpdate") )}
    
    state.theMessage = theMessage
    theMap = "https://www.google.com/maps/search/?api=1&query=${presenceDevice.currentValue("latitude")},${presenceDevice.currentValue("longitude")}"
    theMapLink = "<a href='${theMap}' target='_blank'>Map</a>"
	
    if(state.alerts == "yes") {
        if(speakAlertsBatt) letsTalk()
        
        if(pushAlertsBatt) pushHandler()
        
        state.alerts = "no"
    } else {
        if((speakHasArrived) && (state.speakAT == "yes")) letsTalk()
        if((speakHasDeparted)  && (state.speakDEP == "yes")) letsTalk()
        if((speakOnTheMove) && (state.speakMOVE == "yes")) letsTalk()
    
        if((pushHasArrived) && (state.speakAT == "yes")) pushHandler()
        if((pushHasDeparted) && (state.speakDEP == "yes")) pushHandler()
        if((pushOnTheMove) && (state.speakMOVE == "yes")) pushHandler()
        
        if((historyMap) && (state.speakAT == "yes")) {
            state.theHistoryMessage = "${state.theMessage} - ${theMapLink}"
            presenceDevice.sendHistory(state.theHistoryMessage)
            log.info "Life360 Tracker-HISTORY-A: ${state.theHistoryMessage}"
        } else if((historyMap) && (state.speakDEP == "yes")) {
            state.theHistoryMessage = "${state.theMessage} - ${theMapLink}"
            presenceDevice.sendHistory(state.theHistoryMessage)
            log.info "Life360 Tracker-HISTORY-D: ${state.theHistoryMessage}"
        } else if((historyMap) && (state.speakMOVE == "yes")) {
            state.theHistoryMessage = "${state.theMessage} - ${theMapLink}"
            presenceDevice.sendHistory(state.theHistoryMessage)
            log.info "Life360 Tracker-HISTORY-M: ${state.theHistoryMessage}"
        } else {
            state.theHistoryMessage = "${theMessage}"
            presenceDevice.sendHistory(state.theHistoryMessage)
            log.info "Life360 Tracker (${state.version}) - HISTORY: ${state.theHistoryMessage}"
        } 
    }
    
    state.theMessage = ""
    state.theHistoryMessage = ""
    theMessage = ""
    state.msg = ""
}

def pushHandler() {
	if(logEnable) log.debug "In pushNow (${state.version})"
    theMessage = "${state.theMessage}\n\n"
    theMap = "https://www.google.com/maps/search/?api=1&query=${presenceDevice.currentValue("latitude")},${presenceDevice.currentValue("longitude")}"
    if(linkPush) {theMessage += "${theMap}"}
	if(logEnable) log.debug "In pushNow (${state.version}) - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
    presenceDevice.sendTheMap(theMap)
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "testSpeaker"){
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.debug "In testButtonHandler - Testing Speaker"
        testResult = "<table><tr><td colspan=3 align=center>----------------------------------------------------------------</td></tr>"
        testResult += "<tr><td colspan=3 align=center><b>Speaker Test Results</b></td></tr>"
        state.speakers.each {
            if(it.hasCommand('setVolumeSpeakAndRestore')) {
                testResult += "<tr><td>${it}</td><td> - </td><td>uses setVolumeSpeakAndRestore</td></tr>"
            } else if(it.hasCommand('playTextAndRestore')) {
                testResult += "<tr><td>${it}</td><td> - </td><td>uses playTextAndRestore</td></tr>"
            } else {
                testResult += "<tr><td>${it}</td><td> - </td><td>needs all volume fields filled in</td></tr>"
            }
        }
        testResult += "<tr><td colspan=3><br>*Note: Speaker proxies can't be accurately tested.<br>If using a speaker proxy like 'What Did I Say', always fill in the failsafe fields.</td><tr>"
        testResult += "<tr><td colspan=3 align=center>----------------------------------------------------------------</td></tr>"
        testResult += "</table>"
        log.info "${testResult}"
    }
}

def whereAmI(evt) { 
    if(logEnable) log.debug "In whereAmI (${state.version})"
    state.address1Value = presenceDevice.currentValue("address1")
    def memberLatitude = new Float (presenceDevice.currentValue("latitude"))
    def memberLongitude = new Float (presenceDevice.currentValue("longitude"))
    
    if(parent.myName01) {
        def placeLatitude01 = new Float (parent.myLatitude01)
        def placeLongitude01 = new Float (parent.myLongitude01)
        def placeRadius01 = new Float (parent.myRadius01)
        def distanceAway01 = haversine(memberLatitude, memberLongitude, placeLatitude01, placeLongitude01)*1000 // in meters
      	boolean isPresent01 = (distanceAway01 <= placeRadius01)
        if(isPresent01) state.address1Value = parent.myName01
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 01 (${parent.myName01}): ${distanceAway01}, isPresent01: ${isPresent01}"
    }
    if(parent.myName02) {
        def placeLatitude02 = new Float (parent.myLatitude02)
        def placeLongitude02 = new Float (parent.myLongitude02)
        def placeRadius02 = new Float (parent.myRadius02)
        def distanceAway02 = haversine(memberLatitude, memberLongitude, placeLatitude02, placeLongitude02)*1000 // in meters
  	    boolean isPresent02 = (distanceAway02 <= placeRadius02)
        if(isPresent02) state.address1Value = parent.myName02
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 02 (${parent.myName02}): ${distanceAway02}, isPresent02: ${isPresent02}"
    }
    if(parent.myName03) {
        def placeLatitude03 = new Float (parent.myLatitude03)
        def placeLongitude03 = new Float (parent.myLongitude03)
        def placeRadius03 = new Float (parent.myRadius03)
        def distanceAway03 = haversine(memberLatitude, memberLongitude, placeLatitude03, placeLongitude03)*1000 // in meters
  	    boolean isPresent03 = (distanceAway03 <= placeRadius03)
        if(isPresent03) state.address1Value = parent.myName03
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 03 (${parent.myName03}): ${distanceAway03}, isPresent03: ${isPresent03}"
    }
    if(parent.myName04) {
        def placeLatitude04 = new Float (parent.myLatitude04)
        def placeLongitude04 = new Float (parent.myLongitude04)
        def placeRadius04 = new Float (parent.myRadius04)
        def distanceAway04 = haversine(memberLatitude, memberLongitude, placeLatitude04, placeLongitude04)*1000 // in meters
  	    boolean isPresent04 = (distanceAway04 <= placeRadius04)
        if(isPresent04) state.address1Value = parent.myName04
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 04 (${parent.myName04}): ${distanceAway04}, isPresent04: ${isPresent04}"
    }
    if(parent.myName05) {
        def placeLatitude05 = new Float (parent.myLatitude05)
        def placeLongitude05 = new Float (parent.myLongitude05)
        def placeRadius05 = new Float (parent.myRadius05)
        def distanceAway05 = haversine(memberLatitude, memberLongitude, placeLatitude05, placeLongitude05)*1000 // in meters
  	    boolean isPresent05 = (distanceAway05 <= placeRadius05)
        if(isPresent05) state.address1Value = parent.myName05
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 05 (${parent.myName05}): ${distanceAway05}, isPresent05: ${isPresent05}"
    }
    if(parent.myName06) {
        def placeLatitude06 = new Float (parent.myLatitude06)
        def placeLongitude06 = new Float (parent.myLongitude06)
        def placeRadius06 = new Float (parent.myRadius06)
        def distanceAway06 = haversine(memberLatitude, memberLongitude, placeLatitude06, placeLongitude06)*1000 // in meters
  	    boolean isPresent06 = (distanceAway06 <= placeRadius06)
        if(isPresent06) state.address1Value = parent.myName06
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 06 (${parent.myName06}): ${distanceAway06}, isPresent06: ${isPresent06}"
    }
    if(parent.myName07) {
        def placeLatitude07 = new Float (parent.myLatitude07)
        def placeLongitude07 = new Float (parent.myLongitude07)
        def placeRadius07 = new Float (parent.myRadius07)
        def distanceAway07 = haversine(memberLatitude, memberLongitude, placeLatitude07, placeLongitude07)*1000 // in meters
  	    boolean isPresent07 = (distanceAway07 <= placeRadius07)
        if(isPresent07) state.address1Value = parent.myName07
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 07 (${parent.myName07}): ${distanceAway07}, isPresent07: ${isPresent07}"
    }
    if(parent.myName08) {
        def placeLatitude08 = new Float (parent.myLatitude08)
        def placeLongitude08 = new Float (parent.myLongitude08)
        def placeRadius08 = new Float (parent.myRadius08)
        def distanceAway08 = haversine(memberLatitude, memberLongitude, placeLatitude08, placeLongitude08)*1000 // in meters
  	    boolean isPresent08 = (distanceAway08 <= placeRadius08)
        if(isPresent08) state.address1Value = parent.myName08
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 08 (${parent.myName08}): ${distanceAway08}, isPresent08: ${isPresent08}"
    }
    if(parent.myName09) {
        def placeLatitude09 = new Float (parent.myLatitude09)
        def placeLongitude09 = new Float (parent.myLongitude09)
        def placeRadius09 = new Float (parent.myRadius09)
        def distanceAway09 = haversine(memberLatitude, memberLongitude, placeLatitude09, placeLongitude09)*1000 // in meters
  	    boolean isPresent09 = (distanceAway09 <= placeRadius09)
        if(isPresent09) state.address1Value = parent.myName09
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 09 (${parent.myName09}): ${distanceAway09}, isPresent09: ${isPresent09}"
    }
    if(parent.myName10) {
        def placeLatitude10 = new Float (parent.myLatitude10)
        def placeLongitude10 = new Float (parent.myLongitude10)
        def placeRadius10 = new Float (parent.myRadius10)
        def distanceAway10 = haversine(memberLatitude, memberLongitude, placeLatitude10, placeLongitude10)*1000 // in meters
  	    boolean isPresent10 = (distanceAway10 <= placeRadius10)
        if(isPresent10) state.address1Value = parent.myName10
        if(logEnable) log.debug "In whereAmI (${state.version}) - Distance Away 10 (${parent.myName10}): ${distanceAway10}, isPresent10: ${isPresent10}"
    }
    
    if((!isPresent01) && (!isPresent02) && (!isPresent03) && (!isPresent04) && (!isPresent05) && (!isPresent06) && (!isPresent07) && (!isPresent08) && (!isPresent09) && (!isPresent10)) state.address1Value = presenceDevice.currentValue("address1")
    userHandler()
}

def haversine(lat1, lon1, lat2, lon2) {
    def R = 6372.8
    // In kilometers
    def dLat = Math.toRadians(lat2 - lat1)
    def dLon = Math.toRadians(lon2 - lon1)
    lat1 = Math.toRadians(lat1)
    lat2 = Math.toRadians(lat2)
 
    def a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
    def c = 2 * Math.asin(Math.sqrt(a))
    def d = R * c
    return(d)
}

def buildMyPlacesList() {
    if(logEnable) log.debug "In buildMyPlacesList (${state.version}) "
    state.myPlacesList = []
    if(parent.myName01) state.myPlacesList = state.myPlacesList.plus([parent.myName01])
    if(parent.myName02) state.myPlacesList = state.myPlacesList.plus([parent.myName02])
    if(parent.myName03) state.myPlacesList = state.myPlacesList.plus([parent.myName03])
    if(parent.myName04) state.myPlacesList = state.myPlacesList.plus([parent.myName04])
    if(parent.myName05) state.myPlacesList = state.myPlacesList.plus([parent.myName05])
    if(parent.myName06) state.myPlacesList = state.myPlacesList.plus([parent.myName06])
    if(parent.myName07) state.myPlacesList = state.myPlacesList.plus([parent.myName07])
    if(parent.myName08) state.myPlacesList = state.myPlacesList.plus([parent.myName08])
    if(parent.myName09) state.myPlacesList = state.myPlacesList.plus([parent.myName09])
    if(parent.myName10) state.myPlacesList = state.myPlacesList.plus([parent.myName10])
    if(logEnable) log.debug "In buildMyPlacesList (${state.version}) - myName01: ${parent.myName01}"
    if(logEnable) log.debug "In buildMyPlacesList (${state.version}) - myPlacesList: ${state.myPlacesList}"
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    if(state.beenHere == null){state.beenHere = "no"}
    if(state.address1Value == null){state.address1Value = presenceDevice.currentValue("address1")}
    if(state.prevPlace == null){state.prevPlace = state.address1Value}
    if(trackSpecific2 == null){trackSpecific2 = "None selected"}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Life360 Tracker - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
