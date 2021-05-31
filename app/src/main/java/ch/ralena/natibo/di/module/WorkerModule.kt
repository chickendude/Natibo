package ch.ralena.natibo.di.module

import androidx.work.CoroutineWorker
import ch.ralena.natibo.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class WorkerModule(private val worker: CoroutineWorker) {
	@ActivityScope
	@Provides
	fun worker(): CoroutineWorker = worker
}