package org.sagebionetworks.assessmentmodel.navigation

import org.sagebionetworks.assessmentmodel.*
import org.sagebionetworks.assessmentmodel.survey.ReservedNavigationIdentifier

open class NodeNavigator(val node: NodeContainer) : Navigator {

    override fun node(identifier: String): Node?
        = node.children.firstOrNull { it.identifier == identifier }

    override fun nodeAfter(currentNode: Node?, branchResult: BranchNodeResult): NavigationPoint {
        val next = nextNode(currentNode, branchResult, false)
        var direction = NavigationPoint.Direction.Forward
        if (next != null && currentNode != null && node.children.indexOf(next) < node.children.indexOf(currentNode)) {
            direction = NavigationPoint.Direction.Backward
        } else if (shouldExitEarly(currentNode, branchResult, false)) {
            direction = NavigationPoint.Direction.Exit
        }
        val start = next?.let { backgroundActionsToStart(it, currentNode == null) }?.toSet()
        val stop = backgroundActionsToStop(next)?.toSet()
        val asyncActionNavigations = if (start != null || stop != null) {
            setOf(AsyncActionNavigation(node.identifier, start, stop))
        } else null
        return NavigationPoint(
            node = next,
            branchResult = branchResult,
            direction = direction,
            asyncActionNavigations = asyncActionNavigations
        )
    }

    override fun nodeBefore(currentNode: Node?, branchResult: BranchNodeResult): NavigationPoint {
        val previous = previousNode(currentNode, branchResult)
        val direction = NavigationPoint.Direction.Backward
        return NavigationPoint(node = previous, branchResult = branchResult, direction = direction)
    }

    override fun hasNodeAfter(currentNode: Node, branchResult: BranchNodeResult): Boolean {
        return nextNode(currentNode, branchResult, true) != null
    }

    override fun allowBackNavigation(currentNode: Node, branchResult: BranchNodeResult): Boolean {
        return previousNode(currentNode, branchResult) != null
    }

    override fun progress(currentNode: Node, branchResult: BranchNodeResult): Progress? {
        val parentNode = node
        val children = node.children
        val isEstimated = false

        val progressMarkers = parentNode.progressMarkers ?: return null
        val nodeIdx = children.lastIndexOf(currentNode)
        if (nodeIdx == -1) return null
        val nodeList = children.subList(0, nodeIdx+1).map { it.identifier }
        val idx = progressMarkers.indexOfLast { nodeList.contains(it) }

        // If the nearest marker before the current node wasn't found return null.
        if (idx == -1) return null
        // If the current node is *after* the last marker return null.
        if ((idx+1 == progressMarkers.count()) && (progressMarkers.last() != currentNode.identifier)) return null
        // Else return the progress.
        return Progress(
                current = idx,
                total = progressMarkers.count(),
                isEstimated = isEstimated)
    }

    // MARK: Node navigation

    /**
     * The navigation rule (if any) associated with this node.
     */
    open fun navigationRuleFor(node: Node, parentResult: BranchNodeResult) : NavigationRule? {
        val result = parentResult.pathHistoryResults.lastOrNull { it.identifier == node.resultId() }
        return if (result is ResultNavigationRule && result.nextNodeIdentifier != null) {
            result
        } else {
            (node as? NavigationRule)
        }
    }

    private val children
        get() = node.children

    private fun shouldExitEarly(currentNode: Node?, parentResult: BranchNodeResult, isPeeking: Boolean): Boolean
        = ReservedNavigationIdentifier.Exit.matching(nextNodeIdentifier(currentNode, parentResult, isPeeking))

    private fun nextNodeIdentifier(currentNode: Node?, parentResult: BranchNodeResult, isPeeking: Boolean): String?
        = currentNode?.let { navigationRuleFor(it, parentResult) }?.nextNodeIdentifier(parentResult, isPeeking)

    private fun nextNode(currentNode: Node?, parentResult: BranchNodeResult, isPeeking: Boolean): Node? {
        val nextIdentifier = nextNodeIdentifier(currentNode, parentResult, isPeeking)
        return if (nextIdentifier != null) {
            node(nextIdentifier)
        } else if (currentNode == null) {
            children.firstOrNull()
        } else {
            val idx = children.indexOf(currentNode)
            if ((idx >= 0) && (idx + 1 < children.count())) children[idx + 1] else null
        }
    }

    private fun previousNode(currentNode: Node?, parentResult: BranchNodeResult): Node? {
        return when {
            currentNode == null -> {
                parentResult.pathHistoryResults.lastOrNull()?.let { marker ->
                    children.lastOrNull { it.resultId() == marker.identifier }
                } ?: children.firstOrNull()
            }
            parentResult.path.count() == 0 -> {
                val idx = children.indexOf(currentNode)
                if (idx - 1 >= 0) children[idx - 1] else null
            }
            else -> {
                val currentResultIndex = parentResult.path.indexOfLast {
                    it.identifier == currentNode.identifier && it.direction == NavigationPoint.Direction.Forward
                }
                if (currentResultIndex <= 0) null else {
                    val resultBefore = parentResult.path[currentResultIndex - 1]
                    children.lastOrNull { it.identifier == resultBefore.identifier }
                }
            }
        }
    }

    // MARK: Async action handling

    open val asyncActionContainer: AsyncActionContainer?
        get() = (node as? AsyncActionContainer)

    open fun backgroundActionsToStart(nextNode: Node, isFirstNode: Boolean): List<AsyncActionConfiguration>?
            = asyncActionContainer?.backgroundActions?.filter {
        (nextNode.identifier == it.startStepIdentifier) || (isFirstNode && it.startStepIdentifier == null)
    }

    open fun backgroundActionsToStop(nextNode: Node?): List<AsyncActionConfiguration>?
            = asyncActionContainer?.backgroundActions?.filter {
        (it is RecorderConfiguration) && (nextNode?.identifier == it.stopStepIdentifier)
    }
}