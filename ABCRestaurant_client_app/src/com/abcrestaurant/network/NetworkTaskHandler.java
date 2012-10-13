package com.abcrestaurant.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.abcrestaurant.common.ABCConfig;
import com.abcrestaurant.common.ABCLogSet;
import com.abcrestaurant.common.CourseCollector;
import com.abcrestaurant.common.CourseInfo;
import com.abcrestaurant.common.Order;
import com.abcrestaurant.common.Category;

public class NetworkTaskHandler extends Handler {
	public static final int SEND_ORDER_TO_SERVER = 0;
	public static final int RRTRIEVE_METADATA_FROM_SERVER = 1;
	public static final int ALTER_ORDERRED_COURSE_STATUS = 2;
	public static final int DUPLICATE = 0;
	public static final int PENDING_TO_COOKING = 1;
	public static final int COOKING_TO_FINISHED = 2;
	private static final int GOOD_RETURN_CODE = 200;
	private static final String TAG = "NetworkTaskHandler";
	private static final boolean DEBUG = false;

	public NetworkTaskHandler() {

	}

	public NetworkTaskHandler(Looper looper) {
		super(looper);
	}

	/**
	 * handle network-related task
	 */
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case SEND_ORDER_TO_SERVER:
			handleSengOrder();
			break;
		case RRTRIEVE_METADATA_FROM_SERVER:
			handleRetrieveMetaData();
			break;
		case ALTER_ORDERRED_COURSE_STATUS: {
			Bundle data = msg.getData();
			int orderID = data.getInt("order_id");
			int courseID = data.getInt("course_id");
			int transMode = data.getInt("status_trans");
			updateOrderredCourseStatus(orderID, courseID, transMode);
		}
			break;
		default:
			Log.e(TAG, "handle msg wrong!");
			break;
		}
	}

	private void updateOrderredCourseStatus(int orderID, int courseID,
			int transMode) {
		// TODO Auto-generated method stub
		if (ABCLogSet.DEBUG_NETWORK) {
			Log.e(ABCLogSet.ORDER_TAG, "updateOrderredCourseStatus(orderID:" + orderID  +
					" courseID:" + courseID +
					" transMode:" + transMode +
					" ) in");
		}
		HttpClient httpClient = new DefaultHttpClient();
		try {
			// 1. construct the multipart entity of the post request
			// MultipartEntity multipart = new MultipartEntity();

			HttpParams httpParams = new BasicHttpParams();
			HttpPost httpPost = new HttpPost(
					ABCConfig.getAlterOrderredCourseURL() + "/" + orderID + "/" + courseID + "/" + transMode);

			httpPost.setParams(httpParams);

			// response = httpClient.execute(httpPost);

			int res = 0;
			res = httpClient.execute(httpPost).getStatusLine()
					.getStatusCode();

			if (res == 200) {
				if (ABCLogSet.DEBUG_NETWORK) {
					Log.e(ABCLogSet.ORDER_TAG, "updateOrderredCourseStatus(orderID:" + orderID  +
							" courseID:" + courseID +
							" transMode:" + transMode +
							" ) succuss!");
				}

				if (NetworkTaskHandler.COOKING_TO_FINISHED != transMode) {
					return;
				}

				Intent intent = new Intent(ABCConfig.FINISH_ON_ALTER_COURSE_STATUS_ACTION);
				//TODO??? we need a context object to return the status to our CookerPanelActivity
				if (null != CourseCollector.instance().getCookerPanelActivityContext()) {
					try {
						CourseCollector.instance().getCookerPanelActivityContext().startActivity(intent);
					}
					catch (ActivityNotFoundException e) {
						// TODO: handle exception
						e.printStackTrace();
						Log.e(TAG, "the activity which can handle action:" + ABCConfig.FINISH_ON_ALTER_COURSE_STATUS_ACTION
								+ " is not found!");
					}
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "Occurs a fatal exception when connecting network:");
			e.printStackTrace();
		}
	}

	private void handleRetrieveMetaData() {
		synchronized (CourseCollector.instance()) {
			if (ABCLogSet.DEBUG_NETWORK) {
				Log.e(ABCLogSet.ORDER_TAG, "handleSengOrder() in");
			}
			HttpClient httpClient = new DefaultHttpClient();
			try {
				// 1. construct the multipart entity of the post request
				// MultipartEntity multipart = new MultipartEntity();

				HttpParams httpParams = new BasicHttpParams();
				HttpPost httpPost = new HttpPost(
						ABCConfig.getServerReceiveMetadataURL());

				httpPost.setParams(httpParams);

				// response = httpClient.execute(httpPost);

				int res = 0;
				res = httpClient.execute(httpPost).getStatusLine()
						.getStatusCode();
				if (res == 200) {
					HttpResponse httpResponse = httpClient.execute(httpPost);
					StringBuilder builder = new StringBuilder();
					BufferedReader bufferedReader2 = new BufferedReader(
							new InputStreamReader(httpResponse.getEntity()
									.getContent()));
					for (String s = bufferedReader2.readLine(); s != null; s = bufferedReader2
							.readLine()) {
						builder.append(s);
					}

					JSONObject metadata = new JSONObject(builder.toString())
							.getJSONObject("metadata");

					JSONObject urlinfo = metadata.getJSONObject("urlinfo");
					ABCConfig
							.setCourseImgURL(urlinfo.getString("courseImgURL"));
					ABCConfig.setCategoryImgURL(urlinfo
							.getString("categoryImgURL"));
					ABCConfig.setCourseNo(Integer.valueOf(metadata
							.getString("courseNo")));
					ABCConfig.setCategoryNo(Integer.valueOf(metadata
							.getString("cateNo")));

					// the categories info
					JSONArray categoriesArray = metadata
							.getJSONArray("categories");
					ArrayList<Category> categories = new ArrayList<Category>();
					for (int i = 0; i < categoriesArray.length(); i++) {
						JSONObject jsonObject2 = (JSONObject) categoriesArray
								.opt(i);
						int id = Integer.valueOf(jsonObject2.getString("id"));
						CharSequence name = jsonObject2.getString("name");
						String imgURL = ABCConfig.getCategoryImgURL() + "/"
								+ id;
						Category cate = new Category(id,
								name.toString(), imgURL);
						if (DEBUG) {
							Log.d(TAG, "the " + i + "th Category:" + cate);
						}
						categories.add(cate);
					}

					CourseCollector.instance().setCategories(categories);

					// the couses info
					JSONArray coursesArray = metadata.getJSONArray("courses");
					ArrayList<CourseInfo> courses = new ArrayList<CourseInfo>();
					for (int i = 0; i < coursesArray.length(); i++) {
						JSONObject jsonObject2 = (JSONObject) coursesArray
								.opt(i);

						int id = Integer.valueOf(jsonObject2.getString("id"));
						CharSequence name = jsonObject2.getString("name");
						String imgURL = ABCConfig.getCourseImgURL() + "/" + id;
						int category_id = Integer.valueOf(jsonObject2
								.getString("category_id"));
						Category category = CourseCollector.instance()
								.getCategoryByID(category_id);
						float price = Float.valueOf(jsonObject2
								.getString("price"));
						int ordercount = Integer.valueOf(jsonObject2
								.getString("order_count"));
						CourseInfo courseInfo = new CourseInfo(
						/* id */
						id, name, imgURL, category, null, null, price);
						courseInfo.setOrderCount(ordercount);
						if (DEBUG) {
							Log.e(TAG, "the " + i + "th courseinfo:"
									+ courseInfo);
						}
						courses.add(courseInfo);
					}

					CourseCollector.instance().setCourses(courses);

					if (DEBUG) {
						Log.e(TAG,
								"The metadata retrieved from server:"
										+ metadata + "\n" + "categoryImgURL:"
										+ ABCConfig.getCategoryImgURL() + "\n"
										+ "courseImgURL:"
										+ ABCConfig.getCourseImgURL() + "\n"
										+ "CourseNo:" + ABCConfig.getCourseNo()
										+ "\n" + "CategoryNo:"
										+ ABCConfig.getCategoryNo());
					}
					ABCConfig.saveLastRetrieveMetaDataTime();
				}
				else {
					ABCConfig.invalidateServerHost();
				}

			} catch (Exception e) {
				Log.e(TAG, "Occurs a fatal exception when connecting network:");
				e.printStackTrace();
				ABCConfig.invalidateServerHost();
			}
		}

		/*
		 * Toast.makeText( ShowOrderActivity.this,
		 * "It occurs an error when the order was sending to the Server, Please rewind to order!"
		 * , Toast.LENGTH_LONG).show();
		 */

		// 4. read the response if (response == null) { return; }

		/*
		 * HttpEntity entity = response.getEntity(); BufferedReader reader =
		 * null; try { reader = new BufferedReader(new InputStreamReader(
		 * entity.getContent())); } catch (IllegalStateException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); } String line = null;
		 *
		 * if (null == reader) { Log.e(TAG, "OMG the reader is " + reader);
		 * return; } try { while ((line = reader.readLine()) != null) Log.e(TAG,
		 * line); } catch (IOException e) { e.printStackTrace(); } try {
		 * reader.close(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

	}

	private void handleSengOrder() {
		// Note: reference to
		// http://zybnet.com/java-http-client-and-post-requests/

		// the class need the httpmime-4.1.3.jar download from
		// http://grepcode.com/snapshot/repo1.maven.org/maven2/org.apache.httpcomponents/httpmime/4.1.3/
		// TODO Auto-generated method stub
		// send the order info in the format of JSON to our server-side
		if (ABCLogSet.DEBUG_NETWORK) {
			Log.e(ABCLogSet.ORDER_TAG, "handleSengOrder() in");
		}
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		try {
			// 1. construct the multipart entity of the post request
			// MultipartEntity multipart = new MultipartEntity();

			HttpParams httpParams = new BasicHttpParams();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			Order order = Order.newOrder(
					CourseCollector.instance().getOrderredCourses());
			/* keep one piece of copy in our CourseCollector */
			CourseCollector.instance().queueCustomerOrder(order);

			JSONObject orderObject = order.toJsonObject();
			// multipart.addPart("json", new
			// StringBody(orderObject.toString()));

			if (ABCLogSet.DEBUG_NETWORK) {
				Log.e(ABCLogSet.ORDER_TAG,
						"orderJSON:" + orderObject.toString());
			}

			nameValuePairs.add(new BasicNameValuePair("json", orderObject
					.toString()));
			HttpPost httpPost = new HttpPost(
					ABCConfig.getServerReceiveOrderURL());
			// httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			// httpPost.setEntity(multipart);

			httpPost.setParams(httpParams);

			Log.v("http", " send  http httpPost:" + httpPost);
			response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			Log.v("http", "statusCode response from server is:" + statusCode);

			EntityUtils.toString(response.getEntity());

			if (GOOD_RETURN_CODE == statusCode) {
				/*
				 * Toast.makeText(ShowOrderActivity.this,
				 * "The order has been sent to server successfully!",
				 * Toast.LENGTH_LONG).show();
				 */
			}
			/*
			 * Toast.makeText(ShowOrderActivity.this,
			 * "The order hasn't been sent to server successfully!",
			 * Toast.LENGTH_LONG).show();
			 */

		} catch (Exception e) {
			Log.e(TAG, "The fatal exception when connecting network:");
			e.printStackTrace();
			/*
			 * Toast.makeText( ShowOrderActivity.this,
			 * "It occurs an error when the order was sending to the Server, Please rewind to order!"
			 * , Toast.LENGTH_LONG).show();
			 */
		}
		/*
		 * // 4. read the response if (response == null) { return; }
		 *
		 * HttpEntity entity = response.getEntity(); BufferedReader reader =
		 * null; try { reader = new BufferedReader(new
		 * InputStreamReader(entity.getContent())); } catch
		 * (IllegalStateException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } String line = null; try { while
		 * ((line = reader.readLine()) != null) Log.e(TAG, line); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } try { reader.close(); } catch (IOException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
	}
}
