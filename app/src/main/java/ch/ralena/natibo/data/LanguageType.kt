package ch.ralena.natibo.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LanguageType(
		var id: String?,
		var name: String?,
		var drawable: Int
) : Parcelable