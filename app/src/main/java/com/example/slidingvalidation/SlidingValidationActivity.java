package com.example.slidingvalidation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.example.slidingvalidation.databinding.ActivitySlidingValidationBinding;


public class SlidingValidationActivity extends AppCompatActivity {
    private ActivitySlidingValidationBinding mBinding;
    public  int phoneX = 0;//手机屏幕的宽度
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_sliding_validation);
        init();
    }

    private long downTime = 0;//第一次按下的毫秒数
    private volatile boolean isMove = false;  //是否正在移动
    private float downX = 0;  //第一次按下的位置
    private int initialWidth;  //初始化按钮的宽度

    @SuppressLint("ClickableViewAccessibility")
    protected void init() {
        getXY(this);
        final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mBinding.slidingBar.getLayoutParams();
        layoutParams.width = (int) (phoneX * 0.9);
        final ConstraintLayout.LayoutParams right = (ConstraintLayout.LayoutParams) mBinding.slidingRight.getLayoutParams();
        initialWidth = right.width;
        mBinding.slidingRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downTime = System.currentTimeMillis();
                        downX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isMove) {
                            float x = event.getX();
                            float v1 = x - downX;
                            if (v1 > 0) {//只要移动的位置不小于按下的位置，就改变长度
                                right.width = initialWidth + (int) v1;
                                if (right.width > layoutParams.width) {//如果长度已经大于背景的长度，那么直接执行成功校验的流程
                                    isMove = true;//设置为不再接收移动事件
                                    right.width = layoutParams.width;
                                    mBinding.slidingRight.setLayoutParams(right);
                                    //显示加载图标
                                    mBinding.slidingImageRight.setVisibility(View.GONE);
                                    mBinding.slidingImageLoding.setVisibility(View.VISIBLE);
                                    //发送请求....
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(1000);//模拟网络延迟,编写你自己的验证逻辑
                                                //验证成功
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //显示成功提示
                                                        mBinding.slidingImageRight.setVisibility(View.GONE);
                                                        mBinding.slidingImageLoding.setVisibility(View.GONE);
                                                        mBinding.slidingSuccessText.setVisibility(View.VISIBLE);
                                                        mBinding.slidingImageSuccess.setVisibility(View.VISIBLE);
                                                        Intent intent1 = new Intent();
                                                        intent1.putExtra("isSuccess", true);
                                                        setResult(66, intent1);
                                                        try {
                                                            Thread.sleep(300);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        //关闭并返回校验结果
                                                        finish();
                                                    }
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();

                                    //验证失败
                                    /*right.width = initialWidth;
                                    mBinding.slidingRight.setLayoutParams(right);
                                    Drawable drawable = getResources().getDrawable(R.drawable.button_yellow);
                                    mBinding.slidingBar.setBackground(drawable);
                                    mBinding.slidingPleaseText.setText(getString("验证失败"));
                                    mBinding.slidingImageRight.setVisibility(View.VISIBLE);
                                    isMove = false;*/

                                }else {
                                    mBinding.slidingRight.setLayoutParams(right);//否则只修改长度
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!isMove) {//如果中途取消。按钮返回原位
                            right.width = initialWidth;
                            mBinding.slidingRight.setLayoutParams(right);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    //获取屏幕大小
    public  void getXY(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        }
        phoneX = point.x;
    }
}
