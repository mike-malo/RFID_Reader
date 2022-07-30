package com.hcuhf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcuhf.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ： on 2021-12-31.
 * ：630646654@qq.com
 */
public class ScanTagListAdapter extends RecyclerView.Adapter<ScanTagListAdapter.ViewHolder> {
    Context mContext;
    ArrayList<HashMap<String, String>> tagList;

    public ScanTagListAdapter(Context mContext, ArrayList<HashMap<String, String>> tagList) {
        this.mContext = mContext;
        this.tagList = tagList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_tag_list_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.tv_count.setText(tagList.get(position).get("count"));
        holder.tv_epc.setText(tagList.get(position).get("epc"));
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_epc;
        TextView tv_count;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_epc = itemView.findViewById(R.id.tv_epc);
            tv_count = itemView.findViewById(R.id.tv_count);
        }
    }
}
