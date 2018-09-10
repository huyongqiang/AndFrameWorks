package com.andframe.impl.helper;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.andframe.R;
import com.andframe.activity.AfStatusActivity;
import com.andframe.annotation.pager.status.StatusContentViewId;
import com.andframe.annotation.pager.status.StatusContentViewType;
import com.andframe.annotation.pager.status.StatusEmpty;
import com.andframe.annotation.pager.status.StatusError;
import com.andframe.annotation.pager.status.StatusInvalidNet;
import com.andframe.annotation.pager.status.StatusLayout;
import com.andframe.annotation.pager.status.StatusProgress;
import com.andframe.annotation.pager.status.idname.StatusContentViewId$;
import com.andframe.api.pager.status.LayoutManager;
import com.andframe.api.pager.status.RefreshManager;
import com.andframe.api.pager.status.StatusHelper;
import com.andframe.api.pager.status.StatusManager;
import com.andframe.api.pager.status.StatusPager;
import com.andframe.api.task.Task;
import com.andframe.application.AfApp;
import com.andframe.exception.AfExceptionHandler;
import com.andframe.fragment.AfStatusFragment;
import com.andframe.util.internal.TAG;
import com.andframe.util.java.AfReflecter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 多状态页面支持
 * Created by SCWANG on 2016/10/22.
 */

public class AfStatusHelper<T> extends AfLoadHelper<T> implements StatusHelper<T> {

    protected StatusPager<T> mPager;

    protected StatusManager mStatusManager;

    public AfStatusHelper(StatusPager<T> pager) {
        super(pager);
        this.mPager = pager;
    }

    @CallSuper
    public void onViewCreated()  {
        View content = mPager.findContentView();
        if (content != null && checkContentViewStruct(content)) {
            mPager.initRefreshAndStatusManager(content, content);
        }

        if (mLoadOnViewCreated && mModel == null) {
            mLoadOnViewCreated = false;
            if (mPager.postTask(new LoadTask()).status() != Task.Status.canceled) {
                mPager.showStatus(StatusManager.Status.progress);
            }
        } else if (mModel != null) {
            mPager.onTaskSucceed(mModel);
        } else {
            mPager.showStatus(StatusManager.Status.empty);
        }
    }

    //<editor-fold desc="初始化布局">
    public View findContentView() {

        Class<?> stop = mPager instanceof Activity ? AfStatusActivity.class : AfStatusFragment.class;
        StatusContentViewId id = AfReflecter.getAnnotation(mPager.getClass(), stop, StatusContentViewId.class);
        if (id != null) {
            return mPager.findViewById(id.value());
        }
        StatusContentViewId$ id$ = AfReflecter.getAnnotation(mPager.getClass(), stop, StatusContentViewId$.class);
        if (id$ != null) {
            Context context = AfApp.get();
            if (context != null) {
                int idv = context.getResources().getIdentifier(id$.value(), "id", context.getPackageName());
                if (idv > 0) {
                    return mPager.findViewById(idv);
                }
            }
        }
        StatusContentViewType type = AfReflecter.getAnnotation(mPager.getClass(), stop, StatusContentViewType.class);
        if (type != null) {
            return AfApp.get().newViewQuery(mPager).query(type.value()).view();
        }

        return super.findContentView();
    }

    @Override
    public boolean checkContentViewStruct(View content) {
        ViewParent parent = content.getParent();
        if (parent == null) {
            AfExceptionHandler.handle("内容视图（ContentView）没有父视图，刷新布局（RefreshManager/StatusManager）初始化失败",
                    TAG.TAG(mPager, "AfStatusHelper", "checkContentViewStruct"));
            return false;
        } else if (parent instanceof ViewPager) {
            AfExceptionHandler.handle("内容视图（ContentView）父视图为ViewPager，刷新布局（RefreshManager/StatusManager）初始化失败，" +
                            "请用其他布局（Layout）作为ContentView的直接父视图，ViewPager的子视图",
                    TAG.TAG(mPager, "AfStatusHelper", "checkContentViewStruct"));
            return false;
        }
        return true;
    }

