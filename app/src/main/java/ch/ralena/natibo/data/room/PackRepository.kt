package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.*
import ch.ralena.natibo.data.room.dao.PackDao
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PackRepository @Inject constructor(
	private val packDao: PackDao
) {
	suspend fun createPack(pack: PackRoom) = packDao.insert(pack)

	suspend fun fetchPack(id: Long) = packDao.getById(id)

	suspend fun fetchPackByName(name: String) =
		packDao.getByName(name)

	suspend fun fetchPackWithSentences(id: Long) = packDao.getWithSentencesById(id)

	suspend fun fetchPacks(): List<PackRoom> =
		packDao.getAll()
}