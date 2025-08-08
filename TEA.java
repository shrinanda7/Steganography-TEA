import java.util.Arrays;

public class TEA {

    private static final int DELTA = 0x9E3779B9;
    private static final int ROUNDS = 32;

    /**
     * Encrypts plaintext with the TEA algorithm and PKCS7 padding.
     * @param plaintext Original bytes
     * @param key 16-byte key
     * @return Encrypted bytes
     */
    public static byte[] encryptWithPadding(byte[] plaintext, byte[] key) {
        int[] k = formatKey(key);
        byte[] padded = addPKCS7Padding(plaintext);

        int[] blocks = new int[padded.length / 4];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = bytesToInt(padded, i * 4);
        }

        for (int i = 0; i < blocks.length; i += 2) {
            encryptBlock(blocks, i, k);
        }

        byte[] encrypted = new byte[padded.length];
        for (int i = 0; i < blocks.length; i++) {
            intToBytes(blocks[i], encrypted, i * 4);
        }
        return encrypted;
    }

    /**
     * Decrypts TEA encrypted bytes and removes PKCS7 padding.
     * @param ciphertext Encrypted bytes
     * @param key 16-byte key
     * @return Decrypted bytes without padding
     */
    public static byte[] decryptWithPadding(byte[] ciphertext, byte[] key) {
        if (ciphertext.length % 8 != 0) {
            throw new IllegalArgumentException("Ciphertext length must be multiple of 8");
        }
        int[] k = formatKey(key);

        int[] blocks = new int[ciphertext.length / 4];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = bytesToInt(ciphertext, i * 4);
        }

        for (int i = 0; i < blocks.length; i += 2) {
            decryptBlock(blocks, i, k);
        }

        byte[] decrypted = new byte[ciphertext.length];
        for (int i = 0; i < blocks.length; i++) {
            intToBytes(blocks[i], decrypted, i * 4);
        }

        return removePKCS7Padding(decrypted);
    }

    // PKCS7 padding: append N bytes of value N, where N = bytes to add
    private static byte[] addPKCS7Padding(byte[] data) {
        int paddingLength = 8 - (data.length % 8);
        byte[] padded = Arrays.copyOf(data, data.length + paddingLength);
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }
        return padded;
    }

    // Remove PKCS7 padding
    private static byte[] removePKCS7Padding(byte[] data) {
        int paddingValue = data[data.length - 1] & 0xFF;
        if (paddingValue < 1 || paddingValue > 8) {
            throw new IllegalArgumentException("Invalid padding");
        }
        for (int i = data.length - paddingValue; i < data.length; i++) {
            if ((data[i] & 0xFF) != paddingValue) {
                throw new IllegalArgumentException("Invalid padding");
            }
        }
        return Arrays.copyOf(data, data.length - paddingValue);
    }

    private static void encryptBlock(int[] blocks, int offset, int[] k) {
        int v0 = blocks[offset], v1 = blocks[offset + 1], sum = 0;
        for (int i = 0; i < ROUNDS; i++) {
            sum += DELTA;
            v0 += ((v1 << 4) + k[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + k[1]);
            v1 += ((v0 << 4) + k[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + k[3]);
        }
        blocks[offset] = v0;
        blocks[offset + 1] = v1;
    }

    private static void decryptBlock(int[] blocks, int offset, int[] k) {
        int v0 = blocks[offset], v1 = blocks[offset + 1];
        int sum = DELTA * ROUNDS;
        for (int i = 0; i < ROUNDS; i++) {
            v1 -= ((v0 << 4) + k[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + k[3]);
            v0 -= ((v1 << 4) + k[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + k[1]);
            sum -= DELTA;
        }
        blocks[offset] = v0;
        blocks[offset + 1] = v1;
    }

    private static int[] formatKey(byte[] key) {
        if (key.length < 16) throw new IllegalArgumentException("Key must be 16 bytes");
        int[] k = new int[4];
        for (int i = 0; i < 4; i++) {
            k[i] = bytesToInt(key, i * 4);
        }
        return k;
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    private static void intToBytes(int val, byte[] dest, int destOffset) {
        dest[destOffset] = (byte) (val >>> 24);
        dest[destOffset + 1] = (byte) (val >>> 16);
        dest[destOffset + 2] = (byte) (val >>> 8);
        dest[destOffset + 3] = (byte) val;
    }
}
