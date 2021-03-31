package ch.ralena.natibo.ui.course.create

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.adapter.CourseAvailableLanguagesAdapter
import ch.ralena.natibo.ui.adapter.CourseSelectedLanguagesAdapter
import ch.ralena.natibo.ui.callback.ItemTouchHelperCallback
import ch.ralena.natibo.ui.fragment.CoursePreparationFragment
import io.realm.Realm
import java.util.*

/**
 * The first screen when creating a new course.
 *
 * You will be given a list of available languages and must select a base language and optionally
 * one or more target languages.
 */
class CoursePickLanguageFragment : Fragment(), CourseSelectedLanguagesAdapter.OnDragListener {
	var availableLanguages: ArrayList<Language>? = null
	var selectedLanguages: ArrayList<Language>? = null
	private var realm: Realm? = null
	private lateinit var availableLanguagesRecyclerView: RecyclerView
	private lateinit var selectedLanguagesRecyclerView: RecyclerView
	var availableAdapter: CourseAvailableLanguagesAdapter? = null
	var selectedAdapter: CourseSelectedLanguagesAdapter? = null
	private var itemTouchHelper: ItemTouchHelper? = null
	private lateinit var checkMenu: MenuItem
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_course_pick_language, container, false)

		// switch to back button
		val activity = activity as MainActivity?
		activity!!.enableBackButton()
		activity.title = getString(R.string.select_languages)
		setHasOptionsMenu(true)
		realm = Realm.getDefaultInstance()
		availableLanguages = Language.getLanguagesSorted(realm)
		selectedLanguages = ArrayList()

		// recycler views
		loadRecyclerViews(view)
		return view
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.check_toolbar, menu)
		checkMenu = menu.getItem(0)
		checkMenu.setVisible(false)
		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_confirm -> loadCoursePreparationFragment()
		}
		return super.onOptionsItemSelected(item)
	}

	private fun loadRecyclerViews(view: View) {
		availableLanguagesRecyclerView = view.findViewById(R.id.availableLanguagesRecyclerView)
		selectedLanguagesRecyclerView = view.findViewById(R.id.selectedLanguagesRecyclerView)
		availableAdapter = CourseAvailableLanguagesAdapter(availableLanguages)
		availableAdapter!!.asObservable().subscribe { language: Language -> availableLanguageClicked(language) }
		availableLanguagesRecyclerView.setAdapter(availableAdapter)
		availableLanguagesRecyclerView.setLayoutManager(GridLayoutManager(context, 3))
		selectedAdapter = CourseSelectedLanguagesAdapter(selectedLanguages, this)
		selectedLanguagesRecyclerView.setAdapter(selectedAdapter)
		selectedLanguagesRecyclerView.setLayoutManager(LinearLayoutManager(context))
		val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(selectedAdapter, false)
		itemTouchHelper = ItemTouchHelper(callback)
		itemTouchHelper!!.attachToRecyclerView(selectedLanguagesRecyclerView)
	}

	private fun availableLanguageClicked(language: Language) {
		if (selectedLanguages!!.contains(language)) {
			selectedLanguages!!.remove(language)
		} else {
			selectedLanguages!!.add(language)
		}
		checkMenu!!.isVisible = selectedLanguages!!.size > 0
		selectedAdapter!!.notifyDataSetChanged()
	}

	private fun loadCoursePreparationFragment() {
		val fragment = CoursePreparationFragment()

		// add language ids in a bundle
		val bundle = Bundle()
		val languageIds = ArrayList<String>()
		for (language in selectedLanguages!!) {
			languageIds.add(language.languageId)
		}
		bundle.putStringArrayList(CoursePreparationFragment.TAG_LANGUAGE_IDS, languageIds)
		fragment.arguments = bundle
		fragmentManager!!.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit()
	}

	override fun onStartDrag(holder: RecyclerView.ViewHolder) {
		itemTouchHelper!!.startDrag(holder)
	}

	companion object {
		val TAG = CoursePickLanguageFragment::class.java.simpleName
		const val TAG_COURSE_ID = "language_id"
	}
}