package org.oar.gymlog.manager.process

import org.oar.gymlog.manager.custom.DefinitionConstants.A
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.HTMLBodyBlock
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

object Download {
    fun download(content: String) {
        val blob = Blob(arrayOf(content), BlobPropertyBag(type = "application/json"))
        val url = URL.createObjectURL(blob)

        val a = A {
            element.apply {
                href = url
                download = "output.json"
            }
        }

        HTMLBodyBlock.apply {
            +a
            a.element.click()
            -a
        }

        URL.revokeObjectURL(url)
    }
}
