/**
 *  ****************  The Flasher Child App  ****************
 *
 *  Design Usage:
 *  Flash your lights based on several triggers!
 *
 *  Copyright 2019-2024 Bryan Turcotte (@bptworld)
 *
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  1.3.3 - 02/19/24 - Updated
 *  1.3.2 - 08/27/23 - Added a delay between Flash sets
 *  1.3.1 - 07/28/23 - A few updates
 *  1.3.0 - 08/15/22 - Bundle Manager changes
 *  ---
 *  1.0.0 - 01/01/20 - Initial release
 *
 */



def setVersion(){
    state.name = "The Flasher"
    state.version = "1.3.3"
}

def syncVersion(evt){
    setVersion()
    sendLocationEvent(name: "updateVersionsInfo", value: "${state.name}:${state.version}")
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
    importUrl: "",
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

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            appControlSection()
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" The Flasher Trigger Options")) {
            input "acceleration", "capability.accelerationSensor", title: "Acceleration Sensor(s)", required:false, multiple:true, submitOnChange:true
            if(acceleration) {
                input "accelerationValue", "bool", defaultValue: false, title: "Flash when Inactive or Active (off = Inactive, On = Active)", description: "Options"
            }

            input "button", "capability.pushableButton", title: "Button Device(s)", required:false, multiple:true, submitOnChange:true
            if(button) {
                input "buttonNumber", "text", title: "Button Number", defaultValue:1, required:true, submitOnChange:true
            }

            input "contact", "capability.contactSensor", title: "Contact Sensor(s)", required:false, multiple:true, submitOnChange:true
            if(contact) {
                input "contactValue", "bool", defaultValue: false, title: "Flash when Closed or Open (off = Closed, On = Open)", description: "Options"
            }

            input "lock", "capability.lock", title: "Lock(s)", required:false, multiple:true, submitOnChange:true
            if(lock) {
                input "lockValue", "bool", defaultValue: false, title: "Flash when Unlocked or Locked (off = Unlocked, On = Locked)", description: "Options"
            }

            input "motion", "capability.motionSensor", title: "Motion Sensor(s)", required:false, multiple:true, submitOnChange:true
            if(motion) {
                input "motionValue", "bool", defaultValue: false, title: "Flash when Inactive or Active (off = Inactive, On = Active)", description: "Options"
            }

            input "myPresence", "capability.presenceSensor", title: "Presence Sensor(s)", required:false, multiple:true, submitOnChange:true
            if(myPresence) {
                input "presenceValue", "bool", defaultValue:false, title: "Flash when Not Present or Present (off=Not Present, On=Present)", description: "Options"
            }

            input "mySwitch", "capability.switch", title: "Switch(es)", required:false, multiple:true, submitOnChange:true
            if(mySwitch) {
                input "switchValue", "bool", defaultValue:false, title: "Flash when Off or On (off = Off, On = On)", description: "Options", submitOnChange:true
                input "resetSwitch", "bool", defaultValue:false, title: "Reset switch after flashing? <small>*(If switch turned on to activate this flash, turn switch off after completing the flash.)</small>", submitOnChange:true
            }

            input "myWater", "capability.waterSensor", title: "Water Sensor(s)", required:false, multiple:true, submitOnChange:true
            if(myWater) {
                input "waterValue", "bool", defaultValue: false, title: "Flash when Wet or Dry (off = Wet, On = Dry)", description: "Options"
            }

            input "timeToRun", "time", title: "Flash at a Certain Time", required:false
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Options")) {
            input "theSwitch", "capability.switch", title: "Select Lights to Flash", multiple:true, submitOnChange:true
            paragraph "<b>Note:</b> If the light isn't returning to it's original state or the light doesn't seem to flash, raise the seconds between on/off."
            input "numFlashes", "number", title: "Number of times to flash in this set<br>(0 = indefinite)", defaultValue:2, required: false, submitOnChange:true, width: 6
            input "delay", "number", title: "Seconds for lights to be on/off<br>(range: 1 to 10)", range:'1..10', defaultValue:2, required: false, width: 6
            input "level", "number", title: "Set Level to X before flash (1..99)", range: '1..99', defaultValue: 99, submitOnchange:true, width:6
            theSwitch.each { hmm ->
                if(hmm.hasCommand('setColor')) {
                    hasSetColor = true   
                }
            }
            if(hasSetColor) {
                input "fColor", "enum", title: "Color", required: false, multiple:false, options: [
                    ["Soft White":"Soft White - Default"],
                    ["White":"White - Concentrate"],
                    ["Daylight":"Daylight - Energize"],
                    ["Warm White":"Warm White - Relax"],
                    "Red","Green","Blue","Yellow","Orange","Purple","Pink"
                ], width:6
            }

            paragraph "<hr>"
            if(acceleration || contact || lock || motion || myPresence || mySwitch || myWater) {
                input "triggerORswitch", "bool", title: "Use the trigger to turn off the Flashing (off) or Select a Control Switch (on)", submitOnChange:true
                if(triggerORswitch) {
                    input "controlSwitch", "capability.switch", title: "Control Switch", required:true, multiple:false, submitOnChange:true
                    paragraph "<b>Flashing will continue until the Control Switch is turned off or the Max Num of Flashes is reached.</b>"
                } else {
                    app.removeSetting("controlSwitch")
                    paragraph "<b>Flashing will continue until the trigger has been reveresed or the Max Num of Flashes is reached.</b>"
                }
            } else {
                input "controlSwitch", "capability.switch", title: "Control Switch", multiple:false, submitOnChange:true
                paragraph "<b>Flashing will continue until the Control Switch is turned off or the Max Num of Flashes is reached.</b>"
            }
            input "maxFlashes", "number", title: "Maximum Number of Flashes<br>(1-500)", required:true, range: '1..500', defaultValue:10, submitOnChange:true, width:6
            if(maxFlashes > numFlashes) {
                input "setDelay", "number", title: "Seconds to wait between sets<br>(range: 1 to 300)", range:'1..300', defaultValue:10, submitOnChange:true, width:6
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Restrictions")) {
            paragraph "Allow flashing between what times"
            input "fromTime", "time", title: "From", required:false, width: 6
            input "toTime", "time", title: "To", required:false, width: 6

            input "myMode", "mode", title: "Allow flashing when in this Mode", multiple:true, submitOnChange:true

            input "days", "enum", title: "Only flash on these days", description: "Days to run", required:false, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            appGeneralSection()
        }
        display2()
    }
}

