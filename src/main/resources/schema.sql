-- =============================================================================
-- SCHEMA - Sistema de Biblioteca
-- Descripción: Modelo de base de datos para gestión de biblioteca escolar.
--              Los usuarios del sistema son principalmente estudiantes.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TABLA: author
-- Almacena los autores de los libros disponibles en la biblioteca.
-- -----------------------------------------------------------------------------
CREATE TABLE author (
    id          BIGSERIAL    PRIMARY KEY,                          -- Identificador único del autor
    first_name  VARCHAR(100) NOT NULL,                            -- Nombre(s) del autor
    last_name   VARCHAR(100) NOT NULL,                            -- Apellido(s) del autor
    pseudonym   VARCHAR(100),                                     -- Seudónimo o nombre literario (opcional)
    birth_date  DATE,                                             -- Fecha de nacimiento del autor
    nationality VARCHAR(80),                                      -- Nacionalidad del autor (ej: "Colombiano")
    biography   TEXT,                                             -- Reseña biográfica del autor
    email       VARCHAR(150) UNIQUE,                              -- Correo electrónico de contacto del autor (único)
    active      BOOLEAN      NOT NULL DEFAULT TRUE,               -- Indica si el autor está activo en el catálogo
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha y hora de registro del autor
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP   -- Fecha y hora de la última modificación
);

-- -----------------------------------------------------------------------------
-- TABLA: category
-- Almacena las categorías o géneros literarios para clasificar los libros.
-- -----------------------------------------------------------------------------
CREATE TABLE category (
    id          BIGSERIAL    PRIMARY KEY,                          -- Identificador único de la categoría
    name        VARCHAR(100) NOT NULL UNIQUE,                     -- Nombre de la categoría (ej: "Ciencia Ficción", "Historia")
    description TEXT,                                             -- Descripción detallada de la categoría
    active      BOOLEAN      NOT NULL DEFAULT TRUE,               -- Indica si la categoría está disponible para asignar
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha y hora de creación de la categoría
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP   -- Fecha y hora de la última modificación
);

-- -----------------------------------------------------------------------------
-- TABLA: book
-- Almacena el catálogo de libros de la biblioteca.
-- Cada libro pertenece a un autor y una categoría.
-- -----------------------------------------------------------------------------
CREATE TABLE book (
    id               BIGSERIAL    PRIMARY KEY,                          -- Identificador único del libro
    title            VARCHAR(200) NOT NULL,                             -- Título del libro
    isbn             VARCHAR(20)  UNIQUE,                               -- Código ISBN del libro (único a nivel mundial)
    description      TEXT,                                              -- Sinopsis o descripción del contenido del libro
    publication_year INT,                                               -- Año de publicación original del libro
    pages            INT,                                               -- Número total de páginas del libro
    language         VARCHAR(50),                                       -- Idioma en que está escrito el libro (ej: "Español")
    publisher        VARCHAR(150),                                      -- Editorial que publicó el libro
    cover_url        VARCHAR(500),                                      -- URL de la imagen de portada del libro
    author_id        BIGINT       NOT NULL REFERENCES author(id),       -- Autor al que pertenece el libro
    category_id      BIGINT       NOT NULL REFERENCES category(id),     -- Categoría o género literario del libro
    active           BOOLEAN      NOT NULL DEFAULT TRUE,                -- Indica si el libro está visible en el catálogo
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- Fecha y hora de registro del libro
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP    -- Fecha y hora de la última modificación
);

CREATE INDEX idx_book_author_id   ON book(author_id);    -- Índice para búsquedas de libros por autor
CREATE INDEX idx_book_category_id ON book(category_id);  -- Índice para búsquedas de libros por categoría

-- -----------------------------------------------------------------------------
-- TABLA: book_copy
-- Almacena los ejemplares físicos de cada libro.
-- Un mismo libro puede tener múltiples ejemplares en distintas condiciones.
-- -----------------------------------------------------------------------------
CREATE TABLE book_copy (
    id               BIGSERIAL    PRIMARY KEY,                         -- Identificador único del ejemplar
    book_id          BIGINT       NOT NULL REFERENCES book(id),        -- Libro al que corresponde este ejemplar
    code             VARCHAR(50)  NOT NULL UNIQUE,                     -- Código de inventario del ejemplar (ej: "LIB-0042")
    status           VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE'         -- Estado actual del ejemplar
                         CHECK (status IN ('AVAILABLE','LOANED','LOST','DAMAGED')),
                                                                       --   AVAILABLE: disponible para préstamo
                                                                       --   LOANED:    actualmente prestado a un estudiante
                                                                       --   LOST:      reportado como perdido
                                                                       --   DAMAGED:   deteriorado, fuera de circulación
    condition        VARCHAR(30)  NOT NULL                             -- Condición física del ejemplar
                         CHECK (condition IN ('NEW','GOOD','FAIR','DAMAGED')),
                                                                       --   NEW:     nuevo, sin uso
                                                                       --   GOOD:    buen estado, uso mínimo
                                                                       --   FAIR:    estado regular, uso moderado
                                                                       --   DAMAGED: deteriorado visiblemente
    acquisition_date DATE,                                             -- Fecha en que la biblioteca adquirió el ejemplar
    price            DECIMAL(10,2),                                    -- Precio de adquisición del ejemplar (para control de inventario)
    location         VARCHAR(100),                                     -- Ubicación física en la biblioteca (ej: "Estante A - Fila 3")
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha y hora de registro del ejemplar
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP   -- Fecha y hora de la última modificación
);

