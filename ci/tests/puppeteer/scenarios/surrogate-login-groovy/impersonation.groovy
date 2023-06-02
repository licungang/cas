import org.apereo.cas.authentication.principal.*

def isWildcardAuthorized(Object... args) {
    def surrogate = args[0].toString()
    def principal = args[1] as Principal
    def logger = args[2]

    logger.info("Checking wildcard access {}", surrogate)
    return principal.id.equals("casuser4")
}

def canAuthenticate(Object... args) {
    def surrogate = args[0].toString()
    def principal = args[1] as Principal
    def service = args[2] as Service
    def logger = args[3]

    logger.info("Checking surrogate access {}", surrogate)
    def accounts = getAccounts(principal.id, logger)
    return accounts.contains(surrogate)
}


def getAccounts(Object... args) {
    def user = args[0].toString()
    def logger = args[1]

    logger.info("Getting accounts for {}", user)
    switch (user) {
        case "casuser1":
            return ["casuser2", "casuser3"]
        case "casuser2":
            return ["casuser3"]
        case "casuser4":
            return ["*"]
        default:
            return []
    }
}
