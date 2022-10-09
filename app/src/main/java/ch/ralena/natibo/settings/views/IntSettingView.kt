package ch.ralena.natibo.settings.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import ch.ralena.natibo.settings.types.IntSetting

@Composable
fun IntSettingView(setting: IntSetting, @StringRes labelResId: Int) {
	var value by remember { mutableStateOf<String?>(setting.get().toString()) }
	Row(modifier = Modifier.padding(vertical = 4.dp)) {
		Column(modifier = Modifier.fillMaxWidth(0.8f)) {
			Text(
				text = stringResource(id = setting.nameId),
				fontSize = 15.sp
			)
			Text(
				text = stringResource(id = setting.descriptionId),
				fontSize = 12.sp
			)
		}
		Spacer(modifier = Modifier.weight(1f))
		TextField(
			value = value ?: "",
			onValueChange = { newString ->
				val digits = newString.filter { it.isDigit() }
				value = digits
				if (digits.isNotBlank()) {
					setting.set(digits.toInt())
				}
			},
			label = { Text(text = stringResource(id = labelResId)) },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
		)
	}
}
