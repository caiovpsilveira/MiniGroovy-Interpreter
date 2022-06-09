package interpreter.value;

public abstract class Value<T> {

  protected Value() {
  }

  public abstract T value();
  public abstract boolean eval();
  public abstract boolean equals(Object obj);
}
