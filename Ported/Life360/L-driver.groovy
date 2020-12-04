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
 *  1.3.0 = 12/04/20 - Fixed condition to trigger presence & address changes 
 *  1.2.6 - 12/03/20 - Exterminating bugs
 *  1.2.5 - 12/02/20 - Prelim fix for address1prev and address1 eventing to allow for Life360 Tracker to keep track of departures / arrivals
 *  1.2.4 - 12/02/20 - Fix wifi status not updating on bpt-StatusTile1
 *  1.2.3 - 12/01/20 - Bug fixes and some winter cleaning
 *  1.2.2 - 12/01/20 - Updated supporting sharptools attributes and merged generatePresenceEvent with extraInfo calls
 *  1.2.1 - 12/01/20 - Avi cleaning up code and applying a more wholesome compare to v 1.1.1 functionality
 *  1.2.0 - 12/01/20 - Avi cleaning up code that I preliminary deem unnecessary (pre-testing)
 *  a.v.i - 11/29/20 - Avi modifications to include capabilities for SharpTools and changes to update logic for main attributes
 *
 *  1.1.1 - 11/22/20 - Fix by Avi (@9369292f1992a7d0e654). Thank you!
 *  1.1.0 - 11/18/20 - Changed boolean to bool
 *  ---
 *  1.0.0 - 01/18/20 - Initial release
 */

import java.text.SimpleDateFormat

metadata {
  definition (name: "Location Tracker User Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Ported/Life360/L-driver.groovy") {
        capability "Actuator"

        // **** Life360 ****
        capability "Presence Sensor"
        capability "Sensor"
        capability "Refresh"
        capability "Battery"
        capability "Power Source"

// Avi - added capabilities to support Sharptools.io Hero tile active attributes functionality
        capability "Switch" // for Sharptools Wifi Active Attribute (Hero Tile)
        capability "Contact Sensor" // for Sharptools Charging Active Attribute (Hero Tile)
        capability "Acceleration Sensor" // for Sharptools Distance Active Attribute (Hero Tile)
        capability "Temperature Measurement" // for Sharptools Speed Active / Rules Attribute (Hero Tile)
// Avi - end adds

        attribute "address1", "string"
        attribute "address1prev", "string"
        attribute "avatar", "string"
        attribute "avatarHtml", "string"
        attribute "battery", "number"
        attribute "charge", "bool"
        attribute "distanceMetric", "Number"
        attribute "distanceKm", "number"
        attribute "distanceMiles", "Number"
        attribute "bpt-history", "string"
        attribute "inTransit", "string"
        attribute "isDriving", "string"
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
        attribute "wifiState", "bool"

// Avi - added attributes to support capabilities added above for Sharptools.io tile formatting
        attribute "contact", "string"
        attribute "acceleration", "string"
        attribute "temperature", "number"
        attribute "switch", "enum", ["on", "off"]
// Avi - end adds

        // **** Life360 ****
        command "refresh"
        command "setBattery",["number","bool"]
        command "sendHistory", ["string"]
        command "sendTheMap", ["string"]
        command "historyClearData"

// Avi - added as a trigger to force renew / revalidate webhook subscription to Life360 notifications
        command "refreshCirclePush"
// Avi - end adds

  }
}

preferences {
    input title:"<b>Location Tracker User</b>", description:"Note: Any changes will take effect only on the NEXT update or forced refresh.", type:"paragraph", element:"paragraph"
    // Avi - commented out the MaxGPS preference field for now.  Code is commented out in the extraInfo function
    //       this may be restored if the currently applied consolidated logic proves to be unreliable
    // input "maxGPSJump", "number", title: "Max GPS Jump", description: "If you are getting a lot of false readings, raise this value", required: true, defaultValue: 25
    input "units", "enum", title: "Distance Units", description: "Miles or Kilometers", required: false, options:["Kilometers","Miles"]
    input "avatarFontSize", "text", title: "Avatar Font Size", required: true, defaultValue: "15"
    input "avatarSize", "text", title: "Avatar Size by Percentage", required: true, defaultValue: "75"
    input "historyFontSize", "text", title: "History Font Size", required: true, defaultValue: "15"
    input "historyHourType", "bool", title: "Time Selection for History Tile (Off for 24h, On for 12h)", required: false, defaultValue: false
    input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
}

def refreshCirclePush() {
    // Avi - manually ensure that Life360 notifications subscription is in order / valid
    log.info "Attempting to resubscribe to circle notifications"
    parent.createCircleSubscription()
}

def sendTheMap(theMap) {
    lastMap = "${theMap}"
    sendEvent(name: "lastMap", value: lastMap, displayed: true)
}

