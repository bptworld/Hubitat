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
 *  Copyright 2018-2020 @BPTWorld - Bryan Turcotte
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 * Changes:
 *
 * V2.0.1 - 04/12/20 - Major changes
 * ---
 * V1.0.0 - 10/01/18 - Initial release. This is where it all started for me. By looking at other people work and piecing things 
 * together. Learning by example! Next up was Googling everything I could find, reading, trying... reading more, trying again.
 * Before long, BPTWorld apps was born. Didn't know you were going to get a history lesson today, did you! lol.
 *
 */

def setVersion(){
	state.version = "v2.0.1"
}

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
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Lighting%20Effects/LE%20Child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
            paragraph "<b>Lighting Effects is designed to work with Hue devices. While other brands may work, nothing is guaranteed.</b> If not using Hue devices, you can try turning on 'Enable Hue in degrees (0-360)' for each device used with this app."
        	paragraph "<b>Fast Dimming:</b>"
    		paragraph "For each Child App, multiple devices can be selected. Each device will run sequential, Device 1, then Device 2, Back to device 1, then device 2..etc."
    		paragraph "To create a random effect, put each device in a separate Child App, using the same switch to turn them on."
       	 	paragraph "<b>Fast Color Changing:</b>"
        	paragraph "Designed for color changing bulbs. This section can control lights individually, or all together within the same child app. Used to change colors between 5 sec and 5 minutes."
			paragraph "<b>Slow Color Changing:</b>"
        	paragraph "Designed for color changing bulbs. This section can control lights individually, or all together within the same child app. Used to change colors between 5 minutes and 3 hours."
			paragraph "<b>Slow Off, On and Loop:</b>"
        	paragraph "Designed to slowly raise or lower any dimmable device. Great for morning or night routines. Also has the ability to setup a loop to continually raise and lower a dimmable device. Note: The dimming is not smooth but rather done in steps."
            paragraph "<b>Important:</b>"
        	paragraph "Be sure to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"
            paragraph "<b>Very Important:</b>"
			paragraph "Remember that the more devices you add and the faster you send commands, the more you're flooding the network. If you see 'normal' devices not responded as quickly or not at all, be sure to scale back the lighting effects."
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
    		input "triggerMode", "enum", title: "Select Lights Type", submitOnChange: true,  options: ["Fast_Dimmer","Fast_Color_Changing","Slow_Color_Changing", "Slow_Off","Slow_On","Slow_Loop"], required: true, Multiple: false
        }
        if(triggerMode == "Fast_Dimmer"){
			section(getFormat("header-green", "${getImage("Blank")}"+" Used to change colors between 5 sec and 5 minutes")) {
				input "dimmers", "capability.switchLevel", title: "Select Dimmable Lights", required: false, multiple: true
				input "sleepytime", "number", title: "Enter the delay between actions - Big number = Slow, Small number = Fast" , required: true, defaultValue: 6000
        	}
        }
       	if(triggerMode == "Fast_Color_Changing"){
            section(getFormat("header-green", "${getImage("Blank")}"+" Select your options")) {
        		input "lights", "capability.colorControl", title: "Select Color Changing Bulbs", required: false, multiple:true
            	input "sleepytime2", "number", title: "Enter the delay between actions in seconds (range 5 to 300)" , required: true, defaultValue: 300, range: '5..300'
				input "sleepPattern", "enum", title: "Delay constant or random", defaultValue: "constant", options: ["constant","random"], required: true, multiple: false
				input "seperate", "enum", title: "Cycle each light individually or all together", defaultValue: "individual", options: ["individual","combined"], required: true, multiple: false
                input "pattern", "enum", title: "Cycle or Randomize each color", defaultValue: "randomize", options: ["randomize","cycle"], required: true, multiple: false
                input "colorSelection", "enum", title: "Choose your colors", options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"
                    ], required: true, multiple: true
			}
		}
		if(triggerMode == "Slow_Color_Changing"){
			section(getFormat("header-green", "${getImage("Blank")}"+" Used to change colors between 5 minutes and 3 hours")) {
        		input "lights", "capability.colorControl", title: "Select Color Changing Bulbs", required: false, multiple:true
            	input "sleepytime2", "number", title: "Enter the delay between actions in minutes (range 5 to 180)" , required: true, defaultValue: 60, range: '5..180'
        		input "seperate", "enum", title: "Cycle each light individually or all together", defaultValue: "individual", options: ["individual","combined"], required: true, multiple: false
                input "pattern", "enum", title: "Cycle or Randomize each color", defaultValue: "randomize", options: ["randomize","cycle"], required: true, multiple: false
				input "colorSelection", "enum", title: "Choose your colors", options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"
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
		section(getFormat("header-green", "${getImage("Blank")}"+" Activate the Dimming/Color Changing when this switch is on")) {
			input "switches", "capability.switch", title: "Switch", required: true, multiple: false
		} 
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input "logEnable", "bool", defaultValue: false, title: "Enable Debug Logging", description: "debugging", submitOnChange:true
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    if(triggerMode == "Fast_Dimmer"){subscribe(switches, "switch", fastDimmerHandler)}
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

def fastDimmerHandler(evt) {
    if(logEnable) log.debug "In fastDimmerHandler (${state.version})"				
	if(switches.currentValue("switch") == "on") {
        if(triggerMode == "Dimmer") {
            for (dimmer in dimmers) {                
                def lowLevel= Math.abs(new Random().nextInt() % 20) + 30
            	dimmer.setLevel(lowLevel)
                
                sTime = Math.abs(new Random().nextInt() % sleepytime)
        		pauseExecution(sTime)
                
                def upLevel= Math.abs(new Random().nextInt() % 75) + 24
            	dimmer.setLevel(upLevel)
                
                sTime = Math.abs(new Random().nextInt() % sleepytime)
            	pauseExecution(sTime)
            }
        }
    	runIn(10, fastDimmerHandler)
    } else if(switches.currentValue("switch") == "off") {
		dimmers.off()
		unschedule()
	}							
}
    
def changeHandler(evt) {            // Modified code from ST - Kristopher Kubicki
	if(logEnable) log.debug "In changeHandler (${state.version})"				
    if(switches.currentValue("switch") == "on") {
		if(logEnable) log.debug "In changeHandler - Color Selection = ${colorSelection}"
        lights.on()
		if(logEnable) log.debug " - - - - - - - - - - In changeHandler - triggerMode = ${triggerMode}"
        	if(triggerMode == "Fast_Color_Changing"){
                for (numberoflights in lights) {
					if(logEnable) log.debug " - - - - - - - - - - sleepPattern = ${sleepPattern}"
					if(sleepPattern == "random"){
                    	slTime = Math.abs(new Random().nextInt() % sleepytime2)
						if(logEnable) log.debug " - - - - - - - - - - In random - slTime: ${slTime}"
					} else{
                    	slTime = sleepytime2
						if(logEnable) log.debug " - - - - - - - - - - In constant - slTime: ${slTime}"
					}
        			def colors = []
                	colors = colorSelection
					if(logEnable) log.debug "In changeHandler - Colors: ${colors}"
				
                	def offLights = lights.findAll { light -> light.currentSwitch == "off"}
                	if(logEnable) log.debug "In changeHandler - offLights: ${offLights}"
                
                	def onLights = lights.findAll { light -> light.currentSwitch == "on"}
                	if(logEnable) log.debug "In changeHandler - onLights: ${onLights}"
    	    		def numberon = onLights.size();
					def numcolors = colors.size();
        			
					if(logEnable) log.debug "In changeHandler - pattern = ${pattern}"
                    if (pattern == 'randomize') {
                    	randOffset = Math.abs(new Random().nextInt()%numcolors)
                    	if(logEnable) log.debug "In changeHandler - Pattern: ${pattern} - Offset: ${randOffset}"
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
								if(logEnable) log.debug "In changeHandler - cycle-combined - onLighgts: ${onLights}, Colors: ${colors[state.colorOffset]}"
							} else {
           						for(def i=0;i<numberon;i++) {
                					sendcolor(onLights[i],colors[(state.colorOffset + i) % numcolors])
									if(logEnable) log.debug "In changeHandler - cycle-randomize - onLighgts: ${onLights[i]}, Colors: ${colors[(state.colorOffset + i) % numcolors]}"
                				}
            				}
            				state.colorOffset = state.colorOffset + 1
                        }
     				}
                }
            }
			if(logEnable) log.debug "In changeHandler - slTime: ${slTime}"
        	runIn(slTime, changeHandler)
	} else if(switches.currentValue("switch") == "off"){
		lights.off()
		unschedule()
	}
}

