/**
 *  ---- Original Header ----
 *
 *  Life360 with States - Hubitat Port
 *
 *  BTRIAL DISTANCE AND SLEEP PATCH 29-12-2017
 *  Updated Code to handle distance from, and sleep functionality
 *
 *  TMLEAFS REFRESH PATCH 06-12-2016 V1.1
 *  Updated Code to match Smartthings updates 12-05-2017 V1.2
 *  Added updateMember function that pulls all usefull information Life360 provides for webCoRE use V2.0
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
 *  Special thanks goes out to @cwwilson08 for working on and figuring out the oauth stuff!
 *  This would not be possible without his work.
 *
 *  Changes:
 *
 *  2.6.0 - 01/07/21 - Interim release reintegrating push notifications with a new POST request
                       to force location name update from real-time servers *  2.5.5 - 12/20/20 - Reliability Improvements:
                     - Added a 1 sec delay after push event to let data packet catch-up
                     - Cleaned up Scheduling
                     - Cleaned up Logging
 *  2.5.3 - 12/17/20 - Fixed a logging issue
 *  2.5.2 - 12/17/20 - 30 second refresh intervals
                       To-Do: Provide a user preference field in app for refresh intervals
 *  2.5.1 - 12/11/20 - Resubscribe to notifications on Update() event
 *  2.5.0 - 12/06/20 - Moved all member location functionality to Location Tracker child Driver
                       Keeping only Circle level functionality at parent app level
 *  ---
 *  v1.0.0 - 06/30/19 - Initial port of ST app (cwwilson08) (bptworld)
 */

import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Life360 with States"
    state.version = "2.6.0"
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
        }
        if (result.data.access_token) {
           state.life360AccessToken = result.data.access_token
            return true;
         }
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
    dynamicPage(name: "listCirclesPage", title: "", install: true, uninstall: true) {
        display()

      if(testLife360Connection()) {
          def urlCircles = "https://api.life360.com/v3/circles.json"
          def resultCircles = null

        httpGet(uri: urlCircles, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response ->
               resultCircles = response
        }

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
            if(logEnable) log.trace "In listPlaces - (${state.version})"
            if (app.installationState == "COMPLETE") uninstallOption = true

            if (!state?.circle) state.circle = settings.circle

            def url = "https://api.life360.com/v3/circles/${state.circle}/places.json"
            def result = null

            httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response ->
               result = response
            }

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
            if(logEnable) log.trace "In listUsers - (${state.version})"
            if (app.installationState == "COMPLETE") uninstallOption = true
            if (!state?.circle) state.circle = settings.circle

            def url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
            def result = null

            httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response ->
               result = response
            }

            def members = result.data.members
            state.members = members

            section(getFormat("header-green", "${getImage("Blank")}"+" Select Life360 Members to Import into Hubitat")) {
                theMembers = members.collectEntries{[it.id, it.firstName+" "+it.lastName]}
                sortedMembers = theMembers.sort { a, b -> a.value <=> b.value }
              input "users", "enum", multiple: true, required:false, title:"Life360 Members: ", options: sortedMembers, submitOnChange: true
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
          input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
          }
            display2()
        }
    }
}

def installed() {
    if(logEnable) log.trace "In installed - (${state.version})"
    if(!state?.circle) state.circle = settings.circle

    settings.users.each {memberId->
        def member = state.members.find{it.id==memberId}
        if(member) {
            // Modified from @Stephack
            def childDevice = childList()
            if(childDevice.find{it.data.vcId == "${member}"}){
                if(logEnable) log.info "${member.firstName} already exists...skipping"
            } else {
                if(logEnable) log.info "Creating Life360 Device: " + member
                try{
                    addChildDevice("BPTWorld", "Location Tracker User Driver", "${app.id}.${member.id}", 1234, ["name": "Life360 - ${member.firstName}", isComponent: false])
                }
                catch (e) {
                    log.error "Child device creation failed with error = ${e}"
                }
            }
            // end mod

            if (childDevice) {
                if(logEnable) log.info "Child Device Successfully Created"
            }
        }
    }
    createCircleSubscription()
}

