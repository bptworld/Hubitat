/**
 *  ****************  Lighting Effects Parent App  ****************
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
        	section ("Create a spooky, sparkly or party effect."){}
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
  			section ("Child Apps"){
				app(name: "anyOpenApp", appName: "Lighting Effects Child", namespace: "BPTWorld", title: "<b>Add a new lighting effect</b>", multiple: true)
        	}
    		section (" "){}
  			section("App Name") {
        		label title: "Enter a name for parent app (optional)", required: false
        	}
			section("Be sure to change any of the preset values to suit your bulbs in Advanced Config.") {}
            	section("Advanced Config:", hideable: true, hidden: true) {
					input "msgColor01Name", "text", required: true, title: "Color Name - 01", defaultValue: "White", width:3
            		input "msgColor01Hue", "number", required: true, title: "Hue - 01", defaultValue: "0", width:3
					input "msgColor01Sat", "number", required: true, title: "Saturation - 01", defaultValue: "0", width:3
					input "msgColor01Lev", "number", required: true, title: "Level - 01", defaultValue: "100", width:3
					
                    input "msgColor02Name", "text", required: false, title: "Color Name - 02", defaultValue: "Red", width:3
                    input "msgColor02Hue", "number", required: false, title: "Hue - 02", defaultValue: "0", width:3
					input "msgColor02Sat", "number", required: false, title: "Saturation - 02", defaultValue: "100", width:3
					input "msgColor02Lev", "number", required: true, title: "Level - 02", defaultValue: "50", width:3
					
					input "msgColor03Name", "text", required: false, title: "Color Name - 03", defaultValue: "Lime", width:3
					input "msgColor03Hue", "number", required: false, title: "Hue - 03", defaultValue: "120", width:3
					input "msgColor03Sat", "number", required: false, title: "Saturation - 03", defaultValue: "100", width:3
					input "msgColor03Lev", "number", required: true, title: "Level - 03", defaultValue: "50", width:3
                    
					input "msgColor04Name", "text", required: false, title: "Color Name - 04", defaultValue: "Blue", width:3
					input "msgColor04Hue", "number", required: false, title: "Hue - 04", defaultValue: 240, width:3
					input "msgColor04Sat", "number", required: false, title: "Saturation - 04", defaultValue: "100", width:3
					input "msgColor04Lev", "number", required: true, title: "Level - 04", defaultValue: "50", width:3
                    
					input "msgColor05Name", "text", required: false, title: "Color Name - 05", defaultValue: "Yellow", width:3
					input "msgColor05Hue", "number", required: false, title: "Hue - 05", defaultValue: "60", width:3
					input "msgColor05Sat", "number", required: false, title: "Saturation - 05", defaultValue: "100", width:3
					input "msgColor05Lev", "number", required: true, title: "Level - 05", defaultValue: "50", width:3
                    
					input "msgColor06Name", "text", required: false, title: "Color Name - 06", defaultValue: "Cyan", width:3
					input "msgColor06Hue", "number", required: false, title: "Hue - 06", defaultValue: 180, width:3
					input "msgColor06Sat", "number", required: false, title: "Saturation - 06", defaultValue: 100, width:3
					input "msgColor06Lev", "number", required: true, title: "Level - 06", defaultValue: 50, width:3
                    
					input "msgColor07Name", "text", required: false, title: "Color Name - 07", defaultValue: "Magenta", width:3
					input "msgColor07Hue", "number", required: false, title: "Hue - 07", defaultValue: 300, width:3
					input "msgColor07Sat", "number", required: false, title: "Saturation - 07", defaultValue: 100, width:3
					input "msgColor07Lev", "number", required: true, title: "Level - 07", defaultValue: 50, width:3
                    
					input "msgColor08Name", "text", required: false, title: "Color Name - 08", defaultValue: "Gray", width:3
					input "msgColor08Hue", "number", required: false, title: "Hue - 08", defaultValue: 0, width:3
					input "msgColor08Sat", "number", required: false, title: "Saturation - 08", defaultValue: 0, width:3
					input "msgColor08Lev", "number", required: true, title: "Level - 08", defaultValue: 50, width:3
                    
					input "msgColor09Name", "text", required: false, title: "Color Name - 09", defaultValue: "Maroon", width:3
					input "msgColor09Hue", "number", required: false, title: "Hue - 09", defaultValue: 0, width:3
					input "msgColor09Sat", "number", required: false, title: "Saturation - 09", defaultValue: 100, width:3
					input "msgColor09Lev", "number", required: true, title: "Level - 09", defaultValue: 25, width:3
                    
					input "msgColor10Name", "text", required: false, title: "Color Name - 10", defaultValue: "Olive", width:3
					input "msgColor10Hue", "number", required: false, title: "Hue - 10", defaultValue: 60, width:3
					input "msgColor10Sat", "number", required: false, title: "Saturation - 10", defaultValue: 100, width:3
					input "msgColor10Lev", "number", required: true, title: "Level - 10", defaultValue: 25, width:3
					
					input "msgColor11Name", "text", required: false, title: "Color Name - 11", defaultValue: "Green", width:3
					input "msgColor11Hue", "number", required: false, title: "Hue - 11", defaultValue: 120, width:3
					input "msgColor11Sat", "number", required: false, title: "Saturation - 11", defaultValue: 100, width:3
					input "msgColor11Lev", "number", required: true, title: "Level - 11", defaultValue: 25, width:3
					
					input "msgColor12Name", "text", required: false, title: "Color Name - 12", defaultValue: "Purple", width:3
					input "msgColor12Hue", "number", required: false, title: "Hue - 12", defaultValue: 300, width:3
					input "msgColor12Sat", "number", required: false, title: "Saturation - 12", defaultValue: 100, width:3
					input "msgColor12Lev", "number", required: true, title: "Level - 12", defaultValue: 25, width:3
					
					input "msgColor13Name", "text", required: false, title: "Color Name - 13", defaultValue: "Teal", width:3
					input "msgColor13Hue", "number", required: false, title: "Hue - 13", defaultValue: 180, width:3
					input "msgColor13Sat", "number", required: false, title: "Saturation - 13", defaultValue: 0, width:3
					input "msgColor13Lev", "number", required: true, title: "Level - 13", defaultValue: 25, width:3
					
					input "msgColor14Name", "text", required: false, title: "Color Name - 14", defaultValue: "Navy", width:3
					input "msgColor14Hue", "number", required: false, title: "Hue - 14", defaultValue: 240, width:3
					input "msgColor14Sat", "number", required: false, title: "Saturation - 14", defaultValue: 100, width:3
					input "msgColor14Lev", "number", required: true, title: "Level - 14", defaultValue: 25, width:3
					
					input "msgColor15Name", "text", required: false, title: "Color Name - 15", defaultValue: "To Be Setup", width:3
					input "msgColor15Hue", "number", required: false, title: "Hue - 15", defaultValue: 0, width:3
					input "msgColor15Sat", "number", required: false, title: "Saturation - 15", defaultValue: 0, width:3
					input "msgColor15Lev", "number", required: true, title: "Level - 15", defaultValue: 0, width:3
            	}
	}
  }
}

def update() {
	if(msgColor01Name) childApps.each {child -> child.myMsgColor01Name(msgColor01Name)}
    if(msgColor02Name) childApps.each {child -> child.myMsgColor02Name(msgColor02Name)}
    if(msgColor03Name) childApps.each {child -> child.myMsgColor03Name(msgColor03Name)}
    if(msgColor04Name) childApps.each {child -> child.myMsgColor04Name(msgColor04Name)}
    if(msgColor05Name) childApps.each {child -> child.myMsgColor05Name(msgColor05Name)}
    if(msgColor06Name) childApps.each {child -> child.myMsgColor06Name(msgColor06Name)}
    if(msgColor07Name) childApps.each {child -> child.myMsgColor07Name(msgColor07Name)}
    if(msgColor08Name) childApps.each {child -> child.myMsgColor08Name(msgColor08Name)}
    if(msgColor09Name) childApps.each {child -> child.myMsgColor09Name(msgColor09Name)}
    if(msgColor10Name) childApps.each {child -> child.myMsgColor10Name(msgColor10Name)}
	if(msgColor11Name) childApps.each {child -> child.myMsgColor11Name(msgColor11Name)}
	if(msgColor12Name) childApps.each {child -> child.myMsgColor12Name(msgColor12Name)}
	if(msgColor13Name) childApps.each {child -> child.myMsgColor13Name(msgColor13Name)}
	if(msgColor14Name) childApps.each {child -> child.myMsgColor14Name(msgColor14Name)}
	if(msgColor15Name) childApps.each {child -> child.myMsgColor15Name(msgColor15Name)}
	
	if(msgColor01Hue) childApps.each { child -> child.myMsgColor01Hue(msgColor01Hue) }
    if(msgColor02Hue) childApps.each { child -> child.myMsgColor02Hue(msgColor02Hue) }
    if(msgColor03Hue) childApps.each { child -> child.myMsgColor03Hue(msgColor03Hue) }
    if(msgColor04Hue) childApps.each { child -> child.myMsgColor04Hue(msgColor04Hue) }
    if(msgColor05Hue) childApps.each { child -> child.myMsgColor05Hue(msgColor05Hue) }
    if(msgColor06Hue) childApps.each { child -> child.myMsgColor06Hue(msgColor06Hue) }
    if(msgColor07Hue) childApps.each { child -> child.myMsgColor07Hue(msgColor07Hue) }
    if(msgColor08Hue) childApps.each { child -> child.myMsgColor08Hue(msgColor08Hue) }
    if(msgColor09Hue) childApps.each { child -> child.myMsgColor09Hue(msgColor09Hue) }
    if(msgColor10Hue) childApps.each { child -> child.myMsgColor10Hue(msgColor10Hue) }
	if(msgColor11Hue) childApps.each { child -> child.myMsgColor11Hue(msgColor11Hue) }
	if(msgColor12Hue) childApps.each { child -> child.myMsgColor12Hue(msgColor12Hue) }
	if(msgColor13Hue) childApps.each { child -> child.myMsgColor13Hue(msgColor13Hue) }
	if(msgColor14Hue) childApps.each { child -> child.myMsgColor14Hue(msgColor14Hue) }
	if(msgColor15Hue) childApps.each { child -> child.myMsgColor15Hue(msgColor15Hue) }
	
	if(msgColor01Sat) childApps.each { child -> child.myMsgColor01Sat(msgColor01Sat) }
    if(msgColor02Sat) childApps.each { child -> child.myMsgColor02Sat(msgColor02Sat) }
    if(msgColor03Sat) childApps.each { child -> child.myMsgColor03Sat(msgColor03Sat) }
    if(msgColor04Sat) childApps.each { child -> child.myMsgColor04Sat(msgColor04Sat) }
    if(msgColor05Sat) childApps.each { child -> child.myMsgColor05Sat(msgColor05Sat) }
    if(msgColor06Sat) childApps.each { child -> child.myMsgColor06Sat(msgColor06Sat) }
    if(msgColor07Sat) childApps.each { child -> child.myMsgColor07Sat(msgColor07Sat) }
    if(msgColor08Sat) childApps.each { child -> child.myMsgColor08Sat(msgColor08Sat) }
    if(msgColor09Sat) childApps.each { child -> child.myMsgColor09Sat(msgColor09Sat) }
    if(msgColor10Sat) childApps.each { child -> child.myMsgColor10Sat(msgColor10Sat) }
	if(msgColor11Sat) childApps.each { child -> child.myMsgColor11Sat(msgColor11Sat) }
	if(msgColor12Sat) childApps.each { child -> child.myMsgColor12Sat(msgColor12Sat) }
	if(msgColor13Sat) childApps.each { child -> child.myMsgColor13Sat(msgColor13Sat) }
	if(msgColor14Sat) childApps.each { child -> child.myMsgColor14Sat(msgColor14Sat) }
	if(msgColor15Sat) childApps.each { child -> child.myMsgColor15Sat(msgColor15Sat) }
	
	if(msgColor01Lev) childApps.each { child -> child.myMsgColor01Sat(msgColor01Lev) }
	if(msgColor02Lev) childApps.each { child -> child.myMsgColor02Sat(msgColor02Lev) }
	if(msgColor03Lev) childApps.each { child -> child.myMsgColor03Sat(msgColor03Lev) }
	if(msgColor04Lev) childApps.each { child -> child.myMsgColor04Sat(msgColor04Lev) }
	if(msgColor05Lev) childApps.each { child -> child.myMsgColor05Sat(msgColor05Lev) }
	if(msgColor06Lev) childApps.each { child -> child.myMsgColor06Sat(msgColor06Lev) }
	if(msgColor07Lev) childApps.each { child -> child.myMsgColor07Sat(msgColor07Lev) }
	if(msgColor08Lev) childApps.each { child -> child.myMsgColor08Sat(msgColor08Lev) }
	if(msgColor09Lev) childApps.each { child -> child.myMsgColor09Sat(msgColor09Lev) }
	if(msgColor10Lev) childApps.each { child -> child.myMsgColor10Sat(msgColor10Lev) }
	if(msgColor11Lev) childApps.each { child -> child.myMsgColor10Sat(msgColor11Lev) }
	if(msgColor12Lev) childApps.each { child -> child.myMsgColor10Sat(msgColor12Lev) }
	if(msgColor13Lev) childApps.each { child -> child.myMsgColor10Sat(msgColor13Lev) }
	if(msgColor14Lev) childApps.each { child -> child.myMsgColor10Sat(msgColor14Lev) }
	if(msgColor15Lev) childApps.each { child -> child.myMsgColor10Sat(msgColor15Lev) }
}

def installCheck(){         
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
	}
    else{
        log.info "Parent Installed OK"
    }
}

def display(){
	section{
		paragraph "<b>Lighting Effects</b><br>App Version: 1.1.7<br>@BPTWorld"
	}        
}         
