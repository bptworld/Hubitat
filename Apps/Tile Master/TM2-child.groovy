/**
 *  ****************  Tile Master 2 Child App  ****************
 *
 *  Design Usage:
 *  Create a tile with multiple devices and customization options.
 *
 *  Copyright 2019-2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  2.5.9 - 02/24/22 - Fixed a cosmetic issue
 *  2.5.8 - 10/09/21 - Adjustment
 *  2.5.7 - 07/03/21 - Minor adjustment
 *  2.5.6 - 06/21/21 - Adjustment to color options
 *  2.5.5 - 04/25/21 - Fixed an issue with the last merge.
 *  2.5.4 - 04/25/21 - Added pushbutton control (up to 4 buttons per device, only push event) - Thank you @ilkeraktuna
 *  2.5.3 - 03/27/21 - Sorted Attributes
 *  2.5.2 - 02/08/21 - Adjustment to 'Select the colors to display based on your setpoints'. Setpoints can now include negative numbers.
 *  2.5.1 - 12/20/20 - Adjustments to device value
 *  2.5.0 - 12/06/20 - Added more error catching
 *  ---
 *  1.0.0 - 02/16/19 - Initially started working on this concept.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Tile Master 2"
	state.version = "2.5.9"
}

definition(
    name: "Tile Master 2 Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create a tile with multiple devices and customization options.",
    category: "",
	parent: "BPTWorld:Tile Master 2",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Tile%20Master/TM2-child.groovy",
)

preferences {
    page name: "pageConfig"
	page name: "copyLineHandler", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Create a tile with multiple devices and customization options."
		}

        if(lineToEdit == null) lineToEdit = 1
        if(lineToEdit == 1) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device and Dashboard Tile")) {}
            section("Important Information Regarding the Virtual Device:<br><b>Please be sure to CLICK HERE and READ this section!</b>", hideable: true, hidden: true) {
                paragraph "Tile Master uses an iFrame within the table that creates the Dashboard Tile. This is the magic that makes device control possible without a new window opening up and ruining the whole experience."
                paragraph "This also has the downside of messing with the virtual device created. While the Dashboard tile isn't effected and continues to update as usual, the Virtual Device itself will not load from the Device page. You will just see a blank (white) screen and the spinning blue thing in the corner. Again, this does not effect the workings of this app or the Dashboard tile. Just the annoyance of not being able to view the device page."
                paragraph "With that said, there really is no reason to view the device page as there are no options, it's just a holding place for the Dashboard tile. But, if for any reason you do want to view the device page, I've added in a switch to turn the iFrame off."
                paragraph "What will happen if this is off?<br> - If you click a value in the Sample tile, a new window will open<br> - If you click a value in the Device page, a new window will open<br> - If you click a value in the Dashboard tile, everything should work as usual (no window opening)"
                paragraph "If you experience anything different, you should turn the iFrame back on and post on the forums. Be sure to mention the issue and what browser you are using."

                input "iFrameOff", "bool", title: "Turn iFrame off?", defaultValue:true, description: "iFrame", submitOnChange:true
                if(iFrameOff) paragraph "<div style='color: green'>iFrames are turned off, virtual device is now accessible from device menu.</div>"
                if(!iFrameOff) paragraph "<div style='color: red'>iFrames are turned on, virtual device will not load from device menu.</div>"
            }

            section() {
                paragraph "Each child app needs a virtual device to store the Tile Master data. Enter a short descriptive name for this device."
			    input "userName", "text", title: "Enter a name for this Tile Device (ie. 'House Temps' will become 'TM - House Temps')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(userName) createChildDevice()
                if(statusMessage == null) statusMessage = "Waiting on status message..."
                paragraph "${statusMessage}"
                input "tileDevice", "capability.actuator", title: "Virtual Device created to send the data to:", required:true, multiple:false
            } 
        }

        state.appInstalled = app.getInstallationState()
		if(state.appInstalled == 'COMPLETE'){
            try { parent.sendIconList() }
            catch (e) { }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Line Options")) {
                if(lineToEdit == 1) { 
                    input "howManyLines", "number", title: "How many lines on Tile (range: 1-9)", range: '1..9', width:6, submitOnChange:true
                    removeExtraLines()
                }
                theRange = "(1..$howManyLines)"
                input "lineToEdit", "number", title: "Which line to edit", range:theRange, width:6, submitOnChange:true
            
                if(state.copyToLine == null) state.copyToLine = false
                if(logEnable) log.debug "Checking for copied line - state.copyToLine: ${state.copyToLine} - lineToEdit: ${lineToEdit} - copyToNumber: ${state.copyToNumber}"

                if(state.copyToLine) {        //  ******** To complete copy **********
                    String lTE = lineToEdit
                    String cTN = state.copyToNumber
                    if(lTE == cTN) {
                        if(logEnable) log.debug "TRUE - state.copyToLine: ${state.copyToLine} - lineToEdit: ${lineToEdit} - copyToNumber: ${state.copyToNumber}"
                        paragraph "<b>******************************************************************************</b>"
                        input "getNewSettings", "bool", defaultValue: "false", title: "<b>Flip this switch to import the new line settings</b>", description: "New Settings", submitOnChange:true
                        paragraph "<b>******************************************************************************</b>"
                        if(getNewSettings) {
                            state.copyToLine = false
                            app?.updateSetting("getNewSettings",[value:"false",type:"bool"])
                            paragraph "<b>Thank you! This section will be removed with the next page refresh.<br>There is no need to flip the switch again.</b>"
                        }
                    } else {
                        if(logEnable) log.debug "FALSE - state.copyToLine: ${state.copyToLine} - lineToEdit: ${lineToEdit} - copyToNumber: ${state.copyToNumber}"
                        paragraph "<b>******************************************************************************</b>"
                        paragraph "<b>    Please go to line ${state.copyToNumber} to complete the line copy.</b>"
                        paragraph "<b>******************************************************************************</b>"
                    }
                } else {
                    href "copyLineHandler", title: "Tile Copy Options", description: "Click here for options"
                }
            }
 
            if(lineToEdit == 1) {
                section(getFormat("header-green", "${getImage("Blank")}"+" Global Style Attributes")) {
                    paragraph "Each line can can have up to 3 sections, each section can have different Style Attributes (Font Size, Text Color, Italic, Bold and Decoration). All of this adds to the Character Count. To combat this, you can choose to use Global Style Attributes. All lines and sections will start with this basic set of Attributes. Each Attribute can still be overwritten by selecting the Attribute as you move through the app."
                    paragraph "* Using Default values will save the most on character count. Defaults are set within the Dashboard."
                    input "align_G", "enum", title: "Alignment", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Left", submitOnChange: true, width: 6
                    input "color_G", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true, width: 6
                    input "fontSize_G", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:4
                    input "italic_G", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:4
                    input "bold_G", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:4
                    input "decoration_G", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                }
            }

            if((lineToEdit > 0) && (lineToEdit <= howManyLines)) {
                x = lineToEdit
                state.lastActiv = "no"

                if(!secGlobal || lineToEdit == 1) {
                    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Table Options")) {   
                        input "nSections_$x", "enum", title: "Number of Sections", required: false, multiple: false, options: ["1","2","3"], submitOnChange: true
                        nSection = app."nSections_$x"            
                        if(nSection == "1") {
                            input "secWidth_$x", "number", title: "Section 1 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:6
                        } else if(nSection == "2") {
                            input "secWidth_$x", "number", title: "Section 1 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:6
                            input "secWidtha_$x", "number", title: "Section 2 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:6
                        } else if(nSection == "3") {
                            input "secWidth_$x", "number", title: "Section 1 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:4
                            input "secWidtha_$x", "number", title: "Section 2 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:4
                            input "secWidthb_$x", "number", title: "Section 3 Percent of Total Width (1 - 100)", description: "1-100", required:false, submitOnChange: true, width:4
                        }
                    
                        secWidth = app."secWidth_$x"
                        secWidtha = app."secWidtha_$x"
                        secWidthb = app."secWidthb_$x"
                        if(secWidth == null) {secWidth = 0}
                        if(secWidtha == null) {secWidtha = 0}
                        if(secWidthb == null) {secWidthb = 0}
                        tableLength = secWidth + secWidtha + secWidthb

                        if(tableLength == 100) {
                            paragraph "Table Width: <font color='green'>${tableLength}</font><br><small>* Total table width must equal 100</small>"
                        } else {
                            paragraph "Table Width: <font color='red'>${tableLength}<br><small>* Total table width must equal 100</small></font>"
                        }
                        if(lineToEdit == 1) {
                            input "secGlobal", "bool", defaultValue: "false", title: "Makes this configuration Global", description: "configuration", submitOnChange: true
                            if(secGlobal) { state.globalSections = nSection }
                        }
                    }  
                }
                
                section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Device Control")) {
                    input "controlDevices_$x", "bool", title: "Enable Device Control? (Requires Maker API)", defaultValue:false, description: "Control Device", submitOnChange:true
                    controlDevices = app."controlDevices_$x"
                    if(controlDevices) {
                        paragraph "Please remember to add each device you would like to control into Maker API"
                    }
                }
                
                if(secGlobal) { nSection = state.globalSections }
                if(nSection == "1" || nSection == "2" || nSection == "3") {
                    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Section 1 Options")) {
                        paragraph "<b>SECTION 1</b>"
                        
                        wildcards = "Wildcards:<br>"
                        wildcards += "- %lastAct% = Use in any text field. Will be replaced with the selected devices Last Activity date/time<br>"
                        wildcards += "- %currDate% = Display the current date<br>"
                        wildcards += "- %currTime% = Display the current time (static display, does not update unless the page is updated)<br>"
                        wildcards += "- %sunset% = Display todays sunset time<br>"
                        wildcards += "- %sunrise% = Display todays sunrise time<br>"
                        wildcards += "- %wLink% = Displays a clickable web link<br>"
                        wildcards += "- %mode% = Displays the current Mode<br>"
                        
                        paragraph "${wildcards}"
                        input "wordsBEF_$x", "text", title: "Text BEFORE Device Status", required: false, submitOnChange: true, width:6
                        input "wordsAFT_$x", "text", title: "Text AFTER Device Status", required: false, submitOnChange: true, width:6

                        wordsBEF = app."wordsBEF_$x"
                        wordsAFT = app."wordsAFT_$x"
                        
                        if(wordsBEF) if(wordsBEF.toLowerCase().contains("wlink")) {
                            input "linkBEF_$x", "text", title: "<b>Text Before contains a link.</b> Enter a friendly name to display on tile.", submitOnChange:true, width:6
                            input "linkBEFL_$x", "text", title: "Link address, DO NOT include http://. This will be added automaticaly", submitOnChange:true, width:6
                            paragraph "ie. bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
                            linkBEF = app."linkBEF_$x"
                        }               
                        if(wordsAFT) if(wordsAFT.toLowerCase().contains("wlink")) {
                            input "linkAFT_$x", "text", title: "<b>Text After is a link.</b> Please enter a friendly name to display on tile.", submitOnChange:true, width:6
                            input "linkAFTL_$x", "text", title: "Link address, DO NOT include http://. This will be added automaticaly", submitOnChange:true, width:6
                            paragraph "ie. bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
                            linkAFT = app."linkAFT_$x"
                        }

                        if(wordsBEF) {if(wordsBEF.contains("lastAct")) state.lastActiv = "yes"}
                        if(wordsAFT) {if(wordsAFT.contains("lastAct")) state.lastActiv = "yes"}  

                        paragraph "<hr>"
                        input "device_$x", "capability.*", title: "Device", required:false, multiple:false, submitOnChange:true
                        
                        theDevice = app."device_$x"

                        if(theDevice) {
                            def allAtts = [:]
                            allAtts = theDevice.supportedAttributes.unique{ it.name }.collectEntries{ [(it):"${it.name}"] }
                            allAtts1 = allAtts.sort { a, b -> a.value <=> b.value }
                            if(controlDevices) paragraph "<b>Controllable device attribute include 'Switch', 'Lock', 'Push' and 'Door'</b>"
			    input "deviceAtts_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAtts1, defaultValue:state.theAtts_$x
                            deviceAtt = app."deviceAtts_$x"
                            
                            input "hideAttr_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                            hideAttr = app."hideAttr_$x"
                            
                            try{ deviceStatus = theDevice.currentValue("${deviceAtt}")}
                            catch (e) {
                                //log.error(getExceptionMessageWithLine(e))
                            }
                            if(deviceStatus == null) deviceStatus = "No Data"
                            if(deviceStatus && deviceAtt) paragraph "Current Status of Device Attribute: ${theDevice} - ${deviceAtt} - ${deviceStatus}"
                            
                            if(controlDevices && deviceAtt && !hideAttr) {
                                if(deviceAtt.toLowerCase() == "switch" || deviceAtt.toLowerCase() == "lock" || deviceAtt.toLowerCase() == "door" || deviceAtt.toLowerCase() == "pushed") { 
                                    cDevID = theDevice.id
                                    //cDevCom = theDevice.getSupportedCommands()
                                    if(parent.hubIP && parent.makerID && parent.accessToken) {
                                        input "ipORcloud_$x", "bool", title: "Use Local or Cloud control", defaultValue:false, description: "Ip or Cloud", submitOnChange:true
                                        ipORcloud = app."ipORcloud_$x"
                                        if(deviceAtt.toLowerCase() == "pushed") {
                                            deviceStatus="push"
                                            if(!ipORcloud) {
                                                controlPush1 = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/push/1?access_token=${parent.accessToken}"
                                                controlPush2 = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/push/2?access_token=${parent.accessToken}"
                                                controlPush3 = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/push/3?access_token=${parent.accessToken}"
                                                controlPush4 = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/push/4?access_token=${parent.accessToken}"
                                            } else {
                                                controlPush1 = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/push/1?access_token=${parent.accessToken}"
                                                controlPush2 = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/push/2?access_token=${parent.accessToken}"
                                                controlPush3 = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/push/3?access_token=${parent.accessToken}"
                                                controlPush4 = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/push/4?access_token=${parent.accessToken}"
                                            }
                                            input "controlPush_$x", "enum", title: "Select the PUSH Maker URL", multiple:false, options: ["$controlPush1","$controlPush2","$controlPush3","$controlPush4"], submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "switch") {
                                            if(!ipORcloud) {
                                                controlOn = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/on?access_token=${parent.accessToken}"
                                                controlOff = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/off?access_token=${parent.accessToken}"
                                            } else {
                                                controlOn = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/on?access_token=${parent.accessToken}"
                                                controlOff = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/off?access_token=${parent.accessToken}"
                                            }
                                            input "controlOn_$x", "enum", title: "Select the ON Maker URL", multiple:false, options: ["$controlOn"], submitOnChange:true
                                            input "controlOff_$x", "enum", title: "Select the OFF Maker URL", multiple:false, options: ["$controlOff"], submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "lock") {
                                            if(!ipORcloud) {
                                                controlLock = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/lock?access_token=${parent.accessToken}"
                                                controlUnlock = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/unlock?access_token=${parent.accessToken}"
                                            } else {
                                                controlLock = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/lock?access_token=${parent.accessToken}"
                                                controlUnlock = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/unlock?access_token=${parent.accessToken}"
                                            }
                                            input "controlLock_$x", "enum", title: "Select the Lock Maker URL", multiple:false, options: ["$controlLock"], submitOnChange:true
                                            input "controlUnlock_$x", "enum", title: "Select the Unlock Maker URL", multiple:false, options: ["$controlUnlock"], submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "door") {
                                            if(!ipORcloud) {
                                                controlClose = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/close?access_token=${parent.accessToken}"
                                                controlOpen = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/open?access_token=${parent.accessToken}"
                                            } else {
                                                controlClose = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/close?access_token=${parent.accessToken}"
                                                controlOpen = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/open?access_token=${parent.accessToken}"
                                            }
                                            input "controlClose_$x", "enum", title: "Select the Close Maker URL", multiple:false, options: ["$controlClose"], submitOnChange:true
                                            input "controlOpen_$x", "enum", title: "Select the Open Maker URL", multiple:false, options: ["$controlOpen"], submitOnChange:true
                                        }
                                    }
                                    
                                    paragraph "To save on character count, use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a>."
                                    input "useBitly_$x", "bool", title: "Use Bitly", defaultValue: false, description: "bitly", submitOnChange: true
                                    useBitly = app."useBitly_$x"
                                    if(useBitly) {
                                        paragraph "--------------------------------------------------------------------"
                                        paragraph "Please use the URLs provided with Bitly."
                                        if(controlPush) paragraph "Push - ${controlPush}"
					if(controlOn) paragraph "On - ${controlOn}"
                                        if(controlOff) paragraph "Off - ${controlOff}"
                                        if(controlLock) paragraph "Lock - ${controlLock}"
                                        if(controlUnlock) paragraph "Unlock - ${controlUnlock}"
                                        if(controlClose) paragraph "Close - ${controlClose}"
                                        if(controlOpen) paragraph "Open - ${controlOpen}"
                                        paragraph "--------------------------------------------------------------------"
                                        
                                        paragraph "Be sure to put 'http://' in front of the Bitly address"
                                        if(deviceAtt.toLowerCase() == "pushed") {
                                            input "bControlPush_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "switch") {
                                            input "bControlOn_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOff_$x", "text", title: "Control <b>Off</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "lock") {
                                            input "bControlLock_$x", "text", title: "Control <b>Lock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlUnlock_$x", "text", title: "Control <b>Unlock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "door") {
                                            input "bControlClose_$x", "text", title: "Control <b>Close</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOpen_$x", "text", title: "Control <b>Open</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                    }
                                }
                            }
                        }
                        paragraph "<hr>"
                        paragraph "<b>Style Attributes</b> - Using Global values will save on character counts."
                        input "overrideGlobal_$x", "bool", title: "Use Global values", defaultValue: true, description: "Global", submitOnChange: true
                        overrideGlobal1 = app."overrideGlobal_$x"
                        if(!overrideGlobal1) {
                            input "align_$x", "enum", title: "Alignment", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Left", submitOnChange: true, width: 6
                            input "vAlign_$x", "enum", title: "Vertical Alignment", required: true, multiple: false, options: ["Baseline","Top","Bottom"], defaultValue: "Baseline", submitOnChange: true, width: 6
                            input "color_$x", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true,width: 12
                            input "fontSize_$x", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:4
                            input "italic_$x", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:4
                            input "bold_$x", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:4
                            input "decoration_$x", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                        }
                        paragraph "<hr>"
                        
                        input "useColors_$x", "bool", title: "Use custom colors on device value", defaultValue: false, description: "Colors", submitOnChange: true
                        uC = app."useColors_$x"
                        if(uC) {
                            input "textORnumber_$x", "bool", title: "<b>Is device value Text or Numbers (off=text, on=numbers)</b>", defaultValue: false, description: "textORnumber", submitOnChange: true
                            textORnumber = app."textORnumber_$x"
                            
                            if(!textORnumber) {
                                paragraph "Assign colors to your attributes. Each Attribute Value must be exact. If unsure of the attribute names, visit the device in question and toggle it to see the two values."
                                paragraph "<small>COMMON PAIRS: Active-Inactive, Clear-Detected, Locked-Unlocked, On-Off, Open-Closed, Present-Not Present, Wet-Dry</small>"

                                input "color1Name_$x", "text", title: "Color 1 Attribute Value<br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "color1Value_$x", "text", title: "Color 1<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6
                                input "color2Name_$x", "text", title: "Color 2 Attribute Value<br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "color2Value_$x", "text", title: "Color 2<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6 
                                color1 = app."color1Value_$x"
                                color2 = app."color2Value_$x"                               
                            }
                            
                            if(textORnumber) {
                                paragraph "Number attributes are based on Low, Inbetween and High values. Select the colors to display based on your setpoints."
                                input "numLow_$x", "decimal", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "numHigh_$x", "decimal", title: "Number >= HIGH", submitOnChange: true, width: 6
                                if(numLow_$x == null) numLow_$x = 0
                                if(numHigh_$x == null) numHigh_$x = 0
                                
                                input "colorNumLow_$x", "text", title: "Choose a Color for Low", submitOnChange: true, width: 4
                                input "colorNum_$x", "text", title: "Choose a Color for Between", submitOnChange: true, width: 4
                                input "colorNumHigh_$x", "text", title: "Choose a Color for High", submitOnChange: true, width: 4
                            }
                        }

                        paragraph "<hr>"
                        input "valueOrCell_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                        input "useColorsBEF_$x", "bool", title: "Use custom colors on 'Text BEFORE Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                        input "useColorsAFT_$x", "bool", title: "Use custom colors on 'Text AFTER Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                        input "useIcons_$x", "bool", title: "Use custom icons instead of device value", defaultValue: false, description: "Icons", submitOnChange: true
                        uI = app."useIcons_$x"
                        if(uI) {                      
                            if(state.allIcons == null || state.allIcons == "") {
                                iconWarning = "--------------------------------------------------------------------------------<br>"
                                iconWarning += "<b>Icons must first be setup in the parent app. Thanks!</b><br>"
                                iconWarning += "--------------------------------------------------------------------------------"
                                paragraph "${iconWarning}"
                            }

                            input "textORnumber_$x", "bool", title: "<b>Is device value Text or Numbers (off=text, on=numbers)</b>", defaultValue: false, description: "textORnumber", submitOnChange: true
                            textORnumber = app."textORnumber_$x"
                            
                            if(!textORnumber) {
                                paragraph "Assign colors to your attributes. Each Attribute Value must be exact. If unsure of the attribute names, visit the device in question and toggle it to see the two values."
                                paragraph "<small>COMMON PAIRS: Active-Inactive, Clear-Detected, Locked-Unlocked, On-Off, Open-Closed, Present-Not Present, Wet-Dry</small>"
                                input "icon1Name_$x", "text", title: "Icon 1 Attribute Value<br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "useWhichIcon1_$x", "enum", title: "Choose an Icon for Value 1", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "icon2Name_$x", "text", title: "Icon 2 Attribute Value<br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "useWhichIcon2_$x", "enum", title: "Choose an Icon for Value 2", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                paragraph "<hr>"
                            }
                            
                            if(textORnumber) {
                                input "iconNumLow_$x", "decimal", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "iconNumHigh_$x", "decimal", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
                                input "useWhichIcon1_$x", "enum", title: "Choose an Icon for Low", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "useWhichIcon3_$x", "enum", title: "Choose an Icon for Between", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "useWhichIcon2_$x", "enum", title: "Choose an Icon for High", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                            }

                            icon1N = app."icon1Name_$x"
                            icon2N = app."icon2Name_$x"
                            icon3N = app."icon3Name_$x"
                            if(icon1N == null) icon1N = ""
                            if(icon2N == null) icon2N = ""
                            if(icon3N == null) icon3N = ""
                            
                            uwi1 = app."useWhichIcon1_$x"
                            uwi2 = app."useWhichIcon2_$x"
                            uwi3 = app."useWhichIcon3_$x"

                            if(uwi1 || uwi2) input "theSize_$x", "number", title: "Icon Size (30 = Default)", required: false, submitOnChange: true

                            if(uwi1) {oneSplit = uwi1.split(" - ")}     
                            if(uwi2) {twoSplit = uwi2.split(" - ")}
                            if(uwi3) {threeSplit = uwi3.split(" - ")}
                            if(uwi1) iconLink1 = "${oneSplit[1]}"
                            if(uwi2) iconLink2 = "${twoSplit[1]}"
                            if(uwi3) iconLink3 = "${threeSplit[1]}"
                            thisSize = app."theSize_$x"
                            if(thisSize == null) thisSize = 30

                            if(iconLink1 == null) {iconLink1 = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
                            if(iconLink2 == null) {iconLink2 = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
                            if(iconLink3 == null) {iconLink3 = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/blank.png"}

                            iconTable = "<table align=center width=50%><tr><td>${icon1N}<br><img src='${iconLink1}' height=${thisSize}></td><td>${icon3N}<br><img src='${iconLink3}' height=${thisSize}></td><td>${icon2N}<br><img src='${iconLink2}' height=${thisSize}></td></tr></table>"
                            paragraph "${iconTable}"
                        }
                    }
                }

                if(nSection == "2" || nSection == "3") {
                    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Section 2 Options")) {
                        paragraph "<b>SECTION 2</b>"
                        paragraph "${wildcards}"
                        input "wordsBEFa_$x", "text", title: "Text BEFORE Device Status", required: false, submitOnChange: true, width:6
                        input "wordsAFTa_$x", "text", title: "Text AFTER Device Status", required: false, submitOnChange: true, width:6

                        wordsBEFa = app."wordsBEFa_$x"
                        wordsAFTa = app."wordsAFTa_$x"
                        
                        if(wordsBEFa) if(wordsBEFa.toLowerCase().contains("wlink")) {
                            input "linkBEFa_$x", "text", title: "<b>Text Before contains a link.</b> Enter a friendly name to display on tile.", submitOnChange:true, width:6
                            input "linkBEFLa_$x", "text", title: "Link address, DO NOT include http://. This will be added automaticaly", submitOnChange:true, width:6
                            paragraph "ie. bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
                        }               
                        if(wordsAFTa) if(wordsAFTa.toLowerCase().contains("wlink")) {
                            input "linkAFTa_$x", "text", title: "<b>Text After is a link.</b> Please enter a friendly name to display on tile.", submitOnChange:true, width:6
                            input "linkAFTLa_$x", "text", title: "Link address, DO NOT include http://. This will be added automaticaly", submitOnChange:true, width:6
                            paragraph "ie. bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
                        }

                        if(wordsBEFa) {if(wordsBEFa.contains("lastAct")) state.lastActiv = "yes"}
                        if(wordsAFTa) {if(wordsAFTa.contains("lastAct")) state.lastActiv = "yes"}  

                        input "devicea_$x", "capability.*", title: "Device", required:false, multiple:false, submitOnChange:true
                        theDevicea = app."devicea_$x"

                        if(theDevicea) {
                            def allAttsa = [:]
                            allAttsa = theDevicea.supportedAttributes.unique{ it.name }.collectEntries{ [(it):"${it.name}"] }
                            allAttsaa = allAttsa.sort { a, b -> a.value <=> b.value }
                            if(controlDevices) paragraph "<b>Controllable device attribute include 'Switch', 'Lock', 'Push' and 'Door'</b>"
                            input "deviceAttsa_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAttsaa, defaultValue:state.theAttsa_$x
                            deviceAtta = app."deviceAttsa_$x"
                            
                            input "hideAttra_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                            hideAttra = app."hideAttra_$x"
                            
                            try{ deviceStatusa = theDevicea.currentValue("${deviceAtta}")}
                            catch (e) {
                                //log.error(getExceptionMessageWithLine(e))
                            }
                            log.warn "deviceStatusa: $deviceStatusa"
                            if(deviceStatusa == null) deviceStatusa = "No Data"
                            if(deviceStatusa && deviceAtta) paragraph "Current Status of Device Attribute: ${theDevicea} - ${deviceAtta} - ${deviceStatusa}"
                            
                            if(controlDevices && deviceAtta && !hideAttra) {
                                if(deviceAtta.toLowerCase() == "switch" || deviceAtta.toLowerCase() == "lock" || deviceAtta.toLowerCase() == "door" || deviceAtta.toLowerCase() == "pushed") {
                                    cDevIDa = theDevicea.id
                                    //cDevComa = theDevicea.getSupportedCommands()
                                    if(parent.hubIP && parent.makerID && parent.accessToken) {
                                        input "ipORclouda_$x", "bool", title: "Use Local or Cloud control", defaultValue:false, description: "Ip or Cloud", submitOnChange:true
                                        ipORclouda = app."ipORclouda_$x"
                                        if(deviceAtta.toLowerCase() == "pushed") {
                                            deviceStatus="push"
                                            if(!ipORclouda) {
                                                controlPush1a = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/push/1?access_token=${parent.accessToken}"
                                                controlPush2a = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/push/2?access_token=${parent.accessToken}"
                                                controlPush3a = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/push/3?access_token=${parent.accessToken}"
                                                controlPush4a = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/push/4?access_token=${parent.accessToken}"
                                            } else {
                                                controlPush1a = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/push/1?access_token=${parent.accessToken}"
                                                controlPush2a = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/push/2?access_token=${parent.accessToken}"
                                                controlPush3a = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/push/3?access_token=${parent.accessToken}"
                                                controlPush4a = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/push/4?access_token=${parent.accessToken}"
                                            }
                                            input "controlPusha_$x", "enum", title: "Select the PUSH Maker URL", multiple:false, options: ["$controlPush1a","$controlPush2a","$controlPush3a","$controlPush4a"], submitOnChange:true
                                        }
					if(deviceAtta.toLowerCase() == "switch") {
                                            if(!ipORclouda) {
                                                controlOna = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/on?access_token=${parent.accessToken}"
                                                controlOffa = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/off?access_token=${parent.accessToken}"
                                            } else {
                                                controlOna = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/on?access_token=${parent.accessToken}"
                                                controlOffa = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/off?access_token=${parent.accessToken}"
                                            }
                                            input "controlOna_$x", "enum", title: "Select the ON Maker URL", multiple:false, options: ["$controlOna"], submitOnChange:true
                                            input "controlOffa_$x", "enum", title: "Select the OFF Maker URL", multiple:false, options: ["$controlOffa"], submitOnChange:true
                                        }
                                        if(deviceAtta.toLowerCase() == "lock") {
                                            if(!ipORclouda) {
                                                controlLocka = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/lock?access_token=${parent.accessToken}"
                                                controlUnlocka = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/unlock?access_token=${parent.accessToken}"
                                            } else {
                                                controlLocka = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/lock?access_token=${parent.accessToken}"
                                                controlUnlocka = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/unlock?access_token=${parent.accessToken}"
                                            }
                                            input "controlLocka_$x", "enum", title: "Select the Lock Maker URL", multiple:false, options: ["$controlLocka"], submitOnChange:true
                                            input "controlUnlocka_$x", "enum", title: "Select the Unlock Maker URL", multiple:false, options: ["$controlUnlocka"], submitOnChange:true
                                        }
                                        if(deviceAtta.toLowerCase() == "door") {
                                            if(!ipORclouda) {
                                                controlClosea = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/close?access_token=${parent.accessToken}"
                                                controlOpena = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDa}/open?access_token=${parent.accessToken}"
                                            } else {
                                                controlClosea = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/close?access_token=${parent.accessToken}"
                                                controlOpena = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDa}/open?access_token=${parent.accessToken}"
                                            }
                                            input "controlClose_$x", "enum", title: "Select the Close Maker URL", multiple:false, options: ["$controlClosea"], submitOnChange:true
                                            input "controlOpena_$x", "enum", title: "Select the Open Maker URL", multiple:false, options: ["$controlOpena"], submitOnChange:true
                                        }

                                    }
                                    
                                    paragraph "To save on character count, use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a>."
                                    input "useBitlya_$x", "bool", title: "Use Bitly", defaultValue: false, description: "bitly", submitOnChange: true
                                    useBitlya = app."useBitlya_$x"
                                    if(useBitlya) {
                                        paragraph "--------------------------------------------------------------------"
                                        paragraph "Please use the URLs provided with Bitly."
                                        if(controlPusha) paragraph "Push - ${controlPusha}"
					if(controlOna) paragraph "On - ${controlOna}"
                                        if(controlOffa) paragraph "Off - ${controlOffa}"
                                        if(controlLocka) paragraph "Lock - ${controlLocka}"
                                        if(controlUnlocka) paragraph "Unlock - ${controlUnlocka}"
                                        if(controlClosea) paragraph "Close - ${controlClosea}"
                                        if(controlOpena) paragraph "Open - ${controlOpena}"                                        
                                        paragraph "--------------------------------------------------------------------"
                                        
                                        paragraph "Be sure to put 'http://' in front of the Bitly address"
                                        if(deviceAtta.toLowerCase() == "pushed") {
                                            input "bControlPusha_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
					if(deviceAtta.toLowerCase() == "switch") {
                                            input "bControlOna_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOffa_$x", "text", title: "Control <b>Off</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtta.toLowerCase() == "lock") {
                                            input "bControlLocka_$x", "text", title: "Control <b>Lock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlUnlocka_$x", "text", title: "Control <b>Unlock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtta.toLowerCase() == "door") {
                                            input "bControlOpena_$x", "text", title: "Control <b>Open</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlClosea_$x", "text", title: "Control <b>Close</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                    }
                                }
                            }
                        }
                        paragraph "<hr>"
                        paragraph "<b>Style Attributes</b> - Using default values will save on character counts."
                        input "overrideGlobala_$x", "bool", title: "Use Global values", defaultValue: true, description: "Global", submitOnChange: true
                        overrideGlobal1a = app."overrideGlobala_$x"
                        if(!overrideGlobal1a) {
                            input "aligna_$x", "enum", title: "Alignment", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Left", submitOnChange: true, width: 6
                            input "vAligna_$x", "enum", title: "Vertical Alignment", required: true, multiple: false, options: ["Baseline","Top","Bottom"], defaultValue: "Baseline", submitOnChange: true, width: 6
                            input "colora_$x", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true, width: 12
                            input "fontSizea_$x", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:4
                            input "italica_$x", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:4
                            input "bolda_$x", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:4
                            input "decorationa_$x", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                        }
                        paragraph "<hr>"
                        
                        input "useColorsa_$x", "bool", title: "Use custom colors on device value", defaultValue: false, description: "Colors", submitOnChange: true
                        uCa = app."useColorsa_$x"
                        if(uCa) {
                            input "textORnumbera_$x", "bool", title: "<b>Is device value Text or Numbers (off=text, on=numbers)</b>", defaultValue: false, description: "textORnumber", submitOnChange: true
                            textORnumbera = app."textORnumbera_$x"
                            
                            if(!textORnumbera) {
                                paragraph "Assign colors to your attributes. Each Attribute Value must be exact. If unsure of the attribute names, visit the device in question and toggle it to see the two values."
                                paragraph "<small>COMMON PAIRS: Active-Inactive, Clear-Detected, Locked-Unlocked, On-Off, Open-Closed, Present-Not Present, Wet-Dry</small>"
                                
                                input "color1Namea_$x", "text", title: "Color 1 Attribute Value<br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "color1Valuea_$x", "text", title: "Color 1<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6
                                input "color2Namea_$x", "text", title: "Color 2 Attribute Value<br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "color2Valuea_$x", "text", title: "Color 2<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6 
                            }
                            
                            if(textORnumbera) {
                                paragraph "Number attributes are based on Low, Inbetween and High values. Select the colors to display based on your setpoints."
                                input "numLowa_$x", "decimal", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "numHigha_$x", "decimal", title: "Number >= HIGH", submitOnChange: true, width: 6
                                if(numLowa_$x == null) numLowa_$x = 0
                                if(numHigha_$x == null) numHigha_$x = 0
                                
                                input "colorNumLowa_$x", "text", title: "Choose a Color for Low", submitOnChange: true, width: 4
                                input "colorNuma_$x", "text", title: "Choose a Color for Between", submitOnChange: true, width: 4
                                input "colorNumHigha_$x", "text", title: "Choose a Color for High", submitOnChange: true, width: 4
                            }
                        }
                        paragraph "<hr>"
                        input "valueOrCella_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                        input "useColorsBEFa_$x", "bool", title: "Use custom colors on 'Text BEFORE Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                        input "useColorsAFTa_$x", "bool", title: "Use custom colors on 'Text AFTER Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                        input "useIconsa_$x", "bool", title: "Use custom icons instead of device value", defaultValue: false, description: "Icons", submitOnChange: true
                        uIa = app."useIconsa_$x"
                        if(uIa) {                      
                            if(state.allIcons == null || state.allIcons == "") {
                                iconWarning = "--------------------------------------------------------------------------------<br>"
                                iconWarning += "<b>Icons must first be setup in the parent app. Thanks!</b><br>"
                                iconWarning += "--------------------------------------------------------------------------------"
                                paragraph "${iconWarning}"
                            }

                            input "textORnumbera_$x", "bool", title: "<b>Is device value Text or Numbers (off=text, on=numbers)</b>", defaultValue: false, description: "textORnumber", submitOnChange: true
                            textORnumbera = app."textORnumbera_$x"
                            
                            if(!textORnumbera) {
                                paragraph "Assign colors to your attributes. Each Attribute Value must be exact. If unsure of the attribute names, visit the device in question and toggle it to see the two values."
                                paragraph "<small>COMMON PAIRS: Active-Inactive, Clear-Detected, Locked-Unlocked, On-Off, Open-Closed, Present-Not Present, Wet-Dry</small>"
                                input "icon1Namea_$x", "text", title: "Icon 1 Attribute Value<br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "useWhichIcon1a_$x", "enum", title: "Choose an Icon for Value 1", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "icon2Namea_$x", "text", title: "Icon 2 Attribute Value<br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "useWhichIcon2a_$x", "enum", title: "Choose an Icon for Value 2", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                paragraph "<hr>"
                            }
                            
                            if(textORnumbera) {
                                input "iconNumLowa_$x", "decimal", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "iconNumHigha_$x", "decimal", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
                                input "useWhichIcon1a_$x", "enum", title: "Choose an Icon for Low", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "useWhichIcon3a_$x", "enum", title: "Choose an Icon for Between", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "useWhichIcon2a_$x", "enum", title: "Choose an Icon for High", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                            }

                            icon1Na = app."icon1Namea_$x"
                            icon2Na = app."icon2Namea_$x"
                            icon3Na = app."icon3Namea_$x"
                            if(icon1Na == null) icon1Na = ""
                            if(icon2Na == null) icon2Na = ""
                            if(icon3Na == null) icon3Na = ""
                        
                            uwi1a = app."useWhichIcon1a_$x"
                            uwi2a = app."useWhichIcon2a_$x"
                            uwi3a = app."useWhichIcon3a_$x"

                            if(uwi1a || uwi2a || uwi3a) input "theSizea_$x", "number", title: "Icon Size (30 = Default)", required: false, submitOnChange: true

                            if(uwi1a) {oneSplita = uwi1a.split(" - ")}    
                            if(uwi2a) {twoSplita = uwi2a.split(" - ")}
                            if(uwi3a) {threeSplita = uwi3a.split(" - ")}
                            if(uwi1a) iconLink1a = "${oneSplita[1]}"
                            if(uwi2a) iconLink2a = "${twoSplita[1]}"
                            if(uwi3a) iconLink3a = "${threeSplita[1]}"
                            thisSizea = app."theSizea_$x"
                            if(thisSizea == null) thisSizea = 30

                            if(iconLink1a == null) {iconLink1a = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
                            if(iconLink2a == null) {iconLink2a = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
                            if(iconLink3a == null) {iconLink3a = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/blank.png"}

                            iconTablea = "<table align=center width=50%><tr><td>${icon1Na}<br><img src='${iconLink1a}' height=${thisSizea}></td><td>${icon3Na}<br><img src='${iconLink3a}' height=${thisSizeb}></td><td>${icon2Na}<br><img src='${iconLink2a}' height=${thisSizea}></td></tr></table>"
                            paragraph "${iconTablea}"
                        }
                    }
                }

                if(nSection == "3") {
                    section(getFormat("header-green", "${getImage("Blank")}"+" Line $x - Section 3 Options")) {
                        paragraph "<b>SECTION 3</b>"
                        paragraph "${wildcards}"
                        input "wordsBEFb_$x", "text", title: "Text BEFORE Device Status", required: false, submitOnChange: true, width:6
                        input "wordsAFTb_$x", "text", title: "Text AFTER Device Status", required: false, submitOnChange: true, width:6

                        wordsBEFb = app."wordsBEFb_$x"
                        wordsAFTb = app."wordsAFTb_$x"
                        
                        if(wordsBEFb) if(wordsBEFb.toLowerCase().contains("wlink")) {
                            input "linkBEFb_$x", "text", title: "<b>Text Before contains a link.</b> Enter a friendly name to display on tile.", submitOnChange:true, width:6
                            input "linkBEFLb_$x", "text", title: "Link address, DO NOT include http://. This will be added automaticaly", submitOnChange:true, width:6
                            paragraph "ie. bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
                        }               
                        if(wordsAFTb) if(wordsAFTb.toLowerCase().contains("wlink")) {
                            input "linkAFTb_$x", "text", title: "<b>Text After is a link.</b> Please enter a friendly name to display on tile.", submitOnChange:true, width:6
                            input "linkAFTLb_$x", "text", title: "Link address, DO NOT include http://. This will be added automaticaly", submitOnChange:true, width:6
                            paragraph "ie. bit.ly/2m0udns<br><small>* It is highly recommended to use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a></small>"
                        }

                        if(wordsBEFb) {if(wordsBEFb.contains("lastAct")) state.lastActiv = "yes"}
                        if(wordsAFTb) {if(wordsAFTb.contains("lastAct")) state.lastActiv = "yes"}  

                        input "deviceb_$x", "capability.*", title: "Device", required:false, multiple:false, submitOnChange:true

                        theDeviceb = app."deviceb_$x"

                        if(theDeviceb) {
                            def allAttsb = [:]
                            allAttsb = theDeviceb.supportedAttributes.unique{ it.name }.collectEntries{ [(it):"${it.name}"] }
                            allAttsba = allAttsb.sort { a, b -> a.value <=> b.value }
                            if(controlDevices) paragraph "<b>Controllable device attribute include 'Switch', 'Lock', 'Push' and 'Door'</b>"
                            input "deviceAttsb_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAttsba, defaultValue:state.theAttsb_$x
                            deviceAttb = app."deviceAttsb_$x"
                                                                                                                              
                            input "hideAttrb_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                            hideAttrb = app."hideAttrb_$x"
                            
                            try { deviceStatusb = theDeviceb.currentValue("${deviceAttb}")}
                            catch (e) {
                                //log.error(getExceptionMessageWithLine(e))
                            }
                            if(deviceStatusb == null) deviceStatusb = "No Data"
                            if(deviceStatusb && deviceAttb) paragraph "Current Status of Device Attribute: ${theDeviceb} - ${deviceAttb} - ${deviceStatusb}"
                            
                            if(controlDevices && deviceAttb && !hideAttrb) {
                                if(deviceAttb.toLowerCase() == "switch" || deviceAttb.toLowerCase() == "lock" || deviceAttb.toLowerCase() == "door" || deviceAttb.toLowerCase() == "pushed") {
                                    cDevIDb = theDeviceb.id
                                    //cDevComb = theDeviceb.getSupportedCommands()
                                    if(parent.hubIP && parent.makerID && parent.accessToken) {
                                        input "ipORcloudb_$x", "bool", title: "Use Local or Cloud control", defaultValue:false, description: "Ip or Cloud", submitOnChange:true
                                        ipORcloudb = app."ipORcloudb_$x"
                                        if(deviceAttb.toLowerCase() == "pushed") {
                                            deviceStatus="push"
                                            if(!ipORcloudb) {
                                                controlPush1b = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/push/1?access_token=${parent.accessToken}"
                                                controlPush2b = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/push/2?access_token=${parent.accessToken}"
                                                controlPush3b = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/push/3?access_token=${parent.accessToken}"
                                                controlPush4b = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/push/4?access_token=${parent.accessToken}"
                                            } else {
                                                controlPush1b = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/push/1?access_token=${parent.accessToken}"
                                                controlPush2b = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/push/2?access_token=${parent.accessToken}"
                                                controlPush3b = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/push/3?access_token=${parent.accessToken}"
                                                controlPush4b = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/push/4?access_token=${parent.accessToken}"
                                            }
                                            input "controlPushb_$x", "enum", title: "Select the PUSH Maker URL", multiple:false, options: ["$controlPush1b","$controlPush2b","$controlPush3b","$controlPush4b"], submitOnChange:true
                                        }
					if(deviceAttb.toLowerCase() == "switch") {
                                            if(!ipORcloudb) {
                                                controlOnb = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/on?access_token=${parent.accessToken}"
                                                controlOffb = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/off?access_token=${parent.accessToken}"
                                            } else {
                                                controlOnb = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/on?access_token=${parent.accessToken}"
                                                controlOffb = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/off?access_token=${parent.accessToken}"
                                            }
                                            input "controlOnb_$x", "enum", title: "Select the ON Maker URL", multiple:false, options: ["$controlOnb"], submitOnChange:true
                                            input "controlOffb_$x", "enum", title: "Select the OFF Maker URL", multiple:false, options: ["$controlOffb"], submitOnChange:true
                                        }
                                        if(deviceAttb.toLowerCase() == "lock") {
                                            if(!ipORcloudb) {
                                                controlLockb = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/lock?access_token=${parent.accessToken}"
                                                controlUnlockb = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevIDb}/unlock?access_token=${parent.accessToken}"
                                            } else {
                                                controlLockb = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/lock?access_token=${parent.accessToken}"
                                                controlUnlockb = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevIDb}/unlock?access_token=${parent.accessToken}"
                                            }
                                            input "controlLockb_$x", "enum", title: "Select the Lock Maker URL", multiple:false, options: ["$controlLockb"], submitOnChange:true
                                            input "controlUnlockb_$x", "enum", title: "Select the Unlock Maker URL", multiple:false, options: ["$controlUnlockb"], submitOnChange:true
                                        }
                                        if(deviceAttb.toLowerCase() == "door") {
                                            if(!ipORcloud) {
                                                controlCloseb = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/close?access_token=${parent.accessToken}"
                                                controlOpenb = "http://${parent.hubIP}/apps/api/${parent.makerID}/devices/${cDevID}/open?access_token=${parent.accessToken}"
                                            } else {
                                                controlCloseb = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/close?access_token=${parent.accessToken}"
                                                controlOpenb = "https://cloud.hubitat.com/api/${parent.cloudToken}/apps/${parent.makerID}/devices/${cDevID}/open?access_token=${parent.accessToken}"
                                            }
                                            input "controlCloseb_$x", "enum", title: "Select the Close Maker URL", multiple:false, options: ["$controlCloseb"], submitOnChange:true
                                            input "controlOpenb_$x", "enum", title: "Select the Open Maker URL", multiple:false, options: ["$controlOpenb"], submitOnChange:true
                                        }
                                    }
                                    
                                    paragraph "To save on character count, use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a>."
                                    input "useBitlyb_$x", "bool", title: "Use Bitly", defaultValue: false, description: "bitly", submitOnChange: true
                                    useBitlyb = app."useBitlyb_$x"
                                    if(useBitlyb) {
                                        paragraph "--------------------------------------------------------------------"
                                        paragraph "Please use the URLs provided with Bitly."
                                        if(controlPushb) paragraph "Push - ${controlPushb}"
					if(controlOnb) paragraph "On - ${controlOnb}"
                                        if(controlOffb) paragraph "Off - ${controlOffb}"
                                        if(controlLockb) paragraph "Lock - ${controlLockb}"
                                        if(controlUnlockb) paragraph "Unlock - ${controlUnlockb}"
                                        if(controlCloseb) paragraph "Close - ${controlCloseb}"
                                        if(controlOpenb) paragraph "Open - ${controlOpenb}"                                        
                                        paragraph "--------------------------------------------------------------------"
                                        
                                        paragraph "Be sure to put 'http://' in front of the Bitly address"
                                        if(deviceAttb.toLowerCase() == "pushed") {
                                            input "bControlPushb_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
					if(deviceAttb.toLowerCase() == "switch") {
                                            input "bControlOnb_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOffb_$x", "text", title: "Control <b>Off</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAttb.toLowerCase() == "lock") {
                                            input "bControlLockb_$x", "text", title: "Control <b>Lock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlUnlockb_$x", "text", title: "Control <b>Unlock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAttb.toLowerCase() == "door") {
                                            input "bControlCloseb_$x", "text", title: "Control <b>Close</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOpenb_$x", "text", title: "Control <b>Open</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                    }
                                }
                            }
                        }
                        paragraph "<hr>"
                        paragraph "<b>Style Attributes</b> - Using default values will save on character counts."
                        input "overrideGlobalb_$x", "bool", title: "Use Global values", defaultValue: true, description: "Global", submitOnChange: true
                        overrideGlobal1b = app."overrideGlobalb_$x"
                        if(!overrideGlobal1b) {
                            input "alignb_$x", "enum", title: "Alignment", required: true, multiple: false, options: ["Left","Center","Right"], defaultValue: "Left", submitOnChange: true, width: 6
                            input "vAlignb_$x", "enum", title: "Vertical Alignment", required: true, multiple: false, options: ["Baseline","Top","Bottom"], defaultValue: "Baseline", submitOnChange: true, width: 6
                            input "colorb_$x", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true, width: 12
                            input "fontSizeb_$x", "number", title: "Font Size (0 = Default)", required: true, defaultValue: "0", submitOnChange: true, width:4
                            input "italicb_$x", "bool", defaultValue: "false", title: "Italic", description: "italic", submitOnChange: true, width:4
                            input "boldb_$x", "bool", defaultValue: "false", title: "Bold", description: "bold", submitOnChange: true, width:4
                            input "decorationb_$x", "enum", title: "Decoration (None = Default)", required: true, multiple: false, options: ["None","overline","line-through","underline","underline overline"], defaultValue: "None", submitOnChange: true, width: 6
                        }
                        paragraph "<hr>"
                        
                        input "useColorsb_$x", "bool", title: "Use custom colors on device value", defaultValue: false, description: "Colors", submitOnChange: true
                        uCb = app."useColorsb_$x"
                        if(uCb) {
                            input "textORnumberb_$x", "bool", title: "<b>Is device value Text or Numbers (off=text, on=numbers)</b>", defaultValue: false, description: "textORnumber", submitOnChange: true
                            textORnumberb = app."textORnumberb_$x"
                            
                            if(!textORnumberb) {
                                paragraph "Assign colors to your attributes. Each Attribute Value must be exact. If unsure of the attribute names, visit the device in question and toggle it to see the two values."
                                paragraph "<small>COMMON PAIRS: Active-Inactive, Clear-Detected, Locked-Unlocked, On-Off, Open-Closed, Present-Not Present, Wet-Dry</small>"
                                
                                input "color1Nameb_$x", "text", title: "Color 1 Attribute Value<br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "color1Valueb_$x", "text", title: "Color 1<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6
                                input "color2Nameb_$x", "text", title: "Color 2 Attribute Value<br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "color2Valueb_$x", "text", title: "Color 2<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6 
                            }
                            
                            if(textORnumberb) {
                                paragraph "Number attributes are based on Low, Inbetween and High values. Select the colors to display based on your setpoints."
                                input "numLowb_$x", "decimal", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "numHighb_$x", "decimal", title: "Number >= HIGH", submitOnChange: true, width: 6
                                if(numLowb_$x == null) numLowb_$x = 0
                                if(numHighb_$x == null) numHighb_$x = 0
                                
                                input "colorNumLowb_$x", "text", title: "Choose a Color for Low", submitOnChange: true, width: 4
                                input "colorNumb_$x", "text", title: "Choose a Color for Between", submitOnChange: true, width: 4
                                input "colorNumHighb_$x", "text", title: "Choose a Color for High", submitOnChange: true, width: 4
                            }                          
                        }
                        paragraph "<hr>"
                        input "valueOrCellb_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                        input "useColorsBEFb_$x", "bool", title: "Use custom colors on 'Text BEFORE Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                        input "useColorsAFTb_$x", "bool", title: "Use custom colors on 'Text AFTER Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                        input "useIconsb_$x", "bool", title: "Use custom icons instead of device value", defaultValue: false, description: "Icons", submitOnChange: true
                        uIb = app."useIconsb_$x"
                        if(uIb) {                      
                            if(state.allIcons == null || state.allIcons == "") {
                                iconWarning = "--------------------------------------------------------------------------------<br>"
                                iconWarning += "<b>Icons must first be setup in the parent app. Thanks!</b><br>"
                                iconWarning += "--------------------------------------------------------------------------------"
                                paragraph "${iconWarning}"
                            }

                            input "textORnumberb_$x", "bool", title: "<b>Is device value Text or Numbers (off=text, on=numbers)</b>", defaultValue: false, description: "textORnumber", submitOnChange: true
                            textORnumberb = app."textORnumberb_$x"
                            
                            if(!textORnumberb) {
                                paragraph "Assign colors to your attributes. Each Attribute Value must be exact. If unsure of the attribute names, visit the device in question and toggle it to see the two values."
                                paragraph "<small>COMMON PAIRS: Active-Inactive, Clear-Detected, Locked-Unlocked, On-Off, Open-Closed, Present-Not Present, Wet-Dry</small>"
                                input "icon1Nameb_$x", "text", title: "Icon 1 Attribute Value<br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "useWhichIcon1b_$x", "enum", title: "Choose an Icon for Value 1", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "icon2Nameb_$x", "text", title: "Icon 2 Attribute Value<br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "useWhichIcon2b_$x", "enum", title: "Choose an Icon for Value 2", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                paragraph "<hr>"
                            }
                            
                            if(textORnumberb) {
                                input "iconNumLowb_$x", "decimal", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "iconNumHighb_$x", "decimal", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
                                input "useWhichIcon1b_$x", "enum", title: "Choose an Icon for Low", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "useWhichIcon3b_$x", "enum", title: "Choose an Icon for Between", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                                input "useWhichIcon2b_$x", "enum", title: "Choose an Icon for High", required:false, multiple:false, submitOnChange:true, options:state.allIcons
                            }

                            icon1Nb = app."icon1Nameb_$x"
                            icon2Nb = app."icon2Nameb_$x"
                            icon3Nb = app."icon3Nameb_$x"
                            if(icon1Nb == null) icon1Nb = ""
                            if(icon2Nb == null) icon2Nb = ""
                            if(icon3Nb == null) icon3Nb = ""
                        
                            uwi1b = app."useWhichIcon1b_$x"
                            uwi2b = app."useWhichIcon2b_$x"
                            uwi3b = app."useWhichIcon3b_$x"

                            if(uwi1b || uwi2b || uwi3b) input "theSizeb_$x", "number", title: "Icon Size (30 = Default)", required: false, submitOnChange: true

                            if(uwi1b) {oneSplitb = uwi1b.split(" - ")}    
                            if(uwi2b) {twoSplitb = uwi2b.split(" - ")}
                            if(uwi3b) {threeSplitb = uwi3b.split(" - ")}
                            if(uwi1b) iconLink1b = "${oneSplitb[1]}"
                            if(uwi2b) iconLink2b = "${twoSplitb[1]}"
                            if(uwi3b) iconLink3b = "${threeSplitb[1]}"
                            thisSizeb = app."theSizeb_$x"
                            if(thisSizeb == null) thisSizeb = 30

                            if(iconLink1b == null) {iconLink1b = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
                            if(iconLink2b == null) {iconLink2b = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
                            if(iconLink3b == null) {iconLink3b = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/blank.png"}

                            iconTableb = "<table align=center width=50%><tr><td>${icon1Nb}<br><img src='${iconLink1b}' height=${thisSizeb}></td><td>${icon3Nb}<br><img src='${iconLink3b}' height=${thisSizeb}></td><td>${icon2Nb}<br><img src='${iconLink2b}' height=${thisSizeb}></td></tr></table>"
                            paragraph "${iconTableb}"
                        }
                    }
                }
            }
    
            if(state.lastActiv == "yes") {
                section(getFormat("header-green", "${getImage("Blank")}"+" Last Activity Formatting")) {
                    input "dateTimeFormat", "enum", title: "Select Formatting", required: true, multiple: false, submitOnChange: true, options: [
                        ["f1":"MMM dd, yyy - h:mm:ss a"],
                        ["f2":"dd MMM, yyy - h:mm:ss a"],
                        ["f3":"MMM dd - h:mm:ss a (12 hour)"],
                        ["f4":"dd MMM - h:mm:ss a (12 hour)"],
                        ["f3a":"MMM dd - h:mm a (12 hour)"],
                        ["f4a":"dd MMM - h:mm a (12 hour)"],
                        ["f5":"MMM dd - HH:mm (24 hour)"],
                        ["f6":"dd MMM - HH:mm (24 hour)"],
                        ["f7":"h:mm:ss a (12 hour)"],
                        ["f8":"HH:mm:ss (24 hour)"],
                    ]
                }
            }
    
            section(getFormat("header-green", "${getImage("Blank")}"+" Current Date / Time Formatting")) {
                if(wordsBEF && wordsBEF.contains("%currDate%") || 
                   wordsAFT && wordsAFT.contains("%currDate%") ||
                   wordsBEFa && wordsBEFa.contains("%currDate%") ||
                   wordsAFTa && wordsAFTa.contains("%currDate%") ||
                   wordsBEFb && wordsBEFb.contains("%currDate%") ||
                   wordsAFTb && wordsAFTb.contains("%currDate%")) {
                    input "cDateFormat", "enum", title: "Select Formatting", required: true, multiple: false, submitOnChange: true, options: [
                        ["cd1":"MMM dd, yyy"],
                        ["cd2":"dd MMM, yyy"],
                        ["cd3":"MMM dd"],
                        ["cd4":"dd MMM"],
                    ], width:6
                } else {
                    paragraph "No Date formatting required.", width:6
                }
                if(wordsBEF && wordsBEF.contains("%currTime%") ||
                   wordsAFT && wordsAFT.contains("%currTime%") ||
                   wordsBEFa && wordsBEFa.contains("%currTime%") ||
                   wordsAFTa && wordsAFTa.contains("%currTime%") ||
                   wordsBEFb && wordsBEFb.contains("%currTime%") ||
                   wordsAFTb && wordsAFTb.contains("%currTime%") ||
                   wordsBEF && wordsBEF.contains("%sunrise%") ||
                   wordsAFT && wordsAFT.contains("%sunrise%") ||
                   wordsBEFa && wordsBEFa.contains("%sunrise%") ||
                   wordsAFTa && wordsAFTa.contains("%sunrise%") ||
                   wordsBEFb && wordsBEFb.contains("%sunrise%") ||
                   wordsAFTb && wordsAFTb.contains("%sunrise%") ||
                   wordsBEF && wordsBEF.contains("%sunset%") ||
                   wordsAFT && wordsAFT.contains("%sunset%") ||
                   wordsBEFa && wordsBEFa.contains("%sunset%") ||
                   wordsAFTa && wordsAFTa.contains("%sunset%") ||
                   wordsBEFb && wordsBEFb.contains("%sunset%") ||
                   wordsAFTb && wordsAFTb.contains("%sunset%")) {
                    input "cTimeFormat", "enum", title: "Select Formatting", required: true, multiple: false, submitOnChange: true, options: [
                        ["ct1":"h:mm:ss a (12 hour)"],
                        ["ct2":"HH:mm:ss (24 hour)"],
                        ["ct3":"h:mm a (12 hour)"],
                        ["ct4":"HH:mm (24 hour)"],
                    ], width:6
                } else {
                    paragraph "No Time formatting required.", width:6
                }
            }
        }

        if(lineToEdit == 1) {
            section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: true}
            if(state.appInstalled != 'COMPLETE') {
                section() {
                    paragraph "<hr>"
                    paragraph "<b>At this point, please press 'Done' to save the app. Then reopen it from the menu to complete the setup. This is required to retrieve the Icons and Colors from the parent app (if needed). Thanks.</b>"
                }
            }
        }

        if(state.appInstalled == 'COMPLETE') {tileHandler("bottom")}
        section() {
           input "logEnable", "bool", defaultValue: "false", title: "Enable Debug Logging", description: "debugging", submitOnChange: true
        }
		display2()
	}
}

def copyLineHandler() {
    dynamicPage(name: "copyLineHandler", title: "", install:false, uninstall:false) {
		display()
        state.theMessage = ""
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Line Options")) {
            paragraph "<b>Copy one tile line to another tile line!</b><br>This will overwrite all settings on the receiving line with the settings of the 'from' line."
            paragraph "<b>Note:</b> Devices will not be carried over to the new line. This may cause errors in the log but most are harmless and can be ignored. IE. If you used %lastAct% in the line, the device associated with it won't carry over, so it will cause an error until a new device is added."
        }
        section() {
            theRange = "(1..$howManyLines)"
            input "fromLine", "number", title: "<b>From</b> Line Number", range: theRange, submitOnChange:true, width:6
            input "toLine", "number", title: "<b>To</b> Line Number", range: theRange, submitOnChange:true, width:6
            
            if(fromLine && toLine) {
                input "copyLine", "bool", defaultValue: "false", title: "Copy Line Now", description: "Copy Line Now", submitOnChange: true
                if(copyLine) doTheLineCopy()
            }
            paragraph "${state.theMessage}"          
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Tile Options")) {
            paragraph "<hr>"
            paragraph "<b>This option has always had problems... sometimes it works, sometimes it doesn't! Working on a different way of doing this but no eta at this time. Thanks!</b>"
            paragraph "<hr>"
            paragraph "<b>Copy another tile to this tile!</b><br>This will overwrite all settings on this tile with the settings of the 'from' tile."
            paragraph "When the copy switch is turned on:<br> - It will only take a few seconds<br> - When complete the switch will turn off<br> - The app number will be blank<br> - At this point you can press 'Next'"
            paragraph "<b>Note:</b> Devices will be carried over to the new tile but the attribute will have to be re-selected. This may cause errors in the log, just remember to go into each line and make sure the device attribute is set to the correct attribute."
        }
        section() {
            input "fromTile", "number", title: "Tile App Number to Copy <b>From</b>", submitOnChange:true, width:6
            
            if(fromTile) {
                input "copyTile", "bool", defaultValue: "false", title: "Copy Tile Now", description: "Copy Tile Now", submitOnChange: true
                if(copyTile) requestTileCopy()
            }
            paragraph "${state.theMessage}"
        }
    }
}

def insertLine() {
    if(logEnable) log.info "In insertLine (${state.version})"
    
    log.info "Settings: ${settings}"
}

def doTheLineCopy() {
    if(logEnable) log.info "In doTheLineCopy (${state.version})"
    
    c = "${fromLine}"
    toThisLine = "${toLine}"
    
    settings.each { theOption ->
        name = theOption.key
        
        if(name.contains("_${c}")) { 
            newName = name.replace("_${c}", "_${toThisLine}")
            nameValue = theOption.value

            if(name.contains("italic") || name.contains("bold") || name.contains("controlDevices") || name.contains("hideAttr") || name.contains("useBitly") || name.contains("useColors") || name.contains("textORnumber") || name.contains("valueOrCell") || name.contains("useColorsBEF") || name.contains("useColorsAFT")) { 
                app.updateSetting("${newName}",[type:"bool",value:nameValue])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValue} - type: bool"
            } else if(name.contains("useWhichIcon1") || name.contains("useWhichIcon2") || name.contains("useWhichIcon3") || name.contains("nSections") || name.contains("decoration") || name.contains("align") || name.contains("vAlign") || name.contains("ipORcloud")) {
                app.updateSetting("${newName}",[type:"enum",value:nameValue])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValue} - type: enum"                 
            } else if(name.contains("device_") && !name.contains("tileDevice")) {
                theDev = app."device_$c"
                nameId = theDev.id   
                app.updateSetting("${newName}",[type:"capability",value:[nameId]])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValue} - type: capability"  
            } else if(name.contains("devicea_") && !name.contains("tileDevice")) {
                theDeva = app."devicea_$c"
                nameIda = theDeva.id   
                app.updateSetting("${newName}",[type:"capability",value:[nameIda]])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValuea} - type: capabilitya"  
            } else if(name.contains("deviceb_") && !name.contains("tileDevice")) {
                theDevb = app."deviceb_$c"
                nameIdb = theDevb.id   
                app.updateSetting("${newName}",[type:"capability",value:[nameIdb]])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValueb} - type: capabilityb"  
            } else if(name.contains("deviceAtts_")) {
                state.theAtts_$c = app."deviceAtts_$c"
                app.updateSetting("${newName}",[type:"enum",value:state.theAtts_$c])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${state.theAtts_$c} - type: Atts enum  ***"
            } else if(name.contains("deviceAttsa_")) {
                state.theAttsa_$c = app."deviceAttsa_$c"
                app.updateSetting("${newName}",[type:"enum",value:state.theAttsa_$c])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${state.theAttsa_$c} - type: Attsa enum  ***"
            } else if(name.contains("deviceAttsb_")) {
                state.theAttsb_$c = app."deviceAttsb_$c"
                app.updateSetting("${newName}",[type:"enum",value:state.theAttsb_$c])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${state.theAttsb_$c} - type: Attsb enum  ***"
            } else {
                app.updateSetting("${newName}",[type:"text",value:nameValue])
                if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValue} - type: text"
            }     
        }
    }
    
    state.copyToLine = true
    state.copyToNumber = "${toLine}"
    if(logEnable) log.info "In doTheCopy - Finished (${state.copyToLine})"
    state.theMessage = "<b>Line Settings have been copied. Hit 'Next' to continue</b>"
    app?.updateSetting("copyLine",[value:"false",type:"bool"])
    app?.updateSetting("fromLine",[value:"",type:"number"])
    app?.updateSetting("toLine",[value:"",type:"number"])
}

def removeExtraLines() {
    if(logEnable) log.info "In removeExtraLines (${state.version})"
    
    if(howManyLines) {
        hml = howManyLines + 1
        for(d=hml;d <= 9;d++) {
            app.removeSetting("nSections_$d"); app.removeSetting("secWidth_$d"); app.removeSetting("secWidtha_$d"); app.removeSetting("secWidthb_$d")
            app.removeSetting("controlDevices_$d")

            app.removeSetting("wordsBEF_$d"); app.removeSetting("wordsBEFa_$d"); app.removeSetting("wordsBEFb_$d")
            app.removeSetting("wordsAFT_$d"); app.removeSetting("wordsAFTa_$d"); app.removeSetting("wordsAFTb_$d")

            app.removeSetting("device_$d"); app.removeSetting("devicea_$d"); app.removeSetting("deviceb_$d")
            app.removeSetting("deviceAtts_$d"); app.removeSetting("deviceAttsa_$d"); app.removeSetting("deviceAttsb_$d")
            app.removeSetting("controlPush_$d"); app.removeSetting("controlPusha_$d"); app.removeSetting("controlPushb_$d")     
            app.removeSetting("hideAttr_$d"); app.removeSetting("hideAttra_$d"); app.removeSetting("hideAttrb_$d")
            app.removeSetting("controlOn_$d"); app.removeSetting("controlOna_$d"); app.removeSetting("controlOnb_$d")
            app.removeSetting("controlOff_$d"); app.removeSetting("controlOffa_$d"); app.removeSetting("controlOffb_$d")
            app.removeSetting("controlLock_$d"); app.removeSetting("controlLocka_$d"); app.removeSetting("controlLockb_$d")
            app.removeSetting("controlUnlock_$d"); app.removeSetting("controlUnlocka_$d"); app.removeSetting("controlUnlockb_$d")
            app.removeSetting("controlClose_$d"); app.removeSetting("controlClosea_$d"); app.removeSetting("controlCloseb_$d")
            app.removeSetting("controlOpen_$d"); app.removeSetting("controlOpena_$d"); app.removeSetting("controlOpenb_$d")
            app.removeSetting("useBitly_$d"); app.removeSetting("useBitlya_$d"); app.removeSetting("useBitlyb_$d")
            app.removeSetting("bcontrolPush_$d"); app.removeSetting("bcontrolPusha_$d"); app.removeSetting("bcontrolPushb_$d") 
	    app.removeSetting("bControlOn_$d"); app.removeSetting("bControlOna_$d"); app.removeSetting("bControlOnb_$d")
            app.removeSetting("bControlOff_$d"); app.removeSetting("bControlOffa_$d"); app.removeSetting("bControlOffb_$d")
            app.removeSetting("bControlLock_$d"); app.removeSetting("bControlLocka_$d"); app.removeSetting("bControlLockb_$d")
            app.removeSetting("bControlUnLock_$d"); app.removeSetting("bControlUnLocka_$d"); app.removeSetting("bControlUnLockb_$d")
            app.removeSetting("bControlClose_$d"); app.removeSetting("bControlClosea_$d"); app.removeSetting("bControlCloseb_$d")
            app.removeSetting("bControlOpen_$d"); app.removeSetting("bControlOpena_$d"); app.removeSetting("bControlOpenb_$d")


            app.removeSetting("overrideGlobal_$d"); app.removeSetting("overrideGlobala_$d"); app.removeSetting("overrideGlobalb_$d")        
            app.removeSetting("align_$d"); app.removeSetting("aligna_$d"); app.removeSetting("alignb_$d")  
            app.removeSetting("vAlign_$d"); app.removeSetting("vAligna_$d"); app.removeSetting("vAlignb_$d")
            app.removeSetting("color_$d"); app.removeSetting("colora_$d"); app.removeSetting("colorb_$d")      
            app.removeSetting("fontSize_$d"); app.removeSetting("fontSizea_$d"); app.removeSetting("fontSizeb_$d")
            app.removeSetting("italic_$d"); app.removeSetting("italica_$d"); app.removeSetting("italicb_$d")
            app.removeSetting("bold_$d"); app.removeSetting("bolda_$d"); app.removeSetting("boldb_$d")
            app.removeSetting("decoration_$d"); app.removeSetting("decorationa_$d"); app.removeSetting("decorationb_$d")

            app.removeSetting("useColors_$d"); app.removeSetting("useColorsa_$d"); app.removeSetting("useColorsb_$d")
            app.removeSetting("textORnumber_$d"); app.removeSetting("textORnumbera_$d"); app.removeSetting("textORnumberb_$d")

            app.removeSetting("color1Name_$d"); app.removeSetting("color1Namea_$d"); app.removeSetting("color1Nameb_$d")
            app.removeSetting("color1Value_$d"); app.removeSetting("color1Valuea_$d"); app.removeSetting("color1Valueb_$d")
            app.removeSetting("color2Name_$d"); app.removeSetting("color2Namea_$d"); app.removeSetting("color2Nameb_$d")
            app.removeSetting("color2Value_$d"); app.removeSetting("color2Valuea_$d"); app.removeSetting("color2Valueb_$d")

            app.removeSetting("valueOrCell_$d"); app.removeSetting("valueOrCella_$d"); app.removeSetting("valueOrCellb_$d")

            app.removeSetting("useColorsBEF_$d"); app.removeSetting("useColorsBEFa_$d"); app.removeSetting("useColorsBEFb_$d")
            app.removeSetting("useColorsAFT_$d"); app.removeSetting("useColorsAFTa_$d"); app.removeSetting("useColorsAFTb_$d")
            app.removeSetting("numLow_$d"); app.removeSetting("numLowa_$d"); app.removeSetting("numLowb_$d")
            app.removeSetting("numHigh_$d"); app.removeSetting("numHigha_$d"); app.removeSetting("numHighb_$d")
            app.removeSetting("colorNumLow_$d"); app.removeSetting("colorNumLowa_$d"); app.removeSetting("colorNumLowb_$d")
            app.removeSetting("colorNumHigh_$d"); app.removeSetting("colorNumHigha_$d"); app.removeSetting("colorNumHighb_$d")

            app.removeSetting("useIcons_$d"); app.removeSetting("useIconsa_$d"); app.removeSetting("useIconsb_$d")
            app.removeSetting("icon1Name_$d"); app.removeSetting("icon1Namea_$d"); app.removeSetting("icon1Nameb_$d")
            app.removeSetting("useWhichIcon1_$d"); app.removeSetting("useWhichIcon1a_$d"); app.removeSetting("useWhichIcon1b_$d")
            app.removeSetting("icon2Name_$d"); app.removeSetting("icon2Namea_$d"); app.removeSetting("icon2Nameb_$d")
            app.removeSetting("useWhichIcon2_$d"); app.removeSetting("useWhichIcon2a_$d"); app.removeSetting("useWhichIcon2b_$d")
            app.removeSetting("iconNumLow_$d"); app.removeSetting("iconNumLowa_$d"); app.removeSetting("iconNumLowb_$d")
            app.removeSetting("iconNumHigh_$d"); app.removeSetting("iconNumHigha_$d"); app.removeSetting("iconNumHighb_$d")
            app.removeSetting("useWhichIcon1_$d"); app.removeSetting("useWhichIcon1a_$d"); app.removeSetting("useWhichIcon1b_$d")
            app.removeSetting("useWhichIcon2_$d"); app.removeSetting("useWhichIcon2a_$d"); app.removeSetting("useWhichIcon2b_$d")
            app.removeSetting("useWhichIcon3_$d"); app.removeSetting("useWhichIcon3a_$d"); app.removeSetting("useWhichIcon3b_$d")
            app.removeSetting("theSize_$d"); app.removeSetting("theSizea_$d"); app.removeSetting("theSizeb_$d")
        }
    }
}

def requestTileCopy() {             // this is sent to the parent app
    if(logEnable) log.info "In requestTileCopy (${state.version})"
    toTile = app.id
    parent.getTileSettings(fromTile,toTile)
}

def sendChildSettings() {           // this is then requested from the parent app
    if(logEnable) log.info "In sendChildSettings (${state.version})"   
    childAppSettings = settings
}

def doTheTileCopy(newSettings) {    // and finally the parent app sends the settings!
    if(logEnable) log.info "In doTheTileCopy (${state.version})"
    if(copyTile) {
        if(logEnable) log.info "In doTheTileCopy - Received: ${newSettings}"       
        newSettings.each { theOption ->
            name = theOption.key
            nameValue = theOption.value
            if(name == "tileDevice" || name == "userName" || name == "copyTile" || name == "fromTile" || name == "logEnable") {
                if(logEnable) log.info "In doTheTileCopy - name: ${name}, so skipping"
            } else {
                if(name.contains("italic") || name.contains("bold") || name.contains("controlDevices") || name.contains("hideAttr") || name.contains("useBitly") || name.contains("useColors") || name.contains("textORnumber") || name.contains("valueOrCell") || name.contains("useColorsBEF") || name.contains("useColorsAFT") || name.contains("secGlobal") || name.contains("overrideGlobal")) { 
                    app.updateSetting("${name}",[type:"bool",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: bool"
                } else if(name.contains("useWhichIcon1") || name.contains("useWhichIcon2") || name.contains("useWhichIcon3") || name.contains("nSections") || name.contains("decoration") || name.contains("align") || name.contains("vAlign")) {
                    app.updateSetting("${name}",[type:"enum",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: enum"
                } else if(name.contains("howManyLines") || name.contains("secWidth")) {
                    app.updateSetting("${name}",[type:"number",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: number"
                } else if(name.contains("device_") && !name.contains("tileDevice")) {
                    nameId = nameValue.id   
                    app.updateSetting("${name}",[type:"capability",value:[nameId]])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - nameId: ${nameId} - type: capability"  
                } else if(name.contains("devicea_") && !name.contains("tileDevice")) {
                    nameIda = nameValue.id   
                    app.updateSetting("${name}",[type:"capability",value:[nameIda]])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - nameId: ${nameId} - type: capabilitya"  
                } else if(name.contains("deviceb_") && !name.contains("tileDevice")) {
                    nameIdb = nameValue.id   
                    app.updateSetting("${name}",[type:"capability",value:[nameIdb]])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - nameId: ${nameId} - type: capabilityb"  
                } else if(name.contains("deviceAtts_")) {
                    app.updateSetting("${name}",[type:"enum",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: Atts enum  ***"
                } else if(name.contains("deviceAttsa_")) {
                    app.updateSetting("${name}",[type:"enum",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: Attsa enum  ***"
                } else if(name.contains("deviceAttsb_")) {
                    app.updateSetting("${name}",[type:"enum",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: Attsb enum  ***"
                } else {
                    app.updateSetting("${name}",[type:"text",value:nameValue])
                    if(logEnable) log.info "In doTheLineCopy - name: ${name} - nameValue: ${nameValue} - type: text"
                }     
            }
        }

        if(logEnable) log.info "In doTheTileCopy - Finished"
        state.theMessage = "<b>Tile Settings have been copied. Hit 'Next' to continue</b>"
        app?.updateSetting("copyTile",[value:"false",type:"bool"])
        app?.updateSetting("fromTile",[value:"",type:"number"])
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
    setDefaults()
    
    for(z=1;z <= howManyLines;z++) {
        theDev = app."device_$z"
        theAtt = app."deviceAtts_$z"
        if(theDev) subscribe(theDev, theAtt, tileHandler)
        
        theDeva = app."devicea_$z"
        theAtta = app."deviceAttsa_$z"
        if(theDeva) subscribe(theDeva, theAtta, tileHandler)
        
        theDevb = app."deviceb_$z"
        theAttb = app."deviceAttsb_$z"
        if(theDevb) subscribe(theDevb, theAttb, tileHandler)
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def tileHandler(evt){
    if(logEnable) log.debug "*************************************** In tileHandler - Start ***************************************"
	if(logEnable) log.debug "In tileHandler (${state.version})"
    
    for(y=1;y <= howManyLines;y++) {
        if(logEnable) log.debug "<b>**********  Starting Line $y  **********</b>"
        if(!secGlobal) nSections = app."nSections_$y"
           
        if(secGlobal && y == 1) nSections = app."nSections_$y"
        
        theDevice = app."device_$y"
        theDevicea = app."devicea_$y"
        theDeviceb = app."deviceb_$y"
        
        if(theDevice) deviceAtts = app."deviceAtts_$y"
        if(theDevicea) deviceAttsa = app."deviceAttsa_$y"
        if(theDeviceb) deviceAttsb = app."deviceAttsb_$y"
        
        useColors = app."useColors_$y"
        useColorsa = app."useColorsa_$y"
        useColorsb = app."useColorsb_$y"
        
        textORnumber = app."textORnumber_$y"
        textORnumbera = app."textORnumbera_$y"
        textORnumberb = app."textORnumberb_$y"
        
        color1Name = app."color1Name_$y"
        color1Namea = app."color1Namea_$y"
        color1Nameb = app."color1Nameb_$y"
        color1Value = app."color1Value_$y"
        color1Valuea = app."color1Valuea_$y"
        color1Valueb = app."color1Valueb_$y"
        
        color2Name = app."color2Name_$y"
        color2Namea = app."color2Namea_$y"
        color2Nameb = app."color2Nameb_$y"
        color2Value = app."color2Value_$y"
        color2Valuea = app."color2Valuea_$y"
        color2Valueb = app."color2Valueb_$y"
        
        numLow = app."numLow_$y"
        numLowa = app."numLowa_$y"
        numLowb = app."numLowb_$y"
        numHigh = app."numHigh_$y"
        numHigha = app."numHigha_$y"
        numHighb = app."numHighb_$y"
        
        colorNumLow = app."colorNumLow_$y"
        colorNumLowa = app."colorNumLowa_$y"
        colorNumLowb = app."colorNumLowb_$y"
        
        colorNum = app."colorNum_$y"
        colorNuma = app."colorNuma_$y"
        colorNumb = app."colorNumb_$y"
        
        colorNumHigh = app."colorNumHigh_$y"
        colorNumHigha = app."colorNumHigha_$y"
        colorNumHighb = app."colorNumHighb_$y"
        
        valueOrCell = app."valueOrCell_$y"
        valueOrCella = app."valueOrCella_$y"
        valueOrCellb = app."valueOrCellb_$y"
        useColorsBEF = app."useColorsBEF_$y"
        useColorsBEFa = app."useColorsBEFa_$y"
        useColorsBEFb = app."useColorsBEFb_$y"
        useColorsAFT = app."useColorsAFT_$y"
        useColorsAFTa = app."useColorsAFTa_$y"
        useColorsAFTb = app."useColorsAFTb_$y"
        wordsBEF = app."wordsBEF_$y"
        wordsBEFa = app."wordsBEFa_$y"
        wordsBEFb = app."wordsBEFb_$y"
        wordsAFT = app."wordsAFT_$y"
        wordsAFTa = app."wordsAFTa_$y"
        wordsAFTb = app."wordsAFTb_$y"
        useIcons = app."useIcons_$y"
        useIconsa = app."useIconsa_$y"
        useIconsb = app."useIconsb_$y"
        if(useIcons) iconSize = app."theSize_$y"
        if(useIconsa) iconSizea = app."theSizea_$y"
        if(useIconsb) iconSizeb = app."theSizeb_$y"
        controlDevices = app."controlDevices_$y"
        controlDevicesa = app."controlDevices_$y"
        controlDevicesb = app."controlDevices_$y"
        
        if(useIcons) {
            icon1Name = app."icon1Name_$y"
            icon2Name = app."icon2Name_$y"
            icon3Name = app."icon3Name_$y"
            iconNumLow = app."iconNumLow_$y"
            iconNumHigh = app."iconNumHigh_$y"
            
            uwi1 = app."useWhichIcon1_$y"
            uwi2 = app."useWhichIcon2_$y"
            uwi3 = app."useWhichIcon3_$y"

            if(uwi1) {oneSplit = uwi1.split(" - ")}     
            if(uwi2) {twoSplit = uwi2.split(" - ")}
            if(uwi3) {threeSplit = uwi3.split(" - ")}
            if(uwi1) iconLink1 = "${oneSplit[1]}"
            if(uwi2) iconLink2 = "${twoSplit[1]}"
            if(uwi3) iconLink3 = "${threeSplit[1]}"

            if(iconLink1 == null) {iconLink1 = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
            if(iconLink2 == null) {iconLink2 = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
            if(iconLink3 == null) {iconLink3 = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/blank.png"}
        }
        if(useIconsa) {
            icon1Namea = app."icon1Namea_$y"
            icon2Namea = app."icon2Namea_$y"
            icon3Namea = app."icon3Namea_$y"
            iconNumLowa = app."iconNumLowa_$y"
            iconNumHigha = app."iconNumHigha_$y"
            
            uwi1a = app."useWhichIcon1a_$y"
            uwi2a = app."useWhichIcon2a_$y"
            uwi3a = app."useWhichIcon3a_$y"

            if(uwi1a) {oneSplita = uwi1a.split(" - ")}     
            if(uwi2a) {twoSplita = uwi2a.split(" - ")}
            if(uwi3a) {threeSplita = uwi3a.split(" - ")}
            if(uwi1a) iconLink1a = "${oneSplita[1]}"
            if(uwi2a) iconLink2a = "${twoSplita[1]}"
            if(uwi3a) iconLink3a = "${threeSplita[1]}"

            if(iconLink1a == null) {iconLink1a = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
            if(iconLink2a == null) {iconLink2a = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
            if(iconLink3a == null) {iconLink3a = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/blank.png"}
        }
        if(useIconsb) {
            icon1Nameb = app."icon1Nameb_$y"
            icon2Nameb = app."icon2Nameb_$y"
            icon3Nameb = app."icon3Nameb_$y"
            iconNumLowb = app."iconNumLowb_$y"
            iconNumHighb = app."iconNumHighb_$y"
            
            uwi1b = app."useWhichIcon1b_$y"
            uwi2b = app."useWhichIcon2b_$y"
            uwi3b = app."useWhichIcon3b_$y"

            if(uwi1b) {oneSplitb = uwi1b.split(" - ")}     
            if(uwi2b) {twoSplitb = uwi2b.split(" - ")}
            if(uwi3b) {threeSplitb = uwi3b.split(" - ")}
            if(uwi1b) iconLink1b = "${oneSplitb[1]}"
            if(uwi2b) iconLink2b = "${twoSplitb[1]}"
            if(uwi3b) iconLink3b = "${threeSplitb[1]}"

            if(iconLink1b == null) {iconLink1b = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
            if(iconLink2b == null) {iconLink2b = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/logo.png"}
            if(iconLink3b == null) {iconLink3b = "https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/blank.png"}
        }
        
	    if(nSections >= "1") {
            if(logEnable) log.debug "<b>In tileHandler - Line: ${y} - Section: 1</b>"
		    if(theDevice) {
                if(logEnable) log.debug "*********** theDevice: ${theDevice} - deviceAtts: ${deviceAtts} ***********"
                try{ if(deviceAtts) deviceStatus = theDevice.currentValue("${deviceAtts}") }
                catch (e) {}
                if(deviceStatus == null) deviceStatus = "No Data"
                if(deviceAtts == "pushed") deviceStatus = "push"
                if(!valueOrCell || useIcons) {
                    getStatusColors(theDevice, deviceStatus, deviceAtts, useColors, textORnumber, color1Name, color1Value, color2Name, color2Value, numLow, numHigh, colorNumLow, colorNum, colorNumHigh, useColorsBEF, useColorsAFT, wordsBEF, wordsAFT, useIcons, iconSize, iconLink1, iconLink2, iconLink3, icon1Name, icon2Name,iconNumLow, iconNumHigh)
                    def (deviceStatus1,wordsBEF1,wordsAFT1) = theStatusCol.split(",")
                    if(logEnable) log.debug "In tileHandler - deviceStatus1: ${deviceStatus1} - wordsBEF1: ${wordsBEF1} - wordsAFT1: ${wordsAFT1}"
                    if(deviceStatus1 != "null") deviceStatus = deviceStatus1
                    if(wordsBEF1 != "null") wordsBEF = wordsBEF1
                    if(wordsAFT1 != "null") wordsAFT = wordsAFT1
                } else {
                    getCellColors(deviceStatus, deviceAtts, textORnumber, color1Name, color1Value, color2Name, color2Value, numLow, numHigh, colorNumLow, colorNum, colorNumHigh)
                    cellColor = theCellColor
                }
            } else {
                if(logEnable) log.debug "In tileHander Line: ${y}-1 - No device found - theDevice: ${theDevice}"
                deviceStatus = ""
            }
	    }

	    if(nSections >= "2") {
            if(logEnable) log.debug "<b>In tileHandler - Line: ${y} - Section: 2</b>"
		    if(theDevicea) {
                try{ if(deviceAttsa) deviceStatusa = theDevicea.currentValue("${deviceAttsa}") }
                catch (e) {}
		if(deviceStatusa == null) deviceStatusa = "No Data"
                if(deviceAttsa == "pushed") deviceStatusa = "push"
                if(!valueOrCella || useIconsa) {
                    getStatusColors(theDevicea, deviceStatusa, deviceAttsa, useColorsa, textORnumbera, color1Namea, color1Valuea, color2Namea, color2Valuea, numLowa, numHigha, colorNumLowa, colorNuma, colorNumHigha, useColorsBEFa, useColorsAFTa, wordsBEFa, wordsAFTa, useIconsa, iconSizea, iconLink1a, iconLink2a, iconLink3a, icon1Namea, icon2Namea,iconNumLowa, iconNumHigha)
                    def (deviceStatus1a,wordsBEF1a,wordsAFT1a) = theStatusCol.split(",")
                    if(logEnable) log.debug "In tileHandler - a - deviceStatus1a: ${deviceStatus1a} - wordsBEF1a: ${wordsBEF1a} - wordsAFT1a: ${wordsAFT1a}"
                    if(deviceStatus1a != "null") deviceStatusa = deviceStatus1a
                    if(wordsBEF1a != "null") wordsBEFa = wordsBEF1a
                    if(wordsAFT1a != "null") wordsAFTa = wordsAFT1a
                } else {
                    getCellColors(deviceStatusa, deviceAttsa, textORnumbera, color1Namea, color1Valuea, color2Namea, color2Valuea, numLowa, numHigha, colorNumLowa, colorNuma, colorNumHigha)
                    cellColora = theCellColor
                }
		    } else {
                if(logEnable) log.debug "In tileHander Line: ${y}-2 - No device found - theDevicea: ${theDevicea}"
                deviceStatusa = ""
            }
	    }
        
	    if(nSections == "3") {
            if(logEnable) log.debug "<b>In tileHandler - Line: ${y} - Section: 3</b>"
		    if(theDeviceb) {
                try{ if(deviceAttsb) deviceStatusb = theDeviceb.currentValue("${deviceAttsb}") }
                catch (e) {}
		if(deviceStatusb == null) deviceStatusb = "No Data"
                if(deviceAttsb == "pushed") deviceStatusb = "push"
		if(!valueOrCellb || useIconsb) {
                    getStatusColors(theDeviceb, deviceStatusb, deviceAttsb, useColorsb, textORnumberb, color1Nameb, color1Valueb, color2Nameb, color2Valueb, numLowb, numHighb, colorNumLowb, colorNumb, colorNumHighb, useColorsBEFb, useColorsAFTb, wordsBEFb, wordsAFTb, useIconsb, iconSizeb, iconLink1b, iconLink2b, iconLink3b, icon1Nameb, icon2Nameb,iconNumLowb, iconNumHighb)
                    def (deviceStatus1b,wordsBEF1b,wordsAFT1b) = theStatusCol.split(",")
                    if(logEnable) log.debug "In tileHandler - b - deviceStatus1b: ${deviceStatus1b} - wordsBEF1b: ${wordsBEF1b} - wordsAFT1b: ${wordsAFT1b}"
                    if(deviceStatus1b != "null") deviceStatusb = deviceStatus1b
                    if(wordsBEF1b != "null") wordsBEFb = wordsBEF1b
                    if(wordsAFT1b != "null") wordsAFTb = wordsAFT1b
                } else {
                    getCellColors(deviceStatusb, deviceAttsb, textORnumberb, color1Nameb, color1Valueb, color2Nameb, color2Valueb, numLowb, numHighb, colorNumLowb, colorNumb, colorNumHighb)
                    cellColorb = theCellColor
                }
		    } else {
                if(logEnable) log.debug "In tileHander Line: ${y}-3 - No device found - theDeviceb: ${theDeviceb}"
                deviceStatusb = ""
            }
	    }
	
// ***** Make the table for line x	*****
        if(logEnable) log.debug "<b>In tileHander - Making the table for line ${y}</b>"
        theTileMap = ""
        
        align_G = app."align_G"
        color_G = app."color_G"
        fontSize_G = app."fontSize_G"
        italic_G = app."italic_G"
        bold_G = app."bold_G"
        decoration_G = app."decoration_G"
        
        styleGlobal = app."overrideGlobal_$y"
        styleGlobala = app."overrideGlobala_$y"
        styleGlobalb = app."overrideGlobalb_$y"

        align = app."align_$y"
        vAlign = app."vAlign_$y"
        color = app."color_$y"
        fontSize = app."fontSize_$y"
        italic = app."italic_$y"
        bold = app."bold_$y"
        decoration = app."decoration_$y"
        hideAttr = app."hideAttr_$y"
        linkBEF = app."linkBEF_$y"
        linkAFT = app."linkAFT_$y"
        linkBEFL = app."linkBEFL_$y"
        linkAFTL = app."linkAFTL_$y"
        useBitly = app."useBitly_$y"
        
        if(!useBitly && deviceAtts == "pushed") controlOn = app."controlPush_$y"
        if(!useBitly && deviceAtts == "pushed") controlOff = app."controlPush_$y"
        if(useBitly && deviceAtts == "pushed") controlOn = app."bControlPush_$y"
        if(useBitly && deviceAtts == "pushed") controlOff = app."bControlPush_$y"
	
	if(!useBitly && deviceAtts == "switch") controlOn = app."controlOn_$y"
        if(!useBitly && deviceAtts == "switch") controlOff = app."controlOff_$y"
        if(useBitly && deviceAtts == "switch") controlOn = app."bControlOn_$y"
        if(useBitly && deviceAtts == "switch") controlOff = app."bControlOff_$y"
        
        if(!useBitly && deviceAtts == "lock") controlOn = app."controlLock_$y"
        if(!useBitly && deviceAtts == "lock") controlOff = app."controlUnlock_$y"
        if(useBitly && deviceAtts == "lock") controlOn = app."bControlLock_$y"
        if(useBitly && deviceAtts == "lock") controlOff = app."bControlUnlock_$y"
        
        if(!useBitly && deviceAtts == "door") controlOn = app."controlClose_$y"
        if(!useBitly && deviceAtts == "door") controlOff = app."controlOpen_$y"
        if(useBitly && deviceAtts == "door") controlOn = app."bControlClose_$y"
        if(useBitly && deviceAtts == "door") controlOff = app."bControlOpen_$y"

        aligna = app."aligna_$y"
        vAligna = app."vAligna_$y"
        colora = app."colora_$y"
        fontSizea = app."fontSizea_$y"
        italica = app."italica_$y"
        bolda = app."bolda_$y"
        decorationa = app."decorationa_$y"
        hideAttra = app."hideAttra_$y"
        linkBEFa = app."linkBEFa_$y"
        linkAFTa = app."linkAFTa_$y"
        linkBEFLa = app."linkBEFLa_$y"
        linkAFTLa = app."linkAFTLa_$y"
        useBitlya = app."useBitlya_$y"
        
        if(!useBitly && deviceAtts == "pushed") controlOna = app."controlPusha_$y"
        if(!useBitly && deviceAtts == "pushed") controlOffa = app."controlPusha_$y"
        if(useBitly && deviceAtts == "pushed") controlOna = app."bControlPusha_$y"
        if(useBitly && deviceAtts == "pushed") controlOffa = app."bControlPusha_$y"

	if(!useBitlya && deviceAttsa == "switch") controlOna = app."controlOna_$y"
        if(!useBitlya && deviceAttsa == "switch") controlOffa = app."controlOffa_$y"
        if(useBitlya && deviceAttsa == "switch") controlOna = app."bControlOna_$y"
        if(useBitlya && deviceAttsa == "switch") controlOffa = app."bControlOffa_$y"
        
        if(!useBitlya && deviceAttsa == "lock") controlOna = app."controlLocka_$y"
        if(!useBitlya && deviceAttsa == "lock") controlOffa = app."controlUnlocka_$y"
        if(useBitlya && deviceAttsa == "lock") controlOna = app."bControlLocka_$y"
        if(useBitlya && deviceAttsa == "lock") controlOffa = app."bControlUnlocka_$y"
        
        if(!useBitlya && deviceAttsa == "door") controlOna = app."controlClosea_$y"
        if(!useBitlya && deviceAttsa == "door") controlOffa = app."controlOpena_$y"
        if(useBitlya && deviceAttsa == "door") controlOna = app."bControlClosea_$y"
        if(useBitlya && deviceAttsa == "door") controlOffa = app."bControlOpena_$y"

        alignb = app."alignb_$y"
        vAlignb = app."vAlignb_$y"
        colorb = app."colorb_$y"
        fontSizeb = app."fontSizeb_$y"
        italicb = app."italicb_$y"
        boldb = app."boldb_$y"
        decorationb = app."decorationb_$y"
        hideAttrb = app."hideAttrb_$y"
        linkBEFb = app."linkBEFb_$y"
        linkAFTb = app."linkAFTb_$y"
        linkBEFLb = app."linkBEFLb_$y"
        linkAFTLb = app."linkAFTLb_$y"
        cDevBEFidb = app."cDeviceBEFidb_$y"
        useBitlyb = app."useBitlyb_$y"
        
        if(!useBitly && deviceAtts == "pushed") controlOnb = app."controlPushb_$y"
        if(!useBitly && deviceAtts == "pushed") controlOffb = app."controlPushb_$y"
        if(useBitly && deviceAtts == "pushed") controlOnb = app."bControlPushb_$y"
        if(useBitly && deviceAtts == "pushed") controlOffb = app."bControlPushb_$y"
	
	if(!useBitlyb && deviceAttsb == "switch") controlOnb = app."controlOnb_$y"
        if(!useBitlyb && deviceAttsb == "switch") controlOffb = app."controlOffb_$y"
        if(useBitlyb && deviceAttsb == "switch") controlOnb = app."bControlOnb_$y"
        if(useBitlyb && deviceAttsb == "switch") controlOffb = app."bControlOffb_$y"
        
        if(!useBitlyb && deviceAttsb == "lock") controlOnb = app."controlLockb_$y"
        if(!useBitlyb && deviceAttsb == "lock") controlOffb = app."controlUnlockb_$y"
        if(useBitlyb && deviceAttsb == "lock") controlOnb = app."bControlLockb_$y"
        if(useBitlyb && deviceAttsb == "lock") controlOffb = app."bControlUnlockb_$y"
        
        if(!useBitlyb && deviceAttsb == "door") controlOnb = app."controlCloseb_$y"
        if(!useBitlyb && deviceAttsb == "door") controlOffb = app."controlOpenb_$y"
        if(useBitlyb && deviceAttsb == "door") controlOnb = app."bControlCloseb_$y"
        if(useBitlyb && deviceAttsb == "door") controlOffb = app."bControlOpenb_$y"

        theGlobalStyle = ""
        if(align_G) theGlobalStyle += "text-align:${align_G};"
        if(color_G != "Default") theGlobalStyle += "color:${color_G};"
        if(fontSize_G != 0) theGlobalStyle += "font-size:${fontSize_G}px;"
        if(italic_G) theGlobalStyle += "font-style:italic;"
        if(bold_G) theGlobalStyle += "font-weight:bold;"
        if(decoration_G != "None") theGlobalStyle += "text-decoration:${decoration_G};"
        if(logEnable) log.debug "In tileHander - theGlobalStyle: ${stheGlobalStyle}"
        
        sec1Style = ""
        if(align) sec1Style += "text-align:${align};"
        if(vAlign != "Baseline") sec1Style += "vertical-align:${vAlign};"
        if(color != "Default") sec1Style += "color:${color};"
        if(fontSize != 0) sec1Style += "font-size:${fontSize}px;"
        if(italic) sec1Style += "font-style:italic;"
        if(bold) sec1Style += "font-weight:bold;"
        if(decoration != "None") sec1Style += "text-decoration:${decoration};"
        if(logEnable) log.debug "In tileHander - sec1Style: ${sec1Style}"
        
        sec2Style = ""
        if(aligna) sec2Style += "text-align:${aligna};"
        if(vAligna != "Baseline") sec2Style += "vertical-align:${vAligna};"
        if(colora != "Default") sec2Style += "color:${colora};"
        if(fontSizea != 0) sec2Style += "font-size:${fontSizea}px;"
        if(italica) sec2Style += "font-style:italic;"
        if(bolda) sec2Style += "font-weight:bold;"
        if(decorationa != "None") sec2Style += "text-decoration:${decorationa};"
        if(logEnable) log.debug "In tileHander - sec2Style: ${sec2Style}"
        
        sec3Style = ""
        if(alignb) sec3Style += "text-align:${alignb};"
        if(vAlignb != "Baseline") sec3Style += "vertical-align:${vAlignb};"
        if(colorb != "Default") sec3Style += "color:${colorb};"
        if(fontSizeb != 0) sec3Style += "font-size:${fontSizeb}px;"
        if(italicb) sec3Style += "font-style:italic;"
        if(boldb) sec3Style += "font-weight:bold;"
        if(decorationb != "None") sec3Style += "text-decoration:${decorationb};"
        if(logEnable) log.debug "In tileHander - sec3Style: ${sec3Style}"
        
        // Looking into adding CSS to place manually in dashboard

        secWidth = app."secWidth_$y"
        secWidtha = app."secWidtha_$y"
        secWidthb = app."secWidthb_$y"

        if(secGlobal && y == 1) theStyle = "style='width:${secWidth}%;"
        if(secGlobal && y != 1) theStyle = "style='"
        
        if(!secGlobal) theStyle = "style='width:${secWidth}%;"
        
        if(!styleGlobal) {     
            theStyle += "${sec1Style}"
        } else {
            theStyle += "${theGlobalStyle}"
        }
        if(valueOrCell) theStyle += "background:${cellColor};"
        
        theStyle += "'"
        if(logEnable) log.debug "In tileHander - theStyle: ${theStyle}"
        
        if(secGlobal && y == 1) theStylea = "style='width:${secWidtha}%;"
        if(secGlobal && y != 1) theStylea = "style='"
        
        if(!secGlobal) theStylea = "style='width:${secWidtha}%;"
        
        if(!styleGlobala) {
            theStylea += "${sec2Style}"
        } else {
            theStylea += "${theGlobalStyle}"
        }
        if(valueOrCella) theStylea += "background:${cellColora};"
        
        theStylea += "'"
        if(logEnable) log.debug "In tileHander - theStylea: ${theStylea}"
        
        if(secGlobal && y == 1) theStyleb = "style='width:${secWidthb}%;"
        if(secGlobal && y != 1) theStyleb = "style='"
        
        if(!secGlobal) theStyleb = "style='width:${secWidthb}%;"
        
        if(!styleGlobalb) {
            theStyleb += "${sec3Style}"
        } else {
            theStyleb += "${theGlobalStyle}"
        }
        if(valueOrCellb) theStyleb += "background:${cellColorb};"
        
        theStyleb += "'"
        if(logEnable) log.debug "In tileHander - theStyleb: ${theStyleb}"
        
// ********** Make the lines/table **********
        if(logEnable) log.debug "Start Make the Lines/Table - line: ${y} - secGlobal: ${secGlobal} - theTileMap: ${theTileMap} - nSections: ${nSections}"
        
        if(!secGlobal) {
            theTileMap = "<table style='width:100%'><tr>"
        } else {
            if(y == 1) theTileMap = "<table style='width:100%'><tr>"
            if(y != 1) theTileMap = "<tr>"
        }
            if(nSections >= "1") {
                theTileMap += "<td $theStyle>"
                makeTileLine(theDevice, wordsBEF, linkBEF, linkBEFL, wordsAFT, linkAFT, linkAFTL, controlOn, controlOff, deviceStatus, controlDevices, deviceAtts, hideAttr)
                theTileMap += "${newWords2}"
            } 
            if(nSections >= "2") {
                theTileMap += "<td $theStylea>"
                makeTileLine(theDevicea, wordsBEFa, linkBEFa, linkBEFLa, wordsAFTa, linkAFTa, linkAFTLa, controlOna, controlOffa, deviceStatusa, controlDevicesa, deviceAttsa, hideAttra)
                theTileMap += "${newWords2}"
            }
            if(nSections == "3") {
                theTileMap += "<td $theStyleb>"
                makeTileLine(theDeviceb, wordsBEFb, linkBEFb, linkBEFLb, wordsAFTb, linkAFTb, linkAFTLb, controlOnb, controlOffb, deviceStatusb, controlDevicesb, deviceAttsb, hideAttrb)
                theTileMap += "${newWords2}"
            }
        
        if(!secGlobal) {
            theTileMap += "</table>"
        } else {
            if(y < howManyLines) theTileMap += "</tr>"
            if(y == howManyLines) theTileMap += "</tr></table>"
        }
        
        if(logEnable) log.debug "End Make the Lines/Table - line: ${y} - secGlobal: ${secGlobal} - theTileMap: ${theTileMap} - nSections: ${nSections}"
// ********** End Make the lines/table **********
        
        if(y == 1) {
            state.theTile_1 = theTileMap
            state.theTileLength_1 = theTileMap.length()
        }
        if(y == 2) {
            state.theTile_2 = theTileMap
            state.theTileLength_2 = theTileMap.length()
        }
        if(y == 3) {
            state.theTile_3 = theTileMap
            state.theTileLength_3 = theTileMap.length()
        }
        if(y == 4) {
            state.theTile_4 = theTileMap
            state.theTileLength_4 = theTileMap.length()
        }
        if(y == 5) {
            state.theTile_5 = theTileMap
            state.theTileLength_5 = theTileMap.length()
        }
        if(y == 6) {
            state.theTile_6 = theTileMap
            state.theTileLength_6 = theTileMap.length()
        }
        if(y == 7) {
            state.theTile_7 = theTileMap
            state.theTileLength_7 = theTileMap.length()
        }
        if(y == 8) {
            state.theTile_8 = theTileMap
            state.theTileLength_8 = theTileMap.length()
        }
        if(y == 9) {
            state.theTile_9 = theTileMap
            state.theTileLength_9 = theTileMap.length()
        }
        
        if(logEnable) log.debug "In tileHandler - Line: ${y} - theTileMap: ${theTileMap}"
        if(logEnable) log.debug "*************************************** In tileHandler - End ***************************************"
    }
    sampleTileHandler(evt)
}

def makeTileLine(theDevice, wordsBEF, linkBEF, linkBEFL, wordsAFT, linkAFT, linkAFTL, controlOn, controlOff, deviceStatus, controlDevices, deviceAtts, hideAttr) {
    if(logEnable) log.debug "In makeTileLine (${state.version}) - theDevice: ${theDevice} - deviceAtts: ${deviceAtts} - hideAttr: ${hideAttr} - deviceStatus: ${deviceStatus}"
    if(logEnable) log.debug "In makeTileLine (${state.version}) - wordsBEF: ${wordsBEF} - linkBEF: ${linkBEF} - linkBEFL: ${linkBEFL}"
    if(logEnable) log.debug "In makeTileLine (${state.version}) - wordsAFT: ${wordsAFT} - linkAFT: ${linkAFT} - linkAFTL: ${linkAFTL}"
    if(logEnable) log.debug "In makeTileLine (${state.version}) - controlOn: ${controlOn} - controlOff: ${controlOff} - controlDevices: ${controlDevices}"
    newWords2 = ""
    
    if(!hideAttr) {
        if(controlDevices && (deviceAtts == "switch" || deviceAtts == "lock" || deviceAtts == "door" || deviceAtts == "pushed")) { 
            if(theDevice) {
		if(deviceAtts == "pushed") cStatus = "push"
		if(deviceAtts == "switch") cStatus = theDevice.currentValue("switch")
                if(deviceAtts == "lock") cStatus = theDevice.currentValue("lock")
                if(deviceAtts == "door") cStatus = theDevice.currentValue("door")            

                if (cStatus == "push") {
                    controlLink = "<a href=${controlOn} target=a>$deviceStatus</a>"
                }
                else if (cStatus == "on" || cStatus == "locked" || cStatus == "closed") {
                    controlLink = "<a href=${controlOff} target=a>$deviceStatus</a>"
                } else {
                    controlLink = "<a href=${controlOn} target=a>$deviceStatus</a>"
                }
            
                if(logEnable) log.debug "In makeTileLine ** - controlLink: ${controlLink} - deviceStatus: ${deviceStatus}"
            } else {
                deviceStatus = ""
                controlLink = ""
            }
        } else {
            controlLink = deviceStatus
        }
    } else {
        controlLink = ""
    }
    
    if(wordsBEF == null) wordsBEF = ""
    if(wordsAFT == null) wordsAFT = ""
    if(controlLink == null) controlLink = ""
    words = "${wordsBEF}" + "${controlLink}" + "${wordsAFT}"
    if(logEnable) log.debug "In makeTileLine - words: ${words}"
    
    if(words) {
        if(words.toLowerCase().contains("wlink")) { 
            try {
                theLink = "<a href='http://${linkURL}' target=_blank>${linkName}</a>"

                if(logEnable) log.debug "In makeTileLine - theLink: ${theLink}"
                if(theLink) {words = words.replace("%wLink%","${theLink}")}
            } catch (e) {
                log.error e
            }
        }

        if(words.toLowerCase().contains("%lastact%")) {
            try {
                if(dateTimeFormat == "f1") dFormat = "MMM dd, yyy - h:mm:ss a"
                if(dateTimeFormat == "f2") dFormat = "dd MMM, yyy - h:mm:ss a"
                if(dateTimeFormat == "f3") dFormat = "MMM dd - h:mm:ss a"
                if(dateTimeFormat == "f4") dFormat = "dd MMM - h:mm:ss a"
                if(dateTimeFormat == "f3a") dFormat = "MMM dd - h:mm a"
                if(dateTimeFormat == "f4a") dFormat = "dd MMM - h:mm a"
                if(dateTimeFormat == "f5") dFormat = "MMM dd - HH:mm"
                if(dateTimeFormat == "f6") dFormat = "dd MMM - HH:mm"
                if(dateTimeFormat == "f7") dFormat = "h:mm:ss a"
                if(dateTimeFormat == "f8") dFormat = "HH:mm:ss"

                lAct = theDevice.getLastActivity().format("${dFormat}")

                if(logEnable) log.debug "In makeTileLine - lAct: ${lAct}"
                if(lAct) {words = words.replace("%lastAct%","${lAct}")}
            } catch (e) {
                log.error e
            }
        }

        if(words.toLowerCase().contains("%currdate%")) {
            try {
                if(cDateFormat == "cd1") cdFormat = "MMM dd, yyy"
                if(cDateFormat == "cd2") cdFormat = "dd MMM, yyy"
                if(cDateFormat == "cd3") cdFormat = "MMM dd"
                if(cDateFormat == "cd4") cdFormat = "dd MMM"

                theDate = new Date()
                if(logEnable) log.debug "In makeTileLine - theDate: ${theDate}"
                cDate = theDate.format("${cdFormat}")

                if(logEnable) log.debug "In makeTileLine - cDate: ${cDate}"
                if(cDate) {words = words.replace("%currDate%","${cDate}")}
            } catch (e) {
                log.error e
            }
        }

        if(words.toLowerCase().contains("%currtime%")) {
            try {
                if(cTimeFormat == "ct1") ctFormat = "h:mm:ss a"
                if(cTimeFormat == "ct2") ctFormat = "HH:mm:ss"
                if(cTimeFormat == "ct3") ctFormat = "h:mm a"
                if(cTimeFormat == "ct4") ctFormat = "HH:mm"

                theDate = new Date()
                tDate = theDate.format("${ctFormat}")

                if(logEnable) log.debug "In makeTileLine - tDate: ${tDate}"
                if(tDate) {words = words.replace("%currTime%","${tDate}")}
            } catch (e) {
                log.error e
            }
        }

        if(words.toLowerCase().contains("%sunset%")) {
            try {
                if(cTimeFormat == "ct1") ctFormat = "h:mm:ss a"
                if(cTimeFormat == "ct2") ctFormat = "HH:mm:ss"
                if(cTimeFormat == "ct3") ctFormat = "h:mm a"
                if(cTimeFormat == "ct4") ctFormat = "HH:mm"

                def ssDate = location.sunset.format("${ctFormat}")

                if(logEnable) log.debug "In makeTileLine - ssDate: ${ssDate}"
                if(ssDate) {words = words.replace("%sunset%","${ssDate}")}
            } catch (e) {
                log.error e
            }
        }

        if(words.toLowerCase().contains("%sunrise%")) {
            try {
                if(cTimeFormat == "ct1") ctFormat = "h:mm:ss a"
                if(cTimeFormat == "ct2") ctFormat = "HH:mm:ss"
                if(cTimeFormat == "ct3") ctFormat = "h:mm a"
                if(cTimeFormat == "ct4") ctFormat = "HH:mm"

                def srDate = location.sunrise.format("${ctFormat}")

                if(logEnable) log.debug "In makeTileLine - srDate: ${srDate}"
                if(srDate) {words = words.replace("%sunrise%","${srDate}")}
            } catch (e) {
                log.error e
            }
        }
        
        if(words.toLowerCase().contains("%mode%")) {
            try {
                if(logEnable) log.debug "In makeTileLine - mode: ${location.mode}"
                def cMode = location.mode
                if(cMode) {words = words.replace("%mode%","${cMode}")}
            } catch (e) {
                log.error e
            }
        }
    } else {
        words = ""
    }
    
    newWords2 = "${words}"

    if(logEnable) log.debug "In makeTileLine - Returning newWords2: ${newWords2}"
    return newWords2
}

def sampleTileHandler(evt){
    if(!state.copyToLine) {
        if(logEnable) log.debug "In sampleTileHandler (${state.version}) - evt: ${evt}"

        section(getFormat("header-green", "${getImage("Blank")}"+" Sample Tile")) {
            paragraph "For testing purposes only"
            if(evt != "top") {
                input "bgColor", "text", title: "Background Color (ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White)", required: false, submitOnChange: true, width: 6
                input "tableWidth", "number", title: "Table Width (1 - 900)", description: "1-900", required: false, defaultValue: "300", submitOnChange: true, width: 6
            }
            makeTile()
            paragraph "<table style='width:${tableWidth}px;background-color:${bgColor};border:1px solid grey'><tr><td>${tileData}</td></tr></table>"

            if(evt != "top") {
                try {
                    totalLength = 45
                    if(state.theTile_1 && 1 <= howManyLines) totalLength = totalLength + state.theTileLength_1
                    if(state.theTile_2 && 2 <= howManyLines) totalLength = totalLength + state.theTileLength_2
                    if(state.theTile_3 && 3 <= howManyLines) totalLength = totalLength + state.theTileLength_3
                    if(state.theTile_4 && 4 <= howManyLines) totalLength = totalLength + state.theTileLength_4
                    if(state.theTile_5 && 5 <= howManyLines) totalLength = totalLength + state.theTileLength_5
                    if(state.theTile_6 && 6 <= howManyLines) totalLength = totalLength + state.theTileLength_6
                    if(state.theTile_7 && 7 <= howManyLines) totalLength = totalLength + state.theTileLength_7
                    if(state.theTile_8 && 8 <= howManyLines) totalLength = totalLength + state.theTileLength_8
                    if(state.theTile_9 && 9 <= howManyLines) totalLength = totalLength + state.theTileLength_9
                    if(!iFrameOff) totalLength = totalLength + 34
                } catch(e) {
                    log.error "Tile Master - Something went wrong. ${e}"
                }

                parag = "Characters - "
                if(state.theTile_1 && 1 <= howManyLines) parag += "Line 1: ${state.theTileLength_1}"
                if(state.theTile_2 && 2 <= howManyLines) parag += " - Line 2: ${state.theTileLength_2}"
                if(state.theTile_3 && 3 <= howManyLines) parag += " - Line 3: ${state.theTileLength_3}"
                if(state.theTile_4 && 4 <= howManyLines) parag += " - Line 4: ${state.theTileLength_4}"
                if(state.theTile_5 && 5 <= howManyLines) parag += " - Line 5: ${state.theTileLength_5}"
                if(state.theTile_6 && 6 <= howManyLines) parag += " - Line 6: ${state.theTileLength_6}"
                if(state.theTile_7 && 7 <= howManyLines) parag += " - Line 7: ${state.theTileLength_7}"
                if(state.theTile_8 && 8 <= howManyLines) parag += " - Line 8: ${state.theTileLength_8}"
                if(state.theTile_9 && 9 <= howManyLines) parag += " - Line 9: ${state.theTileLength_9}"
                if(!iFrameOff) parag += " - iFrame: 34"
                if(logEnable) log.debug "${parag}"
                paragraph "<hr>"
                paragraph "${parag}<br>* This is only an estimate. Actual character count can be found in the tile device."
                if(totalLength <= 1024) {
                    paragraph "Total Number of Characters: <font color='green'>${totalLength}</font><br><small>* Must stay under 1024 to display on Dashboard.<br>* Count includes all html characters needed to format the tile.</small>"
                } else {
                    paragraph "Total Number of Characters: <font color='red'>${totalLength}<br><small>* Must stay under 1024 to display on Dashboard.<br>* Count includes all html characters needed to format the tile.</small></font>"
                }
            }
        }
	}
}

def makeTile() {
    if(logEnable) log.debug "*************************************** In makeTile - Start ***************************************"
    if(logEnable) log.debug "In makeTile (${state.version}) - howManyLines: ${howManyLines}"
    if(secGlobal) tileData = "<table width=100%><tr><td>"
    if(!secGlobal) tileData = "<table width=100%>"
    
    if(state.theTile_1 && 1 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td >"
        tileData += state.theTile_1
    }
    if(state.theTile_2 && 2 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_2
    }
    if(state.theTile_3 && 3 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_3
    }
    if(state.theTile_4 && 4 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_4
    }
    if(state.theTile_5 && 5 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_5
    }
    if(state.theTile_6 && 6 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_6
    }
    if(state.theTile_7 && 7 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_7
    }
    if(state.theTile_8 && 8 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_8
    }
    if(state.theTile_9 && 9 <= howManyLines) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += state.theTile_9
    }
    if(!iFrameOff) {
        if(!secGlobal) tileData += "<tr><td>"
        tileData += "<iframe name=a width=1 height=1 />"
    }
    
    if(!secGlobal) tileData += "</table>"
    if(secGlobal) tileData += "</table>"
    
    if(logEnable) log.debug "In makeTile - tileData: ${tileData}"
    if(tileDevice) {
        try {
            tileDevice.sendTile01(tileData)
            if(logEnable) log.debug "In makeTile - tileData sent"
        } catch (e) {
            log.warn "Tile Master - Be sure the Tile Device selected is using the 'Tile Master Driver'. It's best to let TM create this device for you."
        }
    } else {
        log.warn "Tile Master - Please select a Tile Device in the first section of the app."
    }
    if(logEnable) log.debug "*************************************** In makeTile - End ***************************************"
    return tileData
}

def getStatusColors(theDevice, deviceStatus, deviceAtts, useColors, textORnumber, color1Name, color1Value, color2Name, color2Value, numLow,numHigh, colorNumLow, colorNum, colorNumHigh, useColorsBEF, useColorsAFT, wordsBEF, wordsAFT, useIcons, iconSize, iconLink1, iconLink2, iconLink3, icon1Name, icon2Name, iconNumLow, iconNumHigh) {
    if(logEnable) log.debug "*************************************** In getStatusColors - Start ***************************************"
    if(logEnable) log.debug "In getStatusColors (${state.version}) - Received - theDevice: ${theDevice} - deviceStatus: ${deviceStatus} - deviceAtts: ${deviceAtts} - useColors: ${useColors} - textORnumber: ${textORnumber} - color1Name: ${color1Name} - color1Value: ${color1Value} - color2Name: ${color2Name} - color2Value: ${color2Value} - useColorsBEF: ${useColorsBEF} - useColorsAFT: ${useColorsAFT} - wordsBEF: ${wordsBEF} - wordsAFT: ${wordsAFT} - useIcons: ${useIcons} - iconSize: ${iconSize} - iconLink1: ${iconLink1} - iconLink2: ${iconLink2} - iconLink3: ${iconLink3} - icon1Name: ${icon1Name} - icon2Name: ${icon2Name} - iconNumLow: ${iconNumLow} - iconNumHigh: ${iconNumHigh}"
    
    if(iconSize == null) iconSize = 30
    deviceStatus1 = null
    wordsBEF1 = null
    wordsAFT1 = null
    state.numError = ""
    
    if(textORnumber && useColors) {
        try {
            numLow = numLow.toInteger()
            numHigh = numHigh.toInteger()
            dStatus = deviceStatus.toInteger()
            
            if(dStatus <= numLow) {
                if(useColors) deviceStatus1 = "<span style='color:${colorNumLow}'>${deviceStatus}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${colorNumLow}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${colorNumLow}'>${wordsAFT}</span>"
                if(useIcon) deviceStatus1 = "<img src='${iconLink1}' style='height:${iconSize}px'>"
            }
            if(dStatus > numLow && dStatus < numHigh) {
                if(useColors) deviceStatus1 = "<span style='color:${colorNum}'>${deviceStatus}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${colorNum}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${colorNum}'>${wordsAFT}</span>"
                if(useIcon) deviceStatus1 = "<img src='${iconLink3}' style='height:${iconSize}px'>"
            }
            if(dStatus >= numHigh) {
                if(useColors) deviceStatus1 = "<span style='color:${colorNumHigh}'>${deviceStatus}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${colorNumHigh}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${colorNumHigh}'>${wordsAFT}</span>"
                if(useIcon) deviceStatus1 = "<img src='${iconLink2}' style='height:${iconSize}px'>"
            }
            state.numError = ""
        } catch (e) {
            state.numError = "Something went wrong with status number colors (useColors)"
            if(logEnable) log.debug "${state.numError}"
        }
    }
    
    if(!textORnumber && useColors) {
        if(color1Name && deviceStatus) {
            sDeviceStatus = deviceStatus.toString()
            if(sDeviceStatus.toLowerCase() == color1Name.toLowerCase()) {
                if(useColors) deviceStatus1 = "<span style='color:${color1Value}'>${color1Name}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${color1Value}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${color1Value}'>${wordsAFT}</span>"
            }
        }
        
        if(color2Name && deviceStatus) {
            sDeviceStatus = deviceStatus.toString()
            if(sDeviceStatus.toLowerCase() == color2Name.toLowerCase()) {
                if(useColors) deviceStatus1 = "<span style='color:${color2Value}'>${color2Name}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${color2Value}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${color2Value}'>${wordsAFT}</span>"
            }
        }
    }
    
    if(textORnumber && useIcons) {
        try {
            iconNumLow = iconNumLow.toInteger()
            iconNumHigh = iconNumHigh.toInteger()
            dStatus = deviceStatus.toInteger()
            
            if(dStatus <= iconNumLow) {
                deviceStatus1 = "<img src='${iconLink1}' style='height:${iconSize}px'>"
            }
            if(dStatus > iconNumLow && dStatus < iconNumHigh) {
                deviceStatus1 = "<img src='${iconLink3}' style='height:${iconSize}px'>"
            }
            if(dStatus >= iconNumHigh) {
                deviceStatus1 = "<img src='${iconLink2}' style='height:${iconSize}px'>"
            }
            state.numError = ""
        } catch (e) {
            state.numError = "Something went wrong with status number colors (useIcon) - Check option 'Is device value Text or Numbers'"
            log.debug "${state.numError}"
            log.error e
        }
    }
  
    if(!textORnumber && useIcons) {
        try {
            if(icon1Name && iconLink1 && deviceStatus) {
                if(deviceStatus.toLowerCase() == icon1Name.toLowerCase()) {
                    deviceStatus1 = "<img src='${iconLink1}' style='height:${iconSize}px'>"
                }
            }
            if(icon2Name && iconLink2 && deviceStatus) {
                if(deviceStatus.toLowerCase() == icon2Name.toLowerCase()) {
                    deviceStatus1 = "<img src='${iconLink2}' style='height:${iconSize}px'>"
                }
            }
        } catch (e) {
            state.numError = "Something went wrong with status text colors (useIcon) - Check option 'Is device value Text or Numbers'"
            log.debug "${state.numError}"
            log.error e
        }
    }
    
    if(deviceStatus1 == null) deviceStatus1 = deviceStatus
    if(wordsBEF1 == null) wordsBEF1 = wordsBEF
    if(wordsAFT1 == null) wordsAFT1 = wordsAFT
    
    theStatusCol = "${deviceStatus1},${wordsBEF1},${wordsAFT1}"
    if(logEnable) log.debug "In getStatusColors - Returning: ${theStatusCol}"
    if(logEnable) log.debug "*************************************** In getStatusColors - End ***************************************"
    return theStatusCol
}

def getCellColors(deviceStatus, deviceAtts, textORnumber, color1Name, color1Value, color2Name, color2Value, numLow, numHigh, colorNumLow, colorNum, colorNumHigh) {
    if(logEnable) log.debug "*************************************** In getCellColors - Start ***************************************"
    if(logEnable) log.debug "In getCellColors (${state.version}) - Received: ${deviceAtts} - ${deviceStatus}"
    theCellColor = null
    state.numError = ""
    
    if(textORnumber) {
        try {
            numLow = numLow.toInteger()
            numHigh = numHigh.toInteger()
            dStatus = deviceStatus.toInteger()
            
            if(dStatus <= numLow) theCellColor = "${colorNumLow}"
            if(dStatus > numLow && dStatus < numHigh) theCellColor = "${colorNum}"
            if(dStatus >= numHigh) theCellColor = "${colorNumHigh}"
            state.numError = ""
        } catch (e) {
            state.numError = "Something went wrong with status cell colors (number)"
            if(logEnable) log.debug "${state.numError}"  
        }
    }
    
    if(!textORnumber) {
        if(color1Name && deviceStatus) {
            if(deviceStatus.toLowerCase() == color1Name.toLowerCase()) {
                theCellColor = "${color1Value}"
            }
        }

        if(color2Name && deviceStatus) {
            if(deviceStatus.toLowerCase() == color2Name.toLowerCase()) {
                theCellColor = "${color2Value}"
            }
        }
    }
  
    if(logEnable) log.debug "In getCellColors - Returning: ${theCellColor}"
    if(logEnable) log.debug "*************************************** In getCellColors - End ***************************************"
    return theCellColor
}

def createChildDevice() {    
    if(logEnable) log.debug "In createChildDevice (${state.version})"
    statusMessage = ""
    if(!getChildDevice("TM - " + userName)) {
        if(logEnable) log.debug "In createChildDevice - Child device not found - Creating device Tile Master - ${userName}"
        try {
            addChildDevice("BPTWorld", "Tile Master Driver", "TM - " + userName, 1234, ["name": "TM - ${userName}", isComponent: false])
            if(logEnable) log.debug "In createChildDevice - Child device has been created! (TM - ${userName})"
            statusMessage = "<b>Device has been been created. (TM - ${userName})</b>"
        } catch (e) { if(logEnable) log.debug "Tile Master unable to create device - ${e}" }
    } else {
        statusMessage = "<b>Device Name (TM - ${userName}) already exists.</b>"
    }
    return statusMessage
}

def masterListHandler(masterList) {
    if(logEnable) log.debug "In masterListHandler - Receiving masterList from parent app"
    try {
        String newList = masterList
        newList = newList.replace("[","").replace("]","").replace(", ",",")
        if(logEnable) log.debug "${newList}"

        def sList = newList.split(",")

        state.allIcons = []
        for(i=0;x < sList.size();i++) {
            def (iconName,iconLink) = sList[i].split(";")
            ics = "${iconName} - ${iconLink}"
            state.allIcons << ics
        }
        if(logEnable) log.debug "In masterListHandler - ${state.allIcons}"
    } catch (e) {
        if(logEnable) log.debug "In masterListHandler - No Icons found"
    }
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
    state.numError = ""
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
        if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
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
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
