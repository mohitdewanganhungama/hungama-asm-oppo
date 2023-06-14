package com.hungama.music.ui.main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.hungama.music.R;
import com.hungama.music.data.model.RowsItem;
import com.hungama.music.utils.CommonUtils;
import com.hungama.music.utils.ImageLoader;
import com.hungama.music.utils.Utils;


public class Itype23PagerAdapter extends PagerAdapter {
    private int pageCount;
    private final RowsItem rowsItem;
    private final Context ctx;
    OnChildItemClick onChildItemClick;
    public interface OnChildItemClick {
        void onUserClick(int childPosition);
    }
    public Itype23PagerAdapter(RowsItem parent, Context ctx, OnChildItemClick onChildItemClick) {
        this.ctx = ctx;
        this.rowsItem = parent;
        this.pageCount = rowsItem.getItems().size();
        this.onChildItemClick = onChildItemClick;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        ViewGroup layout = (ViewGroup) LayoutInflater.from(collection.getContext())
                .inflate(R.layout.row_itype_23, collection, false);
        TextView title = layout.findViewById(R.id.tvTitle);
        TextView subTitle = layout.findViewById(R.id.tvSubTitle);
        TextView subTitle2 = layout.findViewById(R.id.tvSubTitle2);
        ImageView ivUserImage = layout.findViewById(R.id.ivUserImage);
        LinearLayoutCompat llMain = layout.findViewById(R.id.llMain);
        TextView txtRent = layout.findViewById(R.id.txtRent);
        ImageView ivRent = layout.findViewById(R.id.ivRent);
        LinearLayoutCompat llRent = layout.findViewById(R.id.llRent);
        CommonUtils.INSTANCE.applyButtonTheme(ctx, llRent);
        if (rowsItem.getItems().get(position).getData().getTitle() != null) {
            title.setText(rowsItem.getItems().get(position).getData().getTitle());
            title.setVisibility(View.VISIBLE);
        } else {
            title.setVisibility(View.GONE);
        }

        if (rowsItem.getItems().get(position).getData().getSubTitle() != null) {
            subTitle.setText(rowsItem.getItems().get(position).getData().getSubTitle());
            subTitle.setVisibility(View.VISIBLE);
        } else {
            subTitle.setVisibility(View.GONE);
        }

        if (rowsItem.getItems().get(position).getData()!=null&&rowsItem.getItems().get(position).getData().getMisc()!=null&&rowsItem.getItems().get(position).getData().getMisc().getSynopsis() != null&&!TextUtils.isEmpty(rowsItem.getItems().get(position).getData().getMisc().getSynopsis())) {
            subTitle2.setText(""+rowsItem.getItems().get(position).getData().getMisc().getSynopsis());
            subTitle2.setVisibility(View.VISIBLE);
        } else {
            subTitle2.setVisibility(View.GONE);
        }

        if(rowsItem.getItems().get(position).getData().getMisc()!=null&&rowsItem.getItems().get(position).getData().getMisc().getMovierights().size()>0){
            CommonUtils.INSTANCE.setLog("TAG", "instantiateItem:"+rowsItem.getItems().get(position).getData());
            Utils.Companion.setMovieRightTextForBucketWithPlay(txtRent, ivRent, rowsItem.getItems().get(position).getData().getMisc().getMovierights(), ctx, rowsItem.getItems().get(position).getData().getId().toString());
        }else{
            txtRent.setVisibility(View.GONE);
        }

        if (rowsItem.getItems().get(position).getData().getImage() != null) {

            ImageLoader.INSTANCE.loadImage(
                    ctx,
                    ivUserImage,
                    rowsItem.getItems().get(position).getData().getImage(),
                    R.drawable.bg_gradient_placeholder
            );
        }

        llMain.setOnClickListener(v -> {
            if (onChildItemClick != null) {
                onChildItemClick.onUserClick(position);
            }
        });

        collection.addView(layout);
        CommonUtils.INSTANCE.setLog("TAG", "instantiateItem: 111" + position);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    void setCount(int count) {
        this.pageCount = count;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(position);
    }
}