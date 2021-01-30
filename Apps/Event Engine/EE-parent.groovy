/**
 *  ****************  Event Engine Parent  ****************
 *
 *  Design Usage:
 *  Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!
 *
 *  Copyright 2020-2021 Bryan Turcotte (@bptworld)
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
 *  1.0.4 - 01/29/21 - Added Default Values
 *  1.0.3 - 10/18/20 - Added Global Variables
 *  1.0.2 - 09/13/20 - Bring on the Cogs!
 *  1.0.1 - 09/12/20 - Name change
 *  1.0.0 - 09/05/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Event Engine"
	state.version = "1.0.4"
}

definition(
    name:"Event Engine",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
)

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
} 

def installed() {
    //log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    //log.debug "Updated with settings: ${settings}"
    unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    initialize()
}

def initialize() {
    //childApps.each {child ->
    //	log.info "Cog: ${child.label}"
    //}
    mapOfChildren()
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Information</b>"
				paragraph "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!"
			}
            
			section(getFormat("header-green", "${getImage("Blank")}"+" Cogs")) {
				app(name: "anyOpenApp", appName: "Event Engine Cog", namespace: "BPTWorld", title: "<b>Add a new 'Cog' to Event Engine</b>", multiple: true)
			}
/*            
            section(getFormat("header-green", "${getImage("Blank")}"+" Spark Plugs")) {
				app(name: "anyOpenApp", appName: "Event Engine Spark Plug", namespace: "BPTWorld", title: "<b>Add a new 'Spark Plug' to Event Engine</b>", multiple: true)
			}
*/            
            section(getFormat("header-green", "${getImage("Blank")}"+" Default Values")) {
                paragraph "Default Values are shared across all Cogs but can be changed within each Cog if needed."
                paragraph "<b>Special Action Option</b><br>Sometimes devices can miss commands due to HE's speed. This option will allow you to adjust the time between commands being sent."
                input "pActionDelay", "number", title: "Delay (in milliseconds - 1000 = 1 second, 3 sec max)", range: '1..3000', defaultValue:100, submitOnChange:true
                paragraph "<hr>"
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Global Variables")) {
                paragraph "Global Variables can be accessed and updated by any EE Cog. A great way to share variables between Cogs!"
                paragraph "- <b>To add or edit a variable</b>, simply fill in the Name and Value below. Variable will be added as soon as you click outside the value field.<br>- <b>To delete a variable</b>, enter in the Name and flip the Add/Edit Delete switch to on."
				input "gName", "text", title: "Variable Name", required:false, width:6
                input "gValue", "text", title: "Set initial value", required:false, submitOnChange:true, width:6
                input "gvAED", "bool", title: "Add/Edit (off) or Delete (on)", description: "Add Edit Delete", defaultValue:false, submitOnChange:true, width:12
                if(gName && gvAED) {
                    gVariablesHandler("del;nothing")
                } else if(gName && gValue) {
                    gVariablesHandler("add;nothing")
                } else {
                    gVariablesHandler("refresh;nothing")
                }
                paragraph "<hr>"
                paragraph "${state.niceMap}"
                paragraph "<hr>"
			}
            
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
                input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
                if(logEnable) {
                    input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours"]
                }
 			}
			display2()
		}
	}
}

def gVariablesHandler(data) {
    if(logEnable) log.debug "In gVariablesHandler (${state.version}) - data: ${data}"
    def (theType, newData) = data.split(";")
    if(state.gvMap == null) state.gvMap = [:]
    
    if(theType == "fromChild") {
        def (cName, cValue) = newData.split(":")
        state.gvMap.put(cName,cValue)
    } else if(theType == "add") {
        state.gvMap.put(gName,gValue)
    } else if(theType == "del") {
        state.gvMap.remove(gName)
    }
      
    if(state.gvMap) {
        niceMap =  "<table width=50% align=center><tr><td><b><u>Name</u></b><td>   <td><b><u>Value</u></b>"
        def theData = "${state.gvMap}".split(",")
        theData.each { it -> 
            def (name, value) = it.split(":")
            if(name.startsWith(" ") || name.startsWith("[")) name = name.substring(1)
            value = value.replace("]","")
            niceMap += "<tr><td>${name}<td> - <td>${value}"
        }                
        niceMap += "</table>"
    }
    state.niceMap = niceMap
    
    app.removeSetting("gName")
    app.removeSetting("gValue")
    app?.updateSetting("gvAED",[value:"false",type:"bool"])
    sendToChildren()
}

def sendToChildren() {
    if(state.gvMap) childApps.each { child ->
        child.globalVariablesHandler(state.gvMap)
    }
}

def selectCogtoParent(data) {
    if(logEnable) log.debug "In selectCogtoParent (${state.version}) - data: ${data}"
    childApps.each { it ->
        if(it.label == data) {
            log.info "Match - ${data}"
            state.mySettings = it.mySettings
        }  
    }
    app.updateSetting("mySettings",[value:"${state.mySettings}",type:"text"])
}

def mapOfChildren() {
    mapOfChildren = []
    childApps.each { it ->
        if(it.label.contains("paused")) {
            
        } else {
            theName = "${it.label}"
            mapOfChildren << theName
        }
    }
    if(mapOfChildren) childApps.each { child ->
        child.newChildAppsHandler(mapOfChildren)
    }
}

def installCheck(){
    display()
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app."}
  	}
  	else{
    	if(logEnable) log.info "Parent Installed OK"
  	}
}

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
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

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
		paragraph getFormat("line")
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
