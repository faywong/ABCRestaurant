package com.abcrestaurant.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.abcrestaurant.R;
import com.abcrestaurant.ui.ScrollLayout.OnScreenChangeListener;
import android.util.Log;

public class PageIndicatorView extends LinearLayout {
	private Context context;
       public static final boolean DEBUG = false;
       public static final String TAG = "PageControlView";

	private int count;

	public void bindScrollViewGroup(ScrollLayout scrollViewGroup) {
		this.count=scrollViewGroup.getChildCount();
		generatePageControl(scrollViewGroup.getCurScreen());
		
		scrollViewGroup.setOnScreenChangeListener(new OnScreenChangeListener() {
			
			public void onScreenChange(int currentIndex) {
				// TODO Auto-generated method stub
				generatePageControl(currentIndex);
			}
		});
	}

	public PageIndicatorView(Context context) {
		super(context);
		this.init(context);
	}
	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context);
	}

	private void init(Context context) {
		this.context=context;
	}

	private void generatePageControl(int currentIndex) {
              if (DEBUG) {
                 Log.e(TAG, "generatePageControl() in with inpara: currentIndex: " + currentIndex);
              }

              int pageNum = this.count;
              if ((currentIndex > (pageNum - 1)) ||(pageNum < 1)) {
                    if(DEBUG) Log.e(TAG, "pageNum:" + pageNum + " currentIndex:" + currentIndex);
                    return;
              }
               
              /* everytime when apk list changed, re-construct the page-control view */
		this.removeAllViews();
    
            for (int i = 0; i < pageNum; i ++) {
                ImageView imageView = new ImageView(context);
                if(i  != currentIndex) {
                    if (DEBUG) Log.e(TAG, "setImageResource page_indicator");
                    imageView.setImageResource(R.drawable.screen_indicator_white);
                }else {
                    if (DEBUG) Log.e(TAG, "setImageResource page_indicator_focused");
                    imageView.setImageResource(R.drawable.screen_indicator_red);

                }
                this.addView(imageView);
            }

	}
}

