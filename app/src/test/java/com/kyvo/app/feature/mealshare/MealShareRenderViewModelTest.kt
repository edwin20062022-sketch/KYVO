package com.kyvo.app.feature.mealshare

import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.mealshare.domain.render.MealShareImageRenderer
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderRequest
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult
import com.kyvo.app.feature.mealshare.presentation.MealShareRenderState
import com.kyvo.app.feature.mealshare.presentation.MealShareRenderViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MealShareRenderViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    @After fun tearDown() = kotlinx.coroutines.Dispatchers.resetMain()

    @Test
    fun successfulRenderReturnsAFileReferenceAndDraftChangesInvalidateIt() = runTest(dispatcher) {
        val viewModel = MealShareRenderViewModel(FakeRenderer())
        val draft = draft()
        viewModel.render(draft)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is MealShareRenderState.Success)
        viewModel.invalidateIfStale("changed")
        assertEquals(MealShareRenderState.Idle, viewModel.state.value)
    }

    @Test
    fun failedRenderExposesControlledError() = runTest(dispatcher) {
        val viewModel = MealShareRenderViewModel(FakeRenderer(Result.failure(IllegalStateException("decode failed"))))
        viewModel.render(draft())
        advanceUntilIdle()
        assertEquals("decode failed", (viewModel.state.value as MealShareRenderState.Error).message)
    }

    @Test
    fun doubleTapWhileRenderingStartsOneRender() = runTest(dispatcher) {
        val renderer = BlockingRenderer()
        val viewModel = MealShareRenderViewModel(renderer)
        viewModel.render(draft())
        runCurrent()
        viewModel.render(draft())
        assertEquals(1, renderer.calls)
        renderer.result.complete(Result.success(renderResult()))
        advanceUntilIdle()
        assertTrue(viewModel.state.value is MealShareRenderState.Success)
    }

    @Test
    fun draftChangeWhileRenderingDiscardsTheStaleResult() = runTest(dispatcher) {
        val renderer = BlockingRenderer()
        val viewModel = MealShareRenderViewModel(renderer)
        val initial = draft()
        viewModel.render(initial)
        runCurrent()
        viewModel.invalidateIfStale(initial.copy(template = MealShareTemplate.EDITORIAL).renderFingerprint())
        renderer.result.complete(Result.success(renderResult()))
        advanceUntilIdle()
        assertEquals(MealShareRenderState.Idle, viewModel.state.value)
    }

    private fun draft() = MealShareDraft(
        photoUri = "file:///cache/photo.jpg",
        items = listOf(MealItem("one", "Pollo", 100.0, "g", 620, 42, 68, 18)),
        template = MealShareTemplate.MINIMAL,
    )

    private class FakeRenderer(private val response: Result<MealShareRenderResult> = Result.success(renderResult())) : MealShareImageRenderer {
        override suspend fun render(request: MealShareRenderRequest): Result<MealShareRenderResult> = response
    }

    private class BlockingRenderer : MealShareImageRenderer {
        var calls = 0
        val result = CompletableDeferred<Result<MealShareRenderResult>>()
        override suspend fun render(request: MealShareRenderRequest): Result<MealShareRenderResult> {
            calls++
            return result.await()
        }
    }
}

private fun renderResult() = MealShareRenderResult(File("/cache/render.jpg"), 1080, 1920, "fingerprint")
