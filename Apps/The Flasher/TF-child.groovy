/**
 *  ****************  The Flasher Child App  ****************
 *
 *  Design Usage:
 *  Flash your lights based on several triggers!
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
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Based on The Flasher - Bob - 2013-02-06 - Smartthings.  Thank you!
 *
 *  Changes:
 *
 *  1.0.8 - 04/26/20 - Small change to flash handler
 *  1.0.7 - 04/26/20 - Changes to flash handler
 *  1.0.6 - 04/25/20 - Cosmetic changes to flash options, set min and max to flash rate
 *  1.0.5 - 04/25/20 - Added moisture trigger, days restriction
 *  1.0.4 - 01/12/20 - Add Mode restriction
 *  1.0.3 - 01/10/20 - Fixed setup error
 *  1.0.2 - 01/09/20 - Added color to Flash options
 *  1.0.1 - 01/08/20 - Added button as a trigger
 *  1.0.0 - 01/01/20 - Initial release
 *
 */

def setVersion(){
	state.version = "1.0.8"
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
            paragraph "<b>Notes:</b><br>Bulb colors are based on Hue bulbs, results may vary with other type of bulbs."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Trigger Options")) {
            input "acceleration", "capability.accelerationSensor", title: "Acceleration Sensor(s)", required: false, multiple: true, submitOnChange: true
            if(acceleration) {
                input "accelerationValue", "bool", defaultValue: false, title: "Flash when Inactive or Active (off = Inactive, On = Active)", description: "Options"
            }
            
            input "button", "capability.pushableButton", title: "Button Device(s)", required: false, multiple: true, submitOnChange: true
            
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
            
            input "myWater", "capability.waterSensor", title: "Water Sensor(s)", required: false, multiple: true, submitOnChange: true
            if(myWater) {
                input "waterValue", "bool", defaultValue: false, title: "Flash when Wet or Dry (off = Wet, On = Dry)", description: "Options"
            }
            
            input "timeToRun", "time", title: "Flash at a Certain Time", required: false
	    }
	    section(getFormat("header-green", "${getImage("Blank")}"+" Flash Options")) {
		    input "theSwitch", "capability.switch", title: "Flash this light", multiple:false, submitOnChange:true
            paragraph "<b>Note:</b> If the light isn't returning to it's original state, raise the Milliseconds between on/off. Range is 1000 to 5000 (1 to 5 seconds)."
		    input "numFlashes", "number", title: "Number of times<br>", required: false, width: 6
            input "delay", "number", title: "Milliseconds for lights to be on/off<br>(default: 1500 - 1000=1 sec)", range:'1000..5000', required: false, width: 6
            if(theSwitch) {
                if(theSwitch.hasCommand('setColor')) {
                    input "fColor", "enum", title: "Color", required: false, multiple:false, options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"
                    ]
                }
            }
            //if(numFlashes == 99) {
            //    if(mySwitch) {
            //        paragraph "<b>Flashing will continue until the switch choosen above is turned off or 99 times is reached.</b>"
            //    } else {
            //        paragraph "<b>Choosing 99 flashes requires the Switch option to be choosen above.</b>"
            //    }
            //}
	    }
        section(getFormat("header-green", "${getImage("Blank")}"+" Restrictions")) {
            paragraph "Allow flashing between what times"
            input "fromTime", "time", title: "From", required:false, width: 6
        	input "toTime", "time", title: "To", required:false, width: 6
            
            input "myMode", "mode", title: "Allow flashing when in this Mode", multiple:true, submitOnChange:true
            
            input "days", "enum", title: "Only flash on these days", description: "Days to run", required:false, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input "logEnable", "bool", defaultValue: false, title: "Enable Debug Logging", description: "Logging", submitOnChange:true
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
    unschedule()
	subscribe()
}

def subscribe() {
    if(acceleration) subscribe(acceleration, "acceleration", accelerationHandler)
    if(button) subscribe(button, "pushed", buttonHandler)
	if(contact) subscribe(contact, "contact", contactHandler)
	if(lock) subscribe(lock, "lock", lockHandler)
	if(motion) subscribe(motion, "motion", motionHandler)
    if(myPresence) subscribe(myPresence, "presence", presenceHandler)
	if(mySwitch) subscribe(mySwitch, "switch", switchHandler)
    if(myWater) subscribe(myWater, "water", moistureHandler)
    if(timeToRun) schedule(timeToRun, timeHandler)
}

def accelerationHandler(evt) {
	if(logEnable) log.debug "In accelerationHandler - Acceleration: $evt.value"
	if(evt.value == "active" && accelerationValue) flashLights()
	if(evt.value == "inactive" && !accelerationValue) flashLights()
}

