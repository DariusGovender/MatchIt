import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    enum class BoardSize {
        EASY, MEDIUM, HARD
    }

    // Test on calculate score method
    private fun calculateScore(moves: Int, totalTime: Double, boardSize: BoardSize): Int {
        val baseScore = 1000

        val timeBonusMultiplier = when (boardSize) {
            BoardSize.EASY -> 1.0
            BoardSize.MEDIUM -> 1.5
            BoardSize.HARD -> 2.0
        }
        val timeBonus = (1000 / totalTime * timeBonusMultiplier).toInt()

        val minimumMovesToWin = when (boardSize) {
            BoardSize.EASY -> 4
            BoardSize.MEDIUM -> 9
            BoardSize.HARD -> 12
        }

        val movePenalty = when (boardSize) {
            BoardSize.EASY -> (moves - minimumMovesToWin) * 5
            BoardSize.MEDIUM -> (moves - minimumMovesToWin) * 5
            BoardSize.HARD -> (moves - minimumMovesToWin) * 5
        }.coerceAtLeast(0)

        return baseScore + timeBonus - movePenalty
    }

    @Test
    fun testEasyLevelWithOptimalMoves() {
        val moves = 4
        val totalTime = 50.0
        val score = calculateScore(moves, totalTime, BoardSize.EASY)
        assertEquals(1020, score)
    }

    @Test
    fun testMediumLevelWithFewMoves() {
        val moves = 9
        val totalTime = 100.0
        val score = calculateScore(moves, totalTime, BoardSize.MEDIUM)
        assertEquals(1015, score)
    }

    @Test
    fun testHardLevelWithExtraMoves() {
        val moves = 15
        val totalTime = 150.0
        val score = calculateScore(moves, totalTime, BoardSize.HARD)
        assertEquals(998, score)
    }

    @Test
    fun testEasyLevelWithExtraMoves() {
        val moves = 10
        val totalTime = 75.0
        val score = calculateScore(moves, totalTime, BoardSize.EASY)
        assertEquals(983, score)
    }
}
