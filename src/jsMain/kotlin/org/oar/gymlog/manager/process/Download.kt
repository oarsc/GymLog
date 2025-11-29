package org.oar.gymlog.manager.process

import org.oar.gymlog.manager.custom.DefinitionConstants.A
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.HTMLBodyBlock
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

object Download {
    fun download(content: String) {
        val blob = Blob(arrayOf(content), BlobPropertyBag(type = "application/json"))
        val url = URL.createObjectURL(blob)

        val a = createBlock(A)
        a.element.apply {
            href = url
            download = "output.json"
        }

        HTMLBodyBlock.append(a)
        a.element.click()
        HTMLBodyBlock.remove(a)

        URL.revokeObjectURL(url)
    }
}
