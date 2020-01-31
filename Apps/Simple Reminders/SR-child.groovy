/**
 *  **************** Simple Reminders Child App ****************
 *
 *  Design Usage:
 *  Setup Simple Reminders through out the day.
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
 *  V1.0.4 - 01/31/20 - Second Attempt to fix flashing, fix Repeat
 *  V1.0.3 - 01/29/20 - Attempt to fix flashing
 *  V1.0.2 - 12/08/19 - Fixed push messages
 *  V1.0.1 - 12/07/19 - Bug fixes
 *  V1.0.0 - 10/15/19 - Initial release.
 *
 */

import groovy.time.TimeCategory

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "SimpleRemindersChildVersion"
	state.version = "v1.0.4"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Simple Reminders Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Setup Simple Reminders through out the day.",
    category: "Convenience",
	parent: "BPTWorld:Simple Reminders",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Reminders/SR-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "reminderOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "notificationOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
            paragraph "Setup Simple Reminders through out the day."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Options, Options, Options")) {
            if(days1) {
                href "reminderOptions", title:"${getImage("checkMarkGreen")} Select Reminder options here", description:"Click here for Options"
            } else {
                href "reminderOptions", title:"Select Reminder options here", description:"Click here for Options"
            }
            if(speakerMP || speakerSS || switchesOn || switchesOff) {
                href "notificationOptions", title:"${getImage("checkMarkGreen")} Select Speech options here", description:"Click here for Options"
            } else {
                href "notificationOptions", title:"Select Speech options here", description:"Click here for Options"
            }
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required: false
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def reminderOptions() {
    dynamicPage(name: "reminderOptions", title: "<h2 style='color:#1A77C9;font-weight: bold'>Reminder Options</h2>", install: false, uninstall:false) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Reminder Options")) {
            paragraph "<b>Reminder 1</b>"
            input "days1", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
            if(days1) {
                input "timeToRun1", "time", title: "Time for Reminder", required: false, width:6
                input "everyOther1", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                input "msg1", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				input "list1", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				if(list1) {
					def values1 = "${msg1}".split(";")
					listMap1 = ""
    				values1.each { item1 -> listMap1 += "${item1}<br>" }
					paragraph "${listMap1}"
				}
                input "repeat1", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
				if(repeat1 && msg1) {
                    input "rControlSwitch1", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					input "repeatSeconds1", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					input "maxRepeats1", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					if(repeatSeconds1) {
						paragraph "Message will repeat every ${repeatSeconds1} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats1})"
						repeatTimeSeconds = (repeatSeconds1 * maxRepeats1)
						int inputNow=repeatTimeSeconds
						int nDayNow = inputNow / 86400
						int nHrsNow = (inputNow % 86400 ) / 3600
						int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
					}
				}
                paragraph "<hr>"
                input "switchesOn1", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			    input "switchesOff1", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                input "switchesFlash1", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                if(switchesFlash1) {                 
		            input "numOfFlashes1", "number", title: "Number of times (default: 2)", required: false, width: 6
                    input "delayFlashes1", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
                }
                input "newMode1", "mode", title: "Change to Mode", required: false, multiple: false
                
                paragraph "<hr>"
    	    }
            
            if(days1) {
                paragraph "<b>Reminder 2</b>"
                input "days2", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days2) {
                    input "timeToRun2", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther2", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg2", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list2", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list2) {
				    	def values2 = "${msg2}".split(";")
				    	listMap2 = ""
    			    	values2.each { item2 -> listMap2 += "${item2}<br>" }
					    paragraph "${listMap2}"
				    }
                    input "repeat2", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
				    if(repeat2 && msg2) {
                        input "rControlSwitch2", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds2", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats2", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds2) {
					    	paragraph "Message will repeat every ${repeatSeconds2} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats2})"
					    	repeatTimeSeconds = (repeatSeconds2 * maxRepeats2)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn2", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff2", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash2", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash2) {
		                input "numOfFlashes2", "number", title: "Number of times (default: 2)", required: false, width: 6
                        input "delayFlashes2", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
                    }
                    input "newMode2", "mode", title: "Change to Mode", required: false, multiple: false
				}
                paragraph "<hr>"
            }
            
            if(days2) {
                paragraph "<b>Reminder 3</b>"
                input "days3", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days3) {
                    input "timeToRun3", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther3", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg3", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list3", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list3) {
				    	def values3 = "${msg3}".split(";")
				    	listMap3 = ""
    			    	values3.each { item3 -> listMap3 += "${item3}<br>" }
					    paragraph "${listMap3}"
				    }
                    input "repeat3", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
				    if(repeat3 && msg3) {
                        input "rControlSwitch3", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds3", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats3", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds3) {
						    paragraph "Message will repeat every ${repeatSeconds3} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats3})"
						    repeatTimeSeconds = (repeatSeconds3 * maxRepeats3)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn3", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff3", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash3", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash3) {
		                input "numOfFlashes3", "number", title: "Number of times (default: 2)", required: false, width: 6
                        input "delayFlashes3", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
                    }
				}
                paragraph "<hr>"
            }
            
            if(days3) {
                paragraph "<b>Reminder 4</b>"
                input "days4", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days4) {
                    input "timeToRun4", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther4", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg4", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list4", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list4) {
				    	def values4 = "${msg4}".split(";")
				    	listMap4 = ""
    			    	values4.each { item4 -> listMap4 += "${item4}<br>" }
					    paragraph "${listMap4}"
				    }
                    input "repeat4", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
                    if(repeat4 && msg4) {
                        input "rControlSwitch4", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds4", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats4", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds4) {
						    paragraph "Message will repeat every ${repeatSeconds4} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats4})"
						    repeatTimeSeconds = (repeatSeconds4 * maxRepeats4)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn4", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff4", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash4", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash4) {
		                input "numOfFlashes4", "number", title: "Number of times (default: 2)", required: false, width: 6
                        input "delayFlashes4", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
                    }
                }
                paragraph "<hr>"
            }
            
            if(days4) {
                paragraph "<b>Reminder 5</b>"
                input "days5", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days5) {
                    input "timeToRun5", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther5", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg5", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list5", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list5) {
				    	def values5 = "${msg5}".split(";")
				    	listMap5 = ""
    			    	values5.each { item5 -> listMap5 += "${item5}<br>" }
					    paragraph "${listMap5}"
				    }
                    input "repeat5", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
                    if(repeat5 && msg5) {
                        input "rControlSwitch5", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds5", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats5", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds5) {
						    paragraph "Message will repeat every ${repeatSeconds5} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats5})"
						    repeatTimeSeconds = (repeatSeconds5 * maxRepeats5)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn5", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff5", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash5", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash5) {
		                input "numOfFlashes5", "number", title: "Number of times (default: 2)", required: false, width: 6
                        input "delayFlashes5", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
                    }
                }
            }
        }
    }
}

