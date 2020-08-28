package com.inderproduction.theinderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Calendar;

public class SplashScreenActivity extends AppCompatActivity
{
    LottieAnimationView lottieAnimationView;
    TextView wishTv;
    Animation topAnimation,middleAnimation,bottomAnimation;
    TextView middletext,bottomText,topText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        wishTv = (TextView) findViewById(R.id.wishtv) ;
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        middleAnimation = AnimationUtils.loadAnimation(this,R.anim.middle_animation);
        bottomAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);


        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            wishTv.setText("Hi \n Good Morning");
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            wishTv.setText("Hi \n Good Afternoon");
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            wishTv.setText("Good Evening");
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            wishTv.setText("Good Evening");

        }

        //hooks
                topText = findViewById(R.id.wishtv);
        bottomText = findViewById(R.id.text3);

        topText.setAnimation(topAnimation);
        bottomText.setAnimation(bottomAnimation);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainActivityIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        },6000);

        lottieAnimationView = findViewById(R.id.lottieAnimationView2);


    }
}




