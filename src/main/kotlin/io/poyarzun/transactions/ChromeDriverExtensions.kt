package io.poyarzun.transactions

import org.openqa.selenium.chrome.ChromeDriver
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.concurrent.TimeUnit


fun ChromeDriver.setWait(wait: Long) {
    manage().timeouts().implicitlyWait(wait, TimeUnit.SECONDS)
}

fun ChromeDriver.request(restTemplate: RestTemplate, url: String): ClientHttpResponse {
    val cookies = manage().cookies
    val req = restTemplate.requestFactory.createRequest(URI(url), HttpMethod.GET)
    val cookieString = cookies.joinToString(";") { "${it.name}=${it.value}" }
    req.headers["Cookie"] = cookieString
    return req.execute()
}
