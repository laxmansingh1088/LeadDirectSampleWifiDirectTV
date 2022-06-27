package com.example.leaddirectsamplewifidirecttv.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resource_detail")
data class ResourceDetail(
    @PrimaryKey val id: Long,
    val fileName: String,
    val fileType: Int,
    val absolutePath: String,
    val fileSize: Long,
    val mimeType: String,
    val isSynced: Boolean = false
)
