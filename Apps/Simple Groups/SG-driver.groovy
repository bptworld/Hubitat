/**
 *  ****************  Simple Groups Driver  ****************
 *
 *  Design Usage:
 *  This driver works with the Simple Groups app.
 *
 *  Copyright 2020-2021 Bryan Turcotte (@bptworld)
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
 *  Changes:
 *
 *  2.0.0 - 01/17/21 - simplified
 *  1.0.2 - 12/31/20 - Added windowShade
 *  1.0.1 - 05/21/20 - Added more stuff
 *  1.0.0 - 05/20/20 - Initial release
 */


metadata {
	definition (name: "Simple Groups Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Groups/SG-driver.groovy") {
        capability "AccelerationSensor"
        capability "CarbonMonoxideDetector"
        capability "ContactSensor"
        capability "DoorControl"
        capability "FilterStatus"
        capability "Lock"
        capability "MotionSensor"
        capability "ShockSensor"
        capability "SleepSensor"
        capability "SmokeDetector"
        capability "SoundSensor"
        capability "Switch"
        capability "TamperAlert"
        capability "Valve"
        capability "WaterSensor"
        capability "WindowShade"
        
        command "ClearStates"
        
        attribute "acceleration", "string"
        attribute "carbonMonoxide", "string"
        attribute "contact", "string"
        attribute "door", "string"
        attribute "filterStatus", "string"
        attribute "lock", "string"
        attribute "motion", "string"
        attribute "shock", "string"
        attribute "sleeping", "string"
        attribute "smoke", "string"
        attribute "sound", "string"
        attribute "switch", "string"
        attribute "tamper", "string"
        attribute "valve", "string"
        attribute "water", "string"
        attribute "windowShade", "string"
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Simple Groups</b>", description: "This device was created by Simple Groups<br><br>The buttons above don't do anything, so please don't try to use them."
        }
    }
}

def ClearStates() {
    state.clear()
}
