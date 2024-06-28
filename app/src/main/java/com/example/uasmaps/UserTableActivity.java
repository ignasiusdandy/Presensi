// ini mengarahkan ke tabel karyawan

package com.example.uasmaps;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Date;

public class UserTableActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_table);

        tableLayout = findViewById(R.id.tableLayout);
        databaseHelper = new DatabaseHelper(this);

        loadUserData();
    }

    private void loadUserData() {
        Cursor cursor = databaseHelper.getAllUsers();
        int counter = 1;

        if (cursor.moveToFirst()) {
            do {
                TableRow tableRow = new TableRow(this);

                TextView idTextView = new TextView(this);
                idTextView.setText(String.valueOf(counter++));
                idTextView.setPadding(8, 8, 8, 8);
                idTextView.setGravity(Gravity.CENTER);
                tableRow.addView(idTextView);

                TextView nameTextView = new TextView(this);
                nameTextView.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_2)));
                nameTextView.setPadding(8, 8, 8, 8);
                tableRow.addView(nameTextView);

                TextView emailTextView = new TextView(this);
                emailTextView.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_6)));
                emailTextView.setPadding(8, 8, 8, 8);
                tableRow.addView(emailTextView);

                TextView ageTextView = new TextView(this);
                ageTextView.setText(String.valueOf(calculateAge(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_4)))));
                ageTextView.setPadding(8, 8, 8, 8);
                ageTextView.setGravity(Gravity.CENTER);
                tableRow.addView(ageTextView);

                tableLayout.addView(tableRow);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private int calculateAge(String dob) {
        // Coba format tanggal yang berbeda
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy"};
        Date date = null;
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                date = sdf.parse(dob);
                break;
            } catch (Exception e) {
                // Lanjutkan ke format berikutnya
            }
        }

        if (date == null) {
            // Jika tidak ada format yang cocok, kembalikan -1 atau nilai default lainnya
            return -1;
        }

        Calendar dobCal = Calendar.getInstance();
        dobCal.setTime(date);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}
