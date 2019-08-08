/**
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Gentle Wake Up
 *
 *  Author: Steve Vlaminck
 *  Date: 2013-03-11
 *
 * 	Gentle Wake Up turns on your lights slowly, allowing you to wake up more
 * 	naturally. Once your lights have reached full brightness, optionally turn on
 * 	more things, or send yourself a text for a more gentle nudge into the waking
 * 	world (you may want to set your normal alarm as a backup plan).
 *
 * ---- End of Original Header ----
 *
 * ---- NEW Header ----
 *
 *  ****************  Gentle Wake Up App  ****************
 *
 *  Design Usage:
 *  Port of the ST Gentle Wake Up App
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
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  v1.0.4 - 08/08/19 - Added optional 'Gradually change the temperature', converted app to partent/child.
 *  v1.0.3 - 08/02/19 - Whoops, found a bug! 
 *  v1.0.2 - 08/01/19 - Added code for 'Repeat X times', gave the app some color!
 *  v1.0.1 - 08/01/19 - More code changes, did some testing...all seems to work!
 *  v1.0.0 - 07/31/19 - Initial port of ST app - Tons of fixes and adjustments to make it compatible with Hubitat...
 *        - Fix date format
 *        - Removed calls for contact info and phonebook
 *        - Fixed push to work with Hubitat
 *        - Fixed speakers to work with Hubitat
 *        - Included my letsTalk rountines to work with just about all speakers
 *        - Made the Message random
 *        - Added switch for logging
 *        - added importUrl
 *        - Fix controllerExplanationPage so it will load, still need to fix the wording
 */

def setVersion() {
	state.version = "v1.0.4"
}

definition(
	name: "Gentle Wake Up Child",
	namespace: "bptWorld",
	author: "Bryan Turcotte",
	description: "Dim your lights up slowly, allowing you to wake up more naturally. Dim by level, color or temperature!",
	category: "port",
    parent: "BPTWorld:Gentle Wake Up",
	iconUrl: "",
	iconX2Url: "",
    pausable: true,
    importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Ported/Gentle%20Wake%20Up/GWU-app.groovy",
)

preferences {
	page(name: "rootPage")
	page(name: "schedulingPage")
	page(name: "completionPage")
	page(name: "numbersPage")
	page(name: "controllerExplanationPage")
	page(name: "unsupportedDevicesPage")
}

def rootPage() {
	dynamicPage(name: "rootPage", title: "", install: true, uninstall: true) {
        section(getFormat("header-green", "${getImage("Blank")}"+" Gentle Wake Up Has A Controller")) {                                       
            href(title: "Learn how to control Gentle Wake Up", page: "controllerExplanationPage", description: null)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" What to dim")) {
			input(name: "dimmers", type: "capability.switchLevel", title: "Dimmers", description: null, multiple: true, required: true, submitOnChange: true)
			if (dimmers) {
				if (dimmersContainUnsupportedDevices()) {
					href(name: "toUnsupportedDevicesPage", page: "unsupportedDevicesPage", title: "Some of your selected dimmers don't seem to be supported", description: "Tap here to fix it", required: true)
				}
				href(name: "toNumbersPage", page: "numbersPage", title: "Duration & Direction", description: numbersPageHrefDescription(), state: "complete")
			}
		}

		if (dimmers) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Rules For Dimming")) {
				href(name: "toSchedulingPage", page: "schedulingPage", title: "Automation", description: schedulingHrefDescription() ?: "Set rules for when to start", state: schedulingHrefDescription() ? "complete" : "")
				input(name: "manualOverride", type: "enum", options: ["cancel": "Cancel dimming", "jumpTo": "Jump to the end"], title: "When one of the dimmers is manually turned off…", description: "dimming will continue", required: false, multiple: false)
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" Completion Actions")) {
				href(name: "toCompletionPage", title: "Completion Actions", page: "completionPage", state: completionHrefDescription() ? "complete" : "", description: completionHrefDescription() ?: "Set rules for what to do when dimming completes")
			}

			section {
				// TODO: fancy label
				label(title: "Label This App", required: false, defaultValue: "", description: "Highly recommended", submitOnChange: true)
                input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
			}
		}
	}
}

def unsupportedDevicesPage() {

	def unsupportedDimmers = dimmers.findAll { !hasSetLevelCommand(it) }

	dynamicPage(name: "unsupportedDevicesPage") {
		if (unsupportedDimmers) {
			section("These devices do not support the setLevel command") {
				unsupportedDimmers.each {
					paragraph deviceLabel(it)
				}
			}
			section {
				input(name: "dimmers", type: "capability.sensor", title: "Please remove the above devices from this list.", submitOnChange: true, multiple: true)
			}
			section {
				paragraph "If you think there is a mistake here, please contact support."
			}
		} else {
			section {
				paragraph "You're all set. You can hit the back button, now. Thanks for cleaning up your settings :)"
			}
		}
	}
}

