package io.poyarzun.transactions

import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import java.net.URI
import java.util.concurrent.TimeUnit

const val MINT_ROOT_URL = "https://mint.intuit.com"
const val MINT_ACCOUNTS_URL = "https://accounts.intuit.com"

@Component
class Mint(val restTemplate: RestTemplate) : AutoCloseable {
    override fun close() {
        driver.quit()
    }

    @Value("\${mint.email}")
    lateinit var email: String
    @Value("\${mint.password}")
    lateinit var password: String

    val logger: Logger = LoggerFactory.getLogger(javaClass)
    val driver = ChromeDriver()

    fun login() {
        logger.info("Loading mint.com")
        driver.get("https://www.mint.com")
        driver.wait(20)
        logger.info("Filling in login details")
        driver.findElementByLinkText("Log In").click()
        driver.findElementById("ius-userid").sendKeys(email)
        driver.findElementById("ius-password").sendKeys(password)
        logger.info("Submitting login form")
        driver.findElementById("ius-sign-in-submit-btn").submit()

        // TODO: pick the email option and submit

//        val code = GmailQuickstart.getVerificationCode()
//        driver.findElementById("ius-code???").sendKeys(code)
//        driver.findElementById("ius-code-submit-btn??").submit()


        while (!driver.currentUrl.startsWith("https://mint.intuit.com/overview.event")) {
            logger.info("Waiting for page to redirect after login")
            logger.info("Current Url is ${driver.currentUrl}")
            Thread.sleep(1000)
        }

        driver.wait(20)
        driver.findElementById("transaction")
    }

    fun getTransactionsCSV(): InputStream {
        val result = requestAndCheck(
            "$MINT_ROOT_URL/transactionDownload.event?accountId=0",
            expectedContentType = "text/csv"
        )
        return result
    }

    private fun requestAndCheck(url: String, method: String = "GET", expectedContentType: String): InputStream {
        val result = driver.request(restTemplate, url)
        if (result.statusCode != HttpStatus.OK) {
            throw IllegalStateException("Non-200 status code: ${result.statusCode}")
        }
        if (!MediaType.parseMediaType("text/csv").isCompatibleWith(result.headers.contentType)) {
            throw IllegalStateException("Unexpected content type: ${result.headers.contentType}")
        }

        return result.body
    }

    private fun ChromeDriver.wait(seconds: Long) {
        manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS)
    }

    private fun ChromeDriver.request(restTemplate: RestTemplate, url: String): ClientHttpResponse {
        val cookies = manage().cookies
        val req = restTemplate.requestFactory.createRequest(URI(url), HttpMethod.GET)
        val cookieString = cookies.joinToString(";") { "${it.name}=${it.value}" }
        logger.info("Making request to $url with cookies: $cookieString")
        req.headers["Cookie"] = cookieString
        return req.execute()
    }
}

