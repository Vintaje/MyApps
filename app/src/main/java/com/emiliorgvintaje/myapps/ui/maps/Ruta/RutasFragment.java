package com.emiliorgvintaje.myapps.ui.maps.Ruta;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.maps.MapasFragment;
import com.emiliorgvintaje.myapps.ui.maps.Ruta.Lista.RutaAdapter;
import com.emiliorgvintaje.myapps.util.GPX.GPXUtil;
import com.emiliorgvintaje.myapps.util.Touch.CustomTouchListener;
import com.emiliorgvintaje.myapps.util.Touch.onItemClickListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class RutasFragment extends Fragment {


    private View root;
    private RecyclerView recyclerView;
    private MapasFragment mapasFragment;
    private RutaAdapter adapter;
    private FrameLayout frameLayout;
    private ArrayList<File> rutas;



    public RutasFragment(MapasFragment mapasFragment){
        this.mapasFragment = mapasFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root =  inflater.inflate(R.layout.rutas_fragment, container, false);
        int resId = R.anim.layout_animation;
        recyclerView = (RecyclerView) root.findViewById(R.id.rvRutas);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        frameLayout = root.findViewById(R.id.frameRutas);
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(root.getContext(), resId);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.addOnItemTouchListener(new CustomTouchListener(root.getContext(), new onItemClickListener() {
            @Override
            public void onClick(View view, int index) {

            }
        }));


        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).onBackPressed();
            }
        });

        return root;
    }

    public RutasFragment returnFragment(){
       return this;

    }



    public void load(){
        new routeLoader().execute();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        load();

    }
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);
        adapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }


    public class routeLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                rutas = new ArrayList<>(Arrays.asList(GPXUtil.readlist()));
            }catch(NullPointerException ex){
                rutas = new ArrayList<>();
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter = new RutaAdapter(rutas, returnFragment(),mapasFragment);

            recyclerView.setAdapter(adapter);

            runLayoutAnimation(recyclerView);
            recyclerView.setHasFixedSize(true);
            adapter.notifyDataSetChanged();
        }
    }


}