def controllerExplanationPage() {
	dynamicPage(name: "controllerExplanationPage", title: "How To Control Gentle Wake Up") {

		section("With other Apps", hideable: true, hidden: false) {
			paragraph "When this App is installed, it will create a controller device which you can use in other Apps for even more customizable automation!"
			paragraph "The controller acts like a switch so any App that can control a switch can control Gentle Wake Up, too!"
		}

		section("More about the controller", hideable: true, hidden: true) {
			paragraph "You can find the controller with your other 'Devices'."
			paragraph "You can start and stop Gentle Wake up by tapping the control on the right."
			paragraph "If you look at the device details screen, you will find even more information about Gentle Wake Up and more fine grain controls."
			paragraph "The slider allows you to jump to any point in the dimming process. Think of it as a percentage. If Gentle Wake Up is set to dim down as you fall asleep, but your book is just too good to put down; simply drag the slider to the left and Gentle Wake Up will give you more time to finish your chapter and drift off to sleep."
			paragraph "In the lower left, you will see the amount of time remaining in the dimming cycle. It does not count down evenly. Instead, it will update whenever the slider is updated; typically every 6-18 seconds depending on the duration of your dimming cycle."
			paragraph "Of course, you may also tap the middle to start or stop the dimming cycle at any time."
		}

		section("Starting and stopping the App itself", hideable: true, hidden: true) {
			paragraph "Tap the 'play' button on the App to start or stop dimming."
		}

		section("Turning off devices while dimming", hideable: true, hidden: true) {
			paragraph "It's best to use other Devices and Apps for triggering the Controller device. However, that isn't always an option."
			paragraph "If you turn off a switch that is being dimmed, it will either continue to dim, stop dimming, or jump to the end of the dimming cycle depending on your settings."
			paragraph "Unfortunately, some switches take a little time to turn off and may not finish turning off before Gentle Wake Up sets its dim level again. You may need to try a few times to get it to stop."
			paragraph "That's why it's best to use devices that aren't currently dimming. Remember that you can use other Apps to toggle the controller. :)"
		}
	}
}

def numbersPage() {
	dynamicPage(name:"numbersPage", title:"") {

		section {
			paragraph(name: "pGraph", title: "These lights will dim", fancyDeviceString(dimmers))
		}

		section {
			input(name: "duration", type: "number", title: "For this many minutes", description: "30", required: false, defaultValue: 30)
		}

		section {
			input(name: "startTempLevel", type: "number", range: "0..99", title: "From this level", defaultValue: defaultStart(), description: "Current Level", required: false, multiple: false, width: 6)
			input(name: "endTempLevel", type: "number", range: "0..99", title: "To this level", defaultValue: defaultEnd(), description: "Between 0 and 99", required: true, multiple: false, width: 6)
		}

		def colorDimmers = dimmersWithSetColorCommand()
		if (colorDimmers) {
			section {
				input(name: "colorize", type: "bool", title: "Gradually change the color of ${fancyDeviceString(colorDimmers)}", description: null, required: false, defaultValue: "false")
			}
		}
        
        def colorTemperatureDimmers = dimmersWithSetColorTemperatureCommand()
		if (colorTemperatureDimmers) {
			section {
				input(name: "colorTemperature", type: "bool", title: "Gradually change the temperature of ${fancyDeviceString(colorTemperatureDimmers)}", description: null, required: false, defaultValue: "false", submitOnChange: true)
                if(colorTemperature) input(name: "startTemp", type: "number", range: "1000..5000", title: "From this temp", description: "Between 1000 and 5000", required: false, multiple: false, width: 6)
			if(colorTemperature) input(name: "endTemp", type: "number", range: "1000..5000", title: "To this temp", description: "Between 1000 and 5000", required: true, multiple: false, width: 6)
			}
		}
	}
}

def defaultStart() {
	if (usesOldSettings() && direction && direction == "Down") {
		return 99
	}
	return 0
}

def defaultEnd() {
	if (usesOldSettings() && direction && direction == "Down") {
		return 0
	}
	return 99
}

def startLevelLabel() {
	if (usesOldSettings()) { // using old settings
		if (direction && direction == "Down") { // 99 -> 1
			return "99%"
		}
		return "0%"
	}
	return hasStartLevel() ? "${startLevel}%" : "Current Level"
}

def endLevelLabel() {
	if (usesOldSettings()) {
		if (direction && direction == "Down") { // 99 -> 1
			return "0%"
		}
		return "99%"
	}
	return "${endLevel}%"
}

