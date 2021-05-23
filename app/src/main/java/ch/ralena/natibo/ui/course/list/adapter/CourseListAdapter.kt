package ch.ralena.natibo.ui.course.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults

class CourseListAdapter(private val courses: ArrayList<CourseRoom>) :
	BaseRecyclerAdapter<CourseListAdapter.ViewHolder, CourseListAdapter.Listener>() {
	interface Listener {
		fun onCourseClicked(course: CourseRoom)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_course_list, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (position < itemCount) holder.bindView(courses[position])
	}

	override fun getItemCount() = courses.size

	fun loadCourses(courses: List<CourseRoom>) {
		this.courses.clear()
		this.courses.addAll(courses)
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val languageName = view.findViewById<TextView>(R.id.languageLabel)
		private val courseTitle = view.findViewById<TextView>(R.id.courseTitleLabel)
		private val numReps = view.findViewById<TextView>(R.id.numRepsLabel)
		private val flagImage = view.findViewById<ImageView>(R.id.flagImageView)

		private lateinit var course: CourseRoom

		init {
			view.setOnClickListener {
				listeners.forEach { it.onCourseClicked(course) }
			}
		}

		fun bindView(course: CourseRoom) {
			this.course = course
			courseTitle.text = course.title
			// TODO: Use String resource instead
//			numReps.text = String.format("%d reps", course.totalReps)
			val language = LanguageData.languages.find { it.id == course.targetLanguageCode }
			language?.let {
				languageName.text = it.name
				flagImage.setImageResource(it.drawable)
			}
		}
	}
}