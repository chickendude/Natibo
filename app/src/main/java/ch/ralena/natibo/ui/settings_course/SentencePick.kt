package ch.ralena.natibo.ui.settings_course

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.ui.study.overview.Sentence
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SentencePick(course: CourseRoom, viewModel: SentencePickViewModel) {
	val event = viewModel.events.collectAsState(initial = SentencePickViewModel.Event.Loading).value
	viewModel.fetchSentences(course)
	Column {
		Text(text = "hi")

		when (event) {
			is SentencePickViewModel.Event.SentencesLoaded -> {
				SentenceList(
					sentences = event.sentences,
					nativeLanguage = event.nativeLanguage,
					targetLanguage = event.targetLanguage
				)
			}
			else -> Unit
		}
	}
}

@Composable
fun SentenceList(
	sentences: List<NatiboSentence>,
	nativeLanguage: LanguageRoom?,
	targetLanguage: LanguageRoom?
) {
	LazyColumn {
		items(sentences) {
			Sentence(it, nativeLanguage, targetLanguage)
		}
	}
}

@HiltViewModel
class SentencePickViewModel @Inject constructor(
	private val sentenceRepository: SentenceRepository,
	private val languageRepository: LanguageRepository
) :
	ViewModel() {
	val events = MutableSharedFlow<Event>()

	fun fetchSentences(course: CourseRoom) {
		viewModelScope.launch {
			val nativeSentences =
				sentenceRepository.fetchSentencesInPack(course.nativeLanguageId, course.packId)
			val targetSentences = if (course.targetLanguageId != null)
				sentenceRepository.fetchSentencesInPack(course.targetLanguageId, course.packId)
			else listOf()

			val sentences = mutableListOf<NatiboSentence>()
			for (i in nativeSentences.indices) {
				val native = nativeSentences.getOrNull(i) ?: continue
				val target = targetSentences.getOrNull(i)
				if (course.targetLanguageId != null && target == null) continue
				sentences.add(NatiboSentence(native, target))
			}

			val nativeLanguage = languageRepository.fetchLanguage(course.nativeLanguageId)
			val targetLanguage = languageRepository.fetchLanguage(course.targetLanguageId ?: -1)
			events.emit(Event.SentencesLoaded(sentences, nativeLanguage, targetLanguage))
		}
	}

	sealed class Event {
		object Loading : Event()
		data class SentencesLoaded(
			val sentences: List<NatiboSentence>,
			val nativeLanguage: LanguageRoom?,
			val targetLanguage: LanguageRoom?
		) : Event()
	}
}

