package com.datavite.distrivite.presentation.instructorContract

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainInstructorContract


@Composable
fun InstructorContractList(
    domainInstructorContracts: List<DomainInstructorContract>,
    authOrgUser: AuthOrgUser,
    modifier:Modifier = Modifier,
    onClickDeleteEmbeddings: (domainInstructorContract:DomainInstructorContract) -> Unit,
    onClickRegister: (domainInstructorContract:DomainInstructorContract) -> Unit,
    onClickReport: (domainInstructorContract:DomainInstructorContract) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {

        items(
            items =domainInstructorContracts,
            key = { domainInstructorContract -> domainInstructorContract.id }) {
                domainInstructorContract ->
            InstructorContractCard(
                domainInstructorContract = domainInstructorContract,
                authOrgUser = authOrgUser,
                onClickRegister = { onClickRegister(domainInstructorContract) },
                onClickDeleteEmbeddings = {
                    onClickDeleteEmbeddings(domainInstructorContract)
                },
                onClickReport = { onClickReport(domainInstructorContract) },
                onClickContribute = { })
        }

    }
}
