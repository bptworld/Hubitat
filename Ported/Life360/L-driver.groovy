/**
 *  Copyright 2015 SmartThings
 *
 *  name: "Life360 User", namespace: "tmleafs", author: "tmleafs"
 *
 *	BTRIAL DISTANCE AND SLEEP PATCH 29-12-2017
 *	Updated Code to handle distance from, and sleep functionality
 *
 *	TMLEAFS REFRESH PATCH 06-12-2016 V1.1
 *	Updated Code to match Smartthings updates 12-05-2017 V1.2
 *	Added Null Return on refresh to fix WebCoRE error 12-05-2017 V1.2
 *	Added updateMember function that pulls all usefull information Life360 provides for webCoRE use V2.0
 *	Changed attribute types added Battery & Power Source capability 
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
 *  Life360-User
 *
 *  Author: jeff
 *  Date: 2013-08-15
 *
 * ---- End of Original Header ----
 *
 *  v1.1.9 - 01/04/20 - Modified how/when Tile gets updated
 *  v1.1.8 - 01/03/20 - Fix for App Watchdog 2
 *  v1.1.7 - 11/03/19 - Minor changes
 *  v1.1.6 - 09/20/19 - Small changes to tile code, rewrite of the History Log code
 *  v1.1.5 - 09/16/19 - Updated 'free' tile to show more data points
 *  v1.1.4 - 08/29/19 - App Watchdog Compatible
 *  v1.1.3 - 08/06/19 - Added new attribute, lastUpdated
 *  V1.1.2 - 07/28/19 - Squashed a bug
 *  V1.1.1 - 07/19/19 - Place is now clickable on the status tile.
 *  V1.1.0 - 07/17/19 - Added map link to attributes
 *  --
 *  V1.0.0 - 06/30/19 - Initial port of driver for Hubitat (bptworld)
 */
 
import java.text.SimpleDateFormat

