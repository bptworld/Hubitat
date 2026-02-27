/**
 *  ****************  Sensor Event Logger-Viewer  ****************
 *
 * Design Usage:
 * Sensor Event Logger (File, Hourly Flush + API + Viewer + CSV + Infinite Scroll)
 * - Buffers events in state.buffer
 * - Flushes to File Manager once per hour OR when buffer reaches maxBufferEntries
 * - Daily rotated files: <base>_YYYYMMDD.log (NDJSON)
 * - Retention by days (deletes old files)
 * - REST endpoints + standalone HTML viewer:
 *    - Device dropdown (populated from loaded data)
 *    - Infinite scroll pagination
 *    - CSV export (/export.csv)
 *
 *  Copyright 2026 Bryan Turcotte (@bptworld)
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *-------------------------------------------------------------------------------------------------------------------
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
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *  1.0.0 - 02/26/26 - Initial release.
 */

def setVersion(){
    state.name = "Sensor Event Logger-Viewer"
	state.version = "1.0.1"
}

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

definition(
  name: "Sensor Event Logger-Viewer",
  namespace: "bptworld",
  author: "Bryan Turcotte",
  description: "Log device/sensor events to File Manager (hourly flush) with retention, REST endpoints, standalone viewer, and CSV export.",
  category: "Convenience",
  iconUrl: "",
  iconX2Url: "",
  oauth: true
)

preferences { page(name: "mainPage") }

mappings {
  path("/logs")       { action: [GET: "apiGetLogs"] }
  path("/files")      { action: [GET: "apiGetFiles"] }
  path("/raw")        { action: [GET: "apiGetRaw"] }
  path("/flush")      { action: [GET: "apiFlush", POST: "apiFlush"] }
  path("/viewer")     { action: [GET: "apiViewer"] }
  path("/export.csv") { action: [GET: "apiExportCsv"] }
}

/* ===========================
   UI
   =========================== */

def mainPage() {
	dynamicPage(name: "mainPage", title: "Sensor Event Logger/Viewer", install: true, uninstall: true) {
      	if(state?.accessToken){
			section() {
            	paragraph """<a href="/apps/api/${app.id}/viewer?access_token=${state.accessToken}" target="_blank" rel="noopener" class="btn btn-default"><b>Open Log Viewer</b></a>"""
      		}

			section("Devices to Log") {
				input "logDevices", "capability.*", title: "Devices to Log", required:false, multiple:true, submitOnChange:true

              	def attrOpts = getSelectedDeviceAttributeOptions()
              	if (!attrOpts) {
                	paragraph "<b>Choose Attributes to Log:</b> Select one or more devices above to populate the attribute list."
              	} else {
                	input "logAttributes", "enum",
                      title: "Choose Attributes to Log",
                      multiple: true,
                      required: false,
                      options: attrOpts,
                      submitOnChange: true
              	}
            }

            section("Buffer / Flush") {
                input "maxBufferEntries", "number",
                title: "Max buffer entries before auto-flush (0 = never auto-flush)",
                defaultValue: 1000, range: "0..20000", required: true
            }

            section("File Settings") {
                input "baseFileName", "text",
                title: "Base filename (letters/numbers/_/- only, no spaces)",
                defaultValue: "sensor_event_log",
                required: true
                input "keepDays", "number", title: "Days to keep log files", defaultValue: 7, range: "1..365"
            }

            section("Log Options") {
                paragraph """<a href="/apps/api/${app.id}/viewer?access_token=${state.accessToken}" target="_blank" rel="noopener" class="btn btn-default"><b>Open Log Viewer</b></a>"""
                input "btnFlush", "button", title: "Flush Buffer Now (send to file)"
                input "btnPrune", "button", title: "Prune Old Files Now"
                input "btnClear", "button", title: "Delete ALL Log Files Now"
            }

            section("Status") {
                int buf = (state?.buffer instanceof List) ? state.buffer.size() : 0
                paragraph "Buffered (not yet written): <b>${buf}</b>"
                paragraph "Today's file: <b>${todayFileName()}</b>"
                paragraph "Log files found: <b>${listLogFiles().size()}</b>"

                if (state?.accessToken) {
                    def token = state.accessToken
                    paragraph "<b>Standalone viewer</b>:<br/>" +
                          "<small>Remember to not share your access token!</small><br/>" +
                          "/apps/api/${app.id}/viewer?access_token=${token}<br/><br/>" +
                          "<b>CSV export</b>:<br/>" +
                          "/apps/api/${app.id}/export.csv?days=7&limit=5000&access_token=${token}"
                } else {
                    paragraph "<b>API</b>: Access token not created yet. It will be created after install/update."
                }
        	}
            section("") {
                paragraph "<hr style='border: none; height: 4px; background-color: blue; border-radius: 2px;'>"
                paragraph "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
            }
		} else {
            paragraph "<b>Access token not created yet. Tap Done/Update, then reopen this app.<br>* Be sure to enable oAuth in Apps Code first.</b>"
      	}	
	}
}


/* ===========================
   Helpers (UI)
   =========================== */

