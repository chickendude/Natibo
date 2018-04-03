package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class LangPack extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	RealmList<Sentence> sentences;
}
