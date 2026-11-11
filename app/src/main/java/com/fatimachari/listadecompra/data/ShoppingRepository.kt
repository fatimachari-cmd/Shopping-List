package com.fatimachari.listadecompra.data

import android.util.Log
import com.fatimachari.listadecompra.models.Category
import com.fatimachari.listadecompra.models.Product
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.SecureRandom

class ShoppingRepository {

    private val db = FirebaseFirestore.getInstance()

    // Generador de códigos aleatorios
    private val random = SecureRandom()
    private val caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    // Escuchar las categorías de una lista
    fun listenCategories(
        listId: String,
        onChange: (List<Category>) -> Unit
    ): ListenerRegistration {

        return db.collection("lists")
            .document(listId)
            .collection("categories")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("Firestore", "Error leyendo categorías", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    }

                    onChange(categories)
                }
            }
    }

    // Escuchar los productos de una lista
    fun listenProducts(
        listId: String,
        onChange: (List<Product>) -> Unit
    ): ListenerRegistration {

        return db.collection("lists")
            .document(listId)
            .collection("products")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("Firestore", "Error leyendo productos", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    }

                    onChange(products)
                }
            }
    }

    // Guardar o actualizar un producto
    fun saveProduct(listId: String, product: Product) {

        val collection = db.collection("lists")
            .document(listId)
            .collection("products")

        val doc = if (product.id.isEmpty()) {
            collection.document()
        } else {
            collection.document(product.id)
        }

        val productToSave = product.copy(id = doc.id)

        doc.set(productToSave)
            .addOnSuccessListener {
                Log.d("Firestore", "Producto guardado: ${doc.id}")
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error al guardar producto", error)
            }
    }

    // Eliminar un producto
    fun deleteProduct(listId: String, productId: String) {

        db.collection("lists")
            .document(listId)
            .collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Producto eliminado: $productId")
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error al eliminar producto", error)
            }
    }

    // Guardar o actualizar una categoría
    fun saveCategory(listId: String, category: Category) {

        val collection = db.collection("lists")
            .document(listId)
            .collection("categories")

        val doc = if (category.id.isEmpty()) {
            collection.document()
        } else {
            collection.document(category.id)
        }

        doc.set(category.copy(id = doc.id))
            .addOnSuccessListener {
                Log.d("Firestore", "Categoría guardada: ${doc.id}")
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error al guardar categoría", error)
            }
    }

    // Eliminar una categoría y sus productos
    fun deleteCategory(
        listId: String,
        categoryId: String,
        productIds: List<String>
    ) {

        val listDoc = db.collection("lists").document(listId)
        val batch = db.batch()

        productIds.forEach { id ->
            batch.delete(listDoc.collection("products").document(id))
        }

        batch.delete(listDoc.collection("categories").document(categoryId))

        batch.commit()
            .addOnSuccessListener {
                Log.d("Firestore", "Categoría eliminada: $categoryId")
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error al eliminar categoría", error)
            }
    }

    // Crear una lista
    fun createList(
        listId: String,
        title: String,
        ownerId: String,
        onResult: (Boolean) -> Unit
    ) {

        val data = hashMapOf(
            "id" to listId,
            "title" to title,
            "ownerId" to ownerId,
            "members" to listOf(ownerId),
            "shareCode" to null,
            "isFavorite" to false
        )

        db.collection("lists")
            .document(listId)
            .set(data)
            .addOnSuccessListener {
                Log.d("Firestore", "Lista creada correctamente")
                onResult(true)
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error al crear la lista", error)
                onResult(false)
            }
    }

    // Escuchar las listas propias y compartidas
    fun listenLists(
        ownerId: String,
        onChange: (List<Map<String, Any?>>) -> Unit
    ): ListenerRegistration {

        val listasPropias = mutableMapOf<String, Map<String, Any?>>()
        val listasCompartidas = mutableMapOf<String, Map<String, Any?>>()

        fun publicarListas() {
            val todasLasListas = listasPropias + listasCompartidas
            onChange(todasLasListas.values.toList())
        }

        // Escuchar las listas propias
        val listenerPropias = db.collection("lists")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("Firestore", "Error leyendo listas propias", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listasPropias.clear()

                    snapshot.documents.forEach { documento ->
                        val datos = documento.data?.toMutableMap()

                        if (datos != null) {
                            datos["id"] = documento.id
                            listasPropias[documento.id] = datos
                        }
                    }

                    publicarListas()
                }
            }

        // Escuchar las listas compartidas
        val listenerCompartidas = db.collection("lists")
            .whereArrayContains("members", ownerId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("Firestore", "Error leyendo listas compartidas", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listasCompartidas.clear()

                    snapshot.documents.forEach { documento ->
                        val datos = documento.data?.toMutableMap()

                        if (datos != null) {
                            datos["id"] = documento.id
                            listasCompartidas[documento.id] = datos
                        }
                    }

                    publicarListas()
                }
            }

        // Cancelar los dos listeners
        return object : ListenerRegistration {
            override fun remove() {
                listenerPropias.remove()
                listenerCompartidas.remove()
            }
        }
    }

    // Generar un código para compartir una lista
    fun createShareCode(
        listId: String,
        ownerId: String,
        onResult: (String?) -> Unit
    ) {

        val codigo = buildString {
            repeat(8) {
                append(caracteres[random.nextInt(caracteres.length)])
            }
        }

        val listRef = db.collection("lists").document(listId)
        val codeRef = db.collection("shareCodes").document(codigo)

        db.runTransaction { transaction ->

            val listSnapshot = transaction.get(listRef)
            val codeSnapshot = transaction.get(codeRef)

            if (!listSnapshot.exists()) {
                throw Exception("La lista no existe")
            }

            if (listSnapshot.getString("ownerId") != ownerId) {
                throw Exception("No eres el propietario de esta lista")
            }

            if (codeSnapshot.exists()) {
                throw Exception("Código duplicado")
            }

            transaction.update(listRef, "shareCode", codigo)

            transaction.set(
                codeRef,
                mapOf(
                    "listId" to listId,
                    "createdBy" to ownerId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            codigo

        }.addOnSuccessListener { codigoCreado ->
            Log.d("Firestore", "Código creado: $codigoCreado")
            onResult(codigoCreado)

        }.addOnFailureListener { error ->
            Log.e("Firestore", "Error generando código", error)
            onResult(null)
        }
    }

    // Unirse a una lista mediante el código
    fun joinListWithCode(
        code: String,
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {

        val codigo = code.trim().uppercase()

        if (codigo.isEmpty() || userId.isEmpty()) {
            onResult(false, "Introduce un código válido")
            return
        }

        val codeRef = db.collection("shareCodes").document(codigo)

        // Comprobar que el código existe
        codeRef.get()
            .addOnSuccessListener { codeDocument ->

                if (!codeDocument.exists()) {
                    onResult(false, "El código no existe")
                    return@addOnSuccessListener
                }

                val listId = codeDocument.getString("listId")

                if (listId.isNullOrEmpty()) {
                    onResult(false, "Código incorrecto")
                    return@addOnSuccessListener
                }

                val listRef = db.collection("lists").document(listId)

                // Añadir directamente al usuario sin leer previamente la lista
                listRef.update(
                    mapOf(
                        "members" to FieldValue.arrayUnion(userId),
                        "joinCode" to codigo
                    )
                )
                    .addOnSuccessListener {
                        Log.d("Firestore", "Usuario añadido a la lista")
                        onResult(true, null)
                    }
                    .addOnFailureListener { error ->
                        Log.e("Firestore", "Error al unirse a la lista", error)

                        onResult(
                            false,
                            "No se ha podido acceder: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error buscando el código", error)

                onResult(
                    false,
                    "Error al comprobar el código: ${error.message}"
                )
            }
    }

    // Guardar el estado de favorito en Firestore
    fun updateListFavorite(
        listId: String,
        isFavorite: Boolean
    ) {

        db.collection("lists")
            .document(listId)
            .update("isFavorite", isFavorite)
            .addOnSuccessListener {
                Log.d("Firestore", "Favorito actualizado: $listId")
            }
            .addOnFailureListener { error ->
                Log.e("Firestore", "Error al actualizar favorito", error)
            }
    }

    fun updateListName(listId: String, nuevoNombre: String) {
        db.collection("lists").document(listId).update("title", nuevoNombre)
            .addOnFailureListener { Log.e("MiCesta", "Error al cambiar nombre: ${it.message}") }
    }

    fun deleteList(listId: String) {
        val listDoc = db.collection("lists").document(listId)
        listDoc.collection("products").get().addOnSuccessListener { productos ->
            listDoc.collection("categories").get().addOnSuccessListener { categorias ->
                val batch = db.batch()
                productos.documents.forEach { batch.delete(it.reference) }
                categorias.documents.forEach { batch.delete(it.reference) }
                batch.delete(listDoc)
                batch.commit().addOnFailureListener { Log.e("MiCesta", "Error al eliminar lista: ${it.message}") }
            }
        }
    }

    fun leaveList(listId: String, userId: String) {
        db.collection("lists").document(listId)
            .update("members", FieldValue.arrayRemove(userId))
            .addOnFailureListener { Log.e("MiCesta", "Error al salir de la lista: ${it.message}") }
    }

    fun listenListName(listId: String, onChange: (String) -> Unit): ListenerRegistration {
        return db.collection("lists").document(listId).addSnapshotListener { doc, _ ->
            if (doc == null) return@addSnapshotListener
            onChange(doc.getString("title") ?: "")
        }
    }

    fun sendInvitation(
        listId: String,
        listName: String,
        fromName: String,
        toEmail: String,
        onResult: (Boolean) -> Unit
    ) {
        val datos = mapOf(
            "listId" to listId,
            "listName" to listName,
            "fromName" to fromName,
            "toEmail" to toEmail.trim().lowercase()
        )
        db.collection("invitations").add(datos)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { error ->
                Log.e("MiCesta", "Error al enviar invitación: ${error.message}")
                onResult(false)
            }
    }

    fun listenInvitations(
        myEmail: String,
        onChange: (List<Map<String, Any?>>) -> Unit
    ): ListenerRegistration {
        return db.collection("invitations")
            .whereEqualTo("toEmail", myEmail.trim().lowercase())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MiCesta", "Error al escuchar invitaciones: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val invitaciones = snapshot.documents.map { doc ->
                    val datos = doc.data.orEmpty().toMutableMap()
                    datos["id"] = doc.id
                    datos
                }
                onChange(invitaciones)
            }
    }

    fun acceptInvitation(invitationId: String, listId: String, userId: String) {
        db.collection("lists").document(listId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnCompleteListener {
                db.collection("invitations").document(invitationId).delete()
            }
    }

    fun rejectInvitation(invitationId: String) {
        db.collection("invitations").document(invitationId).delete()
    }
}
