package ch.ralena.natibo.data;

import java.util.ArrayList;
import java.util.List;

import ch.ralena.natibo.R;

public class LanguageData {
	public static List<LanguageType> languages;

	static {
		languages = getLanguages();
	}

	public static LanguageType getLanguageById(String id) {
		for (LanguageType languageType : languages) {
			if (languageType.id.equals(id))
				return languageType;
		}
		return null;
	}


	public static List<LanguageType> getLanguages() {
		List<LanguageType> languages = new ArrayList<>();
		languages.add(new LanguageType("AR","Arabic (Standard)", R.drawable.flag_arabic_msa));
		languages.add(new LanguageType("ARE","Arabic (Egyptian)", R.drawable.flag_arabic_egypt));
		languages.add(new LanguageType("","Armenian", R.drawable.flag_armenian));
		languages.add(new LanguageType("EU","Basque", R.drawable.flag_basque));
		languages.add(new LanguageType("","Belarusian", R.drawable.flag_belarusian));
		languages.add(new LanguageType("CA","Catalan", R.drawable.flag_catalan));
		languages.add(new LanguageType("YUE","Chinese (Cantonese)", R.drawable.flag_chinese_hongkong));
		languages.add(new LanguageType("","Chinese (Hakka)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("ZS","Chinese (Mandarin, China)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("ZT","Chinese (Mandarin, Taiwan)", R.drawable.flag_chinese_taiwan));
		languages.add(new LanguageType("SHA","Chinese (Shanghainese)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("MNN","Chinese (Taiwanese Hokkien)", R.drawable.flag_chinese_taiwan));
		languages.add(new LanguageType("SHA","Chinese (Shanghainese)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("","Chinese (Wenzhounese)", R.drawable.flag_chinese_china));
		languages.add(new LanguageType("","Czech", R.drawable.flag_czech));
		languages.add(new LanguageType("NL","Dutch", R.drawable.flag_dutch));
		languages.add(new LanguageType("EN", "English (US)", R.drawable.flag_english_us));
		languages.add(new LanguageType("","Estonian", R.drawable.flag_estonian));
		languages.add(new LanguageType("","Finnish", R.drawable.flag_finnish));
		languages.add(new LanguageType("FR","French", R.drawable.flag_french));
		languages.add(new LanguageType("DE","German", R.drawable.flag_german));
		languages.add(new LanguageType("EL","Greek", R.drawable.flag_greek));
		languages.add(new LanguageType("HI","Hindi", R.drawable.flag_indian));
		languages.add(new LanguageType("HU","Hungarian", R.drawable.flag_hungarian));
		languages.add(new LanguageType("","Icelandic", R.drawable.flag_icelandic));
		languages.add(new LanguageType("","Indonesian", R.drawable.flag_indonesian));
		languages.add(new LanguageType("IT","Italian", R.drawable.flag_italian));
		languages.add(new LanguageType("JA","Japanese", R.drawable.flag_japanese));
		languages.add(new LanguageType("KR","Korean", R.drawable.flag_korean));
		languages.add(new LanguageType("","Latvian", R.drawable.flag_latvian));
		languages.add(new LanguageType("","Lithuanian", R.drawable.flag_lithuanian));
		languages.add(new LanguageType("MN","Mongolian", R.drawable.flag_mongolian));
		languages.add(new LanguageType("","Polish", R.drawable.flag_polish));
		languages.add(new LanguageType("PB","Portuguese (Brazil)", R.drawable.flag_portuguese_brazil));
		languages.add(new LanguageType("RU","Russian", R.drawable.flag_russian));
		languages.add(new LanguageType("","Serbian", R.drawable.flag_serbian));
		languages.add(new LanguageType("","Slovak", R.drawable.flag_slovak));
		languages.add(new LanguageType("ESM","Spanish (Mexico)", R.drawable.flag_spanish_mexico));
		languages.add(new LanguageType("ES","Spanish (Spain)", R.drawable.flag_spanish_spain));
		languages.add(new LanguageType("SW","Swahili", R.drawable.flag_none));
		languages.add(new LanguageType("SV","Swedish", R.drawable.flag_swedish));
		languages.add(new LanguageType("TGL","Tagalog", R.drawable.flag_tagalog));
		languages.add(new LanguageType("TH","Thai", R.drawable.flag_thai));
		languages.add(new LanguageType("TR","Turkish", R.drawable.flag_turkish));
		languages.add(new LanguageType("UKR","Ukrainian", R.drawable.flag_ukrainian));
		languages.add(new LanguageType("VNN","Vietnamese (Northern)", R.drawable.flag_vietnamese_north));

		return languages;
	}
}
