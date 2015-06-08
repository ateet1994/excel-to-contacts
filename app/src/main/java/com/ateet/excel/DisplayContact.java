package com.ateet.excel;

import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class DisplayContact extends ActionBarActivity {

    EditText name, phone, email;
    DBHelper db;
    Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info);

        name = (EditText) findViewById(R.id.editText);
        phone = (EditText) findViewById(R.id.editText2);

        email = (EditText) findViewById(R.id.editText3);
        db = new DBHelper(this);

        final int id = getIntent().getIntExtra("idEdit", -1);
        if (id != -1){
            Cursor res = db.getData(id);
            res.moveToFirst();
            name.setText(res.getString(res.getColumnIndex(DBHelper.CONTACTS_COLUMN_NAME)));
            phone.setText(res.getString(res.getColumnIndex(DBHelper.CONTACTS_COLUMN_PHONE)));

            email.setText(res.getString(res.getColumnIndex(DBHelper.CONTACTS_COLUMN_EMAIL)));
            res.close();
        }
        save = (Button) findViewById(R.id.save_contact);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = name.getText().toString().trim();
                String p = phone.getText().toString().trim();

                String em = email.getText().toString().trim();
                if (n.length() > 0 && p.length() > 0 ) {
                    if (id == -1)
                      db.insertContact(n, p, em);
                    else db.updateContact(id, n, p, em);
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                    //setResult(RESULT_CANCELED);
                    //finish();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}
