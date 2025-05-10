package com.example.slo_plo.utils
import com.example.slo_plo.model.LogRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.jvm.java

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "plogging_logs"

    /** 날짜별 일지 불러오기 */
    fun loadLogRecord(date: LocalDate, callback: (LogRecord?) -> Unit) {
        val docId = DateUtils.toDocId(date)
        db.collection(COLLECTION)
            .document(docId)
            .get()
            .addOnSuccessListener { snap ->
                callback(snap.toObject(LogRecord::class.java))
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    /** 일지 저장 */
    fun saveLogRecord(
        userId: String,
        record: LogRecord,
        callback: (Boolean) -> Unit
    ) {
        val col = db.collection("users")
            .document(userId)
            .collection("plogging_logs")
        col.add(record)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun loadLogRecordsForDate(
        userId: String,
        date: LocalDate,
        callback: (List<LogRecord>) -> Unit
    ) {
        val dateId = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        db.collection("users")
            .document(userId)
            .collection("plogging_logs")
            .whereEqualTo("dateId", dateId)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .mapNotNull { it.toObject(LogRecord::class.java) }
                callback(list)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}
