/* FE_flowvars.js — clean version (no legacy file writes)
   - Save/Delete go ONLY to app endpoints.
   - Maintenance: Update Vars / Clear Global / Clear Flow (bulk delete via app).
   - Provides autoLoadGlobalVarsFromHubitat + initVariablesAfterCreds used by FE_test-2.html.
   - Single guarded click handler for Save (prevents double POSTs).
   - Duplicate-name detection disables Save and blocks POST (and turns name red).
*/

(function () {
  "use strict";

  // -------------------------
  // Helpers & shared state
  // -------------------------
  function __bareFlowName(flowFile) {
    return String(flowFile || "").replace(/\.json$/i, "");
  }

  function __getHubitatCreds() {
    var appId = (document.getElementById("hubitatAppId") && document.getElementById("hubitatAppId").value || localStorage.getItem("hubitatAppId") || "").trim();
    var token = (document.getElementById("hubitatToken") && document.getElementById("hubitatToken").value || localStorage.getItem("hubitatToken") || "").trim();
    if (!appId || !token) throw new Error("Missing Hubitat App ID or Token");
    return { appId: appId, token: token };
  }

  async function __appPost(path, payload) {
    var c = __getHubitatCreds();
    var url = "/apps/api/" + c.appId + "/" + path + "?access_token=" + encodeURIComponent(c.token);
    var res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload || {})
    });
    if (!res.ok) {
      var t; try { t = await res.text(); } catch (_e) { t = res.statusText; }
      throw new Error(path + " failed: HTTP " + res.status + " " + t);
    }
    try { return await res.json(); } catch (_e2) { return null; }
  }

  // Read-only file fetcher (left available; vars are loaded via /variables now)
  async function fetchHubitatVarFileContent(fileName) {
    try {
      var c = __getHubitatCreds();
      if (!fileName) return null;
      var url = "/apps/api/" + c.appId + "/getFile?access_token=" + encodeURIComponent(c.token) + "&name=" + encodeURIComponent(fileName);
      var res = await fetch(url);
      if (!res.ok) return null;
      return await res.text();
    } catch (_e) {
      return null;
    }
  }
  // Expose for FE_test-2.html (overrides any stub)
  window.fetchHubitatVarFileContent = fetchHubitatVarFileContent;

  // mirrors for inspector + delete dropdown
  var globalVars  = Array.isArray(window.FE_global_vars) ? window.FE_global_vars.slice() : [];
  var allFlowVars = (window.FE_flowvars && typeof window.FE_flowvars === "object") ? JSON.parse(JSON.stringify(window.FE_flowvars)) : {};
  var currentFlow = ""; // bare flow name
  var saveInFlight = false;

  // -------------------------
  // App endpoints wrappers
  // -------------------------
  async function saveVariableToApp(o) {
    return __appPost("saveVariable", {
      scope: o.scope,                               // "global" | "flow"
      flow: o.scope === "flow" ? __bareFlowName(o.flowFile) : "", // only for flow
      name: o.name,
      type: o.type,
      value: o.value
    });
  }

  async function deleteVariableInApp(o) {
    return __appPost("deleteVariable", {
      scope: o.scope,
      flow: o.scope === "flow" ? __bareFlowName(o.flowFile) : "",
      name: o.name
    });
  }

  // -------------------------
  // flowVars API (minimal)
  // -------------------------
  var root = window.flowVars || (window.flowVars = {});

  root.getCurrentFlowFile = function () { return currentFlow; };
  root.setCurrentFlowFile = function (flowFile) {
    currentFlow = __bareFlowName(flowFile);
    if (currentFlow && !allFlowVars[currentFlow]) allFlowVars[currentFlow] = [];
    try { updateDeleteVarDropdown(); } catch (_e) {}
  };

  root.getVars = function (scope) {
    if (scope === "global") return globalVars.slice();
    if (scope === "flow")   return (allFlowVars[currentFlow] || []).slice();
    return globalVars.slice().concat((allFlowVars[currentFlow] || []).slice());
  };

  root.setGlobalVars = function (arr) {
    globalVars = Array.isArray(arr) ? arr.slice() : [];
    window.FE_global_vars = globalVars.slice();
  };

  root.setAllFlowVarsMap = function (map) {
    var out = {};
    if (Array.isArray(map)) {
      for (var i = 0; i < map.length; i++) {
        var r = map[i];
        if (!r || !r.flow || !r.name) continue;
        var b = String(r.flow).replace(/\.json$/i, "");
        if (!out[b]) out[b] = [];
        out[b].push({ name: r.name, type: r.type || "String", value: r.value });
      }
    } else if (map && typeof map === "object") {
      var keys = Object.keys(map);
      for (var j = 0; j < keys.length; j++) {
        var k = keys[j];
        var b2 = k.replace(/\.json$/i, "");
        out[b2] = Array.isArray(map[k]) ? map[k] : [];
      }
    }
    allFlowVars = out;
    window.FE_flowvars = out;
    if (currentFlow && !allFlowVars[currentFlow]) allFlowVars[currentFlow] = [];
  };

  // -------------------------
  // Variable Manager UI
  // -------------------------
  root.renderManager = function (el) {
    if (!el) return;

    // Minimal stylesheet: only style the Save button when disabled and the duplicate name
    (function injectVarManagerStyles(){
      if (document.getElementById("fe-vars-style-lock")) return;
      var style = document.createElement("style");
      style.id = "fe-vars-style-lock";
      style.textContent =
        "#saveNewVarBtn:disabled{background:#666;pointer-events:none;cursor:not-allowed;}" +
        "#newVarName.fe-dup{border:2px solid #f33;color:#f33;box-shadow:0 0 0 2px rgba(255,51,51,.2);}";
      document.head.appendChild(style);
    })();

    var html = "";
    html += "<hr>";
    html += "<div style='display:flex;align-items:center;'><b>Variables</b>";
    html += "  <button id='maintVarsBtn' title='Show maintenance options' style='margin-left:8px;font-size:11px;padding:1px 9px;border-radius:7px;cursor:pointer;background:#aaa;color:#232a2d;border:none;box-shadow:0 1px 4px #0003;'>Maintenance</button>";
    html += "</div>";
    html += "<div id='maintVarsRow' style='display:none;margin:7px 0 8px 0;'>";
    html += "  <button id='refreshVarsBtn' style='font-size:11px;padding:2px 12px;border-radius:7px;cursor:pointer;background:#6ca0dc;color:#fff;border:none;margin-right:12px;'>Update Vars</button>";
    html += "  <button id='clearGlobalVarsBtn' style='font-size:11px;padding:2px 12px;border-radius:7px;cursor:pointer;background:#888;color:#fff;border:none;margin-right:12px;'>Clear Global Vars</button>";
    html += "  <button id='clearFlowVarsBtn' style='font-size:11px;padding:2px 12px;border-radius:7px;cursor:pointer;background:#888;color:#fff;border:none;'>Clear Flow Vars</button>";
    html += "</div>";
    html += "<br><b>Add New Variable</b><br><hr>";
    html += "<div id='newVarRow' style='display:flex;flex-direction:column;gap:8px;margin:12px 0 8px 0;align-items:flex-start;'>";
    html += "  <label for='newVarScope'>Scope</label>";
    html += "  <select id='newVarScope' style='width:110px;font-size:13px;'><option value=''> (select) </option><option value='flow'>Flow</option><option value='global'>Global</option></select>";
    html += "  <label for='newVarName' style='margin-top:5px;'>Variable Name</label>";
    html += "  <input id='newVarName' placeholder='Variable Name' style='width:180px;font-size:13px;padding:2px 5px;'>";
    html += "  <label for='newVarValue' style='margin-top:5px;'>Initial Value</label>";
    html += "  <input id='newVarValue' placeholder='Initial Value' style='width:180px;font-size:13px;padding:2px 5px;'>";
    html += "  <label for='newVarType' style='margin-top:5px;'>Type</label>";
    html += "  <select id='newVarType' style='width:110px;font-size:13px;'><option value=''> (select) </option><option value='String'>String</option><option value='Number'>Number</option><option value='Boolean'>Boolean</option></select>";
    html += "  <button id='saveNewVarBtn' style='background:#1d9d53;color:#fff;border:none;border-radius:7px;padding:4px 24px;font-size:15px;margin-top:12px;cursor:pointer;'>Save</button>";
    html += "</div>";
    html += "<hr><b>Delete a Variable</b><br>";
    html += "<div id='deleteVarRow' style='display:flex;flex-direction:column;gap:8px;margin:10px 0 0 0;align-items:flex-start;'>";
    html += "  <label for='deleteVarSelect'>Variable Name</label>";
    html += "  <select id='deleteVarSelect' style='width:210px;font-size:13px;'><option value=''> (select) </option></select>";
    html += "  <button id='deleteVarBtn' style='background:#e64b4b;color:#fff;border:none;border-radius:7px;padding:4px 24px;font-size:15px;'>Delete</button>";
    html += "</div>";

    el.innerHTML = html;

    // helper: remove any previous handlers by cloning nodes
    function resetBtn(id) {
      var node = document.getElementById(id);
      if (!node || !node.parentNode) return null;
      var clone = node.cloneNode(true);
      node.parentNode.replaceChild(clone, node);
      return document.getElementById(id);
    }

    var maintBtn   = resetBtn("maintVarsBtn");
    var refreshBtn = resetBtn("refreshVarsBtn");
    var saveBtn    = resetBtn("saveNewVarBtn");
    var delBtn     = resetBtn("deleteVarBtn");
    var clrGBtn    = resetBtn("clearGlobalVarsBtn");
    var clrFBtn    = resetBtn("clearFlowVarsBtn");

    // Grab inputs for validation helpers
    var scopeSel = document.getElementById("newVarScope");
    var nameEl   = document.getElementById("newVarName");
    var typeSel  = document.getElementById("newVarType");
    var valEl    = document.getElementById("newVarValue");

    updateDeleteVarDropdown();

    if (maintBtn) maintBtn.onclick = function () {
      var row = document.getElementById("maintVarsRow");
      if (!row) return;
      row.style.display = (row.style.display === "none" || !row.style.display) ? "block" : "none";
    };

    if (refreshBtn) refreshBtn.onclick = async function () {
      try { if (typeof window.refreshVarsAndInspector === "function") await window.refreshVarsAndInspector(); } catch (_e) {}
      try { updateDeleteVarDropdown(); } catch (_e2) {}
      alert("Variables refreshed.");
    };

    if (clrGBtn) clrGBtn.onclick = async function () {
      if (!confirm("This can not be undone. Clear ALL Global Vars?")) return;
      var names = Array.isArray(window.FE_global_vars) ? window.FE_global_vars.map(function(v){return v && v.name;}).filter(Boolean) : [];
      for (var i=0;i<names.length;i++) {
        try { await deleteVariableInApp({ scope:"global", flowFile:"", name:names[i] }); } catch (_e) {}
      }
      window.FE_global_vars = [];
      globalVars = [];
      try { if (typeof window.renderVariableInspector === "function") window.renderVariableInspector(); } catch (_e3) {}
      updateDeleteVarDropdown();
      setSaveState();
      alert("All global variables cleared.");
    };

    if (clrFBtn) clrFBtn.onclick = async function () {
      if (!confirm("This can not be undone. Clear ALL Flow Vars for the current flow?")) return;
      var flowFile = root.getCurrentFlowFile ? root.getCurrentFlowFile() : "";
      var key = __bareFlowName(flowFile);
      var list = (window.FE_flowvars && window.FE_flowvars[key]) ? window.FE_flowvars[key].slice() : [];
      for (var i=0;i<list.length;i++) {
        var nm = list[i] && list[i].name;
        if (!nm) continue;
        try { await deleteVariableInApp({ scope:"flow", flowFile: flowFile, name:nm }); } catch (_e) {}
      }
      if (!window.FE_flowvars) window.FE_flowvars = {};
      window.FE_flowvars[key] = [];
      allFlowVars[key] = [];
      try { if (typeof window.renderVariableInspector === "function") window.renderVariableInspector(); } catch (_e2) {}
      updateDeleteVarDropdown();
      setSaveState();
      alert("All flow variables cleared for " + (key || "(none)"));
    };

    // sanitize fields on blur
    if (nameEl) nameEl.onblur = function () {
      var sanitized = (this.value || "").replace(/[^a-zA-Z0-9_-]/g, "");
      if (this.value && this.value !== sanitized) { alert("Variable Name can only use letters, numbers, _ or -"); this.value = sanitized; }
      setSaveState();
    };
    if (valEl) valEl.onblur = function () {
      var sv = String(this.value || "").replace(/['"]/g, "");
      sv = sv.replace(/[^\w\s\-.,:;!?@#$/(){}\[\]=+*<>\\]/g, "");
      if (this.value && this.value !== sv) { alert("Removed quotes and special characters from value."); this.value = sv; }
    };

    // === unified state + helpers ===
    function currentFlowKey() {
      return __bareFlowName((root.getCurrentFlowFile && root.getCurrentFlowFile()) || currentFlow || "");
    }
    function anyDupIn(list, name) {
      if (!Array.isArray(list)) return false;
      var n = String(name || "").toLowerCase();
      for (var i=0;i<list.length;i++) {
        var v = list[i];
        if (v && typeof v.name === "string" && v.name.toLowerCase() === n) return true;
      }
      return false;
    }
    function isDuplicateName(name, scope) {
      if (!name || !scope) return false;
      var key = currentFlowKey();
      if (scope === "global") {
        return anyDupIn(globalVars, name) || anyDupIn(window.FE_global_vars, name);
      }
      if (scope === "flow") {
        var a = (allFlowVars[key] || []);
        var b = (window.FE_flowvars && window.FE_flowvars[key]) || [];
        return anyDupIn(a, name) || anyDupIn(b, name);
      }
      return false;
    }

    // Only this function changes how the Save button looks/behaves
    function setSaveState() {
      var scope = (scopeSel && scopeSel.value || "").trim();
      var type  = (typeSel  && typeSel.value  || "").trim();
      var rawName = (nameEl && nameEl.value   || "").trim();
      var name = rawName.replace(/[^a-zA-Z0-9_-]/g, "");

      // Always keep inputs enabled & editable
      if (nameEl) { nameEl.disabled = false; nameEl.readOnly = false; }
      if (valEl)  { valEl.disabled  = false; valEl.readOnly  = false; }

      var nameDup = isDuplicateName(name, scope);

      // visual for name field only
      if (!name || nameDup) {
        nameEl && nameEl.classList.add("fe-dup");
      } else {
        nameEl && nameEl.classList.remove("fe-dup");
      }

      var disable = !!saveInFlight || !scope || !type || !name || !!nameDup;

      // HARD disable via attribute only (no pointer-events hacks)
      if (saveBtn) saveBtn.disabled = disable;

      return !disable; // true means form OK to save
    }

    // validate on user edits
    if (scopeSel) scopeSel.addEventListener("change", setSaveState);
    if (nameEl)  nameEl.addEventListener("input",  setSaveState);
    if (valEl)   valEl.addEventListener("input",   setSaveState);
    if (typeSel) typeSel.addEventListener("change", setSaveState);
    // initial
    setSaveState();

    // Single guarded Save
    if (saveBtn) saveBtn.onclick = async function () {
      if (!setSaveState()) {
        alert("Duplicate or invalid name in this scope. Pick a different name.");
        return;
      }
      if (saveInFlight) return;
      saveInFlight = true;
      setSaveState(); // disables button

      try {
        var scope = (scopeSel && scopeSel.value || "").trim();
        var name  = (nameEl  && nameEl.value  || "").trim();
        var value = (valEl   && valEl.value   || "");
        var type  = (typeSel && typeSel.value || "");

        name  = name.replace(/[^a-zA-Z0-9_-]/g, "");
        value = String(value).replace(/['"]/g, "").replace(/[^\w\s\-.,:;!?@#$/(){}\[\]=+*<>\\]/g, "");

        if (type === "Number")  value = Number(value);
        if (type === "Boolean") value = (value === "true" || value === "1");

        var flowFile = (root.getCurrentFlowFile && root.getCurrentFlowFile()) || currentFlow || "";
        await saveVariableToApp({ scope: scope, flowFile: flowFile, name: name, type: type, value: value });

        // local reflect (upsert)
        if (scope === "global") {
          if (!Array.isArray(globalVars)) globalVars = [];
          var gi = -1; for (var i=0;i<globalVars.length;i++) if (globalVars[i] && globalVars[i].name && globalVars[i].name.toLowerCase() === name.toLowerCase()) { gi = i; break; }
          if (gi >= 0) globalVars[gi] = { name:name, type:type, value:value };
          else globalVars.push({ name:name, type:type, value:value });
          window.FE_global_vars = globalVars.slice();
        } else {
          var key = __bareFlowName(flowFile);
          if (!allFlowVars[key]) allFlowVars[key] = [];
          var arr = allFlowVars[key];
          var fi = -1; for (var j=0;j<arr.length;j++) if (arr[j] && arr[j].name && arr[j].name.toLowerCase() === name.toLowerCase()) { fi = j; break; }
          if (fi >= 0) arr[fi] = { name:name, type:type, value:value };
          else arr.push({ name:name, type:type, value:value });
          allFlowVars[key] = arr;
          window.FE_flowvars = allFlowVars;
        }

        try { if (typeof window.renderVariableInspector === "function") window.renderVariableInspector(); } catch (_e) {}
        updateDeleteVarDropdown();

        // clear form then re-validate to re-enable Save
        if (scopeSel) scopeSel.value = "";
        if (nameEl)   nameEl.value   = "";
        if (valEl)    valEl.value    = "";
        if (typeSel)  typeSel.value  = "";
        if (nameEl && nameEl.focus) nameEl.focus();
        setSaveState();

        alert("Variable saved.");
      } catch (err) {
        alert(err && err.message ? err.message : String(err));
      } finally {
        saveInFlight = false;
        setSaveState(); // restore button if form valid
      }
    };

    // Delete
    if (delBtn) delBtn.onclick = async function () {
      var sel = document.getElementById("deleteVarSelect");
      var rawName = (sel && sel.value) || "";
      if (!rawName) return alert("Choose a variable to delete.");
      if (!confirm("This can not be undone. Are you sure?")) return;

      // Scope is stored on the option itself
      var opt = sel && sel.options && sel.options[sel.selectedIndex];
      var scope = (opt && opt.dataset && (opt.dataset.scope === "flow" ? "flow" : opt.dataset.scope === "global" ? "global" : "")) || "";
      var name  = rawName;

      if (!scope || !name) { alert("Variable selection could not be parsed."); return; }

      var flowFile = (root.getCurrentFlowFile && root.getCurrentFlowFile()) || currentFlow || "";

      try {
        await deleteVariableInApp({ scope: scope, flowFile: flowFile, name: name });
      } catch (e) {
        alert("Delete failed: " + (e && e.message ? e.message : String(e)));
        return;
      }

      // local removal
      if (scope === "global") {
        var gi = -1; for (var i=0;i<globalVars.length;i++) if (globalVars[i] && globalVars[i].name === name) { gi = i; break; }
        if (gi >= 0) globalVars.splice(gi, 1);
        window.FE_global_vars = globalVars.slice();
      } else {
        var key2 = __bareFlowName(flowFile);
        var arr = allFlowVars[key2] || [];
        var di = -1; for (var j=0;j<arr.length;j++) if (arr[j] && arr[j].name === name) { di = j; break; }
        if (di >= 0) arr.splice(di, 1);
        allFlowVars[key2] = arr;
        window.FE_flowvars = allFlowVars;
      }

      try { if (typeof window.renderVariableInspector === "function") window.renderVariableInspector(); } catch (_e2) {}
      updateDeleteVarDropdown();
      setSaveState();
      alert("Variable deleted.");
    };
  };

  // Back-compat alias
  if (typeof root.setCurrentFlowVars !== "function") {
    root.setCurrentFlowVars = function (flowFile) { return root.setCurrentFlowFile(flowFile); };
  }

  // -------------------------
  // Inspector helpers
  // -------------------------
  window.updateDeleteVarDropdown = function updateDeleteVarDropdown() {
    var sel = document.getElementById("deleteVarSelect");
    if (!sel) return;

    var currentFlowFile = (window.flowVars && window.flowVars.getCurrentFlowFile && window.flowVars.getCurrentFlowFile()) || "";
    var key = __bareFlowName(currentFlowFile);

    var flowMap = (window.FE_flowvars && typeof window.FE_flowvars === "object") ? window.FE_flowvars : {};
    var flowArr = Array.isArray(flowMap[key]) ? flowMap[key] : [];
    var globArr = Array.isArray(window.FE_global_vars) ? window.FE_global_vars : [];

    var opts = ["<option value=''> (select) </option>"];

    var gCopy = globArr.slice().sort(function (a, b) { return (a.name || "").localeCompare(b.name || ""); });
    for (var i = 0; i < gCopy.length; i++) {
      var gv = gCopy[i];
      if (gv && gv.name) {
        opts.push("<option data-scope='global' value='" + String(gv.name).replace(/'/g, "&#39;") + "'>" + gv.name + "</option>");
      }
    }

    var fCopy = flowArr.slice().sort(function (a, b) { return (a.name || "").localeCompare(b.name || ""); });
    for (var j = 0; j < fCopy.length; j++) {
      var fv = fCopy[j];
      if (fv && fv.name) {
        opts.push("<option data-scope='flow' value='" + String(fv.name).replace(/'/g, "&#39;") + "'>" + fv.name + " (flow)</option>");
      }
    }

    sel.innerHTML = opts.join("");
  };

  // -------------------------
  // Init helpers
  // -------------------------
  async function autoLoadGlobalVarsFromHubitat(retries) {
    retries = typeof retries === "number" ? retries : 2;
    try {
      const appId = (document.getElementById("hubitatAppId")?.value || localStorage.getItem("hubitatAppId") || "").trim();
      const token = (document.getElementById("hubitatToken")?.value || localStorage.getItem("hubitatToken") || "").trim();
      if (!appId || !token) return;

      const res = await fetch(`/apps/api/${encodeURIComponent(appId)}/variables?access_token=${encodeURIComponent(token)}`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();

      const globals = Array.isArray(data?.globals) ? data.globals : [];
      const flows   = (data?.flows && typeof data.flows === "object") ? data.flows : {};

      window.FE_global_vars = globals;
      window.FE_flowvars    = flows;
      if (window.flowVars?.setGlobalVars)     window.flowVars.setGlobalVars(globals);
      if (window.flowVars?.setAllFlowVarsMap) window.flowVars.setAllFlowVarsMap(flows);

      if (typeof window.renderVariableInspector === "function") {
        window.renderVariableInspector();
      }
    } catch (e) {
      if (retries > 0) setTimeout(() => autoLoadGlobalVarsFromHubitat(retries - 1), 300);
    }
  }
  window.autoLoadGlobalVarsFromHubitat = window.autoLoadGlobalVarsFromHubitat || autoLoadGlobalVarsFromHubitat;

  window.initVariablesAfterCreds = async function () {
    try { await autoLoadGlobalVarsFromHubitat(2); } catch (_e) {}
    var cf = root.getCurrentFlowFile ? root.getCurrentFlowFile() : "";
    if (cf && !allFlowVars[__bareFlowName(cf)]) allFlowVars[__bareFlowName(cf)] = [];
    try { if (typeof window.renderVariableInspector === "function") window.renderVariableInspector(); } catch (_e2) {}
    updateDeleteVarDropdown();
  };

  // keep mirrors in sync on script load
  if (Array.isArray(window.FE_global_vars)) globalVars = window.FE_global_vars.slice();
  if (window.FE_flowvars && typeof window.FE_flowvars === "object") allFlowVars = window.FE_flowvars;

})(); // end IIFE
