package ch.ralena.natibo.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.ralena.natibo.ui.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.data.LanguageData;
import ch.ralena.natibo.ui.fragment.LanguageImportFragment;
import ch.ralena.natibo.data.room.object.Language;
import ch.ralena.natibo.data.room.object.Pack;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;

/**
 * Methods for importing a .gls file into the database.
 */
public class GLSImporter {
	public static final String TAG = GLSImporter.class.getSimpleName();

	private static final int STATUS_OK = 0;
	private static final int STATUS_MISSING_GSP = 1;
	private static final int STATUS_INVALID_LANGUAGE = 2;

	private static int BUFFER_SIZE = 1024;

	private ContentResolver contentResolver;

	private PublishSubject<Integer> progressSubject;
	private PublishSubject<Integer> totalSubject;
	private PublishSubject<Integer> actionSubject;
	private PublishSubject<String> fileNameSubject;

	public GLSImporter() {
		progressSubject = PublishSubject.create();
		totalSubject = PublishSubject.create();
		actionSubject = PublishSubject.create();
		fileNameSubject = PublishSubject.create();
	}

	public PublishSubject<Integer> progressObservable() {
		return progressSubject;
	}

	public PublishSubject<Integer> totalObservable() {
		return totalSubject;
	}

	public PublishSubject<Integer> actionSubject() {
		return actionSubject;
	}

	public PublishSubject<String> fileNameSubject() {
		return fileNameSubject;
	}

	public void importPack(MainActivity activity, Uri uri) {
		// TODO: 18/03/18 1. Do some file verification to make sure all files are indeed there
		// TODO: 18/03/18 2. Verify file names/strip the 'EN - ' bit out
		actionSubject.onNext(LanguageImportFragment.ACTION_OPENING_FILE);

		Thread thread = new Thread(() -> {
			Realm realm = Realm.getDefaultInstance();
			contentResolver = activity.getContentResolver();
			String packFileName = "";

			if (uri.getScheme().equals("file")) {
				int index = uri.toString().lastIndexOf("/");
				packFileName = uri.toString().substring(index + 1);
			} else if (uri.getScheme().equals("content")) {
				Cursor cursor =
						contentResolver.query(uri, null, null, null, null);
				if (cursor != null) {
					int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					cursor.moveToFirst();
					packFileName = cursor.getString(nameIndex);
					cursor.close();
				} else {
					activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), String.format("Error opening file: %s", uri), Toast.LENGTH_SHORT).show());
					actionSubject.onNext(LanguageImportFragment.ACTION_EXIT);
					return;
				}
			}

			// pass the filename back to the fragment
			fileNameSubject.onNext(packFileName);

