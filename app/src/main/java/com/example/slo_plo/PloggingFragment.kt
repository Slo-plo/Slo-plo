package com.example.slo_plo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class PloggingFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPloggingBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPloggingBinding.inflate(inflater, container, false)

        // 위치 정보 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 네이버 맵 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_plogging_map) as MapFragment
        mapFragment.getMapAsync(this)  // 맵 준비가 완료되면 콜백을 호출

        return binding.root
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 권한 확인 후 위치 가져오기
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 있으면 위치 가져오기
            getDeviceLocation()
        }
    }

    // 권한 요청 후 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되었으면 위치 가져오기
                getDeviceLocation()
            } else {
                // 권한 거부된 경우 처리
                Toast.makeText(requireContext(), "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 위치 정보 가져오기
    private fun getDeviceLocation() {
        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // 권한이 승인된 상태에서 위치 정보 가져오기
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // 네이버 지도 SDK의 LatLng 사용
                    val latLng = LatLng(location.latitude, location.longitude)

                    // 로그 찍기: 위치 정보 확인
                    Log.d("PloggingFragment", "위치: Lat: ${location.latitude}, Lng: ${location.longitude}")

                    // 네이버 맵에 마커 추가
                    val marker = Marker()
                    marker.position = latLng
                    marker.map = naverMap

                    // 지도 위치를 사용자의 위치로 이동
                    Log.d("PloggingFragment", "카메라 이동: ${latLng.latitude}, ${latLng.longitude}")
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
                }
            }
            .addOnFailureListener {
                // 실패 시 로그 찍기
                Log.e("PloggingFragment", "위치 정보를 가져올 수 없습니다.")
                Toast.makeText(requireContext(), "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
