package ch.ralena.natibo.ui.shared_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.model.NatiboSentence

// Constants
private val LANGUAGE_WIDTH = 35.dp

@Composable
fun SentenceList(
	sentences: List<NatiboSentence>,
	nativeLanguage: LanguageRoom? = null,
	targetLanguage: LanguageRoom? = null,
	onSentenceClicked: ((NatiboSentence) -> Unit)? = null
) {
	LazyColumn {
		items(sentences) { sentence ->
			Sentence(sentence, nativeLanguage, targetLanguage, onClick = onSentenceClicked)
		}
	}
}

@Composable
fun Sentence(
	sentence: NatiboSentence,
	nativeLanguage: LanguageRoom?,
	targetLanguage: LanguageRoom?,
	onClick: ((NatiboSentence) -> Unit)? = null
) {
	val nativeStyle = TextStyle(fontSize = 15.sp)
	val targetStyle = TextStyle(fontSize = 12.sp, color = TextLight)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(5.dp)
			.clickable { onClick?.invoke(sentence) }
	) {
		Text(
			modifier = Modifier.padding(end = 8.dp),
			text = sentence.native.index.toString(),
			style = nativeStyle
		)
		Column {
			sentence.target?.original?.let { targetSentence ->
				Row {
					Text(
						modifier = Modifier.width(LANGUAGE_WIDTH),
						text = targetLanguage?.code ?: "TL",
						style = nativeStyle
					)
					Text(text = targetSentence, style = nativeStyle)
				}
			}
			Row {
				Text(
					modifier = Modifier.width(LANGUAGE_WIDTH),
					text = nativeLanguage?.code ?: "NL",
					style = targetStyle
				)
				Text(text = sentence.native.original, style = targetStyle)
			}
		}
	}
}
