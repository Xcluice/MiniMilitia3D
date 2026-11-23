package com.xcluice.minimilitia3d

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SfxEngine {
    @Volatile
    var muted: Boolean = false
    private val pool = Executors.newFixedThreadPool(2)
    private val sampleRate = 22050

    fun play(kind: String) {
        if (muted) return
        pool.execute {
            try {
                val dur = when (kind) {
                    "uzi" -> 0.06f
                    "magnum" -> 0.14f
                    "shotgun" -> 0.22f
                    "rpg" -> 0.30f
                    "explode" -> 0.50f
                    "hit" -> 0.05f
                    else -> 0.12f
                }
                val n = (sampleRate * dur).toInt()
                val pcm = ShortArray(n)
                var lp = 0f
                for (i in 0 until n) {
                    val t = i.toFloat() / sampleRate
                    val env = exp((-i.toFloat() / (n * 0.32f)).toDouble()).toFloat()
                    val sample = when (kind) {
                        "uzi", "magnum", "shotgun" -> {
                            val noise = Random.nextFloat() * 2f - 1f
                            val alpha = if (kind == "uzi") 0.45f else if (kind == "magnum") 0.25f else 0.15f
                            lp += alpha * (noise - lp)
                            lp * env * 0.55f
                        }
                        "explode" -> {
                            val noise = Random.nextFloat() * 2f - 1f
                            lp += 0.07f * (noise - lp)
                            (lp + sin(2.0 * PI * 55.0 * t).toFloat() * 0.4f) * env * 0.85f
                        }
                        "rpg" -> {
                            val freq = 220f - 140f * (t / dur)
                            sin(2.0 * PI * freq * t).toFloat() * env * 0.45f
                        }
                        "hit" -> sin(2.0 * PI * 1450.0 * t).toFloat() * env * 0.35f
                        "pickup" -> sin(2.0 * PI * (560f + 320f * (t / dur)) * t).toFloat() * env * 0.35f
                        else -> sin(2.0 * PI * 190.0 * t).toFloat() * env * 0.30f
                    }
                    pcm[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                }
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(pcm, 0, pcm.size)
                track.play()
                Thread.sleep((dur * 1000).toLong() + 30L)
                track.stop()
                track.release()
            } catch (_: Throwable) {
            }
        }
    }
}
