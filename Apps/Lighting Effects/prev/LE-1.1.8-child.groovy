/**
 *  ****************  Lighting Effects Child ****************
 *
 *  Design Usage:
 *   - Designed to make static holiday lights dim or flicker randomly.
 *   - Randomly change colors on color change bulbs, completely separate or all together.
 *   - Slowly dim a set of dimmable devices, great for night time routines.
 *   - Slowly raise a set of dimmable devices, great for morning routines.
 *   - Setup a loop to continually raise and lower dimmable devices. 
 *   - Create a spooky, sparkly or party effect.
 *
 *  Copyright 2018 @BPTWorld - Bryan Turcotte
 *
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.1.8 - 12/20/18 - Fixed a nasty bug in Fast_Color_Changing.
 *  V1.1.7 - 12/19/18 - Changed some wording - 'Enable Hue in degrees (0-360)', added 'Not necessary for Hue bulbs'
 *  V1.1.6 - 12/05/18 - Added 'Slow Color Changing' option. Lots of code cleanup.
 *  V1.1.5 - 11/22/18 - Added ability to pause child apps using code developed by @Cobra - Andrew Parker. Thanks!
 *  V1.1.4 - 11/03/18 - All colors are now defined in Custom Color Presets (Parent app). Colors now include Hue, Saturation and
 *						Level for better color control. All colors are customizable, create up to 15 colors in the Parent app. Be 
 *                      sure to turn on 'Enable Hue in degrees (0-360)' for each device used with this app.
 *  V1.1.3 - 10/24/18 - Added portions of ST apps 'Slow Raiser' and 'Dimming Slowly' - 2015 Bruce Ravenel (@Bravenel). Modified
 *                      into 'Slow Off', 'Slow On' and 'Slow Loop' routines. Thanks Bruce! 
 *  V1.1.2 - 10/23/18 - Minor fixes and adjustments
 *  V1.1.1 - 10/23/18 - Color changing can now be random or cycle through.
 *  V1.1.0 - 10/22/18 - MAJOR UPDATE - Added random color changing for color changing bulbs! Based on code from ST app - Holiday
 *                      Color Lights 2016 by ygelfand, thank you. Took a major redesign but the end result was well worth it!
 *                      Also changed the name to Lighting Effects!
 *  V1.0.4 - 10/21/18 - Changed up the dim and flickering routine for a better effect. Also added clearer instructions.
 *  V1.0.3 - 10/03/18 - Added Debug code and Sleep Timer - Thanks again to @Cobra - Andrew Parker (notice a trend here ;))
 *  V1.0.2 - 10/03/18 - Converted to Parent/Child using code developed by @Cobra - Andrew Parker
 *  V1.0.1 - 10/02/18 - Modified to dim instead of just flicker with the help of @Cobra - Andrew Parker
 *  V1.0.0 - 10/01/18 - Hubitat Port of ST app 'Candle Flicker' - 2015 Kristopher Kubicki
 *
 */

