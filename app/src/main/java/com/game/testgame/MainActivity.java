package com.game.testgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.game.testgame.R;
import com.game.testgame.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {
    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        remoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(40)
                .build();
        remoteConfig.setConfigSettingsAsync(settings);

        remoteConfig.setDefaultsAsync(R.xml.remote_config_default);

        ImageView loading_img = findViewById(R.id.imageView6);

        RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotate.setDuration(5000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        loading_img.startAnimation(rotate);
        boolean isSimActive = isExistsAndActiveSim(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(1000);
                fadeOut.setFillAfter(true);
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(1000);
                fadeIn.setFillAfter(true);
                ImageView imageView = findViewById(R.id.imageView);
                ImageView loading_img = findViewById(R.id.imageView6);
                imageView.startAnimation(fadeOut);
                loading_img.startAnimation(fadeOut);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(intent);
                        if (!isSimActive) {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        } else {
                            overridePendingTransition(0, 0);
                        }

                        finish();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        }, 5000);
        remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    boolean isSimActive = isExistsAndActiveSim(MainActivity.this);
                    if (!isSimActive) {
                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                                fadeOut.setDuration(1000);
                                fadeOut.setFillAfter(true);
                                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                fadeIn.setDuration(1000);
                                fadeIn.setFillAfter(true);
                                ImageView imageView = findViewById(R.id.imageView);
                                ImageView loading_img = findViewById(R.id.imageView6);
                                imageView.startAnimation(fadeOut);
                                loading_img.startAnimation(fadeOut);

                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                        startActivity(intent);
                                        if (!isSimActive) {
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        } else {
                                            overridePendingTransition(0, 0);
                                        }

                                        finish();

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                            }
                        }, 5000);
                    }
                }
            }
        });
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Якщо системні панелі навігації видимі, ховаємо їх
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideUi();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        hideUi();
    }
    public static boolean isExistsAndActiveSim(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telephonyManager.getSimState();
            return simState == TelephonyManager.SIM_STATE_READY;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void hideUi() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat insetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
            if (insetsController != null) {
                insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                insetsController.hide(WindowInsetsCompat.Type.systemBars());
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}