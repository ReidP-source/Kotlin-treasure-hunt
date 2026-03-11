package com.example.treasurehunt.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class GameMode {
    STORY,
    ADVENTURE
}

enum class AdventureDifficulty(val storageKey: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    companion object {
        fun fromStorageKey(value: String): AdventureDifficulty =
            entries.firstOrNull { it.storageKey == value.lowercase() } ?: EASY
    }
}

enum class AccuracyMode {
    PRECISE,
    APPROXIMATE,
    UNAVAILABLE
}

@Serializable
data class GameContent(
    val storyMode: StoryModeContent,
    val adventureMode: AdventureModeContent,
    val stickers: List<StickerDefinition>,
    val tuning: TuningConfig,
    val uiText: UiText
)

@Serializable
data class StoryModeContent(
    val title: String,
    val subtitle: String,
    val rewardStickerId: String,
    val checkpoints: List<StoryCheckpointContent>
)

@Serializable
data class StoryCheckpointContent(
    val id: String,
    val title: String,
    val clue: String,
    val hint: String,
    val target: GeoPoint,
    val isFinalTarget: Boolean = false
)

@Serializable
data class AdventureModeContent(
    val title: String,
    val subtitle: String,
    val difficulties: List<AdventureDifficultyConfig>
)

@Serializable
data class AdventureDifficultyConfig(
    val difficulty: String,
    val checkpointCount: Int,
    val distanceFeet: DistanceRangeFeet
)

@Serializable
data class DistanceRangeFeet(
    val min: Int,
    val max: Int
)

@Serializable
data class StickerDefinition(
    val id: String,
    val name: String,
    val description: String,
    val collectionLabel: String,
    val rarityWeight: Int,
    val difficulties: List<String> = emptyList(),
    val storyReward: Boolean = false
)

@Serializable
data class TuningConfig(
    val preciseBaseRadiusMeters: Float,
    val approximateBaseRadiusMeters: Float,
    val preciseAccuracyMultiplier: Float,
    val approximateAccuracyMultiplier: Float,
    val nearDistanceMeters: Float,
    val midDistanceMeters: Float,
    val farUpdateIntervalMillis: Long,
    val midUpdateIntervalMillis: Long,
    val nearUpdateIntervalMillis: Long,
    val threeStarMinutes: Int,
    val twoStarMinutes: Int
)

@Serializable
data class UiText(
    val storyCardTitle: String,
    val storyCardSubtitle: String,
    val storyCardDetail: String,
    val adventureCardTitle: String,
    val adventureCardSubtitle: String,
    val howToPlayTitle: String,
    val howToPlaySteps: List<String>,
    val permissionsTitle: String,
    val permissionsBody: String,
    val approximateBody: String
)

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class PlayerProgress(
    val unlockedStickerIds: Set<String> = emptySet(),
    val storyCompleted: Boolean = false,
    val storyBestTimeMs: Long? = null,
    val storyBestStars: Int = 0,
    val adventureBestTimesMs: Map<String, Long> = emptyMap(),
    val adventureBestStars: Map<String, Int> = emptyMap()
)

data class HuntCheckpoint(
    val id: String,
    val title: String,
    val clue: String,
    val hint: String,
    val target: GeoPoint,
    val isFinalTarget: Boolean,
    val completed: Boolean = false,
    val legDistanceFeet: Int? = null,
    val directionLabel: String? = null
)

data class ActiveHuntState(
    val mode: GameMode,
    val title: String,
    val subtitle: String,
    val difficulty: AdventureDifficulty? = null,
    val checkpoints: List<HuntCheckpoint> = emptyList(),
    val activeIndex: Int = 0,
    val hasStarted: Boolean = false,
    val generationPending: Boolean = false,
    val elapsedMs: Long = 0L,
    val isHintVisible: Boolean = false,
    val statusMessage: String? = null,
    val currentLocation: LocationSnapshot? = null,
    val currentHeadingDegrees: Float? = null,
    val distanceToTargetMeters: Float? = null,
    val absoluteBearingToTarget: Float? = null,
    val accuracyMode: AccuracyMode = AccuracyMode.UNAVAILABLE
) {
    val activeCheckpoint: HuntCheckpoint?
        get() = checkpoints.getOrNull(activeIndex)
}

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
)

data class PermissionStatus(
    val fineGranted: Boolean = false,
    val coarseGranted: Boolean = false,
    val locationServicesEnabled: Boolean = true
) {
    val hasAnyLocationPermission: Boolean
        get() = fineGranted || coarseGranted

    val accuracyMode: AccuracyMode
        get() = when {
            fineGranted -> AccuracyMode.PRECISE
            coarseGranted -> AccuracyMode.APPROXIMATE
            else -> AccuracyMode.UNAVAILABLE
        }
}

data class HuntCompletion(
    val mode: GameMode,
    val difficulty: AdventureDifficulty? = null,
    val elapsedMs: Long,
    val stars: Int,
    val finalCheckpointTitle: String,
    val finalCheckpointHint: String,
    val finalCheckpointLocation: String,
    val rewardStickerId: String? = null,
    val rewardWasNew: Boolean = false,
    val message: String
)

data class LocationValidationResult(
    val success: Boolean,
    val distanceMeters: Float,
    val thresholdMeters: Float,
    val message: String
)

data class TreasureHuntUiState(
    val isLoading: Boolean = true,
    val content: GameContent? = null,
    val permissionStatus: PermissionStatus = PermissionStatus(),
    val progress: PlayerProgress = PlayerProgress(),
    val activeHunt: ActiveHuntState? = null,
    val completion: HuntCompletion? = null,
    val stickyMessage: String? = null
)

data class HuntGuidanceState(
    val currentLocation: LocationSnapshot? = null,
    val headingDegrees: Float? = null,
    val distanceToTargetMeters: Float? = null,
    val absoluteBearingToTarget: Float? = null
)

val AdventureDifficultyConfig.level: AdventureDifficulty
    get() = AdventureDifficulty.fromStorageKey(difficulty)

fun GameMode.displayName(): String = when (this) {
    GameMode.STORY -> "Story Mode"
    GameMode.ADVENTURE -> "Adventure Mode"
}

fun AdventureDifficulty.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

fun StickerDefinition.isEligibleFor(difficulty: AdventureDifficulty): Boolean {
    if (storyReward) return false
    return difficulties.map(String::lowercase).contains(difficulty.storageKey)
}

fun PlayerProgress.bestTimeFor(difficulty: AdventureDifficulty): Long? = adventureBestTimesMs[difficulty.storageKey]

fun PlayerProgress.bestStarsFor(difficulty: AdventureDifficulty): Int = adventureBestStars[difficulty.storageKey] ?: 0

@Serializable
data class ContentEnvelope(
    @SerialName("storyMode") val storyMode: StoryModeContent,
    @SerialName("adventureMode") val adventureMode: AdventureModeContent,
    @SerialName("stickers") val stickers: List<StickerDefinition>,
    @SerialName("tuning") val tuning: TuningConfig,
    @SerialName("uiText") val uiText: UiText
) {
    fun toGameContent(): GameContent = GameContent(
        storyMode = storyMode,
        adventureMode = adventureMode,
        stickers = stickers,
        tuning = tuning,
        uiText = uiText
    )
}
