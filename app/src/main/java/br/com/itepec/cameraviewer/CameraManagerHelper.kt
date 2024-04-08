package br.com.itepec.cameraviewer

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class CameraManagerHelper(private val context: Context) {

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun getExternalCameraId(): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_EXTERNAL) {
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    // Função para iniciar a câmera externa
    fun startExternalCamera() {
        val externalCameraId = getExternalCameraId()
        if (externalCameraId != null) {
            // Faça aqui a inicialização da câmera externa usando o ID da câmera
            // Exemplo: val camera = Camera.open(externalCameraId)
        } else {
            // Lidar com o caso em que não há câmera externa disponível
        }
    }
}
