package ch.ralena.natibo.di

import android.media.MediaMetadataRetriever
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
	@Singleton
	@Provides
	fun mediaMetadataRetriever(): MediaMetadataRetriever = MediaMetadataRetriever()
}