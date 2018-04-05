package ch.ralena.glossikaschedule.object;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Language extends RealmObject {
	@PrimaryKey
	@Index
	private String language_id;

	private String long_name;

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

	public String getLong_name() {
		return long_name;
	}

	public void setLong_name(String long_name) {
		this.long_name = long_name;
	}
}
