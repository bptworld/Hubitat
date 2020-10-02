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
        
        // extra attributes for Location Tracker
        //attribute "address1", "string"
        attribute "activity", "string"
        attribute "avatarURL", "string"
        //attribute "battery", "number"
        attribute "currentCity", "string"
        attribute "currentState", "string"
        attribute "currentpostalCode", "string"
        attribute "lastUpdateDate", "string"
        attribute "lastUpdateTime", "string"
		//attribute "latitude", "number"
        //attribute "longitude", "number"
        attribute "locAlt", "number"
        attribute "locSpd", "number"
        attribute "mapURL", "string"
        //attribute "wifiState", "string"
        
        // **** Life360 ****
	    command "refresh"
        command "setBattery",["number","boolean"]
        command "sendHistory", ["string"]
        command "sendTheMap", ["string"]
        command "historyClearData"
        
        // **** Location Tracker ****
        //command "sendTheMap", ["string"]
        command "deviceLoc", ["string"]
        command "deviceOther", ["string"]
	}
}
           
preferences {
	//input title:"<b>Location Tracker User</b>", description:"Note: Any changes will take effect only on the NEXT update or forced refresh. Items with (Places) are optional and only needed when the NEW Location Tracker app is released", type:"paragraph", element:"paragraph"
    input title:"<b>Location Tracker User</b>", description:"Note: Any changes will take effect only on the NEXT update or forced refresh.", type:"paragraph", element:"paragraph"
    
    //input "apiKey", "text", title: "API Key from Google Maps (Places)", required: false
    //input "consumerKey", "text", title: "Consumer Key from MapQuest (Places)", required: false
    //input "threshold", "number", title: "Min minutes between checks (Places)", required: false, defaultValue: 2
    //input "avatarURL", "text", title: "Avatar URL (Places)", required: false
        
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

// *********************************************************
// **********  Start of Location Tracker - Places **********
// *********************************************************

// **** Location Tracker - Places ****
def deviceLoc(date, time, latitude, longitude, locSpd, locAlt) {
    if(logEnable) log.debug "In deviceLoc - date: ${date}, time: ${time}, Lat: ${latitude}, Lng: ${longitude}, locSpd: ${locSpd}, locAlt: ${locAlt}"
    sendEvent(name: "lastUpdateDate", value: date)
    sendEvent(name: "lastUpdateTime", value: time)
    sendEvent(name: "latitude", value: latitude)
    sendEvent(name: "longitude", value: longitude)
    sendEvent(name: "locSpd", value: locSpd)
    sendEvent(name: "locAlt", value: locAlt)
    
    getLocation(latitude, longitude)
}

// **** Location Tracker - Places ****
def deviceOther(battery, wifi) {
    if(logEnable) log.debug "In deviceOther - Batt: ${battery}, wifi: ${wifi}"
    sendEvent(name: "battery", value: battery)
    sendEvent(name: "wifiState", value: wifi)
    //sendEvent(name: "activity", value: activity)
    //sendEvent(name: "mapURL", value: mapURL)
    
    sendEvent( name: "lastLocationUpdate", value: "Last location update on:\r\n${formatLocalTime("MM/dd/yyyy @ h:mm:ss a")}" )
    def date = new Date()
    sendEvent(name: "lastUpdated", value: date.format("MM-dd - h:mm:ss a"))
    sendStatusTile1()
}

// **** Location Tracker - Places ****
def getLocation(latitude, longitude) {
	if(logEnable) log.debug "In getLocation"
    if(state.timeMin == null) state.timeMin = 5
    getTimeDiff()
    
    // http://www.mapquestapi.com/geocoding/v1/reverse?key=KEY&location=30.333472,-81.470448&includeRoadMetadata=true&includeNearestIntersection=true

    if(consumerKey) {
        if(state.timeMin >= threshold) {
            theUrl = "https://www.mapquestapi.com/geocoding/v1/reverse?key=${consumerKey}&location=${latitude},${longitude}&includeRoadMetadata=true&includeNearestIntersection=true"
            def params = [uri: "${theUrl}", contentType: "application/json"]

		    httpGet(params) { response ->
			    theResults = response.data
                //if(logEnable) log.debug "In getLocation - response: ${response.data}"
                
                address1 = theResults.results.locations.street.toString()
                currentCity = theResults.results.locations.adminArea5.toString()
                currentState = theResults.results.locations.adminArea3.toString()
                currentpostalCode = theResults.results.locations.postalCode.toString()
                
                address1 = "${address1}".replace("[","").replace("]","")
                currentCity = "${currentCity}".replace("[","").replace("]","")
                currentState = "${currentState}".replace("[","").replace("]","")
                currentpostalCode = "${currentpostalCode}".replace("[","").replace("]","")
                
                currentStateZip = "${currentState} ${currentpostalCode}"
                currentCountry = "-"
                
                if(logEnable) log.debug "In getLocation - street: ${address1} - City: ${currentCity} - State: ${currentState} - postalCode: ${currentpostalCode}"
                
                newAddress = address1
                oldAddress = device.currentValue('address1')
                if(newAddress != oldAddress) {
                    sendEvent(name: "address1prev", value: oldAddress)
                    sendEvent(name: "address1", value: newAddress)
                    sendEvent(name: "since", value: since)

                    if(newAddress == "home" || newAddress == "Home") { 
                        sendEvent(name: "presence", value: "present", isStateChange: true)
                    } else {
                        sendEvent(name: "presence", value: "not present", isStateChange: true)
                    }
                }
                
                prevAddress = device.currentValue('address1prev')
                if(prevAddress == null) {
                    sendEvent(name: "address1prev", value: "Lost")
                }
                
                sendEvent(name: "currentCity", value: currentCity)
                sendEvent(name: "currentStateZip", value: currentStateZip)
                sendEvent(name: "currentCountry", value: currentCountry)
                def lastRan = new Date()
                long unxSince = lastRan.getTime()
                state.unxSince = unxSince/1000
            }
        } else {
            if(logEnable) log.debug "In getLocation - Can't check for current stats - Under the ${threshold} min threshold."
        }
    }
    
    if(apiKey) {
        if(state.timeMin >= threshold) {
            theUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${latitude},${longitude}&result_type=street_address&key=${apiKey}"
            
            def params = [uri: "${theUrl}", contentType: "application/json"]

		    httpGet(params) { response ->
			    theResults = response.data
                //if(logEnable) log.debug "In getLocation - response: ${response.data}"
                
                formatted_address = theResults.results.formatted_address.toString()
                
                formatted_address = "${formatted_address}".replace("[","").replace("]","")
                
                def currentAddress = formatted_address.split(",")
                if(logEnable) log.debug "In getLocation  - 0: ${currentAddress[0]} - 1: ${currentAddress[1]} - 2: ${currentAddress[2]} - 3: ${currentAddress[3]}"
                if(logEnable) log.debug "In getLocation  - street: ${currentAddress[0]} - City: ${currentAddress[1]} - State Zip: ${currentAddress[2]} - Country: ${currentAddress[3]}"
                
                newAddress = currentAddress[0]
                oldAddress = device.currentValue('address1')
                if(newAddress != oldAddress) {
                    sendEvent(name: "address1prev", value: oldAddress)
                    sendEvent(name: "address1", value: newAddress)
                    sendEvent(name: "since", value: since)

                    if(newAddress == "home" || newAddress == "Home") { 
                        sendEvent(name: "presence", value: "present", isStateChange: true)
                    } else {
                        sendEvent(name: "presence", value: "not present", isStateChange: true)
                    }
                }
                    
                prevAddress = device.currentValue('address1prev')
                if(prevAddress == null) {
                    sendEvent(name: "address1prev", value: "Lost")
                }
                
                sendEvent(name: "currentCity", value: currentAddress[1])
                sendEvent(name: "currentStateZip", value: currentAddress[2])
                sendEvent(name: "currentCountry", value: currentAddress[3])
                def lastRan = new Date()
                long unxSince = lastRan.getTime()
                state.unxSince = unxSince/1000
            }
        } else {
            if(logEnable) log.debug "In getLocation - Can't check for current stats - Under the ${threshold} min threshold."
        }
    }
    def date = new Date()
    sendEvent(name: "lastUpdated", value: date.format("MM-dd - h:mm:ss a"))
    sendEvent( name: "lastLocationUpdate", value: "Last location update on:\r\n${formatLocalTime("MM/dd/yyyy @ h:mm:ss a")}" )
    sendStatusTile1()
}

// **** Location Tracker - Places ****
def getTimeDiff() {
    try {
        if(logEnable) log.debug "In getTimeDiff"
   	    def now = new Date()
        long unxNow = now.getTime()
        unxNow = unxNow/1000
        int unxSince = state.unxSince      
        long timeDiff = Math.abs(unxNow-unxSince)
        if(logEnable) log.debug "In getTimeDiff - since: ${unxSince}, Now: ${unxNow}, Diff: ${timeDiff}"   
	    state.timeMin = (((timeDiff % 86400 ) % 3600 ) / 60).toInteger()   
        if(logEnable) log.debug "In getTimeDiff - Time Diff: ${state.timeMin} mins"
    } catch (e) {
        if(logEnable) log.warn "In getTimeDiff - Something went wrong - setting Time Diff to 5 min so it will run - ERROR: ${e}"
    }
}

// *********************************************************
// ***********  End of Location Tracker - Places ***********
// *********************************************************

// *********************************************************
// ******************  Start of Life360  *******************
// *********************************************************

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

// **** Life360 ****
private extraInfo(address1,address2,battery,charge,endTimestamp,inTransit,isDriving,latitude,longitude,since,speedMetric,speedMiles,speedKm,wifiState,xplaces,avatar,avatarHtml,lastUpdated) {
    if(logEnable) log.debug "extrainfo = Address 1 = $address1 | Address 2 = $address2 | Battery = $battery | Charging = $charge | Last Checkin = $endTimestamp | Moving = $inTransit | Driving = $isDriving | Latitude = $latitude | Longitude = $longitude | Since = $since | Speedmeters = $speedMetric | SpeedMPH = $speedMiles | SpeedKPH = $speedKm | Wifi = $wifiState"

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

// **** Life360 ****
def setMemberId(String memberId) {
   if(logEnable) log.debug "MemberId = ${memberId}"
   state.life360MemberId = memberId
}

// **** Life360 ****
private String formatValue(boolean present) {
	if (present)
	return "present"
	else
	return "not present"
}

// **** Life360 ****
private formatDescriptionText(String linkText, boolean present) {
	if (present)
		return "Life360 User $linkText has arrived"
	else
	return "Life360 User $linkText has left"
}

// **** Life360 ****
def getMemberId() {
	if(logEnable) log.debug "MemberId = ${state.life360MemberId}"
    return(state.life360MemberId)
}

// **** Life360 ****
private getState(boolean present) {
	if (present)
		return "arrived"
	else
	return "left"
}

// **** Life360 ****
def refresh() {
	//parent.refresh()
    return null
}

// **** Life360 ****
def setBattery(int percent, boolean charging, charge) {
    if(percent != device.currentValue("battery")) { sendEvent(name: "battery", value: percent) }
    
    def ps = device.currentValue("powerSource") == "BTRY" ? "false" : "true"
    if(charge != ps) { sendEvent(name: "powerSource", value: (charging ? "DC":"BTRY")) }
}

// **** Life360 ****
private formatLocalTime(format = "EEE, MMM d yyyy @ h:mm:ss a z", time = now()) {
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}

// *********************************************************
// *******************  End of Life360  ********************
// *********************************************************
