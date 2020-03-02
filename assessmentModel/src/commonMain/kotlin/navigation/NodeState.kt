package org.sagebionetworks.assessmentmodel.navigation

import org.sagebionetworks.assessmentmodel.*
import org.sagebionetworks.assessmentmodel.survey.Question
import org.sagebionetworks.assessmentmodel.survey.QuestionStateImpl

/**
 * The [RootNodeController] is a platform-specific implementation of the UI that is described by the [Assessment] model.
 */
interface RootNodeController {

    /**
     * Does this controller know how to handle the given node?
     *
     * A node may be used to define UI that is not applicable on all platforms. For example, showing a section of a
     * survey on the same screen on a tablet, but across a sequence of screens on a phone where the available space
     * is limited.
     */
    fun canHandle(node: Node): Boolean

    /**
     * Is there a custom [NodeState] for the given [node]? This can be used to return a custom implementation such as
     * a node state that blocks forward progress until a login service call is returned.
     */
    fun customNodeStateFor(node: Node, parent: BranchNodeState): NodeState? = null

    /**
     * Handle going forward to the given [nodeState] with appropriate UI, View, and animations.
     */
    fun handleGoForward(nodeState: NodeState,
                        requestedPermissions: Set<Permission>? = null,
                        asyncActionNavigations: Set<AsyncActionNavigation>? = null)

    /**
     * Handle going back to the given [nodeState] with appropriate UI, View, and animations.
     */
    fun handleGoBack(nodeState: NodeState,
                     requestedPermissions: Set<Permission>? = null,
                     asyncActionNavigations: Set<AsyncActionNavigation>? = null)

    /**
     * Handle finishing the [Assessment]. Save state and dismiss the view.
     */
    fun handleFinished(reason: FinishedReason, nodeState: NodeState, error: Error? = null)
}

enum class FinishedReason {
    Complete, Error, EarlyExit, Discarded, SaveProgress
}

interface NodeState {

    /**
     * The [node] tied to *this* [NodeState]. For any given [NodeState], there is one and only one [Node] associated
     * with that state.
     */
    val node: Node

    /**
     * The [parent] (if any) for the node chain.
     */
    val parent: BranchNodeState?

    /**
     * The [Result] associated with [node] for this component in the node chain. This is the result that is added to the
     * path history. Since this is a pointer to an object, that object might be mutable.
     */
    val currentResult: Result

    /**
     * Method to call when the participant taps the "Next" button or a timed step is completed.
     */
    fun goForward(requestedPermissions: Set<Permission>? = null,
                  asyncActionNavigations: Set<AsyncActionNavigation>? = null)

    /**
     * Method to call when the participant taps the "Back" button or the active step gets a signal to go back to the
     * previous node in the navigation.
     */
    fun goBackward(requestedPermissions: Set<Permission>? = null,
                   asyncActionNavigations: Set<AsyncActionNavigation>? = null)
}

fun NodeState.goIn(direction: NavigationPoint.Direction,
                   requestedPermissions: Set<Permission>? = null,
                   asyncActionNavigations: Set<AsyncActionNavigation>? = null) {
    if (direction == NavigationPoint.Direction.Forward) {
        goForward(requestedPermissions, asyncActionNavigations)
    }
    else {
        goBackward(requestedPermissions, asyncActionNavigations)
    }
}

fun NodeState.root() :  NodeState {
    var thisPath: NodeState = this
    while (thisPath.parent != null) {
        thisPath = thisPath.parent!!
    }
    return thisPath
}

interface BranchNodeState : NodeState {

    /**
     * The controller for running the full flow of steps and nodes.
     */
    var rootNodeController: RootNodeController?

    /**
     * Override the [node] to require return of a [BranchNode].
     */
    override val node: BranchNode

    /**
     * The current child that defines the current navigation state.
     */
    val currentChild: NodeState?

    /**
     * Override the [currentResult] to require return of a [BranchNodeResult]
     */
    override val currentResult: BranchNodeResult
}

open class LeafNodeStateImpl(override val node: Node, override val parent: BranchNodeState) : NodeState {
    override val currentResult: Result by lazy { node.createResult() }

    override fun goForward(requestedPermissions: Set<Permission>?,
                           asyncActionNavigations: Set<AsyncActionNavigation>?) {
        parent.goForward(requestedPermissions, asyncActionNavigations)
    }

    override fun goBackward(requestedPermissions: Set<Permission>?,
                            asyncActionNavigations: Set<AsyncActionNavigation>?) {
        parent.goBackward(requestedPermissions, asyncActionNavigations)
    }
}

open class BranchNodeStateImpl(override val node: BranchNode, final override val parent: BranchNodeState? = null) : BranchNodeState {

    override val currentResult: BranchNodeResult by lazy { node.createResult() }
    private val navigator: Navigator by lazy { node.getNavigator() }

    override var currentChild: NodeState? = null
        protected set

    override var rootNodeController: RootNodeController?
        get() = parent?.rootNodeController ?: _rootNodeController
        set(value) { _rootNodeController = value }
    private var _rootNodeController: RootNodeController? = null

