package com.example.matchgame.models

import com.example.matchgame.utils.DEFAULT_ICONS

class MemoryGame(
    private val boardSize: BoardSize,
    private val customImages: List<String>?
) {

    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null

    init {
        if(customImages == null) {
            val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumOfPairs())
            val randomisedImages = (chosenImages + chosenImages).shuffled()
            cards = randomisedImages.map { MemoryCard(it) }
        }
        else {
            val randomisedImages = (customImages + customImages).shuffled()
            cards = randomisedImages.map { MemoryCard(it.hashCode(),it) }
        }

    }

    fun flipCard(position: Int): Boolean {
        numCardFlips++
        val card = cards[position]
        var foundMatch = false

        if(indexOfSingleSelectedCard == null) {
            // 0 or 2 cards flipped over
            restoreCards()
            indexOfSingleSelectedCard = position
        }
        else{
            // 1 card flipped over
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if(cards[position1].identifier != cards[position2].identifier) {
            return false
        }
        else{
            cards[position1].isMatched = true
            cards[position2].isMatched = true
            numPairsFound++
            return true
        }
    }

    private fun restoreCards() {
        for(card in cards) {
            if(!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumOfPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numCardFlips / 2
    }
}