definition(
    name: "Lighting Effects Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a spooky, sparkly or party effect.",
    category: "",
    
parent: "BPTWorld:Lighting Effects",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    )

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Lighting Effects Child</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
        	paragraph "<b>Fast Dimming:</b>"
    		paragraph "Designed for dimming modules (z-wave/zigbee). For each Child App, multiple devices can be selected. Each device will run sequential, Device 1, then Device 2, Back to device 1, then device 2..etc."
    		paragraph "To create a random effect, put each device in a separate Child App, using the same switch to turn them on."
        	paragraph "<b>Fast Color Changing:</b>"
        	paragraph "Designed for color changing bulbs (any bulb that has 'colorControl' capability. This section can control lights individually, or all together within the same child app. Used to change colors between 5 sec and 5 minutes."
        	paragraph "Be sure to turn on 'Enable Hue in degrees (0-360)' for each device used with this app."
			paragraph "<b>Slow Color Changing:</b>"
        	paragraph "Designed for color changing bulbs (any bulb that has 'colorControl' capability. This section can control lights individually, or all together within the same child app. Used to change colors between 5 minutes and 3 hours."
        	paragraph "Be sure to turn on 'Enable Hue in degrees (0-360)' for each device used with this app. (Not necessary for Hue bulbs)"
			paragraph "<b>Slow Off, On and Loop:</b>"
        	paragraph "Designed to slowly raise or lower any dimmable device. Great for morning or night routines. Also has the ability to setup a loop to continually raise and lower a dimmable device. Note: The dimming is not smooth but rather done in steps."
            paragraph "<b>Important:</b>"
        	paragraph "Be sure to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"
            paragraph "<b>Very Important:</b>"
			paragraph "Remember that the more devices you add and the faster you send commands, the more you're flooding the network. If you see 'normal' devices not responded as quickly or not at all, be sure to scale back the lighting effects."
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
    		input "triggerMode", "enum", title: "Select Lights Type", submitOnChange: true,  options: ["Fast_Dimmer","Fast_Color_Changing","Slow_Color_Changing", "Slow_Off","Slow_On","Slow_Loop"], required: true, Multiple: false
        
        	if(triggerMode == "Fast_Dimmer"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Used to change colors between 5 sec and 5 minutes")) {
					input "dimmers", "capability.switchLevel", title: "Select Dimmable Lights", required: false, multiple: true
					input "sleepytime", "number", title: "Enter the delay between actions - Big number = Slow, Small number = Fast" , required: true, defaultValue: 6000
        		}
        	}
       		if(triggerMode == "Fast_Color_Changing"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Select your options")) {
					paragraph "Be sure to turn on 'Enable Hue in degrees (0-360)' for each device used with this app. (Not necessary for Hue bulbs)"
        			input "lights", "capability.colorControl", title: "Select Color Changing Bulbs", required: false, multiple:true
					input "brightnessLevel", "number", title: "Brightness Level (range 1 to 100)", required: true, defaultValue: 99, range: '1..180'
            		input "sleepytime2", "number", title: "Enter the delay between actions in seconds (range 5 to 300)" , required: true, defaultValue: 300, range: '5..300'
					input "sleepPattern", "enum", title: "Delay constant or random", defaultValue: "constant", options: ["constant","random"], required: true, multiple: false
					input "seperate", "enum", title: "Cycle each light individually or all together", defaultValue: "individual", options: ["individual","combined"], required: true, multiple: false
                	input "pattern", "enum", title: "Cycle or Randomize each color", defaultValue: "randomize", options: ["randomize","cycle"], required: true, multiple: false
					input "colorSelection", "enum", title: "Choose your colors", options: [
                		[color01:"${parent.msgColor01Name}"],
                		[color02:"${parent.msgColor02Name}"],
                		[color03:"${parent.msgColor03Name}"],
                		[color04:"${parent.msgColor04Name}"],
                		[color05:"${parent.msgColor05Name}"],
                		[color06:"${parent.msgColor06Name}"],
                		[color07:"${parent.msgColor07Name}"],
                		[color08:"${parent.msgColor08Name}"],
                		[color09:"${parent.msgColor09Name}"],
                		[color10:"${parent.msgColor10Name}"],
						[color11:"${parent.msgColor11Name}"],
						[color12:"${parent.msgColor12Name}"],
						[color13:"${parent.msgColor13Name}"],
						[color14:"${parent.msgColor14Name}"],
						[color15:"${parent.msgColor15Name}"],
            		], required: true, multiple: true
				}
			}
			if(triggerMode == "Slow_Color_Changing"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Used to change colors between 5 minutes and 3 hours")) {
					paragraph "Be sure to turn on 'Enable Hue in degrees (0-360)' for each device used with this app."
        			input "lights", "capability.colorControl", title: "Select Color Changing Bulbs", required: false, multiple:true
					input "brightnessLevel", "number", title: "Brightness Level (range 1 to 100)", required: true, defaultValue: 99, range: '1..180'
            		input "sleepytime2", "number", title: "Enter the delay between actions in minutes (range 5 to 180)" , required: true, defaultValue: 60, range: '5..180'
        			input "seperate", "enum", title: "Cycle each light individually or all together", defaultValue: "individual", options: ["individual","combined"], required: true, multiple: false
                	input "pattern", "enum", title: "Cycle or Randomize each color", defaultValue: "randomize", options: ["randomize","cycle"], required: true, multiple: false
					input "colorSelection", "enum", title: "Choose your colors", options: [
                		[color01:"${parent.msgColor01Name}"],
                		[color02:"${parent.msgColor02Name}"],
                		[color03:"${parent.msgColor03Name}"],
                		[color04:"${parent.msgColor04Name}"],
                		[color05:"${parent.msgColor05Name}"],
                		[color06:"${parent.msgColor06Name}"],
                		[color07:"${parent.msgColor07Name}"],
                		[color08:"${parent.msgColor08Name}"],
                		[color09:"${parent.msgColor09Name}"],
                		[color10:"${parent.msgColor10Name}"],
						[color11:"${parent.msgColor11Name}"],
						[color12:"${parent.msgColor12Name}"],
						[color13:"${parent.msgColor13Name}"],
						[color14:"${parent.msgColor14Name}"],
						[color15:"${parent.msgColor15Name}"],
            		], required: true, multiple: true
				}
			}
    		if(triggerMode == "Slow_On"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Select your options")) {
            		input "dimmers", "capability.switchLevel", title: "Select dimmer devices to slowly raise", required: true, multiple: true
    				input "minutes", "number", title: "Takes how many minutes to raise (1 to 60)", required: true, multiple: false, defaultValue:5, range: '1..60'
    				input "targetLevelHigh", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
					tMode = "Slow_On"
				}
   		 	}
    		if(triggerMode == "Slow_Off"){
    			section(getFormat("header-green", "${getImage("Blank")}"+" Select your options")) {
            		input "dimmers", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required: true, multiple: true
    				input "minutes", "number", title: "Takes how many minutes to dim (1 to 60)", required: true, multiple: false, defaultValue:5, range: '1..60'
    				input "targetLevelLow", "number", title: "Target Level (1 to 99)", required: true, multiple: false, defaultValue: 1, range: '1..99'
					tMode = "Slow_Off"
        		}
   			}
    		if(triggerMode == "Slow_Loop"){
    			section(getFormat("header-green", "${getImage("Blank")}"+" Select your options")) {
        			input "dimmers", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required: true, multiple: true
    				input "minutes", "number", title: "Takes how many minutes per dim or raise (1 to 60)", required: true, multiple: false, defaultValue:5, range: '1..60'
    				input "targetLevelHigh", "number", title: "Target Level - High(1 to 99)", required: true, multiple: false, defaultValue: 99, range: '1..99'
            		input "targetLevelLow", "number", title: "Target Level - Low(1 to 99)", required: true, multiple: false, defaultValue: 1, range: '1..99'
            		tMode = "Slow_Loop"
       			}    
   			 }   
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Activate the Dimming/Color Changing when this switch is on")) {
			input "switches", "capability.switch", title: "Switch", required: true, multiple: false
		} 
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=35 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	unschedule()
	logCheck()
	initialize()
}

