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
}