def installed() {
    if(logEnable) log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
    state.oldValue = null
    state.switchSaved = false
    initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp) {
        log.info "${app.label} is Paused"
    } else {
        subscribe(location, "appToRun", checkAppToRun)
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
}

def runAppFromEE() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In runAppFromEE - Running ${app.label}" 
        atomicState.runLoop = true
        flashLights()
    }
}

def accelerationHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In accelerationHandler - Acceleration: $evt.value"
        if(evt.value == "active" && accelerationValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "inactive" && accelerationValue) {
            atomicState.runLoop = false
        }
   
        if(evt.value == "inactive" && !accelerationValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "active" && !accelerationValue) {
            atomicState.runLoop = false
        }
    }
}

def buttonHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        bNumber = evt.value
        if(logEnable) log.debug "In buttonHandler - Button: $bNumber"
        if(bNumber == buttonNumber) {
            atomicState.runLoop = true
            flashLights()
        }
    }
}

def contactHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In contactHandler - Contact: $evt.value"
        if(evt.value == "open" && contactValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "closed" && contactValue) {
            atomicState.runLoop = false
        }
        if(evt.value == "closed" && !contactValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "open" && !contactValue) {
            atomicState.runLoop = false
        }
    }
}

def lockHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In lockHandler - Lock: $evt.value"  
        if(evt.value == "locked" && lockValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "unlocked" && lockValue) {
            atomicState.runLoop = false
        }
   
        if(evt.value == "unlocked" && !lockValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "locked" && !lockValue) {
            atomicState.runLoop = false
        }
    }
}

def motionHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In motionHandler - Motion: $evt.value"
        if(evt.value == "active" && motionValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "inactive" && motionValue) {
            atomicState.runLoop = false
        }
   
        if(evt.value == "inactive" && !motionValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "active" && !motionValue) {
            atomicState.runLoop = false
        }
    }
}

def presenceHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In presenceHandler - Presence: $evt.value"   
        if(evt.value == "present" && presenceValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "not present" && presenceValue) {
            atomicState.runLoop = false
        }
   
        if(evt.value == "not present" && !presenceValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "present" && !presenceValue) {
            atomicState.runLoop = false
        }
    }
}

def switchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In switchHandler - Switch: $evt.value"
        state.switchOriginalValue = evt.value
        if(evt.value == "on" && switchValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "off" && switchValue) {
            atomicState.runLoop = false
        }
   
        if(evt.value == "off" && !switchValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "on" && !switchValue) {
            atomicState.runLoop = false
        }
    }
}

def moistureHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In moistureHandler - Water: $evt.value"
        if(evt.value == "dry" && waterValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "wet" && waterValue) {
            atomicState.runLoop = false
        }
   
        if(evt.value == "wet" && !waterValue) {
            atomicState.runLoop = true
            flashLights()
        } else if(evt.value == "dry" && !waterValue) {
            atomicState.runLoop = false
        }
    }
}

def timeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In timeHandler - Time: $timeToRun"
        atomicState.runLoop = true
        flashLights()
    }
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

def flashLights() {
    checkEnableHandler()
    if (numFlashes == 0 && triggerORswitch) {
        if(!controlSwitch) {
            log.warn "ERROR: numFlashes == 0 and no control Switch defined. This should not happen."
            return
        }
    }
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "******************* Start - The Flasher *******************"
        if(logEnable) log.debug "In flashLights (${state.version})"
        checkTime()
        checkMode()
        dayOfTheWeekHandler()
        if(state.oldSwitchValues == null) state.oldSwitchValues = [:]

        if(state.timeBetween) {
            if(state.modeMatch) {
                if(state.daysMatch) {
                    theSwitch.each { tSwitch ->
                        preValues = state.oldSwitchValues.get(tSwitch)                      
                        if(!preValues) {
                            oldSwitchState = tSwitch.currentValue("switch")
                            if(controlSwitch) { controlSwitch.on() }
                            if(tSwitch.hasCommand('setColor')) {
                                oldHueColor = tSwitch.currentValue("hue")
                                oldSaturation = tSwitch.currentValue("saturation")
                                oldLevel = tSwitch.currentValue("level")
                                oldValue = [status: oldSwitchState, hue: oldHueColor, saturation: oldSaturation, level: oldLevel]
                                if(logEnable) log.debug "In flashLights - setColor - ${tSwitch} - saving oldValue: $oldValue"
                                state.oldSwitchValues.put(tSwitch, oldValue)
                            } else if(tSwitch.hasCommand('setLevel')) { 
                                oldLevel = tSwitch.currentValue("level")
                                oldValue = [status: oldSwitchState, level: oldLevel]
                                if(logEnable) log.debug "In flashLights - setLevel - ${tSwitch} - saving oldValue: $oldValue"
                                state.oldSwitchValues.put(tSwitch, oldValue)
                            } else {
                                oldValue = [status: oldSwitchState]
                                if(logEnable) log.debug "In flashLights - on/off - ${tSwitch} - saving oldValue: $oldValue"
                                state.oldSwitchValues.put(tSwitch, oldValue)
                            }
                        } else {
                            if(logEnable) log.info "In flashLights - ${tSwitch} - switch was already saved: $preValues"
                        }
                    }
                    def delay = delay ?: 1
                    def numFlashes = numFlashes ?: 2
                    
                    setLevelandColorHandler(fColor, level)
                    state.levelValue = level
                    
                    runIn(1, doLoopHandler, [data:[delay,numFlashes]])
                } else {
                    if(logEnable) log.debug "In flashLights - Days does not match, can't flash lights."
                }
            } else {
                if(logEnable) log.debug "In flashLights - Mode does not match, can't flash lights."
            }
        } else {
            if(logEnable) log.debug "In flashLights - Outside of allowed time to flash lights."
        }
    }
}

