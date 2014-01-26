package com.arbol.reegle.models;

import com.arbol.reegle.utility.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 1/20/14.
 */
public class Language {
    static final private String[] aLangNames = {
            "English", "French", "German", "Spanish", "Portuguese"
    };
    static final private String[] aLangCodes = {
            "en", "fr", "de", "es", "pt"
    };

    static public List<String> listAll(){
        return ListUtils.aToList(aLangNames);
    }

    static public String getCodes(String languages){
        String[] chosenNames = languages.split(", ");
        List<String> codes = new ArrayList<String>();
        List<String> allNames = listAll();
        for (String s: chosenNames) {
            codes.add(aLangCodes[allNames.indexOf(s)]);
        }
        return ListUtils.join(codes, ",");
    }
}
