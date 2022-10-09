package ch.ralena.natibo.settings.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.ralena.natibo.settings.types.BooleanSetting

@Composable
fun BooleanSettingView(setting: BooleanSetting) {
	val checkedState = remember { mutableStateOf(setting.get()) }
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
		Switch(checked = checkedState.value,
			onCheckedChange = {
				setting.set(it)
				checkedState.value = it
			})
	}
}

