package com.example.myapplication.feature_drawing_charts;

import android.view.ViewConfiguration;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.myapplication.R;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

public class DrawingCanvasView extends View {

    public static final int MODE_PEN = 0;
    public static final int MODE_ERASER = 1;

    public static class Stroke {
        public final Path path;
        public final Paint paint;

        public Stroke(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    private final StrokeHistoryManager<Stroke> historyManager = new StrokeHistoryManager<>();

    private Path currentPath;
    private Paint currentPaint;

    private int brushColor = Color.rgb(233, 30, 99);
    private float brushSize = 12f;
    private float eraserSize = 40f;
    private int currentMode = MODE_PEN;

    private float currentX;
    private float currentY;
    private float downX;
    private float downY;

    private Bitmap drawingBitmap;
    private Canvas bitmapCanvas;

    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private GestureDetector gestureDetector;

    private boolean gestureHandled = false;
    private boolean isDrawing = false;

    private final Runnable pendingTap = new Runnable() {
        @Override
        public void run() {
            // Mot lan cham ngan tao mot cham ve
            if (currentPath != null && !gestureHandled) {
                finishStroke();
            }
        }
    };

    public DrawingCanvasView(Context context) {
        super(context);
        init();
    }

    public DrawingCanvasView(
            Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setHapticFeedbackEnabled(true);
        setBackgroundColor(Color.WHITE);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        gestureDetector = new GestureDetector(
                getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        gestureHandled = true;
                        isDrawing = false;

                        removeCallbacks(pendingTap);

                        currentPath = null;
                        currentPaint = null;

                        undo();
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                        Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.msg_double_tap_undo),
                                Toast.LENGTH_SHORT
                        ).show();

                        invalidate();
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        gestureHandled = true;
                        isDrawing = false;

                        removeCallbacks(pendingTap);

                        currentPath = null;
                        currentPaint = null;

                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                        if (isEraserMode()) {
                            setPenMode();

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.msg_pen_mode),
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {
                            setEraserMode();

                            Toast.makeText(
                                    getContext(),
                                    getContext().getString(R.string.msg_eraser_mode),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        invalidate();
                    }
                }
        );
    }

    private Paint createPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        if (currentMode == MODE_ERASER) {
            paint.setStrokeWidth(eraserSize);
            paint.setXfermode(
                    new PorterDuffXfermode(
                            PorterDuff.Mode.CLEAR
                    )
            );
        } else {
            paint.setColor(brushColor);
            paint.setStrokeWidth(brushSize);
        }

