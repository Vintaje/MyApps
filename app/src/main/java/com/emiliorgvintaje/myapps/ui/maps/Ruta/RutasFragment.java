package com.emiliorgvintaje.myapps.ui.maps.Ruta;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.location.Location;
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

import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.maps.MapasFragment;
import com.emiliorgvintaje.myapps.ui.maps.Ruta.Lista.RutaAdapter;
import com.emiliorgvintaje.myapps.util.GPX.GPXUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class RutasFragment extends Fragment {

    public static RutasFragment newInstance() {
        return new RutasFragment();
    }

    private View root;
    private RecyclerView recyclerView;
    private MapasFragment mapasFragment;
    private RutaAdapter adapter;
    private ArrayList<File> rutas;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root =  inflater.inflate(R.layout.rutas_fragment, container, false);
        int resId = R.anim.layout_animation;
        recyclerView = root.findViewById(R.id.rvRutas);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(root.getContext(), resId);
        recyclerView.setLayoutAnimation(animation);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new routeLoader().execute();

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

            rutas = new ArrayList<>(Arrays.asList(GPXUtil.readlist()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter = new RutaAdapter(rutas);
            recyclerView.setAdapter(adapter);

            runLayoutAnimation(recyclerView);
            recyclerView.setHasFixedSize(true);

        }
    }


}
