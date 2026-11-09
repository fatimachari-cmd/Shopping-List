package com.fatimachari.listadecompra.data

import android.util.Log
import com.fatimachari.listadecompra.models.Category
import com.fatimachari.listadecompra.models.Product
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ShoppingRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun lista(listId: String) = db.collection("lists").document(listId)

    fun listenLists(
        userId: String,
        onChange: (List<Map<String, Any?>>) -> Unit
    ): ListenerRegistration {
        return db.collection("lists")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MiCesta", "Error al escuchar listas: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val listas = snapshot.documents.map { doc ->
                    val datos = doc.data.orEmpty().toMutableMap()
                    datos["id"] = doc.id
                    datos
                }
                onChange(listas)
            }
    }

    fun createList(listId: String, title: String, ownerId: String, onResult: (Boolean) -> Unit) {
        val datos = mapOf(
            "title" to title,
            "ownerId" to ownerId,
            "members" to listOf(ownerId),
            "isFavorite" to false
        )
        lista(listId).set(datos)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { error ->
                Log.e("MiCesta", "Error al crear lista: ${error.message}")
                onResult(false)
            }
    }

    fun updateListName(listId: String, nuevoNombre: String) {
        lista(listId).update("title", nuevoNombre)
            .addOnFailureListener { Log.e("MiCesta", "Error al cambiar nombre: ${it.message}") }
    }

    fun updateListFavorite(listId: String, isFavorite: Boolean) {
        lista(listId).update("isFavorite", isFavorite)
            .addOnFailureListener { Log.e("MiCesta", "Error al marcar favorita: ${it.message}") }
    }

    fun deleteList(listId: String) {
        lista(listId).collection("products").get().addOnSuccessListener { productos ->
            lista(listId).collection("categories").get().addOnSuccessListener { categorias ->

                val batch = db.batch()
                productos.documents.forEach { batch.delete(it.reference) }
                categorias.documents.forEach { batch.delete(it.reference) }
                batch.delete(lista(listId))
                batch.commit()
                    .addOnFailureListener { Log.e("MiCesta", "Error al eliminar lista: ${it.message}") }
            }
        }
    }

    fun leaveList(listId: String, userId: String) {
        lista(listId).update("members", FieldValue.arrayRemove(userId))
            .addOnFailureListener { Log.e("MiCesta", "Error al salir de la lista: ${it.message}") }
    }

    fun listenListName(listId: String, onChange: (String) -> Unit): ListenerRegistration {
        return lista(listId).addSnapshotListener { doc, _ ->
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
        lista(listId).update("members", FieldValue.arrayUnion(userId))
            .addOnCompleteListener {
                db.collection("invitations").document(invitationId).delete()
            }
    }

    fun rejectInvitation(invitationId: String) {
        db.collection("invitations").document(invitationId).delete()
    }

    fun listenCategories(listId: String, onChange: (List<Category>) -> Unit): ListenerRegistration {
        return lista(listId).collection("categories")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val categorias = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                onChange(categorias)
            }
    }

    fun saveCategory(listId: String, category: Category) {
        val coleccion = lista(listId).collection("categories")
        val doc = if (category.id == "") coleccion.document() else coleccion.document(category.id)
        doc.set(category.copy(id = doc.id))
    }

    fun deleteCategory(listId: String, categoryId: String, productIds: List<String>) {
        val batch = db.batch()
        productIds.forEach { id ->
            batch.delete(lista(listId).collection("products").document(id))
        }
        batch.delete(lista(listId).collection("categories").document(categoryId))
        batch.commit()
    }

    fun listenProducts(listId: String, onChange: (List<Product>) -> Unit): ListenerRegistration {
        return lista(listId).collection("products")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val productos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }
                onChange(productos)
            }
    }

    fun saveProduct(listId: String, product: Product) {
        val coleccion = lista(listId).collection("products")
        val doc = if (product.id == "") coleccion.document() else coleccion.document(product.id)
        doc.set(product.copy(id = doc.id))
    }

    fun deleteProduct(listId: String, productId: String) {
        lista(listId).collection("products").document(productId).delete()
    }
}