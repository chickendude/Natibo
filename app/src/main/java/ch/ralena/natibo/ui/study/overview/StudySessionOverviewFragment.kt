package ch.ralena.natibo.ui.study.overview

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.databinding.FragmentStudySessionOverviewBinding
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

// Constants
private val LANGUAGE_WIDTH = 35.dp

@AndroidEntryPoint
class StudySessionOverviewFragment :
	BaseFragment<
			FragmentStudySessionOverviewBinding,
			StudySessionOverviewViewModel.Listener,
			StudySessionOverviewViewModel>(FragmentStudySessionOverviewBinding::inflate),
	StudySessionOverviewViewModel.Listener {
	var course: CourseRoom? = null
	private lateinit var activity: MainActivity

	override fun setupViews(view: View) {
		activity = requireActivity() as MainActivity
		activity.title = getString(R.string.session_overview)
		// load schedules from database
		viewModel.registerListener(this)
		val id = requireArguments().getLong(KEY_COURSE_ID)
		viewModel.getSentences(id)
	}

	override fun sessionLoaded(
		session: NatiboSession,
		languages: Pair<LanguageRoom?, LanguageRoom?>
	) {
		binding.composeView.setContent {
			SessionOverview(languages = languages, sentences = session.sentences)
		}

//		adapter.asObservable().subscribe(this::loadSentenceListFragment);
	}

	companion object {
		private val TAG = StudySessionOverviewFragment::class.java.simpleName
		const val KEY_COURSE_ID = "language_id"
	}
}

@Composable
fun SessionOverview(
	languages: Pair<LanguageRoom?, LanguageRoom?>,
	sentences: List<NatiboSentence>
) {
	LazyColumn {
		items(sentences) { sentence ->
			Sentence(sentence, languages.first, languages.second)
		}
	}
}

@Composable
fun Sentence(
	sentence: NatiboSentence,
	nativeLanguage: LanguageRoom?,
	targetLanguage: LanguageRoom?
) {
	Row(modifier = Modifier.padding(5.dp)) {
		Text(
			modifier = Modifier.padding(end = 8.dp),
			text = sentence.native.index.toString()
		)
		Column {
			Row {
				Text(modifier = Modifier.width(LANGUAGE_WIDTH), text = nativeLanguage?.code ?: "NL")
				Text(text = sentence.native.original)
			}
			sentence.target?.original?.let { targetSentence ->
				Row {
					Text(
						modifier = Modifier.width(LANGUAGE_WIDTH),
						text = targetLanguage?.code ?: "TL"
					)
					Text(text = targetSentence)
				}
			}
		}
	}
}