rootProject.name = "itmo-is25-highload"
include("storage-service")
include("client")
include("storage-service:server-component")
findProject(":storage-service:server-component")?.name = "server-component"
include("storage-service:storage-component")
findProject(":storage-service:storage-component")?.name = "storage-component"
include("storage-service:client-component")
findProject(":storage-service:client-component")?.name = "client-component"
include("storage-service:storage-core")
findProject(":storage-service:storage-core")?.name = "storage-core"
include("storage-service:storage-rpc-proxy")
findProject(":storage-service:storage-rpc-proxy")?.name = "storage-rpc-proxy"
include("storage-service:storage-redis")
findProject(":storage-service:storage-redis")?.name = "storage-redis"
include("storage-service:storage-lsm-core")
