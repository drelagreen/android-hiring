package com.example.myapplication.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import com.example.myapplication.R

class GenderSelectorButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageButton(context, attrs, defStyle) {
    enum class GenderType { MALE, FEMALE }

    private val defaultElevation: Float
    private val selectedElevation: Float
    private val pressedElevation: Float
    private val genderType: GenderType
    private val animDuration = 200L

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GenderSelectorButton,
            0, 0
        ).apply {
            defaultElevation = getDimension(
                R.styleable.GenderSelectorButton_defaultElevation,
                resources.getDimension(R.dimen.gender_button_elevation_default)
            )
            selectedElevation = getDimension(
                R.styleable.GenderSelectorButton_selectedElevation,
                resources.getDimension(R.dimen.gender_button_elevation_selected)
            )
            pressedElevation = getDimension(
                R.styleable.GenderSelectorButton_pressedElevation,
                resources.getDimension(R.dimen.gender_button_elevation_pressed)
            )
            genderType = when (getInt(R.styleable.GenderSelectorButton_genderType, 0)) {
                1 -> GenderType.FEMALE
                else -> GenderType.MALE
            }
            recycle()
        }

        val paddings = resources.getDimensionPixelSize(R.dimen.gender_button_padding)
        setPadding(paddings, paddings, paddings, paddings)

        when (genderType) {
            GenderType.MALE -> {
                setImageResource(R.drawable.ic_male)
                setBackgroundResource(R.drawable.bg_gender_selector_male)
            }
            GenderType.FEMALE -> {
                setImageResource(R.drawable.ic_female)
                setBackgroundResource(R.drawable.bg_gender_selector_female)
            }
        }

        isFocusable = true
        isClickable = true
        ViewCompat.setElevation(this, 0f)
        translationZ = defaultElevation
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        val targetZ = if (selected) selectedElevation else defaultElevation
        isClickable = !selected
        animate().translationZ(targetZ).setDuration(animDuration).start()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        val targetZ = when {
            pressed -> pressedElevation
            isSelected -> selectedElevation
            else -> defaultElevation
        }
        animate().translationZ(targetZ).setDuration(animDuration).start()
    }
}
