package com.ontheway.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ontheway.activity.AfActivity;
import com.ontheway.activity.framework.AfPageable;
import com.ontheway.activity.framework.AfView;
import com.ontheway.application.AfApplication;
import com.ontheway.application.AfDaemonThread;
import com.ontheway.application.AfExceptionHandler;
import com.ontheway.exception.AfException;
import com.ontheway.exception.AfToastException;
import com.ontheway.thread.AfTask;
import com.ontheway.thread.AfThreadWorker;
import com.ontheway.util.AfBundle;
import com.ontheway.util.AfIntent;
/**
 * 框架 AfFragment
 * @author SCWANG
 *
 *	以下是 AfFragment 像子类提供的 功能方法
 *
	protected void buildThreadWorker()
	 * 为本页面开启一个独立后台线程 供 postTask 的 任务(AfTask)运行 注意：开启线程之后 postTask
	 * 任何任务都会在该线程中运行。 如果 postTask 前一个任务未完成，后一个任务将等待
	 * 
	protected AfTask postTask(AfTask task)
	 * 抛送任务到Worker执行

	AfPageable 接口中的方法
	public Activity getActivity();
	public void makeToastLong(String tip);
	public void makeToastShort(String tip);
	public void makeToastLong(int resid);
	public void makeToastShort(int resid);
	public boolean getSoftInputStatus();
	public boolean getSoftInputStatus(View view);
	public void setSoftInputEnable(EditText editview, boolean enable);
	public void showProgressDialog(String message);
	public void showProgressDialog(String message, boolean cancel);
	public void showProgressDialog(String message, boolean cancel,int textsize);
	public void showProgressDialog(String message, listener);
	public void showProgressDialog(String message, listener, int textsize);
	public void hideProgressDialog();
	public void startActivity(Class<? extends AfActivity> tclass);
	public void startActivityForResult(Class<AfActivity> tclass,int request);
	
	public void doShowDialog(String title, String message);
	public void doShowDialog(String title, String message,OnClickListener);
	public void doShowDialog(String title, String message,String ,OnClickListener);
	public void doShowDialog(String, String,String,OnClickListener,String,OnClickListener);
	public void doShowDialog(int,String,String,String,OnClickListener,String,OnClickListener);
	public void doShowDialog(int,String,String,String,Listener,String,Listener,String,Listener);
	
	public void doShowViewDialog(title, View view,String positive, OnClickListener );
	public void doShowViewDialog(title, View view,String positive, OnClickListener , String negative,OnClickListener );
	public void doShowViewDialog(title,view,String,Listener,String,Listener,String,Listener);
	public void doShowViewDialog(int iconres, title,  view,String, OnClickListener,String,OnClickListener );
	public void doShowViewDialog(int iconres,title,view,String,Listener,String,Listener,String,Listener);
	
	public void doSelectItem(String title,String[] items,OnClickListener);
	public void doSelectItem(String title,String[] items,OnClickListener,cancel);
	public void doSelectItem(String title,String[] items,OnClickListener,oncancel);
	
	public void doInputText(String title,InputTextListener listener);
	public void doInputText(String title,int type,InputTextListener listener);
	public void doInputText(String title,String defaul,int type,InputTextListener listener);
	
	AfPageListener 接口中的方法
	public void onSoftInputShown();
	public void onSoftInputHiden();
	public void onQueryChanged();
}
 */
public abstract class AfFragment extends Fragment implements AfPageable, OnGlobalLayoutListener {

	public static final String EXTRA_DATA = "EXTRA_DATA";
	public static final String EXTRA_INDEX = "EXTRA_INDEX";
	public static final String EXTRA_RESULT = "EXTRA_RESULT";

	public static final int LP_MP = LayoutParams.MATCH_PARENT;
	public static final int LP_WC = LayoutParams.WRAP_CONTENT;
	// 根视图
	protected View mRootView = null;