    @Override
    public void initRefreshAndStatusManager(@NonNull View refreshContent, @NonNull View statusContent) {
        mRefreshManager = mPager.initRefreshLayout(refreshContent);
        mStatusManager = mPager.initStatusLayout(statusContent);

        if (mStatusManager != null || mRefreshManager != null) {
            if (refreshContent == statusContent) {
                ViewGroup group = (ViewGroup) refreshContent.getParent();
                int i = group.indexOfChild(refreshContent);
                group.removeViewAt(i);
                ViewGroup.LayoutParams params = refreshContent.getLayoutParams();
                mPager.initRefreshAndStatusManagerOrder(mRefreshManager, mStatusManager, refreshContent, group, i, params);
            } else {
                boolean isStatusOtter = true;
                for (ViewParent status = statusContent.getParent() ; status != null; status = status.getParent()) {
                    if (status == refreshContent) {
                        isStatusOtter = false;
                        break;
                    }
                }

                ViewGroup refreshGroup = (ViewGroup) refreshContent.getParent();
                int refreshIndex = refreshGroup.indexOfChild(refreshContent);
                ViewGroup.LayoutParams refreshParams = refreshContent.getLayoutParams();
                if (mRefreshManager != null) {
                    refreshGroup.removeViewAt(refreshIndex);
                    mRefreshManager.setContentView(refreshContent);
                    if (isStatusOtter || mStatusManager == null) {
                        refreshGroup.addView(mRefreshManager.getLayout(),refreshIndex,refreshParams);
                    }
                }

                if (mStatusManager != null) {
                    ViewGroup statusGroup = (ViewGroup) statusContent.getParent();
                    int statusIndex = statusGroup.indexOfChild(statusContent);
                    statusGroup.removeViewAt(statusIndex);
                    ViewGroup.LayoutParams statusParams = statusContent.getLayoutParams();
                    mStatusManager.setContentView(statusContent);
                    statusGroup.addView(mStatusManager.getLayout(),statusIndex,statusParams);
                }

                if (mRefreshManager != null && mStatusManager != null && !isStatusOtter) {
                    refreshGroup.addView(mRefreshManager.getLayout(),refreshIndex,refreshParams);
                }
            }
        }
    }

    @Override
    public void initRefreshAndStatusManagerOrder(LayoutManager refresh, LayoutManager status, View content, ViewGroup parent, int index, ViewGroup.LayoutParams lp) {
        if (refresh != null && status != null) {
            refresh.setContentView(content);
            status.setContentView(refresh.getLayout());
            parent.addView(status.getLayout(), index, lp);
        } else if (refresh != null) {
            refresh.setContentView(content);
            parent.addView(refresh.getLayout(), index, lp);
        } else if (status != null) {
            status.setContentView(content);
            parent.addView(status.getLayout(), index, lp);
        }
    }

    @Override
    public RefreshManager initRefreshLayout(View content) {
        RefreshManager layoutManager = mPager.newRefreshManager(content.getContext());
        layoutManager.setOnRefreshListener(mPager);
        return layoutManager;
    }

    public StatusManager initStatusLayout(View content) {
        StatusManager layoutManager = mPager.newStatusManager(content.getContext());
        layoutManager.setOnRefreshListener(mPager);

        Class<?> stop = mPager instanceof Activity ? AfStatusActivity.class : AfStatusFragment.class;
        StatusLayout status = AfReflecter.getAnnotation(mPager.getClass(), stop, StatusLayout.class);
        if (status != null) {
            layoutManager.setEmptyLayout(status.empty(), status.emptyTxtId());
            layoutManager.setProgressLayout(status.progress(), status.progressTxtId());
            if (status.error() !=  0) {
                layoutManager.setErrorLayout(status.error(), status.errorTxtId());
            }
            if (status.invalidNet() != 0) {
                layoutManager.setInvalidNetLayout(status.invalidNet(), status.invalidNetTxtId());
            }
        } else {
            StatusEmpty empty = combineStatusEmpty(mPager.getClass(), stop);
            StatusError error = combineStatusError(mPager.getClass(), stop);
            StatusProgress progress = combineStatusProgress(mPager.getClass(), stop);
            StatusInvalidNet invalidNet = combineStatusInvalidNet(mPager.getClass(), stop);

            if (empty != null) {
                String message = empty.message();
                if (TextUtils.isEmpty(message) && empty.messageId() !=  0) {
                    message = AfApp.get().getString(empty.messageId());
                }
                layoutManager.setEmptyLayout(empty.value(), empty.txtId(), empty.btnId(), message);
            }
            if (error != null) {
                layoutManager.setErrorLayout(error.value(), error.txtId(), error.btnId());
            }
            if (invalidNet != null) {
                layoutManager.setInvalidNetLayout(invalidNet.value(), invalidNet.txtId(), invalidNet.btnId());
            }
            if (progress != null) {
                layoutManager.setProgressLayout(progress.value(), progress.txtId());
            }
        }
        layoutManager.autoCompletedLayout();
        return layoutManager;
    }

