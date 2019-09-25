/**
 *  ****************  Hub Watchdog Driver  ****************
 *
 *  Design Usage:
 *  This driver formats data to be displayed on Hubitat's Dashboards.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
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
 *  Changes:
 *
 *  V1.0.3 - 09/25/19 - More tweaks
 *  V1.0.2 - 09/25/19 - Attempt to fix a null object error
 *  V1.0.1 - 09/25/19 - Added a lot of data points
 *  V1.0.0 - 09/24/19 - Initial release
 */
    
def setVersion(){
    appName = "HubWatchdogDriver"
	version = "v1.0.3" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Hub Watchdog Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Hub%20Watchdog/HW-driver.groovy") {
   		capability "Initialize"
		capability "Actuator"
        capability "Switch"
		
        attribute "readings1", "string"
    	attribute "dataPoints1", "string"
        attribute "dataPoints2", "string"
        attribute "dataPoints3", "string"
		attribute "lastDataPoint1", "string"
        attribute "dataPointsB", "string"
        attribute "numOfCharacters1", "string"
        attribute "numOfCharacters2", "string"
        attribute "numOfCharacters3", "string"
        attribute "numOfCharactersB", "string"
        attribute "maxDelay", "number"
        attribute "readingsSize1", "number"
        attribute "listSizeB", "number"
        
        attribute "meanD", "number"
        attribute "midNumberD", "number"
        attribute "medianD", "number"
        attribute "minimumD", "number"
        attribute "maximumD", "number"
        
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
        
        command "dataPoint1"
        command "on"
        command "off"
        command "clearData1"
        command "maxDelay"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "12")
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
			input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def dataPoint1(message) {
    if(logEnable) log.trace "In dataPoint1 - Received: ${message}"
    theMessage = message
    sendEvent(name: "lastDataPoint1", value: theMessage)
    makeList(theMessage)
}

def maxDelay(message) {
    if(logEnable) log.trace "In maxDelay - Received: ${message}"
    if(message) maxDelay = message.toFloat()
    sendEvent(name: "maxDelay", value: maxDelay)
}

