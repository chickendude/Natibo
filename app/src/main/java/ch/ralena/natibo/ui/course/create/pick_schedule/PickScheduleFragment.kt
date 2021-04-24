package ch.ralena.natibo.ui.course.create.pick_schedule

import android.text.InputType
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import ch.ralena.natibo.R
import ch.ralena.natibo.databinding.FragmentCoursePickScheduleBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.ScheduleTextWatcher
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.SentencesPerDayTextWatcher
import javax.inject.Inject

// TODO: 13/04/18 move to course detail page
class PickScheduleFragment :
		BaseFragment<FragmentCoursePickScheduleBinding,
				PickScheduleViewModel.Listener,
				PickScheduleViewModel>(FragmentCoursePickScheduleBinding::inflate),
		PickScheduleViewModel.Listener,
		ScheduleTextWatcher.Listener,
		SentencesPerDayTextWatcher.Listener {

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var scheduleTextWatcher: ScheduleTextWatcher

	@Inject
	lateinit var sentencesPerDayTextWatcher: SentencesPerDayTextWatcher

	companion object {
		val TAG: String = PickScheduleFragment::class.java.simpleName
		const val TAG_LANGUAGE_IDS = "tag_language_ids"
	}

	override fun setupViews(view: View) {
		viewModel.registerListener(this)

		activity.enableBackButton()
		activity.title = "Create an AI Course"

		// We have a check button
		setHasOptionsMenu(true)

		viewModel.fetchLanguages(arguments?.getStringArray(TAG_LANGUAGE_IDS))

		binding.courseTitleEdit.setOnClickListener {
			binding.courseTitleEdit.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
		}

		// Custom schedule edit should start off gone
		binding.customScheduleEdit.apply {
			visibility = View.GONE
			addTextChangedListener(scheduleTextWatcher)
		}

		// Set up sentencesPerDay EditText and SeekBar
		binding.sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher)
		binding.sentencesPerDaySeek.apply {
			setOnSeekBarChangeListener(seekBarChangeListener)
			progress = 9
		}


		// set up radio listeners
		binding.reviewScheduleRadioGroup.apply {
			check(binding.fourDayRadio.id)
			setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
				binding.customScheduleEdit.apply {
					if (checkedId == binding.customDayRadio.id) {
						visibility = View.VISIBLE
						requestFocus()
					} else
						visibility = View.GONE
				}
			}
		}
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onStart() {
		super.onStart()
		viewModel.registerListener(this)
		scheduleTextWatcher.registerListener(this)
		sentencesPerDayTextWatcher.registerListener(this)
	}

	override fun onStop() {
		super.onStop()
		viewModel.unregisterListener(this)
		scheduleTextWatcher.unregisterListener(this)
		sentencesPerDayTextWatcher.unregisterListener(this)
	}

	// seek bar change listener
	var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
		override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
			binding.sentencesPerDayEdit.apply {
				removeTextChangedListener(sentencesPerDayTextWatcher)
				setText((progress + 1).toString())
				setSelection(text.length)
				addTextChangedListener(sentencesPerDayTextWatcher)
			}
		}

		override fun onStartTrackingTouch(seekBar: SeekBar) {}
		override fun onStopTrackingTouch(seekBar: SeekBar) {}
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.check_toolbar, menu)
		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_confirm -> {
				val checkedRadioId = binding.reviewScheduleRadioGroup.checkedRadioButtonId
				val dailyReviews = binding.root.findViewById<RadioButton>(checkedRadioId).text.toString()
				val numSentencesPerDay = binding.sentencesPerDayEdit.text.toString().toInt()
				val isChorus = binding.chorusCheckBox.isChecked
				val title = binding.courseTitleEdit.text.toString()
				viewModel.createCourse(dailyReviews, numSentencesPerDay, title, isChorus)
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun setCourseTitle(title: String) {
		binding.courseTitleEdit.setText(title)
	}

	override fun onScheduleTextChanged(pattern: String) {
		binding.customDayRadio.text = viewModel.getSchedulePatternFromString(pattern)
	}

	override fun onSentencesPerDayTextChanged(pattern: String, cursorPosition: Int) {
		sentencesPerDayTextWatcher.unregisterListener(this)
		val sentencesPerDay = viewModel.getSentencesPerDayFromString(pattern)
		binding.sentencesPerDayEdit.setText(sentencesPerDay.toString())
		binding.sentencesPerDaySeek.progress = sentencesPerDay - 1
		sentencesPerDayTextWatcher.registerListener(this)
	}
}