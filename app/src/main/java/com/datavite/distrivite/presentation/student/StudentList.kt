package com.datavite.distrivite.presentation.student

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainStudent


@Composable
fun StudentList(
    domainStudents: List<DomainStudent>,
    authOrgUser: AuthOrgUser,
    modifier:Modifier = Modifier,
    onClickDeleteEmbeddings: (domainStudent:DomainStudent) -> Unit,
    onClickLearn: (domainStudent:DomainStudent) -> Unit) {
    LazyColumn(
        modifier = modifier
    ) {

        items(
            items =domainStudents,
            key = { domainStudent -> domainStudent.id }) {
                domainStudent ->
            StudentCard(
                domainStudent = domainStudent,
                authOrgUser = authOrgUser,
                onClickLearn = { onClickLearn(domainStudent) },
                onClickDeleteEmbeddings = {
                    onClickDeleteEmbeddings(domainStudent)
                },
                onClickTeach = {},
                onClickContribute = { })
        }

    }
}