def initialize() {
	subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
    if(triggerMode == "Fast_Dimmer"){subscribe(switches, "switch", eventHandler)}
    if(triggerMode == "Fast_Color_Changing"){
        subscribe(switches, "switch", changeHandler)
    	state.colorOffset=0
    }
	if(triggerMode == "Slow_Color_Changing"){
        subscribe(switches, "switch", slowChangeHandler)
    	state.colorOffset=0
    }
    if(triggerMode == "Slow_On"){subscribe(switches, "switch", slowonHandler)}
    if(triggerMode == "Slow_Off"){subscribe(switches, "switch", slowoffHandler)}
    if(triggerMode == "Slow_Loop"){subscribe(switches, "switch", slowonHandler)}
}

def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	LOGDEBUG("IN enablerSwitchHandler - Enabler Switch = ${enablerSwitch2}")
	LOGDEBUG("Enabler Switch = $state.enablerSwitch2")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("Enabler Switch is OFF - Child app is active.")
    }
}

def eventHandler(evt) {
	LOGDEBUG("In eventHandler...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")							
	if(switches.currentValue("switch") == "on") {
        if(triggerMode == "Dimmer"){
            for (dimmer in dimmers) {
            	def lowLevel= Math.abs(new Random().nextInt() % 20) + 30
            	def upLevel= Math.abs(new Random().nextInt() % 75) + 24  
            	def lowLevel2= Math.abs(new Random().nextInt() % 20) + 30
            	def upLevel2= Math.abs(new Random().nextInt() % 75) + 24
        		LOGDEBUG("Device: $dimmer - Low Level: $lowLevel Low - High Level: $upLevel - Sleep Time: $sleepytime")
            	state.sleepTime = Math.abs(new Random().nextInt() % sleepytime)
            	dimmer.setLevel(lowLevel)
        		pause(state.sleepTime)
            	dimmer.setLevel(upLevel)
            	pause(state.sleepTime)
            	dimmer.setLevel(lowLevel2)
        		pause(state.sleepTime)
            	dimmer.setLevel(upLevel2)
            	pause(state.sleepTime)
            }
        }
    	runIn(10, eventHandler)
    } else if(switches.currentValue("switch") == "off"){
		dimmers.off()
		unschedule()
	}							
	}
}
    
