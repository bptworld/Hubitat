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
 *  V1.0.0 - 04/13/19 - Initial release
 */

metadata {
	definition (name: "Marine Weather", namespace: "BPTWorld", author: "Bryan Turcotte") {
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
		
	}
	preferences() {    	
        section(){
			input name: "about", type: "paragraph", title: "Marine Weather", description: "Note: All values are in Metric."
			input name:"aKey", type:"text", title: "Authorization Key", required: true
		//	input name:"unitFormat", type:"enum", title: "Unit Format", required: true, options: ["Imperial", "Metric"]
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

	//	notUsable = airPressure,visiblity

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
				state.airTemperature = airTemperature.toString().replaceAll("[\\[\\]]", "")
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
			} else {
				state.currentDirection = currentDirection.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - cloudCover: ${state.cloudCover}"
       		sendEvent(name: "currentDirection", value: state.currentDirection, isStateChange: true)
			
			currentSpeed = response.data.hours.currentSpeed.value[0]
			if(!currentSpeed) {
				state.currentSpeed = "No Data"
			} else {
				state.currentSpeed = currentSpeed.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - currentSpeed: ${state.currentSpeed}"
       		sendEvent(name: "currentSpeed", value: state.currentSpeed, isStateChange: true)
			
			gust = response.data.hours.gust.value[0]
			if(logEnable) log.debug "In getWeatherData - raw gust: ${gust}"
			if(!gust) {
				state.gust = "No Data"
			} else {
				state.gust = gust.toString().replaceAll("[\\[\\]]", "")
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
				state.precipitation = precipitation.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - precipitation: ${state.precipitation}"
           	sendEvent(name: "precipitation", value: state.precipitation, isStateChange: true)
			
			seaLevel = response.data.hours.seaLevel.value[0]
			if(!seaLevel) {
				state.seaLevel = "No Data"
			} else {
				state.seaLevel = seaLevel.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - seaLevel: ${state.seaLevel}"
           	sendEvent(name: "seaLevel", value: state.seaLevel, isStateChange: true)
			
			snowDepth = response.data.hours.snowDepth.value[0]
			if(!snowDepth) {
				state.snowDepth = "No Data"
			} else {
				state.snowDepth = snowDepth.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - snowDepth: ${state.snowDepth}"
			sendEvent(name: "snowDepth", value: state.snowDepth, isStateChange: true)
			
			swellDirection = response.data.hours.swellDirection.value[0]
			if(!swellDirection) {
				state.swellDirection = "No Data"
			} else {
				state.swellDirection = swellDirection.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - swellDirection: ${state.swellDirection}"
			sendEvent(name: "swellDirection", value: state.swellDirection, isStateChange: true)
			
			swellHeight = response.data.hours.swellHeight.value[0]
			if(!swellHeight) {
				state.swellHeight = "No Data"
			} else {
				state.swellHeight = swellHeight.toString().replaceAll("[\\[\\]]", "")
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
				state.visiblity = visiblity.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - visiblity: ${state.visiblity}"
			sendEvent(name: "visiblity", value: state.visiblity, isStateChange: true)
			
			waterTemperature = response.data.hours.waterTemperature.value[0]
			if(!waterTemperature) {
				state.waterTemperature = "No Data"
			} else {
				state.waterTemperature = waterTemperature.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - waterTemperature: ${state.waterTemperature}"
			sendEvent(name: "waterTemperature", value: state.waterTemperature, isStateChange: true)
			
			waveDirection = response.data.hours.waveDirection.value[0]
			if(!waveDirection) {
				state.waveDirection = "No Data"
			} else {
				state.waveDirection = waveDirection.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - waveDirection: ${state.waveDirection}"
			sendEvent(name: "waveDirection", value: state.waveDirection, isStateChange: true)
			
			waveHeight = response.data.hours.waveHeight.value[0]
			if(!waveHeight) {
				state.waveHeight = "No Data"
			} else {
				state.waveHeight = waveHeight.toString().replaceAll("[\\[\\]]", "")
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
			} else {
				state.windWaveDirection = windWaveDirection.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - windWaveDirection: ${state.windWaveDirection}"
			sendEvent(name: "windWaveDirection", value: state.windWaveDirection, isStateChange: true)
			
			windWaveHeight = response.data.hours.windWaveHeight.value[0]
			if(!windWaveHeight) {
				state.windWaveHeight = "No Data"
			} else {
				state.windWaveHeight = windWaveHeight.toString().replaceAll("[\\[\\]]", "")
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
			} else {
				state.windDirection = windDirection.toString().replaceAll("[\\[\\]]", "")
			}
			if(logEnable) log.debug "In getWeatherData - windDirection: ${state.windDirection}"
			sendEvent(name: "windDirection", value: state.windDirection, isStateChange: true)
			
			windSpeed = response.data.hours.windSpeed.value[0]
			if(!windSpeed) {
				state.windSpeed = "No Data"
			} else {
				state.windSpeed = windSpeed.toString().replaceAll("[\\[\\]]", "")
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