def slowChangeHandler(evt) {        // Modified code from ST - Kristopher Kubicki
	if(logEnable) log.debug "In slowChangeHandler (${state.version})"				
    if(switches.currentValue("switch") == "on") {
		if(logEnable) log.debug "In slowChangeHandler - Color Selection: ${colorSelection}"
        lights.on()
        	if(triggerMode == "Slow_Color_Changing"){
                for (numberoflights in lights) {
                    slpTime = (sleepytime2*60)
        			def colors = []
                	colors = colorSelection
					if(logEnable) log.debug "In slowChangeHandler - Colors: ${colors}"
				
                	def offLights = lights.findAll { light -> light.currentSwitch == "off"}
                	if(logEnable) log.debug "In slowChangeHandler - offLights: ${offLights}"
                
                	def onLights = lights.findAll { light -> light.currentSwitch == "on"}
                	if(logEnable) log.debug "In slowChangeHandler - onLights: ${onLights}"
    	    		def numberon = onLights.size();
					def numcolors = colors.size();
        			
					if(logEnable) log.debug "In slowChangeHandler - pattern: ${pattern}"
                    if (pattern == 'randomize') {
                    	randOffset = Math.abs(new Random().nextInt()%numcolors)
                    	if(logEnable) log.debug "In slowChangeHandler - Pattern: ${pattern} - Offset: ${randOffset}"
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
								if(logEnable) log.debug "In slowChangeHandler - cycle-combined - onLighgts: ${onLights}, Colors: ${colors[state.colorOffset]}"
							} else {
           						for(def i=0;i<numberon;i++) {
                					sendcolor(onLights[i],colors[(state.colorOffset + i) % numcolors])
									if(logEnable) log.debug "In slowChangeHandler - cycle-randomize - onLighgts: ${onLights[i]}, Colors: ${colors[(state.colorOffset + i) % numcolors]}"
                				}
            				}
            				state.colorOffset = state.colorOffset + 1
                        }
     				}
                }
            }
			if(logEnable) log.debug "In slowChangeHandler - slpTime: ${slpTime}"
        	runIn(slpTime, slowChangeHandler)
	} else if(switches.currentValue("switch") == "off"){
		lights.off()
		unschedule()
	}
}

