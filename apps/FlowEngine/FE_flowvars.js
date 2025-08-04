// FE_flowvars.js – Per-Flow Variable/Expression Engine for FlowEngine

async function autoLoadGlobalVarsFromHubitat(retries = 8) {
  let txt;
  try {
    txt = await fetchHubitatVarFileContent("FE_global_vars.json");
  } catch (e) {
    if (retries > 0) return setTimeout(() => autoLoadGlobalVarsFromHubitat(retries - 1), 300);
    return;
  }
  if (!txt || !txt.trim()) return;
  let parsed;
  try {
    parsed = JSON.parse(txt);
  } catch (e) {
    return;
  }
  window.FE_global_vars      = parsed;
  window.FE_global_var_names = parsed.map(v => v.name).filter(Boolean);

  if (window.flowVars?.setGlobalVars) flowVars.setGlobalVars(parsed);
  if (window.flowVars?.renderManager) flowVars.renderManager(document.getElementById("variableManager"), { globalVars: true });
  if (editor?.selected_id) renderEditor(editor.getNodeFromId(editor.selected_id));
  renderVariableInspector();
}

// --- Hubitat File Manager helpers for variables ---
async function uploadVarsToHubitat(obj, fileName) {
  const appId = document.getElementById("hubitatAppId")?.value.trim();
  const token = document.getElementById("hubitatToken")?.value.trim();
  if (!appId || !token) { alert("Missing Hubitat appId/token"); return false; }
  const url = `/apps/api/${appId}/uploadFile?access_token=${token}&name=${encodeURIComponent(fileName)}`;
  const res = await fetch(url, {
    method: "POST",
    body: JSON.stringify(obj),
    headers: { "Content-Type": "application/json" }
  });
  if (res.ok) {
    logAction(fileName + " Loaded Sucessefully")
    return true;
  } else {
    alert("Failed to upload vars to Hubitat: " + (await res.text()));
    return false;
  }
}

async function fetchHubitatVarFileContent(fileName) {
  const appId = document.getElementById("hubitatAppId")?.value.trim();
  const token = document.getElementById("hubitatToken")?.value.trim();
  if (!appId || !token) { alert("Missing Hubitat appId/token"); return null; }
  if (!fileName || fileName === "null") { alert("No file selected to fetch."); return null; }
  const url = `/apps/api/${appId}/getFile?access_token=${token}&name=${encodeURIComponent(fileName)}`;
  const res = await fetch(url);
  if (!res.ok) { alert("Failed to get file: " + (await res.text())); return null; }
  return await res.text();
}

