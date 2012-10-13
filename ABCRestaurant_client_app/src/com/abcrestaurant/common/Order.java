package com.abcrestaurant.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class Order {
    private static final String TAG = "Order";

	private static int totalOrders = 1;
	/* the id member is not useful, we should let the server produce a order ID */
	private int id;
	//it's just a suggestion, we kept the real time in server-side
	private String time;
	private ArrayList<CourseInfo> mOrderredCourses;
    SimpleDateFormat mformatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.CHINA);


	synchronized static int getTotalOrderNo() {
		return totalOrders++;
	}

	public void setRealID(int realID) {
		id = realID;
	}

	private Order(int dummyID, ArrayList<CourseInfo> courses) {
		this.id = dummyID;
		mformatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		this.time = mformatter.format(System.currentTimeMillis());
		this.mOrderredCourses = courses;
	}

	public ArrayList<CourseInfo> getOrderredCourses() {
		return mOrderredCourses;
	}

	/* the only method to calculate the total price of the courses */
	synchronized static public float getTotalCost(ArrayList<CourseInfo> targetCourses) {
		float cost = 0.0f;
		for (CourseInfo course : targetCourses) {
			cost += course.getPrice() * course.getNum();
		}
		return cost;
	}

	public static Order newOrder(ArrayList<CourseInfo> courses) {
		return new Order(getTotalOrderNo(), courses);
	}

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

	public JSONObject toJsonObject() {
		JSONObject orderObject = new JSONObject();
		JSONObject courseCollectorObject = new JSONObject();
		try {
			orderObject.put("id", id);
			orderObject.put("time", time);
			orderObject.put("ip", getLocalIpAddress());
			orderObject.put("totalprice", Order.getTotalCost(getOrderredCourses()));

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < mOrderredCourses.size(); i++ ) {
			CourseInfo course =  mOrderredCourses.get(i);
			JSONObject courseObject = new JSONObject();
			try {
				courseObject.put("id", course.getID());
				courseObject.put("num", course.getNum());
				courseCollectorObject.put("course" + i, courseObject);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			orderObject.put("courses", courseCollectorObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orderObject;
	}
}
