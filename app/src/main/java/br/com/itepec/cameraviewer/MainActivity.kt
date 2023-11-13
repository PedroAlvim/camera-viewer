package br.com.itepec.cameraviewer

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import br.com.itepec.cameraviewer.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null

    private var gridActivated = false

    private var currentZoomRatio = 1f
    private val zoomStep = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.hide()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setGridBehavior()
        setZoomBehavior()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setGridBehavior() {
        binding.gridBtn.setOnClickListener {
            if (gridActivated) {
                binding.grid1.isVisible = false
                binding.grid2.isVisible = false

                binding.gridBtn.setImageResource(R.drawable.ic_grid)
            } else {
                binding.grid1.isVisible = true
                binding.grid2.isVisible = true

                binding.gridBtn.setImageResource(R.drawable.ic_grid_off)
            }
            gridActivated = !gridActivated
        }
    }

    private fun setZoomBehavior() {
        binding.btn3.setOnClickListener {
            currentZoomRatio += zoomStep

            val maxZoomRatio = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
            if (currentZoomRatio > maxZoomRatio) {
                currentZoomRatio = maxZoomRatio
            }

            camera?.cameraControl?.setZoomRatio(currentZoomRatio)
            updateZoomButtonsState()
        }

        binding.btn1.setOnClickListener {
            currentZoomRatio -= zoomStep

            // Verifique se o nível de zoom está abaixo do zoom mínimo
            val minZoomRatio = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
            if (currentZoomRatio < minZoomRatio) {
                currentZoomRatio = minZoomRatio
            }

            camera?.cameraControl?.setZoomRatio(currentZoomRatio)
            updateZoomButtonsState()
        }

        updateZoomButtonsState()
    }

    private fun updateZoomButtonsState() {
        val maxZoomRatio = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
        val minZoomRatio = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f

        binding.btn3.isEnabled = currentZoomRatio < maxZoomRatio

        binding.btn1.isEnabled = currentZoomRatio > minZoomRatio

        if (!binding.btn3.isEnabled && !binding.btn1.isEnabled) {
            binding.btn3.isEnabled = true
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Usado para vincular o ciclo de vida das câmeras ao proprietário do ciclo de vida
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).createSurfaceProvider())
            }

            imageCapture = ImageCapture.Builder().build()

            // Seleciona a câmera traseira como padrão
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincule os casos de uso antes de vincular novamente
                cameraProvider.unbindAll()

                // Vincule os casos de uso à câmera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Falha na vinculação dos casos de uso", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Verifica a permissão da câmera
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissões não concedidas pelo usuário.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXGFG"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
