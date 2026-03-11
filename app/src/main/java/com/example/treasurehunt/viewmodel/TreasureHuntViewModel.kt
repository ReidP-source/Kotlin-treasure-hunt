package com.example.treasurehunt.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehunt.data.GameContentRepository
import com.example.treasurehunt.data.PlayerProgressStore
import com.example.treasurehunt.game.GameEngine
import com.example.treasurehunt.location.CompassManager
import com.example.treasurehunt.location.LocationTracker
import com.example.treasurehunt.model.AccuracyMode
import com.example.treasurehunt.model.ActiveHuntState
import com.example.treasurehunt.model.AdventureDifficulty
import com.example.treasurehunt.model.GameContent
import com.example.treasurehunt.model.GameMode
import com.example.treasurehunt.model.GeoPoint
import com.example.treasurehunt.model.HuntCheckpoint
import com.example.treasurehunt.model.HuntCompletion
import com.example.treasurehunt.model.HuntGuidanceState
import com.example.treasurehunt.model.PermissionStatus
import com.example.treasurehunt.model.PlayerProgress
import com.example.treasurehunt.model.StickerDefinition
import com.example.treasurehunt.model.TreasureHuntUiState
import com.example.treasurehunt.model.bestStarsFor
import com.example.treasurehunt.model.bestTimeFor
import com.example.treasurehunt.model.isEligibleFor
import com.example.treasurehunt.model.level
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

class TreasureHuntViewModel(application: Application) : AndroidViewModel(application) {

    private val contentRepository = GameContentRepository(application)
    private val progressStore = PlayerProgressStore(application)
    private val locationTracker = LocationTracker(application)
    private val compassManager = CompassManager(application)
    private val random = Random(System.currentTimeMillis())

    private val _uiState = MutableStateFlow(TreasureHuntUiState())
    val uiState: StateFlow<TreasureHuntUiState> = _uiState.asStateFlow()
    private val _guidanceState = MutableStateFlow(HuntGuidanceState())
    val guidanceState: StateFlow<HuntGuidanceState> = _guidanceState.asStateFlow()

    private var startupPermissionGateConsumed: Boolean = false

    private var timerJob: Job? = null
    private var timerBaseElapsedMs: Long = 0L
    private var timerStartedRealtimeMs: Long? = null

    init {
        refreshPermissionState()
        observeProgress()
        observeLocation()
        observeCompass()
        loadContent()
    }

    fun refreshPermissionState() {
        val status = PermissionStatus(
            fineGranted = locationTracker.hasFinePermission(),
            coarseGranted = locationTracker.hasCoarsePermission(),
            locationServicesEnabled = locationTracker.isLocationServicesEnabled()
        )
        _uiState.value = _uiState.value.copy(
            permissionStatus = status,
            activeHunt = _uiState.value.activeHunt?.copy(
                accuracyMode = status.accuracyMode,
                statusMessage = permissionMessage(status)
            )
        )
        val hunt = _uiState.value.activeHunt
        val content = _uiState.value.content
        val activeCheckpoint = hunt?.activeCheckpoint
        if (hunt?.hasStarted == true &&
            activeCheckpoint != null &&
            content != null &&
            status.hasAnyLocationPermission
            ) {
            locationTracker.beginTracking(
                                activeCheckpoint.target,
                                status.accuracyMode,
                                content.tuning
                            )
        } else if (!status.hasAnyLocationPermission) {
            locationTracker.stopTracking()
        }
    }

    fun prepareStoryHunt() {
        val content = _uiState.value.content ?: return
        stopTimer()
        locationTracker.stopTracking()
        resetGuidanceState()
        _uiState.value = _uiState.value.copy(
            completion = null,
            stickyMessage = null,
            activeHunt = GameEngine.buildStoryHunt(content.storyMode, _uiState.value.permissionStatus.accuracyMode).copy(
                statusMessage = permissionMessage(_uiState.value.permissionStatus)
            )
        )
    }

    fun prepareAdventureHunt(difficulty: AdventureDifficulty) {
        val content = _uiState.value.content ?: return
        stopTimer()
        locationTracker.stopTracking()
        resetGuidanceState()
        _uiState.value = _uiState.value.copy(
            completion = null,
            stickyMessage = null,
            activeHunt = GameEngine.buildAdventureShell(
                content = content.adventureMode,
                difficulty = difficulty,
                accuracyMode = _uiState.value.permissionStatus.accuracyMode
            ).copy(statusMessage = permissionMessage(_uiState.value.permissionStatus))
        )
    }

