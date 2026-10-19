package sevenward.nandofit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sevenward.nandofit.ui.WorkoutState
import sevenward.nandofit.ui.WorkoutViewModel
import sevenward.nandofit.ui.theme.*

@Composable
fun SummaryScreen(
    viewModel: WorkoutViewModel,
    onHomeReturn: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    SummaryScreenContent(state = state, onHomeReturn = onHomeReturn)
}

@Composable
fun SummaryScreenContent(
    state: WorkoutState,
    onHomeReturn: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = NandoNeonGreen,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "¡Entrenamiento Completado!", 
                style = MaterialTheme.typography.headlineMedium, 
                color = MaterialTheme.colorScheme.onBackground, 
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            val durationSeconds = if (state.sessionStartTime > 0) (System.currentTimeMillis() - state.sessionStartTime) / 1000 else state.timerRemainingSeconds.toLong()
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.background(NandoNeonGreen.copy(alpha = 0.15f), CircleShape).padding(12.dp)) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = NandoNeonGreen)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(String.format("%02d:%02d", mins, secs), color = NandoWhite, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Duración", color = NandoWhite.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
                
                ElevatedCard(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = NandoDarkCard),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.background(NandoNeonGreen.copy(alpha = 0.15f), CircleShape).padding(12.dp)) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NandoNeonGreen)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.totalSessionVolume.toString().removeSuffix(".0"), color = NandoWhite, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Volumen (kg)", color = NandoWhite.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onHomeReturn,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NandoNeonGreen, contentColor = NandoBlack),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("VOLVER AL INICIO", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        }
    }
}

@Preview
@Composable
fun SummaryScreenPreview() {
    NandoFitTheme {
        SummaryScreenContent(
            state = WorkoutState(
                totalSessionVolume = 4500.0,
                timerRemainingSeconds = 3600 // 60 mins dummy
            ),
            onHomeReturn = {}
        )
    }
}
