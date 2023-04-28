package request

import organization.Organization
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val name: String,
    val primArgs: List<String>,
    val organization: Organization?,
    val key: String,
)
