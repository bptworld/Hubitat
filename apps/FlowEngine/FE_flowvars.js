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
    if (managerEl) renderVarsList("flow");
  }

  async function loadFlowVarsFor(flowFile) {
    // Loads flow vars for a flow name, or creates empty
    if (!allFlowVars[flowFile]) allFlowVars[flowFile] = [];
    flowVars = allFlowVars[flowFile];
    currentFlowFile = flowFile;
    notifyVarsChange();
    if (managerEl) renderVarsList("flow");
  }

  // --- VARIABLE MANAGER SIDEBAR UI ---
  function renderManager(el, opts = {}) {
    managerEl = el;
    let html = `<hr><b>Variables</b>
      <div>
        <button id="addVarBtn" title="Add a new variable">Add</button>
    <button id="switchScopeBtn" title="Toggle single/global variables view">${opts.globalVars ? "Show Flow Vars" : "Show Global Vars"}</button>
    ${opts.globalVars ? `
      <br><br><b>Global Variable Options</b><br><hr>
      <button id="exportVarsBtn" title="Export global variables to a file">Export</button>
      <button id="importVarsBtn" title="Import global variables from a file">Import</button>
      <hr>
    ` : "<br><br><b>Flow Variable Options</b><br><hr>"}
  </div>
  <div id="varsList" style="margin-top:10px;"></div>
`
    el.innerHTML = html;
    renderVarsList(opts.globalVars ? "global" : "flow");
    document.getElementById('addVarBtn').onclick = () => {
      let arr = opts.globalVars ? globalVars : flowVars;
      arr.push({ name:"", value:"", type:"String" });
      renderVarsList(opts.globalVars ? "global" : "flow");
      notifyVarsChange();
      if (opts.globalVars) {
        markExportNeeded(true);
      } else {
        allFlowVars[currentFlowFile] = flowVars;
        saveAllFlowVarsFile();
        markFlowNeedsSave(true);
      }
    };
    if (opts.globalVars) {
      // Export global variables to Hubitat
      document.getElementById('exportVarsBtn').onclick = async () => {
        let arr = globalVars;
        let fileName = "FE_global_vars.json";
        let ok = await uploadVarsToHubitat(arr, fileName);
        if (ok) {
          markExportNeeded(false);
          logAction("Global variables exported to Hubitat as " + fileName, "success");
        }
      };
      // Import global variables from Hubitat
      document.getElementById('importVarsBtn').onclick = async () => {
        try {
          const txt = await fetchHubitatVarFileContent("FE_global_vars.json");
          if (!txt) return;
          let arr = JSON.parse(txt);
          if (Array.isArray(arr)) {
            globalVars = arr;
            renderVarsList("global");
            notifyVarsChange();
            logAction("Imported " + arr.length + " global vars", "success");
          } else {
            alert("File does not contain an array.");
          }
        } catch(e) {
          alert("Failed to import: " + e.message);
        }
      };
    }
    document.getElementById('switchScopeBtn').onclick = () => {
      opts.globalVars = !opts.globalVars;
      renderManager(managerEl, opts);
    };
  }

  function renderVarsList(scope = "flow") {
    updateCtx();
    const arr   = scope === "global" ? globalVars : flowVars;
    const vlist = managerEl.querySelector('#varsList');
    vlist.innerHTML = '';
    if (arr.length === 0) {
      vlist.innerHTML = `
        <div style="color:#aaa; margin-top:10px;">
          There are no ${scope === "global" ? "Global" : "Local"} variables.
          <br><hr>
        </div>`;
      return;
    }
    arr.forEach((v, i) => {
      const type       = parseType(v.value);
      const valDisplay = getVarResolved(v);
      const circ       = hasCircular(v.name, () => arr);
      const used       = isVarUsed(v.name, arr) || false;
      vlist.innerHTML += `
        <div style="margin-bottom:3px;${circ ? 'background:#441919;' : ''}">
          <input
            type="text"
            value="${htmlEscape(v.name)}"
            style="width:120px;${used ? '' : 'background:#ffffff;border:1.5px solid #b04343;'}"
            placeholder="variable name"
            oninput="flowVars.setVarName('${scope}', ${i}, this.value)"
          >
          <input
            type="text"
            value="${htmlEscape(v.value)}"
            style="width:70px"
            placeholder="value"
            onchange="flowVars.setVarVal('${scope}', ${i}, this.value)"
          >
          <span style="font-size:12px; color:${typeColor(type)}">
            ${type}
          </span>
          <span style="font-size:13px; color:${circ ? '#f33' : '#b7ffac'};">
            ${circ
              ? "ERR: Circular"
              : (valDisplay !== undefined ? " = " + htmlEscape(valDisplay) : "")
            }
          </span>
          <button
            onclick="flowVars.delVar('${scope}', ${i})"
            class="delvarbtn"
          >✕</button>
        </div>`;
    });
  }

  // --- INLINE UI EVENTS FOR VARS ---
  root.setVarName = function(scope, i, name) {
    let arr = scope === "global" ? globalVars : flowVars;
    arr[i].name = name;
    notifyVarsChange();
    if (scope === "global") {
      markExportNeeded(true);
    } else {
      allFlowVars[currentFlowFile] = flowVars;
      saveAllFlowVarsFile();
      markFlowNeedsSave(true);
    }
  };
  root.setVarVal = function(scope, i, val) {
    let arr = scope === "global" ? globalVars : flowVars;
    arr[i].value = val;
    renderVarsList(scope);
    notifyVarsChange();
    if (scope === "global") {
      markExportNeeded(true);
    } else {
      allFlowVars[currentFlowFile] = flowVars;
      saveAllFlowVarsFile();
      markFlowNeedsSave(true);
    }
  };
  root.delVar = function(scope, i) {
    let arr = scope === "global" ? globalVars : flowVars;
    arr.splice(i,1);
    renderVarsList(scope);
    notifyVarsChange();
    if (scope === "global") {
      markExportNeeded(true);
    } else {
      allFlowVars[currentFlowFile] = flowVars;
      saveAllFlowVarsFile();
      markFlowNeedsSave(true);
    }
  };

  // --- PUBLIC API ---
  root.flowVars = flowVars;
  root.globalVars = globalVars;
  root.renderManager = renderManager;
  root.evaluate = function(expr) { updateCtx(); return safeEval(expr, ctx); };
  root.add = function(name, value, type, scope="flow") {
    let arr = scope === "global" ? globalVars : flowVars;
    arr.push({ name, value, type });
    renderVarsList(scope);
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
    await autoLoadGlobalVarsFromHubitat();      // Loads globals and updates UI
    await window.flowVars?.loadAllFlowVarsFile?.(); // Loads all flow vars
  };

  updateCtx();
})();
