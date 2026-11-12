# 🧺 MiCesta — Lista de compra compartida

> **Haz la compra en equipo.** Organiza tus listas y compártelas con quien quieras.

MiCesta es una aplicación Android para crear listas de la compra y compartirlas con otras personas en tiempo real. Está hecha con **Kotlin**, **Jetpack Compose**, **Firebase** y **Trello**.

Proyecto desarrollado por **Fatima Chari** y **Loubna Houta** (DAM, Institut Baix Camp).

---

## 📱 Capturas de pantalla

| Bienvenida | Inicio de sesión | Registro |
|:---:|:---:|:---:|
| <img width="359" height="753" alt="Captura de pantalla 2026-11-12 003437" src="https://github.com/user-attachments/assets/a4af4c61-0a77-408c-a463-3cf82b1176be" /> |<img width="357" height="539" alt="Captura de pantalla 2026-11-12 003633" src="https://github.com/user-attachments/assets/abc37a05-1e91-4f40-8e60-943f8059b8e6" /> | <img width="358" height="612" alt="Captura de pantalla 2026-11-12 003743" src="https://github.com/user-attachments/assets/2a1686d5-ad1c-40a5-984b-a84c542aa587" /> |

| Mis listas | Detalle de una lista | Categorías |
|:---:|:---:|:---:|
| <img width="360" height="758" alt="Captura de pantalla 2026-11-12 014535" src="https://github.com/user-attachments/assets/b81ee3f4-57c8-4271-b087-69c7da69ef08" /> | <img width="360" height="761" alt="Captura de pantalla 2026-11-12 013045" src="https://github.com/user-attachments/assets/da11030d-86b9-4363-8e13-0e9a299f06a2" /> | <img width="357" height="257" alt="Captura de pantalla 2026-11-12 015330" src="https://github.com/user-attachments/assets/efc7663c-87ff-47ad-8aac-299f74a69840" /> |

| Invitaciones | Favoritos | Perfil |
|:---:|:---:|:---:|
| <img width="362" height="466" alt="Captura de pantalla 2026-11-12 013200" src="https://github.com/user-attachments/assets/2f48144e-0075-493d-b7cd-5fd07f9971ec" /> | <img width="720" height="1600" alt="WhatsApp Image 2026-11-12 at 01 34 21" src="https://github.com/user-attachments/assets/25f88230-3ff3-4840-9979-99c11414fc74" /> | <img width="720" height="1600" alt="WhatsApp Image 2026-11-12 at 01 51 46" src="https://github.com/user-attachments/assets/f8cd8288-4c65-4433-a077-0746892f40a6" /> |
|  |  | <img width="361" height="761" alt="Captura de pantalla 2026-11-12 014656" src="https://github.com/user-attachments/assets/ce48b24f-2dee-4bf9-acdb-dc20a876c9fe" /> |

---

## ✨ Funcionalidades

- **Cuenta de usuario**: registro e inicio de sesión con email y contraseña, o con Google.
- **Mis listas**: crear, renombrar y eliminar listas. Si una lista es compartida y no eres su dueño/a, la puedes abandonar.
- **Favoritos**: marca tus listas favoritas desde el menú ⋮ y encuéntralas en su pestaña.
- **Listas compartidas**: invita a otras personas a una lista. Quien recibe la invitación puede aceptarla o rechazarla.
- **Productos**: añade, edita y elimina productos con cantidad y unidad, y márcalos como completados.
- **Categorías**: organiza los productos por categorías. Al eliminar una categoría se pide confirmación, porque también se eliminan sus productos.
- **Clasificación automática**: al escribir un producto (por ejemplo "leche"), la app propone su categoría (por ejemplo "Lácteos").
- **Sincronización en tiempo real**: los cambios se ven al instante en todos los dispositivos gracias a Firestore.
- **Perfil**: consulta tus datos y cierra sesión.

---

## 🛠️ Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose + Material 3 |
| Autenticación | Firebase Authentication (email y Google) |
| Base de datos | Cloud Firestore |
| Organización | Trello |
| Inicio de sesión con Google | Credential Manager |

---

## 📋 Organización del proyecto

Para repartir el trabajo y seguir el avance usamos un tablero de Trello, con tareas por columnas (pendiente, en curso y hecho).

<img width="1876" height="899" alt="Captura de pantalla 2026-11-01 201548" src="https://github.com/user-attachments/assets/33ad2774-4caf-40cc-ad98-0d485b8da687" />

---

## 🗂️ Estructura del proyecto

```
app/src/main/java/com/fatimachari/listadecompra/
├── MainActivity.kt          # Punto de entrada y navegación entre pantallas
├── auth/
│   └── AuthRepository.kt    # Login, registro y Google Sign-In
├── data/
│   └── ShoppingRepository.kt# Listas, productos, categorías e invitaciones (Firestore)
├── models/                  # ListaCompra, Product, Category, Invitacion
└── ui/
    ├── screen/              # Welcome, Login, Register, MyList (listas, favoritos, perfil)
    ├── listdetail/          # Pantalla de detalle de una lista
    └── theme/               # Colores, tipografía y tema
```

### Modelo de datos en Firestore

```
lists/{listId}
 ├── products/{productId}
 └── categories/{categoryId}
```

---

## 🚀 Cómo ejecutarlo

1. Clona el repositorio:
   ```bash
   git clone https://github.com/fatimachari-cmd/Shopping-List.git
   ```
2. Abre la carpeta con **Android Studio**.
3. Espera a que Gradle sincronice las dependencias.
4. Ejecuta la app en un emulador o en un móvil con Android 7.0 o superior (botón **Run**).

> La app usa Firebase. El archivo `google-services.json` ya está en `app/`. Si quieres usar tu propio proyecto de Firebase, sustitúyelo por el tuyo y activa **Authentication** (email y Google) y **Firestore**.

---

## 👩‍💻 Autoras

| Autora | Parte principal |
|---|---|
| **Fatima Chari** | Inicio de sesión, mis listas, navegación, compartir y perfil |
| **Loubna** | Detalle de lista, productos, categorías y clasificación automática |

---

## 📄 Licencia

Proyecto educativo realizado como actividad de clase.
