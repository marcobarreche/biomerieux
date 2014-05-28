package org.opencv.samples.biomerieux;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.samples.biomerieux.album.PublicAlbum;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

@SuppressLint({ "SdCardPath", "NewApi" })
public class FdActivity extends Activity {
	private static final String TAG = "OCVSample::Activity";
	public final static String EXTRA_MESSAGE = "org.opencv.samples.biomerieux.PictureActivity";
	private static final String PNG_FILE_PREFIX = "IMG_";
	private static final String PNG_FILE_SUFFIX = ".png";
	private CameraPreview cameraPreview;

	public void onBackPressed() {
		this.finish();
	}

	private static Camera getCameraInstance() {
		int cameraId = -1;
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				break;
			}
		}
		if (cameraId >= 0) {
			try {
				return Camera.open(cameraId);
			} catch (Exception e) {
				Log.e("TAG"," error: " + e.toString());
			}
		}
		return null;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Keep the screen on and brightful as long as the screen is visible by the user.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Show the camera to the user
		setContentView(R.layout.camera_layout);
		cameraPreview = new CameraPreview(this, getCameraInstance());
	}
	
	private BaseLoaderCallback callbackFunction = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (cameraPreview.mCamera == null){
				cameraPreview.setCamera(getCameraInstance());
				FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
				preview.addView(cameraPreview);
				cameraPreview.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cameraPreview.mCamera.takePicture(null, null, new PictureCallback() {
							@Override
							public void onPictureTaken(byte[] data, Camera camera) {
								takePicture(data);
							}
						});
					}
				});
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		cameraPreview.stopPreviewAndFreeCamera();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, callbackFunction);
	}

	@Override
	public void onPause() {
		super.onPause();
		cameraPreview.stopPreviewAndFreeCamera();
	}

	public void onDestroy() {
		super.onDestroy();
		cameraPreview.stopPreviewAndFreeCamera();
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static String getOutputFilename () {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String filename = PNG_FILE_PREFIX + timeStamp + PNG_FILE_SUFFIX;
		return new PublicAlbum().save_filename(filename).getAbsolutePath();
	}

	private void showPictureActivity(String imagePath) {
		Intent intent = new Intent(this, PictureActivity.class);
		intent.putExtra(EXTRA_MESSAGE, imagePath);
		startActivity(intent);
		this.finish();
	}

	private String takePicture(byte[] data) {
		String outputFilename = getOutputFilename();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(outputFilename));
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, ">> There was an unexpected error");
		} catch (IOException e) {
			Log.e(TAG, ">> There was an unexpected error");
		}

		showPictureActivity(outputFilename);
		return outputFilename;
	}
}
