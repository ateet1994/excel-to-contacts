package com.ateet.excel;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.provider.ContactsContract.CommonDataKinds;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener {

    private ContactAdapter mContactAdapter;
    private Cursor mCursor;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CREATE_CONTACT = 1;
    private static final int EDIT_CONTACT = 2;
    private static final int REQUEST_PICK_FILE = 3;
    private DBHelper db;
    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);
        mCursor = db.getAllContacts();
        if (mCursor.moveToFirst())
            mContactAdapter = new ContactAdapter(this, mCursor, 0);
        else mContactAdapter = new ContactAdapter(getApplicationContext(), null, 0);
        ListView list = (ListView) findViewById(R.id.listView1);
        list.setEmptyView(findViewById(R.id.empty));
        list.setAdapter(mContactAdapter);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

        handleIntent(getIntent());
        registerForContextMenu(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView =
                (SearchView) item.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    updateList();
                }
            }
        });
//            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
//                @Override
//                public boolean onClose() {
//                    Log.d(TAG, "close");
//                    updateList();
//                    return false;
//                }
//            });


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.create_contact:
                //Log.d("ateet", "create");
                Intent intent = new Intent(MainActivity.this, DisplayContact.class);
                startActivityForResult(intent, CREATE_CONTACT);
                return true;
            case R.id.refresh_list:
                updateList();
                return true;
            case R.id.help:
                AlertDialog.Builder alertadd = new AlertDialog.Builder(
                        MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View view = factory.inflate(R.layout.help, null);
                alertadd.setView(view);
                alertadd.setMessage(this.getString(R.string.help));
                alertadd.setTitle("Help");
                alertadd.show();
                return true;
            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(this.getString(R.string.about))
                        .setTitle("About");
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.download:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String url = sharedPref.getString(getString(R.string.pref_download_key), "Input a URL in settings");
                new DownloadExcel().execute(url);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.core:
                View view2 = findViewById(R.id.core);
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view2);
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.popupmenu);
                popupMenu.show();
                return true;
            case R.id.deleteall:
                db.deleteAll();
                updateList();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CREATE_CONTACT:
                case EDIT_CONTACT:
                    updateList();
                    break;
                case REQUEST_PICK_FILE:
                    if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
                        String excelFile;
                        excelFile = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH)).getPath();
                        Toast.makeText(getApplicationContext(), excelFile, Toast.LENGTH_SHORT).show();
                        readXls(MainActivity.this, excelFile);
                        updateList();
                    }
                    break;
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item selection
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        mCursor.moveToPosition(info.position);
        switch (item.getItemId()) {
            case R.id.call:
                String phone = "tel:" + mCursor.getString(DBHelper.COL_PHONE1);
                Toast.makeText(getApplicationContext(), phone, Toast.LENGTH_SHORT).show();
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(phone));
                if (callIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(callIntent);
                }
                return true;
            case R.id.message:
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + mCursor.getString(DBHelper.COL_PHONE1)));
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
                return true;
            case R.id.mail:
                String mail = mCursor.getString(DBHelper.COL_EMAIL1);
                if (mail.length() < 1) {
                    Toast.makeText(getApplicationContext(), "Empty Email field", Toast.LENGTH_SHORT).show();
                    return true;
                }
//                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                        "mailto",mCursor.getString(DBHelper.COL_EMAIL), null));
//                startActivity(Intent.createChooser(emailIntent, "Send email..."));

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mail));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            case R.id.edit:
                Intent editIntent = new Intent(MainActivity.this, DisplayContact.class);
                editIntent.putExtra("idEdit", mCursor.getLong(DBHelper.COL_ID));
                startActivityForResult(editIntent, EDIT_CONTACT);
                return true;
            case R.id.delete:
                db.deleteContact(mCursor.getLong(DBHelper.COL_ID));
                updateList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void getFile() {
        Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);

        // Set the initial directory to be the sdcard
        intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory().getPath());

        // Show hidden files
        //intent.putExtra(FilePickerActivity.EXTRA_SHOW_HIDDEN_FILES, true);

        // Only make .png files visible
        ArrayList<String> extensions = new ArrayList<>();
        extensions.add(".xls");
        intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);

        // Start the activity
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    private void readXls(MainActivity context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("ateet", "Storage not available or read only");
            return;
        }
        Vector<ContentValues> cVVector = new Vector<ContentValues>();

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
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cVVector.size() > 0) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                db.bulkInsert(cvArray);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.read_file:
                getFile();
                return true;
            case R.id.write:
                writeXls(getApplicationContext());
                return true;
            case R.id.readPB:
                new ReadPB().execute();
                return true;
            case R.id.writePB:
                new WritePB().execute();
                return true;
            default:
                return false;
        }
    }

    class DownloadExcel extends AsyncTask<String, String, String> {

        private String message, filename;
        private boolean success;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                String url_name = f_url[0];
                URL url = new URL(url_name);
                URLConnection connection = url.openConnection();
                connection.connect();
                if (!connection.getContentType().contains("excel"))
                    throw new Exception("Input file should be a .xls file");
                filename = url_name.substring(url_name.lastIndexOf('/'));
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(getExternalFilesDir(null) + filename);

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
                message = "Download Complete";
                success = true;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                message = e.getMessage();
                success = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (success) {
                readXls(MainActivity.this, getExternalFilesDir(null) + filename);
                updateList();
            }
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

    }

    private void writeXls(Context context) {
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
        File file = new File(context.getExternalFilesDir(null), "export.xls");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);

        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    private void updateList() {
        mCursor = db.getAllContacts();
        mContactAdapter.changeCursor(mCursor);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchKeyString(query);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCursor.close();
        db.close();
    }

    public void searchKeyString(String key) {
        String selectQuery = "SELECT  * FROM " + DBHelper.TABLE_NAME + " WHERE " + DBHelper.COLUMN_NAME + " LIKE ?";

        SQLiteDatabase dbRead = db.getReadableDatabase();
        mCursor = dbRead.rawQuery(selectQuery, new String[]{key + "%"});
        // db.rawQuery("SELECT * FROM "+table+" WHERE KEY_KEY LIKE ?", new String[] {key+"%"});
        // if you want to get everything starting with that key value
        if (mCursor.moveToFirst()) {
            mContactAdapter.changeCursor(mCursor);
        } else mContactAdapter.changeCursor(null);
    }

    public void readContactsFromPhoneBook() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String[] insert = new String[11];
                int count = 0;
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    System.out.println("name : " + name + ", ID : " + id);
                    insert[count++] = name;

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            new String[]{CommonDataKinds.Phone.NUMBER},
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
                            new String[]{CommonDataKinds.Email.ADDRESS},
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
                    db.insertContact(insert);
                }
            }
        }
        cur.close();
    }

    class ReadPB extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            readContactsFromPhoneBook();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateList();
            Toast.makeText(getApplicationContext(), "Reading complete", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeContactsToPhoneBook() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        mCursor.moveToFirst();
        for (int i = 0; i < mCursor.getCount(); i++) {

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
                            mCursor.getString(DBHelper.COL_NAME)).build());
            for (int j = 1; j < 6; j++) {
                String phone = mCursor.getString(DBHelper.cols[j]);
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
                String email = mCursor.getString(DBHelper.cols[j]);
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
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }
    class WritePB extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            writeContactsToPhoneBook();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(), getExternalFilesDir(null) + "export.xls", Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }
}


