package com.abcrestaurant.common;

import com.abcrestaurant.network.NetworkTaskHandler;

import android.os.Handler;
import android.os.HandlerThread;

public class NetworkHelper {
	private static NetworkHelper sStatus = null;
	private Handler mNetworkHandler = null;
	private HandlerThread mNetworkThread = null;
	public static NetworkHelper instance() {
		if (null == sStatus) {
			sStatus = new NetworkHelper();
		}
		return sStatus;
	}

	private NetworkHelper() {
		if (null == mNetworkThread) {
			mNetworkThread = new HandlerThread("network_task_thread");
			mNetworkThread.start();
		}

		if (null == mNetworkHandler) {
			mNetworkHandler = new NetworkTaskHandler(
					mNetworkThread.getLooper());
		}

	}

	public Handler getNetworkHandler() {
		return mNetworkHandler;
	}
}