    fun startActiveHunt() {
        val state = _uiState.value
        val content = state.content ?: return
        val hunt = state.activeHunt ?: return
        viewModelScope.launch {
            if (!state.permissionStatus.locationServicesEnabled || !state.permissionStatus.hasAnyLocationPermission) {
                updateActiveHunt {
                    it.copy(statusMessage = permissionMessage(_uiState.value.permissionStatus))
                }
                return@launch
            }
            val readyHunt = if (hunt.mode == GameMode.ADVENTURE && hunt.checkpoints.isEmpty()) {
                generateAdventureHunt(hunt, content)
            } else {
                hunt.copy(hasStarted = true, statusMessage = permissionMessage(_uiState.value.permissionStatus))
            }
            updateActiveHunt { readyHunt }
            val activeCheckpoint = readyHunt.activeCheckpoint
            if (activeCheckpoint != null) {
                locationTracker.beginTracking(
                    target = activeCheckpoint.target,
                    accuracyMode = _uiState.value.permissionStatus.accuracyMode,
                    tuning = content.tuning
                )
            }
            startTimer()
        }
    }

    fun abandonActiveHunt() {
        stopTimer()
        locationTracker.stopTracking()
        resetGuidanceState()
        _uiState.value = _uiState.value.copy(activeHunt = null, stickyMessage = null)
    }

    fun clearCompletion() {
        stopTimer()
        locationTracker.stopTracking()
        resetGuidanceState()
        _uiState.value = _uiState.value.copy(completion = null, activeHunt = null, stickyMessage = null)
    }

    fun showHint() {
        _uiState.value.activeHunt ?: return
        stopTimer()
        updateActiveHunt { it.copy(isHintVisible = true) }
    }

    fun dismissHint() {
        val hasStarted = _uiState.value.activeHunt?.hasStarted == true
        updateActiveHunt { it.copy(isHintVisible = false) }
        if (hasStarted && _uiState.value.completion == null) {
            startTimer()
        }
    }

    fun dismissStickyMessage() {
        _uiState.value = _uiState.value.copy(stickyMessage = null)
    }

    fun setCompassActive(active: Boolean) {
        if (active) compassManager.start() else compassManager.stop()
    }

    fun checkFoundIt() {
        val state = _uiState.value
        val hunt = state.activeHunt ?: return
        val checkpoint = hunt.activeCheckpoint ?: return
        val content = state.content ?: return
        if (!hunt.hasStarted) return
        viewModelScope.launch {
            val location = runCatching { locationTracker.requestCurrentLocation() }.getOrNull()
            if (location == null) {
                updateActiveHunt {
                    it.copy(statusMessage = "Unable to fetch your current location. Try again in a more open area.")
                }
                return@launch
            }
            val validation = GameEngine.validateLocation(
                current = location,
                target = checkpoint.target,
                accuracyMode = _uiState.value.permissionStatus.accuracyMode,
                tuning = content.tuning
            )
            if (!validation.success) {
                updateActiveHunt {
                    it.copy(
                        statusMessage = validation.message
                    )
                }
                return@launch
            }

            val updatedCheckpoints = hunt.checkpoints.mapIndexed { index, existing ->
                if (index == hunt.activeIndex) existing.copy(completed = true) else existing
            }
            if (checkpoint.isFinalTarget) {
                completeHunt(
                    hunt = hunt.copy(checkpoints = updatedCheckpoints),
                    finalCheckpoint = checkpoint,
                    content = content
                )
            } else {
                val nextIndex = hunt.activeIndex + 1
                val nextCheckpoint = updatedCheckpoints.getOrNull(nextIndex)
                updateActiveHunt {
                    hunt.copy(
                        checkpoints = updatedCheckpoints,
                        activeIndex = nextIndex,
                        statusMessage = "Checkpoint cleared. ${nextCheckpoint?.title ?: "Keep going."}"
                    )
                }
                if (nextCheckpoint != null) {
                    locationTracker.beginTracking(
                        target = nextCheckpoint.target,
                        accuracyMode = _uiState.value.permissionStatus.accuracyMode,
                        tuning = content.tuning
                    )
                }
            }
        }
    }

    fun consumeStartupPermissionGate(hasAnyLocationPermission: Boolean): Boolean {
        if (startupPermissionGateConsumed) return false
        startupPermissionGateConsumed = true
        return !hasAnyLocationPermission
    }

    private fun observeProgress() {
        viewModelScope.launch {
            progressStore.progressFlow.collectLatest { progress ->
                _uiState.value = _uiState.value.copy(progress = progress)
            }
        }
    }

