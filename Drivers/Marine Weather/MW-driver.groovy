/**
 *  ****************  Marine Weather - Storm Glass Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Marine Weather - Storm Glass data to be used with Hubitat.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V1.0.1 - 04/16/19 - Added metric to imperial conversions, cardinal directions and importUrl
 *  V1.0.0 - 04/13/19 - Initial release
 */

metadata {
	definition (name: "Marine Weather", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Drivers/Marine%20Weather/MW-driver.groovy") {
   		capability "Actuator"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"

        command "pollNow"
		
    	attribute "time", "string"
		attribute "dailyRequests", "string"
		attribute "dailyQuota", "string"
		attribute "airTemperature", "string"
        attribute "airPressure", "string"
        attribute "cloudCover", "string"
        attribute "currentDirection", "string"
        attribute "currentSpeed", "string"
        attribute "gust", "string"
        attribute "humidity", "string"
        attribute "iceCover", "string"
        attribute "precipitation", "string"
        attribute "seaLevel", "string"
		attribute "snowDepth", "string"
		attribute "swellDirection", "string"
		attribute "swellHeight", "string"
		attribute "swellPeriod", "string"
		attribute "visiblity", "string"
		attribute "waterTemperature", "string"
		attribute "waveDirection", "string"
		attribute "waveHeight", "string"
		attribute "wavePeriod", "string"
		attribute "windWaveDirection", "string"
		attribute "windWaveHeight", "string"
		attribute "windWavePeriod", "string"
		attribute "windDirection", "string"
		attribute "windSpeed", "string"
		
		attribute "currentCardDirection", "string"
		attribute "swellCardDirection", "string"
		attribute "waveCardDirection", "string"
		attribute "windWaveCardDirection", "string"
		attribute "windCardDirection", "string"
	}
	preferences() {    	
        section(){
			input name: "about", type: "paragraph", title: "Marine Weather", description: ""
			input name:"aKey", type:"text", title: "Authorization Key", required: true
			input name:"unitFormat", type:"enum", title: "Unit Format", required: true, options: ["Imperial", "Metric"]
			input name:"lat", type:"text", title: "Latitude", require: true, defaultValue: "${location.latitude}"
			input name:"lng", type:"text", title: "Longitude", require: true, defaultValue: "${location.longitude}"
			input name:"source", type:"enum", title: "Source", required: true, options: ["noaa", "sg"]
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def pollNow(){
	getWeatherData()
}

def getWeatherData() {
	getUnixDate()
	theStart = "${state.unixDateNow}"
	theEnd = "${state.unixDateNow}"
	theSource = "${source}"
	params = "airTemperature,cloudCover,currentDirection,currentSpeed,gust,humidity,iceCover,precipitation,seaLevel,snowDepth,swellDirection,swellPeriod,waterTemperature,waveDirection,waveHeight,wavePeriod,windWaveDirection,windWaveHeight,windWavePeriod,windDirection,windSpeed"

	//	not usable = airPressure,visiblity

	sgURI = "https://api.stormglass.io/point?lat=${lat}&lng=${lng}&params=${params}&source=${theSource}&start=${theStart}&end=${theEnd}"
	if(logEnable) log.debug "URI: ${sgURI}"
	def requestParams =
		[
			uri:  sgURI,
			requestContentType: "application/json",
			contentType: "application/json",
			headers: [
				Authorization: "${aKey}"
       		]
		]
	httpGet(requestParams) { response ->
		if(logEnable) log.debug "In getWeatherData - response: ${response.status}"
		if(response.status == 200) {	
			sendEvent(name: "dailyRequests", value: response.data.meta.requestCount, isStateChange: true)
			sendEvent(name: "dailyQuota", value: response.data.meta.dailyQuota, isStateChange: true)
			sendEvent(name: "time", value: response.data.hours.time[0], isStateChange: true)
			
			airPressure = response.data.hours.airPressure.value[0]
			if(!airPressure) {
				state.airPressure = "No Data"
			} else {
				state.airPressure = airPressure.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - airPressure: ${state.airPressure}"
			sendEvent(name: "airPressure", value: state.airPressure, isStateChange: true)
			
			airTemperature = response.data.hours.airTemperature.value[0]
			if(!airTemperature) {
				state.airTemperature = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aTemp = airTemperature.toString().replaceAll("[\\[\\]]", "")
					// Celsius to Fahrenheit
					aTempI = aTemp.toFloat()
					fahrenheit = (aTempI * (9/5) + 32).round(2)
					state.airTemperature = fahrenheit
				} else {
					state.airTemperature = airTemperature.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - airTemperature: ${state.airTemperature}"
			sendEvent(name: "airTemperature", value: state.airTemperature, isStateChange: true)
			
			cloudCover = response.data.hours.cloudCover.value[0]
			if(!cloudCover) {
				state.cloudCover = "No Data"
			} else {
				state.cloudCover = cloudCover.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - cloudCover: ${state.cloudCover}"
			sendEvent(name: "cloudCover", value: state.cloudCover, isStateChange: true)
			
			currentDirection = response.data.hours.currentDirection.value[0]
			if(!currentDirection) {
				state.currentDirection = "No Data"
				state.currentCardDirection = "No Data"
			} else {
				state.currentDirection = currentDirection.toString().replaceAll("[\\[\\]]", "")
				// Cardinal Direction
				cardDirection = state.currentDirection.toFloat()
				if((cardDirection >= 0) && (cardDirection <= 11.25)) state.currentCardDirection = "N"
				if((cardDirection >= 11.26) && (cardDirection <= 33.75)) state.currentCardDirection = "NNE"
				if((cardDirection >= 33.76) && (cardDirection <= 56.25)) state.currentCardDirection = "NE"
				if((cardDirection >= 56.26) && (cardDirection <= 78.75)) state.currentCardDirection = "ENE"
				if((cardDirection >= 78.76) && (cardDirection <= 101.25)) state.currentCardDirection = "E"
				if((cardDirection >= 101.26) && (cardDirection <= 123.75)) state.currentCardDirection = "ESE"
				if((cardDirection >= 123.76) && (cardDirection <= 146.25)) state.currentCardDirection = "SE"
				if((cardDirection >= 146.26) && (cardDirection <= 168.75)) state.currentCardDirection = "SSE"
				if((cardDirection >= 168.76) && (cardDirection <= 191.25)) state.currentCardDirection = "S"
				if((cardDirection >= 191.26) && (cardDirection <= 213.75)) state.currentCardDirection = "SSW"
				if((cardDirection >= 213.76) && (cardDirection <= 236.25)) state.currentCardDirection = "SW"
				if((cardDirection >= 236.26) && (cardDirection <= 258.75)) state.currentCardDirection = "WSW"
				if((cardDirection >= 258.76) && (cardDirection <= 281.25)) state.currentCardDirection = "W"
				if((cardDirection >= 281.26) && (cardDirection <= 303.75)) state.currentCardDirection = "WNW"
				if((cardDirection >= 303.76) && (cardDirection <= 326.25)) state.currentCardDirection = "NW"
				if((cardDirection >= 326.26) && (cardDirection <= 348.75)) state.currentCardDirection = "NNW"
				if((cardDirection >= 348.76) && (cardDirection <= 360.00)) state.currentCardDirection = "N"
			}
			if(logEnable) log.debug "In getWeatherData - currentDirection: ${state.currentDirection} - Cardinal Dir: ${state.currentCardDirection}"
       		sendEvent(name: "currentDirection", value: state.currentDirection, isStateChange: true)
			sendEvent(name: "currentCardDirection", value: state.currentCardDirection, isStateChange: true)
			
			currentSpeed = response.data.hours.currentSpeed.value[0]
			if(!currentSpeed) {
				state.currentSpeed = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aSpeed = currentSpeed.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aSpeedI = aSpeed.toFloat()
					speed = (aSpeedI * 3.28084).round(2)
					state.currentSpeed = speed
				} else {
					state.currentSpeed = currentSpeed.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - currentSpeed: ${state.currentSpeed}"
       		sendEvent(name: "currentSpeed", value: state.currentSpeed, isStateChange: true)
			
			gust = response.data.hours.gust.value[0]
			if(logEnable) log.debug "In getWeatherData - raw gust: ${gust}"
			if(!gust) {
				state.gust = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aGust = gust.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aGustI = aGust.toFloat()
					gst = (aGustI * 3.28084).round(2)
					state.gust = gst
				} else {
					state.gust = gust.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - gust: ${state.gust}"
       		sendEvent(name: "gust", value: state.gust, isStateChange: true)
			
			humidity = response.data.hours.humidity.value[0]
			if(!humidity) {
				state.humidity = "No Data"
			} else {
				state.humidity = humidity.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - humidity: ${state.humidity}"
           	sendEvent(name: "humidity", value: state.humidity, isStateChange: true)
			
			iceCover = response.data.hours.iceCover.value[0]
			if(!iceCover) {
				state.iceCover = "No Data"
			} else {
				state.iceCover = iceCover.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - iceCover: ${state.iceCover}"
           	sendEvent(name: "iceCover", value: state.iceCover, isStateChange: true)
			
			precipitation = response.data.hours.precipitation.value[0]
			if(!precipitation) {
				state.precipitation = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aPrecipitation = precipitation.toString().replaceAll("[\\[\\]]", "")
					// kg/m2 to lb/ft2
					aPrecipitationI = aPrecipitation.toFloat()
					precip = (aPrecipitationI * 23.7303604042319).round(2)
					state.precipitation = precip
				} else {
					state.precipitation = precipitation.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - precipitation: ${state.precipitation}"
           	sendEvent(name: "precipitation", value: state.precipitation, isStateChange: true)
			
			seaLevel = response.data.hours.seaLevel.value[0]
			if(!seaLevel) {
				state.seaLevel = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aSeaLevel = seaLevel.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aSeaLevelI = aSeaLevel.toFloat()
					seaL = (aSeaLevelI * 3.2808).round(2)
					state.seaLevel = seaL
				} else {
					state.seaLevel = seaLevel.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - seaLevel: ${state.seaLevel}"
           	sendEvent(name: "seaLevel", value: state.seaLevel, isStateChange: true)
			
			snowDepth = response.data.hours.snowDepth.value[0]
			if(!snowDepth) {
				state.snowDepth = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aSnowDepth = snowDepth.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aSnowDepthI = aSnowDepth.toFloat()
					snowD = (aSnowDepthI * 3.2808).round(2)
					state.snowDepth = snowD
				} else {
					state.snowDepth = snowDepth.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - snowDepth: ${state.snowDepth}"
			sendEvent(name: "snowDepth", value: state.snowDepth, isStateChange: true)
			
			swellDirection = response.data.hours.swellDirection.value[0]
			if(!swellDirection) {
				state.swellDirection = "No Data"
				state.swellCardDirection = "No Data"
			} else {
				state.swellDirection = swellDirection.toString().replaceAll("[\\[\\]]", "")
				// Cardinal Direction
				cardDirection = state.swellDirection.toFloat()
				if((cardDirection >= 0) && (cardDirection <= 11.25)) state.swellCardDirection = "N"
				if((cardDirection >= 11.26) && (cardDirection <= 33.75)) state.swellCardDirection = "NNE"
				if((cardDirection >= 33.76) && (cardDirection <= 56.25)) state.swellCardDirection = "NE"
				if((cardDirection >= 56.26) && (cardDirection <= 78.75)) state.swellCardDirection = "ENE"
				if((cardDirection >= 78.76) && (cardDirection <= 101.25)) state.swellCardDirection = "E"
				if((cardDirection >= 101.26) && (cardDirection <= 123.75)) state.swellCardDirection = "ESE"
				if((cardDirection >= 123.76) && (cardDirection <= 146.25)) state.swellCardDirection = "SE"
				if((cardDirection >= 146.26) && (cardDirection <= 168.75)) state.swellCardDirection = "SSE"
				if((cardDirection >= 168.76) && (cardDirection <= 191.25)) state.swellCardDirection = "S"
				if((cardDirection >= 191.26) && (cardDirection <= 213.75)) state.swellCardDirection = "SSW"
				if((cardDirection >= 213.76) && (cardDirection <= 236.25)) state.swellCardDirection = "SW"
				if((cardDirection >= 236.26) && (cardDirection <= 258.75)) state.swellCardDirection = "WSW"
				if((cardDirection >= 258.76) && (cardDirection <= 281.25)) state.swellCardDirection = "W"
				if((cardDirection >= 281.26) && (cardDirection <= 303.75)) state.swellCardDirection = "WNW"
				if((cardDirection >= 303.76) && (cardDirection <= 326.25)) state.swellCardDirection = "NW"
				if((cardDirection >= 326.26) && (cardDirection <= 348.75)) state.swellCardDirection = "NNW"
				if((cardDirection >= 348.76) && (cardDirection <= 360.00)) state.swellCardDirection = "N"
			}
			if(logEnable) log.debug "In getWeatherData - swellDirection: ${state.swellDirection} - Cardinal Dir: ${state.swellCardDirection}"
			sendEvent(name: "swellDirection", value: state.swellDirection, isStateChange: true)
			sendEvent(name: "swellCardDirection", value: state.swellCardDirection, isStateChange: true)
			
			swellHeight = response.data.hours.swellHeight.value[0]
			if(!swellHeight) {
				state.swellHeight = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aswellHeight = swellHeight.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aswellHeightI = aswellHeight.toFloat()
					swellH = (aswellHeightI * 3.2808).round(2)
					state.swellHeight = swellH
				} else {
					state.swellHeight = swellHeight.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - swellHeight: ${state.swellHeight}"
			sendEvent(name: "swellHeight", value: state.swellHeight, isStateChange: true)
			
			swellPeriod = response.data.hours.swellPeriod.value[0]
			if(!swellPeriod) {
				state.swellPeriod = "No Data"
			} else {
				state.swellPeriod = swellPeriod.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - swellPeriod: ${state.swellPeriod}"
			sendEvent(name: "swellPeriod", value: state.swellPeriod, isStateChange: true)
			
			visiblity = response.data.hours.visiblity.value[0]
			if(!visiblity) {
				state.visiblity = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					avisiblity = visiblity.toString().replaceAll("[\\[\\]]", "")
					// km to miles
					avisiblityI = visiblity.toFloat()
					visab = (avisiblityI * 0.6213711922).round(2)
					state.visiblity = visab
				} else {
					state.visiblity = visiblity.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - visiblity: ${state.visiblity}"
			sendEvent(name: "visiblity", value: state.visiblity, isStateChange: true)
			
			waterTemperature = response.data.hours.waterTemperature.value[0]
			if(!waterTemperature) {
				state.waterTemperature = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					awTemp = waterTemperature.toString().replaceAll("[\\[\\]]", "")
					// Celsius to Fahrenheit
					awTempI = awTemp.toFloat()
					fahrenheit = (awTempI * (9/5) + 32).round(2)
					state.airTemperature = fahrenheit
				} else {
					state.waterTemperature = waterTemperature.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - waterTemperature: ${state.waterTemperature}"
			sendEvent(name: "waterTemperature", value: state.waterTemperature, isStateChange: true)
			
			waveDirection = response.data.hours.waveDirection.value[0]
			if(!waveDirection) {
				state.waveDirection = "No Data"
				state.waveCardDirection = "No Data"
			} else {
				state.waveDirection = waveDirection.toString().replaceAll("[\\[\\]]", "")
				// Cardinal Direction
				cardDirection = state.waveDirection.toFloat()
				if((cardDirection >= 0) && (cardDirection <= 11.25)) state.waveCardDirection = "N"
				if((cardDirection >= 11.26) && (cardDirection <= 33.75)) state.waveCardDirection = "NNE"
				if((cardDirection >= 33.76) && (cardDirection <= 56.25)) state.waveCardDirection = "NE"
				if((cardDirection >= 56.26) && (cardDirection <= 78.75)) state.waveCardDirection = "ENE"
				if((cardDirection >= 78.76) && (cardDirection <= 101.25)) state.waveCardDirection = "E"
				if((cardDirection >= 101.26) && (cardDirection <= 123.75)) state.waveCardDirection = "ESE"
				if((cardDirection >= 123.76) && (cardDirection <= 146.25)) state.waveCardDirection = "SE"
				if((cardDirection >= 146.26) && (cardDirection <= 168.75)) state.waveCardDirection = "SSE"
				if((cardDirection >= 168.76) && (cardDirection <= 191.25)) state.waveCardDirection = "S"
				if((cardDirection >= 191.26) && (cardDirection <= 213.75)) state.waveCardDirection = "SSW"
				if((cardDirection >= 213.76) && (cardDirection <= 236.25)) state.waveCardDirection = "SW"
				if((cardDirection >= 236.26) && (cardDirection <= 258.75)) state.waveCardDirection = "WSW"
				if((cardDirection >= 258.76) && (cardDirection <= 281.25)) state.waveCardDirection = "W"
				if((cardDirection >= 281.26) && (cardDirection <= 303.75)) state.waveCardDirection = "WNW"
				if((cardDirection >= 303.76) && (cardDirection <= 326.25)) state.waveCardDirection = "NW"
				if((cardDirection >= 326.26) && (cardDirection <= 348.75)) state.waveCardDirection = "NNW"
				if((cardDirection >= 348.76) && (cardDirection <= 360.00)) state.waveCardDirection = "N"
			}
			if(logEnable) log.debug "In getWeatherData - waveDirection: ${state.waveDirection} - Cardinal Dir: ${state.waveCardDirection}"
			sendEvent(name: "waveDirection", value: state.waveDirection, isStateChange: true)
			sendEvent(name: "waveCardDirection", value: state.waveCardDirection, isStateChange: true)
			
			waveHeight = response.data.hours.waveHeight.value[0]
			if(!waveHeight) {
				state.waveHeight = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aHeight = waveHeight.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aHeightI = aHeight.toFloat()
					waveH = (aHeightI * 3.2808).round(2)
					state.waveHeight = waveH
				} else {
					state.waveHeight = waveHeight.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - waveHeight: ${state.waveHeight}"
			sendEvent(name: "waveHeight", value: state.waveHeight, isStateChange: true)
			
			wavePeriod = response.data.hours.wavePeriod.value[0]
			if(!wavePeriod) {
				state.wavePeriod = "No Data"
			} else {
				state.wavePeriod = wavePeriod.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - wavePeriod: ${state.wavePeriod}"
			sendEvent(name: "wavePeriod", value: state.wavePeriod, isStateChange: true)
			
			windWaveDirection = response.data.hours.windWaveDirection.value[0]
			if(!windWaveDirection) {
				state.windWaveDirection = "No Data"
				state.windWaveCardDirection = "No Data"
			} else {
				state.windWaveDirection = windWaveDirection.toString().replaceAll("[\\[\\]]", "")
				// Cardinal Direction
				cardDirection = state.windWaveDirection.toFloat()
				if((cardDirection >= 0) && (cardDirection <= 11.25)) state.windWaveCardDirection = "N"
				if((cardDirection >= 11.26) && (cardDirection <= 33.75)) state.windWaveCardDirection = "NNE"
				if((cardDirection >= 33.76) && (cardDirection <= 56.25)) state.windWaveCardDirection = "NE"
				if((cardDirection >= 56.26) && (cardDirection <= 78.75)) state.windWaveCardDirection = "ENE"
				if((cardDirection >= 78.76) && (cardDirection <= 101.25)) state.windWaveCardDirection = "E"
				if((cardDirection >= 101.26) && (cardDirection <= 123.75)) state.windWaveCardDirection = "ESE"
				if((cardDirection >= 123.76) && (cardDirection <= 146.25)) state.windWaveCardDirection = "SE"
				if((cardDirection >= 146.26) && (cardDirection <= 168.75)) state.windWaveCardDirection = "SSE"
				if((cardDirection >= 168.76) && (cardDirection <= 191.25)) state.windWaveCardDirection = "S"
				if((cardDirection >= 191.26) && (cardDirection <= 213.75)) state.windWaveCardDirection = "SSW"
				if((cardDirection >= 213.76) && (cardDirection <= 236.25)) state.windWaveCardDirection = "SW"
				if((cardDirection >= 236.26) && (cardDirection <= 258.75)) state.windWaveCardDirection = "WSW"
				if((cardDirection >= 258.76) && (cardDirection <= 281.25)) state.windWaveCardDirection = "W"
				if((cardDirection >= 281.26) && (cardDirection <= 303.75)) state.windWaveCardDirection = "WNW"
				if((cardDirection >= 303.76) && (cardDirection <= 326.25)) state.windWaveCardDirection = "NW"
				if((cardDirection >= 326.26) && (cardDirection <= 348.75)) state.windWaveCardDirection = "NNW"
				if((cardDirection >= 348.76) && (cardDirection <= 360.00)) state.windWaveCardDirection = "N"
			}
			if(logEnable) log.debug "In getWeatherData - windWaveDirection: ${state.windWaveDirection} - Cardinal Dir: ${state.windwaveCardDirection}"
			sendEvent(name: "windWaveDirection", value: state.windWaveDirection, isStateChange: true)
			sendEvent(name: "windWaveCardDirection", value: state.windWaveCardDirection, isStateChange: true)
			
			windWaveHeight = response.data.hours.windWaveHeight.value[0]
			if(!windWaveHeight) {
				state.windWaveHeight = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					aHeight = windWaveHeight.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					aHeightI = aHeight.toFloat()
					windH = (aHeightI * 3.2808).round(2)
					state.windWaveHeight = windH
				} else {
					state.windWaveHeight = windWaveHeight.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - windWaveHeight: ${state.windWaveHeight}"
			sendEvent(name: "windWaveHeight", value: state.windWaveHeight, isStateChange: true)
			
			windWavePeriod = response.data.hours.windWavePeriod.value[0]
			if(!windWavePeriod) {
				state.windWavePeriod = "No Data"
			} else {
				state.windWavePeriod = windWavePeriod.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - windWavePeriod: ${state.windWavePeriod}"
			sendEvent(name: "windWavePeriod", value: state.windWavePeriod, isStateChange: true)
			
			windDirection = response.data.hours.windDirection.value[0]
			if(!windDirection) {
				state.windDirection = "No Data"
				state.windCardDirection = "No Data"
			} else {
				state.windDirection = windDirection.toString().replaceAll("[\\[\\]]", "")
				// Cardinal Direction
				cardDirection = state.windDirection.toFloat()
				if((cardDirection >= 0) && (cardDirection <= 11.25)) state.windCardDirection = "N"
				if((cardDirection >= 11.26) && (cardDirection <= 33.75)) state.windCardDirection = "NNE"
				if((cardDirection >= 33.76) && (cardDirection <= 56.25)) state.windCardDirection = "NE"
				if((cardDirection >= 56.26) && (cardDirection <= 78.75)) state.windCardDirection = "ENE"
				if((cardDirection >= 78.76) && (cardDirection <= 101.25)) state.windCardDirection = "E"
				if((cardDirection >= 101.26) && (cardDirection <= 123.75)) state.windCardDirection = "ESE"
				if((cardDirection >= 123.76) && (cardDirection <= 146.25)) state.windCardDirection = "SE"
				if((cardDirection >= 146.26) && (cardDirection <= 168.75)) state.windCardDirection = "SSE"
				if((cardDirection >= 168.76) && (cardDirection <= 191.25)) state.windCardDirection = "S"
				if((cardDirection >= 191.26) && (cardDirection <= 213.75)) state.windCardDirection = "SSW"
				if((cardDirection >= 213.76) && (cardDirection <= 236.25)) state.windCardDirection = "SW"
				if((cardDirection >= 236.26) && (cardDirection <= 258.75)) state.windCardDirection = "WSW"
				if((cardDirection >= 258.76) && (cardDirection <= 281.25)) state.windCardDirection = "W"
				if((cardDirection >= 281.26) && (cardDirection <= 303.75)) state.windCardDirection = "WNW"
				if((cardDirection >= 303.76) && (cardDirection <= 326.25)) state.windCardDirection = "NW"
				if((cardDirection >= 326.26) && (cardDirection <= 348.75)) state.windCardDirection = "NNW"
				if((cardDirection >= 348.76) && (cardDirection <= 360.00)) state.windCardDirection = "N"
			}
			if(logEnable) log.debug "In getWeatherData - windDirection: ${state.windDirection} - Cardinal Dir: ${state.windCardDirection}"
			sendEvent(name: "windDirection", value: state.windDirection, isStateChange: true)
			sendEvent(name: "windCardDirection", value: state.windCardDirection, isStateChange: true)
			
			windSpeed = response.data.hours.windSpeed.value[0]
			if(!windSpeed) {
				state.windSpeed = "No Data"
			} else {
				if(unitFormat == "Imperial") {
					awindSpeed = windSpeed.toString().replaceAll("[\\[\\]]", "")
					// Meters to Feet
					awindSpeedI = awindSpeed.toFloat()
					windS = (awindSpeedI * 3.2808).round(2)
					state.windSpeed = windS
				} else {
					state.windSpeed = windSpeed.toString().replaceAll("[\\[\\]]", "")
				}
			}
			if(logEnable) log.debug "In getWeatherData - windSpeed: ${state.windSpeed}"
			sendEvent(name: "windSpeed", value: state.windSpeed, isStateChange: true)
		
			if(logEnable) log.debug "In getWeatherData - Request no. ${response.data.meta.requestCount}, Daily quota: ${response.data.meta.dailyQuota}"
		}
		else if(response.status == 400) {	
			if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Your request is invalid."
		}	
		else if(response.status == 401) {	
			if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Unauthorized - Your API key is invalid."
		}
		else if(response.status == 429) {	
			if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Too Many Requests - You've reached your daily limit."
		}	
		else if(response.status == 500) {	
			if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Internal Server Error - We had a problem with our server. Try again later."
		}	
		else if(response.status == 503) {	
			if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Service Unavailable - We're temporarily offline for maintenance. Please try again later."
		}	
		else {
			if(logEnable) log.debug "In getWeatherData - Bad Request - ${response.status} - Something went wrong, please try again."
		}
	}
}

def getUnixDate() {
	Date currentDateNow = new Date()
	unixDateNowRaw = currentDateNow.getTime() / 1000
	state.unixDateNow = "${unixDateNowRaw}".split("\\.")[0]
	
	Date currentDate1 = new Date().plus(1)
	unixDate1Raw = currentDate1.getTime() / 1000
	state.unixDate1 = "${unixDate1Raw}".split("\\.")[0]
	
	if(logEnable) log.debug "unixDateNow: ${state.unixDateNow} - unixDate1: ${state.unixDate1}"
}
