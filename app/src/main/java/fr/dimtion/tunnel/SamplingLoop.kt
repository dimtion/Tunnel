package fr.dimtion.tunnel

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import ca.uol.aig.fftpack.RealDoubleFFT
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.pow

class SamplingLoop(val activity: MainActivity) : Thread() {

    val TAG = "SamplingLoop"

    val sampleRate = 44100
    val sampleDuration = 2000  // in ms
    val minBuffSize = sampleRate * sampleDuration / 1000

    val lastFrequencyStoreSize = 3

    private lateinit var recorder: AudioRecord

    val lastFreqs: ArrayBlockingQueue<Double> = ArrayBlockingQueue(lastFrequencyStoreSize)


    fun initRecorder_M() {
        recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(2 * minBuffSize)  // Twice because 16 bits
            .build()
    }

    override fun run() {
        initRecorder_M()

        // TODO: Test the Automatic Gain Control (AGC)

        if (recorder.state == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "Fail to initialize AudioRecord")
            return
        }


        val audioSamples = ShortArray(recorder.bufferSizeInFrames)
        var audioSamplesFFT: DoubleArray

        recorder.startRecording()
        var recording = true
        var i = 0
        val fft = RealDoubleFFT(audioSamples.size)

        Log.i(TAG, "Starting recording")
        Log.d(TAG, "audioSampleSize ${audioSamples.size}")
        while (recording) {
            val readLen = recorder.read(
                audioSamples,
                0,
                audioSamples.size,
                AudioRecord.READ_BLOCKING
            )
            if (readLen == 0) {
                Log.w(TAG, "We have read $readLen bytes from the audio source")
            }
            if (readLen < 0) {
                Log.e(TAG, "Error while reading from input source $readLen")
            }
            // Normalize
            audioSamplesFFT = normalizeSamples(audioSamples)
            val maxFreq = calculateMaxFreq(fft, audioSamplesFFT)
            pushFreq(maxFreq)

            // Log.d(TAG, "READ LEN: $readLen")
            activity.runOnUiThread {
                activity.updateTextView("%.1f".format(lastFreqs.average()))
            }

            i++
            // if (i > 1000) {
            //     recording = false
            // }

        }
        recorder.stop()
    }

    private fun normalizeSamples(inputSamples: ShortArray): DoubleArray {
        return inputSamples.map { x: Short -> x / (2.0.pow(16) - 1) }.toDoubleArray()
    }

    private fun calculateMaxFreq(fft: RealDoubleFFT, inputSamples: DoubleArray): Double {
        fft.ft(inputSamples)
        var maxId = 0
        var max = 0.0
        inputSamples.forEachIndexed { i, x ->
            if (x > max) {
                max = x
                maxId = i
            }
        }
        Log.d(TAG, "$maxId $max")
        return maxId * sampleRate.toDouble() / inputSamples.size.toDouble() / 2.0
    }

    private fun pushFreq(freq: Double) {
        if (lastFreqs.size > 0 && lastFreqs.size >= lastFrequencyStoreSize) {
            lastFreqs.remove()
        }
        lastFreqs.add(freq)
    }
}