def createCircleSubscription() {
    if(logEnable) log.trace "In createCircleSubscription - (${state.version})"
    if(logEnable) log.info "Remove any existing Life360 Webhooks for this Circle."

    def deleteUrl = "https://api.life360.com/v3/circles/${state.circle}/webhook.json"
    try { // ignore any errors - there many not be any existing webhooks

        httpDelete (uri: deleteUrl, headers: ["Authorization": "Bearer ${state.life360AccessToken}" ]) {response ->
            result = response}
    }

    catch (e) {
        log.debug (e)
    }

    // subscribe to the life360 webhook to get push notifications on place events within this circle

    if(logEnable) log.info "Create a new Life360 Webhooks for this Circle."
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

    if (result.data?.hookUrl) {
        log.info "Successfully subscribed to Life360 circle push events"
        if(logEnable) log.debug "Confirmation: ${result.data?.hookUrl}"
        updateMembers()
        scheduleUpdates()
    }

}

def updated() {
    if(logEnable) log.trace "In updated - (${state.version})"
    if (!state?.circle) { state.circle = settings.circle }

    settings.users.each {memberId->
        def externalId = "${app.id}.${memberId}"
        def deviceWrapper = getChildDevice("${externalId}")

        if (!deviceWrapper) { // device isn't there - so we need to create
            member = state.members.find{it.id==memberId}
            // Modified from @Stephack
            def childDevice = childList()
            if(childDevice.find{it.data.vcId == "${member}"}){
                if(logEnable) log.info "${member.firstName} already exists...skipping"
            } else {
                if(logEnable) log.info "Creating Life360 Device: " + member
                try{
                    addChildDevice("BPTWorld", "Location Tracker User Driver", "${app.id}.${member.id}", 1234, ["name": "Life360 - ${member.firstName}", isComponent: false])
                }
                catch (e) {
                    log.error "Child device creation failed with error = ${e}"
                }
            }
            // end mod

            if (childDevice) {
                if(logEnable) log.info "Child Device Successfully Created"
                createCircleSubscription()
            }
        }
    }

    def childDevices = childList()
    if(logEnable) log.debug "Child Devices: ${childDevices}"
    childDevices.each {childDevice->
        def (childAppName, childMemberId) = childDevice.deviceNetworkId.split("\\.")
        if (!settings.users.find{it==childMemberId}) {
            deleteChildDevice(childDevice.deviceNetworkId)
            def member = state.members.find {it.id==memberId}
            if (member) state.members.remove(member)
        }
    }
    updateMembers()
    // Since we updated the app, make sure we also reschedule the updateMembers function
    scheduleUpdates()
}

def initialize() {
    // TODO: subscribe to attributes, devices, locations, etc.
}

