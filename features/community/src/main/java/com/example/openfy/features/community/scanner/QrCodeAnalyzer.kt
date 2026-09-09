package com.example.openfy.features.community.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
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
            val buffer: ByteBuffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = imageProxy.width
            val height = imageProxy.height

            // Crop to center 60% for high speed and energy efficiency
            val cropWidth = (width * 0.7f).toInt()
            val cropHeight = (height * 0.7f).toInt()
            val left = (width - cropWidth) / 2
            val top = (height - cropHeight) / 2

            val source = PlanarYUVLuminanceSource(
                data, width, height,
                left, top, cropWidth, cropHeight,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = try {
                reader.decodeWithState(binaryBitmap)
            } catch (_: Exception) {
                // If cropped failed, try full frame
                try {
                    val fullSource = PlanarYUVLuminanceSource(
                        data, width, height,
                        0, 0, width, height,
                        false
                    )
                    reader.decodeWithState(BinaryBitmap(HybridBinarizer(fullSource)))
                } catch (_: Exception) {
                    null
                }
            } finally {
                reader.reset()
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
}
