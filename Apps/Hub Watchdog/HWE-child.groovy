/**
 *  ****************  Hub Watchdog Examiner App  ****************
 *
 *  Design Usage:
 *  Simple way to monitor if your hub is slowing down or not.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *-------------------------------------------------------------------------------------------------------------------
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.0 - 09/2296/19 - Initial release.
 *
 */

import hubitat.helper.RMUtils

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "HubWatchdogExaminerChildVersion"
	state.version = "v1.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Hub Watchdog Examiner Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Simple way to monitor if your hub is slowing down or not.",
    category: "",
	parent: "BPTWorld:Hub Watchdog",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Hub%20Watchdog/HWE-child.groovy",
)

preferences {
    page name: "pageConfig"
    page name: "reportRawOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
    page name: "reportSummaryOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Hub Watchdog Examiner</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
            paragraph "Simple way to monitor if your hub is slowing down or not."
			paragraph "<b>Notes:</b>"
			paragraph "- You can use any type of 'switched' device you want to test. Virtual, Zwave or Zigbee<br>- Remember, any device you use will turn off after 5 seconds to test.<br>- Best to use an extra plugin module for testing."
		}  
        section(getFormat("header-green", "${getImage("Blank")}"+" Data Devices")) {
			input(name: "getDevice1", type: "capability.actuator", title: "Device 1 to compare:", submitOnChange: true)
            input(name: "getDevice2", type: "capability.actuator", title: "Device 2 to compare:", submitOnChange: true)
            input(name: "getDevice3", type: "capability.actuator", title: "Device 3 to compare:", submitOnChange: true)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Color Coding the Raw Data")) {
            input "colorZwav", "text", title: "Color code data Zwav", submitOnChange: true, width: 6
            input "colorZigb", "text", title: "Color code data Zigb", submitOnChange: true, width: 6
            input "colorVirt", "text", title: "Color code data Virt", submitOnChange: true, width: 6
            input "colorOther", "text", title: "Color code data Other", submitOnChange: true, width: 6
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			input "testBtn1", "button", title: "Collect Summary Data Now", width: 6
            input "testBtn2", "button", title: "Collect Raw Data Now", width: 6
            href "reportSummaryOptions", title: "Summary Report", description: "Click here to view the Summary Report."
            href "reportRawOptions", title: "Raw Data Report", description: "Click here to view the Raw Data Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "debugging", submitOnChange: true)
		}
		display2()
	}
}

