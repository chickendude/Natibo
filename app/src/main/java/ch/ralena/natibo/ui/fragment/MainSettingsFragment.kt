package ch.ralena.natibo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
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
import ch.ralena.natibo.R
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.utils.StorageManager
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
					BooleanSetting(settings.pauseOnClose)
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
				setting.value = it
				setting.save()
				checkedState.value = it
			})
	}
}

interface NatiboSetting<T> {
	val key: String
	val nameId: Int
	val descriptionId: Int
	fun get(): T
	fun save()
}

class MainSettings @Inject constructor(storageManager: StorageManager) {
	val pauseOnOpen: BooleanSetting = BooleanSetting(
		"main_pause_on_open",
		R.string.settings_pause_on_open_title,
		R.string.settings_pause_on_open_description,
		storageManager
	)
	val pauseOnClose: BooleanSetting = BooleanSetting(
		"main_setting2",
		R.string.settings_pause_on_open_title,
		R.string.settings_pause_on_open_description,
		storageManager
	)
}

data class BooleanSetting(
	override val key: String,
	@StringRes override val nameId: Int,
	@StringRes override val descriptionId: Int,
	private val storageManager: StorageManager,
	var value: Boolean = false,
) : NatiboSetting<Boolean> {
	override fun get() = storageManager.getBoolean(key, value)
	override fun save() = storageManager.putBoolean(key, value)
}

