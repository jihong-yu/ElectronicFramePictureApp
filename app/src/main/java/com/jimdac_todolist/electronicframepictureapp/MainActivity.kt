package com.jimdac_todolist.electronicframepictureapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    lateinit var startForResult: ActivityResultLauncher<Intent>

    private val startPhotoFrameModeButton: Button by lazy {
        findViewById(R.id.startPhotoFrameModeButton)
    }

    private val addPhotoButton: Button by lazy {
        findViewById(R.id.addPhotoButton)
    }

    private val imageUriList: MutableList<Uri> by lazy {
        mutableListOf()
    }

    private val imageViewList: List<ImageView> by lazy {
        mutableListOf<ImageView>().apply {
            this.add(findViewById(R.id.imageView11))
            this.add(findViewById(R.id.imageView12))
            this.add(findViewById(R.id.imageView13))
            this.add(findViewById(R.id.imageView21))
            this.add(findViewById(R.id.imageView22))
            this.add(findViewById(R.id.imageView23))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //사진추가 버튼을 실행 후 결과를 반환하는 콜백메서드
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                imageLoadingActivityResult(result)
            }

        //사진추가하기 버튼 클릭 리스너 
        initAddPhotoButton()

        //전자액자 실행하기 버튼 클릭 리스너
        initStartPhotoFrameModeButton()
    }

    private fun initStartPhotoFrameModeButton() {
        startPhotoFrameModeButton.setOnClickListener { 
            if(imageUriList.size == 0){
                Toast.makeText(this@MainActivity, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else{
                val intent = Intent(this@MainActivity,PhotoFrameActivity::class.java)
                imageUriList.forEachIndexed { index, uri ->
                    intent.putExtra("photo$index",uri.toString())
                }
                intent.putExtra("photoListSize",imageUriList.size)
                startActivity(intent)
            }
        }
    }

    ////사진추가하기 버튼 클릭 리스너 등록 메서드
    private fun initAddPhotoButton() {
        addPhotoButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //todo 권한이 주어졌다면
                    navigatePhotos()
                }

                //만약 사용자가 해당 권한 체크를 거부했을 경우 해당 인자로 들어간 권한이 true를 반환(다시보지않기를 선택시 false반환)
                //동의하기를 눌러 권한 체킹과정에서 다시 또 권한을 거부했다면 그 이후부터는 false를 반환
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    //todo 교육용 팝업 확인 후 권한 팝업을 띄우는 기능
                    showPermissionContextPopup()
                }
                else -> {
                    Log.d("TAG", "else: ")
                    //todo 권한 요청 팝업 띄우기
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }
        }
    }

    //사용자가 권한을 거부했을 경우 권한에 대한 요청이유를 보여주는 팝업
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("권한이 필요합니다.")
            .setMessage("전자액자 앱에서 사진을 불러오기 위해 권한이 필요 합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    //requestPermissions으로 권한 요청한 뒤 허용 or 거부 되었을때 실행되는 콜백함수
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //todo 권한이 부여된 것입니다.
                    navigatePhotos()
                } else {
                    //만약 권한을 영영 거부했을 경우,앱 설정화면으로 이동하여 권한을 직접 허용하도록 유도
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("경고").setMessage("권한이 거부되어 실행할 수 없습니다. 권한을 허용해주세요")
                        .setPositiveButton("확인") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                            startActivity(intent)
                        }.create().show()
                }
            }
        }
    }

    private fun imageLoadingActivityResult(result: ActivityResult) {
        //todo 결과가 정상적으로 전달이 된다면 실행
        if (result.resultCode == Activity.RESULT_OK) {
            //이미지 폴더를 열고 어떠한 이미지를 선택하면 선택한 이미지에대한 데이터를 반환
            val seletedImageUri: Uri? = result.data?.data
            if (seletedImageUri != null) {
                //imageUriList 를 따로 만들어 해당 List의 크기를 이용하여 imageViewList의 인덱스에 접근하였음
                //이미지를 6개가 꽉찼어도 계속해서 다시 첫번째 칸부터 선택할수 있게 하기 위해 나머지연산자를 이용하여 무한루프를 돌도록 하였음
                imageViewList[imageUriList.size % 6].setImageURI(seletedImageUri)
                imageUriList.add(seletedImageUri)
            } else { //해당 이미지의 주소가 null 값이라면
                Toast.makeText(this@MainActivity, "이미지를 가져오는데 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
            //사용자가 이미지를 선택중 뒤로가기버튼을 눌렀다면
        } else if(result.resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(this@MainActivity, "이미지 가져오기가 취소 되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "오류가 발생 하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //휴대폰 내장 이미지 폴더를 볼 수 있는 메서드
    private fun navigatePhotos() {
        //액션을 주면 안드로이드 내장에 있는 컨텐츠를 가져오는 액티비티 실행
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" //갤러리에서 image에 해당하는 모든 타입을 설정(png,jpg..) vs 동영상은 video/*
        
        startForResult.launch(intent)
    }
}