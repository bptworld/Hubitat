/**
 *  **************** Weather Dot Gov Child App  ****************
 *
 *  Design Usage:
 *  Making the Weather.gov data usable with Hubitat.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
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
 *  1.1.7 - 05/09/20 - New font options for Current Weather Tile
 *  1.1.6 - 06/25/20 - Fix for font size issue
 *  1.1.5 - 05/25/20 - Attempt to fix an issue with forecast tiles
 *  1.1.4 - 05/23/20 - Rewrite of some of the tables
 *  1.1.3 - 05/07/20 - Added multiple alert tiles and summary tile
 *  1.1.2 - 04/27/20 - Cosmetic changes
 *  1.1.1 - 04/24/20 - Adjustments Asthma and Pollen forecasts
 *  1.1.0 - 04/20/20 - Added Asthma and Pollen forecasts, adjustments to Alerts
 *  1.0.9 - 04/19/20 - Lots of adjustments
 *  1.0.8 - 04/18/20 - Added a switch to know when an alert is active or not
 *  1.0.7 - 04/18/20 - More modifications
 *  1.0.6 - 04/18/20 - Push alerts working correctly, message wildcards added
 *  1.0.5 - 04/17/20 - Move work on Alerts, added a bunch of code traps
 *  1.0.4 - 04/17/20 - Started adding notifications to Alerts
 *  1.0.3 - 04/17/20 - Added Alerts
 *  1.0.2 - 04/12/20 - Fixed Forecast from exceeding the 1024 limit
 *  1.0.1 - 04/08/20 - Fixed typo
 *  1.0.0 - 04/07/20 - Initial release.
 *
 */

def setVersion(){
    state.name = "Weather Dot Gov"
	state.version = "1.1.7"
}

definition(
    name: "Weather Dot Gov Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Making the Weather.gov data usable with Hubitat.",
    category: "Convenience",
	parent: "BPTWorld:Weather Dot Gov",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Weather%20Dot%20Gov/WDG-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "currentTileOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "dailyTileOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "forecastTileOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "alertTileOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "pollenOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Making the Weather.gov data usable with Hubitat."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            paragraph "Each child app needs TWO virtual devices. One to store the weather raw data and the second to store the tile data. Enter a short descriptive name for each device."
			input "dataName", "text", title: "Enter a name for this Data Device (ie. 'Dracut Weather Data' will become 'WDG - Dracut Weather Data')", required:true, submitOnChange:true
            paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
            if(dataName) createDataChildDevice()
            if(statusMessageD == null) statusMessageD = "Waiting on status message..."
            paragraph "${statusMessageD}"
            input "dataDevice", "capability.actuator", title: "Vitual Device created to send the raw data", required:true, multiple:false
            
            input "tileName", "text", title: "Enter a name for this Tile Device (ie. 'Dracut Weather Tile' will become 'WDG - Dracut Weather Tile')", required:true, submitOnChange:true
            paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
            if(tileName) createTileChildDevice()
            if(statusMessageT == null) statusMessageT = "Waiting on status message..."
            paragraph "${statusMessageT}"
            input "tileDevice", "capability.actuator", title: "Vitual Device created to send the tile data", required:true, multiple:false
        } 
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Location Options")) {
            input "lat", "text", title: "Latitude", require: true, defaultValue: "${location.latitude}", width:6
			input "lng", "text", title: "Longitude", require: true, defaultValue: "${location.longitude}", width:6
            input "zipCode", "text", title: "Zip Code", required: true, defaultValue: "${location.zipCode}"
            paragraph "Station ID can be found be visitng the <a href='http://weather.gov' target='_blank'>Weather.gov</a> homepage and putting in your zipcode or City, ST to find the ID (ie. KASH)"
            input "station", "text", title: "Station ID (UPPERCASE)", submitOnChange:true, width:6
            input "unitFormat", "enum", title: "Unit Format", required: true, options: ["Imperial", "Metric"], submitOnChange:true, width:6
            if(lat && lng && station && zipCode && unitFormat) sendDataOptions()
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Create Some Dashboard Tiles")) {
            if(updateTimeC) {
                href "currentTileOptions", title:"${getImage("optionsGreen")} Create Current Weather Tile", description:"Click here for Options"
            } else {
                href "currentTileOptions", title:"${getImage("optionsRed")} Create Current Weather Tile", description:"Click here for Options"
            }
            //if(updateTimeW) {
                href "dailyTileOptions", title:"${getImage("optionsGreen")} Create Daily Weather Tile", description:"Click here for Options"
            //} else {
            //    href "dailyTileOptions", title:"${getImage("optionsRed")} Create Daily Weather Tile", description:"Click here for Options"
            //}
            if(smallTileF) {
                href "forecastTileOptions", title:"${getImage("optionsGreen")} Create Forecast Weather Tile", description:"Click here for Options"
            } else {
                href "forecastTileOptions", title:"${getImage("optionsRed")} Create Forecast Weather Tile", description:"Click here for Options"
            }
            if(useNotify) {
                href "alertTileOptions", title:"${getImage("optionsGreen")} Create Alert Weather Tile", description:"Click here for Options"
            } else {
                href "alertTileOptions", title:"${getImage("optionsRed")} Create Alert Weather Tile", description:"Click here for Options"
            }
            if(fontSizeIndex) {
                href "pollenOptions", title:"${getImage("optionsGreen")} Asthma and Pollen Tile", description:"Click here for Options"
            } else {
                href "pollenOptions", title:"${getImage("optionsRed")} Asthma and Pollen Tile", description:"Click here for Options"
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this child app", required: false, submitOnChange: true
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def currentTileOptions() {
    dynamicPage(name: "currentTileOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Current Weather Tile Options")) {
            paragraph "Time to setup the Current Weather Tile for use with Dashboards!"
            
            input "fontSizeCurrentTileCTemp", "text", title: "Font Size for Current Temp", required: true, defaultValue:"20", submitOnChange:true, width:6
            input "fontSizeCurrentTileCStats", "text", title: "Font Size for Current Stats", required: true, defaultValue:"20", submitOnChange:true, width:6
            input "updateTimeC", "enum", title: "How ofter to update tile?", options:["1_Min","5_Min","10_Min","15_Min","30_Min","1_Hour","3_Hour"], submitOnChange:true  
            
            paragraph "<hr>"
            input "updateTileC", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileC) {
                getCurrentData()
                app?.updateSetting("updateTileC",[value:"false",type:"bool"])
            }

            input "tileStyleC", "enum", title: "Choose Syle for tile 1", options: ["Horizontal","Vertical"], submitOnChange:true
            
            paragraph "<hr>"
            if(tileStyleC == "Horizontal") {
                if(state.currentTable1) {
                    paragraph "${state.currentTable1}"
                    state.cTableSize = state.currentTable1.size()
                } else {
                    paragraph "Please flip the 'Manually Update Tiles' switch"
                }
            }
            if(tileStyleC == "Vertical") {
                if(state.currentTable2) {
                    paragraph "${state.currentTable2}"
                    state.cTableSize = state.currentTable2.size()
                } else {
                    paragraph "Please flip the 'Manually Update Tiles' switch"
                }
            }
            paragraph "<hr>"            
            if(state.cTableSize) paragraph "Table Character Count: ${state.cTableSize}"
        }
    }
}

