package com.example.slo_plo.utils
import com.example.slo_plo.model.LogRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import kotlin.jvm.java

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "logs"

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
    fun saveLogRecord(record: LogRecord, callback: (Boolean) -> Unit) {
        val docId = record.dateId  // LogRecord.dateId에는 이미 DateUtils.toDocId(date) 사용
        db.collection(COLLECTION)
            .document(docId)
            .set(record)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