			if (packFileName.toLowerCase().endsWith(".gls")) {

				BufferedOutputStream bos;
				InputStream is;

				try {
					// first pass
					is = getInputStream(uri);
					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

					// check if there's anything missing in the file
					int status = countFiles(zis, realm);
					if(status > STATUS_OK) {
						int stringResId;
						switch (status) {
							case STATUS_INVALID_LANGUAGE:
								stringResId = R.string.language_not_supported;
								break;
							case STATUS_MISSING_GSP:
								stringResId = R.string.missing_gsp;
								break;
							default:
								stringResId = R.string.error_opening_file;
								break;
						}
						activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), activity.getString(stringResId), Toast.LENGTH_SHORT).show());
						actionSubject.onNext(LanguageImportFragment.ACTION_EXIT);
						return;
					}

					// second pass
					ZipEntry zipEntry;
					is = getInputStream(uri);
					zis = new ZipInputStream(new BufferedInputStream(is));

					// used to calculate length of mp3 file
					MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

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
							if (entryName.contains(".mp3")) {
								File folder = new File(activity.getFilesDir() + "/" + language + "/" + book);
								if (!folder.isDirectory()) {
									folder.mkdirs();
								}

								// set up file path
								File audioFile = new File(folder.getAbsolutePath() + "/" + number);

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
								int index = Integer.parseInt(number.replace(".mp3", ""));
								Language lang = realm.where(Language.class).equalTo("languageId", language).findFirst();

								// calculate mp3s length
								String mp3Uri = audioFile.getAbsolutePath();
								metadataRetriever.setDataSource(mp3Uri);

								int mp3Length = Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
								Pack pack = lang.getPack(book);
								pack.createSentenceOrUpdate(realm, index, null, null, null, mp3Uri, mp3Length);
							} else {
								Log.d(TAG, "Skipping: " + entryName);
							}
							progressSubject.onNext(++fileNumber);
						}
					}

					is.close();
					zis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), "Sorry, this filetype is not supported!", Toast.LENGTH_SHORT).show());
				actionSubject.onNext(LanguageImportFragment.ACTION_EXIT);
				return;
			}

		});
		thread.start();

	}

	private InputStream getInputStream(Uri uri) throws FileNotFoundException {
		if (uri.getScheme().equals("file")) {
			File file = new File(uri.getPath());
			return new FileInputStream(file);
		} else
			return contentResolver.openInputStream(uri);
	}

	/**
	 * Counts the number of files in a pack and does some basic verification to ensure files are
	 * in order.
	 **/
	private int countFiles(ZipInputStream zis, Realm realm) throws IOException {
		// update action in fragment
		actionSubject.onNext(LanguageImportFragment.ACTION_COUNTING_SENTENCES);

		final byte[] buffer = new byte[BUFFER_SIZE];
		ZipEntry zipEntry;
		int numFiles = 0;
		int bytesRead;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// calculate number of files
		String baseLanguage = "";
		String targetLanguage = "";
		String packName = "";
		boolean hasGspFile = false;
		while ((zipEntry = zis.getNextEntry()) != null) {
			// only count the sentence mp3 files
			if (zipEntry.getName().endsWith("mp3")) {
				packName = zipEntry.getName().split(" - ")[1];
				numFiles++;
				actionSubject.onNext(LanguageImportFragment.ACTION_COUNTING_SENTENCES);
				totalSubject.onNext(numFiles);
			} else if (zipEntry.getName().endsWith(".gsp")) {
				hasGspFile = true;
				actionSubject.onNext(LanguageImportFragment.ACTION_READING_SENTENCES);
				// extract base language and target language from file name
				String[] nameParts = zipEntry.getName().split("-");
				baseLanguage = nameParts[0].trim();
				if (nameParts.length > 3)
					targetLanguage = nameParts[1].trim();
				// extract contents of file into the StringBuilder
				while ((bytesRead = zis.read(buffer, 0, BUFFER_SIZE)) >= 0) {
					baos.write(buffer, 0, bytesRead);
				}
			}
		}

		if (!hasGspFile)
			return STATUS_MISSING_GSP;

		if (LanguageData.getLanguageById(baseLanguage) == null)
			return STATUS_INVALID_LANGUAGE;

		// --- begin transaction
		realm.beginTransaction();

		// create base language and pack if they don't exist
		Language base = realm.where(Language.class).equalTo("languageId", baseLanguage).findFirst();
		if (base == null) {
			base = realm.createObject(Language.class, baseLanguage);
		}

		Pack basePack = base.getPack(packName);
		if (basePack == null) {
			basePack = realm.createObject(Pack.class, UUID.randomUUID().toString());
			basePack.setBook(packName);
			base.getPacks().add(basePack);
		}

		// create target language and pack if they don't exist
		Language target;
		Pack targetPack = null;
		if (!targetLanguage.equals("")) {
			target = realm.where(Language.class).equalTo("languageId", targetLanguage).findFirst();
			if (target == null) {
				target = realm.createObject(Language.class, targetLanguage);
			}

			targetPack = target.getPack(packName);
			if (targetPack == null) {
				targetPack = realm.createObject(Pack.class, UUID.randomUUID().toString());
				targetPack.setBook(packName);
				target.getPacks().add(targetPack);
			}
		}

		realm.commitTransaction();
		// --- end transaction


		// update action in fragment
		actionSubject.onNext(LanguageImportFragment.ACTION_EXTRACTING_TEXT);

		progressSubject.onNext(0);
		totalSubject.onNext(numFiles / (targetLanguage.equals("") ? 1 : 2));

		String[] sentenceList = baos.toString("UTF-8").split("\n");
		String[] sections = sentenceList[0].split("\t");

		for (int i = 1; i < sentenceList.length; i++) {
			progressSubject.onNext(i);
			String[] sentenceParts = sentenceList[i].split("\t");
			int index = Integer.parseInt(sentenceParts[0]);

			String sentence = null;
			String translation = null;
			String ipa = null;
			String romanization = null;
			for (int j = 0; j < sentenceParts.length; j++) {
				String value = sentenceParts[j];
				switch (sections[j]) {
					case "index":
						break;
					case "sentence":
						sentence = value;
						break;
					case "translation":
						translation = value;
						break;
					case "IPA":
						ipa = value;
						break;
					case "romanization":
						romanization = value;
						break;
				}
			}

			// create or update target and base sentences
			if (!targetLanguage.equals(""))
				targetPack.createSentenceOrUpdate(realm, index, translation, ipa, romanization, null, 0);
			basePack.createSentenceOrUpdate(realm, index, sentence, ipa, romanization, null, 0);
		}

		actionSubject.onNext(LanguageImportFragment.ACTION_EXTRACTING_AUDIO);
		progressSubject.onNext(0);
		totalSubject.onNext(numFiles);

		return STATUS_OK;
	}
}