def weekdays() {
	["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
}

def weekends() {
	["Saturday", "Sunday"]
}

def schedulingPage() {
	dynamicPage(name: "schedulingPage", title: "Rules For Automatically Dimming Your Lights") {

		section(getFormat("header-green", "${getImage("Blank")}"+" Use Other Apps!")) {
			href(title: "Learn how to control Gentle Wake Up", page: "controllerExplanationPage", description: null)
		}

		section(getFormat("header-green", "${getImage("Blank")}"+" Allow Automatic Dimming")) {
			input(name: "days", type: "enum", title: "On These Days", description: "Every day", required: false, multiple: true, options: weekdays() + weekends())
		}

		section(getFormat("header-green", "${getImage("Blank")}"+" Start Dimming...")) {
			input(name: "startTime", type: "time", title: "At This Time", description: null, required: false)
			input(name: "modeStart", title: "When Entering This Mode", type: "mode", required: false, mutliple: false, submitOnChange: true, description: null)
			if (modeStart) {
				input(name: "modeStop", title: "Stop when leaving '${modeStart}' mode", type: "bool", required: false)
			}
		}

	}
}

def completionPage() {
	dynamicPage(name: "completionPage", title: "Completion Rules") {

		section(getFormat("header-green", "${getImage("Blank")}"+" Switches")) {
			input(name: "completionSwitches", type: "capability.switch", title: "Set these switches", description: null, required: false, multiple: true, submitOnChange: true)
			if (completionSwitches) {
				input(name: "completionSwitchesState", type: "enum", title: "To", description: null, required: false, multiple: false, options: ["on", "off"], defaultValue: "on")
				input(name: "completionSwitchesLevel", type: "number", title: "Optionally, Set Dimmer Levels To", description: null, required: false, multiple: false, range: "(0..99)")
			}
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Options")) { 
            input "completionPush", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
            paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
            input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required: false, multiple: true, submitOnChange: true
            input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required: false, multiple: true, submitOnChange: true
            input(name: "speakerProxy", type: "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy")
        }
        if(speakerMP || speakerSS) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		        paragraph "NOTE: Not all speakers can use volume controls."
                paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required: true, width: 6
		        input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required: true, width: 6
                input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required: false, submitOnChange: true
		        if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required: true, width: 6
    	    	if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required: true, width: 6
            }
		}
        if(completionPush || speakerMP || speakerSS) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Message")) {
                input "completionMessage", "text", title: "Random Message to be spoken or pushed - Separate each message with <b>;</b> (semicolon)",  required: true, submitOnChange: true
	            input(name: "msgList", type: "bool", defaultValue: "false", title: "Show a list view of the messages?", description: "List View", submitOnChange: "true")
			    if(msgList) {
				    def values = "${completionMessage}".split(";")
			    	listMap = ""
    		    	values.each { item -> listMap += "${item}<br>"}
			    	paragraph "${listMap}"
			    }
                input(name: "oRepeat", type: "bool", defaultValue: "false", title: "<b>Repeat Message?</b>", description: "Repeat Message", submitOnChange: "true")
				if(oRepeat) {
					paragraph "Repeat message every X seconds until 'Controller Switch' is turned off OR max number of repeats is reached."
					input "repeatSeconds", "number", title: "Repeat message every X seconds (1 to 600 seconds - 300=5 min, 600=10 min)", required: true, defaultValue:10, range: '1..600', submitOnChange: true
					input "maxRepeats", "number", title: "Max number of repeats (1 to 100)", required: true, defaultValue:99, range: '1..100', submitOnChange: "true"
					if(repeatSeconds) {
						paragraph "Message will repeat every ${repeatSeconds} seconds until the Control Switch is turned off <b>OR</b> the Max number of repeats is reached (${maxRepeats})"
						state.repeatTimeSeconds = (repeatSeconds * maxRepeats)
						int inputNow = state.repeatTimeSeconds
						int nDayNow = inputNow / 86400
						int nHrsNow = (inputNow % 86400 ) / 3600
						int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
						int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
						paragraph "In this case, it would take ${nHrsNow} Hours, ${nMinNow} Mins and ${nSecNow} Seconds to reach the max number of repeats (if Controller Switch is not turned off)"
					}
				}
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
        		input "fromTime", "time", title: "From", required: false
        		input "toTime", "time", title: "To", required: false
			}
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Modes")) {
			input(name: "completionMode", type: "mode", title: "Change ${location.name} Mode To", description: null, required: false)
		}

		section(getFormat("header-green", "${getImage("Blank")}"+" Delay")) {
			input(name: "completionDelay", type: "number", title: "Delay This Many Minutes Before Executing These Actions", description: "0", required: false)
		}
	}
}

// ========================================================
// Handlers
// ========================================================

def installed() {
	if(logEnable) log.debug "Installing 'Gentle Wake Up' with settings: ${settings}"

	initialize()
}

def updated() {
	if(logEnable) log.debug "Updating 'Gentle Wake Up' with settings: ${settings}"
	unschedule()

	def controller = getController()
	if (controller) {
		controller.label = app.label
	}

	initialize()
}

private initialize() {
	stop("settingsChange")

	if (startTime) {
		if(logEnable) log.debug "scheduling dimming routine to run at $startTime"
		schedule(startTime, "scheduledStart")
	}

	// TODO: make this an option
	subscribe(app, appHandler)

	subscribe(location, locationHandler)

	if (manualOverride) {
		subscribe(dimmers, "switch.off", stopDimmersHandler)
	}

	if (!getAllChildDevices()) {
		// create controller device and set name to the label used here
		def dni = "${new Date().getTime()}"
        if(logEnable) log.debug "label: ${app.label} - dni: ${dni}"
		addChildDevice("smartthings", "Gentle Wake Up Controller", dni, null, ["label": app.label])
		state.controllerDni = dni
	}
}

def appHandler(evt) {
	if(logEnable) log.debug "appHandler evt: ${evt.value}"
	if (evt.value == "touch") {
		if (atomicState.running) {
			stop("appTouch")
		} else {
			start("appTouch")
		}
	}
}

