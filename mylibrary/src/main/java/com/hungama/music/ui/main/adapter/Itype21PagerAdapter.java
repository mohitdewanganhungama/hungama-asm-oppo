package com.hungama.music.ui.main.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.hungama.music.R;
import com.hungama.music.data.model.RowsItem;
import com.hungama.music.utils.CommonUtils;
import com.hungama.music.utils.ImageLoader;
import com.hungama.music.utils.Utils;

/**
 * @author Nikita Olifer
 */
public class Itype21PagerAdapter extends PagerAdapter {
    private int pageCount;
    private final RowsItem rowsItem;
    private final Context ctx;
    OnChildItemClick onChildItemClick;


    public Itype21PagerAdapter(RowsItem parent, Context ctx,OnChildItemClick onChildItemClick) {
        this.ctx = ctx;
        this.rowsItem = parent;
        this.pageCount = rowsItem.getItems().size();
        this.onChildItemClick = onChildItemClick;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        ViewGroup layout = (ViewGroup) LayoutInflater.from(collection.getContext())
                .inflate(R.layout.row_itype_21, collection, false);
        TextView title = layout.findViewById(R.id.tvTitle);
        TextView subTitle = layout.findViewById(R.id.tvSubTitle);
        TextView txtRent = layout.findViewById(R.id.txtRent);
        TextView tvNumber = layout.findViewById(R.id.tvNumber);
        ImageView ivUserImage = layout.findViewById(R.id.ivUserImage);
        LinearLayoutCompat llMain = layout.findViewById(R.id.llMain);
        LinearLayoutCompat llRent = layout.findViewById(R.id.llRent);
        ImageView ivRent = layout.findViewById(R.id.ivRent);
        TextView rating = layout.findViewById(R.id.txtRating);
        RelativeLayout rlRating = layout.findViewById(R.id.rlRating);
        CommonUtils.INSTANCE.applyButtonTheme(ctx, llRent);
        int tempPos = position + 1;
        if (tempPos >9) {
            tvNumber.setText("" + tempPos);
        } else {
            tvNumber.setText("0" + tempPos);

        }

            /*TextPaint paint = tvNumber.getPaint();
            float width = paint.measureText(tvNumber.getText().toString());
            float angleInRadians = (float) Math.toRadians(90);
            float length = width;

            float endX = (float) (Math.cos(angleInRadians) * length);
            float endY = (float) (Math.sin(angleInRadians) * length);


            Shader textShader = new LinearGradient(0f, 0f,endX,endY,
                    new int[]{
                            Color.parseColor("#ffffff"),
                            Color.parseColor("#00ffffff")
                    }, null, Shader.TileMode.CLAMP);
            tvNumber.getPaint().setShader(textShader);*/

        int c1 = ContextCompat.getColor(
                ctx, R.color.colorWhite
        );
        int c2 = ContextCompat.getColor(
                ctx, R.color.transparent
        );
        Shader shader = new LinearGradient(0, 0, 0, tvNumber.getTextSize(),
                new int[]{c1, Color.TRANSPARENT},
                new float[]{0, 0.9f}, Shader.TileMode.CLAMP);
        tvNumber.getPaint().setShader(shader);


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

        if(rowsItem.getItems().get(position).getData().getMisc()!=null&&rowsItem.getItems().get(position).getData().getMisc().getMovierights().size()>0){
            Utils.Companion.setMovieRightTextForBucketWithPlay(txtRent, ivRent, rowsItem.getItems().get(position).getData().getMisc().getMovierights(), ctx, rowsItem.getItems().get(position).getData().getId().toString());
        }else{
            txtRent.setVisibility(View.GONE);
        }

        if (rowsItem.getItems().get(position).getData().getMisc().getRating_critic().toString().equals("0")){
            rlRating.setVisibility(View.GONE);
        }else{
            try {
                String ratingString = rowsItem.getItems().get(position).getData().getMisc().getRating_critic();
                float ratingDouble = Float.parseFloat(ratingString);
                rating.setText(Float.toString(ratingDouble));
                rlRating.setVisibility(View.VISIBLE);
            }catch (Exception e){
                rlRating.setVisibility(View.GONE);
            }
        }

        if (rowsItem.getItems().get(position).getData().getImage() != null) {
//            CommonUtils.INSTANCE.setArtImageBg(true, rowsItem.getItems().get(position).getData().getImage(),llMain);
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

    public interface OnChildItemClick {
        void onUserClick(int childPosition);
    }
}