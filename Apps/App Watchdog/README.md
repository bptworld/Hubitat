# App Watchdog
<b>Design Usage:</b><br>
See if any compatible app needs an update, all in one place.<br><br>

<b>New Install:</b><br>
* Copy the Parent code from GitHub into a ‘New App’ under the ‘Apps Code’ menu then click ‘Done’
* Copy the Child code from GitHub into a second ‘New App’ & also save this
* Go to ‘Apps’
* Click ‘Load New Apps’
* Select ‘Abacus’ under ‘User Apps’
* Click 'Done'
* Go back to Apps and open 'App Watchdog'
<br>
You can now create new child apps directly from here.<br><br>
<b>*** HOW TO DISPLAY THE DATA ON A DASHBOARD TILE ***</b>
<b>Design Usage:</b><br>
This driver formats the data to be displayed on Hubitat's Dashboards.<br><br>
<b>New Install:</b><br>
* Copy the Driver code from GitHub into a ‘New driver’ under the ‘Drivers Code’ menu then click ‘Done’<br>
* Go to ‘Devices’ and create a new 'Add Virtual Device'<br>
* Name your new device something like 'App Watchdog Tile'<br>
* Come up with a new 'Device Network ID'<br>
* Select ‘App Watchdog Tile’ under ‘Type’<br>
* Click 'Save Device'<br>
<br><br>
<b>Setup Options:</b><br>
Commands Section<br>
* Ignore everything in this section
<br>
Preferences<br>
* Enable logging - Turns the device logging on and off<br>
* Font Size - Changes the size of the font used on the tile 'bigger number = bigger font'<br>
<br><br>
<b>Usage:</b><br>
Now all you have to do is add this device to one of your dashboards to see the data anytime!<br>
Add a new tile with the following selections
- Pick a device = App Watchdog
- Pick a template = attribute
- 3rd box = watchDevice

<br>
Bryan<br>
@BPTWorld

