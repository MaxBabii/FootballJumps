package com.game.testgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;

public class GameActivity extends AppCompatActivity {
    private MainGame mainGame;
    private WebView NAME_WEB_VIEW_SHOW;
    private FirebaseRemoteConfig remoteConfig;
    private PhoneStateListener phoneStateListener;
    private ConnectivityManager.NetworkCallback networkCallback;
    private static final String ONESIGNAL_APP_ID = "c8e9c00f-8922-475c-b54a-fa82dd7e7223";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mainGame = findViewById(R.id.mainGame);

        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        FirebaseApp.initializeApp(this);
        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        remoteConfig.setConfigSettingsAsync(settings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default);
        NAME_WEB_VIEW_SHOW = findViewById(R.id.NAME_WEB_VIEW_SHOW);
        NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        WebSettings webSettings = NAME_WEB_VIEW_SHOW.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);

                boolean isSimActive = isExistsAndActiveSim(GameActivity.this);

                if (!isSimActive) {
                    NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);
                }
            }
        };
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                boolean isSimActive = isExistsAndActiveSim(GameActivity.this);
                if (isSimActive) {
                    runOnUiThread(() -> {
                        mainGame.pause();
                        onFetchAndActivateSuccess();
                        NAME_WEB_VIEW_SHOW.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);

                runOnUiThread(() -> {NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);
                });
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(),
                networkCallback
        );
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    hideUi();
                }
            }
        });
        hideUi();
        getData();
        mainGame.resume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
    @Override
    protected void onResume() {
        super.onResume();
        hideUi();
        mainGame.resume();
    }
    @Override
    public void onBackPressed() {
        if (NAME_WEB_VIEW_SHOW.canGoBack()) {
            NAME_WEB_VIEW_SHOW.goBack();
        } else if (!isExistsAndActiveSim(GameActivity.this) || !isInternetConnected()) {
            onFetchAndActivateFail();
        } else {
        }
    }
    private void getData() {
        boolean isSimActive = isExistsAndActiveSim(this);
        if (isSimActive) {
            remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if (task.isSuccessful()) {
                        if (isSimActive) onFetchAndActivateSuccess();
                    } else {
                        NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);

                        onFetchAndActivateFail();
                    }
                }
            });
        }else{

            NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);

            onFetchAndActivateFail();
        }
    }
    private void onFetchAndActivateSuccess() {
        NAME_WEB_VIEW_SHOW.setVisibility(View.VISIBLE);
        String mainLink = remoteConfig.getString("main_link");
        NAME_WEB_VIEW_SHOW.loadUrl(mainLink);
        NAME_WEB_VIEW_SHOW.setWebViewClient(new WebViewClient());

    }
    private void  onFetchAndActivateFail(){

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedNickname = sharedPreferences.getString("nickname", "");
        final Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);

        TextView nickname = findViewById(R.id.nickname);
        ImageButton restart = findViewById(R.id.restart_btn);
        ImageButton exit_btn = findViewById(R.id.back_btn);
        ImageButton info_btn = findViewById(R.id.info_game_btn);

        exit_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);
            Intent intent = new Intent(GameActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        info_btn.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);
            String policyLink = remoteConfig.getString("policy_link");

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(policyLink));
            startActivity(intent);
        });

        restart.setOnClickListener(view -> {
            view.startAnimation(buttonAnimation);
        });

        if (!savedNickname.isEmpty()) {
            nickname.setText(savedNickname);
        }
        NAME_WEB_VIEW_SHOW.setVisibility(View.INVISIBLE);

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
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
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