def notificationOptions() {
    dynamicPage(name: "notificationOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) {
            paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required:false, multiple:true, submitOnChange:true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required:false, multiple:true, submitOnChange:true
           paragraph "This app supports speaker proxies like, 'Follow Me'. This allows all speech to be controlled by one app. Follow Me features - Priority Messaging, volume controls, voices, sound files and more!"
           input "speakerProxy", "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange:true
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
		        paragraph "Speaker proxy in use"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
    	}
        
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def initialize() {
    setVersion()
    setDefaults()
	if(timeToRun1) schedule(timeToRun1, setTo1)
    if(timeToRun2) schedule(timeToRun2, setTo2)
    if(timeToRun3) schedule(timeToRun3, setTo3)
    if(timeToRun4) schedule(timeToRun4, setTo4)
    if(timeToRun5) schedule(timeToRun5, setTo5)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def setTo1(evt) { startTheProcess("1") }
def setTo2(evt) { startTheProcess("2") }
def setTo3(evt) { startTheProcess("3") }
def setTo4(evt) { startTheProcess("4") }
def setTo5(evt) { startTheProcess("5") }

def startTheProcess(numb) {
    if(logEnable) log.debug "In startTheProcess (${state.version}) - ${numb}"
    state.repeatSeconds = ""
    state.maxRepeats = ""
    state.everyOther = false
    
    if(numb == "1") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat1) {
            state.repeatSeconds = repeatSeconds1
            state.maxRepeats = maxRepeats1
        }
        if(everyOther1) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days1)
        if(state.daysMatch && switchesOn1) switchesOnHandler(switchesOn1)
        if(state.daysMatch && switchesOff1) switchesOffHandler(switchesOff1)
        if(state.daysMatch && switchesFlash1) flashLights(switchesFlash1,numOfFlashes1,delayFlashes1)
        if(state.daysMatch && newMode1) modeHandler(newMode1)
        if(state.daysMatch && (speakerMP || speakerSS) && msg1 != null) messageHandler(msg1)
    }
    if(numb == "2") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat2) {
            state.repeatSeconds = repeatSeconds2
            state.maxRepeats = maxRepeats2
        }
        if(everyOther2) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days2)
        if(state.daysMatch && switchesOn2) switchesOnHandler(switchesOn2)
        if(state.daysMatch && switchesOff2) switchesOffHandler(switchesOff2)
        if(state.daysMatch && switchesFlash2) flashLights(switchesFlash2,numOfFlashes2,delayFlashes2)
        if(state.daysMatch && (speakerMP || speakerSS) && msg2 != null) messageHandler(msg2)
    }
    if(numb == "3") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat3) {
            state.repeatSeconds = repeatSeconds3
            state.maxRepeats = maxRepeats3
        }
        if(everyOther3) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days3)
        if(state.daysMatch && switchesOn3) switchesOnHandler(switchesOn3)
        if(state.daysMatch && switchesOff3) switchesOffHandler(switchesOff3)
        if(state.daysMatch && switchesFlash3) flashLights(switchesFlash3,numOfFlashes3,delayFlashes3)
        if(state.daysMatch && (speakerMP || speakerSS) && msg3 != null) messageHandler(msg3)
    }
    if(numb == "4") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat4) {
            state.repeatSeconds = repeatSeconds4
            state.maxRepeats = maxRepeats4
        }
        if(everyOther4) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days4)
        if(state.daysMatch && switchesOn4) switchesOnHandler(switchesOn4)
        if(state.daysMatch && switchesOff4) switchesOffHandler(switchesOff4)
        if(state.daysMatch && switchesFlash4) flashLights(switchesFlash4,numOfFlashes4,delayFlashes4)
        if(state.daysMatch && (speakerMP || speakerSS) && msg4 != null) messageHandler(msg4)
    }
    if(numb == "5") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat5) {
            state.repeatSeconds = repeatSeconds5
            state.maxRepeats = maxRepeats5
        }
        if(everyOther5) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days5)
        if(state.daysMatch && switchesOn5) switchesOnHandler(switchesOn5)
        if(state.daysMatch && switchesOff5) switchesOffHandler(switchesOff5)
        if(state.daysMatch && switchesFlash5) flashLights(switchesFlash5,numOfFlashes5,delayFlashes5)
        if(state.daysMatch && (speakerMP || speakerSS) && msg5 != null) messageHandler(msg5)
    }
}

