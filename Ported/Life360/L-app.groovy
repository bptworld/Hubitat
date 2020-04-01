/**
 *  ---- Original Header ----
 *
 *  Life360 with States - Hubitat Port
 *
 *	BTRIAL DISTANCE AND SLEEP PATCH 29-12-2017
 *	Updated Code to handle distance from, and sleep functionality
 *
 *	TMLEAFS REFRESH PATCH 06-12-2016 V1.1
 *	Updated Code to match Smartthings updates 12-05-2017 V1.2
 *	Added updateMember function that pulls all usefull information Life360 provides for webCoRE use V2.0
 *	
 *  Copyright 2014 Jeff's Account
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
 * ---- End of original header ----
 *
 * ---- New Header ----
 *
 *  ****************  L360 with States App  ****************
 *
 *  Design Usage:
 *  Life360 with all States included
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Special thanks goes out to @cwwilson08 for working on and figuring out the oauth stuff!  This would not be possible
 *  without his work.
 *
 *  Changes:
 *
 *  V2.0.3 - 04/01/20 - Added a timeout to get http commands
 *  V2.0.2 - 01/21/20 - Adjusted app to work with new driver
 *  V2.0.1 - 01/03/20 - Adjusted logging to not show sensitive data
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  ---
 *  v1.0.0 - 06/30/19 - Initial port of ST app (cwwilson08) (bptworld)
 */

import java.text.SimpleDateFormat

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion
    state.appName = "Life360withStatesParentVersion"
	state.version = "v2.0.3"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Life360 with States",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Life360 with all States Included",
	category: "",
    iconUrl: "",
    iconX2Url: "",
    oauth: [displayName: "Life360", displayLink: "Life360"],
    singleInstance: true,
    importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Ported/Life360/L-app.groovy",
) {
	appSetting "clientId"
	appSetting "clientSecret"
}

preferences {
    page(name: "Credentials", title: "Enter Life360 Credentials", content: "getCredentialsPage", nextPage: "testLife360Connection", install: false)
    page(name: "listCirclesPage", title: "Select Life360 Circle", content: "listCircles", install: false)
    page(name: "myPlaces", title: "My Places", content: "myPlaces", install: true)
}

mappings {
	path("/placecallback") {
		action: [
              POST: "placeEventHandler",
              GET: "placeEventHandler"
		]
	}
    
    path("/receiveToken") {
		action: [
            POST: "receiveToken",
            GET: "receiveToken"
		]
	}
}

def getCredentialsPage() {
    if(logEnable) log.debug "In getCredentialsPage - (${state.version})"
    if(state.life360AccessToken) {
        listCircles()
    } else {
        dynamicPage(name: "Credentials", title: "Enter Life360 Credentials", nextPage: "listCirclesPage", uninstall: true, install:false){
            section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Credentials")) {
    		    input "username", "text", title: "Life360 Username?", multiple: false, required: true
    		    input "password", "password", title: "Life360 Password?", multiple: false, required: true, autoCorrect: false
    	    }
        }
    }
}

def getCredentialsErrorPage(String message) {
    if(logEnable) log.debug "In getCredentialsErrorPage - (${state.version})"
    dynamicPage(name: "Credentials", title: "Enter Life360 Credentials", nextPage: "listCirclesPage", uninstall: uninstallOption, install:false) {
    	section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Credentials")) {
    		input "username", "text", title: "Life360 Username?", multiple: false, required: true
    		input "password", "password", title: "Life360 Password?", multiple: false, required: true, autoCorrect: false
            paragraph "${message}"
    	}
    }
}