private List<String> getSelectedDeviceAttributeOptions() {
  def devs = settings?.logDevices
  if (!devs) return []

  List<String> names = []
  devs.each { d ->
    try {
      d?.supportedAttributes?.each { a ->
        def n = a?.name?.toString()
        if (n) names << n
      }
    } catch (e) {
      // ignore device that doesn't expose supportedAttributes cleanly
    }
  }

  names = names.collect { it?.trim() }.findAll { it }
  names = names.unique()
  names.sort { it.toLowerCase() }
  return names
}

/* ===========================
   Lifecycle
   =========================== */

def installed() { initialize() }

def updated() {
  unsubscribe()
  unschedule()
  initialize()
}

def initialize() {
  if (!(state.buffer instanceof List)) state.buffer = []
  if (state.flushing == null) state.flushing = false

  if (!state.accessToken) {
    try { createAccessToken() } catch (e) { log.warn "createAccessToken failed: ${e}" }
  }

  if (logDevices) {
    // Subscribe only to selected attributes (union of attributes across chosen devices)
    List attrs = []
    if (settings?.logAttributes instanceof List) {
      attrs = settings.logAttributes
    } else if (settings?.logAttributes) {
      attrs = [settings.logAttributes]
    }
    attrs = (attrs ?: []).collect { it?.toString()?.trim() }.findAll { it }

    if (!attrs) {
      // Failsafe: if nothing selected, subscribe to all attributes
      logDevices.each { subscribe(it, "all", eventHandler) }
    } else {
      logDevices.each { d -> attrs.each { a -> subscribe(d, a, eventHandler) } }
    }
  }

  runEvery1Hour("flushBufferToFile")
  runEvery1Hour("pruneOldFiles")
  pruneOldFiles()
}

/* ===========================
   Event Handler (BUFFER ONLY)
   =========================== */

def eventHandler(evt) {
  long nowMs = now()

  def entry = [
    ts  : nowMs,
    dt  : new Date(nowMs).format("yyyy-MM-dd HH:mm:ss", location.timeZone),
    dev : evt?.device?.displayName ?: "Unknown",
    attr: evt?.name ?: "",
    val : (evt?.value != null ? evt.value.toString() : ""),
    unit: (evt?.unit != null ? evt.unit.toString() : ""),
    desc: (evt?.descriptionText != null ? evt.descriptionText.toString() : "")
  ]

  if (!(state.buffer instanceof List)) state.buffer = []
  state.buffer << entry

  int maxBuf = safeInt(settings?.maxBufferEntries, 1000)
  if (maxBuf > 0 && state.buffer.size() >= maxBuf) {
    flushBufferToFile()
  }
}

/* ===========================
   Flush (Hourly or Auto)
   =========================== */

def flushBufferToFile() {
  if (state.flushing) return
  state.flushing = true

  try {
    if (!(state.buffer instanceof List) || state.buffer.size() == 0) return

    String fileName = todayFileName()

    StringBuilder sb = new StringBuilder()
    state.buffer.each { sb.append(JsonOutput.toJson(it)).append("\n") }

    appendToHubFile(fileName, sb.toString())
    state.buffer = []
  } catch (e) {
    log.warn "flushBufferToFile error: ${e}"
  } finally {
    state.flushing = false
  }
}

/* ===========================
   File Manager
   =========================== */

String sanitizedBaseName() {
  String s = (settings?.baseFileName ?: "sensor_event_log").toString().trim()
  if (!s) s = "sensor_event_log"
  return s.replaceAll(/[^A-Za-z0-9\-_]/, "_")
}

String todayFileName() {
  String ymd = new Date().format("yyyyMMdd", location.timeZone)
  return "${sanitizedBaseName()}_${ymd}.log"
}

String fileNameForDate(String ymd) {
  return "${sanitizedBaseName()}_${ymd}.log"
}

void appendToHubFile(String fileName, String newText) {
  byte[] existing = null
  try { existing = downloadHubFile(fileName) } catch (ignored) {}

  String combined = ""
  if (existing && existing.length > 0) combined = new String(existing, "UTF-8")
  combined += newText

  uploadHubFile(fileName, combined.getBytes("UTF-8"))
}

List<String> listLogFiles() {
  def files = []
  try {
    def all = getHubFiles() ?: []
    String prefix = sanitizedBaseName() + "_"
    all.each { m ->
      String n = (m?.name ?: m?.fileName ?: "")?.toString()
      if (n && n.startsWith(prefix) && n.endsWith(".log")) files << n
    }
  } catch (e) {
    log.warn "getHubFiles failed: ${e}"
  }
  return files.sort()
}

/* ===========================
   Retention
   =========================== */

void pruneOldFiles() {
  int days = safeInt(settings?.keepDays, 7)
  if (days < 1) days = 1

  String cutoffStr = new Date(now() - (days * 86400000L)).format("yyyyMMdd", location.timeZone)
  long cutoffYmd = cutoffStr.toLong()

  listLogFiles().each { fn ->
    def m = (fn =~ /_(\d{8})\.log$/)
    if (m.find()) {
      long ymd = m.group(1).toLong()
      if (ymd < cutoffYmd) {
        try { deleteHubFile(fn) } catch (e) { log.warn "deleteHubFile(${fn}) failed: ${e}" }
      }
    }
  }
}

