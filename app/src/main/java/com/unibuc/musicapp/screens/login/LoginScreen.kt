package com.unibuc.musicapp.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.components.EmailInput
import com.unibuc.musicapp.components.PasswordInput
import com.unibuc.musicapp.navigation.MusicScreens


@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {

    if(viewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.FeedScreen.name)
    } else {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {
                // Logo Component
                LoginForm(navController = navController, loading = false) { email, password ->
                    viewModel.login(email, password) {
                        navController.navigate(MusicScreens.FeedScreen.name) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginForm(
    navController: NavController,
    loading: Boolean = false,
    onDone: (String, String) -> Unit = { _, _ -> }
) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordVisibility = rememberSaveable { mutableStateOf(false) }
    val passwordFocusRequest = FocusRequester.Default
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(email.value, password.value) {
        email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }
    val modifier = Modifier
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFADD8E6)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "MUSIC LIVE", fontSize = 30.sp)
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        EmailInput(emailState = email, enabled = !loading,
            onAction = KeyboardActions {
            passwordFocusRequest.requestFocus()
        })
        PasswordInput(
            modifier = Modifier.focusRequester(passwordFocusRequest),
            passwordState=password,
            labelId = "Password",
            enabled = !loading,
            passwordVisibility = passwordVisibility,
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
                onDone(email.value.trim(), password.value.trim())
            })
        SubmitButton(
            textId = "Sign In",
            loading = loading,
            validInputs = valid) {
            onDone(email.value.trim(), password.value.trim())
            keyboardController?.hide()
        }
        Row {
            Text(text = "Don't have an account?")
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "Sign Up",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF0000EE),
                    textDecoration = TextDecoration.Underline
                ),
                modifier=Modifier.clickable { navController.navigate(MusicScreens.RegisterScreen.name) }
            )
        }
    }
}

@Composable
fun SubmitButton(textId: String, loading: Boolean, validInputs: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        enabled = !loading && validInputs,
        shape = CircleShape) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(25.dp))
        else Text(text= textId, modifier = Modifier.padding(5.dp))

    }
}

