import com.e13mort.gitlab_report.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

data class TestProject(
    val id: String,
    val name: String,
    val branches: Branches,
    val mergeRequests: MergeRequests
) : Project {
    override fun id(): String = id

    override fun name(): String = name

    override fun branches(): Branches = branches

    override fun mergeRequests(): MergeRequests = mergeRequests
}

data class TestBranches(
    val branches: List<Branch>
) : Branches {
    override suspend fun count(): Long = branches.size.toLong()

    override suspend fun values(): Flow<Branch> = branches.asFlow()
}

data class TestBranch(
    val name: String
) : Branch {
    override fun name(): String = name
}

data class TestMergeRequests(
    val project: Project,
    val requests: List<MergeRequest>
) : MergeRequests {
    override suspend fun project(): Project = project

    override suspend fun count(): Long = requests.size.toLong()

    override suspend fun values(): Flow<MergeRequest> = requests.asFlow()
}

data class TestMergeRequest(
    val id: String,
    val state: MergeRequest.State,
    val sourceBranch: Branch,
    val targetBranch: Branch,
    val createdTime: Long,
    val closedTime: Long?,
    val assignees: List<User>,
    val events: List<MergeRequestEvent>
) : MergeRequest {
    override fun id(): String = id

    override fun state(): MergeRequest.State = state

    override fun sourceBranch(): Branch = sourceBranch

    override fun targetBranch(): Branch = targetBranch

    override fun createdTime(): Long = createdTime

    override fun closedTime(): Long? = closedTime

    override fun assignees(): List<User> = assignees

    override fun events(): List<MergeRequestEvent> = events
}

data class TestUser(
    val id: Long,
    val name: String,
    val userName: String
) : User {
    override fun id(): Long = id

    override fun name(): String = name

    override fun userName(): String = userName

}

data class TestMergeRequestEvent(
    val id: Long,
    val type: MergeRequestEvent.Type,
    val timeMillis: Long,
    val user: User,
    val content: String
) : MergeRequestEvent {
    override fun id(): Long = id

    override fun type(): MergeRequestEvent.Type = type

    override fun timeMillis(): Long = timeMillis

    override fun user(): User = user

    override fun content(): String = content

}