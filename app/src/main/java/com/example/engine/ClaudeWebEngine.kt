package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class WebSearchResult(
    val title: String,
    val snippet: String,
    val url: String,
    val source: String
)

data class UrlFetchResult(
    val url: String,
    val statusCode: Int,
    val isSuccess: Boolean,
    val title: String,
    val contentPreview: String,
    val byteSize: Long,
    val contentType: String,
    val latencyMs: Long,
    val errorMessage: String? = null
)

data class ConnectivityStatus(
    val isConnected: Boolean,
    val latencyMs: Long,
    val endpoint: String,
    val details: String
)

object ClaudeWebEngine {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    /**
     * Testa a conectividade real com a Internet medindo a latência em milissegundos.
     */
    suspend fun checkConnectivity(): ConnectivityStatus = withContext(Dispatchers.IO) {
        val testUrl = "https://dns.google/resolve?name=google.com&type=A"
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(testUrl)
                .header("User-Agent", "ClaudeReplica/1.0 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                val elapsed = System.currentTimeMillis() - start
                if (response.isSuccessful) {
                    ConnectivityStatus(
                        isConnected = true,
                        latencyMs = elapsed,
                        endpoint = "dns.google (HTTPS)",
                        details = "Conexão de alta velocidade ativa • Latência: ${elapsed}ms • HTTP ${response.code}"
                    )
                } else {
                    ConnectivityStatus(
                        isConnected = false,
                        latencyMs = elapsed,
                        endpoint = testUrl,
                        details = "Resposta inesperada: HTTP ${response.code}"
                    )
                }
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            ConnectivityStatus(
                isConnected = false,
                latencyMs = elapsed,
                endpoint = testUrl,
                details = "Falha de conexão: ${e.localizedMessage ?: "Sem acesso à rede"}"
            )
        }
    }

    /**
     * Realiza busca na internet em tempo real consultando múltiplas fontes (Wikipedia PT/EN e DuckDuckGo API).
     */
    suspend fun searchWeb(query: String): List<WebSearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<WebSearchResult>()
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val encoded = try {
            URLEncoder.encode(cleanQuery, "UTF-8")
        } catch (_: Exception) {
            cleanQuery
        }