def testLife360Connection() {
    if(logEnable) log.debug "In testLife360Connection - (${state.version})"
    if(state.life360AccessToken) {
        if(logEnable) log.debug "In testLife360Connection - Good!"
   		//listCircles()
        true
    } else {
        if(logEnable) log.debug "In testLife360Connection - Bad!"
    	initializeLife360Connection()
    }
}

 def initializeLife360Connection() {
    if(logEnable) log.debug "In initializeLife360Connection - (${state.version})"

    initialize()

    def username = settings.username
    def password = settings.password

    def url = "https://api.life360.com/v3/oauth2/token.json"
        
    def postBody =  "grant_type=password&" +
    				"username=${username}&"+
                    "password=${password}"

    def result = null

    try {
       
     		httpPost(uri: url, body: postBody, headers: ["Authorization": "Basic cFJFcXVnYWJSZXRyZTRFc3RldGhlcnVmcmVQdW1hbUV4dWNyRUh1YzptM2ZydXBSZXRSZXN3ZXJFQ2hBUHJFOTZxYWtFZHI0Vg==" ]) {response -> 
     		    result = response
                //if(logEnable) log.debug result
    		}
        if (result.data.access_token) {
            //if(logEnable) log.debug result
       		state.life360AccessToken = result.data.access_token
            return true;
       	}
    	//if(logEnable) log.debug "Life360 initializeLife360Connection, response=${result.data}"
        return ;   
    }
    catch (e) {
       log.error "Life360 initializeLife360Connection, error: $e"
       return false;
    }
}

def listCircles() {
    if(logEnable) log.debug "In listCircles - (${state.version})"
    def uninstallOption = false
    if (app.installationState == "COMPLETE") uninstallOption = true
    dynamicPage(name: "listCirclesPage", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 with States</h2>", install: true, uninstall: true) {
        display()
    	// get connected to life360 api

    	if(testLife360Connection()) {
    	    def urlCircles = "https://api.life360.com/v3/circles.json"
 
    	    def resultCircles = null
            //if(logEnable) log.debug "AccessToken: ${state.life360AccessToken}"
       
		    httpGet(uri: urlCircles, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response -> 
    	         resultCircles = response
		    }

		    //if(logEnable) log.debug "Circles: ${resultCircles.data}"
    	    def circles = resultCircles.data.circles
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Circle")) {
        	    input "circle", "enum", multiple: false, required:true, title:"Life360 Circle", options: circles.collectEntries{[it.id, it.name]}, submitOnChange: true	
            }
            
            if(circles) {
                  state.circle = settings.circle
            } else {
    	        getCredentialsErrorPage("Invalid Usernaname or password.")
            }
        }

        if(circle) {
            if(logEnable) log.debug "In listPlaces - (${state.version})"
            if (app.installationState == "COMPLETE") uninstallOption = true
       
            if (!state?.circle) state.circle = settings.circle

            // call life360 and get the list of places in the circle

            def url = "https://api.life360.com/v3/circles/${state.circle}/places.json"
     
            def result = null
       
            httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response -> 
     	        result = response
            }

            //if(logEnable) log.debug "Places=${result.data}" 

            def places = result.data.places
            
            state.places = places
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Place to Match Current Location")) {
                paragraph "Please select the ONE Life360 Place that matches your Hubitat location: ${location.name}"
                thePlaces = places.collectEntries{[it.id, it.name]}
                sortedPlaces = thePlaces.sort { a, b -> a.value <=> b.value }
                input "place", "enum", multiple: false, required:true, title:"Life360 Places: ", options: sortedPlaces, submitOnChange: true
            }
        }
        
        if(place && circle) {
            if(logEnable) log.debug "In listUsers - (${state.version})"
            // understand whether to present the Uninstall option
            if (app.installationState == "COMPLETE") uninstallOption = true

            if (!state?.circle) state.circle = settings.circle

            // call life360 and get list of users (members)

            def url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
     
            def result = null
       
            httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response -> 
     	        result = response
            }

            //if(logEnable) log.debug "Members=${result.data}"

            // save members list for later

            def members = result.data.members

            state.members = members

            // build preferences page
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Members to Import into Hubitat")) {
                theMembers = members.collectEntries{[it.id, it.firstName+" "+it.lastName]}
                sortedMembers = theMembers.sort { a, b -> a.value <=> b.value }
        	    input "users", "enum", multiple: true, required:false, title:"Life360 Members: ", options: sortedMembers, submitOnChange: true
            }
            // ** App Watchdog Code **
            section("This app supports App Watchdog 2! Click here for more Information", hideable: true, hidden: true) {
				paragraph "<b>Information</b><br>See if any compatible app needs an update, all in one place!"
                paragraph "<b>Requirements</b><br> - Must install the app 'App Watchdog'. Please visit <a href='https://community.hubitat.com/t/release-app-watchdog/9952' target='_blank'>this page</a> for more information.<br> - When you are ready to go, turn on the switch below<br> - Then select 'App Watchdog Data' from the dropdown.<br> - That's it, you will now be notified automaticaly of updates."
                input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")
			}
            if(sendToAWSwitch) {
                section(getFormat("header-green", "${getImage("Blank")}"+" App Watchdog 2")) {    
                    if(sendToAWSwitch) input(name: "awDevice", type: "capability.actuator", title: "Please select 'App Watchdog Data' from the dropdown", submitOnChange: true, required: true, multiple: false)
			        if(sendToAWSwitch && awDevice) setVersion()
                }
            }
            // ** End App Watchdog Code **
            section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
			    input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	    }
            display2()
        }
    }
}

