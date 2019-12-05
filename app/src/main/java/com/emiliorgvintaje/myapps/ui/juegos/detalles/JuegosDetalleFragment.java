package com.emiliorgvintaje.myapps.ui.juegos.detalles;import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emiliorgvintaje.myapps.DBC;
import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.juegos.Juego;
import com.emiliorgvintaje.myapps.util.MyB64;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class JuegosDetalleFragment extends Fragment {


    private static boolean editable;
    public static boolean nuevo;
    private Juego juego;
    private View root;

    private EditText titulo, sinopsis, lanzamiento, precio;
    private ImageView imagen;
    private Spinner spPlataformaJuegos;
    public static Juego juegoguardado;


    // Constantes
    private static final int GALERIA = 1;
    private static final int CAMARA = 2;


    // Directorio para salvar las cosas
    private static final String IMAGE_DIRECTORY = "/myapps";
    Uri photoURI;

    private FloatingActionButton fabAgregarFoto;

    public JuegosDetalleFragment(Juego juego, boolean editable, boolean nuevo) {

        this.juego = juego;
        this.editable = editable;
        this.nuevo = nuevo;
        ((MainActivity) getActivity()).editando = false;
        juegoguardado = new Juego();

    }

    public JuegosDetalleFragment(boolean nuevo) {

        this.juego = new Juego("", "", "", "", 0.00F, "");
        this.editable = true;
        this.nuevo = nuevo;
        ((MainActivity) getActivity()).nuevo = true;
        juegoguardado = new Juego();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.juegos_detalle_fragment, container, false);


        try {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.mis_juegos_title) + " - " + juego.getNombre().substring(0, 12));
        } catch (Exception ex) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.mis_juegos_title) + " - Nuevo Juego");

        }
        return root;


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel

        titulo = (EditText) root.findViewById(R.id.etTituloJuego);
        sinopsis = (EditText) root.findViewById(R.id.etSinopsisJuego);
        lanzamiento = (EditText) root.findViewById(R.id.etReleaseDate);
        precio = (EditText) root.findViewById(R.id.etPrecioJuego);
        imagen = (ImageView) root.findViewById(R.id.ivJuegoDetalle);
        spPlataformaJuegos = (Spinner) root.findViewById(R.id.spPlataformaJuego);
        fabAgregarFoto = (FloatingActionButton) root.findViewById(R.id.fabAgregarFoto);

        rellenarSpinner();
        this.fabAgregarFoto.show();

        //Instanciamos el listener del spinner
        spPlataformaJuegos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Here you change your value or do whatever you want

                switch (spPlataformaJuegos.getSelectedItem().toString()) {
                    case "PS4":
                        juegoguardado.setPlataforma("PS4");
                        break;
                    case "XBOXONE":
                        juegoguardado.setPlataforma("XBOXONE");
                        break;
                    case "XBOX360":
                        juegoguardado.setPlataforma("XBOX360");
                        break;
                    case "3DS2DS":
                        juegoguardado.setPlataforma("3DS2DS");
                        break;
                    case "NSWITCH":
                        juegoguardado.setPlataforma("NSWITCH");
                        break;
                    case "PCFISICO":
                        juegoguardado.setPlataforma("PCFISICO");
                        break;
                    case "PCDIGITAL":
                        juegoguardado.setPlataforma("PCDIGITAL");
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Seteamos los colores a usar
        seleccionarItemSP();
        this.titulo.setTextColor(getResources().getColor(R.color.whitebox));
        this.precio.setTextColor(getResources().getColor(R.color.whitebox));
        this.lanzamiento.setTextColor(getResources().getColor(R.color.whitebox));

        modoVisualizacion();

        //Ocultamos la barra lateral del navigation
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((MainActivity) getActivity()).setDrawerLocked(true);
        if (editable) {
            ((MainActivity) getActivity()).editando = true;
        } else {
            ((MainActivity) getActivity()).editando = false;
        }
        editTextListeners();

        //Listener de la fecha de lanzamiento para desplegar el calendario
        lanzamiento.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(root.getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Listener del FAB para agregar una foto, despliega dicho dialogo
        fabAgregarFoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mostrarDialogoFoto();

            }
        });


    }


    /**
     * Dialogo de eleccion de App para el recurso de la imagen, muestra opciones de Galeria o Camara
     *
     */
    private void mostrarDialogoFoto() {
        AlertDialog.Builder fotoDialogo = new AlertDialog.Builder(root.getContext());
        fotoDialogo.setTitle("Seleccionar Acción");
        String[] fotoDialogoItems = {
                "Seleccionar fotografía de galería",
                "Capturar fotografía desde la cámara"};
        fotoDialogo.setItems(fotoDialogoItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                elegirFotoGaleria();
                                break;
                            case 1:
                                tomarFotoCamara();
                                break;
                        }
                    }
                });
        fotoDialogo.show();
    }

    /**
     * Instanciamos e iniciamos el Intent de la Galeria
     *
     */
    public void elegirFotoGaleria() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALERIA);
    }


    /**
     * Instanciamos e iniciamos el Intent de la Camara
     *
     */
    private void tomarFotoCamara() {
        // Si queremos hacer uso de fotos en aklta calidad
        try {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            // Eso para alta o baja
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            // Esto para alta calidad
            photoURI = Uri.fromFile(this.crearFichero());
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);

            // Esto para alta y baja
            startActivityForResult(intent, CAMARA);
        } catch (Exception ex) {
            Toast.makeText(getContext(), "Error: por favor compruebe los permisos", Toast.LENGTH_SHORT).show();


        }
    }


    /**
     * Resultado del Intent de Galeria o Camara
     *
     * @param requestCode Codigo de respuesta
     * @param resultCode Codigo de resultado
     * @param data Intent realizado
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("FOTO", "Opción::--->" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_CANCELED) {
            return;
        }

        String b64;
        if (requestCode == GALERIA) {
            Log.d("FOTO", "Entramos en Galería");
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                Uri contentURI = data.getData();
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentURI);


                    b64 = salvarImagen(bitmap);
                    this.imagen.setImageBitmap(MyB64.base64ToBitmap(b64));
                    this.juego.setImagen(b64);

                    // Guardamos el b64 en el objeto estatico
                    juegoguardado.setImagen(b64);


                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(root.getContext(), "¡Fallo Galeria!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMARA) {
            Log.d("FOTO", "Entramos en Camara");
            // Cogemos la imagen, pero podemos coger la imagen o su modo en baja calidad (thumbnail
            Bitmap thumbnail = null;
            try {
                // Esta línea para baja
                //thumbnail = (Bitmap) data.getExtras().get("data");
                // Esto para alta
                thumbnail = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoURI);
                Matrix matrix = new Matrix();


                ExifInterface ei = new ExifInterface(photoURI.getPath());
                borrarFichero(photoURI.getPath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap thumb;

                //Giramos la imagen en funcion de como se haya hecho la foto para que al usuario le aparezca derecha
                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        thumb = thumbnail.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(),
                                matrix, true);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        thumb = thumbnail.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(),
                                matrix, true);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        thumb = thumbnail.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(),
                                matrix, true);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        thumb = thumbnail;
                }

                // recogemos la imagen en B64
                b64 = salvarImagen(thumb);

                //Seteamos el ImageView con la Conversion de la Imagen en B64 a Bitmap
                this.imagen.setImageBitmap(MyB64.base64ToBitmap(b64));


                // Borramos el fichero de la URI
                borrarFichero(photoURI.getPath());

                //Seteamos nuestro B64 en el objeto estatico para guardar/actualizar en la base de datos desde el Main
                juegoguardado.setImagen(b64);
                Toast.makeText(root.getContext(), "¡Foto Guardada!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(root.getContext(), "¡Fallo de la Camara!", Toast.LENGTH_SHORT).show();
            }


        }


    }


    /**
     * Borramos el fichero temporal creado
     * @param path Ruta del archivo
     */
    private void borrarFichero(String path) {
        // Borramos la foto de alta calidad
        File fdelete = new File(path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("FOTO", "Foto borrada::--->" + path);
            } else {
                Log.d("FOTO", "Foto NO borrada::--->" + path);
            }
        }
    }


    /**
     * Creamos un fichero en una ruta con un nombre fijado anteriormente, este nombre
     * son la hora actual pasada a milisegundos
     *
     * @return Fichero guardado
     */
    public File crearFichero() {
        // Nombre del fichero
        String nombre = crearNombreFichero();
        return salvarFicheroPublico(nombre);
    }

    /**
     * Para la creacion de los ficheros, creamos el nombre con este metodo usando la fecha
     * convertida a milisegundos
     *
     * @return Nombre del fichero
     */
    private String crearNombreFichero() {
        return Calendar.getInstance().getTimeInMillis() + ".jpg";
    }

    private File salvarFicheroPublico(String nombre) {
        // Vamos a obtener los datos de almacenamiento externo
        File dirFotos = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // Si no existe el directorio, lo creamos solo si es publico
        if (!dirFotos.exists()) {
            dirFotos.mkdirs();
        }

        // Vamos a crear el fichero con la fecha
        try {
            File f = new File(dirFotos, nombre);
            f.createNewFile();
            return f;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    // Función para salvar una imagem
    public String salvarImagen(Bitmap myBitmap) {
        // Comprimimos la imagen
        float porcentaje = 480 / (float) myBitmap.getWidth();

        Bitmap bitmap = Bitmap.createScaledBitmap(myBitmap, 480, (int) (myBitmap.getHeight() * porcentaje), false);
        byte[] byteArray = comprimirImagen(bitmap).toByteArray();
        Bitmap compress = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        String res = MyB64.bitmapToBase64(compress);

        this.juego.setImagen(res);

        return res;
    }


    /**
     * Comprimimos la calidad de la imagen
     *
     * @param myBitmap bitmap a comprimir
     * @return array de Bytes
     */
    private ByteArrayOutputStream comprimirImagen(Bitmap myBitmap) {
        // Stream de binario
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Seleccionamos la calidad y la trasformamos y comprimimos
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
        return bytes;
    }


    /**
     * Listener de los campos editables en caso de que queramos editar o crear uno nuevo
     * Estos controlan, cada vez que se introduce un caracter, el estado del texto
     * para indicar al usuario si tiene que realizar alguna accion
     *
     */
    public void editTextListeners() {


        titulo.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!titulo.getText().toString().isEmpty()) {

                    juegoguardado.setNombre(titulo.getText().toString());
                } else {
                    Toast.makeText(root.getContext(), "Debes introducir un titulo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        precio.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String digs = precio.getText().toString().replace("€", "\n").trim();
                if (!digs.isEmpty()) {
                    String formatted = String.valueOf(Double.parseDouble(digs));

                    juegoguardado.setPrecio(Float.parseFloat(formatted));
                } else {
                    Toast.makeText(root.getContext(), "Debes introducir un precio", Toast.LENGTH_SHORT).show();
                }
            }
        });
        lanzamiento.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!lanzamiento.getText().toString().isEmpty()) {

                    juegoguardado.setFecha_lanzamiento(lanzamiento.getText().toString());
                } else {
                    Toast.makeText(root.getContext(), "Debes introducir una fecha", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        sinopsis.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    //Seteamos la sinopsis cada vez que se edite
                    juegoguardado.setDescripcion(sinopsis.getText().toString());
                } catch (Exception ex) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * En base a los parametros introducidos en el constructor al instanciar el Fragment, mostraremos
     * los EditText habilitados o deshabilitado en funcion de la accion que haya realizado el usuario
     *
     * boolean editable: True-Los campos se habilitan para poder realizar la edicion de los mismos
     * boolean nuevo: True-Se vacian todos los campos en funcion de si se quiere crear uno nuevo, sino se cargan los datos del actual
     *
     * Tambien cargamos los nombres respectivos en la Toolbar
     *
     */
    public void modoVisualizacion() {
        if (!editable) {
            this.titulo.setEnabled(false);
            this.sinopsis.setEnabled(false);
            this.lanzamiento.setEnabled(false);
            this.precio.setEnabled(false);
            this.spPlataformaJuegos.setEnabled(false);
            ((MainActivity) getActivity()).editando = false;
            ((MainActivity) getActivity()).hideSave();
            this.fabAgregarFoto.hide();
        } else if (editable && !nuevo) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Mis Juegos - Editar Juego");

            System.out.println(juego.getImagen());
        }
        if (!nuevo) {

            this.sinopsis.setTextColor(getResources().getColor(android.R.color.black));


            this.titulo.setText(juego.getNombre());
            this.sinopsis.setText(juego.getDescripcion());
            this.lanzamiento.setText(juego.getFecha_lanzamiento());
            this.precio.setText(String.format("%s", Float.toString(juego.getPrecio())));
            this.imagen.setImageBitmap(MyB64.base64ToBitmap(this.juego.getImagen()));

            juegoguardado = juego;


        } else {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Mis Juegos - Nuevo Juego");
        }


        if (!editable && !nuevo) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Mis Juegos - Visualizar");
            ((MainActivity) getActivity()).showBack();
        }

    }


    /**
     * Switch del Spinner para seleccionar la plataforma y setearla en el item estatico
     *
     */
    private void seleccionarItemSP() {

        switch (juego.getPlataforma()) {
            case "PS4":
                spPlataformaJuegos.setSelection(0);
                break;
            case "XBOXONE":
                juegoguardado.setPlataforma("XBOXONE");
                spPlataformaJuegos.setSelection(1);
                break;
            case "XBOX360":
                juegoguardado.setPlataforma("XBOX360");
                spPlataformaJuegos.setSelection(2);
                break;
            case "3DS2DS":
                juegoguardado.setPlataforma("3DS2DS");
                spPlataformaJuegos.setSelection(3);
                break;
            case "NSWITCH":
                juegoguardado.setPlataforma("NSWITCH");
                spPlataformaJuegos.setSelection(4);
                break;
            case "PCFISICO":
                juegoguardado.setPlataforma("PCFISICO");
                spPlataformaJuegos.setSelection(5);
                break;
            case "PCDIGITAL":
                juegoguardado.setPlataforma("PCDIGITAL");
                spPlataformaJuegos.setSelection(6);
                break;
        }

    }

    /**
     *
     * Rellenamos el Spinner de Plataformas con las opciones disponibles almacenadas por defecto en la base
     * de datos al iniciar la App por primera vez
     *
     */
    public void rellenarSpinner() {

        DBC bdEjemplo = new DBC(this.getContext(), "BDJuegos", null, 1);
        SQLiteDatabase bd = bdEjemplo.getReadableDatabase();

        ArrayList<String> categorias = new ArrayList<>();
        //Si hemos abierto correctamente la base de datos
        if (bd != null) {
            //Seleccionamos todos
            Cursor c = bd.rawQuery(" SELECT nombre FROM Plataforma", null);
            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                //Recorremos el cursor hasta que no haya más registros
                do {

                    categorias.add(c.getString(0));
                } while (c.moveToNext());
            }
            //Cerramos la base de datos
            bd.close();

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(root.getContext(), R.layout.plataforma_item, categorias);
            dataAdapter.setDropDownViewResource(R.layout.plataforma_item);
            spPlataformaJuegos.setAdapter(dataAdapter);

        }
    }

    //Creamos nuestro Calendario y DatePickerDialog para la fecha de lanzamiento
    final Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {


        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        lanzamiento.setText(sdf.format(myCalendar.getTime()));
    }


}
