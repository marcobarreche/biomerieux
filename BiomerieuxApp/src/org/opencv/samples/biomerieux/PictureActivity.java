package org.opencv.samples.biomerieux;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.biomerieux.capsules.Capsules;
import org.opencv.samples.biomerieux.capsules.ColorPos;
import org.opencv.samples.biomerieux.capsules.SmartCapsules;
import org.opencv.samples.biomerieux.catalog.Catalog;
import org.opencv.samples.biomerieux.catalog.CatalogEntryWithProb;
import org.opencv.samples.biomerieux.detection.Classifier;
import org.opencv.samples.biomerieux.exception.BiomerieuxException;
import org.opencv.samples.biomerieux.utils.Sci;
import org.opencv.samples.biomerieux.utils.SuperMat;
import org.opencv.samples.biomerieux.utils.Tuple;
import org.xml.sax.SAXException;

import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends FragmentActivity{
	private static final String EXTENDED_POS_EVAL_COLOR_XML = "extended_pos_eval_color.xml";

	private static enum TouchActions {down, move};

	private static final Integer INFO_COLOR = Color.parseColor("#FFFFFF");
	private static final String TAG = "OCVSample::Activity";
	private static final Double ZOOM_FACTOR = 0.4;
	private static final int TICKNESS = 2;

	public static final String TAG_GOT_POS = "got_positions";
	public static final String TAG_EXP_POS = "expected_positions";
	public static final String TAG_GOT_EVAL = "got_evaluation";
	public static final String TAG_EXP_EVAL = "expected_evaluation";
	public static final String TAG_DETECTED = "detected";
	public static final String TAG_IMAGE = "image";

	private Map<Tuple<Integer, Integer>, Mat> chunks;
	private Catalog catalog;
	private CatalogEntryWithProb firstPrediction;
	private Classifier classifier;
	private ImageView ivBase, ivExpanded;
	private List<Boolean> gotEvaluation, expEvaluation;
	private int selectedIndex, backButtonPressed;
	private MenuItem mItemBack, mItemOk, mItemPost;
	private Rect rectExpanded;
	private SmartCapsules gotPositions, expPositions;
	private SuperMat imageBase;
	private TextView textView;
	private ColorPos colorPos;
	private TouchActions touchActions;
	private String imagePath;
	private int [] currentZoom; 

	public void onResume() {
		super.onResume();

		backButtonPressed = 0;
		initClassifier();
		initCatalogAndColorPos();
		initGotAndExpectedResults();

		if (this.gotEvaluation.size() > 0 && this.expPositions != null) {
			initChunks(imageBase);
			initEvaluation(imageBase);
		}
	}
	
	private void cleanImage() {
		for (Entry<Tuple<Integer, Integer>, Mat> element: chunks.entrySet()) {
			Tuple<Integer, Integer> tuple = element.getKey();
			imageBase.paste(element.getValue(), tuple.x - TICKNESS, tuple.y - TICKNESS);
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Keep the screen on and brightful as long as the screen is visible by the user.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Show the camera to the user
		setContentView(R.layout.smart_capsules);

		// initialized values
		chunks = new HashMap<Tuple<Integer, Integer>, Mat>();
		gotEvaluation = new ArrayList<Boolean>();
		expEvaluation = new ArrayList<Boolean>();
		gotPositions = null;
		expPositions = null;
		backButtonPressed = 0;
		selectedIndex = -1;
		rectExpanded = null;
		currentZoom = new int [] {0, 0};
		
		// XXX: Where is the photo??
		imageBase = getImageFromPhoto();

		// Initialize textView, mExpandedView and mBaseView
		textView = (TextView) findViewById(R.id.textView2);
		ivExpanded = (ImageView) findViewById(R.id.expanded_image);
		ivBase = (ImageView) findViewById(R.id.base_image);
		ivBase.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return touch (event, true);
			}
		});
		ivExpanded.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return touch (event, false);
			}
		});
	}

	private void initGotAndExpectedResults() {
		try {
	    	Tuple<SmartCapsules, List<Boolean>> tuple = SmartCapsules.api20e_reader(imageBase, classifier, colorPos);
	    	gotPositions = tuple.x;
		    gotEvaluation = tuple.y;
	    } catch (Exception e) {
			displayMessageAndBack2Video("Error. We could not identify the capsules.");
		}

		if (gotPositions != null && gotEvaluation.size() > 0) {
			expPositions = new SmartCapsules(gotPositions.getCapsules());
			expEvaluation = new ArrayList<Boolean>(gotEvaluation);
		}
	}

	@Override
	public void onBackPressed() {
		if (backButtonPressed == 0) {
			fromMat2ImageView(ivBase, imageBase);
			showIvBase(true);
			rectExpanded = null;
		} else if (backButtonPressed == 1) {
			back2VideoActivity();
		}
		backButtonPressed ++;
	}
	
	private String saveImage(){
		String image = FdActivity.getOutputFilename();
		cleanImage();
		Mat dst = new Mat();
		org.opencv.imgproc.Imgproc.cvtColor(imageBase, dst, Imgproc.COLOR_RGB2BGR);
		Highgui.imwrite(image, dst);
		return image;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mItemOk) {
			saveImage();
			displayMessageAndBack2Video("The image was saved successfully");
		} else if (item == mItemPost) {
			String image = saveImage();
			try {
				make_post(gotPositions, expPositions, gotEvaluation, expEvaluation, gotPositions, image);
				new File(image).delete();
			} catch (JSONException e) {
				new File(image).delete();
			}
		} else if (item == mItemBack) {
			back2VideoActivity();
		}
		return true;
	}
	
	private void displayMessageAndBack2Video(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		back2VideoActivity();
	}

	private void showIvBase(boolean ok) {
		ivExpanded.setVisibility(ok ? View.GONE : View.VISIBLE);
		ivBase.setVisibility(ok ? View.VISIBLE : View.VISIBLE);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		mItemBack = menu.add("Back");
		mItemOk = menu.add("Save image");
		mItemPost = menu.add("Make a HTTP Post");
		return true;
	}

	public boolean onPrepareOptionsMenu (Menu menu) {
		menu.getItem(0).setVisible(selectedIndex <= 0);
		menu.getItem(1).setVisible(selectedIndex <= 0);
		return true;
	}

	private boolean touch (MotionEvent event, Boolean ivBaseShown) {
		int [] prevZoom = new int [] {currentZoom[0], currentZoom[1]} ;
		currentZoom = new int [] {
				Float.valueOf(event.getX()).intValue(), Float.valueOf(event.getY()).intValue()
		};

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			touchActions = TouchActions.down;
			if (ivBaseShown) {
				rectExpanded = new Rect(
					Math.max(0, currentZoom[0] - Double.valueOf(ivBase.getWidth() * ZOOM_FACTOR / 2).intValue()),
					Math.max(0, currentZoom[1] - Double.valueOf(ivBase.getHeight() * ZOOM_FACTOR / 2).intValue()),
					Double.valueOf(ivBase.getWidth() * ZOOM_FACTOR).intValue(),
					Double.valueOf(ivBase.getHeight() * ZOOM_FACTOR).intValue()
				);
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP){
			if (touchActions.equals(TouchActions.down)) {
				updateEvaluation (currentZoom[0], currentZoom[1]);
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			touchActions = TouchActions.move;
			updateRectExpand(currentZoom, prevZoom);
			resizeImage(ivExpanded, ivBase, rectExpanded);
			if (ivBaseShown) {
				showIvBase(false);
			}
		}
		return true;
	}

	private void updateRectExpand(int [] currentZoom, int[] prevZoom) {
		int x = Double.valueOf((-currentZoom[0] + prevZoom[0]) * (double) rectExpanded.width / (double)ivBase.getWidth()).intValue() + rectExpanded.x;
		int y = Double.valueOf((-currentZoom[1] + prevZoom[1]) * (double) rectExpanded.height / (double)ivBase.getHeight()).intValue() + rectExpanded.y;
		rectExpanded.x = Math.max(0, Math.min(x, ivBase.getWidth() - rectExpanded.width));
		rectExpanded.y = Math.max(0, Math.min(y, ivBase.getHeight() - rectExpanded.height));
	}
	
	private void updateEvaluation (int x, int y) {
		Tuple<Integer, Integer> coord = getRealCoord(x, y);
		try {
			selectedIndex = expPositions.getCapsuleAprox(coord.x, coord.y);
		} catch (BiomerieuxException e) {
			selectedIndex = -1;
		}
		if (selectedIndex < 0) {
			selectedIndex = -1;
			return;
		}

		try {
			expPositions.changeAndPaintEvaluation(imageBase, expEvaluation, selectedIndex);
			// Display into the screen.
			fromMat2ImageView(ivBase, imageBase);
			writeInfo(catalog.searchb(expEvaluation).name);
			if (rectExpanded != null) {
				resizeImage(ivExpanded, ivBase, rectExpanded);
			}
		} catch (BiomerieuxException e) {
			Log.e(TAG, "It was not possible to change the evaluation");
		}
		selectedIndex = -1;
	}

	private void initChunks(SuperMat image) {
		for (int i = 0; i < expPositions.getCapsules().length; i++) {
			Rect rect = expPositions.getSelection(i);
			Rect crop = new Rect(
				Math.max(0, rect.x - TICKNESS), 
				Math.max(0, rect.y - TICKNESS), 
				rect.width + TICKNESS * 2, 
				rect.height + TICKNESS
			);
			chunks.put(new Tuple<Integer, Integer>(rect.x, rect.y), image.submat(crop).clone());
		}
	}
	
	/**
	 * This image paint the image base and the evaluation (+, -, +, + ...)
	 * @param image
	 */
	private void initEvaluation(SuperMat image) {
		try {
			expPositions.draw_eval(image, expEvaluation);
		} catch (BiomerieuxException e) {
			displayMessageAndBack2Video("There was an error in the evaluation.");
		}
		firstPrediction = catalog.searchb(expEvaluation);
		writeInfo(firstPrediction.name);
		fromMat2ImageView(ivBase, image);
	}

	private void make_post(Capsules got_pos, Capsules exp_pos, List<Boolean> got_eval,
			List<Boolean> exp_eval, Capsules detected, String filepath) throws JSONException {
		SendHttpRequestTask t = new SendHttpRequestTask(this);
		String[] params = new String[] {
			got_pos.createJsonString(),
			exp_pos.createJsonString(),
			Sci.createJsonEvaluation(got_eval),
			Sci.createJsonEvaluation(exp_eval),
			detected.createJsonString(),
			filepath
		};
		t.execute(params);
	}

	private void back2VideoActivity() {
		startActivity(new Intent(this, FdActivity.class));
		finish();
	}

	private File createFile (int raw, String filename) {
		InputStream is = getResources().openRawResource(raw);
		File res = new File(getFilesDir(), filename);
		try {
			FileOutputStream os = new FileOutputStream(res);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
		}
		return res;
	}

	private void initClassifier () {
		File cascade_xml = createFile(R.raw.cascade, Classifier.FILENAMEPATH);
		try {
			classifier = new Classifier(cascade_xml);
			if (classifier.getClassifier() == null) {
				displayMessageAndBack2Video("Failed to load cascade classifier " + classifier.getFilename());
			}
		} catch (IOException e1) {
			displayMessageAndBack2Video("There was an error at the Creation of the classifier. " + e1.getMessage());
		}
		cascade_xml.delete();
	}

	private void initCatalogAndColorPos () {
		File catalog_csv = createFile(R.raw.api20e, Catalog.FILENAMEPATH);
		try {
			InputStream is = new FileInputStream(catalog_csv.getAbsolutePath());
			catalog = new Catalog(is, SmartCapsules.MAX_NUM_CAPSULES);
		} catch (FileNotFoundException e) {
			Log.e(TAG, ">> The file " + Catalog.FILENAMEPATH + " was not saved temporaly.");
		} catch (IOException e) {
			Log.e(TAG, ">> We were not able to create an object Catalog.");
		} finally {
			catalog_csv.delete();
		}
		
		File color_file = createFile (R.raw.extended_pos_eval_color, EXTENDED_POS_EVAL_COLOR_XML);
		try {
			colorPos = new ColorPos(color_file);
		} catch (IOException e) {
			Log.e(TAG, ">> We were not able to create an object ColorPos.");
		} catch (ParserConfigurationException e) {
			Log.e(TAG, ">> We were not able to create an object ColorPos.");
		} catch (SAXException e) {
			Log.e(TAG, ">> We were not able to create an object ColorPos.");
		} finally {
			color_file.delete();
		}
	}

	private void writeInfo(String info) {
		textView.setText("  " + info);
		textView.setTextColor(INFO_COLOR);
	}

	private Tuple<Integer, Integer> getRealCoord (int x, int y) {
		int newX = x;
		int newY = y;
		if (rectExpanded != null) {
			newX = rectExpanded.x + Double.valueOf(
					Double.valueOf((Double.valueOf(newX) / Double.valueOf(ivBase.getWidth()))) * rectExpanded.width).intValue();
			newY = rectExpanded.y + Double.valueOf(
					Double.valueOf((Double.valueOf(newY) / Double.valueOf(ivBase.getHeight()))) * rectExpanded.height).intValue();
		}
		newX = Double.valueOf(newX * Double.valueOf(imageBase.width()) / Double.valueOf(ivBase.getWidth())).intValue();
		newY = Double.valueOf(newY * Double.valueOf(imageBase.height()) / Double.valueOf(ivBase.getHeight())).intValue();
		return new Tuple<Integer, Integer>(newX, newY);
	}

	private static void resizeImage(ImageView ivDest, ImageView ivOrigin, Rect crop) {		
		Bitmap dstBmp = Bitmap.createBitmap(fromImageView2Bitmap(ivOrigin), crop.x, crop.y, crop.width, crop.height);
		ivDest.setImageBitmap(dstBmp);
	}

	public static File fromBitmap2File(Bitmap bmp, String filename) throws IOException {
		File fn2 = new File(filename);    
		FileOutputStream out = new FileOutputStream(fn2);  
		bmp.compress(Bitmap.CompressFormat.PNG, 90, out);  
		out.flush();  
		out.close();
		return fn2;
	}

	private static Bitmap fromImageView2Bitmap(ImageView iv) {
		Bitmap bmp = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		iv.draw(c);
		iv.invalidate();
		return bmp;
	}

	private static Bitmap fromMat2ImageView(ImageView imageView, final SuperMat image) {
		Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
		Mat clone = image.clone();
		Utils.matToBitmap(clone, bm);
		imageView.setImageBitmap(bm);
		return bm;
	}

	boolean error_in_post = false;
	class SendHttpRequestTask extends AsyncTask<String, Void, String>{
		private static final String POST_PROTOCOL = "http";
		private static final String POST_DOMAIN = "eu7.thumbr.io";
		private static final int POST_PORT = 9090;
		private static final String POST_FILE = "/";
		private Activity parent;
		public SendHttpRequestTask(Activity p) {
			parent = p;
		}
		@Override
		protected String doInBackground(String... params) {
			String got_positions = params[0];
			String expected_positions = params[1];
			String got_evaluation = params[2];
			String expected_evaluation = params[3];
			String detected = params[4];
			Bitmap b = BitmapFactory.decodeFile(params[5]);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			b.compress(CompressFormat.PNG, 0, baos);

			try {
				HttpClient client = new HttpClient(POST_PROTOCOL, POST_DOMAIN, POST_PORT, POST_FILE);
				client.connectForMultipart();
				client.addFormPart(PictureActivity.TAG_GOT_POS, got_positions);
				client.addFormPart(PictureActivity.TAG_EXP_POS, expected_positions);
				client.addFormPart(PictureActivity.TAG_GOT_EVAL, got_evaluation);
				client.addFormPart(PictureActivity.TAG_EXP_EVAL, expected_evaluation);
				client.addFormPart(PictureActivity.TAG_DETECTED, detected);
				client.addFilePart(PictureActivity.TAG_IMAGE, params[5], baos.toByteArray());
				client.finishMultipart();
				client.getResponse();
				parent.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
							parent.getBaseContext(),
							"The HTTP POST request was sent successfully.",
							Toast.LENGTH_LONG).show();
					}
				});
			}
			catch(Throwable t) {
				parent.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
							parent.getBaseContext(),
							"The HTTP POST request was not sent. Make sure you are connected to internet",
							Toast.LENGTH_LONG).show();
					}
				});
			}
			return null;
		}
		
	}

	private SuperMat getImageFromPhoto() {
		imagePath = getIntent().getStringExtra(FdActivity.EXTRA_MESSAGE); // TODO: UNCOMMENT
		SuperMat img = new SuperMat(Highgui.imread(imagePath));
	
		Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGB);
		new File(imagePath).delete(); // TODO: UNCOMMENT
		return img;
	}
}
