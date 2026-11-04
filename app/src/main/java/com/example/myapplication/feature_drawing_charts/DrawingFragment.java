package com.example.myapplication.feature_drawing_charts;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class DrawingFragment extends Fragment {

    private DrawingCanvasView drawingCanvas;
    private DynamicChartView dynamicChart;
    private AnalogClockView analogClock;

    private Button btnDrawingMode;
    private Button btnChartMode;
    private Button btnClockMode;

    private LinearLayout drawingControls;
    private LinearLayout chartControls;

    private TextView txtBrushSize;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_drawing,
                container,
                false
        );

        analogClock = view.findViewById(R.id.analogClock);

        // 1. Anh xa cac View
        drawingCanvas =
                view.findViewById(R.id.drawingCanvas);

        dynamicChart =
                view.findViewById(R.id.dynamicChart);

        drawingControls =
                view.findViewById(R.id.drawingControls);

        chartControls =
                view.findViewById(R.id.chartControls);

        txtBrushSize =
                view.findViewById(R.id.txtBrushSize);

        // 2. Nut chuyen che do
        btnDrawingMode =
                view.findViewById(R.id.btnDrawingMode);

        btnChartMode =
                view.findViewById(R.id.btnChartMode);

        btnClockMode =
                view.findViewById(R.id.btnClockMode);

        // 3. Cac nut chon mau
        Button btnBlack =
                view.findViewById(R.id.btnBlack);

        Button btnRed =
                view.findViewById(R.id.btnRed);

        Button btnBlue =
                view.findViewById(R.id.btnBlue);

        Button btnGreen =
                view.findViewById(R.id.btnGreen);

        Button btnYellow =
                view.findViewById(R.id.btnYellow);

        // 4. Nut Undo, Redo, Clear, Save va Pen/Eraser
        Button btnUndo =
                view.findViewById(R.id.btnUndo);

        Button btnRedo =
                view.findViewById(R.id.btnRedo);

        Button btnClear =
                view.findViewById(R.id.btnClear);

        Button btnSave =
                view.findViewById(R.id.btnSave);

        Button btnPen =
                view.findViewById(R.id.btnPen);

        Button btnEraser =
                view.findViewById(R.id.btnEraser);

        // 5. Thanh chinh do day but
        SeekBar seekBrushSize =
                view.findViewById(R.id.seekBrushSize);

        // 6. Cac nut dieu khien bieu do
        Button btnStartChart =
                view.findViewById(R.id.btnStartChart);

        Button btnPauseChart =
                view.findViewById(R.id.btnPauseChart);

        Button btnResetChart =
                view.findViewById(R.id.btnResetChart);

        // =====================================
        // A. CHUYEN CHE DO DRAWING / CHART / CLOCK
        // =====================================

        btnDrawingMode.setOnClickListener(v -> showMode(0));
        btnChartMode.setOnClickListener(v -> showMode(1));
        btnClockMode.setOnClickListener(v -> showMode(2));

        // =====================================
        // B. CHON MAU BUT
        // =====================================

        btnBlack.setOnClickListener(v ->
                drawingCanvas.setBrushColor(Color.BLACK));

        btnRed.setOnClickListener(v ->
                drawingCanvas.setBrushColor(Color.RED));

        btnBlue.setOnClickListener(v ->
                drawingCanvas.setBrushColor(Color.BLUE));

        btnGreen.setOnClickListener(v ->
                drawingCanvas.setBrushColor(Color.GREEN));

        btnYellow.setOnClickListener(v ->
                drawingCanvas.setBrushColor(Color.YELLOW));

        // =====================================
        // C. CHINH DO DAY NET VE
        // =====================================

        seekBrushSize.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        int size = Math.max(1, progress);

                        if (drawingCanvas.isEraserMode()) {
                            drawingCanvas.setEraserSize(size);
                            txtBrushSize.setText(
                                    getString(R.string.lbl_eraser_size_format, size)
                            );
                        } else {
                            drawingCanvas.setBrushSize(size);
                            txtBrushSize.setText(
                                    getString(R.string.lbl_brush_size_format, size)
                            );
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar) {
                    }
                }
        );

        // =====================================
        // D. UNDO, REDO, CLEAR, SAVE VA PEN/ERASER
        // =====================================

        btnUndo.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            drawingCanvas.undo();
        });
        btnRedo.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            drawingCanvas.redo();
        });
        btnClear.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            drawingCanvas.clearCanvas();
        });

        btnSave.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (getContext() != null) {
                String fileName = "ScienceLab_Art_" + System.currentTimeMillis();
                boolean success = drawingCanvas.exportToGallery(getContext(), fileName);
                if (success) {
                    Toast.makeText(getContext(), getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_save_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPen.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            drawingCanvas.setPenMode();
            int size = (int) drawingCanvas.getBrushSize();
            seekBrushSize.setProgress(size);
            txtBrushSize.setText(getString(R.string.lbl_brush_size_format, size));
        });

        btnEraser.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            drawingCanvas.setEraserMode();
            int size = (int) drawingCanvas.getEraserSize();
            seekBrushSize.setProgress(size);
            txtBrushSize.setText(getString(R.string.lbl_eraser_size_format, size));
        });

        // =====================================
        // E. DIEU KHIEN DYNAMIC CHART
        // =====================================

        btnStartChart.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            dynamicChart.startAnimation();
        });
        btnPauseChart.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            dynamicChart.pauseAnimation();
        });
        btnResetChart.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            dynamicChart.resetChart();
        });

        // Mac dinh hien thi DRAWING (mode 0)
        showMode(0);

        return view;
    }

    private void showMode(int mode) {
        if (dynamicChart != null) {
            dynamicChart.pauseAnimation();
        }

        if (drawingCanvas != null) {
            drawingCanvas.setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        }
        if (dynamicChart != null) {
            dynamicChart.setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        }
        if (analogClock != null) {
            analogClock.setVisibility(mode == 2 ? View.VISIBLE : View.GONE);
        }

        if (drawingControls != null) {
            drawingControls.setVisibility(mode == 0 ? View.VISIBLE : View.GONE);
        }
        if (chartControls != null) {
            chartControls.setVisibility(mode == 1 ? View.VISIBLE : View.GONE);
        }

        updateTabUI(mode);
    }

    private void updateTabUI(int selectedTab) {
        Button[] tabs = {
                btnDrawingMode,
                btnChartMode,
                btnClockMode
        };

        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] == null) continue;

            boolean selected = (i == selectedTab);

            int color = selected
                    ? Color.parseColor("#4355C7")
                    : Color.parseColor("#64748B");

            tabs[i].setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color)
            );

            tabs[i].setTextColor(Color.WHITE);
            tabs[i].setSelected(selected);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (dynamicChart != null) {
            dynamicChart.pauseAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (dynamicChart != null) {
            dynamicChart.pauseAnimation();
        }

        drawingCanvas = null;
        dynamicChart = null;
        analogClock = null;
        btnDrawingMode = null;
        btnChartMode = null;
        btnClockMode = null;
        drawingControls = null;
        chartControls = null;
        txtBrushSize = null;
    }
}
