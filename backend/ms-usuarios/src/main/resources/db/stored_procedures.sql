-- Stored Procedure: buscar_usuario_por_email
-- Busca un usuario por su correo electronico.
-- Implementado para cumplir con el requisito de JPA + Stored Procedures.
-- Usado en: UsuarioRepository.buscarPorEmailSP(email)

CREATE OR REPLACE PROCEDURE buscar_usuario_por_email(
    p_email VARCHAR,
    INOUT result REFCURSOR DEFAULT 'result'
)
LANGUAGE plpgsql
AS $$
BEGIN
    OPEN result FOR
        SELECT id, nombre, email, password
        FROM usuarios
        WHERE email = p_email;
END;
$$;
