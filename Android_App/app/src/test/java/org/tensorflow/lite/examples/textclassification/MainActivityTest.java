package org.tensorflow.lite.examples.textclassification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MainActivityTest {

    @Test
    public void isBlank_trueForNull() {
        assertTrue(MainActivity.isBlank(null));
    }

    @Test
    public void isBlank_trueForEmptyString() {
        assertTrue(MainActivity.isBlank(""));
    }

    @Test
    public void isBlank_trueForWhitespaceOnly() {
        assertTrue(MainActivity.isBlank("   \n\t"));
    }

    @Test
    public void isBlank_falseForRealText() {
        assertFalse(MainActivity.isBlank("Great product"));
    }
}
