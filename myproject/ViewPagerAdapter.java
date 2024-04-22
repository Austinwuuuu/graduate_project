package com.example.myproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.lottie.LottieAnimationView;

public class ViewPagerAdapter extends PagerAdapter {

    Context context;

    int images[] = {
            R.raw.select,
            R.raw.people,
            R.raw.map,
            R.raw.painting
    };

    int headings[] = {
            R.string.textTitle1,
            R.string.textTitle2,
            R.string.textTitle3,
            R.string.textTitle4
    };

    int descriptions[] = {
            R.string.textDescription1,
            R.string.textDescription2,
            R.string.textDescription3,
            R.string.textDescription4
    };

    public ViewPagerAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide, container, false);

        LottieAnimationView animationView = view.findViewById(R.id.animationView);
        TextView slideHeading = (TextView) view.findViewById(R.id.textTitle1);
        TextView slideDescription = (TextView) view.findViewById(R.id.textDescription1);

        animationView.setAnimation(images[position]);
        slideHeading.setText(headings[position]);
        slideDescription.setText(descriptions[position]);


        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((LinearLayout) object);

    }
}
