package ch.ralena.natibo

import androidx.work.Configuration

class DebugApplication : MainApplication() {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}