    override fun goForward(requestedPermissions: Set<Permission>?,
                           asyncActionNavigations: Set<AsyncActionNavigation>?) {
        val next = getNextNode()
        unionNavigationSets(next, requestedPermissions, asyncActionNavigations)
        if (next.node != null) {
            // Go to next node if it is not null.
            moveTo(next)
        } else {
            finish(next)
        }
    }

    /**
     * Move to the given node.
     *
     * Throws: [NullPointerException] if the [NavigationPoint.node] or [rootNodeController] are null.
     */
    open fun moveTo(navigationPoint: NavigationPoint) {
        val controller = rootNodeController ?: throw NullPointerException("Unexpected null rootNodeController")
        val node = navigationPoint.node ?: throw NullPointerException("Unexpected null navigationPoint.node")
        if (controller.canHandle(node)) {
            // If the controller can handle the node state then it is responsible for showing it. Just set the current
            // child and hand off control to the root node controller.
            getLeafNodeState(navigationPoint)?.let { nodeState ->
                currentChild = nodeState
                if (navigationPoint.direction == NavigationPoint.Direction.Forward) {
                    controller.handleGoForward(nodeState, navigationPoint.requestedPermissions, navigationPoint.asyncActionNavigations)
                } else {
                    controller.handleGoBack(nodeState, navigationPoint.requestedPermissions, navigationPoint.asyncActionNavigations)
                }
            } ?: run {
                TODO("syoung 02/10/2020 Not implemented. Handle case where a step is skipped by the branch state.")
            }
        } else {
            // Otherwise, see if we have a node to move to.
            getBranchNodeState(navigationPoint)?.let { nodeState ->
                currentChild = nodeState
                nodeState.goIn(navigationPoint.direction, navigationPoint.requestedPermissions, navigationPoint.asyncActionNavigations)
            } ?: run {
                TODO("syoung 02/04/2020 Not implemented. Handle case where a step is skipped by the root controller.")
            }
        }
    }

    /**
     * Finish any navigation required at this level. This will check to see if the returned navigation point requires
     * exiting the entire run or just that this section is finished.
     */
    open fun finish(navigationPoint: NavigationPoint) {
        when {
            navigationPoint.direction == NavigationPoint.Direction.Exit ->
                rootNodeController?.handleFinished(FinishedReason.EarlyExit, this)
            parent == null ->
                rootNodeController?.handleFinished(FinishedReason.Complete, this)
            else ->
                parent.goIn(navigationPoint.direction, navigationPoint.requestedPermissions, navigationPoint.asyncActionNavigations)
        }
    }

    /**
     * Get the next node to show after the current node.
     */
    open fun getNextNode(): NavigationPoint {
        appendChildResultIfNeeded()
        val currentNode = currentChild?.node
        return if (currentNode == null) navigator.start() else navigator.nodeAfter(currentNode, currentResult)
    }

    /**
     * Get the leaf node for the given [navigationPoint]. This method assumes that the [NavigationPoint.node] is not
     * null.
     */
    open fun getLeafNodeState(navigationPoint: NavigationPoint): NodeState? {
        val node = navigationPoint.node ?: throw NullPointerException("Unexpected null navigationPoint.node")
        return rootNodeController?.customNodeStateFor(node, this) ?: when (node) {
            is Question -> QuestionStateImpl(node, this)
            else -> LeafNodeStateImpl(node, this)
        }
    }

    /**
     * Look to see if the next node to move to is a navigation point that can return a branch node state. The base
     * class looks for conformance to the [NodeContainer] interface and the [Assessment] interface in that order.
     */
    open fun getBranchNodeState(navigationPoint: NavigationPoint): BranchNodeState? {
        return (navigationPoint.node as? BranchNode)?.let { BranchNodeStateImpl(it, this) }
    }

    /**
     * Union the requested permissions and async actions with the returned navigation point.
     */
    open fun unionNavigationSets(navigationPoint: NavigationPoint,
                            requestedPermissions: Set<Permission>?,
                            asyncActionNavigations: Set<AsyncActionNavigation>?) {
        if (requestedPermissions != null) {
            TODO("syoung 02/05/2020 Add unit test")
            navigationPoint.requestedPermissions = requestedPermissions.plus(navigationPoint.requestedPermissions ?: setOf())
        }
        if (asyncActionNavigations != null) {
            TODO("syoung 02/05/2020 Add unit test")
            navigationPoint.asyncActionNavigations = asyncActionNavigations.union(navigationPoint.asyncActionNavigations ?: setOf())
        }
    }

    /**
     * Add the current child result to the end of the path results for this [currentResult]. If the last result has the
     * same identifier as the current child result then this should *not* append. The assumption is that the controller
     * has added the *desired* result but not edited the current child result.
     */
    open fun appendChildResultIfNeeded() {
        currentChild?.currentResult?.let { childResult ->
            if (currentResult.pathHistoryResults.lastOrNull()?.let { it.identifier != childResult.identifier } != false) {
                currentResult.pathHistoryResults.add(childResult)
            }
        }
    }

    override fun goBackward(requestedPermissions: Set<Permission>?,
                            asyncActionNavigations: Set<AsyncActionNavigation>?) {
        TODO("syoung 02/04/2020 Not implemented.")
    }
}