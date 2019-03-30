# Snapshot
<b>Design Usage:</b><br>
Monitor lights, devices and sensors. Easily see their status right on your dashboard.<br><br>

<b>New Install:</b><br>
* Copy the Parent code from GitHub into a ‘New App’ under the ‘Apps Code’ menu then click ‘Done’
* Copy the Child code from GitHub into a second ‘New App’ & save this too
* Go to ‘Apps’
* Click ‘Load New Apps’
* Select ‘Snapshot’ under ‘User Apps’
* Click 'Done'
* Go back to Apps and open 'Snapshot'
<br>
You can now create new child apps directly from here.<br><br>
<br><br>
<b>*** HOW TO DISPLAY THE DATA ON A DASHBOARD TILE ***</b>
<b>Design Usage:</b><br>
This driver formats the data to be displayed on Hubitat's Dashboards.<br><br>
<b>New Install:</b><br>
* Copy the Driver code from GitHub into a ‘New driver’ under the ‘Drivers Code’ menu then click ‘Done’<br>
* Go to ‘Devices’ and create a new 'Add Virtual Device'<br>
* Name your new device something like 'Snapshot Tile'<br>
* Come up with a new 'Device Network ID'<br>
* Select ‘Snapshot Tile’ under ‘Type’<br>
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
- Pick a device = Snapshot Tile
- Pick a template = attribute
- 3rd box = EACH attribute holds 5 lines of data. So mulitple boxes are now necessary. The options are snapshotContact1-5 OR snapshotSwitch1-5

<br>
Bryan<br>
@BPTWorld
