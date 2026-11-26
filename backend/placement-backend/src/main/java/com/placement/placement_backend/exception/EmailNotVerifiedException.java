package com.placement.placement_backend.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Verify your email using the link we sent before signing in.");
    }
}
