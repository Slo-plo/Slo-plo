package com.example.slo_plo.utils
import android.util.Log
import com.example.slo_plo.model.LogRecord
import com.example.slo_plo.model.TrashBin
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.jvm.java

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "plogging_logs"

    //날짜별 일지 불러오기
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

    //일지 저장
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

    fun loadLogRecordsForDate(userId: String, date: LocalDate, callback: (List<LogRecord>) -> Unit) {
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("plogging_logs")
            .whereEqualTo("dateId", dateStr)
            .get()
            .addOnSuccessListener { snapshot ->
                val records = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(LogRecord::class.java)?.copy(docId = doc.id) // ✅ 문서 ID를 직접 넣어줌
                }
                callback(records)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
    
    // 사용자의 모든 일지 정보
    fun loadAllLogRecords(
        userId: String,
        callback: (List<LogRecord>) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("plogging_logs")
            .get()
            .addOnSuccessListener { snap ->
                val records = snap.documents
                    .mapNotNull { it.toObject(LogRecord::class.java) }
                callback(records)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun loadTrashBins(callback: (List<TrashBin>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("trash")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("TrashBin", "문서 수: ${snapshot.size()}")
                val bins = snapshot.documents.mapNotNull { doc ->
                    val bin = doc.toObject(TrashBin::class.java)
                    Log.d("TrashBin", "매핑된 쓰레기통: $bin")
                    bin
                }
                Log.d("TrashBin", "최종 마커 수: ${bins.size}")
                callback(bins)
            }
            .addOnFailureListener { e ->
                Log.e("TrashBin", "Firestore 가져오기 실패: ${e.message}", e)
            }
    }

}
