/**
 *  ****************  Random Dim and Flicker Child ****************
 *
 *  Design Usage:
 *  Designed to make normally static holiday decorations/lights dim randomly. Creating a spooky or sparkly effect.
 *
 *  Copyright 2018 @BPTWorld - Bryan Turcotte
 *  Major contributions from @Cobra - Andrew Parker
 *
 *  This App is free!
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
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! - @BPTWorld
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *  
 *  
 *  V1.0.3 - 10/03/18 - Added Debug code and Sleep Timer - Thanks again to @Cobra - Andrew Parker (notice a trend here ;))
 *  V1.0.2 - 10/03/18 - Converted to Parent/Child using code developed by @Cobra - Andrew Parker
 *  V1.0.1 - 10/02/18 - Modified to dim instead of flicker with the help of @Cobra - Andrew Parker
 *  V1.0.0 - 10/01/18 - Hubitat Port of ST app 'Candle Flicker' - 2015 Kristopher Kubicki
 *
 */
 
 
 
 
definition(
    name: "Random Dim and Flicker Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Designed to make lights dim randomly. Creating a spooky or sparkly effect.",
    category: "",
    
parent: "BPTWorld:Random Dim and Flicker",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    )

	preferences {
        display()
        
        section("Remember to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"){}
        
		section("Select Dimmable Lights") {
		input "dimmers", "capability.switchLevel", title: "Lights", required: true, multiple: true
	}

	section("Activate the Dimming when this switch is on") {
		input "switches", "capability.switch", title: "Switch", required: true, multiple: false
	}
        
	section("Enter the delay between actions - Big number = Slow, Small number = Fast") {
		input "sleepytime", "number", title: "Enter sleep time" , required: true, defaultValue: 6000
	}  
    
    section() {
        input "debugMode", "bool", title: "Enable Debug Logging", required: true, defaultValue: false
    }
}


def installed() {
	initialize()
}

def updated() {	
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule() 
	subscribe(switches, "switch", eventHandler)
}


def eventHandler(evt) {
	if(switches.currentValue("switch") == "on") {
		for (dimmer in dimmers) {      
                	def lowLevel= Math.abs(new Random().nextInt() % 20) + 30
                	def upLevel= Math.abs(new Random().nextInt() % 75) + 24    
        LOGDEBUG("Device: $dimmer - Low Level: $lowLevel Low - High Level: $upLevel - Sleep Time: $sleepytime")
            dimmer.setLevel(upLevel)
            state.sleepTime = Math.abs(new Random().nextInt() % sleepytime)
        	pause(state.sleepTime)
            dimmer.setLevel(lowLevel)
            
        	}
        	state.sleepTime = Math.abs(new Random().nextInt() % sleepytime)
        	pause(state.sleepTime)
        	runIn(5,"eventHandler")
	}
    else if(switches.currentValue("switch") == "off") {dimmers.off()}
 
        
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
  
	section{paragraph "Child App Version: 1.0.3"}
         
} 