// --- Core Variable Engine ---
(function() {
  // Expose to global scope
  const root = window.flowVars = {};

  // --- STATE ---
  let globalVars = [];
  let flowVars   = []; // only for the currently loaded flow
  let allFlowVars = {}; // { "FlowName.json": [ ... ], ... }
  let currentFlowFile = null; // "FlowName.json"
  let managerEl = null;
  let ctx = {};
  let listeners = [];

  // --- TYPE & UTILS ---
  function parseType(val) {
    if (typeof val === 'number') return "Number";
    if (typeof val === 'boolean') return "Boolean";
    if (typeof val === 'object') return "Object";
    if (typeof val !== 'string') return "String";
    if (/^\d+(\.\d+)?$/.test(val)) return "Number";
    if (/^(true|false)$/i.test(val)) return "Boolean";
    if (/^\$\(.+\)$/.test(val) || /[><=!&|+\-*\/]/.test(val)) return "Expression";
    return "String";
  }

  function htmlEscape(str) {
    return String(str).replace(/[&<>"']/g, function(m) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]);
    });
  }

  function typeColor(type) {
    return ({
      "Expression": "#e8b84a",
      "Number": "#5cf",
      "Boolean": "#7f7",
      "String": "#baf",
      "Object": "#fa8"
    })[type] || "#fff";
  }
  function errorColor(err) { return err ? "#f33" : "#b7ffac"; }

  // --- SAFE EVALUATOR ---
  const safeFns = {
    now: () => Date.now(),
    dayOfWeek: () => (new Date()).getDay(),
    min: Math.min,
    max: Math.max,
    abs: Math.abs,
    round: Math.round,
    floor: Math.floor,
    ceil: Math.ceil,
    clamp: (v, mn, mx) => Math.max(mn, Math.min(mx, v)),
  };
  function safeEval(expr, context = {}) {
    try {
      if (/window|document|Function|eval|require|process|global/.test(expr)) throw new Error("Unsafe!");
      expr = expr.replace(/\$\((\w+)\)/g, (_, n) => (n in context) ? JSON.stringify(context[n]) : 'undefined');
      let sandbox = Object.assign({}, safeFns, context);
      let f = new Function(...Object.keys(sandbox), `return (${expr})`);
      return f(...Object.values(sandbox));
    } catch(e) {
      return `ERR: ${e.message}`;
    }
  }

  function extractDeps(str) {
    let out = [];
    String(str).replace(/\$\((\w+)\)/g, (_, n) => { out.push(n); return n; });
    return out;
  }

  function hasCircular(varName, getVars, seen = {}) {
    if (seen[varName]) return true;
    seen[varName] = true;
    const v = getVars().find(vv => vv.name === varName);
    if (!v) return false;
    const deps = extractDeps(v.value);
    return deps.some(dep => hasCircular(dep, getVars, {...seen}));
  }

  function getVarResolved(v, visited = {}) {
    if (!v || !v.name) return '';
    if (visited[v.name]) return 'ERR: Circular!';
    visited[v.name] = true;
    if (parseType(v.value) === "Expression") {
      try { updateCtx(); return safeEval(v.value, ctx); }
      catch (e) { return `ERR: ${e.message}`; }
    }
    if (parseType(v.value) === "Number") return parseFloat(v.value);
    if (parseType(v.value) === "Boolean") return /^true$/i.test(v.value);
    return v.value;
  }

  function updateCtx() {
    ctx = {};
    (globalVars || []).forEach(v => ctx[v.name] = getVarResolved(v));
    (flowVars   || []).forEach(v => ctx[v.name] = getVarResolved(v));
  }

  function onVarsChange(fn) { listeners.push(fn); }
  function notifyVarsChange() { listeners.forEach(fn => fn(flowVars, globalVars)); }

  function suggestVars(fragment, scope = "all") {
    let arr = (scope === "global") ? globalVars :
              (scope === "flow") ? flowVars :
              [...globalVars, ...flowVars];
    return arr.map(v => v.name).filter(n => n.startsWith(fragment));
  }

  function makeVarTooltip(varName) {
    let v = flowVars.find(vv => vv.name === varName) || globalVars.find(vv => vv.name === varName);
    if (!v) return `<span style="color:#f33;">[not found]</span>`;
    let type = parseType(v.value);
    let val = getVarResolved(v);
    let circ = hasCircular(varName, () => [...flowVars, ...globalVars]);
    return `
      <b style="color:${typeColor(type)};">${htmlEscape(varName)}</b><br>
      <span style="font-size:11px;color:#aaa;">${htmlEscape(v.value)}</span><br>
      <span style="font-size:13px;color:${errorColor(circ)};">
        ${circ ? 'Circular ref!' : '='+htmlEscape(val)}
      </span>
    `;
  }

  function isVarUsed(name, arr) {
    let regex = new RegExp("\\$\\(" + name + "\\)", "g");
    let others = (arr === flowVars) ? [...flowVars] : [...globalVars];
    for (let v of others) {
      if (v.value && regex.test(v.value)) return true;
    }
    return false;
  }

  // --- FLOW VARS FILE HANDLING ---
  async function loadAllFlowVarsFile() {
    try {
      const txt = await fetchHubitatVarFileContent("FE_flow_vars.json");
      if (!txt) return {};
      const obj = JSON.parse(txt);
      return (typeof obj === "object" && obj) ? obj : {};
    } catch(e) {
      return {};
    }
  }

  async function saveAllFlowVarsFile() {
    await uploadVarsToHubitat(allFlowVars, "FE_flow_vars.json");
  }

  async function setCurrentFlowVars(flowFile) {
    currentFlowFile = flowFile;
    if (!flowFile) { flowVars = []; notifyVarsChange(); return; }
    if (!allFlowVars[flowFile]) allFlowVars[flowFile] = [];
    flowVars = allFlowVars[flowFile];
    notifyVarsChange();
    window.updateDeleteVarDropdown && updateDeleteVarDropdown();
  }

  async function loadFlowVarsFor(flowFile) {
    // Loads flow vars for a flow name, or creates empty
    if (!allFlowVars[flowFile]) allFlowVars[flowFile] = [];
    flowVars = allFlowVars[flowFile];
    currentFlowFile = flowFile;
    notifyVarsChange();
  }

  function renderManager(el, opts = {}) {
    managerEl = el;
    let html = `
      <hr>
      <div style="display:flex;align-items:center;">
        <b>Variables</b>
        <button id="maintVarsBtn" title="Show maintenance options"
          style="margin-left:8px;font-size:11px;padding:1px 9px;border-radius:7px;cursor:pointer;background:#aaa;color:#232a2d;border:none;box-shadow:0 1px 4px #0003;">
          Maintenance
        </button>
      </div>
      <div id="maintVarsRow" style="display:none;margin:7px 0 8px 0;">
        <button id="clearGlobalVarsBtn" style="font-size:11px;padding:2px 12px;border-radius:7px;cursor:pointer;background:#888;color:#fff;border:none;margin-right:12px;">Clear Global Vars</button>
        <button id="clearFlowVarsBtn" style="font-size:11px;padding:2px 12px;border-radius:7px;cursor:pointer;background:#888;color:#fff;border:none;">Clear Flow Vars</button>
      </div>
      <br><b>Add New Variable</b><br><hr>
      <div id="newVarRow" style="display:flex;flex-direction:column;gap:8px;margin:12px 0 8px 0;align-items:flex-start;">
        <label for="newVarScope">Scope</label>
        <select id="newVarScope" style="width:110px;font-size:13px;">
          <option value="">(select)</option>
          <option value="flow">Flow</option>
          <option value="global">Global</option>
        </select>
        <label for="newVarName" style="margin-top:5px;">Variable Name</label>
        <input id="newVarName" placeholder="Variable Name" style="width:180px;font-size:13px;padding:2px 5px;">
        <label for="newVarValue" style="margin-top:5px;">Initial Value</label>
        <input id="newVarValue" placeholder="Initial Value" style="width:180px;font-size:13px;padding:2px 5px;">
        <label for="newVarType" style="margin-top:5px;">Type</label>
        <select id="newVarType" style="width:110px;font-size:13px;">
          <option value="">(select)</option>
          <option value="String">String</option>
          <option value="Number">Number</option>
          <option value="Boolean">Boolean</option>
        </select>
        <button id="saveNewVarBtn" style="background:#1d9d53;color:#fff;border:none;border-radius:7px;padding:4px 24px;font-size:15px;margin-top:12px;">Save</button>
      </div>
      <hr>
      <b>Delete a Variable</b><br>
      <div id="deleteVarRow" style="display:flex;flex-direction:column;gap:8px;margin:10px 0 0 0;align-items:flex-start;">
        <label for="deleteVarSelect">Variable Name</label>
        <select id="deleteVarSelect" style="width:210px;font-size:13px;">
          <option value="">(select)</option>
        </select>
        <button id="deleteVarBtn" style="background:#e64b4b;color:#fff;border:none;border-radius:7px;padding:4px 24px;font-size:15px;">Delete</button>
      </div>
    `;

    el.innerHTML = html;

    // --- Maint toggle ---
    document.getElementById("maintVarsBtn").onclick = () => {
      const row = document.getElementById("maintVarsRow");
      row.style.display = row.style.display === "none" ? "block" : "none";
    };
    document.getElementById("clearGlobalVarsBtn").onclick = async () => {
      if (!confirm("This can not be undone. Are you sure you want to clear ALL Global Vars?")) return;
      await uploadToHubitatFile("FE_global_vars.json", "[]");
      alert("FE_global_vars.json has been cleared.");
      if (typeof refreshVarsAndInspector === "function") await refreshVarsAndInspector();
    };
    document.getElementById("clearFlowVarsBtn").onclick = async () => {
      if (!confirm("This can not be undone. Are you sure you want to clear ALL Flow Vars?")) return;
      await uploadToHubitatFile("FE_flow_vars.json", "{}");
      alert("FE_flow_vars.json has been cleared.");
      if (typeof refreshVarsAndInspector === "function") await refreshVarsAndInspector();
    };

    // --- Sanitize fields on blur ---
    document.getElementById("newVarName").onblur = function () {
      let sanitized = this.value.replace(/[^a-zA-Z0-9_-]/g, "");
      if (this.value && this.value !== sanitized) {
        alert("Variable Name can only use letters, numbers, _ or -");
        this.value = sanitized;
      }
    };
    document.getElementById("newVarValue").onblur = function () {
      let sanitized = String(this.value).replace(/['"]/g, ""); // Remove all quotes
      sanitized = sanitized.replace(/[^\w\s\-.,:;!?@#$/\\()[\]{}=+*<>]/g, "");
      if (this.value && this.value !== sanitized) {
        alert("Removed quotes and special characters from value.");
        this.value = sanitized;
      }
    };

    // --- Save new variable ---
    document.getElementById('saveNewVarBtn').onclick = async () => {
      const scope = document.getElementById("newVarScope").value;
      let name  = document.getElementById("newVarName").value.trim();
      let value = document.getElementById("newVarValue").value;
      const type  = document.getElementById("newVarType").value;
      if (!scope) return alert("Select variable scope (Flow or Global).");
      if (!name) return alert("Enter a variable name.");
      if (!type) return alert("Select a variable type.");

      // Sanitize again before save for safety
      name = name.replace(/[^a-zA-Z0-9_-]/g, "");
      value = String(value).replace(/['"]/g, ""); // Remove all quotes
      value = value.replace(/[^\w\s\-.,:;!?@#$/\\()[\]{}=+*<>]/g, "");

      if (!name) return alert("Variable Name can only use letters, numbers, _ or -");

      if (type === "Number") value = Number(value);
      if (type === "Boolean") value = (value === "true" || value === "1");

      const vObj = { name, value, type };
      if (scope === "global") {
        globalVars.push(vObj);
        await uploadVarsToHubitat(globalVars, "FE_global_vars.json");
      } else {
        allFlowVars[currentFlowFile] = allFlowVars[currentFlowFile] || [];
        allFlowVars[currentFlowFile].push(vObj);
        await saveAllFlowVarsFile();
      }
      // Clear inputs
      document.getElementById("newVarScope").value = "";
      document.getElementById("newVarName").value = "";
      document.getElementById("newVarValue").value = "";
      document.getElementById("newVarType").value = "";
      if (typeof refreshVarsAndInspector === "function") await refreshVarsAndInspector();
      // Also update the delete dropdown
      updateDeleteVarDropdown();
    };

    // --- Handle Delete button ---
    document.getElementById('deleteVarBtn').onclick = async () => {
      const val = document.getElementById("deleteVarSelect").value;
      if (!val) return alert("Choose a variable to delete.");
      if (!confirm("This can not be undone. Are you sure?")) return;
      const [scope, name] = val.split("|");
      let changed = false;
      if (scope === "global") {
        const idx = globalVars.findIndex(v => v.name === name);
        if (idx > -1) {
          globalVars.splice(idx, 1);
          await uploadVarsToHubitat(globalVars, "FE_global_vars.json");
          changed = true;
        }
      } else if (scope === "flow" && currentFlowFile && Array.isArray(allFlowVars[currentFlowFile])) {
        const idx = allFlowVars[currentFlowFile].findIndex(v => v.name === name);
        if (idx > -1) {
          allFlowVars[currentFlowFile].splice(idx, 1);
          await saveAllFlowVarsFile();
          changed = true;
        }
      }
      if (changed) {
        alert("Variable deleted.");
        if (typeof refreshVarsAndInspector === "function") await refreshVarsAndInspector();
        updateDeleteVarDropdown();
      } else {
        alert("Variable not found or could not be deleted.");
      }
    };
  }

  // --- Populate Delete Variable dropdown ---
  function updateDeleteVarDropdown() {
    const sel = document.getElementById("deleteVarSelect");
    if (!sel) return;
    // Build list: all globals, all flow vars for active flow
    let arr = [];
    (globalVars || []).forEach(v => arr.push({ name: v.name, scope: "global" }));
    if (allFlowVars && currentFlowFile && Array.isArray(allFlowVars[currentFlowFile])) {
      allFlowVars[currentFlowFile].forEach(v => arr.push({ name: v.name, scope: "flow" }));
    }
    // Remove dupes, then sort
    const seen = {};
    arr = arr.filter(v => {
      if (!v.name || seen[v.name + v.scope]) return false;
      seen[v.name + v.scope] = true;
      return true;
    });
    arr.sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: "base" }));

    sel.innerHTML = `<option value="">(select)</option>`;
    arr.forEach(v => {
      let label = v.name + (v.scope === "flow" ? " [Flow]" : " [Global]");
      let value = v.scope + "|" + v.name;
      sel.innerHTML += `<option value="${value}">${label}</option>`;
    });
  }
  window.updateDeleteVarDropdown = updateDeleteVarDropdown;

  // --- PUBLIC API ---
  root.flowVars = flowVars;
  root.globalVars = globalVars;
  root.renderManager = renderManager;
  root.evaluate = function(expr) { updateCtx(); return safeEval(expr, ctx); };
  root.add = function(name, value, type, scope="flow") {
    let arr = scope === "global" ? globalVars : flowVars;
    arr.push({ name, value, type });
    notifyVarsChange();
  };
  root.getResolved = getVarResolved;
  root.isVarUsed = isVarUsed;
  root.onVarsChange = onVarsChange;
  root.suggestVars = suggestVars;
  root.updateCtx = updateCtx;
  root.getVars = function(scope="all") {
    if (scope === "global") return globalVars;
    if (scope === "flow") return flowVars;
    return [...globalVars, ...flowVars];
  };

  root.setGlobalVars = function(arr) {
    globalVars.length = 0;
    if (Array.isArray(arr)) for (const v of arr) globalVars.push(v);
    updateCtx();
    notifyVarsChange();
  };

  // --- Flow variable access/refresh ---
  root.loadAllFlowVarsFile = async function() {
    allFlowVars = await loadAllFlowVarsFile();
    // If current flow is set, load its vars
    if (currentFlowFile) await loadFlowVarsFor(currentFlowFile);
  };
  root.setCurrentFlow = async function(flowFile) {
    await loadAllFlowVarsFile();
    await setCurrentFlowVars(flowFile);
  };
  root.saveAllFlowVarsFile = saveAllFlowVarsFile;
  root.getFlowVarsFor = function(flowFile) {
    return allFlowVars[flowFile] || [];
  };
  root.getCurrentFlowFile = function() {
    return currentFlowFile;
  };

  // --- Initialization: load all flow vars at startup ---
  window.initVariablesAfterCreds = async function() {
    logAction("Loading Vars...", "info")
    await autoLoadGlobalVarsFromHubitat();          // Loads globals and updates UI
    await window.flowVars?.loadAllFlowVarsFile?.(); // Loads all flow vars
  };

  updateCtx();
})();
