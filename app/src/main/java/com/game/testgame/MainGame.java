package com.game.testgame;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MainGame extends SurfaceView implements Runnable {
    private Thread thread;
    private boolean isRunning = false;
    private int dropThreshold = 400;
    private int visiblePlatformCount = 6;
    private SurfaceHolder holder;
    private boolean plus_points = true;
    private boolean doodleVisible = true;

    private boolean canHandleTouch = true;
    public int scoreP = 0;
    private Paint paint;
    private float fingerX = -1;
    private Bitmap background, platform, doodle;
    private PlatformPosition[] platformsPosition;
    private int x = 100, y = 1000, h = 150;
    private float dy = 0;
    public MainGame(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        paint = new Paint();
        int platformSpacing = 400; // Відстань між платформами
        int startY = 1500;

        // Завантаження зображень
        background = BitmapFactory.decodeResource(getResources(), R.drawable.back_main);
        platform = BitmapFactory.decodeResource(getResources(), R.drawable.back_stage);
        doodle = BitmapFactory.decodeResource(getResources(), R.drawable.back_ball);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        background = Bitmap.createScaledBitmap(background, screenWidth, screenHeight, false);

        int newWidthForDoodle = 80;
        int newHeightForDoodle = 80;
        doodle = Bitmap.createScaledBitmap(doodle, newWidthForDoodle, newHeightForDoodle, false);

        int newWidthForPlatform = 200;
        int newHeightForPlatform = 50;
        platform = Bitmap.createScaledBitmap(platform, newWidthForPlatform, newHeightForPlatform, false);

        platformsPosition = new PlatformPosition[500];

        for (int i = 0; i < 500; i++) {
            platformsPosition[i] = new PlatformPosition(new Random().nextInt(500), startY);
            startY -= platformSpacing; // Зменшуємо висоту для наступної платформи
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canHandleTouch) {
            return true; // Відмовляйте від обробки подій торкання, якщо флаг вимкнуто
        }

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                // Обробка події натискання або руху пальця
                fingerX = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                // Обробка події відпускання пальця
                fingerX = -1; // Скидання координат, щоб зупинити переміщення об'єкта
                break;
        }

        return true; // Повернення true, щоб показати, що подія була оброблена
    }


    public int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0; // Повернення 0, якщо розмір не знайдено
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!holder.getSurface().isValid())
                continue;

            Canvas canvas = holder.lockCanvas();
            // Малювання гри
            drawGame(canvas);

            // Оновлення стану гри
            updateGame();

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void updateGame() {
        int endGameThreshold = getHeight();
        if (fingerX != -1) {
            // Обробка руху пальця вліво і вправо
            x = (int) fingerX - doodle.getWidth() / 2; // Центруємо об'єкт відносно пальця

            // Обмеження руху об'єкта в межах екрану
            if (x < 0) {
                x = 0;
            } else if (x + doodle.getWidth() > getWidth()) {
                x = getWidth() - doodle.getWidth();
            }
        }

        // Обробка гравітації
        dy += 0.2;
        y += dy;

        // Відскок м'яча
        if (y + doodle.getHeight() > getHeight()) {
            y = getHeight() - doodle.getHeight();
            dy = -12; // Задайте значення, наскільки м'яч відскочить вгору
        }

        // Перевірка, чи об'єкт доходить ближче до верхньої частини екрану
        if (y < dropThreshold) {
            // Опускання панелей вниз з такою самою швидкістю, як м'яч
            for (int i = 0; i < visiblePlatformCount; i++) {
                platformsPosition[i].y += 7;

                // Якщо платформа вийшла за межі екрану знизу, перемістіть її вгору
                if (platformsPosition[i].y > getHeight()) {
                    platformsPosition[i].y = -platform.getHeight(); // Почніть платформу зверху
                    platformsPosition[i].x = new Random().nextInt(getWidth() - platform.getWidth()); // Змініть X платформи
                }
            }
        }
        if (y + doodle.getHeight() >= getHeight()) {
            doodleVisible = false;
            canHandleTouch = false;
            Activity mainActivity = (Activity) getContext();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    TextView nickname = mainActivity.findViewById(R.id.nickname);
                    ImageView imageView7 = mainActivity.findViewById(R.id.imageView7);
                    TextView your_score = mainActivity.findViewById(R.id.your_score);
                    ImageView imageView8 = mainActivity.findViewById(R.id.imageView8);
                    ImageButton restart_btn = mainActivity.findViewById(R.id.restart_btn);
                    nickname.setVisibility(View.VISIBLE);
                    imageView7.setVisibility(View.VISIBLE);
                    your_score.setVisibility(View.VISIBLE);
                    imageView8.setVisibility(View.VISIBLE);
                    restart_btn.setVisibility(View.VISIBLE);
                    restart_btn.setOnClickListener(view -> {
                        doodleVisible = true;
                        canHandleTouch = true;
                        if (y + doodle.getHeight() >= getHeight()) {
                            scoreP = 0;
                        }
                        scoreP = 0;
                        nickname.setVisibility(View.INVISIBLE);
                        imageView7.setVisibility(View.INVISIBLE);
                        your_score.setVisibility(View.INVISIBLE);
                        imageView8.setVisibility(View.INVISIBLE);
                        restart_btn.setVisibility(View.INVISIBLE);
                        x = 100;
                        y = 1000;

                        restartGame();
                    });
                }
            });
        }
        Activity mainActivity = (Activity) getContext();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView score = mainActivity.findViewById(R.id.score);
                TextView your_score = mainActivity.findViewById(R.id.your_score);
                your_score.setText("" + scoreP);
                score.setText("" + scoreP);
            }
        });
        for (int i = 0; i < 10; i++) {
            if ((x + 50 > platformsPosition[i].x) &&
                    (x + 20 < platformsPosition[i].x + platform.getWidth()) &&
                    (y + 70 > platformsPosition[i].y) &&
                    (y + 70 < platformsPosition[i].y + platform.getHeight()) &&
                    (dy > 0)) {
                dy = -12;
                if(plus_points) {
                    scoreP += 100;
                }else scoreP += 0;
            }
        }

    }

    private void drawGame(Canvas canvas) {
        // Очищення екрану
        canvas.drawRGB(0, 0, 0);

        int navigationBarHeight = getNavigationBarHeight(getContext());

        // Розрахунок розміру фонового зображення
        int screenWidth = canvas.getWidth();
        int screenHeight = canvas.getHeight();

        // Встановіть координати і розміри фонового зображення
        Rect backgroundRect = new Rect(0, 0, screenWidth, screenHeight + navigationBarHeight);

        // Малювання фону
        canvas.drawBitmap(background, null, backgroundRect, paint);

        // Малювання гравця
        if (doodleVisible) {
            canvas.drawBitmap(doodle, x, y, paint);
        }
        // Малювання платформ
        for (int i = 0; i < 10; i++) {
            canvas.drawBitmap(platform, platformsPosition[i].x, platformsPosition[i].y, paint);
        }
    }

    public void resume() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void restartGame() {
        // Перезапуск гри з початковими значеннями
        x = 100;
        y = 1000;
        dy = 0;

        int platformSpacing = 300;
        int startY = 1500;
        for (int i = 0; i < visiblePlatformCount; i++) {
            platformsPosition[i].x = new Random().nextInt(getWidth() - platform.getWidth());
            platformsPosition[i].y = startY;
            startY -= platformSpacing;
        }
    }


    public void start() {
        resume();
    }

    public void stop() {
        pause();
    }
}

