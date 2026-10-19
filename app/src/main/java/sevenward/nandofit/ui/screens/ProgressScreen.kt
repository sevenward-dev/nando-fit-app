package sevenward.nandofit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sevenward.nandofit.data.model.Exercise
import sevenward.nandofit.data.model.SetLog
import sevenward.nandofit.ui.WorkoutViewModel
import sevenward.nandofit.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    val allExercises by viewModel.allExercises.collectAsState()
    val history by viewModel.history.collectAsState()
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAllExercises()
    }

    LaunchedEffect(selectedExercise) {
        selectedExercise?.let {
            viewModel.loadHistoryForExercise(it.id)
        }
    }
    
    ProgressScreenContent(
        allExercises = allExercises,
        history = history,
        selectedExercise = selectedExercise,
        onExerciseSelect = { selectedExercise = it },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreenContent(
    allExercises: List<Exercise>,
    history: List<SetLog>,
    selectedExercise: Exercise?,
    onExerciseSelect: (Exercise) -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolución", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = NandoWhite,
                    navigationIconContentColor = NandoNeonGreen
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Selector de Ejercicio con estilo mejorado
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedExercise?.name ?: "Selecciona un ejercicio",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NandoNeonGreen) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NandoNeonGreen,
                        unfocusedBorderColor = NandoDarkCard,
                        focusedTextColor = NandoWhite,
                        unfocusedTextColor = NandoWhite,
                        containerColor = NandoDarkCard
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(NandoDarkCard)
                ) {
                    allExercises.forEach { ex ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(ex.name, color = NandoWhite, fontWeight = FontWeight.Bold)
                                    Text("Día ${ex.dayNumber}", color = NandoWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                onExerciseSelect(ex)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedVisibility(
                visible = selectedExercise != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
            ) {
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay registros aún", color = NandoWhite.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Analytics Header
                        item {
                            ExerciseAnalyticsHeader(history)
                        }

                        val grouped = history.groupBy { it.sessionId }.values.sortedByDescending { sets -> sets.first().timestamp }
                        
                        grouped.forEach { sets ->
                            val date = Date(sets.first().timestamp)
                            val format = SimpleDateFormat("EEEE, d MMM yyyy", Locale("es", "ES")).format(date)
                                .replaceFirstChar { it.uppercase() }
                            
                            val sessionVolume = sets.sumOf { it.weightKg * it.reps }
                            val best1RM = sets.maxOfOrNull { estimate1RM(it.weightKg, it.reps) } ?: 0.0
                            
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = format,
                                            fontWeight = FontWeight.Bold,
                                            color = NandoWhite,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Vol: ${sessionVolume.toInt()} kg",
                                            fontWeight = FontWeight.Bold,
                                            color = NandoNeonGreen,
                                            fontSize = 14.sp
                                        )
                                    }
                                    
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("SERIE", color = NandoWhite.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                                Text("PESO X REPS", color = NandoWhite.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                                                Text("ESFUERZO", color = NandoWhite.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                                            }
                                            
                                            sets.forEachIndexed { index, setLog ->
                                                val isPR = estimate1RM(setLog.weightKg, setLog.reps) == best1RM
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Set Number Bubble
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .background(if (isPR) NandoNeonGreen.copy(alpha = 0.2f) else NandoWhite.copy(alpha = 0.1f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("${setLog.setNumber}", color = if (isPR) NandoNeonGreen else NandoWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                    
                                                    // Weight x Reps
                                                    Text(
                                                        text = "${setLog.weightKg.toString().removeSuffix(".0")} kg × ${setLog.reps}", 
                                                        fontWeight = FontWeight.ExtraBold, 
                                                        color = NandoWhite,
                                                        fontSize = 18.sp,
                                                        modifier = Modifier.weight(2f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    
                                                    // Effort Badge
                                                    EffortBadge(effort = setLog.effortLevel, modifier = Modifier.weight(1.5f))
                                                }
                                                if (index < sets.size - 1) {
                                                    HorizontalDivider(color = NandoWhite.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EffortBadge(effort: String, modifier: Modifier = Modifier) {
    val cleanEffort = effort.substringBefore("(").trim()
    val (bgColor, textColor) = when {
        cleanEffort.contains("Fallo", ignoreCase = true) || cleanEffort.contains("FAILURE", ignoreCase = true) -> 
            Color(0xFFEF4444).copy(alpha = 0.2f) to Color(0xFFEF4444)
        cleanEffort.contains("Óptimo", ignoreCase = true) || cleanEffort.contains("OPTIMAL", ignoreCase = true) -> 
            Color(0xFFF59E0B).copy(alpha = 0.2f) to Color(0xFFF59E0B)
        else -> 
            NandoNeonGreen.copy(alpha = 0.2f) to NandoNeonGreen
    }
    
    val displayText = when {
        cleanEffort.contains("Fallo", ignoreCase = true) || cleanEffort.contains("FAILURE", ignoreCase = true) -> "FALLO"
        cleanEffort.contains("Óptimo", ignoreCase = true) || cleanEffort.contains("OPTIMAL", ignoreCase = true) -> "ÓPTIMO"
        else -> "FÁCIL"
    }
    
    Box(
        modifier = modifier
            .wrapContentWidth(Alignment.End)
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(displayText, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

@Composable
fun ExerciseAnalyticsHeader(history: List<SetLog>) {
    if (history.isEmpty()) return
    
    val maxWeight = history.maxOfOrNull { it.weightKg } ?: 0.0
    val maxVolume = history.groupBy { it.sessionId }.values.maxOfOrNull { sets -> sets.sumOf { it.weightKg * it.reps } } ?: 0.0
    val best1RM = history.maxOfOrNull { estimate1RM(it.weightKg, it.reps) } ?: 0.0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalyticCard(
            title = "PR Peso",
            value = "${maxWeight.toString().removeSuffix(".0")} kg",
            icon = Icons.Default.EmojiEvents,
            modifier = Modifier.weight(1f)
        )
        AnalyticCard(
            title = "Max Vol",
            value = "${maxVolume.toInt()} kg",
            icon = Icons.Default.AutoGraph,
            modifier = Modifier.weight(1f)
        )
        AnalyticCard(
            title = "1RM Est.",
            value = "${best1RM.roundToInt()} kg",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AnalyticCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = NandoNeonGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = NandoNeonGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = NandoWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(title, color = NandoNeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// Epley Formula for 1RM
fun estimate1RM(weight: Double, reps: Int): Double {
    if (reps == 1) return weight
    return weight * (1 + reps / 30.0)
}

@Preview
@Composable
fun ProgressScreenPreview() {
    NandoFitTheme {
        ProgressScreenContent(
            allExercises = listOf(Exercise(name = "Sentadilla")),
            history = listOf(SetLog(setNumber = 1, weightKg = 100.0, reps = 10, effortLevel = "RIR 2")),
            selectedExercise = Exercise(name = "Sentadilla"),
            onExerciseSelect = {},
            onBack = {}
        )
    }
}
