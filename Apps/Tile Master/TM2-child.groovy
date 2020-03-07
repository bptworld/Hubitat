/**
 *  ****************  Tile Master 2 Child App  ****************
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.3.2 - 03/07/20 - Missed two lines of code that displayed in the log, heads exploded.
 *  V2.3.1 - 03/06/20 - Fixed icons, now use ANY attribute with icons!
 *  V2.3.0 - 03/05/20 - Alright, this time I got it! Maybe
 *  V2.2.9 - 03/05/20 - Another Bug fix
 *  V2.2.8 - 03/05/20 - Bug fixes
 *  V2.2.7 - 03/02/20 - Lots of cosmetic changes
 *                    - iFrame now gets added to character count when activated
 *                    - Added more Global Variables, dropping character count way down
 *                    - All status color options are now entered directly into the child app
 *                    - ANY attribute can now have status colors
 *  V2.2.6 - 02/28/20 - More work on att values and a few other adjustments
 *  V2.2.5 - 02/27/20 - Attempt attributes showing No Data
 *  V2.2.4 - 02/26/20 - Added support for Tile to Tile copying
 *  V2.2.3 - 02/25/20 - Added the ability to copy one line to another
 *  V2.2.2 - 02/23/20 - More bug fixes and enhancements
 *  V2.2.1 - 02/22/20 - Bug fixes.
 *  V2.2.0 - 02/22/20 - Locks can now be controlled, bug fixes.
 *  V2.1.9 - 02/22/20 - Multi-Device Control!
 *  V2.1.8 - 02/19/20 - Changes to text color option, web Links and icon height. Added current Date and Time to wildcard options.
 *  V2.1.7 - 02/16/20 - Status Icons and the ability to change BEF and/or AFT text color based on device value
 *  V2.1.6 - 02/11/20 - BIG changes - Streamlined code, reduced by over 1000 lines! (Wow!)
 *            - Each child app will now automatically create the Tile Device if needed
 *            - Each Tile can now have 9 lines, built for anyones needs! (remember, you still can only have 1024 characters)
 *            - Added Text Decoration, Bold and Italic to each line options
 *            - Each section can change color based on Device Value
 *  ---
 *  V1.0.0 - 02/16/19 - Initially started working on this concept.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "TileMaster2ChildVersion"
	state.version = "v2.3.2"
   
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
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
            section("Important Information Regarding the Virtual Device:", hideable: true, hidden: true) {
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
                input "tileDevice", "capability.actuator", title: "Vitual Device created to send the data to:", required:true, multiple:false
            } 
        }

        state.appInstalled = app.getInstallationState()
		if(state.appInstalled == 'COMPLETE'){
            try { parent.sendIconList() }
            catch (e) { }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Line Options")) {
                if(lineToEdit == 1) { input "howManyLines", "number", title: "How many lines on Tile (range: 1-9)", range: '1..9', width:6, submitOnChange:true }
                theRange = "(1..$howManyLines)"
                input "lineToEdit", "number", title: "Which line to edit", range:theRange, width:6, submitOnChange:true
                
                href "copyLineHandler", title: "Tile Copy Options", description: "Click here for options"
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

                if(state.appInstalled == 'COMPLETE') {tileHandler("top")}
                
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
                        wildcards += "- %wLink% = Displays a clickable web link<br>"
                        
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
                            if(controlDevices) paragraph "<b>Controllable device attribute include 'Switch' and 'Lock'</b>"
                            input "deviceAtts_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAtts
                            deviceAtt = app."deviceAtts_$x"
                            
                            input "hideAttr_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                            hideAttr = app."hideAttr_$x"
                            
                            deviceStatus = theDevice.currentValue("${deviceAtt}")
                            if(deviceStatus == null) deviceStatus = "No Data"

                            if(theDevice && deviceAtt) paragraph "Current Status of Device Attribute: ${theDevice} - ${deviceAtt} - ${deviceStatus}"
                            
                            if(controlDevices && deviceAtt && !hideAttr) {
                                //paragraph "deviceAtt: ${deviceAtt}"
                                if(deviceAtt.toLowerCase() == "switch" || deviceAtt.toLowerCase() == "lock") {
                                    cDevID = theDevice.id
                                    instruct = "<b>Device ID: ${cDevID} - Type this number in where the Maker API URL says [DEVICE ID]</b><br>"
                                    if(deviceAtt.toLowerCase() == "switch") instruct += "<b>Also, Replace [COMMANDS] with on and off respectively.</b><br>"
                                    if(deviceAtt.toLowerCase() == "lock") instruct += "<b>Also, Replace [COMMANDS] with lock and unlock respectively.</b><br>"
                                    paragraph "${instruct}"
 
                                    if(deviceAtt.toLowerCase() == "switch") {
                                        input "controlOn_$x", "text", title: "Control <b>On</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                        input "controlOff_$x", "text", title: "Control <b>Off</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                    }
                                    if(deviceAtt.toLowerCase() == "lock") {
                                        input "controlLock_$x", "text", title: "Control <b>Lock</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                        input "controlUnlock_$x", "text", title: "Control <b>Unlock</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                    }
                                    paragraph "To save on the all important character count, use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a>. Be sure to use the URLs created above with Bitly."
                                    input "useBitly_$x", "bool", title: "Use Bitly", defaultValue: false, description: "bitly", submitOnChange: true
                                    useBitly = app."useBitly_$x"
                                    if(useBitly) {
                                        paragraph "Be sure to put 'http://' in front of the Bitly address"
                                        if(deviceAtt.toLowerCase() == "switch") {
                                            input "bControlOn_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOff_$x", "text", title: "Control <b>Off</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtt.toLowerCase() == "lock") {
                                            input "bControlLock_$x", "text", title: "Control <b>Lock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlUnlock_$x", "text", title: "Control <b>Unlock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
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
                            input "color_$x", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true,width: 6
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

                                input "color1Name_$x", "text", title: "<span style='color: ${color1}'>Color 1 Attribute Value</span><br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "color1Value_$x", "text", title: "Color 1<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6
                                input "color2Name_$x", "text", title: "<span style='color: ${color2}'>Color 2 Attribute Value</span><br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "color2Value_$x", "text", title: "Color 2<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6 
                                color1 = app."color1Value_$x"
                                color2 = app."color2Value_$x"
                                paragraph "<hr>"
                            
                                input "valueOrCell_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                                input "useColorsBEF_$x", "bool", title: "Use custom colors on 'Text BEFORE Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                                input "useColorsAFT_$x", "bool", title: "Use custom colors on 'Text AFTER Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                            }
                            
                            if(textORnumber) {
                                paragraph "Number attributes are based on Low, Inbetween and High values. Select the colors to display based on your setpoints."
                                input "numLow_$x", "text", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "numHigh_$x", "text", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
                                input "colorNumLow_$x", "text", title: "Number <= ${numLow}", submitOnChange: true, width: 4
                                input "colorNum_$x", "text", title: "Number Between", submitOnChange: true, width: 4
                                input "colorNumHigh_$x", "text", title: "Number >= ${numHigh}", submitOnChange: true, width: 4
                            }
                        }

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
                                input "iconNumLow_$x", "text", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "iconNumHigh_$x", "text", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
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
                            if(controlDevices) paragraph "<b>Controllable device attribute include 'Switch' and 'Lock'</b>"
                            input "deviceAttsa_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAttsa
                            deviceAtta = app."deviceAttsa_$x"
                            
                            input "hideAttra_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                            hideAttra = app."hideAttra_$x"
                            
                            deviceStatusa = theDevicea.currentValue("${deviceAtta}")
                            if(deviceStatusa == null) deviceStatusa = "No Data"
                            
                            if(theDevicea && deviceAtta) paragraph "Current Status of Device Attribute: ${theDevicea} - ${deviceAtta} - ${deviceStatusa}"
                            
                            if(controlDevices && deviceAtta && !hideAttra) {
                                if(deviceAtta.toLowerCase() == "switch" || deviceAtta.toLowerCase() == "lock") {
                                    cDevIDa = theDevicea.id
                                    instructa = "<b>Device ID: ${cDevIDa} - Type this number in where the Maker API URL says [DEVICE ID]</b><br>"
                                    if(deviceAtta.toLowerCase() == "switch") instructa += "<b>Also, Replace [COMMANDS] with on and off respectively.</b><br>"
                                    if(deviceAtta.toLowerCase() == "lock") instructa += "<b>Also, Replace [COMMANDS] with lock and unlock respectively.</b><br>"
                                    paragraph "${instructa}"

                                    if(deviceAtta.toLowerCase() == "switch") {
                                        input "controlOna_$x", "text", title: "Control <b>On</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                        input "controlOffa_$x", "text", title: "Control <b>Off</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                    }
                                    if(deviceAtta.toLowerCase() == "lock") {
                                        input "controlLocka_$x", "text", title: "Control <b>Lock</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                        input "controlUnlocka_$x", "text", title: "Control <b>Unlock</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                    }

                                    paragraph "To save on the all important character count, use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a>. Be sure to use the URLs created above with Bitly."
                                    input "useBitlya_$x", "bool", title: "Use Bitly", defaultValue: false, description: "bitly", submitOnChange: true
                                    useBitlya = app."useBitlya_$x"
                                    if(useBitlya) {
                                        paragraph "Be sure to put 'http://' in front of the Bitly address"
                                        if(deviceAtta.toLowerCase() == "switch") {
                                            input "bControlOna_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOffa_$x", "text", title: "Control <b>Off</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAtta.toLowerCase() == "lock") {
                                            input "bControlLocka_$x", "text", title: "Control <b>Lock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlUnlocka_$x", "text", title: "Control <b>Unlock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
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
                            input "colora_$x", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true, width: 6
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
                                paragraph "<hr>"
                                input "valueOrCella_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                                input "useColorsBEFa_$x", "bool", title: "Use custom colors on 'Text BEFORE Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                                input "useColorsAFTa_$x", "bool", title: "Use custom colors on 'Text AFTER Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                            }
                            
                            if(textORnumbera) {
                                paragraph "Number attributes are based on Low, Inbetween and High values. Select the colors to display based on your setpoints."
                                input "numLowa_$x", "text", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "numHigha_$x", "text", title: "Number >= HIGH", submitOnChange: true, width: 6
                                if(numLowa_$x == null) numLowa_$x = 0
                                if(numHigha_$x == null) numHigha_$x = 0
                                input "colorNumLowa_$x", "text", title: "Number <= ${numLowa}", submitOnChange: true, width: 4
                                input "colorNuma_$x", "text", title: "Number Between", submitOnChange: true, width: 4
                                input "colorNumHigha_$x", "text", title: "Number >= ${numHigha}", submitOnChange: true, width: 4
                            }
                        }

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
                                input "iconNumLowa_$x", "text", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "iconNumHigha_$x", "text", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
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
                            if(controlDevices) paragraph "<b>Controllable device attribute include 'Switch' and 'Lock'</b>"
                            input "deviceAttsb_$x", "enum", title: "Attribute", required:true, multiple:false, submitOnChange:true, options:allAttsb
                            deviceAttb = app."deviceAttsb_$x"
                                                                                                                              
                            input "hideAttrb_$x", "bool", title: "Hide Attribute value<br>", defaultValue: false, description: "Attribute", submitOnChange: true
                            hideAttrb = app."hideAttrb_$x"
                            
                            deviceStatusb = theDeviceb.currentValue("${deviceAttb}")
                            if(deviceStatusb == null) deviceStatusb = "No Data"
                            
                            if(theDeviceb && deviceAttb) paragraph "Current Status of Device Attribute: ${theDeviceb} - ${deviceAttb} - ${deviceStatusb}"
                            
                            if(controlDevices && deviceAttb && !hideAttrb) {
                                if(deviceAttb.toLowerCase() == "switch" || deviceAttb.toLowerCase() == "lock") {
                                    cDevIDb = theDeviceb.id
                                    instructb = "<b>Device ID: ${cDevIDb} - Type this number in where the Maker API URL says [DEVICE ID]</b><br>"
                                    if(deviceAttb.toLowerCase() == "switch") instructb += "<b>Also, Replace [COMMANDS] with on and off respectively.</b><br>"
                                    if(deviceAttb.toLowerCase() == "lock") instructb += "<b>Also, Replace [COMMANDS] with lock and unlock respectively.</b><br>"
                                    paragraph "${instructb}"

                                    if(deviceAttb.toLowerCase() == "switch") {
                                        input "controlOnb_$x", "text", title: "Control <b>On</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                        input "controlOffb_$x", "text", title: "Control <b>Off</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                    }
                                    if(deviceAttb.toLowerCase() == "lock") {
                                        input "controlLockb_$x", "text", title: "Control <b>Lock</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                        input "controlUnlockb_$x", "text", title: "Control <b>Unlock</b> URL from Maker API", required:true, multiple:false, submitOnChange:true
                                    }

                                    paragraph "To save on the all important character count, use a url shortener, like <a href='https://bitly.com/' target='_blank'>bitly.com</a>. Be sure to use the URLs created above with Bitly."
                                    input "useBitlyb_$x", "bool", title: "Use Bitly", defaultValue: false, description: "bitly", submitOnChange: true
                                    useBitlyb = app."useBitlyb_$x"
                                    if(useBitlyb) {
                                        paragraph "Be sure to put 'http://' in front of the Bitly address"
                                        if(deviceAttb.toLowerCase() == "switch") {
                                            input "bControlOnb_$x", "text", title: "Control <b>On</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlOffb_$x", "text", title: "Control <b>Off</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                        }
                                        if(deviceAttb.toLowerCase() == "lock") {
                                            input "bControlLockb_$x", "text", title: "Control <b>Lock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
                                            input "bControlUnlockb_$x", "text", title: "Control <b>Unlock</b> URL from Bitly", required:true, multiple:false, submitOnChange:true
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
                            input "colorb_$x", "text", title: "Text Color - ie. Default, Black, Blue, Brown, Green, Orange, Red, Yellow, White", required: true, defaultValue: "Default", submitOnChange: true, width: 6
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
                                
                                input "color1Nameb_$x", "text", title: "<span style='color: ${color1b}'>Color 1 Attribute Value</span><br><small>ie. On, Open, ect.</small>", submitOnChange: true, width: 6
		                        input "color1Valueb_$x", "text", title: "Color 1<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6
                                input "color2Nameb_$x", "text", title: "<span style='color: ${color2b}'>Color 2 Attribute Value</span><br><small>ie. Off, Closed, etc.</small>", submitOnChange: true, width: 6
                                input "color2Valueb_$x", "text", title: "Color 2<br><small>ie. Black, Blue, Brown, Green, Orange, Red, Yellow, White</small>", submitOnChange: true, width: 6 
                                color1b = app."color1Valueb_$x"
                                color2b = app."color2Valueb_$x"
                                paragraph "<hr>"
                                input "valueOrCellb_$x", "bool", title: "Change the color of the device value or entire cell (off = value, on = cell)", defaultValue: false, description: "Colors", submitOnChange: true
                                input "useColorsBEFb_$x", "bool", title: "Use custom colors on 'Text BEFORE Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                                input "useColorsAFTb_$x", "bool", title: "Use custom colors on 'Text AFTER Device Status'", defaultValue: false, description: "Colors", submitOnChange: true, width: 6
                            }
                            
                            if(textORnumberb) {
                                paragraph "Number attributes are based on Low, Inbetween and High values. Select the colors to display based on your setpoints."
                                input "numLowb_$x", "text", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "numHighb_$x", "text", title: "Number >= HIGH", submitOnChange: true, width: 6
                                if(numLowb_$x == null) numLowb_$x = 0
                                if(numHighb_$x == null) numHighb_$x = 0
                                input "colorNumLowb_$x", "text", title: "<span style='color: ${colorNumLowb};font-size: 25px'>Number <= ${numLowb}</span>", submitOnChange: true, width: 4, defaultValue: "blue"
                                input "colorNumb_$x", "text", title: "<span style='color: ${colorNumb};font-size: 25px'>Number Between</span>", submitOnChange: true, width: 4, defaultValue: ""
                                input "colorNumHighb_$x", "text", title: "<span style='color: ${colorNumHighb};font-size: 25px'>Number >= ${numHighb}</span>", submitOnChange: true, width: 4, defaultValue: "red"
                            }                          
                        }

                        input "useIconsb_$x", "bool", title: "Use custom icons instead of device value", defaultValue: false, description: "Icons", submitOnChange: true
                        uI = app."useIconsb_$x"
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
                                input "iconNumLowb_$x", "text", title: "Number <= LOW", submitOnChange: true, width: 6
                                input "iconNumHighb_$x", "text", title: "Number >= HIGH", submitOnChange: true, width: 6
                                
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
                   wordsAFTb && wordsAFTb.contains("%currTime%")) {
                    input "cTimeFormat", "enum", title: "Select Formatting", required: true, multiple: false, submitOnChange: true, options: [
                        ["ct1":"h:mm:ss a (12 hour)"],
                        ["ct2":"HH:mm:ss (24 hour)"],
                        ["ct3":"h:mm a (12 hour)"],
                        ["ct4":"HH:mm a (24 hour)"],
                    ], width:6
                } else {
                    paragraph "No Time formatting required.", width:6
                }
            }
        }

		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: true}
        if(state.appInstalled != 'COMPLETE') {
            section() {
                paragraph "<hr>"
                paragraph "<b>At this point, please press 'Done' to save the app. Then reopen it from the menu to complete the setup. This is required to retrieve the Icons and Colors from the parent app (if needed). Thanks.</b>"
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
            paragraph "<b>Copy another tile to this tile!</b><br>This will overwrite all settings on this tile with the settings of the 'from' tile."
            paragraph "When the copy switch is turned on:<br> - It will only take a few seconds<br> - When complete the switch will turn off<br> - The app number will be blank<br> - At this point you can press 'Next'"
            paragraph "<b>Note:</b> Devices will not be carried over to the new tile. This may cause errors in the log but most are harmless and can be ignored. IE. If you used %lastAct% in the child app, the device associated with it won't carry over, so it will cause an error until a new device is added."
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

def doTheLineCopy() {
    if(logEnable) log.info "In doTheLineCopy (${state.version})"
    
    fromThisLine = "_${fromLine}"
    toThisLine = "_${toLine}"
    
    settings.each { theOption ->
        name = theOption.key
        value = theOption.value

        if(name.contains("${fromThisLine}")) { 
            nameValue = theOption.value
            newName = name.replace("${fromThisLine}", "${toThisLine}")
            app?.updateSetting("${newName}", nameValue)
            if(logEnable) log.info "In doTheLineCopy - newName: ${newName} - nameValue: ${nameValue}"
        }
    }

    if(logEnable) log.info "In doTheCopy - Finished"
    state.theMessage = "<b>Line Settings have been copied. Hit 'Next' to continue</b>"
    app?.updateSetting("copyLine",[value:"false",type:"bool"])
    app?.updateSetting("fromLine",[value:"",type:"number"])
    app?.updateSetting("toLine",[value:"",type:"number"])
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

def doTheTileCopy(newSettings) {    // and finally the parent app send the settings!
    if(logEnable) log.info "In doTheTileCopy (${state.version})"
    if(copyTile) {
        if(logEnable) log.info "In doTheTileCopy - Received: ${newSettings}"
        
        newSettings.each { theOption ->
            name = theOption.key
            value = theOption.value
            if(name == "tileDevice" || name == "userName" || name == "copyTile" || name == "fromTile" || name == "logEnable") {
                if(logEnable) log.info "In doTheTileCopy - name: ${name}, so skipping"
            } else {
                if(name.contains("device_") || name.contains("devicea_") ||name.contains("deviceb_")) {
                    if(logEnable) log.info "In doTheTileCopy - Name is a device, so skipping"
                } else {
                    app?.updateSetting(name, value)
                }
                if(logEnable) log.info "In doTheTileCopy - name: ${name} - value: ${value}"
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
    
    for(x=1;x <= howManyLines;x++) {
        theDev = app."device_$x"
        theAtt = app."deviceAtts_$x"
        if(theDev) subscribe(theDev, theAtt, tileHandler)
        
        theDeva = app."devicea_$x"
        theAtta = app."deviceAttsa_$x"
        if(theDeva) subscribe(theDeva, theAtta, tileHandler)
        
        theDevb = app."deviceb_$x"
        theAttb = app."deviceAttsb_$x"
        if(theDevb) subscribe(theDevb, theAttb, tileHandler)
    }
      
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
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
            iconNumLow = app."iconNumLow_$x"
            iconNumHigh = app."iconNumHigh_$x"
            
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
            iconNumLowa = app."iconNumLowa_$x"
            iconNumHigha = app."iconNumHigha_$x"
            
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
            iconNumLowb = app."iconNumLowb_$x"
            iconNumHighb = app."iconNumHighb_$x"
            
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
			    deviceStatus = theDevice.currentValue("${deviceAtts}")
                if(deviceStatus == null) deviceStatus = "No Data"
                if(!valueOrCell || useIcons) {
                    getStatusColors(theDevice, deviceStatus, deviceAtts, useColors, textORnumber, color1Name, color1Value, color2Name, color2Value, numLow, numHigh, colorNumLow, colorNum, colorNumHigh, useColorsBEF, useColorsAFT, wordsBEF, wordsAFT, useIcons, iconSize, iconLink1, iconLink2, iconLink3, icon1Name, iconNumLow, iconNumHigh)
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
			    deviceStatusa = theDevicea.currentValue("${deviceAttsa}")
			    if(deviceStatusa == null) deviceStatusa = "No Data"
                if(!valueOrCella || useIconsa) {
                    getStatusColors(theDevicea, deviceStatusa, deviceAttsa, useColorsa, textORnumbera, color1Namea, color1Valuea, color2Namea, color2Valuea, numLowa, numHigha, colorNumLowa, colorNuma, colorNumHigha, useColorsBEFa, useColorsAFTa, wordsBEFa, wordsAFTa, useIconsa, iconSizea, iconLink1a, iconLink2a, iconLink3a, icon1Namea, iconNumLowa, iconNumHigha)
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
			    deviceStatusb = theDeviceb.currentValue("${deviceAttsb}")
			    if(deviceStatusb == null) deviceStatusb = "No Data"
                if(!valueOrCellb || useIconsb) {
                    getStatusColors(theDeviceb, deviceStatusb, deviceAttsb, useColorsb, textORnumberb, color1Nameb, color1Valueb, color2Nameb, color2Valueb, numLowb, numHighb, colorNumLowb, colorNumb, colorNumb, useColorsBEFb, useColorsAFTb, wordsBEFb, wordsAFTb, useIconsb, iconSizeb, iconLink1b, iconLink2b, iconLink3b, icon1Nameb, iconNumLowb, iconNumHighb)
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
        
        if(!useBitly && deviceAtts == "switch") controlOn = app."controlOn_$y"
        if(!useBitly && deviceAtts == "switch") controlOff = app."controlOff_$y"
        if(useBitly && deviceAtts == "switch") controlOn = app."bControlOn_$y"
        if(useBitly && deviceAtts == "switch") controlOff = app."bControlOff_$y"
        
        if(!useBitly && deviceAtts == "lock") controlOn = app."controlLock_$y"
        if(!useBitly && deviceAtts == "lock") controlOff = app."controlUnlock_$y"
        if(useBitly && deviceAtts == "lock") controlOn = app."bControlLock_$y"
        if(useBitly && deviceAtts == "lock") controlOff = app."bControlUnlock_$y"
        
        aligna = app."aligna_$y"
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
        
        if(!useBitlya && deviceAttsa == "switch") controlOna = app."controlOna_$y"
        if(!useBitlya && deviceAttsa == "switch") controlOffa = app."controlOffa_$y"
        if(useBitlya && deviceAttsa == "switch") controlOna = app."bControlOna_$y"
        if(useBitlya && deviceAttsa == "switch") controlOffa = app."bControlOffa_$y"
        
        if(!useBitlya && deviceAttsa == "lock") controlOna = app."controlLocka_$y"
        if(!useBitlya && deviceAttsa == "lock") controlOffa = app."controlUnlocka_$y"
        if(useBitlya && deviceAttsa == "lock") controlOna = app."bControlLocka_$y"
        if(useBitlya && deviceAttsa == "lock") controlOffa = app."bControlUnlocka_$y"
        
        alignb = app."alignb_$y"
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
        
        if(!useBitlyb && deviceAttsb == "switch") controlOnb = app."controlOnb_$y"
        if(!useBitlyb && deviceAttsb == "switch") controlOffb = app."controlOffb_$y"
        if(useBitlyb && deviceAttsb == "switch") controlOnb = app."bControlOnb_$y"
        if(useBitlyb && deviceAttsb == "switch") controlOffb = app."bControlOffb_$y"
        
        if(!useBitlyb && deviceAttsb == "lock") controlOnb = app."controlLockb_$y"
        if(!useBitlyb && deviceAttsb == "lock") controlOffb = app."controlUnlockb_$y"
        if(useBitlyb && deviceAttsb == "lock") controlOnb = app."bControlLockb_$y"
        if(useBitlyb && deviceAttsb == "lock") controlOffb = app."bControlUnlockb_$y"
        
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
        if(color != "Default") sec1Style += "color:${color};"
        if(fontSize != 0) sec1Style += "font-size:${fontSize}px;"
        if(italic) sec1Style += "font-style:italic;"
        if(bold) sec1Style += "font-weight:bold;"
        if(decoration != "None") sec1Style += "text-decoration:${decoration};"
        if(logEnable) log.debug "In tileHander - sec1Style: ${sec1Style}"
        
        sec2Style = ""
        if(aligna) sec2Style += "text-align:${aligna};"
        if(colora != "Default") sec2Style += "color:${colora};"
        if(fontSizea != 0) sec2Style += "font-size:${fontSizea}px;"
        if(italica) sec2Style += "font-style:italic;"
        if(bolda) sec2Style += "font-weight:bold;"
        if(decorationa != "None") sec2Style += "text-decoration:${decorationa};"
        if(logEnable) log.debug "In tileHander - sec2Style: ${sec2Style}"
        
        sec3Style = ""
        if(alignb) sec3Style += "text-align:${alignb};"
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
        if(controlDevices && (deviceAtts == "switch" || deviceAtts == "lock")) { 
            if(theDevice) {
                toControlOn = controlOn
                toControlOff = controlOff
                if(deviceAtts == "switch") cStatus = theDevice.currentValue("switch")
                if(deviceAtts == "lock") cStatus = theDevice.currentValue("lock")
            
                if(cStatus == "on" || cStatus == "locked") {
                    controlLink = "<a href=${toControlOff} target=a>$deviceStatus</a>"
                    //parent.controlDeviceHandler(toControlOff)
                } else {
                    controlLink = "<a href=${toControlOn} target=a>$deviceStatus</a>"
                    //parent.controlDeviceHandler(toControlOn)
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
            if(cTimeFormat == "ct4") ctFormat = "HH:mm a"
                 
            theDate = new Date()
            tDate = theDate.format("${ctFormat}")
             
            if(logEnable) log.debug "In makeTileLine - tDate: ${tDate}"
            if(tDate) {words = words.replace("%currTime%","${tDate}")}
        } catch (e) {
            log.error e
        }
    }
    
    newWords2 = "${words}"

    if(logEnable) log.debug "In makeTileLine - Returning newWords2: ${newWords2}"
    return newWords2
}

def sampleTileHandler(evt){
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
    if(secGlobal) tileData += "</td></tr></table>"
    
    if(logEnable) log.debug "In makeTile - tileData: ${tileData}"
    if(tileDevice) {
        tileDevice.sendTile01(tileData)
        if(logEnable) log.debug "In makeTile - tileData sent"
    }
    if(logEnable) log.debug "*************************************** In makeTile - End ***************************************"
    return tileData
}

def getStatusColors(theDevice, deviceStatus, deviceAtts, useColors, textORnumber, color1Name, color1Value, color2Name, color2Value, numLow,numHigh, colorNumLow, colorNum, colorNumHigh, useColorsBEF, useColorsAFT, wordsBEF, wordsAFT, useIcon, iconSize, iconLink1, iconLink2, iconLink3, icon1Name, iconNumLow, iconNumHigh) {
    if(logEnable) log.debug "*************************************** In getStatusColors - Start ***************************************"
    if(logEnable) log.debug "In getStatusColors (${state.version}) - Received - theDevice: ${theDevice} - deviceStatus: ${deviceStatus} - deviceAtts: ${deviceAtts} - useColors: ${useColors} - textORnumber: ${textORnumber} - color1Name: ${color1Name} - color1Value: ${color1Value} - color2Name: ${color2Name} - color2Value: ${color2Value} - useColorsBEF: ${useColorsBEF} - useColorsAFT: ${useColorsAFT} - wordsBEF: ${wordsBEF} - wordsAFT: ${wordsAFT} - useIcon: ${useIcon} - iconSize: ${iconSize}"
    
    if(iconSize == null) iconSize = 30
    deviceStatus1 = null
    wordsBEF1 = null
    wordsAFT1 = null
    state.numError = ""
    
    if(textORnumber && useColors) {
        try {
            numLow = numLow.toInteger()
            numHigh = numHigh.toInteger()
            if(deviceStatus <= numLow) {
                if(useColors) deviceStatus1 = "<span style='color:${colorNumLow}'>${deviceStatus}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${colorNumLow}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${colorNumLow}'>${wordsAFT}</span>"
                if(useIcon) deviceStatus1 = "<img src='${iconLink1}' style='height:${iconSize}px'>"
            }
            if(deviceStatus > numLow && deviceStatus < numHigh) {
                if(useColors) deviceStatus1 = "<span style='color:${colorNum}'>${deviceStatus}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${colorNum}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${colorNum}'>${wordsAFT}</span>"
                if(useIcon) deviceStatus1 = "<img src='${iconLink3}' style='height:${iconSize}px'>"
            }
            if(deviceStatus >= numHigh) {
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
        if(color1Name) {
            if(deviceStatus.toLowerCase() == color1Name.toLowerCase()) {
                if(useColors) deviceStatus1 = "<span style='color:${color1Value}'>${color1Name}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${color1Value}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${color1Value}'>${wordsAFT}</span>"
            }
        }
        
        if(color2Name) {
            if(deviceStatus.toLowerCase() == color2Name.toLowerCase()) {
                if(useColors) deviceStatus1 = "<span style='color:${color2Value}'>${color2Name}</span>"
                if(useColorsBEF) wordsBEF1 = "<span style='color:${color2Value}'>${wordsBEF}</span>"
                if(useColorsAFT) wordsAFT1 = "<span style='color:${color2Value}'>${wordsAFT}</span>"
            }
        }
    }
    
    if(textORnumber && useIcon) {
        try {
            iconNumLow = iconNumLow.toInteger()
            iconNumHigh = iconNumHigh.toInteger()
            if(deviceStatus <= iconNumLow) {
                if(useIcon) deviceStatus1 = "<img src='${iconLink1}' style='height:${iconSize}px'>"
            }
            if(deviceStatus > iconNumLow && deviceStatus < iconNumHigh) {
                if(useIcon) deviceStatus1 = "<img src='${iconLink3}' style='height:${iconSize}px'>"
            }
            if(deviceStatus >= iconNumHigh) {
                if(useIcon) deviceStatus1 = "<img src='${iconLink2}' style='height:${iconSize}px'>"
            }
            state.numError = ""
        } catch (e) {
            state.numError = "Something went wrong with status number colors (useIcon)"
            if(logEnable) log.debug "${state.numError}"
        }
    }
  
    if(!textORnumber && useIcon) {
        if(icon1Name && iconLink1) {
            if(deviceStatus.toLowerCase() == icon1Name.toLowerCase()) {
                if(useIcon) deviceStatus1 = "<img src='${iconLink1}' style='height:${iconSize}px'>"
            }
        }
        if(icon2Name && iconLink2) {
            if(deviceStatus.toLowerCase() == icon2Name.toLowerCase()) {
                if(useIcon) deviceStatus1 = "<img src='${iconLink2}' style='height:${iconSize}px'>"
            }
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
            if(deviceStatus <= numLow) theCellColor = "${colorNumLow}"
            if(deviceStatus > numLow && deviceStatus < numHigh) theCellColor = "${colorNum}"
            if(deviceStatus >= numHigh) theCellColor = "${colorNumHigh}"
            state.numError = ""
        } catch (e) {
            state.numError = "Something went wrong with status cell colors (number)"
            if(logEnable) log.debug "${state.numError}"  
        }
    }
    
    if(!textORnumber) {
        if(color1Name) {
            if(deviceStatus.toLowerCase() == color1Name.toLowerCase()) {
                theCellColor = "${color1Value}"
            }
        }

        if(color2Name) {
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
        if(logEnable) log.debug "In createChildDevice - Child device not found - Creating device Location Tracker - ${userName}"
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

// https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/button-power-red.png

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " Tile Master 2 - ${theName}")) {
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
