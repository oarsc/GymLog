package org.oar.gymlog.manager.utils

import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlin.js.Promise

object Utils {
    fun confirm(text: String) = window.confirm(text)

    fun setTimeout(delay: Int = 0, runnable: () -> Unit) = window.setTimeout(runnable, delay)
    fun clearTimeout(timeoutId: Int) = window.clearTimeout(timeoutId)

    fun setInterval(delay: Int, runnable: () -> Unit) = window.setInterval(runnable, delay)
    fun clearInterval(intervalId: Int) = window.clearInterval(intervalId)

    fun fetchText(url: String, callback: (String) -> Unit) =
        window.fetch(url).then { it.text() }.then(callback)

    fun <T> fetchJson(url: String, callback: (T) -> Unit) =
        window.fetch(url).then { it.json() as Promise<T> }.then(callback)

    val JSON = Json {
        ignoreUnknownKeys = true
    }
}