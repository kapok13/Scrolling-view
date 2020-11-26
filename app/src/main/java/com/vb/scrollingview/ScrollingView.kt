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
    }

    var animationDuration = 100L

    private var spinsEndCallback: SpinsAnimationEndListener? = null

    private lateinit var currentImage: ImageView
    private lateinit var nextImage: ImageView

    private lateinit var picsList: MutableList<Int>
    private lateinit var tagsList: MutableList<Int>

    private var lastRes = 0
    private var rotates = 0

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
        currentImage.animate().translationY(-height.toFloat()).duration = animationDuration
        nextImage.translationY = nextImage.height.toFloat()
        nextImage.animate().translationY(0f)
            .setUpdateListener { it.duration = animationDuration }
            .setListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    setImage(currentImage, Random.nextInt(0, picsList.size))
                    setImage(nextImage, Random.nextInt(0, picsList.size))
                    currentImage.translationY = 0f
                    if (rotates != rotateCount) {
                        spin(rotateCount)
                        rotates++
                    } else {
                        lastRes = 0
                        rotates = 0
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
            .duration = animationDuration
    }

    fun setCurrentImage(imageId: Int) {
        currentImage.setImageResource(imageId)
        nextImage.setImageResource(imageId)
    }

    fun setSpinData(imgIds: Set<Int>) {
        for ((counter, imgs) in imgIds.withIndex()) {
            picsList.add(imgs)
            tagsList.add(counter)
        }
    }

    fun setWinChance(imgResId: Int, until: Int) {
        if (picsList.contains(imgResId)) {
            for (i in 1..until) {
                picsList.add(imgResId)
                tagsList.add(picsList.indexOf(imgResId))
            }
        }
    }

    fun setImage(pic: ImageView, tagg: Int) {
        pic.setImageResource(picsList[tagg])
        pic.tag = tagsList[tagg]
    }


    fun setOnAnimationEndListener(spinsListener: SpinsAnimationEndListener) {
        spinsEndCallback = spinsListener
    }

    interface SpinsAnimationEndListener {
        fun onSpinsEnd(image: ImageView, tag: Int)
    }
}