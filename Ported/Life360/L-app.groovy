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
 *  V1.0.4 - 07/02/19 - Name changed to 'Life360 with States' to avoid confusion.
 *  v1.0.3 - 07/01/19 - Added both Long and Short Instructions.
 *  v1.0.2 - 07/01/19 - More code cleanup. Combined pages and colorized headers. Added importURL. Fixed 'Now Connected' page with
 *                      Hubitat info. Added newClientID up top in app to make it easier when pasting in code.
 *  v1.0.1 - 06/30/19 - Added code to turn logging on and off. Tons of little code changes here and there for Hubitat (bptworld)
 *  v1.0.0 - 06/30/19 - Initial port of ST app (cwwilson08) (bptworld)
 */

//***********************************************************
def newClientID() {
    state.newClientID = "MGVhZGNiOGQtZjUzZi00M2JmLWEyYzctOWRkZDA4YjgyZjBj"
}
//***********************************************************

def setVersion() {
	state.version = "v1.0.4"
}

definition(
    name: "Life360 with States",
    namespace: "tmleafs",
    author: "tmleafs",
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
	//page(name: "Credentials", title: "Life360 Authentication", content: "authPage", nextPage: "testLife360Connection", install: false)
    page(name: "Credentials", title: "Life360 Authentication", content: "authPage", install: false)
    page(name: "listCirclesPage", title: "Select Life360 Circle", content: "listCircles")
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
    
		return dynamicPage(name: "Credentials", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 with States</h2>", uninstall: true, install: false) {
            display()
            section("LONG Instructions:", hideable: true, hidden: true) {
                paragraph "<b>Install Instructions</b>"
                paragraph "Please follow the instructions carefully! It seems complicated but it really only takes 2 minutes. I simply went into a lot of detail to avoid all the questions ;)."
                paragraph "<hr>"
                paragraph "1. In Hubitat open 3 tabs, (a) this one, (b) 'Apps Code', 'Life360 (Connect)' and (c) Logs"
                paragraph "<hr>"
                paragraph "2. In tab (a), Click on the big 'Life360' button<br>3. Enter in your Life360 Username and Password<br>4. You WILL get an error screen like this...<br><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/L360-XMLError.png'><br>5. Highlight and COPY the long code of letters and numbers<br>ie. MWE2YTk3NGQtNGQ0JHF00ODkzL00000N2E2MWU2NzZmMmRm"
                paragraph "<hr>"
                paragraph "6. Change browser tab to (b), Around Line 35 look for 'state.newClientID' and then paste in the long code you copied in the last step.<br>7. Save the app."
                paragraph "<hr>"
                paragraph "8. Change browser tab to (c). Locate 'Life360 (connect)' up top and click on it. Now down below look for this line. ie. ACCESS TOKEN: 735955da-7e31-4cef-81d2-ea100000416<br>9. Hightlight and COPY just the full set of numbers and letters (make sure there is no leading space)"
                paragraph "<hr>"
                paragraph "10. Change browser tab back to (a).<br>11. In the browser url bar look toward the middle and find 'access_token='.<br>12. Double click on the set of numbers and letters that are after that to hightlight it.<br>13. PASTE in the code saved in step 9. and then press 'ENTER' key."
                paragraph "<img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/L360-URL.png'><br><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/L360-URL2.png'>"
                paragraph "<hr>"
                paragraph "14. You should now see the 'You are now connected' screen. <b>Congratulations!</b>"
            }
            section("SHORT Instructions:", hideable: true, hidden: true) {
                paragraph "1. In Hubitat open 3 tabs, (a) this one, (b) 'Apps Code', 'Life360 (Connect)' and (c) Logs"
                paragraph "2. In tab (a), Click the 'Life360' button and follow through the flow until you get the error XML screen."
                paragraph "3. COPY the long code of letters and numbers<br>ie. MWE2YTk3N0648888800ODkzLWJlMzktN2E2MWU2NzZmMmRm"
                paragraph "4. Change browser tab to (b), Around Line 35 look for 'state.newClientID' and then paste in the long code you copied in the last step and 'SAVE' the app"
                paragraph "5. Change browser tab to (c). Look for this line in logs: ACCESS TOKEN: 7352345a-7e31-4cef-81d2-ea10ebfc8123 and COPY just the full set of letters and numbers."
                paragraph "6. Change browser tab back to (a). Replace the access_token= in the URL with the one you copied in the last step and hit 'ENTER'."
                paragraph "7. You should now see the 'You are now connected' screen. <b>Congratulations!</b>"  
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Credentials")) {
    			href url:redirectUrl, style:"embedded", required:false, title:"Life360", description:description
		    }
   	 	}
    }
    else
    {
        newClientID()
        state.life360AccessToken = "${state.newClientID}"
    	testLife360Connection()
    }
}

