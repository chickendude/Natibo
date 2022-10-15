package ch.ralena.natibo.ui.shared_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
	LaunchedEffect(key1 = "start") {
		viewModel.fetchSessions(course.id)
	}

	val event =
		viewModel.events().collectAsState(initial = SessionListViewModel.Event.Loading).value

	val widths = listOf(50.dp, 80.dp)

	Column {
		when (event) {
			is SessionListViewModel.Event.SessionsLoaded -> {
				Header(widths = widths)
				Sessions(sessions = event.sessions, widths = widths)
			}
			else -> {
				Text(text = "Loading...")
			}
		}
	}
}

@Composable
fun Header(widths: List<Dp>) {
	Row(modifier = Modifier.fillMaxWidth()) {
		Text(modifier = Modifier.width(widths[0]), textAlign = TextAlign.Center, text = "Index")
		Text(
			modifier = Modifier.width(widths[1]),
			textAlign = TextAlign.Center,
			text = "Completed?"
		)
		Text(modifier = Modifier.weight(1f), text = "Sentences Studied")
	}
}

@Composable
fun Sessions(sessions: List<SessionRoom>, widths: List<Dp>) {
	LazyColumn {
		items(sessions) { session ->
			SessionItem(session = session, widths = widths)
		}
	}
}

@Composable
fun SessionItem(
	session: SessionRoom,
	widths: List<Dp>,
	viewModel: SessionItemViewModel = SessionItemViewModel()
) {
	Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
		Text(
			modifier = Modifier.width(widths[0]),
			textAlign = TextAlign.Center,
			text = "${session.index}"
		)
		Checkbox(
			modifier = Modifier.width(widths[1]),
			checked = session.isCompleted,
			enabled = false,
			onCheckedChange = {})
		Text(
			modifier = Modifier.weight(1f),
			text = viewModel.getIndexString(session.sentenceIndices)
		)
	}
}

class SessionItemViewModel {
	fun getIndexString(sentenceIndices: String): String {
		val indices = sentenceIndices.split(",").map { it.toInt() }
		return "${indices.min()} - ${indices.max()}"
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
