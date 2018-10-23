
 
MAIN MENU
Location
Devices
Apps
Settings
ADVANCED
Apps Code
Drivers Code
Hub Events
Location Events
Logs
Lighting Effects Child

1
        "Turquoise": [hue: 47, saturation: null],
        "Aqua": [hue: 50, saturation: null],
        "Amber": [hue: 13, saturation: null],
        "Yellow": [hue: 17, saturation: null],
        "Safety_Orange": [hue: 7, saturation: null],
        "Orange": [hue: 10, saturation: null],
        "Indigo": [hue: 73, saturation: null],
        "Purple": [hue: 82, saturation: 100],
        "Pink": [hue: 90.78, saturation: 67.84],
        "Raspberry": [hue: 94 , saturation: null],
        "Red": [hue: 0, saturation: null ],
        "Brick_Red": [hue: 4, saturation: null],
    ]
    def newcolor = colorPallet."${color}"
    LOGDEBUG(" ${color} = ${newcolor}")
    if(newcolor.saturation == null) newcolor.saturation = 100
    newcolor.level = brightnessLevel
    lights*.setColor(newcolor)
    LOGDEBUG("Setting Color = ${color} for: ${lights}")
}
​
// define debug action
def logCheck(){
    state.checkLog = debugMode
    if(state.checkLog == true){
        log.info "All Logging Enabled"
    }
    else if(state.checkLog == false){
        log.info "Further Logging Disabled"
    }
}
​
// logging...
def LOGDEBUG(txt){
    try {
        if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
        log.error("LOGDEBUG unable to output requested data!")
    }
}
​
def display(){
    section{paragraph "Child App Version: 1.1.1"}
} 
Copyright © 2018 Hubitat, Inc. | Terms of Service | Community | Documentation | Support