void deleteAllLogFiles() {
  listLogFiles().each { fn ->
    try { deleteHubFile(fn) } catch (e) { log.warn "deleteHubFile(${fn}) failed: ${e}" }
  }
}

/* ===========================
   Reader with server-side filters + cursor pagination
   =========================== */

List<Map> readRecentEntriesFiltered(int keepDays, int limit, long sinceTs, long beforeTs, String q, String device) {
  if (keepDays < 1) keepDays = 1
  if (limit < 1) limit = 1
  if (limit > 5000) limit = 5000

  String qlc = (q ?: "").toString().trim().toLowerCase()
  String devNeedle = (device ?: "").toString().trim()

  def slurper = new JsonSlurper()
  def hits = []

  def files = listLogFiles()
  if (!files) return []

  def recentFiles = files.takeRight(Math.min(keepDays, files.size()))

  // Scan files; collect matches; sort desc; return top limit
  recentFiles.each { fn ->
    try {
      byte[] b = downloadHubFile(fn)
      if (!b || b.length == 0) return
      String txt = new String(b, "UTF-8")
      txt.split("\n").each { line ->
        if (!line) return
        try {
          def obj = slurper.parseText(line)
          if (!(obj instanceof Map)) return

          long ts = (obj.ts instanceof Number) ? obj.ts.toLong() : 0L
          if (sinceTs > 0L && ts < sinceTs) return
          if (beforeTs > 0L && ts >= beforeTs) return

          if (devNeedle) {
            String d = (obj.dev ?: "").toString()
            if (d != devNeedle) return
          }

          if (qlc) {
            String hay = [
              obj.dt, obj.dev, obj.attr, obj.val, obj.unit, obj.desc
            ].collect { (it ?: "").toString() }.join(" ").toLowerCase()
            if (!hay.contains(qlc)) return
          }

          hits << obj
        } catch (ignored) {}
      }
    } catch (ignored) {}
  }

  if (!hits) return []

  hits = hits.sort { a, b -> ((b.ts ?: 0L) as Long) <=> ((a.ts ?: 0L) as Long) }
  if (hits.size() > limit) hits = hits.take(limit)
  return hits
}

/* ===========================
   REST API
   =========================== */

def apiGetLogs() {
  int days     = safeInt(params?.days, safeInt(settings?.keepDays, 7))
  int limit    = safeInt(params?.limit, 500)
  long sinceTs = safeLong(params?.sinceTs, 0L)
  long beforeTs= safeLong(params?.beforeTs, 0L)
  String q     = params?.q?.toString()
  String device= params?.device?.toString()

  def entries = readRecentEntriesFiltered(days, limit, sinceTs, beforeTs, q, device)

  // hasMore heuristic: if we returned full page, assume there may be more
  boolean hasMore = (entries.size() == Math.min(limit, 5000))

  render contentType: "application/json", data: JsonOutput.toJson([
    ok: true,
    appId: app.id,
    days: days,
    limit: limit,
    sinceTs: sinceTs,
    beforeTs: beforeTs,
    q: q ?: "",
    device: device ?: "",
    count: entries.size(),
    hasMore: hasMore,
    entries: entries
  ])
}

def apiGetFiles() {
  def files = listLogFiles()
  render contentType: "application/json", data: JsonOutput.toJson([
    ok: true,
    appId: app.id,
    base: sanitizedBaseName(),
    count: files.size(),
    files: files
  ])
}

def apiGetRaw() {
  String ymd = (params?.date ?: "").toString().trim()
  if (!ymd || !(ymd ==~ /\d{8}/)) {
    render status: 400, contentType: "application/json", data: JsonOutput.toJson([ok:false, error:"Provide ?date=YYYYMMDD"])
    return
  }

  String fn = fileNameForDate(ymd)
  try {
    byte[] b = downloadHubFile(fn)
    if (!b) {
      render status: 404, contentType: "application/json", data: JsonOutput.toJson([ok:false, error:"File not found", file: fn])
      return
    }
    render contentType: "text/plain", data: new String(b, "UTF-8")
  } catch (e) {
    render status: 500, contentType: "application/json", data: JsonOutput.toJson([ok:false, error:"download failed", detail: e.toString(), file: fn])
  }
}

def apiFlush() {
  flushBufferToFile()
  int buf = (state?.buffer instanceof List) ? state.buffer.size() : 0
  render contentType: "application/json", data: JsonOutput.toJson([
    ok: true,
    flushed: true,
    file: todayFileName(),
    bufferedNow: buf
  ])
}

/* ===========================
   CSV Export
   =========================== */

