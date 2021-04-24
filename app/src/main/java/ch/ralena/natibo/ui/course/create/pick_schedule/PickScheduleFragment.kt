package ch.ralena.natibo.ui.course.create.pick_schedule

import android.text.InputType
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.databinding.FragmentCoursePickScheduleBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.ScheduleTextWatcher
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.SentencesPerDayTextWatcher
import io.realm.Realm
import javax.inject.Inject
import kotlin.collections.ArrayList

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


	private lateinit var realm: Realm
	private var languages: List<Language>? = null

	companion object {
		val TAG: String = PickScheduleFragment::class.java.simpleName
		const val TAG_LANGUAGE_IDS = "tag_language_ids"
	}

	override fun setupViews(view: View) {
		// Enable back button
		activity.enableBackButton()
		activity.title = "Create an AI Course"

		// We have a check button
		setHasOptionsMenu(true)
		realm = Realm.getDefaultInstance()

		// Load language IDs
		arguments?.run {
			getStringArray(TAG_LANGUAGE_IDS)?.let {
				viewModel.saveLanguageIds(it)
			}
		}

		binding.languageNamesLabel.setOnClickListener {
			binding.languageNamesLabel.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
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
			check(R.id.fourDayRadio)
			setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
				binding.customScheduleEdit.apply {
					if (checkedId == R.id.customDayRadio) {
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
		viewModel.fetchLanguages()
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
	// TODO: Move to ViewModel
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
				// TODO: Move to ViewModel
				createCourse()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}

	private fun createCourse() {
		// TODO: Move to ViewModel
		// TODO: Ensure sentences per day value isn't 0
		val checkedRadioId = binding.reviewScheduleRadioGroup.checkedRadioButtonId
		val checkedButton = getActivity()!!.findViewById<RadioButton>(checkedRadioId)
		val dailyReviews = checkedButton.text.toString().split(" / ").toTypedArray()
		val numSentencesPerDay = binding.sentencesPerDayEdit.text.toString().toInt()

		// "base-target-target" if chorus enabled, otherwise "base-target"
		val order = StringBuilder()
		for (i in languages!!.indices) {
			if ((i > 0 || languages!!.size == 1) && binding.chorusCheckBox.isChecked) order.append(i)
			order.append(i)
		}

		viewModel.createCourse(order.toString(), numSentencesPerDay, dailyReviews, binding.languageNamesLabel.text.toString(), languages!!)
	}

	override fun onLanguagesLoaded(languages: List<Language>) {
		this.languages = languages
		// display languages in the course
		val languageNames = ArrayList<String>()
		for (language in languages) {
			languageNames.add(language.longName)
		}
		binding.languageNamesLabel.setText(languageNames.joinToString(" → "))
	}

	override fun onCourseCreated(course: Course) {
		// todo: screennavigator
		activity.loadCourseListFragment(course.id)
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