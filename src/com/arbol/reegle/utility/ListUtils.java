package com.arbol.reegle.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 12/11/13.
 */
public class ListUtils {
    static public String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list)
        {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }
    static public String join(String[] a, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : a){
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }

    static public List<String> aToList(String[] a){
        List<String> l = new ArrayList<String>();
        for (String s : a) {
            l.add(s);
        }
        return l;
    }
}
