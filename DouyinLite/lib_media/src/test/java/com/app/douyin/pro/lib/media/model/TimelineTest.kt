package com.app.douyin.pro.lib.media.model

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class TimelineTest {

    @Test
    fun `getTotalDurationMs should return sum of clip durations`() {
        val timeline = EditingTimeline()
        val mockUri = mock(Uri::class.java)

        timeline.videoMainTrack.add(VideoClip("1", mockUri, 0L, 5000L, 5000L))
        timeline.videoMainTrack.add(VideoClip("2", mockUri, 1000L, 3000L, 5000L))

        // 5000 + (3000-1000) = 7000
        assertEquals(7000L, timeline.getTotalDurationMs())
    }
}