	protected AfThreadWorker mWorker = null;
	protected ProgressDialog mProgress = null;
	protected boolean mIsRecycled = false;

	/**
	 * @Description: 获取LOG日志 TAG 是 AfFragment 的方法
	 * 用户也可以重写自定义TAG,这个值AfActivity在日志记录时候会使用
	 * 子类实现也可以使用
	 * @Author: scwang
	 * @Version: V1.0, 2015-2-14 上午10:58:00
	 * @Modified: 初次创建TAG方法
	 * @return
	 */
	protected String TAG() {
		// TODO Auto-generated method stub
		return "AfFragment("+getClass().getName()+")";
	}
	protected String TAG(String tag) {
		// TODO Auto-generated method stub
		return "AfFragment("+getClass().getName()+")."+tag;
	}
	/**
	 * 判断是否被回收
	 * @return true 已经被回收
	 */
	@Override
	public boolean isRecycled() {
		// TODO Auto-generated method stub
		return mIsRecycled ;
	}
	
	/**
	 * 为本页面开启一个独立后台线程 供 postTask 的 任务(AfTask)运行 注意：开启线程之后 postTask
	 * 任何任务都会在该线程中运行。 如果 postTask 前一个任务未完成，后一个任务将等待
	 */
	protected void buildThreadWorker() {
		// TODO Auto-generated method stub
		if (mWorker == null) {
			mWorker = new AfThreadWorker(this.getClass().getSimpleName());
		}
	}
	/**
	 * 抛送任务到Worker执行
	 * @param task
	 */
	public AfTask postTask(AfTask task) {
		// TODO Auto-generated method stub
		if (mWorker != null) {
			return mWorker.postTask(task);
		}
		return AfDaemonThread.postTask(task);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	public void startActivity(Class<? extends AfActivity> tclass) {
		// TODO Auto-generated method stub
		startActivity(new Intent(getActivity(), tclass));
	}
	
	public void startActivityForResult(Class<? extends AfActivity> tclass,int request) {
		// TODO Auto-generated method stub
		startActivityForResult(new Intent(getActivity(),tclass), request);
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 * final 重写 onActivityResult 使用 try-catch 调用 
	 * 		onActivityResult(AfIntent intent, int questcode,int resultcode)
	 * @see AfFragment#onActivityResult(AfIntent intent, int questcode,int resultcode)
	 * {@link AfFragment#onActivityResult(AfIntent intent, int questcode,int resultcode)}
	 */
	@Override
	public final void onActivityResult(int questcode, int resultcode, Intent data) {
		// TODO Auto-generated method stub
		try {
			onActivityResult(new AfIntent(data), questcode, resultcode);
		} catch (Throwable e) {
			// TODO: handle exception
			if (!(e instanceof AfToastException)) {
				AfExceptionHandler.handler(e, TAG("onActivityResult"));
			}
			makeToastLong("反馈信息读取错误！",e);
		}
	}

	/**
	 * 安全 onActivityResult(AfIntent intent, int questcode,int resultcode) 
	 * 在onActivityResult(int questCode, int resultCode, Intent data) 中调用
	 * 并使用 try-catch 提高安全性，子类请重写这个方法 
	 * @see AfFragment#onActivityResult(int, int, android.content.Intent)
	 * {@link AfFragment#onActivityResult(int, int, android.content.Intent)}
	 * @param intent
	 * @param questcode
	 * @param resultcode
	 */
	protected void onActivityResult(AfIntent intent, int questcode,int resultcode) {
		// TODO Auto-generated method stub
		super.onActivityResult(questcode, resultcode, intent);
	}

	/**
	 * 自定义 View onCreate(Bundle)
	 */
	protected abstract void onCreated(AfView rootView, AfBundle bundle)throws Exception;

	/**
	 * 自定义 View onCreateView(LayoutInflater, ViewGroup)
	 */
	protected abstract View onCreateView(LayoutInflater inflater,
			ViewGroup container);

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.onQueryChanged();
	}
	
	@Override
	public final void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
	}
	
	/**
	 * 锁住 上级的 View onCreateView(LayoutInflater, ViewGroup, Bundle)
	 */
	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle bundle) {
		// TODO Auto-generated method stub
		
		mRootView = onCreateView(inflater, container);
		if (mRootView == null) {
			mRootView = super.onCreateView(inflater, container,bundle);
		}
		try {
			mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
			onCreated(new AfView(mRootView), new AfBundle(getArguments()));
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			if (!(e instanceof AfToastException)) {
				AfExceptionHandler.handler(e, TAG("onCreateView"));
			}
			makeToastLong("页面初始化异常！",e);
		}
		return mRootView;
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
//		mRootView = null;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mIsRecycled = true;
		if (mWorker != null) {
			mWorker.quit();
		}
	}

