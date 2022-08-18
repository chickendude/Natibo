package ch.ralena.natibo.ui.course.create.pick_schedule

import android.text.InputType
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.databinding.FragmentCoursePickScheduleBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.adapter.PackAdapter
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.ScheduleTextWatcher
import ch.ralena.natibo.ui.course.create.pick_schedule.textwatchers.SentencesPerDayTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PickScheduleFragment :
	BaseFragment<FragmentCoursePickScheduleBinding,
			PickScheduleViewModel.Listener,
			PickScheduleViewModel>(FragmentCoursePickScheduleBinding::inflate),
	PickScheduleViewModel.Listener,
	ScheduleTextWatcher.Listener,
	SentencesPerDayTextWatcher.Listener {

	companion object {
		val TAG: String = PickScheduleFragment::class.java.simpleName
		const val TAG_NATIVE_ID = "tag_native_id"
		const val TAG_TARGET_ID = "tag_target_id"
		const val TAG_PACK_ID = "tag_pack_id"
	}

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var packAdapter: PackAdapter

	@Inject
	lateinit var scheduleTextWatcher: ScheduleTextWatcher

	@Inject
	lateinit var sentencesPerDayTextWatcher: SentencesPerDayTextWatcher

	override fun setupViews(view: View) {
		viewModel.registerListener(this)

		activity.enableBackButton()
		activity.title = "Create an AI Course"

		// We have a check button
		setHasOptionsMenu(true)

		requireArguments().apply {
			val nativeId = getLong(TAG_NATIVE_ID)
			val targetId = getLong(TAG_TARGET_ID)
			val packId = getLong(TAG_PACK_ID)
			viewModel.fetchData(nativeId, targetId, packId)
		}

		binding.apply {
			packRecyclerview.apply {
				layoutManager = LinearLayoutManager(context)
				adapter = packAdapter
			}

			courseTitleEdit.setOnClickListener {
				binding.courseTitleEdit.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
			}

			// Custom schedule edit should start off gone
			customScheduleEdit.apply {
				visibility = View.GONE
				addTextChangedListener(scheduleTextWatcher)
			}

			// Set up sentencesPerDay EditText and SeekBar
			sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher)
			sentencesPerDaySeekbar.apply {
				setOnSeekBarChangeListener(seekBarChangeListener)
				progress = 9
			}

			// set up radio listeners
			reviewScheduleRadioGroup.apply {
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
				val dailyReviews =
					binding.root.findViewById<RadioButton>(checkedRadioId).text.toString()
				val numSentencesPerDay = binding.sentencesPerDayEdit.text.toString().toInt()
				val startingSentence =
					binding.startingSentenceEdit.text.toString().toIntOrNull() ?: 1
				val chorus = binding.chorusRadiogroup.checkedRadioButtonId
				val title = binding.courseTitleEdit.text.toString()
				viewModel.createCourse(
					dailyReviews,
					numSentencesPerDay,
					startingSentence,
					title,
					chorus
				)
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
		binding.sentencesPerDaySeekbar.progress = sentencesPerDay - 1
		sentencesPerDayTextWatcher.registerListener(this)
	}
}