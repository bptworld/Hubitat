/**
 *  ****************  Follow Me App  ****************
 *  Design Usage:
 *  Never miss a message again. Send messages to your occupied room speakers when home or by push when away. Automatically!
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
 *  2.2.1 - 06/11/20 - Added more debug logging
 *  2.2.0 - 05/31/20 - Chasing a bug in push handler
 *  2.1.9 - 05/31/20 - Adjustments to zone off handler and a few other little bits
 *  2.1.8 - 05/30/20 - Virtual Device can now be automatically created - Recommended to delete device and recreate
 *  2.1.7 - 05/30/20 - Fixed a typo with contact sensors
 *  2.1.6 - 05/29/20 - Adjustments to push handler
 *  2.1.5 - 05/11/20 - Added a default speak option
 *  2.1.4 - 04/27/20 - Cosmetic changes
 *  2.1.3 - 04/15/20 - Adjustments to speaker queue
 *  2.1.2 - 04/01/20 - Fixed priority volume
 *  2.1.1 - 12/02/19 - Speech queue is now optional
 *  2.1.0 - 11/13/19 - Major rewrite - More possibilities!
 *  ---
 *  1.0.0 - 03/17/19 - Initial release.
 *
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat
    
def setVersion(){
    state.name = "Follow Me"
	state.version = "2.2.1"   
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
    page name: "soundOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "testSpeaker", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "speakerStatus", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "", nextPage: null, install: true, uninstall: true) {
		display()
		getVoices()
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- Create a new child app for each room that has a speaker in it.<br>- Push child app can have up to 5 sensors defined.<br>- If more than 5 sensors are needed, simply add another child device."
			paragraph "<b>Priority Messages</b>"
			paragraph "- Each message sent to 'Follow Me' can have a priority assigned to it.<br>- Volume levels can then be adjusted by priority level.<br>- ie. (l)Dogs are hungry;(m)Door has been open too long;(h)Heat is on and window is open"
			paragraph "<b>Requirements:</b>"
			paragraph "- Virtual Device using our custom 'Follow Me' driver"
            paragraph "<hr>"
            paragraph "NOTE: Not all speakers can use volume controls, play sounds and/or restore to what it was doing before the speech event. Please use the report below to see some known speaker abilities."
            href "speakerStatus", title: "Known Speaker Abilities", description: "Click to see report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Initial Setup")) {
            label title: "Enter a name for this child app", required: false
    		//input "messageDest", "enum", title: "Select message destination", submitOnChange: true, options: ["Speakers","Push","Queue"], required: true
            input "messageDest", "enum", title: "Select message destination", submitOnChange: true, options: ["Speakers","Push"], required: true
		}
		// Speakers
		if(messageDest == "Speakers") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Activation Type for Room Speakers")) {
    			input "triggerMode", "enum", title: "Select message activation Type", submitOnChange: true, options: ["Always_On","Contact_Sensor","Motion_Sensor","Switch"], required: true, Multiple: false
				if(triggerMode == "Always_On"){
					paragraph "Selected speakers will always play messages."	
				}
				if(triggerMode == "Contact_Sensor"){
					input "myContacts", "capability.contactSensor", title: "Select the contact sensor(s) to activate the speaker", required: true, multiple: true
					input "contactOption", "enum", title: "Select contact option - If (option), Speaker is On", options: ["Open","Closed"], required: true
					input "sZoneWaiting", "number", title: "After contact changes, wait X minutes to turn the speaker off", required: true, defaultValue: 5
				}
				if(triggerMode == "Motion_Sensor"){
					input "myMotion", "capability.motionSensor", title: "Select the motion sensor(s) to activate the speaker", required: true, multiple: true
					input "sZoneWaiting", "number", title: "After motion stops, wait X minutes to turn the speaker off", required: true, defaultValue: 5
				}
				if(triggerMode == "Switch"){
					input "mySwitches", "capability.switch", title: "Select Switch(es) to activate the speaker", required: true, multiple: true
					input "sZoneWaiting", "number", title: "After Switch is off, wait X minutes to turn the speaker off", required: true, defaultValue: 5
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
                    input "gInitialize", "bool", title: "When using Google/Nest devices sometimes an Initialize is necessary (not always). Initialize Google/Nest devices before sending speech?", required: true, defaultValue: false
                    input "gInitRepeat", "number", title: "Initialize Google/Nest devices every X minutes? (recommended: 4)", required: false
                }
                if(speakerType == "sonosSpeaker") {
                    paragraph "<b>Speaker type is a Sonos Device. Sonos devices can play custom sounds and change voices.</b>"
                }
                if(speakerType == "otherSpeaker") {
                    paragraph "<b>Speaker type is an Other Device.</b>"
                }
                paragraph "<b>Note:</b> Some speakers just don't play nicely with Follow Me. If your speaker is having an issue, please turn this switch on."
                input "defaultSpeak", "bool", title: "Use default 'speak'", defaultValue:false, submitOnChange:true
                paragraph "<hr>"
                paragraph "<b>If the command sent doesn't have the ability to set the volume, this app will try to do it. It will also return the volume to the previous state after the speech.</b>"
                input "volSpeech", "number", title: "Speaker volume for speech (if not automatic)", description: "0-100", required:true
          		paragraph "<hr>"
                paragraph "<b>Quiet Time Options</b>"
                input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required:false, submitOnChange:true
			    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true, width: 6
    		    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true, width: 6
		        paragraph "<hr>"
                paragraph "<b>Speech Restriction Options</b>"
                paragraph "Speech can also be restricted to within a certain time frame. (Optional)"
        		input "fromTime", "time", title: "From", required: false, width: 6
        		input "toTime", "time", title: "To", required: false, width: 6
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
				input "messagePriority", "bool", defaultValue:false, title: "Use Message Priority features?", description: "Message Priority", submitOnChange:true
				if((messagePriority) && (speakerSS) && (speakerType != "echoSpeaksSpeaker")) input "priorityVoices", "bool", defaultValue:false, title: "Use different voices for each Priority level?", description: "Priority Voices", submitOnChange:true
				if((messagePriority) && (speakerSS) && (speakerType != "echoSpeaksSpeaker")) input "messageSounds", "bool", defaultValue:false, title: "Play a sound before message?", description: "Message Sounds", submitOnChange:true
			}
			if(messagePriority) {
				section("Instructions for Message Priority:", hideable: true, hidden: true) {
					paragraph "<b>Notes:</b>"
					paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place the option bracket in front of any message to be spoken and the volume and/or voice will be adjusted accordingly."
                    paragraph "Format: [priority:sound]"
                    paragraph "<b>[F:0]</b> - Fun<br><b>[R:0]</b> - Random<br><b>[L:0]</b> - Low<br><b>[N:0]</b> - Normal<br><b>[H:0]</b> - High"
					paragraph "You can also specify a sound file to be played before a message!<br><br><b>[1] - [5]</b> - Specify a files URL"
					paragraph "<b>ie.</b> [L:0]Amy is home or [N:3]Window has been open too long or [H:0]Heat is on and window is open"
					paragraph "If you JUST want a sound file played with NO speech after, use [L:1]. or [N:3]. etc. Notice the DOT after the [], that is the message and will not be spoken."
					paragraph "Also notice there is no spaces between the option and the message."
				}
				section() {
					paragraph "Normal priority will use the standard volume set in the 'Speaker Options' Section"
					input "volLow", "number", title: "Speaker volume for Low priority", description: "0-100", required:true, width:4
                    paragraph "Speaker volume for Normal priority<br>${volSpeech}", width:4
					input "volHigh", "number", title: "Speaker volume for High priority", description: "0-100", required:true, width:4
				}
				if(speakerSS && (speakerType != "echoSpeaksSpeaker")) {
                    if(priorityVoices) {
                        section(getFormat("header-green", "${getImage("Blank")}"+" Voice Options")) {
                            href "voiceOptions", title:"Voice Options Setup", description:"Click here to setup the voices"
                        }
					}
				    if(messageSounds) {
					    section(getFormat("header-green", "${getImage("Blank")}"+" Sound Options")) {
					    	href "soundOptions", title:"Sound Options Setup", description:"Click here to setup the sound files"
					    }
                    }
				} else {
					section() {
						paragraph "* Priority Voice and Sound options are only available when using Speech Synth option."
					}
				}
			}
		}
        
		// both Speakers and Push
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Speech Device")) {
            paragraph "This app requires a 'virtual device' to 'catch' the speech and send it here. All child apps will share this device."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have FM create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Follow Me')", required:true, submitOnChange:true
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
		// both Speakers and Push
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "logEnable", "bool", defaultValue: "false", title: "Enable Debug Logging", description: "Enable extra logging for debugging."
            paragraph "<hr>"
            paragraph "Follow Me can use a custom speech queue. If you would like to try this experimental queueing system, turn this switch on."
            input "useQueue", "bool", defaultValue:false, title: "Use speech queueing", description: "speech queue", submitOnChange:true
            
            if(useQueue) {
                paragraph "Follow Me uses a custom speech queue. Sometimes it gets 'stuck' and queues all the messages. To recover from this, please use the options below."
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
        }
	}
}	

def soundOptions(){
    dynamicPage(name: "soundOptions", title: "Sound Option Setup", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Sound Options")) {
			paragraph "Link to any sound file you want.  ie. http://192.168.7.89:820/fastpops1.mp3"
            paragraph "<small>Remember you can always try the URL in a browser, to be sure it is valid.</small>"
            input "testTheSpeakers", "capability.speechSynthesis", title: "Choose speaker for testing", required: true, submitOnChange: true
			input "sound1", "text", title: "Sound - 1", required: false, submitOnChange: true
            if(sound1 && testTheSpeakers) input "s1Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound1 && testTheSpeakers && s1Length) input "testBtn1", "button", title: "Test Sound 1", width: 3
            
			input "sound2", "text", title: "Sound - 2", required: false, width: 9, submitOnChange: true
            if(sound2 && testTheSpeakers) input "s2Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound2 && testTheSpeakers && s2Length) input "testBtn2", "button", title: "Test Sound 2", width: 3
            
			input "sound3", "text", title: "Sound - 3", required: false, width: 9, submitOnChange: true
            if(sound3 && testTheSpeakers) input "s3Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound3 && testTheSpeakers && s3Length) input "testBtn3", "button", title: "Test Sound 3", width: 3
            
			input "sound4", "text", title: "Sound - 4", required: false, width: 9, submitOnChange: true
            if(sound4 && testTheSpeakers) input "s4Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound4 && testTheSpeakers && s4Length) input "testBtn4", "button", title: "Test Sound 4", width: 3
            
			input "sound5", "text", title: "Sound - 5", required: false, width: 9, submitOnChange: true
            if(sound5 && testTheSpeakers) input "s5Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound5 && testTheSpeakers && s5Length) input "testBtn5", "button", title: "Test Sound 5", width: 3
            
            input "sound6", "text", title: "Sound - 6", required: false, width: 9, submitOnChange: true
            if(sound6 && testTheSpeakers) input "s6Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound6 && testTheSpeakers && s6Length) input "testBtn6", "button", title: "Test Sound 6", width: 3
            
            input "sound7", "text", title: "Sound - 7", required: false, width: 9, submitOnChange: true
            if(sound7 && testTheSpeakers) input "s7Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound7 && testTheSpeakers && s7Length) input "testBtn7", "button", title: "Test Sound 7", width: 3
            
            input "sound8", "text", title: "Sound - 8", required: false, width: 9, submitOnChange: true
            if(sound8 && testTheSpeakers) input "s8Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound8 && testTheSpeakers && s8Length) input "testBtn8", "button", title: "Test Sound 8", width: 3
            
            input "sound9", "text", title: "Sound - 9", required: false, width: 9, submitOnChange: true
            if(sound9 && testTheSpeakers) input "s9Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound9 && testTheSpeakers && s9Length) input "testBtn9", "button", title: "Test Sound 9", width: 3
            
            input "sound0", "text", title: "Sound - 0", required: false, width: 9, submitOnChange: true
            if(sound0 && testTheSpeakers) input "s0Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound0 && testTheSpeakers && s0Length) input "testBtn0", "button", title: "Test Sound 0", width: 3
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
	initialize()
}

def initialize() {
    setDefaults()
	subscribe(gvDevice, "latestMessage", startHandler)
	if(triggerMode == "Contact_Sensor") subscribe(myContacts, "contact", contactSensorHandler)
	if(triggerMode == "Motion_Sensor") subscribe(myMotion, "motion", motionSensorHandler)
	if(triggerMode == "Switch") subscribe(mySwitches, "switch", switchHandler)
	if(presenceSensor1) subscribe(presenceSensor1, "presence", presenceSensorHandler1)
	if(presenceSensor2) subscribe(presenceSensor2, "presence", presenceSensorHandler2)
	if(presenceSensor3) subscribe(presenceSensor3, "presence", presenceSensorHandler3)
	if(presenceSensor4) subscribe(presenceSensor4, "presence", presenceSensorHandler4)
	if(presenceSensor5) subscribe(presenceSensor5, "presence", presenceSensorHandler5)
	if(gInitRepeat) runIn(gInitRepeat,initializeSpeaker)
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def presenceSensorHandler1(evt){
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

def presenceSensorHandler2(evt){
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

def presenceSensorHandler3(evt){
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

def presenceSensorHandler4(evt){
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

def presenceSensorHandler5(evt){
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

def alwaysOnHandler() {
	if(logEnable) log.debug "In alwaysOnHandler (${state.version}) - setting sZone to true"
	state.sZone = true
	speakerStatus = "${app.label}:${state.sZone}"
	gvDevice.sendFollowMeSpeaker(speakerStatus)
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	if(logEnable) log.debug "In contactSensorHandler (${state.version}) - sZone: ${state.sZone} - Status: ${state.contactStatus}"
	if(contactOption == "Closed") {
		if(state.contactStatus == "closed") {
			if(logEnable) log.debug "In contactSensorHandler - setting sZone to true"
			state.sZone = true
			speakerStatus = "${app.label}:${state.sZone}"
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
			state.sZone = true
			speakerStatus = "${app.label}:${state.sZone}"
			gvDevice.sendFollowMeSpeaker(speakerStatus)
		}
		if(state.contactStatus == "closed") {
			sOff = sZoneWaiting * 60
			runIn(sOff,zoneOffHandler)
		}
	}
}

def motionSensorHandler(evt) {
	state.motionStatus = evt.value
	if(logEnable) log.debug "In motionSensorHandler (${state.version}) - sZone: ${state.sZone} - Status: ${state.motionStatus}"
	if(state.motionStatus == "active") {
		if(logEnable) log.debug "In motionSensorHandler - setting sZone to true"
		state.sZone = true
		speakerStatus = "${app.label}:${state.sZone}"
		gvDevice.sendFollowMeSpeaker(speakerStatus)
	}
	if(state.motionStatus == "inactive") {
		sOff = sZoneWaiting * 60
		runIn(sOff,zoneOffHandler)
	}
}

def switchHandler(evt) {
	state.switchStatus = evt.value
	if(logEnable) log.debug "In switchHandler (${state.version}) - sZone: ${state.sZone} - Status: ${state.switchStatus}"
	if(state.switchStatus == "on") {
		if(logEnable) log.debug "In switchHandler - setting sZone to true"
		state.sZone = true
		speakerStatus = "${app.label}:${state.sZone}"
		gvDevice.sendFollowMeSpeaker(speakerStatus)
	}
	if(state.switchStatus == "off") {
		sOff = sZoneWaiting * 60
		runIn(sOff,zoneOffHandler)
	}
}

def startHandler(evt) { 
	if(logEnable) log.debug "**********  Follow Me (${state.version}) - Start Talking  **********"
    
    if(logEnable) log.debug "In startHandler (${state.version})"
    if(messageDest == "Speakers") letsTalkQueue(evt)
	if(messageDest == "Push" || messageDest == "Queue") pushOrQueue(evt)
}

def zoneOffHandler() {
    if(logEnable) log.debug "In zoneOffHandler (${state.version}) - Checking for status change"
	if(state.contactStatus == "open" || state.motionStatus == "active" || state.switchStatus == "on"){
		state.sZone = true
		if(logEnable) log.debug "In zoneOffHandler - Zone status changed, staying on - sZone: ${state.sZone}"
	} else {
		state.sZone = false
		speakerStatus = "${app.label}:${state.sZone}"
		gvDevice.sendFollowMeSpeaker(speakerStatus)
		if(logEnable) log.debug "In zoneOffHandler - Zone is now off - sZone: ${state.sZone}"
	}
}

def initializeSpeaker() {
	if(logEnable) log.debug "In initializeSpeaker (${state.version}) - Initializing ${speakerSS}"
	speakerSS.initialize()
	if(gInitRepeat) repeat = gInitRepeat * 60
	if(gInitRepeat) runIn(repeat,initializeSpeaker)
}

// **********  Start code modified from @djgutheinz  **********
def letsTalkQueue(evt) {
    theText = evt.value
    if(useQueue) {
        if(logEnable) log.debug "In letsTalkQueue (${state.version}) - theText: ${theText}"
	    state.TTSQueue << [theText]
	    if(!state.playingTTS) { runInMillis(500, processQueue) }
    } else {
        if(logEnable) log.debug "In letsTalkQueue (${state.version}) - Queue not activated, going to letsTalk"
        letsTalk(theText)
    }
}

def processQueue() {
	if(logEnable) log.debug "In processQueue (${state.version})"
	state.playingTTS = true
    if(state.TTSQueue.size() >= maxQueued) clearTheQueue()
	if(state.TTSQueue.size() == 0) {
		state.playingTTS = false
        if(logEnable) log.info "In processQueue - size: ${state.TTSQueue.size()} - playingTTS: ${state.playingTTS} - Finished Playing"
		return
	}
	def nextTTS = state.TTSQueue[0]
    if(logEnable) log.info "In processQueue - size: ${state.TTSQueue.size()} - playingTTS: ${state.playingTTS} - Playing Next: ${nextTTS}"
    state.TTSQueue.remove(0)
	letsTalk(nextTTS)
    runIn(1,processQueue)
}
// **********  End code modified from @djgutheinz  **********

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk - msg: ${msg}"
    def message =  new JsonSlurper().parseText(msg) // Code modified from @storageanarchy
    // Reminder to reference the attributes as message.message, message.priority, message.title, etc

	if(triggerMode == "Always_On") alwaysOnHandler()
	if(state.sZone){
		checkTime()
		checkVol()
        
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
            
        checkPriority(priorityValue)
        
		if(logEnable) log.debug "In letsTalk - continuing"
		if(state.timeBetween) {
			state.sStatus = "speaking"
			speakerStatus = "${app.label}:${state.sStatus}"
			gvDevice.sendFollowMeSpeaker(speakerStatus)
            
            theMessage = message.message
            try {
                duration = textToSpeech(theText).duration + 3
            } catch (e) {
		        duration = 10
			}
            theDuration = duration * 1000
            
            state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
            state.speakers.each {
                priorityVoicesHandler(it,priorityVoice,theMessage)               
                if(!defaultSpeak) {    
                    switch(message.method) {        // Code modified from @storageanarchy
                        case 'deviceNotification':
                        beforeVolume(it)
                        it.speak(message.message)
                        pauseExecution(theDuration)
                        afterVolume(it)
                        if(logEnable) log.debug "In letsTalk - deviceNotification Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playAnnouncement':
                        it.playAnnouncement(message.message, message.priority, message.speakLevel, message.returnLevel, message.title)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - playAnnouncement Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playAnnouncementAll':
                        it.playAnnouncementAll(message.message, message.priority, message.speakLevel, message.returnLevel, message.title)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - playAnnouncementAll Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playText':
                        beforeVolume(it)
                        it.playText(message.message)
                        pauseExecution(theDuration)
                        afterVolume(it)
                        if(logEnable) log.debug "In letsTalk - playText Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playTextAndRestore':
                        beforeVolume(it)
                        it.playTextAndRestore(message.message, message.returnLevel)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - playTextAndRestore Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playTextAndResume':
                        beforeVolume(it)
                        it.playTextAndResume(message.message, message.returnLevel)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - playTextAndResume Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playTrack':
                        beforeVolume(it)
                        playSound(it)
                        it.playTrack(state.uriMessage)
                        pauseExecution(theDuration)
                        afterVolume(it)
                        if(logEnable) log.debug "In letsTalk - playTrack Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'playTrackAndRestore':
                        beforeVolume(it)
                        playSound(it)
                        it.playTrackAndRestore(state.uriMessage, message.returnLevel)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - playTrackAndRestore Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'setVolume':
                        it.setVolume(message.speakLevel)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - setVolume Received - speaker: ${it} - ${message.speakLevel}"
                        break;
                        case 'setVolumeSpeakAndRestore':
                        it.setVolumeSpeakAndRestore(message.message, message.priority, message.speakLevel, message.returnLevel)
                        pauseExecution(theDuration)
                        if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'setVolumeAndSpeak':
                        it.setVolumeAndSpeak(message.message, message.priority, message.speakLevel)
                        pauseExecution(theDuration)
                        afterVolume(it)
                        if(logEnable) log.debug "In letsTalk - setVolumeAndSpeak Received - speaker: ${it} - ${message.message}"
                        break;
                        case 'speak':
                        if(logEnable) log.debug "In letsTalk - speak - speaker: ${it} - Using best case handler"
                        if(it.hasCommand('setVolumeSpeakAndRestore')) {
                            if(logEnable) log.debug "In letsTalk - (speak) setVolumeSpeakAndRestore - ${it} - message: ${message.message}"
                            def prevVolume = it.currentValue("volume")
                            it.setVolumeSpeakAndRestore(state.volume, message.message, prevVolume)
                            pauseExecution(theDuration)
                        } else if(it.hasCommand('playTextAndRestore')) {   
                            if(logEnable) log.debug "In letsTalk - (speak) playTextAndRestore - ${it} - message: ${message.message}"
                            def prevVolume = it.currentValue("volume")
                            beforeVolume(it)
                            it.playTextAndRestore(message.message, prevVolume)
                            pauseExecution(theDuration)
                        } else if(it.hasCommand('playTrack')) {
                            if(logEnable) log.debug "In letsTalk - (speak) playTrack Received - speaker: ${it} - ${message.message}"
                            beforeVolume(it)
                            playSound(it)
                            it.playTrack(state.uriMessage)
                            pauseExecution(theDuration)
                            afterVolume(it)                               
                        } else {		        
                            if(logEnable) log.debug "In letsTalk - (speak) - ${it} - message: ${message.message}"
                            beforeVolume(it)
                            it.speak(message.message)
                            pauseExecution(theDuration)
                            afterVolume(it)
                        }
                        break; 
                    }
                } else {
                    if(logEnable) log.debug "In letsTalk - (Default speak) - ${it} - message: ${message.message}"
                    beforeVolume(it)
                    it.speak(message.message)
                    pauseExecution(theDuration)
                    afterVolume(it)
                }
            }
            speakerStatus = "${app.label}:${state.sZone}"
			gvDevice.sendFollowMeSpeaker(speakerStatus)
			if(logEnable) log.debug "In letsTalk - Ready for next message"
        } else {
		    if(logEnable) log.debug "In letsTalk (${state.version}) - Messages not allowed at this time"
	    }
	} else {
		if(logEnable) log.debug "In letsTalk (${state.version}) - Zone is off"
	}
}

def playSound(it) {
    if(state.sound) {
        it.playTrack(state.sound)
	    pauseExecution(state.sLength)
    }
}

def beforeVolume(it) {
    if(logEnable) log.debug "In beforeVolume (${state.version})"
    state.prevVolume = it.currentValue("volume")
    if(it.hasCommand('setVolume')) {
        it.setVolume(state.volume)
        if(logEnable) log.debug "In beforeVolume - Setting volume to ${state.volume}"
    } else {
        if(logEnable) log.debug "In beforeVolume - Volume was not changed"
    }
}
    
def afterVolume(it) {
    if(logEnable) log.debug "In afterVolume (${state.version})"
    if(it.hasCommand('setVolume')) {
        it.setVolume(state.prevVolume)
        if(logEnable) log.debug "In afterVolume - Setting volume to ${state.prevVolume}"
    } else {
        if(logEnable) log.debug "In afterVolume - Volume was not changed"
    }
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if(fromTime) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
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

def checkVol() {
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
    	if(state.quietTime) {
            if(logEnable) log.debug "In checkVol - Time within range - Using Quiet Time"
    		state.volume = volQuiet
		} else {
            if(logEnable) log.debug "In checkVol - Time outside of range - Not using Quiet Time"
			state.volume = volSpeech
		}
	} else {
        if(logEnable) log.debug "In checkVol - NO Quite Time Specified"
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

def priorityVoicesHandler(it,priorityVoice,lastSpoken) {
    if(lastSpoken == ".") lastSpoken = ""
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
            if(it.hasCommand('playTrack')) {
                state.sound = ""
                if(priorityVoice.contains("X")) {
                    state.sound = ""
                    state.sLength = s1Length * 1000
                } else
	            if(priorityVoice.contains("1")) {
                    if(sound1) {
                        state.sound = sound1
                        state.sLength = s1Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 1 not defined"
                    }
                } else
	            if(priorityVoice.contains("2")) {
                    if(sound2) {
                        state.sound = sound2
                        state.sLength = s2Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 2 not defined"
                    }
                } else
	            if(priorityVoice.contains("3")) {
                    if(sound3) {
                        state.sound = sound3
                        state.sLength = s3Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 3 not defined"
                    }
                } else
                if(priorityVoice.contains("4")) {
                    if(sound4) {
                        state.sound = sound4
                        state.sLength = s4Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 4 not defined"
                    }
                } else
                if(priorityVoice.contains("5")) {
                    if(sound5) {
                        state.sound = sound5
                        state.sLength = s5Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 5 not defined"
                    }
                } else
                if(priorityVoice.contains("6")) {
                    if(sound6) {
                        state.sound = sound6
                        state.sLength = s6Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 6 not defined"
                    }
                } else
                if(priorityVoice.contains("7")) {
                    if(sound7) {
                        state.sound = sound7
                        state.sLength = s7Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 7 not defined"
                    }
                } else
                if(priorityVoice.contains("8")) {
                    if(sound8) {
                        state.sound = sound8
                        state.sLength = s8Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 8 not defined"
                    }
                } else
                if(priorityVoice.contains("9")) {
                    if(sound9) {
                        state.sound = sound9
                        state.sLength = s9Length * 1000
                    } else {
                        if(logEnable) log.debug "${app.label} - Sound 9 not defined"
                    }
                } else
                if(priorityVoice.contains("0")) {
                    if(sound0) {
                        state.sound = ""
                        state.sLength = s0Length * 1000
                    } else { 
                        if(logEnable) log.debug "${app.label} - Sound 0 not defined"
                    }
                }
            } else { 
                if(logEnable) log.debug "Follow Me - ${speaker} doesn't support playTrack"
            }
        } catch (e) {
            //log.warn "Follow Me - priorityVoicesHandler - Something went wrong!"
            state.sound = ""
            state.sLength = 1000
        }
        state.uriMessage = uriMessage
    }
    if(logEnable) log.debug "In priorityVoicesHandler - Speaker: ${it} - priorityVoice: ${priorityVoice} - Voice: ${state.voiceSelected} - Message: ${lastSpoken} - uriMessage: ${state.uriMessage}"
}

def pushOrQueue(evt) {
    msg = evt.value
	if(logEnable) log.debug "In pushOrQueue (${state.version}) - ${msg}"
    def message =  new JsonSlurper().parseText(msg)    
    theMessage = message.message
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
		lastSpoken = "[L]" + theMessage
	}
	if(priorityValue.toUpperCase().contains("N")) {
		lastSpoken = "[N]" + theMessage
	}
	if(priorityValue.toUpperCase().contains("H")) {
		lastSpoken = "[H]" + theMessage
	}
    
	if(state.IH1 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH1 Sending message: ${theMessage}"
    	    sendPushMessage1.deviceNotification(theMessage)
        }
        if(messageDest == "Queue") {
            ps = "IH1"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH2 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH2 Sending message: ${theMessage}"
    	    sendPushMessage2.deviceNotification(theMessage)
        }
        if(messageDest == "Queue") {
            ps = "IH2"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH3 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH3 Sending message: ${theMessage}"
    	    sendPushMessage3.deviceNotification(theMessage)
        }
        if(messageDest == "Queue") {
            ps = "IH3"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH4 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH4 Sending message: ${theMessage}"
    	    sendPushMessage4.deviceNotification(theMessage)
        }
        if(messageDest == "Queue") {
            ps = "IH4"
            letsQueue(ps,theMessage)
        }
	}
	if(state.IH5 == "no") {
        if(messageDest == "Push") {
		    if(logEnable) log.debug "In pushOrQueue - IH5 Sending message: ${theMessage}"
    	    sendPushMessage5.deviceNotification(theMessage)
        }
        if(messageDest == "Queue") {
            ps = "IH5"
            letsQueue(ps,theMessage)
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
    if(state.whichButton == "testBtn1"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 1 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound1)
        } catch(e1) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn2"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 2 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound2)
        } catch(e2) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn3"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 3 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound3)
        } catch(e3) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn4"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 4 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound4)
        } catch(e4) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn5"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 5 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound5)
        } catch(e5) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn6"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 6 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound6)
        } catch(e6) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn7"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 7 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound7)
        } catch(e7) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn8"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 8 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound8)
        } catch(e8) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn9"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 9 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound9)
        } catch(e9) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn0"){
        if(logEnable) log.debug "In testButtonHandler - Testing Sound 0 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound0)
        } catch(e0) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
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
    if(logEnable) log.debug "In clearTheQueue (${state.version}) - Resetting the Queue"
    state.TTSQueue = []
	state.playingTTS = false
}

def showTheQueue() {
    app?.updateSetting("showQueue",[value:"false",type:"bool"])
    if(logEnable) log.debug "In showTheQueue (${state.version})"	
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

def setDefaults(){
	if(logEnable) log.debug "In setDefaults..."
	if(logEnable == null) {logEnable = false}
	if(messagePriority == null) {messagePriority = false}
    state.playingTTS = false
	state.TTSQueue = []
	if(state.sZone == null) {state.sZone = false}
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

