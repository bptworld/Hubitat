**Flow Engine Editor User Manual**

---

**Overview**

Flow Engine is a visual automation tool for Hubitat that allows users to build, manage, and execute automation workflows using a drag-and-drop interface. It supports complex conditions, schedules, variables, device actions, and notifications without the need for scripting. Workflows (called "flows") are made up of interconnected nodes representing logic blocks.

**Please Note:** Flow Engine is designed for use on the Chrome Browser. All other browsers may work, but some features won't behave correctly or at all.

---

**Getting Started**

1. **Open Flow Engine Editor:** Load the HTML file in a web browser hosted from your Hubitat File Manager.
2. **Authenticate:** Enter your Hubitat App ID and Access Token in the top toolbar.
3. **Load Devices:** Click `Load Devices` to import your Hubitat devices into Flow Engine.
4. **Start Building:** Add and connect nodes to construct automations visually.

---

**Interface Breakdown**

- **Toolbar:**
  - Node Creation (Triggers, Conditions, Actions, etc.)
  - Flow Operations (Load, Save, Export, Undo/Redo)
  - Display Controls (Background image, Snap to Grid, Brightness slider)
  - Alignment and Distribution tools

- **Canvas (Drawflow):**
  - Central area where nodes are added, moved, connected.
  - Use right-click for node options (e.g., Lock/Unlock, Delete).

- **Editor Panel:**
  - Shows editable configuration for selected node.
  - Includes device pickers, comparators, values, color pickers, variable selectors.

- **Variable Inspector:**
  - Displays current global and flow variables.

- **Log Box:**
  - Shows execution logs and errors.

---

**Core Buttons and Their Functionality**

- `Load Devices`: Fetch and display all devices from Hubitat.
- `Load Flow`: Load a saved flow from Hubitat file storage.
- `Save Flow`: Save the current flow back to Hubitat with the given flow name.
- `New Flow`: Clears the canvas for a new flow.
- `Undo` / `Redo`: Traverse backward/forward through editing actions.
- `Screenshot Flow`: Capture an image of the current layout.
- `Export Flow`: Export a stripped version of the flow without device bindings.

---

**Creating a Flow**

1. **Add Nodes:** Use the buttons in the toolbar to add:
   - Event Triggers
   - Schedule Triggers
   - Device Actions
   - Conditions
   - Logic Gates (AND/OR/NOT)
   - Variable Setters
   - Delays
   - Notifications
   - Repeaters, Comments, etc.

2. **Edit Nodes:**
   - Click on a node to open the configuration editor.
   - Choose devices, attributes, comparators, and values.
   - Use time-based inputs (time of day, sunset/sunrise offsets).

3. **Connect Nodes:**
   - Drag from output port of one node to input of another to define flow.

4. **Test and Save:**
   - Use Test tools (if enabled).
   - Save using `Save Flow`.

---

**Node Types and Examples**

- **Event Trigger**
  - Triggers flow when a device's attribute changes.
  - *Options:* Comparator, Value, Debounce, Click pattern (single/double).
  - *Example:* `Kitchen Motion Sensor switch == on`

- **Schedule**
  - Time-based trigger. Supports repeat days, exact time, or cron.
  - *Options:* Repeat days, time, cron syntax.
  - *Example:* `Every Monday at 8:00 AM`

- **Device Action**
  - Sends commands to one or multiple devices.
  - *Options:* Command (e.g., on, off, setLevel), Value, setColor (with picker).
  - *Example:* `Turn off Kitchen Lights`

- **Condition**
  - Conditional logic evaluation.
  - *Options:* Attribute, Comparator, Value, Show status, Time conditions (sunrise/sunset offset).
  - *Example:* `Time of Day == sunset -10`

- **Logic (AND / OR / NOT)**
  - Evaluate combined condition paths.
  - *Use:* Chain multiple inputs before proceeding.
  - *Example:* `If (Motion AND After Sunset)`

- **Delay / Delay Min**
  - Insert pause before proceeding.
  - *Options:* ms or minutes.
  - *Example:* `Delay 3000 ms`

- **Notification**
  - Send a speech or push notification.
  - *Options:* Notification type, Device, Message.
  - *Example:* `Speech: “Garage door is still open!”`

- **Set Variable**
  - Set a flow or global variable.
  - *Options:* Variable name, Value, Scope (global/flow).

- **Not Matching Var**
  - Checks if multiple devices do not match a value and stores results.
  - *Example:* `Save list of lights not set to 100%`.

- **Save / Restore Device State**
  - Capture current device state, restore later.
  - *Use case:* Pause automation and return to previous state.

- **Repeat**
  - Repeat child actions a set number of times or indefinitely.
  - *Options:* Delay between repeats, Max repeats.

- **Do Nothing**
  - Logic placeholder or visual endpoint.

- **Comment**
  - Freeform notes, useful for documentation or separation.

---

**Variables**

- **Global Variables**
  - Stored in `FE_global_vars.json`
  - Shared across all flows

- **Flow Variables**
  - Created and modified in a specific flow.
  - Use `Set Variable` or `Not Matching Var` to update.

- **Inspector View**
  - Auto-updates every 1.5 seconds with current variable values.

---

**Flow Editing Tips**

- **Multi-select nodes** with `Ctrl+click`
- **Drag and move group** once selected
- **Right-click** a node for options: Lock/Unlock, Delete, Copy, etc.
- **Lock nodes** to prevent accidental edits or moves. Locked nodes show a padlock icon and ignore drag events.
- **Align & Distribute** tools help with neat layout
- **Background image** support for custom canvas look
- **Grid brightness** slider for visibility
- **Snap to grid** toggles node placement alignment

---

**Saving and Loading Flows**

- **Save Flow**
  - Name the flow using allowed characters (letters, numbers, underscore).
  - Hit `Save Flow` to store on Hubitat.
  - Saving a flow in the editor immediately updates the flow on Hubitat—no need to open the Hubitat "One" app or perform extra syncing steps.
  - Hit `Save Flow` to store on Hubitat.

- **Load Flow**
  - Use the dropdown after clicking `Load Flow` to select and import a saved flow.


---

**Sharing Flows**

- Use `Export Flow` to remove device bindings and share logic.
- Ideal for templates or community sharing.
- Device re-selection is required when importing shared flows.

---



End of Manual

