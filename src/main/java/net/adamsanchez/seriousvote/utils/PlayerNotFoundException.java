package net.adamsanchez.seriousvote.utils;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