def letsTalk(msg) {
	if(logEnable) log.debug "In letsTalk (${state.version}) - Here we go"
	checkTime()
	checkVol()
    checkEveryOther()
    if(logEnable) log.debug "In letsTalk - Checking daysMatch: ${state.daysMatch} - timeBetween: ${state.timeBetween} - thisTime: ${state.thisTime}"
    if(state.timeBetween && state.daysMatch && state.thisTime == "yes") {
        if(msg) state.theMsg = msg
        if(state.maxRepeats == null || state.maxRepeats == "") state.maxRepeats = 1
        if(state.repeatSeconds == null || state.repeatSeconds == "") state.repeatSeconds = 2
        
        for(x=0;x < state.maxRepeats;x++) {
            checkControlSwitch()
            if(state.controlValue == "on") {               
                def duration = Math.max(Math.round(state.theMsg.length()/12),2)+3
                theDuration = duration * 1000
                state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
    	        if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${state.theMsg}, volRestore: ${volRestore}"
                state.speakers.each { it ->
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
                        pauseExecution(theDuration)
                        if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                        if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
                    }
                } 
                repeatDelay = state.repeatSeconds * 1000
                pauseExecution(repeatDelay)
            } else {
                if(logEnable) log.debug "In checkRepeat - Control Switch is OFF (${state.controlValue})."
            }
        }
        if(state.theSwitch) state.theSwitch.off()
        if(logEnable) log.debug "In letsTalk - Finished speaking"  
        if(sendPushMessage) pushNow(state.theMsg)
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
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

def checkControlSwitch() {
    if(logEnable) log.debug "In checkControlSwitch (${state.version})"
    state.controlValue = ""
    state.theSwitch = ""
    if(repeat1) { state.controlValue = rControlSwitch1.currentValue("switch"); state.theSwitch = rControlSwitch1 }
    if(repeat2) { state.controlValue = rControlSwitch2.currentValue("switch"); state.theSwitch = rControlSwitch2 }
    if(repeat3) { state.controlValue = rControlSwitch3.currentValue("switch"); state.theSwitch = rControlSwitch3 }
    if(repeat4) { state.controlValue = rControlSwitch4.currentValue("switch"); state.theSwitch = rControlSwitch4 }
    if(repeat5) { state.controlValue = rControlSwitch5.currentValue("switch"); state.theSwitch = rControlSwitch5 }
    if(state.controlValue == null || state.controlValue == "") state.controlValue = "on"
}

def checkEveryOther() {
    if(logEnable) log.debug "In checkEveryOther (${state.version})"
    if(state.everyOther) {
        if(state.thisTime == null || state.thisTime == "") state.thisTime = "no"
        if(state.thisTime == "yes") {
            state.thisTime = "no"
        } else {
            state.thisTime = "yes"
        }
    } else {
        state.thisTime = "yes"
    }
    if(logEnable) log.debug "In checkEveryOther - thisTime: ${state.thisTime}"
}

def messageHandler(msg) {
	if(logEnable) log.debug "In messageHandler (${state.version})"
	state.theMsg = ""
    
	def values = "${msg}".split(";")
	vSize = values.size()
    count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	msg = values[randomKey]
	if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, msg: ${msg}"
    
	if(logEnable) log.debug "In messageHandler - theMsg: ${theMsg}"
        
    letsTalk(msg)
}

def dayOfTheWeekHandler(days) {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
	Calendar date = Calendar.getInstance()
	int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK)
	if(dayOfTheWeek == 1) state.dotWeek = "Sunday"
	if(dayOfTheWeek == 2) state.dotWeek = "Monday"
	if(dayOfTheWeek == 3) state.dotWeek = "Tuesday"
	if(dayOfTheWeek == 4) state.dotWeek = "Wednesday"
	if(dayOfTheWeek == 5) state.dotWeek = "Thursday"
	if(dayOfTheWeek == 6) state.dotWeek = "Friday"
	if(dayOfTheWeek == 7) state.dotWeek = "Saturday"

	if(days.contains(state.dotWeek)) {
		if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Passed"
		state.daysMatch = true
	} else {
		if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Check Failed"
		state.daysMatch = false
	}
}

