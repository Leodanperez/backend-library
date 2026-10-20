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

    const user = await response.json();
    const nombre = [user.firstName, user.lastName].filter(Boolean).join(" ");

    if (requiredRole && user.role !== requiredRole) {
      window.location.href = LOGIN_PATH;
      return;
    }

    sessionStorage.setItem("role", user.role);
    sessionStorage.setItem("nombre", nombre);
    document.documentElement.classList.remove("auth-checking");
    fillSession(nombre);
  } catch (err) {
    window.location.href = LOGIN_PATH;
  }
}

// Corre apenas el DOM está listo para leer data-role-required
document.addEventListener("DOMContentLoaded", protectPage);

function fillSession(nombre) {
  const initials = nombre.split(" ").filter(Boolean).slice(0, 2).map(n => n[0]).join("").toUpperCase();
  const elNombre = document.getElementById("topbar-nombre");
  const elAvatar = document.getElementById("topbar-avatar");
  if (elNombre) elNombre.textContent = nombre;
  if (elAvatar) elAvatar.textContent = initials;
  document.querySelectorAll(".topnav-nombre").forEach(el => { el.textContent = nombre; });
  document.querySelectorAll(".topnav-avatar").forEach(el => { el.textContent = initials; });
  const elDash = document.getElementById("dash-nombre");
  if (elDash) elDash.textContent = nombre;
  const elWelcome = document.getElementById("welcome-nombre");
  if (elWelcome) elWelcome.textContent = nombre;
}

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
