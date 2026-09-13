package com.alzimer.echobox.domain.data.model.home

import com.alzimer.echobox.domain.data.model.home.chart.Chart
import com.alzimer.echobox.domain.data.model.mood.Mood
import com.alzimer.echobox.domain.utils.Resource

data class HomeResponse(
    val homeItem: Resource<ArrayList<HomeItem>>,
    val exploreMood: Resource<Mood>,
    val exploreChart: Resource<Chart>,
)