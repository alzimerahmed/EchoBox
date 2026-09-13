package com.alzimer.echobox.domain.repository

import com.alzimer.echobox.domain.data.model.update.UpdateData
import com.alzimer.echobox.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {
    fun checkForGithubReleaseUpdate(): Flow<Resource<UpdateData>>
}