package com.fatimachari.listadecompra.data

import android.util.Log
import com.fatimachari.listadecompra.models.Category
import com.fatimachari.listadecompra.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ShoppingRepository {
    private val db = FirebaseFirestore.getInstance()

    fun listenCategories(listId: String, onChange: (List<Category>) -> Unit): ListenerRegistration {
        return db.collection("lists").document(listId).collection("categories")
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

    fun listenProducts(listId: String, onChange: (List<Product>) -> Unit): ListenerRegistration {
        return db.collection("lists").document(listId).collection("products")
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

    // Guarda o actualiza un producto en Firestore
    fun saveProduct(listId: String, product: Product) {
        val collection = db.collection("lists").document(listId).collection("products")
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
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar producto", e)
            }
    }

    // Elimina un producto de Firestore
    fun deleteProduct(listId: String, productId: String) {
        db.collection("lists").document(listId).collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Producto eliminado: $productId")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al eliminar producto", e)
            }
    }
}
