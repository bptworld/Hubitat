/**
 *  ****************  Location Tracker User Driver  ****************
 *
 *  Design Usage:
 *  This driver stores the user data to be used with Location Tracker.
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
 *  Special thanks to namespace: "tmleafs", author: "tmleafs" for the work on the Life360 ST driver
 *
 *  Changes:
 *
 *  1.0.8 - 10/07/20 - Attempting fix for jumping GPS
 *  1.0.7 - 10/01/20 - Added code adjustments from @napalmcsr
 *  1.0.6 - 06/17/20 - Added code for address1prev, other adjustments
 *  1.0.5 - 05/31/20 - Adjustments
 *  1.0.4 - 05/30/20 - Fix for History
 *  1.0.3 - 05/30/20 - Cosmetic Change - Recommended to delete device and recreate.
 *  1.0.2 - 05/29/20 - Adjusted placement of date/time stamp, made tile 'smartly' friendly
 *  1.0.1 - 04/12/20 - Added last updated date/time to StatusTile1, other small adjustments
 *  1.0.0 - 01/18/20 - Initial release
 */

import java.text.SimpleDateFormat

metadata {
	definition (name: "Location Tracker User Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
        capability "Actuator"
        
        // **** Life360 ****
	    capability "Presence Sensor"
	    capability "Sensor"
        capability "Refresh"
        capability "Battery"
        capability "Power Source"

        attribute "address1", "string"
        attribute "address1prev", "string"
        attribute "avatar", "string"
        attribute "avatarHtml", "string"
  	    attribute "battery", "number"
   	    attribute "charge", "boolean" //boolean
	    attribute "distanceMetric", "Number"
   	    attribute "distanceKm", "number"
	    attribute "distanceMiles", "Number"
	    attribute "bpt-history", "string"
       	attribute "inTransit", "string" //boolean
   	    attribute "isDriving", "string" //boolean
        attribute "lastLogMessage", "string"
        attribute "lastMap", "string"
        attribute "lastUpdated", "string"
   	    attribute "latitude", "number"
   	    attribute "longitude", "number"
        attribute "numOfCharacters", "number"
        attribute "savedPlaces", "map"
   	    attribute "since", "number"
   	    attribute "speedMetric", "number"
        attribute "speedMiles", "number"
        attribute "speedKm", "number"
        attribute "status", "string"
        attribute "bpt-statusTile1", "string"
   	    attribute "wifiState", "boolean" //boolean
        
        // **** Life360 ****
	    command "refresh"
        command "setBattery",["number","boolean"]
        command "sendHistory", ["string"]
        command "sendTheMap", ["string"]
        command "historyClearData"
	}
}
           
preferences {
    input title:"<b>Location Tracker User</b>", description:"Note: Any changes will take effect only on the NEXT update or forced refresh.", type:"paragraph", element:"paragraph"
    input "maxGPSJump", "number", title: "Max GPS Jump", description: "If you are getting a lot of false readings, raise this value", required: true, defaultValue: 25
	input "units", "enum", title: "Distance Units", description: "Miles or Kilometers", required: false, options:["Kilometers","Miles"]
    input "avatarFontSize", "text", title: "Avatar Font Size", required: true, defaultValue: "15"
    input "avatarSize", "text", title: "Avatar Size by Percentage", required: true, defaultValue: "75"
    input "historyFontSize", "text", title: "History Font Size", required: true, defaultValue: "15"
    input "historyHourType", "bool", title: "Time Selection for History Tile (Off for 24h, On for 12h)", required: false, defaultValue: false
    input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
} 
      
def sendTheMap(theMap) {
    lastMap = "${theMap}" 
    sendEvent(name: "lastMap", value: lastMap, displayed: true)
}
    
