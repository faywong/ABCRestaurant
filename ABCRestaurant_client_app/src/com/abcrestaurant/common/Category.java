package com.abcrestaurant.common;

import android.util.Log;

public class Category {
	private String name;
	private int id;
	private String imgURL;
	private static boolean DEBUG = ABCLogSet.DEBUG;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Category(name:" + name + " id:" + id + "imgURL: " + imgURL + ")";
	}

	public Category(int _id, String _name, String URL) {
		name = _name;
		id = _id;
		imgURL = URL;
	}

	public String getImgURL() {
		return imgURL;
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}

	public boolean isInThisCagegory(CourseInfo course) {
		/*the category with id = 0 is a special id to represent any category*/
		if (DEBUG ) {
			Log.d("isInThisCagegory", "id: " + id + " res: "  + ((id == 0) || (course.getCategory().getID() == this.id)));
		}
		return (id == 0) || (course.getCategory().getID() == this.id);
	}

}