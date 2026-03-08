package com.emulnk.core

import com.emulnk.model.ScreenTarget
import com.emulnk.model.StoreWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

sealed class EditAction {
    data class Move(
        val widgetId: String, val screen: ScreenTarget,
        val oldX: Int, val oldY: Int, val newX: Int, val newY: Int
    ) : EditAction()

    data class Resize(
        val widgetId: String, val screen: ScreenTarget,
        val oldW: Int, val oldH: Int, val newW: Int, val newH: Int
    ) : EditAction()

    data class ToggleEnabled(
        val widgetId: String, val screen: ScreenTarget,
        val oldEnabled: Boolean, val newEnabled: Boolean
    ) : EditAction()

    data class ChangeAlpha(
        val widgetId: String, val screen: ScreenTarget,
        val oldAlpha: Float, val newAlpha: Float
    ) : EditAction()

    data class AddWidget(
        val widget: StoreWidget, val screen: ScreenTarget
    ) : EditAction()

    data class RemoveWidget(
        val widget: StoreWidget, val screen: ScreenTarget
    ) : EditAction()
}

data class BuilderState(
    val isEditMode: Boolean = false,
    val isBuilderMode: Boolean = false,
    val selectedWidgetId: String? = null,
    val selectedScreen: ScreenTarget? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

class BuilderSession(isBuilderMode: Boolean = false) {

    private val _state = MutableStateFlow(BuilderState(isBuilderMode = isBuilderMode))
    val state: StateFlow<BuilderState> = _state

    private val undoStack = mutableListOf<EditAction>()
    private val redoStack = mutableListOf<EditAction>()

    fun executeAction(action: EditAction) {
        undoStack.addCapped(action)
        redoStack.clear()
        updateUndoRedoState()
    }

    fun undo(): EditAction? {
        val action = undoStack.removeLastOrNull() ?: return null
        redoStack.addCapped(action)
        updateUndoRedoState()
        return action
    }

    fun redo(): EditAction? {
        val action = redoStack.removeLastOrNull() ?: return null
        undoStack.addCapped(action)
        updateUndoRedoState()
        return action
    }

    fun commitMove(widgetId: String, screen: ScreenTarget, oldX: Int, oldY: Int, newX: Int, newY: Int) {
        if (!_state.value.isEditMode) return
        if (oldX == newX && oldY == newY) return
        executeAction(EditAction.Move(widgetId, screen, oldX, oldY, newX, newY))
    }

    fun commitResize(widgetId: String, screen: ScreenTarget, oldW: Int, oldH: Int, newW: Int, newH: Int) {
        if (!_state.value.isEditMode) return
        if (oldW == newW && oldH == newH) return
        executeAction(EditAction.Resize(widgetId, screen, oldW, oldH, newW, newH))
    }

    fun selectWidget(id: String?, screen: ScreenTarget?) {
        _state.update { it.copy(selectedWidgetId = id, selectedScreen = screen) }
    }

    fun setEditMode(editing: Boolean) {
        _state.update { it.copy(isEditMode = editing) }
        if (!editing) {
            undoStack.clear()
            redoStack.clear()
            updateUndoRedoState()
        }
    }

    private fun updateUndoRedoState() {
        _state.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
    }

    private fun <T> MutableList<T>.addCapped(item: T) {
        add(item)
        if (size > OverlayConstants.MAX_UNDO_STACK) removeAt(0)
    }
}
