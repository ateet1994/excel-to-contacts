package com.ateet.excel;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ContactAdapter extends CursorAdapter{


    public ContactAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_contacts, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.nameView.setText(cursor.getString(DBHelper.COL_NAME));
        viewHolder.phoneView.setText(cursor.getString(DBHelper.COL_PHONE));
    }

    public static class ViewHolder {
        public final TextView nameView;
        public final TextView phoneView;

        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            phoneView = (TextView) view.findViewById(R.id.list_item_phone_textview);
        }
    }
}
