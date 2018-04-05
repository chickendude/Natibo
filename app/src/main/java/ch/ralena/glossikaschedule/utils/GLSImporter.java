package ch.ralena.glossikaschedule.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.ralena.glossikaschedule.object.Language;
import ch.ralena.glossikaschedule.object.Pack;
import ch.ralena.glossikaschedule.object.Sentence;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Methods for importing a .gsl file into the database.
 */
public class GLSImporter {
	public static final String TAG = GLSImporter.class.getSimpleName();

	private static int BUFFER_SIZE = 1024;
	private static List<String> ACCEPTED_LANGUAGES = Arrays.asList("EN", "ES", "ZS");

	PublishSubject<Integer> progressSubject;
	PublishSubject<Integer> totalSentencesSubject;

	public GLSImporter() {
		progressSubject = PublishSubject.create();
		totalSentencesSubject = PublishSubject.create();
	}

	public PublishSubject<Integer> progressObservable() {
		return progressSubject;
	}

	public PublishSubject<Integer> totalObservable() {
		return totalSentencesSubject;
	}

	public void importPack(Context ctx, Uri uri) {
		// TODO: 18/03/18 1. Do some file verification to make sure all files are indeed there
		// TODO: 18/03/18 2. Add into database
		// TODO: 18/03/18 3. Verify file names/strip the 'EN - ' bit out
		// TODO: 18/03/18 4. Create fragment where you can view all of your audio files

		Thread thread = new Thread(() -> {
			Realm realm = Realm.getDefaultInstance();
			ContentResolver contentResolver = ctx.getContentResolver();
			String packFileName = "";

			Cursor cursor =
					contentResolver.query(uri, null, null, null, null);
			if (cursor != null) {
				int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				cursor.moveToFirst();
				packFileName = cursor.getString(nameIndex);
				cursor.close();
			}

			if (packFileName.toLowerCase().endsWith(".gls")) {

				BufferedOutputStream bos;
				InputStream is;

				try {
					// first pass
					is = contentResolver.openInputStream(uri);
					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

					countFiles(zis, realm);

					// second pass
					ZipEntry zipEntry;
					is = contentResolver.openInputStream(uri);
					zis = new ZipInputStream(new BufferedInputStream(is));

					// loop through files in the .gls zip
					int fileNumber = 0;
					while ((zipEntry = zis.getNextEntry()) != null) {
						String entryName = zipEntry.getName();
						if (entryName.contains(".mp3")) {
							String[] parts = entryName.split(" - ");
							String language = parts[0];
							String book = parts[1];
							String number = parts[2];

							// make sure it's one of the accepted languages
							if (ACCEPTED_LANGUAGES.contains(language) && entryName.contains(".mp3")) {
								progressSubject.onNext(++fileNumber);
								File folder = new File(ctx.getFilesDir() + "/" + language);
								if (!folder.isDirectory()) {
									folder.mkdir();
								}

								// set up file path
								File audioFile = new File(ctx.getFilesDir() + "/" + language + "/" + number);

								Log.d(TAG, audioFile.toURI().toString());

								// actually write the file
								byte buffer[] = new byte[BUFFER_SIZE];
								FileOutputStream fos = new FileOutputStream(audioFile);
								bos = new BufferedOutputStream(fos, BUFFER_SIZE);
								int count;
								while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
									bos.write(buffer, 0, count);
								}

								// flush and close the stream before moving on to the next file
								bos.flush();
								bos.close();

								// now set up database objects which we will fill in after extracting all mp3s
								realm.executeTransaction(r -> {
									// load language pack or create it if it doesn't exist
									Language lang = realm.where(Language.class).equalTo("language_id", language).findFirst();
									if (lang == null) {
										lang = realm.createObject(Language.class, language);
									}

									// create a language pack (ie. F1, F2, F3) for the language if it doesn't exist
									Pack pack = lang.getPack(book);
									if (pack == null) {
										pack = realm.createObject(Pack.class, UUID.randomUUID().toString());
										pack.setBook(book);
										lang.getPacks().add(pack);
									}

									// find index of current sentence (sentence #1-1000, #1001-2000, etc)
									int index = Integer.parseInt(number.replace(".mp3", ""));

									// load sentence with this index or create one if it doesn't exist
									RealmList<Sentence> sentences = pack.getSentences();
									Sentence sentence = pack.getSentenceWithIndex(index);
									if (sentence == null) {
										sentence = new Sentence();
										sentence.setIndex(index);
										sentences.add(sentence);
									}

									Log.d(TAG, "Added sentence no. " + index);
								});
							} else {
								Log.d(TAG, "Skipping: " + entryName);
							}
						} else if (entryName.contains(".gsp")) {
							fileNumber++;
						}
					}

					is.close();
					zis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
		thread.start();

	}

	/**
	 * Counts the number of files in a pack and does some basic verification to ensure files are
	 * in order.
	 **/
	private boolean countFiles(ZipInputStream zis, Realm realm) throws IOException {
		final byte[] buffer = new byte[BUFFER_SIZE];
		ZipEntry zipEntry;
		int numFiles = 0;
		StringBuilder builder = new StringBuilder();
		int bytesRead;

		// calculate number of files
		String baseLanguage = null;
		String targetLanguage = null;
		while ((zipEntry = zis.getNextEntry()) != null) {
			// only count the sentence mp3 files
			if (zipEntry.getName().contains("mp3")) {
				numFiles++;
				totalSentencesSubject.onNext(numFiles);
			} else if (zipEntry.getName().contains(".gsp")) {
				// extract base language and target language from file name
				String[] nameParts = zipEntry.getName().split("-");
				baseLanguage = nameParts[0].trim();
				targetLanguage = nameParts[1].trim();
				// extract contents of file into the StringBuilder
				while ((bytesRead = zis.read(buffer, 0, BUFFER_SIZE)) >= 0) {
					builder.append(new String(buffer, 0, bytesRead));
				}
			}
		}

		if(baseLanguage == null || targetLanguage == null)
			return false;

		Language base = realm.where(Language.class).equalTo("language_id", baseLanguage).findFirst();
		if (base == null) {
			base = realm.createObject(Language.class, baseLanguage);
		}

		Language target = realm.where(Language.class).equalTo("language_id", baseLanguage).findFirst();
		if (base == null) {
			target = realm.createObject(Language.class, baseLanguage);
		}

		String[] sentenceList = builder.toString().split("\n");
		String[] sections = sentenceList[0].split("\t");

		for (String line : sentenceList) {
			if (sections[0].equals("index")) {

			}
		}

		Log.d(TAG, builder.toString());
		return true;
	}
}
