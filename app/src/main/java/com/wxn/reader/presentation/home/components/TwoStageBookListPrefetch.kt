package com.wxn.reader.presentation.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListPrefetchScope
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.foundation.lazy.layout.NestedPrefetchScope

private const val BOOK_LIST_PREFETCH_COUNT = 2

/**
 * Preserves Compose's urgent prefetch for the next card and additionally prepares one card farther
 * ahead at normal priority. This avoids composing two new cards in the same scroll frame.
 */
@OptIn(ExperimentalFoundationApi::class)
internal class TwoStageBookListPrefetchStrategy : LazyListPrefetchStrategy {
    private val pendingPrefetches = mutableMapOf<Int, LazyLayoutPrefetchState.PrefetchHandle>()
    private var lastScrollDelta: Float? = null

    override fun LazyListPrefetchScope.onScroll(
        delta: Float,
        layoutInfo: LazyListLayoutInfo,
    ) {
        if (delta == 0f) return

        lastScrollDelta = delta
        val targets = twoStageBookListPrefetchTargets(
            scrollDelta = delta,
            visibleItemIndices = layoutInfo.visibleItemsInfo.map { it.index },
            totalItemCount = layoutInfo.totalItemsCount,
        )
        updateTargets(targets)
        promoteNearestPrefetchWhenNeeded(delta, layoutInfo, targets.firstOrNull())
    }

    override fun LazyListPrefetchScope.onVisibleItemsUpdated(layoutInfo: LazyListLayoutInfo) {
        val delta = lastScrollDelta ?: return
        updateTargets(
            twoStageBookListPrefetchTargets(
                scrollDelta = delta,
                visibleItemIndices = layoutInfo.visibleItemsInfo.map { it.index },
                totalItemCount = layoutInfo.totalItemsCount,
            ),
        )
    }

    override fun NestedPrefetchScope.onNestedPrefetch(firstVisibleItemIndex: Int) = Unit

    private fun LazyListPrefetchScope.updateTargets(targets: List<Int>) {
        val targetSet = targets.toSet()
        (pendingPrefetches.keys - targetSet).forEach { index ->
            pendingPrefetches.remove(index)?.cancel()
        }
        targets.filterNot(pendingPrefetches::contains).forEach { index ->
            pendingPrefetches[index] = schedulePrefetch(index)
        }
    }

    private fun promoteNearestPrefetchWhenNeeded(
        delta: Float,
        layoutInfo: LazyListLayoutInfo,
        nearestTarget: Int?,
    ) {
        if (nearestTarget == null || layoutInfo.visibleItemsInfo.isEmpty()) return

        val scrollingTowardEnd = delta < 0f
        val nearestVisibleItem = if (scrollingTowardEnd) {
            layoutInfo.visibleItemsInfo.last()
        } else {
            layoutInfo.visibleItemsInfo.first()
        }
        val expectedNearestTarget = if (scrollingTowardEnd) {
            nearestVisibleItem.index + 1
        } else {
            nearestVisibleItem.index - 1
        }
        if (nearestTarget != expectedNearestTarget) return

        val distanceToNextItem = if (scrollingTowardEnd) {
            nearestVisibleItem.offset + nearestVisibleItem.size +
                layoutInfo.mainAxisItemSpacing - layoutInfo.viewportEndOffset
        } else {
            layoutInfo.viewportStartOffset - nearestVisibleItem.offset
        }
        val reachesNextItem = if (scrollingTowardEnd) {
            distanceToNextItem.toFloat() < -delta
        } else {
            distanceToNextItem.toFloat() < delta
        }
        if (reachesNextItem) {
            pendingPrefetches[nearestTarget]?.markAsUrgent()
        }
    }
}

internal fun twoStageBookListPrefetchTargets(
    scrollDelta: Float,
    visibleItemIndices: List<Int>,
    totalItemCount: Int,
): List<Int> {
    if (visibleItemIndices.isEmpty()) return emptyList()

    val candidates = if (scrollDelta < 0f) {
        (visibleItemIndices.last() + 1)..(visibleItemIndices.last() + BOOK_LIST_PREFETCH_COUNT)
    } else {
        (visibleItemIndices.first() - 1) downTo
            (visibleItemIndices.first() - BOOK_LIST_PREFETCH_COUNT)
    }
    return candidates.filter { it in 0 until totalItemCount }
}
