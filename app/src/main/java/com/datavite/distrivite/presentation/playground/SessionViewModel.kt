package com.datavite.distrivite.presentation.playground

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SessionViewModel : ViewModel() {
    private val _instructors = MutableLiveData<List<Instructor>>(listOf(
        Instructor(1, "John Doe"),
        Instructor(2, "Jane Smith"),
        Instructor(3, "Albert Johnson")
    ))
    val instructors: LiveData<List<Instructor>> = _instructors

    private val _periods = MutableLiveData<List<Period>>(listOf(
        Period(1, "09:00 - 10:00"),
        Period(2, "10:00 - 11:00"),
        Period(3, "11:00 - 12:00")
    ))
    val periods: LiveData<List<Period>> = _periods

    private val _courses = MutableLiveData<List<Course>>(listOf(
        Course(1, "Mathematics"),
        Course(2, "Physics"),
        Course(3, "Chemistry")
    ))

    val courses: LiveData<List<Course>> = _courses

    private val _filteredInstructors = MutableLiveData<List<Instructor>>()
    val filteredInstructors: LiveData<List<Instructor>> = _filteredInstructors

    private val _filteredPeriods = MutableLiveData<List<Period>>()
    val filteredPeriods: LiveData<List<Period>> = _filteredPeriods

    private val _filteredCourses = MutableLiveData<List<Course>>()
    val filteredCourses: LiveData<List<Course>> = _filteredCourses

    fun filterInstructors(query: String) {
        _filteredInstructors.value = _instructors.value?.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun filterPeriods(query: String) {
        _filteredPeriods.value = _periods.value?.filter {
            it.time.contains(query, ignoreCase = true)
        }
    }

    fun filterCourses(query: String) {
        _filteredCourses.value = _courses.value?.filter {
            it.title.contains(query, ignoreCase = true)
        }
    }

    fun insertSession(session: Session) {
        // Save session to repository or database
    }
}
