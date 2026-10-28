package com.fatimachari.listadecompra.ui.theme.listdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fatimachari.listadecompra.models.Category
import com.fatimachari.listadecompra.models.Product
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ListDetailScreen(listId: String, onBack: () -> Unit) {

    // La lista de categorias
    val categories = remember {
        mutableStateListOf(
            Category(id = "1", name = "Frutas y verduras"),
            Category(id = "2", name = "Lácteos")
        )
    }

    // La lista de productos
    val products = remember {
        mutableStateListOf(
            Product(id = "1", name = "Manzana", categoryId = "1"),
            Product(id = "2", name = "Plátano", categoryId = "1"),
            Product(id = "3", name = "Leche", categoryId = "2"),
            Product(id = "4", name = "Queso", categoryId = "2")
        )
    }

    // Creamos variables de estado
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf(if (categories.isNotEmpty()) categories[0].id else "") }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var categoryError by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Compra semanal", style = MaterialTheme.typography.titleLarge)

        // Repetimos por cada categoría
        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    editingCategory = category
                    categoryName = category.name
                    categoryError = false
                    showCategoryDialog = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar categoría")
                }
                IconButton(onClick = { categoryToDelete = category }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar categoría")
                }
            }

            // Repetimos por cada producto de esta categoría
            products.filter { it.categoryId == category.id }.forEach { product ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = product.completed,
                        onCheckedChange = {
                            val index = products.indexOf(product)
                            products[index] = product.copy(completed = it)
                        }
                    )
                    Text(
                        text = product.name,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (product.completed) TextDecoration.LineThrough
                        else TextDecoration.None
                    )
                    IconButton(onClick = {
                        editingProduct = product
                        newName = product.name
                        selectedCategoryId = product.categoryId
                        error = false
                        showDialog = true
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar producto"
                        )
                    }
                    IconButton(onClick = { products.remove(product) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar producto")
                    }
                }
            }
        }

        // Botón para añadir producto nuevo
        Button(
            onClick = {
                editingProduct = null
                newName = ""
                selectedCategoryId = if (categories.isNotEmpty()) categories[0].id else ""
                error = false
                showDialog = true
            },
            modifier = Modifier.padding(top = 16.dp),
            enabled = categories.isNotEmpty()
        ) {
            Text("Añadir producto")
        }

        // Botón para añadir nueva categoría
        Button(
            onClick = {
                editingCategory = null
                categoryName = ""
                categoryError = false
                showCategoryDialog = true
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Nueva categoría")
        }

        // Diálogo para nueva/editar categoría
        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = { Text(if (editingCategory == null) "Nueva categoría" else "Editar categoría") },
                text = {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it; categoryError = false },
                        label = { Text("Nombre de la categoría") },
                        isError = categoryError,
                        supportingText = { if (categoryError) Text("El nombre no puede estar vacío") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (categoryName.isBlank()) {
                            categoryError = true
                        } else {
                            val editing = editingCategory
                            if (editing == null) {
                                categories.add(
                                    Category(
                                        id = System.currentTimeMillis().toString(),
                                        name = categoryName.trim()
                                    )
                                )
                            } else {
                                val index = categories.indexOfFirst { it.id == editing.id }
                                if (index != -1) {
                                    categories[index] = editing.copy(name = categoryName.trim())
                                }
                            }
                            categoryName = ""
                            showCategoryDialog = false
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para eliminar categoría
        val toDelete = categoryToDelete
        if (toDelete != null) {
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Eliminar categoría") },
                text = {
                    Text("¿Seguro que quieres eliminar \"${toDelete.name}\"? También se eliminarán todos sus productos.")
                },
                confirmButton = {
                    Button(onClick = {
                        products.removeAll { it.categoryId == toDelete.id }
                        categories.removeAll { it.id == toDelete.id }
                        categoryToDelete = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para añadir/editar producto
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingProduct == null) "Añadir producto" else "Editar producto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = {
                                newName = it;
                                error = false
                                if (editingProduct == null) {
                                    val guess = guessCategoryName(it)
                                    val match =
                                        categories.firstOrNull { c -> c.name.lowercase() == guess }
                                    if (match != null)
                                        selectedCategoryId = match.id
                                }
                            },
                            label = { Text("Nombre del producto") },
                            isError = error,
                            supportingText = { if (error) Text("El nombre no puede estar vacío") }
                        )

                        Text("Categoría", modifier = Modifier.padding(top = 16.dp))

                        categories.forEach { category ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id }
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
                                products.add(
                                    Product(
                                        id = System.currentTimeMillis().toString(),
                                        name = newName.trim(),
                                        categoryId = selectedCategoryId
                                    )
                                )
                            } else {
                                val index = products.indexOfFirst { it.id == editing.id }
                                if (index != -1) {
                                    products[index] = editing.copy(
                                        name = newName.trim(),
                                        categoryId = selectedCategoryId
                                    )
                                }
                            }
                            newName = ""
                            showDialog = false
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Cada palabra clave apunta al nombre de la categoría donde debe ir
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
    return keywords.entries.firstOrNull { text.contains(it.key) }?.value
}



