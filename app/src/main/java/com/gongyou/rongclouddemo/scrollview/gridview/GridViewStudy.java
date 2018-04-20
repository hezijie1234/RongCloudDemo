package com.gongyou.rongclouddemo.scrollview.gridview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.gongyou.rongclouddemo.R;

public class GridViewStudy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view_study);
        GridView gridView = findViewById(R.id.scrollview);
        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView;
                if (convertView == null){
                    imageView = new ImageView(GridViewStudy.this);

                }else {
                    imageView = (ImageView) convertView;
                }
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.login_logo));
                return imageView;
            }
        });
    }
}
