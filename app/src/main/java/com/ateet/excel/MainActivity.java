package com.ateet.excel;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements PopupMenu.OnMenuItemClickListener {

    private static ContactAdapter mContactAdapter;
    static Cursor mCursor;
    private static final int CREATE_CONTACT = 1;
    private static final int EDIT_CONTACT = 2;
    private static final int REQUEST_PICK_FILE = 3;
    private static DBHelper db;

    private TextView empty;
    private String emptyText = "Click '+' button\n Checkout help page";
    private static TextView count;
    static AsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);
        mCursor = db.getAllContacts();
        if (mCursor.moveToFirst())
            mContactAdapter = new ContactAdapter(this, mCursor, 0);
        else mContactAdapter = new ContactAdapter(getApplicationContext(), null, 0);
        empty = (TextView) findViewById(R.id.empty);
        empty.setText(emptyText);
        ListView list = (ListView) findViewById(R.id.listView1);
        list.setEmptyView(findViewById(R.id.empty));
        list.setAdapter(mContactAdapter);
        count = (TextView) findViewById(R.id.count);
        count.setText("Total Count: " + mCursor.getCount());

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
                    empty.setText(emptyText);
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
                Intent intent = new Intent(MainActivity.this, DisplayContact.class);
                startActivityForResult(intent, CREATE_CONTACT);
                return true;
            case R.id.refresh_list:
                updateList();
                return true;
            case R.id.help:
                startActivity(new Intent(this, HelpActivity.class));
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
                String url = sharedPref.getString(getString(R.string.pref_download_key), "Input a URL(with protocol) in settings");
                if (url == null || url.length() == 0)
                    Toast.makeText(getApplicationContext(), "Input a URL(with protocol) in settings", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(getApplicationContext(), "Downloading... Please Wait", Toast.LENGTH_SHORT).show();
                    new RWAsyncTask(getApplicationContext()).execute(String.valueOf(RWAsyncTask.DOWNLOAD), url);
                }
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
                new AlertDialog.Builder(this)
                        .setTitle("Delete All")
                        .setMessage("Are you sure?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                db.deleteAll();
                                updateList();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            default:
                return super.onOptionsItemSelected(item);
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
                CharSequence[] seq = {
                        mCursor.getString(DBHelper.COL_PHONE1),
                        mCursor.getString(DBHelper.COL_PHONE2),
                        mCursor.getString(DBHelper.COL_PHONE3),
                        mCursor.getString(DBHelper.COL_PHONE4),
                        mCursor.getString(DBHelper.COL_PHONE5)
                };
                Intent intent = new Intent(Intent.ACTION_CALL);
                dialogForFieldSelection(seq, "tel:", intent);
//                String phone = "tel:" + mCursor.getString(DBHelper.COL_PHONE1);
//                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(phone));
//                if (callIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(callIntent);
//                }
                return true;
            case R.id.message:
                seq = new CharSequence[]{
                        mCursor.getString(DBHelper.COL_PHONE1),
                        mCursor.getString(DBHelper.COL_PHONE2),
                        mCursor.getString(DBHelper.COL_PHONE3),
                        mCursor.getString(DBHelper.COL_PHONE4),
                        mCursor.getString(DBHelper.COL_PHONE5)
                };
                intent = new Intent(Intent.ACTION_VIEW);
                dialogForFieldSelection(seq, "sms:", intent);
                return true;
            case R.id.mail:
//                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                        "mailto",mCursor.getString(DBHelper.COL_EMAIL), null));
//                startActivity(Intent.createChooser(emailIntent, "Send email..."));

                intent = new Intent(Intent.ACTION_SENDTO);
                seq = new CharSequence[]{
                        mCursor.getString(DBHelper.COL_EMAIL1),
                        mCursor.getString(DBHelper.COL_EMAIL2),
                        mCursor.getString(DBHelper.COL_EMAIL3),
                        mCursor.getString(DBHelper.COL_EMAIL4),
                        mCursor.getString(DBHelper.COL_EMAIL5)
                };
                dialogForFieldSelection(seq, "mailto:", intent);
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


    private void dialogForFieldSelection(final CharSequence[] items, final String prot, final Intent intent) {
        boolean flag = true;
        for (CharSequence item : items)
            if (item != null && item.length() > 0)
                flag = false;
        if (flag) {
            Toast.makeText(getApplicationContext(), "Fill at least one field", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String action = prot + items[item];
                intent.setData(Uri.parse(action));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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
                        Toast.makeText(getApplicationContext(), "Reading... Please Wait", Toast.LENGTH_SHORT).show();
                        task = new RWAsyncTask(getApplicationContext()).execute(String.valueOf(RWAsyncTask.READ_XLS), excelFile);
                        updateList();
                    }
                    break;
            }
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (task != null) {
            Toast.makeText(getApplicationContext(), "Already running, Please Wait", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            switch (item.getItemId()) {
                case R.id.read_file:
                    getFile();
                    return true;
                case R.id.write:
                    Toast.makeText(getApplicationContext(), "Writing... Please Wait", Toast.LENGTH_SHORT).show();
                    task = new RWAsyncTask(getApplicationContext()).execute(String.valueOf(RWAsyncTask.WRITE_XLS));
                    return true;
                case R.id.readPB:
                    Toast.makeText(getApplicationContext(), "Reading... Please Wait", Toast.LENGTH_SHORT).show();
                    task = new RWAsyncTask(getApplicationContext()).execute(String.valueOf(RWAsyncTask.READ_PHONEBOOK));
                    return true;
                case R.id.writePB:
                    Toast.makeText(getApplicationContext(), "Writing... Please Wait", Toast.LENGTH_SHORT).show();
                    task = new RWAsyncTask(getApplicationContext()).execute(String.valueOf(RWAsyncTask.WRITE_PHONEBOOK));
                    return true;
                default:
                    return false;
            }
        }
    }

    public static void updateList() {
        mCursor = db.getAllContacts();
        mContactAdapter.changeCursor(mCursor);
        count.setText("Total Count: " + mCursor.getCount());
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
        if (task != null) {
            Toast.makeText(getApplicationContext(), "Cancelling...", Toast.LENGTH_SHORT).show();
            task.cancel(true);
        }
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
        } else {
            mContactAdapter.changeCursor(null);
            empty.setText("No Results Found");
        }
    }


}


