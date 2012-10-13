package com.abcrestaurant.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.abcrestaurant.common.ABCConfig;
import com.abcrestaurant.common.ABCLogSet;
import com.abcrestaurant.common.CourseCollector;
import com.abcrestaurant.common.CourseInfo;
import com.abcrestaurant.common.NetworkHelper;
import com.abcrestaurant.R;
import com.abcrestaurant.network.NetworkTaskHandler;
import com.abcrestaurant.utils.lazylist.ImageLoader;

public class CookerPanelActivity extends Activity implements OnGestureListener,
		OnTouchListener {
	private ViewFlipper mFlipper;
	GestureDetector mGestureDetector;
	private int mCurrentLayoutState;
    private ImageLoader mImageLoader;
	private static final int FLING_MIN_DISTANCE = 100;
	private static final int FLING_MIN_VELOCITY = 100;
	private int mCurCourseID = 0;
	private int mCurOrderID = 0;
	private RadioGroup mRadioGroup;
	private static final String TAG = CookerPanelActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (ABCLogSet.DEBUG_COOKER_PANEL) {
			Log.e(TAG, "onCreate() in!");
		}
		setContentView(R.layout.cookerpanel);
		mFlipper = (ViewFlipper) findViewById(R.id.flipper);
		mGestureDetector = new GestureDetector(
				(android.view.GestureDetector.OnGestureListener) this);
		mFlipper.setOnTouchListener(this);
		mCurrentLayoutState = 0;
		mFlipper.setLongClickable(true);
        mImageLoader = new ImageLoader(this);
        mRadioGroup = (RadioGroup)findViewById(R.id.order_status_RadioGroup);
        /* this is ugly!!!*/
        CourseCollector.instance().setCookerPanelActivityContext(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (ABCLogSet.DEBUG_COOKER_PANEL) {
			Log.e(TAG, "onDestroy() in!");
		}
        CourseCollector.instance().setCookerPanelActivityContext(null);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (ABCLogSet.DEBUG_COOKER_PANEL) {
			Log.e(TAG, "onResume() in!");
		}
		Intent intent = getIntent();
		if (null == intent) {
			return;
		}else if (ABCConfig.FINISH_ON_ALTER_COURSE_STATUS_ACTION == intent.getAction()) {
			/* once the alter the course success, finish myself */
			if (ABCLogSet.DEBUG_COOKER_PANEL) {
				Log.e(TAG, "Received FINISH_ON_ALTER_COURSE_STATUS_ACTION, ready to finish()!");
			}
			finish();
		}

		mCurCourseID = intent.getIntExtra("course_id", 0);
		mCurOrderID = intent.getIntExtra("order_id", 0);
		if (ABCLogSet.DEBUG_COOKER_PANEL) {
			Log.e(TAG, "the course_id: " + mCurCourseID);
		}

		if (0 == mCurCourseID) {
			return;
		}

		/*first queue it, then process the first one */
		CourseCollector collector = CourseCollector.instance();
		collector.queueCookerTask(new CourseCollector.OrderItem(mCurOrderID, mCurCourseID));

		CourseCollector.OrderItem item = collector.getFirstCookerTask();
		mCurCourseID = item.courseID;
		mCurOrderID = item.orderID;

		CourseInfo info = collector.getCourseByID(item.courseID);
		if (null == info) {
			if (ABCLogSet.DEBUG) {
				Log.e(TAG, "the course respect to (id:" + mCurCourseID + ") is null ");
			}
			return;
		}

		TextView courceNameView = (TextView)findViewById(R.id.cur_course_name);
		courceNameView.setText(info.getName());
		TextView courseIdView = (TextView)findViewById(R.id.cur_course_id);
		courseIdView.setText("CourseID: " + String.valueOf(mCurCourseID));
		TextView orderIdView = (TextView)findViewById(R.id.cur_order_id);
		orderIdView.setText("OrderID: " + String.valueOf(mCurOrderID));
		ImageView coursImageView = (ImageView)findViewById(R.id.cur_course_img);
        mImageLoader.DisplayImage(info.getImgURL(), coursImageView);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				Message msg = NetworkHelper
						.instance()
						.getNetworkHandler()
						.obtainMessage(NetworkTaskHandler.ALTER_ORDERRED_COURSE_STATUS);
				Bundle data = new Bundle();
				data.putInt("course_id", mCurCourseID);
				data.putInt("order_id", mCurOrderID);
				switch(checkedId) {
					case R.id.StartBtn:
						data.putInt("status_trans", NetworkTaskHandler.PENDING_TO_COOKING);
						break;
					case R.id.FinishBtn:
						data.putInt("status_trans", NetworkTaskHandler.COOKING_TO_FINISHED);
						break;
					default:
						Log.e(TAG, "In OnCheckedChangeListener() Should not go here!");
						data.putInt("status_trans", NetworkTaskHandler.DUPLICATE);
						break;
				}
				msg.setData(data);
				msg.sendToTarget();
			}
		});
	}

	public void switchLayoutStateTo(int switchTo) {
		while (mCurrentLayoutState != switchTo) {
			if (mCurrentLayoutState > switchTo) {
				mCurrentLayoutState--;
				mFlipper.setInAnimation(inFromLeftAnimation());
				mFlipper.setOutAnimation(outToRightAnimation());
				mFlipper.showPrevious();
			} else {
				mCurrentLayoutState++;
				mFlipper.setInAnimation(inFromRightAnimation());
				mFlipper.setOutAnimation(outToLeftAnimation());
				mFlipper.showNext();
			}
		}
	}

	protected Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	protected Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	protected Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	protected Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (ABCLogSet.DEBUG) {
			Log.e("DEBUG", "onFling() in e1.getX() - e2.getX():"
					+ (e1.getX() - e2.getX()) + " velocityX:" + velocityX);
		}

		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			mFlipper.setInAnimation(inFromRightAnimation());
			mFlipper.setOutAnimation(outToLeftAnimation());
			mFlipper.showNext();
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			mFlipper.setInAnimation(inFromLeftAnimation());
			mFlipper.setOutAnimation(outToRightAnimation());
			mFlipper.showPrevious();
		}

		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

}
