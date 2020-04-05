/**
 *  ****************  Weather Gov Data Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Weather Gov data to be used with Hubitat.
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
 *  V1.0.0 - 04/03/20 - Initial release
 */

def setVersion(){
    appName = "WeatherGovDriver"
	version = "v1.0.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Weather Gov Data Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"

        command "getPointsData"
        command "getWeatherWeeklyData"
        command "getWeatherData"
		
    	attribute "office", "string"
		attribute "gridX", "string"
        attribute "gridY", "string"
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
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(){
			input name: "about", type: "paragraph", title: "<b>Weather Data from Weather.gov</b><br>The Latitude & Longitude are automaticaly set to your hubs location. Only change if you would like a different location.", description: ""
            input name:"lat", type:"text", title: "Latitude", require: true, defaultValue: "${location.latitude}"
			input name:"lng", type:"text", title: "Longitude", require: true, defaultValue: "${location.longitude}"
            input name:"station", type:"text", title: "Station ID"
            input name:"unitFormat", type:"enum", title: "Unit Format", required: true, options: ["Imperial", "Metric"]
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
        }
    }
}

def getPointsData() {
    if(logEnable) log.debug "In getPointsData"
	currentDate = new Date()
    sendEvent(name: "responseStatus", value: "Getting Points Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)

	pointsURL = "https://api.weather.gov/points/${lat},${lng}"
	if(logEnable) log.debug "pointsURL: ${pointsURL}"
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
                def office = response.data.properties.cwa
                log.info "Office: ${office}"
                sendEvent(name: "office", value: office)

                def gridX = response.data.properties.gridX
                sendEvent(name: "gridX", value: gridX)

                def gridY = response.data.properties.gridY
                sendEvent(name: "gridY", value: gridY)
            } else {
                if(logEnable) log.debug "In getPointsData - Bad Request - ${response.status} - Something went wrong, please try again."
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

def getWeatherWeeklyData() {
    if(logEnable) log.debug "In getWeatherWeeklyData"
	currentDate = new Date()
    sendEvent(name: "responseStatus", value: "Getting Weather Data...")
    sendEvent(name: "lastUpdated", value: currentDate, isStateChange: true)

    office = device.currentValue('office')
    gridX = device.currentValue('gridX')
    gridY = device.currentValue('gridY')
    
	forecastURL = "https://api.weather.gov/gridpoints/${office}/${gridX},${gridY}/forecast"
	if(logEnable) log.debug "forecastURL: ${forecastURL}"
	def requestParams =
		[
			uri: forecastURL,
            requestContentType: "application/json",
			contentType: "application/json",
            timeout: 30,
		]
    try {
        httpGet(requestParams) { response ->
            if(logEnable) log.info "In getWeatherWeeklyData - response: ${response.status}"
            
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
            if(logEnable) log.debug "In getWeatherWeeklyData - Bad Request - ${response.status} - Something went wrong, please try again."
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
    
    forecastURL = "https://api.weather.gov/stations/${station}/observations/latest"
    
	if(logEnable) log.debug "forecastURL: ${forecastURL}"
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
                        kphTOmph(xwindSpeed)
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
                        inTOmm(xprecipitationLastHour)
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
                    if(unitFormat == "Imperial") {
                        inTOmm(xprecipitationLast3Hours)
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
                    if(unitFormat == "Imperial") {
                        inTOmm(xprecipitationLast6Hours)
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
                    if(unitFormat == "Imperial") {
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
                    if(unitFormat == "Imperial") {
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

