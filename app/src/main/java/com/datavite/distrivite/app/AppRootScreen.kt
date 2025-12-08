package com.datavite.distrivite.app

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.datavite.distrivite.presentation.billing.BillingScreen
import com.datavite.distrivite.presentation.billing.BillingViewModel
import com.datavite.distrivite.presentation.shopping.ShoppingScreen
import com.datavite.distrivite.presentation.shopping.ShoppingViewModel
import com.datavite.distrivite.presentation.transaction.TransactionScreen
import com.datavite.distrivite.presentation.transaction.TransactionViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ShoppingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.BillingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TransactionScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppRootScreen(
){

    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        /*
        val instructorContractViewModel: InstructorContractViewModel = hiltViewModel()
        val studentViewModel: StudentViewModel = hiltViewModel()
        val teachingSessionViewModel: TeachingSessionViewModel = hiltViewModel()
        val teachingCourseViewModel : TeachingCourseViewModel = hiltViewModel()
        */
        val shoppingViewModel: ShoppingViewModel = hiltViewModel()
        val billingViewModel: BillingViewModel = hiltViewModel()
        val transactionViewModel: TransactionViewModel = hiltViewModel()

        DestinationsNavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            navGraph = NavGraphs.root,

            navController = navController, //!! this is important,
            dependenciesContainerBuilder = {}
        ){

            composable(ShoppingScreenDestination) {
                ShoppingScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = shoppingViewModel // Pass the shared ViewModel
                )
            }

            composable(BillingScreenDestination) {
                BillingScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = billingViewModel // Pass the shared ViewModel
                )
            }

            composable(TransactionScreenDestination) {
                TransactionScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = transactionViewModel // Pass the shared ViewModel
                )
            }
            /*
            composable(TeachingSessionScreenDestination) {
                TeachingSessionScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = teachingSessionViewModel // Pass the shared ViewModel
                )
            }

            composable(TeachingSessionDetailScreenDestination) {
                TeachingSessionDetailScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = teachingSessionViewModel // Pass the shared ViewModel
                )
            }
            composable(AddSessionScreenDestination) {
                AddSessionScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = teachingSessionViewModel // Pass the shared ViewModel
                )
            }
            composable(TeachingSessionRecognitionScreenDestination) {
                TeachingSessionRecognitionScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = teachingSessionViewModel // Pass the shared ViewModel
                )
            }
            composable(TeachingCourseScreenDestination) {
                TeachingCourseScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = teachingCourseViewModel // Pass the shared ViewModel
                )
            }
            composable(InstructorContractScreenDestination) {
                InstructorContractScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = instructorContractViewModel // Pass the shared ViewModel
                )
            }
            composable(InstructorContractRegisterFaceScreenDestination) {
                InstructorContractRegisterFaceScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = instructorContractViewModel // Pass the shared ViewModel
                )
            }
            composable(InstructorContractRecognitionScreenDestination) {
                InstructorContractRecognitionScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = instructorContractViewModel // Pass the shared ViewModel
                )
            }
            composable(SessionReportScreenDestination) {
                SessionReportScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = instructorContractViewModel // Pass the shared ViewModel
                )
            }
            composable(StudentScreenDestination) {
                StudentScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = studentViewModel // Pass the shared ViewModel
                )
            }
            composable(StudentRegisterFaceScreenDestination) {
                StudentRegisterFaceScreen(
                    navigator = this.destinationsNavigator,
                    viewModel = studentViewModel // Pass the shared ViewModel
                )
            }
            */

        }
    }
}
