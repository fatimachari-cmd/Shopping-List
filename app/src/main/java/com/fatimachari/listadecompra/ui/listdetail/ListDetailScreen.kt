package com.fatimachari.listadecompra.ui.listdetail

import com.fatimachari.listadecompra.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatimachari.listadecompra.data.ShoppingRepository
import com.fatimachari.listadecompra.models.Category
import com.fatimachari.listadecompra.models.Product
import com.fatimachari.listadecompra.util.CategoriaAutomatica

@Composable
fun ListDetailScreen(listId: String, onBack: () -> Unit) {

    // Colores de la pantalla

    val fondo = Fondo
    val principal = Principal
    val texto = Texto
    val blanco = Color.White

    // Listas de categorías y productos
    val categories = remember { mutableStateListOf<Category>() }
    val products = remember { mutableStateListOf<Product>() }

    var expandedCategories by remember {
        mutableStateOf(setOf<String>())
    }

    // Variables para los productos
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    var selectedCategoryId by remember {
        mutableStateOf("")
    }

    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // true si la persona ha elegido la categoría a mano (entonces no clasificamos solos)
    var categoriaManual by remember { mutableStateOf(false) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var categoryError by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val scrollState = rememberScrollState()
    val repository = remember { ShoppingRepository() }

    // Nombre real de la lista (se lee de Firestore)

    var nombreLista by remember { mutableStateOf("") }

    // Escuchar los cambios de Firebase
    DisposableEffect(listId) {

        val categoriesListener = repository.listenCategories(listId) {
            categories.clear()
            categories.addAll(it)
        }

        val productsListener = repository.listenProducts(listId) {
            products.clear()
            products.addAll(it)
        }

        val nameListener = repository.listenListName(listId) {
            nombreLista = it
        }

        onDispose {
            categoriesListener.remove()
            productsListener.remove()
            nameListener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .statusBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            // CABECERA
            val comprados = products.count { it.completed }
            val total = products.size

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(blanco, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = principal
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = nombreLista,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = texto
                    )
                    Text(
                        text = "$comprados de $total comprados",
                        fontSize = 13.sp,
                        color = Gris
                    )
                }
            }

            // Barra de progreso
            if (total > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { comprados.toFloat() / total },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = principal,
                    trackColor = Borde
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MENSAJE SI NO HAY CATEGORÍAS
            if (categories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        tint = principal,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Todavía no hay categorías",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = texto
                    )
                    Text(
                        "Añade un producto y su categoría se crea sola",
                        color = Gris
                    )
                }
            }

            // CATEGORÍAS
            categories.forEachIndexed { index, category ->

                val colorCategoria = when (index % 4) {
                    0 -> Color(0xFFD9E8DC)
                    1 -> Color(0xFFEADFC2)
                    2 -> Color(0xFFCCE2D6)
                    else -> Color(0xFFE6D9CF)
                }

                val isExpanded = category.id in expandedCategories

                // Cabecera de categoría
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                expandedCategories =
                                    if (isExpanded) {
                                        expandedCategories - category.id
                                    } else {
                                        expandedCategories + category.id
                                    }
                            },
                        shape = RoundedCornerShape(50.dp),
                        color = colorCategoria
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                color = texto,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${products.count { it.categoryId == category.id }}",
                                color = texto,
                                fontSize = 13.sp
                            )
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = texto
                            )
                        }
                    }

                    // Opciones de la categoría
                    var menuCategoriaAbierto by remember {
                        mutableStateOf(false)
                    }

                    Box {
                        IconButton(
                            onClick = {
                                menuCategoriaAbierto = true
                            }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = principal
                            )
                        }

                        DropdownMenu(
                            expanded = menuCategoriaAbierto,
                            onDismissRequest = {
                                menuCategoriaAbierto = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    editingCategory = category
                                    categoryName = category.name
                                    categoryError = false
                                    showCategoryDialog = true
                                    menuCategoriaAbierto = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    categoryToDelete = category
                                    menuCategoriaAbierto = false
                                }
                            )
                        }
                    }
                }

                if (isExpanded) {

                    products
                        .filter { it.categoryId == category.id }
                        .forEach { product ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 8.dp,
                                        top = 6.dp
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = blanco
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 1.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(25.dp)
                                            .background(
                                                color = if (product.completed) {
                                                    principal
                                                } else {
                                                    Color.Transparent
                                                },
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = if (product.completed) {
                                                    principal
                                                } else {
                                                    Gris
                                                },
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                repository.saveProduct(
                                                    listId,
                                                    product.copy(
                                                        completed = !product.completed
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (product.completed) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Comprado",
                                                tint = blanco,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = product.name,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp),
                                        color = if (product.completed) Gris else texto,
                                        textDecoration = if (product.completed) {
                                            TextDecoration.LineThrough
                                        } else {
                                            null
                                        }
                                    )

                                    // Menú de opciones del producto
                                    var menuProductoAbierto by remember {
                                        mutableStateOf(false)
                                    }

                                    Box {
                                        IconButton(
                                            onClick = {
                                                menuProductoAbierto = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "Opciones",
                                                tint = principal
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = menuProductoAbierto,
                                            onDismissRequest = {
                                                menuProductoAbierto = false
                                            }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    editingProduct = product
                                                    categoriaManual = true
                                                    newName = product.name
                                                    selectedCategoryId =
                                                        product.categoryId
                                                    error = false
                                                    showDialog = true
                                                    menuProductoAbierto = false
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text("Eliminar") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    repository.deleteProduct(
                                                        listId,
                                                        product.id
                                                    )
                                                    menuProductoAbierto = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(fondo)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            // BOTONES DE ABAJO
            Button(
                onClick = {
                    editingProduct = null
                    categoriaManual = false
                    newName = ""
                    selectedCategoryId = categories.firstOrNull()?.id ?: ""
                    error = false
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = principal
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir producto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    editingCategory = null
                    categoryName = ""
                    categoryError = false
                    showCategoryDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = principal
                )
            ) {
                Text("Nueva categoría")
            }
        }
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showCategoryDialog = false
            },
            title = {
                Text(
                    if (editingCategory == null) {
                        "Nueva categoría"
                    } else {
                        "Editar categoría"
                    }
                )
            },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        categoryError = false
                    },
                    label = {
                        Text("Nombre de la categoría")
                    },
                    isError = categoryError,
                    supportingText = {
                        if (categoryError) {
                            Text("El nombre no puede estar vacío")
                        }
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (categoryName.isBlank()) {
                        categoryError = true
                    } else {
                        val editing = editingCategory

                        if (editing == null) {
                            repository.saveCategory(
                                listId,
                                Category(name = categoryName.trim())
                            )
                        } else {
                            repository.saveCategory(
                                listId,
                                editing.copy(name = categoryName.trim())
                            )
                        }

                        categoryName = ""
                        showCategoryDialog = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCategoryDialog = false
                }) {
                    Text("Cancelar")
                }
            },
            containerColor = fondo
        )
    }

    // Diálogo para eliminar categoría
    categoryToDelete?.let { category ->

        AlertDialog(
            onDismissRequest = {
                categoryToDelete = null
            },
            title = {
                Text("Eliminar categoría")
            },
            text = {
                Text(
                    "¿Seguro que quieres eliminar \"${category.name}\"? También se eliminarán todos sus productos."
                )
            },
            confirmButton = {
                Button(onClick = {
                    val ids = products
                        .filter { it.categoryId == category.id }
                        .map { it.id }

                    repository.deleteCategory(
                        listId,
                        category.id,
                        ids
                    )

                    expandedCategories =
                        expandedCategories - category.id

                    categoryToDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    categoryToDelete = null
                }) {
                    Text("Cancelar")
                }
            },
            containerColor = fondo
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(
                    if (editingProduct == null) {
                        "Añadir producto"
                    } else {
                        "Editar producto"
                    }
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            error = false

                            // Clasificación automática
                            if (editingProduct == null && !categoriaManual) {
                                val sugerida = CategoriaAutomatica.sugerir(it)
                                val existente = sugerida?.let { s ->
                                    categories.firstOrNull { c -> CategoriaAutomatica.coincide(c.name, s) }
                                }

                                selectedCategoryId = when {
                                    existente != null -> existente.id
                                    sugerida != null -> ""   // se creará al guardar
                                    else -> categories.firstOrNull()?.id ?: ""
                                }
                            }
                        },
                        label = {
                            Text("Nombre del producto")
                        },
                        isError = error,
                        supportingText = {
                            if (error) {
                                Text("El nombre no puede estar vacío")
                            }
                        }
                    )

                    // Aviso de la categoría automática
                    val sugerida = if (editingProduct == null && !categoriaManual && newName.isNotBlank()) {
                        CategoriaAutomatica.sugerir(newName)
                    } else {
                        null
                    }
                    if (sugerida != null) {
                        val destino = categories.firstOrNull { CategoriaAutomatica.coincide(it.name, sugerida) }
                        Text(
                            if (destino != null) "Categoría automática: «${destino.name}»"
                            else "Se creará la categoría «$sugerida»",
                            color = Principal,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        "Categoría",
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    categories.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategoryId == category.id,
                                onClick = {
                                    categoriaManual = true
                                    selectedCategoryId = category.id
                                }
                            )

                            Text(category.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isBlank()) {
                        error = true
                    } else {
                        val editing = editingProduct

                        if (editing == null) {
                            var idCategoria = selectedCategoryId

                            // Si la categoría sugerida no existe, la creamos
                            val sugerida = if (categoriaManual) null else CategoriaAutomatica.sugerir(newName)
                            if (sugerida != null) {
                                val existente = categories.firstOrNull { CategoriaAutomatica.coincide(it.name, sugerida) }
                                idCategoria = existente?.id
                                    ?: repository.saveCategory(listId, Category(name = sugerida))
                            } else if (idCategoria == "") {
                                idCategoria = repository.saveCategory(listId, Category(name = "Otros"))
                            }

                            repository.saveProduct(
                                listId,
                                Product(
                                    name = newName.trim(),
                                    categoryId = idCategoria
                                )
                            )

                            // Abrimos la categoría para que se vea el producto
                            expandedCategories = expandedCategories + idCategoria
                        } else {
                            repository.saveProduct(
                                listId,
                                editing.copy(
                                    name = newName.trim(),
                                    categoryId = selectedCategoryId
                                )
                            )
                        }

                        newName = ""
                        showDialog = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("Cancelar")
                }
            },
            containerColor = fondo
        )
    }
}