def installed() {
    if(logEnable) log.debug "In installed - (${state.version})"
	if(!state?.circle) state.circle = settings.circle
    
    settings.users.each {memberId->
    	// if(logEnable) log.debug "Find by Member Id = ${memberId}"
    	def member = state.members.find{it.id==memberId}

       	// create the device
        if(member) {
          // Modified from @Stephack
            def childDevice = childList()
            if(childDevice.find{it.data.vcId == "${member}"}){
                if(logEnable) log.debug "${member.firstName} already exists...skipping"
            } else {
                if(logEnable) log.debug "Creating Life360 Device: " + member
                try{
                    addChildDevice("BPTWorld", "Location Tracker User Driver", "${app.id}.${member.id}", 1234, ["name": "Life360 - ${member.firstName}", isComponent: false])
                }
                catch (e) {
                    log.error "Child device creation failed with error = ${e}"
                }
            }
          // end mod
            
            if (childDevice)
        	{
        		if(logEnable) log.debug "Child Device Successfully Created"
     			generateInitialEvent (member, childDevice)
       		}
    	}
    }
    createCircleSubscription()
}

def createCircleSubscription() {
    if(logEnable) log.debug "In createCircleSubscription - (${state.version})"

    if(logEnable) log.debug "Remove any existing Life360 Webhooks for this Circle."

    def deleteUrl = "https://api.life360.com/v3/circles/${state.circle}/webhook.json"

    try { // ignore any errors - there many not be any existing webhooks

    	httpDelete (uri: deleteUrl, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     		result = response}
    		}

    catch (e) {

    	log.debug (e)
    }

    // subscribe to the life360 webhook to get push notifications on place events within this circle

    if(logEnable) log.debug "Create a new Life360 Webhooks for this Circle."

    createAccessToken() // create our own OAUTH access token to use in webhook url
   
    def hookUrl = "${getApiServerUrl()}/${hubUID}/apps/${app.id}/placecallback?access_token=${state.accessToken}"

    def url = "https://api.life360.com/v3/circles/${state.circle}/webhook.json"
        
    def postBody =  "url=${hookUrl}"

    def result = null

    try {
     	httpPost(uri: url, body: postBody, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	result = response}
    } catch (e) {
        log.debug (e)
    }

    // response from this call looks like this:
    // {"circleId":"41094b6a-32fc-4ef5-a9cd-913f82268836","userId":"0d1db550-9163-471b-8829-80b375e0fa51","clientId":"11",
    //    "hookUrl":"https://testurl.com"}

    //if(logEnable) log.debug "Response = ${result}"

    if (result.data?.hookUrl) {
    	    if(logEnable) log.debug "Webhook creation successful."

    	}
    }

