package com.hungama.music.utils;


import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Purpose: Performs the Date format operation.
 */
public class DateUtils {

    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_DD_MM_YYYY_HH_MM_ss = "dd-MM-yyyy HH:mm:ss";
    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT_YYYY_MM_DD_T_HH_MM_WITH_T = "yyyy-MM-dd'T'HH:mm:ss+'Z'";
    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_TX = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static final String DATE_FORMAT_DD_MM_YYYY_slash = "dd/MM/yyyy";
    public static final String DATE_FORMAT_DD_MMMM_HH_MM = "dd MMMM, HH:mm";
    public static final String DATE_FORMAT_DD_MMMM_YYYY = "dd MMMM, yyyy";
    public static final String DATE_FORMAT_DD_MMM_YYYY = "dd MMM, yyyy";
    public static final String DATE_FORMAT_MMMM_DD_YYYY = "MMMM dd, yyyy";
    public static final String DATE_FORMAT_HH_MM_A = "HH:mm a";
    public static final String DATE_FORMAT_hh_mm_A = "hh:mm a";
    public static final String DATE_FORMAT_HH_MM = "HH:mm";
    public static final String DATE_YYYY = "yyyy";
    public static final String DATE_FORMAT_DD = "dd";
    public static final String DATE_FORMAT_MMM = "MMM";
    public static final String DATE_FORMAT_DD_MM_YYYY_HH_MM_SS = "dd-MM-yyyy HH:mm:ss.SSS";
    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";