def locationHandler(evt) {
	if(logEnable) log.debug "locationHandler evt: ${evt.value}"

	if (!modeStart) {
		return
	}

	def isSpecifiedMode = (evt.value == modeStart)
	def modeStopIsTrue = (modeStop && modeStop != "false")

	if (isSpecifiedMode && canStartAutomatically()) {
		start("modeChange")
	} else if (!isSpecifiedMode && modeStopIsTrue) {
		stop("modeChange")
	}

}

def stopDimmersHandler(evt) {
	if(logEnable) log.debug "stopDimmersHandler evt: ${evt.value}"
	def percentComplete = completionPercentage()
	// Often times, the first thing we do is turn lights on or off so make sure we don't stop as soon as we start
	if (percentComplete > 2 && percentComplete < 98) {
		if (manualOverride == "cancel") {
			if(logEnable) log.debug "STOPPING in stopDimmersHandler"
			stop("manualOverride")
		} else if (manualOverride == "jumpTo") {
			def end = dynamicEndLevel()
			if(logEnable) log.debug "Jumping to 99% complete in stopDimmersHandler"
			jumpTo(99)
		}

	} else {
		if(logEnable) log.debug "not stopping in stopDimmersHandler"
	}
}

// ========================================================
// Scheduling
// ========================================================

def scheduledStart() {
	if (canStartAutomatically()) {
		start("schedule")
	}
}

public def start(source) {
	if(logEnable) log.debug "START"

	sendStartEvent(source)

	setLevelsInState()

	atomicState.running = true
	atomicState.runCounter = 0

	atomicState.start = new Date().getTime()

	schedule("0 * * * * ?", "healthCheck")
	increment()
}

public def stop(source) {
	if(logEnable) log.debug "STOP"

	sendStopEvent(source)

	atomicState.running = false
	atomicState.start = 0
	atomicState.runCounter = 0

	unschedule("healthCheck")
}

private healthCheck() {
	if(logEnable) log.debug "'Gentle Wake Up' healthCheck"

	if (!atomicState.running) {
		return
	}

	increment()
}

// ========================================================
// Controller
// ========================================================

def sendStartEvent(source) {
	if(logEnable) log.debug "sendStartEvent(${source})"
	def eventData = [
			name: "sessionStatus",
			value: "running",
			descriptionText: "${app.label} has started dimming",
			displayed: true,
			linkText: app.label,
			isStateChange: true
	]
	if (source == "modeChange") {
		eventData.descriptionText += " because of a mode change"
	} else if (source == "schedule") {
		eventData.descriptionText += " as scheduled"
	} else if (source == "appTouch") {
		eventData.descriptionText += " because you pressed play on the app"
	} else if (source == "controller") {
		eventData.descriptionText += " because you pressed play on the controller"
	}

	sendControllerEvent(eventData)
}

def sendStopEvent(source) {
	if(logEnable) log.debug "sendStopEvent(${source})"
	def eventData = [
			name: "sessionStatus",
			value: "stopped",
			descriptionText: "${app.label} has stopped dimming",
			displayed: true,
			linkText: app.label,
			isStateChange: true
	]
	if (source == "modeChange") {
		eventData.descriptionText += " because of a mode change"
		eventData.value += "cancelled"
	} else if (source == "schedule") {
		eventData.descriptionText = "${app.label} has finished dimming"
	} else if (source == "appTouch") {
		eventData.descriptionText += " because you pressed play on the app"
		eventData.value += "cancelled"
	} else if (source == "controller") {
		eventData.descriptionText += " because you pressed stop on the controller"
		eventData.value += "cancelled"
	} else if (source == "settingsChange") {
		eventData.descriptionText += " because the settings have changed"
		eventData.value += "cancelled"
	} else if (source == "manualOverride") {
		eventData.descriptionText += " because the dimmer was manually turned off"
		eventData.value += "cancelled"
    } else if (source == "maxRepeats") {
		eventData.descriptionText += " because the max number of repeats was reached"
		eventData.value += "cancelled"
	}

	// send 100% completion event
	sendTimeRemainingEvent(100)

	// send a non-displayed 0% completion to reset tiles
	sendTimeRemainingEvent(0, false)

	// send sessionStatus event last so the event feed is ordered properly
	sendControllerEvent(eventData)
}

def sendTimeRemainingEvent(percentComplete, displayed = true) {
	if(logEnable) log.debug "sendTimeRemainingEvent(${percentComplete})"

	def percentCompleteEventData = [
			name: "percentComplete",
			value: percentComplete as int,
			displayed: displayed,
			isStateChange: true
	]
	sendControllerEvent(percentCompleteEventData)

	def duration = sanitizeInt(duration, 30)
	def timeRemaining = duration - (duration * (percentComplete / 100))
	def timeRemainingEventData = [
			name: "timeRemaining",
			value: displayableTime(timeRemaining),
			displayed: displayed,
			isStateChange: true
	]
	sendControllerEvent(timeRemainingEventData)
}

def sendControllerEvent(eventData) {
	def controller = getController()
	if (controller) {
		controller.controllerEvent(eventData)
	}
}

