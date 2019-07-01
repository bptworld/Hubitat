/**
 *  life360
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
 *  v1.0.1 - 06/30/19 - Added code to turn logging on and off. Tons of little code changes here and there for Hubitat (bptworld)
 *  v1.0.0 - 06/30/19 - Initial port of ST app (cwwilson08) (bptworld)
 */

definition(
    name: "Life360 (Connect)",
    namespace: "tmleafs",
    author: "tmleafs",
    description: "Life360 Service Manager",
	category: "",
    iconUrl: "",
    iconX2Url: "",
    oauth: [displayName: "Life360", displayLink: "Life360"],
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "clientSecret"
}

preferences {
	//page(name: "Credentials", title: "Life360 Authentication", content: "authPage", nextPage: "listCirclesPage", install: false)
    page(name: "Credentials", title: "Life360 Authentication", content: "authPage", install: false)
    page(name: "listCirclesPage", title: "Select Life360 Circle", nextPage: "listPlacesPage", content: "listCircles", install: false)
    page(name: "listPlacesPage", title: "Select Life360 Place", nextPage: "listUsersPage", content: "listPlaces", install: false)
    page(name: "listUsersPage", title: "Select Life360 Users", content: "listUsers", install: true)
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

def authPage() {
    if(logEnable) log.debug "authPage()"
    
    def description = "Life360 Credentials Already Entered."
    def uninstallOption = false
    if (app.installationState == "COMPLETE")
       uninstallOption = true

	if(!state.life360AccessToken) {
	    if(logEnable) log.debug "about to create access token"
		createAccessToken()
        description = "Click to enter Life360 Credentials."

		def redirectUrl = oauthInitUrl()
    
		return dynamicPage(name: "Credentials", title: "Life360", nextPage:"Credentials", uninstall: true, install: false) {
		    section {
    			href url:redirectUrl, style:"embedded", required:false, title:"Life360", description:description
		    }
   	 	}
    }
    else
    {
        state.life360AccessToken = "ZTVmMjNjNmEtMDYzNC00ODY0LWE2YzMtNDgyMTQ1MWE3YjZl"
    	listCircles()
    }
}

def receiveToken() {
	state.life360AccessToken = params.access_token
    def html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=100%">
<title>Life360 to Hubitat Connection</title>
<style type="text/css">
	@font-face {
		font-family: 'Swiss 721 W01 Thin';
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
		font-weight: normal;
		font-style: normal;
	}
	@font-face {
		font-family: 'Swiss 721 W01 Light';
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
		font-weight: normal;
		font-style: normal;
	}
	.container {
		width: 560px;
		padding: 40px;
		/*background: #eee;*/
		text-align: center;
	}
	img {
		vertical-align: middle;
	}
	img:nth-child(2) {
		margin: 0 30px;
	}
	p {
		font-size: 2.2em;
		font-family: 'Swiss 721 W01 Thin';
		text-align: center;
		color: #666666;
		padding: 0 40px;
		margin-bottom: 0;
	}
/*
	p:last-child {
		margin-top: 0px;
	}
*/
	span {
		font-family: 'Swiss 721 W01 Light';
	}
</style>
</head>
<body>
	<div class="container">
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/life360@2x.png" alt="Life360 icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<p>Your Life360 Account is now connected to Hubitat!</p>
		<p>Now go back to your hub and 'Add User App' again to finish setting up Life360 (Connect)</p>
	</div>
</body>
</html>
"""
	render contentType: 'text/html', data: html
}

def oauthInitUrl() {
    //log.debug "In oauthInitUrl..."
    def stcid = getSmartThingsClientId();

 	state.oauthInitState = UUID.randomUUID().toString()
    
 	def oauthParams = [
    	response_type: "token", 
        client_id: stcid,  
        redirect_uri: buildRedirectUrl() 
    ]

	return "https://api.life360.com/v3/oauth2/authorize?" + toQueryString(oauthParams)
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getSmartThingsClientId() {
   return "pREqugabRetre4EstetherufrePumamExucrEHuc"
}

def getServerUrl() { apiServerUrl("receiveToken") }

def buildRedirectUrl() {
    //log.debug "In buildRedirectUrl..."

    log.debug "ACCESS TOKEN: ${state.accessToken}"
    testToken = "c4f25cb7-5a47-455c-a290-145131859fe2"
    return "${getApiServerUrl()}/${hubUID}/apps/${app.id}/receiveToken"
}

def testLife360Connection() {
    if(logEnable) log.debug "In testLife360Connection..."
   	if (state.life360AccessToken)
   		true
    else
    	false
}

def listCircles(){
    if(logEnable) log.debug "In listCircles..."
	// understand whether to present the Uninstall option
    def uninstallOption = false
    if (app.installationState == "COMPLETE")
       uninstallOption = true

	// get connected to life360 api
	if (testLife360Connection()) {
    	def url = "https://api.life360.com/v3/circles.json"
 
    	def result = null
        if(logEnable) log.debug state.life360AccessToken
       
		httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
    	 	result = response
		}

		if(logEnable) log.debug "Circles=${result.data}"
    
    	def circles = result.data.circles
    
        if(logEnable) log.debug "In listCircles - Num of Circles: ${circles.size}"
    	if (circles.size > 1) {
    	    return (
    			dynamicPage(name: "listCirclesPage", title: "Life360 Circles", uninstall: true, install: false) {
     		   		section("Select Life360 Circle:") {
        				input "circle", "enum", multiple: false, required:true, title:"Life360 Circle: ", options: circles.collectEntries{[it.id, it.name]}	
        			}
    			}
	        )
    	}
    	else {
       		state.circle = circles[0].id
            state.circleName = circles[0].name
            if(logEnable) log.debug "In listCircles - Only have ${circles.size} circle, selecting ${state.circleName} (id:${state.circle}) and moving on"
       		return (listPlaces())
    	}  
	}
    else {
    	authPage()
    }
}

def listPlaces() {
    if(logEnable) log.debug "In listPlaces..."
	// understand whether to present the Uninstall option
    def uninstallOption = false
    if (app.installationState == "COMPLETE")
       uninstallOption = true
       
	if (!state?.circle)
        state.circle = settings.circle

	// call life360 and get the list of places in the circle
 	def url = "https://api.life360.com/v3/circles/${state.circle}/places.json"
    def result = null
       
	httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	result = response
	}

	if(logEnable) log.debug "Places=${result.data}" 
    def places = result.data.places
    state.places = places
       
    dynamicPage(name: "listPlacesPage", title: "Life360 Places", install:false) {
        section("Select Life360 Place to Match Current Location:") {
            paragraph "Please select the ONE Life360 Place that matches your Hubitat location: ${location.name}"
            paragraph "NOTE: Most of the time, this will be called 'Home'"
        	input "place", "enum", multiple: false, required:true, title:"Life360 Places: ", options: places.collectEntries{[it.id, it.name]}
            paragraph "<small>(If you don't see the 'Next' button, please refresh the page)</small>"
        }
    }
}

def listUsers() {
    if(logEnable) log.debug "In listUsers..."
	// understand whether to present the Uninstall option
    def uninstallOption = false
    if (app.installationState == "COMPLETE")
       uninstallOption = true
    
	if (!state?.circle)
        state.circle = settings.circle

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
    dynamicPage(name: "listUsersPage", title: "Life360 Users", uninstall: true, install: true) {
        section("Select Life360 Users to Import into Hubitat:") {
            paragraph "NOTES: Remember to remove users from this list before removing app."
        	input "users", "enum", multiple: true, required: false, title:"Life360 Users: ", options: members.collectEntries{[it.id, it.firstName+" "+it.lastName]}
        }
        section("Other Options") {
        	input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
        }
    }
}

def installed() {
    if(logEnable) log.debug "In installed..."
	if (!state?.circle)
        state.circle = settings.circle

	if(logEnable) log.debug "In installed..."
    
    settings.users.each {memberId->
    	// log.debug "Find by Member Id = ${memberId}"
    	def member = state.members.find{it.id==memberId}
        // if(logEnable) log.debug "After Find Attempt.
       	// if(logEnable) log.debug "Member Id = ${member.id}, Name = ${member.firstName} ${member.lastName}, Email Address = ${member.loginEmail}"
        // if(logEnable) log.debug "External Id=${app.id}:${member.id}"
       	// create the device
        if (member) {
       		def childDevice = addChildDevice("tmleafs", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true])
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
    	if(logEnable) log.debug (e)
    }
    
    if(logEnable) log.debug "Create a new Life360 Webhooks for this Circle."
    
    createAccessToken() // create our own OAUTH access token to use in webhook url
    
    def hookUrl = "${serverUrl}/api/smartapps/installations/${app.id}/placecallback?access_token=${state.accessToken}".encodeAsURL()
    def url = "https://api.life360.com/v3/circles/${state.circle}/webhook.json"  
    def postBody =  "url=${hookUrl}"
    def result = null
    
    try {
 	    httpPost(uri: url, body: postBody, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	    result = response}
    } catch (e) {
        if(logEnable) log.debug (e)
    }
    
    if(logEnable) log.debug "Response = ${response}"
    
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
       		def childDevice = addChildDevice("tmleafs", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true])
        
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
    	if(logEnable) log.info "Life360 generateInitialEvent($member, $childDevice)"
        
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
        childDevice?.extraInfo(address1,address2,battery,charging,member.location.endTimestamp,moving,driving,latitude,longitude,member.location.since,speedmeters,speedMPH,speedKPH,wifi)
       
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
	if(logEnable) log.info "Life360 placeEventHandler: params=$params"
    if(logEnable) log.info "Life360 placeEventHandler: settings.place=$settings.place"
    
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
listPlaces()
listUsers()
updated()
}

def updateMembers(){
    if(logEnable) log.debug "In updateMembers..."
	if (!state?.circle)
    	state.circle = settings.circle
    
    	def url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
    	def result = null
       
	httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	result = response
	}

	//if(logEnable) log.debug "Latest Members=${result.data}"
    	def members = result.data.members
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
        deviceWrapper.extraInfo(address1,address2,battery,charging,member.location.endTimestamp,moving,driving,latitude,longitude,member.location.since,speedMetric,speedMiles,speedKm,wifi)
             
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
