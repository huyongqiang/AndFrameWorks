package com.andframe.widget.treeview;

import android.annotation.SuppressLint;
import android.os.Build.VERSION;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.andframe.widget.multichoice.AfMultiChoiceItem;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


@SuppressWarnings("unused")
public abstract class AfTreeViewItem<T> extends AfMultiChoiceItem<T> {

    protected int retract = 35;
    protected AfTreeNode<T> mNode = null;
    protected View mTreeViewContent = null;
    protected LinearLayout mTreeViewLayout = null;
    protected AfTreeViewAdapter<T> mTreeViewAdapter = null;

    public AfTreeViewItem() {
    }

    public AfTreeViewItem(int layoutId) {
        super(layoutId);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        float density = view.getContext().getResources().getDisplayMetrics().density;
        retract = (int) (density * 20 + 0.5f);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public View inflateLayout(View view, AfTreeViewAdapter<T> adapter) {
        //设置适配器
        mTreeViewAdapter = adapter;
        //创建布局
        mTreeViewContent = view;
        mTreeViewContent.setFocusable(false);
        mTreeViewLayout = new LinearLayout(view.getContext());
        mTreeViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        mTreeViewLayout.setGravity(Gravity.CENTER_VERTICAL);
//		mTreeViewLayout.setOnClickListener(this);
        //设置背景
        if (VERSION.SDK_INT < 16) {
            mTreeViewLayout.setBackgroundDrawable(view.getBackground());
        } else {
            mTreeViewLayout.setBackground(view.getBackground());
        }
        view.setBackgroundResource(android.R.color.transparent);
        //包装View
        LayoutParams lpView = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        mTreeViewLayout.addView(view, lpView);
        mTreeViewLayout.setLayoutParams(params);

        return mTreeViewLayout;
    }

    @Override
    protected boolean onBinding(T model, int index, SelectStatus status) {
        if (mTreeViewLayout != null && mNode != null) {
            mTreeViewLayout.setPadding(mNode.level * retract, 0, 0, 0);
            return onBinding(mNode.value, index, mNode.level, mNode.isExpanded, status);
        }
        return onBinding(model, index, 0, false, status);
    }

    public void setNode(AfTreeNode<T> node) {
        mNode = node;
    }

    public boolean isCanSelect(T value, int index) {
        return mNode == null || mNode.children == null || mNode.children.size() == 0;
    }

    /**
     * @param level      所在树的层数（树根为0）
     * @param isExpanded 树节点是否张开
     * @param status     选择状态{NONE,UNSELECT,SELECTED}
     * @return 绘制 选择状态 返回 TRUE 否则 FALSE
     */
    protected abstract boolean onBinding(T model, int index, int level, boolean isExpanded, SelectStatus status);


}