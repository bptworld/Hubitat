/**
 *  **************** Simple Reminders Child App ****************
 *
 *  Design Usage:
 *  Setup Simple Reminders through out the day.
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
 *  V1.0.0 - 10/15/19 - Initial release.
 *
 */

import groovy.time.TimeCategory

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "SimpleRemindersChildVersion"
	state.version = "v1.0.0"
    
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
            if(days01) {
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
            input "days01", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
            if(days01) {
                input "timeToRun01", "time", title: "Time for Reminder", required: false, width:6
                input "everyOther01", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                input "msg01", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				input "list01", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				if(list01) {
					def values01 = "${msg01}".split(";")
					listMap01 = ""
    				values01.each { item01 -> listMap01 += "${item01}<br>" }
					paragraph "${listMap01}"
				}
                input "repeat01", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
				if(repeat01 && msg01) {
                    input "rControlSwitch01", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					input "repeatSeconds01", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					input "maxRepeats01", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					if(repeatSeconds01) {
						paragraph "Message will repeat every ${repeatSeconds01} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats01})"
						repeatTimeSeconds = (repeatSeconds01 * maxRepeats01)
						int inputNow=repeatTimeSeconds
						int nDayNow = inputNow / 86400
						int nHrsNow = (inputNow % 86400 ) / 3600
						int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
					}
				}
                paragraph "<hr>"
                input "switchesOn01", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			    input "switchesOff01", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                input "switchesFlash01", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                if(switchesFlash01) {
		            input "numFlashes01", "number", title: "Flash this number of times", required: true, width: 4, defaultValue: 2
                    input "onFor01", "number", title: "On for (in seconds)", required: true, width: 4, defaultValue: 1
		            input "offFor01", "number", title: "Off for (in seconds)", required: true, width: 4, defaultValue: 1
                }
                input "newMode01", "mode", title: "Change to Mode", required: false, multiple: false
                
                paragraph "<hr>"
    	    }
            
            if(days01) {
                paragraph "<b>Reminder 2</b>"
                input "days02", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days02) {
                    input "timeToRun02", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther02", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg02", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list02", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list02) {
				    	def values02 = "${msg02}".split(";")
				    	listMap02 = ""
    			    	values02.each { item02 -> listMap02 += "${item02}<br>" }
					    paragraph "${listMap02}"
				    }
                    input "repeat02", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
				    if(repeat02 && msg02) {
                        input "rControlSwitch02", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds02", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats02", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds02) {
					    	paragraph "Message will repeat every ${repeatSeconds02} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats02})"
					    	repeatTimeSeconds = (repeatSeconds02 * maxRepeats02)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn02", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff02", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash02", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash02) {
		                input "numFlashes02", "number", title: "Flash this number of times", required: true, width: 4, defaultValue: 2
                        input "onFor02", "number", title: "On for (in seconds)", required: true, width: 4, defaultValue: 1
		                input "offFor02", "number", title: "Off for (in seconds)", required: true, width: 4, defaultValue: 1
                    }
                    input "newMode02", "mode", title: "Change to Mode", required: false, multiple: false
				}
                paragraph "<hr>"
            }
            
            if(days02) {
                paragraph "<b>Reminder 3</b>"
                input "days03", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days03) {
                    input "timeToRun03", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther03", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg03", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list03", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list03) {
				    	def values03 = "${msg03}".split(";")
				    	listMap03 = ""
    			    	values03.each { item03 -> listMap03 += "${item03}<br>" }
					    paragraph "${listMap03}"
				    }
                    input "repeat03", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
				    if(repeat03 && msg03) {
                        input "rControlSwitch03", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds03", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats03", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds03) {
						    paragraph "Message will repeat every ${repeatSeconds03} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats03})"
						    repeatTimeSeconds = (repeatSeconds03 * maxRepeats03)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn03", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff03", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash03", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash03) {
		                input "numFlashes03", "number", title: "Flash this number of times", required: true, width: 4, defaultValue: 2
                        input "onFor03", "number", title: "On for (in seconds)", required: true, width: 4, defaultValue: 1
		                input "offFor03", "number", title: "Off for (in seconds)", required: true, width: 4, defaultValue: 1
                    }
				}
                paragraph "<hr>"
            }
            
            if(days03) {
                paragraph "<b>Reminder 4</b>"
                input "days04", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days04) {
                    input "timeToRun04", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther04", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg04", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list04", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list04) {
				    	def values04 = "${msg04}".split(";")
				    	listMap04 = ""
    			    	values04.each { item04 -> listMap04 += "${item04}<br>" }
					    paragraph "${listMap04}"
				    }
                    input "repeat04", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
                    if(repeat04 && msg04) {
                        input "rControlSwitch04", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds04", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats04", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds04) {
						    paragraph "Message will repeat every ${repeatSeconds04} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats04})"
						    repeatTimeSeconds = (repeatSeconds04 * maxRepeats04)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn04", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff04", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash04", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash04) {
		                input "numFlashes04", "number", title: "Flash this number of times", required: true, width: 4, defaultValue: 2
                        input "onFor04", "number", title: "On for (in seconds)", required: true, width: 4, defaultValue: 1
		                input "offFor04", "number", title: "Off for (in seconds)", required: true, width: 4, defaultValue: 1
                    }
                }
                paragraph "<hr>"
            }
            
            if(days04) {
                paragraph "<b>Reminder 5</b>"
                input "days05", "enum", title: "Set Reminder on these days", description: "Days", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], width:6
                if(days05) {
                    input "timeToRun05", "time", title: "Time for Reminder", required: false, width:6
                    input "everyOther05", "bool", defaultValue: false, title: "<b>Every other?</b>", description: "Every other", submitOnChange: true
                    paragraph "<small>* To get this on the right schedule, don't turn this switch on until the week you want to start the reminder. Also, if using the Every Other option - only ONE day can be selected.</small>"
                    input "msg05", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)",  required: false, submitOnChange: "true"
				    input "list05", "bool", defaultValue: "false", title: "Show a list view of the random messages?", description: "List", submitOnChange: true
				    if(list05) {
				    	def values05 = "${msg05}".split(";")
				    	listMap05 = ""
    			    	values05.each { item05 -> listMap05 += "${item05}<br>" }
					    paragraph "${listMap05}"
				    }
                    input "repeat05", "bool", defaultValue: false, title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: true
                    if(repeat05 && msg05) {
                        input "rControlSwitch05", "capability.switch", title: "Control Switch to turn this reminder on and off", required: true, multiple: false
					    paragraph "<b>* Control Switch is required when using the Message Repeat option.</b>"
					    paragraph "Repeat message every X seconds until 'Control Switch' is turned off OR max number of repeats is reached."
					    input "repeatSeconds05", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					    input "maxRepeats05", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: true
					    if(repeatSeconds05) {
						    paragraph "Message will repeat every ${repeatSeconds05} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats05})"
						    repeatTimeSeconds = (repeatSeconds05 * maxRepeats05)
						    int inputNow=repeatTimeSeconds
						    int nDayNow = inputNow / 86400
						    int nHrsNow = (inputNow % 86400 ) / 3600
						    int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						    int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						    paragraph "In this case, it would take ${nHrsNow} Hour(s), ${nMinNow} Min(s) and ${nSecNow} Second(s) to reach the max number of repeats (if Control Switch is not turned off)"
                        }
					}
                    paragraph "<hr>"
                    input "switchesOn05", "capability.switch", title: "Turn these switches ON", required: false, multiple: true
			        input "switchesOff05", "capability.switch", title: "Turn these switches OFF", required: false, multiple: true
                    input "switchesFlash05", "capability.switch", title: "Flash these lights", required: false, multiple: true, submitOnChange:true
                    if(switchesFlash05) {
		                input "numFlashes05", "number", title: "Flash this number of times", required: true, width: 4, defaultValue: 2
                        input "onFor05", "number", title: "On for (in seconds)", required: true, width: 4, defaultValue: 1
		                input "offFor05", "number", title: "Off for (in seconds)", required: true, width: 4, defaultValue: 1
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
	if(timeToRun01) schedule(timeToRun01, setTo01)
    if(timeToRun02) schedule(timeToRun02, setTo02)
    if(timeToRun03) schedule(timeToRun03, setTo03)
    if(timeToRun04) schedule(timeToRun04, setTo04)
    if(timeToRun05) schedule(timeToRun05, setTo05)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def setTo01(evt) { startTheProcess("01") }
def setTo02(evt) { startTheProcess("02") }
def setTo03(evt) { startTheProcess("03") }
def setTo04(evt) { startTheProcess("04") }
def setTo05(evt) { startTheProcess("05") }

def startTheProcess(numb) {
    if(logEnable) log.debug "In startTheProcess (${state.version}) - ${numb}"
    if(numb == "01") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat01) {
            state.repeatSeconds = repeatSeconds01
            state.maxRepeats = maxRepeats01
            state.numb = "01"
        }
        if(everyOther01) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days01)
        if(state.daysMatch && (speakerMP || speakerSS) && msg01 != null) messageHandler(msg01)
        if(state.daysMatch && switchesOn01) switchesOnHandler(switchesOn01)
        if(state.daysMatch && switchesOff01) switchesOffHandler(switchesOff01)
        if(state.daysMatch && switchesFlash01) flashLights(switchesFlash01,numFlashes01,onFor01,offFor01)
        if(state.daysMatch && newMode01) modeHandler(newMode01)
    }
    if(numb == "02") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat02) {
            state.repeatSeconds = repeatSeconds02
            state.maxRepeats = maxRepeats02
            state.numb = "02"
        }
        if(everyOther02) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days02)
        if(state.daysMatch && (speakerMP || speakerSS) && msg02 != null) messageHandler(msg02)
        if(state.daysMatch && switchesOn02) switchesOnHandler(switchesOn02)
        if(state.daysMatch && switchesOff02) switchesOffHandler(switchesOff02)
        if(state.daysMatch && switchesFlash02) flashLights(switchesFlash02,numFlashes02,onFor02,offFor02)
    }
    if(numb == "03") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat03) {
            state.repeatSeconds = repeatSeconds03
            state.maxRepeats = maxRepeats03
            state.numb = "03"
        }
        if(everyOther03) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days03)
        if(state.daysMatch && (speakerMP || speakerSS) && msg03 != null) messageHandler(msg03)
        if(state.daysMatch && switchesOn03) switchesOnHandler(switchesOn03)
        if(state.daysMatch && switchesOff03) switchesOffHandler(switchesOff03)
        if(state.daysMatch && switchesFlash03) flashLights(switchesFlash03,numFlashes03,onFor03,offFor03)
    }
    if(numb == "04") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat04) {
            state.repeatSeconds = repeatSeconds04
            state.maxRepeats = maxRepeats04
            state.numb = "04"
        }
        if(everyOther04) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days04)
        if(state.daysMatch && (speakerMP || speakerSS) && msg04 != null) messageHandler(msg04)
        if(state.daysMatch && switchesOn04) switchesOnHandler(switchesOn04)
        if(state.daysMatch && switchesOff04) switchesOffHandler(switchesOff04)
        if(state.daysMatch && switchesFlash04) flashLights(switchesFlash04,numFlashes04,onFor04,offFor04)
    }
    if(numb == "05") {
        if(logEnable) log.debug "In startTheProcess (${numb})"
        if(repeat05) {
            state.repeatSeconds = repeatSeconds05
            state.maxRepeats = maxRepeats05
            state.numb = "05"
        }
        if(everyOther05) {
            state.everyOther = true
        }
        dayOfTheWeekHandler(days05)
        if(state.daysMatch && (speakerMP || speakerSS) && msg05 != null) messageHandler(msg05)
        if(state.daysMatch && switchesOn05) switchesOnHandler(switchesOn05)
        if(state.daysMatch && switchesOff05) switchesOffHandler(switchesOff05)
        if(state.daysMatch && switchesFlash05) flashLights(switchesFlash05,numFlashes05,onFor05,offFor05)
    }
}

