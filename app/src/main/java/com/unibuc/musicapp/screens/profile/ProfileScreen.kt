package com.unibuc.musicapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unibuc.musicapp.navigation.MusicScreens
import com.unibuc.musicapp.screens.login.LoginViewModel

@Composable
fun ProfileScreen(navController: NavController,
                  viewModel: ProfileViewModel = hiltViewModel(),
                  loginViewModel: LoginViewModel = hiltViewModel()) {
    val userProfile by viewModel.userInfo.observeAsState()
    if (!loginViewModel.isLoggedIn()) {
        navController.navigate(MusicScreens.LoginScreen.name)
    } else {
        if (userProfile == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top){
                    Text(
                        text = userProfile!!.firstName + " " + userProfile!!.lastName,
                        modifier = Modifier.padding(top = 30.dp).background(Color.Cyan)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .background(Color.Cyan),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.background(Color.Red)

                        ) {
                            Text(text = "Posts", modifier = Modifier.background(Color.Green))
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(Color.Yellow)) {
                                Text(text = userProfile!!.posts.toString(), color = Color.Black)
                            }
                        }

                        // Spacer with a weight modifier to take up available space
                        Spacer(Modifier.weight(1f))


                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                            Text(text = "Following")
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                                Text(text = userProfile!!.following.toString(), color = Color.Black)
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(text = "Followers")
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                                Text(text = userProfile!!.followers.toString(), color = Color.Black)
                            }
                        }
                        
                    }
                }
            }
        }
    }
}