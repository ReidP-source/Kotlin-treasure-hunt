package com.example.treasurehunt.game

import com.example.treasurehunt.model.AccuracyMode
import com.example.treasurehunt.model.ActiveHuntState
import com.example.treasurehunt.model.AdventureDifficulty
import com.example.treasurehunt.model.AdventureDifficultyConfig
import com.example.treasurehunt.model.AdventureModeContent
import com.example.treasurehunt.model.GameMode
import com.example.treasurehunt.model.GeoPoint
import com.example.treasurehunt.model.HuntCheckpoint
import com.example.treasurehunt.model.LocationSnapshot
import com.example.treasurehunt.model.LocationValidationResult
import com.example.treasurehunt.model.StoryModeContent
import com.example.treasurehunt.model.TuningConfig
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
object GameEngine {

    fun buildStoryHunt(content: StoryModeContent, accuracyMode: AccuracyMode): ActiveHuntState {
        return ActiveHuntState(
            mode = GameMode.STORY,
            title = content.title,
            subtitle = content.subtitle,
            checkpoints = content.checkpoints.map { checkpoint ->
                HuntCheckpoint(
                    id = checkpoint.id,
                    title = checkpoint.title,
                    clue = checkpoint.clue,
                    hint = checkpoint.hint,
                    target = checkpoint.target,
                    isFinalTarget = checkpoint.isFinalTarget
                )
            },
            accuracyMode = accuracyMode
        )
    }

    fun buildAdventureShell(
        content: AdventureModeContent,
        difficulty: AdventureDifficulty,
        accuracyMode: AccuracyMode
    ): ActiveHuntState {
        return ActiveHuntState(
            mode = GameMode.ADVENTURE,
            title = content.title,
            subtitle = content.subtitle,
            difficulty = difficulty,
            accuracyMode = accuracyMode
        )
    }

    fun generateAdventureCheckpoints(
        start: GeoPoint,
        difficultyConfig: AdventureDifficultyConfig,
        randomInt: (IntRange) -> Int,
        randomBearing: () -> Double
    ): List<HuntCheckpoint> {
        var anchor = start
        return List(difficultyConfig.checkpointCount) { index ->
            val distanceFeet = randomInt(difficultyConfig.distanceFeet.min..difficultyConfig.distanceFeet.max)
            val bearingDegrees = randomBearing()
            val directionLabel = cardinalDirection(bearingDegrees)
            val target = movePoint(
                origin = anchor,
                distanceMeters = distanceFeet * FEET_TO_METERS,
                bearingDegrees = bearingDegrees
            )
            anchor = target
            val finalTarget = index == difficultyConfig.checkpointCount - 1
            HuntCheckpoint(
                id = "${difficultyConfig.difficulty}-${index + 1}",
                title = if (finalTarget) "Treasure Target" else "Checkpoint ${index + 1}",
                clue = if (finalTarget) {
                    "The treasure is hidden somewhere to the $directionLabel. Follow the compass and trust your last steps."
                } else {
                    "Turn toward the $directionLabel and let the compass guide your next move."
                },
                hint = "You still need about $distanceFeet feet before the next checkpoint.",
                target = target,
                isFinalTarget = finalTarget,
                legDistanceFeet = distanceFeet,
                directionLabel = directionLabel
            )
        }
    }

    fun validateLocation(
        current: LocationSnapshot,
        target: GeoPoint,
        accuracyMode: AccuracyMode,
        tuning: TuningConfig
    ): LocationValidationResult {
        val distanceMeters = distanceMeters(
            start = GeoPoint(current.latitude, current.longitude),
            end = target
        )
        val threshold = validationRadiusMeters(
            accuracyMode = accuracyMode,
            reportedAccuracyMeters = current.accuracyMeters,
            tuning = tuning
        )
        val success = distanceMeters <= threshold
        val message = if (success) {
            "Checkpoint found."
        } else {
            "Move closer. You are ${distanceMeters.roundToInt()} m away and need to be within ${threshold.roundToInt()} m."
        }
        return LocationValidationResult(
            success = success,
            distanceMeters = distanceMeters,
            thresholdMeters = threshold,
            message = message
        )
    }