def sendStatusTile1() {
    if(logEnable) log.debug "In sendStatusTile1 - Making the Avatar Tile"    
    def avat = device.currentValue('avatar')
    if(avat == null || avat == "") avat = avatarURL
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
    lUpdated = device.currentValue('lastUpdated')
    sFont = avatarFontSize.toInteger() / 1.25
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E hh:mm a")
    String dateSince = DATE_FORMAT.format(theDate)

    theMap = "https://www.google.com/maps/search/?api=1&query=${device.currentValue('latitude')},${device.currentValue('longitude')}"
    
	tileMap = "<div style='overflow:auto;height:90%'><table width='100%'>"
    tileMap += "<tr><td width='25%' align=center><img src='${avat}' height='${avatarSize}%'>"
    tileMap += "<td width='75%'><p style='font-size:${avatarFontSize}px'>"
    tileMap += "At: <a href='${theMap}' target='_blank'>${add1}</a><br>"
    tileMap += "Since: ${dateSince}<br>${device.currentValue('status')}<br>"
    
    if(units == "Kilometers") tileMap += "${binTransita} - ${bSpeedKm} KMH<br>"
    if(units == "Miles") tileMap += "${binTransita} - ${bSpeedMiles} MPH<br>"
    
    tileMap += "Phone Lvl: ${bLevel} - ${bCharge} - ${bWifiS}<br></p>"
    tileMap += "<p style='width:100%;text-align:right;font-size:${sFont}px'>${lUpdated}&nbsp; &nbsp; &nbsp; &nbsp;</p>"
    tileMap += "</table></div>"
    
	tileDevice1Count = tileMap.length()
	if(tileDevice1Count <= 1000) {
		if(logEnable) log.debug "tileMap - has ${tileDevice1Count} Characters<br>${tileMap}"
	} else {
		log.warn "In sendStatusTile1 - Too many characters to display on Dashboard (${tileDevice1Count})"
	}
	sendEvent(name: "bpt-statusTile1", value: tileMap, displayed: true)
}

def sendHistory(msgValue) {
    if(logEnable) log.trace "In sendHistory - nameValue: ${msgValue}"

    if(msgValue.contains("No Data")) {
       if(logEnable) log.trace "In sendHistory - Nothing to report (No Data)"
    } else {   
        try {
            if(state.list1 == null) state.list1 = []
            
            getDateTime()
	        last = "${newDate} - ${msgValue}"
            state.list1.add(0,last)  

            if(state.list1) {
                listSize1 = state.list1.size()
            } else {
                listSize1 = 0
            }

            int intNumOfLines = 10
            if (listSize1 > intNumOfLines) state.list1.removeAt(intNumOfLines)
            String result1 = state.list1.join(";")
            def lines1 = result1.split(";")

            theData1 = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px'><tr><td>"
            
            for (i=0;i<intNumOfLines && i<listSize1;i++) {
                combined = theData1.length() + lines1[i].length()
                if(combined < 1006) {
                    theData1 += "${lines1[i]}<br>"
                }
            }

            theData1 += "</table></div>"
            if(logEnable) log.debug "theData1 - ${theData1.replace("<","!")}"       

            dataCharCount1 = theData1.length()
            if(dataCharCount1 <= 1024) {
                if(logEnable) log.debug "What did I Say Attribute - theData1 - ${dataCharCount1} Characters"
            } else {
                theData1 = "Too many characters to display on Dashboard (${dataCharCount1})"
            }
  
	        sendEvent(name: "bpt-history", value: theData1, displayed: true)
            sendEvent(name: "numOfCharacters", value: dataCharCount1, displayed: true)
            sendEvent(name: "lastLogMessage", value: msgValue, displayed: true)
        }
        catch(e1) {
            log.warn "In sendHistory - Something went wrong<br>${e1}"
            log.error e1
        }
    }
    sendStatusTile1()
}

def installed() {
    log.info "Location Tracker User Driver Installed"
    historyClearData()
}

def updated() {
    log.info "Location Tracker User Driver has been Updated"
}

def getDateTime() {
	def date = new Date()
	if(historyHourType == false) newDate=date.format("E HH:mm")
	if(historyHourType == true) newDate=date.format("E hh:mm a")
    return newDate
}

def historyClearData() {
	if(logEnable) log.debug "In historyClearData - Clearing the data"
    msgValue = "-"
    logCharCount = "0"
    state.list1 = []	
	historyLog = "Waiting for Data..."
    sendEvent(name: "bpt-history", value: historyLog, displayed: true)
    sendEvent(name: "numOfCharacters1", value: logCharCount1, displayed: true)
    sendEvent(name: "lastLogMessage1", value: msgValue, displayed: true)
}	

