package io.poyarzun.transactions

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import org.jsoup.Jsoup
import org.springframework.core.io.ClassPathResource
import java.nio.charset.Charset
import java.time.Instant
import java.util.*


object GmailQuickstart {

    val APPLICATION_NAME = "Gmail API Java Quickstart"
    val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    val TOKENS_DIRECTORY_PATH = "tokens"

    val SCOPES = listOf(GmailScopes.GMAIL_READONLY)
    val CREDENTIALS_FILE_PATH = "credentials.json"


    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     */
    fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val credentialFile = ClassPathResource(CREDENTIALS_FILE_PATH)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, credentialFile.inputStream.reader())

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    fun getVerificationCode(): String {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Gmail.Builder(
            HTTP_TRANSPORT,
            GmailQuickstart.JSON_FACTORY,
            GmailQuickstart.getCredentials(HTTP_TRANSPORT)
        )
            .setApplicationName(GmailQuickstart.APPLICATION_NAME)
            .build()

        var code = getCode(service)
        while (code == null) {
            Thread.sleep(10000)
            code = getCode(service)
        }
        return code
    }

    fun getCode(service: Gmail): String? {
        // Print the labels in the user's account.
        val user = "me"
        val listRequest = service.users().messages().list(user)
        listRequest.q = "\"Verification code:\" AND Mint"
        val listResponse = listRequest.execute()
        val fiveMinutesAgo = Instant.now().plusSeconds(-60)
        listResponse.messages.forEach {
            val message = service.users().messages().get(user, it.id).execute()
            val date = Date(message.internalDate)
            if (date.toInstant().isBefore(fiveMinutesAgo)) {
                println("Too old: $date")
                return@forEach
            }

            val data = message.payload.body.data
            val body = Base64.getUrlDecoder().decode(data).toString(Charset.defaultCharset())

            println("--------------")
            println(body)

            val document = Jsoup.parse(body)
            val candidateElements = document.getElementsMatchingText("Verification code:\\s+\\d{6}")
            val smallestElement = candidateElements.minBy { it.text().length }
            if (smallestElement == null) {
                println("No elements in the message matched")
                return@forEach
            }

            println("${candidateElements.size}")
            println(smallestElement.text())

            val regex = Regex("Verification code:\\s+(\\d{6})")
            val matchResult = regex.find(smallestElement.text())
            if (matchResult == null) {
                println("No regex match")
                return@forEach
            }

            val (code) = matchResult.destructured

            return code
        }
        return null
    }
}
