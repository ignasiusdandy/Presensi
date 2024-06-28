package com.example.uasmaps;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "user.db";
    public static final String TABLE_NAME = "user_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "USERNAME";
    public static final String COL_3 = "PASSWORD";
    public static final String COL_4 = "DOB";
    public static final String COL_5 = "ADDRESS";
    public static final String COL_6 = "email";

    public static final String KARYAWAN_TABLE = "tabel_karyawan";
    public static final String KARYAWAN_COL_1 = "ID";
    public static final String KARYAWAN_COL_2 = "USER_ID"; // Foreign key ke tabel_user
    public static final String KARYAWAN_COL_3 = "waktuCekIn";
    public static final String KARYAWAN_COL_4 = "statusCekIn";
    public static final String KARYAWAN_COL_5 = "waktuCekOut";
    public static final String KARYAWAN_COL_6 = "statusCekOut";
    public static final String KARYAWAN_COL_7 = "tanggal";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, PASSWORD TEXT, DOB TEXT, ADDRESS TEXT, email TEXT)");

        db.execSQL("CREATE TABLE " + KARYAWAN_TABLE + " ("
                + KARYAWAN_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KARYAWAN_COL_2 + " INTEGER, "
                + KARYAWAN_COL_3 + " TEXT, "
                + KARYAWAN_COL_4 + " TEXT, "
                + KARYAWAN_COL_5 + " TEXT, "
                + KARYAWAN_COL_6 + " TEXT, "
                + KARYAWAN_COL_7 + " TEXT, " // Kolom baru untuk tanggal
                + "FOREIGN KEY (" + KARYAWAN_COL_2 + ") REFERENCES " + TABLE_NAME + "(" + COL_1 + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + KARYAWAN_TABLE);
        onCreate(db);
    }

    public boolean insertData(String username, String password, String dob, String address, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, username);
        contentValues.put(COL_3, password);
        contentValues.put(COL_4, dob);
        contentValues.put(COL_5, address);
        contentValues.put(COL_6, email);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

        public boolean cekUsername(String username){
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_2 + " = ?";
            Cursor cursor = db.rawQuery(query, new  String[]{username});
            boolean exists = cursor.getCount()>0;
            cursor.close();
            return exists;
        }
        public boolean cekEmail(String email) {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_6 + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{email});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        }

    public boolean insertDataKaryawan(Context context, String username, String waktuCekIn, String statusCekIn, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Dapatkan ID pengguna dari username
        Cursor cursor = db.rawQuery("SELECT " + COL_1 + " FROM " + TABLE_NAME + " WHERE " + COL_2 + " = ?", new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(COL_1));
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put(KARYAWAN_COL_2, userId);
            contentValues.put(KARYAWAN_COL_3, waktuCekIn);
            contentValues.put(KARYAWAN_COL_4, statusCekIn);
            contentValues.put(KARYAWAN_COL_7, tanggal); // Tambahkan tanggal
            long result = db.insert(KARYAWAN_TABLE, null, contentValues);
            if (result != -1) {
                // Penyisipan berhasil
                return true;
            } else {
                // Penyisipan gagal, tampilkan toast
                Toast.makeText(context, "Gagal menambahkan data ke database", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(context, "User tidak ada", Toast.LENGTH_SHORT).show();
            // Username tidak ditemukan
            return false;
        }
    }

    public void updateDataKaryawan(String username, String waktuCekOut, String statusCekOut, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Query untuk mendapatkan userId berdasarkan username
        Cursor cursor = db.rawQuery("SELECT " + COL_1 + " FROM " + TABLE_NAME + " WHERE " + COL_2 + " = ?", new String[]{username});
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(COL_1));
            cursor.close();
        } else {
            // Handle kondisi jika userId tidak ditemukan
            cursor.close();
            return;
        }

        // Membuat ContentValues untuk pembaruan data
        ContentValues values = new ContentValues();
        values.put(KARYAWAN_COL_5, waktuCekOut);
        values.put(KARYAWAN_COL_6, statusCekOut);

        // Update data karyawan berdasarkan userId dan tanggal
        db.update(KARYAWAN_TABLE, values, KARYAWAN_COL_2 + "=? AND " + KARYAWAN_COL_7 + "=?", new String[]{String.valueOf(userId), tanggal});
    }



    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE email=? AND PASSWORD=?", new String[]{email, password});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public String dapatUsername(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT USERNAME FROM " + TABLE_NAME + " WHERE email=? AND PASSWORD=?", new String[]{email, password});
        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(COL_2));
            cursor.close();
            return username;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
    // Metode untuk memuat data absensi terakhir pengguna
    public Cursor getLastPresenceData(String username, String date) {
        SQLiteDatabase db = this.getReadableDatabase(); // Mendapatkan instance database yang dapat dibaca
        String query = "SELECT * FROM " + KARYAWAN_TABLE + " WHERE " + KARYAWAN_COL_2 +
                " = (SELECT " + COL_1 + " FROM " + TABLE_NAME + " WHERE " + COL_2 + " = ?) AND " +
                KARYAWAN_COL_7 + " = ?"; // Menyusun query SQL untuk mendapatkan data absensi
        return db.rawQuery(query, new String[]{username, date}); // Menjalankan query dan mengembalikan hasilnya dalam bentuk Cursor
    }
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

}