def letsTalk(msg) {
	if(logEnable) log.debug "In letsTalk (${state.version}) - Here we go"
	checkTime()
	checkVol()
    checkEveryOther()
    if(logEnable) log.debug "In letsTalk - Checking daysMatch: ${state.daysMatch} - timeBetween: ${state.timeBetween} - thisTime: ${state.thisTime}"
    if(state.timeBetween && state.daysMatch && state.thisTime == "yes") {
        if(msg == null || msg == "") msg = state.lastMsg 
        theMsg = msg
        
        def duration = Math.max(Math.round(theMsg.length()/12),2)+3
        theDuration = duration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
    	if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${theMsg}, volRestore: ${volRestore}"
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
                pauseExecution(theDuration)
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
            }
        }
            
        if(logEnable) log.debug "In letsTalk - Finished speaking"  
        if(sendPushMessage) pushNow(theMsg)
        
        if(state.rControlSwitch) {
            state.lastMsg = theMsg
            checkRepeat()
        }
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

def checkRepeat() {
    if(logEnable) log.debug "In checkRepeat (${state.version}) - numb: ${state.numb}"
    if(state.numb == "01") { cSwitch = rControlSwitch01.currentValue("switch"); theSwitch = rControlSwitch01 }
    if(state.numb == "02") { cSwitch = rControlSwitch02.currentValue("switch"); theSwitch = rControlSwitch02 }
    if(state.numb == "03") { cSwitch = rControlSwitch03.currentValue("switch"); theSwitch = rControlSwitch03 }
    if(state.numb == "04") { cSwitch = rControlSwitch04.currentValue("switch"); theSwitch = rControlSwitch04 }
    if(state.numb == "05") { cSwitch = rControlSwitch05.currentValue("switch"); theSwitch = rControlSwitch05 }    

    if(logEnable) log.debug "In checkRepeat - Control Switch is ${cSwitch}"
	if(cSwitch == "on") {
        if(logEnable) log.debug "In checkRepeat - repeat ON - value: ${cSwitch}"
        if(state.numRepeats == null) state.numRepeats = 1
		if(state.numRepeats < state.maxRepeats) {
            if(logEnable) log.debug "In checkRepeat - repeat ON - numRepeats: ${state.numRepeats} - maxRepeats: ${state.maxRepeats}"
		    state.numRepeats = state.numRepeats + 1
			runIn(state.repeatSeconds,letsTalk)
		} else {
			if(logEnable) log.debug "In checkRepeat - Max repeats has been reached."
            theSwitch.off()
            state.numRepeats = 1
            if(logEnable) log.debug "In checkRepeat - Control Switch is now OFF (${cSwitch})."
		}
        log.info "Simple Reminders - Control Switch is ON, message will repeat in ${state.repeatSeconds} seconds."
	} else {
		if(logEnable) log.debug "In checkRepeat - Control Switch is ${cSwitch}."
	}
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

def pushHandler(){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${state.msg}"
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

private flashLights(switchesFlash,numFlashes,onFor,offFor) {    // Code modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
	def doFlash = true
    
	if(logEnable) log.debug "In flashLights - lastActivated: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		state.lastActivated = now()
		if(logEnable) log.debug "In flashLights - lastActivated set to: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0
		numFlashes.times {
			if(logEnable) log.debug "In flashLights - Switch on after $delay sec"
			switchesFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.on()
				}
				else {
                    pauseExecution(delay)
					s.off()
				}
			}
			delay += onFor
			if(logEnable) log.debug "In flashLights - Switch off after $delay sec"
			switchesFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.off()
				}
				else {
                    pauseExecution(delay)
					s.on()
				}
			}
			delay += offFor
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
