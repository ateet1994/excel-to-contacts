package com.ateet.excel;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

public class RWAsyncTask extends AsyncTask<String, Void, Void>{

    private Context mContext;
    private DBHelper db;

    public static final int READ_XLS = 0;
    public static final int WRITE_XLS = 1;
    public static final int READ_PHONEBOOK = 2;
    public static final int WRITE_PHONEBOOK = 3;

    private boolean update;


    public RWAsyncTask(Context context) {
        this.mContext = context;
        db = new DBHelper(mContext);
    }

    private void readXls(String filename) {

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
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                db.bulkInsert(cvArray);
            }
        }
    }

    public boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    @Override
    protected Void doInBackground(String... params) {
        switch (Integer.parseInt(params[0])) {
            case READ_XLS:
                readXls(params[1]);
                update = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(update) {
            MainActivity.updateList();
            update = false;
        }
    }
}
