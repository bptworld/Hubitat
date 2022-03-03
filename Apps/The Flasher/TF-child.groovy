/**
 *  ****************  The Flasher Child App  ****************
 *
 *  Design Usage:
 *  Flash your lights based on several triggers!
 *
 *  Copyright 2019-2022 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  1.2.6 - 03/03/22 - Added Button number, Other minor changes
 *  1.2.5 - 02/01/22 - More changes
 *  1.2.4 - 01/30/22 - Big change to presets, now only allows one preset per child app.
 *  1.2.3 - 01/29/22 - Adjustments to Presets
 *  1.2.2 - 01/28/22 - Some much needed adjustments
 *  1.2.1 - 12/21/21 - Fix this...Fix that
 *  1.2.0 - 12/20/21 - Major overhaul, check your child apps!
 *  ---
 *  1.0.0 - 01/01/20 - Initial release
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "The Flasher"
    state.version = "1.2.6"
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
        section(getFormat("header-green", "${getImage("Blank")}"+" Control Options")) {
            paragraph "This child app can work as a stand alone app with triggers and actions, just like any other app.<br>BUT, it can also be setup to be controlled by other BPTWorld apps!  By creating Presets that can be used in select BPTWorld apps."
            input "presetsORstandalon", "bool", title: "'Stand Alone' or 'Controlled by Other Apps'", defaultValue:false, submitOnChange:true
        }
        if(presetsORstandalon) { // controlled by other apps
            section(getFormat("header-green", "${getImage("Blank")}"+" Data Device")) {
                paragraph "Each child app needs a virtual device to store the Presets and to control the app."
                input "useExistingDevice", "bool", title: "Use existing device (off) or have TF create a new one for you (on)", defaultValue:false, submitOnChange:true
                if(useExistingDevice) {
                    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'TF - Preset 1')", required:true, submitOnChange:true
                    paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                    if(dataName) createDataChildDevice()
                    if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                    paragraph "${statusMessageD}"
                }
                input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false, submitOnChange:true
                if(!useExistingDevice) {
                    app.removeSetting("dataName")
                    paragraph "<small>* Device must use the 'The Flasher Driver'.</small>"
                }
            }

            if(dataDevice) {
                section(getFormat("header-green", "${getImage("Blank")}"+" Flash Options")) {
                    input "theSwitch", "capability.switch", title: "Flash this light", multiple:false, submitOnChange:true
                    paragraph "<b>Note:</b> If the light isn't returning to it's original state or the light doesn't seem to flash, raise the seconds between on/off."
                    input "numFlashes", "number", title: "Number of times<br>(0 = indefinite)", required: false, submitOnChange:true, width: 6
                    input "delay", "number", title: "Seconds for lights to be on/off<br>(range: 1 to 10)", range:'1..10', required: false, width: 6
                    if(theSwitch) {
                        if(theSwitch.hasCommand('setLevel')) {
                            input "level", "number", title: "Set Level to X before flash (1..99)", range: '1..99', defaultValue: 99, submitOnchange:true
                        }
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
                }
            } else {
                app.removeSetting("theSwitch")
                app.removeSetting("numFlashes")
                app.removeSetting("delay")
                app.removeSetting("fColor")
            }
        } else {
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
                    input "switchValue", "bool", defaultValue:false, title: "Flash when Off or On (off = Off, On = On)", description: "Options"
                }

                input "myWater", "capability.waterSensor", title: "Water Sensor(s)", required:false, multiple:true, submitOnChange:true
                if(myWater) {
                    input "waterValue", "bool", defaultValue: false, title: "Flash when Wet or Dry (off = Wet, On = Dry)", description: "Options"
                }

                input "timeToRun", "time", title: "Flash at a Certain Time", required:false
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Flash Options")) {
                input "theSwitch", "capability.switch", title: "Flash this light", multiple:false, submitOnChange:true
                paragraph "<b>Note:</b> If the light isn't returning to it's original state or the light doesn't seem to flash, raise the seconds between on/off."
                input "numFlashes", "number", title: "Number of times<br>(0 = indefinite)", required: false, submitOnChange:true, width: 6
                input "delay", "number", title: "Seconds for lights to be on/off<br>(range: 1 to 10)", range:'1..10', required: false, width: 6
                if(theSwitch) {
                    if(theSwitch.hasCommand('setLevel')) {
                        input "level", "number", title: "Set Level to X before flash (1..99)", range: '1..99', defaultValue: 99, submitOnchange:true
                    }
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
                paragraph "<hr>"
                if(acceleration || contact || lock || motion || myPresence || myWater) {
                    input "triggerORswitch", "bool", title: "Use the trigger to turn off the Flashing (off) or Select a Control Switch (on)", submitOnChange:true
                    input "maxFlashes", "number", title: "Maximum Number of Flashes<br>(1-500)", required:true, submitOnChange:true
                    if(triggerORswitch) {
                        input "controlSwitch", "capability.switch", title: "Control Switch", required:true, multiple:false, submitOnChange:true
                        paragraph "<b>Flashing will continue until the Control Switch is turned off or the Max Num of Flashes is reached.</b>"
                    } else {
                        app.removeSetting("controlSwitch")
                        paragraph "<b>Flashing will continue until the trigger has been reveresed or the Max Num of Flashes is reached.</b>"
                    }
                } else {
                    input "maxFlashes", "number", title: "Maximum Number of Flashes<br>(1-500)", required:true, submitOnChange:true
                    input "controlSwitch", "capability.switch", title: "Control Switch", multiple:false, submitOnChange:true
                    paragraph "<b>Flashing will continue until the Control Switch is turned off or the Max Num of Flashes is reached.</b>"
                }
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Restrictions")) {
                paragraph "Allow flashing between what times"
                input "fromTime", "time", title: "From", required:false, width: 6
                input "toTime", "time", title: "To", required:false, width: 6

                input "myMode", "mode", title: "Allow flashing when in this Mode", multiple:true, submitOnChange:true

                input "days", "enum", title: "Only flash on these days", description: "Days to run", required:false, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
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
        if(presetsORstandalon) {
            if(dataDevice) subscribe(dataDevice, "presetCommand", runPresetHandler)
        } else {
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
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def runPresetHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
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

        if(state.timeBetween) {
            if(state.modeMatch) {
                if(state.daysMatch) {
                    state.oldSwitchState = theSwitch.currentValue("switch")
                    if(logEnable) log.debug "In flashLights - theSwitch: ${theSwitch} | numFlashes: ${numFlashes} | delay: ${delay} | fColor: ${fColor} | level: ${level} - Original State: ${state.oldSwitchState}"
                    def delay = delay ?: 1
                    def numFlashes = numFlashes ?: 2

                    if(logEnable) log.debug "In flashLights - switchSaved: $state.switchSaved"
                    if(state.switchSaved == null) state.switchSaved = false
                    if(state.switchSaved == false) {
                        if(controlSwitch) { controlSwitch.on() }
                        if(theSwitch.hasCommand('setColor')) {
                            state.oldValue = null
                            oldHueColor = theSwitch.currentValue("hue")
                            oldSaturation = theSwitch.currentValue("saturation")
                            oldLevel = theSwitch.currentValue("level")
                            state.oldValue = [hue: oldHueColor, saturation: oldSaturation, level: oldLevel]
                            state.switchSaved = true
                            if(logEnable) log.debug "In flashLights - setColor - saving oldValue: $state.oldValue"

                            theData = "${fColor};${level}"
                            if(logEnable) log.debug "In flashLights - Sending to setLevelandColorHandler - theData: $theData"
                            setLevelandColorHandler(theData)
                        } else if(theSwitch.hasCommand('setLevel')) {
                            state.oldValue = null
                            oldLevel = theSwitch.currentValue("level")
                            state.oldValue = oldLevel
                            state.switchSaved = true
                            if(logEnable) log.debug "In flashLights - setLevel - saving oldValue: $state.oldValue"
                            state.value = level
                        }
                    } else {
                        if(logEnable) log.info "In flashLights - switchSaved was already $state.switchSaved"
                    }
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
    if(logEnable) log.debug "In doLoopHandler - runLoop: ${atomicState.runLoop}"
    if(atomicState.runLoop) {
        if(atomicState.onOff == null) atomicState.onOff = true
        if(state.count == null) state.count = 0
        theSwitch.each { s ->
            if(atomicState.onOff) {
                if(logEnable) log.debug "In doLoopHandler - Switching $s.displayName, on (count: $state.count)"
                if(s.hasCommand('setColor')) {
                    if(logEnable) log.debug "In doLoopHandler - $s.displayName, setColor($state.value)"
                    s.setColor(state.value)
                } else if(s.hasCommand('setLevel')) {
                    if(logEnable) log.debug "In doLoopHandler - $s.displayName, setLevel($state.level)"
                    s.setLevel(state.value)
                } else {
                    if(logEnable) log.debug "In doLoopHandler - $s.displayName, on"
                    s.on()
                }
                atomicState.onOff = false
                state.count = state.count + 1
            } else {
                if(logEnable) log.debug "In doLoopHandler - $s.displayName, off"
                s.off()
                atomicState.onOff = true
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
        if(maxFlashes == null) maxFlashes = 99
        if(state.count >= maxFlashes) { atomicState.runLoop = false }

        if(atomicState.runLoop) { 
            if(delay > 10 || delay == null) delay = 1
            runIn(delay, doLoopHandler, [data:[delay,numFlashes]]) 
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
    if(logEnable) log.debug "In setInitialState - Resetting switch - Working on: $theSwitch - oldSwitchState: ${state.oldSwitchState} - oldValue: ${state.oldValue}"
    if(theSwitch.hasCommand('setColor')) {
        theSwitch.setColor(state.oldValue)
    } else if(theSwitch.hasCommand('setLevel')) {
        theSwitch.setLevel(state.oldValue)
    }
    pauseExecution(500)
    if(state.oldSwitchState == "on") {
        theSwitch.on()
    } else {
        theSwitch.off()
    }
    pauseExecution(500)
    theNewStatus = theSwitch.currentValue("switch")
    state.switchSaved = false
    if(logEnable) log.debug "In setInitialState - ${theSwitch} is now: ${theNewStatus} (original state: ${state.oldSwitchState})"
    if(logEnable) log.debug "******************* Finished - The Flasher *******************"
}

def setLevelandColorHandler(data) {
    if(logEnable) log.debug "In setLevelandColorHandler (${state.version})"
    if(data) {
        if(logEnable) log.debug "In setLevelandColorHandler - Received Data: ${data}"
        def (theColor, theLevel) = data.split(";")
        state.theColor = theColor
        state.theLevel = theLevel
        if(logEnable) log.trace "In setLevelandColorHandler - theColor: ${state.theColor} - theLevel: ${state.theLevel}"
    }

    if(state.theColor == null) state.theColor = "White"
    if(state.theLevel == null) state.theLevel = 99

    def hueColor = 52
    def saturation = 100

    switch(state.theColor) {
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
    state.value = [hue: hueColor, saturation: saturation, level: state.theLevel as Integer]
    if(logEnable) log.debug "In setLevelandColorHandler - value: ${state.value}"
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

def createDataChildDevice() {
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "The Flasher Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "The Flasher unable to create device - ${e}" }
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
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
            if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch} - ${eSwitch}"
        }
    }
}

def getImage(type) {                    // Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {            // Modified from @Stephack Code
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
