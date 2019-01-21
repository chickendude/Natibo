package ch.ralena.natibo.object;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Sentence extends RealmObject {
	@PrimaryKey
	@Index
	private String id = UUID.randomUUID().toString();

	private int index;

	private String text;           // main text format
	private String alternate;      // some languages (eg. Japanese) have a second text format
	private String romanization;
	private String ipa;
	private String uri;            // mp3 location
	private int timeInMillis;            // timeInMillis of mp3 in ms

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAlternate() {
		return alternate;
	}

	public void setAlternate(String alternate) {
		this.alternate = alternate;
	}

	public String getRomanization() {
		return romanization;
	}

	public void setRomanization(String romanization) {
		this.romanization = romanization;
	}

	public String getIpa() {
		return ipa;
	}

	public void setIpa(String ipa) {
		this.ipa = ipa;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getTimeInMillis() {
		return timeInMillis;
	}

	public void setTimeInMillis(int timeInMillis) {
		this.timeInMillis = timeInMillis;
	}
}
