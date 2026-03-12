package at.aau.serg.controllers

import at.aau.serg.models.GameResult
import at.aau.serg.services.GameResultService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val gameResultService: GameResultService
) {

    @GetMapping
    fun getLeaderboard(@RequestParam(required = false) rank: Int?): ResponseEntity<List<GameResult>> {
        val sorted = gameResultService.getGameResults()
            .sortedWith(compareBy({ -it.score }, { it.timeInSeconds })) // ← timeInSeconds statt id

        if (rank == null) {
            return ResponseEntity.ok(sorted) // kein rank → ganzes Leaderboard
        }

        if (rank < 1 || rank > sorted.size) {
            return ResponseEntity.badRequest().build() // ungültiger rank → HTTP 400
        }

        val index = rank - 1                          // rank ist 1-basiert, Index 0-basiert
        val from = maxOf(0, index - 3)                // max. 3 Plätze oberhalb
        val to = minOf(sorted.size - 1, index + 3)    // max. 3 Plätze unterhalb

        return ResponseEntity.ok(sorted.subList(from, to + 1))
    }

}