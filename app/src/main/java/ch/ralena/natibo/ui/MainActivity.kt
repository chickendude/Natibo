package ch.ralena.natibo.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.service.StudySessionServiceKt
import ch.ralena.natibo.ui.language.importer.LanguageImportFragment
import ch.ralena.natibo.ui.study.insession.StudySessionFragment
import ch.ralena.natibo.utils.ScreenNavigator
import ch.ralena.natibo.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainViewModel.Listener {
	companion object {
		private val TAG = MainActivity::class.java.simpleName
		private const val KEY_SERVICE_BOUND = "key_service_bound"
		private const val REQUEST_PICK_GLS = 1
		const val REQUEST_LOAD_SESSION = 2
	}

	@Inject
	lateinit var viewModel: MainViewModel

	@Inject
	lateinit var screenNavigator: ScreenNavigator

	// fields
	private lateinit var bottomNavigationView: BottomNavigationView
	private var studySessionService: StudySessionServiceKt? = null
	private var isServiceBound = false
	val sessionPublish = PublishSubject.create<StudySessionServiceKt?>()

	private val serviceConnection: ServiceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName, service: IBinder) {
			val binder = service as StudySessionServiceKt.StudyBinder
			studySessionService = binder.service
			isServiceBound = true
			// publish the service object
			sessionPublish.onNext(studySessionService!!)
		}

		override fun onServiceDisconnected(name: ComponentName) {
			isServiceBound = false
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		// set up toolbar
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)

		bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
		bottomNavigationView.setOnNavigationItemSelectedListener {
			when (it.itemId) {
				R.id.menu_course -> {
					screenNavigator.toCourseListFragment()
					true
				}
				R.id.menu_languages -> {
					screenNavigator.toLanguageListFragment()
					true
				}
				R.id.menu_settings -> {
					screenNavigator.toMainSettingsFragment()
					true
				}
				else -> false
			}
		}

		// if the device wasn't rotated, load the starting page
		if (savedInstanceState == null) screenNavigator.toCourseListFragment()
	}

	override fun onDestroy() {
		if (isServiceBound) {
			unbindService(serviceConnection)
			studySessionService!!.removeNotification()
			studySessionService!!.stopSelf()
		}
		super.onDestroy()
	}

	override fun onResume() {
		super.onResume()
		if (isServiceBound && studySessionService != null) {
			studySessionService!!.removeNotification()
		}
	}

	override fun onPause() {
		super.onPause()
		if (isServiceBound) {
			studySessionService!!.buildNotification()
		}
	}

	/**
	 * Opens a file browser to search for a language pack to import into the app.
	 */
	fun importLanguagePack() {
		screenNavigator.clearBackStack()
		val mediaIntent = Intent(Intent.ACTION_GET_CONTENT)
		mediaIntent.type = "application/*"
		startActivityForResult(mediaIntent, REQUEST_PICK_GLS)
	}

	/**
	 * Loads the CourseListFragment fragment.
	 */
	// TODO: Delete when everything is using ScreenNavigator
	@JvmOverloads
	fun loadCourseListFragment(courseId: Long? = null) {
		screenNavigator.toCourseListFragment(courseId)
	}

	@Deprecated("Deprecated in Java")
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		// TODO: Move to ScreenNavigator
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_PICK_GLS) {
				val fragment = LanguageImportFragment()
				val bundle = Bundle()
				bundle.putParcelable(LanguageImportFragment.EXTRA_URI, data!!.data)
				fragment.arguments = bundle
				supportFragmentManager.beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.commit()
			} else if (requestCode == REQUEST_LOAD_SESSION) {
				val fragment =
					StudySessionFragment()
				supportFragmentManager
					.beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.commit()
			}
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}

	// public methods

	fun setMenuToCourses() {
		bottomNavigationView.menu.findItem(R.id.menu_course).isChecked = true
	}

	fun setMenuToLanguages() {
		bottomNavigationView.menu.findItem(R.id.menu_languages).isChecked = true
	}

	fun setMenuToSettings() {
		bottomNavigationView.menu.findItem(R.id.menu_settings).isChecked = true
	}

	fun enableBackButton() {
		supportActionBar!!.setDisplayShowHomeEnabled(true)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
	}

	fun disableBackButton() {
		supportActionBar!!.setDisplayShowHomeEnabled(false)
		supportActionBar!!.setDisplayHomeAsUpEnabled(false)
	}

	fun snackBar(stringId: Int) {
		snackBar(getString(stringId))
	}

	fun snackBar(message: String?) {
		val snackbar = Snackbar.make(
			findViewById(R.id.fragmentPlaceHolder),
			message!!,
			Snackbar.LENGTH_INDEFINITE
		)
		snackbar.setAction(R.string.ok) { v: View? -> snackbar.dismiss() }
		snackbar.show()
	}

	// --- study session service methods ---
	fun startSession(course: CourseRoom) {
		// TODO: inject and/or move into ViewModel
		val storage = Utils.Storage(this)
		storage.courseId = course.id
//		storage.putDayId(course.getCurrentDay().getId())

		// if we aren't bound to the service, start it if necessary and bind to it so that we can interact with it.
		// if we are bound to it, we need to tell it to start a new session
		if (!isServiceBound) {
			val intent = Intent(this, StudySessionServiceKt::class.java)
			if (Build.VERSION.SDK_INT >= 26)
				startForegroundService(intent)
			else
				startService(intent)
			bindService(intent, serviceConnection, BIND_AUTO_CREATE)
		} else {
			val intent = Intent(StudySessionServiceKt.BROADCAST_START_SESSION)
			sendBroadcast(intent)
			sessionPublish.onNext(studySessionService!!)
		}
	}

	fun stopSession() {
		if (isServiceBound) {
			unbindService(serviceConnection)
			studySessionService!!.removeNotification()
			studySessionService!!.stopSelf()
		}
	}
}