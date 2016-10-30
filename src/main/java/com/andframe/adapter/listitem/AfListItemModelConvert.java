package com.andframe.adapter.listitem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.andframe.api.adapter.ListItem;
import com.andframe.api.ModelConvertor;

/**
 * 相似列表布局适配器
 * @author 树朾
 * @param <From> 实际需要布局模板
 * @param <To> 可以适配的模板
 */
@SuppressWarnings("unused")
public abstract class AfListItemModelConvert<From, To> implements ListItem<From>, ModelConvertor<From, To> {
	
	private ListItem<To> item;
	/**
	 * 一个可以适配页面的布局
	 */
	public AfListItemModelConvert(ListItem<To> item) {
		this.item = item;
	}

	@Override
	public void onBinding(View view, From model, int index) {
		item.onBinding(view, this.convert(model), index);
	}

	@Override
	public View onCreateView(Context context, ViewGroup parent) {
		return item.onCreateView(context, parent);
	}

}
