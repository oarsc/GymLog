package org.scp.gymlog.ui.main.routines.placeholder

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {
	/**
	 * An array of sample (placeholder) items.
	 */
	val ITEMS: MutableList<PlaceholderItem> = ArrayList()
	private const val COUNT = 25

	init {
		// Add some sample items.
		for (i in 1..COUNT) {
			ITEMS.add(createPlaceholderItem(i))
		}
	}

	private fun createPlaceholderItem(position: Int): PlaceholderItem {
		return PlaceholderItem(position.toString(), "Item $position", makeDetails(position))
	}

	private fun makeDetails(position: Int): String {
		val builder = StringBuilder()
		builder.append("Details about Item: ").append(position)
		for (i in 0 until position) {
			builder.append("\nMore details information here.")
		}
		return builder.toString()
	}

	/**
	 * A placeholder item representing a piece of content.
	 */
	class PlaceholderItem(id: String, val content: String, details: String) {
		override fun toString(): String {
			return content
		}
	}
}