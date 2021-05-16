package ch.ralena.natibo.ui.course.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.databinding.FragmentCourseDetailBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.adapter.CourseDetailAdapter
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.settings_course.CourseSettingsFragment
import ch.ralena.natibo.ui.study_session.StudySessionFragment
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmList
import java.util.*
import javax.inject.Inject

// TODO: 13/04/18 if no sentence sets have been chosen, prompt to select sentence packs.
class CourseDetailFragment : BaseFragment<
		FragmentCourseDetailBinding,
		CourseDetailViewModel.Listener,
		CourseDetailViewModel>(FragmentCourseDetailBinding::inflate) {

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var courseDetailAdapter: CourseDetailAdapter

	private lateinit var course: Course
	private lateinit var realm: Realm
	private lateinit var adapter: CourseDetailAdapter


	override fun setupViews(view: View) {
		activity.title = "Course Title"
		activity.enableBackButton()

		binding.booksRecyclerView.apply {
			adapter = courseDetailAdapter
			layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
		}

		// todo: subscribe to courseDetailAdapter
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view: View = inflater.inflate(R.layout.fragment_course_detail, container, false)

		// load schedules from database
		val id = arguments!!.getString(TAG_COURSE_ID)
		realm = Realm.getDefaultInstance()
		course = realm.where(Course::class.java).equalTo("id", id).findFirst()!!
		val targetLanguage: Language = course.getLanguages().first()!!
//		val activity = activity as MainActivity
		activity.setTitle(course.getTitle())
//		activity.enableBackButton()
//		activity.setMenuToCourses()
		loadCourseInfo(view, targetLanguage)
		val matchingPacks: RealmList<Pack> =
			targetLanguage.getMatchingPacks(course.getLanguages().last())

		// set up icons and button
		prepareDeleteCourseIcon(view, activity)
		prepareSettingsIcon(view, activity)
		prepareStartSessionButton(view)
		return view
	}

	private fun prepareStartSessionButton(view: View) {
		val startSessionButton = view.findViewById<Button>(R.id.startSessionButton)
		startSessionButton.setText(
			if (course.getCurrentDay() == null || course.getCurrentDay()
					.isCompleted()
			) R.string.start_session else R.string.continue_session
		)
		startSessionButton.setOnClickListener { v: View? ->
			// make sure we have books added before starting, otherwise it'll crash!
			if (course.getPacks().size == 0) {
				Toast.makeText(context, R.string.add_book_first, Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			// now we can load the fragment
			val fragment = StudySessionFragment()
			val bundle = Bundle()
			bundle.putString(StudySessionFragment.KEY_COURSE_ID, course.getId())
			fragment.setArguments(bundle)
			fragmentManager!!.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit()
		}
	}

	private fun loadCourseInfo(view: View, targetLanguage: Language) {
		// load total reps
		val totalRepsText: TextView = view.findViewById<TextView>(R.id.totalRepsText)
		totalRepsText.setText(String.format(Locale.US, "%d", course.getTotalReps()))

		// load total sentences seen
		val totalSentencesSeenText: TextView =
			view.findViewById<TextView>(R.id.totalSentencesSeenText)
		totalSentencesSeenText.text = String.format(Locale.US, "%d", course.numSentencesSeen)

		// load flag image
		val flagImage = view.findViewById<ImageView>(R.id.flagImageView)
		flagImage.setImageResource(course.languages.last()!!.languageType.drawable)
	}

	private fun prepareDeleteCourseIcon(view: View, activity: MainActivity) {
		val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)
		val deleteConfirmListener = View.OnClickListener { v: View? ->
			val courseId: String = course.getId()
			realm.executeTransactionAsync { r: Realm ->
				r.where(Course::class.java)
					.equalTo("id", courseId).findFirst()?.deleteFromRealm()
			}
			activity.stopSession()
			activity.loadCourseListFragment()
		}
		deleteIcon.setOnClickListener { v: View? ->
			Snackbar.make(view, R.string.confirm_delete, Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.delete, deleteConfirmListener)
				.show()
		}
	}

	private fun prepareSettingsIcon(view: View, activity: MainActivity?) {
		val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
		settingsIcon.setOnClickListener { v: View? ->
			val fragment =
				CourseSettingsFragment()

			// load fragment ID into fragment arguments
			val bundle = Bundle()
			bundle.putString(CourseSettingsFragment.KEY_ID, course.getId())
			fragment.arguments = bundle

			// load the course settings fragment
			fragmentManager!!.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit()
		}
	}

	private fun addRemovePack(pack: Pack) {
		if (course.getPacks().contains(pack)) {
			realm.executeTransaction { r: Realm? -> course.getPacks().remove(pack) }
		} else {
			realm.executeTransaction { r: Realm? -> course.getPacks().add(pack) }
		}
		adapter.notifyDataSetChanged()
	}

	companion object {
		val TAG = CourseDetailFragment::class.java.simpleName
		const val TAG_COURSE_ID = "language_id"
	}
}