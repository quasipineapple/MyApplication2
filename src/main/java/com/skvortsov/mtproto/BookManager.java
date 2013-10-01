package com.skvortsov.mtproto;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by сергей on 20.08.13.
 */
public class BookManager {

    private static Book book;

    public static void BuildBook(InputStream source){
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(source);
        book = gson.fromJson(reader, Book.class);
    }

    public static Book getBook() {
        return book;
    }

    public static void setBook(Book book) {
        BookManager.book = book;
    }

}
