package com.emiliorgvintaje.myapps.ui.musicplayer.Playing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.musicplayer.MusicFragment;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.Audio;

import java.util.ArrayList;

public class ViewSongAdapter extends PagerAdapter {


    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Audio> audiolist;
    private MusicFragment musicFragment;

    public ViewSongAdapter(Context context, ArrayList<Audio> audiolist, MusicFragment musicFragment) {
        this.context = context;
        this.audiolist = audiolist;
        this.musicFragment = musicFragment;
    }

    @Override
    public int getCount() {
        return audiolist.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.song_image_item, null);
        ImageView imageView = (ImageView)view.findViewById(R.id.image);
        imageView.setImageBitmap(musicFragment.getAlbumart(Long.parseLong(audiolist.get(position).getCaratula())));
        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);


        return view;


    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;

        vp.removeView(view);

    }

}
