package sevenward.nandofit.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sevenward.nandofit.WorkoutTimerService
import sevenward.nandofit.data.model.Exercise
import sevenward.nandofit.data.model.SetLog
import sevenward.nandofit.data.model.WorkoutSession
import sevenward.nandofit.data.repository.FirestoreRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OverloadBadge { GREEN, AMBER, WHITE }

data class WorkoutState(
    val suggestedDay: Int = 1,
    val selectedDay: Int = 1,
    val exercisesForDay: List<Exercise> = emptyList(),
    val currentSessionId: String? = null,
    val currentExerciseIndex: Int = 0,
    val currentSetNumber: Int = 1,
    val completedSetsByExercise: Map<String, Int> = emptyMap(),
    val ghostInputs: List<SetLog> = emptyList(),
    val overloadBadge: OverloadBadge = OverloadBadge.WHITE,
    val timerRemainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val timerExerciseId: String? = null,
    val isSessionComplete: Boolean = false,
    val totalSessionVolume: Double = 0.0,
    val sessionStartTime: Long = 0L,
    val allSessions: List<WorkoutSession> = emptyList(),
    val trainedDatesMap: Map<String, WorkoutSession> = emptyMap(),
    val currentStreakWeeks: Int = 0,
    val monthlyWorkoutsCount: Int = 0,
    val isLoading: Boolean = true
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirestoreRepository()
    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedExercisesIfEmpty()
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Verificamos si existe CUALQUIER sesión hoy, sin importar el día (1, 2 o 3)
            val anySessionToday = repository.getAnySessionForToday(todayDateString)
            
            val nextDay = if (anySessionToday != null) {
                anySessionToday.dayNumber
            } else {
                val lastSession = repository.getLastCompletedSession()
                when (lastSession?.dayNumber) {
                    1 -> 2
                    2 -> 3
                    3 -> 1
                    else -> 1
                }
            }
            
            _state.value = _state.value.copy(
                suggestedDay = nextDay,
                selectedDay = nextDay,
                isLoading = false
            )
            selectDay(nextDay)
            refreshCalendarData()
        }
    }

    fun refreshCalendarData() {
        viewModelScope.launch {
            val sessions = repository.getAllWorkoutSessions()
            
            // Priorizar sesiones completadas o recientes por fecha
            val trainedMap = sessions
                .filter { it.dateString.isNotEmpty() && (it.completed || it.totalVolumeKg > 0) }
                .groupBy { it.dateString }
                .mapValues { entry ->
                    entry.value.maxByOrNull { it.totalVolumeKg } ?: entry.value.first()
                }

            // Calcular mes actual
            val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val monthlyCount = trainedMap.keys.count { it.startsWith(currentMonthStr) }

            // Calcular semanas en racha (aproximación simplificada)
            // Agrupamos por año y semana del año
            val calendar = java.util.Calendar.getInstance()
            val weekSet = trainedMap.keys.mapNotNull { dateStr ->
                try {
                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                    if (d != null) {
                        calendar.time = d
                        val year = calendar.get(java.util.Calendar.YEAR)
                        val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
                        "$year-$week"
                    } else null
                } catch (e: Exception) { null }
            }.toSet()

            calendar.time = Date()
            var currentStreak = 0
            while (true) {
                val year = calendar.get(java.util.Calendar.YEAR)
                val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
                if (weekSet.contains("$year-$week")) {
                    currentStreak++
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, -1)
                } else {
                    // Ver si apenas es inicio de semana y no ha entrenado, probar con la semana pasada
                    if (currentStreak == 0) {
                        val prevCal = calendar.clone() as java.util.Calendar
                        prevCal.add(java.util.Calendar.WEEK_OF_YEAR, -1)
                        val pYear = prevCal.get(java.util.Calendar.YEAR)
                        val pWeek = prevCal.get(java.util.Calendar.WEEK_OF_YEAR)
                        if (weekSet.contains("$pYear-$pWeek")) {
                            currentStreak++
                            calendar.add(java.util.Calendar.WEEK_OF_YEAR, -2) // Ya contamos la anterior
                            continue
                        }
                    }
                    break
                }
            }

            _state.value = _state.value.copy(
                allSessions = sessions,
                trainedDatesMap = trainedMap,
                monthlyWorkoutsCount = monthlyCount,
                currentStreakWeeks = currentStreak
            )
        }
    }

    fun selectDay(day: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedDay = day, isLoading = true)
            val exercises = repository.getExercisesForDay(day)
            
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val sessionForToday = repository.getTodayActiveOrLatestSession(todayDateString, day)
            
            if (sessionForToday != null) {
                val logs = repository.getSetLogsForSession(sessionForToday.id)
                val completedMap = logs.groupBy { it.exerciseId }.mapValues { it.value.size }
                
                // Calculate current exercise index and set number based on the logs
                var currentExIndex = 0
                var currentSetNum = 1
                
                for ((index, ex) in exercises.withIndex()) {
                    val setsForEx = completedMap[ex.id] ?: 0
                    if (setsForEx < 3) {
                        currentExIndex = index
                        currentSetNum = setsForEx + 1
                        break
                    }
                }
                
                // If all exercises have 3 sets, point to the last one
                if (currentExIndex == 0 && exercises.isNotEmpty() && (completedMap[exercises.last().id] ?: 0) >= 3) {
                    currentExIndex = exercises.size - 1
                    currentSetNum = 3
                }
                
                val totalVolume = logs.sumOf { it.weightKg * it.reps }
                
                _state.value = _state.value.copy(
                    exercisesForDay = exercises,
                    currentSessionId = sessionForToday.id,
                    sessionStartTime = sessionForToday.dateTimestamp,
                    totalSessionVolume = totalVolume,
                    completedSetsByExercise = completedMap,
                    isSessionComplete = sessionForToday.completed,
                    currentExerciseIndex = currentExIndex,
                    currentSetNumber = currentSetNum
                )
            } else {
                _state.value = _state.value.copy(
                    exercisesForDay = exercises,
                    currentSessionId = null,
                    sessionStartTime = 0L,
                    totalSessionVolume = 0.0,
                    completedSetsByExercise = emptyMap(),
                    isSessionComplete = false,
                    currentExerciseIndex = 0,
                    currentSetNumber = 1
                )
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }


    fun startWorkout(targetExerciseIndex: Int? = null) {
        viewModelScope.launch {
            var sessionId = state.value.currentSessionId
            if (sessionId == null) {
                val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val session = WorkoutSession(
                    dayNumber = state.value.selectedDay,
                    dateTimestamp = System.currentTimeMillis(),
                    dateString = todayDateString
                )
                sessionId = repository.createWorkoutSession(session)
                _state.value = _state.value.copy(
                    currentSessionId = sessionId,
                    sessionStartTime = System.currentTimeMillis(),
                    totalSessionVolume = 0.0,
                    completedSetsByExercise = emptyMap(),
                    isSessionComplete = false
                )
            }
            
            val targetIndex = targetExerciseIndex ?: getFirstIncompleteExerciseIndex()
            val completedSets = state.value.completedSetsByExercise[state.value.exercisesForDay.getOrNull(targetIndex)?.id] ?: 0
            
            _state.value = _state.value.copy(
                currentExerciseIndex = targetIndex,
                currentSetNumber = completedSets + 1
            )
            loadExerciseDataForCurrent()
        }
    }

    private fun getFirstIncompleteExerciseIndex(): Int {
        val st = state.value
        for ((index, ex) in st.exercisesForDay.withIndex()) {
            val completedSets = st.completedSetsByExercise[ex.id] ?: 0
            if (completedSets < 3) return index
        }
        return 0
    }

    private suspend fun loadExerciseDataForCurrent() {
        val st = state.value
        if (st.currentExerciseIndex < st.exercisesForDay.size) {
            val exercise = st.exercisesForDay[st.currentExerciseIndex]
            val ghosts = repository.getGhostInputs(exercise.id, st.currentSessionId)
            
            val badge = if (ghosts.size == 3) {
                val allMax = ghosts.all { it.reps >= exercise.maxReps }
                if (allMax) OverloadBadge.GREEN else OverloadBadge.AMBER
            } else {
                OverloadBadge.WHITE
            }
            
            _state.value = _state.value.copy(
                ghostInputs = ghosts,
                overloadBadge = badge
            )
        } else {
            finishWorkout()
        }
    }

    fun saveSetAndContinue(weightKg: Double, reps: Int, effort: String) {
        viewModelScope.launch {
            val st = state.value
            val exercise = st.exercisesForDay[st.currentExerciseIndex]
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val setLog = SetLog(
                sessionId = st.currentSessionId ?: "",
                exerciseId = exercise.id,
                setNumber = st.currentSetNumber,
                weightKg = weightKg,
                reps = reps,
                effortLevel = effort,
                timestamp = System.currentTimeMillis(),
                dateString = todayDateString
            )
            repository.saveSetLog(setLog)
            
            // Actualizar volumen de sesión
            val newVolume = st.totalSessionVolume + (weightKg * reps)
            val sessionToUpdate = WorkoutSession(
                id = st.currentSessionId ?: "",
                dayNumber = st.selectedDay,
                dateTimestamp = st.sessionStartTime,
                dateString = todayDateString,
                durationSeconds = (System.currentTimeMillis() - st.sessionStartTime) / 1000,
                totalVolumeKg = newVolume,
                completed = false
            )
            repository.updateWorkoutSession(sessionToUpdate)
            
            val newCompletedSets = st.completedSetsByExercise.toMutableMap()
            newCompletedSets[exercise.id] = (newCompletedSets[exercise.id] ?: 0) + 1
            
            val nextSetNumber = if (st.currentSetNumber < 3) st.currentSetNumber + 1 else {
                val nextIndex = st.currentExerciseIndex + 1
                if (nextIndex < st.exercisesForDay.size) {
                    (newCompletedSets[st.exercisesForDay[nextIndex].id] ?: 0) + 1
                } else {
                    1
                }
            }
            val nextExerciseIndex = if (st.currentSetNumber < 3) st.currentExerciseIndex else st.currentExerciseIndex + 1
            
            if (nextExerciseIndex < st.exercisesForDay.size) {
                val timerSeconds = if (st.currentSetNumber < 3) exercise.restSecondsBetweenSets else exercise.transitionRestSeconds
                
                _state.value = _state.value.copy(
                    completedSetsByExercise = newCompletedSets,
                    totalSessionVolume = newVolume,
                    currentSetNumber = nextSetNumber,
                    currentExerciseIndex = nextExerciseIndex
                )
                
                startTimer(timerSeconds, exercise.id)
                if (st.currentSetNumber == 3) {
                    loadExerciseDataForCurrent()
                }
            } else {
                _state.value = _state.value.copy(
                    completedSetsByExercise = newCompletedSets,
                    totalSessionVolume = newVolume
                )
                finishWorkout()
            }
            refreshCalendarData()
        }
    }

    fun updateSetupPinNote(exerciseId: String, note: String) {
        viewModelScope.launch {
            repository.updateExerciseSetupPinNote(exerciseId, note)
            val updatedList = state.value.exercisesForDay.map {
                if (it.id == exerciseId) it.copy(setupPinNote = note) else it
            }
            _state.value = _state.value.copy(exercisesForDay = updatedList)
        }
    }

    private fun vibrateDevice() {
        val context = getApplication<Application>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    private fun startTimer(seconds: Int, exerciseId: String) {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            timerRemainingSeconds = seconds, 
            isTimerRunning = true,
            timerExerciseId = exerciseId
        )
        
        // Iniciar servicio en primer plano para mostrar la cápsula
        val exerciseName = state.value.exercisesForDay.find { it.id == exerciseId }?.name ?: "Descanso"
        val intent = Intent(getApplication(), WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_START
            putExtra(WorkoutTimerService.EXTRA_TIME_MILLIS, seconds * 1000L)
            putExtra(WorkoutTimerService.EXTRA_EXERCISE_NAME, exerciseName)
        }
        getApplication<Application>().startService(intent)

        timerJob = viewModelScope.launch {
            while (_state.value.timerRemainingSeconds > 0) {
                delay(1000L)
                _state.value = _state.value.copy(timerRemainingSeconds = _state.value.timerRemainingSeconds - 1)
            }
            _state.value = _state.value.copy(isTimerRunning = false, timerExerciseId = null)
            vibrateDevice()
            stopTimerService()
        }
    }

    fun adjustTimer(deltaSeconds: Int) {
        val newTime = (_state.value.timerRemainingSeconds + deltaSeconds).coerceAtLeast(0)
        _state.value = _state.value.copy(timerRemainingSeconds = newTime)
        
        if (newTime == 0) {
            timerJob?.cancel()
            _state.value = _state.value.copy(isTimerRunning = false, timerExerciseId = null)
            stopTimerService()
        } else {
            // Actualizar la notificación con el nuevo tiempo
            val exerciseName = state.value.exercisesForDay.find { it.id == state.value.timerExerciseId }?.name ?: "Descanso"
            val intent = Intent(getApplication(), WorkoutTimerService::class.java).apply {
                action = WorkoutTimerService.ACTION_START
                putExtra(WorkoutTimerService.EXTRA_TIME_MILLIS, newTime * 1000L)
                putExtra(WorkoutTimerService.EXTRA_EXERCISE_NAME, exerciseName)
            }
            getApplication<Application>().startService(intent)
        }
    }

    fun skipTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(timerRemainingSeconds = 0, isTimerRunning = false, timerExerciseId = null)
        stopTimerService()
    }
    
    private fun stopTimerService() {
        val intent = Intent(getApplication(), WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    private fun finishWorkout() {
        viewModelScope.launch {
            val st = state.value
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val session = WorkoutSession(
                id = st.currentSessionId ?: "",
                dayNumber = st.selectedDay,
                dateTimestamp = st.sessionStartTime,
                dateString = todayDateString,
                durationSeconds = (System.currentTimeMillis() - st.sessionStartTime) / 1000,
                totalVolumeKg = st.totalSessionVolume,
                completed = true
            )
            repository.updateWorkoutSession(session)
            _state.value = _state.value.copy(isSessionComplete = true)
            refreshCalendarData()
        }
    }

    // Use for ProgressScreen
    private val _history = MutableStateFlow<List<SetLog>>(emptyList())
    val history: StateFlow<List<SetLog>> = _history.asStateFlow()

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val allExercises: StateFlow<List<Exercise>> = _allExercises.asStateFlow()

    fun loadAllExercises() {
        viewModelScope.launch {
            _allExercises.value = repository.getAllExercises()
        }
    }

    fun loadHistoryForExercise(exerciseId: String) {
        viewModelScope.launch {
            _history.value = repository.getExerciseHistory(exerciseId)
        }
    }
}
