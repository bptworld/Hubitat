/**
 *  ****************  Weather Dot Gov Data Driver  ****************
 *
 *  Design Usage:
 *  Making the Weather.gov data usable with Hubitat.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 * ------------------------------------------------------------------------------------------------------------------------------
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
 *  1.0.8 - 04/19/20 - Tweaking things
 *  1.0.7 - 04/18/20 - Added switch capability
 *  1.0.6 - 04/18/20 - Fixed another issue with Alerts
 *  1.0.5 - 04/18/20 - Fixed typo with alert description
 *  1.0.4 - 04/17/20 - More adjustments
 *  1.0.3 - 04/17/20 - More adjustments for alerts
 *  1.0.2 - 04/17/20 - Added alerts
 *  1.0.1 - 04/13/20 - Fixed Wind speed, precipitation calculations
 *  1.0.0 - 04/07/20 - Initial release
 */

metadata {
	definition (name: "Weather Dot Gov Data Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Weather%20Dot%20Gov/WDG-data-driver.groovy") {
        capability "Switch"
   		capability "Actuator"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"

        command "switch"
        command "dataOptions"
        command "getPointsData"
        command "getWeeklyData"
        command "getWeatherData"
        command "getAlertData"
        //command "getRadarData"
		
        attribute "switch", "string"
        
        attribute "lat", "string"
        attribute "lng", "string"
        attribute "station", "string"
        attribute "unitFormat", "string"
        
    	attribute "pointsOffice", "string"
        attribute "pointsForecastZone", "string"
        attribute "pointsCounty", "string"        
        attribute "pointsFireWeatherZone", "string"
        attribute "pointsTimeZone", "string"
        attribute "pointsRadarStation", "string" 
		attribute "pointsGridX", "string"
        attribute "pointsGridY", "string"
 
        attribute "responseStatus", "string"
        attribute "lastUpdated", "string"
        
        attribute "zforecast_1", "string"
        attribute "zforecast_2", "string"
        attribute "zforecast_3", "string"
        attribute "zforecast_4", "string"
        attribute "zforecast_5", "string"
        attribute "zforecast_6", "string"
        attribute "zforecast_7", "string"
        attribute "zforecast_8", "string"
        attribute "zforecast_9", "string"
        attribute "zforecast_10", "string"
        attribute "zforecast_11", "string"
        attribute "zforecast_12", "string"
        attribute "zforecast_13", "string"
        attribute "zforecast_14", "string"
        
        attribute "textDescription", "string"
        attribute "icon", "string"
        attribute "presentWeather", "string"
        attribute "temperature", "number"
        attribute "dewpoint", "number"
        attribute "windDirection", "string"
        attribute "windSpeed", "number"
        attribute "windGust", "number"
        attribute "barometricPressure", "number"
        attribute "seaLevelPressure", "number"
        attribute "visibility", "number"
        attribute "maxTemperatureLast24Hours", "number"
        attribute "minTemperatureLast24Hours", "number"
        attribute "precipitationLastHour", "number"
        attribute "precipitationLast3Hours", "number"
        attribute "precipitationLast6Hours", "number"
        attribute "relativeHumidity", "number"
        attribute "windChill", "number"
        attribute "heatIndex", "number"
        
        attribute "alertTitle", "string"
        attribute "alertStatus", "string"
        attribute "alertMessageType", "string"
        attribute "alertCategory", "string"
        attribute "alertSeverity", "string"
        attribute "alertCertainty", "string"
        attribute "alertUrgency", "string"
        attribute "alertEvent", "string"
        attribute "alertHeadline", "string"
        attribute "alertDescription", "string"
        attribute "alertInstruction", "string"
        attribute "alertInstruction", "string"
        
        attribute "radar", "string"
	}
	preferences() {    	
        section(){
			input name: "about", type: "paragraph", title: "<b>Weather Data from Weather.gov</b><br>This driver holds the raw data for use with dashboards", description: ""
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
        }
    }
}

def on() {
    if(logEnable) log.info "In on - Turning Switch On"
    device.on
    sendEvent(name: "switch", value: "on", displayed: true)
}

