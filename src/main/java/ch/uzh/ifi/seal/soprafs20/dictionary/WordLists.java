package ch.uzh.ifi.seal.soprafs20.dictionary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Singleton class WordList that makes a list of the backup dictionary for better
 * and easier queries
 */
public class WordLists {
    private static WordLists wordLists;
    ArrayList<String> dictionary = new ArrayList<>();

    //private constructor
    private WordLists() throws IOException{
        File file = new File(String.valueOf(new File("words.txt").getAbsoluteFile()));

        // scan every word and add it to the list

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                dictionary.add(scanner.next());
            }
        }
    }

    //public get instance method

    public static WordLists getInstance() throws IOException{
        if(wordLists == null){
            wordLists = new WordLists();
        }

        return wordLists;
    }

    //check if word is in list
    public boolean contains(String word){
        return dictionary.contains(word.toUpperCase());
    }
}