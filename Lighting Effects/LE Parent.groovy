/**
 *  ****************  Lighting Effects Parent App  ****************
 *
 *  Design Usage:
 *  Designed to make static holiday lights dim or flicker randomly. Can also randomly change colors on color change
 *  bulbs, completely seperate or all together. Creating a spooky, sparkly or party effect.
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
 *  V1.1.0 - 10/22/18 - MAJOR UPDATE - Added random color changing for color changing bulbs! Based on code from ST app - Holiday
 *                      Color Lights 2016 by ygelfand, thank you. Took a major redesign but the end result was well worth it!
 *                      Also changed the app name to Lighting Effects!
 *  V1.0.4 - 10/21/18 - Changed up the dim and flickering routine for a better effect. Also added clearer instructions.
 *  V1.0.3 - 10/03/18 - Added Debug code and Sleep Timer - Thanks again to @Cobra - Andrew Parker (notice a trend here ;))
 *  V1.0.2 - 10/03/18 - Converted to Parent/Child using code developed by @Cobra - Andrew Parker
 *  V1.0.1 - 10/02/18 - Modified to dim instead of flicker with the help of @Cobra - Andrew Parker
 *  V1.0.0 - 10/01/18 - Hubitat Port of ST app 'Candle Flicker' - 2015 Kristopher Kubicki
 *
 */

definition(
    name:"Lighting Effects",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Parent App for 'Lighting Effects' childapps ",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
    )

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "There are ${childApps.size()} child apps"
    childApps.each {child ->
    log.info "Child app: ${child.label}"
    }
    
}

def mainPage() {
    dynamicPage(name: "mainPage") {
      installCheck()
        
	if(state.appInstalled == 'COMPLETE'){
		display()
        	section ("Designed to make static holiday lights dim or flicker randomly. Can also randomly change colors on color change bulbs, completely seperate or all together. Creating a spooky, sparkly or party effect."){}
        	section("Instructions:", hideable: true, hidden: true) {
        		paragraph "Dimming:"
    			paragraph "Designed for dimming modules (z-wave/zigbee). For each Child App, multiple devices can be selected. Each device will run sequential, Device 1, then Device 2, Back to device 1, then device 2..etc."
    			paragraph "To create a random effect, put each device in a separate Child App, using the same switch to turn them on."
        		paragraph "Color Changing:"
        		paragraph "Designed for color changing bulbs (any bulb that has 'colorControl' capability. This section can control lights individually, or all together within the same child app."
        		paragraph "Important:"
        		paragraph "Be sure to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"
        	}
  			section ("Child Apps"){
				app(name: "anyOpenApp", appName: "Lighting Effects Child", namespace: "BPTWorld", title: "<b>Add a new lighting effect</b>", multiple: true)
        	}
    		section (" "){}
  			section("App Name") {
        		label title: "Enter a name for parent app (optional)", required: false
        	}    
	}
  }
}

def installCheck(){         
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
	}
    else{
 	//       log.info "Parent Installed OK"
    }
}

def display(){
	section{paragraph "Version: 1.1.0<br>@BPTWorld"}
}         

def setVersion(){
		state.InternalName = "LightingEffectsParent"  
}
