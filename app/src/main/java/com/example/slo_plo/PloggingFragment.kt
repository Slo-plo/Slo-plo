package com.example.slo_plo

import android.Manifest
import android.app.Activity
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
import com.naver.maps.map.LocationTrackingMode

class PloggingFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPloggingBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap
    private lateinit var settingsClient: SettingsClient

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

    // 마커 선언
    private var locationMarker: Marker? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPloggingBinding.inflate(inflater, container, false)

        // 위치 정보 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        settingsClient = LocationServices.getSettingsClient(requireContext())

        // 네이버 맵 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_plogging_map) as MapFragment
        mapFragment.getMapAsync(this)  // 맵 준비가 완료되면 콜백을 호출

        return binding.root
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 먼저 위치 권한 확인
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 권한 요청
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            // 권한이 있으면 위치 서비스 확인
            if (!isLocationEnabled()) {
                // 위치 서비스가 꺼져 있으면 다이얼로그 띄우기
                promptToEnableLocation()
            } else {
                // 권한도 있고 위치 서비스도 켜져 있으면 위치 가져오기
                getDeviceLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptToEnableLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // 위치 서비스가 이미 켜져 있으면, 위치 정보 가져오기
            getDeviceLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // 위치 설정 다이얼로그 호출
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettingsLauncher.launch(intentSenderRequest)
                } catch (sendEx: Exception) {
                    Log.e("PloggingFragment", "Failed to resolve location settings", sendEx)
                }
            }
        }
    }

    private fun getDeviceLocation() {
        Log.d("PloggingFragment", "getDeviceLocation 호출됨")

        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PloggingFragment", "위치 권한이 없음")
            // 권한이 없으면 요청
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        // 위치 서비스가 켜져 있는지 확인
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

                    // 지도 위치를 사용자의 위치로 이동
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

                    // 지도 줌 레벨 설정
                    naverMap.moveCamera(CameraUpdate.zoomTo(20.0))

                    // 위치 추적 모드 활성화
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow

                    // 위치 업데이트 시작
                    requestNewLocation()
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

    // 새로운 위치 요청 함수 추가
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

                        // 마커 갱신
                        if (locationMarker != null) {
                            locationMarker?.position = latLng
                        } else {
                            locationMarker = Marker()
                            locationMarker?.position = latLng
                            locationMarker?.map = naverMap
                        }

                        // 지도 위치를 사용자의 위치로 이동
                        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

                        // 지도 줌 레벨 설정
                        naverMap.moveCamera(CameraUpdate.zoomTo(20.0))

                        // 위치 추적 모드 활성화
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 위치 서비스가 꺼져 있으면 플로깅 프래그먼트 종료
    private fun finishFragment() {
        requireActivity().onBackPressed()  // 프래그먼트 종료
    }
}