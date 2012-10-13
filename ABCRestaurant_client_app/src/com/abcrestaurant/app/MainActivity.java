package com.abcrestaurant.app;

import java.util.ArrayList;
import java.util.List;

import org.androidpn.client.ServiceManager;

import com.abcrestaurant.client.R;
import com.abcrestaurant.client.R.drawable;
import com.abcrestaurant.client.R.id;
import com.abcrestaurant.client.R.layout;
import com.abcrestaurant.client.R.string;
import com.abcrestaurant.common.ABCConfig;
import com.abcrestaurant.common.ABCLogSet;
import com.abcrestaurant.common.Category;
import com.abcrestaurant.common.CourseCollector;
import com.abcrestaurant.common.CourseInfo;
import com.abcrestaurant.ui.AllCoursesView;
import com.abcrestaurant.ui.DragController;
import com.abcrestaurant.ui.AllCoursesView.CheckoutButton;
import com.abcrestaurant.utils.lazylist.ImageLoader;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.content.res.Resources;

public class MainActivity extends Activity implements
		View.OnClickListener, AdapterView.OnItemClickListener {
	private DragController mDragController;
	private AllCoursesView mAllCoursesGrid;
	private static boolean DEBUG = ABCLogSet.DEBUG;
	private ListView mCagegoryList;
	private CourseCategoryAdapter mCategoriesAdapter;
	public static final int RETRIEVE_METADATA_FINISHED = 0;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (DEBUG) {
				Log.d(TAG, "The msg received is: " + msg.what);
			}

			switch (msg.what) {
			case MainActivity.RETRIEVE_METADATA_FINISHED:
				populateCourses();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};
	public static final String TAG = "ABCRestaurantActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (ABCLogSet.DEBUG_LIFECYCLE) {
			Log.e(ABCLogSet.MAIN_TAG, "onCreate() in");
		}
		super.onCreate(savedInstanceState);
		// Start the Notification service first, because is async and so important
		ServiceManager serviceManager = new ServiceManager(this);
		serviceManager.setNotificationIcon(R.drawable.notification);
		serviceManager.startService();
		// Check some user settings(Server Host name) second
		checkSettings();
		setContentView(R.layout.mainlayout);
		mDragController = new DragController(this);
		getLayoutInflater();
		CourseCollector.instance().setHandler(mHandler);
		setupViews();
	}
	
	@Override
	protected void onResume() {
		if (ABCLogSet.DEBUG)
			Log.e(ABCLogSet.MAIN_TAG, "onResume() in");
		// TODO Auto-generated method stub
		super.onResume();
		if (ABCLogSet.DEBUG) {
			Log.e(ABCLogSet.MAIN_TAG, "needRetrieveMetaData:" + ABCConfig.needRetrieveMetaData());
		}
		
		if (ABCConfig.needRetrieveMetaData()) {
			Log.e(TAG, "Before retrieveCursesInfo()");
			CourseCollector.instance().retrieveCursesInfo();
			Log.e(TAG, "retrieveCursesInfo() Over");
		}
		populateCourses();
	}

	private void checkSettings() {
	    final SharedPreferences ipAddrPrefs = getSharedPreferences(ABCConfig.IP_ADDR_SETTING_PREFS_KEY, MODE_PRIVATE);
		String serverHostName = ipAddrPrefs.getString(ABCConfig.IP_ADDR_SETTING_PREFS_KEY, ABCConfig.InvalidServerHostName);
		Log.i(TAG,"The saved serverHostName is " + serverHostName);

		if (!ABCConfig.isServerHostValid(null) && ABCConfig.isServerHostValid(serverHostName)) {
			ABCConfig.setServerHostName(serverHostName);
		}
		
		if (!ABCConfig.isServerHostValid(serverHostName)) {
			Intent launchSettingIntent = new Intent(ABCConfig.LAUNCH_ABCRESTAURANT_SETTING_ACTION);
			try {
				startActivityForResult(launchSettingIntent, ABCConfig.REQUEST_CODE_LAUNCH_SETTING);
			}
			catch (ActivityNotFoundException anfe) {
				Log.e(TAG, "FATAL¡¡ERROR!!!The ABCSetting Activity is not found!");
				anfe.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (ABCConfig.REQUEST_CODE_LAUNCH_SETTING == requestCode) {
			if (DEBUG) {
				Log.d(TAG, "The Setting is completed!");
			}
		}
	}

	private void setupViews() {
		// TODO Auto-generated method stub
		if (DEBUG) {
			Log.d(TAG, "setupViews() in");
		}
		CheckoutButton checkoutButton = null;
		CheckoutButton clearallButton = null;
		CheckoutButton orderHistoryButton = null;

		try {
			mAllCoursesGrid = (AllCoursesView) findViewById(R.id.all_courses_view);
			checkoutButton = (CheckoutButton) findViewById(R.id.all_courses_checkout_button);
			clearallButton = (CheckoutButton) findViewById(R.id.all_courses_clearall_button);
			orderHistoryButton = (CheckoutButton) findViewById(R.id.all_courses_historyorder_button);

			mCagegoryList = (ListView) findViewById(R.id.CategoryList);
		} catch (Resources.NotFoundException e) {
			Log.e(TAG, "Resouce cannot be found!");
		}
		mAllCoursesGrid.setDragController(mDragController);
		((View) mAllCoursesGrid).setWillNotDraw(false); // We don't want a hole
														// punched in our
														// window.
		// Manage focusability manually since this thing is always visible
		((View) mAllCoursesGrid).setFocusable(false);
		mDragController.setScrollView(mAllCoursesGrid);

		/*
		 *
		 * CharSequence name, int uUID, Category category, Bitmap checkedBitmap,
		 * Intent intent, Bitmap iconBitmap, CourseDetail details
		 */
		// test couse 1
		// Bitmap CourseBackgroup = BitmapFactory.decodeResource(getResources(),
		// R.drawable.bbjy);
		//
		// Category cate = CourseCollector.instance().getAllCategories().get(1);
		// mDragController.setScrollView(mAllCoursesGrid);
		//
		// mCourses.add(new CourseInfo("Chuancai1", 001, cate, CourseBackgroup,
		// null, CourseBackgroup, null, 18.00f));
		//
		// // test couse 2
		// CourseBackgroup = BitmapFactory.decodeResource(getResources(),
		// R.drawable.hswcy);
		// cate = CourseCollector.instance().getAllCategories().get(2);
		//
		// mCourses.add(new CourseInfo("Ecai1", 002, cate, CourseBackgroup,
		// null,
		// CourseBackgroup, null, 15.00f));
		// CourseCollector.instance().setCourses(mCourses);
		checkoutButton.setOnClickListener(this);
		clearallButton.setOnClickListener(this);
		orderHistoryButton.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void populateCourses() {
		bindAllCourses(CourseCollector.instance().getCourses());
	}

	/**
	 * Add the icons for all courses & categories.
	 *
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAllCourses(ArrayList<CourseInfo> courses) {
		if (DEBUG) {
			Log.e(TAG, "bindAllCourses() in");
		}
		/*
		 *
		 * build category related views
		 */
		if (null == CourseCollector.instance().getAllCategories()) {
			return;
		}
		mCategoriesAdapter = new CourseCategoryAdapter(this, 0, CourseCollector
				.instance().getAllCategories());
		mCagegoryList.setAdapter(mCategoriesAdapter);
		mCagegoryList.setOnItemClickListener(this);

		if (null != mAllCoursesGrid) {
			mAllCoursesGrid.setCourses(courses);
		}
	}

	@Override
	public void onClick(View v) {
		if (ABCLogSet.DEBUG) {
			Log.e(TAG, "onClick() in viewid:" + v.getId());
		}
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.all_courses_checkout_button:
			order();
			break;
		case R.id.all_courses_clearall_button:
			clearAll();
			break;
		case R.id.all_courses_historyorder_button:
			showHistoryOrder();
			break;
		}

	}

	private void showHistoryOrder() {
		// TODO Auto-generated method stub
		if (ABCLogSet.DEBUG) {
			Log.e(TAG, "showHistoryOrder() in");
		}

		if (null == CourseCollector.instance().getFirstCustomerOrder()) {
			Toast.makeText(MainActivity.this,
					getResources().getString(R.string.order_history_noting_promt),
					Toast.LENGTH_LONG).show();
			return;
		}

		Intent showOrderHistoryIntent = new Intent(ABCConfig.SHOW_ORDER_HISTORY_INTENT_ACTION);
		try {
			startActivity(showOrderHistoryIntent);
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
			Log.e(TAG, "the showOrder Activity is not found!");
		}
	}

	private void order() {
		// TODO Auto-generated method stub
		if (ABCLogSet.DEBUG) {
			Log.e(TAG, "checkout() in");
		}

		if (CourseCollector.instance().getOrderredCourses().size() == 0) {
			Toast.makeText(MainActivity.this,
					getResources().getString(R.string.ordernoting_promt),
					Toast.LENGTH_LONG).show();
			return;
		}

		Intent showOrderIntent = new Intent(ABCConfig.SHOW_ORDER_INTENT_ACTION);
		try {
			startActivity(showOrderIntent);
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
			Log.e(TAG, "the showOrder Activity is not found!");
		}

	}

	private void clearAll() {
		// TODO Auto-generated method stub
		if (ABCLogSet.DEBUG) {
			Log.e(TAG, "clearAll() in");
		}
		for (CourseInfo info : CourseCollector.instance().getCourses()) {
			info.setSelected(false);
		}
		mAllCoursesGrid.adaptAll(AllCoursesView.UPDATE_WHEN_NORMAL);
	}

	public class CourseCategoryAdapter extends ArrayAdapter<Category> {
		private List<Category> mList;
		private final LayoutInflater mInflater;
		private ImageLoader mImageLoader;

		public CourseCategoryAdapter(Context context, int textViewResourceId,
				List<Category> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			mList = objects;
			mInflater = LayoutInflater.from(context);
			mImageLoader = new ImageLoader(context);

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Category getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Category info = mList.get(position);
			if (null == info) {
				Log.e(TAG, "getView Error!");
				return null;
			}

			if (null == convertView) {
				convertView = (LinearLayout) mInflater.inflate(
						R.layout.coursecategoryitem, parent, false);
			}

			/*
			 * SpannableString sp; sp = new SpannableString("TaskName:\n" +
			 * info.baseActivity.getClassName() + "\n"); sp.setSpan(new
			 * ForegroundColorSpan(Color.GREEN), 0, "TaskName:\n".length(),
			 * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); sp.setSpan(new
			 * ForegroundColorSpan(Color.WHITE), "TaskName:\n".length(),
			 * sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); sp.setSpan(new
			 * StyleSpan(android.graphics.Typeface.BOLD_ITALIC),
			 * "TaskName:\n".length(), sp.length(),
			 * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			 */

			/* TODO:Need to fix??? */
			String imgURL;
			if (null != mList.get(position)) {
				imgURL = mList.get(position).getImgURL();
			} else {
				Log.e(TAG, "Error when build the view!");
				return null;
			}

			ImageView cateImage = (ImageView) convertView
					.findViewById(R.id.CateImg);

			mImageLoader.DisplayImage(imgURL, cateImage);

			//cateBtn.setOnClickListener(ABCRestaurantActivity.this);

			return convertView;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (ABCLogSet.DEBUG) {
			Log.e(TAG, "onItemClick() in position " + position);
		}

		CourseCollector.instance().setCurrentCategory(position);
		mAllCoursesGrid.adaptAll(AllCoursesView.UPDATE_WHEN_NORMAL);
	}

}