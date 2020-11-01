package main.exception;

public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String msg, Exception e) {
    super(msg + " because of " + e.toString());
  }

  public UnauthorizedException() {
  }
}