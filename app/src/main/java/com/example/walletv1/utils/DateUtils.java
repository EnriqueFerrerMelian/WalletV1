package com.example.walletv1.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    // Obtener el primer día de la semana (domingo o lunes, según la configuración)
    public static long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // Primer día de la semana
        return calendar.getTimeInMillis();
    }

    // Obtener el último día de la semana (sábado o domingo, dependiendo de la región)
    public static long getEndOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // Primer día de la semana
        calendar.add(Calendar.DATE, 6); // Sumar 6 días para obtener el último día de la semana
        return calendar.getTimeInMillis();
    }

    // Obtener el primer día del mes
    public static long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // El primer día del mes
        return calendar.getTimeInMillis();
    }

    // Obtener el último día del mes
    public static long getEndOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // Último día del mes
        return calendar.getTimeInMillis();
    }

    // Obtener la fecha de hoy (inicio-medianoche)
    public static long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Establecer la hora a medianoche
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    // Obtener la fecha de hoy (fin-medianoche)
    public static long getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23); // Establecer la hora a las 23 (11:59 PM)
        calendar.set(Calendar.MINUTE, 59); // Establecer los minutos a 59
        calendar.set(Calendar.SECOND, 59); // Establecer los segundos a 59
        calendar.set(Calendar.MILLISECOND, 999); // Establecer los milisegundos a 999
        return calendar.getTimeInMillis();
    }

    // Formatear una fecha a un formato específico (por ejemplo, "dd/MM/yyyy")
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