def getController() {
	def dni = state.controllerDni
	if (!dni) {
		log.warn "no controller dni"
		return null
	}
	def controller = getChildDevice(dni)
	if (!controller) {
		log.warn "no controller"
		return null
	}
	//if(logEnable) log.debug "controller: ${controller}"
	return controller
}

// ========================================================
// Setting levels
// ========================================================


private increment() {

	if (!atomicState.running) {
		return
	}

	if (atomicState.runCounter == null) {
		atomicState.runCounter = 1
	} else {
		atomicState.runCounter = atomicState.runCounter + 1
	}
	def percentComplete = completionPercentage()

	if (percentComplete > 99) {
		percentComplete = 99
	}

	if (atomicState.runCounter > 100) {
		log.error "Force stopping Gentle Wakeup due to too many increments"
		// If increment has already been called 100 times, then stop regardless of state
		percentComplete = 100
	} else {
		updateDimmers(percentComplete)
	}
	if (percentComplete < 99) {

		def runAgain = stepDuration()
		if(logEnable) log.debug "Rescheduling to run again in ${runAgain} seconds"

		runIn(runAgain, 'increment', [overwrite: true])

	} else {

		int completionDelay = completionDelaySeconds()
		if (completionDelay) {
			if(logEnable) log.debug "Finished with steps. Scheduling completion for ${completionDelay} second(s) from now"
			runIn(completionDelay, 'completion', [overwrite: true])
			unschedule("healthCheck")
			// don't let the health check start incrementing again while we wait for the delayed execution of completion
		} else {
			if(logEnable) log.debug "Finished with steps. Execution completion"
			completion()
		}
	}
}

def setupTemps() {
    if(state.runSetupTempsOnce == "no") {
        state.runSetupTempsOnce = "yes"
        state.currentTemp = startTemp
        seconds = duration * 60
        state.dimStep = endTemp / seconds
        state.dimTemp = state.currentTemp
        
        if(startTemp < endTemp) state.tempUpDown = "Up"
        if(startTemp > endTemp) state.tempUpDown = "Down"
        if(logEnable) log.debug "In setupTemps - tempUpDown: ${state.tempUpDown}"
    }
}

def updateDimmers(percentComplete) {
    setupTemps()
    
	dimmers.each { dimmer ->
		def nextLevel = dynamicLevel(dimmer, percentComplete)

		if (nextLevel == 0) {
            dimmer.off()
		} else {
			def shouldChangeColors = (colorize && colorize != "false")
            def shouldChangeColorTemperature = (colorTemperature && colorTemperature != "false")
			if (shouldChangeColors && hasSetColorCommand(dimmer)) {
				def hue = getHue(dimmer, nextLevel)
				if(logEnable) log.debug "Setting ${deviceLabel(dimmer)} level to ${nextLevel} and hue to ${hue}"
				dimmer.setColor([hue: hue, saturation: 100, level: nextLevel])
            } else if (shouldChangeColorTemperature && hasSetColorTemperatureCommand(dimmer)) {                   
                dimmer.setLevel(nextLevel)
                if(state.tempUpDown== "Up") colorTempStepUp(dimmer)
                if(state.tempUpDown== "Down") colorTempStepDown(dimmer)
                if(logEnable) log.debug "Setting ${deviceLabel(dimmer)} level to ${nextLevel} and temperatue to ${state.currentTemp}"
			} else if (hasSetLevelCommand(dimmer)) {
				if(logEnable) log.debug "Setting ${deviceLabel(dimmer)} level to ${nextLevel}"
				dimmer.setLevel(nextLevel)
			} else {
				log.warn "${deviceLabel(dimmer)} does not have setColor, setColorTemperature or setLevel commands."
			}
		}
	}
	sendTimeRemainingEvent(percentComplete)
}

int dynamicLevel(dimmer, percentComplete) {
	def start = atomicState.startLevels[dimmer.id]
	def end = dynamicEndLevel()

	if (!percentComplete) {
		return start
	}

	def totalDiff = end - start
	def actualPercentage = percentComplete / 100
	def percentOfTotalDiff = totalDiff * actualPercentage

	(start + percentOfTotalDiff) as int
}

// ========================================================
// Completion
// ========================================================

private completion() {
	if(logEnable) log.debug "Starting completion block"

	if (!atomicState.running) {
		return
	}

	stop("schedule")
	handleCompletionSwitches()
	handleCompletionMessaging()
	handleCompletionModesAndPhrases()   
    state.runSetupTempsOnce = "no"
}

private handleCompletionSwitches() {
	completionSwitches.each { completionSwitch ->

		def isDimmer = hasSetLevelCommand(completionSwitch)

		if (completionSwitchesLevel && isDimmer) {
			completionSwitch.setLevel(completionSwitchesLevel)
		} else {
			def command = completionSwitchesState ?: "on"
			completionSwitch."${command}"()
		}
	}
}

private handleCompletionMessaging() {
    if (completionMessage) {
        messageHandler()
		if (completionPush) {
            completionPush.deviceNotification(state.theMessage)
		}
		if (speakerMP || speakerSS) {
			letsTalk()
		}
	}
}