	/**
	 * 第一次切换到本页面
	 */
	protected void onFirstSwitchOver() {

	}

	/**
	 * 每次切换到本页面
	 * 
	 * @param count
	 *            切换序号
	 */
	protected void onSwitchOver(int count) {

	}

	/**
	 * 离开本页面
	 */
	protected void onSwitchLeave() {

	}

	/**
	 * 查询系统数据变动
	 */
	public void onQueryChanged() {
		// TODO Auto-generated method stub

	}

	
	@Override
	public boolean getSoftInputStatus() {
		// TODO Auto-generated method stub
		InputMethodManager imm = null;
		String Server = Context.INPUT_METHOD_SERVICE;
		imm = (InputMethodManager) getActivity().getSystemService(Server);
		return imm.isActive();
	}
	
	@Override
	public boolean getSoftInputStatus(View view) {
		// TODO Auto-generated method stub
		InputMethodManager imm = null;
		String Server = Context.INPUT_METHOD_SERVICE;
		imm = (InputMethodManager) getActivity().getSystemService(Server);
		return imm.isActive(view);
	}
	
	@Override
	public void setSoftInputEnable(EditText editview, boolean enable) {
		// TODO Auto-generated method stub
		editview.setFocusable(true);
		InputMethodManager imm = null;
		String Server = Context.INPUT_METHOD_SERVICE;
		imm = (InputMethodManager) getActivity().getSystemService(Server);
		if (enable) {
			editview.setFocusableInTouchMode(true);
			editview.requestFocus();
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		} else {
			imm.hideSoftInputFromWindow(editview.getWindowToken(), 0);
		}
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		Activity activity = getActivity();
		if (activity == null) {
			return AfApplication.getAppContext();
		}
		return activity;
	}

	@Override
	public void makeToastLong(String tip) {
		// TODO Auto-generated method stub
		Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();
	}

