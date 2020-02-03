![banner](https://github.com/Vintaje/MyApps/blob/master/bannermyapps.png)

Desarrollo de la APP MyAPPs para Android

## Descripcion

Proyecto que incluye multiples soluciones como CRUD, Multimedia, tratamiento de datos de internet y servicios del sistema 

## Tabla de Contenido

# 1. Dependencias
## 2. Lector RSS (eurogamer.es)
### 3. CRUD Agenda de Juegos
#### 4. Reproductor de Musica
##### 5. Reproductor de Video
###### 6. Test de Sensores



# 1. Dependencias

```
    implementation 'com.google.code.gson:gson:2.3.1'
    implementation 'com.karumi:dexter:6.0.0'
    implementation 'com.chibde:audiovisualizer:2.2.0'
    implementation 'com.gauravk.audiovisualizer:audiovisualizer:0.9.2'
    implementation 'com.github.PhilJay:MPAndroidChart:v3.0.2'
    implementation 'com.pierfrancescosoffritti.androidyoutubeplayer:chromecast-sender:0.23'
    implementation 'com.squareup.picasso:picasso:2.71828'
```

## 2. Lector RSS

Extraemos, tratamos y mostramos en un RecyclerView informacion a traves del RSS en un fichero XML de Eurogamer.es
```
RSS Source - https://eurogamer.es
```


### 3. CRUD Agenda Juegos

Creacion de una agenda en la cual almacenamos informacion e imagenes en b64 dentro de una BBDD
```
BBDD - SQLite
```


#### 4. Reproductor de Musica

Reproductor de Audios encontrados en el propio dispositivo, para reproducir se usa un servicio creado el cual lee un fichero XML creada por la APP donde se guarda una lista de reproduccion y el index de la cancion actual

Estructura del XML
```
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <int name="audioIndex" value="-1" />
    <string name="audioArrayList"></string>
</map>
```

##### 5. Reproductor de Video

Reproductor sencillo el cual recoge y reproduce en un VideoView dentro de un Fragment, videos encontrados en el dispositivos
```
Tambien, gracias a la dependencia de Pier Francesco, reproducimos videos de Youtube insertando la URL
```


###### 6. Test de Sensores

Fragment con varias animaciones personales para comprobar los valores de los sensores, estas son por ejemplo, una vista de avion, mover un objeto con el acelerometro, brujula para orientacion o un grafico para comprobar el Flujo Magnetico



