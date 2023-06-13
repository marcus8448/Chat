package io.github.marcus8448.chat.core.api.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.spec.InvalidKeySpecException;

public interface Encrypted<D> {
    D decrypt(Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException;
}