def doLoopHandler(delay, numFlashes) {
    if(state.count == null) {
        state.count = 0
    } else {
        state.count = state.count + 1
    }
    if(logEnable) log.debug "In doLoopHandler - runLoop: ${atomicState.runLoop} - count: $state.count"
    if(atomicState.runLoop) {
        theSwitch.each { s ->
            if(s.currentValue('switch') == "off" || state.count == 0) {
                if(logEnable) log.debug "In doLoopHandler - Switching $s.displayName, on (count: $state.count)"
                if(s.hasCommand('setColor')) {
                    if(logEnable) log.debug "In doLoopHandler - $s.displayName, setColor: ${state.colorValue} ($fColor)"
                    s.setColor(state.colorValue)
                } else if(s.hasCommand('setLevel')) {
                    if(logEnable) log.debug "In doLoopHandler - $s.displayName, setLevel($level)"
                    s.setLevel(level)
                } else {
                    if(logEnable) log.debug "In doLoopHandler - $s.displayName, on"
                    s.on()
                }
            } else {
                if(logEnable) log.debug "In doLoopHandler - $s.displayName, off"
                s.off()
            }
        }

        if(triggerORswitch) {
            cStat = controlSwitch.currentValue('switch', true)
            if(logEnable) log.debug "In doLoopHandler - Checking controlSwitch: $controlSwitch - status: $cStat"
            if(cStat == "off") {
                atomicState.runLoop = false
            }      
        }
        if(logEnable) log.debug "In doLoopHandler - count: ${state.count} - VS - numFlashes: ${numFlashes} (maxFlashes: ${maxFlashes})"
        if(numFlashes >= 1) {
            if(state.count >= numFlashes) { atomicState.runLoop = false }
        }
        maxFlashes = maxFlashes ?: 99
        if(state.count >= maxFlashes) { atomicState.runLoop = false }

        if(atomicState.runLoop) {
            if(state.count >= numFlashes && setDelay) {
                if(logEnable) log.debug "In doLoopHandler - Will run next set in ${setDelay} seconds"
                runIn(setDelay, doLoopHandler, [data:[delay,numFlashes]])
            } else {
                if(delay > 10 || delay == null) delay = 2
                runIn(delay, doLoopHandler, [data:[delay,numFlashes]])
            }
        } else {
            unschedule(doLoopHandler)
            atomicState.onOff = false
            state.count = null
            runIn(delay, setInitialState)
        }
    } else {
        unschedule(doLoopHandler)
        atomicState.onOff = false
        state.count = null
        runIn(delay, setInitialState)
    }
}

def setInitialState() {
    if(logEnable) log.debug "In setInitialState (${state.version})"    
    theSwitch.each { ts ->
        def oldValues = state.oldSwitchValues.get(ts.toString())
        if(oldValues) {
            if(logEnable) log.debug "In setInitialState - (${ts} - oldValues: ${oldValues}"
            pauseExecution(500)
            if(oldValues.status == "on") {
                if(ts.hasCommand('setColor')) {
                    ts.setColor(oldValues)
                } else if(ts.hasCommand('setLevel')) {
                    ts.setLevel(oldValues.level)
                } else {
                    ts.on()
                }
            } else {
                ts.off()
            }
            pauseExecution(500)
            theNewStatus = ts.currentValue("switch")
            state.switchSaved = false
            if(logEnable) log.debug "In setInitialState - ${ts} is now: ${theNewStatus} (original state: ${oldValues})"
        } else {
            if(logEnable) log.warn "In setInitialState - Something went wrong - No oldValues found!"
        }
    }
    if(mySwitch && resetSwitch) {
        if(state.switchOriginalValue == "on") {
            if(logEnable) log.debug "In setInitialState - Resetting trigger switch to OFF"
            mySwitch.off()
        } else {
            if(logEnable) log.debug "In setInitialState - Resetting trigger switch to ON"
            mySwitch.on()
        }
    } else {
        if(logEnable) log.debug "In setInitialState - NOT Resetting the trigger switch"
    }
    if(logEnable) log.debug "******************* Finished - The Flasher *******************"
}

