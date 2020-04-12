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
 *  V1.0.2 - 04/12/20 - Fixed Forecast from exceeding the 1024 limit
 *  V1.0.1 - 04/08/20 - Fixed typo
 *  V1.0.0 - 04/07/20 - Initial release.
 *
 */

def setVersion(){
	state.version = "v1.0.2"
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
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Making the Weather.gov data usable with Hubitat."
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            paragraph "Each child app needs a TWO virtual devices. One to store the weather raw data and the second to store the tile data. Enter a short descriptive name for each device."
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
            paragraph "Station ID can be found be visitng the <a href='http://weather.gov' target='_blank'>Weather.gov</a> homepage and putting in your zipcode or City, ST to find the ID (ie. KASH)"
            input "station", "text", title: "Station ID (UPPERCASE)", submitOnChange:true
            input "unitFormat", "enum", title: "Unit Format", required: true, options: ["Imperial", "Metric"], submitOnChange:true
            if(lat && lng && station && unitFormat) sendDataOptions()
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
            if(updateTimeA) {
                href "alertTileOptions", title:"${getImage("optionsGreen")} Create Alert Weather Tile", description:"Click here for Options"
            } else {
                href "alertTileOptions", title:"${getImage("optionsRed")} Create Alert Weather Tile", description:"Click here for Options"
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
            
            input "updateTimeC", "enum", title: "How ofter to update tile?", options: ["1_Min","5_Min","10_Min","15_Min","30_Min","1_Hour","3_Hour"], submitOnChange:true  
            
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
            paragraph "Daily data is only available in Imperial. Data is automaticaly updated every 3 hours."
            input "smallTileF", "bool", title: "Make the print smaller on tile", description: "", submitOnChange:true
            paragraph "<hr>"
            input "updateTileF", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileF) {
                getWeeklyData()
                pauseExecution(1000)
                fTile1 = tileDevice.currentValue('forecastTable1')
                fTile2 = tileDevice.currentValue('forecastTable2')
                fTile3 = tileDevice.currentValue('forecastTable3')
                fTile4 = tileDevice.currentValue('forecastTable4')
                               
                app?.updateSetting("updateTileF",[value:"false",type:"bool"])
            }
            if(fTile1) fSize1 = fTile1.size()
            if(fTile2) fSize2 = fTile2.size()
            if(fTile3) fSize3 = fTile3.size()
            if(fTile4) fSize4 = fTile4.size()
            
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
		section(getFormat("header-green", "${getImage("Blank")}"+" Alert Tile Options")) {
            paragraph "<hr>"
            paragraph "<b>Coming soon</b>"
            paragraph "<hr>"
            paragraph "Time to setup the Alert Tile for use with Dashboards!"
            //paragraph "Note: Although you have the option on the main page between Imperial or Metric. Forecast data is only available in Imperial."
            paragraph "<hr>"
            input "updateTileA", "bool", title: "Manually Update Tiles", description: "", submitOnChange:true
            if(updateTileA) {
                //getWeeklyData()
                //pauseExecution(1000)
                //tile1 = tileDevice.currentValue('weeklyDataTile01')
                               
                app?.updateSetting("updateTileA",[value:"false",type:"bool"])
            }
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
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def sendDataOptions() {
    if(logEnable) log.debug "In sendDataOptions (${state.version})"
    
    theOptions = "${lat}:${lng}:${station}:${unitFormat}"
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

    fontSize = "45"
            
    currentTable1 =  "<table width=100% align=center>"
    currentTable1 += "<tr><td width=90><img src='https://${cIcon}'>"
    currentTable1 += "<td><span style='font-weight:bold'>${cTextDescription}</span><br><span style='font-size:${fontSize}px;font-weight:bold'>${cTemp1}</span>"
    currentTable1 += "<td><b>Humidity:</b> ${cRelativeHumidity}%<br><b>Wind Speed:</b> ${cWindSpeed}<br><b>Barometer:</b> ${cBarometricPressure}"
    currentTable1 += "<td><b>Dewpoint:</b> ${cDewpoint}<br><b>Visibility:</b> ${cVisibility}<br><b>Last Updated:</b>"
    currentTable1 += "</tr><tr><td colspan=4 align=center><small>${cLastUpdated}</small>"
    currentTable1 += "</tr></table>"

    state.currentTable1 = currentTable1
    
    currentTable2 =  "<table width=100% align=center>"
    currentTable2 += "<tr><td width=100 align=center><img src='https://${cIcon}'><br>"
    currentTable2 += "<span style='font-weight:bold'>${cTextDescription}</span><br>"
    currentTable2 += "<span style='font-size:${fontSize}px;font-weight:bold'>${cTemp1}</span><br>"
    currentTable2 += "<b>Humidity:</b> ${cRelativeHumidity}%<br>"
    currentTable2 += "<b>Wind Speed:</b> ${cWindSpeed}<br>"
    currentTable2 += "<b>Barometer:</b> ${cBarometricPressure}<br>"
    currentTable2 += "<b>Dewpoint:</b> ${cDewpoint}<br>"
    currentTable2 += "<b>Visibility:</b> ${cVisibility}<br>"
    currentTable2 += "<b>Last Updated:</b><br>"
    currentTable2 += "<small>${cLastUpdated}</small>"
    currentTable2 += "</tr></table>"
    
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
        weeklyTable_$x += "<table width=100% align=center>"
        weeklyTable_$x += "<tr><td width=90 height=60 align=center><small><b>${wName}</b></small></td></tr>"
        weeklyTable_$x += "<tr><td width=90 align=center><img src='https://${wIcon}'></td></tr>"
        weeklyTable_$x += "<tr><td width=90 height=100 align=center><small>${wShortForecast}</small></td></tr>"
        weeklyTable_$x += "<tr><td width=90 align=center><span style='font-weight:bold'>${wTemp1}</span></td></tr>"
        weeklyTable_$x += "</table>"
        
        if(x == 0) forecastTable1 =  "<table width=100% align=left>"
        if(smallTileF) { if(x >= 0 && x <= 3) forecastTable1 += "<tr><td align=left><small><b>${wName}</b> - ${wDetailedForecast}</small></td></tr>" }
        if(!smallTileF) { if(x >= 0 && x <= 3) forecastTable1 += "<tr><td align=left><b>${wName}</b> - ${wDetailedForecast}</td></tr>" }
        if(x == 3) forecastTable1 += "</table>"
        
        if(x == 4) forecastTable2 =  "<table width=100% align=left>"
        if(smallTileF) { if(x >= 4 && x <= 7) forecastTable2 += "<tr><td align=left><small><b>${wName}</b> - ${wDetailedForecast}</small></td></tr>" }
        if(!smallTileF) { if(x >= 4 && x <= 7) forecastTable2 += "<tr><td align=left><b>${wName}</b> - ${wDetailedForecast}</td></tr>" }
        if(x == 7) forecastTable2 += "</table>"
        
        if(x == 8) forecastTable3 =  "<table width=100% align=left>"
        if(smallTileF) { if(x >= 8 && x <= 11) forecastTable3 += "<tr><td align=left><small><b>${wName}</b> - ${wDetailedForecast}</small></td></tr>" }
        if(!smallTileF) { if(x >= 8 && x <= 11) forecastTable3 += "<tr><td align=left><b>${wName}</b> - ${wDetailedForecast}</td></tr>" }
            
        if(x == 11) forecastTable3 += "</table>"
        
        if(x == 12) forecastTable4 =  "<table width=100% align=left>"
        if(smallTileF) { if(x >= 12 && x <= 13) forecastTable4 += "<tr><td align=left><small><b>${wName}</b> - ${wDetailedForecast}</small></td></tr>" }
        if(!smallTileF) { if(x >= 12 && x <= 13) forecastTable4 += "<tr><td align=left><b>${wName}</b> - ${wDetailedForecast}</td></tr>" }
        if(x == 13) forecastTable4 += "</table>"
        
        if(logEnable) log.debug "In weeklytTileOptions - Sending 'weeklyTable' to tile device (${tileDevice})"
        tileDevice.weeklyData(weeklyTable_$x)
    }
    
    tileDevice.forecastData1(forecastTable1.take(1020))
    tileDevice.forecastData2(forecastTable2.take(1020))
    tileDevice.forecastData3(forecastTable3.take(1020))
    tileDevice.forecastData4(forecastTable4.take(1020))
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

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " Weather Dot Gov - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Weather Dot Gov - @BPTWorld<br>${state.version}</div>"
	}       
}
