package ch.ralena.natibo.di

import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.BINARY)
annotation class AppScope

@Scope
@Retention(AnnotationRetention.BINARY)
annotation class WorkerScope
