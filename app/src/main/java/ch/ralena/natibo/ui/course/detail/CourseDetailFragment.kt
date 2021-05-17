package ch.ralena.natibo.ui.course.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.databinding.FragmentCourseDetailBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.detail.adapter.PackAdapter
import ch.ralena.natibo.ui.settings_course.CourseSettingsFragment
import ch.ralena.natibo.ui.study_session.StudySessionFragment
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmList
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

// TODO: 13/04/18 if no sentence sets have been chosen, prompt to select sentence packs.
class CourseDetailFragment
	: BaseFragment<
		FragmentCourseDetailBinding,
		CourseDetailViewModel.Listener,
		CourseDetailViewModel>(FragmentCourseDetailBinding::inflate),
	PackAdapter.Listener,
	CourseDetailViewModel.Listener {

	companion object {
		val TAG: String = CourseDetailFragment::class.java.simpleName
		const val TAG_COURSE_ID = "language_id"
	}

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var packAdapter: PackAdapter

	override fun setupViews(view: View) {
		activity.enableBackButton()

		packAdapter.registerListener(this)
		viewModel.registerListener(this)

		binding.booksRecyclerView.apply {
			adapter = packAdapter
			layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
		}

		binding.startSessionButton.setOnClickListener {
			viewModel.startSession()
		}

		val id = arguments?.getString(TAG_COURSE_ID)
		viewModel.fetchCourse(id)
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	private fun prepareStartSessionButton(view: View) {
//		val startSessionButton = view.findViewById<Button>(R.id.startSessionButton)
//		startSessionButton.setText(
//			if (course.getCurrentDay() == null || course.getCurrentDay()
//					.isCompleted()
//			) R.string.start_session else R.string.continue_session
//		)
	}

	private fun loadCourseInfo(course: Course) {
		binding.totalRepsText.text = String.format(Locale.US, "%d", course.totalReps)
		binding.totalSentencesSeenText.text =
			String.format(Locale.US, "%d", course.numSentencesSeen)
		binding.flagImageView.setImageResource(course.languages.last()!!.languageType.drawable)
	}

	private fun prepareDeleteCourseIcon(view: View, activity: MainActivity) {
//		val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)
//		val deleteConfirmListener = View.OnClickListener { v: View? ->
//			val courseId: String = course.getId()
//			realm.executeTransactionAsync { r: Realm ->
//				r.where(Course::class.java)
//					.equalTo("id", courseId).findFirst()?.deleteFromRealm()
//			}
//			activity.stopSession()
//			activity.loadCourseListFragment()
//		}
//		deleteIcon.setOnClickListener { v: View? ->
//			Snackbar.make(view, R.string.confirm_delete, Snackbar.LENGTH_INDEFINITE)
//				.setAction(R.string.delete, deleteConfirmListener)
//				.show()
//		}
	}

	private fun prepareSettingsIcon(view: View, activity: MainActivity?) {
//		val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
//		settingsIcon.setOnClickListener { v: View? ->
//			val fragment =
//				CourseSettingsFragment()
//
//			// load fragment ID into fragment arguments
//			val bundle = Bundle()
//			bundle.putString(CourseSettingsFragment.KEY_ID, course.getId())
//			fragment.arguments = bundle
//
//			// load the course settings fragment
//			fragmentManager!!.beginTransaction()
//				.replace(R.id.fragmentPlaceHolder, fragment)
//				.addToBackStack(null)
//				.commit()
//		}
	}

	override fun onBookClicked(pack: Pack) {
		val packId = pack.id
		// We have to use our own single thread context, otherwise Realm complains about access on
		// other threads. Can't wait to switch over to Room...
		val singleThreadContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
		lifecycleScope.launch(Dispatchers.Main) {
			withContext(singleThreadContext) {
				viewModel.addRemovePack(packId)
			}
			packAdapter.toggleTargetPack(pack)
		}
	}

	override fun onCourseFetched(course: Course) {
		activity.title = course.title
		val targetLanguage = course.languages.first()!!
		loadCourseInfo(course)
		val matchingPacks: List<Pack> =
			targetLanguage.getMatchingPacks(course.languages.last())
		packAdapter.loadPacks(matchingPacks)
	}

	override fun onCourseNotFound() {
		Toast.makeText(context, R.string.course_not_found, Toast.LENGTH_SHORT).show()
	}

	override fun noPacksSelected() {
		Toast.makeText(context, R.string.add_book_first, Toast.LENGTH_SHORT).show()
	}
}