def getSummaryData(){
    if(logEnable) log.debug "In getDataSummary (${state.version})"
    if(getDevice1) {
        meanD1 = getDevice1.currentValue("meanD")
        medianD1 = getDevice1.currentValue("medianD")
        minimumD1 = getDevice1.currentValue("minimumD")
        maximumD1 = getDevice1.currentValue("maximumD")
        readingsSize1 = getDevice1.currentValue("readingsSize1")
        listSizeB1 = getDevice1.currentValue("listSizeB")
        listSizeW1 = getDevice1.currentValue("listSizeW")
        maxDelay1 = getDevice1.currentValue("maxDelay")
        warnValue1 = getDevice1.currentValue("warnValue")
    }
    if(getDevice2) {
        meanD2 = getDevice2.currentValue("meanD")
        medianD2 = getDevice2.currentValue("medianD")
        minimumD2 = getDevice2.currentValue("minimumD")
        maximumD2 = getDevice2.currentValue("maximumD")
        readingsSize2 = getDevice2.currentValue("readingsSize1")
        listSizeB2 = getDevice2.currentValue("listSizeB")
        listSizeW2 = getDevice2.currentValue("listSizeW")
        maxDelay2 = getDevice2.currentValue("maxDelay")
        warnValue2 = getDevice2.currentValue("warnValue")
    }
    if(getDevice3) {
        meanD3 = getDevice3.currentValue("meanD")
        medianD3 = getDevice3.currentValue("medianD")
        minimumD3 = getDevice3.currentValue("minimumD")
        maximumD3 = getDevice3.currentValue("maximumD")
        readingsSize3 = getDevice3.currentValue("readingsSize1")
        listSizeB3 = getDevice3.currentValue("listSizeB")
        listSizeW3 = getDevice3.currentValue("listSizeW")
        maxDelay3 = getDevice3.currentValue("maxDelay")
        warnValue3 = getDevice3.currentValue("warnValue")
    }

    state.reportStats1 = "<table width='100%'><tr><td><b>${getDevice1}</b></td></tr><tr><td> </td></tr>"
    state.reportStats1 += "<tr><td>Number of Data Points: ${readingsSize1}<br>Over Max Threshold: ${listSizeB1}<br>Over Warning Threshold: ${listSizeW1}<br>Current Max Delay: ${maxDelay1}<br>Current Warning Delay: ${warnValue1}</td></tr><tr><td> </td></tr>"
    state.reportStats1 += "<tr><td>Mean Delay: ${meanD1}<br>Median Delay: ${medianD1}<br>Minimum Delay: ${minimumD1}<br>Maximum Delay: ${maximumD1}</td></tr></table>"  
    
    state.reportStats2 = "<table width='100%'><tr><td><b>${getDevice2}</b></td></tr><tr><td> </td></tr>"
    state.reportStats2 += "<tr><td>Number of Data Points: ${readingsSize2}<br>Over Max Threshold: ${listSizeB2}<br>Over Warning Threshold: ${listSizeW2}<br>Current Max Delay: ${maxDelay2}<br>Current Warning Delay: ${warnValue2}</td></tr><tr><td> </td></tr>"
    state.reportStats2 += "<tr><td>Mean Delay: ${meanD2}<br>Median Delay: ${medianD2}<br>Minimum Delay: ${minimumD2}<br>Maximum Delay: ${maximumD2}</td></tr></table>"
    
    state.reportStats3 = "<table width='100%'><tr><td><b>${getDevice3}</b></td></tr><tr><td> </td></tr>"
    state.reportStats3 += "<tr><td>Number of Data Points: ${readingsSize3}<br>Over Max Threshold: ${listSizeB3}<br>Over Warning Threshold: ${listSizeW3}<br>Current Max Delay: ${maxDelay3}<br>Current Warning Delay: ${warnValue3}</td></tr><tr><td> </td></tr>"
    state.reportStats3 += "<tr><td>Mean Delay: ${meanD3}<br>Median Delay: ${medianD3}<br>Minimum Delay: ${minimumD3}<br>Maximum Delay: ${maximumD3}</td></tr></table>"
}

def getRawData(){
    if(logEnable) log.debug "In getTheData (${state.version})"
    if(getDevice1) deviceData1 = getDevice1.currentValue("list1")
    if(getDevice2) deviceData2 = getDevice2.currentValue("list1")
    if(getDevice3) deviceData3 = getDevice3.currentValue("list1")
    
    if(deviceData1) deviceD1 = deviceData1.replace("["," ").replace("]","")
    if(deviceData2) deviceD2 = deviceData2.replace("["," ").replace("]","")
    if(deviceData3) deviceD3 = deviceData3.replace("["," ").replace("]","")
    state.deviceData = [deviceD1, deviceD2, deviceD3].flatten().findAll{it}
}

