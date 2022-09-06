package com.example.keepthetime_project.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.keepthetime_project.R
import com.example.keepthetime_project.databinding.FragmentMyFriendBinding
import com.example.keepthetime_project.databinding.FragmentMyProfileBinding
import com.example.keepthetime_project.databinding.FragmentRequestedUsersBinding


class RequestedUsersFragment : BaseFragment() {

    private var mBinding : FragmentRequestedUsersBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentRequestedUsersBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEvents()
        setValues()
    }

    override fun setEvents() {
    }

    override fun setValues() {
    }




}