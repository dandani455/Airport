package airport.core.controllers.utils;

public class Response<T> {
    
    private String message;
    private int status;
    private T object;

    public Response(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public Response(int status, String message) {
        this.message = message;
        this.status = status;
    }

    public Response(String message, int status, T object) {
        this.message = message;
        this.status = status;
        this.object = object;
    }

    public Response(int status, String message, T object) {
        this.message = message;
        this.status = status;
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public T getObject() {
        return object;
    }
    
}
