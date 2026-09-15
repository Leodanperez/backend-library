/* =========================================================
   auth.js
   Maneja el login y la redirección según el rol del usuario.
   Se incluye SOLO en pages/login.html

   El backend genera el JWT y lo manda como cookie httpOnly
   (Set-Cookie en la respuesta). JavaScript NUNCA ve ese token,
   así que aquí solo nos importa lo que venga en el body de la
   respuesta (ej. el rol), y dejar que el navegador guarde la
   cookie solo.
   ========================================================= */

const API_URL = "http://localhost:8080/api/v1/auth/login";

// A qué carpeta va cada rol una vez logueado.
// Las rutas son relativas a pages/login.html
const ROLE_HOME = {
  ADMIN: "admin/dashboard.html",
  LIBRARIAN: "biblioteca/dashboard.html",
  STUDENT: "student/dashboard.html",
};

function showError(message) {
  const errorBox = document.getElementById("login-error");
  errorBox.textContent = message;
  errorBox.style.display = "block";
}

function hideError() {
  const errorBox = document.getElementById("login-error");
  errorBox.style.display = "none";
}

async function handleLoginSubmit(event) {
  event.preventDefault();
  hideError();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include", // <- CLAVE: permite que el navegador guarde la cookie httpOnly
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      showError("Correo o contraseña incorrectos. Inténtalo de nuevo.");
      return;
    }

    // El JWT ya quedó guardado como cookie httpOnly por el navegador.
    // Lo único que necesitamos del body es el rol, para saber a dónde redirigir.
    const data = await response.json(); // ej. { role: "ADMIN", nombre: "Rosa Castillo" }
    const role = data.role;

    // Guardamos SOLO el rol (dato no sensible) para pintar la UI
    // (nombre en la topbar, qué menú mostrar). La seguridad real
    // la sigue validando el backend con la cookie en cada request.
    sessionStorage.setItem("role", role);
    if (data.nombre) sessionStorage.setItem("nombre", data.nombre);

    const destino = ROLE_HOME[role];
    if (!destino) {
      showError("Tu cuenta no tiene un rol válido asignado.");
      return;
    }

    window.location.href = destino;
  } catch (err) {
    showError("No se pudo conectar con el servidor. Intenta de nuevo.");
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("form");
  form.addEventListener("submit", handleLoginSubmit);
});
