/*
 * Copyright (C) 2026 ArtiomITPROGRAMING
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.openfy.features.community.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
            put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
        }
        setHints(hints)
    }

    private var isScanning = true

    fun pauseScanning() {
        isScanning = false
    }

    fun resumeScanning() {
        isScanning = true
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        try {
            val yPlane = imageProxy.planes[0]
            val yBuffer = yPlane.buffer
            val rowStride = yPlane.rowStride
            val width = imageProxy.width
            val height = imageProxy.height
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            // 1. Extract tightly-packed Y-plane, handling hardware rowStride padding
            val yData = ByteArray(width * height)
            if (rowStride == width) {
                yBuffer.position(0)
                yBuffer.get(yData)
            } else {
                for (row in 0 until height) {
                    yBuffer.position(row * rowStride)
                    yBuffer.get(yData, row * width, width)
                }
            }

            // 2. Rotate according to camera sensor orientation
            val (frameData, frameWidth, frameHeight) = when (rotationDegrees) {
                90 -> Triple(rotateYuv90(yData, width, height), height, width)
                180 -> Triple(rotateYuv180(yData, width, height), width, height)
                270 -> Triple(rotateYuv270(yData, width, height), height, width)
                else -> Triple(yData, width, height)
            }

            // 3. Center crop to 75% for high speed and viewfinder match
            val cropWidth = (frameWidth * 0.75f).toInt()
            val cropHeight = (frameHeight * 0.75f).toInt()
            val cropLeft = (frameWidth - cropWidth) / 2
            val cropTop = (frameHeight - cropHeight) / 2

            val source = PlanarYUVLuminanceSource(
                frameData, frameWidth, frameHeight,
                cropLeft, cropTop, cropWidth, cropHeight,
                false
            )

            // 4. Try decoding with HybridBinarizer first, then GlobalHistogramBinarizer
            var result = try {
                reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
            } catch (_: Exception) {
                null
            } finally {
                reader.reset()
            }

            if (result == null) {
                result = try {
                    reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
                } catch (_: Exception) {
                    null
                } finally {
                    reader.reset()
                }
            }

            // If cropped fails, try full frame
            if (result == null) {
                val fullSource = PlanarYUVLuminanceSource(
                    frameData, frameWidth, frameHeight,
                    0, 0, frameWidth, frameHeight,
                    false
                )
                result = try {
                    reader.decodeWithState(BinaryBitmap(HybridBinarizer(fullSource)))
                } catch (_: Exception) {
                    try {
                        reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(fullSource)))
                    } catch (_: Exception) {
                        null
                    }
                } finally {
                    reader.reset()
                }
            }

            if (result != null && result.text.isNotBlank()) {
                isScanning = false
                onQrCodeScanned(result.text)
            }
        } catch (_: Exception) {
            // Ignore frame decode exceptions
        } finally {
            imageProxy.close()
        }
    }

    private fun rotateYuv90(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)
        var i = 0
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                rotated[i++] = data[y * width + x]
            }
        }
        return rotated
    }

    private fun rotateYuv180(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)
        val last = data.size - 1
        for (i in 0 until data.size) {
            rotated[i] = data[last - i]
        }
        return rotated
    }

    private fun rotateYuv270(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)
        var i = 0
        for (x in width - 1 downTo 0) {
            for (y in 0 until height) {
                rotated[i++] = data[y * width + x]
            }
        }
        return rotated
    }
}
