package com.thoughtworks.martdhis2sync.exception;
public class PsiException extends RuntimeException{
    public PsiException() {
        super();
    }

    public PsiException(String message, Throwable cause) {
        super(message, cause);
    }

    public PsiException(String message) {
        super(message);
    }

    public PsiException(Throwable cause) {
        super(cause);
    }
}
