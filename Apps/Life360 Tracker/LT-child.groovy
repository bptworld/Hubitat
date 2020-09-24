/**
 *  ****************  Life360 Tracker Child App  ****************
 *
 *  Design Usage:
 *  Track your Life360 users. Works with the Life360 with States app.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  2.1.0 - 09/02/20 - Cosmetic changes
 *  2.0.9 - 06/22/20 - Changes to letsTalk
 *  2.0.8 - 06/19/20 - added The Flasher
 *  2.0.7 - 06/18/20 - Major Changes. Going at it a different way
 *  2.0.6 - 04/27/20 - Cosmetic changes
 *  2.0.5 - 12/10/19 - Minor bug fixes
 *  2.0.4 - 11/23/19 - More code adjustments
 *  2.0.3 - 11/03/19 - Code changes to remove some gremlins
 *  2.0.2 - 09/20/19 - History logging adjustments
 *  2.0.1 - 09/06/19 - Fixed bug with timeMove
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  --
 *  1.0.0 - 07/01/19 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Life360 Tracker"
	state.version = "2.1.0"
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
    dynamicPage(name: "pageConfig", title: "", nextPage: null, install: true, uninstall: true) {
		display()        
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Track your Life360 users. Works with the user Life360 with States app."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Device")) {
			input "presenceDevice", "capability.presenceSensor", title: "Select Life360 User Device", required:true
            input "friendlyName", "text", title: "Friendly Name used in messages for this Device", required:true, submitOnChange:true
		}
    if(presenceDevice) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Place Tracking")) {
            paragraph "This will track the coming and going of places."
            paragraph "This will combine your Starred Places from Life360 and any 'My Places' created in the Life360 Parent app."
            savedPlacesTemp = presenceDevice.currentValue("savedPlaces")
            if(savedPlacesTemp) state.lPlaces = presenceDevice.currentValue("savedPlaces").replace("[","").replace("]","").replace(" ,", ",").replace(", ", ",")
            buildMyPlacesList()
            
            state.allPlaces = [state.lPlaces, state.myPlacesList].flatten().findAll{it} 
            state.thePlaces = "${state.allPlaces}".replace("[","").replace("]","").replace(" ,", ",").replace(", ", ",")               
            state.values = "${state.thePlaces}".split(",")
            
            input "trackSpecific", "enum", title:"Life360 Places", options: state.values, multiple:true, required:true, submitOnChange:true
            input "oG1List", "bool", defaultValue: false, title: "Show a list view of Specific Places?", description: "List View", submitOnChange:true
            if(oG1List) {
                def valuesG1 = "${trackSpecific}".split(",")
                listMapG1 = ""
                valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
                paragraph "${listMapG1}".replace("[","").replace("]","")
            }
            
            input "timeConsideredHere", "number", title: "Time to be considered at a Place (in Minutes, range 1 to 10)", required:true, submitOnChange:true, defaultValue: 2, range: '1..10'
        }
        
// *** Home ***            
        section(getFormat("header-green", "${getImage("Blank")}"+" Home")) {    
            paragraph "'Home' is a unique place in Life360 and with Automation. For most us we want things to happen as soon as possible.  With this app, one needs to be at a Place for at least 1 minute before it will trigger things. This is to stop false alarms and to be sure we are not just passing by.  But with 'Home' sometimes things should work different..."
            input "homeDelayed", "bool", defaultValue:false, title: "Should Tracker announce when you arrive at 'Home', after the 1 minute wait? (off='No', on='Yes')", description: "Home Wait", submitOnChange:true
            paragraph "<small>This is useful if you have another app announcing when you are home, like 'Welcome Home'</small>"
            input "homeNow", "bool", defaultValue:false, title: "Should Tracker announce when the User arrives at 'Home', with NO wait? (off='No', on='Yes')", description: "Home Instant", submitOnChange:true
            paragraph "<small>This will give a nice heads up that someone is home. But can be a false alarm if they are just driving by.</small>"
            input "homeDepartedDelayed", "bool", defaultValue:false, title: "Should Tracker announce when the User departs from 'Home', after a 1 minute delay? (off='No', on='Yes')", description: "Delayed Home Depart", submitOnChange:true
            paragraph "<small>This will give a heads up that someone has departed home. But help with false announcements.</small>"
            input "homeDeparted", "bool", defaultValue:false, title: "Should Tracker announce right away, when the User departs from 'Home'? (off='No', on='Yes')", description: "Home Depart", submitOnChange:true
            paragraph "<small>This will give a heads up that someone has departed home. As soon as it is detected.</small>"
            paragraph "Note: Home options will only work if you have the Speak or Push when 'Has arrived'and/or 'Has departed' switch in Message Options turned on."
        }
// *** End Home ***
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a device<br>%place% - returns the current place<br>%lastplace% - returns the place departed"
            paragraph "* PLUS - all attribute names can be used as wildcards! Just make sure the name is exact, capitalization counts!  ie. %powerSource%, %distanceMiles% or %wifiState%"
            paragraph "<b>'Has Arrived'</b>"
            input "speakHasArrived", "bool", defaultValue:false, title: "Speak when", description: "Speak Has Arrived", width: 4, submitOnChange:true
            input "pushHasArrived", "bool", defaultValue:false, title: "Push when", description: "Push Has Arrived", width: 4, submitOnChange:true
            input "historyHasArrived", "bool", defaultValue:false, title: "Log History when", description: "History Has arrived", width: 4, submitOnChange:true
			if(speakHasArrived || pushHasArrived || historyHasArrived) input "messageAT", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has arrived at %place%"
			if(speakHasArrived || pushHasArrived || historyHasArrived) input "atMsgList", "bool", defaultValue:false, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
			if((speakHasArrived || pushHasArrived || historyHasArrived) && atMsgList) {
				def values = "${messageAT}".split(";")
				listMapAT = ""
    			values.each { item -> listMapAT += "${item}<br>"}
				paragraph "${listMapAT}"
			}
            if(speakHasArrived || pushHasArrived || historyHasArrived) paragraph "<hr>"
            
            paragraph "<b>'Has Departed'</b>"
            input "speakHasDeparted", "bool", defaultValue:false, title: "Speak when", description: "Speak Has departed", width: 4, submitOnChange:true
            input "pushHasDeparted", "bool", defaultValue:false, title: "Push when", description: "Push Has departed", width: 4, submitOnChange:true
            input "historyHasDeparted", "bool", defaultValue:false, title: "Log History when", description: "History Has departed", width: 4, submitOnChange:true
			if(speakHasDeparted || pushHasDeparted || historyHasDeparted) input "messageDEP", "text", title: "Random Message to be spoken when <b>'has departed'</b> a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has departed from %place%"
			if(speakHasDeparted || pushHasDeparted || historyHasDeparted) input "depMsgList", "bool", defaultValue:false, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
			if((speakHasDeparted || pushHasDeparted || historyHasDeparted) && depMsgList) {
				def values = "${messageDEP}".split(";")
				listMapDEP = ""
    			values.each { item -> listMapDEP += "${item}<br>"}
                paragraph "${listMapDEP}"
			}
            if(speakHasDeparted || pushHasDeparted || historyHasDeparted) paragraph "<hr>"
            
            paragraph "<b>'On the Move'</b>"
            input "speakOnTheMove", "bool", defaultValue:false, title: "Speak when", description: "Speak On the Move", width: 4, submitOnChange:true
            input "pushOnTheMove", "bool", defaultValue:false, title: "Push when", description: "Push On the Move", width: 4, submitOnChange:true
            input "historyOnTheMove", "bool", defaultValue:false, title: "Log History when", description: "History On the Move", width: 4, submitOnChange:true
            if(speakOnTheMove || pushOnTheMove || historyOnTheMove) input "timeMove", "number", title: "How often to report 'On the Move' (in Minutes, range 1 to 30)", required: true, submitOnChange: true, defaultValue: 5, range: '1..30'
            if(speakOnTheMove || pushOnTheMove || historyOnTheMove) input "messageMOVE", "text", title: "Random Message to be spoken when <b>'on the move'</b> near a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% is on the move near %place%"
			if(speakOnTheMove || pushOnTheMove || historyOnTheMove) input "moveMsgList", "bool", defaultValue:false, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
			if((speakOnTheMove || pushOnTheMove || historyOnTheMove) && moveMsgList) {
				def values = "${messageMOVE}".split(";")
				listMapMove = ""
    			values.each { item -> listMapMove += "${item}<br>"}
				paragraph "${listMapMove}"
			}
            
            if(speakOnTheMove || pushOnTheMove || historyOnTheMove) {
                paragraph "If someone stops at a place this app isn't tracking, a message will be spoken."
                input "messageMOVEStopped", "text", title: "Random Message to be spoken when <b>'on the move' but Stopped</b> - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has stopped near %place%"
                input "moveSMsgList", "bool", defaultValue:false, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
                if(moveSMsgList) {
                    def valuesS = "${messageMOVEStopped}".split(";")
                    listMapMoveS = ""
                    valuesS.each { item -> listMapMoveS += "${item}<br>"}
                    paragraph "${listMapMoveS}"
                }
            }
            
            paragraph "<hr>"
            input "historyMap", "bool", defaultValue:false, title: "Add Map Link to History message?", description: "History Map", submitOnChange:true
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
            section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
                paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
                input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
                if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
                paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
                input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
                if(useTheFlasher) {
                    input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:false, multiple:false
                    input "flashArrivedHomePreset", "number", title: "Select the Preset to use when someone arrives at Home (1..5)", required:false, submitOnChange:true
                    input "flashDepartedHomePreset", "number", title: "Select the Preset to use when someone departs Home (1..5)", required:false, submitOnChange:true
                    
                    input "flashArrivedPlacePreset", "number", title: "Select the Preset to use when someone arrives at a Place (1..5)", required:false, submitOnChange:true
                    input "flashDepartedPlacePreset", "number", title: "Select the Preset to use when someone departs a Place (1..5)", required:false, submitOnChange:true
                    
                    input "flashArrivedNotAllowedPreset", "number", title: "Select the Preset to use when someone arrives at a Place NOT allowed (1..5)", required:false, submitOnChange:true
                }
            }
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            input "isDataDevice", "capability.switch", title: "Turn this device on/off (On = at place, Off = moving)", required: false, multiple: false
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Extra Options")) {           
            href "alertsConfig", title: "Alerts", description: "Phone Battery - Places Not Allowed"
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this automation", required: false
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Debugging"
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
            input "speakAlertsBatt", "bool", defaultValue:false, title: "Speak on Alert", description: "Speak On Alert", width:6
            input "pushAlertsBatt", "bool", defaultValue:false, title: "Push on Alert", description: "Push On Alert", width:6
            input "messageAlertBatt", "text", title: "Random Message to be spoken when <b>'Alert - Battery'</b> - Separate each message with <b>;</b> (semicolon)",  required: true, defaultValue: "%name%'s phone battery (%battery%) needs charging"
            paragraph "<hr>"
            paragraph "<b>Places Warning</b> - Sometimes there are Places one should not go."
            input "trackSpecific2", "enum", title:"Warn when at these Places", options: state.values, multiple: true, required:false
            input "speakHasArrived2", "bool", defaultValue:false, title: "Speak when 'Has arrived'", description: "Speak Has Arrived", width: 4
            input "pushHasArrived2", "bool", defaultValue:false, title: "Push when 'Has arrived'", description: "Push Has Arrived", width: 4
            input "historyHasArrived2", "bool", defaultValue:false, title: "Log History when 'Has arrived'", description: "History Has arrived", width: 4
			input "messageAT2", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)", required: true, defaultValue: "%name% has arrived at %place% but should NOT be there"
            
			input "speakHasDeparted2", "bool", defaultValue:false, title: "Speak when 'Has departed'", description: "Speak Has departed", width: 4
            input "pushHasDeparted2", "bool", defaultValue:false, title: "Push when 'Has departed'", description: "Push Has departed", width: 4
            input "historyHasDeparted2", "bool", defaultValue:false, title: "Log History when 'Has departed'", description: "History Has departed", width: 4
			input "messageDEP2", "text", title: "Random Message to be spoken when <b>'has departed'</b> a place - Separate each message with <b>;</b> (semicolon)", required: true, defaultValue: "%name% has departed %place% but should NOT have been there"
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
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        subscribe(presenceDevice, "address1", userHandler)
    }
}

def userHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "---------- Start Log - Life360 Tracker Child - ${state.version} ----------"
        whereAmI()
        alertBattHandler()

        state.match = false
        state.placeToMatch = ""
        if(trackSpecific2 == null) trackSpecific2 = "NoData"

        trackSpecific2.each { it ->
            if(it == state.address1Value) {
                state.match = true
                placeNotAllowedHandler(it) 
            }
        } 

        trackSpecific.each { it ->
            if(it == state.address1Value) {
                if(it.toLowerCase() == "home") {
                    state.match = true
                    homeArrivedHandler(it)
                } else {
                    state.match = true
                    arrivedHandler(it) 
                }
            } else if(it == state.address1Prev) {
                if(it.toLowerCase() == "home") {
                    state.match = true
                    homeDepartedHandler(it)
                } else {
                    state.match = true
                    departedHandler(it) 
                }
            }
        }

        if(state.match == false) {
            if(logEnable) log.debug "In userHandler - No Match Found, Going to movingHandler - match: ${state.match}"
            movingHandler(state.address1Value)
        }

        if(logEnable) log.debug "---------- End Log - Life360 Tracker Child - ${state.version} ----------"
    }
}

// Start Tracking

def arrivedHandler(aPlace) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "********* In arrivedHandler (${state.version}) *********"
        if(aPlace == null) aPlace = state.address1Value
        msg = ""

        if(logEnable) log.debug "In arrivedHandler - ${friendlyName} is near ${aPlace}"
        int timeHere = timeConsideredHere * 60
        getTimeDiff()
        def theTimeDiff = timeDiff
        if(logEnable) log.debug "In arrivedHandler - timeDiff: ${theTimeDiff} vs timeHere: ${timeHere}" 
        if(timeDiff >= timeHere) {
            if(logEnable) log.debug "In arrivedHandler - Time at Place: ${timeDiff} IS greater than: ${timeHere}"
            msg = "${messageAT}"
            where = "arrived"
            messageHandler(where,msg,aPlace)
            if(isDataDevice) isDataDevice.on()
            state.sMove = null
        } else {  // ***  timeDiff is NOT GREATER THAN timeHere ***
            if(logEnable) log.debug "In arrivedHandler - Time at Place: ${timeDiff} IS NOT greater than: ${timeHere}"
            if(isDataDevice) isDataDevice.off()
            runIn(30, arrivedHandler, [overwrite: false]) 
        }
        if(logEnable) log.debug "********* In arrivedHandler - End *********"
    }
}

def departedHandler(dPlace) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "********* In departedHandler (${state.version}) *********"
        if(dPlace == null) dPlace = state.address1Prev
        msg = ""

        if(logEnable) log.debug "In departedHandler - ${friendlyName} has departed from ${dPlace}"
        where = "departed"
        msg = "${messageDEP}"
        messageHandler(where,msg,dPlace)
        if(isDataDevice) isDataDevice.off()
        if(logEnable) log.debug "********* In departedHandler - End *********"
    }
}

def movingHandler(mPlace) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "********* In movingHandler (${state.version}) *********"
        if(!timeMove) timeMove = 5
        int timeMoving = timeMove * 60
        if(mPlace == null) mPlace = state.address1Value
        msg = ""

        getTimeMoving()   
        if(logEnable) log.debug "In arrivedHandler - movingDiff: ${movingDiff} vs timeMoving: ${timeMoving}" 
        if(movingDiff >= timeMoving) {
            state.sMove = null
            if(state.movePrevPlace == mPlace) {
                if(logEnable) log.debug "In movingHandler - ${friendlyName} has stopped near ${mPlace}"
                msg = "${messageMOVEStopped}"
                where = "moving"
                messageHandler(where,msg,mPlace)
                state.movePrevPlace = mPlace
            } else {           
                msg = "${messageMOVE}"
                where = "moving"
                messageHandler(where,msg,mPlace)
                if(logEnable) log.debug "In movingHandler - ${friendlyName} is near ${mPlace}"
            }
        } else {
            if(logEnable) log.debug "In movingHandler - ${friendlyName} has been here less than ${timeMove} minutes but is near ${mPlace}"
            if(state.movePrevPlace == null) { state.movePrevPlace = mPlace }
        }     
        if(isDataDevice) isDataDevice.off()
        if(logEnable) log.debug "********* In movingHandler - End *********"
    }
}

// *** Track Home ***

def homeArrivedHandler(haPlace) {
    if(logEnable) log.debug "********* In homeArrivedHandler (${state.version}) *********"
    if(haPlace == null) haPlace = "home"
    msg = ""
    
    if(homeNow) {
        if(logEnable) log.debug "In homeArrivedHandler - Home Now - ${friendlyName} has arrived at ${haPlace}"
        msg = "${messageAT}"
        where = "HomeArrived"
        messageHandler(where,msg,haPlace)
        if(isDataDevice) isDataDevice.on()
    } else if(homeDelayed) {
        getTimeDiff()
        if(timeDiff >= 60) {
            if(logEnable) log.debug "In homeArrivedHandler - Home Delay - ${friendlyName} has arrived at ${haPlace}"
            msg = "${messageAT}"
            where = "HomeArrived"
            messageHandler(where,msg,haPlace)
            if(isDataDevice) isDataDevice.on()
        } else {
            if(logEnable) log.debug "In homeArrivedHandler - ${friendlyName} arrived Home less than ${timeHere} minute(s) ago"
            runIn(30, homeArrivedHandler, [overwrite: false])
        }
    }
    if(logEnable) log.debug "********* In homeArrivedHandler - End *********"
}

def homeDepartedHandler(daPlace) {
    if(logEnable) log.debug "********* In homeDepartedHandler (${state.version}) *********"
    if(daPlace == null) daPlace = "home"
    msg = ""
    
    if(homeDeparted) {
        if(logEnable) log.debug "In homeDepartedHandler - Home Departed - ${friendlyName} has departed from ${daPlace}"
        msg = "${messageDEP}"
        where = "HomeDeparted"
        messageHandler(where,msg,daPlace)
    } else if(homeDepartedDelayed) {
        getTimeMoving()      
        if(logEnable) log.debug "In homeDepartedHandler - movingDiff: ${movingDiff}"
        if(movingDiff >= 60) {
            if(logEnable) log.debug "In homeDepartedHandler - ${friendlyName} has departed Home over 1 minute ago and is on the move near ${daPlace}"
            msg = "${messageDEP}"
            where = "HomeDeparted"
            messageHandler(where,msg,daPlace)
        } else {
            if(logEnable) log.debug "In homeDepartedHandler - ${friendlyName} departed Home less than 1 minute ago but is near ${daPlace}"
            runIn(30, homeDepartedHandler, [overwrite: false])
        }
    }
    if(logEnable) log.debug "********* In homeDepartedHandler - End *********"
}

// *** Track placeNotAllowed ***

def placeNotAllowedHandler(naPlace) {
    if(logEnable) log.debug "********* In placeNotAllowedHandler (${state.version}) *********"
    if(naPlace == null) naPlace = state.address1Value
    msg = ""
    
    if(logEnable) log.debug "In placeNotAllowedHandler - ${friendlyName} has arrived at ${naPlace}"    
    int timeHere = timeConsideredHere * 60
    getTimeDiff()
    if(timeDiff >= timeHere) {
        if(logEnable) log.debug "In placeNotAllowedHandler - Time at Place: ${timeDiff} IS greater than: ${timeHere}"
        msg = "${messageAT2}"
        where = "aAT2"
        messageHandler(where,msg,naPlace)  
        if(isDataDevice) isDataDevice.on()
    } else {  // *** timeDiff is NOT GREATER THAN timeHere ***
        if(logEnable) log.debug "In placeNotAllowedHandler - Time at Place: ${timeDiff} IS NOT greater than: ${timeHere}"
        if(isDataDevice) isDataDevice.off()
        runIn(30, placeNotAllowedHandler, [overwrite: false])
    }
    if(logEnable) log.debug "********* In placeNotAllowedHandler - End *********"
}

def alertBattHandler() {
    if(logEnable) log.debug "In alertBattHandler (${state.version})"
    battery = presenceDevice.currentValue("battery")
    charge = presenceDevice.currentValue("charge")
    battPlace = ""
    if(logEnable) log.debug "In alertBattHandler - battery: ${battery} - prev battery: ${state.prevBatt} - charge: ${charge} - alertBattRepeat: ${state.alertBattRepeat}"
    
    if((battery <= alertBatt) && (charge == "false") && (state.alertBattRepeat != "yes")) {
        if(logEnable) log.debug "In alertBattHandler - battery (${battery}) needs charging! - Step 1 - battery: ${battery} <= Prev: ${state.prevBatt}"
        msg = "${messageAlertBatt}"
        state.prevBatt = battery - 10
        state.alerts = "yes"
        state.alertBattRepeat = "yes"
        where = "battAlert"
        messageHandler(where,msg,battPlace)
    } else if((battery <= state.prevBatt) && (charge == "false")) {
        if(logEnable) log.debug "In alertBattHandler - battery (${battery}) needs charging! - Step 2 - battery: ${battery} <= Prev: ${state.prevBatt}"
        msg = "${messageAlertBatt}"
        state.prevBatt = state.prevBatt - 10
        state.alertBattRepeat = "yes"
        state.alerts = "yes"
        where = "battAlert"
        messageHandler(where,msg,battPlace)
    } else if(charge == "true") {
        if(logEnable) log.debug "In alertBattHandler - battery (${battery}) is charging. - Step 3"
        state.alertBattRepeat = "no"
        state.prevBatt = 0
    }  
    state.alerts = "no"
    if(logEnable) log.debug "In alertBattHandler - End"
}

def getTimeDiff() {
	if(logEnable) log.debug "In getTimeDiff (${state.version})"   
	long since = presenceDevice.currentValue("since")
   	def now = new Date()
    long unxNow = now.getTime()
    unxNow = unxNow/1000    
    timeDiff = Math.abs(unxNow-since)
    if(logEnable) log.debug "In getTimeDiff - since: ${since}, Now: ${unxNow}, Diff: ${timeDiff}"
    return timeDiff
}

def getTimeMoving() {
	if(logEnable) log.debug "In getTimeMoving (${state.version})"
    if(state.sMove == null || state.sMove == "") {
        def now = new Date()
        long startMove = now.getTime()
        state.sMove = startMove
        if(logEnable) log.debug "In getTimeMoving - NEW sMove: ${state.sMove}"
    } 
    
   	def now = new Date()
    long unxNow = now.getTime()
    long unxPrev = state.sMove
    unxNow = unxNow/1000
    unxPrev = unxPrev/1000
    movingDiff = Math.abs(unxNow-unxPrev)
    if(logEnable) log.debug "In getTimeMoving - prev: ${unxPrev}, Now: ${unxNow}, Diff: ${movingDiff}"
    return movingDiff
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(msg)
    }
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def messageHandler(where,msg,place) {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    
	def values = "${msg}".split(";")
	vSize = values.size()
	count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	state.message = values[randomKey]
    
    if(logEnable) log.debug "In messageHandler - where: ${where} - place: ${place} - message: ${state.message}" 
	if(state.message.contains("%name%")) {state.message = state.message.replace('%name%', friendlyName )}
    
    if(where != "HomeArrived" || where != "HomeDeparted") { 
        if(state.message.contains("%place%")) state.message = state.message.replace('%place%', place) 
        if(state.message.contains("%lastplace%")) state.message = state.message.replace('%lastplace%', place)
    }
    
    if(where == "HomeArrived") {
        if( state.message.contains("%lastplace%") || state.message.contains("%place%") ) {
            state.message = state.message.replace('%lastplace%', 'home') 
            state.message = state.message.replace('%place%', 'home')
        }
    }
    
    if(where == "HomeDeparted") { 
        if(state.message.contains("%lastplace%") || state.message.contains("%place%")) {
            state.message = state.message.replace('%lastplace%', 'home') 
            state.message = state.message.replace('%place%', 'home')
        }
    }
            
    if(state.message.contains("%address1%")) {state.message = state.message.replace('%address1%', place) }
    if(state.message.contains("%battery%")) {
        String currBatt = presenceDevice.currentValue("battery")
        state.message = state.message.replace('%battery%', currBatt)
    }
    
    if(state.message.contains("%charge%")) {state.message = state.message.replace('%charge%', presenceDevice.currentValue("charge") )}
    if(state.message.contains("%distanceKm%")) {state.message = state.message.replace('%distanceKm%', presenceDevice.currentValue("distanceKm") )}
    if(state.message.contains("%distanceMetric%")) {state.message = state.message.replace('%distanceMetric%', presenceDevice.currentValue("distanceMetric") )}
    if(state.message.contains("%distanceMiles%")) {state.message = state.message.replace('%distanceMiles%', presenceDevice.currentValue("distanceMiles") )}
    if(state.message.contains("%inTransit%")) {state.message = state.message.replace('%inTransit%', presenceDevice.currentValue("inTransit") )}
    if(state.message.contains("%isDriving%")) {state.message = state.message.replace('%isDriving%', state.presenceDevice.currentValue("isDriving") )}
    if(state.message.contains("%lastCheckin%")) {
        String currLastCheckin = presenceDevice.currentValue("lastCheckin")
        state.message = state.message.replace('%lastCheckin%', currLastCheckin)    
    }
    if(state.message.contains("%latitude%")) {
        String currLatitude = presenceDevice.currentValue("latitude")
        state.message = state.message.replace('%latitude%', currLatitude)    
    }
    if(state.message.contains("%longitude%")) {
        String currLongitude = presenceDevice.currentValue("longitude")
        state.message = state.message.replace('%longitude%', currLongitude)    
    }
    if(state.message.contains("%powerSource%")) {state.message = state.message.replace('%powerSource%', state.presenceDevice.currentValue("powerSource") )}
    if(state.message.contains("%presence%")) {state.message = state.message.replace('%presence%', state.presenceDevice.currentValue("presence") )}
    if(state.message.contains("%speedKm%")) {state.message = state.message.replace('%speedKm%', state.presenceDevice.currentValue("speedKm") )}
    if(state.message.contains("%speedMetric%")) {state.message = state.message.replace('%speedMetric%', state.presenceDevice.currentValue("speedMetric") )}
    if(state.message.contains("%speedMiles%")) {state.message = state.message.replace('%speedMiles%', state.presenceDevice.currentValue("speedMiles") )}
    if(state.message.contains("%wifiState%")) {state.message = state.message.replace('%wifiState%', state.presenceDevice.currentValue("wifiState") )}
    if(state.message.contains("%display%")) {state.message = state.message.replace('%display%', state.presenceDevice.currentValue("display") )}
    if(state.message.contains("%status%")) {state.message = state.message.replace('%status%', state.presenceDevice.currentValue("status") )}
    if(state.message.contains("%lastLocationUpdate%")) {state.message = state.message.replace('%lastLocationUpdate%', state.presenceDevice.currentValue("lastLocationUpdate") )}

    theMap = "https://www.google.com/maps/search/?api=1&query=${presenceDevice.currentValue("latitude")},${presenceDevice.currentValue("longitude")}"
    theMapLink = "<a href='${theMap}' target='_blank'>Map</a>"
	
    if(state.alerts == "yes") {
        if(speakAlertsBatt && (speakerMP || speakerSS)) letsTalk(state.message)       
        if(pushAlertsBatt && sendPushMessage) pushHandler(state.message)       
        state.alerts = "no"
    } else {
        if(state.message == "No Data") {
            if(logEnable) log.debug "In messageHandler - Life360 reported 'No Data' - So doing nothing"
        } else {
            if(state.previousMessage == null) state.previousMessage = "abcdefghijkl"
            if(state.previousMessage.toLowerCase() != state.message.toLowerCase()) {       
                if(where == "arrived" || where == "HomeArrived") {
                    if(speakHasArrived) letsTalk(state.message)
                    if(pushHasArrived) pushHandler(state.message)
                    if(useTheFlasher && flashArrivedHomePreset) {
                        flashData = "Preset::${flashArrivedHomePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                    if(useTheFlasher && flashArrivedPlacePreset) {
                        flashData = "Preset::${flashArrivedPlacePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                } else if(where == "departed" || where == "HomeDeparted") {
                    if(speakHasDeparted) letsTalk(state.message)
                    if(pushHasDeparted) pushHandlertheMessage(state.message)
                    if(useTheFlasher && flashDepartedHomePreset) {
                        flashData = "Preset::${flashDepartedHomePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                    if(useTheFlasher && flashDepartedPlacePreset) {
                        flashData = "Preset::${flashDepartedPlacePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                } else if(where == "moving") {
                    if(speakOnTheMove) letsTalk(state.message)             
                    if(pushOnTheMove) pushHandler(state.message)
                    if(useTheFlasher && flashArrivedNotAllowedPreset) {
                        flashData = "Preset::${flashArrivedNotAllowedPreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                }

                if(historyMap && historyHasArrived) {
                    state.theHistoryMessage = "${state.message} - ${theMapLink}"
                    presenceDevice.sendHistory(state.theHistoryMessage)
                    if(logEnable) log.info "Life360 Tracker-HISTORY-A: ${state.theHistoryMessage}"
                } else if(historyMap && historyHasDeparted) {
                    state.theHistoryMessage = "${state.message} - ${theMapLink}"
                    presenceDevice.sendHistory(state.theHistoryMessage)
                    if(logEnable) log.info "Life360 Tracker-HISTORY-D: ${state.theHistoryMessage}"
                } else if(historyMap && historyOnTheMove) {
                    state.theHistoryMessage = "${state.message} - ${theMapLink}"
                    presenceDevice.sendHistory(state.theHistoryMessage)
                    if(logEnable) log.info "Life360 Tracker-HISTORY-M: ${state.theHistoryMessage}"
                } else if(historyHasArrived) {
                    state.theHistoryMessage = "${state.message}"
                    presenceDevice.sendHistory(state.theHistoryMessage)
                    if(logEnable) log.info "Life360 Tracker-HISTORY-A: ${state.theHistoryMessage}"
                } else if(historyHasDeparted) {
                    state.theHistoryMessage = "${state.message}"
                    presenceDevice.sendHistory(state.theHistoryMessage)
                    if(logEnable) log.info "Life360 Tracker-HISTORY-D: ${state.theHistoryMessage}"
                } else if(historyOnTheMove) {
                    state.theHistoryMessage = "${state.message}"
                    presenceDevice.sendHistory(state.theHistoryMessage)
                    if(logEnable) log.info "Life360 Tracker-HISTORY-M: ${state.theHistoryMessage}"
                } 
            } else {
                if(logEnable) log.debug "In messageHandler - Duplicate message, not sending."
            }
        }
    }   
    state.previousMessage = state.message
    state.message = ""
    state.theHistoryMessage = ""
    theMessage = ""
}

def pushHandler(msg) {
	if(logEnable) log.debug "In pushNow (${state.version})"
    theMessage = "${msg}\n\n"
    theMap = "https://www.google.com/maps/search/?api=1&query=${presenceDevice.currentValue("latitude")},${presenceDevice.currentValue("longitude")}"
    if(linkPush) {theMessage += "${theMap}"}
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
    presenceDevice.sendTheMap(theMap)
}

def whereAmI(evt) { 
    if(logEnable) log.debug "In whereAmI (${state.version})"
    state.address1Value = presenceDevice.currentValue("address1")
    state.address1Prev = presenceDevice.currentValue("address1prev")
    def memberLatitude = new Float (presenceDevice.currentValue("latitude"))
    def memberLongitude = new Float (presenceDevice.currentValue("longitude"))
    
    if(parent.myName01) {
        def placeLatitude01 = new Float (parent.myLatitude01)
        def placeLongitude01 = new Float (parent.myLongitude01)
        def placeRadius01 = new Float (parent.myRadius01)
        def distanceAway01 = haversine(memberLatitude, memberLongitude, placeLatitude01, placeLongitude01)*1000 // in meters
      	boolean isPresent01 = (distanceAway01 <= placeRadius01)
        if(isPresent01) state.address1Value = parent.myName01
        if(logEnable) log.debug "In whereAmI - Distance Away 01 (${parent.myName01}): ${distanceAway01}, isPresent01: ${isPresent01}"
    }
    if(parent.myName02) {
        def placeLatitude02 = new Float (parent.myLatitude02)
        def placeLongitude02 = new Float (parent.myLongitude02)
        def placeRadius02 = new Float (parent.myRadius02)
        def distanceAway02 = haversine(memberLatitude, memberLongitude, placeLatitude02, placeLongitude02)*1000 // in meters
  	    boolean isPresent02 = (distanceAway02 <= placeRadius02)
        if(isPresent02) state.address1Value = parent.myName02
        if(logEnable) log.debug "In whereAmI - Distance Away 02 (${parent.myName02}): ${distanceAway02}, isPresent02: ${isPresent02}"
    }
    if(parent.myName03) {
        def placeLatitude03 = new Float (parent.myLatitude03)
        def placeLongitude03 = new Float (parent.myLongitude03)
        def placeRadius03 = new Float (parent.myRadius03)
        def distanceAway03 = haversine(memberLatitude, memberLongitude, placeLatitude03, placeLongitude03)*1000 // in meters
  	    boolean isPresent03 = (distanceAway03 <= placeRadius03)
        if(isPresent03) state.address1Value = parent.myName03
        if(logEnable) log.debug "In whereAmI - Distance Away 03 (${parent.myName03}): ${distanceAway03}, isPresent03: ${isPresent03}"
    }
    if(parent.myName04) {
        def placeLatitude04 = new Float (parent.myLatitude04)
        def placeLongitude04 = new Float (parent.myLongitude04)
        def placeRadius04 = new Float (parent.myRadius04)
        def distanceAway04 = haversine(memberLatitude, memberLongitude, placeLatitude04, placeLongitude04)*1000 // in meters
  	    boolean isPresent04 = (distanceAway04 <= placeRadius04)
        if(isPresent04) state.address1Value = parent.myName04
        if(logEnable) log.debug "In whereAmI - Distance Away 04 (${parent.myName04}): ${distanceAway04}, isPresent04: ${isPresent04}"
    }
    if(parent.myName05) {
        def placeLatitude05 = new Float (parent.myLatitude05)
        def placeLongitude05 = new Float (parent.myLongitude05)
        def placeRadius05 = new Float (parent.myRadius05)
        def distanceAway05 = haversine(memberLatitude, memberLongitude, placeLatitude05, placeLongitude05)*1000 // in meters
  	    boolean isPresent05 = (distanceAway05 <= placeRadius05)
        if(isPresent05) state.address1Value = parent.myName05
        if(logEnable) log.debug "In whereAmI - Distance Away 05 (${parent.myName05}): ${distanceAway05}, isPresent05: ${isPresent05}"
    }
    if(parent.myName06) {
        def placeLatitude06 = new Float (parent.myLatitude06)
        def placeLongitude06 = new Float (parent.myLongitude06)
        def placeRadius06 = new Float (parent.myRadius06)
        def distanceAway06 = haversine(memberLatitude, memberLongitude, placeLatitude06, placeLongitude06)*1000 // in meters
  	    boolean isPresent06 = (distanceAway06 <= placeRadius06)
        if(isPresent06) state.address1Value = parent.myName06
        if(logEnable) log.debug "In whereAmI - Distance Away 06 (${parent.myName06}): ${distanceAway06}, isPresent06: ${isPresent06}"
    }
    if(parent.myName07) {
        def placeLatitude07 = new Float (parent.myLatitude07)
        def placeLongitude07 = new Float (parent.myLongitude07)
        def placeRadius07 = new Float (parent.myRadius07)
        def distanceAway07 = haversine(memberLatitude, memberLongitude, placeLatitude07, placeLongitude07)*1000 // in meters
  	    boolean isPresent07 = (distanceAway07 <= placeRadius07)
        if(isPresent07) state.address1Value = parent.myName07
        if(logEnable) log.debug "In whereAmI - Distance Away 07 (${parent.myName07}): ${distanceAway07}, isPresent07: ${isPresent07}"
    }
    if(parent.myName08) {
        def placeLatitude08 = new Float (parent.myLatitude08)
        def placeLongitude08 = new Float (parent.myLongitude08)
        def placeRadius08 = new Float (parent.myRadius08)
        def distanceAway08 = haversine(memberLatitude, memberLongitude, placeLatitude08, placeLongitude08)*1000 // in meters
  	    boolean isPresent08 = (distanceAway08 <= placeRadius08)
        if(isPresent08) state.address1Value = parent.myName08
        if(logEnable) log.debug "In whereAmI - Distance Away 08 (${parent.myName08}): ${distanceAway08}, isPresent08: ${isPresent08}"
    }
    if(parent.myName09) {
        def placeLatitude09 = new Float (parent.myLatitude09)
        def placeLongitude09 = new Float (parent.myLongitude09)
        def placeRadius09 = new Float (parent.myRadius09)
        def distanceAway09 = haversine(memberLatitude, memberLongitude, placeLatitude09, placeLongitude09)*1000 // in meters
  	    boolean isPresent09 = (distanceAway09 <= placeRadius09)
        if(isPresent09) state.address1Value = parent.myName09
        if(logEnable) log.debug "In whereAmI - Distance Away 09 (${parent.myName09}): ${distanceAway09}, isPresent09: ${isPresent09}"
    }
    if(parent.myName10) {
        def placeLatitude10 = new Float (parent.myLatitude10)
        def placeLongitude10 = new Float (parent.myLongitude10)
        def placeRadius10 = new Float (parent.myRadius10)
        def distanceAway10 = haversine(memberLatitude, memberLongitude, placeLatitude10, placeLongitude10)*1000 // in meters
  	    boolean isPresent10 = (distanceAway10 <= placeRadius10)
        if(isPresent10) state.address1Value = parent.myName10
        if(logEnable) log.debug "In whereAmI - Distance Away 10 (${parent.myName10}): ${distanceAway10}, isPresent10: ${isPresent10}"
    }
    if(logEnable) log.debug "In whereAmI - Finished - Going back to userHandler"
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
    if(logEnable) log.debug "In buildMyPlacesList (${state.version})"
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
    if(logEnable) log.debug "In buildMyPlacesList - myPlacesList: ${state.myPlacesList}"
}

// ********** Normal Stuff **********

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable == null) {logEnable = false}
    if(state.address1Value == null) {state.address1Value = presenceDevice.currentValue("address1")}
    if(state.address1Prev == null) {state.address1Prev = presenceDevice.currentValue("address1prev")}
    if(trackSpecific2 == null) {trackSpecific2 = "NoData"}
    if(state.previousMessage == null) state.previousMessage = "abcdefghijkl"
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
