package com.datavite.distrivite.presentation.leave

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainLeave
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun LeaveList(
    leaveRecords: List<DomainLeave>,
    authOrgUser: AuthOrgUser,
    onClickApprove: (domainLeave:DomainLeave) -> Unit,
    onClickReject: (domainLeave:DomainLeave) -> Unit,
    ) {
    // State for current time
    var currentTime by remember { mutableStateOf(getCurrentTime()) }

    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Update every second
            currentTime = getCurrentTime()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Leave List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            items(leaveRecords) { domainLeave ->
                LeaveListItem(
                    domainLeave = domainLeave,
                    authOrgUser = authOrgUser,
                    onClickApprove = {
                        onClickApprove(domainLeave)
                    },
                    onClickReject = {
                        onClickReject(domainLeave)
                    }
                )
            }
        }

    }
}

@Composable
fun LeaveListItem(
    domainLeave: DomainLeave,
    authOrgUser: AuthOrgUser,
    onClickApprove: () -> Unit,
    onClickReject: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = domainLeave.employeeName, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Type: ${domainLeave.type}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Reason: ${domainLeave.reason}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "From: ${domainLeave.startDate}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "To: ${domainLeave.endDate}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: ${domainLeave.status}", style = MaterialTheme.typography.bodyMedium)

            // Contact Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val isPending = domainLeave.status.contains("ending")
                if (isPending) {
                    if (authOrgUser.isAdmin) {
                        ActionButton(
                            onClick = onClickApprove,
                            icon = Icons.Filled.CheckCircle,
                            contentDescription = "Approve",
                            buttonText = "Approve"
                        )

                        ActionButton(
                            onClick = onClickReject,
                            icon = Icons.Filled.Delete,
                            contentDescription = "Reject",
                            buttonText = "Reject"
                        )
                    }else {

                    }
                }else {

                }

            }
        }
    }
}

private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.FRENCH)
    return sdf.format(Date())
}


@Composable
fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    buttonText: String,
    isEnabled:Boolean=true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp).clickable {
            if (isEnabled)  onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = buttonText, color = MaterialTheme.colorScheme.primary)
    }
}

/*
@Preview
@Composable
fun LeaveListScreenPreview() {
    LeaveList(
        leaveRecords = listOf(
            DomainLeave(
                id = "2",
                created = "2023-09-26T10:20:31.120000+01:00",
                modified = "2023-10-26T06:18:05.127316+01:00",
                userId = "f9c81f8c-23e5-4190-947c-322b762fef32",
                orgId = "b9e036b6-d1ea-4f91-8330-2b5acdf291f8",
                orgSlug = "isb",
                employeeId = "617b5de1-6193-4e1b-8b53-266a8b5ab887",
                orgUserId = "617b5de1-6193-4e1b-8b53-266a8b5ab887",
                startDate = "2023-10-21",
                endDate = "2023-10-21",
                reason = "09:00 AM",
                type = "Sick",
                hourlySalary = 500.2,
                status = "Approved",
            ),

        ),
        onClickApprove = {},
        onClickReject = {},
        authOrgUser = AuthOrgUser(

        )
    )
}*/
