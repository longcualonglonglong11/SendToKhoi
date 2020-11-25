package com.apcs2.helperapp.entity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.helperapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HouseAdapter extends BaseAdapter {
    private ArrayList<LandMark>landMarkArrayList;
    Context context;
    String curUserId;
    @Override
    public int getCount() {
        return landMarkArrayList.size();
    }


    public HouseAdapter(ArrayList<LandMark> landMarkArrayList, Context context, String curUserID) {
        this.landMarkArrayList = landMarkArrayList;
        this.context = context;
        this.curUserId = curUserID;
    }

    @Override
    public Object getItem(int position) {
        return landMarkArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHouse viewHouse;
        LayoutInflater houseInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            viewHouse = new ViewHouse();
        } else {
            viewHouse = (ViewHouse) convertView.getTag();
        }
        convertView = houseInflater.inflate(R.layout.display_a_house, null);
        viewHouse.houseImage = (ImageView)convertView.findViewById(R.id.imageView_house_image);
        viewHouse.type = (TextView)convertView.findViewById(R.id.textView_house_type);
        viewHouse.price = (TextView)convertView.findViewById(R.id.textView_house_price);
        viewHouse.address = (TextView)convertView.findViewById(R.id.textView_house_address);
        convertView.setTag(viewHouse);
        LandMark landMark = landMarkArrayList.get(position);

        viewHouse.price.setText(landMark.getPrice());
        viewHouse.address.setText(landMark.getAddress());
        String imgUrl = landMark.getImgUrl();
        Picasso.get().load(imgUrl).into(viewHouse.houseImage);
        if (landMark.getUserId().equals(curUserId)) {
            String type_display = landMark.getCategory() + " (Your house)";
            viewHouse.type.setText(type_display);
        }
        else {
            viewHouse.type.setText(landMark.getCategory());
        }
        Log.d("NAUUU", String.valueOf(position));
        return convertView;
    }

}
