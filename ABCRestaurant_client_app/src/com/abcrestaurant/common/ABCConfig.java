package com.abcrestaurant.common;

import android.os.SystemClock;
import android.util.Log;

public class ABCConfig {
	/*all courses screen*/
	private static int mRowNoAllCourses = 2;
	private static int mAppsNoAllCourses = 8;
	private static String categoryImgURL = "";
	private static String coursesImgURL = "";
	private static int courseNo;
	private static int categoryNo;
    public static final String PUSH_COURSE_TO_COOKER_TITLE = "NEWCOURSEFORCOOKER";
    public static final String SHOW_COOKER_PANEL_ACTION = "android.intent.action.SHOW_COOKER_PANEL";
    public static final String LAUNCH_ABCRESTAURANT_SETTING_ACTION = "android.intent.action.LAUNCH_ABCRESTAURANT_SETTING";
    public static final int REQUEST_CODE_LAUNCH_SETTING = 1;

    /* This action is used to launch the cooker panel activity after altering the ordered course status successfully  */
    public static final String FINISH_ON_ALTER_COURSE_STATUS_ACTION =
    		"android.intent.action.FINISH_SELF_ON_ALTER_COURSE";

    public static final String SHOW_ORDER_INTENT_ACTION = "android.intent.action.SHOW_ORDER";
    public static final String SHOW_ORDER_HISTORY_INTENT_ACTION = "android.intent.action.SHOW_ORDER_HISTORY";
    public static final String IP_ADDR_SETTING_PREFS_KEY = "ABC_SERVER_NAME";
    public static final String InvalidServerHostName = "255.255.255.255";
//    private static String mServerHostName = "192.168.1.133";
    private static String mServerHostName = InvalidServerHostName;

    private static long lastRetrieveMetaDataTime = -1;
    private static long updateMetaDataInterval = 300000;

	public static int getRowNOAllCourcesSrc() {
		return mRowNoAllCourses;
	}

	public static void setRowNOAllCourcesSrc(int newRowNo) {
		mRowNoAllCourses = newRowNo;
	}
	
	public static int getAppNOAllCourcesSrc() {
		return mAppsNoAllCourses;
	}
	
	public static void setAppNOAllCourcesSrc(int newAppsNoPerAllCourses) {
		mAppsNoAllCourses = newAppsNoPerAllCourses;
	}

	public static String getServerHostName() {
		return mServerHostName;
	}
	
	public static void setServerHostName(String newHostName) {
		mServerHostName = newHostName;
	}

	public static boolean isServerHostValid(String hostName) {
		if (null == hostName) {
			return (!InvalidServerHostName.equals(mServerHostName));
		}
		return (!InvalidServerHostName.equals(hostName));
	}
	
	public static void invalidateServerHost() {
		mServerHostName = InvalidServerHostName;
	}
	
	public static String getServerReceiveOrderURL() {
		return ABCConfig.getABCServerRootURL() + "/orderproc/saveclientorder";
	}

	public static String getServerReceiveMetadataURL() {
		return ABCConfig.getABCServerRootURL() + "/dataforclient/getMetadata";
	}

	public static String getAlterOrderredCourseURL() {
		return ABCConfig.getABCServerRootURL() + "/orderproc/alterOrderredCourseStatus";
	}

	synchronized public static void setCourseImgURL(String URL) {
		coursesImgURL = URL;
	}

	public static String getCourseImgURL() {
		return coursesImgURL;
	}

	synchronized public static void setCategoryImgURL(String URL) {
		categoryImgURL = URL;
	}

	public static int getCourseNo() {
		return courseNo;
	}

	synchronized public static void setCourseNo(int no) {
		courseNo = no;
	}

	public static int getCategoryNo() {
		return categoryNo;
	}

	synchronized public static void setCategoryNo(int no) {
		categoryNo = no;
	}

	public static String getCategoryImgURL() {
		return categoryImgURL;
	}

	public static String getABCServerRootURL() {
		return "http://" + getServerHostName() + "/abcrestaurant/index.php";
	}

	public static boolean needRetrieveMetaData() {
		if (ABCLogSet.DEBUG_CONFIG)
			Log.e(ABCLogSet.CONFIG_TAG, "needRetrieveMetaData() in retrieveDelta:" 
				+ (SystemClock.uptimeMillis() - lastRetrieveMetaDataTime) + " updateMetaDataInterval:" 
					+ updateMetaDataInterval);
		return (SystemClock.uptimeMillis() - lastRetrieveMetaDataTime) > updateMetaDataInterval;
	}

	public static void saveLastRetrieveMetaDataTime() {
		lastRetrieveMetaDataTime = SystemClock.uptimeMillis();
	}
}
