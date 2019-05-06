/*
Dylan Veraart
3/23/2019
InvalidInputException.java
This exception is used for handling invalid file syntax and although it's beyond
the scope of the project is very useful for debugging.
 */
public class InvalidInputException extends Exception {

  public InvalidInputException() {
    super();
  }

  public InvalidInputException(String s) {
    super(s);
  }

}