def sendStatusTile1() {
    if(logEnable) log.debug "In sendStatusTile1 - Making the Avatar Tile"
    def avat = device.currentValue("avatar")
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

    tileMap += "Phone Lvl: ${bLevel} - ${bCharge} - ${bWifiS}</p>"
    tileMap += "<p style='width:100%'>${lUpdated}</p>" //Avi - cleaned up formatting (cosmetic / personal preference only)
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

def generatePresenceEvent(memberPresence, address1, battery, charge, distanceAway, endTimestamp, inTransit, isDriving, latitude , longitude, since, speedMetric, wifiState, thePlaces, avatar, avatarHtml) {
    // Avi - cleaned up this function to (hopefully) streamline behavior

    if(logEnable) log.debug "In generatePresenceEvent = memberPresence = $memberPresence | Address 1 = $address1 | Battery = $battery | Charging = $charge | distanceAway: $distanceAway | Last Checkin = $endTimestamp | Moving = $inTransit | Driving = $isDriving | Latitude = $latitude | Longitude = $longitude | Since = $since | Speedmetric = $speedMetric | Wifi = $wifiState"

    def prevAddress = (device.currentValue('address1') != null) ? device.currentValue('address1') : "Lost"
    // def lastUpdated = formatLocalTime("MM/dd/yyyy @ h:mm:ss a")
    def lastUpdated = new Date()

    if(logEnable) log.debug "prevAddress = $prevAddress | newAddress = $address1"

    // Avi - If our address changed

    if (address1 != prevAddress) {

      // *** Update address & presence ***
      def linkText = getLinkText(device)
      def handlerName = (memberPresence == "present") ? "arrived" : "left"
      def descriptionText = "Life360 member" + linkText + " has " + handlerName

      if (logEnable) log.info "linkText = $linkText, descriptionText = $descriptionText, handlerName = $handlerName, memberPresence = $memberPresence"

        // *** Presence ***
        def results = [
          name: "presence",
          value: memberPresence,
          linkText: linkText,
          descriptionText: descriptionText,
          handlerName: handlerName
        ]

        sendEvent (results)
        state.presence = memberPresence

        if (logEnable) log.info "results = $results"

        // *** Address ***
        // Update old and current address attributes
        sendEvent( name: "address1prev", value: prevAddress)
        sendEvent( name: "address1", value: address1 )
        sendEvent( name: "lastLocationUpdate", value: lastUpdated )
        sendEvent( name: "since", value: since )
      }

        sendEvent( name: "longitude", value: longitude )
        sendEvent( name: "latitude", value: latitude )

        // *** Speed ***
        // Update speed in metric, km and miles

        // Speed in Metrics
        sendEvent( name: "speedMetric", value: speedMetric )

        // Speed in km
        speedKm = (speedMetric * 3.6).toDouble().round(2)
        sendEvent( name: "speedKm", value: speedKm )

        // Speed in miles
        speedMiles = (speedMetric * 2.23694).toDouble().round(2)
        sendEvent( name: "speedMiles", value: speedMiles )

        // *** Distance ***
        // Update distance in metric, km and miles

        // Distance in metric
        sendEvent( name: "distanceMetric", value: distanceAway.toDouble().round(2) )

        // Distance in km
        distanceKm = (distanceAway / 1000).toDouble().round(2)
        sendEvent( name: "distanceKm", value: distanceKm )

        // Distance in miles
        distanceMiles = ((distanceAway / 1000) / 1.609344).toDouble().round(2)
        sendEvent( name: "distanceMiles", value: distanceMiles )

        // Avi - Sharptools.io attribute for distance tile - Set acceleration to
        // active state only if we are *not* home...
        sAcceleration = (memberPresence == "not present") ? "active" : "inactive"
        sendEvent( name: "acceleration", value: sAcceleration )

        // Update state variables and display on device page
        state.oldDistanceAway = device.currentValue("distanceMetric")

        // Update status attribute with appropriate distance units
        // and update temperature attribute with appropriate speed units
        // as chosen by users in device preferences
        def sStatus
        if(units == "Kilometers" || units == null || units == "") {
          sStatus = sprintf("%.2f", distanceKm) + "  km from Home"
          sendEvent( name: "status", value: sStatus )
          state.status = sStatus

          sendEvent( name: "temperature", value: speedKm )
        }
        else {
          sStatus = sprintf("%.2f", distanceMiles) + " miles from Home"
          sendEvent( name: "status", value: sStatus )
          state.status = sStatus

          sendEvent( name: "temperature", value: speedMiles )
        }


    sendEvent( name: "inTransit", value: inTransit )
    sendEvent( name: "isDriving", value: isDriving )

    // *** Battery ***
    // How is our battery doing?
    setBattery(battery.toInteger(), charge.toBoolean(), charge.toString())
    sendEvent( name: "battery", value: battery )

    // Sharptools.io tile attribute: If Battery is charging set contact sensor to open.  closed if not charging
    def cContact = charge.toBoolean() ? "open" : "closed"
    sendEvent( name: "charge", value: charge )
    sendEvent( name: "contact", value: cContact )

    // *** Wifi ***
    // Sharptools.io tile attribute - if wifi on then set switch to on
    def sSwitch = wifiState.toBoolean() ? "on" : "off"
    sendEvent( name: "wifiState", value: wifiState )
    sendEvent( name: "switch", value: sSwitch )

    // *** All others ***
    sendEvent( name: "savedPlaces", value: thePlaces )
    sendEvent( name: "avatar", value: avatar )
    sendEvent( name: "avatarHtml", value: avatarHtml )

    // *** Timestamp last update ***
    sendEvent ( name: "lastUpdated", value: lastUpdated )
    state.update = true

    // Lastly update the status tile
    sendStatusTile1()
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
  parent.refresh()
  parent.updateMembers()
    //return null
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
