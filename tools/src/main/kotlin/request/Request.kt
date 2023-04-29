package request

import organization.Organization
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
    val key: String,
    val primArgs: List<String> = listOf(),
    val organization: Organization? = null,
    val script: List<Request> = listOf(),
)
