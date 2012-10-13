package com.abcrestaurant.app;

import com.abcrestaurant.common.ABCLogSet;
import com.abcrestaurant.common.CourseCollector;
import com.abcrestaurant.common.CourseInfo;
import com.abcrestaurant.common.NetworkHelper;
import com.abcrestaurant.R;
import com.abcrestaurant.network.NetworkTaskHandler;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShowOrderActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "ShowOrderActivity";
	private ListView mOrderListView;
	OrderListAdapter mOrderListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orderlist);
		Button confirmBtn = (Button) findViewById(R.id.order_confirm_btn);
		Button cancelBtn = (Button) findViewById(R.id.order_cancel_btn);
		if ((null != confirmBtn) && (null != cancelBtn)) {
			confirmBtn.setOnClickListener(this);
			cancelBtn.setOnClickListener(this);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
		mOrderListView = (ListView) findViewById(R.id.OrderList);
		ArrayList<CourseInfo> orderredCourses = new ArrayList<CourseInfo>();
		if (null != mOrderListView) {
			ArrayList<CourseInfo> allCourses = CourseCollector.instance()
					.getCourses();
			for (CourseInfo course : allCourses) {
				if (course.isSelected()) {
					orderredCourses.add(course);
				}
			}
		}
		mOrderListAdapter = new OrderListAdapter(this, 0, 0, orderredCourses);
		mOrderListView.setAdapter(mOrderListAdapter);
		TextView totalPriceView = (TextView) findViewById(R.id.TotalPrice);
		if (null != totalPriceView) {
			totalPriceView.setText(getResources().getString(
					R.string.TotalPriceLabel)
					+ Float.toString(CourseCollector.instance()
							.getOrderredTotalPrice()));
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private class OrderListAdapter extends ArrayAdapter<CourseInfo> {
		private List<CourseInfo> mOrderredCourses;
		private LayoutInflater mInflater;

		public OrderListAdapter(Context context, int resource,
				int textViewResourceId, List<CourseInfo> objects) {
			super(context, resource, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			mInflater = LayoutInflater.from(context);
			mOrderredCourses = objects;
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return mOrderredCourses.size();
		}

		public CourseInfo getItem(int position) {
			// TODO Auto-generated method stub
			return mOrderredCourses.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final CourseInfo info = mOrderredCourses.get(position);

			if (null == convertView) {
				// convertView = itemLayout.findViewById(R.id.courseItem);
				if (ABCLogSet.DEBUG_SHOW_ORDER) {
					Log.e(TAG, "in getView() inflate from orderitem layout");
				}
				convertView = (RelativeLayout) mInflater.inflate(
						R.layout.orderitem, null, false);
			}
			final TextView courseNameView = (TextView) convertView
					.findViewById(R.id.CouseName);
			final TextView coursePriceView = (TextView) convertView
					.findViewById(R.id.CoursePrice);
			courseNameView.setText(mOrderredCourses.get(position).getName());

			if (ABCLogSet.DEBUG) {
				Log.e(TAG, "mOrderredCourses.get(position).getPrice() is "
						+ mOrderredCourses.get(position).getPrice());
			}
			coursePriceView.setText(Float.toString(mOrderredCourses.get(
					position).getPrice()));

			RelativeLayout courseNoRelLayout = (RelativeLayout) convertView
					.findViewById(R.id.courseNoLayout);
			if (null == courseNoRelLayout) {
				Log.e(TAG, "The course NO. layout cannot be found!");
				return convertView;
			}
			final TextView noText = (TextView) courseNoRelLayout
					.findViewById(R.id.CourseNo);

			noText.setText(Integer.toString(info.getNum()));

			final Button incBtn = (Button) courseNoRelLayout
					.findViewById(R.id.incNo);

			if (null != incBtn) {
				incBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						info.incNum();
						noText.setText(Integer.toString(info.getNum()));
						// update the total price Label
						TextView totalPriceView = (TextView) findViewById(R.id.TotalPrice);
						if (null != totalPriceView) {
							totalPriceView.setText(getResources().getString(
									R.string.TotalPriceLabel)
									+ Float.toString(CourseCollector.instance()
											.getOrderredTotalPrice()));
						}
					}
				});
			}

			final Button decBtn = (Button) courseNoRelLayout
					.findViewById(R.id.decNo);

			if (null != decBtn) {
				decBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						info.decNum();
						noText.setText(Integer.toString(info.getNum()));
						// update the total price Label
						TextView totalPriceView = (TextView) findViewById(R.id.TotalPrice);
						if (null != totalPriceView) {
							totalPriceView.setText(getResources().getString(
									R.string.TotalPriceLabel)
									+ Float.toString(CourseCollector.instance()
											.getOrderredTotalPrice()));
						}
					}
				});
			}

			final Button delBtn = (Button) convertView
					.findViewById(R.id.delBtn_orderItem);
			if (null != delBtn) {
				delBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						info.setSelected(false);
						mOrderListAdapter.remove(info);
						mOrderListAdapter.notifyDataSetChanged();

						TextView totalPriceView = (TextView) findViewById(R.id.TotalPrice);
						if (null != totalPriceView) {
							totalPriceView.setText(getResources().getString(
									R.string.TotalPriceLabel)
									+ Float.toString(CourseCollector.instance()
											.getOrderredTotalPrice()));
						}
					}
				});
			}
			return convertView;
		}

	}

	@Override
	public void onClick(View v) {
		if (ABCLogSet.DEBUG) {
			Log.e(ABCLogSet.ORDER_TAG, "onClick() in");
		}
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.order_confirm_btn:
			order();
			break;
		case R.id.order_cancel_btn:
			cancel_order();
			break;
		default:
			Log.e(TAG, "some thing wrong in onClickListener");
		}
	}

	private void order() {
		if (ABCLogSet.DEBUG) {
			Log.e(ABCLogSet.ORDER_TAG, "order() in");
		}
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.order_last_confirm);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setPositiveButton(R.string.confirm_btn_label,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendOrderInfoToServer();

						Dialog dialog1 = new AlertDialog.Builder(
								ShowOrderActivity.this).setMessage(
								R.string.order_success_msg).create();
						dialog1.show();
						// TODO???
						/*
						 * also should create a notification
						 */
						dialog1.dismiss();
						finish();
					}

				});

		builder.setNegativeButton(R.string.cancel_btn_label,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// returnToABCHome(ShowOrderActivity.this);
						dialog.dismiss();
						// finish();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void cancel_order() {
		if (ABCLogSet.DEBUG) {
			Log.e(ABCLogSet.ORDER_TAG, "cancel_order() in");
		}
		// TODO Auto-generated method stub
		ShowOrderActivity.this.finish();
		// returnToABCHome(this);
	}

	private void sendOrderInfoToServer() {
		Message msg = NetworkHelper.instance().getNetworkHandler()
				.obtainMessage(NetworkTaskHandler.SEND_ORDER_TO_SERVER);
		// Message msg = mSendOrderHandler
		// .obtainMessage(NetworkTaskHandler.RRTRIEVE_METADATA_FROM_SERVER);
		msg.sendToTarget();
	}

	/*
	 * this method return to the Home of the ABCRestaurant and the
	 */
	private void returnToABCHome(Context context) {
		try {
			Intent goHomeIntent = new Intent("android.intent.action.goABCHome");
			context.startActivity(goHomeIntent);
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.e(TAG, "The ABC Home activity is not found!");
		}
	}

}