def updated() {
    if(logEnable) log.debug "In updated - (${state.version})"
	if (!state?.circle)
        state.circle = settings.circle

	if(logEnable) log.debug "In updated() method."
 
    // loop through selected users and try to find child device for each
    settings.users.each {memberId->
    	def externalId = "${app.id}.${memberId}"
        
		// find the appropriate child device based on my app id and the device network id
		def deviceWrapper = getChildDevice("${externalId}")
        
        if (!deviceWrapper) { // device isn't there - so we need to create
    
    		member = state.members.find{it.id==memberId}
            
          // Modified from @Stephack  
            def childDevice = childList()
            if(childDevice.find{it.data.vcId == "${member}"}){
                if(logEnable) log.debug "${member.firstName} already exists...skipping"
            } else {
                if(logEnable) log.debug "Creating Life360 Device: " + member
                try{
                    addChildDevice("BPTWorld", "Location Tracker User Driver", "${app.id}.${member.id}", 1234, ["name": "Life360 - ${member.firstName}", isComponent: false])
                }
                catch (e) {
                    log.error "Child device creation failed with error = ${e}"
                }
            }
          // end mod
            
        	if (childDevice)
        	{
        		// if(logEnable) log.debug "Child Device Successfully Created"
 				generateInitialEvent (member, childDevice)
       		}
    	}
        else {
          	// if(logEnable) log.debug "Find by Member Id = ${memberId}"
    		def member = state.members.find{it.id==memberId}
        	generateInitialEvent (member, deviceWrapper)
        }
    }

	// Now remove any existing devices that represent users that are no longer selected
    def childDevices = childList()
    
    if(logEnable) log.debug "Child Devices: ${childDevices}"
    
    childDevices.each {childDevice->
        // log.debug "(l-439) Child = ${childDevice}, DNI=${childDevice.deviceNetworkId}"
        
        def (childAppName, childMemberId) = childDevice.deviceNetworkId.split("\\.")
        //if(logEnable) log.debug "Child Member Id = ${childMemberId}"
        //if(logEnable) log.debug "Settings.users = ${settings.users}"
        if (!settings.users.find{it==childMemberId}) {
            deleteChildDevice(childDevice.deviceNetworkId)
            def member = state.members.find {it.id==memberId}
            if (member) state.members.remove(member)
        }
    }
}

