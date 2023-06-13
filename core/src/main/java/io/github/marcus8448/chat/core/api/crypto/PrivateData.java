package io.github.marcus8448.chat.core.api.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public interface PrivateData<E> {
    E encrypt(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException;
}
