/**
 *  ****************  Home Tracker Child App  ****************
 *
 *  Design Usage:
 *  Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums to let
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.2 - 09/14/19 - Tried to make the opening and closing sections more clear. Added green check marks to sections that are
 *    filled out. Found a couple of typos.
 *  V2.0.1 - 09/13/19 - Adjusted message sections
 *  V2.0.0 - 09/10/19 - Combined Welcome Home, Departures and Arrivals. Major rewrite of all models.
 *
 */

import groovy.time.TimeCategory
import hubitat.helper.RMUtils

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "HomeTrackerChildVersion"
	state.version = "v2.0.1"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Home Tracker Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!",
    category: "",
	parent: "BPTWorld:Home Tracker",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Home%20Tracker/HT-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "presenceOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "messageOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "welcomeHomeOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "doorLockOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "ruleMachineOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Home Tracker</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
            paragraph "Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message <i>after</i> entering the home!"
        	paragraph "<b>Type of 'Welcome Home' Triggers:</b>"
    		paragraph "<b>Unlock or Door Open</b><br>Both of these work pretty much the same. When door or lock is triggered, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement."
			paragraph "Each trigger can have multiple selections but this is an 'or' function. Meaning it only takes one device to trigger the actions. ie. Door1 or Door2 has been opened. If you require a different delay per door/lock, then separate child apps would be required - one for each door or lock."
			paragraph "<b>Motion Sensor</b><br>When motion sensor becomes active, it will check to see which presence sensors have recently become 'present' within your set time. The system will then wait your set delay before making the announcement. If you require a different delay per motion sensor, then separate child apps would be required - one for each motion sensor."
			paragraph "This trigger also works with Hubitat's built in 'Zone Motion Controllers' app. Which allows you to do some pretty cool things with motion sensors."
			paragraph "<b>Requirements:</b>"
			paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Options, Options, Options")) {
            if(ps1a || ps2a || ps3a || ps4a || ps5a || ps6a || ps7a || ps8a || ps9a || ps10a || ps11a || ps12a || ps13a || ps14a || ps15a || ps16a || ps17a || ps18a || ps19a || ps20a) {
                href "presenceOptions", title:"${getImage("checkMarkGreen")} Presence Options", description:"Click here to setup the Presence Options"
            } else {
                href "presenceOptions", title:"Presence Options", description:"Click here to setup the Presence Options"
            }   
            if(omessage || omessageD || omessageHN) {
                href "messageOptions", title:"${getImage("checkMarkGreen")} Message Options", description:"Click here to setup the Message Options"
            } else {
                href "messageOptions", title:"Message Options", description:"Click here to setup the Message Options"
            }
            if(speakerMP || speakerSS) {
                href "speechOptions", title:"${getImage("checkMarkGreen")} Notification Options", description:"Click here to setup the Notification Options"
            } else {
                href "speechOptions", title:"Notification Options", description:"Click here to setup the Notification Options"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Special Features")) {
            if(triggerMode) {
                href "welcomeHomeOptions", title:"${getImage("checkMarkGreen")} Welcome Home Options", description:"Click here to setup the Welcome Home Options"
            } else {
                href "welcomeHomeOptions", title:"Welcome Home Options", description:"Click here to setup the Welcome Home Options"
            }
    	    if(theLocks) {
                href "doorLockOptions", title:"${getImage("checkMarkGreen")} Door Lock Options", description:"Click here to setup the Door Lock Options"
            } else {
                href "doorLockOptions", title:"Door Lock Options", description:"Click here to setup the Door Lock Options"
            }
            
            href "ruleMachineOptions", title:"Rule Machine Options", description:"Click here to setup the Rule Machine Options"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Global Variables")) {
			paragraph "This app requires a 'virtual device' to send variables between child apps. This is to prevent multiple announcements.<br>ie. Person A comes home and enters door 1, walks through the house and opens door 2 to let the dogs out.  We only want one 'Welcome Home' message to be played."
			paragraph "* Vitual Device must use our custom 'Welcome Home Driver'"
			input "gvDevice", "capability.actuator", title: "Virtual Device created for Welcome Home", required: false, multiple: false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging")
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
			paragraph "In case the Queue gets stuck, flip this switch to clear the queue."
            input(name: "clearQueue", type: "bool", defaultValue: "false", title: "Clear the Queue", description: "Clear", submitOnChange: "true")
            if(clearQueue) {
			    app?.updateSetting("clearQueue",[value:"false",type:"bool"])
				if(logEnable) log.debug "Resetting the Queue"
                state.TTSQueue = []
				state.playingTTS = false
			}
        }
		display2()
	}
}

