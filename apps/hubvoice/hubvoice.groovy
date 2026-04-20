/**
 * HubVoice
 * Local Voice Control for Hubitat
 * Made with the help of CoPilot Pro AI
 */
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

definition(
  name: "HubVoice",
  namespace: "bptworld",
  author: "Bryan Turcotte",
  description: "Local Voice Control for Hubitat",
  category: "Convenience",
  iconUrl: "",
  iconX2Url: "",
  iconX3Url: "",
  singleInstance: true
)

preferences {
  page(name: "mainPage")
  page(name: "testPage")
  page(name: "diagPage")
  page(name: "instructionPage")
}

private String appRev() {
  return "beta-018"
}

private Integer maxDebugRouteSteps() {
  return 60
}

/* =========================
   ADVANCED CONFIGURATION CONSTANTS
   ========================= */
private Integer defaultMaxTrackedKeys() { return 5000 }
private Integer defaultDbMaxEvents() { return 2000 }
private Integer defaultMaxRequestsPerMinute() { return 30 }
private Integer defaultBfMaxFails() { return 5 }
private Integer defaultBfWindowMins() { return 10 }
private Integer defaultBfLockoutMins() { return 10 }
private Integer defaultRiskyAuditMax() { return 200 }
private Integer defaultReplayWindowSecs() { return 90 }
private Integer defaultReplayNonceTtlMins() { return 10 }
private Integer defaultLowBatteryThreshold() { return 25 }
private Integer defaultOfflineWindowHours() { return 24 }
private Integer defaultSportsCacheSeconds() { return 45 }
private String defaultFallbackProvider() { return "gemini" }
private String defaultGeminiModel() { return "gemini-2.5-flash" }
private String defaultChatGptModel() { return "gpt-4.1-mini" }
private String defaultChatGptLiveModel() { return "gpt-4.1" }
private BigDecimal defaultChatGptMiniInputUsdPer1M() { return 0.40G }
private BigDecimal defaultChatGptMiniOutputUsdPer1M() { return 1.60G }
private BigDecimal defaultChatGptStdInputUsdPer1M() { return 2.00G }
private BigDecimal defaultChatGptStdOutputUsdPer1M() { return 8.00G }
private BigDecimal defaultGeminiFlashInputUsdPer1M() { return 0.30G }
private BigDecimal defaultGeminiFlashOutputUsdPer1M() { return 2.50G }
private BigDecimal defaultGeminiProInputUsdPer1M() { return 1.25G }
private BigDecimal defaultGeminiProOutputUsdPer1M() { return 10.00G }
private Integer defaultGeminiGroundedFreePerDay() { return 1500 }
private BigDecimal defaultGeminiGroundedUsdPerPrompt() { return 0.035G }
private String defaultGeminiLiveModel() {
  String tier = (settings?.geminiAccountTier ?: defaultGeminiAccountTier()).toString().trim()
  return (tier == "tier_1") ? "gemini-2.5-pro" : "gemini-2.5-flash"
}
private Integer defaultGemini429CooldownSecs() { return 75 }
private String defaultGeminiAccountTier() { return "tier_1" }

private Map geminiQuotaProfile() {
  String tier = (settings?.geminiAccountTier ?: defaultGeminiAccountTier()).toString().trim()
  boolean freeTier = (tier != "tier_1")
  return [
    tier: tier,
    isFreeTier: freeTier,
    cooldownSecs: freeTier ? 75 : 20,
    // Paid tier mirrors practical limits while still leaving headroom to avoid hard API limits.
    maxCallsPerMin: freeTier ? 4 : 120,
    maxCallsPerDay: freeTier ? 18 : 900,
    firstTokens: freeTier ? 180 : 220,
    liveFirstTokens: freeTier ? 260 : 320,
    retryTokens: freeTier ? 280 : 380,
    allowGroundedRetry: !freeTier
  ]
}

private boolean tryConsumeGeminiQuotaSlot(Integer perMinLimit, Integer perDayLimit) {
  try {
    long nowMs = now()
    long cutoff = nowMs - 60000L
    List times = (state.geminiCallTimes instanceof List) ? (List)state.geminiCallTimes : []
    List<Long> pruned = times.collect { it instanceof Number ? (it as Long) : 0L }.findAll { it > cutoff }
    if((perMinLimit ?: 0) > 0 && pruned.size() >= perMinLimit) {
      state.geminiCallTimes = pruned
      return false
    }

    String dayKey = new Date(nowMs).format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map daily = (state.geminiDailyQuota instanceof Map) ? (Map)state.geminiDailyQuota : [day: dayKey, count: 0]
    if((daily.day ?: "") != dayKey) daily = [day: dayKey, count: 0]
    Integer dayCount = (daily.count instanceof Number) ? (daily.count as Integer) : 0
    if((perDayLimit ?: 0) > 0 && dayCount >= perDayLimit) {
      state.geminiDailyQuota = daily
      state.geminiCallTimes = pruned
      return false
    }

    pruned << nowMs
    daily.count = dayCount + 1
    state.geminiCallTimes = pruned
    state.geminiDailyQuota = daily
    return true
  } catch(e) {
    return true
  }
}

private String fmtUsd(BigDecimal amt) {
  BigDecimal value = (amt ?: 0G) as BigDecimal
  return String.format(java.util.Locale.US, "\$%.4f", value.doubleValue())
}

private Map geminiModelPricing(String modelName) {
  String mk = (modelName ?: "").toString().toLowerCase()
  if(mk.contains("pro")) {
    return [inputUsdPer1M: defaultGeminiProInputUsdPer1M(), outputUsdPer1M: defaultGeminiProOutputUsdPer1M()]
  }
  return [inputUsdPer1M: defaultGeminiFlashInputUsdPer1M(), outputUsdPer1M: defaultGeminiFlashOutputUsdPer1M()]
}

private Map extractGeminiUsageMetadata(def data) {
  try {
    def usage = data?.usageMetadata
    if(usage instanceof Map) {
      return [
        promptTokens: safeInt(usage.promptTokenCount, 0),
        outputTokens: safeInt(usage.candidatesTokenCount, 0),
        totalTokens: safeInt(usage.totalTokenCount, 0)
      ]
    }
  } catch(e) {}
  return [promptTokens: 0, outputTokens: 0, totalTokens: 0]
}

private void pruneGeminiCostHistory() {
  try {
    if(!(state.geminiCostHistory instanceof Map)) {
      state.geminiCostHistory = [:]
      return
    }
    String cutoff = new Date(now() - (90L * 24L * 60L * 60L * 1000L)).format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map cleaned = [:]
    ((Map)state.geminiCostHistory).each { k, v ->
      if((k ?: "").toString() >= cutoff) cleaned[k] = v
    }
    state.geminiCostHistory = cleaned
  } catch(e) {}
}

private void trackGeminiCost(String modelName, Map usage, boolean grounded=false) {
  try {
    Map pricing = geminiModelPricing(modelName)
    int promptTokens = safeInt(usage?.promptTokens, 0) ?: 0
    int outputTokens = safeInt(usage?.outputTokens, 0) ?: 0
    if(promptTokens <= 0 && outputTokens <= 0) return

    pruneGeminiCostHistory()
    state.geminiCostHistory = (state.geminiCostHistory instanceof Map) ? state.geminiCostHistory : [:]
    String dayKey = new Date().format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map day = state.geminiCostHistory[dayKey] instanceof Map ? (Map)state.geminiCostHistory[dayKey] : [
      inputTokens: 0, outputTokens: 0, groundedPrompts: 0,
      tokenCostUsd: 0G, groundingCostUsd: 0G, totalCostUsd: 0G, byModel: [:]
    ]
    day.inputTokens = safeInt(day.inputTokens, 0) + promptTokens
    day.outputTokens = safeInt(day.outputTokens, 0) + outputTokens

    BigDecimal tokenCost = (((pricing.inputUsdPer1M as BigDecimal) * (promptTokens as BigDecimal)) + ((pricing.outputUsdPer1M as BigDecimal) * (outputTokens as BigDecimal))) / 1000000G
    day.tokenCostUsd = ((day.tokenCostUsd ?: 0G) as BigDecimal) + tokenCost

    if(grounded) {
      int beforeGrounded = safeInt(day.groundedPrompts, 0) ?: 0
      day.groundedPrompts = beforeGrounded + 1
      if(day.groundedPrompts > defaultGeminiGroundedFreePerDay()) {
        day.groundingCostUsd = ((day.groundingCostUsd ?: 0G) as BigDecimal) + defaultGeminiGroundedUsdPerPrompt()
      }
    }

    day.totalCostUsd = ((day.tokenCostUsd ?: 0G) as BigDecimal) + ((day.groundingCostUsd ?: 0G) as BigDecimal)
    if(!(day.byModel instanceof Map)) day.byModel = [:]
    String modelKey = (modelName ?: "unknown").toString()
    Map modelRec = day.byModel[modelKey] instanceof Map ? (Map)day.byModel[modelKey] : [inputTokens: 0, outputTokens: 0, costUsd: 0G]
    modelRec.inputTokens = safeInt(modelRec.inputTokens, 0) + promptTokens
    modelRec.outputTokens = safeInt(modelRec.outputTokens, 0) + outputTokens
    modelRec.costUsd = ((modelRec.costUsd ?: 0G) as BigDecimal) + tokenCost
    day.byModel[modelKey] = modelRec

    state.geminiCostHistory[dayKey] = day
  } catch(e) {}
}

