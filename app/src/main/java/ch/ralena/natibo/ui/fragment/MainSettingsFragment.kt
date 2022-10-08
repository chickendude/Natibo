package ch.ralena.natibo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import ch.ralena.natibo.settings.MainSettings
import ch.ralena.natibo.settings.types.BooleanSetting
import ch.ralena.natibo.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainSettingsFragment : Fragment() {
	@Inject
	lateinit var settings: MainSettings

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext()).apply {
			setContent {
				Column(modifier = Modifier.padding(8.dp)) {
					Text(
						text = "Course Settings",
						fontSize = 20.sp,
						modifier = Modifier
							.padding(5.dp)
							.fillMaxWidth(),
						textAlign = TextAlign.Center
					)
					BooleanSetting(settings.pauseOnOpen)
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		(activity as MainActivity?)!!.setMenuToSettings()
	}

	companion object {
		val TAG = MainSettingsFragment::class.java.simpleName
	}
}

@Composable
fun BooleanSetting(setting: BooleanSetting) {
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
