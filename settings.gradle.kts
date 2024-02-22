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
findProject(":storage-service:storage-lsm-core")?.name = "storage-lsm-core"
include("storage-service:storage-lsm-replication-component")
findProject(":storage-service:storage-lsm-replication-component")?.name = "storage-lsm-replication-component"
include("rate-limit:rate-limit-component")
findProject(":rate-limit:rate-limit-component")?.name = "rate-limit-component"
include("rate-limit:rate-limit-server-component")
findProject(":rate-limit:rate-limit-server-component")?.name = "rate-limit-server-component"
include("rate-limit")
include("rate-limit:rate-limit-client-component")
findProject(":rate-limit:rate-limit-client-component")?.name = "rate-limit-client-component"
include("target-service")
include("target-service:target-service-server-component")
findProject(":target-service:target-service-server-component")?.name = "target-service-server-component"
include("target-service:target-service-client-component")
findProject(":target-service:target-service-client-component")?.name = "target-service-client-component"
include("target-client")
include("common")

