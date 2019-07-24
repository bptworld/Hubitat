/**
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
 *  Special thanks goes out to @cwwilson08 for working on and figuring out the oauth stuff!  This would not be possible
 *  without his work.
 *
 *  V1.1.4 - 07/23/19 - Places list now in alphabetical order.
 *  V1.1.3 - 07/12/19 - 1 min update http calls now use async thanks to cwwilson08
 *  V1.1.2 - 07/10/19 - More changes from cwwilson08
 *  V1.1.1 - 07/09/19 - Minor change to how the places are sent over
 *  V1.1.0 - 07/08/19 - Lists are now sent over to driver automatically, Added Avatar and code cleanup (cwwilson08)
 *  V1.0.9 - 07/07/19 - No more crazy setup thanks to cwwilson08!
 *  V1.0.8 - 07/06/19 - Fixed an issue with multiple circles
 *  V1.0.7 - 07/03/19 - More work done on webhooks and Oauth (cwwilson08)
 *  V1.0.6 - 07/03/19 - More code cleanup
 *  V1.0.5 - 07/02/19 - Updated namespace/author so if something goes wrong people know who to contact.
 *  V1.0.4 - 07/02/19 - Name changed to 'Life360 with States' to avoid confusion.
 *  v1.0.3 - 07/01/19 - Added both Long and Short Instructions.
 *  v1.0.2 - 07/01/19 - More code cleanup. Combined pages and colorized headers. Added importURL. Fixed 'Now Connected' page with
 *                      Hubitat info. Added newClientID up top in app to make it easier when pasting in code.
 *  v1.0.1 - 06/30/19 - Added code to turn logging on and off. Tons of little code changes here and there for Hubitat (bptworld)
 *  v1.0.0 - 06/30/19 - Initial port of ST app (cwwilson08) (bptworld)
 */

def setVersion() {
	state.version = "v1.1.4"
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
    if(logEnable) log.debug "In getCredentialsPage..."
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
    if(logEnable) log.debug "In getCredentialsErrorPage..."
    dynamicPage(name: "Credentials", title: "Enter Life360 Credentials", nextPage: "listCirclesPage", uninstall: uninstallOption, install:false) {
    	section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Credentials")) {
    		input "username", "text", title: "Life360 Username?", multiple: false, required: true
    		input "password", "password", title: "Life360 Password?", multiple: false, required: true, autoCorrect: false
            paragraph "${message}"
    	}
    }
}

def testLife360Connection() {
    if(logEnable) log.debug "In testLife360Connection..."
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
    if(logEnable) log.debug "In initializeLife360Connection..."

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
                if(logEnable) log.debug result
    		}
        if (result.data.access_token) {
            if(logEnable) log.debug result
       		state.life360AccessToken = result.data.access_token
            return true;
       	}
    	log.info "Life360 initializeLife360Connection, response=${result.data}"
        return ;   
    }
    catch (e) {
       log.error "Life360 initializeLife360Connection, error: $e"
       return false;
    }
}

def listCircles() {
    if(logEnable) log.debug "In listCircles..."
    def uninstallOption = false
    if (app.installationState == "COMPLETE") uninstallOption = true
    dynamicPage(name: "listCirclesPage", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 with States</h2>", install: true, uninstall: true) {
        display()
    	// get connected to life360 api

    	if(testLife360Connection()) {
    	    def urlCircles = "https://api.life360.com/v3/circles.json"
 
    	    def resultCircles = null
            if(logEnable) log.debug "AccessToken: ${state.life360AccessToken}"
       
		    httpGet(uri: urlCircles, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
    	         resultCircles = response
		    }

		    if(logEnable) log.debug "Circles: ${resultCircles.data}"
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
            if(logEnable) log.debug "In listPlaces..."
            if (app.installationState == "COMPLETE") uninstallOption = true
       
            if (!state?.circle) state.circle = settings.circle

            // call life360 and get the list of places in the circle

            def url = "https://api.life360.com/v3/circles/${state.circle}/places.json"
     
            def result = null
       
            httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	        result = response
            }

            if(logEnable) log.debug "Places=${result.data}" 

            def places = result.data.places
            
            state.places = places
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Place to Match Current Location")) {
                paragraph "Please select the ONE Life360 Place that matches your Hubitat location: ${location.name}"
                input "place", "enum", multiple: false, required:true, title:"Life360 Places: ", options: places.collectEntries{[it.id, it.name]}, submitOnChange: true
            }
        }
        
        if(place && circle) {
            if(logEnable) log.debug "In listUsers..."
            // understand whether to present the Uninstall option
            if (app.installationState == "COMPLETE") uninstallOption = true

            if (!state?.circle) state.circle = settings.circle

            // call life360 and get list of users (members)

            def url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
     
            def result = null
       
            httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	        result = response
            }

            if(logEnable) log.debug "Members=${result.data}"

            // save members list for later

            def members = result.data.members

            state.members = members

            // build preferences page
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Users to Import into Hubitat")) {
        	    input "users", "enum", multiple: true, required:false, title:"Life360 Users: ", options: members.collectEntries{[it.id, it.firstName+" "+it.lastName]}, submitOnChange: true
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
			    input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	    }
            display2()
        }
    }
}