private Map getGeminiCostSummary() {
  pruneGeminiCostHistory()
  Map hist = (state.geminiCostHistory instanceof Map) ? (Map)state.geminiCostHistory : [:]
  String todayKey = new Date().format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
  String monthKey = new Date().format("yyyyMM", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
  Map today = hist[todayKey] instanceof Map ? (Map)hist[todayKey] : [:]
  Map todayByModel = (today.byModel instanceof Map) ? (Map)today.byModel : [:]
  BigDecimal monthCost = 0G
  BigDecimal monthTokenCost = 0G
  BigDecimal monthGroundingCost = 0G
  int monthInputTokens = 0
  int monthOutputTokens = 0
  int monthGroundedPrompts = 0
  Map monthByModel = [:]
  hist.each { k, v ->
    if((k ?: "").toString().startsWith(monthKey) && v instanceof Map) {
      monthCost += ((v.totalCostUsd ?: 0G) as BigDecimal)
      monthTokenCost += ((v.tokenCostUsd ?: 0G) as BigDecimal)
      monthGroundingCost += ((v.groundingCostUsd ?: 0G) as BigDecimal)
      monthInputTokens += safeInt(v.inputTokens, 0) ?: 0
      monthOutputTokens += safeInt(v.outputTokens, 0) ?: 0
      monthGroundedPrompts += safeInt(v.groundedPrompts, 0) ?: 0
      if(v.byModel instanceof Map) {
        ((Map)v.byModel).each { mk, mv ->
          String modelKey = (mk ?: "unknown").toString()
          Map existing = monthByModel[modelKey] instanceof Map ? (Map)monthByModel[modelKey] : [inputTokens: 0, outputTokens: 0, costUsd: 0G]
          existing.inputTokens = safeInt(existing.inputTokens, 0) + (safeInt(mv?.inputTokens, 0) ?: 0)
          existing.outputTokens = safeInt(existing.outputTokens, 0) + (safeInt(mv?.outputTokens, 0) ?: 0)
          existing.costUsd = ((existing.costUsd ?: 0G) as BigDecimal) + ((mv?.costUsd ?: 0G) as BigDecimal)
          monthByModel[modelKey] = existing
        }
      }
    }
  }
  return [
    todayCostUsd: (today.totalCostUsd ?: 0G) as BigDecimal,
    todayTokenCostUsd: (today.tokenCostUsd ?: 0G) as BigDecimal,
    todayGroundingCostUsd: (today.groundingCostUsd ?: 0G) as BigDecimal,
    todayInputTokens: safeInt(today.inputTokens, 0) ?: 0,
    todayOutputTokens: safeInt(today.outputTokens, 0) ?: 0,
    todayGroundedPrompts: safeInt(today.groundedPrompts, 0) ?: 0,
    todayByModel: todayByModel,
    monthCostUsd: monthCost,
    monthTokenCostUsd: monthTokenCost,
    monthGroundingCostUsd: monthGroundingCost,
    monthInputTokens: monthInputTokens,
    monthOutputTokens: monthOutputTokens,
    monthGroundedPrompts: monthGroundedPrompts,
    monthByModel: monthByModel
  ]
}

private String geminiModelShortLabel(String modelName) {
  String mk = (modelName ?: "").toString().toLowerCase()
  if(mk.contains("pro")) return "Pro"
  if(mk.contains("flash")) return "Flash"
  return (modelName ?: "Unknown").toString()
}

private String formatGeminiCostBreakdown(Map byModel) {
  if(!(byModel instanceof Map) || byModel.isEmpty()) return "none"
  List<String> keys = ((Map)byModel).keySet().collect { it?.toString() ?: "unknown" }.sort { a, b ->
    BigDecimal av = ((((Map)byModel)[a]?.costUsd ?: 0G) as BigDecimal)
    BigDecimal bv = ((((Map)byModel)[b]?.costUsd ?: 0G) as BigDecimal)
    return bv <=> av
  }
  return keys.collect { modelKey ->
    Map rec = ((Map)byModel)[modelKey] instanceof Map ? (Map)((Map)byModel)[modelKey] : [:]
    "${geminiModelShortLabel(modelKey)} ${fmtUsd((rec.costUsd ?: 0G) as BigDecimal)} (${safeInt(rec.inputTokens, 0) ?: 0}/${safeInt(rec.outputTokens, 0) ?: 0})"
  }.join(" | ")
}

private Integer clearGeminiCostCurrentMonth() {
  try {
    if(!(state.geminiCostHistory instanceof Map)) {
      state.geminiCostHistory = [:]
      return 0
    }
    String monthKey = new Date().format("yyyyMM", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map cleaned = [:]
    int removed = 0
    ((Map)state.geminiCostHistory).each { k, v ->
      if((k ?: "").toString().startsWith(monthKey)) {
        removed++
      } else {
        cleaned[k] = v
      }
    }
    state.geminiCostHistory = cleaned
    state.lastGeminiCostResetAt = now()
    state.lastGeminiCostResetScope = "month"
    return removed
  } catch(e) {
    return 0
  }
}

private Integer clearGeminiCostToday() {
  try {
    if(!(state.geminiCostHistory instanceof Map)) {
      state.geminiCostHistory = [:]
      return 0
    }
    String todayKey = new Date().format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Integer removed = state.geminiCostHistory.containsKey(todayKey) ? 1 : 0
    ((Map)state.geminiCostHistory).remove(todayKey)
    state.lastGeminiCostResetAt = now()
    state.lastGeminiCostResetScope = "today"
    return removed
  } catch(e) {
    return 0
  }
}

private Integer clearGeminiCostAllHistory() {
  try {
    if(!(state.geminiCostHistory instanceof Map)) {
      state.geminiCostHistory = [:]
      return 0
    }
    Integer removed = ((Map)state.geminiCostHistory).size()
    state.geminiCostHistory = [:]
    state.lastGeminiCostResetAt = now()
    state.lastGeminiCostResetScope = "all"
    return removed
  } catch(e) {
    return 0
  }
}

private Map chatGptModelPricing(String modelName) {
  String mk = (modelName ?: "").toString().toLowerCase()
  if(mk.contains("mini")) {
    return [inputUsdPer1M: defaultChatGptMiniInputUsdPer1M(), outputUsdPer1M: defaultChatGptMiniOutputUsdPer1M()]
  }
  return [inputUsdPer1M: defaultChatGptStdInputUsdPer1M(), outputUsdPer1M: defaultChatGptStdOutputUsdPer1M()]
}

private void pruneChatGptCostHistory() {
  try {
    if(!(state.chatgptCostHistory instanceof Map)) {
      state.chatgptCostHistory = [:]
      return
    }
    String cutoff = new Date(now() - (90L * 24L * 60L * 60L * 1000L)).format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map cleaned = [:]
    ((Map)state.chatgptCostHistory).each { k, v ->
      if((k ?: "").toString() >= cutoff) cleaned[k] = v
    }
    state.chatgptCostHistory = cleaned
  } catch(e) {}
}

private void trackChatGptCost(String modelName, Map usage, boolean grounded=false) {
  try {
    Map pricing = chatGptModelPricing(modelName)
    int promptTokens = safeInt(usage?.promptTokens, 0) ?: 0
    int outputTokens = safeInt(usage?.outputTokens, 0) ?: 0
    if(promptTokens <= 0 && outputTokens <= 0) return

    pruneChatGptCostHistory()
    state.chatgptCostHistory = (state.chatgptCostHistory instanceof Map) ? state.chatgptCostHistory : [:]
    String dayKey = new Date().format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map day = state.chatgptCostHistory[dayKey] instanceof Map ? (Map)state.chatgptCostHistory[dayKey] : [
      inputTokens: 0, outputTokens: 0, groundedPrompts: 0,
      tokenCostUsd: 0G, totalCostUsd: 0G, byModel: [:]
    ]
    day.inputTokens = safeInt(day.inputTokens, 0) + promptTokens
    day.outputTokens = safeInt(day.outputTokens, 0) + outputTokens
    if(grounded) day.groundedPrompts = (safeInt(day.groundedPrompts, 0) ?: 0) + 1

    BigDecimal tokenCost = (((pricing.inputUsdPer1M as BigDecimal) * (promptTokens as BigDecimal)) + ((pricing.outputUsdPer1M as BigDecimal) * (outputTokens as BigDecimal))) / 1000000G
    day.tokenCostUsd = ((day.tokenCostUsd ?: 0G) as BigDecimal) + tokenCost
    day.totalCostUsd = ((day.tokenCostUsd ?: 0G) as BigDecimal)

    if(!(day.byModel instanceof Map)) day.byModel = [:]
    String modelKey = (modelName ?: "unknown").toString()
    Map modelRec = day.byModel[modelKey] instanceof Map ? (Map)day.byModel[modelKey] : [inputTokens: 0, outputTokens: 0, costUsd: 0G]
    modelRec.inputTokens = safeInt(modelRec.inputTokens, 0) + promptTokens
    modelRec.outputTokens = safeInt(modelRec.outputTokens, 0) + outputTokens
    modelRec.costUsd = ((modelRec.costUsd ?: 0G) as BigDecimal) + tokenCost
    day.byModel[modelKey] = modelRec
    state.chatgptCostHistory[dayKey] = day
  } catch(e) {}
}

private Map getChatGptCostSummary() {
  pruneChatGptCostHistory()
  Map hist = (state.chatgptCostHistory instanceof Map) ? (Map)state.chatgptCostHistory : [:]
  String todayKey = new Date().format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
  String monthKey = new Date().format("yyyyMM", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
  Map today = hist[todayKey] instanceof Map ? (Map)hist[todayKey] : [:]
  Map todayByModel = (today.byModel instanceof Map) ? (Map)today.byModel : [:]
  BigDecimal monthCost = 0G
  int monthInputTokens = 0
  int monthOutputTokens = 0
  int monthGroundedPrompts = 0
  Map monthByModel = [:]
  hist.each { k, v ->
    if((k ?: "").toString().startsWith(monthKey) && v instanceof Map) {
      monthCost += ((v.totalCostUsd ?: 0G) as BigDecimal)
      monthInputTokens += safeInt(v.inputTokens, 0) ?: 0
      monthOutputTokens += safeInt(v.outputTokens, 0) ?: 0
      monthGroundedPrompts += safeInt(v.groundedPrompts, 0) ?: 0
      if(v.byModel instanceof Map) {
        ((Map)v.byModel).each { mk, mv ->
          String modelKey = (mk ?: "unknown").toString()
          Map existing = monthByModel[modelKey] instanceof Map ? (Map)monthByModel[modelKey] : [inputTokens: 0, outputTokens: 0, costUsd: 0G]
          existing.inputTokens = safeInt(existing.inputTokens, 0) + (safeInt(mv?.inputTokens, 0) ?: 0)
          existing.outputTokens = safeInt(existing.outputTokens, 0) + (safeInt(mv?.outputTokens, 0) ?: 0)
          existing.costUsd = ((existing.costUsd ?: 0G) as BigDecimal) + ((mv?.costUsd ?: 0G) as BigDecimal)
          monthByModel[modelKey] = existing
        }
      }
    }
  }
  return [
    todayCostUsd: (today.totalCostUsd ?: 0G) as BigDecimal,
    todayInputTokens: safeInt(today.inputTokens, 0) ?: 0,
    todayOutputTokens: safeInt(today.outputTokens, 0) ?: 0,
    todayGroundedPrompts: safeInt(today.groundedPrompts, 0) ?: 0,
    todayByModel: todayByModel,
    monthCostUsd: monthCost,
    monthInputTokens: monthInputTokens,
    monthOutputTokens: monthOutputTokens,
    monthGroundedPrompts: monthGroundedPrompts,
    monthByModel: monthByModel
  ]
}

private Integer clearChatGptCostCurrentMonth() {
  try {
    if(!(state.chatgptCostHistory instanceof Map)) {
      state.chatgptCostHistory = [:]
      return 0
    }
    String monthKey = new Date().format("yyyyMM", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Map cleaned = [:]
    int removed = 0
    ((Map)state.chatgptCostHistory).each { k, v ->
      if((k ?: "").toString().startsWith(monthKey)) removed++
      else cleaned[k] = v
    }
    state.chatgptCostHistory = cleaned
    state.lastChatGptCostResetAt = now()
    state.lastChatGptCostResetScope = "month"
    return removed
  } catch(e) {
    return 0
  }
}

private Integer clearChatGptCostToday() {
  try {
    if(!(state.chatgptCostHistory instanceof Map)) {
      state.chatgptCostHistory = [:]
      return 0
    }
    String todayKey = new Date().format("yyyyMMdd", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    Integer removed = state.chatgptCostHistory.containsKey(todayKey) ? 1 : 0
    ((Map)state.chatgptCostHistory).remove(todayKey)
    state.lastChatGptCostResetAt = now()
    state.lastChatGptCostResetScope = "today"
    return removed
  } catch(e) {
    return 0
  }
}

private Integer clearChatGptCostAllHistory() {
  try {
    if(!(state.chatgptCostHistory instanceof Map)) {
      state.chatgptCostHistory = [:]
      return 0
    }
    Integer removed = ((Map)state.chatgptCostHistory).size()
    state.chatgptCostHistory = [:]
    state.lastChatGptCostResetAt = now()
    state.lastChatGptCostResetScope = "all"
    return removed
  } catch(e) {
    return 0
  }
}

private Map getOrInitStats() {
  if(!(state.queryStats instanceof Map)) {
    state.queryStats = [total: 0, bySource: [:], lastQueries: [], geminiUsage: [models: [:], attempts: [:], categories: [:]], chatgptUsage: [models: [:], attempts: [:], categories: [:]]]
  }
  if(!(state.queryStats.geminiUsage instanceof Map)) state.queryStats.geminiUsage = [models: [:], attempts: [:], categories: [:]]
  if(!(state.queryStats.chatgptUsage instanceof Map)) state.queryStats.chatgptUsage = [models: [:], attempts: [:], categories: [:]]
  return state.queryStats
}

private void trackQueryResult(String source, String query) {
  Map stats = getOrInitStats()
  stats.total = (stats.total ?: 0) + 1
  if(!(stats.bySource instanceof Map)) stats.bySource = [:]
  stats.bySource[source] = (stats.bySource[source] ?: 0) + 1
  if(!(stats.lastQueries instanceof List)) stats.lastQueries = []
  stats.lastQueries.push([ts: now(), query: query, source: source])
  if(stats.lastQueries.size() > 50) stats.lastQueries = stats.lastQueries.drop(1)
  state.queryStats = stats
}

private Map getQueryStats() {
  Map stats = getOrInitStats()
  return [
    totalQueries: stats.total ?: 0,
    bySource: stats.bySource ?: [:],
    lastCount: stats.lastQueries?.size() ?: 0,
    geminiModels: (stats.geminiUsage?.models instanceof Map) ? stats.geminiUsage.models : [:],
    geminiAttempts: (stats.geminiUsage?.attempts instanceof Map) ? stats.geminiUsage.attempts : [:],
    geminiCategories: (stats.geminiUsage?.categories instanceof Map) ? stats.geminiUsage.categories : [:],
    chatgptModels: (stats.chatgptUsage?.models instanceof Map) ? stats.chatgptUsage.models : [:],
    chatgptAttempts: (stats.chatgptUsage?.attempts instanceof Map) ? stats.chatgptUsage.attempts : [:],
    chatgptCategories: (stats.chatgptUsage?.categories instanceof Map) ? stats.chatgptUsage.categories : [:]
  ]
}

private void trackGeminiUsage(String modelName, String attemptLabel, String category) {
  Map stats = getOrInitStats()
  if(!(stats.geminiUsage instanceof Map)) stats.geminiUsage = [models: [:], attempts: [:], categories: [:]]
  if(!(stats.geminiUsage.models instanceof Map)) stats.geminiUsage.models = [:]
  if(!(stats.geminiUsage.attempts instanceof Map)) stats.geminiUsage.attempts = [:]
  if(!(stats.geminiUsage.categories instanceof Map)) stats.geminiUsage.categories = [:]

  String modelKey = (modelName ?: "unknown").toString()
  String attemptKey = (attemptLabel ?: "unknown").toString()
  String categoryKey = (category ?: "general").toString()
  stats.geminiUsage.models[modelKey] = (stats.geminiUsage.models[modelKey] ?: 0) + 1
  stats.geminiUsage.attempts[attemptKey] = (stats.geminiUsage.attempts[attemptKey] ?: 0) + 1
  stats.geminiUsage.categories[categoryKey] = (stats.geminiUsage.categories[categoryKey] ?: 0) + 1
  state.queryStats = stats
}

private void trackChatGptUsage(String modelName, String attemptLabel, String category) {
  Map stats = getOrInitStats()
  if(!(stats.chatgptUsage instanceof Map)) stats.chatgptUsage = [models: [:], attempts: [:], categories: [:]]
  if(!(stats.chatgptUsage.models instanceof Map)) stats.chatgptUsage.models = [:]
  if(!(stats.chatgptUsage.attempts instanceof Map)) stats.chatgptUsage.attempts = [:]
  if(!(stats.chatgptUsage.categories instanceof Map)) stats.chatgptUsage.categories = [:]

  String modelKey = (modelName ?: "unknown").toString()
  String attemptKey = (attemptLabel ?: "unknown").toString()
  String categoryKey = (category ?: "general").toString()
  stats.chatgptUsage.models[modelKey] = (stats.chatgptUsage.models[modelKey] ?: 0) + 1
  stats.chatgptUsage.attempts[attemptKey] = (stats.chatgptUsage.attempts[attemptKey] ?: 0) + 1
  stats.chatgptUsage.categories[categoryKey] = (stats.chatgptUsage.categories[categoryKey] ?: 0) + 1
  state.queryStats = stats
}

private void pruneSportsApiCache(Integer maxAgeSecs) {
  try {
    if(!(state.sportsApiCache instanceof Map)) {
      state.sportsApiCache = [:]
      return
    }
    long cutoff = now() - ((maxAgeSecs ?: 3600) as Long) * 1000L
    Map cleaned = [:]
    ((Map)state.sportsApiCache).each { k, v ->
      if(v instanceof Map && v.ts instanceof Number && (v.ts as Long) >= cutoff) {
        cleaned[k] = v
      }
    }
    state.sportsApiCache = cleaned
  } catch(e) {
    // Best-effort cleanup only.
  }
}

/* =========================
   SAFE HELPERS
   ========================= */
private String safeId(def obj) {
  if(obj == null) return null
  try {
    if(obj instanceof Map) {
      def v = obj.id ?: obj["id"]
      return v != null ? v.toString() : null
    }
    if(obj.metaClass?.hasProperty(obj, "id")) return obj.id?.toString()
  } catch(e) {}
  return null
}

private String safeName(def obj) {
  if(obj == null) return null
  try {
    if(obj instanceof Map) {
      def v = obj.displayName ?: obj.name ?: obj["displayName"] ?: obj["name"]
      return v != null ? v.toString() : null
    }
    if(obj.metaClass?.hasProperty(obj, "displayName")) return obj.displayName?.toString()
    if(obj.metaClass?.hasProperty(obj, "name")) return obj.name?.toString()
  } catch(e) {}
  return obj?.toString()
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Hubitat Voice - ${appRev()}", install: true, uninstall: true) {
        section() {
            href name: "goInstructions", title: "Open Install Instructions", description: "How to add satellites and more!", page: "instructionPage"
        }
        
        section() {
            paragraph "Be sure to fill out all sections before using this test feature."
            href name: "goTest", title: "Open Test Tool", description: "Type a question and see the answer", page: "testPage"
            href name: "goDiag", title: "Open Diagnostics", description: "See parser stats, rate limits, and last request", page: "diagPage"
        }
        
        section("<b>Devices to include</b>") {}
        section("Master List", hideable: true, hidden: true) {
          	input "qaDevices", "capability.*", title: "Choose Devices - Select the devices you want this app to understand.", multiple: true, required: true, submitOnChange: true
        }

        section("<b>Device Aliases (optional)</b>") {
            input "deviceAliases", "textarea", title: "Aliases - Add nicknames for devices to improve matching. One per line in the form: Device Name = alias1, alias2, alias3", required: false
        }

        section("<b>Satellite Rooms (optional)</b>") {
            paragraph "Map each satellite name to its room so generic requests like 'turn on ceiling light' prefer devices in that room. One per line: satellite-name = room name"
            input "satelliteRooms", "textarea", title: "Satellite to Room Map", required: false
        }

        section("<b>Notify Options</b> (used for Tasker)") {
            input "useTasker", "bool", title: "Use Hubitat Notifications with Tasker", required: false, submitOnChange:true
            if(useTasker) {
        		input "notificationDevice", "capability.notification", title: "Notification Device (for push messages)", required: false, multiple: true, submitOnChange: true, width: 6
        		input "speaker", "capability.speechSynthesis", title: "Speak on this chrome device (optional)", required: false, multiple: true, submitOnChange: true, width: 6
            }
        }

        section("<hr>") {}
        section("<b>Storage Limits</b>") {
            paragraph "To avoid excessive state growth when tracking many devices, the app caps the number of unique (device + attribute + value) keys kept in memory. Oldest keys are evicted first."
            input "maxTrackedKeys", "number", title: "Max tracked keys in state (device+attr+value)", required: false, defaultValue: defaultMaxTrackedKeys(), width:6
            input "dbMaxEvents", "number", title: "DB query max events to scan per question (counts)", required: false, defaultValue: defaultDbMaxEvents(), width:6
        }

        section("<hr>") {}
        section("<b>Behavior</b>") {
            input "weatherLocation", "text", title: "Weather location for generic weather questions (zip or city, optional)", required: false
            
            input "shortTts", "bool", title: "Short voice responses (TTS-friendly)", required: false, defaultValue: true
            input "geminiFallbackEnabled", "bool", title: "Enable AI fallback when HubVoice cannot answer", required: false, defaultValue: false, submitOnChange: true
            if(geminiFallbackEnabled) {
                input "aiFallbackProvider", "enum", title: "AI fallback provider", required: false, defaultValue: defaultFallbackProvider(), submitOnChange: true, options: ["gemini":"Gemini", "chatgpt":"ChatGPT"]
                String fallbackProvider = selectedFallbackProvider()
                if(fallbackProvider == "gemini") {
                  input "geminiAccountTier", "enum", title: "Gemini account tier", required: false, defaultValue: defaultGeminiAccountTier(), options: ["free_tier_1":"Free Tier 1", "tier_1":"Tier 1 (Paid)"]
                  input "geminiApiKey", "password", title: "Gemini API Key", required: false
                  input "geminiModel", "text", title: "Gemini model", required: false, defaultValue: defaultGeminiModel()
                  input "geminiLiveModel", "text", title: "Gemini model for live/news/sports questions (recommended: gemini-2.5-pro for Paid Tier 1)", required: false, defaultValue: defaultGeminiLiveModel()
                } else {
                  input "chatgptApiKey", "password", title: "ChatGPT API Key", required: false
                  input "chatgptModel", "text", title: "ChatGPT model", required: false, defaultValue: defaultChatGptModel()
                  input "chatgptLiveModel", "text", title: "ChatGPT model for live/news/sports questions", required: false, defaultValue: defaultChatGptLiveModel()
                }
                input "sportsPreferredLeagues", "text", title: "Preferred sports leagues (optional, comma-separated: NFL,NBA,MLB,NHL,MLS,EPL,NCAAF,NCAAB)", required: false
                input "sportsPreferredTeams", "text", title: "Preferred sports teams (optional, comma-separated)", required: false
                input "sportsCacheSeconds", "number", title: "Sports response cache seconds", required: false, defaultValue: defaultSportsCacheSeconds()
                if(fallbackProvider == "gemini") {
                  Map stats = getQueryStats()
                  Map costSummary = getGeminiCostSummary()
                  String cacheSize = (state.sportsApiCache instanceof Map) ? "${state.sportsApiCache.size()} entries" : "0 entries"
                  String statsText = "Query Stats: ${stats.totalQueries} total | ESPN: ${stats.bySource?.espn_scoreboard ?: 0} | Gemini: ${stats.bySource?.gemini ?: 0} | Flash: ${stats.geminiModels?.'gemini-2.5-flash' ?: 0} | Pro: ${stats.geminiModels?.'gemini-2.5-pro' ?: 0} | Retries: ${stats.geminiAttempts?.retry_grounded ?: 0} | Cache: ${cacheSize}"
                  String costText = "Estimated Gemini cost: Today ${fmtUsd(costSummary.todayCostUsd as BigDecimal)} | Month ${fmtUsd(costSummary.monthCostUsd as BigDecimal)} | Today tokens in/out: ${costSummary.todayInputTokens ?: 0}/${costSummary.todayOutputTokens ?: 0} | Grounded today: ${costSummary.todayGroundedPrompts ?: 0}"
                  String todayBreakdownText = "Today by model: ${formatGeminiCostBreakdown(costSummary.todayByModel as Map)}"
                  String monthBreakdownText = "Month by model: ${formatGeminiCostBreakdown(costSummary.monthByModel as Map)}"
                  paragraph "<small>${statsText}</small>"
                  paragraph "<small>${costText}</small>"
                  paragraph "<small>${todayBreakdownText}</small>"
                  paragraph "<small>${monthBreakdownText}</small>"
                  input "confirmResetGeminiCostCounters", "bool", title: "I understand these Gemini cost reset actions are destructive", required: false, defaultValue: false, submitOnChange: true
                  if(settings?.confirmResetGeminiCostCounters == true) {
                    paragraph "<small><b>Warning:</b> Choose whether to clear today only, the current month, or all recorded Gemini cost history.</small>"
                    input "resetGeminiCostCountersToday", "button", title: "Reset Today Only"
                    input "resetGeminiCostCountersMonth", "button", title: "Reset Current Month"
                    input "resetGeminiCostCountersAll", "button", title: "Reset All History"
                  }
                  if(state?.lastGeminiCostResetAt) {
                    String resetAt = new Date(state.lastGeminiCostResetAt as Long).format("yyyy-MM-dd HH:mm", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
                    String resetScope = (state.lastGeminiCostResetScope ?: "unknown").toString()
                    paragraph "<small>Last Gemini cost reset: ${resetAt} (${resetScope})</small>"
                  }
                  paragraph "<small>Estimate uses Gemini 2.5 public pricing as of 3/22/26 and recorded token usage. Actual Google billing can differ slightly.</small>"
                  paragraph "Only used when HubVoice cannot answer a non-control question. Responses are limited to short 1-4 sentence replies. Sign up and create a key in Google AI Studio: <a href='https://aistudio.google.com/' target='_blank'>aistudio.google.com</a>"
                } else {
                  Map stats = getQueryStats()
                  Map costSummary = getChatGptCostSummary()
                  String cacheSize = (state.sportsApiCache instanceof Map) ? "${state.sportsApiCache.size()} entries" : "0 entries"
                  String modelMini = defaultChatGptModel()
                  String modelLive = defaultChatGptLiveModel()
                  String statsText = "Query Stats: ${stats.totalQueries} total | ESPN: ${stats.bySource?.espn_scoreboard ?: 0} | ChatGPT: ${stats.bySource?.chatgpt ?: 0} | ${modelMini}: ${stats.chatgptModels?.get(modelMini) ?: 0} | ${modelLive}: ${stats.chatgptModels?.get(modelLive) ?: 0} | Retries: ${stats.chatgptAttempts?.retry_grounded ?: 0} | Cache: ${cacheSize}"
                  String costText = "Estimated ChatGPT cost: Today ${fmtUsd(costSummary.todayCostUsd as BigDecimal)} | Month ${fmtUsd(costSummary.monthCostUsd as BigDecimal)} | Today tokens in/out: ${costSummary.todayInputTokens ?: 0}/${costSummary.todayOutputTokens ?: 0} | Grounded today: ${costSummary.todayGroundedPrompts ?: 0}"
                  String todayBreakdownText = "Today by model: ${formatGeminiCostBreakdown(costSummary.todayByModel as Map)}"
                  String monthBreakdownText = "Month by model: ${formatGeminiCostBreakdown(costSummary.monthByModel as Map)}"
                  paragraph "<small>${statsText}</small>"
                  paragraph "<small>${costText}</small>"
                  paragraph "<small>${todayBreakdownText}</small>"
                  paragraph "<small>${monthBreakdownText}</small>"
                  input "confirmResetChatGptCostCounters", "bool", title: "I understand these ChatGPT cost reset actions are destructive", required: false, defaultValue: false, submitOnChange: true
                  if(settings?.confirmResetChatGptCostCounters == true) {
                    paragraph "<small><b>Warning:</b> Choose whether to clear today only, the current month, or all recorded ChatGPT cost history.</small>"
                    input "resetChatGptCostCountersToday", "button", title: "Reset Today Only"
                    input "resetChatGptCostCountersMonth", "button", title: "Reset Current Month"
                    input "resetChatGptCostCountersAll", "button", title: "Reset All History"
                  }
                  if(state?.lastChatGptCostResetAt) {
                    String resetAt = new Date(state.lastChatGptCostResetAt as Long).format("yyyy-MM-dd HH:mm", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
                    String resetScope = (state.lastChatGptCostResetScope ?: "unknown").toString()
                    paragraph "<small>Last ChatGPT cost reset: ${resetAt} (${resetScope})</small>"
                  }
                  paragraph "<small>Estimate uses OpenAI public pricing assumptions for GPT-4.1/GPT-4.1-mini and recorded token usage. Actual billing can differ slightly.</small>"
                  paragraph "Only used when HubVoice cannot answer a non-control question. Responses are limited to short 1-4 sentence replies. Create an API key in OpenAI Platform: <a href='https://platform.openai.com/' target='_blank'>platform.openai.com</a>"
                }
            }
            input "maxRequestsPerMinute", "number", title: "Rate limit: max /ask requests per minute (0 = disable)", required: false, defaultValue: defaultMaxRequestsPerMinute()
            
            input "securityCode", "text", title:"Security code for risky actions (optional)", required:false
            
            input "useGenericDeny", "bool", title: "Security: Use generic denial message for risky actions", required: false, defaultValue: true, submitOnChange:true
            if(useGenericDeny) {
            	input "genericDenyMsg", "text", title: "Generic denial message", required: false, defaultValue: "Unable to complete request."
            }
            
            input "bfEnabled", "bool", title: "Security: Brute-force protection for risky actions", required: false, defaultValue: true, submitOnChange:true
            if(bfEnabled) {
            	input "bfMaxFails", "number", title: "Brute-force: Max failed code attempts", required: false, defaultValue: defaultBfMaxFails(), width:4
            	input "bfWindowMins", "number", title: "Brute-force: Failure window (minutes)", required: false, defaultValue: defaultBfWindowMins(), width:4
            	input "bfLockoutMins", "number", title: "Brute-force: Lockout duration (minutes)", required: false, defaultValue: defaultBfLockoutMins(), width:4
            }

            input "enforceRiskyLockAllowlist", "bool", title: "Security: Restrict risky lock actions to allowlist", required: false, defaultValue: false, submitOnChange:true
            if(enforceRiskyLockAllowlist) {
            	input "riskyLockDevices", "capability.lock", title: "Risky lock allowlist", required: false, multiple: true
            }

            input "allowHsmControl", "bool", title: "Allow voice to arm/disarm HSM (requires security code)", required: false, defaultValue: false
            input "allowHsmArmHomeVoice", "bool", title: "Allow voice HSM arm home/night", required: false, defaultValue: true, submitOnChange:true
            if(allowHsmArmHomeVoice) {
                input "hsmArmHomeValue", "enum", title: "HSM arm home value", required: false, options: ["armHome","armNight"], defaultValue: "armHome"
            }
            
            input "allowHsmArmAwayVoice", "bool", title: "Allow voice HSM arm away", required: false, defaultValue: true, submitOnChange:true
            if(allowHsmArmAwayVoice) {
                input "hsmArmAwayValue", "enum", title: "HSM arm away value", required: false, options: ["armAway"], defaultValue: "armAway"
            }
            input "allowHsmDisarmVoice", "bool", title: "Allow voice HSM disarm", required: false, defaultValue: true

            input "riskyAuditMax", "number", title: "Risky action audit entries to keep", required: false, defaultValue: defaultRiskyAuditMax()
            input "replayProtectionEnabled", "bool", title: "Security: Replay protection for risky actions", required: false, defaultValue: false
            input "replayWindowSecs", "number", title: "Replay window (seconds)", required: false, defaultValue: defaultReplayWindowSecs()
            input "replayNonceTtlMins", "number", title: "Replay nonce retention (minutes)", required: false, defaultValue: defaultReplayNonceTtlMins()
            
            input "lowBatteryThreshold", "number", title: "Battery low threshold (%)", required: false, defaultValue: defaultLowBatteryThreshold()
            input "offlineWindowHours", "number", title: "Default offline/stale window (hours)", required: false, defaultValue: defaultOfflineWindowHours()
        }

        section("<hr>") {}
        section("<b>Endpoint Info</b> - Reminder: Do NOT share your token with anyone or they will be able to control your system.") {
            input "showEndpoints", "bool", title: "Show the endpoints", required: false, defaultValue: false, submitOnChange: true
            if(showEndpoints) {
                ensureAccessToken()
                def appId = app.id
                def token = state.accessToken

                def localBase = getFullLocalApiServerUrl() ?: "http://<hub-ip>"
                def cloudBase = getFullApiServerUrl() ?: "https://cloud.hubitat.com"

                def localAsk  = "${localBase}/ask?q=%avcomm&access_token=${token}"
                def localPing = "${localBase}/ping?access_token=${token}"
                def localHealth = "${localBase}/health?access_token=${token}"
                def localTasker = "${localBase}/ask?access_token=${token}&d=tasker&q=%avcomm"
                def localMini = "${localBase}/ask?access_token=${token}&d=mini&q={query}"

                // Cloud base from getFullApiServerUrl() already includes /api/<cloudHubId>
                // So DO NOT add /api/<id> again.
                def cloudAsk  = "${cloudBase}/ask?q=%avcomm&access_token=${token}"
                def cloudPing = "${cloudBase}/ping?access_token=${token}"
                def cloudHealth = "${cloudBase}/health?access_token=${token}"
                def cloudTasker = "${cloudBase}/ask?access_token=${token}&d=tasker&q=%avcomm"
                def cloudMini  = "${cloudBase}/ask?access_token=${token}&d=mini&q={query}"

                paragraph "<b>Local Ask:</b> ${localAsk}"
                paragraph "<b>Local Ping:</b> ${localPing}"
                paragraph "<b>Local Health:</b> ${localHealth}"
                paragraph "<b>Local HubVoiceTasker:</b> ${localTasker}"
                paragraph "<b>Local HubVoiceMini app:</b> ${localMini}"
                paragraph ""
                paragraph "<b>Cloud Ask:</b> ${cloudAsk}"
                paragraph "<b>Cloud Ping:</b> ${cloudPing}"
                paragraph "<b>Cloud Health:</b> ${cloudHealth}"
                paragraph "<b>Cloud HubVoiceTasker:</b> ${cloudTasker}"
                paragraph "<b>Cloud HubVoiceMini app:</b> ${cloudMini}"
            }
        }

        section("<hr>") {}
        section("<b>Self-Test</b>") {
            ensureAccessToken()
            input "selfTestEnabled", "bool", title: "Enable /selftest endpoint", required: false, defaultValue: false, submitOnChange: true
            if(selfTestEnabled) {
                def token = state.accessToken
                def localBase = getFullLocalApiServerUrl() ?: "http://<hub-ip>"
                def cloudBase = getFullApiServerUrl() ?: "https://cloud.hubitat.com"

                String qStk = ""
                try {
                  String stk = (settings?.selfTestKey ?: "").toString().trim()
                  if(stk) qStk = "&stk=${java.net.URLEncoder.encode(stk, 'UTF-8')}"
                } catch(e) {}

                def localSelf = "${localBase}/selftest?view=html&access_token=${token}${qStk}"
                def cloudSelf = "${cloudBase}/selftest?view=html&access_token=${token}${qStk}"

                paragraph "<a target='_blank' rel='noopener' href='${localSelf}' style='display:inline-block;padding:8px 12px;border-radius:8px;background:#1f6feb;color:#fff !important;-webkit-text-fill-color:#fff;text-decoration:none;'>Run Local Self-Test</a> - <a target='_blank' rel='noopener' href='${cloudSelf}' style='display:inline-block;padding:8px 12px;border-radius:8px;background:#0a7f3f;color:#fff !important;-webkit-text-fill-color:#fff;text-decoration:none;'>Run Cloud Self-Test</a>"
                paragraph "Opens in a new tab with a readable report."
            }
        }
        
        section("<hr>") {
        	bMes =  "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>HubVoice - ${appRev()}"
        	bMes += "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a> <img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/venmo.png' width='100'></div>"
        	paragraph bMes
        }
	}
}

def testPage() {
    String diagJson = ""
    try {
      diagJson = JsonOutput.prettyPrint(JsonOutput.toJson(state?.lastDebug ?: [:]))
    } catch(e) {
      try { diagJson = (state?.lastDebug ?: [:]).toString() } catch(ex) { diagJson = "" }
    }
      dynamicPage(name: "testPage", title: "Test Voice Question", uninstall: false, install: false) {
        section("") {
          // reset paragraph margins so our custom HTML boxes have no gaps
          paragraph "<style>p {margin:0 !important; padding:0 !important;}</style>"
        }
        section("Question") {
          input "testQuestion", "text",
            title: "Type a question (include the device name)",
            required: false,
            submitOnChange: true
          paragraph "Canary examples: house status | run a security check | is front door open | turn all bathroom lights off"
        }

        def q = (settings?.testQuestion ?: "").toString().trim()
        if(q) {
          dbgInit(q, "testPage")  // Initialize debug state
          String query = normalize(q)
          String ansOut = null
          def nluIntent = null
          boolean testBulkHandled = false

          def bulkCmd = parseBulkLightCommand(q)
          if(bulkCmd) {
            String action = (bulkCmd.action ?: "").toString().toLowerCase()
            String scope = (bulkCmd.scope ?: "").toString().trim()
            try {
              state.lastDebug.mode = "control_bulk"
              state.lastDebug.bulk = state.lastDebug.bulk ?: [:]
              state.lastDebug.bulk.type = 'lights'
              state.lastDebug.bulk.action = action
              state.lastDebug.bulk.scope = scope
              state.lastDebug.bulk.scopeTokens = bcTokens(scope)
              state.lastDebug.bulk.nameMustContainAny = ['light','lamp']
            } catch(e) {}

            def targets = bulkFindLightsByName(scope)
            try {
              state.lastDebug.bulk.targetsCount = targets?.size() ?: 0
              state.lastDebug.bulk.targets = (targets ?: []).collect{ it?.displayName ?: it?.name ?: '?' }
            } catch(e) {}
            dbgRoute('test_bulk_lights_match')
            ansOut = bulkDoLights(action, targets)
            testBulkHandled = true
          }

          if(!testBulkHandled) {
            def constraint = inferDeviceConstraint(query, null)
            def dev = matchDeviceFromQuery(query, constraint)

    if(!dev) {
      // Try group questions that don't name a specific device (e.g., "are any doors open")
      def gIntent = detectGroupIntent(query)
      if(gIntent) {
        dbgRoute("detectGroupIntent")
        boolean risky = (gIntent?.risky == true) || (gIntent?.mode in ["hsm_arm_home","hsm_arm_away","hsm_disarm"])
        if(risky) {
          // Risky group actions require security code (TEST PAGE only; do not early-return from the page)
          if(bruteForceLockedOut(null)) {
            ansOut = riskyDeny("Too many failed attempts. Try again later.")
          } else {
            String appCodeSet = (settings?.securityCode ?: "").toString().trim()
            String codeProvided = extractSecurityCode(q)
            if(!appCodeSet) {
              ansOut = riskyDeny("Security code is not configured.")
            } else if(!codeProvided) {
              bruteForceRecordFail(null)
              ansOut = riskyDeny("Please include the security code.")
            } else if(codeProvided != appCodeSet) {
              bruteForceRecordFail(null)
              ansOut = riskyDeny("Security code incorrect.")
            } else {
              bruteForceReset(null)
              def gRes = answerForGroup(query, gIntent)
              ansOut = (gRes?.answer ?: "No answer.").toString()
            }
          }
        } else {
          def gRes = answerForGroup(query, gIntent)
          ansOut = (gRes?.answer ?: "No answer.").toString()
        }
      } else {
        ansOut = "I couldn't find a matching device. Make sure the device name appears in the question and the device is selected in the app."
      }
    } else {
      def intent
      if(nluIntent?.mode in ["device_status","duration_query","why_did_action"]) {
        intent = [mode:nluIntent.mode]
      } else {
        intent = detectIntent(query, dev)
      }
      dbgRoute("answerFor")
      if(intent?.mode) {
        state.lastDebug = state.lastDebug ?: [:]
        state.lastDebug.mode = intent.mode
        state.lastDebug.intent = intent.toString()
      }
      if(dev?.displayName) {
        state.lastDebug = state.lastDebug ?: [:]
        state.lastDebug.dev = dev.displayName
      }
      def result = answerFor(dev, query, intent)
      ansOut = (result?.answer ?: "No answer.").toString()
    }
      }
      // single line, UI-safe
      ansOut = ansOut
        .replaceAll(/[\r\n\t]+/, " ")
        .replace('\u00A0' as char, ' ' as char)
        .replaceAll(/\s+/, " ")
        .trim()

      diagJson = ""
      try {
        diagJson = JsonOutput.prettyPrint(JsonOutput.toJson(state?.lastDebug ?: [:]))
      } catch(e) {
        try { diagJson = (state?.lastDebug ?: [:]).toString() } catch(ex) { diagJson = "" }
      }

        // --- Debug Summary (most useful fields) ---
      def dbg = state?.lastDebug ?: [:]
      String dbgSummary = """Q: ${htmlEscape((dbg.q ?: '') as String)}
Mode: ${htmlEscape((dbg.mode ?: '') as String)}   Intent: ${htmlEscape((dbg.intent ?: '') as String)}
Dev: ${htmlEscape((dbg.dev ?: '') as String)}   Best: ${htmlEscape((dbg.bestDev ?: '') as String)}
Bulk: ${htmlEscape((dbg.bulk ?: [:]).toString())}
Error: ${htmlEscape((dbg.error ?: '') as String)}"""
      section("Answer") {
          block =  "<div style='margin-top:-8px; border-radius:12px; background:rgba(0,0,0,.06); border:1px solid rgba(0,0,0,.12);'>"
          block += "<div style='font-weight:700;'>Result</div>"
          block += "<div style='font-size:14px; line-height:1.35;'>${htmlEscape(ansOut)}</div><br>"
          block += "<div style='font-weight:700;'>Debug Summary</div>"
          block += "<pre style='word-break:break-word; margin:5; font-size:12px; line-height:1.35;'>${dbgSummary}</pre>"
          block += "</div>"
          
          block2 =  "<div style='margin-top:-8px; padding:12px; border-radius:12px; background:#f7f7f7; border:1px solid #e2e2e2;'>"
          block2 += "<div style='font-weight:700; margin-bottom:6px;'>Diagnostics JSON</div>"
          block2 += "<pre style='white-space:pre-wrap; word-break:break-word; margin:0; font-size:12px; line-height:1.35;'>${htmlEscape(diagJson)}</pre>"
          block2 += "</div>"
          
        paragraph block
        paragraph block2
      }
    } else {
      section("") {
          block2 =  "<div style='margin-top:-8px; padding:12px; border-radius:12px; background:#f7f7f7; border:1px solid #e2e2e2;'>"
          block2 += "<div style='font-weight:700; margin-bottom:6px;'>Diagnostics JSON</div><br>"
          block2 += "<pre style='white-space:pre-wrap; word-break:break-word; margin:0; font-size:12px; line-height:1.35;'>${htmlEscape(diagJson)}</pre>"
          block2 += "</div>"
          
        paragraph "Enter a question above to see the answer here."
        paragraph block2
      }
    }
  }
}

def diagPage() {
  dynamicPage(name: "diagPage", title: "Diagnostics", uninstall: false, install: false) {
    section("Stats") {
      def devCt = (settings?.qaDevices ?: [])?.size() ?: 0
      def keysCt = (state?.keyOrder ?: [])?.size() ?: 0
      def cacheCt = (state?.lastByDevAttrValue ?: [:])?.size() ?: 0
      def askCt = (state?.askTimes ?: [])?.size() ?: 0
      String aiProvider = selectedFallbackProvider()
      paragraph "<b>Devices selected:</b> ${devCt}"
      paragraph "<b>Tracked keys (LRU list):</b> ${keysCt} / ${(settings?.maxTrackedKeys ?: 5000)}"
      paragraph "<b>lastByDevAttrValue device buckets:</b> ${cacheCt}"
      paragraph "<b>Requests in last minute (rate-limit window):</b> ${askCt} / ${(settings?.maxRequestsPerMinute ?: 30)}"
      paragraph "<b>Risky audit entries:</b> ${(state?.riskyAudit ?: [])?.size() ?: 0} / ${(settings?.riskyAuditMax ?: 200)}"
      paragraph "<b>AI fallback provider:</b> ${htmlEscape(aiProvider)}"
      paragraph "<b>App revision:</b> ${appRev()}"
    }
    section("Last request") {
      paragraph "<b>Last question:</b> ${htmlEscape((state?.lastQuestion ?: '') as String)}"
      paragraph "<b>Last intent:</b> ${htmlEscape((state?.lastIntent ?: '') as String)}"
      paragraph "<b>Last device:</b> ${htmlEscape((state?.lastDeviceName ?: '') as String)}"
      paragraph "<b>Last answer:</b> ${htmlEscape((state?.lastAnswer ?: '') as String)}"
      paragraph "<b>Last AI provider:</b> ${htmlEscape((state?.lastAiProvider ?: '') as String)}"
      paragraph "<b>Last AI model:</b> ${htmlEscape((state?.lastGeminiModel ?: state?.lastAiModel ?: '') as String)}"
      paragraph "<b>Last AI grounded:</b> ${state?.lastAiGrounded == true ? 'yes' : 'no'}"
      if(state?.lastError) paragraph "<b>Last error:</b> ${htmlEscape((state?.lastError ?: '') as String)}"
    }
    section("Risky audit (latest 10)") {
      def ra = ((state?.riskyAudit ?: []) as List)
      def recent = ra ? ra.takeRight(10).reverse() : []
      if(!recent) {
        paragraph "No risky actions logged."
      } else {
        recent.each { e ->
          def ts = null
          try { ts = (e?.ts instanceof Number) ? fmtWhen((e.ts as Long)) : "at an unknown time" } catch(ignored) { ts = "at an unknown time" }
          def okTxt = (e?.ok == true) ? "OK" : "DENY"
          def action = (e?.action ?: "").toString()
          def target = (e?.target ?: "").toString()
          def reason = (e?.reason ?: "").toString()
          paragraph "<b>${okTxt}</b> ${htmlEscape(action)} ${htmlEscape(target)} ${htmlEscape(ts)} (${htmlEscape(reason)})"
        }
      }
    }
    section("Controls") {
      input "clearDiagnostics", "bool", title: "Clear diagnostic state on Save", required: false, submitOnChange: true
      input "clearRiskyAudit", "bool", title: "Clear risky audit on Save", required: false, submitOnChange: true
      if(settings?.clearDiagnostics) {
        state.remove("lastQuestion"); state.remove("lastIntent"); state.remove("lastDeviceName"); state.remove("lastAnswer"); state.remove("lastError")
        state.askTimes = []
      }
      if(settings?.clearRiskyAudit) {
        state.riskyAudit = []
      }
      paragraph "Tip: Disable clear toggles after using them."
    }
  }
}

def instructionPage() {
	dynamicPage(name: "instructionPage", title: "Instructions", uninstall: false, install: false) {
        section("<hr>") {}
        section("<b>Testing</b>") {
            paragraph "Test, Test... Is this thing on?"
            test =  "- After filling out all fields and clicking 'Done'<br>"
            test += "- Go back in to HubVoice and choose 'Open Test Tool'<br>"
            test += "- Type in whatever you want and hit enter or click outside the box<br>"
            test += "- Like magic, your answer appears below and/or the devices changed!"
            paragraph test
        }
        
        section("<hr>") {}
        section("<b>Tasker for Android</b>") {
            paragraph "With Tasker, you can get answers or control you home with voice from anywhere!"
            tasker =  "- Requires Tasker and the plugin Autovoice installed on you Android device<br>"
            tasker += "- I've uploaded my xml (ask_hubitat.xml) on <a href='https://www.dropbox.com/scl/fo/aga0cmmfaad55ycbkqztg/AApwFgaRanzVyaxL6fvbA9c?rlkey=cwiyrfi7bp1jnxb6m4eoxop9m&st=liank6au&dl=0' target='_blank'>Dropbox</a>. You can import that in to Tasker<br>"
            tasker += "- Once imported, the only thing you should have to change is the 'app URL' found within the Hubitat app<br>"
            tasker += "- Once you have this working, you can make a widget for it, so you have quick access!"
            paragraph tasker
        }
        
        section("<hr>") {}
        section("<b>HubVoiceMini Text for Windows</b>") {
            paragraph "HubVoiceMini is a small Windows desktop utility that lets you quickly send typed commands to your HubVoice / Hubitat voice endpoint. A fast keyboard alternative when voice control isn’t convenient."
            mini =  "- Download the file (HubVoiceMini_Setup.exe) from <a href='https://www.dropbox.com/scl/fo/aga0cmmfaad55ycbkqztg/AApwFgaRanzVyaxL6fvbA9c?rlkey=cwiyrfi7bp1jnxb6m4eoxop9m&st=liank6au&dl=0' target='_blank'>Dropbox</a>.<br>"
            mini += "- Double click the file to install"
            paragraph mini
        }
        
        section("<hr>") { paragraph "Now for the fun stuff!"}
        section("<b>Requirements for Voice Control</b>") {
            req =  "- An always on Win PC running HubVoiceSat<br>"
            req += "- At least one compatible Satellite speaker (running custom HubVoiceSat firmware):<br>"
            req += "  -- HA Voice Satellite Preview Edition<br>"
            req += "  -- Future Proof Homes Satellite-1"
            paragraph req
        }
        
        section("<b>How to Install HubVoiceSat on Win PC</b>") {
            winPC =  "- Download the lastest zip (hubvoiced-sat=2026.xx.xx.x=release.zip) from <a href='https://www.dropbox.com/scl/fo/aga0cmmfaad55ycbkqztg/AApwFgaRanzVyaxL6fvbA9c?rlkey=cwiyrfi7bp1jnxb6m4eoxop9m&st=liank6au&dl=0' target='_blank'>Dropbox</a>, using the always on Win PC<br>"
            winPC += "- Unzip the folder, then copy all of its contents to c:\\HubViewSat - This needs to be our working directory"
            paragraph winPC
        }
        
        section("<b>Flashing the HA Voice Assistant PE for the First Time</b>") {
            firstFlash =  "- Plug the Home Assistant Voice Preview Edition into your Windows PC with a USB data cable.<br>"
            firstFlash += "- Open your c:\\HubVoiceSat and open-usb-flash-page.bat, or go to https://web.esphome.io/.<br>"
            firstFlash += "- Click Connect.<br>"
            firstFlash += "- Pick the HA PE serial/USB device from the browser prompt.<br>"
            firstFlash += "- If the page asks, allow USB access.<br>"
            firstFlash += "- Choose Install.<br>"
            firstFlash += "- When it asks for a file, be SURE to use the correct one:<br>"
            firstFlash += "  -- HA Volice Satellite PE - select hubvoice-sat-xxxxx-factory.bin.<br>"
            firstFlash += "- Wait for the flash to finish and let the device reboot.<br><br>"
            firstFlash += "- Now click the '3 vertical dots'<br>"
            firstFlash += "- Select 'Configure Wi-Fi'"
            firstFlash += "- Enter in your Wi-Fi credentials"
            firstFlash += "- It'll spin and come back with 'Provisioned!'<br>"
            firstFlash += "- Now click 'Visit Device' and enter in your WiFi Setup SSID/Password, to make sure that it's saved.<br>"
            firstFlash += "- You can now close the esphome page.<br>"
            firstFlash += "For later updates, do not use the factory file again. Use a hubvoice-sat-xxxx-ota.bin with the device’s web page OTA upload option."
            paragraph firstFlash
        }
        
        section("<b>Setting up FPH Satellite-1 For the First Time</b>") {
            fphFlash =  "- Plug in the satellite, be sure to use the adapter that came with the satellite<br>"
            fphFlash += "- Visit the website: https://www.improv-wifi.com/<br>"
            fphFlash += "- Look for 'Improv via BLE' and click the blue button 'Connect Device to Wi-Fi'<br>"
            fphFlash += "- A list of devices will popup. Click the 'Satellite1-xxxx and click 'Pair'<br>"
            fphFlash += "- It'll then start to spin, press the 'control' button (right button) on the Sat-1<br>"
            fphFlash += "- Back on the screen, it'll ask you for your Wi-Fi credentials<br>"
            fphFlash += "- Once entered, you should see a success message.<br><br>"
            fphFlash += "You are now ready to flash your sat-1<br>"
            fphFlash += "- Plug the sat-1 into your computers USB<br>"
            fphFlash += "- Visit the website: https://web.esphome.io/<br>"
            fphFlash += "- Be sure to flash the hubvoice-sat-fph-xxxxxx-factory.bin<br>"
            fphFlash += "- Wait for it to finish rebooting. Then disconnect it form the computer.<br>"
            fphFlash += "- Plug it back into the wall using the supplied adapter.<br>"
			paragraph fphFlash
        }
        
        section("<b>Ready to run</b>") {
            runit =  "- In c:\\HubVoiceSat, double click on HubVoiceSat.exe<br>"
            runit += "- Be patient, it can take a few seconds to open up<br>"
            runit += "- At this point, you should see a webpage. Have fun!"
            paragraph runit
        }
        
        section("<b>To add a Satellite to HubVoiceSat</b>") {
            addIt =  "- Open HubVoiceSat<br>"
            addIt += "- Scroll down to Satellites and enter in your new details<br>"
            addIt += "- Click 'Save'"
            paragraph addIt
        }
            
        section("<b>Future Updates</b>") {
            future =  "<b>Hubitat App Updates<<b> - Available through Hubitat Package Manager<br>"
            future += "<b>All EXE Updates</b> -  Available through downloads on <a href='https://www.dropbox.com/scl/fo/aga0cmmfaad55ycbkqztg/AApwFgaRanzVyaxL6fvbA9c?rlkey=cwiyrfi7bp1jnxb6m4eoxop9m&st=liank6au&dl=0' target='_blank'>Dropbox</a>.<br>"
            future += "<b>Satellite Updates</b> - Available through the webserver satellite page.<br>"
            future += "With, of course, a forum posting announcing the update availability!"
            paragraph future
        }
        section("<hr>") {}
  	}
}

/* ---------------- Helpers: Rate limiting + parsing ---------------- */

private boolean rateLimitOk() {
  Integer maxPerMin = safeInt(settings?.maxRequestsPerMinute, defaultMaxRequestsPerMinute())
  if(maxPerMin == null) maxPerMin = defaultMaxRequestsPerMinute()
  if(maxPerMin <= 0) return true
  Long now = now()
  state.askTimes = (state.askTimes ?: []).findAll { t -> (t instanceof Number) && (now - (t as Long) < 60000L) }
  if((state.askTimes as List).size() >= maxPerMin) return false
  (state.askTimes as List) << now
  return true
}


private Double safeDouble(def v, Double dflt=null) {
  try { return (v == null) ? dflt : (v as Double) } catch(e) { return dflt }
}

private boolean hasWord(String q, String w) {
  if(!q || !w) return false
  return (q =~ (/\b${java.util.regex.Pattern.quote(w.toLowerCase())}\b/)).find()
}


private List deviceMatchesFromQuery(String query) {
  query = normalize((query ?: "").toString())
  def idx = (state?.devIndex ?: buildDeviceIndex()) ?: [:]   // normalizedName -> deviceId
  def pool = (qaDevices ?: [])
  List matches = []

  idx.each { nameNorm, did ->
    try {
      String n = (nameNorm ?: "").toString()
      if(!n) return
      def d = pool.find { it?.id?.toString() == did?.toString() }
      if(!d) return

      // exact contains check
      if(query.contains(n)) {
        matches << d
      } else {
        // token overlap heuristic
        def toks = n.split(/\s+/).findAll { it }
        int hit = toks.count { t -> t.size() > 2 && query.contains(t) }
        if(hit >= Math.min(2, toks.size())) matches << d
      }
    } catch(e) { }
  }

  // de-dupe by id, preserve order
  def seen = [:]
  List out = []
  matches.each { m ->
    def id = m?.id?.toString()
    if(id && !seen[id]) { seen[id] = true; out << m }
  }
  return out
}

private Map disambiguateOrPick(String query) {
  List ms = deviceMatchesFromQuery(query)
  if(!ms || ms.size()==0) return [device:null, candidates:[]]
  if(ms.size()==1) return [device: ms[0], candidates:[]]
  // try prefer longest name match
  def best = null
  int bestLen = -1
  ms.each { m ->
    String n = normalize(m?.displayName ?: m?.name ?: "")
    if(query.contains(n) && n.size() > bestLen) { best = m; bestLen = n.size() }
  }
  if(best) return [device:best, candidates:[]]
  // ambiguous
  return [device:null, candidates: ms.take(5)]
}

private String fmtDurationMs(Long ms) {
  if(ms == null || ms < 0) return ""
  long s = Math.round(ms/1000.0)
  long d = (long)(s/86400); s%=86400
  long h = (long)(s/3600);  s%=3600
  long m = (long)(s/60);    s%=60
  List parts=[]
  if(d) parts << "${d} day${d==1?'':'s'}"
  if(h) parts << "${h} hour${h==1?'':'s'}"
  if(m) parts << "${m} minute${m==1?'':'s'}"
  if(!d && !h && !m) parts << "${s} second${s==1?'':'s'}"
  return parts.join(", ")
}

mappings {
  path("/ask")   	{ action: [GET: "handleAsk"] }
  path("/ping") 	{ action: [GET: "handlePing"] }
  path("/devices") 	{ action: [GET: "handleDevices"] }
  path("/selftest") { action: [GET: "handleSelfTest"] }
  path("/health") 	{ action: [GET: "handleHealth"] }
}

/* ---------------- Lifecycle ---------------- */

def installed() {
  initialize()
}

def updated() {
  unsubscribe()
  initialize()
    List sats = []
    Integer i = 1
    while(settings["satName_${i}"]) {
        sats << [
            name: settings["satName_${i}"],
            ip  : settings["satIP_${i}"]
        ]
        i++
    }
    state.satellites = sats
}

def appButtonHandler(btn) {
  if(["resetGeminiCostCountersToday", "resetGeminiCostCountersMonth", "resetGeminiCostCountersAll"].contains(btn)) {
    if(settings?.confirmResetGeminiCostCounters != true) {
      log.warn "Ignored Gemini cost reset request because confirmation was not enabled."
      return
    }
    Integer removed = 0
    String scopeLabel = ""
    if(btn == "resetGeminiCostCountersToday") {
      removed = clearGeminiCostToday()
      scopeLabel = "today"
    } else if(btn == "resetGeminiCostCountersMonth") {
      removed = clearGeminiCostCurrentMonth()
      scopeLabel = "current month"
    } else if(btn == "resetGeminiCostCountersAll") {
      removed = clearGeminiCostAllHistory()
      scopeLabel = "all history"
    }
    log.info "Gemini cost counters reset for ${scopeLabel}. Removed ${removed} day entr${removed == 1 ? 'y' : 'ies'}."
    return
  }

  if(["resetChatGptCostCountersToday", "resetChatGptCostCountersMonth", "resetChatGptCostCountersAll"].contains(btn)) {
    if(settings?.confirmResetChatGptCostCounters != true) {
      log.warn "Ignored ChatGPT cost reset request because confirmation was not enabled."
      return
    }
    Integer removed = 0
    String scopeLabel = ""
    if(btn == "resetChatGptCostCountersToday") {
      removed = clearChatGptCostToday()
      scopeLabel = "today"
    } else if(btn == "resetChatGptCostCountersMonth") {
      removed = clearChatGptCostCurrentMonth()
      scopeLabel = "current month"
    } else if(btn == "resetChatGptCostCountersAll") {
      removed = clearChatGptCostAllHistory()
      scopeLabel = "all history"
    }
    log.info "ChatGPT cost counters reset for ${scopeLabel}. Removed ${removed} day entr${removed == 1 ? 'y' : 'ies'}."
  }
}

def initialize() {
  ensureAccessToken()

  state.devIndex = buildDeviceIndex()
  state.satelliteRoomMap = buildSatelliteRoomMap()
  state.lastByDevAttrValue = state.lastByDevAttrValue ?: [:] // [devId:[attr:[valueStr:ts]]]
  state.keyOrder = state.keyOrder ?: []  // LRU-ish list of "devId|attr|value"
  state.keySet   = state.keySet   ?: [:] // map for fast contains

  // Subscribe to all events for selected devices.
  qaDevices?.each { d ->
    try {
      subscribe(d, "*", "onDeviceEvent")
    } catch(e) {
      // Some drivers may not like wildcard; fallback: subscribe to common attrs.
      ["switch","contact","motion","lock","presence","temperature","humidity","battery","water","acceleration"].each { a ->
        try { subscribe(d, a, "onDeviceEvent") } catch(ignored) {}
      }
    }
  }
}

/* ---------------- Event tracking ---------------- */

def onDeviceEvent(evt) {
  if(!evt) return

  def devId = (evt.deviceId ?: evt.device?.id)?.toString()
  if(!devId) return

  def attr = (evt.name ?: "").toString()
  def val  = (evt.value ?: "").toString()
  if(!attr) return

  def ts = now()

  // Track "last time this device+attr had THIS value"
  def byAttrVal = state.lastByDevAttrValue[devId] ?: [:]
  def valMap = byAttrVal[attr] ?: [:]

  boolean isNewKey = !valMap.containsKey(val)
  valMap[val] = ts

  byAttrVal[attr] = valMap
  state.lastByDevAttrValue[devId] = byAttrVal

  // LRU-ish key cap: unique key = devId|attr|val
  if(isNewKey) {
    def k = "${devId}|${attr}|${val}"
    if(!state.keySet?.get(k)) {
      state.keyOrder = (state.keyOrder ?: [])
      state.keySet   = (state.keySet   ?: [:])
      state.keyOrder << k
      state.keySet[k] = 1
      pruneTrackedKeysIfNeeded()
    }
  }
}

def pruneTrackedKeysIfNeeded() {
  Integer maxKeys = getMaxTrackedKeys()
  if(!maxKeys || maxKeys < 100) maxKeys = 100

  def order = (state.keyOrder ?: [])
  def setm  = (state.keySet   ?: [:])

  // Evict oldest keys until under the cap
  while(order.size() > maxKeys) {
    def k = order.remove(0)
    setm.remove(k)

    // Remove from main map as well
    try {
      def parts = k.split("\\|", 3)
      if(parts?.size() == 3) {
        def devId = parts[0]
        def attr  = parts[1]
        def val   = parts[2]
        def byAttrVal = state.lastByDevAttrValue[devId]
        if(byAttrVal instanceof Map) {
          def valMap = byAttrVal[attr]
          if(valMap instanceof Map) {
            valMap.remove(val)
            if(valMap.isEmpty()) byAttrVal.remove(attr)
          }
          if(byAttrVal.isEmpty()) state.lastByDevAttrValue.remove(devId)
        }
      }
    } catch(ignored) {}
  }

  state.keyOrder = order
  state.keySet   = setm
}

private Integer getMaxTrackedKeys() {
  try {
    def v = settings?.maxTrackedKeys
    if(v == null || v.toString().trim() == "") return 5000
    return (v as Integer)
  } catch(e) {
    return 5000
  }
}

private Integer getDbMaxEvents() {
  try {
    def v = settings?.dbMaxEvents
    if(v == null || v.toString().trim() == "") return 2000
    Integer n = (v as Integer)
    if(n < 100) n = 100
    if(n > 20000) n = 20000
    return n
  } catch(e) {
    return 2000
  }
}

/* ---------------- Handlers ---------------- */

def handlePing() {
  log.debug "PING HIT!"
  render contentType: "text/plain", data: "pong"
}

def handleHealth() {
  if(!isAuthorizedRequest()) {
    return render(contentType: "application/json", data: JsonOutput.toJson([ok:false, error:"unauthorized"]), status: 401)
  }
  Integer devCt = (settings?.qaDevices ?: [])?.size() ?: 0
  Integer askCt = (state?.askTimes ?: [])?.size() ?: 0
  return render(contentType: "application/json", data: JsonOutput.toJson([ok:true, service:"hubitat_voice", rev:appRev(), devicesSelected:devCt, rateWindowCount:askCt, ts:now()]), status: 200)
}

def handleDevices() {
    log.debug "--------------------  In handleDevices  --------------------"
  def list = (qaDevices ?: []).collect { d ->
    [
      id: d.id?.toString(),
      name: d.displayName,
      label: d.label,
      type: d.typeName,
      capabilities: (d.capabilities ?: []).collect { it?.name }.findAll { it }
    ]
  }
  renderJson([ok:true, count:list.size(), devices:list])
}

def handleSelfTest() {
  try {
    if(!isAuthorizedRequest()) return respondAsk("Unauthorized", 401, [error:"unauthorized"])

    if(settings?.selfTestEnabled != true) {
      return respondAsk("Self-test endpoint is disabled.", 403, [ok:false, error:"selftest_disabled"])
    }

    String expected = (settings?.selfTestKey ?: "").toString().trim()
    if(expected) {
      String got = (params?.stk ?: "").toString()
      if(got != expected) return respondAsk("Unauthorized self-test key.", 401, [error:"unauthorized_selftest_key"])
    }

    def r = runSelfTests()
    String ans = "Self-test ${r.passCount}/${r.total} passed."
    int st = (r.passCount == r.total) ? 200 : 500

    String view = (params?.view ?: params?.format ?: "").toString().trim().toLowerCase()
    if(view in ["html","page","report"]) {
      return render(contentType: "text/html", data: selfTestHtml(r), status: st)
    }

    String pretty = (params?.pretty ?: "").toString().trim().toLowerCase()
    if(pretty in ["1","true","yes","y"]) {
      Map body = [ok:(st>=200 && st<300), answer:ans, mode:"selftest"] + (r ?: [:])
      return render(contentType: "text/plain", data: JsonOutput.prettyPrint(JsonOutput.toJson(body)), status: st)
    }

    return respondAsk(ans, st, [mode:"selftest"] + (r ?: [:]))
  } catch(ex) {
    log.debug "handleSelfTest error: ${ex}"
    return respondAsk("Self-test failed.", 500, [ok:false, error:"selftest_error"])
  }
}

private Map runSelfTests() {
  List results = []
  def add = { String name, def expected, def actual ->
    boolean ok = (expected == actual)
    results << [name:name, ok:ok, expected:"${expected}", actual:"${actual}"]
  }

  add("mode_control", "control", hvModeForQuery("turn on kitchen light"))
  add("mode_status", "status", hvModeForQuery("is front door open"))

  def b = parseBulkLightCommand("turn all bathroom lights off")
  add("bulk_light_action", "off", (b?.action ?: ""))
  add("bulk_light_scope", "bathroom", (b?.scope ?: ""))

  add("group_house_summary", "house_summary", detectGroupIntent(normalize("house status"))?.mode)
  add("group_secure_check", "secure_check", detectGroupIntent(normalize("run a security check"))?.mode)
  add("group_hsm_disarm", "hsm_disarm", detectGroupIntent(normalize("disarm hsm"))?.mode)

  add("code_digits_spaced", "0034", extractSecurityCode("unlock front door security code 0 0 3 4"))
  add("code_words", "0034", extractSecurityCode("unlock front door security code zero zero three four"))
  add("strip_phrase", "unlock front door", stripSecurityCodePhrase("unlock front door security code 0034"))
  add("strip_phrase_pin", "unlock front door", stripSecurityCodePhrase("unlock front door pin 0034"))
  add("digits_words", "0123", digitsFromSpeech("zero one two three", 8))

  def rw = rollingWindowStartMs("in the last 24 hours")
  boolean rollOk = (rw instanceof Number) && ((rw as Long) > 0L)
  add("rolling_window", true, rollOk)
  add("rolling_window_label", "in the last 24 hours", rollingWindowLabel("opened in the last 24 hours"))
  String rwRaw = (rw instanceof Number) ? "${rw}" : "null"

  int passCt = results.count { it.ok == true }
  return [total:results.size(), passCount:passCt, failed:results.findAll{ !it.ok }, results:results, rollingWindowRaw:rwRaw, selfTestRev:appRev()]
}

private String selfTestHtml(Map r) {
  Map rep = (r instanceof Map) ? r : [:]
  int total = safeInt(rep?.total, 0)
  int passCount = safeInt(rep?.passCount, 0)
  int failCount = Math.max(0, total - passCount)
  String status = (failCount == 0) ? "PASS" : "FAIL"
  String badgeColor = (failCount == 0) ? "#1a7f37" : "#b42318"

  def rows = (rep?.results instanceof List) ? (rep.results as List) : []
  String tr = rows.collect { t ->
    boolean ok = (t?.ok == true)
    String icon = ok ? "PASS" : "FAIL"
    String bg = ok ? "#f0fdf4" : "#fff1f2"
    String name = htmlEscape((t?.name ?: "") as String)
    String exp = htmlEscape((t?.expected ?: "") as String)
    String act = htmlEscape((t?.actual ?: "") as String)
    return "<tr style='background:${bg}'><td style='padding:8px;border-bottom:1px solid #e5e7eb'><b>${icon}</b></td><td style='padding:8px;border-bottom:1px solid #e5e7eb'>${name}</td><td style='padding:8px;border-bottom:1px solid #e5e7eb'><code>${exp}</code></td><td style='padding:8px;border-bottom:1px solid #e5e7eb'><code>${act}</code></td></tr>"
  }.join("")

  String failedList = ""
  def failed = (rep?.failed instanceof List) ? (rep.failed as List) : []
  if(failed) {
    failedList = "<h3 style='margin:16px 0 8px'>Failures</h3><ul style='margin:0 0 0 18px;padding:0'>" +
      failed.collect { f -> "<li><b>${htmlEscape((f?.name ?: "") as String)}</b>: expected <code>${htmlEscape((f?.expected ?: "") as String)}</code>, actual <code>${htmlEscape((f?.actual ?: "") as String)}</code></li>" }.join("") +
      "</ul>"
  }

  return """
<!doctype html>
<html>
<head>
  <meta charset='utf-8'>
  <meta name='viewport' content='width=device-width, initial-scale=1'>
  <title>Hubitat Voice Self-Test</title>
</head>
<body style='font-family:Segoe UI,Arial,sans-serif;background:#f8fafc;color:#0f172a;margin:0;padding:16px'>
  <div style='max-width:980px;margin:0 auto;background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:16px'>
    <div style='display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap'>
      <h2 style='margin:0'>Hubitat Voice Self-Test</h2>
      <span style='background:${badgeColor};color:#fff;padding:6px 10px;border-radius:999px;font-weight:700'>${status}</span>
    </div>
    <p style='margin:10px 0 14px;font-size:15px'>Passed <b>${passCount}</b> of <b>${total}</b> tests.</p>
    <table style='width:100%;border-collapse:collapse;font-size:14px'>
      <thead>
        <tr style='background:#f1f5f9;text-align:left'>
          <th style='padding:8px;border-bottom:1px solid #e5e7eb'>Result</th>
          <th style='padding:8px;border-bottom:1px solid #e5e7eb'>Test</th>
          <th style='padding:8px;border-bottom:1px solid #e5e7eb'>Expected</th>
          <th style='padding:8px;border-bottom:1px solid #e5e7eb'>Actual</th>
        </tr>
      </thead>
      <tbody>${tr}</tbody>
    </table>
    ${failedList}
  </div>
</body>
</html>
""".toString()
}

def respondText(String ans) {
  dbgPut('answer', (ans ?: '').toString())
  try { state.lastDebug = state.lastDebug ?: [:]; state.lastDebug.answer = (ans ?: '').toString() } catch(e) {}
  return respondAsk((ans ?: "").toString(), 200)
}

private def respondAsk(String ans, Integer status = 200, Map payload = [:]) {
  Integer st = status ?: 200
  String out = assistantStyleText((ans ?: "").toString())

  // Backward-compatible escape hatch: /ask?format=text
  String fmt = (params?.format ?: "").toString().trim().toLowerCase()
  if(fmt in ["text","plain","txt"]) {
    return render(contentType: "text/plain", data: out, status: st)
  }

  Map body = [ok: (st >= 200 && st < 300)]
  if(payload instanceof Map) body.putAll(payload)
  body.answer = out
  return render(contentType: "application/json", data: JsonOutput.toJson(body), status: st)
}

private boolean geminiFallbackReady() {
  try {
    return !!settings?.geminiFallbackEnabled && !!((settings?.geminiApiKey ?: "").toString().trim())
  } catch(e) {
    return false
  }
}

private String selectedFallbackProvider() {
  try {
    String p = (settings?.aiFallbackProvider ?: defaultFallbackProvider()).toString().trim().toLowerCase()
    return (p == "chatgpt") ? "chatgpt" : "gemini"
  } catch(e) {
    return "gemini"
  }
}

private boolean chatGptFallbackReady() {
  try {
    return !!settings?.geminiFallbackEnabled && selectedFallbackProvider() == "chatgpt" && !!((settings?.chatgptApiKey ?: "").toString().trim())
  } catch(e) {
    return false
  }
}

private boolean aiFallbackReady() {
  String p = selectedFallbackProvider()
  if(p == "chatgpt") return chatGptFallbackReady()
  return geminiFallbackReady()
}

private String extractChatGptText(def data) {
  try {
    String direct = (data?.output_text ?: "").toString().trim()
    if(direct) return direct
  } catch(e) {}

  try {
    def output = data?.output
    if(output instanceof List) {
      List<String> txts = []
      output.each { item ->
        if((item?.type ?: "").toString() == "message" && item?.content instanceof List) {
          item.content.each { c ->
            String t = ""
            if((c?.type ?: "").toString() in ["output_text", "text"]) {
              t = (c?.text ?: c?.value ?: "").toString().trim()
            }
            if(t) txts << t
          }
        }
      }
      String joined = txts.join(" ").replaceAll(/\s+/, " ").trim()
      if(joined) return joined
    }
  } catch(e) {}

  return ""
}

private List<String> extractChatGptSourceUrls(def data) {
  List<String> out = []
  try {
    def output = data?.output
    if(output instanceof List) {
      output.each { item ->
        if((item?.type ?: "").toString() == "message" && item?.content instanceof List) {
          item.content.each { c ->
            if(c?.annotations instanceof List) {
              c.annotations.each { a ->
                String u = (a?.url ?: "").toString().trim()
                if(u) out << u
              }
            }
          }
        }
      }
    }
  } catch(e) {}
  return out.unique()
}

private String extractChatGptFinishReason(def data) {
  try {
    String status = (data?.status ?: "").toString().trim()
    if(status) return status
  } catch(e) {}
  return ""
}

private Map extractChatGptUsageMetadata(def data) {
  try {
    def usage = data?.usage
    if(usage instanceof Map) {
      return [
        promptTokens: safeInt(usage.input_tokens, 0),
        outputTokens: safeInt(usage.output_tokens, 0),
        totalTokens: safeInt(usage.total_tokens, 0)
      ]
    }
  } catch(e) {}
  return [promptTokens: 0, outputTokens: 0, totalTokens: 0]
}

private String chooseChatGptModel(String baseModel, String liveModel, String category) {
  if(category in ["sports", "news", "reasoning", "live"]) {
    return (liveModel ?: baseModel ?: defaultChatGptModel()).toString()
  }
  return (baseModel ?: liveModel ?: defaultChatGptModel()).toString()
}

private Map askChatGptFallback(String queryText) {
  if(!chatGptFallbackReady()) return null

  String apiKey = (settings?.chatgptApiKey ?: "").toString().trim()
  String baseModel = (settings?.chatgptModel ?: defaultChatGptModel()).toString().trim()
  String liveModel = (settings?.chatgptLiveModel ?: defaultChatGptLiveModel()).toString().trim()
  if(!baseModel) baseModel = defaultChatGptModel()
  boolean liveInfo = isLiveInfoQuery(queryText)
  boolean sportsLive = isSportsLiveQuery(queryText)
  String queryCategory = geminiQueryCategory(queryText)
  String model = chooseChatGptModel(baseModel, liveModel, queryCategory)

  String sysPrompt = "You are the spoken response voice for a smart home assistant, matching Google Assistant on Google Home style. Reply naturally, concise, and conversational in 1 to 3 short sentences. Use plain text only: no markdown, no bullet lists, no labels, no citations, no URLs, and no emojis. Do not mention being an AI model. Give direct answers first, then one brief detail if helpful."
  sysPrompt += " If the user asks about smart-home state you cannot verify from Hubitat, say that briefly and suggest checking the device in one short phrase."
  if(liveInfo) {
    sysPrompt += " This question asks for current information. Use live web search for breaking news, live events, scores, status, and today's weather. Answer confidently with 'as of HH:MM time' when possible. If web search fails, say briefly what info you cannot access and offer the best answer you can."
  }
  if(sportsLive) {
    sysPrompt += " For sports questions: prioritize verified data from official sources. Include exact team names, final/live score (like '4 to 2'), game status, and elapsed time. If you find the game is live, mention inning/quarter/period. Be specific: say 'Final, 4-2' not just 'win'."
  }

  Integer chatgpt429CooldownSecs = safeInt(settings?.chatgpt429CooldownSecs, defaultGemini429CooldownSecs())
  if(chatgpt429CooldownSecs == null || chatgpt429CooldownSecs < 15) chatgpt429CooldownSecs = defaultGemini429CooldownSecs()
  long cooldownUntil = 0L
  try { cooldownUntil = (state?.chatgpt429CooldownUntil ?: 0L) as Long } catch(ignore) { cooldownUntil = 0L }
  if(cooldownUntil > now()) {
    return [
      answer: "ChatGPT is busy right now. Please try again in a minute.",
      grounded: false,
      searchQueries: [],
      sourceUrls: [],
      groundingMetadata: [:],
      finishReason: "quota_cooldown",
      model: model,
      fallbackSource: "chatgpt",
      sports_source: "none",
      sports_league: "",
      provider: "chatgpt"
    ]
  }

  def callChatGpt = { String askText, String modelName, BigDecimal temp, Integer tokenLimit, String attemptLabel ->
    String resultText = ""
    List<String> sourceUrls = []
    String finishReason = ""
    Map usageMetadata = [promptTokens: 0, outputTokens: 0, totalTokens: 0]

    trackChatGptUsage(modelName, attemptLabel, queryCategory)

    Map reqBody = [
      model: modelName,
      input: [
        [role: "system", content: [[type: "input_text", text: sysPrompt]]],
        [role: "user", content: [[type: "input_text", text: askText]]]
      ],
      tools: [[type: "web_search_preview"]],
      temperature: temp,
      max_output_tokens: tokenLimit
    ]

    Map params = [
      uri: "https://api.openai.com/v1/responses",
      headers: [
        "Authorization": "Bearer ${apiKey}"
      ],
      contentType: "application/json",
      requestContentType: "application/json",
      body: reqBody
    ]

    try {
      httpPostJson(params) { resp ->
        if(resp?.status in [200, 201]) {
          def data = resp?.data
          if(!(data instanceof Map)) {
            try {
              data = new JsonSlurper().parseText(resp?.data?.toString() ?: "")
            } catch(ignore) {}
          }
          resultText = extractChatGptText(data)
          sourceUrls = extractChatGptSourceUrls(data)
          finishReason = extractChatGptFinishReason(data)
          usageMetadata = extractChatGptUsageMetadata(data)
          trackChatGptCost(modelName, usageMetadata, (sourceUrls ? true : false))
          log.debug "ChatGPT fallback response ok. attempt=${attemptLabel}, grounded=${sourceUrls ? true : false}, sources=${sourceUrls?.size() ?: 0}, finishReason=${finishReason ?: 'unknown'}, rawLen=${resultText?.size() ?: 0}"
          log.debug "ChatGPT usage (${attemptLabel}): prompt=${usageMetadata.promptTokens ?: 0}, output=${usageMetadata.outputTokens ?: 0}, total=${usageMetadata.totalTokens ?: 0}"
        } else if((resp?.status as Integer) == 429) {
          finishReason = "quota_429"
          try { state.chatgpt429CooldownUntil = now() + ((chatgpt429CooldownSecs as Long) * 1000L) } catch(ignore) {}
        }
      }
    } catch(e) {
      String em = (e?.toString() ?: "").toLowerCase()
      if(em.contains("status code: 429") || em.contains("too many requests") || em.contains("rate limit")) {
        finishReason = "quota_429"
        try { state.chatgpt429CooldownUntil = now() + ((chatgpt429CooldownSecs as Long) * 1000L) } catch(ignore) {}
      } else {
        log.debug "ChatGPT fallback request failed on attempt=${attemptLabel}: ${e}"
      }
    }

    return [
      answer: compactGeminiAnswer(resultText),
      grounded: (sourceUrls ? true : false),
      searchQueries: [],
      sourceUrls: sourceUrls,
      groundingMetadata: [:],
      usageMetadata: usageMetadata,
      finishReason: finishReason,
      model: modelName,
      fallbackSource: "chatgpt",
      sports_source: (sportsLive ? (sourceUrls ? "chatgpt_search" : "") : ""),
      sports_league: "",
      provider: "chatgpt"
    ]
  }

  Map first = callChatGpt(queryText, model, (liveInfo ? 0.25G : 0.5G), (liveInfo ? 320 : 220), "first")
  Map chosen = first
  boolean chatgptRateLimited = (["quota_429"].contains((first?.finishReason ?: "").toString()))

  if(liveInfo && !chatgptRateLimited && (first?.grounded != true || !(first?.answer))) {
    def tz = location?.timeZone ?: TimeZone.getTimeZone("UTC")
    String nowStamp = new Date().format("yyyy-MM-dd HH:mm z", tz)
    String retryQuery = "${queryText}. Use live web results. Current time is ${nowStamp}."
    if(sportsLive) retryQuery += " Focus on latest sports result with teams, score, game status, and update time."
    String retryModel = chooseChatGptModel(baseModel, liveModel, queryCategory)
    Map second = callChatGpt(retryQuery, retryModel, 0.2G, 380, "retry_grounded")
    if(second?.answer && ((second?.grounded == true) || !(first?.answer))) {
      chosen = second
    }
    chatgptRateLimited = chatgptRateLimited || (["quota_429"].contains((second?.finishReason ?: "").toString()))
  }

  if(sportsLive && !(chosen?.answer)) {
    return [
      answer: "Sorry, I can't get live sports right now. Please try again in a minute.",
      grounded: false,
      searchQueries: [],
      sourceUrls: [],
      groundingMetadata: [:],
      finishReason: "sports_unavailable",
      model: (chosen?.model ?: model),
      fallbackSource: "chatgpt",
      sports_source: "none",
      sports_league: "",
      provider: "chatgpt"
    ]
  }

  if(!(chosen?.answer) && chatgptRateLimited) {
    return [
      answer: "ChatGPT is busy right now. Please try again in a minute.",
      grounded: false,
      searchQueries: [],
      sourceUrls: [],
      groundingMetadata: [:],
      finishReason: "quota_429",
      model: (chosen?.model ?: model),
      fallbackSource: "chatgpt",
      sports_source: "none",
      sports_league: "",
      provider: "chatgpt"
    ]
  }

  String resultText = (chosen?.answer ?: "").toString()
  if(resultText && liveInfo && chosen?.grounded != true && !resultText.toLowerCase().startsWith("last available update says")) {
    resultText = "Last available update says ${resultText}"
  }
  if(resultText) {
    state.lastAiAnswer = resultText
    state.lastAiAt = now()
    state.lastAiProvider = "chatgpt"
    state.lastAiModel = (chosen?.model ?: model)
    state.lastAiGrounded = (chosen?.grounded == true)
    state.lastAiSources = (chosen?.sourceUrls instanceof List) ? chosen.sourceUrls : []
    state.lastAiFinishReason = (chosen?.finishReason ?: "")
    String trackSource = (chosen?.fallbackSource ?: "chatgpt")
    if(chosen?.sports_source) trackSource = chosen.sports_source
    trackQueryResult(trackSource, queryText)
    return [
      answer: resultText,
      grounded: (chosen?.grounded == true),
      searchQueries: (chosen?.searchQueries instanceof List) ? chosen.searchQueries : [],
      sourceUrls: (chosen?.sourceUrls instanceof List) ? chosen.sourceUrls : [],
      groundingMetadata: [:],
      finishReason: (chosen?.finishReason ?: ""),
      model: (chosen?.model ?: model),
      fallbackSource: (chosen?.fallbackSource ?: "chatgpt"),
      sports_source: (chosen?.sports_source ?: ""),
      sports_league: (chosen?.sports_league ?: ""),
      provider: "chatgpt"
    ]
  }

  return null
}

private Map askSelectedFallback(String queryText) {
  String p = selectedFallbackProvider()
  if(p == "chatgpt") return askChatGptFallback(queryText)
  return askGeminiFallback(queryText)
}

private String extractGeminiText(def data) {
  try {
    String direct = (data?.text ?: "").toString().trim()
    if(direct) return direct
  } catch(e) {}

  try {
    def cands = data?.candidates
    if(cands instanceof List) {
      for(def cand in cands) {
        def content = cand?.content
        def parts = content?.parts
        if(parts instanceof List) {
          List<String> txts = []
          for(def p in parts) {
            String txt = (p?.text ?: "").toString().trim()
            if(txt) txts << txt
          }
          String joined = txts.join(" ").replaceAll(/\s+/, " ").trim()
          if(joined) return joined
        }
      }
    }
  } catch(e) {}

  return ""
}

private Map extractGeminiGroundingMetadata(def data) {
  try {
    def cands = data?.candidates
    if(cands instanceof List && cands) {
      def gm = cands[0]?.groundingMetadata
      if(gm instanceof Map) return (Map)gm
    }
  } catch(e) {}
  return [:]
}

private List<String> extractGeminiGroundingSources(Map groundingMetadata) {
  List<String> out = []
  try {
    def chunks = groundingMetadata?.groundingChunks
    if(chunks instanceof List) {
      chunks.each { ch ->
        String uri = (ch?.web?.uri ?: "").toString().trim()
        if(uri) out << uri
      }
    }
  } catch(e) {}
  return out.unique()
}

private List<String> extractGeminiSearchQueries(Map groundingMetadata) {
  List<String> out = []
  try {
    def qs = groundingMetadata?.webSearchQueries
    if(qs instanceof List) {
      qs.each { q ->
        String s = (q ?: "").toString().trim()
        if(s) out << s
      }
    }
  } catch(e) {}
  return out
}

private String extractGeminiFinishReason(def data) {
  try {
    def cands = data?.candidates
    if(cands instanceof List && cands) {
      String reason = (cands[0]?.finishReason ?: "").toString().trim()
      if(reason) return reason
    }
  } catch(e) {}
  return ""
}

private String formatScoreWithSsml(String score) {
  if(!score || !score.matches(/^\d+-\d+$/)) return score
  String[] parts = score.split(/-/)
  if(parts.size() != 2) return score
  String readAs = "${parts[0]} to ${parts[1]}"
  return "<sub alias=\"${readAs}\">${score}</sub>"
}

private String numberToWordsUnder100(Integer n) {
  List<String> ones = ["zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"]
  List<String> tens = ["","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"]
  if(n == null || n < 0 || n > 99) return "${n}"
  if(n < 20) return ones[n]
  int t = (int)(n / 10)
  int o = n % 10
  return o == 0 ? tens[t] : "${tens[t]}-${ones[o]}"
}

private String dayOrdinalWord(String dayStr) {
  Map<String, String> dateMap = [
    "1": "first", "21": "twenty-first", "22": "twenty-second", "23": "twenty-third",
    "2": "second", "3": "third", "4": "fourth", "5": "fifth",
    "6": "sixth", "7": "seventh", "8": "eighth", "9": "ninth", "10": "tenth",
    "11": "eleventh", "12": "twelfth", "13": "thirteenth", "14": "fourteenth",
    "15": "fifteenth", "16": "sixteenth", "17": "seventeenth", "18": "eighteenth",
    "19": "nineteenth", "20": "twentieth", "24": "twenty-fourth", "25": "twenty-fifth",
    "26": "twenty-sixth", "27": "twenty-seventh", "28": "twenty-eighth",
    "29": "twenty-ninth", "30": "thirtieth", "31": "thirty-first"
  ]
  return dateMap[dayStr] ?: dayStr
}

private String spokenYear(String yearStr) {
  if(!(yearStr ?: "").matches(/\d{4}/)) return yearStr
  Integer y = safeInt(yearStr, null)
  if(y == null) return yearStr
  if(y >= 2000 && y <= 2099) {
    Integer last = y % 100
    if(last == 0) return "two thousand"
    return "twenty ${numberToWordsUnder100(last)}"
  }
  return yearStr
}

private String formatDateWithSsml(String dateStr) {
  if(!dateStr) return dateStr
  String s = dateStr
  s = s.replaceAll(/\b(January|February|March|April|May|June|July|August|September|October|November|December)\s+([0-3]?\d),\s*(\d{4})\b/) { all, month, day, year ->
    String d = day.toString().replaceFirst(/^0/, "")
    String dayAlias = dayOrdinalWord(d)
    String yearAlias = spokenYear(year.toString())
    return "${month} <sub alias=\"${dayAlias}\">${d}</sub>, <sub alias=\"${yearAlias}\">${year}</sub>"
  }
  s = s.replaceAll(/(?<!>)\b(20\d{2})\b(?!<\/sub>)/) { all, year ->
    String yearAlias = spokenYear(year.toString())
    return "<sub alias=\"${yearAlias}\">${year}</sub>"
  }
  return s
}

private String formatScoreLineWithSsml(String awayName, String awayScore, String homeName, String homeScore) {
  if(!awayScore && !homeScore) return "${awayName} vs ${homeName}."
  String sslmScore = awayScore && homeScore ? formatScoreWithSsml("${awayScore}-${homeScore}") : "${awayScore}${homeScore}"
  String[] parts = sslmScore.split(/<sub[^>]*>/)
  if(parts.size() >= 1 && sslmScore.contains("<sub")) {
    def subMatch = sslmScore =~ /<sub[^>]*>(.*?)<\/sub>/
    String scoreText = ""
    if(subMatch) {
      scoreText = subMatch[0][0]
    }
    return "${awayName} ${scoreText}, ${homeName}."
  }
  return "${awayName} ${awayScore}, ${homeName} ${homeScore}."
}

private String formatTimeWithSsml(String timeStr) {
  if(!timeStr) return timeStr
  def timeMatch = timeStr =~ /(\d{1,2}):(\d{2})\s*(AM|PM|am|pm)?/
  if(!timeMatch) return timeStr
  def match = timeMatch[0]
  String hour = match[1].toString().replaceAll("^0", "")
  String min = match[2]
  String ampm = match[3] ? " " + match[3].toUpperCase() : ""
  String minWord = min == "00" ? "" : min.replaceAll("^0", "")
  String readAs = minWord ? "${hour} ${minWord}" : hour
  readAs += ampm
  return "<sub alias=\"${readAs}\">${timeStr}</sub>"
}

private String formatPronouncedName(String displayName, String pronunciationHint) {
  if(!displayName || !pronunciationHint) return displayName
  return "<sub alias=\"${pronunciationHint}\">${displayName}</sub>"
}

private String applySpeechFriendlyFormatting(String text) {
  String s = (text ?: "").toString()
  if(!s) return s
  s = formatDateWithSsml(s)
  s = s.replaceAll(/\b(\d{1,3})-(\d{1,3})\b/) { all, a, b ->
    return formatScoreWithSsml("${a}-${b}")
  }
  s = s.replaceAll(/\b(\d{1,2}:\d{2}\s*(?:AM|PM|am|pm)?)\b/) { all, t ->
    return formatTimeWithSsml(t.toString())
  }
  return s
}

private String stripSsmlTags(String text) {
  if(!text) return text
  return text.replaceAll(/<[^>]+>/, "").replaceAll(/\s+/, " ").trim()
}

private String compactGeminiAnswer(String text) {
  String s = (text ?: "").toString().trim()
  if(!s) return ""
  s = s.replaceAll(/```[\s\S]*?```/, " ")
  s = s.replaceAll(/`([^`]*)`/, '$1')
  s = s.replaceAll(/\[(\d+)\]/, "")
  s = s.replaceAll(/\((https?:\/\/[^\s)]+)\)/, "")
  s = s.replaceAll(/https?:\/\/\S+/, "")
  s = s.replaceAll(/\*\*|__|~~/, "")
  s = s.replaceAll(/(?m)^\s*[-*]\s+/, "")
  s = s.replaceAll(/\s+/, " ").trim()
  s = s.replaceAll(/(?i)^as an ai( language model)?[, ]+/, "")
  s = s.replaceAll(/(?i)^as a language model[, ]+/, "")
  s = s.replaceAll(/(?i)^i (can(not|'t)|cannot) access (real[- ]?time|live) (data|information)[^.!?]*[.!?]?\s*/, "")
  s = s.replaceAll(/(?i)^i do not have access to[^.!?]*[.!?]?\s*/, "")

  try {
    def parts = s.split(/(?<=[.!?])\s+/).findAll { it?.trim() }
    if(parts && parts.size() > 4) {
      s = parts.take(4).join(" ").trim()
    }
  } catch(e) {}

  if(s.size() > 420) s = s.take(417).trim() + "..."
  return s
}

private String assistantStyleText(String text) {
  String s = (text ?: "").toString().trim()
  if(!s) return ""

  s = s.replaceAll(/\s+/, " ").trim()
  s = s.replace(" · ", ", ")
  s = s.replaceAll(/\s*\(\"[^\"]*\"\)\s*/, " ").replaceAll(/\s+/, " ").trim()

  String low = s.toLowerCase()
  if(low == "no answer." || low == "no answer") {
    s = "Sorry, I couldn't find an answer."
  } else if(low == "error processing request." || low == "error processing request") {
    s = "Sorry, I ran into a problem."
  } else if(low.contains("didn't understand that request") || low.contains("didn’t understand that request")) {
    s = "Sorry, I didn't catch that. Please try again."
  }

  boolean shortMode = (settings?.shortTts != false)
  if(shortMode) {
    try {
      def parts = s.split(/(?<=[.!?])\s+/).findAll { it?.trim() }
      if(parts && parts.size() > 3) {
        s = parts.take(3).join(" ").trim()
      }
    } catch(e) {}
    if(s.size() > 260) s = s.take(257).trim() + "..."
  }

  return s
}

private boolean isLiveInfoQuery(String queryText) {
  String q = normalize(queryText)
  if(!q) return false
  return (
    q.contains("live") ||
    q.contains("right now") ||
    q.contains("currently") ||
    q.contains("in progress") ||
    q.contains("score right now") ||
    q.contains("whats the score") ||
    q.contains("what s the score") ||
    q.contains("current score") ||
    q.contains("game score now") ||
    q.contains("playing right now") ||
    q.contains("today s score") ||
    q.contains("todays score") ||
    q.contains("sports") ||
    q.contains("sport") ||
    q.contains("game") ||
    q.contains("match") ||
    q.contains("final") ||
    q.contains("quarter") ||
    q.contains("inning") ||
    q.contains("period") ||
    q.contains("halftime") ||
    q.contains("standings") ||
    q.contains("who won") ||
    q.contains("wins") ||
    q.contains("losses") ||
    q.contains("record") ||
    q.contains("mlb") ||
    q.contains("nba") ||
    q.contains("nfl") ||
    q.contains("nhl") ||
    q.contains("ncaa") ||
    q.contains("soccer") ||
    q.contains("football") ||
    q.contains("baseball") ||
    q.contains("basketball") ||
    q.contains("hockey") ||
    q.contains("tennis") ||
    q.contains("golf")
  )
}

private boolean isSportsLiveQuery(String queryText) {
  String q = normalize(queryText)
  if(!q) return false
  return (
    q.contains("score") ||
    q.contains("game") ||
    q.contains("match") ||
    q.contains("standings") ||
    q.contains("who won") ||
    q.contains("playing") ||
    q.contains("mlb") ||
    q.contains("nba") ||
    q.contains("nfl") ||
    q.contains("nhl") ||
    q.contains("ncaa") ||
    q.contains("soccer") ||
    q.contains("football") ||
    q.contains("baseball") ||
    q.contains("basketball") ||
    q.contains("hockey") ||
    q.contains("tennis") ||
    q.contains("golf")
  )
}

private boolean isNewsQuery(String queryText) {
  String q = normalize(queryText)
  if(!q) return false
  return (
    q.contains("breaking news") ||
    q.contains("latest news") ||
    q.contains("headline") ||
    q.contains("headlines") ||
    q.contains("news about") ||
    q.contains("news on") ||
    q.startsWith("what happened") ||
    q.contains("what happened today") ||
    q.contains("update on")
  )
}

private boolean isReasoningHeavyQuery(String queryText) {
  String q = normalize(queryText)
  if(!q) return false
  return (
    q.startsWith("why ") ||
    q.startsWith("how ") ||
    q.startsWith("explain ") ||
    q.contains("difference between") ||
    q.contains("compare") ||
    q.contains("better than") ||
    q.contains("should i")
  )
}

private String geminiQueryCategory(String queryText) {
  if(isSportsLiveQuery(queryText)) return "sports"
  if(isNewsQuery(queryText)) return "news"
  if(isReasoningHeavyQuery(queryText)) return "reasoning"
  if(isLiveInfoQuery(queryText)) return "live"
  return "general"
}

private String chooseGeminiModel(String baseModel, String liveModel, Map quotaProfile, String category) {
  boolean paidTier = ((quotaProfile?.isFreeTier ?: true) == false)
  if(paidTier && (category in ["sports", "news", "reasoning", "live"])) {
    return (liveModel ?: baseModel ?: "gemini-2.5-flash").toString()
  }
  return (baseModel ?: liveModel ?: "gemini-2.5-flash").toString()
}

private boolean shouldRetryPaidGrounding(Map first, boolean liveInfo, Map quotaProfile, String category) {
  if(!liveInfo) return false
  if((quotaProfile?.allowGroundedRetry == true) != true) return false
  String firstAnswer = (first?.answer ?: "").toString().trim()
  if(!firstAnswer) return true
  if(first?.grounded == true) return false
  if(category in ["sports", "news", "live"]) return true
  if(category == "reasoning" && firstAnswer.size() < 120) return true
  return false
}

private Map askGeminiFallback(String queryText) {
  if(!geminiFallbackReady()) return null

  String apiKey = (settings?.geminiApiKey ?: "").toString().trim()
  String baseModel = (settings?.geminiModel ?: "gemini-2.5-flash").toString().trim()
  if(!baseModel) baseModel = "gemini-2.5-flash"
  String liveModel = (settings?.geminiLiveModel ?: "").toString().trim()
  boolean liveInfo = isLiveInfoQuery(queryText)
  boolean sportsLive = isSportsLiveQuery(queryText)
  String queryCategory = geminiQueryCategory(queryText)
  Map quotaProfile = geminiQuotaProfile()
  Integer sportsCacheSecs = safeInt(settings?.sportsCacheSeconds, defaultSportsCacheSeconds())
  if(sportsCacheSecs == null || sportsCacheSecs < 0) sportsCacheSecs = defaultSportsCacheSeconds()
  Integer gemini429CooldownSecs = safeInt(settings?.gemini429CooldownSecs, (quotaProfile.cooldownSecs as Integer))
  if(gemini429CooldownSecs == null || gemini429CooldownSecs < 15) gemini429CooldownSecs = defaultGemini429CooldownSecs()
  String model = chooseGeminiModel(baseModel, liveModel, quotaProfile, queryCategory)
  String sysPrompt = "You are the spoken response voice for a smart home assistant, matching Google Assistant on Google Home style. Reply naturally, concise, and conversational in 1 to 3 short sentences. Use plain text only: no markdown, no bullet lists, no labels, no citations, no URLs, and no emojis. Do not mention being an AI model. Give direct answers first, then one brief detail if helpful."
  sysPrompt += " If the user asks about smart-home state you cannot verify from Hubitat, say that briefly and suggest checking the device in one short phrase."
  if(liveInfo) {
    sysPrompt += " This question asks for current information. Use Google Search grounding to find: breaking news, live events, scores, status, today's weather. Answer confidently with 'as of HH:MM time' when possible. If grounding fails, say briefly what info you cannot access and offer the best answer you can."
  }
  if(sportsLive) {
    sysPrompt += " For sports questions: prioritize verified data from official sources. Include exact team names, final/live score (like '4 to 2'), game status, and elapsed time. If you find the game is live, mention inning/quarter/period. Be specific: say 'Final, 4-2' not just 'win'."
  }

  try {
    def inferSportsLeagues = { String qNorm ->
      List<Map> out = []
      def addLeague = { String sport, String league, String label ->
        if(!sport || !league) return
        String key = "${sport}/${league}"
        if(!out.find { "${it.sport}/${it.league}" == key }) {
          out << [sport:sport, league:league, label:label]
        }
      }

      if(qNorm.contains("nfl") || (qNorm.contains("football") && !qNorm.contains("college"))) addLeague("football", "nfl", "NFL")
      if(qNorm.contains("ncaa") || qNorm.contains("college football") || qNorm.contains("ncaaf")) addLeague("football", "college-football", "College Football")
      if(qNorm.contains("nba") || (qNorm.contains("basketball") && !qNorm.contains("college"))) addLeague("basketball", "nba", "NBA")
      if(qNorm.contains("ncaa") || qNorm.contains("college basketball") || qNorm.contains("ncaab")) addLeague("basketball", "mens-college-basketball", "College Basketball")
      if(qNorm.contains("mlb") || qNorm.contains("baseball")) addLeague("baseball", "mlb", "MLB")
      if(qNorm.contains("nhl") || qNorm.contains("hockey")) addLeague("hockey", "nhl", "NHL")
      if(qNorm.contains("soccer")) {
        addLeague("soccer", "usa.1", "MLS")
        addLeague("soccer", "eng.1", "Premier League")
      }
      if(qNorm.contains("pga") || qNorm.contains("golf")) addLeague("golf", "pga", "PGA Tour")
      if(qNorm.contains("tennis") || qNorm.contains("atp") || qNorm.contains("wta")) {
        addLeague("tennis", "atp", "ATP Tennis")
        addLeague("tennis", "wta", "WTA Tennis")
      }
      if(qNorm.contains("ufc") || qNorm.contains("mma")) addLeague("mma", "ufc", "UFC")
      if(qNorm.contains("cricket")) addLeague("cricket", "international", "Cricket")
      if(qNorm.contains("rugby") || qNorm.contains("nrl")) {
        addLeague("rugby-league", "nrl", "NRL")
        addLeague("rugby-union", "international", "Rugby Union")
      }

      if(!out) {
        String prefRaw = (settings?.sportsPreferredLeagues ?: "").toString().toUpperCase()
        if(prefRaw) {
          def prefList = prefRaw.split(/[,\s]+/).findAll { it }
          prefList.each { code ->
            if(code == "NFL") addLeague("football", "nfl", "NFL")
            if(code == "NCAAF") addLeague("football", "college-football", "College Football")
            if(code == "NBA") addLeague("basketball", "nba", "NBA")
            if(code == "NCAAB") addLeague("basketball", "mens-college-basketball", "College Basketball")
            if(code == "MLB") addLeague("baseball", "mlb", "MLB")
            if(code == "NHL") addLeague("hockey", "nhl", "NHL")
            if(code == "MLS") addLeague("soccer", "usa.1", "MLS")
            if(code == "EPL") addLeague("soccer", "eng.1", "Premier League")
          }
        }
      }

      if(!out) {
        addLeague("football", "nfl", "NFL")
        addLeague("basketball", "nba", "NBA")
        addLeague("baseball", "mlb", "MLB")
        addLeague("hockey", "nhl", "NHL")
      }

      return out
    }

    def sportsTeamTokens = { String qNorm ->
      List<String> stop = ["what","whats","whats","the","score","scores","game","games","match","today","tonight","now","right","live","sports","sport","who","won","is","are","in","on","for","of","vs","versus","current","latest","update","status","playing","play","at","and","a","an","my"]
      List<String> base = (qNorm ?: "").split(/\s+/).findAll { t ->
        String tok = (t ?: "").trim()
        tok && tok.size() >= 3 && !(tok in stop)
      }

      String prefTeamsRaw = (settings?.sportsPreferredTeams ?: "").toString().toLowerCase()
      if(prefTeamsRaw) {
        base.addAll(prefTeamsRaw.split(/[,\s]+/).findAll { it && it.size() >= 3 && !(it in stop) })
      }

      Map<String, List<String>> aliasMap = [
        // NFL
        "niners": ["49ers","san","francisco"],
        "chiefs": ["kansas","city"],
        "pats": ["patriots","new","england"],
        "pack": ["packers","green","bay"],
        "cards": ["cardinals","arizona"],
        "fins": ["dolphins","miami"],
        "ravens": ["baltimore"],
        "steelers": ["pittsburgh"],
        "cowboys": ["dallas"],
        "eagles": ["philadelphia"],
        "giants": ["new","york"],
        "broncos": ["denver"],
        "chargers": ["los","angeles"],
        "seahawks": ["seattle"],
        "bucs": ["buccaneers","tampa"],
        "titans": ["tennessee"],
        "jaguars": ["jacksonville"],
        "saints": ["new","orleans"],
        "texans": ["houston"],
        "colts": ["indianapolis"],
        "vikings": ["minnesota"],
        "lions": ["detroit"],
        "bears": ["chicago"],
        "falcons": ["atlanta"],
        "bills": ["buffalo"],
        // NBA
        "lakers": ["los","angeles"],
        "celtics": ["boston"],
        "warriors": ["golden","state"],
        "heat": ["miami"],
        "sixers": ["philadelphia"],
        "nets": ["brooklyn"],
        "bucks": ["milwaukee"],
        "raptors": ["toronto"],
        "nuggets": ["denver"],
        "suns": ["phoenix"],
        "mavs": ["mavericks","dallas"],
        "grizzlies": ["memphis"],
        // MLB
        "yanks": ["yankees","new","york"],
        "mets": ["new","york"],
        "dodgers": ["los","angeles"],
        "giants": ["san","francisco"],
        "sox": ["red","sox","white","sox"],
        "cubbies": ["cubs","chicago"],
        "pirates": ["pittsburgh"],
        // NHL
        "leafs": ["leaves","toronto"],
        "habs": ["canadiens","montreal"],
        "wings": ["red","wings","detroit"],
        "bruins": ["boston"],
        "avalanche": ["colorado"],
        "rangers": ["new","york"],
        // Soccer
        "city": ["man","city","manchester"],
        "united": ["manchester","united"],
        "arsenal": ["london"],
        "liverpool": ["pool"],
        "spurs": ["tottenham"],
        // Golf
        "tiger": ["woods"],
        // Tennis
        "djokovic": ["novak"],
        "nadal": ["rafa"],
        "federer": ["roger"],
        // MMA
        "ufc": ["fighting"]
      ]
      String qx = (qNorm ?: "")
      aliasMap.each { k, vals ->
        if(qx.contains(k)) {
          vals.each { v -> if(v) base << v.toString().toLowerCase() }
        }
      }

      return base.unique()
    }

    def fetchEspnSportsFallback = { String rawQuery ->
      String qNorm = normalize(rawQuery)
      if(!qNorm) return null
      String cacheKey = "sports::" + qNorm
      try {
        // Keep state size under control by pruning stale entries on read path.
        pruneSportsApiCache(Math.max((sportsCacheSecs ?: defaultSportsCacheSeconds()) * 20, 3600))
        state.sportsApiCache = (state.sportsApiCache instanceof Map) ? state.sportsApiCache : [:]
        Map c = state.sportsApiCache[cacheKey] instanceof Map ? (Map)state.sportsApiCache[cacheKey] : null
        if(c && c.ts instanceof Number && ((now() - (c.ts as Long)) <= (sportsCacheSecs as Long) * 1000L)) {
          if(c.payload instanceof Map && c.payload.answer) {
            Map cp = [:]
            cp.putAll((Map)c.payload)
            cp.finishReason = "sports_api_cache"
            return cp
          }
        }
      } catch(e) {}

      List<Map> leagues = inferSportsLeagues(qNorm)
      List<String> teamTokens = sportsTeamTokens(qNorm)

      for(Map lg in leagues) {
        String sport = (lg?.sport ?: "").toString()
        String league = (lg?.league ?: "").toString()
        if(!sport || !league) continue

        String uri = "https://site.api.espn.com/apis/site/v2/sports/${sport}/${league}/scoreboard"
        Map data = null
        String errorType = null
        try {
          httpGet(uri: uri, contentType: "application/json", timeout: 8) { resp ->
            if(resp?.status == 200) {
              def body = resp?.data
              if(body instanceof Map) data = (Map)body
            } else if(resp?.status == 503 || resp?.status == 502) {
              errorType = "sports_api_unavailable"
            }
          }
        } catch(e) {
          if(e.message?.contains("Read timed out")) {
            errorType = "sports_api_timeout"
          } else if(e.message?.contains("refused") || e.message?.contains("unreachable")) {
            errorType = "network_unavailable"
          }
          data = null
        }
        if(errorType == "network_unavailable" && !leagues.any { it != lg }) {
          return [answer: "Sorry, I can't reach that right now.", grounded: false, finishReason: "network_error"]
        }
        if(!(data instanceof Map)) continue

        List events = (data?.events instanceof List) ? (List)data.events : []
        if(!events) continue

        def bestEvt = null
        if(teamTokens) {
          for(def ev in events) {
            try {
              def comp = ev?.competitions instanceof List && ev.competitions ? ev.competitions[0] : null
              def cops = (comp?.competitors instanceof List) ? (List)comp.competitors : []
              String blob = cops.collect { c ->
                def t = c?.team
                return [t?.displayName, t?.shortDisplayName, t?.abbreviation].findAll { it }.join(" ")
              }.join(" ").toLowerCase()
              if(teamTokens.any { tok -> blob.contains(tok) }) {
                bestEvt = ev
                break
              }
            } catch(e) {}
          }
        }
        if(!bestEvt) bestEvt = events[0]
        if(!bestEvt) continue

        try {
          def comp = bestEvt?.competitions instanceof List && bestEvt.competitions ? bestEvt.competitions[0] : null
          def cops = (comp?.competitors instanceof List) ? (List)comp.competitors : []
          def home = cops.find { it?.homeAway?.toString() == "home" } ?: (cops ? cops[0] : null)
          def away = cops.find { it?.homeAway?.toString() == "away" } ?: (cops?.size() > 1 ? cops[1] : null)

          String homeName = (home?.team?.shortDisplayName ?: home?.team?.displayName ?: "Home").toString()
          String awayName = (away?.team?.shortDisplayName ?: away?.team?.displayName ?: "Away").toString()
          String homeScore = (home?.score != null) ? home.score.toString() : ""
          String awayScore = (away?.score != null) ? away.score.toString() : ""
          String status = (comp?.status?.type?.shortDetail ?: comp?.status?.type?.detail ?: bestEvt?.status?.type?.shortDetail ?: "").toString()

          String gameLine
          if(homeScore && awayScore) {
            String sslmScore = formatScoreWithSsml("${awayScore}-${homeScore}")
            gameLine = "${awayName} ${sslmScore}, ${homeName}."
          } else if(homeScore || awayScore) {
            gameLine = "${awayName} ${awayScore}, ${homeName} ${homeScore}."
          } else {
            gameLine = "${awayName} vs ${homeName}."
          }

          String asOf = new Date().format("h:mm a", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
          String ans = "${gameLine} ${status ?: 'Status unavailable'}. As of ${asOf}."

          Map result = [
            answer: compactGeminiAnswer(ans),
            grounded: true,
            searchQueries: [rawQuery],
            sourceUrls: [uri],
            groundingMetadata: [source:"espn_scoreboard", league: (lg?.label ?: "")],
            finishReason: "sports_api_fallback",
            model: "espn-scoreboard",
            fallbackSource: "espn",
            sports_source: "espn_scoreboard",
            sports_league: (lg?.label ?: "")
          ]
          try {
            state.sportsApiCache = (state.sportsApiCache instanceof Map) ? state.sportsApiCache : [:]
            state.sportsApiCache[cacheKey] = [ts: now(), payload: result]
          } catch(ignore2) {}
          trackQueryResult("espn_scoreboard", rawQuery)
          return result
        } catch(e) {
          continue
        }
      }
      return null
    }

    long cooldownUntil = 0L
    try { cooldownUntil = (state?.gemini429CooldownUntil ?: 0L) as Long } catch(ignore) { cooldownUntil = 0L }
    if(cooldownUntil > now()) {
      if(sportsLive) {
        Map sportsApi = fetchEspnSportsFallback(queryText)
        if(sportsApi?.answer) {
          return sportsApi
        }
      }
      return [
        answer: "Google is busy right now. Please try again in a minute.",
        grounded: false,
        searchQueries: [],
        sourceUrls: [],
        groundingMetadata: [:],
        finishReason: "quota_cooldown",
        model: model,
        fallbackSource: "none",
        sports_source: "none",
        sports_league: ""
      ]
    }

    def callGemini = { String askText, String modelName, BigDecimal temp, Integer tokenLimit, String attemptLabel ->
      def resultText = ""
      Map groundingMetadata = [:]
      Map usageMetadata = [promptTokens: 0, outputTokens: 0, totalTokens: 0]
      List<String> sourceUrls = []
      List<String> searchQueries = []
      String finishReason = ""

      trackGeminiUsage(modelName, attemptLabel, queryCategory)

      if(!tryConsumeGeminiQuotaSlot((quotaProfile.maxCallsPerMin as Integer), (quotaProfile.maxCallsPerDay as Integer))) {
        finishReason = "local_quota_guard"
        log.debug "Gemini local quota guard blocked request. attempt=${attemptLabel}, tier=${quotaProfile.tier}, perMin=${quotaProfile.maxCallsPerMin}, perDay=${quotaProfile.maxCallsPerDay}"
        return [
          answer: "",
          grounded: false,
          searchQueries: [],
          sourceUrls: [],
          groundingMetadata: [:],
          finishReason: finishReason,
          model: modelName,
          fallbackSource: "gemini",
          sports_source: (sportsLive ? "gemini_search" : ""),
          sports_league: ""
        ]
      }

      Map reqBody = [
        system_instruction: [
          parts: [[text: sysPrompt]]
        ],
        contents: [
          [parts: [[text: askText]]]
        ],
        tools: [
          [google_search: [:]]
        ],
        generationConfig: [
          maxOutputTokens: tokenLimit,
          temperature: temp
        ]
      ]

      Map params = [
        uri: "https://generativelanguage.googleapis.com/v1beta/models/${java.net.URLEncoder.encode(modelName, 'UTF-8')}:generateContent",
        headers: [
          "x-goog-api-key": apiKey
        ],
        contentType: "application/json",
        requestContentType: "application/json",
        body: reqBody
      ]

      log.debug "Gemini fallback request starting. attempt=${attemptLabel}, model=${modelName}, liveInfo=${liveInfo}, sportsLive=${sportsLive}, query=${askText}"

      try {
        httpPostJson(params) { resp ->
          if(resp?.status in [200, 201]) {
            def data = resp?.data
            if(!(data instanceof Map)) {
              try {
                data = new JsonSlurper().parseText(resp?.data?.toString() ?: "")
              } catch(ignore) {}
            }
            resultText = extractGeminiText(data)
            groundingMetadata = extractGeminiGroundingMetadata(data)
            sourceUrls = extractGeminiGroundingSources(groundingMetadata)
            searchQueries = extractGeminiSearchQueries(groundingMetadata)
            finishReason = extractGeminiFinishReason(data)
            usageMetadata = extractGeminiUsageMetadata(data)
            trackGeminiCost(modelName, usageMetadata, (sourceUrls ? true : false))
            log.debug "Gemini fallback response ok. attempt=${attemptLabel}, grounded=${sourceUrls ? true : false}, searches=${searchQueries?.size() ?: 0}, sources=${sourceUrls?.size() ?: 0}, finishReason=${finishReason ?: 'unknown'}, rawLen=${resultText?.size() ?: 0}"
            log.debug "Gemini usage (${attemptLabel}): prompt=${usageMetadata.promptTokens ?: 0}, output=${usageMetadata.outputTokens ?: 0}, total=${usageMetadata.totalTokens ?: 0}"
            if(resultText) log.debug "Gemini raw answer (${attemptLabel}): ${resultText}"
            if(searchQueries) log.debug "Gemini search queries (${attemptLabel}): ${searchQueries}"
            if(sourceUrls) log.debug "Gemini grounding sources (${attemptLabel}): ${sourceUrls}"
          } else {
            log.debug "Gemini fallback returned status ${resp?.status} on attempt=${attemptLabel}"
            if((resp?.status as Integer) == 429) {
              finishReason = "quota_429"
              try { state.gemini429CooldownUntil = now() + ((gemini429CooldownSecs as Long) * 1000L) } catch(ignore) {}
            }
          }
        }
      } catch(e) {
        String em = (e?.toString() ?: "").toLowerCase()
        if(em.contains("status code: 429") || em.contains("too many requests") || em.contains("resourceexhausted")) {
          finishReason = "quota_429"
          try { state.gemini429CooldownUntil = now() + ((gemini429CooldownSecs as Long) * 1000L) } catch(ignore) {}
          log.debug "Gemini fallback rate-limited on attempt=${attemptLabel}; will use local fallback if available"
        } else {
          log.debug "Gemini fallback request failed on attempt=${attemptLabel}: ${e}"
        }
      }

      return [
        answer: compactGeminiAnswer(resultText),
        grounded: (sourceUrls ? true : false),
        searchQueries: searchQueries,
        sourceUrls: sourceUrls,
        groundingMetadata: groundingMetadata,
        usageMetadata: usageMetadata,
        finishReason: finishReason,
        model: modelName,
        fallbackSource: "gemini",
        sports_source: (sportsLive ? "gemini_search" : ""),
        sports_league: ""
      ]
    }

    BigDecimal firstTemp = liveInfo ? 0.25G : 0.5G
    Integer firstTokens = liveInfo ? (quotaProfile.liveFirstTokens as Integer) : (quotaProfile.firstTokens as Integer)
    Map first = callGemini(queryText, model, firstTemp, firstTokens, "first")
    Map chosen = first
    boolean geminiRateLimited = (["quota_429", "local_quota_guard"].contains((first?.finishReason ?: "").toString()))

    if(shouldRetryPaidGrounding(first, liveInfo, quotaProfile, queryCategory) && !geminiRateLimited) {
      def tz = location?.timeZone ?: TimeZone.getTimeZone("UTC")
      String nowStamp = new Date().format("yyyy-MM-dd HH:mm z", tz)
      String retryQuery = "${queryText}. Use live web results. Current time is ${nowStamp}."
      if(sportsLive) {
        retryQuery += " Focus on latest sports result with teams, score, game status, and update time."
      }
      String retryModel = chooseGeminiModel(baseModel, liveModel, quotaProfile, queryCategory)
      Map second = callGemini(retryQuery, retryModel, 0.2G, (quotaProfile.retryTokens as Integer), "retry_grounded")
      if(second?.answer) {
        boolean preferSecond = (second?.grounded == true) || !(first?.answer)
        if(preferSecond) chosen = second
      }
      geminiRateLimited = geminiRateLimited || (["quota_429", "local_quota_guard"].contains((second?.finishReason ?: "").toString()))
    }

    if(sportsLive && (chosen?.grounded != true || !(chosen?.answer))) {
      Map sportsApi = fetchEspnSportsFallback(queryText)
      if(sportsApi?.answer) {
        chosen = sportsApi
      }
    }

    if(sportsLive && !(chosen?.answer)) {
      return [
        answer: "Sorry, I can't get live sports right now. Please try again in a minute.",
        grounded: false,
        searchQueries: [],
        sourceUrls: [],
        groundingMetadata: [:],
        finishReason: "sports_unavailable",
        model: (chosen?.model ?: model),
        fallbackSource: "none",
        sports_source: "none",
        sports_league: ""
      ]
    }

    if(!(chosen?.answer) && geminiRateLimited) {
      return [
        answer: "Google is busy right now. Please try again in a minute.",
        grounded: false,
        searchQueries: [],
        sourceUrls: [],
        groundingMetadata: [:],
        finishReason: "quota_429",
        model: (chosen?.model ?: model),
        fallbackSource: "none",
        sports_source: "none",
        sports_league: ""
      ]
    }

    String resultText = (chosen?.answer ?: "").toString()
    if(resultText && liveInfo && chosen?.grounded != true && !resultText.toLowerCase().startsWith("last available update says")) {
      resultText = "Last available update says ${resultText}"
    }
    if(resultText && chosen?.finishReason == "sports_api_cache" && !resultText.toLowerCase().startsWith("last update says")) {
      resultText = "Last update says ${resultText}"
    }
    if(resultText) {
      log.debug "Gemini final answer (${resultText.size()} chars): ${resultText}"
      state.lastGeminiAnswer = resultText
      state.lastGeminiAt = now()
      state.lastGeminiModel = (chosen?.model ?: model)
      state.lastGeminiGrounded = (chosen?.grounded == true)
      state.lastGeminiQueries = (chosen?.searchQueries instanceof List) ? chosen.searchQueries : []
      state.lastGeminiSources = (chosen?.sourceUrls instanceof List) ? chosen.sourceUrls : []
      state.lastGeminiFinishReason = (chosen?.finishReason ?: "")
      state.lastGeminiAnswerLen = resultText.size()
      state.lastAiProvider = "gemini"
      state.lastAiModel = (chosen?.model ?: model)
      state.lastAiGrounded = (chosen?.grounded == true)
      String trackSource = (chosen?.fallbackSource ?: "gemini")
      if(chosen?.sports_source) trackSource = chosen.sports_source
      trackQueryResult(trackSource, queryText)
      return [
        answer: resultText,
        grounded: (chosen?.grounded == true),
        searchQueries: (chosen?.searchQueries instanceof List) ? chosen.searchQueries : [],
        sourceUrls: (chosen?.sourceUrls instanceof List) ? chosen.sourceUrls : [],
        groundingMetadata: (chosen?.groundingMetadata instanceof Map) ? chosen.groundingMetadata : [:],
        finishReason: (chosen?.finishReason ?: ""),
        model: (chosen?.model ?: model),
        fallbackSource: (chosen?.fallbackSource ?: "gemini"),
        sports_source: (chosen?.sports_source ?: ""),
        sports_league: (chosen?.sports_league ?: "")
      ]
    }
  } catch(e) {
    log.debug "Gemini fallback error: ${e}"
    state.lastGeminiError = e?.toString()
    try {
      if(isSportsLiveQuery(queryText)) {
        pruneSportsApiCache(Math.max((sportsCacheSecs ?: defaultSportsCacheSeconds()) * 20, 3600))
      }
    } catch(ignore) {}
  }

  return null
}

private boolean shouldUseGeminiFallback(Map resultMap, String answerText, String queryText, Map intent=null) {
  if(!aiFallbackReady()) return false
  if(!(queryText ?: "").toString().trim()) return false

  String mode = ((intent?.mode ?: resultMap?.mode ?: "") as String).toLowerCase()
  if(mode == "command") return false

  String err = ((resultMap?.error ?: "") as String).toLowerCase()
  if(err in [
    "no_device",
    "unknown_intent",
    "no_current_value",
    "no_history",
    "no_activity",
    "weather_unavailable",
    "weather_error",
    "no_candidates"
  ]) return true

  String low = (answerText ?: "").toString().toLowerCase()
  if(!low) return true
  if(low == "no answer.") return true
  if(low.contains("didn't understand")) return true
  if(low.contains("did not understand")) return true
  if(low.contains("couldn't get the weather")) return true
  if(low.contains("i can’t read")) return true
  if(low.contains("i couldn't find")) return true

  return false
}

private Map maybeUseGeminiFallback(String queryText, String answerText, Map resultMap=[:], Map intent=null) {
  if(!shouldUseGeminiFallback(resultMap, answerText, queryText, intent)) {
    return [answer: answerText, payload: (resultMap ?: [:]), used: false]
  }

  Map fallback = askSelectedFallback(queryText)
  if(!(fallback instanceof Map) || !fallback?.answer) {
    return [answer: answerText, payload: (resultMap ?: [:]), used: false]
  }

  String provider = (fallback?.provider ?: selectedFallbackProvider()).toString()

  Map merged = [:]
  if(resultMap instanceof Map) merged.putAll(resultMap)
  merged.ok = true
  merged.answer = fallback.answer
  merged.gemini_used = true
  merged.fallback_source = (fallback.fallbackSource ?: provider)
  merged.ai_provider = provider
  merged.ai_model = (fallback.model ?: "")
  merged.gemini_grounded = (fallback.grounded == true)
  merged.gemini_model = (fallback.model ?: "")
  if(fallback.sports_source) merged.sports_source = fallback.sports_source
  if(fallback.sports_league) merged.sports_league = fallback.sports_league
  if(fallback.searchQueries instanceof List) merged.gemini_search_queries = fallback.searchQueries
  if(fallback.sourceUrls instanceof List) merged.gemini_sources = fallback.sourceUrls
  try {
    dbgPut('geminiUsed', true)
    dbgPut('geminiGrounded', (fallback.grounded == true))
    dbgPut('geminiQueries', (fallback.searchQueries instanceof List) ? fallback.searchQueries : [])
    dbgPut('geminiSources', (fallback.sourceUrls instanceof List) ? fallback.sourceUrls : [])
    dbgPut('geminiFinishReason', (fallback.finishReason ?: '').toString())
    dbgPut('geminiAnswerLen', safeInt(fallback?.answer?.size(), 0))
    dbgPut('fallbackSource', (fallback?.fallbackSource ?: provider).toString())
    dbgPut('aiProvider', provider)
    dbgPut('aiModel', (fallback?.model ?: '').toString())
    dbgPut('sportsSource', (fallback?.sports_source ?: '').toString())
    dbgPut('sportsLeague', (fallback?.sports_league ?: '').toString())
    state.lastDebug = state.lastDebug ?: [:]
    state.lastDebug.geminiUsed = true
    state.lastDebug.geminiGrounded = (fallback.grounded == true)
    state.lastDebug.geminiQueries = (fallback.searchQueries instanceof List) ? fallback.searchQueries : []
    state.lastDebug.geminiSources = (fallback.sourceUrls instanceof List) ? fallback.sourceUrls : []
    state.lastDebug.geminiFinishReason = (fallback.finishReason ?: '').toString()
    state.lastDebug.geminiAnswerLen = safeInt(fallback?.answer?.size(), 0)
    state.lastDebug.fallbackSource = (fallback?.fallbackSource ?: provider).toString()
    state.lastDebug.aiProvider = provider
    state.lastDebug.aiModel = (fallback?.model ?: '').toString()
    state.lastDebug.sportsSource = (fallback?.sports_source ?: '').toString()
    state.lastDebug.sportsLeague = (fallback?.sports_league ?: '').toString()
  } catch(e) {}
  return [answer: fallback.answer, payload: merged, used: true]
}

/* =========================================================
   Bulk helpers (ALL commands)
   ========================================================= */
private List<String> hvStopwords() {
  return ["the","a","an","to","please","could","would","can","you","me","my","all","every","of","in","on","at","for","with","and",
          "are","is","was","were","be","been","have","has","had","do","does","did",
          "any","some","which","what","there","turn","switched","turned",
          "or","nor","but","if","so","yet","also","just","now","up","go",
          // state/value words that must not bleed into scope/room tokens
          "off","not","no","yes","open","closed","locked","unlocked",
          // common filler words that occasionally survive the base list
          "right","tell","show","give","put","get","let","use","see","hey","ok","okay","hi"]
}
private String hvNorm(String s) {
  return (s ?: "").toString().toLowerCase()
           .replaceAll(/[^a-z0-9\s]/," ")
           .replaceAll(/\s+/," ")
           .trim()
}

private String safeStripSecurityCodePhrase(String s) {
  try {
    if(this.metaClass.respondsTo(this, "stripSecurityCodePhrase", String)) {
      return stripSecurityCodePhrase(s)
    }
  } catch(e) {}
  return (s ?: "").toString()
}

private List<String> hvTokens(String s) {
  def t = hvNorm(s).split(/\s+/).findAll{ it }
  def stop = hvStopwords()
  return t.findAll{ !(it in stop) }
}
private boolean hvNameHasAll(def dev, List<String> toks) {
  if(!dev || !toks) return false
  def n = hvNorm(dev?.displayName ?: dev?.name ?: "")
  for(String tok in toks) {
    if(!tok) continue
    if(!n.contains(tok)) return false
  }
  return true
}
private boolean hvNameHasAny(def dev, List<String> toks) {
  if(!dev || !toks) return false
  def n = hvNorm(dev?.displayName ?: dev?.name ?: "")
  for(String tok in toks) {
    if(tok && n.contains(tok)) return true
  }
  return false
}
private List bulkFindSwitches(List<String> scopeTokens, List<String> mustHaveAnyNameTokens) {
  def devs = (location?.devices ?: []).findAll { d ->
    try {
      if(!d) return false
      // capability check first
      if(d.metaClass?.respondsTo(d,"hasCapability", String) && d.hasCapability("Switch")) return true
      // fallback: commands
      if(d?.hasCommand("on") || d?.hasCommand("off")) return true
    } catch(e) {}
    return false
  }
  def out = devs.findAll { d ->
    hvNameHasAll(d, scopeTokens) && (mustHaveAnyNameTokens ? hvNameHasAny(d, mustHaveAnyNameTokens) : true)
  }
  return out
}
private List bulkFindLocks(List<String> scopeTokens, List<String> mustHaveAnyNameTokens) {
  def devs = (location?.devices ?: []).findAll { d ->
    try {
      if(!d) return false
      if(d.metaClass?.respondsTo(d,"hasCapability", String) && d.hasCapability("Lock")) return true
      if(d?.hasCommand("lock") || d?.hasCommand("unlock")) return true
    } catch(e) {}
    return false
  }
  def out = devs.findAll { d ->
    hvNameHasAll(d, scopeTokens) && (mustHaveAnyNameTokens ? hvNameHasAny(d, mustHaveAnyNameTokens) : true)
  }
  return out
}
private String bulkDoSwitches(String action, List devs) {
  if(!devs) return "I couldn't find matching devices for that request."
  int ok=0; def failed=[]
  devs.each{ d ->
    try {
      if(action=="on") d.on() else d.off()
      ok++
    } catch(e) { failed << (d?.displayName ?: d?.name ?: "Unknown") }
  }
  String phrase = (action=="on" ? "turning on" : "turning off")
  if(!failed) return "Sure. ${phrase.capitalize()} ${ok} device${ok==1?'':'s'}."
  return "Sure. ${phrase.capitalize()} ${ok} device${ok==1?'':'s'}. I wasn't able to update: ${failed.join(', ')}."
}
private String bulkDoLocks(String action, List devs) {
  if(!devs) return "I couldn't find matching locks for that request."
  int ok=0; def failed=[]
  devs.each{ d ->
    try {
      if(action=="unlock") d.unlock() else d.lock()
      ok++
    } catch(e) { failed << (d?.displayName ?: d?.name ?: "Unknown") }
  }
  String phrase = (action=="unlock" ? "unlocking" : "locking")
  if(!failed) return "Sure. ${phrase.capitalize()} ${ok} lock${ok==1?'':'s'}."
  return "Sure. ${phrase.capitalize()} ${ok} lock${ok==1?'':'s'}. I wasn't able to update: ${failed.join(', ')}."
}

/* =========================================================
   MODE SEPARATION: control vs status
   ========================================================= */
private boolean hvIsControlQuery(String qIn) {
  String q = (qIn ?: "").toString().toLowerCase()
  // Strong command verbs
  if(q =~ /(?i)\b(turn|switch|set)\b/ && (q.contains(" on") || q.contains(" off"))) return true
  if(q =~ /(?i)\b(lock|unlock)\b/) return true
  if(q =~ /(?i)\b(set|dim|raise|lower)\b/) return true
  // Treat open/close as control only for imperative phrasing (not status questions like "is front door open")
  if(q =~ /(?i)^\s*(open|close)\b/) return true
  if(q =~ /(?i)\b(please|can you|could you)\s+(open|close)\b/) return true
  if(q =~ /(?i)\b(arm|disarm)\b/) return true
  // "all" bulk actions are control
  if(q =~ /(?i)\b(lock|unlock)\s+all\b/) return true
  if(q =~ /(?i)\b(turn|switch|set)\s+all\b/) return true
  return false
}

private boolean hvIsStatusQuery(String qIn) {
  String q = (qIn ?: "").toString().toLowerCase()
  // Questions / status phrases
  if(q.startsWith("is ") || q.startsWith("are ") || q.startsWith("what ") || q.startsWith("which ") || q.startsWith("when ") || q.startsWith("how ")) return true
  if(q.contains(" status")) return true
  if(q.contains(" battery")) return true
  if(q.contains(" last ") || q.contains(" last activity")) return true
  if(q.contains(" how long")) return true
  if(q.contains(" any ") && (q.contains(" open") || q.contains(" unlocked") || q.contains(" on"))) return true
  return false
}

private String hvModeForQuery(String qIn) {
  // Hard rule: control wins if both match
  if(hvIsControlQuery(qIn)) return "control"
  if(hvIsStatusQuery(qIn)) return "status"
  // Default: status (safer)
  return "status"
}

/* =========================================================
   DEVICE RESOLUTION WRAPPERS (keeps control vs status separate)
   ========================================================= */
private def matchDeviceForControl(String q, Map constraint=[:]) {
  // Control must resolve to devices that can execute commands (on/off, lock/unlock, etc.)
  def c = (constraint ?: [:]).clone()
  c.mode = "control"
  return matchDeviceFromQuery(q, c)
}
private def matchDeviceForStatus(String q, Map constraint=[:]) {
  // Status must resolve to devices that have attributes / states
  def c = (constraint ?: [:]).clone()
  c.mode = "status"
  return matchDeviceFromQuery(q, c)
}

/* =========================================================
   BULK CONTROL HELPERS (Switches/Lights)
   ========================================================= */
private List<String> bcStop() {
  return ["the","a","an","to","please","could","would","can","you","me","my","all","every","of","in","on","at","for","with","and"]
}
private String bcNorm(String s) {
  return (s ?: "").toString().toLowerCase()
           .replaceAll(/[^a-z0-9\s]/," ")
           .replaceAll(/\s+/," ")
           .trim()
}
private List<String> bcTokens(String s) {
  def t = bcNorm(s).split(/\s+/).findAll{ it }
  def stop = bcStop()
  return t.findAll{ !(it in stop) }
}
private boolean bcHasAll(def dev, List<String> toks) {
  if(!dev || !toks) return false
  def n = bcNorm(dev?.displayName ?: dev?.name ?: "")
  for(String tok in toks) {
    if(tok && !n.contains(tok)) return false
  }
  return true
}
private boolean bcHasAny(def dev, List<String> toks) {
  if(!dev || !toks) return false
  def n = bcNorm(dev?.displayName ?: dev?.name ?: "")
  for(String tok in toks) {
    if(tok && n.contains(tok)) return true
  }
  return false
}
private boolean bcCanSwitch(def d) {
  try {
    if(!d) return false
    boolean byCap = false
    try {
      if(d.metaClass?.respondsTo(d,"hasCapability", String)) {
        byCap = (d.hasCapability("Switch") == true)
      }
    } catch(e) {}
    boolean byCmd = false
    try {
      byCmd = (d?.hasCommand("on") || d?.hasCommand("off"))
    } catch(e2) {}
    return (byCap || byCmd)
  } catch(e3) {
    return false
  }
}
private List bulkFindLightsByName(String scopePhrase) {
  // scopePhrase example: "bathroom" or "master bathroom"
  def scopeToks = bcTokens(scopePhrase).findAll{ !(it in ["light","lights","lamp","lamps"]) }
  def pool = (qaDevices ?: location?.devices ?: [])
  def devs = pool.findAll { d ->
    try {
      if(!d) return false
      // Ensure it's switch-capable (capability OR command support)
      if(!bcCanSwitch(d)) return false
      // Require light/lamp in the name so we don't hit heaters/fans/etc even if they have Switch
      if(!bcHasAny(d, ["light","lights","lamp","lamps"])) return false
      // Require all scope tokens (e.g., bathroom)
      return bcHasAll(d, scopeToks)
    } catch(e) { return false }
  }
  return devs
}
private String bulkDoLights(String action, List devs) {
  if(!devs) return "I couldn't find matching lights for that request."
  int ok=0
  def failed=[]
  devs.each { d ->
    try {
      if(action=="on") d.on() else d.off()
      ok++
    } catch(e) {
      failed << (d?.displayName ?: d?.name ?: "Unknown")
    }
  }
  String phrase = (action=="on" ? "turning on" : "turning off")
  if(!failed) return "Sure. ${phrase.capitalize()} ${ok} light${ok==1?'':'s'}."
  return "Sure. ${phrase.capitalize()} ${ok} light${ok==1?'':'s'}. I wasn't able to update: ${failed.join(', ')}."
}
private Map parseBulkLightCommand(String raw) {
  String s = bcNorm(safeStripSecurityCodePhrase(raw))
  if(!s) return null
  if(!(s =~ /\b(turn|switch|set)\b/).find()) return null
  if(!(s =~ /\ball\b/).find()) return null
  if(!(s =~ /\b(lights?|lamps?)\b/).find()) return null

  def m1 = (s =~ /(?:turn|switch|set)\s+all\s+(.+?)\s+(?:lights?|lamps?)\s+(on|off)\b/)
  if(m1 && m1.find()) {
    return [scope:(m1.group(1) ?: "").trim(), action:(m1.group(2) ?: "").toLowerCase()]
  }

  def m2 = (s =~ /(?:turn|switch|set)\s+(on|off)\s+all\s+(.+?)\s+(?:lights?|lamps?)\b/)
  if(m2 && m2.find()) {
    return [scope:(m2.group(2) ?: "").trim(), action:(m2.group(1) ?: "").toLowerCase()]
  }

  String action = null
  if((s =~ /\boff\b/).find()) action = "off"
  else if((s =~ /\bon\b/).find()) action = "on"
  if(!action) return null

  String scope = s
    .replaceAll(/\b(turn|switch|set|all|lights?|lamps?|on|off|the|please)\b/, " ")
    .replaceAll(/\s+/, " ")
    .trim()
  return [scope:scope, action:action]
}

/* =========================================================
   Debug helpers (Test page diagnostics)
   ========================================================= */
private void dbgInit(String q, String mode=null) {
  try {
    state.lastDebug = state.lastDebug ?: [:]
    state.lastDebug.ts = now()
    state.lastDebug.q = (q ?: "").toString()
    state.lastDebug.sDev = (state?.sdev ?: "").toString().toLowerCase()
    state.lastDebug.qNorm = (q ?: "").toString().toLowerCase()
    if(mode != null) state.lastDebug.mode = (mode ?: "").toString()
    state.lastDebug.route = []
    // Clear per-request sub-objects
    state.lastDebug.bulk = [:]
    state.lastDebug.candidatesTop = []
    state.lastDebug.bestDev = ""
    state.lastDebug.dev = ""
    state.lastDebug.intent = ""
    state.lastDebug.error = ""
  } catch(e) {}
}
private void dbgRoute(String step) {
  try {
    state.lastDebug = state.lastDebug ?: [:]
    state.lastDebug.route = (state.lastDebug.route instanceof List) ? state.lastDebug.route : []
    state.lastDebug.route << (step ?: "")
    Integer keep = maxDebugRouteSteps()
    if(keep == null || keep < 10) keep = 10
    if((state.lastDebug.route as List).size() > keep) state.lastDebug.route = (state.lastDebug.route as List).takeRight(keep)
  } catch(e) {}
}
private void dbgPut(String k, v) {
  try {
    state.lastDebug = state.lastDebug ?: [:]
    state.lastDebug[k] = v
  } catch(e) {}
}

def handleAsk() {
  log.debug "--------------------------------------------------------"

  try {
    if(!isAuthorizedRequest()) {
      ans = "Unauthorized"
      sendMessage(ans)
      return respondAsk(ans, 401, [error: "unauthorized"])
    }

    if(!rateLimitOk()) {
      ans = "Rate limit exceeded. Try again in a moment."
      sendMessage(ans)
      return respondAsk(ans, 429, [error: "rate_limited"])
    }

    def q = (params.q ?: params.text ?: "").toString().trim()
    // Restore detailed debug logging, but redact access_token
    def paramsRedacted = params instanceof Map ? params.collect { k,v -> k == 'access_token' ? 'access_token:[REDACTED]' : "${k}:${v}" }.join(", ") : params
    log.debug "-------------------- In handleAsk --------------------"
    log.debug "PARAMS RAW: [${paramsRedacted}]"
    log.debug "Q VALUE: ${q}"
    state.sdev = (params.d ?: "").toString().trim()
    state.callbackUrl = (params.callbackUrl ?: "").toString().trim()

    // Extract satRoom from query if present (e.g., 'are the bathroom lights on' -> satRoom = 'bathroom')
    String satRoom = null
    def roomMatch = (q =~ /\b(the|in|of)?\s*([a-zA-Z0-9 ]+?)\s+(lights?|lamps?|fans?|switches?)\b/)
    if(roomMatch && roomMatch.find()) {
      satRoom = roomMatch.group(2)?.trim()?.toLowerCase()
    }
    // Store for debugging
    state.satRoom = satRoom

    // Only force group status mode for light/lamp/fan questions (not control commands)
    def earlyGroupWords = ["light","lights","lamp","lamps","fan","fans"]
    def qNorm = q.toLowerCase()
    boolean hasGroupWord = earlyGroupWords.any { qNorm.contains(it) }
    boolean isQuestion = (qNorm.contains("are ") || qNorm.contains("which") || qNorm.contains("what") || qNorm.contains("?") )
    if(hasGroupWord && isQuestion) {
      // Before forcing group routing, check if a specific named device appears in the query.
      // A genuine device name must:
      //   (a) be a substring of the normalised query, AND
      //   (b) contain at least one token that is NOT just a room word or a group/type word
      //       (prevents "Bathroom Lights" scene name from hijacking "bathroom lights" group queries)
      List<String> pureGroupWords = ["light","lights","lamp","lamps","fan","fans","ceiling","sconce",
                                     "switch","switches","outlet","outlets","plug","plugs"]
      boolean namedDeviceInQuery = false
      try {
        def devIdx = (state?.devIndex ?: buildDeviceIndex()) ?: [:]
        String qNormFull = normalize(q)
        List<String> roomTokens = satRoom ? satRoom.tokenize(" ").collect{ it.trim().toLowerCase() }.findAll{ it } : []
        devIdx.each { nameNorm, did ->
          if(namedDeviceInQuery) return
          if(!nameNorm || nameNorm.size() <= 4) return
          if(!qNormFull.contains(nameNorm)) return
          // Require at least one token in the device name that isn't the room name or a pure group word
          List<String> nameToks = nameNorm.tokenize(" ").findAll{ it.size() >= 3 }
          boolean hasDistinctToken = nameToks.any { tok -> !(tok in pureGroupWords) && !(tok in roomTokens) }
          if(hasDistinctToken) namedDeviceInQuery = true
        }
      } catch(e) { log.debug "earlyGroupCheck err: ${e}" }

      if(!namedDeviceInQuery) {
        // No specific device name found – treat as a group question
        def wantValue = "on"
        if(qNorm.contains(" off") || qNorm.contains("are off") || qNorm.contains("turned off")) wantValue = "off"
        // Determine group type from query (lights vs fans)
        String earlyGroup = "lights"
        String earlyAttr  = "switch"
        if((qNorm.contains("fan") || qNorm.contains("fans")) &&
           !(qNorm.contains("light") || qNorm.contains("lamp"))) {
          earlyGroup = "fans"
        }
        def groupIntent = [mode:"device_group_status", group:earlyGroup, attr:earlyAttr, wantValue:wantValue, _forced:true, _satRoom:satRoom]
        def gRes = routeGroup(q, groupIntent)
        String ans = (gRes?.answer ?: "No answer.").toString()
        sendMessage(ans)
        return respondText(ans)
      }
      // Named device found – fall through to normal device routing below
    }

// =========================================================
// CONTROL: BULK lights by name tokens
// =========================================================
def bulkCmd = parseBulkLightCommand(q)
if(bulkCmd) {
  String action = (bulkCmd.action ?: "").toString().toLowerCase()
  String scope = (bulkCmd.scope ?: "").toString().trim()
  try {
    state.lastDebug.mode = "control_bulk"
    state.lastDebug.bulk = state.lastDebug.bulk ?: [:]
    state.lastDebug.bulk.type = 'lights'
    state.lastDebug.bulk.action = action
    state.lastDebug.bulk.scope = scope
    state.lastDebug.bulk.scopeTokens = bcTokens(scope)
    state.lastDebug.bulk.nameMustContainAny = ['light','lamp']
  } catch(e) {}
  def targets = bulkFindLightsByName(scope)
  try { state.lastDebug.bulk.targetsCount = targets?.size() ?: 0; state.lastDebug.bulk.targets = (targets ?: []).collect{ it?.displayName ?: it?.name ?: '?' } } catch(e) {}
  dbgRoute('bulk_lights_match')
  log.debug "BULK LIGHTS (${action}) scope='${scope}' targets=${targets?.size() ?: 0} -> " + (targets ? targets.collect{ it?.displayName ?: it?.name ?: "?" }.join(", ") : "")
  ans = bulkDoLights(action, targets)
  sendMessage(ans)
  return respondText(ans)
}

  dbgInit(q)
  dbgRoute('handleAsk_start')

// =========================================================
// BULK "ALL" COMMANDS (no single device needed)
// =========================================================
String qStrip = hvNorm(safeStripSecurityCodePhrase(q))
// 1) Switches/Lights: "turn all <scope> lights off|on"
def mSw = (qStrip =~ /(?i)\b(turn|switch)\s+(on|off)\s+all\s+(.+?)\s+(lights?|lamps?)\b/)
boolean mSwFirst = (mSw && mSw.find())
boolean mSwSecond = false
if(!mSwFirst) {
  mSw = (qStrip =~ /(?i)\b(turn|switch)\s+all\s+(.+?)\s+(lights?|lamps?)\s+(on|off)\b/)
  mSwSecond = (mSw && mSw.find())
}
if(mSwFirst || mSwSecond) {
  String action = ((mSw.groupCount() >= 4 && (mSw.group(2) in ["on","off"])) ? mSw.group(2) : mSw.group(mSw.groupCount())).toString().toLowerCase()
  String scope = (mSw.groupCount() >= 4 && (mSw.group(3) != null) ? mSw.group(3) : mSw.group(2)).toString()
  def scopeTokens = hvTokens(scope)
  def targets = bulkFindSwitches(scopeTokens, ["light","lamp"])
  ans = bulkDoSwitches(action, targets)
  sendMessage(ans)
  return respondText(ans)
}

// 2) Locks: "lock all <scope> doors/locks ..." (security code required)
def mLk = (qStrip =~ /(?i)\b(lock|unlock)\s+all\s+(.+?)\s+(doors?|locks?)\b/)
boolean mLkScoped = (mLk && mLk.find())
boolean mLkGlobal = false
if(!mLkScoped) {
  mLk = (qStrip =~ /(?i)\b(lock|unlock)\s+all\s+(doors?|locks?)\b/)
  mLkGlobal = (mLk && mLk.find())
}
if(mLkScoped || mLkGlobal) {
  String act = (mLk.group(1) ?: "").toString().toLowerCase()
  String kind = null
  String scope = ""
  if(mLk.groupCount() >= 3 && mLk.group(3)) { scope = (mLk.group(2) ?: ""); kind = (mLk.group(3) ?: "") }
  else if(mLk.groupCount() >= 2 && mLk.group(2)) { kind = (mLk.group(2) ?: "") }
  kind = (kind ?: "").toString().toLowerCase()
  def scopeTokens = hvTokens(scope)
  def mustAny = []
  if(kind.startsWith("door")) mustAny = ["door"]
  else if(kind.startsWith("lock")) mustAny = ["lock","door"]

  def providedCode = extractSecurityCode(q)
  log.debug "SECURITY CODE PARSED (bulk lock): ${providedCode ?: '(none)'}"
  def gate = authorizeRiskyAction(null, providedCode)
  if(gate?.ok != true) {
    ans = (gate?.msg ?: "Unable to complete request.")
    riskyAudit("lock_bulk", act, (scope ?: kind ?: "all"), false, "security_denied", q, (providedCode != null))
    sendMessage(ans)
    return respondText(ans)
  }

  def replay = replayGuardForRisky("lock_bulk_${act}_${kind ?: 'all'}")
  if(replay?.ok != true) {
    ans = (replay?.msg ?: "Unable to complete request.")
    riskyAudit("lock_bulk", act, (scope ?: kind ?: "all"), false, (replay?.error ?: "replay_denied"), q, (providedCode != null))
    sendMessage(ans)
    return respondText(ans)
  }

  def targets = bulkFindLocks(scopeTokens, mustAny)
  targets = filterRiskyLockTargets(targets)
  if(!targets) {
    ans = "I couldn't find any allowed locks that match that request."
    riskyAudit("lock_bulk", act, (scope ?: kind ?: "all"), false, "allowlist_empty", q, (providedCode != null))
    sendMessage(ans)
    return respondText(ans)
  }

  ans = bulkDoLocks(act, targets)
  riskyAudit("lock_bulk", act, (scope ?: kind ?: "all"), true, "executed", q, (providedCode != null))
  sendMessage(ans)
  return respondText(ans)
}


      if(!q) {
          ans = "No query provided."
		  sendMessage(ans)
          return respondAsk(ans, 200, [ok:false, error:"no_query"])
      }
    def qMatch = safeStripSecurityCodePhrase(q)
    def query = normalize(qMatch)
    state.lastQuestion = q

    // Ignore placeholder transcripts from STT silence/empty captures.
    if(query in ["no answer", "no speech detected", "no speech", "empty query"]) {
      log.debug "Ignoring placeholder transcript: ${query}"
      return respondAsk("", 200, [ok:false, error:"empty_transcript"])
    }

    // Optional: structured NLU (params.nlu) can short-circuit routing
    def nlu = parseNluParam()
    def nluIntent = nlu ? nluToInternalIntent(nlu) : null

    // If NLU identified a group intent, route it immediately (no device needed)
    if(nluIntent?.mode && ROUTE_GROUP().containsKey(nluIntent.mode)) {
      def gRes = routeGroup(query, nluIntent)
      String ans = (gRes?.answer ?: "No answer.").toString()
      def fb = maybeUseGeminiFallback(q, ans, (gRes ?: [:]), nluIntent)
      ans = (fb?.answer ?: ans).toString()
      gRes = (fb?.payload instanceof Map) ? fb.payload : (gRes ?: [:])
      state.lastAnswer = ans
      state.lastIntent = "nlu-group:${nluIntent.mode}"
      sendMessage(ans)
      return respondAsk(ans, 200, [mode:nluIntent.mode] + (gRes ?: [:]))
    }


    // High-priority whole-house intents (run before device matching)
    def hp = detectGroupIntent(query)
    if(hp?.mode in ["house_summary","secure_check","battery_report","last_activity","device_group_status","any_open","any_state","hsm_arm_home","hsm_arm_away","hsm_disarm","hub_mode","hsm_status","time_now","weather_today","weather_tomorrow"]) {

// Risky group actions (HSM arm/disarm) require security code
if(hp?.risky || (hp?.mode in ["hsm_arm_home","hsm_arm_away","hsm_disarm"])) {
  if(bruteForceLockedOut(null)) {
    String ans = riskyDeny("Too many failed attempts. Try again later.")
    riskyAudit("hsm", (hp?.mode ?: ""), "hsm", false, "security_lockout", q, false)
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"security_lockout"])
  }
  def appCodeSet = (settings?.securityCode ?: "").toString().trim()
  def codeProvided = extractSecurityCode(q)
  if(!appCodeSet) {
    String ans = riskyDeny("Security code is not configured.")
    riskyAudit("hsm", (hp?.mode ?: ""), "hsm", false, "security_not_configured", q, false)
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"security_not_configured"])
  }
  if(!codeProvided) {
    bruteForceRecordFail(null)
    String ans = riskyDeny("Please include the security code.")
    riskyAudit("hsm", (hp?.mode ?: ""), "hsm", false, "security_code_missing", q, false)
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"security_code_missing"])
  }
  if(codeProvided != appCodeSet) {
    bruteForceRecordFail(null)
    String ans = riskyDeny("Security code incorrect.")
    riskyAudit("hsm", (hp?.mode ?: ""), "hsm", false, "security_code_invalid", q, true)
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"security_code_invalid"])
  }
  bruteForceReset(null)

  def hsmReplay = replayGuardForRisky("hsm_${hp?.mode ?: 'action'}")
  if(hsmReplay?.ok != true) {
    String ans = (hsmReplay?.msg ?: "Unable to complete request.")
    riskyAudit("hsm", (hp?.mode ?: ""), "hsm", false, (hsmReplay?.error ?: "replay_denied"), q, (codeProvided != null))
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:(hsmReplay?.error ?: "replay_denied")])
  }
}
      def gRes = answerForGroup(query, hp)
      String ans = (gRes?.answer ?: "No answer.").toString()
      def fb = maybeUseGeminiFallback(q, ans, (gRes ?: [:]), hp)
      ans = (fb?.answer ?: ans).toString()
      gRes = (fb?.payload instanceof Map) ? fb.payload : (gRes ?: [:])
      state.lastAnswer = ans
      state.lastIntent = "group:${hp.mode}"
      sendMessage(ans)
      return respondAsk(ans, 200, [mode:hp.mode] + (gRes ?: [:]))
    }

    // Device resolution: prefer NLU device slots, else fall back to query text matching
    def constraint = inferDeviceConstraint(query, nluIntent)
    def dev = null
    if(nlu && nluIntent?.mode) {
      def r = resolveDeviceFromNlu(nlu, constraint)
      dev = r?.device
      if(dev) state.lastDeviceName = r?.deviceName
    }

    // Match device exactly like Test Page does (query text)
    if(!dev) dev = ((mode=='control') ? matchDeviceForControl(query, constraint) : matchDeviceForStatus(query, constraint))

    log.debug "dev - ${dev}"
    dbgPut('dev', (dev?.displayName ?: dev?.name ?: ''))
    try { state.lastDebug.dev = (dev?.displayName ?: dev?.name ?: '') } catch(e) {}

    if(!dev) {

      // Group questions that don't name a specific device (e.g., "are any doors open")
      def gIntent = detectGroupIntent(query)
      if(gIntent) {
        def gRes = answerForGroup(query, gIntent)
        String ans = (gRes?.answer ?: "No answer.").toString()
        def fb = maybeUseGeminiFallback(q, ans, (gRes ?: [:]), gIntent)
        ans = (fb?.answer ?: ans).toString()
        gRes = (fb?.payload instanceof Map) ? fb.payload : (gRes ?: [:])
        state.lastAnswer = ans
        state.lastIntent = "group:${gIntent?.mode ?: ''}"
        sendMessage(ans)
        return respondAsk(ans, 200, [mode:gIntent?.mode] + (gRes ?: [:]))
      }

            // Fallback / clarify: suggest likely devices or example phrases
      def dd = disambiguateOrPick(query)
      def candNames = []
      try {
        candNames = (dd?.candidates instanceof List) ? dd.candidates.collect{ it?.displayName ?: it?.name } : []
      } catch(e) { }
      String ans = clarifyIntent(q, candNames)
      def fb = maybeUseGeminiFallback(q, ans, [ok:false, error:"no_device"], null)
      ans = (fb?.answer ?: ans).toString()
      def outPayload = (fb?.payload instanceof Map) ? fb.payload : [ok:false, error:"no_device"]

      state.lastAnswer = ans
      state.lastIntent = "no_device"
      sendMessage(ans)
      return respondAsk(ans, 200, outPayload)
    }

    state.lastDeviceName = dev?.displayName ?: dev?.name

    def intent
    if(nluIntent?.mode in ["device_status","duration_query","why_did_action"]) {
      intent = [mode:nluIntent.mode]
    } else {
      intent = detectIntent(query, dev)
    }
    String resolvedMode = intent?.mode ?: "unknown"
    state.lastIntent = resolvedMode
    if(resolvedMode == "unknown") {
      log.warn "UNMATCHED_QUERY: q=${q} | normalised=${query} | device=${dev?.displayName ?: 'none'}"
    }
    // Risky command security gate (security code only)
    // - If Lock Code Manager has codes for the lock, we require a code and validate against LCM.
    // - Otherwise, if this app has a securityCode set, we require/validate against that.
    def codeProvided = extractSecurityCode(q)
    if((q ?: "").toString().toLowerCase().contains("security code")) { log.debug "SECURITY CODE PARSED: ${codeProvided ?: '(none)'}" }
    def appCodeSet = (settings?.securityCode ?: "").toString().trim()
    def lcmCodesExist = false
    try { lcmCodesExist = lcmHasAnyCodes(dev) } catch(e) { lcmCodesExist = false }

    def securityRequired = (lcmCodesExist || (appCodeSet?.size() > 0))

    if(intent?.mode == "command" && intent?.risky && securityRequired) {

  if((intent?.attr == "lock") && !isRiskyLockAllowed(dev)) {
    String ans = riskyDeny("That lock is not allowed for risky voice actions.")
    riskyAudit("lock_device", (intent?.cmd ?: ""), (dev?.displayName ?: dev?.name ?: ""), false, "allowlist_denied", q, (codeProvided != null))
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"risky_target_not_allowed"])
  }

  if(bruteForceLockedOut(dev)) {
    String ans = riskyDeny("Too many failed attempts. Try again later.")
    riskyAudit("lock_device", (intent?.cmd ?: ""), (dev?.displayName ?: dev?.name ?: ""), false, "security_lockout", q, (codeProvided != null))
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"security_lockout"])
  }

  if(codeProvided) {
    if(!hasValidSecurityCode(dev, q)) {
      bruteForceRecordFail(dev)
      String ans = riskyDeny("Security code incorrect.")
      riskyAudit("lock_device", (intent?.cmd ?: ""), (dev?.displayName ?: dev?.name ?: ""), false, "security_code_invalid", q, true)
      state.lastAnswer = ans
      state.lastIntent = "confirm_action"
      sendMessage(ans)
      return respondAsk(ans, 200, [ok:false, error:"security_code_invalid"])
    }
    bruteForceReset(dev)

    def lockReplay = replayGuardForRisky("lock_${intent?.cmd ?: 'command'}_${dev?.id ?: 'nodev'}")
    if(lockReplay?.ok != true) {
      String ans = (lockReplay?.msg ?: "Unable to complete request.")
      riskyAudit("lock_device", (intent?.cmd ?: ""), (dev?.displayName ?: dev?.name ?: ""), false, (lockReplay?.error ?: "replay_denied"), q, true)
      state.lastAnswer = ans
      state.lastIntent = "confirm_action"
      sendMessage(ans)
      return respondAsk(ans, 200, [ok:false, error:(lockReplay?.error ?: "replay_denied")])
    }
  } else {
    bruteForceRecordFail(dev)
    String ans = riskyDeny("Please include the security code.")
    riskyAudit("lock_device", (intent?.cmd ?: ""), (dev?.displayName ?: dev?.name ?: ""), false, "security_code_missing", q, false)
    state.lastAnswer = ans
    state.lastIntent = "confirm_action"
    sendMessage(ans)
    return respondAsk(ans, 200, [ok:false, error:"security_code_missing"])
  }
}

def resultMap = answerFor(dev, query, intent)
    String ans = (resultMap?.answer ?: "No answer.").toString()
    def fb = maybeUseGeminiFallback(q, ans, (resultMap ?: [:]), intent)
    ans = (fb?.answer ?: ans).toString()
    resultMap = (fb?.payload instanceof Map) ? fb.payload : (resultMap ?: [:])
    state.lastAnswer = ans
    if(intent?.mode == "command" && intent?.risky && intent?.attr == "lock") {
      riskyAudit("lock_device", (intent?.cmd ?: ""), (dev?.displayName ?: dev?.name ?: ""), (resultMap?.ok == true), ((resultMap?.ok == true) ? "executed" : "command_failed"), q, (codeProvided != null))
    }
	sendMessage(ans)
    return respondAsk(ans, 200, (resultMap ?: [:]))

  } catch(ex) {
    log.debug "handleAsk error: ${ex}"
    state.lastError = ex?.toString()
    return respondAsk("Sorry, I ran into a problem handling that request.", 200, [ok:false, error:"handle_ask_error"])
  }
}


/* =========================================================
   Explainability (why_did_action)
   - Logs actions executed by THIS endpoint (command mode)
   - Answers "why did <device> ..." using state.actionLog
   ========================================================= */
def recordAction(dev, String devName, String attr, String cmd, def value, String queryText) {
  try {
    if(state.actionLog == null) state.actionLog = []
    def entry = [
      ts: now(),
      deviceId: (dev?.id?.toString() ?: ""),
      device: (devName ?: ""),
      attribute: (attr ?: ""),
      command: (cmd ?: ""),
      value: (value != null ? value : null),
      query: (queryText ?: "")
    ]
    state.actionLog << entry
    if(state.actionLog.size() > 80) state.actionLog = state.actionLog.takeRight(80)
  } catch(e) { }
}

def findLastAction(String deviceId, String wantCmd = null, String wantAttr = null) {
  try {
    def log = (state.actionLog instanceof List) ? (state.actionLog as List) : []
    if(!log) return null
    for(int i=log.size()-1; i>=0; i--) {
      def e = log[i]
      if(deviceId && (e?.deviceId?.toString() != deviceId.toString())) continue
      if(wantCmd && e?.command?.toString() != wantCmd) continue
      if(wantAttr && e?.attribute?.toString() != wantAttr) continue
      return e
    }
  } catch(ex) { }
  return null
}


/* =========================================================
   Fallback / Clarify intent
   - Returns a short Tasker-friendly prompt when we can't route
   ========================================================= */

/* =========================================================
   Confirm action via spoken security code
   Example: "unlock the front door lock security code 1234"
   ========================================================= */
private String digitsFromSpeech(String raw, Integer maxDigits = 8) {
  if(!raw) return ""
  int cap = (maxDigits == null || maxDigits <= 0) ? 8 : maxDigits

  Map wordMap = [
    "zero":"0", "oh":"0", "o":"0",
    "one":"1", "won":"1",
    "two":"2", "to":"2", "too":"2",
    "three":"3",
    "four":"4", "for":"4", "fore":"4",
    "five":"5",
    "six":"6",
    "seven":"7",
    "eight":"8", "ate":"8",
    "nine":"9"
  ]

  String s = raw.toString().toLowerCase()
  s = s.replaceAll(/[^a-z0-9\s\-]/, " ")
  s = s.replaceAll(/\s+/, " ").trim()

  String out = ""
  def toks = s.split(/[\s\-]+/).findAll { it }
  toks.each { t ->
    String tok = t.toString()
    if(tok ==~ /\d+/) {
      tok.each { ch ->
        if(out.size() < cap) out += ch.toString()
      }
    } else {
      String d = wordMap[tok]
      if(d && out.size() < cap) out += d
    }
    if(out.size() >= cap) return
  }
  return out
}

def extractSecurityCode(String raw) {
  if(!raw) return null
  String s = raw.toString()
  try { s = s.replace('\u00A0',' ') } catch(e) {}
  try { s = s.replaceAll(/[\u200B-\u200D\uFEFF]/, "") } catch(e) {}
  s = s.replaceAll(/\s+/, " ").trim()

  String low = s.toLowerCase()

  int idx = -1
  String used = null
  for(String marker : ["security code","securitycode","code","pin"]) {
    int i = low.indexOf(marker)
    if(i >= 0 && (idx < 0 || i < idx)) { idx = i; used = marker }
  }
  if(idx < 0) return null

  int start = idx + ((used ?: "").length())
  if(start < 0 || start >= s.length()) return null

  String tail = s.substring(start)
  String digits = ""
  boolean started = false
  for(int i=0; i<tail.length(); i++) {
    char c = tail.charAt(i)
    if(Character.isDigit(c)) {
      digits += c
      started = true
      if(digits.length() >= 8) break
      continue
    }
    if(started) {
      if(digits.length() >= 3 && Character.isLetter(c)) break
    }
  }

  if(!(digits && digits.size() >= 3)) {
    String spoken = digitsFromSpeech(tail, 8)
    if(spoken && spoken.size() >= 3) digits = spoken
  }

  if(digits && digits.size() >= 3) return digits
  return null
}

def stripSecurityCodePhrase(String raw) {
  if(!raw) return raw
  String s = raw.toString()
  try { s = s.replace('\u00A0',' ') } catch(e) {}
  try { s = s.replaceAll(/[\u200B-\u200D\uFEFF]/, "") } catch(e) {}
  String low = s.toLowerCase()

  int idx = -1
  String used = null
  for(String marker : ["security code","securitycode","code","pin"]) {
    int i = low.indexOf(marker)
    if(i >= 0 && (idx < 0 || i < idx)) { idx = i; used = marker }
  }
  if(idx < 0) return s.replaceAll(/\s+/, " ").trim()

  int end = idx + (used?.length() ?: 0)
  boolean started = false
  int lastDigitPos = -1
  for(int i=end; i<s.length(); i++) {
    char c = s.charAt(i)
    if(Character.isDigit(c)) { started = true; lastDigitPos = i; continue }
    if(started) {
      if(Character.isLetter(c)) break
    }
  }
  int cutEnd = (lastDigitPos >= 0) ? (lastDigitPos + 1) : end
  String out = s.substring(0, idx) + " " + s.substring(cutEnd)
  return out.replaceAll(/\s+/, " ").trim()
}
/* ---------------- Risky-action security helpers ---------------- */

private String riskyDeny(String specific) {
  if(settings?.useGenericDeny == true) {
    def msg = (settings?.genericDenyMsg ?: "Unable to complete request.").toString()
    return msg?.trim() ? msg.trim() : "Unable to complete request."
  }
  return (specific ?: "Unable to complete request.").toString()
}

private String callerIdSafe() {
  try {
    def ip = params?.ip ?: params?.source ?: params?.src ?: null
    if(!ip) {
      try { ip = request?.remoteAddr } catch(e2) { }
    }
    return (ip ?: "unknown").toString()
  } catch(e) {
    return "unknown"
  }
}

private String bfKeyFor(def dev) {
  String did = dev?.id?.toString() ?: "noDev"
  return "${did}|${callerIdSafe()}"
}

private boolean bruteForceLockedOut(def dev) {
  if(settings?.bfEnabled != true) return false
  try {
    def bf = (state.bruteforce ?: [:])
    def k = bfKeyFor(dev)
    def rec = bf[k]
    if(!(rec instanceof Map)) return false
    Long until = (rec.lockoutUntilMs ?: 0L) as Long
    if(until && now() < until) return true
  } catch(e) {
    // ignore
  }
  return false
}

private void bruteForceRecordFail(def dev) {
  if(settings?.bfEnabled != true) return
  try {
    int maxFails = safeInt(settings?.bfMaxFails, 5)
    int windowMins = safeInt(settings?.bfWindowMins, 10)
    int lockoutMins = safeInt(settings?.bfLockoutMins, 10)
    if(maxFails <= 0 || windowMins <= 0 || lockoutMins <= 0) return

    def bf = (state.bruteforce ?: [:])
    def k = bfKeyFor(dev)
    def rec = (bf[k] instanceof Map) ? bf[k] : [:]
    Long nowMs = now()

    Long firstMs = (rec.firstMs ?: 0L) as Long
    Integer count = safeInt(rec.count, 0)

    if(!firstMs || (nowMs - firstMs) > (windowMins * 60_000L)) {
      firstMs = nowMs
      count = 0
      rec.lockoutUntilMs = 0L
    }

    count = (count ?: 0) + 1
    rec.count = count
    rec.firstMs = firstMs
    rec.lastMs = nowMs

    if(count >= maxFails) {
      rec.lockoutUntilMs = nowMs + (lockoutMins * 60_000L)
      log.debug "Brute-force lockout triggered for ${k} (fails=${count}, lockoutMins=${lockoutMins})"
    }

    bf[k] = rec
    state.bruteforce = bf
  } catch(e) {
    log.debug "bruteForceRecordFail error: ${e}"
  }
}

private void bruteForceReset(def dev) {
  if(settings?.bfEnabled != true) return
  try {
    def bf = (state.bruteforce ?: [:])
    def k = bfKeyFor(dev)
    if(bf?.containsKey(k)) {
      bf.remove(k)
      state.bruteforce = bf
    }
  } catch(e) { }
}

private Integer getRiskyAuditMax() {
  Integer n = safeInt(settings?.riskyAuditMax, 200)
  if(n == null) n = 200
  if(n < 20) n = 20
  if(n > 2000) n = 2000
  return n
}

private void riskyAudit(String kind, String action, String target, boolean ok, String reason, String queryText=null, Boolean codePresent=null) {
  try {
    def log = (state?.riskyAudit instanceof List) ? (state.riskyAudit as List) : []
    log << [
      ts: now(),
      caller: callerIdSafe(),
      kind: (kind ?: "").toString(),
      action: (action ?: "").toString(),
      target: (target ?: "").toString(),
      ok: (ok == true),
      reason: (reason ?: "").toString(),
      query: (queryText ?: "").toString(),
      codePresent: (codePresent == null ? null : (codePresent == true))
    ]
    Integer keep = getRiskyAuditMax()
    if(log.size() > keep) log = log.takeRight(keep)
    state.riskyAudit = log
  } catch(e) {
    log.debug "riskyAudit error: ${e}"
  }
}

private List riskyLockAllowlist() {
  try {
    def src = settings?.riskyLockDevices
    if(!src) return []
    if(src instanceof Collection) return (src as List).findAll { it }
    return [src]
  } catch(e) {
    return []
  }
}

private boolean isRiskyLockAllowed(def dev) {
  if(!dev) return false
  if(settings?.enforceRiskyLockAllowlist != true) return true
  def allowed = riskyLockAllowlist()
  if(!allowed) return false
  String did = dev?.id?.toString()
  return allowed.any { it?.id?.toString() == did }
}

private List filterRiskyLockTargets(List devs) {
  List inList = (devs ?: []).findAll { it }
  if(settings?.enforceRiskyLockAllowlist != true) return inList
  def allowedIds = (riskyLockAllowlist() ?: []).collect { it?.id?.toString() }.findAll { it } as Set
  return inList.findAll { d -> allowedIds.contains(d?.id?.toString()) }
}

private boolean hsmActionAllowed(String mode) {
  if(mode == "hsm_arm_home") return (settings?.allowHsmArmHomeVoice != false)
  if(mode == "hsm_arm_away") return (settings?.allowHsmArmAwayVoice != false)
  if(mode == "hsm_disarm") return (settings?.allowHsmDisarmVoice != false)
  return true
}

private Integer getReplayWindowSecs() {
  Integer n = safeInt(settings?.replayWindowSecs, 90)
  if(n == null) n = 90
  if(n < 10) n = 10
  if(n > 600) n = 600
  return n
}

private Integer getReplayNonceTtlMins() {
  Integer n = safeInt(settings?.replayNonceTtlMins, 10)
  if(n == null) n = 10
  if(n < 1) n = 1
  if(n > 120) n = 120
  return n
}

private void pruneReplayNonces() {
  try {
    Long nowMs = now()
    def m = (state?.replayNonces instanceof Map) ? (state.replayNonces as Map) : [:]
    m = m.findAll { k, v ->
      Long exp = null
      try { exp = (v as Long) } catch(ignored) { exp = null }
      return exp != null && exp > nowMs
    }
    state.replayNonces = m
  } catch(e) {
    state.replayNonces = state.replayNonces ?: [:]
  }
}

private Map replayGuardForRisky(String actionKey) {
  if(settings?.replayProtectionEnabled != true) return [ok:true]

  String nonce = (params?.nonce ?: "").toString().trim()
  String tsRaw = (params?.ts ?: params?.timestamp ?: "").toString().trim()
  if(!nonce || !tsRaw) return [ok:false, error:"replay_missing", msg:riskyDeny("Replay protection requires nonce and timestamp.")]

  Long ts = null
  try { ts = Long.parseLong(tsRaw) } catch(e) { ts = null }
  if(ts == null) return [ok:false, error:"replay_bad_ts", msg:riskyDeny("Invalid replay timestamp.")]
  if(ts < 1000000000000L) ts = ts * 1000L

  Long nowMs = now()
  Long maxSkew = (getReplayWindowSecs() as Long) * 1000L
  if(Math.abs(nowMs - ts) > maxSkew) return [ok:false, error:"replay_stale", msg:riskyDeny("Request timestamp is outside the allowed replay window.")]

  pruneReplayNonces()
  def m = (state?.replayNonces instanceof Map) ? (state.replayNonces as Map) : [:]
  String key = "${callerIdSafe()}|${actionKey ?: 'risky'}|${nonce}"
  Long seenExp = null
  try { seenExp = (m[key] as Long) } catch(ignored) { seenExp = null }
  if(seenExp != null && seenExp > nowMs) {
    return [ok:false, error:"replay_duplicate", msg:riskyDeny("Duplicate risky request blocked.")]
  }

  Long ttl = (getReplayNonceTtlMins() as Long) * 60L * 1000L
  m[key] = nowMs + ttl
  state.replayNonces = m
  return [ok:true]
}

/* ---------------- Device constraint inference ---------------- */

private Map inferDeviceConstraint(String queryNorm, Map nluIntent) {
  String q = (queryNorm ?: "").toString()
  String mode = (nluIntent?.mode ?: "").toString()

  // If NLU already tells us the kind of device, honor it first
  if(mode) {
    if(mode.contains("lock") || mode == "confirm_action") return [attrs:["lock"]]
    if(mode.contains("contact") || mode.contains("door") || mode.contains("window")) return [attrs:["contact"]]
    if(mode.contains("motion")) return [attrs:["motion"]]
    if(mode.contains("presence")) return [attrs:["presence"]]
    if(mode.contains("water") || mode.contains("wet") || mode.contains("dry")) return [attrs:["water"]]
    if(mode.contains("switch") || mode.contains("light")) return [attrs:["switch"]]
  }

  // Heuristics from the text
  if(q =~ /(?i)\b(unlock|lock|locked|unlocked)\b/) return [attrs:["lock"]]
  if(q =~ /(?i)\b(open|opened|close|closed|shut)\b/) return [attrs:["contact"]]
  if(q =~ /(?i)\b(motion|active|inactive)\b/) return [attrs:["motion"]]
  if(q =~ /(?i)\b(present|presence|away|home)\b/) return [attrs:["presence"]]
  if(q =~ /(?i)\b(wet|dry|water)\b/) return [attrs:["water"]]
  if(q =~ /(?i)\b(on|off|turn on|turn off|switch)\b/) return [attrs:["switch"]]

  return [:]
}

private boolean deviceMatchesConstraint(dev, Map constraint) {
  if(!constraint) return true
  def attrs = (constraint?.attrs instanceof List) ? (List)constraint.attrs : []
  if(!attrs || attrs.size()==0) return true
  return attrs.every { a -> deviceSupportsAttr(dev, a?.toString()) }
}

/* ---------------- Device alias helpers ---------------- */

private Map aliasToDeviceIdIndex() {
  Map idx = [:]  // normalizedAlias -> deviceId
  String raw = (settings?.deviceAliases ?: "").toString()
  if(!raw?.trim()) return idx

  raw.split(/\r?\n/).each { line ->
    def ln = (line ?: "").toString().trim()
    if(!ln) return
    if(ln.startsWith("#")) return
    def parts = ln.split("=", 2)
    if(parts.size() < 2) return
    String devName = parts[0].trim()
    String aliases = parts[1].trim()
    if(!devName || !aliases) return
    def dev = (qaDevices ?: []).find { (it?.displayName ?: it?.name)?.toString() == devName }
    if(!dev) return
    String did = dev.id?.toString()
    aliases.split(",").each { a ->
      String al = (a ?: "").toString().trim()
      if(!al) return
      String n = normalize(al)
      if(n) idx[n] = did
    }
  }
  return idx
}

private List aliasesForDeviceId(String devId) {
  List out = []
  try {
    def idx = aliasToDeviceIdIndex()
    idx.each { k,v -> if(v?.toString() == devId?.toString()) out << k }
  } catch(e) { }
  return out
}

private Map buildSatelliteRoomMap() {
  Map out = [:]
  String raw = (settings?.satelliteRooms ?: "").toString()
  if(!raw?.trim()) return out

  raw.split(/\r?\n/).each { line ->
    String ln = (line ?: "").toString().trim()
    if(!ln || ln.startsWith("#")) return
    def parts = ln.split("=", 2)
    if(parts.size() < 2) return

    String sat = normalize(parts[0])
    String room = normalize(parts[1])
    if(sat && room) out[sat] = room
  }

  return out
}

private String roomForSatellite(String satName) {
  if(!satName) return null
  Map m = (state?.satelliteRoomMap instanceof Map) ? (state.satelliteRoomMap as Map) : buildSatelliteRoomMap()
  String key = normalize(satName)
  String room = (m[key] ?: "").toString().trim()
  return room ?: null
}

private boolean queryMentionsAnyKnownRoom(String queryNorm) {
  if(!queryNorm) return false
  Map m = (state?.satelliteRoomMap instanceof Map) ? (state.satelliteRoomMap as Map) : buildSatelliteRoomMap()
  def rooms = (m?.values() ?: []).collect { normalize(it?.toString()) }.findAll { it }
  return rooms.any { room -> queryNorm.contains(room) }
}

private boolean deviceMatchesRoom(def dev, String roomNorm) {
  if(!dev || !roomNorm) return false
  String dn = normalize(dev?.displayName ?: dev?.name ?: "")
  if(!dn) return false
  List toks = roomNorm.split(/\s+/).findAll { it }
  if(!toks) return false
  return toks.every { tok -> dn.contains(tok.toString()) }
}



def parseLockCodesFromDevice(dev) {
  try {
    if(!dev) return null
    // Common attribute is "lockCodes" (JSON string)
    def raw = null
    try { raw = dev.currentValue("lockCodes") } catch(e) { raw = null }
    if(raw == null) return null
    def txt = raw.toString()
    if(!txt || txt == "null") return null
    def map = new groovy.json.JsonSlurper().parseText(txt)
    return (map instanceof Map) ? map : null
  } catch(e) {
    return null
  }
}

def lcmHasAnyCodes(dev) {
  def m = parseLockCodesFromDevice(dev)
  try {
    return (m instanceof Map) && (m.size() > 0)
  } catch(e) { return false }
}

def bulkLockUnlock(String action, boolean doorsOnly) {
  // Prefer explicit user-selected locks if configured (safer + more reliable than scanning location.devices)
  def baseLocks = []
  try {
    if(settings?.bulkLocks) {
      baseLocks = (settings.bulkLocks instanceof Collection) ? (settings.bulkLocks as List) : [settings.bulkLocks]
    }
  } catch(e) {}

  if(!baseLocks) {
    // Fallback: scan all devices and keep anything that looks like a lock
    baseLocks = (location?.devices ?: []).findAll { d ->
      try {
        if(!d) return false
        // Prefer capability check if available
        if(d.metaClass?.respondsTo(d, "hasCapability", String)) {
          if(d.hasCapability("Lock")) return true
        }
        // Fallback: method presence
        if(d.metaClass?.respondsTo(d, "lock") || d.metaClass?.respondsTo(d, "unlock")) return true
        if(d?.hasCommand("lock") || d?.hasCommand("unlock")) return true
      } catch(e2) {}
      return false
    }
  }

  // Name-based "doors" filter (include garage + entry, etc.)
  def locks = baseLocks.findAll { d ->
    try {
      if(!d) return false
      if(doorsOnly) {
        def n = (d.displayName ?: d.name ?: "").toString().toLowerCase()
        if(!(n.contains("door") || n.contains("entry") || n.contains("front") || n.contains("back") || n.contains("garage"))) return false
      }
      // Make sure the command exists
      if(action == "lock") return (d?.hasCommand("lock") || d.metaClass?.respondsTo(d, "lock"))
      return (d?.hasCommand("unlock") || d.metaClass?.respondsTo(d, "unlock"))
    } catch(e3) { return false }
  } ?: []

  if(!locks) return doorsOnly ? "No door locks found." : "No locks found."

  log.debug "BULK ${action.toUpperCase()} targets (${locks.size()}): " + locks.collect{ it?.displayName ?: it?.name ?: "?" }.join(", ")

  int ok = 0
  def failed = []
  locks.each { d ->
    try {
      if(action == "lock") d.lock()
      else d.unlock()
      ok++
    } catch(e) {
      failed << "${d?.displayName ?: d?.name ?: 'Unknown'} (${e?.class?.simpleName})"
      log.debug "BULK ${action} failed for ${d?.displayName ?: d?.name}: ${e}"
    }
  }

  String phrase = (action=="lock" ? "locking" : "unlocking")
  if(!failed) return "Sure. ${phrase.capitalize()} ${ok} lock${ok==1?'':'s'}."
  return "Sure. ${phrase.capitalize()} ${ok} lock${ok==1?'':'s'}. I couldn't update: ${failed.join(', ')}."
}

def authorizeRiskyAction(def lockDevForLcm, String providedCode) {
  // Returns [ok:true] or [ok:false, msg:"..."]
  try {
    boolean useGeneric = ((settings?.useGenericDeny == true) || (settings?.useGenericDenialForRisky == true))
    String genericMsg = (settings?.genericDenyMsg ?: settings?.genericDenialMsg ?: "Unable to complete request.").toString()

    if(!providedCode) {
      if(useGeneric) return [ok:false, msg:genericMsg]
      return [ok:false, msg:"Please include the security code."]
    }

    // Prefer your existing validateLockSecurityCode() if present (LCM-first / local fallback)
    if(this.metaClass.respondsTo(this, "validateLockSecurityCode", Object, String)) {
      def vr = validateLockSecurityCode(lockDevForLcm, providedCode)
      if(vr?.ok == true) return [ok:true]
      def msg = vr?.msg ?: "Security code incorrect."
      if(useGeneric) msg = genericMsg
      return [ok:false, msg:msg]
    }

    // Fallback to local securityCode setting if present
    def local = (settings?.securityCode ?: "").toString().trim()
    if(local && local == providedCode) return [ok:true]

    def msg = "Security code incorrect."
    if(useGeneric) msg = genericMsg
    return [ok:false, msg:msg]
  } catch(e) {
    log.debug "authorizeRiskyAction error: ${e}"
    def msg = (settings?.genericDenyMsg ?: settings?.genericDenialMsg ?: "Unable to complete request.").toString()
    return [ok:false, msg:msg]
  }
}

def lcmCodeMatches(dev, String provided) {
  if(!provided) return false
  def m = parseLockCodesFromDevice(dev)
  if(!(m instanceof Map) || m.isEmpty()) return false
  try {
    for(def k : m.keySet()) {
      def v = m[k]
      def code = null
      if(v instanceof Map) code = v.code
      if(code != null && code.toString() == provided.toString()) return true
    }
  } catch(e) { }
  return false
}

// LCM-first: If lockCodes are available on the lock, ONLY accept codes found there.
// If lockCodes are not available, fall back to this app's securityCode setting.
def hasValidSecurityCode(dev, String raw) {
  def provided = extractSecurityCode(raw)
  if(!provided) return false

  if(lcmHasAnyCodes(dev)) {
    return lcmCodeMatches(dev, provided)
  }

  def expected = (settings?.securityCode ?: "").toString().trim()
  if(!expected) return false
  return provided == expected
}


def clarifyIntent(String rawQuery, List candNames = null) {
  return "Sorry, I didn't catch that request."
}

def sendMessage(ans) {
    try {
        String raw = (ans ?: "").toString().trim().toLowerCase()
        if(raw in ["no answer", "no answer.", "no query provided.", ""]) {
            log.debug "In sendMessage - suppressing placeholder response: ${raw}"
            return
        }

        String msg = assistantStyleText((ans ?: "").toString())
        String low = msg.toLowerCase()
        if(low.contains("understand that request")) {
            log.debug "In sendMessage - Something went wrong - didn't understand the question"
            ans = "Sorry, I didn't understand"
        } else {
            ans = msg
        }
    } catch(e) { }
    log.debug "In sendMessage - sdev: ${state.sdev}"
    String _sdev = (state?.sdev ?: "").toString().trim()
    if(_sdev && _sdev != "mini") {
        log.debug "In sendMessage - sending to ${_sdev} - ${ans}"
        sendAnswerToPc(ans)
    } else {
        if(speaker) {
            String speakText = applySpeechFriendlyFormatting(ans)
            log.debug "In sendMessage - sending to Speaker - ${speakText}"
            try {
              speaker*.speak(speakText)
            } catch(e) {
              // Fallback when speaker rejects SSML markup.
              try { speaker*.speak(stripSsmlTags(speakText)) } catch(ignore) {}
            }
        }
        if(notificationDevice) {
            log.debug "In sendMessage - state.sdev: ${state.sdev} - ${ans}"
            if(state.sdev=="mini") {
                // do nothing
            } else {
                try { notificationDevice*.deviceNotification(ans) } catch(e) { }
            }
        }
    }
}

private boolean isAuthorizedRequest() {
  def token = (params.access_token ?: "").toString()
  if(!token) return false
  if(!state?.accessToken) return false
  return token == state.accessToken.toString()
}

def deferredSpeak(evt) {
  try {
    def txt = evt?.text ?: evt?.data?.text
    if(txt) maybeSpeak(txt.toString())
  } catch(e) {
    log.debug "deferredSpeak failed: ${e}"
  }
}

/* ---------------- Intent + Answering ---------------- */

private Map answerFor(dev, String query, Map intent) {
  def devId = dev.id?.toString()
  def devName = dev.displayName

  def attr = intent.attr
  def mode = intent.mode   // "current" or "last"
  // Alias normalization
  if(mode == "duration_query") mode = "duration"

  // Explainability (why_did_action)
  if(mode == "why_did_action") {
    String deviceId = dev?.id?.toString()
    def e = findLastAction(deviceId, null, null)
    if(!e) {
      def ans = "I can only explain actions from this voice endpoint, and I don't have a recent command logged for ${devName}."
      return [ok:false, error:"no_action_log", device:devName, answer: ans]
    }
    Long ts = (e.ts instanceof Number) ? ((Number)e.ts).longValue() : null
    String whenStr = ts ? new Date(ts).format("MMM d, h:mm a", location?.timeZone) : ""
    String cmdStr = (e.command ?: "").toString()
    String valStr = (e.value != null) ? " ${e.value}" : ""
    String qStr = (e.query ?: "").toString()
    def ans = "Because this voice endpoint ran ${cmdStr}${valStr} on ${devName}" + (whenStr ? " at ${whenStr}" : "") + (qStr ? " (request: ${qStr})." : ".")
    return [ok:true, device:devName, answer: ans]
  }

  def wantValue = intent.wantValue // e.g. "open", "on"
  def prettyAttr = attr ?: "state"


  // Device status summary (single device)
  if(mode == "device_status") {
    def parts = []
    def add = { String a, String label = null ->
      if(deviceSupportsAttr(dev, a)) {
        def v = safeCurrent(dev, a)
        if(v != null) parts << "${label ?: a}: ${v}"
      }
    }
    add("contact","contact")
    add("lock","lock")
    add("switch","switch")
    add("level","level")
    add("motion","motion")
    add("water","water")
    add("presence","presence")
    add("temperature","temperature")
    add("humidity","humidity")
    add("battery","battery")

    if(!parts) {
      try {
        def attrs = dev?.supportedAttributes?.collect{ it?.name }?.findAll{ it } ?: []
        if(attrs) {
          def a0 = attrs[0].toString()
          def v0 = safeCurrent(dev, a0)
          if(v0 != null) parts << "${a0}: ${v0}"
        }
      } catch(e) { }
    }

    String ans = parts ? "${devName} — " + parts.join(", ") : "${devName} — No readable attributes."
    return [ok:true, answer: ans, device: devName]
  }


// Device command (optional control)
if(mode == "command") {
  def cmd = (intent?.cmd ?: "").toString()
  try {
    if(attr == "switch") {
      if(cmd == "on") dev.on()
      else if(cmd == "off") dev.off()
    } else if(attr == "lock") {
      if(cmd == "lock") dev.lock()
      else if(cmd == "unlock") dev.unlock()
    } else if(attr == "level") {
      Integer lvl = (intent?.value instanceof Number) ? ((Number)intent.value).intValue() : safeInt(intent?.value, null)
      if(lvl != null) {
        if(lvl < 0) lvl = 0
        if(lvl > 100) lvl = 100
        dev.setLevel(lvl)
      }
    } else if(attr == "thermostat") {
      BigDecimal v = (intent?.value instanceof Number) ? new BigDecimal(intent.value.toString()) : extractNumber(intent?.value?.toString())
      if(v != null) {
        String tcmd = (cmd ?: "").toString()
        if(tcmd == "setHeat") {
          if(hasDeviceCommand(dev, "setHeatingSetpoint")) dev.setHeatingSetpoint(v)
          else if(hasDeviceCommand(dev, "setThermostatSetpoint")) dev.setThermostatSetpoint(v)
        } else if(tcmd == "setCool") {
          if(hasDeviceCommand(dev, "setCoolingSetpoint")) dev.setCoolingSetpoint(v)
          else if(hasDeviceCommand(dev, "setThermostatSetpoint")) dev.setThermostatSetpoint(v)
        } else { // setPoint
          if(hasDeviceCommand(dev, "setThermostatSetpoint")) dev.setThermostatSetpoint(v)
          else {
            String modeNow = (dev.currentValue("thermostatMode") ?: "").toString()
            if(modeNow == "cool" && hasDeviceCommand(dev, "setCoolingSetpoint")) dev.setCoolingSetpoint(v)
            else if(modeNow == "heat" && hasDeviceCommand(dev, "setHeatingSetpoint")) dev.setHeatingSetpoint(v)
            else if(hasDeviceCommand(dev, "setHeatingSetpoint")) dev.setHeatingSetpoint(v)
          }
        }
      }
    } else if(attr == "fan") {
      if(cmd == "setSpeed" && intent?.value) {
        if(hasDeviceCommand(dev, "setSpeed")) dev.setSpeed(intent.value.toString())
      } else if(cmd == "on") {
        if(hasDeviceCommand(dev, "on")) dev.on()
      } else if(cmd == "off") {
        if(hasDeviceCommand(dev, "off")) dev.off()
      }
    } else if(attr == "colorTemperature") {
      Integer k = (intent?.value instanceof Number) ? ((Number)intent.value).intValue() : safeInt(intent?.value, null)
      if(k != null && hasDeviceCommand(dev, "setColorTemperature")) dev.setColorTemperature(k)
    } else if(attr == "scene") {
      if(cmd == "push") {
        if(hasDeviceCommand(dev, "push")) dev.push()
        else if(hasDeviceCommand(dev, "on")) dev.on()
      }
    } else if(attr == "color") {
      def m = intent?.value
      if(m instanceof Map) {
        if(hasDeviceCommand(dev, "setColor")) dev.setColor(m)
        else {
          if(m.hue != null && hasDeviceCommand(dev, "setHue")) dev.setHue(m.hue as Integer)
          if(m.saturation != null && hasDeviceCommand(dev, "setSaturation")) dev.setSaturation(m.saturation as Integer)
          if(m.level != null && hasDeviceCommand(dev, "setLevel")) dev.setLevel(m.level as Integer)
        }
      }
    }
  } catch(e) {
    def ans = "I couldn’t run that command on ${devName}."
    return [ok:false, error:"command_failed", device:devName, attribute:attr, answer:ans]
  }
  recordAction(dev, devName, attr, cmd, intent?.value, query)
  def ans = commandAnswer(devName, attr, cmd, intent?.value)
  return [ok:true, device:devName, attribute:attr, command:cmd, answer:ans]
}

// Numeric comparison (temperature/humidity/battery etc.)
if(mode == "compare") {
  def cur = safeCurrent(dev, attr)
  if(cur == null) {
    def ans = "Sorry, I can't read ${attr} for ${devName}."
    return [ok:false, error:"no_current_value", device:devName, attribute:attr, answer:ans]
  }
  BigDecimal curNum
  try { curNum = new BigDecimal(cur.toString()) } catch(e) { curNum = null }
  if(curNum == null) {
    def ans = "Sorry, I can't interpret ${attr} for ${devName}."
    return [ok:false, error:"bad_numeric_value", device:devName, attribute:attr, value:"${cur}", answer:ans]
  }
  String op = (intent?.op ?: "").toString()
  BigDecimal target = null
  try { target = new BigDecimal(intent?.target?.toString()) } catch(e) { target = null }
  if(target == null) {
    def ans = "I didn't catch the number to compare against."
    return [ok:false, error:"missing_target", device:devName, attribute:attr, value:"${cur}", answer:ans]
  }
  boolean okCmp = evalCompare(curNum, op, target)
  def unit = (intent?.unit ?: "").toString()
  String curStr = fmtNumber(curNum)
  String tgtStr = fmtNumber(target)
  def opWords = compareWords(op)
  def ans = okCmp ?
    "Yes, ${devName} is ${curStr}${unit ? " ${unit}" : ""}, which is ${opWords} ${tgtStr}${unit ? " ${unit}" : ""}." :
    "No, ${devName} is ${curStr}${unit ? " ${unit}" : ""}, which is not ${opWords} ${tgtStr}${unit ? " ${unit}" : ""}."
  return [ok:true, device:devName, attribute:attr, value:curStr, target:tgtStr, op:op, answer:ans]
}

// Current state

  // Current state
  if(mode == "current") {
    def currentVal = safeCurrent(dev, attr)
    if(currentVal == null) {
      def ans = "Sorry, I can't read ${prettyAttr} for ${devName}."
      return [ok:false, error:"no_current_value", device:devName, attribute:attr, answer:ans]
    }
    def ans = currentAnswer(devName, attr, currentVal)
    return [ok:true, device:devName, attribute:attr, value:"${currentVal}", answer:ans]
  }

  // Duration since current state began (uses currentState().date)
  if(mode == "duration") {
    def st = safeCurrentState(dev, attr)
    if(!st || st?.date == null) {
      def ans = "Sorry, I can't determine how long ${devName} has been in that state."
      return [ok:false, error:"no_state_time", device:devName, attribute:attr, answer:ans]
    }
    def curVal = (st?.value != null) ? st.value.toString() : null
    long sinceMs = (st.date.time as Long)
    long nowMs = now()
    long durMs = Math.max(0L, nowMs - sinceMs)
    def durStr = fmtDuration(durMs)

    if(wantValue && curVal && curVal != wantValue) {
      // It isn’t currently in the requested value
      def ans = "${devName} is currently ${curVal}. It has been ${curVal} for ${durStr}."
      return [ok:true, device:devName, attribute:attr, value:curVal, since:sinceMs, durationMs:durMs, answer:ans]
    }

    // Currently in desired state (or no specific value requested)
    def prettyVal = wantValue ?: (curVal ?: "that state")
    def ans2 = "${devName} has been ${prettyVal} for ${durStr}."
    return [ok:true, device:devName, attribute:attr, value:(wantValue ?: curVal), since:sinceMs, durationMs:durMs, answer:ans2]
  }



  // Count events over a window (DB-backed). Example: "how many times was the front door opened today"
  if(mode == "count") {
    Long startMs = null
    if(intent?.startMs instanceof Long) startMs = (Long)intent.startMs
    if(!startMs) {
      String window = (intent.window ?: "today").toString()
      startMs = windowStartMs(window)
      if(!startMs) startMs = windowStartMs("today")
    }

    Integer maxScan = getDbMaxEvents()
    def c = countDeviceEvents(dev, attr, wantValue, new Date(startMs), maxScan)

    String whenStr = (intent?.windowLabel ? intent.windowLabel.toString() :
                     ((intent.window == "yesterday") ? "yesterday" :
                      (intent.window == "thisWeek") ? "this week" :
                      (intent.window == "lastWeek") ? "last week" :
                      (intent.window == "thisMonth") ? "this month" :
                      (intent.window == "lastMonth") ? "last month" : "today"))

    def phrase = countPhrase(attr, wantValue)
    def ans = "${devName} was ${phrase} ${c} time${c==1?'':'s'} ${whenStr}."
    return [ok:true, device:devName, attribute:attr, value:wantValue, count:c, window:window, answer:ans]
  }

  // Last time value occurred (e.g., last opened)
  if(mode == "last") {
    def ts = lastDeviceEventTs(dev, attr, wantValue, getDbMaxEvents())
    if(!ts) {
      // Hybrid fallback: last-change timestamp from Hubitat currentState()
      def st = safeCurrentState(dev, attr)
      def stTs = st?.date ? (st.date.time as Long) : null
      def stVal = (st?.value != null) ? st.value.toString() : null
      if(stTs) {
        def whenStr = fmtWhen(stTs)
        def ans = "The last ${prettyAttr} change for ${devName} was ${whenStr}${stVal ? " (value ${stVal})" : ""}."
        return [ok:true, device:devName, attribute:attr, value:stVal, when:stTs, answer:ans]
      }

      def ans2 = "I don't have a recorded time for ${devName} ${prettyAttr} ${wantValue ?: ''}. Make sure that device is selected in the app and has recent events."
      return [ok:false, error:"no_history", device:devName, attribute:attr, value:wantValue, answer:ans2]
    }
def whenStr2 = fmtWhen(ts)
    def phrase = lastPhrase(attr, wantValue)
    def ans3 = "${devName} was last ${phrase} ${whenStr2}."
    return [ok:true, device:devName, attribute:attr, value:wantValue, when:ts, answer:ans3]
  }

  def ans4 = "Sorry, I wasn't sure how to handle that for ${devName}."
  return [ok:false, error:"unknown_intent", device:devName, answer:ans4]
}

private Map detectIntent(String query, dev) {
  // Figure out which capability/attribute question is about.
  // Priority order: explicit words -> likely attrs.
  def has = { String s -> query.contains(s) }
  // "how long has ... been X" => duration questions
  def isDuration = (has("how long") && (has(" been ") || has(" been") || has("has ") || has("have ")))

  // "when/last" => last timestamp questions
  def isLast = (has("when ") || has("when was") || has("last ") || has("last time") || has("previous")) && !isDuration

  // "how many / times / count" => count questions over a window (DB-backed)
  def rollingStart = rollingWindowStartMs(query)
  def isCount = (has("how many") || has("many times") || has("times") || has("count") || has("number of")) &&
                (rollingStart != null || has("today") || has("yesterday") || has("this week") || has("last week") || has("this month") || has("last month"))





  // Duration query: "how long has X been open/on/active/etc"
  if(isDuration) {
    return [mode:"duration_query"]
  }

  // Last change for this device: "when was", "last time"
  if(isLast) {
    return [mode:"last"]
  }

  // Count queries (events in a window)
  if(isCount) {
    return [mode:"count", startMs: rollingStart]
  }

  // "status" questions for a single device => summarize key attributes
  if(has("status") || (has("what") && has("state"))) {
    if(!isDuration && !isLast && !isCount) {
      return [mode:"device_status"]
    }
  }


  // Explainability: "why did <device> turn on/off/unlock/lock"
  if(has("why did") || has("why is") || has("why was")) {
    def want = null
    if(has("turn on") || has("turned on")) want = "on"
    else if(has("turn off") || has("turned off")) want = "off"
    else if(has("unlock") || has("unlocked")) want = "unlock"
    else if(has("lock") || has("locked")) want = "lock"
    return [mode:"why_did_action", want: want]
  }

// Command intent: set level (dimming) and set thermostat setpoints
def lvl = parseSetLevel(query)
if(lvl != null && deviceSupportsAttr(dev, "level")) {
  return [mode:"command", attr:"level", cmd:"setLevel", value:lvl]
}

def tset = parseThermostatSet(query)
if(tset != null && (deviceSupportsAttr(dev, "thermostat") || deviceSupportsAttr(dev, "heatingSetpoint") || deviceSupportsAttr(dev, "coolingSetpoint"))) {
  // cmd: setHeat / setCool / setPoint
  return [mode:"command", attr:"thermostat", cmd:tset.cmd, value:tset.value, risky:(tset.risky ?: false)]
}



// Command intent: fan speed (fanControl)
def fset = parseFanSet(query)
if(fset != null && (hasDeviceCommand(dev, "setSpeed") || hasDeviceCommand(dev, "on") || hasDeviceCommand(dev, "off"))) {
  if(fset.cmd == "setSpeed") return [mode:"command", attr:"fan", cmd:"setSpeed", value:fset.value]
  if(fset.cmd == "on") return [mode:"command", attr:"fan", cmd:"on"]
  if(fset.cmd == "off") return [mode:"command", attr:"fan", cmd:"off"]
}

// Command intent: color / color temperature
def cset = parseColorSet(query)
if(cset != null) {
  if(cset.cmd == "setColorTemperature" && hasDeviceCommand(dev, "setColorTemperature")) {
    return [mode:"command", attr:"colorTemperature", cmd:"setColorTemperature", value:cset.value]
  }
  if(cset.cmd == "setColor" && (hasDeviceCommand(dev, "setColor") || (hasDeviceCommand(dev, "setHue") && hasDeviceCommand(dev, "setSaturation")))) {
    return [mode:"command", attr:"color", cmd:"setColor", value:cset.value]
  }
}



// Command intent: scenes/routines: "activate", "run", "start"
if(has("activate") || has("run ") || has("start ")) {
  if(hasDeviceCommand(dev, "push")) {
    return [mode:"command", attr:"scene", cmd:"push"]
  }
  if(deviceSupportsAttr(dev, "switch")) {
    return [mode:"command", attr:"switch", cmd:"on"]
  }
}

// Command intent (optional control): "turn on/off", "lock/unlock"
def isCommand = (has("turn on") || has("turn off") || has("switch on") || has("switch off") ||
                 ((has("turn ") || has("switch ")) && (has(" on") || has(" off"))) ||
                 (has("set ") && (has(" on") || has(" off"))) ||
                 has("lock ") || has("unlock ") || has("unlock") || has("lock the") || has("turn the"))
if(isCommand) {
  if((has("turn on") || has("switch on") || (has(" on") && (has("turn ") || has("switch ") || has("set ")))) && deviceSupportsAttr(dev, "switch")) {
    return [mode:"command", attr:"switch", cmd:"on"]
  }
  if((has("turn off") || has("switch off") || (has(" off") && (has("turn ") || has("switch ") || has("set ")))) && deviceSupportsAttr(dev, "switch")) {
    return [mode:"command", attr:"switch", cmd:"off"]
  }
  if((has("unlock") || has("unlocked")) && deviceSupportsAttr(dev, "lock")) {
    return [mode:"command", attr:"lock", cmd:"unlock", risky:true]
  }
  if((has("lock") || has("locked")) && deviceSupportsAttr(dev, "lock")) {
    return [mode:"command", attr:"lock", cmd:"lock"]
  }
}

// Numeric comparison intent: "is basement below 60", "above 75", "under 20 percent"
def cmp = parseComparison(query)
if(cmp) {
  String unit = (cmp.unit ?: "").toString()
  if((has("temperature") || has("temp") || has("degrees")) && deviceSupportsAttr(dev, "temperature")) {
    return [mode:"compare", attr:"temperature", op:cmp.op, target:cmp.target, unit: unit ?: "°"]
  }
  if((has("humidity") || has("humid") || has("percent")) && deviceSupportsAttr(dev, "humidity")) {
    return [mode:"compare", attr:"humidity", op:cmp.op, target:cmp.target, unit: unit ?: "%"]
  }
  if(has("battery") && deviceSupportsAttr(dev, "battery")) {
    return [mode:"compare", attr:"battery", op:cmp.op, target:cmp.target, unit: unit ?: "%"]
  }
}


  // Determine attribute + desired value for "last"
  // Contact
  if(has("open") || has("opened") || has("close") || has("closed") || has("door")) {
    if(deviceSupportsAttr(dev, "contact")) {

      // Count (DB-backed): "how many times ... opened today"
      if(isCount) {
        def want = (has("close") || has("closed")) ? "closed" : "open"
        if(rollingStart != null) {
          return [mode:"count", attr:"contact", wantValue:want, startMs:rollingStart, windowLabel: rollingWindowLabel(query)]
        }
        def win = has("yesterday") ? "yesterday" : (has("today") ? "today" : (has("last week") ? "lastWeek" : (has("this week") ? "thisWeek" : (has("last month") ? "lastMonth" : (has("this month") ? "thisMonth" : "today")))))
        return [mode:"count", attr:"contact", wantValue:want, window:win]
      }

            // Duration: "how long has ... been open"
      if(isDuration) {
        def want = (has("close") || has("closed")) ? "closed" : "open"
        return [mode:"duration", attr:"contact", wantValue:want]
      }

      if(isLast) {
        def want = (has("close") || has("closed")) ? "closed" : "open"
        return [mode:"last", attr:"contact", wantValue:want]
      } else {
        return [mode:"current", attr:"contact"]
      }
    }
  }

  
  // Water / leak
  if(has("wet") || has("dry") || has("leak") || has("water") || has("flood")) {
    if(deviceSupportsAttr(dev, "water")) {

      // Count (DB-backed): "how many times ... wet today"
      if(isCount) {
        def want = (has("dry")) ? "dry" : "wet"
        if(rollingStart != null) {
          return [mode:"count", attr:"water", wantValue:want, startMs:rollingStart, windowLabel: rollingWindowLabel(query)]
        }
        def win = has("yesterday") ? "yesterday" : (has("today") ? "today" : (has("last week") ? "lastWeek" : (has("this week") ? "thisWeek" : (has("last month") ? "lastMonth" : (has("this month") ? "thisMonth" : "today")))))
        return [mode:"count", attr:"water", wantValue:want, window:win]
      }

      // Duration: "how long has ... been wet"
      if(isDuration) {
        def want = (has("dry")) ? "dry" : "wet"
        return [mode:"duration", attr:"water", wantValue:want]
      }

      if(isLast) {
        def want = (has("dry")) ? "dry" : "wet"
        return [mode:"last", attr:"water", wantValue:want]
      } else {
        return [mode:"current", attr:"water"]
      }
    }
  }

// Switch / light
  if(has("light") || has("switch") || has("on") || has("off") || has("turned on") || has("turned off")) {
    if(deviceSupportsAttr(dev, "switch")) {
      if(isLast) {
        def want = (has("off") || has("turned off")) ? "off" : "on"
        return [mode:"last", attr:"switch", wantValue:want]
      } else {
        return [mode:"current", attr:"switch"]
      }
    }
  }

  // Motion
  if(has("motion") || has("active") || has("inactive") || has("moved") || has("no motion")) {
    if(deviceSupportsAttr(dev, "motion")) {

      // Count (DB-backed): "how many times ... active today" / "how many times ... no motion"
      if(isCount) {
        def want = (has("inactive") || has("no motion")) ? "inactive" : "active"
        if(rollingStart != null) {
          return [mode:"count", attr:"motion", wantValue:want, startMs:rollingStart, windowLabel: rollingWindowLabel(query)]
        }
        def win = has("yesterday") ? "yesterday" : (has("today") ? "today" : (has("last week") ? "lastWeek" : (has("this week") ? "thisWeek" : (has("last month") ? "lastMonth" : (has("this month") ? "thisMonth" : "today")))))
        return [mode:"count", attr:"motion", wantValue:want, window:win]
      }

      // Duration: "how long has ... been active/inactive"
      if(isDuration) {
        def want = (has("inactive") || has("no motion")) ? "inactive" : "active"
        return [mode:"duration", attr:"motion", wantValue:want]
      }

      if(isLast) {
        def want = (has("inactive") || has("no motion")) ? "inactive" : "active"
        return [mode:"last", attr:"motion", wantValue:want]
      } else {
        return [mode:"current", attr:"motion"]
      }
    }
  }

  // Lock
  if(has("lock") || has("locked") || has("unlock") || has("unlocked")) {
    if(deviceSupportsAttr(dev, "lock")) {
      if(isLast) {
        def want = (has("unlock") || has("unlocked")) ? "unlocked" : "locked"
        return [mode:"last", attr:"lock", wantValue:want]
      } else {
        return [mode:"current", attr:"lock"]
      }
    }
  }

  // Presence
  if(has("present") || has("not present") || has("away") || has("arrived") || has("left")) {
    if(deviceSupportsAttr(dev, "presence")) {

      // Count (DB-backed): "how many times ... present today" (arrivals) / "not present" (departures)
      if(isCount) {
        def want = (has("not present") || has("away") || has("left")) ? "not present" : "present"
        if(rollingStart != null) {
          return [mode:"count", attr:"presence", wantValue:want, startMs:rollingStart, windowLabel: rollingWindowLabel(query)]
        }
        def win = has("yesterday") ? "yesterday" : (has("today") ? "today" : (has("last week") ? "lastWeek" : (has("this week") ? "thisWeek" : (has("last month") ? "lastMonth" : (has("this month") ? "thisMonth" : "today")))))
        return [mode:"count", attr:"presence", wantValue:want, window:win]
      }

      // Duration: "how long has ... been present/away"
      if(isDuration) {
        def want = (has("not present") || has("away") || has("left")) ? "not present" : "present"
        return [mode:"duration", attr:"presence", wantValue:want]
      }

      if(isLast) {
        def want = (has("not present") || has("away") || has("left")) ? "not present" : "present"
        return [mode:"last", attr:"presence", wantValue:want]
      } else {
        return [mode:"current", attr:"presence"]
      }
    }
  }

  // Temperature
  if(has("temperature") || has("temp") || has("degrees")) {
    if(deviceSupportsAttr(dev, "temperature")) {
      return [mode:"current", attr:"temperature"]
    }
  }

  // Humidity
  if(has("humidity") || has("humid")) {
    if(deviceSupportsAttr(dev, "humidity")) {
      return [mode:"current", attr:"humidity"]
    }
  }

  // Battery
  if(has("battery")) {
    if(deviceSupportsAttr(dev, "battery")) {
      return [mode:"current", attr:"battery"]
    }
  }

  // Fallback: if last-ish phrasing, give last activity for device (most recent event)
  if(isLast) {
    return [mode:"last", attr: mostLikelyAttr(dev)]
  }

  // Default: current "switch" if available, else contact, else most likely
  if(deviceSupportsAttr(dev, "switch")) return [mode:"current", attr:"switch"]
  if(deviceSupportsAttr(dev, "contact")) return [mode:"current", attr:"contact"]
  return [mode:"current", attr: mostLikelyAttr(dev)]
}

/* ---------------- Device matching ---------------- */

private Map buildDeviceIndex() {
  def idx = [:]  // normalizedName -> deviceId
  (qaDevices ?: []).each { d ->
    def n = normalize(d.displayName)
    if(n) idx[n] = d.id?.toString()
  }
  return idx
}


/* ---------------- Group questions (no specific device name) ---------------- */


private Map detectGroupIntent(String query) {
  def has = { String s -> query.contains(s) }

  boolean anyish =
    has("any ") || has("are there") || has("is there") || has("do we have") || has("do any") || has("does any") || has("are any") || has("is any")

  if(has("what time is it") || has("whats the time") || has("what s the time") || has("current time") || has("tell me the time")) {
    return [mode:"time_now"]
  }

  if(((has("weather") || has("forecast")) && (has("tomorrow") || has("tomorrows") || has("tomorrow s"))) ||
     (has("outside") && (has("tomorrow") || has("tomorrows") || has("tomorrow s")))) {
    return [mode:"weather_tomorrow"]
  }

  if(((has("weather") || has("forecast")) && (has("today") || has("todays") || has("today s") || has("current"))) ||
     (has("outside") && (has("today") || has("todays") || has("today s"))) ||
     has("how is it outside") || has("how s it outside") || has("hows it outside") ||
     has("what is it like outside") || has("what s it like outside") || has("whats it like outside") ||
     has("how is the weather outside") || has("how s the weather outside") || has("hows the weather outside")) {
    return [mode:"weather_today"]
  }

  if(has("what s the weather") || has("whats the weather") || has("weather today") || has("today s weather") || has("todays weather") ||
     has("how is the weather") || has("how s the weather") || has("hows the weather") ||
     has("what s the forecast") || has("whats the forecast") ||
     has("how is it outside") || has("how s it outside") || has("hows it outside") ||
     has("what is it like outside") || has("what s it like outside") || has("whats it like outside")) {
    return [mode:"weather_today"]
  }

  // Whole-house summary / exception reporting
  if(has("status of the house") || has("house status") || has("home status") || has("house summary") ||
     (has("what") && (has("status") || has("summary")) && (has("house") || has("home"))) ||
     (has("status") && (has("house") || has("home"))) ) {
    return [mode:"house_summary"]
  }

  // "is everything secure" / "security check" (only report problems; otherwise say all clear)
  if(has("everything secure") || has("all secure") || has("are we secure") ||
     has("security check") || (has("security") && has("check")) || has("run a security check") || has("do a security check") ||
     (has("secure") && (has("everything") || has("house") || has("home") || has("we"))) ||
     has("is everything ok") || has("is everything okay")) {
    return [mode:"secure_check"]
  }


  
  // Device group status (list matching devices)
  // Examples: "which lights are on", "what lights are off", "which doors are open", "which locks are unlocked"
  boolean askingOff = has(" off") || has("are off") || has("turned off") || has("not on")
  boolean askingOn  = has(" on")  || has("are on")  || has("turned on")  || has("is on")
  if((has("which") || has("what")) && (has("light") || has("lights") || has("lamp") || has("lamps"))) {
    String wv = askingOff ? "off" : "on"
    return [mode:"device_group_status", group:"lights", attr:"switch", wantValue:wv]
  }
  if((has("which") || has("what")) && (has("fan") || has("fans"))) {
    String wv = askingOff ? "off" : "on"
    return [mode:"device_group_status", group:"fans", attr:"switch", wantValue:wv]
  }
  if((has("which") || has("what")) && (has("door") || has("doors"))) {
    String wv = (has("closed") || has("shut")) ? "closed" : "open"
    return [mode:"device_group_status", group:"doors", attr:"contact", wantValue:wv]
  }
  if((has("which") || has("what")) && (has("window") || has("windows"))) {
    String wv = (has("closed") || has("shut")) ? "closed" : "open"
    return [mode:"device_group_status", group:"windows", attr:"contact", wantValue:wv]
  }
  if((has("which") || has("what")) && (has("lock") || has("locks"))) {
    String wv = (has("locked") && !has("unlocked")) ? "locked" : "unlocked"
    return [mode:"device_group_status", group:"locks", attr:"lock", wantValue:wv]
  }
  if((has("which") || has("what")) && has("motion")) {
    String wv = (has("inactive") || has("no motion")) ? "inactive" : "active"
    return [mode:"device_group_status", group:"motion", attr:"motion", wantValue:wv]
  }
  if((has("which") || has("what")) && (has("leak") || has("leaks") || has("water") || has("wet"))) {
    return [mode:"device_group_status", group:"water", attr:"water", wantValue:"wet"]
  }

// HSM control (arm/disarm) - risky actions (requires allowHsmControl + security code)
if(has("disarm") || has("disarm hsm") || has("disarm security") || has("turn off hsm")) {
  return [mode:"hsm_disarm", risky:true]
}
if((has("arm") || has("set hsm")) && (has("away") || has("arm away"))) {
  return [mode:"hsm_arm_away", risky:true]
}
if((has("arm") || has("set hsm")) && (has("home") || has("night") || has("stay") || has("arm home") || has("arm night"))) {
  return [mode:"hsm_arm_home", risky:true]
}

// Location mode / HSM questions (no device name)
  if(has("what mode") || has("house mode") || (has("mode") && (has("what") || has("current") || has("is the house") || has("is house")))) {
    return [mode:"hub_mode"]
  }
  if(has("hsm") || has("armed") || has("arm") || has("disarm") || has("intrusion")) {
    return [mode:"hsm_status"]
  }

  // Last activity (no device name)
  if(has("last activity") || has("last event") || has("what was the last") || has("what happened last")) {
    return [mode:"last_activity"]
  }

  // Stale/offline style questions
  if(has("offline") || has("not reporting") || has("stale") || has("haven't reported") || has("hasnt reported")) {
    def start = rollingWindowStartMs(query)  // reuse "last 24 hours" style
    return [mode:"stale", startMs: start]
  }

  // Lights on/off
  if((has("light") || has("lights") || has("lamp") || has("lamps")) && anyish) {
    String wv = (has(" off") || has("are off") || has("turned off")) ? "off" : "on"
    return [mode:"any_state", group:"lights", attr:"switch", wantValue:wv]
  }

  // Fans on/off
  if((has("fan") || has("fans")) && anyish) {
    String wv = (has(" off") || has("are off") || has("turned off")) ? "off" : "on"
    return [mode:"any_state", group:"fans", attr:"switch", wantValue:wv]
  }


  // Battery report (alias to low battery scan)
  if((has("battery report") || has("battery status") || has("batteries report") || has("low battery report") || has("low batteries report")) &&
     (anyish || has("show") || has("list") || has("any") || has("are there") || has("do we have"))) {
    def thr = extractNumber(query)
    if(thr == null) thr = 25
    return [mode:"battery_report", threshold:thr]
  }

  // Batteries low
  if(has("battery") && (anyish || has("low battery") || has("batteries low"))) {
    def thr = extractNumber(query)
    if(thr == null) thr = 25
    return [mode:"battery_low", threshold:thr]
  }

  // Doors unlocked
  if((has("door") || has("doors")) && (has("unlocked") || has("unlock")) && anyish) {
    return [mode:"any_state", group:"doors_unlocked", attr:"lock", wantValue:"unlocked"]
  }

  // "are any doors open/closed"
  if((has("door") || has("doors")) && (anyish || has("any doors open") || has("any door open") || has("any doors closed") || has("any door closed"))) {
    if(has("closed") || has("close") || has("shut")) {
      return [mode:"any_state", group:"doors", attr:"contact", wantValue:"closed"]
    }
    if(has("open") || has("opened")) {
      return [mode:"any_open", group:"doors", attr:"contact", wantValue:"open"]
    }
  }

  // "are any windows open/closed"
  if((has("window") || has("windows")) && (anyish || has("any windows open") || has("any window open") || has("any windows closed") || has("any window closed"))) {
    if(has("closed") || has("close") || has("shut")) {
      return [mode:"any_state", group:"windows", attr:"contact", wantValue:"closed"]
    }
    if(has("open") || has("opened")) {
      return [mode:"any_open", group:"windows", attr:"contact", wantValue:"open"]
    }
  }

  // Wet/dry leaks
  if((has("wet") || has("leak") || has("leaks") || has("water") || has("flood")) && anyish) {
    def want = has("dry") ? "dry" : "wet"
    return [mode:"any_state", group:"water", attr:"water", wantValue:want]
  }

  // Any motion active
  if((has("motion") || has("moving")) && (has("active") || has("motion")) && anyish) {
    return [mode:"any_state", group:"motion", attr:"motion", wantValue:"active"]
  }

  // Anyone present / away
  if((has("anyone") || has("any body") || has("anybody") || has("any")) && (has("present") || has("home") || has("away"))) {
    if(has("away") || has("not present")) {
      return [mode:"any_state", group:"presence", attr:"presence", wantValue:"not present"]
    }
    if(has("present") || has("home")) {
      return [mode:"any_state", group:"presence", attr:"presence", wantValue:"present"]
    }
  }

  return null
}


/* =========================================================
   Intent Parser + Capability Router (structured)
   - Accept optional NLU JSON via params.nlu (URL-encoded JSON string)
   - Normalizes intent + resolves device slots
   - Routes to capability handlers via ROUTE_TABLE (keeps /ask thin)
   ========================================================= */

private Map safeJson(String s) {
  try {
    if(!s) return null
    def sl = new groovy.json.JsonSlurper()
    def obj = sl.parseText(s.toString())
    return (obj instanceof Map) ? (Map)obj : null
  } catch(e) {
    log.debug "safeJson parse failed: ${e}"
    return null
  }
}

/** Expect params.nlu to be URL-encoded JSON.
 *  Example:
 *   &nlu={"intent":"house_summary","confidence":0.92,"slots":{}}
 */
private Map parseNluParam() {
  def raw = (params?.nlu ?: "").toString()
  if(!raw) return null
  // Hubitat already decodes query params, but Tasker sometimes double-encodes.
  String s = raw
  try { s = java.net.URLDecoder.decode(raw, "UTF-8") } catch(ignored) {}
  def nlu = safeJson(s)
  if(!nlu) return null

  String intent = (nlu.intent ?: nlu?.data?.intent ?: "").toString().trim().toLowerCase()
    dbgPut('intent', (intent ?: '').toString())
    dbgRoute('intent_'+(intent ?: ''))
    try { state.lastDebug.intent = (intent ?: '').toString() } catch(e) {}

  BigDecimal conf
  try { conf = (nlu.confidence ?: nlu?.data?.confidence ?: 0) as BigDecimal } catch(e) { conf = 0 }
  Map slots = (nlu.slots instanceof Map) ? (Map)nlu.slots : ((nlu?.data?.slots instanceof Map) ? (Map)nlu.data.slots : [:])

  if(!intent) return null
  return [intent:intent, confidence:conf, slots:slots, raw:nlu]
}

private boolean nluConfOk(Map nlu) {
  if(!nlu) return false
  try { return ((nlu.confidence ?: 0) as BigDecimal) >= 0.70G } catch(e) { return false }
}

private String canonicalIntent(String intentIn) {
  String i = (intentIn ?: "").toString().trim().toLowerCase()
  if(!i) return ""
  // Normalize common synonyms
  Map alias = [
    "home_status":"house_summary",
    "status_house":"house_summary",
    "secure":"secure_check",
    "security_check":"secure_check",
    "battery":"battery_report",
    "low_battery":"battery_report",
    "last_event":"last_activity",
    "duration":"duration_query"
  ]
  return (alias[i] ?: i).toString()
}

private Map slotVal(Map slots, String key) {
  if(!slots || !key) return null
  def v = slots[key]
  if(v instanceof Map) return (Map)v
  if(v != null) return [value:v]
  return null
}

private BigDecimal slotConf(Map slot) {
  if(!slot) return 0
  try { return (slot.confidence ?: 0) as BigDecimal } catch(e) { return 0 }
}

private Map resolveDeviceFromNlu(Map nlu, Map constraint=null) {
  if(!nlu) return null
  Map slots = (Map)(nlu.slots ?: [:])

  // Prefer explicit device id
    // Prefer explicit device id (handle both Map slots and plain strings)
  def devIdSlot = slotVal(slots, "device_id")
  def devSlot   = slotVal(slots, "device")
  def devIdRaw  = devIdSlot?.value
  if(!devIdRaw && (devSlot instanceof Map)) {
    // Some NLU payloads may provide device as a Map with an id, others provide a name string
    devIdRaw = (devSlot?.id ?: (devSlot?.value instanceof Map ? safeId(devSlot.value) : null))
  }
  String devId = (devIdRaw ?: "")?.toString()
  if(devId) {
    def dev = qaDevices?.find { it?.id?.toString() == devId }
    if(dev) {
    if(!deviceMatchesConstraint(dev, constraint)) return null
    return [device:dev, deviceId:devId, deviceName:(dev.displayName ?: dev.name)]
  }
  }

  // Fall back to device name
  Map dn = slotVal(slots, "device_name") ?: slotVal(slots,"device")
  String name = (dn?.value ?: "")?.toString().trim()
  if(name) {
    def dev = matchDeviceByName(name)
    if(dev) {
    if(!deviceMatchesConstraint(dev, constraint)) return null
    return [device:dev, deviceId:dev.id?.toString(), deviceName:(dev.displayName ?: dev.name)]
  }
  }

  return null
}

/** Simple name match used by NLU device slots (doesn't require name to appear in query) */
private def matchDeviceByName(String nameIn) {
  String needle = normalize((nameIn ?: "").toString())
  if(!needle) return null
  def all = (qaDevices ?: [])
  // Exact normalized match first
  def exact = all.find { normalize(it?.displayName ?: it?.name) == needle }
  if(exact) return exact
  // Contains match
  def contains = all.find { normalize(it?.displayName ?: it?.name).contains(needle) || needle.contains(normalize(it?.displayName ?: it?.name)) }
  if(contains) return contains
  return null
}

/** Converts NLU intent to the internal "mode" used by existing handlers. */
private Map nluToInternalIntent(Map nlu) {
  if(!nluConfOk(nlu)) return null
  String ci = canonicalIntent(nlu.intent)
  if(!ci) return null

  // Group intents (no device needed)
  if(ci in ["house_summary","secure_check","hub_mode","hsm_status","battery_report","last_activity","duration_query","device_group_status","any_open","any_state"]) {
    return [mode:ci]
  }

  // Device-level intents
  if(ci in ["device_status","last_activity","duration_query","why_did_action","command"]) {
    return [mode:ci]
  }

  return [mode:ci]
}

/** Router tables (Hubitat-safe) */
def ROUTE_GROUP() {
  return [
    "house_summary" : "answerForGroup",
    "secure_check"  : "answerForGroup",
    "hub_mode"      : "answerForGroup",
    "hsm_status"    : "answerForGroup",
    "hsm_arm_home"  : "answerForGroup",
    "hsm_arm_away"  : "answerForGroup",
    "hsm_disarm"    : "answerForGroup",
    "battery_report": "answerForGroup",
    "device_group_status": "answerForGroup",
    "any_open": "answerForGroup",
    "any_state": "answerForGroup",
    "last_activity": "answerForGroup",
    "time_now": "answerForGroup",
    "weather_today": "answerForGroup",
    "weather_tomorrow": "answerForGroup"
  ]
}


private Map routeGroup(String query, Map intent) {
  if(!intent?.mode) return null
  def handlerName = ROUTE_GROUP()[intent.mode]
  if(handlerName && this.respondsTo(handlerName)) {
    return this."${handlerName}"(query, intent)
  }
  return null
}


private Map answerForGroup(String query, Map intent) {
  def devices = (qaDevices ?: [])

if(intent?.mode == "time_now") {
  def tz = location?.timeZone ?: TimeZone.getTimeZone("America/New_York")
  String nowStr = new Date().format("h:mm a", tz)
  return [ok:true, answer:"It is ${nowStr}."]
}

if(intent?.mode in ["weather_today","weather_tomorrow"]) {
  Integer dayIndex = (intent?.mode == "weather_tomorrow") ? 1 : 0
  def wx = getWeatherAnswer(dayIndex)
  return wx ?: [ok:false, error:"weather_unavailable", answer:"Sorry, I couldn't get the weather right now."]
}

  if(!devices) {
    return [ok:false, error:"no_devices", answer:"No devices are selected in the app yet."]
  }



// HSM control (arm/disarm) - risky actions
if(intent?.mode in ["hsm_arm_home","hsm_arm_away","hsm_disarm"]) {
  if(settings?.allowHsmControl != true) {
    riskyAudit("hsm", (intent?.mode ?: ""), "hsm", false, "hsm_control_disabled", query, null)
    return [ok:false, error:"hsm_control_disabled", answer:"HSM control is disabled."]
  }
  if(!hsmActionAllowed((intent?.mode ?: "").toString())) {
    riskyAudit("hsm", (intent?.mode ?: ""), "hsm", false, "hsm_action_disabled", query, null)
    return [ok:false, error:"hsm_action_disabled", answer:"That HSM voice action is disabled."]
  }
  try {
    String value = null
    if(intent.mode == "hsm_arm_home") value = (settings?.hsmArmHomeValue ?: "armHome").toString()
    if(intent.mode == "hsm_arm_away") value = (settings?.hsmArmAwayValue ?: "armAway").toString()
    if(intent.mode == "hsm_disarm") value = "disarm"
    if(!value) {
      riskyAudit("hsm", (intent?.mode ?: ""), "hsm", false, "hsm_value_missing", query, null)
      return [ok:false, error:"hsm_value_missing", answer:"Sorry, I couldn't set Hubitat Safety Monitor."]
    }
    sendLocationEvent(name: "hsmSetArm", value: value)
    riskyAudit("hsm", (intent?.mode ?: ""), "hsm", true, "executed", query, null)
    return [ok:true, answer:"Sure. Security mode set to ${value}."]
  } catch(e) {
    log.debug "HSM control error: ${e}"
    riskyAudit("hsm", (intent?.mode ?: ""), "hsm", false, "hsm_error", query, null)
    return [ok:false, error:"hsm_error", answer:"Sorry, I couldn't set Hubitat Safety Monitor."]
  }
}


  // Device group status (list matching devices) / any_open / any_state
  if(intent?.mode in ["device_group_status","any_open","any_state"]) {
    String attr  = (intent?.attr       ?: "").toString()
    String want  = (intent?.wantValue  ?: "").toString()
    String group = (intent?.group      ?: "").toString()
    String satRoom = (intent?._satRoom ?: "").toString().trim().toLowerCase()

    // Room alias map: spoken shorthand → canonical token that appears in device names
    Map<String,String> roomAliases = [
      "bath":"bathroom", "baths":"bathroom",
      "bdrm":"bedroom",  "bed":"bedroom",
      "liv":"living",    "lvng":"living",
      "kit":"kitchen",
      "fam":"family",
      "din":"dining",
      "gar":"garage",
      "mstr":"master",   "mast":"master",
      "util":"utility",
      "laun":"laundry",
      "off":"office",    // "office" itself is fine; "off" is now a stopword so won't reach here
    ]

    // Try to extract a scope/room from the query (e.g., 'kitchen', 'living room')
    List<String> queryTokens = hvTokens(query)
    // groupWords: device-type words that should never be treated as room/scope tokens
    // stateWords: state/value words that survive hvStopwords() but must not pollute scope
    List<String> groupWords  = ["light","lights","lamp","lamps","ceiling","sconce","fan","fans",
                                "door","doors","window","windows","lock","locks",
                                "motion","motions","water"]
    List<String> stateWords  = ["off","not","yes","open","closed","locked","unlocked",
                                "active","inactive","wet","dry","hot","cold","dim","bright"]
    List<String> scopeTokens = queryTokens
      .findAll { !(it in groupWords) && !(it in stateWords) && it.size() >= 3 }
      .collect { roomAliases.containsKey(it) ? roomAliases[it] : it }  // expand aliases
    // Fall back to _satRoom from the intent when the query tokeniser found nothing useful
    if((!scopeTokens || scopeTokens.isEmpty()) && satRoom) {
      scopeTokens = satRoom.tokenize(" ")
        .findAll { it.size() >= 3 }
        .collect { roomAliases.containsKey(it) ? roomAliases[it] : it }
    }

    // Build a human-readable room prefix ("bathroom ", "kitchen ", or "")
    String roomPrefix = scopeTokens ? scopeTokens.join(" ") + " " : ""

    def matches    = []   // devices currently in the wanted state
    def notMatches = []   // devices in scope but NOT in the wanted state

    devices.each { d ->
      try {
        String dn  = (d?.displayName ?: d?.name ?: "").toString()
        // Use hvNorm so special chars (slashes, hyphens) are stripped consistently
        // with the tokens derived from the query via hvNorm/hvTokens
        String dnl = hvNorm(dn)

        // Group filtering: does this device belong to the requested group?
        boolean groupOk = true
        if(group == "doors")   groupOk = (dnl.contains("door") || dnl.contains("garage"))
        else if(group == "windows") groupOk = dnl.contains("window")
        else if(group == "lights")  groupOk = (dnl.contains("light") || dnl.contains("lamp") ||
                                               dnl.contains("ceiling") || dnl.contains("sconce") ||
                                               deviceSupportsAttr(d,"level"))
        else if(group == "fans")   groupOk = (dnl.contains("fan") || dnl.contains("vent") ||
                                              dnl.contains("exhaust") || dnl.contains("blower"))
        else if(group == "locks")  groupOk = deviceSupportsAttr(d,"lock")
        else if(group == "motion") groupOk = deviceSupportsAttr(d,"motion")
        else if(group == "water")  groupOk = deviceSupportsAttr(d,"water")

        // Scope filtering: ALL scope tokens must appear somewhere in the normalised device name
        if(groupOk && scopeTokens && scopeTokens.size() > 0) {
          for(String tok in scopeTokens) {
            if(!dnl.contains(tok)) { groupOk = false; break }
          }
        }

        if(!groupOk) return

        // Track which state each in-scope device is in
        if(attr && deviceSupportsAttr(d, attr)) {
          def v = safeCurrent(d, attr)?.toString()
          if(v == want) matches << dn
          else          notMatches << dn
        }
      } catch(e) { }
    }

    String label = group ?: "devices"

    // Maps a state value to its natural opposite for readable responses
    Map<String,String> oppositeOf = [
      "on":"off",           "off":"on",
      "open":"closed",      "closed":"open",
      "locked":"unlocked",  "unlocked":"locked",
      "active":"inactive",  "inactive":"active",
      "wet":"dry",          "dry":"wet",
      "present":"away",     "away":"present",
    ]
    String notState = oppositeOf.get(want, "not ${want}")

    // Closure: returns "is" for a single item, "are" for multiple
    def isAre = { List l -> l.size() == 1 ? "is" : "are" }

    // No in-scope devices found at all
    if(matches.isEmpty() && notMatches.isEmpty()) {
      String msg = roomPrefix ? "I don't see any ${roomPrefix}${label}." : "No ${label} found."
      return [ok:true, mode:intent.mode, group:group, attr:attr, wantValue:want, answer: msg]
    }

    // Special-case: lock "all locked" phrasing
    if(group == "locks" && want == "unlocked" && matches.isEmpty()) {
      return [ok:true, mode:intent.mode, group:group, attr:attr, wantValue:want,
              answer: "All ${roomPrefix}locks are locked."]
    }

    // None are in the wanted state — tell the user what state they ARE in
    if(matches.isEmpty()) {
      String ans
      if(notMatches.size() == 1) {
        ans = "No, the ${notMatches[0]} is ${notState}."
      } else {
        ans = "No, all the ${roomPrefix}${label} are ${notState}."
      }
      return [ok:true, mode:intent.mode, group:group, attr:attr, wantValue:want, answer: ans]
    }

    // At least some devices ARE in the wanted state
    String ans
    if(matches.size() == 1 && notMatches.isEmpty()) {
      // Exactly one device, and it's the only one in scope
      ans = "Yes, the ${matches[0]} is ${want}."
    } else if(notMatches.isEmpty()) {
      // All in-scope devices are in the wanted state
      ans = "Yes, all the ${roomPrefix}${label} are ${want}."
    } else if(matches.size() == 1) {
      // One matches, the rest don't — name both sides explicitly
      ans = "Yes, the ${matches[0]} is ${want}, but the ${notMatches.join(", ")} ${isAre(notMatches)} ${notState}."
    } else {
      // Multiple match; some don't — list both sides
      ans = "Yes, the following ${roomPrefix}${label} are ${want}: " + matches.join(", ") + "."
      ans += " The ${notMatches.join(", ")} ${isAre(notMatches)} ${notState}."
    }
    return [ok:true, mode:intent.mode, group:group, attr:attr, wantValue:want, answer: ans]
  }

  // Whole-house summary
  if(intent?.mode == "house_summary" || intent?.mode == "secure_check") {
    def openContacts = []
    def openDoors = []
    def openWindows = []
    def unlocked = []
    def lightsOn = []
    def motionActive = []
    def wetSensors = []
    def thermostats = []

    devices.each { d ->
      try {
        String dn = (d?.displayName ?: d?.name ?: "").toString()
        String dnl = dn.toLowerCase()

        // Contact open (doors/windows)
        if(deviceSupportsAttr(d, "contact")) {
          def v = safeCurrent(d, "contact")?.toString()
          if(v == "open") {
            openContacts << dn
            if(dnl.contains("window")) openWindows << dn
            else if(dnl.contains("door") || dnl.contains("garage")) openDoors << dn
          }
        }

        // Locks unlocked
        if(deviceSupportsAttr(d, "lock")) {
          def v = safeCurrent(d, "lock")?.toString()
          if(v == "unlocked") unlocked << dn
        }

        // Switches on (lights)
        if(deviceSupportsAttr(d, "switch")) {
          def v = safeCurrent(d, "switch")?.toString()
          if(v == "on") {
            // Prefer devices that look like lights, but include any switch if nothing else matches
            if(dnl.contains("light") || dnl.contains("lamp") || dnl.contains("ceiling") || dnl.contains("sconce")) lightsOn << dn
            else lightsOn << dn
          }
        }

        // Motion active
        if(deviceSupportsAttr(d, "motion")) {
          def v = safeCurrent(d, "motion")?.toString()
          if(v == "active") motionActive << dn
        }

        // Water wet
        if(deviceSupportsAttr(d, "water")) {
          def v = safeCurrent(d, "water")?.toString()
          if(v == "wet") wetSensors << dn
        }

        // Thermostat summary
        if(deviceSupportsAttr(d, "thermostatMode") || deviceSupportsAttr(d, "temperature")) {
          // Only treat it as a thermostat if it has thermostatMode OR thermostatOperatingState OR setpoints
          if(deviceSupportsAttr(d, "thermostatMode") || deviceSupportsAttr(d, "heatingSetpoint") || deviceSupportsAttr(d, "coolingSetpoint") || deviceSupportsAttr(d, "thermostatOperatingState")) {
            def temp = safeCurrent(d, "temperature")
            def mode = safeCurrent(d, "thermostatMode")
            def op   = safeCurrent(d, "thermostatOperatingState")
            def hs   = safeCurrent(d, "heatingSetpoint")
            def cs   = safeCurrent(d, "coolingSetpoint")
            def sp   = safeCurrent(d, "thermostatSetpoint")
            thermostats << [name: dn, temp: temp, mode: mode, op: op, hs: hs, cs: cs, sp: sp]
          }
        }
      } catch(ignored) { }
    }

    // Secure-check: exception-only reporting
    if(intent?.mode == "secure_check") {
      def probs = []
      if(openContacts) probs << "Open: " + openContacts.join(", ")
      if(unlocked)     probs << "Unlocked: " + unlocked.join(", ")
      if(wetSensors)   probs << "Water leak: " + wetSensors.join(", ")

      if(!probs) {
        String ans = "Everything looks secure. Doors and windows are closed, locks are secure, and no leaks are detected."
        return [ok:true, answer: ans]
      } else {
        String ans = "I found a few things: " + probs.join(". ") + "."
        return [ok:true, answer: ans]
      }
    }

    // House summary: include only non-empty sections
    def parts = []
    if(openDoors)    parts << "Doors open: " + openDoors.join(", ")
    if(openWindows)  parts << "Windows open: " + openWindows.join(", ")
    if(!openDoors && !openWindows && openContacts) parts << "Open: " + openContacts.join(", ")

    if(unlocked)     parts << "Doors unlocked: " + unlocked.join(", ")
    if(lightsOn)     parts << "Lights on: " + lightsOn.join(", ")
    if(motionActive) parts << "Motion: " + motionActive.join(", ")
    if(wetSensors)   parts << "Leaks: " + wetSensors.join(", ")

    // Thermostat line(s)
    if(thermostats) {
      def tLines = thermostats.collect { t ->
        def bits = []
        if(t.temp != null) bits << "${t.temp}"
        if(t.mode) bits << "${t.mode}"
        if(t.op) bits << "${t.op}"
        // Prefer thermostatSetpoint; otherwise show heat/cool setpoints
        if(t.sp != null) bits << "set ${t.sp}"
        else {
          if(t.hs != null) bits << "heat ${t.hs}"
          if(t.cs != null) bits << "cool ${t.cs}"
        }
        return (bits ? "${t.name}: " + bits.join(" ") : "${t.name}")
      }
      parts << "Thermostat: " + tLines.join("; ")
    }

    String ans
    if(!parts) ans = "Everything looks good right now."
    else ans = parts.join(". ") + "."

    return [ok:true, answer: ans]
  }


// Hub mode
if(intent?.mode == "hub_mode") {
  def mname = null
  try { mname = location?.mode } catch(e) { mname = null }
  if(!mname) return [ok:false, error:"no_mode", answer:"Sorry, I can't read the current hub mode."]
  return [ok:true, mode:mname.toString(), answer:"Your home is in ${mname} mode."]
}

// HSM status
if(intent?.mode == "hsm_status") {
  def st = null
  try { st = location?.hsmStatus } catch(e) { st = null }
  if(!st) return [ok:false, error:"no_hsm", answer:"Sorry, I can't read Hubitat Safety Monitor status right now."]
  return [ok:true, hsmStatus:st.toString(), answer:"Hubitat Safety Monitor is ${st}."]
}

// Last activity across selected devices
if(intent?.mode == "last_activity") {
  def best = findLastActivity()
  if(!best) return [ok:false, error:"no_activity", answer:"I couldn’t find any recent activity."]
  def whenStr = fmtWhen(best.ts as Long)
  return [ok:true, device:best.device, attribute:best.attr, value:best.val, when:best.ts, answer:"The latest activity was ${best.device}, ${best.attr} ${best.val}, ${whenStr}."]
}

// Stale/offline check
if(intent?.mode == "stale") {
  Long startMs = (intent?.startMs instanceof Long) ? (Long)intent.startMs : null
  if(!startMs) startMs = now() - (24L*60L*60L*1000L) // default 24h
  def stale = findStaleDevices(startMs)
  if(!stale) return [ok:true, count:0, answer:"All selected devices have reported recently."]
  def names = stale.collect{ it.name }.join(", ")
  def windowLabel = "in the last " + fmtDuration(Math.max(0L, now()-startMs))
  return [ok:true, count:stale.size(), matches:names, answer:"These devices haven't reported ${windowLabel}: ${names}."]
}

// Battery low
if(intent?.mode in ["battery_low","battery_report"]) {
  BigDecimal thr = null
  try { thr = new BigDecimal(intent?.threshold?.toString()) } catch(e) { thr = new BigDecimal("25") }
  def low = []
  (qaDevices ?: []).findAll{ deviceSupportsAttr(it,"battery") }.each { d ->
    def v = safeCurrent(d,"battery")
    if(v==null) return
    try {
      def n = new BigDecimal(v.toString())
      if(n < thr) low << [name:(d.displayName ?: "Device"), val:n]
    } catch(ignored) {}
  }
  if(!low) return [ok:true, count:0, answer:"Good news, no batteries are below ${fmtNumber(thr)}%." ]
  def list = low.sort{ it.val }.collect{ "${it.name} (${fmtNumber(it.val)}%)" }.join(", ")
  return [ok:true, count:low.size(), matches:low.collect{it.name}, answer:"These batteries are below ${fmtNumber(thr)}%: ${list}."]
}


  String attr = (intent?.attr ?: "").toString()
  if(!attr) attr = "contact"

  def candidates = devices.findAll { d -> deviceSupportsAttr(d, attr) }
  if(!candidates) {
    return [ok:false, error:"no_candidates", answer:"I couldn't find selected devices that support ${attr}."]
  }

  String grp = (intent?.group ?: "").toString()
  if(grp == "doors" || grp == "windows") {
    String needle = (grp == "doors") ? "door" : "window"
    def byName = candidates.findAll { d -> (d?.displayName ?: "").toString().toLowerCase().contains(needle) }
    if(byName) candidates = byName
  } else if(grp == "lights") {
    def byName = candidates.findAll { d -> (d?.displayName ?: "").toString().toLowerCase().contains("light") }
    if(byName) candidates = byName
  } else if(grp == "doors_unlocked") {
    // Prefer lock devices with "door" in the name
    def byName = candidates.findAll { d -> (d?.displayName ?: "").toString().toLowerCase().contains("door") }
    if(byName) candidates = byName
  } else if(grp == "motion") {
    def byName = candidates.findAll { d -> (d?.displayName ?: "").toString().toLowerCase().contains("motion") }
    if(byName) candidates = byName
  } else if(grp == "presence") {
    // no special filter
  } else if(grp == "water") {
    // Prefer devices with "water" or "leak" in the name (if any)
    def byName = candidates.findAll { d ->
      def n = (d?.displayName ?: "").toString().toLowerCase()
      n.contains("water") || n.contains("leak") || n.contains("flood")
    }
    if(byName) candidates = byName
  } else {
    // Default phrasing if unknown group
    grp = (attr == "water") ? "water" : (attr=="switch" ? "lights" : (attr=="lock" ? "locks" : (attr=="presence" ? "presence" : "doors")) )
  }

  String want = (intent?.wantValue ?: (attr == "water" ? "wet" : "open")).toString()

  def matchNames = []
  candidates.each { d ->
    def v = safeCurrent(d, attr)
    if(v != null && v.toString() == want) {
      matchNames << (d?.displayName ?: d?.name ?: "Device")
    }
  }

  if(matchNames) {
    String list = matchNames.join(", ")
    if(grp == "lights") {
      return [ok:true, count:matchNames.size(), matches:matchNames, answer:"Yes, these lights are on: ${list}."]
    }
    if(grp == "doors_unlocked" || grp == "locks") {
      return [ok:true, count:matchNames.size(), matches:matchNames, answer:"Yes, these locks are unlocked: ${list}."]
    }
    if(grp == "motion") {
      return [ok:true, count:matchNames.size(), matches:matchNames, answer:"Yes, motion is active on: ${list}."]
    }
    if(grp == "presence") {
      def lbl = (want == "present") ? "Present" : "Away"
      return [ok:true, count:matchNames.size(), matches:matchNames, answer:"Yes, ${lbl.toLowerCase()}: ${list}."]
    }
    if(grp == "windows") {
      return [ok:true, count:matchNames.size(), matches:matchNames, answer:(want == "closed" ? "Yes, these windows are closed: ${list}." : "Yes, these windows are open: ${list}.")]
    }
    if(grp == "water") {
      return [ok:true, count:matchNames.size(), matches:matchNames, answer:(want == "dry" ? "Yes, these sensors are dry: ${list}." : "Yes, these sensors are wet: ${list}.")]
    }
    // doors (default)
    return [ok:true, count:matchNames.size(), matches:matchNames, answer:(want == "closed" ? "Yes, these doors are closed: ${list}." : "Yes, these doors are open: ${list}.")]
  }

  if(grp == "windows") return [ok:true, count:0, matches:[], answer:(want == "closed" ? "No, all windows are open." : "No, all windows are closed.")]
  if(grp == "water")   return [ok:true, count:0, matches:[], answer:(want == "dry" ? "No, I don't see dry water sensors." : "No, all water sensors are dry.")]
  return [ok:true, count:0, matches:[], answer:(want == "closed" ? "No, all doors are open." : "No, all doors are closed.")]
}

private def matchDeviceFromQuery(String query, Map constraint=null) {
// Mode separation: control vs status
String __mode = (constraint?.mode ?: "").toString()


  log.debug "--------------------  In matchDeviceFromQuery  --------------------"
  def bestDev = null
  def bestLen = 0
  String queryNorm = normalize(query)
  String satRoom = roomForSatellite(state?.sdev)
  boolean useRoomBias = !!satRoom && !queryMentionsAnyKnownRoom(queryNorm)
  try {
    dbgPut('satellite', (state?.sdev ?: ''))
    dbgPut('satelliteRoom', (satRoom ?: ''))
    dbgPut('roomBias', useRoomBias ? 'on' : 'off')
    state.lastDebug = state.lastDebug ?: [:]
    state.lastDebug.satellite = (state?.sdev ?: '')
    state.lastDebug.satelliteRoom = (satRoom ?: '')
    state.lastDebug.roomBias = useRoomBias
  } catch(e) {}


  def candidates = (qaDevices ?: []).findAll { d -> deviceMatchesConstraint(d, constraint) }
  log.debug "matchDeviceFromQuery: queryNorm='${queryNorm}' satRoom='${satRoom}' useRoomBias=${useRoomBias} candidates=${candidates.size()}"

  // When the satellite has a known room and the query doesn't already name a room,
  // build augmented query variants with the room prepended so that e.g.
  // "ceiling light" from Livingroom1 matches "Livingroom Ceiling Light".
  // We try both the spaced form ("living room ceiling light") and the compact
  // form ("livingroom ceiling light") because device names use either convention.
  List<String> queryVariants = [queryNorm]
  String roomCompact = null
  if(useRoomBias && satRoom) {
    String roomSpaced = normalize(satRoom)          // e.g. "living room"
    roomCompact = roomSpaced.replace(" ", "")       // e.g. "livingroom"
    queryVariants << "${roomSpaced} ${queryNorm}".toString()
    if(roomCompact != roomSpaced) queryVariants << "${roomCompact} ${queryNorm}".toString()
  }

  def aliasIdx = aliasToDeviceIdIndex()

  // 1) Alias contains match (strongest) — room-augmented variants tried first
  queryVariants.each { qv ->
    if(bestDev) return
    if(aliasIdx && aliasIdx.size() > 0) {
      aliasIdx.each { String aliasNorm, String did ->
        if(aliasNorm && qv.contains(aliasNorm) && aliasNorm.length() > bestLen) {
          def d = candidates.find { it?.id?.toString() == did?.toString() }
          if(d) { bestLen = aliasNorm.length(); bestDev = d }
        }
      }
    }
  }

  // 2) Direct contains match on device displayName — room-augmented variants tried first
  if(!bestDev) {
    queryVariants.each { qv ->
      if(bestDev) return
      candidates.each { d ->
        def nameNorm = normalize(d.displayName)
        if(!nameNorm) return
        if(qv.contains(nameNorm) && nameNorm.length() > bestLen) {
          bestLen = nameNorm.length()
          bestDev = d
        }
      }
    }
  }

  // 3) Token overlap fuzzy (device name + aliases)
  if(!bestDev) {
    def qTokens = queryNorm.split(/\s+/).findAll { it }
    def qTokenSet = [] as Set
    qTokens.each { t ->
      if(!t) return
      qTokenSet << t
      // Basic singular/plural normalization: light <-> lights, door <-> doors
      if(t.endsWith("s") && t.length() > 3) qTokenSet << t.substring(0, t.length() - 1)
      else if(t.length() > 2) qTokenSet << (t + "s")
    }
    // Add compact room token so that a device named "Livingroom Ceiling Light"
    // (single token "livingroom") scores a hit when the satellite room is "living room".
    if(roomCompact) qTokenSet << roomCompact

    def bestScore = 0.0
    candidates.each { d ->
      def dn = normalize(d.displayName)
      if(!dn) return
      def dTokens = dn.split(/\s+/).findAll { it && it.length() > 2 }

      // Add alias tokens for this device
      def did = d?.id?.toString()
      aliasesForDeviceId(did).each { an ->
        an.split(/\s+/).each { t -> if(t && t.length() > 2) dTokens << t }
      }

      dTokens = dTokens.unique()
      if(!dTokens) return

      def overlap = dTokens.count { t -> qTokenSet.contains(t) }
      // Weighted score: overlap plus small bonus for longer names and same-room devices.
      def score = (overlap as Double) + (Math.min(dTokens.size(), 10) * 0.05)
      if(useRoomBias && deviceMatchesRoom(d, satRoom)) score += 1.5d
      // Only update bestDev when overlap threshold is met AND score is better.
      int minOverlap = (dTokens.size() <= 2) ? 1 : 2
      if(overlap >= minOverlap && score > bestScore) {
        bestScore = score
        bestDev = d
      }
    }
  }

  log.debug "bestDev: ${bestDev}"
    dbgPut('bestDev', (bestDev?.displayName ?: bestDev?.name ?: ''))
    try { state.lastDebug.bestDev = (bestDev?.displayName ?: bestDev?.name ?: '') } catch(e) {}

  return bestDev
}

/* ---------------- State helpers ---------------- */
private Long lastTimestampFor(String devId, String attr, String wantValue) {
  def byAttrVal = (state.lastByDevAttrValue ?: [:])[devId]
  if(!byAttrVal) return null
  def valMap = byAttrVal[attr]
  if(!valMap) return null
  if(wantValue == null) return null
  return valMap[wantValue]
}

private def safeCurrentState(dev, String attr) {
  try {
    return dev.currentState(attr)
  } catch(e) {
    return null
  }
}

private def safeCurrent(dev, String attr) {
  try {
    return dev.currentValue(attr)
  } catch(e) {
    return null
  }
}

private boolean deviceSupportsAttr(dev, String attr) {
  try {
    if(!dev || !attr) return false
    String a = attr.toString()

    // Primary path: explicit supportedAttributes metadata.
    if(dev.supportedAttributes?.any { it?.name == a }) return true

    // Capability fallbacks for drivers that omit supportedAttributes.
    if(dev.metaClass?.respondsTo(dev, "hasCapability", String)) {
      if(a == "contact" && dev.hasCapability("Contact Sensor")) return true
      if(a == "lock" && dev.hasCapability("Lock")) return true
      if(a == "switch" && dev.hasCapability("Switch")) return true
      if(a == "motion" && dev.hasCapability("Motion Sensor")) return true
      if(a == "water" && dev.hasCapability("Water Sensor")) return true
      if(a == "presence" && dev.hasCapability("Presence Sensor")) return true
      if(a == "temperature" && dev.hasCapability("Temperature Measurement")) return true
      if(a == "humidity" && dev.hasCapability("Relative Humidity Measurement")) return true
      if(a == "battery" && dev.hasCapability("Battery")) return true
    }

    // Last fallback: if currentValue(attr) is readable at all, treat as supported.
    try {
      def v = dev.currentValue(a)
      if(v != null) return true
    } catch(ignored) {}

    return false
  } catch(e) {
    return false
  }
}

private String mostLikelyAttr(dev) {
  def prefs = ["switch","contact","motion","lock","presence","temperature","humidity","battery","water"]
  def sup = []
  try { sup = dev.supportedAttributes?.collect { it?.name }?.findAll { it } ?: [] } catch(e) { sup = [] }
  for(a in prefs) { if(sup.contains(a)) return a }
  return sup ? sup[0] : "switch"
}



/* ---------------- DB-backed Counting ---------------- */


private Long rollingWindowStartMs(String query) {
  try {
    String q = (query ?: "").toString().toLowerCase()
      .replaceAll(/[^a-z0-9\s]/, " ")
      .replaceAll(/\s+/, " ")
      .trim()

    if(!q) return null
    if(!(q.contains("last") || q.contains("past") || q.contains("previous"))) return null

    // Hubitat-safe parser: scan token pairs instead of relying only on regex groups.
    List toks = q.split(/\s+/).findAll { it }
    Long n = null
    String unit = null

    for(int i=0; i<toks.size()-1; i++) {
      String a = (toks[i] ?: "").toString()
      String b = (toks[i+1] ?: "").toString()
      if(a ==~ /\d+/) {
        if(b ==~ /hours?|hrs?|hr/) { n = (a as Long); unit = "h"; break }
        if(b ==~ /minutes?|mins?|min/) { n = (a as Long); unit = "m"; break }
        if(b ==~ /days?/) { n = (a as Long); unit = "d"; break }
        if(b ==~ /weeks?/) { n = (a as Long); unit = "w"; break }
      }
    }

    // Fallback regex parse for compact forms like "24hr" / "15min".
    if(n == null || unit == null) {
      def m = (q =~ /(\d+)\s*(hours?|hrs?|hr|minutes?|mins?|min|days?|weeks?)\b/)
      if(m && m.find()) {
        n = (m.group(1) as Long)
        String u = (m.group(2) ?: "").toString()
        if(u.startsWith("h")) unit = "h"
        else if(u.startsWith("m")) unit = "m"
        else if(u.startsWith("d")) unit = "d"
        else if(u.startsWith("w")) unit = "w"
      }
    }

    if(n == null || n <= 0L || unit == null) return null

    Long deltaMs = 0L
    if(unit == "h") deltaMs = n * 60L * 60L * 1000L
    else if(unit == "m") deltaMs = n * 60L * 1000L
    else if(unit == "d") deltaMs = n * 24L * 60L * 60L * 1000L
    else if(unit == "w") deltaMs = n * 7L * 24L * 60L * 60L * 1000L
    else return null

    Long maxBackMs = 90L * 24L * 60L * 60L * 1000L
    if(deltaMs > maxBackMs) deltaMs = maxBackMs

    Long nowMs = now() as Long
    return (nowMs - deltaMs) as Long
  } catch(e) {
    return null
  }
}


private String rollingWindowLabel(String query) {
  try {
    String q = (query ?: "").toString().toLowerCase()
    def m = (q =~ /(?:in\s+(?:the\s+)?)?(?:last|past|previous)\s+(\d+)\s*(hours?|hrs?|hr|minutes?|mins?|min|days?|weeks?)\b/)
    if(!m || !m.find()) return null
    String n = m.group(1)
    String unit = m.group(2)
    // Normalize unit phrasing a bit
    if(unit == "hr" || unit == "hrs") unit = "hours"
    if(unit == "min" || unit == "mins") unit = "minutes"
    return "in the last ${n} ${unit}"
  } catch(e) {
    return null
  }
}

private Long windowStartMs(String window) {
  def tz = location?.timeZone ?: TimeZone.getTimeZone("America/New_York")
  def cal = Calendar.getInstance(tz)
  cal.setTime(new Date())
  // normalize to start-of-day
  cal.set(Calendar.HOUR_OF_DAY, 0)
  cal.set(Calendar.MINUTE, 0)
  cal.set(Calendar.SECOND, 0)
  cal.set(Calendar.MILLISECOND, 0)

  switch(window) {
    case "yesterday":
      cal.add(Calendar.DATE, -1)
      return cal.timeInMillis

    case "thisWeek":
      // move to first day of week in hub timezone
      cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek())
      return cal.timeInMillis

    case "lastWeek":
      cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek())
      cal.add(Calendar.DATE, -7)
      return cal.timeInMillis

    case "thisMonth":
      cal.set(Calendar.DAY_OF_MONTH, 1)
      return cal.timeInMillis

    case "lastMonth":
      cal.set(Calendar.DAY_OF_MONTH, 1)
      cal.add(Calendar.MONTH, -1)
      return cal.timeInMillis

    case "today":
    default:
      return cal.timeInMillis
  }
}


private Long lastDeviceEventTs(dev, String attr, String wantValue, Integer maxScan) {
  if(!dev || !attr || wantValue == null) return null
  if(!maxScan || maxScan < 100) maxScan = 100

  // Look back up to 90 days (hard clamp) to avoid expensive full-history scans
  Long sinceMs = now() - (90L * 24L * 60L * 60L * 1000L)
  Date since = new Date(sinceMs)

  List evts = null

  try { evts = dev.eventsSince(since, [max: maxScan]) } catch(ignored) { evts = null }
  if(evts == null) {
    try { evts = dev.statesSince(since, [max: maxScan]) } catch(ignored2) { evts = null }
  }
  if(evts == null) {
    try { evts = dev.events([max: maxScan]) } catch(ignored3) { evts = [] }
  }

  Long best = null
  (evts ?: []).each { e ->
    try {
      String n = (e?.name ?: e?.attribute)?.toString()
      if(n != attr) return
      String v = (e?.value != null) ? e.value.toString() : null
      if(v != wantValue) return

      Date d = e?.date ?: e?.dateCreated ?: e?.rawDateCreated
      Long t = d ? d.time : null
      if(t == null) return
      if(t < sinceMs) return
      if(best == null || t > best) best = t
    } catch(ignored4) {}
  }
  return best
}


private Integer countDeviceEvents(dev, String attr, String wantValue, Date since, Integer maxScan) {
  if(!dev || !attr) return 0
  if(!maxScan || maxScan < 100) maxScan = 100

  List evts = null

  // Preferred: eventsSince (DB query filtered by time)
  try {
    evts = dev.eventsSince(since, [max: maxScan])
  } catch(ignored) { evts = null }

  // Some environments expose statesSince instead
  if(evts == null) {
    try {
      evts = dev.statesSince(since, [max: maxScan])
    } catch(ignored2) { evts = null }
  }

  // Fallback: last N events then filter by date ourselves
  if(evts == null) {
    try {
      evts = dev.events([max: maxScan])
    } catch(ignored3) { evts = [] }
  }

  Long sinceMs = since?.time ?: 0L
  int c = 0
  (evts ?: []).each { e ->
    try {
      String n = (e?.name ?: e?.attribute)?.toString()
      if(n != attr) return
      String v = (e?.value != null) ? e.value.toString() : null
      if(wantValue != null && v != wantValue) return

      Date d = e?.date ?: e?.dateCreated ?: e?.rawDateCreated
      Long t = d ? d.time : null
      if(t != null && t < sinceMs) return

      c++
    } catch(ignored4) {}
  }
  return c
}

private String countPhrase(String attr, String wantValue) {
  if(attr == "contact") return (wantValue == "closed") ? "closed" : "opened"
  if(attr == "switch")  return (wantValue == "off") ? "turned off" : "turned on"
  if(attr == "motion")  return (wantValue == "inactive") ? "inactive" : "active"
  if(attr == "lock")    return (wantValue == "unlocked") ? "unlocked" : "locked"
  if(attr == "water")   return (wantValue == "dry") ? "dry" : "wet"
  if(attr == "presence")return (wantValue == "not present") ? "not present" : "present"
  return "${attr} changed to ${wantValue}"
}

/* ---------------- Formatting ---------------- */


private String htmlEscape(String s) {
  if(s == null) return ""
  return s.toString()
    .replace("&","&amp;")
    .replace("<","&lt;")
    .replace(">","&gt;")
    .replace('"',"&quot;")
    .replace("'","&#39;")
}

private String normalize(String s) {
  if(!s) return ""
  def x = s.toString().toLowerCase()
  x = x.replaceAll("[^a-z0-9\\s]", " ")
  x = x.replaceAll("\\s+", " ").trim()
  return x
}


private String fmtDuration(Long ms) {
  if(ms == null) return "an unknown amount of time"
  long s = (long)Math.floor(ms / 1000L)
  if(s < 60) return "${s} second${s==1?'':'s'}"
  long m = (long)Math.floor(s / 60L); s = s % 60L
  if(m < 60) {
    return "${m} minute${m==1?'':'s'}" + (s>0 ? " ${s} second${s==1?'':'s'}" : "")
  }
  long h = (long)Math.floor(m / 60L); m = m % 60L
  if(h < 24) {
    return "${h} hour${h==1?'':'s'}" + (m>0 ? " ${m} minute${m==1?'':'s'}" : "")
  }
  long d = (long)Math.floor(h / 24L); h = h % 24L
  return "${d} day${d==1?'':'s'}" + (h>0 ? " ${h} hour${h==1?'':'s'}" : "")
}


private String fmtWhen(Long ts) {
  if(!ts) return "at an unknown time"
  def dt = new Date(ts)
  // Example: "today at 8:14 AM" or "Feb 27 at 8:14 AM"
  def nowDt = new Date()
  def sameDay = dt.format("yyyyMMdd", location?.timeZone) == nowDt.format("yyyyMMdd", location?.timeZone)
  def timeStr = dt.format("h:mm a", location?.timeZone)
  if(sameDay) return "today at ${timeStr}"
  def dateStr = dt.format("MMM d", location?.timeZone)
  return "on ${dateStr} at ${timeStr}"
}

private String currentAnswer(String devName, String attr, def val) {
  def v = (val == null) ? "unknown" : val.toString()
  if(attr == "switch")  return "${devName} is ${v == 'on' ? 'on' : (v == 'off' ? 'off' : v)}."
  if(attr == "contact") return "${devName} is ${v == 'open' ? 'open' : (v == 'closed' ? 'closed' : v)}."
  if(attr == "motion")  return "${devName} is ${v == 'active' ? 'active' : (v == 'inactive' ? 'inactive' : v)}."
  if(attr == "lock")    return "${devName} is ${v == 'locked' ? 'locked' : (v == 'unlocked' ? 'unlocked' : v)}."
  if(attr == "water")   return "${devName} is ${v == 'wet' ? 'wet' : (v == 'dry' ? 'dry' : v)}."
  if(attr == "presence")return "${devName} is ${v == 'present' ? 'present' : ((v == 'not present' || v == 'not_present') ? 'not present' : v)}."
  return "${devName} ${attr} is ${v}."
}

private String lastPhrase(String attr, String wantValue) {
  if(attr == "contact") return (wantValue == "closed") ? "closed" : "opened"
  if(attr == "switch")  return (wantValue == "off") ? "turned off" : "turned on"
  if(attr == "motion")  return (wantValue == "inactive") ? "inactive" : "active"
  if(attr == "lock")    return (wantValue == "unlocked") ? "unlocked" : "locked"
  if(attr == "water")   return (wantValue == "dry") ? "dry" : "wet"
  if(attr == "presence")return (wantValue == "not present") ? "not present" : "present"
  return "${attr} changed to ${wantValue}"
}

/* ---------------- Speak + Render ---------------- */

private void maybeSpeak(String text) {
    log.debug "--------------------  In maybeSpeak  --------------------"
    log.debug "Sending text: ${text}"
    if(text && notificationDevice) {
        try { 
            notificationDevice.deviceNotification(text)
            log.debug "Message sent!"
        } catch(e) {
            log.debug "Message was NOT sent. ${e}" 
        }
    }
 
    if(text && speaker) {
    String speakText = applySpeechFriendlyFormatting(text)
    try {
      speaker.speak(speakText)
    } catch(e) {
      // Fallback when speaker rejects SSML markup.
      try { speaker.speak(stripSsmlTags(speakText)) } catch(ignore) { /* ignore */ }
    }
	}
}

private void renderJson(Map m) {
  // Always return real JSON (not Groovy Map toString)
  def body = JsonOutput.toJson(m ?: [:])
  render status: 200, contentType: "application/json", data: body
}

def sendAnswerToPc(String answer) {
    String _sdev = (state?.sdev ?: "").toString().trim()
    if (!_sdev || !state.callbackUrl) return

    try {
        httpGet(uri: state.callbackUrl, query: [r: answer, d: _sdev]) { resp ->
            log.debug "Sent answer to PC for sat '${_sdev}', got ${resp.status}"
        }
    } catch (e) {
        log.error "Failed to send answer to PC for sat '${_sdev}': ${e}"
    }
}

/**
 * Toggle a switch entity on a satellite via the HubVoice runtime.
 *
 * @param satId     Satellite ID (from satellites.csv), e.g. "sat-lr"
 * @param entityId  ESPHome object_id of the switch, e.g. "whisper_mode"
 * @param state     true = on, false = off
 */
def setSatelliteSwitch(String satId, String entityId, boolean state) {
    if(!satId || !entityId) {
        log.warn "setSatelliteSwitch: satId and entityId are required"
        return
    }
    // Derive runtime base URL from the most recent callback URL (strips "/answer")
    String base = (state?.callbackUrl ?: "").toString().replaceAll(/\/answer\??.*$/, "").trim()
    if(!base) {
        log.warn "setSatelliteSwitch: no callbackUrl in state - runtime base URL unknown"
        return
    }
    String stateParam = state ? "on" : "off"
    String url = "${base}/satellite-switch"
    try {
        httpGet(uri: url, query: [d: satId, entity: entityId, state: stateParam]) { resp ->
            log.debug "setSatelliteSwitch ${satId}/${entityId}=${stateParam}: ${resp.status}"
        }
    } catch (e) {
        log.error "setSatelliteSwitch failed for ${satId}/${entityId}: ${e}"
    }
}

/* ---------------- Tokens / URLs ---------------- */

private void ensureAccessToken() {
  if(!state.accessToken) {
    try {
      state.accessToken = createAccessToken()
    } catch(e) {
      // If token creation fails, user must enable OAuth in App status sometimes; but most hubs allow createAccessToken().
      state.accessToken = state.accessToken ?: null
    }
  }
}

private String safeLocalBase() {
  try {
    def u = getFullLocalApiServerUrl()
    return u ?: "http://<hub-ip>"
  } catch(e) {
    return "http://<hub-ip>"
  }
}

private String safeCloudBase() {
  try {
    def u = getFullApiServerUrl()
    return u ?: "https://cloud.hubitat.com"
  } catch(e) {
    return "https://cloud.hubitat.com"
  }
}

private String weatherLocationQuery() {
  String loc = (settings?.weatherLocation ?: "").toString().trim()
  if(loc) return loc
  try {
    def zip = location?.zipCode?.toString()?.trim()
    if(zip) return zip
  } catch(e) {}
  return null
}

private Map getWeatherAnswer(Integer dayIndex) {
  Integer idx = (dayIndex == null || dayIndex < 0) ? 0 : dayIndex
  String loc = weatherLocationQuery()
  if(!loc) {
    return [ok:false, error:"weather_location_missing", answer:"Set a weather location in the HubVoice app first."]
  }

  try {
    String target = java.net.URLEncoder.encode(loc, "UTF-8")
    Map result = null
    httpGet(uri: "https://wttr.in/${target}?format=j1", contentType: "application/json") { resp ->
      if(resp?.status != 200) return
      def data = resp?.data
      if(!(data instanceof Map)) return

      def days = (data.weather instanceof List) ? (data.weather as List) : []
      if(idx >= days.size()) return
      def day = days[idx] instanceof Map ? (days[idx] as Map) : [:]
      def cur = data.current_condition instanceof List && data.current_condition ? data.current_condition[0] : [:]

      String label = (idx == 0) ? "Today" : "Tomorrow"
      String cond = null
      if(day?.hourly instanceof List && day.hourly) {
        def mid = day.hourly.find { it instanceof Map && (it.time?.toString() in ["1200","900","1500"]) }
        if(mid instanceof Map) {
          try { cond = mid.weatherDesc?.getAt(0)?.value?.toString() } catch(e) {}
        }
      }
      if(!cond) {
        try { cond = cur?.weatherDesc?.getAt(0)?.value?.toString() } catch(e) {}
      }
      String hi = (day?.maxtempF ?: day?.maxtempC ?: "").toString()
      String lo = (day?.mintempF ?: day?.mintempC ?: "").toString()
      String current = (cur?.temp_F ?: cur?.temp_C ?: "").toString()

      String ans
      if(idx == 0 && current) {
        ans = "${label}'s weather is ${cond ?: 'unavailable'} with a current temperature of ${current} degrees"
        if(hi || lo) ans += ", and a high of ${hi} and low of ${lo}"
        ans += "."
      } else {
        ans = "${label}'s weather looks ${cond ?: 'unavailable'}"
        if(hi || lo) ans += " with a high of ${hi} and low of ${lo}"
        ans += "."
      }
      result = [ok:true, answer:ans]
    }
    return result ?: [ok:false, error:"weather_unavailable", answer:"Sorry, I couldn't get the weather right now."]
  } catch(e) {
    log.debug "Weather lookup failed: ${e}"
    return [ok:false, error:"weather_error", answer:"Sorry, I couldn't get the weather right now."]
  }
}

/* ---------------- Extra helpers: commands, numeric compare, summaries ---------------- */

private Map parseComparison(String query) {
  try {
    String q = (query ?: "").toString().toLowerCase()
    // e.g. "below 60", "under 25%", "above 75 degrees"
    def m = (q =~ /(above|over|greater than|more than|below|under|less than)\s+(-?\d+(?:\.\d+)?)/)
    if(!m || !m.find()) return null
    String word = m.group(1)
    String num  = m.group(2)
    String op = (word in ["above","over","greater than","more than"]) ? "gt" : "lt"
    String unit = q.contains("%") ? "%" : (q.contains("degree") || q.contains("temp") ? "°" : "")
    return [op:op, target:num, unit:unit]
  } catch(e) {
    return null
  }
}

private boolean evalCompare(BigDecimal cur, String op, BigDecimal target) {
  if(op == "gt") return (cur > target)
  if(op == "lt") return (cur < target)
  if(op == "ge") return (cur >= target)
  if(op == "le") return (cur <= target)
  if(op == "eq") return (cur == target)
  return false
}

private String compareWords(String op) {
  if(op == "gt") return "above"
  if(op == "lt") return "below"
  if(op == "ge") return "at or above"
  if(op == "le") return "at or below"
  if(op == "eq") return "equal to"
  return "compared to"
}

private String fmtNumber(BigDecimal n) {
  try {
    if(n == null) return ""
    if(n.scale() <= 0) return n.toBigInteger().toString()
    // trim trailing zeros
    def s = n.stripTrailingZeros().toPlainString()
    return s
  } catch(e) {
    return n?.toString()
  }
}

private String commandAnswer(String devName, String attr, String cmd, def value=null) {
  if(attr == "switch") {
    if(cmd == "on") return "Sure. Turning on ${devName}."
    if(cmd == "off") return "Sure. Turning off ${devName}."
  }
  if(attr == "lock") {
    if(cmd == "lock") return "Sure. Locking ${devName}."
    if(cmd == "unlock") return "Sure. Unlocking ${devName}."
  }
  if(attr == "level") {
    Integer lvl = (value instanceof Number) ? ((Number)value).intValue() : safeInt(value, null)
    if(lvl != null) return "Sure. Setting ${devName} to ${lvl}%."
    return "Sure. Setting ${devName} level."
  }
  if(attr == "thermostat") {
    BigDecimal v = (value instanceof Number) ? new BigDecimal(value.toString()) : extractNumber(value?.toString())
    if(v != null) {
      if(cmd == "setHeat") return "Sure. Setting heat on ${devName} to ${v}."
      if(cmd == "setCool") return "Sure. Setting cooling on ${devName} to ${v}."
      return "Sure. Setting ${devName} to ${v}."
    }
    return "Sure. Updating ${devName}."
  }

  if(attr == "fan") {
    if(cmd == "setSpeed") return "Sure. Setting ${devName} fan to ${value}."
    if(cmd == "on") return "Sure. Turning on ${devName} fan."
    if(cmd == "off") return "Sure. Turning off ${devName} fan."
  }
  if(attr == "colorTemperature") {
    return "Sure. Setting ${devName} to ${value} kelvin."
  }
  if(attr == "color") {
    return "Sure. Updating ${devName} color."
  }
  if(attr == "scene") {
    return "Sure. Activating ${devName}."
  }

  return "Sure. Done."
}


private Integer safeInt(def x, Integer fallback=null) {
  try {
    if(x == null) return fallback
    if(x instanceof Number) return ((Number)x).intValue()
    String s = x.toString().trim()
    if(!s) return fallback
    return Integer.parseInt(s.replaceAll("[^0-9-]",""))
  } catch(e) {
    return fallback
  }
}

private boolean hasDeviceCommand(dev, String cmdName) {
  try {
    if(dev == null || !cmdName) return false
    // DeviceWrapper typically supports hasCommand(), but be defensive
    if(dev.metaClass?.respondsTo(dev, "hasCommand", String)) {
      return (dev.hasCommand(cmdName) == true)
    }
    // Fallback: try respondTo method name
    return (dev.metaClass?.respondsTo(dev, cmdName)?.size() ?: 0) > 0
  } catch(e) {
    return false
  }
}

// Parse "set <device> to 30%" / "dim <device> to 20" / "set level to 50"
private Integer parseSetLevel(String q) {
  if(!q) return null
  String s = q.toLowerCase()

  // Require a control-ish verb to avoid accidental matches
  boolean okVerb = (s.contains("set ") || s.contains("dim ") || s.contains("brightness") || s.contains("level"))
  if(!okVerb) return null

  def m = (s =~ /\b(?:set|dim|adjust|change)\b.*?\b(?:to|at)\s*(\d{1,3})\s*(?:%|percent)?\b/)
  if(m.find()) {
    Integer v = safeInt(m.group(1), null)
    if(v == null) return null
    if(v < 0) v = 0
    if(v > 100) v = 100
    return v
  }

  // "set <device> 30%" (no "to")
  m = (s =~ /\b(?:set|dim|adjust|change)\b.*?(\d{1,3})\s*(?:%|percent)\b/)
  if(m.find()) {
    Integer v = safeInt(m.group(1), null)
    if(v == null) return null
    if(v < 0) v = 0
    if(v > 100) v = 100
    return v
  }
  return null
}

// Parse thermostat setpoint: "set thermostat to 68", "set heat to 70", "set cooling to 74"
private Map parseThermostatSet(String q) {
  if(!q) return null
  String s = q.toLowerCase()

  // Must include set-style verb + a number that looks like a setpoint
  if(!(s.contains("set ") || s.contains("adjust ") || s.contains("change "))) return null

  def m = (s =~ /\b(?:to|at)\s*(\d{2,3})(?:\.\d+)?\b/)
  if(!m.find()) {
    // "set heat 70"
    m = (s =~ /\bset\b.*?\b(\d{2,3})(?:\.\d+)?\b/)
    if(!m.find()) return null
  }
  BigDecimal v = extractNumber(m.group(1))
  if(v == null) return null

  boolean risky = (v < 55 || v > 80)

  // Basic sanity clamp: most residential setpoints
  if(v < 35) v = 35
  if(v > 95) v = 95

  String cmd = "setPoint"
  if(s.contains("heat") || s.contains("heating")) cmd = "setHeat"
  if(s.contains("cool") || s.contains("cooling") || s.contains("ac") || s.contains("a/c")) cmd = "setCool"

  // If user says "thermostat" without heat/cool, keep setPoint
  return [cmd:cmd, value:v, risky:risky]
}

private Map parseFanSet(String q) {
  if(!q) return null
  String s = q.toLowerCase()
  if(!(s.contains("fan") || s.contains("ceiling fan") || s.contains("blower"))) return null
  if(!(s.contains("set ") || s.contains("fan ") || s.contains("speed") || s.contains("to "))) return null

  // speeds
  def m = (s =~ /\b(?:set|change|adjust)\b.*?\bfan\b.*?\b(?:to|at)\s*(low|medium|med|high|auto|on|off)\b/)
  def mm = m.find() ? m : null
  if(mm == null) {
    m = (s =~ /\bfan\b.*?\bspeed\b.*?\b(low|medium|med|high|auto|on|off)\b/)
    mm = m.find() ? m : null
  }
  if(mm != null) {
    String sp = mm.group(1)
    if(sp == "med") sp = "medium"
    if(sp == "off") return [cmd:"off"]
    if(sp == "on")  return [cmd:"on"]
    return [cmd:"setSpeed", value:sp]
  }

  return null
}

private Map parseColorSet(String q) {
  if(!q) return null
  String s = q.toLowerCase()
  if(!(s.contains("color") || s.contains("colour") || s.contains("to "))) return null
  if(!(s.contains("set ") || s.contains("make ") || s.contains("turn ") || s.contains("change "))) return null

  // color temperature: "2700k", "set to 3000 k", "warm white"
  def mt = (s =~ /\b(\d{3,4})\s*k\b/)
  if(mt.find()) {
    Integer k = safeInt(mt.group(1), null)
    if(k != null) return [cmd:"setColorTemperature", value:k]
  }
  if(s.contains("warm white")) return [cmd:"setColorTemperature", value:2700]
  if(s.contains("soft white")) return [cmd:"setColorTemperature", value:3000]
  if(s.contains("cool white")) return [cmd:"setColorTemperature", value:4000]
  if(s.contains("daylight"))   return [cmd:"setColorTemperature", value:5000]

  // named colors
  Map<String, Map> colors = [
    "red":[hue:0,   saturation:100],
    "orange":[hue:10, saturation:100],
    "yellow":[hue:16, saturation:100],
    "green":[hue:33, saturation:100],
    "cyan":[hue:50, saturation:100],
    "blue":[hue:66, saturation:100],
    "purple":[hue:75, saturation:100],
    "pink":[hue:83, saturation:60],
    "white":[hue:0, saturation:0]
  ]
  String hit = null
  colors.keySet().each { c -> if(s.contains(" "+c) || s.contains(c+" ")) { hit = c } }
  if(hit) {
    def base = colors[hit]
    Integer lvl = null
    def ml = (s =~ /\b(\d{1,3})\s*(?:%|percent)\b/)
    if(ml.find()) lvl = safeInt(ml.group(1), null)
    if(lvl != null) { if(lvl<0) lvl=0; if(lvl>100) lvl=100 }
    return [cmd:"setColor", value:[hue:base.hue, saturation:base.saturation, level:(lvl ?: 100)]]
  }
  return null
}



private BigDecimal extractNumber(String query) {
  try {
    def m = ((query ?: "") =~ /(-?\d+(?:\.\d+)?)/)
    if(!m || !m.find()) return null
    return new BigDecimal(m.group(1))
  } catch(e) { return null }
}

private Map findLastActivity() {
  def bestTs = null
  def best = null
  Integer perDevMax = 10
  (qaDevices ?: []).each { d ->
    try {
      def evs = null
      try { evs = d.events([max: perDevMax]) } catch(ignored) { evs = null }
      if(!(evs instanceof List)) return
      evs.each { e ->
        try {
          Long ts = null
          if(e?.date) ts = (e.date.time as Long)
          else if(e?.unixTime) ts = (e.unixTime as Long)
          if(ts == null) return
          if(bestTs == null || ts > bestTs) {
            bestTs = ts
            best = [device:(d.displayName ?: "Device"), attr:(e?.name ?: ""), val:(e?.value ?: ""), ts:ts]
          }
        } catch(ignored2) {}
      }
    } catch(ignored) {}
  }
  return best
}

private List findStaleDevices(Long startMs) {
  def out = []
  (qaDevices ?: []).each { d ->
    try {
      String attr = mostLikelyAttr(d)
      def st = safeCurrentState(d, attr)
      if(!st?.date) return
      Long ts = (st.date.time as Long)
      if(ts < startMs) {
        out << [name:(d.displayName ?: "Device"), attr:attr, ts:ts]
      }
    } catch(ignored) {}
  }
  return out
}
