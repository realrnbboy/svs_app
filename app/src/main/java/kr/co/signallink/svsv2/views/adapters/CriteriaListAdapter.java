package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.location.Criteria;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.model.CriteriaModel;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.views.interfaces.RmsListClickListener;


/**
 * Created by hslee on 2020.03.19
 */

public class CriteriaListAdapter extends ArrayAdapter<CriteriaModel> {
    private Context context;
    private int itemLayoutResource;

    Resources resouces = null;

    View mainView;

    public CriteriaListAdapter(Context context, int itemLayoutResource, ArrayList<CriteriaModel> criteriaModel, Resources resouces) {
        super(context, itemLayoutResource, criteriaModel);
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

        final CriteriaModel criteriaModel = getItem(position);

        int statusColor;
        String statusString = criteriaModel.getStatus();

        if( "PROBLEM".equals(statusString) ) {
            statusColor = ContextCompat.getColor(context, R.color.myred);
        }
        else if( "CONCERN".equals(statusString) ) {
            statusColor = ContextCompat.getColor(context, R.color.myorange);
        }
        else {
            statusColor = ContextCompat.getColor(context, R.color.mygreen);
        }

        TextView textViewCriteria = view.findViewById(R.id.textViewCriteria);
        TextView textViewStatus = view.findViewById(R.id.textViewStatus);

        textViewCriteria.setText(criteriaModel.getCriteria());
        textViewStatus.setText(statusString);
        textViewStatus.setTextColor(statusColor);

        return view;
    }
}