package com.game.testgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Texture {
    private Bitmap image;
    private int width;
    private int height;

    public Texture(Context context, int resourceId) {
        loadFromResource(context, resourceId);
    }

    public void loadFromResource(Context context, int resourceId) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // Забороняємо автоматичне масштабування зображення

            image = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            width = image.getWidth();
            height = image.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