        // 1. Wikipedia em Português (Busca de Artigos e Conceitos)
        try {
            val wikiUrl = "https://pt.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&utf8=1&format=json&srlimit=4"
            val req = Request.Builder()
                .url(wikiUrl)
                .header("User-Agent", "ClaudeReplicaBot/1.0 (Android; Education)")
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val json = JSONObject(body)
                    val queryObj = json.optJSONObject("query")
                    val searchArr = queryObj?.optJSONArray("search")
                    if (searchArr != null) {
                        for (i in 0 until searchArr.length()) {
                            val item = searchArr.getJSONObject(i)
                            val title = item.optString("title", "")
                            val snippetRaw = item.optString("snippet", "")
                            val cleanSnippet = stripHtml(snippetRaw)
                            val pageUrl = "https://pt.wikipedia.org/wiki/${URLEncoder.encode(title.replace(' ', '_'), "UTF-8")}"
                            results.add(
                                WebSearchResult(
                                    title = title,
                                    snippet = cleanSnippet,
                                    url = pageUrl,
                                    source = "Wikipédia (PT)"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Segue para a próxima fonte se falhar
        }

        // 2. DuckDuckGo Instant Answer API
        try {
            val ddgUrl = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            val req = Request.Builder()
                .url(ddgUrl)
                .header("User-Agent", "ClaudeReplicaBot/1.0 (Android)")
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val json = JSONObject(body)
                    val abstractText = json.optString("AbstractText", "")
                    val abstractSource = json.optString("AbstractSource", "DuckDuckGo")
                    val abstractUrl = json.optString("AbstractURL", "")
                    val heading = json.optString("Heading", cleanQuery)

                    if (abstractText.isNotBlank()) {
                        results.add(
                            0, // Prioridade
                            WebSearchResult(
                                title = heading,
                                snippet = abstractText,
                                url = if (abstractUrl.isNotBlank()) abstractUrl else "https://duckduckgo.com/?q=$encoded",
                                source = abstractSource
                            )
                        )
                    }

                    val related = json.optJSONArray("RelatedTopics")
                    if (related != null) {
                        for (i in 0 until minOf(related.length(), 3)) {
                            val topic = related.optJSONObject(i) ?: continue
                            val text = topic.optString("Text", "")
                            val firstUrl = topic.optString("FirstURL", "")
                            if (text.isNotBlank()) {
                                results.add(
                                    WebSearchResult(
                                        title = text.take(60),
                                        snippet = text,
                                        url = firstUrl,
                                        source = "DuckDuckGo Web"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignora erro
        }

        // 3. Wikipedia EN como fallback caso PT não encontre nada
        if (results.isEmpty()) {
            try {
                val wikiEnUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&utf8=1&format=json&srlimit=3"
                val req = Request.Builder()
                    .url(wikiEnUrl)
                    .header("User-Agent", "ClaudeReplicaBot/1.0 (Android; Education)")
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val json = JSONObject(body)
                        val queryObj = json.optJSONObject("query")
                        val searchArr = queryObj?.optJSONArray("search")
                        if (searchArr != null) {
                            for (i in 0 until searchArr.length()) {
                                val item = searchArr.getJSONObject(i)
                                val title = item.optString("title", "")
                                val snippetRaw = item.optString("snippet", "")
                                val cleanSnippet = stripHtml(snippetRaw)
                                val pageUrl = "https://en.wikipedia.org/wiki/${URLEncoder.encode(title.replace(' ', '_'), "UTF-8")}"
                                results.add(
                                    WebSearchResult(
                                        title = title,
                                        snippet = cleanSnippet,
                                        url = pageUrl,
                                        source = "Wikipedia (EN)"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) { }
        }

        results
    }

    /**
     * Faz download real do conteúdo HTML ou JSON de qualquer URL da internet e extrai o texto legível.
     */
    suspend fun fetchUrl(targetUrl: String): UrlFetchResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        var normalized = targetUrl.trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }

        try {
            val req = Request.Builder()
                .url(normalized)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 ClaudeReplica/1.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,application/json,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            client.newCall(req).execute().use { resp ->
                val latency = System.currentTimeMillis() - start
                val code = resp.code
                val contentType = resp.header("Content-Type") ?: "text/html"
                val bodyStr = resp.body?.string() ?: ""
                val byteSize = bodyStr.toByteArray().size.toLong()

                if (resp.isSuccessful) {
                    val title = extractTitle(bodyStr).ifBlank { normalized }
                    val cleanText = extractReadableText(bodyStr)
                    UrlFetchResult(
                        url = normalized,
                        statusCode = code,
                        isSuccess = true,
                        title = title,
                        contentPreview = cleanText.take(1800),
                        byteSize = byteSize,
                        contentType = contentType,
                        latencyMs = latency
                    )
                } else {
                    UrlFetchResult(
                        url = normalized,
                        statusCode = code,
                        isSuccess = false,
                        title = "Erro HTTP $code",
                        contentPreview = "O servidor retornou status HTTP $code.",
                        byteSize = byteSize,
                        contentType = contentType,
                        latencyMs = latency,
                        errorMessage = "HTTP $code: ${resp.message}"
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - start
            UrlFetchResult(
                url = normalized,
                statusCode = 0,
                isSuccess = false,
                title = "Falha ao acessar URL",
                contentPreview = "",
                byteSize = 0,
                contentType = "unknown",
                latencyMs = latency,
                errorMessage = e.localizedMessage ?: "Erro de conexão de rede"
            )
        }
    }

    /**
     * Extrai a tag <title> de documentos HTML.
     */
    private fun extractTitle(html: String): String {
        return try {
            val pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                stripHtml(matcher.group(1)?.trim() ?: "")
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Limpa scripts, estilos e tags HTML, convertendo para texto limpo e legível.
     */
    fun extractReadableText(html: String): String {
        return try {
            var text = html
            // Remove scripts e estilos
            text = text.replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            text = text.replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            text = text.replace(Regex("<!--[\\s\\S]*?-->"), " ")
            // Remove tags HTML
            text = stripHtml(text)
            // Normaliza espaços em branco e quebras de linha
            text = text.replace(Regex("\\s+"), " ").trim()
            text
        } catch (_: Exception) {
            html.take(500)
        }
    }

    /**
     * Remove tags HTML e decodifica entidades básicas.
     */
    fun stripHtml(input: String): String {
        return input
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }
}
