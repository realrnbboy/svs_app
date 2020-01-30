package kr.co.signallink.svsv2.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.signallink.svsv2.model.Matrix2Model;


/**
 * Created by hslee on 2020.01.30
 */

public class Matrix2ListAdapter extends ArrayAdapter<Matrix2Model> {
    private Context context;
    private int itemLayoutResource;

    Resources resouces = null;

    View mainView;

    public Matrix2ListAdapter(Context context, int itemLayoutResource, ArrayList<Matrix2Model> matrix2Model, Resources resouces) {
        super(context, itemLayoutResource, matrix2Model);
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

        final Matrix2Model matrix2Model = getItem(position);

        return view;
    }
}