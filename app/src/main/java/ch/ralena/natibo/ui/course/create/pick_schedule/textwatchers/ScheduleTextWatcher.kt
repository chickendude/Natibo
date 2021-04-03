package ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers

import android.text.Editable
import android.text.TextWatcher
import ch.ralena.natibo.ui.base.BaseListener
import javax.inject.Inject

class ScheduleTextWatcher @Inject constructor() :
		TextWatcher,
		BaseListener<ScheduleTextWatcher.Listener>() {
	interface Listener {
		fun onScheduleTextChanged(pattern: String)
	}

	override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
	override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
		for (l in listeners)
			l.onScheduleTextChanged(s.toString())
	}

	override fun afterTextChanged(s: Editable) {}
}