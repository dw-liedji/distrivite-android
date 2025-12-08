package com.datavite.distrivite.presentation.signUp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.generated.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SignInScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun SignUpScreen(navigator: DestinationsNavigator, viewModel: SignUpViewModel = hiltViewModel()){
    val state = viewModel.state
    Column(modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = state.firstName,
            onValueChange = {
                viewModel.onEvent(SignUpEvent.FirstNameChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Firstname")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = state.lastName,
            onValueChange = {
                viewModel.onEvent(SignUpEvent.LastNameChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Lastname")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = state.birthDate,
            onValueChange = {
                viewModel.onEvent(SignUpEvent.BirthDateChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "BirthDate")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = state.email,
            onValueChange = {
                viewModel.onEvent(SignUpEvent.EmailChanged(it))
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
                viewModel.onEvent(SignUpEvent.PasswordChanged(it))
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
                    navigator.navigate(SignInScreenDestination())
                },
            ) {
                Text(text = "Sign In")
            }
            Button(
                onClick = {
                    navigator.navigate(ProfileScreenDestination())
                },
            ) {
                Text(text = "Sign Up")
            }
        }
    }
}
