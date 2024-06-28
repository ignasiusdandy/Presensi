// ini tampil data untuk riwayat cekin
package com.example.uasmaps;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TampilDataActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView textViewSelectedDate;
    private TableLayout tableLayout;
    private TextView textViewNoData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tampil_data);

        dbHelper = new DatabaseHelper(this);

        Button buttonPickDate = findViewById(R.id.buttonPickDate);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        tableLayout = findViewById(R.id.tableLayout);
        textViewNoData = findViewById(R.id.textViewNoData);

        buttonPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Tampilkan data cekin hari ini saat pertama kali dibuka
        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(calendar.getTime());
        String tanggal = TanggalData.convertTanggal(todayDate);
        textViewSelectedDate.setText(tanggal);
        loadData(todayDate);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        CustomDatePickerDialog datePickerDialog = new CustomDatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String selectedDate = sdf.format(calendar.getTime());
                        String tanggal = TanggalData.convertTanggal(selectedDate);
                        textViewSelectedDate.setText(tanggal);
                        loadData(selectedDate);
                    }
                },
                calendar,
                new CustomDatePickerDialog.OnInvalidDateSelectedListener() {
                    @Override
                    public void onInvalidDateSelected(int dayOfWeek) {
                        if (dayOfWeek == Calendar.SATURDAY) {
                            Toast.makeText(TampilDataActivity.this, "Hari Sabtu adalah hari libur", Toast.LENGTH_SHORT).show();
                        } else if (dayOfWeek == Calendar.SUNDAY) {
                            Toast.makeText(TampilDataActivity.this, "Hari Minggu adalah hari libur", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        datePickerDialog.show();
    }

    private void loadData(String tanggal) {
        // Remove all rows except the header
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT u." + DatabaseHelper.COL_2 + ", k." + DatabaseHelper.KARYAWAN_COL_3 + ", k." + DatabaseHelper.KARYAWAN_COL_4 + ", k." + DatabaseHelper.KARYAWAN_COL_5 + ", k." + DatabaseHelper.KARYAWAN_COL_6 +
                " FROM " + DatabaseHelper.KARYAWAN_TABLE + " k INNER JOIN " + DatabaseHelper.TABLE_NAME + " u ON k." + DatabaseHelper.KARYAWAN_COL_2 + " = u." + DatabaseHelper.COL_1 +
                " WHERE k." + DatabaseHelper.KARYAWAN_COL_7 + " = ?", new String[]{tanggal});

        if (cursor != null && cursor.moveToFirst()) {
            textViewNoData.setVisibility(View.GONE);
            do {
                String username = cursor.getString(0);
                String waktuCekIn = cursor.getString(1);
                String statusCekIn = cursor.getString(2);
                String waktuCekOut = cursor.getString(3);
                String statusCekOut = cursor.getString(4);

                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView usernameTextView = new TextView(this);
                usernameTextView.setText(username);
                usernameTextView.setPadding(8, 8, 8, 8);
                usernameTextView.setTextSize(11);
                usernameTextView.setGravity(Gravity.CENTER);
                usernameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.3f));
                tableRow.addView(usernameTextView);

                TextView waktuCekInTextView = new TextView(this);
                waktuCekInTextView.setText(waktuCekIn);
                waktuCekInTextView.setPadding(8, 8, 8, 8);
                waktuCekInTextView.setTextSize(11);
                waktuCekInTextView.setGravity(Gravity.CENTER);
                waktuCekInTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f));
                tableRow.addView(waktuCekInTextView);

                TextView statusCekInTextView = new TextView(this);
                statusCekInTextView.setText(statusCekIn);
                if ("Terlambat".equals(statusCekIn)) {  // Menggunakan "Terlambat".equals(statusCekIn) untuk menghindari NPE
                    statusCekInTextView.setTextColor(ContextCompat.getColor(this, R.color.merah));
                } else if ("Hadir".equals(statusCekIn)) {
                    statusCekInTextView.setTextColor(ContextCompat.getColor(this, R.color.hijau));
                }
                statusCekInTextView.setPadding(8, 8, 8, 8);
                statusCekInTextView.setTextSize(11);
                statusCekInTextView.setGravity(Gravity.CENTER);
                statusCekInTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f));
                tableRow.addView(statusCekInTextView);

                TextView waktuCekOutTextView = new TextView(this);
                waktuCekOutTextView.setText(waktuCekOut);
                waktuCekOutTextView.setPadding(8, 8, 8, 8);
                waktuCekOutTextView.setTextSize(11);
                waktuCekOutTextView.setGravity(Gravity.CENTER);
                waktuCekOutTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f));
                tableRow.addView(waktuCekOutTextView);

                TextView statusCekOutTextView = new TextView(this);
                statusCekOutTextView.setText(statusCekOut);
                if ("Terlambat".equals(statusCekOut)) {  // Menggunakan "Terlambat".equals(statusCekOut) untuk menghindari NPE
                    statusCekOutTextView.setTextColor(ContextCompat.getColor(this, R.color.merah));
                } else if ("Hadir".equals(statusCekOut)) {
                    statusCekOutTextView.setTextColor(ContextCompat.getColor(this, R.color.hijau));
                }
                statusCekOutTextView.setPadding(8, 8, 8, 8);
                statusCekOutTextView.setTextSize(11);
                statusCekOutTextView.setGravity(Gravity.CENTER);
                statusCekOutTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f));
                tableRow.addView(statusCekOutTextView);

                tableLayout.addView(tableRow);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            textViewNoData.setVisibility(View.VISIBLE);
        }
    }
}
