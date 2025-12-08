package com.datavite.distrivite.presentation.contribute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.domain.model.DomainOrganizationUser


@Composable
fun DomainOrganizationUserList(
    organizationUsers: List<DomainOrganizationUser>,
    modifier:Modifier = Modifier,
    onClickDeleteEmbeddings: (domainUserOrganization:DomainOrganizationUser) -> Unit,
    onClickLearn: (domainUserOrganization:DomainOrganizationUser) -> Unit) {
    LazyColumn(
        modifier = modifier
    ) {

        items(
            items =organizationUsers,
            key = { domainOrganizationUser -> domainOrganizationUser.id }) {
                domainOrganizationUser ->
            DomainOrganizationUserCard(
                domainOrganizationUser=domainOrganizationUser,
                onClickLearn = { onClickLearn (domainOrganizationUser) },
                onClickDeleteEmbeddings = {
                    onClickDeleteEmbeddings(domainOrganizationUser)
                },
                onClickTeach = {},
                onClickContribute = { })
        }

        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround){
                Spacer(modifier = Modifier.fillMaxWidth().padding(16.dp))
                Text(text = "Pagination by liedji")
                Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { /*TODO*/ }) {
                        Text(text = "Preview")
                    }

                    OutlinedButton(onClick = { /*TODO*/ }) {
                        Text(text = "Next")
                    }
                }
                Spacer(modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        }
    }
}
