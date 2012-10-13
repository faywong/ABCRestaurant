package com.abcrestaurant.common;

import java.util.ArrayList;

import com.abcrestaurant.common.Category;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

public class CourseInfo {
	private static final String TAG = "CourseInfo";
    /**
     * The cource name.
     */
    public CharSequence name;

    private int UUID;
    private Category category;
    private boolean selected = false;
    private float price;

    private String imgURL;

    private int num = 0;

	/**
     * The intent used to display the details of the cource.
     */
    private Intent intent;

    /**
     * A bitmap version of the application icon.
     */
    private Bitmap iconBitmap;

    CourseDetail details;
    /*Populate from local resource version*/
    public CourseInfo(CharSequence name, int uUID, Category category,
			 Intent intent, Bitmap iconBitmap,
			CourseDetail details, float price) {
		super();
		this.name = name;
		UUID = uUID;
		this.setCategory(category);
		this.intent = intent;
		this.iconBitmap = iconBitmap;
		this.details = details;
		this.price = price;
	}

    /*Populate from remote server version*/
    public CourseInfo(int uUID, CharSequence name, String imgURL, Category category,
			 Intent intent,
			CourseDetail details, float price) {
		super();
		this.name = name;
		this.UUID = uUID;
		this.setCategory(category);
		this.imgURL = imgURL;
		this.intent = intent;
		this.details = details;
		this.price = price;
	}

    public String getImgURL() {
    	return this.imgURL;
    }
    public void setOrderCount(int orderCount) {
	}

    @Override
	public String toString() {
		// TODO Auto-generated method stub
		return "id:"+ UUID +" name:" + name + " imgURL:" + imgURL + " price:" + price;
	}

	public Bitmap getIconBitmap() {
    	return iconBitmap;
    }

    public Intent getIntent() {
    	return intent;
    }

    public int getID() {
    	return UUID;
    }

    public static void dumpCourseInfoList(String tag, String label,
            ArrayList<CourseInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (CourseInfo info: list) {
            Log.d(tag, "name=\"" + info.name +
                   " iconBitmap=" + info.iconBitmap);
        }
    }
	public boolean isSelected() {
		return selected;
	}

	synchronized public void setSelected(boolean selected) {
		if (ABCLogSet.DEBUG) {
			Log.e(TAG, "setting the selected to " + selected);
		}
		this.selected = selected;

		if (this.selected == true) {
			num = 1;
		}else {
			num = 0;
		}
	}

	synchronized public int getNum() {
		return this.num;
	}

	synchronized public void incNum() {
		++this.num;
	}

	synchronized public void decNum() {
		if (this.num == 0) {
			return;
		}
		if (ABCLogSet.DEBUG) {
			if (this.num < 0) {
				Log.e(ABCLogSet.MAIN_TAG, "the NO. of one course won be less than zero!");
			}
		}
		--this.num;
	}

	public Category getCategory() {
		return category;
	}

	synchronized public void setCategory(Category category) {
		this.category = category;
	}

	public CharSequence getName() {
		return this.name;
	}

	public float getPrice() {
		return this.price;
	}
}
