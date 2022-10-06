package com.example.keepthetime_project

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.viewModels
import com.example.keepthetime_project.databinding.ActivityEditAppointmentBinding
import com.example.keepthetime_project.viewmodel.AddAppointmentViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.odsay.odsayandroidsdk.API
import com.odsay.odsayandroidsdk.ODsayData
import com.odsay.odsayandroidsdk.ODsayService
import com.odsay.odsayandroidsdk.OnResultCallbackListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditAppointmentActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityEditAppointmentBinding
    private val addAppointmentViewModel by viewModels<AddAppointmentViewModel>()

    private var marker: Marker? = null
    private var selectedLatLng: LatLng? = null
    private var path: PathOverlay? = null

    //    약속 시간 저장하는 멤버변수, 현재시간 세팅
    val mSelectedAppointmentDateTime = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvents()
        setValues()
        observer()
    }

    override fun setEvents() {
        binding.txtDate.setOnClickListener {
            val dateSet = object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayofMonth: Int) {

                    mSelectedAppointmentDateTime.set(year, month, dayofMonth)
                    val simpleDate = SimpleDateFormat("yy/MM/dd")

                    binding.txtDate.text = simpleDate.format(mSelectedAppointmentDateTime.time)
                }
            }

            val datePickerDialog = DatePickerDialog(
                mContext,
                dateSet,
                mSelectedAppointmentDateTime.get(Calendar.YEAR),
                mSelectedAppointmentDateTime.get(Calendar.MONTH),
                mSelectedAppointmentDateTime.get(Calendar.DAY_OF_MONTH)
            ).show()

        }

        binding.txtTime.setOnClickListener {
            val timeSet = object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {

                    mSelectedAppointmentDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    mSelectedAppointmentDateTime.set(Calendar.MINUTE, minute)

                    val simpleDate = SimpleDateFormat("a h시 mm분")
                    binding.txtTime.text = simpleDate.format(mSelectedAppointmentDateTime.time)

                }
            }

            val timePickerDialog = TimePickerDialog(
                mContext,
                timeSet,
                12, 0, false
            ).show()
        }

        binding.btnSave.setOnClickListener {

            val inputTitle = binding.edtTitle.text.toString()
            if (inputTitle.isEmpty()) {
                Toast.makeText(mContext, "약속 제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.txtDate.text == "약속 일자") {
                Toast.makeText(mContext, "일자를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.txtDate.text == "약속 시간") {
                Toast.makeText(mContext, "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val now = Calendar.getInstance()
            if (mSelectedAppointmentDateTime.timeInMillis < now.timeInMillis) {
                Toast.makeText(mContext, "현재 이후의 시간으로 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val inputPlaceName = binding.edtPlaceName.text.toString()
            if (inputPlaceName.isEmpty()) {
                Toast.makeText(mContext, "장소 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedLatLng == null) {
                Toast.makeText(mContext, "약속 장소를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val simpleDate = SimpleDateFormat("yyyy-MM-dd HH:mm")

            addAppointmentViewModel.postAddAppointment(
                mContext,
                inputTitle,
                simpleDate.format(mSelectedAppointmentDateTime.time),
                inputPlaceName,
                selectedLatLng!!.latitude,
                selectedLatLng!!.longitude
            )
        }

    }

    override fun setValues() {
        setMap()


    }

    private fun observer() {

        addAppointmentViewModel.appointment.observe(this, androidx.lifecycle.Observer {
            if (it.code == 200) {
                Toast.makeText(mContext, "약속 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {

//        임시 위,경도 - 서울역
        val coord = LatLng(37.554706, 126.970794)

        val cameraUpdate = CameraUpdate.scrollTo(coord)
        naverMap.moveCamera(cameraUpdate)

//        마커 객체 생성 및 맵에 표시하기
        naverMap.setOnMapClickListener { pointF, latLng ->

//            마커 객체가 없을 시 새 마커 생성
            if (marker == null) {
                marker = Marker()
            }

            marker?.position = latLng
            marker?.map = naverMap

            selectedLatLng = latLng

//            coord ~ 선택지까지 경로 그리기 (naverMap PathOverlay + ODSay 라이브러리)
//            path 객채가 없을 시 새 객체 생성

            val myODSayService =
                ODsayService.init(mContext, "qiVt8z/WkU8uqSBTpRQe+DllZJUeVZiTYUrk+h10pz8")

            myODSayService.requestSearchPubTransPath(
                coord.longitude.toString(),
                coord.latitude.toString(),
                latLng.longitude.toString(),
                latLng.latitude.toString(),
                "0", null, "0",
                object : OnResultCallbackListener{
                    override fun onSuccess(p0: ODsayData?, p1: API?) {
                        // ODSayData 파싱
                        p0?.let {
                            val resultObj = it.json.getJSONObject("result")
                            val pathArray = resultObj.getJSONArray("path")
                            val firstPath = pathArray.getJSONObject(0)   // 첫번째 경로 추출
                            val subPathArray = firstPath.getJSONArray("subPath")    // 경로내보기

                            val stationLatLngList = ArrayList<LatLng>() // 첫번째 경로 지나는 위,경도 값 저장 List
                            stationLatLngList.add(coord) // 시작점 추가

                            for(i in 0 until subPathArray.length()){    // 서브패스 배열 길이만큼 돌아
                                val subPathObj = subPathArray.getJSONObject(i)
                                if(!subPathObj.isNull("passStopList")){
                                    val pathStopListObj = subPathObj.getJSONObject("passStopList")
                                    val stationsList = pathStopListObj.getJSONArray("stations")

                                    for (j in 0 until stationsList.length()){   // 경로 내 정거장 리스트 길이만큼 돌아서
                                        val stationsObj = stationsList.getJSONObject(j)
                                        val x = stationsObj.getString("x").toDouble()   // 경도 추출 lng
                                        val y = stationsObj.getString("y").toDouble()   // 위도 추출 lat

                                        stationLatLngList.add(LatLng(y,x))
                                    }
                                }

                            }

                            stationLatLngList.add(latLng) // 도착지 추가

                            path?.coords = stationLatLngList
                            path?.map = naverMap
                        }

                    }

                    override fun onError(p0: Int, p1: String?, p2: API?) {

                    }

                }
            )

            if (path == null) {
                path = PathOverlay()
            }
            path?.coords = listOf(      //빈 리스트를 만들어서,출발 / 도착지 지정
                coord, latLng
            )
            path?.map = naverMap

        }

    }

}