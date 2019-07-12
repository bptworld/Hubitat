/**
 *  ****************  Life360 Tracker Free Child App  ****************
 *
 *  Design Usage:
 *  Track your Life360 users. Works with the free version of Life360 and the Life360 with States app.
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
 *  V1.0.8 - 07/12/19 - Removed 'How often to track Places'. Lots of changes to arrived/moving/departed.
 *  V1.0.7 - 07/11/19 - Added code for dashboard tiles, Info and Places tiles. Complete rewrite for arrived/moving/departed. Removed tracking all.
 *  V1.0.6 - 07/09/19 - Still trying to get push only working correctly!
 *  V1.0.5 - 07/09/19 - Lots of changes to arrived/departed and on the move sections. Fixed push only.
 *  V1.0.4 - 07/09/19 - Select places now shows a list to choose from
 *  V1.0.3 - 07/09/19 - Another minor change to departed
 *  V1.0.2 - 07/08/19 - Fixed another typo with Departed.  Also added 'time to be considered gone'
 *  V1.0.1 - 07/08/19 - Fix typo speakHasDeparted vs speakHasDeparted (thanks spalexander68!)
 *  V1.0.0 - 07/07/19 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.8"
}

definition(
    name: "Life360 Tracker Free Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track your Life360 users. Works with the free version of Life360 and the Life360 with States app.",
    category: "Convenience",
	parent: "BPTWorld:Life360 Tracker",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Life360%20Tracker/LTF-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "myPlacesPage")
    page(name: "alertsConfig")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Tracker Free</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Track your Life360 users. Works with the free version of Life360 and the Life360 with States app."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Device")) {
			input "presenceDevice", "capability.presenceSensor", title: "Select Life360 User Device", required: true
            input "friendlyName", "text", title: "Friendly Name used in messages for this Device", required: true, submitOnChange: "true"
		}
    if(presenceDevice) {
        section(getFormat("header-green", "${getImage("Blank")}"+" My Places")) {
        	href "myPlacesPage", title: "My Places", description: "Click here to create custom Places"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Place Tracking")) {
            paragraph "This will track the coming and going of places."
            buildMyPlacesList()
            def thePlaces = "${state.myPlacesList}".replace("[","").replace("]","").replace("]","").replace(" ,", ",").replace(", ", ",")
            state.values = "${thePlaces}".split(",")
            input "trackSpecific", "enum", title:"Life360 Places", options: state.values, multiple: true, required:false, submitOnChange: true
            input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of Specific Places?", description: "List View", submitOnChange: "true")
            if(oG1List) {
			    def valuesG1 = "${trackSpecific}".split(";")
			    listMapG1 = ""
    			valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
				paragraph "${listMapG1}".replace("[","").replace("]","")
			}
            input "timeConsideredHere", "number", title: "Time to be considered at a Place (in Minutes, range 2 to 10)", required: true, submitOnChange: true, defaultValue: 2, range: '2..10'
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
			paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a device<br>%place% - returns the place arrived or departed"
            paragraph "* PLUS - all attribute names can be used as wildcards! Just make sure the name is exact, capitalization counts!  ie. %powerSource%, %distanceMiles% or %wifiState%"
            input(name: "speakHasArrived", type: "bool", defaultValue: "false", title: "Speak when someone 'Has arrived'", description: "Speak Has Arrived", submitOnChange: true)
            input(name: "pushHasArrived", type: "bool", defaultValue: "false", title: "Push when someone 'Has arrived'", description: "Push Has Arrived", submitOnChange: true)
			if(speakHasArrived || pushHasArrived) input "messageAT", "text", title: "Random Message to be spoken when <b>'has arrived'</b> at a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has arrived at %place%"
			if(speakHasArrived || pushHasArrived) input(name: "atMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if((speakHasArrived || pushHasArrived) && atMsgList) {
				def values = "${messageAT}".split(";")
				listMapAT = ""
    			values.each { item -> listMapAT += "${item}<br>"}
				paragraph "${listMapAT}"
			}
            if(speakHasArrived || pushHasArrived) paragraph "<hr>"
            
            
            input(name: "speakHasDeparted", type: "bool", defaultValue: "false", title: "Speak when someone 'Has departed'", description: "Speak Has departed", submitOnChange: true)
            input(name: "pushHasDeparted", type: "bool", defaultValue: "false", title: "Push when someone 'Has departed'", description: "Push Has departed", submitOnChange: true)
			if(speakHasDeparted || pushHasDeparted) input "messageDEP", "text", title: "Random Message to be spoken when <b>'has departed'</b> a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% has departed from %place%"
			if(speakHasDeparted || pushHasDeparted) input(name: "depMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if((speakHasDeparted || pushHasDeparted) && depMsgList) {
				def values = "${messageDEP}".split(";")
				listMapDEP = ""
    			values.each { item -> listMapDEP += "${item}<br>"}
                paragraph "${listMapDEP}"
			}
            if(speakHasDeparted || pushHasDeparted) paragraph "<hr>"
            
            
            input(name: "speakOnTheMove", type: "bool", defaultValue: "false", title: "Speak when someone 'is on the move'", description: "Speak On the Move", submitOnChange: true)
            input(name: "pushOnTheMove", type: "bool", defaultValue: "false", title: "Push when someone 'is on the move'", description: "Push On the Move", submitOnChange: true)
            if(speakOnTheMove || pushOnTheMove) input "messageMOVE", "text", title: "Random Message to be spoken when <b>'on the move'</b> near a place - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true, defaultValue: "%name% is on the move near %place%"
			if(speakOnTheMove || pushOnTheMove) input(name: "moveMsgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			if((speakOnTheMove || pushOnTheMove) && moveMsgList) {
				def values = "${messageMOVE}".split(";")
				listMapMove = ""
    			values.each { item -> listMapMove += "${item}<br>"}
				paragraph "${listMapMove}"
			}
            if(speakOnTheMove || pushOnTheMove) paragraph "<hr>"
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
//        section(getFormat("header-green", "${getImage("Blank")}"+" Extra Options")) {           
//            href "alertsConfig", title: "Alerts", description: "Click here to setup Alerts."
//		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
    }
		display2()
	}
}

def myPlacesPage() {
    dynamicPage(name: "myPlacesPage", title: "<h2 style='color:#1A77C9;font-weight: bold'>My Places</h2>", nextPage: "pageConfig", install: false, uninstall: false) {
        section(getFormat("header-green", "${getImage("Blank")}"+" My Places")) {
            paragraph "Use any service you like to find the Lat and Long for the place you want to save.<br>ie. <a href='https://www.latlong.net' target='_blank'>www.latlong.net</a>"

            input "myName01", "text", title: "<b>My Place Name 01</b>", required: false, submitOnChange: true
            if(myName01) {
                input "myLatitude01", "text", title: "My Latitude 01", required: true, width: 4
                input "myLongitude01", "text", title: "My Longitude 01", required: true, width: 4
                input "myRadius01", "text", title: "My Radius 01", required: true, defaultValue: 250, width: 4
            }
            input "myName02", "text", title: "<b>My Place Name 02</b>", required: false, submitOnChange: true
            if(myName02) {
                input "myLatitude02", "text", title: "My Latitude 02", required: true, width: 4
                input "myLongitude02", "text", title: "My Longitude 02", required: true, width: 4
                input "myRadius02", "text", title: "My Radius 02", required: true, defaultValue: 250, width: 4
            }
            input "myName03", "text", title: "<b>My Place Name 03</b>", required: false, submitOnChange: true
            if(myName03) {
                input "myLatitude03", "text", title: "My Latitude 03", required: true, width: 4
                input "myLongitude03", "text", title: "My Longitude 03", required: true, width: 4
                input "myRadius03", "text", title: "My Radius 03", required: true, defaultValue: 250, width: 4
            }
            input "myName04", "text", title: "<b>My Place Name 04</b>", required: false, submitOnChange: true
            if(myName04) {
                input "myLatitude04", "text", title: "My Latitude 04", required: true, width: 4
                input "myLongitude04", "text", title: "My Longitude 04", required: true, width: 4
                input "myRadius04", "text", title: "My Radius 04", required: true, defaultValue: 250, width: 4
            }
            input "myName05", "text", title: "<b>My Place Name 05</b>", required: false, submitOnChange: true
            if(myName05) {
                input "myLatitude05", "text", title: "My Latitude 05", required: true, width: 4
                input "myLongitude05", "text", title: "My Longitude 05", required: true, width: 4
                input "myRadius05", "text", title: "My Radius 05", required: true, defaultValue: 250, width: 4
            }
            if(myName06) {
                input "myLatitude06", "text", title: "My Latitude 06", required: true, width: 4
                input "myLongitude06", "text", title: "My Longitude 06", required: true, width: 4
                input "myRadius06", "text", title: "My Radius 06", required: true, defaultValue: 250, width: 4
            }
            if(myName07) {
                input "myLatitude07", "text", title: "My Latitude 07", required: true, width: 4
                input "myLongitude07", "text", title: "My Longitude 07", required: true, width: 4
                input "myRadius07", "text", title: "My Radius 07", required: true, defaultValue: 250, width: 4
            }
            if(myName08) {
                input "myLatitude08", "text", title: "My Latitude 08", required: true, width: 4
                input "myLongitude08", "text", title: "My Longitude 08", required: true, width: 4
                input "myRadius08", "text", title: "My Radius 08", required: true, defaultValue: 250, width: 4
            }
            if(myName09) {
                input "myLatitude09", "text", title: "My Latitude 09", required: true, width: 4
                input "myLongitude09", "text", title: "My Longitude 09", required: true, width: 4
                input "myRadius09", "text", title: "My Radius 09", required: true, defaultValue: 250, width: 4
            }
            if(myName10) {
                input "myLatitude10", "text", title: "My Latitude 10", required: true, width: 4
                input "myLongitude10", "text", title: "My Longitude 10", required: true, width: 4
                input "myRadius10", "text", title: "My Radius 10", required: true, defaultValue: 250, width: 4
            }
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
    subscribe(presenceDevice, "lastLocationUpdate", whereAmI)
}

def whereAmI() {
    state.address1Value = presenceDevice.currentValue("address1")
    def memberLatitude = new Float (presenceDevice.currentValue("latitude"))
    def memberLongitude = new Float (presenceDevice.currentValue("longitude"))
    
    if(myName01) {
        def placeLatitude01 = new Float (settings.myLatitude01)
        def placeLongitude01 = new Float (settings.myLongitude01)
        def placeRadius01 = new Float (settings.myRadius01)
        def distanceAway01 = haversine(memberLatitude, memberLongitude, placeLatitude01, placeLongitude01)*1000 // in meters
      	boolean isPresent01 = (distanceAway01 <= placeRadius01)
        if(isPresent01) state.address1Value = myName01
        if(logEnable) log.debug "Distance Away 01 (${myName01}): ${distanceAway01}, isPresent01: ${isPresent01}"
    }
    if(myName02) {
        def placeLatitude02 = new Float (settings.myLatitude02)
        def placeLongitude02 = new Float (settings.myLongitude02)
        def placeRadius02 = new Float (settings.myRadius02)
        def distanceAway02 = haversine(memberLatitude, memberLongitude, placeLatitude02, placeLongitude02)*1000 // in meters
  	    boolean isPresent02 = (distanceAway02 <= placeRadius02)
        if(isPresent02) state.address1Value = myName02
        if(logEnable) log.debug "Distance Away 02 (${myName02}): ${distanceAway02}, isPresent02: ${isPresent02}"
    }
    if(myName03) {
        def placeLatitude03 = new Float (settings.myLatitude03)
        def placeLongitude03 = new Float (settings.myLongitude03)
        def placeRadius03 = new Float (settings.myRadius03)
        def distanceAway03 = haversine(memberLatitude, memberLongitude, placeLatitude03, placeLongitude03)*1000 // in meters
  	    boolean isPresent03 = (distanceAway03 <= placeRadius03)
        if(isPresent03) state.address1Value = myName03
        if(logEnable) log.debug "Distance Away 03 (${myName03}): ${distanceAway03}, isPresent03: ${isPresent03}"
    }
    if(myName04) {
        def placeLatitude04 = new Float (settings.myLatitude04)
        def placeLongitude04 = new Float (settings.myLongitude04)
        def placeRadius04 = new Float (settings.myRadius04)
        def distanceAway04 = haversine(memberLatitude, memberLongitude, placeLatitude04, placeLongitude04)*1000 // in meters
  	    boolean isPresent04 = (distanceAway04 <= placeRadius04)
        if(isPresent04) state.address1Value = myName04
        if(logEnable) log.debug "Distance Away 04 (${myName04}): ${distanceAway04}, isPresent04: ${isPresent04}"
    }
    if(myName05) {
        def placeLatitude05 = new Float (settings.myLatitude05)
        def placeLongitude05 = new Float (settings.myLongitude05)
        def placeRadius05 = new Float (settings.myRadius05)
        def distanceAway05 = haversine(memberLatitude, memberLongitude, placeLatitude05, placeLongitude05)*1000 // in meters
  	    boolean isPresent05 = (distanceAway05 <= placeRadius05)
        if(isPresent05) state.address1Value = myName05
        if(logEnable) log.debug "Distance Away 05 (${myName05}): ${distanceAway05}, isPresent05: ${isPresent05}"
    }
    if(myName06) {
        def placeLatitude06 = new Float (settings.myLatitude06)
        def placeLongitude06 = new Float (settings.myLongitude06)
        def placeRadius06 = new Float (settings.myRadius06)
        def distanceAway06 = haversine(memberLatitude, memberLongitude, placeLatitude06, placeLongitude06)*1000 // in meters
  	    boolean isPresent06 = (distanceAway06 <= placeRadius06)
        if(isPresent06) state.address1Value = myName06
        if(logEnable) log.debug "Distance Away 06 (${myName06}): ${distanceAway06}, isPresent06: ${isPresent06}"
    }
    if(myName07) {
        def placeLatitude07 = new Float (settings.myLatitude07)
        def placeLongitude07 = new Float (settings.myLongitude07)
        def placeRadius07 = new Float (settings.myRadius07)
        def distanceAway07 = haversine(memberLatitude, memberLongitude, placeLatitude07, placeLongitude07)*1000 // in meters
  	    boolean isPresent07 = (distanceAway07 <= placeRadius07)
        if(isPresent07) state.address1Value = myName07
        if(logEnable) log.debug "Distance Away 07 (${myName07}): ${distanceAway07}, isPresent07: ${isPresent07}"
    }
    if(myName08) {
        def placeLatitude08 = new Float (settings.myLatitude08)
        def placeLongitude08 = new Float (settings.myLongitude08)
        def placeRadius08 = new Float (settings.myRadius08)
        def distanceAway08 = haversine(memberLatitude, memberLongitude, placeLatitude08, placeLongitude08)*1000 // in meters
  	    boolean isPresent08 = (distanceAway08 <= placeRadius08)
        if(isPresent08) state.address1Value = myName08
        if(logEnable) log.debug "Distance Away 08 (${myName08}): ${distanceAway08}, isPresent08: ${isPresent08}"
    }
    if(myName09) {
        def placeLatitude09 = new Float (settings.myLatitude09)
        def placeLongitude09 = new Float (settings.myLongitude09)
        def placeRadius09 = new Float (settings.myRadius09)
        def distanceAway09 = haversine(memberLatitude, memberLongitude, placeLatitude09, placeLongitude09)*1000 // in meters
  	    boolean isPresent09 = (distanceAway09 <= placeRadius09)
        if(isPresent09) state.address1Value = myName09
        if(logEnable) log.debug "Distance Away 09 (${myName09}): ${distanceAway09}, isPresent09: ${isPresent09}"
    }
    if(myName10) {
        def placeLatitude10 = new Float (settings.myLatitude10)
        def placeLongitude10 = new Float (settings.myLongitude10)
        def placeRadius10 = new Float (settings.myRadius10)
        def distanceAway10 = haversine(memberLatitude, memberLongitude, placeLatitude10, placeLongitude10)*1000 // in meters
  	    boolean isPresent10 = (distanceAway10 <= placeRadius10)
        if(isPresent10) state.address1Value = myName10
        if(logEnable) log.debug "Distance Away 10 (${myName10}): ${distanceAway10}, isPresent10: ${isPresent10}"
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

def userHandler(evt) {
    if(logEnable) log.debug "In userHandler..."
    state.address1Value = presenceDevice.currentValue("address1")
    getTimeDiff()
    int timeHere = timeConsideredHere * 60
    // reset speaking
    state.speakAT = "no"
    state.speakDEP = "no"
    state.speakMOVE = "no"
    state.msg = ""
    
    if(state.address1Value == state.prevPlace) {
        if(logEnable) log.debug "In userHandler - address1: ${state.address1Value} MATCHES state.prevPlace: ${state.prevPlace}"
        if(trackSpecific.contains(state.address1Value)) {
            if(state.tDiff > timeHere) {
                if(logEnable) log.debug "In userHandler - Time at Place: ${state.tDiff} IS greater than: ${timeHere}"
                if(state.beenHere == "no") {
                    if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} has arrived at ${state.address1Value}"
                    state.msg = "${messageAT}"
                    state.speakAT = "yes"
                    state.lastAtPlace = state.address1Value
                    if(speakHasArrived || pushHasArrived) messageHandler()
                } else {
                    if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} has been at ${state.address1Value} for ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
                }
                if(isDataDevice) isDataDevice.on()
                state.prevPlace = state.address1Value
                state.beenHere = "yes"
                state.onTheMove = "no"
                if(logEnable) log.debug "In userHandler - state.tDiff > timeHere - TRUE - beenHere: ${state.beenHere} - onTheMove: ${state.onTheMove}"
            } else {  // *** ! state.tDiff > timeHere ***
                if(logEnable) log.debug "In userHandler - Time at Place: ${state.tDiff} IS NOT greater than: ${timeHere}"
                if(isDataDevice) isDataDevice.off()
                state.prevPlace = state.address1Value
                state.beenHere = "no"
                state.onTheMove = "yes"
                if(logEnable) log.debug "In userHandler - state.tDiff > timeHere - FALSE - beenHere: ${state.beenHere} - onTheMove: ${state.onTheMove}"
            }
        } else {  // *** ! trackSpecific.contains(state.address1Value) ***
		    if(logEnable) log.debug "In userHandler - Track Specific - ${friendlyName} is not at a place this app is tracking ${state.address1Value}"
            state.prevPlace = state.address1Value
            if(isDataDevice) isDataDevice.off()
            state.beenHere = "no"
            state.onTheMove = "yes"
            if(logEnable) log.debug "In userHandler - trackSpecific.contains(state.address1Value) - FALSE - beenHere: ${state.beenHere} - onTheMove: ${state.onTheMove}"
        }
    } else {  // *** ! state.address1Value == state.prevPlace ***
        if(logEnable) log.debug "In userHandler - address1: ${state.address1Value} DOES NOT MATCH state.prevPlace: ${state.prevPlace}"
        if(state.beenHere == "yes") {
            if(logEnable) log.debug "In userHandler - ${friendlyName} has departed from ${state.lastAtPlace}"
            state.msg = "${messageDEP}"
            state.speakDEP = "yes"
            if(speakHasDeparted || pushDeparted) messageHandler()
        } else {
            if(logEnable) log.debug "In userHandler - ${friendlyName} is on the move near ${state.address1Value}"
            state.msg = "${messageMOVE}"
            state.speakMOVE = "yes"
            if(speakOnTheMove || pushOnTheMove) messageHandler()
        }
        state.prevPlace = state.address1Value
        if(isDataDevice) isDataDevice.off()
        state.beenHere = "no"
        state.onTheMove = "yes"
        if(logEnable) log.debug "In userHandler - trackSpecific.contains(state.address1Value) - Departed/Move - beenHere: ${state.beenHere} - onTheMove: ${state.onTheMove}"
    } 
}

def getTimeDiff() {
	if(logEnable) log.debug "In getTimeDiff..."
	long since = presenceDevice.currentValue("since")
   	def now = new Date()
    long unxNow = now.getTime()
    unxNow = unxNow/1000    
    long timeDiff = Math.abs(unxNow-since)
    state.tDiff = timeDiff
    if(logEnable) log.debug "In getTimeDiff - since: ${since}, Now: ${unxNow}, Diff: ${timeDiff}"
    
	state.timeDay = (timeDiff / 86400).toInteger()
    state.timeHrs = ((timeDiff % 86400 ) / 3600).toInteger()
	state.timeMin = (((timeDiff % 86400 ) % 3600 ) / 60).toInteger()
	state.timeSec = (((timeDiff % 86400 ) % 3600 ) % 60).toInteger()
    
    if(logEnable) log.debug "In getTimeDiff - Time Diff: ${state.timeDay} days, ${state.timeHrs} hrs, ${state.timeMin} mins & ${state.timeSec} secs"
}

def letsTalk() {
	if(logEnable) log.debug "In letsTalk..."
	checkTime()
	checkVol()
	atomicState.randomPause = Math.abs(new Random().nextInt() % 1500) + 400
	if(logEnable) log.debug "In letsTalk - pause: ${atomicState.randomPause}"
	pauseExecution(atomicState.randomPause)
	if(logEnable) log.debug "In letsTalk - continuing"
	if(state.timeBetween == true) {
		state.theMsg = "${state.theMessage}"
    	if(logEnable) log.debug "In letsTalk - speaker: ${speaker}, vol: ${state.volume}, msg: ${state.theMsg}, volRestore: ${volRestore}"
        speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
        atomicState.speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
            state.speakers.each {
                if(logEnable) log.debug "Speaker in use: ${it}"
                if(speakerProxy) {
                    if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                    it.speak(state.theMsg)
                } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                    if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                    def prevVolume = it.currentValue("volume")
                    it.setVolumeSpeakAndRestore(state.volume, state.theMsg, prevVolume)
                } else if(it.hasCommand('playTextAndRestore')) {   
                    if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    def prevVolume = it.currentValue("volume")
                    it.playTextAndRestore(state.theMsg, prevVolume)
                } else {		        
                    if(logEnable) log.debug "In letsTalk - ${it}"
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
	    if(logEnable) log.debug "In letsTalk - that's it!"  
		log.info "${app.label} - ${state.theMsg}"
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
}

def checkVol(){
	if(logEnable) log.debug "In checkVol..."
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler..."
	message = state.msg
    
	def values = "${message}".split(";")
	vSize = values.size()
	count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMessage = values[randomKey]
    
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, theMessage: ${theMessage}" 
	if(theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', friendlyName )}
    if(theMessage.contains("%place%")) {theMessage = theMessage.replace('%place%', state.address1Value )}
    if(theMessage.contains("%address1%")) {theMessage = theMessage.replace('%address1%', presenceDevice.currentValue("address1") )}
    if(theMessage.contains("%address2%")) {theMessage = theMessage.replace('%address2%', presenceDevice.currentValue("address2") )}
    if(theMessage.contains("%battery%")) {theMessage = theMessage.replace('%battery%', presenceDevice.currentValue("battery") )}
    if(theMessage.contains("%charge%")) {theMessage = theMessage.replace('%charge%', presenceDevice.currentValue("charge") )}
    if(theMessage.contains("%distanceKm%")) {theMessage = theMessage.replace('%distanceKm%', presenceDevice.currentValue("distanceKm") )}
    if(theMessage.contains("%distanceMetric%")) {theMessage = theMessage.replace('%distanceMetric%', presenceDevice.currentValue("distanceMetric") )}
    if(theMessage.contains("%distanceMiles%")) {theMessage = theMessage.replace('%distanceMiles%', presenceDevice.currentValue("distanceMiles") )}
    if(theMessage.contains("%inTransit%")) {theMessage = theMessage.replace('%inTransit%', presenceDevice.currentValue("inTransit") )}
    if(theMessage.contains("%isDriving%")) {theMessage = theMessage.replace('%isDriving%', state.presenceDevice.currentValue("isDriving") )}
    if(theMessage.contains("%lastCheckin%")) {theMessage = theMessage.replace('%lastCheckin%', state.presenceDevice.currentValue("lastCheckin") )}
    if(theMessage.contains("%latitude%")) {theMessage = theMessage.replace('%latitude%', state.presenceDevice.currentValue("latitude") )}
    if(theMessage.contains("%longitude%")) {theMessage = theMessage.replace('%longitude%', state.presenceDevice.currentValue("longitude") )}
    if(theMessage.contains("%powerSource%")) {theMessage = theMessage.replace('%powerSource%', state.presenceDevice.currentValue("powerSource") )}
    if(theMessage.contains("%presence%")) {theMessage = theMessage.replace('%presence%', state.presenceDevice.currentValue("presence") )}
    if(theMessage.contains("%speedKm%")) {theMessage = theMessage.replace('%speedKm%', state.presenceDevice.currentValue("speedKm") )}
    if(theMessage.contains("%speedMetric%")) {theMessage = theMessage.replace('%speedMetric%', state.presenceDevice.currentValue("speedMetric") )}
    if(theMessage.contains("%speedMiles%")) {theMessage = theMessage.replace('%speedMiles%', state.presenceDevice.currentValue("speedMiles") )}
    if(theMessage.contains("%wifiState%")) {theMessage = theMessage.replace('%wifiState%', state.presenceDevice.currentValue("wifiState") )}
    if(theMessage.contains("%display%")) {theMessage = theMessage.replace('%display%', state.presenceDevice.currentValue("display") )}
    if(theMessage.contains("%status%")) {theMessage = theMessage.replace('%status%', state.presenceDevice.currentValue("status") )}
    if(theMessage.contains("%lastLocationUpdate%")) {theMessage = theMessage.replace('%lastLocationUpdate%', state.presenceDevice.currentValue("lastLocationUpdate") )}
	state.theMessage = "${theMessage}"
	presenceDevice.sendHistory(theMessage)
    if((speakHasArrived) && (state.speakAT == "yes")) letsTalk()
    if((speakHasDeparted)  && (state.speakDEP == "yes")) letsTalk()
    if((speakOnTheMove) && (state.speakMOVE == "yes")) letsTalk()
    if((pushHasArrived) && (state.speakAT == "yes")) pushHandler()
    if((pushHasDeparted) && (state.speakDEP == "yes")) pushHandler()
    if((pushOnTheMove) && (state.speakMOVE == "yes")) pushHandler()
    log.info "HISTORY - ${theMessage}"
}

def pushHandler() {
	if(logEnable) log.debug "In pushNow..."
    theMessage = "${state.theMessage}\n\n"
    if(linkPush) {theMessage += "https://www.google.com/maps/search/?api=1&query=${presenceDevice.currentValue("latitude")},${presenceDevice.currentValue("longitude")}"}
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler - Button Pressed: ${state.whichButton}"
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

def buildMyPlacesList() {
    if(logEnable) log.debug "In buildMyPlacesList..."
    state.myPlacesList = []
    if(myName01) state.myPlacesList = state.myPlacesList.plus([myName01])
    if(myName02) state.myPlacesList = state.myPlacesList.plus([myName02])
    if(myName03) state.myPlacesList = state.myPlacesList.plus([myName03])
    if(myName04) state.myPlacesList = state.myPlacesList.plus([myName04])
    if(myName05) state.myPlacesList = state.myPlacesList.plus([myName05])
    if(myName06) state.myPlacesList = state.myPlacesList.plus([myName06])
    if(myName07) state.myPlacesList = state.myPlacesList.plus([myName07])
    if(myName08) state.myPlacesList = state.myPlacesList.plus([myName08])
    if(myName09) state.myPlacesList = state.myPlacesList.plus([myName09])
    if(myName10) state.myPlacesList = state.myPlacesList.plus([myName10])
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    if(state.beenHere == null){state.beenHere = "no"}
    if(state.address1Value == null){state.address1Value = presenceDevice.currentValue("address1")}
    if(state.prevPlace == null){state.prevPlace = presenceDevice.currentValue("address1")}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Life360 Tracker Free - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