def setLevelandColorHandler(theColor, theLevel) {
    if(logEnable) log.debug "In setLevelandColorHandler (${state.version}) - theColor: ${theColor} - theLevel: ${theLevel}"

    if(theColor == null) theColor = "White"
    if(theLevel == null) theLevel = 99

    def hueColor = 52
    def saturation = 100

    switch(theColor) {
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
    state.colorValue = [hue: hueColor, saturation: saturation, level: theLevel as Integer]
    if(logEnable) log.debug "In setLevelandColorHandler - value: ${state.colorValue}"
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

// ~~~~~ start include (202) BPTWorld.bpt-mandatoryStuff ~~~~~
library ( // library marker BPTWorld.bpt-mandatoryStuff, line 1
        base: "app", // library marker BPTWorld.bpt-mandatoryStuff, line 2
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-mandatoryStuff, line 3
        category: "Apps", // library marker BPTWorld.bpt-mandatoryStuff, line 4
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-mandatoryStuff, line 5
        name: "bpt-mandatoryStuff", // library marker BPTWorld.bpt-mandatoryStuff, line 6
        namespace: "BPTWorld", // library marker BPTWorld.bpt-mandatoryStuff, line 7
        documentationLink: "", // library marker BPTWorld.bpt-mandatoryStuff, line 8
        version: "1.0.0", // library marker BPTWorld.bpt-mandatoryStuff, line 9
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-mandatoryStuff, line 10
) // library marker BPTWorld.bpt-mandatoryStuff, line 11

import groovy.json.* // library marker BPTWorld.bpt-mandatoryStuff, line 13
import java.util.TimeZone // library marker BPTWorld.bpt-mandatoryStuff, line 14
import groovy.transform.Field // library marker BPTWorld.bpt-mandatoryStuff, line 15
import groovy.time.TimeCategory // library marker BPTWorld.bpt-mandatoryStuff, line 16
import java.text.SimpleDateFormat // library marker BPTWorld.bpt-mandatoryStuff, line 17

def setLibraryVersion() { // library marker BPTWorld.bpt-mandatoryStuff, line 19
    state.libraryVersion = "1.0.0" // library marker BPTWorld.bpt-mandatoryStuff, line 20
} // library marker BPTWorld.bpt-mandatoryStuff, line 21

def checkHubVersion() { // library marker BPTWorld.bpt-mandatoryStuff, line 23
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-mandatoryStuff, line 24
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-mandatoryStuff, line 25
    if(logEnable) log.debug "In checkHubVersion - Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-mandatoryStuff, line 26
} // library marker BPTWorld.bpt-mandatoryStuff, line 27

def parentCheck(){   // library marker BPTWorld.bpt-mandatoryStuff, line 29
	state.appInstalled = app.getInstallationState()  // library marker BPTWorld.bpt-mandatoryStuff, line 30
	if(state.appInstalled != 'COMPLETE'){ // library marker BPTWorld.bpt-mandatoryStuff, line 31
		parentChild = true // library marker BPTWorld.bpt-mandatoryStuff, line 32
  	} else { // library marker BPTWorld.bpt-mandatoryStuff, line 33
    	parentChild = false // library marker BPTWorld.bpt-mandatoryStuff, line 34
  	} // library marker BPTWorld.bpt-mandatoryStuff, line 35
} // library marker BPTWorld.bpt-mandatoryStuff, line 36

def appControlSection() { // library marker BPTWorld.bpt-mandatoryStuff, line 38
    input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-mandatoryStuff, line 39
    if(pauseApp) { // library marker BPTWorld.bpt-mandatoryStuff, line 40
        if(app.label) { // library marker BPTWorld.bpt-mandatoryStuff, line 41
            if(!app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-mandatoryStuff, line 42
                app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>") // library marker BPTWorld.bpt-mandatoryStuff, line 43
            } // library marker BPTWorld.bpt-mandatoryStuff, line 44
        } // library marker BPTWorld.bpt-mandatoryStuff, line 45
    } else { // library marker BPTWorld.bpt-mandatoryStuff, line 46
        if(app.label) { // library marker BPTWorld.bpt-mandatoryStuff, line 47
            if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-mandatoryStuff, line 48
                app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>") // library marker BPTWorld.bpt-mandatoryStuff, line 49
            } // library marker BPTWorld.bpt-mandatoryStuff, line 50
        } // library marker BPTWorld.bpt-mandatoryStuff, line 51
    } // library marker BPTWorld.bpt-mandatoryStuff, line 52
    if(pauseApp) {  // library marker BPTWorld.bpt-mandatoryStuff, line 53
        paragraph app.label // library marker BPTWorld.bpt-mandatoryStuff, line 54
    } else { // library marker BPTWorld.bpt-mandatoryStuff, line 55
        label title: "Enter a name for this automation", required:true, submitOnChange:true // library marker BPTWorld.bpt-mandatoryStuff, line 56
    } // library marker BPTWorld.bpt-mandatoryStuff, line 57
} // library marker BPTWorld.bpt-mandatoryStuff, line 58

def appGeneralSection() { // library marker BPTWorld.bpt-mandatoryStuff, line 60
    input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-mandatoryStuff, line 61
    if(logEnable) { // library marker BPTWorld.bpt-mandatoryStuff, line 62
        input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"] // library marker BPTWorld.bpt-mandatoryStuff, line 63
    } // library marker BPTWorld.bpt-mandatoryStuff, line 64
    paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time." // library marker BPTWorld.bpt-mandatoryStuff, line 65
    input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app <small>(When selected switch is ON, app is disabled.)</small>", submitOnChange:true, required:false, multiple:true // library marker BPTWorld.bpt-mandatoryStuff, line 66
} // library marker BPTWorld.bpt-mandatoryStuff, line 67

def uninstalled() { // library marker BPTWorld.bpt-mandatoryStuff, line 69
    sendLocationEvent(name: "updateVersionInfo", value: "${app.id}:remove") // library marker BPTWorld.bpt-mandatoryStuff, line 70
} // library marker BPTWorld.bpt-mandatoryStuff, line 71

def logsOff() { // library marker BPTWorld.bpt-mandatoryStuff, line 73
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-mandatoryStuff, line 74
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-mandatoryStuff, line 75
} // library marker BPTWorld.bpt-mandatoryStuff, line 76

def checkEnableHandler() { // library marker BPTWorld.bpt-mandatoryStuff, line 78
    setVersion() // library marker BPTWorld.bpt-mandatoryStuff, line 79
    state.eSwitch = false // library marker BPTWorld.bpt-mandatoryStuff, line 80
    if(disableSwitch) {  // library marker BPTWorld.bpt-mandatoryStuff, line 81
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-mandatoryStuff, line 82
        disableSwitch.each { it -> // library marker BPTWorld.bpt-mandatoryStuff, line 83
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-mandatoryStuff, line 84
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-mandatoryStuff, line 85
        } // library marker BPTWorld.bpt-mandatoryStuff, line 86
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-mandatoryStuff, line 87
    } // library marker BPTWorld.bpt-mandatoryStuff, line 88
} // library marker BPTWorld.bpt-mandatoryStuff, line 89

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-mandatoryStuff, line 91
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-mandatoryStuff, line 92
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-mandatoryStuff, line 93
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 94
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 95
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 96
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-mandatoryStuff, line 97
    if(type == "logo") return "${loc}logo.png height=40>" // library marker BPTWorld.bpt-mandatoryStuff, line 98
    if(type == "qmark") return "${loc}question-mark-icon.png height=16>" // library marker BPTWorld.bpt-mandatoryStuff, line 99
    if(type == "qmark2") return "${loc}question-mark-icon-2.jpg height=16>" // library marker BPTWorld.bpt-mandatoryStuff, line 100
} // library marker BPTWorld.bpt-mandatoryStuff, line 101

