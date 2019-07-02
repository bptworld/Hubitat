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
 *  V1.0.2 - 07/02/19 - Added clientID, updated namespace/author so if something goes wrong people know who to contact.
 *  V1.0.1 - 06/30/19 - Added code to turn debug logging on and off (bptworld)
 *  V1.0.0 - 06/30/19 - Initial port of driver for Hubitat (bptworld)
 */
 
preferences {
	input title:"Distance", description:"This feature allows you change the display of distance to either Miles or KM. Please note, any changes will take effect only on the NEXT update or forced refresh.", type:"paragraph", element:"paragraph"
	input name: "units", type: "enum", title: "Distance Units", description: "Miles or Kilometers", required: false, options:["Kilometers","Miles"]
    input "clientID", "text", title: "Saved clientID from Life360 Install - Just here for safekeeping!",  required: false
    input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
} 
 
metadata {
	definition (name: "Life360 User", namespace: "BPTWorld", author: "Bryan Turcotte") {
	capability "Presence Sensor"
	capability "Sensor"
    capability "Refresh"
	capability "Sleep Sensor"
    capability "Battery"
    capability "Power Source"
    
	attribute "distanceMetric", "Number"
   	attribute "distanceKm", "Number"
	attribute "distanceMiles", "Number"
    attribute "prevAddress1", "String"
    attribute "prevAddress2", "String"
	attribute "address1", "String"
  	attribute "address2", "String"
  	attribute "battery", "number"
   	attribute "charge", "boolean"
   	attribute "lastCheckin", "number"
   	attribute "inTransit", "boolean"
   	attribute "isDriving", "boolean"
   	attribute "latitude", "number"
   	attribute "longitude", "number"
   	attribute "since", "number"
   	attribute "speedMetric", "number"
    attribute "speedMiles", "number"
    attribute "speedKm", "number"
   	attribute "wifiState", "boolean"

	command "refresh"
	command "asleep"
    command "awake"
    command "toggleSleeping"
    command "setBattery",["number","boolean"]
    
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}
}

def generatePresenceEvent(boolean present, homeDistance) {
	if(logEnable) log.debug "Life360 generatePresenceEvent (present = $present, homeDistance = $homeDistance)"
	def presence = formatValue(present)
	def linkText = getLinkText(device)
	def descriptionText = formatDescriptionText(linkText, present)
	def handlerName = getState(present)

	def sleeping = (presence == 'not present') ? 'not sleeping' : device.currentValue('sleeping')
	
	if (sleeping != device.currentValue('sleeping')) {
    	sendEvent( name: "sleeping", value: sleeping, isStateChange: true, displayed: true, descriptionText: sleeping == 'sleeping' ? 'Sleeping' : 'Awake' )
    }
	
    def display = presence + (presence == 'present' ? ', ' + sleeping : '')
	if (display != device.currentValue('display')) {
    	sendEvent( name: "display", value: display, isStateChange: true, displayed: false )
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
    sendEvent( name: "status", value: status, isStateChange: true, displayed: false )
    state.update = true}
    }else{
	def statusDistance = (homeDistance / 1000) / 1.609344 
   	def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " Miles from: Home"
    if(status != device.currentValue('status')){
   	sendEvent( name: "status", value: status, isStateChange: true, displayed: false )
    state.update = true}
	}
	
    def km = sprintf("%.2f", homeDistance / 1000)
    if(km.toDouble().round(2) != device.currentValue('distanceKm')){
    sendEvent( name: "distanceKm", value: km.toDouble().round(2), isStateChange: true, displayed: false )
    state.update = true}
    
    def miles = sprintf("%.2f", (homeDistance / 1000) / 1.609344)
	if(miles.toDouble().round(2) != device.currentValue('distanceMiles')){    
    sendEvent( name: "distanceMiles", value: miles.toDouble().round(2), isStateChange: true, displayed: false )
	state.update = true}
    
    if(homeDistance.toDouble().round(2) != device.currentValue('distanceMetric')){
	sendEvent( name: "distanceMetric", value: homeDistance.toDouble().round(2), isStateChange: true, displayed: false )
	state.update = true}
    
    if(state.update == true){
	sendEvent( name: "lastLocationUpdate", value: "Last location update on:\r\n${formatLocalTime("MM/dd/yyyy @ h:mm:ss a")}", displayed: false ) 
	state.update = false}
}

