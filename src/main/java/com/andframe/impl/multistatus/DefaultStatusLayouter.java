package com.andframe.impl.multistatus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.andframe.R;
import com.andframe.api.multistatus.OnRefreshListener;
import com.andframe.api.multistatus.StatusLayouter;
import com.andframe.module.AfFrameSelector;

/**
 * 可切换状态页面的布局
 * Created by SCWANG on 2016/10/20.
 */

public class DefaultStatusLayouter implements StatusLayouter {

    private final FrameLayout mFrameLayout;
    private final AfFrameSelector mFrameSelector;
    private View mContentView;
    private View mEmptyLayout;
    private View mErrorLayout;
    private View mProgressLayout;
    private View mInvalidnetLayout;
    private OnRefreshListener mOnRefreshListener;
    private View.OnClickListener mOnRefreshClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnRefreshListener != null && mOnRefreshListener.onRefresh()) {
                showProgress();
            }
        }
    };

    public DefaultStatusLayouter(Context content) {
        this(new FrameLayout(content));
    }

    public DefaultStatusLayouter(FrameLayout frameLayout) {
        mFrameLayout = frameLayout;
        mFrameSelector = new AfFrameSelector(frameLayout);
        if (mFrameLayout.getChildCount() > 0) {
            mContentView = mFrameLayout.getChildAt(0);
        }
    }

    @Override
    public ViewGroup getLayout() {
        return mFrameLayout;
    }

    //<editor-fold desc="视图初始化">
    @Override
    public void setContenView(View content) {
        if (mContentView != null) {
            mFrameLayout.removeView(mContentView);
        }
        mFrameLayout.addView(content);
        mContentView = content;
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    @Override
    public void setProgressLayoutId(int progressLayoutId) {
        if (mProgressLayout != null) {
            mFrameLayout.removeView(mProgressLayout);
        }
        mProgressLayout = LayoutInflater.from(mFrameLayout.getContext()).inflate(progressLayoutId, null);
        mFrameLayout.addView(mProgressLayout);
//        mProgressLayout = View.inflate(mFrameLayout.getContext(), progressLayoutId, mFrameLayout);
    }

    @Override
    public void setEmptyLayoutId(int emptyLayoutId) {
        if (mEmptyLayout != null) {
            mFrameLayout.removeView(mEmptyLayout);
        }
        mEmptyLayout = LayoutInflater.from(mFrameLayout.getContext()).inflate(emptyLayoutId, null);
        mEmptyLayout.setOnClickListener(mOnRefreshClickListener);
        mFrameLayout.addView(mEmptyLayout);
    }

    @Override
    public void setErrorLayoutId(int errorLayoutId) {
        if (mErrorLayout != null) {
            mFrameLayout.removeView(mErrorLayout);
        }
        mErrorLayout = LayoutInflater.from(mFrameLayout.getContext()).inflate(errorLayoutId, null);
        mErrorLayout.setOnClickListener(mOnRefreshClickListener);
        mFrameLayout.addView(mErrorLayout);
    }

    @Override
    public void setInvalidnetLayoutId(int invalidnetLayoutId) {
        if (mInvalidnetLayout != null) {
            mFrameLayout.removeView(mInvalidnetLayout);
        }
        mInvalidnetLayout = LayoutInflater.from(mFrameLayout.getContext()).inflate(invalidnetLayoutId, null);
        mInvalidnetLayout.setOnClickListener(mOnRefreshClickListener);
        mFrameLayout.addView(mInvalidnetLayout);
    }

    @Override
    public void autoCompletedLayout() {
        if (mEmptyLayout == null) {
            setEmptyLayoutId(R.layout.af_module_nodata);
        }
        if (mErrorLayout == null) {
            mErrorLayout = mEmptyLayout;
        }
        if (mProgressLayout == null) {
            setProgressLayoutId(R.layout.af_module_progress);
        }
    }
    //</editor-fold>

    //<editor-fold desc="视图切换">
    @Override
    public void showEmpty() {
        if (mEmptyLayout != null) {
            mFrameSelector.selectFrame(mEmptyLayout);
        }
    }

    @Override
    public void showContent() {
        if (mContentView != null) {
            mFrameSelector.selectFrame(mContentView);
        }
    }

    @Override
    public void showProgress() {
        if (mProgressLayout != null) {
            mFrameSelector.selectFrame(mProgressLayout);
        }
    }

    @Override
    public void showError(String error) {
        if (mErrorLayout != null) {
            mFrameSelector.selectFrame(mErrorLayout);
        }
    }

    @Override
    public boolean isProgress() {
        return mProgressLayout != null && mFrameSelector.isCurrent(mProgressLayout);
    }
    //</editor-fold>
}
