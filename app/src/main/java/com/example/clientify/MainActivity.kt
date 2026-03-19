package com.example.clientify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// ─── color palettee
private val AccentBlue      = Color(0xFF2B40F5)
private val AppBlack        = Color(0xFF000000)
private val White           = Color(0xFFFFFFFF)
private val WhiteMuted      = Color(0xB3FFFFFF)   // 70% white
private val WhiteDim        = Color(0x66FFFFFF)   // 40% white
private val SurfaceElevated = Color(0xFF15152A)
private val ErrorRed        = Color(0xFFFF5C6E)

// ─── Activity ─────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClientifyTheme {
                ClientifyApp()
            }
        }
    }
}

@Composable
private fun ClientifyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary      = AccentBlue,
            background   = AppBlack,
            surface      = Color(0xFF0D0D1A),
            onPrimary    = White,
            onBackground = White,
            onSurface    = White,
            error        = ErrorRed
        ),
        content = content
    )
}

// ─── Root Screen ──────────────────────────────────────────────────────────────
@Composable
fun ClientifyApp() {
    var hoursInput    by remember { mutableStateOf("") }
    var rateInput     by remember { mutableStateOf("") }
    var estimatedCost by remember { mutableStateOf<Double?>(null) }
    var hoursError    by remember { mutableStateOf<String?>(null) }
    var rateError     by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hoursInput, rateInput) { estimatedCost = null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f    to AppBlack,
                            0.5563f to AppBlack,
                            1.0f    to AccentBlue
                        ),
                        center = Offset(
                            x = size.width * 0.5009f,
                            y = size.height * -0.0344f
                        ),
                        radius = size.width * 1.1797f
                    )
                )
            }
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        HeaderSection()

        Spacer(modifier = Modifier.height(48.dp))

        ClientifyTextField(
            value         = hoursInput,
            onValueChange = { hoursInput = it; hoursError = null },
            label         = "Working Hours",
            placeholder   = "e.g. 40",
            errorMessage  = hoursError
        )

        Spacer(modifier = Modifier.height(16.dp))

        ClientifyTextField(
            value         = rateInput,
            onValueChange = { rateInput = it; rateError = null },
            label         = "Hourly Rate (Rp)",
            placeholder   = "e.g. 150000",
            errorMessage  = rateError
        )

        Spacer(modifier = Modifier.height(32.dp))

        CalculateButton {
            val hours = hoursInput.trim().toDoubleOrNull()
            val rate  = rateInput.trim().toDoubleOrNull()
            var valid = true

            if (hours == null || hours <= 0) {
                hoursError = "Enter a valid number of hours"
                valid = false
            }
            if (rate == null || rate <= 0) {
                rateError = "Enter a valid hourly rate"
                valid = false
            }
            if (valid && hours != null && rate != null) {
                estimatedCost = hours * rate
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = estimatedCost != null,
            enter = fadeIn(tween(400)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy)
                    ) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(400)
                    )
        ) {
            estimatedCost?.let { ResultCard(cost = it) }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun HeaderSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, AccentBlue.copy(alpha = 0.6f), RoundedCornerShape(50))
                .background(AccentBlue.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text         = "B2B COST ESTIMATOR",
                color        = AccentBlue,
                fontSize     = 10.sp,
                fontWeight   = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Clientify",
            style = TextStyle(
                fontSize     = 52.sp,
                fontWeight   = FontWeight.Black,
                color        = White,
                letterSpacing = (-2).sp,
                shadow = Shadow(
                    color      = AccentBlue.copy(alpha = 0.55f),
                    offset     = Offset(0f, 4f),
                    blurRadius = 28f
                )
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Estimate your project cost instantly",
            style = TextStyle(
                fontSize     = 15.sp,
                fontWeight   = FontWeight.Normal,
                color        = WhiteMuted,
                letterSpacing = 0.2.sp,
                textAlign    = TextAlign.Center
            )
        )
    }
}

// field input
@Composable
private fun ClientifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMessage: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text     = label,
            style    = TextStyle(
                fontSize     = 11.sp,
                fontWeight   = FontWeight.SemiBold,
                color        = WhiteMuted,
                letterSpacing = 0.9.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val borderColor = when {
            errorMessage != null -> ErrorRed
            value.isNotEmpty()   -> AccentBlue.copy(alpha = 0.7f)
            else                 -> White.copy(alpha = 0.08f)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
        ) {
            TextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier.fillMaxWidth(),
                textStyle     = TextStyle(
                    fontSize     = 16.sp,
                    fontWeight   = FontWeight.Medium,
                    color        = White,
                    letterSpacing = 0.3.sp
                ),
                placeholder = {
                    Text(
                        text  = placeholder,
                        style = TextStyle(fontSize = 16.sp, color = WhiteDim)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine      = true,
                colors          = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = AccentBlue
                )
            )
        }

        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text     = errorMessage ?: "",
                color    = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 5.dp, start = 4.dp)
            )
        }
    }
}

// CTA
@Composable
private fun CalculateButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "button_scale"
    )

    Button(
        onClick           = onClick,
        interactionSource = interactionSource,
        modifier          = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        shape  = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = White,
            contentColor   = AppBlack
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text  = "Calculate Cost",
            style = TextStyle(
                fontSize     = 15.sp,
                fontWeight   = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
private fun ResultCard(cost: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AccentBlue.copy(alpha = 0.18f), SurfaceElevated)
                )
            )
            .border(
                width  = 1.dp,
                brush  = Brush.linearGradient(
                    colors = listOf(AccentBlue.copy(alpha = 0.5f), White.copy(alpha = 0.05f))
                ),
                shape  = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 28.dp, vertical = 28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxWidth()
        ) {
            Text(
                text  = "ESTIMATED COST",
                style = TextStyle(
                    fontSize     = 10.sp,
                    fontWeight   = FontWeight.Bold,
                    color        = AccentBlue,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = formatRupiah(cost),
                style = TextStyle(
                    fontSize     = 36.sp,
                    fontWeight   = FontWeight.Black,
                    color        = White,
                    letterSpacing = (-0.5).sp,
                    textAlign    = TextAlign.Center,
                    shadow = Shadow(
                        color      = AccentBlue.copy(alpha = 0.6f),
                        offset     = Offset(0f, 2f),
                        blurRadius = 16f
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier  = Modifier.fillMaxWidth(0.4f),
                color     = White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = "Ready to share with your client",
                style = TextStyle(
                    fontSize     = 12.sp,
                    color        = WhiteMuted,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}

// helper formatting
private fun formatRupiah(value: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
    fmt.maximumFractionDigits = 0
    return "Rp ${fmt.format(value)}"
}