//class CoursePickSentenceFragment : Fragment() {
//	private var language: Language? = null
//	private var course: Course? = null
//	private var sentences: RealmList<Sentence>? = null
//	private var curSentence: Sentence? = null
//	private var realm: Realm? = null
//	private var mediaPlayer: MediaPlayer? = null
//	private var recyclerView: RecyclerView? = null
//	private var seekBar: SeekBar? = null
//	private var seekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null
//	private var checkMenu: MenuItem? = null
//	override fun onCreateView(
//		inflater: LayoutInflater,
//		container: ViewGroup?,
//		savedInstanceState: Bundle?
//	): View? {
//		val view: View = inflater.inflate(R.layout.fragment_sentence_list, container, false)
//		setHasOptionsMenu(true)
//		realm = Realm.getDefaultInstance()
//		mediaPlayer = MediaPlayer()
//
//		// load language and pack from database
//		val courseId = requireArguments().getString(TAG_COURSE_ID, null)
//		course = realm?.where<Course>(Course::class.java)?.equalTo("id", courseId)?.findFirst()
//		language = course?.languages?.last()
//
//		// load language name
//		requireActivity().title = language!!.languageType.name
//		loadSentences()
//
//		// prepare seekbar
//		loadSeekBar(view)
//
//		// set up recyclerlist and adapter
//		loadRecyclerView(view)
//		return view
//	}
//
//	private fun loadRecyclerView(view: View) {
//		recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
//		val adapter = PickSentenceAdapter(language!!.languageId, sentences)
//		recyclerView?.adapter = adapter
//		val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
//		recyclerView?.layoutManager = layoutManager
//		adapter.asObservable()
//			.subscribe(Consumer<Sentence> { sentence: Sentence -> onSentenceClicked(sentence) })
//		recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//				super.onScrolled(recyclerView, dx, dy)
//				seekBar?.setOnSeekBarChangeListener(null)
//				seekBar?.progress =
//					(layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//				seekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
//			}
//
//			override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//				super.onScrollStateChanged(recyclerView, newState)
//			}
//		})
//	}
//
//	private fun loadSeekBar(view: View) {
//		seekBar = view.findViewById<SeekBar>(R.id.seekbar)
//		seekBar?.max = sentences?.size ?: 0
//		seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
//			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//				recyclerView?.scrollToPosition(progress)
//			}
//
//			override fun onStartTrackingTouch(seekBar: SeekBar) {}
//			override fun onStopTrackingTouch(seekBar: SeekBar) {
//				recyclerView?.scrollToPosition(seekBar.getProgress())
//			}
//		}
//		seekBar?.setOnSeekBarChangeListener(seekBarChangeListener)
//	}
//
//	private fun loadSentences() {
//		sentences = RealmList<Sentence>()
//		for (pack in course?.getPacks() ?: listOf()) {
//			// use a sentence with index of -1 to separate the books
//			val sentence = Sentence()
//			sentence.setIndex(-1)
//			sentence.setText(pack.getBook())
//			sentences?.add(sentence)
//			sentences?.addAll(pack.getSentences())
//		}
//	}
//
//	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//		inflater.inflate(R.menu.check_toolbar, menu)
//		checkMenu = menu.getItem(0)
//		checkMenu?.setVisible(false)
//		super.onCreateOptionsMenu(menu, inflater)
//	}
//
//	override fun onOptionsItemSelected(item: MenuItem): Boolean {
//		when (item.itemId) {
//			R.id.action_confirm -> setStartingSentence()
//		}
//		return super.onOptionsItemSelected(item)
//	}
//
//	private fun setStartingSentence() {
//		val curDay = course?.currentDay
//		if (curDay != null && !curDay.isCompleted()
//			&& curDay.totalReviews != curDay.getNumReviewsLeft()
//		) {
//			val inProgressDialog: AlertDialog = AlertDialog.Builder(
//				requireContext()
//			)
//				.setTitle(R.string.session_in_progress)
//				.setMessage(R.string.reset_session)
//				.setPositiveButton(
//					android.R.string.yes,
//					DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> updateStartingCourse() })
//				.setNegativeButton(
//					android.R.string.no,
//					DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> })
//				.create()
//			inProgressDialog.show()
//		} else {
//			updateStartingCourse()
//		}
//	}
//
//	private fun updateStartingCourse() {
//		course?.setStartingSentenceForAllSchedules(realm, curSentence)
//		var book = ""
//		for (pack in course?.packs ?: listOf()) {
//			if (pack.getSentences().contains(curSentence)) {
//				book = pack.getBook()
//				break
//			}
//		}
//		Toast.makeText(
//			context,
//			String.format(
//				Locale.getDefault(),
//				getString(R.string.sentence_set),
//				book,
//				curSentence?.getIndex()
//			),
//			Toast.LENGTH_SHORT
//		).show()
//		requireActivity().onBackPressed()
//	}
//
//	override fun onResume() {
//		super.onResume()
//		(activity as MainActivity?)?.setMenuToLanguages()
//	}
//
//	private fun onSentenceClicked(sentence: Sentence) {
//		curSentence = sentence
//		checkMenu!!.isVisible = true
//		try {
//			mediaPlayer?.reset()
//			mediaPlayer?.setDataSource(sentence.getUri())
//			mediaPlayer?.prepare()
//			mediaPlayer?.start()
//		} catch (e: IOException) {
//			e.printStackTrace()
//		}
//	}
//
//	companion object {
//		private val TAG = CoursePickSentenceFragment::class.java.simpleName
//		const val TAG_COURSE_ID = "course_id"
//	}
//}