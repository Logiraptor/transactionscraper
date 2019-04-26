package io.poyarzun.transactions

import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.InputStream


@Component
class MintService(val restTemplate: RestTemplate) {
    private val mintRootUrl = "https://mint.intuit.com"

    @Value("\${mint.email}")
    lateinit var email: String

    @Value("\${mint.password}")
    lateinit var password: String

    fun begin(): Executor = Executor(this)

    class Executor(private val mintService: MintService) : AutoCloseable {
        val logger: Logger = LoggerFactory.getLogger(javaClass)

        private val driver = ChromeDriver()

        override fun close() {
            driver.quit()
        }

        fun login() {
            with(mintService) {
                logger.info("Loading mint.com")
                driver.get("https://www.mint.com")
                driver.setWait(20)
                fillInLoginForm(this)

                while (!driver.currentUrl.startsWith("https://mint.intuit.com/overview.event")) {
                    logger.info("Waiting for page to redirect after login")
                    logger.info("Current Url is ${driver.currentUrl}")

                    // look for "Receive code by email" and select it
                    val multiFactorEmailOption = driver.findElementsById("ius-mfa-option-email")
                    if (multiFactorEmailOption.size != 0) {
                        handle2FA(multiFactorEmailOption)
                    }

                    Thread.sleep(1000)
                }

                driver.setWait(20)
                driver.findElementById("transaction")
            }
        }

        private fun fillInLoginForm(mintService: MintService) {
            logger.info("Filling in login details")
            driver.findElementByLinkText("Log In").click()
            driver.findElementById("ius-userid").sendKeys(mintService.email)
            driver.findElementById("ius-password").sendKeys(mintService.password)
            logger.info("Submitting login form")
            driver.findElementById("ius-sign-in-submit-btn").submit()
        }

        private fun handle2FA(multiFactorEmailOption: MutableList<WebElement>) {
            multiFactorEmailOption.first().click()
            // Then click "Continue"
            driver.findElementById("ius-mfa-options-submit-btn").click()

            val code = GmailService.getVerificationCode()
            logger.trace("Got the code: $code")
            driver.findElementById("ius-mfa-confirm-code").sendKeys(code)
            driver.findElementById("ius-mfa-otp-submit-btn").submit()
        }

        fun getTransactions(): List<Transaction> =
            Csv.parse(requestAndCheck("${mintService.mintRootUrl}/transactionDownload.event?accountId=0"))

        private fun requestAndCheck(url: String): InputStream = with(mintService) {
            with(driver.request(restTemplate, url)) {
                when {
                    statusCode.isError -> throw IllegalStateException("Non-200 status code: $statusCode")
                    !MediaType.parseMediaType("text/csv").isCompatibleWith(headers.contentType) ->
                        throw IllegalStateException("Unexpected content type: ${headers.contentType}")
                    else -> return body
                }
            }
        }
    }
}

