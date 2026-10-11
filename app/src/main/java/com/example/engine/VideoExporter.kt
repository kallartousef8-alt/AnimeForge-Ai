package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.models.AnimeFilter
import com.example.data.models.AspectRatio
import com.example.data.models.CaptionItem
import com.example.data.models.ExportFps
import com.example.data.models.ExportResolution
import com.example.data.repository.AnimeProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

data class ExportResult(
    val file: File,
    val contentUri: Uri?,
    val durationSeconds: Int,
    val width: Int,
    val height: Int,
    val fileSizeFormatted: String
)

class VideoExporter(private val context: Context) {

    suspend fun exportProjectToMp4(
        project: AnimeProject,
        resolution: ExportResolution,
        aspectRatio: AspectRatio,
        fps: ExportFps,
        onProgress: (Float) -> Unit
    ): Result<ExportResult> = withContext(Dispatchers.IO) {
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        var samplesWritten = 0

        try {
            val rawWidth: Int
            val rawHeight: Int
            when (aspectRatio) {
                AspectRatio.PORTRAIT_9_16 -> {
                    rawHeight = resolution.height
                    rawWidth = (rawHeight * 9f / 16f).toInt()
                }
                AspectRatio.LANDSCAPE_16_9 -> {
                    rawWidth = resolution.height
                    rawHeight = (rawWidth * 9f / 16f).toInt()
                }
                AspectRatio.SQUARE_1_1 -> {
                    rawHeight = resolution.height
                    rawWidth = rawHeight
                }
            }

            // H.264 macroblock alignment: width and height must be divisible by 16
            val width = ((rawWidth / 16) * 16).coerceAtLeast(320)
            val height = ((rawHeight / 16) * 16).coerceAtLeast(320)

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val safeTitle = project.title.replace("[^a-zA-Z0-9_]".toRegex(), "_")
            val outputFile = File(exportDir, "AnimeForge_${safeTitle}_$timeStamp.mp4")

            val frameRate = fps.fps
            val durationMs = project.durationMs.coerceIn(3000L, 60000L)
            val totalFrames = ((durationMs * frameRate) / 1000L).toInt().coerceAtLeast(frameRate)

            val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
            val codec = MediaCodec.createEncoderByType(mimeType)
            encoder = codec

            val caps = codec.codecInfo.getCapabilitiesForType(mimeType)
            val supportedFormats = caps.colorFormats

            val chosenColorFormat = when {
                supportedFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) ->
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
                supportedFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) ->
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
                supportedFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) ->
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
                else -> supportedFormats.firstOrNull() ?: MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            }
            val isPlanar = (chosenColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)

            val bitrate = if (resolution == ExportResolution.RES_1080P) 4_000_000 else 2_500_000
            val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, chosenColorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                try {
                    setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
                    setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31)
                } catch (_: Exception) {}
            }

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            val m = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = m
            var videoTrackIndex = -1

            val bufferInfo = MediaCodec.BufferInfo()
            val frameDurationUs = 1_000_000L / frameRate

            // Reusable Bitmap and Canvas
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val numPixels = width * height
            val argbArray = IntArray(numPixels)
            val yuvArray = ByteArray(numPixels * 3 / 2)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                style = Paint.Style.STROKE
            }

            fun drain(endOfStream: Boolean) {
                while (true) {
                    val outIndex = codec.dequeueOutputBuffer(bufferInfo, if (endOfStream) 12000 else 0)
                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            val newFormat = codec.outputFormat
                            videoTrackIndex = m.addTrack(newFormat)
                            m.start()
                            muxerStarted = true
                        }
                    } else if (outIndex >= 0) {
                        val encodedData = codec.getOutputBuffer(outIndex)
                        if (encodedData != null && muxerStarted) {
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0
                            }
                            if (bufferInfo.size != 0) {
                                encodedData.position(bufferInfo.offset)
                                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                m.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                                samplesWritten++
                            }
                        }
                        val isEos = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                        codec.releaseOutputBuffer(outIndex, false)
                        if (isEos) break
                    }
                }
            }

            for (frameIndex in 0 until totalFrames) {
                val currentPtsUs = frameIndex * frameDurationUs
                val currentMs = currentPtsUs / 1000L
                val isLast = (frameIndex == totalFrames - 1)

                drawAnimeFrame(
                    canvas = canvas,
                    width = width,
                    height = height,
                    currentMs = currentMs,
                    totalDurationMs = durationMs,
                    filter = project.activeFilter,
                    clips = project.clips,
                    captions = project.captions,
                    title = project.title,
                    paint = paint,
                    textPaint = textPaint,
                    strokePaint = strokePaint
                )

                bitmap.getPixels(argbArray, 0, width, 0, 0, width, height)

                if (isPlanar) {
                    encodeYUV420P(yuvArray, argbArray, width, height)
                } else {
                    encodeYUV420SP(yuvArray, argbArray, width, height)
                }

                var queued = false
                var tries = 0
                while (!queued && tries < 60) {
                    val inIndex = codec.dequeueInputBuffer(10000)
                    if (inIndex >= 0) {
                        val inBuffer = codec.getInputBuffer(inIndex)
                        inBuffer?.clear()
                        val bytesToPut = minOf(yuvArray.size, inBuffer?.remaining() ?: 0)
                        inBuffer?.put(yuvArray, 0, bytesToPut)
                        val flags = if (isLast) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                        codec.queueInputBuffer(inIndex, 0, bytesToPut, currentPtsUs, flags)
                        queued = true
                    } else {
                        drain(false)
                        tries++
                    }
                }

                drain(false)

                val progress = (frameIndex + 1).toFloat() / totalFrames
                onProgress(progress * 0.9f)
            }

            // Drain remaining frames until EOS
            var remainingDrains = 40
            while (remainingDrains-- > 0 && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) == 0) {
                drain(true)
            }

            bitmap.recycle()

            if (samplesWritten == 0) {
                throw IllegalStateException("Video encoder produced 0 output samples")
            }

            try {
                codec.stop()
            } catch (_: Exception) {}
            try {
                codec.release()
            } catch (_: Exception) {}
            encoder = null

            if (muxerStarted && videoTrackIndex >= 0 && samplesWritten > 0) {
                try {
                    m.stop()
                } catch (e: Exception) {
                    Log.w("VideoExporter", "Muxer stop warning", e)
                }
            }
            try {
                m.release()
            } catch (_: Exception) {}
            muxer = null

            onProgress(0.96f)

            // Save copy to MediaStore for Android Gallery
            val contentUri = saveToMediaStore(outputFile, project.title)
            onProgress(1.0f)

            val sizeBytes = outputFile.length()
            val sizeFormatted = String.format(Locale.US, "%.1f MB", sizeBytes / (1024f * 1024f))

            Result.success(
                ExportResult(
                    file = outputFile,
                    contentUri = contentUri,
                    durationSeconds = (durationMs / 1000).toInt(),
                    width = width,
                    height = height,
                    fileSizeFormatted = sizeFormatted
                )
            )
        } catch (e: Exception) {
            Log.e("VideoExporter", "Export error", e)
            try {
                encoder?.stop()
            } catch (_: Exception) {}
            try {
                encoder?.release()
            } catch (_: Exception) {}
            if (muxerStarted && samplesWritten > 0) {
                try {
                    muxer?.stop()
                } catch (_: Exception) {}
            }
            try {
                muxer?.release()
            } catch (_: Exception) {}

            Result.failure(e)
        }
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize

        var index = 0
        for (j in 0 until height) {
            val isEven = (j and 1) == 0
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()

                if (isEven && (i and 1) == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv420sp[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                    yuv420sp[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                }
            }
        }
    }

    private fun encodeYUV420P(yuv420p: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        val qFrame = frameSize / 4
        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + qFrame

        var index = 0
        for (j in 0 until height) {
            val isEven = (j and 1) == 0
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv420p[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()

                if (isEven && (i and 1) == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv420p[uIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                    yuv420p[vIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                }
            }
        }
    }

    private fun drawAnimeFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        currentMs: Long,
        totalDurationMs: Long,
        filter: AnimeFilter,
        clips: List<com.example.data.models.VideoClipItem>,
        captions: List<CaptionItem>,
        title: String,
        paint: Paint,
        textPaint: Paint,
        strokePaint: Paint
    ) {
        val t = currentMs / 1000f

        // Locate active clip in timeline
        var accumulatedMs = 0L
        var activeClip: com.example.data.models.VideoClipItem? = null
        var clipLocalMs = 0L
        for (c in clips) {
            val dur = c.trimmedDurationMs
            if (currentMs in accumulatedMs until (accumulatedMs + dur)) {
                activeClip = c
                clipLocalMs = currentMs - accumulatedMs
                break
            }
            accumulatedMs += dur
        }
        if (activeClip == null && clips.isNotEmpty()) {
            activeClip = clips.last()
            clipLocalMs = activeClip.trimmedDurationMs
        }

        // Apply clip effects matrix transforms if present
        val effect = activeClip?.effectType ?: com.example.data.models.AnimeEffectType.NONE
        val intensity = activeClip?.effectIntensity ?: 0.6f
        val effectDur = activeClip?.effectDurationMs ?: 2000L
        val inEffectWindow = clipLocalMs < effectDur

        canvas.save()

        if (inEffectWindow) {
            when (effect) {
                com.example.data.models.AnimeEffectType.SHAKE -> {
                    val shakeX = (sin(t * 40.0) * intensity * 18).toFloat()
                    val shakeY = (kotlin.math.cos(t * 36.0) * intensity * 18).toFloat()
                    canvas.translate(shakeX, shakeY)
                }
                com.example.data.models.AnimeEffectType.ZOOM_IN -> {
                    val progress = (clipLocalMs.toFloat() / effectDur.toFloat()).coerceIn(0f, 1f)
                    val scale = 1.0f + progress * intensity * 0.35f
                    canvas.scale(scale, scale, width / 2f, height / 2f)
                }
                com.example.data.models.AnimeEffectType.ZOOM_OUT -> {
                    val progress = (clipLocalMs.toFloat() / effectDur.toFloat()).coerceIn(0f, 1f)
                    val scale = 1.0f + (1.0f - progress) * intensity * 0.35f
                    canvas.scale(scale, scale, width / 2f, height / 2f)
                }
                com.example.data.models.AnimeEffectType.BEAT_SYNC -> {
                    val pulseScale = 1.0f + (sin(t * 12.0) * 0.5f + 0.5f).toFloat() * intensity * 0.12f
                    canvas.scale(pulseScale, pulseScale, width / 2f, height / 2f)
                }
                else -> {}
            }
        }

        // 1. Dynamic Anime Background with gradient & energy pulse
        val activeFilter = if (activeClip?.filter != null && activeClip.filter != AnimeFilter.NONE) activeClip.filter else filter
        val bgColors = when (activeFilter) {
            AnimeFilter.CYBERPUNK_NEON -> intArrayOf(0xFF0D0221.toInt(), 0xFF0F084B.toInt(), 0xFF26408B.toInt())
            AnimeFilter.SAKURA_DUSK -> intArrayOf(0xFF2B0938.toInt(), 0xFF541348.toInt(), 0xFF87255B.toInt())
            AnimeFilter.MOONLIGHT_SHADOW -> intArrayOf(0xFF030A1C.toInt(), 0xFF061838.toInt(), 0xFF0B2559.toInt())
            AnimeFilter.MANGA_NOIR -> intArrayOf(0xFF000000.toInt(), 0xFF181818.toInt(), 0xFF282828.toInt())
            AnimeFilter.GOLDEN_AURA -> intArrayOf(0xFF1E0E00.toInt(), 0xFF3D1D00.toInt(), 0xFF5E3000.toInt())
            else -> intArrayOf(0xFF0A0B1A.toInt(), 0xFF141838.toInt(), 0xFF231C4D.toInt())
        }

        // Draw background gradient
        val shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            bgColors, null, android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // 2. Anime energy lines / motion speed bars
        val pulse = (sin(t * 8.0) * 0.5f + 0.5f).toFloat()
        val numBars = 12
        for (i in 0 until numBars) {
            val yPos = ((currentMs * 1.5f + i * (height / numBars)) % height).toFloat()
            val alpha = (40 + pulse * 40).toInt()
            paint.color = when (filter) {
                AnimeFilter.CYBERPUNK_NEON -> Color.argb(alpha, 0, 240, 255)
                AnimeFilter.SAKURA_DUSK -> Color.argb(alpha, 255, 105, 180)
                AnimeFilter.GOLDEN_AURA -> Color.argb(alpha, 255, 215, 0)
                AnimeFilter.MANGA_NOIR -> Color.argb(alpha, 255, 255, 255)
                else -> Color.argb(alpha, 168, 85, 247)
            }
            paint.strokeWidth = 3f + (i % 3) * 2f
            canvas.drawLine(0f, yPos, width.toFloat(), yPos, paint)
        }

        // 3. Center Anime Emblem / Focus Visual
        val centerX = width / 2f
        val centerY = height * 0.42f
        val radius = width * 0.28f + (pulse * 12f)

        // Outer glowing ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = when (filter) {
            AnimeFilter.CYBERPUNK_NEON -> Color.argb(180, 0, 240, 255)
            AnimeFilter.SAKURA_DUSK -> Color.argb(180, 255, 46, 147)
            AnimeFilter.GOLDEN_AURA -> Color.argb(180, 255, 214, 0)
            AnimeFilter.MANGA_NOIR -> Color.argb(180, 255, 255, 255)
            else -> Color.argb(180, 168, 85, 247)
        }
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Inner glowing core
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(70, 255, 255, 255)
        canvas.drawCircle(centerX, centerY, radius * 0.75f, paint)

        // Center Action Play symbol / Katana silhouette
        paint.color = Color.WHITE
        val path = android.graphics.Path().apply {
            moveTo(centerX - radius * 0.25f, centerY - radius * 0.35f)
            lineTo(centerX + radius * 0.40f, centerY)
            lineTo(centerX - radius * 0.25f, centerY + radius * 0.35f)
            close()
        }
        canvas.drawPath(path, paint)

        // Title watermark at top
        textPaint.textSize = width * 0.045f
        textPaint.color = Color.argb(200, 255, 255, 255)
        canvas.drawText("ANIMEFORGE AI • $title", centerX, height * 0.08f, textPaint)

        // 4. Burn-in Animated Subtitles
        val activeCaption = captions.find { currentMs in it.startMs..it.endMs }
        if (activeCaption != null && activeCaption.text.isNotBlank()) {
            val capY = height * activeCaption.yPositionRatio
            val capText = if (activeCaption.style.isUppercase) activeCaption.text.uppercase() else activeCaption.text

            val baseTextSize = width * (activeCaption.fontSizeSp / 360f)
            // Punchy scale pop on appearance
            val elapsedInCaption = (currentMs - activeCaption.startMs).toFloat()
            val popScale = if (elapsedInCaption < 180f) {
                1.0f + (1.0f - elapsedInCaption / 180f) * 0.22f
            } else 1.0f

            textPaint.textSize = baseTextSize * popScale
            strokePaint.textSize = baseTextSize * popScale
            strokePaint.strokeWidth = baseTextSize * 0.16f

            // Shadow / Outline
            val outlineColor = try {
                Color.parseColor(activeCaption.style.outlineColorHex)
            } catch (_: Exception) {
                Color.BLACK
            }
            val primaryColor = try {
                Color.parseColor(activeCaption.style.primaryColorHex)
            } catch (_: Exception) {
                Color.YELLOW
            }

            strokePaint.color = outlineColor
            canvas.drawText(capText, centerX, capY, strokePaint)

            textPaint.color = primaryColor
            canvas.drawText(capText, centerX, capY, textPaint)
        }

        // Overlay Anime Effects: FLASH, GLOW, MOTION_BLUR, CINEMATIC_BARS
        if (inEffectWindow) {
            when (effect) {
                com.example.data.models.AnimeEffectType.FLASH -> {
                    val fade = (1.0f - (clipLocalMs.toFloat() / effectDur.toFloat())).coerceIn(0f, 1f)
                    val alpha = (fade * intensity * 240).toInt()
                    if (alpha > 0) {
                        paint.style = Paint.Style.FILL
                        paint.color = Color.argb(alpha, 255, 255, 255)
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                    }
                }
                com.example.data.models.AnimeEffectType.GLOW -> {
                    val glowAlpha = ((sin(t * 16.0) * 0.5f + 0.5f) * intensity * 200).toInt()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = width * 0.05f
                    paint.color = Color.argb(glowAlpha, 0, 240, 255)
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                }
                com.example.data.models.AnimeEffectType.MOTION_BLUR -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f
                    val lineAlpha = (intensity * 180).toInt()
                    paint.color = Color.argb(lineAlpha, 255, 255, 255)
                    for (l in 0 until 16) {
                        val ly = ((currentMs * 3.5f + l * 40f) % height).toFloat()
                        canvas.drawLine(0f, ly, width.toFloat(), ly + 25f, paint)
                    }
                }
                com.example.data.models.AnimeEffectType.CINEMATIC_BARS -> {
                    paint.style = Paint.Style.FILL
                    paint.color = Color.BLACK
                    val barH = height * 0.12f * intensity
                    canvas.drawRect(0f, 0f, width.toFloat(), barH, paint)
                    canvas.drawRect(0f, height - barH, width.toFloat(), height.toFloat(), paint)
                }
                else -> {}
            }
        }

        canvas.restore()

        // Progress bar at the very bottom
        val progWidth = (currentMs.toFloat() / totalDurationMs.toFloat()) * width
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(230, 255, 46, 147)
        canvas.drawRect(0f, height - 8f, progWidth, height.toFloat(), paint)
    }

    private fun saveToMediaStore(file: File, title: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.TITLE, title)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/AnimeForge")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (_: Exception) {
            return null
        }
    }

    fun getShareUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
