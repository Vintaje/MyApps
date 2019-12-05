package com.emiliorgvintaje.myapps.ui.rssfolder.noticiaDetalle;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.rssfolder.Noticia;

public class NoticiaDetalleFragment extends Fragment {

    public Noticia noticia;
    public static Noticia noticiatweet;
    private TextView tvNoticiaDetalles;
    private WebView wvNoticiaDetalles;

    public NoticiaDetalleFragment(){

    }

    public NoticiaDetalleFragment(Noticia noticia){

        this.noticia = noticia;
        noticiatweet = noticia;
    }

    public static NoticiaDetalleFragment newInstance(Noticia noticia) {

        return new NoticiaDetalleFragment(noticia);

    }

    public Noticia getNoticia() {
        return noticia;
    }

    public void setNoticia(Noticia noticia) {
        this.noticia = noticia;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).showShare();


        //Ocultamos la barra lateral del navigation
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((MainActivity)getActivity()).setDrawerLocked(true);

        return inflater.inflate(R.layout.noticia_detalle_fragment, container, false);

    }

    /**
     * onActivityCreated
     *
     * Creamos un String de cabecera en la que establecemos un css para el
     * webview junto con el contenido y el cierre del mismo documento
     * Instanciamos los objetos del layout con los campos de nuestra Noticia y lo mostramos
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).showFloatingActionButton();

        // TODO: Use the ViewModel
        String head = "<head><style>body {font-family: 'Gibson,sans-serif'; text-align: justify} " +
                "img{width: 100%; border-radius: 10px;} " +
                "a{ text-decoration: none;" +
                "   color: black;}" +
                ".logo{margin-top:10px;} .test{\n" +
                "    margin-top: 25px;\n" +
                "    font-size: 16px;\n" +
                "    text-align: justify;\n" +
                "\n" +
                "    -webkit-animation: fadein 2s; /* Safari, Chrome and Opera > 12.1 */\n" +
                "       -moz-animation: fadein 2s; /* Firefox < 16 */\n" +
                "        -ms-animation: fadein 2s; /* Internet Explorer */\n" +
                "         -o-animation: fadein 2s; /* Opera < 12.1 */\n" +
                "            animation: fadein 2s;\n" +
                "}\n" +
                "\n" +
                "@keyframes fadein {\n" +
                "    from { opacity: 0; }\n" +
                "    to   { opacity: 1; }\n" +
                "}" +
                "@-webkit-keyframes fadein {\n" +
                "    from { opacity: 0; }\n" +
                "    to   { opacity: 1; }\n" +
                "}</style></head>";
        String end = "<img class=\"logo\" src=\"https://d2skuhm0vrry40.cloudfront.net/2018/eg12/EG-Logo-es.png/EG11/resize/-1x92/format/png/logo.png?v20191023163704\" alt=\"eurogamerlogo\" /></body></html>";
        String cuerpo = "<div class=\"test\">"+noticia.getDesc().substring(0, noticia.getDesc().indexOf("</p><p><a "))+"</div>"+"<div class=\"fecha\">Publicado: "+noticia.getFecha()+"</div>";


        tvNoticiaDetalles = (TextView) getView().findViewById(R.id.tvTituloDetalles);
        wvNoticiaDetalles = (WebView) getView().findViewById(R.id.wvDescripcionDetalles);

        tvNoticiaDetalles.setText(noticia.getTitulo());
        wvNoticiaDetalles.loadData(head+cuerpo+end, "text/html",null);

    }



}
