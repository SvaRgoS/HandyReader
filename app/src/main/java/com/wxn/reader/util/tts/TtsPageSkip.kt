package com.wxn.reader.util.tts

enum class TtsPageSkipDirection {
    Backward,
    Forward,
}

sealed class TtsPageSkipTarget {
    data class Page(val pageIndex: Int) : TtsPageSkipTarget()

    data object PreviousChapter : TtsPageSkipTarget()

    data object NextChapter : TtsPageSkipTarget()
}

object TtsPageSkip {
    fun target(
        currentPageIndex: Int,
        pageCount: Int,
        direction: TtsPageSkipDirection,
    ): TtsPageSkipTarget? {
        if (currentPageIndex !in 0 until pageCount) {
            return null
        }
        return when (direction) {
            TtsPageSkipDirection.Backward -> {
                if (currentPageIndex == 0) {
                    TtsPageSkipTarget.PreviousChapter
                } else {
                    TtsPageSkipTarget.Page(currentPageIndex - 1)
                }
            }

            TtsPageSkipDirection.Forward -> {
                if (currentPageIndex == pageCount - 1) {
                    TtsPageSkipTarget.NextChapter
                } else {
                    TtsPageSkipTarget.Page(currentPageIndex + 1)
                }
            }
        }
    }
}
