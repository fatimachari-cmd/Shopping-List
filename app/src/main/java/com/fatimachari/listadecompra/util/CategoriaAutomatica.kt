package com.fatimachari.listadecompra.util

import java.text.Normalizer

private class Grupo(
    val nombreEs: String,
    val nombreCa: String,
    val palabrasEs: List<String>,
    val palabrasCa: List<String>
)

private val grupos = listOf(
    Grupo(
        "Lácteos", "Làctics",
        listOf("leche", "queso", "yogur", "yogurt", "mantequilla", "nata", "requeson", "kefir",
            "natillas", "cuajada", "flan", "huevo", "huevos", "crema"),
        listOf("llet", "formatge", "iogurt", "mantega", "brossat", "quefir", "mato", "ou", "ous")
    ),
    Grupo(
        "Frutas y verduras", "Fruites i verdures",
        listOf("manzana", "platano", "naranja", "limon", "pera", "fresa", "uva", "melon", "sandia",
            "kiwi", "mandarina", "melocoton", "cereza", "aguacate", "lechuga", "tomate", "zanahoria",
            "cebolla", "patata", "pimiento", "pepino", "calabacin", "ajo", "espinaca", "espinacas",
            "brocoli", "coliflor", "berenjena", "champinon", "seta", "puerro", "apio", "fruta",
            "verdura", "verduras", "frutas"),
        listOf("poma", "platan", "taronja", "llimona", "maduixa", "raim", "sindria", "pressec",
            "cirera", "alvocat", "enciam", "tomaquet", "pastanaga", "ceba", "pebrot", "cogombre",
            "carbasso", "all", "espinacs", "broquil", "alberginia", "xampinyo", "bolet", "porro",
            "api", "fruita", "fruites")
    ),
    Grupo(
        "Carne", "Carn",
        listOf("carne", "pollo", "ternera", "cerdo", "cordero", "jamon", "chorizo", "salchicha",
            "hamburguesa", "bacon", "panceta", "lomo", "pavo", "embutido", "salchichon", "longaniza",
            "conejo", "costilla"),
        listOf("carn", "pollastre", "vedella", "porc", "xai", "pernil", "xorizo", "botifarra",
            "cansalada", "llom", "embotit", "conill", "costella", "salsitxa")
    ),
    Grupo(
        "Pescado", "Peix",
        listOf("pescado", "merluza", "salmon", "atun", "bacalao", "sardina", "gamba", "langostino",
            "calamar", "pulpo", "mejillon", "lubina", "dorada", "trucha", "anchoa", "marisco", "sepia"),
        listOf("peix", "lluc", "tonyina", "bacalla", "llagosti", "pop", "musclo", "llobarro",
            "orada", "truita", "anxova", "marisc")
    ),
    Grupo(
        "Panadería", "Fleca",
        listOf("pan", "baguette", "bollo", "croissant", "magdalena", "galleta", "tostada", "harina",
            "bizcocho", "cereales", "donut", "napolitana", "panecillo"),
        listOf("pa", "barra", "brioix", "galeta", "farina", "pastis", "cereals", "ensaimada", "coca")
    ),
    Grupo(
        "Bebidas", "Begudes",
        listOf("agua", "refresco", "cerveza", "vino", "zumo", "cola", "cafe", "te", "infusion",
            "gaseosa", "sidra", "licor", "ron", "whisky", "tonica", "batido"),
        listOf("aigua", "refresc", "cervesa", "vi", "suc", "infusio", "gasosa", "rom", "batut")
    ),
    Grupo(
        "Limpieza", "Neteja",
        listOf("lejia", "detergente", "jabon", "suavizante", "fregona", "estropajo", "bayeta",
            "lavavajillas", "desinfectante", "amoniaco", "basura", "basuras", "limpiador"),
        listOf("lleixiu", "detergent", "sabo", "suavitzant", "fregall", "baieta", "rentaplats",
            "desinfectant", "amoniac", "escombraries", "netejador")
    ),
    Grupo(
        "Higiene", "Higiene",
        listOf("champu", "gel", "dentifrico", "cepillo", "desodorante", "compresa", "compresas",
            "panuelo", "panuelos", "algodon", "colonia", "maquinilla", "higienico"),
        listOf("xampu", "dentifrici", "raspall", "desodorant", "mocadors", "coto", "maquineta",
            "higienic")
    ),
    Grupo(
        "Despensa", "Rebost",
        listOf("arroz", "pasta", "macarrones", "espagueti", "espaguetis", "lentejas", "garbanzos",
            "judias", "aceite", "sal", "azucar", "vinagre", "salsa", "conserva", "conservas",
            "mayonesa", "ketchup", "miel", "mermelada", "chocolate", "cacao"),
        listOf("arros", "macarrons", "llenties", "cigrons", "mongetes", "oli", "sucre",
            "maionesa", "ketxup", "mel", "melmelada", "xocolata", "cacau")
    )
)

object CategoriaAutomatica {

    // Quita tildes y mayúsculas: "Làctics" -> "lactics"
    private fun sinTildes(texto: String): String {
        val separado = Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
        return separado.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    // Esta palabra del producto es la palabra clave? también plural: tomate -> tomates
    private fun mismaPalabra(palabra: String, clave: String): Boolean {
        return palabra == clave || palabra == clave + "s" || palabra == clave + "es" || palabra == clave + "os"
    }

    // Devuelve el nombre de la categoría para un producto, o null si no lo reconoce.
    // Si la palabra es catalana ("llet"), el nombre sale en catalán ("Làctics").
    fun sugerir(productName: String): String? {
        val palabras = sinTildes(productName).split(Regex("[^a-z]+")).filter { it.isNotEmpty() }

        for (grupo in grupos) {
            for (palabra in palabras) {
                if (grupo.palabrasEs.any { mismaPalabra(palabra, it) }) return grupo.nombreEs
                if (grupo.palabrasCa.any { mismaPalabra(palabra, it) }) return grupo.nombreCa
            }
        }
        return null
    }

    // Esta categoría ya existente es la sugerida? (vale en español o catalán)
    fun coincide(nombreCategoria: String, sugerida: String): Boolean {
        val categoria = sinTildes(nombreCategoria)
        val sugeridaLimpia = sinTildes(sugerida)
        if (categoria == sugeridaLimpia) return true

        val grupo = grupos.firstOrNull {
            sinTildes(it.nombreEs) == sugeridaLimpia || sinTildes(it.nombreCa) == sugeridaLimpia
        } ?: return false

        return categoria == sinTildes(grupo.nombreEs) || categoria == sinTildes(grupo.nombreCa)
    }
}