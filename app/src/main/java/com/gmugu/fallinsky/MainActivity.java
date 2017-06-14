package com.gmugu.fallinsky;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private FrameLayout root;
    private GroundView groundView;
    private ImageView figure;
    private ImageView heroIv;
    private TextView gradeTv;
    private AnimationDrawable heroRunDrawable;
    private final float figureLen = 100;

    private float figureScale;
    private final int HERO_WIDTH = 60;
    private final int HERO_HEIGHT = 120;
    private int grade = 0;
    private boolean canTouth = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = (FrameLayout) findViewById(R.id.root);
        groundView = (GroundView) findViewById(R.id.ground_view);
        figure = (ImageView) findViewById(R.id.figure);
        gradeTv = (TextView) findViewById(R.id.grade);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int grounpHeight = groundView.getGrounpHeight();
                        Log.e(TAG, grounpHeight + "");
                        heroIv = new ImageView(MainActivity.this);
                        heroIv.setImageDrawable(getResources().getDrawable(R.drawable.heromask_normal));
                        heroIv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(HERO_WIDTH, HERO_HEIGHT);

                        layoutParams.setMargins(50, grounpHeight - 120, 0, 0);
                        heroIv.setLayoutParams(layoutParams);
                        root.addView(heroIv);

                    }
                });
            }
        }).start();

        root.setOnTouchListener(new View.OnTouchListener() {

            private Timer timer;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!canTouth) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    resetFigur();
//                    resetHero();

                    if (timer == null) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        figureScale += 0.05;
                                        if (figureScale >= 4) {
                                            cancel();
                                        }
                                        figure.setScaleX(figureScale);
                                        figure.setScaleY(figureScale);
                                    }
                                });
                            }
                        }, 0, 20);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    timer.cancel();
                    timer = null;
                    canTouth = false;
                    figureDownAnim();

                }

                return true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                initSound();
            }
        }).start();

    }

    private Ringtone deathSound;
    private Ringtone popSound;

    private void initSound() {
        if (deathSound == null) {
            Uri soundUri1 = Uri.parse("android.resource://" + MainActivity.this.getPackageName() + "/" + R.raw.aiiiii);
            deathSound = RingtoneManager.getRingtone(MainActivity.this, soundUri1);
            deathSound.setStreamType(AudioManager.STREAM_SYSTEM);
        }
        if (popSound == null) {
            Uri soundUri2 = Uri.parse("android.resource://" + MainActivity.this.getPackageName() + "/" + R.raw.pop);
            popSound = RingtoneManager.getRingtone(MainActivity.this, soundUri2);
            popSound.setStreamType(AudioManager.STREAM_SYSTEM);
        }

    }

    private void resetHero() {
        heroIv.clearAnimation();
        heroIv.setImageDrawable(getResources().getDrawable(R.drawable.heromask_normal));
    }

    private void resetFigur() {
        figure.clearAnimation();
        figure.setScaleX(1);
        figure.setScaleY(1);
        figureScale = 1;
    }

    private void figureDownAnim() {
        Log.e(TAG, figure.getX() + "--" + figure.getY());
        final float newLen = figureLen * figureScale;

        float toYDelta;
        float speed = 5;
        if (newLen <= groundView.getHoleSide()) {
            toYDelta = groundView.getGrounpHeight() + groundView.getHoleSide() - figure.getY() - figureLen / 2 - newLen / 2;
        } else {
            toYDelta = groundView.getGrounpHeight() - figure.getY() - figureLen / 2 - newLen / 2;
        }
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, toYDelta);
        animation.setDuration((long) (toYDelta / speed));
        animation.setFillAfter(true);
        figure.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (newLen > groundView.getHoleSide()) {
                    heroRunButDeath1();
                } else if (groundView.getHoleSide() - newLen >= 40) {
                    heroRunButDeath2();
                } else {
                    heroRunToNext();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        Log.e(TAG, groundView.getGrounpHeight() + "-" + groundView.getHoleSide() + "-" + figure.getY() + "-" + newLen);
    }

    private void heroRun() {
        if (heroRunDrawable == null) {
            heroRunDrawable = new AnimationDrawable();
            heroRunDrawable.addFrame(getResources().getDrawable(R.drawable.heromask_run1), 50);
            heroRunDrawable.addFrame(getResources().getDrawable(R.drawable.heromask_run2), 50);
        }
        heroIv.setImageDrawable(heroRunDrawable);
        heroRunDrawable.start();
    }

    private final float heroSpeed = 1;

    //被撞到
    private void heroRunButDeath1() {
        heroRun();
        float newLen = figureLen * figureScale;

        float toXDelta = groundView.getVisibleWidth() / 2 - newLen / 2 - heroIv.getX() - heroIv.getMeasuredWidth();
        TranslateAnimation animation = new TranslateAnimation(0, toXDelta, 0, 0);
        animation.setFillAfter(true);
        float duration = toXDelta / heroSpeed;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                try {
                    deathSound.play();
                    heroIv.setImageDrawable(getResources().getDrawable(R.drawable.heromask_death));
                    heroIv.setPivotX(HERO_WIDTH);
                    heroIv.setPivotY(HERO_HEIGHT);
                    heroIv.setRotation(-90);
                    heroIv.setTranslationY(-HERO_WIDTH);

                    gameOver();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (duration > 0) {
            animation.setDuration((long) duration);
            heroIv.startAnimation(animation);
        }
    }

    private void gameOver() {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("分数:" + grade).setPositiveButton("重来", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }
        }).show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    //掉下去
    private void heroRunButDeath2() {
        heroRun();

        final float toXDelta = groundView.getVisibleWidth() / 2 - groundView.getHoleSide() / 2 - heroIv.getX();
        TranslateAnimation animation = new TranslateAnimation(0, toXDelta, 0, 0);
        animation.setFillAfter(true);
        float duration = toXDelta / heroSpeed;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                try {
                    heroIv.setImageDrawable(getResources().getDrawable(R.drawable.heromask_death));

                    float toYDelta = groundView.getHoleSide() - HERO_WIDTH;
                    TranslateAnimation animation1 = new TranslateAnimation(toXDelta, toXDelta, 0, toYDelta);
                    float duration = toYDelta / heroSpeed;
                    animation1.setFillAfter(true);
                    animation1.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            heroIv.setPivotX(0);
                            heroIv.setPivotY(HERO_HEIGHT);
                            heroIv.setRotation(90);
                            deathSound.play();
                            gameOver();

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    if (duration > 0) {
                        animation1.setDuration((long) duration);
                        heroIv.startAnimation(animation1);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (duration > 0) {
            animation.setDuration((long) duration);
            heroIv.startAnimation(animation);
        }
    }

    //通过
    private void heroRunToNext() {
        heroRun();

        final float toXDelta = groundView.getVisibleWidth() / 2 - groundView.getHoleSide() / 2 - heroIv.getX() - HERO_WIDTH;
        TranslateAnimation animation = new TranslateAnimation(0, toXDelta, 0, 0);
        animation.setFillAfter(true);
        float duration = toXDelta / heroSpeed;

        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                float newLen = figureScale * figureLen;
                final float formX = (groundView.getHoleSide() - newLen) / 2 + toXDelta;
                final float toX = formX + newLen;
                float formY = groundView.getHoleSide() - newLen;
                float toY = formY;
                Log.e(TAG, formX + " " + toX + " " + formY + " " + toY);
                TranslateAnimation animation1 = new TranslateAnimation(formX, toX, formY, toY);
                animation1.setFillAfter(true);
                animation1.setAnimationListener(new Animation.AnimationListener() {
                    public Timer nextAnimTimer;

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        float fromXd = HERO_WIDTH + toX;
                        float toX = groundView.getVisibleWidth();
                        TranslateAnimation animation2 = new TranslateAnimation(fromXd, toX, 0, 0);
                        animation2.setFillAfter(true);
                        animation2.setAnimationListener(new Animation.AnimationListener() {
                            private final int times = 500;//动画持续时间(毫秒)
                            private final int frames = 60;//动画帧数

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
//                                heroIv.clearAnimation();
//                                heroIv.setTranslationX();
                                groundView.resetHoleSide();
                                groundView.resetNextCancas();

                                if (nextAnimTimer == null) {
                                    nextAnimTimer = new Timer();
                                    nextAnimTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            groundView.groundOffset += groundView.getVisibleWidth() / (times * frames / 1000);
                                            if (groundView.groundOffset >= groundView.getVisibleWidth()) {
                                                groundView.groundOffset = 0;
                                                groundView.swapGrounp();
                                                cancel();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        figure.setScaleY(1);
                                                        figure.setScaleX(1);
                                                        figure.clearAnimation();
                                                        figure.setTranslationX(0);
                                                        heroIv.clearAnimation();
                                                        heroIv.setTranslationX(0);
                                                        heroIv.setImageDrawable(getResources().getDrawable(R.drawable.heromask_normal));

                                                    }
                                                });
                                            } else {
                                                groundView.handler.sendEmptyMessage(0);
                                                figure.setTranslationX(-groundView.groundOffset);
                                                heroIv.setTranslationX(-groundView.groundOffset);
                                            }
                                            canTouth = true;
                                        }
                                    }, 0, 1000 / frames);
                                    nextAnimTimer = null;
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        long duration = (long) ((toX - fromXd) / heroSpeed);
                        if (duration > 0) {
                            animation2.setDuration(duration);
                            heroIv.startAnimation(animation2);
                        } else {
                            Log.e(TAG, "error:duration=" + duration);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                long duration = (long) (newLen / heroSpeed);
                if (duration > 0) {
                    animation1.setDuration(duration);
                    heroIv.startAnimation(animation1);
                }
                popSound.play();
                grade += 1;
                gradeTv.setText("分数:" + grade);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (duration > 0) {
            animation.setDuration((long) duration);
            heroIv.startAnimation(animation);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groundView != null) {
            groundView.recycle();
        }
    }
}
