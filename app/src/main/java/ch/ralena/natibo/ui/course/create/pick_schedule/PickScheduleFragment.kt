package ch.ralena.natibo.ui.course.create.pick_schedule

import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.ScheduleTextWatcher
import ch.ralena.natibo.utils.Utils
import io.realm.Realm
import javax.inject.Inject
import kotlin.collections.ArrayList

// TODO: 13/04/18 move to course detail page
class PickScheduleFragment :
		BaseFragment<PickScheduleViewModel.Listener, PickScheduleViewModel>(),
		PickScheduleViewModel.Listener,
		ScheduleTextWatcher.Listener {

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var scheduleTextWatcher: ScheduleTextWatcher


	private lateinit var realm: Realm
	private var languages: List<Language>? = null

	// Views
	lateinit var languageNamesLabel: EditText
	lateinit var sentencesPerDayEdit: EditText
	lateinit var sentencesPerDaySeek: SeekBar
	lateinit var customScheduleEdit: EditText
	lateinit var reviewScheduleRadioGroup: RadioGroup
	lateinit var fourDayRadio: RadioButton
	lateinit var fiveDayRadio: RadioButton
	lateinit var customDayRadio: RadioButton
	lateinit var chorusCheckBox: CheckBox

	companion object {
		val TAG: String = PickScheduleFragment::class.java.simpleName
		const val TAG_LANGUAGE_IDS = "tag_language_ids"
	}

	override fun provideLayoutId() = R.layout.fragment_course_pick_schedule

	override fun setupViews(view: View) {
		// switch to back button
		activity.enableBackButton()
		activity.title = "Create an AI Course"

		// we have a check button
		setHasOptionsMenu(true)
		realm = Realm.getDefaultInstance()

		// load language ids
		arguments?.run {
			getStringArray(TAG_LANGUAGE_IDS)?.let {
				viewModel.saveLanguageIds(it)
			}
		}

		// load views
		languageNamesLabel = view.findViewById(R.id.languageNamesLabel)
		sentencesPerDayEdit = view.findViewById(R.id.sentencesPerDayEdit)
		sentencesPerDaySeek = view.findViewById(R.id.sentencesPerDaySeek)
		customScheduleEdit = view.findViewById(R.id.customScheduleEdit)
		reviewScheduleRadioGroup = view.findViewById(R.id.reviewScheduleRadioGroup)
		fourDayRadio = view.findViewById(R.id.fourDayRadio)
		fiveDayRadio = view.findViewById(R.id.fiveDayRadio)
		customDayRadio = view.findViewById(R.id.customDayRadio)
		chorusCheckBox = view.findViewById(R.id.chorusCheckBox)

		languageNamesLabel.setOnClickListener { languageNamesLabel.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS }

		// custom schedule edit should start off gone
		customScheduleEdit.visibility = View.GONE
		customScheduleEdit.addTextChangedListener(scheduleTextWatcher)

		// set up sentencesPerDay EditText and SeekBar
		sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher)
		sentencesPerDaySeek.setOnSeekBarChangeListener(seekBarChangeListener)
		sentencesPerDaySeek.progress = 9

		// set up radio listeners
		reviewScheduleRadioGroup.check(R.id.fourDayRadio)
		reviewScheduleRadioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
			if (checkedId == R.id.customDayRadio) {
				customScheduleEdit.visibility = View.VISIBLE
				customScheduleEdit.requestFocus()
			}
			else customScheduleEdit.visibility = View.GONE
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
	}

	override fun onStop() {
		super.onStop()
		viewModel.unregisterListener(this)
		scheduleTextWatcher.unregisterListener(this)
	}

	// text watchers
	var sentencesPerDayTextWatcher: TextWatcher = object : TextWatcher {
		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
			if (Utils.isNumeric(s.toString())) {
				val number = s.toString().toInt()
				if (number <= 100) {
					sentencesPerDaySeek.progress = number - 1
					sentencesPerDayEdit.setSelection(start + count)
				} else {
					sentencesPerDayEdit.setText(100.toString())
				}
			}
		}

		override fun afterTextChanged(s: Editable) {}
	}

	// seek bar change listener
	var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
		override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
			sentencesPerDayEdit.removeTextChangedListener(sentencesPerDayTextWatcher)
			sentencesPerDayEdit.setText((progress + 1).toString())
			sentencesPerDayEdit.setSelection(sentencesPerDayEdit.text.length)
			sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher)
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
				createCourse()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}

	private fun createCourse() {
		val checkedRadioId = reviewScheduleRadioGroup.checkedRadioButtonId
		val checkedButton = getActivity()!!.findViewById<RadioButton>(checkedRadioId)
		val dailyReviews = checkedButton.text.toString().split(" / ").toTypedArray()
		val numSentencesPerDay = sentencesPerDayEdit.text.toString().toInt()

		// "base-target-target" if chorus enabled, otherwise "base-target"
		val order = StringBuilder()
		for (i in languages!!.indices) {
			if ((i > 0 || languages!!.size == 1) && chorusCheckBox.isChecked) order.append(i)
			order.append(i)
		}

		viewModel.createCourse(order.toString(), numSentencesPerDay, dailyReviews, languageNamesLabel.text.toString(), languages!!)
	}

	override fun onLanguagesLoaded(languages: List<Language>) {
		this.languages = languages
		// display languages in the course
		val languageNames = ArrayList<String>()
		for (language in languages) {
			languageNames.add(language.longName)
		}
		languageNamesLabel.setText(TextUtils.join(" → ", languageNames))
	}

	override fun onCourseCreated(course: Course) {
		activity.loadCourseListFragment(course.id)
	}

	override fun onScheduleTextChanged(pattern: String) {
		customDayRadio.text = viewModel.getSchedulePatternFromString(pattern)
	}
}