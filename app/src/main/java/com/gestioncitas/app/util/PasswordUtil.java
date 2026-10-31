package com.gestioncitas.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Gestión de contraseñas con SHA-256 y sal aleatoria por usuario.
 *
 * Las contraseñas no se guardan en texto plano: se almacenan como
 * "salHex(32):hashHex(64)", donde el hash es SHA-256(sal + password).
 */
public final class PasswordUtil {

    /** Tamaño de la sal aleatoria en bytes (se guardan como 32 caracteres hex). */
    private static final int SALT_BYTES = 16;

    private PasswordUtil() {}

    /** Genera "sal:hash" para la contraseña indicada. */
    public static String hash(String password) {
        try {
            byte[] sal = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(sal);
            String salHex = aHex(sal);
            return salHex + ":" + aHex(digest(salHex, password));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Indica si la contraseña coincide con el "sal:hash" almacenado. */
    public static boolean verificar(String password, String almacenado) {
        if (password == null || almacenado == null || !esHash(almacenado)) {
            return false;
        }
        String salHex = almacenado.substring(0, almacenado.indexOf(':'));
        try {
            byte[] esperado = deHex(almacenado.substring(almacenado.indexOf(':') + 1));
            byte[] actual = digest(salHex, password);
            // Comparación en tiempo constante (evita filtrar por dónde falla)
            return MessageDigest.isEqual(esperado, actual);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Detecta si un valor ya está en el formato "salHex:hashHex". */
    public static boolean esHash(String valor) {
        if (valor == null) {
            return false;
        }
        int posicion = valor.indexOf(':');
        if (posicion != SALT_BYTES * 2 || valor.length() != SALT_BYTES * 2 + 1 + 64) {
            return false;
        }
        return esHex(valor.substring(0, posicion)) && esHex(valor.substring(posicion + 1));
    }

    private static byte[] digest(String salHex, String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salHex.getBytes(StandardCharsets.UTF_8));
        md.update(password.getBytes(StandardCharsets.UTF_8));
        return md.digest();
    }

    private static boolean esHex(String texto) {
        if (texto.isEmpty()) {
            return false;
        }
        for (char c : texto.toCharArray()) {
            if (Character.digit(c, 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String aHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] deHex(String texto) {
        byte[] bytes = new byte[texto.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(texto.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
