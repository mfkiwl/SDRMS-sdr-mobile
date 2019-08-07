package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var convolutionFilterLength = 10
    private var convolutionDataLength = 50000
    private var fftWidth = 1024
    private var fftDataLength = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup convolution button listener
        convolutionButton.setOnClickListener {
            onConvolutionButtonClick()
        }

        // setup convolution filter length slider
        val filterLengthHandler = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                convolutionFilterLength = progress + 1
                setFilterLengthLabel()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        filterLengthBar.setOnSeekBarChangeListener(filterLengthHandler)
        setFilterLengthLabel()

        // setup fft button listener
        fftButton.setOnClickListener {
            onFFTButtonClick()
        }

        // setup fft width slider
        val fftWidthHandler = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fftWidth = (progress + 1) * 1024
                setFFTWidthLabel()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        fftWidthBar.setOnSeekBarChangeListener(fftWidthHandler)
        setFFTWidthLabel()

    }

    private fun onConvolutionButtonClick() {
        // perform JVM convolution benchmark
        val jvmTotalTime = convolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmSamplesPerSecond = if (jvmTotalTime != 0L) convolutionDataLength * 1000 / jvmTotalTime else convolutionDataLength * 1000
        val jvmResultLabel = "JVM total time: $jvmTotalTime ms\nJVM samples/s: $jvmSamplesPerSecond"
        setConvolutionResult(jvmResultLabel)

        // perform NDK convolution benchmark
        val ndkTotalTime = ndkConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkSamplesPerSecond = if (ndkTotalTime != 0L) convolutionDataLength * 1000 / ndkTotalTime else convolutionDataLength * 1000
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK samples/s: $ndkSamplesPerSecond"
        setConvolutionResult("$jvmResultLabel\n\n$ndkResultLabel")
    }

    private fun onFFTButtonClick() {
        // perform JVM FFT benchmark
        val jvmTotalTime = fftBenchmark(fftWidth, fftDataLength * fftWidth)
        val jvmFFTsPerSecond = if (jvmTotalTime != 0L) fftDataLength * 1000 / jvmTotalTime else fftDataLength * 1000
        val jvmResultLabel = "JVM total time: $jvmTotalTime ms\nJVM FFTs/s: $jvmFFTsPerSecond"
        setFFTResult(jvmResultLabel)

        // perform NDK FFT benchmark
        val ndkTotalTime = ndkFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkFFTsPerSecond = if (ndkTotalTime != 0L) fftDataLength * 1000 / ndkTotalTime else fftDataLength * 1000
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK FFTs/s: $ndkFFTsPerSecond"
        setFFTResult("$jvmResultLabel\n\n$ndkResultLabel")
    }

    private fun setConvolutionResult(result: String) {
        convolutionResultText.text = result
    }

    private fun setFilterLengthLabel() {
        filterLengthLabel.text = "Filter length: $convolutionFilterLength"
    }

    private fun setFFTResult(result: String) {
        fftResultText.text = result
    }

    private fun setFFTWidthLabel() {
        fftWidthLabel.text = "FFT width: $fftWidth"
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun ndkConvolutionBenchmark(filterLength: Int, dataLength: Int): Long
    external fun ndkFFTBenchmark(fftWidth: Int, dataLength: Int): Long

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
