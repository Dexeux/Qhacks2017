package com.example.chrisexn.hug;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chrisexn on 2/4/2017.
 */

public class DrawerAdapter extends ArrayAdapter {

    public DrawerAdapter(Context context, Object[] objects) {
        super(context, R.layout.drawer_element, objects);
        this.context = context;
        this.layoutResourceId =  R.layout.drawer_element;
    }

    private Context context;
    private int layoutResourceId;
    static class ViewHolder {
        ImageView img;
        TextView text;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String obj = (String)getItem(position);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View v = convertView;
        ViewHolder viewHolder;
        if (v == null) {
            viewHolder = new ViewHolder();
            v = inflater.inflate(layoutResourceId, parent, false);
            viewHolder.img =  (ImageView) v.findViewById(R.id.drawer_icon);
            viewHolder.text = (TextView) v.findViewById(R.id.drawer_text);
            v.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)v.getTag();
        }
        viewHolder.text.setText(obj);
        if(obj.equals("Logout")){
            viewHolder.img.setImageResource(R.mipmap.ic_logout);
        }else if (obj.equals("Hugs")){
            viewHolder.img.setImageResource(R.mipmap.ic_hug);
        }

        return v;
    }

}
