package fr.milekat.cite_bungee.utils_tools;

import fr.milekat.cite_bungee.MainBungee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateMilekat {
    private static final Pattern periodPattern = Pattern.compile("([0-9]+)([smhj])");

    /**
     *      Retourne une date sous le format dd/MM/yyyy HH:mm:ss à l'heure actuelle
     * @return date
     */
    public static String setDateNow() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return df.format(new Date());
    }

    /**
     *      Retourne la Date sous le format dd/MM/yyyy HH:mm:ss
     * @param date date à convertir
     * @return en string
     */
    public static String setDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return df.format(date);
    }

    /**
     *      Converti une string dd/MM/yyyy HH:mm:ss en Java Date
     * @param date string à convertir
     * @return Java Date
     */
    public static Date getDate(String date) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            return df.parse(date);
        } catch (ParseException throwables) {
            MainBungee.warning("Impossible d'effectuer une convertion de date !");
            if (MainBungee.logDebug) throwables.printStackTrace();
            return null;
        }
    }

    /**
     *      Compare le temps entre 2 dates
     * @param date1 date de départ (Plus petite pour positif)
     * @param date2 date d'arrivée
     * @return HashMap avec D h m s ms
     */
    public static HashMap<String, String> getReamingTime(Date date1, Date date2){
        HashMap<String, String> RtHashMap = new HashMap<>();
        long diff = date1.getTime() - date2.getTime();
        RtHashMap.put("ms","" + diff);
        RtHashMap.put("s","" + diff / 1000 % 60);
        RtHashMap.put("m","" + diff / (60 * 1000) % 60);
        RtHashMap.put("h","" + diff / (60 * 60 * 1000) % 24);
        RtHashMap.put("D","" + diff / (24 * 60 * 60 * 1000));
        return RtHashMap;
    }

    /**
     *      Convertir les 4j5h9m3s en duration
     * @param period String à transformer en duration
     * @return duration
     */
    public static Long stringToPeriod(String period){
        if(period == null) return null;
        period = period.toLowerCase(Locale.ENGLISH);
        Matcher matcher = periodPattern.matcher(period);
        Instant instant=Instant.EPOCH;
        while(matcher.find()){
            int num = Integer.parseInt(matcher.group(1));
            String typ = matcher.group(2);
            switch (typ) {
                case "j":
                    instant=instant.plus(Duration.ofDays(num));
                    break;
                case "h":
                    instant=instant.plus(Duration.ofHours(num));
                    break;
                case "m":
                    instant=instant.plus(Duration.ofMinutes(num));
                    break;
                case "s":
                    instant=instant.plus(Duration.ofSeconds(num));
                    break;
            }
        }
        return instant.toEpochMilli();
    }

    /**
     *      Envoie le temps entre restant avant Date !
     * @param date joueur
     */
    public static String reamingToString(Date date) {
        HashMap<String, String> reamingMute = DateMilekat.getReamingTime(date, new Date());
        String time = "";
        if (!reamingMute.get("D").equals("0")) {
            time = reamingMute.get("D") + "jours ";
        }
        if (!reamingMute.get("h").equals("0")) {
            time = time + reamingMute.get("h") + "h ";
        }
        if (!reamingMute.get("m").equals("0")) {
            time = time + reamingMute.get("m") + "m ";
        }
        if (!reamingMute.get("s").equals("0")) {
            time = time + reamingMute.get("s") + "s ";
        }
        return Web.remLastChar(time);
    }

    /**
     *      From https://www.spigotmc.org/threads/converting-ticks-to-min-hours-days.346046/
     */
    public static String getTime(int secondsx) {
        int days = (int) TimeUnit.SECONDS.toDays(secondsx);
        int hours = (int) (TimeUnit.SECONDS.toHours(secondsx) - TimeUnit.DAYS.toHours(days));
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(secondsx) - TimeUnit.HOURS.toMinutes(hours)
                - TimeUnit.DAYS.toMinutes(days));
        int seconds = (int) (TimeUnit.SECONDS.toSeconds(secondsx) - TimeUnit.MINUTES.toSeconds(minutes)
                - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days));
        if (days != 0) {
            return days + "j, " + hours + "h, " + minutes + "m, " + seconds + "s";
        } else {
            if (hours != 0) {
                return hours + "h, " + minutes + "m, " + seconds + "s";
            } else {
                if (minutes != 0) {
                    return minutes + "m, " + seconds + "s";
                } else {
                    return seconds + "s";
                }
            }
        }
    }
}