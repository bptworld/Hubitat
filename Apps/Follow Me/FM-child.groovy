/**
 *  ****************  Follow Me App  ****************
 *  Design Usage:
 *  Never miss a message again. Send messages to your occupied room speakers when home or by push when away. Automatically!
 *
 *  Copyright 2019-2021 Bryan Turcotte (@bptworld)
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  2.3.3 - 01/14/21 - Added 'App Description'
 *  2.3.2 - 01/13/21 - Fixed a typo
 *  2.3.1 - 01/12/21 - Adjustments to Priority processing
 *  2.3.0 - 01/05/21 - Adjustments to Speech Queue, more logging options, cosmetic changes
 *  ---
 *  1.0.0 - 03/17/19 - Initial release.
 *
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat
    
def setVersion(){
    state.name = "Follow Me"
	state.version = "2.3.3"
}

definition(
    name: "Follow Me Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Never miss a message again. Send messages to your occupied room speakers when home or by push when away. Automatically!",
    category: "",
	parent: "BPTWorld:Follow Me",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Follow%20Me/FM-child.groovy",
)

preferences {
    page(name: "pageConfig")
	page name: "pushOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "voiceOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "testSpeaker", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "speakerStatus", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "", nextPage: null, install: true, uninstall: true) {
		display()
		getVoices()
        state.appD = ""
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
            speakerNotes =  "<b>Speakers:</b><br>"
            speakerNotes += "- Create a new child app for each room that has a speaker in it you want to control."
            
            pushNotes =  "<b>Push:</b><br>"
            pushNotes += "- Only one child app is need for up to 5 pressence sensors<br>"
            pushNotes += "- If more than 5 sensors are needed, simply add another child app."
            
            pmNotes =  "<b>Priority Messages</b><br>"
            pmNotes += "- Each message sent to 'Follow Me' can have a priority assigned to it.<br>"
            pmNotes += "- Volume levels can also be adjusted by priority level."
          
            sAbilities = "Remember: Not all speakers can use volume controls, play sounds and/or restore to what it was doing before the speech event. Please use the report below to see some known speaker abilities."
            
            paragraph "${speakerNotes}"
            paragraph "${pushNotes}"
            paragraph "${pmNotes}"
            paragraph "${sAbilities}"

            paragraph "<hr>"
            href "speakerStatus", title: "Known Speaker Abilities", description: "Click to see report."
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Speech Device")) {
            paragraph "This app requires a 'virtual device' to 'catch' the speech and send it here. All child apps will share this device."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have FM create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this virtual Device (ie. 'Follow Me')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "gvDevice", "capability.actuator", title: "Virtual Device created for FM", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Follow Me Driver'.</small>"
            }
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Message Destination")) {
    		//input "messageDest", "enum", title: "Select message destination", submitOnChange: true, options: ["Speakers","Push","Queue"], required: true
            input "messageDest", "enum", title: "Select message destination", submitOnChange: true, options: ["Speakers","Push"], required: true
            state.appD += "<b>Destination</b>: ${messageDest}<br>"
		}
        
		// Speakers
		if(messageDest == "Speakers") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Activation Type for Room Speakers")) {
    			input "triggerMode", "enum", title: "Select message activation Type", submitOnChange: true, options: ["Always_On","Contact_Sensor","Motion_Sensor","Presence_Sensor","Switch"], required: true, Multiple: false
				if(triggerMode == "Always_On"){
					paragraph "Selected speakers will always play messages."	
				}
				if(triggerMode == "Contact_Sensor"){
					input "myContacts", "capability.contactSensor", title: "Select the contact sensor(s) to activate the speaker", required: true, multiple: true
					input "contactOption", "enum", title: "Select contact option - If (option), Speaker is On", options: ["Open","Closed"], required: true
					input "sZoneWaiting", "number", title: "After contact changes, wait X minutes to turn the speaker off", required: true, defaultValue: 5
                    state.appD += "<b>Activation Type</b>: ${triggerMode} - Device: ${myContacts} - Option: ${contactOption} - Minutes to Off: ${sZoneWaiting}<br>"
                } else {
                    state.appD -= "<b>Activation Type</b>: ${triggerMode} - Device: ${myContacts} - Option: ${contactOption} - Minutes to Off: ${sZoneWaiting}<br>"
                    app.removeSetting("myContacts")
                    app.removeSetting("contactOption")
                }
				if(triggerMode == "Motion_Sensor"){
					input "myMotion", "capability.motionSensor", title: "Select the motion sensor(s) to activate the speaker", required: true, multiple: true
					input "sZoneWaiting", "number", title: "After motion stops, wait X minutes to turn the speaker off", required: true, defaultValue: 5
                    state.appD += "<b>Activation Type</b>: ${triggerMode} - Device: ${myMotion} - Minutes to Off: ${sZoneWaiting}"
				} else {
                    state.appD -= "<b>Activation Type</b>: ${triggerMode} - Device: ${myMotion} - Minutes to Off: ${sZoneWaiting}"
                    app.removeSetting("myMotion")
                }
                if(triggerMode == "Presence_Sensor"){
					input "myPresence", "capability.presenceSensor", title: "Select the presence sensor(s) to activate the speaker", required: true, multiple: true
					input "sZoneWaiting", "number", title: "After becoming not present, wait X minutes to turn the speaker off", required: true, defaultValue: 5
                    state.appD += "<b>Activation Type</b>: ${triggerMode} - Device: ${myPresence} - Minutes to Off: ${sZoneWaiting}<br>"
				} else {
                    state.appD -= "<b>Activation Type</b>: ${triggerMode} - Device: ${myPresence} - Minutes to Off: ${sZoneWaiting}<br>"
                    app.removeSetting("myPresence")
                }
				if(triggerMode == "Switch"){
					input "mySwitches", "capability.switch", title: "Select Switch(es) to activate the speaker", required: true, multiple: true
					input "sZoneWaiting", "number", title: "After Switch is off, wait X minutes to turn the speaker off", required: true, defaultValue: 5
                    state.appD += "<b>Activation Type</b>: ${triggerMode} - Device: ${mySwitches} - Minutes to Off: ${sZoneWaiting}<br>"
				} else {
                    state.appD -= "<b>Activation Type</b>: ${triggerMode} - Device: ${mySwitches} - Minutes to Off: ${sZoneWaiting}<br>"
                    app.removeSetting("mySwitches")
                }
			}
			
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
                paragraph "Please select your speakers below.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
              	input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
         		input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true         
                input "speakerType", "enum", title: "Select Speaker Type", options: [
                    ["echoSpeaksSpeaker":"Echo Speaks Device"],
                    ["googleSpeaker":"Google/Nest Device"],
                    ["sonosSpeaker":"Sonos Device"],
                    ["otherSpeaker":"Other Speaker"],
                ], required: true, multiple: false, submitOnChange: true
                
                state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
                
                if(speakerType == "echoSpeaksSpeaker") {
                    paragraph "<b>Speaker type is an Echo Speaks Device. Echo devices can not play a custom sound or change voices.</b>"
                }
                if(speakerType == "googleSpeaker") {
                    paragraph "<b>Speaker type is a Google/Nest Device. Google/Nest devices can play custom sounds and change voices.</b>"
                    input "gInitialize", "bool", title: "When using Google/Nest devices sometimes an Initialize is necessary (not always). Initialize Google/Nest devices before sending speech?", required: true, defaultValue: false, submitOnChange:true
                    if(gInitialize) {
                        input "gInitRepeat", "number", title: "Initialize Google/Nest devices every X minutes? (recommended: 4)", required: false
                    } else {
                        app.updateSetting("gInitRepeat",[value:"false",type:"bool"])
                    }
                }
                if(speakerType == "sonosSpeaker") {
                    paragraph "<b>Speaker type is a Sonos Device. Sonos devices can play custom sounds and change voices.</b>"
                }
                if(speakerType == "otherSpeaker") {
                    paragraph "<b>Speaker type is an Other Device.</b>"
                }
                paragraph "<b>Note:</b> Some speakers just don't play nicely with Follow Me. If your speaker is having an issue, please try turning this switch on."
                input "defaultSpeak", "bool", title: "Use default 'speak'", defaultValue:false, submitOnChange:true
                if(speakerType == "googleSpeaker") {
                    state.appD += "<b>Speaker Option</b>: MediaPlayer: ${speakerMP} - SpeechSynthesis: ${speakerSS} - ${speakerType} - Initialize: ${gInitRepeat} - Use Default: ${defaultSpeak}<br>"
                } else {
                    state.appD += "<b>Speaker Option</b>: MediaPlayer: ${speakerMP} - SpeechSynthesis: ${speakerSS} - ${speakerType} - Use Default: ${defaultSpeak}<br>"
                }
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Volume Options")) {
                paragraph "<b>If the command sent doesn't have the ability to set the volume, this app will try to do it.<br>It will also return the volume to the previous state after the speech.</b>"
                input "volSpeech", "number", title: "Speaker volume for speech (if not automatic)", description: "0-100", required:true
          		paragraph "<hr>"
                paragraph "<b>Quiet Time Override Options</b><br>This will override any other volume settings."
                input "useQuietTime", "bool", title: "Use Quiet Time", defaultValue:false, submitOnChange:true
                if(useQuietTime) {
                    input "volQuiet", "number", title: "Quiet Time Speaker volume", description: "0-100", required:false, submitOnChange:true
                    input "QfromTime", "time", title: "Quiet Time Start", required: false, width: 6
                    input "QtoTime", "time", title: "Quiet Time End", required: false, width: 6
                    input "midnightCheckQ", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                } else {
                    app.removeSetting("volQuiet")
                    app.removeSetting("QfromTime")
                    app.removeSetting("QtoTime")
                    app.removeSetting("midnightCheckQ")
                }
                paragraph "<hr>"
		        paragraph "<b>Speech Restriction Options</b><br>Speech can also be restricted to within a certain time frame."
                input "useSpeechRestriction", "bool", title: "Use Speech Restriction", defaultValue:false, submitOnChange:true
                if(useSpeechRestriction) {
                    input "fromTime", "time", title: "From", required: false, width: 6, submitOnChange:true
                    input "toTime", "time", title: "To", required: false, width: 6
                    input "midnightCheckR", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                } else {
                    app.removeSetting("fromTime")
                    app.removeSetting("toTime")
                    app.removeSetting("midnightCheckR")
                }
                state.appD += "<b>Volume Options</b>:<br> - Speaker Volume: ${volSpeech} - Quiet Time: ${useQuietTime} - Q Volume: ${volQuiet} - Q From: ${QfromTime} - Q To: ${QtoTime} - Q Midnight: ${midnightCheckQ}<br>"
                state.appD += " - Speech Restriction: ${useSpeechRestriction} - R From: ${fromTime} - R To: ${toTime} - R Midnight: ${midnightCheckR}<br>"
			}
            
			section(getFormat("header-green", "${getImage("Blank")}"+" Message Priority (Advanced Options)")) {
                paragraph "The following are considered 'Advanced Options'. They should only be used once the app is up and running."
				input "messagePriority", "bool", defaultValue:false, title: "Use Message Priority features", description: "Message Priority", submitOnChange:true
				if((messagePriority) && (speakerSS) && (speakerType != "echoSpeaksSpeaker")) input "priorityVoices", "bool", defaultValue:false, title: "Use different voices for each Priority level", description: "Priority Voices", submitOnChange:true
				if((messagePriority) && (speakerSS) && (speakerType != "echoSpeaksSpeaker")) input "messageSounds", "bool", defaultValue:false, title: "Play a sound before message", description: "Message Sounds", submitOnChange:true
                state.appD += "<b>Message Priority</b>: ${messagePriority} - priorityVoices: ${priorityVoices} - messageSounds: ${messageSounds}<br>"
			}
            
			if(messagePriority) {
				section("Instructions for Message Priority:", hideable: true, hidden: true) {
					paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place the option bracket in front of any message to be spoken and the Volume, Voice and/or Speaker will be adjusted accordingly."
                    paragraph "Format: [priority:sound:speaker]<br><small>Note: Any option not needed, replace with a 0 (zero).</small>"
                    
                    paragraph "<b>Priority:</b><br>This can change the voice used and the color of the message displayed on the Dashboard Tile.<br>[F:0:0] - Fun<br>[R:0:0] - Random<br>[L:0:0] - Low<br>[N:0:0] - Normal<br>[H:0:0] - High"
					
                    paragraph "<b>Sound:</b><br>You can also specify a sound file to be played before a message!<br>[1] - [5] - Specify a files URL"
					paragraph "<b>ie.</b> [L:0:0]Amy is home or [N:3:0]Window has been open too long or [H:0:0]Heat is on and window is open"
                    paragraph "If you JUST want a sound file played with NO speech after, use [L:1:0]. or [N:3:0]. etc. Notice the DOT after the [], that is the message and will not be spoken."
                    
                    paragraph "<b>Speaker:</b><br>While Follow Me allows you to setup your speakers in many ways, sometimes you want it to ONLY speak on a specific device. This option will do just that! Just replace with the corresponding speaker number from the Follow Me Parent App."
                    paragraph "<b>*</b> <i>Be sure to have the 'Priority Speaker Options' section completed in the Follow Me Parent App.</i>"
                    
                    paragraph "<hr>"
					paragraph "<b>General Notes:</b>"
                    paragraph "Priority Voice and Sound options are only available when using Speech Synth option.<br>Also notice there is no spaces between the option and the message."
                    paragraph "<b>ie.</b> [N:3:0]Window has been open too long"
				}
				section() {
					paragraph "Normal priority will use the standard volume set in the 'Speaker Options' Section"
					input "volLow", "number", title: "Speaker volume for Low priority", description: "0-100", required:true, width:4
                    paragraph "Speaker volume for Normal priority<br>${volSpeech}", width:4
					input "volHigh", "number", title: "Speaker volume for High priority", description: "0-100", required:true, width:4
                    state.appD += " - Low: ${volLow} - High: ${volHigh}<br>"
				}
				if(speakerSS && (speakerType != "echoSpeaksSpeaker")) {
                    if(priorityVoices) {
                        section(getFormat("header-green", "${getImage("Blank")}"+" Voice Options")) {
                            href "voiceOptions", title:"Voice Options Setup", description:"Click here to setup the voices"
                        }
					}
				    if(messageSounds) {
					    section(getFormat("header-green", "${getImage("Blank")}"+" Sound Options")) {
					    	paragraph "Sound files can be setup in the Parent App."
					    }
                    }
				} else {
					section() {
						paragraph "* Priority Voice and Sound options are only available when using Speech Synth option."
					}
				}
            } else {
                state.appD -= " - Low: ${volLow} - High: ${volHigh}<br>"
            }
		}
        
		// Push
		if(messageDest == "Push") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options for Push Messages")) {
				href "pushOptions", title:"Presence and Push Setup", description:"Select up to 5 presence sensor / push combinations"
			}
		}
        
        if(messageDest == "Queue") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options for Queued Messages")) {
				paragraph "With this option, choose which Presence Sensors to have messages Queued for when they arrive back home."
			}
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Speech Queue (Experimental)")) {
            paragraph "Follow Me can use a custom speech queue. If you would like to try this experimental queueing system, turn this switch on."
            input "useQueue", "bool", defaultValue:false, title: "Use speech queueing", description: "speech queue", submitOnChange:true
            
            if(useQueue) {
                paragraph "Follow Me uses a custom speech queue. Sometimes it gets 'stuck' and queues all the messages. To recover from this, please use the options below."
                input "logQueue", "bool", title: "Enable Queue Debug Logging", description: "Enable extra logging for debugging.", defaultValue:false, submitOnChange:true
			    input "maxQueued", "number", title: "Max number of messages to be queued before auto clear is issued (default=5)", required: true, defaultValue: 5
                input "clearQueue", "bool", defaultValue:false, title: "Manually Clear the Queue right now", description: "Clear", submitOnChange:true, width:6
                if(clearQueue) clearTheQueue()
            
                input "showQueue", "bool", defaultValue:false, title: "Update the current Queue display below", description: "Show", submitOnChange:true, width:6
                if(showQueue) showTheQueue()
                def now = new Date()
                paragraph "<b>Current Queue</b> - ${now}"
                paragraph "${state.TTSQueue}"
                paragraph "<small>* Blank [] is good! Mulitple messages is not!</small>"
                paragraph "<hr>"
                state.appD += "<b>Speech Queue</b>: ${useQueue}<br>"
                state.appD += " - logQueue: ${logQueue} - maxQueued: ${maxQueued}<br>"
            } else {
                state.appD -= " - logQueue: ${logQueue} - maxQueued: ${maxQueued}<br>"
                app?.updateSetting("logQueue",[value:"false",type:"bool"])
                clearTheQueue()
            }
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" The App Description")) {
            paragraph "This will give a short description on how the App will operate. This is also an easy way to share how to do things. Just copy the text below and post it on the HE forums!"
            paragraph "<hr>"
            paragraph "<b>Follow Me App (${state.version}) - ${app.label}</b>"
            if(state.appD) paragraph state.appD.replaceAll("null","NA")
            if(state.pushD) paragraph state.pushD.replaceAll("null","NA")
            if(state.priorityV) paragraph state.priorityV.replaceAll("null","NA")
            paragraph "<hr>"
            paragraph "<small>* If you're not seeing your descriptions from a certain section, please re-visit that section.</small>"
            input "resetApp", "bool", defaultValue:false, title: "Refresh The App Description <small>(This will happen immediately)</small>", description: "App", submitOnChange:true
            if(resetApp) {
                app.updateSetting("resetApp",[value:"false",type:"bool"])
            }
        }
		display2()
	}
}

def pushOptions(){
    dynamicPage(name: "pushOptions", title: "Presence and Push Setup", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Presence Options for Push Messages")) {
			paragraph "Select up to 5 presence sensor/Push Device combinations to receive messages when away from home."
			paragraph "<b>Combination 1</b>"
			input("presenceSensor1", "capability.presenceSensor", title: "Presence Sensor 1", required: false, width: 6)
			input("sendPushMessage1", "capability.notification", title: "Push Device 1", required: false, width: 6)
			paragraph "<b>Combination 2</b>"
			input("presenceSensor2", "capability.presenceSensor", title: "Presence Sensor 2", required: false, width: 6)
			input("sendPushMessage2", "capability.notification", title: "Push Device 2", required: false, width: 6)
			paragraph "<b>Combination 3</b>"
			input("presenceSensor3", "capability.presenceSensor", title: "Presence Sensor 3", required: false, width: 6)
			input("sendPushMessage3", "capability.notification", title: "Push Device 3", required: false, width: 6)
			paragraph "<b>Combination 4</b>"
			input("presenceSensor4", "capability.presenceSensor", title: "Presence Sensor 4", required: false, width: 6)
			input("sendPushMessage4", "capability.notification", title: "Push Device 4", required: false, width: 6)
			paragraph "<b>Combination 5</b>"
			input("presenceSensor5", "capability.presenceSensor", title: "Presence Sensor 5", required: false, width: 6)
			input("sendPushMessage5", "capability.notification", title: "Push Device 5", required: false, width: 6)
            
            state.pushD = "<b>Presence and Push Setup</b>:<br>"
            if(presenceSensor1 || sendPushMessage1) state.pushD += " - presenceSensor1: ${presenceSensor1} - sendPushMessage1: ${sendPushMessage1}<br>"
            if(presenceSensor2 || sendPushMessage2) state.pushD += " - presenceSensor2: ${presenceSensor2} - sendPushMessage2: ${sendPushMessage2}<br>"
            if(presenceSensor3 || sendPushMessage3) state.pushD += " - presenceSensor3: ${presenceSensor3} - sendPushMessage3: ${sendPushMessage3}<br>"
            if(presenceSensor4 || sendPushMessage4) state.pushD += " - presenceSensor4: ${presenceSensor4} - sendPushMessage4: ${sendPushMessage4}<br>"
            if(presenceSensor5 || sendPushMessage5) state.pushD += " - presenceSensor5: ${presenceSensor5} - sendPushMessage5: ${sendPushMessage5}<br>"
		}
	}
}		

def voiceOptions(){
    dynamicPage(name: "voiceOptions", title: "Voice Option Setup", install: false, uninstall:false){
		section("Select Voices for different priorities") {
            input "testTheSpeakers", "capability.speechSynthesis", title: "Choose speaker for testing", required: true, submitOnChange: true
            input "testPhrase", "text", title: "Test Phrase", required: true, defaultValue: "This is a test", submitOnChange: true              
			input "voiceFun", "enum", title: "Select Voice for priority - Fun", options: state.list, required: false, submitOnChange: true, width: 7
            if(voiceFun && testTheSpeakers) input "testVoiceFun", "button", title: "Test Voice Fun", width: 5
			input "voiceRandom", "enum", title: "Select Voice for priority - Random (Must select 2 or more)", options: state.list, required: false, multiple: true, submitOnChange: true, width: 7
            if(voiceRandom && testTheSpeakers) input "testVoiceRandom", "button", title: "Test Voice Random", width: 5
			input "voiceLow", "enum", title: "Select Voice for priority - Low", options: state.list, required: false, submitOnChange: true, width: 7
            if(voiceLow && testTheSpeakers) input "testVoiceLow", "button", title: "Test Voice Low", width: 5
			input "voiceNorm", "enum", title: "Select Voice for priority - Normal", options: state.list, required: false, submitOnChange: true, width: 7
            if(voiceNorm && testTheSpeakers) input "testVoiceNorm", "button", title: "Test Voice Normal", width: 5
		    input "voiceHigh", "enum", title: "Select Voice for priority - High", options: state.list, required: false, submitOnChange: true, width: 7
            if(voiceHigh && testTheSpeakers) input "testVoiceHigh", "button", title: "Test Voice High", width: 5
            
            state.priorityV = "<b>Voice Option Setup</b>:<br>"
            if(voiceFun) state.priorityV += " - voiceFun: ${voiceFun}<br>"
            if(voiceRandom) state.priorityV += " - voiceFun: ${voiceRandom}<br>"
            if(voiceLow) state.priorityV += " - voiceFun: ${voiceLow}<br>"
            if(voiceNorm) state.priorityV += " - voiceFun: ${voiceNorm}<br>"
            if(voiceHigh) state.priorityV += " - voiceFun: ${voiceHigh}<br>"
        }
	}
}		

def speakerStatus(){
    dynamicPage(name: "pushOptions", title: "What each type of Speaker can do", install: false, uninstall:false){
        section() {
            paragraph "This table is provided soley for informational purposes only. Hopefully it will remove some confusion on what speakers can and can not do. Hope this helps!"   
        }
		section() {
			voiceSpeakers = "<table align=center width=100%>"
            voiceSpeakers += "<tr><td><b>Speaker Type</b></td><td> - </td><td><b>Play<br>Sounds</b></td><td><b>Change<br>Voices</b></td><td><b>Auto Restore<br>Volume</b></td></tr>"
            voiceSpeakers += "<tr><td>Google/Nest</td><td> - </td><td>yes</td><td>yes</td><td>no</td></tr>"
            voiceSpeakers += "<tr><td>Echo</td><td> - </td><td>no</td><td>no</td><td>yes</td></tr>"
            voiceSpeakers += "<tr><td>Sonos</td><td> - </td><td>yes</td><td>yes</td><td>yes</td></tr>"
            voiceSpeakers += "</table>"
            paragraph "${voiceSpeakers}"
            paragraph "<hr>"
// ** Abilites List **
            byApp = "<table align=center width=100%>"
            byApp += "<tr><td><b>App</b></td><td> - </td><td><b>Ability</b></td></tr>"
            byApp += "<tr><td>alextts</td><td> - </td><td>speak</td></tr>"
            byApp += "<tr><td>Cast-Web</td><td> - </td><td>playText, playTextAndRestore, playTextAndResume, speak</td></tr>"
            byApp += "<tr><td>Chromecast Integration</td><td> - </td><td>playTrack, speak</td></tr>"
            byApp += "<tr><td>Echo Speaks</td><td> - </td><td>playAnnouncement, playAnnouncementAll, setVolumeAndSpeak, setVolumeSpeakAndRestore, speak<br><small>* Please see Echo Speaks documentation for usuage.</small></td></tr>"

            byApp += "</table>"
            paragraph "${byApp}"
            paragraph "<hr>"
            paragraph "* If you have any info, please send it to @bptworld on the HE message board."
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
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp) {
        log.info "${app.label} is Paused"
    } else {
        setDefaults()
        subscribe(gvDevice, "latestMessage", startHandler)
        if(triggerMode == "Contact_Sensor") subscribe(myContacts, "contact", contactSensorHandler)
        if(triggerMode == "Motion_Sensor") subscribe(myMotion, "motion", motionSensorHandler)
        if(triggerMode == "Presence_Sensor") subscribe(myPresence, "presence", presenceSensorHandler)
        if(triggerMode == "Switch") subscribe(mySwitches, "switch", switchHandler)
        if(presenceSensor1) subscribe(presenceSensor1, "presence", presenceSensorHandler1)
        if(presenceSensor2) subscribe(presenceSensor2, "presence", presenceSensorHandler2)
        if(presenceSensor3) subscribe(presenceSensor3, "presence", presenceSensorHandler3)
        if(presenceSensor4) subscribe(presenceSensor4, "presence", presenceSensorHandler4)
        if(presenceSensor5) subscribe(presenceSensor5, "presence", presenceSensorHandler5)
        if(gInitRepeat) runIn(gInitRepeat,initializeSpeaker)
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def presenceSensorHandler1(evt){
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.presenceSensorValue1 = evt.value
        if(logEnable) log.debug "In presenceSensorHandler1 (${state.version}) - Presence Sensor: ${state.presenceSensorValue1}"
        if(state.presenceSensorValue1 == "not present"){
            if(logEnable) log.debug "Presence Sensor 1 is not present."
            state.IH1 = "no"
        } else {
            if(logEnable) log.debug "Presence Sensor 1 is present."
            state.IH1 = "yes"
        }
    }
}

def presenceSensorHandler2(evt){
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.presenceSensorValue2 = evt.value
        if(logEnable) log.debug "In presenceSensorHandler2 (${state.version}) - Presence Sensor: ${state.presenceSensorValue2}"
        if(state.presenceSensorValue2 == "not present"){
            if(logEnable) log.debug "Presence Sensor 2 is not present."
            state.IH2 = "no"
        } else {
            if(logEnable) log.debug "Presence Sensor 2 is present."
            state.IH2 = "yes"
        }
    }
}

def presenceSensorHandler3(evt){
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.presenceSensorValue3 = evt.value
        if(logEnable) log.debug "In presenceSensorHandler3 (${state.version}) - Presence Sensor: ${state.presenceSensorValue3}"
        if(state.presenceSensorValue3 == "not present"){
            if(logEnable) log.debug "Presence Sensor 3 is not present."
            state.IH3 = "no"
        } else {
            if(logEnable) log.debug "Presence Sensor 3 is present."
            state.IH3 = "yes"
        }
    }
}

def presenceSensorHandler4(evt){
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.presenceSensorValue4 = evt.value
        if(logEnable) log.debug "In presenceSensorHandler4 (${state.version}) - Presence Sensor: ${state.presenceSensorValue4}"
        if(state.presenceSensorValue4 == "not present"){
            if(logEnable) log.debug "Presence Sensor 4 is not present."
            state.IH4 = "no"
        } else {
            if(logEnable) log.debug "Presence Sensor 4 is present."
            state.IH4 = "yes"
        }
    }
}

def presenceSensorHandler5(evt){
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.presenceSensorValue5 = evt.value
        if(logEnable) log.debug "In presenceSensorHandler5 (${state.version}) - Presence Sensor: ${state.presenceSensorValue5}"
        if(state.presenceSensorValue5 == "not present"){
            if(logEnable) log.debug "Presence Sensor 5 is not present."
            state.IH5 = "no"
        } else {
            if(logEnable) log.debug "Presence Sensor 5 is present."
            state.IH5 = "yes"
        }
    }
}

def alwaysOnHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In alwaysOnHandler (${state.version}) - setting sZone to true"
        atomicState.sZone = true
        speakerStatus = "${app.label}:${atomicState.sZone}"
        gvDevice.sendFollowMeSpeaker(speakerStatus)
    }
}

def contactSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.contactStatus = evt.value
        if(logEnable) log.debug "In contactSensorHandler (${state.version}) - sZone: ${atomicState.sZone} - Status: ${state.contactStatus}"
        if(contactOption == "Closed") {
            if(state.contactStatus == "closed") {
                if(logEnable) log.debug "In contactSensorHandler - setting sZone to true"
                atomicState.sZone = true
                speakerStatus = "${app.label}:${atomicState.sZone}"
                gvDevice.sendFollowMeSpeaker(speakerStatus)
            }
            if(state.contactStatus == "open") {
                sOff = sZoneWaiting * 60
                runIn(sOff,zoneOffHandler)
            }
        }
        if(contactOption == "Open") {
            if(state.contactStatus == "open") {
                if(logEnable) log.debug "In contactSensorHandler (${state.version}) - setting sZone to true"
                atomicState.sZone = true
                speakerStatus = "${app.label}:${atomicState.sZone}"
                gvDevice.sendFollowMeSpeaker(speakerStatus)
            }
            if(state.contactStatus == "closed") {
                sOff = sZoneWaiting * 60
                runIn(sOff,zoneOffHandler)
            }
        }
    }
}

def motionSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.motionStatus = evt.value
        if(logEnable) log.debug "In motionSensorHandler (${state.version}) - sZone: ${atomicState.sZone} - Status: ${state.motionStatus}"
        if(state.motionStatus == "active") {
            if(logEnable) log.debug "In motionSensorHandler - setting sZone to true"
            atomicState.sZone = true
            speakerStatus = "${app.label}:${atomicState.sZone}"
            gvDevice.sendFollowMeSpeaker(speakerStatus)
        }
        if(state.motionStatus == "inactive") {
            sOff = sZoneWaiting * 60
            runIn(sOff,zoneOffHandler)
        }
    }
}

def presenceSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.presenceStatus = evt.value
        if(logEnable) log.debug "In presenceSensorHandler (${state.version}) - sZone: ${atomicState.sZone} - Status: ${state.presenceStatus}"
        if(state.presenceStatus == "present") {
            if(logEnable) log.debug "In presenceSensorHandler - setting sZone to true"
            atomicState.sZone = true
            speakerStatus = "${app.label}:${atomicState.sZone}"
            gvDevice.sendFollowMeSpeaker(speakerStatus)
        }
        if(state.presenceStatus == "not present") {
            sOff = sZoneWaiting * 60
            runIn(sOff,zoneOffHandler)
        }
    }
}

def switchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.switchStatus = evt.value
        if(logEnable) log.debug "In switchHandler (${state.version}) - sZone: ${atomicState.sZone} - Status: ${state.switchStatus}"
        if(state.switchStatus == "on") {
            if(logEnable) log.debug "In switchHandler - setting sZone to true"
            atomicState.sZone = true
            speakerStatus = "${app.label}:${atomicState.sZone}"
            gvDevice.sendFollowMeSpeaker(speakerStatus)
        }
        if(state.switchStatus == "off") {
            sOff = sZoneWaiting * 60
            runIn(sOff,zoneOffHandler)
        }
    }
}

def prioritySpeaker(data) {
    if(logEnable) log.debug "In prioritySpeaker (${state.version})"
    prioritySpeaker = null
    state.priMatch = false
    
    try {
        def thePriority = data.split(":")
        theValueCount = thePriority.size()
        if(logEnable) log.debug "In prioritySpeaker - theValueCount: ${theValueCount}"

        if(theValueCount >= 1) priorityValue = thePriority[0]
        if(theValueCount >= 2) priorityVoice = thePriority[1]
        if(theValueCount >= 3) prioritySpeaker = thePriority[2]

        if(priorityValue == null) priorityValue = "X"
        if(priorityVoice == null || priorityVoice =="0") priorityVoice = "X"
        if(prioritySpeaker == null || prioritySpeaker == "0") prioritySpeaker = "X"
        if(logEnable) log.debug "In prioritySpeaker - priorityValue: ${priorityValue} - priorityVoice: ${priorityVoice} - prioritySpeaker: ${prioritySpeaker}"
    } catch (e) {
        log.warn "In prioritySpeaker - Something went wrong with your speech priority formatting. Please check your syntax. ie. [N:1:0]"
        if(logEnable) log.error "In prioritySpeaker - ${e}"
        priorityValue = "X"
        priorityVoice = "X"
        prioritySpeaker = "X"
    }
    
    def prioritySpeaker1 = parent.pSpeaker1
    def prioritySpeaker2 = parent.pSpeaker2
    def prioritySpeaker3 = parent.pSpeaker3
    def prioritySpeaker4 = parent.pSpeaker4
    def prioritySpeaker5 = parent.pSpeaker5
    def prioritySpeaker6 = parent.pSpeaker6
    def prioritySpeaker7 = parent.pSpeaker7
    def prioritySpeaker8 = parent.pSpeaker8
    def prioritySpeaker9 = parent.pSpeaker9
    def prioritySpeaker10 = parent.pSpeaker10

    state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
    
    if(prioritySpeaker != "X") {
        state.speakers.each { it ->
            String theSpeaker = "${it.displayName}"
            
            if(prioritySpeaker == "1") {
                String ps1 = prioritySpeaker1
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 1: ${sp1}"
                if(theSpeaker == ps1) {
                    if(logEnable) log.debug "In prioritySpeaker 1 - MATCH!"
                    priSpeaker = prioritySpeaker1
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "2") {
                String ps2 = prioritySpeaker2
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 2: ${ps2}"
                if(theSpeaker == ps2) {
                    if(logEnable) log.debug "In prioritySpeaker 2 - MATCH!"
                    priSpeaker = prioritySpeaker2
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "3") {
                String ps3 = prioritySpeaker3
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 3: ${ps3}"
                if(theSpeaker == ps3) {
                    if(logEnable) log.debug "In prioritySpeaker 3 - MATCH!"
                    priSpeaker = prioritySpeaker3
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "4") {
                String ps4 = prioritySpeaker4
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 4: ${ps4}"
                if(theSpeaker == ps4) {
                    if(logEnable) log.debug "In prioritySpeaker 4 - MATCH!"
                    priSpeaker = prioritySpeaker4
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "5") {
                String ps5 = prioritySpeaker5
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 5: ${ps5}"
                if(theSpeaker == ps5) {
                    if(logEnable) log.debug "In prioritySpeaker 5 - MATCH!"
                    priSpeaker = prioritySpeaker5
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "6") {
                String ps6 = prioritySpeaker6
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 6: ${ps6}"
                if(theSpeaker == ps6) {
                    if(logEnable) log.debug "In prioritySpeaker 6 - MATCH!"
                    priSpeaker = prioritySpeaker6
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "7") {
                String ps7 = prioritySpeaker7
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 7: ${ps7}"
                if(theSpeaker == ps7) {
                    if(logEnable) log.debug "In prioritySpeaker 7 - MATCH!"
                    priSpeaker = prioritySpeaker7
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "8") {
                String ps8 = prioritySpeaker8
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 8: ${ps8}"
                if(theSpeaker == ps8) {
                    if(logEnable) log.debug "In prioritySpeaker 8 - MATCH!"
                    priSpeaker = prioritySpeaker8
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "9") {
                String ps9 = prioritySpeaker9
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 9: ${ps9}"
                if(theSpeaker == ps9) {
                    if(logEnable) log.debug "In prioritySpeaker 9 - MATCH!"
                    priSpeaker = prioritySpeaker9
                    state.priMatch = true
                    atomicState.sZone = true
                }
            } else if(prioritySpeaker == "10") {
                String ps10 = prioritySpeaker10
                if(logEnable) log.debug "In prioritySpeaker - Checking for Priority Speaker - theSpeaker: ${theSpeaker} vs 10: ${ps10}"
                if(theSpeaker == ps10) {
                    if(logEnable) log.debug "In prioritySpeaker 10 - MATCH!"
                    priSpeaker = prioritySpeaker10
                    state.priMatch = true
                    atomicState.sZone = true
                }
            }
        }
    } else {
        state.priMatch = true
    }
    if(logEnable) log.debug "In prioritySpeaker - priSpeaker: ${priSpeaker}"
    return priSpeaker
}

def startHandler(evt) { 
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "**********  Follow Me (${state.version}) - Start Talking  **********"

        if(logEnable) log.debug "In startHandler (${state.version})"
        if(messageDest == "Speakers") letsTalkQueue(evt)
        if(messageDest == "Push" || messageDest == "Queue") pushOrQueue(evt)
        
        if(logEnable) log.debug "**********  Follow Me (${state.version}) - End Talking  **********"
    }
}

def zoneOffHandler() {
    if(logEnable) log.debug "In zoneOffHandler (${state.version}) - Checking for status change"
	if(state.contactStatus == "open" || state.motionStatus == "active" || state.switchStatus == "on"){
		atomicState.sZone = true
		if(logEnable) log.debug "In zoneOffHandler - Zone status changed, staying on - sZone: ${atomicState.sZone}"
	} else {
		atomicState.sZone = false
		speakerStatus = "${app.label}:${atomicState.sZone}"
		gvDevice.sendFollowMeSpeaker(speakerStatus)
		if(logEnable) log.debug "In zoneOffHandler - Zone is now off - sZone: ${atomicState.sZone}"
	}
}

def initializeSpeaker() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In initializeSpeaker (${state.version}) - Initializing ${speakerSS}"
        speakerSS.initialize()
        if(gInitRepeat) repeat = gInitRepeat * 60
        if(gInitRepeat) runIn(repeat,initializeSpeaker)
    }
}

// **********  Start code modified from @djgutheinz  **********
def letsTalkQueue(evt) {
    theText = evt.value
    if(useQueue) {
        if(logQueue) log.debug "In letsTalkQueue (${state.version}) - theText: ${theText}"
	    state.TTSQueue << [theText]
	    if(!atomicState.playingTTS) { runInMillis(500, processQueue) }
    } else {
        if(logEnable) log.debug "In letsTalkQueue (${state.version}) - Queue not activated, going to letsTalk"
        letsTalk(theText)
    }
}

def processQueue() {
	if(logQueue) log.debug "In processQueue (${state.version})"
	atomicState.playingTTS = true
    int maxQ = maxQueued
    int queueSize = state.TTSQueue.size()
    if(logQueue) log.info "In processQueue - size: ${queueSize} vs maxQ: ${maxQ}"
    
    if(queueSize >= maxQ) {
        if(logQueue) log.info "In processQueue - queueSize is too much: ${queueSize}, going to clearTheQueue"
        clearTheQueue()
    }
    
	if(queueSize == 0) {
		atomicState.playingTTS = false
        if(logQueue) log.info "In processQueue - size: ${queueSize} - playingTTS: ${atomicState.playingTTS} - Finished Playing"
		return
	}
	def nextTTS = state.TTSQueue[0]
    if(logQueue) log.info "In processQueue - size: ${queueSize} - playingTTS: ${atomicState.playingTTS} - Playing Next: ${nextTTS}"
    state.TTSQueue.remove(0)
	letsTalk(nextTTS)
    runIn(1,processQueue)
}
// **********  End code modified from @djgutheinz  **********

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk - msg: ${msg}"
    def message =  new JsonSlurper().parseText(msg)
    if(message.message.contains("]")) {
        oldMes = message.message
        splitMes = oldMes.split("]")
        newMessage = splitMes[1]
    } else {
        newMessage = message.message
    }
    
    state.speakers = null

    prioritySpeaker(message.priority)
    
    if(priSpeaker) {
        state.speakers = priSpeaker
        if(logEnable) log.debug "In letsTalk - priSpeaker - speakers: ${state.speakers}"
    } else {
        state.speakers = state.speakers
        if(logEnable) log.debug "In letsTalk - state.speakers - speakers: ${state.speakers}"
    }
                   
	if(triggerMode == "Always_On") alwaysOnHandler()
	if(atomicState.sZone && state.priMatch){
		checkTime()            
        checkPriority(priorityValue)
        checkVol()
		if(logEnable) log.debug "In letsTalk - continuing"
		if(state.timeBetween) {
			state.sStatus = "speaking"
			speakerStatus = "${app.label}:${state.sStatus}"
			gvDevice.sendFollowMeSpeaker(speakerStatus)
            
            try {
                duration = textToSpeech(theText).duration + 3
            } catch (e) {
		        duration = 10
			}
            theDuration = duration * 1000
            
            if(logEnable) log.debug "In letsTalk - **** Last check **** - speakers: ${state.speakers}"
            state.speakers.each { it ->
                if(logEnable) log.debug "In letsTalk - Sending to priorityVoicesHandler - speaker: ${it} - priorityVoice: ${priorityVoice} - newMessage: ${newMessage}" 
                priorityVoicesHandler(it,priorityVoice,newMessage)
                
                if(!defaultSpeak) {    
                    switch(message.method) {        // Code modified from @storageanarchy
                        case 'deviceNotification':
                            beforeVolume(it)
                            it.speak(newMessage)
                            pauseExecution(theDuration)
                            afterVolume(it)
                            if(logEnable) log.debug "In letsTalk - deviceNotification Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'playAnnouncement':
                            it.playAnnouncement(newMessage, message.priority, message.speakLevel, message.returnLevel, message.title)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - playAnnouncement Received - speaker: ${it} - ${newMessagee}"
                            break;
                        case 'playAnnouncementAll':
                            it.playAnnouncementAll(newMessagee, message.priority, message.speakLevel, message.returnLevel, message.title)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - playAnnouncementAll Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'playText':
                            beforeVolume(it)
                            it.playText(newMessage)
                            pauseExecution(theDuration)
                            afterVolume(it)
                            if(logEnable) log.debug "In letsTalk - playText Received - speaker: ${it} - $newMessage}"
                            break;
                        case 'playTextAndRestore':
                            beforeVolume(it)
                            it.playTextAndRestore(newMessage, message.returnLevel)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - playTextAndRestore Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'playTextAndResume':
                            beforeVolume(it)
                            it.playTextAndResume(newMessage, message.returnLevel)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - playTextAndResume Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'playTrack':
                            beforeVolume(it)
                            if(state.sound) {
                                it.playTrack(state.sound)
                                soundDur = state.sLength * 1000
                                pauseExecution(soundDur)
                            }
                            pauseExecution(500)
                            it.playTrack(state.uriMessage)
                            pauseExecution(theDuration)
                            afterVolume(it)
                            if(logEnable) log.debug "In letsTalk - playTrack Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'playTrackAndRestore':
                            beforeVolume(it)
                            if(state.sound) {
                                it.playTrack(state.sound)
                                soundDur = state.sLength * 1000
                                pauseExecution(soundDur)
                            }
                            pauseExecution(500)
                            it.playTrackAndRestore(state.uriMessage, message.returnLevel)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - playTrackAndRestore Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'setVolume':
                            it.setVolume(message.speakLevel)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - setVolume Received - speaker: ${it} - ${message.speakLevel}"
                            break;
                        case 'setVolumeSpeakAndRestore':
                            it.setVolumeSpeakAndRestore(newMessage, message.priority, message.speakLevel, message.returnLevel)
                            pauseExecution(theDuration)
                            if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'setVolumeAndSpeak':
                            it.setVolumeAndSpeak(newMessage, message.priority, message.speakLevel)
                            pauseExecution(theDuration)
                                afterVolume(it)
                            if(logEnable) log.debug "In letsTalk - setVolumeAndSpeak Received - speaker: ${it} - ${newMessage}"
                            break;
                        case 'speak':
                            if(logEnable) log.debug "In letsTalk - speak - speaker: ${it} - Using best case handler"
                            if(it.hasCommand('setVolumeSpeakAndRestore')) {
                                if(logEnable) log.debug "In letsTalk - (speak) setVolumeSpeakAndRestore - ${it} - message: ${newMessage}"
                                def prevVolume = it.currentValue("volume")
                                it.setVolumeSpeakAndRestore(state.volume, newMessage, prevVolume)
                                pauseExecution(theDuration)
                            } else if(it.hasCommand('playTextAndRestore')) {   
                                if(logEnable) log.debug "In letsTalk - (speak) playTextAndRestore - ${it} - message: ${newMessage}"
                                def prevVolume = it.currentValue("volume")
                                beforeVolume(it)
                                it.playTextAndRestore(newMessage, prevVolume)
                                pauseExecution(theDuration)
                            } else if(it.hasCommand('playTrack')) {
                                if(logEnable) log.debug "In letsTalk - (speak) playTrack Received - speaker: ${it} - ${newMessage}"
                                beforeVolume(it)
                                if(state.sound) {
                                    try {
                                        it.playTrack(state.sound)
                                        soundDur = state.sLength * 1000
                                        pauseExecution(soundDur)
                                    } catch(e) {
                                        // do nothing
                                    }
                                }
                                pauseExecution(500)
                                try {
                                    it.playTrack(state.uriMessage)
                                    pauseExecution(theDuration)
                                    afterVolume(it) 
                                } catch(e) {
                                    // do nothing 
                                }                              
                            } else {		        
                                if(logEnable) log.debug "In letsTalk - (speak) - ${it} - message: ${newMessage}"
                                beforeVolume(it)
                                try {
                                    it.speak(newMessage)
                                } catch(e) {
                                    // do nothing 
                                }     
                                pauseExecution(theDuration)
                                afterVolume(it)
                            }
                        break; 
                    }
                } else {
                    if(logEnable) log.debug "In letsTalk - (Default speak) - ${it} - message: ${newMessage}"
                    try {
                        it.speak(newMessage)
                    } catch(e) {
                        // do nothing 
                    }     
                    pauseExecution(theDuration)
                }
            }
            speakerStatus = "${app.label}:${atomicState.sZone}"
			gvDevice.sendFollowMeSpeaker(speakerStatus)
			if(logEnable) log.info "In letsTalk - Ready for next message"
            
        } else {
		    if(logEnable) log.debug "In letsTalk (${state.version}) - Messages not allowed at this time"
	    }
	} else {
		if(logEnable) log.debug "In letsTalk (${state.version}) - Zone is off"
	}
    if(logEnable) log.info "--------------------------------------------------------------------------------------"
}

def beforeVolume(it) {
    if(logEnable) log.debug "In beforeVolume (${state.version}) - it: ${it}"
    try {
        state.prevVolume = it.currentValue("volume")
        if(it.hasCommand('setVolume')) {
            if(state.volume == null) state.volume = state.prevVolume
            it.setVolume(state.volume)           
            if(logEnable) log.debug "In beforeVolume - Setting volume to ${state.volume}"
        } else {
            if(logEnable) log.debug "In beforeVolume - Volume was not changed"
        }
    } catch(e) {
        // do nothing
    }   
}
    
def afterVolume(it) {
    if(logEnable) log.debug "In afterVolume (${state.version}) - it: ${it}"
    try {
        if(it.hasCommand('setVolume')) {
            it.setVolume(state.prevVolume) 
            if(logEnable) log.debug "In afterVolume - Setting volume to ${state.prevVolume}"
        } else {
            if(logEnable) log.debug "In afterVolume - Volume was not changed"
        }
    } catch(e) {
        // do nothing
    }
        
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if(fromTime) {
        if(midnightCheckR) {
            state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime)+1, new Date(), location.timeZone)
        } else {
		    state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
        }
		if(state.betweenTime) {
            if(logEnable) log.debug "In checkTime - Time within range - Don't Speak"
			state.timeBetween = true
		} else {
            if(logEnable) log.debug "In checkTime - Time outside of range - Can Speak"
			state.timeBetween = false
		}
  	} else {  
        if(logEnable) log.debug "In checkTime - NO Time Restriction Specified"
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"    
    if(days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        def dayCheck = days.contains(day)

        if(dayCheck) {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Passed"
            state.daysMatch = true
        } else {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Check Failed"
            state.daysMatch = false
        }
    } else {
        state.daysMatch = true
    }
    if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch}"
}

def checkVol() {
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
        if(midnightCheckQ) {
            state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime)+1, new Date(), location.timeZone)
        } else {
		    state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
        }
    	if(state.quietTime) {
            if(logEnable) log.debug "In checkVol - Time within range - Using Quiet Time - setting volume to ${volQuiet}"
    		state.volume = volQuiet
		} else {
            if(logEnable) log.debug "In checkVol - Time outside of range - Not using Quiet Time - setting volume to ${volSpeech}"
			state.volume = volSpeech
		}
	} else {
        if(logEnable) log.debug "In checkVol - NO Quite Time Specified - setting volume to ${volSpeech}"
		state.volume = volSpeech
	}
}
    
def checkPriority(priorityValue) {
    if(logEnable) log.debug "In checkPriority (${state.version})"
    
    if(priorityValue == "X") {
        if(logEnable) log.debug "In checkPriority - priorityValue: ${priorityValue}, so skipping"
        state.volume = volSpeech
		state.voiceSelected = voiceNorm 
    } else {
		if(priorityValue.toUpperCase().contains("F")) {
            state.volume = volSpeech
		    state.voiceSelected = voiceFun
        } else if(priorityValue.toUpperCase().contains("R")) {
		    randomHandler()
            state.volume = volSpeech
			state.voiceSelected = state.randVoice
        } else if(priorityValue.toUpperCase().contains("L")) {
            state.volume = volLow
		    state.voiceSelected = voiceLow
        } else if(priorityValue.toUpperCase().contains("N")) {
		    state.volume = volSpeech
		    state.voiceSelected = voiceNorm
        } else if(priorityValue.toUpperCase().contains("H")) {
		    state.volume = volHigh
		    state.voiceSelected = voiceHigh
        } else {
            state.volume = volSpeech
		    state.voiceSelected = voiceNorm
            if(logEnable) log.debug "In checkPriority - Priority Not found, moving on"
        }
        if(logEnable) log.debug "In checkPriority - priorityValue: ${priorityValue} - Priority volume: ${state.volume}"    
    }
}

def priorityVoicesHandler(speaker,priorityVoice,lastSpoken) {
    if(logEnable) log.debug "In priorityVoicesHandler - Received - speaker: ${speaker} - priorityVoice: ${priorityVoice} - lastSpoken: ${lastSpoken}"
    if(lastSpoken == ".") lastSpoken = ""
    state.sound = ""
    if(priorityVoice == "0") {
        if(logEnable) log.debug "In priorityVoicesHandler (${state.version}) - priorityVoice: ${priorityVoice}, so skipping"
		state.voiceSelected = voiceNorm
        def tts = textToSpeech(lastSpoken,state.voiceSelected)
	    def uriMessage = "${tts.get('uri')}"
        state.uriMessage = uriMessage
        if(logEnable) log.debug "In priorityVoicesHandler - New uri: ${uriMessage}"
    } else {
	    def tts = textToSpeech(lastSpoken,state.voiceSelected)
	    def uriMessage = "${tts.get('uri')}"
        try {
            if(speaker.hasCommand('playTrack')) {
                if(priorityVoice.contains("X")) {
                    state.sound = ""
                    state.sLength = parent.s1Length
                } else if(priorityVoice.contains("1")) {
                    if(parent.sound1) {
                        state.sound = parent.sound1
                        state.sLength = parent.s1Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 1 not defined"
                    }
                } else if(priorityVoice.contains("2")) {
                    if(parent.sound2) {
                        state.sound = parent.sound2
                        state.sLength = parent.s2Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 2 not defined"
                    }
                } else if(priorityVoice.contains("3")) {
                    if(parent.sound3) {
                        state.sound = parent.sound3
                        state.sLength = parent.s3Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 3 not defined"
                    }
                } else if(priorityVoice.contains("4")) {
                    if(parent.sound4) {
                        state.sound = parent.sound4
                        state.sLength = parent.s4Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 4 not defined"
                    }
                } else if(priorityVoice.contains("5")) {
                    if(parent.sound5) {
                        state.sound = parent.sound5
                        state.sLength = parent.s5Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 5 not defined"
                    }
                } else if(priorityVoice.contains("6")) {
                    if(parent.sound6) {
                        state.sound = parent.sound6
                        state.sLength = parent.s6Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 6 not defined"
                    }
                } else if(priorityVoice.contains("7")) {
                    if(parent.sound7) {
                        state.sound = parent.sound7
                        state.sLength = parent.s7Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 7 not defined"
                    }
                } else if(priorityVoice.contains("8")) {
                    if(parent.sound8) {
                        state.sound = parent.sound8
                        state.sLength = parent.s8Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 8 not defined"
                    }
                } else if(priorityVoice.contains("9")) {
                    if(parent.sound9) {
                        state.sound = parent.sound9
                        state.sLength = parent.s9Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 9 not defined"
                    }
                } else if(priorityVoice.contains("10")) {
                    if(parent.sound10) {
                        state.sound = parent.sound10
                        state.sLength = parent.s10Length
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 10 not defined"
                    }
                }
            } else { 
                if(logEnable) log.debug "Follow Me - ${speaker} doesn't support playTrack"
            }
        } catch (e) {
            //log.warn "Follow Me - priorityVoicesHandler - Something went wrong!"
            state.sound = ""
            state.sLength = 1
        }
        state.uriMessage = uriMessage
    }
    if(logEnable) log.debug "In priorityVoicesHandler - Speaker: ${speaker} - priorityVoice: ${priorityVoice} - Voice: ${state.voiceSelected} - Message: ${lastSpoken} - uriMessage: ${state.uriMessage}"
}

def pushOrQueue(evt) {
    msg = evt.value
	if(logEnable) log.debug "In pushOrQueue (${state.version}) - ${msg}"
    def message =  new JsonSlurper().parseText(msg)    
    theMessage = message.message
    
    if(theMessage.contains("]")) {
        def (p, pushMsg) = theMessage.split("]")
    } else {
        pushMsg = theMessage
    }
       
    try {
        def thePriority = message.priority.split(":")
        priorityValue = thePriority[0]
        priorityVoice = thePriority[1]
    } catch (e) {
        log.warn "Follow Me - Something went wrong with your speech priority formatting. Please check your syntax. ie. [N:1]"
        if(logEnable) log.error "In letsTalk - ${e}"
        priorityValue = "X"
        priorityVoice = "X"
    }
    
    if(priorityValue.toUpperCase().contains("L")) {
		pushMsg = "[L]" + pushMsg
	}
	if(priorityValue.toUpperCase().contains("N")) {
		pushMsg = "[N]" + pushMsg
	}
	if(priorityValue.toUpperCase().contains("H")) {
		pushMsg = "[H]" + pushMsg
	}
    
	if(state.IH1 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH1 Sending message: ${pushMsg}"
            if(sendPushMessage1) sendPushMessage1.deviceNotification(pushMsg)
        }
        if(messageDest == "Queue") {
            ps = "IH1"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH2 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH2 Sending message: ${pushMsg}"
    	    if(sendPushMessage2) sendPushMessage2.deviceNotification(pushMsg)
        }
        if(messageDest == "Queue") {
            ps = "IH2"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH3 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH3 Sending message: ${pushMsg}"
    	    if(sendPushMessage3) sendPushMessage3.deviceNotification(pushMsg)
        }
        if(messageDest == "Queue") {
            ps = "IH3"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH4 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH4 Sending message: ${pushMsg}"
    	    if(sendPushMessage4) sendPushMessage4.deviceNotification(pushMsg)
        }
        if(messageDest == "Queue") {
            ps = "IH4"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH5 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH5 Sending message: ${pushMsg}"
    	    if(sendPushMessage5) sendPushMessage5.deviceNotification(pushMsg)
        }
        if(messageDest == "Queue") {
            ps = "IH5"
            letsQueue(ps,pushMsg)
        }
	}
}

def letsQueue(ps,theMessage) {
    if(logEnable) log.debug "In letsQueue (${state.version})"
    // Work in progress
}

def getVoices(){						// Modified from @mike.maxwell
	if(logEnable) log.debug "In getVoices (${state.version})"
	def voices = getTTSVoices()
	voices.sort{ a, b ->
		a.language <=> b.language ?: a.gender <=> b.gender ?: a.gender <=> b.gender  
	}    
    state.list = voices.collect{ ["${it.name}": "${it.language}:${it.gender}:${it.name}"] }
}

def randomHandler() {
	if(logEnable) log.debug "In randomHandler (${state.version}) - voiceRandom: ${voiceRandom}"
    if(voiceRandom) {
	    vSize = voiceRandom.size()
	    count = vSize.toInteger()
        def randomKey = new Random().nextInt(count)
	    state.randVoice = voiceRandom[randomKey]
    } else {
        log.warn "Follow Me (${state.version}) - No random voices selected."
    }
	if(logEnable) log.debug "In randomHandler - Random - vSize: ${vSize}, randomVoice: ${state.randVoice}"
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    
    if(state.whichButton == "testVoiceFun"){
        if(logEnable) log.debug "In testButtonHandler - Testing Voice Fun on Speaker: ${testTheSpeakers}"
        def tts = textToSpeech(testPhrase,voiceFun)
	    def uriMessage = "${tts.get('uri')}"
        try {
            testTheSpeakers.playTrack(uriMessage)
        } catch(ef) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack." }    
    }
    if(state.whichButton == "testVoiceRandom"){
        if(logEnable) log.debug "In testButtonHandler - Testing Voice Random on Speaker: ${testTheSpeakers}"
        randomHandler()
        def tts = textToSpeech(testPhrase,state.randVoice)
	    def uriMessage = "${tts.get('uri')}"
        try {
            testTheSpeakers.playTrack(uriMessage)
        } catch(er) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack." }    
    }
    if(state.whichButton == "testVoiceLow"){
        if(logEnable) log.debug "In testButtonHandler - Testing Voice Low on Speaker: ${testTheSpeakers}"
        def tts = textToSpeech(testPhrase,voiceLow)
	    def uriMessage = "${tts.get('uri')}"
        try {
            testTheSpeakers.playTrack(uriMessage)
        } catch(el) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack." }    
    }
    if(state.whichButton == "testVoiceNorm"){
        if(logEnable) log.debug "In testButtonHandler - Testing Voice Norm on Speaker: ${testTheSpeakers}"
        def tts = textToSpeech(testPhrase,voiceNorm)
	    def uriMessage = "${tts.get('uri')}"
        try {
            testTheSpeakers.playTrack(uriMessage)
        } catch(en) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack." }    
    }
    if(state.whichButton == "testVoiceHigh"){
        if(logEnable) log.debug "In testButtonHandler - Testing Voice High on Speaker: ${testTheSpeakers}"
        def tts = textToSpeech(testPhrase,voiceHigh)
	    def uriMessage = "${tts.get('uri')}"
        try {
            testTheSpeakers.playTrack(uriMessage)
        } catch(eh) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack." }    
    }
}

def clearTheQueue() {
    app?.updateSetting("clearQueue",[value:"false",type:"bool"])
    if(logQueue) log.debug "In clearTheQueue (${state.version}) - Resetting the Queue"
    state.TTSQueue = []
	atomicState.playingTTS = false
}

def showTheQueue() {
    app?.updateSetting("showQueue",[value:"false",type:"bool"])
    if(logQueue) log.debug "In showTheQueue (${state.version})"	
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Follow Me Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Follow Me unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
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
	if(logEnable) log.debug "In setDefaults..."
	if(messagePriority == null) {messagePriority = false}
    atomicState.playingTTS = false
	state.TTSQueue = []
	if(atomicState.sZone == null) {atomicState.sZone = false}
	if(state.IH1 == null) {state.IH1 = "blank"}
	if(state.IH2 == null) {state.IH2 = "blank"}
	if(state.IH3 == null) {state.IH3 = "blank"}
	if(state.IH4 == null) {state.IH4 = "blank"}
	if(state.IH5 == null) {state.IH5 = "blank"}
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

def display(data) {
    if(data == null) data = ""
    setVersion()
    getHeaderAndFooter()
    if(app.label) {
        if(app.label.contains("(Paused)")) {
            theName = app.label - " <span style='color:red'>(Paused)</span>"
        } else {
            theName = app.label
        }
    }
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
        paragraph getFormat("line")
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
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
}
