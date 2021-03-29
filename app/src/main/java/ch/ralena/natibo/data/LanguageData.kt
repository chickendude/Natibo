package ch.ralena.natibo.data

import ch.ralena.natibo.R
import java.util.*

object LanguageData {
	@JvmField
	var languages: List<LanguageType> = buildLanguages()

	@JvmStatic
	fun getLanguageById(id: String): LanguageType? {
		return languages.find { it.id == id }
	}

	fun buildLanguages(): List<LanguageType> {
		val languages: MutableList<LanguageType> = ArrayList()
		languages.add(LanguageType("AR", "Arabic (Standard)", R.drawable.flag_arabic_msa))
		languages.add(LanguageType("ARE", "Arabic (Egyptian)", R.drawable.flag_arabic_egypt))
		languages.add(LanguageType("", "Armenian", R.drawable.flag_armenian))
		languages.add(LanguageType("EU", "Basque", R.drawable.flag_basque))
		languages.add(LanguageType("", "Belarusian", R.drawable.flag_belarusian))
		languages.add(LanguageType("CA", "Catalan", R.drawable.flag_catalan))
		languages.add(LanguageType("YUE", "Chinese (Cantonese)", R.drawable.flag_chinese_hongkong))
		languages.add(LanguageType("", "Chinese (Hakka)", R.drawable.flag_chinese_china))
		languages.add(LanguageType("ZS", "Chinese (Mandarin, China)", R.drawable.flag_chinese_china))
		languages.add(LanguageType("ZT", "Chinese (Mandarin, Taiwan)", R.drawable.flag_chinese_taiwan))
		languages.add(LanguageType("SHA", "Chinese (Shanghainese)", R.drawable.flag_chinese_china))
		languages.add(LanguageType("MNN", "Chinese (Taiwanese Hokkien)", R.drawable.flag_chinese_taiwan))
		languages.add(LanguageType("SHA", "Chinese (Shanghainese)", R.drawable.flag_chinese_china))
		languages.add(LanguageType("", "Chinese (Wenzhounese)", R.drawable.flag_chinese_china))
		languages.add(LanguageType("", "Czech", R.drawable.flag_czech))
		languages.add(LanguageType("NL", "Dutch", R.drawable.flag_dutch))
		languages.add(LanguageType("EN", "English (US)", R.drawable.flag_english_us))
		languages.add(LanguageType("EST", "Estonian", R.drawable.flag_estonian))
		languages.add(LanguageType("", "Finnish", R.drawable.flag_finnish))
		languages.add(LanguageType("FR", "French", R.drawable.flag_french))
		languages.add(LanguageType("DE", "German", R.drawable.flag_german))
		languages.add(LanguageType("EL", "Greek", R.drawable.flag_greek))
		languages.add(LanguageType("HI", "Hindi", R.drawable.flag_indian))
		languages.add(LanguageType("HU", "Hungarian", R.drawable.flag_hungarian))
		languages.add(LanguageType("", "Icelandic", R.drawable.flag_icelandic))
		languages.add(LanguageType("", "Indonesian", R.drawable.flag_indonesian))
		languages.add(LanguageType("IT", "Italian", R.drawable.flag_italian))
		languages.add(LanguageType("JA", "Japanese", R.drawable.flag_japanese))
		languages.add(LanguageType("KR", "Korean", R.drawable.flag_korean))
		languages.add(LanguageType("", "Latvian", R.drawable.flag_latvian))
		languages.add(LanguageType("", "Lithuanian", R.drawable.flag_lithuanian))
		languages.add(LanguageType("MN", "Mongolian", R.drawable.flag_mongolian))
		languages.add(LanguageType("", "Polish", R.drawable.flag_polish))
		languages.add(LanguageType("PB", "Portuguese (Brazil)", R.drawable.flag_portuguese_brazil))
		languages.add(LanguageType("RU", "Russian", R.drawable.flag_russian))
		languages.add(LanguageType("", "Serbian", R.drawable.flag_serbian))
		languages.add(LanguageType("", "Slovak", R.drawable.flag_slovak))
		languages.add(LanguageType("ESM", "Spanish (Mexico)", R.drawable.flag_spanish_mexico))
		languages.add(LanguageType("ES", "Spanish (Spain)", R.drawable.flag_spanish_spain))
		languages.add(LanguageType("SW", "Swahili", R.drawable.flag_none))
		languages.add(LanguageType("SV", "Swedish", R.drawable.flag_swedish))
		languages.add(LanguageType("TGL", "Tagalog", R.drawable.flag_tagalog))
		languages.add(LanguageType("TH", "Thai", R.drawable.flag_thai))
		languages.add(LanguageType("TR", "Turkish", R.drawable.flag_turkish))
		languages.add(LanguageType("UKR", "Ukrainian", R.drawable.flag_ukrainian))
		languages.add(LanguageType("VNN", "Vietnamese (Northern)", R.drawable.flag_vietnamese_north))
		return languages
	}
}