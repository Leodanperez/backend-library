/* =========================================================
   auth-guard.js
   Se incluye en TODAS las páginas protegidas (admin, biblioteca, student).

   Como el JWT vive en una cookie httpOnly, JavaScript no puede
   leerla ni comprobar si expiró. Por eso el guardián le pregunta
   al backend "¿quién soy?" en cada carga de página: el navegador
   manda la cookie solo (gracias a credentials: "include"), y el
   backend responde según si la sesión sigue siendo válida.

   Uso: en el <body> de cada página pon el rol permitido, ej:
   <body data-role-required="ADMIN">
   =========================================================
*/

const ME_URL = "http://localhost:8080/api/v1/auth/me";
const LOGIN_PATH = "../login.html";

async function protectPage() {
  const requiredRole = document.body.dataset.roleRequired;

  try {
    const response = await fetch(ME_URL, {
      method: "GET",
      credentials: "include", // manda la cookie httpOnly automáticamente
    });

    if (!response.ok) {
      // 401/403: no hay sesión válida o expiró
      window.location.href = LOGIN_PATH;
      return;
    }

    const user = await response.json(); // ej. { role: "ADMIN", nombre: "Rosa Castillo" }

    if (requiredRole && user.role !== requiredRole) {
      // Sesión válida, pero entrando a una carpeta que no le corresponde
      window.location.href = LOGIN_PATH;
      return;
    }

    // Sesión válida y rol correcto: ahora sí mostramos el contenido
    sessionStorage.setItem("role", user.role);
    if (user.nombre) sessionStorage.setItem("nombre", user.nombre);
    document.documentElement.classList.remove("auth-checking");
  } catch (err) {
    window.location.href = LOGIN_PATH;
  }
}

// Corre apenas el DOM está listo para leer data-role-required
document.addEventListener("DOMContentLoaded", protectPage);

/**
 * Llama esto desde el botón "Cerrar sesión" de cada layout.
 * Le pide al backend que invalide/borre la cookie del lado del servidor.
 */
async function logout() {
  try {
    await fetch("http://localhost:8080/api/v1/auth/logout", {
      method: "POST",
      credentials: "include",
    });
  } finally {
    sessionStorage.clear();
    window.location.href = "../login.html";
  }
}
