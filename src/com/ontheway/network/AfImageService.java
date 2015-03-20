package com.ontheway.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.widget.ImageView;

import com.ontheway.application.AfAppSettings;
import com.ontheway.application.AfApplication;
import com.ontheway.application.AfDaemonThread;
import com.ontheway.application.AfExceptionHandler;
import com.ontheway.caches.AfImageCaches;
import com.ontheway.exception.AfException;
import com.ontheway.thread.AfDownloader.DownloadEntity;
import com.ontheway.thread.AfDownloader.DownloadTask;
import com.ontheway.thread.AfHandlerTask;
import com.ontheway.util.AfImageHelper;
import com.ontheway.util.AfNetwork;

public class AfImageService {
	
	public interface LoadImageListener{
		/**图片成功加载完成**/
		boolean onImageLoaded(ImageView view,Drawable drawable);
		/**图片加载失败**/
		boolean onImageFailed(ImageView view,String error,Throwable ex);
	}

	public static final int EFFECT_NONE = 0x00;
	public static final int EFFECT_GRAY = 0x01;
	public static final int EFFECT_ROUND = 0x02;
	public static final int EFFECT_ROUNDCORNER = 0x04;
	public static final int EFFECT_SELFADAPTION = 0x08;//自适应参数
	
	public enum Effect{
		/*** 无效果*/
		NONE(EFFECT_NONE),	
		/** * 黑白 */
		GRAY(EFFECT_GRAY),	
		/*** 圆形效果*/
		ROUND(EFFECT_ROUND),
		/*** 圆角效果*/
		ROUNDCORNER(EFFECT_ROUNDCORNER),//
		/*** 图片自适应大小*/
		SELFADAPTION(EFFECT_SELFADAPTION),//
		//以下是组合 
		GRAY_ROUND(EFFECT_GRAY|EFFECT_ROUND),
		GRAY_ROUNDCORNER(EFFECT_GRAY|EFFECT_ROUNDCORNER),
		GRAY_SELFADAPTION(EFFECT_GRAY|EFFECT_SELFADAPTION);
		
		int value = EFFECT_NONE;
		private Effect(int effect) {
			// TODO Auto-generated constructor stub
			value = effect;
		}
		public int value() {
			return value;
		}
	}
	
	protected static AfImageService mInstance;
	
	public static void initialize(Context context){
		if(mInstance == null){
			mInstance = AfApplication.getApp().getImageService();
		}
	}

	public static AfImageService getInstance(){
		return mInstance;
	}

	protected BitmapDrawable mBitmapLoading;
	protected BitmapDrawable mBitmapGetFail;
	protected BitmapDrawable mBitmapNotFind;

	protected HashMap<String, ImageTask> mLoadingTask = new HashMap<String, ImageTask>();


	public void setBitmapGetFail(BitmapDrawable mBitmapGetFail) {
		this.mBitmapGetFail = mBitmapGetFail;
	}
	public void setBitmapLoading(BitmapDrawable mBitmapLoading) {
		this.mBitmapLoading = mBitmapLoading;
	}
	public void setBitmapNotFind(BitmapDrawable mBitmapNotFind) {
		this.mBitmapNotFind = mBitmapNotFind;
	}
	/**
	 * 在没有加载图片之前 可以使用默认图片
	 * @return
	 */
	public BitmapDrawable getImageLoading() {
		return mBitmapGetFail;
	}

	/**
	 * 在没有加载图片之前 可以使用默认图片
	 * @return
	 */
	public BitmapDrawable getImageGetFail() {
		return mBitmapLoading;
	}

