package com.bt.clipbo.data.repository

import com.bt.clipbo.data.database.TagDao
import com.bt.clipbo.data.database.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository
    @Inject
    constructor(
        private val tagDao: TagDao,
    ) {
        fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

        fun searchTags(query: String): Flow<List<TagEntity>> = tagDao.searchTags(query)

        suspend fun insertTag(tag: TagEntity): Long = tagDao.insertTag(tag)

        suspend fun updateTag(tag: TagEntity) = tagDao.updateTag(tag)

        suspend fun deleteTag(tag: TagEntity) = tagDao.deleteTag(tag)

        suspend fun getTagByName(name: String): TagEntity? = tagDao.getTagByName(name)

        suspend fun incrementUsageCount(tagId: Long) = tagDao.incrementUsageCount(tagId)
    }
