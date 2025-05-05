package com.example.slo_plo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.slo_plo.databinding.FragmentPloggingBinding
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.geometry.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.overlay.Marker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class PloggingFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPloggingBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap
    private lateinit var settingsClient: SettingsClient

    private var locationMarker: Marker? = null
    private var locationCallback: LocationCallback? = null

    private var startTime: Long = 0
    private var timer: Timer? = null

    private var totalDistance: Float = 0f
    private var prevLocation: Location? = null

    // 위치 설정 결과를 받기 위한 런처 추가
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d("PloggingFragment", "위치 서비스가 활성화")
                Toast.makeText(requireContext(), "위치 서비스가 활성화되었습니다.", Toast.LENGTH_SHORT).show()
                // 위치 서비스가 활성화되면 바로 위치 가져오기 시도
                getDeviceLocation()
            }
            Activity.RESULT_CANCELED -> {
                Log.d("PloggingFragment", "위치 서비스가 꺼져 있음")
                Toast.makeText(requireContext(), "플로깅 기능을 사용하려면 위치 서비스를 켜야 합니다.", Toast.LENGTH_SHORT).show()
                finishFragment()
            }
        }
    }

    // 위치 권한 요청을 위한 런처 추가
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            getDeviceLocation()
        } else {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finishFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPloggingBinding.inflate(inflater, container, false)

        // 위치 정보 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        settingsClient = LocationServices.getSettingsClient(requireContext())

        // 맵 프래그먼트를 초기화하고 콜백 연결
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_plogging_map) as MapFragment
        mapFragment.getMapAsync(this)

        binding.btnPloggingEnd.setOnClickListener {
            showDialogForEnd()
        }

        binding.btnPloggingCancel.setOnClickListener {
            showDialogForCancel()
        }

        return binding.root
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.uiSettings.isCompassEnabled = false
        naverMap.uiSettings.isScaleBarEnabled = false
        naverMap.uiSettings.isZoomControlEnabled = false

        // 권한 확인 후 위치 서비스 확인 및 위치 요청
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            if (!isLocationEnabled()) {
                promptToEnableLocation()
            } else {
                getDeviceLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // 위치가 꺼져있으면 설정 다이얼로그를 띄워 사용자에게 요청
    private fun promptToEnableLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getDeviceLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettingsLauncher.launch(intentSenderRequest)
                } catch (sendEx: Exception) {
                    Log.e("PloggingFragment", "Failed to resolve location settings", sendEx)
                }
            }
        }
    }

    // 위치 정보 획득 시 초기 지도 위치 설정 및 위치 추적 시작
    private fun getDeviceLocation() {
        Log.d("PloggingFragment", "getDeviceLocation 호출됨")

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PloggingFragment", "위치 권한이 없음")
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        if (!isLocationEnabled()) {
            Log.d("PloggingFragment", "위치 서비스가 꺼져 있음")
            promptToEnableLocation()
            return
        }

        Log.d("PloggingFragment", "위치 정보 요청 시작")
        // 권한이 승인된 상태에서 위치 정보 가져오기
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("PloggingFragment", "위치 정보 가져옴: ${location.latitude}, ${location.longitude}")
                    val latLng = LatLng(location.latitude, location.longitude)

                    Log.d("PloggingFragment", "사용자 방향: ${location.bearing}°")

                    // 지도 위치를 사용자의 위치로 이동
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

                    // 지도 줌 레벨 설정
                    naverMap.moveCamera(CameraUpdate.zoomTo(20.0))

                    // 현재 사용자의 베어링을 가져와서 카메라에 적용
                    val bearing = location.bearing // 사용자의 방향 값

                    // CameraPosition 생성
                    val cameraPosition = CameraPosition(
                        latLng,         // 위치
                        20.0,           // 줌 레벨
                        0.0,            // 기울기
                        bearing.toDouble() // 회전 각도
                    )

                    naverMap.moveCamera(CameraUpdate.toCameraPosition(cameraPosition)) // 지도 회전

                    // 위치 추적 모드 활성화
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow

                    // 위치 업데이트 시작
                    requestNewLocation()

                    // 플로깅 시간 측정 시작
                    startRecordTime()
                } else {
                    Log.d("PloggingFragment", "위치 정보가 null임")
                    Toast.makeText(requireContext(), "위치 정보를 다시 가져옵니다.", Toast.LENGTH_SHORT).show()

                    // 위치 정보가 null이면 새로운 위치 요청
                    requestNewLocation()
                }
            }
            .addOnFailureListener { e ->
                Log.e("PloggingFragment", "위치 정보 가져오기 실패", e)
                Toast.makeText(requireContext(), "위치 정보를 가져올 수 없습니다. 다시 시도합니다.", Toast.LENGTH_SHORT).show()

                // 실패 시 새로운 위치 요청
                requestNewLocation()
            }
    }

    // 위치가 주기적으로 갱신될 때 호출되는 콜백 설정 및 거리 계산, 지도 이동 처리
    private fun requestNewLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000  // 5초마다 위치 업데이트
            fastestInterval = 2000  // 최소 2초마다 업데이트
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        Log.d("PloggingFragment", "새 위치 정보 가져옴: ${location.latitude}, ${location.longitude}")
                        val latLng = LatLng(location.latitude, location.longitude)

                        updateRecordDist(location)

                        Log.d("PloggingFragment", "사용자 방향: ${location.bearing}°")

                        // 마커 갱신
                        if (locationMarker != null) {
                            locationMarker?.position = latLng
                        } else {
                            locationMarker = Marker()
                            locationMarker?.position = latLng
                            locationMarker?.map = naverMap
                        }

                        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

                        val bearing = location.bearing

                        val cameraPosition = CameraPosition(
                            latLng,
                            20.0,
                            0.0,
                            bearing.toDouble()
                        )

                        naverMap.moveCamera(CameraUpdate.toCameraPosition(cameraPosition)) // 지도 회전
                        naverMap.moveCamera(CameraUpdate.zoomTo(20.0))

                        naverMap.locationTrackingMode = LocationTrackingMode.Follow
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    // 이전 위치와 현재 위치 사이의 거리를 계산하고 UI에 갱신
    private fun updateRecordDist(location: Location) {
        prevLocation?.let { prevLoc ->
            val distance = prevLoc.distanceTo(location)
            totalDistance += distance

            val displayDistance = if (totalDistance < 1000) {
                "${totalDistance.toInt()} m"
            } else {
                String.format(Locale.KOREA, "%.1f km", totalDistance / 1000)
            }

            binding.tvPloggingDistance.text = "이동 거리 - $displayDistance"
        }
        prevLocation = location
    }

    // 타이머 시작 및 시간 표시 갱신
    private fun startRecordTime() {
        startTime = System.currentTimeMillis()
        binding.tvPloggingTime.text = "시간 - 00 : 00"

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread {
                    updateRecordTime()
                }
            }
        }, 0, 1000)
    }

    // 타이머 중지
    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    // 위치 추적 콜백 제거
    private fun stopRecordTime() {
        timer?.cancel()
        timer = null
    }

    // 경과 시간을 계산하여 UI에 시간 표시 갱신
    private fun updateRecordTime() {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        val minutes = elapsedTime / 60
        val seconds = elapsedTime % 60
        binding.tvPloggingTime.text = String.format(Locale.KOREA, "시간 - %02d : %02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 프래그먼트 종료
    private fun finishFragment() {
        stopRecordTime()
        stopLocationUpdates()
        parentFragmentManager.popBackStack()  // 프래그먼트 종료
    }

    // 플로깅 종료 다이얼로그 표시
    private fun showDialogForEnd() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("플로깅 종료")
            .setMessage("플로깅을 종료하고 일지를 작성하시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                // Todo: 일지 화면으로 이동하면서 플로깅 기록 보내기
                stopRecordTime()
                stopLocationUpdates()
                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    // 플로깅 취소 다이얼로그 표시
    private fun showDialogForCancel() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("플로깅 취소")
            .setMessage("플로깅을 취소하고 홈화면으로 돌아가시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                dialog.dismiss()  // 다이얼로그 먼저 닫기
                finishFragment()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()  // 다이얼로그 닫기
            }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼 클릭 이벤트를 감지하여 다이얼로그 띄우기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showDialogForCancel()
        }
    }
}