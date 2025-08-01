// FE_flowvars.js - Full Advanced Variable/Expression Engine for FlowEngine

// --- Hubitat File Manager helpers for variables ---
async function uploadVarsToHubitat(varsArr, fileName) {
  const appId = document.getElementById("hubitatAppId")?.value.trim();
  const token = document.getElementById("hubitatToken")?.value.trim();
  if (!appId || !token) { alert("Missing Hubitat appId/token"); return false; }
  const url = `/apps/api/${appId}/uploadFile?access_token=${token}&name=${encodeURIComponent(fileName)}`;
  const res = await fetch(url, {
    method: "POST",
    body: JSON.stringify(varsArr),
    headers: { "Content-Type": "application/json" }
  });
  if (res.ok) return true;
  alert("Failed to upload vars to Hubitat: " + (await res.text()));
  return false;
}
async function fetchHubitatVarFiles() {
  const appId = document.getElementById("hubitatAppId")?.value.trim();
  const token = document.getElementById("hubitatToken")?.value.trim();
  if (!appId || !token) { alert("Missing Hubitat appId/token"); return []; }
  const url = `/apps/api/${appId}/listFiles?access_token=${token}`;
  const res = await fetch(url);
  if (!res.ok) { alert("Failed to list files on Hubitat: " + (await res.text())); return []; }
  const arr = await res.json();
  // PATCH: handle both plain array and {files:[...]}
  const fileArr = Array.isArray(arr) ? arr : arr.files;
  if (!fileArr) return [];
  return fileArr.filter(x => x.endsWith('.json'));
}

async function fetchHubitatVarFileContent(fileName) {
  const appId = document.getElementById("hubitatAppId")?.value.trim();
  const token = document.getElementById("hubitatToken")?.value.trim();

  if (!appId || !token) {
    alert("Missing Hubitat appId/token");
    return null;
  }

  if (!fileName || fileName === "null") {
    alert("No file selected to fetch.");
    return null;
  }

  const url = `/apps/api/${appId}/getFile?access_token=${token}&name=${encodeURIComponent(fileName)}`;
  const res = await fetch(url);
  if (!res.ok) {
    alert("Failed to get file: " + (await res.text()));
    return null;
  }
  return await res.text();
}

