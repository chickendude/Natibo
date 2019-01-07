package ch.ralena.natibo.object;

import java.util.ArrayList;
import java.util.Collections;

import ch.ralena.natibo.data.LanguageData;
import ch.ralena.natibo.data.LanguageType;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Language extends RealmObject {
	@PrimaryKey
	@Index
	private String languageId;

	RealmList<Pack> packs;

	public String getLanguageId() {
		return languageId;
	}

	public RealmList<Pack> getPacks() {
		return packs;
	}

	public Pack getPack(String book) {
		for (Pack pack : packs) {
			if (pack.getBook().equals(book)) {
				return pack;
			}
		}
		return null;
	}

	public boolean hasBook(String book) {
		boolean hasBook = false;
		for (Pack p : packs) {
			hasBook = p.getBook().equals(book) || hasBook;
		}
		return hasBook;
	}

	public String getLongName() {
		return getLanguageType().getName();
	}

	public LanguageType getLanguageType() {
		LanguageType languageType = null;
		for (LanguageType language : LanguageData.languages) {
			if (this.languageId.equals(language.getId())) {
				return language;
			}
		}
		return languageType;
	}

	public int getSentenceCount() {
		int numSentences = 0;
		for (Pack pack : packs) {
			numSentences += pack.getSentences().size();
		}
		return numSentences;
	}

	/**
	 * Looks for matching sentence packs in another language set.
	 *
	 * @param targetLanguage language to check for matching packs
	 * @return RealmList containing packs shared between both languages
	 */
	public RealmList<Pack> getMatchingPacks(Language targetLanguage) {
		RealmList<Pack> matchingPacks = new RealmList<>();
		for (Pack basePack : packs) {
			for (Pack targetPack : targetLanguage.getPacks()) {
				if (basePack.getBook().equals(targetPack.getBook())) {
					matchingPacks.add(basePack);
					break;
				}
			}
		}
		return matchingPacks;
	}

	public Pack getMatchingPack(Pack pack) {
		for (Pack p : packs) {
			if (p.getBook().equals(pack.getBook())) {
				return p;
			}
		}
		return null;
	}

	// ### STATIC HELPER METHODS ###

	public static ArrayList<Language> getLanguagesSorted(Realm realm) {
		RealmResults<Language> languages = realm.where(Language.class).findAll();
		ArrayList<Language> languagesSorted = new ArrayList<>();
		languagesSorted.addAll(languages);
		Collections.sort(languagesSorted, (lang1, lang2) -> lang1.getLongName().compareTo(lang2.getLongName()));
		return languagesSorted;
	}

}
