package mrhi3.ai.studio.wordscramble

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

/**
 * 펜 클래스: 그리기 경로를 저장하는 데이터 클래스
 */
class Pen(
    val x: Float,
    val y: Float,
    val moveStatus: Int,
    val color: Int,
    val size: Int
) {
    companion object {
        const val STATE_START = 0    // 펜의 상태(움직임 시작)
        const val STATE_MOVE = 1     // 펜의 상태(움직이는 중)
    }

    /**
     * 현재 pen의 상태가 움직이는 상태인지 반환합니다.
     */
    fun isMove(): Boolean {
        return moveStatus == STATE_MOVE
    }
}

/**
 * DrawCanvas 클래스: 그림 그리기 기능을 제공하는 커스텀 뷰
 */
class DrawCanvas(context: Context) : View(context) {
    companion object {
        const val PEN_SIZE = 3       // 기본 펜 사이즈
    }

    private val drawCommandList = ArrayList<Pen>()  // 그리기 경로가 기록된 리스트
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var loadDrawImage: Bitmap? = null                // 호출된 이전 그림
    private var color = Color.BLACK   // 현재 펜 색상
    private var size = PEN_SIZE      // 현재 펜 크기

    init {
        init()
    }

    /**
     * 그리기에 필요한 요소를 초기화 합니다.
     */
    fun init() {
        drawCommandList.clear()
        loadDrawImage = null
        color = Color.BLACK
        size = PEN_SIZE
        invalidate()
    }

    /**
     * 현재까지 그린 그림을 Bitmap으로 반환합니다.
     */
    fun getCurrentCanvas(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)

        loadDrawImage?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        for (i in drawCommandList.indices) {
            val p = drawCommandList[i]
            paint.color = p.color
            paint.strokeWidth = p.size.toFloat()

            if (p.isMove()) {
                val prevP = drawCommandList[i - 1]
                canvas.drawLine(prevP.x, prevP.y, p.x, p.y, paint)
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val action = e.action
        val state = if (action == MotionEvent.ACTION_DOWN) Pen.STATE_START else Pen.STATE_MOVE
        drawCommandList.add(Pen(e.x, e.y, state, color, size))
        invalidate()
        return true
    }
}