def dailyTileOptions() {
    dynamicPage(name: "dailyTileOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Daily Weather Tile Options")) {
            paragraph "Time to setup the Daily Weather Tile for use with Dashboards!"
            paragraph "Daily data is only available in Imperial. Data is automaticaly updated every 3 hours."
            paragraph "<hr>"
            input "updateTileW", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileW) {
                getWeeklyData()
                pauseExecution(1000)
                tile1 = tileDevice.currentValue('weeklyDataTile01')
                tile2 = tileDevice.currentValue('weeklyDataTile02')
                tile3 = tileDevice.currentValue('weeklyDataTile03')
                tile4 = tileDevice.currentValue('weeklyDataTile04')
                tile5 = tileDevice.currentValue('weeklyDataTile05')
                tile6 = tileDevice.currentValue('weeklyDataTile06')
                tile7 = tileDevice.currentValue('weeklyDataTile07')
                tile8 = tileDevice.currentValue('weeklyDataTile08')
                tile9 = tileDevice.currentValue('weeklyDataTile09')
                tile10 = tileDevice.currentValue('weeklyDataTile10')
                tile11 = tileDevice.currentValue('weeklyDataTile11')
                tile12 = tileDevice.currentValue('weeklyDataTile12')
                tile13 = tileDevice.currentValue('weeklyDataTile13')
                tile14 = tileDevice.currentValue('weeklyDataTile14')
                
                if(tile1) size1 = tile1.size()
                if(tile2) size2 = tile2.size()
                if(tile3) size3 = tile3.size()
                if(tile4) size4 = tile4.size()
                if(tile5) size5 = tile5.size()
                if(tile6) size6 = tile6.size()
                if(tile7) size7 = tile7.size()
                if(tile8) size8 = tile8.size()
                if(tile9) size9 = tile9.size()
                if(tile10) size10 = tile10.size()
                if(tile11) size11 = tile11.size()
                if(tile12) size12 = tile12.size()
                if(tile13) size13 = tile13.size()
                if(tile14) size14 = tile14.size()
                
                app?.updateSetting("updateTileW",[value:"false",type:"bool"])
            }
           
            theWeatherDays1 =  "<table width=100% align=center><tr valign=top>"
            theWeatherDays1 += "<td>${tile1}<br>Tile Count: ${size1}</td>"
            theWeatherDays1 += "<td>${tile2}<br>Tile Count: ${size2}</td>"
            theWeatherDays1 += "<td>${tile3}<br>Tile Count: ${size3}</td>"
            theWeatherDays1 += "<td>${tile4}<br>Tile Count: ${size4}</td>"
            theWeatherDays1 += "<td>${tile5}<br>Tile Count: ${size5}</td>"
            theWeatherDays1 += "<td>${tile6}<br>Tile Count: ${size6}</td>"
            theWeatherDays1 += "<td>${tile7}<br>Tile Count: ${size7}</td>"          
            theWeatherDays1 += "</tr></table>"
            
            theWeatherDays2 =  "<table width=100% align=center><tr valign=top>"
            theWeatherDays2 += "<td>${tile8}<br>Tile Count: ${size8}</td>"
            theWeatherDays2 += "<td>${tile9}<br>Tile Count: ${size9}</td>"
            theWeatherDays2 += "<td>${tile10}<br>Tile Count: ${size10}</td>"
            theWeatherDays2 += "<td>${tile11}<br>Tile Count: ${size11}</td>"
            theWeatherDays2 += "<td>${tile12}<br>Tile Count: ${size12}</td>"
            theWeatherDays2 += "<td>${tile13}<br>Tile Count: ${size13}</td>"
            theWeatherDays2 += "<td>${tile14}<br>Tile Count: ${size14}</td>"          
            theWeatherDays2 += "</tr></table>"
            
            paragraph "<hr>"
            if(tile1) {
                paragraph "${theWeatherDays1}"
                paragraph "<hr>"   
                paragraph "${theWeatherDays2}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
        }
    }
}

