package com.jimdac_todolist.electronicframepictureapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import java.util.*
import kotlin.concurrent.timer

class PhotoFrameActivity : AppCompatActivity() {

    private val photoList = mutableListOf<Uri>()
    private var currentPosition = 0
    private val backgroundPhotoImageView: ImageView by lazy {
        findViewById(R.id.backgroundPhotoImageView)
    }

    private val photoImageView: ImageView by lazy {
        findViewById(R.id.photoImageView)
    }

    private lateinit var timer:Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_frame)
        
        //Intent로부터 Uri를 가져오기
        getPhotoUriFromIntent()


    }

    //onstop 된 후 다시 restart될 때도 실행될 수 있게 하기 위해 onStart에서 타이머를 실행해준다.
    override fun onStart() {
        super.onStart()
        //timer 메서드 시작
        startTimer()
    }

    //Intent로부터 Uri를 가져오는 메서드
    private fun getPhotoUriFromIntent() {

        val size = intent.getIntExtra("photoListSize", 0)
        for (i in 0 until size) {
            intent.getStringExtra("photo$i").let {
                photoList.add(Uri.parse(it))
            }
        }
    }

    //timer - 일정한 시간을 주기로 반복 동작을 수행할때 쓰는 기능 ( 반복주기 속성 'period')
    //백그라운드로 실행되기 때문에 UI 조작x
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

    //앱이 정지되었을 때 Timer를 정지시켜준다.
    override fun onStop() {
        super.onStop()
        timer.cancel()
    }

    //앱이 종료되었을 때도 타이머를 정지시켜준다.
    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}