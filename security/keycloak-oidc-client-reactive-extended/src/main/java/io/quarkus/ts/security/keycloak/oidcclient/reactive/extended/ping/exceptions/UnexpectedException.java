package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.exceptions;

public class UnexpectedException extends CustomException {
    public static final int UNIQUE_SERVICE_ERROR_ID = 0;

    private static final long serialVersionUID = 4442033229110468176L;

    public UnexpectedException(String msg) {
        super(msg, UNIQUE_SERVICE_ERROR_ID);
    }

}
