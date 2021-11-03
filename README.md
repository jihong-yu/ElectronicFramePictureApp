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
<img src="" width="700" height="1000">
<img src="https://user-images.githubusercontent.com/57440834/139875491-df5d713c-8ee8-4ae1-b99d-d5fa49550b60.png" width="700" height="1000">

---
### 4. 추가설명

기초적인 어플이기 때문에 ViewBinding 과 같은 라이브러리는 사용하지 않았으며 모두 각각 findViewById로 xml 뷰에 접근하였습니다.<br>

```kotlin
private val openButton: Button by lazy {
        findViewById(R.id.openButton)
    }
```
<br>

passwordChanging 라는 boolean형 변수를 만들어 사용자가 현재 비밀번호 변경 버튼을 눌렀는지 체크하였습니다. 만약 눌렀다면 입력한 비밀번호가 현재 비밀번호와 맞는지 체크 후 맞다면 그 값을 
true로 변경하였습니다. 또한 사용자가 변경상태를 알 수 있도록 버튼의 색깔도 빨간색으로 변경하였습니다.

```kotlin
private var passwordChanging = false

//만약 입력한패스워드와 저장되어있는 패스워드가 같아서 패스워드를 변경하기를 원한다면
            if(spPassword == inputPassword){
                //패스워드변경중 변수에 true값 대입
                passwordChanging = true
                Toast.makeText(this@MainActivity, "원하는 비밀번호를 입력 후 다시한번 버튼을 눌러주세요", Toast.LENGTH_LONG).show()
                //비밀번호 변경시 배경색을 빨간색으로 변경하여 변경중이라는 것을 사용자에게 알려준다.
                changePasswordButton.setBackgroundColor(getColor(R.color.red))
```
<br>


패스워드는 다음과 같이 SharedPreferences에 저장하였습니다. 여기서 코틀린확장함수 edit을 사용하였으며 패스워드 저장같은 경우는 비동기적으로 실행
되기 보다는 동기적으로 실행되어야 하므로 apply보다는 commit을 사용하였습니다.(commit = true) <br>
```kotlin
//만약 패스워드를 변경중이라면
            if(passwordChanging){
                //새로운 패스워드를 SharedPreferences에 넣는다.
                sp.edit(commit = true) {
                    val inputPassword = numberPicker1.value.toString() + numberPicker2.value.toString() +numberPicker3.value.toString()
                    this.putString("password",inputPassword)
                }
                Toast.makeText(this@MainActivity, "비밀번호 변경 완료", Toast.LENGTH_SHORT).show()
                //패스워드 변경중이 완료되었으면 다시 false값으로 돌린다.
                passwordChanging = false
                //패스워드변경을 완료하였으면 배경색을 다시 검은색으로 복구시킨다.
                changePasswordButton.setBackgroundColor(getColor(R.color.black))
                return@setOnClickListener
            }
```
<br>


만약 입력한 비밀번호와 저장되어 있는 비밀번호가 다르다면 AlertDialog를 띄어서 보여주었습니다.
  ```kotlin
  //비밀번호가 잘못되었을때 알람 띄어주는 메서드
    private fun failedAlertDialogMessage(){
        val dialog = AlertDialog.Builder(this@MainActivity)
        dialog.setTitle("실패!!")
        dialog.setMessage("비밀번호가 잘못되었습니다.")
        dialog.setPositiveButton("확인") { _, _ ->  }
        dialog.show()
    }
  ```
  <br>
  
  
  글 저장부분에 있어서는 Handler와 Runnable을 이용하여 처리하였습니다. 기본적인 Handler를 초기화 후 텍스트에 작성한 글을 SharedPrefrences에 저장하는 Runnable을 설정하였습니다. 그 후
  editText의 addTextChangedListener를 불러와 사용자가 글 작성을 끝난 직후 afterTextChanged에 해당 Runnable를 Handler가 post해주는 식으로 코드를 짰습니다. 단, 이렇게 사용할 경우 성능
  상 사용자가 글을 치고 난 후 계속해서 SharedPreferences에 저장하기 때문에 Handler의 Postdelayed와 removeCallbacks 2개를 이용해서 사용자가 글을 작성한 후 다시 글을 쓰기까지의 텀이 존재
  할 경우에만 SharedPreferences에 저장하도록 처리하였습니다.
  
  ```kotlin
 private val handler:Handler = Handler(Looper.getMainLooper())
 
 val runnable = Runnable {
            //edit 에서 commit은 동기적으로실행되기때문에 main쓰레드의 처리중인 쓰레드가 블락킹이 될 수도
            //있다. apply는 비동기적으로 실행되기 때문에 blocking안됨
            sp.edit{
                this.putString("diaryText",editText.text.toString())
            }
        }
        
  editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            //postDelayed로 인해 0.5초마다 해당 runnable를 실행하지만 removeCallbacks으로인해 계속해서 취소하게된다.
            //즉, 마지막 runnable만 실행하게 된다.
            override fun afterTextChanged(s: Editable?) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable,500L)
            }
        })
  ```
  
