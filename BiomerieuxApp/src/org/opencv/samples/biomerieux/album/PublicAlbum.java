package org.opencv.samples.biomerieux.album;

import java.io.File;

import android.os.Environment;

public class PublicAlbum implements Album {
	private static final String ALBUM_NAME = "biomerieux";

	@Override
	public File save_filename(String filename) {
		File folder = new File(
			Environment.getExternalStorageDirectory(),
			ALBUM_NAME
		);
        folder.mkdirs();
        File file = new File(folder, filename);
        return file;
	}

}
