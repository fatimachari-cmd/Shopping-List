package com.fatimachari.listadecompra.ui.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatimachari.listadecompra.R
import com.fatimachari.listadecompra.auth.AuthRepository
import com.fatimachari.listadecompra.data.ShoppingRepository
import com.fatimachari.listadecompra.models.Invitacion
import com.fatimachari.listadecompra.models.ListaCompra
import com.fatimachari.listadecompra.ui.theme.*
import com.fatimachari.listadecompra.util.base64ABitmap
import com.fatimachari.listadecompra.util.uriABase64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MyListScreen(
    onListClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository: ShoppingRepository = remember { ShoppingRepository() }

    // USUARIO CONECTADO
    val usuario: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val miId: String = usuario?.uid ?: ""
    val miCorreo: String = usuario?.email ?: ""

    // Nombre: el que escribió al registrarse (o Google). Si no hay, usamos el correo
    val nombreGuardado: String = usuario?.displayName ?: ""
    var miNombre: String by remember {
        mutableStateOf(if (nombreGuardado.isBlank()) miCorreo.substringBefore("@") else nombreGuardado)
    }

    // Foto de perfil (la guardamos en Firestore como texto)
    var foto: Bitmap? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    // Elegir una foto de la galería
    val elegirFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val texto = uriABase64(context, uri)
            if (texto != null) {
                repository.saveUserPhoto(miId, texto)
            } else {
                Toast.makeText(context, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Datos de la pantalla
    var listas: List<ListaCompra> by remember { mutableStateOf(listOf()) }
    var invitaciones: List<Invitacion> by remember { mutableStateOf(listOf()) }
    val numProductos: MutableMap<String, Int> = remember { mutableStateMapOf() }
    var pestana: String by remember { mutableStateOf("Listas") }

    // Ventanas abiertas o cerradas
    var mostrarCrear: Boolean by remember { mutableStateOf(false) }
    var listaAEditar: ListaCompra? by remember { mutableStateOf(null) }
    var listaAEliminar: ListaCompra? by remember { mutableStateOf(null) }
    var listaAInvitar: ListaCompra? by remember { mutableStateOf(null) }
    var cambiarNombre: Boolean by remember { mutableStateOf(false) }

    // Mis listas
    DisposableEffect(miId) {
        val escucha = repository.listenLists(miId) { datos ->
            val nuevasListas = mutableListOf<ListaCompra>()

            for (dato in datos) {
                val lista = ListaCompra(
                    id = dato["id"].toString(),
                    nombre = dato["title"].toString(),
                    esFavorita = (dato["favoritedBy"] as? List<*>)?.contains(miId) == true,
                    esMia = dato["ownerId"] == miId
                )
                nuevasListas.add(lista)
            }

            listas = nuevasListas
        }
        onDispose { escucha.remove() }
    }

    // Invitaciones que me han enviado
    DisposableEffect(miCorreo) {
        val escucha = repository.listenInvitations(miCorreo) { datos ->
            val nuevas = mutableListOf<Invitacion>()

            for (dato in datos) {
                val invitacion = Invitacion(
                    id = dato["id"].toString(),
                    listId = dato["listId"].toString(),
                    nombreLista = dato["listName"].toString(),
                    deQuien = dato["fromName"].toString()
                )
                nuevas.add(invitacion)
            }

            invitaciones = nuevas
        }
        onDispose { escucha.remove() }
    }

    // Mi foto de perfil
    DisposableEffect(miId) {
        val escucha = repository.listenUserPhoto(miId) { texto ->
            foto = base64ABitmap(texto)
        }
        onDispose { escucha.remove() }
    }

    // Número de productos de cada lista
    DisposableEffect(listas.map { it.id }) {
        val escuchas = mutableListOf<ListenerRegistration>()

        for (lista in listas) {
            val escucha = repository.listenProducts(lista.id) { productos ->
                numProductos[lista.id] = productos.size
            }
            escuchas.add(escucha)
        }

        onDispose {
            for (escucha in escuchas) {
                escucha.remove()
            }
        }
    }

    // Listas que se enseñan según la pestaña
    val listasQueSeVen = mutableListOf<ListaCompra>()
    for (lista in listas) {
        if (pestana == "Listas" || lista.esFavorita) {
            listasQueSeVen.add(lista)
        }
    }

    // PANTALLA
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {

        // Cabecera
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(stringResource(R.string.app_name), color = Principal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val titulo = when (pestana) {
                "Favoritos" -> "Mis favoritos"
                "Perfil" -> "Mi perfil"
                else -> "Mis listas"
            }
            Text(titulo, color = Texto, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        // Contenido

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp)
        ) {
            if (pestana == "Perfil") {

                // Pestaña Perfil
                PantallaPerfil(
                    nombre = miNombre,
                    correo = miCorreo,
                    foto = foto,
                    onCambiarFoto = { elegirFoto.launch("image/*") },
                    onCambiarNombre = { cambiarNombre = true },
                    numListas = listas.size,
                    numCompartidas = listas.count { !it.esMia },
                    onLogout = onLogout
                )

            } else {

                // Invitaciones pendientes (solo en la pestaña Listas)

                if (pestana == "Listas") {
                    for (invitacion in invitaciones) {
                        TarjetaInvitacion(
                            invitacion = invitacion,
                            onAceptar = { repository.acceptInvitation(invitacion.id, invitacion.listId, miId) },
                            onRechazar = { repository.rejectInvitation(invitacion.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (listasQueSeVen.isEmpty()) {

                    // Mensaje cuando no hay listas
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.List, null, tint = Principal, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (pestana == "Favoritos") {
                            Text("Aún no tienes favoritas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Texto)
                            Text("Márcalas desde el menú ⋮ de cada lista", color = Gris)
                        } else {
                            Text("Todavía no hay listas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Texto)
                            Text("Crea una con el botón de abajo", color = Gris)
                        }
                    }

                } else {

                    // Tarjetas de las listas
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listasQueSeVen) { lista ->
                            TarjetaLista(
                                lista = lista,
                                productos = numProductos[lista.id] ?: 0,
                                onAbrir = { onListClick(lista.id) },
                                onFavorito = { repository.updateListFavorite(lista.id, miId, !lista.esFavorita) },
                                onEditar = { listaAEditar = lista },
                                onInvitar = { listaAInvitar = lista },
                                onEliminar = { listaAEliminar = lista }
                            )
                        }
                    }
                }

                // Botón crear lista
                if (pestana == "Listas") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { mostrarCrear = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear nueva lista", fontSize = 16.sp)
                    }
                }
            }
        }

        // Pestañas de abajo:

        NavigationBar(containerColor = Blanco) {
            NavigationBarItem(
                selected = pestana == "Listas",
                onClick = { pestana = "Listas" },
                icon = { Icon(Icons.Filled.List, null) },
                label = { Text("Listas") }
            )
            NavigationBarItem(
                selected = pestana == "Favoritos",
                onClick = { pestana = "Favoritos" },
                icon = { Icon(Icons.Filled.Favorite, null) },
                label = { Text("Favoritos") }
            )
            NavigationBarItem(
                selected = pestana == "Perfil",
                onClick = { pestana = "Perfil" },
                icon = { Icon(Icons.Filled.Person, null) },
                label = { Text("Perfil") }
            )
        }
    }

    // Crear lista
    if (mostrarCrear) {
        VentanaTexto(
            titulo = "Nueva lista",
            etiqueta = "Nombre de la lista",
            textoInicial = "",
            boton = "Crear",
            onAceptar = { nombre ->
                repository.createList(UUID.randomUUID().toString(), nombre, miId) { }
                mostrarCrear = false
            },
            onCancelar = { mostrarCrear = false }
        )
    }

    // Cambiar nombre
    if (listaAEditar != null) {
        VentanaTexto(
            titulo = "Cambiar nombre",
            etiqueta = "Nombre de la lista",
            textoInicial = listaAEditar!!.nombre,
            boton = "Guardar",
            onAceptar = { nombre ->
                repository.updateListName(listaAEditar!!.id, nombre)
                listaAEditar = null
            },
            onCancelar = { listaAEditar = null }
        )
    }

    // Cambiar mi nombre (perfil)
    if (cambiarNombre) {
        VentanaTexto(
            titulo = "Tu nombre",
            etiqueta = "Nombre",
            textoInicial = miNombre,
            boton = "Guardar",
            onAceptar = { nombre ->
                scope.launch {
                    authRepository.updateName(nombre).onSuccess {
                        miNombre = nombre
                    }.onFailure {
                        Toast.makeText(context, "No se pudo cambiar el nombre", Toast.LENGTH_SHORT).show()
                    }
                }
                cambiarNombre = false
            },
            onCancelar = { cambiarNombre = false }
        )
    }

    // Invitar a alguien por correo
    if (listaAInvitar != null) {
        val lista = listaAInvitar!!
        var error by remember { mutableStateOf("") }

        VentanaTexto(
            titulo = "Invitar a \"${lista.nombre}\"",
            etiqueta = "Correo de la otra persona",
            textoInicial = "",
            boton = "Enviar",
            error = error,
            onAceptar = { correo ->
                if (!correo.contains("@")) {
                    error = "Escribe un correo válido"
                } else if (correo.equals(miCorreo, ignoreCase = true)) {
                    error = "Ese es tu propio correo"
                } else {
                    repository.sendInvitation(lista.id, lista.nombre, miNombre, correo) { correcto ->
                        val mensaje = if (correcto) "Invitación enviada" else "No se pudo enviar"
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                    }
                    listaAInvitar = null
                }
            },
            onCancelar = { listaAInvitar = null }
        )
    }

    // Eliminar lista (o salir si es de otra persona)
    if (listaAEliminar != null) {
        val lista = listaAEliminar!!
        AlertDialog(
            onDismissRequest = { listaAEliminar = null },
            title = { Text(if (lista.esMia) "Eliminar lista" else "Salir de la lista") },
            text = {
                if (lista.esMia) {
                    Text("Se borrará \"${lista.nombre}\" para todos. ¿Seguro?")
                } else {
                    Text("Ya no verás \"${lista.nombre}\". ¿Seguro?")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (lista.esMia) {
                        repository.deleteList(lista.id)
                    } else {
                        repository.leaveList(lista.id, miId)
                    }
                    listaAEliminar = null
                }) {
                    Text(if (lista.esMia) "Eliminar" else "Salir", color = Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { listaAEliminar = null }) { Text("Cancelar", color = Gris) }
            }
        )
    }
}

//  Tarjeta de una lista
@Composable
fun TarjetaLista(
    lista: ListaCompra,
    productos: Int,
    onAbrir: () -> Unit,
    onFavorito: () -> Unit,
    onEditar: () -> Unit,
    onInvitar: () -> Unit,
    onEliminar: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrir() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icono de lista
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrincipalClaro, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.List, null, tint = Principal)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Nombre y número de productos
            Column(modifier = Modifier.weight(1f)) {
                Text(lista.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Texto)
                if (lista.esMia) {
                    Text("$productos productos", fontSize = 13.sp, color = Gris)
                } else {
                    Text("$productos productos · Compartida", fontSize = 13.sp, color = Principal)
                }
            }

            // Menú de los 3 puntos
            Box {
                IconButton(onClick = { menuAbierto = true }) {
                    Icon(Icons.Filled.MoreVert, "Opciones", tint = Gris)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {

                    // Favorito
                    DropdownMenuItem(
                        text = { Text(if (lista.esFavorita) "Quitar de favoritos" else "Añadir a favoritos") },
                        leadingIcon = {
                            Icon(
                                if (lista.esFavorita) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                null,
                                tint = if (lista.esFavorita) Rojo else Gris
                            )
                        },
                        onClick = { menuAbierto = false; onFavorito() }
                    )

                    // Cambiar nombre
                    DropdownMenuItem(
                        text = { Text("Cambiar nombre") },
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        onClick = { menuAbierto = false; onEditar() }
                    )

                    // Invitar (solo si la lista es mía)
                    if (lista.esMia) {
                        DropdownMenuItem(
                            text = { Text("Invitar") },
                            leadingIcon = { Icon(Icons.Filled.PersonAdd, null) },
                            onClick = { menuAbierto = false; onInvitar() }
                        )
                    }

                    // Eliminar o salir
                    DropdownMenuItem(
                        text = { Text(if (lista.esMia) "Eliminar" else "Salir de la lista", color = Rojo) },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Rojo) },
                        onClick = { menuAbierto = false; onEliminar() }
                    )
                }
            }
        }
    }
}

// Tarjeta de invitacion
@Composable
fun TarjetaInvitacion(
    invitacion: Invitacion,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrincipalClaro),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${invitacion.deQuien} te invita a unirte a", fontSize = 13.sp, color = Texto)
            Text(invitacion.nombreLista, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Texto)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRechazar) { Text("Rechazar", color = Texto) }
                Button(onClick = onAceptar, shape = RoundedCornerShape(12.dp)) { Text("Aceptar") }
            }
        }
    }
}