def pushNow(theMsg){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

private flashLights(switchesToFlash,numOfFlashes,delayFlashes) {    // Modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
	def doFlash = true
	def delay = delayFlashes ?: 500
	def numFlashes = numOfFlashes ?: 2

	if(logEnable) log.debug "In flashLights - LAST ACTIVATED: ${state.lastActivated}"
	if(state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (delay)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash - ELAPSED: $elapsed - LAST ACTIVATED: ${state.lastActivated}"
	}

	if(doFlash) {
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		state.lastActivated = now()
		if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switchesToFlash.collect{it.currentSwitch != "on"}

		numFlashes.times {
			if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.on()
				}
				else {
                    pauseExecution(delay)
					s.off()
				}
			}
			if(logEnable) log.debug "In flashLights - Switch off after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.off()
				}
				else {
                    pauseExecution(delay)
					s.on()
				}
			}
		}
	}
}

def switchesOnHandler(switchesOn) {
	switchesOn.each { it ->
		if(logEnable) log.debug "In switchOnHandler - Turning on ${it}"
		it.on()
	}
}

def switchesOffHandler(switchesOff) {
	switchesOff.each { it ->
		if(logEnable) log.debug "In switchOffHandler - Turning off ${it}"
		it.off()
	}
}

def modeHandler(newMode) {
	if(logEnable) log.debug "In modeHandler - Changing mode to ${newMode}"
	setLocationMode(newMode)
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    state.numRepeats = 1
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
    section (getFormat("title", "${getImage("logo")}" + " Simple Reminders - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Simple Reminders - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