    private fun observeLocation() {
        viewModelScope.launch {
            locationTracker.location.collectLatest { snapshot ->
                val hunt = _uiState.value.activeHunt ?: return@collectLatest
                val target = hunt.activeCheckpoint?.target
                val distance = if (snapshot != null && target != null) {
                    GameEngine.distanceMeters(
                        start = GeoPoint(snapshot.latitude, snapshot.longitude),
                        end = target
                    )
                } else {
                    null
                }
                val bearing = if (snapshot != null && target != null) {
                    GameEngine.absoluteBearingDegrees(
                        start = GeoPoint(snapshot.latitude, snapshot.longitude),
                        end = target
                    )
                } else {
                    null
                }
                val current = _guidanceState.value
                val next = current.copy(
                    currentLocation = snapshot,
                    distanceToTargetMeters = distance,
                    absoluteBearingToTarget = bearing
                )
                if (!guidanceEquals(current, next)) {
                    _guidanceState.value = next
                }
            }
        }
    }
    // Experimental API -- Probably stable but we'll see...
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeCompass() {
        viewModelScope.launch {
            compassManager.headingDegrees
                .map { heading -> heading?.roundToInt()?.toFloat() }
                .sample(250L)
                .distinctUntilChanged()
                .collectLatest { heading ->
                val nextHeading = if (_uiState.value.activeHunt?.hasStarted == true) heading else null
                if (_guidanceState.value.headingDegrees != nextHeading) {
                    _guidanceState.value = _guidanceState.value.copy(headingDegrees = nextHeading)
                }
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            val content = runCatching { contentRepository.load() }.getOrNull()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                content = content,
                stickyMessage = if (content == null) "Unable to load packaged game data." else null
            )
        }
    }

    private suspend fun generateAdventureHunt(
        hunt: ActiveHuntState,
        content: GameContent
    ): ActiveHuntState {
        updateActiveHunt { hunt.copy(generationPending = true, statusMessage = "Generating your route...") }
        val current = locationTracker.requestCurrentLocation()
        if (current == null) {
            updateActiveHunt {
                it.copy(generationPending = false, statusMessage = "Adventure mode needs your current outdoor location to generate a route.")
            }
            return hunt
        }
        val config = content.adventureMode.difficulties.firstOrNull { it.level == hunt.difficulty } ?: content.adventureMode.difficulties.first()
        val checkpoints = GameEngine.generateAdventureCheckpoints(
            start = GeoPoint(current.latitude, current.longitude),
            difficultyConfig = config,
            randomInt = { range -> random.nextInt(range.first, range.last + 1) },
            randomBearing = { random.nextDouble(0.0, 360.0) }
        )
        return hunt.copy(
            checkpoints = checkpoints,
            hasStarted = true,
            generationPending = false,
            currentLocation = current,
            statusMessage = permissionMessage(_uiState.value.permissionStatus)
        )
    }

    private suspend fun completeHunt(
        hunt: ActiveHuntState,
        finalCheckpoint: HuntCheckpoint,
        content: GameContent
    ) {
        stopTimer()
        locationTracker.stopTracking()
        val stars = GameEngine.calculateStars(hunt.elapsedMs, content.tuning)
        val currentProgress = _uiState.value.progress
        val reward = selectRewardSticker(hunt, currentProgress, content)
        val updatedProgress = mergeProgress(currentProgress, hunt, stars, reward)
        progressStore.update { updatedProgress }
        _uiState.value = _uiState.value.copy(
            progress = updatedProgress,
            activeHunt = null,
            completion = HuntCompletion(
                mode = hunt.mode,
                difficulty = hunt.difficulty,
                elapsedMs = hunt.elapsedMs,
                stars = stars,
                finalCheckpointTitle = finalCheckpoint.title,
                finalCheckpointHint = finalCheckpoint.hint,
                finalCheckpointLocation = formatTargetLocation(finalCheckpoint.target),
                rewardStickerId = reward?.id,
                rewardWasNew = reward != null,
                message = if (hunt.mode == GameMode.STORY) {
                    "Congratulations! Grandpa's final game ends with a sweet reward."
                } else {
                    "Congratulations! Treasure secured. The trail is complete."
                }
            )
        )
    }

    private fun mergeProgress(
        current: PlayerProgress,
        hunt: ActiveHuntState,
        stars: Int,
        reward: StickerDefinition?
    ): PlayerProgress {
        val unlocked = current.unlockedStickerIds.toMutableSet()
        reward?.id?.let(unlocked::add)
        return when (hunt.mode) {
            GameMode.STORY -> current.copy(
                unlockedStickerIds = unlocked,
                storyCompleted = true,
                storyBestTimeMs = current.storyBestTimeMs?.let { minOf(it, hunt.elapsedMs) } ?: hunt.elapsedMs,
                storyBestStars = maxOf(current.storyBestStars, stars)
            )

            GameMode.ADVENTURE -> {
                val difficulty = requireNotNull(hunt.difficulty)
                current.copy(
                    unlockedStickerIds = unlocked,
                    adventureBestTimesMs = current.adventureBestTimesMs + (
                        difficulty.storageKey to (current.bestTimeFor(difficulty)?.let { minOf(it, hunt.elapsedMs) } ?: hunt.elapsedMs)
                        ),
                    adventureBestStars = current.adventureBestStars + (
                        difficulty.storageKey to maxOf(current.bestStarsFor(difficulty), stars)
                        )
                )
            }
        }
    }

