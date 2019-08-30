/**
 *  Modified from Virtual Container - Copyright 2018 Stephan Hackett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	11/18/18	- added appCreateDevice(vName, vType, vSpace, vId) method for use by other smartApps. This method adds an additional parameter 'vId' that allows the smartapp to index all the children in some way.
 *				- added childList() to be used by parent smartApps to request a list of all childDevices.
 *
 * ----- End original Header -----
 *
 * ---- New Header ----
 *
 *  ****************  L360 Container Driver  ****************
 *
 *  Design Usage:
 *  For use with the 'Life360 with States' App.
 *
 *  Special thanks to @stephack for this amazing code and for allowing others to use/modify and learn from it!
 *
 * Changes:
 *
 *  v1.0.2 - 08/29/19 - App Watchdog compatible
 *  v1.0.1 - 07/26/19 - Stripped down to the bare minimum so this version only works with Life360 with States
 *                    - Changed name/namespace/author to stop any confusion between this version and the original
 *                    - Added info box, Debug Logging switch and importURL 
 *  v1.0.0 - 07/25/19 - Modified for use with Life360 with States
 *
 */

def setVersion(){
    appName = "Life360Container"
	version = "v1.0.2" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Life360 Container", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Ported/Life360/L-container-driver.groovy") {
        capability "Refresh"
        attribute "containerSize", "number"	//stores the total number of child switches created by the container
        command "createDevice", ["DEVICE LABEL", "DRIVER TYPE ", "NAMESPACE ."] //create any new Virtual Device
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
    }
}

preferences {
	input name: "about", type: "paragraph", element: "paragraph", title: "Life360 Container", description: "This driver is for use with the Life360 with States App."
    input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
}

def childList(){
	def children = getChildDevices()
    updateSize()
	return children
}

def appCreateDevice(vName, vType, vSpace, vId){
    try{
    	if(logEnable) log.debug "Attempting to create Virtual Device: Namespace: ${vSpace}, Type: ${vType}, Label: ${vName}"
		childDevice = addChildDevice(vSpace, vType, "${vId}", [label: "${vName}", isComponent: false, "vcId": "${vId}"])
    	if(logEnable) log.debug "Success from Life360 Container"
    	updateSize()
    }
    catch (Exception e){
         log.warn "Unable to create device. Please enter a valid driver type!"
    }
}

def refresh() {
	if(logEnable) log.debug "Refreshing Container values"
    updateLabels()
    updateSize()
}

def installed() {
	if(logEnable) log.debug "Installing Life360 Container"
	refresh()
}

def updated() {
    if(logEnable) log.debug "Updating Life360 Container"
	initialize()
}

def initialize() {
	if(logEnable) log.debug "Initializing Life360 Container"
	updateSize()
}

def updateSize() {
	int mySize = getChildDevices().size()
    sendEvent(name:"containerSize", value: mySize)
}

def updateLabels() { // syncs device label with componentLabel data value
    if(logEnable) log.debug "Updating Life360 Container device labels"
    def myChildren = getChildDevices()
    myChildren.each{
        if(it.label != it.data.label) {
            it.updateDataValue("label", it.label)
        }
    }
    updateSize()
}
