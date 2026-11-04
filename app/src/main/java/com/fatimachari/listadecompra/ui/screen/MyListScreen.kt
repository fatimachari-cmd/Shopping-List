
package com.fatimachari.listadecompra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import com.fatimachari.listadecompra.data.ShoppingRepository

// Modelo de una lista de compra
data class ListaCompra(
    val id: String,
    val nombre: String,
    val productos: Int = 0,
    val esFavorita: Boolean = false
)

@Composable
fun MyListScreen(
    onListClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {

    // Colores de la aplicación
    val fondo = Color(0xFFF6F3EB)
    val verde = Color(0xFF587653)
    val texto = Color(0xFF30382F)
    val gris = Color(0xFF899187)
    val blanco = Color(0xFFFFFFFF)
    val rosa = Color(0xFFD8757B)

    val repository = remember { ShoppingRepository() }

    // Obtener el usuario conectado
    val ownerId = FirebaseAuth.getInstance()
        .currentUser?.uid.orEmpty()

    // Estados de las listas
    var listas by remember {
        mutableStateOf(listOf<ListaCompra>())
    }

    // Contador de productos
    val productCounts = remember {
        mutableStateMapOf<String, Int>()
    }

    // Cargar listas propias y compartidas
    DisposableEffect(ownerId) {

        val listener = if (ownerId.isNotBlank()) {
            repository.listenLists(ownerId) { datos ->

                listas = datos.mapNotNull { dato ->

                    val id = dato["id"] as? String
                        ?: return@mapNotNull null

                    ListaCompra(
                        id = id,
                        nombre = dato["title"] as? String ?: "",
                        productos = (dato["productCount"] as? Long)?.toInt() ?: 0,
                        esFavorita = dato["isFavorite"] as? Boolean ?: false
                    )
                }
            }
        } else {
            null
        }

        onDispose {
            listener?.remove()
        }
    }

    // Escuchar los productos de cada lista
    DisposableEffect(listas.map { it.id }) {

        val productListeners = listas.map { lista ->
            repository.listenProducts(lista.id) { productos ->
                productCounts[lista.id] = productos.size
            }
        }

        onDispose {
            productListeners.forEach { it.remove() }
        }
    }

    var pantalla by remember {
        mutableStateOf("Listas")
    }

    // Estados para crear listas
    var mostrarVentana by remember {
        mutableStateOf(false)
    }

    var nombreNuevo by remember {
        mutableStateOf("")
    }

    // Estados para editar listas
    var listaAEditar by remember {
        mutableStateOf<ListaCompra?>(null)
    }

    var nombreEditado by remember {
        mutableStateOf("")
    }

    // Estado para eliminar listas
    var listaAEliminar by remember {
        mutableStateOf<ListaCompra?>(null)
    }

    // Estados para compartir listas
    var listaParaCompartir by remember {
        mutableStateOf<String?>(null)
    }

    var codigoCompartir by remember {
        mutableStateOf<String?>(null)
    }

    var cargandoCodigo by remember {
        mutableStateOf(false)
    }

    // Estados para unirse a una lista
    var mostrarUnirse by remember {
        mutableStateOf(false)
    }

    var codigoIntroducido by remember {
        mutableStateOf("")
    }

    var mensajeUnirse by remember {
        mutableStateOf<String?>(null)
    }

    var cargandoUnirse by remember {
        mutableStateOf(false)
    }

    // Filtrar listas favoritas
    val listasVisibles = if (pantalla == "Favoritos") {
        listas.filter { it.esFavorita }
    } else {
        listas
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .statusBarsPadding()
    ) {

        // CABECERA
        Column(
            modifier = Modifier.padding(22.dp)
        ) {

            Text(
                text = "MiCesta",
                color = verde,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when (pantalla) {
                    "Favoritos" -> "Mis favoritos"
                    "Perfil" -> "Mi perfil"
                    else -> "Mis listas"
                },
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = texto
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = when (pantalla) {
                    "Favoritos" -> "Tus listas favoritas"
                    "Perfil" -> "Gestiona tu cuenta"
                    else -> "Organiza tus compras fácilmente"
                },
                fontSize = 14.sp,
                color = gris
            )
        }

        // CONTENIDO PRINCIPAL
        when (pantalla) {

            // PANTALLA DE PERFIL
            "Perfil" -> {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(35.dp))

                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(90.dp),
                        tint = verde
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Mi cuenta",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = texto
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = blanco
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLogout()
                                }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = Color.Red
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Cerrar sesión",
                                color = Color.Red,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = gris
                            )
                        }
                    }
                }
            }

            // PANTALLA DE LISTAS Y FAVORITOS
            else -> {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = if (pantalla == "Favoritos")
                                "Listas favoritas"
                            else "Todas mis listas",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = texto
                        )

                        Text(
                            text = "${listasVisibles.size} listas",
                            color = gris,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // ESTADO VACÍO
                    if (listasVisibles.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Icon(
                                    imageVector = if (pantalla == "Favoritos")
                                        Icons.Default.FavoriteBorder
                                    else Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = verde,
                                    modifier = Modifier.size(65.dp)
                                )

                                Spacer(modifier = Modifier.height(15.dp))

                                Text(
                                    text = if (pantalla == "Favoritos")
                                        "Todavía no tienes favoritos"
                                    else "Todavía no tienes listas",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = texto
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    text = if (pantalla == "Favoritos")
                                        "Marca tus listas como favoritas"
                                    else "Crea tu primera lista de compra",
                                    color = gris,
                                    fontSize = 14.sp
                                )
                            }
                        }

                    } else {

                        // MOSTRAR TARJETAS
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 15.dp)
                        ) {

                            items(
                                items = listasVisibles,
                                key = { it.id }
                            ) { lista ->

                                var menuAbierto by remember {
                                    mutableStateOf(false)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = blanco
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp
                                    )
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        // ICONO DE COMPRA
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBasket,
                                            contentDescription = null,
                                            tint = verde,
                                            modifier = Modifier.size(32.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // NOMBRE Y PRODUCTOS
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    onListClick(lista.id)
                                                }
                                        ) {

                                            Text(
                                                text = lista.nombre,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = texto
                                            )

                                            Text(
                                                text = "${productCounts[lista.id] ?: 0} productos",
                                                fontSize = 13.sp,
                                                color = gris
                                            )
                                        }

                                        // MENÚ DE OPCIONES
                                        Box {

                                            IconButton(
                                                onClick = {
                                                    menuAbierto = true
                                                }
                                            ) {

                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Opciones",
                                                    tint = gris
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = menuAbierto,
                                                onDismissRequest = {
                                                    menuAbierto = false
                                                }
                                            ) {

                                                // EDITAR
                                                DropdownMenuItem(
                                                    text = {
                                                        Text("Editar")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        nombreEditado = lista.nombre
                                                        listaAEditar = lista
                                                        menuAbierto = false
                                                    }
                                                )

                                                // FAVORITOS
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (lista.esFavorita)
                                                                "Quitar de favoritos"
                                                            else
                                                                "Añadir a favoritos"
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = if (lista.esFavorita)
                                                                Icons.Default.Favorite
                                                            else
                                                                Icons.Default.FavoriteBorder,
                                                            contentDescription = null,
                                                            tint = rosa
                                                        )
                                                    },
                                                    onClick = {
                                                        repository.updateListFavorite(
                                                            listId = lista.id,
                                                            isFavorite = !lista.esFavorita
                                                        )
                                                        menuAbierto = false
                                                    }
                                                )

                                                // COMPARTIR
                                                DropdownMenuItem(
                                                    text = {
                                                        Text("Compartir")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Share,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {

                                                        listaParaCompartir = lista.id
                                                        codigoCompartir = null
                                                        cargandoCodigo = true
                                                        menuAbierto = false

                                                        repository.createShareCode(
                                                            listId = lista.id,
                                                            ownerId = ownerId
                                                        ) { codigo ->

                                                            codigoCompartir = codigo
                                                            cargandoCodigo = false
                                                        }
                                                    }
                                                )

                                                // ELIMINAR
                                                DropdownMenuItem(
                                                    text = {
                                                        Text("Eliminar")
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        listaAEliminar = lista
                                                        menuAbierto = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // BOTONES DE LISTAS
                    if (pantalla == "Listas") {

                        Button(
                            onClick = {
                                nombreNuevo = ""
                                mostrarVentana = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = verde
                            )
                        ) {

                            Icon(
                                Icons.Default.Add,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Crear nueva lista",
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                codigoIntroducido = ""
                                mensajeUnirse = null
                                mostrarUnirse = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {

                            Icon(
                                Icons.Default.GroupAdd,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Unirse a una lista")
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }

        // NAVEGACIÓN INFERIOR
        NavigationBar(
            containerColor = blanco
        ) {

            NavigationBarItem(
                selected = pantalla == "Listas",
                onClick = {
                    pantalla = "Listas"
                },
                icon = {
                    Icon(Icons.Default.List, contentDescription = null)
                },
                label = {
                    Text("Listas")
                }
            )

            NavigationBarItem(
                selected = pantalla == "Favoritos",
                onClick = {
                    pantalla = "Favoritos"
                },
                icon = {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                },
                label = {
                    Text("Favoritos")
                }
            )

            NavigationBarItem(
                selected = pantalla == "Perfil",
                onClick = {
                    pantalla = "Perfil"
                },
                icon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                label = {
                    Text("Perfil")
                }
            )
        }
    }

    // DIÁLOGO PARA CREAR UNA LISTA
    if (mostrarVentana) {

        AlertDialog(
            onDismissRequest = {
                mostrarVentana = false
            },
            title = {
                Text("Nueva lista")
            },
            text = {

                OutlinedTextField(
                    value = nombreNuevo,
                    onValueChange = {
                        nombreNuevo = it
                    },
                    label = {
                        Text("Nombre")
                    },
                    placeholder = {
                        Text("Ej. Compra semanal")
                    },
                    singleLine = true
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        if (nombreNuevo.isNotBlank()) {

                            val nuevaLista = ListaCompra(
                                id = UUID.randomUUID().toString(),
                                nombre = nombreNuevo.trim()
                            )

                            repository.createList(
                                listId = nuevaLista.id,
                                title = nuevaLista.nombre,
                                ownerId = ownerId
                            ) { correcto ->

                                if (correcto) {
                                    mostrarVentana = false
                                    nombreNuevo = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("Crear", color = verde)
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarVentana = false
                    }
                ) {
                    Text("Cancelar", color = gris)
                }
            },
            containerColor = blanco
        )
    }

    // DIÁLOGO PARA EDITAR UNA LISTA
    listaAEditar?.let { lista ->

        AlertDialog(
            onDismissRequest = {
                listaAEditar = null
            },
            title = {
                Text("Editar lista")
            },
            text = {

                OutlinedTextField(
                    value = nombreEditado,
                    onValueChange = {
                        nombreEditado = it
                    },
                    label = {
                        Text("Nombre")
                    },
                    singleLine = true
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        if (nombreEditado.isNotBlank()) {

                            listas = listas.map { otra ->

                                if (otra.id == lista.id) {
                                    otra.copy(
                                        nombre = nombreEditado.trim()
                                    )
                                } else {
                                    otra
                                }
                            }

                            listaAEditar = null
                        }
                    }
                ) {
                    Text("Guardar", color = verde)
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        listaAEditar = null
                    }
                ) {
                    Text("Cancelar", color = gris)
                }
            },
            containerColor = blanco
        )
    }

    // DIÁLOGO PARA ELIMINAR UNA LISTA
    listaAEliminar?.let { lista ->

        AlertDialog(
            onDismissRequest = {
                listaAEliminar = null
            },
            title = {
                Text("Eliminar lista")
            },
            text = {
                Text("¿Seguro que quieres eliminar \"${lista.nombre}\"?")
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        listas = listas.filterNot {
                            it.id == lista.id
                        }

                        listaAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        listaAEliminar = null
                    }
                ) {
                    Text("Cancelar", color = gris)
                }
            },
            containerColor = blanco
        )
    }

    // DIÁLOGO PARA COMPARTIR UNA LISTA
    listaParaCompartir?.let {

        AlertDialog(
            onDismissRequest = {
                listaParaCompartir = null
            },
            title = {
                Text("Compartir lista")
            },
            text = {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (cargandoCodigo) {

                        CircularProgressIndicator(color = verde)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Generando código...")

                    } else if (codigoCompartir != null) {

                        Text("Comparte este código con otras personas:")

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = codigoCompartir!!,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = verde
                        )

                    } else {

                        Text("No se ha podido generar el código. Inténtalo de nuevo.")
                    }
                }
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        listaParaCompartir = null
                    }
                ) {
                    Text("Cerrar", color = verde)
                }
            },
            containerColor = blanco
        )
    }

    // DIÁLOGO PARA UNIRSE A UNA LISTA
    if (mostrarUnirse) {

        AlertDialog(
            onDismissRequest = {

                if (!cargandoUnirse) {
                    mostrarUnirse = false
                }
            },
            title = {
                Text("Unirse a una lista")
            },
            text = {

                Column {

                    Text("Introduce el código de invitación:")

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = codigoIntroducido,
                        onValueChange = {
                            codigoIntroducido = it.uppercase()
                            mensajeUnirse = null
                        },
                        label = {
                            Text("Código")
                        },
                        singleLine = true
                    )

                    mensajeUnirse?.let { mensaje ->

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = mensaje,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {

                TextButton(
                    enabled = !cargandoUnirse,
                    onClick = {

                        val userId = FirebaseAuth.getInstance()
                            .currentUser?.uid.orEmpty()

                        if (codigoIntroducido.isBlank()) {

                            mensajeUnirse = "Introduce un código."

                        } else if (userId.isBlank()) {

                            mensajeUnirse = "Debes iniciar sesión."

                        } else {

                            cargandoUnirse = true

                            repository.joinListWithCode(
                                code = codigoIntroducido,
                                userId = userId
                            ) { correcto, error ->

                                cargandoUnirse = false

                                if (correcto) {

                                    mostrarUnirse = false
                                    codigoIntroducido = ""
                                    mensajeUnirse = null

                                } else {

                                    mensajeUnirse = error
                                        ?: "No se ha podido unir."
                                }
                            }
                        }
                    }
                ) {

                    if (cargandoUnirse) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = verde
                        )

                    } else {

                        Text("Unirse", color = verde)
                    }
                }
            },
            dismissButton = {

                TextButton(
                    enabled = !cargandoUnirse,
                    onClick = {
                        mostrarUnirse = false
                    }
                ) {
                    Text("Cancelar", color = gris)
                }
            },
            containerColor = blanco
        )
    }
}
