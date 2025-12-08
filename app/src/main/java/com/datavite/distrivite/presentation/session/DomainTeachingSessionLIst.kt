package com.datavite.distrivite.presentation.session

import FilterOption
import TeachingSessionFilters
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainTeachingSession


@Composable
fun DomainTeachingSessionList(
    teachingSessions: List<DomainTeachingSession>,
    authOrgUser: AuthOrgUser,
    modifier:Modifier = Modifier,
    onSelectFilterOption: (selectedOption:FilterOption)-> Unit,
    currentSelectedFilterOption:FilterOption,
    onTeachingStart:(domainTeachingSession: DomainTeachingSession) -> Unit,
    onTeachingEnd:(domainTeachingSession: DomainTeachingSession) -> Unit,
    onAttend:(domainTeachingSession: DomainTeachingSession) -> Unit,
    onTeachingApprove:(domainTeachingSession: DomainTeachingSession) -> Unit,
    onClick:(domainTeachingSession: DomainTeachingSession) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {

        item {
            TeachingSessionFilters(
                onFilterSelected = {onSelectFilterOption(it)},
                selectedOption = currentSelectedFilterOption
            )
        }

        items(
            items =teachingSessions,
            key = { domainTeachingSession -> domainTeachingSession.id }) {
                domainTeachingSession ->
            DomainTeachingSessionCard(
                domainTeachingSession = domainTeachingSession,
                authOrgUser = authOrgUser,
                onTeachingStart = { onTeachingStart(domainTeachingSession) },
                onTeachingEnd = { onTeachingEnd(domainTeachingSession) },
                onAttend = { onAttend(domainTeachingSession) },
                onTeachingApprove = { onTeachingApprove(domainTeachingSession)},
                onClick = {
                    onClick(domainTeachingSession)
                }
            )
        }

    }
}
