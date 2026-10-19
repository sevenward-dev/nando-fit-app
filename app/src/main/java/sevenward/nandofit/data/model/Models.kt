package sevenward.nandofit.data.model

data class Exercise(
    val id: String = "",
    val dayNumber: Int = 0,
    val blockNumber: Int = 0,
    val orderIndex: Int = 0,
    val name: String = "",
    val minReps: Int = 0,
    val maxReps: Int = 0,
    val restSecondsBetweenSets: Int = 0,
    val transitionRestSeconds: Int = 0,
    val technicalTip: String = "",
    val setupPinNote: String = ""
)

data class WorkoutSession(
    val id: String = "",
    val dayNumber: Int = 0,
    val dateTimestamp: Long = 0L,
    val dateString: String = "",
    val durationSeconds: Long = 0L,
    val totalVolumeKg: Double = 0.0,
    val completed: Boolean = false
)

data class SetLog(
    val id: String = "",
    val sessionId: String = "",
    val exerciseId: String = "",
    val setNumber: Int = 0,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val effortLevel: String = "", // EASY, OPTIMAL_80_85, FAILURE
    val timestamp: Long = 0L,
    val dateString: String = ""
)