def setVersion(){
    appName = "Life360User"
	version = "v1.1.9" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

preferences {
	input title:"<b>Life360 User</b>", description:"Note: Any changes will take effect only on the NEXT update or forced refresh.", type:"paragraph", element:"paragraph"
	input name: "units", type: "enum", title: "Distance Units", description: "Miles or Kilometers", required: false, options:["Kilometers","Miles"]
    input "avatarFontSize", "text", title: "Avatar Font Size", required: true, defaultValue: "15"
    input "avatarSize", "text", title: "Avatar Size by Percentage", required: true, defaultValue: "75"

    input "numOfLines", "number", title: "How many lines to display on History Tile (from 1 to 10 only)", required:true, defaultValue: 5
    input "historyFontSize", "text", title: "History Font Size", required: true, defaultValue: "15"
    input "historyHourType", "bool", title: "Time Selection for History Tile (Off for 24h, On for 12h)", required: false, defaultValue: false
    input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
} 
 
metadata {
	definition (name: "Life360 User", namespace: "BPTWorld", author: "Bryan Turcotte", importURL: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Ported/Life360/L-driver.groovy") {
        capability "Actuator"
	    capability "Presence Sensor"
	    capability "Sensor"
        capability "Refresh"
	    capability "Sleep Sensor"
        capability "Battery"
        capability "Power Source"

	    attribute "distanceMetric", "Number"
   	    attribute "distanceKm", "Number"
	    attribute "distanceMiles", "Number"
        attribute "address1Prev", "String"
	    attribute "address1", "String"
  	    attribute "address2", "String"
  	    attribute "battery", "number"
   	    attribute "charge", "boolean" //boolean
   	    attribute "lastCheckin", "number"
       	attribute "inTransit", "String" //boolean
   	    attribute "isDriving", "String" //boolean
   	    attribute "latitude", "number"
   	    attribute "longitude", "number"
   	    attribute "since", "number"
   	    attribute "speedMetric", "number"
        attribute "speedMiles", "number"
        attribute "speedKm", "number"
   	    attribute "wifiState", "boolean" //boolean
        attribute "savedPlaces", "map"
        attribute "avatar", "string"
        attribute "avatarHtml", "string"
        attribute "life360Tile1", "string"
        attribute "history", "string"
        attribute "status", "string"
        attribute "lastMap", "string"
        attribute "lastUpdated", "string"
        attribute "numOfCharacters", "number"
        attribute "lastLogMessage", "string"
        
	    command "refresh"
	    command "asleep"
        command "awake"
        command "toggleSleeping"
        command "setBattery",["number","boolean"]
        command "sendHistory", ["string"]
        command "sendTheMap", ["string"]
        command "historyClearData"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
}

def sendTheMap(theMap) {
    lastMap = "${theMap}" 
    sendEvent(name: "lastMap", value: lastMap, displayed: true)
}
    
def sendLife360Tile1() {
    if(logEnable) log.debug "in Life360 User - Making the Avatar Tile"
    def avat = device.currentValue('avatar')
    def add1 = device.currentValue('address1')
    def bLevel = device.currentValue('battery')
    def bCharge = device.currentValue('powerSource')
    def bSpeedKm = device.currentValue('speedKm')
    def bSpeedMiles = device.currentValue('speedMiles')

    if(add1 == "No Data") add1 = "Between Places"
    
    def binTransit = device.currentValue('inTransit')
    if(binTransit == "true") {
        binTransita = "Moving"
    } else {
        binTransita = "Not Moving"
    }

    def bWifi = device.currentValue('wifiState')
    if(bWifi == "true") {
        bWifiS = "Wifi"
    } else {
        bWifiS = "No Wifi"
    }

    int sEpoch = device.currentValue('since')
    if(sEpoch == null) {
        theDate = use( groovy.time.TimeCategory ) {
            new Date( 0 )
        }
    } else {
        theDate = use( groovy.time.TimeCategory ) {
            new Date( 0 ) + sEpoch.seconds
        }
    }
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E hh:mm a")
    String dateSince = DATE_FORMAT.format(theDate)

    theMap = "https://www.google.com/maps/search/?api=1&query=${device.currentValue('latitude')},${device.currentValue('longitude')}"
    
	tileMap = "<table width='100%' valign='top'>"
    tileMap += "<tr><td width='25%'><img src='${avat}' height='${avatarSize}%'></td>"
    tileMap += "<td width='75%'><p style='font-size:${avatarFontSize}px'>At: <a href='${theMap}' target='_blank'>${add1}</a><br>"
    tileMap += "Since: ${dateSince}<br>${device.currentValue('status')}<br>"
    if(units == "Kilometers") tileMap += "${binTransita} - ${bSpeedKm} KMH<br>"
    if(units == "Miles") tileMap += "${binTransita} - ${bSpeedMiles} MPH<br>"
    tileMap += "Phone Lvl: ${bLevel} - ${bCharge} - ${bWifiS}</p></td>"
    tileMap += "</tr></table>"
    
	tileDevice1Count = tileMap.length()
	if(tileDevice1Count <= 1000) {
		if(logEnable) log.debug "tileMap - has ${tileDevice1Count} Characters<br>${tileMap}"
	} else {
		log.warn "Life360 - Too many characters to display on Dashboard (${tileDevice1Count})"
	}
	sendEvent(name: "life360Tile1", value: tileMap, displayed: true)
}

def sendHistory(msgValue) {
    if(logEnable) log.trace "In sendHistory - nameValue: ${msgValue}"

    if(msgValue.contains('Data')) {
        if(logEnable) log.trace "In sendHistory - Nothing to report (No Data)"
    } else {   
        try {
            getDateTime()
	        nMessage = newDate + " - " + msgValue
        
            if(state.list == null) state.list = []
            state.list.add(0,nMessage)  

            listSize = state.list.size()
            if(listSize > 10) state.list.removeAt(10)

            String result = state.list.join(";")
            logCharCount = result.length()
	        if(logCharCount <= 1000) {
	            if(logEnable) log.debug "Life360 User Driver - ${logCharCount} Characters"
	        } else {
	            logTop10 = "Too many characters to display on Dashboard - ${logCharCount}"
	        }

            def lines = result.split(";")
            if(logEnable) log.trace "In sendHistory - numOfLines: ${numOfLines}"
            logTop10 = "<table><tr><td align='Left'><div style='font-size:${historyFontSize}px'>"
            if(numOfLines >= 1) logTop10 += "${lines[0]}<br>"
            if(numOfLines >= 2) logTop10 += "${lines[1]}<br>"
            if(numOfLines >= 3) logTop10 += "${lines[2]}<br>"
            if(numOfLines >= 4) logTop10 += "${lines[3]}<br>"
            if(numOfLines >= 5) logTop10 += "${lines[4]}<br>"
            if(numOfLines >= 6) logTop10 += "${lines[5]}<br>"
            if(numOfLines >= 7) logTop10 += "${lines[6]}<br>"
            if(numOfLines >= 8) logTop10 += "${lines[7]}<br>"
            if(numOfLines >= 9) logTop10 += "${lines[8]}<br>"
            if(numOfLines >= 10) logTop10 += "${lines[9]}"
            logTop10 += "</div></td></tr></table>"
    
	        sendEvent(name: "history", value: logTop10, displayed: true)
            sendEvent(name: "numOfCharacters", value: logCharCount, displayed: true)
            sendEvent(name: "lastLogMessage", value: msgValue, displayed: true)
        }
        catch(e1) {
            log.warn "Life360 User - sendHistory - Something went wrong<br>${e1}"        
        }
    }
    sendLife360Tile1()
}

def installed(){
    log.info "Life360 User Installed"
    historyClearData()
}

def updated() {
    log.info "Life360 User has been Updated"
}

def getDateTime() {
	def date = new Date()
	if(historyHourType == false) newDate=date.format("E HH:mm")
	if(historyHourType == true) newDate=date.format("E hh:mm a")
    return newDate
}

def historyClearData(){
	if(logEnable) log.debug "Life360 User Driver - Clearing the data"
    msgValue = "-"
    logCharCount = "0"
    state.list1 = []	
	historyLog = "Waiting for Data..."
    sendEvent(name: "history", value: historyLog, displayed: true)
    sendEvent(name: "numOfCharacters1", value: logCharCount1, displayed: true)
    sendEvent(name: "lastLogMessage1", value: msgValue, displayed: true)
}	

def generatePresenceEvent(boolean present, homeDistance) {
	if(logEnable) log.debug "Life360 generatePresenceEvent (present = $present, homeDistance = $homeDistance)"
	def presence = formatValue(present)
	def linkText = getLinkText(device)
	def descriptionText = formatDescriptionText(linkText, present)
	def handlerName = getState(present)
	def sleeping = (presence == 'not present') ? 'not sleeping' : device.currentValue('sleeping')
	
	if (sleeping != device.currentValue('sleeping')) {
	    sendEvent( name: "sleeping", value: sleeping, descriptionText: sleeping == 'sleeping' ? 'Sleeping' : 'Awake' )
    }
	
    def display = presence + (presence == 'present' ? ', ' + sleeping : '')
    if (display != device.currentValue('display')) {
	    sendEvent( name: "display", value: display,  )
    }
	
	def results = [
		name: "presence",
		value: presence,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
	]
	if(logEnable) log.debug "Generating Event: ${results}"
	sendEvent (results)
	
    if(units == "Kilometers" || units == null || units == ""){
	    def statusDistance = homeDistance / 1000
	    def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " km from: Home"
        if(status != device.currentValue('status')){
            sendEvent( name: "status", value: status, isStateChange:true)
            state.update = true
        }
    } else {
	    def statusDistance = (homeDistance / 1000) / 1.609344 
   	    def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " Miles from: Home"
        if(status != device.currentValue('status')){
   	        sendEvent( name: "status", value: status, isStateChange:true )
            state.update = true
        }
        state.status = status
    }
	
    def km = sprintf("%.2f", homeDistance / 1000)
    if(km.toDouble().round(2) != device.currentValue('distanceKm')){
        sendEvent( name: "distanceKm", value: km.toDouble().round(2) )
        state.update = true
    }

    def miles = sprintf("%.2f", (homeDistance / 1000) / 1.609344)
	if(miles.toDouble().round(2) != device.currentValue('distanceMiles')){    
        sendEvent( name: "distanceMiles", value: miles.toDouble().round(2) )
	    state.update = true
    }

    if(homeDistance.toDouble().round(2) != device.currentValue('distanceMetric')){
	    sendEvent( name: "distanceMetric", value: homeDistance.toDouble().round(2) )
	    state.update = true
    }

    if(state.update == true){
	    sendEvent( name: "lastLocationUpdate", value: "Last location update on:\r\n${formatLocalTime("MM/dd/yyyy @ h:mm:ss a")}" )
	    state.update = false
    }
    
    sendLife360Tile1()
}

private extraInfo(address1,address2,battery,charge,endTimestamp,inTransit,isDriving,latitude,longitude,since,speedMetric,speedMiles,speedKm,wifiState,xplaces,avatar,avatarHtml,lastUpdated){
	//if(logEnable) log.debug "extrainfo = Address 1 = $address1 | Address 2 = $address2 | Battery = $battery | Charging = $charge | Last Checkin = $endTimestamp | Moving = $inTransit | Driving = $isDriving | Latitude = $latitude | Longitude = $longitude | Since = $since | Speedmeters = $speedMetric | SpeedMPH = $speedMiles | SpeedKPH = $speedKm | Wifi = $wifiState"
	   
    if(address1.contains('Data')) {
        if(logEnable) log.trace "In extraInfo - Nothing to report (No Data)"
    } else {
    	if(address1 != device.currentValue('address1')){
            sendEvent( name: "address1Prev", value: device.currentValue('address1') )
            sendEvent( name: "address1", value: address1, isStateChange: true, displayed: true )
            sendEvent( name: "since", value: since )
	    }
        if(address2 != device.currentValue('address2')){
            sendEvent( name: "address2", value: address2 )   
	    }
	    if(battery != device.currentValue('battery'))
       	    sendEvent( name: "battery", value: battery )
        if(charge != device.currentValue('charge'))
   	        sendEvent( name: "charge", value: charge )

        def curcheckin = device.currentValue('lastCheckin').toString()
        if(endTimestamp != curcheckin)
       	    sendEvent( name: "lastCheckin", value: endTimestamp )
        if(inTransit != device.currentValue('inTransit'))
       	    sendEvent( name: "inTransit", value: inTransit )

	    def curDriving = device.currentValue('isDriving')
        //if(logEnable) log.debug "Current Driving Status = $curDriving - New Driving Status = $isDriving"
        if(isDriving != device.currentValue('isDriving')){
	    //if(logEnable) log.debug "If was different, isDriving = $isDriving"
       	    sendEvent( name: "isDriving", value: isDriving )
        }
        def curlat = device.currentValue('latitude').toString()
        def curlong = device.currentValue('longitude').toString()
        latitude = latitude.toString()
        longitude = longitude.toString()
        if(latitude != curlat)
            sendEvent( name: "latitude", value: latitude )
        if(longitude != curlong)
           	sendEvent( name: "longitude", value: longitude )
        if(speedMetric != device.currentValue('speedMetric'))
    	    sendEvent( name: "speedMetric", value: speedMetric )
        if(speedMiles != device.currentValue('speedMiles'))
        	sendEvent( name: "speedMiles", value: speedMiles )
        if(speedKm != device.currentValue('speedKm'))
        	sendEvent( name: "speedKm", value: speedKm )
        if(wifiState != device.currentValue('wifiState'))
           	sendEvent( name: "wifiState", value: wifiState )
        setBattery(battery.toInteger(), charge.toBoolean(), charge.toString())

        sendEvent( name: "savedPlaces", value: xplaces )
    
        sendEvent( name: "avatar", value: avatar )
        sendEvent( name: "avatarHtml", value: avatarHtml )

        sendEvent( name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a") )
    }
    sendLife360Tile1()
}

def setMemberId (String memberId) {
   if(logEnable) log.debug "MemberId = ${memberId}"
   state.life360MemberId = memberId
}

def getMemberId () {
	if(logEnable) log.debug "MemberId = ${state.life360MemberId}"
    return(state.life360MemberId)
}

private String formatValue(boolean present) {
	if (present)
	return "present"
	else
	return "not present"
}

private formatDescriptionText(String linkText, boolean present) {
	if (present)
		return "Life360 User $linkText has arrived"
	else
	return "Life360 User $linkText has left"
}

private getState(boolean present) {
	if (present)
		return "arrived"
	else
	return "left"
}

private toggleSleeping(sleeping = null) {
	sleeping = sleeping ?: (device.currentValue('sleeping') == 'not sleeping' ? 'sleeping' : 'not sleeping')
	def presence = device.currentValue('presence');
	
	if (presence != 'not present') {
		if (sleeping != device.currentValue('sleeping')) {
			sendEvent( name: "sleeping", value: sleeping,  descriptionText: sleeping == 'sleeping' ? 'Sleeping' : 'Awake' )
		}
		
		def display = presence + (presence == 'present' ? ', ' + sleeping : '')
		if (display != device.currentValue('display')) {
			sendEvent( name: "display", value: display )
		}
	}
}

def asleep() {
	toggleSleeping('sleeping')
}

def awake() {
	toggleSleeping('not sleeping')
}

def refresh() {
	parent.refresh()
return null
}

def setBattery(int percent, boolean charging, charge){
	if(percent != device.currentValue("battery"))
		sendEvent(name: "battery", value: percent);
    
def ps = device.currentValue("powerSource") == "BTRY" ? "false" : "true"
if(charge != ps)
		sendEvent(name: "powerSource", value: (charging ? "DC":"BTRY"));
}

private formatLocalTime(format = "EEE, MMM d yyyy @ h:mm:ss a z", time = now()) {
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}