private handleCompletionModesAndPhrases() {
	if (completionMode) {
		setLocationMode(completionMode)
	}
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
        speechDuration = Math.max(Math.round(state.theMsg.length()/12),2)+3		// Code from @djgutheinz
        atomicState.speechDuration2 = speechDuration * 1000
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.debug "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${state.theMsg}, volRestore: ${volRestore}"
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
	    if(logEnable) log.debug "In letsTalk - that's it!"  
		log.info "${app.label} - ${state.theMsg}"
        if(oRepeat) messageRepeatHandler()
	} else {
		if(logEnable) log.debug "In letsTalk - Messages not allowed at this time"
	}
}

def messageRepeatHandler() {
    if(state.numRepeats == null) state.numRepeats = 1
    def controller = getController()
    controllerSwitch = controller.currentValue("switch")
    if(logEnable) log.debug "In messageRepeatHandler - controllerSwitch: ${controllerSwitch} - numRepeats so far: ${state.numRepeats}"
    if(controllerSwitch == "on") {
		if(state.numRepeats <= maxRepeats) {
			state.numRepeats = state.numRepeats + 1
            messageHandler()
			runIn(repeatSeconds,letsTalk)
		} else {
            log.info "${app.label} - Max repeats (${maxRepeats}) has been reached."
            stop("maxRepeats")
            controller.off()
            state.numRepeats = 1
		}
	} else {
		log.info "${app.label} - Set to repeat but Controller Switch is Off."
        state.numRepeats = 1
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
	message = completionMessage
    
	def values = "${message}".split(";")
	vSize = values.size()
	count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMessage = values[randomKey]
    
    state.theMessage = theMessage
}

// ========================================================
// Helpers
// ========================================================

def setLevelsInState() {
	def startLevels = [:]
	dimmers.each { dimmer ->
		if (usesOldSettings()) {
			startLevels[dimmer.id] = defaultStart()
		} else if (hasStartLevel()) {
			startLevels[dimmer.id] = startLevel
		} else {
			def dimmerIsOff = dimmer.currentValue("switch") == "off"
			startLevels[dimmer.id] = dimmerIsOff ? 0 : dimmer.currentValue("level")
		}
	}

	atomicState.startLevels = startLevels
}

def canStartAutomatically() {

	def today = new Date().format("EEEE")
	if(logEnable) log.debug "today: ${today}, days: ${days}"

	if (!days || days.contains(today)) {// if no days, assume every day
		return true
	}

	if(logEnable) log.debug "should not run"
	return false
}

def completionPercentage() {
	if(logEnable) log.debug "checkingTime"

	if (!atomicState.running) {
		return
	}

	def now = new Date().getTime()
	def timeElapsed = now - atomicState.start
	def totalRunTime = totalRunTimeMillis() ?: 1
	def percentComplete = timeElapsed / totalRunTime * 100
	if(logEnable) log.debug "percentComplete: ${percentComplete}"

	return percentComplete
}

int totalRunTimeMillis() {
	int minutes = sanitizeInt(duration, 30)
	convertToMillis(minutes)
}

int convertToMillis(minutes) {
	def seconds = minutes * 60
	def millis = seconds * 1000
	return millis
}

def timeRemaining(percentComplete) {
	def normalizedPercentComplete = percentComplete / 100
	def duration = sanitizeInt(duration, 30)
	def timeElapsed = duration * normalizedPercentComplete
	def timeRemaining = duration - timeElapsed
	return timeRemaining
}

int millisToEnd(percentComplete) {
	convertToMillis(timeRemaining(percentComplete))
}

String displayableTime(timeRemaining) {
	def timeString = "${timeRemaining}"
	def parts = timeString.split(/\./)
	if (!parts.size()) {
		return "0:00"
	}
	def minutes = parts[0]
	if (parts.size() == 1) {
		return "${minutes}:00"
	}
	def fraction = "0.${parts[1]}" as double
	def seconds = "${60 * fraction as int}".padLeft(2, "0")
	return "${minutes}:${seconds}"
}

def jumpTo(percentComplete) {
	def millisToEnd = millisToEnd(percentComplete)
	def endTime = new Date().getTime() + millisToEnd
	def duration = sanitizeInt(duration, 30)
	def durationMillis = convertToMillis(duration)
	def shiftedStart = endTime - durationMillis
	atomicState.start = shiftedStart
	updateDimmers(percentComplete)
	sendTimeRemainingEvent(percentComplete)
}

int dynamicEndLevel() {
	if (usesOldSettings()) {
		if (direction && direction == "Down") {
			return 0
		}
		return 99
	}
	return endLevel as int
}

def getHue(dimmer, level) {
	def start = atomicState.startLevels[dimmer.id] as int
	def end = dynamicEndLevel()
	if (start > end) {
		return getDownHue(level)
	} else {
		return getUpHue(level)
	}
}

def getUpHue(level) {
	getBlueHue(level)
}

def getDownHue(level) {
	getRedHue(level)
}

def colorTempStepUp(dimmer) {
	if(logEnable) log.debug "In colorTempStepUp"			
    if(state.currentTemp < endTemp) {
        if(logEnable) log.debug "colorTempStepUp - dimTemp: ${state.dimTemp} - dimStep: ${state.dimStep}"
        state.dimTemp = state.dimTemp + state.dimStep
        if(logEnable) log.debug "colorTempStepUp - NEW dimTemp: ${state.dimTemp}"
        if(state.dimTemp > endTemp) {state.dimTemp = endTemp}
        state.currentTemp = state.dimTemp.toInteger()
        if(logEnable) log.debug "colorTempStepUp - NEW currentTemp: ${state.currentTemp}"
    	dimmer.setColorTemperature(state.currentTemp)
        if(logEnable) log.debug "colorTempStepUp - Current Temp: ${state.currentTemp} - dimStep: ${state.dimStep} - targetTemp: ${endTemp}"
    } else{
        if(logEnable) log.debug "colorTempStepUp - FINISHED - Current Temp: ${state.currentTemp} - dimStep: ${state.dimStep} - targetTemp: ${endTemp}"    
    }
}

def colorTempStepDown(dimmer) {
	if(logEnable) log.debug "In colorTempStepDown"			
    if(state.currentTemp > endTemp) {
        if(logEnable) log.debug "colorTempStepDown - dimTemp: ${state.dimTemp} - dimStep: ${state.dimStep}"
        state.dimTemp = state.dimTemp + state.dimStep
        if(logEnable) log.debug "colorTempStepDown - NEW dimTemp: ${state.dimTemp}"
        if(state.dimTemp > endTemp) {state.dimTemp = endTemp}
        state.currentTemp = state.dimTemp.toInteger()
        if(logEnable) log.debug "colorTempStepDown - NEW currentTemp: ${state.currentTemp}"
    	dimmer.setColorTemperature(state.currentTemp)
        if(logEnable) log.debug "colorTempStepDown - Current Temp: ${state.currentTemp} - dimStep: ${state.dimStep} - targetTemp: ${endTemp}"
    } else{
        if(logEnable) log.debug "colorTempStepDown - FINISHED - Current Temp: ${state.currentTemp} - dimStep: ${state.dimStep} - targetTemp: ${endTemp}"    
    }
}

private getBlueHue(level) {
	if (level < 5) return 72
	if (level < 10) return 71
	if (level < 15) return 70
	if (level < 20) return 69
	if (level < 25) return 68
	if (level < 30) return 67
	if (level < 35) return 66
	if (level < 40) return 65
	if (level < 45) return 64
	if (level < 50) return 63
	if (level < 55) return 62
	if (level < 60) return 61
	if (level < 65) return 60
	if (level < 70) return 59
	if (level < 75) return 58
	if (level < 80) return 57
	if (level < 85) return 56
	if (level < 90) return 55
	if (level < 95) return 54
	if (level >= 95) return 53
}

private getRedHue(level) {
	if (level < 6) return 1
	if (level < 12) return 2
	if (level < 18) return 3
	if (level < 24) return 4
	if (level < 30) return 5
	if (level < 36) return 6
	if (level < 42) return 7
	if (level < 48) return 8
	if (level < 54) return 9
	if (level < 60) return 10
	if (level < 66) return 11
	if (level < 72) return 12
	if (level < 78) return 13
	if (level < 84) return 14
	if (level < 90) return 15
	if (level < 96) return 16
	if (level >= 96) return 17
}

private dimmersContainUnsupportedDevices() {
	def found = dimmers.find { hasSetLevelCommand(it) == false }
	return found != null
}

private hasSetLevelCommand(device) {
	return hasCommand(device, "setLevel")
}

private hasSetColorCommand(device) {
	return hasCommand(device, "setColor")
}

private hasSetColorTemperatureCommand(device) {
	return hasCommand(device, "setColorTemperature")
}

private hasCommand(device, String command) {
	return (device.supportedCommands.find { it.name == command } != null)
}

private dimmersWithSetColorCommand() {
	def colorDimmers = []
	dimmers.each { dimmer ->
		if (hasSetColorCommand(dimmer)) {
			colorDimmers << dimmer
		}
	}
	return colorDimmers
}

private dimmersWithSetColorTemperatureCommand() {
	def temperatureDimmers = []
	dimmers.each { dimmer ->
		if (hasSetColorTemperatureCommand(dimmer)) {
			temperatureDimmers << dimmer
		}
	}
	return temperatureDimmers
}

private int sanitizeInt(i, int defaultValue = 0) {
	try {
		if (!i) {
			return defaultValue
		} else {
			return i as int
		}
	}
	catch (Exception e) {
		log.warn e
		return defaultValue
	}
}

private completionDelaySeconds() {
	int completionDelayMinutes = sanitizeInt(completionDelay)
	int completionDelaySeconds = (completionDelayMinutes * 60)
	return completionDelaySeconds ?: 0
}

private stepDuration() {
	int minutes = sanitizeInt(duration, 30)
	int stepDuration = (minutes * 60) / 100
	return stepDuration ?: 1
}

private debug(message) {
	//log.debug "${message}\nstate: ${state}"
}

public hubitatDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public humanReadableStartDate() {
    new Date().parse(hubitatDateFormat(), "${startTime}".replace("+00:00","+0000")).format("h:mm a")
}

def fancyString(listOfStrings) {
	def fancify = { list ->
		return list.collect {
			def label = it
			if (list.size() > 1 && it == list[-1]) {
				label = "and ${label}"
			}
			label
		}.join(", ")
	}
	return fancify(listOfStrings)
}

def fancyDeviceString(devices = []) {
	fancyString(devices.collect { deviceLabel(it) })
}

def deviceLabel(device) {
	return device.label ?: device.name
}

def schedulingHrefDescription() {

	def descriptionParts = []
	if (days) {
		if (days == weekdays()) {
			descriptionParts << "On weekdays,"
		} else if (days == weekends()) {
			descriptionParts << "On weekends,"
		} else {
			descriptionParts << "On ${fancyString(days)},"
		}
	}

	descriptionParts << "${fancyDeviceString(dimmers)} will start dimming"

	if (startTime) {
		descriptionParts << "at ${humanReadableStartDate()}"
	}

	if (modeStart) {
		if (startTime) {
			descriptionParts << "or"
		}
		descriptionParts << "when ${location.name} enters '${modeStart}' mode"
	}

	if (descriptionParts.size() <= 1) {
		// dimmers will be in the list no matter what. No rules are set if only dimmers are in the list
		return null
	}

	return descriptionParts.join(" ")
}

def completionHrefDescription() {

	def descriptionParts = []
	def example = "Switch1 will be turned on. Switch2, Switch3, and Switch4 will be dimmed to 50%. The message '<message>' will be spoken and sent as a push notification. The mode will be changed to '<mode>'."

	if (completionSwitches) {
		def switchesList = []
		def dimmersList = []


		completionSwitches.each {
			def isDimmer = completionSwitchesLevel ? hasSetLevelCommand(it) : false

			if (isDimmer) {
				dimmersList << deviceLabel(it)
			}

			if (!isDimmer) {
				switchesList << deviceLabel(it)
			}
		}


		if (switchesList) {
			descriptionParts << "${fancyString(switchesList)} will be turned ${completionSwitchesState ?: 'on'}."
		}

		if (dimmersList) {
			descriptionParts << "${fancyString(dimmersList)} will be dimmed to ${completionSwitchesLevel}%."
		}

	}

	if (completionMessage && (completionPush || speakerMP || speakerSS)) {
		def messageParts = []

		if (speakerMP || speakerSS) {
			messageParts << "spoken"
		}
		if (completionPush) {
			messageParts << "sent as a push notification"
		}

		descriptionParts << "A random message (from your custom list) will be ${fancyString(messageParts)}."
	}

	if (completionMode) {
		descriptionParts << "The mode will be changed to '${completionMode}'."
	}

	return descriptionParts.join(" ")
}

def numbersPageHrefDescription() {
	def title = "All dimmers will dim for ${duration ?: '30'} minutes from ${startLevelLabel()} to ${endLevelLabel()}"
	if (colorize) {
		def colorDimmers = dimmersWithSetColorCommand()
		if (colorDimmers == dimmers) {
			title += " and will gradually change color."
		} else {
			title += ".\n${fancyDeviceString(colorDimmers)} will gradually change color."
		}
	}
    if (temperature) {
		def colorTemperatureDimmers = dimmersWithSetColorTemperatureCommand()
		if (colorTemperatureDimmers == dimmers) {
			title += " and will gradually change temperature."
		} else {
			title += ".\n${fancyDeviceString(colorTemperatureDimmers)} will gradually change temperature."
		}
	}
	return title
}

def hueSatToHex(h, s) {
	def convertedRGB = hslToRgb(h, s, 0.5)
	return rgbToHex(convertedRGB)
}

def hslToRgb(h, s, l) {
	def r, g, b;

	if (s == 0) {
		r = g = b = l; // achromatic
	} else {
		def hue2rgb = { p, q, t ->
			if (t < 0) t += 1;
			if (t > 1) t -= 1;
			if (t < 1 / 6) return p + (q - p) * 6 * t;
			if (t < 1 / 2) return q;
			if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
			return p;
		}

		def q = l < 0.5 ? l * (1 + s) : l + s - l * s;
		def p = 2 * l - q;

		r = hue2rgb(p, q, h + 1 / 3);
		g = hue2rgb(p, q, h);
		b = hue2rgb(p, q, h - 1 / 3);
	}

	return [r * 255, g * 255, b * 255];
}

def rgbToHex(red, green, blue) {
	def toHex = {
		int n = it as int;
		n = Math.max(0, Math.min(n, 255));
		def hexOptions = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"]

		def firstDecimal = ((n - n % 16) / 16) as int
		def secondDecimal = (n % 16) as int

		return "${hexOptions[firstDecimal]}${hexOptions[secondDecimal]}"
	}

	def rgbToHex = { r, g, b ->
		return toHex(r) + toHex(g) + toHex(b)
	}

	return rgbToHex(red, green, blue)
}

def usesOldSettings() {
	!hasEndLevel()
}

def hasStartLevel() {
	return (startLevel != null && startLevel != "")
}

def hasEndLevel() {
	return (endLevel != null && endLevel != "")
}

// ********** Normal Stuff **********

def getImage(type) {							// Modified Code from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){					// Modified Code from @Stephack
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Gentle Wake Up Port - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${state.version}</div>"
	}       
}
