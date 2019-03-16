/**
 *  ****************  Snapshot Child  ****************
 *
 *  Design Usage:
 *  Monitor lights, devices and sensors. Easily see their status right on your dashboard.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
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
 *
 *  V1.0.0 - 03/16/19 - Initial Release
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
	name: "Snapshot Child",
	namespace: "BPTWorld",
	author: "Bryan Turcotte",
	description: "Monitor lights, devices and sensors. Easily see their status right on your dashboard.",
	category: "Convenience",
	parent: "BPTWorld:Snapshot",
	iconUrl: "",
	iconX2Url: "",
	iconX3Url: "",
)

preferences {
	page(name: "pageConfig")
}

def pageConfig() {
	dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Snapshot</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "REAL TIME - Be careful with this option. Too many devices updating in real time <u>WILL</u> slow down and/or crash the hub."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Type of Trigger")) {
			input "triggerMode", "enum", required: true, title: "Select Trigger Frequency", submitOnChange: true,  options: ["Real Time", "Every X minutes", "On Demand"]
			if(triggerMode == "Real Time") {
				paragraph "<b>Be careful with this option. Too many devices updating in real time <u>WILL</u> slow down and/or crash the hub.</b>"
				input "realTimeSwitch", "capability.switch", title: "App Control Switch", required: true
			}
			if(triggerMode == "Every X minutes") {
				paragraph "<b>Choose how often to take a Snapshot of your selected devices.</b>"
				input "repeatSwitch", "capability.switch", title: "App Control Switch", required: true
				input "timeDelay", "number", title: "Every X Minutes (1 to 60)", required: true, range: '1..60'
			}
			if(triggerMode == "On Demand") {
				paragraph "<b>Only take a snapshot when this switch is turned on OR the Maintenance Reset button is pressed.</b>"
				paragraph "Recommended to create a virtual device with 'Enable auto off' set to '1s'"
				input "onDemandSwitch", "capability.switch", title: "App Control Switch", required: true
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Devices to Monitor")) {
            input "switches", "capability.switch", title: "Switches", multiple: true, required: false, submitOnChange: true
            input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false, submitOnChange: true
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Snapshot Tile'<br> - Use our 'Snapshot Tile' Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Snapshot Tile<br>- Pick a template = attribute<br>- 3rd box = snapshotSwitch or snapshotContact"
			}
		section() {
			input(name: "snapshotTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Counts to:", submitOnChange: true, required: false, multiple: false)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+"  Maintenance")) {
			paragraph "When removing devices from app, it will be necessary to reset the maps. After turning on switch, Click the button that will appear. All tables will be cleared and repopulated with the current devices."
            input(name: "maintSwitch", type: "bool", defaultValue: "false", title: "Clear all tables", description: "Clear all tables", submitOnChange: "true")
			if(maintSwitch) input "resetBtn", "button", title: "Click here to reset maps"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false, submitOnChange: true}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOGDEBUG("Updated with settings: ${settings}")
	unsubscribe()
	logCheck()
	initialize()
}

def initialize() {
	if(enableSwitch1) subscribe(enableSwitch, "switch", switchEnable)
	if(triggerMode == "On Demand") subscribe(onDemandSwitch, "switch", onDemandSwitchHandler)
	if(triggerMode == "Every X minutes") subscribe(repeatSwitch, "switch", repeatSwitchHandler)
	if(triggerMode == "Real Time") subscribe(realTimeSwitch, "switch", realTimeSwitchHandler)
}

def realTimeSwitchHandler(evt) {
	LOGDEBUG("In realTimeSwitchHandler...")
	state.realTimeSwitchStatus = evt.value
	if(state.realTimeSwitchStatus == "on") {
		LOGDEBUG("In realTimeSwitchHandler - subscribe")
		subscribe(switches, "switch", switchHandler)
		subscribe(contacts, "contact", contactHandler)
		runIn(1, maintHandler)
	} else {
		LOGDEBUG("In realTimeSwitchHandler - unsubscribe")
		unsubscribe(switches)
		unsubscribe(contacts)
	}
}

def repeatSwitchHandler(evt) {
	LOGDEBUG("In repeatSwitchHandler...")
	state.repeatSwitchStatus = repeatSwitch.currentValue("switch")
	state.runDelay = timeDelay * 60
	if(state.repeatSwitchStatus == "on") {
		maintHandler()
	}
	runIn(state.runDelay,repeatSwitchHandler)
}

def onDemandSwitchHandler(evt) {
	LOGDEBUG("In onDemandSwitchHandler...")
	state.onDemandSwitchStatus = evt.value
	if(state.onDemandSwitchStatus == "on") maintHandler()
}

def switchMapHandler() {
	LOGDEBUG("In switchMapHandler...")
	checkMaps()
	//switchMapS = state.onSwitchMap.sort {it.value}
	LOGDEBUG("In switchMapHandler - Map<br>${switchMapS}")
	
	LOGDEBUG("In switchMapHandler - Sorting Maps")
	onSwitchMapS = state.onSwitchMap.sort { a, b -> a.key <=> b.key }
	offSwitchMapS = state.offSwitchMap.sort { a, b -> a.key <=> b.key }
	
	def fOnSwitchMap = "<table width='100%'>"
	try {
		onSwitchMapS.each { stuffOn -> 
			LOGDEBUG("In switchMapHandler - Building Table ON with ${stuffOn.key}")
			fOnSwitchMap += "<tr><td style='text-align: left; width: 80%'> ${stuffOn.key}</td><td style='width: 20%'><div style='color: red;'>on</div></td></tr>"
		}
	} catch (e) {
		if(fOnSwitchMap == null) fOnSwitchMap = " Nothing to display"
	}
	fOnSwitchMap += "</table>"
	LOGDEBUG("In switchMapHandler - On Map<br>${fOnSwitchMap}")
    snapshotTileDevice.sendSnapshotSwitchOnMap(fOnSwitchMap)
	
	def fOffSwitchMap = "<table width='100%'>"
	try {
		offSwitchMapS.each { stuffOff -> 
			LOGDEBUG("In switchMapHandler - Building Table OFF with ${stuffOff.key}")
			fOffSwitchMap += "<tr><td style='text-align: left; width: 80%'> ${stuffOff.key}</td><td style='width: 20%'><div style='color: green;'>off</div></td></tr>"
		}
	} catch (e) {
		if(fOffSwitchMap == null) fOffSwitchMap = " Nothing to display"
	}
	fOffSwitchMap += "</table>"
	LOGDEBUG("In switchMapHandler - Off Map<br>${fOffSwitchMap}")
    snapshotTileDevice.sendSnapshotSwitchOffMap(fOffSwitchMap)
}

def contactMapHandler() {
	LOGDEBUG("In contactMapHandler...")
	checkMaps()
	LOGDEBUG("In switchMapHandler - Sorting Maps")
	openContactMapS = state.openContactMap.sort { a, b -> a.key <=> b.key }
	closedContactMapS = state.closedContactMap.sort { a, b -> a.key <=> b.key }
	
	// *** OPEN ***
	def fOpenContactMap = "<table width='100%'>"
	try {
		openContactMapS.each { stuffOpen -> 
			fOpenContactMap += "<tr><td style='text-align: left; width: 80%'> ${stuffOpen.key}</td><td style='width: 20%'><div style='color: red;'>open</div></td></tr>"
		}
	} catch (e) {
		if(fOpenContactMap == null) fOpenContactMap = " Nothing to display"
	}
	fOpenContactMap += "</table>"
	LOGDEBUG("In contactMapHandler...<br>${fOpenContactMap}")
    snapshotTileDevice.sendSnapshotContactOpenMap(fOpenContactMap)
	
	// *** CLOSED ***
	def fClosedContactMap = "<table width='100%'>"
	try {
		closedContactMapS.each { stuffClosed -> 
			fClosedContactMap += "<tr><td style='text-align: left; width: 80%'> ${stuffClosed.key}</td><td style='width: 20%'><div style='color: green;'>closed</div></td></tr>"
		}
	} catch (e) {
		if(fClosedContactMap == null) fClosedContactMap = " Nothing to display"
	}
	fClosedContactMap += "</table>"
	LOGDEBUG("In contactMapHandler...<br>${fClosedContactMap}")
    snapshotTileDevice.sendSnapshotContactClosedMap(fClosedContactMap)
}

def switchHandler(evt){
	def switchName = evt.displayName
	def switchStatus = evt.value
	LOGDEBUG("In switchHandler...${switchName} - ${switchStatus}")
	if(switchStatus == "on") {
		state.offSwitchMap.remove(switchName)
		state.onSwitchMap.put(switchName, switchStatus)
		LOGDEBUG("In switchHandler - ON<br>${state.onSwitchMap}")
	}
	if(switchStatus == "off") {
		state.onSwitchMap.remove(switchName)
		state.offSwitchMap.put(switchName, switchStatus)
		LOGDEBUG("In switchHandler - OFF<br>${state.offSwitchMap}")
	}
	switchMapHandler()
}

def contactHandler(evt){
	def contactName = evt.displayName
	def contactStatus = evt.value
	LOGDEBUG("In contactHandler...${contactName}: ${contactStatus}")
	if(contactStatus == "open") {
		state.closedContactMap.remove(contactName)
		state.openContactMap.put(contactName, contactStatus)
		LOGDEBUG("In contactHandler - OPEN<br>${state.openContactMap}")
	}
	if(contactStatus == "closed") {
		state.openContactMap.remove(contactName)
		state.closedContactMap.put(contactName, contactStatus)
		LOGDEBUG("In contactHandler - CLOSED<br>${state.closedContactMap}")
	}
	contactMapHandler()
}

def checkMaps() {
	if(state.offSwitchMap == null) {
		state.offSwitchMap = [:]
	}
	if(state.onSwitchMap == null) {
		state.onSwitchMap = [:]
	}
	if(state.closedContactMap == null) {
		state.closedContactMap = [:]
	}
	if(state.openContactMap == null) {
		state.openContactMap = [:]
	}
}

def maintHandler(evt){
	LOGDEBUG("In maintHandler...")
	state.offSwitchMap = [:]
	state.onSwitchMap = [:]
	state.closedContactMap = [:]
	state.openContactMap = [:] 
	LOGDEBUG("In maintHandler...Tables have been cleared!")
	LOGDEBUG("In maintHandler...Repopulating tables")
	switches.each { device ->
		def switchName = device.displayName
		def switchStatus = device.currentValue('switch')
		LOGDEBUG("In maintHandler - Working on ${switchName} - ${switchStatus}")
		if(switchStatus == "on") state.onSwitchMap.put(switchName, switchStatus)
		if(switchStatus == "off") state.offSwitchMap.put(switchName, switchStatus)
	}
	switchMapHandler()
	contacts.each { device ->
		def contactName = device.displayName
		def contactStatus = device.currentValue('contact')
		LOGDEBUG("In maintHandler - Working on ${contactName} - ${contactStatus}")
		if(contactStatus == "open") state.openContactMap.put(contactName, contactStatus)
		if(contactStatus == "closed") state.closedContactMap.put(contactName, contactStatus)
	}
	contactMapHandler()
}

def appButtonHandler(btn){  // *****************************
	// section(){input "resetBtn", "button", title: "Click here to reset maps"}
    runIn(1, maintHandler)
}  

// Normal Stuff

def pauseOrNot(){							// Modified from @Cobra Code
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

def logCheck(){								// Modified from @Cobra Code
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){							// Modified from @Cobra Code
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {						// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){				// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Snapshot - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
