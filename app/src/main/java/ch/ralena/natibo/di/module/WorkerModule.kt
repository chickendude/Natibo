package ch.ralena.natibo.di.module

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.work.CoroutineWorker
import ch.ralena.natibo.di.ActivityScope
import ch.ralena.natibo.di.WorkerScope
import dagger.Module
import dagger.Provides

@Module
class WorkerModule(private val worker: CoroutineWorker) {
	@ActivityScope
	@Provides
	fun worker(): CoroutineWorker = worker

	@WorkerScope
	@Provides
	fun appContext(): Context = worker.applicationContext

	@WorkerScope
	@Provides
	fun mediaMetadataRetriever(): MediaMetadataRetriever = MediaMetadataRetriever()
}