def changeHandler(evt) {
	LOGDEBUG("In changeHandler...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")							
    if(switches.currentValue("switch") == "on") {
		LOGDEBUG("In changeHandler...")
		LOGDEBUG("Color Selection = ${colorSelection}")
        lights.on()
		LOGDEBUG(" - - - - - - - - - - I'm in changeHandler, triggerMode = ${triggerMode}")
        	if(triggerMode == "Fast_Color_Changing"){
                for (numberoflights in lights) {
					LOGDEBUG(" - - - - - - - - - - sleepPattern = ${sleepPattern}")
					if(sleepPattern == "random"){
                    	state.sleepTime2 = Math.abs(new Random().nextInt() % sleepytime2)
						LOGDEBUG(" - - - - - - - - - - I'm in random, state.sleepTime2 = ${state.sleepTime2}")
					} else{
                    	state.sleepTime2 = sleepytime2
						LOGDEBUG(" - - - - - - - - - - I'm in constant, state.sleepTime2 = ${state.sleepTime2}")
					}
        			def colors = []
                	colors = colorSelection
					LOGDEBUG("Colors = ${colors}")
				
                	def offLights = lights.findAll { light -> light.currentSwitch == "off"}
                	LOGDEBUG("offLights = ${offLights}")
                
                	def onLights = lights.findAll { light -> light.currentSwitch == "on"}
                	LOGDEBUG("onLights = ${onLights}")
    	    		def numberon = onLights.size();
					def numcolors = colors.size();
        			
					LOGDEBUG("pattern = ${pattern}")
                    if (pattern == 'randomize') {
                    	randOffset = Math.abs(new Random().nextInt()%numcolors)
                    	LOGDEBUG("Pattern: ${pattern}")
						LOGDEBUG("Offset: ${randOffset}")
                		if (seperate == 'combined') {
                        	sendcolor(onLights,colors[randOffset])
                		} else {
           					for(def i=0;i<numberon;i++) {
                            	sendcolor(onLights[i],colors[(randOffset + i) % numcolors])
                			}
            			}
                	} else if (pattern == 'cycle') {
                       	if (onLights.size() > 0) {
							if (state.colorOffset >= numcolors ) {
            					state.colorOffset = 0
            				}
							if (seperate == 'combined') {
								sendcolor(onLights,colors[state.colorOffset])
								LOGDEBUG("changeHandler-cycle-combined = onLighgts: ${onLights}, Colors: ${colors[state.colorOffset]}")
							} else {
           						for(def i=0;i<numberon;i++) {
                					sendcolor(onLights[i],colors[(state.colorOffset + i) % numcolors])
									LOGDEBUG("changeHandler-cycle-randomize = onLighgts: ${onLights[i]}, Colors: ${colors[(state.colorOffset + i) % numcolors]}")
                				}
            				}
            				state.colorOffset = state.colorOffset + 1
                        }
     				}
                }
            }
			LOGDEBUG("sleepTime2: ${state.sleepTime2}")
        	runIn(state.sleepTime2, changeHandler)
	} else if(switches.currentValue("switch") == "off"){
		lights.off()
		unschedule()
	}
	}
}

