package com.andframe.module;

import android.annotation.SuppressLint;
import android.view.View;

import com.andframe.annotation.interpreter.Injecter;
import com.andframe.annotation.interpreter.ViewBinder;
import com.andframe.annotation.view.BindLayout;
import com.andframe.api.ViewQuery;
import com.andframe.api.Viewer;
import com.andframe.api.ViewModuler;
import com.andframe.application.AfApp;
import com.andframe.exception.AfExceptionHandler;
import com.andframe.util.java.AfReflecter;

/**
 * 视图模块实现基类
 */
@SuppressLint("ViewConstructor")
@SuppressWarnings("unused")
public abstract class AfViewModule extends AfViewWrapper implements Viewer, ViewModuler {

	public static <T extends AfViewModule> T init(Class<T> clazz, Viewer viewable, int viewId) {
		try {
			T module = AfReflecter.newUnsafeInstance(clazz);
//            Constructor<?>[] constructors = clazz.getConstructors();
//            for (int i = 0; i < constructors.length && module == null; i++) {
//                Class<?>[] parameterTypes = constructors[i].getParameterTypes();
//                if (parameterTypes.length == 0) {
//                    module = clazz.newInstance();
//                } else if (parameterTypes.length == 1 && Viewer.class.isAssignableFrom(parameterTypes[0])) {
//                    module = (T) constructors[i].newInstance(viewable);
//                }
//            }
			if (module != null && !module.isValid()) {
				AfViewModule viewModule = module;
				viewModule.setTarget(viewable, viewable.findViewByID(viewId));
			}
			return module;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static <T extends AfViewModule> T init(Class<T> clazz, Viewer viewable) {
		BindLayout annotation = AfReflecter.getAnnotation(clazz, AfViewModule.class, BindLayout.class);
		if (annotation == null) {
			return null;
		}
		return init(clazz, viewable, annotation.value());
	}

	protected AfViewModule(){
		super(new View(AfApp.get()));
	}

	protected AfViewModule(View view) {
		super(view);
	}

	protected AfViewModule(Viewer view) {
		super(new View(view.getContext()));
		BindLayout layout = AfReflecter.getAnnotation(this.getClass(), AfViewModule.class, BindLayout.class);
		if (layout != null) {
			wrapped = view.findViewById(layout.value());
		} else {
			wrapped = null;
		}
	}

	protected AfViewModule(Viewer view, int id) {
		super(new View(view.getContext()));
		wrapped = view.findViewById(id);
	}

	/**
	 * 如果不想要采用 注入的形式
	 * 子类构造函数中必须调用这个函数
	 */
	protected void initializeComponent(Viewer viewable){
		BindLayout layout = AfReflecter.getAnnotation(this.getClass(), AfViewModule.class, BindLayout.class);
		if (wrapped == null && layout != null) {
			wrapped = viewable.findViewById(layout.value());
		}
		setTarget(viewable, wrapped);
	}

	private void setTarget(final Viewer viewable, final View target) {
		if (target != null) {
			this.wrapped = target;
			this.onCreated(viewable, target);
		}
	}

	protected void onCreated(Viewer viewable, View view) {
		this.doInject();
	}

	protected void doInject(){
		if(isValid()){
			Injecter.doInject(this, getContext());
			ViewBinder.doBind(this, wrapped);
		}
	}

	@Override
	public void hide() {
		if(isValid()){
			setVisibility(View.GONE);
		}
	}

	@Override
	public void show() {
		if(isValid()){
			setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean isValid() {
		return wrapped != null;
	}

	@Override
	public boolean isVisibility() {
		return isValid() && getVisibility() == View.VISIBLE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends View> T findViewByID(int id) {
		try {
			return (T) wrapped.findViewById(id);
		} catch (Throwable e) {
			AfExceptionHandler.handle(e, "AfViewModule.findViewByID");
		}
		return null;
	}

	@Override
	public <T extends View> T findViewById(int id, Class<T> clazz) {
		View view = wrapped.findViewById(id);
		if (clazz.isInstance(view)) {
			return clazz.cast(view);
		}
		return null;
	}

	/**
	 * 开始 ViewQuery 查询
	 * @param id 控件Id
	 */
	@SuppressWarnings("unused")
	protected ViewQuery $(int... id) {
		ViewQuery query = AfApp.get().newViewQuery(getView());
		if (id == null || id.length == 0) {
			return query;
		}
		return query.id(id);
	}
	@SuppressWarnings("unused")
	protected ViewQuery $(View view, View... views) {
		return AfApp.get().newViewQuery(getView()).id(view,views);
	}


}
