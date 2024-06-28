package com.example.uasmaps;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    DatabaseHelper db;
    EditText username, password, repassword, dob, address, email;
    Button register;
    ImageView imageViewCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        repassword = findViewById(R.id.repassword);
        dob = findViewById(R.id.dob);
        address = findViewById(R.id.address);
        register = findViewById(R.id.register);
        email = findViewById(R.id.email);
        imageViewCalendar = findViewById(R.id.imageViewCalendar);

        imageViewCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String repass = repassword.getText().toString().trim();
                String birth = dob.getText().toString().trim();
                String addr = address.getText().toString().trim();
                String ema = email.getText().toString().trim();

                // Validasi semua field harus terisi
                if (user.isEmpty() || pass.isEmpty() || repass.isEmpty() || birth.isEmpty() || addr.isEmpty() || ema.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Semua data harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidEmail(ema)) {
                    Toast.makeText(RegisterActivity.this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.equals(repass)) {
                    if (db.cekUsername(user)) {
                        Toast.makeText(RegisterActivity.this, "Username sudah terdaftar", Toast.LENGTH_SHORT).show();
                    } else if (db.cekEmail(ema)) {
                        Toast.makeText(RegisterActivity.this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
                    }else {
                    boolean insert = db.insertData(user, pass, birth, addr, ema);
                    if (insert) {
                        Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                        intent.putExtra("USERNAME", user);
                        startActivity(intent);
//                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                        startActivity(intent);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }} else {
                    Toast.makeText(RegisterActivity.this, "Passwords Tidak Sama!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Method untuk validasi format email menggunakan regex
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Set the maximum date to December 31st, 2004
        c.set(2005, Calendar.DECEMBER, 31);
        long maxDate = c.getTimeInMillis() - 1;

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Set the selected date to the EditText

                        dob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, year, month, day);

        // Set the maximum date to 2004-12-31
        datePickerDialog.getDatePicker().setMaxDate(maxDate);

        datePickerDialog.show();
    }
}
