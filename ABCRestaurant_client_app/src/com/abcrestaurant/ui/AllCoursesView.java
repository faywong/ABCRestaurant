/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abcrestaurant.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.graphics.Rect;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.abcrestaurant.R;
import com.abcrestaurant.common.ABCConfig;
import com.abcrestaurant.common.ABCLogSet;
import com.abcrestaurant.common.Category;
import com.abcrestaurant.common.CourseCollector;
import com.abcrestaurant.common.CourseInfo;
import com.abcrestaurant.utils.lazylist.ImageLoader;

public class AllCoursesView
        extends RelativeLayout
        implements AllItemsView,
                   AdapterView.OnItemClickListener,
                   AdapterView.OnItemLongClickListener,
                   View.OnKeyListener,
                   DragSource, DropTarget, DragController.DragListener{

    private static final String TAG = "AllCoursesView";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_DRAG = false;

    public static int UPDATE_WHEN_CLICK = 0x0001;
    public static int UPDATE_WHEN_NORMAL = 0x0002;

    private DragController mDragController;
    private ScrollLayout mScrollLayout = null;
    private PageIndicatorView mPageControl;
    public static int APPSPERPAGE = ABCConfig.getAppNOAllCourcesSrc();
    public static int COLSPERPAGE = ABCConfig.getAppNOAllCourcesSrc() / ABCConfig.getRowNOAllCourcesSrc();
    private ArrayList<GridView> mGridList = null;
    private ArrayList<CoursesAdapter> mAdpList = null;
    private int lastLongClickPos = 0;


    private static final Collator sCollator = Collator.getInstance();
    private static final Comparator<CourseInfo> APP_NAME_COMPARATOR
            = new Comparator<CourseInfo>() {
        public final int compare(CourseInfo a, CourseInfo b) {
            return sCollator.compare(a.name.toString(), b.name.toString());
        }
    };
    private LayoutInflater mInflater;

    private ArrayList<CourseInfo> mAllCoursesList = new ArrayList<CourseInfo>();
    private ArrayList<CourseInfo> mCurCategoryCources = new ArrayList<CourseInfo>();

    // preserve compatibility with 3D all apps:
    //    0.0 -> hidden
    //    1.0 -> shown and opaque
    //    intermediate values -> partially shown & partially opaque
    private float mZoom;

    public static class CheckoutButton extends ImageButton {
        public CheckoutButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        public View focusSearch(int direction) {
            if (direction == FOCUS_UP) return super.focusSearch(direction);
            return null;
        }
    }

    public class CoursesAdapter extends ArrayAdapter<CourseInfo> {
            private final LayoutInflater mInflater;
            private ImageLoader mImageLoader;

            private ArrayList<CourseInfo> mList;
            public final int APP_NO = ABCConfig.getAppNOAllCourcesSrc();
            public final int ROW_NO = ABCConfig.getRowNOAllCourcesSrc();



            public CoursesAdapter(Context context, ArrayList<CourseInfo> list, int page) {
                super(context, 0, list);
                mInflater = LayoutInflater.from(context);
                mImageLoader = new ImageLoader(context);

                mList = new ArrayList<CourseInfo>();
                int i = page * APP_NO;
                int iEnd = i+APP_NO;
                while ((i<list.size()) && (i<iEnd)) {
                    mList.add(list.get(i));
                    if (DEBUG) {
                        Log.e(TAG, "in CoursesAdapter(), list.get("+ i + "):" + "title-->" + list.get(i).name);
                    }
                    i++;
                }
            }

            public String toString() {
                String result = "";
                for (CourseInfo ai : mList) {
                    result += ("application title:" + ai.name + "\n");
                }
                return result;
            }

            public boolean swap(int src, int dst) {
                if (DEBUG)
                Log.e(TAG, "swap() in, src:" + src + " dst:" + dst);

                if (src == dst) {
                    return true;
                }
                CourseInfo srcApp = getItem(src);
                CourseInfo dstApp = getItem(dst);

                if (null == srcApp || null == dstApp) {
                     return false;
                }
                if (DEBUG) {
                    Log.e(TAG, "before swap() , srcAppTitle:" + srcApp.name.toString()+ " dstAppTitle:" + dstApp.name.toString());
                }
                mList.set(dst, srcApp);
                mList.set(src, dstApp);
                if (DEBUG) {
                    Log.e(TAG, "after swap() , srcAppTitle:" + getItem(src).name.toString()+ " dstAppTitle:" + getItem(dst).name.toString());
                }
                return true;
            }

            public int getCount() {
                // TODO Auto-generated method stub
                return mList.size();
            }

            public CourseInfo getItem(int position) {
                // TODO Auto-generated method stub
                return mList.get(position);
            }

            public long getItemId(int position) {
                // TODO Auto-generated method stub
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                // TODO Auto-generated method stub

                        final CourseInfo info = mList.get(position);

                        if (convertView == null) {

                            if (DEBUG) {
                                Log.d(TAG, "in getView() convertView is null");
                            }


                            if (null == convertView) {
                            	//convertView = itemLayout.findViewById(R.id.courseItem);
                            	if (DEBUG) {
                            		Log.e(TAG, "in getView() inflate from courseitem layout");
                            	}
                            	convertView = (LinearLayout) mInflater.inflate(R.layout.courseitem, parent, false);
                            }
                        }

//                        final TextView textView = (TextView) convertView.findViewById(R.id.courseItem);
//                        if (DEBUG) {
//                            Log.d(TAG, "icon bitmap = " + info.getIconBitmap()
//                                + " density = " + info.getIconBitmap().getDensity());
//                        }
//                        info.getIconBitmap().setDensity(Bitmap.DENSITY_NONE);
//                        textView.setPadding(0, 10, 0, 0);
//                        textView.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(info.getIconBitmap()), null, null);
//                        textView.setText(info.name);

                        ImageView courseImg = (ImageView) convertView.findViewById(R.id.courseImg);
                        mImageLoader.DisplayImage(info.getImgURL(), courseImg);
                        TextView courseName =  (TextView) convertView.findViewById(R.id.courseName);
                        courseName.setText(info.getName());
                        updateSelectedState((FrameLayout)convertView.findViewById(R.id.courseItemLayout), info, UPDATE_WHEN_NORMAL);
                        return convertView;

            }
    }

    public AllCoursesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        //setVisibility(View.GONE);
        setSoundEffectsEnabled(false);
        mScrollLayout = (ScrollLayout) findViewById(R.id.all_courses_scroll);
        if (null == mAdpList) {
            mAdpList = new ArrayList<CoursesAdapter>();
        }

        //mCoursesAdapter = new CoursesAdapter(getContext(), mAllCoursesList);
        //mCoursesAdapter.setNotifyOnChange(false);
    }

    @Override
    protected void onFinishInflate() {
       if (DEBUG) {
	    Log.e(TAG, "onFinishInflate() in");
       }
        setBackgroundColor(Color.BLACK);
    }

    public AllCoursesView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }


    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (!isVisible()) return false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
