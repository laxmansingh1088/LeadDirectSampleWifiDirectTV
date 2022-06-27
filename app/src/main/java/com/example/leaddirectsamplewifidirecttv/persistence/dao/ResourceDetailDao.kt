package com.example.leaddirectsamplewifidirecttv.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.leaddirectsamplewifidirecttv.persistence.entities.ResourceDetail

@Dao
interface ResourceDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resourceDetail: ArrayList<ResourceDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResourceDetailList(resourceDetail: List<ResourceDetail>)


    @Query("SELECT * FROM resource_detail")
    fun getResourceDetailList(): List<ResourceDetail>


    @Query("DELETE FROM resource_detail")
    suspend fun deleteAll(): Int


}