def styleHandler(data){
    if(logEnable) log.debug "In styleHandler (${state.version})"
    if(data.contains(" - Zwav")) {
        strippedData = data.replace(" - Zwav","")
        def (dataZw1, dataZw2) = strippedData.split(" - ")
        colorData = "<span style='color:${colorZwav}'>${dataZw1}</span> - ${dataZw2}"
        //if(logEnable) log.debug "In styleHandler (${state.version}) - Zwav - colorData: ${colorData}"
        return colorData
    }
    if(data.contains(" - Zigb")) {
        strippedData = data.replace(" - Zigb","")
        def (dataZb1, dataZb2) = strippedData.split(" - ")
        colorData = "<span style='color:${colorZigb}'>${dataZb1}</span> - ${dataZb2}"
        //if(logEnable) log.debug "In styleHandler (${state.version}) - ZigB - colorData: ${colorData}"
        return colorData
    }
    if(data.contains(" - Virt")) {
        strippedData = data.replace(" - Virt","")
        def (dataV1, dataV2) = strippedData.split(" - ")
        colorData = "<span style='color:${colorVirt}'>${dataV1}</span> - ${dataV2}"
        //if(logEnable) log.debug "In styleHandler (${state.version}) - Virt - colorData: ${colorData}"
        return colorData
    }
    if(data.contains(" - Other")) {
        strippedData = data.replace(" - Other","")
        def (dataO1, dataO2) = strippedData.split(" - ")
        colorData = "<span style='color:${colorVirt}'>${dataO1}</span> - ${dataO2}"
        //if(logEnable) log.debug "In styleHandler (${state.version}) - Virt - colorData: ${colorData}"
        return colorData
    }
}
    
