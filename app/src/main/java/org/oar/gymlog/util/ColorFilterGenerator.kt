package org.oar.gymlog.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates a [ColorMatrixColorFilter] to adjust the hue, saturation, brightness, or
 * contrast of an [Bitmap], [Drawable], or [ImageView].
 *
 *
 * Example usage:
 * <br></br>
 * `imageView.setColorFilter(ColorFilterGenerator.from(Color.BLUE).to(Color.RED));`
 *
 * @author Jared Rummler <jared.rummler></jared.rummler>@gmail.com>
 */
class ColorFilterGenerator private constructor() {
    // Based off answer from StackOverflow
    // See: https://stackoverflow.com/a/15119089/1048340
    init {
        throw AssertionError()
    }

    // Builder
    // --------------------------------------------------------------------------------------------
    class Builder {
        var hue = 0
        var brightness = 0f
        var saturation = 0f
        fun setHue(hue: Int): Builder {
            this.hue = hue
            return this
        }

        fun setBrightness(brightness: Float): Builder {
            this.brightness = brightness
            return this
        }

        fun setSaturation(saturation: Float): Builder {
            this.saturation = saturation
            return this
        }

        fun build(): ColorFilter {
            val cm = ColorMatrix()
            adjustBrightness(cm, brightness)
            adjustSaturation(cm, saturation)
            adjustHue(cm, hue.toFloat())
            return ColorMatrixColorFilter(cm)
        }
    }

    class From {
        val oldColor: Int

        constructor(bitmap: Bitmap) {
            oldColor = getAverageColor(bitmap)
        }

        constructor(oldColor: Int) {
            this.oldColor = oldColor
        }

        fun to(newColor: Int): ColorFilter {
            val hsvOld = getHsv(oldColor)
            val hsvNew = getHsv(newColor)
            val hue = (0 - hsvOld[0]).toInt()
            val saturation = hsvNew[1] - hsvOld[1]
            val brightness = (hsvNew[2] - hsvOld[2])
            return Builder()
                .setHue(hue)
                //.setSaturation(saturation)
                .setBrightness(brightness)
                .build()
        }
    }

