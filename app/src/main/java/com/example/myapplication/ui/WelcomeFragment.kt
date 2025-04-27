package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentWelcomeBinding
import com.example.myapplication.ui.WelcomeDialogFragmentViewModel.Intent
import com.example.myapplication.ui.WelcomeDialogFragmentViewModel.State
import com.example.myapplication.ui.WelcomeDialogFragmentViewModel.State.Gender

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    companion object {
        private const val MIN_AGE = 1
        private const val MAX_AGE = 99
        private val DROPDOWN_AGES = listOf("--") +
                (MIN_AGE..MAX_AGE).map { it.toString() }.toList()
    }

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private var intentHandler: ((Intent) -> Unit)? = null

    private var initialState: State? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentWelcomeBinding.bind(view)

        binding.btnMale.setOnClickListener {
            binding.btnMale.isSelected = true
            binding.btnFemale.isSelected = false
            intentHandler?.invoke(
                Intent.OnGenderChange(Gender.MALE)
            )
        }

        binding.btnFemale.setOnClickListener {
            binding.btnFemale.isSelected = true
            binding.btnMale.isSelected = false
            intentHandler?.invoke(
                Intent.OnGenderChange(Gender.FEMALE)
            )
        }

        initAgeDropdown()

        binding.btnContinue.setOnClickListener {
            intentHandler?.invoke(Intent.OnContinueClick)
        }

        initialState?.let {
            updateUI(it)
        }
    }

    private fun initAgeDropdown() {
        val spinnerAdapter = SpinnerAdapter(requireContext(), DROPDOWN_AGES)
        binding.spinnerAge.adapter = spinnerAdapter
        binding.spinnerAge.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, pos: Int, id: Long
                ) {
                    if (pos == 0) return
                    spinnerAdapter.selectedPosition = pos
                    intentHandler?.invoke(
                        Intent.OnAgeChange(DROPDOWN_AGES.getOrNull(pos)?.toIntOrNull() ?: 0)
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>) = Unit
            }
    }

    fun setIntentHandler(handler: ((Intent) -> Unit)?) {
        intentHandler = handler
    }

    fun setState(state: State) {
        Log.i("TAG", "setState: $state")
        if (view == null) {
            initialState = state
        } else {
            updateUI(state)
        }
    }

    private fun updateUI(state: State) {
        Log.i("TAG", "updateUI: $state")
        when (state.gender) {
            Gender.MALE -> {
                binding.btnFemale.isSelected = false
                binding.btnMale.isSelected = true
            }

            Gender.FEMALE -> {
                binding.btnFemale.isSelected = true
                binding.btnMale.isSelected = false
            }

            Gender.UNKNOWN -> {
                binding.btnFemale.isSelected = false
                binding.btnMale.isSelected = false
            }
        }

        if (state.age < MIN_AGE || state.age > MAX_AGE) {
            binding.spinnerAge.setSelection(0)
        } else {
            binding.spinnerAge.setSelection(DROPDOWN_AGES.indexOf(state.age.toString()))
        }

        binding.btnContinue.isEnabled = state.isButtonEnabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        intentHandler = null
    }

    class SpinnerAdapter(
        context: Context,
        items: List<String>
    ) : ArrayAdapter<String>(context, R.layout.item_dropdown_default, items) {

        var selectedPosition: Int = 0
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val tv = (convertView as? TextView) ?: LayoutInflater.from(context)
                .inflate(R.layout.item_dropdown_closed, parent, false) as TextView
            tv.text = getItem(selectedPosition)
            return tv
        }

        override fun getDropDownView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            val tv = (convertView as? TextView)
                ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown_default, parent, false) as TextView
            tv.text = getItem(position)

            if (position == selectedPosition) {
                tv.setTextAppearance(R.style.AgeDropdown_Item_Selected)
            } else {
                tv.setTextAppearance(R.style.AgeDropdown_Item)
            }
            return tv
        }
    }
}
