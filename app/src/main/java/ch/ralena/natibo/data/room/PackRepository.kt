package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.dao.PackDao
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PackRepository @Inject constructor(
	private val packDao: PackDao
) {
	suspend fun createPack(pack: PackRoom) = packDao.insert(pack)


	suspend fun fetchPacks(): List<PackRoom> =
		packDao.getAll()
}