    fun validationRadiusMeters(
        accuracyMode: AccuracyMode,
        reportedAccuracyMeters: Float,
        tuning: TuningConfig
    ): Float {
        return when (accuracyMode) {
            AccuracyMode.PRECISE -> max(
                tuning.preciseBaseRadiusMeters,
                reportedAccuracyMeters * tuning.preciseAccuracyMultiplier
            )

            AccuracyMode.APPROXIMATE -> max(
                tuning.approximateBaseRadiusMeters,
                reportedAccuracyMeters * tuning.approximateAccuracyMultiplier
            )

            AccuracyMode.UNAVAILABLE -> tuning.approximateBaseRadiusMeters
        }
    }

    fun calculateStars(elapsedMs: Long, tuning: TuningConfig): Int {
        val minutes = elapsedMs.milliseconds.inWholeMinutes
        return when {
            minutes <= tuning.threeStarMinutes -> 3
            minutes <= tuning.twoStarMinutes -> 2
            else -> 1
        }
    }

    fun distanceMeters(start: GeoPoint, end: GeoPoint): Float {
        val earthRadius = 6_371_000.0
        val latDelta = Math.toRadians(end.latitude - start.latitude)
        val lonDelta = Math.toRadians(end.longitude - start.longitude)
        val startLat = Math.toRadians(start.latitude)
        val endLat = Math.toRadians(end.latitude)
        val haversine = sin(latDelta / 2) * sin(latDelta / 2) +
            cos(startLat) * cos(endLat) * sin(lonDelta / 2) * sin(lonDelta / 2)
        val c = 2 * atan2(kotlin.math.sqrt(haversine), kotlin.math.sqrt(1 - haversine))
        return (earthRadius * c).toFloat()
    }

    fun absoluteBearingDegrees(start: GeoPoint, end: GeoPoint): Float {
        val startLat = Math.toRadians(start.latitude)
        val endLat = Math.toRadians(end.latitude)
        val deltaLon = Math.toRadians(end.longitude - start.longitude)
        val y = sin(deltaLon) * cos(endLat)
        val x = cos(startLat) * sin(endLat) - sin(startLat) * cos(endLat) * cos(deltaLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing + 360.0) % 360.0).toFloat()
    }

    fun relativeBearingDegrees(deviceHeading: Float, targetBearing: Float): Float {
        return ((targetBearing - deviceHeading + 540f) % 360f) - 180f
    }

    fun formatElapsed(elapsedMs: Long): String {
        val totalSeconds = (elapsedMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun checkpointProgressLabel(activeIndex: Int, totalCount: Int): String {
        val current = if (totalCount == 0) 0 else minOf(activeIndex + 1, totalCount)
        return "Checkpoint $current of $totalCount"
    }

    private fun movePoint(origin: GeoPoint, distanceMeters: Double, bearingDegrees: Double): GeoPoint {
        val bearingRadians = Math.toRadians(bearingDegrees)
        val angularDistance = distanceMeters / 6_371_000.0
        val lat1 = Math.toRadians(origin.latitude)
        val lon1 = Math.toRadians(origin.longitude)

        val lat2 = asin(
            sin(lat1) * cos(angularDistance) +
                cos(lat1) * sin(angularDistance) * cos(bearingRadians)
        )
        val lon2 = lon1 + atan2(
            sin(bearingRadians) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )

        return GeoPoint(
            latitude = Math.toDegrees(lat2),
            longitude = Math.toDegrees(lon2)
        )
    }

    private fun cardinalDirection(bearingDegrees: Double): String {
        val normalized = (bearingDegrees % 360 + 360) % 360
        return when (normalized) {
            in 22.5..<67.5 -> "northeast"
            in 67.5..<112.5 -> "east"
            in 112.5..<157.5 -> "southeast"
            in 157.5..<202.5 -> "south"
            in 202.5..<247.5 -> "southwest"
            in 247.5..<292.5 -> "west"
            in 292.5..<337.5 -> "northwest"
            else -> "north"
        }
    }
}

private const val FEET_TO_METERS = 0.3048