//                mLauncher.closeAllApps(true);
                break;
            default:
                return false;
        }

        return true;
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	FrameLayout courseItemLayout = (FrameLayout)v.findViewById(R.id.courseItemLayout);
    	if(ABCLogSet.DEBUG) {
	    	Log.e(TAG, "the parent is " + parent + " the view item is "
	    			+ v + " position:" + position + " row id " + id);
    	}
    	CourseInfo ci = (CourseInfo)parent.getItemAtPosition(position);
    	if(ABCLogSet.DEBUG) {
	    	Log.e(TAG, "the ci is " + ci);
    	}
    	updateSelectedState(courseItemLayout, ci, UPDATE_WHEN_CLICK);
		/*
    	TextView checkedTextView = (TextView) mInflater.inflate(R.layout.courseitemcheckedview, null);
    	if (!ci.isSelected()) {
	    	checkedTextView.setText("Selected!");
	    	courseItemLayout.addView(checkedTextView);

	    	ci.setSelected(true);
    	}else {
        	ci.setSelected(false);
        	courseItemLayout.removeViewAt(1);
    	}*/

    }

    private void updateSelectedState(FrameLayout itemLayout, CourseInfo ci, int flag) {
        if (DEBUG) {
            Log.d(TAG, "updateSelectedState() in flags:" + flag);
        }

//    	TextView checkedTextView = (TextView) mInflater.inflate(R.layout.courseitemcheckedview, null);
        ImageView checkedTextView = (ImageView) mInflater.inflate(R.layout.courseitemcheckedview, null);

        if (DEBUG) {
            Log.d(TAG, "ci.isSelected():" + ci.isSelected());
        }

    	if (!ci.isSelected()) {
    		if (UPDATE_WHEN_NORMAL == (flag & UPDATE_WHEN_NORMAL)) {
	        	if (itemLayout.getChildCount() > 1) {
	        		itemLayout.removeViewAt(itemLayout.getChildCount() - 1);
	        	}
    		}

	    	if (UPDATE_WHEN_CLICK == (flag & UPDATE_WHEN_CLICK)) {
	            if (DEBUG) {
	                Log.d(TAG, "set the selected flag to true when CLICK Event");
	            }
	            ci.setSelected(true);
//		    	checkedTextView.setText("Selected!");
		    	itemLayout.addView(checkedTextView);
	    	}
    	}else {
    		if (UPDATE_WHEN_NORMAL == (flag & UPDATE_WHEN_NORMAL)) {
//		    	checkedTextView.setText("Selected!");
		    	itemLayout.addView(checkedTextView);
    		}else if (UPDATE_WHEN_CLICK == (flag & UPDATE_WHEN_CLICK)) {
	            if (DEBUG) {
	                Log.d(TAG, "set the selected flag to false when CLICK Event, childCount:" + itemLayout.getChildCount());
	            }
	        	ci.setSelected(false);
	        	if (itemLayout.getChildCount() > 1) {
	        		itemLayout.removeViewAt(itemLayout.getChildCount() - 1);
	        	}
	    	}

    	}
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!view.isInTouchMode()) {
            return false;
        }
        lastLongClickPos = position;
		// CourseInfo app = (CourseInfo) parent.getItemAtPosition(position);

		// if (DEBUG) {
		// Log.e(TAG, "onItemLongClick() In position:" + position +
		// " app-title:" + app.title.toString());
		// }
		// mDragController.startDrag(view, this, app,
		// DragController.DRAG_ACTION_COPY);
		// mDragController.startDrag(view, this, app,
		// DragController.DRAG_ACTION_MOVE);
		// mLauncher.closeAllApps(true);
		// mDragController.removeDragListener(null);
		// mDragController.setDragListener(this);

        return true;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, android.graphics.Rect prev) {

        if (gainFocus) {
            //mGrid.requestFocus();
            if ((null != mGridList) && (null != mScrollLayout)) {
                GridView tmp = mGridList.get(mScrollLayout.getCurScreen());
                if (null != tmp) {
                    tmp.requestFocus();
                }
            }
        }
    }

    public void setDragController(DragController dragger) {
        mDragController = dragger;
    }

    public void onDropCompleted(View target, boolean success) {
    }

    /**
     * Zoom to the specifed level.
     *
     * @param zoom [0..1] 0 is hidden, 1 is open
     */
    public void zoom(float zoom, boolean animate) {
        if (DEBUG && null != mGridList) {
            Log.e(TAG, "zoom() In listsize:" + mGridList.size());
        }

//     Log.d(TAG, "zooming " + ((zoom == 1.0) ? "open" : "closed"));
        cancelLongPress();

        mZoom = zoom;



        if (isVisible()) {
            getParent().bringChildToFront(this);
            setVisibility(View.VISIBLE);
            //mGrid.setAdapter(mCoursesAdapter);

            if (DEBUG) {
                Log.e(TAG, "mScrollLayout:" + mScrollLayout + " mGridList:" + mGridList);
            }

            if (animate) {
                startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.all_apps_2d_fade_in));
            } else {
                onAnimationEnd();
            }
        } else {
            if (animate) {
                startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.all_apps_2d_fade_out));
            } else {
                onAnimationEnd();
            }
        }
    }

    protected void onAnimationEnd() {
        if (!isVisible()) {
            setVisibility(View.GONE);
            //mGrid.setAdapter(null);
            mZoom = 0.0f;
        } else {
            mZoom = 1.0f;
        }

//        mLauncher.zoomed(mZoom);
    }

    public boolean isVisible() {
        return mZoom > 0.001f;
    }

    @Override
    public boolean isOpaque() {
        return mZoom > 0.999f;
    }

    public void setCourses(ArrayList<CourseInfo> list) {
        mAllCoursesList.clear();
        addCourses(list);
    }

    public void addCourses(ArrayList<CourseInfo> list) {
        if (DEBUG && (null != list)) {
            Log.e(TAG, "addApps: " + list.size() + " apps: " + list.toString());
        }

        if (null == list) {
        	return;
        }

        final int N = list.size();

        for (int i=0; i<N; i++) {
            final CourseInfo item = list.get(i);
            int index = Collections.binarySearch(mAllCoursesList, item,
                    APP_NAME_COMPARATOR);
            if (index < 0) {
                index = -(index+1);
            }
            if (DEBUG) Log.e(TAG, "mAllCoursesList.add() index:" + index);
            mAllCoursesList.add(index, item);
        }

      if ((DEBUG) && (null != mGridList)) {
            Log.e(TAG, "added finish gridlist NO. is :" + mGridList.size());
      }

        //mCoursesAdapter.notifyDataSetChanged();
        adaptAll(AllCoursesView.UPDATE_WHEN_NORMAL);
    }

    public void adaptAll(int reason) {
    		  Log.e(TAG, "adaptAll() in");
              if (null == mAllCoursesList || mAllCoursesList.isEmpty()) {
		            if (DEBUG) {
		                Log.e(TAG, "Return when mAllCoursesList:" + mAllCoursesList + " mAllCoursesList.isEmpty:" + mAllCoursesList.isEmpty());
		            }
		            return;
              }

              if (null == mScrollLayout) {
                  mScrollLayout = (ScrollLayout) findViewById(R.id.all_courses_scroll);
              }

              if (null == mGridList) {
                  mGridList = new ArrayList<GridView>();
                  mGridList.clear();
              }

              if (null != mScrollLayout) {
                  mScrollLayout.removeAllViews();
              }

              if (DEBUG) Log.e(TAG, "Before build mAllCoursesList.size() is:" + mAllCoursesList.size());

              Category requstCatgegory = CourseCollector.instance().getCurrentCategory();
              if (null == requstCatgegory) {
            	  return;
              }

              if (!mCurCategoryCources.isEmpty()) {
            	  mCurCategoryCources.clear();
              }

              for (CourseInfo course : mAllCoursesList) {
            	  if(requstCatgegory.isInThisCagegory(course)) {
            		  mCurCategoryCources.add(course);
            	  }
              }

              //CourseCategoryCollector.instance().getCurrentCategory();
              if (DEBUG) Log.e(TAG, "After filter mCurCategoryCources.size() is:" + mCurCategoryCources.size());

              int totalPageNo =  (int)((mCurCategoryCources.size() + (APPSPERPAGE - 1)) / APPSPERPAGE) ;

              if (DEBUG) Log.e(TAG, "totalPageNo is:"+ totalPageNo);

              if (null == mAdpList) {
                  mAdpList = new ArrayList<CoursesAdapter>();
              }

              mAdpList.clear();

              for (int i = 0; i < totalPageNo; i++) {
                  if (DEBUG) Log.e(TAG, "Building Page-->"+ i);
                  GridView appGridView = new GridView(getContext());
                  CoursesAdapter tmpAdp = null;

                  tmpAdp = new CoursesAdapter(getContext(), mCurCategoryCources, i);

                  appGridView.setAdapter(tmpAdp);
                  appGridView.setFocusable(false);
                  appGridView.setNumColumns(COLSPERPAGE);
                  appGridView.setOnItemClickListener(this);
                  appGridView.setOnItemLongClickListener(this);
                  appGridView.setBackgroundColor(Color.BLACK);
                  appGridView.setCacheColorHint(Color.BLACK);
                  if (null != mScrollLayout) {
                      mScrollLayout.addView(appGridView);
                      mGridList.add(appGridView);
                  }
                  if (DEBUG) {
                      Log.e(TAG, "\n**************************************************");
                      if (DEBUG) Log.e(TAG, "Building AppAdapter-->"+ i);
                      Log.e(TAG, "the tmpAdp is " + tmpAdp);
                      Log.e(TAG, "**************************************************\n");
                  }
                  mAdpList.add(tmpAdp);
              }

              mPageControl = (PageIndicatorView) findViewById(R.id.pageIndicator);
              if (null == mPageControl) throw new Resources.NotFoundException();
              if (null != mScrollLayout) {
                  mPageControl.bindScrollViewGroup(mScrollLayout);
              }
    }

    public void removeCourses(ArrayList<CourseInfo> list) {
        final int N = list.size();
        for (int i=0; i<N; i++) {
            final CourseInfo item = list.get(i);
            int index = findAppByComponent(mAllCoursesList, item);
            if (index >= 0) {
                mAllCoursesList.remove(index);
            } else {
                if (DEBUG) Log.w(TAG, "couldn't find a match for item \"" + item + "\"");
                // Try to recover.  This should keep us from crashing for now.
            }
        }
        //mCoursesAdapter.notifyDataSetChanged();
        adaptAll(AllCoursesView.UPDATE_WHEN_NORMAL);

    }

    public void updateCourses(ArrayList<CourseInfo> list) {
        // Just remove and add, because they may need to be re-sorted.
        removeCourses(list);
        addCourses(list);
    }

    private static int findAppByComponent(ArrayList<CourseInfo> list, CourseInfo item) {
        ComponentName component = item.getIntent().getComponent();
        final int N = list.size();
        for (int i=0; i<N; i++) {
            CourseInfo x = list.get(i);
            if (x.getIntent().getComponent().equals(component)) {
                return i;
            }
        }
        return -1;
    }

    public void dumpState() {
        CourseInfo.dumpCourseInfoList(TAG, "mAllCoursesList", mAllCoursesList);
    }

    public void surrender() {

    }

    /* implement DragListener interface
      *  onDragStart() & onDragEnd()
      * implemented by wangfei<wang.fei@pset.suntec.net> on 2011-11-30
      */

    /* A drag has begun */
    public void onDragStart(DragSource source, Object info, int dragAction) {
        /*
        RelativeLayout homeArrowSet = (RelativeLayout) findViewById(R.id.all_apps_button_cluster);
        if (null != homeArrowSet) {
            homeArrowSet.setVisibility(VISIBLE);
        }
        if (DEBUG_DRAG) {
            Log.e(TAG, "onDragStart() called");
        }
        */
    }

    /**
     * The drag has eneded
     */
    public void onDragEnd() {
    /*
        RelativeLayout homeArrowSet = (RelativeLayout) findViewById(R.id.all_apps_button_cluster);
        if (null != homeArrowSet) {
            homeArrowSet.setVisibility(VISIBLE);
        }
    */
        if (DEBUG_DRAG) {
            Log.d(TAG, "onDragEnd() called");
        }
    }

    /*
      * implement DropTarget interface
      * implemented by wangfei<wang.fei@pset.suntec.net> on 2011-11-30
      */

   public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
                DragView dragView, Object dragInfo) {

            if (DEBUG) {
                Log.d(TAG, "allapps2d onDrop() in");
            }


            if (source != this) {
                if (DEBUG_DRAG) {
                    Log.e(TAG, "can't drop to other scrolllayout");
                }
                return;
            }

            int curSrnInx = mScrollLayout.getCurScreen();
            int srcInx = lastLongClickPos;

            GridView appGridView = mGridList.get(curSrnInx);
            if (null == appGridView) {
                if (DEBUG_DRAG) {
                    Log.e(TAG, "cannot get the gridview(index:" + curSrnInx + ")");
                }
                return;
            }
            int dstInx = appGridView.pointToPosition(x, y);
            if (AdapterView.INVALID_POSITION == dstInx) {
                if (DEBUG_DRAG) {
                    Log.e(TAG, "the drop point does not intersect an item");
                }
                return;
            }

            if (DEBUG_DRAG) {
                Log.e(TAG, "the app swap(srcInx:" + srcInx + " dstInx:" + dstInx + ")" + " curSrnInx:" + curSrnInx);
            }

            if (null != mAdpList && null != mScrollLayout) {
                CoursesAdapter tmpAppAdapter = mAdpList.get(curSrnInx);
                if (DEBUG) {
                    Log.e(TAG, "the size of mAdpList is:" + mAdpList.size());
                    for (int i = 0; i < mAdpList.size(); i++) {
                        Log.e(TAG, "\n---------------------------------------------");
                        Log.e(TAG, "the "+ i + "th AppAdapter is:\n" + mAdpList.get(i));
                        Log.e(TAG, "---------------------------------------------\n");
                    }
                    Log.e(TAG, "the CoursesAdapter of current screen(index:"+ curSrnInx +") is:\n" + tmpAppAdapter);
                }
                if (null == tmpAppAdapter) {
                    return;
                }
                tmpAppAdapter.swap(srcInx, dstInx);
                tmpAppAdapter.notifyDataSetChanged();
            }

        }

    public boolean acceptDrop(DragSource source, int x, int y,
            int xOffset, int yOffset, DragView dragView, Object dragInfo) {

            if (DEBUG) {
                Log.d(TAG, "allapps2d acceptDrop() in");
            }

            if (!isVisible() || source != this) {
                if (DEBUG_DRAG) {
                    Log.e(TAG, "in acceptDrop(), is not visible now, so can't accept drop");
                }
                return false;
            }

            int scroWidth = mScrollLayout.getWidth();
            int scroHeight = mScrollLayout.getHeight();
            if (0 == scroWidth || 0 == scroHeight) {
                Log.e(TAG, "ScrollLayout has not been inflatten");
                return false;
            }

            if (DEBUG_DRAG) {
                Log.e(TAG, "in acceptDrop(), scroWidth:" + scroWidth + " scroHeight:" + scroHeight);
            }
            int appNo = ABCConfig.getAppNOAllCourcesSrc();
            int rowNo = ABCConfig.getRowNOAllCourcesSrc();

            int xGate = scroWidth * rowNo / appNo / 3;
            int yGate = scroHeight / rowNo / 3;
            if (DEBUG_DRAG) {
                Log.e(TAG, "in acceptDrop(), xGate:" + xGate + " yGate:" + yGate + " xOffset:" + xOffset + " yOffset:" + yOffset);
                Log.e(TAG, "in acceptDrop(), result is " + ( (xOffset >= xGate || yOffset >= yGate)
                            == true ? "true" : "false"));
            }

            return xOffset >= xGate || yOffset >= yGate;
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }


    public Rect estimateDropLocation(DragSource source, int x, int y,
              int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
            return new Rect();
    }
}