def generatePresenceEvent(boolean present, homeDistance) {
    if(logEnable) log.debug "In generatePresenceEvent - present: $present - homeDistance: $homeDistance"
    def linkText = getLinkText(device)
    def descriptionText = formatDescriptionText(linkText, present)
    def handlerName = getState(present)

    CurrentAddress = device.currentValue('address1')

    if(!present){
        if(CurrentAddress == "home" || CurrentAddress == "Home") {
            present = true
        }
    }

    def presence = formatValue(present)
    def results = [
        name: "presence",
        value: presence,
        linkText: linkText,
        descriptionText: descriptionText,
        handlerName: handlerName,
    ]
    if(logEnable) log.debug "In generatePresenceEvent - Generating Event: ${results}"
    sendEvent (results)

    if(units == "Kilometers" || units == null || units == ""){
        def statusDistance = homeDistance / 1000
        def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " km from: Home"
        if(status != device.currentValue('status')){
            sendEvent( name: "status", value: status )
            state.update = true
        }
    } else {
        def statusDistance = (homeDistance / 1000) / 1.609344 
        def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " Miles from: Home"
        if(status != device.currentValue('status')){
            sendEvent( name: "status", value: status )
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

    sendStatusTile1()
}

private extraInfo(address1, address2, battery, charge, distanceAway, endTimestamp, inTransit, isDriving, latitude , longitude, since, speedMetric ,speedMiles, speedKm, wifiState, xplaces, avatar, avatarHtml, lastUpdated) {
    if(logEnable) log.debug "extrainfo = Address 1 = $address1 | Address 2 = $address2 | Battery = $battery | Charging = $charge | distanceAway: $distanceAway | Last Checkin = $endTimestamp | Moving = $inTransit | Driving = $isDriving | Latitude = $latitude | Longitude = $longitude | Since = $since | Speedmeters = $speedMetric | SpeedMPH = $speedMiles | SpeedKPH = $speedKm | Wifi = $wifiState"

    if(state.oldDistanceAway == null) state.oldDistanceAway = distanceAway
    if(distanceAway == null) distanceAway = 0
    int newDistance = Math.abs(state.oldDistanceAway - distanceAway)
    if(logEnable) log.trace "oldDistanceAway: ${state.oldDistanceAway} - distanceAway: ${distanceAway} = newDistance: ${newDistance}"
    int theJump = maxGPSJump ?: 25
    if(newDistance >= theJump) {
        if(logEnable) log.trace "newDistance (${newDistance}) is greater than maxGPSJump (${theJump}) - Updating Data"
        state.oldDistanceAway = distanceAway
        newAddress = address1
        oldAddress = device.currentValue('address1')
        log.debug "oldAddress = $oldAddress | newAddress = $newAddress" 
        if(newAddress != oldAddress) {
            sendEvent(name: "address1prev", value: oldAddress)
            sendEvent(name: "address1", value: newAddress)
            sendEvent(name: "since", value: since)
        }

        prevAddress = device.currentValue('address1prev')
        if(prevAddress == null) {
            sendEvent(name: "address1prev", value: "Lost")
        }

        if(battery != device.currentValue('battery')) { sendEvent(name: "battery", value: battery) }    
        if(charge != device.currentValue('charge')) { sendEvent(name: "charge", value: charge) }

        if(inTransit != device.currentValue('inTransit')) { sendEvent(name: "inTransit", value: inTransit) }

        def curDriving = device.currentValue('isDriving') 
        if(isDriving != device.currentValue('isDriving')) { sendEvent(name: "isDriving", value: isDriving) }

        def curlat = device.currentValue('latitude').toString()
        latitude = latitude.toString()
        if(latitude != curlat) { sendEvent(name: "latitude", value: latitude) }

        def curlong = device.currentValue('longitude').toString()
        longitude = longitude.toString()
        if(longitude != curlong) { sendEvent(name: "longitude", value: longitude) }

        if(speedMetric != device.currentValue('speedMetric')) { sendEvent(name: "speedMetric", value: speedMetric) }

        if(speedMiles != device.currentValue('speedMiles')) { sendEvent(name: "speedMiles", value: speedMiles) }

        if(speedKm != device.currentValue('speedKm')) { sendEvent(name: "speedKm", value: speedKm) }

        if(wifiState != device.currentValue('wifiState')) { sendEvent(name: "wifiState", value: wifiState) }

        setBattery(battery.toInteger(), charge.toBoolean(), charge.toString())

        sendEvent(name: "savedPlaces", value: xplaces)

        sendEvent(name: "avatar", value: avatar)

        sendEvent(name: "avatarHtml", value: avatarHtml)

        sendEvent(name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a"))

        sendStatusTile1()
    }
}

def setMemberId(String memberId) {
   if(logEnable) log.debug "MemberId = ${memberId}"
   state.life360MemberId = memberId
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

def getMemberId() {
	if(logEnable) log.debug "MemberId = ${state.life360MemberId}"
    return(state.life360MemberId)
}

private getState(boolean present) {
	if (present)
		return "arrived"
	else
	return "left"
}

def refresh() {
	//parent.refresh()
    return null
}

def setBattery(int percent, boolean charging, charge) {
    if(percent != device.currentValue("battery")) { sendEvent(name: "battery", value: percent) }
    
    def ps = device.currentValue("powerSource") == "BTRY" ? "false" : "true"
    if(charge != ps) { sendEvent(name: "powerSource", value: (charging ? "DC":"BTRY")) }
}

private formatLocalTime(format = "EEE, MMM d yyyy @ h:mm:ss a z", time = now()) {
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}
