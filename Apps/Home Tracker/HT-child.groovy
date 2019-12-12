/**
 *  ****************  Home Tracker Child App  ****************
 *
 *  Design Usage:
 *  Track the coming and going of house members with announcements and push messages. Including a 'Welcome Home' message after entering the home!
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
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
 *  V2.1.4 - 12/12/19 - Major rework, things are looking better!
 *  V2.1.3 - 12/12/19 - Simplified globalBH status, better status updates
 *  V2.1.2 - 12/11/19 - Still working on whether to make announcements or not
 *  V2.1.1 - 12/11/19 - Reworked timDiff handler, lots of little changes
 *  V2.1.0 - 12/10/19 - Reworking how locks as presence sensor are handled, Added alt pronounce for locks
 *  V2.0.9 - 12/10/19 - Fixed an issue with Greetings
 *  V2.0.8 - 11/30/19 - Fixed an issue causing Welcome Home to not be announced. Lot's of cosmetic changes
 *  V2.0.7 - 11/04/19 - Code changes to get rid of some gremlins, got rid of the message queue as it was always getting stuck
 *  V2.0.6 - 10/13/19 - Cosmetic changes to Global Variable section and new error message if not used. New option 'auto clear' for message queueing issues.
 *  V2.0.5 - 10/04/19 - Support for alt pronounce of Presence Sensor names (aaronward)
 *  V2.0.4 - 10/03/19 - LOTS of changes, added some rule machine options, lock codes can be used as presence sensors and name
will be added to the announcement. Greetings have returned! Be sure to load up each child app and fill in the missing pieces!
 *  V2.0.3 - 09/14/19 - More tweaking...
 *  V2.0.2 - 09/14/19 - Tried to make the opening and closing sections more clear. Added green check marks to sections that are
filled out. Found a couple of typos.
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
	state.version = "v2.1.4"
    
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
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
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
    	    if(theLocks || myLock1 || myLock2 || myLock3 || myLock4) {
                href "doorLockOptions", title:"${getImage("checkMarkGreen")} Door Lock Options", description:"Click here to setup the Door Lock Options"
            } else {
                href "doorLockOptions", title:"Door Lock Options", description:"Click here to setup the Door Lock Options"
            }
            
            href "ruleMachineOptions", title:"Rule Machine Options", description:"Click here to setup the Rule Machine Options"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Global Variables")) {
			paragraph "This app <b>requires</b> a 'virtual device' to send variables between child apps. This is to prevent multiple announcements.<br>ie. Person A comes home and enters door 1, walks through the house and opens door 2 to let the dogs out.  We only want one 'Welcome Home' message to be played."
			paragraph "* Vitual Device must use our custom 'Home Tracker Driver'"
			input "gvDevice", "capability.actuator", title: "Virtual Device created for Home Tracker", required: false, multiple: false
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging")
		}
		display2()
	}
}

def presenceOptions(){
    dynamicPage(name: "presenceOptions", title: "Presence Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Sensor Options")) { 
			paragraph "If a presence sensor has been present for less than x minutes, after the trigger, then speak the message."
            input "timeHome", "number", title: "How many minutes can the presence sensor be present and still be considered for a welcome home message (default=10)", required: true, defaultValue: 10  
            
            paragraph "If a presence sensor has been not present for less than x minutes, after the trigger, then speak the message."
            input "timeAway", "number", title: "How many minutes can the presence sensor be not present and still be considered for a departed message (default=2)", required: true, defaultValue: 2
            
            paragraph "Note: If you are not seeing your 'Friendly Names', then go back to the parent app, enter them in and hit 'done' before setting up any child apps."
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
			if(ps7a) input("presenceSensor0", "capability.presenceSensor", title: "Match a Presence Sensor to a Friendly Name", required: true)
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
    }
}

def speechOptions(){
    dynamicPage(name: "speechOptions", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerProxy", "bool", defaultValue: false, title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange: true
        }
        if(!speakerProxy) {
            if(speakerMP || speakerSS) {
		        section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		            paragraph "NOTE: Not all speakers can use volume controls.", width:8
                    paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                    input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required:true, width:6
		            input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required:true, width:6
                    input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required:false, submitOnChange:true
		    	    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required:true, width:6
    	    	    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required:true, width:6
                }
		    }
		    section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
                input "fromTime", "time", title: "From", required:false, width: 6
        	    input "toTime", "time", title: "To", required:false, width: 6
		    }
        } else {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Proxy")) {
		        paragraph "Speaker proxy in use."
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
    }
}

def messageOptions(){
    dynamicPage(name: "messageOptions", title: "Message Options", install: false, uninstall:false){
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Home NOW Message Options")) {
			input "homeNow", "bool", defaultValue: false, title: "Announce when a presence sensor arrives at 'Home', with NO wait? (off='No', on='Yes')", description: "Home Instant", submitOnChange:true
            paragraph "This will give a heads up that someone is home. But can be a false alarm if they are just driving by."
            if(homeNow) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
                paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. 'Welcome back %name%' + 'Nice to see you again'"
                paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
                input(name: "oRandomHN", type: "bool", defaultValue: "false", title: "Random Opening and Closing Messages?", description: "Random", submitOnChange: "true")
			    if(!oRandomHN) input "omessageHN", "text", title: "<b>Opening message</b> to be spoken - Single message", required: true
                if(!oRandomHN) input "cmessageHN", "text", title: "<b>Closing message</b> to be spoken - Single message", required: true
			    if(oRandomHN) {
				    input "omessageHN", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				    input(name: "oMsgListHN", type: "bool", defaultValue: "true", title: "Show a list view of the opening messages?", description: "List View", submitOnChange: "true")
				    if(oMsgListHN) {
				    	def ovaluesHN = "${omessageHN}".split(";")
				    	olistMapHN = ""
    				    ovaluesHN.each { item -> olistMapHN += "${item}<br>"}
					    paragraph "${olistMapHN}"
				    }
                    input "cmessageHN", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				    input(name: "cMsgListHN", type: "bool", defaultValue: "true", title: "Show a list view of the closing messages?", description: "List View", submitOnChange: "true")
				    if(cMsgListHN) {
				    	def cvaluesHN = "${cmessageHN}".split(";")
					    clistMapHN = ""
    				    cvaluesHN.each { item -> clistMapHN += "${item}<br>"}
					    paragraph "${clistMapHN}"
				    }
                }
            }
        }       
		section(getFormat("header-green", "${getImage("Blank")}"+" Welcome Home Message Options")) {
            paragraph "This will speak a nice 'Welcome Home' message AFTER you have entered the house."
            paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
            paragraph "Message constructed as 'Greeting' + 'Opening message' + 'Closing message'<br>REMEMBER to use your wildcards!<br>ie. 'Welcome back %name%' + 'Nice to see you again'"
            
            paragraph "<hr>"
            paragraph "<b>Greeting Options</b>"
            paragraph "Between what times will Greeting 1 be used"
            input "fromTimeG1", "time", title: "From", required: true, width: 6
        	input "toTimeG1", "time", title: "To", required: true, width: 6
            input "oRandomG1", "bool", defaultValue:false, title: "Random Greeting 1?", description: "Random", submitOnChange:true
			if(!oRandomG1) input "greeting1", "text", required: true, title: "Greeting - 1 (am) - Single message", defaultValue: "Good Morning"
			if(oRandomG1) {
				input "greeting1", "text", title: "Random Greeting - 1 (am) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG1List", type: "bool", defaultValue: "false", title: "Show a list view of random messages 1?", description: "List View", submitOnChange: "true")
				if(oG1List) {
					def valuesG1 = "${greeting1}".split(";")
					listMapG1 = ""
    				valuesG1.each { itemG1 -> listMapG1 += "${itemG1}<br>" }
					paragraph "${listMapG1}"
				}
			}
            paragraph "Between what times will Greeting 2 be used"
            input "fromTimeG2", "time", title: "From", required: true, width: 6
        	input "toTimeG2", "time", title: "To", required: true, width: 6
            input(name: "oRandomG2", type: "bool", defaultValue: "false", title: "Random Greeting 2?", description: "Random", submitOnChange: "true")
			if(!oRandomG2) input "greeting2", "text", required: true, title: "Greeting - 2 (pm before 6) - Single message", defaultValue: "Good Afternoon"
			if(oRandomG2) {
				input "greeting2", "text", title: "Random Greeting - 2 (pm before 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG2List", type: "bool", defaultValue: "false", title: "Show a list view of the random messages 2?", description: "List View", submitOnChange: "true")
				if(oG2List) {
					def valuesG2 = "${greeting2}".split(";")
					listMapG2 = ""
    				valuesG2.each { itemG2 -> listMapG2 += "${itemG2}<br>" }
					paragraph "${listMapG2}"
				}
			}
            paragraph "Between what times will Greeting 3 be used"
            input "fromTimeG3", "time", title: "From", required: true, width: 6
        	input "toTimeG3", "time", title: "To", required: true, width: 6
            input(name: "oRandomG3", type: "bool", defaultValue: "false", title: "Random Greeting 3?", description: "Random", submitOnChange: "true")
			if(!oRandomG3) input "greeting3", "text", required: true, title: "Greeting - 3 (pm after 6) - Single message", defaultValue: "Good Evening"
			if(oRandomG3) {
				input "greeting3", "text", title: "Random Greeting - 3 (pm after 6) - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oG3List", type: "bool", defaultValue: "false", title: "Show a list view of the random messages 3?", description: "List View", submitOnChange: "true")
				if(oG3List) {
					def valuesG3 = "${greeting3}".split(";")
					listMapG3 = ""
    				valuesG3.each { itemG3 -> listMapG3 += "${itemG3}<br>" }
					paragraph "${listMapG3}"
				}
			}
			paragraph "<hr>"
            
            paragraph "<b>Opening and Closing Options</b>"
            paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
			input(name: "oRandom", type: "bool", defaultValue: "false", title: "Random Opening and Closing Messages?", description: "Random", submitOnChange: "true")
			if(!oRandom) input "omessage", "text", title: "<b>Opening message</b> to be spoken - Single message", required: true
            if(!oRandom) input "cmessage", "text", title: "<b>Closing message</b> to be spoken - Single message", required: true
			if(oRandom) {
				input "omessage", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
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
			}
            paragraph "<hr>"
            input "delay1", "number", title: "How many seconds from the time the trigger being activated to the announcement being made (default=10)", required: true, defaultValue: 10
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Departed Message Options")) {
            paragraph "Please only choose <b>one</b> option"
            input "departedNow", "bool", defaultValue: false, title: "Announce when the presence sensor departs, right away? (off='No', on='Yes')", description: "Home Depart", submitOnChange:true
            paragraph "<small>This will give a heads up that someone has departed. As soon as it is detected.</small>"
			input "departedDelayed", "bool", defaultValue: false, title: "Announce when the presence sensor departs, after a 2 minute delay? (off='No', on='Yes')", description: "Delayed Home Depart", submitOnChange:true
            paragraph "<small>This will give a heads up that someone has departed. But help with false announcements.</small>" 
            if(departedNow && departedDelayed) { paragraph "<b>Please only select ONE Departed option!</b>" }
            paragraph "<hr>"
            
            if(departedNow || departedDelayed) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Presence Sensor<br>%is_are% - returns 'is' or 'are' depending on number of sensors<br>%has_have% - returns 'has' or 'have' depending on number of sensors"
                paragraph "Message constructed as 'Opening message' + 'Closing message' - REMEMBER to use your wildcards!<br>ie. '%name%' + 'is no longer here'"
                paragraph "If either Opening or Closing field isn't required, simply put a . (period) in that field"
                input(name: "oRandomD", type: "bool", defaultValue: "false", title: "Random Opening and Closing Messages?", description: "Random", submitOnChange: "true")
			    if(!oRandomD) input "omessageD", "text", title: "<b>Opening message</b> to be spoken - Single message", required: true
                if(!oRandomD) input "cmessageD", "text", title: "<b>Closing message</b> to be spoken - Single message", required: true
			    if(oRandomD) {
				    input "omessageD", "text", title: "<b>Opening message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				    input(name: "oMsgListD", type: "bool", defaultValue: "true", title: "Show a list view of the opening messages?", description: "List View", submitOnChange: "true")
				    if(oMsgListD) {
				    	def ovaluesD = "${omessageD}".split(";")
				    	olistMapD = ""
    				    ovaluesD.each { item -> olistMapD += "${item}<br>"}
					    paragraph "${olistMapD}"
				    }
                    input "cmessageD", "text", title: "<b>Closing message</b> to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
				    input(name: "cMsgListD", type: "bool", defaultValue: "true", title: "Show a list view of the closing messages?", description: "List View", submitOnChange: "true")
				    if(cMsgListD) {
				    	def cvaluesD = "${cmessageD}".split(";")
					    clistMapD = ""
    				    cvaluesD.each { item -> clistMapD += "${item}<br>"}
					    paragraph "${clistMapD}"
				    }
                }
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
        section(getFormat("header-green", "${getImage("Blank")}"+" Lock User Names")) {
            paragraph "If you have a name that Hubitat has a hard time pronouncing, you can put in an 'alternate' Pronunciation for each name. <small>* Only needed if you want to change the pronunciation of a name.</small>"
		    input "lockName1", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
            input "lockPronounce1", "text", title: "'Alt' Pronunciation", required: false, width: 6
            if(lockName1) {
                input "lockName2", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce2", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName2) {
                input "lockName3", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce3", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName3) {
                input "lockName4", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce4", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName4) {
                input "lockName5", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce5", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName5) {
                input "lockName6", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce6", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName6) {
                input "lockName7", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce7", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName7) {
                input "lockName8", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce8", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName8) {
                input "lockName9", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce9", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName9) {
                input "lockName10", "text", title: "Name found in Lock Code Manager", required: false, submitOnChange: true, width: 6
                input "lockPronounce10", "text", title: "'Alt' Pronunciation", required: false, width: 6
            }
            if(lockName10) {
                paragraph "<small>* Max 10 names has been reached. If more names are needed please message bptworld</small>"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Lock Announcements")) {
            paragraph "This will give a heads up that someone has unlocked a door."
            input "theLocks", "capability.lock", title: "Select Locks to announce when unlocked", multiple: true, submitOnChange: true
            if(theLocks) {
                paragraph "<u>Optional wildcards:</u><br>%name% - returns the Friendly Name associcated with a Lock Code<br>%door% - returns the Name of the Lock"
                input(name: "nRandomDU", type: "bool", defaultValue: "false", title: "Random 'unlocked' message?", description: "Random", submitOnChange: "true")
                if(!nRandomDU) input "nMessageDU", "text", title: "Message to be spoken - Single message",  required: true
                if(nRandomDU) {
				    input "nMessageDU", "text", title: "Message to be spoken - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange: true
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
            paragraph "If a person doesn't have presence tracking available, they can still be included in the 'Welcome Home' message by lock code."
			paragraph "Note: Lock codes must be setup in Hubitat's Lock Code Manager."
            input "myLock1", "capability.lock", title: "Activate the 'Welcome Home' message when this door is unlocked - Lock 1"
            input "myLock2", "capability.lock", title: "Activate the 'Welcome Home' message when this door is unlocked - Lock 2"
            input "myLock3", "capability.lock", title: "Activate the 'Welcome Home' message when this door is unlocked - Lock 3"
            input "myLock4", "capability.lock", title: "Activate the 'Welcome Home' message when this door is unlocked - Lock 4"
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
            paragraph "<b>If everyone leaves...</b>"
            input "rmEveryoneLeaves", "enum", title: "Select which rules to run", options: rules, multiple: true
            paragraph "<hr>"
            paragraph "<b>If nobody's home and someone returns...</b>"
            input "rmAnyoneReturns", "enum", title: "Select which rules to run", options: rules, multiple: true
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

	if(presenceSensor1) subscribe(presenceSensor1, "presence", presenceSensorHandler)
	if(presenceSensor2) subscribe(presenceSensor2, "presence", presenceSensorHandler)
	if(presenceSensor3) subscribe(presenceSensor3, "presence", presenceSensorHandler)
	if(presenceSensor4) subscribe(presenceSensor4, "presence", presenceSensorHandler)
	if(presenceSensor5) subscribe(presenceSensor5, "presence", presenceSensorHandler)
    if(presenceSensor6) subscribe(presenceSensor6, "presence", presenceSensorHandler)
    if(presenceSensor7) subscribe(presenceSensor7, "presence", presenceSensorHandler)
    if(presenceSensor8) subscribe(presenceSensor8, "presence", presenceSensorHandler)
    if(presenceSensor9) subscribe(presenceSensor9, "presence", presenceSensorHandler)
    if(presenceSensor10) subscribe(presenceSensor10, "presence", presenceSensorHandler)
    if(presenceSensor11) subscribe(presenceSensor11, "presence", presenceSensorHandler)
    if(presenceSensor12) subscribe(presenceSensor12, "presence", presenceSensorHandler)
    if(presenceSensor13) subscribe(presenceSensor13, "presence", presenceSensorHandler)
    if(presenceSensor14) subscribe(presenceSensor14, "presence", presenceSensorHandler)
    if(presenceSensor15) subscribe(presenceSensor15, "presence", presenceSensorHandler)
    if(presenceSensor16) subscribe(presenceSensor16, "presence", presenceSensorHandler)
    if(presenceSensor17) subscribe(presenceSensor17, "presence", presenceSensorHandler)
    if(presenceSensor18) subscribe(presenceSensor18, "presence", presenceSensorHandler)
    if(presenceSensor19) subscribe(presenceSensor19, "presence", presenceSensorHandler)
    if(presenceSensor20) subscribe(presenceSensor20, "presence", presenceSensorHandler)
    if(myLock1) subscribe(myLock1, "lock.unlocked", presenceSensorHandler)
    if(myLock2) subscribe(myLock2, "lock.unlocked", presenceSensorHandler)
    if(myLock3) subscribe(myLock3, "lock.unlocked", presenceSensorHandler)
    if(myLock4) subscribe(myLock4, "lock.unlocked", presenceSensorHandler)
    if(theLocks) subscribe(theLocks, "lock.unlocked", lockHandler)
	
	if(triggerMode == "Door_Lock"){subscribe(lock1, "lock", lockHandler)}
	if(triggerMode == "Contact_Sensor"){subscribe(contactSensor, "contact", contactSensorHandler)}
	if(triggerMode == "Motion_Sensor"){subscribe(motionSensor1, "motion", motionSensorHandler)}
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def presenceSensorHandler(evt){
    if(logEnable) log.debug "In presenceSensorHandler - *** Starting ***"
    triggerName = evt.getDisplayName()
    if(logEnable) log.debug "In presenceSensorHandler (${state.version}) - triggerName: ${triggerName}"
    match = false
    if(triggerName.contains("${presenceSensor1}")) { match = true;numb = 1 }
    if(triggerName.contains("${presenceSensor2}")) { match = true;numb = 2 }
    if(triggerName.contains("${presenceSensor3}")) { match = true;numb = 3 }
    if(triggerName.contains("${presenceSensor4}")) { match = true;numb = 4 }
    if(triggerName.contains("${presenceSensor5}")) { match = true;numb = 5 }
    if(triggerName.contains("${presenceSensor6}")) { match = true;numb = 6 }
    if(triggerName.contains("${presenceSensor7}")) { match = true;numb = 7 }
    if(triggerName.contains("${presenceSensor8}")) { match = true;numb = 8 }
    if(triggerName.contains("${presenceSensor9}")) { match = true;numb = 9 }
    if(triggerName.contains("${presenceSensor10}")) { match = true;numb = 10 }
    if(triggerName.contains("${presenceSensor11}")) { match = true;numb = 11 }
    if(triggerName.contains("${presenceSensor12}")) { match = true;numb = 12 }
    if(triggerName.contains("${presenceSensor13}")) { match = true;numb = 13 }
    if(triggerName.contains("${presenceSensor14}")) { match = true;numb = 14 }
    if(triggerName.contains("${presenceSensor15}")) { match = true;numb = 15 }
    if(triggerName.contains("${presenceSensor16}")) { match = true;numb = 16 }
    if(triggerName.contains("${presenceSensor17}")) { match = true;numb = 17 }
    if(triggerName.contains("${presenceSensor18}")) { match = true;numb = 18 }
    if(triggerName.contains("${presenceSensor19}")) { match = true;numb = 19 }
    if(triggerName.contains("${presenceSensor20}")) { match = true;numb = 20 }
    if(triggerName.contains("${myLock1}")) { match = true;numb = 21 }
    if(triggerName.contains("${myLock2}")) { match = true;numb = 22 }
    if(triggerName.contains("${myLock3}")) { match = true;numb = 23 }
    if(triggerName.contains("${myLock4}")) { match = true;numb = 24 }

    if(match) {
        if(logEnable) log.debug "In presenceSensorHandler - We have a match! numb: ${numb}"
        whichPresenceSensor(numb)
        // returned from whichPresenceSensor with - pSensor,lastActivity,fName,sendDataM,globalBH
        
        state.sendDataM = sendDataM
        if(logEnable) log.debug "In presenceSensorHandler - fName: ${fName} - pSensor: ${pSensor} - sendDataM: ${sendDataM} - globalBH: ${globalBH}"
    
        if(pSensor == "not present"){
        	if(logEnable) log.debug "In presenceSensorHandler - ${fName} - Presence Sensor is not present."
            if(departedNow) {
                handler = "messageDeparted"
                whosHere(handler)
            } else if(departedDelayed) {
                runIn(120, whosHere, [data: [key1:'messageDeparted']])
            } else {
                handler = "noMessage"
                whosHere(handler)
            }
        } else {
            unschedule(scheduledMessageNowHandler)
            if(homeNow) {
                handler = "messageHomeNow"
                whosHere(handler)
            } else {
                handler = "messageHome"
                whosHere(handler)
            }
            if(logEnable) log.debug "In presenceSensorHandler - ${fName} - Presence Sensor is present - Waiting for Trigger **********"
        }
    } else {
        if(logEnable) log.warn "In presenceSensorHandler - No match found - triggerName: ${triggerName}"
    }
}

def whichPresenceSensor(numb) {
    if(logEnable) log.debug "In whichPresenceSensor (${state.version}) - Checking which Presence Sensor: ${numb}"
    if(numb==1) { 
        pSensor=presenceSensor1.currentValue("presence")
        lastActivity = presenceSensor1.getLastActivity()
        if(parent.pronounce1.contains("Not set") || parent.procunciation1=="") fName="${parent.friendlyName1}"
        else fName="${parent.pronounce1}"
        sendDataM="sendDataMap01"
        try {
            globalBH = gvDevice.currentValue("globalBH01")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==2) {
        pSensor=presenceSensor2.currentValue("presence")
        lastActivity = presenceSensor2.getLastActivity()
        if(parent.pronounce2.contains("Not set") || parent.procunciation2=="") fName="${parent.friendlyName2}"
        else fName="${parent.pronounce2}"
        sendDataM="sendDataMap02"
        try {
            globalBH = gvDevice.currentValue("globalBH02")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==3) {
        pSensor=presenceSensor3.currentValue("presence")
        lastActivity = presenceSensor3.getLastActivity()
        if(parent.pronounce3.contains("Not set") || parent.procunciation3=="") fName=parent.friendlyName3
        else fName=parent.pronounce3
        sendDataM="sendDataMap03"
        try {
            globalBH = gvDevice.currentValue("globalBH03")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==4) {
        pSensor=presenceSensor4.currentValue("presence")
        lastActivity = presenceSensor4.getLastActivity()
        if(parent.pronounce4.contains("Not set") || parent.procunciation4=="") fName=parent.friendlyName4
        else fName=parent.pronounce4
        sendDataM="sendDataMap04"
        try {
            globalBH = gvDevice.currentValue("globalBH04")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==5) {
        pSensor=presenceSensor5.currentValue("presence")
        lastActivity = presenceSensor5.getLastActivity()
        if(parent.pronounce5.contains("Not set") || parent.procunciation5=="") fName=parent.friendlyName5
        else fName=parent.pronounce5
        sendDataM="sendDataMap05"
        try {
            globalBH = gvDevice.currentValue("globalBH05")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==6) {
        pSensor=presenceSensor6.currentValue("presence")
        lastActivity = presenceSensor6.getLastActivity()
        if(parent.pronounce6.contains("Not set") || parent.procunciation6=="") fName=parent.friendlyName6
        else fName=parent.pronounce6
        sendDataM="sendDataMap06"
        try {
            globalBH = gvDevice.currentValue("globalBH06")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==7) {
        pSensor=presenceSensor7currentValue("presence")
        lastActivity = presenceSensor7.getLastActivity()
        if(parent.pronounce7.contains("Not set") || parent.procunciation7=="") fName=parent.friendlyName7
        else fName=parent.pronounce7
        sendDataM="sendDataMap07"
        try {
            globalBH = gvDevice.currentValue("globalBH07")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==8) {
        pSensor=presenceSensor8currentValue("presence")
        lastActivity = presenceSensor8.getLastActivity()
        if(parent.pronounce8.contains("Not set") || parent.procunciation8=="") fName=parent.friendlyName8
        else fName=parent.pronounce8
        sendDataM="sendDataMap08"
        try {
            globalBH = gvDevice.currentValue("globalBH08")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==9) {
        pSensor=presenceSensor9.currentValue("presence")
        lastActivity = presenceSensor9.getLastActivity()
        if(parent.pronounce9.contains("Not set") || parent.procunciation9=="") fName=parent.friendlyName9
        else fName=parent.pronounce9
        sendDataM="sendDataMap09"
        try {
            globalBH = gvDevice.currentValue("globalBH09")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==10) {
        pSensor=presenceSensor10.currentValue("presence")
        lastActivity = presenceSensor10.getLastActivity()
        if(parent.pronounce10.contains("Not set") || parent.procunciation10=="") fName=parent.friendlyName10
        else fName=parent.pronounce10
        sendDataM="sendDataMap10"
        try {
            globalBH = gvDevice.currentValue("globalBH10")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==11) {
        pSensor=presenceSensor11.currentValue("presence")
        lastActivity = presenceSensor11.getLastActivity()
        if(parent.pronounce11.contains("Not set") || parent.procunciation11=="") fName=parent.friendlyName11
        else fName=parent.pronounce11
        sendDataM="sendDataMap11"
        try {
            globalBH = gvDevice.currentValue("globalBH11")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==12) {
        pSensor=presenceSensor12.currentValue("presence")
        lastActivity = presenceSensor12.getLastActivity()
        if(parent.pronounce12.contains("Not set") || parent.procunciation12=="") fName=parent.friendlyName12
        else fName=parent.pronounce12
        sendDataM="sendDataMap12"
        try {
            globalBH = gvDevice.currentValue("globalBH12")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==13) {
        pSensor=presenceSensor13.currentValue("presence")
        lastActivity = presenceSensor13.getLastActivity()
        if(parent.pronounce13.contains("Not set") || parent.procunciation13=="") fName=parent.friendlyName13
        else fName=parent.pronounce13
        sendDataM="sendDataMap13"
        try {
            globalBH = gvDevice.currentValue("globalBH13")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==14) {
        pSensor=presenceSensor14.currentValue("presence")
        lastActivity = presenceSensor14.getLastActivity()
        if(parent.pronounce14.contains("Not set") || parent.procunciation14=="") fName=parent.friendlyName14
        else fName=parent.pronounce14
        sendDataM="sendDataMap14"
        try {
            globalBH = gvDevice.currentValue("globalBH14")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==15) {
        pSensor=presenceSensor15.currentValue("presence")
        lastActivity = presenceSensor15.getLastActivity()
        if(parent.pronounce15.contains("Not set") || parent.procunciation15=="") fName=parent.friendlyName15
        else fName=parent.pronounce15
        sendDataM="sendDataMap15"
        try {
            globalBH = gvDevice.currentValue("globalBH15")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==16) {
        pSensor=presenceSensor16.currentValue("presence")
        lastActivity = presenceSensor16.getLastActivity()
        if(parent.pronounce16.contains("Not set") || parent.procunciation16=="") fName=parent.friendlyName16
        else fName=parent.pronounce16
        sendDataM="sendDataMap16"
        try {
            globalBH = gvDevice.currentValue("globalBH16")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==17) {
        pSensor=presenceSensor17.currentValue("presence")
        lastActivity = presenceSensor17.getLastActivity()
        if(parent.pronounce17.contains("Not set") || parent.procunciation17=="") fName=parent.friendlyName17
        else fName=parent.pronounce17
        sendDataM="sendDataMap17"
        try {
            globalBH = gvDevice.currentValue("globalBH17")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==18) {
        pSensor=presenceSensor18.currentValue("presence")
        lastActivity = presenceSensor18.getLastActivity()
        if(parent.pronounce18.contains("Not set") || parent.procunciation18=="") fName=parent.friendlyName18
        else fName=parent.pronounce18
        sendDataM="sendDataMap18"
        try {
            globalBH = gvDevice.currentValue("globalBH18")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==19) {
        pSensor=presenceSensor19.currentValue("presence")
        lastActivity = presenceSensor19.getLastActivity()
        if(parent.pronounce19.contains("Not set") || parent.procunciation19=="") fName=parent.friendlyName19
        else fName=parent.pronounce19
        sendDataM="sendDataMap19"
        try {
            globalBH = gvDevice.currentValue("globalBH19")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==20) {
        pSensor=presenceSensor20.currentValue("presence")
        lastActivity = presenceSensor20.getLastActivity()
        if(parent.pronounce20.contains("Not set") || parent.procunciation20=="") fName=parent.friendlyName20
        else fName=parent.pronounce20
        sendDataM="sendDataMap20"
        try {
            globalBH = gvDevice.currentValue("globalBH20")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Presence Sensor Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==21) {
        pSensor=myLock1.currentValue("lock")
        lastActivity = myLock1.getLastActivity()
        codeName=myLock1.currentValue("lastCodeName")
        getLockUserName(codeName)
        
        sendDataM="sendDataMap21"
        try {
            globalBH = gvDevice.currentValue("globalBH21")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Lock Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==22) {
        pSensor=myLock2.currentValue("lock")
        lastActivity = myLock2.getLastActivity()
        codeName=myLock2.currentValue("lastCodeName")
        getLockUserName(codeName)
        
        sendDataM="sendDataMap22"
        try {
            globalBH = gvDevice.currentValue("globalBH22")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Lock Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==23) {
        pSensor=myLock3.currentValue("lock")
        lastActivity = myLock3.getLastActivity()
        codeName=myLock3.currentValue("lastCodeName")
        getLockUserName(codeName)
        
        sendDataM="sendDataMap23"
        try {
            globalBH = gvDevice.currentValue("globalBH23")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Lock Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(numb==24) {
        pSensor=myLock4.currentValue("lock")
        lastActivity = myLock4.getLastActivity()
        codeName=myLock4.currentValue("lastCodeName")
        getLockUserName(codeName)
        
        sendDataM="sendDataMap24"
        try {
            globalBH = gvDevice.currentValue("globalBH24")
        } catch (e) {}
        if(globalBH == null || globalBH == "") {
            if(logDebug) log.debug "In whichPresenceSensor - fName: ${fName} - New Lock Found!"
            globalBH = "waitingForData"
            gvDevice."${sendDataM}"(globalBH)
        }
    }
    if(logEnable) log.debug "In whichPresenceSensor - pSensor: ${pSensor} - lastActivity: ${lastActivity} - fName: ${fName} - sendDataM: ${sendDataM} - globalBH: ${globalBH}"
    return [pSensor,lastActivity,fName,sendDataM,globalBH]
}

def getLockUserName(codeName) {
    if(logEnable) log.debug "In getLockUserName (${state.version})"
    
    if(codeName == lockName1) {
        fName="${lockPronounce1}"
    } else if(codeName == lockName2) {
        fName="${lockPronounce2}"
    } else if(codeName == lockName3) {
        fName="${lockPronounce3}"
    } else if(codeName == lockName4) {
        fName="${lockPronounce4}"
    } else if(codeName == lockName5) {
        fName="${lockPronounce5}"
    } else if(codeName == lockName6) {
        fName="${lockPronounce6}"
    } else if(codeName == lockName7) {
        fName="${lockPronounce7}"
    } else if(codeName == lockName8) {
        fName="${lockPronounce8}"
    } else if(codeName == lockName9) {
        fName="${lockPronounce9}"
    } else if(codeName == lockName10) {
        fName="${lockPronounce10}"
    } else {
        fName="${codeName}"
    }
    
    if(logEnable) log.debug "In getLockUserName - fName: ${fName}"
    return fName
}

def lockHandler(evt) {
    lockdata = evt.data
	lockStatus = evt.value
	lockName = evt.displayName
	if(logEnable) log.debug "In lockHandler (${state.version}) - Lock: ${lockName} - Status: ${lockStatus} - theLocks: ${theLocks}"
	if(lockStatus == "unlocked") {
        if(logEnable) log.debug "In lockHandler - Lock: ${lockName} - Status: ${lockStatus} - We're in!"
        if(theLocks) {
            //if(logEnable) log.debug "In lockHandler - lockdata: ${lockdata}"
            if (lockdata && !lockdata[0].startsWith("{")) {
                lockdata = decrypt(lockdata)
                //log.info "Lock Data: ${lockdata}"
                if (lockdata == null) {
                    log.warn "Unable to decrypt lock code from device: ${lockName}"
                    return
                }
            }
            def codeMap = parseJson(lockdata ?: "{}").find{ it }
            //if(logEnable) log.debug "In lockHandler - codeMap: ${codeMap}"
            if (!codeMap) {
                if(logEnable) log.debug "In lockHandler - Lock Code not available."
                return
            }
            
            codeName = "${codeMap?.value?.name}"
            if(logEnable) log.debug "In lockHandler - GOING TO getLockUserName"
            getLockUserName(codeName)
            
	        if(logEnable) log.debug "In lockHandler - ${lockName} was unlocked by ${fName}"	

	        if(nRandomDU) {
		        def nvaluesDU = "${nMessageDU}".split(";")
		        nvSizeDU = nvaluesDU.size()
		        ncountDU = nvSizeDU.toInteger()
    	        def nrandomKeyDU = new Random().nextInt(ncountDU)

		        theMessage = nvaluesDU[nrandomKeyDU]
		        if(logEnable) log.info "In messageDoorUnlocked - Random - nvSize: ${nvSizeDU}, nrandomKey: ${nrandomKeyDU}, theMessage: ${theMessage}"
	        } else {
		        theMessage = "${nMessageDU}"
		        if(logEnable) log.info "In messageDoorUnlocked - Static - theMessage: ${theMessage}"
            }
            if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', "${fName}" )}
            if (theMessage.contains("%door%")) {theMessage = theMessage.replace('%door%', "${lockName}" )}
            if(logEnable) log.debug "In messageDoorUnlocked - going to letsTalk with theMessage"
            state.canSpeak = "yes"
            
            letsTalk(theMessage)
        }
        if(logEnable) log.debug "In lockHandler - Lock: ${lockName} - Status: ${lockStatus} - 1"
        handler = "lock"
        letsDoSomething(handler)
	}
    if(logEnable) log.debug "In lockHandler - Lock: ${lockName} - Status: ${lockStatus} - 2"
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	state.contactName = evt.displayName
	if(logEnable) log.debug "In contactSensorHandler (${state.version}) - Contact: ${state.contactName} - Status: ${state.contactStatus}"
	if(csOpenClosed == "Open") {
		if(state.contactStatus == "open") {
			if(logEnable) log.debug "In contactSensorHandler - open"
			handler = "contact"
            letsDoSomething(handler)
		}
	}
	if(csOpenClosed == "Closed") {
		if(state.contactStatus == "closed") {
			if(logEnable) log.debug "In contactSensorHandler - closed"
			handler = "contact"
            letsDoSomething(handler)
		}
	}
}

def motionSensorHandler(evt) {
	state.motionStatus = evt.value
	state.motionName = evt.displayName
	if(logEnable) log.debug "In motionSensorHandler (${state.version}) - Motion Name: ${state.motionName} - Status: ${state.motionStatus}"
	if(state.motionStatus == "active") {
		if(logEnable) log.debug "In motionSensorHandler - active"
		handler = "motion"
        letsDoSomething(handler)
	}
}

def whosHere(handler) {
    if(logEnable) log.debug "In whosHere (${state.version}) - ${handler}"
    state.presenceMap = [:]
    state.prevNameCount = state.nameCount
	state.nameCount = 0
	state.canSpeak = "no"
    if(presenceSensor1) getTimeDiff(1,handler)
	if(presenceSensor2) getTimeDiff(2,handler)
	if(presenceSensor3) getTimeDiff(3,handler)
	if(presenceSensor4) getTimeDiff(4,handler)
	if(presenceSensor5) getTimeDiff(5,handler)
    if(presenceSensor6) getTimeDiff(6,handler)
    if(presenceSensor7) getTimeDiff(7,handler)
    if(presenceSensor8) getTimeDiff(8,handler)
    if(presenceSensor9) getTimeDiff(9,handler)
    if(presenceSensor10) getTimeDiff(10,handler)
    if(presenceSensor11) getTimeDiff(11,handler)
    if(presenceSensor12) getTimeDiff(12,handler)
    if(presenceSensor13) getTimeDiff(13,handler)
    if(presenceSensor14) getTimeDiff(14,handler)
    if(presenceSensor15) getTimeDiff(15,handler)
    if(presenceSensor16) getTimeDiff(16,handler)
    if(presenceSensor17) getTimeDiff(17,handler)
    if(presenceSensor18) getTimeDiff(18,handler)
    if(presenceSensor19) getTimeDiff(19,handler)
    if(presenceSensor20) getTimeDiff(20,handler)
    if(myLock1) getTimeDiff(21,handler)
    if(myLock2) getTimeDiff(22,handler)
    if(myLock3) getTimeDiff(23,handler)
    if(myLock4) getTimeDiff(24,handler)

    if(logEnable) log.warn "In whosHere - handler: ${handler} - canSpeak: ${state.canSpeak}"
    letsDoSomething(handler)
}

def letsDoSomething(handler) {
    if(logEnable) log.debug "In letsDoSomething (${state.version}) - ${handler}"
    if(handler == "messageHomeNow" && state.canSpeak == "yes") messageHomeNow()
    if(handler == "messageDeparted" && state.canSpeak == "yes") messageDeparted()
    if(handler == "lock" && state.canSpeak == "yes") messageWelcomeHome()
	if(handler == "contact" && state.canSpeak == "yes") messageWelcomeHome()
    if(handler == "motion" && state.canSpeak == "yes") messageWelcomeHome()
  
    if(state.nameCount == 0 && rmEveryoneLeaves) rulesHandler(rmEveryoneLeaves)
    if(state.prevNameCount == 0 && state.nameCount > 0 && rmAnyoneReturns) rulesHandler(rmAnyoneReturns)
//    runIn(1,getGlobalBHStatus)
}

def getTimeDiff(numb,handler) {
    if(logEnable) log.debug "In getTimeDiff (${state.version})"
    whichPresenceSensor(numb)
    // returned from whichPresenceSensor - pSensor,lastActivity,fName,sendDataM,globalBH
    
    if(logEnable) log.debug "In getTimeDiff - ********** STARTING - ${numb} - ${fName} **********"
    
    if(logEnable) log.info "In getTimeDiff - returned from whichPresenceSensor - pSensor: ${pSensor} - lastActivity: ${lastActivity} - fName: ${fName} - sendDataM: ${sendDataM} - globalBH: ${globalBH}"
    
    if(globalBH == null || globalBH == "") {
        globalBH = "waitingForData"
        gvDevice."${sendDataM}"(globalBH)
    }

    if(timeHome == null || timeHome == "") timeHome = 5
    
    long timeDiff
   	def now = new Date()
    def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
    long unxNow = now.getTime()
    long unxPrev = prev.getTime()
    unxNow = unxNow/1000
    unxPrev = unxPrev/1000
    timeDiff = Math.abs(unxNow-unxPrev)
    timeDiff = Math.round(timeDiff/60)
    
    if(logEnable) log.info "In getTimeDiff - ${fName} - Starting as - timeDiff: ${timeDiff} - pSensor: ${pSensor} - globalBH: ${globalBH} - handler: ${handler}"

    if(pSensor == "present" || pSensor =="unlocked") {
        if(logEnable) log.info "In getTimeDiff (present/unlocked) - Here we go!"
        
        // ** Home Now **
        if(handler == "messageHomeNow") {
            if(timeDiff < 1) {
                if(globalBH != "announcingHomeNow") {
                    if(logEnable) log.debug "In getTimeDiff (present/homeNow) - ${fName} just got here! Time Diff: ${timeDiff} - globalBH: ${globalBH}"
			        state.nameCount = state.nameCount + 1
                    if(state.nameCount == 1) state.presenceMap = ["${fName}"]
			        if(state.nameCount >= 2) state.presenceMap += ["${fName}"]
			        state.canSpeak = "yes"
                    globalBH = "announcingHomeNow"
                    gvDevice."${sendDataM}"(globalBH)
                    if(logEnable) log.trace "In getTimeDiff (present/homeNow) - ${fName} - globalBH: ${globalBH} - Added to homeNow announcement."
                }
            } else {
                globalBH = "hasAnnouncedHomeNow"
                gvDevice."${sendDataM}"(globalBH)
                if(logEnable) log.trace "In getTimeDiff (present/homeNow) - ${fName} - globalBH: ${globalBH} - No homeNow announcement needed."
            }
        }
        
        // ** Welcome Home **
        if(timeDiff < timeHome) { 
            if(globalBH != "announcingHome") { 
		        if(logEnable) log.debug "In getTimeDiff (present/welcomeHome) - ${fName} just got here! Time Diff: ${timeDiff} - globalBH: ${globalBH}"
			    state.nameCount = state.nameCount + 1
                if(state.nameCount == 1) state.presenceMap = ["${fName}"]
		        if(state.nameCount >= 2) state.presenceMap += ["${fName}"]
			    state.canSpeak = "yes"
                globalBH = "announcingHome"
                gvDevice."${sendDataM}"(globalBH)
                if(logEnable) log.trace "In getTimeDiff (present/welcomeHome) - ${fName} - globalBH: ${globalBH} - Added to welcomeHome announcement."
            } else {
                if(logEnable) log.trace "In getTimeDiff (presentt/welcomeHome) - ${fName} - globalBH: ${globalBH} - welcomeHome announcement was already made."
            }
        } else {
            globalBH = "beenHome"
            gvDevice."${sendDataM}"(globalBH)
            if(logEnable) log.trace "In getTimeDiff (present) - ${fName} - globalBH: ${globalBH} - Home too long (${timeDiff}). No home announcements needed."
		}
	}
    
    if(pSensor == "not present" || pSensor == "locked") {
        if(logEnable) log.info "In getTimeDiff (not present/locked) - Here we go!"
        
        // ** justLeftHome **
        if(timeDiff < timeAway) {
            if(globalBH != "announcingJustLeft") {
                if(logEnable) log.debug "In getTimeDiff (not present/justLeftHome) - ${fName} just left! Time Diff: ${timeDiff} - globalBH: ${globalBH}"
                if(handler != "noMessage") {
			        state.nameCount = state.nameCount + 1
                    if(state.nameCount == 1) state.presenceMap = ["${fName}"]
			        if(state.nameCount >= 2) state.presenceMap += ["${fName}"]
			        state.canSpeak = "yes"
			        globalBH = "announcingJustLeft"
                    gvDevice."${sendDataM}"(globalBH)
                    if(logEnable) log.trace "In getTimeDiff (not present/justLeftHome) - ${fName} - globalBH: ${globalBH} - Added to justLeft announcement."
                } else {
                    globalBH = "notHome"
                    gvDevice."${sendDataM}"(globalBH)
                    if(logEnable) log.trace "In getTimeDiff (not present/justLeftHome) - ${fName} - globalBH: ${globalBH} - No away announcement needed."
                }
            } else {
                if(logEnable) log.trace "In getTimeDiff (not present/justLeftHome) - ${fName} - globalBH: ${globalBH} - justLeft announcement was already made."
            }
        // ** notHome **    
        } else {
            globalBH = "notHome"
            gvDevice."${sendDataM}"(globalBH)  
            if(logEnable) log.trace "In getTimeDiff (not present/notHome) - ${fName} - globalBH: ${globalBH} - Gone too long (${timeDiff}). No away announcements needed."
        }
    }
    if(logEnable) log.debug "In getTimeDiff - ********** END - ${numb} - ${fName} **********"
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
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', getName() )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
    if(logEnable) log.debug "In messageHomeNow - going to letsTalk with theMessage"
	letsTalk(theMessage)
}

def messageWelcomeHome() {   // Uses a modified version of @Matthew opening and closing message code
    checkTimeForGreeting()
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

        theMessage = "${state.greeting}, " + ovalues[orandomKey] + ". " + cvalues[crandomKey]
		if(logEnable) log.debug "In messageWelcomeHome - Random - ovSize: ${ovSize}, orandomKey: ${orandomKey}; Random - cvSize: ${cvSize}, crandomKey: ${crandomKey}, theMessage: ${theMessage}"
	} else {
		theMessage = "${state.greeting}, ${omessage}. ${cmessage}"
		if(logEnable) log.debug "In messageWelcomeHome - Static - theMessage: ${theMessage}"
	}
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', getName() )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
    if(delay1 == null || delay1 == "") delay1 = 5
    if(logEnable) log.debug "In messageWelcomeHome - Waiting ${delay1} seconds to Speak"
	def delay1ms = delay1 * 1000
	pauseExecution(delay1ms)
    if(logEnable) log.debug "In messageWelcomeHome - going to letsTalk with theMessage"
	letsTalk(theMessage)
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
	if (theMessage.contains("%name%")) {theMessage = theMessage.replace('%name%', getName() )}
	if (theMessage.contains("%is_are%")) {theMessage = theMessage.replace('%is_are%', "${is_are}" )}
	if (theMessage.contains("%has_have%")) {theMessage = theMessage.replace('%has_have%', "${has_have}" )}
    if(logEnable) log.debug "In messageDeparted - going to letsTalk with theMessage"
	letsTalk(theMessage)
}

def letsTalk(theMessage) {
	if(logEnable) log.debug "In letsTalk (${state.version})"
	checkTime()
	checkVol()
    if(state.timeBetween == true) {
		theMsg = theMessage
        speechDuration = Math.max(Math.round(theMsg.length()/12),2)+3		// Code from @djgutheinz
        speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, theMsg: ${theMsg}, volRestore: ${volRestore}"
        state.speakers.each { it ->
            if(logEnable) log.debug "Speaker in use: ${it}"
            if(speakerProxy) {
                if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                it.speak(theMsg)
            } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                def prevVolume = it.currentValue("volume")
                it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
            } else if(it.hasCommand('playTextAndRestore')) {   
                if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                def prevVolume = it.currentValue("volume")
                it.playTextAndRestore(theMsg, prevVolume)
            } else {		        
                if(logEnable) log.debug "In letsTalk - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                it.speak(theMsg)
                pauseExecution(speechDuration2)
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
            }
        }
        state.canSpeak = "no"
	    if(logEnable) log.debug "In letsTalk - Finished speaking"  
	    log.info "${app.label} - ${theMsg}"
        if(sendPushMessage) pushNow(theMsg)
	} else {
        state.canSpeak = "no"
        if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def checkVol(){
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - setting volume: ${state.volume}"
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
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def checkTimeForGreeting() {
    if(logEnable) log.debug "In checkTimeForGreeting (${state.version}) - G1 - ${fromTimeG1} - ${toTimeG1}"
    state.betweenTimeG1 = timeOfDayIsBetween(toDateTime(fromTimeG1), toDateTime(toTimeG1), new Date(), location.timeZone)
    if(state.betweenTimeG1) {
        if(oRandomG1) {
		    def values = "${greeting1}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    	    def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			if(logEnable) log.debug "In checkTimeForGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm: ${timeampm} - timehh: ${timeHH}"
		} else {
			state.greeting = "${greeting1}"
			if(logEnable) log.debug "In checkTimeForGreeting - Static - greeting: ${state.greeting}"
		}
    }
    
    if(logEnable) log.debug "In checkTimeForGreeting (${state.version}) - G2 - ${fromTimeG2} - ${toTimeG2}"
    state.betweenTimeG2 = timeOfDayIsBetween(toDateTime(fromTimeG2), toDateTime(toTimeG2), new Date(), location.timeZone)
    if(state.betweenTimeG2) {
        if(oRandomG2) {
		    def values = "${greeting2}".split(";")
		    vSize = values.size()
		    count = vSize.toInteger()
    	    def randomKey = new Random().nextInt(count)
		    state.greeting = values[randomKey]
		    if(logEnable) log.debug "In checkTimeForGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm: ${timeampm} - timehh: ${timeHH}"
		} else {
		    state.greeting = "${greeting2}"
		    if(logEnable) log.debug "In checkTimeForGreeting - Static - greeting: ${state.greeting}"
        }
    }

    if(logEnable) log.debug "In checkTimeForGreeting (${state.version}) - G3 - ${fromTimeG3} - ${toTimeG3}"
    state.betweenTimeG3 = timeOfDayIsBetween(toDateTime(fromTimeG3), toDateTime(toTimeG3), new Date(), location.timeZone)
    if(state.betweenTimeG3) {
        if(oRandomG3) {
		    def values = "${greeting3}".split(";")
			vSize = values.size()
			count = vSize.toInteger()
    		def randomKey = new Random().nextInt(count)
			state.greeting = values[randomKey]
			if(logEnable) log.debug "In checkTimeForGreeting - Random - vSize: ${vSize}, randomKey: ${randomKey}, greeting: ${state.greeting} timeampm = ${timeampm} - timehh = ${timeHH}"
		} else {
			state.greeting = "${greeting3}"
			if(logEnable) log.debug "In checkTimeForGreeting - Static - greeting: ${state.greeting}"
	    }
    }
}

private getNameNEW() {
    if(logEnable) log.debug "In getName (${state.version}) - Number of Names: ${state.nameCount}, Names: ${state.presenceMap}"
    presenceMap = state.presenceMap.unique()
    nameCount = presenceMap.size()
	name = ""
	
    if(logEnable) log.debug "In getName - presenceMap: ${presenceMap}"
	presenceMap.each { it -> 
        for (i = 1; i <= nameCount; i++) {
            if(logEnable) log.debug "*********** In getName - B - nameCount: ${nameCount} - Working on: ${i} - name: ${name}"
            if(!name.contains("${it}")) {
                if(logEnable) log.debug "*********** In getName - MATCH - Working on: ${i} - name: ${name}"
                if(i == 1) {
                    name = "${it}"
                } else if(i > 1 && i < nameCount) {
                    name = "${name}" + ", ${it}"
                } else {
                    name = "${name}" + " and ${it}"
                }
            }
            if(logEnable) log.debug "*********** In getName - A - nameCount: ${nameCount} - Working on: ${i} - name: ${name}"
		}
    }
  
    is_are = (name.contains(' and ') ? 'are' : 'is')
	has_have = (name.contains(' and ') ? 'have' : 'has')
    
	if(name == null || name == "") names = "Whoever you are"
    
	if(logEnable) log.debug "Name: ${name}"
	return name
}

private getName(){
	if(logEnable) log.debug "In getName (${state.version}) - Number of Names: ${state.nameCount}, Names: ${state.presenceMap}"
    presenceMap = state.presenceMap.unique()
    nameCount = presenceMap.size()
	name = ""
	myCount = 0
	if(nameCount == 1) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=1: myCount = ${myCount}"
			name = "${it}"
		}
	}
	if(nameCount == 2) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=2: myCount: ${myCount}"
			myCount = myCount + 1
            if(!name.contains("${it}")) {
			    name = "${name}" + "${it} "
			    if(myCount == 1) name = "${name}" + "and "
            }
		}
		name = "${name}" + "!"
	}
	if(nameCount == 3) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=3: myCount: ${myCount}"
			myCount = myCount + 1
            if(!name.contains("${it}")) {
			    name = "${name}" + "${it}, "
			    if(myCount == 2) name = "${name}" + "and "
            }
		}
	}
	if(nameCount == 4) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=4: myCount: ${myCount}"
			myCount = myCount + 1
            if(!name.contains("${it}")) {
			    name = "${name}" + "${it}, "
			    if(myCount == 3) name = "${name}" + "and "
            }
		}
	}
	if(nameCount == 5) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=5: myCount: ${myCount}"
			myCount = myCount + 1
            if(!name.contains("${it}")) {
			    name = "${name}" + "${it}, "
			    if(myCount == 4) name = "${name}" + "and "
            }
		}
	}
    if(nameCount == 6) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=6: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 5) name = "${name}" + "and "
		}
	}
    if(nameCount == 7) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=7: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 6) name = "${name}" + "and "
		}
	}
    if(nameCount == 8) {
		presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=8: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 7) name = "${name}" + "and "
		}
	}
    if(nameCount == 9) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=9: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 8) name = "${name}" + "and "
		}
	}
    if(nameCount == 10) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=10: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 9) name = "${name}" + "and "
		}
	}
    if(nameCount == 11) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=11: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 10) name = "${name}" + "and "
		}
	}
    if(nameCount == 12) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=12: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 11) name = "${name}" + "and "
		}
	}
    if(nameCount == 13) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=13: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 12) name = "${name}" + "and "
		}
	}
    if(nameCount == 14) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=14: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 13) name = "${name}" + "and "
		}
	}
    if(nameCount == 15) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=15: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 14) name = "${name}" + "and "
		}
	}
    if(nameCount == 16) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=16: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 15) name = "${name}" + "and "
		}
	}
    if(nameCount == 17) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=17: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 16) name = "${name}" + "and "
		}
	}
    if(nameCount == 18) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=18: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 17) name = "${name}" + "and "
		}
	}
    if(nameCount == 19) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=19: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 18) name = "${name}" + "and "
		}
	}
    if(nameCount == 20) {
		state.presenceMap.each { it -> 
			if(logEnable) log.debug "*********** In nameCount=20: myCount: ${myCount}"
			myCount = myCount + 1
			name = "${name}" + "${it}, "
			if(myCount == 19) name = "${name}" + "and "
		}
	}
  
    is_are = (name.contains(' and ') ? 'are' : 'is')
	has_have = (name.contains(' and ') ? 'have' : 'has')
    
	if(name == null || name == "") names = "Whoever you are"
    
	if(logEnable) log.debug "Name: ${name}"
	return name
}

def rulesHandler(rules) {
    if(logEnable) log.debug "In rulesHandler - rules: ${rules}"
    RMUtils.sendAction(rules, "runRule", app.label)
}

def pushNow(msg) {
	if(logEnable) log.debug "In pushNow (${state.version})"
	if(sendPushMessage) {
		pushMessage = "${app.label} \n"
		pushMessage += msg
		if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
        sendPushMessage.deviceNotification(pushMessage)
	}	
}

def getGlobalBHStatus() {
    if(logEnable) log.debug "In getGlobalBHStatus (${state.version})"
    CheckGlobalBH01=gvDevice.currentValue("globalBH01")
    CheckGlobalBH02=gvDevice.currentValue("globalBH02")
    CheckGlobalBH03=gvDevice.currentValue("globalBH03")
    CheckGlobalBH04=gvDevice.currentValue("globalBH04")
    CheckGlobalBH05=gvDevice.currentValue("globalBH05")
    CheckGlobalBH06=gvDevice.currentValue("globalBH06")
    CheckGlobalBH07=gvDevice.currentValue("globalBH07")
    CheckGlobalBH08=gvDevice.currentValue("globalBH08")
    CheckGlobalBH09=gvDevice.currentValue("globalBH09")
    CheckGlobalBH10=gvDevice.currentValue("globalBH10")
    CheckGlobalBH11=gvDevice.currentValue("globalBH11")
    CheckGlobalBH12=gvDevice.currentValue("globalBH12")
    CheckGlobalBH13=gvDevice.currentValue("globalBH13")
    CheckGlobalBH14=gvDevice.currentValue("globalBH14")
    CheckGlobalBH15=gvDevice.currentValue("globalBH15")
    CheckGlobalBH16=gvDevice.currentValue("globalBH16")
    CheckGlobalBH17=gvDevice.currentValue("globalBH17")
    CheckGlobalBH18=gvDevice.currentValue("globalBH18")
    CheckGlobalBH19=gvDevice.currentValue("globalBH19")
    CheckGlobalBH20=gvDevice.currentValue("globalBH20")
    
    CheckGlobalBH21=gvDevice.currentValue("globalBH21")
    CheckGlobalBH22=gvDevice.currentValue("globalBH22")
    CheckGlobalBH23=gvDevice.currentValue("globalBH23")
    CheckGlobalBH24=gvDevice.currentValue("globalBH24")
    
    if(logEnable) log.debug "********** Start Global Status Check **********"
    
    if(CheckGlobalBH01) {if(logEnable) log.debug "Checking - GlobalBH01: ${CheckGlobalBH01}"}
    if(CheckGlobalBH02) {if(logEnable) log.debug "Checking - GlobalBH02: ${CheckGlobalBH02}"}
    if(CheckGlobalBH03) {if(logEnable) log.debug "Checking - GlobalBH03: ${CheckGlobalBH03}"}
    if(CheckGlobalBH04) {if(logEnable) log.debug "Checking - GlobalBH04: ${CheckGlobalBH04}"}
    if(CheckGlobalBH05) {if(logEnable) log.debug "Checking - GlobalBH05: ${CheckGlobalBH05}"}
    if(CheckGlobalBH06) {if(logEnable) log.debug "Checking - GlobalBH06: ${CheckGlobalBH06}"}
    if(CheckGlobalBH07) {if(logEnable) log.debug "Checking - GlobalBH07: ${CheckGlobalBH07}"}
    if(CheckGlobalBH08) {if(logEnable) log.debug "Checking - GlobalBH08: ${CheckGlobalBH08}"}
    if(CheckGlobalBH09) {if(logEnable) log.debug "Checking - GlobalBH09: ${CheckGlobalBH09}"}
    if(CheckGlobalBH10) {if(logEnable) log.debug "Checking - GlobalBH10: ${CheckGlobalBH10}"}
    if(CheckGlobalBH11) {if(logEnable) log.debug "Checking - GlobalBH11: ${CheckGlobalBH11}"}
    if(CheckGlobalBH12) {if(logEnable) log.debug "Checking - GlobalBH12: ${CheckGlobalBH12}"}
    if(CheckGlobalBH13) {if(logEnable) log.debug "Checking - GlobalBH13: ${CheckGlobalBH13}"}
    if(CheckGlobalBH14) {if(logEnable) log.debug "Checking - GlobalBH14: ${CheckGlobalBH14}"}
    if(CheckGlobalBH15) {if(logEnable) log.debug "Checking - GlobalBH15: ${CheckGlobalBH15}"}
    if(CheckGlobalBH16) {if(logEnable) log.debug "Checking - GlobalBH16: ${CheckGlobalBH16}"}
    if(CheckGlobalBH17) {if(logEnable) log.debug "Checking - GlobalBH17: ${CheckGlobalBH17}"}
    if(CheckGlobalBH18) {if(logEnable) log.debug "Checking - GlobalBH18: ${CheckGlobalBH18}"}
    if(CheckGlobalBH19) {if(logEnable) log.debug "Checking - GlobalBH19: ${CheckGlobalBH19}"}
    if(CheckGlobalBH20) {if(logEnable) log.debug "Checking - GlobalBH20: ${CheckGlobalBH20}"}
    
    if(CheckGlobalBH21) {if(logEnable) log.debug "Checking - GlobalBH21: ${CheckGlobalBH21}"}
    if(CheckGlobalBH22) {if(logEnable) log.debug "Checking - GlobalBH22: ${CheckGlobalBH22}"}
    if(CheckGlobalBH23) {if(logEnable) log.debug "Checking - GlobalBH23: ${CheckGlobalBH23}"}
    if(CheckGlobalBH24) {if(logEnable) log.debug "Checking - GlobalBH24: ${CheckGlobalBH24}"}
 
    if(logEnable) log.debug "********** End Global Status Check **********"   
}

// ********** Normal Stuff **********

def setDefaults(){
	if(state.presenceMap == null) state.presenceMap = [:]
	if(logEnable == null){logEnable = false}
	state.nameCount = 0
	state.canSpeak = "no"
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

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " Home Tracker - ${theName}")) {
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
