package ch.ralena.natibo.ui.sentences.listener

import android.widget.SeekBar
import ch.ralena.natibo.ui.base.BaseListener
import javax.inject.Inject

class SentenceSeekBarChangeListener @Inject constructor() :
	BaseListener<SentenceSeekBarChangeListener.Listener>(),
	SeekBar.OnSeekBarChangeListener {
	interface Listener {
		fun onProgressChanged(progress: Int)
	}

	override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
		listeners.forEach { it.onProgressChanged(progress) }
	}

	override fun onStartTrackingTouch(seekBar: SeekBar) {}
	override fun onStopTrackingTouch(seekBar: SeekBar) {
		listeners.forEach { it.onProgressChanged(seekBar.progress) }
	}
}