def presenceOptions(){
    dynamicPage(name: "presenceOptions", title: "Presence Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Presence Home NOW Options")) { 
			input(name: "homeNow", type: "bool", defaultValue: "false", title: "Announce when a presence sensor arrives at 'Home', with NO wait? (off='No', on='Yes')", description: "Home Instant", submitOnChange: "true")
            paragraph "<small>This will give a heads up that someone is home. But can be a false alarm if they are just driving by.</small>" 
        }

		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Home DELAYED Options")) { 
			paragraph "If a presence sensor has been detected for less than x minutes (set the minutes below), after the trigger, then speak the message."
			paragraph "Note: If you are not seeing your 'Friendly Names', then go back to the parent app, enter them in and hit 'done' before setting up any child apps."
            input "timeHome", "number", title: "How many minutes can the presence sensor be home and still be considered for a welcome home message (default=10)", required: true, defaultValue: 10  
            
			if(parent.friendlyName1 != "Not set") input(name: "ps1a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName1}", description: "PS1", submitOnChange: true)
			if(ps1a) input("presenceSensor1", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			if(parent.friendlyName2 != "Not set") input(name: "ps2a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName2}", description: "PS2", submitOnChange: true)
			if(ps2a) input("presenceSensor2", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			if(parent.friendlyName3 != "Not set") input(name: "ps3a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName3}", description: "PS3", submitOnChange: true)
			if(ps3a) input("presenceSensor3", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			if(parent.friendlyName4 != "Not set") input(name: "ps4a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName4}", description: "PS4", submitOnChange: true)
			if(ps4a) input("presenceSensor4", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
			if(parent.friendlyName5 != "Not set") input(name: "ps5a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName5}", description: "PS5", submitOnChange: true)
			if(ps5a) input("presenceSensor5", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            
            if(parent.friendlyName6 != "Not set") input(name: "ps6a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName6}", description: "PS6", submitOnChange: true)
			if(ps6a) input("presenceSensor6", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName7 != "Not set") input(name: "ps7a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName7}", description: "PS7", submitOnChange: true)
			if(ps7a) input("presenceSensor7", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName8 != "Not set") input(name: "ps8a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName8}", description: "PS8", submitOnChange: true)
			if(ps8a) input("presenceSensor8", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName9 != "Not set") input(name: "ps9a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName9}", description: "PS9", submitOnChange: true)
			if(ps9a) input("presenceSensor9", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName10 != "Not set") input(name: "ps10a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName10}", description: "PS10", submitOnChange: true)
			if(ps10a) input("presenceSensor10", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            
            if(parent.friendlyName11 != "Not set") input(name: "ps11a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName11}", description: "PS11", submitOnChange: true)
			if(ps11a) input("presenceSensor11", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName12 != "Not set") input(name: "ps12a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName12}", description: "PS12", submitOnChange: true)
			if(ps12a) input("presenceSensor12", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName13 != "Not set") input(name: "ps13a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName13}", description: "PS13", submitOnChange: true)
			if(ps13a) input("presenceSensor13", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName14 != "Not set") input(name: "ps14a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName14}", description: "PS14", submitOnChange: true)
			if(ps14a) input("presenceSensor14", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName15 != "Not set") input(name: "ps15a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName15}", description: "PS15", submitOnChange: true)
			if(ps15a) input("presenceSensor15", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            
            if(parent.friendlyName16 != "Not set") input(name: "ps16a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName16}", description: "PS16", submitOnChange: true)
			if(ps16a) input("presenceSensor16", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName17 != "Not set") input(name: "ps17a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName17}", description: "PS17", submitOnChange: true)
			if(ps17a) input("presenceSensor17", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName18 != "Not set") input(name: "ps18a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName18}", description: "PS18", submitOnChange: true)
			if(ps18a) input("presenceSensor18", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName19 != "Not set") input(name: "ps19a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName19}", description: "PS19", submitOnChange: true)
			if(ps19a) input("presenceSensor19", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
            if(parent.friendlyName20 != "Not set") input(name: "ps20a", type: "bool", defaultValue: "false", title: "Use ${parent.friendlyName20}", description: "PS20", submitOnChange: true)
			if(ps20a) input("presenceSensor20", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Presence Departed Options")) { 
            paragraph "Please only choose one option"
            input(name: "departedNow", type: "bool", defaultValue: "false", title: "Announce when the presence sensor departs, right away? (off='No', on='Yes')", description: "Home Depart", submitOnChange: "true")
            paragraph "<small>This will give a heads up that someone has departed. As soon as it is detected.</small>"
			input(name: "departedDelayed", type: "bool", defaultValue: "false", title: "Announce when the presence sensor departs, after a 2 minute delay? (off='No', on='Yes')", description: "Delayed Home Depart", submitOnChange: "true")
            paragraph "<small>This will give a heads up that someone has departed. But help with false announcements.</small>" 
        }
    }
}

def speechOptions(){
    dynamicPage(name: "speechOptions", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
           input(name: "speakerProxy", type: "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy")
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		    paragraph "NOTE: Not all speakers can use volume controls.", width:8
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
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
    }
}

def messageOptions(){
    dynamicPage(name: "messageOptions", title: "Message Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Home NOW Message Options")) {
            paragraph "This will give a heads up that someone is home. But can be a false alarm if they are just driving by."
            paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor"
            paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. 'Welcome back %name%' + 'Nice to see you again'"
            paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
            if(homeNow) {
                input(name: "oRandomHN", type: "bool", defaultValue: "false", title: "Random Opening and Closing Messages?", description: "Random", submitOnChange: "true")
			    if(!oRandomHN) input "omessageHN", "text", title: "<b>Opening message</b> to be spoken - Single message",  required: true
                if(!oRandomHN) input "cmessageHN", "text", title: "<b>Closing message</b> to be spoken - Single message",  required: true
			    if(oRandomHN) {
				    input "omessageHN", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				    input(name: "oMsgListHN", type: "bool", defaultValue: "true", title: "Show a list view of the opening messages?", description: "List View", submitOnChange: "true")
				    if(oMsgListHN) {
				    	def ovaluesHN = "${omessageHN}".split(";")
				    	olistMapHN = ""
    				    ovaluesHN.each { item -> olistMapHN += "${item}<br>"}
					    paragraph "${olistMapHN}"
				    }
                    input "cmessageHN", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				    input(name: "cMsgListHN", type: "bool", defaultValue: "true", title: "Show a list view of the closing messages?", description: "List View", submitOnChange: "true")
				    if(cMsgListHN) {
				    	def cvaluesHN = "${cmessageHN}".split(";")
					    clistMapHN = ""
    				    cvaluesHN.each { item -> clistMapHN += "${item}<br>"}
					    paragraph "${clistMapHN}"
				    }
                }
            } else {
                paragraph "Turn this option on in the 'Presence Options' section."
            }
        }       
		section(getFormat("header-green", "${getImage("Blank")}"+" Welcome Home Message Options")) {
            paragraph "This will speak a nice 'Welcome Home' message AFTER you have entered the house."
            paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
            paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. 'Welcome back %name%' + 'Nice to see you again'"
            paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
			input(name: "oRandom", type: "bool", defaultValue: "false", title: "Random Opening and Closing Messages?", description: "Random", submitOnChange: "true")
			if(!oRandom) input "omessage", "text", title: "<b>Opening message</b> to be spoken - Single message",  required: true
            if(!oRandom) input "cmessage", "text", title: "<b>Closing message</b> to be spoken - Single message",  required: true
			if(oRandom) {
				input "omessage", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				input(name: "oMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the opening messages?", description: "List View", submitOnChange: "true")
				if(oMsgList) {
					def ovalues = "${omessage}".split(";")
					olistMap = ""
    				ovalues.each { item -> olistMap += "${item}<br>"}
					paragraph "${olistMap}"
				}
                input "cmessage", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				input(name: "cMsgList", type: "bool", defaultValue: "true", title: "Show a list view of the closing messages?", description: "List View", submitOnChange: "true")
				if(cMsgList) {
					def cvalues = "${cmessage}".split(";")
					clistMap = ""
    				cvalues.each { item -> clistMap += "${item}<br>"}
					paragraph "${clistMap}"
				}
                paragraph "<hr>"
                input "delay1", "number", title: "How many seconds from the time the trigger being activated to the announcement being made (default=10)", required: true, defaultValue: 10
			}
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Departed Message Options")) {
            paragraph "This will give a heads up that someone has departed."
            paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor"
            paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. '%name%' + 'is no longer here'"
            paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
            if(departedNow || departedDelayed) {
			    input(name: "oRandomD", type: "bool", defaultValue: "false", title: "Random Opening and Closing Messages?", description: "Random", submitOnChange: "true")
			    if(!oRandomD) input "omessageD", "text", title: "<b>Opening message</b> to be spoken - Single message",  required: true
                if(!oRandomD) input "cmessageD", "text", title: "<b>Closing message</b> to be spoken - Single message",  required: true
			    if(oRandomD) {
				    input "omessageD", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				    input(name: "oMsgListD", type: "bool", defaultValue: "true", title: "Show a list view of the opening messages?", description: "List View", submitOnChange: "true")
				    if(oMsgListD) {
				    	def ovaluesD = "${omessageD}".split(";")
				    	olistMapD = ""
    				    ovaluesD.each { item -> olistMapD += "${item}<br>"}
					    paragraph "${olistMapD}"
				    }
                    input "cmessageD", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				    input(name: "cMsgListD", type: "bool", defaultValue: "true", title: "Show a list view of the closing messages?", description: "List View", submitOnChange: "true")
				    if(cMsgListD) {
				    	def cvaluesD = "${cmessageD}".split(";")
					    clistMapD = ""
    				    cvaluesD.each { item -> clistMapD += "${item}<br>"}
					    paragraph "${clistMapD}"
				    }
                }
            } else {
                paragraph "Turn this option on in the 'Presence Options' section."
            }
        }
    }
}

def welcomeHomeOptions(){
    dynamicPage(name: "welcomeHomeOptions", title: "Welcome Home Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Welcome Home Options")) { 
            paragraph "Welcome Home is a special feature that waits for you to enter the house <i>before</i> making the announcement!  Welcoming you home with a personalized message."
            input "triggerMode", "enum", title: "Select activation Type", submitOnChange: true,  options: ["Contact_Sensor","Door_Lock","Motion_Sensor"], required: true, Multiple: false
			if(triggerMode == "Door_Lock"){
				input "lock1", "capability.lock", title: "Activate the welcome message when this door is unlocked", required: true, multiple: true
			}
			if(triggerMode == "Contact_Sensor"){
				input "contactSensor", "capability.contactSensor", title: "Activate the welcome message when this contact sensor is activated", required: true, multiple: true
				input "csOpenClosed", "enum", title: "Activate when Opened or Closed" , options: ["Open","Closed"], required: true, defaultValue: "Open"
			}
			if(triggerMode == "Motion_Sensor"){
				input "motionSensor1", "capability.motionSensor", title: "Activate the welcome message when this motion sensor is activated", required: true, multiple: true
			}
        }
    }
}

def doorLockOptions(){
    dynamicPage(name: "doorLockOptions", title: "Door Lock Options", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Lock Announcements")) {
            paragraph "This will give a heads up that someone has unlocked a door."
            input "theLocks", "capability.lock", title: "Select Locks to announce when unlocked", multiple: true, submitOnChange: true
            if(theLocks) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Lock Code<br>%door% - returns the Name of the Lock"
                input(name: "nRandomDU", type: "bool", defaultValue: "false", title: "Random 'unlocked' message?", description: "Random", submitOnChange: "true")
                if(!nRandomDU) input "nMessageDU", "text", title: "Message to be spoken - Single message",  required: true
                if(nRandomDU) {
				    input "nMessageDU", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
				    input(name: "nMsgListDU", type: "bool", defaultValue: "true", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
				    if(nMsgListDU) {
					    def nvaluesDU = "${nMessageDU}".split(";")
					    nlistMapDU = ""
    			        nvaluesDU.each { item -> nlistMapDU += "${item}<br>"}
					    paragraph "${nlistMapDU}"
                    }
				}
            }
        }       
        section(getFormat("header-green", "${getImage("Blank")}"+" Door Lock As Presence Sensor")) {
            paragraph "If a person doesn't have presence tracking available, Arrivals can be tracked by lock code."
			paragraph "Note: Lock codes must be setup in Hubitat's Lock Code Manager."
            input "myLock1", "capability.lock", title: "Activate the arrival message when this door is unlocked - Lock 1"
            input "myLock2", "capability.lock", title: "Activate the arrival message when this door is unlocked - Lock 2"
            input "myLock3", "capability.lock", title: "Activate the arrival message when this door is unlocked - Lock 3"
            input "myLock4", "capability.lock", title: "Activate the arrival message when this door is unlocked - Lock 4"
        }
    }
}

def ruleMachineOptions(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "ruleMachineOptions", title: "Rule Machine Options", install: false, uninstall:false){
        section(){
            paragraph "<b>Run a rule when certain conditions are met.</b>"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Rule Machine Options")) {
            paragraph "<br>********** <b>Coming Soon!</b> **********<br>"
            paragraph "<b>If everyone leaves...</b>"
        //    input "rmEveryoneLeaaves", "enum", title: "Select which rule actions to run", options: rules, multiple: true
            paragraph "<hr>"
            paragraph "<b>If nobody's home and someone returns...</b>"
        //    input "rmAnyoneReturns", "enum", title: "Select which rule actions to run", options: rules, multiple: true
            paragraph "<hr>"
            
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
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

	if(presenceSensor1) subscribe(presenceSensor1, "presence", presenceSensorHandler1)
	if(presenceSensor2) subscribe(presenceSensor2, "presence", presenceSensorHandler2)
	if(presenceSensor3) subscribe(presenceSensor3, "presence", presenceSensorHandler3)
	if(presenceSensor4) subscribe(presenceSensor4, "presence", presenceSensorHandler4)
	if(presenceSensor5) subscribe(presenceSensor5, "presence", presenceSensorHandler5)
    if(presenceSensor6) subscribe(presenceSensor6, "presence", presenceSensorHandler6)
    if(presenceSensor7) subscribe(presenceSensor7, "presence", presenceSensorHandler7)
    if(presenceSensor8) subscribe(presenceSensor8, "presence", presenceSensorHandler8)
    if(presenceSensor9) subscribe(presenceSensor9, "presence", presenceSensorHandler9)
    if(presenceSensor10) subscribe(presenceSensor10, "presence", presenceSensorHandler10)
    if(presenceSensor11) subscribe(presenceSensor11, "presence", presenceSensorHandler11)
    if(presenceSensor12) subscribe(presenceSensor12, "presence", presenceSensorHandler12)
    if(presenceSensor13) subscribe(presenceSensor13, "presence", presenceSensorHandler13)
    if(presenceSensor14) subscribe(presenceSensor14, "presence", presenceSensorHandler14)
    if(presenceSensor15) subscribe(presenceSensor15, "presence", presenceSensorHandler15)
    if(presenceSensor16) subscribe(presenceSensor16, "presence", presenceSensorHandler16)
    if(presenceSensor17) subscribe(presenceSensor17, "presence", presenceSensorHandler17)
    if(presenceSensor18) subscribe(presenceSensor18, "presence", presenceSensorHandler18)
    if(presenceSensor19) subscribe(presenceSensor19, "presence", presenceSensorHandler19)
    if(presenceSensor20) subscribe(presenceSensor20, "presence", presenceSensorHandler20)
    if(myLock1) subscribe(myLock1, "lock.unlocked", lockCodeHandler)
    if(myLock2) subscribe(myLock2, "lock.unlocked", lockCodeHandler)
    if(myLock3) subscribe(myLock3, "lock.unlocked", lockCodeHandler)
    if(myLock4) subscribe(myLock4, "lock.unlocked", lockCodeHandler)
    if(theLocks) subscribe(theLocks, "lock.unlocked", lockCodeHandler)
	
	if(triggerMode == "Door_Lock"){subscribe(lock1, "lock", lockHandler)}
	if(triggerMode == "Contact_Sensor"){subscribe(contactSensor, "contact", contactSensorHandler)}
	if(triggerMode == "Motion_Sensor"){subscribe(motionSensor1, "motion", motionSensorHandler)}
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def setupNewStuff() {
	if(logEnable) log.debug "In setupNewStuff - Setting up Maps"
	if(state.presenceMap == null) state.presenceMap = [:]
}
		
def presenceSensorHandler1(evt){
	state.presenceSensorValue1 = evt.value   
	if(logEnable) log.debug "In presenceSensorHandler1 - ${parent.friendlyName1} - Presence Sensor: ${state.presenceSensorValue1}"
    if(state.presenceSensorValue1 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName1} - Presence Sensor is not present - Been Here is now off."
		state.globalBH1 = "no"
		gvDevice.sendDataMap1(state.globalBH1)
        if(departedNow) {
            state.nowName = "${parent.friendlyName1}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName1}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName1} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName1}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler2(evt){
	state.presenceSensorValue2 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler2 - ${parent.friendlyName2} - Presence Sensor: ${state.presenceSensorValue2}"
    if(state.presenceSensorValue2 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName2} - Presence Sensor is not present - Been Here is now off."
		state.globalBH2 = "no"
		gvDevice.sendDataMap2(state.globalBH2)
        if(departedNow) {
            state.nowName = "${parent.friendlyName2}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName2}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName2} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName2}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler3(evt){
	state.presenceSensorValue3 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler3 - ${parent.friendlyName3} - Presence Sensor: ${state.presenceSensorValue3}"
    if(state.presenceSensorValue3 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName3} - Presence Sensor is not present - Been Here is now off."
		state.globalBH3 = "no"
		gvDevice.sendDataMap3(state.globalBH3)
        if(departedNow) {
            state.nowName = "${parent.friendlyName3}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName3}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName3} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName3}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler4(evt){
	state.presenceSensorValue4 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler4 - ${parent.friendlyName4} - Presence Sensor: ${state.presenceSensorValue4}"
    if(state.presenceSensorValue4 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName4} - Presence Sensor is not present - Been Here is now off."
		state.globalBH4 = "no"
		gvDevice.sendDataMap4(state.globalBH4)
        if(departedNow) {
            state.nowName = "${parent.friendlyName4}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName4}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName4} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName4}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler5(evt){
	state.presenceSensorValue5 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler5 - ${parent.friendlyName5} - Presence Sensor: ${state.presenceSensorValue5}"
    if(state.presenceSensorValue5 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName5} - Presence Sensor is not present - Been Here is now off."
		state.globalBH5 = "no"
		gvDevice.sendDataMap5(state.globalBH5)
        if(departedNow) {
            state.nowName = "${parent.friendlyName5}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName5}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName5} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName5}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler6(evt){
	state.presenceSensorValue6 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler6 - ${parent.friendlyName6} - Presence Sensor: ${state.presenceSensorValue6}"
    if(state.presenceSensorValue6 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName6} - Presence Sensor is not present - Been Here is now off."
		state.globalBH6 = "no"
		gvDevice.sendDataMap6(state.globalBH6)
        if(departedNow) {
            state.nowName = "${parent.friendlyName6}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName6}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName6} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName6}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler7(evt){
	state.presenceSensorValue7 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler7 - ${parent.friendlyName7} - Presence Sensor: ${state.presenceSensorValue7}"
    if(state.presenceSensorValue7 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName7} - Presence Sensor is not present - Been Here is now off."
		state.globalBH7 = "no"
		gvDevice.sendDataMap7(state.globalBH7)
        if(departedNow) {
            state.nowName = "${parent.friendlyName7}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName7}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName7} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName7}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler8(evt){
	state.presenceSensorValue8 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler8 - ${parent.friendlyName8} - Presence Sensor: ${state.presenceSensorValue8}"
    if(state.presenceSensorValue8 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName8} - Presence Sensor is not present - Been Here is now off."
		state.globalBH8 = "no"
		gvDevice.sendDataMap8(state.globalBH8)
        if(departedNow) {
            state.nowName = "${parent.friendlyName8}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName8}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName8} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName8}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler9(evt){
	state.presenceSensorValue9 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler9 - ${parent.friendlyName9} - Presence Sensor: ${state.presenceSensorValue9}"
    if(state.presenceSensorValue9 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName9} - Presence Sensor is not present - Been Here is now off."
		state.globalBH9 = "no"
		gvDevice.sendDataMap9(state.globalBH9)
        if(departedNow) {
            state.nowName = "${parent.friendlyName9}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName9}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName9} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName9}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler10(evt){
	state.presenceSensorValue10 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler10 - ${parent.friendlyName10} - Presence Sensor: ${state.presenceSensorValue10}"
    if(state.presenceSensorValue10 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName10} - Presence Sensor is not present - Been Here is now off."
		state.globalBH10 = "no"
		gvDevice.sendDataMap10(state.globalBH10)
        if(departedNow) {
            state.nowName = "${parent.friendlyName10}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName10}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName10} - Presence Sensor is present - Waiting for Trigger"
        unschedule(scheduledMessageNowHandler)
        if(homeNow) {
            state.nowName = "${parent.friendlyName10}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler11(evt){
	state.presenceSensorValue11 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler11 - ${parent.friendlyName11} - Presence Sensor: ${state.presenceSensorValue11}"
    if(state.presenceSensorValue11 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName11} - Presence Sensor is not present - Been Here is now off."
		state.globalBH11 = "no"
		gvDevice.sendDataMap11(state.globalBH11)
        if(departedNow) {
            state.nowName = "${parent.friendlyName11}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName11}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName11} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName11}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler12(evt){
	state.presenceSensorValue12 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler12 - ${parent.friendlyName12} - Presence Sensor: ${state.presenceSensorValue12}"
    if(state.presenceSensorValue12 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName12} - Presence Sensor is not present - Been Here is now off."
		state.globalBH12 = "no"
		gvDevice.sendDataMap12(state.globalBH12)
        if(departedNow) {
            state.nowName = "${parent.friendlyName12}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName12}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName12} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName12}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler13(evt){
	state.presenceSensorValue13 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler13 - ${parent.friendlyName13} - Presence Sensor: ${state.presenceSensorValue13}"
    if(state.presenceSensorValue13 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName13} - Presence Sensor is not present - Been Here is now off."
		state.globalBH13 = "no"
		gvDevice.sendDataMap13(state.globalBH13)
        if(departedNow) {
            state.nowName = "${parent.friendlyName13}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName13}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName13} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName13}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler14(evt){
	state.presenceSensorValue14 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler14 - ${parent.friendlyName14} - Presence Sensor: ${state.presenceSensorValue14}"
    if(state.presenceSensorValue14 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName14} - Presence Sensor is not present - Been Here is now off."
		state.globalBH14 = "no"
		gvDevice.sendDataMap14(state.globalBH14)
        if(departedNow) {
            state.nowName = "${parent.friendlyName14}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName14}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName14} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName14}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler15(evt){
	state.presenceSensorValue15 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler15 - ${parent.friendlyName15} - Presence Sensor: ${state.presenceSensorValue15}"
    if(state.presenceSensorValue15 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName15} - Presence Sensor is not present - Been Here is now off."
		state.globalBH15 = "no"
		gvDevice.sendDataMap15(state.globalBH15)
        if(departedNow) {
            state.nowName = "${parent.friendlyName15}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName15}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName15} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName15}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler16(evt){
	state.presenceSensorValue16 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler16 - ${parent.friendlyName16} - Presence Sensor: ${state.presenceSensorValue16}"
    if(state.presenceSensorValue16 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName16} - Presence Sensor is not present - Been Here is now off."
		state.globalBH16 = "no"
		gvDevice.sendDataMap16(state.globalBH16)
        if(departedNow) {
            state.nowName = "${parent.friendlyName16}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName16}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName16} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName16}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler17(evt){
	state.presenceSensorValue17 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler17 - ${parent.friendlyName17} - Presence Sensor: ${state.presenceSensorValue17}"
    if(state.presenceSensorValue17 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName17} - Presence Sensor is not present - Been Here is now off."
		state.globalBH17 = "no"
		gvDevice.sendDataMap17(state.globalBH17)
        if(departedNow) {
            state.nowName = "${parent.friendlyName17}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName17}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName17} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName17}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler18(evt){
	state.presenceSensorValue18 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler18 - ${parent.friendlyName18} - Presence Sensor: ${state.presenceSensorValue18}"
    if(state.presenceSensorValue18 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName18} - Presence Sensor is not present - Been Here is now off."
		state.globalBH18 = "no"
		gvDevice.sendDataMap18(state.globalBH18)
        if(departedNow) {
            state.nowName = "${parent.friendlyName18}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName18}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName18} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName18}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler19(evt){
	state.presenceSensorValue19 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler19 - ${parent.friendlyName19} - Presence Sensor: ${state.presenceSensorValue19}"
    if(state.presenceSensorValue19 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName19} - Presence Sensor is not present - Been Here is now off."
		state.globalBH19 = "no"
		gvDevice.sendDataMap19(state.globalBH19)
        if(departedNow) {
            state.nowName = "${parent.friendlyName19}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName19}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName19} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName19}"
            messageHomeNow()
        }
    }
}

def presenceSensorHandler20(evt){
	state.presenceSensorValue20 = evt.value
	if(logEnable) log.debug "In presenceSensorHandler20 - ${parent.friendlyName20} - Presence Sensor: ${state.presenceSensorValue20}"
    if(state.presenceSensorValue20 == "not present"){
    	if(logEnable) log.debug "${parent.friendlyName20} - Presence Sensor is not present - Been Here is now off."
		state.globalBH20 = "no"
		gvDevice.sendDataMap20(state.globalBH20)
        if(departedNow) {
            state.nowName = "${parent.friendlyName20}"
            messageDeparted()
        }
        if(departedDelayed) {
            state.nowName = "${parent.friendlyName20}"
            runIn(120, messageDeparted)
        }
    } else {
		if(logEnable) log.debug "${parent.friendlyName20} - Presence Sensor is present - Waiting for Trigger"
        if(homeNow) {
            state.nowName = "${parent.friendlyName20}"
            messageHomeNow()
        }
    }
}

def lockHandler(evt) {
	state.lockStatus = evt.value
	state.lockName = evt.displayName
	if(logEnable) log.debug "In lockHandler - Lock: ${state.lockName} - Status: ${state.lockStatus}"
	if(state.lockStatus == "unlocked") {
		if(logEnable) log.debug "In lockHandler..."
        whosHere()
	}
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	state.contactName = evt.displayName
	if(logEnable) log.debug "In contactSensorHandler - Contact: ${state.contactName} - Status: ${state.contactStatus}"
	if(csOpenClosed == "Open") {
		if(state.contactStatus == "open") {
			if(logEnable) log.debug "In contactSensorHandler..."
			whosHere()
		}
	}
	if(csOpenClosed == "Closed") {
		if(state.contactStatus == "closed") {
			if(logEnable) log.debug "In contactSensorHandler..."
			whosHere()
		}
	}
}

def motionSensorHandler(evt) {
	state.motionStatus = evt.value
	state.motionName = evt.displayName
	if(logEnable) log.debug "In motionSensorHandler - Motion Name: ${state.motionName} - Status: ${state.motionStatus}"
	if(state.motionStatus == "active") {
		if(logEnable) log.debug "In motionSensorHandler..."
		whosHere()
	}
}

def whosHere() {
    state.presenceMap = [:]
	state.nameCount = 0
	state.canSpeak = "no"
    if(presenceSensor1) getTimeDiff(1)
	if(presenceSensor2) getTimeDiff(2)
	if(presenceSensor3) getTimeDiff(3)
	if(presenceSensor4) getTimeDiff(4)
	if(presenceSensor5) getTimeDiff(5)
    if(presenceSensor6) getTimeDiff(6)
    if(presenceSensor7) getTimeDiff(7)
    if(presenceSensor8) getTimeDiff(8)
    if(presenceSensor9) getTimeDiff(9)
    if(presenceSensor10) getTimeDiff(10)
    if(presenceSensor11) getTimeDiff(11)
    if(presenceSensor12) getTimeDiff(12)
    if(presenceSensor13) getTimeDiff(13)
    if(presenceSensor14) getTimeDiff(14)
    if(presenceSensor15) getTimeDiff(15)
    if(presenceSensor16) getTimeDiff(16)
    if(presenceSensor17) getTimeDiff(17)
    if(presenceSensor18) getTimeDiff(18)
    if(presenceSensor19) getTimeDiff(19)
    if(presenceSensor20) getTimeDiff(20)
    
   // if(state.canSpeak == "yes" && rmAnyoneReturns) rulesHandler(rmAnyoneReturns)
	if(state.canSpeak == "yes") messageWelcomeHome()
}

def getTimeDiff(numb) {
    if(numb == 1) { pSensor = presenceSensor1;fName = "${parent.friendlyName1}";globalBH = "${state.globalBH1}";sendDataM = "sendDataMap1";globalBH = gvDevice.currentValue("globalBH1") }
    if(numb == 2) { pSensor = presenceSensor2;fName = "${parent.friendlyName2}";globalBH = "${state.globalBH2}";sendDataM = "sendDataMap2";globalBH = gvDevice.currentValue("globalBH2") }
    if(numb == 3) { pSensor = presenceSensor3;fName = "${parent.friendlyName3}";globalBH = "${state.globalBH3}";sendDataM = "sendDataMap3";globalBH = gvDevice.currentValue("globalBH3") }
    if(numb == 4) { pSensor = presenceSensor4;fName = "${parent.friendlyName4}";globalBH = "${state.globalBH4}";sendDataM = "sendDataMap4";globalBH = gvDevice.currentValue("globalBH4") }
    if(numb == 5) { pSensor = presenceSensor5;fName = "${parent.friendlyName5}";globalBH = "${state.globalBH5}";sendDataM = "sendDataMap5";globalBH = gvDevice.currentValue("globalBH5") }
    if(numb == 6) { pSensor = presenceSensor6;fName = "${parent.friendlyName6}";globalBH = "${state.globalBH6}";sendDataM = "sendDataMap6";globalBH = gvDevice.currentValue("globalBH6") }
    if(numb == 7) { pSensor = presenceSensor7;fName = "${parent.friendlyName7}";globalBH = "${state.globalBH7}";sendDataM = "sendDataMap7";globalBH = gvDevice.currentValue("globalBH7") }
    if(numb == 8) { pSensor = presenceSensor8;fName = "${parent.friendlyName8}";globalBH = "${state.globalBH8}";sendDataM = "sendDataMap8";globalBH = gvDevice.currentValue("globalBH8") }
    if(numb == 9) { pSensor = presenceSensor9;fName = "${parent.friendlyName9}";globalBH = "${state.globalBH9}";sendDataM = "sendDataMap9";globalBH = gvDevice.currentValue("globalBH9") }
    if(numb == 10) { pSensor = presenceSensor10;fName = "${parent.friendlyName10}";globalBH = "${state.globalBH10}";sendDataM = "sendDataMap10";globalBH = gvDevice.currentValue("globalBH10") }
    if(numb == 11) { pSensor = presenceSensor11;fName = "${parent.friendlyName11}";globalBH = "${state.globalBH11}";sendDataM = "sendDataMap11";globalBH = gvDevice.currentValue("globalBH11") }
    if(numb == 12) { pSensor = presenceSensor12;fName = "${parent.friendlyName12}";globalBH = "${state.globalBH12}";sendDataM = "sendDataMap12";globalBH = gvDevice.currentValue("globalBH12") }
    if(numb == 13) { pSensor = presenceSensor13;fName = "${parent.friendlyName13}";globalBH = "${state.globalBH13}";sendDataM = "sendDataMap13";globalBH = gvDevice.currentValue("globalBH13") }
    if(numb == 14) { pSensor = presenceSensor14;fName = "${parent.friendlyName14}";globalBH = "${state.globalBH14}";sendDataM = "sendDataMap14";globalBH = gvDevice.currentValue("globalBH14") }
    if(numb == 15) { pSensor = presenceSensor15;fName = "${parent.friendlyName15}";globalBH = "${state.globalBH15}";sendDataM = "sendDataMap15";globalBH = gvDevice.currentValue("globalBH15") }
    if(numb == 16) { pSensor = presenceSensor16;fName = "${parent.friendlyName16}";globalBH = "${state.globalBH16}";sendDataM = "sendDataMap16";globalBH = gvDevice.currentValue("globalBH16") }
    if(numb == 17) { pSensor = presenceSensor17;fName = "${parent.friendlyName17}";globalBH = "${state.globalBH17}";sendDataM = "sendDataMap17";globalBH = gvDevice.currentValue("globalBH17") }
    if(numb == 18) { pSensor = presenceSensor18;fName = "${parent.friendlyName18}";globalBH = "${state.globalBH18}";sendDataM = "sendDataMap18";globalBH = gvDevice.currentValue("globalBH18") }
    if(numb == 19) { pSensor = presenceSensor19;fName = "${parent.friendlyName19}";globalBH = "${state.globalBH19}";sendDataM = "sendDataMap19";globalBH = gvDevice.currentValue("globalBH19") }
    if(numb == 20) { pSensor = presenceSensor20;fName = "${parent.friendlyName20}";globalBH = "${state.globalBH20}";sendDataM = "sendDataMap20";globalBH = gvDevice.currentValue("globalBH20") }

    if(logEnable) log.debug "In getTimeDiff - ${numb} - ${fName}"
	def sensorStatus = pSensor.currentValue("presence")
	if(logEnable) log.debug "${fName} - Presence Sensor Status: ${sensorStatus}"
	if(sensorStatus == "present") {
		if(logEnable) log.debug "${fName} - Global Been Here: ${globalBH}"
		def lastActivity = pSensor.getLastActivity()
		
		if(logEnable) log.debug "${fName} - lastActivity: ${lastActivity}"
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		if(logEnable) log.debug "${fName} - timeDiff: ${timeDiff}"
  		if(timeDiff < timeHome) {
			if(globalBH == "no") {
				log.info "${app.label} - ${fName} just got here! Time Diff = ${timeDiff}"
				state.nameCount = state.nameCount + 1
                if(state.nameCount == 1) state.presenceMap = ["${fName}"]
				if(state.nameCount >= 2) state.presenceMap += ["${fName}"]
				state.canSpeak = "yes"
				globalBH = "yes"
                //dataMap = "${globalBH}:yes"
				gvDevice."${sendDataM}"(globalBH)
                if(logEnable) log.trace "${app.label} - ${fName} - Sent 1 (yes) - sendDataM: ${sendDataM} - globalBH ${globalBH}"
			} else {
                log.info "${app.label} - ${fName} - Global 'Been Here' is ${globalBH}. No announcement needed."
			}
		} else {
			globalBH = "no"
			gvDevice."${sendDataM}"(globalBH)
            if(logEnable) log.trace "${app.label} - ${fName} - Sent 2 (no) - sendDataM: ${sendDataM} - globalBH ${globalBH}"
            log.info "${app.label} - ${fName} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
        if(logEnable) log.debug "${fName} - Global Been Here: ${globalBH}"
        if(logEnable) log.debug "${fName} - Presence Sensor: ${sensorStatus} - No announcement needed."
		globalBH = "no"
        gvDevice."${sendDataM}"(globalBH)
        if(logEnable) log.trace "${app.label} - ${fName} - Sent 3 (no) - sendDataM: ${sendDataM} - globalBH ${globalBH}"
	}
}

def lockCodeHandler(evt) {
    if(logEnable) log.debug "In lockCodeHandler (${state.version})"
    state.presenceMap = ""
	state.nameCount = 0
	def data = evt.data
    state.lockName = evt.displayName
    //log.info "Lock Data: ${data}"
    if (data && !data[0].startsWith("{")) {
        data = decrypt(data)
        log.info "Lock Data: ${data}"
        if (data == null) {
            log.warn "Unable to decrypt lock code from device:${state.lockName}"
            return
        }
    }
    def codeMap = parseJson(data ?: "{}").find{ it }
    if (!codeMap) return
    
    state.nowName = "${codeMap?.value?.name}"
	if(logEnable) log.debug "In lockCodeHandler - ${state.lockName} was unlocked by ${state.nowName}"	
    if(myLock1 || myLock2 || myLock3 || myLock4) {
        state.nameCount = state.nameCount + 1
	    if(state.nameCount == 1) state.presenceMap = ["${state.nowName}"]
	    if(state.nameCount >= 2) state.presenceMap += ["${state.nowName}"]
	    if(logEnable) log.debug "In lockCodeHandler - ${state.presenceMap}"
    }
    state.canSpeak = "yes"
    if(theLocks) messageDoorUnlocked()
    if(myLock1 || myLock2 || myLock3 || myLock4) whosHere()
}

def letsTalkQueue(text) {
    if(logEnable) log.debug "In letsTalkQueue - ${text}"
    // Start modified from @djgutheinz
    def duration = Math.max(Math.round(text.length()/12),2)+3
	state.TTSQueue << [text, duration]
    
    queueSize = state.TTSQueue.size()
 
	if(state.playingTTS == false) { 
        if(logEnable) log.debug "In letsTalkQueue - playingTTS: ${state.playingTTS} - queueSize: ${queueSize} - Going to Lets Talk"
        runIn(1, letsTalk)
    } else {
        if(logEnable) log.debug "In letsTalkQueue - playingTTS: ${state.playingTTS} - queueSize: ${queueSize} - Queing the message"
    }
    // End modified from @djgutheinz
}

def letsTalk() {
    // Start modified from @djgutheinz
    state.playingTTS = true
	def queueSize = state.TTSQueue.size()
	if(queueSize == 0) {
		state.playingTTS = false
        if(logEnable) log.debug "In letsTalk - queueSize: ${queueSize} - Finished Speaking"
		return
	}
    def nextTTS = state.TTSQueue[0]
    state.TTSQueue.remove(0)
    // End modified from @djgutheinz
    
	    if(logEnable) log.debug "In letsTalk (${state.version}) - Here we go"
	    checkTime()
	    checkVol()
        if(state.timeBetween == true) {
		    theMsg = nextTTS[0]
    	    if(logEnable) log.debug "In letsTalk (${state.version}) - speaker: ${speaker}, vol: ${state.volume}, msg: ${theMsg}, volRestore: ${volRestore}"
            state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
            state.speakers.each {
                if(logEnable) log.debug "Speaker in use: ${it}"
                if(speakerProxy) {
                    if(logEnable) log.debug "In letsTalk (${state.version}) - speakerProxy - ${it}"
                    it.speak(theMsg)
                } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                    if(logEnable) log.debug "In letsTalk (${state.version}) - setVolumeSpeakAndRestore - ${it}"
                    def prevVolume = it.currentValue("volume")
                    it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
                } else if(it.hasCommand('playTextAndRestore')) {   
                    if(logEnable) log.debug "In letsTalk (${state.version}) - playTextAndRestore - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    def prevVolume = it.currentValue("volume")
                    it.playTextAndRestore(theMsg, prevVolume)
                } else {		        
                    if(logEnable) log.debug "In letsTalk (${state.version}) - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    it.speak(theMsg)
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                    if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
                }
            }
            state.canSpeak = "no"
	        if(logEnable) log.debug "In letsTalk (${state.version}) - Finished speaking, checking queue in ${nextTTS[1]} seconds"  
		    log.info "${app.label} - ${theMsg}"
            if(sendPushMessage) pushNow(theMsg)
            runIn(nextTTS[1], letsTalk)    // Modified from @djgutheinz
	    } else {
            state.canSpeak = "no"
		    if(logEnable) log.debug "In letsTalk (${state.version}) - Messages not allowed at this time"
            runIn(nextTTS[1], letsTalk)    // Modified from @djgutheinz
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

def messageWelcomeHome() {   // Uses a modified version of @Matthew opening and closing message code
	if(logEnable) log.debug "In messageWelcomeHome (${state.version})"
	if(oRandom) {
		def ovalues = "${omessage}".split(";")
		ovSize = ovalues.size()
		ocount = ovSize.toInteger()
    	def orandomKey = new Random().nextInt(ocount)

        def cvalues = "${cmessage}".split(";")
		cvSize = cvalues.size()
		ccount = cvSize.toInteger()
    	def crandomKey = new Random().nextInt(ccount)

		theMessage = ovalues[orandomKey] + ". " + cvalues[crandomKey]
		if(logEnable) log.debug "In messageWelcomeHome - Random - ovSize: ${ovSize}, orandomKey: ${orandomKey}; Random - cvSize: ${cvSize}, crandomKey: ${crandomKey}, theMessage: ${theMessage}"
	} else {
		theMessage = "${omessage}. ${cmessage}"
		if(logEnable) log.debug "In messageWelcomeHome - Static - theMessage: ${theMessage}"
	}
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', getName() )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
    if(logEnable) log.debug "In messageWelcomeHome - Waiting ${delay1} seconds to Speak"
	def delay1ms = delay1 * 1000
	pauseExecution(delay1ms)
    if(logEnable) log.debug "In messageWelcomeHome - going to letsTalkQueue with theMessage"
	letsTalkQueue(theMessage)
}

def messageHomeNow() {
	if(logEnable) log.debug "In messageHomeNow (${state.version})"
	if(oRandomHN) {
		def ovaluesHN = "${omessageHN}".split(";")
		ovSizeHN = ovaluesHN.size()
		ocountHN = ovSizeHN.toInteger()
    	def orandomKeyHN = new Random().nextInt(ocountHN)

        def cvaluesHN = "${cmessageHN}".split(";")
		cvSizeHN = cvaluesHN.size()
		ccountHN = cvSizeHN.toInteger()
    	def crandomKeyHN = new Random().nextInt(ccountHN)

		theMessage = ovaluesHN[orandomKeyHN] + ". " + cvaluesHN[crandomKeyHN]
		if(logEnable) log.debug "In messageHomeNow - Random - ovSizeHN: ${ovSizeHN}, orandomKeyHN: ${orandomKeyHN}; Random - cvSizeHN: ${cvSizeHN}, crandomKeyHN: ${crandomKeyHN}, theMessage: ${theMessage}"
	} else {
		theMessage = "${omessageHN}. ${cmessageHN}"
		if(logEnable) log.debug "In messageHomeNow - Static - theMessage: ${theMessage}"
	}
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', "${state.nowName}" )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
    if(logEnable) log.debug "In messageHomeNow - going to letsTalkQueue with theMessage"
	letsTalkQueue(theMessage)
}

def messageDeparted() {
    if(logEnable) log.debug "In messageDeparted (${state.version})"
    if(oRandomD) {
		def ovaluesD = "${omessageD}".split(";")
		ovSizeD = ovaluesD.size()
		ocountD = ovSizeD.toInteger()
    	def orandomKeyD = new Random().nextInt(ocountD)

        def cvaluesD = "${cmessageD}".split(";")
		cvSizeD = cvaluesD.size()
		ccountD = cvSizeD.toInteger()
    	def crandomKeyD = new Random().nextInt(ccountD)

		theMessage = ovaluesD[orandomKeyD] + ". " + cvaluesD[crandomKeyD]
		if(logEnable) log.debug "In messageDeparted - Random - ovSizeD: ${ovSizeD}, orandomKeyD: ${orandomKeyD}; Random - cvSizeD: ${cvSizeD}, crandomKeyD: ${crandomKeyD}, theMessage: ${theMessage}"
	} else {
		theMessage = "${omessageD}. ${cmessageD}"
		if(logEnable) log.debug "In messageDeparted - Static - theMessage: ${theMessage}"
	}
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', "${state.nowName}" )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
    if(logEnable) log.debug "In messageDeparted - going to letsTalkQueue with theMessage"
	letsTalkQueue(theMessage)
}

def messageDoorUnlocked() {
	if(logEnable) log.debug "In messageDoorUnlocked (${state.version})"
	if(nRandomDU) {
		def nvaluesDU = "${nMessageDU}".split(";")
		nvSizeDU = nvaluesDU.size()
		ncountDU = nvSizeDU.toInteger()
    	def nrandomKeyDU = new Random().nextInt(ncountDU)

		theMessage = nvaluesDU[nrandomKeyDU]
		if(logEnable) log.debug "In messageDoorUnlocked - Random - nvSize: ${nvSizeDU}, nrandomKey: ${nrandomKeyDU}, theMessage: ${theMessage}"
	} else {
		theMessage = "${nMessageDU}"
		if(logEnable) log.debug "In messageDoorUnlocked - Static - theMessage: ${theMessage}"
	}
    if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', "${state.nowName}" )}
    if (theMessage.contains("%door%")) {theMessage = theMessage.replace('%door%', "${state.lockName}" )}
    if(logEnable) log.debug "In messageDoorUnlocked - going to letsTalkQueue with theMessage"
    letsTalkQueue(theMessage)
}

private getName(){
	if(logEnable) log.debug "In getName - Number of Names: ${state.nameCount}, Names: ${state.presenceMap}"
	name = ""
	myCount = 0
	if(state.nameCount == 1) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=1: myCount = ${myCount}"
			name = "${it}"
		}
	}
	if(state.nameCount == 2) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=2: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it} "
			if(myCount == 1) name = "${name}" + "and "
		}
		name = "${name}" + "!"
	}
	if(state.nameCount == 3) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=3: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 2) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 4) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=4: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 3) name = "${name}" + "and "
		}
	}
	if(state.nameCount == 5) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=5: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 4) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 6) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=6: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 5) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 7) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=7: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 6) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 8) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=8: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 7) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 9) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=9: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 8) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 10) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=10: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 9) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 11) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=11: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 10) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 12) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=12: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 11) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 13) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=13: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 12) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 14) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=14: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 13) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 15) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=15: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 14) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 16) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=16: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 15) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 17) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=17: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 16) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 18) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=18: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 17) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 19) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=19: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 18) name = "${name}" + "and "
		}
	}
    if(state.nameCount == 20) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=20: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 19) name = "${name}" + "and "
		}
	}
    
	is_are = (name.contains(' and ') ? 'are' : 'is')
	has_have = (name.contains(' and ') ? 'have' : 'has')
	if(name == null) names = "Whoever you are"
	if(name == "") names = "Whoever you are"
	if(logEnable) log.debug "AGAIN...Name = ${name}"
	return name
}

def rulesHandler(rules) {
    if(logEnable) log.debug "In rulesHandler - Running ${rules}"
    RMUtils.sendAction(rules, "runRule", app.label)
}

def pushNow(msg) {
	if(logEnable) log.debug "In pushNow"
	if(sendPushMessage) {
		pushMessage = "${app.label} \n"
		pushMessage += msg
		if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
        sendPushMessage.deviceNotification(pushMessage)
	}	
}

// ********** Normal Stuff **********

def setDefaults(){
	setupNewStuff()
	if(logEnable == null){logEnable = false}
	state.nameCount = 0
	state.canSpeak = "no"
    state.playingTTS = false
	state.TTSQueue = []
}

def getImage(type) {					// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=15 width=15>"
}

def getFormat(type, myText=""){			// Modified from @Stephack
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Home Tracker - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
