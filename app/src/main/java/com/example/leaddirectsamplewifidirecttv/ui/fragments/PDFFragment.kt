package com.example.leaddirectsamplewifidirecttv.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.databinding.FragmentPdfBinding
import com.example.leadp2pdirect.servers.FileModel
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File
import java.io.InputStream

class PDFFragment : Fragment(R.layout.fragment_pdf) {
    private lateinit var binding: FragmentPdfBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val filePathsList = requireArguments().getSerializable("filePathsList") as ArrayList<FileModel>
        Log.d("someeee", "$filePathsList")
        binding = getView().let { FragmentPdfBinding.bind(it!!) }
        showPdf(filePathsList?.get(0).absoluteFilePath)
    }

    private fun showPdf(path: String?) {
        binding.pdfView?.fromFile(File(path))?.pageFitPolicy(FitPolicy.WIDTH)?.load()
        // binding.pdfView?.fromAsset("sample3.pdf")?.pageFitPolicy(FitPolicy.WIDTH)?.load()
    }


}