def getFormat(type, myText=null, page=null) {			// Modified code from @Stephack // library marker BPTWorld.bpt-mandatoryStuff, line 103
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid #000000;box-shadow: 2px 3px #80BC00;border-radius: 10px'>${myText}</div>" // library marker BPTWorld.bpt-mandatoryStuff, line 104
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />" // library marker BPTWorld.bpt-mandatoryStuff, line 105
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-mandatoryStuff, line 106

    if(type == "button-blue") return "<a style='color:white;text-align:center;font-size:20px;font-weight:bold;background-color:#03FDE5;border:1px solid #000000;box-shadow:3px 4px #8B8F8F;border-radius:10px' href='${page}'>${myText}</a>" // library marker BPTWorld.bpt-mandatoryStuff, line 108
} // library marker BPTWorld.bpt-mandatoryStuff, line 109

def display(data) { // library marker BPTWorld.bpt-mandatoryStuff, line 111
    if(data == null) data = "" // library marker BPTWorld.bpt-mandatoryStuff, line 112
    if(app.label) { // library marker BPTWorld.bpt-mandatoryStuff, line 113
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-mandatoryStuff, line 114
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-mandatoryStuff, line 115
        } else { // library marker BPTWorld.bpt-mandatoryStuff, line 116
            theName = app.label // library marker BPTWorld.bpt-mandatoryStuff, line 117
        } // library marker BPTWorld.bpt-mandatoryStuff, line 118
    } // library marker BPTWorld.bpt-mandatoryStuff, line 119
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-mandatoryStuff, line 120
    if(!state.name) { state.name = "" } // library marker BPTWorld.bpt-mandatoryStuff, line 121
    if(state.name == theName) { // library marker BPTWorld.bpt-mandatoryStuff, line 122
        headerName = state.name // library marker BPTWorld.bpt-mandatoryStuff, line 123
    } else { // library marker BPTWorld.bpt-mandatoryStuff, line 124
        if(state.name == null || state.name == "") { // library marker BPTWorld.bpt-mandatoryStuff, line 125
            headerName = "${theName}" // library marker BPTWorld.bpt-mandatoryStuff, line 126
        } else { // library marker BPTWorld.bpt-mandatoryStuff, line 127
            headerName = "${state.name} - ${theName}" // library marker BPTWorld.bpt-mandatoryStuff, line 128
        } // library marker BPTWorld.bpt-mandatoryStuff, line 129
    } // library marker BPTWorld.bpt-mandatoryStuff, line 130
    section() { // library marker BPTWorld.bpt-mandatoryStuff, line 131
        paragraph "<h2 style='color:#1A77C9;font-weight: bold'>${headerName}</h2><div style='color:#1A77C9'>A BPTWorld App</div>" // library marker BPTWorld.bpt-mandatoryStuff, line 132

        //<a href='https://community.hubitat.com/t/release-bundle-manager-the-only-place-to-find-bptworld-bundles-find-install-and-update-bundles-quickly-and-easily/94567/295' target='_blank'>Bundle Manager</a>! // library marker BPTWorld.bpt-mandatoryStuff, line 134

        paragraph getFormat("line") // library marker BPTWorld.bpt-mandatoryStuff, line 136
    } // library marker BPTWorld.bpt-mandatoryStuff, line 137
} // library marker BPTWorld.bpt-mandatoryStuff, line 138