def receiveToken() {
	state.life360AccessToken = params.access_token
    def hub = location.hubs[0]
    state.hubIP = "${hub.getDataValue("localIP")}"
    def html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=100%">
<title>Life360 to Hubitat Connection</title>
</head>
<body>
		<p align='center'><img src="https://s3.amazonaws.com/smartapp-icons/Partner/life360@2x.png" alt="Life360 icon" /></p>
		<H1 align='center'><b>Your Life360 Account is connected to Hubitat!<b></H1>
		<H3 align='center'><b>Now...<br>1. Go back to your hub<br>2. Click 'Add User App'<br>3. Select 'Life360 (Connect)' again<br>4. Finish setting up Life360 (Connect)!</b></H3>
        <H3 align='center'><a href="http://${state.hubIP}/installedapp/list">CLICK HERE TO GO BACK TO YOUR HUB</a></H3>
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
    return "${getApiServerUrl()}/${hubUID}/apps/${app.id}/receiveToken"
}

def testLife360Connection() {
    if(logEnable) log.debug "In testLife360Connection..."
    if(state.life360AccessToken) {
        if(logEnable) log.debug "In testLife360Connection - Good!"
   		listCircles()
    } else {
        if(logEnable) log.debug "In testLife360Connection - Bad!"
    	authPage()
    }
}

def listCircles() {
    if(logEnable) log.debug "In listCircles..."
    dynamicPage(name: "listCirclesPage", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 (Connect)</h2>", install: true, uninstall: true) {
        display()
	    // get connected to life360 app
    	def urlCircles = "https://api.life360.com/v3/circles.json"
 
    	def resultCircles = null
        if(logEnable) log.debug "AccessToken: ${state.life360AccessToken}"
       
		httpGet(uri: urlCircles, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
    	     resultCircles = response
		}

		if(logEnable) log.debug "Circles: ${resultCircles.data}"
    	def circles = resultCircles.data.circles
    
        if(logEnable) log.debug "In listCircles - Num of Circles: ${circles.size}"
    	if (circles.size > 1) {
    	    return (
                section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Circle")) {
        			input "circle", "enum", multiple: false, required:true, title:"Life360 Circle", options: circles.collectEntries{[it.id, it.name]}, submitOnChange: true	
        		}
	        )
    	} else {
            log.debug "In listCircles - Num of Circles: ${circles.size} - Circle 0: ${circles[0].id}"
       	    state.circle = circles[0].id
            state.circleName = circles[0].name
            if(logEnable) log.debug "In listCircles - Only have ${circles.size} circle, selecting ${state.circleName} (id:${state.circle}) and moving on"
       	    section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Circle")) {
        		paragraph "<b>selected:</b> ${state.circleName}"
        	}
    	}
        if (!state?.circle) state.circle = settings.circle
        if(state.circle) {
            if(logEnable) log.debug "In listPlaces..."
	        // call life360 and get the list of places in the circle
 	        def urlPlaces = "https://api.life360.com/v3/circles/${state.circle}/places.json"
            def resultPlaces = null
       
	        httpGet(uri: urlPlaces, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
     	        resultPlaces = response
	        }

	        if(logEnable) log.debug "Places: ${resultPlaces.data}" 
            def places = resultPlaces.data.places
            state.places = places
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Location")) {
               paragraph "Please select the ONE Life360 Place that matches your Hubitat location: ${location.name}"
               paragraph "<small>NOTE: Most of the time, this will be called 'Home'</small>"
               input "place", "enum", multiple: false, required:true, title:"Life360 Place", options: places.collectEntries{[it.id, it.name]}, submitOnChange: true
            }
        }
        if(place) {
            if(logEnable) log.debug "In listUsers..."
	        if (!state?.circle) state.circle = settings.circle

            // call life360 and get list of users (members)
            def urlMembers = "https://api.life360.com/v3/circles/${state.circle}/members.json"
            def resultMembers = null
       
	        httpGet(uri: urlMembers, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response -> 
         	    resultMembers = response
	        }

	        if(logEnable) log.debug "Members: ${resultMembers.data}"
        
            // save members list for later
            def members = resultMembers.data.members
            state.members = members
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 User")) {
                paragraph "<small>NOTE: Remember to remove users from this list before removing app.</small>"
                input "users", "enum", multiple: true, required: false, title:"Select Life360 Users to Import into Hubitat", options: members.collectEntries{[it.id, it.firstName+" "+it.lastName]}, submitOnChange: true
            }
            section("Other Options") {
        	    input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
            }
        }
        display2()
    }
}

def installed() {
    if(logEnable) log.debug "In installed..."
	if(!state?.circle) state.circle = settings.circle
    
    settings.users.each {memberId->
    	// log.debug "Find by Member Id = ${memberId}"
    	def member = state.members.find{it.id==memberId}
        // if(logEnable) log.debug "After Find Attempt.
       	// if(logEnable) log.debug "Member Id = ${member.id}, Name = ${member.firstName} ${member.lastName}, Email Address = ${member.loginEmail}"
        // if(logEnable) log.debug "External Id=${app.id}:${member.id}"
       	// create the device
        if(member) {
       		def childDevice = addChildDevice("tmleafs", "Life360 User", "${app.id}.${member.id}",null,[name:member.firstName, completedSetup: true])
    	}
    }
    refresh()
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
