/**
 *  ****************  The Flasher Child App  ****************
 *
 *  Design Usage:
 *  Flash your lights based on several triggers!
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
 *  Based on The Flasher - Bob - 2013-02-06 - Smartthings.  Thank you!
 *
 *  Changes:
 *
 *  V1.0.0 - 01/01/20 - Initial release
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "TheFlasherChildVersion"
	state.version = "v1.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { //log.error "In setVersion - ${e}"
    }
}

definition(
    name: "The Flasher Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Flash your lights based on several triggers!",
    category: "",
	parent: "BPTWorld:The Flasher",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/The%20Flasher/TF-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
            paragraph "Flash your lights based on several triggers!"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Trigger Options")) {
            input "acceleration", "capability.accelerationSensor", title: "Acceleration Sensor(s)", required: false, multiple: true, submitOnChange: true
            if(acceleration) {
                input "accelerationValue", "bool", defaultValue: false, title: "Flash when Inactive or Active (off = Inactive, On = Active)", description: "Options"
            }
            
            input "contact", "capability.contactSensor", title: "Contact Sensor(s)", required: false, multiple: true, submitOnChange: true
            if(contact) {
                input "contactValue", "bool", defaultValue: false, title: "Flash when Closed or Open (off = Closed, On = Open)", description: "Options"
            }
            
            input "lock", "capability.lock", title: "Lock(s)", required: false, multiple: true, submitOnChange: true
            if(lock) {
                input "lockValue", "bool", defaultValue: false, title: "Flash when Unlocked or Locked (off = Unlocked, On = Locked)", description: "Options"
            }
            
            input "motion", "capability.motionSensor", title: "Motion Sensor(s)", required: false, multiple: true, submitOnChange: true
            if(motion) {
                input "motionValue", "bool", defaultValue: false, title: "Flash when Inactive or Active (off = Inactive, On = Active)", description: "Options"
            }
            
		    input "myPresence", "capability.presenceSensor", title: "Presence Sensor(s)", required: false, multiple: true, submitOnChange: true
            if(myPresence) {
                input "presenceValue", "bool", defaultValue: false, title: "Flash when Not Present or Present (off = Not Present, On = Present)", description: "Options"
            }

		    input "mySwitch", "capability.switch", title: "Switch(es)", required: false, multiple: true, submitOnChange: true
            if(mySwitch) {
                input "switchValue", "bool", defaultValue: false, title: "Flash when Off or On (off = Off, On = On)", description: "Options"
            }
	    }
	    section(getFormat("header-green", "${getImage("Blank")}"+" Flash Options")) {
		    input "switches", "capability.switch", title: "Flash these lights", multiple: true
		    input "numFlashes", "number", title: "Number of times (default: 2)", required: false, width: 6
            input "delay", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
	    }
        section(getFormat("header-green", "${getImage("Blank")}"+" Allow flashing between what times? (Optional)")) {
            input "fromTime", "time", title: "From", required:false, width: 6
        	input "toTime", "time", title: "To", required:false, width: 6
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input "logEnable", "bool", defaultValue: false, title: "Enable Debug Logging", description: "Enable extra logging"
		}
		display2()
	}
}    


def installed() {
	if(logEnable) log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
	if(logEnable) log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe()
}

def subscribe() {
    if(acceleration) subscribe(acceleration, "acceleration", accelerationHandler)
	if(contact) subscribe(contact, "contact", contactHandler)
	if(lock) subscribe(lock, "lock", lockHandler)
	if(motion) subscribe(motion, "motion", motionHandler)
    if(myPresence) subscribe(myPresence, "presence", presenceHandler)
	if(mySwitch) subscribe(mySwitch, "switch", switchHandler)
}

def accelerationHandler(evt) {
	if(logEnable) log.debug "In accelerationHandler - Acceleration: $evt.value"
	if(evt.value == "active" && accelerationValue) flashLights()
	if(evt.value == "inactive" && !accelerationValue) flashLights()
}

def contactHandler(evt) {
	if(logEnable) log.debug "In contactHandler - Contact: $evt.value"
	if(evt.value == "open" && contactValue) flashLights()
	if(evt.value == "closed" && !contactValue) flashLights()
}

def lockHandler(evt) {
	if(logEnable) log.debug "In lockHandler - Lock: $evt.value"
	if(evt.value == "locked" && lockValue) flashLights()
	if(evt.value == "unlocked" && !lockValue) flashLights()
}

def motionHandler(evt) {
	if(logEnable) log.debug "In motionHandler - Motion: $evt.value"
	if(evt.value == "active" && motionValue) flashLights()
	if(evt.value == "inactive" && !motionValue) flashLights()
}

def presenceHandler(evt) {
	if(logEnable) log.debug "In presenceHandler - Presence: $evt.value"
    if(evt.value == "present" && presenceValue) flashLights()
	if(evt.value == "not present" && !presenceValue) flashLights()
}

def switchHandler(evt) {
	if(logEnable) log.debug "In switchHandler - Switch: $evt.value"
    if(evt.value == "on" && switchValue) flashLights()
	if(evt.value == "off" && !switchValue) flashLights()
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

private flashLights() {    // Modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
    checkTime()
    
    if(state.timeBetween) {
	    def doFlash = true
	    def delay = delay ?: 500
	    def numFlashes = numFlashes ?: 2

	    if(logEnable) log.debug "In flashLights - LAST ACTIVATED: ${state.lastActivated}"
	    if(state.lastActivated) {
		    def elapsed = now() - state.lastActivated
		    def sequenceTime = (numFlashes + 1) * (delay)
		    doFlash = elapsed > sequenceTime
		    if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash - ELAPSED: $elapsed - LAST ACTIVATED: ${state.lastActivated}"
	    }

	    if(doFlash) {        // Modified from ST documents
	    	if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
	    	state.lastActivated = now()
	    	if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: ${state.lastActivated}"
	    	def initialActionOn = switches.collect{it.currentSwitch != "on"}

	    	numFlashes.times {
	    		if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
		    	switches.eachWithIndex {s, i ->
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
			    switches.eachWithIndex {s, i ->
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
    } else {
        if(logEnable) log.debug "In flashLights - Outside of allowed time to flash lights."
    }
}

// ********** Normal Stuff **********

def setDefaults(){
	if(settings.logEnable == null){settings.logEnable = false}
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
    section (getFormat("title", "${getImage("logo")}" + " The Flasher - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>The Flasher - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