    public static String convertDate(final String sourceDateFormat, final String destinationDateFormat, final String sourceDate) {

        if (!TextUtils.isEmpty(sourceDate)){
            final SimpleDateFormat sourceFormat = new SimpleDateFormat(sourceDateFormat);
            final SimpleDateFormat desiredFormat = new SimpleDateFormat(destinationDateFormat);


            final Date date;
            try {
                date = sourceFormat.parse(sourceDate);

                return desiredFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }


    public static String convertDate(final String sourceDateFormat, Date date) {

        final SimpleDateFormat sourceFormat = new SimpleDateFormat(sourceDateFormat, Locale.getDefault());

        return sourceFormat.format(date);
    }

    public static String convertDateToTimeStamp(final String sourceDateFormat, long milisecond) {
        milisecond=milisecond*1000;
        final SimpleDateFormat sourceFormat = new SimpleDateFormat(sourceDateFormat, Locale.getDefault());
        final Date date;
        date = new Date(milisecond);
        return sourceFormat.format(date);
    }

    public static String convertDateToTimeStampForDate(final String sourceDateFormat, long milisecond) {
        final SimpleDateFormat sourceFormat = new SimpleDateFormat(sourceDateFormat, Locale.getDefault());
        final Date date;
        date = new Date(milisecond);
        return sourceFormat.format(date);
    }



    public static String convertTimeHMS(long millis) {
        long hours = millis / 3600L;
        long minutes = (millis % 3600) / 60;
        long seconds = (millis % 3600) % 60;

        String hms = String.format("%02d hours %02d min %02d sec", hours,minutes,seconds);

        return hms;
    }

    public static String convertTimeHM(long millis) {

        String hms = String.format("%02dh %02dmin", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1));
        CommonUtils.INSTANCE.setLog("TAG", "convertTimeHM millis"+millis+" hms: "+hms);

        return hms;
    }
    public static String convertTimeMS(long millis) {
        long minutes = (millis % 3600) / 60;
        long seconds = (millis % 3600) % 60;

        String hms = String.format("%2d min %2d secs", minutes,seconds);

        return hms;
    }

    public static String changeDateTimeFormat(String strDate, String srcFormat, String destFormat) {
        Date date = new Date();
        String strFormatedDate = "";
        try {
            DateFormat formatter = new SimpleDateFormat(srcFormat);
            date = formatter.parse(strDate);
            strFormatedDate = new SimpleDateFormat(destFormat).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strFormatedDate;
    }

    public static String getCurrentDate() {
        Date date = new Date();
        String strFormatedDate = "";
        try {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
            strFormatedDate = formatter.format(date);

            //setLog("DATEUTILS", "getCurrentDateTime: " + strFormatedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strFormatedDate;
    }

    public static Date getCurrentDateTime() throws ParseException {
        Date date = new Date();
        String strFormatedDate = "";
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT_DD_MM_YYYY_HH_MM_SS);
        try {


            strFormatedDate = formatter.format(date);
            return formatter.parse(strFormatedDate);
            //setLog("DATEUTILS", "getCurrentDateTime: " + strFormatedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
    public static String getCurrentDateTimeNewFormat() {
        Date date = new Date();
        String strFormatedDate = "";
        try {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT_DD_MM_YYYY_HH_MM_SS);
            strFormatedDate = formatter.format(date);

            //setLog("DATEUTILS", "getCurrentDateTime: " + strFormatedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strFormatedDate;
    }

    public static String getCurrentDateTimeForCoin() {
        Date date = new Date();
        String strFormatedDate = "";
        try {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT_DD_MM_YYYY_HH_MM_ss);
            strFormatedDate = formatter.format(date);


            CommonUtils.INSTANCE.setLog("DATEUTILS", "getCurrentDateTime:"+strFormatedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strFormatedDate;
    }



    public static long convertDateTimeIntoMilesecond(String strDate, String srcFormat) {
        Date date = new Date();
        String strFormatedDate = "";
        try {
            DateFormat formatter = new SimpleDateFormat(srcFormat);
            date = formatter.parse(strDate);

            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Long curreentTimeStamp(){
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(new Date().getTime());
//        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
//        cal.set(Calendar.MINUTE, 0); // set minutes to zero
//        cal.set(Calendar.SECOND, 0); //set seconds to zero
//        setLog("Time", "curreentTimeStamp : "+(cal.getTimeInMillis()/1000));
//        return cal.getTimeInMillis()/1000;

         Long timeInMS=new Date().getTime();
        CommonUtils.INSTANCE.setLog("timeInMS", "curreentTimeStamp : "+timeInMS);
        return timeInMS;
    }

    /**
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getTodayStartTimeStamp(long milisecond) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(milisecond);
            cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
            cal.set(Calendar.MINUTE, 0); // set minutes to zero
            cal.set(Calendar.SECOND, 0); //set seconds to zero
            CommonUtils.INSTANCE.setLog("Time", cal.getTime().toString());
            return ""+cal.getTimeInMillis()/1000;

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }


    /**
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getTodayEndTimeStamp(long milisecond) {
        try {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(milisecond);
            cal.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
            cal.set(Calendar.MINUTE, 59); // set minutes to zero
            cal.set(Calendar.SECOND, 59); //set seconds to zero
            CommonUtils.INSTANCE.setLog("Time", cal.getTime().toString());
            return ""+cal.getTimeInMillis()/1000;


        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static Boolean isDate1AfterDate2(String date1, String date2){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD_HH_MM_ss);
        Date convertedDate = new Date();
        Date convertedDate2 = new Date();
        try {
            convertedDate = dateFormat.parse(date1);
            convertedDate2 = dateFormat.parse(date2);
            if (convertedDate2.after(convertedDate)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public static String convertLongTodateTime(Long datetime){
        try {
            Date date = new Date(datetime);
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD_HH_MM_ss);
            return dateFormat.format(date);
        }catch (Exception e){
            return "";
        }

    }

    public static int compareToDay(String date1, String date2) {
        if (TextUtils.isEmpty(date1) || TextUtils.isEmpty(date2)) {
            return 0;
        }
        return date1.compareTo(date2);
    }

    public static Date convertStringToDate(String strDate, String format){
        Date date= null;
        try {
            date = new SimpleDateFormat(format).parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getCurrentDateTime(String destinationFormat) throws ParseException {
        Date date = new Date();
        String strFormatedDate = "";
        try {
            DateFormat formatter = new SimpleDateFormat(destinationFormat);
            strFormatedDate = formatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strFormatedDate;
    }
}
