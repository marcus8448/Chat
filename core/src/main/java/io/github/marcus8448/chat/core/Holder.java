package io.github.marcus8448.chat.core;

public class Holder<T> {
    private T value;
    
    public Holder() {}
    
    public Holder(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
