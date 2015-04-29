package com.github.sviperll.adt4j.examples;

public abstract class BaseOptionalSupport<T> {

  //  Provide an abstract accept method which will be 'implemented' by the value object.
  abstract <R, E extends Exception> R accept(BaseOptionalVisitor<T,R,E> visitor) throws E;

  // Implement the flatMap operation for our BaseOptional value, without having to double wrap on top of the class.
  public <U> BaseOptional<U> flatMap(final Function<T, BaseOptional<U>> function) {
    return accept(new BaseOptionalVisitor<T, BaseOptional<U>, RuntimeException>() {
      @Override
      public BaseOptional<U> missing() {
        return BaseOptional.missing();
      }

      @Override
      public BaseOptional<U> present(T value) {
        return function.apply(value);
      }
    });
  }

  // Implement the map operation for our BaseOptional value, without having to double wrap on top of the class.
  public <U> BaseOptional<U> map(final Function<T, U> function) {
    return accept(new BaseOptionalVisitor<T, BaseOptional<U>, RuntimeException>() {
      @Override
      public BaseOptional<U> missing() {
        return BaseOptional.missing();
      }

      @Override
      public BaseOptional<U> present(T value) {
        return BaseOptional.present(function.apply(value));
      }
    });
  }

  public String toString(BaseOptional<String> optional) {
    return optional.accept(new BaseOptionalVisitor<String, String, RuntimeException>() {
      @Override
      public String missing() throws RuntimeException {
        return "BaseOptional.missing()";
      }

      @Override
      public String present(String value) throws RuntimeException {
        return "BaseOptional.present(\"" + value + "\")";
      }
    });
  }


}
