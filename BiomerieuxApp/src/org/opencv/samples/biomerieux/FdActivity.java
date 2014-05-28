package org.opencv.samples.biomerieux;

import java.io.File;
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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressLint({ "SdCardPath", "NewApi" })
public class FdActivity extends Activity {
	public final static String EXTRA_MESSAGE = "org.opencv.samples.biomerieux.PictureActivity";
	private static final String PNG_FILE_PREFIX = "IMG_";
	private static final String PNG_FILE_SUFFIX = ".png";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private CameraPreview cameraPreview;
	private Uri fileUri;

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

	private void activateAndroidCamera() {
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    fileUri = Uri.fromFile(new File(getOutputFilename()));
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // Save the image
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_layout);
		cameraPreview = null;
		try {
			cameraPreview = new CameraPreview(this, getCameraInstance());
			Log.d("tag", "tag hola");
		} catch (java.lang.NoSuchMethodError e) {
			activateAndroidCamera();  // Activate Android Camera!!!
		}
	}
	
	/**
	 * Here we store the file url as it will be null after returning from camera
	 * app
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putParcelable("file_uri", fileUri);
	}
	 
	/*
	 * Here we restore the fileUri again
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    fileUri = savedInstanceState.getParcelable("file_uri");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	showPictureActivity(fileUri.toString().substring("file://".length()));
	        } else if (resultCode == RESULT_CANCELED) {
	            Toast.makeText(getApplicationContext(),
	                    "User cancelled image capture", Toast.LENGTH_SHORT)
	                    .show();
	        } else {
	            Toast.makeText(getApplicationContext(),
	                    "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
	                    .show();
	        }
	    }
	}
	
	private BaseLoaderCallback callbackFunction = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (cameraPreview != null && cameraPreview.mCamera == null){
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
		if (cameraPreview != null)
			cameraPreview.stopPreviewAndFreeCamera();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, callbackFunction);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (cameraPreview != null)
			cameraPreview.stopPreviewAndFreeCamera();
	}

	public void onDestroy() {
		super.onDestroy();
		if (cameraPreview != null)
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
			showPictureActivity(outputFilename);
			return outputFilename;
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "It was not possible to save the picture.", Toast.LENGTH_SHORT).show();
		}
		return "";
	}
}
