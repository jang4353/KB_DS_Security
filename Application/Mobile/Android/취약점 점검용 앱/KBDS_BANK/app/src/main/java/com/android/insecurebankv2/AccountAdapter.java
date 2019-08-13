package com.android.insecurebankv2;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountAdapter extends BaseAdapter {
    Context context;
    ArrayList<Account_item> account_itemArrayList;

    TextView username_textView;
    TextView account_number_textView;
    TextView balance_textView;

    public AccountAdapter(Context context, ArrayList<Account_item> account_itemArrayList) {
        this.context = context;
        this.account_itemArrayList = account_itemArrayList;
    }


    @Override
    public int getCount() {
        return this.account_itemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.account_itemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.account_item_list,null);
            username_textView = (TextView)convertView.findViewById(R.id.account_username);
            account_number_textView = (TextView)convertView.findViewById(R.id.account_account_number);
            balance_textView = (TextView)convertView.findViewById(R.id.account_balance);

            username_textView.setText("계좌주인 : " + account_itemArrayList.get(position).getUsername());
            account_number_textView.setText("계좌번호 : " + account_itemArrayList.get(position).getAccount_number());
            balance_textView.setText("남은 잔액 : " + account_itemArrayList.get(position).getBalance()+"원");
        }


        return convertView;
    }
}