def installed() {
    if(logEnable) log.debug "In installed..."
	if(!state?.circle) state.circle = settings.circle
    
    settings.users.each {memberId->
    	// if(logEnable) log.debug "Find by Member Id = ${memberId}"
    	def member = state.members.find{it.id==memberId}
        // if(logEnable) log.debug "After Find Attempt.
       	// if(logEnable) log.debug "Member Id = ${member.id}, Name = ${member.firstName} ${member.lastName}, Email Address = ${member.loginEmail}"
        // if(logEnable) log.debug "External Id=${app.id}:${member.id}"
       	// create the device
        if(member) {
            // old
            def childDevice = addChildDevice("BPTWorld", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true])
       		
            // new
           // def childDevice = addChildDevice("BPTWorld", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true, isComponent: false, componentName: "Life360 with States", componentLabel: member.firstName])
    	        	
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
    if(logEnable) log.debug "In createCircleSubscription..."

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

    if(logEnable) log.debug "Response = ${result}"

    if (result.data?.hookUrl) {
    	    if(logEnable) log.debug "Webhook creation successful. Response = ${result.data}"

    	}
    }

def updated() {
    if(logEnable) log.debug "In updated..."
	if (!state?.circle)
        state.circle = settings.circle

	if(logEnable) log.debug "In updated() method."
 
    // loop through selected users and try to find child device for each
    settings.users.each {memberId->
    	def externalId = "${app.id}.${memberId}"

		// find the appropriate child device based on my app id and the device network id
		def deviceWrapper = getChildDevice("${externalId}")
        
        if (!deviceWrapper) { // device isn't there - so we need to create
    
    		// if(logEnable) log.debug "Find by Member Id = ${memberId}"
    
    		def member = state.members.find{it.id==memberId}
       
       		// create the device
            // old
       		def childDevice = addChildDevice("BPTWorld", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true])
 
            // new
            //def childDevice = addChildDevice("BPTWorld", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true, isComponent: false, componentName: "Life360 with States", componentLabel: member.firstName])
            
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
    def childDevices = getAllChildDevices()
    if(logEnable) log.debug "Child Devices = ${childDevices}"
    
    childDevices.each {childDevice->
    	if(logEnable) log.debug "Child = ${childDevice}, DNI=${childDevice.deviceNetworkId}"
        
        // def childMemberId = childDevice.getMemberId()
        def splitStrings = childDevice.deviceNetworkId.split("\\.")
        if(logEnable) log.debug "Strings = ${splitStrings}"
        def childMemberId = splitStrings[1]
        if(logEnable) log.debug "Child Member Id = ${childMemberId}"
        if(logEnable) log.debug "Settings.users = ${settings.users}"
        if (!settings.users.find{it==childMemberId}) {
            deleteChildDevice(childDevice.deviceNetworkId)
            def member = state.members.find {it.id==memberId}
            if (member)
            	state.members.remove(member)
        }
    }
}

def generateInitialEvent (member, childDevice) {
    
    if(logEnable) log.debug "In generateInitialEvent..."
    runEvery1Minute(updateMembers)
    // lets figure out if the member is currently "home" (At the place)
    
    try { // we are going to just ignore any errors
    	if(logEnable)log.info "Life360 generateInitialEvent($member, $childDevice)"
        
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
            
            
        xplaces = state.places.name.replaceAll(", ",",")
              
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
        childDevice?.extraInfo(address1,address2,battery,charging,member.location.endTimestamp,moving,driving,latitude,longitude,member.location.since,speedmeters,speedMPH,speedKPH,wifi,xplaces,avatar,avatarHtml)
       
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
	if(logEnable) log.debug "Life360 placeEventHandler: params=$params"
    if(logEnable) log.debug "Life360 placeEventHandler: settings.place=$settings.place"
    
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
    if(logEnable) log.debug "In updateMembers..."
	if (!state?.circle) state.circle = settings.circle
    
    def url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
    def result = null
    sendCmd(url, result)
}

def sendCmd(url, result){ 
    def requestParams = [ uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}"]  ]
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
       
    thePlaces = state.places.sort { a, b -> a.name <=> b.name }
        
    xplaces = "${thePlaces.name}".replaceAll(", ",",")
        
    log.warn "xplaces: ${xplaces}"
        
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
        deviceWrapper.extraInfo(address1,address2,battery,charging,member.location.endTimestamp,moving,driving,latitude,longitude,member.location.since,speedMetric,speedMiles,speedKm,wifi,xplaces,avatar,avatarHtml)
             
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