def off() {
    if(logEnable) log.info "In off - Turning Switch Off"
    device.off
    sendEvent(name: "switch", value: "off", displayed: true)
} 

def dataOptions(data) {
    if(logEnable) log.debug "In dataOptions"
    def (lat,lng,station,unitFormat) = data.split(':')
    sendEvent(name: "lat", value: lat)
    sendEvent(name: "lng", value: lng)
    sendEvent(name: "station", value: station)
    sendEvent(name: "unitFormat", value: unitFormat)
    pauseExecution(1000)
    getPointsData()
}

def getPointsData() {
    if(logEnable) log.debug "In getPointsData"
	currentDate = new Date()
    sendEvent(name: "responseStatus", value: "Getting Points Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)

    lat1 = device.currentValue('lat')
    lng1 = device.currentValue('lng')
	pointsURL = "https://api.weather.gov/points/${lat1},${lng1}"
	if(logEnable) log.debug "In getPointsData - pointsURL: ${pointsURL}"
	def requestParams =
		[
			uri: pointsURL,
            requestContentType: "application/json",
			contentType: "application/json",
            timeout: 30,
		]
    try {
        httpGet(requestParams) { response ->
            if(logEnable) log.info "In getPointsData - response: ${response.status}"
            
            if(response.status == 200) {	
                def pointsOffice = response.data.properties.cwa
                if(logEnable) log.info "pointsOffice: ${pointsOffice}"
                sendEvent(name: "pointsOffice", value: pointsOffice)
                
                def pointsForecastZone1 = response.data.properties.forecastZone
                //ie. https://api.weather.gov/zones/forecast/MAZ005
                int fSize = pointsForecastZone1.size()
                fZone = fSize - 6
                def pointsForecastZone = pointsForecastZone1.drop(fZone)
                if(logEnable) log.info "pointsForecastZone: ${pointsForecastZone}"
                sendEvent(name: "pointsForecastZone", value: pointsForecastZone)
                
                def pointsCounty1 = response.data.properties.county              
                //ie. https://api.weather.gov/zones/county/MAC017
                int cSize = pointsCounty1.size()
                cZone = cSize - 6
                def pointsCounty = pointsCounty1.drop(cZone)            
                if(logEnable) log.info "pointsCounty: ${pointsCounty}"
                sendEvent(name: "pointsCounty", value: pointsCounty)
                
                def pointsFireWeatherZone1 = response.data.properties.fireWeatherZone              
                //ie. https://api.weather.gov/zones/fire/MAZ005
                int fwSize = pointsFireWeatherZone1.size()
                fwZone = fwSize - 6
                def pointsFireWeatherZone = pointsFireWeatherZone1.drop(fwZone)               
                if(logEnable) log.info "pointsFireWeatherZone: ${pointsFireWeatherZone}"
                sendEvent(name: "pointsFireWeatherZone", value: pointsFireWeatherZone)
                
                def pointsTimeZone = response.data.properties.timeZone
                if(logEnable) log.info "pointsTimeZone: ${pointsTimeZone}"
                sendEvent(name: "pointsTimeZone", value: pointsTimeZone)

                def pointsRadarStation = response.data.properties.radarStation
                if(logEnable) log.info "pointsRadarStation: ${pointsRadarStation}"
                sendEvent(name: "pointsRadarStation", value: pointsRadarStation)
                
                def pointsGridX = response.data.properties.gridX
                sendEvent(name: "pointsGridX", value: pointsGridX)

                def pointsGridY = response.data.properties.gridY
                sendEvent(name: "pointsGridY", value: pointsGridY)
            } else {
                if(logEnable) log.debug "In getPointsData - Bad Request - ${response.status} - Something went wrong, please try again."
                runOnce(1,getPointsData)
            }
            currentDate = new Date()
            sendEvent(name: "responseStatus", value: response.status)
            sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
        }
    } catch (e) {
        log.error e
        theError = "${e}"
        def reason = theError.split(':')
        currentDate = new Date()
        sendEvent(name: "responseStatus", value: reason[1])
        sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    }
}

def getWeeklyData() {
    if(logEnable) log.debug "In getWeeklyData"
	currentDate = new Date()
    sendEvent(name: "responseStatus", value: "Getting Weather Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)

    gridOffice = device.currentValue('pointsOffice')
    pointsGridX = device.currentValue('pointsGridX')
    pointsGridY = device.currentValue('pointsGridY')
    
    if(pointsGridX == null || pointsGridY == null) getPointsData()
    
	forecastURL = "https://api.weather.gov/gridpoints/${gridOffice}/${pointsGridX},${pointsGridY}/forecast"
	if(logEnable) log.debug "In getWeeklyData - forecastURL: ${forecastURL}"
    log.info "forecastURL: ${forecastURL}"
	def requestParams =
		[
			uri: forecastURL,
            requestContentType: "application/json",
			contentType: "application/json",
            timeout: 30,
		]
    try {
        httpGet(requestParams) { response ->
            if(logEnable) log.info "In getWeeklyData - response: ${response.status}"
            
            if(response.status == 200) {
                for(x=0;x<14;x++) {
                    def number = response.data.properties.periods.number[x]
                    def name = response.data.properties.periods.name[x]
                    def temperature = response.data.properties.periods.temperature[x]
                    def temperatureUnit = response.data.properties.periods.temperatureUnit[x]
                    def temperatureTrend = response.data.properties.periods.temperatureTrend[x]
                    def windSpeed = response.data.properties.periods.windSpeed[x]
                    def windDirection = response.data.properties.periods.windDirection[x]
                    def icona = response.data.properties.periods.icon[x]
                    def icon = icona.replace("https://", "")
                    def shortForecast = response.data.properties.periods.shortForecast[x]
                    def detailedForecast = response.data.properties.periods.detailedForecast[x]
                    def forcast = "${number}:${name}:${temperature}:${temperatureUnit}:${temperatureTrend}:${windSpeed}:${windDirection}:${icon}:${shortForecast}:${detailedForecast}"
                    y = x+1
                    sendEvent(name: "zforecast_$y", value: forcast)
                }
            } else {
                if(logEnable) log.debug "In getWeeklyData - Bad Request - ${response.status} - Something went wrong, please try again."
                runOnce(1,getWeeklyData)
            }
            currentDate = new Date()
            sendEvent(name: "responseStatus", value: response.status)
            sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
        }
    } catch (e) {
        log.error e
        theError = "${e}"
        def reason = theError.split(':')
        currentDate = new Date()
        sendEvent(name: "responseStatus", value: reason[1])
        sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    }
}

def getWeatherData() {
    if(logEnable) log.debug "In getWeatherData"

    sendEvent(name: "responseStatus", value: "Getting Weather Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    
    unitFormat1 = device.currentValue('unitFormat')
    station1 = device.currentValue('station')
    forecastURL = "https://api.weather.gov/stations/${station1}/observations/latest"
    
	if(logEnable) log.debug "In getWeatherData - forecastURL: ${forecastURL}"
	def requestParams =
		[
			uri: forecastURL,
            requestContentType: "application/json",
			contentType: "application/json",
            timeout: 30,
		]
    try {
        httpGet(requestParams) { response ->
            if(logEnable) log.info "In getWeatherData - response: ${response.status}"
            
            if(response.status == 200) {
                def textDescription = response.data.properties.textDescription
                sendEvent(name: "textDescription", value: textDescription)
                
                def icona = response.data.properties.icon
                def icon = icona.replace("https://", "")
                sendEvent(name: "icon", value: icon)
                                
                def timestamp = response.data.properties.timestamp
                sendEvent(name: "timestamp", value: timestamp)
                                
                def xtemperature = response.data.properties.temperature.value
                if(!xtemperature) {
                    temperature = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        cTOf(xtemperature)
                        temperature = theUnit
                    } else {
                        unitI = xtemperature.toFloat()
                        temperature = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - temperature: ${temperature}"
                sendEvent(name: "temperature", value: temperature)
                     
                
                def xdewpoint = response.data.properties.dewpoint.value
                if(!xdewpoint) {
                    temperature = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        cTOf(xdewpoint)
                        dewpoint = theUnit
                    } else {
                        unitI = xdewpoint.toFloat()
                        dewpoint = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - dewpoint: ${dewpoint}"
                sendEvent(name: "dewpoint", value: dewpoint)
                
                
                def xwindDirection = response.data.properties.windDirection.value
                if(!xwindDirection) {
                    windDirection = "No Data"
                } else {
                    direction(xwindDirection)
                    windDirection = theUnit
                }
                if(logEnable) log.debug "In getWeatherData - windDirection: ${windDirection}"
                sendEvent(name: "windDirection", value: windDirection)

                
                def xwindSpeed = response.data.properties.windSpeed.value
                if(!xwindSpeed) {
                    windSpeed = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mpsTOmph(xwindSpeed)
                        windSpeed = theUnit
                    } else {
                        unitI = xwindSpeed.toFloat()
                        windSpeed = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - windSpeed: ${windSpeed}"
                sendEvent(name: "windSpeed", value: windSpeed)
                
                
                def xwindGust = response.data.properties.windGust.value
                if(!xwindGust) {
                    windGust = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        kphTOmph(xwindGust)
                        windGust = theUnit
                    } else {
                        unitI = xwindGust.toFloat()
                        windGust = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - windGust: ${windGust}"
                sendEvent(name: "windGust", value: windGust)
                
                
                def xbarometricPressure = response.data.properties.barometricPressure.value
                if(!xbarometricPressure) {
                    barometricPressure = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mbTOinhg(xbarometricPressure)
                        barometricPressure = theUnit
                    } else {
                        unitI = xbarometricPressure.toFloat()
                        barometricPressure = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - barometricPressure: ${barometricPressure}"
                sendEvent(name: "barometricPressure", value: barometricPressure)
                
                
                def xseaLevelPressure = response.data.properties.seaLevelPressure.value
                if(!xseaLevelPressure) {
                    seaLevelPressure = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mbTOinhg(xseaLevelPressure)
                        seaLevelPressure = theUnit
                    } else {
                        unitI = xseaLevelPressure.toFloat()
                        seaLevelPressure = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - seaLevelPressure: ${seaLevelPressure}"
                sendEvent(name: "seaLevelPressure", value: seaLevelPressure)
                
                
                def xvisibility = response.data.properties.visibility.value
                if(!xvisibility) {
                    visibility = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mTOft(xvisibility)
                        visibility = theUnit
                    } else {
                        mTOk(xvisibility)
                        visibility = theUnit
                    }
                }
                if(logEnable) log.debug "In getWeatherData - visibility: ${visibility}"
                sendEvent(name: "visibility", value: visibility)
                
                
                def xmaxTemperatureLast24Hours = response.data.properties.maxTemperatureLast24Hours.value
                if(!xmaxTemperatureLast24Hours) {
                    maxTemperatureLast24Hours = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        cTOf(xmaxTemperatureLast24Hours)
                        maxTemperatureLast24Hours = theUnit
                    } else {
                        unitI = xmaxTemperatureLast24Hours.toFloat()
                        maxTemperatureLast24Hours = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - maxTemperatureLast24Hours: ${maxTemperatureLast24Hours}"
                sendEvent(name: "maxTemperatureLast24Hours", value: maxTemperatureLast24Hours)
                
                
                def xminTemperatureLast24Hours = response.data.properties.minTemperatureLast24Hours.value
                if(!xminTemperatureLast24Hours) {
                    minTemperatureLast24Hours = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        cTOf(xminTemperatureLast24Hours)
                        minTemperatureLast24Hours = theUnit
                    } else {
                        unitI = xminTemperatureLast24Hours.toFloat()
                        minTemperatureLast24Hours = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - minTemperatureLast24Hours: ${minTemperatureLast24Hours}"
                sendEvent(name: "minTemperatureLast24Hours", value: minTemperatureLast24Hours)
                
                
                def xprecipitationLastHour = response.data.properties.precipitationLastHour.value
                if(!xprecipitationLastHour) {
                    precipitationLastHour = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mmTOin(xprecipitationLastHour)
                        precipitationLastHour = theUnit
                    } else {
                        unitI = xprecipitationLastHour.toFloat()
                        precipitationLastHour = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - precipitationLastHour: ${precipitationLastHour}"
                sendEvent(name: "precipitationLastHour", value: precipitationLastHour)
                
                
                def xprecipitationLast3Hours = response.data.properties.precipitationLast3Hours.value
                if(!xprecipitationLast3Hours) {
                    precipitationLast3Hours = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mmTOin(xprecipitationLast3Hours)
                        precipitationLast3Hours = theUnit
                    } else {
                        unitI = xpxprecipitationLast3Hours.toFloat()
                        xprecipitationLast3Hours = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - precipitationLast3Hours: ${precipitationLast3Hours}"
                sendEvent(name: "precipitationLast3Hours", value: precipitationLast3Hours)
                
                
                def xprecipitationLast6Hours = response.data.properties.precipitationLast6Hours.value
                if(!xprecipitationLast6Hours) {
                    precipitationLast6Hours = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        mmTOin(xprecipitationLast6Hours)
                        precipitationLast6Hours = theUnit
                    } else {
                        unitI = xpxprecipitationLast6Hours.toFloat()
                        xprecipitationLast6Hours = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - precipitationLast6Hours: ${precipitationLast6Hours}"
                sendEvent(name: "precipitationLast6Hours", value: precipitationLast6Hours)
                
                
                def xrelativeHumidity = response.data.properties.relativeHumidity.value
                if(!xrelativeHumidity) {
                    relativeHumidity = "No Data"
                } else {
                    unitI = xrelativeHumidity.toFloat()
                    relativeHumidity = unitI.round(2)
                }
                if(logEnable) log.debug "In getWeatherData - relativeHumidity: ${relativeHumidity}"
                sendEvent(name: "relativeHumidity", value: relativeHumidity)
                
                
                def xwindChill = response.data.properties.windChill.value
                if(!xwindChill) {
                    windChill = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        cTOf(xwindChill)
                        windChill = theUnit
                    } else {
                        unitI = xwindChill.toFloat()
                        windChill = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - windChill: ${windChill}"
                sendEvent(name: "windChill", value: windChill)
                
                
                def xheatIndex = response.data.properties.heatIndex.value
                if(!xheatIndex) {
                    heatIndex = "No Data"
                } else {
                    if(unitFormat1 == "Imperial") {
                        cTOf(xheatIndex)
                        heatIndex = theUnit
                    } else {
                        unitI = xheatIndex.toFloat()
                        heatIndex = unitI.round(2)
                    }
                }
                if(logEnable) log.debug "In getWeatherData - heatIndex: ${heatIndex}"
                sendEvent(name: "heatIndex", value: heatIndex)    
            } else {
                if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Something went wrong, please try again."
                runOnce(1,getWeatherData)
            }
            currentDate = new Date()
            sendEvent(name: "responseStatus", value: response.status)
            sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
        }
    } catch (e) {
        log.error e
        theError = "${e}"
        def reason = theError.split(':')
        currentDate = new Date()
        sendEvent(name: "responseStatus", value: reason[1])
        sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    }
}


def getAlertData() {
    if(logEnable) log.debug "In getAlertData"
	currentDate = new Date()
    sendEvent(name: "responseStatus", value: "Getting Alert Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    
    zone = device.currentValue('pointsForecastZone')
    
    alertURL = "https://api.weather.gov/alerts?active=true&status=actual&zone=${zone}&urgency=expected"

    
	if(logEnable) log.debug "In getAlertData - alertURL: ${alertURL}"
	def requestParams =
		[
			uri: alertURL,
            requestContentType: "application/json",
			contentType: "application/json",
            timeout: 30,
		]
    try {
        httpGet(requestParams) { response ->
            if(logEnable) log.info "In getAlertData - response: ${response.status}"
            
            if(response.status == 200) {
                try { 
                    def alertTitle = response.data.title
                    if(alertTitle == null) alertTitle = "No Data"
                    if(logEnable) log.info "In getAlertData - alertTitle: ${alertTitle}"
                    sendEvent(name: "alertTitle", value: alertTitle)
                              
                    def alertStatus = response.data.features.properties.status
                    if(alertStatus == null) alertStatus = "No Data"
                    if(logEnable) log.info "In getAlertData - alertStatus: ${alertStatus}"
                    sendEvent(name: "alertStatus", value: alertStatus)
                                
                    def alertMessageType = response.data.features.properties.messageType
                    if(alertMessageType == null) alertMessageType = "No Data"
                    if(logEnable) log.info "In getAlertData - alertMessageType: ${alertMessageType}"
                    sendEvent(name: "alertMessageType", value: alertMessageType)
                              
                    def alertCategory = response.data.features.properties.category
                    if(alertCategory == null) alertCategory = "No Data"
                    if(logEnable) log.info "In getAlertData - alertCategory: ${alertCategory}"
                    sendEvent(name: "alertCategory", value: alertCategory)                
                
                    def alertSeverity = response.data.features.properties.severity
                    if(alertSeverity == null) alertSeverity = "No Data"
                    beforeSeverity = device.currentValue('alertSeverity')
                    if(logEnable) log.info "In getAlertData - alertSeverity: ${alertSeverity}"
                    if(alertSeverity == beforeSeverity) {
                        sendEvent(name: "alertSeverity", value: alertSeverity)
                    } else {
                        sendEvent(name: "alertSeverity", value: alertSeverity, isStateChange: true)
                    }
                               
                    def alertCertainty = response.data.features.properties.certainty
                    if(alertCertainty == null) alertCertainty = "No Data"
                    if(logEnable) log.info "In getAlertData - alertCertainty: ${alertCertainty}"
                    sendEvent(name: "alertCertainty", value: alertCertainty)
                                               
                    def alertUrgency = response.data.features.properties.urgency
                    if(alertUrgency == null) alertUrgency = "No Data"
                    beforeUrgency = device.currentValue('alertUrgency')
                    if(logEnable) log.info "In getAlertData - alertUrgency: ${alertUrgency}"
                    if(alertUrgency == beforeUrgency) {
                        sendEvent(name: "alertUrgency", value: alertUrgency)
                    } else {
                        sendEvent(name: "alertUrgency", value: alertUrgency, isStateChange: true)
                    }
                               
                    def alertEvent = response.data.features.properties.event
                    if(alertEvent == null) alertEvent = "No Data"
                    if(logEnable) log.info "In getAlertData - alertEvent: ${alertEvent}"
                    sendEvent(name: "alertEvent", value: alertEvent)
                               
                    def alertHeadline = response.data.features.properties.headline
                    if(alertHeadline == null) alertHeadline = "No Data"
                    if(logEnable) log.info "In getAlertData - alertHeadline: ${alertHeadline}"
                    sendEvent(name: "alertHeadline", value: alertHeadline)
                                
                    def alertDescription = response.data.features.properties.description
                    if(alertDescription == null) alertDescription = "No Data"
                    beforeDescription = device.currentValue('alertDescription')
                    if(logEnable) log.info "In getAlertData - alertDescription: ${alertDescription}"
                    if(alertDescription == beforeDescription) {
                        sendEvent(name: "alertDescription", value: alertDescription)
                    } else {
                        sendEvent(name: "alertDescription", value: alertDescription, isStateChange: true)
                    }
                                  
                    def alertInstruction = response.data.features.properties.instruction
                    if(alertInstruction == null) alertInstruction = "No Data"
                    if(logEnable) log.info "In getAlertData - alertInstruction: ${alertInstruction}"
                    sendEvent(name: "alertInstruction", value: alertInstruction) 
                } catch (e) {
                    if(logEnable) log.error "In getAlertData - ${e}"
                }                             
            } else {
                if(logEnable) log.debug "In getAlertData - Bad Request - ${response.status} - Something went wrong, please try again."
                runOnce(1,getAlertData)
            }
            currentDate = new Date()
            sendEvent(name: "responseStatus", value: response.status)
            sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
        }
    } catch (e) {
        log.error e
        theError = "${e}"
        def reason = theError.split(':')
        currentDate = new Date()
        sendEvent(name: "responseStatus", value: reason[1])
        sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    }
}


def getRadarData() {
    if(logEnable) log.debug "In getRadarData"
	currentDate = new Date()
    sendEvent(name: "responseStatus", value: "Getting Weather Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    
    station1 = device.currentValue('station')
	forecastURL = "https://api.weather.gov/stations/radar/${station1}"
	if(logEnable) log.debug "In getRadarData - forecastURL: ${forecastURL}"
	def requestParams =
		[
			uri: forecastURL,
            requestContentType: "application/json",
			contentType: "application/json",
            timeout: 30,
		]
    try {
        httpGet(requestParams) { response ->
            if(logEnable) log.info "In getRadarData - response: ${response.status}"
            
            if(response.status == 200) {
                def radar = response.data.properties.precipitationLast3Hours.value
                sendEvent(name: "radar", value: radar)
            } else {
                if(logEnable) log.debug "In getRadarData - Bad Request - ${response.status} - Something went wrong, please try again."
                runOnce(1,getRadarData)
            }
            currentDate = new Date()
            sendEvent(name: "responseStatus", value: response.status)
            sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
        }
    } catch (e) {
        log.error e
        theError = "${e}"
        def reason = theError.split(':')
        currentDate = new Date()
        sendEvent(name: "responseStatus", value: reason[1])
        sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)
    }
}
  
    
private cTOf(unit) {
    // Celsius to Fahrenheit
	unitI = unit.toFloat()
	theUnit = (unitI * (9/5) + 32).round(2)
	return theUnit
}

private mmTOin(unit){
    // MM to IN     
    unitI = unit.toFloat()           
    theUnit = (unitI / 25.4).round(2)
	return theUnit              
}

private mbTOinhg(unit){
    // MB to inHG     
    unitI = unit.toFloat()
    theUnit = ((unitI/100) * 0.02953).round(2)
	return theUnit              
}

private kphTOmph(unit){
    // KPH to MPH     
    unitI = unit.toFloat()           
    theUnit = (unitI * 0.621371).round(2)
	return theUnit              
}

private mpsTOmph(unit){
    // MPS to MPH     
    unitI = unit.toFloat()           
    theUnit = (unitI * 2.23694).round(2)
	return theUnit              
}

private mTOft(unit){
    // M to FT to Miles     
    unitI = unit.toFloat()           
    theUnit = ((unitI * 3.28084) * 0.000189394).round(2)
	return theUnit              
}

private mTOk(unit){
    // M to K    
    unitI = unit.toFloat()           
    theUnit = (unitI * 0.001).round(2)
	return theUnit              
}

private direction(unit) {
    // Cardinal Direction
	cardDirection = unit.toFloat()
	if((cardDirection >= 0) && (cardDirection <= 11.25)) direction = "N"
	if((cardDirection >= 11.26) && (cardDirection <= 33.75)) direction = "NNE"
	if((cardDirection >= 33.76) && (cardDirection <= 56.25)) direction = "NE"
	if((cardDirection >= 56.26) && (cardDirection <= 78.75)) direction = "ENE"
	if((cardDirection >= 78.76) && (cardDirection <= 101.25)) direction = "E"
	if((cardDirection >= 101.26) && (cardDirection <= 123.75)) direction = "ESE"    
    if((cardDirection >= 123.76) && (cardDirection <= 145.25)) direction = "SE"   
	if((cardDirection >= 146.26) && (cardDirection <= 168.75)) direction = "SSE"
	if((cardDirection >= 168.76) && (cardDirection <= 191.25)) direction = "S"
	if((cardDirection >= 191.26) && (cardDirection <= 213.75)) direction = "SSW"
	if((cardDirection >= 213.76) && (cardDirection <= 236.25)) direction = "SW"
	if((cardDirection >= 236.26) && (cardDirection <= 258.75)) direction = "WSW"
	if((cardDirection >= 258.76) && (cardDirection <= 281.25)) direction = "W"
	if((cardDirection >= 281.26) && (cardDirection <= 303.75)) direction = "WNW"
	if((cardDirection >= 303.76) && (cardDirection <= 326.25)) direction = "NW"
	if((cardDirection >= 326.26) && (cardDirection <= 348.75)) direction = "NNW"
	if((cardDirection >= 348.76) && (cardDirection <= 360.00)) direction = "N"
    if(direction == null) direction = cardDirection
    theUnit = direction
    return theUnit
}
