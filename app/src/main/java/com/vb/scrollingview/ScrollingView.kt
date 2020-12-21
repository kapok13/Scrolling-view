package com.vb.scrollingview

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import kotlin.random.Random

class ScrollingView : LinearLayout {

    private var columnsCount = 0
    private var rowsCount = 0

    constructor(context: Context) : super(context)

    constructor (context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {

        context.theme.obtainStyledAttributes(attributeSet, R.styleable.ScrollingView, 0, 0).apply {
            try {
                columnsCount = getInteger(R.styleable.ScrollingView_columnCount, 0)
                rowsCount = getInteger(R.styleable.ScrollingView_rowCount, 0)
                imgWidth = getDimensionPixelSize(R.styleable.ScrollingView_images_width, 100)
                imgHeight = getDimensionPixelSize(R.styleable.ScrollingView_images_height, 100)
                titleImageResId = getResourceId(R.styleable.ScrollingView_title_image, 0)
            } finally {
                init(context)
                recycle()
            }
        }
    }

    var animationDuration = 50L

    private var spinsEndCallback: SpinsAnimationListener? = null

    lateinit var scrollingContainer: LinearLayout

    private lateinit var picsList: MutableList<Int>
    private lateinit var tagsList: MutableList<Int>
    private lateinit var columnsCurrentContainersList: MutableList<LinearLayout>
    private lateinit var columnsNextContainersList: MutableList<LinearLayout>
    private val callbackTagList = mutableListOf<Int>()

    private var titleImageResId = 0
    private var imgWidth = 0
    private var imgHeight = 0
    private var lastRes = 0
    private var rotates = 0
    private var counter = 0
    private var spinsMethodExecuteCounter = 1

    private fun init(context: Context) {
        picsList = mutableListOf()
        tagsList = mutableListOf()
        columnsCurrentContainersList = mutableListOf()
        columnsNextContainersList = mutableListOf()
        with(LayoutInflater.from(context).inflate(R.layout.image_view_scrolling, this)) {
            scrollingContainer = findViewById(R.id.image_vies_scrolling_container)
            initImgs(context)
            for (i in columnsCurrentContainersList) {
                i.translationY = height.toFloat()
            }
            for (i in columnsNextContainersList) {
                i.translationY = height.toFloat()
            }
            for (columns in 0 until columnsCount) {
                for (rows in 0 until rowsCount)
                    (columnsCurrentContainersList[columns].getChildAt(rows) as ImageView).setImageResource(
                        titleImageResId
                    )
            }
        }
    }

    private fun initImgs(context: Context) {
        if (columnsCount > 0 && rowsCount > 0) {
            for (i in 0 until columnsCount) {
                val frame = FrameLayout(context)
                val currentLinear = LinearLayout(context)
                currentLinear.orientation = VERTICAL
                val nextLinear = LinearLayout(context)
                nextLinear.orientation = VERTICAL
                for (k in 0 until rowsCount) {
                    with(ImageView(context)) {
                        layoutParams = LayoutParams(imgWidth, imgHeight)
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                        currentLinear.addView(this)
                    }
                }
                for (m in 0 until rowsCount) {
                    with(ImageView(context)) {
                        layoutParams = LayoutParams(imgWidth, imgHeight)
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                        nextLinear.addView(this)
                    }
                }
                frame.addView(currentLinear)
                frame.addView(nextLinear)
                columnsCurrentContainersList.add(currentLinear)
                columnsNextContainersList.add(nextLinear)
                scrollingContainer.addView(frame)
            }
        }
    }

    fun spin(rotateCount: Int) {
        callbackTagList.clear()
        for (i in 0 until columnsCurrentContainersList.size) {
            spin(rotateCount + i * 2, columnsCurrentContainersList[i], columnsNextContainersList[i])
        }
    }

    private fun spin(
        rotateCount: Int,
        currentImageContainer: LinearLayout,
        nextImageContainer: LinearLayout
    ) {
        val positionsList = mutableListOf<Int>()
        currentImageContainer.animate().translationY(height.toFloat()).duration = animationDuration
        nextImageContainer.translationY = -nextImageContainer.height.toFloat()
        nextImageContainer.animate().translationY(0f)
            .setDuration(animationDuration)
            .setUpdateListener { it.duration = animationDuration }
            .setListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    if (counter == 0) {
                        spinsEndCallback?.onSpinsStart(rotateCount * animationDuration)
                        counter++
                    }
                    for (i in 0 until rowsCount) {
                        val resIdPos = Random.nextInt(0, picsList.size)
                        positionsList.add(resIdPos)
                        setImage((nextImageContainer.getChildAt(i) as ImageView), resIdPos)
                    }
                }

                override fun onAnimationEnd(animation: Animator?) {
                    for (i in 0 until rowsCount) setImage(
                        (currentImageContainer.getChildAt(i) as ImageView),
                        positionsList[i]
                    )
                    currentImageContainer.translationY = 0f
                    if (rotates <= rotateCount) {
                        spin(rotateCount, currentImageContainer, nextImageContainer)
                        rotates++
                    } else {
                        for (i in 0 until rowsCount) callbackTagList.add(
                            (currentImageContainer.getChildAt(
                                i
                            ) as ImageView).tag.toString().toInt()
                        )
                        if (spinsMethodExecuteCounter == columnsCurrentContainersList.size) {
                            lastRes = 0
                            rotates = 0
                            counter = 0
                            spinsMethodExecuteCounter = 1
                            spinsEndCallback?.onSpinsEnd(callbackTagList)
                        } else {
                            spinsMethodExecuteCounter++
                        }
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
    }


    fun setSpinData(imgIds: Set<Int>) {
        for (i in 1..5) {
            for ((counter, imgs) in imgIds.withIndex()) {
                picsList.add(imgs)
                tagsList.add(counter)
            }
        }
    }

    fun setWinChance(imgResId: Int, until: Int) {
        val imgPosList = mutableListOf<Int>()
        if (until in 1..10 && picsList.contains(imgResId)) {
            for ((position, i) in picsList.withIndex()) {
                if (i == imgResId)
                    imgPosList.add(position)
            }
            if (imgPosList.size > until) {
                var differences = imgPosList.size - until
                while (differences > 0) {
                    picsList.removeAt(imgPosList[0])
                    tagsList.removeAt(imgPosList[0])
                    imgPosList.removeAt(0)
                    differences--
                }
            } else if (imgPosList.size < until) {
                var differences = until - imgPosList.size
                while (differences > 0) {
                    picsList.add(imgResId)
                    tagsList.add(picsList.indexOf(imgResId))
                    differences--
                }
            }
        }
    }

    private fun setImage(pic: ImageView, tagg: Int) {
        pic.setImageResource(picsList[tagg])
        pic.tag = tagsList[tagg]
    }


    fun setAnimationListener(spinsListener: SpinsAnimationListener) {
        spinsEndCallback = spinsListener
    }

    interface SpinsAnimationListener {
        fun onSpinsStart(duration: Long)
        fun onSpinsEnd(tags: MutableList<Int>)
    }
}