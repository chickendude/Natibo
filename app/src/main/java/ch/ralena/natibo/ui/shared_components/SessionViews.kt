package ch.ralena.natibo.ui.shared_components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SessionList(course: CourseRoom, viewModel: SessionListViewModel) {
	val event =
		viewModel.events().collectAsState(initial = SessionListViewModel.Event.Loading).value
	viewModel.fetchSessions(course.id)

	LazyColumn {
		when (event) {
			is SessionListViewModel.Event.SessionsLoaded -> {
				items(event.sessions) { session ->
					Row {
						Text(text = "${session.index}")
						Text(text = "${session.isCompleted}")
						Text(text = session.sentenceIndices)
					}
				}
			}
			else -> Unit
		}
	}
}

@HiltViewModel
class SessionListViewModel @Inject constructor(
	private val sessionRepository: SessionRepository
) : ViewModel() {
	private val events = MutableSharedFlow<Event>()
	fun events() = events.asSharedFlow()

	fun fetchSessions(courseId: Long) {
		viewModelScope.launch {
			val sessions = sessionRepository.fetchSessionsInCourse(courseId)
			events.emit(Event.SessionsLoaded(sessions))
		}
	}

	sealed class Event {
		data class SessionsLoaded(val sessions: List<SessionRoom>) : Event()
		object Loading : Event()
	}
}
