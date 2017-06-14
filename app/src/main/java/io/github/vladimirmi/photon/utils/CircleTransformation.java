package io.github.vladimirmi.photon.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

/**
 * Developer Vladimir Mikhalev, 08.12.2016.
 */


public class CircleTransformation implements Transformation<Bitmap> {
    private BitmapPool mBitmapPool;
    private float mBorderWidth;

    public CircleTransformation(Context context, float borderWidth) {
        this(Glide.get(context).getBitmapPool());
        mBorderWidth = borderWidth;
    }

    public CircleTransformation(BitmapPool pool) {
        this.mBitmapPool = pool;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap result = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        BitmapShader shader =
                new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (x != 0 || y != 0) {
            // source isn't square, move viewport to center
            Matrix matrix = new Matrix();
            matrix.setTranslate(-x, -y);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r - mBorderWidth, paint);

        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(mBorderWidth);
        canvas.drawCircle(r, r, r - mBorderWidth, paint);

        return BitmapResource.obtain(result, mBitmapPool);
    }

    @Override
    public String getId() {
        return "CircleTransformation()";
    }
}
