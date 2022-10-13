package ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers

import android.text.Editable
import android.text.TextWatcher
import ch.ralena.natibo.ui.base.BaseListener
import javax.inject.Inject

class SentencesPerDayTextWatcher @Inject constructor() :
		TextWatcher,
		BaseListener<SentencesPerDayTextWatcher.Listener>() {
	interface Listener {
		fun onSentencesPerDayTextChanged(pattern: String, cursorPosition: Int)
	}

	override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
	override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
		val cursorPosition = start + count
		for (l in listeners)
			l.onSentencesPerDayTextChanged(s.toString(), cursorPosition)
	}

	override fun afterTextChanged(s: Editable) {}
}