// La ventana del perfil
@Composable
fun PantallaPerfil(
    nombre: String,
    correo: String,
    foto: Bitmap?,
    onCambiarFoto: () -> Unit,
    onCambiarNombre: () -> Unit,
    numListas: Int,
    numCompartidas: Int,
    onLogout: () -> Unit
) {
    // Si no hay foto, se ve la primera letra del nombre
    val inicial = if (nombre.isNotEmpty()) nombre.substring(0, 1).uppercase() else "?"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Foto (se pulsa para cambiarla)
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(PrincipalClaro)
                    .clickable { onCambiarFoto() },
                contentAlignment = Alignment.Center
            ) {
                if (foto != null) {
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(inicial, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Principal)
                }
            }

            // Icono de cámara
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Principal, CircleShape)
                    .clickable { onCambiarFoto() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CameraAlt, "Cambiar foto", tint = Blanco, modifier = Modifier.size(16.dp))
            }
        }

        // Nombre (con lápiz para editarlo) y correo
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Texto)
            IconButton(onClick = onCambiarNombre) {
                Icon(Icons.Filled.Edit, "Cambiar nombre", tint = Gris, modifier = Modifier.size(18.dp))
            }
        }
        Text(correo, fontSize = 14.sp, color = Gris)

        Spacer(modifier = Modifier.height(24.dp))

        // Datos de la cuenta
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FilaDato(Icons.Filled.List, "Mis listas", "$numListas")
                Spacer(modifier = Modifier.height(14.dp))
                FilaDato(Icons.Filled.Group, "Compartidas conmigo", "$numCompartidas")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cerrar sesión
        OutlinedButton(
            onClick = onLogout,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(Icons.Filled.Logout, null, tint = Rojo)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión", color = Rojo)
        }
    }
}

// Una fila del perfil: icono + título + valor

@Composable
fun FilaDato(icono: ImageVector,titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PrincipalClaro, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Principal)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(titulo, fontSize = 12.sp, color = Gris)
            Text(valor, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Texto)
        }
    }
}

// Ventana con un campo de texto:
@Composable
fun VentanaTexto(
    titulo: String,
    etiqueta: String,
    textoInicial: String,
    boton: String,
    error: String = "",
    onAceptar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(textoInicial) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text(etiqueta) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                if (error != "") {
                    Text(error, color = Rojo, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (texto.isNotBlank()) onAceptar(texto.trim()) }) {
                Text(boton)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = Gris) }
        }
    )
}