def forecastTileOptions() {
    dynamicPage(name: "forecastTileOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Forecast Tile Options")) {
            paragraph "Time to setup the Forecast Tile for use with Dashboards!"
            paragraph "Daily data is only available in Imperial."
            input "smallTileF", "bool", title: "Make the print smaller on tile", description: "", submitOnChange:true
            if(smallTileF) input "sF", "number", title: "Font Size", defaultValue:15, submitOnChange:true
            paragraph "<small>Note: This automatically updates every 3 hours</small>"
            paragraph "<hr>"
            input "updateTileF", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileF) {
                getWeeklyData()
                pauseExecution(1000)
                fTile1 = tileDevice.currentValue('forecastTable1')
                fTile2 = tileDevice.currentValue('forecastTable2')
                fTile3 = tileDevice.currentValue('forecastTable3')
                fTile4 = tileDevice.currentValue('forecastTable4')
                fTile5 = tileDevice.currentValue('forecastTable5')
                               
                app?.updateSetting("updateTileF",[value:"false",type:"bool"])
            }
            if(fTile1) fSize1 = fTile1.size()
            if(fTile2) fSize2 = fTile2.size()
            if(fTile3) fSize3 = fTile3.size()
            if(fTile4) fSize4 = fTile4.size()
            if(fTile5) fSize5 = fTile5.size()
            
            paragraph "<hr>"
            if(fTile1) {
                paragraph "${fTile1}"
                paragraph "Tile Count: ${fSize1}"
                paragraph "<hr>"   
                paragraph "${fTile2}"
                paragraph "Tile Count: ${fSize2}"
                paragraph "<hr>"
                paragraph "${fTile3}"
                paragraph "Tile Count: ${fSize3}"
                paragraph "<hr>"
                paragraph "${fTile4}"
                paragraph "Tile Count: ${fSize4}"
                paragraph "<hr>"
                paragraph "${fTile5}"
                paragraph "Tile Count: ${fSize5}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
        }
    }
}
     
