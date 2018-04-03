package ch.ralena.glossikaschedule.object;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Sentence extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private int index;

	private String text;	// main text format
	private String alternate;	// some languages (eg. Japanese) have a second text format
	private String romanization;
	private String ipa;
	private String uri;	// mp3 location
}
