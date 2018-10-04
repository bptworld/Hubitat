/**
 *  ****************  Random Dim and Flicker  ****************
 *
 *  Design Usage:
 *  Designed to make lights dim randomly. Creating a spooky or sparkly effect.
 *
 *  Copyright 2018 @BPTWorld - Bryan Turcotte
 *  Major contributions from @Cobra - Andrew Parker
 *
 *  This App is free!
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
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! - @BPTWorld
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *  
 *  
 *  V1.0.1 - 10/03/18 - Added in some instructions
 *  V1.0.0 - 10/03/18 - Converted to Parent/Child using code developed by @Cobra - Andrew Parker
 *
 */



definition(
    name:"Random Dim and Flicker",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Parent App for 'Random Dim and Flicker' childapps ",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
    )







preferences {
	
     page name: "mainPage", title: "", install: true, uninstall: true
     
} 


def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
   
    log.info "There are ${childApps.size()} child apps"
    childApps.each {child ->
    log.info "Child app: ${child.label}"
    }
    
}


def mainPage() {
    dynamicPage(name: "mainPage") {
      installCheck()
        
if(state.appInstalled == 'COMPLETE'){
			display()
    section ("For each Child App, multiple devices can be selected. Each device will run sequential, Device 1, then Device 2, Back to device 1, then device 2..etc."){}
    section ("To create a random effect, put each device in a separate Child App, using the same switch to turn them on."){}
    section ("Be sure to turn off 'Enable descriptionText logging' for each device. Can create a LOT of log entries!"){}
  section ("Child Apps"){
		app(name: "anyOpenApp", appName: "Random Dim and Flicker Child", namespace: "BPTWorld", title: "<b>Add a new set of devices to dim</b>", multiple: true)
            }
    section (" "){}
  section("App name") {
        label title: "Enter a name for parent app (optional)", required: false
            }    
	}
  }
}



def installCheck(){         
   state.appInstalled = app.getInstallationState() 
  if(state.appInstalled != 'COMPLETE'){
section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  }
    else{
 //       log.info "Parent Installed OK"
    }
	}


def display(){
  
	section{paragraph "Version: 1.0.1<br>@BPTWorld"}
         
}         

def setVersion(){
		state.InternalName = "RandomDimandFlickerParent"  
}

