package com.charmflex.app.flexiexpensesmanager.core.crypto

import org.apache.catalina.webresources.FileResource
import org.apache.tomcat.util.file.ConfigurationSource.Resource
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

@Service
class SignatureService(
    @Value("\${signature.private-key}")
    privateKeyPemPath: String,
) {
    private var privateKey: PrivateKey = loadPrivateKey(privateKeyPemPath)

    /**
     * Load RSA private key from PEM (PKCS#8 format).
     */
    private fun loadPrivateKey(path: String): PrivateKey {
        val pemResource = FileSystemResource(path)
        val pem = pemResource.inputStream.bufferedReader().use(BufferedReader::readText)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(pem)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)

        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    /**
     * Sign payload with SHA256withRSA and return Base64 signature.
     */
    fun sign(payload: String): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(payload.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(signature.sign())
    }
}