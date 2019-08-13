package com.android.insecurebankv2;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TalkListAdapter extends BaseAdapter {
    Context context;
    ArrayList<TalkList_item> talkList_itemArrayList;

    TextView title_textView;
    TextView content_textView;
    TextView date_textView;
    TextView username_textView;

    public TalkListAdapter(Context context, ArrayList<TalkList_item> talkList_itemArrayList) {
        this.context = context;
        this.talkList_itemArrayList = talkList_itemArrayList;
    }


    @Override
    public int getCount() {
        return this.talkList_itemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.talkList_itemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list,null);
            title_textView = (TextView)convertView.findViewById(R.id.title_textview);
            content_textView = (TextView)convertView.findViewById(R.id.content_textview);
            username_textView = (TextView)convertView.findViewById(R.id.username_textview);
            date_textView = (TextView)convertView.findViewById(R.id.date_textview);

            title_textView.setText("제목 : " + talkList_itemArrayList.get(position).getTitle());
            content_textView.setText("내용 : " + talkList_itemArrayList.get(position).getContent());
            date_textView.setText("작성일 : " + talkList_itemArrayList.get(position).getDate());
            username_textView.setText("작성자 : " + talkList_itemArrayList.get(position).getUsername());
        }


        return convertView;
    }

}
