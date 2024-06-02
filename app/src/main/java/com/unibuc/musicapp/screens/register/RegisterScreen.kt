package com.unibuc.musicapp.screens.register

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.unibuc.musicapp.components.EmailInput
import com.unibuc.musicapp.components.IntInputField
import com.unibuc.musicapp.components.NameInput
import com.unibuc.musicapp.components.PasswordInput
import com.unibuc.musicapp.data.InstrumentOption
import com.unibuc.musicapp.dto.RegisterDto
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel
import com.unibuc.musicapp.screens.login.SubmitButton
import com.unibuc.musicapp.utils.Genre
import com.unibuc.musicapp.utils.Instrument
import com.unibuc.musicapp.utils.Role
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun RegisterScreen(navController: NavController,
                   viewModel: RegisterScreenViewModel = hiltViewModel(),
                   loginViewModel: LoginViewModel = hiltViewModel()
                   ) {
    if(loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.FeedScreen.name)
    } else {
        val isRegistering by viewModel.isRegistering.collectAsState();
        Surface(modifier = Modifier.fillMaxSize()) {

            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {
                if (isRegistering) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(120.dp),
                            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth * 2)
                    }
                } else {
                    // Logo Component
                    RegisterForm(navController = navController, loading = false) { registerDto ->
                        viewModel.register(registerDto) {
                            navController.navigate(MusicScreens.LoginScreen.name) {
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
}

@SuppressLint("UnrememberedMutableState")
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun RegisterForm(
    navController: NavController,
    loading: Boolean,
    onDone: (RegisterDto) -> Unit = { }
) {
    val email = rememberSaveable { mutableStateOf("") }
    val lastName = rememberSaveable { mutableStateOf("") }
    val firstName = rememberSaveable { mutableStateOf("") }
    val age = rememberSaveable { mutableIntStateOf(0) }
    val password = rememberSaveable { mutableStateOf("") }
    val confirmPassword = rememberSaveable { mutableStateOf("") }
    val passwordVisibility = rememberSaveable { mutableStateOf(false) }
    val confirmPasswordVisibility = rememberSaveable { mutableStateOf(false) }
    val passwordFocusRequest = FocusRequester.Default
    val confirmPasswordFocusRequest = FocusRequester.Default
    val lastNameFocusRequest = FocusRequester.Default
    val firstNameFocusRequest = FocusRequester.Default
    val ageFocusRequest = FocusRequester.Default
    val userHasInteracted = remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(Role.USER) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val options = remember { mutableStateListOf(
        InstrumentOption(Instrument.VOCALIST, false),
        InstrumentOption(Instrument.ACOUSTIC_GUITAR, false),
        InstrumentOption(Instrument.ELECTRIC_GUITAR, false),
        InstrumentOption(Instrument.BASS, false),
        InstrumentOption(Instrument.DRUMS, false)
    ) }

    val ageRepresentation = when {
        // If the user hasn't interacted yet, and the number is 0, show empty
        !userHasInteracted.value && age.intValue == 0 -> ""
        // Otherwise, show the actual number, even if it's 0
        else -> age.intValue.toString()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = derivedStateOf{
        (email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
                && lastName.value.trim().isNotEmpty() && firstName.value.trim().isNotEmpty()
                && confirmPassword.value.trim() == password.value.trim()
                && (age.intValue > 0) && (age.intValue < 120) && (imageUri != null)
                && options.any { it.isSelected })

    }.value
    val modifier = Modifier
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())



    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Image picked
                imageUri = result.data?.data // URI of selected image
            }
        }
    )

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, open gallery
                openImagePicker()
            } else {
                // Permission denied
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun prepareImageFilePart(context: Context, imageUri: Uri, partName: String): MultipartBody.Part {
        // Get the file's content URI and open an input stream
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val file = File(context.cacheDir, "temp_image") // Create a temporary file
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }

        // Create RequestBody instance from file
        val requestFile = file
            .asRequestBody("image/jpeg".toMediaTypeOrNull())
        inputStream?.close()
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    // Optional: Display the selected image


    Column(modifier = Modifier
        .padding(top = 10.dp)
        .verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop

                // Adjust size as needed
            )
        }
        Button(onClick = {
            // Check and request permission
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    openImagePicker() // No need for permissions on API 29 and above for gallery access
                }
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    openImagePicker() // Permission already granted
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }) {
            Text("Upload Image")
        }

        EmailInput(emailState = email, enabled = !loading,
            onAction = KeyboardActions {
                lastNameFocusRequest.requestFocus()
            })
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            NameInput(
                modifier = Modifier
                    .width(180.dp)
                    .focusRequester(lastNameFocusRequest),
                valueState = lastName, enabled = !loading, labelId= "Last Name",
                onAction = KeyboardActions {
                    firstNameFocusRequest.requestFocus()
                })

            NameInput(
                modifier = Modifier
                    .width(180.dp)
                    .focusRequester(firstNameFocusRequest),
                valueState = firstName, enabled = !loading, labelId= "First Name",
                onAction = KeyboardActions {
                    ageFocusRequest.requestFocus()
                })
        }
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {

            IntInputField(
                modifier = Modifier
                    .width(180.dp)
                    .focusRequester(firstNameFocusRequest),
                valueState = age, textRepresentation = ageRepresentation,
                userHasInteracted = userHasInteracted, enabled = !loading,
                onAction = KeyboardActions {
                    passwordFocusRequest.requestFocus()
                })
            RoleDropdownMenu(selectedRole = selectedRole, onSelectRole = { selectedRole = it })
        }
        PasswordInput(
            modifier = Modifier.focusRequester(passwordFocusRequest),
            passwordState=password,
            labelId = "Password",
            enabled = !loading,
            passwordVisibility = passwordVisibility,
            imeAction= ImeAction.Next,
            onAction = KeyboardActions {
                confirmPasswordFocusRequest.requestFocus()
            })
        PasswordInput(
            modifier = Modifier.focusRequester(passwordFocusRequest),
            passwordState=confirmPassword,
            labelId = "Confirm Password",
            enabled = !loading,
            passwordVisibility = confirmPasswordVisibility,
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
            })

        MultiSelectDropdown(options)

        SubmitButton(
            textId = "Sign Up",
            loading = loading,
            validInputs = valid) {
            onDone(
                RegisterDto(
                email.value.trim(),
                password.value.trim(),
                lastName.value.trim(),
                firstName.value.trim(),
                selectedRole,
                age.intValue,
                prepareImageFilePart(context, imageUri!!, "profilePicture"),
                options.filter { it.isSelected }.map { it.instrument }.toList()),

            )
            keyboardController?.hide()
        }

        Row {
            Text(text = "Already have an account?")
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "Sign In",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF0000EE),
                    textDecoration = TextDecoration.Underline
                ),
                modifier=Modifier.clickable { navController.navigate(MusicScreens.LoginScreen.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(options: MutableList<InstrumentOption>) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = options.filter { it.isSelected }.joinToString { it.instrument.toString() },
            onValueChange = {},
            label = { Text("Select Instruments") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown Icon",
                    Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .width(350.dp)
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .width(350.dp)
                .padding(horizontal = 20.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    option.isSelected = !option.isSelected
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                            }
                    ) {
                        Checkbox(
                            checked = option.isSelected,
                            onCheckedChange = { isSelected ->
                                option.isSelected = isSelected
                            }
                        )
                        Text(text = option.instrument.toString(), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdownMenu(selectedRole: Role, onSelectRole: (Role) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(Role.USER, Role.MANAGER)

    Column (modifier = Modifier
        .width(180.dp)
        .padding(end = 10.dp, bottom = 2.dp, start = 10.dp), verticalArrangement = Arrangement.Center) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            androidx.compose.material.TextField(
                readOnly = true,
                value = selectedRole.toString(),
                onValueChange = {},
                label = { Text(text = "Genre", fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .menuAnchor() // Important for positioning the menu
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                items.forEachIndexed { index, item ->
                    val backgroundColor = if (index % 2 == 0) Color(0xFFECEBEB) else Color.White
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = item.toString()) },
                        onClick = {
                            onSelectRole(item)
                            expanded = false
                        },
                        modifier = Modifier.background(backgroundColor)
                    )
                }
            }
        }
    }
}