CREATE INDEX idx_book_copy_book_id ON book_copy(book_id);  -- Índice para búsquedas de ejemplares por libro

-- -----------------------------------------------------------------------------
-- TABLA: users
-- Almacena los usuarios del sistema: estudiantes, bibliotecarios y administradores.
-- Nota: "user" es palabra reservada en PostgreSQL, por eso se usa "users".
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id                BIGSERIAL    PRIMARY KEY,                          -- Identificador único del usuario
    first_name        VARCHAR(100) NOT NULL,                             -- Nombre(s) del estudiante o usuario
    last_name         VARCHAR(100) NOT NULL,                             -- Apellido(s) del estudiante o usuario
    email             VARCHAR(150) NOT NULL UNIQUE,                      -- Correo institucional del estudiante (usado para login)
    password          VARCHAR(255) NOT NULL,                             -- Contraseña encriptada (bcrypt)
    phone             VARCHAR(20),                                       -- Número de teléfono de contacto del estudiante
    address           VARCHAR(250),                                      -- Dirección de residencia del estudiante
    birth_date        DATE,                                              -- Fecha de nacimiento del estudiante
    role              VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'            -- Rol del usuario dentro del sistema
                          CHECK (role IN ('ADMIN','LIBRARIAN','STUDENT')),
                                                                         --   STUDENT:    estudiante, solo puede solicitar préstamos
                                                                         --   LIBRARIAN:  bibliotecario, gestiona préstamos y catálogo
                                                                         --   ADMIN:      administrador, acceso total al sistema
    registration_date DATE         NOT NULL DEFAULT CURRENT_DATE,        -- Fecha en que el estudiante fue registrado en el sistema
    active            BOOLEAN      NOT NULL DEFAULT TRUE,                -- Indica si la cuenta del estudiante está habilitada
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- Fecha y hora de creación del registro
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP    -- Fecha y hora de la última modificación
);

-- -----------------------------------------------------------------------------
-- TABLA: loan_status
-- Catálogo de estados posibles para un préstamo.
-- Valores esperados: REQUESTED, PENDING, RETURNED, CANCELLED, OVERDUE
-- -----------------------------------------------------------------------------
CREATE TABLE loan_status (
    id          BIGSERIAL    PRIMARY KEY,                          -- Identificador único del estado
    name        VARCHAR(50)  NOT NULL UNIQUE,                     -- Nombre del estado (ej: "PENDING", "RETURNED")
    description VARCHAR(255),                                     -- Descripción legible del estado para mostrar en pantalla
    active      BOOLEAN      NOT NULL DEFAULT TRUE,               -- Indica si el estado está en uso
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha y hora de creación del registro
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP   -- Fecha y hora de la última modificación
);

-- -----------------------------------------------------------------------------
-- TABLA: loan
-- Registra los préstamos de ejemplares a estudiantes.
-- Cada préstamo vincula un estudiante con un ejemplar físico de un libro.
-- -----------------------------------------------------------------------------
CREATE TABLE loan (
    id             BIGSERIAL PRIMARY KEY,                                  -- Identificador único del préstamo
    user_id        BIGINT    NOT NULL REFERENCES users(id),                -- Estudiante que solicitó el préstamo
    book_copy_id   BIGINT    NOT NULL REFERENCES book_copy(id),            -- Ejemplar físico prestado al estudiante
    loan_status_id BIGINT    NOT NULL REFERENCES loan_status(id),          -- Estado actual del préstamo
    loan_date      DATE      NOT NULL DEFAULT CURRENT_DATE,                -- Fecha en que se realizó el préstamo
    due_date       DATE      NOT NULL,                                     -- Fecha límite para devolver el ejemplar
    return_date    DATE,                                                   -- Fecha real en que el estudiante devolvió el ejemplar (NULL si aún no devuelve)
    renewal_count  INT       NOT NULL DEFAULT 0,                           -- Cantidad de veces que el estudiante renovó el préstamo (máximo 3)
    observations   TEXT,                                                   -- Notas adicionales sobre el préstamo (ej: daños al devolver)
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,           -- Fecha y hora de creación del registro
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP            -- Fecha y hora de la última modificación
);

CREATE INDEX idx_loan_user_id        ON loan(user_id);         -- Índice para consultar préstamos por estudiante
CREATE INDEX idx_loan_book_copy_id   ON loan(book_copy_id);    -- Índice para consultar préstamos por ejemplar
CREATE INDEX idx_loan_loan_status_id ON loan(loan_status_id);  -- Índice para filtrar préstamos por estado