def slowonHandler(evt) {                // Modified code from @Bravenel
	if(logEnable) log.debug "In slowonHandler (${state.version})"							
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
    if(logEnable) log.debug "In slowonHandler - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
    dimStepUp()			
}

def dimStepUp() {                        // Modified code from @Bravenel
	if(logEnable) log.debug "In dimStepUp (${state.version})"			
    if(switches.currentValue("switch") == "on") {
    	if(state.currentLevel < targetLevelHigh) {
        	state.dimLevel = state.dimLevel + state.dimStep
            if(state.dimLevel > targetLevelHigh) {state.dimLevel = targetLevelHigh}
        	state.currentLevel = state.dimLevel.toInteger()
    		dimmers.setLevel(state.currentLevel)
            if(logEnable) log.debug "dimStepUp - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
        	runIn(10,dimStepUp)
    	} else{
            if(logEnable) log.debug "dimStepUp - tMode = ${tMode}"
            if(tMode == "Slow_Loop") {
                runIn(1,slowoffHandler)
            } else{
            	switches.off()
        		if(logEnable) log.debug "dimStepUp - tMode: ${tMode} - Current Level: ${state.currentLevel} - targetLevel: ${targetLevelHigh} - Target Level Reached"
            }
    	}
    } else{
        if(logEnable) log.debug "Current Level: ${state.currentLevel} - Control Switch turned Off"					
	}
}

def slowoffHandler(evt) {                        // Modified code from @Bravenel
	if(logEnable) log.debug "In slowoffHandler (${state.version})"			
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
    if(logEnable) log.debug "In slowoffHandler - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}"
    dimStepDown()
}

def dimStepDown() {                            // Modified code from @Bravenel
	if(logEnable) log.debug "In dimStepDown (${state.version})"		
    if(switches.currentValue("switch") == "on") {
    	if(state.currentLevel > targetLevelLow) {
            state.dimStep = state.dimStep1
        	state.dimLevel = state.dimLevel - state.dimStep
            if(state.dimLevel < targetLevelLow) {state.dimLevel = targetLevelLow}
        	state.currentLevel = state.dimLevel.toInteger()
    		dimmers.setLevel(state.currentLevel)
            if(logEnable) log.debug "In dimStepDown - tMode: ${tMode} - Current Level: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelLow}"
        	runIn(10,dimStepDown)
    	} else{
            if(logEnable) log.debug "In dimStepDown - tMode = ${tMode}"
            if(tMode == "Slow_Loop") {
                runIn(1,slowonHandler)
            } else {
            	switches.off()
        		if(logEnable) log.debug "In dimStepDown - tMode: ${tMode} - Current Level: ${state.currentLevel} - targetLevel: ${targetLevelLow} - Target Level Reached"
    		}
        }    
    } else{
        if(logEnable) log.debug "Current Level: ${state.currentLevel} - Control Switch turned Off"
    }						
}

def sendcolor(lights,color) {
    if(logEnable) log.debug "In sendcolor (${state.version})"
    def hueColor = 0
    def saturation = 100
    switch(color) {
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
	def value = [switch: "on", hue: hueColor, saturation: saturation, level: onLevel as Integer ?: 100]			
	lights*.setColor(value)
    if(logEnable) log.debug "In sendcolor - Setting lights: ${lights} - value: ${value}"		
}

// ********** Normal Stuff **********

def setDefaults(){									
    if(logEnable) log.debug "Initialising defaults..."
	if(logEnable == null){logEnable = false}
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
    section (getFormat("title", "${getImage("logo")}" + " Lighting Effects - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Lighting Effects - @BPTWorld<br>${state.version}</div>"
	}       
}  
