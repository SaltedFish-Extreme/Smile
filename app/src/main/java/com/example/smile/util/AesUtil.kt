package com.example.smile.util

import android.annotation.SuppressLint
import android.os.Build
import com.example.smile.app.AppConfig.AesParsingKey
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * AES解析工具类
 *
 * @return 解析后的链接地址
 */
@SuppressLint("GetInstance")
fun String.decrypt(): String {
    val raw = AesParsingKey.toByteArray()
    val secretKeySpec = SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    val encrypted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getDecoder().decode(replace("ftp://", "").trim())
    } else {
        return ""
    }
    val original = cipher.doFinal(encrypted)
    return String(original)
}