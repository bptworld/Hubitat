/**
 *  **************** Device Check Plus Child App  ****************
 *
 *  Design Usage:
 *  Check selected devices, then warn you what's not in the right state.
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
 *  V1.0.1 - 10/13/19 - Cosmetic changes. 
 *  V1.0.0 - 10/13/19 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "DeviceCheckPlusChildVersion"
	state.version = "v1.0.1"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Device Check Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Check selected devices, then warn you what's not in the right state.",
    category: "Convenience",
	parent: "BPTWorld:Device Check Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Check%20Plus/DCP-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "checkConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "triggerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "notificationOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Device Check Plus</h2>", install: true, uninstall: true) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Check selected devices, then warn you what's not in the right state."
            paragraph "<b>Examples of Usage:</b>"
            paragraph " - <u>Getting ready to go to bed</u><br> * hit the 'On demand' switch (or use Google to turn it on)<br> * Check will run and announce any problems!<br> * Go to bed knowing everything is secure!"
            paragraph " - <u>Heat is on</u><br> * Someone opens a window or door<br> * Check will run and announce what window is open!"
            paragraph " - <u>Cool is on</u><br> * Someone closes a door<br> * Check will run and announce that the door should be open when cool is on!"
            paragraph " - <u>Other usage...</u><br> * Going out? Make sure all your windows are closed<br> * Is it raining, check the windows!<br> * Think you forgot to do something? This will let you know!"
            paragraph "<b>The only limit is your imagination!</b>"
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Options, Options and more Options")) {
            if(switchesOn || switchesOff || contactsOpen || contactsClosed || locksLocked || locksUnlocked) {
                href "checkConfig", title:"${getImage("checkMarkGreen")} Select Devices to check", description:"Click here for Options"
            } else {
                href "checkConfig", title:"Select Devices to check", description:"Click here for Options"
            }
            
            if(onDemandSwitch || days || modeName || thermostats) {
                href "triggerOptions", title:"${getImage("checkMarkGreen")} Select Trigger options here", description:"Click here for Options"
            } else {
                href "triggerOptions", title:"Select Trigger options here", description:"Click here for Options"
            }
            
            if(isDataDevice || preMsg || postMsg) {
                href "notificationOptions", title:"${getImage("checkMarkGreen")} Select Notification options here", description:"Click here for Options"
            } else {
                href "notificationOptions", title:"Select Notification options here", description:"Click here for Options"
            }
            
            if(isDataDevice || preMsg || postMsg) {
                href "speechOptions", title:"${getImage("checkMarkGreen")} Select Speech options here", description:"Click here for Options"
            } else {
                href "speechOptions", title:"Select Speech options here", description:"Click here for Options"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            paragraph "Device Check Plus uses an experimental speach queue. In testing, sometimes it gets 'stuck' and queues all the messages. To recover from this, please use the options below."
			input "maxQueued", "number", title: "Max number of messages to be queued before auto clear is issued (default=5)", required: true, defaultValue: 5
            input(name: "clearQueue", type: "bool", defaultValue: "false", title: "Manually Clear the Queue right now", description: "Clear", submitOnChange: "true")
            if(clearQueue) clearTheQueue()
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: true
		}
		display2()
	}
}

def checkConfig() {
    dynamicPage(name: "checkConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Check Devices</h2>", install: false, uninstall:false) {
		section(getFormat("header-green", "${getImage("Blank")}"+"  Devices to Check")) {
			input "switchesOn", "capability.switch", title: "Switches that should be ON", multiple: true, required: false
			input "switchesOff", "capability.switch", title: "Switches that should be OFF", multiple: true, required: false
			input "contactsOpen", "capability.contactSensor", title: "Contact Sensors that should be OPEN", multiple: true, required: false
			input "contactsClosed", "capability.contactSensor", title: "Contact Sensors that should be CLOSED", multiple: true, required: false
			input "locksLocked", "capability.lock", title: "Door Locks that should be LOCKED", multiple: true, required: false
			input "locksUnlocked", "capability.lock", title: "Door Locks that should be UNLOCKED", multiple: true, required: false
		}
    }
}

def triggerOptions() {
    dynamicPage(name: "triggerOptions", title: "<h2 style='color:#1A77C9;font-weight: bold'>Trigger Options</h2>", install: false, uninstall:false) {
		section(getFormat("header-green", "${getImage("Blank")}"+"  Trigger Options")) {
            paragraph "<b>Run 'Device Check' anytime this switch is turned on.</b> Recommended to create a 'virtual switch' with 'Enable auto off' set to '1s'"
            input "onDemandSwitch", "capability.switch", title: "Check On Demand Switch", required: false
            paragraph "<hr>"
            paragraph "<b>Run 'Device Check' on a set schedule</b> (optional)"
            input(name: "days", type: "enum", title: "Only run on these days", description: "Days to run", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
            if(days) input "timeToRun", "time", title: "Auto Run at", required: true
            paragraph "<hr>"
            paragraph "<b>Run 'Device Check' on Mode Changes</b> (optional)"
            input "modeName", "mode", title: "When hub changes to this Mode", required: false, multiple: true
            paragraph "<hr>"
            paragraph "<b>Run 'Device Check' on Thermostat Activity</b> (optional)"
            input "thermostats", "capability.thermostat", title: "Thermostat to track", required: false, multiple: true, submitOnChange: true
            if(thermostats) {
                input "thermOption", "bool", title: "Use Mode or State (off=Mode, on=State)", description: "Therm Options", defaultValue: true, submitOnChange: true
                paragraph " - <b>Mode</b>: When in heat or cool mode, it will trigger a 'Device Check' anytime a selected device changes state."
                paragraph " - <b>State</b>: This will trigger a 'Device Check' anytime the thermostat goes into heating or cooling state."
            }
        }
    }
}

def notificationOptions() {
    dynamicPage(name: "notificationOptions", title: "<h2 style='color:#1A77C9;font-weight: bold'>Notification Options</h2>", install: false, uninstall:false) {
		section(getFormat("header-green", "${getImage("Blank")}"+"  Notification Options")) {
            input "isDataDevice", "capability.switch", title: "Turn this device on if there are devices to report", submitOnChange: true, required: false, multiple: false
			paragraph "<hr>"
            paragraph "Receive device notifications with voice and push options."
			paragraph "Each of the following messages will only be spoken if necessary..."
			input(name: "oRandomPre", type: "bool", defaultValue: "false", title: "Random Pre Message?", description: "Random", submitOnChange: "true")
			if(!oRandomPre) input "preMsg", "text", required: true, title: "Pre Message - Single message", defaultValue: "Warning"
			if(oRandomPre) {
				input "preMsg", "text", title: "Random Pre Message - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oPreList", type: "bool", defaultValue: "false", title: "Show a list view of the random pre messages?", description: "List View", submitOnChange: "true")
				if(oPreList) {
					def valuesPre = "${preMsg}".split(";")
					listMapPre = ""
    				valuesPre.each { itemPre -> listMapPre += "${itemPre}<br>" }
					paragraph "${listMapPre}"
				}
			}
            paragraph "<b>All switches/contacts/locks in the wrong state will then be spoken</b>"
			input(name: "oRandomPost", type: "bool", defaultValue: "false", title: "Random Post Message?", description: "Random", submitOnChange: "true")
			if(!oRandomPost) input "postMsg", "text", required: true, title: "Post Message - Single message", defaultValue: "This is all I have to say"
			if(oRandomPost) {
				input "postMsg", "text", title: "Random Post Message - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: "true"
				input(name: "oPostList", type: "bool", defaultValue: "false", title: "Show a list view of the random post messages?", description: "List View", submitOnChange: "true")
				if(oPostList) {
					def valuesPost = "${postMsg}".split(";")
					listMapPost = ""
    				valuesPost.each { itemPost -> listMapPost += "${itemPost}<br>" }
					paragraph "${listMapPost}"
				}
			}
        }
    }
}

def speechOptions() {
    dynamicPage(name: "speechOptions", title: "<h2 style='color:#1A77C9;font-weight: bold'>Speech Options</h2>", install: false, uninstall:false) {
		section(getFormat("header-green", "${getImage("Blank")}"+"  Speech Options")) {
            paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
           input(name: "speakerProxy", type: "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy")
        }
        if(speakerMP || speakerSS) {
		    section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		        paragraph "NOTE: Not all speakers can use volume controls.", width:8
                paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true, width: 6
		        input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true, width: 6
                input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required: false, submitOnChange: true
		    	if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true, width: 6
    	    	if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true, width: 6
            }
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

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
	initialize()
}

def initialize() {
    setDefaults()
	if(onDemandSwitch) subscribe(onDemandSwitch, "switch.on", deviceHandler)
    if(days) schedule(timeToRun, deviceHandler)
    if(modeName) subscribe(location, "mode", modeHandler)
    if(thermostats && thermOption == true) subscribe(thermostats, "thermostatOperatingState.heating", thermostatHandler)
    if(thermostats && thermOption == true) subscribe(thermostats, "thermostatOperatingState.cooling", thermostatHandler)
    
    if(thermostats && thermOption == false) subscribe(contactsOpen, "contact", thermostatModeHandler)
    if(thermostats && thermOption == false) subscribe(contactsClosed, "contact", thermostatModeHandler)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def deviceHandler(evt){
    if(logEnable) log.debug "In deviceHandler (${state.version})"
    state.wrongSwitchesMSG = ""
    state.wrongContactsMSG = ""
    state.wrongLocksMSG = ""

// Start Switch
	if(switchesOn || switchesOff) {
		if(switchesOn) {
			switchesOn.each { sOn -> 
				def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('switch')
				if(logEnable) log.debug "In deviceHandler - Switch On - ${switchName} - ${switchStatus}"
				if(switchStatus == "off") state.wrongSwitchesMSG += "${switchName}, "
			}
		}
		if(switchesOff) {
			switchesOff.each { sOff -> 
				def switchName = sOff.displayName
				def switchStatus = sOff.currentValue('switch')
				if(logEnable) log.debug "In deviceHandler - Switch Off - ${switchName} - ${switchStatus}"
				if(switchStatus == "on") state.wrongSwitchesMSG += "${switchName}, "
			}
		}
	}
	
// Start Contacts
	if(contactsOpen || contactsClosed) {
		if(contactsOpen) {
			contactsOpen.each { cOpen ->
				def contactName = cOpen.displayName
				def contactStatus = cOpen.currentValue('contact')
				if(logEnable) log.debug "In deviceHandler - Contact Open - ${contactName} - ${contactStatus}"
                if(contactStatus == "closed") state.wrongContactsMSG += "${contactName}, "
			}
		}
		if(contactsClosed) {
			contactsClosed.each { cClosed ->
				def contactName = cClosed.displayName
				def contactStatus = cClosed.currentValue('contact')
				if(logEnable) log.debug "In deviceHandler - Contact Closed - ${contactName} - ${contactStatus}"
				if(contactStatus == "open") state.wrongContactsMSG += "${contactName}, "
			}
		}
	}
		
// Start Locks
	if(locksUnlocked || locksLocked) {
		if(locksUnlocked) {
			locksUnlocked.each { lUnlocked ->
				def lockName = lUnlocked.displayName
				def lockStatus = lUnlocked.currentValue('lock')
				if(logEnable) log.debug "In deviceHandler - Locks Unlocked - ${lockName} - ${lockStatus}"
				if(lockStatus == "locked") state.wrongLocksMSG += "${lockName}, "
			}
		}
		if(locksLocked) {
			locksLocked.each { lLocked ->
				def lockName = lLocked.displayName
				def lockStatus = lLocked.currentValue('lock')
				if(logEnable) log.debug "In deviceHandler - Locks Locked - ${lockName} - ${lockStatus}"
				if(lockStatus == "unlocked") state.wrongLocksMSG += "${lockName}, "
			}
		}
	}
    
// Is there Data
    if((state.wrongSwitchesMSG != "") || (state.wrongContactsMSG != "") || (state.wrongLocksMSG != "")) {
        if(isDataDevice) { isDataDevice.on() }
        state.isData = "yes"
        messageHandler()
    }
    if((state.wrongSwitchesMSG == "") && (state.wrongContactsMSG == "") && (state.wrongLocksMSG == "")) {
        if(isDataDevice) { isDataDevice.off() }
        state.isData = "no"
    }
}

def letsTalkQueue(text) {
    if(logEnable) log.debug "In letsTalkQueue (${state.version}) - ${text}"
    // Start modified from @djgutheinz
    def duration = Math.max(Math.round(text.length()/12),2)+3
	state.TTSQueue << [text, duration]
    
    queueSize = state.TTSQueue.size()
    if(queueSize > maxQueued) clearTheQueue()
 
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
	queueSize = state.TTSQueue.size()
	if(queueSize == 0) {
		state.playingTTS = false
        if(logEnable) log.debug "In letsTalk (${state.version}) - queueSize: ${queueSize} - Finished Speaking"
		return
	}
    def nextTTS = state.TTSQueue[0]
    state.TTSQueue.remove(0)
    // End modified from @djgutheinz
    
	    if(logEnable) log.debug "In letsTalk (${state.version}) - Here we go"
        dayOfTheWeekHandler()
	    checkTime()
	    checkVol()
        if(logEnable) log.debug "In letsTalk - Checking daysMatch: ${state.daysMatch} - timeBetween: ${state.timeBetween}"
        if(state.timeBetween && state.daysMatch) {
		    theMsg = nextTTS[0]
            theDuration = nextTTS[1] * 1000
            state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
    	    if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${theMsg}, volRestore: ${volRestore}"
            state.speakers.each { it ->
                if(logEnable) log.debug "Speaker in use: ${it}"
                if(speakerProxy) {
                    if(logEnable) log.debug "In letsTalk - speakerProxy - ${it}"
                    it.speak(theMsg)
                    alreadyPaused = "no"
                } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                    if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                    def prevVolume = it.currentValue("volume")
                    it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
                    alreadyPaused = "no"
                } else if(it.hasCommand('playTextAndRestore')) {   
                    if(logEnable) log.debug "In letsTalk - playTextAndRestore - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    def prevVolume = it.currentValue("volume")
                    it.playTextAndRestore(theMsg, prevVolume)
                    alreadyPaused = "no"
                } else {		        
                    if(logEnable) log.debug "In letsTalk - ${it}"
                    if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                    if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    it.speak(theMsg)
                    pauseExecution(theDuration)
                    alreadyPaused = "yes"
                    queueSize = state.TTSQueue.size()
                    if(queueSize == 0) {
                        if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(volRestore)
                        if(volRestore && (it.hasCommand('setVolume'))) it.setVolume(volRestore)
                    }
                }
            }
            state.canSpeak = "no"
	        if(logEnable) log.debug "In letsTalk - Finished speaking, checking queue"  
		    log.info "${app.label} - ${theMsg}"
            if(sendPushMessage) pushNow(theMsg)
            if(alreadyPaused == "no") {
                runIn(nextTTS[1], letsTalk) 
            } else {
                runIn(1, letsTalk)
            }
	    } else {
            state.canSpeak = "no"
		    if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
            runIn(nextTTS[1], letsTalk)    // Modified from @djgutheinz
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

def modeHandler(evt) {
	if(logEnable) log.debug "In modeHandler (${state.version})"
	state.modeNow = location.mode
    state.matchFound = false
    
    if(modeName) {
        modeName.each { it ->
            if(logEnable) log.debug "In modeHandler - Checking if ${state.modeNow} contains ${it}"
            if(state.modeNow.contains(it)) {
                state.matchFound = true
			    if(logEnable) log.debug "In modeHandler - Match Found - modeName: ${modeName} - modeNow: ${state.modeNow}"
		    }
        }
        if(state.matchFound) {
            deviceHandler()
        } else {
            if(logEnable) log.debug "In modeHandler - No Match Found"
        }
    }
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
    if(days) {
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
    } else {
        state.daysMatch = true
    }
	if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch}"
}

def thermostatHandler(evt) {
    if(logEnable) log.debug "In thermostatHandler (${state.version})"
    state.thermFound = false
    if(thermostats) {
        thermostats.each { therm ->
            def thermName = therm.displayName
			def thermStatus = therm.currentValue('thermostatOperatingState')
            if(thermStatus != "idle") {
                state.thermFound = true
			    if(logEnable) log.debug "In thermostatHandler - Match Found - thermName: ${thermName} - thermStatus: ${thermStatus}"
            }
		}
        if(state.thermFound) {
            deviceHandler()
        } else {
            if(logEnable) log.debug "In thermostatHandler - No Match Found"
        }
    }
}

def thermostatModeHandler(evt) {
    if(logEnable) log.debug "In thermostatModeHandler (${state.version})"
    state.thermModeFound = false
    if(thermostats) {
        thermostats.each { thermMode ->
            def thermModeName = thermMode.displayName
			def thermModeStatus = thermMode.currentValue('thermostatMode')
            if(thermModeStatus != "off") {
                state.thermModeFound = true
			    if(logEnable) log.debug "In thermostatModeHandler - Match Found - thermModeName: ${thermModeName} - thermMStatus: ${thermModeStatus}"
            }
		}
        if(state.thermModeFound) {
            deviceHandler()
        } else {
            if(logEnable) log.debug "In thermostatModeHandler - No Match Found"
        }
    } 
}


def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    if(state.isData == "yes") {
	    state.theMsg = ""
    
	    if(oRandomPre) {
	    	def values = "${preMsg}".split(";")
	    	vSize = values.size()
		    count = vSize.toInteger()
    	    def randomKey = new Random().nextInt(count)
		    state.preMsgR = values[randomKey]
		    if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, Pre Msg: ${state.preMsgR}"
	    } else {
		    state.preMsgR = "${preMsg}"
		    if(logEnable) log.debug "In messageHandler - Static - Pre Msg: ${state.preMsgR}"
	    }
	
	    if(oRandomPost) {
	    	def values = "${postMsg}".split(";")
	    	vSize = values.size()
		    count = vSize.toInteger()
        	def randomKey = new Random().nextInt(count)
		    state.postMsgR = values[randomKey]
		    if(logEnable) log.debug "In messageHandler - Random - vSize: ${vSize}, randomKey: ${randomKey}, Post Msg: ${state.postMsgR}"
	    } else {
		    state.postMsgR = "${postMsg}"
		    if(logEnable) log.debug "In messageHandler - Static - Post Msg: ${state.postMsgR}"
	    }
	
	    state.theMsg = "${state.preMsgR}, "
    
        if(state.wrongSwitchesMSG) { state.theMsg += " Switches: ${state.wrongSwitchesMSG.substring(0, state.wrongSwitchesMSG.length() - 2)}." }
        if(state.wrongDevicesMSG) { state.theMsg += " Devices: ${state.wrongDevicesMSG.substring(0, state.wrongDevicesMSG.length() - 2)}." }
        if(state.wrongContactsMSG) { state.theMsg += " Contacts: ${state.wrongContactsMSG.substring(0, state.wrongContactsMSG.length() - 2)}." }
        if(state.wrongLocksMSG) { state.theMsg += " Locks: ${state.wrongLocksMSG.substring(0, state.wrongLocksMSG.length() - 2)}." }
    
	    state.theMsg += " ${state.postMsgR}"
	    if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
        
        letsTalkQueue(state.theMsg)
    } else {
		if(logEnable) log.debug "In messageHandler - No message needed"
    }
}

def pushHandler(){
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${state.msg}"
	if(logEnable) log.debug "In pushNow...Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.msg = ""
}

def clearTheQueue() {
    app?.updateSetting("clearQueue",[value:"false",type:"bool"])
    if(logEnable) log.debug "In clearTheQueue (${state.version}) - Resetting the Queue"
    state.TTSQueue = []
	state.playingTTS = false
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    state.canSpeak = "no"
    state.playingTTS = false
	state.TTSQueue = []
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=15 width=15>"
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Check Plus - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