def buttonHandler(evt) {
	if(logEnable) log.debug "In buttonHandler - Button: $evt.value"
	flashLights()
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

def moistureHandler(evt) {
	if(logEnable) log.debug "In moistureHandler - Water: $evt.value"
	if(evt.value == "dry" && waterValue) flashLights()
	if(evt.value == "wet" && !waterValue) flashLights()
}

def timeHandler(evt) {
	if(logEnable) log.debug "In timeHandler - Time: -"
	flashLights()
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

def checkMode() {
    if(logEnable) log.debug "In checkMode (${state.version})"
    state.modeMatch = false
    
    if(myMode) {
        myMode.each { it ->
            if(it.contains(location.mode)) {
                state.modeMatch = true
            }
        }
    } else {
        state.modeMatch = true
    }
    if(logEnable) log.debug "In checkMode - modeMatch: ${state.modeMatch}"
}

private flashLights() {    // Modified from ST documents
    if(logEnable) log.debug "******************* Start - The Flasher *******************"
    if(logEnable) log.debug "In flashLights (${state.version})"
    checkTime()
    checkMode()
    dayOfTheWeekHandler()
    
    if(state.timeBetween) {
        if(state.modeMatch) {
            if(state.daysMatch) {
                def doFlash = true
                def delay = delay ?: 1500
                def numFlashes = numFlashes ?: 2

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
                    if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: $state.lastActivated"

                    state.oldSwitchState = theSwitch.currentValue("switch")
                    
                    if(theSwitch.hasCommand('setColor')) {
                        oldHueColor = theSwitch.currentValue("hue")
                        oldSaturation = theSwitch.currentValue("saturation")
                        oldLevel = theSwitch.currentValue("level")                        
                        state.oldValue = [hue: oldHueColor, saturation: oldSaturation, level: oldLevel]
                        
                        if(logEnable) log.debug "In flashLights - setColor - saving oldValue: $state.oldValue"
                        setLevelandColorHandler()
                    }

                    def initialActionOn = (state.oldSwitchState != "on")

                    numFlashes.times {
                        if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
                        theSwitch.eachWithIndex {s, i ->
                            if(initialActionOn) {
                                pauseExecution(delay)

                                if(s.hasCommand('setColor')) {
                                    if(logEnable) log.debug "In flashLights - $s.displayName, setColor($state.value)"
                                    s.setColor(state.value)
                                } else {
                                    if(logEnable) log.debug "In flashLights - $s.displayName, on"
                                    s.on()
                                } 
                            } else {
                                pauseExecution(delay)
                                if(logEnable) log.debug "In flashLights - $s.displayName, off"
                                s.off()
                            }
                        }
                        if(logEnable) log.debug "In flashLights - Switch off after $delay milliseconds"
                        theSwitch.eachWithIndex {s, i ->
                            if(initialActionOn) {
                                pauseExecution(delay)
                                if(logEnable) log.debug "In flashLights - $s.displayName, off"
                                s.off()
                            } else {
                                pauseExecution(delay)
                                if(s.hasCommand('setColor')) {
                                    if(logEnable) log.debug "In flashLights - $s.displayName, setColor($state.value)"
                                    s.setColor(state.value)
                                } else {
                                    if(logEnable) log.debug "In flashLights - $s.displayName, on"
                                    s.on()
                                }
                            }
                        }
                    }

                    if(logEnable) log.debug "In flashLights - Resetting switch - Working on: $theSwitch"
                    if(theSwitch.hasCommand('setColor')) {
                        theSwitch.setColor(state.oldValue)
                        if(logEnable) log.debug "In flashLights - Resetting switch - switch: $theSwitch - oldValue: $state.oldValue"
                    }
                    if(state.oldSwitchState == "on") {
                        theSwitch.on()
                    } else {
                        theSwitch.off()
                    }
                }
            } else {
                if(logEnable) log.debug "In flashLights - Days does not match, can't flash lights."
            }    
        } else {
            if(logEnable) log.debug "In flashLights - Mode does not match, can't flash lights."
        }
    } else {
        if(logEnable) log.debug "In flashLights - Outside of allowed time to flash lights."
    }
    if(logEnable) log.debug "******************* Finished - The Flasher *******************"
}

def setLevelandColorHandler() {
    if(logEnable) log.debug "In setLevelandColorHandler (${state.version}) - fColor: ${fColor}"
    def hueColor = 0
    def saturation = 100
	int onLevel = 99
    switch(fColor) {
            case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }
    state.value = [hue: hueColor, saturation: saturation, level: onLevel as Integer ?: 100]
    if(logEnable) log.debug "In setLevelandColorHandler - value: ${state.value}"
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
		paragraph "<div style='color:#1A77C9;text-align:center'>The Flasher - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${state.version}</div>"
	}       
}