def makeList(theMessage) {
    if(logEnable) log.trace "In makeList - working on ${theMessage}"
    def maxDelay = device.currentValue('maxDelay')
    
        try {
            if(state.readings1 == null) state.readings1 = []
            state.readings1.add(0,theMessage) 
            
            if(state.readings1) readingsSize1 = state.readings1.size()
            if(readingsSize1 > 30) state.readings1.removeAt(30)
            
            getDateTime()
     
            // Over Max Delay Threshold
            if(logEnable) log.trace "In makeList - Checking if ${theMessage} >= ${maxDelay}"
            if(theMessage >= maxDelay) {
                if(logEnable) log.trace "In makeList - maxDelay was greater than the threshold!"
                nMessageB = newdate + " - " + theMessage
                if(state.listB == null) state.listB = []
                state.listB.add(0,nMessageB)  

                if(state.listB) {
                    listSizeB = state.listB.size()
                } else {
                    listSizeB = 0
                }
                if(listSizeB > 10) state.listB.removeAt(10)

                String resultB = state.listB.join(";")
                def linesB = resultB.split(";")
            
                if(logEnable) log.trace "In makeList - Making theDataB List"
                theDataB = "<table><tr><td><div style='font-size:${fontSize}px'>"
                if(listSizeB >= 1) theDataB += "${linesB[0]}<br>"
                if(listSizeB >= 2) theDataB += "${linesB[1]}<br>"
                if(listSizeB >= 3) theDataB += "${linesB[2]}<br>"
                if(listSizeB >= 4) theDataB += "${linesB[3]}<br>"
                if(listSizeB >= 5) theDataB += "${linesB[4]}<br>"
                if(listSizeB >= 6) theDataB += "${linesB[5]}<br>"
                if(listSizeB >= 7) theDataB += "${linesB[6]}<br>"
                if(listSizeB >= 8) theDataB += "${linesB[7]}<br>"
                if(listSizeB >= 9) theDataB += "${linesB[8]}<br>"
                if(listSizeB >= 10) theDataB += "${linesB[9]}<br>"
                theDataB += "</div></td></tr></table>"
                
                dataCharCountB = theDataB.length()
	            if(dataCharCountB <= 1024) {
	        	    if(logEnable) log.debug "Hub Watchdog Driver - dataPointsB - ${dataCharCountB} Characters"
	            } else {
                    theDataB = "Too many characters to display on Dashboard (${dataCharCountB})"
	            }
            }
            // End Over Max Delay
            
	        // Full List
            nMessage1 = newdate + " - " + theMessage
            if(state.list1 == null) state.list1 = []
            state.list1.add(0,nMessage1)  

            if(state.list1) {
                listSize1 = state.list1.size()
            } else {
                listSize1 = 0
            }
            
            if(listSize1 > 30) state.list1.removeAt(30)

            String result1 = state.list1.join(";")
            def lines1 = result1.split(";")
            
            if(logEnable) log.trace "In makeList - All"
            theData1 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 1) theData1 += "${lines1[0]}<br>"
            if(listSize1 >= 2) theData1 += "${lines1[1]}<br>"
            if(listSize1 >= 3) theData1 += "${lines1[2]}<br>"
            if(listSize1 >= 4) theData1 += "${lines1[3]}<br>"
            if(listSize1 >= 5) theData1 += "${lines1[4]}<br>"
            if(listSize1 >= 6) theData1 += "${lines1[5]}<br>"
            if(listSize1 >= 7) theData1 += "${lines1[6]}<br>"
            if(listSize1 >= 8) theData1 += "${lines1[7]}<br>"
            if(listSize1 >= 9) theData1 += "${lines1[8]}<br>"
            if(listSize1 >= 10) theData1 += "${lines1[9]}<br>"
            theData1 += "</div></td></tr></table>"
            
            dataCharCount1 = theData1.length()
	        if(dataCharCount1 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints1 - ${dataCharCount1} Characters"
	        } else {
                theData1 = "Too many characters to display on Dashboard (${dataCharCount1})"
	        }
            
            theData2 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 11) theData2 += "${lines1[10]}<br>"
            if(listSize1 >= 12) theData2 += "${lines1[11]}<br>"
            if(listSize1 >= 13) theData2 += "${lines1[12]}<br>"
            if(listSize1 >= 14) theData2 += "${lines1[13]}<br>"
            if(listSize1 >= 15) theData2 += "${lines1[14]}<br>"
            if(listSize1 >= 16) theData2 += "${lines1[15]}<br>"
            if(listSize1 >= 17) theData2 += "${lines1[16]}<br>"
            if(listSize1 >= 18) theData2 += "${lines1[17]}<br>"
            if(listSize1 >= 19) theData2 += "${lines1[18]}<br>"
            if(listSize1 >= 20) theData2 += "${lines1[19]}"
            theData2 += "</div></td></tr></table>"
    
            dataCharCount2 = theData2.length()
	        if(dataCharCount2 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints2 - ${dataCharCount2} Characters"
	        } else {
                theData2 = "Too many characters to display on Dashboard (${dataCharCount2})"
	        }
            
            theData3 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 21) theData3 += "${lines1[20]}<br>"
            if(listSize1 >= 22) theData3 += "${lines1[21]}<br>"
            if(listSize1 >= 23) theData3 += "${lines1[22]}<br>"
            if(listSize1 >= 24) theData3 += "${lines1[23]}<br>"
            if(listSize1 >= 25) theData3 += "${lines1[24]}<br>"
            if(listSize1 >= 26) theData3 += "${lines1[25]}<br>"
            if(listSize1 >= 27) theData3 += "${lines1[26]}<br>"
            if(listSize1 >= 28) theData3 += "${lines1[27]}<br>"
            if(listSize1 >= 29) theData3 += "${lines1[28]}<br>"
            if(listSize1 >= 30) theData3 += "${lines1[29]}"
            theData3 += "</div></td></tr></table>"
    
            dataCharCount3 = theData3.length()
	        if(dataCharCount3 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints3 - ${dataCharCount3} Characters"
	        } else {
                theData3 = "Too many characters to display on Dashboard (${dataCharCount3})"
	        }
            // End Full List
            
	        sendEvent(name: "dataPoints1", value: theData1, displayed: true)
            sendEvent(name: "numOfCharacters1", value: dataCharCount1, displayed: true)
            
            sendEvent(name: "dataPoints2", value: theData2, displayed: true)
            sendEvent(name: "numOfCharacters2", value: dataCharCount2, displayed: true)
            
            sendEvent(name: "dataPoints3", value: theData3, displayed: true)
            sendEvent(name: "numOfCharacters3", value: dataCharCount3, displayed: true)
            
            sendEvent(name: "readings1", value: state.readings1, displayed: true)
            sendEvent(name: "dataPointsB", value: theDataB, displayed: true)
            
            
            // Lets make sure Data
            if(logEnable) log.debug "Hub Watchdog Driver - Lets Make Some Data"            
            
    //  ** From https://www.javaworld.com/article/2073174/groovy--means--medians--modes--and-ranges-calculations.html            
            numberItems = state.readings1.size()
            sum = 0
            modeMap = new HashMap<BigDecimal, Integer>()
            for (item in state.readings1) {
               sum += item
               if (modeMap.get(item) == null) {
                   modeMap.put(item, 1)
               } else {
                  count = modeMap.get(item) + 1
                  modeMap.put(item, count)
               }
            }
            mode = new ArrayList<Integer>()
            modeCount = 0
            modeMap.each() { key, value -> 
               if (value > modeCount) { mode.clear(); mode.add(key); modeCount = value}
               else if (value == modeCount) { mode.add(key) }
            }
            sumDelayRecorded = sum 
            mean = sum / numberItems
            midNumber = (int)(numberItems/2)
            median = numberItems %2 != 0 ? state.readings1[midNumber] : (state.readings1[midNumber] + state.readings1[midNumber-1])/2 
            minimum = Collections.min(state.readings1)
            maximum = Collections.max(state.readings1)
    // *** end From            

            if(state.readings1) readingsSize1 = state.readings1.size()
            if(state.listB) {
                listSizeB = state.listB.size()
            } else {
                listSizeB = 0
            }
            
            sendEvent(name: "readingsSize1", value: readingsSize1, displayed: true)
            sendEvent(name: "listSizeB", value: listSizeB, displayed: true)
            
            sendEvent(name: "meanD", value: mean, displayed: true)
            sendEvent(name: "midNumberD", value: midNumber, displayed: true)
            sendEvent(name: "medianD", value: median, displayed: true)
            sendEvent(name: "minimumD", value: minimum, displayed: true)
            sendEvent(name: "maximumD", value: maximum, displayed: true)
            
        }
        catch(e1) {
            log.error "${e1}"
        }
}

def installed(){
    log.info "Hub Watchdog Driver has been Installed"
}

def updated() {
    log.info "Hub Watchdog Driver has been Updated"
}

def initialize() {
    log.info "In initialize"
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newdate=date.format("MM-d HH:mm")
	if(hourType == true) newdate=date.format("MM-d hh:mm")
    return newdate
}

def on() {
    sendEvent(name: "switch", value: on, isStateChange: true)
}

def off() {
    sendEvent(name: "switch", value: off, isStateChange: true)
}

def clearData1() {
    state.readings1 = []
    state.list1 = []
    state.listB = []
    
    sendEvent(name: "dataPoints1", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters1", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints2", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters2", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints3", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters3", value: 0, displayed: true)
    
    sendEvent(name: "dataPointsB", value: "-", displayed: true)
    sendEvent(name: "numOfCharactersB", value: 0, displayed: true)
    
    sendEvent(name: "readings1", value: state.readings1, displayed: true)
    sendEvent(name: "readingsSize1", value: 0, displayed: true)
    sendEvent(name: "listSizeB", value: 0, displayed: true)
    sendEvent(name: "listSize1", value: 0, displayed: true)
}