def apiExportCsv() {
  int days     = safeInt(params?.days, safeInt(settings?.keepDays, 7))
  int limit    = safeInt(params?.limit, 5000)
  long sinceTs = safeLong(params?.sinceTs, 0L)
  long beforeTs= safeLong(params?.beforeTs, 0L)
  String q     = params?.q?.toString()
  String device= params?.device?.toString()

  if (limit < 1) limit = 1
  if (limit > 50000) limit = 50000 // allow bigger exports

  def entries = readRecentEntriesFiltered(days, limit, sinceTs, beforeTs, q, device)

  StringBuilder sb = new StringBuilder()
  sb.append("ts,dt,device,attr,value,unit,desc\n")
  entries.each { e ->
    sb.append(csv(e.ts)).append(",")
    sb.append(csv(e.dt)).append(",")
    sb.append(csv(e.dev)).append(",")
    sb.append(csv(e.attr)).append(",")
    sb.append(csv(e.val)).append(",")
    sb.append(csv(e.unit)).append(",")
    sb.append(csv(e.desc)).append("\n")
  }

  // Force download-ish behavior via content-type + filename header
  def fn = "${sanitizedBaseName()}_export_${new Date().format('yyyyMMdd_HHmmss', location.timeZone)}.csv"
  render(
    contentType: "text/csv",
    data: sb.toString(),
    headers: ["Content-Disposition": "attachment; filename=\"${fn}\""]
  )
}

