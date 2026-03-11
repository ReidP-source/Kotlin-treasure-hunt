package com.example.treasurehunt.navigation

import com.example.treasurehunt.model.AdventureDifficulty
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
object TreasureHuntRoutes {
    const val Permissions = "permissions"
    const val Home = "home"
    const val GeneratingStory = "generating_story"
    const val GeneratingAdventurePattern = "generating_adventure/{difficulty}"
    const val ReturningHome = "returning_home"
    const val Story = "story"
    const val Completion = "completion"
    const val Stickers = "stickers"
    const val AdventurePattern = "adventure/{difficulty}"

    fun adventure(difficulty: AdventureDifficulty): String = "adventure/${difficulty.storageKey}"
    fun generatingAdventure(difficulty: AdventureDifficulty): String = "generating_adventure/${difficulty.storageKey}"
}
