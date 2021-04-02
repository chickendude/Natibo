package ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import ch.ralena.natibo.ui.base.BaseListener
import ch.ralena.natibo.utils.Utils
import javax.inject.Inject

class ScheduleTextWatcher @Inject constructor() :
		TextWatcher,
		BaseListener<ScheduleTextWatcher.Listener>() {
	interface Listener {
		fun onScheduleTextChanged(pattern: String)
	}

	override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
	override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
		var pattern = "? / ? / ?"
		if (s.isNotEmpty()) {
			val numbers = s.toString().split("[*.,/ ]").toTypedArray()
			var areAllNumbers = true
			for (number in numbers) {
				areAllNumbers = areAllNumbers && Utils.isNumeric(number)
			}
			if (areAllNumbers) {
				pattern = TextUtils.join(" / ", numbers)
			}
		}
		for (l in listeners)
			l.onScheduleTextChanged(pattern)
	}

	override fun afterTextChanged(s: Editable) {}
}