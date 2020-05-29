/**
 *  ****************  Weather Dot Gov Tile Driver  ****************
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
 *  1.0.5 - 05/29/20 - Renamed attributes to be 'Smartly' friendly
 *  1.0.4 - 05/07/20 - Added multiple alert data
 *  1.0.3 - 04/19/20 - Added Asthma and Pollen Forecasts
 *  1.0.2 - 04/17/20 - Added alerts
 *  1.0.1 - 04/12/20 - Added 5th tile to forecast data
 *  1.0.0 - 04/07/20 - Initial release
 */

metadata {
	definition (name: "Weather Dot Gov Tile Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Weather%20Dot%20Gov/WDG-tile-driver.groovy") {
   		capability "Actuator"

        command "currentData"
        command "weeklyData"
        command "forecastData1"
        command "forecastData2"
        command "forecastData3"
        command "forecastData4"
        command "forecastData5"
        command "alertSummaryData"
        command "alertData1"
        command "alertData2"
        command "alertData3"
        command "alertData4"
        command "alertData5"
        
        command "asthmaYesterdayData"
        command "asthmaTodayData"
        command "asthmaTomorrowData"        
        command "pollenYesterdayData"
        command "pollenTodayData"
        command "pollenTomorrowData"
		
    	attribute "bpt-currentDataTile", "string"
        
        attribute "bpt-weeklyDataTile01", "string"
        attribute "bpt-weeklyDataTile02", "string"
        attribute "bpt-weeklyDataTile03", "string"
        attribute "bpt-weeklyDataTile04", "string"
        attribute "bpt-weeklyDataTile05", "string"
        attribute "bpt-weeklyDataTile06", "string"
        attribute "bpt-weeklyDataTile07", "string"
        attribute "bpt-weeklyDataTile08", "string"
        attribute "bpt-weeklyDataTile09", "string"
        attribute "bpt-weeklyDataTile10", "string"
        attribute "bpt-weeklyDataTile11", "string"
        attribute "bpt-weeklyDataTile12", "string"
        attribute "bpt-weeklyDataTile13", "string"
        attribute "bpt-weeklyDataTile14", "string"
        
        attribute "bpt-forecastTable1", "string"
        attribute "bpt-forecastTable2", "string"
        attribute "bpt-forecastTable3", "string"
        attribute "bpt-forecastTable4", "string"
        attribute "bpt-forecastTable5", "string"
        
        attribute "bpt-alertSummaryTile", "string"
        attribute "bpt-alertTile1", "string"
        attribute "bpt-alertTile2", "string"
        attribute "bpt-alertTile3", "string"
        attribute "bpt-alertTile4", "string"
        attribute "bpt-alertTile5", "string" 
        
        attribute "bpt-asthmaYesterdayTile", "string"
		attribute "bpt-asthmaTodayTile", "string"
		attribute "bpt-asthmaTomorrowTile", "string"        
        attribute "bpt-pollenYesterdayTile", "string"
		attribute "bpt-pollenTodayTile", "string"
		attribute "bpt-pollenTomorrowTile", "string"
	}
	preferences() {    	
        section(){
			input name: "about", type: "paragraph", title: "<b>Weather Data from Weather.gov</b><br>This driver holds the tile data for use with dashboards", description: ""
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
        }
    }
}

def currentData(stuff) {
    if(logEnable) log.debug "In currentData"    
    sendEvent(name: "currentDataTile", value: stuff)
}

def weeklyData(stuff) {
    if(logEnable) log.debug "In weeklyData"
    def (theNumb,theTable) = stuff.split('::')
    //log.info "${stuff}"
    if(theNumb == "1") sendEvent(name: "weeklyDataTile01", value: theTable)
    if(theNumb == "2") sendEvent(name: "weeklyDataTile02", value: theTable)
    if(theNumb == "3") sendEvent(name: "weeklyDataTile03", value: theTable)
    if(theNumb == "4") sendEvent(name: "weeklyDataTile04", value: theTable)
    if(theNumb == "5") sendEvent(name: "weeklyDataTile05", value: theTable)
    if(theNumb == "6") sendEvent(name: "weeklyDataTile06", value: theTable)
    if(theNumb == "7") sendEvent(name: "weeklyDataTile07", value: theTable)
    if(theNumb == "8") sendEvent(name: "weeklyDataTile08", value: theTable)
    if(theNumb == "9") sendEvent(name: "weeklyDataTile09", value: theTable)
    if(theNumb == "10") sendEvent(name: "weeklyDataTile10", value: theTable)
    if(theNumb == "11") sendEvent(name: "weeklyDataTile11", value: theTable)
    if(theNumb == "12") sendEvent(name: "weeklyDataTile12", value: theTable)
    if(theNumb == "13") sendEvent(name: "weeklyDataTile13", value: theTable)
    if(theNumb == "14") sendEvent(name: "weeklyDataTile14", value: theTable)
}

def forecastData1(stuff) {
    if(logEnable) log.debug "In forecastData1"
    sendEvent(name: "forecastTable1", value: stuff)   
}

def forecastData2(stuff) {
    if(logEnable) log.debug "In forecastData2"
    sendEvent(name: "forecastTable2", value: stuff)   
}

def forecastData3(stuff) {
    if(logEnable) log.debug "In forecastData3"
    sendEvent(name: "forecastTable3", value: stuff)   
}

def forecastData4(stuff) {
    if(logEnable) log.debug "In forecastData4"
    sendEvent(name: "forecastTable4", value: stuff)   
}

def forecastData5(stuff) {
    if(logEnable) log.debug "In forecastData5"
    sendEvent(name: "forecastTable5", value: stuff)   
}

def alertSummaryData(stuff) {
    if(logEnable) log.debug "In alertSummaryData"
    sendEvent(name: "alertSummaryTile", value: stuff)   
}

def alertData1(stuff) {
    if(logEnable) log.debug "In alertData1"
    sendEvent(name: "alertTile1", value: stuff)   
}

def alertData2(stuff) {
    if(logEnable) log.debug "In alertData2"
    sendEvent(name: "alertTile2", value: stuff)   
}

def alertData3(stuff) {
    if(logEnable) log.debug "In alertData3"
    sendEvent(name: "alertTile3", value: stuff)   
}

def alertData4(stuff) {
    if(logEnable) log.debug "In alertData4"
    sendEvent(name: "alertTile4", value: stuff)   
}

def alertData5(stuff) {
    if(logEnable) log.debug "In alertData5"
    sendEvent(name: "alertTile5", value: stuff)   
}

def asthmaYesterdayData(stuff) {
    if(logEnable) log.debug "In asthmaYesterdayData"
    sendEvent(name: "asthmaYesterdayTile", value: stuff)   
}

def asthmaTodayData(stuff) {
    if(logEnable) log.debug "In asthmaTodayData"
    sendEvent(name: "asthmaTodayTile", value: stuff)   
}

def asthmaTomorrowData(stuff) {
    if(logEnable) log.debug "In asthmaTomorrowData"
    sendEvent(name: "asthmaTomorrowTile", value: stuff)   
}

def pollenYesterdayData(stuff) {
    if(logEnable) log.debug "In pollenYesterdayData"
    sendEvent(name: "pollenYesterdayTile", value: stuff)   
}

def pollenTodayData(stuff) {
    if(logEnable) log.debug "In pollenTodayData"
    sendEvent(name: "pollenTodayTile", value: stuff)   
}

def pollenTomorrowData(stuff) {
    if(logEnable) log.debug "In pollenTomorrowData"
    sendEvent(name: "pollenTomorrowTile", value: stuff)   
}
