package com.example.silentshield.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class SafeBrowsingRequest(
    val client: ClientInfo,
    val threatInfo: ThreatInfo
)

data class ClientInfo(
    val clientId: String = "silentshield",
    val clientVersion: String = "1.0.0"
)

data class ThreatInfo(
    val threatTypes: List<String> = listOf(
        "MALWARE",
        "SOCIAL_ENGINEERING",
        "UNWANTED_SOFTWARE",
        "POTENTIALLY_HARMFUL_APPLICATION"
    ),
    val platformTypes: List<String> = listOf("ANDROID"),
    val threatEntryTypes: List<String> = listOf("URL"),
    val threatEntries: List<ThreatEntry>
)

data class ThreatEntry(val url: String)

data class SafeBrowsingResponse(
    val matches: List<ThreatMatch>? = null
)

data class ThreatMatch(
    val threatType: String,
    val platformType: String,
    val threat: ThreatEntry
)

interface SafeBrowsingApi {
    @POST("v4/threatMatches:find")
    suspend fun checkUrl(
        @Query("key") apiKey: String,
        @Body request: SafeBrowsingRequest
    ): SafeBrowsingResponse
}