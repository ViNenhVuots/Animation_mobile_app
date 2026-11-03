package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.myapplication.feature_drawing_charts.StrokeHistoryManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test suite verifying the correctness of Stroke history,
 * Two-way Undo/Redo stack operations, and Redo clearing behavior.
 */
public class DrawingCanvasUnitTest {

    private StrokeHistoryManager<String> historyManager;

    @Before
    public void setUp() {
        historyManager = new StrokeHistoryManager<>();
    }

    @Test
    public void testUndoStackState() {
        // Step 1: Add 3 strokes
        historyManager.addStroke("Stroke 1: line from (10,10) to (50,50)");
        historyManager.addStroke("Stroke 2: circle at (100,100)");
        historyManager.addStroke("Stroke 3: Bézier curve quadTo(200,200)");

        assertEquals(3, historyManager.getStrokeCount());
        assertEquals(0, historyManager.getRedoCount());
        assertTrue(historyManager.canUndo());
        assertFalse(historyManager.canRedo());

        // Step 2: Call undo 1 time
        String undone = historyManager.undo();

        assertNotNull(undone);
        assertEquals("Stroke 3: Bézier curve quadTo(200,200)", undone);
        assertEquals(2, historyManager.getStrokeCount());
        assertEquals(1, historyManager.getRedoCount());
        assertTrue(historyManager.canUndo());
        assertTrue(historyManager.canRedo());
    }

    @Test
    public void testRedoStackState() {
        // Step 1: Add 3 strokes and undo once
        historyManager.addStroke("Stroke A");
        historyManager.addStroke("Stroke B");
        historyManager.addStroke("Stroke C");

        historyManager.undo(); // removes Stroke C
        assertEquals(2, historyManager.getStrokeCount());
        assertEquals(1, historyManager.getRedoCount());

        // Step 2: Call redo -> verify stroke count is restored to 3
        String redone = historyManager.redo();

        assertNotNull(redone);
        assertEquals("Stroke C", redone);
        assertEquals(3, historyManager.getStrokeCount());
        assertEquals(0, historyManager.getRedoCount());
        assertTrue(historyManager.canUndo());
        assertFalse(historyManager.canRedo());

        // Redo on empty stack returns null safely
        String redoneAgain = historyManager.redo();
        assertNull(redoneAgain);
        assertEquals(3, historyManager.getStrokeCount());
    }

    @Test
    public void testNewStrokeClearsRedo() {
        // Step 1: Add strokes and undo 2 times
        historyManager.addStroke("Stroke 1");
        historyManager.addStroke("Stroke 2");
        historyManager.addStroke("Stroke 3");

        historyManager.undo(); // undo 3
        historyManager.undo(); // undo 2
        assertEquals(1, historyManager.getStrokeCount());
        assertEquals(2, historyManager.getRedoCount());

        // Step 2: Draw a new stroke -> the Redo stack must be completely cleared
        historyManager.addStroke("Stroke 4 (Brand New Action)");

        assertEquals(2, historyManager.getStrokeCount());
        assertEquals("Redo stack must be 0 after new stroke", 0, historyManager.getRedoCount());
        assertFalse(historyManager.canRedo());

        // Calling redo must now return null because forward history was wiped
        assertNull(historyManager.redo());
    }
}

