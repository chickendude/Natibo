package ch.ralena.natibo.data

import ch.ralena.natibo.R

object LanguageData {
	val languages: List<LanguageType> by lazy { buildLanguages() }

	@JvmStatic
	fun getLanguageById(id: String): LanguageType? = languages.find { it.id == id }

	private fun buildLanguages() = listOf(
		LanguageType("AR", "Arabic (Standard)", R.drawable.flag_arabic_msa),
		LanguageType("ARE", "Arabic (Egyptian)", R.drawable.flag_arabic_egypt),
		LanguageType("", "Armenian", R.drawable.flag_armenian),
		LanguageType("EU", "Basque", R.drawable.flag_basque),
		LanguageType("", "Belarusian", R.drawable.flag_belarusian),
		LanguageType("CA", "Catalan", R.drawable.flag_catalan),
		LanguageType("YUE", "Chinese (Cantonese)", R.drawable.flag_chinese_hongkong),
		LanguageType("", "Chinese (Hakka)", R.drawable.flag_chinese_china),
		LanguageType("ZS", "Chinese (Mandarin, China)", R.drawable.flag_chinese_china),
		LanguageType("ZT", "Chinese (Mandarin, Taiwan)", R.drawable.flag_chinese_taiwan),
		LanguageType("SHA", "Chinese (Shanghainese)", R.drawable.flag_chinese_china),
		LanguageType("MNN", "Chinese (Taiwanese Hokkien)", R.drawable.flag_chinese_taiwan),
		LanguageType("SHA", "Chinese (Shanghainese)", R.drawable.flag_chinese_china),
		LanguageType("", "Chinese (Wenzhounese)", R.drawable.flag_chinese_china),
		LanguageType("", "Czech", R.drawable.flag_czech),
		LanguageType("NL", "Dutch", R.drawable.flag_dutch),
		LanguageType("EN", "English (US)", R.drawable.flag_english_us),
		LanguageType("EST", "Estonian", R.drawable.flag_estonian),
		LanguageType("", "Finnish", R.drawable.flag_finnish),
		LanguageType("FR", "French", R.drawable.flag_french),
		LanguageType("DE", "German", R.drawable.flag_german),
		LanguageType("EL", "Greek", R.drawable.flag_greek),
		LanguageType("HI", "Hindi", R.drawable.flag_indian),
		LanguageType("HU", "Hungarian", R.drawable.flag_hungarian),
		LanguageType("HW", "Hawaiian", R.drawable.flag_hawaiian),
		LanguageType("", "Icelandic", R.drawable.flag_icelandic),
		LanguageType("", "Indonesian", R.drawable.flag_indonesian),
		LanguageType("IT", "Italian", R.drawable.flag_italian),
		LanguageType("JA", "Japanese", R.drawable.flag_japanese),
		LanguageType("KR", "Korean", R.drawable.flag_korean),
		LanguageType("", "Latvian", R.drawable.flag_latvian),
		LanguageType("", "Lithuanian", R.drawable.flag_lithuanian),
		LanguageType("MN", "Mongolian", R.drawable.flag_mongolian),
		LanguageType("", "Polish", R.drawable.flag_polish),
		LanguageType("PB", "Portuguese (Brazil)", R.drawable.flag_portuguese_brazil),
		LanguageType("RU", "Russian", R.drawable.flag_russian),
		LanguageType("", "Serbian", R.drawable.flag_serbian),
		LanguageType("", "Slovak", R.drawable.flag_slovak),
		LanguageType("ESM", "Spanish (Mexico)", R.drawable.flag_spanish_mexico),
		LanguageType("ES", "Spanish (Spain)", R.drawable.flag_spanish_spain),
		LanguageType("SW", "Swahili", R.drawable.flag_none),
		LanguageType("SV", "Swedish", R.drawable.flag_swedish),
		LanguageType("TGL", "Tagalog", R.drawable.flag_tagalog),
		LanguageType("TH", "Thai", R.drawable.flag_thai),
		LanguageType("TR", "Turkish", R.drawable.flag_turkish),
		LanguageType("UKR", "Ukrainian", R.drawable.flag_ukrainian),
		LanguageType("VNN", "Vietnamese (Northern)", R.drawable.flag_vietnamese_north)
	)
}