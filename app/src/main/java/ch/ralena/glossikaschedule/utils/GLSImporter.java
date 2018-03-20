package ch.ralena.glossikaschedule.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.reactivex.subjects.PublishSubject;

/**
 * Methods for importing a .gsl file into the database.
 */
public class GLSImporter {
	public static final String TAG = GLSImporter.class.getSimpleName();
	private static int BUFFER_SIZE = 1024;
	private static List<String> ACCEPTED_LANGUAGES = Arrays.asList("EN", "ZS");
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
		// TODO: 18/03/18 5. Dialog + progress bar

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				ContentResolver contentResolver = ctx.getContentResolver();
				String dbName = "";

				Cursor cursor =
						contentResolver.query(uri, null, null, null, null);
				if (cursor != null) {
					int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					cursor.moveToFirst();
					dbName = cursor.getString(nameIndex);
					cursor.close();
				}

				if (dbName.toLowerCase().endsWith(".gls")) {

					BufferedOutputStream bos;
					InputStream is;

					try {
						is = contentResolver.openInputStream(uri);

						// first pass
						ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
						ZipEntry zipEntry;
						int numFiles = 0;

						// calculate number of files
						while ((zipEntry = zis.getNextEntry()) != null) {
							numFiles++;
							totalSentencesSubject.onNext(numFiles);
						}

						// second pass
						is = contentResolver.openInputStream(uri);
						zis = new ZipInputStream(new BufferedInputStream(is));

						// loop through files in the .gls zip
						int fileNumber = 0;
						while ((zipEntry = zis.getNextEntry()) != null) {
							progressSubject.onNext(++fileNumber);
							String entryName = zipEntry.getName();
							String language = entryName.split(" - ")[0];

							// make sure it's one of the accepted languages
							if (ACCEPTED_LANGUAGES.contains(language)) {
								File folder = new File(ctx.getFilesDir() + "/" + language);
								if (!folder.isDirectory()) {
									folder.mkdir();
								}

								// set up file path
								File audioFile = new File(ctx.getFilesDir() + "/" + language + "/" + entryName);

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
							} else {
								// invalid file
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
			}
		});
		thread.start();

	}
}
