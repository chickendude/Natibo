package ch.ralena.natibo.data.room.`object`

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PackRoom(
	val name: String,
	val languageCode: String,

	@PrimaryKey(autoGenerate = true) val id: Int = 0
)
