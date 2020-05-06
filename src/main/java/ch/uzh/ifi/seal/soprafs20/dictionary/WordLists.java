package ch.uzh.ifi.seal.soprafs20.dictionary;
import java.io.File;
import java.util.Scanner;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

//how does toArray work?

public class WordLists {

    // singelton class wordlists

    private static WordLists wordLists;

    //get path of absolute file
    private File file = new File(String.valueOf(new File("words.txt").getAbsoluteFile()));
    ArrayList<String> dictionary = new ArrayList<>();



    //private constructor
    private WordLists() throws IOException{


        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            dictionary.add(scanner.next());
        }

    }

    public static WordLists getInstance() throws IOException{
        if(wordLists == null){
            wordLists = new WordLists();
        }

        return wordLists;
    }


    //check if word is in list
    public Boolean contains(String word){
        return dictionary.contains(word.toUpperCase());
    }

}