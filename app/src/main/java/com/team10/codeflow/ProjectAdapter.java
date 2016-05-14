package com.team10.codeflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 08/05/2016.
 */
public class ProjectAdapter extends ArrayAdapter {
    List list = new ArrayList();
    private ProjectsMenu mActivity;

    public ProjectAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ProjectAdapter(Context context, int resource, ProjectsMenu activity) {
        super(context, resource);
        mActivity = activity;
    }

    static class DataHandler {
        TextView p_title;
        TextView p_date;
        TextView p_desc;
        ImageButton kebab;
    }

    @Override
    public void add(Object object) {
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row;
        row = convertView;
        DataHandler handler;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.project_row_layout, parent, false);
            handler = new DataHandler();
            handler.p_title = (TextView) row.findViewById(R.id.textView_title);
            handler.p_date = (TextView) row.findViewById(R.id.textView_date);
            handler.p_desc = (TextView) row.findViewById(R.id.textView_desc);
            handler.kebab = (ImageButton) row.findViewById(R.id.imageButton_kebab);
            row.setTag(handler);
        } else {
            handler = (DataHandler) row.getTag();
        }
        ProjectInfo dataProvider;
        dataProvider = (ProjectInfo) this.getItem(position);
        handler.p_title.setText(dataProvider.getTitle());
        handler.p_date.setText(dataProvider.getDate());
        handler.p_desc.setText(dataProvider.getDesc());
        handler.kebab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                mActivity.openContextMenu(v);

            }
        });

        return row;
    }

}
