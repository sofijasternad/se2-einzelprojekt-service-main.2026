package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import org.mockito.Mockito.`when` as whenever

class LeaderboardControllerTests {

    private lateinit var mockedService: GameResultService
    private lateinit var controller: LeaderboardController

    @BeforeEach
    fun setup() {
        mockedService = mock<GameResultService>()
        controller = LeaderboardController(mockedService)
    }

    // ↓ GEÄNDERT: getLeaderboard(null) statt getLeaderboard(), .body!! für ResponseEntity
    @Test
    fun test_getLeaderboard_correctScoreSorting() {
        val first = GameResult(1, "first", 20, 20.0)
        val second = GameResult(2, "second", 15, 10.0)
        val third = GameResult(3, "third", 10, 15.0)

        whenever(mockedService.getGameResults()).thenReturn(listOf(second, first, third))

        val res = controller.getLeaderboard(null)

        verify(mockedService).getGameResults()
        assertEquals(3, res.body!!.size)
        assertEquals(first, res.body!![0])
        assertEquals(second, res.body!![1])
        assertEquals(third, res.body!![2])
    }

    // ↓ GEÄNDERT: ID-Sortierung → Zeit-Sortierung (kürzere Zeit = besser)
    @Test
    fun test_getLeaderboard_sameScore_CorrectTimeSorting() {
        val first = GameResult(3, "first", 20, 10.0)   // kürzeste Zeit → Platz 1
        val second = GameResult(1, "second", 20, 15.0)
        val third = GameResult(2, "third", 20, 20.0)   // längste Zeit → Platz 3

        whenever(mockedService.getGameResults()).thenReturn(listOf(second, third, first))

        val res = controller.getLeaderboard(null)

        verify(mockedService).getGameResults()
        assertEquals(3, res.body!!.size)
        assertEquals(first, res.body!![0])
        assertEquals(second, res.body!![1])
        assertEquals(third, res.body!![2])
    }

    // ↓ NEU: rank=null → ganzes Leaderboard
    @Test
    fun test_getLeaderboard_noRank_returnsFullList() {
        val results = listOf(
            GameResult(1, "player1", 20, 10.0),
            GameResult(2, "player2", 15, 12.0)
        )
        whenever(mockedService.getGameResults()).thenReturn(results)

        val res = controller.getLeaderboard(null)

        assertEquals(200, res.statusCode.value())
        assertEquals(2, res.body!!.size)
    }

    // ↓ NEU: gültiger rank → Fenster ±3 Plätze
    @Test
    fun test_getLeaderboard_validRank_returnsWindow() {
        val results = (1..7).map { GameResult(it.toLong(), "player$it", 100 - it, it.toDouble()) }
        whenever(mockedService.getGameResults()).thenReturn(results)

        val res = controller.getLeaderboard(4)

        assertEquals(200, res.statusCode.value())
        assertEquals(7, res.body!!.size) // Plätze 1-7 (alle im Fenster)
    }

    // ↓ NEU: rank am Rand → Fenster wird abgeschnitten
    @Test
    fun test_getLeaderboard_rankAtEdge_clipsWindow() {
        val results = (1..5).map { GameResult(it.toLong(), "player$it", 100 - it, it.toDouble()) }
        whenever(mockedService.getGameResults()).thenReturn(results)

        val res = controller.getLeaderboard(1)

        assertEquals(200, res.statusCode.value())
        assertEquals(4, res.body!!.size) // Plätze 1-4
    }

    // ↓ NEU: rank zu groß → HTTP 400
    @Test
    fun test_getLeaderboard_rankTooLarge_returns400() {
        whenever(mockedService.getGameResults()).thenReturn(listOf(
            GameResult(1, "player1", 10, 5.0)
        ))

        val res = controller.getLeaderboard(99)

        assertEquals(400, res.statusCode.value())
    }

    // ↓ NEU: rank negativ → HTTP 400
    @Test
    fun test_getLeaderboard_rankNegative_returns400() {
        whenever(mockedService.getGameResults()).thenReturn(listOf(
            GameResult(1, "player1", 10, 5.0)
        ))

        val res = controller.getLeaderboard(-1)

        assertEquals(400, res.statusCode.value())
    }

    // ↓ NEU: rank = 0 → HTTP 400
    @Test
    fun test_getLeaderboard_rankZero_returns400() {
        whenever(mockedService.getGameResults()).thenReturn(listOf(
            GameResult(1, "player1", 10, 5.0)
        ))

        val res = controller.getLeaderboard(0)

        assertEquals(400, res.statusCode.value())
    }
}