    @NonNull
    public StatusManager newStatusManager(Context context) {
        return AfApp.get().newStatusManager(context);
    }
    //</editor-fold>

    //<editor-fold desc="数据加载">

    @Override
    public boolean isEmpty(T model) {
        if (model instanceof Collection) {
            return ((Collection) model).isEmpty();
        }
        return model == null;
    }

//    @Override
//    public void onTaskFinish(@NonNull Task task, T data) {
//        if (task.success()) {
//            mPager.onTaskSucceed(data);
//        } else {
//            mPager.onTaskFailed(task);
//        }
//    }

    public void onTaskSucceed(T data) {
        if (mPager.isEmpty(data)) {
            mPager.showStatus(StatusManager.Status.empty);
        } else {
//            mPager.showStatus(StatusManager.Status.content);
//            mPager.onTaskLoaded(data);
            mPager.showContent(data);
        }
    }

    public void onTaskFailed(@NonNull Task task) {
        if (mModel != null) {
            mPager.showStatus(StatusManager.Status.content);
            mPager.makeToastShort(task.makeErrorToast(AfApp.get().getString(R.string.status_load_fail)));
        } else {
            if (task.exception() instanceof java.net.BindException ||
                    task.exception() instanceof java.net.NoRouteToHostException ||
                    task.exception() instanceof java.net.SocketException) {
                mPager.showStatus(StatusManager.Status.invalidNet);
            } else {
                try {
                    ConnectivityManager manager;
                    manager = (ConnectivityManager) mPager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (manager != null) {
                        NetworkInfo network = manager.getActiveNetworkInfo();
                        if (network != null && network.getState() == NetworkInfo.State.CONNECTED) {
                            mPager.showStatus(StatusManager.Status.error, task.makeErrorToast(AfApp.get().getString(R.string.status_load_fail)));
                            return;
                        }
                    }
                    mPager.showStatus(StatusManager.Status.invalidNet);
                } catch (Throwable e) {
                    mPager.showStatus(StatusManager.Status.error, task.makeErrorToast(AfApp.get().getString(R.string.status_load_fail)));
                }
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="页面状态">
    @Override
    public void showStatus(StatusManager.Status status, String... msg) {
        switch (status) {
            case other:
                break;
            case progress:
                if (msg.length == 0) {
                    mPager.showProgress();
                } else {
                    mPager.showProgress(msg[0]);
                }
                break;
            case content:
                mPager.showContent();
                break;
            case empty:
                mPager.showEmpty();
                break;
            case error:
                if (msg.length == 0) {
                    mPager.showError("未知错误");
                } else {
                    mPager.showError(msg[0]);
                }
                break;
            case invalidNet:
                mPager.showInvalidNet();
                break;
        }
    }

    public void showEmpty() {
        if (mStatusManager != null && !mStatusManager.isEmpty()) {
            mStatusManager.showEmpty();
        } else if ((mRefreshManager == null || !mRefreshManager.isRefreshing())) {
            mPager.hideProgressDialog();
        }
    }

    @Override
    public void showEmpty(@NonNull String message) {
        if (mStatusManager != null && !mStatusManager.isEmpty()) {
            mStatusManager.showEmpty(message);
        } else if ((mRefreshManager == null || !mRefreshManager.isRefreshing())) {
            mPager.hideProgressDialog();
        }
    }

    public void showContent() {
        if (mStatusManager != null && !mStatusManager.isContent()) {
            mStatusManager.showContent();
        } else if (mRefreshManager != null && mRefreshManager.isRefreshing()) {
            mRefreshManager.finishRefresh(true);
        } else {
            mPager.hideProgressDialog();
        }
    }

    @Override
    public void showContent(@NonNull T model) {
        mPager.showStatus(StatusManager.Status.content);
        mPager.onTaskLoaded(model);
    }

    public void showProgress() {
        if ((mRefreshManager == null || !mRefreshManager.isRefreshing())) {
            if (mStatusManager != null) {
                if (!mStatusManager.isProgress())
                    mStatusManager.showProgress();
            } else {
                mPager.showProgressDialog(AfApp.get().getString(R.string.status_loading));
            }
        }
    }

    @Override
    public void showInvalidNet() {
        if (mStatusManager != null) {
            mStatusManager.showInvalidNet();
        } else {
            mPager.makeToastShort(AfApp.get().getString(R.string.status_invalid_net));
        }
    }

    public void showError(@NonNull String error) {
        if (mRefreshManager != null && mRefreshManager.isRefreshing()) {
            mRefreshManager.finishRefresh(false);
        } else if (mStatusManager == null || !mStatusManager.isProgress()) {
            mPager.hideProgressDialog();
        }
        if (mStatusManager != null) {
            mStatusManager.showError(error);
        } else {
            mPager.makeToastShort(error);
        }
    }

    @Override
    public void showProgress(@NonNull String progress) {
        if (mStatusManager != null) {
            mStatusManager.showProgress(progress);
        } else {
            if (!mPager.isProgressDialogShowing()) {
                mPager.showProgressDialog(progress);
            } else {
                mPager.setProgressDialogText(progress);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Annotation 实现类">

    public <TT extends Annotation> List<TT> getAnnotations(Class<?> type, Class<?> stoptype, Class<TT> annot) {
        List<TT> list = new ArrayList<>();
        while (type != null && !type.equals(stoptype)) {
            if (type.isAnnotationPresent(annot)) {
                list.add(type.getAnnotation(annot));
            }
            type = type.getSuperclass();
        }
        if (type != null && type.equals(stoptype)) {
            if (type.isAnnotationPresent(annot)) {
                list.add(type.getAnnotation(annot));
            }
        }
        Collections.reverse(list);
        return list;
    }

    public StatusEmpty combineStatusEmpty(Class<?> type, Class<?> stoptype) {
        StatusEmptyImpl impl = new StatusEmptyImpl();
        impl.value = R.layout.af_module_nodata;
        impl.txtId = R.id.module_nodata_description;
        List<StatusEmpty> empties = getAnnotations(type, stoptype, StatusEmpty.class);
        for (StatusEmpty tEmpty : empties) {
            impl.combine(tEmpty);
        }
        return impl;
    }

    public StatusError combineStatusError(Class<?> type, Class<?> stoptype) {
        StatusErrorImpl impl = new StatusErrorImpl();
        impl.value = R.layout.af_module_nodata;
        impl.txtId = R.id.module_nodata_description;
        List<StatusError> empties = getAnnotations(type, stoptype, StatusError.class);
        for (StatusError tEmpty : empties) {
            impl.combine(tEmpty);
        }
        return impl;
    }

    public StatusInvalidNet combineStatusInvalidNet(Class<?> type, Class<?> stoptype) {
        StatusInvalidNetImpl impl = new StatusInvalidNetImpl();
        impl.value = R.layout.af_module_nodata;
        impl.txtId = R.id.module_nodata_description;
        impl.message = AfApp.get().getString(R.string.status_invalid_net);
        List<StatusInvalidNet> empties = getAnnotations(type, stoptype, StatusInvalidNet.class);
        for (StatusInvalidNet tEmpty : empties) {
            impl.combine(tEmpty);
        }
        return impl;
    }

    public StatusProgress combineStatusProgress(Class<?> type, Class<?> stoptype) {
        StatusProgressImpl impl = new StatusProgressImpl();
        impl.value = R.layout.af_module_progress;
        impl.txtId = R.id.module_progress_loadinfo;
        List<StatusProgress> empties = getAnnotations(type, stoptype, StatusProgress.class);
        for (StatusProgress tEmpty : empties) {
            impl.combine(tEmpty);
        }
        return impl;
    }

    private static class StatusBaseImpl {

        public int value;
        public int txtId;
        public int btnId;
        public int messageId;
        public String message;
        protected Class<? extends Annotation> annotationType;

        public StatusBaseImpl(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public int value() {
            return value;
        }
        public int txtId() {
            return txtId;
        }
        public int btnId() {
            return btnId;
        }
        public int messageId() {
            return messageId;
        }
        public String message() {
            return message;
        }
        public Class<? extends Annotation> annotationType() {
            return annotationType;
        }
    }

    private static class StatusEmptyImpl extends StatusBaseImpl implements StatusEmpty {

        public StatusEmptyImpl() {
            super(StatusEmpty.class);
        }

//        public StatusEmptyImpl(StatusEmpty annotation) {
//            super(StatusEmpty.class);
//            this.value = annotation.value();
//            this.txtId = annotation.txtId();
//            this.btnId = annotation.btnId();
//            this.message = annotation.message();
//        }

        public void combine(StatusEmpty annotation) {
            this.value = annotation.value() != 0 ? annotation.value() : value;
            this.txtId = annotation.txtId() != 0 ? annotation.txtId() : txtId;
            this.btnId = annotation.btnId() != 0 ? annotation.btnId() : btnId;
            this.messageId = annotation.messageId() != 0 ? annotation.messageId() : messageId;
            this.message = TextUtils.isEmpty(annotation.message()) ? message : annotation.message();
        }
    }

    private static class StatusErrorImpl extends StatusBaseImpl implements StatusError {

        public StatusErrorImpl() {
            super(StatusError.class);
        }

//        public StatusErrorImpl(StatusError annotation) {
//            super(StatusError.class);
//            this.value = annotation.value();
//            this.txtId = annotation.txtId();
//            this.btnId = annotation.btnId();
//        }

        public void combine(StatusError annotation) {
            this.value = annotation.value() != 0 ? annotation.value() : value;
            this.txtId = annotation.txtId() != 0 ? annotation.txtId() : txtId;
            this.btnId = annotation.btnId() != 0 ? annotation.btnId() : btnId;
        }
    }

    private static class StatusInvalidNetImpl extends StatusBaseImpl implements StatusInvalidNet {

        public StatusInvalidNetImpl() {
            super(StatusInvalidNet.class);
        }

//        public StatusInvalidNetImpl(StatusInvalidNet annotation) {
//            super(StatusInvalidNet.class);
//            this.value = annotation.value();
//            this.txtId = annotation.txtId();
//            this.btnId = annotation.btnId();
//        }

        public void combine(StatusInvalidNet annotation) {
            this.value = annotation.value() != 0 ? annotation.value() : value;
            this.txtId = annotation.txtId() != 0 ? annotation.txtId() : txtId;
            this.btnId = annotation.btnId() != 0 ? annotation.btnId() : btnId;
        }
    }

    private static class StatusProgressImpl extends StatusBaseImpl implements StatusProgress {

        public StatusProgressImpl() {
            super(StatusProgress.class);
        }

//        public StatusProgressImpl(StatusProgress annotation) {
//            super(StatusProgress.class);
//            this.value = annotation.value();
//            this.txtId = annotation.txtId();
//            this.message = annotation.message();
//        }

        public void combine(StatusProgress annotation) {
            this.value = annotation.value() != 0 ? annotation.value() : value;
            this.txtId = annotation.txtId() != 0 ? annotation.txtId() : txtId;
            this.message = TextUtils.isEmpty(annotation.message()) ? message : annotation.message();
        }
    }

    //</editor-fold>
}
