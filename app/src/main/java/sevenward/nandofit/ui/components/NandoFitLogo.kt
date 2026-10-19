package sevenward.nandofit.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import sevenward.nandofit.ui.theme.NandoBlack
import sevenward.nandofit.ui.theme.NandoNeonGreen
import sevenward.nandofit.ui.theme.NandoWhite

@Composable
fun NandoFitLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp
) {
    val darkTheme = isSystemInDarkTheme()
    val baseColor = if (darkTheme) NandoWhite else NandoBlack
    
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = baseColor,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = fontSize
                )
            ) {
                append("Nando")
            }
            withStyle(
                style = SpanStyle(
                    color = NandoNeonGreen,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = fontSize,
                    shadow = Shadow(
                        color = NandoNeonGreen,
                        offset = Offset.Zero,
                        blurRadius = 12f
                    )
                )
            ) {
                append("Fit+")
            }
        }
    )
}
