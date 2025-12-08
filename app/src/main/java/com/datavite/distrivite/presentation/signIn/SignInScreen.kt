package com.datavite.distrivite.presentation.signIn

import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.generated.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SignUpScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@Destination<RootGraph>
@Composable
fun SignInScreen(navigator: DestinationsNavigator, viewModel: SignInViewModel = hiltViewModel()){
    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(context, viewModel){
        viewModel.authenticationState.collect{ authenticationState ->
            when(authenticationState) {
                is AuthenticationState.AuthInitial -> {
                    Toast.makeText(context, "Initial Setup", Toast.LENGTH_SHORT).show()
                }

                is AuthenticationState.Authenticated -> {
                    Toast.makeText(context, authenticationState.authMessage, Toast.LENGTH_SHORT).show()
                    navigator.navigate(ProfileScreenDestination){
                        //popUpTo(SignInScreenDestination.route){inclusive= true}
                    }
                }

                is AuthenticationState.UnAuthenticated -> {
                    Toast.makeText(context, authenticationState.unAuthenticatedMessage, Toast.LENGTH_SHORT).show()
                }

                is AuthenticationState.Error -> {
                    Toast.makeText(context, authenticationState.errorMessage, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }

        }
    }
    Column (modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        TextField(
            value = state.email,
            onValueChange = {
                viewModel.onEvent(SignInEvent.EmailChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Email")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = state.password,
            onValueChange = {
                viewModel.onEvent(SignInEvent.PasswordChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Password")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row (horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    navigator.navigate(SignUpScreenDestination())
                },
            ) {
                Text(text = "Sign Up")
            }
            Button(
                onClick = {
                    viewModel.onEvent(SignInEvent.SubmitButtonClicked)
                },
            ) {
                Text(text = "Sign In")
            }
        }

    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}