	/**
	 * 在没有加载图片之前 可以使用默认图片
	 * @return
	 */
	public BitmapDrawable getImageNotFind() {
		return mBitmapNotFind;
	}

	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 */
	public static void bindImage(String url, ImageView view) {
		bindImage(url, view, 0);
	}
	
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param enable 是否使用缓存
	 */
	public static void bindImage(String url, ImageView view, boolean enable) {
		bindImage(url, view, 0, enable);
	}

	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片 缓存连接
	 */
	public static void bindImage(String url, ImageView view, String idefault) {
		bindImage(url, view, idefault,true) ;
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片的资源ID
	 */
	public static void bindImage(String url, ImageView view, int idefault) {
		bindImage(url, view, idefault,true) ;
	}

	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param effect 图片处理效果
	 * @param view
	 */
	public static void bindImage(String url, ImageView view, Effect effect) {
		if(mInstance != null){
			mInstance.doBind(url, view, 0,effect.value, true);
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param effect 图片处理效果
	 * @param view
	 * @param idefault 默认图片 缓存连接
	 */
	public static void bindImage(String url, ImageView view, Effect effect, String idefault) {
		if(mInstance != null){
			mInstance.doBind(url, view, idefault,effect.value, true);
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param effect 图片处理效果
	 * @param view
	 * @param idefault 默认图片的资源ID
	 */
	public static void bindImage(String url, ImageView view, Effect effect, int idefault) {
		if(mInstance != null){
			mInstance.doBind(url, view, idefault,effect.value, true);
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片 缓存连接
	 */
	public static void bindImage(String url, ImageView view, String sdefault, boolean enable) {
		// TODO Auto-generated method stub
		if(mInstance != null){
			mInstance.doBind(url, view, sdefault,EFFECT_NONE, enable);
		}
	}

	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片的资源ID
	 * @param enable 是否使用缓存
	 */
	public static void bindImage(String url, ImageView view, int idefault, boolean enable) {
		// TODO Auto-generated method stub
		if(mInstance != null){
			mInstance.doBind(url, view, idefault,EFFECT_NONE, enable);
		}
	}

	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param effect 图片处理效果
	 * @param idefault 默认图片的资源ID
	 * @param enable 是否使用缓存
	 */
	public static void bindImage(String url, ImageView view,Effect effect, int idefault, boolean enable) {
		// TODO Auto-generated method stub
		if(mInstance != null){
			mInstance.doBind(url, view, idefault,effect.value, enable);
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param effect 图片处理效果
	 * @param idefault 默认图片的资源ID
	 * @param enable 是否使用缓存
	 */
	public static void bindImage(String url, LoadImageListener listener) {
		// TODO Auto-generated method stub
		if(mInstance != null){
			mInstance.doBind(url, (null), listener, "",EFFECT_NONE, true);
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param effect 图片处理效果
	 * @param idefault 默认图片的资源ID
	 * @param enable 是否使用缓存
	 */
	public static void bindImage(String url, LoadImageListener listener,Effect effect, String sdefault, boolean enable) {
		// TODO Auto-generated method stub
		if(mInstance != null){
			mInstance.doBind(url, (null), listener, sdefault,effect.value, enable);
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片 缓存连接
	 */
	protected void doBind(String url, ImageView view, String sdefault,int effect, boolean enable) {
		if (bindNoImage(view,null, sdefault,effect)) {
			if (url != null && url.length() > 0) {
				// 先从图片缓冲中读取图片
				if (!bindCaches(url,view,null,enable,effect)) {
					// 如果失败从网络上加载数据
					bindDefault(view,null, sdefault, getImageLoading(),effect);
					postTask(new ImageTask(url, view,null, sdefault,effect));
				} 
			}else {
				bindDefault(view,null, sdefault, getImageNotFind(),effect);
			} 
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片 缓存连接
	 */
	protected void doBind(String url, ImageView view,LoadImageListener listener, String sdefault,int effect, boolean enable) {
		if (bindNoImage(view,listener, sdefault,effect)) {
			if (url != null && url.length() > 0) {
				// 先从图片缓冲中读取图片
				if (!bindCaches(url,view,listener,enable,effect)) {
					// 如果失败从网络上加载数据
					bindDefault(view,null, sdefault, getImageLoading(),effect);
					postTask(new ImageTask(url, view,listener, sdefault,effect));
				}
			}else {
				bindDefault(view,listener, sdefault, getImageNotFind(),effect);
			}
		}
	}
	/**
	 * 将图片URL绑定到控件ImageView
	 * @param url 图片连接 http:// 格式
	 * @param view
	 * @param idefault 默认图片的资源ID
	 * @param enable 是否使用缓存
	 */
	protected void doBind(String url, ImageView view, int idefault,int effect, boolean enable){
		if (bindNoImage(view,null, idefault,effect)) {
			if (url != null && url.length() > 0) {
				// 先从图片缓冲中读取图片
				if (!bindCaches(url,view,null,enable,effect)) {
					// 如果失败从网络上加载数据
					bindDefault(view,null, idefault, getImageLoading(),effect);
					postTask(new ImageTask(url, view,null, idefault,effect));
				}
			} else {
				bindDefault(view,null, idefault, getImageNotFind(),effect);
			}
		} 
	}

	protected boolean bindCaches(String url,ImageView view,LoadImageListener listener,boolean enable,int effect) {
		// TODO Auto-generated method stub
		if(enable){
			AfApplication app = AfApplication.getApp();
			AfImageCaches caches = AfImageCaches.getInstance();
			BitmapDrawable tButmap = caches.get(app,url);
			if (tButmap != null) {
				bindImageBitmap(view,listener,tButmap,effect);
				return true;
			}
		}
		return false;
	}

	protected void bindImageBitmap(final ImageView view,LoadImageListener listener, BitmapDrawable bitmap ,int effect) {
		// TODO Auto-generated method stub
		AfApplication app = AfApplication.getApp();
		if((effect & EFFECT_ROUND) == EFFECT_ROUND){
			bitmap = AfImageHelper.toRoundBitmap(app,bitmap,false);
		}
		if((effect & EFFECT_ROUNDCORNER) == EFFECT_ROUNDCORNER){
			bitmap = AfImageHelper.toRoundCornerBitmap(app,bitmap,false);
		}
		if((effect & EFFECT_GRAY) == EFFECT_GRAY){
			bitmap = AfImageHelper.toGrayBitmap(app, bitmap);
		}
		if((effect & EFFECT_SELFADAPTION) == EFFECT_SELFADAPTION){
			if (view != null) {
				view.setAdjustViewBounds(true);
			}
		}
		if (listener != null) {
			if (!listener.onImageLoaded(view, bitmap) && view != null) {
				view.setImageDrawable(bitmap);
			}
		}else if (view != null) {
			view.setImageDrawable(bitmap);
		}
//		if((effect & EFFECT_SELFADAPTION) == EFFECT_SELFADAPTION){
//			Bitmap bp = bitmap.getBitmap();
//			//Point size = AfMeasure.measureView(view);
//			final float AspectRatio = 1.0f*bp.getHeight()/bp.getWidth();
//			view.setScaleType(ScaleType.FIT_XY);
//			int width = view.getWidth();
//			if (width > 0) {
//				LayoutParams lp = view.getLayoutParams();
//				lp.width = width;
//				lp.height = (int)(width*AspectRatio);
//				view.setLayoutParams(lp);
//			}else {
//				new Handler(AfApplication.getApp().getMainLooper()).postDelayed(new Runnable() {
//					
//					private int count = 0;
//					private float mAspectRatio  = AspectRatio;
//					private ImageView mView = view;
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						while (count++ < 100) {
//							int width = view.getWidth();
//							if (width > 0) {
//								LayoutParams lp = mView.getLayoutParams();
//								lp.width = width;
//								lp.height = (int)(width*mAspectRatio);
//								view.setLayoutParams(lp);
//							}else {
//								new Handler(AfApplication.getApp().getMainLooper()).postDelayed(this,50);
//							}
//						}
//					}
//				},50);
//			}
//		}
	}

	protected static boolean isSettingNoImage() {
		// TODO Auto-generated method stub
		int network = AfApplication.getNetworkStatus();
		AfAppSettings setting = AfAppSettings.getInstance();
		return (network == AfNetwork.TYPE_MOBILE && setting.isNoImage());
	}

	protected boolean bindNoImage(ImageView view,LoadImageListener listener, int idefault,int effect) {
		// TODO Auto-generated method stub
		if (isSettingNoImage()) {
			bindDefault(view,listener, idefault, getImageNotFind(),effect);
			return false;
		}
		return true;
	}

	protected boolean bindNoImage(ImageView view,LoadImageListener listener, String idefault,int effect) {
		// TODO Auto-generated method stub
		if (isSettingNoImage()) {
			bindDefault(view,listener, idefault, getImageNotFind(),effect);
			return false;
		}
		return true;
	}

	protected void bindDefault(ImageView view,LoadImageListener listener, int idefault, BitmapDrawable image,int effect) {
		// TODO Auto-generated method stub
		// 如果失败从网络上加载数据
		if (idefault == 0 && image != null) {
			bindImageBitmap(view,listener,image,effect);
		} else if(idefault > 0){
			view.setImageResource(idefault);
		}
	}

	protected void bindDefault(ImageView view,LoadImageListener listener, String idefault,BitmapDrawable image,int effect) {
		// TODO Auto-generated method stub
		// 如果失败从网络上加载数据
		if (idefault == null && image != null) {
			bindImageBitmap(view,listener,image,effect);
		} else if(idefault != null){
			AfApplication app = AfApplication.getApp();
			AfImageCaches imagecaches = AfImageCaches.getInstance();
			BitmapDrawable bitmap = imagecaches.get(app,idefault);
			if (bitmap != null) {
				bindImageBitmap(view,listener,bitmap,effect);
			} else {
				bindImageBitmap(view,listener,image,effect);
			}
		}
	}

	/**
	 * 把任务post到App的Worker执行
	 */
	private void postTask(ImageTask task) {
		// TODO Auto-generated method stub
		// 如果在正在加载列表中已经存在当前Url
		ImageTask tTask = mLoadingTask.get(task.mLinkUrl);
		if (tTask == null) {
			// 把当前Url从添加到正在加载列表
			mLoadingTask.put(task.mLinkUrl, task);
			// 把任务发送到App线程中去执行
			//AfApplication.postTask(task);
			AfDaemonThread.postTask(task);
		} else {
			//让已经存在的 task 帮助 下载
			tTask.incidentallyTake(task);
		}
	}
	

	protected class ImageTask extends AfHandlerTask{

		public int mDefaultId = 0;
		public int mEffect = EFFECT_NONE;

		public String mLinkUrl;
		public String mDefaultUrl;
		public BitmapDrawable mBitmap;
		public ImageView mImageView;
		public List<ImageTask> mltIncidentallyTask = new ArrayList<ImageTask>();

		// 标识是否可以不用下载直接读取缓存
		private Boolean mIsCanReadCaches = false;
		private LoadImageListener mListener;

		private ImageTask(String link, ImageView view,LoadImageListener listener,int effect) {
			if (view != null) {
				view.setTag(this);
			}
			this.mImageView = view;
			this.mListener = listener;
			this.mLinkUrl = link;
			this.mEffect = effect;
			mltIncidentallyTask.add(this);
		}
		
		private ImageTask(String link, ImageView view,LoadImageListener listener, int defaultid,int effect) {
			this(link,view,listener,effect);
			this.mDefaultId = defaultid;
		}

		private ImageTask(String link, ImageView view,LoadImageListener listener, String sdefault,int effect) {
			this(link,view,listener,effect);
			this.mDefaultUrl = sdefault;
		}

		@Override
		protected boolean onHandle(Message msg) {
			// TODO Auto-generated method stub
			// 如果任务成功执行完成
			if (msg.what == ImageTask.RESULT_FINISH) {
				for (ImageTask task : mltIncidentallyTask) {
					task.mBitmap = mBitmap;
					task.onFinish();
//					if (task != this) {
//						task.mLinkUrl.toString();
//					}
				}
			}
			// 如果任务执行失败
			else {
				for (ImageTask task : mltIncidentallyTask) {
					task.onFailed();
				}
			}
			// 把当前Url从正在加载列表中移除
			mLoadingTask.remove(mLinkUrl);
			return true;
		}

		private void onFailed() {
			// TODO Auto-generated method stub
			if (mListener== null || !mListener.onImageFailed(mImageView,mErrors,mException)) {
				if (mImageView != null && mImageView.getTag() == this) {
						if (mDefaultId > 0)
							//view.setImageResource(mDefaultId);
							bindDefault(mImageView,null, mDefaultId, getImageGetFail(),mEffect);
						else if (mDefaultUrl != null && mDefaultUrl.length() > 0)
							bindDefault(mImageView,null, mDefaultUrl, getImageGetFail(),mEffect);
						else
							bindImageBitmap(mImageView,null, getImageGetFail(),mEffect);
				}
			}
		}

		private void onFinish() {
			// TODO Auto-generated method stub
			if (mListener== null || !mListener.onImageLoaded(mImageView,mBitmap)) {
				if (mImageView != null && mImageView.getTag() == this) {
					bindImageBitmap(mImageView,null, mBitmap,mEffect);
				}
			}
		}

		@Override
		protected void onWorking(Message msg) throws Exception {
			// TODO Auto-generated method stub
			AfApplication app = AfApplication.getApp();
			if (mIsCanReadCaches == true) {
				AfImageCaches caches = AfImageCaches.getInstance();
				mBitmap = caches.get(app,mLinkUrl);
				if (mBitmap != null) {
					return;
				}
			}
			if (mLinkUrl.toLowerCase(Locale.ENGLISH).startsWith("http://")) {
				//mBitmap = AfFileService.downloadDrawable(app,mLinkUrl);
				DownloadEntity endity = new DownloadEntity();
				endity.DownloadUrl = mLinkUrl;
				endity.DownloadPath = AfImageCaches.getInstance().mapPath(mLinkUrl);
				DownloadTask task = new DownloadTask(endity, null);
				task.onPrepare();
				task.run();
				AfImageCaches caches = AfImageCaches.getInstance();
				mBitmap = caches.get(app,mLinkUrl);
				if (mBitmap != null) {
					return;
				}else {
					throw new AfException("未知异常，找不到文件");
				}
			}else {
				Bitmap bitmap = BitmapFactory.decodeFile(mLinkUrl);
				mBitmap = new BitmapDrawable(app.getResources(), bitmap);
				bitmap.toString();//用于当bitmap=null时抛出异常
			}
			try {
				AfImageCaches.getInstance().put(mLinkUrl, mBitmap);
			} catch (Throwable e) {
				// TODO: handle exception
				AfExceptionHandler.handler(e, "图片服务缓存到本地失败");
			}
		}

		/**
		 * 两个Task的任务相同情况下，让前一个顺便帮后一个处理 取消后一个Task
		 * @param imageTask
		 */
		private void incidentallyTake(ImageTask imageTask) {
			// TODO Auto-generated method stub
			mltIncidentallyTask.add(imageTask);
		}

	}

}