	@Override
	public void makeToastShort(String tip) {
		// TODO Auto-generated method stub
		Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void makeToastLong(int resid) {
		// TODO Auto-generated method stub
		Toast.makeText(getContext(), resid, Toast.LENGTH_LONG).show();
	}

	@Override
	public void makeToastShort(int resid) {
		// TODO Auto-generated method stub
		Toast.makeText(getContext(), resid, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void makeToastLong(String tip,Throwable e) {
		// TODO Auto-generated method stub
		tip = AfException.handle(e, tip);
		Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();
	}

	
	@Override
	public final View findViewById(int id) {
		if (mRootView != null) {
			return mRootView.findViewById(id);
		}
		return null;
	}

	@Override
	public final TextView findTextViewById(int id) {
		View view = findViewById(id);
		if (view instanceof TextView) {
			return (TextView) view;
		}
		return null;
	}

	@Override
	public final ImageView findImageViewById(int id) {
		View view = findViewById(id);
		if (view instanceof ImageView) {
			return (ImageView) view;
		}
		return null;
	}

	@Override
	public final Button findButtonById(int id) {
		View view = findViewById(id);
		if (view instanceof Button) {
			return (Button) view;
		}
		return null;
	}

	@Override
	public final EditText findEditTextById(int id) {
		View view = findViewById(id);
		if (view instanceof EditText) {
			return (EditText) view;
		}
		return null;
	}

	@Override
	public final CheckBox findCheckBoxById(int id) {
		View view = findViewById(id);
		if (view instanceof CheckBox) {
			return (CheckBox) view;
		}
		return null;
	}

	@Override
	public final RadioButton findRadioButtonById(int id) {
		View view = findViewById(id);
		if (view instanceof RadioButton) {
			return (RadioButton) view;
		}
		return null;
	}

	@Override
	public final ListView findListViewById(int id) {
		View view = findViewById(id);
		if (view instanceof ListView) {
			return (ListView) view;
		}
		return null;
	}

	@Override
	public GridView findGridViewById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof GridView) {
			return (GridView) view;
		}
		return null;
	}

	@Override
	public final LinearLayout findLinearLayoutById(int id) {
		View view = findViewById(id);
		if (view instanceof LinearLayout) {
			return (LinearLayout) view;
		}
		return null;
	}

	@Override
	public final FrameLayout findFrameLayoutById(int id) {
		View view = findViewById(id);
		if (view instanceof FrameLayout) {
			return (FrameLayout) view;
		}
		return null;
	}

	@Override
	public final RelativeLayout findRelativeLayoutById(int id) {
		View view = findViewById(id);
		if (view instanceof RelativeLayout) {
			return (RelativeLayout) view;
		}
		return null;
	}

	@Override
	public final ScrollView findScrollViewById(int id) {
		View view = findViewById(id);
		if (view instanceof ScrollView) {
			return (ScrollView) view;
		}
		return null;
	}

	@Override
	public ViewPager findViewPagerById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof ViewPager) {
			return (ViewPager) view;
		}
		return null;
	}

	@Override
	public WebView findWebViewById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof WebView) {
			return (WebView) view;
		}
		return null;
	}
	
	@Override
	public ProgressBar findProgressBarById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof ProgressBar) {
			return (ProgressBar) view;
		}
		return null;
	}

	@Override
	public RatingBar findRatingBarById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof RatingBar) {
			return (RatingBar) view;
		}
		return null;
	}

	@Override
	public ExpandableListView findExpandableListViewById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof ExpandableListView) {
			return (ExpandableListView) view;
		}
		return null;
	}

	@Override
	public HorizontalScrollView findHorizontalScrollViewById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof HorizontalScrollView) {
			return (HorizontalScrollView) view;
		}
		return null;
	}

	@Override
	public DatePicker findDatePickerById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof DatePicker) {
			return (DatePicker) view;
		}
		return null;
	}

	@Override
	public TimePicker findTimePickerById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof TimePicker) {
			return (TimePicker) view;
		}
		return null;
	}
	
	@Override
	public RadioGroup findRadioGroupById(int id) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (view instanceof RadioGroup) {
			return (RadioGroup) view;
		}
		return null;
	}

	@Override
	public <T> T findViewById(int id, Class<T> clazz) {
		// TODO Auto-generated method stub
		View view = findViewById(id);
		if (clazz.isInstance(view)) {
			return clazz.cast(view);
		}
		return null;
	}
	/**
	 * 显示 进度对话框
	 * 
	 * @param message
	 *            消息
	 */
	public final void showProgressDialog(String message) {
		// TODO Auto-generated method stub
		showProgressDialog(message, false, 25);
	}

	/**
	 * 显示 进度对话框
	 * 
	 * @param message
	 *            消息
	 * @param cancel
	 *            是否可取消
	 */
	public final void showProgressDialog(String message, boolean cancel) {
		// TODO Auto-generated method stub
		showProgressDialog(message, cancel, 25);
	}

	/**
	 * 显示 进度对话框
	 * 
	 * @param message
	 *            消息
	 * @param cancel
	 *            是否可取消
	 * @param textsize
	 *            字体大小
	 */
	public final void showProgressDialog(String message, boolean cancel,
			int textsize) {
		// TODO Auto-generated method stub
		mProgress = new ProgressDialog(getActivity());
		mProgress.setMessage(message);
		mProgress.setCancelable(cancel);
		mProgress.setOnCancelListener(null);
		mProgress.show();

		setDialogFontSize(mProgress, textsize);
	}

	/**
	 * 显示 进度对话框
	 * 
	 * @param message
	 *            消息
	 * @param cancel
	 *            是否可取消
	 * @param textsize
	 *            字体大小
	 */
	public final void showProgressDialog(String message,
			OnCancelListener listener) {
		// TODO Auto-generated method stub
		mProgress = new ProgressDialog(getActivity());
		mProgress.setMessage(message);
		mProgress.setCancelable(true);
		mProgress.setOnCancelListener(listener);
		mProgress.show();

		setDialogFontSize(mProgress, 25);
	}

	/**
	 * 显示 进度对话框
	 * 
	 * @param message
	 *            消息
	 * @param cancel
	 *            是否可取消
	 * @param textsize
	 *            字体大小
	 */
	public final void showProgressDialog(String message,
			OnCancelListener listener, int textsize) {
		// TODO Auto-generated method stub
		mProgress = new ProgressDialog(getActivity());
		mProgress.setMessage(message);
		mProgress.setCancelable(true);
		mProgress.setOnCancelListener(listener);
		mProgress.show();

		setDialogFontSize(mProgress, textsize);
	}

	/**
	 * 实现 onGlobalLayout 
	 * 	用于计算 软键盘的弹出和隐藏
	 * 	子类在对 onGlobalLayout 重写的时候请调用 
	 * 		super.onGlobalLayout();
	 * 	否则不能对软键盘进行监听
	 */
	int lastdiff = -1;
	@Override
	public void onGlobalLayout() {
		// TODO Auto-generated method stub
		if(mRootView != null){
			int diff = mRootView.getRootView().getHeight() - mRootView.getHeight();
			if(lastdiff > -1){
				if(lastdiff < diff){
					this.onSoftInputShown();
				}else if(lastdiff > diff){
					this.onSoftInputHiden();
				}
			}
			lastdiff = diff;
		}
	}
	
	@Override
	public void onSoftInputHiden() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSoftInputShown() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 隐藏 进度对话框
	 */
	public final void hideProgressDialog() {
		// TODO Auto-generated method stub
		if (mProgress != null && !isRecycled()) {
			mProgress.dismiss();
			mProgress = null;
		}
	}

	/**
	 * 显示对话框 并添加默认按钮 "我知道了"
	 * @param title 显示标题
	 * @param message 显示内容
	 */
	public void doShowDialog(String title, String message) {
		doShowDialog(0,title,message,"我知道了", null, "", null);
	}
	/**
	 * 显示对话框 并添加默认按钮 "我知道了"
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param lpositive 点击  "我知道了" 响应事件
	 */
	public void doShowDialog(String title, String message,OnClickListener lpositive) {
		doShowDialog(0,title,message,"我知道了", lpositive, "", null);
	}
	/**
	 * 显示对话框 
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 */
	public void doShowDialog(String title, String message,String positive,OnClickListener lpositive) {
		doShowDialog(0,title,message,positive, lpositive, "", null);
	}
	/**
	 * 显示对话框 
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	public void doShowDialog(String title, String message,
			String positive, OnClickListener lpositive, String negative,
			OnClickListener lnegative) {
		doShowDialog(0,title,message,positive, lpositive,negative,lnegative);
	}	

	/**
	 * 显示对话框 
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param neutral 详细 按钮显示信息
	 * @param lneutral 点击  详细 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@Override
	public void doShowDialog(String title, String message, 
			String positive, OnClickListener lpositive, 
			String neutral, OnClickListener lneutral, 
			String negative,OnClickListener lnegative) {
		// TODO Auto-generated method stub
		doShowDialog(0, title, message,positive, lpositive, neutral, lneutral, negative,lnegative);
	}
	/**
	 * 显示对话框 
	 * @param iconres 对话框图标
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@Override
	public void doShowDialog(int iconres, String title, String message,
			String positive, OnClickListener lpositive, String negative,
			OnClickListener lnegative) {
		doShowDialog(iconres, title, message,positive, lpositive, "", null, negative,lnegative);
	}

	/**
	 * 显示对话框 
	 * @param iconres 对话框图标
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param neutral 详细 按钮显示信息
	 * @param lneutral 点击  详细 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	public void doShowDialog(int iconres, String title, String message,
			String positive, OnClickListener lpositive, 
			String neutral, OnClickListener lneutral, 
			String negative,OnClickListener lnegative) {
		// TODO Auto-generated method stub
		doShowDialog(-1, iconres, title, message, positive, lpositive, neutral, lneutral, negative, lnegative);
	}

	/**
	 * 显示视图对话框 
	 * @param theme 主题
	 * @param iconres 对话框图标
	 * @param title 显示标题
	 * @param message 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param neutral 详细 按钮显示信息
	 * @param lneutral 点击  详细 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@SuppressLint("NewApi")
	@Override
	public void doShowDialog(int theme, int iconres, 
			String title,String message, 
			String positive, OnClickListener lpositive,
			String neutral, OnClickListener lneutral, 
			String negative,OnClickListener lnegative) {
		// TODO Auto-generated method stub
		Builder builder = null;
		try {
			builder = new Builder(getActivity());
		} catch (Throwable ex) {
			// TODO: handle exception
			return;
		}
		if (theme > 0) {
			try {
				builder = new Builder(getActivity(), theme);
			} catch (Throwable e) {
				// TODO: handle exception
				builder = new Builder(getActivity());
			}
		}
		builder.setTitle(title);
		builder.setMessage(message);
		if (iconres > 0) {
			builder.setIcon(iconres);
		}
		if (positive != null && positive.length() > 0) {
			builder.setPositiveButton(positive, lpositive);
		}
		if (neutral != null && neutral.length() > 0) {
			builder.setNeutralButton(neutral, lneutral);
		}
		if (negative != null && negative.length() > 0) {
			builder.setNegativeButton(negative, lnegative);
		}
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}
	/**
	 * 显示视图对话框 
	 * @param title 显示标题
	 * @param view 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 */
	@Override
	public void doShowViewDialog(String title, View view, String positive,
			OnClickListener lpositive) {
		// TODO Auto-generated method stub
		doShowViewDialog(title, view, positive, lpositive,"",null);
	}

	/**
	 * 显示视图对话框 
	 * @param title 显示标题
	 * @param view 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@Override
	public void doShowViewDialog(String title, View view, String positive,
			OnClickListener lpositive, String negative,
			OnClickListener lnegative) {
		// TODO Auto-generated method stub
		doShowViewDialog(0,title,view,positive, lpositive,negative,lnegative);
	}
	/**
	 * 显示视图对话框 
	 * @param title 显示标题
	 * @param view 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param neutral 详细 按钮显示信息
	 * @param lneutral 点击  详细 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@Override
	public void doShowViewDialog(String title, View view,
			String positive, OnClickListener lpositive, 
			String neutral, OnClickListener lneutral, 
			String negative,OnClickListener lnegative) {
		doShowViewDialog(0,title,view,positive, lpositive,neutral,lneutral,negative,lnegative);
	}
	/**
	 * 显示视图对话框 
	 * @param iconres 对话框图标
	 * @param title 显示标题
	 * @param view 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@Override
	public void doShowViewDialog(int iconres, String title, View view,
			String positive, OnClickListener lpositive, 
			String negative,OnClickListener lnegative) {
		doShowViewDialog(0,title,view,positive, lpositive,"",null,negative,lnegative);
	}
	/**
	 * 显示视图对话框 
	 * @param iconres 对话框图标
	 * @param title 显示标题
	 * @param view 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param neutral 详细 按钮显示信息
	 * @param lneutral 点击  详细 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@Override
	public void doShowViewDialog(int iconres, String title, View view,
			String positive, OnClickListener lpositive, 
			String neutral, OnClickListener lneutral, 
			String negative,OnClickListener lnegative) {
		// TODO Auto-generated method stub
		doShowViewDialog(-1, iconres, title, view, positive, lpositive, neutral, lneutral, negative, lnegative);
	}

	/**
	 * 显示视图对话框 
	 * @param theme 主题
	 * @param iconres 对话框图标
	 * @param title 显示标题
	 * @param view 显示内容
	 * @param positive 确认 按钮显示信息
	 * @param lpositive 点击  确认 按钮 响应事件
	 * @param neutral 详细 按钮显示信息
	 * @param lneutral 点击  详细 按钮 响应事件
	 * @param negative 按钮显示信息
	 * @param lnegative 点击  拒绝 按钮 响应事件
	 */
	@SuppressLint("NewApi")
	@Override
	public void doShowViewDialog(int theme, 
			int iconres, String title,View view, 
			String positive, OnClickListener lpositive,
			String neutral, OnClickListener lneutral, 
			String negative,OnClickListener lnegative) {
		// TODO Auto-generated method stub
		Builder builder = null;
		try {
			builder = new Builder(getActivity());
		} catch (Throwable e) {
			// TODO: handle exception
			return ;
		}
		if (theme > 0) {
			try {
				builder = new Builder(getActivity(), theme);
			} catch (Throwable e) {
				// TODO: handle exception
				builder = new Builder(getActivity());
			}
		}
		builder.setTitle(title);
		RelativeLayout.LayoutParams lp = null;
		lp = new RelativeLayout.LayoutParams(LP_WC,LP_WC);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		RelativeLayout layout = new RelativeLayout(getActivity());
		layout.addView(view, lp);
		builder.setView(layout);
		if (iconres > 0) {
			builder.setIcon(iconres);
		}
		if (positive != null && positive.length() > 0) {
			builder.setPositiveButton(positive, lpositive);
		}
		if (neutral != null && neutral.length() > 0) {
			builder.setNeutralButton(neutral, lneutral);
		}
		if (negative != null && negative.length() > 0) {
			builder.setNegativeButton(negative, lnegative);
		}
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

	/**
	 * 显示一个单选对话框 （设置可取消）
	 * @param title 对话框标题
	 * @param items 选择菜单项
	 * @param listener 选择监听器
	 * @param cancel 取消选择监听器
	 */
	public void doSelectItem(String title,String[] items,OnClickListener listener,
			boolean cancel){
		Builder dialog = new Builder(getActivity());
		dialog.setItems(items,listener);
		if(title != null){
			dialog.setTitle(title);
			dialog.setCancelable(false);
			if(cancel){
				dialog.setNegativeButton("取消", null);
			}
		}else{
			dialog.setCancelable(cancel);
		}
		dialog.show();
	}

	/**
	 * 显示一个单选对话框 
	 * @param title 对话框标题
	 * @param items 选择菜单项
	 * @param listener 选择监听器
	 * @param oncancel 取消选择监听器
	 */
	public void doSelectItem(String title,String[] items,OnClickListener listener,
			final OnClickListener oncancel) {
		// TODO Auto-generated method stub
		Builder dialog = new Builder(getActivity());
		if(title != null){
			dialog.setTitle(title);
			dialog.setCancelable(false);
			dialog.setNegativeButton("取消", oncancel);
		}else if(oncancel != null){
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					oncancel.onClick(dialog, 0);
				}
			});
		}
		dialog.setItems(items,listener);
		dialog.show();
	}

	/**
	 * 显示一个单选对话框 （默认可取消）
	 * @param title 对话框标题
	 * @param items 选择菜单项
	 * @param listener 选择监听器
	 */
	public void doSelectItem(String title,String[] items,OnClickListener listener) {
		// TODO Auto-generated method stub
		doSelectItem(title, items, listener, null);
	}

	/**
	 * 弹出一个文本输入框
	 * @param title 标题
	 * @param listener 监听器
	 */
	public void doInputText(String title,InputTextListener listener) {
		doInputText(title, "", InputType.TYPE_CLASS_TEXT, listener);
	}

	/**
	 * 弹出一个文本输入框
	 * @param title 标题
	 * @param type android.text.InputType
	 * @param listener 监听器
	 */
	public void doInputText(String title,int type,InputTextListener listener) {
		doInputText(title, "", type, listener);
	}

	/**
	 * 弹出一个文本输入框
	 * @param title 标题
	 * @param defaul 默认值
	 * @param type android.text.InputType
	 * @param listener 监听器
	 */
	public void doInputText(String title,String defaul,int type,InputTextListener listener) {
		final EditText input = new EditText(getActivity());
		final int defaullength = defaul.length();
		final InputTextListener flistener = listener;
		input.setText(defaul);
		input.clearFocus();
		input.setInputType(type);
		Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(input);
		builder.setCancelable(false);
		builder.setTitle(title);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				setSoftInputEnable(input, false);
				dialog.dismiss();
				flistener.onInputTextComfirm(input);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				setSoftInputEnable(input, false);
				dialog.dismiss();
				if(flistener instanceof InputTextCancelable){
					InputTextCancelable cancel = (InputTextCancelable)flistener;
					cancel.onInputTextCancel(input);
				}
			}
		});
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			public void onShow(DialogInterface dialog) {
				setSoftInputEnable(input, true);
				input.setSelection(0,defaullength);
			}
		});
		dialog.show();
	}
	
	protected void setProgressDialogText(ProgressDialog dialog, String text) {
		Window window = dialog.getWindow();
		View view = window.getDecorView();
		setViewFontText(view, text);
	}

	private void setViewFontText(View view, String text) {
		// TODO Auto-generated method stub
		if (view instanceof ViewGroup) {
			ViewGroup parent = (ViewGroup) view;
			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				setViewFontText(parent.getChildAt(i), text);
			}
		} else if (view instanceof TextView) {
			TextView textview = (TextView) view;
			textview.setText(text);
		}
	}
	
	private void setDialogFontSize(Dialog dialog, int size) {
		Window window = dialog.getWindow();
		View view = window.getDecorView();
		setViewFontSize(view, size);
	}

	private void setViewFontSize(View view, int size) {
		if (view instanceof ViewGroup) {
			ViewGroup parent = (ViewGroup) view;
			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				setViewFontSize(parent.getChildAt(i), size);
			}
		} else if (view instanceof TextView) {
			TextView textview = (TextView) view;
			textview.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
		}
	}

	/**
	 * 按下返回按键
	 * @return 返回 true 表示已经处理 否则 Activity 会处理
	 */
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 按键按下事件
	 * @return 返回 true 表示已经处理 否则 Activity 会处理
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 按键弹起事件
	 * @return 返回 true 表示已经处理 否则 Activity 会处理
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 按键重复事件
	 * @return 返回 true 表示已经处理 否则 Activity 会处理
	 */
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 按键onKeyShortcut事件
	 * @return 返回 true 表示已经处理 否则 Activity 会处理
	 */
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 按键onKeyLongPress事件
	 * @return 返回 true 表示已经处理 否则 Activity 会处理
	 */
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