def slowChangeHandler(evt) {
	LOGDEBUG("In slowChangeHandler...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")							
    if(switches.currentValue("switch") == "on") {
		LOGDEBUG("In slowChangeHandler...")
		LOGDEBUG("Color Selection = ${colorSelection}")
        lights.on()
        	if(triggerMode == "Slow_Color_Changing"){
                for (numberoflights in lights) {
                    state.sleepTime2 = (sleepytime2*60)
        			def colors = []
                	colors = colorSelection
					LOGDEBUG("Colors = ${colors}")
				
                	def offLights = lights.findAll { light -> light.currentSwitch == "off"}
                	LOGDEBUG("offLights = ${offLights}")
                
                	def onLights = lights.findAll { light -> light.currentSwitch == "on"}
                	LOGDEBUG("onLights = ${onLights}")
    	    		def numberon = onLights.size();
					def numcolors = colors.size();
        			
					LOGDEBUG("pattern = ${pattern}")
                    if (pattern == 'randomize') {
                    	randOffset = Math.abs(new Random().nextInt()%numcolors)
                    	LOGDEBUG("Pattern: ${pattern}")
						LOGDEBUG("Offset: ${randOffset}")
                		if (seperate == 'combined') {
                        	sendcolor(onLights,colors[randOffset])
                		} else {
           					for(def i=0;i<numberon;i++) {
                            	sendcolor(onLights[i],colors[(randOffset + i) % numcolors])
                			}
            			}
                	} else if (pattern == 'cycle') {
                       	if (onLights.size() > 0) {
							if (state.colorOffset >= numcolors ) {
            					state.colorOffset = 0
            				}
							if (seperate == 'combined') {
								sendcolor(onLights,colors[state.colorOffset])
								LOGDEBUG("changeHandler-cycle-combined = onLighgts: ${onLights}, Colors: ${colors[state.colorOffset]}")
							} else {
           						for(def i=0;i<numberon;i++) {
                					sendcolor(onLights[i],colors[(state.colorOffset + i) % numcolors])
									LOGDEBUG("changeHandler-cycle-randomize = onLighgts: ${onLights[i]}, Colors: ${colors[(state.colorOffset + i) % numcolors]}")
                				}
            				}
            				state.colorOffset = state.colorOffset + 1
                        }
     				}
                }
            }
			LOGDEBUG("sleepTime2: ${state.sleepTime2}")
        	runIn(state.sleepTime2, slowChangeHandler)
	} else if(switches.currentValue("switch") == "off"){
		lights.off()
		unschedule()
	}
	}
}

def slowonHandler(evt) {
	LOGDEBUG("In slowonHandler...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
								
    if(dimmers[0].currentSwitch == "off") {
        dimmers.setLevel(0)
        state.currentLevel = 0
    } else{
        state.currentLevel = dimmers[0].currentLevel
    }
    if(minutes == 0) return
    seconds = minutes * 6
    state.dimStep = targetLevelHigh / seconds
    state.dimLevel = state.currentLevel
    LOGDEBUG("slowonHandler - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
    dimStepUp()			
	}
}

def dimStepUp() {
	LOGDEBUG("In dimStepUp...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")						
    if(switches.currentValue("switch") == "on") {
    	if(state.currentLevel < targetLevelHigh) {
        	state.dimLevel = state.dimLevel + state.dimStep
            if(state.dimLevel > targetLevelHigh) {state.dimLevel = targetLevelHigh}
        	state.currentLevel = state.dimLevel.toInteger()
    		dimmers.setLevel(state.currentLevel)
            LOGDEBUG("dimStepUp - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}")
        	runIn(10,dimStepUp)
    	} else{
            LOGDEBUG("dimStepUp - tMode = ${tMode}")
            if(tMode == "Slow_Loop") {
                runIn(1,slowoffHandler)
            } else{
            	switches.off()
        		LOGDEBUG("dimStepUp - tMode: ${tMode} - Current Level: ${state.currentLevel} - targetLevel: ${targetLevelHigh} - Target Level Reached")
            }
    	}
    } else{
        LOGDEBUG("Current Level: ${state.currentLevel} - Control Switch turned Off")
    }					
	}
}

def slowoffHandler(evt) {
	LOGDEBUG("In slowoffHandler...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")					
    if(dimmers[0].currentSwitch == "off") {
        dimmers.setLevel(99)
        state.currentLevel = 99
    } else{
        state.currentLevel = dimmers[0].currentLevel
    }
    if(minutes == 0) return
    seconds = minutes * 6
    state.dimStep1 = (targetLevelLow / seconds) * 100
    state.dimLevel = state.currentLevel
    LOGDEBUG("slowoffHandler - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
    dimStepDown()					
	}
}

