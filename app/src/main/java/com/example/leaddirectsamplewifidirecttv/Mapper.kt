package com.example.leaddirectsamplewifidirecttv

import androidx.room.PrimaryKey
import com.example.leaddirectsamplewifidirecttv.persistence.entities.ResourceDetail
import com.example.leadp2pdirect.servers.FileModel

object Mapper {
    fun convertToResourceDetailList(list: ArrayList<FileModel>): ArrayList<ResourceDetail> {
        val resourceDetailList = ArrayList<ResourceDetail>()
        for (fileModel in list) {
            val resourceDetail = ResourceDetail(
                fileModel.id,
                fileModel.fileName,
                fileModel.type,
                "/storage/emulated/0/Download/com.example.leaddirectsamplewifidirecttv/${fileModel.fileName}",
                fileModel.fileLength,
                fileModel.mimeType,
                false
            )
            resourceDetailList.add(resourceDetail)
        }
        return resourceDetailList
    }
}