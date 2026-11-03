package com.example.myapplication.feature_drawing_charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Pure Java manager for stroke undo/redo history.
 * Decoupled from the Android View framework for deterministic unit testing.
 */
public class StrokeHistoryManager<T> {

    private final List<T> strokes = new ArrayList<>();
    private final Stack<T> redoStrokes = new Stack<>();

    public void addStroke(T stroke) {
        if (stroke != null) {
            strokes.add(stroke);
            redoStrokes.clear();
        }
    }

    public T undo() {
        if (!strokes.isEmpty()) {
            T removed = strokes.remove(strokes.size() - 1);
            redoStrokes.push(removed);
            return removed;
        }
        return null;
    }

    public T redo() {
        if (!redoStrokes.isEmpty()) {
            T restored = redoStrokes.pop();
            strokes.add(restored);
            return restored;
        }
        return null;
    }

    public void clear() {
        strokes.clear();
        redoStrokes.clear();
    }

    public int getStrokeCount() {
        return strokes.size();
    }

    public int getRedoCount() {
        return redoStrokes.size();
    }

    public List<T> getStrokes() {
        return Collections.unmodifiableList(strokes);
    }

    public boolean canUndo() {
        return !strokes.isEmpty();
    }

    public boolean canRedo() {
        return !redoStrokes.isEmpty();
    }
}

