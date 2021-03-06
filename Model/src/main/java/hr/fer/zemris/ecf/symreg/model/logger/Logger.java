package hr.fer.zemris.ecf.symreg.model.logger;

/**
 * Defines a way of logging errors.
 *
 * @author Domagoj Stanković
 * @version 1.0
 */
public interface Logger {

  /**
   * Logs given message.
   *
   * @param message Message to be logged
   */
  void log(String message);

  /**
   * Logs exception message and stack trace.
   *
   * @param e Exception to be logged
   */
  void log(Exception e);

}
