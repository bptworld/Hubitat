/**
 *  **************** Device Check Plus Child App  ****************
 *
 *  Design Usage:
 *  Check selected devices, then warn you what's not in the right state.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to metion it on the Hubitat forums!  Thanks.
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
 *  V1.0.4 - 12/07/19 - Fixed some minor bugs
 *  V1.0.3 - 11/17/19 - Removed speech queue, now only available with Follow Me!
 *  V1.0.2 - 10/16/19 - More cosmetic changes, added Device Time in State trigger
 *  V1.0.1 - 10/13/19 - Cosmetic changes. 
 *  V1.0.0 - 10/13/19 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "DeviceCheckPlusChildVersion"
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
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Check selected devices, then warn you what's not in the right state."
            paragraph "<b>Examples of Usage:</b>"
            paragraph " - <u>Getting ready to go to bed</u><br> * hit the 'On demand' switch (or use Google to turn it on)<br> * Check will run and announce any problems!<br> * Go to bed knowing everything is secure!"
            paragraph " - <u>Heat is on</u><br> * Someone opens a window or door<br> * Check will run and announce what window is open!"
            paragraph " - <u>Cool is on</u><br> * Someone closes a door<br> * Check will run and announce that the door should be open when cool is on!"
            paragraph " - <u>Other usage...</u><br> * Going out? Make sure all your windows are closed<br> * Is it raining, check the windows!<br> * Think you forgot to do something? This will let you know!"
            paragraph "<b>The only limit is your imagination!</b>"
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Options")) {
            if(onDemandSwitch || days || modeName || thermostats || useTime) {
                href "triggerOptions", title:"${getImage("optionsGreen")} Select Trigger options here", description:"Click here for Options"
            } else {
                href "triggerOptions", title:"${getImage("optionsRed")} Select Trigger options here", description:"Click here for Options"
            }
            
            if(switchesOn || switchesOff || contactsOpen || contactsClosed || locksLocked || locksUnlocked) {
                href "checkConfig", title:"${getImage("optionsGreen")} Select Devices to check", description:"Click here for Options"
            } else {
                href "checkConfig", title:"${getImage("optionsRed")} Select Devices to check", description:"Click here for Options"
            }
            
            if(isDataDevice || preMsg || postMsg) {
                href "notificationOptions", title:"${getImage("optionsGreen")} Select Notification options here", description:"Click here for Options"
            } else {
                href "notificationOptions", title:"${getImage("optionsRed")} Select Notification options here", description:"Click here for Options"
            }
            
            if(speakerMP || speakerSS) {
                href "speechOptions", title:"${getImage("optionsGreen")} Select Speech options here", description:"Click here for Options"
            } else {
                href "speechOptions", title:"${getImage("optionsRed")} Select Speech options here", description:"Click here for Options"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def checkConfig() {
    dynamicPage(name: "checkConfig", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+"  Devices to Check")) {
            paragraph "<b>Select your devices from the options below</b>"
			input "switchesOn", "capability.switch", title: "Switches that should be ON", multiple:true, required:false
			input "switchesOff", "capability.switch", title: "Switches that should be OFF", multiple:true, required:false
			input "contactsOpen", "capability.contactSensor", title: "Contact Sensors that should be OPEN", multiple:true, required:false
			input "contactsClosed", "capability.contactSensor", title: "Contact Sensors that should be CLOSED", multiple:true, required:false
			input "locksLocked", "capability.lock", title: "Door Locks that should be LOCKED", multiple:true, required:false
			input "locksUnlocked", "capability.lock", title: "Door Locks that should be UNLOCKED", multiple:true, required:false
		}
    }
}

def triggerOptions() {
    dynamicPage(name: "triggerOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+"  Trigger Options")) {
            paragraph "This will check if a device is not in the right state (ie. Open when it should be closed)"
            input "useState", "bool", title: "Use Device State as Trigger", description: "use State", submitOnChange:true, defaultValue:false
            
            input "useTime", "bool", title: "Use Device Time in State as Trigger", description: "use Time", submitOnChange:true, defaultValue:false
            paragraph "<hr>"
            if(useState) {
                paragraph "<b>State Triggers</b>"
                paragraph "<b>Run 'Device Check' anytime this switch is turned on.</b> Recommended to create a 'virtual switch' with 'Enable auto off' set to '1s'"
                input "onDemandSwitch", "capability.switch", title: "Check On Demand Switch", required:false
                paragraph "<hr>"
                paragraph "<b>Run 'Device Check' on a set schedule</b> (optional)"
                input "days", "enum", title: "Only run on these days", description: "Days to run", required:false, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                if(days) input "timeToRun", "time", title: "Auto Run at", required:true
                paragraph "<hr>"
                paragraph "<b>Run 'Device Check' on Mode Changes</b> (optional)"
                input "modeName", "mode", title: "When hub changes to this Mode", required:false, multiple:true
                paragraph "<hr>"
                paragraph "<b>Run 'Device Check' on Thermostat Activity</b> (optional)"
                input "thermostats", "capability.thermostat", title: "Thermostat to track", required:false, multiple:true, submitOnChange:true
                if(thermostats) {
                    input "thermOption", "bool", title: "Use Mode or State (off=Mode, on=State)", description: "Therm Options", defaultValue:true, submitOnChange:true
                    paragraph " - <b>Mode</b>: When in heat or cool mode, it will trigger a 'Device Check' anytime a selected device changes state."
                    paragraph " - <b>State</b>: This will trigger a 'Device Check' anytime the thermostat goes into heating or cooling state."
                }
            }
            
            if(useTime) {
                paragraph "<b>Time Triggers</b>"
                input "timeInState", "number", title: "How many minutes should the device be in state before notification", defaultValue: 2, required: true, submitOnChange: true
            }
        }
    }
}

def notificationOptions() {
    dynamicPage(name: "notificationOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+"  Notification Options")) {
            input "isDataDevice", "capability.switch", title: "Turn this device on if there are devices to report", submitOnChange:true, required:false, multiple:false
			paragraph "<hr>"
            paragraph "Receive device notifications with voice and push options. Each of the following messages will only be spoken if necessary."
			
			input "preMsg", "text", title: "Random Pre Message - Separate each message with <b>;</b> (semicolon)",  required:true, submitOnChange:true
			input "oPreList", "bool", defaultValue:false, title: "Show a list view of the random pre messages?", description: "List View", submitOnChange:true
			if(oPreList) {
				def valuesPre = "${preMsg}".split(";")
				listMapPre = ""
    			valuesPre.each { itemPre -> listMapPre += "${itemPre}<br>" }
				paragraph "${listMapPre}"
			}
            paragraph "<b>All switches/contacts/locks in the wrong state will then be spoken</b>"
			input "postMsg", "text", title: "Random Post Message - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange:true
			input "oPostList", "bool", defaultValue:false, title: "Show a list view of the random post messages?", description: "List View", submitOnChange:true
			if(oPostList) {
				def valuesPost = "${postMsg}".split(";")
				listMapPost = ""
    			valuesPost.each { itemPost -> listMapPost += "${itemPost}<br>" }
				paragraph "${listMapPost}"
			}
        }
    }
}

def speechOptions() {
    dynamicPage(name: "speechOptions", title: "", install:false, uninstall:false) {
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
		        paragraph "Speaker proxy in use."
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
    unsubscribe()
	initialize()
}

def initialize() {
    setDefaults()
	if(onDemandSwitch) subscribe(onDemandSwitch, "switch.on", deviceStateHandler)
    if(days) schedule(timeToRun, deviceStateHandler)
    if(modeName) subscribe(location, "mode", modeHandler)
    if(thermostats && thermOption == true) subscribe(thermostats, "thermostatOperatingState.heating", thermostatHandler)
    if(thermostats && thermOption == true) subscribe(thermostats, "thermostatOperatingState.cooling", thermostatHandler) 
    if(thermostats && thermOption == false) subscribe(contactsOpen, "contact", thermostatModeHandler)
    if(thermostats && thermOption == false) subscribe(contactsClosed, "contact", thermostatModeHandler)
    
    if(useTime && contactsOpen) subscribe(contactsOpen, "contact.open", deviceTimeHandler)
    if(useTime && contactsClosed) subscribe(contactsClosed, "contact.closed", deviceTimeHandler)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def deviceStateHandler(evt){
    if(logEnable) log.debug "In deviceStateHandler (${state.version})"
    state.wrongSwitchesMSG = ""
    state.wrongContactsMSG = ""
    state.wrongLocksMSG = ""

// Start Switch
	if(switchesOn || switchesOff) {
		if(switchesOn) {
			switchesOn.each { sOn -> 
				def switchName = sOn.displayName
				def switchStatus = sOn.currentValue('switch')
				if(logEnable) log.debug "In deviceStateHandler - Switch On - ${switchName} - ${switchStatus}"
				if(switchStatus == "off") state.wrongSwitchesMSG += "${switchName}, "
			}
		}
		if(switchesOff) {
			switchesOff.each { sOff -> 
				def switchName = sOff.displayName
				def switchStatus = sOff.currentValue('switch')
				if(logEnable) log.debug "In deviceStateHandler - Switch Off - ${switchName} - ${switchStatus}"
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
				if(logEnable) log.debug "In deviceStateHandler - Contact Open - ${contactName} - ${contactStatus}"
                if(contactStatus == "closed") state.wrongContactsMSG += "${contactName}, "
			}
		}
		if(contactsClosed) {
			contactsClosed.each { cClosed ->
				def contactName = cClosed.displayName
				def contactStatus = cClosed.currentValue('contact')
				if(logEnable) log.debug "In deviceStateHandler - Contact Closed - ${contactName} - ${contactStatus}"
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
				if(logEnable) log.debug "In deviceStateHandler - Locks Unlocked - ${lockName} - ${lockStatus}"
				if(lockStatus == "locked") state.wrongLocksMSG += "${lockName}, "
			}
		}
		if(locksLocked) {
			locksLocked.each { lLocked ->
				def lockName = lLocked.displayName
				def lockStatus = lLocked.currentValue('lock')
				if(logEnable) log.debug "In deviceStateHandler - Locks Locked - ${lockName} - ${lockStatus}"
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

def timeContactHandler(evt){
    if(logEnable) log.debug "In timeContactHandler (${state.version}) - evt: ${evt}"
    beginningValue = evt.currentValue("contact")
    pauseExecution(5000)
    

    
}
    
def checkTimeInState(device) {
    def lastActivity = device.getLastActivity()
}

def letsTalk() {
	    if(logEnable) log.debug "In letsTalk (${state.version}) - Here we go"
        dayOfTheWeekHandler()
	    checkTime()
	    checkVol()
        if(logEnable) log.debug "In letsTalk - Checking daysMatch: ${state.daysMatch} - timeBetween: ${state.timeBetween}"
        if(state.timeBetween && state.daysMatch) {
		    theMsg = state.theMsg
            def duration = Math.max(Math.round(theMsg.length()/12),2)+3
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
		    log.info "${app.label} - ${theMsg}"
            if(sendPushMessage) pushNow(theMsg)
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
            deviceStateHandler()
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
            deviceStateHandler()
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
            deviceStateHandler()
        } else {
            if(logEnable) log.debug "In thermostatModeHandler - No Match Found"
        }
    } 
}


def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    if(state.isData == "yes") {
	    state.theMsg = ""
    
	    def valuesPre = "${preMsg}".split(";")
	    vSizePre = valuesPre.size()
		countPre = vSizePre.toInteger()
    	def randomKeyPre = new Random().nextInt(countPre)
		state.preMsgR = valuesPre[randomKeyPre]
		if(logEnable) log.debug "In messageHandler - Random Pre - vSize: ${vSizePre}, randomKey: ${randomKeyPre}, Pre Msg: ${state.preMsgR}"
	   
	    def valuesPost = "${postMsg}".split(";")
	    vSizePost = valuesPost.size()
		countPost = vSizePost.toInteger()
        def randomKeyPost = new Random().nextInt(countPost)
		state.postMsgR = valuesPost[randomKeyPost]
		if(logEnable) log.debug "In messageHandler - Random Post - vSize: ${vSizePost}, randomKey: ${randomKeyPost}, Msg: ${state.postMsgR}"
	
	    state.theMsg = "${state.preMsgR}, "
    
        if(state.wrongSwitchesMSG) { state.theMsg += " Switches: ${state.wrongSwitchesMSG.substring(0, state.wrongSwitchesMSG.length() - 2)}." }
        if(state.wrongDevicesMSG) { state.theMsg += " Devices: ${state.wrongDevicesMSG.substring(0, state.wrongDevicesMSG.length() - 2)}." }
        if(state.wrongContactsMSG) { state.theMsg += " Contacts: ${state.wrongContactsMSG.substring(0, state.wrongContactsMSG.length() - 2)}." }
        if(state.wrongLocksMSG) { state.theMsg += " Locks: ${state.wrongLocksMSG.substring(0, state.wrongLocksMSG.length() - 2)}." }
    
	    state.theMsg += " ${state.postMsgR}"
	    if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
        
        letsTalk()
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

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
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
    section (getFormat("title", "${getImage("logo")}" + " Device Check Plus - ${theName}")) {
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
