package com.datavite.distrivite.presentation.teachingcourse

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.datavite.distrivite.R
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.domain.model.DomainTeachingCourse


@Composable
fun TeachingCourseList(
    teachingCourses: List<DomainTeachingCourse>,
    modifier: Modifier = Modifier,
    authOrgUser:AuthOrgUser
) {
    LazyColumn(
        modifier = modifier
    ) {

        items(
            items =teachingCourses,
            key = { domainTeachingCourse -> domainTeachingCourse.id }) {
                domainTeachingCourse -> TeachingCourseCard(
            educationTerm = domainTeachingCourse.educationTerm,
            module = domainTeachingCourse.module,
            course = domainTeachingCourse.course,
            code = domainTeachingCourse.code,
            klass = domainTeachingCourse.klass,
            credit = domainTeachingCourse.credit,
            instructor = domainTeachingCourse.instructor,
            hours = domainTeachingCourse.durationInHours.toInt(),
            progression = domainTeachingCourse.progression / domainTeachingCourse.durationInHours,
            courseImageRes = R.drawable.ic_launcher_background,
            authOrgUser=authOrgUser
                )
        }
    }
}
