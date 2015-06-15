package com.ateet.excel;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class RWAsyncTask extends AsyncTask<String, Void, Void>{

    private Context mContext;
    private DBHelper db;

    public static final int READ_XLS = 0;
    public static final int WRITE_XLS = 1;
    public static final int READ_PHONEBOOK = 2;
    public static final int WRITE_PHONEBOOK = 3;
    public static final int DOWNLOAD = 4;

    private boolean update;
    private boolean success;
    private String message;


    public RWAsyncTask(Context context) {
        this.mContext = context;
        db = new DBHelper(mContext);
    }

    private void readXls(String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("ateet", "Storage not available or read only");
            return;
        }
        Vector<ContentValues> cVVector = new Vector<>();

        try {
            // Creating Input Stream
            InputStream myInput;


            File file = new File(filename);
            myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            if (rowIter.hasNext()) rowIter.next();
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                String[] str = new String[11];
                short count = 0;
                while (cellIter.hasNext()) {
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    myCell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    String s = myCell.getStringCellValue();
//                    if (s != null && s.length() > 0)
                    str[count++] = s;
                }
                ContentValues contactValues = new ContentValues();
                if (str[0] != null && str[1] != null && str[0].length() > 0 && str[1].length() > 0) {
//                    db.insertContact(str);
                    contactValues.put(DBHelper.COLUMN_NAME, str[0]);
                    contactValues.put(DBHelper.COLUMN_PHONE1, str[1]);
                    for (int i = 2; i < 11; i++) {
                        if (str[i] == null) str[i] = "";
                        contactValues.put(DBHelper.PROJECTIONS[i + 1], str[i]);
                    }
                    cVVector.add(contactValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            message = e.getMessage();
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                db.bulkInsert(cvArray);
                success = true;
                message = "Read Successful";
            }
            else { success = false; message = "failed";}
        }
    }

    private void writeXls() {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("ateet", "Storage not available or read only");
            return;
        }

        Workbook wb = new HSSFWorkbook();
        Cell c;
        Row row;
        int countRow = 0;

        Sheet sheet1;
        sheet1 = wb.createSheet("Sheet1");
        // Generate column headings
        SQLiteDatabase dbRead = db.getReadableDatabase();
        Cursor res = dbRead.rawQuery("select * from " + DBHelper.TABLE_NAME, null);
        res.moveToFirst();
        String[] cols = DBHelper.PROJECTIONS;
        while (!res.isAfterLast()) {
            row = sheet1.createRow(countRow++);

            for (int countCol = 1; countCol < 12; countCol++) {
                c = row.createCell(countCol - 1);
                if (countRow == 1)
                    c.setCellValue(cols[countCol]);
                else
                    c.setCellValue(res.getString(countCol));
            }

            res.moveToNext();
        }
        res.close();
        File file = new File(mContext.getExternalFilesDir(null), "export.xls");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            wb.write(os);
        } catch (Exception e) {
            success = false;
            message = e.getMessage();
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        success = true;
        message = mContext.getExternalFilesDir(null) + "/export.xls";
    }

    public boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public void readContactsFromPhoneBook() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        try {
            if (cur.getCount() > 0) {
                Vector<ContentValues> cVVector = new Vector<>();
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String[] insert = new String[11];
                    ContentValues contactValues = new ContentValues();
                    int count = 0;
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    System.out.println("name : " + name + ", ID : " + id);
                        insert[count++] = name;

                        // get the phone number
                        Cursor pCur = cr.query(ContactsContract.Data.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.Data.RAW_CONTACT_ID + "=?" + " AND "
                                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                                new String[]{String.valueOf(id)}, null);

                        while (pCur.moveToNext()) {
                            String phone = pCur.getString(
                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        System.out.println("phone" + phone);
                            insert[count++] = phone;
                        }
                        pCur.close();

                        count = 6;
                        //get email and type

                        Cursor emailCur = cr.query(ContactsContract.Data.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                                ContactsContract.Data.RAW_CONTACT_ID + "=?" + " AND "
                                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'",
                                new String[]{String.valueOf(id)}, null);
                        while (emailCur.moveToNext()) {
                            // This would allow you get several email addresses
                            // if the email addresses were stored in an array
                            String email = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//                        System.out.println("Email " + email);
                            insert[count++] = email;
                        }
                        emailCur.close();
//                    db.insertContact(insert);
                        for (int i = 0; i < 11; i++) {
                            if (insert[i] == null) insert[i] = "";
                            contactValues.put(DBHelper.PROJECTIONS[i + 1], insert[i]);
                        }
                        cVVector.add(contactValues);
                    }
                }
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    db.bulkInsert(cvArray);
                }
            }
            cur.close();
        } catch (Exception e) {
            success = false;
            message = e.getMessage();
        }
        success = true;
        message = "Read Successful";
    }

    public void writeContactsToPhoneBook() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        Cursor cur = MainActivity.mCursor;
        cur.moveToFirst();
        for (int i = 0; i < cur.getCount(); i++) {

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());


            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            cur.getString(DBHelper.COL_NAME)).build());
            for (int j = 1; j < 6; j++) {
                String phone = cur.getString(DBHelper.cols[j]);
                if (phone != null)
                    ops.add(ContentProviderOperation.
                            newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build());
            }

            for (int j = 6; j < 11; j++) {
                String email = cur.getString(DBHelper.cols[j]);
                if (email != null)
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                            .build());
            }

            try {
                mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                success = false;
                message = e.getMessage();
            }

        }
        success = true;
        message = "Write Successful";
    }

    private String downloadXls(String urlString) {
        int count;
        String filename, location;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.connect();
            if (!connection.getContentType().contains("excel"))
                throw new Exception("Input file should be a .xls file");
            filename = urlString.substring(urlString.lastIndexOf('/'));
            location = mContext.getExternalFilesDir(null) + filename;
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            OutputStream output = new FileOutputStream(location);

            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            message = location;
            success = true;
            return location;

        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            return null;
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        switch (Integer.parseInt(params[0])) {
            case DOWNLOAD:
                params[1] = downloadXls(params[1]);
                update = true;
                if (params[1] == null)
                    break;

            case READ_XLS:
                readXls(params[1]);
                update = true;
                break;
            case WRITE_XLS:
                writeXls();
                update = false;
                break;
            case READ_PHONEBOOK:
                readContactsFromPhoneBook();
                update = true;
                break;
            case WRITE_PHONEBOOK:
                writeContactsToPhoneBook();
                update = false;
                break;

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (success) {
            if (update) {
                MainActivity.updateList();
                update = false;
            }
        }
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        db.close();
    }
}
