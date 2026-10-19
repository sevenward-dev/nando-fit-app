package sevenward.nandofit.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.tasks.await
import sevenward.nandofit.data.model.Exercise
import sevenward.nandofit.data.model.SetLog
import sevenward.nandofit.data.model.WorkoutSession

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
    }

    suspend fun seedExercisesIfEmpty() {
        try {
            val batch = db.batch()
            val exercises = mutableListOf<Exercise>()

            // Day 1: Torso
            exercises.add(Exercise("day1_ex1", 1, 1, 1, "Press Inclinado", 8, 10, 150, 150))
            exercises.add(Exercise("day1_ex2", 1, 1, 2, "Jalón al Pecho", 8, 12, 120, 150))
            exercises.add(Exercise("day1_ex3", 1, 1, 3, "Aperturas", 10, 15, 90, 150))
            exercises.add(Exercise("day1_ex4", 1, 1, 4, "Remo en T", 8, 10, 150, 150))
            exercises.add(Exercise("day1_ex5", 1, 2, 5, "Elevaciones Laterales", 12, 15, 75, 120))
            exercises.add(Exercise("day1_ex6", 1, 2, 6, "Curl Bíceps", 10, 12, 90, 120))
            exercises.add(Exercise("day1_ex7", 1, 2, 7, "Extensión Tríceps", 10, 12, 90, 120))

            // Day 2: Pierna
            exercises.add(Exercise("day2_ex1", 2, 1, 1, "Sentadilla en Máquina", 8, 10, 150, 150))
            exercises.add(Exercise("day2_ex2", 2, 1, 2, "Hip Thrust", 8, 12, 150, 150))
            exercises.add(Exercise("day2_ex3", 2, 1, 3, "Extensión de Cuádriceps", 10, 15, 90, 150))
            exercises.add(Exercise("day2_ex4", 2, 1, 4, "Curl Femoral Sentado", 10, 15, 90, 150))
            exercises.add(Exercise("day2_ex5", 2, 2, 5, "Elevaciones Laterales", 12, 15, 75, 120))
            exercises.add(Exercise("day2_ex6", 2, 2, 6, "Pantorrilla", 12, 15, 75, 120))
            exercises.add(Exercise("day2_ex7", 2, 2, 7, "Abdomen", 12, 15, 60, 120))

            // Day 3: Full Body
            exercises.add(Exercise("day3_ex1", 3, 1, 1, "Prensa de Piernas", 8, 10, 150, 150))
            exercises.add(Exercise("day3_ex2", 3, 1, 2, "Press Plano", 8, 10, 150, 150))
            exercises.add(Exercise("day3_ex3", 3, 1, 3, "Remo con Cable", 10, 12, 120, 150))
            exercises.add(Exercise("day3_ex4", 3, 1, 4, "Press Militar", 8, 12, 120, 150))
            exercises.add(Exercise("day3_ex5", 3, 2, 5, "Curl Femoral", 10, 15, 90, 120))
            exercises.add(Exercise("day3_ex6", 3, 2, 6, "Elevaciones Laterales", 12, 15, 75, 120))
            exercises.add(Exercise("day3_ex7", 3, 2, 7, "Biserie Brazos (Bíceps + Tríceps)", 10, 12, 90, 120))

            exercises.forEach { ex ->
                val ref = db.collection("exercises").document(ex.id)
                batch.set(ref, ex)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getExercisesForDay(dayNumber: Int): List<Exercise> {
        return try {
            val snapshot = db.collection("exercises")
                .whereEqualTo("dayNumber", dayNumber)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Exercise::class.java) }.sortedBy { it.orderIndex }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun updateExerciseSetupPinNote(exerciseId: String, note: String) {
        db.collection("exercises").document(exerciseId).update("setupPinNote", note)
    }

    fun createWorkoutSession(session: WorkoutSession): String {
        val ref = db.collection("workoutSessions").document()
        val sessionWithId = session.copy(id = ref.id)
        ref.set(sessionWithId)
        return ref.id
    }

    fun updateWorkoutSession(session: WorkoutSession) {
        if (session.id.isEmpty()) return
        db.collection("workoutSessions").document(session.id).set(session)
    }

    fun saveSetLog(setLog: SetLog) {
        val ref = db.collection("setLogs").document()
        val setWithId = setLog.copy(id = ref.id)
        // We don't use .await() here because if offline, it will suspend forever. 
        // Firestore handles local persistence automatically.
        ref.set(setWithId)
    }

    suspend fun getTodayActiveOrLatestSession(todayDateString: String, dayNumber: Int): WorkoutSession? {
        return try {
            // Buscamos todas las sesiones del día para evitar requerir índices compuestos en Firestore
            val snapshot = db.collection("workoutSessions")
                .whereEqualTo("dateString", todayDateString)
                .get()
                .await()
                
            val sessions = snapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }
            
            // Si no encuentra por dateString, busca por timestamp como respaldo (útil para sesiones antiguas)
            if (sessions.isEmpty()) {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                
                val fallbackSnapshot = db.collection("workoutSessions")
                    .whereEqualTo("dayNumber", dayNumber)
                    .get()
                    .await()
                    
                return fallbackSnapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }
                    .filter { it.dateTimestamp >= startOfToday }
                    .maxByOrNull { it.dateTimestamp }
            }
            
            sessions.filter { it.dayNumber == dayNumber }
                .maxByOrNull { it.dateTimestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSetLogsForSession(sessionId: String): List<SetLog> {
        return try {
            val snapshot = db.collection("setLogs")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(SetLog::class.java) }.sortedBy { it.setNumber }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAnySessionForToday(todayDateString: String): WorkoutSession? {
        return try {
            val snapshot = db.collection("workoutSessions")
                .whereEqualTo("dateString", todayDateString)
                .get()
                .await()
            val sessions = snapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }
            
            if (sessions.isEmpty()) {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                
                val fallbackSnapshot = db.collection("workoutSessions")
                    .get()
                    .await()
                
                return fallbackSnapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }
                    .filter { it.dateTimestamp >= startOfToday }
                    .maxByOrNull { it.dateTimestamp }
            }
            
            sessions.maxByOrNull { it.dateTimestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllWorkoutSessions(): List<WorkoutSession> {
        return try {
            val snapshot = db.collection("workoutSessions").get().await()
            snapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }
                .sortedByDescending { it.dateTimestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLastCompletedSession(): WorkoutSession? {
        return try {
            val snapshot = db.collection("workoutSessions")
                .whereEqualTo("completed", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }
                .maxByOrNull { it.dateTimestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getGhostInputs(exerciseId: String, excludeSessionId: String? = null): List<SetLog> {
        return try {
            val lastSessionWithExSnapshot = db.collection("setLogs")
                .whereEqualTo("exerciseId", exerciseId)
                .get()
                .await()
            
            val validLog = lastSessionWithExSnapshot.documents
                .mapNotNull { it.toObject(SetLog::class.java) }
                .sortedByDescending { it.timestamp }
                .firstOrNull { it.sessionId != excludeSessionId }
                
            if (validLog == null) return emptyList()

            val sessionId = validLog.sessionId
            val allSetsSnapshot = db.collection("setLogs")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()
                
            allSetsSnapshot.documents
                .mapNotNull { it.toObject(SetLog::class.java) }
                .filter { it.exerciseId == exerciseId }
                .sortedBy { it.setNumber }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getExerciseHistory(exerciseId: String): List<SetLog> {
        return try {
            val snapshot = db.collection("setLogs")
                .whereEqualTo("exerciseId", exerciseId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(SetLog::class.java) }.sortedBy { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getAllExercises(): List<Exercise> {
        return try {
            val snapshot = db.collection("exercises").get().await()
            snapshot.documents.mapNotNull { it.toObject(Exercise::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
