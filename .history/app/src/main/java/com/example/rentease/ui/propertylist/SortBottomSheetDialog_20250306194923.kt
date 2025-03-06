package com.example.rentease.ui.propertylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rentease.R
import com.example.rentease.databinding.BottomSheetSortBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortBottomSheetDialog : BottomSheetDialogFragment() {
    private var _binding: BottomSheetSortBinding? = null
    private val binding get() = _binding!!

    private var currentSortOption: SortOption = SortOption.NEWEST
    private var onSortOptionSelected: ((SortOption) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the current sort option
        when (currentSortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> binding.sortPriceLowToHigh.isChecked = true
            SortOption.PRICE_HIGH_TO_LOW -> binding.sortPriceHighToLow.isChecked = true
            SortOption.NEWEST -> binding.sortNewest.isChecked = true
            SortOption.OLDEST -> binding.sortOldest.isChecked = true
        }

        // Handle button clicks
        binding.applyButton.setOnClickListener {
            val selectedOption = when {
                binding.sortPriceLowToHigh.isChecked -> SortOption.PRICE_LOW_TO_HIGH
                binding.sortPriceHighToLow.isChecked -> SortOption.PRICE_HIGH_TO_LOW
                binding.sortNewest.isChecked -> SortOption.NEWEST
                binding.sortOldest.isChecked -> SortOption.OLDEST
                else -> currentSortOption
            }
            onSortOptionSelected?.invoke(selectedOption)
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setCurrentSortOption(option: SortOption) {
        currentSortOption = option
    }

    fun setOnSortOptionSelectedListener(listener: (SortOption) -> Unit) {
        onSortOptionSelected = listener
    }

    enum class SortOption {
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        NEWEST,
        OLDEST
    }

    companion object {
        fun newInstance(
            currentOption: SortOption,
            onOptionSelected: (SortOption) -> Unit
        ): SortBottomSheetDialog {
            return SortBottomSheetDialog().apply {
                setCurrentSortOption(currentOption)
                setOnSortOptionSelectedListener(onOptionSelected)
            }
        }
    }
}
