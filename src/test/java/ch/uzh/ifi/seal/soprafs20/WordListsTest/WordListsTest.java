package ch.uzh.ifi.seal.soprafs20.WordListsTest;

import ch.uzh.ifi.seal.soprafs20.dictionary.WordLists;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordListsTest {

    @Test
    void Contains_words() throws IOException {
        WordLists wordLists = WordLists.getInstance();
        wordLists.contains("hello");
        assertTrue(wordLists.contains("hello"));
        assertFalse(wordLists.contains("hlo"));
        assertTrue(wordLists.contains("heLLo"));
        assertTrue(wordLists.contains("Hello"));
        assertFalse(wordLists.contains(""));
        assertTrue(wordLists.contains("ant"));
        assertTrue(wordLists.contains("bow"));
    }

    @Test
    void Contains_words_2() throws IOException {
        WordLists wordLists = WordLists.getInstance();
        wordLists.contains("snow");
        assertTrue(wordLists.contains("Snow"));
        assertFalse(wordLists.contains("aksdfhasdf"));
        assertFalse(wordLists.contains("Sn0w"));
        assertFalse(wordLists.contains(""));
    }

    @Test
    void Contains_words_hoe() throws IOException {
        WordLists wordLists = WordLists.getInstance();
        assertTrue(wordLists.contains("hoe"));
    }
}