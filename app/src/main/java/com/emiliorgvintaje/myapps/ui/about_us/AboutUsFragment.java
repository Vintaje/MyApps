package com.emiliorgvintaje.myapps.ui.about_us;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

public class AboutUsFragment extends Fragment {

    private Button btAboutTwitter;
    private Button btAboutEnviar;
    private EditText tietAboutNombre;
    private EditText tietAboutAsunto;
    private EditText tietAboutMensaje;
    private ImageView ivMyPhoto;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_aboutus, container, false);
        ((MainActivity) getActivity()).hideFloatingActionButton();

        btAboutTwitter = (Button)root.findViewById(R.id.btAboutTwitter);

        btAboutEnviar = (Button) root.findViewById(R.id.btAboutEnviar);

        tietAboutAsunto = (EditText) root.findViewById(R.id.tietAboutAsunto);
        tietAboutMensaje = (EditText) root.findViewById(R.id.tietAboutMensaje);
        tietAboutNombre = (EditText) root.findViewById(R.id.tiettAboutEnviarNombre);

        //Listener del boton que lleva al Twitter del Desarrollador
        btAboutTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/vintajeskull98"));
                startActivity(browserIntent);
            }
        });

        //Listener del boton que envia el email escrito para el Desarrollador
        btAboutEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!datosCompletos()){
                    Snackbar.make(view, "Por favor, rellena los datos", Snackbar.LENGTH_LONG).show();
                }else{
                    enviarEmail(view);
                }
            }
        });

        ivMyPhoto = (ImageView) root.findViewById(R.id.ivMyPhoto);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Picasso.get()
                .load("https://media.licdn.com/dms/image/C4E03AQHXjT_UNy2vWg/profile-displayphoto-shrink_200_200/0?e=1577923200&v=beta&t=VhnmST4EvORwKRkzDf51zCw7vghHaE0372-twjbQTSM")
                .fit().centerCrop().transform(new com.emiliorgvintaje.myapps.ui.rssfolder.CircleTransform()).into(ivMyPhoto);

    }


    /**
     * Método para enviar un email
     */
    private void enviarEmail(View v){
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:vintajeskull98@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, tietAboutAsunto.getText().toString());
            intent.putExtra(android.content.Intent.EXTRA_TEXT, tietAboutMensaje.getText().toString());
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Snackbar.make(v, "Necesitas una app de email", Snackbar.LENGTH_LONG)
                        .show();
            }
        }catch(Exception ex){
            Snackbar.make(v, "Error inesperado: Contacte con el desarrollador", Snackbar.LENGTH_LONG)
                    .show();
        }


    }

    /**
     * Comprobacion del contenido de los datos
     * @return boolean
     */
    protected boolean datosCompletos(){

        if(tietAboutNombre.getText().toString().isEmpty() || tietAboutMensaje.getText().toString().isEmpty() || tietAboutAsunto.getText().toString().isEmpty()){
            return false;
        }

        return true;
    }
}