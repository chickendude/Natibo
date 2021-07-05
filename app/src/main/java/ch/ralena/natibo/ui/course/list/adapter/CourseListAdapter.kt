package ch.ralena.natibo.ui.course.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter

class CourseListAdapter(
	private val courses: ArrayList<CourseRoom>,
	private val languages: ArrayList<LanguageRoom>
) :
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

	fun loadData(courses: List<CourseRoom>, languages: List<LanguageRoom>) {
		this.courses.clear()
		this.courses.addAll(courses)
		this.languages.clear()
		this.languages.addAll(languages)
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val languageName = view.findViewById<TextView>(R.id.languageLabel)
		private val courseTitle = view.findViewById<TextView>(R.id.courseTitleLabel)
		private val numReps = view.findViewById<TextView>(R.id.numRepsLabel)
		private val flagImage = view.findViewById<ImageView>(R.id.flag_image)

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
			val language = languages.find { it.id == course.targetLanguageId }
			language?.let {
				languageName.text = it.name
				flagImage.setImageResource(it.flagDrawable)
			}
		}
	}
}