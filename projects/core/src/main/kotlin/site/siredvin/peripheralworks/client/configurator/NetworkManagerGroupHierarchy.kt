package site.siredvin.peripheralworks.client.configurator

data class NetworkManagerGroupLeaf(val fullName: String, val segments: List<String>)

data class NetworkManagerGroupNode(
    val segment: String,
    val path: List<String>,
    val group: NetworkManagerGroupLeaf?,
    val children: List<NetworkManagerGroupNode>,
)

class NetworkManagerGroupHierarchy private constructor(
    val roots: List<NetworkManagerGroupNode>,
    val leaves: List<NetworkManagerGroupLeaf>,
) {
    fun search(query: String): List<NetworkManagerGroupLeaf> = leaves.filter { it.fullName.contains(query, ignoreCase = true) }

    companion object {
        fun build(groups: Collection<String>, delimiter: String): NetworkManagerGroupHierarchy {
            val leaves = groups.distinct().sorted().map { NetworkManagerGroupLeaf(it, split(it, delimiter)) }
            val root = MutableNode("")
            leaves.forEach { leaf ->
                var node = root
                leaf.segments.forEach { segment -> node = node.children.getOrPut(segment) { MutableNode(segment) } }
                node.group = leaf
            }
            return NetworkManagerGroupHierarchy(root.freeze(emptyList()).children, leaves)
        }

        private fun split(name: String, delimiter: String): List<String> {
            if (delimiter.isEmpty()) return listOf(name)
            val result = mutableListOf<String>()
            var start = 0
            while (true) {
                val end = name.indexOf(delimiter, start)
                if (end < 0) {
                    result.add(name.substring(start))
                    return result
                }
                result.add(name.substring(start, end))
                start = end + delimiter.length
            }
        }
    }

    private class MutableNode(val segment: String) {
        var group: NetworkManagerGroupLeaf? = null
        val children = sortedMapOf<String, MutableNode>()

        fun freeze(parentPath: List<String>): NetworkManagerGroupNode {
            val path = if (parentPath.isEmpty() && segment.isEmpty()) emptyList() else parentPath + segment
            return NetworkManagerGroupNode(segment, path, group, children.values.map { it.freeze(path) })
        }
    }
}
