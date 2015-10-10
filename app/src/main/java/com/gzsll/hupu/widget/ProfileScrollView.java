package com.gzsll.hupu.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.gzsll.hupu.R;
import com.gzsll.hupu.support.storage.bean.UserInfo;
import com.gzsll.hupu.support.utils.SystemBarTintManager;
import com.gzsll.hupu.ui.activity.BaseActivity;

/**
 * Created by sll on 2015/9/14.
 */
public class ProfileScrollView extends ScrollView {
    public ProfileScrollView(Context context) {
        super(context);
        init();
    }

    public ProfileScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private static final int INVALID_POINTER = -1;

    private LinearLayout child;
    private View layTop;
    private View tabs;
    private ViewPager viewPager;
    private View viewToolbar;
    private View ivCover;

    private View refreshView;
    private int mActivePointerId = INVALID_POINTER;
    private float mInitialMotionY;
    private int action_size;

    private BaseActivity activity;
    private UserInfo mUser;
    private SystemBarTintManager manager;

    private void init() {
        activity = (BaseActivity) getContext();
        manager = new SystemBarTintManager(activity);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (getChildCount() > 0) {
            if (getChildAt(0).getHeight() > 0 && child == null) {
                child = (LinearLayout) getChildAt(0);
            }
        }

        int themeColor = resolveColor(R.attr.colorPrimary, Color.BLUE);

        if (child != null && child.getHeight() > 0 && tabs == null) {
            tabs = child.getChildAt(1);
            tabs.setBackgroundColor(themeColor);
            viewPager = (ViewPager) child.getChildAt(2);

            layTop = activity.findViewById(R.id.layTop);
            layTop.setBackgroundColor(themeColor);
            viewToolbar = activity.findViewById(R.id.viewToolbar);
            if (viewToolbar != null)
                viewToolbar.setBackgroundColor(themeColor);
            ivCover = activity.findViewById(R.id.ivCover);
            activity.findViewById(R.id.viewBgDes).setBackgroundColor(themeColor);

            action_size = activity.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
            int statusBar = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                statusBar = manager.getConfig().getStatusBarHeight();
            }


            viewPager.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getHeight() - tabs.getHeight() - action_size - statusBar));
        }
    }

    public int resolveColor(@AttrRes int attr, int fallback) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (viewToolbar != null)
            viewToolbar.setAlpha(Math.abs(t * 1.0f / (ivCover.getHeight() - action_size)));

        // 设置显示Actionbar的title
        if (activity != null) {
            if (viewToolbar != null && viewToolbar.getAlpha() >= 0.75f) {
                activity.getSupportActionBar().setTitle(mUser.getUsername());
            } else {
                activity.getSupportActionBar().setTitle("");
            }
        }
    }

    public void setAbsListView(View refreshView) {
        this.refreshView = refreshView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (refreshView != null) {
            boolean canChildScrollUp = ViewCompat.canScrollVertically(refreshView, -1);
            if (canChildScrollUp) {
                return false;
            }
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                if (yDiff < 0) {
                    if (getChildAt(0).getMeasuredHeight() <= getHeight() + getScrollY()) {
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    public void setUser(UserInfo user) {
        this.mUser = user;
    }
}
