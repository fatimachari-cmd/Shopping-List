
package com.fatimachari.listadecompra.ui.theme.listdetail

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fatimachari.listadecompra.data.ShoppingRepository
import com.fatimachari.listadecompra.models.Category
import com.fatimachari.listadecompra.models.Product

@Composable
fun ListDetailScreen(listId: String, onBack: () -> Unit) {

    // Colores de la pantalla
    val fondo = Color(0xFFF6F3EB)
    val verde = Color(0xFF587653)
    val texto = Color(0xFF30382F)
    val blanco = Color.White

    // Listas de categorías y productos
    val categories = remember { mutableStateListOf<Category>() }
    val products = remember { mutableStateListOf<Product>() }

    // Controlar qué categorías están abiertas
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

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var categoryError by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val scrollState = rememberScrollState()
    val repository = remember { ShoppingRepository() }

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

        onDispose {
            categoriesListener.remove()
            productsListener.remove()
        }
    }

    // Pantalla principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .statusBarsPadding()
    ) {

        // CONTENIDO DESPLAZABLE
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            // CABECERA
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = verde
                    )
                }

                Text(
                    text = "Compra semanal",
                    style = MaterialTheme.typography.titleLarge,
                    color = texto
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MENSAJE CUANDO NO HAY CATEGORÍAS
            if (categories.isEmpty()) {
                Text(
                    text = "Todavía no hay categorías. Pulsa «Nueva categoría» para empezar.",
                    color = texto,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // MOSTRAR CATEGORÍAS
            categories.forEachIndexed { index, category ->

                // Colores suaves para las categorías
                val colorCategoria = when (index % 4) {
                    0 -> Color(0xFFDDEBD8)
                    1 -> Color(0xFFDCEAF2)
                    2 -> Color(0xFFF5E3D5)
                    else -> Color(0xFFF3E3E8)
                }

                val isExpanded = category.id in expandedCategories

                // CABECERA DE CATEGORÍA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Tarjeta pulsable: abre y cierra los productos
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
                        Text(
                            text = category.name,
                            color = texto,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            )
                        )
                    }

                    // Menú de opciones de la categoría
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
                                tint = verde
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

                // MOSTRAR PRODUCTOS SOLO SI LA CATEGORÍA ESTÁ ABIERTA
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

                                    // CÍRCULO PARA MARCAR COMO COMPRADO
                                    Box(
                                        modifier = Modifier
                                            .size(25.dp)
                                            .background(
                                                color = if (product.completed) {
                                                    verde
                                                } else {
                                                    Color.Transparent
                                                },
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = if (product.completed) {
                                                    verde
                                                } else {
                                                    Color(0xFF899187)
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

                                    // Nombre del producto
                                    Text(
                                        text = product.name,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp),
                                        color = texto
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
                                                tint = verde
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

        // BOTONES FIJOS EN LA PARTE INFERIOR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(fondo)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            // AÑADIR PRODUCTO
            Button(
                onClick = {
                    editingProduct = null
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
                    containerColor = verde
                ),
                enabled = categories.isNotEmpty()
            ) {
                Text("Añadir producto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // NUEVA CATEGORÍA
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
                    contentColor = verde
                )
            ) {
                Text("Nueva categoría")
            }
        }
    }

    // DIÁLOGO PARA CREAR O EDITAR CATEGORÍA
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

    // DIÁLOGO PARA ELIMINAR CATEGORÍA
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

    // DIÁLOGO PARA AÑADIR O EDITAR PRODUCTO
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
                            if (editingProduct == null) {
                                val guess = guessCategoryName(it)

                                val match = categories.firstOrNull { c ->
                                    c.name.lowercase() == guess
                                }

                                if (match != null) {
                                    selectedCategoryId = match.id
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
                            repository.saveProduct(
                                listId,
                                Product(
                                    name = newName.trim(),
                                    categoryId = selectedCategoryId
                                )
                            )
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

// Palabras clave para clasificar productos automáticamente
val keywords = mapOf(
    "leche" to "lácteos",
    "queso" to "lácteos",
    "yogur" to "lácteos",
    "mantequilla" to "lácteos",
    "manzana" to "frutas y verduras",
    "plátano" to "frutas y verduras",
    "naranja" to "frutas y verduras",
    "lechuga" to "frutas y verduras",
    "tomate" to "frutas y verduras",
    "zanahoria" to "frutas y verduras"
)

fun guessCategoryName(productName: String): String? {
    val text = productName.trim().lowercase()

    return keywords.entries.firstOrNull {
        text.contains(it.key)
    }?.value
}