def reportRawOptions(){
    dynamicPage(name: "reportRawOptions", title: "Report Raw Data", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Report Raw Data")) {
            if(colorVirt == null) colorVirt = " - "
            if(colorZwav == null) colorZwav = " - "
            if(colorZigb == null) colorZigb = " - "
            if(colorOther == null) colorOther = " - "
            
            paragraph "Date Color Codes - Virtual: <span style='color:${colorVirt}'>${colorVirt}</span>, Zwave: <span style='color:${colorZwav}'>${colorZwav}</span>, Zigbee: <span style='color:${colorZigb}'>${colorZigb}</span>, Other: <span style='color:${colorOther}'>${colorOther}</span>"
            
            String result1 = state.deviceData.join(",")
            def data = result1.split(",")
            dataS = data.sort { a, b -> b <=> a }
            if(dataS) {
                dataSize1 = dataS.size()
            } else {
                dataSize1 = 0
            }
            
            if(logEnable) log.debug "In reportOptions - dataSize1: ${dataSize1} - dataS: ${dataS}"
            
            if(dataSize1 >= 1) { styleHandler(dataS[0]); theDataPoints1 = "${colorData}<br>" }
            if(dataSize1 >= 2) { styleHandler(dataS[1]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 3) { styleHandler(dataS[2]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 4) { styleHandler(dataS[3]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 5) { styleHandler(dataS[4]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 6) { styleHandler(dataS[5]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 7) { styleHandler(dataS[6]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 8) { styleHandler(dataS[7]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 9) { styleHandler(dataS[8]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 10) { styleHandler(dataS[9]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 11) { styleHandler(dataS[10]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 12) { styleHandler(dataS[11]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 13) { styleHandler(dataS[12]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 14) { styleHandler(dataS[13]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 15) { styleHandler(dataS[14]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 16) { styleHandler(dataS[15]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 17) { styleHandler(dataS[16]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 18) { styleHandler(dataS[17]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 19) { styleHandler(dataS[18]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 20) { styleHandler(dataS[19]); theDataPoints1 += "${colorData}<br>" }   
            if(dataSize1 >= 21) { styleHandler(dataS[20]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 22) { styleHandler(dataS[21]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 23) { styleHandler(dataS[22]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 24) { styleHandler(dataS[23]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 25) { styleHandler(dataS[24]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 26) { styleHandler(dataS[25]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 27) { styleHandler(dataS[26]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 28) { styleHandler(dataS[27]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 29) { styleHandler(dataS[28]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 30) { styleHandler(dataS[29]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 31) { styleHandler(dataS[30]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 32) { styleHandler(dataS[31]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 33) { styleHandler(dataS[32]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 34) { styleHandler(dataS[33]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 35) { styleHandler(dataS[34]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 36) { styleHandler(dataS[35]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 37) { styleHandler(dataS[36]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 38) { styleHandler(dataS[37]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 39) { styleHandler(dataS[38]); theDataPoints1 += "${colorData}<br>" }
            if(dataSize1 >= 40) { styleHandler(dataS[39]); theDataPoints1 += "${colorData}<br>" }
            
            if(dataSize1 >= 41) { styleHandler(dataS[40]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 42) { styleHandler(dataS[41]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 43) { styleHandler(dataS[42]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 44) { styleHandler(dataS[43]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 45) { styleHandler(dataS[44]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 46) { styleHandler(dataS[45]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 47) { styleHandler(dataS[46]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 48) { styleHandler(dataS[47]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 49) { styleHandler(dataS[48]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 50) { styleHandler(dataS[49]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 51) { styleHandler(dataS[50]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 52) { styleHandler(dataS[51]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 53) { styleHandler(dataS[52]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 54) { styleHandler(dataS[53]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 55) { styleHandler(dataS[54]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 56) { styleHandler(dataS[55]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 57) { styleHandler(dataS[56]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 58) { styleHandler(dataS[57]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 59) { styleHandler(dataS[58]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 60) { styleHandler(dataS[59]); theDataPoints2 += "${colorData}<br>" }            
            if(dataSize1 >= 61) { styleHandler(dataS[60]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 62) { styleHandler(dataS[61]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 63) { styleHandler(dataS[62]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 64) { styleHandler(dataS[63]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 65) { styleHandler(dataS[64]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 66) { styleHandler(dataS[65]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 67) { styleHandler(dataS[66]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 68) { styleHandler(dataS[67]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 69) { styleHandler(dataS[68]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 70) { styleHandler(dataS[69]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 71) { styleHandler(dataS[60]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 72) { styleHandler(dataS[71]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 73) { styleHandler(dataS[72]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 74) { styleHandler(dataS[73]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 75) { styleHandler(dataS[74]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 76) { styleHandler(dataS[75]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 77) { styleHandler(dataS[76]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 78) { styleHandler(dataS[77]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 79) { styleHandler(dataS[78]); theDataPoints2 += "${colorData}<br>" }
            if(dataSize1 >= 80) { styleHandler(dataS[79]); theDataPoints2 += "${colorData}<br>" }
            
            if(dataSize1 >= 81) { styleHandler(dataS[80]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 82) { styleHandler(dataS[81]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 83) { styleHandler(dataS[82]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 84) { styleHandler(dataS[83]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 85) { styleHandler(dataS[84]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 86) { styleHandler(dataS[85]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 87) { styleHandler(dataS[86]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 88) { styleHandler(dataS[87]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 89) { styleHandler(dataS[88]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 90) { styleHandler(dataS[89]); theDataPoints3 += "${colorData}<br>" } 
            if(dataSize1 >= 91) { styleHandler(dataS[90]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 92) { styleHandler(dataS[91]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 93) { styleHandler(dataS[92]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 94) { styleHandler(dataS[93]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 95) { styleHandler(dataS[94]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 96) { styleHandler(dataS[95]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 97) { styleHandler(dataS[96]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 98) { styleHandler(dataS[97]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 99) { styleHandler(dataS[98]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 100) { styleHandler(dataS[99]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 101) { styleHandler(dataS[100]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 102) { styleHandler(dataS[101]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 103) { styleHandler(dataS[102]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 104) { styleHandler(dataS[103]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 105) { styleHandler(dataS[104]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 106) { styleHandler(dataS[105]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 107) { styleHandler(dataS[106]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 108) { styleHandler(dataS[107]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 109) { styleHandler(dataS[108]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 110) { styleHandler(dataS[109]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 111) { styleHandler(dataS[110]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 112) { styleHandler(dataS[111]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 113) { styleHandler(dataS[112]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 114) { styleHandler(dataS[113]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 115) { styleHandler(dataS[114]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 116) { styleHandler(dataS[115]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 117) { styleHandler(dataS[116]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 118) { styleHandler(dataS[117]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 119) { styleHandler(dataS[118]); theDataPoints3 += "${colorData}<br>" }
            if(dataSize1 >= 120) { styleHandler(dataS[119]); theDataPoints3 += "${colorData}<br>" }
            
            if(dataSize1 >= 121) { styleHandler(dataS[110]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 122) { styleHandler(dataS[121]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 123) { styleHandler(dataS[122]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 124) { styleHandler(dataS[123]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 125) { styleHandler(dataS[124]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 126) { styleHandler(dataS[125]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 127) { styleHandler(dataS[126]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 128) { styleHandler(dataS[127]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 129) { styleHandler(dataS[128]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 130) { styleHandler(dataS[129]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 131) { styleHandler(dataS[130]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 132) { styleHandler(dataS[131]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 133) { styleHandler(dataS[132]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 134) { styleHandler(dataS[133]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 135) { styleHandler(dataS[134]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 136) { styleHandler(dataS[135]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 137) { styleHandler(dataS[136]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 138) { styleHandler(dataS[137]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 139) { styleHandler(dataS[138]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 140) { styleHandler(dataS[139]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 141) { styleHandler(dataS[140]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 142) { styleHandler(dataS[141]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 143) { styleHandler(dataS[142]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 144) { styleHandler(dataS[143]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 145) { styleHandler(dataS[144]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 146) { styleHandler(dataS[145]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 147) { styleHandler(dataS[146]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 148) { styleHandler(dataS[147]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 149) { styleHandler(dataS[148]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 150) { styleHandler(dataS[149]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 151) { styleHandler(dataS[150]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 152) { styleHandler(dataS[151]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 153) { styleHandler(dataS[152]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 154) { styleHandler(dataS[153]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 155) { styleHandler(dataS[154]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 156) { styleHandler(dataS[155]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 157) { styleHandler(dataS[156]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 158) { styleHandler(dataS[157]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 159) { styleHandler(dataS[158]); theDataPoints4 += "${colorData}<br>" }
            if(dataSize1 >= 160) { styleHandler(dataS[159]); theDataPoints4 += "${colorData}<br>" }
            
            if(dataSize1 >= 161) { styleHandler(dataS[160]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 162) { styleHandler(dataS[161]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 163) { styleHandler(dataS[162]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 164) { styleHandler(dataS[163]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 165) { styleHandler(dataS[164]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 166) { styleHandler(dataS[165]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 167) { styleHandler(dataS[166]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 168) { styleHandler(dataS[167]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 169) { styleHandler(dataS[168]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 170) { styleHandler(dataS[169]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 171) { styleHandler(dataS[170]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 172) { styleHandler(dataS[171]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 173) { styleHandler(dataS[172]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 174) { styleHandler(dataS[173]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 175) { styleHandler(dataS[174]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 176) { styleHandler(dataS[175]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 177) { styleHandler(dataS[176]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 178) { styleHandler(dataS[177]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 179) { styleHandler(dataS[178]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 180) { styleHandler(dataS[179]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 181) { styleHandler(dataS[180]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 182) { styleHandler(dataS[181]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 183) { styleHandler(dataS[182]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 184) { styleHandler(dataS[183]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 185) { styleHandler(dataS[184]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 186) { styleHandler(dataS[185]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 187) { styleHandler(dataS[186]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 188) { styleHandler(dataS[187]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 189) { styleHandler(dataS[188]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 190) { styleHandler(dataS[189]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 191) { styleHandler(dataS[190]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 192) { styleHandler(dataS[191]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 193) { styleHandler(dataS[192]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 194) { styleHandler(dataS[193]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 195) { styleHandler(dataS[194]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 196) { styleHandler(dataS[195]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 197) { styleHandler(dataS[196]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 198) { styleHandler(dataS[197]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 199) { styleHandler(dataS[198]); theDataPoints5 += "${colorData}<br>" }
            if(dataSize1 >= 200) { styleHandler(dataS[199]); theDataPoints5 += "${colorData}<br>" }
            
            if(dataSize1 >= 201) { styleHandler(dataS[200]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 202) { styleHandler(dataS[201]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 203) { styleHandler(dataS[202]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 204) { styleHandler(dataS[203]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 205) { styleHandler(dataS[204]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 206) { styleHandler(dataS[205]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 207) { styleHandler(dataS[206]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 208) { styleHandler(dataS[207]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 209) { styleHandler(dataS[208]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 210) { styleHandler(dataS[209]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 211) { styleHandler(dataS[210]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 212) { styleHandler(dataS[211]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 213) { styleHandler(dataS[212]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 214) { styleHandler(dataS[213]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 215) { styleHandler(dataS[214]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 216) { styleHandler(dataS[215]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 217) { styleHandler(dataS[216]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 218) { styleHandler(dataS[217]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 219) { styleHandler(dataS[218]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 220) { styleHandler(dataS[219]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 221) { styleHandler(dataS[220]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 222) { styleHandler(dataS[221]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 223) { styleHandler(dataS[222]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 224) { styleHandler(dataS[223]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 225) { styleHandler(dataS[224]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 226) { styleHandler(dataS[225]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 227) { styleHandler(dataS[226]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 228) { styleHandler(dataS[227]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 229) { styleHandler(dataS[228]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 230) { styleHandler(dataS[229]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 231) { styleHandler(dataS[230]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 232) { styleHandler(dataS[231]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 233) { styleHandler(dataS[232]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 234) { styleHandler(dataS[233]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 235) { styleHandler(dataS[234]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 236) { styleHandler(dataS[235]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 237) { styleHandler(dataS[236]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 238) { styleHandler(dataS[237]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 239) { styleHandler(dataS[238]); theDataPoints6 += "${colorData}<br>" }
            if(dataSize1 >= 240) { styleHandler(dataS[239]); theDataPoints6 += "${colorData}<br>" }
            
            if(theDataPoints1 == null) theDataPoints1 = "No Data"
            if(theDataPoints2 == null) theDataPoints2 = "No Data"
            if(theDataPoints3 == null) theDataPoints3 = "No Data"
            if(theDataPoints4 == null) theDataPoints4 = "No Data"
            if(theDataPoints5 == null) theDataPoints5 = "No Data"
            if(theDataPoints6 == null) theDataPoints6 = "No Data"
            
            report1 = "<table width='100%' align='center' border='1'><tr><td colspan='4'><b>Raw Data</b></a></td></tr>"
            report1+= "<tr><td width='33%'>${theDataPoints1}</td><td width='33%'>${theDataPoints2}</td><td width='33%'>${theDataPoints3}</td></tr></table>"
            
            report2 = "<table width='100%' align='center' border='1'><tr><td colspan='4'><b>Raw Data</b></a></td></tr>"
            report2+= "<tr><td width='33%'>${theDataPoints4}</td><td width='33%'>${theDataPoints5}</td><td width='33%'>${theDataPoints6}</td></tr></table>"
            
            paragraph "${report1}"
            paragraph "Date Color Codes - Virtual: <span style='color:${colorVirt}'>${colorVirt}</span>, Zwave: <span style='color:${colorZwav}'>${colorZwav}</span>, Zigbee: <span style='color:${colorZigb}'>${colorZigb}</span>, Other: <span style='color:${colorOther}'>${colorOther}</span>"
            paragraph "${report2}"
        }
    }
}

def reportSummaryOptions(){
    dynamicPage(name: "reportOptions", title: "Report Summary Data", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Report Summary Data")) {
            if(getDevice1 == null || getDevice1 == "") state.reportStats1 = "Nothing to report"
            if(getDevice2 == null || getDevice2 == "") state.reportStats2 = "Nothing to report"
            if(getDevice3 == null || getDevice3 == "") state.reportStats3 = "Nothing to report"
            
            paragraph "<table width='100%' border='1'><tr><td width='33%'>${state.reportStats1}</td><td width='33%'>${state.reportStats2}</td><td width='33%'>${state.reportStats3}</td></tr></table>"
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    setDefaults()

    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "testBtn1"){
        getSummaryData()
    }
    if(state.whichButton == "testBtn2"){
        getRawData()
    }
}
    
// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Hub Watchdog Examiner - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
