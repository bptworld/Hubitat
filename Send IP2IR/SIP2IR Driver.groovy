/**
 *  ****************  IP2IR Telnet Driver  ****************
 *
 *  Design Usage:
 *  This driver is designed to send commands to an iTach IP2IR device.
 *
 *  IR Codes can be found using Global Cache Control Tower IR Database, https://irdb.globalcache.com/
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Thanks to Carson Dallum's (@cdallum) for the original IP2IR driver code
 *  Originally based on: Mike Maxwell's and Allan Klein's code
 *
 *  Usage:
 *  1. Add this code in the Hubitat 'Drivers Code' section.
 *  2. Add the 'Send IP2IR' code to the 'Apps Code' section.
 *
 ------------------------------------------------------------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 * 
 *  V1.0.0 - 10/15/18 - Initial release
 */

metadata {
	definition (name: "IP2IR Telnet", namespace: "BPTWorld", author: "Bryan Turcotte") {
	capability "Initialize"
    capability "Telnet"
    capability "Notification"
}
    
    preferences() {
    
        section(""){
            input "ipaddress", "text", required: true, title: "iTach IP2IR IP Address", defaultValue: "0.0.0.0"
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}

def deviceNotification(message) {
    LOGDEBUG("Sending Message: ${message}")
    
    def code = message
    return new hubitat.device.HubAction("""$code\r\n""",hubitat.device.Protocol.TELNET)
}


def initialize(){
    telnetClose() 
	try {
		LOGDEBUG("Opening telnet connection")
        telnetConnect([terminalType: 'VT100'], "${ipaddress}", 4998, null, null)
  		//give it a chance to start
		pauseExecution(1000)
   		LOGDEBUG("Telnet connection established")
    } catch(e) {
		LOGDEBUG("initialize error: ${e.message}")
    }
}

def installed(){
	initialize()
}

def updated(){
	initialize()
}

def LOGDEBUG(txt){
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