        return paint;
    }

    public void setPenMode() {
        currentMode = MODE_PEN;
    }

    public void setEraserMode() {
        currentMode = MODE_ERASER;
    }

    public boolean isEraserMode() {
        return currentMode == MODE_ERASER;
    }

    public void setBrushColor(int color) {
        brushColor = color;
        currentMode = MODE_PEN;
    }

    public void setBrushSize(float size) {
        brushSize = Math.max(1f, size);
    }

    public void setEraserSize(float size) {
        eraserSize = Math.max(1f, size);
    }

    public int getBrushColor() {
        return brushColor;
    }

    public float getBrushSize() {
        return brushSize;
    }

    public float getEraserSize() {
        return eraserSize;
    }

    public StrokeHistoryManager<Stroke> getHistoryManager() {
        return historyManager;
    }

    public int getStrokeCount() {
        return historyManager.getStrokeCount();
    }

    public int getRedoCount() {
        return historyManager.getRedoCount();
    }

    @Override
    protected void onSizeChanged(
            int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            if (drawingBitmap != null) {
                drawingBitmap.recycle();
            }

            drawingBitmap = Bitmap.createBitmap(
                    w, h, Bitmap.Config.ARGB_8888
            );

            bitmapCanvas = new Canvas(drawingBitmap);

            redrawAllStrokes();
        }
    }

    private void redrawAllStrokes() {
        if (bitmapCanvas == null) return;

        bitmapCanvas.drawColor(
                Color.TRANSPARENT,
                PorterDuff.Mode.CLEAR
        );

        List<Stroke> strokes = historyManager.getStrokes();
        for (Stroke stroke : strokes) {
            bitmapCanvas.drawPath(
                    stroke.path,
                    stroke.paint
            );
        }

        invalidate();
    }

    public void undo() {
        Stroke removed = historyManager.undo();
        if (removed != null) {
            redrawAllStrokes();
        }
    }

    public void redo() {
        Stroke restored = historyManager.redo();
        if (restored != null) {
            if (bitmapCanvas != null) {
                bitmapCanvas.drawPath(restored.path, restored.paint);
            }
            invalidate();
        }
    }

    public void clearCanvas() {
        removeCallbacks(pendingTap);

        historyManager.clear();
        currentPath = null;
        currentPaint = null;
        isDrawing = false;

        redrawAllStrokes();
    }

    private void finishStroke() {
        if (currentPath != null && currentPaint != null) {
            Stroke stroke = new Stroke(currentPath, currentPaint);
            historyManager.addStroke(stroke);

            // O(1) direct drawing onto the existing bitmap backing canvas
            if (bitmapCanvas != null) {
                bitmapCanvas.drawPath(stroke.path, stroke.paint);
            }

            currentPath = null;
            currentPaint = null;

            invalidate();
        }
    }

    public boolean exportToGallery(Context context, String fileName) {
        if (drawingBitmap == null || drawingBitmap.getWidth() <= 0 || drawingBitmap.getHeight() <= 0 || context == null) {
            return false;
        }

        String name = (fileName != null && !fileName.trim().isEmpty())
                ? fileName.trim()
                : "ScienceLab_" + System.currentTimeMillis();
        if (!name.toLowerCase().endsWith(".png")) {
            name += ".png";
        }

        Bitmap exportBitmap = null;
        OutputStream outputStream = null;
        Uri imageUri = null;

        try {
            exportBitmap = Bitmap.createBitmap(
                    drawingBitmap.getWidth(),
                    drawingBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(exportBitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(drawingBitmap, 0, 0, null);

            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ScienceLab");
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
            } else {
                File directory = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "ScienceLab"
                );
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File imageFile = new File(directory, name);
                contentValues.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            }

            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (imageUri == null) {
                return false;
            }

            outputStream = resolver.openOutputStream(imageUri);
            if (outputStream == null) {
                return false;
            }

            boolean success = exportBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(imageUri, contentValues, null, null);
            }

            if (success) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            if (imageUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    context.getContentResolver().delete(imageUri, null, null);
                } catch (Exception ignored) {}
            }
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception ignored) {}
            }
            if (exportBitmap != null && !exportBitmap.isRecycled()) {
                exportBitmap.recycle();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (drawingBitmap == null) {
            return;
        }

        if (currentPath == null || currentPaint == null) {
            canvas.drawBitmap(
                    drawingBitmap, 0, 0, bitmapPaint
            );
            return;
        }

        int layer = canvas.saveLayer(
                0, 0,
                getWidth(), getHeight(),
                null
        );

        canvas.drawBitmap(
                drawingBitmap, 0, 0, bitmapPaint
        );

        canvas.drawPath(
                currentPath, currentPaint
        );

        canvas.restoreToCount(layer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gestureDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                // Neu GestureDetector vua nhan double tap
                if (gestureHandled) {
                    return true;
                }

                removeCallbacks(pendingTap);

                currentPath = new Path();
                currentPaint = createPaint();

                currentPath.moveTo(x, y);
                currentPath.lineTo(x, y);

                downX = x;
                downY = y;
                currentX = x;
                currentY = y;

                isDrawing = false;

                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:

                if (gestureHandled) {
                    return true;
                }

                if (currentPath != null) {

                    float dx = x - downX;
                    float dy = y - downY;

                    // Chi bat dau ve khi ngon tay di chuyen
                    if (!isDrawing) {
                        if (dx * dx + dy * dy < 100f) {
                            return true;
                        }
                        isDrawing = true;
                    }

                    currentPath.quadTo(
                            currentX,
                            currentY,
                            (x + currentX) / 2f,
                            (y + currentY) / 2f
                    );

                    currentX = x;
                    currentY = y;

                    invalidate();
                }

                return true;

            case MotionEvent.ACTION_UP:

                if (gestureHandled) {
                    currentPath = null;
                    currentPaint = null;
                    isDrawing = false;
                    gestureHandled = false;
                    invalidate();
                    return true;
                }

                if (currentPath != null) {
                    currentPath.lineTo(x, y);

                    if (isDrawing) {
                        finishStroke();
                    } else {
                        // Cho de phan biet cham don va nhan dup
                        postDelayed(
                                pendingTap,
                                ViewConfiguration.getDoubleTapTimeout()
                        );
                    }
                }

                performClick();
                return true;

            case MotionEvent.ACTION_CANCEL:

                removeCallbacks(pendingTap);

                currentPath = null;
                currentPaint = null;
                isDrawing = false;
                gestureHandled = false;

                invalidate();
                return true;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(pendingTap);

        if (drawingBitmap != null) {
            drawingBitmap.recycle();
            drawingBitmap = null;
            bitmapCanvas = null;
        }

        super.onDetachedFromWindow();
    }
}
