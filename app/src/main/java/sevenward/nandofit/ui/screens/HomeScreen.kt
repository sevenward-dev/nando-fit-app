package sevenward.nandofit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sevenward.nandofit.data.model.Exercise
import sevenward.nandofit.ui.WorkoutState
import sevenward.nandofit.ui.WorkoutViewModel
import sevenward.nandofit.ui.components.NandoFitLogo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import sevenward.nandofit.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: WorkoutViewModel,
    onStartWorkout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    HomeScreenContent(
        state = state,
        onSelectDay = { viewModel.selectDay(it) },
        onStartWorkout = { index ->
            viewModel.startWorkout(index)
            onStartWorkout()
        },
        onAdjustTimer = { viewModel.adjustTimer(it) },
        onSkipTimer = { viewModel.skipTimer() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: WorkoutState,
    onSelectDay: (Int) -> Unit,
    onStartWorkout: (Int?) -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSkipTimer: () -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NandoNeonGreen)
        }
        return
    }

    var showCalendarSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header estilo Lyfta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NandoFitLogo(modifier = Modifier.height(32.dp))
                
                val currentMonthDay = SimpleDateFormat("MMMM d", Locale("es", "ES")).format(Date())
                Text(
                    text = currentMonthDay.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    color = NandoWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                IconButton(
                    onClick = { showCalendarSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(NandoDarkCard, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendario",
                        tint = NandoNeonGreen
                    )
                }
            }

            // Tira de Calendario Semanal
            WeeklyCalendarStrip(trainedDatesMap = state.trainedDatesMap)

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de sugerencia (simplificada ahora que tenemos el header)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, NandoNeonGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = NandoNeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Rutina sugerida: Día ${state.suggestedDay}",
                        color = NandoWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val tabs = listOf("Lun • Torso", "Mié • Pierna", "Vie • Full Body")
            TabRow(
                selectedTabIndex = state.selectedDay - 1,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = NandoNeonGreen,
                indicator = { tabPositions ->
                    if (state.selectedDay - 1 in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[state.selectedDay - 1]),
                            color = NandoNeonGreen
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedDay == index + 1,
                        onClick = { onSelectDay(index + 1) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (state.selectedDay == index + 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (state.selectedDay == index + 1) NandoNeonGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp), // padding for FAB
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val blocks = state.exercisesForDay.groupBy { it.blockNumber }
                blocks.forEach { (blockNumber, exercises) ->
                    item {
                        Text(
                            text = "BLOQUE $blockNumber",
                            fontWeight = FontWeight.Bold,
                            color = NandoNeonGreen.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    itemsIndexed(exercises) { index, ex ->
                        var isExpanded by remember { mutableStateOf(false) }
                        
                        val completedSets = state.completedSetsByExercise[ex.id] ?: 0
                        val isCompleted = completedSets >= 3

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    onStartWorkout(state.exercisesForDay.indexOf(ex))
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(NandoNeonGreen.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = NandoNeonGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = ex.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (ex.technicalTip.isNotEmpty()) {
                                        IconButton(
                                            onClick = { isExpanded = !isExpanded },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = "Info", tint = NandoNeonGreen)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("3 x ${ex.minReps}-${ex.maxReps} reps", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        }
                                    }
                                    Text(
                                        text = if (isCompleted) "Completado ✓" else "$completedSets/3 series",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isCompleted) NandoNeonGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (isExpanded && ex.technicalTip.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = NandoNeonGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(ex.technicalTip, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 18.sp)
                                    }
                                }
                                if (state.isTimerRunning && state.timerExerciseId == ex.id) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                progress = { state.timerRemainingSeconds / ex.restSecondsBetweenSets.toFloat() },
                                                modifier = Modifier.size(24.dp),
                                                color = NandoNeonGreen,
                                                strokeWidth = 3.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Descanso: ${state.timerRemainingSeconds}s", color = NandoNeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(onClick = { onAdjustTimer(-15) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)) { Text("-15s", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp) }
                                            TextButton(onClick = { onSkipTimer() }, contentPadding = PaddingValues(0.dp), modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)) { Text("Saltar", color = NandoNeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                            TextButton(onClick = { onAdjustTimer(15) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)) { Text("+15s", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onStartWorkout(null) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .safeDrawingPadding()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = NandoNeonGreen, contentColor = NandoBlack),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.currentSessionId != null) "CONTINUAR RUTINA" else "INICIAR ENTRENAMIENTO", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }

    if (showCalendarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCalendarSheet = false },
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            ConsistencyCalendarSheet(
                trainedDatesMap = state.trainedDatesMap,
                currentStreakWeeks = state.currentStreakWeeks,
                monthlyWorkoutsCount = state.monthlyWorkoutsCount
            )
        }
    }
}

@Composable
fun WeeklyCalendarStrip(trainedDatesMap: Map<String, sevenward.nandofit.data.model.WorkoutSession>) {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.time = Date()
    
    // Set to start of week
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }

    val daysOfWeek = listOf("L", "M", "M", "J", "V", "S", "D")
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until 7) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayNum = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val isToday = dateStr == todayStr
            val isTrained = trainedDatesMap.containsKey(dateStr)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isToday) "HOY" else daysOfWeek[i],
                    color = if (isToday) NandoNeonGreen else NandoWhite.copy(alpha = 0.5f),
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                    fontSize = 12.sp
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isToday && !isTrained) NandoDarkCard else if (isTrained) NandoNeonGreen.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isTrained) NandoNeonGreen else if (isToday) NandoNeonGreen.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTrained) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Entrenado",
                            tint = NandoNeonGreen,
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                        )
                    }
                    Text(
                        text = dayNum,
                        color = if (isTrained) NandoNeonGreen else NandoWhite,
                        fontWeight = if (isToday || isTrained) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

@Composable
fun ConsistencyCalendarSheet(
    trainedDatesMap: Map<String, sevenward.nandofit.data.model.WorkoutSession>,
    currentStreakWeeks: Int,
    monthlyWorkoutsCount: Int
) {
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Constancia",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NandoWhite,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Insights Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.weight(1f).aspectRatio(1f),
                colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (monthlyWorkoutsCount / 12f).coerceIn(0f, 1f) },
                            modifier = Modifier.size(64.dp),
                            color = NandoNeonGreen,
                            strokeWidth = 6.dp,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Text("$monthlyWorkoutsCount", color = NandoWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sesiones este mes", color = NandoWhite.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            ElevatedCard(
                modifier = Modifier.weight(1f).aspectRatio(1f),
                colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔥", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("$currentStreakWeeks", color = NandoWhite, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Semanas en racha", color = NandoWhite.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                val newCal = selectedMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                selectedMonth = newCal
                selectedDate = null
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = NandoWhite)
            }
            
            Text(
                text = monthFormat.format(selectedMonth.time).replaceFirstChar { it.uppercase() },
                color = NandoWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            IconButton(onClick = { 
                val newCal = selectedMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                selectedMonth = newCal
                selectedDate = null
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = NandoWhite)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Grid
        val daysOfWeek = listOf("L", "M", "M", "J", "V", "S", "D")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { day ->
                Text(day, color = NandoWhite.copy(alpha = 0.5f), fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val cal = selectedMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        // Ajuste para que Lunes sea el primer día (1) y Domingo sea el último (7)
        var firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sun) to 6 (Sat)
        if (firstDayOfWeek == 0) firstDayOfWeek = 7 // 1 (Mon) to 7 (Sun)
        val offset = firstDayOfWeek - 1

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val numRows = kotlin.math.ceil((maxDays + offset) / 7.0).toInt()

        var currentDay = 1

        for (row in 0 until numRows) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceAround) {
                for (col in 0 until 7) {
                    if (row == 0 && col < offset || currentDay > maxDays) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, currentDay)
                        val dateStr = dateFormat.format(cal.time)
                        val isTrained = trainedDatesMap.containsKey(dateStr)
                        val isSelected = selectedDate == dateStr
                        
                        // Determinar si es un día sugerido (L, M, V) -> (col == 0, 2, 4)
                        val isScheduled = (col == 0 || col == 2 || col == 4) && !isTrained && cal.time.after(Date())

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(if (isTrained) NandoNeonGreen else if (isSelected) NandoDarkCard else androidx.compose.ui.graphics.Color.Transparent)
                                .border(
                                    1.dp, 
                                    if (isSelected && !isTrained) NandoNeonGreen else if (isScheduled) NandoWhite.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent, 
                                    CircleShape
                                )
                                .clickable { 
                                    if (isTrained) selectedDate = dateStr 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                color = if (isTrained) NandoBlack else NandoWhite,
                                fontWeight = if (isTrained || isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                        currentDay++
                    }
                }
            }
        }
        
        // Detalle interactivo
        AnimatedVisibility(visible = selectedDate != null) {
            val session = trainedDatesMap[selectedDate]
            if (session != null) {
                Spacer(modifier = Modifier.height(24.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NandoNeonGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Día ${session.dayNumber}", color = NandoWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Volumen", color = NandoWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                                Text("${session.totalVolumeKg} kg", color = NandoWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Duración", color = NandoWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                                val mins = session.durationSeconds / 60
                                Text("${mins} min", color = NandoWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    NandoFitTheme {
        HomeScreenContent(
            state = WorkoutState(
                isLoading = false,
                suggestedDay = 1,
                selectedDay = 1,
                exercisesForDay = listOf(
                    Exercise("1", 1, 1, 1, "Press Inclinado", 8, 10, 150, 150),
                    Exercise("2", 1, 1, 2, "Jalón al Pecho", 8, 12, 120, 150)
                )
            ),
            onSelectDay = {},
            onStartWorkout = {},
            onAdjustTimer = {},
            onSkipTimer = {}
        )
    }
}