    private fun selectRewardSticker(
        hunt: ActiveHuntState,
        progress: PlayerProgress,
        content: GameContent
    ): StickerDefinition? {
        return when (hunt.mode) {
            GameMode.STORY -> {
                if (progress.storyCompleted) {
                    null
                } else {
                    content.stickers.firstOrNull { it.id == content.storyMode.rewardStickerId }
                        ?.takeUnless { progress.unlockedStickerIds.contains(it.id) }
                }
            }

            GameMode.ADVENTURE -> {
                val difficulty = hunt.difficulty ?: return null
                val candidates = content.stickers
                    .filter { it.isEligibleFor(difficulty) && !progress.unlockedStickerIds.contains(it.id) }
                    .ifEmpty {
                        content.stickers.filter { it.isEligibleFor(difficulty) }
                    }
                if (candidates.isEmpty()) return null
                weightedStickerRoll(candidates)
            }
        }
    }

    private fun weightedStickerRoll(candidates: List<StickerDefinition>): StickerDefinition {
        val totalWeight = candidates.sumOf { it.rarityWeight }
        val roll = random.nextInt(totalWeight)
        var cursor = 0
        return candidates.first { sticker ->
            cursor += sticker.rarityWeight
            roll < cursor
        }
    }

    private fun permissionMessage(status: PermissionStatus): String? {
        return when {
            !status.locationServicesEnabled -> "Turn on device location services to begin the hunt."
            !status.hasAnyLocationPermission -> "Unable to access your location. Please enable location services."
            status.accuracyMode == AccuracyMode.APPROXIMATE -> "Approximate location enabled. Precise location gives the best scoring accuracy."
            else -> null
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        val hunt = _uiState.value.activeHunt ?: return
        timerBaseElapsedMs = hunt.elapsedMs
        timerStartedRealtimeMs = SystemClock.elapsedRealtime()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                updateActiveHunt { hunt ->
                    if (hunt.hasStarted && !hunt.isHintVisible) {
                        val startedAt = timerStartedRealtimeMs ?: return@updateActiveHunt hunt
                        val now = SystemClock.elapsedRealtime()
                        val elapsedMs = timerBaseElapsedMs + (now - startedAt)
                        if (elapsedMs == hunt.elapsedMs) hunt else hunt.copy(elapsedMs = elapsedMs)
                    } else {
                        hunt
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        val startedAt = timerStartedRealtimeMs
        if (startedAt != null) {
            val now = SystemClock.elapsedRealtime()
            val finalElapsedMs = timerBaseElapsedMs + (now - startedAt)
            timerBaseElapsedMs = finalElapsedMs
            updateActiveHunt { hunt ->
                if (finalElapsedMs == hunt.elapsedMs) hunt else hunt.copy(elapsedMs = finalElapsedMs)
            }
        }
        timerJob?.cancel()
        timerJob = null
        timerStartedRealtimeMs = null
    }

    private fun updateActiveHunt(transform: (ActiveHuntState) -> ActiveHuntState) {
        val current = _uiState.value.activeHunt ?: return
        val next = transform(current)
        if (next != current) {
            _uiState.value = _uiState.value.copy(activeHunt = next)
        }
    }

    private fun resetGuidanceState() {
        if (_guidanceState.value != HuntGuidanceState()) {
            _guidanceState.value = HuntGuidanceState()
        }
    }

    private fun guidanceEquals(previous: HuntGuidanceState, next: HuntGuidanceState): Boolean {
        return previous.currentLocation == next.currentLocation &&
            previous.headingDegrees == next.headingDegrees &&
            nearlyEqual(previous.distanceToTargetMeters, next.distanceToTargetMeters, 1f) &&
            bearingDeltaWithin(previous.absoluteBearingToTarget, next.absoluteBearingToTarget, 2f)
    }

    private fun nearlyEqual(previous: Float?, next: Float?, epsilon: Float): Boolean {
        if (previous == null || next == null) return previous == next
        return abs(previous - next) <= epsilon
    }

    private fun bearingDeltaWithin(previous: Float?, next: Float?, epsilon: Float): Boolean {
        if (previous == null || next == null) return previous == next
        val delta = abs(((next - previous + 540f) % 360f) - 180f)
        return delta <= epsilon
    }

    private fun formatTargetLocation(point: GeoPoint): String {
        val lat = String.format(Locale.US, "%.5f", point.latitude)
        val lon = String.format(Locale.US, "%.5f", point.longitude)
        return "Lat $lat, Lon $lon"
    }
}
