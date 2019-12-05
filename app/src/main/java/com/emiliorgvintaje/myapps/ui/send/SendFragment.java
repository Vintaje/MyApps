package com.emiliorgvintaje.myapps.ui.send;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.google.android.material.snackbar.Snackbar;

public class SendFragment extends Fragment {


    private Button btEnviar;
    private EditText tietContactoEmail;
    private EditText tietContactoNombre;
    private Switch swMainContactar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_send, container, false);
        ((MainActivity) getActivity()).hideFloatingActionButton();

        //Instancia de los items
        btEnviar = (Button) root.findViewById(R.id.btEnviar);
        tietContactoEmail = (EditText) root.findViewById(R.id.tietContactoEmail);
        tietContactoNombre = (EditText) root.findViewById(R.id.tietContactoNombre);
        swMainContactar = (Switch) root.findViewById(R.id.swMainContactar);

        btEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Comprobamos el estado de los datos y del Switch, si esta correcto, enviamos el email
                if(!datosCompletos()){
                    Snackbar.make(view, "Por favor, rellena los datos", Snackbar.LENGTH_LONG).show();
                }else if(!SwIsChecked()){
                    Snackbar.make(view, "Contactar desactivado, no se enviara nada", Snackbar.LENGTH_LONG).show();
                }else{
                    enviarEmail(view);
                }
            }
        });

        return root;
    }

    /**
     * Comprobamos si el switch está marcado
     *
     * @return boolean
     */
    protected boolean SwIsChecked(){

        if(swMainContactar.isChecked()){
            return true;
        }

        return false;
    }

    /**
     * Método para enviar un email
     */
    private void enviarEmail(View v){
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"+tietContactoEmail.getText().toString()));
            //intent.putExtra(Intent.EXTRA_EMAIL, this.email);
            intent.putExtra(Intent.EXTRA_SUBJECT, "MyAPPs!!");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "Estoy usando una App con multiples soluciones desarrollada por https://twitter.com/vintajeskull98!! " +
                    "\nLa puedes encontrar por el nombre de MyApps");
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
     * Comprobamos el contenido de los datos
     * @return boolean
     */
    protected boolean datosCompletos(){

        if(tietContactoEmail.getText().toString().isEmpty() || tietContactoNombre.getText().toString().isEmpty()){
            return false;
        }

        return true;
    }
}