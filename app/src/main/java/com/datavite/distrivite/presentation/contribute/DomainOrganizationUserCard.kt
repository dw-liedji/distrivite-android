package com.datavite.distrivite.presentation.contribute

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.R
import com.datavite.distrivite.domain.model.DomainOrganizationUser


@Composable
fun DomainOrganizationUserCard(
    domainOrganizationUser: DomainOrganizationUser,
    onClickLearn: () -> Unit,
    onClickTeach: () -> Unit,
    onClickDeleteEmbeddings: () -> Unit,
    onClickContribute: () -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Organization Image
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(shape = RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Spacer
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = domainOrganizationUser.name,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = domainOrganizationUser.name,
                style = MaterialTheme.typography.titleMedium
            )

            /*Text(
                text = domainOrganizationUser.embeddings.toString(),
                style = MaterialTheme.typography.titleMedium
            )*/

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {

                ActionButton(
                    onClick = onClickLearn,
                    icon = if(domainOrganizationUser.embeddings.isEmpty()) Icons.Filled.DocumentScanner else Icons.Filled.CheckCircle,
                    contentDescription = "Register Face",
                    buttonText = "Register Face"
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

@Composable
fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    buttonText: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp).clickable { onClick() }
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
