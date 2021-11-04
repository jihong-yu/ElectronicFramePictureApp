# ElectronicFramePictureApp
휴대폰 내 사진을 최대 6개까지 선택 후 사진들을 애니메이션으로 실행시키는 App입니다.

### 1. 개발환경
* IDE: Android Studio Arctic Fox | 2020.3.1 Canary 1
* Language : Kotlin
---
### 2. 사용 라이브러리
* Timer 사용(코틀린 기본 제공)
---
### 3. 지원기능
1. 휴대폰 내부에 저장되어 있는 이미지들을 최대 6개 까지 선택할 수 있습니다.
2. 이미지들을 선택 후 애니메이션으로 해당 이미지들을 차례대로 볼 수 있습니다.
<img src="https://user-images.githubusercontent.com/57440834/140091846-336e3301-9d71-413c-985f-b8c3e817039c.png" width="700" height="1000">
<img src="https://user-images.githubusercontent.com/57440834/140091914-d36db92c-ce71-480d-9e57-8200639d9ec0.png" width="700" height="1000">

---

### 4. 추가설명
기초적인 어플이기 때문에 ViewBinding 과 같은 라이브러리는 사용하지 않았으며 모두 각각 findViewById로 xml 뷰에 접근하였습니다.<br>

```kotlin
private val startPhotoFrameModeButton: Button by lazy {
        findViewById(R.id.startPhotoFrameModeButton)
    }
```
<br>

사진추가하기 버튼을 클릭할 경우 권한을 체크하여 권한이 허용되지 않았다면 권한을 요청하고 만약 권한을 거부한다면 한번더 해당 권한이 필요한 이유를 다이얼로그를 띄어서 권한을 요청하는 식으로 
했습니다. 
```kotlin
//사진추가하기 버튼 클릭 리스너 등록 메서드
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
```
<br>


만약 위의 과정에도 불구하고 사용자가 권한을 요청을 거부했다면 해당 버튼을 클릭할때마다 권한이 필요하기 때문에 사용자가 직접 휴대폰 내부의 (설정 - 앱 - 권한)으로 이동하도록 설정하였습니다. 권한을 요청하면 자동으로 실행되는 onRequestPermissionsResult 콜백 함수에서 AlertDialog를 띄어 확인버튼을 눌르면 StartActivity로 이동할 수 있도록 해당 기능을 구현하였습니다.
```kotlin
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
```
<br>


또한 휴대폰 내장 이미지 폴더에 접근은 Deprecated 된 startActivityForResult 대신 registerForActivityResult를 사용하였습니다.
  ```kotlin
  //사진추가 버튼을 실행 후 결과를 반환하는 콜백메서드
        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                imageLoadingActivityResult(result)
            }
            
        //휴대폰 내장 이미지 폴더를 볼 수 있는 메서드
    private fun navigatePhotos() {
        //액션을 주면 안드로이드 내장에 있는 컨텐츠를 가져오는 액티비티 실행
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" //갤러리에서 image에 해당하는 모든 타입을 설정(png,jpg..) vs 동영상은 video/*
        //intent 실행
        startForResult.launch(intent)
    }
  ```
  <br>
  
  

  사용자가 이미지를 클릭후 실행되는 메서드는 imageLoadingActivityResult로 정의하였으며 바로 위 코드에서 볼 수 있듯이 사진추가 버튼을 클릭 후 자동으로 실행되는 콜백메서드에서 실행하였습니다.
  ```kotlin
 // 이미지를 클릭한 후에 실행되는 메서드
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
  ```
  <br>
  
  
  전자액자 실행하기 버튼 클릭시 사용자가 선택한 이미지들을 intent에 실어서 photoFrameActivity로 넘겨주었습니다.
  ```kotlin
  //전자액자 실행하기 버튼 클릭시 PhotoFrameActivity로 이미지 Uri를 넘겨줌
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
  ```


<br>

타이머를 설정하여 5초마다 한번씩 실행되게 설정하였습니다. 두개의 이미지 뷰를 겹치게 설정하였으며 1번 이미지뷰에 첫번째 이미지를 보이게 두었으며 2번째 이미지뷰에 그다음 이미지를 투명도 0 -> 1로 바꿔주면서 서서히 나타나게 설정하였습니다. 이 과정이 이미지6개를 돌면서 5초마다 실행이 되게 설정하였습니다. 또한 timer함수는 비동기로 실행되기 때문에 UI 조작이 불가능 하여  runOnUiThread로 Main UI에 접근하였습니다.
```kotlin
//timer - 일정한 시간을 주기로 반복 동작을 수행할때 쓰는 기능 ( 반복주기 속성 'period')
    //비동기로 실행되기 때문에 UI 조작x
    private fun startTimer() {
        //5초마다 계속해서 실행(단, 타이머는 백그라운드에서도 계속해서 실행이됨)
        timer = timer(period = 5 * 1000L) {
            runOnUiThread {

                val current = currentPosition
                backgroundPhotoImageView.setImageURI(photoList[current])

                val next = if (currentPosition + 1 >= photoList.size) 0 else currentPosition + 1

                //투명도(0f를 주게 되면 완전히 투명하게된다.)
                photoImageView.alpha = 0f
                photoImageView.setImageURI(photoList[next])
                //투명도가 0~1까지 애니메이션 형식으로 투명도가 지속시간만큼 바뀜
                photoImageView.animate().alpha(1.0f).setDuration(1000).start()
                currentPosition = next

            }
        }
    }
```



<br>

마지막으로 Timer와 같은 비동기 실행 함수는 백그라운드에서도 계속 실행이 되므로 생명주기 (onStop에서는 멈추게 하였고 onStart에서 시작하게 하여 백그라운드에서 실행되지 않도록 하였습니다.
```kotlin
//onstop 된 후 다시 restart될 때도 실행될 수 있게 하기 위해 onStart에서 타이머를 실행해준다.
    override fun onStart() {
        super.onStart()
        //timer 메서드 시작
        startTimer()
    }
    
//앱이 정지되었을 때 Timer를 정지시켜준다.
    override fun onStop() {
        super.onStop()
        timer.cancel()
    }
```