def alertTileOptions() {
    dynamicPage(name: "alertTileOptions", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "Time to setup the Alert Tile for use with Dashboards!"
            paragraph "The alerts are checked every hour."
            //paragraph "Alerts are based on Urgency and checked every 3 hours unless:<br> - Urgency is <i>expected</i>, check every 1 hour<br> - Urgency is <i>immediate</i>, check every 15 minutes" 
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Alert Options")) {           
            input "useNotify", "bool", title: "Use Notifications", description: "", defaultValue:false, submitOnChange:true
        }
        if(useNotify) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
                paragraph "Notifications can be sent anytime the Urgency and/or Severity changes."
                input "notifyOnUrgency", "enum", title: "Get notification based on Urgency", options: ["any", "immediate", "expected", "future", "past"], defaultValue:"any", multiple:true, submitOnChange:true
                input "notifyOnSeverity", "enum", title: "Get notification based on Severity", options: ["any", "extreme", "severe", "moderate", "minor"], defaultValue:"any", multiple:true, submitOnChange:true
                input "notifyOn", "bool", title: "and/or (off = 'and', on = 'or')", description: "", defaultValue:false, submitOnChange:true
                if(!notifyOn) {
                    nOn = "and"
                } else {
                    nOn = "or"
                }
                paragraph "Notifications will be sent when Urgency is ${notifyOnUrgency} ${nOn} Severity is ${notifyOnSeverity}"
                paragraph "Note: The data device will also turn on/off based on whether there is an active notification or not."
                paragraph "<hr>"
                paragraph "Notifications can be also be sent anytime the Alert Description changes. Useful if you want to receive updates after the initial alert."
                input "notifyOnDesc", "bool", title: "Get notification when Description is updated", defaultValue:false, submitOnChange:true
   
            }   
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
                input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
    	    }
            if(sendPushMessage) {
                section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
                    wildTable =  "<table width=100%><tr><td colspan=2 width=100%><b>Wildcards</b>:"
                    wildTable += "<tr><td width=50%> %urgency% - Urgency value<br> %severity% - Severity value<br> %desc% - Alert Description"
                    wildTable += "<td width=50%> %inst% - Alert Instructions<br> %lbreak% - Line Break<br>"
                    wildTable += "</table>"
                    
                    paragraph "${wildTable}"
                    input "notifyMsg", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)", required:false, defaultValue:"Weather Alert - %urgency%-%severity%%lbreak%%lbreak%%desc%%lbreak%%lbreak%%inst%", submitOnChange:true
                    input "oList", "bool", defaultValue:false, title: "Show a list view of the random messages?", description: "List View", submitOnChange:true
                    if(oList) {
                        def values = "${notifyMsg}".split(";")
                        listMap = ""
                        values.each { item -> listMap += "${item}<br>" }
                        paragraph "${listMap}"
                    }
    	        }
            }
        }
     
        section(getFormat("header-green", "${getImage("Blank")}"+" Tile Options")) {    
            input "alertFontSize", "number", title: "Font Size", defaultValue:15, submitOnChange:true
            paragraph "<hr>"
            input "updateTileA", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileA) {
                getAlertData()
                pauseExecution(1000)
                tile1 = tileDevice.currentValue('alertTile1')
                tile2 = tileDevice.currentValue('alertTile2')
                tile3 = tileDevice.currentValue('alertTile3')
                tile4 = tileDevice.currentValue('alertTile4')
                tile5 = tileDevice.currentValue('alertTile5')
                tileSummary = tileDevice.currentValue('alertSummaryTile')
                app?.updateSetting("updateTileA",[value:"false",type:"bool"])
            }
            if(tile1) fTile1 = tile1.size()
            if(tile2) fTile2 = tile2.size()
            if(tile3) fTile3 = tile3.size()
            if(tile4) fTile4 = tile4.size()
            if(tile5) fTile5 = tile5.size()
            if(tileSummary) fTileSummary = tileSummary.size()
            
            paragraph "<hr>"
            if(tileSummary) {
                paragraph "<b>Tile Summary</b>"
                paragraph "${tileSummary}"
                paragraph "Tile Count: ${fTileSummary}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
            if(tile1) {
                paragraph "<b>Tile 1</b>"
                paragraph "${tile1}"
                paragraph "Tile Count: ${fTile1}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
            if(tile2) {
                paragraph "<b>Tile 2</b>"
                paragraph "${tile2}"
                paragraph "Tile Count: ${fTile2}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
            if(tile3) {
                paragraph "<b>Tile 3</b>"
                paragraph "${tile3}"
                paragraph "Tile Count: ${fTile3}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
            if(tile4) {
                paragraph "<b>Tile 4</b>"
                paragraph "${tile4}"
                paragraph "Tile Count: ${fTile4}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
            if(tile5) {
                paragraph "<b>Tile 5</b>"
                paragraph "${tile5}"
                paragraph "Tile Count: ${fTile5}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
        }
    }
}

def pollenOptions() {
    dynamicPage(name: "pollenOptions", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "Time to setup the Asthma and Pollen Tile for use with Dashboards!"
            paragraph "Data is collected from asthmaforecast.com and pollen.com" 
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Forecast Options")) {           
		    input "fontSizeIndex", "text", title: "Font Size-Index Line (ie. 7.6 - Medium-High)", required: true, defaultValue:"15", submitOnChange:true
		    input "fontSizeTriggers", "text", title: "Font Size-Triggers Line (ie. Juniper, Poplar, Alder)", required: true, defaultValue:"15", submitOnChange:true
            paragraph "<hr>"
            input "updateTileP", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileP) {
                getAsthmaData()
                getPollenData()
                pauseExecution(1000)
                asthmaYesterdayTile = tileDevice.currentValue('asthmaYesterdayTile')
                asthmaTodayTile = tileDevice.currentValue('asthmaTodayTile')
                asthmaTomorrowTile = tileDevice.currentValue('asthmaTomorrowTile')
                
                pollenYesterdayTile = tileDevice.currentValue('pollenYesterdayTile')
                pollenTodayTile = tileDevice.currentValue('pollenTodayTile')
                pollenTomorrowTile = tileDevice.currentValue('pollenTomorrowTile')
                
                app?.updateSetting("updateTileP",[value:"false",type:"bool"])
            }
            if(asthmaYesterdayTile) aTile1 = asthmaYesterdayTile.size()
            if(asthmaTodayTile) aTile2 = asthmaTodayTile.size()
            if(asthmaTomorrowTile) aTile3 = asthmaTomorrowTile.size()
            
            if(pollenYesterdayTile) pTile1 = pollenYesterdayTile.size()
            if(pollenTodayTile) pTile2 = pollenTodayTile.size()
            if(pollenTomorrowTile) pTile3 = pollenTomorrowTile.size()
            
            asthmaTable =  "<table width=100% align=center>"
            asthmaTable += "<tr><td align=center>${asthmaYesterdayTile}"
            asthmaTable += "<td align=center>${asthmaTodayTile}"
            asthmaTable += "<td align=center>${asthmaTomorrowTile}"
            asthmaTable += "<tr><td>Tile Count: ${aTile1}"
            asthmaTable += "<td>Tile Count: ${aTile2}"
            asthmaTable += "<td>Tile Count: ${aTile3}"
            asthmaTable += "</table>"
            
            pollenTable =  "<table width=100% align=center>"
            pollenTable += "<tr><td align=center>${pollenYesterdayTile}"
            pollenTable += "<td align=center>${pollenTodayTile}"
            pollenTable += "<td align=center>${pollenTomorrowTile}"
            pollenTable += "<tr><td>Tile Count: ${pTile1}"
            pollenTable += "<td>Tile Count: ${pTile2}"
            pollenTable += "<td>Tile Count: ${pTile3}"
            pollenTable += "</table>"
                    
            paragraph "<hr>"
            if(asthmaYesterdayTile) {
                paragraph "${asthmaTable}"
                paragraph "${pollenTable}"
            } else {
                paragraph "Please flip the 'Manually Update Tiles' switch"
            }
            paragraph "<hr>"
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    getCurrentData()
    getWeeklyData()
	initialize()
}

def initialize() {
    setDefaults()
	if(updateTimeC == "1_Min") runEvery1Minute(getCurrentData)
    if(updateTimeC == "5_Min") runEvery5Minutes(getCurrentData)
    if(updateTimeC == "10_Min") runEvery10Minutes(getCurrentData)
    if(updateTimeC == "15_Min") runEvery15Minutes(getCurrentData)
    if(updateTimeC == "30_Min") runEvery30Minutes(getCurrentData)
	if(updateTimeC == "1_Hour") runEvery1Hour(getCurrentData)
    if(updateTimeC == "3_Hour") runEvery3Hours(getCurrentData)
    
    runEvery3Hours(getWeeklyData)
    
    if(useNotify) initializeAlerts() 
    if(useNotify) subscribe(dataDevice, "alertSeverity", alertNotifications)
    if(useNotify) subscribe(dataDevice, "alertUrgency", alertNotifications)
    if(useNotify) subscribe(dataDevice, "alertDescription", alertNotifications)
    
    if(zipCode) {
        runEvery3Hours(getPollenData)
        runEvery3Hours(getAsthmaData)
    }
}

def initializeAlerts() {
    if(logEnable) log.debug "In initializeAlerts (${state.version})"
    // Urgency (immediate, expected, future, past, unknown)
/*    
    alertUrgency = dataDevice.currentValue('alertUrgency')
      
    if(alertUrgency) {
        if(alertUrgency.toLowerCase() == "future") {
            runEvery3Hours(getAlertData)
        } else if(alertUrgency.toLowerCase() == "expected") {
            runEvery1Hour(getAlertData)
        } else if(alertUrgency.toLowerCase() == "immediate") {
            runEvery15Minutes(getAlertData)
        } else {
            runEvery3Hours(getAlertData)
        }
    }
*/
    runEvery1Hour(getAlertData)
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def sendDataOptions() {
    if(logEnable) log.debug "In sendDataOptions (${state.version})"
    
    theOptions = "${lat}:${lng}:${station}:${zipCode}:${unitFormat}"
    dataDevice.dataOptions(theOptions)
}

def getCurrentData(evt) {
    if(logEnable) log.debug "In getCurrentData (${state.version})"
    dataDevice.getWeatherData()
    pauseExecution(1000)
    
    cIcon = dataDevice.currentValue('icon')
    cTemp = dataDevice.currentValue('temperature')
    if(cTemp) cTemp1 = cTemp.toInteger()
    cTextDescription = dataDevice.currentValue('textDescription')
    cRelativeHumidity = dataDevice.currentValue('relativeHumidity')
    cWindSpeed = dataDevice.currentValue('windSpeed')
    cBarometricPressure = dataDevice.currentValue('barometricPressure')
    cDewpoint = dataDevice.currentValue('dewpoint')
    cVisibility = dataDevice.currentValue('visibility')
    cLastUpdated = dataDevice.currentValue('lastUpdated')
            
    if(cTemp1 == null) cTemp1 = "No Data"
    if(cTextDescription == null) cTextDescription = "No Data"
    if(cRelativeHumidity == null) cRelativeHumidity = "No Data"
    if(cWindSpeed == null) cWindSpeed = "No Data"
    if(cBarometricPressure == null) cBarometricPressure = "No Data"
    if(cDewpoint == null) cDewpoint = "No Data"
    if(cVisibility == null) cVisibility = "No Data"
    if(cLastUpdated == null) cLastUpdated = "No Data"

    if(fontSizeCurrentTileCTemp == null) fontSizeCurrentTileCTemp = 30
    if(fontSizeCurrentTileCStats == null) fontSizeCurrentTileCStats = 15
            
    currentTable1 =  "<div style='overflow:auto;height:90%'>"
    currentTable1 += "<table style='width:100%;align-content:center'>"
    currentTable1 += "<tr><td><img src='https://${cIcon}'>"
    currentTable1 += "<td><span style='font-weight:bold'>${cTextDescription}</span><br><span style='font-size:${fontSizeCurrentTileCTemp}px;font-weight:bold'>${cTemp1}</span>"
    currentTable1 += "<td><span style='font-size:${fontSizeCurrentTileCStats}px'><b>Humidity:</b> ${cRelativeHumidity}%<br><b>Wind Speed:</b> ${cWindSpeed}<br><b>Barometer:</b> ${cBarometricPressure}</span>"
    currentTable1 += "<td><span style='font-size:${fontSizeCurrentTileCStats}px'><b>Dewpoint:</b> ${cDewpoint}<br><b>Visibility:</b> ${cVisibility}<br><b>Last Updated:</b></span>"
    currentTable1 += "<tr><td colspan=4 align=center><small>${cLastUpdated}</small>"
    currentTable1 += "</table></div>"

    state.currentTable1 = currentTable1
    
    currentTable2 =  "<div style='overflow:auto;height:90%'><table style='width:100%;align-content:center'>"
    currentTable2 += "<tr><td style='width:100%;align-content:center'><img src='https://${cIcon}'><br>"
    currentTable2 += "<span style='font-weight:bold'>${cTextDescription}</span><br>"
    currentTable2 += "<span style='font-size:${fontSizeCurrentTileCTemp}px;font-weight:bold'>${cTemp1}</span><br>"
    currentTable2 += "<span style='font-size:${fontSizeCurrentTileCStats}px;font-weight:bold'><b>Humidity:</b> ${cRelativeHumidity}%<br>"
    currentTable2 += "<b>Wind Speed:</b> ${cWindSpeed}<br>"
    currentTable2 += "<b>Barometer:</b> ${cBarometricPressure}<br>"
    currentTable2 += "<b>Dewpoint:</b> ${cDewpoint}<br>"
    currentTable2 += "<b>Visibility:</b> ${cVisibility}<br>"
    currentTable2 += "<b>Last Updated:</b></span><br>"
    currentTable2 += "<small>${cLastUpdated}</small>"
    currentTable2 += "</table></div>"
    
    state.currentTable2 = currentTable2
            
    if(logEnable) log.debug "In currentTileOptions - Sending 'currentTable' to tile device (${tileDevice})"
    if(tileStyleC == "Horizontal") { tileDevice.currentData(state.currentTable1) }
    if(tileStyleC == "Vertical") { tileDevice.currentData(state.currentTable2) }
}

def getWeeklyData(evt) {
    if(logEnable) log.debug "In getWeeklyData (${state.version})"
    dataDevice.getWeeklyData()
    pauseExecution(1000)
    
    //${number}:${name}:${temperature}:${temperatureUnit}:${temperatureTrend}:${windSpeed}:${windDirection}:${icon}:${shortForecast}:${detailedForecast}
    for(x=0;x<14;x++) {
        theY = x + 1
        theDay = "zforecast_${theY}"
        
        if(x==0) theData1 = dataDevice.currentValue('zforecast_1')
        if(x==1) theData1 = dataDevice.currentValue('zforecast_2')
        if(x==2) theData1 = dataDevice.currentValue('zforecast_3')
        if(x==3) theData1 = dataDevice.currentValue('zforecast_4')
        if(x==4) theData1 = dataDevice.currentValue('zforecast_5')
        if(x==5) theData1 = dataDevice.currentValue('zforecast_6')
        if(x==6) theData1 = dataDevice.currentValue('zforecast_7')
        if(x==7) theData1 = dataDevice.currentValue('zforecast_8')
        if(x==8) theData1 = dataDevice.currentValue('zforecast_9')
        if(x==9) theData1 = dataDevice.currentValue('zforecast_10')
        if(x==10) theData1 = dataDevice.currentValue('zforecast_11')
        if(x==11) theData1 = dataDevice.currentValue('zforecast_12')
        if(x==12) theData1 = dataDevice.currentValue('zforecast_13')
        if(x==13) theData1 = dataDevice.currentValue('zforecast_14')

        if(theData1) {
            theData = theData1.split(":")
            wNumber = theData[0]
            wName = theData[1]
            wTemp1 = theData[2]
            if(wTemp1) wTemp = wTemp1.toInteger()
            wTempUnit = theData[3]
            wTemperatureTrend = theData[4]
            wWindSpeed = theData[5]
            wWindDirection = theData[6]
            wIcon = theData[7]
            wShortForecast = theData[8]
            wDetailedForecast = theData[9]
        }
            
        if(wNumber == null) wNumber = "No Data"
        if(wName == null) wName = "No Data"
        if(wTemp == null) wTemp = "No Data"
        if(wTemperatureTrend == null) wTemperatureTrend = "No Data"
        if(wWindSpeed == null) wWindSpeed = "No Data"
        if(wWindDirection == null) wWindDirection = "No Data"
        if(wIcon == null) wIcon = "No Data"
        if(wShortForecast == null) wShortForecast = "No Data"
        if(wDetailedForecast == null) wDetailedForecast = "No Data"

        wfontSize = "45"

        // zforecast_1 : 1:This Afternoon:63:F:null:13 mph:NW:api.weather.gov/icons/land/day/few?size=medium:Sunny:Sunny, with a high near 63. Northwest wind around 13 mph, with gusts as high as 23 mph.
    
        weeklyTable_$x = "${wNumber}::"
        weeklyTable_$x += "<div style='overflow:auto;height:90%'><table style='width:100%;height:100%;align-content:center'>"
        weeklyTable_$x += "<tr style='vertical-align:text-top;align-content:center'><td><small><b>${wName}</b></small><br>"
        weeklyTable_$x += "<img width=50% src='https://${wIcon}'><br>"
        weeklyTable_$x += "<small>${wShortForecast}</small>"
        weeklyTable_$x += "<tr style='vertical-align:text-bottom;align-content:center'><td>${wTemp1}"
        weeklyTable_$x += "</table></div>"
        
        if(logEnable) log.debug "In weeklytTileOptions - Sending 'weeklyTable' to tile device (${tileDevice})"
        tileDevice.weeklyData(weeklyTable_$x)
        
        if(x == 0) forecastTable1 =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:left'>"
        if(smallTileF) { if(x >= 0 && x <= 2) forecastTable1 += "<tr><td style='text-align:left;font-size:${sF}px'><b>${wName}</b> - ${wDetailedForecast}" }
        if(!smallTileF) { if(x >= 0 && x <= 2) forecastTable1 += "<tr><td style='text-align:left'><b>${wName}</b> - ${wDetailedForecast}" }
        
        if(x == 3) forecastTable2 =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:left'>"
        if(smallTileF) { if(x >= 3 && x <= 5) forecastTable2 += "<tr><td style='text-align:left;font-size:${sF}px'><b>${wName}</b> - ${wDetailedForecast}" }
        if(!smallTileF) { if(x >= 3 && x <= 5) forecastTable2 += "<tr><td style='text-align:left'><b>${wName}</b> - ${wDetailedForecast}" }
        
        if(x == 6) forecastTable3 =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:left'>"
        if(smallTileF) { if(x >= 6 && x <= 8) forecastTable3 += "<tr><td style='text-align:left;font-size:${sF}px'><b>${wName}</b> - ${wDetailedForecast}" }
        if(!smallTileF) { if(x >= 6 && x <= 8) forecastTable3 += "<tr><td style='text-align:left'><b>${wName}</b> - ${wDetailedForecast}" }    
        
        if(x == 9) forecastTable4 =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:left'>"
        if(smallTileF) { if(x >= 9 && x <= 11) forecastTable4 += "<tr><td style='text-align:left;font-size:${sF}px'><b>${wName}</b> - ${wDetailedForecast}" }
        if(!smallTileF) { if(x >= 9 && x <= 11) forecastTable4 += "<tr><td style='text-align:left'><b>${wName}</b> - ${wDetailedForecast}" }
        
        if(x == 12) forecastTable5 =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:left'>"
        if(smallTileF) { if(x >= 12 && x <= 13) forecastTable5 += "<tr><td style='text-align:left;font-size:${sF}px'><b>${wName}</b> - ${wDetailedForecast}" }
        if(!smallTileF) { if(x >= 12 && x <= 13) forecastTable5 += "<tr><td style='text-align:left'><b>${wName}</b> - ${wDetailedForecast}" }
    }
    
    if(forecastTable1) {
        forecastTable1a =  forecastTable1.take(1009)
        forecastTable1a += "</table></div>"
        tileDevice.forecastData1(forecastTable1a)       
    }
    
    if(forecastTable2) {
        forecastTable2a =  forecastTable2.take(1009)
        forecastTable2a += "</table></div>"
        tileDevice.forecastData2(forecastTable2a)       
    }
    
    if(forecastTable3) {
        forecastTable3a =  forecastTable3.take(1009)
        forecastTable3a += "</table></div>"
        tileDevice.forecastData3(forecastTable3a)       
    }
    
    if(forecastTable4) {
        forecastTable4a =  forecastTable4.take(1009)
        forecastTable4a += "</table></div>"
        tileDevice.forecastData4(forecastTable4a)       
    }
    
    if(forecastTable5) {
        forecastTable5a =  forecastTable5.take(1009)
        forecastTable5a += "</table></div>"
        tileDevice.forecastData5(forecastTable5a)       
    }
}

def getAlertData(evt) {
    if(logEnable) log.debug "In getAlertData (${state.version})"
    dataDevice.getAlertData()
    pauseExecution(1000)
    
    alertTitle = dataDevice.currentValue("alertTitle")
    log.info "alertTitle: ${alertTitle}"
    
    for(x=0;x<5;x++) {
        alertStatus = dataDevice.currentValue("alertStatus_$x")
        alertMessageType = dataDevice.currentValue("alertMessageType_$x")
        alertCategory = dataDevice.currentValue("alertCategory_$x")
        alertSeverity = dataDevice.currentValue("alertSeverity_$x")
        alertCertainty = dataDevice.currentValue("alertCertainty_$x")
        alertUrgency = dataDevice.currentValue("alertUrgency_$x")
        alertEvent = dataDevice.currentValue("alertEvent_$x")
        alertHeadline = dataDevice.currentValue("alertHeadline_$x")
        alertDescription = dataDevice.currentValue("alertDescription_$x")
        alertInstruction = dataDevice.currentValue("alertInstruction_$x")

        log.info "alertStatus: ${alertStatus} - alertCategory: ${alertCategory}"

        titleFontSize = alertFontSize + 2
        statusFontSize = alertFontSize - 2

        if(alertDescription != "No Data") {
            alertTable =  "<div style='overflow:auto;height:90%'><table width=100% align=center>"
            alertTable += "<tr><td style='width:100%'>"
            alertTable += "<span style='font-size:${alertFontSize}px;font-weight:bold'>${alertHeadline}</span><br>"
            alertTable += "<span style='font-size:${statusFontSize}px;font-weight:bold'>Message Type: ${alertMessageType} - Urgency: ${alertUrgency} - Severity: ${alertSeverity} - Certainty: ${alertCertainty}</span><hr>"

            alertAnnouncement = "<span style='font-size:${statusFontSize}px'>Message Type: ${alertMessageType} - Urgency: ${alertUrgency} - Severity: ${alertSeverity} - Certainty: ${alertCertainty}</span>"
            
            if(alertDescription) {
                aTableSize = alertTable.size()
                charLefta = 980 - aTableSize
                alertDescription1 = alertDescription.take(charLefta)
                alertTable += "<span style='font-size:${alertFontSize}px'>${alertDescription1}"
            }
            alertTable += "<hr>"
            if(alertInstruction) {
                bTableSize = alertTable.size()
                charLeftb = 980 - bTableSize
                if(charLeftb > 0) {
                    alertInstruction1 = alertInstruction.take(charLeftb)
                    alertTable += "${alertInstruction1}"
                }
            }
            alertTable += "</span></table></div>"
            dataDevice.on()
        } else {
            alertTable =  "<table width=100% align=center>"
            alertTable += "<tr><td style='width:100%'>"
            alertTable += "<span style='font-size:${titleFontSize}px;font-weight:bold'>No alerts</span><br>"
            alertTable += "</table>"
            dataDevice.off()
        }
        if(x == 0) tileDevice.alertData1(alertTable)
        if(x == 1) tileDevice.alertData2(alertTable)
        if(x == 2) tileDevice.alertData3(alertTable)
        if(x == 3) tileDevice.alertData4(alertTable)
        if(x == 4) tileDevice.alertData5(alertTable)
    }
    
    alertHeadline1 = dataDevice.currentValue("alertHeadline_0")
    alertHeadline2 = dataDevice.currentValue("alertHeadline_1")
    alertHeadline3 = dataDevice.currentValue("alertHeadline_2")
    alertHeadline4 = dataDevice.currentValue("alertHeadline_3")
    alertHeadline5 = dataDevice.currentValue("alertHeadline_4")
        
    alertSummaryTable =  "<div style='overflow:auto;height:90%'><table width=100%>"
    alertSummaryTable += "<tr><td style='width:100%;text-align:left'>"
    alertSummaryTable += "<span style='font-size:${alertFontSize}px'>- ${alertHeadline1}<br>- ${alertHeadline2}<br>- ${alertHeadline3}<br>- ${alertHeadline4}<br>- ${alertHeadline5}</span><br>"
    alertSummaryTable += "</table></div>"
        
    tileDevice.alertSummaryData(alertSummaryTable)
        
    initializeAlerts()
} 

def alertNotifications(evt) {
    if(logEnable) log.debug "In alertNotifications (${state.version}) ***"
    notifyNow = false
    urgencyNow = false
    severityNow = false
    
    // Urgency (immediate, expected, future, past, unknown)
    // Severity (extreme, severe, moderate, minor, unknown)
    // Certainty (observed, likely, possible, unlikely, unknown)
    
    checkSeverity = dataDevice.currentValue('alertSeverity')
    checkUrgency = dataDevice.currentValue('alertUrgency')
    
    notifyOnUrgency.each { ita ->
        if(logEnable) log.debug "In alertNotifications - Urgency - ${checkUrgency} vs ${ita}"
        if(checkUrgency.toLowerCase() == ita || ita == 'all') {
            urgencyNow = true
        }
    }
           
    notifyOnSeverity.each { itb ->
        if(logEnable) log.debug "In alertNotifications - Severity - ${checkSeverity} vs ${itb}"
        if(checkSeverity.toLowerCase() == itb || itb == 'all') {
            severityNow = true
        }
    }
           
    if(!useNotify) {    // and
        if(urgencyNow && severityNow) notifyNow = true
    } else {            // or
        if(urgencyNow || severityNow) notifyNow = true
    }
    
    if(logEnable) log.debug "In alertNotifications - severityNow: ${severityNow} - urgencyNow: ${urgencyNow} - notifyNow: ${notifyNow}"
    if(notifyNow) {
        messageHandler()
        if(sendPushMessage) pushHandler()
    }
}
 
def getAsthmaData() {
    if(logEnable) log.debug "In getAsthmaData (${state.version})"
    dataDevice.getAsthmaData()
    pauseExecution(1000)
    
    asthmaIndexToday = dataDevice.currentValue('asthmaIndexToday')
    asthmaCategoryToday = dataDevice.currentValue('asthmaCategoryToday')
    asthmaTriggersToday = dataDevice.currentValue('asthmaTriggersToday')
    asthmaIndexTomorrow = dataDevice.currentValue('asthmaIndexTomorrow')
    asthmaCategoryTomorrow = dataDevice.currentValue('asthmaCategoryTomorrow')
    asthmaTriggersTomorrow = dataDevice.currentValue('asthmaTriggersTomorrow')   
    asthmaIndexYesterday = dataDevice.currentValue('asthmaIndexYesterday')
    asthmaCategoryYesterday = dataDevice.currentValue('asthmaCategoryYesterday')
    asthmaTriggersYesterday = dataDevice.currentValue('asthmaTriggersYesterday')
    
	if(logEnable) log.debug "In asthmaYesterdayTileMap"
	asthmaDataYesterday =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:center'>"
	asthmaDataYesterday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>Asthma Forecast Yesterday<br>${zipCode}</span>"
	asthmaDataYesterday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeIndex}px'>${asthmaIndexYesterday} - ${asthmaCategoryYesterday}</span>"
	asthmaDataYesterday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>${asthmaTriggersYesterday}</span>"
	asthmaDataYesterday += "</table></div>"
	tileDevice.asthmaYesterdayData(asthmaDataYesterday)

	if(logEnable) log.debug "In asthmaTodayTileMap"
	asthmaDataToday =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:center'>"
	asthmaDataToday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>Asthma Forecast Today<br>${zipCode}</span>"
	asthmaDataToday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeIndex}px'>${asthmaIndexToday} - ${asthmaCategoryToday}</span>"
	asthmaDataToday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>${asthmaTriggersToday}</span>"
	asthmaDataToday += "</table></div>"
	tileDevice.asthmaTodayData(asthmaDataToday)

	if(logEnable) log.debug "In asthmaTomorrowTileMap"
	asthmaDataTomorrow =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:center'>"
	asthmaDataTomorrow += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>Asthma Forecast Tomorrow<br>${zipCode}</span>"
	asthmaDataTomorrow += "<tr><td style='text-align:center'><span style='font-size:${fontSizeIndex}px'>${asthmaIndexTomorrow} - ${asthmaCategoryTomorrow}</span>"
	//asthmaDataTomorrow += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>${asthmaTriggersTomorrow}</span>"
	asthmaDataTomorrow += "</table></div>"
	tileDevice.asthmaTomorrowData(asthmaDataTomorrow)   
}


def getPollenData() {
    if(logEnable) log.debug "In getPollenData (${state.version})"
    dataDevice.getPollenData()
    pauseExecution(1000)
    
    pollenIndexToday = dataDevice.currentValue('pollenIndexToday')
    pollenCategoryToday = dataDevice.currentValue('pollenCategoryToday')
    pollenTriggersToday = dataDevice.currentValue('pollenTriggersToday')
    pollenIndexTomorrow = dataDevice.currentValue('pollenIndexTomorrow')
    pollenCategoryTomorrow = dataDevice.currentValue('pollenCategoryTomorrow')
    pollenTriggersTomorrow = dataDevice.currentValue('pollenTriggersTomorrow')   
    pollenIndexYesterday = dataDevice.currentValue('pollenIndexYesterday')
    pollenCategoryYesterday = dataDevice.currentValue('pollenCategoryYesterday')
    pollenTriggersYesterday = dataDevice.currentValue('pollenTriggersYesterday')
    
	if(logEnable) log.debug "In pollenYesterdayTileMap"
	pollenDataYesterday =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:center'>"
	pollenDataYesterday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>Pollen Forecast Yesterday<br>${zipCode}</span>"
	pollenDataYesterday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeIndex}px'>${pollenIndexYesterday} - ${pollenCategoryYesterday}</span>"
	pollenDataYesterday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>${pollenTriggersYesterday}</span>"
	pollenDataYesterday += "</table></div>"
	tileDevice.pollenYesterdayData(pollenDataYesterday)

	if(logEnable) log.debug "In pollenTodayTileMap"
	pollenDataToday =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:center'>"
	pollenDataToday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>Pollen Forecast Today<br>${zipCode}</span>"
	pollenDataToday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeIndex}px'>${pollenIndexToday} - ${pollenCategoryToday}</span>"
	pollenDataToday += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>${pollenTriggersToday}</span>"
	pollenDataToday += "</table></div>"
	tileDevice.pollenTodayData(pollenDataToday)

	if(logEnable) log.debug "In pollenTomorrowTileMap"
	pollenDataTomorrow =  "<div style='overflow:auto;height:90%'><table style='width:100%;text-align:center'>"
	pollenDataTomorrow += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>Pollen Forecast Tomorrow<br>${zipCode}</span>"
	pollenDataTomorrow += "<tr><td style='text-align:center'><span style='font-size:${fontSizeIndex}px'>${pollenIndexTomorrow} - ${pollenCategoryTomorrow}</span>"
	pollenDataTomorrow += "<tr><td style='text-align:center'><span style='font-size:${fontSizeTriggers}px'>${pollenTriggersTomorrow}</span>"
	pollenDataTomorrow += "</table></div>"
	tileDevice.pollenTomorrowData(pollenDataTomorrow)   
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    state.theMsg = ""
    
	def values = "${notifyMsg}".split(";")
	vSize = values.size()
    count = vSize.toInteger()
    def randomKey = new Random().nextInt(count)
	theMessage = values[randomKey]
    
    theSeverity = dataDevice.currentValue('alertSeverity')
    theCertainty = dataDevice.currentValue('alertCertainty')
    theUrgency = dataDevice.currentValue('alertUrgency')
    theDescription = dataDevice.currentValue('alertDescription')
    theInstruction = dataDevice.currentValue('alertInstruction')
    
    if(theMessage.contains("%severity%")) {theMessage = theMessage.replace('%severity%', theSeverity)}
    if(theMessage.contains("%certainty%")) {theMessage = theMessage.replace('%certainty%', theCertainty)}
    if(theMessage.contains("%urgency%")) {theMessage = theMessage.replace('%urgency%', theUrgency)}
    if(theMessage.contains("%desc%")) {theMessage = theMessage.replace('%desc%', theDescription)}
    if(theMessage.contains("%inst%")) {theMessage = theMessage.replace('%inst%', theInstruction)}
    if(theMessage.contains("%lbreak%")) {theMessage = theMessage.replace('%lbreak%', '\n')}
    
    state.theMsg = theMessage
	if(logEnable) log.debug "In messageHandler - Random Pre - vSize: ${vSize}, randomKey: ${randomKey}, noMsg: ${state.theMsg}"
}

def pushHandler() {
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${state.theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.theMsg = ""
}



    
def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice("WDG - " + dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device Weather Dot Gov - ${dataName}"
        try {
            addChildDevice("BPTWorld", "Weather Dot Gov Data Driver", "WDG - " + dataName, 1234, ["name": "WDG - ${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child tile device has been created! (WDG - ${dataName})"
            statusMessageD = "<b>Device has been been created. (WDG - ${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Weather Dot Gov unable to create data device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (WDG - ${dataName}) already exists.</b>"
    }
    return statusMessageD
}

def createTileChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageT = ""
    if(!getChildDevice("WDG - " + tileName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device Weather Dot Gov - ${tileName}"
        try {
            addChildDevice("BPTWorld", "Weather Dot Gov Tile Driver", "WDG - " + tileName, 5678, ["name": "WDG - ${tileName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child tile device has been created! (WDG - ${tileName})"
            statusMessageT = "<b>Device has been been created. (WDG - ${tileName})</b>"
        } catch (e) { if(logEnable) log.debug "Weather Dot Gov unable to create tile device - ${e}" }
    } else {
        statusMessageT = "<b>Device Name (WDG - ${tileName}) already exists.</b>"
    }
    return statusMessageT
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
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
        //if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        //if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
