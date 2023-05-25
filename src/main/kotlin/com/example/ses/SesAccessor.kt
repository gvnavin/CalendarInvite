package com.example.ses

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.mail.internet.MimeMessage
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.RawMessage
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest

object SesAccessor {

    val client = SesClient.builder()
        .region(Region.AP_SOUTH_1)
//        need to uncomment while testing, below line should not be checked in to repo
        .credentialsProvider(ProfileCredentialsProvider.create("dev"))
        .build()

    fun sendEmail(mimeMessage: MimeMessage) {

        val outputStream = ByteArrayOutputStream()
        mimeMessage.writeTo(outputStream)

        val buf = ByteBuffer.wrap(outputStream.toByteArray())

        val arr = ByteArray(buf.remaining())
        buf[arr]

        val data = SdkBytes.fromByteArray(arr)

        val rawMessage = RawMessage.builder()
            .data(data)
            .build()

        val rawEmailRequest = SendRawEmailRequest.builder()
            .rawMessage(rawMessage)
            .build()

        client.sendRawEmail(rawEmailRequest)
        client.close()
    }

}