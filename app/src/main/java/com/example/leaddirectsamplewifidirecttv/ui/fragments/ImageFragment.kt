package com.example.leaddirectsamplewifidirecttv.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.databinding.FragmentImageBinding
import com.example.leadp2pdirect.servers.FileModel

class ImageFragment : Fragment(R.layout.fragment_image) {
    private lateinit var binding: FragmentImageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val filePathsList = requireArguments().getSerializable("filePathsList") as ArrayList<FileModel>
        Log.d("someeee", "$filePathsList")
        binding= getView().let { FragmentImageBinding.bind(it!!) }
        showImage(filePathsList?.get(0).absoluteFilePath)
    }

    private fun showImage(path:String?){
        Glide.with(this).load(path).into(binding.imageView!!)
    }
}