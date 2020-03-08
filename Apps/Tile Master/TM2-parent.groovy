/**
 *  ****************  Tile Master 2 Parent ****************
 *
 *  Design Usage:
 *  Create a tile with multiple devices and customization options.
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
 *  V2.1.1 - 03/02/20 - Removed status color options from parent app
 *  V2.1.0 - 02/26/20 - Added support for Tile to Tile copying
 *  V2.0.9 - 02/16/20 - Added Custom Icons!
 *  V2.0.8 - 02/12/20 - Cosmetic changes
 *  V2.0.7 - 02/12/20 - Added default color codes
 *  V2.0.6 - 02/05/20 - Support Smoke/CO decectors (clear-detected) by @LostJen. Thanks!
 *  V2.0.5 - 11/04/19 - Fixed some typo's in the color options, thanks scubamikejax904!
 *  V2.0.4 - 09/27/19 - Fixed missing motion color options
 *  V2.0.3 - 09/22/19 - Added color options for Temperature and Battery Levels
 *  V2.0.2 - 09/21/19 - Added device value color options
 *  V2.0.1 - 09/20/19 - Initial release.
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.0 - 02/16/19 - Initially started working on this concept but never released.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "TileMaster2ParentVersion"
	state.version = "v2.1.1"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name:"Tile Master 2",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a tile with multiple devices and customization options.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Tile%20Master/TM2-parent.groovy",
)

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
    page name: "iconOptions", title: "", install: false, uninstall: true, nextPage: "mainPage"
    page name: "colorOptions", title: "", install: false, uninstall: true, nextPage: "mainPage"
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
    if(awDevice) schedule("0 0 3 ? * * *", setVersion)
    sendIconList()
}

def mainPage() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display()
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Information</b>"
				paragraph "Create a tile with multiple devices and customization options."
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
                paragraph "Be sure to complete the 'Global Icon Options' section and hit 'done' if you want to use Icons with your devices."
				app(name: "anyOpenApp", appName: "Tile Master 2 Child", namespace: "BPTWorld", title: "<b>Add a new 'Tile Master 2' child</b>", multiple: true)
			}
            // ** App Watchdog Code **
            section("This app supports App Watchdog 2! Click here for more Information", hideable: true, hidden: true) {
				paragraph "<b>Information</b><br>See if any compatible app needs an update, all in one place!"
                paragraph "<b>Requirements</b><br> - Must install the app 'App Watchdog'. Please visit <a href='https://community.hubitat.com/t/release-app-watchdog/9952' target='_blank'>this page</a> for more information.<br> - When you are ready to go, turn on the switch below<br> - Then select 'App Watchdog Data' from the dropdown.<br> - That's it, you will now be notified automaticaly of updates."
                input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")
			}
            if(sendToAWSwitch) {
                section(getFormat("header-green", "${getImage("Blank")}"+" App Watchdog 2")) {    
                    if(sendToAWSwitch) input(name: "awDevice", type: "capability.actuator", title: "Please select 'App Watchdog Data' from the dropdown", submitOnChange: true, required: true, multiple: false)
			        if(sendToAWSwitch && awDevice) setVersion()
                }
            }
            // ** End App Watchdog Code **
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
                input "logEnable", "bool", defaultValue: "false", title: "Enable Debug Logging", description: "Enable extra logging for debugging."
 			}

            section(getFormat("header-green", "${getImage("Blank")}"+" Global Icon Config")) {}
            section("Icon Options:", hideable: true, hidden: false) {
                if(iconName || iconURL) {
                    href "iconOptions", title:"${getImage("optionsGreen")} Select Icons", description:"Click here for Options"
                } else {
                    href "iconOptions", title:"${getImage("optionsRed")} Select Icons", description:"Click here for Options"
                }
            }
        }
    }
}

def iconOptions() {
    dynamicPage(name: "iconOptions", title: "", install:false, uninstall:false) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Icon Options")) {}
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Exchange your device status with Icons!</b>"
            
            instruct =  "- It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small> "
            instruct += "This will save on character counts.<br>"
            instruct += "- Remember to add icons for both values (ie. on and off, open and closed)<br>"
            instruct += "- Add as many icons as you like, you can use different icons for different devices!<br>"
            instruct += "- All icon URL addresses must include 'http://'<br>"
            instruct += "- When deleting icons, only the Name is required (Must be exact)"
            
            paragraph "${instruct}"
		}
        section() {
            input "iconName", "text", title: "Icon Name (ie. Green Check Mark)", submitOnChange:true
            input "iconURL", "text", title: "Icon URL (ie. http://bit.ly/2m0udns) <small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>", submitOnChange:true
            input "addIcon", "button", title: "Add Icon", width: 4
            input "delIcon", "button", title: "Delete Icon", width: 4
            input "delList", "button", title: "Delete All Icons", width: 4
            if(state.message != null) paragraph "<b>${state.message}</b>"
            if(state.deleteAllIcons) {
                input "delListYes", "button", title: "Yes", width: 6
                input "delListNo", "button", title: "No", width: 6
            }
            paragraph "<hr>"
            if(state.iconList == null || state.iconList == "") state.iconList = "Empty"
            paragraph "<b>Icon List</b><br>${state.iconList}"
        }
    }
}

def addIcon() {
    if(logEnable) log.debug "******************* addIcon - Start *******************"
    if(logEnable) log.debug "In addIcon (${state.version})"

    if(state.theList == null) state.theList = []
    newIcon = "${iconName};${iconURL}"
    checkForMatch(iconName)
    if(!match) {
        state.theList << newIcon   
        state.theList = state.theList.sort()
        state.message = "New Icon has been added"
    } else {
        state.message = "Icon was found with the same name, please use a different name for each icon"
    }
    if(logEnable) log.debug "******************* addIcon - End *******************"
    buildIconList()
}

def buildIconList() {
    if(logEnable) log.debug "******************* buildIconList - Start *******************"
    if(logEnable) log.debug "In buildIconList (${state.version})"
    if(state.theList) {           
	    def listView = "${state.theList}".split(", ")
		theList = "<table>"
    	listView.each { item -> 
            def (iconName,iconAdd) = item.split(";")
            iconName = iconName.replace("[","")
            iconAdd = iconAdd.replace("]","")
            log.debug "working on theList: ${iconName} - ${iconAdd}"
            theList += "<tr><td>${iconName}</td><td> - </td><td><img src='$iconAdd' height=30></td></tr>"
        }
        theList += "</table>"
        state.iconList = "${theList}"
    }
    if(logEnable) log.debug "******************* buildIconList - End *******************"
    sendIconList()
}

def sendIconList() {
    if(logEnable) log.debug "******************* sendIconList - Start *******************"
    if(logEnable) log.debug "In sendIconList (${state.version}) - Sending List"
    childApps.each {child -> child.masterListHandler(state.theList)}
    if(logEnable) log.debug "In sendIconList (${state.version}) - List Sent!"
    if(logEnable) log.debug "******************* sendIconList - End *******************"
}

def checkForMatch(iconName) {
    if(logEnable) log.debug "In checkForMatch (${state.version})"
    match = false
    listCount = state.theList.size()
    for(x=0;x < listCount;x++) {
        if(logEnable) log.debug "In checkForMatch - Looking for ${iconName}"
        theItem = state.theList[x]
        def (listName, listAdd) = theItem.split(";")
        if(listName == iconName) {
            if(logEnable) log.debug "In checkForMatch - Match! - listName: ${listName} vs iconName: ${iconName}"
            match = true
        } else {
            if(logEnable) log.debug "In checkForMatch - Didin't Match! - listName: ${listName} vs iconName: ${iconName}"
        }
    }
    return match
}

def delIcon() {
    if(logEnable) log.debug "******************* delIcon - Start *******************"
    if(logEnable) log.debug "In delIcon (${state.version})"
    match = false
    listCount = state.theList.size()
    for(x=0;x < listCount;x++) {
        if(logEnable) log.debug "In delIcon - Looking for ${iconName}"
        theItem = state.theList[x]
        def (listName, listAdd) = theItem.split(";")
        if(listName == iconName) {
            if(logEnable) log.debug "In delIcon - Match! - listName: ${listName} vs iconName: ${iconName}"
            state.theList.remove(state.theList[x])
            listCount = state.theList.size()
            match = true
        } else {
            if(logEnable) log.debug "In delIcon - Didin't Match! - listName: ${listName} vs iconName: ${iconName}"
        }
    }
    if(match) {
        state.message = "Icon ${iconName} has been deleted"
    } else {
        state.message = "Icon ${iconName} was not found, Icon not deleted"
    }
    if(logEnable) log.debug "******************* delIcon - End *******************"
    buildIconList()
}

def delAllIcons() {
    if(logEnable) log.debug "In delList (${state.version})"
    state.iconList = null
    state.theList = null
    state.message = "All Icons have been deleted"
    state.deleteAllIcons = false
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "addIcon"){
        addIcon()
    }
    if(state.whichButton == "delIcon"){
        delIcon()
    }
    if(state.whichButton == "delList"){
        state.message = "Are you sure you want to DELETE all icons? This can not be undone."
        state.deleteAllIcons = true
    }
    if(state.whichButton == "delListYes"){
        delAllIcons()
    }
    if(state.whichButton == "delListNo"){
        state.message = "Delete All Icons was cancelled"
        state.deleteAllIcons = false
    }
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

def getTileSettings(fromTile,toTile) {
    if(logEnable) log.debug "In getTileSettings - fromTile: ${fromTile}"
    // Get the settings from 'other' child
    childApps.each { child ->
        if(logEnable) log.debug "In getTileSettings - Checking Child: ${child.id} vs fromTile: ${fromTile}"
        if(child.id == fromTile) {
            if(logEnable) log.debug "In getTileSettings - MATCH! (${fromTile})"
            theSettings = child.sendChildSettings()
            if(logEnable) log.debug "In getTileSettings - theSettings: (${theSettings})"
        }
	} 
    // Send the settings to 'new' child
    childApps.each { child ->
        if(logEnable) log.debug "In getTileSettings - Checking Child: ${child.id} vs toTile: ${toTile}"
        if(child.id == toTile) {
            if(logEnable) log.debug "In getTileSettings - MATCH! (${toTile})"
		    child.doTheTileCopy(theSettings)
        }
	} 
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

// https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    section (getFormat("title", "${getImage("logo")}" + " Tile Master 2")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Tile Master 2 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
