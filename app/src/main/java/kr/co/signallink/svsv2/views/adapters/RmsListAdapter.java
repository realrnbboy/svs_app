package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.model.RmsModel;


/**
 * Created by hslee on 2020.01.30
 */

public class RmsListAdapter extends ArrayAdapter<RmsModel> {
    private Context context;
    private int itemLayoutResource;

    Resources resouces = null;

    View mainView;

    public RmsListAdapter(Context context, int itemLayoutResource, ArrayList<RmsModel> rmsModel, Resources resouces) {
        super(context, itemLayoutResource, rmsModel);
        this.itemLayoutResource = itemLayoutResource;
        this.context = context;
        this.resouces = resouces;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(this.itemLayoutResource, null);
        }

        mainView = view;

        final RmsModel rmsModel = getItem(position);

        int rmsColor;
        int statusColor;
        String rmsString;
        String statusString;

        if( rmsModel.getRms() >= rmsModel.getDanger() ) {
            rmsColor = ContextCompat.getColor(context, R.color.myred);
            statusColor = ContextCompat.getColor(context, R.color.myred);
            rmsString = rmsModel.getRms() + "mm/s" + " (" + rmsModel.getDanger() + ")";
            statusString = "PROBLEM";
        }
        else if( rmsModel.getRms() >= rmsModel.getWarning() ) {
            rmsColor = ContextCompat.getColor(context, R.color.myblue);
            statusColor = ContextCompat.getColor(context, R.color.myorange);
            rmsString = rmsModel.getRms() + "mm/s" + " (" + rmsModel.getWarning() + ")";
            statusString = "CONCERN";
        }
        else {
            rmsColor = ContextCompat.getColor(context, R.color.myblue);
            statusColor = ContextCompat.getColor(context, R.color.mygreen);
            rmsString = rmsModel.getRms() + "mm/s";
            statusString = "GOOD";
        }

        TextView textViewName = view.findViewById(R.id.textViewName);
        TextView textViewRms = view.findViewById(R.id.textViewRms);
        TextView textViewStatus = view.findViewById(R.id.textViewStatus);

        textViewName.setText(rmsModel.getName());
        textViewRms.setText(rmsString);
        textViewRms.setTextColor(rmsColor);
        textViewStatus.setText(statusString);
        textViewStatus.setTextColor(statusColor);

        return view;
    }
}