def display2() { // library marker BPTWorld.bpt-mandatoryStuff, line 140
    setVersion() // library marker BPTWorld.bpt-mandatoryStuff, line 141
    section() { // library marker BPTWorld.bpt-mandatoryStuff, line 142
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" } // library marker BPTWorld.bpt-mandatoryStuff, line 143
        paragraph getFormat("line") // library marker BPTWorld.bpt-mandatoryStuff, line 144
        if(state.version) { // library marker BPTWorld.bpt-mandatoryStuff, line 145
            bMes = "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}" // library marker BPTWorld.bpt-mandatoryStuff, line 146
        } else { // library marker BPTWorld.bpt-mandatoryStuff, line 147
            bMes = "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name}" // library marker BPTWorld.bpt-mandatoryStuff, line 148
        } // library marker BPTWorld.bpt-mandatoryStuff, line 149
        bMes += "</div>" // library marker BPTWorld.bpt-mandatoryStuff, line 150
        paragraph "${bMes}" // library marker BPTWorld.bpt-mandatoryStuff, line 151
        paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>" // library marker BPTWorld.bpt-mandatoryStuff, line 152
    } // library marker BPTWorld.bpt-mandatoryStuff, line 153
} // library marker BPTWorld.bpt-mandatoryStuff, line 154

// ~~~~~ end include (202) BPTWorld.bpt-mandatoryStuff ~~~~~
