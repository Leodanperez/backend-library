import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.nio.file.*;

public class GenKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        String priv = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(pair.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----";

        String pub = "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(pair.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";

        Files.writeString(Path.of("src/main/resources/keys/private.pem"), priv);
        Files.writeString(Path.of("src/main/resources/keys/public.pem"), pub);
        System.out.println("Keys generated successfully");
    }
}
