package com.lifeonline.app.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): Boolean {
        return try {
            currentFile = File(context.cacheDir, "voice_task_${System.currentTimeMillis()}.m4a")
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            r.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(16000)
                setOutputFile(currentFile!!.absolutePath)
                prepare()
                start()
            }
            recorder = r
            true
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            false
        }
    }

    fun stopRecording(): File? {
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            currentFile
        } catch (e: Exception) {
            e.printStackTrace()
            recorder?.release()
            recorder = null
            null
        }
    }

    fun cancelRecording() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            // ignore
        }
        recorder?.release()
        recorder = null
        currentFile?.delete()
        currentFile = null
    }
}
