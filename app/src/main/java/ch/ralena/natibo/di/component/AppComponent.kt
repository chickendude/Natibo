package ch.ralena.natibo.di.component

import ch.ralena.natibo.di.AppScope
import ch.ralena.natibo.di.module.ActivityModule
import ch.ralena.natibo.di.module.AppModule
import ch.ralena.natibo.di.module.WorkerModule
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import dagger.Component

@AppScope
@Component(modules = [AppModule::class])
interface AppComponent {
	fun newActivityComponent(activityModule: ActivityModule): ActivityComponent
	fun newWorkerComponent(workerModule: WorkerModule): WorkerComponent
}