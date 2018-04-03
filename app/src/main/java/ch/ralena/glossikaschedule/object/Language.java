package ch.ralena.glossikaschedule.object;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Language extends RealmObject {
	@PrimaryKey
	@Index
	private String language_id;

	RealmList<Pack> packs;

	public String getLanguage_id() {
		return language_id;
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
}