def placeEventHandler() {
  if(logEnable) log.debug "Life360 placeEventHandler: Received Life360 Push Event - Updating Members Location Status..."

  def circleId = params?.circleId
  def placeId = params?.placeId
  def memberId = params?.userId
  def direction = params?.direction
  def timestamp = params?.timestamp
  def requestId = null
  def requestResult = null
  def isPollable = null

  def externalId = "${app.id}.${memberId}"
  def deviceWrapper = getChildDevice("${externalId}")

// Following commented code is still work in progress and has not been completed yet...
/*
  if (deviceWrapper) {
    deviceWrapper.updateMemberPresence(params)
      if(logEnable) log.debug "Life360 place presence event raised on child device: ${externalId}"
  }
    else {
      log.warn "Life360 couldn't find child device associated with inbound Life360 event."
    }
*/

  if(logEnable) log.trace "In placeHandler - about to post request update..."
  def postUrl = "https://api.life360.com/v3/circles/${circleId}/members/${memberId}/request.json"
  requestResult = null

  // once a push event is received, we can force a real-time update of location data via issuing the following
  // post request against the member for which the push event was received
      try {

           httpPost(uri: postUrl, body: ["type": "location"], headers: ["Authorization": "Bearer ${state.life360AccessToken}"]) {response ->
               requestResult = response
           }
               requestId = requestResult.data?.requestId
               isPollable = requestResult.data?.isPollable
               if(logEnable) log.debug "PlaceHandler Post response = ${requestResult.data}  params direction = $direction"
               if(logEnable) log.debug "PlaceHandler Post requestId = ${requestId} isPollable = $isPollable"

// Following commented code is still work in progress and has not been completed yet...
/*
          if (isPollable = "1") {
              def getUrl = "https://api.life360.com/v3/circles/members/request/${requestId}"
              if(logEnable) log.debug "httpGet for request is = $getUrl"

              requestResult = null

              httpGet(uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}", timeout: 30 ]) {response ->
                 requestResult = response
          }
              if(logEnable) log.debug "after httpGet = result = $requestResult  location should be = ${requestResult.data.location}"
                  def location = result.data.location

                  if(logEnable) log.debug "PlaceHandler get response = ${location}  params.direction = $direction"

          }
*/
  }
      catch (e) {
             log.error "Life360 request post / get, error: $e"
      }

  // we got a PUSH EVENT from Life360 - better update everything by pulling a fresh data packet
  // But first, wait a second to let packet catch-up with push event
  updateMembers()
}


def refresh() {
    listCircles()
    updateMembers()
    scheduleUpdates()
}

def scheduleUpdates() {
    if (logEnable) log.trace "In scheduleUpdates..."
    // Continue to update Info for all members every 30 seconds
    schedule("0/30 * * * * ? *", updateMembers)
}

def updateMembers(){
    if(logEnable) log.trace "In updateMembers - (${state.version})"

    if (!state?.circle) state.circle = settings.circle

    url = "https://api.life360.com/v3/circles/${state.circle}/members.json"
    def result = null
    sendCmd(url, result)
}

def sendCmd(url, result){
    def requestParams = [ uri: url, headers: ["Authorization": "Bearer ${state.life360AccessToken}"], timeout: 10 ]
    asynchttpGet("cmdHandler", requestParams)
}

def cmdHandler(resp, data) {
    if(logEnable) log.trace "In cmdHandler..."
    // Avi - pushed all data straight down to child device for self-containment

    if(resp.getStatus() == 200 || resp.getStatus() == 207) {
        result = resp.getJson()
        def members = result.members
        state.members = members

        // Get a *** sorted *** list of places for easier navigation
        def thePlaces = state.places.sort { a, b -> a.name <=> b.name }.name
        def home = state.places.find{it.id==settings.place}

        // Iterate through each member and trigger an update from payload
        settings.users.each {memberId->
            def externalId = "${app.id}.${memberId}"
            def member = state.members.find{it.id==memberId}
            try {
                // find the appropriate child device based on app id and the device network id
                def deviceWrapper = getChildDevice("${externalId}")

                // send circle places and home to individual children
                deviceWrapper.generatePresenceEvent(member, thePlaces, home)

            } catch(e) {
                if(logEnable) log.debug "In cmdHandler - catch - member: ${member}"
                if(logEnable) log.debug e
            }
        }
    }
}

def uninstalled() {
    removeChildDevices(getChildDevices())
    unschedule()
    unsubscribe()
}

private removeChildDevices(delete) {
    delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def childList() {
    def children = getChildDevices()
    if(logEnable) log.debug "In childList - children: ${children}"
    return children
}

// ********** Normal Stuff **********

def getImage(type) {          // Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {      // Modified from @Stephack Code
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
        paragraph getFormat("line")
    }
}

def display2() {
    section() {
        paragraph getFormat("line")
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
    }
}

def getHeaderAndFooter() {
    def params = [
        uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
        requestContentType: "application/json",
        contentType: "application/json",
        timeout: 30
    ]

    try {
        def result = null
        httpGet(params) { resp ->
            state.headerMessage = resp.data.headerMessage
            state.footerMessage = resp.data.footerMessage
        }
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