String csv(def v) {
  String s = (v == null) ? "" : v.toString()
  // Quote if contains special CSV chars
  boolean needs = (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
  s = s.replace("\"", "\"\"")
  return needs ? "\"${s}\"" : s
}

/* ===========================
   Standalone Viewer (HTML)
   =========================== */

def apiViewer() {
  try {
    String token = params?.access_token?.toString() ?: ""
    String base = "/apps/api/${app.id}"
    render contentType: "text/html", data: buildViewerHtml(base, token)
  } catch (Exception e) {
    log.error "Viewer error", e
    render status: 500, contentType: "application/json",
      data: JsonOutput.toJson([ok:false, error:"viewer_failed", detail: e.toString()])
  }
}

String buildViewerHtml(String apiBase, String token) {
  return """<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" referrerpolicy="no-referrer" />
  <title>Sensor Event Logger</title>
  <style>
    :root{color-scheme:dark;}
    body{margin:0;font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial;background:#0b0d12;color:#e8ecf1}
    header{position:sticky;top:0;background:rgba(11,13,18,.96);backdrop-filter:blur(10px);border-bottom:1px solid rgba(255,255,255,.08);z-index:5}
    .wrap{max-width:1200px;margin:0 auto;padding:14px}
    .row{display:flex;gap:10px;flex-wrap:wrap;align-items:center}
    .pill{display:inline-flex;gap:8px;align-items:center;padding:8px 10px;border:1px solid rgba(255,255,255,.10);border-radius:999px;background:rgba(255,255,255,.04)}
    input,select,button{background:#121623;color:#e8ecf1;border:1px solid rgba(255,255,255,.12);border-radius:10px;padding:10px 12px;font-size:14px}
    input::placeholder{color:rgba(232,236,241,.55)}
    button{cursor:pointer}
    button:hover{border-color:rgba(255,255,255,.22)}
    .grow{flex:1 1 280px}
    .stats{opacity:.8;font-size:13px}
    main{padding:0 0 30px}
    .list{max-width:1200px;margin:0 auto;padding:10px 14px}

    .dayhdr{position:sticky;top:118px;z-index:4;margin:14px 0 8px 0;padding:6px 12px;border-radius:999px;border:1px solid rgba(255,255,255,.10);backdrop-filter:blur(8px);font-weight:900;letter-spacing:.3px;font-size:13px;display:inline-flex;align-items:center;gap:10px}
    .dayhdr .dot{width:8px;height:8px;border-radius:999px;background:rgba(255,255,255,.40);display:inline-block}
    .item.dayA{background: rgba(0,0,0,.60) !important;}
.item{display:grid;grid-template-columns:190px 280px 140px 1fr;align-items:center;gap:24px;border:1px solid rgba(255,255,255,.10);border-radius:14px;padding:12px 18px;margin:8px 0;font-size:15px}
    .item.dayA{background: rgba(0,0,0,.60) !important;}
    .item.dayB{background: rgba(255,255,255,.12) !important;}
    .dayhdr.dayA{background: rgba(0,0,0,.45) !important;}
    .dayhdr.dayB{background: rgba(255,255,255,.10) !important;}

    .dt{font-weight:900;white-space:nowrap}
    .dev{opacity:.95;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
    /* device icon + name */
    .devwrap{display:flex;align-items:center;gap:10px;min-width:0}
    .ico{width:16px;height:16px;flex:0 0 16px;opacity:.92;display:inline-block}
    .ico svg{width:16px;height:16px;display:block}
    .ico i{font-size:16px;line-height:16px;display:block}
    .ico path,.ico circle,.ico rect,.ico line,.ico polyline{stroke:rgba(232,236,241,.95);fill:none;stroke-width:2;stroke-linecap:round;stroke-linejoin:round}


    /* icon font fallback */
    .faico{display:none}
    .svgico{display:block}
    body.fa-ok .faico{display:block}
    body.fa-ok .svgico{display:none}

    .attr{opacity:.75;white-space:nowrap}
    .val{font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
    .val .d{font-weight:500;opacity:.65;margin-left:10px}
.top{display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap}
    .dt{font-weight:900}
    .dev{opacity:.95}
    .kv{margin-top:6px}
    .k{opacity:.75}
    .desc{margin-top:6px;opacity:.75;font-size:13px;line-height:1.25}
    .muted{opacity:.65}
    .danger{border-color:rgba(255,90,90,.35)}
    .footer{max-width:1200px;margin:0 auto;padding:0 14px 24px}
    .loadmore{width:100%;padding:12px 14px;border-radius:14px}
  </style>
</head>
<body>
<header>
  <div class="wrap">
    <div class="row" style="justify-content:space-between;">
      <div style="font-weight:950;font-size:16px">Sensor Event Logger</div>
      <div class="stats" id="status">—</div>
    </div>

    <div class="row" style="margin-top:10px">
      <input class="grow" id="q" placeholder="Search (device / attr / value / description)..." />

      <div class="pill">
        <span class="muted">Device</span>
        <select id="dev">
          <option value="">All</option>
        </select>
      </div>

      <div class="pill">
        <span class="muted">Days</span>
        <select id="days">
          <option>1</option><option>3</option><option selected>7</option><option>14</option><option>30</option><option>90</option>
        </select>
      </div>

      <div class="pill">
        <span class="muted">Page</span>
        <select id="limit">
          <option>200</option><option selected>500</option><option>1000</option><option>2000</option><option>5000</option>
        </select>
      </div>

      <button id="load">Load</button>

      <label class="pill" style="cursor:pointer">
        <input type="checkbox" id="auto" style="margin:0 6px 0 0; transform:translateY(1px)"/>
        <span class="muted">Auto</span>
      </label>

      <button id="export">Export CSV</button>
      <button id="flush" class="danger" title="Flush buffer to file now">Flush</button>
    </div>

    <div class="row" style="margin-top:10px">
      <div class="stats" id="meta"></div>
    </div>
  </div>
</header>

<main>
  <div class="list" id="list"></div>
  <div class="footer">
    <button id="more" class="loadmore">Load older…</button>
  </div>
</main>

<script>
(() => {
  const API_BASE = ${JsonOutput.toJson(apiBase)};
  const TOKEN = ${JsonOutput.toJson(token)};
  const el = (id)=>document.getElementById(id);

  let items = [];          // currently displayed (server-filtered)
  let hasMore = true;      // server says "maybe"
  let loading = false;
  let cursorBeforeTs = 0;  // load older than this
  let autoTimer = null;

  // Font Awesome detection + fallback swap (no blank icons)
  // If FA loads, body gets class "fa-ok" and we show FA icons; otherwise SVG stays visible.
  function initFontAwesomeWatcher(){
    const link = document.querySelector('link[href*="font-awesome"]');
    if(link){
      link.addEventListener("load", () => { setFaOkClass(); render(true); });
      link.addEventListener("error", () => { document.body.classList.remove("fa-ok"); render(true); });
    }
    // Check now and again shortly after (covers cached/slow loads)
    setFaOkClass();
    setTimeout(() => { setFaOkClass(); render(true); }, 600);
    setTimeout(() => { setFaOkClass(); render(true); }, 1500);
  }

  function esc(s){
    return (s ?? "").toString()
      .replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;")
      .replaceAll('"',"&quot;").replaceAll("'","&#39;");
  }

  function fmt(n){ return new Intl.NumberFormat().format(n); }

  function setStatus(t){ el("status").textContent = t; }

  function iconKeyForEntry(e){
    // prefer attribute, fallback to parsing description text
    const a = (e?.attr ?? "").toString().toLowerCase();
    const d = (e?.desc ?? "").toString().toLowerCase();

    // attribute-based mapping
    if(a.includes("motion")) return "motion";
    if(a.includes("contact")) return "contact";
    if(a.includes("temperature") || a === "temp") return "temp";
    if(a.includes("humidity")) return "humidity";
    if(a.includes("illuminance") || a.includes("lux")) return "light";
    if(a.includes("water") || a.includes("leak")) return "water";
    if(a.includes("smoke")) return "smoke";
    if(a.includes("carbonmonoxide") || a.includes("co")) return "co";
    if(a.includes("presence") || a.includes("occup")) return "presence";
    if(a.includes("acceleration") || a.includes("vibration")) return "vibration";
    if(a.includes("battery")) return "battery";
    if(a.includes("power") || a.includes("energy") || a.includes("watt")) return "power";
    if(a.includes("switch")) return "switch";
    if(a.includes("lock")) return "lock";
    if(a.includes("tamper")) return "tamper";

    // description heuristics
    if(d.includes("motion")) return "motion";
    if(d.includes("contact")) return "contact";
    if(d.includes("open") || d.includes("closed")) return "contact";
    if(d.includes("temperature")) return "temp";
    if(d.includes("humidity")) return "humidity";
    if(d.includes("leak") || d.includes("wet") || d.includes("dry")) return "water";
    if(d.includes("smoke")) return "smoke";
    if(d.includes("carbon monoxide") || d.includes(" co ")) return "co";
    if(d.includes("battery")) return "battery";

    return "sensor";
  }

  function iconEmojiForKey(k){
    switch(k){
      case "motion": return "🏃";
      case "contact": return "🚪";
      case "temp": return "🌡️";
      case "humidity": return "💧";
      case "light": return "💡";
      case "water": return "🚰";
      case "smoke": return "🔥";
      case "co": return "☠️";
      case "presence": return "👤";
      case "vibration": return "📳";
      case "battery": return "🔋";
      case "power": return "⚡";
      case "switch": return "⏻";
      case "lock": return "🔒";
      case "tamper": return "🛡️";
      default: return "📟";
    }
  }

  function iconHtmlForKey(k){
    // Font Awesome Free (via CDN). Using Solid icons only.
    // NOTE: If your tablet/PC has no internet, these will not render (fallback still works for dropdown).
    switch(k){
      case "motion":     return `<i class="fa-solid fa-person-running" aria-hidden="true"></i>`;
      case "contact":    return `<i class="fa-solid fa-door-closed" aria-hidden="true"></i>`;
      case "temp":       return `<i class="fa-solid fa-temperature-half" aria-hidden="true"></i>`;
      case "humidity":   return `<i class="fa-solid fa-droplet" aria-hidden="true"></i>`;
      case "light":      return `<i class="fa-solid fa-lightbulb" aria-hidden="true"></i>`;
      case "water":      return `<i class="fa-solid fa-faucet-drip" aria-hidden="true"></i>`;
      case "smoke":      return `<i class="fa-solid fa-fire" aria-hidden="true"></i>`;
      case "co":         return `<i class="fa-solid fa-skull-crossbones" aria-hidden="true"></i>`;
      case "presence":   return `<i class="fa-solid fa-user" aria-hidden="true"></i>`;
      case "vibration":  return `<i class="fa-solid fa-mobile-screen" aria-hidden="true"></i>`;
      case "battery":    return `<i class="fa-solid fa-battery-three-quarters" aria-hidden="true"></i>`;
      case "power":      return `<i class="fa-solid fa-bolt" aria-hidden="true"></i>`;
      case "switch":     return `<i class="fa-solid fa-power-off" aria-hidden="true"></i>`;
      case "lock":       return `<i class="fa-solid fa-lock" aria-hidden="true"></i>`;
      case "tamper":     return `<i class="fa-solid fa-shield-halved" aria-hidden="true"></i>`;
      default:           return `<i class="fa-solid fa-microchip" aria-hidden="true"></i>`;
    }
  }

  function iconSvgForKey(k){
    // Inline SVG fallback (no external deps)
    switch(k){
      case "motion":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="16" cy="5" r="2"></circle>
          <path d="M6 22l3-6 3 2 2 6"></path>
          <path d="M10 11l3-3 4 2"></path>
          <path d="M7 13l3-2"></path>
        </svg>`;
      case "contact":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <rect x="4" y="4" width="8" height="16" rx="2"></rect>
          <rect x="14" y="7" width="6" height="10" rx="2"></rect>
          <line x1="12" y1="12" x2="14" y2="12"></line>
        </svg>`;
      case "temp":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M14 14.8V5a2 2 0 0 0-4 0v9.8a4 4 0 1 0 4 0z"></path>
          <line x1="12" y1="6" x2="12" y2="14"></line>
        </svg>`;
      case "humidity":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 2s6 7 6 12a6 6 0 0 1-12 0c0-5 6-12 6-12z"></path>
        </svg>`;
      case "light":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M9 18h6"></path>
          <path d="M10 22h4"></path>
          <path d="M12 2a7 7 0 0 0-4 12c.6.6 1 1.3 1 2h6c0-.7.4-1.4 1-2a7 7 0 0 0-4-12z"></path>
        </svg>`;
      case "water":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 2s6 7 6 12a6 6 0 0 1-12 0c0-5 6-12 6-12z"></path>
          <line x1="8" y1="20" x2="16" y2="20"></line>
        </svg>`;
      case "smoke":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 2c1 2 2 3 2 5a2 2 0 0 1-4 0c0-2 1-3 2-5z"></path>
          <path d="M7 22h10"></path>
          <path d="M8 18c0-2 2-3 4-3s4 1 4 3"></path>
        </svg>`;
      case "co":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="12" r="9"></circle>
          <path d="M9 10c.5-1 1.5-1.5 3-1.5S14.5 9 15 10"></path>
          <path d="M9 15h6"></path>
        </svg>`;
      case "presence":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="8" r="3"></circle>
          <path d="M5 22c1-4 4-6 7-6s6 2 7 6"></path>
        </svg>`;
      case "vibration":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <rect x="8" y="2" width="8" height="20" rx="2"></rect>
          <line x1="6" y1="7" x2="4" y2="9"></line>
          <line x1="6" y1="17" x2="4" y2="15"></line>
          <line x1="18" y1="7" x2="20" y2="9"></line>
          <line x1="18" y1="17" x2="20" y2="15"></line>
        </svg>`;
      case "battery":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <rect x="3" y="8" width="16" height="8" rx="2"></rect>
          <line x1="21" y1="10" x2="21" y2="14"></line>
          <line x1="6" y1="12" x2="16" y2="12"></line>
        </svg>`;
      case "power":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M13 2L3 14h7l-1 8 12-14h-7l-1-6z"></path>
        </svg>`;
      case "switch":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <line x1="12" y1="2" x2="12" y2="10"></line>
          <path d="M7.5 6.5a7 7 0 1 0 9 0"></path>
        </svg>`;
      case "lock":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <rect x="5" y="11" width="14" height="11" rx="2"></rect>
          <path d="M8 11V8a4 4 0 0 1 8 0v3"></path>
        </svg>`;
      case "tamper":
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 2l8 4v6c0 6-4 10-8 10S4 18 4 12V6l8-4z"></path>
          <line x1="12" y1="7" x2="12" y2="13"></line>
          <circle cx="12" cy="16.5" r="0.6"></circle>
        </svg>`;
      default:
        return `<svg viewBox="0 0 24 24" aria-hidden="true">
          <rect x="4" y="4" width="16" height="16" rx="3"></rect>
          <line x1="8" y1="9" x2="16" y2="9"></line>
          <line x1="8" y1="13" x2="16" y2="13"></line>
          <line x1="8" y1="17" x2="13" y2="17"></line>
        </svg>`;
    }
  }

  function iconDualForKey(k){
    // Always render SVG; swap to Font Awesome only if it successfully loads.
    return `<span class="faico">\${iconHtmlForKey(k)}</span><span class="svgico">\${iconSvgForKey(k)}</span>`;
  }

  function checkFontAwesomeLoaded(){
    // Font Awesome renders icons via ::before content. If loaded, content will be a glyph, not "none".
    const t = document.createElement("i");
    t.className = "fa-solid fa-bolt";
    t.style.position = "absolute";
    t.style.left = "-9999px";
    t.style.top = "-9999px";
    document.body.appendChild(t);
    const c = getComputedStyle(t, "::before").getPropertyValue("content");
    document.body.removeChild(t);
    return (c && c !== "none" && c !== "normal" && c !== '""');
  }

  function setFaOkClass(){
    try{
      if(checkFontAwesomeLoaded()){
        document.body.classList.add("fa-ok");
      }else{
        document.body.classList.remove("fa-ok");
      }
    }catch(_){
      document.body.classList.remove("fa-ok");
    }
  }

  async function getJson(url){
    const r = await fetch(url, {cache:"no-store"});
    if(!r.ok) throw new Error("HTTP " + r.status);
    return await r.json();
  }

  function buildUrl(beforeTs=0){
    const days = parseInt(el("days").value, 10);
    const limit = parseInt(el("limit").value, 10);
    const q = el("q").value.trim();
    const device = el("dev").value;

    const u = new URL(API_BASE + "/logs", location.origin);
    u.searchParams.set("days", days);
    u.searchParams.set("limit", limit);
    if(beforeTs > 0) u.searchParams.set("beforeTs", beforeTs);
    if(q) u.searchParams.set("q", q);
    if(device) u.searchParams.set("device", device);
    u.searchParams.set("access_token", TOKEN);
    return u.toString();
  }

  function buildExportUrl(){
    const days = parseInt(el("days").value, 10);
    const q = el("q").value.trim();
    const device = el("dev").value;

    const u = new URL(API_BASE + "/export.csv", location.origin);
    u.searchParams.set("days", days);
    u.searchParams.set("limit", 50000);
    if(q) u.searchParams.set("q", q);
    if(device) u.searchParams.set("device", device);
    u.searchParams.set("access_token", TOKEN);
    return u.toString();
  }

  function render(append=false, newOnTop=false){
    const list = el("list");

    let lastDay = "";
    const out = [];

    function dayInfoFor(dayKey){
      // Even days => GRAY, Odd days => BLACK (per request)
      const ymd = (dayKey || "").replaceAll("-", "");
      const n = parseInt(ymd, 10);
      const isEven = Number.isFinite(n) ? (n % 2 === 0) : true;
      const bgRow = isEven ? "rgba(255,255,255,.12)" : "rgba(0,0,0,.65)";
      const bgHdr = isEven ? "rgba(255,255,255,.08)" : "rgba(0,0,0,.55)";
      return { isEven, bgRow, bgHdr };
    }

    for(const e of items){
      const dt = (e && e.dt) ? String(e.dt) : "";
      const dayKey = dt.length >= 10 ? dt.substring(0,10) : "";

      if(dayKey && dayKey !== lastDay){
        const di = dayInfoFor(dayKey);
        out.push(`
          <div class="dayhdr" style="background: \${di.bgHdr}">
            <span class="dot"></span>
            <span>\${dayKey}</span>
          </div>
        `);
        lastDay = dayKey;
      }

      const di = dayInfoFor(dayKey);
      const unit = e.unit ? " " + esc(e.unit) : "";
      const d = e.desc ? String(e.desc) : "";
      const dHtml = d ? `<span class="d">\${esc(d)}</span>` : "";

      out.push(`
        <div class="item" style="background: \${di.bgRow}">
          <div class="dt">\${esc(e.dt || "")}</div>
          <div class="dev">
            <div class="devwrap">
              <span class="ico">\${iconDualForKey(iconKeyForEntry(e))}</span>
              <span style="min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">\${esc(e.dev || "")}</span>
            </div>
          </div>
          <div class="attr">\${esc(e.attr || "")}</div>
          <div class="val">\${esc(e.val || "")}\${unit}\${dHtml}</div>
        </div>
      `);
    }

    const html = out.join("");

    list.innerHTML = html;

    el("meta").innerHTML =
      `<span class="muted">Loaded</span> <b>\${fmt(items.length)}</b> <span class="muted">entries</span>` +
      (cursorBeforeTs ? ` <span class="muted">| oldestTs</span> <b>\${cursorBeforeTs}</b>` : "");

    el("more").style.display = (hasMore && items.length>0) ? "block" : "none";
    el("more").disabled = loading;
    el("more").textContent = loading ? "Loading…" : (hasMore ? "Load older…" : "No more");
  }


  function rebuildDeviceDropdownFromItems(){
    const sel = el("dev");
    const current = sel.value || "";
    const map = new Map(); // devName -> iconKey

    for(const e of items){
      const dev = (e?.dev ?? "").toString();
      if(!dev) continue;
      if(!map.has(dev)){
        map.set(dev, iconKeyForEntry(e));
      }
    }

    const devices = Array.from(map.keys()).sort((a,b)=>a.localeCompare(b));

    sel.innerHTML = '<option value="">All</option>' + devices.map(d => {
      const k = map.get(d) || "sensor";
      const emo = iconEmojiForKey(k);
      // NOTE: <option> can't render HTML/SVG reliably; emoji is safe.
      return `<option value="\${esc(d)}">\${emo} \${esc(d)}</option>`;
    }).join("");

    // restore selection if still present
    if(current && devices.includes(current)) sel.value = current;
  }

  async function loadFresh(){
    if(loading) return;
    loading = true;
    hasMore = true;
    cursorBeforeTs = 0;
    items = [];
    render(false);

    setStatus("Loading…");
    try{
      const url = buildUrl(0);
      const data = await getJson(url);
      const chunk = Array.isArray(data.entries) ? data.entries : [];
      hasMore = !!data.hasMore;

      items = chunk;
      // cursor = oldest ts in list
      cursorBeforeTs = items.length ? Math.min(...items.map(x => Number(x.ts||0)).filter(x=>x>0)) : 0;

      rebuildDeviceDropdownFromItems();
      render(false);

      setStatus(new Date().toLocaleTimeString());
    }catch(e){
      setStatus("Load failed: " + e.message);
    }finally{
      loading = false;
      render(true);
    }
  }

  async function loadOlder(){
    if(loading || !hasMore || !cursorBeforeTs) return;
    loading = true;
    render(true);

    setStatus("Loading older…");
    try{
      const url = buildUrl(cursorBeforeTs);
      const data = await getJson(url);
      const chunk = Array.isArray(data.entries) ? data.entries : [];
      hasMore = !!data.hasMore;

      // append older entries
      if(chunk.length){
        items = items.concat(chunk);
        cursorBeforeTs = Math.min(cursorBeforeTs, ...chunk.map(x => Number(x.ts||0)).filter(x=>x>0));
      }

      rebuildDeviceDropdownFromItems();
      render(true);

      setStatus(new Date().toLocaleTimeString());
    }catch(e){
      setStatus("Load older failed: " + e.message);
    }finally{
      loading = false;
      render(true);
    }
  }

  async function flush(){
    const url = API_BASE + "/flush?access_token=" + encodeURIComponent(TOKEN);
    setStatus("Flushing…");
    try{
      await fetch(url, {cache:"no-store"});
      setStatus("Flushed " + new Date().toLocaleTimeString());
      await loadFresh();
    }catch(e){
      setStatus("Flush failed: " + e.message);
    }
  }

  function setAuto(on){
    if(autoTimer) { clearInterval(autoTimer); autoTimer = null; }
    if(on){
      autoTimer = setInterval(loadFresh, 15000);
      loadFresh();
    }
  }

  // infinite scroll trigger
  window.addEventListener("scroll", () => {
    const nearBottom = (window.innerHeight + window.scrollY) >= (document.body.offsetHeight - 800);
    if(nearBottom) loadOlder();
  });

  el("load").addEventListener("click", loadFresh);
  el("more").addEventListener("click", loadOlder);
  el("flush").addEventListener("click", flush);
  el("export").addEventListener("click", () => { window.location.href = buildExportUrl(); });

  // changing filters triggers fresh load
  el("days").addEventListener("change", loadFresh);
  el("limit").addEventListener("change", loadFresh);
  el("dev").addEventListener("change", loadFresh);

  // search: debounce + fresh load (server-side filter)
  let qTimer=null;
  el("q").addEventListener("input", () => {
    clearTimeout(qTimer);
    qTimer = setTimeout(loadFresh, 250);
  });

  el("auto").addEventListener("change", (ev)=>setAuto(!!ev.target.checked));

  // initial
  initFontAwesomeWatcher();
  loadFresh();
})();
</script>
</body>
</html>"""
}

/* ===========================
   Buttons
   =========================== */

def appButtonHandler(btn) {
  if (btn == "btnFlush") flushBufferToFile()
  if (btn == "btnPrune") pruneOldFiles()
  if (btn == "btnClear") deleteAllLogFiles()
}

/* ===========================
   Helpers
   =========================== */

int safeInt(def v, int dflt) {
  try { return Integer.parseInt(v?.toString()) } catch (ignored) { return dflt }
}

long safeLong(def v, long dflt) {
  try { return Long.parseLong(v?.toString()) } catch (ignored) { return dflt }
}

List<String> parseAttrList(String s) {
  if (!s) return []
  return s.split(",").collect { it.trim() }.findAll { it }.unique()
}
