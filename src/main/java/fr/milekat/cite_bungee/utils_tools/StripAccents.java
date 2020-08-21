package fr.milekat.cite_bungee.utils_tools;

import java.text.Normalizer;

public class StripAccents {
    public static String stripAccents(String s)
    {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }
}
