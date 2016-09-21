package com.tpad.myViews;


import com.mywebrtc.util.MyLog;

import com.RTC.TpadRTC.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View
{
	private float currentX = 40;
	private float currentY = 50;
    private Bitmap rawBitmap;	
    private Paint paint;
    private Matrix matrix;
    private final String  TAG="DrawView";  
    private boolean  drawLinesShowFlag;
	/**
	 * @param context
	 */
	public DrawView(Context context , AttributeSet set)
	{
		super(context , set);
		rawBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.choselight);	
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
        matrix=new Matrix();		
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
//		super.onDraw(canvas);
		//创建画笔		
		//设置画笔的颜色
		//绘制一个小圆（作为小球）
	//	canvas.drawCircle(currentX , currentY , 15 , p);
		if (drawLinesShowFlag) {
			paint.setColor(Color.argb(0xa0,0xff,0xff,0xff));
			paint.setStrokeWidth(5.0f);
			canvas.drawLine(0, (currentY + 20.0f), 1200, (currentY + 20.0f),
					paint);// x line
			canvas.drawLine((currentX + 20.f), 0, (currentX + 20.0f), 1200,
					paint); // y line
			paint.setColor(Color.argb(0xa0,0x00,0x00,0x30));	
			if (currentX > 150.0f) {
				canvas.drawLine((currentX - 180.0f), (currentY - 20.0f),
						(currentX - 30.0f), (currentY - 20.0f), paint);// x info
																		// line
				canvas.drawLine((currentX - 30.0f), (currentY - 20.0f),
						(currentX + 20.f), (currentY + 20.0f), paint);// x info
																		// line
				paint.setTextAlign(Align.CENTER);
				paint.setTextSize(18.0f);
				canvas.drawText(String.format("(x:%f,y:%f)", (currentX + 20.f),
						(currentY + 20.0f)), (currentX - 100.0f),
						(currentY - 30.0f), paint);
			}
			else 
			{
				canvas.drawLine((currentX +30.0f), (currentY - 20.0f),
						(currentX +180.0f), (currentY - 20.0f), paint);// x info
																		// line
				canvas.drawLine((currentX +30.0f), (currentY - 20.0f),
						(currentX + 20.f), (currentY + 20.0f), paint);// x info
																		// line
				paint.setTextAlign(Align.CENTER);
				paint.setTextSize(18.0f);
				canvas.drawText(String.format("(x:%f,y:%f)", (currentX + 20.f),
						(currentY + 20.0f)), (currentX +150.0f),
						(currentY - 30.0f), paint);				
			}

		}		
	//	matrix.setTranslate((currentX+70.0f),(currentY+30.0f));
		matrix.setTranslate((currentX-40.0f),(currentY-40.0f));	
		canvas.drawBitmap(rawBitmap, matrix,null);		
	}
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//当前组件的currentX、currentY两个属性
        switch (event.getAction()) {
	        case MotionEvent.ACTION_UP:		
	        	drawLinesShowFlag=false;
	    		currentX = event.getX();
	    		currentY = event.getY();	 	        	
	    		MyLog.printf(TAG,"ACTION_UP x=%f y=%f",currentX,currentY);	 
	    		invalidate();
	            break;
	        case MotionEvent.ACTION_DOWN:
	        	drawLinesShowFlag=true;
	    		currentX = event.getX();
	    		currentY = event.getY();	 
	    		MyLog.printf(TAG,"ACTION_DOWN x=%f y=%f",currentX,currentY);
	    		invalidate();
	        	break;
	        case MotionEvent.ACTION_MOVE:
	    		currentX = event.getX();
	    		currentY = event.getY();	
	    		MyLog.printf(TAG,"ACTION_MOVE x=%f y=%f",currentX,currentY);	    		
	    		//通知改组件重绘
	    		invalidate();
	        	break;
        }
		//返回true表明处理方法已经处理该事件	    		       
		return true;
	}	
}