package se.payerl.mobilenotes.util

/**
 * Manager for position-based ordering with gap strategy.
 *
 * This provides a reusable solution for ordering any entities (Folders, Notes, NoteItems)
 * using position values with large gaps for efficient insertions.
 *
 * Strategy:
 * - Initial gap between items: 1,000,000
 * - When inserting between items: use midpoint position
 * - ~20 levels of binary division before gap exhaustion
 * - Lazy normalization only when UNIQUE constraint violation occurs
 */
object PositionManager {

    /**
     * Gap between positions for new items added at the end.
     * Large gap (1M) allows ~20 levels of insertions between items.
     */
    const val POSITION_GAP = 1_000_000L

    /**
     * Calculate position for a new item added at the end of a list.
     *
     * @param currentMaxPosition The highest position in the current list, or null if empty
     * @return Position value with gap from max position (or 0 if first item)
     */
    fun calculatePositionForNewItem(currentMaxPosition: Long?): Long {
        return (currentMaxPosition ?: -POSITION_GAP) + POSITION_GAP
    }

    /**
     * Calculate position for inserting an item between two existing items.
     *
     * @param previousPosition Position of item before insertion point (or 0 if inserting at start)
     * @param nextPosition Position of item after insertion point (or Long.MAX_VALUE if inserting at end)
     * @return Midpoint position between the two items
     *
     * Note: If gap < 2, this will eventually cause UNIQUE constraint violation,
     * triggering lazy normalization in the storage layer.
     */
    fun calculatePositionBetween(previousPosition: Long, nextPosition: Long): Long {
        require(previousPosition < nextPosition) {
            "previousPosition ($previousPosition) must be less than nextPosition ($nextPosition)"
        }
        return previousPosition + (nextPosition - previousPosition) / 2
    }

    /**
     * Re-normalize all positions with fresh gaps.
     * Should be called when UNIQUE constraint violations occur (rare).
     *
     * @param itemCount Number of items to normalize
     * @return List of new positions (0, 1000000, 2000000, ...)
     */
    fun normalizePositions(itemCount: Int): List<Long> {
        return List(itemCount) { index -> index * POSITION_GAP }
    }
}

