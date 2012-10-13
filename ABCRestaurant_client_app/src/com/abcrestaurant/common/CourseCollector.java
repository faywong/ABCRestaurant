package com.abcrestaurant.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.abcrestaurant.app.MainActivity;
import com.abcrestaurant.network.NetworkTaskHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CourseCollector {
	public static final String TAG = "CourseCategoryCollector";
	private static CourseCollector sInstance = null;
	private static int mCurInx = 0;
	private static int mRetrieveMetadataStatus = 0;
	private static int RETRIEVE_METADATA_COURSE_OK = 1;
	private static int RETRIEVE_METADATA_CATE_OK = 1 << 1;
	/* used to store the cooker's cook task */
	private static LinkedList<OrderItem> mCookerTaskQueue;
	private static Object mCookerItemQueueLock;
	/*
	 * used to store the customer's valid order info, if check out, then empty
	 * this
	 */
	private static LinkedList<Order> mCustomerOrderQueue;
	private static Object mCustomerOrderQueueLock;
	private Handler mHandler;
	private ArrayList<Category> mCategories;
	private ArrayList<CourseInfo> mCourses;
	private Context coursePanelContext;
	private static boolean DEBUG = ABCLogSet.DEBUG;

	public static class OrderItem {
		public int orderID;
		public int courseID;

		public OrderItem(int _orderID, int _courseID) {
			// TODO Auto-generated constructor stub
			orderID = _orderID;
			courseID = _courseID;
		}
	}

	private CourseCollector() {
		mCookerTaskQueue = new LinkedList<CourseCollector.OrderItem>();
		mCustomerOrderQueue = new LinkedList<Order>();
		mCookerItemQueueLock = new Object();
		mCustomerOrderQueueLock = new Object();
	}

	public void queueCustomerOrder(Order order) {
		synchronized (mCustomerOrderQueueLock) {
			mCustomerOrderQueue.add(order);
		}
	}

	public Order getFirstCustomerOrder() {

		synchronized (mCustomerOrderQueueLock) {
			try {
				if (mCustomerOrderQueue.isEmpty()) {
					return null;
				} else {
					return mCustomerOrderQueue.getFirst();
				}
			} catch (NoSuchElementException e) {
				return null;
			}
		}
	}

	/* once check out, clear the orders with the corresponding orderID */
	public void clearCustomerOrder(Order order) {
		synchronized (mCustomerOrderQueueLock) {
			mCustomerOrderQueue.clear();
		}
	}

	public void queueCookerTask(CourseCollector.OrderItem orderItem) {
		synchronized (mCookerItemQueueLock) {
			mCookerTaskQueue.add(orderItem);
		}
	}

	public CourseCollector.OrderItem getFirstCookerTask() {
		synchronized (mCookerItemQueueLock) {
			return mCookerTaskQueue.poll();
		}
	}

	/*
	 * Retrieves and removes the head of the queue represented by this deque (in
	 * other words, the first element of this deque), or returns null if this
	 * deque is empty.
	 */
	public CourseCollector.OrderItem dequeueCookerTask(
			CourseCollector.OrderItem orderItem) {
		synchronized (mCookerItemQueueLock) {
			return mCookerTaskQueue.poll();
		}
	}

	public Category getCategoryByID(int id) {
		for (Category it : mCategories) {
			if (it.getID() == id) {
				return it;
			}
		}
		return null;

	}

	public static CourseCollector instance() {
		if (null == sInstance) {
			sInstance = new CourseCollector();
		}
		return sInstance;
	}

	public ArrayList<Category> getAllCategories() {
		return mCategories;
	}

	public synchronized void setCurrentCategory(int inx) {
		if (mCategories.get(inx) != null) {
			mCurInx = inx;
		} else {
			if (ABCLogSet.DEBUG_COURSE_CATEGORY) {
				Log.e(TAG, "setCurrentCategory() with inx:" + inx + " invalid!");
			}
		}
	}

	synchronized void setCurrentCategory(Category c) {
		mCurInx = mCategories.indexOf(c);
	}

	public synchronized int getCurrentCategoryInx() {
		return mCurInx;
	}

	public synchronized Category getCurrentCategory() {
		return mCategories.get(mCurInx);
	}

	public void setCourses(ArrayList<CourseInfo> courses) {
		synchronized (this) {
			mCourses = courses;
			mRetrieveMetadataStatus |= RETRIEVE_METADATA_COURSE_OK;
			upDataUI();
		}
	}

	private void upDataUI() {
		if (DEBUG) {
			Log.d(TAG, "upDataUI() in");
		}
		if ((RETRIEVE_METADATA_COURSE_OK == (mRetrieveMetadataStatus & RETRIEVE_METADATA_COURSE_OK))
				&& ((RETRIEVE_METADATA_CATE_OK == (mRetrieveMetadataStatus & RETRIEVE_METADATA_CATE_OK)))) {
			if (DEBUG) {
				Log.d(TAG, "send the event:"
						+ MainActivity.RETRIEVE_METADATA_FINISHED);
			}
			sendEvent(MainActivity.RETRIEVE_METADATA_FINISHED);
		}
	}

	private void sendEvent(int eventID) {
		if (null != mHandler) {
			Message msg = mHandler.obtainMessage();
			if (DEBUG) {
				Log.d(TAG, "send msg:RETRIEVE_METADATA_FINISHED in setCourses");
			}
			msg.sendToTarget();
		}
	}

	public void setCategories(ArrayList<Category> categories) {
		synchronized (this) {
			mCategories = categories;
			mRetrieveMetadataStatus |= RETRIEVE_METADATA_CATE_OK;
			upDataUI();
		}
	}

	public ArrayList<CourseInfo> getCourses() {
		return mCourses;
	}

	public CourseInfo getCourseByID(int id) {
		for (CourseInfo info : mCourses) {
			if (info.getID() == id) {
				return info;
			}
		}
		return null;
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	public ArrayList<CourseInfo> getOrderredCourses() {
		ArrayList<CourseInfo> orderred = new ArrayList<CourseInfo>();

		for (CourseInfo course : mCourses) {
			if (course.isSelected()) {
				orderred.add(course);
			}
		}

		return orderred;
	}

	public void setCookerPanelActivityContext(Context ctx) {
		coursePanelContext = ctx;
	}

	public Context getCookerPanelActivityContext() {
		return coursePanelContext;
	}

	public float getOrderredTotalPrice() {
		if (null == mCourses || mCourses.isEmpty()) {
			return 0.0f;
		}
		ArrayList<CourseInfo> tmp = new ArrayList<CourseInfo>();
		for (CourseInfo course : mCourses) {
			if (course.isSelected()) {
				// totalPrice += course.getPrice() * course.getNum();
				tmp.add(course);
			}
		}
		return Order.getTotalCost(tmp);
	}

	public void retrieveCursesInfo() {
		Message msg = NetworkHelper
				.instance()
				.getNetworkHandler()
				.obtainMessage(NetworkTaskHandler.RRTRIEVE_METADATA_FROM_SERVER);
		msg.sendToTarget();
	}

}
