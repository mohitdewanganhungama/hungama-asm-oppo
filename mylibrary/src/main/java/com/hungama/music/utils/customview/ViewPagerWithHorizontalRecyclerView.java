package com.hungama.music.utils.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.hungama.music.utils.CommonUtils;

public class ViewPagerWithHorizontalRecyclerView extends ViewPager {

    public ViewPagerWithHorizontalRecyclerView(Context context) {
        super(context);
    }

    public ViewPagerWithHorizontalRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v instanceof RecyclerView){
            CommonUtils.INSTANCE.setLog("PAGER", "IS");
            return false;
        } else {
            CommonUtils.INSTANCE.setLog("PAGER", "IS NOT " + v.toString());
        }

        return super.canScroll(v, checkV, dx, x, y);
    }
}