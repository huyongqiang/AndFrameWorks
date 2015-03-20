package com.ontheway.thread;

import java.util.TimerTask;

import com.ontheway.application.AfExceptionHandler;

public abstract class AfTimerTask extends TimerTask{

	protected abstract void onTimer();

	@Override
	public final void run() {
		// TODO Auto-generated method stub
		try {
			this.onTimer();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			String remark = "AfTimerTask.run.onTimer �����쳣��\r\n";
			remark += "class = " + getClass().toString();
			AfExceptionHandler.handler(e, remark);
		}
	}
}
