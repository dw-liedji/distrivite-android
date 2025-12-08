package com.datavite.distrivite.presentation.instructorContract

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.datavite.distrivite.R
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainInstructorContract

@Composable
fun InstructorContractCard(
    domainInstructorContract: DomainInstructorContract,
    authOrgUser: AuthOrgUser,
    onClickRegister: () -> Unit,
    onClickReport: () -> Unit,
    onClickDeleteEmbeddings: () -> Unit,
    onClickContribute: () -> Unit
) {
    val studentStatus = if (domainInstructorContract.isManager) {
        " (Admin) " // Green color for 100% or more
    } else {
        "" // Default primary color
    }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Organization Image
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.person2),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Spacer(modifier = Modifier.weight(16f))

                    // Spacer
                    Text(
                        text = domainInstructorContract.name + studentStatus,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = domainInstructorContract.contract,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            // Contact Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val isFaced = domainInstructorContract.embeddings.isNotEmpty()
                if (isFaced) {

                    ActionButton(
                        onClick = onClickReport,
                        icon = Icons.Filled.QueryStats,
                        contentDescription = "Report",
                        buttonText = "Report",
                    )

                    if (authOrgUser.isAdmin) {
                        ActionButton(
                            onClick = onClickRegister,
                            icon = Icons.Filled.CheckCircle,
                            contentDescription = "Register",
                            buttonText = "Register"
                        )

                        ActionButton(
                            onClick = onClickDeleteEmbeddings,
                            icon = Icons.Filled.Delete,
                            contentDescription = "Del",
                            buttonText = "Del"
                        )
                    }else {
                        ActionButton(
                            onClick = onClickRegister,
                            icon = Icons.Filled.CheckCircle,
                            contentDescription = "Register",
                            buttonText = "Register",
                            isEnabled = false
                        )
                    }
                }else {
                    ActionButton(
                        onClick = onClickRegister,
                        icon = Icons.Filled.DocumentScanner,
                        contentDescription = "Register",
                        buttonText = "Register"
                    )

                    ActionButton(
                        onClick = onClickDeleteEmbeddings,
                        icon = Icons.Filled.Delete,
                        contentDescription = "Del",
                        buttonText = "Del"
                    )
                }

            }
        }
    }
    }
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
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                if (isEnabled) onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
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
