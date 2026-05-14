package com.example.postmark.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postmark.ui.theme.InkBlack
import com.example.postmark.ui.theme.MutedStone
import com.example.postmark.ui.theme.Parchment
import com.example.postmark.ui.theme.ParchmentDark

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.success) { if (state.success) onLoggedIn() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Parchment)
            .padding(horizontal = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                // "Par Avion" stamp — angled to feel hand-applied
                Box(
                    modifier = Modifier
                        .rotate(-3f)
                        .border(2.dp, InkBlack)
                        .background(ParchmentDark)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("PAR AVION", style = MaterialTheme.typography.labelLarge, color = MutedStone)
                }

                Spacer(Modifier.height(24.dp))
                Text("Postmark", style = MaterialTheme.typography.displayLarge, color = InkBlack)
                Text(
                    "your travels, stamped in time",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MutedStone
                )

                Spacer(Modifier.height(48.dp))

                FieldLabel("Email")
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = inkColors()
                )
                Spacer(Modifier.height(20.dp))

                FieldLabel("Password")
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    colors = inkColors()
                )

                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                }

                Spacer(Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = vm::register,
                        enabled = !state.loading,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues()
                    ) {
                        Text("REGISTER", letterSpacing = 2.sp, fontSize = 13.sp)
                    }
                    Button(
                        onClick = vm::login,
                        enabled = !state.loading,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlack, contentColor = Parchment),
                        contentPadding = PaddingValues()
                    ) {
                        if (state.loading) CircularProgressIndicator(modifier = Modifier.height(20.dp), color = Parchment, strokeWidth = 2.dp)
                        else Text("LOGIN", letterSpacing = 2.sp, fontSize = 13.sp)
                    }
                }
            }

            Text(
                "\"the world is a book, and those who do not travel read only one page\"",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MutedStone,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MutedStone,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun inkColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = InkBlack,
    unfocusedBorderColor = InkBlack,
    focusedContainerColor = ParchmentDark,
    unfocusedContainerColor = ParchmentDark
)
