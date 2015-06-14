package com.ateet.excel;

import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class DisplayContact extends ActionBarActivity {

    EditText name, phone1, email1, phone2, email2, phone3, email3, phone4, email4, phone5, email5;
    DBHelper db;
    Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info);

        name = (EditText) findViewById(R.id.editText);
        phone1 = (EditText) findViewById(R.id.editText2);
        phone2 = (EditText) findViewById(R.id.editText3);
        phone3 = (EditText) findViewById(R.id.editText4);
        phone4 = (EditText) findViewById(R.id.editText5);
        phone5 = (EditText) findViewById(R.id.editText6);

        email1 = (EditText) findViewById(R.id.editText7);
        email2 = (EditText) findViewById(R.id.editText8);
        email3 = (EditText) findViewById(R.id.editText9);
        email4 = (EditText) findViewById(R.id.editText10);
        email5 = (EditText) findViewById(R.id.editText11);
        db = new DBHelper(this);

        final long id = getIntent().getLongExtra("idEdit", -1);
        if (id != -1){
            Cursor res = db.getData(id);
            res.moveToFirst();
            name.setText(res.getString(res.getColumnIndex(DBHelper.COLUMN_NAME)));
            phone1.setText(res.getString(DBHelper.COL_PHONE1));
            phone2.setText(res.getString(DBHelper.COL_PHONE2));
            phone3.setText(res.getString(DBHelper.COL_PHONE3));
            phone4.setText(res.getString(DBHelper.COL_PHONE4));
            phone5.setText(res.getString(DBHelper.COL_PHONE5));

            email1.setText(res.getString(DBHelper.COL_EMAIL1));
            email2.setText(res.getString(DBHelper.COL_EMAIL2));
            email3.setText(res.getString(DBHelper.COL_EMAIL3));
            email4.setText(res.getString(DBHelper.COL_EMAIL4));
            email5.setText(res.getString(DBHelper.COL_EMAIL5));

            res.close();
        }
        save = (Button) findViewById(R.id.save_contact);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] val = {
                        name.getText().toString().trim(),
                        phone1.getText().toString().trim(),
                        phone2.getText().toString().trim(),
                        phone3.getText().toString().trim(),
                        phone4.getText().toString().trim(),
                        phone5.getText().toString().trim(),
                        email1.getText().toString().trim(),
                        email2.getText().toString().trim(),
                        email3.getText().toString().trim(),
                        email4.getText().toString().trim(),
                        email5.getText().toString().trim()

                };
                if (val[0].length() > 0 && val[1].length() > 0 ) {
                    if (id == -1)
                      db.insertContact(val);
                    else db.updateContact(id, val);
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
