package com.datavite.distrivite.presentation.welcome

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.datavite.distrivite.presentation.signIn.AuthenticationState
import com.ramcosta.composedestinations.generated.destinations.ShoppingScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>(start = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    navigator: DestinationsNavigator,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.welcomeUiState.collectAsState()
    val authenticationState by viewModel.authenticationState.collectAsState()

    val context = LocalContext.current

    val spoofingApps = listOf("com.fakegps", "com.spooferapp")
    val installedApps = context.packageManager.getInstalledApplications(0)
    val detected = installedApps.any { it.packageName in spoofingApps }

    if (!isAutoTimeEnabled(context)) {
        // Notify user to enable the automatic time setting
        BasicAlertDialog(onDismissRequest = { /*TODO*/ }) {
            Text(text = "Please enable automatic time in the device settings.")
        }
        // You could display a dialog with instructions on how to enable it
        Log.i("TimeSettings", "Please enable automatic time in the device settings.")
    }else {
        when(authenticationState) {
                is AuthenticationState.Welcome  -> {
                    //Toast.makeText(context, "Please provide show credential for login.", Toast.LENGTH_SHORT).show()
                    TiqTaqWatch()
                }
                is AuthenticationState.AuthInitial  -> {
                    //Toast.makeText(context, "Please provide show credential for login.", Toast.LENGTH_SHORT).show()
                    Login(
                        uiState = uiState,
                        onOrganizationChange = viewModel::updateOrganizationCredential,
                        onEmailChange = viewModel::updateEmailCredential,
                        onPasswordChange = viewModel::updatePasswordCredential,
                        onSignIn = viewModel::signIn
                    )
                }
                is AuthenticationState.Loading -> {
                    LoginIndicator()
                }

                is AuthenticationState.Authenticated -> {
                    val authMessage =(authenticationState as AuthenticationState.Authenticated).authMessage
                    AuthenticationSuccess(message = "You are logged In", )
                    Toast.makeText(context, authMessage, Toast.LENGTH_SHORT).show()
                    navigator.navigate(ShoppingScreenDestination){
                        //popUpTo(WelcomeScreenDestination.route){inclusive= true}
                    }
                }

                is AuthenticationState.UnAuthenticated -> {
                    val unAuthenticatedMessage = (authenticationState as AuthenticationState.UnAuthenticated).unAuthenticatedMessage
                    Toast.makeText(context, unAuthenticatedMessage, Toast.LENGTH_SHORT).show()
                    Login(
                        uiState = uiState,
                        onOrganizationChange = viewModel::updateOrganizationCredential,
                        onEmailChange = viewModel::updateEmailCredential,
                        onPasswordChange = viewModel::updatePasswordCredential,
                        onSignIn = viewModel::signIn
                    )
                }

                is AuthenticationState.Error -> {
                    val errorMessage = (authenticationState as AuthenticationState.Error).errorMessage
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Login(
                        uiState = uiState,
                        onOrganizationChange = viewModel::updateOrganizationCredential,
                        onEmailChange = viewModel::updateEmailCredential,
                        onPasswordChange = viewModel::updatePasswordCredential,
                        onSignIn = viewModel::signIn
                    )
                }
            }

        }

    }



fun isAutoTimeEnabled(context: Context): Boolean {
    return try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME) == 1
    } catch (e: Settings.SettingNotFoundException) {
        e.printStackTrace()
        false
    }
}
@Composable
fun Login(
    uiState: WelcomeUiState,
    onOrganizationChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onSignIn: () -> Unit = {},
    isLoading: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome Back 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Sign in to your organization account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ORG ID
            OutlinedTextField(
                value = uiState.organizationCredential,
                onValueChange = onOrganizationChange,
                label = { Text("Organization ID") },
                placeholder = { Text("e.g., 10245") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            // EMAIL
            OutlinedTextField(
                value = uiState.emailCredential,
                onValueChange = onEmailChange,
                label = { Text("Email Address") },
                placeholder = { Text("you@example.com") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            // PASSWORD
            OutlinedTextField(
                value = uiState.passwordCredential,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = onSignIn,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Sign In", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Optional footer
            TextButton(onClick = { /* Forgot password flow */ }) {
                Text("Forgot password?", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun LoginIndicator() {
    // Loading State: Show a circular progress indicator
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}