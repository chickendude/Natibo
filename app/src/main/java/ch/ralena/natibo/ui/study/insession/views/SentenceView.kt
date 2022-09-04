package ch.ralena.natibo.ui.study.insession.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.magnifier
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import ch.ralena.natibo.model.NatiboSentence
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Sentences(currentSentence: StateFlow<NatiboSentence?>) {
	val sentence = currentSentence.collectAsState().value ?: return
	Column(modifier = Modifier.padding(5.dp)) {
		Text(text = sentence.native.index.toString())
		Text(text = sentence.native.original, modifier = Modifier.padding(5.dp))
		Text(text = sentence.native.ipa, modifier = Modifier.padding(5.dp))
		Text(text = sentence.native.romanization, modifier = Modifier.padding(5.dp))
		sentence.target?.let {
			Divider(color = Color.Black, modifier = Modifier.fillMaxWidth())
			Text(text = it.original, modifier = Modifier.padding(5.dp))
			Text(text = it.ipa, modifier = Modifier.padding(5.dp))
			Text(text = it.romanization, modifier = Modifier.padding(5.dp))
		}
	}
}