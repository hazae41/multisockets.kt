package hazae41.sockets

import io.ktor.http.cio.websocket.WebSocketSession
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import io.ktor.http.cio.websocket.send

object B64 {
    fun to(data: ByteArray) = Base64.getEncoder().encodeToString(data)
    fun from(data: String) = Base64.getDecoder().decode(data)
}

object RSA {

    fun generate() = KeyPairGenerator.getInstance("RSA").generateKeyPair()

    fun encrypt(data: String, key: PublicKey) =
        Cipher.getInstance("RSA").run {
            init(Cipher.ENCRYPT_MODE, key)
            B64.to(doFinal(data.toByteArray()))
        }

    fun decrypt(data: String, key: PrivateKey) =
        Cipher.getInstance("RSA").run {
            init(Cipher.DECRYPT_MODE, key)
            String(doFinal(B64.from(data)))
        }

    fun PrivateKey(key64: String) = B64.from(key64).run {
        KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(this))
            .also{Arrays.fill(this, 0.toByte())}
    }

    fun PublicKey(key64: String) = B64.from(key64).run {
        KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(this))
    }

    fun save(key: PrivateKey) = KeyFactory.getInstance("RSA")
        .getKeySpec(key, PKCS8EncodedKeySpec::class.java)
        .encoded.run{
            B64.to(this).also{Arrays.fill(this, 0.toByte())
        }
    }

    fun save(key: PublicKey) = KeyFactory.getInstance("RSA")
        .getKeySpec(key, X509EncodedKeySpec::class.java)
        .let { B64.to(it.encoded) }
}

object AES {
    fun generate() = KeyGenerator.getInstance("AES").run {
        init(128)
        generateKey()
    }

    fun encrypt(data: String, key: SecretKey) =
        Cipher.getInstance("AES").run {
            init(Cipher.ENCRYPT_MODE, key)
            B64.to(doFinal(data.toByteArray()))
        }

    fun decrypt(data: String, key: SecretKey) =
        Cipher.getInstance("AES").run {
            init(Cipher.DECRYPT_MODE, key)
            String(doFinal(B64.from(data)))
        }

    fun toKey(key: String) = B64.from(key).run {
        SecretKeySpec(this, 0, size, "AES")
    }

    fun toString(key: SecretKey) = B64.to(key.encoded)
}