def generateInitialEvent (member, childDevice) {
    
    if(logEnable) log.debug "In generateInitialEvent - (${state.version})"
    runEvery1Minute(updateMembers)
    //schedule("30 * * * * ?", updateMembers)
    // lets figure out if the member is currently "home" (At the place)
    
    try { // we are going to just ignore any errors
    	//if(logEnable)log.info "Life360 generateInitialEvent($member, $childDevice)"
        
        def place = state.places.find{it.id==settings.place}

		if (place) {
        	def memberLatitude = new Float (member.location.latitude)
            def memberLongitude = new Float (member.location.longitude)
            def memberAddress1 = member.location.address1
            def memberLocationName = member.location.name
            def placeLatitude = new Float (place.latitude)
            def placeLongitude = new Float (place.longitude)
            def placeRadius = new Float (place.radius)
           
        
        	if(logEnable) log.debug "Member Location = ${memberLatitude}/${memberLongitude}"
            if(logEnable) log.debug "Place Location = ${placeLatitude}/${placeLongitude}"
            if(logEnable) log.debug "Place Radius = ${placeRadius}"
        
        	def distanceAway = haversine(memberLatitude, memberLongitude, placeLatitude, placeLongitude)*1000 // in meters
  
        	if(logEnable) log.debug "Distance Away = ${distanceAway}"
  
  			boolean isPresent = (distanceAway <= placeRadius)

			if(logEnable) log.info "Life360 generateInitialEvent, member: ($memberLatitude, $memberLongitude), place: ($placeLatitude, $placeLongitude), radius: $placeRadius, dist: $distanceAway, present: $isPresent"
              
        def address1
        def address2
        def speed
        def speedmeters
        def speedMPH
        def speedKPH 
        def xplaces
        def avatar
        def lastUpdated
             
        xplaces = state.places.name.replaceAll(", ",",")
        
        lastUpdated = new Date()
            
        if (member.avatar != null){
        avatar = member.avatar
        avatarHtml =  "<img src= \"${avatar}\">"
        
    } else {
           
        avatar = "not set"
        avatarHtml = "not set"
        }

        if(member.location.address1 == null || member.location.address1 == "")
        address1 = "No Data"
        else
        address1 = member.location.address1
        
        if(member.location.address2 == null || member.location.address2 == "")
        address2 = "No Data"
        else
        address2 = member.location.address2
        
		//Covert 0 1 to False True	
	    def charging = member.location.charge == "0" ? "false" : "true"
        def moving = member.location.inTransit == "0" ? "false" : "true"
		def driving = member.location.isDriving == "0" ? "false" : "true"
	    def wifi = member.location.wifiState == "0" ? "false" : "true"
        
        //Fix Iphone -1 speed 
        if(member.location.speed.toFloat() == -1){
        speed = 0
        speed = speed.toFloat()}
        else
        speed = member.location.speed.toFloat()

		if(speed > 0 ){
        speedmeters = speed.toDouble().round(2)
        speedMPH = speedmeters.toFloat() * 2.23694
        speedMPH = speedMPH.toDouble().round(2)
        speedKPH = speedmeters.toFloat() * 3.6
        speedKPH = speedKPH.toDouble().round(2)
        }else{
        speedmeters = 0
        speedMPH = 0
        speedKPH = 0
        }
        
        def battery = Math.round(member.location.battery.toDouble())
        def latitude = member.location.latitude.toFloat()
        def longitude = member.location.longitude.toFloat()
        
		//Sent data	
        childDevice?.extraInfo(address1,address2,battery,charging,member.location.endTimestamp,moving,driving,latitude,longitude,member.location.since,speedmeters,speedMPH,speedKPH,wifi,xplaces,avatar,avatarHtml,lastUpdated)
       
        childDevice?.generatePresenceEvent(isPresent, distanceAway)
        
        // if(logEnable) log.debug "After generating presence event."          
    	}    
     }
    catch (e) {
    	// eat it
    }  
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    
    if(awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def haversine(lat1, lon1, lat2, lon2) {
    def R = 6372.8
    // In kilometers
    def dLat = Math.toRadians(lat2 - lat1)
    def dLon = Math.toRadians(lon2 - lon1)
    lat1 = Math.toRadians(lat1)
    lat2 = Math.toRadians(lat2)
 
    def a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
    def c = 2 * Math.asin(Math.sqrt(a))
    def d = R * c
    return(d)
}

def placeEventHandler() {
	if(logEnable) log.warn "Life360 placeEventHandler: params= THIS IS THE LINE I'M LOOKING FOR"
    //if(logEnable) log.debug "Life360 placeEventHandler: settings.place=$settings.place"
    
    def circleId = params?.circleId
    def placeId = params?.placeId
    def userId = params?.userId
    def direction = params?.direction
    def timestamp = params?.timestamp
    
    if (placeId == settings.place) {
		def presenceState = (direction=="in")
		def externalId = "${app.id}.${userId}"
        
		// find the appropriate child device based on my app id and the device network id
		def deviceWrapper = getChildDevice("${externalId}")

		// invoke the generatePresenceEvent method on the child device
		if (deviceWrapper) {
			deviceWrapper.generatePresenceEvent(presenceState, 0)
    		if(logEnable) log.debug "Life360 event raised on child device: ${externalId}"
		}
   		else {
    		log.warn "Life360 couldn't find child device associated with inbound Life360 event."
    	}
    }
}

def refresh() {
    listCircles()
    updated()
}

def updateMembers(){
    if(logEnable) log.debug "In updateMembers - (${state.version})"
	if (!state?.circle) state.circle = settings.circle
    
    def url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
    def result = null
    sendCmd(url, result)
}

def sendCmd(url, result){ 
    def requestParams = [ uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}"], timeout: 10 ]
    asynchttpGet("cmdHandler", requestParams)
}

def cmdHandler(resp, data) {
    
    if(resp.getStatus() == 200 || resp.getStatus() == 207) {
       
        result = resp.getJson()
	
	//if(logEnable) log.debug "Latest Members=${result.data}"
    	def members = result.members
    	state.members = members
    
	settings.users.each {memberId->
    
    	//if(logEnable) log.debug "appid $app.id memberid $memberId"	
    
    	def externalId = "${app.id}.${memberId}"
        
        //if(logEnable) log.debug "ExternalId = $externalId"
        
   	def member = state.members.find{it.id==memberId}

    //if(logEnable) log.debug "member = $member"

	// find the appropriate child device based on my app id and the device network id

    def deviceWrapper = getChildDevice("${externalId}") 
    def address1
    def address2
    def speed
    def speedMetric
    def speedMiles
    def speedKm
    def xplaces
    def lastUpdated
       
    thePlaces = state.places.sort { a, b -> a.name <=> b.name }
        
    xplaces = "${thePlaces.name}".replaceAll(", ",",")
    
    lastUpdated = new Date()
        
    //log.warn "xplaces: ${xplaces}"
        
    if (member.avatar != null){
        avatar = member.avatar
        avatarHtml =  "<img src= \"${avatar}\">"
        
    } else {
           
        avatar = "not set"
        avatarHtml = "not set"
        }
                  
    if(member.location.address1 == null || member.location.address1 == "")
        address1 = "No Data"
    else
        address1 = member.location.address1
        
    if(member.location.address2 == null || member.location.address2 == "")
        address2 = "No Data"
    else
        address2 = member.location.address2
        
    //Covert 0 1 to False True	
	def charging = member.location.charge == "0" ? "false" : "true"
    def moving = member.location.inTransit == "0" ? "false" : "true"
	def driving = member.location.isDriving == "0" ? "false" : "true"
	def wifi = member.location.wifiState == "0" ? "false" : "true"
        
    //Fix Iphone -1 speed 
    if(member.location.speed.toFloat() == -1){
        speed = 0
        speed = speed.toFloat()}
    else
        speed = member.location.speed.toFloat()
        
	if(speed > 0 ){
        speedMetric = speed.toDouble().round(2)
        speedMiles = speedMetric.toFloat() * 2.23694
        speedMiles = speedMiles.toDouble().round(2)
        speedKm = speedMetric.toFloat() * 3.6
        speedKm = speedKm.toDouble().round(2)
    }else{
        speedMetric = 0
        speedMiles = 0
        speedKm = 0
    }
                
    def battery = Math.round(member.location.battery.toDouble())
    def latitude = member.location.latitude.toFloat()
    def longitude = member.location.longitude.toFloat()
    //if(logEnable) log.debug "extrainfo = Address 1 = $address1 | Address 2 = $address2 | Battery = $battery | Charging = $charging | Last Checkin = $member.location.endTimestamp | Moving = $moving | Driving = $driving | Latitude = $latitude | Longitude = $longitude | Since = $member.location.since | Speedmeters = $speedMetric | SpeedMPH = $speedMiles | SpeedKPH = $speedKm | Wifi = $wifi"
        deviceWrapper.extraInfo(address1,address2,battery,charging,member.location.endTimestamp,moving,driving,latitude,longitude,member.location.since,speedMetric,speedMiles,speedKm,wifi,xplaces,avatar,avatarHtml,lastUpdated)
             
    def place = state.places.find{it.id==settings.place}
	if (place) {
        def memberLatitude = new Float (member.location.latitude)
        def memberLongitude = new Float (member.location.longitude)
        def memberAddress1 = member.location.address1
        def memberLocationName = member.location.name
        def placeLatitude = new Float (place.latitude)
        def placeLongitude = new Float (place.longitude)
        def placeRadius = new Float (place.radius)
        def distanceAway = haversine(memberLatitude, memberLongitude, placeLatitude, placeLongitude)*1000 // in meters
  
  		boolean isPresent = (distanceAway <= placeRadius)

		if(logEnable) log.info "Life360 Update member ($member.firstName): ($memberLatitude, $memberLongitude), place: ($placeLatitude, $placeLongitude), radius: $placeRadius, dist: $distanceAway, present: $isPresent"
  			
        deviceWrapper.generatePresenceEvent(isPresent, distanceAway)
            }
        }     
    }
}

def childList() {
	def children = getChildDevices()
    if(logEnable) log.debug "In childList - children: ${children}"
	return children
}

// ********** Normal Stuff **********

def getImage(type) {					// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Life360 with States - @cwwilson08 & @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
