package com.datavite.distrivite.presentation.logout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.generated.destinations.WelcomeScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun OrgSignOutScreen(navigator: DestinationsNavigator, viewModel: OrgSignOutViewModel = hiltViewModel()) {
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val totalPendingOperation by viewModel.totalPendingOperation.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoggedOut){
            navigator.navigate(WelcomeScreenDestination){
                //popUpTo(WelcomeScreenDestination.route){inclusive=true}
            }
        }else {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                if (totalPendingOperation != 0 ) {
                    Text(text = "You still have $totalPendingOperation pending operations. You must sync before signing out. Please, make sure you synced before trying again.")
                }
                else {

                    Text(text = "Are you sure to Sign out ?")
                    Button(onClick = {
                        viewModel.signOut()
                    }) {
                        Text(text = "Sign out")
                    }
                }
            }

        }



    }
}
