package com.datavite.distrivite.presentation.employee

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainEmployee


@Composable
fun EmployeeList(
    employees: List<DomainEmployee>,
    authOrgUser: AuthOrgUser,
    modifier:Modifier = Modifier,
    onClickDeleteEmbeddings: (domainUserOrganization:DomainEmployee) -> Unit,
    onClickLearn: (domainUserOrganization:DomainEmployee) -> Unit) {
    LazyColumn(
        modifier = modifier
    ) {

        items(
            items =employees,
            key = { domainEmployee -> domainEmployee.id }) {
                domainEmployee ->
            EmployeeCard(
                domainEmployee = domainEmployee,
                authOrgUser = authOrgUser,
                onClickLearn = { onClickLearn(domainEmployee) },
                onClickDeleteEmbeddings = {
                    onClickDeleteEmbeddings(domainEmployee)
                },
                onClickTeach = {},
                onClickContribute = { })
        }

    }
}
