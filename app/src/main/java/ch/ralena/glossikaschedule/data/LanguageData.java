package ch.ralena.glossikaschedule.data;

import java.util.ArrayList;
import java.util.List;

import ch.ralena.glossikaschedule.R;

public class LanguageData {
	public static List<LanguageType> languages;

	static {
		languages = getLanguages();
	}

	public static List<LanguageType> getLanguages() {
		List<LanguageType> languages = new ArrayList<>();
		languages.add(new LanguageType("Arabic (Standard)", R.drawable.flag_arabic_msa));
		languages.add(new LanguageType("Arabic (Egyptian)", R.drawable.flag_arabic_egypt));
		languages.add(new LanguageType("Armenian", R.drawable.flag_armenian));
		languages.add(new LanguageType("Belarusian", R.drawable.flag_belarusian));
		languages.add(new LanguageType("Catalan", R.drawable.flag_catalan));
		languages.add(new LanguageType("Chinese (Cantonese)", R.drawable.flag_chinese_hongkong));
		languages.add(new LanguageType("Chinese (Hakka)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("Chinese (Mandarin, China)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("Chinese (Mandarin, Taiwan)", R.drawable.flag_chinese_taiwan));
		languages.add(new LanguageType("Chinese (Taiwanese Hokkien)", R.drawable.flag_chinese_taiwan));
		languages.add(new LanguageType("Chinese (Wenzhounese)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("Czech", R.drawable.flag_czech));
		languages.add(new LanguageType("Dutch", R.drawable.flag_dutch));
		languages.add(new LanguageType("Estonian", R.drawable.flag_estonian));
		languages.add(new LanguageType("Finnish", R.drawable.flag_finnish));
		languages.add(new LanguageType("French", R.drawable.flag_french));
		languages.add(new LanguageType("German", R.drawable.flag_german));
		languages.add(new LanguageType("Greek", R.drawable.flag_greek));
		languages.add(new LanguageType("Hindi", R.drawable.flag_hindi));
		languages.add(new LanguageType("Hungarian", R.drawable.flag_hungarian));
		languages.add(new LanguageType("Icelandic", R.drawable.flag_icelandic));
		languages.add(new LanguageType("Indonesian", R.drawable.flag_indonesian));
		languages.add(new LanguageType("Italian", R.drawable.flag_italian));
		languages.add(new LanguageType("Japanese", R.drawable.flag_japanese));
		languages.add(new LanguageType("Korean", R.drawable.flag_korean));
		languages.add(new LanguageType("Latvian", R.drawable.flag_latvian));
		languages.add(new LanguageType("Lithuanian", R.drawable.flag_lithuanian));
		languages.add(new LanguageType("Mongolian", R.drawable.flag_mongolian));
		languages.add(new LanguageType("Polish", R.drawable.flag_polish));
		languages.add(new LanguageType("Portuguese (Brazil)", R.drawable.flag_portuguese_brazil));
		languages.add(new LanguageType("Russian", R.drawable.flag_russian));
		languages.add(new LanguageType("Serbian", R.drawable.flag_serbian));
		languages.add(new LanguageType("Slovak", R.drawable.flag_slovak));
		languages.add(new LanguageType("Spanish (Mexico)", R.drawable.flag_spanish_mexico));
		languages.add(new LanguageType("Spanish (Spain)", R.drawable.flag_spanish_spain));
		languages.add(new LanguageType("Swahili", R.drawable.flag_none));
		languages.add(new LanguageType("Swedish", R.drawable.flag_sweden));
		languages.add(new LanguageType("Tagalog", R.drawable.flag_tagalog));
		languages.add(new LanguageType("Thai", R.drawable.flag_thai));
		languages.add(new LanguageType("Turkish", R.drawable.flag_turkish));
		languages.add(new LanguageType("Ukrainian", R.drawable.flag_ukrainian));
		languages.add(new LanguageType("Vietnamese (Northern)", R.drawable.flag_vietnamese_north));

		return languages;
	}
}