def dimStepDown() {
	LOGDEBUG("In dimStepDown...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")						
    if(switches.currentValue("switch") == "on") {
    	if(state.currentLevel > targetLevelLow) {
            state.dimStep = state.dimStep1
        	state.dimLevel = state.dimLevel - state.dimStep
            if(state.dimLevel < targetLevelLow) {state.dimLevel = targetLevelLow}
        	state.currentLevel = state.dimLevel.toInteger()
    		dimmers.setLevel(state.currentLevel)
            LOGDEBUG("dimStepDown - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}")
        	runIn(10,dimStepDown)
    	} else{
            LOGDEBUG("dimStepDown - tMode = ${tMode}")
            if(tMode == "Slow_Loop") {
                runIn(1,slowonHandler)
            } else {
            	switches.off()
        		LOGDEBUG("dimStepDown - tMode: ${tMode} - Current Level: ${state.currentLevel} - targetLevel: ${targetLevelLow} - Target Level Reached")
    		}
        }    
    } else{
        LOGDEBUG("Current Level: ${state.currentLevel} - Control Switch turned Off")
    }						
	}
}

def sendcolor(lights,color) {
	LOGDEBUG("In sendcolor...Pause: $pause1")
	if(pause1 == true){log.warn "Unable to continue - App paused"}
    if(pause1 == false){LOGDEBUG("Continue - App NOT paused")						
	if (brightnessLevel<1) {
		brightnessLevel=1
	}
    else if (brightnessLevel>100) {
		brightnessLevel=100
	}
    def colorPallet = [
		"color01": [hue: parent.msgColor01Hue, saturation: parent.msgColor01Sat, level: parent.msgColor01Lev],
    	"color02": [hue: parent.msgColor02Hue, saturation: parent.msgColor02Sat, level: parent.msgColor02Lev],
    	"color03": [hue: parent.msgColor03Hue, saturation: parent.msgColor03Sat, level: parent.msgColor03Lev],
    	"color04": [hue: parent.msgColor04Hue, saturation: parent.msgColor04Sat, level: parent.msgColor04Lev],
    	"color05": [hue: parent.msgColor05Hue, saturation: parent.msgColor05Sat, level: parent.msgColor05Lev],
    	"color06": [hue: parent.msgColor06Hue, saturation: parent.msgColor06Sat, level: parent.msgColor06Lev],
    	"color07": [hue: parent.msgColor07Hue, saturation: parent.msgColor07Sat, level: parent.msgColor07Lev],
    	"color08": [hue: parent.msgColor08Hue, saturation: parent.msgColor08Sat, level: parent.msgColor08Lev],
    	"color09": [hue: parent.msgColor09Hue, saturation: parent.msgColor09Sat, level: parent.msgColor09Lev],
    	"color10": [hue: parent.msgColor10Hue, saturation: parent.msgColor10Sat, level: parent.msgColor10Lev],
		"color11": [hue: parent.msgColor11Hue, saturation: parent.msgColor11Sat, level: parent.msgColor11Lev],
		"color12": [hue: parent.msgColor12Hue, saturation: parent.msgColor12Sat, level: parent.msgColor12Lev],
		"color13": [hue: parent.msgColor13Hue, saturation: parent.msgColor13Sat, level: parent.msgColor13Lev],
		"color14": [hue: parent.msgColor14Hue, saturation: parent.msgColor14Sat, level: parent.msgColor14Lev],
		"color15": [hue: parent.msgColor15Hue, saturation: parent.msgColor15Sat, level: parent.msgColor15Lev],
    ]
	def newcolor = colorPallet."${color}"
    LOGDEBUG("${color} = ${newcolor}")
	newcolor.level = brightnessLevel					
	lights*.setColor(newcolor)
    LOGDEBUG("Setting Color = ${color} on: ${lights}")					
	}
}

def pauseOrNot(){
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def setDefaults(){
  LOGDEBUG("Initialising defaults...")
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(debugMode == null){debugMode = false}
	if(state.checkLog == null){state.checkLog = false}
}

def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Lighting Effects - App Version: 1.1.8 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a></div>"
	}
}

