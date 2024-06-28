//untuk tampil data
package com.example.uasmaps;

import java.util.HashMap;
import java.util.Map;

public class TanggalData {
    public static String convertTanggal(String date) {
        String[] dateParts = date.split("-");
        String year = dateParts[0];
        String month = dateParts[1];
        String day = dateParts[2];

        // Convert month from number to Indonesian name
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("01", "Januari");
        monthMap.put("02", "Februari");
        monthMap.put("03", "Maret");
        monthMap.put("04", "April");
        monthMap.put("05", "Mei");
        monthMap.put("06", "Juni");
        monthMap.put("07", "Juli");
        monthMap.put("08", "Agustus");
        monthMap.put("09", "September");
        monthMap.put("10", "Oktober");
        monthMap.put("11", "November");
        monthMap.put("12", "Desember");

        return day + " " + monthMap.get(month) + " " + year;
    }
}