    companion object {
        fun from(drawable: Drawable): From {
            return From(drawableToBitmap(drawable))
        }

        fun from(bitmap: Bitmap): From {
            return From(bitmap)
        }

        fun from(color: Int): From {
            return From(color)
        }

        // --------------------------------------------------------------------------------------------
        private val DELTA_INDEX = doubleArrayOf(
            0.0,
            0.01,
            0.02,
            0.04,
            0.05,
            0.06,
            0.07,
            0.08,
            0.1,
            0.11,
            0.12,
            0.14,
            0.15,
            0.16,
            0.17,
            0.18,
            0.20,
            0.21,
            0.22,
            0.24,
            0.25,
            0.27,
            0.28,
            0.30,
            0.32,
            0.34,
            0.36,
            0.38,
            0.40,
            0.42,
            0.44,
            0.46,
            0.48,
            0.5,
            0.53,
            0.56,
            0.59,
            0.62,
            0.65,
            0.68,
            0.71,
            0.74,
            0.77,
            0.80,
            0.83,
            0.86,
            0.89,
            0.92,
            0.95,
            0.98,
            1.0,
            1.06,
            1.12,
            1.18,
            1.24,
            1.30,
            1.36,
            1.42,
            1.48,
            1.54,
            1.60,
            1.66,
            1.72,
            1.78,
            1.84,
            1.90,
            1.96,
            2.0,
            2.12,
            2.25,
            2.37,
            2.50,
            2.62,
            2.75,
            2.87,
            3.0,
            3.2,
            3.4,
            3.6,
            3.8,
            4.0,
            4.3,
            4.7,
            4.9,
            5.0,
            5.5,
            6.0,
            6.5,
            6.8,
            7.0,
            7.3,
            7.5,
            7.8,
            8.0,
            8.4,
            8.7,
            9.0,
            9.4,
            9.6,
            9.8,
            10.0
        )

        fun adjustHue(cm: ColorMatrix, value: Float) {
            val limitedHue = value.coerceIn(-180f, 180f) / 180f * Math.PI.toFloat()
            if (limitedHue == 0f) {
                return
            }
            val cosVal = cos(limitedHue)
            val sinVal = sin(limitedHue)
            val lumR = 0.213f
            val lumG = 0.715f
            val lumB = 0.072f
            val mat = floatArrayOf(
                lumR + cosVal * (1 - lumR) + sinVal * -lumR,
                lumG + cosVal * -lumG + sinVal * -lumG,
                lumB + cosVal * -lumB + sinVal * (1 - lumB),
                0f, 0f,

                lumR + cosVal * -lumR + sinVal * 0.143f,
                lumG + cosVal * (1 - lumG) + sinVal * 0.140f,
                lumB + cosVal * -lumB + sinVal * -0.283f,
                0f, 0f,

                lumR + cosVal * -lumR + sinVal * -(1 - lumR),
                lumG + cosVal * -lumG + sinVal * lumG,
                lumB + cosVal * (1 - lumB) + sinVal * lumB,
                0f, 0f,

                0f, 0f, 0f, 1f, 0f,
                0f, 0f, 0f, 0f, 1f
            )
            cm.postConcat(ColorMatrix(mat))
        }

        fun adjustBrightness(cm: ColorMatrix, value: Float) {
            val value = value.coerceIn(-100f, 100f)
            if (value == 0f) {
                return
            }
            val mat = floatArrayOf(
                1f, 0f, 0f, 0f, value,
                0f, 1f, 0f, 0f, value,
                0f, 0f, 1f, 0f, value,
                0f, 0f, 0f, 1f, 0f,
                0f, 0f, 0f, 0f, 1f
            )
            cm.postConcat(ColorMatrix(mat))
        }

        fun adjustSaturation(cm: ColorMatrix, value: Float) {
            var value = value.coerceIn(-100f, 100f)
            if (value == 0f) {
                return
            }
            val x = 1 + if (value > 0) 3 * value / 100 else value / 100
            val lumR = 0.3086f
            val lumG = 0.6094f
            val lumB = 0.0820f
            val mat = floatArrayOf(
                lumR * (1 - x) + x,
                lumG * (1 - x),
                lumB * (1 - x),
                0f, 0f,

                lumR * (1 - x),
                lumG * (1 - x) + x,
                lumB * (1 - x),
                0f, 0f,

                lumR * (1 - x),
                lumG * (1 - x),
                lumB * (1 - x) + x,
                0f, 0f,

                0f, 0f, 0f, 1f, 0f,
                0f, 0f, 0f, 0f, 1f
            )
            cm.postConcat(ColorMatrix(mat))
        }

        // --------------------------------------------------------------------------------------------

        private fun getHsv(color: Int): FloatArray {
            val hsv = FloatArray(3)
            Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv)
            return hsv
        }

        /**
         * Converts a [Drawable] to a [Bitmap]
         *
         * @param drawable
         * The [Drawable] to convert
         * @return The converted [Bitmap].
         */
        private fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            } else if (drawable is PictureDrawable) {
                val pictureDrawable = drawable
                val bitmap = Bitmap.createBitmap(
                    pictureDrawable.intrinsicWidth,
                    pictureDrawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                canvas.drawPicture(pictureDrawable.picture)
                return bitmap
            }
            var width = drawable.intrinsicWidth
            width = if (width > 0) width else 1
            var height = drawable.intrinsicHeight
            height = if (height > 0) height else 1
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        /**
         * Calculate the average red, green, blue color values of a bitmap
         *
         * @param bitmap
         * a [Bitmap]
         * @return
         */
        private fun getAverageColorRGB(bitmap: Bitmap): IntArray {
            val width = bitmap.width
            val height = bitmap.height
            var size = width * height
            val pixels = IntArray(size)
            var r: Int
            var g: Int
            var b: Int
            b = 0
            g = b
            r = g
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            for (i in 0 until size) {
                val pixelColor = pixels[i]
                if (pixelColor == Color.TRANSPARENT) {
                    size--
                    continue
                }
                r += Color.red(pixelColor)
                g += Color.green(pixelColor)
                b += Color.blue(pixelColor)
            }
            r /= size
            g /= size
            b /= size
            return intArrayOf(
                r, g, b
            )
        }

        /**
         * Calculate the average color value of a bitmap
         *
         * @param bitmap
         * a [Bitmap]
         * @return
         */
        private fun getAverageColor(bitmap: Bitmap): Int {
            val rgb = getAverageColorRGB(bitmap)
            return Color.argb(255, rgb[0], rgb[1], rgb[2])
        }
    }
}