(function() {
  // Expose to global scope
  const root = window.flowVars = {};

  // --- STATE ---
  let flowVars = [];
  let globalVars = [];
  let managerEl = null;  // Root UI element
  let ctx = {};          // Current eval context (all vars)
  let listeners = [];    // For external change notifications

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

  // Utility: escape HTML (for safe tooltips)
  function htmlEscape(str) {
    return String(str).replace(/[&<>"']/g, function(m) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]);
    });
  }

  // Utility: colors for types
  function typeColor(type) {
    return ({
      "Expression": "#e8b84a",
      "Number": "#5cf",
      "Boolean": "#7f7",
      "String": "#baf",
      "Object": "#fa8"
    })[type] || "#fff";
  }

  // Utility: highlight errors
  function errorColor(err) { return err ? "#f33" : "#b7ffac"; }

  // --- SAFE EVALUATOR ---
  // Built-ins for expressions
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
    // Add your own as needed!
  };
  function safeEval(expr, context = {}) {
    try {
      // Disallow unsafe words!
      if (/window|document|Function|eval|require|process|global/.test(expr)) throw new Error("Unsafe!");
      // Replace $(foo) with context.foo
      expr = expr.replace(/\$\((\w+)\)/g, (_, n) =>
        (n in context) ? JSON.stringify(context[n]) : 'undefined'
      );
      // Bind safe functions and context vars
      let sandbox = Object.assign({}, safeFns, context);
      let f = new Function(...Object.keys(sandbox), `return (${expr})`);
      return f(...Object.values(sandbox));
    } catch(e) {
      return `ERR: ${e.message}`;
    }
  }
  // --- DEPENDENCY GRAPH ---
  // Extract $(foo) variable references from a string/expression
  function extractDeps(str) {
    let out = [];
    String(str).replace(/\$\((\w+)\)/g, (_, n) => { out.push(n); return n; });
    return out;
  }

  // Check for circular dependencies (DFS)
  function hasCircular(varName, getVars, seen = {}) {
    if (seen[varName]) return true;
    seen[varName] = true;
    const v = getVars().find(vv => vv.name === varName);
    if (!v) return false;
    const deps = extractDeps(v.value);
    return deps.some(dep => hasCircular(dep, getVars, {...seen}));
  }

  // --- VALUE RESOLUTION (with dependency/expr) ---
  function getVarResolved(v, visited = {}) {
    if (!v || !v.name) return '';
    // Circular reference check
    if (visited[v.name]) return 'ERR: Circular!';
    visited[v.name] = true;
    // Expressions
    if (parseType(v.value) === "Expression") {
      try {
        updateCtx();
        return safeEval(v.value, ctx);
      } catch (e) {
        return `ERR: ${e.message}`;
      }
    }
    // Numbers/booleans
    if (parseType(v.value) === "Number") return parseFloat(v.value);
    if (parseType(v.value) === "Boolean") return /^true$/i.test(v.value);
    // Plain string
    return v.value;
  }

  // --- CONTEXT EVALUATION ---
  function updateCtx() {
    ctx = {};
    (globalVars || []).forEach(v => ctx[v.name] = getVarResolved(v));
    (flowVars || []).forEach(v => ctx[v.name] = getVarResolved(v));
  }

  // --- EVENT LISTENERS FOR VAR CHANGES (external modules can subscribe) ---
  function onVarsChange(fn) { listeners.push(fn); }
  function notifyVarsChange() { listeners.forEach(fn => fn(flowVars, globalVars)); }

  // --- AUTOCOMPLETE SUGGESTIONS (for UI fields) ---
  function suggestVars(fragment, scope = "all") {
    // scope: all, flow, global
    let arr = (scope === "global") ? globalVars :
              (scope === "flow") ? flowVars :
              [...globalVars, ...flowVars];
    return arr.map(v => v.name).filter(n => n.startsWith(fragment));
  }

  // --- TOOLTIP for variable reference (can wire up to any field) ---
  function makeVarTooltip(varName) {
    let v = flowVars.find(vv => vv.name === varName) ||
            globalVars.find(vv => vv.name === varName);
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
        markFlowNeedsSave(true)
      }
    };
    if (opts.globalVars) {
      // Export global variables to Hubitat
      document.getElementById('exportVarsBtn').onclick = async () => {
        let arr = globalVars;
        let fileName = "FE_global_vars.json";
        let ok = await uploadVarsToHubitat(arr, fileName);
        if (ok) {
          markExportNeeded(false); // Set back to green!
          logAction("Global variables exported to Hubitat as " + fileName, "success");
        }
      };
      // Import global variables from Hubitat
      document.getElementById('importVarsBtn').onclick = async () => {
        // Instantly fetch FE_global_vars.json from Hubitat, no picker
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

  // And ensure your setVarVal re‑renders the list:
  root.setVarVal = function(scope, i, val) {
    const arr = scope === "global" ? globalVars : flowVars;
    arr[i].value = val;
    renderVarsList(scope);
    notifyVarsChange();
    if (scope === "global") markExportNeeded(true);
  };

  // --- VARIABLE USAGE DETECTION (across all scopes) ---
  function isVarUsed(name, arr) {
    let regex = new RegExp("\\$\\(" + name + "\\)", "g");
    let others = (arr === flowVars) ? [...flowVars] : [...globalVars];
    for (let v of others) {
      if (v.value && regex.test(v.value)) return true;
    }
    return false;
  }

  // --- INLINE UI EVENTS FOR VARS ---
  root.setVarName = function(scope, i, name) {
    let arr = scope === "global" ? globalVars : flowVars;
    arr[i].name = name;
    notifyVarsChange();
    if (scope === "global") markExportNeeded(true);
  };
  root.setVarVal = function(scope, i, val) {
    let arr = scope === "global" ? globalVars : flowVars;
    arr[i].value = val;
    // now re‑draw the list so the type badge updates
    renderVarsList(scope);
    notifyVarsChange();
    if (scope === "global") markExportNeeded(true);
  };
  root.delVar = function(scope, i) {
    let arr = scope === "global" ? globalVars : flowVars;
    arr.splice(i,1);
    renderVarsList(scope);
    notifyVarsChange();
    if (scope === "global") markExportNeeded(true);
  };

  // --- AUTOCOMPLETE: For UI input fields ---
  // Suggests variable names for a given fragment (call in keyup/input)
  root.suggest = function(fragment, scope = "all") {
    return suggestVars(fragment, scope);
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

  // --- OPTIONAL: Hook for custom field autocomplete/tooltip integration ---
  // Example: in your input field's oninput:
  //   let suggestions = flowVars.suggest(this.value);
  // Example: onmouseover for $(foo): show flowVars.varTooltip("foo")

  // --- FINAL CLEANUP ---
  
    root.updateCtx = updateCtx;
    root.getVars = function(scope="all") {
      if (scope === "global") return globalVars;
      if (scope === "flow") return flowVars;
      return [...globalVars, ...flowVars];
    };

    // Set the globalVars array safely (for when loading from file!)
    root.setGlobalVars = function(arr) {
      globalVars.length = 0;
      if (Array.isArray(arr)) for (const v of arr) globalVars.push(v);
      updateCtx();
      notifyVarsChange();
    };

  updateCtx();
})();
