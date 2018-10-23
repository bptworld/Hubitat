/**
 *  ****************  Lighting Effects Child ****************
 *
 *  Design Usage:
 *  Designed to make static holiday lights dim or flicker randomly. Can also randomly change colors on color change
 *  bulbs, completely separate or all together. Creating a spooky, sparkly or party effect.
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
 *  V1.1.2 - 10/24/18 - Minor fixes and adjustments
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
    description: "Designed to make static holiday lights dim or flicker randomly. Can also randomly change colors on color change bulbs, completely separate or all together. Creating a spooky, sparkly or party effect.",
    category: "",
    
parent: "BPTWorld:Lighting Effects",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    )

preferences {
    display()
        section ("Designed to make static holiday lights dim or flicker randomly. Can also randomly change colors on color change bulbs, completely separate or all together. Creating a spooky, sparkly or party effect."){}    
        section("Instructions:", hideable: true, hidden: true) {
        	paragraph "Dimming:"
    		paragraph "Designed for dimming modules (z-wave/zigbee). For each Child App, multiple devices can be selected. Each device will run sequential, Device 1, then Device 2, Back to device 1, then device 2..etc."
    		paragraph "To create a random effect, put each device in a separate Child App, using the same switch to turn them on."
        	paragraph "Color Changing:"
        	paragraph "Designed for color changing bulbs (any bulb that has 'colorControl' capability. This section can control lights individually, or all together within the same child app."
        	paragraph "Important:"
        	paragraph "Be sure to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"
        }
   		section() {
    	input "triggerMode", "enum", required: true, title: "Select Lights Type", submitOnChange: true,  options: ["Dimmer", "Color_Changing"] 
            if(triggerMode == "Dimmer"){ 
			section("Select Dimmable Lights") {
				input "dimmers", "capability.switchLevel", title: "Select Dimmable Lights", required: false, multiple: true
				input "sleepytime", "number", title: "Enter the delay between actions - Big number = Slow, Small number = Fast" , required: true, defaultValue: 6000
        	}
        }
            
        if(triggerMode == "Color_Changing"){
        	section("Select Color Changing Bulbs") {
        		input "lights", "capability.colorControl", title: "Bulbs", required: false, multiple:true
				input "brightnessLevel", "number", title: "Brightness Level (1-100)?", required:false, defaultValue:100, range: '1..100'
            	input "sleepytime2", "number", title: "Enter the delay between actions - Big number = Slow, Small number = Fast" , required: true, defaultValue: 6000
        		input "seperate", "enum", title: "Cycle each light individually, or all together?", defaultValue: "individual", options: [
					[individual:"Individual"],
					[combined:"Combined"],
				], required: true, multiple: false
                input "pattern", "enum", title: "Cycle each color or Randomize?", defaultValue: "randomize", options: [
					[randomize:"Randomize"],
					[cycle:"Cycle"],
				], required: true, multiple: false
      
           		input "colorSelection", "enum", title: "Choose your colors", options: [
                	[White:"White"],
                	[Daylight:"Daylight"],
                	[Soft_White:"Soft White"],
                	[Warm_White:"Warm White"],
                	[Navy_Bluek:"Navy Blue"],
                	[Blue:"Blue"],
                	[Green:"Green"],
                	[Turquoise:"Turquoise"],
                	[Aqua:"Aqua"],
                	[Amber:"Amber"],
                	[Yellow:"Yellow"],
                	[Safety_Orange:"Safety Orange"],
                	[Orange:"Orange"],
                	[Indigo:"Indigo"],
                	[Purple:"Purple"],
                	[Pink:"Pink"],
                	[Raspberry:"Raspberry"],
                	[Red:"Red"],
                	[Brick_Red:"Brick Red"],
            	], required: true, multiple: true
        	}
        }
    }
    
		section("Activate the Dimming/Color Changing when this switch is on") {
			input "switches", "capability.switch", title: "Switch", required: true, multiple: false
		} 
    	section() {
        	input "debugMode", "bool", title: "Enable Debug Logging", required: true, defaultValue: false
    	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    if(triggerMode == "Dimmer"){subscribe(switches, "switch", eventHandler)}
    if(triggerMode == "Color_Changing"){
        subscribe(switches, "switch", changeHandler)
    	state.colorOffset=0
    }
}

def eventHandler(evt) {
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
    	runIn(1,"eventHandler")
    } else if(switches.currentValue("switch") == "off"){dimmers.off()}
}
    
def changeHandler(evt) {
    if(switches.currentValue("switch") == "on") {
        lights.on()
        	if(triggerMode == "Color_Changing"){
                for (numberoflights in lights) {
                    state.sleepTime2 = Math.abs(new Random().nextInt() % sleepytime2)
                    
        			def colors = []
                	colors = colorSelection
				
                	def offLights = lights.findAll { light -> light.currentSwitch == "off"}
                	LOGDEBUG("offLights = ${offLights}")
                
                	def onLights = lights.findAll { light -> light.currentSwitch == "on"}
                	LOGDEBUG("onLights = ${onLights}")
    	    		def numberon = onLights.size();
					def numcolors = colors.size();
        		
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
							if (seperate == 'combined')
								sendcolor(onLights,colors[state.colorOffset])
            				else {
            					LOGDEBUG("Colors: ${colors}")
           						for(def i=0;i<numberon;i++) {
                					sendcolor(onLights[i],colors[(state.colorOffset + i) % numcolors])
                				}
            				}
            				state.colorOffset = state.colorOffset + 1
                        }
     				}
                    pause(state.sleepTime2)
                }
            }
        	runIn(1,"changeHandler")
	} else if(switches.currentValue("switch") == "off"){lights.off()}
}

def sendcolor(lights,color) {
	LOGDEBUG("In sendcolor")
	if (brightnessLevel<1) {
		brightnessLevel=1
	}
    else if (brightnessLevel>100) {
		brightnessLevel=100
	}

    def colorPallet = [
    	"White": [hue: 0, saturation: 0],
    	"Daylight": [hue: 53, saturation: 91],
    	"Soft_White": [hue: 23, saturation: 56],
    	"Warm_White": [hue: 20, saturation: 80],
    	"Navy_Blue": [hue: 61, saturation: 100],
    	"Blue": [hue: 65, saturation: 100],
    	"Green": [hue: 33, saturation: 100],
    	"Turquoise": [hue: 47, saturation: 100],
    	"Aqua": [hue: 50, saturation: 100],
    	"Amber": [hue: 13, saturation: 100],
    	"Yellow": [hue: 17, saturation: 100],
    	"Safety_Orange": [hue: 7, saturation: 100],
    	"Orange": [hue: 10, saturation: 100],
    	"Indigo": [hue: 73, saturation: 100],
    	"Purple": [hue: 82, saturation: 100],
    	"Pink": [hue: 91, saturation: 68],
    	"Raspberry": [hue: 94 , saturation: 100],
    	"Red": [hue: 0, saturation: 100],
    	"Brick_Red": [hue: 4, saturation: 100],
    ]
	def newcolor = colorPallet."${color}"
    LOGDEBUG(" ${color} = ${newcolor}")
    if(newcolor.saturation == null) newcolor.saturation = 100
    newcolor.level = brightnessLevel
	lights*.setColor(newcolor)
    LOGDEBUG("Setting Color = ${color} for: ${lights}")
}

// define debug action
def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "Further Logging Disabled"
	}
}

// logging...
def LOGDEBUG(txt){
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}

def display(){
	section{paragraph "Child App Version: 1.1.2"}
} 
