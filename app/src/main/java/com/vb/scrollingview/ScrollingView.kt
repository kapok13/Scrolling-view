package com.vb.scrollingview

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.random.Random

class ScrollingView : FrameLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor (context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context)
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.ScrollingView, 0, 0).apply {
            try {
                currentImage.setImageResource(
                    getResourceId(
                        R.styleable.ScrollingView_setCurrentImage,
                        0
                    )
                )
                nextImage.setImageResource(getResourceId(R.styleable.ScrollingView_setNextImage, 0))
                currentImage.setBackgroundResource(
                    getResourceId(
                        R.styleable.ScrollingView_setCurrentImageBackground,
                        0
                    )
                )
                nextImage.setBackgroundResource(
                    getResourceId(
                        R.styleable.ScrollingView_setNextImageBackground,
                        0
                    )
                )
            } finally {
                recycle()
            }
        }
    }

    var animationDuration = 100L

    private var spinsEndCallback: SpinsAnimationListener? = null

    private lateinit var currentImage: ImageView
    private lateinit var nextImage: ImageView

    private lateinit var picsList: MutableList<Int>
    private lateinit var tagsList: MutableList<Int>

    private var lastRes = 0
    private var rotates = 0
    private var counter = 0

    private fun init(context: Context) {
        picsList = mutableListOf()
        tagsList = mutableListOf()
        with(LayoutInflater.from(context).inflate(R.layout.image_view_scrolling, this)) {
            currentImage = findViewById(R.id.current_card)
            nextImage = findViewById(R.id.next_card)
            nextImage.translationY = height.toFloat()
        }
    }


    fun spin(rotateCount: Int) {
        val resIdPos = Random.nextInt(0, picsList.size)
        currentImage.animate().translationY(height.toFloat()).duration = animationDuration
        nextImage.translationY = -nextImage.height.toFloat()
        nextImage.animate().translationY(0f)
            .setDuration(animationDuration)
            .setUpdateListener { it.duration = animationDuration }
            .setListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    if (counter == 0) {
                        spinsEndCallback?.onSpinsStart(rotateCount * animationDuration)
                        counter++
                    }
                    setImage(nextImage, resIdPos)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    setImage(currentImage, resIdPos)
                    currentImage.translationY = 0f
                    if (rotates != rotateCount) {
                        spin(rotateCount)
                        rotates++
                    } else {
                        lastRes = 0
                        rotates = 0
                        counter = 0
                        val finalRandom = Random.nextInt(0, picsList.size)
                        setImage(nextImage, finalRandom)
                        setImage(currentImage, finalRandom)
                        spinsEndCallback?.onSpinsEnd(
                            currentImage,
                            currentImage.tag.toString().toInt()
                        )
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
    }

    fun setCurrentImage(imageId: Int) {
        currentImage.setImageResource(imageId)
        nextImage.setImageResource(imageId)
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
            for (i in picsList) {
                if (i == imgResId)
                    imgPosList.add(picsList.indexOf(i))
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
        fun onSpinsEnd(image: ImageView, tag: Int)
    }
}