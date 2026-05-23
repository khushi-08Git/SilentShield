package com.example.silentshield.data.firebase

import com.example.silentshield.presentation.home.AlertType
import com.example.silentshield.presentation.home.CommunityReport
import com.example.silentshield.presentation.home.RiskLevel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("community_reports")

    fun getReportsStream(): Flow<List<CommunityReport>> = callbackFlow {
        val listener = col
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reports = snapshot.documents.mapNotNull { doc ->
                    try {
                        CommunityReport(
                            id          = doc.id,
                            type        = AlertType.valueOf(
                                doc.getString("type") ?: "CALL"
                            ),
                            source      = doc.getString("source") ?: "",
                            description = doc.getString("description") ?: "",
                            reportedBy  = doc.getString("reportedBy") ?: "anonymous",
                            timeAgo     = formatTimestamp(
                                doc.getLong("timestamp") ?: 0L
                            ),
                            upvotes     = (doc.getLong("upvotes") ?: 0L).toInt(),
                            riskLevel   = RiskLevel.valueOf(
                                doc.getString("riskLevel") ?: "MEDIUM"
                            )
                        )
                    } catch (e: Exception) { null }
                }
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    suspend fun submitReport(
        type: AlertType,
        source: String,
        description: String,
        riskLevel: RiskLevel = RiskLevel.MEDIUM
    ): Result<Unit> = try {
        col.add(
            mapOf(
                "type"        to type.name,
                "source"      to source.trim(),
                "description" to description.trim(),
                "reportedBy"  to "user_${(1000..9999).random()}",
                "riskLevel"   to riskLevel.name,
                "upvotes"     to 0,
                "timestamp"   to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun upvoteReport(reportId: String): Result<Unit> = try {
        val doc     = col.document(reportId).get().await()
        val current = doc.getLong("upvotes") ?: 0L
        col.document(reportId).update("upvotes", current + 1).await()
        Result.success(Unit)
    } catch (e: Exception) {

        Result.failure(e)
    }

    private fun formatTimestamp(ts: Long): String {
        if (ts == 0L) return "Just now"
        val diff    = System.currentTimeMillis() - ts
        val minutes = diff / 60_000
        val hours   = diff / 3_600_000
        val days    = diff / 86_400_000
        return when {
            minutes < 1  -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours   < 24 -> "$hours hr ago"
            days    < 7  -> "$days days ago"
            else         -> "A while ago"
        }
    }
}