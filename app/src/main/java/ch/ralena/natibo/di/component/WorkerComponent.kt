package ch.ralena.natibo.di.component

import ch.ralena.natibo.di.WorkerScope
import ch.ralena.natibo.di.module.WorkerModule
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import ch.ralena.natibo.ui.study.insession.worker.StudySessionWorker
import dagger.Subcomponent

@WorkerScope
@Subcomponent(modules = [WorkerModule::class])
interface WorkerComponent {
	fun inject(worker: PackImporterWorker)
	fun inject(worker: StudySessionWorker)
}