private extraInfo(address1,address2,battery,charge,endTimestamp,inTransit,isDriving,latitude,longitude,since,speedMetric,speedMiles,speedKm,wifiState){
	//if(logEnable) log.debug "extrainfo = Address 1 = $address1 | Address 2 = $address2 | Battery = $battery | Charging = $charge | Last Checkin = $endTimestamp | Moving = $inTransit | Driving = $isDriving | Latitude = $latitude | Longitude = $longitude | Since = $since | Speedmeters = $speedMetric | SpeedMPH = $speedMiles | SpeedKPH = $speedKm | Wifi = $wifiState"
	   
	if(address1 != device.currentValue('address1')){
    sendEvent( name: "prevAddress1", value: device.currentValue('address1'), isStateChange: true, displayed: false )
    sendEvent( name: "address1", value: address1, isStateChange: true, displayed: false )
	}
    if(address2 != device.currentValue('address2')){
    sendEvent( name: "prevAddress2", value: device.currentValue('address2'), isStateChange: true, displayed: false )
    sendEvent( name: "address2", value: address2, isStateChange: true, displayed: false )   
	}
	if(battery != device.currentValue('battery'))
   	sendEvent( name: "battery", value: battery, isStateChange: true, displayed: false )
    if(charge != device.currentValue('charge'))
   	sendEvent( name: "charge", value: charge, isStateChange: true, displayed: false )
    
    def curcheckin = device.currentValue('lastCheckin').toString()
    if(endTimestamp != curcheckin)
   	sendEvent( name: "lastCheckin", value: endTimestamp, isStateChange: true, displayed: false )
    if(inTransit != device.currentValue('inTransit'))
   	sendEvent( name: "inTransit", value: inTransit, isStateChange: true, displayed: false )
    
	def curDriving = device.currentValue('isDriving')
    //if(logEnable) log.debug "Current Driving Status = $curDriving - New Driving Status = $isDriving"
    if(isDriving != device.currentValue('isDriving')){
	//if(logEnable) log.debug "If was different, isDriving = $isDriving"
   	sendEvent( name: "isDriving", value: isDriving, isStateChange: true, displayed: false )
    }
    def curlat = device.currentValue('latitude').toString()
    def curlong = device.currentValue('longitude').toString()
    latitude = latitude.toString()
    longitude = longitude.toString()
    if(latitude != curlat)
    sendEvent( name: "latitude", value: latitude, isStateChange: true, displayed: false )
    if(longitude != curlong)
   	sendEvent( name: "longitude", value: longitude, isStateChange: true, displayed: false )
    if(since != device.currentValue('since'))
   	sendEvent( name: "since", value: since, isStateChange: true, displayed: false )
    if(speedMetric != device.currentValue('speedMetric'))
	sendEvent( name: "speedMetric", value: speedMetric, isStateChange: true, displayed: false )
    if(speedMiles != device.currentValue('speedMiles'))
	sendEvent( name: "speedMiles", value: speedMiles, isStateChange: true, displayed: false )
    if(speedKm != device.currentValue('speedKm'))
	sendEvent( name: "speedKm", value: speedKm, isStateChange: true, displayed: false )
    if(wifiState != device.currentValue('wifiState'))
   	sendEvent( name: "wifiState", value: wifiState, isStateChange: true, displayed: false )
   	setBattery(battery.toInteger(), charge.toBoolean(), charge.toString())
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
			sendEvent( name: "sleeping", value: sleeping, isStateChange: true, displayed: true, descriptionText: sleeping == 'sleeping' ? 'Sleeping' : 'Awake' )
		}
		
		def display = presence + (presence == 'present' ? ', ' + sleeping : '')
		if (display != device.currentValue('display')) {
			sendEvent( name: "display", value: display, isStateChange: true, displayed: false )
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
		sendEvent(name: "battery", value: percent, isStateChange: true, displayed: false);
        
    def ps = device.currentValue("powerSource") == "battery" ? "false" : "true"
    if(charge != ps)
		sendEvent(name: "powerSource", value: (charging ? "dc":"battery"), isStateChange: true, displayed: false);
}

private formatLocalTime(format = "EEE, MMM d yyyy @ h:mm:ss a z", time = now()) {
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}
