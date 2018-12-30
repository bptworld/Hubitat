/**
 *  ****************  Motion Controlled Scene Lighting App  ****************
 *
 *  Design Usage:
 *  Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'.
 *  Hubitat has all these great built-in apps. This app brings a few of them together!
 *		- Uses 'Modes', 'Groups and Scenes' and 'Zone Motion Controller'
 *		- Motion is the key to making everything happen. User can select individual motion sensors
 *		  or choose a Zone Motion Controller device
 *		- Turn lights on/off based on Mode, utilizing Hubitat Scenes
 *		- Optionally: Can also use light level as a condition
 *		- Select up to 8 different Modes/Scene combinations per child app
 *		- Each child app has a Lights Dim Warning when X amount of time has passed without motion
 *		- If still no motion after Dim Warning, turn off all lights in Scene after X amount of time
 *		- When motion is activated, lights are reset to the appropriate Scene
 *		- Each child app also has a Safety Net which turns off any user selected lights after X
 *		  amount of time has passed. Can be any lights, in Scene or not. (ie. all lights in room)
 *		- Ability to pause any child app
 *		- Ability to Enable/Disable child app via a switch
 *		- Parent/Child App structure
 *		- Create as many child apps as needed
 *		- Displays the current Mode next to the Parent App Name for easy reference
 *
 *	
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *
 *  Also thanks to Stephan Hackett (@stephacka) for sharing his code/app on how to dim lights when there is no motion.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.1 - 12/30/18 - Updated to new theme.
 *  V1.0.0 - 12/19/18 - Initial release.
 *
 */

definition(
    name: "Motion Controlled Scene Lighting",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'.",
    category: "",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
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
	subscribe(location, "mode", nameChange)
    log.info "There are ${childApps.size()} child apps"
    childApps.each {child ->
    log.info "Child app: ${child.label}"
    }
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section(getFormat("title", "${app.label}")) {
				paragraph "<div style='color:#1A77C9'>Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Info:</b>"
    			paragraph "Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'."
				paragraph "<b>Prerequisites:</b>"
				paragraph "- Must already have at least one Scene setup in Hubitats 'Groups and Scenes' built in app.<br>- Have at least one dimmable buld included in each Scene.<br>- (Optional) Have a virutal switch created to Enable/Disable each child app."
			}
  			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "Motion Controlled Scene Lighting Child", namespace: "BPTWorld", title: "<b>Add a new 'Motion Controlled Scene Lighting' child</b>", multiple: true)
  			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			display()
		}
	}
}

def nameChange(evt){
	def appName = "Motion Controlled Scene Lighting"
	app.updateLabel(appName + ("<font color = 'green'> ${location.mode} </font>" ))
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

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=35 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display(){
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Motion Controlled Scene Lighting - App Version: 1.0.0 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a></div>"
	}       
}         

