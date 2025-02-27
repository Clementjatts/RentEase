package com.example.rentease.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * BaseFragment provides common functionality for all fragments in the app.
 * It handles the view binding lifecycle and provides hooks for setting up UI and observers.
 * 
 * @param VB The type of ViewBinding used by the fragment.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    
    private var _binding: VB? = null
    
    /**
     * The view binding for the fragment.
     * This property is only valid between onCreateView and onDestroyView.
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")
    
    /**
     * Inflates the view binding for the fragment.
     * This method must be implemented by subclasses to provide the correct view binding.
     */
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }
    
    /**
     * Set up the UI components of the fragment.
     * This method is called after the view is created.
     */
    protected open fun setupUI() {
        // To be implemented by subclasses
    }
    
    /**
     * Set up the observers for the fragment.
     * This method is called after the view is created.
     */
    protected open fun setupObservers() {
        // To be implemented by subclasses
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
