package com.datavite.distrivite.app
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Difference
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.BillingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.BulkCreditPaymentScreenDestination
import com.ramcosta.composedestinations.generated.destinations.CloudScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShoppingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TransactionScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec


enum class BottomNavigationBarItem(
    val direction: DirectionDestinationSpec,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
){
    Shop(direction = ShoppingScreenDestination,
        title = "Shop",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        hasNews = true
    ),
    Bill(direction = BillingScreenDestination,
        title = "Bills",
        selectedIcon = Icons.Filled.Difference,
        unselectedIcon = Icons.Outlined.Difference,
        hasNews = true
    ),
    Transaction(direction = TransactionScreenDestination,
        title = "TXT",
        selectedIcon = Icons.Filled.AttachMoney,
        unselectedIcon = Icons.Outlined.AttachMoney,
        hasNews = true
    ),
    RecoverCredit(direction = BulkCreditPaymentScreenDestination,
        title = "Recv",
        selectedIcon = Icons.Filled.CreditCard,          // Changed
        unselectedIcon = Icons.Outlined.CreditCard,      // Changed
        hasNews = true
    ),
    Cloud(direction = CloudScreenDestination,
        title = "Cloud",
        selectedIcon = Icons.Filled.Cloud,
        unselectedIcon = Icons.Outlined.Cloud,
        hasNews = true
    ),
}

@Composable
fun BottomNavigationBar(route: String, destinationsNavigator: DestinationsNavigator) {
    NavigationBar {
        // Track selected item based on the current route
        val selectedItemIndex = BottomNavigationBarItem.entries.indexOfFirst { it.direction.route == route }

        BottomNavigationBarItem.entries.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedItemIndex,
                onClick = {
                    if (index == selectedItemIndex) {
                        // If the current item is selected, pop back stack to its root
                        destinationsNavigator.popBackStack(item.direction, false)
                    } else {
                        // Navigate to the selected item's direction
                        destinationsNavigator.navigate(item.direction) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = {
                    Text(text = item.title)
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount != null) {
                                Badge {
                                    Text(text = item.badgeCount.toString())
                                }
                            } else if (item.hasNews) {
                                Badge()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }
                }
            )
        }
    }
}



