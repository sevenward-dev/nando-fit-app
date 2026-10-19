package sevenward.nandofit.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sevenward.nandofit.data.model.Exercise
import sevenward.nandofit.ui.OverloadBadge
import sevenward.nandofit.ui.WorkoutState
import sevenward.nandofit.ui.WorkoutViewModel
import sevenward.nandofit.ui.theme.*

@Composable
fun ActiveWorkoutScreen(
    viewModel: WorkoutViewModel,
    onWorkoutComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSessionComplete) {
        if (state.isSessionComplete) {
            onWorkoutComplete()
        }
    }

    if (state.currentExerciseIndex >= state.exercisesForDay.size) return

    val exercise = state.exercisesForDay[state.currentExerciseIndex]
    
    ActiveWorkoutScreenContent(
        state = state,
        exercise = exercise,
        onUpdateSetupPinNote = { id, note -> viewModel.updateSetupPinNote(id, note) },
        onSaveSetAndContinue = { weight, reps, effort -> viewModel.saveSetAndContinue(weight, reps, effort) },
        onAdjustTimer = { delta -> viewModel.adjustTimer(delta) },
        onSkipTimer = { viewModel.skipTimer() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreenContent(
    state: WorkoutState,
    exercise: Exercise,
    onUpdateSetupPinNote: (String, String) -> Unit,
    onSaveSetAndContinue: (Double, Int, String) -> Unit,
    onAdjustTimer: (Int) -> Unit,
    onSkipTimer: () -> Unit
) {
    var weightInput by remember { mutableStateOf(0.0) }
    var repsInput by remember { mutableStateOf(0) }
    var effortInput by remember { mutableStateOf("Óptimo (RIR 1-2)") }
    var pinNote by remember { mutableStateOf(exercise.setupPinNote) }
    var showPinNoteSheet by remember { mutableStateOf(false) }

    val ghostForSet = state.ghostInputs.find { it.setNumber == state.currentSetNumber }
    LaunchedEffect(exercise.id, state.currentSetNumber, state.ghostInputs) {
        if (ghostForSet != null) {
            weightInput = ghostForSet.weightKg
            repsInput = ghostForSet.reps
        } else {
            weightInput = 0.0
            repsInput = exercise.minReps
        }
        pinNote = exercise.setupPinNote
    }

    val progress = if (state.exercisesForDay.isNotEmpty()) {
        (state.currentExerciseIndex + (state.currentSetNumber - 1) / 3f) / state.exercisesForDay.size.toFloat()
    } else 0f

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Ejercicio ${state.currentExerciseIndex + 1} de ${state.exercisesForDay.size}", fontWeight = FontWeight.Bold)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = NandoNeonGreen,
                    trackColor = NandoDarkCard
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    exercise.name, 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (badgeColor, badgeText) = when (state.overloadBadge) {
                        OverloadBadge.GREEN -> NandoNeonGreen to "Sube +2.5 kg"
                        OverloadBadge.AMBER -> Color(0xFFFFA500) to "Sube reps"
                        OverloadBadge.WHITE -> NandoWhite to "Busca 80-85% RM"
                    }
                    AssistChip(
                        onClick = { },
                        label = { Text(badgeText, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = badgeColor.copy(alpha = 0.15f),
                            labelColor = badgeColor
                        ),
                        border = AssistChipDefaults.assistChipBorder(borderColor = badgeColor.copy(alpha = 0.5f), enabled = true)
                    )
                    
                    AssistChip(
                        onClick = { showPinNoteSheet = true },
                        label = { Text("Ajuste: ${pinNote.ifEmpty { "Agregar" }}") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = NandoDarkCard,
                            labelColor = NandoWhite,
                            leadingIconContentColor = NandoNeonGreen
                        ),
                        border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent, enabled = true)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..3) {
                        val isCompleted = i < state.currentSetNumber
                        val isCurrent = i == state.currentSetNumber
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isCompleted -> NandoNeonGreen
                                        isCurrent -> NandoNeonGreen.copy(alpha = 0.5f)
                                        else -> NandoDarkCard
                                    }
                                )
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Serie ${state.currentSetNumber} de 3", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Objetivo: ${exercise.minReps}-${exercise.maxReps} reps", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f).aspectRatio(0.85f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PESO (KG)", fontWeight = FontWeight.Bold, color = NandoWhite.copy(alpha = 0.6f), fontSize = 12.sp)
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(
                                    weightInput.toString().removeSuffix(".0"),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NandoWhite
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(
                                    onClick = { weightInput = (weightInput - 2.5).coerceAtLeast(0.0) },
                                    modifier = Modifier.background(NandoBlack, CircleShape).size(40.dp)
                                ) { Icon(Icons.Default.Remove, contentDescription = "-", tint = NandoWhite) }
                                
                                IconButton(
                                    onClick = { weightInput += 2.5 },
                                    modifier = Modifier.background(NandoBlack, CircleShape).size(40.dp)
                                ) { Icon(Icons.Default.Add, contentDescription = "+", tint = NandoNeonGreen) }
                            }
                            Text(
                                "Anterior: ${ghostForSet?.weightKg?.toString()?.removeSuffix(".0") ?: "--"}",
                                fontSize = 11.sp,
                                color = NandoWhite.copy(alpha = 0.4f)
                            )
                        }
                    }
                    
                    ElevatedCard(
                        modifier = Modifier.weight(1f).aspectRatio(0.85f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("REPS", fontWeight = FontWeight.Bold, color = NandoWhite.copy(alpha = 0.6f), fontSize = 12.sp)
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(
                                    repsInput.toString(),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NandoWhite
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(
                                    onClick = { repsInput = (repsInput - 1).coerceAtLeast(0) },
                                    modifier = Modifier.background(NandoBlack, CircleShape).size(40.dp)
                                ) { Icon(Icons.Default.Remove, contentDescription = "-", tint = NandoWhite) }
                                
                                IconButton(
                                    onClick = { repsInput += 1 },
                                    modifier = Modifier.background(NandoBlack, CircleShape).size(40.dp)
                                ) { Icon(Icons.Default.Add, contentDescription = "+", tint = NandoNeonGreen) }
                            }
                            Text(
                                "Anterior: ${ghostForSet?.reps ?: "--"}",
                                fontSize = 11.sp,
                                color = NandoWhite.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("ESFUERZO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val effortOptions = listOf("Fácil (RIR 3+)", "Óptimo (RIR 1-2)", "Fallo (RIR 0)")
                    effortOptions.forEach { effort ->
                        val isSelected = effortInput == effort
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NandoNeonGreen else NandoDarkCard)
                                .clickable { effortInput = effort }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = effort.substringBefore("(").trim(),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NandoBlack else NandoWhite,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onSaveSetAndContinue(weightInput, repsInput, effortInput)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = if (state.isTimerRunning) 80.dp else 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NandoNeonGreen, contentColor = NandoBlack),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TERMINAR SERIE ${state.currentSetNumber}", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }

            AnimatedVisibility(
                visible = state.isTimerRunning,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().border(1.dp, NandoNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = NandoBlack)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { state.timerRemainingSeconds / exercise.restSecondsBetweenSets.toFloat() },
                                modifier = Modifier.size(48.dp),
                                color = NandoNeonGreen,
                                trackColor = NandoDarkCard,
                                strokeWidth = 4.dp
                            )
                            Text(
                                "${state.timerRemainingSeconds}",
                                color = NandoWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onAdjustTimer(-15) }) {
                                Text("-15s", color = NandoWhite)
                            }
                            Button(
                                onClick = onSkipTimer,
                                colors = ButtonDefaults.buttonColors(containerColor = NandoNeonGreen, contentColor = NandoBlack),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Saltar", fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { onAdjustTimer(15) }) {
                                Text("+15s", color = NandoWhite)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPinNoteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPinNoteSheet = false },
            containerColor = NandoDarkCard
        ) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 24.dp)) {
                Text("Ajuste de Máquina", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = NandoWhite)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pinNote,
                    onValueChange = { pinNote = it },
                    placeholder = { Text("Ej: Asiento 4, Respaldo 2") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NandoNeonGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        focusedTextColor = NandoWhite,
                        unfocusedTextColor = NandoWhite
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        onUpdateSetupPinNote(exercise.id, pinNote)
                        showPinNoteSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NandoNeonGreen, contentColor = NandoBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
fun ActiveWorkoutScreenPreview() {
    NandoFitTheme {
        ActiveWorkoutScreenContent(
            state = WorkoutState(
                currentSetNumber = 2,
                overloadBadge = OverloadBadge.GREEN,
                isTimerRunning = true,
                timerRemainingSeconds = 45
            ),
            exercise = Exercise(
                name = "Sentadilla",
                blockNumber = 1,
                minReps = 8,
                maxReps = 12,
                technicalTip = "Baja hasta romper el paralelo"
            ),
            onUpdateSetupPinNote = { _, _ -> },
            onSaveSetAndContinue = { _, _, _ -> },
            onAdjustTimer = {},
            onSkipTimer = {}
        )
    }
}
